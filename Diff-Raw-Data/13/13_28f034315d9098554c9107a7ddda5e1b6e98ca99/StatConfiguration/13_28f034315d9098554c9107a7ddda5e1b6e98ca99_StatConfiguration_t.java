 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tehbeard.BeardStat;
 
 import me.tehbeard.utils.syringe.configInjector.InjectConfig;
 
 /**
  *
  * @author James
  */
 class StatConfiguration {
     
     @InjectConfig("general.debug")
     public boolean debugMode;
     
     @InjectConfig("general.verbose")
     public boolean verboseMode;
     
     @InjectConfig("stats.configversion")
     public int configVersion;
     
     @InjectConfig("stats.database.type")
     public String dbType;

    @Override
    public String toString() {
        return "StatConfiguration{" + "debugMode=" + debugMode + ", verboseMode=" + verboseMode + ", configVersion=" + configVersion + ", dbType=" + dbType + '}';
    }
    
     
 }
