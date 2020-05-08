 package org.eweb4j.mvc;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.eweb4j.mvc.upload.UploadFile;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.ReflectUtil;
 
 /**
  * 如果是属性为pojo，例如： private Pet pet; 那么绑定参数的时候，优先找到 pet.name绑定到pet里面的name属性。
  * 如果找不到pet.name 参数，则找name参数绑定。如果还找不到，就不进行任何绑定。
  * 
  * 可以看到跟属性名字“pet”有关。支持深层次。例如： pet.master.name
  * 
  * @author weiwei
  * 
  */
 public class ParamUtil {
 
 	private static Object getLastPojo(Object parentPojo, String pojoParamName,
 			Hashtable<String, Object> hasPojo) throws InstantiationException,
 			IllegalAccessException, IllegalArgumentException,
 			InvocationTargetException {
 		if (parentPojo == null)
 			return null;
 
 		if (Map.class.isAssignableFrom(parentPojo.getClass())) {
 			return parentPojo;
 		}
 
 		ReflectUtil _ru = new ReflectUtil(parentPojo);
 
 		Method pojoSetter = _ru.getSetter(pojoParamName);
 		if (pojoSetter == null)
 			return parentPojo;
 
 		Class<?> pojoClass = pojoSetter.getParameterTypes()[0];
 
 		Object subPojo = hasPojo.get(pojoParamName);
 		if (subPojo == null) {
 			if (Map.class.isAssignableFrom(pojoClass)) {
 				subPojo = new HashMap<String, Object>();
 			} else {
 				subPojo = pojoClass.newInstance();
 			}
 			hasPojo.put(pojoParamName, subPojo);
 		}
 
 		pojoSetter.invoke(parentPojo, subPojo);
 
 		return subPojo;
 
 	}
 
 	private static void invokeSetter(Object invokeObj, String[] paramValue,
 			Method setter) throws IllegalAccessException,
 			InvocationTargetException {
 
 		Class<?> paramClass = setter.getParameterTypes()[0];
 
 		if (paramValue.length > 1 || paramClass.isArray()) {
 			Object obj = ClassUtil.getParamVals(paramClass, paramValue);
 			if (obj != null)
 				setter.invoke(invokeObj, new Object[] { obj });
 		} else {
 			Object obj = ClassUtil.getParamVal(paramClass, paramValue[0]);
 			if (obj != null)
 				setter.invoke(invokeObj, obj);
 		}
 
 	}
 
 	public static void injectParam(Context context, ReflectUtil ru, String startName) throws Exception {
 		
 		Hashtable<String, Object> hasPojo = new Hashtable<String, Object>();
 		Map<String, String[]> paramMap = context.getQueryParamMap();
 		
 		paramForeach: for (Entry<String, String[]> entry : paramMap.entrySet()) {
 			String paramName = entry.getKey();
 			String[] paramValue = entry.getValue();
 
 			if (paramValue == null || paramValue.length == 0)
 				continue;
 			
 			Method setter = null;
 			
 			String[] pojoParamNames = paramName.split("\\.");
 			if (pojoParamNames.length > 1) {
 				Object lastPojo = ru.getObject();
 				
 				if (startName != null && startName.trim().length() > 0) {
 					if (!pojoParamNames[0].equals(startName))
 						continue;
 				}
 
 				int lastIndex = pojoParamNames.length - 1;
 				for (int i = 0; i < lastIndex; i++) {
 					if (startName != null && startName.trim().length() > 0) {
 						if ((i+1) == lastIndex)
 							break;
 						
 						lastPojo = getLastPojo(lastPojo, pojoParamNames[i + 1], hasPojo);
 					} else {
 						lastPojo = getLastPojo(lastPojo, pojoParamNames[i], hasPojo);
 					}
 					
 					if (lastPojo == null)
 						continue paramForeach;
 				}
 
 				String _paramName = pojoParamNames[lastIndex];
 
 				if (Map.class.isAssignableFrom(lastPojo.getClass())) {
 					Map<String, Object> map = (HashMap<String, Object>) lastPojo;
 					if (paramValue.length <= 1)
 						map.put(_paramName, paramValue[0]);
 					else
 						map.put(_paramName, paramValue);
 
 					lastPojo = map;
 				}
 
 				ReflectUtil lpRu = new ReflectUtil(lastPojo);
 
 				setter = lpRu.getSetter(_paramName);
 				if (setter == null)
 					continue;
 
 				invokeSetter(lastPojo, paramValue, setter);
 
 			} else {
 				setter = ru.getSetter(paramName);
 				if (setter == null)
 					continue;
 
 				invokeSetter(ru.getObject(), paramValue, setter);
 			}
 		}
 		
 		injectFile(context, ru ,startName);
 	}
 	
 	public static void injectFile(Context context, ReflectUtil ru, String startName) throws Exception{
 		Hashtable<String, Object> hasPojo = new Hashtable<String, Object>();
 		paramForeach: for (Entry<String, List<UploadFile>> en : context.getUploadMap().entrySet()){
 			String paramName = en.getKey();
 			List<UploadFile> paramValue = en.getValue();
 			
 			if (paramValue == null || paramValue.size() == 0)
 				continue;
 			
 			Method setter = null;
 			
 			String[] pojoParamNames = paramName.split("\\.");
 			if (pojoParamNames.length > 1) {
 				Object lastPojo = ru.getObject();
 				
 				if (startName != null && startName.trim().length() > 0) {
 					if (!pojoParamNames[0].equals(startName))
 						continue;
 				}
 	
 				int lastIndex = pojoParamNames.length - 1;
 				
 				for (int i = 0; i < lastIndex; i++) {
 					if (startName != null && startName.trim().length() > 0) {
 						if ((i+1) == lastIndex)
 							break;
 						
 						lastPojo = getLastPojo(lastPojo, pojoParamNames[i + 1], hasPojo);
 					} else {
 						lastPojo = getLastPojo(lastPojo, pojoParamNames[i], hasPojo);
 					}
 					
 					if (lastPojo == null)
 						continue paramForeach;
 				}
 	
 				String _paramName = pojoParamNames[lastIndex];
 	
 				if (Map.class.isAssignableFrom(lastPojo.getClass())) {
 					Map<String, Object> map = (HashMap<String, Object>) lastPojo;
 					if (paramValue.size() <= 1)
 						map.put(_paramName, paramValue.get(0));
 					else
 						map.put(_paramName, paramValue);
 	
 					lastPojo = map;
 				}
 	
 				ReflectUtil lpRu = new ReflectUtil(lastPojo);
 	
 				setter = lpRu.getSetter(_paramName);
 				if (setter == null)
 					continue;
 				
 				Class<?> clazz = setter.getParameterTypes()[0];
 				if (File.class.isAssignableFrom(clazz)){
 					setter.invoke(lastPojo, paramValue.get(0).getTmpFile());
 				}else if (File[].class.isAssignableFrom(clazz)){
 					File[] files = new File[paramValue.size()];
 					for (int j = 0; j < files.length; j++)
 						files[j] = paramValue.get(j).getTmpFile();
 					
 					setter.invoke(lastPojo, new Object[]{files});
 				}if (UploadFile.class.isAssignableFrom(clazz)){
 					setter.invoke(lastPojo, paramValue.get(0));
 				}else if (UploadFile[].class.isAssignableFrom(clazz)){
 					UploadFile[] files = new UploadFile[paramValue.size()];
 					for (int j = 0; j < files.length; j++)
 						files[j] = paramValue.get(j);
 					
 					setter.invoke(lastPojo, new Object[]{files});
 				}
 				
 			} else {
 				setter = ru.getSetter(paramName);
 				if (setter == null)
 					continue;
 	
 				Class<?> clazz = setter.getParameterTypes()[0];
 				if (File.class.isAssignableFrom(clazz)){
 					setter.invoke(ru.getObject(), paramValue.get(0).getTmpFile());
 				}else if (File[].class.isAssignableFrom(clazz)){
 					File[] files = new File[paramValue.size()];
 					for (int j = 0; j < files.length; j++)
 						files[j] = paramValue.get(j).getTmpFile();
 					
 					setter.invoke(ru.getObject(), new Object[]{files});
 				}if (UploadFile.class.isAssignableFrom(clazz)){
 					setter.invoke(ru.getObject(), paramValue.get(0));
 				}else if (UploadFile[].class.isAssignableFrom(clazz)){
 					UploadFile[] files = new UploadFile[paramValue.size()];
 					for (int j = 0; j < files.length; j++)
 						files[j] = paramValue.get(j);
 					
 					setter.invoke(ru.getObject(), new Object[]{files});
 				}
 			}
 		}
 	}
 
 	// 将request的请求参数转到另外一个map中去
	public static Map<String, String[]> copyReqParams(HttpServletRequest req)
			throws UnsupportedEncodingException {
 		Map<String, String[]> map = new HashMap<String, String[]>();
 		for (@SuppressWarnings("unchecked")
		Iterator<Entry<String, String[]>> it = req.getParameterMap().entrySet()
				.iterator(); it.hasNext();) {
 			Entry<String, String[]> e = it.next();
 			String key = URLDecoder.decode(e.getKey(), "utf-8");
 			String[] val = e.getValue();
 			String[] values = new String[val.length];
 			for (int i = 0; i < values.length; i++) {
 				values[i] = URLDecoder.decode(val[i], "utf-8");
 			}
 			map.put(key, values);
 		}
 		return map;
 	}
 
 	public static Map<String, String[]> getPathParamMap(
 			Map<String, List<?>> urlParams) {
 
 		Map<String, String[]> map = new HashMap<String, String[]>();
 
 		List<String> urlParamNames = (List<String>) urlParams
 				.get("urlParamNames");
 
 		List<String> urlParamValues = (List<String>) urlParams
 				.get("urlParamValues");
 
 		if (urlParams == null || urlParamNames == null
 				|| urlParamNames.size() == 0 || urlParamValues == null
 				|| urlParamValues.size() == 0)
 			return map;
 
 		for (int i = 0; i < urlParams.get("urlParamNames").size(); i++) {
 			String name = urlParamNames.get(i).substring(1);
 			int lastIndex = name.length() - 1;
 			if (lastIndex <= 0)
 				continue;
 
 			String subName = name.substring(0, lastIndex);
 			map.put(subName, new String[] { urlParamValues.get(i) });
 		}
 
 		return map;
 	}
 
 }
