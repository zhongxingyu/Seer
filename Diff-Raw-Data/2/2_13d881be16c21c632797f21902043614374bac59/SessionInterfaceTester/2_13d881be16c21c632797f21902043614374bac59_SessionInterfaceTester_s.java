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
 package au.edu.uts.eng.remotelabs.schedserver.session.intf.tests;
 
 import java.lang.reflect.Field;
 import java.util.Date;
 
 import junit.framework.TestCase;
 
 import org.apache.axiom.om.OMAbstractFactory;
 import org.apache.axiom.om.OMElement;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociation;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociationId;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserClass;
import au.edu.uts.eng.remotelabs.schedserver.dataaccess.tests.DataAccessTestSetup;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.SessionInterface;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.FinishSession;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.FinishSessionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.GetSessionInformation;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.GetSessionInformationResponse;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.InSessionType;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.ResourceIDType;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.SessionType;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.UserIDType;
 
 /**
  * Tests the {@link Session} class.
  */
 public class SessionInterfaceTester extends TestCase
 {
     /** Object of class under test. */
     private SessionInterface sessionIntf;
 
     @Override
     @Before
     public void setUp() throws Exception
     {
         DataAccessTestSetup.setup();
         this.sessionIntf = new SessionInterface();
         
         Field f = SessionInterface.class.getDeclaredField("notTest");
         f.setAccessible(true);
         f.set(this.sessionIntf, Boolean.FALSE);
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.schedserver.session.intf.SessionInterface#finishSession(au.edu.uts.eng.remotelabs.schedserver.session.intf.types.FinishSession)}.
      */
     @Test
     public void testFinishSession() throws Exception
     {
         org.hibernate.Session db = DataAccessActivator.getNewSession();
         
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         Date now = new Date();
         
         db.beginTransaction();
         
         User user = new User("sessiontest", "testns", "USER");
         db.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("sessclass");
         uc1.setActive(true);
         uc1.setQueuable(true);
         uc1.setPriority((short)4);
         db.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         db.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Session_Test_Rig_Type");
         db.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("session,test,rig,type");
         db.persist(caps);
         
         Rig r = new Rig();
         r.setName("Session_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         db.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         p1.setAllowedExtensions((short)10);
         db.persist(p1);
         
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
         ses.setAssignedRigName(r.getName());
         ses.setAssignmentTime(now);
         ses.setRig(r);
         db.persist(ses);
         db.getTransaction().commit();
         
         db.beginTransaction();
         db.refresh(uc1);
         db.refresh(user);
         db.refresh(p1);
         db.refresh(r);
         db.refresh(rt);
         db.getTransaction().commit();
                 
         FinishSession request = new FinishSession();
         UserIDType uid = new UserIDType();
         request.setFinishSession(uid);
         uid.setUserQName(user.getNamespace() + ':' + user.getName());
         
         FinishSessionResponse resp = this.sessionIntf.finishSession(request);
         
         db.refresh(ses);
         assertFalse(ses.isActive());
         assertNotNull(ses.getRemovalReason());
         assertNotNull(ses.getRemovalTime());
         
         db.beginTransaction();
         db.delete(ses);
         db.delete(p1);
         db.delete(r);
         db.delete(rt);
         db.delete(caps);
         db.delete(ass);
         db.delete(uc1);
         db.delete(user);
         db.getTransaction().commit();
         
         assertNotNull(resp);
         InSessionType in = resp.getFinishSessionResponse();
         assertNotNull(in);
         assertFalse(in.getIsInSession());
         
         OMElement ele = resp.getOMElement(FinishSessionResponse.MY_QNAME, OMAbstractFactory.getOMFactory());
         assertNotNull(ele);
         
         String xml = ele.toStringWithConsume();
         assertNotNull(xml);
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.schedserver.session.intf.SessionInterface#getSessionInformation(au.edu.uts.eng.remotelabs.schedserver.session.intf.types.GetSessionInformation)}.
      */
     @Test
     public void testGetSessionInformation() throws Exception
     {
         org.hibernate.Session db = DataAccessActivator.getNewSession();
         
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         Date now = new Date(System.currentTimeMillis() - 1000);
         
         db.beginTransaction();
         
         User user = new User("sessiontest", "testns", "USER");
         db.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("sessclass");
         uc1.setActive(true);
         uc1.setQueuable(true);
         uc1.setPriority((short)4);
         db.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         db.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Session_Test_Rig_Type");
         db.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("session,test,rig,type");
         db.persist(caps);
         
         Rig r = new Rig();
         r.setName("Session_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         db.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         p1.setAllowedExtensions((short)10);
         p1.setExtensionDuration(300);
         p1.setSessionDuration(3600);
         db.persist(p1);
         
         Session ses = new Session();
         ses.setActive(true);
         ses.setReady(true);
         ses.setActivityLastUpdated(now);
         ses.setExtensions((short) 10);
         ses.setPriority((short) 5);
         ses.setRequestTime(now);
         ses.setRequestedResourceId(r.getId());
         ses.setRequestedResourceName(r.getName());
         ses.setResourceType("RIG");
         ses.setResourcePermission(p1);
         ses.setUser(user);
         ses.setUserName(user.getName());
         ses.setUserNamespace(user.getNamespace());
         ses.setAssignedRigName(r.getName());
         ses.setAssignmentTime(before);
         ses.setRig(r);
         db.persist(ses);
         db.getTransaction().commit();
         
         db.beginTransaction();
         db.refresh(uc1);
         db.refresh(user);
         db.refresh(p1);
         db.refresh(r);
         db.refresh(rt);
         db.getTransaction().commit();
                 
         GetSessionInformation request = new GetSessionInformation();
         UserIDType uid = new UserIDType();
         request.setGetSessionInformation(uid);
         uid.setUserQName(user.getNamespace() + ':' + user.getName());
         
         GetSessionInformationResponse resp = this.sessionIntf.getSessionInformation(request);
         
         db.beginTransaction();
         db.delete(ses);
         db.delete(p1);
         db.delete(r);
         db.delete(rt);
         db.delete(caps);
         db.delete(ass);
         db.delete(uc1);
         db.delete(user);
         db.getTransaction().commit();
         
         assertNotNull(resp);
         SessionType s = resp.getGetSessionInformationResponse();
         assertNotNull(s);
         assertTrue(s.getIsInSession());
         assertTrue(s.getIsReady());
         assertFalse(s.getIsCodeAssigned());
         
         ResourceIDType res = s.getResource();
         assertNotNull(res);
         assertEquals("RIG", res.getType());
         assertEquals(r.getId().intValue(), res.getResourceID());
         assertEquals(r.getName(), res.getResourceName());
         assertEquals(r.getContactUrl(), s.getContactURL());
         
         assertEquals(ses.getExtensions(), s.getExtensions());
         
         int tm = 10;
         assertEquals(tm, s.getTime());
         
         tm = 3590;
         assertEquals(tm, s.getTimeLeft());
         
         assertNull(s.getWarningMessage());
         
         OMElement ele = resp.getOMElement(GetSessionInformation.MY_QNAME, OMAbstractFactory.getOMFactory());
         assertNotNull(ele);
         
         String xml = ele.toStringWithConsume();
         assertNotNull(xml);
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.schedserver.session.intf.SessionInterface#getSessionInformation(au.edu.uts.eng.remotelabs.schedserver.session.intf.types.GetSessionInformation)}.
      */
     @Test
     public void testGetSessionInformationExtension() throws Exception
     {
         org.hibernate.Session db = DataAccessActivator.getNewSession();
         
         Date before = new Date(System.currentTimeMillis() - 10000);
         Date after = new Date(System.currentTimeMillis() + 10000);
         Date now = new Date(System.currentTimeMillis() - 1000);
         
         db.beginTransaction();
         
         User user = new User("sessiontest", "testns", "USER");
         db.persist(user);
         
         UserClass uc1 = new UserClass();
         uc1.setName("sessclass");
         uc1.setActive(true);
         uc1.setQueuable(true);
         uc1.setPriority((short)4);
         db.persist(uc1);
         
         UserAssociation ass = new UserAssociation(new UserAssociationId(user.getId(), uc1.getId()), uc1, user);
         db.persist(ass);
         
         RigType rt = new RigType();
         rt.setName("Session_Test_Rig_Type");
         db.persist(rt);
         
         RigCapabilities caps = new RigCapabilities("session,test,rig,type");
         db.persist(caps);
         
         Rig r = new Rig();
         r.setName("Session_Rig_Test_Rig1");
         r.setRigType(rt);
         r.setRigCapabilities(caps);
         r.setLastUpdateTimestamp(before);
         r.setActive(true);
         r.setOnline(true);
         r.setInSession(true);
         db.persist(r);
         
         ResourcePermission p1 = new ResourcePermission();
         p1.setType("RIG");
         p1.setUserClass(uc1);
         p1.setStartTime(before);
         p1.setExpiryTime(after);
         p1.setRig(r);
         p1.setAllowedExtensions((short)10);
         p1.setExtensionDuration(300);
         p1.setSessionDuration(3600);
         db.persist(p1);
         
         Session ses = new Session();
         ses.setActive(true);
         ses.setReady(true);
         ses.setActivityLastUpdated(now);
         ses.setExtensions((short) 8);
         ses.setPriority((short) 5);
         ses.setRequestTime(now);
         ses.setRequestedResourceId(r.getId());
         ses.setRequestedResourceName(r.getName());
         ses.setResourceType("RIG");
         ses.setResourcePermission(p1);
         ses.setUser(user);
         ses.setUserName(user.getName());
         ses.setUserNamespace(user.getNamespace());
         ses.setAssignedRigName(r.getName());
         ses.setAssignmentTime(before);
         ses.setRig(r);
         db.persist(ses);
         db.getTransaction().commit();
         
         db.beginTransaction();
         db.refresh(uc1);
         db.refresh(user);
         db.refresh(p1);
         db.refresh(r);
         db.refresh(rt);
         db.getTransaction().commit();
                 
         GetSessionInformation request = new GetSessionInformation();
         UserIDType uid = new UserIDType();
         request.setGetSessionInformation(uid);
         uid.setUserQName(user.getNamespace() + ':' + user.getName());
         
         GetSessionInformationResponse resp = this.sessionIntf.getSessionInformation(request);
         
         db.beginTransaction();
         db.delete(ses);
         db.delete(p1);
         db.delete(r);
         db.delete(rt);
         db.delete(caps);
         db.delete(ass);
         db.delete(uc1);
         db.delete(user);
         db.getTransaction().commit();
         
         assertNotNull(resp);
         SessionType s = resp.getGetSessionInformationResponse();
         assertNotNull(s);
         assertTrue(s.getIsInSession());
         assertTrue(s.getIsReady());
         assertFalse(s.getIsCodeAssigned());
         
         ResourceIDType res = s.getResource();
         assertNotNull(res);
         assertEquals("RIG", res.getType());
         assertEquals(r.getId().intValue(), res.getResourceID());
         assertEquals(r.getName(), res.getResourceName());
         assertEquals(r.getContactUrl(), s.getContactURL());
         
         assertEquals(ses.getExtensions(), s.getExtensions());
         
         int tm = 10;
         assertEquals(tm, s.getTime());
         
         tm = 4190;
         assertEquals(tm, s.getTimeLeft());
         
         assertNull(s.getWarningMessage());
         
         OMElement ele = resp.getOMElement(GetSessionInformation.MY_QNAME, OMAbstractFactory.getOMFactory());
         assertNotNull(ele);
         
         String xml = ele.toStringWithConsume();
         assertNotNull(xml);
     }
 }
