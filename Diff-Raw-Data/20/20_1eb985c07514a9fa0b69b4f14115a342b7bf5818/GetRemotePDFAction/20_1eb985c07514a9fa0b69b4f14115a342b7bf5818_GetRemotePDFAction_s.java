 package gov.nih.nci.cagwas.web.action;
 
 import gov.nih.nci.cagwas.application.zip.ZipFindingsHelper;
 import gov.nih.nci.cagwas.web.action.RemoteContentHelper;
 import gov.nih.nci.caintegrator.application.util.SafeHTMLUtil;
 
 import java.io.FileInputStream;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  * The AboutSetupAction class is used to get the remote content for the about
  * page and then redirect to it.
  * <P>
  * @author mholck
  * @see org.apache.struts.action.Action
  */
 public class GetRemotePDFAction extends Action
 {
 	/**
 	 * execute is called when this action is posted to
 	 * <P>
 	 * @param mapping The ActionMapping for this action as configured in struts
 	 * @param form The ActionForm that posted to this action if any
 	 * @param request The HttpServletRequest for the current post
 	 * @param response The HttpServletResponse for the current post
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception
 	{
 		ActionForward forward = null;
 		String pdfFilename = request.getParameter("file");
 		if(pdfFilename != null){
 			pdfFilename =  SafeHTMLUtil.clean(pdfFilename);
 		}
 		// Load the properties files
 		Properties mailProperties = new Properties();
 		String fileName = System.getProperty(ZipFindingsHelper.getMailPropertiesFilename());
 		
 		FileInputStream in = new FileInputStream(fileName);
 		mailProperties.load(in);
 		
 		String remoteUrl = mailProperties.getProperty("remote.url");
 		String remoteContent = null;
 		RemoteContentHelper rcHelp = new RemoteContentHelper();
 		remoteContent = rcHelp.getRemoteContent(remoteUrl + "/" + pdfFilename);
 		
		if (remoteContent != null)
 		{
 			forward = new ActionForward();
 			forward.setPath(remoteUrl + "/" + pdfFilename);
 			forward.setRedirect(true);
 		}
 		else{
 			forward = mapping.findForward("ploneError");
 		}
 		return forward;
 	}
 
 }
