 package com.wiredmind.booleanengine.relations;
 
 import org.apache.commons.chain.Context;
 
 /**
  * Tests whether a property is contained within a specified list of values.
  */
 public class In extends AbstractBinaryRelation<Object, String> {
 
     public final static long serialVersionUID = 1L;
     private final String[] vals;
 
     public In(Object key, String val) {
         super(key, null);
        vals = val.split(",");
     }
 
     public In(Object key, String... vals) {
         super(key, null);
         this.vals = vals;
     }
 
     @Override
     public boolean execute(Context context) throws Exception {
         setPropertyValue(context, key);
 
         for (String s : vals) {
             if (comparePropertyTo(s) == 0) {
                 truthValue = true;
                 return CONTINUE_PROCESSING;
             }
         }
 
         return PROCESSING_COMPLETE;
     }
 
     @Override
     public boolean isTruthValue(Context context) throws Exception {
         execute(context);
         return truthValue;
     }
 }
