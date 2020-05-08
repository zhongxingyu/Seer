 package com.nanuvem.lom.kernel;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.nanuvem.lom.kernel.dao.AttributeValueDao;
 import com.nanuvem.lom.kernel.dao.DaoFactory;
 import com.nanuvem.lom.kernel.dao.InstanceDao;
 import com.nanuvem.lom.kernel.validator.deployer.AttributeTypeDeployer;
 
 public class InstanceServiceImpl {
 
 	private InstanceDao instanceDao;
 	private AttributeValueDao attributeValueDao;
 	private ClassServiceImpl classService;
 
 	InstanceServiceImpl(DaoFactory daoFactory, ClassServiceImpl classService) {
 		this.classService = classService;
 		this.attributeValueDao = daoFactory.createAttributeValueDao();
 		this.instanceDao = daoFactory.createInstanceDao();
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
 			try {
 				ObjectMapper objectMapper = new ObjectMapper();
 				JsonFactory factory = objectMapper.getJsonFactory();
 				JsonNode jsonNode = objectMapper.readTree(factory
 						.createJsonParser(attributeValue.getAttribute()
 								.getConfiguration()));
 
 				if (jsonNode
 						.has(AttributeTypeDeployer.DEFAULT_CONFIGURATION_NAME)) {
 					String defaultField = jsonNode.get(
 							AttributeTypeDeployer.DEFAULT_CONFIGURATION_NAME)
 							.asText();
 					if (attributeValue.getValue() == null
 							&& defaultField != null) {
 						attributeValue.setValue(defaultField);
 					}
 				}
 			} catch (Exception e) {
 				throw new MetadataException(
 						"Invalid value for Attribute configuration: "
 								+ attributeValue.getAttribute()
										.getConfiguration());
 			}
 		}
 	}
 
 	public Instance findInstanceById(Long id) {
 		return this.instanceDao.findInstanceById(id);
 	}
 }
