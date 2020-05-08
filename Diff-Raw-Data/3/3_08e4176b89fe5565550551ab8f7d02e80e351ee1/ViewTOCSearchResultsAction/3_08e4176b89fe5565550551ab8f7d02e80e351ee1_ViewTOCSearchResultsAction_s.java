 /**
  * 
  * $Id: ViewTOCSearchResultsAction.java,v 1.12 2009-06-11 17:42:53 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.11  2009/06/01 17:02:53  pandyas
  * getting ready for QA build
  *
  * Revision 1.10  2008/10/22 18:19:07  schroedn
  * Removed the criteria table data from the search results for TOC
  *
  * Revision 1.9  2008/07/28 17:20:47  pandyas
  * Modifed to prevent SQL inject - added HTTP Header
  * App scan performed on July 24, 2008
  *
  * Revision 1.8  2008/07/15 15:18:48  pandyas
  * minor change
  *
  * Revision 1.7  2008/07/15 15:18:26  pandyas
  * Modified to prevent SQL injection
  * Scan conducted on July 14 2008
  *
  * Revision 1.6  2008/05/27 14:58:04  pandyas
  * Removed debug statements
  *
  * Revision 1.5  2008/05/27 14:52:52  pandyas
  * Modified to prevent SQL injection
  * Cleaned parameter name before proceeding
  * Re: Apps Scan run 05/23/2008
  *
  * Revision 1.4  2008/05/21 19:06:53  pandyas
  * Modified TOC action to prevent SQL injection
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.3  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.service.impl.*;
 import gov.nih.nci.camod.util.NameValueList;
 import gov.nih.nci.camod.util.SafeHTMLUtil;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 public class ViewTOCSearchResultsAction extends BaseAction {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
     	
     	log.info("In ViewTOCSearchResultsAction.execute");
     	// get and clean header to prevent SQL injection
        	String sID = null;
         if (request.getHeader("X-Forwarded-For") != null){
         	sID = request.getHeader("X-Forwarded-For");
         	log.info("cleaned X-Forwarded-For: " + sID);
             sID = SafeHTMLUtil.clean(sID);
         }  
         
 		sID = request.getHeader("Referer");
     	
     	// prevents Referer Header injection
    	if ( sID != null && sID != "" && sID.contains("camod")) {}
    	if ( sID != null && sID != "" && sID.contains("emice")) {}
     	else {
     		return (mapping.findForward("failure"));
     	}
         
         // Clean all headers for security scan (careful about what chars you allow)
     	String headername = "";
     	for(Enumeration e = request.getHeaderNames(); e.hasMoreElements();){
     		headername = (String)e.nextElement();
     		log.info("ViewTOCSearchResultsAction headername: " + headername);
     		String cleanHeaders = SafeHTMLUtil.clean(headername);
     		log.debug("ViewTOCSearchResultsAction cleaned headername: " + headername);
     	}         
   
         String theForward = "next";
         
         try {
       
             // clean TOCQUERYKEY ahead of try loop - then loop checks if it is a valid choice - security scan code
             String theKey = (String) request.getParameter(Constants.Parameters.TOCQUERYKEY);
             if (theKey != null && theKey.length() > 0) {
             	theKey = SafeHTMLUtil.clean(theKey);
             log.info("ViewTOCSearchResultsAction cleaned theKey: " + theKey);
             }        	
             
         	//Remove any retained criteriatable values
     		request.getSession().setAttribute(Constants.CRITERIATABLE, "" );    		
     		request.getSession().setAttribute(Constants.NOSAVEOPTION, "false");
     		
             // This is meant to prevent SQL injection into the key value of the TOC queries
         	// Security scan failed so this is checked first and the query will not run unless clean
             if (theKey != null && theKey.length() > 0)
             { 
 	            NameValueList.generateTableOfContentsList();
 	            request.getSession().setAttribute(Constants.Dropdowns.SEARCHTOCDROP, NameValueList.getTableOfContentsList());
 	            if (!SafeHTMLUtil.isValidValue(theKey,Constants.Dropdowns.SEARCHTOCDROP,request))
 	            {
 	                // set theForward to failure - fail gracefully but do not let query continue
 	            	System.out.println("TOC: The key is not an accepted value - end query " + theKey);  
 	                theForward = "failure";
 	            } else {	            
 		            log.debug("theKey is a valid value - continue with querry: " + theKey); 
             
 		            // Handle external linkage
 		            if (request.getSession().getAttribute(Constants.TOCSearch.TOC_QUERY_RESULTS) == null) {
 		 
 		                // Get the TOC manager workflow
 		                TOCManager theTOCManager = new TOCManager(getServlet().getServletContext().getRealPath("/")
 		                        + Constants.TOCSearch.TOC_QUERY_FILE);
 		
 		                List theResults = theTOCManager.process();
 		                log.debug("TOC: " + theResults); 
 		                request.getSession().setAttribute(Constants.TOCSearch.TOC_QUERY_RESULTS, theResults);		                
 		            }
 
 		            List theGroupList = (List) request.getSession().getAttribute(Constants.TOCSearch.TOC_QUERY_RESULTS);
 		            log.debug("theGroupList: " + theGroupList); 
 
 		            for (int i = 0; i < theGroupList.size(); i++) {
 		                TOCQueryGroup theQueryGroup = (TOCQueryGroup) theGroupList.get(i);
 		                List theQueryList = theQueryGroup.getQueries();
 		                for (int j = 0; j < theQueryList.size(); j++) {
 		                    TOCQuery theQuery = (TOCQuery) theQueryList.get(j);              
 		
 		                    if (theQuery.getKey().equals(theKey)) {                   	
 		                        request.getSession().setAttribute(Constants.SEARCH_RESULTS, theQuery.getResults());
 		                        log.info("TOC theQuery.getResults(): " + theQuery.getResults());
 		                        break;
 		                    }
 		                }
 		            }
 	            }  // end of SafeHTMLUtil.isValidValue
           }   // end of theKey != null         
         } catch (Exception e) {
 
             theForward = "failure";
             log.debug("Caught an error running the canned query: ", e);
 
             // Encountered an error saving the model.
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, theMsg);
         }
         log.info("Exiting ViewTOCSearchResultsAction theForward: "+ theForward);
         return mapping.findForward(theForward);
     }
     
 
         
 }
