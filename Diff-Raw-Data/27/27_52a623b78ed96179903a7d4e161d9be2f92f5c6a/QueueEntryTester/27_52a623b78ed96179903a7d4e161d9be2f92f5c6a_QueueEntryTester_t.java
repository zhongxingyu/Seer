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
  * @date 4th January 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.queuer.impl.tests;
 
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Date;
import java.util.HashMap;
 import java.util.Properties;
 
 import junit.framework.TestCase;
 
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RequestCapabilitiesDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.AcademicPermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Config;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.MatchingCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.MatchingCapabilitiesId;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociation;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociationId;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserClass;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserLock;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.logger.impl.SystemErrLogger;
import au.edu.uts.eng.remotelabs.schedserver.queuer.impl.InnerQueue;
import au.edu.uts.eng.remotelabs.schedserver.queuer.impl.Queue;
 import au.edu.uts.eng.remotelabs.schedserver.queuer.impl.QueueEntry;
 
 /**
  * Tests the {@link QueueEntry} class.
  */
 public class QueueEntryTester extends TestCase
 {
     /* Object of class under test. */
     private QueueEntry entry;
     
     /* Session for test. */
     private org.hibernate.Session session;
     
     public QueueEntryTester(String name) throws Exception
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
        Queue queue = Queue.getInstance();
        Field f = Queue.class.getDeclaredField("rigQueues");
        f.setAccessible(true);
        f.set(queue, new HashMap<Long, InnerQueue>());
        f = Queue.class.getDeclaredField("typeQueues");
        f.setAccessible(true);
        f.set(queue, new HashMap<Long, InnerQueue>());
        f = Queue.class.getDeclaredField("capabilityQueues");
        f.setAccessible(true);
        f.set(queue, new HashMap<Long, InnerQueue>());

        f = Queue.class.getDeclaredField("notTest");
        f.setAccessible(true);
        f.set(queue, Boolean.FALSE);
     
         this.session = DataAccessActivator.getNewSession();
         this.entry = new QueueEntry(this.session);
     }
     
     @Override
     @After
     public void tearDown() throws Exception
     {
         this.session.close();
     }
     
     @Test
     public void testIsInQueue()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         Date now = new Date();
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(false);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         
         Session ses = new Session();
         ses.setActive(true);
         ses.setReady(true);
         ses.setActivityLastUpdated(now);
         ses.setExtensions((short) 5);
         ses.setPriority((short) 5);
         ses.setRequestTime(now);
         ses.setRequestedResourceId(r.getId());
         ses.setRequestedResourceName(r.getName());
         ses.setResourceType("RIG");
         ses.setResourcePermission(p1);
         ses.setUser(user);
         ses.setUserName(user.getName());
         ses.setUserNamespace(user.getNamespace());
         this.session.persist(ses);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(ses);
                 
         boolean inQueue = this.entry.isInQueue(user); 
         
         this.session.beginTransaction();
         this.session.delete(ses);
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(inQueue);
     }
     
         @Test
     public void testAddToQueue()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         Date now = new Date();
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         uc1.setPriority((short)4);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(false);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         p1.setAllowedExtensions((short)10);
         this.session.persist(p1);
         
         Session ses = new Session();
         ses.setActive(true);
         ses.setReady(true);
         ses.setActivityLastUpdated(now);
         ses.setExtensions((short) 5);
         ses.setPriority((short) 5);
         ses.setRequestTime(now);
         ses.setRequestedResourceId(r.getId());
         ses.setRequestedResourceName(r.getName());
         ses.setResourceType("RIG");
         ses.setResourcePermission(p1);
         ses.setUser(user);
         ses.setUserName(user.getName());
         ses.setUserNamespace(user.getNamespace());
         this.session.persist(ses);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(ses);
                 
         assertTrue(this.entry.isInQueue(user));
         assertTrue(this.entry.hasPermission(p1.getId()));
         assertTrue(this.entry.canUserQueue());
         assertTrue(this.entry.addToQueue("/foo/bar"));
         
         Session ent = this.entry.getActiveSession();
         assertNotNull(ent);
         assertTrue(ent.isActive());
         assertFalse(ent.isReady());
         assertEquals(uc1.getPriority(), ent.getPriority());
         assertEquals(p1.getAllowedExtensions(), ent.getExtensions());
         assertEquals(Math.floor(now.getTime() / 1000), Math.floor(ent.getActivityLastUpdated().getTime() / 1000));
         assertEquals("/foo/bar", ent.getCodeReference());
         assertEquals(Math.floor(now.getTime() / 1000), Math.floor(ent.getRequestTime().getTime() / 1000));
         assertEquals("RIG", ent.getResourceType());
         assertEquals(r.getId(), ent.getRequestedResourceId());
         assertEquals(r.getName(), ent.getRequestedResourceName());
         assertEquals(p1.getId(), ent.getResourcePermission().getId());
         assertEquals(user.getId(), ent.getUser().getId());
         assertEquals(user.getName(), ent.getUserName());
         assertEquals(user.getNamespace(), ent.getUserNamespace());
         
         this.session.beginTransaction();
         this.session.delete(ent);
         this.session.delete(ses);
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
     }
     
     @Test
     public void testIsInQueueNotIn()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         Date now = new Date();
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(false);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         
         Session ses = new Session();
         ses.setActive(false);
         ses.setReady(true);
         ses.setActivityLastUpdated(now);
         ses.setExtensions((short) 5);
         ses.setPriority((short) 5);
         ses.setRequestTime(now);
         ses.setRequestedResourceId(r.getId());
         ses.setRequestedResourceName(r.getName());
         ses.setResourceType("RIG");
         ses.setResourcePermission(p1);
         ses.setUser(user);
         ses.setUserName(user.getName());
         ses.setUserNamespace(user.getNamespace());
         this.session.persist(ses);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(ses);
                 
         boolean inQueue = this.entry.isInQueue(user); 
         
         this.session.beginTransaction();
         this.session.delete(ses);
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(inQueue);
     }
     
     @Test
     public void testCanQueueRig()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(false);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
     
     @Test
     public void testCanQueueRigOffline()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(false);
         r.setInSession(false);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertFalse(canQue);
     }
     
     
     @Test
     public void testCanQueueRigInUse()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         this.session.persist(r);
         
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
     
     @Test
     public void testCanQueueRigQueue()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(false);
         this.session.persist(uc1);
         
         UserClass uc2 = new UserClass();
         uc2.setName("uc2");
         uc2.setActive(true);
         uc2.setQueuable(true);
         this.session.persist(uc2);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         UserAssociation ass2 = new UserAssociation(new UserAssociationId(user.getId(), uc2.getId()), uc2, user);
         this.session.persist(ass2);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("RIG");
         p2.setUserClass(uc2);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRig(r);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(uc2);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(ass2);
         this.session.delete(uc1);
         this.session.delete(uc2);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
     
     @Test
     public void testCanQueueRigNoQueue()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(false);
         this.session.persist(uc1);
         
         UserClass uc2 = new UserClass();
         uc2.setName("uc2");
         uc2.setActive(true);
         uc2.setQueuable(false);
         this.session.persist(uc2);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         UserAssociation ass2 = new UserAssociation(new UserAssociationId(user.getId(), uc2.getId()), uc2, user);
         this.session.persist(ass2);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("RIG");
         p2.setUserClass(uc2);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRig(r);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(uc2);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(ass2);
         this.session.delete(uc1);
         this.session.delete(uc2);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertFalse(canQue);
     }
     
     @Test
     public void testCanQueueType()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(false);
         r.setInSession(false);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(true);
         r1.setInSession(false);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(false);
         r3.setInSession(false);
         this.session.persist(r3);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("TYPE");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRigType(rt);
         this.session.persist(p1);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
     
     @Test
     public void testCanQueueTypeLocked()
     {
         Date before = new Date(System.currentTimeMillis() - 100000);
         Date after = new Date(System.currentTimeMillis() + 100000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(false);
         r.setInSession(false);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(true);
         r1.setInSession(false);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(false);
         r3.setInSession(false);
         this.session.persist(r3);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("TYPE");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRigType(rt);
         this.session.persist(p1);
         
         UserLock lock = new UserLock(user, p1, true, "abc123");
         this.session.persist(lock);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(lock);
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertFalse(canQue);
     }
     
     @Test
     public void testCanQueueTypeLockedUnlocked()
     {
         Date before = new Date(System.currentTimeMillis() - 100000);
         Date after = new Date(System.currentTimeMillis() + 100000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(false);
         r.setInSession(false);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(true);
         r1.setInSession(false);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(false);
         r3.setInSession(false);
         this.session.persist(r3);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("TYPE");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRigType(rt);
         this.session.persist(p1);
         
         UserLock lock = new UserLock(user, p1, false, "abc123");
         this.session.persist(lock);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(lock);
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
 
     @Test
     public void testCanQueueTypeOffline()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(false);
         r.setInSession(false);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("TYPE");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRigType(rt);
         this.session.persist(p1);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertFalse(canQue);
     }
 
     @Test
     public void testCanQueueTypeNoQueue()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(false);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("TYPE");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRigType(rt);
         this.session.persist(p1);
 
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertFalse(canQue);
     }
 
     @Test
     public void testCanQueueCaps()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(false);
         r.setInSession(false);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(true);
         r1.setInSession(false);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(false);
         r3.setInSession(false);
         this.session.persist(r3);
         this.session.getTransaction().commit();
 
         this.entry.isInQueue(user);
         RequestCapabilitiesDao dao = new RequestCapabilitiesDao(this.session);
         RequestCapabilities reqCaps = dao.addCapabilities("perm");
         
         this.session.beginTransaction();
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("CAPABILITY");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRequestCapabilities(reqCaps);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(reqCaps);
         this.session.refresh(caps);
         
         boolean res = this.entry.hasPermission("CAPABILITY", 0, reqCaps.getCapabilities());
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.getTransaction().commit();
         
         dao.delete(reqCaps);
         
         this.session.beginTransaction();
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
     
     @Test
     public void testCanQueueCapsAllOffline()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(false);
         r.setInSession(false);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(false);
         r1.setInSession(false);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(false);
         r3.setInSession(false);
         this.session.persist(r3);
         this.session.getTransaction().commit();
         
         RequestCapabilitiesDao dao = new RequestCapabilitiesDao(this.session);
         RequestCapabilities reqCaps = dao.addCapabilities("perm");
         
         this.session.beginTransaction();
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("CAPABILITY");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRequestCapabilities(reqCaps);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(reqCaps);
         this.session.refresh(caps);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("CAPABILITY", 0, reqCaps.getCapabilities());
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.getTransaction().commit();
         
         dao.delete(reqCaps);
         
         this.session.beginTransaction();
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertFalse(canQue);
     }
     
     @Test
     public void testCanQueueCapsQueue()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(true);
         r1.setInSession(true);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(true);
         r3.setInSession(true);
         this.session.persist(r3);
         this.session.getTransaction().commit();
         
         RequestCapabilitiesDao dao = new RequestCapabilitiesDao(this.session);
         RequestCapabilities reqCaps = dao.addCapabilities("perm");
         
         this.session.beginTransaction();
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("CAPABILITY");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRequestCapabilities(reqCaps);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(reqCaps);
         this.session.refresh(caps);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("CAPABILITY", 0, reqCaps.getCapabilities());
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.getTransaction().commit();
         
         dao.delete(reqCaps);
         
         this.session.beginTransaction();
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
     
     @Test
     public void testCanQueueCapsNoQueue()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(false);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(true);
         r1.setInSession(false);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(true);
         r3.setInSession(true);
         this.session.persist(r3);
         this.session.getTransaction().commit();
         
         RequestCapabilitiesDao dao = new RequestCapabilitiesDao(this.session);
         RequestCapabilities reqCaps = dao.addCapabilities("perm");
         
         this.session.beginTransaction();
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("CAPABILITY");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRequestCapabilities(reqCaps);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(reqCaps);
         this.session.refresh(caps);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("CAPABILITY", 0, reqCaps.getCapabilities());
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.getTransaction().commit();
         
         dao.delete(reqCaps);
         
         this.session.beginTransaction();
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertTrue(canQue);
     }
     
     @Test
     public void testCanQueueCapsNoQueueNonFree()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(false);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         this.session.persist(r);
         
         Rig r1 = new Rig();
         r1.setName("Perm_Rig_Test_Rig2");
         r1.setRigType(rt);
         r1.setRigCapabilities(caps);
         r1.setLastUpdateTimestamp(before);
         r1.setActive(true);
         r1.setOnline(true);
         r1.setInSession(true);
         this.session.persist(r1);
         
         Rig r3 = new Rig();
         r3.setName("Perm_Rig_Test_Rig3");
         r3.setRigType(rt);
         r3.setRigCapabilities(caps);
         r3.setLastUpdateTimestamp(before);
         r3.setActive(true);
         r3.setOnline(true);
         r3.setInSession(true);
         this.session.persist(r3);
         this.session.getTransaction().commit();
 
         this.entry.isInQueue(user);
         RequestCapabilitiesDao dao = new RequestCapabilitiesDao(this.session);
         RequestCapabilities reqCaps = dao.addCapabilities("perm");
         
         this.session.beginTransaction();
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("CAPABILITY");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRequestCapabilities(reqCaps);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
         this.session.refresh(reqCaps);
         this.session.refresh(caps);
         
         boolean res = this.entry.hasPermission("CAPABILITY", 0, reqCaps.getCapabilities());
         boolean canQue = this.entry.canUserQueue();
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.getTransaction().commit();
         
         dao.delete(reqCaps);
         
         this.session.beginTransaction();
         this.session.delete(r);
         this.session.delete(r1);
         this.session.delete(r3);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         assertFalse(canQue);
     }
 
     @Test
     public void testHasPermissionResRig()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
     }
     
     @Test
     public void testHasPermissionResRigQueuable()
     {
         Date before = new Date(System.currentTimeMillis() - 1000000);
         Date after = new Date(System.currentTimeMillis() + 1000000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setKickable(false);
         uc1.setQueuable(false);
         this.session.persist(uc1);
         
         UserClass uc2 = new UserClass();
         uc2.setName("uc2");
         uc2.setActive(true);
         uc2.setKickable(false);
         uc2.setQueuable(true);
         this.session.persist(uc2);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         UserAssociation ass2 = new UserAssociation(new UserAssociationId(user.getId(), uc2.getId()), uc2, user);
         this.session.persist(ass2);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("RIG");
         p2.setUserClass(uc2);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRig(r);
         this.session.persist(p2);
         
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(uc2);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(ass2);
         this.session.delete(uc2);
         this.session.delete(uc1);
         this.session.delete(uc2);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         ResourcePermission f = this.entry.getResourcePermission();
         assertNotNull(f);
         assertTrue(f.getUserClass().isQueuable());
         assertEquals(f.getId(), p2.getId());
         assertEquals(f.getUserClass().getId(), uc2.getId());
     }
     
     @Test
     public void testHasPermissionResRigKickable()
     {
         Date before = new Date(System.currentTimeMillis() - 1000000);
         Date after = new Date(System.currentTimeMillis() + 1000000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         uc1.setQueuable(true);
         uc1.setKickable(true);
         this.session.persist(uc1);
         
         UserClass uc2 = new UserClass();
         uc2.setName("uc2");
         uc2.setActive(true);
         uc2.setKickable(false);
         uc2.setQueuable(true);
         this.session.persist(uc2);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         UserAssociation ass2 = new UserAssociation(new UserAssociationId(user.getId(), uc2.getId()), uc2, user);
         this.session.persist(ass2);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("RIG");
         p2.setUserClass(uc2);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRig(r);
         this.session.persist(p2);
         
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(uc2);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(ass2);
         this.session.delete(uc2);
         this.session.delete(uc1);
         this.session.delete(uc2);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
         ResourcePermission f = this.entry.getResourcePermission();
         assertNotNull(f);
         assertTrue(f.getUserClass().isQueuable());
         assertFalse(f.getUserClass().isKickable());
         assertEquals(f.getId(), p2.getId());
         assertEquals(f.getUserClass().getId(), uc2.getId());
     }
     
     @Test
     public void testHasPermissionResRigName()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         UserClass uc2 = new UserClass();
         uc2.setName("uc2");
         uc2.setActive(true);
         this.session.persist(uc2);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc2.getId()), uc2, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc2);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(uc2);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", 0, r.getName());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(uc2);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
     }
     
     @Test
     public void testHasPermissionResRigNotActive()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(false);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionResRigExpired()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(after);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("RIG", r.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionResType()
     {
         Date before = new Date(System.currentTimeMillis() - 1000000);
         Date after = new Date(System.currentTimeMillis() + 1000000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("TYPE");
         p2.setUserClass(uc1);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRigType(rt);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
     }
     
     @Test
     public void testHasPermissionResNoType()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("TYPE");
         p2.setUserClass(uc1);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRigType(rt);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", 0, "wrong_type");
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionResWrongType()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigType rt2 = new RigType();
         rt2.setName("Perm_Test_Rig_Type2");
         this.session.persist(rt2);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("TYPE");
         p2.setUserClass(uc1);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRigType(rt);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", 0, rt2.getName());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(rt2);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionResTypeName()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("TYPE");
         p2.setUserClass(uc1);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRigType(rt);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", 0, rt.getName());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
     }
     
     @Test
     public void testHasPermissionResTypeNotActive()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(false);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("TYPE");
         p2.setUserClass(uc1);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         p2.setRigType(rt);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionResTypeExpired()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Perm_Test_Rig_Type");
         this.session.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         Rig r = new Rig();
         r.setName("Perm_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         this.session.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("TYPE");
         p2.setUserClass(uc1);
         p2.setStartTime(after);
         p2.setExpiryTime(after);
         p2.setRigType(rt);
         this.session.persist(p2);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(r);
         this.session.refresh(rt);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("TYPE", rt.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(r);
         this.session.delete(rt);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionResCaps()
     {
         Date before = new Date(System.currentTimeMillis() - 1000000);
         Date after = new Date(System.currentTimeMillis() + 1000000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
 
         
         RequestCapabilities caps = new RequestCapabilities("perm,test,rig,type");
         this.session.persist(caps);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         this.session.persist(p1);
         ResourcePermission p2 = new ResourcePermission();
         p2.setType("TYPE");
         p2.setUserClass(uc1);
         p2.setStartTime(before);
         p2.setExpiryTime(after);
         this.session.persist(p2);
         ResourcePermission p3 = new ResourcePermission();
         p3.setType("CAPABILITY");
         p3.setUserClass(uc1);
         p3.setStartTime(before);
         p3.setExpiryTime(after);
         p3.setRequestCapabilities(caps);
         this.session.persist(p3);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         this.session.refresh(p2);
         this.session.refresh(caps);
 
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission("CAPABILITY", caps.getId(), null);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(p2);
         this.session.delete(p3);
         this.session.delete(caps);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
     }
 
     @Test
     public void testHasPermission()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission(p1.getId());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
     }
     
     @Test
     public void testHasPermissionMultiple()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
 
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         UserClass uc2 = new UserClass();
         uc2.setName("uc2");
         uc2.setActive(true);
         this.session.persist(uc2);
         UserClass uc3 = new UserClass();
         uc3.setName("uc3");
         uc3.setActive(true);
         this.session.persist(uc3);
         UserClass uc4 = new UserClass();
         uc4.setName("uc4");
         uc4.setActive(true);
         this.session.persist(uc4);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc4.getId()), uc4, user);
         this.session.persist(ass);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc4);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(uc2);
         this.session.refresh(uc3);
         this.session.refresh(uc4);
         this.session.refresh(user);
         this.session.refresh(p1);
         
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission(p1.getId());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(uc2);
         this.session.delete(uc3);
         this.session.delete(uc4);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertTrue(res);
     }
     
     @Test
     public void testHasPermissionUCNotActive()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(false);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission(p1.getId());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionUCExpired()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         this.session.persist(ass);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(before);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission(p1.getId());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionUserNotAMember()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(user);
         this.session.refresh(p1);
         
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission(p1.getId());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(uc1);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     @Test
     public void testHasPermissionMultipleNotAMember()
     {
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         this.session.beginTransaction();
         User user = new User("qperm1", "testns", "USER");
         this.session.persist(user);
 
         UserClass uc1 = new UserClass();
         uc1.setName("uc1");
         uc1.setActive(true);
         this.session.persist(uc1);
         UserClass uc2 = new UserClass();
         uc2.setName("uc2");
         uc2.setActive(true);
         this.session.persist(uc2);
         UserClass uc3 = new UserClass();
         uc3.setName("uc3");
         uc3.setActive(true);
         this.session.persist(uc3);
         UserClass uc4 = new UserClass();
         uc4.setName("uc4");
         uc4.setActive(true);
         this.session.persist(uc4);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc3.getId()), uc3, user);
         this.session.persist(ass);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc4);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         this.session.refresh(uc1);
         this.session.refresh(uc2);
         this.session.refresh(uc3);
         this.session.refresh(uc4);
         this.session.refresh(user);
         this.session.refresh(p1);
         
         this.entry.isInQueue(user);
         boolean res = this.entry.hasPermission(p1.getId());
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(ass);
         this.session.delete(uc1);
         this.session.delete(uc2);
         this.session.delete(uc3);
         this.session.delete(uc4);
         this.session.delete(user);
         this.session.getTransaction().commit();
         
         assertFalse(res);
     }
     
     public void testIsResourcePermissionActive() throws Exception
     {
         this.session.beginTransaction();
         UserClass uc = new UserClass();
         uc.setName("name_1");
         this.session.persist(uc);
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc);
         p1.setStartTime(new Date(System.currentTimeMillis() - 10000));
         p1.setExpiryTime(new Date(System.currentTimeMillis() + 10000));
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         Method m = QueueEntry.class.getDeclaredMethod("isResourcePermissionActive", ResourcePermission.class);
         m.setAccessible(true);
         boolean valid = (Boolean) m.invoke(this.entry, p1);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(uc);
         this.session.getTransaction().commit();
         
         assertTrue(valid);
     }
     
     public void testIsResourcePermissionActiveBeforeStart() throws Exception
     {
         this.session.beginTransaction();
         UserClass uc = new UserClass();
         uc.setName("name_1");
         this.session.persist(uc);
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc);
         p1.setStartTime(new Date(System.currentTimeMillis() + 10000));
         p1.setExpiryTime(new Date(System.currentTimeMillis() + 10000));
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         Method m = QueueEntry.class.getDeclaredMethod("isResourcePermissionActive", ResourcePermission.class);
         m.setAccessible(true);
         boolean valid = (Boolean) m.invoke(this.entry, p1);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(uc);
         this.session.getTransaction().commit();
         
         assertFalse(valid);
     }
     
     public void testIsResourcePermissionActiveAfterExpiry() throws Exception
     {
         this.session.beginTransaction();
         UserClass uc = new UserClass();
         uc.setName("name_1");
         this.session.persist(uc);
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("Rig");
         p1.setUserClass(uc);
         p1.setStartTime(new Date(System.currentTimeMillis() - 10000));
         p1.setExpiryTime(new Date(System.currentTimeMillis() - 10000));
         this.session.persist(p1);
         this.session.getTransaction().commit();
         
         Method m = QueueEntry.class.getDeclaredMethod("isResourcePermissionActive", ResourcePermission.class);
         m.setAccessible(true);
         boolean valid = (Boolean) m.invoke(this.entry, p1);
         
         this.session.beginTransaction();
         this.session.delete(p1);
         this.session.delete(uc);
         this.session.getTransaction().commit();
         
         assertFalse(valid);
     }
 }
