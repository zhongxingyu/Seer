
 package edu.wustl.query.bizlogic;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import edu.wustl.cider.query.CiderQuery;
 import edu.wustl.cider.query.CiderWorkFlowDetails;
 import edu.wustl.cider.querymanager.CiderQueryPrivilege;
 import edu.wustl.cider.util.global.CiderConstants;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.dao.AbstractDAO;
 import edu.wustl.common.dao.DAO;
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.query.AbstractQuery;
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.common.query.factory.ITableManagerFactory;
 import edu.wustl.common.query.itablemanager.ITableManager;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.exceptions.SqlException;
 import edu.wustl.common.querysuite.queryobject.IAbstractQuery;
 import edu.wustl.common.querysuite.queryobject.ICompositeQuery;
 import edu.wustl.common.querysuite.queryobject.IParameterizedQuery;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.impl.ParameterizedQuery;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.domain.Workflow;
 import edu.wustl.query.domain.WorkflowDetails;
 import edu.wustl.query.domain.WorkflowItem;
 import edu.wustl.query.querymanager.Count;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 import edu.wustl.query.workflowexecutor.WorkflowManager;
 
 /**
  * @author vijay_pande
  * BizLogic class to insert/update WorkFlow Object
  */
 public class WorkflowBizLogic extends DefaultBizLogic
 {
 
 	private static org.apache.log4j.Logger logger = Logger.getLogger(WorkflowBizLogic.class);
 
 	private final WorkflowManager workflowManager = new WorkflowManager();
 
 
 	/**
 	 * Inserts domain object
 	 * @param obj The object to be inserted.
 	 * @param dao the dao object
 	 * @param sessionDataBean session specific data
 	 * @throws DAOException
 	 */
 	@Override
 	protected void insert(Object obj, DAO dao, SessionDataBean sessionDataBean) throws DAOException
 	{
 		Workflow workflow = (Workflow) obj;
 		workflow.setCreatedBy(sessionDataBean.getUserId());
 
 		logger.info("In  WORKFLOW  BIZ LOGIC >>>>>> INSERT METHOD");
 		logger.info("#### Workflow Name #### ::  " + workflow.getName());
 		try
 		{
 			for (WorkflowItem workflowItem : workflow.getWorkflowItemList())
 			{
 				IAbstractQuery query = workflowItem.getQuery();
 				if (query.getId() == null)
 				{
 					saveCompositeQuery(dao, sessionDataBean, (ICompositeQuery) query);
 				}
 			}
 			dao.insert(workflow, sessionDataBean, false, false);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			throw new DAOException("Could not insert Workflow:" + e.getMessage() + e);
 		}
 	}
 
 	/**
 	 * Method to save compositeQuery object
 	 * @param dao Object of DAO
 	 * @param sessionDataBean object of SessionDataBean
 	 * @param query object of ICompositeQuery to be saved
 	 * @throws UserNotAuthorizedException User not authorized exception
 	 * @throws DAOException DAO exception
 	 */
 	private void saveCompositeQuery(DAO dao, SessionDataBean sessionDataBean, ICompositeQuery query)
 			throws UserNotAuthorizedException, DAOException
 	{
 		if (query.getOperation().getOperandOne().getId() == null)
 		{
 			saveCompositeQuery(dao, sessionDataBean, (ICompositeQuery) query.getOperation()
 					.getOperandOne());
 		}
 		if (query.getOperation().getOperandTwo().getId() == null)
 		{
 			saveCompositeQuery(dao, sessionDataBean, (ICompositeQuery) query.getOperation()
 					.getOperandTwo());
 		}
 		dao.insert(query, sessionDataBean, false, true);
 	}
 
 	/**
 	 * Updates  domain object
 	 * @param dao the dao object
 	 * @param obj The object to be updated into the database.
 	 * @param oldObj old object that is to be updated
 	 * @param sessionDataBean session specific data
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 */
 	@Override
 	protected void update(DAO dao, Object obj, Object oldObj, SessionDataBean sessionDataBean)
 			throws DAOException, UserNotAuthorizedException
 	{
 		Workflow workflow = (Workflow) obj;
 		workflow.setCreatedBy(sessionDataBean.getUserId());
 		for (WorkflowItem workflowItem : workflow.getWorkflowItemList())
 		{
 			IAbstractQuery query = workflowItem.getQuery();
 			if (query.getId() == null)
 			{
 				saveCompositeQuery(dao, sessionDataBean, (ICompositeQuery) query);
 			}
 		}
 		dao.update(obj, null, false, false, false);
 	}
 
     /**
      * @param workflowDetails
      * @param request
      * @return
      * @throws BizLogicException
      */
     public Map<Long, Integer> runWorkflow(WorkflowDetails workflowDetails, HttpServletRequest request)//(Long queryId, HttpServletRequest request)
             throws BizLogicException
     {
         WorkflowManager wfm = new WorkflowManager();
         Map<Long, Integer> execIdMap = null;
         try
         {
             // generate the
             execIdMap = wfm.execute(workflowDetails);
         } catch (QueryModuleException ex)
         {
             BizLogicException bizLogicException = new BizLogicException(ex.getMessage(), ex);
             throw bizLogicException;
         } catch (MultipleRootsException ex)
         {
             BizLogicException bizLogicException = new BizLogicException(ex.getMessage(), ex);
             throw bizLogicException;
         } catch (SqlException ex)
         {
             BizLogicException bizLogicException = new BizLogicException(ex.getMessage(), ex);
             throw bizLogicException;
         }
 
         return execIdMap;
     }
 
 
 	/**
 	 * @param workflowId
 	 * @param queryId=id of query for which counts will be returned
 	 * @return count value
 	 * @throws DAOException
 	 * @throws QueryModuleException
 	 * @throws SQLException
 	 */
 	public Map<Long, Integer> executeGetCountQuery(WorkflowDetails workflowDetails, Long queryId, HttpServletRequest request)//(Long queryId, HttpServletRequest request)
 			throws BizLogicException
 	{
 
 
 		//map of query names and execution ids
 //		Map<String, Integer> executionIdsMap = new HashMap<String, Integer>();
 //		executionIdsMap.put("q1", 101);
 //		executionIdsMap.put("q2", 102);
 //		executionIdsMap.put("[Query:q1]Union[Query:q2]", 83);
 //		return executionIdsMap;
 
 
 
 		//map of query names and execution ids
 		Map<Long, Integer> executionIdsMap = new HashMap<Long, Integer>();
 
 		//TO DO
 		/*
 		 * set the IQuery from the query ID
 		 */
 		edu.wustl.common.querysuite.queryobject.impl.AbstractQuery query = null;
 		try
 		{
 			if (queryId != null)
 			{
 				AbstractDAO dao = DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 				dao.openSession(null);
 				try
 				{
 				    // Get the query
 				    query = (edu.wustl.common.querysuite.queryobject.impl.AbstractQuery) dao.retrieve(edu.wustl.common.querysuite.queryobject.impl.AbstractQuery.class.getName(), Long.valueOf(queryId));
 					if (query == null)
                     {
                         throw new BizLogicException(
                                 "No query exists with the Id - " + queryId
                                         + " !");
                     }
 				}
 				finally
 				{
 					dao.closeSession();
 				}
 			}
 
 			AbstractQueryUIManager qUIManager = null;
 
 			if (query instanceof IParameterizedQuery)
 			{
                 int queryExecId;
                 qUIManager = AbstractQueryUIManagerFactory
                         .configureDefaultAbstractUIQueryManager(
                                 this.getClass(), request, (IQuery) query);
                 queryExecId = qUIManager.searchQuery(null);
                 executionIdsMap.put(queryId, queryExecId);
             }
 			else if (query instanceof ICompositeQuery)
 			{
 //		        ICompositeQuery queryClone = new DyExtnObjectCloner().clone((ICompositeQuery)query);
 //		        new HibernateCleanser(queryClone).clean();
 			    Long projectIdVal = (long) ((CiderWorkFlowDetails) workflowDetails).getProjectId();
 			    if (projectIdVal <= 0)
                 {
                     projectIdVal = null;
                 }
 
 			    long userId = ((CiderWorkFlowDetails) workflowDetails).getUserId();
 			    Long workflowId = workflowDetails.getWorkflow().getId();
 
 			    CiderQueryPrivilege privilege = null;
 		        HttpSession session = request.getSession();
 				if(session.getAttribute(CiderConstants.CIDER_QUERY_PRIVILEGE)!=null)
 				{
 					privilege =(CiderQueryPrivilege)session.getAttribute(CiderConstants.CIDER_QUERY_PRIVILEGE);
 				}
 				else
 				{
 					privilege = new CiderQueryPrivilege(true,false);
 				}
 		        AbstractQuery ciderQuery = new CiderQuery(query, 0, null,
 		                userId,
                         projectIdVal,
                         request.getRemoteAddr(),
                         workflowId,privilege
                         );
 		        // get the latest execution ids for this workflow first'
 //                Map<Long, Integer> preExecIdMap = new HashMap<Long, Integer>();
 		        Map<Long, Integer> preExecIdMap = generateQueryExecIdMap(userId, workflowId, projectIdVal);
 		        preExecIdMap.remove(query.getId());
 		        executionIdsMap.putAll(workflowManager.execute(workflowDetails, ciderQuery, preExecIdMap));
 
 			}
 		}
 		catch (DAOException ex)
 		{
 			BizLogicException bizLogicException = new BizLogicException(ex.getMessage(), ex);
 			throw bizLogicException;
 		}
 
 		catch (QueryModuleException e)
 		{
 			BizLogicException bizLogicException = new BizLogicException(e.getMessage(), e);
 			throw bizLogicException;
 		} catch (MultipleRootsException e)
         {
             BizLogicException bizLogicException = new BizLogicException(e.getMessage(), e);
             throw bizLogicException;
         } catch (SqlException e)
         {
             BizLogicException bizLogicException = new BizLogicException(e.getMessage(), e);
             throw bizLogicException;
         }
 
 		return executionIdsMap;
 
 
 	}
 
 	/**
 	 * @param queryId
 	 *            id of query for which counts will be returned
 	 * @param request
 	 *            The Request Object.
 	 * @return The Map containing the key as <code>queryId</code> and the value
 	 *         as <code>QueryExecutionId</code>.
 	 *         <ul>
 	 *         <li>In case of a parameterized query the map will contain a
 	 *         single entry corresponding to the concerned query.
 	 *         <li>In case of a Composite query the map will contain the entries
 	 *         corresponding to itself as well as all the child queries (PQs +
 	 *         CQs)
 	 *         </ul>
 	 * @throws BizLogicException
 	 */
 	public Map<Integer, Integer> executeGetCountQuery(Long queryId, HttpServletRequest request)
 			throws BizLogicException
 	{
 		//map of query names and execution ids
 		Map<Integer, Integer> executionIdsMap = new HashMap<Integer, Integer>();
 
 		//TO DO
 		/*
 		 * set the IQuery from the query ID
 		 */
 		IQuery query = null;
 		try
 		{
 			if (queryId != null)
 			{
 				AbstractDAO dao = DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 				dao.openSession(null);
 				query = (IParameterizedQuery) dao.retrieve(ParameterizedQuery.class.getName(), Long
 						.valueOf(queryId));
 				dao.closeSession();
 			}
 			AbstractQueryUIManager qUIManager = null;
 
 
 			int queryExecId;
 
 			qUIManager = AbstractQueryUIManagerFactory.configureDefaultAbstractUIQueryManager(this
 					.getClass(), request, query);
 			queryExecId = qUIManager.searchQuery(null);
 
 //			return queryExecId;
 
 		}
 		catch (DAOException ex)
 		{
 			BizLogicException bizLogicException = new BizLogicException(ex.getMessage(), ex);
 			throw bizLogicException;
 		}
 
 		catch (QueryModuleException e)
 		{
 			BizLogicException bizLogicException = new BizLogicException(e.getMessage(), e);
 			throw bizLogicException;
 		}
 
 		return executionIdsMap;
 
 	}
 
 
 
 	/**
 	 * This function returns the count for the
 	 * given executionID
 	 * @param queryExecId
 	 * @return
 	 * @throws QueryModuleException
 	 */
	public Count getCount(int queryExecId) throws QueryModuleException
 	{
		Count count = workflowManager.getCount(queryExecId);
 //		Count count = new Count();
 //		count.setCount(new Date().getSeconds());
 //		count.setQuery_exection_id(queryExecId);
 //		count.setStatus("Completed");
 
 		return count;
 
 	}
 
 	/**
 	 * Overriding the parent class's method to validate the enumerated attribute values
 	 */
 	@Override
 	protected boolean validate(Object obj, DAO dao, String operation) throws DAOException
 	{
 		if (obj == null)
 		{
 			throw new DAOException("NULL object passed for validation");
 		}
 		Workflow workflow = (Workflow) obj;
 
 		//validate empty workflow
 		if (workflow.getName().equals(""))
 		{
 			throw new DAOException("Workflow Name cannot be empty");
 		}
 
 
 
 		Session session = null;
 		try {
 			session = DBUtil.getCleanSession();
 
 			Query query = null;
 			String wfName = getWorkflowName(workflow.getName());
 			query = session.createQuery("select id from " + Workflow.class.getName() + "  Workflow where upper(Workflow.name) = "
 					+ "'"+wfName.toUpperCase()+"'");
 
 
 			List list = query.list();
 			//edit
 			if (workflow.getId() != null && !list.isEmpty())
 			{
 				if (workflow.getId().equals(list.get(0))) {
 					return true;
 				} else
 				{
 					throw new DAOException("Workflow with same name already exists");
 				}
 			}
 			else if (!list.isEmpty())
 			{
 				throw new DAOException("Workflow with same name already exists");
 			}
 		} catch (BizLogicException e) {
 			e.printStackTrace();
 		}
 		finally
 		{
 			session.close();;
 
 		}
 		return true;
 
 	}
 
 	/**
 	 * This method returns the name of
 	 * workflow to validate it.
 	 * @param workflowName= name of workflow
 	 * @return workflow name
 	 */
 	private String getWorkflowName(String workflowName)
 	{
 		if(workflowName.contains("'"))
 		{
 			// replace "'" with "''"
 			return workflowName.replace("'","''");
 		}
 		return workflowName;
 	}
 
 	/**
 	 * This method adds the given query to workflow.
 	 * Workflow to which query is to be added is identified
 	 * by workflow id
 	 * @param workflowId =workflow id
 	 * @param query=query to be added in workflow
 	 * @param sessionDataBean session related data
 	 * @throws DAOException
 	 * @throws UserNotAuthorizedException
 	 * @throws BizLogicException
 	 */
 	public void addWorkflowItem(Long workflowId, IQuery query, SessionDataBean sessionDataBean)
 	throws DAOException, UserNotAuthorizedException, BizLogicException
 	{
 	DefaultBizLogic defaultBizLogic = new DefaultBizLogic();
 	Workflow workflow = (Workflow) defaultBizLogic.retrieve(Workflow.class.getName(),
 	workflowId);
 	if(isQueryAlreadyExists(workflow.getWorkflowItemList(), query.getId()))
 	{
 	WorkflowItem workflowItem = new WorkflowItem();
 	workflowItem.setQuery(query);
 	List workflowItemList = workflow.getWorkflowItemList();
 	workflowItemList.add(workflowItem);
 	workflow.setWorkflowItemList(workflowItemList);
 	defaultBizLogic.update(workflow, null, Constants.HIBERNATE_DAO, sessionDataBean);
 	}
 	}
 
 	
 	public boolean isQueryAlreadyExists(List<WorkflowItem> workflowItemList, Long queryId)
 	{
 		if(workflowItemList!=null&&queryId!=null)
 		{
 			Iterator<WorkflowItem> workflowItemIter=workflowItemList.iterator();
 			while (workflowItemIter.hasNext()) {
 				if(workflowItemIter.next().getQuery().getId().equals(queryId))
 				{
 					return false;
 				}
 				
 			}
 		}
 		return true;
 	}
 
 	
 	
 	
 	/**
      * This method returns the map of query execution ids
      *
      * @param workflowId
      * @return
      */
     public List<Integer> generateExecutionIdMap(Long workflowId, Long userId)
             throws BizLogicException
     {
         return generateExecutionIdMap(workflowId, userId, null);
     }
 
     /**
      * This method returns the List of query execution ids
      *
      * @param workflowId
      * @return
      */
     public List<Integer> generateExecutionIdMap(Long workflowId, Long userId,
             Long projectId) throws BizLogicException
     {
         DefaultBizLogic defaultBizLogic = new DefaultBizLogic();
         Workflow workflow;
         try {
             workflow = (Workflow) defaultBizLogic.retrieve(Workflow.class.getName(),
                     workflowId);
 
             Map<Long, Integer> execIdMap = generateQueryExecIdMap(userId, workflow.getId(), projectId);
 
 
             List<Integer> queryExecIdsList=new ArrayList<Integer>(workflow.getWorkflowItemList().size());
 
             // For each workflow item list fetch the latest execution count.
             List<WorkflowItem> workflowItemList = workflow.getWorkflowItemList();
             for (WorkflowItem workflowItem : workflowItemList)
             {
                 // Get the query object
                 Long queryId = workflowItem.getQuery().getId();
                 Integer queryExecId = execIdMap.get(queryId);
                 if (queryExecId == null)
                 {
                     queryExecId = 0;
                 }
                 queryExecIdsList.add(queryExecId);
             }
             return queryExecIdsList;
         } catch (DAOException ex) {
             BizLogicException bizLogicException = new BizLogicException(ex.getMessage(), ex);
             throw bizLogicException;
         }
     }
 
     /**
      * @param userId
      * @param workflowId
      * @param projectId
      * @return
      * @throws BizLogicException
      */
     public Map<Long, Integer> generateQueryExecIdMap(Long userId,
 			Long workflowId, Long projectId) throws BizLogicException
 	{
 		// Get the map of execution Ids from DB
 		ITableManager itableManager = ITableManagerFactory
 				.getDefaultITableManager();
 		try {
 			Map<Long, Integer> execIdMap = itableManager
 					.getLatestExecutionCountId(null, userId, workflowId,
 							projectId);
 			return execIdMap;
 		} catch (DAOException ex) {
 			BizLogicException bizLogicException = new BizLogicException(ex
 					.getMessage(), ex);
 			throw bizLogicException;
 
 		} catch (SQLException ex) {
 			BizLogicException bizLogicException = new BizLogicException(ex
 					.getMessage(), ex);
 			throw bizLogicException;
 		}
 
 	}
 
     /**
      * This method returns the latest project that was accessed on the workflow
      * page.
      *
      * @param workflowId
      *            The Workflow Id.
      * @param userId
      *            The Current Logged In User Id
      * @return The Project Id that was last accessed
      * @throws BizLogicException
      *             if there is an error while fetching the projectId
      */
     public Long getLatestProject(Long workflowId, Long userId)
             throws BizLogicException
     {
         // Get the map of execution Ids from DB
         ITableManager itableManager = ITableManagerFactory
                 .getDefaultITableManager();
 
         try {
             Long projectId = itableManager
                     .getLatestProjectId(workflowId,userId);
             return projectId;
         } catch (DAOException ex) {
             BizLogicException bizLogicException = new BizLogicException(ex
                     .getMessage(), ex);
             throw bizLogicException;
 
         } catch (SQLException ex) {
             BizLogicException bizLogicException = new BizLogicException(ex
                     .getMessage(), ex);
             throw bizLogicException;
         }
     }
}
