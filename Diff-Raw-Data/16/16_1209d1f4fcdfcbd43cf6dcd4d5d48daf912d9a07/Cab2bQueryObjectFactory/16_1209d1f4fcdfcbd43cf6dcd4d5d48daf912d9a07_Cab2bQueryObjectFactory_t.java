 package edu.wustl.cab2b.common.queryengine;
 
import java.util.Date;

 import edu.wustl.common.querysuite.factory.QueryObjectFactory;
 
 public class Cab2bQueryObjectFactory extends QueryObjectFactory {
     public static ICab2bQuery createCab2bQuery() {
        ICab2bQuery query = new Cab2bQuery();
        query.setCreatedDate(new Date());
        return query;
     }
 }
