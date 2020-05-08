 package org.chai.kevin.value;
 
 /* 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.JaqlService;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.Data;
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.NormalizedDataElement;
 import org.chai.kevin.data.RawDataElement;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.location.CalculationEntity;
 import org.chai.kevin.location.DataLocationEntity;
 import org.chai.kevin.location.DataEntityType;
 import org.hisp.dhis.period.Period;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.ibm.jaql.json.type.JsonValue;
 
 public class ExpressionService {
 
 	private static final Log log = LogFactory.getLog(ExpressionService.class);
 	private static final Log expressionLog = LogFactory.getLog("ExpressionLog");
 	
 	private DataService dataService;
 	private LocationService locationService;
 	private ValueService valueService;
 	private JaqlService jaqlService;
 
 	public static class StatusValuePair {
 		public Status status = null;
 		public Value value = null;
 	}
 	
 	@Transactional(readOnly=true)
 	public <T extends CalculationPartialValue> List<T> calculatePartialValues(Calculation<T> calculation, CalculationEntity entity, Period period) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(calculation="+calculation+",period="+period+",entity="+entity+")");
 		
 		List<T> result = new ArrayList<T>();
 		for (String expression : calculation.getPartialExpressions()) {
 			result.addAll(calculatePartialValues(calculation, expression, entity, period));
 		}
 		return result;
 	}
 	
 	private <T extends CalculationPartialValue> Set<T> calculatePartialValues(Calculation<T> calculation, String expression, CalculationEntity entity, Period period) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(expression="+expression+",period="+period+",entity="+entity+")");
 		
 		Set<T> result = new HashSet<T>();
 		for (DataEntityType type : locationService.listTypes()) {
 			Set<DataEntityType> collectForType = new HashSet<DataEntityType>();
 			collectForType.add(type);
 			List<DataLocationEntity> facilities = entity.collectDataLocationEntities(null, collectForType);
 			
 			if (!facilities.isEmpty()) {
 				Map<DataLocationEntity, StatusValuePair> values = new HashMap<DataLocationEntity, StatusValuePair>();
 				for (DataLocationEntity facility : facilities) {
 					StatusValuePair statusValuePair = getExpressionStatusValuePair(expression, Calculation.TYPE, period, facility, DataElement.class);
 					values.put(facility, statusValuePair);
 				}
 				result.add(calculation.getCalculationPartialValue(expression, values, entity, period, type));
 			}
 		}
 		return result;
 	}
 	
 	@Transactional(readOnly=true)
 	public NormalizedDataElementValue calculateValue(NormalizedDataElement normalizedDataElement, DataLocationEntity facility, Period period) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(normalizedDataElement="+normalizedDataElement+",period="+period+",facility="+facility+")");
 		
 		String expression = normalizedDataElement.getExpression(period, facility.getType().getCode());
 		
 		StatusValuePair statusValuePair = getExpressionStatusValuePair(expression, normalizedDataElement.getType(), period, facility, RawDataElement.class);
 		NormalizedDataElementValue expressionValue = new NormalizedDataElementValue(statusValuePair.value, statusValuePair.status, facility, normalizedDataElement, period);
 		
 		if (log.isDebugEnabled()) log.debug("getValue()="+expressionValue);
 		return expressionValue;
 	}
 
 	// location has to be a facility
 	private <T extends DataElement<S>, S extends DataValue> StatusValuePair getExpressionStatusValuePair(String expression, Type type, Period period, DataLocationEntity facility, Class<T> clazz) {
 		StatusValuePair statusValuePair = new StatusValuePair();
 		if (expression == null) {
 			statusValuePair.status = Status.DOES_NOT_APPLY;
 			statusValuePair.value = Value.NULL_INSTANCE();
 		}
 		else {
 			Map<String, T> datas = getDataInExpression(expression, clazz);
 			if (hasNullValues(datas.values())) {
 				statusValuePair.value = Value.NULL_INSTANCE();
 				statusValuePair.status = Status.MISSING_DATA_ELEMENT;
 			}
 			else {
 				Map<String, Value> valueMap = new HashMap<String, Value>();
 				Map<String, Type> typeMap = new HashMap<String, Type>();
 				
 				for (Entry<String, T> entry : datas.entrySet()) {
 					DataValue dataValue = valueService.getDataElementValue(entry.getValue(), facility, period);
 					valueMap.put(entry.getValue().getId().toString(), dataValue==null?null:dataValue.getValue());
 					typeMap.put(entry.getValue().getId().toString(), entry.getValue().getType());
 				}
 				
 				if (hasNullValues(valueMap.values())) {
 					statusValuePair.value = Value.NULL_INSTANCE();
 					statusValuePair.status = Status.MISSING_VALUE;
 				}
 				else {
 					try {
 						statusValuePair.value = jaqlService.evaluate(expression, type, valueMap, typeMap);
 						statusValuePair.status = Status.VALID;
 					} catch (IllegalArgumentException e) {
						expressionLog.error("expression={"+expression+"}, type={"+type+"}, period={"+period+"}, facility={"+facility+"}, valueMap={"+valueMap+"}, typeMap={"+typeMap+"}");
 						log.warn("there was an error evaluating expression: "+expression, e);
 						statusValuePair.value = Value.NULL_INSTANCE();
 						statusValuePair.status = Status.ERROR;
 					}
 				}
 			}
 		}
 		return statusValuePair;
 	}
 
 
 	// TODO do this for validation rules
 	@Transactional(readOnly=true)
 	public <T extends Data<?>> boolean expressionIsValid(String formula, Class<T> allowedClazz) {
 		if (formula.contains("\n")) return false;
 		Map<String, T> variables = getDataInExpression(formula, allowedClazz);
 		
 		if (hasNullValues(variables.values())) return false;
 		
 		Map<String, String> jaqlVariables = new HashMap<String, String>();
 		for (Entry<String, T> variable : variables.entrySet()) {
 			Type type = variable.getValue().getType();
 			jaqlVariables.put(variable.getKey(), type.getJaqlValue(type.getPlaceHolderValue()));
 		}
 		
 		try {
 			jaqlService.getJsonValue(formula, jaqlVariables);	
 		} catch (IllegalArgumentException e) {
 			return false;
 		}
 		return true;
     }
 	
 	@Transactional(readOnly=true)
     public <T extends Data<?>> Map<String, T> getDataInExpression(String expression, Class<T> clazz) {
     	if (log.isDebugEnabled()) log.debug("getDataInExpression(expression="+expression+", clazz="+clazz+")");
     	
         Map<String, T> dataInExpression = new HashMap<String, T>();
     	Set<String> placeholders = getVariables(expression);
 
     	for (String placeholder : placeholders) {
             T data = null;
             try {
             	data = dataService.getData(Long.parseLong(placeholder.replace("$", "")), clazz);
             }
             catch (NumberFormatException e) {
             	log.error("wrong format for dataelement: "+placeholder);
             }
 
             dataInExpression.put(placeholder, data);
         }
     	
     	if (log.isDebugEnabled()) log.debug("getDataInExpression()="+dataInExpression);
         return dataInExpression;
     }
 
     public static Set<String> getVariables(String expression) {
     	Set<String> placeholders = null;
         if ( expression != null ) {
         	placeholders = new HashSet<String>();
             final Matcher matcher = Pattern.compile("\\$\\d+").matcher( expression );
             
             while (matcher.find())  {
             	String match = matcher.group();
 	            placeholders.add(match);
             }
         }
         return placeholders;
     }
     
     public static String convertStringExpression(String expression, Map<String, String> mapping) {
         String result = expression;
         for (Entry<String, String> entry : mapping.entrySet()) {
         	// TODO validate key
         	if (!Pattern.matches("\\$\\d+", entry.getKey())) throw new IllegalArgumentException("key does not match expression pattern: "+entry);
         	result = result.replaceAll("\\"+entry.getKey()+"(\\z|\\D|$)", entry.getValue().replace("$", "\\$")+"$1");
 		}
         return result;
     }
 	
 	private static <T extends Object> boolean hasNullValues(Collection<T> values) {
 		for (Object object : values) {
 			if (object == null) return true;
 		}
 		return false;
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 	
 	public void setJaqlService(JaqlService jaqlService) {
 		this.jaqlService = jaqlService;
 	}
 }
