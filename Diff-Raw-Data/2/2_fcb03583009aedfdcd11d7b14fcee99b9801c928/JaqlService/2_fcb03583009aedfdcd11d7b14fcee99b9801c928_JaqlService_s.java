 package org.chai.kevin;
 
 import grails.plugin.springcache.annotations.Cacheable;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.value.Value;
 
 import com.ibm.jaql.json.type.JsonValue;
 import com.ibm.jaql.lang.JaqlQuery;
 
 public class JaqlService {
 
 	private static final Log log = LogFactory.getLog(JaqlService.class);
 	
 	public JaqlService jaqlService;
 	
 	public static JsonValue jsonValue(String expression, Map<String, String> variables) {
 		Map<String, JsonValue> valueMap = new HashMap<String, JsonValue>();
 		for (Entry<String, String> variable : variables.entrySet()) {
 			JsonValue variableValue = jsonValue(variable.getValue(), new HashMap<String, String>());
 			valueMap.put(variable.getKey(), variableValue);
 		}
 
 		return executeQuery(expression, valueMap);
 	}
 
	@Cacheable("jsonValueCache")
 	public JsonValue getJsonValue(String expression, Map<String, String> variables) {
 		Map<String, JsonValue> valueMap = new HashMap<String, JsonValue>();
 		for (Entry<String, String> variable : variables.entrySet()) {
 			JsonValue variableValue = getMe().getJsonValue(variable.getValue(), new HashMap<String, String>());
 			valueMap.put(variable.getKey(), variableValue);
 		}
 
 		return executeQuery(expression, valueMap);
 	}
 	
 	/**
 	 * Return null if the expression is not correctly typed or returns null
 	 * 
 	 * @throws {@link IllegalArgumentException} if one of the arguments is null
 	 */
 	public Value evaluate(String expression, Type type, Map<String, Value> variables, Map<String, Type> types) throws IllegalArgumentException {
 		if (log.isDebugEnabled()) log.debug("evaluate(expression="+expression+", variables="+variables+")");
 		
 		Map<String, String> jaqlVariables = new HashMap<String, String>();
 		for (Entry<String, Value> variable : variables.entrySet()) {
 			// value can be null
 			if (variable.getValue() != null) {
 				String jaqlValue = types.get(variable.getKey()).getJaqlValue(variable.getValue());
 				if (jaqlValue.equals("null")) jaqlValue = "\"null\"";
 				jaqlVariables.put("$"+variable.getKey(), jaqlValue);
 			}
 		}
 		
 		JsonValue jsonValue = getMe().getJsonValue(expression, jaqlVariables);
 		if (jsonValue == null) return Value.NULL_INSTANCE();
 		return type.getValueFromJaql(jsonValue.toString());
 	}
 	
 	private static JsonValue executeQuery(String expression, Map<String, JsonValue> valueMap) throws IllegalArgumentException {
 		if (log.isDebugEnabled()) log.debug("executeQuery(expression="+expression+", valueMap="+valueMap+")");	
 		
 		JsonValue value = null;
 		JaqlQuery query = new JaqlQuery();
 		
 		if (expression.contains("roundup")) {
 			try {
 				query.registerJavaUDF("roundup", RoundUp.class);
 			} catch (Exception e) {
 				log.error("could not load roundup method", e);
 			}
 		}
 		query.setQueryString(expression.replaceAll("\\s", " "));
 		
 		for (Entry<String, JsonValue> entry : valueMap.entrySet()) {
 			query.setVar(entry.getKey(), entry.getValue());
 		}
 		try {
 			value = query.evaluate();
 		} catch (Exception e) {
 			log.warn("error evaluating: "+expression, e);
 			throw new IllegalArgumentException("error evaluating: "+expression, e);
 		} finally {
 			try {query.close();} catch (IOException e) {}
 		}
 		
 		if (log.isDebugEnabled()) log.debug("executeQuery(...)="+value);
 		return value;
 	}
 	
 	public void setJaqlService(JaqlService jaqlService) {
 		this.jaqlService = jaqlService;
 	}
 	
 	// for internal call through transactional proxy
 	public JaqlService getMe() {
 		return jaqlService;
 	}
 }
