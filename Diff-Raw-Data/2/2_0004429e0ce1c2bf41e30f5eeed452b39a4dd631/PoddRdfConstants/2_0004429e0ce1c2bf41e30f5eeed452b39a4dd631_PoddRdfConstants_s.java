 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
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
     
     public static final String DATA_REPOSITORY = "http://purl.org/podd/ns/dataRepository#";
     
     /** Default value is urn:podd:default:usermanagementgraph: */
     public static final URI DEFAULT_USER_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:usermanagementgraph:");
     
     /** Default value is urn:podd:default:artifactmanagementgraph: */
     public static final URI DEFAULT_ARTIFACT_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:artifactmanagementgraph:");
     
     public static final URI DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:filerepositorymanagementgraph:");
     
     /** Default value is urn:podd:default:schemamanagementgraph */
     public static final URI DEFAULT_SCHEMA_MANAGEMENT_GRAPH = PoddRdfConstants.VF
             .createURI("urn:podd:default:schemamanagementgraph");
     
     /** http://purl.org/podd/ns/err#contains */
     public static final URI ERR_CONTAINS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_ERROR, "contains");
     
     /** The Exception class http://purl.org/podd/ns/err#exceptionClass */
     public static final URI ERR_EXCEPTION_CLASS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_ERROR,
             "exceptionClass");
     
     /** What/who identified the error http://purl.org/podd/ns/err#identifier */
     public static final URI ERR_IDENTIFIER = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_ERROR, "identifier");
     
     /** Error source/what caused the error http://purl.org/podd/ns/err#source */
     public static final URI ERR_SOURCE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_ERROR, "source");
     
     /** Type to identify an Error http://purl.org/podd/ns/err#Error */
     public static final URI ERR_TYPE_ERROR = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_ERROR, "Error");
     
     /** Type to identify the TopError http://purl.org/podd/ns/err#TopError */
     public static final URI ERR_TYPE_TOP_ERROR = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_ERROR, "TopError");
     
     public static final String HTTP = "http://www.w3.org/2011/http#";
     
     /** http://www.w3.org/2011/http#reasonPhrase */
     public static final URI HTTP_REASON_PHRASE = ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.HTTP,
             "reasonPhrase");
     
     /** http://www.w3.org/2011/http#statusCodeValue */
     public static final URI HTTP_STATUS_CODE_VALUE = ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.HTTP,
             "statusCodeValue");
     
     /**
      * An arbitrary prefix to use for automatically assigning ontology IRIs to inferred ontologies.
      * There are no versions delegated to inferred ontologies, and the ontology IRI is generated
      * using the version IRI of the original ontology, which must be unique.
      */
     public static final String INFERRED_PREFIX = "urn:podd:inferred:ontologyiriprefix:";
     
     /**
      * The key used in the podd.properties file to locate the data repository aliases file.
      */
     public static final String KEY_ALIASES = "podd.datarepository.aliases";
     
     /**
      * The key used in the podd.properties file to locate the schema manifest file.
      */
     public static final String KEY_SCHEMAS = "podd.schemas.manifest";
     
     /**
      * The OMV vocabulary defines a property for the current version of an ontology, so we are
      * reusing it here.
      */
     public static final URI OMV_CURRENT_VERSION = PoddRdfConstants.VF.createURI("http://omv.ontoware.org/ontology#",
             "currentVersion");
     
     public static final URI OWL_MAX_QUALIFIED_CARDINALITY = PoddRdfConstants.VF
             .createURI("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
     
     public static final URI OWL_MIN_QUALIFIED_CARDINALITY = PoddRdfConstants.VF
             .createURI("http://www.w3.org/2002/07/owl#minQualifiedCardinality");
     
     public static final URI OWL_QUALIFIED_CARDINALITY = PoddRdfConstants.VF
             .createURI("http://www.w3.org/2002/07/owl#qualifiedCardinality");
     
     public static final URI OWL_VERSION_IRI = OWL.VERSIONIRI;
     
     public static final String PATH_BASE_ONTOLOGIES_VERSION_1 = "/ontologies/version/1/";
     
     /** Path to default alias file */
     public static final String PATH_DEFAULT_ALIASES_FILE = "/com/github/podd/api/file/default-file-repositories.ttl";
     
     /** Path to default alias file */
     public static final String PATH_DEFAULT_SCHEMAS = "/podd-schema-manifest.ttl";
     
     /** Path to poddAnimal.owl */
     public static final String PATH_PODD_ANIMAL = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddAnimal.owl";
     
     /** Path to poddBase.owl */
     public static final String PATH_PODD_BASE = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddBase.owl";
     
     /**
      * Path to poddDataRepository.owl.
      * 
      * This ontology is NOT part of the standard schema ontologies. It is a separate ontology used
      * to validate Data Repository configurations.
      */
     public static final String PATH_PODD_DATA_REPOSITORY = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1
             + "poddDataRepository.owl";
     
     /** Path to dcTerms.owl */
     public static final String PATH_PODD_DCTERMS = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1 + "dcTerms.owl";
     
     /** Path to foaf.owl */
     public static final String PATH_PODD_FOAF = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1 + "foaf.owl";
     
     /** Path to poddPlant.owl */
     public static final String PATH_PODD_PLANT = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddPlant.owl";
     
     /** Path to poddScience.owl */
     public static final String PATH_PODD_SCIENCE = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddScience.owl";
     
     /** Path to poddUser.owl */
     public static final String PATH_PODD_USER = PoddRdfConstants.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddUser.owl";
     
     public static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";;
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Exactly_One */
     public static final URI PODD_BASE_CARDINALITY_EXACTLY_ONE = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_Exactly_One");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many */
     public static final URI PODD_BASE_CARDINALITY_ONE_OR_MANY = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_One_Or_Many");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_Many */
     public static final URI PODD_BASE_CARDINALITY_ZERO_OR_MANY = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_Zero_Or_Many");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_One */
     public static final URI PODD_BASE_CARDINALITY_ZERO_OR_ONE = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "Cardinality_Zero_Or_One");
     
     public static final URI PODD_BASE_CONTAINS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "contains");
     
     /** http://purl.org/podd/ns/poddBase#createdAt */
     public static final URI PODD_BASE_CREATED_AT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "createdAt");
     
     /**
      * Creating a property for PODD to track the currentInferredVersion for the inferred axioms
      * ontology when linking from the ontology IRI.
      */
     public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "currentInferredVersion");
     
     /** http://purl.org/podd/ns/poddBase#DataReference */
     public static final URI PODD_BASE_DATA_REFERENCE_TYPE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DataReference");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileReference */
     public static final URI PODD_BASE_DATA_REFERENCE_TYPE_SPARQL = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "SPARQLDataReference");
     
     /** http://purl.org/podd/ns/poddBase#hasDisplayType */
     public static final URI PODD_BASE_DISPLAY_TYPE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasDisplayType");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_CheckBox */
     public static final URI PODD_BASE_DISPLAY_TYPE_CHECKBOX = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_CheckBox");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_DropDownList */
     public static final URI PODD_BASE_DISPLAY_TYPE_DROPDOWN = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_DropDownList");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_Table */
     public static final URI PODD_BASE_DISPLAY_TYPE_FIELDSET = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_FieldSet");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_LongText */
     public static final URI PODD_BASE_DISPLAY_TYPE_LONGTEXT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "DisplayType_LongText");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_ShortText */
     public static final URI PODD_BASE_DISPLAY_TYPE_SHORTTEXT = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "DisplayType_ShortText");
     
     public static final URI PODD_BASE_DO_NOT_DISPLAY = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "doNotDisplay");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileReference */
     public static final URI PODD_BASE_FILE_REFERENCE_TYPE_SSH = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "SSHFileReference");
     
     // ----- custom representation of cardinalities -----
     
     /**
      * http://purl.org/podd/ns/poddBase#hasAlias.
      * 
      * This property is used to specify an "alias" value found inside a DataReference.
      */
     public static final URI PODD_BASE_HAS_ALIAS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "hasAlias");
     
     /** http://purl.org/podd/ns/poddBase#hasAllowedValue */
     public static final URI PODD_BASE_HAS_ALLOWED_VALUE = ValueFactoryImpl.getInstance().createURI(
             PoddRdfConstants.PODD_BASE, "hasAllowedValue");
     
     /**
      * http://purl.org/podd/ns/poddBase#hasCardinality. Represents a <b>hasCardinality</b> property.
      */
     public static final URI PODD_BASE_HAS_CARDINALITY = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasCardinality");
     
     /** http://purl.org/podd/ns/poddBase#hasDataReference */
     public static final URI PODD_BASE_HAS_DATA_REFERENCE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasDataReference");
     
     /** http://purl.org/podd/ns/poddBase#hasPath */
     public static final URI PODD_BASE_HAS_FILE_PATH = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasPath");
     
     // ----- file reference constants -----
     
     /** http://purl.org/podd/ns/poddBase#hasFileName */
     public static final URI PODD_BASE_HAS_FILENAME = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasFileName");
     
     public static final URI PODD_BASE_HAS_PRINCIPAL_INVESTIGATOR = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "hasPrincipalInvestigator");
     
     public static final URI PODD_BASE_HAS_PROJECT_OBSERVER = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasProjectObserver");
     
     public static final URI PODD_BASE_HAS_PROJECT_MEMBER = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasProjectMember");
     
     public static final URI PODD_BASE_HAS_PROJECT_ADMINISTRATOR = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "hasProjectAdministrator");
     
     public static final URI PODD_BASE_HAS_PUBLICATION_STATUS = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.PODD_BASE, "hasPublicationStatus");
     
     /** http://purl.org/podd/ns/poddBase#hasSPARQLGraph */
     public static final URI PODD_BASE_HAS_SPARQL_GRAPH = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "hasSPARQLGraph");
     
     public static final URI PODD_BASE_HAS_TOP_OBJECT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "artifactHasTopObject");
     
     /**
      * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
      * a particular versioned ontology.
      */
     public static final URI PODD_BASE_INFERRED_VERSION = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "inferredVersion");
     
     /** http://purl.org/podd/ns/poddBase#lastModified */
     public static final URI PODD_BASE_LAST_MODIFIED = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "lastModified");
     
     public static final URI PODD_BASE_NOT_PUBLISHED = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "NotPublished");
     
     public static final URI PODD_BASE_PUBLISHED = PoddRdfConstants.VF
             .createURI(PoddRdfConstants.PODD_BASE, "Published");
     
     /** http://purl.org/podd/ns/poddBase#refersTo */
     public static final URI PODD_BASE_REFERS_TO = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "refersTo");
     
     public static final URI PODD_BASE_WEIGHT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "weight");
     
     /** http://purl.org/podd/ns/poddBase#DataRepository */
     public static final URI PODD_DATA_REPOSITORY = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "DataRepository");
     
     /**
      * http://purl.org/podd/ns/poddBase#hasDataRepositoryAlias
      * 
      * This property is ONLY used in the Data Repository management implementations.
      */
     public static final URI PODD_DATA_REPOSITORY_ALIAS = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryAlias");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryHost */
     public static final URI PODD_DATA_REPOSITORY_HOST = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "hasDataRepositoryHost");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryPath */
     public static final URI PODD_DATA_REPOSITORY_PATH = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "hasDataRepositoryPath");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryPort */
     public static final URI PODD_DATA_REPOSITORY_PORT = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "hasDataRepositoryPort");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryProtocol */
     public static final URI PODD_DATA_REPOSITORY_PROTOCOL = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryProtocol");
     
     public static final String PODD_DCTERMS = "http://purl.org/podd/ns/dcTerms#";
     
     /**
      * TODO: Temporary domain for specifying error messages in RDF
      */
     public static final String PODD_ERROR = "http://purl.org/podd/ns/err#";
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryFingerprint */
     public static final URI PODD_FILE_REPOSITORY_FINGERPRINT = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryFingerprint");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositorySecret */
     public static final URI PODD_FILE_REPOSITORY_SECRET = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositorySecret");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryUsername */
     public static final URI PODD_FILE_REPOSITORY_USERNAME = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "hasDataRepositoryUsername");
     
     public static final String PODD_FOAF = "http://purl.org/podd/ns/foaf#";
     
     /** http://purl.org/podd/ns/poddBase#HTTPFileRepository */
     public static final URI PODD_HTTP_FILE_REPOSITORY = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "HTTPFileRepository");
     
     public static final String PODD_PLANT = "http://purl.org/podd/ns/poddPlant#";
     
     public static final URI PODD_REPLACED_TEMP_URI_WITH = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE,
             "replacedTempUriWith");
     
     /** http://purl.org/podd/ns/poddUser#roleMappedObject */
     public static final URI PODD_ROLEMAPPEDOBJECT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER,
             "roleMappedObject");
     
     public static final String PODD_SCIENCE = "http://purl.org/podd/ns/poddScience#";
     
     /** http://purl.org/podd/ns/poddScience#Project */
    public static final Object PODD_SCIENCE_PROJECT = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE,
             "Project");
     
     /** http://purl.org/podd/ns/poddBase#SPARQLDataRepository */
     public static final URI PODD_SPARQL_DATA_REPOSITORY = PoddRdfConstants.VF.createURI(
             PoddRdfConstants.DATA_REPOSITORY, "SPARQLDataRepository");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileRepository */
     public static final URI PODD_SSH_FILE_REPOSITORY = PoddRdfConstants.VF.createURI(PoddRdfConstants.DATA_REPOSITORY,
             "SSHFileRepository");
     
     public static final String PODD_USER = "http://purl.org/podd/ns/poddUser#";
     
     /** http://purl.org/podd/ns/poddUser#address */
     public static final URI PODD_USER_ADDRESS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER, "address");
     
     /** http://purl.org/podd/ns/poddUser#homepage */
     public static final URI PODD_USER_HOMEPAGE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER, "homepage");
     
     /** http://purl.org/podd/ns/poddUser#oldSecret */
     public static final URI PODD_USER_OLDSECRET = PoddRdfConstants.VF
             .createURI(PoddRdfConstants.PODD_USER, "oldSecret");
     
     /** http://purl.org/podd/ns/poddUser#orcid */
     public static final URI PODD_USER_ORCID = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER, "orcid");
     
     /** http://purl.org/podd/ns/poddUser#organization */
     public static final URI PODD_USER_ORGANIZATION = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER,
             "organization");
     
     /** http://purl.org/podd/ns/poddUser#phone */
     public static final URI PODD_USER_PHONE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER, "phone");
     
     /** http://purl.org/podd/ns/poddUser#position */
     public static final URI PODD_USER_POSITION = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER, "position");
     
     /** http://purl.org/podd/ns/poddUser#status */
     public static final URI PODD_USER_STATUS = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER, "status");
     
     /** http://purl.org/podd/ns/poddUser#title */
     public static final URI PODD_USER_TITLE = PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_USER, "title");
     
     /**
      * @Deprecated Unused. Remove if not needed.
      */
     public static final URI SCOPE_ARTIFACT = PoddRdfConstants.VF
             .createURI("http://purl.org/podd/poddBase#PoddArtifact");
     
     /**
      * @Deprecated Unused. Remove if not needed.
      */
     public static final URI SCOPE_REPOSITORY = PoddRdfConstants.VF
             .createURI("http://purl.org/podd/poddBase#PoddRepository");
     
     /**
      * [http://purl.org/podd/ns/artifact/artifact89]
      * 
      * A dummy Artifact URI for test purposes.
      */
     public static final URI TEST_ARTIFACT = PoddRdfConstants.VF
             .createURI("http://purl.org/podd/ns/artifact/artifact89");
     
     public static final URI PODD_SCHEMA_CLASSPATH = PoddRdfConstants.VF
             .createURI("http://purl.org/podd/ns/schema#classpath");
     
 }
