 /*
  * Created on Nov 16, 2006
  * @author
  *
  */
 
 package edu.common.dynamicextensions.processor;
 
 import javax.servlet.http.HttpServletRequest;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.interfaces.GroupUIBeanInterface;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants;
 
 /**
  * @author preeti_munot
  *
  */
 public class ApplyGroupDefinitionProcessor extends BaseDynamicExtensionsProcessor
 {
 
 	/**
 	 *
 	 */
 	private ApplyGroupDefinitionProcessor()
 	{
 		super();
 	}
 
 	/**
 	 * 
 	 * @return new instance of ApplyGroupDefinitionProcessor
 	 */
 	public static ApplyGroupDefinitionProcessor getInstance()
 	{
 		return new ApplyGroupDefinitionProcessor();
 	}
 
 	/**
 	 * @param containerObject Container Object
 	 * @param groupUIBean
 	 * @param operationMode 
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public EntityGroupInterface saveGroupDetails(GroupUIBeanInterface groupUIBean,
 			ContainerInterface containerObject, String operationMode, HttpServletRequest request)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface container=containerObject;
 		GroupProcessor groupProcessor = GroupProcessor.getInstance();
 		String groupOperation = groupUIBean.getGroupOperation();
 		EntityGroupInterface objEntityGroup = null;
 
 		//Use existing group
 		String createGroupAs = groupUIBean.getCreateGroupAs();
 		if ((createGroupAs != null)
 				&& (createGroupAs.equals(ProcessorConstants.GROUP_CREATEFROM_EXISTING)))
 		{
 			//entityGroup = groupProcessor.getEntityGroupByIdentifier(groupUIBean.getGroupName());
			if (container != null)
 			{
 				EntityInterface entity = (EntityInterface) container.getAbstractEntity();
 				EntityGroupInterface existingEntityGroup = DynamicExtensionsUtility
 						.getEntityGroup(entity);
 				if (operationMode.equals(DEConstants.EDIT_FORM))
 				{
 					existingEntityGroup.setDescription(groupUIBean.getGroupDescription());
 					if (existingEntityGroup.getId() != Long.parseLong(groupUIBean.getGroupName()))
 					{
 						//Remove entity form existing group 
 						existingEntityGroup.removeEntity(entity);
 						existingEntityGroup.removeMainContainer(container);
 
 						//create changed Entity group
 						EntityGroupInterface newEntityGroup = DynamicExtensionsUtility
 								.getEntityGroupByIdentifier(groupUIBean.getGroupName());
 						newEntityGroup.setDescription(groupUIBean.getGroupDescription());
 						newEntityGroup.addMainContainer(container);
 						newEntityGroup.addEntity(entity);
 						entity.setEntityGroup(newEntityGroup);
 						container.setAbstractEntity(entity);
 
 						objEntityGroup = newEntityGroup;
 
 						//Save to DB that now the form is related to another Entitygroup 
 						//Save only if its not shownext page operation
 						if ((groupOperation != null)
 								&& !(groupOperation.equals(ProcessorConstants.SHOW_NEXT_PAGE)))
 						{
 							groupProcessor.saveEntityGroup(existingEntityGroup);
 							groupProcessor.saveEntityGroup(objEntityGroup);
 							ContainerProcessor.getInstance().saveContainer(container);
 						}
 					}
 					else
 					{
 						//If group selected is same as old group then assign the existing entity group
 						objEntityGroup = existingEntityGroup;
 					}
 				}
 			}
			else
			{
				objEntityGroup = groupProcessor.getEntityGroupByIdentifier(groupUIBean
						.getGroupName());
				container = DomainObjectFactory.getInstance().createContainer();
				container.setCaption("New Form");
				CacheManager.addObjectToCache(request, DEConstants.CONTAINER_INTERFACE, container);
				CacheManager.addObjectToCache(request, DEConstants.ENTITYGROUP_INTERFACE,
						objEntityGroup);
			}
 		}
 		else if ((createGroupAs != null)
 				&& (createGroupAs.equals(ProcessorConstants.GROUP_CREATEAS_NEW))
 				&& operationMode.equals(DEConstants.EDIT_FORM))
 		{
 			//This is the case where Form is already is within one group.But in Edit mode the form Group is created newly
 			//In this case ,new group requires to create ,the entity and container set to it
 			//Remove container and entity from old group 
 			if (container != null)
 			{
 				EntityInterface entity = (EntityInterface) container.getAbstractEntity();
 				EntityGroupInterface existingEntityGroup = DynamicExtensionsUtility
 						.getEntityGroup(entity);
 				if (operationMode.equals(DEConstants.EDIT_FORM))
 				{
 					//check whether groupname already exist
 					DynamicExtensionsUtility.validateName(groupUIBean.getGroupNameText());
 					//Remove entity form existing group 
 					existingEntityGroup.removeEntity(entity);
 					existingEntityGroup.removeMainContainer(container);
 
 					//Create new entitygroup and assign container to it.
 					EntityGroupInterface newEntityGroup = groupProcessor.createEntityGroup();
 					groupProcessor.populateEntityGroupDetails(newEntityGroup, groupUIBean);
 					newEntityGroup.addMainContainer(container);
 					newEntityGroup.addEntity(entity);
 					entity.setEntityGroup(newEntityGroup);
 					container.setAbstractEntity(entity);
 					objEntityGroup = newEntityGroup;
 					//Save only if its not shownext page operation
 					if ((groupOperation != null)
 							&& !(groupOperation.equals(ProcessorConstants.SHOW_NEXT_PAGE)))
 					{
 						groupProcessor.saveEntityGroup(existingEntityGroup);
 						objEntityGroup = groupProcessor.saveEntityGroup(newEntityGroup);
 						ContainerProcessor.getInstance().saveContainer(container);
 					}
 				}
 			}
 		}
 		else
 		{
 			//Create new entity group and validate entity group name
 			DynamicExtensionsUtility.validateName(groupUIBean.getGroupNameText());
 			objEntityGroup = groupProcessor.createEntityGroup();
 			groupProcessor.populateEntityGroupDetails(objEntityGroup, groupUIBean);
 		}
 
 		if (createGroupAs.equals(ProcessorConstants.GROUP_CREATEAS_NEW)
 				&& (groupOperation != null) && (groupOperation.equals(ProcessorConstants.SAVE_GROUP)))
 		{
 		
 				//Save to DB 
 				objEntityGroup = groupProcessor.saveEntityGroup(objEntityGroup);
 		}
 		return objEntityGroup;
 	}
 
 	/**
 	 * @param groupForm
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public void updateEntityGroup(ContainerInterface container, EntityGroupInterface entityGroup,
 			GroupUIBeanInterface groupUIBean) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		EntityInterface entity = null;
 
 		if ((entityGroup != null) && (container != null))
 		{
 			entity = (Entity) container.getAbstractEntity();
 			if (entity != null)
 			{
 				//entity.removeAllEntityGroups();
 				entity.setEntityGroup(entityGroup);
 			}
 
 			//Save to DB
 			String groupOperation = groupUIBean.getGroupOperation();
 			if ((groupOperation != null) && (groupOperation.equals(ProcessorConstants.SAVE_GROUP)))
 			{
 				//Save to DB
 				EntityManager.getInstance().persistEntity(entity);
 			}
 		}
 	}
 }
