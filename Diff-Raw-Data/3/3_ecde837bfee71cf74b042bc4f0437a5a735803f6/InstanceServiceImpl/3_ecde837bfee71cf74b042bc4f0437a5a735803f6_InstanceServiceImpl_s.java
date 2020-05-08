 package com.nanuvem.lom.kernel;
 
 import org.codehaus.jackson.JsonNode;
 
 import com.nanuvem.lom.kernel.dao.AttributeValueDao;
 import com.nanuvem.lom.kernel.dao.DaoFactory;
 import com.nanuvem.lom.kernel.dao.InstanceDao;
 import com.nanuvem.lom.kernel.util.JsonNodeUtil;
 import com.nanuvem.lom.kernel.validator.deployer.AttributeTypeDeployer;
 import com.nanuvem.lom.kernel.validator.deployer.Deployers;
 
 public class InstanceServiceImpl {
 
 	private InstanceDao instanceDao;
 	private AttributeValueDao attributeValueDao;
 	private ClassServiceImpl classService;
 	private Deployers deployers;
 
 	InstanceServiceImpl(DaoFactory daoFactory, ClassServiceImpl classService,
 			Deployers deployers) {
 		this.classService = classService;
 		this.deployers = deployers;
 		this.instanceDao = daoFactory.createInstanceDao();
 		this.attributeValueDao = daoFactory.createAttributeValueDao();
 	}
 
 	public void create(Instance instance) {
 		if (instance.getClazz() == null) {
 			throw new MetadataException(
 					"Invalid value for Instance class: The class is mandatory");
 		}
 		Class clazz;
 		try {
 			clazz = this.classService.readClass(instance.getClazz()
 					.getFullName());
 		} catch (MetadataException e) {
 			throw new MetadataException("Unknown class: "
 					+ instance.getClazz().getFullName());
 		}
 		instance.setClazz(clazz);
 		this.validateAndAssignDefaultValueInAttributesValues(instance, clazz);
 
 		this.instanceDao.create(instance);
 		for (AttributeValue value : instance.getValues()) {
 			value.setInstance(instance);
 			this.attributeValueDao.create(value);
 
 		}
 	}
 
 	private void validateAndAssignDefaultValueInAttributesValues(
 			Instance instance, Class clazz) {
 
 		for (AttributeValue attributeValue : instance.getValues()) {
 			if (!(clazz.getAttributes().contains(attributeValue.getAttribute()))) {
 				throw new MetadataException("Unknown attribute for "
 						+ instance.getClazz().getFullName() + ": "
 						+ attributeValue.getAttribute().getName());
 			}
 			this.validateTypeOfValue(attributeValue);
 
 			boolean nullConfiguration = attributeValue.getAttribute()
 					.getConfiguration() == null;
 			boolean emptyConfiguration = (attributeValue.getAttribute()
 					.getConfiguration().isEmpty());
 			if (!nullConfiguration && !emptyConfiguration) {
 				JsonNode jsonNode = JsonNodeUtil.validate(attributeValue
 						.getAttribute().getConfiguration(),
 						"Invalid value for Attribute configuration: "
 								+ attributeValue.getAttribute()
 										.getConfiguration());
 				this.applyDefaultValueWhenAvailable(attributeValue, jsonNode);
 			}
 		}
 	}
 
 	private void applyDefaultValueWhenAvailable(AttributeValue attributeValue,
 			JsonNode jsonNode) {
 		if (jsonNode.has(AttributeTypeDeployer.DEFAULT_CONFIGURATION_NAME)) {
 			String defaultField = jsonNode.get(
 					AttributeTypeDeployer.DEFAULT_CONFIGURATION_NAME).asText();
 			if (attributeValue.getValue() == null && defaultField != null) {
 				attributeValue.setValue(defaultField);
 			}
 		}
 	}
 
 	private void validateTypeOfValue(AttributeValue attributeValue) {
 		if (attributeValue.getValue() != null) {
 
 			Attribute attribute = attributeValue.getAttribute();
 			AttributeType attributeType = attribute.getType();
 			AttributeTypeDeployer deployer = deployers
 					.get(attributeType.name());
 			java.lang.Class<?> attributeClass = deployer.getAttributeClass();
 
 			if (!attributeClass.isInstance(attributeValue.getValue())) {
 				throw new MetadataException(
 						"Invalid value for the Instance. The '"
 								+ attribute.getName()
 								+ "' attribute can only get values ​​of type "
 								+ attributeType);
 			}
 		}
 	}
 
 	public Instance findInstanceById(Long id) {
 		return this.instanceDao.findInstanceById(id);
 	}
}
