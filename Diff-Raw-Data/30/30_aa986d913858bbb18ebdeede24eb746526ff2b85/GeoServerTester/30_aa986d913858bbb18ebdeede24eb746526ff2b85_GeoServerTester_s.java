 package it.geosolutions.geoservertester;
 
 import it.geosolutions.batchgeocoder.io.Input;
 import it.geosolutions.batchgeocoder.io.Output;
 import it.geosolutions.batchgeocoder.model.Location;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.lang.StringUtils;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.filter.Expression;
 import org.geotools.filter.text.cql2.CQL;
 import org.geotools.filter.text.cql2.CQLException;
 
 public class GeoServerTester {
 	private static final String GEOSERVER_URL_PARAMETER = "geoserver.url";
 	private static final String TYPENAME_PARAMETER  = "typename";
 	private static final String CONNECTION_PARAMETERS_CAPABILITIES_URL = "WFSDataStoreFactory:GET_CAPABILITIES_URL";
 	private static Logger LOG = Logger.getLogger(GeoServerTester.class
 			.getCanonicalName());
 
 	private Input repo;
 	
	private Output listGeocoded;
	private Output outDiscarded;
 	private Configuration conf;
 
 	public GeoServerTester(Configuration conf) {
 		this.conf = conf;
 
 	}
 
 	public void runTest() {
 		loadInput();
 
 	}
 
 	private void loadInput() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Configuration getConf() {
 		return conf;
 	}
 
 	public void setConf(Configuration conf) {
 		this.conf = conf;
 	}
 
 	public void test(List<Location> locations) {
 		int errors=0;
 		int found=0;
 		int notfound=0;
 		int multiple=0;
 		Map connectionParameters = new HashMap();
 		connectionParameters.put(CONNECTION_PARAMETERS_CAPABILITIES_URL, conf.getProperty(GEOSERVER_URL_PARAMETER) );
 		System.out.println(locations.size());
 		
 		DataStore data;
 		FeatureSource<SimpleFeatureType, SimpleFeature> source;
 		String typeName = conf.getString(TYPENAME_PARAMETER);
 		try {
 			//connection
 			data = DataStoreFinder.getDataStore( connectionParameters );
 			//discovery
 			/*
 			String typeNames[] = data.getTypeNames();
 			typeName = typeNames[0];
 			SimpleFeatureType schema = data.getSchema( typeName );
 			*/
 			
 			//target
 			source = data.getFeatureSource( typeName );
 		
 		} catch (IOException e) {
 			LOG.severe("UNABLE TO INIZIALIZE CONNECTION TO THE WFS");
 			return;
 		}
 		for(Location l : locations){
 			Filter filter;
 			try {
 				
 				String[] components = l.getName().split("'");
 				String escaped_name = StringUtils.join(components, "''");
 				filter = CQL.toFilter("name ='"+escaped_name+"'");
 			} catch (CQLException e) {
 				LOG.severe("UNABLE TO CREATE FILTER for" + l.getName() );
 				e.printStackTrace();
 				
 				return;
 			}
 			try {
 				Query query = new DefaultQuery( typeName, filter );
 				FeatureCollection<SimpleFeatureType, SimpleFeature> features = null;
 				
 					features = source.getFeatures( query );
 				
 				int number = features.size();
 				if(number==1){
 					match(l);
 				}else{
					notMatch(l,number);
 					if(number>1){
 						
 						multiple ++;
 					}else if(number<1){
 						notfound++;
 						
 					}
 				}
 			} catch (IOException e) {
 				notMatch(l);
 				errors++;
 			}
 			
 		}
		report(found,notfound,multiple,errors);
 		
 	}
 
	private void report(int found, int notfound,int multiple, int errors) {
 		System.out.println("\nFound:"+found);
 		System.out.println("\nNot Found:"+notfound);
 		System.out.println("\nMultiple:"+multiple);
 		System.out.println("\nErrors:"+errors);
 		int tot = notfound +found+multiple +errors;
 		System.out.println("\nTotal:"+tot);
 		
 	}
 
 	private void notMatch(Location l) {
		System.out.println("ERROR WHILE GETTING THE OBJECT");
 		
 		
 	}
 
 	private void match(Location l) {
		System.out.println("FOUND!");
 		
 	}
 
	private void notMatch(Location l,int nResults) {
		System.out.println("NOT FOUND!");
 		
 		
 	}
 }
