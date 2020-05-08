 package com.nanuvem.lom.kernel;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.nanuvem.lom.kernel.dao.AttributeDao;
 import com.nanuvem.lom.kernel.dao.DaoFactory;
 import com.nanuvem.lom.kernel.validator.AttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.BooleanAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.MaximumLengthAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.MaximumRepeatAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.MinAndMaxConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.MinimumLengthAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.MinimumNumbersAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.MinimumSymbolsAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.MinimumUppersAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.RegexAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.StringAttributeConfigurationValidator;
 import com.nanuvem.lom.kernel.validator.ValidationError;
 
 public class AttributeServiceImpl {
 
 	private AttributeDao attributeDao;
 
 	private ClassServiceImpl classService;
 
 	private final Integer MINIMUM_VALUE_FOR_THE_ATTRIBUTE_SEQUENCE = 1;
 
 	private String[] fieldsValidators = { "mandatory", "default", "regex",
 			"minlength", "maxlength", "minUppers", "minNumbers", "minSymbols",
 			"maxRepeat" };
 
 	private final String MANDATORY_CONFIGURATION_NAME = fieldsValidators[0];
 	private final String DEFAULT_CONFIGURATION_NAME = fieldsValidators[1];
 	private final String REGEX_CONFIGURATION_NAME = fieldsValidators[2];
 	private final String MINLENGTH_CONFIGURATION_NAME = fieldsValidators[3];
 	private final String MAXLENGTH_CONFIGURATION_NAME = fieldsValidators[4];
 
 	private final String MINUPPERS_CONFIGURATION_NAME = fieldsValidators[5];
 	private final String MINNUMBERS_CONFIGURATION_NAME = fieldsValidators[6];
 	private final String MINSYMBOLS_CONFIGURATION_NAME = fieldsValidators[7];
 	private final String MAXREPEAT_CONFIGURATION_NAME = fieldsValidators[8];
 
 	private final String PREFIX_EXCEPTION_MESSAGE_CONFIGURATION = "Invalid configuration for attribute";
 
 	private Map<String, List<AttributeConfigurationValidator>> validators = new HashMap<String, List<AttributeConfigurationValidator>>();
 
 	public AttributeServiceImpl(DaoFactory dao) {
 		this.classService = new ClassServiceImpl(dao);
 		this.attributeDao = dao.createAttributeDao();
 
 		for (AttributeType attributeType : AttributeType.values()) {
 			List<AttributeConfigurationValidator> listValidators = new ArrayList<AttributeConfigurationValidator>();
 			this.validators.put(attributeType.toString(), listValidators);
 
 			if (attributeType.equals(AttributeType.TEXT)
 					|| attributeType.equals(AttributeType.LONGTEXT)) {
 
 				listValidators.add(new RegexAttributeConfigurationValidator(
 						REGEX_CONFIGURATION_NAME, DEFAULT_CONFIGURATION_NAME));
 			}
 
 			if (attributeType.equals(AttributeType.PASSWORD)) {
 
 				listValidators
 						.add(new MinimumUppersAttributeConfigurationValidator(
 								MINUPPERS_CONFIGURATION_NAME,
 								DEFAULT_CONFIGURATION_NAME));
 
 				listValidators
 						.add(new MinimumNumbersAttributeConfigurationValidator(
 								MINNUMBERS_CONFIGURATION_NAME,
 								DEFAULT_CONFIGURATION_NAME));
 
 				listValidators
 						.add(new MinimumSymbolsAttributeConfigurationValidator(
 								MINSYMBOLS_CONFIGURATION_NAME,
 								DEFAULT_CONFIGURATION_NAME));
 
 				listValidators
 						.add(new MaximumRepeatAttributeConfigurationValidator(
 								MAXREPEAT_CONFIGURATION_NAME,
 								DEFAULT_CONFIGURATION_NAME));
 			}
 
 			listValidators.add(new BooleanAttributeConfigurationValidator(
 					MANDATORY_CONFIGURATION_NAME));
 			listValidators.add(new StringAttributeConfigurationValidator(
 					DEFAULT_CONFIGURATION_NAME));
 			listValidators
 					.add(new MinimumLengthAttributeConfigurationValidator(
 							MINLENGTH_CONFIGURATION_NAME,
 							DEFAULT_CONFIGURATION_NAME));
 			listValidators
 					.add(new MaximumLengthAttributeConfigurationValidator(
 							MAXLENGTH_CONFIGURATION_NAME,
 							DEFAULT_CONFIGURATION_NAME));
 			listValidators
 					.add(new MinAndMaxConfigurationValidator(
 							MAXLENGTH_CONFIGURATION_NAME,
 							MINLENGTH_CONFIGURATION_NAME));
 		}
 	}
 
 	private void validateCreate(Attribute attribute) {
 		this.validateExistingAttributeNotInClass(attribute);
 		int currentNumberOfAttributes = attribute.getClazz().getAttributes()
 				.size();
 		if (attribute.getSequence() != null) {
 			boolean minValueForSequence = attribute.getSequence() < MINIMUM_VALUE_FOR_THE_ATTRIBUTE_SEQUENCE;
 			boolean maxValueForSequence = currentNumberOfAttributes + 1 < attribute
 					.getSequence();
 
 			if (minValueForSequence || maxValueForSequence) {
 				throw new MetadataException(
 						"Invalid value for Attribute sequence: "
 								+ attribute.getSequence());
 			}
 		} else {
 			attribute.setSequence(currentNumberOfAttributes + 1);
 		}
 
 		if (attribute.getName() == null || attribute.getName().isEmpty()) {
 			throw new MetadataException("The name of a Attribute is mandatory");
 		}
 
 		this.validateNameAttribute(attribute);
 
 		if (attribute.getType() == null) {
 			throw new MetadataException("The type of a Attribute is mandatory");
 		}
 		this.validateConfigurationAttribute(attribute);
 	}
 
 	private void validateNameAttribute(Attribute attribute) {
 		if (attribute.getName() == null || attribute.getName().isEmpty()) {
 			throw new MetadataException("The name of an Attribute is mandatory");
 		}
 
 		if (!Pattern.matches("[a-zA-Z1-9]{1,}", attribute.getName())) {
 			throw new MetadataException("Invalid value for Attribute name: "
 					+ attribute.getName());
 		}
 	}
 
 	private List<Attribute> findAllAttributesForClass(Class clazz) {
 		if (clazz != null && !clazz.getFullName().isEmpty()) {
 			Class foundClass = this.classService.readClass(clazz.getFullName());
 			if (foundClass != null && foundClass.getAttributes() != null
 					&& foundClass.getAttributes().size() > 0) {
 				return foundClass.getAttributes();
 			}
 		}
 		return null;
 	}
 
 	private void validateConfigurationAttribute(Attribute attribute) {
 		String configuration = attribute.getConfiguration();
 		if (configuration != null && !configuration.isEmpty()) {
 			JsonNode jsonNode = validateJson(configuration);
 			List<ValidationError> errors = new ArrayList<ValidationError>();
 			List<AttributeConfigurationValidator> attributeValidators = this.validators
 					.get(attribute.getType().toString());
 
 			this.validateExistingFieldValidator(jsonNode.getFieldNames(),
 					attribute);
 
 			for (AttributeConfigurationValidator validator : attributeValidators) {
 				validator.validate(errors, attribute, jsonNode);
 			}
 
 			if (!errors.isEmpty()) {
 				String errorMessage = "";
 				for (ValidationError error : errors) {
 					if (errorMessage.isEmpty()) {
 						errorMessage += PREFIX_EXCEPTION_MESSAGE_CONFIGURATION
 								+ " " + attribute.getName() + ": "
 								+ error.getMessage();
 					} else {
 						errorMessage += ", " + error.getMessage();
 					}
 				}
 				throw new MetadataException(errorMessage);
 			}
 		}
 	}
 
	private void validateExistingFieldValidator(Iterator<String> nextField,
 			Attribute attribute) {
 
		while (nextField.hasNext()) {
			String nextIterator = nextField.next();
 			for (int i = 0; i < this.fieldsValidators.length; i++) {
 				if (nextIterator.equals(this.fieldsValidators[i])) {
 					return;
 				}
 			}
 			throw new MetadataException("Invalid configuration for attribute "
 					+ attribute.getName()
 					+ ": the anyconf configuration attribute is unknown");
 		}
 	}
 
 	private JsonNode validateJson(String configuration) {
 		JsonNode jsonNode = null;
 		try {
 			ObjectMapper objectMapper = new ObjectMapper();
 			JsonFactory factory = objectMapper.getJsonFactory();
 			jsonNode = objectMapper.readTree(factory
 					.createJsonParser(configuration));
 		} catch (Exception e) {
 			throw new MetadataException(
 					"Invalid value for Attribute configuration: "
 							+ configuration);
 		}
 		return jsonNode;
 	}
 
 	private Class validateExistingClassForAttribute(Attribute attribute) {
 		Class clazz = null;
 		try {
 			clazz = classService.readClass(attribute.getClazz().getFullName());
 		} catch (MetadataException e) {
 			throw new MetadataException("Invalid Class: "
 					+ attribute.getClazz().getFullName());
 		}
 		return clazz;
 	}
 
 	private void validateExistingAttributeNotInClass(Attribute attribute) {
 		List<Attribute> foundAttributes = this
 				.findAllAttributesForClass(attribute.getClazz());
 		if (foundAttributes != null) {
 			for (Attribute at : foundAttributes) {
 				if (at.getName().equalsIgnoreCase(attribute.getName())) {
 					this.throwMetadataExceptionOnAttributeDuplication(attribute);
 				}
 			}
 		}
 	}
 
 	private void validateExistingAttributeNotInClassOnUpdate(Attribute attribute) {
 		if (attribute.getId() != null) {
 			Attribute foundAttributes = this
 					.findAttributeByNameAndClassFullName(attribute.getName(),
 							attribute.getClazz().getFullName());
 			if (foundAttributes != null) {
 				if (!attribute.getId().equals(foundAttributes.getId())) {
 					this.throwMetadataExceptionOnAttributeDuplication(attribute);
 				}
 			}
 		}
 	}
 
 	private void throwMetadataExceptionOnAttributeDuplication(
 			Attribute attribute) {
 		throw new MetadataException("Attribute duplication on "
 				+ attribute.getClazz().getFullName()
 				+ " Class. It already has an attribute "
 				+ StringUtils.lowerCase(attribute.getName() + "."));
 	}
 
 	public void create(Attribute attribute) {
 		Class clazz = validateExistingClassForAttribute(attribute);
 		attribute.setClazz(clazz);
 		this.validateCreate(attribute);
 		this.attributeDao.create(attribute);
 	}
 
 	public List<Attribute> listAllAttributes(String classFullName) {
 		return this.attributeDao.listAllAttributes(classFullName);
 	}
 
 	public Attribute findAttributeById(Long id) {
 		if (id != null) {
 			return this.attributeDao.findAttributeById(id);
 		} else {
 			return null;
 		}
 	}
 
 	public Attribute findAttributeByNameAndClassFullName(String nameAttribute,
 			String classFullName) {
 
 		if ((nameAttribute != null && !nameAttribute.isEmpty())
 				&& (classFullName != null && !classFullName.isEmpty())) {
 			if (!classFullName.contains(".")) {
 				classFullName = ClassServiceImpl.PREVIOUS_NAME_DEFAULT_OF_THE_CLASSFULLNAME
 						+ "." + classFullName;
 			}
 			return this.attributeDao.findAttributeByNameAndClassFullName(
 					nameAttribute, classFullName);
 		}
 		return null;
 	}
 
 	public Attribute update(Attribute attribute) {
 		this.validateNameAttribute(attribute);
 		this.validateUpdateSequence(attribute);
 		this.validateUpdateType(attribute);
 		this.validateExistingAttributeNotInClassOnUpdate(attribute);
 		this.validateConfigurationAttribute(attribute);
 
 		return this.attributeDao.update(attribute);
 	}
 
 	private void validateUpdateType(Attribute attribute) {
 		Attribute attributeFound = this.findAttributeById(attribute.getId());
 
 		if (!attributeFound.getType().equals(attribute.getType())) {
 			throw new MetadataException(
 					"Can not change the type of an attribute");
 		}
 	}
 
 	private void validateUpdateSequence(Attribute attribute) {
 		Class clazz = this.classService.readClass(attribute.getClazz()
 				.getFullName());
 		int currentNumberOfAttributes = clazz.getAttributes()
 				.get(clazz.getAttributes().size() - 1).getSequence();
 
 		if (attribute.getSequence() != null) {
 			boolean minValueForSequence = attribute.getSequence() < MINIMUM_VALUE_FOR_THE_ATTRIBUTE_SEQUENCE;
 			boolean maxValueForSequence = currentNumberOfAttributes < attribute
 					.getSequence();
 
 			if (!(minValueForSequence || maxValueForSequence)) {
 				return;
 			}
 		}
 		throw new MetadataException("Invalid value for Attribute sequence: "
 				+ attribute.getSequence());
 	}
 }
