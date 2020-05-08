 
 package edu.wustl.query.bizlogic;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.beans.QueryResultObjectDataBean;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.query.queryobject.impl.metadata.QueryOutputTreeAttributeMetadata;
 import edu.wustl.common.query.queryobject.impl.metadata.SelectedColumnsMetadata;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IOutputAttribute;
 import edu.wustl.common.querysuite.queryobject.IOutputEntity;
 import edu.wustl.common.querysuite.queryobject.IOutputTerm;
 import edu.wustl.common.querysuite.queryobject.impl.OutputAttribute;
 import edu.wustl.common.tree.QueryTreeNodeData;
 import edu.wustl.common.util.Utility;
 import edu.wustl.query.actionForm.CategorySearchForm;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.QueryCSMUtil;
 import edu.wustl.query.util.querysuite.QueryDetails;
 import edu.wustl.query.util.querysuite.QueryModuleConstants;
 import edu.wustl.query.util.querysuite.TemporalColumnUIBean;
 
 /**
  * This bizlogic is called when user wants to define columns to be shown for grid results.
  * 
  * @author deepti_shelar
  *
  */
 public class DefineGridViewBizLogic
 {
 
 	/**
 	 * This list denotes the selected columns in a session.
 	 */
 	List<NameValueBean> prevSelectedColumnList;
 
 	/**
 	 * returns class name when passed a OutputTreeDataNode
 	 * @param node OutputTreeDataNode
 	 * @return String className
 	 */
 	public String getClassName(OutputTreeDataNode node)
 	{
 		if (node != null)
 		{
 			IOutputEntity outputEntity = node.getOutputEntity();
 			EntityInterface dynamicExtensionsEntity = outputEntity.getDynamicExtensionsEntity();
 			String className = dynamicExtensionsEntity.getName();
 			className = Utility.parseClassName(className);
 			return className;
 		}
 		return null;
 	}
 	
 	/**
 	 * This method returns main Entities 
 	 * @param queryDetailsObj
 	 * @param mainEntityList
 	 * @param rootEntitiesList
 	 * @return
 	 */
 	private List<String>getMainEntityIdList(QueryDetails queryDetailsObj,List<Integer> mainEntityList, List<Integer> rootEntitiesList)
 	{
 		List<String> mainEntityIdList = new ArrayList<String>(); 
 		Set<String>keySet = queryDetailsObj.getUniqueIdNodesMap().keySet();
 		//IOutputEntity outputEntity;
 		Iterator <Integer>itr = mainEntityList.iterator();
 		while(itr.hasNext())
 		{
 			int mainEntityExpId = itr.next().intValue();
 			Iterator <String> iterator = keySet.iterator();
 			Object keyId;
 			while (iterator.hasNext())
 			{
 				keyId = iterator.next();
 				String nodeId = keyId.toString();
 				OutputTreeDataNode node = queryDetailsObj.getUniqueIdNodesMap().get(nodeId);
 				int nodeExpId =node.getExpressionId();
 				//outputEntity = node.getOutputEntity();
 				//EntityInterface dynamicExtensionsEntity = outputEntity.getDynamicExtensionsEntity();
 				//Long entityId = dynamicExtensionsEntity.getId();
                 if(mainEntityExpId == nodeExpId  && (!isRootEntityOfQuery(nodeExpId,rootEntitiesList)) && (!mainEntityIdList.contains(nodeId)))
     			{
     				mainEntityIdList.add(nodeId);
     				break;
     			}
 			}
 		}
 		return mainEntityIdList;
 	}
 	
 	private boolean isRootEntityOfQuery(int entityExpId,List<Integer> rootEntitiesList)
 	{
 		boolean isRootEntity = false;
 		Iterator<Integer> itr = rootEntitiesList.iterator();
 		while(itr.hasNext())
 		{
 			int rootEntityExpId =itr.next().intValue();
 			if(entityExpId == rootEntityExpId)
 			{
 				isRootEntity = true;
 				break;
 			}
 		}
 		return isRootEntity;
 	}
 
 	/*private String getEntityTd(QueryDetails queryDetailsObj)
 	{
 		OutputTreeDataNode currentSelectedObject = queryDetailsObj.getCurrentSelectedObject();
 		IOutputEntity outputEntity = currentSelectedObject.getOutputEntity();
 		EntityInterface rootEntity = outputEntity.getDynamicExtensionsEntity();
         String rootEntityId = rootEntity.getId().toString();
 		return rootEntityId;
 	}*/
 
 	/**
 	 * Creates tree structure data required for defining grid view.
 	 * @param categorySearchForm action form
 	 * @param uniqueIdNodesMap map of nodes present in query
 	 * @param treeDataVector vector to store tree data
 	 * @param currentSelectedObject
 	 * @param prevSelectedColumnNameValueBeanList 
 	 */
 	public void createTree(CategorySearchForm categorySearchForm, QueryDetails queryDetailsObj,List<QueryTreeNodeData> treeDataVector, OutputTreeDataNode currentSelectedObject,List<NameValueBean> prevSelectedColumnList)
 	{
       this.prevSelectedColumnList = prevSelectedColumnList;
       if (!queryDetailsObj.getUniqueIdNodesMap().isEmpty())
       {
            addRootNode(treeDataVector);
            Set keySet = queryDetailsObj.getUniqueIdNodesMap().keySet();
            Iterator iterator = keySet.iterator();
            Object id;
            OutputTreeDataNode node;
            while (iterator.hasNext())
            {
         	   id = iterator.next();
                node = null;
                if (id instanceof String)
                {
                    String nodeId = id.toString();
                    node = queryDetailsObj.getUniqueIdNodesMap().get(nodeId);
                    addClassAndAttributeNodes(treeDataVector, node, categorySearchForm,
                    currentSelectedObject);
                 }
             }
       	}
 	}
 
 
 private void addClassAndAttributeNodes(List<QueryTreeNodeData> treeDataVector,
             OutputTreeDataNode node, CategorySearchForm categorySearchForm,
             OutputTreeDataNode currentSelectedObject)
 {
       long selectedObjectId = currentSelectedObject.getId();
       long id = node.getId();
       String uniqueNodeId = node.getUniqueNodeId();
       IOutputEntity outputEntity = node.getOutputEntity();
       EntityInterface dynamicExtensionsEntity = outputEntity.getDynamicExtensionsEntity();
       String className = dynamicExtensionsEntity.getName();
       className = Utility.parseClassName(className);
       String classDisplayName = Utility.getDisplayLabel(className);
       String treeClassNodeId = Constants.CLASS + Constants.UNDERSCORE + uniqueNodeId
                   + Constants.UNDERSCORE + className;
       QueryTreeNodeData classTreeNode = new QueryTreeNodeData();
       classTreeNode.setIdentifier(treeClassNodeId);
       classTreeNode.setObjectName(className);
       classTreeNode.setDisplayName(classDisplayName);
       classTreeNode.setParentIdentifier(Constants.ROOT);
       classTreeNode.setParentObjectName("");
       treeDataVector.add(classTreeNode);
       //boolean isSelectedObject = false;
       if (selectedObjectId == id)
       {
             //isSelectedObject = true;
             if (prevSelectedColumnList == null)
             {
                   categorySearchForm.setCurrentSelectedNodeInTree(treeClassNodeId);
             }
       }
        addAttributeNodes(treeDataVector, className, treeClassNodeId, categorySearchForm, node
                   .getAttributes());
 }
 
 /**
  * Adds attribute nodes to tree.
  * @param treeDataVector vector to store tree data
  * @param className String class name
  * @param treeClassNodeId parentid to add to tree
  * @param categorySearchForm action form 
  * @param attributeMetadataList  Metadata to be added
  */
 private void addAttributeNodes(List<QueryTreeNodeData> treeDataVector, String className,
             String treeClassNodeId, CategorySearchForm categorySearchForm,
             List<QueryOutputTreeAttributeMetadata> attributeMetadataList)
 {
       //List<QueryOutputTreeAttributeMetadata> attributeMetadataList = node.getAttributes();
       
       AttributeInterface attribute;
       String attributeName;
       String attributeDisplayName;
       String treeAttributeNodeId;
       QueryTreeNodeData attributeTreeNode;
       List<NameValueBean> defaultSelectedColumnList = categorySearchForm.getSelectedColumnNameValueBeanList(); 
     if(defaultSelectedColumnList==null)
     {
     	defaultSelectedColumnList = new ArrayList<NameValueBean>();      
     }
       
       for (QueryOutputTreeAttributeMetadata attributeMetadata : attributeMetadataList)
       {
             attribute = attributeMetadata.getAttribute();
             boolean isNotViewable = edu.wustl.query.util.global.Utility.isNotViewable(attribute);
 			if(isNotViewable)
 			{
 				continue;
 			}
             attributeName = attribute.getName();
             attributeDisplayName = Utility.getDisplayLabel(attributeName);
             //attributeWithClassName = attributeMetadata.getDisplayName();
             treeAttributeNodeId = attributeMetadata.getUniqueId();
             attributeTreeNode = new QueryTreeNodeData();
             attributeTreeNode.setIdentifier(treeAttributeNodeId);
             attributeTreeNode.setObjectName(attributeName);
             attributeTreeNode.setDisplayName(attributeDisplayName);
             attributeTreeNode.setParentIdentifier(treeClassNodeId);
             attributeTreeNode.setParentObjectName(className);
             treeDataVector.add(attributeTreeNode);
              /*(if("".equals(attributeName))
             { 
                 nameValueBean = new NameValueBean();
                         nameValueBean.setName(attributeWithClassName);
                         nameValueBean.setValue(treeAttributeNodeId);
                         defaultSelectedColumnNameValueBeanList.add(nameValueBean);
             }*/
       }
       
       if (prevSelectedColumnList != null)
       {
             categorySearchForm
               .setSelectedColumnNameValueBeanList(prevSelectedColumnList);
       }
       else 
       {
          categorySearchForm.setSelectedColumnNameValueBeanList(defaultSelectedColumnList);
       }
 }
 
 	
 	/**
 	 * This method creates XML String for containment tree
 	 * @param categorySearchForm
 	 * @param queryDetailsObj
 	 * @param prevSelectedColumnNVBList
 	 * @param xmlString
 	 * @return The method returns the XML string to create a containment tree 
 	 */
 	public StringBuilder createContainmentTree(QueryDetails queryDetailsObj,StringBuilder xmlString)
 	{
 		//this.prevSelectedColumnNameValueBeanList = prevSelectedColumnNVBList;
 		List<Integer> mainEntityExpIdsList = queryDetailsObj.getMainEntityList();
 		if (!queryDetailsObj.getUniqueIdNodesMap().isEmpty())
 		{
 			//Adding root node of tree
 			addRootNode(xmlString);
 			
 			//This List will maintain list of all root entities in Query 
 			List <Integer>rootNodesExpIdsList = new ArrayList<Integer>(); 
 			List<OutputTreeDataNode> rootOutputTreeNodeList = queryDetailsObj.getRootOutputTreeNodeList();
 			Iterator <OutputTreeDataNode> rootNodeItr = rootOutputTreeNodeList.iterator(); 
 			while(rootNodeItr.hasNext())
 			{
 				OutputTreeDataNode currentSelectedObject = rootNodeItr.next();
 				queryDetailsObj.setCurrentSelectedObject(currentSelectedObject);
 				rootNodesExpIdsList.add(Integer.valueOf(currentSelectedObject.getExpressionId()));
 			    
 			    //Add each root node tree and it's corresponding Containment Entities
 				addIQueryRootNodeToTree(queryDetailsObj, xmlString);
 				xmlString.append("</item>");
 			}
 			//Get List of all main Entity Id's, except IQuery root nodes 
 			List<String> mainEntityIdList = getMainEntityIdList(queryDetailsObj,mainEntityExpIdsList,rootNodesExpIdsList); 
 			
 			//After IQuery tree node, Add Main Entities Nodes and their attributes to Tree, avoiding the root node of the Query
 			addAllMainEntitiesToTree(queryDetailsObj,mainEntityIdList,xmlString);
 		}
 		
 		return xmlString;
 	}
 
 	/**
 	 * This method add all main entities to containment tree except root node
 	 * @param categorySearchForm
 	 * @param queryDetailsObj
 	 * @param mainEntityIdList
 	 * @param xmlString
 	 */
 	private void addAllMainEntitiesToTree(QueryDetails queryDetailsObj,List<String> mainEntityIdList, StringBuilder xmlString)
 	{
 		OutputTreeDataNode currentSelectedObject;
 		for(int i=0; i<mainEntityIdList.size(); i++)
 		{
 			String nodeId = mainEntityIdList.get(i);
 			currentSelectedObject = queryDetailsObj.getUniqueIdNodesMap().get(nodeId);
 			queryDetailsObj.setCurrentSelectedObject(currentSelectedObject);
 			addEntityAndAttributes(queryDetailsObj,true,xmlString);
 			
 			//For each main Entity add all Containment
 			addAllContainmentsOfMainExpression(queryDetailsObj, xmlString); 
 			xmlString.append("</item>");
 		}
 	}
 
 	/**
 	 * This method adds the root node of IQuery to containment tree
 	 * @param categorySearchForm
 	 * @param queryDetailsObj
 	 * @param xmlString
 	 */
 	private void addIQueryRootNodeToTree(QueryDetails queryDetailsObj, StringBuilder xmlString)
 	{
 		Map<Integer,String> parentNodesIdMap = new HashMap<Integer,String>(); 
 		queryDetailsObj.setParentNodesIdMap(parentNodesIdMap);
 		addEntityAndAttributes(queryDetailsObj,true, xmlString);
 		
 		//Add all the containments of the Main Entity
 		addAllContainmentsOfMainExpression(queryDetailsObj, xmlString); 
 	}
 
 	private void addRootNode(List<QueryTreeNodeData> treeDataVector)
 	{
 		QueryTreeNodeData classTreeNode = new QueryTreeNodeData();
 		String rootNodeId = Constants.ROOT;
 		classTreeNode.setIdentifier(rootNodeId);
 		classTreeNode.setObjectName(rootNodeId);
 		String rootDiplayName = Constants.CLASSES_PRESENT_IN_QUERY;
 		classTreeNode.setDisplayName(rootDiplayName);
 		classTreeNode.setParentIdentifier(Constants.ZERO_ID);
 		classTreeNode.setParentObjectName("");
 		treeDataVector.add(classTreeNode);
 	}
 	
 	
 	/**
 	 * Adds root node to the tree, its a just a label node saying classes present in query.
 	 * @param treeDataVector  vector to store tree data
 	 */
 	private void addRootNode(StringBuilder xmlString)
 	{
 		//Creating the XML string
 		xmlString.append("<tree id=\"0\">");
 		xmlString.append("<item id = \""+ Constants.ROOT+ "\" text = \""+ Constants.CLASSES_PRESENT_IN_QUERY + "\"  style=\"font-family:Arial, Helvetica, sans-serif;font-size: 12px;color: #3c3c3c;\" name= \""+ Constants.ROOT +"\" parent= \"" + Constants.ZERO_ID + "\" parentObjName = "+ "\"\"" +">");
 	}
 
 	/**
 	 * This method adds class node to the tree.And calls addAttributeNodes method to add attribute nodes to tree.
 	 * @param treeDataVector  vector to store tree data
 	 * @param node OutputTreeDataNode node to be added in tree
 	 * @param categorySearchForm action form
 	 * @param currentSelectedObject  OutputTreeDataNode
 	 */
 	private void addEntityAndAttributes(QueryDetails queryDetailsObj,boolean isMainEntity,StringBuilder xmlString)
 	{
         //parentNodesIdMap will store expression id of main entity and it's treeClassNodeId 		
 		Map<Integer,String> parentNodesIdMap = queryDetailsObj.getParentNodesIdMap();
 		OutputTreeDataNode currentSelectedObject = queryDetailsObj.getCurrentSelectedObject();
 		String uniqueNodeId = currentSelectedObject.getUniqueNodeId();
 		IOutputEntity outputEntity = currentSelectedObject.getOutputEntity();
 	    
 		Map <Integer,List<Integer>> mainEntityContainmentIdsMap = queryDetailsObj.getMainExpEntityExpressionIdMap();
 		int expressionId = currentSelectedObject.getExpressionId();
 		
 		EntityInterface dynamicExtensionsEntity = outputEntity.getDynamicExtensionsEntity();
 		String className = dynamicExtensionsEntity.getName();
 		className = Utility.parseClassName(className);
 		String classDisplayName = Utility.getDisplayLabel(className);
 		String treeClassNodeId = Constants.CLASS + Constants.UNDERSCORE + uniqueNodeId+ Constants.UNDERSCORE + className;
 		String parentId ="";
 		if(isMainEntity == true)
 		{
 			//If it's a Main Entity , it's always going to be under main Entity
 			parentId = Constants.ZERO_ID;
 			parentNodesIdMap.put(Integer.valueOf(expressionId),treeClassNodeId);
 		}
 		else
 		{
 			//If its a containment Entity
 			parentId = getParentForChildEntity(parentNodesIdMap,expressionId,mainEntityContainmentIdsMap);
 		}
 		xmlString.append("<item id = \""+ treeClassNodeId + "\" text = \""+ classDisplayName + "\" style=\"font-family:Arial, Helvetica, sans-serif;font-size: 12px;color: #3c3c3c;\" imheight=\"18\" imwidth=\"18\" name= \""+ className +"\" parent= \"" + parentId + "\" parentObjName = "+ "\"\"" +">");	
 		addAttributeToClassEntity(className, treeClassNodeId,currentSelectedObject.getAttributes(),xmlString);
 		if(!isMainEntity)
 		{
 			xmlString.append("</item>");
 		}
 	}
 	
 	/**
 	 * This method adds all containments of a main entity to containment tree
 	 * @param queryDetailsObj
 	 * @param categorySearchForm
 	 * @param xmlString
 	 */
 	private void addAllContainmentsOfMainExpression(QueryDetails queryDetailsObj,StringBuilder xmlString)
 	{
 		OutputTreeDataNode currentSelectedObject = queryDetailsObj.getCurrentSelectedObject();
 		
 		int mainExpId = currentSelectedObject.getExpressionId();
 		
 	    //From containmentMap get all the containment of the main Entity
 		List <String> containmentEntitiesIds = getContainmentIdsList(queryDetailsObj,mainExpId);
 
 		//Here u get the list of Unique ID's of all Containments Entities
 		Iterator<String> itr = containmentEntitiesIds.iterator();
 		String uniqueKeyId = "";
 		while(itr.hasNext())
 		{
 			uniqueKeyId = itr.next();
 			currentSelectedObject = queryDetailsObj.getUniqueIdNodesMap().get(uniqueKeyId);	
			queryDetailsObj.setCurrentSelectedObject(currentSelectedObject);
			addEntityAndAttributes(queryDetailsObj,false,xmlString);
 		}
 	}
 
 	private List <String> getContainmentIdsList(QueryDetails queryDetailsObj,int mainExpId)
 	{
 		OutputTreeDataNode currentSelectedObject;
 		List <String> containmentEntitiesIds = new ArrayList<String>();
 		Map <Integer,List<Integer>> mainEntityContainmentIdsMap = queryDetailsObj.getMainExpEntityExpressionIdMap();
 		List<Integer> expressionIdsList = mainEntityContainmentIdsMap.get(Integer.valueOf(mainExpId));
 		Iterator<Integer> expsIdsItr = expressionIdsList.iterator();
 		Set<String>keySet = queryDetailsObj.getUniqueIdNodesMap().keySet();
 		while(expsIdsItr.hasNext())
 		{
 			Integer expId = expsIdsItr.next();
 			String uniqueKeyId = "";
 			Iterator <String> keyItr = keySet.iterator();
 			while(keyItr.hasNext())
 			{
 				uniqueKeyId = keyItr.next();
 				currentSelectedObject = queryDetailsObj.getUniqueIdNodesMap().get(uniqueKeyId);	
 				int entityEpxId = currentSelectedObject.getExpressionId();
 				if((entityEpxId == expId.intValue()) && (expId.intValue() != mainExpId))
 				{
 					containmentEntitiesIds.add(uniqueKeyId);
 					break;
 				}
 			}
 		}
 		return containmentEntitiesIds;
 	}
 	
 	private void addAttributeToClassEntity (String className,
             String treeClassNodeId, 
             List<QueryOutputTreeAttributeMetadata> attributeMetadataList,StringBuilder xmlString)
 {
       AttributeInterface attribute;
       String attributeName;
       String attributeDisplayName;
       String treeAttributeNodeId;
       for (QueryOutputTreeAttributeMetadata attributeMetadata : attributeMetadataList)
       {
             attribute = attributeMetadata.getAttribute();
             boolean isNotViewable = edu.wustl.query.util.global.Utility.isNotViewable(attribute);
             boolean isVIHidden = edu.wustl.query.util.global.Utility.istagPresent(attribute,Constants.TAGGED_VALUE_VI_HIDDEN);
 			if(isNotViewable || isVIHidden)
 			{
 				continue;
 			}
             attributeName = attribute.getName();
             attributeDisplayName = Utility.getDisplayLabel(attributeName);
             treeAttributeNodeId = attributeMetadata.getUniqueId();
             xmlString.append("<item id = \""+ treeAttributeNodeId + "\" text = \""+ attributeDisplayName + "\" style=\"font-family:Arial, Helvetica, sans-serif;font-size: 12px;color: #3c3c3c;\" imheight=\"18\" imwidtht=\"18\"  name= \""+ attributeName +"\" parent= \"" + treeClassNodeId + "\" parentObjName = \""+ className + "\"" +">");
             xmlString.append("</item>");
       }
  }
 
 	private String getParentForChildEntity(Map<Integer, String> parentNodesIdMap,int entityExpId,Map <Integer,List<Integer>> mainExpEntityExpressionIdMap)
 	{
 		//Else it will be one of the containment of the main entity, so it's parent should be main entity
 		Integer mainEntityExpId = getMainEntityIdForChildEntity(entityExpId, mainExpEntityExpressionIdMap);
 		String parentIdString = "";
 		if(mainEntityExpId != null)
 		{
 			parentIdString = parentNodesIdMap.get(mainEntityExpId);
 		}
 		return parentIdString;
 	}
 
 	private Integer getMainEntityIdForChildEntity(int entityExpId,Map <Integer,List<Integer>> mainExpEntityExpressionIdMap)
 	{
 		Integer mainExpressionId = null;
 		Set<Integer> mainExpskeySet = mainExpEntityExpressionIdMap.keySet();
 		Iterator< Integer> keySetItr = mainExpskeySet.iterator();
 		while(keySetItr.hasNext())
 		{
 			Integer mainExpId = keySetItr.next();
 			List <Integer> expIdsList = mainExpEntityExpressionIdMap.get(mainExpId);
 			Iterator<Integer> expIdsItr = expIdsList.iterator();
 			while(expIdsItr.hasNext())
 			{
 				Integer expId = expIdsItr.next();
 				if(entityExpId == expId.intValue())
 				{
 					mainExpressionId = mainExpId;
 				    break;	
 				}
 			}
 		}
 		return mainExpressionId;
 	}
 
 	
 	/**
 	 * returns list of seletced columns
 	 * @param selectedColumnIds
 	 * @param constraints 
 	 * @param uniqueIdNodesMap map of id and Node
 	 * @param constraints 
 	 * @return SelectedColumnsMetadata SelectedColumnsMetadata
 	 */
 	public void getSelectedColumnsMetadata(CategorySearchForm categorySearchForm,
 			QueryDetails queryDetailsObj, SelectedColumnsMetadata selectedColumnsMetadata)
 	{
 		String[] selectedColumnIds = categorySearchForm.getSelectedColumnNames();
 		List<QueryOutputTreeAttributeMetadata> attribureMetadataList = new ArrayList<QueryOutputTreeAttributeMetadata>();
 		List<IOutputAttribute> outputAttributeList = new ArrayList<IOutputAttribute>();
 		List<NameValueBean> selectedColumnNameValue = new ArrayList<NameValueBean>();
 		IOutputAttribute attr;
 		String columnId;
 		//String[] split;
 		String expressionId;
 		OutputTreeDataNode outputTreeDataNode;
 		int lenOfCIds = selectedColumnIds.length;
 		for (int i = 0; i < lenOfCIds; i++)
 		{
 			attr = null;
 			columnId = selectedColumnIds[i];
 			String[] split = columnId.split(Constants.EXPRESSION_ID_SEPARATOR);
 			expressionId = split[QueryModuleConstants.INDEX_PARAM_ZERO];
 			outputTreeDataNode = getMatchingOutputTreeDataNode(queryDetailsObj, expressionId);
 			if (outputTreeDataNode != null)
 			{
 				List<QueryOutputTreeAttributeMetadata> attributes = outputTreeDataNode
 						.getAttributes();
 				NameValueBean nameValueBean;
 				for (QueryOutputTreeAttributeMetadata attributeMetaData : attributes)
 				{
 					if (columnId.equals(attributeMetaData.getUniqueId()))
 					{
 						if(attributeMetaData.getAttribute().getName().equals(Constants.NAME) 
 								&& attributeMetaData.getAttribute().getEntity().getName().equals(Constants.MED_ENTITY_NAME))
 						{
 							for(QueryOutputTreeAttributeMetadata idAttr:attributes)
 							{
 								if(idAttr.getAttribute().getName().equals(Constants.ID))
 								{
 									attribureMetadataList.add(idAttr);
 									attr = new OutputAttribute(queryDetailsObj.getQuery().getConstraints().getExpression(outputTreeDataNode
 											.getExpressionId()), idAttr.getAttribute());
 									outputAttributeList.add(attr);
 									nameValueBean = new NameValueBean(idAttr.getDisplayName(),
 											idAttr.getUniqueId());
 									selectedColumnNameValue.add(nameValueBean);
 								}
 							}
 						}
 						attribureMetadataList.add(attributeMetaData);
 						attr = new OutputAttribute(queryDetailsObj.getQuery().getConstraints().getExpression(outputTreeDataNode
 								.getExpressionId()), attributeMetaData.getAttribute());
 						outputAttributeList.add(attr);
 						nameValueBean = new NameValueBean(attributeMetaData.getDisplayName(),
 								attributeMetaData.getUniqueId());
 						selectedColumnNameValue.add(nameValueBean);
 						break;
 					}
 				}
 			}
 		}
 		selectedColumnsMetadata.setSelectedAttributeMetaDataList(attribureMetadataList);
 		selectedColumnsMetadata.setSelectedOutputAttributeList(outputAttributeList);
 		selectedColumnsMetadata.setSelectedColumnNameValueBeanList(selectedColumnNameValue);
 	}
 
 	/**
 	 * Returns the matching OutputTreeDataNode, compares expression id.
 	 * @param uniqueIdNodesMap map of all nodes in query
 	 * @param expressionId string id
 	 * @return OutputTreeDataNode node
 	 */
 	private OutputTreeDataNode getMatchingOutputTreeDataNode(QueryDetails queryDetailsObj,
 			String expressionId)
 	{
 		OutputTreeDataNode outputTreeDataNode = null;
 		Set<String> keySet = queryDetailsObj.getUniqueIdNodesMap().keySet();
 		Iterator<String> iterator = keySet.iterator();
 		Object next;
 		while (iterator.hasNext())
 		{
 			next = iterator.next();
 			if (next instanceof String)
 			{
 				outputTreeDataNode = queryDetailsObj.getUniqueIdNodesMap().get(next);
 				if (Integer.valueOf(outputTreeDataNode.getExpressionId()).toString().equals(
 						expressionId))
 				{
 					break;
 				}
 			}
 		}
 		return outputTreeDataNode;
 	}
 
 	/**
 	 * 
 	 * @param categorySearchForm
 	 * @param selectedAttributeMetaDataList
 	 * @param selectedColumnNames
 	 * @param queryResultObjecctDataMap
 	 * @param nodeData 
 	 * @param mainEntityMap
 	 * @param uniqueIdNodesMap
 	 * @return
 	 */
 	public List<String> getSelectedColumnList(CategorySearchForm categorySearchForm,
 			List<QueryOutputTreeAttributeMetadata> selectedAttributeMetaDataList,
 			StringBuffer selectedColumnNames,
 			Map<Long, QueryResultObjectDataBean> queryResultObjecctDataMap,
 			QueryDetails queryDetailsObj,
 			String nodeData)
 	{
 		queryResultObjecctDataMap.clear();
 		Map<String, IOutputTerm> outputTermsColumns = queryDetailsObj.getOutputTermsColumns();
 		List<String> definedColumnsList = new ArrayList<String>();
 		List<NameValueBean> selectedColumnNameValue = new ArrayList<NameValueBean>();
 		//		List<QueryOutputTreeAttributeMetadata> selectedAttributeMetaDataList = selectedColumnsMetadata
 		//			.getSelectedAttributeMetaDataList();
 		QueryResultObjectDataBean queryResulObjectDataBean = null;
 		//Vector<Integer> identifiedColumnIds = null;
 		//Vector<Integer> objectColumnIds = null;
 		List<EntityInterface> defineViewNodeList = new ArrayList<EntityInterface>();
 		int columnIndex = 0;
 		int totalFileTypeAttributes = 0;
 		Iterator<QueryOutputTreeAttributeMetadata> iterator = selectedAttributeMetaDataList
 				.iterator();
 		QueryOutputTreeAttributeMetadata element;
 		while (iterator.hasNext())
 		{
 			element = iterator.next();
 			queryResulObjectDataBean = queryResultObjecctDataMap.get(element.getTreeDataNode()
 					.getId());
 			if (queryResulObjectDataBean == null)
 			{
 				//identifiedColumnIds = new Vector<Integer>();
 				//objectColumnIds = new Vector<Integer>();
 				queryResulObjectDataBean = QueryCSMUtil.getQueryResulObjectDataBean(element
 						.getTreeDataNode(), queryDetailsObj);
 				queryResultObjecctDataMap.put(element.getTreeDataNode().getId(),
 						queryResulObjectDataBean);
 				defineViewNodeList.add(element.getTreeDataNode().getOutputEntity()
 						.getDynamicExtensionsEntity());
 			}
 
 			if (element.getAttribute().getIsIdentified() != null
 					&& element.getAttribute().getIsIdentified())
 			{
 				queryResulObjectDataBean.getIdentifiedDataColumnIds().add(columnIndex);
 			}
 			if (Constants.ID.equals(element.getAttribute().getName()))
 			{
 				if (queryResulObjectDataBean.isMainEntity())
 				{
 					queryResulObjectDataBean.setMainEntityIdentifierColumnId(columnIndex);
 				}
 				else
 				{
 					queryResulObjectDataBean.setEntityId(columnIndex);
 				}
 			}
 			if (Constants.FILE.equals(element.getAttribute().getDataType()))
 			{
 				queryResulObjectDataBean.setClobeType(true);
 				Map beanMetadataMap = queryResulObjectDataBean
 						.getFileTypeAtrributeIndexMetadataMap();
 				int fileTypeIndex = columnIndex + totalFileTypeAttributes;
 				beanMetadataMap.put(fileTypeIndex, element);
 				queryResulObjectDataBean.setFileTypeAtrributeIndexMetadataMap(beanMetadataMap);
 				totalFileTypeAttributes++;
 			}
 			else
 			{
 				queryResulObjectDataBean.getObjectColumnIds().add(columnIndex);
 				selectedColumnNames.append(element.getColumnName());
 				selectedColumnNames.append(Constants.DELIMETER);
 				definedColumnsList.add(element.getDisplayName());
 				columnIndex++;
 			}
 			NameValueBean nameValueBean = new NameValueBean(element.getDisplayName(), element
 					.getUniqueId());
 			selectedColumnNameValue.add(nameValueBean);
 		}
 		Iterator<Long> mapItr = queryResultObjecctDataMap.keySet().iterator();
 		String sql = "";
 		if (selectedColumnNames.lastIndexOf(Constants.DELIMETER) != -1)
 		{
 			sql = selectedColumnNames.substring(0, selectedColumnNames
 					.lastIndexOf(Constants.DELIMETER));
 		} 
 		if (outputTermsColumns!=null && !outputTermsColumns.isEmpty())
 		{
 			QueryOutputSpreadsheetBizLogic gridBizLogic = new QueryOutputSpreadsheetBizLogic();
 			IConstraints constraints = queryDetailsObj.getQuery().getConstraints();
 			TemporalColumnUIBean temporalColumnUIBean = new TemporalColumnUIBean(null, sql,
 					definedColumnsList, outputTermsColumns, columnIndex, constraints);
 			gridBizLogic.modifySqlForTemporalColumns(temporalColumnUIBean, queryDetailsObj,
 					nodeData);
 			sql = temporalColumnUIBean.getSql();
 			columnIndex = temporalColumnUIBean.getColumnIndex();
 		}
 		selectedColumnNames.replace(0, selectedColumnNames.length(), sql);
 		while (mapItr.hasNext())
 		{
 			queryResulObjectDataBean = queryResultObjecctDataMap.get(mapItr.next());
 			if (queryResulObjectDataBean.getMainEntityIdentifierColumnId() == -1)
 			{
 				Map<EntityInterface, Integer> entityIdIndexMap = new HashMap<EntityInterface, Integer>();
 				sql = QueryCSMUtil.updateEntityIdIndexMap(queryResulObjectDataBean, columnIndex,
 						sql, defineViewNodeList, entityIdIndexMap, queryDetailsObj);
 				selectedColumnNames.replace(QueryModuleConstants.ARGUMENT_ZERO, selectedColumnNames
 						.length(), sql);
 				if (queryResulObjectDataBean.isMainEntity())
 				{
 					EntityInterface entity = queryResulObjectDataBean.getEntity();
 					Integer integer = queryResulObjectDataBean.getEntityIdIndexMap().get(entity);
 					queryResulObjectDataBean.setMainEntityIdentifierColumnId(integer);
 				}
 				else
 				{
 					EntityInterface mainEntity = queryResulObjectDataBean.getMainEntity();
 					if (queryResulObjectDataBean.getEntityIdIndexMap().get(mainEntity) != null)
 					{
 						queryResulObjectDataBean
 								.setMainEntityIdentifierColumnId(queryResulObjectDataBean
 										.getEntityIdIndexMap().get(mainEntity));
 					}
 				}
 			}
 		}
 		categorySearchForm.setSelectedColumnNameValueBeanList(selectedColumnNameValue);
 		return definedColumnsList;
 	}
 
 	/**
 	 * @param selectedColumnNames buffer to store selected columns 
 	 * @param sql formed for newly defined columns
 	 * @return String sql
 	 */
 	public String createSQLForSelectedColumn(String columnsInSql, String sql)
 	{
 		//String columnsInSql = selectedColumnNames.toString();
 		int indexOfSelectDistict = sql.indexOf(Constants.SELECT_DISTINCT);
 		int selectDistictLength = Constants.SELECT_DISTINCT.length();
 		int indexOfFrom = sql.indexOf(Constants.FROM);
 		StringBuffer newSql = new StringBuffer();
 		newSql.append(sql.substring(indexOfSelectDistict, selectDistictLength));
 		newSql.append(Constants.SPACE);
 		newSql.append(columnsInSql);
 		newSql.append(sql.substring(indexOfFrom));
 		return newSql.toString();
 	}
 
 	/**
 	 * returns list of seletced columns
 	 * @param constraints 
 	 * @param categorySearchForm form
 	 * @param uniqueIdNodesMap map of id and Node
 	 * @return SelectedColumnsMetadata SelectedColumnsMetadata
 	 */
 	public SelectedColumnsMetadata getColumnsMetadataForSelectedNode(
 			OutputTreeDataNode outputTreeDataNode, SelectedColumnsMetadata selectedColumnsMetadata,
 			IConstraints constraints)
 	{
 		if (outputTreeDataNode != null)
 		{
 			List<IOutputAttribute> selectedOutputAttributeList = new ArrayList<IOutputAttribute>();
 			List<QueryOutputTreeAttributeMetadata> attributes = outputTreeDataNode.getAttributes();
 			selectedColumnsMetadata.setSelectedAttributeMetaDataList(attributes);
 			if (selectedColumnsMetadata.isDefinedView())
 			{
 				AttributeInterface attribute;
 				OutputAttribute attr;
 				for (QueryOutputTreeAttributeMetadata metadata : attributes)
 				{
 					attribute = metadata.getAttribute();
 					attr = new OutputAttribute(constraints.getExpression(outputTreeDataNode
 							.getExpressionId()), attribute);
 					selectedOutputAttributeList.add(attr);
 				}
 				selectedColumnsMetadata.setSelectedOutputAttributeList(selectedOutputAttributeList);
 			}
 		}
 		return selectedColumnsMetadata;
 	}
 
 	/**
 	 * 
 	 * @param values
 	 * @param selectedAttributeList
 	 * @param selectedColumnsMetadata
 	 */
 	public void getSelectedColumnMetadataForSavedQuery(Collection<OutputTreeDataNode> values,
 			List<IOutputAttribute> selectedAttributeList,
 			SelectedColumnsMetadata selectedColumnsMetadata)
 	{
 		//Collection<OutputTreeDataNode> values = uniqueIdNodesMap.values();
 		List<QueryOutputTreeAttributeMetadata> attribureMetadataList = new ArrayList<QueryOutputTreeAttributeMetadata>();
 		Set newSet = new HashSet<QueryOutputTreeAttributeMetadata>();
 		List<NameValueBean> selectedColumnNameValue = new ArrayList<NameValueBean>();
 		for (OutputTreeDataNode node : values)
 		{
 			for (IOutputAttribute outAttr : selectedAttributeList)
 			{
 				if (outAttr.getAttribute().getEntity().getId().equals(
 						node.getOutputEntity().getDynamicExtensionsEntity().getId()))
 				{
 					List<QueryOutputTreeAttributeMetadata> attributes = node.getAttributes();
 					for (QueryOutputTreeAttributeMetadata attributeMetaData : attributes)
 					{
 						if (outAttr.getAttribute().equals(attributeMetaData.getAttribute()))
 						{
 							boolean add = newSet.add(attributeMetaData);
 							NameValueBean nameValueBean;
 							if (add)
 							{
 								attribureMetadataList.add(attributeMetaData);
 								nameValueBean = new NameValueBean(attributeMetaData
 										.getDisplayName(), attributeMetaData.getUniqueId());
 								selectedColumnNameValue.add(nameValueBean);
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 		selectedColumnsMetadata.setSelectedAttributeMetaDataList(attribureMetadataList);
 		selectedColumnsMetadata.setSelectedColumnNameValueBeanList(selectedColumnNameValue);
 	}
 	
 	
 	
 
 	
 }
