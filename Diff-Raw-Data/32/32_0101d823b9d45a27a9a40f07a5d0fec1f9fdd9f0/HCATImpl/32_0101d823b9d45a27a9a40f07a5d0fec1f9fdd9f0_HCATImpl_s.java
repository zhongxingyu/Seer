 package savi.hcat.analytics.coprocessor;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.coprocessor.BaseEndpointCoprocessor;
 import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
 import org.apache.hadoop.hbase.regionserver.InternalScanner;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import savi.hcat.common.util.XConstants;
 
 
 public class HCATImpl extends BaseEndpointCoprocessor implements HCATProtocol {
 
 	private static Log LOG = LogFactory.getLog(HCATImpl.class);
 	
 	
 	@Override
 	public synchronized RStatResult getSummary(Scan scan,String region, String condition,String unit,String starttime,String endtime) throws IOException {
 		
 		long sTime = System.currentTimeMillis();
 		LOG.info(sTime+": in the getSummary: "+scan.toJSON()+";"+condition+";"+unit+";"+starttime+";"+endtime);
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);		
 		
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();
 		RStatResult results = new RStatResult().initHashUnit();
 		results = results.setHashUnit(unit, starttime, endtime); // by month/week	
 		LOG.info("the scan start row "+Bytes.toString(scan.getStartRow()) + "; end-row: "+Bytes.toString(scan.getStopRow()));	
 		results.setRegion(region); // set city in order to aggregate in the client 
 		LOG.info("get the region "+region);
 		
 		boolean hasMoreResult = false;				
 		/**Step2: iterate the result from the scanner**/		
 		int cell_num = 0;
 		int row_num = 0;		
 		int kvLength = 0;		
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row_num++;					
 					for(KeyValue kv:keyvalues){
 						LOG.info(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						cell_num++;
 						String rowKey = Bytes.toString(kv.getRow());						
 						String unitKey = results.parseUnitKey(rowKey);
 						if(condition.equals("unfinished")){
 							if(Bytes.toString(kv.getValue()).equals("null"))
 								results.addOnHashUnit(unitKey, 1);	
 						}else if(condition.equals("finished")){
 							if(!Bytes.toString(kv.getValue()).equals("null"))
 								results.addOnHashUnit(unitKey, 1);
 						}else{
 							results.addOnHashUnit(unitKey, 1); // there is no condition, 
 						}											
 					}
 				}				
 				keyvalues.clear();
 
 			} while (hasMoreResult);
 
 			long eTime = System.currentTimeMillis();
 
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row_num);
 			results.setCells(cell_num);
 			results.setKvLength(kvLength);
 			LOG.info("Hash Unit: "+results.getHashUnit().toString());	
 			LOG.info("exe_time=>"+(eTime-sTime)+";result=>"+results.getHashUnit().size()+";cell_num=>"+cell_num+";row_num=>"+row_num);	
 
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 
 		return results;	
 
 	}
 
 	
 	
 	@Override
 	public RStatResult getPercentage(Scan scan, String condition, String unit,
 			String starttime, String endtime, String numerator)
 			throws IOException {
 		long sTime = System.currentTimeMillis();
 		LOG.info(sTime+": in getPercentage: "+scan.toJSON()+";"+condition+";"+unit+";"+starttime+";"+endtime+";numberator=>"+numerator);
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);		
 		
 		
 		RStatResult unitHashArrayResult = new RStatResult().initHashUnitArray();
 		unitHashArrayResult = unitHashArrayResult.setHashUnitArray(unit, starttime, endtime); // by month/week	
 		LOG.info("the scan start row "+Bytes.toString(scan.getStartRow()) + "; end-row: "+Bytes.toString(scan.getStopRow()));		
 		String r = Bytes.toString(scan.getStartRow()).split(XConstants.ROW_KEY_DELIMETER)[0];		
 		unitHashArrayResult.setRegion(r); // set region in order to aggregate in the client 
 		LOG.info("get the region "+r);
 		
 					
 		/**Step2: iterate the result from the scanner**/		
 		int cell_num = 0;
 		int row_num = 0;		
 		int kvLength = 0;
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();
 		boolean hasMoreResult = false;	
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row_num++;					
 					for(KeyValue kv:keyvalues){
 						LOG.info(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						cell_num++;
 						String rowKey = Bytes.toString(kv.getRow());						
 						String unitKey = unitHashArrayResult.parseUnitKey(rowKey);
 						
 						String qualifer = Bytes.toString(kv.getQualifier());						
 						String cellValue = Bytes.toString(kv.getValue());
 						LOG.info("qualifer=>"+qualifer+";cellValue=>"+cellValue);
 						JSONTokener tokener = new JSONTokener(cellValue);
 						JSONObject desc = new JSONObject(tokener);						
 						if(numerator.equals(XConstants.POST_VALUE_SERVICE)){
 							int status = desc.getInt("status");	
 							LOG.info("unitKey=>"+unitKey+";status=>"+status);							
 							unitHashArrayResult.addOnUnitHashArray(unitKey,qualifer,status,4);	
 						}else if(numerator.equals(XConstants.POST_VALUE_MEDIA)){
 							unitHashArrayResult.addOnUnitHashArray(unitKey,qualifer,0,1);
 						}										
 					}
 				}				
 				keyvalues.clear();
 
 			} while (hasMoreResult);
 
 			long eTime = System.currentTimeMillis();
 
 			unitHashArrayResult.setStart(sTime);
 			unitHashArrayResult.setEnd(eTime);
 			unitHashArrayResult.setRows(row_num);
 			unitHashArrayResult.setCells(cell_num);
 			unitHashArrayResult.setKvLength(kvLength);
 			LOG.info("Hash Unit: "+unitHashArrayResult.getHashUnitArray().toString());	
 			LOG.info("exe_time=>"+(eTime-sTime)+";result=>"+unitHashArrayResult.getHashUnitArray().size()+
 					";cell_num=>"+cell_num+";row_num=>"+row_num);	
 
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 
 		return unitHashArrayResult;	
 	}
 
 
 	public RStatResult doWindowQuery(Scan scan,String region, HashMap<String,Rectangle2D.Double> quads)throws IOException{
 		long sTime = System.currentTimeMillis();
 		LOG.info(sTime+": in the getSummary: "+scan.toJSON()+";"+quads.toString());
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);		
 		
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();
		RStatResult results = new RStatResult().initHashUnit();		
 		LOG.info("the scan start row "+Bytes.toString(scan.getStartRow()) + "; end-row: "+Bytes.toString(scan.getStopRow()));	
 		results.setRegion(region); // set city in order to aggregate in the client 
 		LOG.info("get the region "+region);
 		
 		boolean hasMoreResult = false;				
 		/**Step2: iterate the result from the scanner**/		
 		int cell_num = 0;
 		int row_num = 0;		
 		int kvLength = 0;		
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row_num++;					
 					for(KeyValue kv:keyvalues){
 						LOG.info(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						cell_num++;											
 						String value = Bytes.toString(kv.getValue());						
 						JSONTokener tokener = new JSONTokener(value);
 						JSONObject desc = new JSONObject(tokener);
 						
 						Point2D.Double resPoint = new Point2D.Double(Double.valueOf(desc.getDouble(XConstants.FIELD_NAME_LATTITUDE)),
 								desc.getDouble(XConstants.FIELD_NAME_LONGITUDE));
 						LOG.info("the patient location: "+ resPoint.toString());
 						for(String key: quads.keySet()){
 							Rectangle2D.Double rec = quads.get(key);
 							if(rec.contains(resPoint)){
 								results.addOnHashUnit(key, 1);
 								LOG.info("the patient belongs to "+ key);
 								break;
 							}
 						}								
 					}
 				}				
 				keyvalues.clear();
 
 			} while (hasMoreResult);
 
 			long eTime = System.currentTimeMillis();
 
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row_num);
 			results.setCells(cell_num);
 			results.setKvLength(kvLength);
 			LOG.info("Hash Unit: "+results.getHashUnit().toString());	
 			LOG.info("exe_time=>"+(eTime-sTime)+";result=>"+results.getHashUnit().size()+";cell_num=>"+cell_num+";row_num=>"+row_num);	
 
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 
 		return results;	
 	}
 
 	@Override
 	public RStatResult doRangeQuery(Scan scan, double latitude, double longitude, double radius) throws IOException {
 		long sTime = System.currentTimeMillis();
 		LOG.info(sTime+": in the getSummary: "+scan.toJSON()+"; latitude=>"+latitude+";longitude=>"+longitude+";distance=>"+radius);
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);		
 		
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();
 		RStatResult results = new RStatResult();		
 		LOG.info("the scan start row "+Bytes.toString(scan.getStartRow()) + "; end-row: "+Bytes.toString(scan.getStopRow()));
 		LOG.info("parts: XXX: "+Bytes.toString(scan.getStartRow()).split(XConstants.ROW_KEY_DELIMETER).length);
 		String r = Bytes.toString(scan.getStartRow()).split(XConstants.ROW_KEY_DELIMETER)[0];		
 		results.setRegion(r); // set city in order to aggregate in the client 
 		LOG.info("get the region "+r);
 		
 		boolean hasMoreResult = false;				
 		/**Step2: iterate the result from the scanner**/		
 		int cell_num = 0;
 		int row_num = 0;		
 		int kvLength = 0;		
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row_num++;					
 					for(KeyValue kv:keyvalues){
 						LOG.info(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						cell_num++;											
 						String value = Bytes.toString(kv.getValue());						
 						JSONTokener tokener = new JSONTokener(value);
 						JSONObject desc = new JSONObject(tokener);
 						
 						Point2D.Double resPoint = new Point2D.Double(Double.valueOf(desc.getDouble(XConstants.FIELD_NAME_LATTITUDE)),
 								desc.getDouble(XConstants.FIELD_NAME_LONGITUDE));
 																		
 						double distance = resPoint.distance(new Point2D.Double(latitude,longitude));
 						LOG.info("the patient location: "+ resPoint.toString()+";distance=>"+distance);
 						if(distance <= radius){
 							results.addDistance(distance, Bytes.toString(kv.getQualifier()));
 						}						
 						LOG.info("the patient location: "+ resPoint.toString());
 						
 					}
 				}				
 				keyvalues.clear();
 
 			} while (hasMoreResult);
 
 			long eTime = System.currentTimeMillis();
 
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row_num);
 			results.setCells(cell_num);
 			results.setKvLength(kvLength);
 			LOG.info("points in the radius: "+results.getRes().toString());	
 			LOG.info("exe_time=>"+(eTime-sTime)+";result=>"+results.getRes().size()+";cell_num=>"+cell_num+";row_num=>"+row_num);	
 
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 
 		return results;	
 	}
 	
 	
 	
 
 	@Override
 	public RStatResult getAverage(Scan scan,String condition,String unit,String starttime,String endtime) throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}	
 
 }
