 /*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
 package org.picketlink.idm.impl.store.ldap.api;
 
 import org.picketlink.idm.api.IdentitySession;
 import org.picketlink.idm.api.IdentitySessionFactory;
 import org.picketlink.idm.api.User;
 import org.picketlink.idm.impl.api.APITestContext;
 import org.picketlink.idm.impl.api.GroupQueryTest;
 import org.picketlink.idm.impl.api.PersistenceManagerTest;
 import org.picketlink.idm.impl.api.RelationshipManagerTest;
 import org.picketlink.idm.impl.api.RoleManagerTest;
 import org.picketlink.idm.impl.api.RoleQueryTest;
 import org.picketlink.idm.impl.api.UserQueryTest;
 import org.picketlink.idm.impl.api.model.SimpleUser;
 import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
 import org.picketlink.idm.test.support.IdentityTestPOJO;
 import org.picketlink.idm.test.support.hibernate.HibernateTestPOJO;
 import org.picketlink.idm.test.support.ldap.LDAPTestPOJO;
 
 
 /**
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
 public class TwoLDAPTestCase
    extends IdentityTestPOJO
    implements APITestContext
 {
 
    IdentitySessionFactory identitySessionFactory;
 
    HibernateTestPOJO hibernateTest = new HibernateTestPOJO();
 
    LDAPTestPOJO ldapTestPOJO = new LDAPTestPOJO();
 
    public void setUp() throws Exception
    {
       super.start();
 
       hibernateTest.start();
       ldapTestPOJO.start();
 
       setRealmName("DB_2LDAP_REALM");
 
       ldapTestPOJO.populateLDIF("ldap/initial-opends.ldif");
 
       identityConfig = "two-ldap-config.xml";
 
       identitySessionFactory = new IdentityConfigurationImpl().
          configure(getIdentityConfig()).buildIdentitySessionFactory();
    }
 
    public void tearDown() throws Exception
    {
       super.stop();
       ldapTestPOJO.stop();
       hibernateTest.stop();
    }
 
    public IdentitySessionFactory getIdentitySessionFactory()
    {
       return identitySessionFactory;
    }
 
    public void testWhatIsAccesible() throws Exception
    {
       IdentitySession session = identitySessionFactory.createIdentitySession(getRealmName());
 
       begin();
 
       // Check if users are retrieved from both LDAPs
       assertEquals("User count", 14, session.getPersistenceManager().getUserCount());
 
       assertNotNull(session.getPersistenceManager().findUser("jduke"));
       assertNotNull(session.getPersistenceManager().findUser("jduke1"));
       assertNotNull(session.getPersistenceManager().findUser("jduke2"));
       assertNotNull(session.getPersistenceManager().findUser("jduke3"));
       assertNotNull(session.getPersistenceManager().findUser("jduke4"));
       assertNotNull(session.getPersistenceManager().findUser("jduke5"));
       assertNotNull(session.getPersistenceManager().findUser("jduke6"));
       assertNotNull(session.getPersistenceManager().findUser("jduke7"));
       assertNotNull(session.getPersistenceManager().findUser("jduke8"));
       assertNotNull(session.getPersistenceManager().findUser("jduke9"));
       assertNotNull(session.getPersistenceManager().findUser("admin"));
       assertNotNull(session.getPersistenceManager().findUser("admin2"));
       assertNotNull(session.getPersistenceManager().findUser("user"));
       assertNotNull(session.getPersistenceManager().findUser("user2"));
 
       // just insane check
       assertNull(session.getPersistenceManager().findUser("user112"));
 
 
       // check if all group types can be accessed
 
       assertNotNull(session.getPersistenceManager().findGroup("employee","internal_role"));
       assertNotNull(session.getPersistenceManager().findGroup("echo","internal_role"));
       assertNotNull(session.getPersistenceManager().findGroup("foo","internal_ou"));
       assertNotNull(session.getPersistenceManager().findGroup("bar","internal_ou"));
 
       assertNotNull(session.getPersistenceManager().findGroup("customer","customers_role"));
       assertNotNull(session.getPersistenceManager().findGroup("partner","customers_role"));
       assertNotNull(session.getPersistenceManager().findGroup("customer_foo","customers_ou"));
       assertNotNull(session.getPersistenceManager().findGroup("customer_bar","customers_ou"));
 
       commit();
    }
 
    public void begin()
    {
       hibernateTest.begin();
    }
 
    public void commit()
    {
       hibernateTest.commit();
    }
 
    @Override
    public void overrideFromProperties() throws Exception
    {
       //To change body of implemented methods use File | Settings | File Templates.
    }
 }
