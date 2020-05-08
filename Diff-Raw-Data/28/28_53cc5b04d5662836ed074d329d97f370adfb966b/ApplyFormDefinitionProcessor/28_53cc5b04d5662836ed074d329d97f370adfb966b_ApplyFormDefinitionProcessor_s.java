 
 package edu.common.dynamicextensions.processor;
 
 /**
  * This processor class mainly helps the action class to call the related Object driven processors
  * to update the Actionforms by Retrieving  data form Cache.
  * @author deepti_shelar
  */
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.webui.actionform.FormDefinitionForm;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 
 public class ApplyFormDefinitionProcessor extends BaseDynamicExtensionsProcessor
 {
 
 	/**
 	 * Default Constructor.
 	 */
 	protected ApplyFormDefinitionProcessor()
 	{
 		super();
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
 	 * @param containerObject : Container object
 	 * @param actionForm : Form object
 	 * @param isActionSave : flag stating whether the object is to be saved to DB
 	 * @return ContainerInterface : Container object
 	 * @throws DynamicExtensionsApplicationException :Exception thrown by Entity Manager
 	 * @throws DynamicExtensionsSystemException :Exception thrown by Entity Manager
 	 */
 	public ContainerInterface addEntityToContainer(ContainerInterface containerObject,
 			FormDefinitionForm actionForm, EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface container = containerObject;
 		ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 		if (container == null)
 		{
 			container = containerProcessor.createContainer();
 		}
 		//validate container name
 		DynamicExtensionsUtility.validateName(actionForm.getFormName());
 		containerProcessor.populateContainer(container, actionForm, entityGroup);
 
 		//Add entity
 		EntityProcessor entityProcessor = EntityProcessor.getInstance();
 		EntityInterface entity = (EntityInterface) container.getAbstractEntity();
 		if (entity == null)
 		{
 			//validate entity name
 			DynamicExtensionsUtility.validateName(actionForm.getFormName());
 			entity = entityProcessor.createAndPopulateEntity(actionForm);
 		}
 		else
 		{
 			//entityProcessor.populateEntity(actionForm, entity);
 			entityProcessor.editEntity(actionForm, entity);
 		}
 
 		container.setAbstractEntity(entity);
 		//entityInterface.setContainer((Container) containerInterface);
 
 		if (entityGroup != null)
 		{
 			associateEntityToGroup(entityGroup, (EntityInterface) container.getAbstractEntity());
 		}
 		containerProcessor.populateContainer(container, actionForm, entityGroup);
 		if (container.getBaseContainer() == null)
 		{
 			entity.setParentEntity(null);
 			entity.populateEntityForConstraintProperties(false);
 		}
 		else
 		{
 			EntityInterface parentEntity = (EntityInterface) container.getBaseContainer()
 					.getAbstractEntity();
 			entity.setParentEntity(parentEntity);
 			entity.populateEntityForConstraintProperties(false);
 		}
 
 		entity.addContainer(container);
 		return container;
 	}
 
 	/**
 	 * @param associationObject Association Object
 	 * @param formDefinitionForm
 	 * @param containerInterface
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 *
 	 */
 	public AssociationInterface associateEntity(AssociationInterface associationObject,
 			ContainerInterface sourceContainer, ContainerInterface targetContainer,
 			FormDefinitionForm formDefinitionForm) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AssociationInterface association = associationObject;
 		if ((formDefinitionForm != null) && (targetContainer != null) && (sourceContainer != null))
 		{
 			EntityInterface targetEntity = (EntityInterface) targetContainer.getAbstractEntity();
 			EntityInterface sourceEntity = (EntityInterface) sourceContainer.getAbstractEntity();
 			if ((sourceEntity != null) && (targetEntity != null))
 			{
 				String viewAs = formDefinitionForm.getViewAs();
 				if ((viewAs != null) && (viewAs.equals(ProcessorConstants.VIEW_AS_FORM)))
 				{
 					association = associateEntity(association, AssociationType.CONTAINTMENT,
 							sourceEntity, targetEntity, Cardinality.ONE, Cardinality.ONE);
 				}
 				else if ((viewAs != null)
 						&& (viewAs.equals(ProcessorConstants.VIEW_AS_SPREADSHEET)))
 				{
 					association = associateEntity(association, AssociationType.CONTAINTMENT,
 							sourceEntity, targetEntity, Cardinality.ONE, Cardinality.MANY);
 				}
 				association.populateAssociationForConstraintProperties();
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
 	private ContainerInterface createTargetEntityContainer(FormDefinitionForm formDefinitionForm,
 			EntityGroupInterface entityGroup) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		//validate container name b4 creating it
 		ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 		DynamicExtensionsUtility.validateName(formDefinitionForm.getFormName());
 		ContainerInterface containerInterface = containerProcessor.createContainer();
 		containerProcessor.populateContainer(containerInterface, formDefinitionForm, entityGroup);
 
 		//Add entity
 		EntityProcessor entityProcessor = EntityProcessor.getInstance();
 		EntityInterface entityInterface = entityProcessor
 				.createAndPopulateEntity(formDefinitionForm);
 
 		//Add entity to container
 		containerInterface.setAbstractEntity(entityInterface);
 		//entityInterface.setContainer((Container) containerInterface);
 		return containerInterface;
 	}
 
 	/**
 	 * @param associationObject Association Object
 	 * @param associationType
 	 * @param sourceEntity
 	 * @param targetEntity
 	 * @param sourceCardinality
 	 * @param targetCardinality
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private AssociationInterface associateEntity(AssociationInterface associationObject,
 			AssociationType associationType, EntityInterface sourceEntity,
 			EntityInterface targetEntity, Cardinality sourceCardinality,
 			Cardinality targetCardinality) throws DynamicExtensionsSystemException
 	{
 		AssociationInterface association = associationObject;
 		if (association == null)
 		{
 			association = DomainObjectFactory.getInstance().createAssociation();
 		}
 		association.setTargetEntity(targetEntity);
 		association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 		association.setName(targetEntity.getName());
 		association.setSourceRole(getRole(associationType, sourceEntity.getName(), Cardinality.ONE,
 				sourceCardinality));
 		association.setTargetRole(getRole(associationType, targetEntity.getName(), Cardinality.ONE,
 				targetCardinality));
 		sourceEntity.addAssociation(association);
 		ConstraintPropertiesInterface constraintProperties = DynamicExtensionsUtility
				.getConstraintProperties(association);
 		association.setConstraintProperties(constraintProperties);
 
 		return association;
 	}
 
 	/**
 	 * @param selectedObjectId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ContainerInterface getTargetEntityContainer(String targetContainerId,
 			EntityGroupInterface entityGroup) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface targetContainer = null;
 		if ((targetContainerId != null) && (!targetContainerId.trim().equals("")))
 		{
 			for (EntityInterface entity : entityGroup.getEntityCollection())
 			{
 				Collection<ContainerInterface> containerCollection = entity
 						.getContainerCollection();
 				for (ContainerInterface currentContainer : containerCollection)
 				{
 					if (currentContainer.getId() != null
 							&& currentContainer.getId().toString().equals(targetContainerId))
 					{
 						targetContainer = currentContainer;
 
 					}
 				}
 			}
 
 			//targetContainer = DynamicExtensionsUtility.getContainerByIdentifier(targetContainerId);
 		}
 		return targetContainer;
 	}
 
 	private RoleInterface getRole(AssociationType associationType, String name,
 			Cardinality minCard, Cardinality maxCard)
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
 			((EntityGroup) entityGroup).setCurrent(true);
 			entity.setEntityGroup(entityGroup);
 		}
 	}
 
 	/**
 	 * @param mainFormContainer
 	 * @param subFormContainer
 	 */
 	public void addSubFormControlToContainer(ContainerInterface mainFormContainer,
 			ContainerInterface subFormContainer, AssociationInterface association)
 	{
 		if ((mainFormContainer != null) && (subFormContainer != null))
 		{
 			ControlProcessor controlProcessor = ControlProcessor.getInstance();
 			ControlInterface subFormControl = controlProcessor.createContainmentAssociationControl(
 					subFormContainer, association);
 			subFormControl.setSequenceNumber(WebUIManager
 					.getSequenceNumberForNextControl(mainFormContainer));
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
 	public ContainerInterface getSubFormContainer(FormDefinitionForm formDefinitionForm,
 			ContainerInterface mainFormContainer, EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface targetEntityContainer = null;
 		if (formDefinitionForm != null)
 		{
 			//Get target entity container
 			Long selectedContainerId = Long.valueOf(0);
 			if (formDefinitionForm.getSelectedObjectId() != null
 					&& formDefinitionForm.getSelectedObjectId().trim().length() != 0)
 			{
 				selectedContainerId = Long.valueOf(formDefinitionForm.getSelectedObjectId());
 			}
 
 			String createAs = formDefinitionForm.getCreateAs();
 			if ((createAs != null) && (createAs.equals(ProcessorConstants.CREATE_FROM_EXISTING)))
 			{
 				targetEntityContainer = getSelectedContainer(mainFormContainer, selectedContainerId);
 				if (targetEntityContainer == null)
 				{
 					targetEntityContainer = getTargetEntityContainer(formDefinitionForm
 							.getSelectedObjectId(), entityGroup);
 					List<ContainerInterface> childContainerList = getAllChildContainers(mainFormContainer);
 					updateReferences(targetEntityContainer, childContainerList);
 
 					//childContainerList.add(0, mainFormContainer);
 					//updateEntityGroupReferences(targetEntityContainer, mainFormContainer);
 				}
 			}
 			else if ((createAs != null) && (createAs.equals(ProcessorConstants.CREATE_AS_NEW)))
 			{
 				targetEntityContainer = createTargetEntityContainer(formDefinitionForm, entityGroup);
 			}
 		}
 		return targetEntityContainer;
 	}
 
 	/**
 	 * @param targetContainer
 	 * @param childContainerList
 	 */
 	private void updateReferences(ContainerInterface targetContainer,
 			List<ContainerInterface> childContainerList)
 	{
 		Collection<ControlInterface> targetControlCollection = targetContainer
 				.getControlCollection();
 		for (ControlInterface controlInterface : targetControlCollection)
 		{
 			if (controlInterface instanceof ContainmentAssociationControlInterface)
 			{
 				ContainmentAssociationControlInterface containmentAssociationControl = (ContainmentAssociationControlInterface) controlInterface;
 				ContainerInterface tempContainer = containmentAssociationControl.getContainer();
 				if (childContainerList.contains(tempContainer))
 				{
 					ContainerInterface actualContainer = (ContainerInterface) childContainerList
 							.get(childContainerList.indexOf(containmentAssociationControl
 									.getContainer()));
 					tempContainer.setAbstractEntity((EntityInterface) actualContainer
 							.getAbstractEntity());
 					//actualContainer.getEntity().setContainer((Container) tempContainer);
 					((AssociationInterface) containmentAssociationControl
 							.getBaseAbstractAttribute())
 							.setTargetEntity((EntityInterface) actualContainer.getAbstractEntity());
 					containmentAssociationControl.setContainer(actualContainer);
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
 				ContainmentAssociationControlInterface containmentAssociationControl = (ContainmentAssociationControlInterface) controlInterface;
 				ContainerInterface tempContainer = containmentAssociationControl.getContainer();
 				childContainerList.add(tempContainer);
 			}
 		}
 		return childContainerList;
 	}
 
 	private ContainerInterface getSelectedContainer(ContainerInterface container, Long containerId)
 	{
 		Collection<ControlInterface> controlCollection = container.getControlCollection();
 
 		for (ControlInterface controlInterface : controlCollection)
 		{
 			if (controlInterface instanceof ContainmentAssociationControlInterface)
 			{
 				ContainmentAssociationControlInterface containmentAssociationControl = (ContainmentAssociationControlInterface) controlInterface;
 				ContainerInterface tempContainer = containmentAssociationControl.getContainer();
 
 				if (tempContainer.getId().equals(containerId))
 				{
 					return tempContainer;
 				}
 				else
 				{
 					return getSelectedContainer(tempContainer, containerId);
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param subFormContainer
 	 * @param mainFormContainer
 	 */
 	public void associateParentGroupToNewEntity(ContainerInterface subFormContainer,
 			ContainerInterface mainFormContainer)
 	{
 		if ((subFormContainer != null) && (mainFormContainer != null))
 		{
 			EntityInterface targetEntity = (EntityInterface) subFormContainer.getAbstractEntity();
 			EntityInterface sourceEntity = (EntityInterface) mainFormContainer.getAbstractEntity();
 			targetEntity.setEntityGroup(sourceEntity.getEntityGroup());
 			sourceEntity.getEntityGroup().addEntity(targetEntity);
 		}
 	}
 
 }
