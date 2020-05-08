 
 package edu.common.dynamicextensions.processor;
 
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.entitymanager.CategoryManager;
 import edu.common.dynamicextensions.entitymanager.CategoryManagerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 
 /**
  * This Class populates the DataEntryForm and saves the same into the Database.
  * @author chetan_patil
  */
 public class ApplyDataEntryFormProcessor extends BaseDynamicExtensionsProcessor
 {
 
 	/**
 	 * Default Constructor
 	 */
 
 	private Long userId;
 
 	public ApplyDataEntryFormProcessor()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * This method returns the instance of ApplyDataEntryFormProcessor.
 	 * @return ApplyDataEntryFormProcessor Instance of ApplyDataEntryFormProcessor
 	 */
 	public static ApplyDataEntryFormProcessor getInstance()
 	{
 		return new ApplyDataEntryFormProcessor();
 	}
 
 	/**
 	 * 
 	 * @param attributeValueMap
 	 * @return
 	 */
 	public Map<BaseAbstractAttributeInterface, Object> removeNullValueEntriesFormMap(
 			Map<BaseAbstractAttributeInterface, Object> attributeValueMap)
 	{
 		Set<Map.Entry<BaseAbstractAttributeInterface, Object>> attributeValueSet = attributeValueMap
 				.entrySet();
 		Iterator attributeValueSetIterator = attributeValueSet.iterator();
 		while (attributeValueSetIterator.hasNext())
 		{
 			Map.Entry<BaseAbstractAttributeInterface, Object> attributeValueEntry = (Map.Entry<BaseAbstractAttributeInterface, Object>) attributeValueSetIterator
 					.next();
 
 			Object value = attributeValueEntry.getValue();
 			if (value == null)
 			{
 				attributeValueSetIterator.remove();
 			}
 			else if (value instanceof List && ((List) value).isEmpty())
 			{
 				attributeValueSetIterator.remove();
 
 			}
 		}
 		return attributeValueMap;
 	}
 
 	/**
 	 * This method will pass the values entered into the controls to EntityManager to insert them in Database.
 	 * @param containerInterface The container of who's value of Control are to be populated. 
 	 * @param attributeValueMap The Map of Attribute and their corresponding values from controls.
 	 * @throws DynamicExtensionsApplicationException on Application exception
 	 * @throws DynamicExtensionsSystemException on System exception
 	 * @return recordIdentifier Record identifier of the last saved record. 
 	 */
 	public String insertDataEntryForm(ContainerInterface container,
 			Map<BaseAbstractAttributeInterface, Object> attributeValueMap)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Long recordIdentifier = null;
 		//quick fix: common manager interface should be used here
 		if (container.getAbstractEntity() instanceof CategoryEntityInterface)
 		{
 			CategoryInterface categoryInterface = ((CategoryEntityInterface) container
 					.getAbstractEntity()).getCategory();
 			CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 			Long categoryRecordId = categoryManager.insertData(categoryInterface,
 					attributeValueMap, userId);
 			recordIdentifier = categoryManager.getEntityRecordIdByRootCategoryEntityRecordId(
 					categoryRecordId, categoryInterface.getRootCategoryElement()
 							.getTableProperties().getName());
 		}
 		else
 		{
 			Map map = attributeValueMap;
 			EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 			recordIdentifier = entityManagerInterface.insertData((EntityInterface) container
 					.getAbstractEntity(), map, userId);
 		}
 
 		return recordIdentifier.toString();
 	}
 
 	/**
 	 * This method will pass the changed (modified) values entered into the controls to EntityManager to update them in Database.
 	 * @param container
 	 * @param attributeValueMap
 	 * @param recordIdentifier
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws SQLException 
 	 */
 	public Boolean editDataEntryForm(ContainerInterface container,
 			Map<BaseAbstractAttributeInterface, Object> attributeValueMap, Long recordIdentifier)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException,
 			SQLException
 	{
 		//Quick fix:
 		if (container.getAbstractEntity() instanceof EntityInterface)
 		{
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			EntityInterface entity = (Entity) container.getAbstractEntity();
 			//Correct this:
 			Map map = attributeValueMap;
			return entityManager.editData(entity, map, recordIdentifier, userId);
 		}
 		else
 		{
 			CategoryInterface categoryInterface = ((CategoryEntityInterface) container
 					.getAbstractEntity()).getCategory();
 			CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 			Long categoryRecordId = categoryManager.getRootCategoryEntityRecordIdByEntityRecordId(
 					recordIdentifier, categoryInterface.getRootCategoryElement()
 							.getTableProperties().getName());
 			return CategoryManager.getInstance().editData(
 					(CategoryEntityInterface) container.getAbstractEntity(), attributeValueMap,
 					categoryRecordId, userId);
 		}
 
 	}
 
 	public Long getUserId()
 	{
 		return userId;
 	}
 
 	public void setUserId(Long userId)
 	{
 		this.userId = userId;
 	}
 
 }
