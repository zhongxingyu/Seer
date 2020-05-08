 
 package edu.common.dynamicextensions.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.parser.CategoryCSVConstants;
 import edu.common.dynamicextensions.validation.category.CategoryValidator;
 
 /**
  * 
  * @author kunal_kamble
  *
  */
 public class CategoryGenerationUtil
 {
 
 	/**
 	 * This method returns the entity from the entity group.
 	 * @param entityName
 	 * @param entityGroup
 	 * @return
 	 */
 	public static EntityInterface getEntity(String entityName, EntityGroupInterface entityGroup)
 	{
 		EntityInterface entityInterface = entityGroup.getEntityByName(entityName);
 		return entityInterface;
 	}
 
 	/**
 	 * Returns the multiplicity in number for the give string
 	 * @param multiplicity
 	 * @return
 	 */
 	public static int getMultiplicityInNumbers(String multiplicity)
 	{
 		int multiplicityI = 1;
 		if (multiplicity.equalsIgnoreCase(CategoryCSVConstants.MULTILINE))
 		{
 			multiplicityI = -1;
 		}
 		if (multiplicity.equalsIgnoreCase(CategoryCSVConstants.SINGLE))
 		{
 			multiplicityI = 1;
 		}
 		return multiplicityI;
 	}
 
 	/**
 	 * Sets the root category entity for the category.
 	 * @param containerCollection
 	 * @param paths
 	 * @param absolutePath
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public static void setRootContainer(CategoryInterface category, List<ContainerInterface> containerCollection,
 			Map<String, List<AssociationInterface>> paths, Map<String, List<String>> absolutePath, Map<String, String> containerNameInstanceMap)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 
 		ContainerInterface rootContainer = null;
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			CategoryEntityInterface categoryEntityInterface = (CategoryEntityInterface) containerInterface.getAbstractEntity();
 			if (categoryEntityInterface.getTreeParentCategoryEntity() == null)
 			{
 				rootContainer = containerInterface;
 				break;
 			}
 		}
 
 		CategoryHelperInterface categoryHelper = new CategoryHelper();
 
 		for (String entityName : absolutePath.keySet())
 		{
 			if (absolutePath.get(entityName).size() == 1)
 			{
 				CategoryEntityInterface categoryEntityInterface = (CategoryEntityInterface) rootContainer.getAbstractEntity();
 				if (!entityName.equals(categoryEntityInterface.getEntity().getName()))
 				{
 					EntityGroupInterface entityGroup = categoryEntityInterface.getEntity().getEntityGroup();
 					EntityInterface entity = entityGroup.getEntityByName(entityName);
 
 					ContainerInterface newRootContainer = categoryHelper.createOrUpdateCategoryEntityAndContainer(entity, null, category, entityName
 							+ "[1]");
 					newRootContainer.setAddCaption(false);
 
 					categoryHelper.associateCategoryContainers(category, entityGroup, newRootContainer, rootContainer, paths
							.get(categoryEntityInterface.getEntity().getName()), 1);
					categoryHelper.addInstanceInformationToPath(((CategoryEntityInterface) rootContainer.getAbstractEntity()).getPath(),
 							containerNameInstanceMap.get(rootContainer.getAbstractEntity().getName()));
 					rootContainer = newRootContainer;
 
 				}
 			}
 		}
 		categoryHelper.setRootCategoryEntity(rootContainer, category);
 	}
 
 	/**
 	 * Retunrs the container having container caption as one passed to this method.
 	 * @param containerCollection
 	 * @param containerCaption
 	 * @return
 	 */
 	public static ContainerInterface getContainer(List<ContainerInterface> containerCollection, String containerCaption)
 	{
 		ContainerInterface container = null;
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			if (containerCaption.equals(containerInterface.getCaption()))
 			{
 				container = containerInterface;
 				break;
 			}
 
 		}
 		return container;
 	}
 
 	/**
 	 * @param containerCollection
 	 * @param entityName
 	 * @return
 	 */
 	public static ContainerInterface getContainerWithEntityName(List<ContainerInterface> containerCollection, String entityName)
 	{
 		ContainerInterface container = null;
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			CategoryEntityInterface categoryEntity = (CategoryEntityInterface) containerInterface.getAbstractEntity();
 			if (entityName.equals(categoryEntity.getEntity().getName()))
 			{
 				container = containerInterface;
 				break;
 			}
 
 		}
 		return container;
 	}
 
 	/**
 	 * Returns the container with given category entity name.
 	 * @param containerCollection
 	 * @param categoryEntityName
 	 * @return
 	 */
 	public static ContainerInterface getContainerWithCategoryEntityName(List<ContainerInterface> containerCollection, String categoryEntityName)
 	{
 		ContainerInterface container = null;
 		for (ContainerInterface containerInterface : containerCollection)
 		{
 			if (categoryEntityName.equals(containerInterface.getAbstractEntity().getName()))
 			{
 				container = containerInterface;
 				break;
 			}
 
 		}
 		return container;
 	}
 
 	/**
 	 * @param paths
 	 * @param entityGroup
 	 * @return
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public static Map<String, List<AssociationInterface>> getAssociationList(Map<String, List<String>> paths, EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException
 	{
 		Map<String, List<AssociationInterface>> listOfPath = new HashMap<String, List<AssociationInterface>>();
 
 		Set<String> entityNames = paths.keySet();
 		for (String entityName : entityNames)
 		{
 
 			//Path stored is from the root. 
 			List<String> list = paths.get(entityName);
 			List<AssociationInterface> assocaitionList = new ArrayList<AssociationInterface>();
 			Iterator<String> entityNamesIterator = list.iterator();
 
 			String sourceEntityName = entityNamesIterator.next();
 			EntityInterface sourceEntity = entityGroup.getEntityByName(sourceEntityName);
 			CategoryValidator.checkForNullRefernce(sourceEntity, "Entity with name " + sourceEntityName + " does not exist");
 			while (entityNamesIterator.hasNext())
 			{
 				EntityInterface targetEntity = entityGroup.getEntityByName(entityNamesIterator.next());
 				for (AssociationInterface associationInterface : sourceEntity.getAssociationCollection())
 				{
 					if (associationInterface.getTargetEntity() == targetEntity)
 					{
 						assocaitionList.add(associationInterface);
 					}
 				}
 				sourceEntity = targetEntity;
 
 			}
 			listOfPath.put(entityName, assocaitionList);
 		}
 		return listOfPath;
 	}
 
 	/**
 	 * This method gets the relative path.
 	 * @param entityNameList ordered entities names in the path
 	 * @param pathMap 
 	 * @return
 	 */
 	public static List<String> getRelativePath(List<String> entityNameList, Map<String, List<String>> pathMap)
 	{
 		List<String> newEntityNameList = new ArrayList<String>();
 		String lastProcessedEntityName = null;
 		for (String entityName : entityNameList)
 		{
 			if (pathMap.get(entityName) == null)
 			{
 				newEntityNameList.add(entityName);
 			}
 			else
 			{
 				lastProcessedEntityName = entityName;
 			}
 		}
 		if (lastProcessedEntityName != null && !newEntityNameList.contains(lastProcessedEntityName))
 		{
 			newEntityNameList.add(0, lastProcessedEntityName);
 		}
 
 		return newEntityNameList;
 	}
 
 	/**
 	 * Returns the entity group used for careting this category 
 	 * @param category
 	 * @param entityGroupName
 	 * @return
 	 */
 	public static EntityGroupInterface getEntityGroup(CategoryInterface category, String entityGroupName)
 	{
 		if (category.getRootCategoryElement() != null)
 		{
 			return category.getRootCategoryElement().getEntity().getEntityGroup();
 		}
 
 		return DynamicExtensionsUtility.retrieveEntityGroup(entityGroupName);
 
 	}
 
 	/**
 	 * This method finds the main category entity.
 	 * @param categoryPaths
 	 * @return
 	 */
 	public static String getMainCategoryEntityName(String[] categoryPaths)
 	{
 		int minimumNumberOfCategoryEntityNames = categoryPaths[0].split("->").length;
 		for (String string : categoryPaths)
 		{
 			if (minimumNumberOfCategoryEntityNames > string.split("->").length)
 			{
 				minimumNumberOfCategoryEntityNames = string.split("->").length;
 			}
 		}
 		String categoryEntityName = categoryPaths[0].split("->")[0];
 
 		a : for (int i = 0; i < minimumNumberOfCategoryEntityNames; i++)
 		{
 			String temp = categoryPaths[0].split("->")[i];
 			for (String string : categoryPaths)
 			{
 				if (!string.split("->")[i].equals(temp))
 				{
 					break a;
 				}
 			}
 			categoryEntityName = categoryPaths[0].split("->")[i];
 		}
 
 		return categoryEntityName;
 	}
 
 }
