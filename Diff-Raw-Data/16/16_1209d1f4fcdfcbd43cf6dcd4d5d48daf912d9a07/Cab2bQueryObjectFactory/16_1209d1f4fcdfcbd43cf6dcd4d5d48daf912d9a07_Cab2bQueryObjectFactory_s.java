 package edu.wustl.cab2b.common.queryengine;
 
 import edu.wustl.common.querysuite.factory.QueryObjectFactory;
 
 public class Cab2bQueryObjectFactory extends QueryObjectFactory {
     public static ICab2bQuery createCab2bQuery() {
        return new Cab2bQuery();
     }
 }
