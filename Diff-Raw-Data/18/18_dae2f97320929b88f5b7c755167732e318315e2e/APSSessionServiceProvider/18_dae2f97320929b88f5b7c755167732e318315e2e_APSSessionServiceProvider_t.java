 /* 
  * 
  * PROJECT
  *     Name
  *         APS Session Service Provider
  *     
  *     Code Version
  *         0.9.0
  *     
  *     Description
  *         Provides an OSGi server wide session.
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
 package se.natusoft.osgi.aps.session.service;
 
 import se.natusoft.osgi.aps.api.misc.session.APSSession;
 import se.natusoft.osgi.aps.api.misc.session.APSSessionService;
 import se.natusoft.osgi.aps.tools.APSLogger;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * Provides an implementation of the APSSessionService service.
  */
 public class APSSessionServiceProvider implements APSSessionService {
     //
     // Private Members
     //
 
     /** The service logger. */
     private APSLogger logger = null;
 
     /** The active sessions. */
     private Map<UUID, APSSessionImpl> sessions = new HashMap<UUID, APSSessionImpl>();
 
     //
     // Constructors
     //
 
     /**
      * Creates a new APSSessionServiceProvider.
      *
      * @param logger The logger to log to.
      */
     public APSSessionServiceProvider(APSLogger logger) {
         this.logger = logger;
     }
 
     //
     // Methods
     //
 
     /**
      * Creates a new session.
      *
      * @param timeoutInMinutes The timeout in minutes.
      */
     @Override
     public APSSession createSession(int timeoutInMinutes) {
         UUID sessionId = UUID.randomUUID();
         APSSessionImpl session = new APSSessionImpl(sessionId, timeoutInMinutes);
         this.sessions.put(sessionId, session);
 
         return session;
     }
 
     /**
     * Creates a new session.
     * <p/>
     * The idea behind this variant is to support distributed sessions. The implementation must use a session id
     * that is unique enough to support this. The APS implementation uses java.util.UUID.
     *
     * @param sessionId The id of the session to create.
     * @param timeoutInMinutes The timeout in minutes.
     */
    @Override
    public APSSession createSession(String sessionId, int timeoutInMinutes) {
        UUID sessId = UUID.fromString(sessionId);
        APSSessionImpl session = new APSSessionImpl(sessId, timeoutInMinutes);
        this.sessions.put(sessId, session);

        return session;
    }

    /**
      * Looks up an existing session by its id.
      *
      * @param sessionId The id of the session to lookup.
      * @return A valid session having the specified id or null.
      */
     @Override
     public APSSession getSession(String sessionId) {
         APSSessionImpl session = this.sessions.get(UUID.fromString(sessionId));
         if (session != null && session.isValid()) {
             return session;
         }
 
         return null;
     }
 
     /**
      * Closes the session represented by the specified id. After this call APSSession.isValid() on an APSSession
      * representing this session will return false.
      *
      * @param sessionId The id of the session to close.
      */
     @Override
     public void closeSession(String sessionId) {
         APSSessionImpl session = this.sessions.remove(UUID.fromString(sessionId));
         if (session != null) {
             session.invalidate();
         }
     }
 }
