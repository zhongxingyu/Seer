 /**
  * Copyright 2011 55 Minutes (http://www.55minutes.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fiftyfive.util;
 
 /**
  * Methods for doing common sanity checks, throwing IllegalArgumentException
  * or IllegalStateException on failure. Many third-party libraries provide
  * this functionality; if your project already depends on them, consider
 * <a href="http://commons.apache.org/lang/api-release/org/apache/commons/lang/Validate.html">commons-lang Validate</a>
  * or
  * <a href="http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/util/Assert.html">Spring Framework Assert</a>.
  */
 public class Assert
 {
     /**
      * Asserts that the given condition is {@code true}.
      * 
      * @throws IllegalArgumentException with {@code message} if
      *         {@code condition} is {@code false}.
      */
     public static void isTrue(boolean condition, String message)
         throws IllegalArgumentException
     {
         if(!condition)
         {
             throw new IllegalArgumentException(message);
         }
     }
     
     /**
      * Asserts that the given condition is {@code false}.
      * 
      * @throws IllegalArgumentException with {@code message} if
      *         {@code condition} is {@code true}.
      */
     public static void isFalse(boolean condition, String message)
         throws IllegalArgumentException
     {
         if(condition)
         {
             throw new IllegalArgumentException(message);
         }
     }
     
     /**
      * Asserts that the given object is not {@code null}.
      * 
      * @throws IllegalArgumentException with the message
      *         "argument cannot be null" if {@code obj} is {@code null}.
      */
     public static void notNull(Object obj)
         throws IllegalArgumentException
     {
         notNull(obj, "argument cannot be null");
     }
 
     /**
      * Asserts that the given object is not {@code null}.
      * 
      * @throws IllegalArgumentException with {@code message} if {@code obj}
      *         is {@code null}.
      */
     public static void notNull(Object obj, String message)
         throws IllegalArgumentException
     {
         if(null == obj)
         {
             throw new IllegalArgumentException(message);
         }
     }
     
     /**
      * Asserts that the given condition is {@code true}.
      * 
      * @throws IllegalStateException with {@code message} if {@code test}
      *         is {@code false}.
      */
     public static void validState(boolean test, String message)
         throws IllegalStateException
     {
         if(!test)
         {
             throw new IllegalStateException(message);
         }
     }
     
     /**
      * The Assert class is not meant to be instantiated.
      */
     private Assert()
     {
         super();
     }
 }
