 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 
 public class WFConfig extends Object {
     /*
      * Object to store WifiConfigurations and level
      * 
      * Why isn't this an extension of WifiConfiguration? Because WFConfig is
      * only downclassed. More efficient than copying all fields individually.
      */
 
     public WifiConfiguration wificonfig;
     public int level;
 
     public WFConfig(final ScanResult sResult, final WifiConfiguration wConfig) {
 	/*
 	 * Constructor for only use of object so far
 	 */
 	level = sResult.level;
 	wificonfig = wConfig;
 	wificonfig.BSSID = sResult.BSSID;
     }
 
     @Override
     public String toString() {
 	StringBuilder result = new StringBuilder();
 	String NEW_LINE = System.getProperty("line.separator");
 
 	result.append(this.getClass().getName() + " Object {" + NEW_LINE);
 	result.append("WifiConfiguration:" + wificonfig.toString() + NEW_LINE);
 	result.append("Level: " + level);
 	result.append("}");
 
 	return result.toString();
     }
 
 }
