 package com.query.bixi.coprocessor;
 
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.coprocessor.BaseEndpointCoprocessor;
 import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
 import org.apache.hadoop.hbase.regionserver.InternalScanner;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import com.util.XCSVFormat;
 
 /**
  * @author 
  */
 public class BixiImplementation extends BaseEndpointCoprocessor implements BixiProtocol {
 
 	static final Log log = LogFactory.getLog(BixiImplementation.class);
 
 	/******************For Location Schema1****QuadTree***************************/
 	public RCopResult copQueryNeighbor4QT(Scan scan,double latitude,double longitude,double radius,XCSVFormat csv)throws IOException{
 		
 		long sTime = System.currentTimeMillis();
 		System.out.println(sTime+": in the copQueryNeighbor4LS1....");
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();
 		//List<String> results = new ArrayList<String>();
 		RCopResult results = new RCopResult();
 		boolean hasMoreResult = false;		
 		Point2D.Double point = new Point2D.Double(latitude,longitude);
 		/**Step2: iterate the result from the scanner**/
 		int cell = 0;
 		int row = 0;
 		int accepted = 0;
 		int kvLength = 0;
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row++;
 					for(KeyValue kv:keyvalues){
 						//System.out.println(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						cell++;
 						// get the distance between this point and the given point	
 						
 						Hashtable<String,String>  pairs = csv.fromPairString(Bytes.toString(kv.getValue()));						
 						
 						Point2D.Double resPoint = new Point2D.Double(Double.valueOf(pairs.get("lat")),Double.valueOf(pairs.get("long")));
 						double distance = resPoint.distance(point);
 						/**Step3: filter the false-positive points**/
 						if(distance <= radius){						
 							//System.out.println("row=>"+Bytes.toString(r.getRow()) + ";colum=>"+Bytes.toString(kv.getQualifier())+ ";station=>"+station.getId());
 							results.getRes().add(Bytes.toString(kv.getQualifier()));
 							accepted++;
 						}
 
 					}
 				}								
 				keyvalues.clear();
 				
 			} while (hasMoreResult);
 			
 			long eTime = System.currentTimeMillis();
 			
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row);
 			results.setCells(cell);
 			results.setKvLength(kvLength);
 			results.setParameter(String.valueOf(radius));
 			//System.out.println("exe_time=>"+(eTime-sTime)+";result=>"+results.getRes().size()+";count=>"+count+";accepted=>"+accepted);	
 						
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 						
 		return results;	
 	}
 	
 	public RCopResult copQueryPoint4QT(Scan scan,double latitude,double longitude,XCSVFormat csv)throws IOException{
 		
 		long sTime = System.currentTimeMillis();
 		System.out.println(sTime+": in the copQueryPoint4QT....");
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();		
 		boolean hasMoreResult = false;				
 		/**Step2: iterate the result from the scanner**/
 		int count = 0;				
 		RCopResult results = new RCopResult();
 		int cell = 0;
 		int row = 0;		
 		int kvLength = 0;
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row++;
 					for(KeyValue kv:keyvalues){
 						
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						cell++;
 						//System.out.println(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						count++;
 						// get the distance between this point and the given point
 						Hashtable<String,String>  pairs = csv.fromPairString(Bytes.toString(kv.getValue()));
 						if((Double.valueOf(pairs.get("lat")).doubleValue() == latitude) && 
 								(Double.valueOf(pairs.get("long")).doubleValue() == longitude)){
 							results.getRes().add(Bytes.toString(kv.getQualifier()));							
 							break;
 						}									
 					}
 				}								
 				keyvalues.clear();
 				
 				
 			} while (hasMoreResult);					
 			
 			long eTime = System.currentTimeMillis();
 			
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row);
 			results.setCells(cell);
 			results.setKvLength(kvLength);
 			results.setParameter("("+String.valueOf(latitude)+":"+String.valueOf(longitude)+")");			
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 						
 		return results;			
 	}
 	
 	public RCopResult copQueryKNN4QT(Scan scan,double latitude,double longitude,XCSVFormat csv)throws IOException{
 		return null;
 	}
 	
 	
 	/******************For Location Schema2* Raster******************************/
 	
 	public RCopResult copQueryNeighbor4Raster(Scan scan,double latitude,double longitude,double radius,XCSVFormat csv)throws IOException{
 		
 		long sTime = System.currentTimeMillis();
 		System.out.println(sTime+": in the copQueryNeighbor4Raster....");
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();
 		RCopResult results = new RCopResult();
 		boolean hasMoreResult = false;		
 		Point2D.Double point = new Point2D.Double(latitude,longitude);
 		
 		/**Step2: iterate the scan result ***/
 		int row = 0;
 		int cell = 0;		
 		int kvLength = 0;
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row++;	
 					
 					for(KeyValue kv:keyvalues){
 						//System.out.println(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						cell++;	
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						// get the distance between this point and the given point
 						
 						Hashtable<String,String>  pairs = csv.fromPairString(Bytes.toString(kv.getValue()));						
 						
 						Point2D.Double resPoint = new Point2D.Double(Double.valueOf(pairs.get("lat")),Double.valueOf(pairs.get("long")));
 						double distance = resPoint.distance(point);
 						/**Step3: filter the false-positive points**/
 						if(distance <= radius){						
 							//System.out.println("row=>"+Bytes.toString(kv.getRow()) + ";colum=>"+Bytes.toString(kv.getQualifier())+ ";station=>"+station.getId());
 							results.getRes().add(pairs.get("id"));
 						}
 							
 					}
 				}								
 				keyvalues.clear();				
 				
 			} while (hasMoreResult);
 			
 			long eTime = System.currentTimeMillis();
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row);
 			results.setCells(cell);
 			results.setKvLength(kvLength);
 			results.setParameter(String.valueOf(radius));
 						
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 						
 		return results;	
 				
 	}	
 	
 	public RCopResult copQueryPoint4Raster(Scan scan,double latitude,double longitude,XCSVFormat csv)throws IOException{
 		
 		long sTime = System.currentTimeMillis();
 		System.out.println(sTime+": in the copQueryPoint4LS2....");
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();		
 		boolean hasMoreResult = false;				
 		
 		/**Step2: iterate the scan result ***/
 		RCopResult results = new RCopResult();
 		int count = 0;
 		int accepted = 0;		
 		int cell = 0;
 		int row = 0;		
 		int kvLength = 0;
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row++;
 					for(KeyValue kv:keyvalues){
 						//System.out.println(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						kvLength = (kvLength < kv.getLength())? kv.getLength():kvLength;
 						cell++;
 						count++;						
 						// get the distance between this point and the given point						
 												
 						Hashtable<String,String>  pairs = csv.fromPairString(Bytes.toString(kv.getValue()));						
 						
 						if((Double.valueOf(pairs.get("lat")).doubleValue() == latitude && Double.valueOf(pairs.get("long")).doubleValue()== longitude)){
 							results.getRes().add(pairs.get("name"));								
 							break;
 						}							
 
 					}
 				}								
 				keyvalues.clear();				
 				
 			} while (hasMoreResult);
 			
 			long eTime = System.currentTimeMillis();
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row);
 			results.setCells(cell);
 			results.setKvLength(kvLength);
 			results.setParameter("("+String.valueOf(latitude)+":"+String.valueOf(longitude)+")");
 			
 			
 		} catch(Exception e){
 			e.printStackTrace();
 		}finally {
 			scanner.close();
 		}
 						
 		return results;		
 	}
 	
 	public RCopResult copQueryKNN4Raster(Scan scan,double latitude,double longitude,XCSVFormat csv)throws IOException{
 		return null;
 	}
 	
 	
 	
 	/******************For Location Schema2* Raster******************************/	
 	public RCopResult copQueryNeighbor4Hybrid(Scan scan,double latitude,double longitude,double radius,XCSVFormat csv)throws IOException{
 		
 		long sTime = System.currentTimeMillis();
 		System.out.println(sTime+": in the copQueryNeighbor4Hybrid....");
 		/**Step1: get internalScanner***/
 		InternalScanner scanner = ((RegionCoprocessorEnvironment) getEnvironment()).getRegion().getScanner(scan);
 		List<KeyValue> keyvalues = new ArrayList<KeyValue>();
 		RCopResult results = new RCopResult();
 		boolean hasMoreResult = false;		
 		Point2D.Double point = new Point2D.Double(latitude,longitude);
 		
 		/**Step2: iterate the scan result ***/
 		int row = 0;
 		int cell = 0;		
 		int kvLength = 0;
 		try {
 			do {
 				hasMoreResult = scanner.next(keyvalues);
 				if(keyvalues != null && keyvalues.size() > 0){	
 					row++;	
 					
 					String id = null;
 					double lat = -1;
 					double lon = -1;
 															
 					for(KeyValue kv:keyvalues){
 						//System.out.println(Bytes.toString(kv.getRow())+"=>"+Bytes.toString(kv.getValue()));
 						cell++;	
 						
						kvLength = (kvLength*3 < kv.getLength()*3)? kv.getLength()*3:kvLength*3;
 						
 						// get the distance between this point and the given point	
 						if(kv.getTimestamp() == 1){
 							id = Bytes.toString(kv.getValue());
 						}else if(kv.getTimestamp() == 2){
 							lat = java.lang.Double.valueOf(Bytes.toString(kv.getValue()));
 						}else if(kv.getTimestamp() == 3){
 							lon = java.lang.Double.valueOf(Bytes.toString(kv.getValue()));
 						}
 						
						// because of the timestamp filter, this is fixed
						if(cell % 3 == 0){
 							Point2D.Double resPoint = new Point2D.Double(lat,lon);									
 							double distance = resPoint.distance(point);
 
 							if (distance <= radius) {										
 								results.getRes().add(id);
							}	
 							id = null;
 							lat = -1;
							lon = -1;							
						}							
											
 					}
 				}								
 				keyvalues.clear();				
 				
 			} while (hasMoreResult);
 			
 			long eTime = System.currentTimeMillis();
 			results.setStart(sTime);
 			results.setEnd(eTime);
 			results.setRows(row);
 			results.setCells(cell);
 			results.setKvLength(kvLength);
 			results.setParameter(String.valueOf(radius));
 						
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			scanner.close();
 		}
 						
 		return results;	
 	}
 	
 	public RCopResult copQueryPoint4Hybrid(Scan scan,double latitude,double longitude,XCSVFormat csv)throws IOException{
 		return null;
 	}
 	
 	public RCopResult copQueryKNN4Hybrid(Scan scan,double latitude,double longitude,XCSVFormat csv)throws IOException{
 		return null;
 	}
 
 }
