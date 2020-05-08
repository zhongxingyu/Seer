 package org.openmrs.module.heightweighttracker.extension.html;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.heightweighttracker.web.MetadataHelper;
 import org.openmrs.module.web.extension.LinkExt;
 
 
 public class HeightWeightTrackerFormItem extends LinkExt {
 	
 	
 	/**
 	 * @return the message code for this link
 	 */
 	@Override
 	public String getLabel() {
 		
 		MetadataHelper.setupConstants();   
 		
 		if(Context.getAdministrationService().getGlobalProperty("heightweighttracker.DisplayLink").equals("true"))
 		{
 			return "Height/Weight charts";
 		}
 		
		return null;
 	}
 	
 	/**
 	 * @return the url to go to
 	 */
 	@Override
 	public String getUrl() {
 		return "module/heightweighttracker/heightweighttracker.form";
 	}
 	
 	/**
      * Returns the required privilege in order to see this section.  Can be a 
      * comma delimited list of privileges.  
      * If the default empty string is returned, only an authenticated 
      * user is required
      * 
      * @return Privilege string
      */
 	@Override
 	public String getRequiredPrivilege() {
         return "View Patients";
     }
 	
 }
