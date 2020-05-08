 /**
  * 
  */
 
 package edu.common.dynamicextensions.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 
 /**
  * TextLengthValidator Class validates the text as per the text length defined for that Control during its creation.
  * If the no text length is provided or the text lenght is zero, no validation checks are made.   
  * @author chetan_patil
  */
 public class TextLengthValidator implements ValidatorRuleInterface
 {
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.validation.ValidatorRuleInterface#validate(edu.common.dynamicextensions.domaininterface.AttributeInterface, java.lang.Object, java.util.Map)
 	 */
 	public boolean validate(AttributeMetadataInterface attribute, Object valueObject,
 			Map<String, String> parameterMap) throws DynamicExtensionsValidationException
 	{
 		boolean isValid = false;
 		AttributeTypeInformationInterface attributeTypeInformation = attribute
 				.getAttributeTypeInformation();
 		String attributeName = attribute.getName();

 		//If control of type TextField is changed to the 
 		//ListBox while creating catgory. These validations should be 
 		//skipped in that case. 
 		if (valueObject instanceof List)
 		{
			return true;
 		}
 		if (valueObject != null)
 		{
 			String value = (String) valueObject;
 			if (attributeTypeInformation != null
 					&& attributeTypeInformation instanceof StringAttributeTypeInformation)
 			{
 				ArrayList<String> placeHolders = new ArrayList<String>();
 
 				StringAttributeTypeInformation stringAttributeTypeInformation = (StringAttributeTypeInformation) attributeTypeInformation;
 				Integer size = stringAttributeTypeInformation.getSize();
				//bug id :7778
				//Fixed by : prashant
				//reviewed by : kunal
				int length = value.length();
				for (int i = 0; i < value.length(); i++)
				{
					if (value.charAt(i) == '\n')
					{
						length -= 2;
					}
				}
				if ((size > 0) && (length <= size))
				{
					isValid = true;
				}
				else
 				{
 					placeHolders.add(attributeName);
 					placeHolders.add((new Long(size)).toString());
 					throw new DynamicExtensionsValidationException("Validation failed", null,
 							"dynExtn.validation.TextLength", placeHolders);
 				}
 			}
 		}
 
 		return isValid;
 	}
 }
