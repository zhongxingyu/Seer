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
 * @date 6th April 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.queuer;
 
 import org.hibernate.Session;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.queuer.impl.Queue;
 
 /**
  * Attempts to run rig assignment, assigning the rig to the next highest 
  * precedence session. If no such session exists, the rig is not assigned.
  * This should be run when a rig is registered, a rig comes online or
  * a rig becomes free. 
  */
 public class QueueRun
 {
     /**
      * Attempts to assign the rig with the specified identifer.
      * 
      * @param id rig identifier
      */
     public static void attemptAssignment(long id)
     {
         Session db = DataAccessActivator.getNewSession();
         QueueRun.attemptAssignment(id, db);
         db.close();
     }
     
     /**
      * Attempts to assign the rig with the specified identifer.
      * The provided database session is used instead of openning a new
      * session.
      * 
      * @param id rig identifier
      * @param db database session
      */
     public static void attemptAssignment(long id, Session db)
     {
         Queue.getInstance().runRigAssignment(id, db);
     }
     
     /**
      * Attempts to assign the specified rig.
      * 
      * @param rig rig to allocate
      */
     public static void attemptAssignment(Rig rig)
     {
         RigDao dao = new RigDao();
         rig = dao.merge(rig);
         QueueRun.attemptAssignment(rig, dao.getSession());
         dao.closeSession();
     }
     
     /**
      * Attempts to assign the specified rig. The provided database session
      * is used instead of opening a new session.
      * 
      * @param rig rig to allocate
      * @param db database session
      */
     public static void attemptAssignment(Rig rig, Session db)
     {
         Queue.getInstance().runRigAssignment(rig.getId(), db);
     }
 }
