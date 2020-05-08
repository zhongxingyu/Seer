 package org.thisamericandream.sgit.struct;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.sun.jna.Structure;
 
 public class TimeT extends Structure {
  long time;
  int offset;
   
   @Override
   public List<String> getFieldOrder() {
     return Arrays.asList("time", "offset");
   }
 }
