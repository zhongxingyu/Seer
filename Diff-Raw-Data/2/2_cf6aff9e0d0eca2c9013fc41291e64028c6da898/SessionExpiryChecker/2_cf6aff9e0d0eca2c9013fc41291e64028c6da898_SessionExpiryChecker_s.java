 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 8th April 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.session.impl;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.criterion.Restrictions;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.queuer.QueueInfo;
 
 /**
  * Either expires or extends the time of all sessions which are close to 
  * the session duration duration.
  */
 public class SessionExpiryChecker implements Runnable
 {
     /** Logger. */
     private Logger logger;
     
     /** Flag to specify if this is a test run. */
     private boolean notTest = true;
     
     public SessionExpiryChecker()
     {
         this.logger = LoggerActivator.getLogger();
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public void run()
     {
         org.hibernate.Session db = null;
         
         try
         {
             db = DataAccessActivator.getNewSession();        
             
             boolean kicked = false;
             
             if (db == null)
             {
                 this.logger.warn("Unable to obtain a database session, for rig session status checker. Ensure the " +
                 		"SchedulingServer-DataAccess bundle is installed and active.");
                 return;
             }
             
             Criteria query = db.createCriteria(Session.class);
             query.add(Restrictions.eq("active", Boolean.TRUE))
                  .add(Restrictions.isNotNull("assignmentTime"));
             
             Date now = new Date();
             
             List<Session> sessions = query.list();
             for (Session ses : sessions)
             {
                 ResourcePermission perm = ses.getResourcePermission();
                 int remaining = perm.getSessionDuration() + // The allowed session time
                         (perm.getAllowedExtensions() - ses.getExtensions()) * perm.getExtensionDuration() -  // Extension time
                         Math.round((System.currentTimeMillis() - ses.getAssignmentTime().getTime()) / 1000); // In session time
     
                 /******************************************************************
                  * For sessions that have been marked for termination, terminate  * 
                  * those that have no more remaining time.                        *
                  ******************************************************************/
                 if (ses.isInGrace())
                 {
                     if (remaining <= 0)
                     {
                         ses.setActive(false);
                         ses.setRemovalTime(now);
                         db.beginTransaction();
                         db.flush();
                         db.getTransaction().commit();
                         
                         this.logger.info("Terminating session for " + ses.getUserNamespace() + ':' + ses.getUserName() + " on " +
                                 ses.getAssignedRigName() + " because it is expired and the grace period has elapsed.");
                         if (this.notTest) new RigReleaser().release(ses, db);
                     }
                 }
                 /******************************************************************
                  * For sessions with remaining time less than the grace duration: *
                  *    1) If the session has no remaining extensions, mark it for  *
                  *       termination.                                             *
                  *    2) Else, if the session rig is queued, mark it for          *
                  *       termination.                                             *
                  *    3) Else, extend the sessions time.                          *
                  ******************************************************************/
                 else if (remaining < ses.getRig().getRigType().getLogoffGraceDuration())
                 {
                     /* Need to make a decision whether to extend time or set for termination. */
                     if (ses.getExtensions() == 0)
                     {
                         this.logger.info("Session for " + ses.getUserNamespace() + ':' + ses.getUserName() + " on " +
                                 "rig " + ses.getAssignedRigName() + " is expired and cannot be extended. Marking " +
                                 "session for expiry and giving a grace period.");
                         ses.setInGrace(true);
                         ses.setRemovalReason("No more session time extensions.");
                         db.beginTransaction();
                         db.flush();
                         db.getTransaction().commit();
                         
                         /* Notification warning. */
                         if (this.notTest) new RigNotifier().notify("Your session will expire in " + remaining + " seconds. " +
                         		"Please finish and exit.", ses, db);
                     }
                     else if (QueueInfo.isQueued(ses.getRig(), db))
                     {
                         this.logger.info("Session for " + ses.getUserNamespace() + ':' + ses.getUserName() + " on " +
                                 "rig " + ses.getAssignedRigName() + " is expired and the rig is queued. Marking " +
                                 "session for expiry and giving a grace period.");
                         ses.setInGrace(true);
                         ses.setRemovalReason("Rig is queued.");
                         db.beginTransaction();
                         db.flush();
                         db.getTransaction().commit();
                         
                         /* Notification warning. */
                         if (this.notTest) new RigNotifier().notify("Your session will end in " + remaining + " seconds. " +
                                 "After this you be removed, so please logoff.", ses, db);
                     }
                     else
                     {
                         this.logger.info("Session for " + ses.getUserNamespace() + ':' + ses.getUserName() + " on " +
                                 "rig " + ses.getAssignedRigName() + " is expired and is having its session time extended.");
                         ses.setExtensions((short)(ses.getExtensions() - 1));
                         db.beginTransaction();
                         db.flush();
                         db.getTransaction().commit();
                     }
                 }
                 /******************************************************************
                  * For sessions created with a user class that can be kicked off, * 
                  * if the rig is queued, the user is kicked off immediately.      *
                  ******************************************************************/
                  /* DODGY The 'kicked' flag is to only allow a single kick per 
                   * pass. This is allow time for the rig to be released and take the
                   * queued session. This is a hack at best, but should be addressed 
                   * by a released - cleaning up meta state. */
                 else if (!kicked && QueueInfo.isQueued(ses.getRig(), db) && perm.getUserClass().isKickable())
                 {
                     kicked = true;
                     
                     /* No grace is being given. */
                     this.logger.info("A kickable user is using a rig that is queued for, so they are being removed.");
                     ses.setActive(false);
                     ses.setRemovalTime(now);
                     ses.setRemovalReason("Resource was queued and user was kickable."); 
                     db.beginTransaction();
                     db.flush();
                     db.getTransaction().commit();
                     
                     if (this.notTest) new RigReleaser().release(ses, db);
                 }
                 /******************************************************************
                  * Finally, for sessions with time still remaining, check         *
                  * session activity timeout.                                      * 
                  ******************************************************************/
                 else if ((System.currentTimeMillis() - ses.getActivityLastUpdated().getTime()) / 1000 > perm.getSessionActivityTimeout())
                 {
                     /* Check activity. */
                     if (this.notTest) new SessionIdleKicker().kickIfIdle(ses, db);
                 }
             }
            
            db.close();
         }
         catch (HibernateException hex)
         {
             this.logger.error("Failed to query database to expired sessions (Exception: " + 
                     hex.getClass().getName() + ", Message:" + hex.getMessage() + ").");
             
             if (db != null && db.getTransaction() != null)
             {
                 try
                 {
                     db.getTransaction().rollback();
                 }
                 catch (HibernateException ex)
                 {
                     this.logger.error("Exception rolling back up session expiry transaction (Exception: " + 
                             ex.getClass().getName() + "," + " Message: " + ex.getMessage() + ").");
                 }
             }
         }
         finally
         {
             try
             {
                 if (db != null) db.close();
             }
             catch (HibernateException ex)
             {
                 this.logger.error("Exception cleaning up database session (Exception: " + ex.getClass().getName() + "," +
                         " Message: " + ex.getMessage() + ").");
             }
         }
     }
 }
