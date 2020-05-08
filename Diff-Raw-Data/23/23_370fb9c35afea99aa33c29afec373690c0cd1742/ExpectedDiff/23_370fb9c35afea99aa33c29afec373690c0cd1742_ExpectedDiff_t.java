 package net.sf.clirr.core.internal.checks;
 
import java.util.Locale;

 import net.sf.clirr.core.ApiDifference;
 import net.sf.clirr.core.MessageTranslator;
 import net.sf.clirr.core.Severity;
 
 /**
  * Describes an expected API change.
  */
 
 public final class ExpectedDiff
 {
    private static MessageTranslator translator = new MessageTranslator(Locale.ENGLISH);
 
     private String report;
     private Severity binaryCompatibilitySeverity;
     private Severity sourceCompatibilitySeverity;
     private String affectedClass;
     private String affectedMethod;
     private String affectedField;
 
     /**
      * Create a new API difference representation.
      *
      * @param report a human readable string describing the change that was made, must be non-null.
      * @param severity the severity in terms of binary and source code compatibility, must be non-null.
      * @param clazz the fully qualified class name where the change occured, must be non-null.
      * @param method the method signature of the method that changed, <code>null</code>
      *   if no method was affected.
      * @param field the field name where the change occured, <code>null</code>
      *   if no field was affected.
      */
     public ExpectedDiff(String report, Severity severity, String clazz, String method, String field)
     {
         this(report, severity, severity, clazz, method, field);
     }
 
     /**
      * Create a new API difference representation.
      *
      * @param report a human readable string describing the change that was made, must be non-null.
      * @param binarySeverity the severity in terms of binary compatibility, must be non-null.
      * @param sourceSeverity the severity in terms of source code compatibility, must be non-null.
      * @param clazz the fully qualified class name where the change occured, must be non-null.
      * @param method the method signature of the method that changed, <code>null</code>
      *   if no method was affected.
      * @param field the field name where the change occured, <code>null</code>
      *   if no field was affected.
      */
     public ExpectedDiff(String report, Severity binarySeverity, Severity sourceSeverity,
                          String clazz, String method, String field)
     {
         checkNonNull(report);
         checkNonNull(binarySeverity);
         checkNonNull(sourceSeverity);
         checkNonNull(clazz);
 
         this.report = report;
         this.binaryCompatibilitySeverity = binarySeverity;
         this.sourceCompatibilitySeverity = sourceSeverity;
         this.affectedClass = clazz;
         this.affectedField = field;
         this.affectedMethod = method;
     }
 
     /**
      * Trivial utility method to verify that a specific object is non-null.
      */
     private void checkNonNull(Object o)
     {
         if (o == null)
         {
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String toString()
     {
         return report + " (" + binaryCompatibilitySeverity + ") - "
                 + affectedClass + '[' + affectedField + '/' + affectedMethod + ']';
     }
 
     /**
      * Returns true if the provided ApiDifference object matches the
      * expected value.
      */
     public boolean matches(ApiDifference diff)
     {
         if (!report.equals(diff.getReport(translator)))
         {
             return false;
         }
 
         if (!binaryCompatibilitySeverity.equals(diff.getBinaryCompatibilitySeverity()))
         {
             return false;
         }
 
         if (!sourceCompatibilitySeverity.equals(diff.getSourceCompatibilitySeverity()))
         {
             return false;
         }
 
 
         final String otherClass = diff.getAffectedClass();
         if (!affectedClass.equals(otherClass))
         {
             return false;
         }
 
         final String otherMethod = diff.getAffectedMethod();
         if (affectedMethod != null ? !affectedMethod.equals(otherMethod) : otherMethod != null)
         {
             return false;
         }
 
         final String otherField = diff.getAffectedField();
         if (affectedField != null ? !affectedField.equals(otherField) : otherField != null)
         {
             return false;
         }
 
         return true;
     }
 }
