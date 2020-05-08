 package com.nanuvem.lom.kernel;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import com.nanuvem.lom.kernel.dao.AttributeDao;
 import com.nanuvem.lom.kernel.dao.ClassDao;
 import com.nanuvem.lom.kernel.dao.DaoFactory;
 
 public class AttributeServiceImpl {
 
 	private ClassDao clazzDao;
 	private AttributeDao attributeDao;
 
 	private final Integer MINIMUM_VALUE_FOR_THE_ATTRIBUTE_SEQUENCE = 1;
 
 	private final String TRUE_VALUE_FOR_THE_ATTRIBUTE_CONFIGURATION = "{mandatory:true}";
 	private final String FALSE_VALUE_FOR_THE_ATTRIBUTE_CONFIGURATION = "{mandatory:false}";
 
 	public AttributeServiceImpl(DaoFactory dao) {
 		this.clazzDao = dao.createClassDao();
 		this.attributeDao = dao.createAttributeDao();
 	}
 
 	private void validate(Attribute attribute) {
 		Class clazz = clazzDao.findClassById(attribute.getClazz().getId());
 		int currentNumberOfAttributes = clazz.getAttributes().size();
 
 		if (attribute.getSequence() != null) {
 			boolean minValueForSequence = attribute.getSequence() < MINIMUM_VALUE_FOR_THE_ATTRIBUTE_SEQUENCE;
 			boolean maxValueForSequence = currentNumberOfAttributes + 1 < attribute
 					.getSequence();
 
 			if (minValueForSequence || maxValueForSequence) {
 				throw new MetadataException(
 						"Invalid value for Attribute sequence: "
 								+ attribute.getSequence());
 			}
 		}
 
 		if (attribute.getSequence() == null) {
 			attribute.setSequence(currentNumberOfAttributes + 1);
 		}
 
 		if (attribute.getName() == null || attribute.getName().isEmpty()) {
 			throw new MetadataException("The name of a Attribute is mandatory");
 		}
 
		if (!Pattern.matches("[a-zA-Z1-9]", attribute.getName())) {
 			throw new MetadataException("Invalid value for Attribute name: "
 					+ attribute.getName());
 		}
 
 		if (attribute.getType() == null) {
 			throw new MetadataException("The type of a Attribute is mandatory");
 		}
 
 		boolean isValueValidForConfiguration = !(attribute.getConfiguration() == null
 				|| attribute.getConfiguration().isEmpty()
 				|| (attribute.getConfiguration()
 						.equals(TRUE_VALUE_FOR_THE_ATTRIBUTE_CONFIGURATION)) || (attribute
 				.getConfiguration()
 				.equals(FALSE_VALUE_FOR_THE_ATTRIBUTE_CONFIGURATION)));
 
 		if (isValueValidForConfiguration) {
 			throw new MetadataException(
 					"Invalid value for Attribute configuration: "
 							+ attribute.getConfiguration());
 		}
 
 	}
 
 	public void create(Attribute attribute) {
 		this.validate(attribute);
 		this.attributeDao.create(attribute);
 	}
 
 	public List<Attribute> listAllAttributes(String fullClassName) {
 		return this.attributeDao.listAllAttributes(fullClassName);
 	}
 
 	public Attribute findAttributeById(Long id) {
 		if (id != null) {
 			return this.attributeDao.findAttributeById(id);
 		} else {
 			return null;
 		}
 	}
 
 	public Attribute findAttributeByNameAndClassFullname(String nameAttribute,
 			String fullnameClass) {
 
 		if ((nameAttribute != null && !nameAttribute.isEmpty())
 				&& (fullnameClass != null && !fullnameClass.isEmpty())) {
 			if (!fullnameClass.contains(".")) {
 				fullnameClass = "default." + fullnameClass;
 			}
 
 			return this.attributeDao.findAttributeByNameAndFullnameClass(
 					nameAttribute, fullnameClass);
 		}
 		return null;
 	}
 }
