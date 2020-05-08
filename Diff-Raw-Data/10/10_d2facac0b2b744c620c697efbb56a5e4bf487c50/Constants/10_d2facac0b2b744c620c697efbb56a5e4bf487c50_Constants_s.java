 /**
  * 
 * $Id: Constants.java,v 1.121 2009-06-08 15:31:53 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.120  2009/06/04 16:28:10  pandyas
  * The Property object for the Zebrafish vocabulary returns only  Preferred_Name, Synonym, NCI_Preferred_Term.  We used to display the "display_name" for both the NCI_Thesaurus and Zebrafish vocabs.
  *
  * Revision 1.119  2009/05/20 17:06:16  pandyas
  * modified for gforge #17325 Upgrade caMOD to use caBIO 4.x and EVS 4.x to get data
  *
  * Revision 1.118  2009/05/14 18:44:30  pandyas
  * modified for gforge #21177  	Upgrade validation on SearchForm
  *
  * Revision 1.117  2009/04/30 20:01:09  pandyas
  * removed two changes meant for EVS - caBIO upgrade which is not built yet
  *
  * Revision 1.116  2009/04/30 18:39:12  pandyas
  * modified for #17833  	Make sure all references to Transplantation are properly named
  * - modified 4 more files
  *
  * Revision 1.115  2009/03/25 16:17:45  pandyas
  * modified for #17833  	Make sure all references to Tranplantation are properly named
  *
  * Revision 1.114  2009/03/13 14:56:47  pandyas
  * modified for #19758  	Remove the filter from the PI drop-down list on admin (Edit Models) search criteria screen
  *
  * Revision 1.113  2008/08/14 06:22:10  schroedn
  * Added constant for microarraydata list
  *
  * Revision 1.112  2008/08/12 19:53:24  pandyas
  * Fixed #12108  	Admin - View Model Assignment is not working
  * Fixed #15053  	Search for models with transgenic or targeted modification on advanced search page confusing
  * Fixed #11640  	Delete availability from IMSR from application
  *
  * Revision 1.111  2008/05/21 19:04:58  pandyas
  * Modified advanced search to prevent SQL injection
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.110  2008/01/16 18:30:46  pandyas
  * Renamed value to Transplant for #8290
  *
  * Revision 1.109  2007/11/25 23:31:45  pandyas
  * Added constant for feature #8816  	Connection to caELMIR - retrieve data for therapy search page
  *
  * Revision 1.108  2007/11/01 13:37:03  pandyas
  * Fixed #8290     Rename graft object into transplant object
  *
  * Revision 1.107  2007/10/18 18:25:30  pandyas
  * Added constants to prevent cross--site scripting attacks
  *
  * Revision 1.106  2007/10/17 18:29:07  pandyas
  * Added constants to prevent cross--site scripting attacks
  *
  * Revision 1.105  2007/08/14 12:03:09  pandyas
  * Implementing EVSPreferredName for Zebrafish models
  *
  * Revision 1.104  2007/07/31 12:04:18  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.103  2007/05/22 12:51:43  pandyas
  * added as work around until the EVS vocabularies are ready for Zebrafish
  *
  * Revision 1.102  2007/05/21 17:32:06  pandyas
  * Modified simple and adv search species drop down to pull from DB (approved model species only)
  *
  * Revision 1.101  2007/05/16 12:27:40  pandyas
  * Modified adv and simple search vocab tree section to populate depending on species selected
  *
  * Revision 1.100  2007/05/10 02:20:11  pandyas
  * Implemented species specific vocabulary trees from EVSTree
  * Added constant for donor species common name
  *
  * Revision 1.99  2007/04/30 20:06:13  pandyas
  * Implemented species specific vocabulary trees from EVSTree
  *
  * Revision 1.98  2007/04/04 13:16:34  pandyas
  * Modified name for conditioning regimen and target site
  *
  * Revision 1.97  2007/03/28 18:00:00  pandyas
  * Modified for the following Test Track items:
  * #462 - Customized search for carcinogens for Jackson Lab data
  * #494 - Advanced search for Carcinogens for Jackson Lab data
  *
  * Revision 1.96  2007/03/27 18:57:45  pandyas
  * Added constants for caMOD 2.3 build tasks
  *
  * Revision 1.95  2006/12/28 16:02:07  pandyas
  * Reverted to previous version - changed CE on adv search page
  *
  * Revision 1.93  2006/11/01 21:19:14  pandyas
  * added constant for state
  *
  * Revision 1.92  2006/10/27 13:02:24  pandyas
  * added constant used for online help - removed the onclick for ToolTips
  *
  * Revision 1.91  2006/10/17 16:14:49  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.90  2006/09/20 16:13:03  pandyas
  * updated with a few caMOD 2.2 constants - will not hurt if unused on dev server
  *
  * Revision 1.88  2006/08/17 17:45:03  pandyas
  * Defect# 410: Deleted constants no longer used due to externalized property  files
  *
  * Revision 1.87  2006/05/24 16:45:55  pandyas
  * Converted StainingMethod to lookup - modified code to pull dropdown list from DB
  *
  * Revision 1.86  2006/05/17 16:12:34  pandyas
  * added better comments
  *
  * Revision 1.85  2006/05/10 15:37:36  schroedn
  * Fixed Dup_Name bug
  *
  * Revision 1.84  2006/05/10 13:28:51  schroedn
  * New Features - Code Review changes
  *
  * Revision 1.83  2006/05/09 18:45:47  georgeda
  * Changes for searching on transient interfaces
  *
  * Revision 1.82  2006/05/04 14:15:33  pandyas
  * Modified/Added to support Morpholino object data in the application
  *
  * Revision 1.81  2006/04/28 19:03:57  schroedn
  * Defect # 238, 261, 55
  * Added Constants used Saving/Editing Queries, Keyword Highlighting and Configuring search result columns
  *
  * Revision 1.80  2006/04/20 19:45:31  pandyas
  * Added constant for otherStrainName used on submitXenograftTransplant.jsp
  *
  * Revision 1.79  2006/04/17 19:15:36  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.78  2005/12/06 19:51:25  georgeda
  * Defect #255 - add SSL
  *
  * Revision 1.77  2005/11/28 18:02:10  georgeda
  * Defect #182.  Get unique set of organs and only display metas. next to the originating organ
  *
  * Revision 1.76  2005/11/28 13:41:35  georgeda
  * Defect #192, handle back arrow for curation changes
  *
  * Revision 1.75  2005/11/18 21:04:54  georgeda
  * Defect #130, added superuser
  *
  * Revision 1.74  2005/11/17 20:42:03  schroedn
  * Defect #93
  *
  * Fixed CAIMAGEGENCONSERVERVIEW variable to point to correct property in camod.properties. This allows for GeneticConstruts to be saved in correct directory on caImage server.
  *
  * Revision 1.73  2005/11/14 14:15:37  georgeda
  * Cleanup
  *
  * Revision 1.72  2005/11/11 21:24:19  georgeda
  * Defect #29.  Separate drug screening and animal model search results.
  *
  * Revision 1.71  2005/11/11 15:37:57  georgeda
  * Fixed IE error with action name
  *
  * Revision 1.70  2005/11/08 21:59:31  georgeda
  * LDAP code
  *
  * Revision 1.69  2005/11/08 17:46:20  pandyas
  * added for Xenograft
  *
  * Revision 1.68  2005/11/08 16:46:33  georgeda
  * Changes for images
  *
  * Revision 1.67  2005/11/07 21:55:10  georgeda
  * Changes for images
  *
  * Revision 1.66  2005/11/03 13:57:58  georgeda
  * Delete functionality changes
  *
  * Revision 1.65  2005/11/02 20:56:04  schroedn
  * Added Staining to Image submission
  *
  * Revision 1.64  2005/11/02 20:28:59  pandyas
  * modified GeneDelivery dropdown source
  *
  * Revision 1.63  2005/11/02 17:15:58  schroedn
  * Updated Image viewer, added constants and properties to camod.properties, merged code to ease changes later
  *
  * Revision 1.62  2005/11/02 16:33:41  georgeda
  * Misc fixes
  *
  * Revision 1.61  2005/10/28 12:47:11  georgeda
  * Action constant
  *
  * Revision 1.60  2005/10/27 18:31:50  georgeda
  * New dropdown options
  *
  * Revision 1.59  2005/10/27 17:13:19  guruswas
  * added publications to capture all publications
  *
  * Revision 1.58  2005/10/27 15:29:59  georgeda
  * Cleanup
  *
  * Revision 1.57  2005/10/26 20:40:30  schroedn
  * Added AssocExpression to EngineeredTransgene submission page
  *
  * Revision 1.56  2005/10/24 22:00:48  pandyas
  * added back availability_list so it doesn't break everyone else
  *
  * Revision 1.55  2005/10/24 21:16:59  pandyas
  * added availability constants
  *
  * Revision 1.54  2005/10/24 21:04:03  schroedn
  * Added Image to submission
  *
  * Revision 1.53  2005/10/24 18:44:41  georgeda
  * Do species from dropdown
  *
  * Revision 1.52  2005/10/24 13:26:28  georgeda
  * Cleanup changes
  *
  * Revision 1.51  2005/10/21 20:46:21  georgeda
  * Added user registration settings
  *
  * Revision 1.50  2005/10/21 19:36:56  schroedn
  * Added Constants for Image upload and retrieval
  *
  * Revision 1.49  2005/10/20 21:35:22  georgeda
  * Added xenograft constant
  *
  * Revision 1.48  2005/10/20 21:20:17  pandyas
  * add animal availability list
  *
  * Revision 1.47  2005/10/20 21:14:15  stewardd
  * added constants used in e-mail generation of InducedMutationManagerImpl and TargetedModificationManagerImpl classe.
  *
  * Revision 1.46  2005/10/20 19:28:28  georgeda
  * Added TOC constants
  *
  * Revision 1.45  2005/10/19 18:56:26  guruswas
  * implemented invivo details page
  *
  * Revision 1.44  2005/10/17 13:25:17  georgeda
  * Work for comments/users
  *
  * Revision 1.43  2005/10/13 16:18:51  pandyas
  * added constant for growth factor dose units
  *
  * Revision 1.42  2005/10/11 20:51:12  schroedn
  * Added constant for ENGINEEREDTRANSGENE_LIST
  *
  * Revision 1.41  2005/10/11 19:56:19  pandyas
  * added constant for assc met list
  *
  * Revision 1.40  2005/10/11 18:12:08  georgeda
  * More comment changes
  *
  * Revision 1.39  2005/10/10 14:05:38  georgeda
  * Cleanup and additions for comment curation
  *
  * Revision 1.37  2005/10/05 20:27:59  guruswas
  * implementation of drug screening search page
  *
  * Revision 1.36  2005/10/05 19:24:14  pandyas
  * added clinical marker list
  *
  * Revision 1.35  2005/10/05 16:21:50  pandyas
  * added histopthology and therapy lists
  *
  * Revision 1.34  2005/10/04 20:18:48  georgeda
  * Updates from search changes
  *
  * Revision 1.33  2005/10/04 20:09:41  schroedn
  * Added Spontaneous Mutation, InducedMutation, Histopathology, TargetedModification and GenomicSegment
  *
  * Revision 1.32  2005/10/03 16:07:39  pandyas
  * modified histopathology constant name to reflect contents
  *
  * Revision 1.31  2005/10/03 15:31:00  pandyas
  * added clinical marker and histopathology constants
  *
  * Revision 1.30  2005/10/03 13:04:19  georgeda
  * Updates from search changes
  *
  * Revision 1.29  2005/09/30 18:47:46  pandyas
  * added all differences to my copy before uploading
  *
  * Revision 1.25  2005/09/27 16:34:31  georgeda
  * Changed administravive route drop down
  *
  *
  */
 package gov.nih.nci.camod;
 
 
 /**
  * Constant values used throughout the application.
  * 
  * <p>
  * <a href="Constants.java.html"><i>View Source</i></a>
  * </p>
  */
 public class Constants {
 	
 
     /** The name of the ResourceBundle used in this application */
     public static final String BUNDLE_KEY = "ApplicationResources";
 
     /** The application scoped attribute for persistence engine used */
     public static final String DAO_TYPE = "daoType";
 
     public static final String DAO_TYPE_HIBERNATE = "hibernate";
 
     /** Application scoped attributes for SSL Switching */
     public static final String HTTP_PORT = "httpPort";
 
     public static final String HTTPS_PORT = "httpsPort";
 
     /**
      * The name of the Administrator role, as specified in web.xml
      */
     public static final String ADMIN_ROLE = "admin";
 
     /**
      * The name of the configuration hashmap stored in application scope.
      */
     public static final String CONFIG = "appConfig";
 
     public static final String UPT_CONTEXT_NAME = "camod";
     
     public static final String CONDITIONAL = "Conditional";
     
     public static final String NOT_CONDITIONAL = "Not Conditional";
     
     public static final String SEARCHRESULTCOLUMNS = "SearchResultsColumns";        
       
     public static final String ITEMSPERPAGE = "ItemsPerPage";
     
     public static final int ITEMSPERPAGEDEFAULT = 15;
     
    public static final String[] SEARCHRESULTCOLUMNSDEFAULT = { "Model Id", "Model Descriptor", "Tumor Sites", "Species"  }; 
     
     public static final String SELECTEDSEARCHRESULTCOLUMNS = "selectedsearchresultcolumns";
     
     public static final String CRITERIATABLE = "criteriatable";
     
     public static final String NOSAVEOPTION = "nosaveoption";
         
     
     /**
      * Used to store list of models currently logged on user has previous
      * entered
      */
     public static final String USERMODELLIST = "usermodellist";
     
     /**
      * Used to store the species for the animal model 
      * used to disable the Organ/Disease tree for non-mouse models.
      */ 
     public static final String AMMODELSPECIES = "animalmodelspecies";    
 
 
     /**
      * Used to store lists for drop down menus
      */
     public interface CaArray {
 
         public static final String URI_START = "caarray.uri_start";
 
         public static final String URI_END = "caarray.uri_end";
     }
 
     /**
      * Used in table of contents searching
      */
     public interface TOCSearch {
 
         public static final String TOC_QUERY_FILE = "config/TOCQueryConfig.xml";
 
         public static final String TOC_QUERY_RESULTS = "TOC_QUERY_RESULTS";
 
     }
 
     /**
      * 
      * Used in AnimalModelSearchResult
      *
      */
     public interface ENVFactors { 
     	
         public static final String AGENT_TYPE = "agentType"; 
         
         public static final String ANTIBODY = "Antibody"; 
         
         public static final String BACTERIA = "Bacteria";        
         
         public static final String TRANSCRIPTIONAL1 = "Transcriptional 1"; 
         
         public static final String CHEMICAL_DRUG = "Chemical / Drug";
         
         public static final String HORMONE = "Hormone";
         
         public static final String GROWTH_FACTOR = "Growth Factor";
         
         public static final String VIRAL = "Viral";
         
         public static final String ENVIRONMOENT = "Environment";
         
         public static final String NUTRITION = "Nutrition";
         
 		public static final String PLASMID = "Plasmid";
 		
 		public static final String SURGERY = "Surgery";
 		
 		public static final String TRANSPOSON = "Transposon";
 		
 		public static final String RADIATION = "Radiation";
         
         public static final String VIRUS = "Virus";
         
         public static final String OTHER = "Other";
         
     }
     
     /**
      * Used to store lists for drop down menus
      */
     public interface Dropdowns {
 
         public static final String ADD_BLANK = "ADD_BLANK";
 
         public static final String ADD_OTHER = "ADD_OTHER";
 
         public static final String ADD_BLANK_AND_OTHER = "ADD_BLANK_AND_OTHER";
 
         public static final String ADD_BLANK_OPTION = "ADD_BLANK_OPTION";
 
         public static final String ADD_OTHER_OPTION = "ADD_OTHER_OPTION";
 
         public static final String ADD_BLANK_AND_OTHER_OPTION = "ADD_BLANK_AND_OTHER_OPTION";
 
         public static final String OTHER_OPTION = "Other";
         
         public static final String NOT_SPECIFIED_OPTION = "Not specified";
 
         // Six zeros is entered for all organ and disease entries without conceptCodes - text entry, ect.
         public static final String CONCEPTCODEZEROS = "000000"; 
         
         // C with six zeros is entered for Mouse disease entered manually from EVSTree
         public static final String CONCEPTCODEZEROSWITHC = "C000000";         
         
         /* all species from DB -  used for various Screens */
         public static final String SPECIESQUERYDROP = "speciesquerydrop.db";          
         
         /* all species except human from DB -  used for model characteristics */
         public static final String NONHUMANSPECIESDROP = "nonhumanspeciesdrop.db"; 
         
         /* Constant for Homo sapiens - used in the query above (NONHUMANSPECIESDROP) */
         public static final String HUMANSCIENTIFICNAME = "Homo sapiens";         
         
         public static final String STRAINTEXTDROP = "StrainText.txt";
         
         /* all strains for a species from DB -  used for various Screens */
         public static final String STRAINDROP = "straindrop.db";  
 
         /* Various text files used in the application - data located in WebRoot/config/dropdowns */
         public static final String SEXDISTRIBUTIONDROP = "SexDistributions.txt";
 
         public static final String DOSAGEUNITSDROP = "DoseUnits.txt";
 
         public static final String ADMINISTRATIVEROUTEDROP = "AdministrativeRoutes.txt";
 
         public static final String AGEUNITSDROP = "AgeUnits.txt";
 
         public static final String PUBDROP = "PublicationStatus.txt";
 
         public static final String TOXICITYGRADESDROP = "ToxicityGrades.txt";
 
         public static final String CLINICALMARKERSDROP = "ClinicalMarkers.txt";
         
         public static final String DEVELOPMENTALSTAGES = "DevelopmentalStages.txt";
         
         public static final String CONDITIONINGREGIMEN = "ConditioningRegimen.txt";
         
         // target site for Morpholino in TransientInterference object 
         public static final String MORPHOLINOTARGETSITE = "MorpholinoTargetSite.txt";
 
         // Diagnosis list for Zebrafish - for Histopathology screen 
         public static final String ZEBRAFISHDIAGNOSISDROP = "ZebrafishDiagnosis.txt";
         
         // Various Dose Units
         public static final String CONCENTRATIONUNITSDROP = "ConcentrationUnits.txt";
         
         public static final String CHEMTHERAPYDOSEUNITSDROP = "ChemTherapyDoseUnits.txt";
 
         public static final String ENVFACTORUNITSDROP = "EnvFactorUnits.txt";
 
         public static final String GENOMESEGSIZEUNITSDROP = "GenomeSegSizeUnits.txt";
 
         public static final String HISTOPATHVOLUMEUNITSDROP = "HistopathVolumeUnits.txt";
 
         public static final String HISTOPATHWEIGHTUNITSDROP = "HistopathWeightUnits.txt";
 
         public static final String HORMONEUNITSDROP = "HormoneUnits.txt";
 
         public static final String NUTFACTORUNITSDROP = "NutFactorUnits.txt";
 
         public static final String RADIATIONUNITSDROP = "RadiationUnits.txt";
 
         public static final String VIRALTREATUNITSDROP = "ViralTreatUnits.txt";
 
         public static final String TARGETEDMODIFICATIONDROP = "TargetedModificationTypes.txt";
 
         public static final String GENOMICSEGMENTDROP = "SegmentTypes.txt";
 
         public static final String GROWTHFACTORDOSEUNITSDROP = "GrowthFactorDoseUnits.txt";
 
         public static final String STAININGDROP = "Staining.db";
 
         // Specific to a single screen
         public static final String PRINCIPALINVESTIGATORDROP = "principalinvestigatordrop.db";
         
         public static final String APPROVEDSPECIESDROP = "approvedspeciesdrop.db";        
         
         public static final String CHEMICALDRUGDROP = "chemdrugdrop.db";
 
         public static final String ENVIRONFACTORDROP = "envfactordrop.db";
 
         public static final String GROWTHFACTORDROP = "growfactordrop.db";
 
         public static final String HORMONEDROP = "hormonedrop.db";
 
         public static final String NUTRITIONFACTORDROP = "nutritionfactordrop.db";
 
         public static final String RADIATIONDROP = "radiationdrop.db";
 
         public static final String SURGERYDROP = "surgerydrop.db";
 
         public static final String VIRUSDROP = "virusdrop.db";
 
         public static final String VIRALVECTORDROP = "ViralVectors.txt";
 
         public static final String SOURCETYPEDROP = "SourceTypes.txt";
 
         public static final String TRANSPLANTATIONADMINSITESDROP = "TransplantationAdminSites.txt";
 
         public static final String INDUCEDMUTATIONDROP = "InducedMutations.txt";
 
         public static final String EXPRESSIONLEVELDROP = "expressionlevel.db";
         
         // Constant for the trangene or targeted modification gene name for the Genetic Description advanced search
         public static final String GENETICDESCRIPTIONDROP = "geneticescriptiondrop.db";        
 
         // Morpholino/siRNA screen dropdowns
         public static final String MORPHOSOURCEDROP = "MorpholinoSources.txt";
         public static final String SIRNASOURCEDROP = "sirnaSources.txt";
         
         public static final String MORPHOTYPEDROP = "MorpholinoTypes.txt";
         public static final String SIRNATYPEDROP = "sirnaTypes.txt";
         
         public static final String SEQUENCEDIRECTIONSDROP = "SequenceDirections.txt";
         
         public static final String DELIVERYMETHODDROP = "DeliveryMethods.txt";
         public static final String SIRNADELIVMETHODDROP = "sirnaDeliveryMethods.txt";
         
         public static final String VISUALLIGANDSDROP = "VisualLigands.txt";
         public static final String SIRNAVISUALLIGANDSDROP = "sirnaVisualLigands.txt";
         
         
         
         public static final String SEARCHRESULTCOLUMNSDROP = "SearchResultsColumns.txt";        
         
         public static final String ITEMSPERPAGEDROP = "ItemsPerPage.txt";
         
         // PI list filtered by Editied-approved for simple and adv search screens
         public static final String PRINCIPALINVESTIGATORQUERYDROP = "principalinvestigatorquerydrop.db";
         
         // PI list NOT filtered by Editied-approved for admin > edit models screen
         public static final String PRINCIPALINVESTIGATORQUERYALLDROP = "principalinvestigatoralldrop.db";         
 
         public static final String INDUCEDMUTATIONAGENTQUERYDROP = "inducedmutationagentquerydrop.db";
                      
         public static final String EXTERNALSOURCEQUERYDROP = "externalsourcequerydrop.db";    
         
         // CI on advanced search taken from DB type and type_altern_entry columns
         public static final String CARCINOGENICAGENTSQUERYDROP = "carcinogenicagentsquerydrop.db"; 
         
         // Environmental Factor name field populated based on agent type slected in adv search
         public static final String ENVIRONMENTALFACTORNAMESDROP = "environmentalfactornames.db"; 
         
         // Gene Name for the Targeted Modification selected from the adv search
         public static final String TARGETEDMODNAMEQUERYDROP = "targetedmodnamequerydrop.db";    
         
         // Gene Name for the Transgene selected from the adv search
         public static final String TRANSGENENAMEQUERYDROP = "transgenenamequerydrop.db";          
         
         // Cell line name field selected in adv search
         public static final String CELLLINENAMEQUERYDROP = "celllinenamequerydrop.db"; 
         
         // clone Designator (Genomic Segment Designator on GUI) from Engineered Gene table selected in adv search
         public static final String CLONEDESIGNATORQUERYDROP = "clonedesignatorquerydrop.db";
         
         // Compound / Drug field selected in adv search from agent (name) table
         public static final String THERAPEUTICAPPROACHDRUGQUERYDROP = "therapeuticapproachdrugquerydrop.db";        
         
 
         // These two are used to display the species and strain currently in the
         // AnimalModelCharacteristics - Transplant screen
         public static final String MODELSPECIES = "modelspecies";
 
         public static final String MODELSTRAIN = "modelstrain";
         
         public static final String OTHERMODELSTRAIN = "othermodelstrain";        
 
         public static final String CHEMICALCLASSESDROP = "ChemicalClasses.txt";
 
         public static final String BIOLOGICALPROCESSDROP = "BiologicalProcess.txt";
 
         public static final String THERAPEUTICTARGETSDROP = "TherapeuticTargets.txt";
 
         // Used for user management
         public static final String USERSDROP = "users.db";
 
         // Used for curation
         public static final String CURATIONSTATESDROP = "curationstates.db";
         public static final String CURATIONSTATESWITHBLANKDROP = "curationstates.db";
         
         // Used for curation
         public static final String USERSFORROLEDROP = "usersforrole.db";
         
         // Used for dropdowns on adminEditModels.jsp - reuses code from USERSFORROLEDROP
         // Both the screeener and editor lists show up together on the jsp
         public static final String USERSFOREDITORROLEDROP = "usersforeditorrole.db"; 
         public static final String USERSFORSCREENERROLEDROP = "usersforscreenerrole.db";         
 
         // Used for role assignment
         public static final String ROLESDROP = "roles.db"; 
         
         public static final String 	ZEBRAFISHORGANLISTSDROP = "ZebrafishOrganList.txt";
 
         public static final String DEVELOPMENTALSTAGETHERAPYDROP = "DevelopmentalStageTherapy.txt";
         
         // Used for validation of searchForm to prevent cross-site scripting and SQL injection attacks
         public static final String SEARCHSPECIESDROP = "searchspecies"; 
         public static final String SEARCHEXTERNALSOURCEDROP = "searchexternalsource";   
         public static final String SEARCHINDUCEDMUTATIONDROP = "searchinducedmutation";  
         public static final String SEARCHCARCINOGENEXPOSUREDROP = "searchcarcinogenexposure";  
         public static final String SEARCHENVIRONFACTORDROP = "searchenvironfactor";
         public static final String SEARCHCELLLINEDROP = "searchcellline";
         public static final String SEARCHGENOMICSEGMENTDROP = "searchgenomicsegment";        
         public static final String SEARCHPIDROP = "searchpi";    
         public static final String SEARCHTOCDROP = "searchtableofcontents";
         public static final String SEARCHTHERAPEUTICDRUGNAMEDROP = "searchtherapeuticdrugname";  
         
         // Used for values selected for the advanced search for Genetic Description
         public static final String ENGINEEREDTRANSGENE = "engineeredTransgene";
         public static final String TARGETEDMODIFICATION = "targetedModification";
       
     }
 
     /**
      * Defines the global constants used as parameters for ftp requests
      */
     public interface CaImage {
         public static final String FTPSERVER = "caimage.ftp.server";
         public static final String FTPUSERNAME = "caimage.ftp.username";
         public static final String FTPPASSWORD = "caimage.ftp.password";
         public static final String FTPMODELSTORAGEDIRECTORY = "caimage.ftp.modelstoragedirectory";
         public static final String FTPGENCONSTORAGEDIRECTORY = "caimage.ftp.genconstoragedirectory";
         public static final String CAIMAGEMODELSERVERVIEW = "caimage.modelview.uri";
         public static final String CAIMAGEGENCONSERVERVIEW = "caimage.genconview.uri";
         public static final String CAIMAGESIDTHUMBVIEW = "caimage.sidthumbview.uri";
         public static final String CAIMAGESIDVIEWURISTART = "caimage.sidview.uri_start";
         public static final String CAIMAGESIDVIEWURIEND = "caimage.sidview.uri_end";
         public static final String CAIMAGEWINDOWSTART = "caimage.window.start";
         public static final String CAIMAGEWINDOWEND = "caimage.window.end";
         public static final String CAIMAGEMODEL = "caimage.model";
         public static final String CAIMAGEGENCON = "caimage.gencon";
         public static final String LEGACYJSP = "catalogviewtumors.jsp?";
         public static final String FILESEP = ";";
         public static final String IMGTAG = "img=";
     }
 
     public interface Ldap {
         public static final String INITIAL_CONTEXT_FACTORY_KEY = "ldap.initial.context.factory";
         public static final String PROVIDER_URL_KEY = "ldap.provider.url";
         public static final String SECURITY_AUTHENTICATION_KEY = "ldap.security.authentication";
         public static final String SECURITY_PROTOCOL_KEY = "ldap.security.protocol";
         public static final String CONTEXT_KEY = "ldap.context";
     }
 
     /**
      * Defines the global constants used as parameters to requests
      */
     public interface Parameters {
 
         public static final String ACTION = "submitAction";
 
         public static final String MODELID = "aModelID";
         
         public static final String PUBID = "APubID";        
         
         public static final String CELLID = "ACellID";         
 
         public static final String PERSONID = "aPersonID";
 
         public static final String QUERYID = "aQueryId";
         
         public static final String MODELSECTIONNAME = "aModelSectionName";
 
         public static final String MODELSECTIONVALUE = "modelSectionValue";
 
         public static final String COMMENTSID = "aCommentsID";
 
         public static final String COMMENTSLIST = "aCommentsList";
 
         public static final String TOCQUERYKEY = "aTOCQueryKey";
 
         public static final String EVENT = "aEvent";
 
         public static final String DELETED = "deleted";
     }
 
     public interface Pages {
 
         public static final String MODEL_CHARACTERISTICS = "General Information Page";
 
         public static final String CARCINOGENIC_INTERVENTION = "Carcinogenic Interventions Page";
 
         public static final String PUBLICATIONS = "Publications page";
 
         public static final String HISTOPATHOLOGY = "Histopathology Page";
 
         public static final String THERAPEUTIC_APPROACHES = "Therapeutic Approaches Page";
 
         public static final String CELL_LINES = "Cell Lines Page";
 
         public static final String IMAGES = "Images Page";
 
         public static final String MICROARRAY = "Microarray Page";
 
         public static final String GENETIC_DESCRIPTION = "Genetic Description Page";
 
         public static final String TRANSPLANTATION = "Transplantation Page";
         
         public static final String TRANSIENT_INTERFERENCE = "Transient Interference Page";
     }
 
     /**
      * Used to determine the current model to edit on submission/edit also used
      * to display the name of the model and it's current status
      */
     public static final String MODELID = "modelid";
 
     public static final String MODELDESCRIPTOR = "modeldescriptor";
 
     public static final String MODELSTATUS = "modelstatus";
     
     // This constants stores the animal model species common name for all 
     // submission screens that have organ and diagnosis AND screens 
     // that display mgi, zfin, or rgd identifier - set in submitAction
     public static final String AMMODELSPECIESCOMMONNAME = "modelspeciescommonname";
     
     // Used to compare AMMODELSPECIESCOMMONNAME to Zebrafish in EvsTreeUtil.java
     public static final String ZEBRAFISH = "Zebrafish";
     
     // This constants stores the donor species common name for the Transplantation screens 
     // set in TransplantationPopulateAction
     public static final String DONORSPECIESCOMMONNAME = "donorspeciescommonname";
     
     // This constants stores the species common name from the simple and advanced search screens 
     // set in TransplantationPopulateAction
     public static final String SEARCHSPECIESCOMMONNAME = "searchspeciescommonname";    
 
     /**
      * Used to prepopulate forms
      */
     public static final String FORMDATA = "formdata";
 
     public static final String ANIMALMODEL = "animalmodel";
 
     public static final String TRANSPLANTATIONMODEL = "transplantationmodel";
 
     public static final String TRANSPLANTATIONRESULTLIST = "transplantationresultlist";
 
     /**
      * Used to store username for current user
      */
     public static final String CURRENTUSER = "camod.loggedon.username";
 
     public static final String CURRENTUSERROLES = "camod.loggedon.userroles";
 
     public static final String LOGINFAILED = "loginfailed";
     
     public static final String NOTLOGGEDIN = "notloggedin";
 
     /**
      * Used for search results
      */
     public static final String KEYWORD_HIGHLIGHT = "keywordhighlight";
     
     public static final String ADMIN_SEARCH_MODEL_ID = "adminsearchmodelid";    
     
     public static final String SEARCH_QUERY = "searchquery";
     
     public static final String SEARCH_RESULTS = "searchResults";
     
     public static final String DUP_NAME = "dupname";
     
     public static final String DUP_NAME_VALUE =  "dupnamevalue";
     
     public static final String QUERY_NAME = "queryname";
     
     public static final String ERRORMESSAGE = "errormessage";
     
     public static final String SEARCH_FORM = "searchform";
     
     // used for search results in adminEditModels
     public static final String CURATION_ASSIGNMENT_FORM = "curationassignmentform";    
     
     public static final String EXECUTE_TIME = "execute_time";
     
     public static final String ELAPSED_TIME = "elapsed_time";
     
     public static final String RERUN_QUERY = "rerunquery";
     
     public static final String USERSAVEDQUERYLIST = "usersavedquerylist";
     
     public static final String USERSQUERYLIST = "userquerylist";
     
     public static final String QUERYHISTORYID = "queryHistoryId"; 
     
     public static final String NUMBEROFSAVEDQUERIES = "numberofsavedqueries";    
     
     public static final String AQUERYID = "aqueryid";
     
     public static final String ASAVEDQUERYID = "asavedqueryid";
     
     public static final String DRUG_SCREEN_SEARCH_RESULTS = "drugScreenSearchResults";
 
     public static final String ADMIN_COMMENTS_SEARCH_RESULTS = "adminCommentsSearchResults";
 
     public static final String ADMIN_MODEL_SEARCH_RESULTS = "adminModelSearchResults";
     
     public static final String ADMIN_MODEL_ASSIGN_SEARCH_RESULTS = "adminModelAssignSearchResults";    
 
     public static final String ADMIN_ROLES_SEARCH_RESULTS = "adminRolesSearchResults";
 
     public static final String TRANSGENE_COLL = "transgeneColl";
 
     public static final String GENOMIC_SEG_COLL = "genomicSegColl";
 
     public static final String TARGETED_MOD_COLL = "targetedModColl";
 
     public static final String TARGETED_MOD_GENE_MAP = "targetedModGeneMap";
 
     public static final String INDUCED_MUT_COLL = "inducedMutColl";
 
     public static final String SPONTANEOUS_MUT_COLL = "spontaneousMutColl";
     
     public static final String TRANSGENE_CNT = "transgeneCnt";
 
     public static final String GENOMIC_SEG_CNT = "genomicSegCnt";
 
     public static final String TARGETED_MOD_CNT = "targetedModCnt";
 
     public static final String INDUCED_MUT_CNT = "inducedMutCnt";
     
     public static final String THERAPEUTIC_APPROACHES_COLL = "therapeuticApproachesColl";
     
     public static final String CLINICAL_PROTOCOLS = "clinProtocols";
     
     public static final String YEAST_DATA = "yeastData";
     
     public static final String INVIVO_DATA = "invivoData";
     
     public static final String PRECLINICAL_MODELS = "preClinicalModels";
     
     public static final String PUBLICATIONS = "publications";
     
     public static final String CARCINOGENIC_INTERVENTIONS_COLL = "carcinogenicInterventionColl";
     
     public static final String DRUG_SCREEN_OPTIONS = "drugScreenSearchOptions";
     
     public static final String NSC_NUMBER = "nsc";
     
     public static final String CAELMIR_STUDY_DATA = "caelmirStudyData";    
     
 
     // /////////////////////////////////////////////////////////////
     // Submission specific constants
     // /////////////////////////////////////////////////////////////
 
     public interface Submit {
 
         /**
          * Used to store required lists for the cardiogentic intervention
          * section of the sidebar menu of the submission section
          */    
         public static final String CHEMICALDRUG_LIST = "chemicaldrug_list";
 
         public static final String ENVIRONMENTALFACTOR_LIST = "environmentalfactor_list";
 
         public static final String GENEDELIVERY_LIST = "genedelivery_list";
 
         public static final String GROWTHFACTORS_LIST = "growthfactors_list";
 
         public static final String HORMONE_LIST = "hormone_list";
 
         public static final String NUTRITIONALFACTORS_LIST = "nutritionalfactors_list";
 
         public static final String RADIATION_LIST = "radiation_list";
 
         public static final String SURGERYOTHER_LIST = "surgeryother_list";
 
         public static final String VIRALTREATMENT_LIST = "viraltreatment_list";
 
         public static final String TRANSPLANTATION_LIST = "transplantation_list";
 
         public static final String SPONTANEOUSMUTATION_LIST = "spontaneousmutation_list";
 
         public static final String INDUCEDMUTATION_LIST = "inducedmutation_list";
 
         public static final String TARGETEDMODIFICATION_LIST = "targetedmodification_list";
 
         public static final String GENOMICSEGMENT_LIST = "genomicsegment_list";
 
         public static final String HISTOPATHOLOGY_LIST = "histopathology_list";
 
         public static final String ASSOCMETASTSIS_LIST = "associatedmetastatis_list";
 
         public static final String ENGINEEREDTRANSGENE_LIST = "engineeredtransgene_list";
 
         public static final String THERAPY_LIST = "therapy_list";
 
         public static final String CLINICALMARKER_LIST = "clinicalmarker_list";
 
         public static final String IMAGE_LIST = "image_list";
 
         public static final String ASSOCIATEDEXPRESSION_LIST = "associatedexpression_list";
         /**
          * Used to store a list of names for the Publication section of the
          * sidebar menu of the submission section
          */
         public static final String PUBLICATION_LIST = "publication_list";
 
         /**
          * Used to store a list of names for the Cell Line section of the
          * sidebar menu of the submission section
          */
         public static final String CELLLINE_LIST = "cellline_list";
         
         public static final String MICROARRAYDATA_LIST = "microarraydata_list";
 
         public static final String ANIMALAVAILABILITY_LIST = "availability_list";
 
         /**
          * Used to store animal model availability for the Model Availability
          * section of the sidebar menu of the submission section
          */
         public static final String INVESTIGATOR_LIST = "investigator_list";
         public static final String JACKSONLAB_LIST = "jacksonlab_list";
         public static final String MMHCC_LIST = "mmhcc_list";
         public static final String ZFIN_LIST = "zfin_list";
         
         /**
          * Used to store animal model availability for the Transient Interference 
          * section of the sidebar menu of the submission section
          */        
         public static final String MORPHOLINO_LIST = "morpholino_list";
         public static final String SIRNA_LIST = "sirna_list";
     }
 
     // /////////////////////////////////////////////////////////////
     // Admin specific constants
     // /////////////////////////////////////////////////////////////
 
     public interface Admin {
 
         /**
          * Defines the different roles in the system
          */
         public interface Roles {
 
             /**
              * A constant that defines the submitter role
              */
             public static final String ALL = "All";
 
             /**
              * A constant that defines the submitter role
              */
             public static final String SUBMITTER = "Public Submitter";
 
             /**
              * A constant that defines the coordinator role
              */
             public static final String COORDINATOR = "MMHCC Coordinator";
 
             /**
              * A constant that defines the coordinator role
              */
             public static final String SUPER_USER = "MMHCC SuperUser";
             
             /**
              * A constant that defines the Editor role
              */
             public static final String EDITOR = "MMHCC Editor";
 
             /**
              * A constant that defines the screener role
              */
             public static final String SCREENER = "MMHCC Screener";
         }
 
         /**
          * Defines the different roles in the system
          */
         public interface Actions {
             /**
              * A constant that defines the text for the generic approved action
              */
             public static final String SCREENER_APPROVE = "screener_approve";
 
             /**
              * A constant that defines the text for the generic approved action
              */
             public static final String EDITOR_APPROVE = "editor_approve";
             
             /**
              * A constant that defines the text for the assign editor action
              */
             public static final String ASSIGN_EDITOR = "assign_editor";
 
             /**
              * A constant that defines the text for the assign screener action
              */
             public static final String ASSIGN_SCREENER = "assign_screener";
 
             /**
              * A constant that defines the text for the need more information
              * action
              */
             public static final String NEED_MORE_INFO = "need_more_info";
 
             /**
              * A constant that defines the text for the generic reject action
              */
             public static final String SCREENER_REJECT = "screener_reject";
 
             /**
              * A constant that defines the text for the complete action
              */
             public static final String COMPLETE = "complete";
 
             /**
              * A constant that defines the text for the complete action from submitOverview
              */
             public static final String BACK_TO_COMPLETE = "back_to_complete";
 
             /**
              * A constant that defines the text for the screener_approve action from submitOverview
              */            
             public static final String BACK_TO_SCREENER_APPROVE = "back_to_screener_approve";
 
             /**
              * A constant that defines the text for the inactive action
              */
             public static final String INACTIVATE = "inactivate";            
         }
         
         /**
          * Defines the different states for a model
          */
         public interface ModelState {
         	
             /**
              * A constant that defines the text for the inactive model state
              */
             public static final String INACTIVE = "Inactive";
             
             /**
              * A constant that defines the text for the Incomplete model state
              * used to set state after duplicating a model from adminEditModels and submitModels
              */
             public static final String INCOMPLETE = "Incomplete";            
         }
 
         /**
          * A constant that defines string used as a variable name in e-mail
          */
         public static final String INDUCED_MUTATION_AGENT_NAME = "inducedmutationagentname";
 
         /**
          * A constant that defines string used as a variable name in e-mail
          */
         public static final String INDUCED_MUTATION_AGENT_TYPE = "inducedmutationagenttype";
 
         /**
          * A constant that defines string used as key for e-mail content
          * associated with induced mutation agent additions
          */
         public static final String INDUCED_MUTATION_AGENT_ADDED = "inducedmutationagentadded";
 
         /**
          * A constant that defines string used as a variable name in e-mail
          */
         public static final String TARGETED_MODIFICATION_NAME = "targetedmodificationname";
 
         /**
          * A constant that defines string used as a variable name in e-mail
          */
         public static final String TARGETED_MODIFICATION_TYPE = "targetedmodificationtype";
 
         /**
          * A constant that defines string used as key for e-mail content
          * associated with targeted modification additions
          */
         public static final String TARGETED_MODIFICATION_ADDED = "targetedmodificationadded";
 
         /**
          * A constant that defines string used as key for e-mail content
          * associated with non-controlled vocabulary use
          */
         public static final String NONCONTROLLED_VOCABULARY = "noncontrolledvocab";
 
         /**
          * A constant that defines what file is used for the model curation
          * process
          */
         public static final String MODEL_CURATION_WORKFLOW = "config/CurationConfig.xml";
 
         /**
          * A constant that defines what file is used for the comment curation
          * process
          */
         public static final String COMMENT_CURATION_WORKFLOW = "config/CommentCurationConfig.xml";
 
         /**
          * Used to set/pull the objects needing to be reviewed out of the
          * request
          */
         public static final String COMMENTS_NEEDING_REVIEW = "commentsNeedingReview";
 
         /**
          * Used to set/pull the objects needing to be reviewed out of the
          * request
          */
         public static final String COMMENTS_NEEDING_ASSIGNMENT = "commentsNeedingAssignment";
 
         /** Used to set/pull the objects needing to be edited out of the request */
         public static final String MODELS_NEEDING_EDITING = "modelsNeedingEditing";
 
         /**
          * Used to set/pull the objects needing to be assigned an editor out of
          * the request
          */
         public static final String MODELS_NEEDING_EDITOR_ASSIGNMENT = "modelsNeedingEditorAssignment";
 
         /** Used to set/pull the objects needing to be edited out of the request */
         public static final String MODELS_NEEDING_MORE_INFO = "modelsNeedingMoreInfo";
 
         /**
          * Used to set/pull the objects needing to be screened out of the
          * request
          */
         public static final String MODELS_NEEDING_SCREENING = "modelsNeedingScreening";
 
         /**
          * Used to set/pull the objects needing to be assigned a screener out of
          * the request
          */
         public static final String MODELS_NEEDING_SCREENER_ASSIGNMENT = "modelsNeedingScreenerAssignment";
         /**
          * Used to set/pull the objects inactivated out of
          * the request
          */
         public static final String MODELS_INACTIVATED = "modelsInactivated";
         
     }
 
     public interface EmailMessage {
 
         public static final String SENDER = "email.sender";
 
         public static final String RECIPIENTS = "email.recipients";
 
         public static final String FROM = "email.from";
 
         public static final String MESSAGE = "email.message";
 
         public static final String SUBJECT = "email.subject";
 
     }
 
     /**
      * 
      * Constants used for fetching EVS data
      * 
      */
     public interface Evs {
 
         /**
          * The namespace to fetch the concepts from
          */
         public static final String NCI_SCHEMA = "NCI_Thesaurus";
         
         /**
          * The namespace to fetch the concepts from
          */
         public static final String ZEBRAFISH_SCHEMA = "Zebrafish";        
 
         /**
          * The tag used to get the display name
          */
         public static final String DISPLAY_NAME_TAG = "Display_Name";
         
         public static final String PREFERRED_NAME_TAG = "Preferred_Name";        
 
         /**
          * The key for the URI in the camod.properties file
          */
         public static final String URI_KEY = "evs.uri";
     }
     
     /**
      * 
      * Constants used for fetching EVS data
      * 
      */
     public interface OnlineHelp {
 
         /**
          * The namespace to fetch the concepts from
          */
         public static final String SKIP = "skip";
     }
 }
