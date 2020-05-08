 package xbrlcore.constants;
 
 /**
  * This class contains some constants needed for excpetion messages. <br/><br/>
  * 
  * @author Daniel Hamm
  */
 public class ExceptionConstants {
 
     /* exception messages */
     public static final String EX_NO_SCHEMA_FILE = "Could not locate schema file ";
 
     public static final String EX_NO_LINKBASE_FILE = "Could not detect linkbase file ";
 
     public static final String EX_NO_LINKBASE_BUILD = "Cannot build linkbase for file ";
 
     public static final String EX_DOUBLE_ELEMENT = "Element ID already available in taxonomy";
 
     public static final String EX_LINKBASE_LOCATOR_WITHOUT_REF = "XBRL element to linkbase locator could not be found";
 
     public static final String EX_DIM_ITEM_WRONG_SUBSTITUTION_GROUP = "Dimension item must have substitution group: xbrldt:dimensionItem";
 
     public static final String EX_HYPERCUBE_ITEM_WRONG_SUBSTITUTION_GROUP = "Hypercube item must have substitution group: xbrldt:hypercubeItem";
 
     public static final String EX_MORE_THAN_ONE_XLINK_LOCATOR = "In one linkbase, there is more than one locator pointing to the same element. Taking the first one ...";
 
     public static final String EX_ELEMENT_HAS_NO_LINKBASE_LOCATOR = "Linkbase locator to XBRL element could not be found";
 
     public static final String EX_NO_DOMAIN_MEMBER_NETWORK = "Domain member network of an explicit dimension could not be determined in definition linkbase.";
 
     public static final String EX_DOUBLE_LOCATOR_LABEL = "There are two locators with the same label pointing to different concepts within the same extended link role of a linkbase";
 
     public static final String EX_INSTANCE_NO_CONTEXT_FOR_FACT_AVAILABLE = "There is no context available for this fact. Please create context first.";
 
     public static final String EX_INSTANCE_FACT_NOT_MATCHES_CONTEXT = "The fact does not match the context.";
 
     public static final String EX_INSTANCE_FACT_INFORMATION_MISSING = "The fact could not be added since some information is missing";
     
     public static final String EX_INSTANCE_FACT_UNIT_MISSING = "The fact could not be added since a non numeric item must have a unit";
 
     public static final String EX_INSTANCE_CONTEXT_INFORMATION_MISSING = "The context could not be added since some information is missing";
 
     public static final String EX_INSTANCE_UNIT_INFORMATION_MISSING = "The unit could not be added since some information is missing";
 
     public static final String EX_INSTANCE_DOUBLE_CONTEXT = "A context with the same ID is already part of the instance document.";
 
     public static final String EX_INSTANCE_DOUBLE_UNIT = "A unit with the same ID is already part of the instance document.";
 
     public static final String EX_INSTANCE_CREATION_DIMENSIONS = "Cannot determine dimensional information in context ";
 
     public static final String EX_INSTANCE_CREATION_NOID_CONTEXT = "Cannot create context without ID";

     public static final String EX_INSTANCE_CREATION_NO_SCHEMA_PREFIX = "Undefined schema prefix: ";
 
     public static final String EX_INSTANCE_CREATION_NOID_UNIT = "Cannot create unit without ID";
 
     public static final String EX_INSTANCE_CREATION_FACT = "Cannot determine XBRL element for fact ";
 
     public static final String EX_INSTANCE_CREATION_NO_CONTEXT = "The fact does not refer to a known context: ";
 
     public static final String EX_INSTANCE_CREATION_NO_UNIT = "The fact does not refer to a known unit: ";
 
 }
