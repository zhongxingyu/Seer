 /* 
  * 
  * PROJECT
  *     Name
  *         APS APIs
  *     
  *     Code Version
  *         0.9.0
  *     
  *     Description
  *         Provides the APIs for the application platform services.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2012 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     Tommy Svensson (tommy@natusoft.se)
  *         Changes:
  *         2012-09-08: Created!
  *         
  */
 package se.natusoft.osgi.aps.api.misc.session;
 
 /**
  * This is not a http session! It is a simple session that can be used by any code running in the same OSGi server.
  */
 public interface APSSessionService {
 
     /** Specifies no timeout. */
     public static int NO_TIMEOUT = 0;
 
     /** Specifies the default timeout. */
     public static int DEFAULT_TIMEOUT = -1;
 
     /** A relatively short timeout. */
     public static int SHORT_TIMEOUT = 5;
 
     /** A medium timeout. */
     public static int MEDIUM_TIMEOUT = 10;
 
     /** A relatively long timeout. */
     public static int LONG_TIMEOUT = 20;
 
     /**
      * Creates a new session.
      *
      * @param timeoutInMinutes The timeout in minutes.
      */
     APSSession createSession(int timeoutInMinutes);
 
     /**
      * Looks up an existing session by its id.
      *
      * @param sessionId The id of the session to lookup.
      *
      * @return A valid session having the specified id or null.
      */
     APSSession getSession(String sessionId);
 
     /**
      * Closes the session represented by the specified id. After this call APSSession.isValid() on an APSSession
      * representing this session will return false.
      *
      * @param sessionId The id of the session to close.
      */
     void closeSession(String sessionId);
 }
