 /**
  * 
  * $Id: NewDropdownUtil.java,v 1.61 2009-05-28 18:49:31 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.60  2009/03/13 15:02:04  pandyas
  * modified for #19758  	Remove the filter from the PI drop-down list on admin (Edit Models) search criteria screen
  *
  * Revision 1.59  2008/08/27 13:56:42  pandyas
  * Updated comments for method
  *
  * Revision 1.58  2008/08/14 17:09:40  pandyas
  * remove debug lines
  *
  * Revision 1.57  2008/08/12 19:40:31  pandyas
  * Fixed #15053  	Search for models with transgenic or targeted modification on advanced search page confusing
  *
  * Revision 1.56  2008/05/21 19:07:43  pandyas
  * Modified advanced search to prevent SQL injection
  * Converted text entry to dropdown lists for easier validation
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.55  2007/10/18 18:28:04  pandyas
  * Modified to prevent cross--site scripting attacks
  *
  * Revision 1.54  2007/10/17 18:36:55  pandyas
  * Trying to set constanst for PI list to use in validation of searchForm
  *
  * Revision 1.53  2007/08/27 15:41:03  pandyas
  * hide debug code printout
  *
  * Revision 1.52  2007/08/07 20:02:45  pandyas
  * removed blank in editor and screener admin list until validation is worked out
  *
  * Revision 1.51  2007/07/31 12:01:20  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.50  2007/05/21 17:37:04  pandyas
  * Modified simple and adv search species drop down to pull from DB (approved model species only)
  *
  * Revision 1.49  2007/03/28 18:03:09  pandyas
  * Modified for the following Test Track items:
  * #462 - Customized search for carcinogens for Jackson Lab data
  * #494 - Advanced search for Carcinogens for Jackson Lab data
  *
  * Revision 1.48  2006/11/09 17:15:56  pandyas
  * Commented out debug
  *
  * Revision 1.47  2006/10/17 16:10:31  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.46  2006/05/24 18:54:37  georgeda
  * Added staining method
  *
  * Revision 1.45  2006/05/24 16:53:09  pandyas
  * Converted StainingMethod to lookup - modified code to pull dropdown list from DB
  * All changes from earlier version were merged into this version manually
  *
  * Revision 1.44  2006/05/23 18:16:38  georgeda
  * Removed hardcode of other into species dropdown
  *
  * Revision 1.43  2006/05/19 16:41:54  pandyas
  * Defect #249 - add other to species on the Xenograft screen
  *
  * Revision 1.42  2006/05/15 15:45:40  georgeda
  * Cleaned up contact info management
  *
  * Revision 1.41  2006/05/10 14:16:14  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.40  2006/04/17 19:08:38  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.39  2005/11/29 20:47:21  georgeda
  * Removed debug
  *
  * Revision 1.38  2005/11/16 21:36:40  georgeda
  * Defect #47, Clean up EF querying
  *
  * Revision 1.37  2005/11/16 19:26:30  pandyas
  * added javadocs
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.util;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.*;
 import gov.nih.nci.camod.service.impl.CurationManagerImpl;
 import gov.nih.nci.camod.service.impl.QueryManagerSingleton;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.camod.service.SpeciesManager;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 public class NewDropdownUtil
 {
 
     private static final Log log = LogFactory.getLog(NewDropdownUtil.class);
 
     private static Map ourFileBasedLists = new HashMap();
 
     public static void populateDropdown(HttpServletRequest inRequest,
                                         String inDropdownKey,
                                         String inFilter) throws Exception
     {
 
         log.debug("Entering NewDropdownUtil.populateDropdown");
 
         log.debug("Generating a dropdown for the following key: " + inDropdownKey);
 
         List theList = null;
         if (inDropdownKey.indexOf(".txt") != -1)
         {
             theList = getTextFileDropdown(inRequest, inDropdownKey);
         }
         else if (inDropdownKey.indexOf(".db") != -1)
         {
             theList = getDatabaseDropdown(inRequest, inDropdownKey, inFilter);
         }
 
         // Add a blank as the first line
         if (Constants.Dropdowns.ADD_BLANK.equals(inFilter))
         {
             addBlank(theList);
         }
 
         // Add a blank as the first line
         else if (Constants.Dropdowns.ADD_BLANK_OPTION.equals(inFilter))
         {
             addBlankOption(theList);
         }
 
         // Add a blank as the first line
         else if (Constants.Dropdowns.ADD_OTHER.equals(inFilter))
         {
             addOther(theList);
         }
 
         // Add a blank as the first line
         else if (Constants.Dropdowns.ADD_OTHER_OPTION.equals(inFilter))
         {
             addOtherOption(theList);
         }
 
         else if (Constants.Dropdowns.ADD_BLANK_AND_OTHER.equals(inFilter))
         {
             addOther(theList);
             addBlank(theList);
         }
 
         else if (Constants.Dropdowns.ADD_BLANK_AND_OTHER_OPTION.equals(inFilter))
         {
             addOtherOption(theList);
             addBlankOption(theList);
         }
 
         if (theList == null)
         {
             throw new IllegalArgumentException("Unknown dropdown list key: " + inDropdownKey);
         }
 
         inRequest.getSession().setAttribute(inDropdownKey, theList);
 
         log.trace("Exiting NewDropdownUtil.populateDropdown");
     }
 
     private static List getDatabaseDropdown(HttpServletRequest inRequest,
                                             String inDropdownKey,
                                             String inFilter) throws Exception
     {
 
         log.debug("Entering NewDropdownUtil.getDatabaseDropdown");
 
         List theReturnList = null;
 
         //modified for species from DB
         if (inDropdownKey.equals(Constants.Dropdowns.SPECIESQUERYDROP))
         {
             theReturnList = getQueryOnlySpeciesList(inRequest, inFilter);
         }
         
         else if (inDropdownKey.equals(Constants.Dropdowns.NONHUMANSPECIESDROP))
         {
             theReturnList = getQueryNonHumanSpeciesList(inRequest, inFilter);
         }        
 
         else if (inDropdownKey.equals(Constants.Dropdowns.STRAINDROP))
         {
             theReturnList = getStrainsList(inRequest, inFilter);
         }
 
         else if (inDropdownKey.equals(Constants.Dropdowns.VIRALVECTORDROP))
         {
             theReturnList = getViralVectorList(inRequest);
         }
 
         else if (inDropdownKey.equals(Constants.Dropdowns.EXPRESSIONLEVELDROP))
         {
             theReturnList = getExpressionLevelList(inRequest);
         }   
 
         // Dropdown lists for submission pages of Carciogenic Intervention section
         else if (inDropdownKey.equals(Constants.Dropdowns.ANTIBODYDROP))
         {
             theReturnList = getEnvironmentalFactorList("Antibody");
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.BACTERIADROP))
         {
             theReturnList = getEnvironmentalFactorList("Bacteria");
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.CHEMICALDRUGDROP))
         {
             theReturnList = getEnvironmentalFactorList("Chemical / Drug");
         }
         else if (inDropdownKey.equals(Constants.Dropdowns.ENVIRONFACTORDROP))
         {
             theReturnList = getEnvironmentalFactorList("Environment");
         }         
         else if (inDropdownKey.equals(Constants.Dropdowns.SURGERYDROP))
         {
             theReturnList = getEnvironmentalFactorList("Other");
         }
         else if (inDropdownKey.equals(Constants.Dropdowns.GROWTHFACTORDROP))
         {
             theReturnList = getEnvironmentalFactorList("Growth Factor");
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.HORMONEDROP))
         {
             theReturnList = getEnvironmentalFactorList("Hormone");
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.NUTRITIONFACTORDROP))
         {
             theReturnList = getEnvironmentalFactorList("Nutrition");
         }
         else if (inDropdownKey.equals(Constants.Dropdowns.PLASMIDDROP))
         {
             theReturnList = getEnvironmentalFactorList("Plasmid");
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.RADIATIONDROP))
         {
             theReturnList = getEnvironmentalFactorList("Radiation");
         }
         else if (inDropdownKey.equals(Constants.Dropdowns.SIGNALINGMOLECULEDROP))
         {
             theReturnList = getEnvironmentalFactorList("Signaling Molecule");
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.TRANSPOSONDROP))
         {
             theReturnList = getEnvironmentalFactorList("Transposon");
         }
         else if (inDropdownKey.equals(Constants.Dropdowns.VIRUSDROP))
         {
             theReturnList = getEnvironmentalFactorList("Viral");
         }
         
         // Dropdown list for main advanced search CI section
         else if (inDropdownKey.equals(Constants.Dropdowns.CARCINOGENICAGENTSQUERYDROP))
         {
             theReturnList = getCarcinogenicAgentList(inRequest);
         }
         // Dropdown list for Ef names after agent type is selected by user
         else if (inDropdownKey.equals(Constants.Dropdowns.ENVIRONMENTALFACTORNAMESDROP))
         {
             theReturnList = getQueryOnlyEnvironmentalFactorList(inRequest, inFilter);
         }  
         // Dropdown list for Targeted Modification names
         else if (inDropdownKey.equals(Constants.Dropdowns.TARGETEDMODNAMEQUERYDROP))
         {
             theReturnList = getGeneNameList(inRequest);
         } 
         // Dropdown list for Trangene names
         else if (inDropdownKey.equals(Constants.Dropdowns.TRANSGENENAMEQUERYDROP))
         {
             theReturnList = getTransgeneNameList(inRequest);
         }         
         
         // Dropdown list for Cell Line names 
         else if (inDropdownKey.equals(Constants.Dropdowns.CELLLINENAMEQUERYDROP))
         {
             theReturnList = getCellLineNameList(inRequest);
         } 
         
         // Dropdown list for Therapeutic Approaches Drug / Compound names 
         else if (inDropdownKey.equals(Constants.Dropdowns.THERAPEUTICAPPROACHDRUGQUERYDROP))
         {
             theReturnList = getTherapeuticDrugNameList(inRequest);
         }         
         // Dropdown list for Genomic Segment Designator 
         else if (inDropdownKey.equals(Constants.Dropdowns.CLONEDESIGNATORQUERYDROP))
         {
             theReturnList = getGenomicSegmentDesignatorList(inRequest);
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.PRINCIPALINVESTIGATORDROP))
         {
             theReturnList = getPrincipalInvestigatorList(inRequest, inFilter);
         }
         
         else if (inDropdownKey.equals(Constants.Dropdowns.APPROVEDSPECIESDROP))
         {
             theReturnList = getApprovedSpeciesList(inRequest, inFilter);
         }        
 
         else if (inDropdownKey.equals(Constants.Dropdowns.PRINCIPALINVESTIGATORQUERYDROP))
         {
             theReturnList = getQueryOnlyPrincipalInvestigatorList(inRequest, inFilter);
         }
         
         else if (inDropdownKey.equals(Constants.Dropdowns.PRINCIPALINVESTIGATORQUERYALLDROP))
         {
             theReturnList = getQueryAllPrincipalInvestigatorList(inRequest, inFilter);
         }        
 
         else if (inDropdownKey.equals(Constants.Dropdowns.INDUCEDMUTATIONAGENTQUERYDROP))
         {
             theReturnList = getQueryOnlyInducedMutationAgentList(inRequest, inFilter);
         }
 
         else if (inDropdownKey.equals(Constants.Dropdowns.USERSDROP))
         {
             theReturnList = getUsersList(inRequest, inFilter);
         }
         else if (inDropdownKey.equals(Constants.Dropdowns.CURATIONSTATESDROP))
         {
             theReturnList = getCurationStatesList(inRequest, inFilter);
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.CURATIONSTATESWITHBLANKDROP))
         {
             theReturnList = getCurationStatesWithBlankList(inRequest, inFilter);
         }  
         else if (inDropdownKey.equals(Constants.Dropdowns.USERSFORROLEDROP))
         {
             theReturnList = getUsersForRoleList(inRequest, inFilter);
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.USERSFOREDITORROLEDROP))
         {
         	theReturnList = getUsersForSpecificRoleList(inRequest, inFilter);
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.USERSFORSCREENERROLEDROP))
         {
         	theReturnList = getUsersForSpecificRoleList(inRequest, inFilter);
         }  
         else if (inDropdownKey.equals(Constants.Dropdowns.ROLESDROP))
         {
             theReturnList = getRolesList(inRequest);
         }
         else if (inDropdownKey.equals(Constants.Dropdowns.STAININGDROP)) 
         {
             theReturnList = getStainingMethod(inRequest);
         }        
         else if (inDropdownKey.equals(Constants.Dropdowns.EXTERNALSOURCEQUERYDROP))
         {
             theReturnList = getExternalSourceList(inRequest);
         } 
         else if (inDropdownKey.equals(Constants.Dropdowns.USERNAMEINDATABASE))
         {
             theReturnList = getUsernameList(inRequest);
         }        
         else
         {
             log.error("No matching dropdown for key: " + inDropdownKey);
             theReturnList = new ArrayList<Object>();
         }
 
         log.debug("Exiting NewDropdownUtil.getDatabaseDropdown");
         return theReturnList;
     }
 
     // Get the context so we can get to our managers
     private static WebApplicationContext getContext(HttpServletRequest inRequest)
     {
         return WebApplicationContextUtils.getRequiredWebApplicationContext(inRequest.getSession().getServletContext());
     }
 
     // Get a text file dropdown
     private static synchronized List getTextFileDropdown(HttpServletRequest inRequest,
                                                          String inDropdownKey) throws Exception
     {
         log.trace("Entering NewDropdownUtil.getTextFileDropdown");
 
         List theReturnList = new ArrayList<Object>();
 
         if (ourFileBasedLists.containsKey(inDropdownKey))
         {
             log.debug("Dropdown already cached");
             List theCachedList = (List) ourFileBasedLists.get(inDropdownKey);
             theReturnList.addAll(theCachedList);
         }
         else
         {
             String theFilename = inRequest.getSession().getServletContext().getRealPath("/config/dropdowns") + "/" + inDropdownKey;
 
             List theList = readListFromFile(theFilename);
 
             // Built a list. Add to static hash
             if (theList.size() != 0)
             {
                 log.debug("Caching new dropdown: " + theList);
                 ourFileBasedLists.put(inDropdownKey, theList);
                 theReturnList.addAll(theList);
             }
         }
 
         log.trace("Exiting NewDropdownUtil.getTextFileDropdown");
         return theReturnList;
     }
 
     // Read from a file
     static private List readListFromFile(String inFilename) throws Exception
     {
         List theReturnList = new ArrayList<Object>();
 
         log.debug("Filename to read dropdown from: " + inFilename);
 
         BufferedReader in = new BufferedReader(new FileReader(inFilename));
 
         boolean isDropdownOption = false;
 
         String str;
         while ((str = in.readLine()) != null)
         {
             log.debug("readListFromFile method: Reading value from file: " + str);
 
             // It's a DropdownOption file
             if (str.indexOf("DROPDOWN_OPTION") > 0)
             {
                 isDropdownOption = true;
             }
             else if (isDropdownOption == true)
             {
                 StringTokenizer theTokenizer = new StringTokenizer(str);
                 String theLabel = theTokenizer.nextToken(",");
                 String theValue = theTokenizer.nextToken(",");
 
                 DropdownOption theDropdownOption = new DropdownOption(theLabel, theValue);
                 theReturnList.add(theDropdownOption);
             }
             else
             {
                 theReturnList.add(str);
             }
         }
         in.close();
 
         return theReturnList;
     }
 
     /**
      * Returns a list of Species that are edited-approved (4 currently)
      * Used for search screens
      * 
      * @return speciesNames
      * @throws Exception
      */
     protected static List getApprovedSpeciesList(HttpServletRequest inRequest,
                                          String inAddBlank) throws Exception
     {
         log.debug("Entering NewDropdownUtil.getApprovedSpeciesList");
 
         // Get values for dropdown lists for Species
         // for each Species, get it's commonName (scientificName)
         List theSpeciesList = QueryManagerSingleton.instance().getApprovedSpecies();
         List<DropdownOption> theReturnList = new ArrayList<DropdownOption>();
 
         if (theSpeciesList != null)
         {
             for (int i = 0; i < theSpeciesList.size(); i++)
             {
                 Species theSpecies = (Species) theSpeciesList.get(i);
                 if (theSpecies.getScientificName() != null)
                 {
                     String theDisplayName = theSpecies.getDisplayName();
                     if (theDisplayName.length() > 0)
                     {
                         DropdownOption theOption = new DropdownOption(theDisplayName, theSpecies.getScientificName());
                         theReturnList.add(theOption);
                     }
                 }
             }
         }
         // Sort the list in 'abc' order
         Collections.sort(theReturnList);
 
         log.debug("Exiting NewDropdownUtil.getApprovedSpeciesList");
         return theReturnList;
     }
 
     /**
      * Returns a list of all Species  
      * Used for submission and search screens
      * 
      * @return speciesNames
      * @throws Exception
      */
     private static List getQueryOnlySpeciesList(HttpServletRequest inRequest,
                                                 String inAddBlank) throws Exception
     {
         log.debug("Entering NewDropdownUtil.getQueryOnlySpeciesList");
 
         // Get values for dropdown lists for Species
         // for each Species, get it's commonName (scientificName)
         List theSpeciesList = QueryManagerSingleton.instance().getQueryOnlySpecies(inRequest);
         List<DropdownOption> theReturnList = new ArrayList<DropdownOption>();
 
         if (theSpeciesList != null)
         {
             for (int i = 0; i < theSpeciesList.size(); i++)
             {
                 Species theSpecies = (Species) theSpeciesList.get(i);
                 if (theSpecies.getScientificName() != null)
                 {
                     String theDisplayName = theSpecies.getDisplayName();
                     if (theDisplayName.length() > 0)
                     {
                         DropdownOption theOption = new DropdownOption(theDisplayName, theSpecies.getScientificName());
                         theReturnList.add(theOption);
                     }
                 }
             }
         }
         log.debug("Exiting getQueryOnlySpeciesList.size " + theReturnList.size());
         return theReturnList;
     }
     
     
     /**
      * Returns a list of all Non Human Species 
      * Used for model char submission screen
      * 
      * @return speciesNames
      * @throws Exception
      */
     private static List getQueryNonHumanSpeciesList(HttpServletRequest inRequest,
                                                 String inAddBlank) throws Exception
     {
         log.debug("Entering NewDropdownUtil.getQueryNonHumanSpeciesList");
 
         // Get values for dropdown lists for Species
         // for each Species, get it's commonName (scientificName)
         List theSpeciesList = QueryManagerSingleton.instance().getQueryOnlySpecies(inRequest);
         List<DropdownOption> theReturnList = new ArrayList<DropdownOption>();
 
         if (theSpeciesList != null)
         {
             for (int i = 0; i < theSpeciesList.size(); i++)
             {
                 Species theSpecies = (Species) theSpeciesList.get(i);
                 if (theSpecies.getScientificName() != null && !theSpecies.getScientificName().equals(Constants.Dropdowns.HUMANSCIENTIFICNAME))
                 {
                     String theDisplayName = theSpecies.getDisplayName();
                     log.debug("Entering NewDropdownUtil.getQueryNonHumanSpeciesList  :" + theDisplayName);
                     if (theDisplayName.length() > 0)
                     {
                         DropdownOption theOption = new DropdownOption(theDisplayName, theSpecies.getScientificName());
                         theReturnList.add(theOption);
                     }
                 }
             }
         }
         return theReturnList;
     }
 
     /**
      * Based on a species name retrieve a list of all Strains
      * 
      * @param speciesName
      * @return strainNames
      * @throws Exception
      */
     private static List getStrainsList(HttpServletRequest inRequest,
                                        String speciesName) throws Exception
     {
         log.debug("Entering NewDropdownUtil.getStrainsList");
 
         //Set constant for the Animal Model species here 
         inRequest.getSession().setAttribute(Constants.AMMODELSPECIES, speciesName);
         log.debug("In getStrainsList speciesName: " + speciesName);
 
         SpeciesManager speciesManager = (SpeciesManager) getContext(inRequest).getBean("speciesManager");
         Species species = null;
 
         List<Strain> strainList = new ArrayList<Strain>();
         List<String> strainNames = new ArrayList<String>();
 
         if (speciesName != null && speciesName.length() > 0)
         {
             // Add blank, other, and Not Specified to strain if user selects other for species 
             if (speciesName.equals(Constants.Dropdowns.OTHER_OPTION))
             {
                 addNotSpecified(strainNames);
                 addOther(strainNames);
                 addBlank(strainNames);
             }
             else
             {
                 species = speciesManager.getByName(speciesName);
                 strainList = new ArrayList<Strain>(species.getStrainCollection());
 
                 if (strainList.size() > 0)
                 {
                     //print out strain names                  
                     for (int i = 0; i < strainList.size(); i++)
                     {
                         Strain strain = (Strain) strainList.get(i);
 
                         if (strain.getName() != null && !strainNames.contains(strain.getName()))
                         {
                             strainNames.add(strain.getName());
                         }
                     }
                     // Sort the list in 'abc' order
                     Collections.sort(strainNames, String.CASE_INSENSITIVE_ORDER);
                     addOther(strainNames);
                     addBlank(strainNames);
                 }
                 else
                 {
                     addOther(strainNames);
                     addBlank(strainNames);
                 }
             }
         }
         return strainNames;
     }
 
 
     /**
      * Returns a list of all Administrative Routes
      * 
      * @return adminList
      * @throws Exception
      */
     private static List getViralVectorList(HttpServletRequest inRequest) throws Exception
     {
 
         // Get values for dropdown lists for Species, Strains
         GeneDeliveryManager geneDeliveryManager = (GeneDeliveryManager) getContext(inRequest).getBean("geneDeliveryManager");
 
         List<GeneDelivery> geneDeliveryList = null;
 
         geneDeliveryList = geneDeliveryManager.getAll();
 
         List<String> viralVectorList = new ArrayList<String>();
         GeneDelivery tmp;
 
         if (geneDeliveryList != null)
         {
             for (int i = 0; i < geneDeliveryList.size(); i++)
             {
                 tmp = (GeneDelivery) geneDeliveryList.get(i);
 
                 if (tmp.getViralVector() != null)
                 {
                     // if the name is not already in the List, add it
                     // (only get unique names)
                     if (!viralVectorList.contains(tmp.getViralVector()))
                         viralVectorList.add(tmp.getViralVector());
                 }
             }
         }
         Collections.sort(viralVectorList);
 
         addOther(viralVectorList);
 
         return viralVectorList;
     }
 
     /**
      * Returns a list of all external sources from animalModel
      * 
      * @return externalSourceList
      * @throws Exception
      */
     private static List getExternalSourceList(HttpServletRequest inRequest) throws Exception
     {
        	log.debug("<NewDropdownUtil> In getExternalSourceList: ");
        	
        	List externalSourceList = QueryManagerSingleton.instance().getExternalSources();       	
         
         Collections.sort(externalSourceList);
         
         return externalSourceList;
     } 
     
     /**
      * Returns a list of all usernames from party
      * 
      * @return usernameList
      * @throws Exception
      */
     private static List getUsernameList(HttpServletRequest inRequest) throws Exception
     {
        	log.debug("<NewDropdownUtil> In getUsernameList: ");
        	
        	List validUsernameList = QueryManagerSingleton.instance().getUsernames();       	
         
         Collections.sort(validUsernameList);
         log.debug("<NewDropdownUtil> validUsernameList.size(): " + validUsernameList.size());
         
         return validUsernameList;
     }     
     
     /**
      * Returns a list of all carcinogenic agents (environmental factor types) from animalModel
      * 
      * @return carcinogenicAgentList
      * @throws Exception
      */
     private static List getCarcinogenicAgentList(HttpServletRequest inRequest) throws Exception
     {
        	log.debug("<NewDropdownUtil> In getCarcinogenicAgentList: ");
        	
        	List carcinogenicAgentList = QueryManagerSingleton.instance().getEnvironmentalFactorAgentTypes();       	
         
         Collections.sort(carcinogenicAgentList);
         
         return carcinogenicAgentList;
     }     
     
     
     /**
      * Returns a list for a type of environmental Factors
      * 
      * @return envList
      */
     private static List getEnvironmentalFactorList(String inType) throws Exception
     {
         log.debug("<getEnvironmentalFactorList> inType: " + inType);
         List theEnvFactorList = QueryManagerSingleton.instance().getEnvironmentalFactors(inType);
 
         addOther(theEnvFactorList);
         return theEnvFactorList;
     }
 
     /**
      * Returns a list of environmental facotr names for a type of environmental Factors
      * 
      * @return envList
      */
     private static List getQueryOnlyEnvironmentalFactorList(HttpServletRequest inRequest,
     												String inType) throws Exception
     {
     	log.debug("<getQueryOnlyEnvironmentalFactorList> inType: " + inType);
         
         List theEnvFactorList = QueryManagerSingleton.instance().getQueryOnlyEnvironmentalFactors(inType);
         
         //set for search validation to prevent cross-site scripting attacks in searchForm
         inRequest.getSession().setAttribute(Constants.Dropdowns.ENVIRONMENTALFACTORNAMESDROP, theEnvFactorList);        
         addBlank(theEnvFactorList);
         return theEnvFactorList;
     }
 
     /**
      * Returns a list of all cell lines
      * 
      * @return cell line list
      * @throws Exception
      */
     private static List getCellLineNameList(HttpServletRequest inRequest) throws Exception
     {
        	log.debug("<NewDropdownUtil> In getCellLineNameList: ");
        	
        	List cellLineList = QueryManagerSingleton.instance().getCellLineNames();       	
         
         Collections.sort(cellLineList, String.CASE_INSENSITIVE_ORDER);
         
         return cellLineList;
     } 
  
     /**
      * Returns a list of all agent names for therapeutic approaches only
      * 
      * @return agent names list
      * @throws Exception
      */
     private static List getTherapeuticDrugNameList(HttpServletRequest inRequest) throws Exception
     {
        	log.debug("<NewDropdownUtil> In getTherapeuticDrugNameList: ");
        	
        	List therapeuticDrugList = QueryManagerSingleton.instance().getTherapeuticDrugNames();       	
         
         Collections.sort(therapeuticDrugList, String.CASE_INSENSITIVE_ORDER);
         
         return therapeuticDrugList;
     } 
     
     /**
      * Returns a list of all clone designators (genomic segment designators)
      * 
      * @return clone designators list
      * @throws Exception
      */
     private static List getGenomicSegmentDesignatorList(HttpServletRequest inRequest) throws Exception
     {
        	log.debug("<NewDropdownUtil> In getGenomicSegmentDesignatorList: ");
        	
        	List genSegDesList = QueryManagerSingleton.instance().getGenomicSegmentDesignators();       	
         
         Collections.sort(genSegDesList, String.CASE_INSENSITIVE_ORDER);
         
         return genSegDesList;
     }
     
     /**
      * Returns a list of all gene names (targeted modification selected
      * for the Genetic Description on the advanced search)
      * 
      * @return gene name list
      * @throws Exception
      */
     private static List getGeneNameList(HttpServletRequest inRequest) throws Exception
     {
     	log.debug("<getGeneNameList>"  );
         
         List theGeneNameList = QueryManagerSingleton.instance().getGeneNames();
         
         //set for search validation to prevent cross-site scripting attacks in searchForm
         inRequest.getSession().setAttribute(Constants.Dropdowns.TARGETEDMODNAMEQUERYDROP, theGeneNameList);
         addBlank(theGeneNameList);
         return theGeneNameList;
     } 
     
     /**
      * Returns a list of all gene names (transgene selected
      * for the Genetic Description on the advanced search)
      * 
      * @return transgene name list
      * @throws Exception
      */
     private static List getTransgeneNameList(HttpServletRequest inRequest) throws Exception
     {
     	log.debug("<getTransgeneNameList>  " );
         
         List theTransgeneNameList = QueryManagerSingleton.instance().getTransgeneNames();
         
         //set for search validation to prevent cross-site scripting attacks in searchForm
         inRequest.getSession().setAttribute(Constants.Dropdowns.TRANSGENENAMEQUERYDROP, theTransgeneNameList);
         addBlank(theTransgeneNameList);
         return theTransgeneNameList;
     }    
     
     /**
      * Returns a list of all Principal Investigators
      * 
      * @return list of PI's
      * @throws Exception
      */
     private static List getPrincipalInvestigatorList(HttpServletRequest inRequest,
                                                      String inAddBlank) throws Exception
     {
         log.debug("Entering NewDropdownUtil.getPrincipalInvestigatorList");
 
         List thePIList = QueryManagerSingleton.instance().getPrincipalInvestigators();
 
         List<DropdownOption> theReturnList = new ArrayList<DropdownOption>();
 
         if (thePIList != null)
         {
             for (int i = 0; i < thePIList.size(); i++)
             {
                 Person thePerson = (Person) thePIList.get(i);
                 if (thePerson.getIsPrincipalInvestigator() != null)
                 {
                     String theDisplayName = thePerson.getDisplayName();
                     if (theDisplayName.length() > 0)
                     {
                         DropdownOption theOption = new DropdownOption(theDisplayName, thePerson.getUsername());
                         theReturnList.add(theOption);
                     }
                 }
             }
         }
 
         log.debug("Exiting NewDropdownUtil.getPrincipalInvestigatorList");
 
         return theReturnList;
     }
 
     /**
      * Returns a list of all Principal Investigators for edited-approved models
      * 
      * @return list of PI's
      * @throws Exception
      */
     private static List getQueryOnlyPrincipalInvestigatorList(HttpServletRequest inRequest,
                                                               String inAddBlank) throws Exception
     {
 
         log.debug("Entering NewDropdownUtil.getQueryOnlyPrincipalInvestigatorList");
         
         //Assign list to a constant so simple search can validate against cross-site scripting in searchForm.validate
         List thePIOnlyList = QueryManagerSingleton.instance().getQueryOnlyPrincipalInvestigators(); 
         log.debug("In NewDropdownUtil.getQueryOnlyPrincipalInvestigatorList - set SEARCHPIDROP constant ");
         inRequest.getSession().setAttribute(Constants.Dropdowns.PRINCIPALINVESTIGATORQUERYDROP, thePIOnlyList);        
         
         return thePIOnlyList;
     }
 
     /**
      * Returns a list of all Principal Investigators for models in all states
      * 
      * @return list of PI's
      * @throws Exception
      */
     private static List getQueryAllPrincipalInvestigatorList(HttpServletRequest inRequest,
                                                               String inAddBlank) throws Exception
     {
 
         log.debug("Entering NewDropdownUtil.getQueryAllPrincipalInvestigatorList");
         
         //Assign list to a constant so simple search can validate against cross-site scripting in searchForm.validate
         List thePIOnlyList = QueryManagerSingleton.instance().getQueryAllPrincipalInvestigators(); 
         log.debug("In NewDropdownUtil.getQueryAllPrincipalInvestigatorList -  ");
         inRequest.getSession().setAttribute(Constants.Dropdowns.PRINCIPALINVESTIGATORQUERYALLDROP, thePIOnlyList);        
         
         return thePIOnlyList;
     }   
     
 
     /**
      * Returns a list of all Agents that were used to induce a mutation
      * 
      * @return list of agent names
      * @throws Exception
      */
     private static List getQueryOnlyInducedMutationAgentList(HttpServletRequest inRequest,
                                                              String inAddBlank) throws Exception
     {
 
         log.debug("Entering NewDropdownUtil.getQueryOnlyInducedMutationAgentList");
 
         List inducedMutationList = QueryManagerSingleton.instance().getQueryOnlyInducedMutationAgents();
         //addOther(inducedMutationList);
         return inducedMutationList;
     }
 
     /**
      * Returns a list of all users
      * 
      * @return list of users
      * @throws Exception
      */
     private static List getUsersList(HttpServletRequest inRequest,
                                      String inAddBlank) throws Exception
     {
         log.trace("Entering NewDropdownUtil.getUsersList");
 
         List thePersonList = Search.query(Person.class);
 
         List<DropdownOption> theReturnList = new ArrayList<DropdownOption>();
 
         // Add all of the display names
         if (thePersonList != null)
         {
             for (int i = 0; i < thePersonList.size(); i++)
             {
                 Person thePerson = (Person) thePersonList.get(i);
 
                 DropdownOption theOption = new DropdownOption(thePerson.getDisplayNameWithOrg(), thePerson.getId().toString());
                 theReturnList.add(theOption);
             }
         }
 
         log.trace("Exiting NewDropdownUtil.getUsersList");
 
         Collections.sort(theReturnList);
         return theReturnList;
     }
     
     /**
      * Returns a list of all Staining Method Names
      * 
      * @throws Exception
      */
     private static List getStainingMethod(HttpServletRequest inRequest) throws Exception
     {
         log.debug("Entering NewDropdownUtil.getStainingMethod");
         
         // Get values for dropdown lists for StainingMethod
         StainingMethodManager stainingMethodManager = (StainingMethodManager) getContext(inRequest).getBean("stainingMethodManager");
 
         List stainingList = null;
 
         stainingList = stainingMethodManager.getAll();
 
         List<String> stainingMethodList = new ArrayList<String>();
         StainingMethod tmp;
 
         if (stainingList != null)
         {
             for (int i = 0; i < stainingList.size(); i++)
             {
                 tmp = (StainingMethod) stainingList.get(i);
 
                 if (tmp.getName() != null)
                 {
                     // if the StainingMethod is not already in the List, add it
                     // (only get unique names)
                     if (!stainingMethodList.contains(tmp.getName()))
                         stainingMethodList.add(tmp.getName());
                 }
             }
         }
         Collections.sort(stainingMethodList);
         log.debug("Exiting NewDropdownUtil.getStainingMethod");        
         return stainingMethodList;
 
     }    
 
 
     
     /**
      * Returns a list of all Expression Level Descriptions
      * 
      * @return list of users
      * @throws Exception
      */
     private static List getExpressionLevelList(HttpServletRequest inRequest) throws Exception
     {
         // Get values for dropdown lists for Expression Level
         ExpressionLevelDescManager expressionLevelDescManager = (ExpressionLevelDescManager) getContext(inRequest).getBean(
                                                                                                                            "expressionLevelDescManager");
 
         List expList = null;
 
         expList = expressionLevelDescManager.getAll();
 
         List<String> expressionLevelList = new ArrayList<String>();
         ExpressionLevelDesc tmp;
 
         if (expList != null)
         {
             for (int i = 0; i < expList.size(); i++)
             {
                 tmp = (ExpressionLevelDesc) expList.get(i);
 
                 if (tmp.getExpressionLevel() != null)
                 {
                     // if the ExpressionLevel is not already in the List, add it
                     // (only get unique names)
                     if (!expressionLevelList.contains(tmp.getExpressionLevel()))
                         expressionLevelList.add(tmp.getExpressionLevel());
                 }
             }
         }
         Collections.sort(expressionLevelList);
         return expressionLevelList;
 
     }
 
     /**
      * Returns a list of all states for a curation flow and add a blank for 
      * adminEditModels.jsp only (previously used for adminCommentsAssignment.jsp
      * and adminModelsAssignment.jsp
      * 
      * @return list of curation states
      * 
      * @throws Exception
      */
     private static List getCurationStatesWithBlankList(HttpServletRequest inRequest,
                                               String inWorkflow) throws Exception
     {
         List theStateList = new ArrayList<Object>(); 
         log.debug("Entering NewDropdownUtil.getCurationStatesList");
 
         // Get the curation manager workflow XML
         CurationManager theCurationManager = new CurationManagerImpl(inRequest.getSession().getServletContext().getRealPath("/") + inWorkflow);
         
         // Add blank to the state list for adminEditModels.jsp
         theStateList = theCurationManager.getAllStateNames();
         addBlank(theStateList);
         return theStateList;
     }
     
     /**
      * Returns a list of all states for a curation flow
      * 
      * @return list of curation states
      * 
      * @throws Exception
      */
     private static List getCurationStatesList(HttpServletRequest inRequest,
                                               String inWorkflow) throws Exception
     {
         List theStateList = new ArrayList<Object>(); 
         log.debug("Entering NewDropdownUtil.getCurationStatesList");
 
         // Get the curation manager workflow XML
         CurationManager theCurationManager = new CurationManagerImpl(inRequest.getSession().getServletContext().getRealPath("/") + inWorkflow);
         
         // Add blank to the state list for adminEditModels.jsp
         theStateList = theCurationManager.getAllStateNames();
         addBlank(theStateList);
         return theStateList;
     }    
 
     /**
      * Returns a list of all states for a curation flow
      * 
      * @return list of curation states
      * 
      * @throws Exception
      */
     private static List getUsersForRoleList(HttpServletRequest inRequest,
                                             String inRoleName) throws Exception
     {
 
         log.trace("Entering NewDropdownUtil.getUsersForRoleList");
 
         List theUserList = new ArrayList<String>();
 
         Role theRole = new Role();
         theRole.setName(inRoleName);
 
         try
         {
             List theRoles = Search.query(theRole);
 
             if (theRoles.size() > 0)
             {
                 theRole = (Role) theRoles.get(0);
                 
                 // Defaults list to blank in the first position - validation does not work
                 //theUserList.add(new DropdownOption(null, null));                 
 
                 // Get the users for the role
                 Set<Party> theUsers = theRole.getPartyCollection();
                 Iterator theIterator = theUsers.iterator();
 
                 // Go through the list of returned Party objects
                 while (theIterator.hasNext())
                 {
                     Object theObject = theIterator.next();
 
                     // Only add when it's actually a person
                     if (theObject instanceof Person)
                     {
                         Person thePerson = (Person) theObject;
                         theUserList.add(new DropdownOption(thePerson.getDisplayName(), thePerson.getUsername()));
                     }
                 }
             }
             else
             {
                 log.warn("Role not found in database: " + inRoleName);
             }
         }
         catch (Exception e)
         {
             log.error("Unable to get roles for user: ", e);
             throw e;
         }
         return theUserList;
     }
     
     /**
      * Returns a list of all Editors or screeners - display and use firstName and lastName
      * instead of the DropdownOption
      * 
      * @return list of Editors or Screeners
      * 
      * @throws Exception
      */
     private static List getUsersForSpecificRoleList(HttpServletRequest inRequest,
                                             String inRoleName) throws Exception
     {
 
         log.debug("Entering NewDropdownUtil.getUsersForSpecificRoleList");
 
         List theUserList = new ArrayList<Object>();
         List<String> theUserNames = new ArrayList<String>();        
 
         Role theRole = new Role();
         theRole.setName(inRoleName);
 
         try
         {
             List theRoles = Search.query(theRole);
 
             if (theRoles.size() > 0)
             {
                 theRole = (Role) theRoles.get(0);
                 
                 // Defaults list to blank in the first position
                 addBlank(theUserList);                
 
                 // Get the users for the role
                 Set<Party> theUsers = theRole.getPartyCollection();
                 Iterator theIterator = theUsers.iterator();
 
                 // Go through the list of returned Party objects
                 while (theIterator.hasNext())
                 {
                     Object theObject = theIterator.next();
 
                     // Only add when it's actually a person
                     if (theObject instanceof Person)
                     {
                         Person thePerson = (Person) theObject;
                         String thePersonName = thePerson.getDisplayName();
                         log.debug("thePerson in list: " + thePersonName);                        
                         theUserList.add(thePersonName);
                         //(new DropdownOption(thePerson.getDisplayName(), thePerson.getUsername()));
                     }
                 }
             }
             else
             {
                 log.warn("Role not found in database: " + inRoleName);
             }
         }
         catch (Exception e)
         {
             log.error("Unable to get roles for user: ", e);
             throw e;
         }
         return theUserList;
     }    
 
     
      
     
     /**
      * Returns a list of all the known roles
      * 
      * @return list of roles
      */
     private static List getRolesList(HttpServletRequest inRequest)
     {
 
         // Generate the roles list
         List<String> theRoles = new ArrayList<String>();
         theRoles.add(Constants.Admin.Roles.ALL);
         theRoles.add(Constants.Admin.Roles.COORDINATOR);
         theRoles.add(Constants.Admin.Roles.EDITOR);
         theRoles.add(Constants.Admin.Roles.SCREENER);
 
         return theRoles;
     }
 
     /**
      * Add Other to the list in the first spot if it's not already there.
      * Removes it and put's it in the first spot if it is.
      */
     private static void addOther(List inList)
     {
 
         if (!inList.contains(Constants.Dropdowns.OTHER_OPTION))
         {
             inList.add(0, Constants.Dropdowns.OTHER_OPTION);
         }
         else
         {
             inList.remove(Constants.Dropdowns.OTHER_OPTION);
             inList.add(0, Constants.Dropdowns.OTHER_OPTION);
         }
     }
 
     /**
      * Add Not Specified to the list in the first spot if it's not already there.
      * Removes it and put's it in the second spot if it is.
      */
     private static void addNotSpecified(List inList)
     {
 
         if (!inList.contains(Constants.Dropdowns.NOT_SPECIFIED_OPTION))
         {
             inList.add(0, Constants.Dropdowns.NOT_SPECIFIED_OPTION);
         }
         else
         {
             inList.remove(Constants.Dropdowns.NOT_SPECIFIED_OPTION);
             inList.add(0, Constants.Dropdowns.NOT_SPECIFIED_OPTION);
         }
     }
 
     /**
      * Add Other to the list in the first spot if it's not already there.
      * Removes it and put's it in the first spot if it is.
      */
     private static void addOtherOption(List inList)
     {
 
         DropdownOption theDropdownOption = new DropdownOption(Constants.Dropdowns.OTHER_OPTION, Constants.Dropdowns.OTHER_OPTION);
 
         if (!inList.contains(theDropdownOption))
         {
             inList.add(0, theDropdownOption);
         }
         else
         {
             inList.remove(theDropdownOption);
             inList.add(0, theDropdownOption);
         }
     }
 
     /**
      * Add "" to the list in the first spot if it's not already there. Removes
      * it and put's it in the first spot if it is.
      */
     private static void addBlank(List inList)
     {
 
         if (!inList.contains(""))
         {
             inList.add(0, "");
         }
         else
         {
             inList.remove("");
             inList.add(0, "");
         }
     }
 
     /**
      * Add "" to the list in the first spot if it's not already there. Removes
      * it and put's it in the first spot if it is.
      */
     private static void addBlankOption(List inList)
     {
 
         DropdownOption theDropdownOption = new DropdownOption("", "");
 
         if (!inList.contains(theDropdownOption))
         {
             inList.add(0, theDropdownOption);
         }
         else
         {
             inList.remove(theDropdownOption);
             inList.add(0, theDropdownOption);
         }
     }
 
 }
