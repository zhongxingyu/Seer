 package org.thisamericandream.sgit.struct;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.sun.jna.Structure;
 
 public class TimeT extends Structure {
  public long time;
  public int offset;
  
  public static class ByValue extends TimeT implements Structure.ByValue {};
   
   @Override
   public List<String> getFieldOrder() {
     return Arrays.asList("time", "offset");
   }
 }
