 package edu.wustl.bulkoperator.processor;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.beanutils.ConvertUtils;
 
 import edu.wustl.bulkoperator.appservice.AppServiceInformationObject;
 import edu.wustl.bulkoperator.csv.CsvReader;
 import edu.wustl.bulkoperator.metadata.Attribute;
 import edu.wustl.bulkoperator.metadata.BulkOperationClass;
 import edu.wustl.bulkoperator.metadata.DateValue;
 import edu.wustl.bulkoperator.util.BulkOperationConstants;
 import edu.wustl.bulkoperator.util.BulkOperationException;
 import edu.wustl.bulkoperator.util.BulkOperationUtility;
 import edu.wustl.common.exception.ErrorKey;
 import edu.wustl.common.util.global.Validator;
 import edu.wustl.common.util.logger.Logger;
 
 public abstract class AbstractBulkOperationProcessor {
 	
 	private static final Logger logger = Logger
 			.getCommonLogger(AbstractBulkOperationProcessor.class);
 	
 	protected BulkOperationClass bulkOperationClass = null;
 	protected AppServiceInformationObject serviceInformationObject = null;
 	private static CustomDateConverter converter=new CustomDateConverter();
 	static {
 		  ConvertUtils.register(converter, java.util.Date.class);
 	};
 	
 	public AbstractBulkOperationProcessor(
 			BulkOperationClass bulkOperationClass,
 			AppServiceInformationObject serviceInformationObject) {
 		this.bulkOperationClass = bulkOperationClass;
 		this.serviceInformationObject = serviceInformationObject;
 	}
 	public final BulkOperationClass getBulkOperationClass() {
 		return bulkOperationClass;
 	}
 
 	protected Object getEntityObject(CsvReader csvReader)
 			throws BulkOperationException {
 		Object staticObject = bulkOperationClass.getClassDiscriminator(csvReader,"");
 		if (staticObject == null) {
 			staticObject = bulkOperationClass.getNewInstance();
 		}
 		return staticObject;
 	}
 
 	abstract Object processObject(Map<String, String> csvData)
 			throws BulkOperationException;
 
 	/**
 	 * 
 	 * @param mainObj
 	 * @param migrationClass
 	 * @param columnSuffix
 	 * @param validate
 	 * @throws BulkOperationException
 	 */
 	public void processObject(Object mainObj,
 			BulkOperationClass migrationClass,CsvReader csvReader,
 			String columnSuffix, boolean validate, int csvRowNumber)
 			throws BulkOperationException {
 		if (migrationClass.getAttributeCollection() != null
 				&& !migrationClass.getAttributeCollection().isEmpty()) {
 			processAttributes(mainObj, migrationClass, csvReader, columnSuffix,
 					validate);
 		}
 
 		if (migrationClass.getContainmentAssociationCollection() != null
 				&& !migrationClass.getContainmentAssociationCollection()
 						.isEmpty()) {
 			processContainments(mainObj, migrationClass, csvReader, columnSuffix,
 					validate, csvRowNumber);
 		}
 
 		if (migrationClass.getReferenceAssociationCollection() != null
 				&& !migrationClass.getReferenceAssociationCollection()
 						.isEmpty()) {
 			processAssociations(mainObj, migrationClass, csvReader, columnSuffix,
 					validate, csvRowNumber);
 		}
 	}
 
 	/**
 	 * 
 	 * @param mainObj
 	 * @param mainMigrationClass
 	 * @param csvData
 	 * @param columnSuffix
 	 * @param validate
 	 * @throws BulkOperationException
 	 */
 	protected void processContainments(Object mainObj,
 			BulkOperationClass mainMigrationClass, CsvReader csvReader,
 			String columnSuffix, boolean validate, int csvRowNumber)
 			throws BulkOperationException {
 		try {
 			Iterator<BulkOperationClass> containmentItert = mainMigrationClass
 					.getContainmentAssociationCollection().iterator();
 			while (containmentItert.hasNext()) {
 				BulkOperationClass containmentMigrationClass = containmentItert
 						.next();
 				String cardinality = containmentMigrationClass.getCardinality();
 				if (cardinality != null && cardinality.equals("*")
 						&& !cardinality.equals("")) {
 					Collection containmentObjectCollection = (Collection) mainMigrationClass
 							.invokeGetterMethod(containmentMigrationClass
 									.getRoleName(), null, mainObj, null);
 					if (containmentObjectCollection == null) {
 						containmentObjectCollection = new LinkedHashSet();
 					}
 					List sortedList = new ArrayList(containmentObjectCollection);
 					containmentObjectCollection = new LinkedHashSet(sortedList);
 					int maxNoOfRecords = containmentMigrationClass
 							.getMaxNoOfRecords().intValue();
 					for (int i = 1; i <= maxNoOfRecords; i++) {
 						
 						if (validate||BulkOperationUtility
 								.checkIfAtLeastOneColumnHasAValueForInnerContainmentForStatic(csvRowNumber,
 										containmentMigrationClass,columnSuffix+ "#" + i, csvReader))
 						{
 						 Object containmentObject =null;
 						if(!validate)
 						{
 							containmentObject = containmentMigrationClass
 									.getClassDiscriminator(csvReader,
 											columnSuffix + "#" + i);// getNewInstance();
 						}			
 							if (containmentObject == null) {
 								if (BulkOperationConstants.JAVA_LANG_STRING_DATATYPE
 										.equals(containmentMigrationClass
 												.getClassName())) {
 									containmentObject = new StringBuffer();
 								} else {
 									containmentObject = containmentMigrationClass
 											.getNewInstance();
 								}
 							}
 							
 							processObject(containmentObject,
 									containmentMigrationClass, csvReader,
 									columnSuffix + "#" + i, validate,
 									csvRowNumber);
 							if (BulkOperationConstants.JAVA_LANG_STRING_DATATYPE
 									.equals(containmentMigrationClass
 											.getClassName())) {
 								containmentObjectCollection
 										.add(containmentObject.toString());
 							} else {
 								containmentObjectCollection
 										.add(containmentObject);
 							}
 							String roleName = containmentMigrationClass
 									.getParentRoleName();
 							if (!Validator.isEmpty(roleName)) {
 								BeanUtils.setProperty(containmentObject,roleName,mainObj);
 							}
 						}
 					}
 					String roleName = containmentMigrationClass.getRoleName();
 					
 					BeanUtils.setProperty(mainObj,roleName,containmentObjectCollection);
 				} else if (cardinality != null && cardinality.equals("1")
 						&& !cardinality.equals("")) {
 					
 					if ( validate||BulkOperationUtility.checkIfAtLeastOneColumnHasAValueForInnerContainmentForStatic(csvRowNumber,
 							containmentMigrationClass,columnSuffix, csvReader)) {
 						Object containmentObject = mainMigrationClass
 								.invokeGetterMethod(containmentMigrationClass
 										.getRoleName(), null, mainObj, null);
 						if (containmentObject == null) {
 							containmentObject = containmentMigrationClass
 									.getClassDiscriminator(csvReader,
 											columnSuffix);
 						}
 						if (containmentObject == null) {
 							Class klass = containmentMigrationClass
 									.getClassObject();
 							Constructor constructor = klass
 									.getConstructor(null);
 							containmentObject = constructor.newInstance();
 						}
 						processObject(containmentObject,
 								containmentMigrationClass, csvReader,
 								columnSuffix, validate, csvRowNumber);
 						String roleName = containmentMigrationClass
 								.getRoleName();
 						
 						BeanUtils.setProperty(mainObj,roleName,containmentObject);
 						
						String parentRoleName = containmentMigrationClass.getParentRoleName();
						if (!Validator.isEmpty(parentRoleName)) {
							BeanUtils.setProperty(containmentObject,parentRoleName,mainObj);
						}
 					}
 				}
 			}
 		} catch (BulkOperationException bulkExp) {
 			logger.error(bulkExp.getMessage(), bulkExp);
 			throw new BulkOperationException(bulkExp.getErrorKey(), bulkExp,
 					bulkExp.getMsgValues());
 		} catch (Exception exp) {
 			logger.error(exp.getMessage(), exp);
 			ErrorKey errorkey = ErrorKey.getErrorKey("bulk.operation.issues");
 			throw new BulkOperationException(errorkey, exp, exp.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 * @param mainObj
 	 * @param mainMigrationClass
 	 * @param csvData
 	 * @param columnSuffix
 	 * @param validate
 	 * @throws BulkOperationException
 	 */
 	private void processAssociations(Object mainObj,
 			BulkOperationClass mainMigrationClass,CsvReader csvReader,
 			String columnSuffix, boolean validate, int csvRowNumber)
 			throws BulkOperationException {
 		try {
 			Iterator<BulkOperationClass> associationItert = mainMigrationClass
 					.getReferenceAssociationCollection().iterator();
 			while (associationItert.hasNext()) {
 				BulkOperationClass associationMigrationClass = associationItert
 						.next();
 				String cardinality = associationMigrationClass.getCardinality();
 				if (cardinality != null && cardinality.equals("*")
 						&& !cardinality.equals("") && mainObj != null) {
 					Collection associationObjectCollection = (Collection) mainMigrationClass
 							.invokeGetterMethod(associationMigrationClass
 									.getRoleName(), null, mainObj, null);
 					if (associationObjectCollection == null) {
 						associationObjectCollection = new LinkedHashSet<Object>();
 					}
 					List sortedList = new ArrayList(associationObjectCollection);
 					associationObjectCollection = new LinkedHashSet(sortedList);
 					int maxNoOfRecords = associationMigrationClass
 							.getMaxNoOfRecords().intValue();
 					for (int i = 1; i <= maxNoOfRecords; i++) {
 						if ( validate||BulkOperationUtility
 								.checkIfAtLeastOneColumnHasAValueForInnerContainmentForStatic(csvRowNumber,
 										associationMigrationClass,columnSuffix+ "#" + i, csvReader)) {
 							Object referenceObject =null;
 							if(!validate)
 							{
 							 referenceObject = associationMigrationClass
 									.getClassDiscriminator(csvReader,
 											columnSuffix + "#" + i);
 							}
 							if (referenceObject == null) {
 								if (BulkOperationConstants.JAVA_LANG_STRING_DATATYPE
 										.equals(associationMigrationClass
 												.getClassName())) {
 									referenceObject = new StringBuffer();
 								} else {
 									referenceObject = associationMigrationClass
 											.getNewInstance();
 								}
 							}
 							processObject(referenceObject,
 									associationMigrationClass, csvReader,
 									columnSuffix + "#" + i, validate,
 									csvRowNumber);
 							if (BulkOperationConstants.JAVA_LANG_STRING_DATATYPE
 									.equals(associationMigrationClass
 											.getClassName())) {
 								associationObjectCollection.add(referenceObject
 										.toString());
 							} else {
 								associationObjectCollection
 										.add(referenceObject);
 							}
 							String roleName = associationMigrationClass
 									.getParentRoleName();
 							if (!Validator.isEmpty(roleName)) {
 								BeanUtils.setProperty(mainObj,roleName,referenceObject);
 							}
 						}
 					}
 					String roleName = associationMigrationClass.getRoleName();
 					
 					BeanUtils.setProperty(mainObj, roleName,associationObjectCollection);
 					
 				} else if (cardinality != null && cardinality.equals("1")
 						&& !cardinality.equals("")) {
 					List<String> attributeList = BulkOperationUtility
 							.getAttributeList(associationMigrationClass,
 									columnSuffix);
 					if (validate||BulkOperationUtility.checkIfAtLeastOneColumnHasAValueForInnerContainmentForStatic(csvRowNumber,
 							associationMigrationClass,columnSuffix, csvReader)) {
 						Object associatedObject = mainMigrationClass.invokeGetterMethod(associationMigrationClass
 										.getRoleName(), null, mainObj, null);
 						if (associatedObject == null) {
 							associatedObject = associationMigrationClass
 									.getClassDiscriminator(csvReader,
 											columnSuffix);
 						}
 						if (associatedObject == null) {
 							Class klass = associationMigrationClass
 									.getClassObject();
 							Constructor constructor = klass
 									.getConstructor(null);
 							associatedObject = constructor.newInstance();
 						}
 						processObject(associatedObject,
 								associationMigrationClass, csvReader,
 								columnSuffix, validate, csvRowNumber);
 						String roleName = associationMigrationClass
 								.getRoleName();
 						
 						BeanUtils.setProperty(mainObj, roleName,associatedObject);
						String parentRoleName = associationMigrationClass
						.getParentRoleName();
						if (!Validator.isEmpty(parentRoleName)) {
							BeanUtils.setProperty(associatedObject,parentRoleName,mainObj);
						}
 					}
 				}
 			}
 		} catch (BulkOperationException bulkExp) {
 			logger.error(bulkExp.getMessage(), bulkExp);
 			throw new BulkOperationException(bulkExp.getErrorKey(), bulkExp,
 					bulkExp.getMsgValues());
 		} catch (Exception exp) {
 			logger.error(exp.getMessage(), exp);
 			ErrorKey errorkey = ErrorKey.getErrorKey("bulk.operation.issues");
 			throw new BulkOperationException(errorkey, exp, exp.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 * @param mainObj
 	 * @param mainMigrationClass
 	 * @param csvData2
 	 * @param columnSuffix
 	 * @param validate
 	 * @throws BulkOperationException
 	 */
 	private void processAttributes(Object mainObj,
 			BulkOperationClass mainMigrationClass,CsvReader csvReader,
 			String columnSuffix, boolean validate)
 			throws BulkOperationException {
 		try {
 			Iterator<Attribute> attributeItertor = mainMigrationClass
 					.getAttributeCollection().iterator();
 			while (attributeItertor.hasNext()) {
 				Attribute attribute = attributeItertor.next();
 
 				if (!Arrays.asList(csvReader.getColumnNames()).contains(attribute.getCsvColumnName() + columnSuffix)) {
 					BulkOperationUtility.throwExceptionForColumnNameNotFound(
 							mainMigrationClass, validate, attribute);
 				} else if(!validate)
 				 {
 					if ("java.lang.String".equals(mainMigrationClass
 							.getClassName())) {
 						if (!Validator.isEmpty(csvReader.getColumn(attribute.getCsvColumnName()
 								+ columnSuffix))) {
 							String csvDataValue =csvReader.getColumn(attribute.getCsvColumnName()+ columnSuffix);
 							
 							Object attributeValue = attribute
 									.getValueOfDataType(csvDataValue, validate,
 											attribute.getCsvColumnName()
 													+ columnSuffix, mainMigrationClass
 													.getClassName());
 							((StringBuffer) mainObj).append(attributeValue);
 						}
 					} else if ("java.lang.Long".equals(mainMigrationClass
 							.getClassName())
 							|| "java.lang.Double".equals(mainMigrationClass
 									.getClassName())
 							|| "java.lang.Integer".equals(mainMigrationClass
 									.getClassName())
 							|| "java.lang.Boolean".equals(mainMigrationClass
 									.getClassName())
 							|| "java.lang.Float".equals(mainMigrationClass
 									.getClassName())) {
 						if (!Validator.isEmpty(csvReader.getColumn(attribute.getCsvColumnName()
 								+ columnSuffix))) {
 							csvReader.getColumn(attribute.getCsvColumnName()
 									+ columnSuffix);
 							mainObj = csvReader;
 						}
 					} else if (String.valueOf(mainObj.getClass()).contains(
 							attribute.getBelongsTo())) {
 						setValueToObject(mainObj, mainMigrationClass, csvReader,
 								columnSuffix, validate, attribute);
 					}// else if ends
 				}// null check if - else ends
 			}// while ends
 		} catch (BulkOperationException bulkExp) {
 			logger.error(bulkExp.getMessage(), bulkExp);
 			throw new BulkOperationException(bulkExp.getErrorKey(), bulkExp,
 					bulkExp.getMsgValues());
 		} catch (Exception exp) {
 			logger.error(exp.getMessage(), exp);
 			ErrorKey errorkey = ErrorKey.getErrorKey("bulk.operation.issues");
 			throw new BulkOperationException(errorkey, exp, exp.getMessage());
 		}
 	}
 
 	protected void setValueToObject(Object mainObj,
 			BulkOperationClass mainMigrationClass,CsvReader csvReader,
 			String columnSuffix, boolean validate, Attribute attribute) throws BulkOperationException {
 		if (!Validator.isEmpty(csvReader.getColumn(attribute.getCsvColumnName()
 				+ columnSuffix))) {
 			String csvDataValue = csvReader.getColumn(attribute.getCsvColumnName()
 					+ columnSuffix);
 			
 			try {
 				 
 				if(attribute.getFormat()!=null)
 				{
 					DateValue value = new DateValue(csvDataValue, attribute.getFormat());
 					BeanUtils.copyProperty(mainObj, attribute.getName(),value);
 				}
 				else
 				{
 					BeanUtils.copyProperty(mainObj, attribute.getName(),csvDataValue);
 				}
 				
 			} catch (IllegalAccessException exp) {
 				logger.error(exp.getMessage(), exp);
 				ErrorKey errorkey = ErrorKey.getErrorKey("bulk.operation.issues");
 				throw new BulkOperationException(errorkey, exp, exp.getMessage());
 			} catch (InvocationTargetException exp) {
 				logger.error(exp.getMessage(), exp);
 				ErrorKey errorkey = ErrorKey.getErrorKey("bulk.operation.issues");
 				throw new BulkOperationException(errorkey, exp, exp.getMessage());
 			}
 		}
 	}
 }
