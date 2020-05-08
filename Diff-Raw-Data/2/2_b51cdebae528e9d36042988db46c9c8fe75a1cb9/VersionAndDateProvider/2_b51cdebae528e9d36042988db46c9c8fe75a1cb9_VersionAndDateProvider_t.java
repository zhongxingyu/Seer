 /**
  * Copyright 2012 Terremark Worldwide Inc.
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
 package com.terremark.impl;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 /**
  * API Version and current date/time provider. API version and current client date/time needs to be set as HTTP headers
  * for every API call HTTP request. This class provides that information.
  *
  * @author <a href="mailto:spasam@terremark.com">Seshu Pasam</a>
  */
 public final class VersionAndDateProvider {
     /** HTTP date/time header format */
     private static final String HTTP_HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
 
     /** API version as configured/set by user */
     private String version;
     /** Clock skew between the API end point server and the client machine */
     private long clockSkew;
 
     /**
      * Calculates the clock skew between the client machine and the Terremark API end point. This method should be
      * package visible. But it is public for the purpose of testing
      *
      * @param currentServerTime Current end point date and time.
      */
     public void calculateClockSkew(final Date currentServerTime) {
         this.clockSkew = System.currentTimeMillis() - currentServerTime.getTime();
     }
 
     /**
      * Set the API version to use for all the client requests. This method should be package visible. But it is public
      * for the purpose of testing
      *
      * @param version API version to use.
      */
     public void setVersion(final String version) {
         this.version = version;
     }
 
     /**
      * Returns the API version to use as configure by the user.
      *
      * @return API version as string.
      */
     public String getVersion() {
         return version;
     }
 
     /**
      * Returns the current date/time of the client machine. The value returned is not the absolute client date/time. The
      * current date/time is updated with the clock skew between the server and the client machine. Currently clock skew
      * between API end point server and the client machine is calculated only once. This probably should be periodically
      * updated.
      *
      * @return Current client date/time.
      */
     public String getDate() {
         // SimpleDateFormat is not thread safe, so we always create one
        final SimpleDateFormat sdf = new SimpleDateFormat(HTTP_HEADER_DATE_FORMAT, Locale.ENGLISH);
         sdf.setTimeZone(TerremarkConstants.GMT_TIME_ZONE);
 
         return sdf.format(new Date(System.currentTimeMillis() - clockSkew));
     }
 }
