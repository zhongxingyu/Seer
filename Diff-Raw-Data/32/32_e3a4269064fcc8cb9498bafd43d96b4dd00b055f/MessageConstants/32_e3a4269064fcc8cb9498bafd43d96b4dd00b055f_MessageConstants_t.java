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
 * This class is used to maintain all Strings that are used for informational
 * messages while logging. These messages are not errors.
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0
  */
 public final class MessageConstants {
 
     /**
      * Hidden constructor
      */
    private MessageConstants() {
    }

     /**
      * Blackboard - reset method message
      */
     public static final String BLACKBOARD_RESET = "Blackboard has been cleaned and ready for problem solving.";
 
     public static final String SUBSTITUTION_ASSERT = "An assertion was given to describe a direct translation of a cipher letter.";
 
     public static final String SENTENCE_SOLVED = "My evaluation has determined that the cryptogram has been solved.";
 
     public static final String UNKNOWN_KNOWLEDGE_SOURCE = "Unknown knowledge source detected.";
 }
