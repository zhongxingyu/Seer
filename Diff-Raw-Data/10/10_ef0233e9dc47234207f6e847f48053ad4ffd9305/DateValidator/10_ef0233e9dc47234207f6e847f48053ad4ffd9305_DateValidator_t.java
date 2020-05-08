 
 package edu.common.dynamicextensions.validation;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 
 /**
  * @author chetan_patil
  *
  */
 public class DateValidator implements ValidatorRuleInterface
 {
 
 	/**
 	 * @see edu.common.dynamicextensions.validation.ValidatorRuleInterface#validate(edu.common.dynamicextensions.domaininterface.AttributeInterface, java.lang.Object, java.util.Map)
 	 * @throws DynamicExtensionsValidationException
 	 */
 	public boolean validate(AttributeMetadataInterface attribute, Object valueObject,
 			Map<String, String> parameterMap, String controlCaption)
 			throws DynamicExtensionsValidationException
 	{
 		boolean valid = true;
		valid = validateDate(attribute, valueObject, controlCaption);
 
 		return valid;
 	}
 
 	/**
 	 * Validate user input for permissible date values for date with range. 
 	 * @param attribute
 	 * @param valueObject
 	 * @param parameterMap
 	 * @param controlCaption
 	 * @param isFromDateRangeValidator
 	 * @return
 	 * @throws DynamicExtensionsValidationException
 	 */
 	public boolean validate(AttributeMetadataInterface attribute, Object valueObject,
 			Map<String, String> parameterMap, String controlCaption,
 			boolean isFromDateRangeValidator) throws DynamicExtensionsValidationException
 	{
 		boolean valid = true;
		valid = validateDate(attribute, valueObject, controlCaption,
 				isFromDateRangeValidator);
 
 		return valid;
 	}
 
 	/**
 	 * Validate user input for permissible date values.
 	 * @param attribute
 	 * @param valueObject
 	 * @param controlCaption
 	 * @param isFromDateRangeValidator
 	 * @return
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private boolean validateDate(AttributeMetadataInterface attribute, Object valueObject,
			String controlCaption,
 			boolean... isFromDateRangeValidator) throws DynamicExtensionsValidationException
 	{
 		boolean valid = true;
 
 		AttributeTypeInformationInterface attributeTypeInformation = attribute
 				.getAttributeTypeInformation();
 
 		if (((valueObject != null) && (!((String) valueObject).trim().equals("")))
 				&& ((attributeTypeInformation != null) && (attributeTypeInformation instanceof DateAttributeTypeInformation)))
 		{
 			DateAttributeTypeInformation dateAttributeTypeInformation = (DateAttributeTypeInformation) attributeTypeInformation;
 			String dateFormat = dateAttributeTypeInformation.getFormat();
 			String value = (String) valueObject;
 			Date tempDate = null;
 
 			try
 			{
 				SimpleDateFormat sf = new SimpleDateFormat(dateFormat);
 				sf.setLenient(false);
 
 				if (isFromDateRangeValidator.length == 0)
 				{
 					tempDate = sf.parse(value);
 				}
 				else
 				{
 					sf.parse(value);
 				}
 			}
 			catch (ParseException parseException)
 			{
 				valid = false;
 			}
 
 			// Validate if year is equal to '0000' or contains '.' symbol
 			if (value.endsWith("0000") || value.contains("."))
 			{
 				valid = false;
 			}
 			// Validate length of entered date
 			if (dateFormat.length() != value.length())
 			{
 				valid = false;
 			}
 
 			if (valid && isFromDateRangeValidator.length == 0 && tempDate.after(new Date()))
 			{
 					reportInvalidInput(controlCaption, "today's date.",
 							"dynExtn.validation.Date.Max");
 			}
 
 			if (!valid)
 			{
 				reportInvalidInput(controlCaption, dateFormat, "dynExtn.validation.Date");
 			}
 		}
 
 		return valid;
 	}
 
 	/**
 	 * Report invalid user inputs to the user.
 	 * @param placeHolderOne
 	 * @param placeHolderTwo
 	 * @param errorKey
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void reportInvalidInput(String placeHolderOne, String placeHolderTwo, String errorKey)
 			throws DynamicExtensionsValidationException
 	{
 		List<String> placeHolders = new ArrayList<String>();
 		placeHolders.add(placeHolderOne);
 		placeHolders.add(placeHolderTwo);
 		throw new DynamicExtensionsValidationException("Validation failed", null, errorKey,
 				placeHolders);
 	}
 }
