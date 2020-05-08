 
 package gov.nih.nci.security.ri.struts.actions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  *  Action to prevent unathorized access to creating Projects. 
  * 
  *  @author Brian Husted
  *
  */
 public class ViewCreateProjectAction extends SecureAction  {
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.ri.struts.actions.SecureAction#executeSecureWorkflow(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward executeSecureWorkflow(ActionMapping mapping,
 			ActionForm form, HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
		// TODO Auto-generated method stub
 		return mapping.findForward( ACTION_SUCCESS );
 	}
 
 }
