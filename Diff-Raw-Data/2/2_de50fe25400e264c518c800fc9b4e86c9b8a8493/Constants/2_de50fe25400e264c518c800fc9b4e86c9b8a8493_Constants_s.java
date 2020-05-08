 package gov.nih.nci.camod;
 
 /**
  * Constant values used throughout the application.
  * 
  * <p>
  * <a href="Constants.java.html"><i>View Source</i></a>
  * </p>
  */
 public class Constants {
     //~ Static fields/initializers =============================================
    
     /** The name of the ResourceBundle used in this application */
     public static final String BUNDLE_KEY = "ApplicationResources";
     
     /** The application scoped attribute for persistence engine used */
     public static final String DAO_TYPE = "daoType";
     public static final String DAO_TYPE_HIBERNATE = "hibernate";
 
     /** Application scoped attribute for authentication url */
     public static final String AUTH_URL = "authURL";
 
     /** Application scoped attributes for SSL Switching */
     public static final String HTTP_PORT = "httpPort";
     public static final String HTTPS_PORT = "httpsPort";
 
     /** The application scoped attribute for indicating a secure login */
     public static final String SECURE_LOGIN = "secureLogin";
 
     /** The encryption algorithm key to be used for passwords */
     public static final String ENC_ALGORITHM = "algorithm";
 
     /** A flag to indicate if passwords should be encrypted */
     public static final String ENCRYPT_PASSWORD = "encryptPassword";
 
     /** File separator from System properties */
     public static final String FILE_SEP = System.getProperty("file.separator");
 
     /** User home from System properties */
     public static final String USER_HOME =
         System.getProperty("user.home") + FILE_SEP;
 
     /**
      * The session scope attribute under which the breadcrumb ArrayStack is
      * stored
      */
     public static final String BREADCRUMB = "breadcrumbs";
 
     /**
      * The session scope attribute under which the User object for the
      * currently logged in user is stored.
      */
     public static final String USER_KEY = "currentUserForm";
 
     /**
      * The request scope attribute under which an editable user form is stored
      */
     public static final String USER_EDIT_KEY = "userForm";
 
     /**
      * The request scope attribute that holds the user list
      */
     public static final String USER_LIST = "userList";
 
     /**
      * The request scope attribute for indicating a newly-registered user
      */
     public static final String REGISTERED = "registered";
 
     /**
      * The name of the Administrator role, as specified in web.xml
      */
     public static final String ADMIN_ROLE = "admin";
 
     /**
      * The name of the User role, as specified in web.xml
      */
     public static final String USER_ROLE = "tomcat";
 
     /**
      * The name of the user's role list, a request-scoped attribute
      * when adding/editing a user.
      */
     public static final String USER_ROLES = "userRoles";
 
     /**
      * The name of the available roles list, a request-scoped attribute
      * when adding/editing a user.
      */
     public static final String AVAILABLE_ROLES = "availableRoles";
 
     /**
      * Name of cookie for "Remember Me" functionality.
      */
     public static final String LOGIN_COOKIE = "sessionId";
 
     /**
      * The name of the configuration hashmap stored in application scope.
      */
     public static final String CONFIG = "appConfig";
     
     /**
      * The request scope attribute that holds the person form.
      */
     public static final String PERSON_KEY = "personForm";
 
     /**
      * The request scope attribute that holds the person list
      */
     public static final String PERSON_LIST = "personList";
   
     /**
      * The request scope attribute that holds the login results
      */
     public static final String FAILURE = "failure";
     
     /**
      * The request scope attribute that holds the login results
      */
     public static final String SUCCESS = "success";
     
     public static final String UPT_CONTEXT_NAME = "camod";
     
     /** 
      * Used to store list of models currently logged on user has previous entered
      */
     public static final String USERMODELLIST = "usermodellist";
     
     /**
      *  Used to store lists for drop down menus
      */
     public interface Dropdowns {
         public static final String SPECIESDROP = "speciesdrop.db";
         public static final String STRAINDROP = "straindrop.db";
         public static final String SEXDISTRIBUTIONDROP = "SexDistributions.txt";
         public static final String DOSAGEUNITSDROP = "DoseUnits.txt";
         public static final String ADMINISTRATIVEROUTEDROP = "adminroutedrop.db";
         public static final String AGEUNITSDROP = "AgeUnits.txt";
         
         //Specific to a single screen
         public static final String CHEMICALDRUGDROP = "chemdrugdrop.db";
         public static final String ENVIRONFACTORDROP = "envfactordrop.db";
         public static final String GROWTHFACTORDROP = "growfactordrop.db";
         public static final String HORMONEDROP = "hormonedrop.db";
         public static final String NUTRITIONFACTORDROP = "nutritionfactordrop.db";
        public static final String RADIATIONDROP = "radiationdrop";
         public static final String SURGERYDROP = "surgerydrop.db";
         public static final String VIRUSDROP = "virusdrop.db";	
     }    
     
     /**
      * Used to determine the current model to edit on submission/edit
      * also used to display the name of the model and it's current status
      */
     public static final String MODELID = "modelid";
     public static final String MODELDESCRIPTOR = "modeldescriptor";
     public static final String MODELSTATUS = "modelstatus";
     
     /**
      * Used to prepopulate forms
      */
     public static final String FORMDATA = "formdata";
     public static final String ANIMALMODEL = "animalmodel";
     
     /**
      * Used to store username for current user 
      */
     public static final String CURRENTUSER = "camod.loggedon.username";
     public static final String CURRENTUSERROLES = "camod.loggedon.userroles";
     public static final String LOGINFAILED = "loginfailed";
 
     /**
 	 * Used for search results
 	 */
     public static final String SEARCH_RESULTS = "searchResults";
     public static final String TRANSGENE_COLL = "transgeneColl";
     public static final String GENOMIC_SEG_COLL = "genomicSegColl";
     public static final String TARGETED_MOD_COLL = "targetedModColl";
     public static final String INDUCED_MUT_COLL = "inducedMutColl";
     public static final String TRANSGENE_CNT = "transgeneCnt";
     public static final String GENOMIC_SEG_CNT = "genomicSegCnt";
     public static final String TARGETED_MOD_CNT = "targetedModCnt";
     public static final String INDUCED_MUT_CNT = "inducedMutCnt";
 
 
     // /////////////////////////////////////////////////////////////
     // Submission specific constants
     // /////////////////////////////////////////////////////////////
     
     public interface Submit {
     	
     	/**
     	 * Used to store a list of names for the cardiogentic intervention section of the sidebar menu of the submission section
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
     }
     
     // /////////////////////////////////////////////////////////////
     // Admin specific constants
     // /////////////////////////////////////////////////////////////
 
     public interface Admin {
 
         /**
          * Defines the different roles in the system
          */
         public interface Roles
         {
             /**
              * A constant that defines the controller role
              */
             public static final String CONTROLLER = "Controller";
             
             /**
              * A constant that defines the Editor role
              */
             public static final String EDITOR = "Editor";
             
             /**
              * A constant that defines the screener role
              */
             public static final String SCREENER = "Screener";
         }
         
         /**
          * Defines the different roles in the system
          */
         public interface Actions
         {
             /**
              * A constant that defines the text for the generic approved action
              */
             public static final String APPROVE = "approve";
             
             /**
              * A constant that defines the text for the assign editor action
              */
             public static final String ASSIGN_EDITOR = "assign_editor";
             
             /**
              * A constant that defines the text for the assign screener action
              */
             public static final String ASSIGN_SCREENER = "assign_screener";
             
             /**
              * A constant that defines the text for the need more information action
              */
             public static final String NEED_MORE_INFO = "need_more_info";
             
             /**
              * A constant that defines the text for the generic reject action
              */
             public static final String REJECT = "reject";           
         }
         
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
     }
 }
