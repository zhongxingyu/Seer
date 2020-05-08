 /**
  * 
  */
 package com.github.podd.utils;
 
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 
 /**
  * Interface containing URI constants for the Ontologies needed in PODD.
  * 
  * @author kutila
  * 
  */
 public interface PoddRdfConstants
 {
     public static final ValueFactory VF = ValueFactoryImpl.getInstance();
     
     /** Path to default alias file */
     public static final String PATH_DEFAULT_ALIASES_FILE = "/com/github/podd/api/file/default-file-repositories.ttl";
     
     /** Path to dcTerms.owl */
     public static final String PATH_PODD_DCTERMS = "/ontologies/dcTerms.owl";
     
     /** Path to foaf.owl */
     public static final String PATH_PODD_FOAF = "/ontologies/foaf.owl";
     
     /** Path to poddUser.owl */
     public static final String PATH_PODD_USER = "/ontologies/poddUser.owl";
     
     /** Path to poddBase.owl */
     public static final String PATH_PODD_BASE = "/ontologies/poddBase.owl";
     
     /** Path to poddScience.owl */
     public static final String PATH_PODD_SCIENCE = "/ontologies/poddScience.owl";
     
     /** Path to poddPlant.owl */
     public static final String PATH_PODD_PLANT = "/ontologies/poddPlant.owl";
     
     /** Path to poddAnimal.owl */
     public static final String PATH_PODD_ANIMAL = "/ontologies/poddAnimal.owl";
     
     /**
      * Path to poddDataRepository.owl.
      * 
      * This ontology is NOT part of the standard schema ontologies. It is a separate ontology used
      * to validate Data Repository configurations.
      */
     public static final String PATH_PODD_DATA_REPOSITORY = "/ontologies/poddDataRepository.owl";
     
     public static final String PODD_DCTERMS = "http://purl.org/podd/ns/dcTerms#";
     
     public static final String PODD_FOAF = "http://purl.org/podd/ns/foaf#";
     
     public static final String PODD_USER = "http://purl.org/podd/ns/poddUser#";
     
     public static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";
     
     public static final String PODD_SCIENCE = "http://purl.org/podd/ns/poddScience#";
     
     public static final String PODD_PLANT = "http://purl.org/podd/ns/poddPlant#";
     
     public static final String DATA_REPOSITORY = "http://purl.org/podd/ns/dataRepository#";
     
     /**
      * An arbitrary prefix to use for automatically assigning ontology IRIs to inferred ontologies.
      * There are no versions delegated to inferred ontologies, and the ontology IRI is generated
      * using the version IRI of the original ontology, which must be unique.
      */
     public static final String INFERRED_PREFIX = "urn:podd:inferred:ontologyiriprefix:";
     
     /** Default value is urn:podd:default:artifactmanagementgraph: */
     public static final URI DEFAULT_ARTIFACT_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:artifactmanagementgraph:");
     
     /** Default value is urn:podd:default:schemamanagementgraph */
     public static final URI DEFAULT_SCHEMA_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:schemamanagementgraph");
     
     /** Default value is urn:podd:default:usermanagementgraph: */
     public static final URI DEF_USER_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:usermanagementgraph:");
     
     public static final URI DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:filerepositorymanagementgraph:");
     
     public static final URI OWL_MAX_QUALIFIED_CARDINALITY = PoddRdfConstants.VF
             .createURI("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
     
     public static final URI OWL_MIN_QUALIFIED_CARDINALITY = PoddRdfConstants.VF
             .createURI("http://www.w3.org/2002/07/owl#minQualifiedCardinality");
     
     public static final URI OWL_QUALIFIED_CARDINALITY = PoddRdfConstants.VF
             .createURI("http://www.w3.org/2002/07/owl#qualifiedCardinality");
     
     public static final URI OWL_VERSION_IRI = PoddRdfConstants.VF.createURI(OWL.NAMESPACE, "versionIRI");
     
     /**
      * The OMV vocabulary defines a property for the current version of an ontology, so we are
      * reusing it here.
      */
     public static final URI OMV_CURRENT_VERSION = PoddRdfConstants.VF.createURI("http://omv.ontoware.org/ontology#",
             "currentVersion");
     
     /**
      * Creating a property for PODD to track the currentInferredVersion for the inferred axioms
      * ontology when linking from the ontology IRI.
      */
     public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "currentInferredVersion");
     
     public static final String HTTP = "http://www.w3.org/2011/http#";
     
     /** http://www.w3.org/2011/http#statusCodeValue */
     public static final URI HTTP_STATUS_CODE_VALUE = ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.HTTP,
             "statusCodeValue");
     
     /** http://www.w3.org/2011/http#reasonPhrase */
     public static final URI HTTP_REASON_PHRASE = ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.HTTP,
             "reasonPhrase");
     
     public static final URI PODD_REPLACED_TEMP_URI_WITH = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "replacedTempUriWith");;
     
     /**
      * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
      * a particular versioned ontology.
      */
     public static final URI PODD_BASE_INFERRED_VERSION = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "inferredVersion");
     
     public static final URI PODD_BASE_HAS_PUBLICATION_STATUS = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "hasPublicationStatus");
     
     public static final URI PODD_BASE_HAS_TOP_OBJECT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "artifactHasTopObject");
     
     public static final URI PODD_BASE_PUBLISHED = PoddRdfConstants.VF
             .createURI(PoddRdfConstants.PODD_BASE, "Published");
     
     public static final URI PODD_BASE_NOT_PUBLISHED = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "NotPublished");
     
     public static final URI PODD_BASE_WEIGHT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "weight");
     
     public static final URI PODD_BASE_DO_NOT_DISPLAY = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "doNotDisplay");
     
     public static final URI PODD_BASE_CONTAINS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "contains");
     
     /** http://purl.org/podd/ns/poddBase#lastModified */
     public static final URI PODD_BASE_LAST_MODIFIED = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "lastModified");
     
     /** http://purl.org/podd/ns/poddBase#createdAt */
     public static final URI PODD_BASE_CREATED_AT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "createdAt");
     
     /** http://purl.org/podd/ns/poddBase#hasDisplayType */
     public static final URI PODD_BASE_DISPLAY_TYPE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasDisplayType");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_ShortText */
     public static final URI PODD_BASE_DISPLAY_TYPE_SHORTTEXT = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "DisplayType_ShortText");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_LongText */
     public static final URI PODD_BASE_DISPLAY_TYPE_LONGTEXT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_LongText");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_DropDownList */
     public static final URI PODD_BASE_DISPLAY_TYPE_DROPDOWN = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_DropDownList");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_CheckBox */
     public static final URI PODD_BASE_DISPLAY_TYPE_CHECKBOX = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_CheckBox");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_Table */
     public static final URI PODD_BASE_DISPLAY_TYPE_FIELDSET = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_FieldSet");
     
     /** http://purl.org/podd/ns/poddBase#hasAllowedValue */
     public static final URI PODD_BASE_HAS_ALLOWED_VALUE = ValueFactoryImpl.getInstance().createURI(
             PoddRdfConstants.PODD_BASE, "hasAllowedValue");
     
     // ----- custom representation of cardinalities -----
     
     /**
      * http://purl.org/podd/ns/poddBase#hasCardinality. Represents a <b>hasCardinality</b> property.
      */
     public static final URI PODD_BASE_HAS_CARDINALITY = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasCardinality");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Exactly_One */
     public static final URI PODD_BASE_CARDINALITY_EXACTLY_ONE = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_Exactly_One");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many */
     public static final URI PODD_BASE_CARDINALITY_ONE_OR_MANY = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_One_Or_Many");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_One */
     public static final URI PODD_BASE_CARDINALITY_ZERO_OR_ONE = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_Zero_Or_One");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_Many */
     public static final URI PODD_BASE_CARDINALITY_ZERO_OR_MANY = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_Zero_Or_Many");
     
     // ----- file reference constants -----
     
     /** http://purl.org/podd/ns/poddBase#hasDataReference */
     public static final URI PODD_BASE_HAS_DATA_REFERENCE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasDataReference");
     
     /** http://purl.org/podd/ns/poddBase#hasFileName */
     public static final URI PODD_BASE_HAS_FILENAME = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasFileName");
     
     /** http://purl.org/podd/ns/poddBase#hasPath */
     public static final URI PODD_BASE_HAS_FILE_PATH = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasPath");
     
     /** http://purl.org/podd/ns/poddBase#hasSPARQLGraph */
     public static final URI PODD_BASE_HAS_SPARQL_GRAPH = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasSPARQLGraph");
     
     /** http://purl.org/podd/ns/poddBase#hasSPARQLEndpoint */
     public static final URI PODD_BASE_HAS_SPARQL_ENDPOINT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasSPARQLEndpoint");
     
     /**
      * http://purl.org/podd/ns/poddBase#hasAlias.
      * 
      * This property is used to specify an "alias" value found inside a DataReference.
      */
     public static final URI PODD_BASE_HAS_ALIAS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "hasAlias");
     
     /** http://purl.org/podd/ns/poddBase#DataReference */
     public static final URI PODD_BASE_DATA_REFERENCE_TYPE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DataReference");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileReference */
     public static final URI PODD_BASE_FILE_REFERENCE_TYPE_SSH = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "SSHFileReference");
     
    /** http://purl.org/podd/ns/poddBase#SSHFileReference */
    public static final URI PODD_BASE_DATA_REFERENCE_TYPE_SPARQL = PoddRdfConstants.VF.createURI(
            PoddRdfConstants.PODD_BASE, "SPARQLDataReference");
    
     // ----- file repository constants -----
     
     /** http://purl.org/podd/ns/poddBase#DataRepository */
     public static final URI PODD_DATA_REPOSITORY = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "DataRepository");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileRepository */
     public static final URI PODD_SSH_FILE_REPOSITORY = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "SSHFileRepository");
     
     /** http://purl.org/podd/ns/poddBase#HTTPFileRepository */
     public static final URI PODD_HTTP_FILE_REPOSITORY = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "HTTPFileRepository");
     
     /**
      * http://purl.org/podd/ns/poddBase#hasDataRepositoryAlias
      * 
      * This property is ONLY used in the Data Repository management implementations.
      */
     public static final URI PODD_DATA_REPOSITORY_ALIAS = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryAlias");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryProtocol */
     public static final URI PODD_DATA_REPOSITORY_PROTOCOL = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryProtocol");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryHost */
     public static final URI PODD_DATA_REPOSITORY_HOST = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "hasDataRepositoryHost");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryPort */
     public static final URI PODD_DATA_REPOSITORY_PORT = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "hasDataRepositoryPort");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryFingerprint */
     public static final URI PODD_FILE_REPOSITORY_FINGERPRINT = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryFingerprint");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryUsername */
     public static final URI PODD_FILE_REPOSITORY_USERNAME = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryUsername");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositorySecret */
     public static final URI PODD_FILE_REPOSITORY_SECRET = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositorySecret");
     
 }
