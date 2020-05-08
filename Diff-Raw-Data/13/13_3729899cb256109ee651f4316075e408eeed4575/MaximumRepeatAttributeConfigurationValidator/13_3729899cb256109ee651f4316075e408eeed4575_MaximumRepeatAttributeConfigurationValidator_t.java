 package com.nanuvem.lom.kernel.validator;
 
 import static com.nanuvem.lom.kernel.validator.AttributeTypeConfigurationValidator.addError;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonNode;
 
 import com.nanuvem.lom.kernel.Attribute;
 
 public class MaximumRepeatAttributeConfigurationValidator extends
 		AttributeConfigurationValidatorWithDefault {
 
 	public MaximumRepeatAttributeConfigurationValidator(String field,
 			String defaultField) {
 		super(field, defaultField);
 	}
 
 	@Override
 	protected void validateDefault(List<ValidationError> errors,
 			Attribute attribute, JsonNode configuration, String defaultValue) {
 
 		Map<String, Integer> mapCounter = new HashMap<String, Integer>();
 		int characterCounter = 0;
 		
 		for (int i = 0; i < defaultValue.toCharArray().length; i++) {
 			Integer count = mapCounter.get(String.valueOf(defaultValue.toCharArray()[i]));
 			if (count == null) {
 				count = new Integer(-1);
 			}
 			count++;
 			mapCounter.put(String.valueOf(defaultValue.toCharArray()[i]), count);
 
 			if (characterCounter < count) {
 				characterCounter = count;
 			}
 		}
 
 		int maxRepeat = configuration.get(field).asInt();
 		if (characterCounter > maxRepeat) {
 			String messagePlural = characterCounter > 1 ? " more than "
 					+ (maxRepeat + 1) + " " : " ";
 			addError(errors, "the default value must not have" + messagePlural
 					+ "repeated characters");
 		}
 	}
 
 	@Override
 	protected AttributeConfigurationValidator createFieldValidator(String field) {
 		return new IntegerAttributeConfigurationValidator(field);
 	}
 
 }
