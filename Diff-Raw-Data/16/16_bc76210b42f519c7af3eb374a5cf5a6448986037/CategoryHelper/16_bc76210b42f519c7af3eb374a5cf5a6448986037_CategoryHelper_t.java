 
 package edu.common.dynamicextensions.util;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.common.dynamicextensions.domain.Category;
 import edu.common.dynamicextensions.domain.CategoryAssociation;
 import edu.common.dynamicextensions.domain.CategoryAttribute;
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.PathAssociationRelationInterface;
 import edu.common.dynamicextensions.domain.UserDefinedDE;
import edu.common.dynamicextensions.domain.userinterface.ComboBox;
 import edu.common.dynamicextensions.domain.userinterface.Container;
import edu.common.dynamicextensions.domain.userinterface.ListBox;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PathInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CategoryAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CheckBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.DatePickerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.FileUploadInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.RadioButtonInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.SelectInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextAreaInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextFieldInterface;
 import edu.common.dynamicextensions.entitymanager.AbstractMetadataManager;
 import edu.common.dynamicextensions.entitymanager.CategoryManager;
 import edu.common.dynamicextensions.entitymanager.CategoryManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 
 /**
  * @author kunal_kamble
  * @author mandar_shidhore
  *
  */
 public class CategoryHelper implements CategoryHelperInterface
 {
 	CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#createCtaegory(java.lang.String)
 	 */
 	public CategoryInterface getCategory(String name) throws DynamicExtensionsSystemException
 	{
 
 		CategoryInterface category = (CategoryInterface) ((AbstractMetadataManager) categoryManager).getObjectByName(Category.class.getName(), name);
 
 		if (category == null)
 		{
 			category = DomainObjectFactory.getInstance().createCategory();
 			category.setName(name);
 		}
 
 		return category;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#saveCategory(edu.common.dynamicextensions.domaininterface.CategoryInterface)
 	 */
 	public void saveCategory(CategoryInterface category) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		try
 		{
 			CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 			categoryManager.persistCategory(category);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 			throw new DynamicExtensionsSystemException("Error while saving a category");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			throw new DynamicExtensionsApplicationException("Error while saving a category");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#createCategoryEntity(java.lang.String, edu.common.dynamicextensions.domaininterface.CategoryInterface[])
 	 */
 	public ContainerInterface createOrUpdateCategoryEntityAndContainer(EntityInterface entity, String containerCaption, CategoryInterface category,
 			String... categoryEntityName)
 	{
 		String newCategoryEntityName = (categoryEntityName.length > 0 ? categoryEntityName[0] : null);
 		CategoryEntityInterface categoryEntity = createOrUpdateCategoryEntity(category, entity, newCategoryEntityName);
 
 		if (containerCaption == null)
 		{
 			containerCaption = entity.getName() + "_category_entity_container";
 
 		}
 		ContainerInterface container = createContainer(categoryEntity, containerCaption);
 		return container;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.util.CategoryHelperInterface#createOrUpdateCategoryEntity(edu.common.dynamicextensions.domaininterface.CategoryInterface, edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.String)
 	 */
 	public CategoryEntityInterface createOrUpdateCategoryEntity(CategoryInterface category, EntityInterface entity, String categoryEntityName)
 	{
 		CategoryEntityInterface categoryEntity = null;
 		if (categoryEntityName != null)
 		{
 			categoryEntity = category.getCategoryEntityByName(categoryEntityName);
 		}
 		if (categoryEntity == null)
 		{
 			categoryEntity = DomainObjectFactory.getInstance().createCategoryEntity();
 			categoryEntity.setName(categoryEntityName);
 			categoryEntity.setEntity(entity);
 		}
 		return categoryEntity;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#setRootCategoryEntity(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.domaininterface.CategoryInterface)
 	 */
 	public void setRootCategoryEntity(ContainerInterface container, CategoryInterface category)
 	{
 		category.setRootCategoryElement((CategoryEntityInterface) container.getAbstractEntity());
 		((CategoryEntityInterface) container.getAbstractEntity()).setCategory(category);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#addControl(edu.common.dynamicextensions.domaininterface.AttributeInterface, edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.categoryManager.CategoryHelperInterface.ControlEnum, java.util.List<edu.common.dynamicextensions.domaininterface.PermissibleValueInterface>[])
 	 */
 	public ControlInterface addOrUpdateControl(EntityInterface entity, String attributeName, ContainerInterface container, ControlEnum controlType,
 			String controlCaption, List<String>... permissibleValueList) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		CategoryAttributeInterface categoryAttribute = createOrupdateCategoryAttribute(entity, attributeName, container);
 
 		ControlInterface control = null;
 		List<String> permissibleValueNameList = (permissibleValueList.length == 0 ? null : permissibleValueList[0]);
 		control = createOrUpdateControl(controlType, controlCaption, container, categoryAttribute, permissibleValueNameList);
 
 		control.setCaption(controlCaption);
 
 		return control;
 	}
 
 	/**
 	 * @param entity
 	 * @param attributeName
 	 * @param container
 	 * @return
 	 */
 	public CategoryAttributeInterface createOrupdateCategoryAttribute(EntityInterface entity, String attributeName, ContainerInterface container)
 	{
 		CategoryAttributeInterface categoryAttribute = (CategoryAttributeInterface) getCategoryAttribute(attributeName, container);
 		if (categoryAttribute == null)
 		{
 			CategoryEntity categoryEntity = (CategoryEntity) container.getAbstractEntity();
 			categoryAttribute = createCategoryAttribute(entity, attributeName, categoryEntity);
 		}
 
 		return categoryAttribute;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.util.CategoryHelperInterface#createCategoryAttribute(edu.common.dynamicextensions.domaininterface.EntityInterface, java.lang.String, edu.common.dynamicextensions.domaininterface.CategoryEntityInterface)
 	 */
 	public CategoryAttributeInterface createCategoryAttribute(EntityInterface entity, String attributeName, CategoryEntityInterface categoryEntity)
 	{
 		CategoryAttributeInterface categoryAttribute = DomainObjectFactory.getInstance().createCategoryAttribute();
 		categoryAttribute.setName(attributeName + " Category Attribute");
 		categoryAttribute.setAttribute(entity.getAttributeByName(attributeName));
 
 		categoryEntity.addCategoryAttribute(categoryAttribute);
 		categoryAttribute.setCategoryEntity(categoryEntity);
 
 		categoryEntity.addCategoryAttribute(categoryAttribute);
 		categoryAttribute.setCategoryEntity(categoryEntity);
 
 		return categoryAttribute;
 
 	}
 
 	/**
 	 * @param controlType
 	 * @param controlCaption
 	 * @param container
 	 * @param categoryAttribute
 	 * @param permissibleValueNameList
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ControlInterface createOrUpdateControl(ControlEnum controlType, String controlCaption, ContainerInterface container,
 			CategoryAttributeInterface categoryAttribute, List<String> permissibleValueNameList) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		ControlInterface control = null;
 		EntityInterface entity = categoryAttribute.getCategoryEntity().getEntity();
 		String attributeName = categoryAttribute.getAttribute().getName();
 		switch (controlType)
 		{
 			case TEXT_FIELD_CONTROL :
 				control = createOrUpdateTextFieldControl(container, categoryAttribute);
 				break;
 			case LIST_BOX_CONTROL :
 				control = createOrUpdateSelectControl(container, categoryAttribute, createPermissibleValuesList(entity, attributeName,
 						permissibleValueNameList), controlType);
 				break;
 			case COMBO_BOX_CONTROL :
 				control = createOrUpdateSelectControl(container, categoryAttribute, createPermissibleValuesList(entity, attributeName,
 						permissibleValueNameList), controlType);
 				break;
 			case DATE_PICKER_CONTROL :
 				control = createOrUpdateDatePickerControl(container, categoryAttribute);
 				break;
 			case FILE_UPLOAD_CONTROL :
 				control = createOrUpdateFileUploadControl(container, categoryAttribute);
 				break;
 			case TEXT_AREA_CONTROL :
 				control = createOrUpdateTextAreaControl(container, categoryAttribute);
 				break;
 			case RADIO_BUTTON_CONTROL :
 				control = createOrUpdateRadioButtonControl(container, categoryAttribute, createPermissibleValuesList(entity, attributeName,
 						permissibleValueNameList));
 				break;
 			case CHECK_BOX_CONTROL :
 				control = createOrUpdateCheckBoxControl(container, categoryAttribute);
 				break;
 		}
 
 		control.setCaption(controlCaption);
 		return control;
 
 	}
 
 	/**
 	 * @param attributeName
 	 * @param container
 	 * @return
 	 */
 	private BaseAbstractAttributeInterface getCategoryAttribute(String attributeName, ContainerInterface container)
 	{
 		BaseAbstractAttributeInterface categoryAttribute = null;
 		for (ControlInterface control : container.getControlCollection())
 		{
 			if (control.getBaseAbstractAttribute() instanceof CategoryAssociation)
 			{
 
 			}
 			else
 			{
 				if (((CategoryAttributeInterface) control.getBaseAbstractAttribute()).getAttribute().getName().equals(attributeName))
 				{
 					categoryAttribute = control.getBaseAbstractAttribute();
 					break;
 				}
 			}
 
 		}
 		return categoryAttribute;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#setParentCategoryEntity(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface)
 	 */
 	public void setParentContainer(ContainerInterface parentContainer, ContainerInterface childContainer)
 	{
 		CategoryEntityInterface parentCategoryEntity = null;
 		CategoryEntityInterface childCategoryEntity = null;
 		if (parentContainer != null)
 		{
 			parentCategoryEntity = (CategoryEntity) parentContainer.getAbstractEntity();
 			if (childContainer != null)
 			{
 				childCategoryEntity = (CategoryEntity) childContainer.getAbstractEntity();
 			}
 		}
 		if (childCategoryEntity != null)
 		{
 			childCategoryEntity.setParentCategoryEntity(parentCategoryEntity);
 		}
 		if (childContainer != null)
 		{
 			childContainer.setBaseContainer(parentContainer);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.catissuecore.test.CategoryHelperInterface#associateCategoryContainers(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, java.util.List, int)
 	 */
 	public CategoryAssociationControlInterface associateCategoryContainers(CategoryInterface category, EntityGroupInterface entityGroup,
 			ContainerInterface sourceContainer, ContainerInterface targetContainer, List<AssociationInterface> associationList, int noOfEntries,
 			String instance) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		CategoryAssociationControlInterface associationControl = null;
 		CategoryAssociationInterface oldAssociation = null;
 
 		ContainerInterface rootContainer = null;
 
 		if (category.getRootCategoryElement() != null)
 		{
 			rootContainer = (new ArrayList<ContainerInterface>(category.getRootCategoryElement().getContainerCollection())).get(0);
 		}
 
 		associationControl = (CategoryAssociationControlInterface) getAssociationControl(rootContainer, targetContainer.getCaption());
 
 		CategoryEntityInterface sourceCategoryEntity = (CategoryEntityInterface) sourceContainer.getAbstractEntity();
 		CategoryEntityInterface targetCategoryEntity = (CategoryEntityInterface) targetContainer.getAbstractEntity();
 		if (associationControl != null)
 		{
 			if (associationControl.getParentContainer().equals(sourceContainer))
 			{
 				return associationControl;
 			}
 			else
 			{
 				removeControl(associationControl.getParentContainer(), associationControl);
 				oldAssociation = (CategoryAssociationInterface) associationControl.getBaseAbstractAttribute();
 				removeCategoryAssociation(oldAssociation);
 				associationControl.setBaseAbstractAttribute(null);
 
 			}
 		}
 
 		CategoryAssociationInterface categoryAssociation = associateCategoryEntities(sourceCategoryEntity, targetCategoryEntity, sourceCategoryEntity
 				.getName()
 				+ " to " + targetCategoryEntity.getName() + " category association", noOfEntries, entityGroup, associationList, instance);
 
 		CategoryAssociationControlInterface categoryAssociationControl = createCategoryAssociationControl(sourceContainer, targetContainer,
 				categoryAssociation, targetContainer.getCaption());
 
 		return categoryAssociationControl;
 	}
 
 	/**
 	 * @param path
 	 * @param associationList
 	 * @param entityGroup
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void updatePath(PathInterface path, List<AssociationInterface> associationList, EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		//clear old path
 		path.setPathAssociationRelationCollection(null);
 
 		int pathSequenceNumber = 1;
 
 		for (AssociationInterface association : associationList)
 		{
 			PathAssociationRelationInterface pathAssociationRelation = factory.createPathAssociationRelation();
 			pathAssociationRelation.setPathSequenceNumber(pathSequenceNumber++);
 			pathAssociationRelation.setAssociation(association);
 
 			pathAssociationRelation.setPath(path);
 			path.addPathAssociationRelation(pathAssociationRelation);
 		}
 	}
 
 	/**
 	 * @param name
 	 * @param entityGroup
 	 * @return
 	 */
 	private AssociationInterface getAssociationByName(String name, EntityGroupInterface entityGroup)
 	{
 		AssociationInterface association = null;
 		for (EntityInterface entity : entityGroup.getEntityCollection())
 		{
 			for (AssociationInterface associationInterface : entity.getAllAssociations())
 			{
 				if (name.equals(associationInterface.getName()))
 				{
 					association = associationInterface;
 				}
 			}
 		}
 		return association;
 	}
 
 	/**
 	 * @param path
 	 * @param instance
 	 */
 	public void addInstanceInformationToPath(PathInterface path, String instance)
 	{
 		String[] entityArray = instance.split("->");
 
 		int counter = 0;
 		for (PathAssociationRelationInterface associationRelation : path.getSortedPathAssociationRelationCollection())
 		{
 			String sourceEntity = entityArray[counter];
 			String targetEntity = entityArray[counter + 1];
 			associationRelation.setSourceInstanceId(Long.parseLong(sourceEntity.substring(sourceEntity.indexOf("[") + 1, sourceEntity.indexOf("]"))));
 			associationRelation.setTargetInstanceId(Long.parseLong(targetEntity.substring(targetEntity.indexOf("[") + 1, targetEntity.indexOf("]"))));
 			counter++;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.catissuecore.test.CategoryHelperInterface#getNextSequenceNumber(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface)
 	 */
 	public int getNextSequenceNumber(ContainerInterface container)
 	{
 		int nextSequenceNumber = 1;
 
 		if (container.getControlCollection() != null)
 		{
 			nextSequenceNumber = container.getControlCollection().size() + 1;
 		}
 
 		return nextSequenceNumber;
 	}
 
 	/**
 	 * @param sourceCategoryEntity source category entity
 	 * @param targetCategoryEntity target category entity
 	 * @param path path information between the category entities
 	 */
 	private PathInterface addPathBetweenCategoryEntities(CategoryEntityInterface sourceCategoryEntity, CategoryEntityInterface targetCategoryEntity)
 	{
 		PathInterface path = DomainObjectFactory.getInstance().createPath();
 		targetCategoryEntity.setPath(path);
 		targetCategoryEntity.setTreeParentCategoryEntity(sourceCategoryEntity);
 		return path;
 	}
 
 	/**
 	 * Method associates the source and the target category entity
 	 * @param sourceCategoryEntity source category entity
 	 * @param targetCategoryEntity target category entity
 	 * @param name name of the category association
 	 * @return CategoryAssociationInterface category association object
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public CategoryAssociationInterface associateCategoryEntities(CategoryEntityInterface sourceCategoryEntity,
 			CategoryEntityInterface targetCategoryEntity, String name, int numberOfentries, EntityGroupInterface entityGroup,
 			List<AssociationInterface> associationList, String instance) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 
 		PathInterface path = addPathBetweenCategoryEntities(sourceCategoryEntity, targetCategoryEntity);
 		updatePath(path, associationList, entityGroup);
 		targetCategoryEntity.setNumberOfEntries(numberOfentries);
 
 		CategoryAssociationInterface categoryAssociation = DomainObjectFactory.getInstance().createCategoryAssociation();
 		categoryAssociation.setName(name);
 
 		sourceCategoryEntity.addChildCategory(targetCategoryEntity);
 
 		categoryAssociation.setCategoryEntity(sourceCategoryEntity);
 		categoryAssociation.setTargetCategoryEntity(targetCategoryEntity);
 
 		sourceCategoryEntity.getCategoryAssociationCollection().add(categoryAssociation);
 
 		addInstanceInformationToPath(path, instance);
 
 		return categoryAssociation;
 	}
 
 	/**
 	 * Method creates the association between the given parent and the target container 
 	 * @param parentContainer main form
 	 * @param targetContainer sub form
 	 * @param categoryAssociation association between category entities
 	 * @param caption name to be displayed on UI
 	 * @return CategoryAssociationControlInterface category association control object
 	 */
 	private CategoryAssociationControlInterface createCategoryAssociationControl(ContainerInterface parentContainer,
 			ContainerInterface targetContainer, CategoryAssociationInterface categoryAssociation, String caption)
 	{
 
 		CategoryAssociationControlInterface categoryAssociationControl = DomainObjectFactory.getInstance().createCategoryAssociationControl();
 		categoryAssociationControl.setSequenceNumber(getNextSequenceNumber(parentContainer));
 		categoryAssociationControl.setCaption(caption);
 		categoryAssociationControl.setContainer(targetContainer);
 		categoryAssociationControl.setBaseAbstractAttribute(categoryAssociation);
 		parentContainer.addControl(categoryAssociationControl);
 		categoryAssociationControl.setParentContainer((Container) parentContainer);
 
 		return categoryAssociationControl;
 	}
 
 	/**
 	 * @param abstractEntity category entity
 	 * @return container object for category entity
 	 */
 	private ContainerInterface createContainer(AbstractEntityInterface abstractEntity, String caption)
 	{
 		ContainerInterface container = null;
 		if (abstractEntity.getContainerCollection().size() > 0)
 		{
 			container = new ArrayList<ContainerInterface>(abstractEntity.getContainerCollection()).get(0);
 		}
 		if (container == null)
 		{
 			container = DomainObjectFactory.getInstance().createContainer();
 			container.setMainTableCss("formRequiredLabel");
 			container.setRequiredFieldIndicatior("*");
 			container.setRequiredFieldWarningMessage("indicates mandatory fields.");
 			container.setAbstractEntity(abstractEntity);
 			abstractEntity.addContainer(container);
 		}
 
 		if (caption == null)
 		{
 			caption = abstractEntity.getName() + " category container";
 		}
 
 		container.setCaption(caption);
 
 		return container;
 	}
 
 	/**
 	 *
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return text field object
 	 */
 	private TextFieldInterface createOrUpdateTextFieldControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		ControlInterface control = getControl(container, baseAbstractAttribute);
 		TextFieldInterface textField = null;
 		if (control != null && !(control instanceof TextFieldInterface))
 		{
 			removeControl(container, control);
 		}
 		else
 		{
 			textField = (TextFieldInterface) control;
 		}
 
 		if (textField == null)
 		{
 			textField = DomainObjectFactory.getInstance().createTextField();
 			textField.setColumns(50);
 			textField.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, textField, baseAbstractAttribute);
 		}
 
 		return textField;
 	}
 
 	private ControlInterface getControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		ControlInterface controlInterface = null;
 		for (ControlInterface control : container.getControlCollection())
 		{
 			if (baseAbstractAttribute.equals(control.getBaseAbstractAttribute()))
 			{
 				controlInterface = control;
 			}
 		}
 		return controlInterface;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @param permissibleValues list of permissible values
 	 * @return list box object
 	 */
 	private SelectInterface createOrUpdateSelectControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute,
 			List<PermissibleValueInterface> permissibleValues, ControlEnum controlType)
 	{
 		ControlInterface control = getControl(container, baseAbstractAttribute);
 		SelectInterface selectControl = null;
 		if (control != null && !(control instanceof SelectInterface))
 		{
 			removeControl(container, control);
 		}
 		else
 		{
			if ((control instanceof ComboBox && controlType.equals(controlType.COMBO_BOX_CONTROL))
					|| (control instanceof ListBox && controlType.equals(controlType.LIST_BOX_CONTROL)))

			{
				selectControl = (SelectInterface) control;
			}
			else if (control != null)
			{
				removeControl(container, control);
			}

 		}
 
 		if (selectControl == null)
 		{
 			if (controlType.equals(controlType.LIST_BOX_CONTROL))
 			{
 				selectControl = DomainObjectFactory.getInstance().createListBox();
 			}
 			else if (controlType.equals(controlType.COMBO_BOX_CONTROL))
 			{
 				selectControl = DomainObjectFactory.getInstance().createComboBox();
 			}
 			selectControl.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, selectControl, baseAbstractAttribute);
 		}
 		//clear old permissible values
 		((CategoryAttribute) baseAbstractAttribute).clearDataElementCollection();
 
 		UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
 		for (PermissibleValueInterface pv : permissibleValues)
 		{
 			userDefinedDE.addPermissibleValue(pv);
 		}
 
 		//add new permissible values
 		((CategoryAttribute) baseAbstractAttribute).setDataElement(userDefinedDE);
 		AttributeTypeInformationInterface attributeTypeInformation = ((CategoryAttribute) baseAbstractAttribute).getAttribute()
 				.getAttributeTypeInformation();
 
 		if (attributeTypeInformation.getDefaultValue() != null)
 		{
 			((CategoryAttribute) baseAbstractAttribute).setDefaultValue(attributeTypeInformation.getDefaultValue());
 		}
 		return selectControl;
 	}
 
 	/**
 	 * @param container
 	 * @param control
 	 */
 	private void removeControl(ContainerInterface container, ControlInterface control)
 	{
 		control.setParentContainer(null);
 		container.getControlCollection().remove(control);
 
 	}
 
 	/**
 	 * @param categoryAssociation
 	 */
 	private void removeCategoryAssociation(CategoryAssociationInterface categoryAssociation)
 	{
 		CategoryEntityInterface sourceCategoryEntity = categoryAssociation.getCategoryEntity();
 		CategoryEntityInterface targetCategoryEntity = categoryAssociation.getTargetCategoryEntity();
 
 		targetCategoryEntity.setPath(null);
 		sourceCategoryEntity.getChildCategories().remove(targetCategoryEntity);
 		sourceCategoryEntity.getCategoryAssociationCollection().remove(categoryAssociation);
 		categoryAssociation.setCategoryEntity(null);
 
 		targetCategoryEntity.setParentCategoryEntity(null);
 	}
 
 	/**
 	 * @param container
 	 * @param control
 	 * @param baseAbstractAttribute
 	 */
 	private void updateContainerAndControl(ContainerInterface container, ControlInterface control,
 			BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		container.addControl(control);
 		control.setParentContainer((Container) container);
 		control.setBaseAbstractAttribute(baseAbstractAttribute);
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return date picker object
 	 */
 	private DatePickerInterface createOrUpdateDatePickerControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		ControlInterface control = getControl(container, baseAbstractAttribute);
 		DatePickerInterface datePicker = null;
 		if (control != null && !(control instanceof DatePickerInterface))
 		{
 			removeControl(container, control);
 		}
 		else
 		{
 			datePicker = (DatePickerInterface) control;
 		}
 
 		if (datePicker == null)
 		{
 			datePicker = DomainObjectFactory.getInstance().createDatePicker();
 			datePicker.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, datePicker, baseAbstractAttribute);
 		}
 
 		datePicker.setDateValueType(ProcessorConstants.DATE_ONLY_FORMAT);
 
 		return datePicker;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return file upload object
 	 */
 	private FileUploadInterface createOrUpdateFileUploadControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		ControlInterface control = getControl(container, baseAbstractAttribute);
 		FileUploadInterface fileUpload = null;
 		if (control != null && !(control instanceof FileUploadInterface))
 		{
 			removeControl(container, control);
 		}
 		else
 		{
 			fileUpload = (FileUploadInterface) control;
 		}
 
 		if (fileUpload == null)
 		{
 			fileUpload = DomainObjectFactory.getInstance().createFileUploadControl();
 			fileUpload.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, fileUpload, baseAbstractAttribute);
 		}
 
 		return fileUpload;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return text area object
 	 */
 	private TextAreaInterface createOrUpdateTextAreaControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		ControlInterface control = getControl(container, baseAbstractAttribute);
 		TextAreaInterface textArea = null;
 		if (control != null && !(control instanceof TextAreaInterface))
 		{
 			removeControl(container, control);
 		}
 		else
 		{
 			textArea = (TextAreaInterface) control;
 		}
 
 		if (textArea == null)
 		{
 			textArea = DomainObjectFactory.getInstance().createTextArea();
 			textArea.setSequenceNumber(getNextSequenceNumber(container));
 			textArea.setColumns(50);
 			textArea.setRows(5);
 			updateContainerAndControl(container, textArea, baseAbstractAttribute);
 		}
 
 		return textArea;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @param permissibleValues list of permissible values
 	 * @return RadioButtonInterface radio button object
 	 */
 	private RadioButtonInterface createOrUpdateRadioButtonControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute,
 			List<PermissibleValueInterface> permissibleValues)
 	{
 		ControlInterface control = getControl(container, baseAbstractAttribute);
 		RadioButtonInterface radioButton = null;
 		if (control != null && !(control instanceof RadioButtonInterface))
 		{
 			removeControl(container, control);
 		}
 		else
 		{
 			radioButton = (RadioButtonInterface) control;
 		}
 
 		if (radioButton == null)
 		{
 			radioButton = DomainObjectFactory.getInstance().createRadioButton();
 			radioButton.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, radioButton, baseAbstractAttribute);
 		}
 
 		UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
 		for (PermissibleValueInterface pv : permissibleValues)
 		{
 			userDefinedDE.addPermissibleValue(pv);
 		}
 
 		((CategoryAttribute) baseAbstractAttribute).setDataElement(userDefinedDE);
 
 		return radioButton;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return check box object
 	 */
 	private CheckBoxInterface createOrUpdateCheckBoxControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		ControlInterface control = getControl(container, baseAbstractAttribute);
 		CheckBoxInterface checkBox = null;
 		if (control != null && !(control instanceof CheckBoxInterface))
 		{
 			removeControl(container, control);
 		}
 		else
 		{
 			checkBox = (CheckBoxInterface) control;
 		}
 
 		if (checkBox == null)
 		{
 			checkBox = DomainObjectFactory.getInstance().createCheckBox();
 			checkBox.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, checkBox, baseAbstractAttribute);
 		}
 
 		return checkBox;
 	}
 
 	/**
 	 * This method creates a list of permissible values for a category attribute
 	 * @param entity entity which contains attribute by the given name
 	 * @param attributeName name of the attribute
 	 * @param desiredPermissibleValues subset of permissible values for this category attribute
 	 * @return list of permissible values for category attribute
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<PermissibleValueInterface> createPermissibleValuesList(EntityInterface entity, String attributeName,
 			List<String> desiredPermissibleValues) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<PermissibleValueInterface> permissibleValues = null;
 
 		try
 		{
 			AttributeInterface attribute = entity.getAttributeByName(attributeName);
 			AttributeTypeInformationInterface attributeTypeInformation = attribute.getAttributeTypeInformation();
 			UserDefinedDEInterface userDefinedDE = (UserDefinedDE) attributeTypeInformation.getDataElement();
 
 			if (userDefinedDE == null || userDefinedDE.getPermissibleValueCollection() == null
 					|| userDefinedDE.getPermissibleValueCollection().size() == 0)
 			{
 				permissibleValues = getPermissibleValueList(attributeTypeInformation, desiredPermissibleValues);
 			}
 			else
 			{
 				permissibleValues = getSubsetOfPermissibleValues(attribute, desiredPermissibleValues);
 			}
 		}
 		catch (ParseException parseException)
 		{
 			throw new DynamicExtensionsSystemException("Parse Exception", parseException);
 		}
 		return permissibleValues;
 	}
 
 	/**
 	 *
 	 * @param attributeTypeInformation
 	 * @param desiredPermissibleValues
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private List<PermissibleValueInterface> getSubsetOfPermissibleValues(AttributeInterface attributeInterface, List<String> desiredPermissibleValues)
 			throws DynamicExtensionsApplicationException
 	{
 		List<PermissibleValueInterface> permissibleValues = new ArrayList<PermissibleValueInterface>();
 
 		AttributeTypeInformationInterface attributeTypeInformation = attributeInterface.getAttributeTypeInformation();
 		UserDefinedDEInterface userDefinedDE = (UserDefinedDEInterface) attributeTypeInformation.getDataElement();
 
 		CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 
 		//if no prmissible values are defined, copy  the all the permissible values
 		//of the original attribute
 		if (desiredPermissibleValues == null)
 		{
 			permissibleValues.addAll(userDefinedDE.getPermissibleValueCollection());
 		}
 		else if (categoryManager.isPermissibleValuesSubsetValid(userDefinedDE, desiredPermissibleValues))
 		{
 			permissibleValues = new ArrayList<PermissibleValueInterface>();
 			for (PermissibleValueInterface pv : userDefinedDE.getPermissibleValueCollection())
 			{
 				if (desiredPermissibleValues.contains(pv.getValueAsObject().toString()))
 				{
 					permissibleValues.add(pv);
 				}
 			}
 		}
 		else
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Invalid subset of persmissible values. Original set of permissible values for the attribute " + attributeInterface.getName()
 							+ " of the entity " + attributeInterface.getEntity().getName() + "is different.");
 		}
 		return permissibleValues;
 
 	}
 
 	/**
 	 *
 	 * @param attributeTypeInformation
 	 * @param desiredPermissibleValues
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws ParseException
 	 */
 	public List<PermissibleValueInterface> getPermissibleValueList(AttributeTypeInformationInterface attributeTypeInformation,
 			List<String> desiredPermissibleValues) throws DynamicExtensionsSystemException, ParseException
 	{
 
 		List<PermissibleValueInterface> permissibleValues = new ArrayList<PermissibleValueInterface>();
 		PermissibleValueInterface permissibleValueInterface = null;
 
 		for (String value : desiredPermissibleValues)
 		{
 			permissibleValueInterface = attributeTypeInformation.getPermissibleValueForString(value);
 			permissibleValues.add(permissibleValueInterface);
 		}
 		return permissibleValues;
 	}
 
 	/**
 	 * @param rootContainer
 	 * @param associationName
 	 * @return
 	 */
 	private AbstractContainmentControlInterface getAssociationControl(ContainerInterface rootContainer, String associationName)
 	{
 		AbstractContainmentControlInterface associationControl = null;
 		if (rootContainer == null)
 		{
 			return associationControl;
 		}
 
 		for (ControlInterface controlInterface : rootContainer.getControlCollection())
 		{
 			if (controlInterface instanceof AbstractContainmentControlInterface)
 			{
 				if (controlInterface.getCaption().equals(associationName))
 				{
 					associationControl = (AbstractContainmentControlInterface) controlInterface;
 					break;
 				}
 				else
 				{
 					associationControl = getAssociationControl(((AbstractContainmentControlInterface) controlInterface).getContainer(),
 							associationName);
 				}
 			}
 		}
 		return associationControl;
 	}
 
 }
