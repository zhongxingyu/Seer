 
 package ualberta.g12.adventurecreator.online;
 
 /**
  * This class represents the object that is obtained from an Elastic Search Request 
  * It provides information like Id, index and type of an object that was uploaded into
  * the server. It is the expected type we get from a gson to json translation.
  * This class has two methods in which we can call to obtain information from.
  * Get Source gets the whole object, while get fields only gets the fields in a query for fields.
  * This code is modified originally from : https://github.com/rayzhangcl/ESDemo
  */
 
 public class ElasticSearchResponse<T> {
     String _index; 
     String _type;
     String _id;
     int _version;
     boolean exists;
     T _source;
     T fields;
     double max_score;
 
     /**
      * This method is used to get the the whole source object obtained from the elastic search response
      * This method should be used to get the object when the elastic search obtains the whole
      * source (object) not fields. 
     * @return
      */
     public T getSource() {
         return _source;
     }
     
     /**
      * This method is used to obtain only the fields of the returned object from the elastic search.
      * For example if we were to search only for the id, author and title fields in a story stoed as 
      * a json object in our webserver. If we were to use elastic search to find only the those fields
      * this method should be used to obtain the results and put into a (partial) object.
     * @return
      */
     public T getFields() {
         return fields;
     }
 }
