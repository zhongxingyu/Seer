 /**
  * Project Wonderland
  *
  * $RCSfile:$
  *
  * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision:$
  * $Date:$
  * $State:$
  */
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jdesktop.wonderland.client.ClientPlugin;
 
 import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
 import org.jdesktop.wonderland.client.comms.SessionStatusListener;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.login.LoginManager;
 import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
 
 /**
  * Plugin to support the audio manager
  * @author jprovino
  */
 public class AudioManagerClientPlugin 
         implements ClientPlugin, SessionLifecycleListener, SessionStatusListener
 {
     private static final Logger logger =
             Logger.getLogger(AudioManagerClientPlugin.class.getName());
     
     private AudioManagerClient client;
     
     public void initialize(LoginManager loginManager) {
        logger.warning("Audio manager initialized");
 
         loginManager.addLifecycleListener(this);
     }
 
     public void sessionCreated(WonderlandSession session) {
     }
 
     public void primarySession(WonderlandSession session) {
         session.addSessionStatusListener(this);
         if (session.getStatus() == WonderlandSession.Status.CONNECTED) {
             connectClient(session);
         }
     }
 
     public void sessionStatusChanged(WonderlandSession session, 
                                      WonderlandSession.Status status)
     {
         logger.fine("session status changed " + session + " status " + status);
         if (status.equals(WonderlandSession.Status.CONNECTED)) {
             connectClient(session);
         } else if (status.equals(WonderlandSession.Status.DISCONNECTED)) {
             disconnectClient();
         }
     }
     
     /**
      * Connect the client.
      * @param session the WonderlandSession to connect to, guaranteed to
      * be in the CONNECTED state.
      */
     protected void connectClient(WonderlandSession session) {
         if (client == null) {
             try {
                 client = new AudioManagerClient(session);
             } catch (ConnectionFailureException e) {
                 logger.log(Level.WARNING, "Connect client error", e);
             }
         }
     }
 
     /**
      * Disconnect the client
      */
     protected void disconnectClient() {
         client.disconnect();
         client = null;
     }
 }
