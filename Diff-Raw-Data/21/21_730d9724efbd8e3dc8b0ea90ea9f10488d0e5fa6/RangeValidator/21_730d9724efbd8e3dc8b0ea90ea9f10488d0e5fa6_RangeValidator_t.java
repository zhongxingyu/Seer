 
 package edu.common.dynamicextensions.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 
 /**
  * This Class validates the range of the numeric data entered by the user. It checks whether the value entered is between the 
  * specified range. The extreme values of the range are obtained from the RuleParameter of the Rule instance. 
  * @author chetan_patil
  * @version 1.0
  */
 public class RangeValidator implements ValidatorRuleInterface
 {
 
 	/**
 	 * This method implements the validate method of the ValidatorRuleInterface.
 	 * This method validates the numeric data entered by the user against the maximum and the 
 	 * minimum values of the rule.
 	 * @param attribute the Attribute whose corresponding value is to be verified.
 	 * @param valueObject the value entered by the user.
 	 * @param parameterMap the parameters of the Rule.
 	 * @throws DynamicExtensionsValidationException if the value is not following the range Rule. 
 	 */
 	public boolean validate(AttributeInterface attribute, Object valueObject,
 			Map<String, String> parameterMap) throws DynamicExtensionsValidationException
 	{
 		boolean valid = true;

 		/* Check for the validity of the number */
 		NumberValidator numberValidator = new NumberValidator();
 		numberValidator.validate(attribute, valueObject, parameterMap);

 		/* Check for the validity of the range of the number against the pre-defined range*/
 		if (valueObject != null)
 		{
 			if (!((String) valueObject).trim().equals(""))
 			{
 				AttributeTypeInformationInterface attributeTypeInformation = attribute
 						.getAttributeTypeInformation();
 				if (attributeTypeInformation != null)
 				{
 					String attributeName = attribute.getName();
 					String value = (String) valueObject;
 
 					Set<Map.Entry<String, String>> parameterSet = parameterMap.entrySet();
 					for (Map.Entry<String, String> parameter : parameterSet)
 					{
 						if (attributeTypeInformation instanceof LongAttributeTypeInformation)
 						{
 							checkLongValidation(parameter, attributeName, value);
 						}
 						else if (attributeTypeInformation instanceof DoubleAttributeTypeInformation)
 						{
 							checkDoubleValidation(parameter, attributeName, value);
 						}
 					}
 				}
 			}
 		}
 		return valid;
 	}
 
 	/**
 	 * This method verifies if the number is a proper Long value or not.
 	 * @param parameter the parameter of the rule in form of <ParameterName, Value> pair. 
 	 * @param attributeName the name of the Attribute.
 	 * @param value the value to be verified.
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void checkLongValidation(Map.Entry<String, String> parameter, String attributeName,
 			String value) throws DynamicExtensionsValidationException
 	{
 		String parameterName = parameter.getKey();
 		String parameterValue = parameter.getValue();
 
 		List<String> placeHolders = null;
 		if (parameterName.equals("min"))
 		{
			if (Long.parseLong(value) < Long.parseLong(parameterValue))
 			{
 				placeHolders = new ArrayList<String>();
 				placeHolders.add(attributeName);
 				placeHolders.add(parameterValue);
 
 				throw new DynamicExtensionsValidationException("Validation failed", null,
 						"dynExtn.validation.Range.Minimum", placeHolders);
 			}
 		}
 		else if (parameterName.equals("max"))
 		{
			if (Long.parseLong(value) > Long.parseLong(parameterValue))
 			{
 				placeHolders = new ArrayList<String>();
 				placeHolders.add(attributeName);
 				placeHolders.add(parameterValue);
 
 				throw new DynamicExtensionsValidationException("Validation failed", null,
 						"dynExtn.validation.Range.Maximum", placeHolders);
 			}
 		}
 	}
 
 	/**
 	 * This method verifies if the number is a proper Double value or not.
 	 * @param parameter the parameter of the rule in form of <ParameterName, Value> pair. 
 	 * @param attributeName the name of the Attribute.
 	 * @param value the value to be verified.
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void checkDoubleValidation(Map.Entry<String, String> parameter, String attributeName,
 			String value) throws DynamicExtensionsValidationException
 	{
 		String parameterName = parameter.getKey();
 		String parameterValue = parameter.getValue();
 
 		List<String> placeHolders = null;
 		if (parameterName.equals("min"))
 		{
			if (Double.parseDouble(value) < Double.parseDouble(parameterValue))
 			{
 				placeHolders = new ArrayList<String>();
 				placeHolders.add(attributeName);
 				placeHolders.add(parameterValue);
 
 				throw new DynamicExtensionsValidationException("Validation failed", null,
 						"dynExtn.validation.Range.Minimum", placeHolders);
 			}
 		}
 		else if (parameterName.equals("max"))
 		{
			if (Double.parseDouble(value) > Double.parseDouble(parameterValue))
 			{
 				placeHolders = new ArrayList<String>();
 				placeHolders.add(attributeName);
 				placeHolders.add(parameterValue);
 
 				throw new DynamicExtensionsValidationException("Validation failed", null,
 						"dynExtn.validation.Range.Maximum", placeHolders);
 			}
 		}
 	}
 
 }
