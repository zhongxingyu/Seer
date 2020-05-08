 package edu.wustl.query.action;
 
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.json.JSONObject;
 
 import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.query.QueryPrivilege;
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.query.bizlogic.WorkflowBizLogic;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Utility;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 
 public class ProjectChangeHandlerAction extends Action {
 
 	/* (non-Javadoc)
 	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
 	 * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest
 	 * , javax.servlet.http. HttpServletResponse)
 	 */
 	@Override
     public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		List<JSONObject> executionQueryResults = new ArrayList<JSONObject>();
 		Writer writer = response.getWriter();
 		  String workflowId=request.getParameter("workflowId");
 		  String projectId =request.getParameter(Constants.SELECTED_PROJECT);
 		  WorkflowBizLogic workflowBizLogic=new WorkflowBizLogic();
 		  Long userId=((SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA)).getUserId();
 		  Map<Long, Integer> map=workflowBizLogic.generateQueryExecIdMap(userId,
 				  Long.valueOf(workflowId),  Long.valueOf(projectId));
           AbstractQueryUIManager qUIManager = AbstractQueryUIManagerFactory
           .getDefaultAbstractUIQueryManager();
          QueryPrivilege privilege = qUIManager.getPrivilege(request);
           executionQueryResults=Utility.generateExecutionQueryResults(map,
		             workflowBizLogic,  qUIManager, privilege);
 
           response.setContentType(Constants.CONTENT_TYPE_TEXT);
           writer.write(new JSONObject().put("executionQueryResults",
                   executionQueryResults).toString());
 		return null;
 		
 	}
}
