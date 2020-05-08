 package org.chai.kevin.survey;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.chai.kevin.ExpressionService;
 import org.chai.kevin.ValueService;
 import org.chai.kevin.survey.validation.SurveyValidationRule;
 import org.chai.kevin.value.DataValue;
 import org.hisp.dhis.organisationunit.OrganisationUnit;
 import org.hisp.dhis.period.Period;
 import org.jfree.util.Log;
 
 public class ValidationService {
 
 	private ValueService valueService;
 	private SurveyElementService surveyElementService;
 	
 	public boolean validate(SurveyElementValue value, SurveyValidationRule validationRule, OrganisationUnit organisationUnit, Period period) {
 		Set<String> placeholders = ExpressionService.getPlaceholders(validationRule.getExpression());
 		
 		Map<String, String> replace = new HashMap<String, String>();
 		for (String placeholder : placeholders) {
 			Long id = Long.parseLong(placeholder);
 			SurveyElementValue surveyElementValue;
 			if (value.getSurveyElement().getId().equals(id)) surveyElementValue = value;
 			else surveyElementValue = value.getSurveyPage().getSurveyElementValue(Long.parseLong(placeholder));
 			
 			DataValue dataValue = null;
 			if (surveyElementValue != null) {
 				dataValue = surveyElementValue.getDataValue();
 			}
 			else {
 				SurveyElement surveyElement = surveyElementService.getSurveyElement(id);
 				if (surveyElement != null) dataValue = valueService.getValue(surveyElement.getDataElement(), organisationUnit, period);
 				else Log.error("validation rule "+validationRule+" refers to unknown survey element: "+id);
 			}
 			String replacement = String.valueOf(dataValue!=null?dataValue.getValue():null);
 			replace.put(placeholder, replacement);
 		}
 		
 		String toEvaluate = ExpressionService.convertStringExpression(validationRule.getExpression(), replace);
 		Object evaluation = ExpressionService.evaluate(toEvaluate);
 		
 		if (evaluation == null || evaluation.equals(0d)) {
 			return false;
 		}
 		else return true;
 	}
 
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setSurveyElementService(
 			SurveyElementService surveyElementService) {
 		this.surveyElementService = surveyElementService;
 	}
 }
