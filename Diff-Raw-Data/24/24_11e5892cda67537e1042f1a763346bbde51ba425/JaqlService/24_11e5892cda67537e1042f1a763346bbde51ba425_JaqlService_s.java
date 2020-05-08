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
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 
 import com.ibm.jaql.json.type.JsonValue;
 import com.ibm.jaql.lang.JaqlQuery;
 
 public class JaqlService {
 
 	private static final Log log = LogFactory.getLog(JaqlService.class);
 	
 	public GrailsApplication grailsApplication;
 	
 	@Cacheable("jsonValueCache")
 	public JsonValue getJsonValue(String expression, Map<String, String> variables) {
 		JsonValue value = null;
 		
 		JaqlQuery query = new JaqlQuery();
 		Map<String, JsonValue> valueMap = new HashMap<String, JsonValue>();
 		for (Entry<String, String> variable : variables.entrySet()) {
 			JsonValue variableValue = getMe().getJsonValue(variable.getValue(), new HashMap<String, String>());
 			valueMap.put(variable.getKey(), variableValue);
 		}
 
 		query.setQueryString(expression);
 		for (Entry<String, JsonValue> entry : valueMap.entrySet()) {
 			query.setVar(entry.getKey(), entry.getValue());
 		}
 		try {
 			value = query.evaluate();
 		} catch (Exception e) {
			log.error("error evaluating: "+expression, e);
 			throw new IllegalArgumentException("error evaluating: "+expression, e);
 		} finally {
 			try {query.close();} catch (IOException e) {}
 		}
 		return value;
 	}
 	
 	
 	/**
 	 * Return null if the expression is not correctly typed or returns null
 	 * 
 	 * @throws {@link IllegalArgumentException} if one of the arguments is null
 	 */
 	public Value evaluate(String expression, Type type, Map<String, Value> variables, Map<String, Type> types) 
 			throws IllegalArgumentException {
 		
 		if (log.isDebugEnabled()) log.debug("evaluate(expression="+expression+", variables="+variables+")");
 		
 		Map<String, String> jaqlVariables = new HashMap<String, String>();
 		for (Entry<String, Value> variable : variables.entrySet()) {
 			// value can be null
 			if (variable.getValue() != null && !variable.getValue().isNull()) {
 				jaqlVariables.put("$"+variable.getKey(), types.get(variable.getKey()).getJaqlValue(variable.getValue()));
 			}
 		}
 		
 		JsonValue jsonValue = getMe().getJsonValue(expression, jaqlVariables);
 		if (jsonValue == null) return Value.NULL;
 		return type.getValueFromJaql(jsonValue.toString());
 	}
 	
 	public void setGrailsApplication(GrailsApplication grailsApplication) {
 		this.grailsApplication = grailsApplication;
 	}
 	
 	// for internal call through transactional proxy
 	public JaqlService getMe() {
 		return grailsApplication.getMainContext().getBean(JaqlService.class);
 	}
 }
