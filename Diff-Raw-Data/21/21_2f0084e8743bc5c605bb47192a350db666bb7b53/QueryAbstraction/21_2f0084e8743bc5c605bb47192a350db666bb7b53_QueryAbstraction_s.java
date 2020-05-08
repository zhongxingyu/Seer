 package com.query;
 
 import java.awt.geom.Rectangle2D;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 
 import org.apache.hadoop.hbase.HRegionInfo;
 import org.apache.hadoop.hbase.client.HTable;
 
 import com.hbase.service.HBaseUtil;
 import com.hbase.service.StatUtil;
 import com.util.XCSVFormat;
 import com.util.XConstants;
 import com.util.XTableSchema;
 import com.util.log.XLogCSV;
 import com.util.log.XLogConst;
 import com.util.quadtree.trie.XQuadTree;
 import com.util.raster.XRaster;
 
 public abstract class QueryAbstraction {
 	
 
 	protected HBaseUtil hbase = null;	
 	protected XTableSchema tableSchema = null;	
 	protected XQuadTree quadTree = null;
 	protected XRaster raster = null;
 	protected XCSVFormat csvFormat = null;
 	
 	// For log 
 	private XLogCSV mainLog = null;
 	private XLogCSV copLog = null;
 	private XLogCSV timeLog = null;
 	protected String logFilePrefix = null;
 	// For statistics
 	public HashMap<String, HRegionInfo> regions = null;
 	public List<Long> timePhase = new ArrayList<Long>();
 	public HashMap<String,String> regionAndRS = null;
 		
 	
 	public QueryAbstraction(String schema_desc_file,String csv_desc_file,String hbase_conf){
 		
 		this.getSchema(schema_desc_file);
 		this.getCSVFormat(csv_desc_file);
 		
 		try {
 			
 			this.setHBase(hbase_conf);
 			this.setStatistics(this.tableSchema.getTableName());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public QueryAbstraction(XTableSchema schema,String csv_desc_file,String hbase_conf){		
 		try {
 			
 			this.tableSchema = schema;
 			this.logFilePrefix = this.tableSchema.getLogFilePrefix();	
 			this.setIndexing();
 			this.getCSVFormat(csv_desc_file);
 			
 
 			this.setHBase(hbase_conf);
 			this.setStatistics(this.tableSchema.getTableName());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * to decide which query class should be used based on indexing
 	 * @return
 	 * @throws Exception
 	 */
 	public int getIndexing() throws Exception{
 		if(this.tableSchema == null)
 			throw new Exception("the table schema is wrong with the indexing ");			
 		
 		return this.tableSchema.getIndexing();						
 	}
 	/**
 	 * get the table schema from the descriptio file
 	 * @param schema_desc_file
 	 */
 	private void getSchema(String schema_desc_file) {
 		try{
 			
 			this.tableSchema = new XTableSchema(schema_desc_file);	
 			this.logFilePrefix = this.tableSchema.getLogFilePrefix();
 			this.setIndexing();
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 	}
 	
 	/**
 	 * get the csv format, because we need to know how the cell value is composed by the csv format
 	 * @param csv_desc_file
 	 */
 	private void getCSVFormat(String csv_desc_file) {
 		try{
 			
 			this.csvFormat = new XCSVFormat(csv_desc_file);	
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 	}	
 	
 	/**
 	 * prepare the indexing
 	 * 
 	 * @param indexing
 	 * @param encoding
 	 */
 	private void setIndexing() throws Exception{
 		if(this.tableSchema == null)
 			throw new Exception("the table schema parser is wrong! ");
 		
 		Rectangle2D.Double space = this.tableSchema.getEntireSpace();
 		double min_size_of_subspace = this.tableSchema.getSubSpace();
 		int indexing = this.tableSchema.getIndexing();
 		int encoding = this.tableSchema.getEncoding();
 		
 		if (indexing == XConstants.INDEX_QUAD_TREE) {
 			if (encoding == XConstants.ENCODING_BINARY) {
 
 				this.quadTree = new XQuadTree(space, min_size_of_subspace);
 				this.quadTree.buildTree(XConstants.ENCODING_BINARY);
 
 			} else if (encoding == XConstants.ENCODING_DECIMAL) {
 				this.quadTree = new XQuadTree(space, min_size_of_subspace);
 				this.quadTree.buildTree(XConstants.ENCODING_DECIMAL);
 			}
 		} else if (indexing == XConstants.INDEX_RASTER) {
 			raster = new XRaster(space, min_size_of_subspace);	
 			
 		} else {
 			System.out.println("Indexing parameter is error!");
 		}
 	}
 	
 	private void setHBase(String hbaseConfPath) throws IOException {
 		try{
 			if(hbase == null){
 				hbase = new HBaseUtil(hbaseConfPath);
 				HTable tableHandler = hbase.getTableHandler(this.tableSchema.getTableName());
 				
 				String scanCache = hbase.getHBaseConfig().get("hbase.block.cache.size");	
 				
 				
 				if(scanCache != null){				
 					hbase.setScanConfig(Integer.valueOf(scanCache), true);				
 				}else{
 					System.out.println("Default scan cache: "+tableHandler.getScannerCaching());
 				}				
 			}
 	
 			
 						
 		}catch(Exception e){
 			if(hbase != null)
 				hbase.closeTableHandler();
 			e.printStackTrace();
 		}		
 	}	
 	
 	/**
 	 * do the statistics for the region and store file
 	 * @param tableName
 	 */
 	private void setStatistics(String tableName){
 		try{
 			StatUtil stat  = new StatUtil();
 			regionAndRS = stat.getAllRegionAndRS(tableName); 
 			stat.closeStat();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param filename
 	 * @param header
 	 * @param n 0-main, 1-cop, 2-time
 	 */
 	public void getCSVLog(int n) throws Exception{
 		
 		if(this.logFilePrefix == null)
 			throw new Exception("the log file prefix should be configured in the schema");
 		if(n == 0){			
 			this.mainLog = new XLogCSV((this.logFilePrefix + "-main.csv"),XLogConst.main_header);
 		}else if(n == 1){			
 			this.copLog = new XLogCSV((this.logFilePrefix + "-cop.csv"),XLogConst.cop_header);
 		}else if(n == 2){			
			this.timeLog = new XLogCSV((this.logFilePrefix += "-time.csv"),XLogConst.time_header);
 		}
 		
 	}
 	
 	public void writeCSVLog(String str,int n){
 		if(n == 0){
 			this.mainLog.write(str);			
 		}else if(n == 1){		
 			this.copLog.write(str);
 		}else if(n == 2){		
 			this.timeLog.write(str);
 		}
 		
 	}
 	public void closeCSVLog(){
 		if(this.mainLog != null)
 			this.mainLog.close();
 		if(this.copLog != null)
 			this.copLog.close();
 		if(this.timeLog != null)
 			this.timeLog.close();
 				
 	}	
 
 	/**Some one wants to know at this time, how many available bikes in the
 	 * stations nearest to me
 	 * 
 	 * @param timestamp
 	 * @param latitude
 	 * @param longitude
 	 * @param radius
 	 */
 	public abstract List<String> copQueryAvailableNear(String timestamp,
 			final double latitude, final double longitude,final double radius);
 	
 	
 	/**
 	 *  A scan method that will fetch the row from the RS and compute the
 	 * distance of the points from the given ones.
 	 * @param timestamp
 	 * @param latitude
 	 * @param longitude
 	 * @param radius
 	 */
 	public abstract HashMap<String,String> scanQueryAvailableNear(String timestamp,
 			final double latitude, final double longitude, final double radius);	
 	
 	
 	/**
 	 * This is for point query with Coprocessor
 	 * @param latitude
 	 * @param longitude
 	 */
 	public abstract String copQueryPoint(final double latitude, final double longitude);
 	/**
 	 * This is for point query with scan
 	 * @param latitude
 	 * @param longitude
 	 */
 	public abstract String scanQueryPoint(double latitude, double longitude);
 	
 	/**
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * @param area
 	 */
 	public abstract void copQueryArea(final double latitude, final double longitude,final int area);	
 	
 	/**
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * @param area : north, south, west, east
 	 */
 	public abstract void scanQueryArea(double latitude, double longitude, int area);
 	
 	
 	/**A coprocessor method that will fetch KNN for the given point 
 	 * 
 	 * @param timestamp
 	 * @param latitude
 	 * @param longitude
 	 * @param radius
 	 */
 	public abstract void copQueryAvailableKNN(String timestamp,
 			final double latitude, final double longitude,final int n);
 	
 	/**
 	 *  A scan method that will fetch the row from the RS and compute the
 	 * distance of the points from the given ones.
 	 * @param timestamp
 	 * @param latitude
 	 * @param longitude
 	 * @param radius
 	 */
 	public abstract TreeMap<Double,String> scanQueryAvailableKNN(String timestamp,
 			double latitude, double longitude,int n);	
 
 	/**
 	 * For debug
 	 * @param timestamp
 	 * @param latitude
 	 * @param longitude
 	 * @param radius
 	 * @return
 	 */
 	//public abstract List<Point2D.Double> debugColumnVersion(String timestamp,double latitude, double longitude, double radius);
 	
 }
