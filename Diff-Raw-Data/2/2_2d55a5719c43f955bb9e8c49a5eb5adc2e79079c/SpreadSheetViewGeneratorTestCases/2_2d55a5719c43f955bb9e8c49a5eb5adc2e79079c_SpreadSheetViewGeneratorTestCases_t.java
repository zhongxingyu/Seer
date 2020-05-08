 
 package edu.wustl.query.spreadsheet;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import edu.wustl.common.bizlogic.QueryBizLogic;
 import edu.wustl.query.utility.Utility;
 import edu.wustl.query.spreadsheet.SpreadSheetData;
 import edu.wustl.query.spreadsheet.SpreadSheetViewGenerator;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Variables;
 import edu.wustl.query.util.querysuite.QueryDetails;
 import edu.wustl.query.viewmanager.NodeId;
 import edu.wustl.query.viewmanager.ViewType;
 import edu.wustl.common.query.impl.PassTwoXQueryGenerator;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.query.queryobject.util.QueryObjectProcessor;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.exceptions.SqlException;
 import edu.wustl.common.querysuite.queryobject.impl.ParameterizedQuery;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.cab2b.server.cache.EntityCache;
 
 
 /**
  * @author vijay_pande
  *
  */
 public class SpreadSheetViewGeneratorTestCases extends TestCase
 {
 	public SpreadSheetViewGeneratorTestCases()
 	{
 		super();
 	}
 
 	static{
 		/**
 		 * Indicating - Do not LOG XQueries
 		 */
 		Variables.isExecutingTestCase = true;
 	        Variables.queryGeneratorClassName = "edu.wustl.common.query.impl.PassOneXQueryGenerator";
 	}
 	
 	
 	public void testCreateSpreadsheet()
 	{
 		try
 		{
 			Variables.viewIQueryGeneratorClassName = "edu.wustl.cider.query.viewgenerator.CiderViewIQueryGenerator";
 			SpreadSheetViewGenerator spreadSheetViewGenerator = new SpreadSheetViewGenerator(ViewType.USER_DEFINED_SPREADSHEET_VIEW);
 			String idOfClickedNode = "NULL::0_NULL_NULL::0_2_Label";
 			NodeId node = new NodeId(idOfClickedNode);
 			
 			QueryDetails queryDetailsObj= getQueryDetailsObj();
 			
 			SpreadSheetData spreadsheetData = new SpreadSheetData();
 			
			spreadSheetViewGenerator.createSpreadsheet(node, queryDetailsObj, spreadsheetData,null);
 			
 			assertTrue("Spreadsheet object populated successfully  ",true);
 		}
 		catch (Exception e) 
 		{
 			System.out.println("Test case failed "+e.getMessage());
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	private ParameterizedQuery getDataQuery() throws DAOException
 	{
 		ParameterizedQuery query = null;
 		QueryBizLogic queryBizLogic = new QueryBizLogic();
 		
 		List queryList = queryBizLogic.retrieve(ParameterizedQuery.class.getName(), "type", Constants.QUERY_TYPE_GET_DATA);
 		if(queryList!=null && queryList.size()>0)
 		{
 			query = (ParameterizedQuery)queryList.get(0);
 		}
 		return query;
 	}
 	
 	private QueryDetails getQueryDetailsObj() throws DAOException, MultipleRootsException, SqlException
 	{
 		IQuery query =getDataQuery();
 		QueryDetails queryDetailsObj = new QueryDetails();
 		queryDetailsObj.setQueryExecutionId(4);
 		queryDetailsObj.setQuery(query);
 		
 		PassTwoXQueryGenerator passTwoQueryGenerator = new PassTwoXQueryGenerator();
 		String generatedQuery = passTwoQueryGenerator.generateQuery(query);
 		//Get the root out put node list , which gives the root node
 		List<OutputTreeDataNode> rootOutputTreeNodeList = passTwoQueryGenerator
 				.getRootOutputTreeNodeList();
 		Map<String, OutputTreeDataNode> uniqueIdNodesMap = QueryObjectProcessor
 		.getAllChildrenNodes(rootOutputTreeNodeList);
 		queryDetailsObj.setUniqueIdNodesMap(uniqueIdNodesMap);
 		
 		return queryDetailsObj;
 	}
 }
