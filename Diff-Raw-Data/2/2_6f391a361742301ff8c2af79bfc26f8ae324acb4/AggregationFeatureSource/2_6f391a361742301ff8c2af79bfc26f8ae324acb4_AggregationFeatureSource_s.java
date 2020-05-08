 package org.geotools.data.aggregation;
 
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import nl.utwente.db.neogeo.preaggregate.AggregateAxis;
 import nl.utwente.db.neogeo.preaggregate.AxisSplitDimension;
 
 import org.geoserver.ows.Dispatcher;
 import org.geoserver.ows.Request;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.Query;
 import org.geotools.data.store.ContentEntry;
 import org.geotools.data.store.ContentFeatureSource;
 import org.geotools.factory.Hints;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.DirectPosition2D;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.geometry.MismatchedDimensionException;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Polygon;
 
 @SuppressWarnings("unchecked")
 /**
  * 
  *
  * @source $URL$
  */
 public class AggregationFeatureSource extends ContentFeatureSource {
 	private static final Logger LOGGER = Logger.getLogger("org.geotools.data.aggregation.AggregationFeatureSource");
 
 	private int totalCnt = -1;
 	private PreAggregate agg = null;
 	AggregateAxis x;
 	AggregateAxis y;
 	AggregateAxis time;
 
 	private int xSize;
 
 	private int ySize;
 
 	private int timeSize;
 
 	private int cntAxis;
 
 	private int[] iv_count;
 
 	public AggregationFeatureSource(ContentEntry entry, Query query) {
 		super(entry,query);
 		String typename = entry.getTypeName();
 		typename = PreAggregate.stripTypeName(typename);
 
 		try {
 			AggregationDataStore data = getDataStore();			
 			agg = data.createPreAggregate(typename);
 		} catch (SQLException e) {
 			LOGGER.severe("SQLException for creating the PreAggregate object:"+e.getMessage());
 			e.printStackTrace();
 		}
 		AggregationDataStore data = this.getDataStore();
 		x = agg.getXaxis();
 		y = agg.getYaxis();
 		time = agg.getTimeAxis();
 
 		xSize = data.getXSize();
 		ySize = data.getYSize();
 		timeSize = data.getTimeSize();
 		cntAxis = agg.getAxis().length;
 		iv_count = new int[cntAxis];
 		for(AggregateAxis a : agg.getAxis()){
 			if(a==x)
 				iv_count[0] = xSize;
 			else if(a==y)
 				iv_count[1] = ySize;
 			else if(a==time)
 				iv_count[2] = timeSize;
 		}
 
 	}
 
 	/**
 	 * Access parent AggregationDataStore
 	 */
 	public AggregationDataStore getDataStore(){
 		return (AggregationDataStore) super.getDataStore();
 	}
 
 	public void test(){
 		SimpleFeatureType s = this.getSchema();
 		CoordinateReferenceSystem crs = s.getCoordinateReferenceSystem();
 		crs.getCoordinateSystem();
 	}
 
 	/**
 	 * Implementation that generates the total bounds
 	 * (many file formats record this information in the header)
 	 */
 	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
 		LOGGER.severe("filter: "+query.getFilter().getClass().getCanonicalName());
 		Area a = agg.getArea();
 		ReferencedEnvelope bounds = new ReferencedEnvelope(a.getLowX(),a.getHighX(),a.getLowY(),a.getHighY(),
 				getSchema().getCoordinateReferenceSystem());
 		return bounds;
 	}
 
 	protected int getCountInternal(Query query) throws IOException {
 		//		int count = -1;
 		//		if (query.getFilter() == Filter.INCLUDE) {
 		if(totalCnt==-1) 
 			totalCnt = this.getDataStore().getTotalCount();
 		//			count = totalCnt;
 		//		}else {
 		//			ContentFeatureCollection cfc = this.getFeatures(query);
 		//			count = 0;
 		//			SimpleFeatureIterator iter = cfc.features();
 		//			while(iter.hasNext()) {
 		//				iter.next();
 		//				count++;
 		//			}
 		//			iter.close();
 		//		}
 		return totalCnt;
 	}
 
 	protected Area getReaderInternalGetFeatureInfo(Request req ) {
 		//LOGGER.severe("request context "+req.getRawKvp());
 
 		Map<String,Object> kvp = req.getKvp();
 		for(Entry<String,Object> e : kvp.entrySet()){
 			LOGGER.severe("kvp: key="+e.getKey()+"   value="+e.getValue().toString());
 		}
 		// key=BBOX   value=ReferencedEnvelope[0.14668148058431 : 0.273466415399465, 51.5469266059974 : 51.6177065420478]
 		ReferencedEnvelope re = (ReferencedEnvelope) kvp.get("BBOX");
 		Envelope te = null;
 
 		// transform CRS
 		try {
 			CoordinateReferenceSystem sourceCrs = CRS.decode((String) kvp.get("SRS"));
 			CoordinateReferenceSystem targetCrs = agg.getCoordinateReferenceSystem(getEntry());
 			LOGGER.severe("source CRS:"+sourceCrs.getName().getCode());
 			LOGGER.severe("target CRS:"+targetCrs.getName().getCode());
 			boolean lenient = true;
 			MathTransform mathTransform
 			= CRS.findMathTransform(sourceCrs, targetCrs, lenient);
 			te = JTS.transform( re, mathTransform);
 		} catch (NoSuchAuthorityCodeException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (FactoryException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (MismatchedDimensionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(te!=null){
 			Area area = new Area(te.getMinX(),te.getMaxX(), te.getMinY(), te.getMaxY());
 			//		and then, if not null (in test enviroment it can be),
 			//		check the kvp map, it has the parsed BBOX among the others
 			return area;
 		} return null;
 	}
 
 	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query)
 	throws IOException {
 		Hints hint = query.getHints();
 
 		// viewparams are under the key VIRTUAL_TABLE_PARAMETERS
 		// format of the valye: key:value;k:v
 		// example: testkey:testvalue;secondtestkey:secondtestvalue
 		Map<String, String> values = null;
 		if(hint != null) {
 			values = (Map<String, String>) hint.get(Hints.VIRTUAL_TABLE_PARAMETERS);
 		}
 		if (values == null) {
 			values = Collections.emptyMap();
 		}
 		for(Entry<String, String> e : values.entrySet()){
 			LOGGER.severe("hint entry: "+e.getKey()+"="+e.getValue());
 		}
 		boolean pa_query = true;
 		if(values.containsKey("query") && values.get("query").equals("standard")){
 			pa_query = false;
 		}
 
 		LOGGER.severe("query properties: "+query.getProperties());
 		LOGGER.severe("extracting the bounding box of the request:");
 		LOGGER.severe("filter: "+query.getFilter().getClass().getCanonicalName());
 		// the TIME parameter will be for a continuous interval in the layer definition at the dimension tabs
 		// the filter is a IsBetweenImpl filter
 		AggregationFilterVisitor visitor = new AggregationFilterVisitor(agg);
 		query.getFilter().accept(visitor, null);
 		LOGGER.severe("query parsing valid? "+visitor.isValid());
 		//if(visitor.isValid()){
 		Area a = visitor.getArea();
 		if(a!=null)
 			LOGGER.severe("Parsed filter: "+a.toString());
 		Timestamp startTime = visitor.getStartTime();
 		Timestamp endTime = visitor.getEndTime();
 		LOGGER.severe("Parsed startTime:"+startTime+"    endTime:"+endTime);
 
 		ResultSet rs = null;
 		Object[][] iv_first_obj = null;
 		int[] range = null;
 		try {
 			Object[][] ret ;
 			Request req = Dispatcher.REQUEST.get();
 			LOGGER.severe("request requets "+req.getRequest());
 			// GetFeatureInfo
 			if("GetFeatureInfo".equals(req.getRequest())){
 				Area area = getReaderInternalGetFeatureInfo(req);
 				if(area== null) return null;
 				ret = reformulateQuery(area,startTime,endTime, this.iv_count);
 				double startX = (Double) ret[0][0];
 				double grid_deltaX = ((Double)ret[0][1]) - startX;
 				double endX = startX+grid_deltaX*((Integer)ret[0][2]);
 				double startY = (Double) ret[1][0];
 				double grid_deltaY = ((Double)ret[1][1]) - startY;
 				double endY = startY+grid_deltaY*((Integer)ret[1][2]);
 				double _startX = (double) (Math.floor(a.getLowX()/grid_deltaX))*grid_deltaX;
 				double _endX = (double) (Math.ceil(a.getHighX()/grid_deltaX))*grid_deltaX;				
 				double _startY = (double) (Math.floor(a.getLowY()/grid_deltaY))*grid_deltaY;
 				double _endY = (double) (Math.ceil(a.getHighY()/grid_deltaY))*grid_deltaY;
 				int[] cnt = new int[cntAxis];
 				cnt[0] = (int) Math.round((_endX-_startX) / grid_deltaX);
 				cnt[1] = (int) Math.round((_endY-_startY) / grid_deltaY);
				cnt[2] = this.iv_count[2];
 				ret = reformulateQuery(new Area(_startX, _endX, _startY, _endY),startTime,endTime, cnt);
 			} else {
 				ret = reformulateQuery(a,startTime,endTime, this.iv_count);
 			}
 			range = new int[ret.length];
 			iv_first_obj = new Object[ret.length][2];
 			for(int i=0;i<ret.length;i++){
 				iv_first_obj[i][0]=ret[i][0];
 				iv_first_obj[i][1]=ret[i][1];
 				range[i]=(Integer) ret[i][2];
 			}
 			if(pa_query){
 				LOGGER.severe("processing the query with Aggregation Index");
 				long start = System.currentTimeMillis();
 				rs = agg.SQLquery_grid(this.getDataStore().getMask(), iv_first_obj, range);
 				LOGGER.severe("query response time [ms]: "+(System.currentTimeMillis()-start));
 			} else{
 				LOGGER.severe("processing the query in standard SQL");
 				long start = System.currentTimeMillis();
 				// TODO call the standard query processing
 				LOGGER.severe("query response time [ms]: "+(System.currentTimeMillis()-start));
 			}
 		} catch (Exception e1) {
 			LOGGER.severe("Cought Exception:"+e1.getMessage());
 			LOGGER.severe("there are not results!");
 			e1.printStackTrace();
 			//throw new IOException(e1.getMessage());
 		}
 		//		if(rs!=null)
 		try {
 			return new AggregationFeatureReader( getState(), rs , iv_first_obj, range, agg.getColumnTypes(getDataStore().getMask()));
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		//		} else
 		//			LOGGER.severe("Was not able to understand the specified filter!");
 		return null;
 	}
 
 	//	@Override
 	//	protected boolean canLimit() {
 	//		return true;
 	//	}
 	//	@Override
 	//	protected boolean canOffset() {
 	//		return true;
 	//	}
 	@Override
 	protected boolean canFilter() {
 		return true;
 	}
 	@Override
 	//	protected boolean canRetype() {
 	//		return true;
 	//	}
 	//	@Override
 	//	protected boolean canSort() {
 	//		return true;
 	//	}
 
 	protected SimpleFeatureType buildFeatureType() throws IOException {
 		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
 		builder.setName( entry.getName() );
 		AggregationDataStore data = this.getDataStore();
 		try {
 			Map<String, Class> h = agg.getColumnTypes(data.getMask());
 			for(Entry<String,Class> en : h.entrySet()){
 				builder.add(en.getKey(), en.getValue());
 			}
 		} catch (ClassNotFoundException e) {
 			LOGGER.severe("transform ClassNotFoundException int a IOException");
 			throw new IOException(e.getMessage());
 			//e.printStackTrace();
 		}
 		//		if(data.hasOutputCount(agg.getAggregateMask()))
 		//			builder.add("cnt", Integer.class);
 		//		if(data.hasOutputSum(agg.getAggregateMask()))
 		//			builder.add("sum", Double.class);
 		//		if(data.hasOutputMin(agg.getAggregateMask()))
 		//			builder.add("min", Double.class);
 		//		if(data.hasOutputMax(agg.getAggregateMask()))
 		//			builder.add("max", Double.class);
 		//		builder.add("time",Timestamp.class);
 
 		//		CRSAuthorityFactory   factory = CRS.getAuthorityFactory(true);
 		//		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84; // <- Coordinate reference system
 		//		try {
 		//			if(crsNumber==-1)
 		//				crsNumber = agg.getCRSNumber(entry);
 		//			crs = factory.createCoordinateReferenceSystem("EPSG:"+crsNumber);
 		//		} catch (NoSuchAuthorityCodeException e) {
 		//			e.printStackTrace();
 		//		} catch (FactoryException e) {
 		//			e.printStackTrace();
 		//		}
 		CoordinateReferenceSystem crs = agg.getCoordinateReferenceSystem(entry);
 		builder.setCRS(crs); // <- Coordinate reference system
 		builder.add("area", Polygon.class );
 
 		// build the type (it is immutable and cannot be modified)
 		final SimpleFeatureType SCHEMA = builder.buildFeatureType();
 		return SCHEMA;
 	}
 
 	private Object[][] reformulateQuery(Area area, Timestamp start, Timestamp end, int[] iv_count) throws RuntimeException{
 		int i=0;
 		//int[] range = new int[agg.getAxis().length];
 		Object[][] iv_obj = new Object[agg.getAxis().length][3];
 		for(AggregateAxis a : agg.getAxis()){
 			AxisSplitDimension dim = null;
 			LOGGER.severe("axis :"+a.columnExpression());
 			if(a==x) {
 				LOGGER.severe(area.getLowX()+"|"+ area.getHighX()+"|"+ iv_count[0]);
 				LOGGER.severe("processing axis x:"+a.columnExpression());
 				dim = a.splitAxis(area.getLowX(), area.getHighX(), iv_count[0]);
 				i=0;
 			} else
 				if(a==y) {
 					LOGGER.severe(area.getLowY()+"|"+ area.getHighY()+"|"+ iv_count[1]);
 					LOGGER.severe("processing axis y:"+a.columnExpression());
 					dim = a.splitAxis(area.getLowY(), area.getHighY(), iv_count[1]);
 					i=1;
 				} else
 					if(a==time) {
 						LOGGER.severe(start+"|"+ end+"|"+ iv_count[2]);
 						LOGGER.severe("processing axis time:"+a.columnExpression());
 						dim = a.splitAxis(start, end, iv_count[2]);
 						i=2;
 					}
 			//if(dim==null) throw new Exception("query area out of available data domain due to problems in axis "+a.columnExpression());
 			//			range[i] = dim.getCount();
 			LOGGER.severe("dim values:"+dim.toString());
 			iv_obj[i][0] = dim.getStart();
 			iv_obj[i][1] = dim.getEnd();
 			iv_obj[i][2] = dim.getCount();
 		}
 		//		ResultSet rs = agg.SQLquery_grid(PreAggregate.AGGR_COUNT, iv_first_obj, range);
 		//		return rs;
 		return iv_obj;
 	}
 	//		
 	//		
 	//        // number of basic units to be split up in boxes in the map
 	//        double origDiffX = (area.getHighX()-area.getLowX())/((Double)x.BASEBLOCKSIZE());
 	//        // the goal is to have xSize boxes thus have a look how many baseboxsizes 
 	//        // can be used per box
 	//        double grid_deltaX = Math.round(origDiffX/((double) xSize))*((Double)x.BASEBLOCKSIZE());
 	//        
 	//        // determine the start of the first box 
 	//        double startX = Math.ceil(area.getLowX()/DFLT_BASEBOXSIZE)* DFLT_BASEBOXSIZE;
 	//        // determine the number of boxes which can be displayed
 	//        double endX = Math.floor(area.getHighX()/DFLT_BASEBOXSIZE)* DFLT_BASEBOXSIZE;
 	//        xSize = grid_deltaX==0 ? 0 : (int) Math.floor((endX - startX)/grid_deltaX);
 	//        
 	//        double origDiffY = (area.getHighY()-area.getLowY())/DFLT_BASEBOXSIZE;
 	//        double grid_deltaY = Math.round(origDiffY/((double) ySize))*DFLT_BASEBOXSIZE;
 	//        // determine the start of the first box 
 	//        double startY = Math.ceil(area.getLowY()/DFLT_BASEBOXSIZE)* DFLT_BASEBOXSIZE;
 	//        // determine the number of boxes which can be displayed
 	//        double endY = Math.floor(area.getHighY()/DFLT_BASEBOXSIZE)* DFLT_BASEBOXSIZE;
 	//        ySize = grid_deltaY==0 ? 0 : (int) Math.floor((endY - startY)/grid_deltaY);
 	//        
 	//        //TODO check with Jan whether this is indeed the way the intervals are calculated
 	//        
 	//        LOGGER.severe("area: "+area.toString());
 	//        LOGGER.severe("X: startX:"+startX+"   xSize:"+xSize+"    grid_deltaX:"+grid_deltaX);
 	//        LOGGER.severe("Y: startY:"+startY+"   ySize:"+ySize+"    grid_deltaY:"+grid_deltaY);
 	//        Object[][] arg1;
 	//		agg.SQLquery_grid(agg.getAggregateMask(), arg1, iv_count);
 	//	}
 }
