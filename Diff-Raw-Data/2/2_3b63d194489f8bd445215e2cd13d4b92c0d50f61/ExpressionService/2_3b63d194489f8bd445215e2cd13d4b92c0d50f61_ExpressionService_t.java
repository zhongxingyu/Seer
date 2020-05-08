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
 import org.chai.kevin.data.Average;
 import org.chai.kevin.data.Data;
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.data.Expression;
 import org.chai.kevin.data.Sum;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.value.CalculationValue;
 import org.chai.kevin.value.DataValue;
 import org.chai.kevin.value.ExpressionValue;
 import org.chai.kevin.value.ExpressionValue.Status;
 import org.chai.kevin.value.StoredValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueCalculator;
 import org.hisp.dhis.organisationunit.OrganisationUnit;
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
 
 	private Map<Class<?>, ValueCalculator<?>> calculatorMap = new HashMap<Class<?>, ValueCalculator<?>>();
 	
 	public ExpressionService() {
 		calculatorMap.put(Expression.class, new ExpressionValueCalculator());
 		calculatorMap.put(DataElement.class, new DataValueCalculator());
 		calculatorMap.put(Average.class, new AverageValueCalculator());
 		calculatorMap.put(Sum.class, new SumValueCalculator());
 	}
 	
 	private class DataValueCalculator implements ValueCalculator<DataValue> {
 		@Override
 		public DataValue getValue(Data<DataValue> dataElement, OrganisationUnit organisationUnit, Period period) {
 			// TODO cache data values
 			return null;
 		}
 	}
 	
 	private class ExpressionValueCalculator implements ValueCalculator<ExpressionValue> {
 		@Override
 		public ExpressionValue getValue(Data<ExpressionValue> expression, OrganisationUnit organisationUnit, Period period) {
 			return calculateValue((Expression)expression, period, organisationService.getOrganisation(organisationUnit.getId()));
 		}
 	}
 
 	
 	private class AverageValueCalculator implements ValueCalculator<CalculationValue> {
 		@Override
 		public CalculationValue getValue(Data<CalculationValue> calculation, OrganisationUnit organisationUnit, Period period) {
 			return calculateAverageValue((Average)calculation, period, organisationService.getOrganisation(organisationUnit.getId()));
 		}
 	}
 	
 	private class SumValueCalculator implements ValueCalculator<CalculationValue> {
 		@Override
 		public CalculationValue getValue(Data<CalculationValue> count, OrganisationUnit organisationUnit, Period period) {
 			return calculateSumValue((Sum)count, period, organisationService.getOrganisation(organisationUnit.getId()));
 		}
 	}
 	
 	public <T extends StoredValue> T calculate(Data<T> data, OrganisationUnit organisationUnit, Period period) {
 		// TODO make a registry class with this code
 		Class<?> clazz = data.getClass();
 		while (!calculatorMap.containsKey(clazz)) {
 			clazz = clazz.getSuperclass();
 		}
 		@SuppressWarnings("unchecked")
 		ValueCalculator<T> calculator = (ValueCalculator<T>)calculatorMap.get(clazz);
 		return data.getValue(calculator, organisationUnit, period);
 	}
 	
 	/**
 	 * 
 	 * @param calculation
 	 * @param period
 	 * @param organisation
 	 * @return
 	 */
 	@Transactional(readOnly=true)
 	public Map<Organisation, ExpressionValue> calculateExpressionValues(Map<String, Expression> expressions, Period period, Organisation organisation) {
 		Map<Organisation, ExpressionValue> result = new HashMap<Organisation, ExpressionValue>();
 		List<Organisation> organisations = organisationService.getChildrenOfLevel(organisation, organisationService.getFacilityLevel());
 		
 		for (Organisation child : organisations) {
 			// TODO use group collection ?
 			organisationService.loadGroup(child);
 			
 			ExpressionValue expressionValue = null;
 			Expression expression = getMatchingExpression(expressions, child);
 			if (expression != null) { 
 				expressionValue = (ExpressionValue)valueService.getValue(expression, child.getOrganisationUnit(), period);
 			}
 			result.put(child, expressionValue);
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns the value or null if it is not aggregated and the organisation has missing values
 	 * 
 	 * If all data elements in the expression are aggregatable, the aggregated value at this organisation
 	 * is returned.
 	 * 
 	 * If one of the data elements in the expression is not aggregatable, the average value of all the organisation's
 	 * children is returned.
 	 * 
 	 * 
 	 * @param expression
 	 * @param period
 	 * @param organisation
 	 * @param values
 	 * @return
 	 */
 	// TODO decide if this can be called with a facility, 
 	@Transactional(readOnly=true)
 	private CalculationValue calculateAverageValue(Average average, Period period, Organisation organisation) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(calculation="+average+",period="+period+",organisation="+organisation+")");
 		
 		Map<Organisation, ExpressionValue> result = calculateExpressionValues(average.getExpressions(), period, organisation);
 
 		CalculationValue calculationValue = new CalculationValue(average, organisation.getOrganisationUnit(), period, 
 				average.getType().getValue(calculateAverage(result)), calculateHasMissingValues(result), calculateHasMissingExpression(result));
 		
 		if (log.isDebugEnabled()) log.debug("calculateValue(...)="+calculationValue);
 		return calculationValue;
 	}
 	
 	@Transactional(readOnly=true)
 	private CalculationValue calculateSumValue(Sum sum, Period period, Organisation organisation) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(sum="+sum+",period="+period+",organisation="+organisation+")");
 		
 		Map<Organisation, ExpressionValue> result = calculateExpressionValues(sum.getExpressions(), period, organisation);
 		
 		CalculationValue countValue = new CalculationValue(sum, organisation.getOrganisationUnit(), period, 
 				sum.getType().getValue(calculateSum(result)), calculateHasMissingValues(result), calculateHasMissingExpression(result));
 		
 		if (log.isDebugEnabled()) log.debug("calculateValue(...)="+countValue);
 		return countValue;
 	}
 	
 	private boolean calculateHasMissingValues(Map<Organisation, ExpressionValue> values) {
 		for (ExpressionValue expressionValue : values.values()) {
 			if (expressionValue != null && expressionValue.getStatus() == Status.MISSING_NUMBER) return true;
 		}
 		return false;
 	}
 	
 	private boolean calculateHasMissingExpression(Map<Organisation, ExpressionValue> values) {
 		for (ExpressionValue expressionValue : values.values()) {
 			if (expressionValue == null) return true;
 		}
 		return false;
 	}
 
 	private Double calculateSum(Map<Organisation, ExpressionValue> values) {
 		Double sum = 0d;
 		for (ExpressionValue expressionValue : values.values()) {
 			if (expressionValue != null && expressionValue.getStatus() == Status.VALID) {
 				try {
 					sum += expressionValue.getValue().getNumberValue().doubleValue();
 				} catch (NumberFormatException e) {
 					log.warn("non-number value found in sum: ", e);
 				}
 			}
 		}
 		return sum;
 	}
 	
 	private Double calculateAverage(Map<Organisation, ExpressionValue> values) {
 		Double sum = 0d;
 		Integer num = 0;
 		for (ExpressionValue expressionValue : values.values()) {
 			if (expressionValue != null && expressionValue.getStatus() == Status.VALID) {
 				if (expressionValue.getValue().getNumberValue() != null) {
 					sum += expressionValue.getValue().getNumberValue().doubleValue();
 					num++;
 				} 
 				else { 
 					log.error("non-number value found in average: "+ expressionValue);
 				}
 			}
 		}
 		Double average = sum / num;
 		if (average.isNaN()) average = null;
 		
 		return average; 
 	}
 	
 	/**
 	 * 
 	 * @param expression
 	 * @param period
 	 * @param organisation
 	 * @return
 	 */
 	@Transactional(readOnly=true)
 	public Map<Organisation, Map<DataElement, DataValue>> calculateDataValues(Expression expression, Period period, Organisation organisation) {
 		Map<Organisation, Map<DataElement, DataValue>> values = new HashMap<Organisation, Map<DataElement,DataValue>>();
 		Map<String, Data<?>> datas = getDataInExpression(expression.getExpression());
 		for (Entry<String, Data<?>> entry : datas.entrySet()) calculateDataValue(entry.getValue(), period, organisation, values);
 		return values;
 	}
 	
 	/**
 	 * The expression has to be aggregatable for this to work
 	 * 
 	 * @param expression
 	 * @param period
 	 * @param organisation
 	 * @param valuesForOrganisation
 	 * @return
 	 */
 	@Transactional(readOnly=true)
 	private ExpressionValue calculateValue(Expression expression, Period period, Organisation organisation) {
 		if (log.isDebugEnabled()) log.debug("calculateValue(expression="+expression+",period="+period+",organisation="+organisation+")");
 		
 		Value value;
 		Status status = null;
 		Map<String, Data<?>> datas = getDataInExpression(expression.getExpression());
 		if (hasNullValues(datas.values())) {
 			value = Value.NULL;
 			status = Status.MISSING_DATA_ELEMENT;
 		}
 		else {
 			if (!isAggregatable(datas.values()) && organisationService.loadLevel(organisation) != organisationService.getFacilityLevel()) {
 				status = Status.NOT_AGGREGATABLE;
 				value = Value.NULL;
 			}
 			else {
 				Map<Organisation, Map<DataElement, DataValue>> values = new HashMap<Organisation, Map<DataElement, DataValue>>();
 				Map<String, Value> valueMap = new HashMap<String, Value>();
 				Map<String, Type> typeMap = new HashMap<String, Type>();
 				
 				for (Entry<String, Data<?>> entry : datas.entrySet()) {
 					StoredValue dataValue = calculateDataValue(entry.getValue(), period, organisation, values);
 					valueMap.put(entry.getValue().getId().toString(), dataValue==null?null:dataValue.getValue());
 					typeMap.put(entry.getValue().getId().toString(), entry.getValue().getType());
 				}
 				
 				if (hasNullValues(valueMap.values())) {
 					value = Value.NULL;
 					status = Status.MISSING_NUMBER;
 				}
 				else {
 					try {
 						value = jaqlService.evaluate(expression.getExpression(), expression.getType(), valueMap, typeMap);
 						status = Status.VALID;
 					} catch (IllegalArgumentException e) {
 						log.error("there was an error evaluating expression: "+expression, e);
 						value = Value.NULL;
 						status = Status.ERROR;
 					}
 				}
 			}
 		}
 		ExpressionValue expressionValue = new ExpressionValue(value, status, organisation.getOrganisationUnit(), expression, period);
 		
 		if (log.isDebugEnabled()) log.debug("getValue()="+expressionValue);
 		return expressionValue;
 		
 	}
 
 	private boolean isAggregatable(Collection<Data<?>> elements) {
 		for (Data<?> data : elements) {
 			if (!data.isAggregatable()) return false;
 		}
 		return true;
 	}
 
 	public Expression getMatchingExpression(Map<String, Expression> expressions, Organisation organisation) {
 		OrganisationUnitGroup group = organisation.getOrganisationUnitGroup();
 		if (log.isDebugEnabled()) log.debug("group on organisation: "+group);
 		if (log.isDebugEnabled()) log.debug("groups on calculations: "+expressions.keySet());
 
 		Expression expression = expressions.get(group.getUuid());
 		if (log.isDebugEnabled()) log.debug("found matching expression: "+expression);
 		return expression;
 	}
 	
 	
 	private StoredValue calculateDataValue(Data<?> data, Period period, Organisation organisation, Map<Organisation, Map<DataElement, DataValue>> values) {
 		if (log.isDebugEnabled()) log.debug("getDataValue(data="+data+", period="+period+", organisation="+organisation+")");
 		
 		StoredValue result = null;
 		// TODO fix this
 		if (data instanceof DataElement) {
 			DataElement dataElement = (DataElement)data;
 			// TODO this should be decided otherwise, 
 			// or defined by the user
 			Map<DataElement, DataValue> valuesForOrganisation = values.get(organisation);
 			if (valuesForOrganisation == null) {
 				valuesForOrganisation = new HashMap<DataElement, DataValue>();
 				values.put(organisation, valuesForOrganisation);
 			}
 			
 			if (!dataElement.isAggregatable() || organisationService.loadLevel(organisation) == organisationService.getFacilityLevel()) {
 				result = valueService.getValue(dataElement, organisation.getOrganisationUnit(), period);
 			}
 			else {
 				List<Organisation> children = organisationService.getChildrenOfLevel(organisation, organisationService.getFacilityLevel());
 				Double value = 0d;
 				for (Organisation child : children) {
 					Map<DataElement, DataValue> valuesForChildOrganisation = new HashMap<DataElement, DataValue>();
 					values.put(child, valuesForChildOrganisation);
 					DataValue dataValue = valueService.getValue(dataElement, child.getOrganisationUnit(), period);
 					valuesForChildOrganisation.put(dataElement, dataValue);
 					if (dataValue != null) {
 						value += dataValue.getValue().getNumberValue().doubleValue();
 					}
 				}
 				result = new DataValue(dataElement, organisation.getOrganisationUnit(), period, dataElement.getType().getValue(value));
 			}
 			valuesForOrganisation.put(dataElement, (DataValue)result);
 		}
 		else {
 			result = valueService.getValue(data, organisation.getOrganisationUnit(), period);	
 		}
 		if (log.isDebugEnabled()) log.debug("getDataValue(...)="+result);
 		return result;
 	}
 	
 
 	
 	private static <T extends Object> boolean hasNullValues(Collection<T> values) {
 		for (Object object : values) {
 			if (object == null) return true;
 		}
 		return false;
 	}
 
 	// TODO do this for validation rules
 	public synchronized boolean expressionIsValid(String formula) {
 		Map<String, Data<?>> variables = getDataInExpression(formula);
 		if (hasNullValues(variables.values())) return false;
 		
 		Map<String, String> jaqlVariables = new HashMap<String, String>();
 		for (Entry<String, Data<?>> variable : variables.entrySet()) {
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
 	
     public static Set<String> getVariables(String expression) {
     	Set<String> placeholders = null;
         if ( expression != null ) {
         	placeholders = new HashSet<String>();
            final Matcher matcher = Pattern.compile("\\$\\d+").matcher(expression);
             
             while (matcher.find())  {
             	String match = matcher.group();
 	            placeholders.add(match);
             }
         }
         return placeholders;
     }
     
     public Map<String, Data<?>> getDataInExpression(String expression) {
         Map<String, Data<?>> dataInExpression = new HashMap<String, Data<?>>();
     	Set<String> placeholders = getVariables(expression);
 
     	for (String placeholder : placeholders) {
             Data<?> data = null;
             try {
             	data = dataService.getData(Long.parseLong(placeholder.replace("$", "")));
             }
             catch (NumberFormatException e) {
             	log.error("wrong format for dataelement: "+placeholder);
             }
 
             dataInExpression.put(placeholder, data);
         }
     	
         return dataInExpression;
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
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setOrganisationService(OrganisationService organisationService) {
 		this.organisationService = organisationService;
 	}
 
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setJaqlService(JaqlService jaqlService) {
 		this.jaqlService = jaqlService;
 	}
 }
