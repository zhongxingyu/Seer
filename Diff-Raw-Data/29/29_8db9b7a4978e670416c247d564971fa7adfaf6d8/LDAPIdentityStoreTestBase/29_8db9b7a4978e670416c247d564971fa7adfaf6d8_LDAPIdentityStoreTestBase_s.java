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
 package org.picketlink.idm.ldap;
 
 import org.opends.server.tools.LDAPModify;
 import org.picketlink.idm.ldap.*;
 import org.picketlink.idm.impl.store.ldap.*;
 import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
 import org.picketlink.idm.impl.configuration.IdentityStoreConfigurationContextImpl;
 import org.picketlink.idm.impl.configuration.jaxb2.JAXB2IdentityConfiguration;
 import org.picketlink.idm.performance.DBTestBase;
 import org.picketlink.idm.spi.configuration.IdentityConfigurationContextRegistry;
 import org.picketlink.idm.spi.configuration.IdentityStoreConfigurationContext;
 import org.picketlink.idm.spi.configuration.metadata.IdentityConfigurationMetaData;
 import org.picketlink.idm.spi.configuration.metadata.IdentityStoreConfigurationMetaData;
 import org.picketlink.idm.spi.store.IdentityStore;
 import org.picketlink.idm.spi.store.IdentityStoreInvocationContext;
 import org.picketlink.idm.spi.store.IdentityStoreSession;
 import org.picketlink.idm.test.support.hibernate.HibernateTestPOJO;
 import org.picketlink.idm.test.support.ldap.LDAPTestPOJO;
 
 import org.jboss.unit.api.pojo.annotations.Create;
 import org.jboss.unit.api.pojo.annotations.Destroy;
 
 import java.net.URL;
 
 /**
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
 public class LDAPIdentityStoreTestBase extends DBTestBase
 {
 
     //protected CommonIdentityLDAPStoreTest commonTest;
     protected IdentityStoreInvocationContext ctx;
     protected IdentityStore store;
     //protected int user_size = 1000;
 
     public LDAPTestPOJO ldapTestPOJO = new LDAPTestPOJO();
 
     public LDAPIdentityStoreTestBase() {
     }
 
     @Create
     public void setUp() throws Exception {
         super.setUp();
 
         ldapTestPOJO.start();
 
        // TODO: make ldap instance configurable
         IdentityConfigurationMetaData configurationMD = JAXB2IdentityConfiguration.createConfigurationMetaData("test-identity-config.xml");
 
         IdentityConfigurationContextRegistry registry = (IdentityConfigurationContextRegistry) new IdentityConfigurationImpl().configure(configurationMD);
 
         IdentityStoreConfigurationMetaData storeMD = null;
 
         for (IdentityStoreConfigurationMetaData metaData : configurationMD.getIdentityStores()) {
             if (metaData.getId().equals("LDAP Identity Store")) {
                 storeMD = metaData;
                 break;
             }
         }
 
         IdentityStoreConfigurationContext context = new IdentityStoreConfigurationContextImpl(configurationMD, registry, storeMD);
 
         store = new LDAPIdentityStoreImpl("LDAP Identity Store");
 
         store.bootstrap(context);
 
         final IdentityStoreSession storeSession = store.createIdentityStoreSession();
 
         ctx = new IdentityStoreInvocationContext() {
 
 
             public IdentityStoreSession getIdentityStoreSession() {
                 return storeSession;
             }
 
             public String getSessionId() {
                 return "id";
             }
 
             public String getRealmId() {
                 return "testRealm";
             }
         };
     }
 
     @Destroy
     public void tearDown() throws Exception {
         //System.out.println("t1");
         super.tearDown();
        ldapTestPOJO.stop();
     }
 
 
     public void flush() throws Exception {
         //nothing
     }
 
 
     public IdentityStore getStore() {
         return store;
     }
 
 
     public IdentityStoreInvocationContext getCtx() {
         return ctx;
     }
 
     public void populateClean() throws Exception
     {
       ldapTestPOJO.populateClean();
     }
 
     public void begin()
     {
 
     }
 
    public void commit()
    {
 
    }
 
 
 }
