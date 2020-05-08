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
 
 import static de.javakaffee.validation.ReflectionUtils.getPropertyValue;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 /**
  * The validator for {@link FieldsMatch}.
  *
  * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
  */
 public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {
 
     private FieldsMatch constraintAnnotation;
 
     @Override
     public void initialize(final FieldsMatch constraintAnnotation) {
         this.constraintAnnotation = constraintAnnotation;
     }
 
     @Override
     public boolean isValid(final Object target, final ConstraintValidatorContext context) {
         final String firstFieldName = constraintAnnotation.first();
         final String secondFieldName = constraintAnnotation.second();
         try {
             final Object firstObj = getPropertyValue(target, firstFieldName);
             final Object secondObj = getPropertyValue(target, secondFieldName);
 
             final boolean valid = firstObj == null && secondObj == null || firstObj != null && firstObj.equals(secondObj);
             if (!valid) {
                 context.buildConstraintViolationWithTemplate(constraintAnnotation.message()).addNode(secondFieldName)
                         .addConstraintViolation().disableDefaultConstraintViolation();
                 return false;
             }
             return valid;
        } catch (final Exception e) {
             // If s.o. would prefer that such an error is silently dropped, an
             // attribute "ignoreExceptions" could be added to the FieldMatch
             // annotation.
             throw new RuntimeException("An error occurred when validating the fields '" + firstFieldName + "' and"
                    + " '" + secondFieldName + "' on bean of type '" + target.getClass().getName(), e);
         }
     }
 }
