 /*
  * Copyright (c) 2013 Kinvey Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.kinvey.business_logic;
 
 import java.util.HashMap;
 
 /**
  * @author mjsalinger
  * @since 2.0
  */
 public class KinveyError {
 
     private static final String KINVEY_USER_ERROR = "UserError";
     private HashMap<String, Object> errorBody = new HashMap<String, Object>();
     private int code;
 
 
     public KinveyError(int code, String error, String description, String debug, Exception ex) {
         errorBody.put("error", error);
         errorBody.put("description", description);
         String debugMsg = new StringBuilder()
                 .append(ex != null ? ex.getClass().getSimpleName() : KINVEY_USER_ERROR)
                 .append(": ")
                 .append(debug)
                 .toString();
         errorBody.put("debugMessage", debugMsg);
         this.code = 400;
     }
 
     public HashMap<String, Object> getErrorBody() {
         return errorBody;
     }
 
     public int getErrorCode() {
         return code;
     }
 
     public static class Builder {
         private static final String BL_RUNTIME_ERROR = "BLRuntimeError";
         private static final String BL_RUNTIME_DESCRIPTION = "The Business Logic script has a runtime error. See debug message for details.";
         private int code;
         private String error;
         private String description;
         private String debug;
         private Exception exception;
 
         public Builder() {
             this.code = 400;
             this.error=BL_RUNTIME_ERROR;
             this.description = BL_RUNTIME_DESCRIPTION;
         }
 
         public KinveyError build() {
             if (exception != null && debug == null) {
                 debug = exception.getMessage();
             }
             return new KinveyError(code, error, description, debug, exception);
         }
 
         public String getDebug() {
             return debug;
         }
 
         public Builder setDebug(String debug) {
             this.debug = debug;
             return this;
         }
 
         public Exception getException() {
             return exception;
         }
 
         public Builder setException(Exception exception) {
             this.exception = exception;
             return this;
         }
     }
 
 }
