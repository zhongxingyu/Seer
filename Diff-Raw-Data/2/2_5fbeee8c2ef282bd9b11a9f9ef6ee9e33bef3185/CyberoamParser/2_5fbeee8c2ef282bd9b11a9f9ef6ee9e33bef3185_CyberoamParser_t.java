 package org.araqne.logparser.krsyslog.cyberoam;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.log.api.V1LogParser;
 
 public class CyberoamParser extends V1LogParser {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(CyberoamParser.class);
 
 	@Override
 	public Map<String, Object> parse(Map<String, Object> params) {
 		String line = (String) params.get("line");
 		if (line == null)
 			return params;
 		try {
 
 			Map<String, Object> m = new HashMap<String, Object>();
 
 			// remove unused tag(e.g. <420>)
 			int b = 0;
 			int e = line.indexOf(">") + 1;
 			StringBuilder builder = new StringBuilder(line);
 			builder.delete(b, e);
 
 			while ((e = builder.indexOf("=", b)) > 0) {
 				String key = builder.substring(b, e);
 				if (key.equals("src_mac")) {
 					String value = builder.substring(e + 1, e + 18);
 					m.put(key, value);
					b = e + 19;
 				} else {
 					if (builder.charAt(e + 1) == '"') {
 						String value = "";
 						int i = e + 2;
 
 						while (true) {
 							char ch = builder.charAt(i);
 							try {
 								if (ch == '"' && builder.charAt(i + 1) == ' ') {
 									value = builder.substring(e + 2, i);
 									break;
 								}
 								i++;
 							} catch (IndexOutOfBoundsException ex) {
 								value = builder.substring(e + 2, i);
 								break;
 							}
 						}
 
 						m.put(key, value);
 						b = i + 2;
 					} else {
 						int endPos = builder.indexOf(" ", e + 1);
 						String value;
 						if (endPos == -1) {
 							value = builder.substring(e + 1);
 							m.put(key, value);
 							break;
 						} else {
 							value = builder.substring(e + 1, endPos);
 							m.put(key, value);
 							b = endPos + 1;
 						}
 					}
 				}
 			}
 
 			return m;
 		} catch (Throwable t) {
 			if (slog.isDebugEnabled())
 				slog.debug("araqne log api: cannot parse cyberoam format - line [{}]", line);
 			return params;
 		}
 	}
 }
