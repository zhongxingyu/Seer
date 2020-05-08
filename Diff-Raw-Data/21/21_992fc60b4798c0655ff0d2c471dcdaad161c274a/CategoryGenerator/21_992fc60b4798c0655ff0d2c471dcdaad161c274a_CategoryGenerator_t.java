 
 package edu.common.dynamicextensions.util.parser;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.CategoryGenerationUtil;
 import edu.common.dynamicextensions.util.CategoryHelper;
 import edu.common.dynamicextensions.util.CategoryHelperInterface;
 import edu.common.dynamicextensions.util.CategoryHelperInterface.ControlEnum;
 import edu.common.dynamicextensions.validation.category.CategoryValidator;
 
 /**
  * @author kunal_kamble
  * This class creates the category/categories defined in 
  * the csv file.
  */
 public class CategoryGenerator
 {
 	private CategoryFileParser categoryFileParser;
 
 	private static final String SET = "set";
 
 	/**
 	 * @param filePath
 	 * @throws DynamicExtensionsSystemException
 	 * @throws FileNotFoundException 
 	 */
 	public CategoryGenerator(String filePath) throws DynamicExtensionsSystemException, FileNotFoundException
 	{
 		categoryFileParser = new CategoryCSVFileParser(filePath);
 	}
 
 	/**
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws ParseException 
 	 */
 	public List<CategoryInterface> getCategoryList() throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException, ParseException
 	{
 		CategoryHelperInterface categoryHelper = new CategoryHelper();
 		List<CategoryInterface> categoryList = new ArrayList<CategoryInterface>();
 		try
 		{
 			while (categoryFileParser.readNext())
 			{
 				// first line in the category file is Category_Definition
 				if (categoryFileParser.hasFormDefination())
 				{
 					continue;
 				}
 
 				//category definition in the file starts
 				//1: read the category name
 				CategoryInterface category = categoryHelper.getCategory(categoryFileParser.getCategoryName());
 
 				//2:read the entity group
 				categoryFileParser.readNext();
 				EntityGroupInterface entityGroup = CategoryGenerationUtil.getEntityGroup(category, categoryFileParser.getEntityGroupName());
 
 				CategoryValidator.checkForNullRefernce(entityGroup, "ERROR AT LINE:" + categoryFileParser.getLineNumber() + "ENTITY GROUP WITH NAME "
 						+ categoryFileParser.getEntityGroupName() + " DOES NOT EXIST");
 
 				categoryFileParser.getCategoryValidator().setEntityGroup(entityGroup);
 
 				//3:get the path represented by ordered entity names
 				categoryFileParser.readNext();
 				Map<String, List<String>> paths = categoryFileParser.getPaths();
 
 				//4:get the association names list
 				Map<String, List<AssociationInterface>> entityNameAssociationMap = CategoryGenerationUtil.getAssociationList(paths, entityGroup);
 
 				List<ContainerInterface> containerCollection = new ArrayList<ContainerInterface>();
 
 				ContainerInterface containerInterface = null;
 				EntityInterface entityInterface = null;
 
 				//5: get the selected attributes and create the controls for them 
 				String displayLabel = null;
 				List<String> categoryEntityName = null;
 				int sequenceNumber = 1;
 				ControlInterface lastControl = null;
 				Map<String, String> categoryEntityNameInstanceMap = new HashMap<String, String>();
 				boolean hasRelatedAttributes = false;
 
 				while (categoryFileParser.readNext())
 				{
 					if (categoryFileParser.hasFormDefination())
 					{
 						break;
 					}
 					if (categoryFileParser.hasRelatedAttributes())
 					{
 						hasRelatedAttributes = true;
 						break;
 					}
 
 					if (categoryFileParser.hasDisplayLable())
 					{
 						displayLabel = categoryFileParser.getDisplyLable();
 						categoryEntityName = createForm(entityGroup, containerCollection, entityNameAssociationMap, category,
 								categoryEntityNameInstanceMap);
 						categoryFileParser.readNext();
 					}
 
 					if (categoryFileParser.hasSubcategory())
 					{
 
 						ContainerInterface sourceContainer = null;
 						if (entityInterface != null)
 						{
 							//always add sub-category to the container
 							sourceContainer = CategoryGenerationUtil.getContainerWithCategoryEntityName(containerCollection, categoryEntityName
 									.get(0));
 						}
 						else
 						{
 							sourceContainer = CategoryGenerationUtil.getContainer(containerCollection, displayLabel);
 						}
 
 						String targetContainerCaption = categoryFileParser.getTargetContainerCaption();
 						ContainerInterface targetContainer = CategoryGenerationUtil.getContainer(containerCollection, targetContainerCaption);
 
 						CategoryValidator.checkForNullRefernce(targetContainer, "ERROR AT LINE:" + categoryFileParser.getLineNumber()
 								+ " DOES NOT FOUND THE SUBCATEGORY WITH THE NAME " + targetContainerCaption);
 
 						String multiplicity = categoryFileParser.getMultiplicity();
 
 						((CategoryEntityInterface) CategoryGenerationUtil.getContainer(containerCollection, targetContainerCaption)
 								.getAbstractEntity()).getEntity();
 						String entityName = ((CategoryEntityInterface) CategoryGenerationUtil.getContainer(containerCollection,
 								targetContainerCaption).getAbstractEntity()).getEntity().getName();
 						List<AssociationInterface> associationNameList = entityNameAssociationMap.get(entityName);
 						CategoryValidator.checkForNullRefernce(associationNameList, "ERROR AT LINE:" + categoryFileParser.getLineNumber()
 								+ " DOES NOT FOUND PATH " + "FOR THE CATEGORY ENTITY " + targetContainerCaption);
 
 						lastControl = categoryHelper.associateCategoryContainers(category, entityGroup, sourceContainer, targetContainer,
 								associationNameList, CategoryGenerationUtil.getMultiplicityInNumbers(multiplicity), categoryEntityNameInstanceMap
 										.get(targetContainer.getAbstractEntity().getName()));
 					}
 					else
 					{
 						//add control to the container
 						Map<String, Collection<SemanticPropertyInterface>> permissibleValues = categoryFileParser.getPermissibleValues();
 
 						String attributeName = categoryFileParser.getAttributeName();
 
 						entityInterface = entityGroup.getEntityByName(categoryFileParser.getEntityName());
 						//Added for Category-inheritance ,check if a given attribute is parent category attribute or not
 						boolean isParentAttribute = true;
 
 						Iterator attrIterator = entityInterface.getAttributeCollection().iterator();
 						while (attrIterator.hasNext())
 						{
 
 							AttributeInterface objAttribute = (AttributeInterface) attrIterator.next();
 							if (attributeName.equals(objAttribute.getName()))
 							{
 								isParentAttribute = false;
 								break;
 							}
 						}
 
 						boolean isAttributeCategoryMatched = false;
 						CategoryValidator.checkForNullRefernce(getcategoryEntityName(categoryEntityName, categoryFileParser.getEntityName()),
 								"ERROR: INSTANCE INFORMATION IS NOT " + "IN THE CORRECT FORMAT" + categoryEntityName);
 						containerInterface = CategoryGenerationUtil.getContainerWithCategoryEntityName(containerCollection, getcategoryEntityName(
 								categoryEntityName, categoryFileParser.getEntityName()));
 
 						AttributeInterface attribute = entityInterface.getAttributeByName(attributeName);
 
 						CategoryValidator.checkForNullRefernce(attribute, "ERROR AT LINE:" + categoryFileParser.getLineNumber()
 								+ " ATTRIBUTE WITH NAME " + attributeName + " DOES NOT FOUND IN THE ENTITY " + entityInterface.getName());
 
 						//if this is parent attribute and currently the parent category entity is not created for given categoryentity,create 
 						//parent category hierarchy up to where attribute is found 
 						if (isParentAttribute)
 						{
 
 							EntityInterface parentEntity = entityInterface.getParentEntity();
 							EntityInterface childEntity = entityInterface;
 							CategoryEntityInterface childCategoryEntity = (CategoryEntityInterface) containerInterface.getAbstractEntity();
 							CategoryEntityInterface parentCategoryEntity = childCategoryEntity.getParentCategoryEntity();
 
 							while (childEntity.getParentEntity() != null)
 							{
 								parentEntity = childEntity.getParentEntity();
 								//check whether the given cat.entity's parent ce is created or not if not create it 
 								if (parentCategoryEntity == null)
 								{
 									ContainerInterface parentContainer = createParentCategoryEntity(childCategoryEntity, parentEntity, entityGroup,
 											containerCollection);
 									parentCategoryEntity = (CategoryEntityInterface) parentContainer.getAbstractEntity();
 									ContainerInterface childcontainerInterface = CategoryGenerationUtil.getContainerWithCategoryEntityName(
 											containerCollection, childCategoryEntity.getName());
 									childcontainerInterface.setBaseContainer(parentContainer);
 								}
 
 								//Iterate over parent entity's attribute ,check whether its present in parententity
 								Iterator parentattrIterator = parentEntity.getAttributeCollection().iterator();
 								while (parentattrIterator.hasNext())
 								{
 									AttributeInterface objParentAttribute = (AttributeInterface) parentattrIterator.next();
 									if (attributeName.equals(objParentAttribute.getName()))
 									{
 										isAttributeCategoryMatched = true;
 										break;
 									}
 								}
 
 								childEntity = childEntity.getParentEntity();
 								childCategoryEntity = parentCategoryEntity;
 								parentCategoryEntity = parentCategoryEntity.getParentCategoryEntity();
 								//If attribute found in parent categoryentity ,break out of loop
 								if (isAttributeCategoryMatched)
 									break;
 
 							}
 							entityInterface = childEntity;
 							containerInterface = (ContainerInterface) (childCategoryEntity.getContainerCollection()).iterator().next();
 						}
 
 						Map<String, Object> rulesMap = categoryFileParser.getRules();
 						if (rulesMap != null && !rulesMap.isEmpty())
 						{
 							CategoryValidator.checkRangeAgainstAttributeValueRange(attribute, rulesMap);
 						}

 						lastControl = categoryHelper.addOrUpdateControl(entityInterface, attributeName, containerInterface, ControlEnum
 								.get(categoryFileParser.getControlType()), categoryFileParser.getControlCaption(), rulesMap, permissibleValues);
 
 						//set default value to attribute's IsRelatedAttribute and IsVisible property-Its required incase of CategoryEntity Edit
 						((CategoryAttributeInterface) lastControl.getAttibuteMetadataInterface()).setIsRelatedAttribute(false);
 						((CategoryAttributeInterface) lastControl.getAttibuteMetadataInterface()).setIsVisible(true);
 						//Set default value to control's option -Its required incase of CategoryEntity Edit
 						categoryHelper.setDefaultControlsOptions(lastControl, ControlEnum.get(categoryFileParser.getControlType()));
 						//Clear categoryentity from relatedattribute collection of root category entity
 						category.removeRelatedAttributeCategoryEntity((CategoryEntityInterface) containerInterface.getAbstractEntity());
 
 						setControlsOptions(lastControl);
 						setDefaultValue(lastControl);
 						//Check for isreadonly option
 						if (lastControl.getIsReadOnly())
 						{
 							((CategoryAttributeInterface) lastControl.getAttibuteMetadataInterface()).setIsRelatedAttribute(true);
 							((CategoryAttributeInterface) lastControl.getAttibuteMetadataInterface()).setIsVisible(true);
 							category.addRelatedAttributeCategoryEntity((CategoryEntityInterface) containerInterface.getAbstractEntity());
 						}
 					}
 
 				}
 
 				CategoryGenerationUtil
 						.setRootContainer(category, containerCollection, entityNameAssociationMap, paths, categoryEntityNameInstanceMap);
 
 				if (hasRelatedAttributes)
 				{
 					handleRelatedAttributes(entityGroup, category, entityNameAssociationMap, containerCollection);
 				}
 				categoryList.add(category);
 			}
 		}
 		catch (FileNotFoundException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while openig CSV file ", e);
 		}
 		catch (IOException e)
 		{
 			throw new DynamicExtensionsSystemException("FATAL ERROR READING FILE" + categoryFileParser.getFilePath() + " AT LINE "
 					+ categoryFileParser.getLineNumber(), e);
 		}
		catch (Exception e)
		{
			if (!(e instanceof DynamicExtensionsSystemException))
			{
				throw new DynamicExtensionsSystemException("FATAL ERROR AT LINE: " + categoryFileParser.getLineNumber() + "READING FILE "
						+ categoryFileParser.getFilePath(), e);
			}
			throw new DynamicExtensionsSystemException("", e);
		}
 		return categoryList;
 	}
 
 	/**
 	 * @param childCategoryEntity
 	 * @param parentEntity
 	 * @param entityGroup
 	 * @param containerCollection
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ContainerInterface createParentCategoryEntity(CategoryEntityInterface childCategoryEntity, EntityInterface parentEntity,
 			EntityGroupInterface entityGroup, Collection<ContainerInterface> containerCollection) throws DynamicExtensionsSystemException
 	{
 		String newCategoryEntityName = parentEntity.getName() + "[1]";
 		CategoryHelper categoryHelper = new CategoryHelper();
 		CategoryInterface parentCategory = categoryHelper.getCategory(newCategoryEntityName);
 
 		ContainerInterface parentContainer = createCategoryEntityAndContainer(entityGroup.getEntityByName(parentEntity.getName()),
 				newCategoryEntityName, newCategoryEntityName, false, containerCollection, parentCategory);
 		((CategoryEntityInterface) childCategoryEntity).setParentCategoryEntity((CategoryEntityInterface) parentContainer.getAbstractEntity());
 		CategoryEntity parentCEntity = (CategoryEntity) parentContainer.getAbstractEntity();
 		parentCEntity.addChildCategory(childCategoryEntity);
 
 		CategoryEntity parentCategoryEntity = (CategoryEntity) ((CategoryEntityInterface) childCategoryEntity).getParentCategoryEntity();
 		parentCategoryEntity.setCreateTable(false);
 		return parentContainer;
 	}
 
 	/**
 	 * @param entityGroup
 	 * @param category
 	 * @param entityNameAssociationMap
 	 * @throws IOException
 	 * @throws ParseException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void handleRelatedAttributes(EntityGroupInterface entityGroup, CategoryInterface category,
 			Map<String, List<AssociationInterface>> entityNameAssociationMap, List<ContainerInterface> containerCollection) throws IOException,
 			ParseException, DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		while (categoryFileParser.readNext())
 		{
 			String[] categoryPaths = categoryFileParser.getCategoryPaths();
 			String[] categoryEntitysInPath = categoryPaths[0].split("->");
 			String categoryEntityName = categoryEntitysInPath[categoryEntitysInPath.length - 1];
 
 			String entityName = categoryEntityName.substring(0, categoryEntityName.indexOf("["));
 			EntityInterface entity = entityGroup.getEntityByName(entityName);
 			categoryFileParser.readNext();
 			String attributeName = categoryFileParser.getRelatedAttributeName();
 			CategoryHelperInterface categoryHelper = new CategoryHelper();
 			boolean newCategoryCreated = false;
 			if (category.getCategoryEntityByName(categoryEntityName) == null)
 			{
 				newCategoryCreated = true;
 			}
 			CategoryEntityInterface categoryEntity = categoryHelper.createOrUpdateCategoryEntity(category, entity, categoryEntityName);
 
 			if (newCategoryCreated)
 			{
 				String associationName = category.getRootCategoryElement() + " to " + categoryEntity.getName() + " association";
 				categoryHelper.associateCategoryEntities(category.getRootCategoryElement(), categoryEntity, associationName, 1, entityGroup,
 						entityNameAssociationMap.get(entityName), categoryPaths[0]);
 			}
 			CategoryValidator.checkForNullRefernce(entity.getAttributeByName(attributeName), "ERROR AT LINE:" + categoryFileParser.getLineNumber()
 					+ " ATTRIBUTE WITH NAME " + attributeName + " DOES NOT FOUND IN THE ENTITY " + entity.getName());
 
 			//Added for category inheritance
 			boolean isParentAttribute = true;
 			Iterator attrIterator = entity.getAttributeCollection().iterator();
 			while (attrIterator.hasNext())
 			{
 
 				AttributeInterface objAttribute = (AttributeInterface) attrIterator.next();
 				if (attributeName.equals(objAttribute.getName()))
 				{
 					isParentAttribute = false;
 					break;
 				}
 			}
 			boolean isAttributeCategoryMatched = false;
 			//if this is parent attribute and currently the parent categoryentity is not created for given categoryentity,create 
 			//parent category hierarchy upto where attribute is found 
 			if (isParentAttribute)
 			{
 				EntityInterface parentEntity = entity.getParentEntity();
 				EntityInterface childEntity = entity;
 				CategoryEntityInterface childCategoryEntity = categoryEntity;
 				CategoryEntityInterface parentCategoryEntity = childCategoryEntity.getParentCategoryEntity();
 
 				while (childEntity.getParentEntity() != null)
 				{
 					parentEntity = childEntity.getParentEntity();
 					//check whether the given cat.entity's parent ce is created or not if not create it 
 					if (parentCategoryEntity == null)
 					{
 						ContainerInterface parentContainer = createParentCategoryEntity(childCategoryEntity, parentEntity, entityGroup,
 								containerCollection);
 						parentCategoryEntity = (CategoryEntityInterface) parentContainer.getAbstractEntity();
 
 						ContainerInterface childcontainerInterface = CategoryGenerationUtil.getContainerWithCategoryEntityName(containerCollection,
 								childCategoryEntity.getName());
 						childcontainerInterface.setBaseContainer(parentContainer);
 					}
 
 					//Iterate over parent entity's attribute ,check whether its present in parententity
 					Iterator parentattrIterator = parentEntity.getAttributeCollection().iterator();
 					while (parentattrIterator.hasNext())
 					{
 						AttributeInterface objParentAttribute = (AttributeInterface) parentattrIterator.next();
 						if (attributeName.equals(objParentAttribute.getName()))
 						{
 							isAttributeCategoryMatched = true;
 							break;
 
 						}
 
 					}
 					childEntity = childEntity.getParentEntity();
 					childCategoryEntity = parentCategoryEntity;
 					parentCategoryEntity = parentCategoryEntity.getParentCategoryEntity();
 					//If attribute found in parent categoryentity ,break out of loop
 					if (isAttributeCategoryMatched)
 						break;
 
 				}
 				entity = childEntity;
 				categoryEntity = childCategoryEntity;
 			}
 
 			CategoryAttributeInterface categoryAttribute = categoryHelper.createCategoryAttribute(entity, attributeName, categoryEntity);
 
 			String defaultValue = categoryFileParser.getDefaultValueForRelatedAttribute();
 			categoryAttribute.setDefaultValue(entity.getAttributeByName(attributeName).getAttributeTypeInformation().getPermissibleValueForString(
 					defaultValue));
 			categoryAttribute.setIsVisible(false);
 			categoryAttribute.setIsRelatedAttribute(true);
 			category.addRelatedAttributeCategoryEntity(categoryEntity);
 
 			if (newCategoryCreated)
 			{
 				String associationName = category.getRootCategoryElement() + " to " + categoryEntity.getName() + " association";
 				categoryHelper.associateCategoryEntities(category.getRootCategoryElement(), categoryEntity, associationName, 1, entityGroup,
 						entityNameAssociationMap.get(entityName), categoryPaths[0]);
 			}
 		}
 
 	}
 
 	/**
 	 * @param categoryEntityNameList
 	 * @param entityName
 	 * @return
 	 */
 	private String getcategoryEntityName(List<String> categoryEntityNameList, String entityName)
 	{
 		String categoryEntityName = null;
 		for (String string : categoryEntityNameList)
 		{
 			if (entityName.equals(string.substring(0, string.indexOf("["))))
 			{
 				categoryEntityName = string;
 			}
 
 		}
 		return categoryEntityName;
 	}
 
 	/**
 	 * @param entityInterface
 	 * @param categoryEntityName
 	 * @param displayLable
 	 * @param showCaption
 	 * @param containerCollection
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ContainerInterface createCategoryEntityAndContainer(EntityInterface entityInterface, String categoryEntityName, String displayLable,
 			Boolean showCaption, Collection<ContainerInterface> containerCollection, CategoryInterface category)
 			throws DynamicExtensionsSystemException
 	{
 		ContainerInterface containerInterface = null;
 		CategoryHelperInterface categoryHelper = new CategoryHelper();
 
 		containerInterface = categoryHelper.createOrUpdateCategoryEntityAndContainer(entityInterface, displayLable, category, categoryEntityName);
 
 		containerInterface.setAddCaption(showCaption);
 
 		containerCollection.add(containerInterface);
 
 		return containerInterface;
 	}
 
 	/**
 	 * @param control
 	 * @param nextLine
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void setControlsOptions(ControlInterface control) throws DynamicExtensionsSystemException
 	{
 		try
 		{
 			Map<String, String> controlOptions = categoryFileParser.getControlOptions();
 
 			if (controlOptions.isEmpty())
 			{
 				return;
 			}
 			for (String optionString : controlOptions.keySet())
 			{
 				String methodName = SET + optionString;
 
 				Class[] types = getParameterType(methodName, control);
				if (types.length < 1)
				{
					throw new DynamicExtensionsSystemException("ERROR AT LINE:" + categoryFileParser.getLineNumber() + " INVALID CONTROL OPTION "
							+ optionString);
				}
 				List<Object> values = new ArrayList<Object>();
 				values.add(getFormattedValues(types[0], controlOptions.get(optionString)));
 
 				Method method;
 
 				method = control.getClass().getMethod(methodName, types);
 
 				method.invoke(control, values.toArray());
 
 			}
 		}
 		catch (SecurityException e)
 		{
 			throw new DynamicExtensionsSystemException("Please conatct administartor", e);
 		}
 		catch (NoSuchMethodException e)
 		{
 			throw new DynamicExtensionsSystemException("Line number:" + categoryFileParser.getLineNumber() + "Incorrect option", e);
 		}
 		catch (IllegalArgumentException e)
 		{
 			throw new DynamicExtensionsSystemException("Please conatct administartor", e);
 		}
 		catch (IllegalAccessException e)
 		{
 			throw new DynamicExtensionsSystemException("Please conatct administartor", e);
 		}
 		catch (InvocationTargetException e)
 		{
 			throw new DynamicExtensionsSystemException("Please conatct administartor", e);
 		}
 		catch (InstantiationException e)
 		{
 			throw new DynamicExtensionsSystemException("Please conatct administartor", e);
 		}
 
 	}
 
 	/**
 	 * This meth
 	 * @param methodName
 	 * @param object
 	 * @return
 	 */
 	private Class[] getParameterType(String methodName, Object object)
 	{
 		Class[] parameterTypes = new Class[0];
 		for (Method method : object.getClass().getMethods())
 		{
 			if (methodName.equals(method.getName()))
 			{
 				parameterTypes = method.getParameterTypes();
 			}
 		}
 
 		return parameterTypes;
 	}
 
 	/**
 	 * @param type
 	 * @param string
 	 * @return
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 * @throws NoSuchMethodException 
 	 * @throws SecurityException 
 	 * @throws InvocationTargetException 
 	 * @throws IllegalArgumentException 
 	 */
 	private Object getFormattedValues(Class type, String string) throws SecurityException, NoSuchMethodException, InstantiationException,
 			IllegalAccessException, IllegalArgumentException, InvocationTargetException
 	{
 		Method method = type.getMethod("valueOf", new Class[]{String.class});
 		return method.invoke(type, new Object[]{string});
 	}
 
 	/**
 	 * @param entityGroup
 	 * @param containerCollection
 	 * @param associationNamesMap
 	 * @param category
 	 * @param categoryEntityNameInstanceMap
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private List<String> createForm(EntityGroupInterface entityGroup, List<ContainerInterface> containerCollection,
 			Map<String, List<AssociationInterface>> associationNamesMap, CategoryInterface category, Map<String, String> categoryEntityNameInstanceMap)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		String displayLable = categoryFileParser.getDisplyLable();
 		Boolean showCaption = categoryFileParser.isShowCaption();
 
 		List<String> categoryEntityName = new ArrayList<String>();
 		String entityName = null;
 		String[] categoryEntitysInPath;
 
 		try
 		{
 			categoryFileParser.readNext();
 			String[] categoryPaths = categoryFileParser.getCategoryPaths();
 
 			for (String categoryPath : categoryPaths)
 			{
 				categoryEntitysInPath = categoryPath.split("->");
 				String newCategoryEntityName = categoryEntitysInPath[categoryEntitysInPath.length - 1];
 				if (!categoryEntityName.contains(newCategoryEntityName))
 				{
 					entityName = newCategoryEntityName.substring(0, newCategoryEntityName.indexOf("["));
 					ContainerInterface container = null;
 					container = createCategoryEntityAndContainer(entityGroup.getEntityByName(entityName), newCategoryEntityName, displayLable,
 							showCaption, containerCollection, category);
 
 					categoryEntityNameInstanceMap.put(container.getAbstractEntity().getName(), getCategoryPath(categoryPaths, newCategoryEntityName));
 
 					categoryEntityName.add(newCategoryEntityName);
 				}
 			}
 
 		}
 		catch (IOException exception)
 		{
 			throw new DynamicExtensionsSystemException("Line number:" + categoryFileParser.getLineNumber() + ": Error reading category entity path");
 		}
 
 		return categoryEntityName;
 	}
 
 	/**
 	 * @param categoryPaths
 	 * @param newCategoryEntityName
 	 * @return
 	 */
 	private String getCategoryPath(String[] categoryPaths, String newCategoryEntityName)
 	{
 		String categoryPath = null;
 		for (String string : categoryPaths)
 		{
 			if (string.endsWith(newCategoryEntityName))
 				categoryPath = string;
 		}
 		return categoryPath;
 	}
 
 	/**
 	 * @param control
 	 * @throws ParseException
 	 */
 	private void setDefaultValue(ControlInterface control) throws ParseException
 	{
 		if (categoryFileParser.getDefaultValue() == null)
 		{
 			return;
 		}
 
 		CategoryAttributeInterface categoryAttribute = (CategoryAttributeInterface) control.getAttibuteMetadataInterface();
 		if (!categoryFileParser.getDefaultValue().equals(categoryAttribute.getDefaultValue()))
 		{
 			categoryAttribute.setDefaultValue(categoryAttribute.getAttribute().getAttributeTypeInformation().getPermissibleValueForString(
 					categoryFileParser.getDefaultValue()));
 		}
 	}
 
 }
