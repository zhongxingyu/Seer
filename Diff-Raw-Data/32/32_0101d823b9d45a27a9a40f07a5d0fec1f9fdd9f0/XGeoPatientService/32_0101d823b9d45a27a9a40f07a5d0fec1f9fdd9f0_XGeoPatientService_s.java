 package savi.hcat.rest.service;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.client.coprocessor.Batch;
 import org.apache.hadoop.hbase.filter.Filter;
 import org.apache.hadoop.hbase.filter.FilterList;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import savi.hcat.analytics.coprocessor.HCATProtocol;
 import savi.hcat.analytics.coprocessor.RStatResult;
 import savi.hcat.common.util.XConstants;
 
 import com.util.XCommon;
 import com.util.XTableSchema;
 import com.util.hybrid.XHybridIndex;
 import com.util.raster.XBox;
 
 public class XGeoPatientService extends XBaseGeoService implements XGeoSpatialInterface{
 
 	private static Log LOG = LogFactory.getLog(XGeoPatientService.class);
 	
 	public XGeoPatientService(){
 		this.tableSchema = new XTableSchema("schema/patient-spatial.schema"); // it is used for proving table description to query in hbase
 		if(this.tableSchema == null){
 			LOG.error("the table schema fails to be loaded ");
 		}else{
 			LOG.info("family name: "+this.tableSchema.getFamilyName()+"; version"+this.tableSchema.getMaxVersions());
 			
 			Rectangle2D.Double space = this.tableSchema.getEntireSpace();
 			Point2D.Double offset = this.tableSchema.getOffset();
 					
 			double min_size_of_subspace = this.tableSchema.getSubSpace();					
 			int encoding = this.tableSchema.getEncoding();
 			
 			this.hybrid = new XHybridIndex(space,this.tableSchema.getTileSize(),offset,min_size_of_subspace);
 			this.hybrid.buildZone(encoding);			
 		}
 		try {
 			this.setHBase();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	/**
 	 * For windows query
 	 */
 	@Override
 	public JSONArray doWindowStatistics(JSONObject request) {
 		LOG.info("in getSummary");
 		// get parameters of the query
 		boolean decomposed = this.decompose(request);
 		if(!decomposed)
 			LOG.error("decompose Error!");
 
 		//prepare the callback function
 		WindowsQueryCallBack callback = new WindowsQueryCallBack(this);
 		
 		// compose the HBase RPC call
 		String[] rowRange = getWindowsScanRowRange();// getRowRange		
 		
 		// send separate queries for each city
 		for(final String region: regions){			
			String start = region+XConstants.ROW_KEY_DELIMETER+rowRange[0];
			String end = region+XConstants.ROW_KEY_DELIMETER+rowRange[1];
 			try {
 				// create the scan 
 				//final Scan scan = hbase.generateScan(new String[]{start,end}, fList,
 					//	new String[] { this.tableSchema.getFamilyName() }, null,-1);
				FilterList fList = getWindowsScanFilterList(region);// getFilter list
 				final Scan scan = hbase.generateScan(null, fList,
 						new String[] { this.tableSchema.getFamilyName() }, null,-1);
 				
 				LOG.info("scan: "+scan.toString());
 				//send the caller 
 				hbase.getHTable().coprocessorExec(HCATProtocol.class,
 						scan.getStartRow(), scan.getStopRow(),
 						new Batch.Call<HCATProtocol, RStatResult>() {
 
 					public RStatResult call(HCATProtocol instance)
 							throws IOException {
 						final HashMap<String,Rectangle2D.Double> scopes = getQuads();
 						return instance.doWindowQuery(scan, region,scopes);
 					};
 				}, callback);
 
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block			
 				e1.printStackTrace();
 			} catch (Throwable e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		// TODO callback.regions;
 		LOG.info("the returned value: "+callback.regions.toString());
 		for(String key: callback.regions.keySet()){
 			RStatResult result = callback.regions.get(key);
 			JSONObject regionJSON = new JSONObject();
 		//	TreeMap<String,Integer> treemap = new TreeMap<String,Integer>(result.getHashUnit());
 			try {
 				regionJSON.put("region", key);
 				JSONArray values = new JSONArray();
 				for(Entry<String,Integer> entry: result.getHashUnit().entrySet()){
 					values.put(new JSONObject().put(entry.getKey(), entry.getValue()));
 				}
 				regionJSON.put("values", values);
 				// add the statistics of request
 				JSONObject reqStatJSON = this.buildRequestStat(result);
 				regionJSON.put("request_stat", reqStatJSON);
 				
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 			this.response.put(regionJSON);
 		}
 		
 		return this.response;
 	}
 	
 	
 	protected String[] getWindowsScanRowRange() {
 		LOG.info("getScanRowRange");
 		String[] rowRange = new String[2];
 		// this means the search would be in the entire data
		rowRange[0] = "";
		rowRange[1] = "*"; // it means include all rows before 
 		LOG.info("row range: "+rowRange[0]+"=>"+rowRange[1]);
 		return rowRange;
 	}
 	
	protected FilterList getWindowsScanFilterList(String region) {
 		FilterList fList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
 		try{
 			Filter filter = hbase.getPrefixFilter(region+XConstants.ROW_KEY_DELIMETER);	
 			fList.addFilter(filter);
 		}catch(Exception e){
 			e.printStackTrace();
 		}		
 		return fList;
 	}
 	
 	protected HashMap<String,Rectangle2D.Double> getQuads(){
 		HashMap<String,Rectangle2D.Double> quads = new HashMap<String,Rectangle2D.Double>();
 		//NW
 		Rectangle2D.Double entireSpace = this.tableSchema.getEntireSpace();
 		quads.put("NW",new Rectangle2D.Double(entireSpace.getMinX(),entireSpace.getMinY(),
 												entireSpace.width/2,entireSpace.height/2));
 		//NE
 		quads.put("NE",new Rectangle2D.Double(entireSpace.getCenterX(),entireSpace.getMinY(),
 												entireSpace.width/2,entireSpace.height/2));
 		//SW
 		quads.put("SW",new Rectangle2D.Double(entireSpace.getMinX(),entireSpace.getCenterY(),
 												entireSpace.width/2,entireSpace.height/2));
 		//SE		
 		quads.put("SE",new Rectangle2D.Double(entireSpace.getCenterX(),entireSpace.getCenterY(),
 												entireSpace.width/2,entireSpace.height/2));
 		
 		return quads;
 	}
 	
 	
 
 
 
 	/**
 	 * For windows query
 	 * @author dan
 	 *
 	 */
 	class WindowsQueryCallBack implements Batch.Callback<RStatResult> {
 		public HashMap<String, RStatResult> regions = new HashMap<String,RStatResult>();
 		int count = 0; // the number of coprocessor
 		XGeoPatientService service = null;
 
 		public WindowsQueryCallBack(XGeoPatientService s) {
 			this.service = s;				
 		}
 
 		@Override
 		public void update(byte[] region, byte[] row, RStatResult result) {	
 			LOG.info("windowsStatCallback: update");			
 			String patientRegion = result.getRegion();
 			Hashtable<String,Integer> hashUnit = result.getHashUnit();
 			System.out.println(patientRegion+"=>"+hashUnit.toString());
 			if(this.regions.containsKey(patientRegion)){
 				Hashtable<String,Integer> temp = this.regions.get(patientRegion).getHashUnit();
 				for(String key:temp.keySet()){
 					temp.put(key, temp.get(key)+hashUnit.get(key));
 				}				
 			}else{
 				this.regions.put(patientRegion, result);
 			}			
 		}
 	}	
 	
 	/**
 	 * For range query
 	 */
 	@Override
 	public JSONArray doRangeSearch(JSONObject request) {
 		LOG.info("in doRangeSearch: "+request.toString());
 		long s_time = System.currentTimeMillis();
 		
 		// get parameters of the query
 		boolean decomposed = this.decompose(request);
 		if(!decomposed)
 			LOG.error("decompose Error!");
 
 		//prepare the callback function
 		RangeQueryCallBack callback = new RangeQueryCallBack(this);
 		
 		// compose the HBase RPC call
 		final double x = Math.abs(this.latitude);
 		final double y = Math.abs(this.longitude);
 		Hashtable<String, XBox[]> matched = this.hybrid.match(x,y, this.radius);
 		String[] rowRange = getRangeScanRowRange(matched);// getRowRange		
 
 		// send separate queries for each city
 		for(String region: regions){			
 			String start = region+XConstants.ROW_KEY_DELIMETER+rowRange[0];
 			String end = region+XConstants.ROW_KEY_DELIMETER+rowRange[1];
 			try {
 				// create the scan 
 				FilterList fList = getRangeScanFilterList(region,matched);// getFilter list
 				final Scan scan = hbase.generateScan(new String[]{start,end}, fList,
 						new String[] { this.tableSchema.getFamilyName() }, null,-1);
 				
 				LOG.info("scan: "+scan.toString());
 				//send the caller 
 				hbase.getHTable().coprocessorExec(HCATProtocol.class,
 						scan.getStartRow(), scan.getStopRow(),
 						new Batch.Call<HCATProtocol, RStatResult>() {
 
 					public RStatResult call(HCATProtocol instance)
 							throws IOException {
 												
 						return instance.doRangeQuery(scan, latitude, longitude,radius);
 					};
 				}, callback);	
 				
 
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block			
 				e1.printStackTrace();
 			} catch (Throwable e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		long cop_end = System.currentTimeMillis();
 		
 		long exe_time = cop_end - s_time;
 					
 		LOG.info("the returned value: "+callback.regions.toString()+";exe_time=>"+exe_time);
 		for(String key: callback.regions.keySet()){
 			RStatResult result = callback.regions.get(key);
 			HashMap<String,Double> one_region = result.getDistances();
 			JSONObject regionJSON = new JSONObject();
 			try {
 				regionJSON.put("region", key);
 				JSONArray values = new JSONArray();
 				for(Entry<String,Double> entry: one_region.entrySet()){
 					values.put(new JSONObject().put(entry.getKey(), entry.getValue()));
 				}
 				regionJSON.put("values", values);
 				
 				// add the statistics of request
 				JSONObject reqStatJSON = this.buildRequestStat(result);
 				regionJSON.put("request_stat", reqStatJSON);				
 				
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}	
 			this.response.put(regionJSON);
 		}
 		
 		return this.response;
 	}
 	
 	protected String[] getRangeScanRowRange(Hashtable<String, XBox[]> matched) {
 		LOG.info("getRangeScanRowRange");
 		String[] rowRange = new String[2];
 		LOG.info("match result from HGrid: "+matched.toString());
 
 		rowRange[0] = matched.get("")[0].getRow();
 		rowRange[1] = matched.get("")[1].getRow(); // it means include all rows before 
 		LOG.info("row range: "+rowRange[0]+"=>"+rowRange[1]);
 		return rowRange;
 	}
 	
 	protected FilterList getRangeScanFilterList(String region,Hashtable<String, XBox[]> matched) {
 		FilterList fList = new FilterList();
 		try{
 			Filter columnFilter = hbase.getColumnRangeFilter((matched.get("")[0].getColumn()+"-").getBytes(),true,					
 					(XCommon.IncFormatString(matched.get("")[1].getColumn())+"-").getBytes(),true);	
 			fList.addFilter(columnFilter);
 			String top = region+"-"+ matched.get("")[0].getRow();
 			String down = region+"-"+ matched.get("")[1].getRow();
 			LOG.info("top row=>"+top+";down=>"+down);
 			Filter rowTopFilter = hbase.getBinaryFilter(">=", top);
 			Filter rowDownFilter = hbase.getBinaryFilter("<=", down);			
 			fList.addFilter(rowTopFilter);
 			fList.addFilter(rowDownFilter);
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		return fList;
 	}	
 	
 	
 	class RangeQueryCallBack implements Batch.Callback<RStatResult> {
 		public HashMap<String, RStatResult> regions = new HashMap<String,RStatResult>();
 		int count = 0; // the number of coprocessor
 		XGeoPatientService service = null;
 
 		public RangeQueryCallBack(XGeoPatientService s) {
 			this.service = s;				
 		}
 
 		@Override
 		public void update(byte[] region, byte[] row, RStatResult result) {	
 			LOG.info("RangeQueryCallBack: update");			
 			String patientRegion = result.getRegion();
 			HashMap<String,Double> distances = result.getDistances();
 			if(this.regions.containsKey(patientRegion)){
 				HashMap<String,Double> temp = this.regions.get(patientRegion).getDistances();
 				temp.putAll(distances);				
 			}else{
 				this.regions.put(patientRegion, result);
 			}			
 		}
 	}
 	
 	
 
 	@Override
 	public JSONArray doKNNSearch(JSONObject request) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 
 }
