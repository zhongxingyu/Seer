 package org.wso2.dss.connectors.mongodb;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jongo.Jongo;
 import org.jongo.MongoCollection;
 import org.jongo.ResultMapper;
 import org.jongo.Update;
 import org.wso2.carbon.dataservices.core.DBUtils;
 import org.wso2.carbon.dataservices.core.DataServiceFault;
 import org.wso2.carbon.dataservices.core.custom.datasource.CustomQueryBasedDS;
 import org.wso2.carbon.dataservices.core.custom.datasource.DataColumn;
 import org.wso2.carbon.dataservices.core.custom.datasource.DataRow;
 import org.wso2.carbon.dataservices.core.custom.datasource.FixedDataRow;
 import org.wso2.carbon.dataservices.core.custom.datasource.QueryResult;
 import org.wso2.carbon.dataservices.core.engine.InternalParam;
 import org.wso2.dss.connectors.mongodb.MongoDBDSConstants.MongoOperation;
 import org.wso2.dss.connectors.mongodb.MongoDBDSConstants.MongoOperationLabels;
 
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.ReadPreference;
 import com.mongodb.ServerAddress;
 import com.mongodb.WriteConcern;
 import com.mongodb.util.JSON;
 
 /**
  * The MongoDB custom data source implementation for WSO2 DSS.
  */
 public class MongoDBDataSource implements CustomQueryBasedDS {
 
 	private Mongo mongo;
 	
 	private Jongo jongo;
 	
 	@Override
 	public void init(Map<String, String> params) throws DataServiceFault {
 		String serversParam = params.get(MongoDBDSConstants.SERVERS);
 		if (DBUtils.isEmptyString(serversParam)) {
 			throw new DataServiceFault("The data source param '" + 
 					MongoDBDSConstants.SERVERS + "' is required");
 		}
 		String[] servers = serversParam.split(",");
 		String database = params.get(MongoDBDSConstants.DATABASE);
 		if (DBUtils.isEmptyString(database)) {
 			throw new DataServiceFault("The data source param '" + 
 					MongoDBDSConstants.DATABASE + "' is required");
 		}
 		try {
 		    this.mongo = new Mongo(this.createServerAddresses(servers));
 		    String writeConcern = params.get(MongoDBDSConstants.WRITE_CONCERN);
 		    if (!DBUtils.isEmptyString(writeConcern)) {
 		    	this.getMongo().setWriteConcern(WriteConcern.valueOf(writeConcern));
 		    }
 		    String readPref = params.get(MongoDBDSConstants.READ_PREFERENCE);
 		    if (!DBUtils.isEmptyString(readPref)) {
 		    	this.getMongo().setReadPreference(ReadPreference.valueOf(readPref));
 		    }
 		    this.jongo = new Jongo(this.getMongo().getDB(database));
 		} catch (Exception e) {
 			throw new DataServiceFault(e);
 		}
 	}
 	
 	public Mongo getMongo() {
 		return mongo;
 	}
 	
 	public Jongo getJongo() {
 		return jongo;
 	}
 	
 	private List<ServerAddress> createServerAddresses(String[] servers) throws Exception {
 		List<ServerAddress> result = new ArrayList<ServerAddress>();
 		String[] tmpAddr;
 		for (String server : servers) {
 			tmpAddr = server.split(":");
 			if (tmpAddr.length == 2) {
 				result.add(new ServerAddress(tmpAddr[0], Integer.parseInt(tmpAddr[1])));
 			} else {
 				result.add(new ServerAddress(tmpAddr[0]));
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public QueryResult executeQuery(String query, List<InternalParam> params)
 			throws DataServiceFault {
 		return new MongoQueryResult(query, params);
 	}
 	
 	@Override
 	public void close() {
 		this.getMongo().close();
 	}
 
 
 	private Object[] decodeQuery(String query) throws DataServiceFault {
 		int i1 = query.indexOf('.');
 		if (i1 == -1) {
 			throw new DataServiceFault("The MongoDB Collection not specified in the query '" +
 					query + "'");
 		}
 		String collection = query.substring(0, i1).trim();
 		int i2 = query.indexOf('(', i1);
 		if (i2 == -1 || i2 - i1 <= 1) {
 			throw new DataServiceFault("Invalid MongoDB operation in the query '" +
 					query + "'");
 		}
 		String operation = query.substring(i1 + 1, i2).trim();
 		int i3 = query.lastIndexOf(')');
 		if (i3 == -1) {
 			throw new DataServiceFault("Invalid MongoDB operation in the query '" +
 					query + "'");
 		}
 		String opQuery = null;
 		if (i3 - i2 > 1) {
 			opQuery = query.substring(i2 + 1, i3).trim();
 		}
 		MongoOperation mongoOp = this.convertToMongoOp(operation);
 		if (mongoOp == MongoOperation.UPDATE) {
 			List<Object> result = new ArrayList<Object>();
 			result.add(collection);
 			result.add(mongoOp);
 			result.addAll(parseInsertQuery(opQuery));
 			return result.toArray();
 		} else {
 		    return new Object[] { collection, mongoOp, this.checkAndCleanOpQuery(opQuery) };
 		}
 	}
 	
 	private String checkAndCleanOpQuery(String opQuery) throws DataServiceFault {
 		if (opQuery == null) {
 			return null;
 		}
 		int a = 0, b = 0;
 		if (opQuery.startsWith("'") || opQuery.startsWith("\"")) {
 			a = 1;
 		}
 		if (opQuery.endsWith("'") || opQuery.endsWith("\"")) {
 			b = 1;
 		}
 		return opQuery.substring(a, opQuery.length() - b);
 	}
 	
 	private List<Object> parseInsertQuery(String opQuery) throws DataServiceFault {
 		List<Object> tokens = new ArrayList<Object>();
 		int bracketCount = 0;
 		StringBuffer buff = new StringBuffer(100);
 		for (char ch : opQuery.toCharArray()) {
 			if (ch == ',' && bracketCount == 0) {
 				tokens.add(this.checkAndCleanOpQuery(buff.toString().trim()));
 				buff.delete(0, buff.length());
 			} else {
 				buff.append(ch);
 				if (ch == '{') {
 					bracketCount++;
 				} else if (ch == '}') {
 					bracketCount--;
 				}
 			}
 		}
 		String lastToken = buff.toString().trim();
 		if (lastToken.length() > 0) {
 			tokens.add(this.checkAndCleanOpQuery(lastToken));
 		}
 		return tokens;
 	}
 	
 	private MongoOperation convertToMongoOp(String operation) throws DataServiceFault {
 		if (MongoOperationLabels.COUNT.equals(operation)) {
 			return MongoOperation.COUNT;
 		} else if (MongoOperationLabels.DROP.equals(operation)) {
 			return MongoOperation.DROP;
 		} else if (MongoOperationLabels.FIND.equals(operation)) {
 			return MongoOperation.FIND;
 		} else if (MongoOperationLabels.FIND_ONE.equals(operation)) {
 			return MongoOperation.FIND_ONE;
 		} else if (MongoOperationLabels.INSERT.equals(operation)) {
 			return MongoOperation.INSERT;
 		} else if (MongoOperationLabels.REMOVE.equals(operation)) {
 			return MongoOperation.REMOVE;
 		} else if (MongoOperationLabels.UPDATE.equals(operation)) {
 			return MongoOperation.UPDATE;
 		} else {
 			throw new DataServiceFault("Unknown MongoDB operation '" + operation + "'");
 		}
 	}
 	
 	public final static class MongoResultMapper implements ResultMapper<String> {
 
 		private static final MongoResultMapper instance = new MongoResultMapper();
 		
 		private MongoResultMapper() {
 		}
 		
 		public static MongoResultMapper getInstance() {
 			return instance;
 		}
 		
 		@Override
 		public String map(DBObject dbo) {
 			return dbo.toString();
 		}
 		
 	}
 	
 	public class MongoQueryResult implements QueryResult {
 		
 		private Iterator<? extends Object> dataIterator;
 		
 		public MongoQueryResult(String query, List<InternalParam> params) throws DataServiceFault {
 			Object[] request = decodeQuery(query);
 			MongoCollection collection = getJongo().getCollection((String) request[0]);
 			String opQuery = (String) request[2];
 			Object[] mongoParams = DBUtils.convertInputParamValues(params);
 			switch ((MongoOperation) request[1]) {
 			case COUNT:
 				this.dataIterator = this.doCount(collection, opQuery, mongoParams);
 				break;
 			case FIND:
 				this.dataIterator = this.doFind(collection, opQuery, mongoParams);
 				break;
 			case FIND_ONE:
 				this.dataIterator = this.doFindOne(collection, opQuery, mongoParams);
 				break;
 			case DROP:
 				this.doDrop(collection);
 				break;
 			case INSERT:
 				this.doInsert(collection, opQuery, mongoParams);
 				break;
 			case REMOVE:
 				this.doRemove(collection, opQuery, mongoParams);
 				break;
 			case UPDATE:
 				if (request.length < 4) {
 					throw new DataServiceFault(
 							"An MongoDB update statement must contain a modifier");
 				}
 				String modifier = (String) request[3];
 				boolean upsert = false;
 				if (request.length > 4) {
 					upsert = Boolean.parseBoolean((String) request[4]);
 				}
 				boolean multi = false;
 				if (request.length > 5) {
 					multi = Boolean.parseBoolean((String) request[5]);
 				}
 				this.doUpdate(collection, opQuery, mongoParams, modifier, upsert, multi);
 				break;
 			}
 		}
 		
 		private Iterator<Long> doCount(MongoCollection collection,
 				String opQuery, Object[] parameters) {
 			long count;
 			if (opQuery != null) {
 				if (parameters.length > 0) {
 					count = collection.count(opQuery, parameters);
 				} else {
 					count = collection.count(opQuery);
 				}
 			} else {
 				count = collection.count();
 			}
 			List<Long> countResult = new ArrayList<Long>();
 			countResult.add(count);
 			return countResult.iterator();
 		}
 		
 		private Iterator<String> doFind(MongoCollection collection,
 				String opQuery, Object[] parameters) {
 			if (opQuery != null) {
 				if (parameters.length > 0) {
					return collection.find(opQuery, parameters).map(MongoResultMapper.getInstance()).iterator();
 				} else {
					return collection.find(opQuery).map(MongoResultMapper.getInstance()).iterator();
 				}
 			} else {
 				return collection.find().map(MongoResultMapper.getInstance()).iterator();
 			}
 		}
 		
 		private Iterator<String> doFindOne(MongoCollection collection,
 				String opQuery, Object[] parameters) {
 			String value;
 			if (opQuery != null) {
 				if (parameters.length > 0) {
					value = collection.findOne(opQuery, parameters).map(MongoResultMapper.getInstance());
 				} else {
 					value = collection.findOne(opQuery).map(MongoResultMapper.getInstance());
 				}
 			} else {
 				value = collection.findOne().map(MongoResultMapper.getInstance());
 			}
 			List<String> result = new ArrayList<String>();
 			result.add(value);
 			return result.iterator();
 		}
 		
 		private void doInsert(MongoCollection collection,
 				String opQuery, Object[] parameters) throws DataServiceFault {
 			String error;
 			if (opQuery != null) {
 				if (parameters.length > 0) {
 					if (opQuery.equals("#")) {
 						error = collection.save(JSON.parse(parameters[0].toString())).getError();
 					} else {
 					    error = collection.insert(opQuery, parameters).getError();
 					}
 				} else {
 					error = collection.insert(opQuery).getError();
 				}
 			} else {
 				throw new DataServiceFault("Mongo insert statements must contain a query");
 			}
 			if (!DBUtils.isEmptyString(error)) {
 				throw new DataServiceFault(error);
 			}
 		}
 		
 		private void doRemove(MongoCollection collection,
 				String opQuery, Object[] parameters) throws DataServiceFault {
 			String error;
 			if (opQuery != null) {
 				if (parameters.length > 0) {
 					error = collection.remove(opQuery, parameters).getError();
 				} else {
 					error = collection.remove(opQuery).getError();
 				}
 			} else {
 				throw new DataServiceFault("Mongo remove statements must contain a query");
 			}
 			if (!DBUtils.isEmptyString(error)) {
 				throw new DataServiceFault(error);
 			}
 		}
 		
 		private void doUpdate(MongoCollection collection,
 				String opQuery, Object[] parameters, String modifier, 
 				boolean upsert, boolean multi) throws DataServiceFault {
 			String error;
 			if (opQuery != null) {
 				if (parameters.length > 0) {
 					Update update = collection.update(opQuery);
 					if (upsert) {
 						update = update.upsert();
 					}
 					if (multi) {
 						update = update.multi();
 					}
 					error = update.with(modifier, parameters).getError();
 				} else {
 					Update update = collection.update(opQuery);
 					if (upsert) {
 						update = update.upsert();
 					}
 					if (multi) {
 						update = update.multi();
 					}
 					error = update.with(modifier).getError();
 				}
 			} else {
 				throw new DataServiceFault("Mongo update statements must contain a query");
 			}
 			if (!DBUtils.isEmptyString(error)) {
 				throw new DataServiceFault(error);
 			}
 		}
 		
 		private void doDrop(MongoCollection collection) {
 			collection.drop();
 		}
 		
 		@Override
 		public List<DataColumn> getDataColumns() throws DataServiceFault {
 			List<DataColumn> result = new ArrayList<DataColumn>();
 			result.add(new DataColumn(MongoDBDSConstants.RESULT_COLUMN_NAME));
 			return result;
 		}
 
 		@Override
 		public boolean hasNext() throws DataServiceFault {
 			return this.dataIterator != null && this.dataIterator.hasNext();
 		}
 
 		@Override
 		public DataRow next() throws DataServiceFault {
 			if (this.dataIterator == null) {
 				throw new DataServiceFault("No Mongo data result available");
 			} else {
 				Object data = this.dataIterator.next();
 				Map<String, String> values = new HashMap<String, String>();
 				values.put(MongoDBDSConstants.RESULT_COLUMN_NAME, data.toString());
 				return new FixedDataRow(values);
 			}
 		}
 		
 	}
 
 }
