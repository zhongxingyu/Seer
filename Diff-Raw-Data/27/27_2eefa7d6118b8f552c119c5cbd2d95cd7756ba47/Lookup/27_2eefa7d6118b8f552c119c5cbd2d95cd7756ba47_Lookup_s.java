 /**
  * Copyright 2013 AppDynamics
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 
package com.appdynamics.lookup;
 
 import java.util.HashMap;
 
 /**
  * Creates a lookup or each attribute of SNMP and assigns an OID value
  */
 public class Lookup
 {
 	private static String baseOID = "1.3.6.1.4.1.40684.1.1.1.1.";
 	private static HashMap<String, String> map = new HashMap<String, String>();
 	private static String[] names = {
 		"application",
 		"triggeredBy",
 		"nodes",
 		"BTs",
 		"machines",
 		"tiers",
 		"eventTime",
 		"severity",
 		"type",
 		"subtype",
 		"summary",
 		"link",
 		"tag"
 	};
 
 	public Lookup ()
 	{
 		if(map.size() == 0)
 		{
 			int idx = 1;
 			
 			for (String name : names)
 			{
 				map.put(name, baseOID + idx++);
 			}
 		}
 	}
 
 	public String getOID(String name)
 	{
 		String val = map.get(name);
 		return val;
 	}
 }
 
