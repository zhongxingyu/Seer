 package org.chai.kevin;
 
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
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.Data;
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.data.NormalizedDataElement;
 import org.chai.kevin.data.RawDataElement;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.value.CalculationPartialValue;
 import org.chai.kevin.value.DataValue;
 import org.chai.kevin.value.NormalizedDataElementValue;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.Status;
 import org.chai.kevin.value.StoredValue;
 import org.chai.kevin.value.Value;
 import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
 import org.hisp.dhis.period.Period;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.ibm.jaql.json.type.JsonValue;
 
 public class ExpressionService {
 
 	private static final Log log = LogFactory.getLog(ExpressionService.class);
 	
 	private DataService dataService;
 	private OrganisationService organisationService;
 	private ValueService valueService;
 	private JaqlService jaqlService;
 
 	public static class StatusValuePair {
 		public Status status = null;
 		public Value value = null;
 	}
 	
//	private boolean calculateHasMissingValues(Map<Organisation, NormalizedDataElementValue> values) {
//		for (NormalizedDataElementValue expressionValue : values.values()) {
//			if (expressionValue != null && expressionValue.getStatus() == Status.MISSING_NUMBER) return true;
//		}
//		return false;
//	}
//	
//	private boolean calculateHasMissingExpression(Map<Organisation, NormalizedDataElementValue> values) {
//		for (NormalizedDataElementValue expressionValue : values.values()) {
//			if (expressionValue == null) return true;
//		}
//		return false;
//	}

 	@Transactional(readOnly=true)
 	public <T extends CalculationPartialValue> Set<T> calculatePartialValues(Calculation<T> calculation, Organisation organisation, Period period) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(calculation="+calculation+",period="+period+",organisation="+organisation+")");
 		
 		Set<T> result = new HashSet<T>();
 		List<String> expressions = calculation.getPartialExpressions();
 		for (String expression : expressions) {
 			result.addAll(calculatePartialValues(calculation, expression, organisation, period));
 		}
 		return result;
 	}
 	
 	private <T extends CalculationPartialValue> Set<T> calculatePartialValues(Calculation<T> calculation, String expression, Organisation organisation, Period period) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(expression="+expression+",period="+period+",organisation="+organisation+")");
 		
 		Set<T> result = new HashSet<T>();
 		Set<OrganisationUnitGroup> organisationUnitGroups = organisationService.getGroupsForExpression();
 		for (OrganisationUnitGroup organisationUnitGroup : organisationUnitGroups) {
 			List<Organisation> facilities = organisationService.getFacilitiesOfGroup(organisation, organisationUnitGroup);
 			
 			if (!facilities.isEmpty()) {
 				Map<Organisation, StatusValuePair> values = new HashMap<Organisation, ExpressionService.StatusValuePair>();
 				for (Organisation facility : facilities) {
 					StatusValuePair statusValuePair = getExpressionStatusValuePair(expression, Calculation.TYPE, period, facility, DataElement.class);
 					values.put(facility, statusValuePair);
 				}
 				result.add(calculation.getCalculationPartialValue(expression, values, organisation, period, organisationUnitGroup.getUuid()));
 			}
 		}
 		return result;
 	}
 	
 	
 	/**
 	 * The expression has to be aggregatable for this to work
 	 * @param facility
 	 * @param period
 	 * @param expression
 	 * @param valuesForOrganisation
 	 * 
 	 * @return
 	 */
 	@Transactional(readOnly=true)
 	public NormalizedDataElementValue calculateValue(NormalizedDataElement normalizedDataElement, Organisation facility, Period period) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(normalizedDataElement="+normalizedDataElement+",period="+period+",organisation="+facility+")");
 		
 		NormalizedDataElementValue expressionValue;
 		if (organisationService.loadLevel(facility) != organisationService.getFacilityLevel()) {
 			throw new IllegalArgumentException("calculating the value of a NormalizedDateElement for non-facility organisation is not possible");
 		}
 		else {
 			organisationService.loadGroup(facility);
 			String expression = normalizedDataElement.getExpression(period, facility.getOrganisationUnitGroup().getUuid());
 			
 			StatusValuePair statusValuePair = getExpressionStatusValuePair(expression, normalizedDataElement.getType(), period, facility, RawDataElement.class);
 			expressionValue = new NormalizedDataElementValue(statusValuePair.value, statusValuePair.status, facility.getOrganisationUnit(), normalizedDataElement, period);
 		}
 		
 		if (log.isDebugEnabled()) log.debug("getValue()="+expressionValue);
 		return expressionValue;
 	}
 
 	// organisation has to be a facility
 	private <T extends DataElement<S>, S extends DataValue> StatusValuePair getExpressionStatusValuePair(String expression, Type type, Period period, Organisation facility, Class<T> clazz) {
 		StatusValuePair statusValuePair = new StatusValuePair();
 		if (expression == null) {
 			statusValuePair.status = Status.DOES_NOT_APPLY;
 			statusValuePair.value = Value.NULL;
 		}
 		else {
 			Map<String, T> datas = getDataInExpression(expression, clazz);
 			if (hasNullValues(datas.values())) {
 				statusValuePair.value = Value.NULL;
 				statusValuePair.status = Status.MISSING_DATA_ELEMENT;
 			}
 			else {
 				Map<String, Value> valueMap = new HashMap<String, Value>();
 				Map<String, Type> typeMap = new HashMap<String, Type>();
 				
 				for (Entry<String, T> entry : datas.entrySet()) {
 					DataValue dataValue = valueService.getDataElementValue(entry.getValue(), facility.getOrganisationUnit(), period);
 					valueMap.put(entry.getValue().getId().toString(), dataValue==null?null:dataValue.getValue());
 					typeMap.put(entry.getValue().getId().toString(), entry.getValue().getType());
 				}
 				
 				if (hasNullValues(valueMap.values())) {
 					statusValuePair.value = Value.NULL;
 					statusValuePair.status = Status.MISSING_VALUE;
 				}
 				else {
 					try {
 						statusValuePair.value = jaqlService.evaluate(expression, type, valueMap, typeMap);
 						statusValuePair.status = Status.VALID;
 					} catch (IllegalArgumentException e) {
 						log.error("there was an error evaluating expression: "+expression, e);
 						statusValuePair.value = Value.NULL;
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
 		
 		JsonValue value = null;
 		try {
 			value = jaqlService.getJsonValue(formula, jaqlVariables);	
 		} catch (IllegalArgumentException e) {
 			return false;
 		}
 		return value != null;
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
 	
 	public void setOrganisationService(OrganisationService organisationService) {
 		this.organisationService = organisationService;
 	}
 
 	public void setJaqlService(JaqlService jaqlService) {
 		this.jaqlService = jaqlService;
 	}
 }
