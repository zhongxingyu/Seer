 /* SVN FILE: $Id: PreviousAddressAction.java 6110 2013-02-01 04:44:33Z jal55 $ */
 package edu.psu.iam.cpr.ip.ui.action;
 
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Result;
 
 import edu.psu.iam.cpr.ip.ui.validation.FieldUtility;
 
 /**
  * PreviousAddressAction validates and processes previous address information  
  * entered by users/clients.
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
 public class AlternateAddressAction extends AddressBaseAction 
 {
 	/* (non-Javadoc)
 	 * @see edu.psu.iam.cpr.ui.action.BaseAction#execute()
 	 */
 	@Override
	@Action(value="previous_address",results={ 
 			@Result(name=SUCCESS,location="/contact_info"             ,type=REDIRECT),
             @Result(name="Welcome"       ,location="/welcome"         ,type=REDIRECT),
             @Result(name="DataAccuracy"  ,location="/data_accuracy"   ,type=REDIRECT),
             @Result(name="LegalName"     ,location="/legal_name"      ,type=REDIRECT),
             @Result(name="CurrentAddress",location="/current_address" ,type=REDIRECT),
             @Result(name="ContactInfo"   ,location="/contact_info"    ,type=REDIRECT),
             @Result(name="PersonalInfo"  ,location="/personal_info"   ,type=REDIRECT),
             @Result(name="IdentityInfo"  ,location="/identity_info"   ,type=REDIRECT),
             @Result(name="VerifyInfo"    ,location="/verify_info"     ,type=REDIRECT),
 			@Result(name="stay on page"  ,location="/jsp/alternateAddress.jsp"),
 			@Result(name="verify"        ,location="/verify_info"     ,type=REDIRECT),
             @Result(name="failure"       ,location="/jsp/endPage.jsp")
                                             })
 	public String execute() 
 	{
 		setPrefix("alt");
 		return super.execute();
 	}
 
 	@Override
 	/**
 	 * This routine will determine whether or not dependent fields [on this screen] have been entered
 	 * @return STAY_ON_PAGE if dependent fields are missing, else return null
 	 */
 	public String dependencyCheck() 
 	{
 		// There are no dependent fields for 'PreviousAddressAction', so send back a null
 		return null;
 	}
 	
 	/**
 	 * Some screens, such as 'PreviousAddressAction' may need to blank-out the country
 	 * if all of the other fields are blank.  
 	 * 
 	 * This keeps us from saving information for a non-required screen, when none of the data fields have been entered.
 	 * 
 	 * This is not the case with CurrentAddress, which is required and does have required fields
 	 * 
 	 * @param returnLocation A 'String' object representing 'success' or 'null'.  
 	 * @return
 	 */
 	public void countryCheck(String returnLocation)
 	{
 		// If the only field present is the dropdown for country, then blank it out
 		if(FieldUtility.fieldIsPresent(getCountry()) && returnLocation.equalsIgnoreCase(SUCCESS) 
 		&& FieldUtility.fieldIsNotPresent(getAddressLine1(), getAddressLine2(), getAddressLine3(), getCity(), getState(), getPostalCode(), getProvince()))
 		{
 			setCountry(null);
 		}
 	}
 }
