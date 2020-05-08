 package edu.wustl.query.action;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.common.bizlogic.IBizLogic;
 import edu.wustl.common.factory.AbstractBizLogicFactory;
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.common.query.factory.ViewIQueryGeneratorFactory;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.query.queryobject.util.QueryObjectProcessor;
 import edu.wustl.common.querysuite.queryobject.IOutputEntity;
 import edu.wustl.common.querysuite.queryobject.IParameterizedQuery;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.impl.ParameterizedQuery;
 import edu.wustl.common.tree.QueryTreeNodeData;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.query.queryexecutionmanager.DataQueryResultsBean;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.IQueryParseUtil;
 import edu.wustl.query.util.querysuite.QueryDetails;
import edu.wustl.query.util.querysuite.ResultsViewTreeUtil;
 import edu.wustl.query.viewmanager.AbstractViewIQueryGenerator;
 import edu.wustl.query.viewmanager.ViewType;
 
 /**
  * This class handles the tree generation of the view results action 
  * @author baljeet_dhindhwal
  */
 
 public class ViewResultsAction extends Action 
 {
 	@SuppressWarnings("unchecked")
 	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{ 
 		HttpSession session = request.getSession();
 		int queryExecutionID = 0; 
 		String dataQueryId = request.getParameter("dataQueryId");
 		String workflowId= request.getParameter(Constants.WORFLOW_ID);
 		Long iqueryId = Long.valueOf(0);
 		if(dataQueryId != null && !dataQueryId.equals(""))
 		{
 			iqueryId = Long.valueOf(dataQueryId);
 		}
 		String qid = (String) request.getParameter(Constants.EXECUTION_ID_OF_QUERY);
 		if(qid!=null)
 		{
 			queryExecutionID =Integer.parseInt(qid);
 		}
 	 	IBizLogic bizLogic = AbstractBizLogicFactory.getBizLogic(ApplicationProperties
 				.getValue("app.bizLogicFactory"), "getBizLogic",
 				Constants.QUERY_INTERFACE_BIZLOGIC_ID);
 	 	
 	 	//Retrieve the "Get Patient Data" Query depending on the iQuery ID 
 	 	final List<IParameterizedQuery> queryList = bizLogic.retrieve(ParameterizedQuery.class
 				.getName(), Constants.ID, iqueryId);
 	 	
 	 	IQuery getPatientDataQuery = null;
 	 	if (queryList != null && !queryList.isEmpty())
 	 	{
 	 		getPatientDataQuery = queryList.get(0);
 	 	}
 	 	List<OutputTreeDataNode> rootOutputTreeNodeList = (List<OutputTreeDataNode>)session.getAttribute(Constants.SAVE_TREE_NODE_LIST);
 		OutputTreeDataNode rootNode = rootOutputTreeNodeList.get(0);
 		IOutputEntity outputEntity = rootNode.getOutputEntity();
 		EntityInterface rootEntity = outputEntity.getDynamicExtensionsEntity();
 		
 		Map <OutputTreeDataNode, List<OutputTreeDataNode>>parentChildrenMap = 
 			IQueryParseUtil.getParentChildrensForaMainNode(rootNode);
 		
 		//Here populate the new query details object
 		QueryDetails queryDetails = new QueryDetails();
 		queryDetails.setCurrentSelectedObject(rootNode);
 		queryDetails.setQuery(getPatientDataQuery);
 		queryDetails.setParentChildrenMap(parentChildrenMap);
		
 		AbstractViewIQueryGenerator queryGenerator = ViewIQueryGeneratorFactory
 		.getDefaultViewIQueryGenerator();
		IQuery generatedQuery = queryGenerator.createIQueryForTreeView(queryDetails);
 		
 		//IQuery generatedQuery = ResultsViewTreeUtil.generateIQuery(rootNode,parentChildrenMap,rootEntity,getPatientDataQuery);
		AbstractQueryUIManager abstractQueryUIManager =
 			AbstractQueryUIManagerFactory.configureDefaultAbstractUIQueryManager(this.getClass(), request, generatedQuery);
 		
 		//Get Data for generated IQuery
 		DataQueryResultsBean  dataQueryResultsBean = abstractQueryUIManager.getData(queryExecutionID, ViewType.TREE_VIEW);
 		List<List<Object>> dataList = dataQueryResultsBean.getAttributeList();
 		
 		//Get the unique node Id map
 		Map<String, OutputTreeDataNode> uniqueIdNodesMap = QueryObjectProcessor.getAllChildrenNodes(rootOutputTreeNodeList);
 		
 		//Setting some session attributes
 		session.setAttribute(Constants.ID_NODES_MAP, uniqueIdNodesMap);
 		session.setAttribute(Constants.DATA_QUERY_ID,iqueryId);
 		session.setAttribute(Constants.WORKFLOW_ID, workflowId);
 		session.setAttribute(Constants.EXECUTION_ID_OF_QUERY, queryExecutionID);
 		session.setAttribute(Constants.PATIENT_QUERY_ROOT_OUT_PUT_NODE_LIST,rootOutputTreeNodeList);
 		session.setAttribute(Constants.PATIENT_DATA_QUERY,getPatientDataQuery);
 
 		
 		String labelNodeId = getUniqueNodeID(rootNode, uniqueIdNodesMap);
 		Vector<QueryTreeNodeData> treeDataVector = new Vector<QueryTreeNodeData>();
 		String displayName = Utility.getDisplayLabel(rootEntity.getName()) + " (" + dataList.size() + ")";
 		QueryTreeNodeData labelNode = getResultsTreeLabelNode(rootEntity,labelNodeId,displayName);	
 		treeDataVector.add(labelNode);
 	    
 	    //Note, tree no is set hardcoded 
 	    Long noOfTrees =  Long.valueOf(1);
 	    session.setAttribute(Constants.NO_OF_TREES,noOfTrees);
 	    String key = Constants.TREE_DATA + "_" + 0;
 	    session.setAttribute(key,treeDataVector);
 		
 		return mapping.findForward(Constants.SUCCESS);
 	}
 
 
 	/**
 	 * 
 	 * @param rootNode
 	 * @param uniqueIdNodesMap
 	 * @return
 	 */
 	private String getUniqueNodeID(OutputTreeDataNode rootNode,
 			Map<String, OutputTreeDataNode> uniqueIdNodesMap) 
 	{
 		String labelNodeId = "";
 		
 		Set<String> uniqueKeySet = uniqueIdNodesMap.keySet();
 		Iterator<String> keyItr = uniqueKeySet.iterator();
 		while(keyItr.hasNext())
 		{
 			String keyId = keyItr.next();
 			OutputTreeDataNode treeDataNode = uniqueIdNodesMap.get(keyId);
 			if(treeDataNode.getExpressionId() == rootNode.getExpressionId())
 			{
 				labelNodeId = keyId;
 				break;
 			}
 		}
 		return labelNodeId;
 	}
 
 	/**
 	 * 
 	 * @param labelEntity
 	 * @param labelNodeId
 	 * @param displayName
 	 * @return
 	 */
 	private QueryTreeNodeData getResultsTreeLabelNode(EntityInterface labelEntity, String labelNodeId, String displayName)
 	{
 		String [] strs = labelNodeId.split("_");
 		String treeNo = strs[0];
 		String name = labelEntity.getName();
 		String nodeId = Constants.NULL_ID + Constants.NODE_SEPARATOR +  treeNo + Constants.UNDERSCORE+ Constants.NULL_ID
 		+Constants.UNDERSCORE + Constants.NULL_ID +Constants.NODE_SEPARATOR+labelNodeId + Constants.UNDERSCORE + Constants.LABEL_TREE_NODE;
 		displayName = "<font size='2' color='#297cc7' face='Arial'><b>"+ displayName + "</b></font>";
 		QueryTreeNodeData treeNode = new QueryTreeNodeData();
 		treeNode.setIdentifier(nodeId);
 		treeNode.setObjectName(name);
 		treeNode.setDisplayName(displayName);
 		treeNode.setParentIdentifier(Constants.ZERO_ID);
 		treeNode.setParentObjectName("");
 		return treeNode;
 	}
 }   
 	
     
 	
 	
 	
 	
 
 
