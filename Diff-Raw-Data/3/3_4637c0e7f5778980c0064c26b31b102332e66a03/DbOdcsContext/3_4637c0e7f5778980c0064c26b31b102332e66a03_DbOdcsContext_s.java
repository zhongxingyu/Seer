 package cz.cuni.mff.odcleanstore.engine.db.model;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.ConnectionException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.QueryException;
 import cz.cuni.mff.odcleanstore.engine.db.DbContext;
 
 public class DbOdcsContext extends DbContext {
 	
 	private static final String ERROR_CREATE_ODCS_CONTEXT = "Error during creating DbOdcsContext";
 	
 	public DbOdcsContext() throws DbOdcsException {
 		try {
 			setConnection(ConfigLoader.getConfig().getBackendGroup().getCleanDBJDBCConnectionCredentials());
 			execute(SQL.USE_ODCS_SCHEMA);
 		}
 		catch(Exception e) {
 			throw new DbOdcsException(ERROR_CREATE_ODCS_CONTEXT, e);
 		}
 	}
 
 	public Graph selectOldestEngineWorkingGraph(String engineUuid) throws DbOdcsException {
 		WrappedResultSet resultSet = null;
 		try {
 			return createDbGraph(resultSet, SQL.SELECT_WORKING_GRAPH, engineUuid);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_SELECT_WORKING_GRAPH, e);
 		} finally {
 			close(resultSet); 
 		}
 	}
 
 	public Graph selectOldestEngineQueuedGraph(String engineUuid) throws DbOdcsException  {
 		WrappedResultSet resultSet = null;
 		try {
 			return createDbGraph(resultSet, SQL.SELECT_QUEUD_GRAPH, engineUuid);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_SELECT_QUEUD_GRAPH, e);
 		} finally {
 			close(resultSet); 
 		}
 
 	}
 	
 	private Graph createDbGraph(WrappedResultSet resultSet, String query, String engineUuid) throws ConnectionException, QueryException, DbOdcsException, SQLException {
 		resultSet = select(query, engineUuid);
 		if(resultSet.next()) {
 			Graph dbGraph = new Graph();
 			dbGraph.pipeline = new Pipeline();
 			dbGraph.id = resultSet.getInt(1);
 			dbGraph.uuid = resultSet.getString(2);
 			dbGraph.state = GraphStates.fromId(resultSet.getInt(3));
 			dbGraph.pipeline.id = resultSet.getInt(4);
 			dbGraph.pipeline.label = resultSet.getString(5);
 			dbGraph.isInCleanDb  = resultSet.getInt(6) != 0;
 			dbGraph.engineUuid = resultSet.getString(7);
 			return dbGraph;
 		}
 		return null;
 	}
 	
 	public boolean updateAttachedEngine(int graphId, String engineUuid) throws DbOdcsException  {
 		try {
 			int updatedRowCount = execute(SQL.UPDATE_ATTACHED_ENGINE, engineUuid, graphId, engineUuid);
 			return updatedRowCount > 0;
 		} catch (Exception e) {
 			throw  new DbOdcsException(SQL.ERROR_UPDATE_ATTACHED_ENGINE, e);
 		}
 	}
 	
 	public boolean updateState( int graphId,  GraphStates newState) throws DbOdcsException  {
 		try {
 			int updatedRowCount = execute(SQL.UPDATE_GRAPH_STATE, newState.toId(), graphId);
 			return updatedRowCount > 0;
 		} catch (Exception e) {
 			throw  new DbOdcsException(SQL.ERROR_UPDATE_GRAPH_STATE, e);
 		}
 	}
 	
 	public boolean updateStateAndIsInCleanDb( int graphId,  GraphStates newState, boolean isInCleanDb) throws DbOdcsException  {
 		try {
 			int updatedRowCount = execute(SQL.UPDATE_GRAPH_STATE_AND_ISINCLEANDB, newState.toId(), isInCleanDb ? 1 : 0, graphId);
 			return updatedRowCount > 0;
 		} catch (Exception e) {
 			throw  new DbOdcsException(SQL.ERROR_UPDATE_GRAPH_STATE_AND_ISINCLEANDB, e);
 		}
 	}
 	
 	public HashSet<String> selectAttachedGraphs(int graphId) throws DbOdcsException  {
 		WrappedResultSet resultSet = null;
 		try {
 			HashSet<String> graphNames = new HashSet<String>();
 			resultSet = select(SQL.SELECT_ATTACHED_GRAPHS, graphId);
 			while(resultSet.next()) {
 				graphNames.add(resultSet.getString(1));
 			}
 			return graphNames;
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_SELECT_ATTACHED_GRAPHS, e);
 		} finally {
 			close(resultSet); 
 		}
 	}
 	
 	public void insertAttachedGraph(int graphId, String name) throws DbOdcsException  {
 		try {
 			execute(SQL.INSERT_ATTACHED_GRAPH, graphId, name);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_INSERT_ATTACHED_GRAPH, e);
 		}
 	}
 	
 	public void deleteAttachedGraphs(int graphId) throws DbOdcsException  {
 		try {
 			execute(SQL.DELETE_ATTACHED_GRAPHS, graphId);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_DELETE_ATTACHED_GRAPHS, e);
 		}
 	}
 	
 	public PipelineCommand[] selectPipelineCommands(int pipelineId) throws DbOdcsException  {
 		WrappedResultSet resultSet = null;
 		try {
 			ArrayList<PipelineCommand> dbPipelineCommands = new ArrayList<PipelineCommand>();
 			resultSet = select(SQL.SELECT_PIPELINE_COMMANDS, pipelineId);
 			while(resultSet.next()) {
 				PipelineCommand mbr = new PipelineCommand();
 				mbr.jarPath = resultSet.getString(1);
 				mbr.fullClassName = resultSet.getString(2);
 				mbr.workDirPath = resultSet.getString(3);
				mbr.configuration = resultSet.getNString(4);
 				mbr.runOnCleanDB = resultSet.getInt(5) != 0;
 				mbr.transformerInstanceID = resultSet.getInt(6);
 				mbr.transformerLabel = resultSet.getString(7);
 				dbPipelineCommands.add(mbr);
 			}
 			return dbPipelineCommands.toArray( new PipelineCommand[0]);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_SELECT_PIPELINE_COMMANDS, e);
 		}
 	}
 	
 	public GroupRule[] selectDnRules(int pipelineId) throws DbOdcsException  {
 		WrappedResultSet resultSet = null;
 		try {
 			ArrayList<GroupRule> groupRules = new ArrayList<GroupRule>();
 			resultSet = select(SQL.SELECT_DN_GROUPS, pipelineId);
 			while(resultSet.next()) {
 				GroupRule mbr = new GroupRule();
 				mbr.transformerInstanceId = resultSet.getInt(1);
 				mbr.groupId = resultSet.getInt(2);
 				groupRules.add(mbr);
 			}
 			return groupRules.toArray(new GroupRule[0]);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_SELECT_DN_GROUPS, e);
 		}
 	}
 	
 	public GroupRule[] selectQaRules(int pipelineId) throws DbOdcsException  {
 		WrappedResultSet resultSet = null;
 		try {
 			ArrayList<GroupRule> groupRules = new ArrayList<GroupRule>();
 			resultSet = select(SQL.SELECT_QA_GROUPS, pipelineId);
 			while(resultSet.next()) {
 				GroupRule mbr = new GroupRule();
 				mbr.transformerInstanceId = resultSet.getInt(1);
 				mbr.groupId = resultSet.getInt(2);
 				groupRules.add(mbr);
 			}
 			return groupRules.toArray(new GroupRule[0]);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_SELECT_QA_GROUPS, e);
 		}
 	}
 	
 	public GroupRule[] selectOiRules(int pipelineId) throws DbOdcsException  {
 		WrappedResultSet resultSet = null;
 		try {
 			ArrayList<GroupRule> groupRules = new ArrayList<GroupRule>();
 			resultSet = select(SQL.SELECT_OI_GROUPS, pipelineId);
 			while(resultSet.next()) {
 				GroupRule mbr = new GroupRule();
 				mbr.transformerInstanceId = resultSet.getInt(1);
 				mbr.groupId = resultSet.getInt(2);
 				groupRules.add(mbr);
 			}
 			return groupRules.toArray(new GroupRule[0]);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_SELECT_OI_GROUPS, e);
 		}
 	}
 	
 	public Pipeline selectDefaultPipeline() throws DbOdcsException {
 		WrappedResultSet resultSet = null;
 		try {
 			resultSet = select(SQL.SELECT_DEFAULT_PIPELINE);
 			if(resultSet.next()) {
 				Pipeline pipeline = new Pipeline();
 				pipeline.id = resultSet.getInt(1);
 				pipeline.label = resultSet.getString(2);
 				return pipeline;
 			}
 			return null;
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.SELECT_DEFAULT_PIPELINE, e);
 		}
 	}
 	
 	public void insertGraphInError(int graphId, PipelineErrorTypes type, String message) throws DbOdcsException  {
 		try {
 			execute(SQL.INSERT_GRAPH_IN_ERROR, graphId, type.toId(), message);
 		} catch (Exception e) {
 			throw new DbOdcsException(SQL.ERROR_INSERT_GRAPH_IN_ERROR, e);
 		}
 	}
 }
