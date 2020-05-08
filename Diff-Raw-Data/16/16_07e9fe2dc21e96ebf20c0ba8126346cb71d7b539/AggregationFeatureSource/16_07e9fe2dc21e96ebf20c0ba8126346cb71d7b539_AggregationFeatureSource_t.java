 package org.geotools.data.aggregation;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import nl.utwente.db.neogeo.preaggregate.PreAggregate;
 
 import org.geotools.data.FeatureReader;
 import org.geotools.data.Join;
 import org.geotools.data.Query;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.store.ContentEntry;
 import org.geotools.data.store.ContentFeatureCollection;
 import org.geotools.data.store.ContentFeatureSource;
 import org.geotools.factory.Hints;
 import org.geotools.factory.Hints.Key;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterVisitor;
 import org.opengis.geometry.BoundingBox;
 import org.opengis.geometry.DirectPosition;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CRSAuthorityFactory;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.vividsolutions.jts.geom.Polygon;
 
 @SuppressWarnings("unchecked")
 /**
  * 
  *
  * @source $URL$
  */
 public class AggregationFeatureSource extends ContentFeatureSource {
 	private static final Logger LOGGER = Logger.getLogger("org.geotools.data.aggregation.AggregationFeatureSource");
 
 	private int crsNumber = -1;
 	private int totalCnt = -1;
 	private Object totalBounds = null;
 	private PreAggregate agg = null;
 	
 	public AggregationFeatureSource(ContentEntry entry, Query query) {
 		super(entry,query);
 		String typename = entry.getTypeName();
		typename = ((AggregationDataStore)entry.getDataStore()).stripTypeName(typename);
 		LOGGER.severe("TypeName: "+typename);
		
 		try {
 			AggregationDataStore data = getDataStore();
 			agg = data.createPreAggregate(typename);
 		} catch (SQLException e) {
			LOGGER.severe("SQLException for creating the PreAggregate object:"+e.getMessage());
			e.printStackTrace();
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
 		CoordinateReferenceSystem crs = query.getCoordinateSystem();
 		if (crs!=null) LOGGER.severe("query CRS: "+crs.getAlias());
 
 
 		ReferencedEnvelope bounds = this.getDataStore().getReferencedEnvelope(entry, getSchema().getCoordinateReferenceSystem());
 
 		return bounds;
 	}
 
 	protected int getCountInternal(Query query) throws IOException {
 		int count = -1;
 		if (query.getFilter() == Filter.INCLUDE) {
 			if(totalCnt==-1) 
 				totalCnt = this.getDataStore().getTotalCount(query.getTypeName());
 			count = totalCnt;
 		}else {
 			ContentFeatureCollection cfc = this.getFeatures(query);
 			count = 0;
 			SimpleFeatureIterator iter = cfc.features();
 			while(iter.hasNext()) {
 				iter.next();
 				count++;
 			}
 			iter.close();
 		}
 		return count;
 	}
 
 	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query)
 	throws IOException {
 		Hints hint = query.getHints();
 		for(Entry<Object, Object> e : hint.entrySet()){
 			LOGGER.severe("hint entry: "+e.getKey()+"="+e.getValue());
 		}
 		// viewparams are under the key VIRTUAL_TABLE_PARAMETERS
 		// format of the valye: key:value;k:v
 		// example: testkey:testvalue;secondtestkey:secondtestvalue
 
 		LOGGER.severe("query properties: "+query.getProperties());
 		//LOGGER.severe("query analysis: "+AggregationUtilities.analyseFilter(query.getFilter()));
 		// Note we ignore 'query' because querying/filtering is handled in superclasses.
 		// http://opensourcejavaphp.net/java/geotools/org/geotools/data/store/ContentFeatureSource.java.html
 		// set capabilities to avoid checking by superclass
 		// canFilter() 
 		// canReproject()
 		// canOffset()
 		// canLimit()
 		// canSort()
 		// canRetype() 
 		ReferencedEnvelope bounds = new ReferencedEnvelope(query.getCoordinateSystem());
 		//		DirectPosition lc = bounds.getLowerCorner();
 		//		DirectPosition uc = bounds.getUpperCorner();
 		//		double[] lcord = lc.getCoordinate();
 		//		double[] ucord = uc.getCoordinate();
 		LOGGER.severe("extracting the bounding box of the request:");
 		//		LOGGER.severe("lower corner dimensions : "+lcord.length);
 		//		LOGGER.severe("lower corner 0 : "+lcord[0]);
 		//		LOGGER.severe("lower corner 1 : "+lcord[1]);
 		//		LOGGER.severe("upper corner dimensions : "+ucord.length);
 		//		LOGGER.severe("upper corner 0 : "+ucord[0]);
 		//		LOGGER.severe("upper corner 1 : "+ucord[1]);
 		//		Filter ff = query.getFilter();
 		//		FilterVisitor ffv;
 		//		ff.accept(ffv, null);
 		LOGGER.severe("filter: "+query.getFilter().getClass().getCanonicalName());
 		// the TIME parameter will be for a continuous interval in the layer definition at the dimension tabs
 		// the filter is a IsBetweenImpl filter
 		// TODO this not covered yet by the filter analysis
 		// if TIME parameter is not specified the filter is based on a IncludeFilter for 
 		// propertyNames [TIME]
 		// TODO handle IncludeFilter
 		//Area a = AggregationUtilities.analyseFilterArea(query.getFilter());
 		AggregationFilterVisitor visitor = new AggregationFilterVisitor();
 		query.getFilter().accept(visitor, null);
 		LOGGER.severe("query parsing valid? "+visitor.isValid());
 //		if(visitor.isValid()){
 			Area a = visitor.getArea();
 			if(a!=null)
 				LOGGER.severe("Parsed filter: "+a.toString());
 			long startTime = visitor.getStartTime();
 			long endTime = visitor.getEndTime();
 			LOGGER.severe("Parsed startTime:"+startTime+"    endTime:"+endTime);
 			return new AggregationFeatureReader( getState(), a );
 //		} else 
 //			return null;
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
 		
 		//TODO move this to PreAggregate
 		CRSAuthorityFactory   factory = CRS.getAuthorityFactory(true);
 		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84; // <- Coordinate reference system
 		try {
 			if(crsNumber==-1)
 				crsNumber = this.getDataStore().getCRSNumber(entry);
 			crs = factory.createCoordinateReferenceSystem("EPSG:"+crsNumber);
 		} catch (NoSuchAuthorityCodeException e) {
 			e.printStackTrace();
 		} catch (FactoryException e) {
 			e.printStackTrace();
 		}
 		builder.setCRS(crs); // <- Coordinate reference system
 		builder.add("area", Polygon.class );
 
 		// build the type (it is immutable and cannot be modified)
 		final SimpleFeatureType SCHEMA = builder.buildFeatureType();
 		return SCHEMA;
 	}
 
 }
