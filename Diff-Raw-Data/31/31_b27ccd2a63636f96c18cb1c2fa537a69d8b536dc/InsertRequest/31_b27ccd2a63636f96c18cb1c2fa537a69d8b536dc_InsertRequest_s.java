 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.requests.wfs;
 
 import org.geotools.feature.*;
 import org.geotools.filter.Filter;
 import org.vfny.geoserver.*;
 import org.vfny.geoserver.requests.*;
 import org.vfny.geoserver.responses.wfs.*;
 import java.util.*;
 import java.util.logging.*;
 
 
 /**
  * Represents a request for an insert operation.  Does some type checking by
  * making sure that all features added have the same schema names  (which is
  * also the type name).   TODO: add increased typechecking, make sure schemas
  * match one another.
  *
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
 * @version $Id: InsertRequest.java,v 1.2 2003/12/16 18:46:09 cholmesny Exp $
  */
 public class InsertRequest extends SubTransactionRequest {
     /** Class logger */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.requests.wfs");
 
     /** The list of features to be inserted. */
     private FeatureCollection features;
 
     /** flag to tell is all locked features should be released. */
     private boolean releaseAll;
 
     /** Specifies the table of features */
     protected String typeName = null;
 
     /**
      * Empty constructor.
      */
     public InsertRequest() {
         features = FeatureCollections.newCollection();
     }
 
     /**
      * Gets whether all locked features should be released after this
      * transaction, or only those that were affected.
      *
      * @return <tt>true</tt> if all locked features should be released.
      */
     public boolean getReleaseAll() {
         return releaseAll;
     }
 
     /**
      * Sets  whether all locked features should be released after this
      * transaction, or only those that were affected.
      *
      * @param releaseAll whether all locked features should be released.
      */
     public void setReleaseAll(boolean releaseAll) {
         this.releaseAll = releaseAll;
     }
 
     /**
      * Adds a feature to this insert request.  Currently fairly permissive,
      * just checks that the typenames match. The datasource will eventually
      * complain, but it would be nice to do some more type-checking, to make
      * sure the schemas match.
      *
      * @param feature To be inserted into the database.
      *
      * @throws WfsException if added typeName does not match the set typeNames.
      */
     public void addFeature(Feature feature) throws WfsException {
         if (typeName == null) {
             features.add(feature);
            setTypeName(feature.getFeatureType().getTypeName());
         } else {
            String addTypeName = feature.getFeatureType().getTypeName();

             if (typeName.equals(addTypeName)) {
                 features.add(feature);
             } else {
                 throw new WfsException(
                     "features do not match- added typeName: " + addTypeName
                     + ", set typeName: " + typeName, handle);
             }
         }
     }
 
     /**
      * Convenience method to add an array of features.  See addFeature.
      *
      * @param features array of features to be inserted.
      *
      * @throws WfsException if the typeNames don't match.
      */
     public void addFeatures(Feature[] features) throws WfsException {
         for (int i = 0; i < features.length; i++) {
             addFeature(features[i]);
         }
     }
 
     /**
      * Returns the type name of the features held in this request.
      *
      * @return the typeName.
      */
     public String getTypeName() {
         return typeName;
     }
 
     /**
      * Sets the name.  This method should generally not be used, as features
      * that are added set their own name and throw exceptions if they don't
      * match the typename.  But this can be set before adding features if you
      * want to ensure that they all match this name.
      *
      * @param typeName the name of the schema of the added features.
      */
     public void setTypeName(String typeName) {
         if ((this.typeName == null) || this.typeName.equals(typeName)) {
             this.typeName = typeName;
         } else {
             //throw exception?  do nothing?  We should not
             //be setting a different type name if the typeName of
             //the schemas is different.  To throw exception we must
             //modify SubTransactionRequest.
         }
     }
 
     /**
      * Returns the features contained in this request.
      *
      * @return the array of features.
      */
     public FeatureCollection getFeatures() {
         return features;
     }
 
     /**
      * Filters can not be added to an insert request.  This is just an override
      * of the setFilter method that throws an exception if called.
      *
      * @param filter a filter.
      *
      * @throws WfsException if called at all.
      */
     public void setFilter(Filter filter) throws WfsException {
         throw new WfsException("Attempted to add filter (" + filter
             + ") to update request: " + handle);
     }
 
     /**
      * Returns the insert short.
      *
      * @return the short representation of INSERT.
      */
     public short getOpType() {
         return INSERT;
     }
 
     /**
      * Gets the string representation of this request.
      *
      * @return A string of the handle, releaseAll status, typeName and features
      *         in this request.
      */
     public String toString() {
         StringBuffer iRequest = new StringBuffer("Handle: " + handle);
         iRequest.append("\nReleaseAll: " + releaseAll);
         iRequest.append("\nTypeName: " + typeName + "\n");
 
         Iterator featIter = features.iterator();
 
         while (featIter.hasNext()) {
             iRequest.append(featIter.next().toString() + "\n");
         }
 
         return iRequest.toString();
     }
 
     /**
      * Override of equals.
      *
      * @param obj the object to test for equality.
      *
      * @return <tt>true</tt> if obj is equal to this.
      *
      * @task REVISIT: I think the feature iteration of geotools still will not
      *       work, as it checks the featureID, but in an insert request the
      *       fid is ignored.
      */
     public boolean equals(Object obj) {
         if (!(obj instanceof InsertRequest)) {
             return false;
         }
 
         InsertRequest testInsert = (InsertRequest) obj;
         boolean isEqual = true;
 
         if (this.handle != null) {
             isEqual = this.handle.equals(testInsert.handle);
         } else {
             isEqual = (testInsert == null);
         }
 
         LOGGER.finest("handles are equal: " + isEqual);
         isEqual = (this.releaseAll == testInsert.releaseAll) && isEqual;
         LOGGER.finest("releaseAll equal: " + isEqual);
 
         if (this.features.size() == testInsert.getFeatures().size()) {
             //TODO: iterator through each collection.  THis will be
             //better when datasources return FeatureDocument.
             //for(int i = 0; i < testInsert.getFeatures().size(); i++) {
             //isEqual = isEqual && this.features.contains
             //(testInsert.getFeatures().get(i));
             //}
         } else {
             isEqual = false;
         }
 
         LOGGER.finest("features are equal " + isEqual);
 
         return isEqual;
     }
 }
