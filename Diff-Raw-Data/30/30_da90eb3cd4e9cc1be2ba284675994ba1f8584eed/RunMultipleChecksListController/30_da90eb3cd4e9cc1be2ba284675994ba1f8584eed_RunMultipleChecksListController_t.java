 package org.openmrs.module.dataintegrity.web.controller;
 
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.dataintegrity.DataIntegrityCheckResultTemplate;
 import org.openmrs.module.dataintegrity.DataIntegrityCheckTemplate;
 import org.openmrs.module.dataintegrity.DataIntegrityService;
 import org.openmrs.web.WebConstants;
 import org.springframework.context.support.MessageSourceAccessor;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 import org.springframework.web.servlet.view.RedirectView;
 
 public class RunMultipleChecksListController extends SimpleFormController {
 	private DataIntegrityService getDataIntegrityService() {
         return (DataIntegrityService)Context.getService(DataIntegrityService.class);
     }
 	
 	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
         List<DataIntegrityCheckTemplate> checks = new ArrayList<DataIntegrityCheckTemplate>();
         if (Context.isAuthenticated()) {
         	checks = getDataIntegrityService().getAllDataIntegrityCheckTemplates(); 
         }
         return checks;
     }
 	
 	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
             BindException errors) throws Exception {
 		HttpSession httpSession = request.getSession();
 		String view = getFormView();
 		if (Context.isAuthenticated()) {
 			
 			MessageSourceAccessor msa = getMessageSourceAccessor();
 			
 			String success = "";
 			String error = "";
 			String checkName = "";
 			
 			DataIntegrityService service = getDataIntegrityService();
 			
 			String[] checkList = request.getParameterValues("integrityCheckId");
 			if (checkList != null) {
 				int successCount = 0;
 				int errorCount = 0;
 				List<DataIntegrityCheckResultTemplate> results = new ArrayList<DataIntegrityCheckResultTemplate>();
				StringBuffer buffer = new StringBuffer();
 				for (String checkId : checkList) {
 					try {
 						int id = Integer.valueOf(checkId);
 						DataIntegrityCheckTemplate template = service.getDataIntegrityCheckTemplate(id);
 						checkName = template.getIntegrityCheckName();
 						DataIntegrityCheckResultTemplate resultTemplate = service.runIntegrityCheck(template, null);
 						results.add(resultTemplate);
 						successCount++;
 					} catch (Exception e) {
						errorCount++;Writer writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter(writer);
						e.printStackTrace(printWriter);
						buffer.append("\r\n" + writer.toString());
 						view = "runMultipleChecks.list";
 					}
 				}
 				httpSession.setAttribute("multipleCheckResults", results);
 				view = getSuccessView();
 				success = msa.getMessage("dataintegrity.runMultipleChecks.totalCount") + " " + (successCount + errorCount) + "<br/>";
 				success += msa.getMessage("dataintegrity.runMultipleChecks.successCount") + " " + successCount+ "<br/>";
				error = errorCount > 0 ? (msa.getMessage("dataintegrity.runMultipleChecks.errorCount") + " " + errorCount + "<br/>") : "";
				error += "Stack Trace: "  + buffer.toString();
 			} else { 
 				error = msa.getMessage("dataintegrity.runMultipleChecks.error");
 				view = "runMultipleChecks.list";
 			}
 		
 			if (!success.equals(""))
 				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
 			if (!error.equals(""))
 				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
 		}
 		
 		return new ModelAndView(new RedirectView(view));
 	}
 }
