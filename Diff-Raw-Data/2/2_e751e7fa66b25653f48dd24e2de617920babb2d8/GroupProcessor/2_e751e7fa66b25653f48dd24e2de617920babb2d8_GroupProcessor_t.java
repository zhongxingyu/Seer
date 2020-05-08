 /*
  * Created on Nov 16, 2006
  * @author
  *
  */
 
 package edu.common.dynamicextensions.processor;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.entitymanager.EntityGroupManager;
 import edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.interfaces.GroupUIBeanInterface;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 
 /**
  * @author preeti_munot
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class GroupProcessor extends BaseDynamicExtensionsProcessor
 {
 
 	/**
 	 * 
 	 *
 	 */
 	private GroupProcessor()
 	{
 
 	}
 
 	/**
 	 * 
 	 * @return new instance of GroupProcessor
 	 */
 	public static GroupProcessor getInstance()
 	{
 		return new GroupProcessor();
 	}
 
 	/**
 	 * 
 	 * @return new object of entity group 
 	 */
 	public EntityGroupInterface createEntityGroup()
 	{
 		return DomainObjectFactory.getInstance().createEntityGroup();
 	}
 
 	/**
 	 * 
 	 * @param entityGroup : Entity Group object to be populated
 	 * @param groupUIBean : Group UI Bean containing information entered by the user
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws DynamicExtensionsApplicationException 
 	 */
 	public void populateEntityGroupDetails(EntityGroupInterface entityGroup,
 			GroupUIBeanInterface groupUIBean) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		if ((entityGroup != null) && (groupUIBean != null))
 		{
 			entityGroup.setName(groupUIBean.getGroupNameText());
 			entityGroup.setDescription(groupUIBean.getGroupDescription());
			entityGroup.setIsSystemGenerated(Boolean.FALSE);
 			EntityGroupManager.getInstance().checkForDuplicateEntityGroupName(entityGroup);
 		}
 	}
 
 	/**
 	 * 
 	 * @param groupUIBean : Group UI Bean object to be populated
 	 * @param entityGroup : Entity Group object containing group details
 	 */
 	public void populategroupUIBeanDetails(GroupUIBeanInterface groupUIBean,
 			EntityGroupInterface entityGroup)
 	{
 		if ((entityGroup != null) && (groupUIBean != null))
 		{
 			groupUIBean.setGroupNameText(entityGroup.getName());
 			groupUIBean.setGroupDescription(entityGroup.getDescription());
 		}
 	}
 
 	/**
 	 * Create entity group object, populate its details and save to DB
 	 * @param groupUIBean  : Bean containing group information added by user on UI
 	 * @return
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public EntityGroupInterface createAndSaveGroup(GroupUIBeanInterface groupUIBean)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		EntityGroupInterface entityGroup = createEntityGroup();
 		populateEntityGroupDetails(entityGroup, groupUIBean);
 		entityGroup = saveEntityGroup(entityGroup);
 		return entityGroup;
 	}
 
 	/**
 	 * 
 	 * @param entityGroup Entity group to be saved to the DB
 	 * @return
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public EntityGroupInterface saveEntityGroup(EntityGroupInterface entityGroupInterface)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 		EntityGroupInterface savedEntityGroup = entityGroupManager
 				.persistEntityGroup(entityGroupInterface);
 		return savedEntityGroup;
 	}
 
 	/**
 	 * 
 	 * @param groupName Name of the group
 	 * @return EntityGroup with given name
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public EntityGroupInterface getEntityGroupByIdentifier(String entityGroupIdentifier)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		return DynamicExtensionsUtility.getEntityGroupByIdentifier(entityGroupIdentifier);
 	}
 
 }
