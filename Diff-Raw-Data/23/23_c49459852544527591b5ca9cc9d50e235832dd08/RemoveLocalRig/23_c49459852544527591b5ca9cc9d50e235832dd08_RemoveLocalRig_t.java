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
  * @date 17th February 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl;
 
 import java.util.Date;
 
 import org.hibernate.Session;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.LocalRigProviderActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigEventListener;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigEventListener.RigStateChangeEvent;
import au.edu.uts.eng.remotelabs.schedserver.rigprovider.identok.impl.IdentityTokenRegister;
 
 /**
  * Removes a local rig by setting it to inactive. The provided removal reason
  * is stored as the offline reason.
  */
 public class RemoveLocalRig
 {
     /** Rig DAO. */
     private final RigDao rigDao;
     
     /** Reason removing a rig failed. */
     private String failedReason;
     
     /** Logger. */
     private final Logger logger;
     
     public RemoveLocalRig()
     {
         this.logger = LoggerActivator.getLogger();
         this.rigDao = new RigDao();
     }
     
     public RemoveLocalRig(Session ses)
     {
         this.logger = LoggerActivator.getLogger();
         this.rigDao = new RigDao(ses);
     }
     
     /**
      * Removes the registration of a rig by setting the active flag to false 
      * and the contact URL to null. <br />
      * The rig record is not deleted.
      * 
      * @param name rig name
      * @param reason reason the rig is being removed
      * @return true if successful, false otherwise
      */
     public boolean removeRig(final String name, final String reason)
     {
         this.logger.debug("Removing the registration of rig '" + name + "' because of reason " + reason + '.');
         
         Rig rig = this.rigDao.findByName(name);
         if (rig == null)
         {
             this.logger.warn("Trying to remove the registration of a rig with name '" + name + "' that is not " +
             		"registered.");
             this.failedReason = "Not registered";
             return false;
         }
         
         rig.setActive(false);
         rig.setOnline(false);
         rig.setOfflineReason(reason);
         rig.setContactUrl(null);
         rig.setLastUpdateTimestamp(new Date());
         
         this.rigDao.flush();
         
        /* Remove the stored identity token. */
        IdentityTokenRegister.getInstance().removeIdentityToken(rig.getName());
        
         /* Provide notification a rig has been removed. */
         for (RigEventListener list : LocalRigProviderActivator.getRigEventListeners())
         {
             list.eventOccurred(RigStateChangeEvent.REMOVED, rig, this.rigDao.getSession());
         }
         return true;
     }
 
     /**
      * @return the failedReason
      */
     public String getFailedReason()
     {
         return this.failedReason;
     }
     
     /**
      * Returns the in use session.
      * 
      * @return session
      */
     public Session getSession()
     {
         return this.rigDao.getSession();
     }
 }
