 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 
 package org.amplafi.flow;
 
 /**
  * Here should be some useful constants for services.
  *
  */
 @Deprecated
 public class ServicesConstants {
     // would really like to fold this into FlowConstants.FSNEXT_FLOW
     // as of 3 Jan 2009 doesn't look like the this constant's value is used directly in javascript.
     @Deprecated
     public static final String FLOW_TYPE = "flow";
     // Used for callback registration
     public static final String FLOW_SERVICE_LISTENER = "fs";
     /**
      *
      */
     public static final String COOKIE_OBJECT = "cookieObject";
     /**
      * json object key containing an array of validation errors.
      */
    public static final String VALIDATION_ERRORS = "validationErrors";
     /**
      * json object key for the errormessage text.
      */
     public static final String ERROR_MESSAGE = "errorMessage";
 
 }
