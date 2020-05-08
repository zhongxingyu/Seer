 package org.araqne.logparser.krsyslog.samsung;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.log.api.V1LogParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ExshieldCsvParser extends V1LogParser {
 	private final Logger slog = LoggerFactory.getLogger(ExshieldCsvParser.class);
 
 	private final String[] ADMITTED_FIELDS = new String[] { "priority", "e_time", "rule_id", "src_ip", "src_port", "dst_ip",
 			"dst_port", "protocol", "recv_byte", "send_byte", "duration", "s_time", "direction" };
 
 	// 0 string, 1 int, 2 long
 	private final int[] ADMITTED_TYPES = new int[] { 0, 0, 0, 0, 1, 0, 1, 0, 2, 2, 1, 0, 0 };
 
 	private final String[] DENIED_FIELDS = new String[] { "priority", "timestamp", "rule_id", "src_ip", "src_port", "dst_ip",
 			"dst_port", "protocol", "action", "sig_no", "deny_cnt", "direction" };
 
 	private final int[] DENIED_TYPES = new int[] { 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0 };
 
 	@Override
 	public Map<String, Object> parse(Map<String, Object> params) {
 		String line = null;
 		try {
 			line = (String) params.get("line");
 			if (line == null)
 				return params;
 
 			Map<String, Object> m = new HashMap<String, Object>();
 
 			int b = line.indexOf('[');
 			int e = line.indexOf(']', b);
 
 			if (b < 0 || e < 0)
 				return params;
 
 			String t = line.substring(b + 1, e);
 			if (t.equals("LOG_ADMITTED")) {
 				m.put("type", "admitted");
 				e++;
 
 				for (int i = 0; i < ADMITTED_FIELDS.length; i++) {
 					b = e + 1;
 					if (i == ADMITTED_FIELDS.length - 1)
 						e = line.indexOf('\n', b);
 					else
 						e = line.indexOf(',', b);
 
 					if (e < 0)
 						e = line.length();
 
 					String field = ADMITTED_FIELDS[i];
 					String value = line.substring(b, e);
 					switch (ADMITTED_TYPES[i]) {
 					case 0:
 						m.put(field, value);
 						break;
 					case 1:
						if (value.isEmpty() || value.equals("-"))
 							m.put(field, null);
 						else
 							m.put(field, Integer.valueOf(value));
 						break;
 					case 2:
						if (value.isEmpty() || value.equals("-"))
 							m.put(field, null);
 						else
 							m.put(field, Long.valueOf(value));
 						break;
 					}
 				}
 
 			} else if (t.equals("LOG_DENIED")) {
 				m.put("type", "denied");
 				e++;
 
 				for (int i = 0; i < DENIED_FIELDS.length; i++) {
 					b = e + 1;
 					if (i == DENIED_FIELDS.length - 1)
 						e = line.indexOf('\n', b);
 					else
 						e = line.indexOf(',', b);
 
 					if (e < 0)
 						e = line.length();
 
 					String field = DENIED_FIELDS[i];
 					String value = line.substring(b, e);
 					switch (DENIED_TYPES[i]) {
 					case 0:
 						m.put(field, value);
 						break;
 					case 1:
						if (value.isEmpty() || value.equals("-"))
 							m.put(field, null);
 						else
 							m.put(field, Integer.valueOf(value));
 						break;
 					case 2:
						if (value.isEmpty() || value.equals("-"))
 							m.put(field, null);
 						else
 							m.put(field, Long.valueOf(value));
 						break;
 					}
 				}
 			} else {
 				return params;
 			}
 
 			return m;
 		} catch (Throwable t) {
 			if (slog.isDebugEnabled())
 				slog.debug("araqne krsyslog parser: cannot parse exshield csv log - " + line, t);
 			return params;
 		}
 	}
 }
