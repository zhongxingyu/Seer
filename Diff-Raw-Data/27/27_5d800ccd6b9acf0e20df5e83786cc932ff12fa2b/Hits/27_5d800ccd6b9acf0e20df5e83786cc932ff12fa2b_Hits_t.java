 
 package ualberta.g12.adventurecreator.online;
 
 import java.util.Collection;
 
 /**
  * A "Hit" is one result from an elastic search. There could be many hits like if we were to search
  * for certain objects with specific properties. 
  * @author Vincent
  *
  * @param <T>
  */
 public class Hits<T> {
     int total;
     double max_score;
     Collection<ElasticSearchResponse<T>> hits;
     
     /**
      * Obtains the hits of an object of a response
      */
     public Collection<ElasticSearchResponse<T>> getHits() {
         return hits;
     }
 
     public String toString() {
         return (super.toString() + "," + total + "," + max_score + "," + hits);
     }
 }
