 /* SVN FILE: $Id: VerifyInfoAction.java 6123 2013-02-05 15:02:06Z jal55 $ */
 package edu.psu.iam.cpr.ip.ui.action;
 
 import java.util.Map;
 
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Result;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 import edu.psu.iam.cpr.ip.ui.soap.SoapClientIP;
 import edu.psu.iam.cpr.ip.ui.validation.FieldUtility;
 import edu.psu.iam.cpr.ip.ui.helper.RegexHelper;
 import edu.psu.iam.cpr.ip.util.MapHelper;
 import edu.psu.iam.cpr.ip.ui.common.MagicNumber;
 import edu.psu.iam.cpr.ip.ui.common.UIConstants;
 
 import java.util.HashMap;
 
 /**
  * VerifyInfoAction - Confirmation/Verification of user-entered data prior to database update.
  * 
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To 
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative 
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @package edu.psu.iam.cpr.ip.ui.action 
  * @author $Author: jal55 $
  * @version $Rev: 6123 $
  * @lastrevision $Date: 2013-02-05 10:02:06 -0500 (Tue, 05 Feb 2013) $
  */
 public class VerifyInfoAction extends BaseAction 
 {
 	/* Key in map for return code from findPerson */
 	static final String STATUS_CODE      = "statusCode";
 	
 	/* Find Person service return codes */
 	public static final int PERSON_FOUND            = 0;                 
 	public static final int PERSON_NOT_FOUND        = 1;                  
 	public static final int PERSON_DATA_ENTRY_ERROR = 2;                 
 	public static final int PERSON_FATAL_ERROR      = 3;                 
 	
 	public static final String DATA_ENTRY_ERRORS1   = "There are data entry error(s).";
 	public static final String DATA_ENTRY_ERRORS2	  = "Please review/verify the data entered";
 	
 	public static final String FATAL_ERROR1_KEY     = "ui.error.unable.to.proceed";  
 	public static final String CALL_CUST_SERVICES   = "ui.error.call.cust.services"; 
 	
 	private String hasConfirmed = "false";
 	
 	/* (non-Javadoc)
 	 * @see edu.psu.iam.cpr.ui.action.BaseAction#execute()
 	 */
 	@Override
 //	@Action(value="verify_info",results={ @Result(name=SUCCESS,location="/policy_info",type=REDIRECT),
 	@Action(value="verify_info",results={ @Result(name=SUCCESS,location="/security_questions",type=REDIRECT),
 			                              @Result(name="success_security_questions",location="/security_questions",type=REDIRECT),
                                           @Result(name="stay on page",location="/jsp/verify.jsp"),
                                           @Result(name="failure",location="/jsp/endPage.jsp")
                                          })
 	public String execute() 
 	{
 		if(!setup("vfy"))
 		{
 			return FAILURE;
 		}
 		
 		log.debug(String.format("%s ", getUniqueId()))  ;
 		
 		// Ensure that 'edits' return to verify if necessary
 		getSessionMap().put(getPrefix() +".return.to.verify", "true");    
 		
 		/* 'success' will go to Policy, by default unless the individual is found by 'findPerson' */
 		String returnLocation = "success";                     
 		
 		if(FieldUtility.fieldIsNotPresent(getBtnsubmit()))
 		{
 			returnLocation =  STAY_ON_PAGE;
 		}
 		
 		// Save form data to session
 		saveFieldsToSession(getPrefix());
 		
 		if(returnLocation.equals(ActionSupport.SUCCESS))
 		{
 			getSessionMap().remove(getPrefix() +".return.to.verify");
 		}
 		
 		/* If 'findPerson' finds the individual, then go to 'security questions', else [by default] go to policy */
 		if(returnLocation.equalsIgnoreCase(SUCCESS))
 		{
 			Map<String, String> map = SoapClientIP.findPerson(getSessionMap(), getUniqueId());
 			log.debug(String.format("%s check the returned data --> %s", getUniqueId(), map));
 			switch(Integer.parseInt(map.get(STATUS_CODE)))
 			{
 				case PERSON_FOUND:               getSessionMap().put(getPrefix() +".person.found", "yes");
 				                                 log.info(String.format("%s the person was FOUND by add person", getUniqueId()));
 				                                 getSessionMap().put("suc.personId", map.get("srv.personId"));  
 				      				             getSessionMap().put("suc.psuId"   , map.get("srv.psuId"));     
 				      				             getSessionMap().put("suc.userId"  , map.get("srv.userId"));    
 				      				             getSessionMap().putAll(map);                                   
 				      				             getSessionMap().put("sec.password.setting", "reset");          
 					                             returnLocation = "success_security_questions"; 
 					                             break;  
 				
 				case PERSON_NOT_FOUND:           getSessionMap().put(getPrefix() +".person.found", "no");
 				                                 getSessionMap().put("sec.password.setting", "initial");        
 				                                 log.info(String.format("%s the person was not FOUND by add person", getUniqueId()));
 				                                 HashMap<String, String> argStringMap = MapHelper.genericObjToStringHashMap(getSessionMap());
 				                                 Map<String, String> status = SoapClientIP.addPerson(argStringMap, getUniqueId());
 				                                 log.info(String.format("%s returnStatus from addPerson--> %s ", getUniqueId(), status));
 
 				                                 log.info(String.format("%s statuscode from addPerson[%s] ", getUniqueId(), Integer.parseInt(status.get("statusCode"))));
                         					 switch(Integer.parseInt(status.get("statusCode")))
                         					 {
                            						/* 0: Send these id(s) to the success page */
                            						case 0:
                                         					getSessionMap().put("suc.personId", status.get("srv.personId"));
                                     						getSessionMap().put("suc.psuId"   , status.get("srv.psuId"));
                                     						getSessionMap().put("suc.userId"  , status.get("srv.userId"));
                                     						getSessionMap().put("formatted.university.id"
                                                               					, RegexHelper.formatUniversityId(getApplicationMap(), status.get("srv.psuId")));
                                     						//Navigation.lock(getSessionMap());
                                     						log.info("Address line 1 alt = " + argStringMap.get(UIConstants.ALT_ADDRESS_LINE1));
                                     						log.info("City alt  = " + argStringMap.get(UIConstants.ALT_CITY));
                                     						if (! SoapClientIP.mapValueIsEmpty(argStringMap.get(UIConstants.ALT_ADDRESS_LINE1))) {
                                     							log.info("We are making the call!");
                                     							SoapClientIP.addAlternateAddress(argStringMap);
                                     						}
                                             				break;
 
                         						/* We think we found you */
                            						case MagicNumber.I3:
                                         					setEndPageHeader(getApplicationString("ui.near.match.header"));
                                             					addActionMessage(getApplicationString("ui.near.match.message1"));
                                             					addActionMessage(getApplicationString("ui.near.match.message2"));
                                             					addReferenceNumber();
                                             					returnLocation = FAILURE;
                                             					break;
                         					    }
 					                            break;                                                 
 
 				case PERSON_DATA_ENTRY_ERROR:    returnLocation = STAY_ON_PAGE;                         
 				                                 addActionMessage(getApplicationString(DATA_ENTRY_ERRORS1));
 				                                 addActionMessage(map.get("statusMsg"));
 				                                 
 				                         		 // Ensure that 'edits' to fix 'error-conditions' return back to Verify page
 				                         		 getSessionMap().put(getPrefix() +".return.to.verify", "true");
 				                                 break;
 
 				case PERSON_FATAL_ERROR:         
 				default:                        setEndPageHeader("Please Contact Customer Services");
 					                            addActionMessage(getApplicationString(FATAL_ERROR1_KEY));
 				                                addActionMessage(getApplicationString(CALL_CUST_SERVICES));
 				                                addReferenceNumber();
 					                            returnLocation = FAILURE; 
 												break;
 			}
 		}
 		
 		return returnLocation;
 	}
 
 	/**
 	 * @return the hasConfirmed
 	 */
 	public String getHasConfirmed() {
 		return hasConfirmed;
 	}
 
 	/**
 	 * @param hasConfirmed the hasConfirmed to set
 	 */
 	public void setHasConfirmed(String hasConfirmed) {
 		this.hasConfirmed = hasConfirmed;
 	}
 
 }
