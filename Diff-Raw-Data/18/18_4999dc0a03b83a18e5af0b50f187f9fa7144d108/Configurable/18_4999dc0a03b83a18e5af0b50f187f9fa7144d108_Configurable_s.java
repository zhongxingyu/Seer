 
 package com.zygon.trade;
 
 /**
  *
  * @author zygon
  */
 public interface Configurable {
     public void configure(Configuration configuration);
    public Schema getChildSchema();
     public Configuration getConfiguration();
     public Schema getSchema();
 }
