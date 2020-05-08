 package cz.cuni.mff.odcleanstore.webfrontend.dao.en;
 
 import org.apache.log4j.Logger;
 
 import cz.cuni.mff.odcleanstore.model.EnumGraphState;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.Dao;
 import cz.cuni.mff.odcleanstore.webfrontend.util.CodeSnippet;
 
 /**
  * A DAO which contains methods to signal the Engine to rerun certain
  * graphs in the clean DB. 
  * 
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  */
 public class EngineOperationsDao extends Dao
 {
 	private static final long serialVersionUID = 1L;
 
 	public enum RULES_GROUPS_TYPE { OI, QA, DN };
 	
 	private static final String INPUT_GRAPHS_TABLE_NAME = Dao.TABLE_NAME_PREFIX + "EN_INPUT_GRAPHS";
 	private static final String INPUT_GRAPHS_STATES_TABLE_NAME = Dao.TABLE_NAME_PREFIX + "EN_INPUT_GRAPHS_STATES";
 	private static final String GRAPHS_IN_ERROR_TABLE_NAME = Dao.TABLE_NAME_PREFIX + "EN_GRAPHS_IN_ERROR";
 	
 	private static Logger logger = Logger.getLogger(EngineOperationsDao.class);
 	
 	/**
 	 * Updates DB contents to signal Engine to rerun all graphs associated with the pipeline given by id.
 	 * 
 	 * @param pipelineId
 	 */
 	public void rerunGraphsForPipeline(final Integer pipelineId) throws Exception
 	{
 		executeInTransaction(new CodeSnippet() {
 			
 			@Override
 			public void execute() throws Exception {
 				String query =
 						"UPDATE " + INPUT_GRAPHS_TABLE_NAME + " " +
 						"SET stateId = (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ?) " +
 						"WHERE " +
 						"	pipelineId = ? AND " +
 						"	stateId = (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ?)";
 					
 				Object[] params = { EnumGraphState.QUEUED.name(), pipelineId, EnumGraphState.FINISHED.name() };
 					
 				logger.debug("queued state label: " + EnumGraphState.QUEUED.name());
 				logger.debug("pipeline id: " + pipelineId);
 				logger.debug("finished state label: " + EnumGraphState.FINISHED.name());
 					
 				jdbcUpdate(query, params);
 				
				query = "DELETE FROM " + GRAPHS_IN_ERROR_TABLE_NAME + " WHERE " +
 						"graphId IN (SELECT id FROM " + INPUT_GRAPHS_TABLE_NAME + " WHERE pipelineId = ?)";
 				
 				params = new Object[] {pipelineId};
 				
 				jdbcUpdate(query, params);
 			}
 		});
 	}
 	
 	/**
 	 * Updates DB contents to signal Engine to rerun all graphs associated with all pipelines
 	 * that have a transformer which is assigned the rule group given by id.
 	 * 
 	 * @param assignmentDao
 	 * @param groupId
 	 */
 	public void rerunGraphsForRulesGroup(final String assignmentTableName, final Integer groupId) throws Exception
 	{
 		executeInTransaction(new CodeSnippet() {
 
 			@Override
 			public void execute() throws Exception {
 				String query =
 						"UPDATE " + INPUT_GRAPHS_TABLE_NAME + " " +
 						"SET stateId = (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ?) " +
 						"WHERE " +
 						"	stateId = (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ?) AND " +
 						"	pipelineId IN (" +
 						"		SELECT DISTINCT TI.pipelineId " +
 						"		FROM " + assignmentTableName + " as RA " +
 						"		JOIN " + TransformerInstanceDao.TABLE_NAME + " as TI " +
 						"		ON (RA.transformerInstanceId = TI.id) " +
 						"		WHERE (RA.groupId = ?) " +
 						"	)";
 					
 				Object[] params = { EnumGraphState.QUEUED.name(), EnumGraphState.FINISHED.name(), groupId };
 					
 				logger.debug("queued state label: " + EnumGraphState.QUEUED.name());
 				logger.debug("finished state label: " + EnumGraphState.FINISHED.name());
 				logger.debug("group id: " + groupId);
 					
 				jdbcUpdate(query, params);
 				
				query = "DELETE FROM " + GRAPHS_IN_ERROR_TABLE_NAME + " WHERE " +
 						"graphId IN (SELECT id FROM " + INPUT_GRAPHS_TABLE_NAME +
 						"	WHERE pipelineId IN (" +
 						"		SELECT DISTINCT TI.pipelineId " +
 						"		FROM " + assignmentTableName + " as RA " +
 						"		JOIN " + TransformerInstanceDao.TABLE_NAME + " as TI " +
 						"		ON (RA.transformerInstanceId = TI.id) " +
 						"		WHERE (RA.groupId = ?)))";
				
				params = new Object[] {groupId};
				
				jdbcUpdate(query, params);
 			}
 			
 		});
 	}
 	
 	/**
 	 * Updates DB contents to signal Engine to rerun pipeline on graph with the given id.
 	 * 
 	 * @param graphId ID of graph to re-run pipeline on
 	 */
 	public void rerunGraph(final Integer graphId) throws Exception
 	{
 		executeInTransaction(new CodeSnippet() {
 
 			@Override
 			public void execute() throws Exception {
 				String query =
 						"UPDATE " + INPUT_GRAPHS_TABLE_NAME + " " +
 						"SET stateId = (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ?) " +
 						"WHERE id = ? AND " +
 						"	stateId IN (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ? OR label = ?)";
 					
 				logger.debug("graph id: " + graphId);
 					
 				jdbcUpdate(query, EnumGraphState.QUEUED.name(), graphId, EnumGraphState.FINISHED.name(), EnumGraphState.WRONG.name());
 
				query = "DELETE FROM " + GRAPHS_IN_ERROR_TABLE_NAME + " WHERE " +
 						"graphId = ?";
 				
 				jdbcUpdate(query, graphId);
 			}
 		});
 	}
 	
 	
 	/**
 	 * Marks graph for deletion by Engine. Should be called only on graphs in state FINISHED or WRONG.
 	 * 
 	 * @param graphId ID of graph to be deleted
 	 */
 	public void queueGraphForDeletion(final Integer graphId) throws Exception
 	{
 		String query =
 			"UPDATE " + INPUT_GRAPHS_TABLE_NAME + " " +
 			"SET stateId = (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ?) " +
 			"WHERE id = ? AND " +
 			"	stateId IN (SELECT id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " WHERE label = ? OR label = ?)";
 		
 		logger.debug("graph id: " + graphId);
 		
 		jdbcUpdate(query, EnumGraphState.QUEUED_FOR_DELETE.name(), graphId, EnumGraphState.FINISHED.name(), EnumGraphState.WRONG.name());
 	}
 }
