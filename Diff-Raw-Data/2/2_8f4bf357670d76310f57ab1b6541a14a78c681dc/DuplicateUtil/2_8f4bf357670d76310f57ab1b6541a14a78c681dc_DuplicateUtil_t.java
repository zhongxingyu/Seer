 /*
  * DuplicateUtil.java
  *
  * Created on October 19, 2005, 3:44 PM
  *
  */
 
 package gov.nih.nci.camod.util;
 
 import java.util.*;
 import org.apache.commons.beanutils.*;
 import org.apache.commons.logging.*;
 
 /**
  * DuplicateUtil - Bean Deep-Copy Utility
  * <p>
  * This class provides a utility method for performing a deep-copy (duplicate) of
  * domain bean objects.  Domain classes that implement the Duplicatable interface
  * will be deep-copied along with any sub-classes.
  * <p>
  * @author Marc Piparo
  * @version 1.1
  * @see Duplicatable
  */
 public class DuplicateUtil {
   
   private static Log log = LogFactory.getLog(DuplicateUtil.class);   
   
   /**
    * Performs deep-copy "Duplicate" of the input parameter source bean object.
    * Source Bean MUST implement Duplicatable interface.  
    * <p>
    * Only public accessible Bean properties will be copied. (i.e properties
    * with "public" set and get methods)
    * <p>
    * Associated property beans (including beans in a collection) will by default 
    * be copied by reference, unless they implement the Duplicatable interface 
    * as well, in which case they too will be deep-copied.
    * <p>  
    * @param src source bean object to be duplicated
    * @throws java.lang.Exception Exception
    * @return Object duplicate bean
    */    
   public static Object duplicateBean(Duplicatable src) throws Exception { 
     return duplicateBeanImpl(src, null, null, null, null);
   }  
   
   /**
    * Performs deep-copy "Duplicate" of the input parameter source bean object.
    * Source Bean MUST implement Duplicatable interface.  
    * <p>
    * Similar functionality to duplicateBean(Duplicatable src) method, however
    * accepts an additional collection parameter which defines properties 
    * that should NOT be copied.
    * <p>
    * excludedProperties parameter consists of a Collection of String objects
    * that should not be copied. String objects should use dot-notation to
    * identify properties.
    * <p>
    * example: to exclude zipcode from a model containing a person object
    * with an address object property bean during a duplicate of the person object.
    * <p>
    * excludedProperties.add("address.zipcode");   
    *   
    * @param src source bean object to be duplicated
    * @param excludedProperties collection of strings representing property names (in dot-notation) not be copied
    * @throws java.lang.Exception Exception
    * @return Object duplicate bean
    */    
   public static Object duplicateBean(Duplicatable src, Collection excludedProperties) throws Exception { 
     return duplicateBeanImpl(src, null, null, null, excludedProperties);
   }  
   
   private static Object duplicateBeanImpl(Object src, List srcHistory, List dupHistory, String path, Collection excludedProperties) throws Exception {    
     Object duplicate = null;    
     
     if (src != null) {
 
       try {
         // reset history collections on root duplicate
         if (srcHistory == null) {
           srcHistory = new ArrayList();
         }   
 
         if (dupHistory == null) {
           dupHistory = new ArrayList();
         }         
 
         // check if we've already duplicated this object
         if (!srcHistory.contains(src)) {
           // add this src object to the history
           srcHistory.add(src);       
 
           // instantiate a new instance of this class       
           // check for virtual enahancer classes (i.e. hibernate lazy loaders)
           Class duplicateClass = null;
           if (src.getClass().getName().indexOf("$$Enhancer") > -1) {
             duplicateClass = src.getClass().getSuperclass();                 
           } else {
             duplicateClass = src.getClass();                
           }
 
           duplicate = (Object) duplicateClass.newInstance(); 
 
           // add this new duplicate object to history
           dupHistory.add(duplicate);
 
           Map beanProps = PropertyUtils.describe(src);
           Iterator props = beanProps.entrySet().iterator();
           log.debug("***** DUPLICATE Deep-Copy of Class: "+duplicateClass.getName());               
 
           // loop thru bean properties
           while (props.hasNext()) {          
             Map.Entry entry = (Map.Entry) props.next();               
             Object propValue = entry.getValue();                    
 
             if (entry.getKey() != null) {                  
               String propName = entry.getKey().toString(); 
 
               // determine path name
               String pathName = "";
               if (path != null) {
                 pathName = path+".";
               }
               pathName += propName;                           
 
               // do no copy property if it is in the excluded list
               if (!(excludedProperties != null && excludedProperties.contains(pathName))) {                        
                 Class propertyType = PropertyUtils.getPropertyType(duplicate, propName); 
 
                 log.debug("** processing copy of property: "+pathName);  
 
                 // check if property is a collection
                 if (propValue instanceof java.util.Collection) {    
                   Collection collectionProperty = (Collection) propValue;
                   if (!collectionProperty.isEmpty()) {       
                     // get collection property -
                     // *note: bean class is responsible for instatiating collection on construction
                     Collection duplicateCollection = (Collection) PropertyUtils.getProperty(duplicate, propName);
                     if (duplicateCollection != null) {
                       // iterate thru collection, duplicate elements and add to collection
                       for (Iterator iter = collectionProperty.iterator(); iter.hasNext();) {                    
                         Object collectionEntry = iter.next();                          
                         duplicateCollection.add(duplicateProperty(collectionEntry, srcHistory, dupHistory, pathName, excludedProperties));                                      
                       }
                     }                             
                   }           
                 } else {
                   // set member property in duplicate object             
                   try {             
                     //log.debug("** copying property: "+pathName);  
                     BeanUtils.copyProperty(duplicate, propName, duplicateProperty(propValue, srcHistory, dupHistory, pathName, excludedProperties));                
                   } catch (Exception ex) {
                     // do nothing. skip and move on. property value may be null, or no set method found.           
                    log.debug("** property '"+propName+"' not copied.  Either no set method, or null.");
                   }
                 } // collection condition                
               }
             } // key=null check
           } // loop end
         } else {
           // this src object has already been duplicated, so return a reference
           // to the duplicate created earlier rather than re-duplicate        
           duplicate = dupHistory.get(srcHistory.indexOf(src));
           log.debug("** skipping - already duplicated: "+src.getClass().getName());          
         }
       } catch (Exception ex) {
         throw new Exception("Error during Bean Duplicate: "+ex); 
       } 
     } // src=null check
     
     return duplicate;                
   }
     
   private static Object duplicateProperty(Object obj, List srcHistory, List dupHistory, String path, Collection excludedProperties) throws Exception {        
     // if property implements Duplicatable, duplicate this object
     // otherwise return a reference
     if (obj instanceof Duplicatable) {       
       return duplicateBeanImpl(obj, srcHistory, dupHistory, path, excludedProperties);
     } else {      
       return obj;
     }                    
   }  
   
 }
 
