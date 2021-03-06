 /*
  *                    BioJava development code
  *
  * This code may be freely distributed and modified under the
  * terms of the GNU Lesser General Public Licence.  This should
  * be distributed with the code.  If you do not have a copy,
  * see:
  *
  *      http://www.gnu.org/copyleft/lesser.html
  *
  * Copyright for this code is held jointly by the individual
  * authors.  These should be listed in @author doc comments.
  *
  * For more information on the BioJava project and its aims,
  * or to join the biojava-l mailing list, visit the home page
  * at:
  *
  *      http://www.biojava.org/
  *
  */
 
 
 package org.biojava.bio;
 
 import java.util.*;
 import java.io.*;
 import java.lang.reflect.*;
 
 import org.biojava.utils.*;
 
 /**
  * Arbitrary annotation associated with one or more objects.
  * <P>
  * Biological information often does not fit design patterns very well, and can
  * be a jumble of facts and relationships. Annotation objects provide a standard
  * way for you to store this mess as a property of an object.
  * <P>
  * Annotations may contain keys that have Annotations as values. In this way,
  * annotations can be shared among multiple Annotatable objects, and you can
  * represent semi-structured data.
  * <P>
  * It is perfectly possible to wrap up almost any tree-like or flat data
  * structure as Annotation.
  *
  * @author Matthew Pocock
  */
 public interface Annotation extends Changeable {
   /**
    * This type symbolizes that one or more properties have changed.
    */
   public static final ChangeType PROPERTY = new ChangeType(
     "Properties have altered",
     "org.biojava.bio.Annotation",
     "PROPERTY"
   );
   
   /**
    * Retrieve the value of a property by key.
    * <P>
    * Unlike the Map collections, it will complain if the key does not exist. It
    * will only return null if the key is defined and has value null.
    *
    * @param key  the key of the property to retrieve
    * @return  the object associated with that key
    * @throws NoSuchElementException if there is no property with the key
    */
   Object getProperty(Object key) throws NoSuchElementException;
   
   /**
    * Set the value of a property.
    * <P>
    * This method throws an exception if either properties can not be
    * added to this object, or that this particular property is immutable or
    * illegal within the implementation.
    *
    * @param key the key object
    * @param value the new value for this key
    * @throws IllegalArgumentException if the property <code>key</code> is not
    *         legal
    * @throws ChangeVetoException if this annotation object can't be changed, or
    *         if the change was vetoed
    */
   void setProperty(Object key, Object value)
   throws IllegalArgumentException, ChangeVetoException;
   
   /**
   * Returns wether there is any property under that key in this Annotation.
    *
    * @param key the key Object to search for
    * @return true if this Annotation knows about the key, false otherwise
    */
   boolean containsProperty(Object key);
   
   /**
    * Get a set of key objects.
    *
    * @return  a Set of key objects
    */
   Set keys();
   
   /**
    * Retern a map that contains the same key/values as this Annotation.
    * <P>
    * If the annotation changes, the map may not reflect this.  The Map
    * may be unmodifiable.
    *
    * @return a Map
    */
   Map asMap();
    
   /**
    * A really useful empty and immutable annotation object.
    * <P>
    * Use this instead of null when you really don't want an object or an
    * implementation to have annotation even though it should implement
    * Annotatable.
    */
   static final Annotation EMPTY_ANNOTATION = new EmptyAnnotation();
   
   /**
    * The empty and immutable implementation.
    */
   class EmptyAnnotation implements Annotation, Serializable {
     public Object getProperty(Object key) throws NoSuchElementException {
       throw new NoSuchElementException(
         "There are no keys in the Empty Annotation object: " +
         key
       );
     }
     
     public void setProperty(Object key, Object value)
     throws ChangeVetoException {
       throw new ChangeVetoException(
         "You can not add properties to the Empty Annotation object: " +
         key + " -> " + value
       );
     }
     
     public boolean containsProperty(Object key) {
       return false;
     }
     
     public Set keys() {
       return Collections.EMPTY_SET;
     }
     
     public Map asMap() {
       //return Collections.EMPTY_MAP; 1.3
       return new HashMap();
     }
     
     public void addChangeListener(ChangeListener cl) {}
     public void addChangeListener(ChangeListener cl, ChangeType ct) {}
     public void removeChangeListener(ChangeListener cl) {}
     public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
     
     private Object writeReplace() throws ObjectStreamException {
       try {
         return new StaticMemberPlaceHolder(Annotation.class.getField("EMPTY_ANNOTATION"));
       } catch (NoSuchFieldException nsfe) {
         throw new NotSerializableException(nsfe.getMessage());
       }
     }
   }
 }
