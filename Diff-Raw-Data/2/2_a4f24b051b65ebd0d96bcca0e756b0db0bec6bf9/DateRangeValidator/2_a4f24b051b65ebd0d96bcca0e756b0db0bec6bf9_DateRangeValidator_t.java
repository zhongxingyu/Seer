 
 package edu.common.dynamicextensions.validation;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.common.util.Utility;
 
 /**
  * @author chetan_patil
  *
  */
 public class DateRangeValidator implements ValidatorRuleInterface
 {
 
 	/**
 	 * @see edu.common.dynamicextensions.validation.ValidatorRuleInterface#validate(edu.common.dynamicextensions.domaininterface.AttributeInterface, java.lang.Object, java.util.Map)
 	 * @throws DynamicExtensionsValidationException
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public boolean validate(AttributeMetadataInterface attribute, Object valueObject,
 			Map<String, String> parameterMap,String controlCaption) throws DynamicExtensionsValidationException, DynamicExtensionsSystemException
 	{
 		boolean valid = true;
 
 		/* Check for the validity of the date */
 		DateValidator dateValidator = new DateValidator();
		dateValidator.validate(attribute, valueObject, parameterMap,controlCaption, true);
 
 		AttributeTypeInformationInterface attributeTypeInformation = attribute
 				.getAttributeTypeInformation();
 		if (((valueObject != null) && (!((String) valueObject).trim().equals("")))
 				&& ((attributeTypeInformation != null))
 				&& (attributeTypeInformation instanceof DateAttributeTypeInformation))
 		{
 			DateAttributeTypeInformation dateAttributeTypeInformation = (DateAttributeTypeInformation) attributeTypeInformation;
 			String dateFormat = dateAttributeTypeInformation.getFormat();
 			String attributeName = attribute.getName();
 			String value = (String) valueObject;
             
             if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT))
             {
                 value = DynamicExtensionsUtility.formatMonthAndYearDate(value);
                 value = value.substring(0, value.length()-4);
             }
             if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT))
             {
                 value = DynamicExtensionsUtility.formatYearDate(value);
                 value = value.substring(0, value.length()-4);
             }
             
 			Set<Map.Entry<String, String>> parameterSet = parameterMap.entrySet();
 			for (Map.Entry<String, String> parameter : parameterSet)
 			{
 				String parameterName = parameter.getKey();
 				String parameterValue = parameter.getValue();
                 
                 if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT))
                 {
                     parameterValue= DynamicExtensionsUtility.formatMonthAndYearDate(parameterValue);
                     parameterValue = parameterValue.substring(0, parameterValue.length()-4);
                 }
                 if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT))
                 {
                     parameterValue = DynamicExtensionsUtility.formatYearDate(parameterValue);
                     parameterValue = parameterValue.substring(0, parameterValue.length()-4);
                 }
                 
 				Date parameterDate = null, valueDate = null;
 				try
 				{
 					parameterDate = Utility.parseDate(parameterValue, "MM-dd-yyyy");
 					valueDate = Utility.parseDate(value, "MM-dd-yyyy");
 				}
 				catch (ParseException ParseException)
 				{
 					List<String> placeHolders = new ArrayList<String>();
 					placeHolders.add(controlCaption);
 					placeHolders.add(dateFormat);
 					throw new DynamicExtensionsValidationException("Validation failed", null,
 							"dynExtn.validation.Date", placeHolders);
 				}
 
 				if (parameterName.equals("min"))
 				{
 					checkMinDate(parameterDate, valueDate, attributeName, parameterValue,controlCaption);
 				}
 				else if (parameterName.equals("max"))
 				{
 					checkMaxDate(parameterDate, valueDate, attributeName, parameterValue,controlCaption);
 				}
 			}
 		}
 		return valid;
 	}
 
 	private void checkMinDate(Date parameterDate, Date valueDate, String attributeName, String parameterValue, String controlCaption)
 			throws DynamicExtensionsValidationException
 	{
 		if (valueDate.before(parameterDate))
 		{
 			List<String> placeHolders = new ArrayList<String>();
 			placeHolders.add(controlCaption);
 
 			placeHolders.add(parameterValue);
 			throw new DynamicExtensionsValidationException("Validation failed", null,
 					"dynExtn.validation.Date.Min", placeHolders);
 		}
 	}
 
 	private void checkMaxDate(Date parameterDate, Date valueDate, String attributeName, String parameterValue, String controlCaption)
 			throws DynamicExtensionsValidationException
 	{
 		if (valueDate.after(parameterDate))
 		{
 			List<String> placeHolders = new ArrayList<String>();
 			placeHolders.add(controlCaption);
 
 			placeHolders.add(parameterValue);
 			throw new DynamicExtensionsValidationException("Validation failed", null,
 					"dynExtn.validation.Date.Max", placeHolders);
 		}
 	}
 
 }
