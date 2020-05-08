 /*
  * Copyright 2011 Martin Grotzke
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.javakaffee.validation;
 
 import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
 import static java.lang.annotation.ElementType.TYPE;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import javax.validation.Constraint;
 import javax.validation.Payload;
 
 /**
  * Validate that 2 fields have the same value. An array of fields and their
  * matching confirmation fields can be supplied.
  *
  * Example, compare 1 pair of fields:
  * <pre>
 * @FieldsMatch(first = "password", second = "confirmPassword", message = "The password fields must match")
  * </pre>
  * Example, compare more than 1 pair of fields:
  * <pre>
 * @FieldsMatch.List({
 *   @FieldsMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
 *   @FieldsMatch(first = "email", second = "confirmEmail", message = "The email fields must match")
  * })
  * </pre>
  * <p>
  * This constraint is based on this
  * <a href="http://stackoverflow.com/questions/1972933/cross-field-validation-with-hibernate-validator-jsr-303">stackoverflow post</a>
  * with these modifications:
  * <ul>
  * <li>there's no dependency on commons beanutils</li>
  * <li>if an Exception occurs when comparing the specified fields the exception is rethrown
  * as RuntimeException (instead of silently dropping it)</li>
  * <li>the thrown ConstraintValidationException (when fields don't match) contains the path of the second field so that
  * it's highlighted correctly and the error message goes to that field.</li>
  * </ul> 
  * </p>
  */
 @Target({ TYPE, ANNOTATION_TYPE })
 @Retention(RUNTIME)
 @Constraint(validatedBy = FieldsMatchValidator.class)
 @Documented
 public @interface FieldsMatch {
 
     String message() default "{validation.constraints.FieldMatch.message}";
 
     Class<?>[] groups() default {};
 
     Class<? extends Payload>[] payload() default {};
 
     /**
      * @return The first field
      */
     String first();
 
     /**
      * @return The second field
      */
     String second();
 
     /**
      * Defines several <code>@FieldMatch</code> annotations on the same element
      *
      * @see FieldsMatch
      */
     @Target({ TYPE, ANNOTATION_TYPE })
     @Retention(RUNTIME)
     @Documented
     @interface List {
         FieldsMatch[] value();
     }
 }
