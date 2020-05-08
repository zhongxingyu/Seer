 package cz.cuni.mff.odcleanstore.engine.pipeline;
 
 
 import java.util.Date;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.engine.Engine;
 import cz.cuni.mff.odcleanstore.engine.Service;
 import cz.cuni.mff.odcleanstore.engine.ServiceState;
 import cz.cuni.mff.odcleanstore.engine.common.FormatHelper;
 import cz.cuni.mff.odcleanstore.engine.db.model.GraphStates;
 import cz.cuni.mff.odcleanstore.engine.db.model.PipelineCommand;
 import cz.cuni.mff.odcleanstore.engine.db.model.PipelineErrorTypes;
 
 /**
  *  @author Petr Jerman
  */
 public final class PipelineService extends Service implements Runnable {
 	
 	private static final String FORMAT_ERROR = "Error during formating message for log";
 
 	private static final Logger LOG = LoggerFactory.getLogger(PipelineService.class);
 	
 	private Object waitForGraphLock;
 	private Object waitPenaltyLock;
 	private PipelineGraphTransformerExecutor activeTransformerExecutor;
 	
 	public PipelineService(Engine engine) {
 		super(engine, "PipelineService");
 		this.waitForGraphLock = new Object();
 		this.waitPenaltyLock = new Object();
 		this.activeTransformerExecutor = null;
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		synchronized (waitForGraphLock) {
 			waitForGraphLock.notify();
 		}
 		synchronized (waitPenaltyLock) {
 			waitPenaltyLock.notify();
 		}
 		if (activeTransformerExecutor != null) {
 			activeTransformerExecutor.shutdown();
 		}
 	}
 
 	public void notifyAboutGraphForPipeline() {
 		synchronized (waitForGraphLock) {
 			waitForGraphLock.notify();
 		}
 	}
 	
 	private PipelineGraphStatus waitForGraphForPipeline() throws PipelineGraphStatusException, InterruptedException {
 		while(getServiceState() == ServiceState.RUNNING) {
 			PipelineGraphStatus status  = PipelineGraphStatus.getNextGraphForPipeline(engine.getEngineUuid());
 			if (status != null) {
 				return status;
 			}
 			synchronized (waitForGraphLock) {
 				if (getServiceState() == ServiceState.RUNNING) {
 					waitForGraphLock.wait(ConfigLoader.getConfig().getEngineGroup().getLookForGraphInterval());
 				}
 			}
 		}
 		return null;	
 	}
 	
 	@Override
 	public void execute() {
 		long _waitPenalty = 0;
 		while (getServiceState() == ServiceState.RUNNING) {
 			PipelineGraphStatus status = null;
 			try {
 				if (_waitPenalty > 1) {
 					synchronized (waitPenaltyLock) {
 						if (getServiceState() == ServiceState.RUNNING) {
 							waitPenaltyLock.wait(ConfigLoader.getConfig().getEngineGroup().getSecondCrashPenalty());
 						}
 					}
 				}
 				while ((status = waitForGraphForPipeline()) != null) {
 					executePipeline(status);
 					_waitPenalty = 0;
 				}
 			} catch (Exception e) {
 				if (status != null && status.isResetPipelineRequest()) {
 					LOG.info(format("--- reset pipeline request detected ---", status));
 				}
 				else {
 					_waitPenalty++;
 					LOG.error(FormatHelper.formatExceptionForLog(e, format("crashed", status)));
 				}
 			}
 		}
 	}
 	
 	private void executePipeline(PipelineGraphStatus status) 
 			throws PipelineGraphManipulatorException, PipelineGraphTransformerExecutorException, PipelineGraphStatusException {
 		
 		PipelineGraphManipulator manipulator = new PipelineGraphManipulator(status); 
 		while (getServiceState() == ServiceState.RUNNING) {
 			switch (status.getState()) {
 				case DELETING:
 					executeStateDeleting(manipulator, status);
 					break;
 				case PROCESSING:
 					executeStateProcessing(manipulator, status);
 					break;
 				case PROCESSED:
 					executeStateProcessed(manipulator, status);
 					break;
 				case PROPAGATED:
 					executeStatePropagated(manipulator, status);
 					break;
 				case OLDGRAPHSPREFIXED:
 					executeStateOldGraphsPrefixed(manipulator, status);
 					break;
 				case NEWGRAPHSPREPARED:
 					executeStateNewGraphsPrepared(manipulator, status);
 					break;					
 				case DIRTY:
 					executeStateDirty(manipulator, status);
 					break;
 				default:
 					return;
 			}
 		}
 	}
 	
 	private void executeStateDeleting(PipelineGraphManipulator manipulator, PipelineGraphStatus status)
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException {
 
 		LOG.info(format("deleting started", status));
 		manipulator.clearGraphsInCleanDB();
 		manipulator.clearGraphsInDirtyDB();
 		manipulator.deleteInputFiles();
 		status.setNoDirtyState(GraphStates.DELETED);
 		LOG.info(format("deleting successfully finished", status));
 	}
 	
 	private void executeStateProcessing(PipelineGraphManipulator manipulator, PipelineGraphStatus status)
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException, PipelineGraphTransformerExecutorException {
 		
 	    LOG.info("----------------------------------------");
 		LOG.info(format("processing in dirty db started", status));
 		manipulator.clearGraphsInDirtyDB();
 		if (!status.isInCleanDb()) {
 			status.deleteAttachedGraphs();
 		}
 		executeStateProcessingLoadData(manipulator, status);
 		executeStateProcessingTransformers(status);
 		if (status.isMarkedForDeleting()) {
 			status.setNoDirtyState(GraphStates.DELETING);
 			LOG.info(format("processing in dirty db successfully finished, graph is marked for deleting", status));
 		}
 		else {
 			status.setNoDirtyState(GraphStates.PROCESSED);
 			LOG.info(format("processing in dirty db successfully finished", status));
 		}
 	}
 	
 	private void executeStateProcessingLoadData(PipelineGraphManipulator manipulator, PipelineGraphStatus status)
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException {
 
 		try {
 			manipulator.loadGraphsIntoDirtyDB();
 		} catch (PipelineGraphManipulatorException e) {
 			if(isRunnningAndDbInstancesAvailable(true)) {
 				String message = formatExceptionForDB("data loading failure", e, null, status);
 				status.setDirtyState(PipelineErrorTypes.DATA_LOADING_FAILURE, message);
 			}
 			throw e;
 		}
 	}
 	
 	private void executeStateProcessingTransformers(PipelineGraphStatus status)
 			throws PipelineGraphTransformerExecutorException, PipelineGraphStatusException  {
 
 		try {
 			this.activeTransformerExecutor = new PipelineGraphTransformerExecutor(status);
 			activeTransformerExecutor.execute();
 
 		} catch (PipelineGraphTransformerExecutorException e) {
 			if(isRunnningAndDbInstancesAvailable(true)) {
 				String message = formatExceptionForDB("transformer processing failure", e, e.getCommand(), status);
 				status.setDirtyState(PipelineErrorTypes.TRANSFORMER_FAILURE, message);
 			}
 			throw e;
 		}
 		finally {
 			this.activeTransformerExecutor = null;
 		}
 	}
 
 	private void executeStateProcessed(PipelineGraphManipulator manipulator, PipelineGraphStatus status) 
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException {
 		
 		LOG.info(format("copying temporary new data from dirty to clean db started", status));
 		
 		try {
 			manipulator.clearNewGraphsInCleanDB();
 			manipulator.copyNewGraphsFromDirtyToCleanDB();
 		} catch (PipelineGraphManipulatorException e) {
 			if(isRunnningAndDbInstancesAvailable(true)) {
 				String message = formatExceptionForDB("copy to clean db", e, null, status);
 				status.setDirtyState(PipelineErrorTypes.COPY_TO_CLEAN_DB_FAILURE, message);
 			}
 			throw e;
 		}
 		
 		status.setNoDirtyState(GraphStates.PROPAGATED);
 		LOG.debug(format("copying temporary new data from dirty to clean db successfully finished", status));
 	}
 
 	private void executeStatePropagated(PipelineGraphManipulator manipulator, PipelineGraphStatus status)
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException {
 
 		LOG.info(format("renaming old graphs in clean db started", status));
 		manipulator.renameGraphsToOldGraphsInCleanDB();
 		status.setNoDirtyState(GraphStates.OLDGRAPHSPREFIXED);
 		LOG.debug(format("renaming old graphs in clean db successfully finished", status));
 	}
 	
 	private void executeStateOldGraphsPrefixed(PipelineGraphManipulator manipulator, PipelineGraphStatus status)
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException {
 
 		LOG.info(format("clearing temporary prefix of new graphs and clearing old graphs in clean db started", status));
 		manipulator.renameNewGraphsToGraphsInCleanDB();
 		manipulator.clearOldGraphsInCleanDB();
 		status.setNoDirtyState(GraphStates.NEWGRAPHSPREPARED);
 		LOG.debug(format("clearing temporary prefix of new graphs and clearing old graphs in clean dbsuccessfully finished", status));
 	}
 	
 	private void executeStateNewGraphsPrepared(PipelineGraphManipulator manipulator, PipelineGraphStatus status)
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException {
 
 		LOG.info(format("cleaning dirty db started", status));
 		manipulator.clearGraphsInDirtyDB();
 		manipulator.deleteInputFiles();
 		if (status.setNoDirtyState(GraphStates.FINISHED) == GraphStates.QUEUED) {
 			LOG.info(format("pipeline successfully finished and graf is returned into queue due restart pipeline request", status));
 		} else {
 			LOG.info(format("pipeline successfully finished", status));
 		}
 	}
 
 	private void executeStateDirty(PipelineGraphManipulator manipulator, PipelineGraphStatus status)
 			throws PipelineGraphManipulatorException, PipelineGraphStatusException {
 
 		LOG.info(format("cleaning dirty graph started", status));
 		manipulator.clearNewGraphsInCleanDB();
 		manipulator.clearGraphsInDirtyDB();
		status.setNoDirtyState(GraphStates.WRONG);
		if (status.setNoDirtyState(GraphStates.FINISHED) == GraphStates.QUEUED) {
 			LOG.info(format("cleaning dirty graph successfully finished, graf is returned into queue due restart pipeline request", status));
 		} else {
 			LOG.info(format("cleaning dirty graph successfully finished, graph moved to WRONG state", status));
 		}
 	}
 	
 	private String format(String message, PipelineGraphStatus status) {
 		try {
 			StringBuilder sb = new StringBuilder();
 			if (status != null) {
 				if(status.getPipelineId() != null) {
 					sb.append("Pipeline ");
 					sb.append(status.getPipelineLabel());
 					sb.append(" - ");
 				}
 				sb.append(message);
 				return FormatHelper.formatGraphMessage(sb.toString(), status.getUuid(), status.isInCleanDbBeforeProcessing());
 			}
 			return  message;
 		} catch(Exception ie) {
 			return  message;
 		} 
 	}
 	
 	private static String formatExceptionForDB(String message, Throwable exception, PipelineCommand command, PipelineGraphStatus status) {
 		try {
 			StringBuilder sb = new StringBuilder();
 			
 			sb.append("Date: ");
 			sb.append(new Date());
 			sb.append("\n");
 			
 			sb.append("Pipeline-id: ");
 			if(status.getPipelineId() != null) {
 				sb.append(status.getPipelineId());
 				sb.append("; label: ");
 				sb.append(status.getPipelineLabel());
 			}
 			sb.append("\n");
 			
 			sb.append("Graph-uuid: ");
 			sb.append(status.getUuid());
 			if (status.isInCleanDbBeforeProcessing()) {
 				sb.append("; existing graph processing");
 			} else {
 				sb.append("; new graph processing");
 			}
 
 			sb.append("\n");
 			
 			if(command != null) {
 				sb.append("Transformer-id: ");
 				sb.append(command.transformerID);
 				sb.append("; label: ");
 				sb.append(command.transformerLabel);
 				sb.append("\n");
 				sb.append("Transformer-instance-id: ");
 				sb.append(command.transformerInstanceID);
 				sb.append("\n");
 			}
 			
 			sb.append("Message: ");
 			sb.append(message);
 			sb.append("\n");
 			sb.append("\n");
 			sb.append("Exception-list: ");
 			while(exception != null) {
 				sb.append("\n        ");
 				sb.append(exception.getClass().getSimpleName());
 				sb.append(" - ");
 				sb.append(exception.getMessage());
 				exception = exception.getCause();
 			}
 			sb.append('\n');
 			return sb.toString();
 		} catch(Exception ie) {
 			return  FORMAT_ERROR;
 		}
 	}
 }
