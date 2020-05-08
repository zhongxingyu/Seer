 package org.thisamericandream.sgit.struct;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.sun.jna.Structure;
 
 public class OidT extends Structure {
  public char[] id = new char[20];
 
   @Override
   public List<String> getFieldOrder() {
     return Arrays.asList("id");
   }
 }
