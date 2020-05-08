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
 package org.picketlink.idm.ldap.cases;
 
 import org.picketlink.idm.impl.store.IdentityTypeEnum;
 import org.picketlink.idm.impl.store.RelationshipTypeEnum;
 import org.picketlink.idm.ldap.*;
 import java.util.ArrayList;
 import org.picketlink.idm.impl.api.PasswordCredential;
 import org.jboss.unit.api.pojo.annotations.Test;
 
 import java.util.Hashtable;
 
 import java.util.List;
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.naming.ldap.InitialLdapContext;
 import javax.naming.ldap.LdapContext;
 
 import org.picketlink.idm.impl.api.IdentitySearchCriteriaImpl;
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.model.IdentityObjectCredential;
 import org.picketlink.idm.spi.search.IdentityObjectSearchCriteria;
 
 
 import org.picketlink.idm.util.PropertiesSingelton;
 
 /**
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
 public class LDAPIdentityStoreTestCase extends LDAPIdentityStoreTestBase
    //implements IdentityStoreTestContext
 {
 
     int user_size = Integer.valueOf(PropertiesSingelton.getInstance().getProperties().getProperty("single.ldap.job.number"));
 
 //    @Test
 //    public void testSimple() throws Exception {
 //        populateClean();
 //
 ////        Hashtable<String, String> envLoc = new Hashtable<String, String>();
 ////        envLoc.put(Context.INITIAL_CONTEXT_FACTORY, directoryConfig.getContextFactory());
 ////        envLoc.put(Context.PROVIDER_URL, directoryConfig.getDescription());
 ////        envLoc.put(Context.SECURITY_AUTHENTICATION, "simple");
 ////        envLoc.put(Context.SECURITY_PRINCIPAL, directoryConfig.getAdminDN());
 ////        envLoc.put(Context.SECURITY_CREDENTIALS, directoryConfig.getAdminPassword());
 //
 //       Hashtable<String, String> envLoc = super.getLdapContext()
 //
 //
 //        LdapContext ldapCtx = null;
 //        try {
 //            ldapCtx = new InitialLdapContext(envLoc, null);
 //            System.out.println("Attributes: " + ldapCtx.getAttributes(directoryConfig.getCleanUpDN()));
 //        } catch (NamingException e) {
 //            e.printStackTrace();
 //        } finally {
 //            try {
 //                if (ldapCtx != null) {
 //                    ldapCtx.close();
 //                }
 //            } catch (NamingException e) {
 //                e.printStackTrace();
 //            }
 //        }
 //    }
 
     @Test
     public void testRelationships() throws Exception {
         populateClean();
         begin();
         resetMeasure();
 
         List<IdentityObject> userList = new ArrayList<IdentityObject>();
 
         for (int i = 0; i < user_size; i++) {
             IdentityObject user = getStore().createIdentityObject(getCtx(), "Evar" + i, IdentityTypeEnum.USER);
             userList.add(user);
         }
         IdentityObject group = getStore().createIdentityObject(getCtx(), "Devisrion1", IdentityTypeEnum.ORGANIZATION);
 
         flush();
         int c = 0;
         for (IdentityObject io : userList) {
             startStopwatch();
             getStore().createRelationship(getCtx(), group, io, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, null, false);
             stopStopwatch();
             storeStopwatch(c++);
         }
         commit();
 
         generateGraph(getGraphNamePfx() + ".testRelationships()", this.getClass().getName());
     }
 
     @Test
     public void testCreateUser() throws Exception {
 
         populateClean();
         begin();
         resetMeasure();
 
         for (int i = 0; i < user_size; i++) {
             startStopwatch();
             IdentityObject user1 = getStore().createIdentityObject(getCtx(), "Adram" + i, IdentityTypeEnum.USER);
             stopStopwatch();
             storeStopwatch(i);
         }
         commit();
         generateGraph(getGraphNamePfx() + ".testCreateUser()", this.getClass().getName());
     }
 
     @Test
     public void testCreateGroup() throws Exception {
 
         populateClean();
         begin();
         resetMeasure();
 
         for (int i = 0; i < user_size; i++) {
             startStopwatch();
             IdentityObject group = getStore().createIdentityObject(getCtx(), "Devisrion" + i, IdentityTypeEnum.ORGANIZATION);
             stopStopwatch();
             storeStopwatch(i);
         }
         commit();
         generateGraph(getGraphNamePfx() + ".testCreateGroup()", this.getClass().getName());
     }
 
     @Test
     public void testRemoveGroup() throws Exception {
 
         String grpPfx = "testRemoveGroup";
 
         populateClean();
         begin();
         resetMeasure();
 
         List<IdentityObject> groupList = new ArrayList<IdentityObject>();
 
         for (int i = 0; i < user_size; i++) {
             IdentityObject group = getStore().createIdentityObject(getCtx(), grpPfx + "Devisrion" + i, IdentityTypeEnum.ORGANIZATION);
             groupList.add(group);
         }
 
         int c = 0;
         for (IdentityObject group : groupList) {
             startStopwatch();
             getStore().removeIdentityObject(ctx, group);
             stopStopwatch();
             storeStopwatch(c++);
         }
 
         commit();
         generateGraph(getGraphNamePfx() + ".testRemoveGroup()", this.getClass().getName());
     }
 
     @Test
     public void testRemoveUser() throws Exception {
 
         String usrPfx = "testRemoveUser";
 
         populateClean();
         begin();
         resetMeasure();
 
         List<IdentityObject> groupList = new ArrayList<IdentityObject>();
 
         for (int i = 0; i < user_size; i++) {
             IdentityObject user = getStore().createIdentityObject(getCtx(), usrPfx + "Adram" + i, IdentityTypeEnum.USER);
             groupList.add(user);
         }
 
         int c = 0;
         for (IdentityObject group : groupList) {
             startStopwatch();
             getStore().removeIdentityObject(ctx, group);
             stopStopwatch();
             storeStopwatch(c++);
         }
 
         commit();
         generateGraph(getGraphNamePfx() + ".testRemoveUser()", this.getClass().getName());
     }
 
     /*
     @Test
     public void testStorePersistence() throws Exception {
     populateClean();
     begin();
     resetMeasure();
 
     commonTest.testStorePersistence();
 
     generateGraph(getGraphNamePfx() + ".testStorePersistence()");
 
     }
      */
     @Test
     public void testFindIdentityObject() throws Exception {
         populateClean();
         begin();
         resetMeasure();
 
         flush();
         for (int i = 0; i < user_size; i++) {
             getStore().createIdentityObject(getCtx(), "Eveax" + i, IdentityTypeEnum.USER);
             startStopwatch();
             getStore().findIdentityObject(getCtx(), "Eveax" + i, IdentityTypeEnum.USER);
             stopStopwatch();
             storeStopwatch(i);
         }
 
         commit();
         flush();
         generateGraph(getGraphNamePfx() + ".testFindIdentityObject()", this.getClass().getName());
     }
 
     @Test
     public void testCriteria() throws Exception {
         populateClean();
         begin();
         resetMeasure();
 
         for (int i = 0; i < user_size; i++) {
             IdentityObject group = getStore().createIdentityObject(getCtx(), "Drxvision" + i, IdentityTypeEnum.ORGANIZATION);
             IdentityObjectSearchCriteria criteria = (IdentityObjectSearchCriteria) new IdentitySearchCriteriaImpl().nameFilter("D*");
             startStopwatch();
             getStore().findIdentityObject(getCtx(), IdentityTypeEnum.USER, criteria);
             stopStopwatch();
             storeStopwatch(i);
         }
         commit();
         flush();
         generateGraph(getGraphNamePfx() + ".testCriteria()", this.getClass().getName());
     }
 
     @Test
     public void testCredentials() throws Exception {
         populateClean();
         begin();
         resetMeasure();
 
         for (int i = 0; i < user_size; i++) {
             IdentityObject user1 = getStore().createIdentityObject(getCtx(), "AsdacmptestCredentials" + i, IdentityTypeEnum.USER);
            IdentityObjectCredential passwordCredential1 = new PasswordCredential("Psacssword2000" + i);
 //            commit();
 //            flush();
             startStopwatch();
             getStore().updateCredential(getCtx(), user1, passwordCredential1);
             stopStopwatch();
             storeStopwatch(i);
         }
 
         commit();
         flush();
         generateGraph(getGraphNamePfx() + ".testCredentials()", this.getClass().getName());
     }
 
 }
