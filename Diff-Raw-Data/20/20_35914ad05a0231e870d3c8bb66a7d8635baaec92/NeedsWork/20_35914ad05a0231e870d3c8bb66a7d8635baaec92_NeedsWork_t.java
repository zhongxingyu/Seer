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
 package ch.raffael.contracts.util;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 
 
 /**
  * Indicates that an element is incomplete and needs some work.
  *
 * Both the compile-time annotation processor as well as the runtime class processor will
 * print warnings whenever they encounter an element annotated with `@NeedsWork`.
 *
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
@Retention(RetentionPolicy.CLASS)
 @Documented
 public @interface NeedsWork {
 
    /**
     * A description of what needs to be done. This will be printed in the warnings.
     */
     String description();
 
    /**
     * References like docs etc.
     */
     String[] seeAlso() default {};
 
    /**
     * If `true`, no warnings will be printed.
     */
    boolean quite() default false;

 }
