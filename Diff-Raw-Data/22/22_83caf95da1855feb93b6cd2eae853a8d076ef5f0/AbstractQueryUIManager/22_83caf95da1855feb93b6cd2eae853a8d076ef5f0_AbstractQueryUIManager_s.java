 package edu.wustl.query.util.querysuite;
 
 /**
  * This class is base for all QueryUIManager classes.
  */
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.beans.NodeInfo;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.query.AbstractQuery;
 import edu.wustl.query.queryexecutionmanager.DataQueryResultsBean;
 import edu.wustl.query.querymanager.Count;
 import edu.wustl.query.viewmanager.ViewType;
 
 public abstract class AbstractQueryUIManager {
 	
 	protected boolean isSavedQuery;
 	protected QueryDetails queryDetailsObj;
 
 	/**
 	 * This method extracts query object and forms results .
 	 * 
 	 * @param option
 	 * @return status
 	 */
 	public int searchQuery(String option) throws QueryModuleException {
 		int query_exec_id = 0;
 			if (queryDetailsObj.getSessionData() != null) {
 				query_exec_id = processQuery(queryDetailsObj.getSessionData());
 			}
 		return query_exec_id;
 	}
 
 	/**
 	 * This method Processes the Query by calling execute method of QueryManager.
 	 * 
 	 * @throws QueryModuleException
 	 */
 	protected abstract int processQuery(SessionDataBean sessionDataBean) throws QueryModuleException ;
 	
	public abstract Count getCount(int query_execution_id) throws QueryModuleException;
 	
 	/**
 	 * This method gets the required objects (in case of Cider objects will be Projects)
 	 * based on which query results can be filtered.
 	 * @param userId get Objects based on user id. 
 	 * @return collection of required objects.
 	 * @throws QueryModuleException
 	 */
 	abstract public List<NameValueBean> getObjects(Long userId) throws QueryModuleException;
 	
 	/**
 	 * This method updates the query object with default conditions
 	 * @throws QueryModuleException
 	 */
 	abstract public void updateQueryForValidation() throws QueryModuleException;
 	
 //	abstract public DataQueryResultsBean getData(int countQueryExecId,List<NodeInfo> upiList, ViewType viewType) throws QueryModuleException, DAOException, SQLException;
 	
 	abstract public DataQueryResultsBean getData(int countQueryExecId, ViewType viewType) throws QueryModuleException;
 	
 	abstract public DataQueryResultsBean getData(int countQueryExecId, String data, ViewType viewType) throws QueryModuleException;
 	/**
	 * This method checks if query returns too few records or not.
	 * @param projectId based on which the results are filtered
 	 * @param countObject having status,result count
	 * @return hasTooFewRecords
 	 * @throws QueryModuleException
 	 */
	abstract public boolean checkTooFewRecords(Count countObject, boolean hasSecurePrivilege) throws QueryModuleException;
 	
 	abstract public AbstractQuery getAbstractQuery();
 	
 	abstract public void setAbstractQuery(AbstractQuery abstractQuery);
 	
	abstract public boolean hasSecurePrivilege(HttpServletRequest request);
}
