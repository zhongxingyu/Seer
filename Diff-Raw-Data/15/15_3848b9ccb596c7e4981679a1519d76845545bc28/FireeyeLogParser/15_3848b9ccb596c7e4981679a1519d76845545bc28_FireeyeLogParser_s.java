 /**
  * Copyright 2014 Eediom Inc.
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
 package org.araqne.logparser.syslog.fireeye;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.log.api.V1LogParser;
 
 public class FireeyeLogParser extends V1LogParser {
 
 	@Override
 	public Map<String, Object> parse(Map<String, Object> params) {
 		try {
			String line = (String) params.get("line");
 			if (line == null)
 				return params;
 
 			Map<String, Object> m = new HashMap<String, Object>();
 
 			int b = 0;
 			int e = line.indexOf('|');
 			String s0 = line.substring(b, e);
 
 			b = e + 1;
 			e = line.indexOf('|', b);
 			String s1 = line.substring(b, e);
 
 			b = e + 1;
 			e = line.indexOf('|', b);
 			String s2 = line.substring(b, e);
 
 			b = e + 1;
 			e = line.indexOf('|', b);
 			String s3 = line.substring(b, e);
 			m.put("ver", s3);
 
 			b = e + 1;
 			e = line.indexOf('|', b);
 			String s4 = line.substring(b, e);
 			m.put("module", s4);
 
 			String detail = line.substring(e + 1);
 
 			b = 0;
 			while (true) {
 				e = detail.indexOf('^', b);
 				if (e < 0)
 					break;
 
 				String pair = detail.substring(b, e);
 				int d = pair.indexOf('=');
 				if (d == -1) {
 					m.put(pair, null);
 				} else {
 					String key = pair.substring(0, d);
 					String val = pair.substring(d + 1);
 					Object value = val;
 					if (key.endsWith("Port"))
 						value = Integer.parseInt(val);
 
 					m.put(key, value);
 				}
 
 				b = e + 1;
 			}
 
 			return m;
 		} catch (Throwable t) {
			t.printStackTrace();
 			return params;
 		}
 	}

 }
