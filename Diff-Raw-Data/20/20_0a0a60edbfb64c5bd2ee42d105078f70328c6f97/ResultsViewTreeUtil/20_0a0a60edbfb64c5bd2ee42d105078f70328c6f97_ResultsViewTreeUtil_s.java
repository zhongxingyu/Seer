 package edu.wustl.query.util.querysuite;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.dag.ambiguityresolver.AmbiguityObject;
 import edu.wustl.cab2b.client.ui.query.ClientQueryBuilder;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.query.IPathFinder;
 import edu.wustl.cab2b.common.queryengine.Cab2bQuery;
 import edu.wustl.common.query.impl.CommonPathFinder;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.metadata.associations.IIntraModelAssociation;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionOperand;
 import edu.wustl.common.querysuite.queryobject.IJoinGraph;
 import edu.wustl.common.querysuite.queryobject.IOutputAttribute;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.IRule;
 import edu.wustl.common.querysuite.queryobject.impl.OutputAttribute;
 import edu.wustl.common.querysuite.queryobject.impl.ParameterizedQuery;
 import edu.wustl.query.flex.dag.DAGResolveAmbiguity;
 
 
 /**
  * 
  * @author baljeet_dhindhwal
  *
  */
 
 public class ResultsViewTreeUtil
 {
 	/**
 	 * 
 	 * @param mainEntity
 	 * @return
 	 */
 	public static Map<EntityInterface, List<EntityInterface>> getAllParentChildrenMap(EntityInterface mainEntity)
 	{
 		HashMap<EntityInterface, List<EntityInterface>> partentChildEntityMap = new HashMap<EntityInterface, List<EntityInterface>>();
 		ArrayList<EntityInterface> mainEntityContainmentList = new ArrayList<EntityInterface>();
 		QueryAddContainmentsUtil.getMainEntityContainments(partentChildEntityMap, mainEntityContainmentList, mainEntity);
 		return partentChildEntityMap;
 	}
 	
 	/**
 	 * 
 	 * @param partentChildEntityMap
 	 * @param rootEntity
 	 * @return
 	 */
 	public static Map<EntityInterface, List<EntityInterface>> getTaggedEntitiesParentChildMap(Map<EntityInterface, List<EntityInterface>> partentChildEntityMap ,EntityInterface rootEntity)
 	{
 		
 		Map<EntityInterface, List<EntityInterface>> taggedEntitiesParentChildMap = new HashMap<EntityInterface, List<EntityInterface>>();
 		Set <EntityInterface> keySet =partentChildEntityMap.keySet();
 	    for(EntityInterface keyEntity : keySet)
 	    {
 	    	List <EntityInterface> taggedEntitiesListForMainEntity = new ArrayList<EntityInterface>(); 
 	    	//this is the case when the tag is also applied on the root Entity
 	    	if(edu.wustl.query.util.global.Utility.istagPresent(keyEntity,"resultview") && rootEntity.getId().longValue()== keyEntity.getId().longValue())
 	    	{
 	    		taggedEntitiesListForMainEntity.add(keyEntity);
 	    	}
 	    	List<EntityInterface> childrenEntities = partentChildEntityMap.get(keyEntity);
 	     	for(EntityInterface childEntity : childrenEntities)
 	     	{
 	     		if(checkForResultViewTag(rootEntity, childEntity))
 		    	{
 	     			taggedEntitiesListForMainEntity.add(childEntity);
 		    	}
 	     	}
 	     	taggedEntitiesParentChildMap.put(keyEntity, taggedEntitiesListForMainEntity);
 	    }
 	    return taggedEntitiesParentChildMap;
 	}
 	
 	/**
 	 * 
 	 * @param rootEntity
 	 * @param childEntity
 	 * @return
 	 */
 	private static boolean checkForResultViewTag(EntityInterface rootEntity,
 			EntityInterface childEntity)
 	{
 		boolean isTagPresent = false;
 		String tagValue = edu.wustl.query.util.global.Utility.getTagValue(childEntity,"resultview");
 		String[] split = tagValue.split(",");
 		for(String entityName : split)
 		{
 			if(entityName.equals(rootEntity.getName()))
 			{
 				isTagPresent = true;
 				break;
 			}
 		}
 		return isTagPresent;
 	}
 	
 	/**
 	 * 
 	 * @param taggedEntitiesParentChildMap
 	 * @param parentChilddrenMap
 	 * @return
 	 */
 	public static Map<EntityInterface, List<EntityInterface>> getPathsMapForTaggedEntity(Map<EntityInterface, List<EntityInterface>> taggedEntitiesParentChildMap,Map<EntityInterface, List<EntityInterface>> parentChilddrenMap)
 	{
 		Map<EntityInterface, List<EntityInterface>> eachTaggedEntityPathMap = new HashMap<EntityInterface, List<EntityInterface>>();
 		Set <EntityInterface> keySet =  taggedEntitiesParentChildMap.keySet();
 		for(EntityInterface entity : keySet)
 		{
 			List <EntityInterface> taggedEntityList = taggedEntitiesParentChildMap.get(entity);
             if(taggedEntityList != null && !taggedEntityList.isEmpty())
             {
             	for(EntityInterface taggedEntity : taggedEntityList)
             	{
             		List <EntityInterface> pathList = new ArrayList<EntityInterface>();
             		pathList.add(taggedEntity);
             		getPathListForTaggedEntity(taggedEntity,pathList,parentChilddrenMap);
             		eachTaggedEntityPathMap.put(taggedEntity, pathList);
             	}
             }
 		}
 		return eachTaggedEntityPathMap;
 	}
 	
  	/**
  	 * 
  	 * @param taggedEntity
  	 * @param pathList
  	 * @param parentChilddrenMap
  	 * @return
  	 */
 	private static List<EntityInterface> getPathListForTaggedEntity(EntityInterface taggedEntity,List <EntityInterface> pathList,Map<EntityInterface, List<EntityInterface>> parentChilddrenMap)
  	{
  		Set<EntityInterface> keySet  = parentChilddrenMap.keySet();
  		for(EntityInterface mainEntity : keySet)
  		{
  			List <EntityInterface> containmentList = parentChilddrenMap.get(mainEntity);
  			if(containmentList.contains(taggedEntity))
  			{
  				pathList.add(mainEntity);
  				getPathListForTaggedEntity(mainEntity,pathList,parentChilddrenMap);
  			}
  		}
  		return pathList;
  	}
 
 	/**
 	 * 
 	 * @param eachTaggedEntityPathMap
 	 * @param m_queryObject
 	 * @param rootEntity
 	 * @param rootExpId
 	 * @return
 	 */
 	public static Map<EntityInterface, List<Integer>> addAllTaggedEntitiesToIQuery(
 			Map<EntityInterface, List<EntityInterface>> eachTaggedEntityPathMap,
 			IClientQueryBuilderInterface m_queryObject,
 			EntityInterface rootEntity, int rootExpId)
 	{
 		Map <EntityInterface, List<Integer>> eachTaggedEntityPathExpressionsMap = new HashMap<EntityInterface, List<Integer>>(); 
 	    
  		//All Intermediate nodes as well tagged entities should be added like containment
  		Set <EntityInterface> taggedEntitiesKeySet = eachTaggedEntityPathMap.keySet();
  		for(EntityInterface taggedEntity : taggedEntitiesKeySet)
  		{
  			List<EntityInterface> pathList = eachTaggedEntityPathMap.get(taggedEntity);
  			List <Integer> pathExpressionsList =  new ArrayList<Integer>();
  			for(int i= pathList.size()-1; i >=0; i--)
  			{
  				EntityInterface pathEntity = pathList.get(i);
  				if(pathEntity.getName().equalsIgnoreCase(rootEntity.getName()))
  				{
  					pathExpressionsList.add(rootExpId);
  				}
  				else
  				{
  					updatePathExpressionsList(pathEntity,m_queryObject, pathExpressionsList);
  				}
  			}
  			eachTaggedEntityPathExpressionsMap.put(taggedEntity,pathExpressionsList);
  		}
 		return eachTaggedEntityPathExpressionsMap;
 	}
 
 	/**
 	 * This method adds the entity with rule to IQuery
 	 * @param m_queryObject
 	 * @param rootEntity
 	 * @param iRule
 	 * @return
 	 */
 	public static int addExpressionToIQuery(IClientQueryBuilderInterface m_queryObject,
 			EntityInterface rootEntity, IRule iRule) 
 	{
 		int rootExpId;
 		if(iRule != null)
 		{
 			rootExpId = m_queryObject.addExpression(iRule, rootEntity);
 		}
 		else
 		{
 			rootExpId = m_queryObject.addExpression(rootEntity);
 		}
 		return rootExpId;
 	}
 
 	
 	/**
 	 * 
 	 * @param pathEntity
 	 * @param m_queryObject
 	 * @param pathExpressionsList
 	 */
 	private static void updatePathExpressionsList(EntityInterface pathEntity ,IClientQueryBuilderInterface m_queryObject,List <Integer> pathExpressionsList)
  	{
  		IQuery query = m_queryObject.getQuery();
  		IConstraints constraints = query.getConstraints();
  		boolean isMatchFound = false ;
  		for(IExpression expression : constraints)
  		{
  			if(expression.getQueryEntity().getDynamicExtensionsEntity().getName().equals(pathEntity.getName()))
  			{
  				pathExpressionsList.add(expression.getExpressionId());
  				isMatchFound = true;
  			}
  		}
  		//If no match is found with any of the expression, only then add the expression to query
  		if(!isMatchFound)
 		{
 				//add new expression to Query and add that expression id to pathExpressionsList
 				int expressionId = ((ClientQueryBuilder) m_queryObject).addExpression(pathEntity);
 				pathExpressionsList.add(expressionId);
 		}
  	}
 	
 	/**
 	 * 
 	 * @param eachTaggedEntityPathExpressionsMap
 	 * @param m_queryObject
 	 */
  	public static void addLinksAmongExpressionsAdded(Map <EntityInterface, List<Integer>> eachTaggedEntityPathExpressionsMap,IClientQueryBuilderInterface m_queryObject )
  	{
  		IQuery query =  m_queryObject.getQuery();
  		IConstraints constraints = query.getConstraints();
  		IJoinGraph graph = constraints.getJoinGraph();
  		
  		Set <EntityInterface> taggedEntityKeySet= eachTaggedEntityPathExpressionsMap.keySet();
  		for(EntityInterface taggedEntity : taggedEntityKeySet)
  		{
  			List <Integer> pathExpressionsList = eachTaggedEntityPathExpressionsMap.get(taggedEntity);
  		    if((pathExpressionsList != null) && (!pathExpressionsList.isEmpty()))
  		    {
  		    	for(int i=0; i<pathExpressionsList.size()-1; i++)
  		    	{
                     int parentExpId = pathExpressionsList.get(i);
                     int childExpId = pathExpressionsList.get(i+1);
  		    		IExpression parentExpression = constraints.getExpression(parentExpId);
                     IExpression childExpresion = constraints.getExpression(childExpId);
                     IIntraModelAssociation association = (IIntraModelAssociation) (graph.getAssociation(
                     		parentExpression, childExpresion));
                     if(association == null)
                     {
                     	//If association is null, only then add the association
                     	IPath path = getIPath(parentExpression.getQueryEntity().getDynamicExtensionsEntity(),childExpresion.getQueryEntity().getDynamicExtensionsEntity());
                 	    if (!m_queryObject.isPathCreatesCyclicGraph(parentExpId, childExpId, path))
                 		{
                 	    	QueryAddContainmentsUtil.linkTwoNodes(parentExpId, childExpId, path, m_queryObject);
                 		}
                     }
  		    	}
  		    }
  		}
  		
  	}
 
 	/**
 	 * 
 	 * @param eachTaggedEntityPathExpressionsMap
 	 * @param m_queryObject
 	 */
  	public static void addTaggedOutputAttributesToQuery(Map <EntityInterface, List<Integer>> eachTaggedEntityPathExpressionsMap,IClientQueryBuilderInterface m_queryObject)
 	{
  		//For each tagged Entity ,get the tagged Attributes and add them to Ioutput Attribute list
  		ParameterizedQuery query = (ParameterizedQuery)m_queryObject.getQuery();
  		List <IOutputAttribute> outputAttributeList =  new ArrayList<IOutputAttribute>();
  		Cab2bQuery cab2bQuery = (Cab2bQuery)query;
 	    IConstraints constraints = query.getConstraints();
  		Set <EntityInterface> taggedEntityKeySet= eachTaggedEntityPathExpressionsMap.keySet();
  		for(EntityInterface taggedEntity : taggedEntityKeySet)
  		{
  			List<Integer> pathExpressionsIdsList = eachTaggedEntityPathExpressionsMap.get(taggedEntity);
  			int taggedEntityExpId = pathExpressionsIdsList.get((pathExpressionsIdsList.size()-1));
  			
  			Collection <AttributeInterface> attributeCollection= taggedEntity.getAllAttributes();
  			for(AttributeInterface attribute : attributeCollection)
  			{
  				if(edu.wustl.query.util.global.Utility.istagPresent(attribute,"resultview"))
  				{
  					//If the Attribute is tagged ,then Create IOutPut attribute and add to list
  					outputAttributeList.add(new OutputAttribute(constraints.getExpression(taggedEntityExpId),attribute));    					
  				}
  			}
  		}
  	   //Setting the IOutPut Attribute List to Query 
  	  cab2bQuery.setOutputAttributeList(outputAttributeList);
 	}
 
 	/**
 	 * 
 	 * @param parentEntity
 	 * @param childEntity
 	 * @return
 	 */
  	public static IPath getIPath(EntityInterface parentEntity,EntityInterface childEntity)
 	{
 		 //Now Add path among expressions added to query
 	    Map<AmbiguityObject, List<IPath>> map = null;
 		AmbiguityObject ambiguityObject = null;
 		ambiguityObject = new AmbiguityObject(parentEntity, childEntity);
 		IPathFinder pathFinder = new CommonPathFinder();
 		DAGResolveAmbiguity resolveAmbigity = new DAGResolveAmbiguity(ambiguityObject, pathFinder);
 		map = resolveAmbigity.getPathsForAllAmbiguities();
 		List<IPath> pathList = map.get(ambiguityObject);
 		return pathList.get(0);
 	}
 
 	
 	/**
 	 * 
 	 * @param generatedQuery
 	 * @param parentChildrenMap
 	 * @param mainEntityTreeDataNode
 	 * @param parentQuery
 	 * @throws MultipleRootsException
 	 */
  	public static void updateGeneratedQuery(IQuery generatedQuery,Map <OutputTreeDataNode, List<OutputTreeDataNode>>parentChildrenMap,OutputTreeDataNode mainEntityTreeDataNode,IQuery parentQuery) throws MultipleRootsException
 	{
  		IClientQueryBuilderInterface m_queryObject = new ClientQueryBuilder();
 		m_queryObject.setQuery(generatedQuery);
 		
 		//Now add children added by user to IQuery with conditions
 		IConstraints constraints = generatedQuery.getConstraints();
 		IJoinGraph graph = constraints.getJoinGraph();
 	 	IExpression rootExpression = graph.getRoot();
 	 	int rootExpressionId = rootExpression.getExpressionId();
 	 	
 	 	//This rootExpression gives the root expression of the Query
 	 	EntityInterface rootEntity = rootExpression.getQueryEntity().getDynamicExtensionsEntity();
 	 	
 	 	List<OutputTreeDataNode> childrenList = parentChildrenMap.get(mainEntityTreeDataNode);
 
 	 	//Add these direct children to IQuery with rule
         //Get the constraints from old patient Data query. This is required to get the rules added on children nodes 	 	
 	 	IConstraints parentQueryConstraints  = parentQuery.getConstraints();
 	 	Map <OutputTreeDataNode,Integer> childrenExpIdsMap =  new HashMap<OutputTreeDataNode, Integer>();
 	 	if(childrenList != null && !childrenList.isEmpty())
 		{
		 	for(OutputTreeDataNode childNode : childrenList)
 		 	{
 		 		int childExpressionId = childNode.getExpressionId();
 		 		IExpression childExpression = parentQueryConstraints.getExpression(childExpressionId);
 		 		
 		 		IRule iRule = getRuleFromExpression(childExpression);
 		 		
 		 		//Now get the Entity,from child node 
 		 		EntityInterface childEntity =  childNode.getOutputEntity().getDynamicExtensionsEntity();
 		 		
 		 		//Now add that expression to generated  IQuery
 		 		int expressionId = addExpressionAndRuleToQuery(m_queryObject, iRule, childEntity);
 		 		
 		 		//This map is used for retrieving the expression id a children that is added to iQuery
 		 		childrenExpIdsMap.put(childNode, expressionId);
 		 		
 		 		//Now add link among entities
 		 		IPath path = getIPath(rootEntity,childEntity);
 		 		if (!m_queryObject.isPathCreatesCyclicGraph(rootExpressionId, expressionId, path))
 		 		{
 		 	    	QueryAddContainmentsUtil.linkTwoNodes(rootExpressionId, expressionId, path, m_queryObject);
 		 		}
 		 	}
 			
 		 	//Now for each children, add further children
		 	for(OutputTreeDataNode childNode : childrenList)
 		 	{
 		 		int childRootExpId = childrenExpIdsMap.get(childNode);
 		 		EntityInterface childRootEntity = childNode.getOutputEntity().getDynamicExtensionsEntity();
 		 		List <OutputTreeDataNode> childrenOutPutList = parentChildrenMap.get(childNode);
 		 		if(childrenOutPutList != null  && !childrenOutPutList.isEmpty())
 		 		{  
 		 			for(OutputTreeDataNode outputNode : childrenOutPutList)
 		 			{
 		 				int expId =  outputNode.getExpressionId();
 		 				IExpression childExpression = parentQueryConstraints.getExpression(expId);
 		 				IRule iRule = getRuleFromExpression(childExpression);
 		 				EntityInterface childEntity =  outputNode.getOutputEntity().getDynamicExtensionsEntity();
 		 				int expressionId = addExpressionAndRuleToQuery(m_queryObject, iRule, childEntity);
 		 				IPath path = getIPath(childRootEntity,childEntity);
		 		 		if (!m_queryObject.isPathCreatesCyclicGraph(childRootExpId, expressionId, path))
 		 		 		{
 		 		 	    	QueryAddContainmentsUtil.linkTwoNodes(childRootExpId, expressionId, path, m_queryObject);
 		 		 		}
 		 			}
 		 		}
 		 	}
 		}	
 	 
 	}
     
 	/**
 	 * 
 	 * @param childExpression
 	 * @return
 	 */
  	public static IRule getRuleFromExpression(IExpression childExpression)
 	{
  		IRule iRule = null;
 		for (IExpressionOperand expressionOperand : childExpression)
 		{
 			if(expressionOperand instanceof IRule)
 			{
 				iRule = (IRule)expressionOperand;
 				break;
 			}
 		}
 		return iRule;
 	}
 	
 	/**
 	 * 
 	 * @param m_queryObject
 	 * @param iRule
 	 * @param childEntity
 	 * @return
 	 */
  	private static int addExpressionAndRuleToQuery(IClientQueryBuilderInterface m_queryObject,
 			IRule iRule, EntityInterface childEntity)
 	{
 		int expressionId;
 		expressionId = addExpressionToIQuery(m_queryObject, childEntity, iRule);
 		return expressionId;
 	}
  	
  	
 	/**
 	 * 
 	 * @param constraints
 	 * @param mainEntityTreeDataNode
 	 * @return
 	 */
  	public static  List<IExpression> getAllParentsHierarchy(IConstraints constraints,
 			OutputTreeDataNode mainEntityTreeDataNode)
 	{
 		int mainEntityOldExpId = mainEntityTreeDataNode.getExpressionId();
 		   IExpression oldPatientDataQueryExp = constraints.getExpression(mainEntityOldExpId);
 		   IJoinGraph oldPatientDataQueryGraph = constraints.getJoinGraph();
 		   
 		   //Get the complete parent child Hierarchy, till root is reached 
 		  
 		   //Extract this as Method
 		   List <IExpression>  parentsList =oldPatientDataQueryGraph.getParentList(oldPatientDataQueryExp);
 		   if(parentsList != null && !parentsList.isEmpty())
 		   {
 			   //Iterate through full hierarchy till root is reached
 			   for(int i=0; i< parentsList.size(); i++)
 			   {
 				   IExpression parentExp = parentsList.get(i);
 				   List <IExpression> listOfParents = oldPatientDataQueryGraph.getParentList(parentExp);
 				   if((listOfParents != null) && (!listOfParents.isEmpty()))
 				   {
 					   parentsList.addAll(listOfParents);
 				   }
 			   }
 		   }
 		return parentsList;
 	}
 	
 	/**
 	 * 
 	 * @param entityName
 	 * @param entityGroups
 	 * @return
 	 */
 	public static EntityInterface getEntityFromCache(String entityName, Collection<EntityGroupInterface> entityGroups)
 	{
 		EntityInterface entity = null;
 		Iterator<EntityGroupInterface> entityGroupsItr =   entityGroups.iterator();
        	while(entityGroupsItr.hasNext())
        	{
        		EntityGroupInterface entityGroup = entityGroupsItr.next();
        		entity =  entityGroup.getEntityByName(entityName);
        		if(entity != null)
        		{
        			break;
        		}
        	}
        	return entity;
 	}
 	
 }
