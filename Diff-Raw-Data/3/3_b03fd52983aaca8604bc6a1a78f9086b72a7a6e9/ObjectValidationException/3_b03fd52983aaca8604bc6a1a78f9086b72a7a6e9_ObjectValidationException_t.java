  /*
   * Copyright 2012 Michael Bischoff
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *   http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
 package de.jpaw.bonaparte.core;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.jpaw.util.ApplicationException;
 
 /**
  * The ObjectValidationException class.
  *
  * @author Michael Bischoff
  * @version $Revision$
  *
  *          Extends the generic ApplicationException class in order to provide error details which are
  *          specific to validation of fields.
  */
 
 public class ObjectValidationException extends ApplicationException {
     private static final long serialVersionUID = 8530206162841355351L;
     private static final Logger logger = LoggerFactory.getLogger(MessageParserException.class);
 
     private static final int OFFSET = VALIDATION_ERROR * CLASSIFICATION_FACTOR + 17000;  // offset for all codes in this class
     private static boolean textsInitialized = false;
 
     private String fieldName;   // if known, the name of the field where the error occured
     private String className;   // if known, the name of the class which contained the field
 
     static public final int MAY_NOT_BE_BLANK      = OFFSET + 1;
     static public final int NO_PATTERN_MATCH      = OFFSET + 2;
     static public final int TOO_MANY_ELEMENTS     = OFFSET + 3;
     static public final int TOO_LONG              = OFFSET + 4;
     static public final int TOO_SHORT             = OFFSET + 5;
    static public final int NOT_ENOUGH_ELEMENTS   = OFFSET + 6;
 
     // Upload textual descriptions only once they're needed for this type of exception class.
     // The idea is that in working environments, we will never need them ;-).
     // There is a small chance of duplicate initialization, because the access to the flag textsInitialized is not
     // synchronized, but duplicate upload does not hurt (is idempotent)
     static private void lazyInitialization() {
         synchronized (codeToDescription) {
             textsInitialized = true;
             codeToDescription.put(MAY_NOT_BE_BLANK     , "Empty, but required field");
             codeToDescription.put(NO_PATTERN_MATCH     , "Field contents does not match required pattern");
             codeToDescription.put(TOO_MANY_ELEMENTS    , "Array contains too many elements");
             codeToDescription.put(TOO_LONG             , "String is too long");
             codeToDescription.put(TOO_SHORT            , "String is too short");
            codeToDescription.put(NOT_ENOUGH_ELEMENTS  , "Array contains not enough elements");
         }
     }
 
     private final String getSpecificDescription() {
         return (className == null ? "?" : className) + "."
              + (fieldName == null ? "?" : fieldName);
     }
 
     private final void constructorSubroutine(String className, String fieldName) {
         this.fieldName = fieldName;
         this.className = className;
         if (!textsInitialized)
             lazyInitialization();
         // for the logger call, do NOT use toString, because that can be overridden, and we're called from a constructor here
         logger.error("Error " + getErrorCode() + " (" + getStandardDescription() + ") for " + getSpecificDescription());
     }
 
     public ObjectValidationException(int errorCode, String fieldName, String className) {
         super(errorCode, null);
         constructorSubroutine(className, fieldName);
     }
 
     public ObjectValidationException(int errorCode) {
         super(errorCode, null);
         constructorSubroutine(null, null);
     }
 
     @Override
     public String toString() {
         return getSpecificDescription() + ": " + super.toString();
     }
 }
