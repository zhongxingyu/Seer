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
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.Session;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification.Operation;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalTokenResolver;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorPlugin;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.lite.ConfigurationImpl;
 import org.sakaiproject.nakamura.lite.LoggingStorageListener;
 import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
 import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
 import org.sakaiproject.nakamura.api.lite.content.ContentManager;
 import org.sakaiproject.nakamura.lite.BaseMemoryRepository;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public abstract class AbstractAccessControlManagerImplTest {
     private static final Logger LOGGER = LoggerFactory
             .getLogger(AbstractAccessControlManagerImplTest.class);
     private StorageClient client;
     private ConfigurationImpl configuration;
     private StorageClientPool clientPool;
     private PrincipalValidatorResolver principalValidatorResolver = new PrincipalValidatorResolverImpl();
 
     @Before
     public void before() throws StorageClientException, AccessDeniedException, ClientPoolException,
             ClassNotFoundException, IOException {
         configuration = new ConfigurationImpl();
         Map<String, Object> properties = Maps.newHashMap();
         properties.put("keyspace", "n");
         properties.put("acl-column-family", "ac");
         properties.put("authorizable-column-family", Security.ZONE_AUTHORIZABLES);
         configuration.activate(properties);
         clientPool = getClientPool(configuration);
         client = clientPool.getClient();
         AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                 configuration);
         authorizableActivator.setup();
         LOGGER.info("Setup Complete");
     }
 
     protected abstract StorageClientPool getClientPool(Configuration configuration)
             throws ClassNotFoundException;
 
     @After
     public void after() throws ClientPoolException {
         client.close();
     }
 
     @Test
     public void test() throws StorageClientException, AccessDeniedException {
         AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
         User currentUser = authenticator.authenticate("admin", "admin");
         String u1 = "user1-" + System.currentTimeMillis();
         String u2 = "user2-" + System.currentTimeMillis();
         String u3 = "user3-" + System.currentTimeMillis();
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, null, new LoggingStorageListener(),
                 principalValidatorResolver);
         AclModification user1 = new AclModification(AclModification.grantKey(u1),
                 Permissions.CAN_ANYTHING.combine(Permissions.CAN_ANYTHING_ACL).getPermission(),
                 AclModification.Operation.OP_REPLACE);
         AclModification user2 = new AclModification(AclModification.grantKey(u2),
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE)
                         .getPermission(), AclModification.Operation.OP_REPLACE);
         AclModification user3 = new AclModification(AclModification.grantKey(u3),
                 Permissions.CAN_READ.getPermission(), AclModification.Operation.OP_REPLACE);
         String basepath = "testpath" + System.currentTimeMillis();
 
         accessControlManagerImpl.setAcl(Security.ZONE_AUTHORIZABLES, basepath,
                 new AclModification[] { user1, user2, user3 });
 
         Map<String, Object> acl = accessControlManagerImpl.getAcl(Security.ZONE_AUTHORIZABLES,
                 basepath);
         Assert.assertEquals(Integer.toHexString(Permissions.CAN_ANYTHING.combine(
                 Permissions.CAN_ANYTHING_ACL).getPermission()), Integer.toHexString((Integer) acl
                 .get(AclModification.grantKey(u1))));
         Assert.assertEquals(
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE)
                         .getPermission(),
                 ((Integer) acl.get(AclModification.grantKey(u2))).intValue());
         Assert.assertEquals(Permissions.CAN_READ.getPermission(),
                 ((Integer) acl.get(AclModification.grantKey(u3))).intValue());
         for (Entry<String, Object> e : acl.entrySet()) {
             LOGGER.info(" ACE {} : {} ", e.getKey(), e.getValue());
         }
         LOGGER.info("Got ACL {}", acl);
 
     }
 
     @Test
     public void testKern1515() throws Exception {
         AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
         User currentUser = authenticator.authenticate("admin", "admin");
         String u3 = "user3-" + System.currentTimeMillis();
         String basepath = "testpath" + System.currentTimeMillis();
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, null, new LoggingStorageListener(),
                 principalValidatorResolver);
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 null, client, configuration, accessControlManagerImpl, null, new LoggingStorageListener());
         authorizableManager.createUser(u3, "User 3", "test",
                 ImmutableMap.of("test", (Object) "test"));
 
         AclModification user3canRead = new AclModification(AclModification.grantKey(u3),
                 Permissions.CAN_READ.getPermission(), AclModification.Operation.OP_OR);
 
         AclModification user3canWrite = new AclModification(AclModification.grantKey(u3),
                 Permissions.CAN_WRITE.getPermission(), AclModification.Operation.OP_OR);
 
         AclModification user3cannotWrite = new AclModification(AclModification.denyKey(u3),
                 Permissions.CAN_WRITE.getPermission(), AclModification.Operation.OP_OR);
 
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath + "/zach",
                 new AclModification[] { user3canRead, user3canWrite });
 
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath + "/zach",
                 new AclModification[] { user3cannotWrite });
 
         Assert.assertFalse("User should not be able to write.", accessControlManagerImpl.can(
                 authorizableManager.findAuthorizable(u3), Security.ZONE_CONTENT,
                 basepath + "/zach", Permissions.CAN_WRITE));
         Assert.assertTrue("User should be able to read.", accessControlManagerImpl.can(
                 authorizableManager.findAuthorizable(u3), Security.ZONE_CONTENT,
                 basepath + "/zach", Permissions.CAN_READ));
     }
 
     @Test
     public void testPrivileges() throws StorageClientException, AccessDeniedException {
         AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
         User currentUser = authenticator.authenticate("admin", "admin");
         String u1 = "user1-" + System.currentTimeMillis();
         String u2 = "user2-" + System.currentTimeMillis();
         String u3 = "user3-" + System.currentTimeMillis();
         String basepath = "testpath" + System.currentTimeMillis();
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, null, new LoggingStorageListener(),
                 principalValidatorResolver);
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
 
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath + "/a/b/c",
                 new AclModification[] { user1CanAnything, user2CantReadWrite, user3cantRead,
                         denyAnon, denyEveryone });
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath + "/a/b",
                 new AclModification[] { user1CanAnything, user2CanReadWrite });
         accessControlManagerImpl.setAcl(Security.ZONE_CONTENT, basepath + "/a",
                 new AclModification[] { user1CanAnything, user3canRead });
 
         Map<String, Object> acl = accessControlManagerImpl.getAcl(Security.ZONE_CONTENT, basepath);
         Assert.assertArrayEquals(new String[] {}, acl.keySet().toArray());
 
         acl = accessControlManagerImpl.getAcl(Security.ZONE_CONTENT, basepath + "/a");
         acl = StorageClientUtils.getFilterMap(acl, null, null,
                 ImmutableSet.of("_aclKey", "_aclPath", "_aclType"), false);
         Assert.assertArrayEquals(Arrays.toString(sortToArray(acl.keySet())), new String[] {
                 AclModification.grantKey(u1), AclModification.grantKey(u3) },
                 sortToArray(acl.keySet()));
         acl = accessControlManagerImpl.getAcl(Security.ZONE_CONTENT, basepath + "/a/b");
         acl = StorageClientUtils.getFilterMap(acl, null, null,
                 ImmutableSet.of("_aclKey", "_aclPath", "_aclType"), false);
         Assert.assertArrayEquals(
                 new String[] { AclModification.grantKey(u1), AclModification.grantKey(u2) },
                 sortToArray(acl.keySet()));
         acl = accessControlManagerImpl.getAcl(Security.ZONE_CONTENT, basepath + "/a/b/c");
         acl = StorageClientUtils.getFilterMap(acl, null, null,
                 ImmutableSet.of("_aclKey", "_aclPath", "_aclType"), false);
         Assert.assertArrayEquals(new String[] { AclModification.denyKey(User.ANON_USER),
                 AclModification.denyKey(Group.EVERYONE), AclModification.grantKey(u1),
                 AclModification.denyKey(u2), AclModification.denyKey(u3) },
                 sortToArray(acl.keySet()));
 
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 null, client, configuration, accessControlManagerImpl, null, new LoggingStorageListener());
         authorizableManager.createUser(u1, "User 1", "test",
                 ImmutableMap.of("test", (Object) "test"));
         authorizableManager.createUser(u2, "User 2", "test",
                 ImmutableMap.of("test", (Object) "test"));
         authorizableManager.createUser(u3, "User 3", "test",
                 ImmutableMap.of("test", (Object) "test"));
 
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
 
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath,
                 Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath,
                 Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath,
                 Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath,
                 Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath,
                 Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath,
                 Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath,
                 Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath,
                 Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath,
                 Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT,
                 basepath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT,
                 basepath, Permissions.ALL));
 
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath
                 + "/a/b/c", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath
                 + "/a/b/c", Permissions.ALL));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath
                 + "/a/b/c", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath
                 + "/a/b/c", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT,
                 basepath + "/a/b/c", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath
                 + "/a/b/c", Permissions.CAN_READ));
 
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath
                 + "/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath
                 + "/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl
                 .can(user2, Security.ZONE_CONTENT, basepath + "/a/b", Permissions.CAN_WRITE
                         .combine(Permissions.CAN_READ).combine(Permissions.CAN_DELETE)));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath
                 + "/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath
                 + "/a/b", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath
                 + "/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath
                 + "/a/b", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath
                 + "/a/b", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT,
                 basepath + "/a/b", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT,
                 basepath + "/a/b", Permissions.ALL));
 
         Assert.assertTrue(accessControlManagerImpl.can(user1, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(adminUser, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user2, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(anonUser, Security.ZONE_CONTENT, basepath
                 + "/a", Permissions.ALL));
         Assert.assertTrue(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT,
                 basepath + "/a", Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(everyoneGroup, Security.ZONE_CONTENT,
                 basepath + "/a", Permissions.ALL));
 
         String[] testpaths = { basepath, basepath + "/a", basepath + "/a/b", basepath + "/a/b/c", };
 
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
                 new String[][] { { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u3 },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u2, u3 },
                         { User.ADMIN_USER, u1 } }, new String[][] { {}, {}, {},
                         { User.ANON_USER, Group.EVERYONE, u2, u3 } });
         checkPermissions(user2, testpaths, new Permission[][] { { Permissions.CAN_READ },
                 { Permissions.CAN_READ },
                 { Permissions.CAN_READ, Permissions.CAN_WRITE, Permissions.CAN_DELETE }, {} },
                 new String[][] { { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u3 },
                         { User.ADMIN_USER, User.ANON_USER, Group.EVERYONE, u1, u2, u3 },
                         { User.ADMIN_USER, u1 } }, new String[][] { {}, {}, {},
                         { User.ANON_USER, Group.EVERYONE, u2, u3 } });
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
         AccessControlManagerImpl acmU = new AccessControlManagerImpl(client, u, configuration,
                 null, new LoggingStorageListener(), principalValidatorResolver);
 
         for (int i = 0; i < testPath.length; i++) {
             Permission[] p = acmU.getPermissions(Security.ZONE_CONTENT, testPath[i]);
             LOGGER.info("Got {} {} {} ",
                     new Object[] { u.getId(), testPath[i], Arrays.toString(p) });
             Assert.assertArrayEquals(expectedPermissions[i], p);
             String[] r = acmU.findPrincipals(Security.ZONE_CONTENT, testPath[i],
                     Permissions.CAN_READ.getPermission(), true);
             Assert.assertArrayEquals(readers[i], sortToArray(ImmutableSet.copyOf(r)));
             r = acmU.findPrincipals(Security.ZONE_CONTENT, testPath[i],
                     Permissions.CAN_READ.getPermission(), false);
             Assert.assertArrayEquals(deniedReaders[i], sortToArray(ImmutableSet.copyOf(r)));
         }
 
     }
 
     private String[] sortToArray(Set<String> keySet) {
         return Ordering.natural().sortedCopy(keySet).toArray(new String[keySet.size()]);
     }
 
     @Test
     public void testTokenPermission() throws StorageClientException, AccessDeniedException {
         AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
         User currentUser = authenticator.authenticate("admin", "admin");
         String u3 = "user3-" + System.currentTimeMillis();
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, null, new LoggingStorageListener(),
                 principalValidatorResolver);
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 null, client, configuration, accessControlManagerImpl, null, new LoggingStorageListener());
         authorizableManager.createUser(u3, "User 3", "test",
                 ImmutableMap.of("test", (Object) "test"));
         Authorizable user3Auth = authorizableManager.findAuthorizable(u3);
         String targetContentPath = "targetContentPath" + System.currentTimeMillis();
         int grantedBitmap = Permissions.CAN_WRITE.getPermission();
         int deniedBitmap = Permissions.CAN_MANAGE.getPermission();
         String aclID = Integer.toHexString(grantedBitmap) + "_" + Integer.toHexString(deniedBitmap);
         String tokenPrincipal = AccessControlManager.DYNAMIC_PRINCIPAL_STEM + aclID;
 
         // grant access to the token, but deny access to everyone else.
         accessControlManagerImpl.setAcl(
                 Security.ZONE_CONTENT,
                 targetContentPath,
                 new AclModification[] {
                         new AclModification(AclModification.denyKey(tokenPrincipal), deniedBitmap,
                                 Operation.OP_REPLACE),
                         new AclModification(AclModification.grantKey(tokenPrincipal),
                                 grantedBitmap, Operation.OP_REPLACE),
                         new AclModification(AclModification.denyKey(Group.EVERYONE),
                                 Permissions.CAN_READ.getPermission(), Operation.OP_REPLACE),
                         new AclModification(AclModification.denyKey(User.ANON_USER),
                                 Permissions.CAN_READ.getPermission(), Operation.OP_REPLACE) });
         // the tokens should not be setup,
         final Content tokentContent = new Content("testtoken/" + tokenPrincipal, null);
         accessControlManagerImpl.signContentToken(tokentContent, targetContentPath);
         LOGGER.info("Checking Token {} ", tokentContent);
         accessControlManagerImpl.setRequestPrincipalResolver(new PrincipalTokenResolver() {
             public void resolveTokens(String principal, List<Content> tokens) {
                 tokens.add(tokentContent);
                 LOGGER.info("Principal {} checked tokens {}", principal, tokens);
             }
         });
         Assert.assertTrue(accessControlManagerImpl.can(user3Auth, Security.ZONE_CONTENT,
                 targetContentPath, Permissions.CAN_WRITE));
         Assert.assertFalse(accessControlManagerImpl.can(user3Auth, Security.ZONE_CONTENT,
                 targetContentPath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3Auth, Security.ZONE_CONTENT,
                 targetContentPath, Permissions.CAN_MANAGE));
 
         accessControlManagerImpl.clearRequestPrincipalResolver();
         LOGGER.info("Done Checking token");
     }
 
     @Test
     public void testTokenPermissionWithPlugin() throws StorageClientException,
             AccessDeniedException {
         AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
         User currentUser = authenticator.authenticate("admin", "admin");
         String u3 = "user3-" + System.currentTimeMillis();
 
         AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                 currentUser, configuration, null, new LoggingStorageListener(),
                 principalValidatorResolver);
         AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                 null, client, configuration, accessControlManagerImpl, null, new LoggingStorageListener());
         authorizableManager.createUser(u3, "User 3", "test",
                 ImmutableMap.of("test", (Object) "test"));
         Authorizable user3Auth = authorizableManager.findAuthorizable(u3);
         String targetContentPath = "targetContentPath" + System.currentTimeMillis();
         int grantedBitmap = Permissions.CAN_WRITE.getPermission();
         int deniedBitmap = Permissions.CAN_MANAGE.getPermission();
         String aclID = Integer.toHexString(grantedBitmap) + "_" + Integer.toHexString(deniedBitmap);
         String tokenPrincipal = AccessControlManager.DYNAMIC_PRINCIPAL_STEM + aclID;
 
         // grant access to the token, but deny access to everyone else.
         accessControlManagerImpl.setAcl(
                 Security.ZONE_CONTENT,
                 targetContentPath,
                 new AclModification[] {
                         new AclModification(AclModification.denyKey(tokenPrincipal), deniedBitmap,
                                 Operation.OP_REPLACE),
                         new AclModification(AclModification.grantKey(tokenPrincipal),
                                 grantedBitmap, Operation.OP_REPLACE),
                         new AclModification(AclModification.denyKey(Group.EVERYONE),
                                 Permissions.CAN_READ.getPermission(), Operation.OP_REPLACE),
                         new AclModification(AclModification.denyKey(User.ANON_USER),
                                 Permissions.CAN_READ.getPermission(), Operation.OP_REPLACE) });
         // the tokens should not be setup,
         final Set<Content> checked = Sets.newHashSet();
         principalValidatorResolver.registerPlugin("testvalidator", new PrincipalValidatorPlugin() {
             public boolean validate(Content proxyPrincipalToken) {
                 checked.add(proxyPrincipalToken);
                 return proxyPrincipalToken.hasProperty("protectedfield");
             }
 
             public String[] getProtectedFields() {
                 return new String[] { "protectedfield", "nullprotectedfield" };
             }
         });
         final Content tokentContent = new Content("testtokenwithPlugin/" + tokenPrincipal,
                 ImmutableMap.of(PrincipalTokenValidator.VALIDATORPLUGIN, (Object) "testvalidator",
                         "protectedfield", "protected"));
         accessControlManagerImpl.signContentToken(tokentContent, targetContentPath);
         LOGGER.info("Checking Token {} ", tokentContent);
         accessControlManagerImpl.setRequestPrincipalResolver(new PrincipalTokenResolver() {
             public void resolveTokens(String principal, List<Content> tokens) {
                 tokens.add(tokentContent);
                 LOGGER.info("Principal {} checked tokens {}", principal, tokens);
             }
         });
         Assert.assertTrue(accessControlManagerImpl.can(user3Auth, Security.ZONE_CONTENT,
                 targetContentPath, Permissions.CAN_WRITE));
         Assert.assertFalse(accessControlManagerImpl.can(user3Auth, Security.ZONE_CONTENT,
                 targetContentPath, Permissions.CAN_READ));
         Assert.assertFalse(accessControlManagerImpl.can(user3Auth, Security.ZONE_CONTENT,
                 targetContentPath, Permissions.CAN_MANAGE));
 
         Assert.assertEquals(1, checked.size());
         accessControlManagerImpl.clearRequestPrincipalResolver();
         LOGGER.info("Done Checking token");
         principalValidatorResolver.unregisterPlugin("testvalidator");
 
     }
 
     @Test
     public void testAccessInheritance() throws Exception {
         // for KERN-2158
         // all our dependencies
         Repository repository = (Repository) new BaseMemoryRepository().getRepository();
         Session adminSession = repository.loginAdministrative();
         AuthorizableManager adminAuthorizableManager = adminSession.getAuthorizableManager();
         ContentManager adminContentManager = adminSession.getContentManager();
         AccessControlManager adminAccessControlManager = adminSession.getAccessControlManager();
 
         // create two users
         Assert.assertTrue(adminAuthorizableManager.createUser("suzy", "suzy", "secret",
                 ImmutableMap.of("firstName", (Object) "Suzy", "lastName", "Queue")));
         Assert.assertTrue(adminAuthorizableManager.createUser("zach", "zach", "secret",
                 ImmutableMap.of("firstName", (Object) "Zach", "lastName", "Thomas")));
 
         // Create the innermost group and make suzy a member.
         Group group;
         Assert.assertTrue(adminAuthorizableManager.createGroup("inner", "inner", null));
         group = (Group) adminAuthorizableManager.findAuthorizable("inner");
         group.addMember("suzy");
         adminAuthorizableManager.updateAuthorizable(group);
         adminAccessControlManager.setAcl(Security.ZONE_AUTHORIZABLES, "inner",
                 new AclModification[] { new AclModification("inner@g", Permissions.CAN_READ.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAccessControlManager.setAcl(Security.ZONE_AUTHORIZABLES, "inner",
                 new AclModification[] { new AclModification("everyone@d", Permissions.ALL.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAccessControlManager.setAcl(Security.ZONE_AUTHORIZABLES, "inner",
                 new AclModification[] { new AclModification("anonymous@d", Permissions.ALL.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAuthorizableManager.updateAuthorizable(group);
 
         // Create a wrapper group.
         Assert.assertTrue(adminAuthorizableManager.createGroup("wrapper", "wrapper", null));
         group = (Group) adminAuthorizableManager.findAuthorizable("wrapper");
         group.addMember("inner");
         adminAuthorizableManager.updateAuthorizable(group);
         adminAccessControlManager.setAcl(Security.ZONE_AUTHORIZABLES, "wrapper",
                 new AclModification[] { new AclModification("wrapper@g", Permissions.CAN_READ.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAccessControlManager.setAcl(Security.ZONE_AUTHORIZABLES, "wrapper",
                 new AclModification[] { new AclModification("everyone@d", Permissions.ALL.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAccessControlManager.setAcl(Security.ZONE_AUTHORIZABLES, "wrapper",
                 new AclModification[] { new AclModification("anonymous@d", Permissions.ALL.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAuthorizableManager.updateAuthorizable(group);
 
         // Create some content to test.
         adminContentManager.update(new Content("a:wrapper", null));
         adminAccessControlManager.setAcl(Security.ZONE_CONTENT, "a:wrapper",
                 new AclModification[] { new AclModification("wrapper@g", Permissions.CAN_READ.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAccessControlManager.setAcl(Security.ZONE_CONTENT, "a:wrapper",
                 new AclModification[] { new AclModification("everyone@d", Permissions.ALL.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
         adminAccessControlManager.setAcl(Security.ZONE_CONTENT, "a:wrapper",
                 new AclModification[] { new AclModification("anonymous@d", Permissions.ALL.getPermission(),
                         AclModification.Operation.OP_REPLACE) });
 
         // Start a new session.
         adminSession.logout();
         Session adminSession2 = repository.loginAdministrative();
         AuthorizableManager adminAuthorizableManager2 = adminSession2.getAuthorizableManager();
         AccessControlManager adminAccessControlManager2 = adminSession2.getAccessControlManager();
 
         // make sure suzy can read
         Authorizable suzy = adminAuthorizableManager2.findAuthorizable("suzy");
         Assert.assertTrue(adminAccessControlManager2.can(suzy, Security.ZONE_AUTHORIZABLES, "inner",
                 Permissions.CAN_READ));
         Assert.assertTrue(adminAccessControlManager2.can(suzy, Security.ZONE_AUTHORIZABLES,
                 "wrapper", Permissions.CAN_READ));
         Assert.assertFalse(adminAccessControlManager2.can(suzy, Security.ZONE_AUTHORIZABLES,
                 "wrapper", Permissions.CAN_WRITE));
         Assert.assertFalse(adminAccessControlManager2.can(suzy, Security.ZONE_CONTENT, "a:wrapper",
                 Permissions.CAN_WRITE));
         Assert.assertTrue(adminAccessControlManager2.can(suzy, Security.ZONE_CONTENT, "a:wrapper",
                 Permissions.CAN_READ));
 
         // Make sure zach cannot
         Authorizable zach = adminAuthorizableManager2.findAuthorizable("zach");
         Assert.assertFalse(adminAccessControlManager2.can(zach, Security.ZONE_AUTHORIZABLES,
                 "wrapper", Permissions.CAN_READ));
         Assert.assertFalse(adminAccessControlManager2.can(zach, Security.ZONE_CONTENT, "a:wrapper",
                 Permissions.CAN_READ));
 
         final Session normalSession = repository.loginAdministrative("suzy");
         final AuthorizableManager normalAuthorizableManager = normalSession
                 .getAuthorizableManager();
         final AccessControlManager normalAccessControlManager = normalSession
                 .getAccessControlManager();
         Authorizable normalSuzy = normalAuthorizableManager.findAuthorizable("suzy");
         Assert.assertTrue(normalAccessControlManager.can(normalSuzy, Security.ZONE_AUTHORIZABLES,
                 "wrapper", Permissions.CAN_READ));
     }
 
 }
