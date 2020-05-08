 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package health.database.DAO;
 
 import health.hbase.models.HBaseDataImport;
 import health.input.jsonmodels.JsonDataPoints;
 import health.input.jsonmodels.JsonDataValues;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.Set;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.filter.BinaryComparator;
 import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
 import org.apache.hadoop.hbase.filter.CompareFilter;
 import org.apache.hadoop.hbase.filter.FilterList;
 import org.apache.hadoop.hbase.filter.RowFilter;
 import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import server.exception.ErrorCodeException;
 import util.DateUtil;
 import util.HBaseConfig;
 
 /**
  * 
  * @author Leon
  */
 public class DataPointDAO extends BaseDAO {
 
 	private final byte[] PROP_COL = Bytes.toBytes("p");
 	private final byte[] VALUE_COL = Bytes.toBytes("v");
 	private final byte[] VALUE_TAG_COL = Bytes.toBytes("vt");
 	private final byte[] TIME_TAG = Bytes.toBytes("tt");
 	private final byte[] BLOCK_ID = Bytes.toBytes("b");
 	private final String health_book = "hb";
 	private final String str_unassignBlockID = "noid";
 	private final byte[] unassignBlockID = Bytes.toBytes(str_unassignBlockID);
 	//create 'hb','p','v','vt'
 	public DataPointDAO() throws IOException {
 		HBaseConfig hbaseconfig = new HBaseConfig();
 		System.out.println("initualizeing......new HBaseConfig();..");
 		Configuration config = hbaseconfig.getConfig();
 		System.out.println("initualizeing......getConfig..");
 //		HBaseAdmin admin = new HBaseAdmin(config);
 //		System.out.println("initualizeing......admin..");
 //		if (!admin.tableExists(health_book)) {
 //			System.out.println("initualizeing......createTable..");
 //			createTable();
 //		}
 		System.out.println("initualizeing......DONE!..");
 	}
 
 	public void createTable() throws MasterNotRunningException,
 			ZooKeeperConnectionException, IOException {
 		HBaseConfig hbaseconfig = new HBaseConfig();
 		Configuration config = hbaseconfig.getConfig();
 		HBaseAdmin admin = new HBaseAdmin(config);
 		HTableDescriptor desc = new HTableDescriptor(health_book);
 		HColumnDescriptor prop = new HColumnDescriptor(PROP_COL);
 		HColumnDescriptor value_col = new HColumnDescriptor(VALUE_COL);
 		HColumnDescriptor val_tag_col = new HColumnDescriptor(VALUE_TAG_COL);
 		desc.addFamily(prop);
 		desc.addFamily(val_tag_col);
 		desc.addFamily(value_col);
 		admin.createTable(desc);
 
 	}
 
 	public int importDatapoints(HBaseDataImport importData,
 			String newBlockName, String newBlockDecs)
 			throws ErrorCodeException, NumberFormatException, IOException,
 			ParseException {
 		DatastreamDAO dsDAO = new DatastreamDAO();
 		return importDatapoints(importData);
 	}
 
 	public int importDatapoints(HBaseDataImport importData)
 			throws ErrorCodeException, NumberFormatException, IOException,
 			ParseException {
 		int dataCounter = 0;
 		HTableInterface table = HBaseConfig.getTable(health_book);
 		List<JsonDataPoints> dataPoints = importData.getData_points();
 		List<Put> putList = new ArrayList<Put>();
 		for (int i = 0; i < dataPoints.size(); i++) {
 			long longAt = 0;
 			try {
 				longAt = Long.parseLong(dataPoints.get(i).getAt());
 			} catch (NumberFormatException ex) {
 				ex.printStackTrace();
 				throw new NumberFormatException();
 			}
 			// server.conf.Utils.log(importData.getStreamID() + "/" +
 			// Long.toString(longAt));
 			byte[] rowKey = toBytes(importData.getDatastream_id() + "/"
 					+ Long.toString(longAt));
 			dataCounter = dataCounter + rowKey.length;
 			Put put = new Put(rowKey);
 			List<JsonDataValues> dataValues = dataPoints.get(i).getValue_list();
 			for (JsonDataValues value : dataValues) {
 				put.add(VALUE_COL, toBytes(value.getUnit_id()),
 						toBytes(value.getVal()));
 				dataCounter = dataCounter + rowKey.length + VALUE_COL.length
 						+ toBytes(value.getUnit_id()).length
 						+ toBytes(value.getVal()).length;
 				if (value.getVal_tag() != null) {
 					put.add(VALUE_TAG_COL, toBytes(value.getUnit_id()),
 							toBytes(value.getVal_tag()));
 					dataCounter = dataCounter
 							+ rowKey.length
 							+ VALUE_TAG_COL.length
 							+ toBytes(value.getUnit_id() + value.getVal_tag()).length;
 				}
 			}
 			if (importData.getBlock_id() != null) {
 				put.add(PROP_COL, BLOCK_ID, toBytes(importData.getBlock_id()));
 				dataCounter = dataCounter + rowKey.length + PROP_COL.length
 						+ BLOCK_ID.length
 						+ toBytes(importData.getBlock_id()).length;
 			} else {
 				put.add(PROP_COL, BLOCK_ID, unassignBlockID);
 				dataCounter = dataCounter + rowKey.length + PROP_COL.length
 						+ BLOCK_ID.length + unassignBlockID.length;
 			}
 			if (dataPoints.get(i).getTimetag() != null) {
 				put.add(PROP_COL, TIME_TAG, toBytes(dataPoints.get(i)
 						.getTimetag()));
 				dataCounter = dataCounter + rowKey.length + PROP_COL.length
 						+ TIME_TAG.length
 						+ toBytes(dataPoints.get(i).getTimetag()).length;
 			}
 			putList.add(put);
 		}
 		table.put(putList);
 		HBaseConfig.putTable(table);
 		return dataCounter;
 	}
 
 	public HBaseDataImport exportDatapoints(String streamID, Long start,
 			Long end, String blockID, HashMap<String, String> dsUnitsList,
 			SimpleDateFormat format) throws ErrorCodeException, IOException,
 			Throwable {
 		Date timerStart = new Date();
 		System.out.println("starting Exporting...." + timerStart);
 		HTableInterface table = HBaseConfig.getTable(health_book);
 		Scan scan = new Scan();
 		scan.setCaching(200000);
 
 		// scan.setStartRow(toBytes(streamID + "/" + Long.toString(start)));
 		// scan.setCacheBlocks(true);
 		FilterList filterList = new FilterList();
 		RowFilter rowFilterStreamID = new RowFilter(
 				CompareFilter.CompareOp.EQUAL, new BinaryPrefixComparator(
 						toBytes(streamID)));
 		filterList.addFilter(rowFilterStreamID);
 		if (start != null && start != 0) {
 			RowFilter fowFilter_startDate = new RowFilter(
 					CompareFilter.CompareOp.GREATER_OR_EQUAL,
 					new BinaryComparator(toBytes(streamID + "/"
 							+ Long.toString(start))));
 			filterList.addFilter(fowFilter_startDate);
 		}
 		if (end != null && end != 0) {
 			RowFilter fowFilter_endDate = new RowFilter(
 					CompareFilter.CompareOp.LESS_OR_EQUAL,
 					new BinaryComparator(toBytes(streamID + "/"
 							+ Long.toString(end))));
 			filterList.addFilter(fowFilter_endDate);
 		}
 		if (blockID != null && !blockID.equalsIgnoreCase(str_unassignBlockID)) {
 			SingleColumnValueFilter blockIDfilter = new SingleColumnValueFilter(
 					PROP_COL, BLOCK_ID, CompareFilter.CompareOp.EQUAL,
 					toBytes(blockID));
 			blockIDfilter.setFilterIfMissing(true);
 			filterList.addFilter(blockIDfilter);
 		}
 		if (dsUnitsList != null && dsUnitsList.size() > 0) {
 			Set<String> entrySet = dsUnitsList.keySet();
 			Iterator<String> itr = entrySet.iterator();
 			while (itr.hasNext()) {
 				String id = itr.next();
 				scan.addColumn(VALUE_COL, toBytes(id));
 				scan.addColumn(VALUE_TAG_COL, toBytes(id));
 			}
 			scan.addFamily(PROP_COL);
 			scan.addFamily(VALUE_TAG_COL);
 		}
 		scan.setFilter(filterList);
 
 		// long rowCount = aggregationClient.rowCount(toBytes(health_book),
 		// null, scan);
 		// System.out.println("totalnumber of rows:"+rowCount);
 		ResultScanner scanner = table.getScanner(scan);
 		Iterator<Result> itr = scanner.iterator();
 		// List<Result> res_List = new ArrayList<Result>();
 		// while (itr.hasNext()) {
 		// Result result = itr.next();
 		// res_List.add(result);
 		// }
 		List<JsonDataPoints> jsonDPList = new ArrayList<JsonDataPoints>();
 	
 		int counter = 0;
 		while (itr.hasNext()) {
 			counter++;
 //			if (counter > max) {
 //				continue;
 //			}
 			Result res = itr.next();
 			JsonDataPoints datapoint = new JsonDataPoints();
 			List<JsonDataValues> hbaseDatavalueList = toJsonDatavaluesModel(
 					res, dsUnitsList);
 			// System.out.println("List<JsonDataValues>:" +
 			// hbaseDatavalueList.size());
 			if (hbaseDatavalueList != null) {
 				datapoint.setValue_list(hbaseDatavalueList);
 			}
 			if (format == null) {
 				Date date=new Date();
 				date.setTime(Long.parseLong(getTimefromRowKey(res)));
 				datapoint.setAt(String.valueOf(date.getTime()));
 			
 			} else {
 				Date date=new Date();
 				date.setTime(Long.parseLong(getTimefromRowKey(res)));
 				datapoint.setAt(format.format(date));
 			}
 			if(counter<=1)
 			{
 				System.out.println(counter+",First Date from Export:"+Long.parseLong(getTimefromRowKey(res)));
 				Date date=new Date();
 				date.setTime(Long.parseLong(getTimefromRowKey(res)));
 				//datapoint.setAt(format.format(date));
 				System.out.println(counter+",First Date from Export long:"+date.getTime());
 				System.out.println(counter+",First Date from Export Format:"+DateUtil.millisecFormat.format(date));
 				
 			}
 			if (res.getValue(PROP_COL, TIME_TAG) != null) {
 				datapoint
 						.setTimetag(toString(res.getValue(PROP_COL, TIME_TAG)));
 			}
 			jsonDPList.add(datapoint);
 		}
 		HBaseConfig.putTable(table);
 		HBaseDataImport dataexport = null;
 		if (jsonDPList.size() > 0) {
 			dataexport = new HBaseDataImport();
 			dataexport.setBlock_id(blockID);
 			dataexport.setData_points(jsonDPList);
 			dataexport.setDatastream_id(streamID);
 			dataexport.setDeviceid(streamID);
 		}
 		Date timerEnd = new Date();
 		System.out.println("Finished Exporting...." + timerEnd);
 		System.out.println("Finished Exporting...."
 				+ (timerEnd.getTime() - timerStart.getTime()) / (1000.00)
 				+ "seconds");
 		return dataexport;
 	}
 
 	public String getTimefromRowKey(Result result) {
 		byte[] rowbytes = result.getRow();
 		String rowKey = toString(rowbytes);
 		String[] values = rowKey.split("/");
 		Date date=new Date(Long.parseLong(values[1]));
 		return Long.toString(date.getTime());
 		
 	}
 
 	public String getStreamIDfromRowKey(Result result) {
 		byte[] rowbytes = result.getRow();
 		String rowKey = toString(rowbytes);
 		String[] values = rowKey.split("/");
 		return values[0];
 	}
 
 	public List<JsonDataValues> toJsonDatavaluesModel(Result res,
 			HashMap<String, String> dsUnitsList) {
 		NavigableMap<byte[], byte[]> familyMap = res.getFamilyMap(VALUE_COL);
 		Set<Map.Entry<byte[], byte[]>> entries = (Set<Map.Entry<byte[], byte[]>>) (Set<Map.Entry<byte[], byte[]>>) familyMap
 				.entrySet();
 		Iterator<Entry<byte[], byte[]>> itrFamily = entries.iterator();
 		List<JsonDataValues> valueList = new ArrayList<JsonDataValues>();
 		while (itrFamily.hasNext()) {
 			Map.Entry<byte[], byte[]> data = (Map.Entry<byte[], byte[]>) itrFamily
 					.next();
 			// System.out.println("Row:" + toString(res.getRow()) + "," +
 			// toString(data.getKey()) + "," + toString(data.getValue()));
 
 			JsonDataValues value = new JsonDataValues();
 			value.setUnit_id(toString(data.getKey()));
 			value.setVal(toString(data.getValue()));
 			if (res.getValue(VALUE_TAG_COL, data.getKey()) != null) {
 				value.setVal_tag(toString(res.getValue(VALUE_TAG_COL,
 						data.getKey())));
 			}
 			if (dsUnitsList != null && dsUnitsList.size() > 0) {
 				if (dsUnitsList.containsKey(value.getUnit_id())) {
 					// System.out.println("adding:"+dsUnitsList.get(toString(data.getKey())));
 					valueList.add(value);
 				} else {
 					// System.out.println("not included" +
 					// toString(data.getKey()));
 				}
 			} else {
 				// System.out.println("is null and -----------");
 				valueList.add(value);
 			}
 		}
 		// System.out.println("valueList size:" + valueList.size());
 		return valueList;
 	}
 
 	public long delete_A_Datapoint(String streamID, long at)
 			throws IOException, ErrorCodeException {
 		Date timerStart = new Date();
 		System.out.println("starting Exporting...." + at);
 		HTableInterface table = HBaseConfig.getTable(health_book);
 		Scan scan = new Scan();
 		scan.setCaching(200000);
 		// scan.setStartRow(toBytes(streamID + "/" + Long.toString(start)));
 		// scan.setCacheBlocks(true);
 		FilterList filterList = new FilterList();
 		RowFilter rowFilterStreamID = new RowFilter(
 				CompareFilter.CompareOp.EQUAL, new BinaryPrefixComparator(
 						toBytes(streamID)));
 		filterList.addFilter(rowFilterStreamID);
 
 		RowFilter fowFilter_startDate = new RowFilter(
 				CompareFilter.CompareOp.EQUAL, new BinaryComparator(
 						toBytes(streamID + "/" + Long.toString(at))));
 		filterList.addFilter(fowFilter_startDate);
 
 		scan.setFilter(filterList);
 		ResultScanner scanner = table.getScanner(scan);
 		Iterator<Result> itr = scanner.iterator();
 		long no_deleted = 0;
 		while (itr.hasNext()) {
 			Result res = itr.next();
 			System.out.println("deleting..." + toString(res.getRow()));
 			Delete delete = new Delete(res.getRow());
 			table.delete(delete);
 			no_deleted++;
 		}
 		HBaseConfig.putTable(table);
 		HBaseDataImport dataexport = null;
 		Date timerEnd = new Date();
 		System.out.println("Finished Deleting...." + timerEnd);
 		System.out.println("Finished Deleting...."
 				+ (timerEnd.getTime() - timerStart.getTime()) / (1000.00)
 				+ "seconds");
 		return no_deleted;
 	}
 
 	public static void main(String args[]) throws MasterNotRunningException,
 			IOException, ParseException, ErrorCodeException {
 		// DataPointDAO importDao = new DataPointDAO();
 		// long start =
 		// DateUtil.parseMillisecFormatToLong("21:49:59.725 16/11/2003");
 		// long end =
 		// DateUtil.parseMillisecFormatToLong("21:49:59.722 16/11/2012");
 		// System.out.println(Long.toString(start));
 
 		// importDao.exportDatapoints("70376e83-10b4-4de8-bde9-989e3111cf69",
 		// start, end, null);
 	}
 
 	public byte[] toBytes(String str) {
 		return Bytes.toBytes(str);
 	}
 
 	public String toString(byte[] bytes) {
 		return Bytes.toString(bytes);
 	}
 }
