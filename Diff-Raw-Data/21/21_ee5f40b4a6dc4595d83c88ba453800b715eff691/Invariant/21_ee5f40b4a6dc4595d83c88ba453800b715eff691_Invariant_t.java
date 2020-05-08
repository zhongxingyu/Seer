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
 
 import ch.raffael.contracts.meta.NeedsWork;
 
 
 /**
  * <p>Define a contract that must be <code>true</code> at all times.</p>
  *
  * <p>Invariants will be checked both on method entry and method exit. <strike>They apply
  * to public non-static methods only, as only these are considered the public interface
  * of an object</strike> (not sure about that).</p>
  *
 * <p><strong>Inheritance</strong>: Invariant contracts may be strengthened by extending
 * classes, but not weakened. Inherited contracts will be <em>AND</em>-associated.</p>
  *
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 @Target(ElementType.TYPE)
 @Retention(RetentionPolicy.CLASS)
 @Documented
 @NeedsWork(description = "Specify when exactly this applies: public only?",
            seeAlso = "JavaDoc")
 public @interface Invariant {
 
     @Syntax("Cel") String[] value();
 
 }
