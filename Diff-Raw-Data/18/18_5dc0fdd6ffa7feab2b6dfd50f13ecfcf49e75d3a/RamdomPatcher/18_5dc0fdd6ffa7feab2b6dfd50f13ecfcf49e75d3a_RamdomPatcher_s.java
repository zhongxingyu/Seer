 package org.ebayopensource.turmeric.monitoring.test;
 
 import com.google.gwt.user.client.Random;
 import com.octo.gwt.test.patchers.AutomaticPatcher;
 import com.octo.gwt.test.patchers.PatchClass;
 import com.octo.gwt.test.patchers.PatchMethod;
 
 @PatchClass(Random.class )
 public class RamdomPatcher extends AutomaticPatcher{
     @PatchMethod
     public static int nextInt(int upperBound) {
        return new Integer((int) Math.random());
     }
     
     @PatchMethod
     public static double nextDouble(){
         return Math.random();
     }
 }
