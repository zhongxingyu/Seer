 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * Defines a describe feature type request.
  * 
  * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
  * @version $VERSION$
  */
 public class DescribeRequest 
     extends Request {
 
     /** Class logger */
     private static Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.requests");
 
     /** Flags whether or not all feature types were requested */
     protected boolean allRequested = true;
 
     /** Stores all feature types */
     protected List featureTypes = new ArrayList();
     
     
     /** Empty constructor. */
     public DescribeRequest() { super(); }
     
     /** Return request type. */
     public String getRequest() { return "DESCRIBEFATURETYPE"; }
     
     /** Return boolean for all requested types. */
     public boolean allRequested() { return this.allRequested; }    
 
     /** Set requested feature types. */
     public void setFeatureTypes(List featureTypes) {
         this.featureTypes = featureTypes;
         this.allRequested = false;
     }
     
    /** Adds a requested feature types to the list. */
     public void addFeatureType(String featureTypes) {
         this.featureTypes.add(featureTypes);
         this.allRequested = false;
     }
 
     /** Return requested feature types. */
     public List getFeatureTypes() { return this.featureTypes; }    
 
 
     /*************************************************************************
      * OVERRIDES OF toString AND equals METHODS.                             *
      *************************************************************************/
     public String toString() {
         String returnString = "DescribeFeatureType Request [feature types: ";
         Iterator i = featureTypes.listIterator();
 
         LOGGER.finest("all req: " + allRequested());
         if( this.allRequested()) {
             return returnString + " ALL ]";
         } else {
             while(i.hasNext()) {
                 returnString = returnString + i.next(); 
                 if(i.hasNext()) {
                     returnString = returnString + ", "; 
                 }
             }
             return returnString + "]";
         }
     }   
 
     public boolean equals(DescribeRequest request) {
	boolean isEqual = true;
         Iterator internal = featureTypes.listIterator();
         Iterator compare = request.getFeatureTypes().listIterator();
        if( request.allRequested()) {
	    isEqual = this.allRequested() && isEqual;
             return isEqual;
         } else {
             while( internal.hasNext()) {
                 if( compare.hasNext()) {
                     isEqual = internal.next().equals( compare.next()) && 
                         isEqual;
                } else {
		    internal.next();
		    isEqual = false;
		}
             }
             if( compare.hasNext()) {
		return false; 
             } else {
		return isEqual;
             }
         }
     }
 }
