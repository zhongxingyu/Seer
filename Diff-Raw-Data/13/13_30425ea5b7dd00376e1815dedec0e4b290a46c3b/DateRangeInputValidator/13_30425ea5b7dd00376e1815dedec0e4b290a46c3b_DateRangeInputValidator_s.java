 package com.iq4j.faces.validators;
 
 import java.util.Date;
 
 import javax.enterprise.inject.Model;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
 import javax.faces.validator.ValidatorException;
 
 import com.iq4j.faces.composites.CompositeHelper;
 import com.iq4j.faces.composites.components.Input;
 
 
 @Model
 @FacesValidator(value="dateRangeInputValidator")
public class DateRangeInputValidator extends AbstractValidator implements Validator {
 	
 	
 	@Override
 	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
 		
 		System.out.println(component.getClientId());
 		
 		Input dateRangeInput = (Input)component.getParent().getParent().getParent().getParent();
 		UIInput min = (UIInput) component.getParent().findComponent("min");
 		UIInput max = (UIInput) component.getParent().findComponent("max");
 		
 		if(min.isValid() && max.isValid())
 		{
 			Date minValue = (Date)min.getValue();
 			Date maxValue = (Date)max.getValue();
 			
 			if(minValue != null && maxValue != null){				
 				int compareResult = minValue.compareTo(maxValue);
 				
 				if(compareResult == 1){
 					throwValidatorException("Start can not be further than end.", dateRangeInput); 
 				}
 				else if( compareResult == 0 ){				
 					if(!CompositeHelper.getBooleanValue(dateRangeInput, "allowEquality")){
 						throwValidatorException("Start date may not be equal end date..", dateRangeInput);
 					}
 				} 	
 			}		
 			else if( (minValue != null) ^ (maxValue != null) )
 			{
 				throwValidatorException( "Invalid date range", dateRangeInput );
 			}
 			
 		}
 		else
 		{
 			throwValidatorException("Invalid date range", dateRangeInput);			
 		}
 		
 	}
 	
 	
 	
 }
