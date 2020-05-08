 /* SVN FILE: $Id: CurrentAddressAction.java 6110 2013-02-01 04:44:33Z jal55 $ */
 package edu.psu.iam.cpr.ip.ui.action;
 
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Result;
 
 import edu.psu.iam.cpr.ip.ui.validation.FieldUtility;
 /**
  * CurrentAddressAction is responsible for acquiring the current address of  
  * the student.
  * 
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To 
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative 
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @package edu.psu.iam.cpr.ip.ui.action 
  * @author $Author: jal55 $
  * @version $Rev: 6110 $
  * @lastrevision $Date: 2013-01-31 23:44:33 -0500 (Thu, 31 Jan 2013) $
  */
 public class CurrentAddressAction extends AddressBaseAction 
 {
 	
 	private String alternateRadio;
 	
 	/* (non-Javadoc)
 	 * @see edu.psu.iam.cpr.ui.action.BaseAction#execute()
 	 */
 	@Override
 	@Action(value="current_address",results={ @Result(name=SUCCESS,location="/contact_info",type=REDIRECT),
             @Result(name="Welcome"       ,location="/welcome"         ,type=REDIRECT),
             @Result(name="DataAccuracy"  ,location="/data_accuracy"   ,type=REDIRECT),
             @Result(name="LegalName"     ,location="/legal_name"      ,type=REDIRECT),
             @Result(name="CurrentAddress",location="/current_address" ,type=REDIRECT),
             @Result(name="AlternateAddress",location="/alternate_address" ,type=REDIRECT),
             @Result(name="ContactInfo"   ,location="/contact_info"    ,type=REDIRECT),
             @Result(name="PersonalInfo"  ,location="/personal_info"   ,type=REDIRECT),
             @Result(name="IdentityInfo"  ,location="/identity_info"   ,type=REDIRECT),
             @Result(name="VerifyInfo"    ,location="/verify_info"     ,type=REDIRECT),
 			                                  @Result(name="stay on page",location="/jsp/currentAddress.jsp"),
 					                          @Result(name="verify",location="/verify_info",type=REDIRECT),
                                               @Result(name="failure",location="/jsp/endPage.jsp")
                                             })
 	public String execute() 
 	{
 		setPrefix("cra");
 		
 		String nextPage = super.execute();
 		
 		if ("true".equals(getAlternateRadio())) {
 			nextPage = "AlternateAddress";
 		}
 		
 		return nextPage;
 	}
 
 	@Override
 	/**
 	 * This routine will determine whether or not dependent fields [on this screen] have been entered
 	 * @return STAY_ON_PAGE if dependent fields are missing, else return null
 	 */
 	public String dependencyCheck() 
 	{
 		String returnLocation = null; 
 		
 		// Current address requires the following fields.  
 		if ("USA".equals(getCountry())) {
			if(FieldUtility.fieldIsNotPresent(getCountry(), getAddressLine1(), getCity(), getPostalCode())) {
 				returnLocation =  STAY_ON_PAGE;
 			}
 		}
 		else {
 			if(FieldUtility.fieldIsNotPresent(getCountry(), getAddressLine1(), getCity())) {
 				returnLocation =  STAY_ON_PAGE;
 			}
 		}
		
		
		// State or Province is required for Current Address
		if(FieldUtility.fieldIsNotPresent(getState()) && FieldUtility.fieldIsNotPresent(getProvince()))
		{
			returnLocation = STAY_ON_PAGE;
		}
		
 		return returnLocation;
 	}
 
 	@Override
 	/**
 	 * Current Address does not need to do anything for special country check, handling.
 	 * This method should only be overridden for address other than Current Address
 	 */
 	public void countryCheck(String returnLocation) 	{ }
 
 	/**
 	 * @param alternateRadio the alternateRadio to set
 	 */
 	public void setAlternateRadio(String alternateRadio) {
 		this.alternateRadio = alternateRadio;
 	}
 
 	/**
 	 * @return the alternateRadio
 	 */
 	public String getAlternateRadio() {
 		return alternateRadio;
 	}
 
 }
