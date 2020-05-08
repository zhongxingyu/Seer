 
 package edu.common.dynamicextensions.util.parser;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.CategoryGenerationUtil;
 import edu.common.dynamicextensions.util.CategoryHelper;
 import edu.common.dynamicextensions.util.CategoryHelperInterface;
 import edu.common.dynamicextensions.util.CategoryHelperInterface.ControlEnum;
 
 /**
  * @author kunal_kamble
  * This class creates the catgory/categories defined in 
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
 				//first line in the categopry file is Category_Definition
 				if (categoryFileParser.hasFormDefination())
 				{
 					continue;
 				}
 
 				//category defination in the file starts
 				//1: read the category name
 				CategoryInterface category = categoryHelper.getCategory(categoryFileParser.getCategoryName());
 
 				//2:read the entity group
 				categoryFileParser.readNext();
 				EntityGroupInterface entityGroup = CategoryGenerationUtil.getEntityGroup(category, categoryFileParser.getEntityGroupName());
 
 				checkForNullRefernce(entityGroup, "Entity group with name " + categoryFileParser.getEntityGroupName() + " at line number "
 						+ categoryFileParser.getLineNumber() + " does not exist");
 
 				categoryFileParser.getCategoryValidator().setEntityGroup(entityGroup);
 
 				//3:get the path represneted by ordered entity names
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
 							//always add subcategory to the container
 							sourceContainer = CategoryGenerationUtil.getContainerWithCategoryEntityName(containerCollection, categoryEntityName
 									.get(0));
 						}
 						else
 						{
 							sourceContainer = CategoryGenerationUtil.getContainer(containerCollection, displayLabel);
 						}
 
 						String targetContainerCaption = categoryFileParser.getTargetContainerCaption();
 						ContainerInterface targetContainer = CategoryGenerationUtil.getContainer(containerCollection, targetContainerCaption);
 
 						checkForNullRefernce(targetContainer, "Error at line No:" + categoryFileParser.getLineNumber()
 								+ " Does not found subcategory with name  " + targetContainerCaption);
 
 						String multiplicity = categoryFileParser.getMultiplicity();
 
 						List<AssociationInterface> associationNameList = entityNameAssociationMap
 								.get(((CategoryEntityInterface) CategoryGenerationUtil.getContainer(containerCollection, targetContainerCaption)
 										.getAbstractEntity()).getEntity().getName());
 						checkForNullRefernce(associationNameList, "Error at line No:" + categoryFileParser.getLineNumber() + " Does not found path "
 								+ "for the category entity " + targetContainerCaption);
 
 						lastControl = categoryHelper.associateCategoryContainers(category, entityGroup, sourceContainer, targetContainer,
								associationNameList, CategoryGenerationUtil.getMultiplicityInNumbers(multiplicity));

						categoryHelper.addInstanceInformationToPath(((CategoryEntityInterface) targetContainer.getAbstractEntity()).getPath(),
								categoryEntityNameInstanceMap.get(targetContainer.getAbstractEntity().getName()));
 
 					}
 					else
 					{
 
 						//add control to the container
 						List<String> permissibleValues = categoryFileParser.getPermissibleValues();
 
 						String attributeName = categoryFileParser.getAttributeName();
 
 						entityInterface = entityGroup.getEntityByName(categoryFileParser.getEntityName());
 
 						containerInterface = CategoryGenerationUtil.getContainerWithCategoryEntityName(containerCollection, getcategoryEntityName(
 								categoryEntityName, categoryFileParser.getEntityName()));
 
 						checkForNullRefernce(entityInterface.getAttributeByName(attributeName), "Line Number:" + categoryFileParser.getLineNumber()
 								+ " attribute with name " + attributeName + " in the entity " + entityInterface.getName() + " does not exist!");
 
 						lastControl = categoryHelper.addOrUpdateControl(entityInterface, attributeName, containerInterface, ControlEnum
 								.get(categoryFileParser.getControlType()), categoryFileParser.getControlCaption(), permissibleValues);
 
 						setControlsOptions(lastControl);
 
 					}
 
 					lastControl.setSequenceNumber(sequenceNumber++);
 				}
 
 				CategoryGenerationUtil
 						.setRootContainer(category, containerCollection, entityNameAssociationMap, paths, categoryEntityNameInstanceMap);
 				if (hasRelatedAttributes)
 				{
 					handleRelatedAttributes(entityGroup, category, entityNameAssociationMap);
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
 			throw new DynamicExtensionsSystemException("Error redaring CSV file " + categoryFileParser.getFilePath() + " at line "
 					+ categoryFileParser.getLineNumber(), e);
 		}
 		return categoryList;
 
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
 			Map<String, List<AssociationInterface>> entityNameAssociationMap) throws IOException, ParseException, DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		while (categoryFileParser.readNext())
 		{
 			String[] categoryPaths = categoryFileParser.getCategoryPaths();
 			String[] categoryEntitysInPath = categoryPaths[0].split("->");
 			String categoryEntityName = categoryEntitysInPath[categoryEntitysInPath.length - 1];
 
 			String entityName = categoryEntityName.substring(0, categoryEntityName.indexOf("["));
 			EntityInterface entity = entityGroup.getEntityByName(entityName);
 
 			CategoryHelperInterface categoryHelper = new CategoryHelper();
 			boolean newCategoryCreated = false;
 			if (category.getCategoryEntityByName(categoryEntityName) == null)
 			{
 				newCategoryCreated = true;
 			}
 			CategoryEntityInterface categoryEntity = categoryHelper.createOrUpdateCategoryEntity(category, entity, categoryEntityName);
 
 			categoryFileParser.readNext();
 
 			String attributeName = categoryFileParser.getRelatedAttributeName();
 			CategoryAttributeInterface categoryAttribute = categoryHelper.createCategoryAttribute(entity, attributeName, categoryEntity);
 
 			String defaultValue = categoryFileParser.getDefaultValueForRelatedAttribute();
 			categoryAttribute.setDefaultValue(entity.getAttributeByName(attributeName).getAttributeTypeInformation().getPermissibleValueForString(
 					defaultValue));
 			categoryAttribute.setIsVisible(false);
 			category.addRelatedAttributeCategoryEntity(categoryEntity);
 
 			if (newCategoryCreated)
 			{
 				String associationName = category.getRootCategoryElement() + " to " + categoryEntity.getName() + " association";
 				categoryHelper.associateCategoryEntities(category.getRootCategoryElement(), categoryEntity, associationName, 1, entityGroup,
						entityNameAssociationMap.get(entityName));
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
 	 * @param object
 	 * @param message
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void checkForNullRefernce(Object object, String message) throws DynamicExtensionsSystemException
 	{
 		if (object == null)
 		{
 			throw new DynamicExtensionsSystemException(message);
 		}
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
 
 			if (categoryPaths.length == 1)
 			{
 				categoryEntitysInPath = categoryPaths[0].split("->");
 				categoryEntityName.add(categoryEntitysInPath[categoryEntitysInPath.length - 1]);
 				entityName = categoryEntityName.get(0).substring(0, categoryEntityName.get(0).indexOf("["));
 
 				EntityInterface entity = entityGroup.getEntityByName(entityName);
 				ContainerInterface container = createCategoryEntityAndContainer(entity, categoryEntityName.get(0), displayLable, showCaption,
 						containerCollection, category);
 				categoryEntityNameInstanceMap.put(container.getAbstractEntity().getName(), categoryPaths[0]);
 
 			}
 			else
 			{
 				categoryEntityName.add(CategoryGenerationUtil.getMainCategoryEntityName(categoryPaths));
 				ContainerInterface mainContainer = CategoryGenerationUtil.getContainerWithCategoryEntityName(containerCollection, categoryEntityName
 						.get(0));
 
 				entityName = categoryEntityName.get(0).substring(0, categoryEntityName.get(0).indexOf("["));
 				if (mainContainer == null)
 				{
 					mainContainer = createCategoryEntityAndContainer(entityGroup.getEntityByName(entityName), categoryEntityName.get(0),
 							displayLable, showCaption, containerCollection, category);
 					categoryEntityNameInstanceMap.put(mainContainer.getAbstractEntity().getName(), categoryPaths[0]);
 				}
 
 				CategoryHelperInterface categoryHelper = new CategoryHelper();
 				for (String categoryPath : categoryPaths)
 				{
 					categoryEntitysInPath = categoryPath.split("->");
 					String newCategoryEntityName = categoryEntitysInPath[categoryEntitysInPath.length - 1];
 					if (!categoryEntityName.contains(newCategoryEntityName))
 					{
 						entityName = newCategoryEntityName.substring(0, newCategoryEntityName.indexOf("["));
 						ContainerInterface container = createCategoryEntityAndContainer(entityGroup.getEntityByName(entityName),
 								newCategoryEntityName, null, false, containerCollection, category);
 						categoryEntityNameInstanceMap.put(container.getAbstractEntity().getName(), getCategoryPath(categoryPaths,
 								newCategoryEntityName));
 
 						categoryHelper.associateCategoryContainers(category, entityGroup, mainContainer, container, associationNamesMap
								.get(entityName), 1);
						categoryHelper.addInstanceInformationToPath(((CategoryEntityInterface) container.getAbstractEntity()).getPath(),
								categoryEntityNameInstanceMap.get(container.getAbstractEntity().getName()));
 
 						categoryEntityName.add(newCategoryEntityName);
 					}
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
 }
