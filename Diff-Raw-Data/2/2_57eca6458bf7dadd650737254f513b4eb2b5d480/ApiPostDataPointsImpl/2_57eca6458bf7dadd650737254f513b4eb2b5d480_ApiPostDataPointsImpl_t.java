 package controllers;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import models.KeyToTableName;
 import models.SecureTable;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.solr.common.SolrInputDocument;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.mvc.Http.Request;
 import play.mvc.results.BadRequest;
 import play.mvc.results.Unauthorized;
 
 import com.alvazan.orm.api.base.NoSqlEntityManager;
 import com.alvazan.orm.api.z3api.NoSqlTypedSession;
 import com.alvazan.orm.api.z8spi.KeyValue;
 import com.alvazan.orm.api.z8spi.conv.StandardConverters;
 import com.alvazan.orm.api.z8spi.meta.DboColumnMeta;
 import com.alvazan.orm.api.z8spi.meta.DboTableMeta;
 import com.alvazan.orm.api.z8spi.meta.TypedColumn;
 import com.alvazan.orm.api.z8spi.meta.TypedRow;
 import com.alvazan.play.NoSql;
 
 import controllers.api.DocumentUtil;
 
 public class ApiPostDataPointsImpl {
 
 	private static final String STATE_COL_NAME = "_state";
 
 	public static String HISTORY_PREFIX = "_history";
 
 	private static final Logger log = LoggerFactory.getLogger(ApiPostDataPointsImpl.class);
 	
 	private static final ObjectMapper mapper = new ObjectMapper();
 
 	public static int postDataImpl(String json, Map<String, Object> data, String user, String password) {
 		//temporary version of 'upload module'.
 		//eventually we will have the concept of an upload module just like our current 
 		//modules that run on data retrieval.  For now, lets keep the interface the same 
 		//(chain functions into the url with options in perens), but just implement this 
 		//one 'module' here.
 		boolean timeIsISOFormat = false;
 		String timeISOFormatColumn = "time";
 		String timeISOStringFormat = "";
 		if (StringUtils.contains(Request.current().path, "UTCV1")) {
 			timeIsISOFormat = true;
 			String utcModString = StringUtils.substringBetween(Request.current().path, "/UTCV1(", ")/");
 			if (StringUtils.isNotEmpty(utcModString)) {
 				String[] options = StringUtils.split(utcModString, ",");
 				for (String s:options) {
 					String[] parts = StringUtils.split(utcModString, "=");
 					if (parts.length == 2 && StringUtils.equals(parts[0].trim(), "columnName"))
 						timeISOFormatColumn = parts[1];
 					else if (parts.length == 2 && StringUtils.equals(parts[0].trim(), "timeFormat"))
 						timeISOStringFormat = parts[1];
 					else
 						throw new RuntimeException("The format of the UTC module is not correct, it must be ../UTCV1/... or .../UTCV1(columnName=<colName>)/... or .../UTCV1(columnName=<colName>,timeFormat=<timeFormat>)/...");
 				}
 			}
 		}		
 		
 				
 		NoSqlEntityManager s = NoSql.em();
 		List dataPoints = (List) data.get("_dataset");
 		if(dataPoints == null) {
 			//let's just wrap the single datapoint in a batch then instead...
 			dataPoints = new ArrayList();
 			dataPoints.add(data);
 		}
 		
 		Collection<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>();
 
 		List<String> rowKeys = new ArrayList<String>();
 		List<Map<String, String>> dataPts = new ArrayList<Map<String,String>>();
 		for(Object map : dataPoints) {
 			Map<String, String> row = (Map<String, String>) map;
 			String tableName = (String) row.get("_tableName");
 			
 			@Deprecated  //These next TWO lines it to go away after 12/15/12 release...
 			String key = (String) row.get("_postKey");
 			String rowKey = KeyToTableName.formKey(tableName, key);
 			if(user != null || key == null) {
 				if(user == null) {
 					if (log.isInfoEnabled())
 						log.info("user is not there, we require user and password");
 					throw new Unauthorized("user must be supplied and was null in basic auth");
 				}
 
 				rowKey = KeyToTableName.formKey(tableName, user, password);
 			}
 
 			rowKeys.add(rowKey);
 			dataPts.add(row);
 		}
 
 		//Now find all keys in one shot...
 		List<KeyValue<KeyToTableName>> info = s.findAllList(KeyToTableName.class, rowKeys);
 		
 		Set<TableKey> keyCheck = new HashSet<TableKey>();
 		for(int i = 0; i < dataPts.size(); i++) {
 			KeyValue<KeyToTableName> keyValue = info.get(i);
 			Map<String, String> row = dataPts.get(i);
 			processData(row, solrDocs, keyValue, user, password, keyCheck, timeIsISOFormat, timeISOFormatColumn, timeISOStringFormat);
 		}
 		
 		s.flush();
 		
 		SearchPosting.saveSolr(json, solrDocs, null);
 		
 		return dataPoints.size();
 	}
 
 
 	private static void processData(Map<String, String> json,
 			Collection<SolrInputDocument> solrDocs, KeyValue<KeyToTableName> keyValue, String user, String password, Set<TableKey> keyCheck,
 			boolean timeIsISOFormat, String timeISOFormatColumn, String timeISOStringFormat) {
 		
 		checkSize(json);
 		
 		String tableName = json.get("_tableName");
 		KeyToTableName info = keyValue.getValue();
 		if(info == null) {
 			if (log.isInfoEnabled())
 				log.info("user="+user+" has no access to table="+tableName);
 			throw new Unauthorized("user="+user+" has no access to table="+tableName);
 		}
 		
 		boolean isUpdate = false;
 		Object updateStr = json.get("_update");
 		if("true".equals(updateStr) || Boolean.TRUE.equals(updateStr))
 			isUpdate = true;
 		
 		DboTableMeta table = info.getTableMeta();
 		
 		NoSqlTypedSession typedSession = NoSql.em().getTypedSession();
 		
 		Object pkValue = json.get(table.getIdColumnMeta().getColumnName());
 		if(pkValue == null) {
 			if (log.isWarnEnabled())
         		log.warn("The table you are inserting requires column='"+table.getIdColumnMeta().getColumnName()+"' to be set and is not found in json request="+json);
 			throw new BadRequest("The table you are inserting requires column='"+table.getIdColumnMeta().getColumnName()+"' to be set and is not found in json request="+json);
 		}
 		
 		//part of short term fix to put date formatting in and have it look like 'upload module'
		if (timeIsISOFormat && table.getIdColumnMeta().getColumnName().equals(timeISOFormatColumn))
 			pkValue = getTimeAsMillisFromString((String)pkValue, timeISOStringFormat);
 
 		TableKey theKey = new TableKey(tableName, pkValue);
 		if(keyCheck.contains(theKey))
 			throw new BadRequest("Your post contains two rows with the same key and table name.  key="+pkValue+" tablename="+tableName+".  This is not allowed");
 		keyCheck.add(theKey);
 		
 		if(table.isTimeSeries())
 			postTimeSeries(json, info, table, pkValue, timeIsISOFormat, timeISOFormatColumn, timeISOStringFormat);
 		else
 			postNormalTable(json, solrDocs, info, isUpdate, table, pkValue, timeIsISOFormat, timeISOFormatColumn, timeISOStringFormat);
 	}
 
 	private static void postTimeSeries(Map<String, String> json,
 			KeyToTableName info, DboTableMeta table, Object pkValue,
 			boolean timeIsISOFormat, String timeISOFormatColumn, String timeISOStringFormat) {
 		if (timeIsISOFormat)
 			throw new BadRequest("Currently Iso Date Format is not supported with the TIME_SERIES table type");
 		if (log.isInfoEnabled())
 			log.info("table name = '" + table.getColumnFamily() + "'");
 		NoSqlTypedSession typedSession = NoSql.em().getTypedSession();		
 		String cf = table.getColumnFamily();
 		
 		DboColumnMeta idColumnMeta = table.getIdColumnMeta();
 		//rowKey better be BigInteger
 		Object timeStamp = convertToStorage(idColumnMeta, pkValue);
 		byte[] colKey = idColumnMeta.convertToStorage2(timeStamp);
 		BigInteger time = (BigInteger) timeStamp;
 		long longTime = time.longValue();
 		//find the partition
 		Long partitionSize = table.getTimeSeriesPartionSize();
 		long partitionKey = (longTime / partitionSize) * partitionSize;
 
 		TypedRow row = typedSession.createTypedRow(table.getColumnFamily());
 		row.setRowKey(new BigInteger(""+partitionKey));	
 		
 		Collection<DboColumnMeta> cols = table.getAllColumns();
 
 		DboColumnMeta col = cols.iterator().next();
 		Object node = json.get(col.getColumnName());
 		if(node == null) {
 			if (log.isWarnEnabled())
 				log.warn("The table you are inserting requires column='"+col.getColumnName()+"' to be set and is not found in json request="+json);
 			throw new BadRequest("The table you are inserting requires column='"+col.getColumnName()+"' to be set and is not found in json request="+json);
 		}
 		
 		Object newValue = convertToStorage(col, node);
 		byte[] val = col.convertToStorage2(newValue);
 		row.addColumn(colKey, val, null);
 
 		//This method also indexes according to the meta data as well
 		typedSession.put(cf, row);
 	}
 
 	private static void postNormalTable(Map<String, String> json,
 			Collection<SolrInputDocument> solrDocs, KeyToTableName info,
 			boolean isUpdate, DboTableMeta table, Object pkValue,
 			boolean timeIsISOFormat, String timeISOFormatColumn, String timeISOStringFormat) {
 		if (log.isInfoEnabled())
 			log.info("normal table name = '" + table.getColumnFamily() + "'");
 
 		NoSqlTypedSession typedSession = NoSql.em().getTypedSession();
 		
 		DboColumnMeta idColumnMeta = table.getIdColumnMeta();
 		Object rowKey = convertToStorage(idColumnMeta, pkValue);
 		String cf = table.getColumnFamily();
 //		TypedRow row = typedSession.find(cf, rowKey);
 //		if(row == null) {
 			//create new row
 			TypedRow row = typedSession.createTypedRow(table.getColumnFamily());
 			row.setRowKey(rowKey);			
 //		} else if(!isUpdate) {
 //			if (log.isWarnEnabled())
 //        		log.warn("pk already in use="+pkValue+" table="+tableName+" user needs to use _update=true in their json");
 //			throw new BadRequest("This row for table="+tableName+" and primary key="+pkValue+" already exists.  Use _update=true in your json if you really want to modify this value");
 //		}
 
 		Collection<DboColumnMeta> cols = table.getAllColumns();
 		
 		long timestamp = System.currentTimeMillis();
 		for(DboColumnMeta col : cols) {
 			Object node = json.get(col.getColumnName());
 			if(node == null) {
 				if (log.isWarnEnabled())
 	        		log.warn("The table you are inserting requires column='"+col.getColumnName()+"' to be set and is not found in json request="+json);
 				throw new BadRequest("The table you are inserting requires column='"+col.getColumnName()+"' to be set and is not found in json request="+json);
 			}
 
 			if (timeIsISOFormat && StringUtils.equals(col.getColumnName(), timeISOFormatColumn))
 				node = getTimeAsMillisFromString((String)node, timeISOStringFormat);
 
 			addColumnData(isUpdate, row, col, node, timestamp);
 		}
 
 		addHistoryInfo(isUpdate, row, timestamp, json);
 		
 		//This method also indexes according to the meta data as well
 		typedSession.put(cf, row);
 		
 		SecureTable sdiTable = info.getSdiTableMeta();
 		SearchPosting.addSolrDataDoc(row, sdiTable, solrDocs);
 	}
 
 	private static Object getTimeAsMillisFromString(String node, String format) {
 		DateTimeFormatter parser = ISODateTimeFormat.basicDateTimeNoMillis();
 		if (StringUtils.isNotBlank(format)) {
 			try {
 				parser = DateTimeFormat.forPattern(format);
 			}
 			catch (IllegalArgumentException iae) {
 				throw new RuntimeException("The date time format you provided is not legal, see this page for valid date formatting http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html");
 			}
 		}
 		Long UtcMillis = parser.parseDateTime(node).getMillis();
 		return ""+UtcMillis;
 		
 	}
 
 
 	private static void addColumnData(boolean isUpdate, TypedRow row, DboColumnMeta col, Object node, long time) {
 		Object newValue = convertToStorage(col, node);
 		if(isUpdate) {
 			//okay, we are updating a row so we need to version this row then so we have the previous contents of the row as well
 			transferColToHistory(row, col, newValue, time);
 		}
 		
 		row.addColumn(col.getColumnName(), newValue);
 	}
 
 	private static void transferColToHistory(TypedRow row, DboColumnMeta col, Object newValue, long time) {
 		String colName = col.getColumnName();
 		TypedColumn column = row.getColumn(colName);
 		if(column == null)
 			return; //nothing to do since it doesn't exist
 
 		Object oldValue = column.getValue();
 		if(newValue.equals(oldValue))
 			return; //nothing to do if values are the same
 		
 		//okay, the value was changed so we must save the oldValue in the history then
 		//the name is _history.<timestamp>.<columnname> for each column that is changed
 		byte[] namePrefix = StandardConverters.convertToBytes(HISTORY_PREFIX);
 		byte[] nameAsBytes = col.getColumnNameAsBytes();
 
 		byte[] name = DocumentUtil.combineName(namePrefix, time, nameAsBytes);
 		byte[] valueRaw = column.getValueRaw();
 		row.addColumn(name, valueRaw, null);
 	}
 
 	private static void addHistoryInfo(boolean isUpdate, TypedRow row, long time, Map<String, String> json) {
 		String state = json.get(STATE_COL_NAME);
 		String user = json.get("_user");
 		String description = json.get("_description");
 		byte[] namePrefix = StandardConverters.convertToBytes(HISTORY_PREFIX);
 		byte[] stateColName = StandardConverters.convertToBytes(STATE_COL_NAME);
 		byte[] name = DocumentUtil.combineName(namePrefix, time, stateColName);
 
 		if(isUpdate) {
 			transferToHistory(row, stateColName, name);
 		}
 
 		//If user supplies any values add the column
 		if(state != null || user != null || description != null) {
 			byte[] valueRaw = DocumentUtil.createDocument(state, user, description);
 			row.addColumn(stateColName, valueRaw, null);
 		} else {
 			//null out the column in case it existed before...
 			row.removeColumn(stateColName);
 		}
 	}
 
 	private static void transferToHistory(TypedRow row, byte[] stateColName, byte[] historyColName) {
 		TypedColumn column = row.getColumn(stateColName);
 		if(column == null)
 			return;
 
 		byte[] valueRaw = column.getValueRaw();
 		row.addColumn(historyColName, valueRaw, null);
 	}
 
 	public static Object convertToStorage(DboColumnMeta col, Object someVal) {
 		try {
 			if(someVal == null)
 				return null;
 			else if("null".equals(someVal))
 				return null; //a fix for when they pass us "null" instead of null
 			
 			String val = ""+someVal;
 			if(val.length() == 0)
 				val = null;
 			return col.convertStringToType(val);
 		} catch(Exception e) { 
 			//Why javassist library throws a checked exception, I don't know as we can't catch a checked exception here
 			if(e instanceof InvocationTargetException &&
 					e.getCause() instanceof NumberFormatException) {
 				if (log.isWarnEnabled())
 	        		log.warn("Cannot convert value="+someVal+" for column="+col.getColumnName()+" table="+col.getOwner().getRealColumnFamily()+" as it needs to be type="+col.getClassType(), e.getCause());
 			}
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private static void checkSize(Map<String, String> jsonMap) {
 		//JsonNode jsonNode = mapper.valueToTree(jsonMap);
 		StringWriter strWriter = new StringWriter();
 		try {
 			mapper.writeValue(strWriter, jsonMap);
 			String json = strWriter.toString();
 			if(json.length() > 5000) {
 				if (log.isInfoEnabled())
 					log.info("json data point larger than 5000");
 				throw new BadRequest("You have a single data point in your batch that is larger than 5000 characters which is not allowed.  If you need this contact us to raise limit");
 			}
 		} catch (JsonGenerationException e) {
 			if (log.isWarnEnabled())
         		log.warn("parse issue1", e);
 			throw new Error("Bug marshalling out to json");
 		} catch (JsonMappingException e) {
 			if (log.isWarnEnabled())
         		log.warn("parse issue2", e);
 			throw new Error("Bug marshalling out to json");
 		} catch (IOException e) {
 			if (log.isWarnEnabled())
         		log.warn("parse issue3", e);
 			throw new Error("Bug marshalling out to json");
 		}
 	}
 }
