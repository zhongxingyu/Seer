 
 package edu.wustl.query.action;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.wustl.common.beans.RecentQueriesBean;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.dao.AbstractDAO;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.JDBCDAO;
 import edu.wustl.common.dao.queryExecutor.PagenatedResultData;
 import edu.wustl.common.querysuite.queryobject.impl.AbstractQuery;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.query.actionForm.ShowRetrieveRecentForm;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.QueryModuleError;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 
 /**
 * @author chitra_garg
 *retrieves the recent queries and set the pagination on
 *retrieved data
 */
 public class RetrieveRecentQueriesAction extends Action
 {
 
 	@Override
     public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		populateRecentQueries(request, (ShowRetrieveRecentForm) actionForm);
 
 		return actionMapping.findForward(request.getParameter(Constants.PAGE_OF));
 
 	}
 
 	/**
 	 * set the total recent queries count for
 	 * a user in session
 	 * @param request
 	 * @return
 	 * @throws QueryModuleException
 	 */
 	public int setRecentQueriesCount(SessionDataBean sessionDataBean) throws QueryModuleException
 	{
 
        String sql ="SELECT COUNT(*) "+
        "FROM QUERY_EXECUTION_LOG qel, COUNT_QUERY_EXECUTION_LOG cqel "+
        "WHERE qel.QUERY_EXECUTION_ID = cqel.COUNT_QUERY_EXECUTION_ID and cqel.USER_ID="+
        sessionDataBean.getUserId() + " and qel.QUERY_TYPE='C' and qel.WORKFLOW_ID IS NULL";

 		int numberOfQueries = 0;
 		List<List<String>> resultCount = executeQuery(sql, sessionDataBean, false, false, null, -1,
 				-1);
 		if (resultCount != null)
 		{
 			numberOfQueries = Integer.valueOf(resultCount.get(0).get(0));
 		}
 
 		return numberOfQueries;
 	}
 
 	/**
 	 * This method set the total number of records to display
 	 * @param request
 	 * @return
 	 * @throws QueryModuleException
 	 */
 	private int setDisplayedRecentQueryCount(HttpServletRequest request)
 			throws QueryModuleException
 	{
 
 		SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 				Constants.SESSION_DATA);
 		int recordCountToDisplay = setRecentQueriesCount(sessionDataBean);
 		String showLastfromRequest = request.getParameter(Constants.SHOW_LAST);
 		int showLast = 0;
 		if (showLastfromRequest == null)
 		{
 			showLast = Constants.SHOW_LAST_COUNT;//25;
 		}
 		else
 		{
 			showLast = Integer.valueOf(request.getParameter(Constants.SHOW_LAST));
 
 		}
 		request.setAttribute(Constants.RESULTS_PER_PAGE, showLast);
 		request.setAttribute("records", recordCountToDisplay);
 		if (showLast < recordCountToDisplay)
 		{
 			recordCountToDisplay = showLast;
 		}
 		return recordCountToDisplay;
 
 	}
 
 	/**
 	 * @param request
 	 * @param showRetrieveRecentForm
 	 * @throws DAOException
 	 * @throws QueryModuleException
 	 */
 	private void populateRecentQueries(HttpServletRequest request,
 			ShowRetrieveRecentForm showRetrieveRecentForm) throws DAOException,
 			QueryModuleException
 	{
 		SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 				Constants.SESSION_DATA);
 
 		request.setAttribute(Constants.PAGE_OF,request.getParameter(Constants.PAGE_OF));
 		request.setAttribute(Constants.RESULTS_PER_PAGE_OPTIONS, Constants.SHOW_LAST_OPTION);
 		int lastIndex = setDisplayedRecentQueryCount(request);
 		List<List<String>> queryResultList = retrieveQueries(sessionDataBean, lastIndex);
 
 		List<RecentQueriesBean> recentQueriesBeanList = populateRecentQueryBean(sessionDataBean,
 				queryResultList);
 		showRetrieveRecentForm.setRecentQueriesBeanList(recentQueriesBeanList);
 		request.setAttribute(Constants.RECENT_QUERIES_BEAN_LIST, recentQueriesBeanList);
 
 
 	}
 
 	/**
 	 * This method returns the  Query List
 	 *
 	 * @param sessionDataBean
 	 * @param sql
 	 * @param pageNum
 	 * @param recordsPerPage
 	 * @param lastIndex
 	 * @return
 	 * @throws QueryModuleException
 	 */
 	private List<List<String>> retrieveQueries(SessionDataBean sessionDataBean,int lastIndex)
 	throws QueryModuleException
 	{
 
 		String sql ="SELECT CREATIONTIME,QUERY_COUNT,QUERY_STATUS,QUERY_ID,QUERY_EXECUTION_ID "+
 		   "FROM QUERY_EXECUTION_LOG qel, COUNT_QUERY_EXECUTION_LOG cqel "+
		   "WHERE qel.QUERY_EXECUTION_ID = cqel.COUNT_QUERY_EXECUTION_ID and cqel.USER_ID="+
		   sessionDataBean.getUserId() + " and qel.QUERY_TYPE='C' and qel.WORKFLOW_ID IS NULL order by qel.CREATIONTIME desc ";
 		List<List<String>> queryResultList = executeQuery(sql, sessionDataBean, false, false, null,
 				0, lastIndex);
 		return queryResultList;
 	}
 
 	/**
 	 * @param sessionDataBean =session related data
 	 * @param queryResultList =List of queries for which
 	 * RecentQueryBean list is to be populate
 	 * @return= RecentQueriesBean List
 	 * @throws DAOException
 	 */
 	private List<RecentQueriesBean> populateRecentQueryBean(SessionDataBean sessionDataBean,
 			List<List<String>> queryResultList) throws DAOException
 	{
 		List<RecentQueriesBean> recentQueriesBeanList = new ArrayList<RecentQueriesBean>();
 
 		for (List<String> parameterizedQuery : queryResultList)
 		{
 			RecentQueriesBean recentQueriesBean = new RecentQueriesBean();
 			recentQueriesBean.setQueryCreationDate(parameterizedQuery.get(0));//(0));
 			recentQueriesBean.setResultCount(Long.valueOf(parameterizedQuery.get(1)));//(100L);
 			recentQueriesBean.setStatus(parameterizedQuery.get(2));//2));
 
 			String title = retrieveQueryName(Long.valueOf(parameterizedQuery.get(3)),
 					sessionDataBean);
 			recentQueriesBean.setQueryTitle(title);
 			recentQueriesBean.setQueyExecutionId(Long.valueOf(parameterizedQuery.get(4)));//Long.valueOf(parameterizedQuery.get(4)));
 			recentQueriesBeanList.add(recentQueriesBean);
 		}
 		return recentQueriesBeanList;
 	}
 
 	/**
 	 * @param id=query id
 	 * @param sessionDataBean session specific data
 	 * @return
 	 * @throws DAOException
 	 */
 	private String retrieveQueryName(Long id, SessionDataBean sessionDataBean) throws DAOException
 	{
 
 		String sourceObjectName = AbstractQuery.class.getName();
 		AbstractDAO dao = DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 		dao.openSession(sessionDataBean);
 
 		String name = ((AbstractQuery) dao.retrieve(sourceObjectName, id)).getName();
 		dao.closeSession();
 
 		return name;
 	}
 
 	/**
 	 * Executes the query & returns the results specified by the offset values i.e. startIndex & noOfRecords.
 	 * @param query
 	 * @param sessionDataBean =session related data
 	 * @param isSecureExecute
 	 * @param hasConditionOnIdentifiedField
 	 * @param queryResultObjectDataMap
 	 * @param startIndex
 	 * @param noOfRecords
 	 * @return
 	 * @throws QueryModuleException
 	 */
 	public List<List<String>> executeQuery(String query, SessionDataBean sessionDataBean,
 			boolean isSecureExecute, boolean hasConditionOnIdentifiedField,
 			Map queryResultObjectDataMap, int startIndex, int noOfRecords)
 			throws QueryModuleException
 	{
 		QueryModuleException queryModExp;
 		List<List<String>> queryResultList = null;
 		JDBCDAO dao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 		try
 		{
 			dao.openSession(sessionDataBean);
 			PagenatedResultData pagiPagenatedResultData = dao.executeQuery(
 					query, sessionDataBean, false, false, null, startIndex, noOfRecords);
 			queryResultList = pagiPagenatedResultData.getResult();
 
 		}
 		catch (ClassNotFoundException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.CLASS_NOT_FOUND);
 			throw queryModExp;
 		}
 		catch (DAOException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.DAO_EXCEPTION);
 			throw queryModExp;
 		}
 		finally
 		{
 			try
 			{
 				dao.closeSession();
 			}
 			catch (DAOException e)
 			{
 				queryModExp = new QueryModuleException(e.getMessage(),
 						QueryModuleError.DAO_EXCEPTION);
 				throw queryModExp;
 			}
 		}
 		return queryResultList;
 	}
 }
