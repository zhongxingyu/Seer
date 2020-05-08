 package com.gentics.cr.util;
 
 import java.util.HashMap;
 
 import com.gentics.cr.CRResolvableBean;
 
 /**
  * This Class only exists until an automatic mapping in the tagmap will be created.
  * Last changed: $Date: 2009-07-10 17:56:53 +0200 (Fr, 10 Jul 2009) $
  * @version $Revision: 142 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class MLMapper {
 
 	private static HashMap<Integer, String> ml_map;
 
 	/**
 	 * Get the Mimetype using an CRResolvableBean.
 	 * @param bean 
 	 */
 	public static String getMimetype(CRResolvableBean bean) {
 		String mimetype = null;
 		if (bean != null) {
			Integer ml_id = bean.getInteger("ml_id", 0);
 			mimetype = getMimetype(ml_id);
 		}
 		return mimetype;
 	}
 
 	/**
 	 * Get the Mimetype using an ml_id.
 	 * @param ml_id
 	 */
 	public static String getMimetype(Integer ml_id) {
 		if (ml_map == null) {
 			ml_map = createMLMap();
 		}
 
 		return ml_map.get(ml_id);
 	}
 
 	private static HashMap<Integer, String> createMLMap() {
 		HashMap<Integer, String> map = new HashMap<Integer, String>();
 		map.put(1, "text/html");
 		map.put(2, "text/html");
 		map.put(3, "text/html");
 		map.put(4, "text/html");
 		map.put(5, "text/html");
 		map.put(6, "text/html");
 		map.put(7, "text/html");
 		map.put(8, "text/xml");
 		map.put(9, "text/css ");
 		map.put(10, "text/js");
 		map.put(11, "text/xml");
 		map.put(12, "text/xml");
 		map.put(13, "text/plain");
 		map.put(14, "text/html");
 
 		return map;
 	}
 }
