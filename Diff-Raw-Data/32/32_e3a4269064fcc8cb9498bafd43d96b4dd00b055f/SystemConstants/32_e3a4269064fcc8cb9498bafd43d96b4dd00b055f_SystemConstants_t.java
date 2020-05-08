 /**
  * Copyright 2010 David L. Whitehurst
  *
  * Licensed under the Apache License, Version 2.0
  * (the "License"); You may not use this file except
  * in compliance with the License. You may obtain a
  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific
  * language governing permissions and limitations under the
  * License.
  *
  */
 package org.dlw.ai.blackboard.util;
 
 /**
  * This utility class provides string constants for system level needs. These
  * constants are not part of the API. They are, however part of the default
  * implementation.
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0
  * 
  */
 public final class SystemConstants {
 
     /**
      * Hidden constructor
      */
     private SystemConstants() {
     }
 
     /**
      * The cryptogram sentence or string could not be asserted.
      */
     public static final String NO_PROBLEM_ASSERT_ERROR = "I could not assert the problem!";
 
     /**
      * The system could not solve the cipher.
      */
    public static final String NO_DECIPHER_ERROR = "I could not decipher the asserted problem!";
 
     /**
      * The empty string is not acceptable as a cipher letter input.
      */
     public static final String EMPTY_CIPHER_LETTER_ERROR = "Cannot accept an empty cipher letter.";
 
     /**
      * The empty string is not acceptable as a plain-text letter input.
      */
     public static final String EMPTY_PLAIN_LETTER_ERROR = "Cannot accept an empty plain letter.";
 
     /**
      * This error string is given when a solution is requested prematurely.
      */
     public static final String EARLY_RETRIEVAL_ERROR = "This method should only be called after the blackboard problem has been solved.";
 
     /**
      * This error indicates that INFO level logging needs to be configured.
      */
     public static final String INFO_LEVEL_FATAL = "FATAL ERROR: The system requires INFO level logging.";
 
 
 }
