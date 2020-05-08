 /**
  * @author dgeorge
  * 
  * $Id: RegisterUserAction.java,v 1.9 2007-09-12 19:36:40 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.8  2006/11/10 20:21:26  pandyas
  * Fixed defect#461 - App support does not receive emails with requests for new user accounts
  *
  * Revision 1.7  2006/08/17 18:08:49  pandyas
  * Defect# 410: Externalize properties files - Code changes to get properties
  *
  * Revision 1.6  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.5  2005/11/16 15:31:16  georgeda
  * Defect #41. Clean up of email functionality
  *
  * Revision 1.4  2005/11/03 20:04:06  georgeda
  * Added PI email
  *
  * Revision 1.3  2005/10/24 15:12:21  georgeda
  * Cleaned up user registration e-mail
  *
  * Revision 1.2  2005/10/24 13:28:17  georgeda
  * Cleanup changes
  *
  * Revision 1.1  2005/10/21 20:47:04  georgeda
  * Initial revision
  *
  *
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.service.impl.PersonManagerSingleton;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.util.SafeHTMLUtil;
 import gov.nih.nci.camod.webapp.form.UserSettingsForm;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 /**
  * 
  * Send an e-mail indicating that a user has requested a change to his/her
  * settings
  * 
  */
 public class RegisterUserAction extends BaseAction {
 
 	/**
 	 * Action used to populate the various admin lists for the curation process
 	 */
 	public ActionForward execute(ActionMapping inMapping, ActionForm inForm,
 			HttpServletRequest inRequest, HttpServletResponse inResponse)
 			throws Exception {
 
 		log.info("<RegisterUserAction> Entering execute");
 
 		String theForward = "next";
 		
 		
 
 		// The user didn't press the cancel button
 		if (isCancelled(inRequest)) {
 			theForward = "cancel";
 		} else {
 			UserSettingsForm theForm = (UserSettingsForm) inForm;
 			
 			// clean all the entered values to prevent nonsense injection of text by security scan
 			if(theForm.getAffiliation() != null & theForm.getAffiliation().length() >0){
 				log.info("theForm.getAffiliation(): " + theForm.getAffiliation());
 				String affilation = SafeHTMLUtil.clean(theForm.getAffiliation());
 				log.info("affilation: " + affilation);
 				theForm.setAffiliation(affilation);
 			}
 			if(theForm.getFirstName() != null & theForm.getFirstName().length() >0){
 				log.info("theForm.getFirstName(): " + theForm.getFirstName());
 				String firstName = SafeHTMLUtil.clean(theForm.getFirstName());
 				log.info("firstName: " + firstName);				
 				theForm.setFirstName(firstName);
 			}
 			if(theForm.getLastName() != null & theForm.getLastName().length() >0){
 				log.info("theForm.getLastName(): " + theForm.getLastName());				
 				String lastName = SafeHTMLUtil.clean(theForm.getLastName());
 				log.info("lastName: " + lastName);				
 				theForm.setLastName(lastName);
 			}
 			if(theForm.getPhone() != null & theForm.getPhone().length() >0){
 				log.info("theForm.getPhone(): " + theForm.getPhone());				
 				String phone = SafeHTMLUtil.clean(theForm.getPhone());
 				log.info("phone: " + phone);				
 				theForm.setPhone(phone);
 			}
 			if(theForm.getEmail() != null & theForm.getEmail().length() >0){
 				log.info("theForm.getEmail(): " + theForm.getEmail());				
 				String email = SafeHTMLUtil.clean(theForm.getEmail());
 				log.info("email: " + email);				
 				theForm.setEmail(email);
 			}
			if(theForm.getPiFirstName() != null & theForm.getPiUsername() == null){
 				log.info("theForm.getPiFirstName(): " + theForm.getPiFirstName());				
 				String piFirstName = SafeHTMLUtil.clean(theForm.getPiFirstName());
 				log.info("piFirstName: " + piFirstName);
 				theForm.setPiFirstName(piFirstName);
 			}
			if(theForm.getPiLastName() != null & theForm.getPiUsername() == null){
 				log.info("theForm.getPiLastName(): " + theForm.getPiLastName());				
 				String piLastName = SafeHTMLUtil.clean(theForm.getPiLastName());
 				log.info("piLastName: " + piLastName);				
 				theForm.setPiLastName(piLastName);
 			}
			if(theForm.getPiEmail() != null & theForm.getPiUsername() == null){
 				String piEmail = SafeHTMLUtil.clean(theForm.getPiEmail());
 				theForm.setPiEmail(piEmail);
 			}
			// Do not check if it matches DB since it just get sent via e-mail
			// Let systems figure out PI if any illegal characters are missing
			if(theForm.getPiUsername() != null & theForm.getPiUsername().length() >0){				
				String piUserName = SafeHTMLUtil.clean(theForm.getPiUsername());
	            theForm.setPiUsername(piUserName);
 			} 
 					
 	    	// get and clean header to prevent SQL injection
 	       	String sID = null;
 	        if (inRequest.getHeader("X-Forwarded-For") != null){
 	        	sID = inRequest.getHeader("X-Forwarded-For");
 	            log.info("cleaned X-Forwarded-For: " + sID);
 	            sID = SafeHTMLUtil.clean(sID);
 	        }
 	        
 	    	// get and clean header to prevent SQL injection
 	        if (inRequest.getHeader("Referer") != null){
 	        	sID = inRequest.getHeader("Referer");
 	            log.info("cleaned Referer: " + sID);
 	            sID = SafeHTMLUtil.clean(sID);
 	        } 
 	        
 	        // Clean all headers for security scan (careful about what chars you allow)
 	    	String headername = "";
 	    	for(Enumeration e = inRequest.getHeaderNames(); e.hasMoreElements();){
 	    		headername = (String)e.nextElement();
 	    		log.info("RegisterUserAction headername: " + headername);
 	    		String cleanHeaders = SafeHTMLUtil.clean(headername);
 	    		log.info("RegisterUserAction cleaned headername: " + headername);
 	    	} 	        
 			
 			try {
 				// Get from default bundle
 				Properties camodProperties = new Properties();
 				String camodPropertiesFileName = null;
 
 				camodPropertiesFileName = System
 						.getProperty("gov.nih.nci.camod.camodProperties");
 
 				try {
 					FileInputStream in = new FileInputStream(
 							camodPropertiesFileName);
 					camodProperties.load(in);
 
 				} catch (FileNotFoundException e) {
 					log.error("Caught exception finding file for properties: ",
 							e);
 					e.printStackTrace();
 				} catch (IOException e) {
 					log.error("Caught exception finding file for properties: ",
 							e);
 					e.printStackTrace();
 				}
 
 				String theUsersToNotify = camodProperties
 						.getProperty("user_settings.user_update_notify");
 
 				StringTokenizer theTokenizer = new StringTokenizer(
 						theUsersToNotify, ",");
 
 				List<String> theNotifyList = new ArrayList<String>();
 				while (theTokenizer.hasMoreElements()) {
 					theNotifyList.add(theTokenizer.nextToken());
 				}
 				String[] theRecipients = new String[theNotifyList.size()];
 				for (int i = 0; i < theNotifyList.size(); i++) {
 					theRecipients[i] = (String) theNotifyList.get(i);
 				}
 
 				// Build the message text
 				String theMailSubject = "caMOD: The following user is requesting a new account: "
 						+ theForm.getFirstName() + " " + theForm.getLastName();
 				String[] theMessageKeys = { "add_new_user" };
 
 				if (theRecipients.length > 0) {
 					// gather variable values to build the e-mail content with
 					Map<String, String> valuesForVariables = new TreeMap<String, String>();
 					valuesForVariables.put("firstName", theForm.getFirstName());
 					valuesForVariables.put("lastName", theForm.getLastName());
 					valuesForVariables.put("email", theForm.getEmail());
 					valuesForVariables.put("phone", theForm.getPhone());
 					valuesForVariables.put("affiliation", theForm
 							.getAffiliation());
 
 					// PI data
 					valuesForVariables.put("isPi", Boolean.toString(theForm
 							.isPrincipalInvestigator()));
 
 					if (theForm.isPrincipalInvestigator() == false) {
 						String thePiUsername = theForm.getPiUsername();
 						if (thePiUsername != null && thePiUsername.length() > 0) {
 							Person thePerson = PersonManagerSingleton
 									.instance().getByUsername(thePiUsername);
 							valuesForVariables.put("piName", thePerson
 									.getDisplayName());
 						} else {
 							valuesForVariables.put("piName", "");
 							valuesForVariables.put("newPiName", theForm
 									.getPiLastName()
 									+ ", " + theForm.getPiFirstName());
 							valuesForVariables.put("piEmail", theForm
 									.getPiEmail());
 						}
 					} else {
 						valuesForVariables.put("piName", "");
 						valuesForVariables.put("piName", "");
 						valuesForVariables.put("newPiName", "");
 					}
 
 					// launch the email
 					MailUtil.sendMail(theRecipients, theMailSubject, "",
 							theForm.getEmail(), theMessageKeys,
 							valuesForVariables);
 				} else {
 					theForward = "failure";
 					log.error("No e-mail address for user updates");
 				}
 			} catch (Exception e) {
 
 				theForward = "failure";
 				log.error("Unable to send e-mail for new user: ", e);
 
 				ActionMessages theMsg = new ActionMessages();
 				theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 						"errors.admin.message"));
 				saveErrors(inRequest, theMsg);
 			}
 		}
 		log.trace("Exiting execute");
 
 		return inMapping.findForward(theForward);
 	}
 }
