 package cz.cuni.mff.odcleanstore.webfrontend.dao.en;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import cz.cuni.mff.odcleanstore.model.EnumGraphState;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.en.GraphInError;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.QueryCriteria;
 import cz.cuni.mff.odcleanstore.webfrontend.util.CodeSnippet;
 
 public class GraphInErrorDao extends DaoForEntityWithSurrogateKey<GraphInError> {
 	
 	private static final long serialVersionUID = 1L;
 	public static final String GRAPHS_IN_ERROR_TABLE_NAME = TABLE_NAME_PREFIX + "EN_GRAPHS_IN_ERROR";
 	public static final String INPUT_GRAPHS_TABLE_NAME = TABLE_NAME_PREFIX + "EN_INPUT_GRAPHS";
 	public static final String INPUT_GRAPHS_STATES_TABLE_NAME = TABLE_NAME_PREFIX + "EN_INPUT_GRAPHS_STATES";
 	public static final String PIPELINE_ERROR_TYPES_TABLE_NAME = TABLE_NAME_PREFIX + "EN_PIPELINE_ERROR_TYPES";
 	public static final String PIPELINES_TABLE_NAME = TABLE_NAME_PREFIX + "PIPELINES";
 	public static final String ATTACHED_ENGINES_TABLE_NAME = TABLE_NAME_PREFIX + "EN_ATTACHED_ENGINES";
 
 	@Override
 	public String getTableName() {
 		return GRAPHS_IN_ERROR_TABLE_NAME;
 	}
 
 	@Override
 	protected ParameterizedRowMapper<GraphInError> getRowMapper() {
 		return new GraphInErrorRowMapper();
 	}
 	
 	@Override
 	protected String getSelectAndFromClause()
 	{
 		String query = "SELECT " +
 			"eGraph.id AS id, " +
 			"iGraph.engineId AS engineId, " +
 			"iGraph.pipelineId AS pipelineId, " +
 			"iGraph.uuid AS uuid, " +
 			"iGraph.stateId AS stateId, " +
 			"iGraph.isInCleanDB AS isInCleanDB, " +
 			"iGraph.namedGraphsPrefix AS namedGraphsPrefix, " +
 			"eGraph.errorTypeId AS errorTypeId, " +
 			"eGraph.errorMessage AS errorMessage," +
 			"engine.uuid AS engineUuid, " +
 			"pipeline.label AS pipelineLabel, " +
 			"iState.label AS stateLabel," +
 			"pError.label AS errorTypeLabel " +
 			"FROM " +
 			INPUT_GRAPHS_TABLE_NAME + " AS iGraph JOIN " +
 			INPUT_GRAPHS_STATES_TABLE_NAME + " AS iState ON iGraph.stateId = iState.id JOIN " +
 			PIPELINES_TABLE_NAME + " AS pipeline ON pipeline.id = iGraph.pipelineId JOIN " +
 			ATTACHED_ENGINES_TABLE_NAME + " AS engine ON engine.id = iGraph.engineId LEFT JOIN " +
 			GRAPHS_IN_ERROR_TABLE_NAME + " AS eGraph ON eGraph.graphId = iGraph.id LEFT JOIN " +
 			PIPELINE_ERROR_TYPES_TABLE_NAME + " AS pError ON eGraph.errorTypeId = pError.id ";
 		
 		return query;
 	}
 	
 	public void markFinished(GraphInError graphInError) throws Exception {
 		mark(graphInError, EnumGraphState.FINISHED);
 	}
 	
 	public void markQueued(GraphInError graphInError) throws Exception {
 		mark(graphInError, EnumGraphState.QUEUED);
 	}
 	
 	public void markQueuedForDelete(GraphInError graphInError) throws Exception {
 		mark(graphInError, EnumGraphState.QUEUED_FOR_DELETE);
 	}
 
 	private void mark(final GraphInError graphInError, final EnumGraphState state) throws Exception {
 		executeInTransaction(new CodeSnippet() {
 			@Override
 			public void execute() throws Exception {
 				QueryCriteria criteria;
 				String query;
 				Object[] param;
 				
 				query = "DELETE FROM " + GRAPHS_IN_ERROR_TABLE_NAME + " AS eGraph WHERE " +
 						"eGraph.graphId IN " +
 						"(SELECT iGraph.id FROM " + INPUT_GRAPHS_TABLE_NAME + " AS iGraph WHERE iGraph.uuid = ? AND iGraph.pipelineId = ? AND iGraph.engineId = ?)";
 
 				param = new Object[] {graphInError.UUID, graphInError.pipelineId, graphInError.engineId};
 				GraphInErrorDao.this.jdbcUpdate(query, param);
 				
 				criteria = new QueryCriteria();
 				criteria.addWhereClause("iGraph.uuid", graphInError.UUID);
 				criteria.addWhereClause("iGraph.pipelineId", graphInError.pipelineId);
 				criteria.addWhereClause("iGraph.engineId", graphInError.engineId);
 				criteria.addWhereClause("state.label", state.name());
 
				query = "INSERT REPLACING " + INPUT_GRAPHS_TABLE_NAME + " (id, uuid, stateId, engineId, pipelineId, isInCleanDB) " +
						"SELECT  iGraph.id, iGraph.uuid, state.id, iGraph.engineId, iGraph.pipelineId, iGraph.isInCleanDB " +
 						"FROM " +
 						INPUT_GRAPHS_TABLE_NAME + " AS iGraph, " +
 						INPUT_GRAPHS_STATES_TABLE_NAME + " AS state " +
 						criteria.buildWhereClause();
 
 				param = criteria.buildWhereClauseParams();
 				GraphInErrorDao.this.jdbcUpdate(query, param);
 			}
 		});
 	}
 
 	public void markAllQueued(QueryCriteria criteria) throws Exception {
 		markAll(criteria, EnumGraphState.QUEUED);
 	}
 	
 	public void markAllQueuedForDelete(QueryCriteria criteria) throws Exception {
 		markAll(criteria, EnumGraphState.QUEUED_FOR_DELETE);
 	}
 	
 	public void markAllFinished(QueryCriteria criteria) throws Exception {
 		markAll(criteria, EnumGraphState.FINISHED);
 	}
 	
 	private void markAll(final QueryCriteria criteria, final EnumGraphState state) throws Exception {
 		executeInTransaction(new CodeSnippet() {
 			@Override
 			public void execute() throws Exception {
 				String query;
 				Object[] param;
 				
 				/**
 				 * DELETE ALL ERRORS BOUND TO GRAPHS (GIVEN BY CRITERIA AND IN STATE = WRONG)
 				 */
 				
 				query = "DELETE FROM " + GRAPHS_IN_ERROR_TABLE_NAME + " AS eGraph WHERE " +
 						"eGraph.graphId IN " +
 						"(SELECT iGraph.id FROM " + INPUT_GRAPHS_TABLE_NAME + " AS iGraph " + criteria.buildWhereClause() + " AND stateId IN" +
 						"(SELECT iState.id FROM " + INPUT_GRAPHS_STATES_TABLE_NAME + " AS iState WHERE iState.label = '" + EnumGraphState.WRONG.name() + "'))";
 
 				param = criteria.buildWhereClauseParams();
 				GraphInErrorDao.this.jdbcUpdate(query, param);
 				
 				/**
 				 * REPLACE STATE ID
 				 */
 				criteria.addWhereClause("iState.label", EnumGraphState.WRONG.name());
 				criteria.addWhereClause("state.label", state.name());
 
 				query =
 						"INSERT REPLACING " + INPUT_GRAPHS_TABLE_NAME + " (id, uuid, stateId, engineId, pipelineId, isInCleanDB) " +
 						"SELECT  iGraph.id, iGraph.uuid, state.id, iGraph.engineId, iGraph.pipelineId, iGraph.isInCleanDB " +
 						"FROM " +
 						INPUT_GRAPHS_TABLE_NAME + " AS iGraph JOIN " +
 						INPUT_GRAPHS_STATES_TABLE_NAME + " AS iState ON iGraph.stateId = iState.id, " +
 						INPUT_GRAPHS_STATES_TABLE_NAME + " AS state " +
 						criteria.buildWhereClause();
 
 				param = criteria.buildWhereClauseParams();
 				GraphInErrorDao.this.jdbcUpdate(query, param);
 			}
 		});
 	}
 }
