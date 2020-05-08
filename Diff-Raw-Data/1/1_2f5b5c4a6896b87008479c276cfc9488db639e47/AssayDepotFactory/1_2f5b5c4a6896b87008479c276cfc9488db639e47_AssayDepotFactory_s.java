 package com.assaydepot;
 
 import com.assaydepot.conf.Configuration;
 
 public class AssayDepotFactory {
   private final Configuration conf;  
   /**
    * Creates a AssayDepotFactory with the given configuration.
    *
    * @param conf the configuration to use
   * @since Twitter4J 2.1.1
    */
   public AssayDepotFactory(Configuration conf) {
       if (conf == null) {
           throw new NullPointerException("configuration cannot be null");
       }
       this.conf = conf;
   }
 }
