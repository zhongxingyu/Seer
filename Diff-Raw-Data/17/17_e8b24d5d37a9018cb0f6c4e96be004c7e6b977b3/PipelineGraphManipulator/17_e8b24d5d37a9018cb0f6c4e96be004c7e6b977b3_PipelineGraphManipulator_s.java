 package cz.cuni.mff.odcleanstore.engine.pipeline;
 
 import java.io.File;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.EnumLogLevel;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.ConnectionException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.QueryException;
 import cz.cuni.mff.odcleanstore.engine.Engine;
 import cz.cuni.mff.odcleanstore.engine.common.FormatHelper;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCSInternal;
 /**
  *  @author Petr Jerman
  */
 final class PipelineGraphManipulator {
 	
 	private static final String ERROR_DELETE_INPUT_FILES = "deleting input files";
 	private static final String ERROR_DELETE_FILE = "deleting file";
 	private static final String ERROR_FILE_STILL_EXIST = ERROR_DELETE_FILE + ", file %s still exists";
 	private static final String ERROR_CLEAR_GRAPHS_IN_DIRTYDB = "clearing graphs in dirty db";
 	private static final String ERROR_COPY_NEW_GRAPHS_TO_CLEANDB = "copying new graphs from dirty db to clean db";
 	private static final String ERROR_RENAMING_OLD_GRAPHS_IN_CLEANDB = "renaming old graphs in clean db";
 	private static final String ERROR_RENAMING_NEW_GRAPHS_IN_CLEANDB = "renaming new graphs in clean db";
 	private static final String ERROR_CLEAR_OLD_GRAPHS_IN_CLEANDB = "clearing old graphs in clean db";
 	private static final String ERROR_CLEAR_NEW_GRAPHS_IN_CLEANDB = "clearing new graphs in clean db";
 	private static final String ERROR_CLEAR_GRAPHS_IN_CLEANDB = "clearing graphs in clean db";
 	private static final String ERROR_LOAD_GRAPHS_FROM_FILE = "loading graphs into clean db from input files";
 	private static final String ERROR_LOAD_METADATAGRAPH_FROM_FILE = "loading metadatagraph (-m.ttl) into clean db from input file";
 	private static final String ERROR_LOAD_METADATAGRAPH_DATABASE_URL = "loaded metadatagraph not contains databaseurl";
 	private static final String ERROR_LOAD_GRAPHS_FROM_CLEAN_DB = "loading graphs into dirty db from clean db";
 	
 	private static final Logger LOG = LoggerFactory.getLogger(PipelineGraphManipulator.class);
 	
 	private PipelineGraphStatus graphStatus;
 	
 	PipelineGraphManipulator(PipelineGraphStatus graphStatus) {
 		this.graphStatus = graphStatus;
 	}
 	
 	void deleteInputFiles() {
 		try {
 			String inputDirPath = Engine.getCurrent().getDirtyDBImportExportDir();
 
 			safeDeleteFile(inputDirPath,  graphStatus.getUuid() + "-d.rdf");
 			safeDeleteFile(inputDirPath,  graphStatus.getUuid() + "-d.ttl");
 			safeDeleteFile(inputDirPath,  graphStatus.getUuid() + "-m.rdf");
 			safeDeleteFile(inputDirPath,  graphStatus.getUuid() + "-m.ttl");
 			safeDeleteFile(inputDirPath,  graphStatus.getUuid() + "-pvm.rdf");
 			safeDeleteFile(inputDirPath,  graphStatus.getUuid() + "-pvm.ttl");
 		}
 		catch(Exception e) {
 			LOG.error(format(ERROR_DELETE_INPUT_FILES));
 		}
 	}
 	
 	void copyNewGraphsFromDirtyToCleanDB() throws PipelineGraphManipulatorException {
 		try {
 			String[] graphs = getAllGraphNames();
 				
 			for (int i = graphs.length - 1; i >= 0; i--) {
 				VirtuosoConnectionWrapper dirtyConnection = null;
 				VirtuosoConnectionWrapper cleanConnection = null;	
 				OutputStreamWriter out = null;
 				File srcFile = null;
 				File dstFile = null;
 				try {
 					String destGraph = ODCSInternal.newGraphPrefix + graphs[i];
 					String tempFileName = generateRandomFileNameForGraph();
 					srcFile = new File(Engine.getCurrent().getDirtyDBImportExportDir(), tempFileName);
 					dstFile = new File(Engine.getCurrent().getCleanDBImportExportDir(), tempFileName);
 
 					graphStatus.checkResetPipelineRequest();
 					dirtyConnection = createDirtyConnection();
 					LOG.info(String.format("Dumping data from dirty db to ttl temporary file for graph %s", graphs[i]));
 					dirtyConnection.exportToTTL(srcFile, graphs[i]);
 					 
 					graphStatus.checkResetPipelineRequest();
 					// move file if neccessary
 					if(!srcFile.getCanonicalFile().equals(dstFile.getCanonicalFile())) {
 						srcFile.renameTo(dstFile);
 					}
 					
 					graphStatus.checkResetPipelineRequest();
 					cleanConnection = createCleanConnection();
 					LOG.info(String.format("Loading data from ttl temporary file to clean db for graph %s", graphs[i]));
 					cleanConnection.insertN3FromFile(dstFile, destGraph, graphs[i]);
 				} finally {
 				    if (dirtyConnection != null) {
 				    	dirtyConnection.closeQuietly();
 				    }
 				    if (cleanConnection != null) {
 				    	cleanConnection.closeQuietly();
 				    }
 				    if (out != null) {
 				    	try { out.close(); } catch(Exception e) {}
 				    }
 					if (srcFile != null) {
 						safeDeleteFile(srcFile);
 					}
 					if (dstFile != null) {
 						safeDeleteFile(dstFile);
 					}
 				}
 			}
 		} catch(Exception e) { 
 			throw new PipelineGraphManipulatorException(format(ERROR_COPY_NEW_GRAPHS_TO_CLEANDB), e);
 		}
 	}
 	
 	void renameGraphsToOldGraphsInCleanDB() throws PipelineGraphManipulatorException {
 		renameGraphsInCleanDB(null , ODCSInternal.oldGraphPrefix, ERROR_RENAMING_OLD_GRAPHS_IN_CLEANDB, false); 
 	}
 	
 	void renameNewGraphsToGraphsInCleanDB()	throws PipelineGraphManipulatorException {
 		renameGraphsInCleanDB(ODCSInternal.newGraphPrefix,  null, ERROR_RENAMING_NEW_GRAPHS_IN_CLEANDB, true);
 	}
 
 	private void renameGraphsInCleanDB(String srcPrefix, String dstPrefix, String errorMessage, boolean descendingDirection)
 			throws PipelineGraphManipulatorException {
 
 		try {
 			String[] graphs = getAllGraphNames();
 			if (descendingDirection) {
 				for (int i = graphs.length - 1; i >= 0;  i--) { 
 					renameGraphInCleanDB(srcPrefix, dstPrefix, graphs[i]); 
 				}
 			} else {
 				for (int i = 0; i < graphs.length;  i++) {
 					renameGraphInCleanDB(srcPrefix, dstPrefix, graphs[i]); 
 				}
 			}
 		} catch(Exception e) { 
 			throw new PipelineGraphManipulatorException(format(errorMessage), e);
 		}
 	}
 	
 	private void renameGraphInCleanDB(String srcPrefix, String dstPrefix, String graph)
 			throws ConnectionException, QueryException {
 
 		VirtuosoConnectionWrapper cleanConnection = null;	
 		try {
 			cleanConnection = createCleanConnection();
 			String srcGraph = (srcPrefix == null ? "" : srcPrefix) + graph;
 			String dstGraph = (dstPrefix == null ? "" : dstPrefix) + graph;
 			cleanConnection.renameGraph(srcGraph, dstGraph);
 		} finally {
 		    if (cleanConnection != null) {
 		    	cleanConnection.closeQuietly();
 		    }
 		}
 	}
 	
 	void clearGraphsInDirtyDB() throws PipelineGraphManipulatorException {
 		try {
 			String[] graphs = getAllGraphNames();
 			for (String graphName : graphs) {
 				VirtuosoConnectionWrapper dirtyConnection = null;	
 				try {
 					dirtyConnection = createDirtyConnection();
					dirtyConnection.clearGraph(graphName);
 				} finally {
 				    if (dirtyConnection != null) {
 				    	dirtyConnection.closeQuietly();
 				    }
 				}
 			}
 		} catch(Exception e) { 
 			throw new PipelineGraphManipulatorException(format(ERROR_CLEAR_GRAPHS_IN_DIRTYDB), e);
 		}
 	}
 	
 	void clearOldGraphsInCleanDB() throws PipelineGraphManipulatorException {
 		clearGraphsInCleanDB(ODCSInternal.oldGraphPrefix, ERROR_CLEAR_OLD_GRAPHS_IN_CLEANDB, false);
 	}
 	
 	void clearNewGraphsInCleanDB() throws PipelineGraphManipulatorException {
 		clearGraphsInCleanDB(ODCSInternal.newGraphPrefix, ERROR_CLEAR_NEW_GRAPHS_IN_CLEANDB, false);
 	}
 	
 	void clearGraphsInCleanDB() throws PipelineGraphManipulatorException {
 		clearGraphsInCleanDB(null, ERROR_CLEAR_GRAPHS_IN_CLEANDB, false);
 	}
 	
 	private void clearGraphsInCleanDB(String prefix, String errorMessage, boolean descendingDirection)
 			throws PipelineGraphManipulatorException {
 		
 		try {
 			String[] graphs = getAllGraphNames();
 			if (descendingDirection) {
 				for (int i = graphs.length - 1; i >= 0;  i--) { 
 					clearGraphInCleanDB(prefix, graphs[i]); 
 				}
 			} else {
 				for (int i = 0; i < graphs.length;  i++) {
 					clearGraphInCleanDB(prefix, graphs[i]); 
 				}
 			}
 		} catch(Exception e) { 
 			throw new PipelineGraphManipulatorException(format(errorMessage), e);
 		}
 	}
 	
 	private void clearGraphInCleanDB(String prefix, String graph)
 			throws ConnectionException, QueryException {
 		VirtuosoConnectionWrapper cleanConnection = null;	
 		try {
 			cleanConnection = createCleanConnection();
 			String clearedGraph = (prefix == null ? "" : prefix) + graph;
			cleanConnection.clearGraph(clearedGraph);
 		} finally {
 		    if (cleanConnection != null) {
 		    	cleanConnection.closeQuietly();
 		    }
 		}
 	}
 	
 	void loadGraphsIntoDirtyDB() throws PipelineGraphManipulatorException {
 		String errorMessage = null;
 		try {
 			if (graphStatus.isInCleanDb()) {
 				errorMessage = ERROR_LOAD_GRAPHS_FROM_CLEAN_DB;
 				loadGraphsIntoDirtyDBFromCleanDB();
 			}
 			else {
 				errorMessage = ERROR_LOAD_GRAPHS_FROM_FILE;
 				loadGraphsIntoDirtyDBFromInputFile();
 			}
 			LOG.info(format("Data succefully loaded to dirty database"));
 		}
 		catch(Exception e) {
 			throw new PipelineGraphManipulatorException(format(errorMessage), e);
 		}
 	}
 	
 	private void loadGraphsIntoDirtyDBFromInputFile() throws Exception {
 		String inputDirPath = Engine.getCurrent().getDirtyDBImportExportDir();
 		String uuid = graphStatus.getUuid();
 		
 		String dataGraphURI = ODCSInternal.dataGraphUriPrefix + uuid;
 		String metadataGraphURI = ODCSInternal.metadataGraphUriPrefix + uuid;
 		String provenanceGraphURI = ODCSInternal.provenanceMetadataGraphUriPrefix + uuid;
 		
 		String dataBaseUrl = null;
 			
 		VirtuosoConnectionWrapper con = null;
 		try {
 			con = createDirtyConnection();
 
 			graphStatus.checkResetPipelineRequest();
 			try {
 				LOG.info(format("Loading metadata from ttl input file"));
 				File mTTLFile = new File(inputDirPath, uuid + "-m.ttl"); 
 				con.insertN3FromFile(mTTLFile, metadataGraphURI, "");
 				
 			} catch (Exception e) {
 				throw new PipelineGraphManipulatorException(format(ERROR_LOAD_METADATAGRAPH_FROM_FILE), e);
 			}
 			
 			graphStatus.checkResetPipelineRequest();
 			try {
 				WrappedResultSet rs = con.executeSelect(String.format(Locale.ROOT, "SPARQL SELECT ?o WHERE { GRAPH <%s> {<%s> <%s> ?o}}", metadataGraphURI, dataGraphURI, ODCS.dataBaseUrl));
 				rs.next();
 				dataBaseUrl = rs.getString(1);
 			} catch (Exception e) {
 				throw new PipelineGraphManipulatorException(format(ERROR_LOAD_METADATAGRAPH_DATABASE_URL), e);
 			}
 			
 			graphStatus.checkResetPipelineRequest();
 			File pvmRDFFile = new File(inputDirPath, uuid + "-pvm.rdf");
 			if (pvmRDFFile.exists()) {
 				LOG.info(format("Loading provenance metadata from rdfxml input file"));
 				con.insertRdfXmlFromFile(pvmRDFFile, provenanceGraphURI, dataBaseUrl);
 				con.insertQuad("<" + dataGraphURI + ">", "<" + ODCS.provenanceMetadataGraph + ">", "<" + provenanceGraphURI + ">", metadataGraphURI);
 			}
 			
 			graphStatus.checkResetPipelineRequest();
 			File pvmTTLFile = new File(inputDirPath, uuid + "-pvm.ttl");
 			if (pvmTTLFile.exists()) {
 				LOG.info(format("Loading provenance metadata from ttl input file"));
 				con.insertN3FromFile(pvmTTLFile, provenanceGraphURI, dataBaseUrl);
 				con.insertQuad("<" + dataGraphURI + ">", "<" + ODCS.provenanceMetadataGraph + ">", "<" + provenanceGraphURI + ">", metadataGraphURI);
 				
 			}
 			
 			graphStatus.checkResetPipelineRequest();
 			File dRDFFile = new File(inputDirPath, uuid + "-d.rdf");
 			if (dRDFFile.exists()) {
 				LOG.info(format("Loading data from rdf input file"));
 				con.insertRdfXmlFromFile(dRDFFile, dataGraphURI, dataBaseUrl);
 				
 			}
 			
 			graphStatus.checkResetPipelineRequest();
 			File dTTLFile = new File(inputDirPath, uuid + "-d.ttl");
 			if (dTTLFile.exists()) {
 				LOG.info(format("Loading data from ttl input file"));
 				con.insertN3FromFile(dTTLFile, dataGraphURI, dataBaseUrl);
 			}
 		} finally {
 			if (con != null) {
 				con.closeQuietly();
 			}
 		}
 	}
 
 	private void loadGraphsIntoDirtyDBFromCleanDB() throws Exception {
 		try {
 			String[] graphs = getAllGraphNames();
 				
 			for (String graphName : graphs) {
 					
 				File srcFile = null;
 				File dstFile = null;
 				OutputStreamWriter out = null;
 				VirtuosoConnectionWrapper cleanConnection = null;
 				VirtuosoConnectionWrapper dirtyConnection = null;
 				try {
 					String tempFileName = generateRandomFileNameForGraph();
 					srcFile = new File(Engine.getCurrent().getCleanDBImportExportDir(), tempFileName);
 					dstFile = new File(Engine.getCurrent().getDirtyDBImportExportDir(), tempFileName);
 					
 					graphStatus.checkResetPipelineRequest();
 					cleanConnection = createCleanConnection();
 					LOG.info(String.format("Dumping data from clean db to ttl temporary file for graph %s", graphName));
 					String query = "CALL dump_graph_ttl('" + graphName + "', '" + srcFile.getAbsolutePath().replace("\\", "/") + "')";
 					cleanConnection.execute(query);
 					
 					graphStatus.checkResetPipelineRequest();
 					// move file if neccessary
 					if(!srcFile.getCanonicalFile().equals(dstFile.getCanonicalFile())) {
 						srcFile.renameTo(dstFile);
 					}
 					
 					graphStatus.checkResetPipelineRequest();
 					dirtyConnection = createDirtyConnection();
 					LOG.info(String.format("Loading data from ttl temporary file to dirty db for graph %s", graphName));
 					dirtyConnection.insertN3FromFile(dstFile, graphName, graphName);
 				} finally {
 				    if (dirtyConnection != null) {
 				    	dirtyConnection.closeQuietly();
 				    }
 				    if (cleanConnection != null) {
 				    	cleanConnection.closeQuietly();
 				    }
 				    if (out != null) {
 				    	try { out.close(); } catch(Exception e) {}
 				    }
 					if (srcFile != null) {
 						safeDeleteFile(srcFile);
 					}
 					if (dstFile != null) {
 						safeDeleteFile(dstFile);
 					}
 				}
 			}
 		} catch(Exception e) { 
 			throw new PipelineGraphManipulatorException(format(ERROR_LOAD_GRAPHS_FROM_CLEAN_DB), e);
 		}
 	}
 
 	private String[] getAllGraphNames() {
 		String uuid = graphStatus.getUuid();
 		
 		ArrayList<String> graphs = new ArrayList<String>();
 		graphs.add(ODCSInternal.dataGraphUriPrefix + uuid);
 		graphs.add(ODCSInternal.metadataGraphUriPrefix + uuid);
 		graphs.add(ODCSInternal.provenanceMetadataGraphUriPrefix + uuid);
 		graphs.addAll(graphStatus.getAttachedGraphs());
 		return graphs.toArray(new String[0]);
 	}
 		
 	private void safeDeleteFile(String dirPath, String fileName) {
 		try {
 			if(dirPath == null) {
 				LOG.debug("dirPath is null");
 			}
 			if(fileName == null) {
 				LOG.debug("fileName is null");
 			}
 			File file = new File(dirPath, fileName);
 			safeDeleteFile(file);
 		} catch(Exception e) {
 			LOG.error(format(ERROR_DELETE_FILE));
 		}
 	}
 
 	// TODO add logging for service application (will be created) !!!	
 	private void safeDeleteFile(File file) {
 		try {
 			if(file == null) {
 				LOG.debug("file is null");
 			}
 			if (!file.delete() && file.exists()) {
 				String message = String.format(ERROR_FILE_STILL_EXIST, file.getName());
 				LOG.error(format(message));
 			}
 		} catch(Exception e) {
 			LOG.error(format(ERROR_DELETE_FILE));
 		}
 	}
 	
 	private String generateRandomFileNameForGraph() {
 		return graphStatus.getUuid() + "-" + UUID.randomUUID() + "-temp.ttl";
 	}
 	
 	private VirtuosoConnectionWrapper createDirtyConnection() throws ConnectionException {
 		JDBCConnectionCredentials credit = ConfigLoader.getConfig().getEngineGroup().getDirtyDBJDBCConnectionCredentials();
 		VirtuosoConnectionWrapper con = VirtuosoConnectionWrapper.createConnection(credit);
 		con.adjustTransactionLevel(EnumLogLevel.STATEMENT_LEVEL, true);
 		con.setQueryTimeout(0);
 		return con;
 	}
 	
 	private VirtuosoConnectionWrapper createCleanConnection() throws ConnectionException {
 		JDBCConnectionCredentials credit = ConfigLoader.getConfig().getEngineGroup().getCleanDBJDBCConnectionCredentials();
 		VirtuosoConnectionWrapper con = VirtuosoConnectionWrapper.createConnection(credit);
 		con.adjustTransactionLevel(EnumLogLevel.STATEMENT_LEVEL, true);
 		con.setQueryTimeout(0);
 		return con;
 	}
 		
 	private String format(String message) {
 		try {
 			return FormatHelper.formatGraphMessage(message, graphStatus.getUuid(), graphStatus.isInCleanDbBeforeProcessing());
 		} catch(Exception e) {
 			return message;
 		}
 	}
 }
