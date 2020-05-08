 package com.coremedia.beanmodeller.processors.analyzator;
 
 import com.coremedia.beanmodeller.beaninformation.ContentBeanInformation;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This class contains all information that occured during the analyzation of content beans.
  */
 public class ContentBeanAnalyzationException extends ContentBeanAnalyzatorInternalException {
   public static final String NOT_INHERITING_ABSTRACT_CONTENT_BEAN_MESSAGE = "This bean does not inherit ";
   public static final String CLASSNAME_TOO_LOGN_FOR_DOCTPYENAME_MESSAGE = "The class name is too long for ";
   public static final String METHODNAME_TOO_LOGN_FOR_DOCTPYENAME_MESSAGE = "The method name is too long for ";
 
   public static final String DUPLICATE_DOCTYPE_NAMES_MESSAGE = "There are duplicate document type names :";
   public static final String DUPLICATE_PROPERTY_NAMES_MESSAGES = "There are duplicate property names :";
   public static final String INVALID_PROPERTY_MESSAGE = "The following Method is no valid content bean property (keep in mind it must be abstract, start with \'get\' or \'is\' and must not have any parameters. ";
   public static final String PROPERTY_NOT_IN_CB_MESSAGE = "You can only define bean properties in classes marked as content bean. ";
   public static final String STRING_PROPERTY_TOO_SHORT_MESSAGE = "String property must have a positive length, but is: ";
   public static final String PROPERTY_RETURN_TYPE_UNKNOWN_MESSAGE = "Method return type could not be mapped to a property type :";
  public static final String INVALID_RETURN_TYPES_MESSAGE = "Thou shalt not use primitive data types like int or boolean and only use ";
   public static final String SCHEMA_DEFINITION_NOT_FOUND_MESSAGE = "The schema definition could not be found: ";
  //TODO rename to _MESSAGE
  public static final String LINKED_DOCTYPE_UNKNOWN = "A referenced document type is not part of the defined ContentBeans: ";
   public static final String VALID_METHOD_HINTS_MESSAGE = "\nKeep in mind it has to be abstract, without parameters and either public or protected.";
 
   private List<ContentBeanAnalyzationError> errors = new LinkedList<ContentBeanAnalyzationError>();
   public static final String INVALID_MIME_TYPE_MESSAGE = "There is a invalid mime type definition";
 
   /**
    * Content bean exceptions can contain more than one error - so it can live without an message
    */
   public ContentBeanAnalyzationException() {
     super();
   }
 
   /**
    * Add a found error to the list of errors.
    *
    * @param beanClass the class where the error occurred
    * @param message   the message of the error
    */
   public void addError(Class beanClass, String message) {
     errors.add(
         new ContentBeanAnalyzationError(beanClass, message)
     );
   }
 
   /**
    * Add a found error to the list of errors.
    *
    * @param beanClass  the class where the error occurred
    * @param beanMethod Method where the error occurred
    * @param message    the message of the error
    */
   public void addError(Class beanClass, String beanMethod, String message) {
     errors.add(
         new ContentBeanAnalyzationError(beanClass, beanMethod, message)
     );
   }
 
   public List<ContentBeanAnalyzationError> getErrors() {
     return errors;
   }
 
   public boolean hasErrors() {
     return !errors.isEmpty();
   }
 
 
   @Override
   public String getMessage() {
     StringBuffer result = new StringBuffer();
     result.append("Error analyizing content beans:\n");
     for (ContentBeanAnalyzationError error : errors) {
       result.append("\t");
       result.append(error.toString());
       result.append("\n");
     }
     return result.toString();
   }
 
   /**
    * Represents a single error while anaylsing the content beans
    */
   public final class ContentBeanAnalyzationError {
     private ContentBeanInformation beanInformation;
     private Class beanClass;
     private String beanMethod; // method where the error occurred (if applicable)
     private String message;
 
 
     private ContentBeanAnalyzationError(ContentBeanInformation beanInformation, Class beanClass, String beanMethod, String message) {
       this.beanInformation = beanInformation;
       this.beanClass = beanClass;
       this.beanMethod = beanMethod;
       this.message = message;
     }
 
     private ContentBeanAnalyzationError(ContentBeanInformation beanInformation, Class beanClass, String message) {
       this(beanInformation, beanClass, "", message);
     }
 
     private ContentBeanAnalyzationError(ContentBeanInformation beanInformation, String message) {
       this(beanInformation, null, message);
     }
 
     private ContentBeanAnalyzationError(Class beanClass, String beanMethod, String message) {
       this(null, beanClass, beanMethod, message);
     }
 
     private ContentBeanAnalyzationError(Class beanClass, String message) {
       this(null, beanClass, message);
     }
 
     private ContentBeanAnalyzationError(String message) {
       this(null, null, "", message);
     }
 
     public ContentBeanInformation getBeanInformation() {
       return beanInformation;
     }
 
     public Class getBeanClass() {
       return beanClass;
     }
 
     public String getMessage() {
       return message;
     }
 
     public String toString() {
       StringBuffer result = new StringBuffer();
       result.append("Error: ");
       if (beanClass != null) {
         result.append("Class ");
         result.append(beanClass.getCanonicalName());
         result.append(" ");
       }
       if (StringUtils.isNotBlank(beanMethod)) {
         result.append("Method ");
         result.append(beanMethod);
         result.append(" ");
       }
       if (beanInformation != null) {
         result.append("DocType ");
         result.append(beanInformation.getDocumentName());
         result.append(" ");
       }
       result.append(message);
 
       return result.toString();
     }
   }
 }
