 
 package edu.wustl.query.spreadsheet;
 
 import java.util.ArrayList;
 import java.util.List;
 
import javax.servlet.http.HttpServletRequest;

import edu.wustl.common.query.AbstractQuery;
 import edu.wustl.common.query.factory.ViewIQueryGeneratorFactory;
 import edu.wustl.common.querysuite.queryobject.IOutputAttribute;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.impl.metadata.SelectedColumnsMetadata;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.QueryDetails;
 import edu.wustl.query.util.querysuite.QueryModuleError;
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
	protected ViewType viewType;
 
	
 	public SpreadSheetViewGenerator(ViewType viewType)
 	{
 		this.viewType = viewType;
 	}
 	
 	public void updateViewOfQuery(ViewType viewType)
 	{
 		this.viewType = viewType;
 	}
 
 	/**
 	 * @param node
 	 * @param queryDetailsObj
 	 * @param spreadsheetData
 	 * @throws QueryModuleException 
 	 */
 	public List<IQuery> createSpreadsheet(NodeId node, QueryDetails queryDetailsObj,
			SpreadSheetData spreadsheetData,AbstractQuery abstractQuery)
 			throws QueryModuleException
 	{
 		ViewManager viewManager = ViewManager.getInstance(viewType);
 		
 		AbstractViewIQueryGenerator queryGenerator = ViewIQueryGeneratorFactory
 				.getDefaultViewIQueryGenerator();
 		List<IQuery> queries = queryGenerator.createQueryForSpreadSheetView(node, queryDetailsObj);
 
 		List<IOutputAttribute> selectedColumns = viewManager.getSelectedColumnList(queries.get(0));
		List<String> columnsList = getColumnList(selectedColumns,abstractQuery);
 
 		spreadsheetData.setColumnsList(columnsList);
 		
 		return queries;
 	}
 
 	/**
 	 * @param outputAttributeList
 	 * @return
 	 * @throws QueryModuleException 
 	 */
	protected List<String> getColumnList(List<IOutputAttribute> outputAttributeList,AbstractQuery abstractQuery)
 			throws QueryModuleException
 	{
 		List<String> columnsList = new ArrayList<String>();
 		if (outputAttributeList.size() == 0)
 		{
 			logger.error("No output attribute defined for patient data query");
 			throw new QueryModuleException("No output attribute defined for patient data query",
 					QueryModuleError.GENERIC_EXCEPTION);
 		}
 		for (IOutputAttribute outputAttribute : outputAttributeList)
 		{
 			if(!(outputAttribute.getAttribute().getName().equals(Constants.ID)
 					&& outputAttribute.getAttribute().getEntity().getName().equals(Constants.MED_ENTITY_NAME)))
 			{
 				String className = outputAttribute.getExpression().getQueryEntity()
 						.getDynamicExtensionsEntity().getName();
 				className = Utility.parseClassName(className);
 				String attrLabel = Utility.getDisplayLabel(outputAttribute.getAttribute().getName());
 				columnsList.add(attrLabel + " : " + className);
 			}
 		}
	    return columnsList;
 	}
 }
