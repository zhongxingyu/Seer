 package cz.cuni.mff.odcleanstore.engine.db.model;
 
 class SQL {
 	
 	/**
 	 * Set odcs DB prefix.
 	 */
 	static final String USE_ODCS_SCHEMA = "USE DB";
 	
 	
 	//-----------------------------------------------------------------------------------------------//
 	
 	/**
 	 * Select oldest working graph for given engine uuid.
 	 * @param first Engine UUID
 	 */
 	static final String SELECT_WORKING_GRAPH = String.format(
 			  " SELECT TOP 1 ig.id, ig.uuid, ig.stateId, ig.pipelineId, ig.isInCleanDB, ae.uuid" 
 			+ " FROM ODCLEANSTORE.EN_INPUT_GRAPHS ig"
 			+ " LEFT JOIN ODCLEANSTORE.EN_ATTACHED_ENGINES ae ON ig.engineId = ae.id"
 			+ " WHERE (ae.uuid = ? OR ae.uuid IS NULL) AND ig.stateId IN(%s,%s,%s,%s,%s)"
 			+ " ORDER BY ig.updated",
 			GraphStates.DIRTY.toId(),
 			GraphStates.PROPAGATED.toId(),
 			GraphStates.DELETING.toId(),
 			GraphStates.PROCESSING.toId(),
 			GraphStates.PROCESSED.toId());
 	
 	static final String ERROR_SELECT_WORKING_GRAPH = "Error during selecting working graph";
 	
 	
 	/**
 	 * Select oldest queued graph for given engine uuid.	
 	 * @param first Engine UUID
 	 */	
 	static final String SELECT_QUEUD_GRAPH = String.format(
 			  " SELECT TOP 1 ig.id, ig.uuid, ig.stateId, ig.pipelineId, ig.isInCleanDB, ae.uuid" 
 			+ " FROM ODCLEANSTORE.EN_INPUT_GRAPHS ig"
 			+ " LEFT JOIN ODCLEANSTORE.EN_ATTACHED_ENGINES ae ON ig.engineId = ae.id"
 			+ " WHERE (ae.uuid = ? OR ae.uuid IS NULL) AND ig.stateId IN(%s,%s,%s)"
 			+ " ORDER BY ig.stateId, ig.updated",
 			GraphStates.QUEUED_FOR_DELETE.toId(),
 			GraphStates.QUEUED_URGENT.toId(),
 			GraphStates.QUEUED.toId());
 	
 	static final String ERROR_SELECT_QUEUD_GRAPH = "Error during selecting queued graph";
 
 	
 	//-----------------------------------------------------------------------------------------------//
 	
 	/**
 	 * Update attached engine id for given graph id.
 	 * @param first Engine UUID
 	 * @param second Graph ID
 	 * @param third Engine UUID
 	 */
 	static final String UPDATE_ATTACHED_ENGINE = String.format(
 			  " UPDATE ODCLEANSTORE.EN_INPUT_GRAPHS\n"
 			+ " SET engineId = (SELECT id FROM ODCLEANSTORE.EN_ATTACHED_ENGINES WHERE uuid = ?)\n"
 			+ " WHERE id = ?\n"
 			+ " AND EXISTS (SELECT * FROM ODCLEANSTORE.EN_ATTACHED_ENGINES WHERE uuid = ?)");			
 
 	static final String ERROR_UPDATE_ATTACHED_ENGINE = "Error during updating attached engine";
 	
 	
 	/**
 	 * Update graph state id for given graph id.
 	 * @param first Graph stateId
 	 * @param second Graph ID
 	 */
 	static final String UPDATE_GRAPH_STATE = String.format(
 			  " UPDATE ODCLEANSTORE.EN_INPUT_GRAPHS" 
 			+ " SET stateId = ?"
 			+ " WHERE Id = ?");
 	
 	static final String ERROR_UPDATE_GRAPH_STATE = "Error during updating graph state";
 
 	
 	/**
 	 * Update graph stateId and isCleanDb for given graph id.
 	 * @param first Graph state ID
 	 * @param second isInCleanDb 
 	 * @param third Graph ID
 	 */
 	static final String UPDATE_GRAPH_STATE_AND_ISINCLEANDB = String.format(
 			  " UPDATE ODCLEANSTORE.EN_INPUT_GRAPHS" 
 			+ " SET stateId = ?, isInCleanDb = ?"
 			+ " WHERE Id = ?");
 	
 	static final String ERROR_UPDATE_GRAPH_STATE_AND_ISINCLEANDB = "Error during updating graph state";
 	
 	//-----------------------------------------------------------------------------------------------//
 		
 	/**
 	 * Select all attached graphs for given graph id.
 	 * @param first Graph ID
 	 */
 	static final String SELECT_ATTACHED_GRAPHS = String.format(
 			  " SELECT name" 
 			+ " FROM ODCLEANSTORE.EN_WORKING_ADDED_GRAPHS"
 			+ " WHERE graphId = ?");
 	
 	static final String ERROR_SELECT_ATTACHED_GRAPHS = "Error during selecting attached graphs";
 	
 	
 	/**
 	 * Insert name of attached graph to given graph id. 
 	 * @param first Graph ID
 	 * @param second Name of graph
 	 */
 	static final String INSERT_ATTACHED_GRAPH = String.format(
 			  " INSERT"  
 			+ " INTO ODCLEANSTORE.EN_WORKING_ADDED_GRAPHS(graphId, name)"
 			+ " VALUES(?,?)");
 	
 	static final String ERROR_INSERT_ATTACHED_GRAPH = "Error during inserting attached graph";
 
 	
 	/**
 	 * Delete all attached graphs for given graph id.
 	 * @param first Graph ID
 	 */
 	static final String DELETE_ATTACHED_GRAPHS = String.format(
 			  " DELETE"  
 			+ " FROM ODCLEANSTORE.EN_WORKING_ADDED_GRAPHS"
 			+ " WHERE graphId = ?");
 
 	static final String ERROR_DELETE_ATTACHED_GRAPHS = "Error during deleting attached graphs";
 	
 
 	//-----------------------------------------------------------------------------------------------//
 	
 	/**
 	 * Select default pipeline id.
 	 */
 	static final String SELECT_DEFAULT_PIPELINE = 
 			  " SELECT TOP 1 id"
 			+ " FROM ODCLEANSTORE.PIPELINES"
 			+ " WHERE isDefault != 0"; 
 	
 	static final String ERROR_SELECT_DEFAULT_PIPELINE = "Error during selecting default pipeline";
 	
 	/**
 	 * Select pipeline commands for given pipeline id.
 	 * @param first PipelineID
 	 */
 	static final String SELECT_PIPELINE_COMMANDS = 
 			  " SELECT t.jarPath, t.fullClassName, ti.workDirPath, ti.configuration, ti.runOnCleanDB, ti.id"
 			+ " FROM ODCLEANSTORE.TRANSFORMERS t, ODCLEANSTORE.PIPELINES p, ODCLEANSTORE.TRANSFORMER_INSTANCES ti"
 			+ " WHERE t.id = ti.transformerId AND ti.pipelineId = p.id" 
 			+ " AND p.id= ?" 
 			+ " ORDER BY ti.priority";
 	
 	static final String ERROR_SELECT_PIPELINE_COMMANDS = "Error during selecting pipeline commands";
 
 	
 	//-----------------------------------------------------------------------------------------------//
 	
 	/**
 	 * Select qa groups for given PipelineID.
 	 * @param first PipelineID
 	 */
 	static final String SELECT_QA_GROUPS = 
 			  " SELECT qa.transformerInstanceId, qa.groupId"
			+ " FROM ODCLEANSTORE.QA_RULES_ASSIGNMENT qa, ODCLEANSTORE.PIPELINES p, ODCLEANSTORE.TRANSFORMER_INSTANCES ti"
 			+ " WHERE qa.transformerInstanceId = ti.transformerId AND ti.pipelineId = p.id" 
 			+ " AND p.id= ?" 
 			+ " ORDER BY qa.transformerInstanceId, qa.groupId";
 	
 	static final String ERROR_SELECT_QA_GROUPS = "Error during selecting qa groups";
 	
 	
 	/**
 	 * Select dn groups for given PipelineID.
 	 * @param first PipelineID
 	 */
 	static final String SELECT_DN_GROUPS = 
 			  " SELECT dn.transformerInstanceId, dn.groupId"
 			+ " FROM ODCLEANSTORE.DN_RULES_ASSIGNMENT dn, ODCLEANSTORE.PIPELINES p, ODCLEANSTORE.TRANSFORMER_INSTANCES ti"
 			+ " WHERE dn.transformerInstanceId = ti.transformerId AND ti.pipelineId = p.id" 
 			+ " AND p.id= ?" 
 			+ " ORDER BY dn.transformerInstanceId, dn.groupId";
 	
 	static final String ERROR_SELECT_DN_GROUPS = "Error during selecting dn groups";
 	
 	//-----------------------------------------------------------------------------------------------//
 	
 	/**
 	 * Insert graph into pipeline error table.
 	 * @param first GraphID
 	 * @param second errorTypeId
 	 * @param third errorMessage  
 	 */
 	static final String INSERT_GRAPH_IN_ERROR = 
 			  " INSERT"  
 			+ " INTO ODCLEANSTORE.EN_GRAPHS_IN_ERROR(graphId, errorTypeId, errorMessage)"
 			+ " VALUES(?,?,?)";
 	
 	static final String ERROR_INSERT_GRAPH_IN_ERROR = "Error during inserting graph in graphs in error";
 }
