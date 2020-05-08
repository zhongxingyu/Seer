 package gov.nih.nci.iso21090;
 
 /**
  * Represents collection of ANY.
  * @author Dan Dumitru
 *
  * @param <T> Collection element type
  */
 @SuppressWarnings({ "PMD.AbstractNaming", "PMD.AbstractClassWithoutAnyMethod" })
 public abstract class Coll<T extends Any> extends Any implements Cloneable {
 
     private static final long serialVersionUID = 2L;
 
 }
