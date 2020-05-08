 /**
  * 
  * @author pandyas
  * 
 * $Id: ClinicalMarkerPopulateAction.java,v 1.6 2006-11-09 17:26:10 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.5  2006/04/20 19:19:36  pandyas
  * Added 'Other' to field and fixed save for other clinical marker
  *
  * Revision 1.4  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.3  2005/11/07 19:14:14  pandyas
  * modified for clinical marker screen
  *
  * Revision 1.2  2005/11/03 18:54:10  pandyas
  * Modified for histopathology screens
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.ClinicalMarker;
 import gov.nih.nci.camod.service.ClinicalMarkerManager;
 import gov.nih.nci.camod.webapp.form.ClinicalMarkerForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class ClinicalMarkerPopulateAction extends BaseAction {
 	
 	/** 
 	 * Pre-populate all field values in the form <FormName> 
 	 *  Used by <jspName>
 	 * 
 	 */ 
 	public ActionForward populate( ActionMapping mapping, 
 								   ActionForm form,
 						           HttpServletRequest request,
 						           HttpServletResponse response)
 	  throws Exception {
 		
 		//System.out.println( "<ClinicalMarkerPopulateAction populate> Entered" );
 		
 		ClinicalMarkerForm clinicalMarkerForm = (ClinicalMarkerForm) form;
 
        	// Grab the current aHistopathID from the session
        	String aHistopathologyID = request.getParameter("aHistopathologyID");
        	//System.out.println( "aHistopathologyID: " +aHistopathologyID );
     	
     	String aClinicalMarkerID = request.getParameter( "aClinicalMarkerID" );		
     	//System.out.println( "aClinicalMarkerID: = " +aClinicalMarkerID);  
     	
     	ClinicalMarkerManager theClinicalMarkerManager = (ClinicalMarkerManager) getBean( "clinicalMarkerManager" );
     	ClinicalMarker theClinicalMarker = theClinicalMarkerManager.get( aClinicalMarkerID ); 
     	
         if (theClinicalMarker == null) {
             request.setAttribute(Constants.Parameters.DELETED, "true");
         } else {
             request.setAttribute("aClinicalMarkerID", aClinicalMarkerID);
             
             // Set the other name and/or selected name from database
            if (theClinicalMarker.getNameUnctrlVocab() != null) {
                 clinicalMarkerForm.setName(Constants.Dropdowns.OTHER_OPTION);
                clinicalMarkerForm.setOtherName(theClinicalMarker.getNameUnctrlVocab());
             } else {
                 clinicalMarkerForm.setName(theClinicalMarker.getName());
             }            
         		
             if (theClinicalMarker.getValue() != null) {        		
         		clinicalMarkerForm.setValue(theClinicalMarker.getValue());
            }   	
         }
         
         // Prepopulate all dropdown fields, set the global Constants to the following
         this.dropdown(request, response); 
 
 		return mapping.findForward("submitClinicalMarkers");
 
 	}
 
 	/**
 	 * Populate the dropdown menus 
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward dropdown( ActionMapping mapping, 
 			   					   ActionForm form,
 			   					   HttpServletRequest request,
 			   					   HttpServletResponse response )
 	  throws Exception {
 		
 		//System.out.println( "<ClinicalMarkerPopulateAction dropdown> Entering ActionForward dropdown()" );
 		
 		//blank out the FORMDATA Constant field
 		ClinicalMarkerForm clinicalMarkerForm = (ClinicalMarkerForm) form;
 		
 		request.getSession().setAttribute( Constants.FORMDATA, clinicalMarkerForm );
 		
 		//setup dropdown menus
 		this.dropdown( request, response );
 		
 		//System.out.println( "<ClinicalMarkerPopulateAction dropdown> Exiting ActionForward dropdown()" );		
 		
 		return mapping.findForward( "submitClinicalMarkers" );
 	}	
 	
 	/**
 	 * Populate all drowpdowns for this type of form 
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 */
 	public void dropdown( HttpServletRequest request,
 						  HttpServletResponse response )
 	  throws Exception {
 		
 			//System.out.println( "<ClinicalMarkerPopulateAction dropdown> Entering void dropdown()" );
 		
 			//Prepopulate all dropdown fields, set the global Constants to the following
 			NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.CLINICALMARKERSDROP, Constants.Dropdowns.ADD_BLANK );
 			
 			//System.out.println( "<ClinicalMarkerPopulateAction dropdown> Exiting void dropdown()" );			
 	}	
 
 }
