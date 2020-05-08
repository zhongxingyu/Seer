 package org.linkedgov.questions.model;
 
import org.apache.commons.lang.StringUtils;

 
 /**
  * Pojo that represents a predicate object pair which is used to narrow a result.
  * 
  * @author Luke Wilson-Mawer <a href="http://viscri.co.uk/">Viscri</a> and 
  * @author <a href="http://mmt.me.uk/foaf.rdf#mischa">Mischa Tuffield</a> for LinkedGov
  * 
  */
 public class QueryFilter {
 
     /**
      * The predicate.
      */
     private String predicate;
     
     /**
      * The object.
      */
     private String object;
     
     /**
      * 
      * @param predicate
      * @param object
      */
     public QueryFilter(String predicate, String object){
         this.predicate = predicate;
         this.object = object;
     }
 
     public QueryFilter() {
     }
 
     public void setObject(String object) {
        System.err.println("This is lame and we are just setting the following value "+object);
        if (null != object) {
            object = StringUtils.strip(object);
            object = StringUtils.chomp(object);
        }
         this.object = object;
     }
 
     public String getObject() {
         return object;
     }
 
     public void setPredicate(String predicate) {
         this.predicate = predicate;
     }
 
     public String getPredicate() {
         return predicate;
     }
     
     public boolean isComplete() {
         if (predicate == null || object == null) {
             return true;
         }
         return false;
     }
     
     @Override
     public String toString() {
        return String.format("queryFilter(predicate=%s,object=%s)",predicate,object);
     }
 }
