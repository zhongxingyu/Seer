 package org.refeed.harpe;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Checks are validations that do not perform any transformation on the
  * input value.
  *
  * This class is intended to be inherited for simplified rule specification.
  *
  * @param <T>
  */
 public abstract class Check<T> implements Validation<T, T> {
     private List<ValidationError> errors;
 
     /**
     * Calls #checkRule and collects all the validation errors it reports or
     * report success otherwise.
      *
      * @param value Value to check
      * @return      Validation result
      * @see #error(String, Object...)
      * @see #errors(java.util.Collection)
      */
     @Override
     public final ValidationResult<T, T> run(T value) {
         this.errors = new LinkedList<ValidationError>();
         checkRule(value);
         // TODO: Can we narrow down also result types?
         return this.errors.isEmpty()
                 ? new ValidationSuccess<T, T>(value, value)
                 : new ValidationFailure<T, T>(value, errors);
     }
 
    /**
     * Checks for validation errors on the input value.
     *
     * Validation errors are reported invoking protected methods #error or
     * #errors.
     *
     * @param value Input value
     */
     protected abstract void checkRule(T value);
 
     protected void error(String formatString, Object... args) {
         this.errors.add(new ValidationError(formatString, args));
     }
 
     protected void error(ValidationError error) {
         this.errors.add(error);
     }
 
     protected void errors(Collection<ValidationError> errors) {
         this.errors.addAll(errors);
     }
 
     private static final Check NO_CHECK = new Check() {
         @Override
         protected void checkRule(Object ignored) {
             // Nothing
         }
     };
 
     public static <T> Check<T> any() {
         return (Check<T>) NO_CHECK;
     }
 
     /**
      * Aggregate a number of rule checks into a complex rule check.
      *
      * @param rules Rules to check.
      * @param <T>   Type of values to check
      * @return      The composed rule check
      */
     public static <T> Check<T> checkAll(final Check<T>... rules) {
         switch(rules.length) {
             case 0:
                 return (Check<T>) NO_CHECK;
             case 1:
                 return rules[0];
             default:
                 return new Check<T>() {
                     public void checkRule(T value) {
                         for (Validation<T, T> validation : rules) {
                             errors(validation.run(value)
                                     .getValidationErrors());
                         }
                     }
                 };
         }
     }
 }
