 package org.apache.hadoop.hbase.coprocessor;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.regionserver.InternalScanner;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import bixi.hbase.query.BixiConstant;
 
 /**
  * @author hv
  */
 public class BixiImplementation extends BaseEndpointCoprocessor implements
 BixiProtocol {
 
 	static final Log log = LogFactory.getLog(BixiImplementation.class);
 
 	private static byte[] colFamily = BixiConstant.SCHEMA1_FAMILY_NAME.getBytes();
 	private final static String BIXI_DELIMITER = "#";
 	private final static int BIXI_DATA_LENGTH = 11;
 
 	@Override
 	public Map<String, Integer> giveAvailableBikes(long milliseconds,
 			List<String> stationIds, Scan scan) throws IOException {
 		// scan has set the time stamp accordingly, i.e., the start and end row of
 		// the scan.
 
 		for (String qualifier : stationIds) {
 			log.debug("adding qualifier: " + qualifier);
 			scan.addColumn(colFamily, qualifier.getBytes());
 		}
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment())
 				.getRegion().getScanner(scan);
 		List<KeyValue> res = new ArrayList<KeyValue>();
 		Map<String, Integer> result = new HashMap<String, Integer>();
 		boolean hasMoreResult = false;
 		try {
 			do {
 				hasMoreResult = scanner.next(res);
 				for (KeyValue kv : res) {
 					//log.debug("got a kv: " + kv);
 					int availBikes = getFreeBikes(kv);
 					String id = Bytes.toString(kv.getQualifier());
 					//log.debug("result to be added is: " + availBikes + " id: " + id);
 					result.put(id, availBikes);
 				}
 				res.clear();
 			} while (hasMoreResult);
 		} finally {
 			scanner.close();
 		}
 		return result;
 	}
 
 	private int getFreeBikes(KeyValue kv) {
 		String availBikes = processKV(kv, 9);
 		//log.debug("availbikes::" + availBikes);
 		try {
 			return Integer
 					.parseInt(availBikes.substring(availBikes.indexOf("=") + 1));
 		} catch (Exception e) {
 			System.err.println("Non numeric value as avail bikes!");
 		}
 		return 0;
 	}
 
 	private String processKV(KeyValue kv, int index) {
 		if (kv == null || index > 10 || index < 0)
 			return null;
 		//log.debug("kv.getValue()" + Bytes.toString(kv.getValue()));
 		String[] str = Bytes.toString(kv.getValue()).split(
 				BixiImplementation.BIXI_DELIMITER);
 		// malformed value (shouldn't had been here.
 		if (str.length != BixiImplementation.BIXI_DATA_LENGTH)
 			return null;
 		return str[index];
 	}
 
 	@Override
 	public Map<String, TotalNum> giveTotalUsage(List<String> stationIds,
 			Scan scan) throws IOException {
 		for (String qualifier : stationIds) {
 			log.debug("adding qualifier: " + qualifier);
			String colName = Integer.toString(Integer.parseInt(qualifier));
			scan.addColumn(colFamily, colName.getBytes());
 		}
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment())
 				.getRegion().getScanner(scan);
 		List<KeyValue> res = new ArrayList<KeyValue>();
 		Map<String, TotalNum> result = new HashMap<String, TotalNum>();
 		boolean hasMoreResult = false;
 		try {
 			do {
 				hasMoreResult = scanner.next(res);
 				for (KeyValue kv : res) {
 					//log.debug("got a kv: " + kv);
 					long emptyDocks = getEmptyDocks(kv);
 					String id = Bytes.toString(kv.getQualifier());
 					TotalNum tn;
 					if(result.containsKey(id)){
 						tn = result.get(id);
 					}else{
 						tn = new TotalNum();
 					}
 					tn.add(emptyDocks);
 					//emptyDocks = emptyDocks + (prevVal != null ? prevVal.intValue() : 0);
 					//log.debug("result to be added is: " + emptyDocks + " id: " + id);
 					result.put(id, tn);
 				}
 				res.clear();
 			} while (hasMoreResult);
 		} finally {
 			scanner.close();
 		}
 		return result;
 	}
 
 	private int getEmptyDocks(KeyValue kv) {
 		String availBikes = processKV(kv, 10);
 		log.debug("emptyDocks::" + availBikes);
 		try {
 			return Integer
 					.parseInt(availBikes.substring(availBikes.indexOf("=") + 1));
 		} catch (Exception e) {
 			System.err.println("Non numeric value as avail bikes!");
 		}
 		return 0;
 	}
 
 	/**
 	 * make a general method that takes a pair of lat/lon and a radius and give a
 	 * boolean whether it was in or out.
 	 * @throws IOException
 	 */
 	@Override
 	public Map<String, Integer> getAvailableBikesFromAPoint(double lat,
 			double lon, double radius, Get get) throws IOException {
 		Result r = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion()
 				.get(get, null);
 		log.debug("r is "+r);
 		log.debug(r.getMap().toString());
 		Map<String, Integer> result = new HashMap<String, Integer>();
 		try {
 			String s = null, latStr = null, lonStr = null;
 			for (KeyValue kv : r.raw()) {
 				s = Bytes.toString(kv.getValue());
 				log.debug("cell value is: "+s);
 				String[] sArr = s.split(BIXI_DELIMITER); // array of key=value pairs
 				latStr = sArr[3];
 				lonStr = sArr[4];
 				latStr = latStr.substring(latStr.indexOf("=")+1);
 				lonStr = lonStr.substring(lonStr.indexOf("=")+1);
 				log.debug("lon/lat values are: "+lonStr +"; "+latStr);
 				double distance =giveDistance(Double.parseDouble(latStr), Double.parseDouble(lonStr),
 						lat, lon)- radius;
 				log.debug("distance is : "+ distance);
 				if ( distance < 0) {// add it
 					result.put(sArr[0], getFreeBikes(kv));
 				}
 			}
 		} finally {
 		}
 		return result;
 	}
 
 	final static double RADIUS = 6371;
 
 	private double giveDistance(double lat1, double lon1, double lat2, double lon2) {
 		double dLon = Math.toRadians(lon1 - lon2);
 		double dLat = Math.toRadians(lat1 - lat2);
 		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(lat1))
 				* Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
 		double res = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double distance = RADIUS * res;
 		return distance;
 	}
 
 	/* Schema 2 implementation */
 	private static byte[] colFamilyStat = BixiConstant.SCHEMA2_BIKE_FAMILY_NAME.getBytes();
 
 	@Override
 	public Map<String, TotalNum> getTotalUsage_Schema2(Scan scan) throws IOException {
 
 		//System.err.println("scanning");
 		scan.addFamily(colFamilyStat);
 
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment())
 				.getRegion().getScanner(scan);
 		List<KeyValue> res = new ArrayList<KeyValue>();
 		Map<String, TotalNum> result = new HashMap<String, TotalNum>();
 		boolean hasMoreResult = false;
 		try {
 			do {
 				hasMoreResult = scanner.next(res);
 				for (KeyValue kv : res) {
 					String stationId = Bytes.toString(kv.getRow()).split("-")[1];
 					String value = new String(kv.getValue());
 					Long usage = Long.parseLong(value.split(";")[1]);
 					if(result.containsKey(stationId)){
 						TotalNum tn = result.get(stationId);
 						tn.add(usage);
 						result.put(stationId, tn);
 					}else{
 						TotalNum tn = new TotalNum();
 						tn.add(usage);
 						result.put(stationId, tn);
 					}
 				}
 				res.clear();
 			} while (hasMoreResult);
 		} finally {
 			scanner.close();
 		}
 		return result;
 	}
 
 	@Override
 	public Map<String, Integer> getAvailableBikesFromAPoint_Schema2(Scan scan) throws IOException {
 		scan.addFamily(colFamilyStat);
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment())
 				.getRegion().getScanner(scan);
 		Map<String, Integer> result = new HashMap<String, Integer>();
 		boolean hasMoreResult = false;
 		List<KeyValue> res = new ArrayList<KeyValue>();
 		try {
 			do {
 				hasMoreResult = scanner.next(res);
 				for (KeyValue kv : res) {
 					String stationId = Bytes.toString(kv.getRow()).split("-")[1];
 					String value = new String(kv.getValue());
 					Integer free = Integer.parseInt(value.split(";")[0]);
 
 					if(result.containsKey(stationId)){
 						result.put(stationId, free + result.get(stationId));
 					}else{
 						result.put(stationId, free);
 					}
 				}
 				res.clear();
 			} while (hasMoreResult);
 		} finally {
 			scanner.close();
 		}
 		return result;
 	}
 
 	public List<String> getStationsNearPoint_Schema2(double lat, double lon) throws IOException {
 		Scan scan = new Scan();
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment())
 				.getRegion().getScanner(scan);
 		boolean hasMoreResult = false;
 		List<KeyValue> res = new ArrayList<KeyValue>();
 		try {
 			do {
 				hasMoreResult = scanner.next(res);
 				for (KeyValue kv : res) {
 					if(!Bytes.toString(kv.getQualifier()).equalsIgnoreCase("ids")){
 						//only look at stationid column
 						continue;
 					}
 					String clusterId = Bytes.toString(kv.getRow());
 					String[] parts = clusterId.split(":");
 					double cLat = Double.parseDouble(parts[0]);
 					double cLon = Double.parseDouble(parts[1]);
 					double dx = Double.parseDouble(parts[2]);
 					double dy = Double.parseDouble(parts[3]);
 					double distx = lat-cLat;
 					double disty = lon-cLon;
 					if(distx >= 0 && distx <= dx && disty >= 0 && disty <= dy){
 						//get stations in cluster
 						return Arrays.asList(Bytes.toString(kv.getValue()).split(","));
 					}
 				}
 				res.clear();
 			} while (hasMoreResult);
 		} finally {
 			scanner.close();
 		}
 		return new ArrayList<String>();
 	}
 
 }
