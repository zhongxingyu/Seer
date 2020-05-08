 package com.iq4j.faces.validators;
 
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
 @FacesValidator(value="integerRangeInputValidator")
public class IntegerRangeInputValidator extends AbstractValidator implements Validator {
 	
 	
 	@Override
 	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
 		
 		System.out.println(component.getClientId());
 		
 		Input integerRangeInput = (Input)component.getParent().getParent().getParent().getParent();
 		UIInput min = (UIInput) component.getParent().findComponent("min");
 		UIInput max = (UIInput) component.getParent().findComponent("max");
 		
 		if(min.isValid() && max.isValid())
 		{
 			Integer minValue = (Integer)min.getValue();
 			Integer maxValue = (Integer)max.getValue();
 			
 			if(minValue != null && maxValue != null){				
 				int compareResult = minValue.compareTo(maxValue);
 				
 				if(compareResult == 1){
 					throwValidatorException("Minimum value can not be grater than maximum..", integerRangeInput);
 				}
 				else if( compareResult == 0 ){				
 					if(!CompositeHelper.getBooleanValue(integerRangeInput, "allowEquality")){
 						throwValidatorException("Minimum may not be equal maximum..", integerRangeInput);
 					}
 				} 	
 			}		
 			else if( (minValue != null) ^ (maxValue != null) )
 			{
 				throwValidatorException( "Invalid value", integerRangeInput );
 			}
 			
 		}
 		else
 		{
 			throwValidatorException("Invalid value", integerRangeInput);			
 		}
 		
 	}
 	
 }
