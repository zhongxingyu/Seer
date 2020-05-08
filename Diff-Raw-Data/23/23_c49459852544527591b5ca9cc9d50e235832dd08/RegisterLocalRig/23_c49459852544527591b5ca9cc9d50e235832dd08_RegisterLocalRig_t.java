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
  * @date 17th February 2010 */
 package au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl;
 
 import java.util.Date;
 
 import org.hibernate.Session;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigCapabilitiesDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigTypeDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.LocalRigProviderActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigEventListener;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigEventListener.RigStateChangeEvent;
import au.edu.uts.eng.remotelabs.schedserver.rigprovider.identok.impl.IdentityTokenRegister;
 
 /**
  * Adds a rig to the Scheduling Server.
  */
 public class RegisterLocalRig
 {   
     /** The rig who is being added. */
     private Rig rig;
     
     /** Rig data access object. */
     private final RigDao rigDao;
     
     /** Rig Type data access object. */
     private final RigTypeDao typeDao;
     
     /** Rig Capabilities data access object. */
     private final RigCapabilitiesDao capsDao;
     
     /** Reason adding the rig failed. */
     private String failedReason;
     
     /** Logger. */
     private final Logger logger;
 
     public RegisterLocalRig()
     {   
         this.logger = LoggerActivator.getLogger();
         
         this.rigDao = new RigDao();
         this.typeDao = new RigTypeDao(this.rigDao.getSession());
         this.capsDao = new RigCapabilitiesDao(this.rigDao.getSession());
     }
 
     /** 
      * Registers a local rig with the provided parameters to the scheduling server.
      * The response specifies registration failed, the error reason field will be
      * set with a reason it failed.
      * <br />
      * The rig name must be unique in the system. If an active rig with the 
      * same name exists, registration will fail. If the type does not exist,
      * a new type is created. If the rig capabilities string are unqiue, 
      * it is added as a new capabilities type.
      * 
      * @param name name of the rig
      * @param type type name of the rig
      * @param capabilities rigs capabilities strings
      * @param contactUrl URL that points the rigs SOAP interface
      * @return true if successful, false otherwise
      */
     public boolean registerRig(final String name, final String type, final String capabilities,
             final String contactUrl)
     {
         this.logger.debug("Attempting to add a new rig with name '" + name + "', of type '" + type + "' with " +
         		"capabilities '" + capabilities + "'.");
         
         this.rig = this.rigDao.findByName(name);
         if (this.rig != null && this.rig.isActive())
         {
             /* Case 1: Rig exists and is active - cannot register rig because another RC owns it. */
             this.logger.warn("Failed registering rig with name '" + name + "' as it already exists and " +
             		"is active. Rig names must be unqiue.");
             this.failedReason = "Exists";
             return false;
         }
         else if (this.rig != null)
         {
             /* Case 2: Rig exists, but does not exist, so the new RC is free to take it. */
             this.logger.info("Registering an inactive existing rig with name '" + name + "' and existing " +
             		"record identifier '" + this.rig.getId() + "'.");
         }
         else
         {
             /* Case 3: Rig does not exist. */
             this.logger.debug("Registering a new rig with name '" + name + "'.");
             this.rig = new Rig();
             this.rig.setName(name);
         }
         
         RigType rigType = this.typeDao.loadOrCreate(type);
         this.rig.setRigType(rigType);
         
         RigCapabilities caps = this.capsDao.findCapabilites(capabilities);
         if (caps == null)
         {
             /* Doesn't exist so add it as a new rig capabilities. */
             caps = this.capsDao.addCapabilities(capabilities);
         }
         this.rig.setRigCapabilities(caps);
         
         this.rig.setContactUrl(contactUrl);
         this.rig.setActive(true);
         this.rig.setLastUpdateTimestamp(new Date());
         this.rig.setOnline(false);
         this.rig.setOfflineReason("Registered but no status received.");
         this.rig.setInSession(false);
         this.rig.setManaged(true); // All local rigs are managed
         
         if (this.rig.getId() == null || this.rig.getId() < 1)
         {
             this.rig = this.rigDao.persist(this.rig);
         }
         this.rigDao.flush();
         
        /* Generate an identity token, before firing events in case subsequent 
         * authenticated calls need to be made. */
        IdentityTokenRegister.getInstance().generateIdentityToken(this.rig.getName());
        
         /* Provide notification a new rig is registered. */
         for (RigEventListener list : LocalRigProviderActivator.getRigEventListeners())
         {
             list.eventOccurred(RigStateChangeEvent.REGISTERED, this.rig, this.rigDao.getSession());
         }
 
         return this.rig.getId() > 0;
     }
 
     /**
      * Returns registering a rig failed.
      * 
      * @return the failedReason
      */
     public String getFailedReason()
     {
         return this.failedReason;
     }
     
     /**
      * Returns a persistent instance of the registered rig.
      * 
      * @return persistent rig instance
      */
     public Rig getRegisteredRig()
     {
         return this.rig;
     }
     
     /**
      * Returns the session the registered rig (as returned by 
      * <code>getRegisteredRig</code>), is attached too. 
      * 
      * @return session
      */
     public Session getSession()
     {
         return this.rigDao.getSession();
     }
 }
