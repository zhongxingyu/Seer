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
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.picketlink.idm.impl.store.IdentityStoreTestContext;
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
 
 
 /**
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
 public class LDAPIdentityStoreTests extends LDAPIdentityStoreTestBase implements IdentityStoreTestContext
 {
 /*
     public LDAPIdentityStoreTests(){
         try {
             setUp();
         } catch (Exception ex) {
             Logger.getLogger(LDAPIdentityStoreTests.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 */
 //    @Test
 //    public void testSimple() throws Exception {
 //        populateClean();
 //
 //        Hashtable<String, String> envLoc = new Hashtable<String, String>();
 //        envLoc.put(Context.INITIAL_CONTEXT_FACTORY, directoryConfig.getContextFactory());
 //        envLoc.put(Context.PROVIDER_URL, directoryConfig.getDescription());
 //        envLoc.put(Context.SECURITY_AUTHENTICATION, "simple");
 //        envLoc.put(Context.SECURITY_PRINCIPAL, directoryConfig.getAdminDN());
 //        envLoc.put(Context.SECURITY_CREDENTIALS, directoryConfig.getAdminPassword());
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
     public void testRelationships(int job_number,int worker_id) throws Exception {
         //System.out.println("WORKER: "+worker_id);
 
         resetMeasure();
 
         List<IdentityObject> userList = new ArrayList<IdentityObject>();
 
         for (int i = 0; i < job_number; i++) {
             IdentityObject user = getStore().createIdentityObject(getCtx(), worker_id+".Evar" + i, IdentityTypeEnum.USER);
             userList.add(user);
         }
         IdentityObject group = getStore().createIdentityObject(getCtx(), worker_id+".Devisrion1", IdentityTypeEnum.ORGANIZATION);
 
         flush();
         int c = 0;
         for (IdentityObject io : userList) {
             startStopwatch();
             getStore().createRelationship(getCtx(), group, io, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, null, false);
             stopStopwatch();
             storeStopwatch(c++);
         }
         commit();
         generateGraph(getGraphNamePfx() + worker_id, this.getClass().getName() + ".testRelationships()");
 
     }
 
     @Test
     public void testCreateUser(int job_number,int worker_id) throws Exception {
 
         resetMeasure();
 
         for (int i = 0; i < job_number; i++) {
             startStopwatch();
             IdentityObject user1 = getStore().createIdentityObject(getCtx(), worker_id+"Adram" + i, IdentityTypeEnum.USER);
             stopStopwatch();
             storeStopwatch(i);
         }
         commit();
         generateGraph(getGraphNamePfx()  + worker_id, this.getClass().getName()+ ".testCreateUser()");
     }
 
     @Test
     public void testCreateGroup(int job_number,int worker_id) throws Exception {
 
         resetMeasure();
 
         for (int i = 0; i < job_number; i++) {
             startStopwatch();
             IdentityObject group = getStore().createIdentityObject(getCtx(), worker_id+"Devisrion" + i, IdentityTypeEnum.ORGANIZATION);
             stopStopwatch();
             storeStopwatch(i);
         }
         commit();
         generateGraph(getGraphNamePfx() + worker_id , this.getClass().getName()+ ".testCreateGroup()");
     }
 
     @Test
     public void testRemoveGroup(int job_number,int worker_id) throws Exception {
 
         String grpPfx = worker_id+"testRemoveGroup";
 
         resetMeasure();
 
         List<IdentityObject> groupList = new ArrayList<IdentityObject>();
 
         for (int i = 0; i < job_number; i++) {
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
         generateGraph(getGraphNamePfx()  + worker_id, this.getClass().getName()+ ".testRemoveGroup()");
     }
 
     @Test
     public void testRemoveUser(int job_number,int worker_id) throws Exception {
 
         String usrPfx = worker_id+"testRemoveUser";
 
 
         resetMeasure();
 
         List<IdentityObject> groupList = new ArrayList<IdentityObject>();
 
         for (int i = 0; i < job_number; i++) {
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
         generateGraph(getGraphNamePfx() + worker_id , this.getClass().getName()+ ".testRemoveUser()");
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
     public void testFindIdentityObject(int job_number,int worker_id) throws Exception {
 
         resetMeasure();
 
         flush();
         for (int i = 0; i < job_number; i++) {
             getStore().createIdentityObject(getCtx(), worker_id+"Eveax" + i, IdentityTypeEnum.USER);
             startStopwatch();
             getStore().findIdentityObject(getCtx(), worker_id+"Eveax" + i, IdentityTypeEnum.USER);
             stopStopwatch();
             storeStopwatch(i);
         }
 
         commit();
         flush();
         generateGraph(getGraphNamePfx() + worker_id , this.getClass().getName()+ ".testFindIdentityObject()");
     }
 
     @Test
     public void testCriteria(int job_number,int worker_id) throws Exception {
 
         resetMeasure();
 
         for (int i = 0; i < job_number; i++) {
             IdentityObject group = getStore().createIdentityObject(getCtx(),worker_id+ "Drxvision" + i, IdentityTypeEnum.ORGANIZATION);
             IdentityObjectSearchCriteria criteria = (IdentityObjectSearchCriteria) new IdentitySearchCriteriaImpl().nameFilter("D*");
             startStopwatch();
             getStore().findIdentityObject(getCtx(), IdentityTypeEnum.USER, criteria);
             stopStopwatch();
             storeStopwatch(i);
         }
         commit();
         flush();
         generateGraph(getGraphNamePfx() + worker_id, this.getClass().getName() + ".testCriteria()");
     }
 
     @Test
     public void testCredentials(int job_number,int worker_id) throws Exception {
 
         resetMeasure();
 
         for (int i = 0; i < job_number; i++) {
             //System.out.println(i+"s");
             IdentityObject user1 = getStore().createIdentityObject(getCtx(), worker_id+"AsdacmptestCredentials" + i, IdentityTypeEnum.USER);
            IdentityObjectCredential passwordCredential1 = new PasswordCredential("Psacssword2000" + i);
 //            commit();
 //            flush();
             startStopwatch();
             getStore().updateCredential(getCtx(), user1, passwordCredential1);
             stopStopwatch();
             storeStopwatch(i);
             //System.out.println(i+"e");
         }
 //System.out.println("pc");
         commit();
        // System.out.println("pf");
         flush();
         //System.out.println("pg");
         generateGraph(getGraphNamePfx() + worker_id , this.getClass().getName()+ ".testCredentials()");
     }
 
 }
