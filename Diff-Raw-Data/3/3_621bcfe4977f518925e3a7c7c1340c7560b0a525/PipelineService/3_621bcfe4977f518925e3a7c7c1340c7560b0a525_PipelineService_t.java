 package cz.cuni.mff.odcleanstore.engine.pipeline;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 
 import cz.cuni.mff.odcleanstore.engine.Engine;
 import cz.cuni.mff.odcleanstore.engine.InputGraphState;
 import cz.cuni.mff.odcleanstore.engine.Service;
 import cz.cuni.mff.odcleanstore.engine.common.ModuleState;
 import cz.cuni.mff.odcleanstore.engine.common.SimpleVirtuosoAccess;
 import cz.cuni.mff.odcleanstore.engine.inputws.ifaces.Metadata;
 import cz.cuni.mff.odcleanstore.transformer.Transformer;
 import cz.cuni.mff.odcleanstore.vocabulary.DC;
 import cz.cuni.mff.odcleanstore.vocabulary.W3P;
 
 /**
  *  @author Petr Jerman
  */
 public final class PipelineService extends Service implements Runnable {
 
 	private static final Logger LOG = Logger.getLogger(PipelineService.class);
 	
 	private WorkingInputGraphStatus _workingInputGraphStatus;
 	private WorkingInputGraph _workingInputGraph;
 
 	public PipelineService(Engine engine) {
 		super(engine);
 	}
 
 	private Object fromInputWSLocks = new Object();
 
 	public void signalInput() {
 		synchronized (fromInputWSLocks) {
 			fromInputWSLocks.notify();
 		}
 	}
 
 	private String waitForInput() {
 		synchronized (fromInputWSLocks) {
 			try {
 				String uuid;
 				for (uuid = _workingInputGraphStatus.getNextProcessingGraphUuid(); uuid == null; uuid = _workingInputGraphStatus.getNextProcessingGraphUuid()) {
 					fromInputWSLocks.wait();
 				}
 				return uuid;
 			} catch (Exception e) {
 				return null;
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				synchronized (this) {
 					if (getModuleState() != ModuleState.NEW && getModuleState() != ModuleState.CRASHED) {
 						return;
 					}
 					setModuleState(ModuleState.INITIALIZING);
 					LOG.info("PipelineService initializing");
 				}
 
 				_workingInputGraphStatus = new WorkingInputGraphStatus("DB.ODCLEANSTORE");
 				_workingInputGraph = new WorkingInputGraph();
 
 				String graphsForRecoveryUuid = _workingInputGraphStatus.getWorkingTransformedGraphUuid();
 				if (graphsForRecoveryUuid != null) {
 					setModuleState(ModuleState.RECOVERY);
 					LOG.info("PipelineService starts recovery");
 					recovery(graphsForRecoveryUuid);
 				}
 				setModuleState(ModuleState.RUNNING);
 				LOG.info("PipelineService running");
 				runPipeline();
 				setModuleState(ModuleState.STOPPED);
 				LOG.info("PipelineService stopped");
 			} catch (Exception e) {
 				_workingInputGraphStatus.setWorkingTransformedGraph(null);
 				setModuleState(ModuleState.CRASHED);
 				String message = String.format("PipelineService crashed - %s", e.getMessage());
 				e.printStackTrace();
 				LOG.error(message);
 			}
 		}
 	}
 
 	private void recovery(String uuid) throws Exception {
 
 		InputGraphState state = _workingInputGraphStatus.getState(uuid);
 
 		switch (state) {
 		case PROCESSING:
 			_workingInputGraph.deleteGraphsFromDirtyDB(_workingInputGraphStatus.getWorkingAttachedGraphNames());
 			_workingInputGraph.deleteGraphFromDirtyDB(Engine.DATA_PREFIX + uuid);
 			_workingInputGraph.deleteGraphFromDirtyDB(Engine.METADATA_PREFIX + uuid);
 
 			_workingInputGraphStatus.deleteWorkingAttachedGraphNames();
 			_workingInputGraphStatus.setState(uuid, InputGraphState.IMPORTED);
 			LOG.info("PipelineService ends recovery from interrupted processing");
 			break;
 		case PROCESSED:
 			processProcessedState(uuid);
 		case PROPAGATED:
 			processPropagatedState(uuid);
 			LOG.info("PipelineService ends recovery from interrupted copying graph from dirty to clean database instance");
 			break;
 		case DELETING:
 			processDeletingState(uuid);
 			LOG.info("PipelineService ends recovery from interrupted deleting graph");
 			break;
 		case DIRTY:
 			_workingInputGraph.deleteGraphsFromDirtyDB(_workingInputGraphStatus.getWorkingAttachedGraphNames());
 			_workingInputGraph.deleteGraphFromDirtyDB(Engine.DATA_PREFIX + uuid);
 			_workingInputGraph.deleteGraphFromDirtyDB(Engine.METADATA_PREFIX + uuid);
 
 			_workingInputGraphStatus.deleteWorkingAttachedGraphNames();
 			_workingInputGraphStatus.setState(uuid, InputGraphState.WRONG);
 			LOG.info("PipelineService ends recovery from crashed pipeline proccesing");
 			break;
 		}
 	}
 
 	private void runPipeline() throws Exception {
 
 		String uuid = null;
 
 		while ((uuid = waitForInput()) != null) {
		TransformedGraphImpl transformedGraphImpl = null;
 			try {
 				LOG.info(String.format("PipelineService starts processing graph %s", uuid));
 				int pipelineId = _workingInputGraphStatus.getGraphPipelineId(uuid);
 				Collection<TransformerCommand> TransformerCommands = TransformerCommand.getActualPlan("DB.ODCLEANSTORE", pipelineId);
 				loadData(uuid);
 				LOG.info(String.format("PipelineService ends data loading for graph %s", uuid));
 				for (TransformerCommand transformerCommand : TransformerCommands) {
 					transformedGraphImpl = transformedGraphImpl == null ? new TransformedGraphImpl(_workingInputGraphStatus, uuid) : new TransformedGraphImpl(transformedGraphImpl);
 					processTransformer(transformerCommand, transformedGraphImpl);
 					if (transformedGraphImpl.isDeleted()) {
 						break;
 					}
 
 				}
 			} catch (Exception e) {
 				_workingInputGraphStatus.setWorkingTransformedGraph(null);
 				_workingInputGraphStatus.setState(uuid, InputGraphState.DIRTY);
 				throw e;
 			}
 
 			if (transformedGraphImpl != null && transformedGraphImpl.isDeleted()) {
 				processDeletingState(uuid);
 			} else {
 				_workingInputGraphStatus.setState(uuid, InputGraphState.PROCESSED);
 				processProcessedState(uuid);
 				processPropagatedState(uuid);
 			}
 		}
 	}
 
 	private void loadData(String uuid) throws Exception {
 		FileInputStream fin = null;
 		ObjectInputStream ois = null;
 		String inserted = null;
 		Metadata metadata = null;
 		String rdfXmlPayload = null;
 		try {
 			fin = new FileInputStream(Engine.INPUTWS_DIR + uuid + ".dat");
 			ois = new ObjectInputStream(fin);
 			inserted = (String) ois.readObject();
 			metadata = (Metadata) ois.readObject();
 			rdfXmlPayload = (String) ois.readObject();
 		} finally {
 			if (ois != null) {
 				ois.close();
 			}
 			if (fin != null) {
 				fin.close();
 			}
 		}
 
 		SimpleVirtuosoAccess sva = null;
 		try {
 			sva = SimpleVirtuosoAccess.createDirtyDBConnection();
 
 			sva.insertQuad("<" + Engine.DATA_PREFIX + uuid + ">", "<" + W3P.insertedAt + ">", inserted, "<" + Engine.METADATA_PREFIX + uuid + ">");
 			sva.insertQuad("<" + Engine.DATA_PREFIX + uuid + ">", "<" + W3P.insertedBy + ">", "'scraper'", "<" + Engine.METADATA_PREFIX + uuid + ">");
 			for (String source : metadata.source) {
 				sva.insertQuad("<" + Engine.DATA_PREFIX + uuid + ">", "<" + W3P.source + ">", "<" + source + ">", "<" + Engine.METADATA_PREFIX + uuid + ">");
 			}
 			for (String publishedBy : metadata.publishedBy) {
 				sva.insertQuad("<" + Engine.DATA_PREFIX + uuid + ">", "<" + W3P.publishedBy + ">", "<" + publishedBy + ">", "<" + Engine.METADATA_PREFIX + uuid + ">");
 			}
 			if (metadata.license != null) {
 				for (String license : metadata.license) {
 					sva.insertQuad("<" + Engine.DATA_PREFIX + uuid + ">", "<" + DC.license + ">", "<" + license + ">", "<" + Engine.METADATA_PREFIX + uuid + ">");
 				}
 			}
 			if (metadata.rdfXmlProvenance != null) {
 				sva.insertRdfXml(metadata.provenanceBaseUrl, metadata.rdfXmlProvenance, Engine.METADATA_PREFIX + uuid);
 			}
 			sva.insertRdfXml(metadata.dataBaseUrl, rdfXmlPayload, Engine.DATA_PREFIX + uuid);
 			sva.commit();
 		} finally {
 			if (sva != null) {
 				sva.close();
 			}
 		}
 	}
 	
 	private Transformer loadCustomTransformer(TransformerCommand transformerCommand) throws Exception {
 		
 		URL url = new File(transformerCommand.getJarPath()).toURL(); 
 		URLClassLoader loader = new URLClassLoader(new URL[]{url}, getClass().getClassLoader());
 		Class<?> trida = Class.forName(transformerCommand.getFullClassName(), true, loader);
 		Object obj = trida.getConstructor(new Class[]{}).newInstance(new Object[]{});
 		return  obj instanceof Transformer ? (Transformer) obj : null;
 	}
 
 	private void processTransformer(TransformerCommand transformerCommand, TransformedGraphImpl transformedGraphImpl) throws Exception {
 		Transformer transformer = null;
 		
 		if (!transformerCommand.getJarPath().equals(".")) {
 			transformer = loadCustomTransformer(transformerCommand);
 		}
 		else {
 			if (transformerCommand.getFullClassName().equals("cz.cuni.mff.odcleanstore.linker.impl.LinkerImpl")) {
 				transformer = new cz.cuni.mff.odcleanstore.linker.impl.LinkerImpl();
 			} else if (transformerCommand.getFullClassName().equals("cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl")) {
 				transformer = new cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl();
 			}	
 		}
 
 		if (transformer != null) {
 			String path = checkTransformerWorkingDirectory(transformerCommand.getWorkDirPath());
 			TransformationContextImpl context = new TransformationContextImpl(transformerCommand.getConfiguration(), path);
 
 			_workingInputGraphStatus.setWorkingTransformedGraph(transformedGraphImpl);
 			transformer.transformNewGraph(transformedGraphImpl, context);
 			LOG.info(String.format("PipelineService ends proccesing %s transformer on graph %s", transformerCommand.getFullClassName(), transformedGraphImpl.getGraphId()));
 			_workingInputGraphStatus.setWorkingTransformedGraph(null);
 		} else {
 			LOG.warn(String.format("PipelineService - unknown transformer %s ignored", transformerCommand.getFullClassName()));
 		}
 	}
 	
 	private String checkTransformerWorkingDirectory(String dirName) throws PipelineException {
 		try {
 			File file = new File(dirName);
 			if (!file.isAbsolute()) {
 				File curdir = new File("");
 				file = new File(curdir.getAbsolutePath() + File.separator + file.getPath());
 			}
 			
  			if (!file.exists()) {
  				satisfyParent(file);
 				file.mkdir();
 			}
 
 			if (!file.isDirectory()) {
 				throw new PipelineException(String.format(" Transformer working directory %s not exists", dirName));
 			}
 
 			if (!file.canRead()) {
 				throw new PipelineException(String.format(" Cannot read from transformer working directory %s", dirName));
 			}
 
 			if (!file.canWrite()) {
 				throw new PipelineException(String.format(" Cannot write to transformer working directory %s", dirName));
 			}
 			return file.getCanonicalPath();
 		} catch (PipelineException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new PipelineException(e);
 		}
 	}
 	
 	private void satisfyParent(File file) {
 		File parent = file.getParentFile();
 		if(parent != null) satisfyParent(parent);
 		if(!file.exists()) {
 			file.mkdir();
 		}
 	}
 
 	private void processDeletingState(String uuid) throws Exception {
 		_workingInputGraph.deleteGraphsFromDirtyDB(_workingInputGraphStatus.getWorkingAttachedGraphNames());
 		_workingInputGraph.deleteGraphFromDirtyDB(Engine.DATA_PREFIX + uuid);
 		_workingInputGraph.deleteGraphFromDirtyDB(Engine.METADATA_PREFIX + uuid);
 
 		_workingInputGraphStatus.deleteGraphAndWorkingAttachedGraphNames(uuid);
 		LOG.info(String.format("PipelineService ends deleting graph %s", uuid));
 	}
 
 	private void processProcessedState(String uuid) throws Exception {
 		ArrayList<String> graphs = new ArrayList<String>();
 		graphs.addAll(_workingInputGraphStatus.getWorkingAttachedGraphNames());
 		graphs.add(Engine.DATA_PREFIX + uuid);
 		graphs.add(Engine.METADATA_PREFIX + uuid);
 
 		_workingInputGraph.copyGraphsFromDirtyDBToCleanDB(graphs);
 
 		_workingInputGraphStatus.setState(uuid, InputGraphState.PROPAGATED);
 	}
 
 	private void processPropagatedState(String uuid) throws Exception {
 		ArrayList<String> graphs = new ArrayList<String>();
 		graphs.addAll(_workingInputGraphStatus.getWorkingAttachedGraphNames());
 		graphs.add(Engine.DATA_PREFIX + uuid);
 		graphs.add(Engine.METADATA_PREFIX + uuid);
 
 		_workingInputGraph.deleteGraphsFromDirtyDB(graphs);
 		File inputFile = new File(Engine.INPUTWS_DIR + uuid + ".dat");
 		inputFile.delete();
 
 		_workingInputGraphStatus.deleteWorkingAttachedGraphNames();
 		_workingInputGraphStatus.setState(uuid, InputGraphState.FINISHED);
 		LOG.info(String.format("PipelineService has finished graph %s", uuid));
 	}
 }
