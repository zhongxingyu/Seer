 
 package edu.common.dynamicextensions.processor;
 
 /**
  * This processor class mainly helps the action class to call the related Object driven processors
  * to update the Actionforms by retriving data form Cache.
  * @author deepti_shelar
  */
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.webui.actionform.FormDefinitionForm;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 
 public class ApplyFormDefinitionProcessor extends BaseDynamicExtensionsProcessor
 {
 
 	/**
 	 * Default Constructor.
 	 */
 	protected ApplyFormDefinitionProcessor()
 	{
 	}
 
 	/**
 	 * Returns the instance of ApplyFormDefinitionProcessor.
 	 * @return ApplyFormDefinitionProcessor
 	 */
 	public static ApplyFormDefinitionProcessor getInstance()
 	{
 		return new ApplyFormDefinitionProcessor();
 	}
 
 	/**
 	 * This method creates a Container if not present in cache. Then it will call to ContainerProcessor will
 	 * populate this Object with the data from actionform.Then EntityProcessor's methods will be called to either create and Populate
 	 * or create and save the entity, Then finally this entity is added to the container.
 	 * @param containerInterface : Container object
 	 * @param actionForm : Form object
 	 * @param isActionSave : flag stating whether the object is to be saved to DB
 	 * @return ContainerInterface : Container object
 	 * @throws DynamicExtensionsApplicationException :Exception thrown by Entity Manager
 	 * @throws DynamicExtensionsSystemException :Exception thrown by Entity Manager
 	 */
 	public ContainerInterface addEntityToContainer(ContainerInterface containerInterface, FormDefinitionForm actionForm,
 			EntityGroupInterface entityGroupInterface) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 		if (containerInterface == null)
 		{
 			containerInterface = containerProcessor.createContainer();
 		}
 		//validate container name
		DynamicExtensionsUtility.validateName(actionForm.getFormName());
		containerProcessor.populateContainerInterface(containerInterface, actionForm);
 
 		//Add entity
 		EntityProcessor entityProcessor = EntityProcessor.getInstance();
 		EntityInterface entityInterface = containerInterface.getEntity();
 		if (entityInterface == null)
 		{
 			//validate entity name
 			DynamicExtensionsUtility.validateName(actionForm.getFormName());
 			entityInterface = entityProcessor.createAndPopulateEntity(actionForm);
 		}
 		else
 		{
 			entityProcessor.populateEntity(actionForm, entityInterface);
 		}
 		containerInterface.setEntity(entityInterface);
 
 		if (entityGroupInterface != null)
 		{
 			associateEntityToGroup(entityGroupInterface, containerInterface.getEntity());
 		}
 		containerProcessor.populateContainerInterface(containerInterface, actionForm);
 		if(containerInterface.getBaseContainer() != null)
 		{
 			EntityInterface parentEntity = containerInterface.getBaseContainer().getEntity();
 			entityInterface.setParentEntity(parentEntity);
 		}
 
 		return containerInterface;
 	}
 
 	/**
 	 * @param formDefinitionForm
 	 * @param containerInterface
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 *
 	 */
 	public AssociationInterface associateEntity(AssociationInterface association, ContainerInterface sourceContainer,
 			ContainerInterface targetContainer, FormDefinitionForm formDefinitionForm) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if ((formDefinitionForm != null) && (targetContainer != null) && (sourceContainer != null))
 		{
 			EntityInterface targetEntity = targetContainer.getEntity();
 			EntityInterface sourceEntity = sourceContainer.getEntity();
 			if ((sourceEntity != null) && (targetEntity != null))
 			{
 				String viewAs = formDefinitionForm.getViewAs();
 				if ((viewAs != null) && (viewAs.equals(ProcessorConstants.VIEW_AS_FORM)))
 				{
 					association = associateEntity(association, AssociationType.CONTAINTMENT, sourceEntity, targetEntity, Cardinality.ONE,
 							Cardinality.ONE);
 				}
 				else if ((viewAs != null) && (viewAs.equals(ProcessorConstants.VIEW_AS_SPREADSHEET)))
 				{
 					association = associateEntity(association, AssociationType.CONTAINTMENT, sourceEntity, targetEntity, Cardinality.ONE,
 							Cardinality.MANY);
 				}
 			}
 		}
 		return association;
 	}
 
 	/**
 	 * @return
 	 */
 	public AssociationInterface createAssociation()
 	{
 		return DomainObjectFactory.getInstance().createAssociation();
 	}
 
 	/**
 	 * @param formDefinitionForm
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private ContainerInterface createTargetEntityContainer(FormDefinitionForm formDefinitionForm) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		//validate container name b4 creating it
 		ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 		DynamicExtensionsUtility.validateName(formDefinitionForm.getFormName());
 		ContainerInterface containerInterface = containerProcessor.createContainer();
 		containerProcessor.populateContainerInterface(containerInterface, formDefinitionForm);
 
 		//Add entity
 		EntityProcessor entityProcessor = EntityProcessor.getInstance();
 		EntityInterface entityInterface = entityProcessor.createAndPopulateEntity(formDefinitionForm);

 		//Add entity to container
 		containerInterface.setEntity(entityInterface);
 		return containerInterface;
 	}
 
 	/**
 	 * @param associationType
 	 * @param sourceEntity
 	 * @param targetEntity
 	 * @param sourceCardinality
 	 * @param targetCardinality
 	 */
 	private AssociationInterface associateEntity(AssociationInterface association, AssociationType associationType, EntityInterface sourceEntity,
 			EntityInterface targetEntity, Cardinality sourceCardinality, Cardinality targetCardinality)
 	{
 		if (association == null)
 		{
 			association = DomainObjectFactory.getInstance().createAssociation();
 		}
 		association.setTargetEntity(targetEntity);
 		association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 		association.setName(targetEntity.getName());
 		association.setSourceRole(getRole(associationType, null, Cardinality.ONE, sourceCardinality));
 		association.setTargetRole(getRole(associationType, targetEntity.getName(), Cardinality.ONE, targetCardinality));
 		sourceEntity.addAssociation(association);
 		return association;
 	}
 
 	/**
 	 * @param selectedObjectId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ContainerInterface getTargetEntityContainer(String targetContainerId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface targetContainer = null;
 		if ((targetContainerId != null) && (!targetContainerId.trim().equals("")))
 		{
 			targetContainer = DynamicExtensionsUtility.getContainerByIdentifier(targetContainerId);
 		}
 		return targetContainer;
 	}
 
 	private RoleInterface getRole(AssociationType associationType, String name, Cardinality minCard, Cardinality maxCard)
 	{
 		RoleInterface role = DomainObjectFactory.getInstance().createRole();
 		role.setAssociationsType(associationType);
 		role.setName(name);
 		role.setMinimumCardinality(minCard);
 		role.setMaximumCardinality(maxCard);
 		return role;
 	}
 
 	/**
 	 * @param entityGroup : Entity Group containing entity
 	 * @param entity : Entity to be associated
 	 */
 	public void associateEntityToGroup(EntityGroupInterface entityGroup, EntityInterface entity)
 	{
 		if ((entityGroup != null) && (entity != null))
 		{
 			//entity.removeAllEntityGroups();
 			entityGroup.addEntity(entity);
 			((EntityGroup)entityGroup).setCurrent(true);
 			entity.addEntityGroupInterface(entityGroup);
 		}
 	}
 
 	/**
 	 * @param mainFormContainer
 	 * @param subFormContainer
 	 */
 	public void addSubFormControlToContainer(ContainerInterface mainFormContainer, ContainerInterface subFormContainer,
 			AssociationInterface association)
 	{
 		if ((mainFormContainer != null) && (subFormContainer != null))
 		{
 			ControlProcessor controlProcessor = ControlProcessor.getInstance();
 			ControlInterface subFormControl = controlProcessor.createContainmentAssociationControl(subFormContainer, association);
 			subFormControl.setSequenceNumber(WebUIManager.getSequenceNumberForNextControl(mainFormContainer));
 			if (subFormControl != null)
 			{
 				mainFormContainer.addControl(subFormControl);
 			}
 		}
 	}
 
 	/**
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public ContainerInterface getSubFormContainer(FormDefinitionForm formDefinitionForm, ContainerInterface mainFormContainer)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface targetEntityContainer = null;
 		if (formDefinitionForm != null)
 		{
 			//Get target entity container
 			Long selectedContainerId = new Long(0);
 			if (formDefinitionForm.getSelectedObjectId() != null && formDefinitionForm.getSelectedObjectId().trim().length() != 0)
 			{
 				selectedContainerId = new Long(formDefinitionForm.getSelectedObjectId());
 			}
 
 			String createAs = formDefinitionForm.getCreateAs();
 			if ((createAs != null) && (createAs.equals(ProcessorConstants.CREATE_FROM_EXISTING)))
 			{
 				List<ContainerInterface> listOfContainers = new ArrayList<ContainerInterface>();
 				getSelectedContainer(mainFormContainer, selectedContainerId,listOfContainers);
 				if (!listOfContainers.isEmpty())
 				{
 					targetEntityContainer = listOfContainers.get(0);
 				}
 				if (targetEntityContainer == null)
 				{
 					targetEntityContainer = getTargetEntityContainer(formDefinitionForm.getSelectedObjectId());
 					List<ContainerInterface> childContainerList = getAllChildContainers(mainFormContainer);
 					updateReferences(targetEntityContainer, childContainerList);
 
 					//childContainerList.add(0, mainFormContainer);
 					//updateEntityGroupReferences(targetEntityContainer, mainFormContainer);
 				}
 			}
 			else if ((createAs != null) && (createAs.equals(ProcessorConstants.CREATE_AS_NEW)))
 			{
 				targetEntityContainer = createTargetEntityContainer(formDefinitionForm);
 			}
 		}
 		return targetEntityContainer;
 	}
 
 	/**
 	 * @param targetContainer
 	 * @param childContainerList
 	 */
 	private void updateReferences(ContainerInterface targetContainer, List<ContainerInterface> childContainerList)
 	{
 		Collection<ControlInterface> targetControlCollection = targetContainer.getControlCollection();
 		for (ControlInterface controlInterface : targetControlCollection)
 		{
 			if (controlInterface instanceof ContainmentAssociationControlInterface)
 			{
 				ContainmentAssociationControlInterface containmentAssociationControlInterface = (ContainmentAssociationControlInterface) controlInterface;
 				ContainerInterface tempContainer = containmentAssociationControlInterface.getContainer();
 				if (childContainerList.contains(tempContainer))
 				{
 					ContainerInterface actualContainer = (ContainerInterface) childContainerList.get(childContainerList
 							.indexOf(containmentAssociationControlInterface.getContainer()));
 					tempContainer.setEntity(actualContainer.getEntity());
 					((AssociationInterface) containmentAssociationControlInterface.getAbstractAttribute()).setTargetEntity(actualContainer
 							.getEntity());
 					containmentAssociationControlInterface.setContainer(actualContainer);
 				}
 				else
 				{
 					updateReferences(tempContainer, childContainerList);
 				}
 			}
 		}
 	}
 
 	private List getAllChildContainers(ContainerInterface container)
 	{
 		Collection<ControlInterface> controlCollection = container.getControlCollection();
 		List childContainerList = new ArrayList();
 		for (ControlInterface controlInterface : controlCollection)
 		{
 
 			if (controlInterface instanceof ContainmentAssociationControlInterface)
 			{
 				ContainmentAssociationControlInterface containmentAssociationControlInterface = (ContainmentAssociationControlInterface) controlInterface;
 				ContainerInterface tempContainer = containmentAssociationControlInterface.getContainer();
 				childContainerList.add(tempContainer);
 			}
 		}
 		return childContainerList;
 	}
 
 	private ContainerInterface getSelectedContainer(ContainerInterface container, Long containerId,List<ContainerInterface> listOfContainers)
 	{
 		if (container.getId().equals(containerId))
 		{
 			listOfContainers.add(container);
 		}
 		else
 		{
 			Collection<ControlInterface> controlCollection = container.getControlCollection();
 			for (ControlInterface controlInterface : controlCollection)
 			{
 				if (controlInterface instanceof ContainmentAssociationControlInterface)
 				{
 					ContainmentAssociationControlInterface containmentAssociationControlInterface = (ContainmentAssociationControlInterface) controlInterface;
 					ContainerInterface tempContainer = containmentAssociationControlInterface.getContainer();
 					if (tempContainer.getId().equals(containerId))
 					{
 						listOfContainers.add(tempContainer);
 						break;
 					}
 					else
 					{
 						getSelectedContainer(tempContainer, containerId,listOfContainers);
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param subFormContainer
 	 * @param mainFormContainer
 	 */
 	public void associateParentGroupToNewEntity(ContainerInterface subFormContainer, ContainerInterface mainFormContainer)
 	{
 		if ((subFormContainer != null) && (mainFormContainer != null))
 		{
 			EntityInterface targetEntity = subFormContainer.getEntity();
 			EntityInterface sourceEntity = mainFormContainer.getEntity();
 			Collection<EntityGroupInterface> sourceEntityGroups = sourceEntity.getEntityGroupCollection();
 			if (sourceEntityGroups != null)
 			{
 				Iterator<EntityGroupInterface> entityGroupIter = sourceEntityGroups.iterator();
 				if (entityGroupIter.hasNext())
 				{
 					EntityGroupInterface sourceEntityGroup = entityGroupIter.next();
 					targetEntity.addEntityGroupInterface(sourceEntityGroup);
 				}
 			}
 
 		}
 	}
 
 }
