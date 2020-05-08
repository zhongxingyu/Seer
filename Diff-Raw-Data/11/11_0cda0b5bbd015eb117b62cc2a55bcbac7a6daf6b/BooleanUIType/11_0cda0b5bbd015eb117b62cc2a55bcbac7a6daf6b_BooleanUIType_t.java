 /*
  * Created on Nov 11, 2005
  */
 package uk.org.ponder.rsf.uitype;
 
 public class BooleanUIType implements UIType {
   public static final BooleanUIType instance = new BooleanUIType();
  // This value is compared by reference equality in UITypes.java - this is not a bad constant (Findbugs)
  public Boolean PLACEHOLDER = new Boolean(false);
 
   public Object getPlaceholder() {
     return PLACEHOLDER;
   }
   public String getName() {
     return "boolean";
   }
   public boolean valueUnchanged(Object oldvalue, Object newvalue) {
     return oldvalue.equals(newvalue);
   }
 }
