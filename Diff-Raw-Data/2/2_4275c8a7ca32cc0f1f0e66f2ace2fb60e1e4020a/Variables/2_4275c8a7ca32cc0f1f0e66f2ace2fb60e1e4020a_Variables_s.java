 
 package edu.wustl.query.util.global;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public class Variables extends edu.wustl.common.util.global.Variables
 {
 
 	public static int maximumTreeNodeLimit;
 	public static Map<String, String> aliasAndPageOfMap = new HashMap<String, String>();
 	public static String queryGeneratorClassName = "";
 	public static Properties properties;
 
 	public static String abstractQueryClassName = "";
 	public static String abstractQueryManagerClassName = "";
 	public static String abstractQueryUIManagerClassName = "";
 	public static String abstractQueryITableManagerClassName = "";
 	public static String viewIQueryGeneratorClassName = "";
	
 	public static String csmUtility = "";
 
 	public static int recordsPerPageForSpreadSheet;
 	public static int recordsPerPageForTree;
 
 	public static int resultLimit;
 	public static String prepareColTypes(List dataColl)
 	{
 		return prepareColTypes(dataColl, false);
 	}
 
 	public static String prepareColTypes(List dataColl, boolean createCheckBoxColumn)
 	{
 		String colType = "";
 		if (dataColl != null && !dataColl.isEmpty())
 		{
 			List rowDataColl = (List) dataColl.get(0);
 
 			Iterator it = rowDataColl.iterator();
 			if (createCheckBoxColumn == true)
 			{
 				colType = "ch,";
 			}
 			while (it.hasNext())
 			{
 				Object obj = it.next();
 				if (obj != null && obj instanceof Number)
 				{
 					colType = colType + "int,";
 				}
 				else if (obj != null && obj instanceof Date)
 				{
 					colType = colType + "date,";
 				}
 				else
 				{
 					colType = colType + "str,";
 				}
 			}
 		}
 		if (colType.length() > 0)
 		{
 			colType = colType.substring(0, colType.length() - 1);
 		}
 		return colType;
 	}
 
 	/**
 	 * Used for inserting data in batches - Statement.executeBatch()
 	 */
 	public static int batchSize;
 
 	/**
 	 * Query ITABLE
 	 */
 	public static String ITABLE = "QUERY_ITABLE";
 
 	/**
 	 * QUERY EXECUTION LOG TABLE
 	 */
 	public static String QUERY_EXECUTION_LOG = "QUERY_EXECUTION_LOG";
 	/** temp variable **/
 	public static int temp = 1;
 	/** 
 	  * for not Logging XQueries test cases get executed 
 	  **/
 	public static boolean isExecutingTestCase = false;
 	
 	/** Thread class used for Exporting data for get Data Query **/
 	public static String exportDataThreadClassName = "";
 	/** Used to get executor for get Data Query **/
 	public static String dataQueryExecutionClassName = "";
 }
