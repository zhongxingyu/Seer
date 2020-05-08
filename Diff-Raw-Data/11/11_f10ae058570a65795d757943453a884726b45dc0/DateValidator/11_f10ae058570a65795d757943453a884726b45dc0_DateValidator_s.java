 
 package edu.common.dynamicextensions.validation;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.common.util.Utility;
 
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
 			Map<String, String> parameterMap) throws DynamicExtensionsValidationException
 	{
 		boolean valid = false;
 		String attributeName = attribute.getName();
 		AttributeTypeInformationInterface attributeTypeInformation = attribute
 				.getAttributeTypeInformation();
 
 		if (((valueObject != null) && (!((String) valueObject).trim().equals("")))
 				&& ((attributeTypeInformation != null) && (attributeTypeInformation instanceof DateAttributeTypeInformation)))
 		{
 			DateAttributeTypeInformation dateAttributeTypeInformation = (DateAttributeTypeInformation) attributeTypeInformation;
 			String dateFormat = dateAttributeTypeInformation.getFormat();
 			String value = (String) valueObject;
 			
 			for (int i=0; i<value.length(); i++)
 			{
 				if (Character.isLetter(value.charAt(i)))
 				{
 					List<String> placeHolders = new ArrayList<String>();
 					placeHolders.add(attributeName);
 					throw new DynamicExtensionsValidationException("Validation failed", null,
 							"dynExtn.validation.Number", placeHolders);
 				}
 			}
            
             if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT))
             {
             	String month = value.substring(0, 2);
 				String year = value.substring(3, value.length());
                 value = month + "-01-" + year;
             }
             if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT))
             {
                 value = DynamicExtensionsUtility.formatYearDate(value);
                 value = value.substring(0, value.length()-4);
             }
 
 			try
 			{
 				Date date = null;
 				date = Utility.parseDate(value, "MM-dd-yyyy");
 				if (date != null)
 				{
 					valid = true;
 				}
 			}
 			catch (ParseException parseException)
 			{
 				List<String> placeHolders = new ArrayList<String>();
 				placeHolders.add(attributeName);
 				placeHolders.add(dateFormat);
 				throw new DynamicExtensionsValidationException("Validation failed", null,
 						"dynExtn.validation.Date", placeHolders);
 			}
 		}
 		return valid;
 	}
 }
