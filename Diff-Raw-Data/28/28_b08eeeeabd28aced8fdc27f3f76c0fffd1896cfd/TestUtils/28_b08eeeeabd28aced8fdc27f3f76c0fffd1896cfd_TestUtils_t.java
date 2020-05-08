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
 
 import static junit.framework.Assert.assertEquals;
 
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Path;
 
import org.junit.Ignore;

 /**
  * Utilities for checks on constraint violations.
  * 
  * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
  */
@Ignore
 public class TestUtils {
 
     private TestUtils() {
         // utility class
     }
 
     public static ConstraintViolation<?> getByPropertyPath(final Set<? extends ConstraintViolation<?>> violations,
             final String propertyPath) {
         for (final ConstraintViolation<?> violation : violations) {
             final Path violationPropertyPath = violation.getPropertyPath();
             if (violationPropertyPath != null && violationPropertyPath.toString().equals(propertyPath)) {
                 return violation;
             }
         }
         return null;
     }
 
     public static void assertPropertyPath(final ConstraintViolation<?> violation, final String expectedPropertyPath) {
         assertEquals("Unexpected propertyPath for violation.", expectedPropertyPath, violation.getPropertyPath()
                 .toString());
     }
 
 }
