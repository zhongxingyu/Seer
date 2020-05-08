 
 package com.google.code.geobeagle.data.di;
 
import com.google.code.geobeagle.ResourceProvider;
 import com.google.code.geobeagle.data.CacheListData;
 import com.google.code.geobeagle.data.DestinationVectors;
 import com.google.code.geobeagle.data.DistanceFormatter;
 import com.google.code.geobeagle.data.DestinationVector.LocationComparator;
 
 public class CacheListDataDI {
 
    public static CacheListData create(ResourceProvider resourceProvider,
            DestinationFactory destinationFactory) {
         final DistanceFormatter distanceFormatter = new DistanceFormatter();
 
         final DestinationVectorFactory destinationVectorFactory = new DestinationVectorFactory(
                destinationFactory, distanceFormatter, resourceProvider);
 
         final LocationComparator locationComparator = new LocationComparator();
         final DestinationVectors destinationVectors = new DestinationVectors(locationComparator,
                 destinationVectorFactory);
         return new CacheListData(destinationVectors, destinationVectorFactory);
     }
 
 }
