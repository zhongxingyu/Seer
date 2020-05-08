 
 package edu.wustl.bulkoperator.processor;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.BeanUtils;
 
 import edu.wustl.bulkoperator.appservice.AbstractBulkOperationAppService;
 import edu.wustl.bulkoperator.appservice.AppServiceInformationObject;
 import edu.wustl.bulkoperator.csv.CsvReader;
 import edu.wustl.bulkoperator.csv.impl.CsvFileReader;
 import edu.wustl.bulkoperator.metadata.Attribute;
 import edu.wustl.bulkoperator.metadata.BulkOperationClass;
 import edu.wustl.bulkoperator.metadata.DateValue;
 import edu.wustl.bulkoperator.metadata.HookingInformation;
 import edu.wustl.bulkoperator.util.BulkOperationException;
 import edu.wustl.bulkoperator.util.BulkOperationUtility;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.exception.ErrorKey;
 import edu.wustl.common.util.global.Validator;
 import edu.wustl.common.util.logger.Logger;
 
 public class DynCategoryBulkOperationProcessor extends AbstractBulkOperationProcessor
 		implements
 		IBulkOperationProcessor
 {
 
 	private static final Logger logger = Logger
 			.getCommonLogger(DynCategoryBulkOperationProcessor.class);
 
 	public DynCategoryBulkOperationProcessor(BulkOperationClass dynExtCategoryBOClass,
 			AppServiceInformationObject serviceInformationObject)
 	{
 		super(dynExtCategoryBOClass, serviceInformationObject);
 	}
 
 	public Object process(CsvReader csvReader, int csvRowCounter,
 			SessionDataBean sessionDataBean) throws BulkOperationException, Exception
 	{
 
 		HashMap<String, Object> dynExtObject = new HashMap<String, Object>();
 		Long recordEntryId = null;
 		try
 		{
 			AbstractBulkOperationAppService bulkOprAppService = AbstractBulkOperationAppService
 					.getInstance(serviceInformationObject.getServiceImplementorClassName(), true,
 							serviceInformationObject.getUserName(), null);
 			processObject(dynExtObject, bulkOperationClass, csvReader, "", false, csvRowCounter);
 			HookingInformation hookingInformationFromTag = bulkOperationClass.getHookingInformation();
 			getinformationForHookingData(csvReader, hookingInformationFromTag);
 			Long recordId = bulkOprAppService
 					.insertData(bulkOperationClass.getClassName(), dynExtObject);
 			hookingInformationFromTag.setCategoryName(bulkOperationClass.getClassName());
 			hookingInformationFromTag.setDynamicExtensionObjectId(recordId);
 			hookingInformationFromTag.setSessionDataBean(sessionDataBean);
 			recordEntryId = bulkOprAppService.hookStaticDEObject(hookingInformationFromTag);
 		}
 		catch (BulkOperationException bulkOprExp)
 		{
 			logger.error(bulkOprExp.getMessage(), bulkOprExp);
 			throw bulkOprExp;
 		}
 		catch (Exception exp)
 		{
 			logger.error(exp.getMessage(), exp);
 			throw exp;
 		}
 		return recordEntryId;
 	}
 
 	@Override
 	Object processObject(Map<String, String> csvData) throws BulkOperationException
 	{
 		return null;
 	}
 
 	protected void setValueToObject(Object mainObj,
 			BulkOperationClass mainMigrationClass,CsvReader csvReader,
 			String columnSuffix, boolean validate, Attribute attribute) throws BulkOperationException {
 		if (!Validator.isEmpty(csvReader.getColumn(attribute.getCsvColumnName()
 				+ columnSuffix))) {
 			String csvDataValue = csvReader.getColumn(attribute.getCsvColumnName()
 					+ columnSuffix);
 			Map<String, Object> categoryDataValueMap = (Map<String, Object>) mainObj;
			try {
				if (csvDataValue == null || "".equals(csvDataValue))
				{
					categoryDataValueMap.put(attribute.getName(), "");
				}
				else
				{	
					if(attribute.getFormat()!=null)
					{
						DateValue value = new DateValue(csvDataValue, attribute.getFormat());
						BeanUtils.copyProperty(mainObj, attribute.getName(),value);
					}
					else
					{
						categoryDataValueMap.put(attribute.getName(), csvDataValue);
					}
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
 
 	/**
 	 *
 	 * @param mainObj
 	 * @param bulkOperationClass
 	 * @param csvData
 	 * @param columnSuffix
 	 * @param validate
 	 * @throws BulkOperationException
 	 */
 	protected void processContainments(Object mainObj, BulkOperationClass bulkOperationClass,
 			CsvReader csvReader, String columnSuffix, boolean validate, int csvRowNumber)
 			throws BulkOperationException
 	{
 		try
 		{
 			Map<String, Object> categoryDataValueMap = (Map<String, Object>) mainObj;
 
 			Iterator<BulkOperationClass> containmentItert = bulkOperationClass
 					.getContainmentAssociationCollection().iterator();
 			while (containmentItert.hasNext())
 			{
 				BulkOperationClass containmentObjectCollection = containmentItert.next();
 				if (containmentObjectCollection.getCardinality() != null)
 				{
 					List<Map<Long, Object>> list = new ArrayList<Map<Long, Object>>();
 
 					int maxNoOfRecords = containmentObjectCollection.getMaxNoOfRecords().intValue();
 					for (int i = 1; i <= maxNoOfRecords; i++)
 					{
 						if (BulkOperationUtility.checkIfAtLeastOneColumnHasAValueForInnerContainment(csvRowNumber,containmentObjectCollection,
 										columnSuffix + "#" + i,csvReader))
 						{
 							Object obj = new HashMap<Long, Object>();
 							processObject(obj, containmentObjectCollection, csvReader, columnSuffix
 									+ "#" + i, validate, csvRowNumber);
 							list.add((Map<Long, Object>) obj);
 						}
 						categoryDataValueMap.put(containmentObjectCollection.getClassName(), list);
 					}
 				}
 			}
 		}
 		catch (BulkOperationException bulkExp)
 		{
 			logger.error(bulkExp.getMessage(), bulkExp);
 			throw new BulkOperationException(bulkExp.getErrorKey(), bulkExp, bulkExp.getMsgValues());
 		}
 		catch (Exception exp)
 		{
 			logger.error(exp.getMessage(), exp);
 			ErrorKey errorkey = ErrorKey.getErrorKey("bulk.operation.issues");
 			throw new BulkOperationException(errorkey, exp, exp.getMessage());
 		}
 	}
 	/**
 	 * 
 	 * @param mainMigrationClass
 	 * @param validate
 	 * @param attribute
 	 * @throws BulkOperationException
 	 */
 	protected void getinformationForHookingData(CsvReader csvReader,
 			HookingInformation hookingInformation)
 			throws ClassNotFoundException, BulkOperationException {
 		Iterator<Attribute> attributeItertor = hookingInformation
 				.getAttributeCollection().iterator();
 		Map<String, Object> map = new HashMap<String, Object>();
 		while (attributeItertor.hasNext()) {
 			Attribute attribute = attributeItertor.next();
 
 			if (!Validator.isEmpty(csvReader.getColumn(attribute.getCsvColumnName()))) {
 				String csvDataValue = csvReader.getColumn(attribute.getCsvColumnName());
 				
 				map.put(attribute.getName(), csvDataValue);
 			}
 		}
 		hookingInformation.setDataHookingInformation(map);
 	}
 
 }
