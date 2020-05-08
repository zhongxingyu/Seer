 
 package edu.wustl.query.action;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.wustl.cider.querymanager.CiderQueryPrivilege;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.query.actionForm.WorkflowForm;
 import edu.wustl.query.bizlogic.WorkflowBizLogic;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 
 /**
  *
  * @author ravindra_jain
  * @created November 26, 2008
  */
 public class WorkflowAction extends Action
 {
 
 	/**
 	 * This action is used for processing Work flow object
 	 */
 	/* (non-Javadoc)
 	 * @see org.apache.struts.action.Action#execute(org.apache.s
 	 * truts.action.ActionMapping, org.apache.struts.action.ActionForm,
 	 *  javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
     public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		HttpSession session = request.getSession();
 		if (request.getParameter(Constants.OPERATION) != null && (request.getParameter("id") != null || request.getAttribute(Constants.WORKFLOW_ID)!=null)
 				&& (!"".equals(request.getParameter(Constants.ID))))
 		{
 
 			request.setAttribute(Constants.OPERATION, edu.wustl.common.util.global.Constants.EDIT);
 			String id=request.getParameter("id");
 			if(id==null)
 			{
 
 				/*for the get patient data/get Count flow
 				the is no id set in request.getParameter so  retrieve
 				 form request.getAttribute
 				*/
 				id=(String) request.getAttribute(Constants.WORKFLOW_ID);
 			}
 			SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA);
 			//added for get Count ->>>latest project Id
 			Long latestProjectId =null;
 			if(request.getAttribute(Constants.EXECUTED_FOR_PROJECT)==null)
 			{
 				latestProjectId = getLatestProjectId(Long.valueOf(id),sessionDataBean.getUserId());
 			}
 			else
 			{
 				latestProjectId=(Long) request.getAttribute(Constants.EXECUTED_FOR_PROJECT);
 			}
			CiderQueryPrivilege privilege = new CiderQueryPrivilege();
 			Long userId = sessionDataBean.getUserId();
 			if(latestProjectId!=null && latestProjectId > 0)
 			{
				privilege.setHasSecurePrivilege(latestProjectId, userId);
				privilege.sethasLimitedPrivilege(latestProjectId, userId);
 			}
			session.removeAttribute(Constants.QUERY_PRIVILEGE);
			session.setAttribute(Constants.QUERY_PRIVILEGE,privilege);
 			setQueryExecutionid(form,sessionDataBean,Long.valueOf(id), latestProjectId);
 		}
 	   else
 		{
 			request.setAttribute(Constants.OPERATION, Constants.ADD);
 		}
 		if (request.getParameter(Constants.ID) != null && (!request.getParameter(Constants.ID).equals("")))
 		{
 			request.setAttribute(Constants.ID, request.getParameter(Constants.ID));
 		}
 		setProjectList(request);
 		//request.setAttribute(Constants.EXECUTED_FOR_PROJECT,2);
 		if(request.getAttribute(Constants.WORKFLOW_ID)!=null)
 		{
 			request.setAttribute(Constants.WORKFLOW_ID,  request.getAttribute(Constants.WORKFLOW_ID));
 		}
 		//request.setAttribute(Constants.CREATEDBY,  ((SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA)).getUserId());
 		return mapping.findForward(Constants.SUCCESS);
 	}
 
 	/**
 	 * Get the latest project Id
 	 * @param id The workflowId
 	 * @param userId The userId
 	 * @return The Latest projectId
 	 * @throws BizLogicException
 	 */
 	private Long getLatestProjectId(Long id, Long userId) throws BizLogicException
     {
         WorkflowBizLogic workflowBizLogic=new WorkflowBizLogic();
         return workflowBizLogic.getLatestProject(id, userId);
     }
 
     private void setProjectList(HttpServletRequest request) throws QueryModuleException
 	{
 		//Retrieve the Project list
 
 		SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 				Constants.SESSION_DATA);
 		AbstractQueryUIManager qUIManager = AbstractQueryUIManagerFactory
 				.getDefaultAbstractUIQueryManager();
 		List<NameValueBean> projectList = qUIManager.getObjects(sessionDataBean.getUserId());
 
 		if (projectList != null)
 		{
 			request.setAttribute(Constants.PROJECT_NAME_VALUE_BEAN, projectList);
 		}
 	}
 	private void setQueryExecutionid(ActionForm form,SessionDataBean sessionDataBean,Long id, Long projectId) throws  BizLogicException
 	{
 		WorkflowForm  workflowForm=(WorkflowForm)form;
 		WorkflowBizLogic workflowBizLogic=new WorkflowBizLogic();
 		List<Integer> queryexecutionId=workflowBizLogic.generateExecutionIdMap(id, sessionDataBean.getUserId(),projectId);
 
 		Integer[] queryExeId = new Integer[queryexecutionId.size()];
 		queryexecutionId.toArray(queryExeId);
 		workflowForm.setQueryExecId(queryExeId);
 		workflowForm.setExecutedForProject(projectId);
 
 	}
}
