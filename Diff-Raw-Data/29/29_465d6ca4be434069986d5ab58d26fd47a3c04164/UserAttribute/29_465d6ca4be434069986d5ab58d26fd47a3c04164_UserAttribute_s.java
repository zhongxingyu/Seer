 package no.feide.moria.directory;
 
 import java.util.Vector;
 
 /**
  * ' Represents a user attribute and its list of values (if any).
  */
 public class UserAttribute {
 
     /** Internal representation of the attribute's values. */
     private Vector myValues;
 
 
     /**
      * Constructor.
      * @param values
      *            A list of attribute values. A <code>null</code> value is
      *            accepted, as is an empty array.
      */
    public UserAttribute(String[] values) {
 
         myValues = new Vector();
         if (values != null)
             for (int i = 0; i < values.length; i++)
                 myValues.add(values[i]);
 
     }
 
 
     /**
      * Get the attribute's values.
      * @return The attribute values, if any. May be an empty array.
      */
     public String[] getValues() {
 
         return (String[]) myValues.toArray(new String[] {});
 
     }
 
 }
