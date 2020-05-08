 /*******************************************************************************
  *  Copyright 2013 Jason Sipula                                                *
  *                                                                             *
  *  Licensed under the Apache License, Version 2.0 (the "License");            *
  *  you may not use this file except in compliance with the License.           *
  *  You may obtain a copy of the License at                                    *
  *                                                                             *
  *      http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                             *
  *  Unless required by applicable law or agreed to in writing, software        *
  *  distributed under the License is distributed on an "AS IS" BASIS,          *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
  *  See the License for the specific language governing permissions and        *
  *  limitations under the License.                                             *
  *******************************************************************************/
 
 package com.vanomaly.superd;
 
 import net.snakedoc.jutils.ConfigException;
 
 public enum Config {
     
     SUPERD("props/default.properties"),
     DATABASE("props/database.properties"),
     LANGUAGE("props/language.properties"),
     PREFS("props/prefs.properties"),
     ;
     
     Config(String configFile) {
         INSTANCE = new net.snakedoc.jutils.Config(configFile);
     }
     
     private final net.snakedoc.jutils.Config INSTANCE;
     
    public String getString(String key) throws ConfigException {
        return INSTANCE.getConfig(key);
     }
     
    public Integer getInteger(String key) throws ConfigException {
         try {
             return Integer.valueOf(INSTANCE.getConfig(key));
        } catch (NumberFormatException e) {
             // fix your config
         }
        return new Integer(null);
     }
     
     /*
     // singleton pattern (Initialization on Demand Holder idiom)
 
     private Config() {}
     
     private static class Configuration {
         
         private static final net.snakedoc.jutils.Config INSTANCE = 
                     new net.snakedoc.jutils.Config("props/default.properties");
         
     }
     
     public static net.snakedoc.jutils.Config getInstance() {
         
         return Configuration.INSTANCE;
         
     }
     */
     
     // singleton pattern (double-check idiom)
 /*
     private static volatile net.snakedoc.jutils.Config instance;
     
     private Config() {} // singleton
     
     public static net.snakedoc.jutils.Config getInstance() {
         
         if (Config.instance == null) {
             
             synchronized (Config.class) {
                 
                 if (Config.instance == null) {
                     
                     Config.instance = new net.snakedoc.jutils.Config("props/default.properties");
                     
                 }
                 
             }
             
         }
         
         return Config.instance;
         
     }
 */
 }
