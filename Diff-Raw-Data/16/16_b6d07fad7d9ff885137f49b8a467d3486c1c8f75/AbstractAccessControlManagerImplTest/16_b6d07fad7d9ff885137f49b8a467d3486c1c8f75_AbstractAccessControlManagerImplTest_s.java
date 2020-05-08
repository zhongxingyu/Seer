 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.accesscontrol;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.lite.ConfigurationImpl;
 import org.sakaiproject.nakamura.lite.LoggingStorageListener;
 import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
 import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public abstract class AbstractAccessControlManagerImplTest {
     private static final Logger LOGGER = LoggerFactory
             .getLogger(AbstractAccessControlManagerImplTest.class);
     private StorageClient client;
     private ConfigurationImpl configuration;
     private StorageClientPool clientPool;
 
     @Before
     public void before() throws StorageClientException, AccessDeniedException, ClientPoolException,
             ClassNotFoundException {
         clientPool = getClientPool();
         client = clientPool.getClient();
         configuration = new ConfigurationImpl();
         Map<String, Object> properties = Maps.newHashMap();
         properties.put("keyspace", "n");
         properties.put("acl-column-family", "ac");
         properties.put("authorizable-column-family", "au");
         configuration.activate(properties);
         AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                 configuration);
         authorizableActivator.setup();
         LOGGER.info("Setup Complete");
     }
 
     protected abstract StorageClientPool getClientPool() throws ClassNotFoundException;
 
     @After
     public void after() throws ClientPoolException {
         client.close();
     }
 
     @Test
     public void test() throws StorageClientException, AccessDeniedException {
         AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
         User currentUser = authenticator.authenticate("admin", "admin");
         String u1 = "user1-"+System.currentTimeMillis();
         String u2 = "user2-"+System.currentTimeMillis();
         String u3 = "user3-"+System.currentTimeMillis();
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, null, new LoggingStorageListener());
         AclModification user1 = new AclModification(u1, Permissions.CAN_ANYTHING.combine(
                 Permissions.CAN_ANYTHING_ACL).getPermission(), AclModification.Operation.OP_REPLACE);
         AclModification user2 = new AclModification(u2, Permissions.CAN_READ
                 .combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE).getPermission(),
                 AclModification.Operation.OP_REPLACE);
         AclModification user3 = new AclModification(u3, Permissions.CAN_READ.getPermission(),
                 AclModification.Operation.OP_REPLACE);
         String basepath = "testpath"+System.currentTimeMillis();
 
         accessControlManagerImpl.setAcl(Security.ZONE_AUTHORIZABLES, basepath,
                 new AclModification[] { user1, user2, user3 });
 
         Map<String, Object> acl = accessControlManagerImpl.getAcl(Security.ZONE_AUTHORIZABLES,
                 basepath);
         Assert.assertEquals(Integer.toHexString(Permissions.CAN_ANYTHING.combine(
                 Permissions.CAN_ANYTHING_ACL).getPermission()), Integer
                 .toHexString(StorageClientUtils.toInt(acl.get(u1))));
         Assert.assertEquals(
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE)
                         .getPermission(), StorageClientUtils.toInt(acl.get(u2)));
         Assert.assertEquals(Permissions.CAN_READ.getPermission(),
                 StorageClientUtils.toInt(acl.get(u3)));
         for (Entry<String, Object> e : acl.entrySet()) {
             LOGGER.info(" ACE {} : {} ", e.getKey(), StorageClientUtils.toInt(e.getValue()));
         }
         LOGGER.info("Got ACL {}", acl);
 
     }
 
     @Test
     public void testPrivileges() throws StorageClientException, AccessDeniedException {
         AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
         User currentUser = authenticator.authenticate("admin", "admin");
         String u1 = "user1-"+System.currentTimeMillis();
         String u2 = "user2-"+System.currentTimeMillis();
         String u3 = "user3-"+System.currentTimeMillis();
         String basepath = "testpath"+System.currentTimeMillis();
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, null,  new LoggingStorageListener());
         AclModification user1CanAnything = new AclModification(AclModification.grantKey(u1),
                 Permissions.CAN_ANYTHING.combine(Permissions.CAN_ANYTHING_ACL).getPermission(),
                 AclModification.Operation.OP_REPLACE);
         AclModification user2CantReadWrite = new AclModification(AclModification.denyKey(u2),
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE)
                         .getPermission(), AclModification.Operation.OP_REPLACE);
         AclModification user3cantRead = new AclModification(AclModification.denyKey(u3),
                 Permissions.CAN_READ.getPermission(), AclModification.Operation.OP_REPLACE);
 
         AclModification denyAnon = new AclModification(AclModification.denyKey(User.ANON_USER),
                 Permissions.ALL.getPermission(), AclModification.Operation.OP_REPLACE);
         AclModification denyEveryone = new AclModification(AclModification.denyKey(Group.EVERYONE),
                 Permissions.ALL.getPermission(), AclModification.Operation.OP_REPLACE);
 
         AclModification user2CanReadWrite = new AclModification(AclModification.grantKey(u2),
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE)
                         .getPermission(), AclModification.Operation.OP_REPLACE);
         AclModification user3canRead = new AclModification(AclModification.grantKey(u3),
                 Permissions.CAN_READ.getPermission(), AclModification.Operation.OP_REPLACE);
 
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath+"/a/b/c",
                 new AclModification[] { user1CanAnything, user2CantReadWrite, user3cantRead,
                         denyAnon, denyEveryone });
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath+"/a/b",
                 new AclModification[] { user1CanAnything, user2CanReadWrite });
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath+"/a", new AclModification[] {
                 user1CanAnything, user3canRead });
 
         Map<String, Object> acl = accessControlManagerImpl
                 .getAcl(Security.ZONE_CONTENT, basepath);
         Assert.assertArrayEquals(new String[] {}, acl.keySet().toArray());
 
         acl = accessControlManagerImpl.getAcl(Security.ZONE_CONTENT, basepath+"/a");
         Assert.assertArrayEquals(Arrays.toString(sortToArray(acl.keySet())),
                 new String[] { AclModification.grantKey(u1), AclModification.grantKey(u3) },
                 sortToArray(acl.keySet()));
         acl = accessControlManagerImpl.getAcl(Security.ZONE_CONTENT, basepath+"/a/b");
         Assert.assertArrayEquals(
                 new String[] { AclModification.grantKey(u1), AclModification.grantKey(u2) },
                 sortToArray(acl.keySet()));
         acl = accessControlManagerImpl.getAcl(Security.ZONE_CONTENT, basepath+"/a/b/c");
         Assert.assertArrayEquals(new String[] { AclModification.denyKey(User.ANON_USER),
                 AclModification.denyKey(Group.EVERYONE), AclModification.grantKey(u1),
                 AclModification.denyKey(u2), AclModification.denyKey(u3) },
                 sortToArray(acl.keySet()));
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser, client,
                 configuration, accessControlManagerImpl, null,  new LoggingStorageListener());
         authorizableManager.createUser(u1, "User 1", "test",
                 ImmutableMap.of("test", StorageClientUtils.toStore("test")));
         authorizableManager.createUser(u2, "User 2", "test",
                 ImmutableMap.of("test", StorageClientUtils.toStore("test")));
         authorizableManager.createUser(u3, "User 3", "test",
                 ImmutableMap.of("test", StorageClientUtils.toStore("test")));
 
         User user1 = (User) authorizableManager.findAuthorizable(u1);
         User user2 = (User) authorizableManager.findAuthorizable(u2);
         User user3 = (User) authorizableManager.findAuthorizable(u3);
         User adminUser = (User) authorizableManager.findAuthorizable(User.ADMIN_USER);
         User anonUser = (User) authorizableManager.findAuthorizable(User.ANON_USER);
         Group everyoneGroup = (Group) authorizableManager.findAuthorizable(Group.EVERYONE);
        
         
         
         Assert.assertNotNull(user1);
         Assert.assertNotNull(user2);
         Assert.assertNotNull(user3);
         Assert.assertNotNull(adminUser);
         Assert.assertNotNull(anonUser);
         Assert.assertNotNull(everyoneGroup);
         
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath, Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath, Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath, Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath, Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath, Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT, basepath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT, basepath, Permissions.ALL));
 
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath+"/a/b/c", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath+"/a/b/c", Permissions.ALL));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath+"/a/b/c", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath+"/a/b/c", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT, basepath+"/a/b/c", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath+"/a/b/c", Permissions.CAN_READ));
 
 
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.CAN_WRITE.combine(Permissions.CAN_READ).combine(Permissions.CAN_DELETE)));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT, basepath+"/a/b", Permissions.ALL));
 
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath+"/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath+"/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath+"/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath+"/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath+"/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath+"/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath+"/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath+"/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT, basepath+"/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT, basepath+"/a", Permissions.ALL));
 
 
         String[] testpaths = { basepath, basepath+"/a", basepath+"/a/b", basepath+"/a/b/c", };
 
         checkPermissions(user1, testpaths, new Permission[][] {
                 { Permissions.CAN_READ },
                 { Permissions.CAN_READ, Permissions.CAN_WRITE, Permissions.CAN_DELETE,
                         Permissions.CAN_READ_ACL, Permissions.CAN_WRITE_ACL,
                         Permissions.CAN_DELETE_ACL, Permissions.CAN_MANAGE, Permissions.ALL },
                 { Permissions.CAN_READ, Permissions.CAN_WRITE, Permissions.CAN_DELETE,
                         Permissions.CAN_READ_ACL, Permissions.CAN_WRITE_ACL,
                         Permissions.CAN_DELETE_ACL, Permissions.CAN_MANAGE, Permissions.ALL },
                 { Permissions.CAN_READ, Permissions.CAN_WRITE, Permissions.CAN_DELETE,
                         Permissions.CAN_READ_ACL, Permissions.CAN_WRITE_ACL,
                         Permissions.CAN_DELETE_ACL, Permissions.CAN_MANAGE, Permissions.ALL } },
                 new String[][] {
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u3 },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u2,
                                 u3 }, { User.ADMIN_USER, u1 } }, new String[][] { {}, {},
                         {}, { User.ANON_USER, Group.EVERYONE, u2, u3 } });
         checkPermissions(user2, testpaths, new Permission[][] { { Permissions.CAN_READ },
                 { Permissions.CAN_READ },
                 { Permissions.CAN_READ, Permissions.CAN_WRITE, Permissions.CAN_DELETE }, {} },
                 new String[][] {
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u3 },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u2,
                                 u3 }, { User.ADMIN_USER, u1 } }, new String[][] { {}, {},
                         {}, { User.ANON_USER, Group.EVERYONE, u2, u3 } });
         checkPermissions(user3, testpaths, new Permission[][] { { Permissions.CAN_READ },
                 { Permissions.CAN_READ }, { Permissions.CAN_READ }, {} }, new String[][] {
                 { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE },
                 { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u3 },
                 { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u2, u3 },
                 { User.ADMIN_USER, u1 } }, new String[][] { {}, {}, {},
                 { User.ANON_USER, Group.EVERYONE, u2, u3 } });
         
 
     }
 
     private void checkPermissions(User u, String[] testPath, Object[][] expectedPermissions,
             String[][] readers, String[][] deniedReaders) throws StorageClientException {
         AccessControlManagerImpl acmU = new AccessControlManagerImpl(client, u, configuration, null,  new LoggingStorageListener());
 
         for (int i = 0; i < testPath.length; i++) {
            Permission[] p = acmU.getPemissions(Security.ZONE_CONTENT, testPath[i]);
             LOGGER.info("Got {} {} {} ",
                     new Object[] { u.getId(), testPath[i], Arrays.toString(p) });
             Assert.assertArrayEquals(expectedPermissions[i], p);
             String[] r = acmU.findPrincipals(Security.ZONE_CONTENT, testPath[i],
                     Permissions.CAN_READ.getPermission(), true);
             Assert.assertArrayEquals(readers[i], sortToArray(ImmutableSet.of(r)));
             r = acmU.findPrincipals(Security.ZONE_CONTENT, testPath[i],
                     Permissions.CAN_READ.getPermission(), false);
             Assert.assertArrayEquals(deniedReaders[i], sortToArray(ImmutableSet.of(r)));
         }
 
     }
 
     private String[] sortToArray(Set<String> keySet) {
         return Lists.sortedCopy(keySet).toArray(new String[keySet.size()]);
     }
 
 }
