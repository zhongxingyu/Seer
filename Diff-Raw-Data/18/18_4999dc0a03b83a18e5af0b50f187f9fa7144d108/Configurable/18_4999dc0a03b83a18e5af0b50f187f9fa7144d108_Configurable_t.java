 
 package com.zygon.trade;
 
 /**
  *
  * @author zygon
  */
 public interface Configurable {
     public void configure(Configuration configuration);
    public ChildSchema getChildSchema();
     public Configuration getConfiguration();
     public Schema getSchema();
 }
