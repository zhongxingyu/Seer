 package eu.dm2e.ws;
 
 import eu.dm2e.ws.api.JobPojo;
 import eu.dm2e.ws.api.ParameterAssignmentPojo;
 import eu.dm2e.ws.api.ParameterConnectorPojo;
 import eu.dm2e.ws.api.WebserviceConfigPojo;
 import eu.dm2e.ws.api.WorkflowConfigPojo;
 import eu.dm2e.ws.api.WorkflowJobPojo;
 import eu.dm2e.ws.api.WorkflowPositionPojo;
 
 
 
 /**
  * @author Konstantin Baierer
  *
  */
 public final class NS {
 	
 	/**
 	 * Single place to collect all OmNom property and class names. 
 	 * 
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class OMNOM {
 		
 		public static final String BASE = "http://onto.dm2e.eu/omnom/";
 		
 		/**
 		 * Abstract Jobs
 		 */
 		public static final String PROP_LOG_ENTRY = BASE + "hasLogEntry";
 		public static final String PROP_JOB_STATUS = BASE + "status";
 		
 		public static final String PROP_ASSIGNMENT = BASE + "assignment";
 		public static final String PROP_WEBSERVICE = BASE + "webservice";
 		/**
 		 * 
 		 * @see JobPojo
 		 * @see WebserviceConfigPojo
 		 */
 		public static final String PROP_WEBSERVICE_CONFIG = BASE + "webserviceConfig";
 		
 		/**
 		 * 
 		 * @see ParameterConnectorPojo
 		 * @see ParameterAssignmentPojo
 		 */
 		public static final String PROP_FOR_PARAM = BASE + "forParam";
 		/**
 		 * 
 		 * @link WorkflowConfigPojo
 		 */
 		public static final String PROP_HAS_POSITION = BASE + "hasPosition";
 		/**
 		 * 
 		 * @see WorkflowConfigPojo#getParameterSlots()
 		 */
 		public static final String PROP_PARAMETER_CONNECTOR = BASE + "parameterConnector";
 		/**
 		 * 
 		 * @see WorkflowJobPojo#getConnectorAssignments()
 		 */
 		public static final String PROP_CONNECTOR_ASSIGNMENT = BASE + "parameterConnectorAssignment";
 		
 		/**
 		 * 
 		 * @see WorkflowPositionPojo
 		 */
 		public static final String PROP_IN_WORKFLOW = BASE + "inWorkflow";
 		
 		/**
 		 * 
 		 * @see ParameterAssignmentPojo
 		 */
 		public static final String PROP_PARAMETER_VALUE = BASE + "parameterValue";
 		
 		/**
 		 * 
 		 * @see WorkflowJobPojo#getWorkflowConfig()
 		 */
 		public static final String PROP_WORKFLOW_CONFIG = BASE + "workflowConfig";
 		
 		public static final String PROP_INPUT_PARAM = BASE + "inputParam";
 		public static final String PROP_OUTPUT_PARAM = BASE + "outputParam";
 		
		public static final String PROP_FROM_POSITION = BASE + "forPosition";
		public static final String PROP_TO_POSITION = BASE + "toPosition";
		
 		public static final String PROP_FROM_PARAM = BASE + "fromParam";
 		public static final String PROP_TO_PARAM = BASE + "toParam";
		public static final String PROP_FROM_STRING = BASE + "fromString";
 		
		public static final String PROP_FROM_WORKFLOW = BASE + "fromWorkflow";
 		
 		public static final String PROP_WORKFLOW = BASE + "workflow";
 		
 		public static final String PROP_IS_REQUIRED = BASE + "isRequired";
 		
 		public static final String PROP_PARAMETER_TYPE = BASE + "parameterType";
 		/**
 		 * 
 		 * @see WorkflowJobPojo
 		 */
 		public static final String CLASS_WORKFLOW_JOB = BASE + "WorkflowJob";
 		
 		/**
 		 * 
 		 * @see WorkflowConfigPojo
 		 */
 		
 		public static final String CLASS_PARAMETER = BASE + "Parameter";
 		public static final String CLASS_PARAMETER_CONNECTOR = BASE + "ParameterConnector";
 		
 		public static final String CLASS_WEBSERVICE = BASE + "Webservice";
 		public static final String CLASS_WEBSERVICE_CONFIG = BASE + "WebserviceConfig";
 		public static final String CLASS_WORKFLOW = BASE + "Workflow";
 		public static final String CLASS_WORKFLOW_POSITION = BASE + "WorkflowPosition";
 		public static final String CLASS_WORKFLOW_CONFIG = BASE + "WorkflowConfig";
 		public static final String CLASS_WORKFLOW_PARAMETER_CONNECTOR = BASE + "WorkflowParameterConnector";
 
 		public static final String PROP_FILE_LOCATION = BASE + "fileLocation";
 
 		public static final String PROP_FILE_STATUS = BASE + "fileStatus";
 
 		public static final String PROP_FILE_EDIT_URI = BASE + "fileEditURI";
 
 		public static final String PROP_MD5 = BASE + "md5";
 
 		public static final String PROP_FILE_RETRIEVAL_URI = BASE + "fileRetrievalURI";
 
 		public static final String CLASS_JOB = BASE + "Job";
 
 		public static final String CLASS_PARAMETER_ASSIGNMENT = BASE + "ParameterAssignment";
 
 		public static final String CLASS_FILE = BASE + "File";
 
 		public static final String CLASS_LOG_ENTRY = BASE + "LogEntry";
 
 		public static final String PROP_LOG_MESSAGE = BASE + "hasLogMessage";
 		
 		public static final String PROP_LOG_LEVEL = BASE + "hasLogLevel";
 
 		public static final String PROP_FOR_SLOT = BASE + "forSlot";
 		public static final String CLASS_PARAMETER_CONNECTOR_ASSIGNMENT = BASE + "ParameterConnectorAssignment";
 
		public static final String PROP_TO_WORKFLOW = BASE + "toWorkflow";
 	}
 	
 	/**
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class RDFS {
 		public static final String BASE = "http://www.w3.org/2000/01/rdf-schema#";
 		
 		public static final String PROP_LABEL = BASE + "label";
 		public static final String PROP_COMMENT = BASE + "comment";
 		
 	}
 	
 	/**
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class CO {
 		
 		public static final String BASE = "http://purl.org/co/";
 		
 		public static final String PROP_ITEM_CONTENT = BASE + "itemContent";
 		public static final String PROP_SIZE		   = BASE + "size";
 		public static final String PROP_INDEX        = BASE + "index";
 		public static final String PROP_ITEM  	   = BASE + "item";
 		public static final String PROP_FIRST_ITEM   = BASE + "firstItem";
 		public static final String PROP_NEXT_ITEM   = BASE + "nextItem";
 		public static final String PROP_LAST_ITEM   = BASE + "lastItem";
 
 		public static final String CLASS_LIST = BASE + "List";
 		public static final String CLASS_ITEM = BASE + "Item";
 	}
 	
 	/**
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class RDF {
 		
 		public static final String BASE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
 		
 		public static final String PROP_TYPE = BASE + "type";
 	}
 	
 	public static final class SKOS {
 		
 		public static final String BASE = "http://www.w3.org/2004/02/skos/core#";
 		
 		public static final String PROP_LABEL = BASE + "label";
 	}
 	
 	/**
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class DC {
 		
 		public static final String BASE = "http://purl.org/dc/elements/1.1/";
 		
 		public static final String PROP_TITLE = BASE + "title";
 		
 		public static final String PROP_DATE = BASE + "date";
 		
 	}
 	
 	/**
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class DCTERMS {
 		
 		public static final String BASE = "http://purl.org/dc/terms/";
 		
 		public static final String PROP_FORMAT = BASE + "format";
 
 		public static final String PROP_EXTENT = BASE + "extent";
 		
 	}
 		
 	/**
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class PROV {
 		
 		public static final String BASE = "http://www.w3.org/ns/prov#";
 		
 		public static final String PROP_WAS_GENERATED_BY = BASE + "wasGeneratedBy";
 
 		public static final String PROP_SPECIALIZATION_OF = BASE  + "specializationOf";
 		public static final String PROP_WAS_REVISION_OF = BASE  + "wasRevisionOf";
 	}
 	
 	public static final class XSD {
 		
 		public static final String BASE = "http://www.w3.org/2001/XMLSchema#";
 		
 		public static final String INT = BASE + "int";
 	}
 	
 	/**
 	 * @author Konstantin Baierer
 	 *
 	 */
 	public static final class VOID {
 		public static final String BASE = "http://rdfs.org/ns/void#";
 		
 		public static final String CLASS_DATASET = BASE + "Dataset";
 	}
 	
 	public static final String
             NS_OMNOM = Config.getString("dm2e.ns.dm2e")
 //			, RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 			, DM2ELOG = "http://onto.dm2e.eu/logging#"
 			, ENDPOINT_SELECT = Config.getString("dm2e.ws.sparql_endpoint")
 			, ENDPOINT_UPDATE = Config.getString("dm2e.ws.sparql_endpoint_statements")
 			;
 //	public static final String[] NAMESPACE_MAP;
 //	
 //	static {
 //		HashMap<String, String> map = new HashMap<>();
 //		map.put("omnom", OMNOM.BASE);
 //		map.put("skos", SKOS.BASE);
 //		map.put("dc", DC.BASE);
 //		map.put("dcterms", DCTERMS.BASE);
 //		map.put("dct", DCTERMS.BASE);
 //		map.put("prov", PROV.BASE);
 //		map.put("rdf", RDF.BASE);
 //		map.put("rdfs", RDFS.BASE);
 //		map.put("co", CO.BASE);
 //		NAMESPACE_MAP = (String[]) Arrays.asList(map).toArray();
 //	}
 }
