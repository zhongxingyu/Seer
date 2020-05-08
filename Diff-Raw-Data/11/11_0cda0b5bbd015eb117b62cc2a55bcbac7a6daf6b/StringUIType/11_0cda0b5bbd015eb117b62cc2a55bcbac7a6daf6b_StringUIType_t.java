 /*
  * Created on Nov 11, 2005
  */
 package uk.org.ponder.rsf.uitype;
 
 public class StringUIType implements UIType {
   public static final StringUIType instance = new StringUIType();
  // This value is compared by reference equality in UITypes.java - this is not a bad constant (Findbugs)
  public String PLACEHOLDER = new String("");
 
   public Object getPlaceholder() {
     return PLACEHOLDER;
   }
   public String getName() {
     return "string";
   }
   public boolean valueUnchanged(Object oldvalue, Object newvalue) {
     return oldvalue.equals(newvalue);
   }
 
 }
