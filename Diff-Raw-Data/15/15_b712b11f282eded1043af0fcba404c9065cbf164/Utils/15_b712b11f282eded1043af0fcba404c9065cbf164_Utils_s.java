 package ar.com.jmfsg.documentation.support;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.springframework.util.StringUtils;
 
 import com.google.common.collect.ImmutableMap;
 
 import ar.com.jmfsg.documentation.model.Group;
 import ar.com.jmfsg.documentation.model.Method;
 import ar.com.jmfsg.documentation.model.RequestMethods;
 
 
 public class Utils {
 
 	private static Comparator<? super MethodHolder> methodComparator = new Comparator<MethodHolder>() {
 		public int compare(MethodHolder o1, MethodHolder o2) {
 			return (o1.data.order == o2.data.order) ?
 				obtainMethodName(o1.data).compareTo(obtainMethodName(o2.data)) 
 				: Math.abs(o1.data.order) < Math.abs(o2.data.order) ? -1 : 1;
 		}
 	};
 
 	@SuppressWarnings("unchecked")
 	public static void normalizeDocumentationData(
 			Map<String, List<Method>> documentation, Map<String, Group> groups,
 			Map<String, Map<String, Object>> documentationByGroup,
 			Map<String, Method> documentationByMethod) {
 		Map<String, Map<String, Object>> newDocumentationByGroup = new HashMap<String, Map<String,Object>>();
 		Map<String, Method> newDocumentationByMethod = new HashMap<String, Method>();
 		for(String module : documentation.keySet()) {
 			for(Method method : documentation.get(module)) {
 				processRequestMethodRetrocompatibility(method);
 				
 				if (!newDocumentationByGroup.containsKey(method.group)) {
 					HashMap<String, Object> hashMap = new HashMap<String, Object>();
                     hashMap.put("methods", new TreeSet<MethodHolder>(methodComparator));
                     hashMap.put("subGroups", new HashMap<String, Map<String, Object>>());
                     newDocumentationByGroup.put(method.group, hashMap);
                 }
 
 				String byMethodKey = new StringBuffer(module).append("-").append(method.friendlyName).toString().replace(" ", "-");
 				
 				if (!StringUtils.isEmpty(method.subGroup)) {
 					Map<String, Object> groupDoc = newDocumentationByGroup.get(method.group);
 					Map<String, Map<String, Object>> subGroupDoc = (Map<String, Map<String, Object>>) groupDoc.get("subGroups");
                     if (!subGroupDoc.containsKey(method.subGroup)) {
                     	HashMap<String, Object> hashMap = new HashMap<String, Object>();
                         hashMap.put("methods", new TreeSet<MethodHolder>(methodComparator));
                         subGroupDoc.put(method.subGroup, hashMap);
                     }
                     
                     Set<MethodHolder> methods = (Set<MethodHolder>) subGroupDoc.get(method.subGroup).get("methods");
                     
                     methods.add(new MethodHolder(method, ImmutableMap.of("internalMethodName", byMethodKey)));
                     
                 }
                 else {
                 	Map<String, Object> groupDoc = newDocumentationByGroup.get(method.group);
                 	Set<MethodHolder> methods = (Set<MethodHolder>) groupDoc.get("methods");
                 	
                 	methods.add(new MethodHolder(method, ImmutableMap.of("internalMethodName", byMethodKey)));
                 }
 						
 				newDocumentationByMethod.put(byMethodKey, method);
 			}
 		}
 		documentationByGroup.clear();
 		documentationByGroup.putAll(newDocumentationByGroup);
 		
 		documentationByMethod.clear();
 		documentationByMethod.putAll(newDocumentationByMethod);
 	}
 
 	/**
 	 * Replaces null or empty requestMappings per method for default requestMapping.
 	 * 
 	 * 
 	 * @param method
 	 */
 	private static void processRequestMethodRetrocompatibility(Method method) {
 		RequestMethods normalizedMethod = new RequestMethods();
		for(String key : method.method.keySet()) {
			if(StringUtils.isEmpty( method.method.get(key))) {
				normalizedMethod.put(key.toLowerCase(), method.requestMapping);
			} else {
				normalizedMethod.put(key.toLowerCase(), method.method.get(key));
 			}
 		}
 		method.method = normalizedMethod;
 	}
 	
 	public static String obtainMethodName(Method method) {
 		return (!StringUtils.isEmpty(method.shortName))? method.shortName : method.requestMapping;
 	}
 }
