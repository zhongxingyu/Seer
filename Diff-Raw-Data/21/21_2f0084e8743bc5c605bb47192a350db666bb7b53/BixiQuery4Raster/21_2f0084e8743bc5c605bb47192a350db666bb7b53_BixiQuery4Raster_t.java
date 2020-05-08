 package com.query.bixi;
 
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.client.coprocessor.Batch;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import com.query.QueryAbstraction;
 import com.query.bixi.coprocessor.BixiProtocol;
 import com.query.bixi.coprocessor.RCopResult;
 import com.util.XCSVFormat;
 import com.util.XTableSchema;
 import com.util.raster.XBox;
 
 public class BixiQuery4Raster extends QueryAbstraction{
 
 	public BixiQuery4Raster(String schema_desc_file,String csv_desc_file,String hbase_conf){
 		super(schema_desc_file,csv_desc_file,hbase_conf);
 	}
 	
 	public BixiQuery4Raster(XTableSchema schema,String csv_desc_file,String hbase_conf){	
 		super(schema,csv_desc_file,hbase_conf);
 	}
 
 	@Override
 	public List<String> copQueryAvailableNear(String timestamp,
 			final double latitude, final double longitude, final double radius) {	
 		try {					
 			this.getCSVLog(0);
			this.getCSVLog(1);			
 			this.timePhase.clear();			
 			/** Step1** Call back class definition **/
 			class BixiCallBack implements Batch.Callback<RCopResult> {
 				RCopResult res = new RCopResult();
 				int count = 0;
 				QueryAbstraction query = null;
 				
 			    public BixiCallBack(QueryAbstraction query){
 				    	this.query = query; 
 				}
 				@Override
 				public void update(byte[] region, byte[] row,RCopResult result) {
 					long current = System.currentTimeMillis();
 					count++;
 					res.getRes().addAll(result.getRes()); // to verify the error when large data
 					res.setStart(result.getStart());
 					res.setEnd(result.getEnd());
 					res.setRows((res.getRows()+result.getRows()));
 					res.setCells(res.getCells()+result.getCells());								
 			    	  // write them into csv file
 			    	 String outStr = "";
 			    	  outStr += "within,"+"cop,"+result.getParameter()+","+result.getStart()+","+result.getEnd()+","+current+","+
 			    			  	result.getRows()+","+result.getCells()+","+result.getKvLength()+","+result.getRes().size()+","+
 			    			  	this.query.regionAndRS.get(Bytes.toString(region))+","+Bytes.toString(region);
 			    	  this.query.writeCSVLog(outStr,1);								    	
 				}
 			}
 			BixiCallBack callBack = new BixiCallBack(this);
 
 			/** Step2** generate the scan ***********/
 			long s_time = System.currentTimeMillis();
 			this.timePhase.add(s_time);
 
 			// match the query area in Raster to get the row range and column
 			// range
 			long match_s = System.currentTimeMillis();
 			XBox[] match_boxes = raster.match(latitude, longitude, radius);
 			long match_time = System.currentTimeMillis()-match_s;
 			String[] rowRange = new String[2];
 			rowRange[0] = match_boxes[0].getRow();
 			rowRange[1] = match_boxes[1].getRow()+"-*";
 
 			String[] c = raster.getColumns(match_boxes[0], match_boxes[1]);
 			// generate the scan
 			final Scan scan = hbase.generateScan(rowRange, null, new String[]{this.tableSchema.getFamilyName()},
 					c, this.tableSchema.getMaxVersions());
 			
 			/** Step3** send out the query to trigger the corresponding function in Coprocessor****/
 			this.timePhase.add(System.currentTimeMillis());
 			final XCSVFormat csv = this.csvFormat;
 			hbase.getHTable().coprocessorExec(BixiProtocol.class,
 					scan.getStartRow(), scan.getStopRow(),
 					new Batch.Call<BixiProtocol, RCopResult>() {
 
 						public RCopResult call(BixiProtocol instance)
 								throws IOException {							
 							return instance.copQueryNeighbor4Raster(scan,
 									latitude, longitude, radius,csv);
 
 						};
 					}, callBack);
 			
 		    long e_time = System.currentTimeMillis();
 		    this.timePhase.add(e_time);
 
 		    
 			long exe_time = e_time - s_time;
 			// write to csv file
 			String outStr = "";
 			outStr += "within,"+"cop,"+callBack.res.getRes().size()+","+callBack.res.getCells()+","+callBack.res.getRows()+","+
 						exe_time+","+match_time+","+this.tableSchema.getSubSpace()+","+radius;	
 					
 						
 			
 			for(int i=0;i<this.timePhase.size();i++){
 				outStr += ",";
 				outStr +=this.timePhase.get(i);
 			}					
 			this.writeCSVLog(outStr, 0);			
 			
 			return callBack.res.getRes();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}catch(Throwable ee){
 			ee.printStackTrace();
 		}finally{
 			hbase.closeTableHandler();					
 			this.closeCSVLog();			
 		}
 		return null;
 
 	}
 
 	@Override
 	public HashMap<String, String> scanQueryAvailableNear(String timestamp,
 			double latitude, double longitude, double radius) {
 		// return result
 		HashMap<String, String> results = new HashMap<String, String>();
 		try {
 			
 			this.getCSVLog(0);					
 			this.timePhase.clear();
 			long sTime = System.currentTimeMillis();
 			this.timePhase.add(sTime);
 
 			Point2D.Double point = new Point2D.Double(latitude, longitude);
 			ResultScanner rScanner = null;
 			
 			
 			// match rect to find the subspace it belongs to
 			long match_s = System.currentTimeMillis();
 			XBox[] match_boxes = raster.match(latitude, longitude, radius);
 			long match_time = System.currentTimeMillis() - match_s;
 			String[] rowRange = new String[2];
 			rowRange[0] = match_boxes[0].getRow();
 			rowRange[1] = match_boxes[1].getRow()+"-*";
 
 			String[] c = raster.getColumns(match_boxes[0], match_boxes[1]);
 
 			// the version here is harded coded, because i cannot get how many
 			// objects in one cell now
 			this.timePhase.add(System.currentTimeMillis());
 			rScanner = this.hbase.getResultSet(rowRange, null,
 					new String[]{this.tableSchema.getFamilyName()}, c, this.tableSchema.getMaxVersions());
 			
 			int count = 0;
 			int row = 0;
 			int accepted = 0;
 
 			for (Result r : rScanner) {
 				
 				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> resultMap = r
 						.getMap();
 
 				for (byte[] family : resultMap.keySet()) {
 					NavigableMap<byte[], NavigableMap<Long, byte[]>> columns = resultMap
 							.get(family);
 					for (byte[] col : columns.keySet()) {
 						NavigableMap<Long, byte[]> values = columns.get(col);
 						for (Long version : values.keySet()) {
 							count++;
 							// get the distance between this point and the given
 							// point
 							
 							Hashtable<String,String> key_values = this.csvFormat.fromPairString(Bytes.toString(values.get(version)));
 
 							Point2D.Double resPoint = new Point2D.Double(Double.valueOf(key_values.get("lat")).doubleValue(),
 											Double.valueOf(key_values.get("long")).doubleValue());									
 							double distance = resPoint.distance(point);
 
 							if (distance <= radius) {
 								accepted++;
 								results.put(key_values.get("id"),String.valueOf(distance));
 							}
 						}
 					}
 				}
 			}
 			long eTime = System.currentTimeMillis();
 			this.timePhase.add(eTime);
 			
 			
 			// write to csv file
 			String outStr = "";
 			outStr += "within,"+"scan,"+accepted+","+count+","+row+","+
 						(eTime-sTime)+","+match_time+","+this.tableSchema.getSubSpace()+","+radius;				
 			
 						
 			for(int i=0;i<this.timePhase.size();i++){
 				outStr += ",";
 				outStr += this.timePhase.get(i);
 			}
 					
 			this.writeCSVLog(outStr, 0);
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}finally{
 			this.hbase.closeTableHandler();			
 			this.closeCSVLog();
 		}
 		return results;
 	}
 
 	@Override
 	public String copQueryPoint(double latitude, double longitude) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String scanQueryPoint(double latitude, double longitude) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void copQueryArea(double latitude, double longitude, int area) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void scanQueryArea(double latitude, double longitude, int area) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void copQueryAvailableKNN(String timestamp, double latitude,
 			double longitude, int n) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public TreeMap<Double, String> scanQueryAvailableKNN(String timestamp,
 			double latitude, double longitude, int n) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 
 }
