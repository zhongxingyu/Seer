 
 package edu.wustl.query.spreadsheet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.common.query.factory.ViewIQueryGeneratorFactory;
 import edu.wustl.common.querysuite.queryobject.IOutputAttribute;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.impl.OutputTreeNode;
 import edu.wustl.common.querysuite.queryobject.impl.metadata.SelectedColumnsMetadata;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.queryexecutionmanager.DataQueryResultsBean;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryDetails;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 import edu.wustl.query.viewmanager.AbstractViewIQueryGenerator;
 import edu.wustl.query.viewmanager.NodeId;
 import edu.wustl.query.viewmanager.ViewManager;
 import edu.wustl.query.viewmanager.ViewType;
 
 /**
  * @author vijay_pande
  *
  */
 public class SpreadSheetViewGenerator
 {
 
 	private org.apache.log4j.Logger logger = Logger.getLogger(SpreadSheetViewGenerator.class);
 
 	protected String dataTypeString;
 	protected String idOfClickedNode;
 	protected QueryDetails queryDetails;
 	protected SelectedColumnsMetadata selectedColumnsMetadata;
 
 	public SpreadSheetData createSpreadSheet()
 	{
 		return null;
 	}
 
 	public OutputTreeNode getClickedNode()
 	{
 		return null;
 	}
 
 	public void updateSpreadSheetForDataNode(SpreadSheetData spreadSheetData)
 	{
 
 	}
 
 	public void updateViewOfQuery(ViewType viewType)
 	{
 
 	}
 
 	/**
 	 * @param queryDetailsObj
 	 * @param idOfClickedNode2
 	 * @param spreadsheetData
 	 * @param request 
 	 * @throws QueryModuleException 
 	 */
 	public void createSpreadsheet(QueryDetails queryDetailsObj, SpreadSheetData spreadsheetData,
 			HttpServletRequest request) throws QueryModuleException
 	{
 		idOfClickedNode = request.getParameter(Constants.TREE_NODE_ID);
		idOfClickedNode = "NULL::0_NULL_NULL::0_2_Label";
 		
 		NodeId node = new NodeId(idOfClickedNode);
 
 		ViewManager viewManager = ViewManager.getInstance(ViewType.USER_DEFINED_SPREADSHEET_VIEW);
 		List<IOutputAttribute> selectedColumns = viewManager.getSelectedColumnList(queryDetailsObj
 				.getQuery());
 		AbstractViewIQueryGenerator queryGenerator = ViewIQueryGeneratorFactory
 				.getDefaultViewIQueryGenerator();
 		queryGenerator.createQueryForSpreadSheetView(node, queryDetailsObj); 
 
 		executeQuery(queryDetailsObj.getQuery(), spreadsheetData, request, queryDetailsObj
 				.getQueryExecutionId(), node.getRootData());
 
 		List<String> columnsList = getColumnList(selectedColumns);
 
 		spreadsheetData.setColumnsList(columnsList);
 
 	}
 
 	/**
 	 * @param outputAttributeList
 	 * @return
 	 */
 	private List<String> getColumnList(List<IOutputAttribute> outputAttributeList)
 	{
 		List<String> columnsList = new ArrayList<String>();
 		for (IOutputAttribute outputAttribute : outputAttributeList)
 		{
 			String className = outputAttribute.getAttribute().getEntity().getName();
 			className = Utility.parseClassName(className);
 			String attrLabel = Utility.getDisplayLabel(outputAttribute.getAttribute().getName());
 			columnsList.add(attrLabel + " : " + className);
 		}
 		return columnsList;
 	}
 
 	/**
 	 * @param query
 	 * @param spreadsheetData
 	 * @param request
 	 * @param queryExecutionId
 	 * @param data 
 	 * @throws QueryModuleException
 	 */
 	private void executeQuery(IQuery query, SpreadSheetData spreadsheetData,
 			HttpServletRequest request, int queryExecutionId, String data) throws QueryModuleException
 	{
 		//getData
 		AbstractQueryUIManager queryUIManager = AbstractQueryUIManagerFactory
 				.configureDefaultAbstractUIQueryManager(this.getClass(), request, query);
 
 		DataQueryResultsBean dataQueryResultsBean;
 		if(data.equals(Constants.NULL_ID))
 		{
 			dataQueryResultsBean = queryUIManager.getData(queryExecutionId,
 				ViewType.SPREADSHEET_VIEW);
 		}
 		else
 		{
 			dataQueryResultsBean = queryUIManager.getData(queryExecutionId, data, ViewType.SPREADSHEET_VIEW);
 		}
 		spreadsheetData.setDataList(dataQueryResultsBean.getAttributeList());
 		spreadsheetData.setDataTypeList(dataQueryResultsBean.getDataTypesList());
 	}
 }
