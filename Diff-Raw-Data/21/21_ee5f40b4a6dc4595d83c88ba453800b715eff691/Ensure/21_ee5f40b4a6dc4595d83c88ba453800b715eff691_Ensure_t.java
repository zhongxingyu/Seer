 /*
  * Copyright 2012-2013 Raffael Herzog
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.raffael.contracts;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import javax.annotation.Syntax;
 
 
 /**
  * <p>Ensure that a <em>postcondition</em> is <code>true</code> on method exit.</p>
  *
 * <p><strong>Inheritance</strong>: Postcondition contracts may be strengthened by
 * subclasses, but not weakened. Inherited preconditions will be
 * <em>AND</em>-associated.</p>
  *
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
 @Retention(RetentionPolicy.CLASS)
 @Documented
 public @interface Ensure {
 
     @Syntax("Cel") String[] value();
 
 }
