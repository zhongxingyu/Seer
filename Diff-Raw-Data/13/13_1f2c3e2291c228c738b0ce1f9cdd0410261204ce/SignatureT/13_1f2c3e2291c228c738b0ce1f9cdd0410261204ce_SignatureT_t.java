 package org.thisamericandream.sgit.struct;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.sun.jna.Structure;
 
 public class SignatureT extends Structure {
   public String name;
   public String email;
  public TimeT.ByValue when;
   
   @Override
   public List<String> getFieldOrder() {
     return Arrays.asList("name", "email", "when");
   }
 }
