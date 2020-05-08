 package br.com.bluesoft.commons;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class RequestUtil {
 
 	/**
 	 * Converte valores vazios(aspas simples) para valores nulos
 	 * @param parameterMap
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static Map convertEmptyValuesToNull(final Map parameterMap) {
 		final Map map = new LinkedHashMap();
 		final Set keys = parameterMap.keySet();

 		for (final Object key : keys) {
 			final Object value = parameterMap.get(key);
 			if (value instanceof String[]) {
 				final String[] values = (String[]) value;
 				for (final String strValue : values) {
 					if (strValue != null && strValue.equals("")) {
 						map.put(key, null);
 					} else {
 						map.put(key, value);
 					}
 				}
 			} else {
 				if (value != null && value.equals("")) {
 					map.put(key, null);
 				} else {
 					map.put(key, value);
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Obtem um parametro do request, atributo do request ou atributo da session, ou atributo do application;
 	 * @param request
 	 * @param name
 	 * @return
 	 */
 	public static Object getRequestObj(final HttpServletRequest request, final String name) {
 		Object value = null;
 		if (request.getParameter(name) != null) {
 			value = request.getParameter(name);
 		} else if (request.getAttribute(name) != null) {
 			value = request.getAttribute(name);
 		} else if (request.getSession().getAttribute(name) != null) {
 			value = request.getSession().getAttribute(name);
 		} else if (request.getSession().getServletContext().getAttribute(name) != null) {
 			value = request.getSession().getServletContext().getAttribute(name);
 		}
 		return value;
 	}
 
 	public static String getRequestStr(final HttpServletRequest request, final String name) {
 		final Object value = getRequestObj(request, name);
 		return value == null ? null : String.valueOf(value);
 	}
 }
