 package org.chai.kevin.value;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.JaqlService;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.form.FormValidationService.ValidatableLocator;
 import org.chai.kevin.location.DataLocation;
 
 public class ValidationService {
 	
 	private static final Log log = LogFactory.getLog(ValidationService.class);
 	
 	public JaqlService jaqlService;
 
 	public Set<String> getPrefixes(String expression, Set<String> prefixes, ValidatableValue validatable, DataLocation location, ValidatableLocator locator, Boolean evaluateTo) {
 		Set<String> result = new HashSet<String>();
 		Set<List<String>> combinations = new HashSet<List<String>>();
 		
 		List<String> toCombine = new ArrayList<String>();
 		toCombine.add(expression);
 		toCombine.addAll(prefixes);
 		
		validatable.getType().getCombinations(validatable.getValue(), toCombine, combinations, "", "");
 		
 		for (List<String> list : combinations) {
 			if (!isWildcard(list)) {
 				if (evaluateTo.equals(evaluate(list.get(0), location, locator))) result.addAll(list.subList(1, list.size()));
 			}
 		}
 		return result;
 	}
 	
 	public Boolean evaluate(String expression, DataLocation location, ValidatableLocator locator) {
 		if (log.isDebugEnabled()) log.debug("evaluate(expression="+expression+")");
 		
 		Map<String, Value> valueMap = new HashMap<String, Value>();
 		Map<String, Type> typeMap = new HashMap<String, Type>();
 		
 		Set<String> placeholders = ExpressionService.getVariables(expression);
 		for (String placeholder : placeholders) {
 			Long id = Long.parseLong(placeholder.replace("$", ""));
 			ValidatableValue value = locator.getValidatable(id, location);
 			if (value == null) log.error("expression "+expression+" refers to unknown survey element");
 			else {
 				valueMap.put(id.toString(), value.getValue());
 				typeMap.put(id.toString(), value.getType());
 			}
 		}
 		
 		Value value = null;
 		try {
 			value = jaqlService.evaluate(expression, Type.TYPE_BOOL(), valueMap, typeMap);
 		} catch (IllegalArgumentException e) {}
 		
 		Boolean result;
 		if (value == null) result = null;
 		else result = value.getBooleanValue();
 		
 		if (log.isDebugEnabled()) log.debug("evaluate(...)="+result);
 		return result;
 	}
 	
 	public static boolean isWildcard(String string) {
 		return string.contains("[_]");
 	}
 
 	public static boolean isWildcard(List<String> strings) {
 		for (String string : strings) {
 			if (isWildcard(string)) return true;
 		}
 		return false;
 	}
 	
 	public void setJaqlService(JaqlService jaqlService) {
 		this.jaqlService = jaqlService;
 	}
 
 }
