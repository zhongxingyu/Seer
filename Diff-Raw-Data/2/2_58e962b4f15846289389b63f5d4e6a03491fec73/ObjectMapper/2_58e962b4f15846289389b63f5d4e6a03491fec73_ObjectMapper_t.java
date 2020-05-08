 package com.pardot.rhombus;
 
 
 import com.datastax.driver.core.*;
 import com.datastax.driver.core.exceptions.AlreadyExistsException;
 import com.datastax.driver.core.utils.UUIDs;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.pardot.rhombus.cobject.*;
 import com.pardot.rhombus.cobject.async.StatementIteratorConsumer;
 import com.pardot.rhombus.util.JsonUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.UUID;
 
 /**
  * Pardot, an ExactTarget company
  * User: Michael Frank
  * Date: 4/17/13
  */
 public class ObjectMapper implements CObjectShardList {
 
 	private static Logger logger = LoggerFactory.getLogger(ObjectMapper.class);
 	private static final int reasonableStatementLimit = 20;
 	private boolean executeAsync = true;
 	private boolean logCql = false;
 	private boolean cacheBoundedQueries = true;
 	private CQLExecutor cqlExecutor;
 	private Session session;
 	private CKeyspaceDefinition keyspaceDefinition;
 	private CObjectCQLGenerator cqlGenerator;
 	private long statementTimeout = 5000;
 
 	public ObjectMapper(Session session, CKeyspaceDefinition keyspaceDefinition) {
 		this.cqlExecutor = new CQLExecutor(session, logCql);
 		this.session = session;
 		this.keyspaceDefinition = keyspaceDefinition;
 		this.cqlGenerator = new CObjectCQLGenerator(keyspaceDefinition.getDefinitions(), this);
 	}
 
 	/**
 	 * Build the tables contained in the keyspace definition.
 	 * This method assumes that its keyspace exists and
 	 * does not contain any tables.
 	 */
 	public void buildKeyspace(Boolean forceRebuild) {
 		//we are about to rework the the keyspaces, so lets clear the bounded query cache
 		cqlExecutor.clearStatementCache();
 		//First build the shard index
 		CQLStatement cql = CObjectCQLGenerator.makeCQLforShardIndexTableCreate();
 		try {
 			cqlExecutor.executeSync(cql);
 		} catch(Exception e) {
 			if(forceRebuild) {
 				CQLStatement dropCql = CObjectCQLGenerator.makeCQLforShardIndexTableDrop();
 				logger.debug("Attempting to drop table with cql {}", dropCql);
 				cqlExecutor.executeSync(dropCql);
 				cqlExecutor.executeSync(cql);
 			} else {
 				logger.debug("Not dropping shard index table");
 			}
 		}
 		//Next build the update index
 		cql = CObjectCQLGenerator.makeCQLforIndexUpdateTableCreate();
 		try{
 			cqlExecutor.executeSync(cql);
 		}
 		catch(Exception e) {
 			logger.debug("Unable to create update index table. It may already exist");
 		}
 		//Now build the tables for each object if the definition contains tables
 		if(keyspaceDefinition.getDefinitions() != null) {
 			for(CDefinition definition : keyspaceDefinition.getDefinitions().values()) {
 				CQLStatementIterator statementIterator = cqlGenerator.makeCQLforCreate(definition.getName());
 				CQLStatementIterator dropStatementIterator = cqlGenerator.makeCQLforDrop(definition.getName());
 				while(statementIterator.hasNext()) {
 					cql = statementIterator.next();
 					CQLStatement dropCql = dropStatementIterator.next();
 					try {
 						cqlExecutor.executeSync(cql);
 					} catch (AlreadyExistsException e) {
 						if(forceRebuild) {
 							logger.debug("ForceRebuild is on, dropping table");
 							cqlExecutor.executeSync(dropCql);
 							cqlExecutor.executeSync(cql);
 						} else {
 							logger.warn("Table already exists and will not be updated");
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void executeStatements(CQLStatementIterator statementIterator) {
 		List<CQLStatementIterator> statementIterators = Lists.newArrayList();
 		statementIterators.add(statementIterator);
 		executeStatements(statementIterators);
 	}
 
 	public void executeStatements(List<CQLStatementIterator> statementIterators) {
 		boolean canExecuteAsync = true;
 		for(CQLStatementIterator statementIterator : statementIterators) {
 			if(!statementIterator.isBounded()) {
 				canExecuteAsync = false;
 				break;
 			}
 		}
 		if(canExecuteAsync &&  this.executeAsync) {
 			logger.debug("Executing statements async");
 			//If this is a bounded statement iterator, send it through the async path
 			long start = System.nanoTime();
 			List<StatementIteratorConsumer> consumers = Lists.newArrayList();
 			for(CQLStatementIterator statementIterator : statementIterators) {
 				StatementIteratorConsumer consumer = new StatementIteratorConsumer((BoundedCQLStatementIterator) statementIterator, cqlExecutor, statementTimeout);
 				consumer.start();
 				consumers.add(consumer);
 			}
 			for(StatementIteratorConsumer consumer : consumers) {
 				consumer.join();
 			}
 			logger.debug("Async execution took {} ms", (System.nanoTime() - start) / 1000000);
 		} else {
 			long start = System.nanoTime();
 			for(CQLStatementIterator statementIterator : statementIterators) {
 				while(statementIterator.hasNext()) {
 					CQLStatement statement = statementIterator.next();
 					cqlExecutor.executeSync(statement);
 				}
 			}
 			logger.debug("Sync execution took {} ms", (System.nanoTime() - start) / 1000000);
 		}
 	}
 
 	@Override
 	public List<Long> getShardIdList(CDefinition def, SortedMap<String, Object> indexValues, CObjectOrdering ordering, @Nullable UUID start, @Nullable UUID end) throws CQLGenerationException {
 		CQLStatement shardIdGet = CObjectCQLGenerator.makeCQLforGetShardIndexList(def, indexValues, ordering, start, end);
 		ResultSet resultSet = cqlExecutor.executeSync(shardIdGet);
 		List<Long> shardIdList = Lists.newArrayList();
 		for(Row row : resultSet) {
 			shardIdList.add(row.getLong("shardid"));
 		}
 		return shardIdList;
 	}
 
 	/**
 	 * Insert a batch of mixed new object with values
 	 * @param objects Objects to insert
 	 * @return ID of most recently inserted object
 	 * @throws CQLGenerationException
 	 */
 	public UUID insertBatchMixed(Map<String, List<Map<String, Object>>> objects) throws CQLGenerationException {
 		logger.debug("Insert batch mixed");
 		List<CQLStatementIterator> statementIterators = Lists.newArrayList();
 		UUID key = null;
 		for(String objectType : objects.keySet()) {
 			for(Map<String, Object> values : objects.get(objectType)) {
 				key = UUIDs.timeBased();
 				long timestamp = System.currentTimeMillis();
 				CQLStatementIterator statementIterator = cqlGenerator.makeCQLforInsert(objectType, values, key, timestamp);
 				statementIterators.add(statementIterator);
 			}
 		}
 		executeStatements(statementIterators);
 		return key;
 	}
 
 	/**
 	 * Insert a new object with values and key
 	 * @param objectType Type of object to insert
 	 * @param values Values to insert
 	 * @param key Time UUID to use as key
 	 * @return ID if newly inserted object
 	 * @throws CQLGenerationException
 	 */
 	public UUID insert(String objectType, Map<String, Object> values, UUID key) throws CQLGenerationException {
 		logger.debug("Insert {}", objectType);
 		if(key == null) {
 			key = UUIDs.timeBased();
 		}
 		long timestamp = System.currentTimeMillis();
 		CQLStatementIterator statementIterator = cqlGenerator.makeCQLforInsert(objectType, values, key, timestamp);
 		executeStatements(statementIterator);
 		return key;
 	}
 
 	/**
 	 * Insert a new objectType with values
 	 * @param objectType Type of object to insert
 	 * @param values Values to insert
 	 * @return UUID of inserted object
 	 * @throws CQLGenerationException
 	 */
 	public UUID insert(String objectType, Map<String, Object> values) throws CQLGenerationException {
 		return insert(objectType, values, (UUID)null);
 	}
 
 	/**
 	 * Used to insert an object with a UUID based on the provided timestamp
 	 * Best used for testing, as time resolution collisions are not accounted for
 	 * @param objectType Type of object to insert
 	 * @param values Values to insert
 	 * @param timestamp Timestamp to use to create the object UUID
 	 * @return the UUID of the newly inserted object
 	 */
 	public UUID insert(String objectType, Map<String, Object> values, Long timestamp) throws CQLGenerationException {
 		UUID uuid = UUIDs.startOf(timestamp);
 		return insert(objectType, values, uuid);
 	}
 
 	/**
 	 * Delete Object of type with id key
 	 * @param objectType Type of object to delete
 	 * @param key Key of object to delete
 	 */
 	public void delete(String objectType, UUID key) {
 		CDefinition def = keyspaceDefinition.getDefinitions().get(objectType);
 		Map<String, Object> values = getByKey(objectType, key);
 		CQLStatementIterator statementIterator = cqlGenerator.makeCQLforDelete(objectType, key, values, null);
 		mapResults(statementIterator, def, 0L);
 	}
 
 	/**
 	 * Update objectType with key using values
 	 * @param objectType Type of object to update
 	 * @param key Key of object to update
 	 * @param values Values to update
 	 * @param timestamp Timestamp to execute update at
 	 * @param ttl Time to live for update
 	 * @return new UUID of the object
 	 * @throws CQLGenerationException
 	 */
 	public UUID update(String objectType, UUID key, Map<String, Object> values, Long timestamp, Integer ttl) throws CQLGenerationException {
 		//New Version
 		//(1) Get the old version
 		Map<String, Object> oldversion = getByKey(objectType, key);
 
 		//(2) Pass it all into the cql generator so it can create the right statements
 		CDefinition def = keyspaceDefinition.getDefinitions().get(objectType);
 		CQLStatementIterator statementIterator = cqlGenerator.makeCQLforUpdate(def,key,oldversion,values);
 		executeStatements(statementIterator);
 		return key;
 	}
 
 	/**
 	 * Update objectType with key using values
 	 * @param objectType Type of object to update
 	 * @param key Key of object to update
 	 * @param values Values to update
 	 * @return new UUID of the object
 	 * @throws CQLGenerationException
 	 */
 	public UUID update(String objectType, UUID key, Map<String, Object> values) throws CQLGenerationException {
 		return update(objectType, key, values, null, null);
 	}
 
 	/**
 	 *
 	 * @param objectType Type of object to get
 	 * @param key Key of object to get
 	 * @return Object of type with key or null if it does not exist
 	 */
 	public Map<String, Object> getByKey(String objectType, UUID key) {
 		CDefinition def = keyspaceDefinition.getDefinitions().get(objectType);
 		CQLStatementIterator statementIterator = cqlGenerator.makeCQLforGet(objectType, key);
 		List<Map<String, Object>> results = mapResults(statementIterator, def, 1L);
 		if(results.size() > 0) {
 			return results.get(0);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @param objectType Type of object to query
 	 * @param criteria Criteria to query by
 	 * @return List of objects that match the specified type and criteria
 	 * @throws CQLGenerationException
 	 */
 	public List<Map<String, Object>> list(String objectType, Criteria criteria) throws CQLGenerationException {
 		CDefinition def = keyspaceDefinition.getDefinitions().get(objectType);
 		CQLStatementIterator statementIterator = cqlGenerator.makeCQLforGet(objectType, criteria);
 		return mapResults(statementIterator, def, criteria.getLimit());
 	}
 
 	protected Map<String, Object> getNextUpdateIndexRow(@Nullable Long lastInstancetoken){
 		CQLStatement cqlForNext = (lastInstancetoken == null) ?
 			cqlGenerator.makeGetFirstEligibleIndexUpdate() : cqlGenerator.makeGetNextEligibleIndexUpdate(lastInstancetoken);
 		Map<String, Object> result = Maps.newHashMap();
		ResultSet resultSet = cqlExecutor.executeSync(cqlForNext);
 		Long nextInstanceToken = resultSet.one().getLong(0);
 
 		CQLStatement cqlForRow = cqlGenerator.makeGetRowIndexUpdate(nextInstanceToken);
 		resultSet = cqlExecutor.executeSync(cqlForRow);
 		Row row = resultSet.one();
 		result.put("id", row.getUUID("id"));
 		result.put("statictablename", row.getString("statictablename"));
 		result.put("instanceid", row.getUUID("instanceid"));
 		result.put("indexvalues", row.getString("indexvalues"));
 		return result;
 	}
 
 
 	/**
 	 * Iterates through cql statements executing them in sequence and mapping the results until limit is reached
 	 * @param statementIterator Statement iterator to execute
 	 * @param definition definition to execute the statements against
 	 * @return Ordered resultset concatenating results from statements in statement iterator.
 	 */
 	private List<Map<String, Object>> mapResults(CQLStatementIterator statementIterator, CDefinition definition, Long limit) {
 		List<Map<String, Object>> results = Lists.newArrayList();
 		int statementNumber = 0;
 		int resultNumber = 0;
 		while(statementIterator.hasNext(resultNumber) ) {
 			CQLStatement cql = statementIterator.next();
 			ResultSet resultSet = cqlExecutor.executeSync(cql);
 			for(Row row : resultSet) {
 				Map<String, Object> result = mapResult(row, definition);
 				results.add(result);
 				resultNumber++;
 			}
 			statementNumber++;
 			if((limit > 0 && resultNumber >= limit) || statementNumber > reasonableStatementLimit) {
 				logger.debug("Breaking from mapping results");
 				break;
 			}
 		}
 		return results;
 	}
 
 	/**
 	 * @param row The row to map
 	 * @param definition The definition to map the row on to
 	 * @return Data contained in a row mapped to the object described in definition.
 	 */
 	private Map<String, Object> mapResult(Row row, CDefinition definition) {
 		Map<String, Object> result = Maps.newHashMap();
 		result.put("id", row.getUUID("id"));
 		for(CField field : definition.getFields().values()) {
 			result.put(field.getName(), getFieldValue(row, field));
 		}
 		return result;
 	}
 
 	private Object getFieldValue(Row row, CField field) {
 		Object fieldValue;
 		switch(field.getType()) {
 			case ASCII:
 			case VARCHAR:
 			case TEXT:
 				fieldValue = row.getString(field.getName());
 				break;
 			case BIGINT:
 			case COUNTER:
 				fieldValue = row.getLong(field.getName());
 				break;
 			case BLOB:
 				fieldValue = row.getBytes(field.getName());
 				break;
 			case BOOLEAN:
 				fieldValue = row.getBool(field.getName());
 				break;
 			case DECIMAL:
 				fieldValue = row.getDecimal(field.getName());
 				break;
 			case DOUBLE:
 				fieldValue = row.getDouble(field.getName());
 				break;
 			case FLOAT:
 				fieldValue = row.getFloat(field.getName());
 				break;
 			case INT:
 				fieldValue = row.getInt(field.getName());
 				break;
 			case TIMESTAMP:
 				fieldValue = row.getDate(field.getName());
 				break;
 			case UUID:
 			case TIMEUUID:
 				fieldValue = row.getUUID(field.getName());
 				break;
 			case VARINT:
 				fieldValue = row.getVarint(field.getName());
 				break;
 			default:
 				fieldValue = null;
 		}
 		return (fieldValue == null ? null : fieldValue);
 	}
 
 	public Map<String, Object> coerceRhombusValuesFromJsonMap(String objectType, Map<String, Object> values) {
 		return JsonUtil.rhombusMapFromJsonMap(values, keyspaceDefinition.getDefinitions().get(objectType));
 	}
 
 	public void setLogCql(boolean logCql) {
 		this.logCql = logCql;
 		this.cqlExecutor.setLogCql(logCql);
 	}
 
 	public boolean isExecuteAsync() {
 		return executeAsync;
 	}
 
 	public void setExecuteAsync(boolean executeAsync) {
 		this.executeAsync = executeAsync;
 	}
 
 	public void teardown() {
 		session.shutdown();
 	}
 
 	public boolean isCacheBoundedQueries() {
 		return cacheBoundedQueries;
 	}
 
 	public void setCacheBoundedQueries(boolean cacheBoundedQueries) {
 		this.cacheBoundedQueries = cacheBoundedQueries;
 	}
 
 	/**
 	 * Set max execution time for insert statements run in parallel
 	 * @param statementTimeout MS to wait before timing out multi insert
 	 */
 	public void setStatementTimeout(long statementTimeout) {
 		this.statementTimeout = statementTimeout;
 	}
 
 	public CQLExecutor getCqlExecutor(){
 		return cqlExecutor;
 	}
 
 }
