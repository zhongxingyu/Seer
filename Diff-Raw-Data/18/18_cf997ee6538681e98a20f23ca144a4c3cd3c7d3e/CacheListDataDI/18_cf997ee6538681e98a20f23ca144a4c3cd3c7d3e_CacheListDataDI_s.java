 
 package com.google.code.geobeagle.data.di;
 
 import com.google.code.geobeagle.data.CacheListData;
 import com.google.code.geobeagle.data.DestinationVectors;
 import com.google.code.geobeagle.data.DistanceFormatter;
 import com.google.code.geobeagle.data.DestinationVector.LocationComparator;
 
import android.content.Context;

 public class CacheListDataDI {
 
    public static CacheListData create(DestinationFactory destinationFactory, Context parent) {
         final DistanceFormatter distanceFormatter = new DistanceFormatter();
 
         final DestinationVectorFactory destinationVectorFactory = new DestinationVectorFactory(
                destinationFactory, distanceFormatter, null);
 
         final LocationComparator locationComparator = new LocationComparator();
         final DestinationVectors destinationVectors = new DestinationVectors(locationComparator,
                 destinationVectorFactory);
         return new CacheListData(destinationVectors, destinationVectorFactory);
     }
 
 }
