 
 package edu.wustl.cab2b.client.metadatasearch;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.SemanticAnnotatableInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.StringValueInterface;
 import edu.wustl.cab2b.common.beans.MatchedClass;
 import edu.wustl.cab2b.common.beans.MatchedClassEntry;
 import edu.wustl.cab2b.common.beans.MatchedClassEntry.MatchCause;
 import edu.wustl.cab2b.common.cache.IEntityCache;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.util.AlphabeticalComparator;
 import edu.wustl.cab2b.common.util.Constants;
 
 /**
  * @author hrishikesh_rajpathak
  * @author gautam_shetty
  */
 public class MetadataSearch
 {
 
 	/**
 	 * List of all Entity objects.
 	 */
 	private static IEntityCache entityCache;
 
 	/**
 	 * Constructor initializing the EntityCache.
 	 * 
 	 * @param entityCache
 	 *            The EntityCache object.
 	 */
 	public MetadataSearch(IEntityCache entityCache)
 	{
 		MetadataSearch.entityCache = entityCache;
 	}
 
 	/**
 	 * Searches the array of search strings on the search targets specified.
 	 * 
 	 * @param searchTarget
 	 *            The search targets to search on.
 	 * @param searchString
 	 *            The search strings.
 	 * @param basedOn
 	 *            The based on fields.
 	 * @return The Matched class object containing all the entities found in the search.
 	 * @throws CheckedException
 	 */
 	public MatchedClass search(int[] searchTarget, String[] searchString, int basedOn)
 			throws CheckedException
 	{
 		if (searchTarget == null || searchString == null)
 		{
 			throw new CheckedException("Search target/string cannot be null");
 		}
 		MatchedClass matchedClass = null;
 		switch (basedOn)
 		{
 			case Constants.BASED_ON_TEXT :
 				matchedClass = searchText(searchString, searchTarget);
 				break;
 			case Constants.BASED_ON_CONCEPT_CODE :
 				matchedClass = searchConceptCode(searchString, searchTarget);
 				break;
 			default :
 				throw new CheckedException("Search target does not exist : " + searchTarget);
 		}
 		matchedClass.setEntityCollection(matchedClass.getSortedEntityCollection());
 		MatchedClass resultantMatchedClassSorted = sortResults(matchedClass);
		return matchedClass;
 	}
 
 	/**
 	 * Searches the TEXT for array of strings passed. Returns the MatchedClass instance containing the matched
 	 * entities in the search. Splits the search on the basis of "searchTarget" parameter.
 	 * 
 	 * @param searchTarget
 	 *            The target on which the search is to be performed.
 	 * @param searchString
 	 *            The string to be searched.
 	 * @return the MatchedClass instance containing the matched entities in the search.
 	 * @throws CheckedException
 	 */
 	private MatchedClass searchText(String[] searchString, int[] searchTarget)
 			throws CheckedException
 	{
 		MatchedClass resultantMatchedClass = new MatchedClass();
 		for (int i = 0; i < searchTarget.length; i++)
 		{
 			MatchedClass matchedClass = null;
 			Collection<EntityInterface> entityCollection = null;
 			Collection<AttributeInterface> attributeCollection = null;
 			switch (searchTarget[i])
 			{
 				case Constants.CLASS :
 					entityCollection = createSearchEntity(searchString);
 					matchedClass = entityCache.getEntityOnEntityParameters(entityCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					matchedClass = entityCache.getCategories(entityCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				case Constants.CLASS_WITH_DESCRIPTION :
 					entityCollection = createSearchEntityWithDesc(searchString);
 					matchedClass = entityCache.getEntityOnEntityParameters(entityCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					matchedClass = entityCache.getCategories(entityCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				case Constants.ATTRIBUTE :
 					attributeCollection = createSearchAttribute(searchString);
 					matchedClass = entityCache.getEntityOnAttributeParameters(attributeCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					matchedClass = entityCache.getCategoriesAttributes(attributeCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				case Constants.ATTRIBUTE_WITH_DESCRIPTION :
 					attributeCollection = createSearchAttributeWithDesc(searchString);
 					matchedClass = entityCache.getEntityOnAttributeParameters(attributeCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					matchedClass = entityCache.getCategoriesAttributes(attributeCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				case Constants.PERMISSIBLEVALUE :
 					Collection<PermissibleValueInterface> pvCollection = createSearchPermissibleValue(searchString);
 					matchedClass = entityCache.getEntityOnPermissibleValueParameters(pvCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				default :
 					throw new CheckedException("Search target does not exist : " + searchTarget[i]);
 			}
 		}
 		return resultantMatchedClass;
 	}
 
 	/**
 	 * Collects the results from individual searches and adds it to the resultClass to show all the results
 	 * together
 	 * 
 	 * @param resultClass
 	 *            Instance of MatchedClass to which all the results are added.
 	 * @param matchClass
 	 *            Instance of MatchedClass which holds the results of current execution.
 	 * @return the MatchedClass instance containing the matched entities in the search.
 	 */
 	private MatchedClass createResultClass(MatchedClass resultClass, MatchedClass matchClass)
 	{
 		for (MatchedClassEntry matchedClassEntry : matchClass.getMatchedClassEntries())
 		{
 			resultClass.addMatchedClassEntry(matchedClassEntry);
 		}
 		return resultClass;
 	}
 
 	/**
 	 * Creates and returns the Entity instance with entity name set as each search string entered for search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private Collection<EntityInterface> createSearchEntity(String[] searchString)
 			throws CheckedException
 	{
 		Collection<EntityInterface> entityCollection = new HashSet<EntityInterface>();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			EntityInterface entity = getEntity(searchString[i], null);
 			entityCollection.add(entity);
 		}
 		return entityCollection;
 	}
 
 	/**
 	 * Creates and returns the Entity instance with entity name and entity description set as each search string
 	 * entered for search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private Collection<EntityInterface> createSearchEntityWithDesc(String[] searchString)
 			throws CheckedException
 	{
 		Collection<EntityInterface> entityCollection = new HashSet<EntityInterface>();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			EntityInterface entity = getEntity(searchString[i], searchString[i]);
 			entityCollection.add(entity);
 		}
 		return entityCollection;
 	}
 
 	/**
 	 * Adds name and description to the entity
 	 * 
 	 * @param name
 	 *            the name of entity string.
 	 * @param desc
 	 *            the description of entity string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private EntityInterface getEntity(String name, String desc)
 	{
 		EntityInterface entity = DomainObjectFactory.getInstance().createEntity();
 		entity.setName(name);
 		entity.setDescription(desc);
 		return entity;
 	}
 
 	/**
 	 * Creates and returns the Attribute instance with Attribute name set as each search string entered for search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private Collection<AttributeInterface> createSearchAttribute(String[] searchString)
 			throws CheckedException
 	{
 		Collection<AttributeInterface> attributeCollection = new HashSet<AttributeInterface>();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			AttributeInterface attribute = getAttribute(searchString[i], null);
 			attributeCollection.add(attribute);
 		}
 		return attributeCollection;
 	}
 
 	/**
 	 * Creates and returns the Attribute instance with Attribute name and Attribute description set as each search
 	 * string entered for search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private Collection<AttributeInterface> createSearchAttributeWithDesc(String[] searchString)
 			throws CheckedException
 	{
 		Collection<AttributeInterface> attributeCollection = new HashSet<AttributeInterface>();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			AttributeInterface attribute = getAttribute(searchString[i], searchString[i]);
 			attributeCollection.add(attribute);
 		}
 		return attributeCollection;
 	}
 
 	/**
 	 * Adds name and description to the attribute
 	 * 
 	 * @param name
 	 *            the name of attribute string.
 	 * @param name
 	 *            the description of attribute string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private AttributeInterface getAttribute(String name, String desc)
 	{
 		AttributeInterface attribute = DomainObjectFactory.getInstance().createStringAttribute();
 		attribute.setName(name);
 		attribute.setDescription(desc);
 		return attribute;
 	}
 
 	/**
 	 * Creates and returns the Attribute instance with Permissible Value of an attribute set as each search string
 	 * entered for search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private Collection<PermissibleValueInterface> createSearchPermissibleValue(String[] searchString)
 			throws CheckedException
 	{
 		Collection<PermissibleValueInterface> permissibleValueCollection = new HashSet<PermissibleValueInterface>();
 		DomainObjectFactory deFactory = DomainObjectFactory.getInstance();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			StringValueInterface value = deFactory.createStringValue();
 			value.setValue(searchString[i]);
 			permissibleValueCollection.add(value);
 		}
 		return permissibleValueCollection;
 	}
 
 	/**
 	 * Searches the CONCEPT CODE for array of strings passed. Returns the MatchedClass instance containing the
 	 * matched entities in the search. Splits the search on the basis of "searchTarget" parameter.
 	 * 
 	 * @param searchTarget
 	 *            The target on which the search is to be performed.
 	 * @param searchString
 	 *            The string to be searched.
 	 * @return the MatchedClass instance containing the matched entities in the search.
 	 * @throws CheckedException
 	 */
 	private MatchedClass searchConceptCode(String[] searchString, int[] searchTarget)
 			throws CheckedException
 	{
 		MatchedClass resultantMatchedClass = new MatchedClass();
 		for (int i = 0; i < searchTarget.length; i++)
 		{
 			MatchedClass matchedClass = null;
 			Collection<EntityInterface> entityCollection = null;
 			Collection<AttributeInterface> attributeCollection = null;
 			switch (searchTarget[i])
 			{
 				case Constants.CLASS :
 					entityCollection = createSearchEntityConceptCode(searchString);
 					matchedClass = entityCache.getEntityOnEntityParameters(entityCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				case Constants.ATTRIBUTE :
 					attributeCollection = createSearchAttributeConceptCode(searchString);
 					matchedClass = entityCache.getEntityOnAttributeParameters(attributeCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				case Constants.PERMISSIBLEVALUE :
 					Collection<PermissibleValueInterface> pvCollection = createSearchPermissibleValueConceptCode(searchString);
 					matchedClass = entityCache.getEntityOnPermissibleValueParameters(pvCollection);
 					resultantMatchedClass = createResultClass(resultantMatchedClass, matchedClass);
 					break;
 				default :
 					throw new CheckedException("Search target does not exist : " + searchTarget);
 			}
 		}
 		return resultantMatchedClass;
 	}
 
 	/**
 	 * Creates and returns the Entity instance collection with concept code set as each search string entered for
 	 * search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private Collection<EntityInterface> createSearchEntityConceptCode(String[] searchString)
 			throws CheckedException
 	{
 		Collection<EntityInterface> entityCollection = new HashSet<EntityInterface>();
 		DomainObjectFactory deFactory = DomainObjectFactory.getInstance();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			EntityInterface entity = deFactory.createEntity();
 			addConceptCode(entity, searchString[i]);
 			entityCollection.add(entity);
 		}
 		return entityCollection;
 	}
 
 	/**
 	 * Creates and returns the Attibute instance collection with concept code set as each search string entered for
 	 * search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the Entity instance which is to be searched.
 	 */
 	private Collection<AttributeInterface> createSearchAttributeConceptCode(String[] searchString)
 			throws CheckedException
 	{
 		Collection<AttributeInterface> attributeCollection = new HashSet<AttributeInterface>();
 		DomainObjectFactory deFactory = DomainObjectFactory.getInstance();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			AttributeInterface attribute = deFactory.createStringAttribute();
 			addConceptCode(attribute, searchString[i]);
 			attributeCollection.add(attribute);
 		}
 		return attributeCollection;
 	}
 
 	/**
 	 * @param owner
 	 * @param conceptCodeStrings
 	 */
 	private void addConceptCode(SemanticAnnotatableInterface owner, String conceptCodeStrings)
 	{
 		SemanticPropertyInterface semanticProperty = DomainObjectFactory.getInstance()
 				.createSemanticProperty();
 		semanticProperty.setConceptCode(conceptCodeStrings);
 		owner.addSemanticProperty(semanticProperty);
 	}
 
 	/**
 	 * Creates and returns the Permissible value instance collection with concept code set as each search string
 	 * entered for search.
 	 * 
 	 * @param searchString
 	 *            the search string.
 	 * @return the collection of StringPermissibleValue instances which is to be searched.
 	 */
 	private Collection<PermissibleValueInterface> createSearchPermissibleValueConceptCode(
 			String[] searchString) throws CheckedException
 	{
 		Collection<PermissibleValueInterface> permissibleValueCollection = new HashSet<PermissibleValueInterface>();
 		DomainObjectFactory deFactory = DomainObjectFactory.getInstance();
 		for (int i = 0; i < searchString.length; i++)
 		{
 			PermissibleValueInterface value = deFactory.createStringValue();
 			addConceptCode(value, searchString[i]);
 			permissibleValueCollection.add(value);
 		}
 		return permissibleValueCollection;
 	}
 
 	/**
 	 * This method sorts the result in alphabetical order as follows:
 	 * 1. Equals
 	 * 2. Starts With
 	 * 3. Contains
 	 * @param resultantMatchedClass
 	 * @return MatchedClass Sorted Matched Class
 	 */
 	private MatchedClass sortResults(MatchedClass resultantMatchedClass) {
 		List<MatchedClassEntry> entityMatches = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> entityDescMatches = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> entitySemPropMatches = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> attributeMatches = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> attrDescMatches = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> attrSemPropMatches = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> pvMatches = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> pvSemPropMatches = new ArrayList<MatchedClassEntry>();
 
 		Map<MatchCause,List<MatchedClassEntry>> causeVsListMap = new HashMap<MatchCause, List<MatchedClassEntry>>();
 		causeVsListMap.put(MatchCause.EntityName, entityMatches);
 		causeVsListMap.put(MatchCause.EntityDescription, entityDescMatches);
 		causeVsListMap.put(MatchCause.EntitySemanticProperty, entitySemPropMatches);
 		causeVsListMap.put(MatchCause.AttributeName, attributeMatches);
 		causeVsListMap.put(MatchCause.AttributeDescription, attrDescMatches);
 		causeVsListMap.put(MatchCause.AttributeSemanticProperty, attrSemPropMatches);
 		causeVsListMap.put(MatchCause.PermissibleValueName, pvMatches);
 		causeVsListMap.put(MatchCause.PermissibleSemanticProperty, pvSemPropMatches);
 
 		List<MatchedClassEntry> matchedClassEntries = resultantMatchedClass.getMatchedClassEntries();
 		for (MatchedClassEntry matchedClassEntry : matchedClassEntries) {
 			for (MatchCause cause : MatchCause.values()) {
 				boolean isAddedToList = populateLists(matchedClassEntry, cause, causeVsListMap.get(cause));
 				if(isAddedToList)
 				{
 					break;
 				}
 			}
 		}
 		List<MatchedClassEntry> allEntries = sortIndividualLists(causeVsListMap);
 		return createResultMatchedClass(allEntries);
 	}
 	/**
 	 * 
 	 * @param allEntries
 	 * @return
 	 */
 	private MatchedClass createResultMatchedClass(List<MatchedClassEntry> allEntries) {
 		MatchedClass sortedMatchedClass = new MatchedClass();
 		sortedMatchedClass.setMatchedClassEntries(allEntries);
 		Set<EntityInterface> entities = new LinkedHashSet<EntityInterface>();
 		for(MatchedClassEntry entry : allEntries) {
 			entities.add(entry.getMatchedEntity());
 		}
 		sortedMatchedClass.setEntityCollection(entities);
 		return sortedMatchedClass;
 	}
 	/**
 	 * 
 	 * @param entityMatches
 	 * @param causeVsListMap
 	 * @return
 	 */
 	private List<MatchedClassEntry> sortIndividualLists(
 			Map<MatchCause, List<MatchedClassEntry>> causeVsListMap) {
 		List<MatchedClassEntry> entityMatchesPosition0 = new ArrayList<MatchedClassEntry>();
 		List<MatchedClassEntry> entityMatchesRest = new ArrayList<MatchedClassEntry>();
 
 		AlphabeticalComparator comparator= new AlphabeticalComparator();
 		List<MatchedClassEntry> allEntries = new  ArrayList<MatchedClassEntry>();
 		
 		divideEntityMatchesList(entityMatchesRest,entityMatchesPosition0,causeVsListMap.get(MatchCause.EntityName));
 		Collections.sort(entityMatchesPosition0,comparator);
 		allEntries.addAll(entityMatchesPosition0);
 		Collections.sort(entityMatchesRest,comparator);
 		allEntries.addAll(entityMatchesRest);
 		
 		for (MatchCause cause : MatchCause.values()) {
 			if(!cause.equals(MatchCause.EntityName))
 			{
 				List<MatchedClassEntry> list = causeVsListMap.get(cause);
 				Collections.sort(list,comparator);
 				allEntries.addAll(list);
 			}
 		}
 		return allEntries;
 	}
 	/**
 	 * Individual lists are populated depending upon their MatchCause and position.
 	 * @param entry
 	 * @param cause
 	 * @param list
 	 * @return
 	 */
 	private boolean populateLists(MatchedClassEntry entry, MatchCause cause, List<MatchedClassEntry> list)
 	{
 		Integer positionOf = entry.positionOf(cause);
 		if(positionOf != null)
 		{
 			list.add(entry);
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * List of Entity names is divided intom two lists.
 	 * One containing only entities which StartsWith the searchText 
 	 * Other containing rest of the entities, other than index 0
 	 * @param entityMatchesRest
 	 * @param entityMatchesPosition0
 	 * @param entityMatches
 	 */
 	private void divideEntityMatchesList(List<MatchedClassEntry> entityMatchesRest,
 			List<MatchedClassEntry> entityMatchesPosition0, List<MatchedClassEntry> entityMatches) {
 		for (MatchedClassEntry matchedClassEntry : entityMatches) {
 			Integer positionOf = matchedClassEntry.positionOf(MatchCause.EntityName);
 			if(positionOf == 0)
 			{
 				entityMatchesPosition0.add(matchedClassEntry);
 			}
 			else
 			{
 				entityMatchesRest.add(matchedClassEntry);
 			}
 		}
 	}
 }
