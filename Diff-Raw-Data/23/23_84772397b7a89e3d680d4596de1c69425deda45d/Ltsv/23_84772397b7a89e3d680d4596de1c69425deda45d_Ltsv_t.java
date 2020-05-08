 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.model;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Labeled Tab-separated Values (LTSV) http://ltsv.org/
  * 
  * License: Public Domain
  * 
  * @author ne_Sachirou
  */
 public class Ltsv implements Serializable {
	private static final long serialVersionUID = 1L;
 
 	private Map<String, String> record = new HashMap<>();
 
 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 * @return
 	 */
 	public Ltsv put(String key, Object value) {
 		record.put(key, value.toString());
 		return this;
 	}
 
 	/**
 	 * 
 	 * @param externalMap
 	 * @return
 	 */
 	public Ltsv append(Map<String, Object> externalMap) {
 		for (final Entry<String, Object> entry : externalMap.entrySet())
 			put(entry.getKey(), entry.getValue());
 		return this;
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public String toString() {
 		StringBuffer buffer = new StringBuffer();
 		for (final Entry<String, String> entry : record.entrySet()) {
 			final String key = entry.getKey()
 					.replaceAll("[^0-9A-Za-z_.-]", "_").trim();
 			final String value = entry.getValue().toString()
 					.replaceAll("[:\t\n\r]", " ").trim();
 			buffer.append(key + ":" + value + "\t");
 		}
 		return buffer.substring(0, buffer.length() - 1);
 	}
 }
