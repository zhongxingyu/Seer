 /*
  * Created on Aug 20, 2007
  * @author
  *
  */
 package edu.common.dynamicextensions.xmi;
 
 /**
  * @author preeti_lodha
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class XMIConstants
 {
 	//MDR related
 	//name of a UML extent (instance of UML metamodel) that the UML models will be loaded into
 	
 	public static final String UML_INSTANCE = "UMLInstance";
 	// name of a MOF extent that will contain definition of UML metamodel
 	public static final String UML_MM = "UML";
 	//tagged values
 	public static final String TAGGED_NAME_ASSOC_DIRECTION = "direction";
 	public static final String TAGGED_VALUE_ASSOC_BIDIRECTIONAL = "Bi-Directional";
 	public static final String TAGGED_VALUE_ASSOC_SRC_DEST= "Source -> Destination";
 	public static final String TAGGED_VALUE_ASSOC_DEST_SRC= "Destination -> Source";
 	public static final String TAGGED_VALUE_ID = "id";
 	public static final String TAGGED_VALUE_CONTAINMENT = "containment";
 	public static final String TAGGED_VALUE_CONTAINMENT_UNSPECIFIED = "Unspecified";
 	public static final String TAGGED_VALUE_CONTAINMENT_NOTSPECIFIED = "Not Specified";
 	public static final String TAGGED_VALUE_IMPLEMENTS_ASSOCIATION = "implements-association";
 	public static final String TAGGED_VALUE_MAPPED_ATTRIBUTES = "mapped-attributes";
 	public static final String TAGGED_VALUE_CORELATION_TABLE = "correlation-table";
 	public static final String TAGGED_VALUE_GEN_TYPE = "gentype";
 	public static final String TAGGED_VALUE_PRODUCT_NAME = "product_name";
 	public static final String TAGGED_VALUE_DESCRIPTION = "description";
 	public static final String TAGGED_VALUE_CONCEPT_CODE = "ConceptCode";
 	public static final String TAGGED_VALUE_DOCUMENTATION = "documentation";
 	
 	public static final String TAGGED_VALUE_DATASOURCE ="DataSource";
 	public static final String TAGGED_VALUE_DEPENDENCY = "Dependency";
 	
 	public static final String TAGGED_VALUE_MAX_LENGTH = "MaxLength";
 	public static final String TAGGED_VALUE_DATE_FORMAT = "Format";
 	public static final String TAGGED_VALUE_PRECISION = "Precision";
 
 	public static final String TAGGED_VALUE_PASSWORD = "Password";
 	public static final String TAGGED_VALUE_MULTILINE = "Multiline";
 	public static final String TAGGED_VALUE_DEFAULT_VALUE = "DefaultValue";
 	public static final String TAGGED_VALUE_URL = "URL";
 	public static final String TAGGED_VALUE_CONCEPT_DEFINITION = "ConceptDefinition";
 	public static final String TAGGED_VALUE_RULE = "Rule";
 	public static final String TAGGED_VALUE_MULTISELECT = "Multiselect";
 	public static final String MULTILINE = "MultiLine";
 	public static final String TAGGED_VALUE_DISPLAY_WIDTH = "DisplayWidth";
 	public static final String TAGGED_VALUE_PHI_ATTRIBUTE = "PHI";
 	public static final String TAGGED_VALUE_FILE_FORMATS = "FileFormats";
 	public static final String TAGGED_VALUE_SEPARATOR = "Separator";
 	public static final String TAGGED_VALUE_ATTRIBUTES_IN_ASSOCIATION_DROP_DOWN = "AttributesInAssociationDropDown";
 		
 	public static final String TAGGED_VALUE_OBJECT_CLASS_CONCEPT_CODE = "ObjectClassConceptCode";
 	public static final String TAGGED_VALUE_OBJECT_CLASS_CONCEPT_DEFINITION = "ObjectClassConceptDefinition";
 	public static final String TAGGED_VALUE_OBJECT_CLASS_CONCEPT_PREFERRED_NAME = "ObjectClassConceptPreferredName";
 	public static final String TAGGED_VALUE_OBJECT_CLASS_CONCEPT_DEFINITION_SOURCE = "ObjectClassConceptDefinitionSource";
 	public static final String TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_CODE = "ObjectClassQualifierConceptCode";
 	public static final String TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_DEFINITION = "ObjectClassQualifierConceptDefinition";
 	public static final String TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_DEFINITION_SOURCE = "ObjectClassQualifierConceptDefinitionSource";
 	public static final String TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_PREFERRED_NAME = "ObjectClassQualifierConceptPreferredName";
 
 	public static final String TAGGED_VALUE_PROPERTY_CONCEPT_CODE = "PropertyConceptCode";
 	public static final String TAGGED_VALUE_PROPERTY_CONCEPT_DEFINITION = "PropertyConceptDefinition";
 	public static final String TAGGED_VALUE_PROPERTY_CONCEPT_PREFERRED_NAME = "PropertyConceptPreferredName";
 	public static final String TAGGED_VALUE_PROPERTY_CONCEPT_DEFINITION_SOURCE = "PropertyConceptDefinitionSource";
 	public static final String TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_CODE = "PropertyQualifierConceptCode";
 	public static final String TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_DEFINITION = "PropertyQualifierConceptDefinition";
 	public static final String TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_DEFINITION_SOURCE = "PropertyQualifierConceptDefinitionSource";
 	public static final String TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_PREFERRED_NAME = "PropertyQualifierConceptPreferredName";
 	public static final String TAGGED_VALUE_OWNER_REVIEWED = "OWNER_REVIEWED";
 	public static final String TAGGED_VALUE_CURATOR_REVIEWED = "CURATOR_REVIEWED";	
 	
 	//Package & model name
 	public static final String PACKAGE_NAME_LOGICAL_VIEW ="Logical View";
 	public static final String PACKAGE_NAME_LOGICAL_MODEL ="Logical Model";
 	public static final String PACKAGE_NAME_DATA_MODEL ="Data Model";
 	public static final String MODEL_NAME = "EA Model";
 	
 	//Primary key/foreign key operations
 	public static final String PRIMARY_KEY = "PK";
 	public static final String FOREIGN_KEY = "FK";
 	public static final String FOREIGN_KEY_PREFIX = "FK_";
 
 	//stereotype constants
 	public static final String STEREOTYPE = "stereotype";
 	public static final String COLUMN = "column";
 	public static final String TABLE = "table";
 	//Streotype base classes
 	public static final String STEREOTYPE_BASECLASS_CLASS = "Class";
 	public static final String STEREOTYPE_BASECLASS_ATTRIBUTE = "Attribute";
 	public static final String STEREOTYPE_BASECLASS_ASSOCIATION = "Association";
 	public static final String TYPE = "type";
 	
 	//Associations
 	public static final String ASSOC_ONE_ONE = "One_To_One_Association";
 	public static final String ASSOC_ONE_MANY = "One_To_Many_Association";
 	public static final String ASSOC_MANY_ONE = "Many_To_One_Association";
 	public static final String ASSOC_MANY_MANY = "Many_To_Many_Association";
 	public static final String ASSOCIATION_PREFIX = "Assoc_";
 	public static final String COLLECTION_SUFFIX = "Collection";
 	
 	public static final String XMI_VERSION_1_1 = "1.1";
 	public static final String XMI_VERSION_1_2 = "1.2";
 	
 	public static final String TEMPORARY_XMI1_1_FILENAME = "tempxmi_1_1.xmi";
 	public static final String XSLT_FILENAME = "XMI_1.4-1.3Transformer.xsl";
 	
 	
 	//Separators
 	public static final String COMMA = ",";
 	public static final String SEPARATOR = "_";
 	public static final String DOT_SEPARATOR = ".";
 	public static final String TAGGED_VALUE_TYPE = "type";
 	public static final String DEFAULT_PACKAGE = "Default";
 	
 	public static final String CATISSUE_PACKAGE = "edu.wustl.catissuecore.domain.";
 
 	public static final String DEFAULT_TEXT_FIELD_MAX_LENGTH = "255";
	public static final String MAX_LENGTH_LIMIT = "999";
 }
