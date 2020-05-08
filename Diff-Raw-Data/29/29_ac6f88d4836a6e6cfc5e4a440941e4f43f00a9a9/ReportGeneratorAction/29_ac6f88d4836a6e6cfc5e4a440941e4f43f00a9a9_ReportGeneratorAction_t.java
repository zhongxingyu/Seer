 /*
  * Created on Nov 19, 2004
  */
 package gov.nih.nci.nautilus.struts.action;
 
 import gov.nih.nci.nautilus.constants.NautilusConstants;
 import gov.nih.nci.nautilus.struts.form.ClinicalDataForm;
 import gov.nih.nci.nautilus.struts.form.ComparativeGenomicForm;
 import gov.nih.nci.nautilus.struts.form.GeneExpressionForm;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 
 public class ReportGeneratorAction extends DispatchAction {
 
	public String goback = "";
	
     public ActionForward compundReport(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		return mapping.findForward("generateReport");
 	}
 
 	public ActionForward previewReport(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
         Object queryCollection = request.getAttribute(NautilusConstants.QUERY_KEY);
 		if(form instanceof GeneExpressionForm) {
             
         }else if(form instanceof ClinicalDataForm) {
             
         }else if(form instanceof ComparativeGenomicForm) {
             
         }
         request.setAttribute(NautilusConstants.QUERY_KEY,queryCollection);
        //we've gotten this far, so we're validated. find failure to refresh page
        //and set att of the req to spawn report prev
        request.setAttribute("preview", new String("yes"));
        System.out.println("back: " + this.goback);
        return mapping.findForward(this.goback);
 //       return mapping.findForward("success");
	}
	
	public ActionForward previewReportGene(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
        this.goback = "backToGeneExp";
		return previewReport(mapping, form, request, response);
	}
	public ActionForward previewReportCGH(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		this.goback = "backToCGH";
		return previewReport(mapping, form, request, response);
	}
	public ActionForward previewReportClinical(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		this.goback = "backToClinical";
		return previewReport(mapping, form, request, response);
 	}
 }
