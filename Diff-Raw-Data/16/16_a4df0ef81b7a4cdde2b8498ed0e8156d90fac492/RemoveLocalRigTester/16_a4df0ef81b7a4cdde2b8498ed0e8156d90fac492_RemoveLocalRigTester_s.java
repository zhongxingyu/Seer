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
  * @date 18th February 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.tests;
 
 
 import java.lang.reflect.Field;
 import java.util.Date;
 import java.util.Properties;
 
 import junit.framework.TestCase;
 
 import org.hibernate.Session;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigCapabilitiesDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigTypeDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.AcademicPermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Config;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.MatchingCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.MatchingCapabilitiesId;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociation;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociationId;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserClass;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserLock;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.logger.impl.SystemErrLogger;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.RemoveLocalRig;
 
 /**
  * Tests the {@link RemoveLocalRig} class.
  */
 public class RemoveLocalRigTester extends TestCase
 {
     /** Object of class under test. */
     private RemoveLocalRig remove;
     
     public RemoveLocalRigTester(String name) throws Exception
     {
         super(name);
         
         /* Set up the logger. */
         Field f = LoggerActivator.class.getDeclaredField("logger");
         f.setAccessible(true);
         f.set(null, new SystemErrLogger());
         
         /* Set up the SessionFactory. */
         AnnotationConfiguration cfg = new AnnotationConfiguration();
         Properties props = new Properties();
         props.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
         props.setProperty("hibernate.connection.url", "jdbc:mysql://127.0.0.1:3306/sahara");
         props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
         props.setProperty("hibernate.connection.username", "sahara");
         props.setProperty("hibernate.connection.password", "saharapasswd");
         props.setProperty("hibernate.show_sql", "true");
         props.setProperty("hibernate.format_sql", "true");
         props.setProperty("hibernate.use_sql_comments", "true");
         props.setProperty("hibernate.generate_statistics", "true");
         cfg.setProperties(props);
         cfg.addAnnotatedClass(AcademicPermission.class);
         cfg.addAnnotatedClass(Config.class);
         cfg.addAnnotatedClass(MatchingCapabilities.class);
         cfg.addAnnotatedClass(MatchingCapabilitiesId.class);
         cfg.addAnnotatedClass(RequestCapabilities.class);
         cfg.addAnnotatedClass(ResourcePermission.class);
         cfg.addAnnotatedClass(Rig.class);
         cfg.addAnnotatedClass(RigCapabilities.class);
         cfg.addAnnotatedClass(RigType.class);
         cfg.addAnnotatedClass(au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session.class);
         cfg.addAnnotatedClass(User.class);
         cfg.addAnnotatedClass(UserAssociation.class);
         cfg.addAnnotatedClass(UserAssociationId.class);
         cfg.addAnnotatedClass(UserClass.class);
         cfg.addAnnotatedClass(UserLock.class);
         
         f = DataAccessActivator.class.getDeclaredField("sessionFactory");
         f.setAccessible(true);
         f.set(null, cfg.buildSessionFactory());
     }
 
     @Override
     @Before
     public void setUp() throws Exception
     {
         this.remove = new RemoveLocalRig();
     }
     
     @Test
     public void testRemoveRig()
     {
         Session ses = DataAccessActivator.getNewSession();
         RigType type = new RigType("rlrType", 180, false);
         new RigTypeDao(ses).persist(type);
         RigCapabilities caps = new RigCapabilities("rlrA,rlrB");
         new RigCapabilitiesDao(ses).persist(caps);
         Rig rig = new Rig(type, caps, "rlr", "http://rlr", new Date(System.currentTimeMillis() - 86400001), // A day ago
                 true, null, false, false, true);
         rig.setActive(true);
         rig.setOnline(true);
         rig.setOfflineReason(null);
         new RigDao(ses).persist(rig);
         
         assertTrue(this.remove.removeRig("rlr", "Shutting down."));
         
         ses.refresh(rig);
         assertFalse(rig.isActive());
         assertFalse(rig.isOnline());
         assertNull(rig.getContactUrl());
         assertNotNull(rig.getOfflineReason());
         assertEquals("Shutting down.", rig.getOfflineReason());
         
         Date ts = rig.getLastUpdateTimestamp();
         assertNotNull(ts);
         assertTrue(new Date().after(ts));
         assertTrue(new Date(System.currentTimeMillis() - 5000).before(ts));
         
         ses.beginTransaction();
         ses.delete(rig);
         ses.delete(type);
         ses.delete(caps);
         ses.getTransaction().commit();
     }
     
     @Test
     public void testRemoveRigAlreadyInActive()
     {
         Session ses = DataAccessActivator.getNewSession();
         RigType type = new RigType("rlrType", 180, false);
         new RigTypeDao(ses).persist(type);
         RigCapabilities caps = new RigCapabilities("rlrA,rlrB");
         new RigCapabilitiesDao(ses).persist(caps);
         Rig rig = new Rig(type, caps, "rlr", "http://rlr", new Date(System.currentTimeMillis() - 86400001), // A day ago
                 true, null, false, false, true);
         rig.setActive(false);
         rig.setOnline(true);
         rig.setOfflineReason("foo bar");
         new RigDao(ses).persist(rig);
         
         assertTrue(this.remove.removeRig("rlr", "Shutting down."));
         
         ses.refresh(rig);
         assertFalse(rig.isActive());
         assertFalse(rig.isOnline());
         assertNull(rig.getContactUrl());
         assertNotNull(rig.getOfflineReason());
         assertEquals("Shutting down.", rig.getOfflineReason());
         
         Date ts = rig.getLastUpdateTimestamp();
         assertNotNull(ts);
         assertTrue(new Date().after(ts));
         assertTrue(new Date(System.currentTimeMillis() - 5000).before(ts));
         
         ses.beginTransaction();
         ses.delete(rig);
         ses.delete(type);
         ses.delete(caps);
         ses.getTransaction().commit();
     }
     
     @Test
     public void testRemoveRigNoRig()
     {
         assertFalse(this.remove.removeRig("removedRig", "Because I said so."));
         assertNotNull(this.remove.getFailedReason());
        assertEquals("Rig 'removedRig' does not exist.", this.remove.getFailedReason());
     }
 
 }
