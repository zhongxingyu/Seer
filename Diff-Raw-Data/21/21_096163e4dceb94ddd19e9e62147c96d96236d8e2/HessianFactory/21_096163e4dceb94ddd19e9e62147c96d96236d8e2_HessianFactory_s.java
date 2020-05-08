 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.storage.hessian;
 
 import java.io.Serializable;
 import java.util.Map;
 
 import org.geogit.api.RevCommit;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevTree;
 import org.geogit.storage.BlobPrinter;
 import org.geogit.storage.ObjectDatabase;
 import org.geogit.storage.ObjectReader;
 import org.geogit.storage.ObjectSerialisingFactory;
 import org.geogit.storage.ObjectWriter;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.FeatureType;
 
 public class HessianFactory implements ObjectSerialisingFactory {
 
     @Override
     public BlobPrinter createBlobPrinter() {
         return new HessianBlobPrinter();
     }
 
     @Override
     public ObjectReader<RevCommit> createCommitReader() {
        return new HessianCommitReader();
     }
 
     @Override
     public ObjectWriter<RevCommit> createCommitWriter(RevCommit commit) {
         return new HessianCommitWriter(commit);
     }
 
     @Override
     public ObjectReader<RevFeature> createFeatureReader(RevFeatureType featureType, String featureId) {
        FeatureType simpleType = (FeatureType) featureType;
         ObjectReader<RevFeature> reader = new HessianFeatureReader(simpleType, featureId, null);
         return reader;
     }
 
     @Override
     public ObjectReader<RevFeature> createFeatureReader(final RevFeatureType featureType,
             final String featureId, final Map<String, Serializable> hints) {
         FeatureType simpleType = (FeatureType) featureType;
         return new HessianFeatureReader(simpleType, featureId, hints);
     }
 
     @Override
     public ObjectWriter<RevFeature> createFeatureWriter(RevFeature feature) {
         return new HessianFeatureWriter((Feature) feature.feature());
     }
 
     @Override
     public ObjectReader<RevTree> createRevTreeReader(ObjectDatabase objectDb) {
         return new HessianRevTreeReader(objectDb, this);
     }
 
     @Override
     public ObjectReader<RevTree> createRevTreeReader(ObjectDatabase objectDb, int order) {
         return new HessianRevTreeReader(objectDb, order, this);
     }
 
     @Override
     public ObjectWriter<RevTree> createRevTreeWriter(RevTree tree) {
         return new HessianRevTreeWriter(tree);
     }
 
     @Override
     public ObjectWriter<RevFeatureType> createFeatureTypeWriter(RevFeatureType type) {
         return new HessianSimpleFeatureTypeWriter((SimpleFeatureType) type.type());
     }
 
     /**
      * @param name
      * @return
      * @see org.geogit.storage.ObjectSerialisingFactory#createFeatureTypeReader()
      */
     @Override
     public ObjectReader<RevFeatureType> createFeatureTypeReader() {
        return new HessianSimpleFeatureTypeReader();
     }
 
     public ObjectReader<RevObject.TYPE> createObjectTypeReader() {
        return new HessianObjectTypeReader();
     }
 
 }
