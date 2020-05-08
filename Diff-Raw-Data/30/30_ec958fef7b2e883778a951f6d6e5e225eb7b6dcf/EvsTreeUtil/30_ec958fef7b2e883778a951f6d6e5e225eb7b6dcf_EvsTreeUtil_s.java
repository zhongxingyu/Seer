 /**
  *  @author georgeda 
  *  
 *  $Id: EvsTreeUtil.java,v 1.9 2007-08-27 15:38:08 pandyas Exp $  
  *  
  *  $Log: not supported by cvs2svn $
  *  Revision 1.8  2007/08/23 16:11:50  pandyas
  *  Removed extra code
  *
  *  Revision 1.7  2007/08/14 17:05:02  pandyas
  *  Bug #8414:  getEVSPreferredDiscription needs to be implemented for Zebrafish vocabulary source
  *
  *  Revision 1.6  2007/08/14 12:03:59  pandyas
  *  Implementing EVSPreferredName for Zebrafish models
  *
  *  Revision 1.5  2006/08/17 17:59:34  pandyas
  *  Defect# 410: Externalize properties files - Code changes to get properties
  *
  *  Revision 1.4  2006/04/21 13:42:12  georgeda
  *  Cleanup
  *
  *  Revision 1.3  2005/11/03 21:47:56  georgeda
  *  Changed EVS api
  *
  *  Revision 1.2  2005/09/22 13:04:31  georgeda
  *  Added app server call
  *
  *  Revision 1.1  2005/09/21 20:34:59  georgeda
  *  Create util for fetching/caching EVS data
  *
  *  
  */
 package gov.nih.nci.camod.util;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.evs.query.EVSQuery;
 import gov.nih.nci.evs.query.EVSQueryImpl;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Static helper class for caching EVS values.
  * 
  */
 public class EvsTreeUtil
 {
     static private final Log log = LogFactory.getLog(EvsTreeUtil.class);
     static private Map<String, String> ourDescriptions = new HashMap<String, String>();
 
     private EvsTreeUtil()
     {}
 
     /**
      * Get a preferred name based on a concept code. Will return the cached
      * value if it has been fetched before.
      * 
      * @param inConceptCode
      *            the concept code to get the preferred name for.
      * 
      * @return the preferred name, or an empty string if something goes wrong.
      */
     public static synchronized String getEVSPreferedDescription(String inConceptCode)
     {
        log.debug("Entering getEVSPreferedDescription");
 
         String theDescription = "";
         String EVSTreeNameSpace = "";
         String DisplayNameTag = "";
 
         if (ourDescriptions.containsKey(inConceptCode))
         {
             theDescription = (String) ourDescriptions.get(inConceptCode);
         }
         else
         {
             try
             {
                log.debug("inConceptCode: " + inConceptCode);                
                 
             	// Define parameters for Zebrafish namespace
                 // Maybe a better way to do this, but I didn't want to send in HttpServletRequest everywhere
             	if(inConceptCode.contains("ZFA:")){
                    log.debug("Zebrafish modelSpecies");
 
             		EVSTreeNameSpace = Constants.Evs.ZEBRAFISH_NAMESPACE;
             		DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG_LOWER_CASE;
             	//Define parameters for all NCI_Thesaurus namespace	
             	} else {
                    log.debug("NOT Zebrafish modelSpecies");                    
             		EVSTreeNameSpace = Constants.Evs.NAMESPACE;
             		DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG;
             	}
             	
            	log.debug("EVSTreeNameSpace: " + EVSTreeNameSpace);
       	
             	
                 ApplicationService theAppService = getApplicationService();
 
                 EVSQuery theConceptNameQuery = new EVSQueryImpl();
                 theConceptNameQuery.getConceptNameByCode(EVSTreeNameSpace, inConceptCode);
 
                 List theConceptNames = (List) theAppService.evsSearch(theConceptNameQuery);
 
                 // Should only be one
                 if (theConceptNames.size() > 0)
                 {
                     String theDisplayName = (String) theConceptNames.get(0);
 
                     EVSQuery theDisplayNameQuery = new EVSQueryImpl();
                     //theDisplayNameQuery.getPropertyValues(Constants.Evs.NAMESPACE, theDisplayName, Constants.Evs.DISPLAY_NAME_TAG);
                     theDisplayNameQuery.getPropertyValues(EVSTreeNameSpace, theDisplayName, DisplayNameTag);
 
                     // Should only be one
                     List theDisplayNameList = (List) theAppService.evsSearch(theDisplayNameQuery);
                    log.debug("theDisplayNameList.size: " + theDisplayNameList.size());
 
                     if (theDisplayNameList.size() > 0)
                     {
                         theDescription = (String) theDisplayNameList.get(0);
                        log.debug("theDescription: " + theDescription);                        
 
                         // Cache for next time
                         ourDescriptions.put(inConceptCode, theDescription);
                     }
                 }
             }
             catch (Exception e)
             {
                 e.printStackTrace();
                 log.error("Exception getting preferred description: ", e);
             }
         }
        log.trace("Exiting getEVSPreferedDescription");
 
         return theDescription;
     }
     
 
 
     /**
      * Get the application service based on the properties file
      * 
      * @return the preferred name, or an empty string if something goes wrong.
      */
     public static ApplicationService getApplicationService()
     {
         // Get the app service uri
 		Properties camodProperties = new Properties();
 		String camodPropertiesFileName = null;
 
 		camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
 		
 		try {
 			
 		FileInputStream in = new FileInputStream(camodPropertiesFileName);
 		camodProperties.load(in);
 	
 		} 
 		catch (FileNotFoundException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();			
 		} catch (IOException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();			
 		}
         return ApplicationService.getRemoteInstance(camodProperties.getProperty("evs.uri"));
     }
 }
