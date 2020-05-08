 
 package edu.wustl.query.util.global;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 import edu.wustl.common.beans.QueryResultObjectData;
 import edu.wustl.common.beans.QueryResultObjectDataBean;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.bizlogic.QueryBizLogic;
 import edu.wustl.common.dao.AbstractDAO;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.QuerySessionData;
 import edu.wustl.common.dao.queryExecutor.PagenatedResultData;
 import edu.wustl.common.querysuite.queryobject.IAbstractQuery;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IOutputAttribute;
 import edu.wustl.common.querysuite.queryobject.IParameterizedQuery;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.LogicalOperator;
 import edu.wustl.common.querysuite.queryobject.RelationalOperator;
 import edu.wustl.common.querysuite.queryobject.impl.AbstractQuery;
 import edu.wustl.common.querysuite.queryobject.impl.ParameterizedQuery;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.bizlogic.QueryOutputSpreadsheetBizLogic;
 import edu.wustl.query.bizlogic.WorkflowBizLogic;
 import edu.wustl.query.querymanager.Count;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.IQueryUpdationUtil;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 
 public class Utility extends edu.wustl.common.util.Utility
 {
 
 	private static String pattern = "MM-dd-yyyy";
 
 	public static Object toNewGridFormat(Object obj)
 	{
 		obj = toGridFormat(obj);
 		if (obj instanceof String)
 		{
 			String objString = (String) obj;
 			StringBuffer tokenedString = new StringBuffer();
 
 			StringTokenizer tokenString = new StringTokenizer(objString, ",");
 
 			while (tokenString.hasMoreTokens())
 			{
 				tokenedString.append(tokenString.nextToken() + " ");
 			}
 			String gridFormattedStr = new String(tokenedString);
 			obj = gridFormattedStr;
 		}
 
 		return obj;
 	}
 
 	/**
 	 * Adds the attribute values in the list in sorted order and returns the
 	 * list containing the attribute values in proper order
 	 * 
 	 * @param dataType -
 	 *            data type of the attribute value
 	 * @param value1 -
 	 *            first attribute value
 	 * @param value2 -
 	 *            second attribute value
 	 * @return List containing value1 and valu2 in sorted order
 	 */
 	public static ArrayList<String> getAttributeValuesInProperOrder(String dataType, String value1,
 			String value2)
 	{
 		String v1 = value1;
 		String v2 = value2;
 		ArrayList<String> attributeValues = new ArrayList<String>();
 		if (dataType.equalsIgnoreCase(EntityManagerConstantsInterface.DATE_ATTRIBUTE_TYPE))
 		{
 			SimpleDateFormat df = new SimpleDateFormat(pattern);
 			try
 			{
 				Date date1 = df.parse(value1);
 				Date date2 = df.parse(value2);
 				if (date1.after(date2))
 				{
 					v1 = value2;
 					v2 = value1;
 				}
 			}
 			catch (ParseException e)
 			{
 				Logger.out
 						.error("Can not parse the given date in getAttributeValuesInProperOrder() method :"
 								+ e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			if (dataType.equalsIgnoreCase(EntityManagerConstantsInterface.INTEGER_ATTRIBUTE_TYPE)
 					|| dataType
 							.equalsIgnoreCase(EntityManagerConstantsInterface.LONG_ATTRIBUTE_TYPE))
 			{
 				if (Long.parseLong(value1) > Long.parseLong(value2))
 				{
 					v1 = value2;
 					v2 = value1;
 				}
 
 			}
 			else
 			{
 				if (dataType
 						.equalsIgnoreCase(EntityManagerConstantsInterface.DOUBLE_ATTRIBUTE_TYPE))
 				{
 					if (Double.parseDouble(value1) > Double.parseDouble(value2))
 					{
 						v1 = value2;
 						v2 = value1;
 					}
 
 				}
 			}
 		}
 		attributeValues.add(v1);
 		attributeValues.add(v2);
 		return attributeValues;
 	}
 
 	public static String getmyData(List dataList)
 	{
 		String myData = "[";
 		int i = 0;
 		if (dataList != null && dataList.size() != 0)
 		{
 			for (i = 0; i < (dataList.size() - 1); i++)
 			{
 				List row = (List) dataList.get(i);
 				int j;
 				myData = myData + "\"";
 				for (j = 0; j < (row.size() - 1); j++)
 				{
 					myData = myData + Utility.toNewGridFormat(row.get(j)).toString();
 					myData = myData + ",";
 				}
 				myData = myData + Utility.toNewGridFormat(row.get(j)).toString();
 				myData = myData + "\"";
 				myData = myData + ",";
 			}
 
 			List row = (List) dataList.get(i);
 			int j;
 			myData = myData + "\"";
 			for (j = 0; j < (row.size() - 1); j++)
 			{
 				myData = myData + Utility.toNewGridFormat(row.get(j)).toString();
 				myData = myData + ",";
 			}
 			myData = myData + Utility.toNewGridFormat(row.get(j)).toString();
 			myData = myData + "\"";
 		}
 		myData = myData + "]";
 		return myData;
 	}
 
 	public static String getcolumns(List columnList)
 	{
 		String columns = "\"";
 		int col;
 		if (columnList != null)
 		{
 			for (col = 0; col < (columnList.size() - 1); col++)
 			{
 				columns = columns + columnList.get(col);
 				columns = columns + ",";
 			}
 			columns = columns + columnList.get(col);
 		}
 		columns = columns + "\"";
 		return columns;
 	}
 
 	public static String getcolWidth(List columnList, boolean isWidthInPercent)
 	{
 
 		String colWidth = "\"";
 		int col;
 		if (columnList != null)
 		{
 			String fixedColWidth = null;
 			if (isWidthInPercent)
 			{
 				fixedColWidth = String.valueOf(100 / columnList.size());
 			}
 			else
 			{
 				fixedColWidth = "100";
 			}
 			for (col = 0; col < (columnList.size() - 1); col++)
 			{
 				colWidth = colWidth + fixedColWidth;
 				colWidth = colWidth + ",";
 			}
 			colWidth = colWidth + fixedColWidth;
 		}
 		colWidth = colWidth + "\"";
 		return colWidth;
 	}
 
 	public static String getcolTypes(List dataList)
 	{
 		StringBuffer colTypes = new StringBuffer();
 		colTypes.append("\"");
 		colTypes.append(Variables.prepareColTypes(dataList));
 		colTypes.append("\"");
 		return colTypes.toString();
 	}
 
 	public static void setGridData(List dataList, List columnList, HttpServletRequest request)
 	{
 		request.setAttribute("myData", getmyData(dataList));
 		request.setAttribute("columns", getcolumns(columnList));
 		boolean isWidthInPercent = false;
 		if (columnList.size() < 10)
 		{
 			isWidthInPercent = true;
 		}
 		request.setAttribute("colWidth", getcolWidth(columnList, isWidthInPercent));
 		request.setAttribute("isWidthInPercent", isWidthInPercent);
 		request.setAttribute("colTypes", getcolTypes(dataList));
 		int heightOfGrid = 100;
 		if (dataList != null)
 		{
 			int noOfRows = dataList.size();
 			heightOfGrid = (noOfRows + 2) * 20;
 			if (heightOfGrid > 240)
 			{
 				heightOfGrid = 230;
 			}
 		}
 		request.setAttribute("heightOfGrid", heightOfGrid);
 		int col = 0;
 		int i = 0;
 		String hiddenColumnNumbers = "";
 		if (columnList != null)
 		{
 			for (col = 0; col < columnList.size(); col++)
 			{
 				if (columnList.get(col).toString().trim().equals("ID")
 						|| columnList.get(col).toString().trim().equals("Status")
 						|| columnList.get(col).toString().trim().equals("Site Name")
 						|| columnList.get(col).toString().trim().equals("Report Collection Date"))
 				{
 					hiddenColumnNumbers = hiddenColumnNumbers + "hiddenColumnNumbers[" + i + "] = "
 							+ col + ";";
 					i++;
 				}
 			}
 		}
 		request.setAttribute("hiddenColumnNumbers", hiddenColumnNumbers);
 	}
 
 	public static Object toNewGridFormatWithHref(List<String> row,
 			Map<Integer, QueryResultObjectData> hyperlinkColumnMap, int index)
 	{
 		Object obj = row.get(index);
 
 		if (obj instanceof String)
 		{
 			obj = toNewGridFormat(obj);
 
 			QueryResultObjectData queryResultObjectData = hyperlinkColumnMap.get(index);
 
 			if (queryResultObjectData != null)// This column is to be shown as
 			// hyperlink.
 			{
 				if (obj == null || obj.equals(""))
 				{
 					obj = "NA";
 				}
 
 				/**
 				 * Name : Prafull Bug ID: 4223 Patch ID: 4223_1 Description:
 				 * Edit User: password fields empty & error on submitting new
 				 * password Added PageOf Attribute as request parameter in the
 				 * link.
 				 */
 				String aliasName = queryResultObjectData.getAliasName();
 				String link = "SimpleSearchEdit.do?"
 						+ Constants.TABLE_ALIAS_NAME + "=" + aliasName
 						+ "&" + Constants.SYSTEM_IDENTIFIER + "="
 						+ row.get(queryResultObjectData.getIdentifierColumnId()) + "&"
 						+ Constants.PAGE_OF + "=" + Variables.aliasAndPageOfMap.get(aliasName);
 				/**
 				 * bug ID: 4225 Patch id: 4225_1 Description : Passing a
 				 * different name to the popup window
 				 */
 				String onclickStr = " onclick=javascript:NewWindow('" + link
 						+ "','simpleSearch','800','600','yes') ";
 				String hrefTag = "<a class='normalLink' href='#'" + onclickStr + ">" + obj + "</a>";
 				// String hrefTag = "<a href='"+ link+ "'>"+obj+"</a>";
 				obj = hrefTag;
 			}
 		}
 		return obj;
 	}
 
 	/**
 	 * This method creates a comma separated string of numbers representing
 	 * column width.
 	 * 
 	 */
 	public static String getColumnWidth(List columnNames)
 	{
 		String colWidth = getColumnWidth((String) columnNames.get(0));
 
 		int size = columnNames.size()-1;
 
 		for (int col = 0; col < size; col++)
 		{
 			String columnName = (String) columnNames.get(col);
 			colWidth = colWidth + "," + getColumnWidth(columnName);
 		}
 		return colWidth;
 	}
 
 	private static String getColumnWidth(String columnName)
 	{
 		/*
 		 * Patch ID: Bug#3090_31 Description: The first column which is just a
 		 * checkbox, used to select the rows, was always given a width of 100.
 		 * Now width of 20 is set for the first column. Also, width of 100 was
 		 * being applied to each column of the grid, which increasing the total
 		 * width of the grid. Now the width of each column is set to 80.
 		 */
 		String columnWidth = null;
 		if ("ID".equals(columnName.trim()))
 		{
 			columnWidth = "0";
 		}
 		else if ("".equals(columnName.trim()))
 		{
 			columnWidth = "20";
 		}
 		else
 		{
 			columnWidth = "150";
 		}
 		return columnWidth;
 	}
 
 	public static String getColumnWidthP(List columnNames)
 	{
 		StringBuffer colWidth = new StringBuffer("");
 
 		int size = columnNames.size();
 		double temp = 97.5f;
 		for (int col = 0; col < size; col++)
 		{
 			colWidth = colWidth.append(String.valueOf(temp / (size)));
 			colWidth = colWidth.append(",");
 		}
 		if(colWidth.lastIndexOf(",")== colWidth.length()-1)
 		{
 			colWidth.deleteCharAt(colWidth.length()-1);
 		}
 		return colWidth.toString();
 	}
 
 	/**
 	 * limits the title of the saved query to 125 characters to avoid horizontal
 	 * scrollbar
 	 * 
 	 * @param title -
 	 *            title of the saved query (may be greater than 125 characters)
 	 * @return - query title upto 125 characters
 	 */
 
 	public static String getQueryTitle(String title)
 	{
 		String multilineTitle = "";
 		if (title.length() <= Constants.CHARACTERS_IN_ONE_LINE)
 		{
 			multilineTitle = title;
 		}
 		else
 		{
 			multilineTitle = title.substring(0, Constants.CHARACTERS_IN_ONE_LINE) + ".....";
 		}
 		return multilineTitle;
 	}
 
 	/**
 	 * returns the entire title to display it in tooltip .
 	 * 
 	 * @param title -
 	 *            title of the saved query
 	 * @return tooltip string
 	 */
 	public static String getTooltip(String title)
 	{
 		String tooltip = title.replaceAll("'", Constants.SINGLE_QUOTE_ESCAPE_SEQUENCE); // escape sequence
 		// for '
 		return tooltip;
 	}
 
 	public static List getPaginationDataList(HttpServletRequest request,
 			SessionDataBean sessionData, int recordsPerPage, int pageNum,
 			QuerySessionData querySessionData) throws DAOException
 	{
 		List paginationDataList;
 		querySessionData.setRecordsPerPage(recordsPerPage);
 		int startIndex = recordsPerPage * (pageNum - 1);
 		QueryBizLogic qBizLogic = new QueryBizLogic();
 		PagenatedResultData pagenatedResultData = qBizLogic.execute(sessionData, querySessionData,
 				startIndex);
 		paginationDataList = pagenatedResultData.getResult();
 		String isSimpleSearch = (String) request.getSession().getAttribute(
 				Constants.IS_SIMPLE_SEARCH);
 		if (isSimpleSearch == null || (!isSimpleSearch.equalsIgnoreCase(Constants.TRUE)))
 		{
 			Map<Long, QueryResultObjectDataBean> queryResultObjectDataBeanMap = querySessionData
 					.getQueryResultObjectDataMap();
 			if (queryResultObjectDataBeanMap != null)
 			{
 				for (Long id : queryResultObjectDataBeanMap.keySet())
 				{
 					QueryResultObjectDataBean bean = queryResultObjectDataBeanMap.get(id);
 					if (bean.isClobeType())
 					{
 						List<String> columnsList = (List<String>) request.getSession()
 								.getAttribute(Constants.SPREADSHEET_COLUMN_LIST);
 						QueryOutputSpreadsheetBizLogic queryBizLogic = new QueryOutputSpreadsheetBizLogic();
 						Map<Integer, Integer> fileTypeIndexMainEntityIndexMap = queryBizLogic
 								.updateSpreadSheetColumnList(columnsList,
 										queryResultObjectDataBeanMap);
 						//	QueryOutputSpreadsheetBizLogic.updateDataList(paginationDataList, fileTypeIndexMainEntityIndexMap);
 						Map exportMetataDataMap = QueryOutputSpreadsheetBizLogic.updateDataList(
 								paginationDataList, fileTypeIndexMainEntityIndexMap);
 						request.getSession().setAttribute(Constants.ENTITY_IDS_MAP,
 								exportMetataDataMap.get(Constants.ENTITY_IDS_MAP));
 						request.getSession().setAttribute(Constants.EXPORT_DATA_LIST,
 								exportMetataDataMap.get(Constants.EXPORT_DATA_LIST));
 						break;
 					}
 				}
 			}
 		}
 		return paginationDataList;
 	}
 
 	/**
 	 * @param entity entity for which Primary Key list is required
 	 * @return List<String> primary key list
 	 */
 	public static List<String> getPrimaryKey(EntityInterface entity)
 	{
 		Collection<TaggedValueInterface> taggedValueCollection = entity.getTaggedValueCollection();
 		List<String> primaryKeyList = new ArrayList<String>();
 		for (TaggedValueInterface tag : taggedValueCollection)
 		{
 			if (Constants.PRIMARY_KEY_TAG_NAME.equals(tag.getKey()))
 			{
 				StringTokenizer stringTokenizer = new StringTokenizer(tag.getValue(), ",");
 				for (int i = 0; stringTokenizer.hasMoreTokens(); i++)
 				{
 					primaryKeyList.add(stringTokenizer.nextToken());
 				}
 			}
 		}
 		return primaryKeyList;
 	}
 
 	/**
 	 * Method to generate SQL for Node
 	 * @param parentNodeId parent node id which contains data
 	 * @param tableName temporary table name
 	 * @param parentIdColumnName parent node ID column names (, separated)
 	 * @param selectSql SQL 
 	 * @param idColumnOfCurrentNode id column name (, separated) of current node
 	 * @return SQL for current node
 	 */
 	public static String getSQLForNode(String parentNodeId, String tableName,
 			String parentIdColumnName, String selectSql, String idColumnOfCurrentNode)
 	{
 		selectSql = selectSql + Constants.FROM + tableName;
 		if (parentNodeId != null)
 		{
 			selectSql = selectSql + Constants.WHERE + " (";
 			StringTokenizer stringTokenizerParentID = new StringTokenizer(parentIdColumnName, ",");
 			StringTokenizer stringTokenizerParentNodeID = new StringTokenizer(parentNodeId,
 					Constants.PRIMARY_KEY_ATTRIBUTE_SEPARATOR);
 			while (stringTokenizerParentID.hasMoreElements())
 			{
 				selectSql = selectSql + stringTokenizerParentID.nextElement() + " = '"
 						+ stringTokenizerParentNodeID.nextElement() + "' " + LogicalOperator.And
 						+ " ";
 			}
 			StringTokenizer stringTokenizerIDofCurrentNode = new StringTokenizer(
 					idColumnOfCurrentNode, ",");
 			while (stringTokenizerIDofCurrentNode.hasMoreElements())
 			{
 				selectSql = selectSql + " " + stringTokenizerIDofCurrentNode.nextElement() + " "
 						+ RelationalOperator.getSQL(RelationalOperator.IsNotNull) + " "
 						+ LogicalOperator.And;
 			}
 			if (selectSql.substring(selectSql.length() - 3).equals(LogicalOperator.And.toString()))
 			{
 				selectSql = selectSql.substring(0, selectSql.length() - 3);
 			}
 			selectSql = selectSql + ")";
 		}
 		return selectSql;
 	}
 
 	public static ActionErrors setActionError(String errorMessage, String key)
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionError error = new ActionError("errors.item", errorMessage);
 		errors.add(ActionErrors.GLOBAL_ERROR, error);
 		return errors;
 	}
 
 	/**
 	 * This method returns the session data bean
 	 * @param request
 	 * @return
 	 */
 	public static SessionDataBean getSessionData(HttpServletRequest request)
 	{
 		Object obj = request.getSession().getAttribute(Constants.SESSION_DATA);
 		SessionDataBean sessionData = null;
 		if (obj != null)
 		{
 			sessionData = (SessionDataBean) obj;
 			return sessionData;
 		}
 		return sessionData;
 	}
 
 	/**
 	 * Method checks if attribute is not viewable in results view of Query.
 	 * @param attribute
 	 * @return isNotViewable
 	 */
 	public static boolean isNotViewable(AttributeInterface attribute)
 	{
 		boolean isNotViewable = false;
 		Collection<TaggedValueInterface> taggedValueCollection = attribute
 				.getTaggedValueCollection();
 		for (TaggedValueInterface tagValue : taggedValueCollection)
 		{
 			if (tagValue.getKey().equals(Constants.TAGGED_VALUE_NOT_VIEWABLE))
 			{
 				isNotViewable = true;
 				break;
 			}
 		}
 		return isNotViewable;
 	}
 
 	public static boolean isMainEntity(EntityInterface entity)
 	{
 		boolean isMainEntity = false;
 		Collection<TaggedValueInterface> taggedValueCollection = entity.getTaggedValueCollection();
 		for (TaggedValueInterface tagValue : taggedValueCollection)
 		{
 			if (tagValue.getKey().equals(Constants.TAGGED_VALUE_MAIN_ENTIY))
 			{
 				isMainEntity = true;
 				break;
 			}
 		}
 		return isMainEntity;
 	}
 
 	public static String removeLastAnd(String select)
 	{
 		String selectString = select;
 		if (select.endsWith(Constants.QUERY_AND))
 		{
 			selectString = selectString.substring(0, selectString.length() - 5);
 		}
 		return selectString;
 	}
 
 	public static void removeLastComma(StringBuilder string)
 	{
 		if (Constants.QUERY_COMMA.equals(string.substring(string.length() - 2)))
 		{
 			string.delete(string.length() - 2, string.length());
 		}
 	}
 
 	public static boolean istagPresent(AbstractMetadataInterface entity, String tag)
 	{
 		boolean isTagPresent = false;
 		Collection<TaggedValueInterface> taggedValueCollection = entity.getTaggedValueCollection();
 		for (TaggedValueInterface tagValue : taggedValueCollection)
 		{
 			if (tagValue.getKey().equals(tag))
 			{
 				isTagPresent = true;
 				break;
 			}
 		}
 		return isTagPresent;
 	}
 
 	/**
 	 * Method to update Query Objects with containments.
 	 * @param session object
 	 * @param query to be updated.
 	 */
 	public static void updateIQueryForContainments(HttpSession session, IQuery query,
 			boolean isDefaultConditionPresent)
 	{
 		if (query != null)
 		{
 			Map<Integer, HashMap<EntityInterface, List<EntityInterface>>> eachExpressionParentChildMap = IQueryUpdationUtil
 					.getAllConatainmentObjects(query, session, isDefaultConditionPresent);
 
 			//Update the IQuery with containment objects......add only those containment objects which are not present in IQuery
 			IQueryUpdationUtil.addConatinmentObjectsToIquery(query, session);
 
 			//IQueryUpdationUtil.addDefaultConditionToIquery(query,session);
 
 			//Add the link/association among parent and containment entities
 			IQueryUpdationUtil.addLinks(eachExpressionParentChildMap, session, query);
 
 		}
 	}
 
 	/**
 	 * get the alias for given attribute to identify it uniquely
 	 * @param attribute
 	 * @return
 	 */
 	public static String getAliasFor(AttributeInterface attribute, IExpression expression)
 	{
 		return getAliasFor(attribute.getName(), expression);
 	}
 
 	public static String getAliasFor(String attributeName, IExpression expression)
 	{
 		return attributeName + "_" + expression.getExpressionId();
 	}
 	
 	/**
 	 * To get IQuery object from Query Id
 	 * @param queryId
 	 * @return
 	 * @throws DAOException
 	 */
 	public static IAbstractQuery getQuery(Long queryId) throws DAOException
 	{
 		IAbstractQuery query = null;
 
 		if (queryId != null)
 		{
 			AbstractDAO dao = DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 			dao.openSession(null);
 			query = (IAbstractQuery) dao.retrieve(AbstractQuery.class.getName(), queryId);
 			dao.closeSession();
 		}
 
 		return query;
 	}
 	
 	/**
 	 * 
 	 * @param entity
 	 * @param tag
 	 * @return
 	 */
 	public static String getTagValue(AbstractMetadataInterface entity, String tag)
 	{
 		String value = "";
 		Collection<TaggedValueInterface> taggedValueCollection = entity.getTaggedValueCollection();
 		for (TaggedValueInterface tagValue : taggedValueCollection)
 		{
 			if (tagValue.getKey().equals(tag))
 			{
 				value = tagValue.getValue();
 				break;
 			}
 		}
 		return value;
 	}
 
 	
 	public static void setQueryOutputAttributeList(IParameterizedQuery query,
 			List<IOutputAttribute> newoutputAttributeList) {
 		query.getOutputAttributeList().clear();
 		  query.getOutputAttributeList().addAll(newoutputAttributeList);
 	}
 	
 	
 	
 	/** This method is used to get a display name
 	 * @param outputAttribute
 	 * @return
 	 */
 	public static String getDisplayNameForColumn(IOutputAttribute outputAttribute)
 	{
 		String columnDisplayName = "";
 		AttributeInterface attribute = outputAttribute.getAttribute();
 		String className = Utility.parseClassName(outputAttribute.getExpression().getQueryEntity().getDynamicExtensionsEntity().getName());
 		className = Utility.getDisplayLabel(className);
 		String attributeLabel = Utility.getDisplayLabel(attribute.getName());
 		columnDisplayName = className +" : " + attributeLabel ;
 		return columnDisplayName;
 	}
 	
 	/**
      * Private method used to generate the List of JSON objects.
      *
      * @param executionIdMap
      *            Execution Id Map
      * @param workflowBizLogic
      *            Instance of BizLogic to be used.
      * @param qUIManager
      *            Instance of the Query UI Manager.
      * @param projectId
      *            Project Id
      * @return The List of JSON Objects
      * @throws QueryModuleException
      *             if error while executing the query.
      */
 	public static List<JSONObject> generateExecutionQueryResults(Map<Long, Integer> executionIdMap,
             WorkflowBizLogic workflowBizLogic, AbstractQueryUIManager qUIManager,
            boolean hasSecurePrivilege) throws QueryModuleException
     {
 		Count resultCount = null;
         JSONObject jsonObject = null;
         List<JSONObject> executionQueryResults = new ArrayList<JSONObject>();
 
 	    Set<Long> titleset = executionIdMap.keySet();
         Iterator<Long> iterator = titleset.iterator();
         while (iterator.hasNext())
         {
             Long query = iterator.next();
 
             resultCount = workflowBizLogic
                    .getCount(executionIdMap.get(query));
            boolean hasFewRecords = qUIManager.checkTooFewRecords(
                      resultCount,hasSecurePrivilege);
            if (hasFewRecords)
            {
            	resultCount.setCount(0);
            } 
             jsonObject = createResultJSON(query,
                         resultCount.getCount(), resultCount
                                 .getStatus(), resultCount
                                 .getQueryExectionId());
             executionQueryResults.add(jsonObject);
         }
         return executionQueryResults;
 	}
 	
 	/**
      * @param queryId =Query identifier for which execute request sent
      * @param errormessage
      * @param workflowId
      * @param queryIndex=row number where results to be displayed
      * @param resultCount=value of result count for query
      * @returns jsonObject
      *
      * creates the jsonObject for input parameters
      */
     public static JSONObject createResultJSON(Long queryId, int resultCount,
             String status, int executionLogId)
     {
         JSONObject resultObject = null;
         resultObject = new JSONObject();
         try
         {
             resultObject.append("queryId", queryId);
             resultObject.append("queryResult", resultCount);
             resultObject.append("status", status);
             resultObject.append("executionLogId", executionLogId);
         }
         catch (JSONException e)
         {
             Logger.out.info("error in initializing json object " + e);
         }
 
         return resultObject;
     }
     /**
      * This method checks if attribute is already present in outputAttributeList of IQuery.
      * @param query
      * @param attribute
      * @return isPresent
      */
     public static boolean isPresentInOutputAttrList(IQuery query, AttributeInterface attribute, IExpression expression)
 	{
 		boolean isPresent = false;
 		for(IOutputAttribute outputAttribute : ((ParameterizedQuery)query).getOutputAttributeList())
 		{
 			if(outputAttribute.getAttribute().equals(attribute) 
 					&& outputAttribute.getExpression().equals(expression))
 			{
 				isPresent = true;
 				break;
 			}
 		}
 		return isPresent;
 	}
 }
