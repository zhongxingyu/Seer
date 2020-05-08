 /**
  * 
  * $Id: DrugScreenSearchAction.java,v 1.13 2008-01-16 18:29:57 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.12  2007/10/31 18:42:56  pandyas
  * Fixed #8290 	Rename graft object into transplant object
  *
  * Revision 1.11  2007/09/12 19:36:40  pandyas
  * modified debug statements for build to stage tier
  *
  * Revision 1.10  2007/08/07 18:28:24  pandyas
  * Renamed to GRAFT as per VCDE comments
  *
  * Revision 1.9  2007/07/31 12:02:38  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.8  2006/04/17 19:09:41  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.7  2005/11/22 15:17:16  georgeda
  * Defect #21, use nsc number for clinical trials
  *
  * Revision 1.6  2005/11/17 20:57:17  georgeda
  * Defect #117, handle back arrow gracefully
  *
  * Revision 1.5  2005/11/15 22:13:46  georgeda
  * Cleanup of drug screening
  *
  * Revision 1.4  2005/11/11 21:24:19  georgeda
  * Defect #29.  Separate drug screening and animal model search results.
  *
  * Revision 1.3  2005/11/10 22:07:36  georgeda
  * Fixed part of bug #21
  *
  * Revision 1.2  2005/10/05 20:28:00  guruswas
  * implementation of drug screening search page
  *
  * Revision 1.1  2005/09/30 18:42:24  guruswas
  * intial implementation of drug screening search and display page
  *
  * Revision 1.2  2005/09/16 15:52:56  georgeda
  * Changes due to manager re-write
  *
  * 
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Agent;
 import gov.nih.nci.camod.service.AgentManager;
 import gov.nih.nci.camod.service.impl.QueryManagerSingleton;
 import gov.nih.nci.camod.util.SafeHTMLUtil;
 import gov.nih.nci.camod.webapp.form.DrugScreenSearchForm;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.common.persistence.hibernate.HQLParameter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.hibernate.Hibernate;
 
 /**
  * 
  * Action used to implement the search for animal models
  *
  */
 public final class DrugScreenSearchAction extends BaseAction {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws IOException, ServletException {
         DrugScreenSearchForm theForm = (DrugScreenSearchForm) form;
         log.debug("the form=" + theForm);
         
         //Clean the nsc number to prevent SQL injection
         String nscNumberEntered = theForm.getNSCNumber();
         if (nscNumberEntered != null && nscNumberEntered.length() > 0) {
         	String nscNumber = SafeHTMLUtil.clean(theForm.getNSCNumber());
         	theForm.setNSCNumber(nscNumber);
             log.info("DrugScreenSearchAction cleaned ndcNumber: " + nscNumber);
         }
         
         request.getSession().setAttribute(Constants.DRUG_SCREEN_OPTIONS, theForm);
 
 		// The following two objects are needed for eQBE.
         try {
            String name = null;        	
             // Clean all headers for security scan (careful about what chars you allow)
         	for(Enumeration e = request.getHeaderNames(); e.hasMoreElements();){
         		name = (String)e.nextElement();
         		log.debug("SimpleSearchPopulateAction headername: " + name);
         		String cleanHeaders = SafeHTMLUtil.clean(name);
        		log.info("SimpleSearchPopulateAction cleaned headername: " + name);
         	} 
         	
         	// get and clean header to prevent SQL injection
            	String sID = null;
             if (request.getHeader("X-Forwarded-For") != null){
             	sID = request.getHeader("X-Forwarded-For");
                 log.info("cleaned X-Forwarded-For: " + sID);
                 sID = SafeHTMLUtil.clean(sID);
             }
             
         	// get and clean header to prevent SQL injection
             if (request.getHeader("Referer") != null){
             	sID = request.getHeader("Referer");
                 log.info("cleaned Referer: " + sID);
                 sID = SafeHTMLUtil.clean(sID);
             }        	
         	
 			HQLParameter[] theParams = new HQLParameter[1];
 			theParams[0] = new HQLParameter();
 			theParams[0].setName("nscNumber");
 			theParams[0].setValue(new Long(theForm.getNSCNumber()));
 			theParams[0].setType(Hibernate.LONG);
 		
 			String theHQLQuery = "from Agent where nsc_number = :nscNumber";
 			log.debug("The HQL query: " + theHQLQuery);
 			List agents = Search.query(theHQLQuery, theParams);
             
             request.getSession().setAttribute(Constants.DRUG_SCREEN_SEARCH_RESULTS, agents);
 			final int agtCount = (agents!=null)?agents.size():0;
 			AgentManager myAgentManager = (AgentManager)getBean("agentManager");
 			final HashMap<Long, Collection> clinProtocols = new HashMap<Long, Collection>();
 			final HashMap<Long, Collection> yeastResults = new HashMap<Long, Collection>();
 			final HashMap<Long, Collection> invivoResults = new HashMap<Long, Collection>();
 			final HashMap<Long, Collection> preClinicalResults = new HashMap<Long, Collection>();
             String theAgentName = null;
             String theCasNumber = null;
             
             Long theNscNumber = new Long(theForm.getNSCNumber());
             
 			for(int i=0; i<agtCount; i++) {
 				Agent a = (Agent)agents.get(i);
 				if (a != null) {
                     theNscNumber = a.getNscNumber();
                    
                     // Get the name
                     if (theAgentName == null) {
                         theAgentName = a.getName();
                     }
                     
                     // Get the cas number
                     if (theCasNumber == null) {
                         theCasNumber = a.getCasNumber();
                     }
                     
 					if (theNscNumber != null) {
 						if (theForm.isDoClinical()) {
 							Collection protocols = myAgentManager.getClinicalProtocols(a);
 							clinProtocols.put(theNscNumber, protocols);
 						}
 						if (theForm.isDoPreClinical()) {
 							try {
 								List models = QueryManagerSingleton.instance().getModelsForThisCompound(theNscNumber);
 								preClinicalResults.put(a.getId(), models);
 							}catch (Exception x) {
 								x.printStackTrace();
 							}
 						}
 						if (theForm.isDoYeast()) {
 							// get the yeast data
 							List yeastStages = myAgentManager.getYeastResults(a, false);
 							yeastResults.put(a.getId(), yeastStages);
 						}
 						if (theForm.isDoInvivo()) {
 							// now get invivo/Transplant data
 							List transplantResults = QueryManagerSingleton.instance().getInvivoResults(a, false);
 							invivoResults.put(a.getId(), transplantResults);
 						}
 					}
 				}
 			}
 			request.getSession().setAttribute(Constants.CLINICAL_PROTOCOLS, clinProtocols);
 			request.getSession().setAttribute(Constants.YEAST_DATA, yeastResults);
 			request.getSession().setAttribute(Constants.INVIVO_DATA, invivoResults);
 			request.getSession().setAttribute(Constants.PRECLINICAL_MODELS, preClinicalResults);
             request.getSession().setAttribute("agentName", theAgentName);
             request.getSession().setAttribute("casNumber", theCasNumber);
             request.getSession().setAttribute("nscNumber", theNscNumber);
         } catch (Exception e) {
             log.error("Exception occurred in DrugScreenSearchAction.execute()", e);
             request.getSession().setAttribute(Constants.DRUG_SCREEN_SEARCH_RESULTS, new ArrayList<Object>());
             // Set the error message
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, msg);
         }
         log.trace("Exiting DrugScreenSearchAction.execute");
         // Forward control to the specified success URI
         return mapping.findForward("next");
     }
 }
