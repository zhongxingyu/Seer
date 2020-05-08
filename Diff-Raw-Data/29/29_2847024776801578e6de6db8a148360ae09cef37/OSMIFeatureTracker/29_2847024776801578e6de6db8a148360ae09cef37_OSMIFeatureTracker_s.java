 package org.openstreetmap.josm.plugins.osminspector;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.geotools.feature.FeatureCollection;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 public class OSMIFeatureTracker 
 {
 
 	private HashMap< Long, SimpleFeature > hashFeatures;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> features;
 
 	public OSMIFeatureTracker( FeatureCollection<SimpleFeatureType, SimpleFeature> featuresIn ) 
 	{
 		hashFeatures 	= new HashMap();
		features		= featuresIn;
		mergeFeatures( features );
 		
 	}
 
 	public boolean mergeFeatures( FeatureCollection<SimpleFeatureType, SimpleFeature> newFeatures )
 	{
 		for( Iterator it = newFeatures.iterator(); it.hasNext(); )
 		{
 			SimpleFeature element = (SimpleFeature) it.next();
 			Long ID = ( Long.parseLong( (String) element.getAttribute( "problem_id" ) ) );
 			
 			if( ! hashFeatures.containsKey(ID ) )
 			{
 				hashFeatures.put( ID, element );
 				features.add( element );
 			}
 		}
 		
 		return true;
 	}
 	
 	public HashMap< Long, SimpleFeature > getFeatureHash()
 	{
 		return hashFeatures;
 	}
 
 	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures()
 	{
 		return features;
 	}
 }
