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
 
 package org.picketlink.idm.impl.store;
 
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.model.IdentityObjectCredential;
 import org.picketlink.idm.spi.model.IdentityObjectAttribute;
 import org.picketlink.idm.spi.search.IdentityObjectSearchCriteria;
 import org.picketlink.idm.spi.store.IdentityObjectSearchCriteriaType;
 import org.picketlink.idm.impl.api.PasswordCredential;
 import org.picketlink.idm.impl.api.BinaryCredential;
 import org.picketlink.idm.impl.api.SimpleAttribute;
 import org.picketlink.idm.impl.api.IdentitySearchCriteriaImpl;
 import org.picketlink.idm.api.SortOrder;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Random;
 import java.util.List;
 
 import junit.framework.Assert;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class CommonIdentityStoreTest extends Assert
 {
 
    IdentityStoreTestContext testContext;
 
    public CommonIdentityStoreTest(IdentityStoreTestContext context)
    {
       this.testContext = context;
    }
 
    public void setTestContext(IdentityStoreTestContext testContext)
    {
       this.testContext = testContext;
    }
 
    public IdentityStoreTestContext getTestContext()
    {
       return testContext;
    }
 
    public void testStorePersistence() throws Exception
    {
 
       testContext.begin();
 
       IdentityObject user1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Adam", IdentityTypeEnum.USER);
       IdentityObject user2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Eva", IdentityTypeEnum.USER);
 
       IdentityObject group1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision1", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision2", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group3 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision3", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group4 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision4", IdentityTypeEnum.ORGANIZATION);
 
       testContext.flush();
 
       assertEquals(0, testContext.getStore().getIdentityObjectsCount(testContext.getCtx(), IdentityTypeEnum.ROLE));
       assertEquals(2, testContext.getStore().getIdentityObjectsCount(testContext.getCtx(), IdentityTypeEnum.USER));
       assertEquals(4, testContext.getStore().getIdentityObjectsCount(testContext.getCtx(), IdentityTypeEnum.ORGANIZATION));
 
       testContext.flush();
 
       testContext.getStore().removeIdentityObject(testContext.getCtx(), user1);
       testContext.getStore().removeIdentityObject(testContext.getCtx(), group1);
       testContext.getStore().removeIdentityObject(testContext.getCtx(), group2);
 
       assertEquals(1, testContext.getStore().getIdentityObjectsCount(testContext.getCtx(), IdentityTypeEnum.USER));
       assertEquals(2, testContext.getStore().getIdentityObjectsCount(testContext.getCtx(), IdentityTypeEnum.ORGANIZATION));
 
       testContext.getStore().removeIdentityObject(testContext.getCtx(), user2);
       testContext.getStore().removeIdentityObject(testContext.getCtx(), group3);
       testContext.getStore().removeIdentityObject(testContext.getCtx(), group4);
 
       assertEquals(0, testContext.getStore().getIdentityObjectsCount(testContext.getCtx(), IdentityTypeEnum.USER));
       assertEquals(0, testContext.getStore().getIdentityObjectsCount(testContext.getCtx(), IdentityTypeEnum.ORGANIZATION));
 
       testContext.commit();
 
    }
 
    public void testFindMethods() throws Exception
    {
 
       testContext.begin();
 
       IdentityObject user1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Adam", IdentityTypeEnum.USER);
       IdentityObject user2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Eva", IdentityTypeEnum.USER);
 
       IdentityObject group1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision1", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision2", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group3 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision3", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group4 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision4", IdentityTypeEnum.ORGANIZATION);
 
       testContext.flush();
 
       IdentityObject xx = testContext.getStore().findIdentityObject(testContext.getCtx(), "Adam", IdentityTypeEnum.USER);
      assertEquals(xx.getId().toLowerCase(), user1.getId().toLowerCase());
 
       xx = testContext.getStore().findIdentityObject(testContext.getCtx(), user2.getId());
      assertEquals(xx.getId().toLowerCase(), user2.getId().toLowerCase());
 
       Collection results = testContext.getStore().findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, null);
       assertEquals(2, results.size());
 
       results = testContext.getStore().findIdentityObject(testContext.getCtx(), IdentityTypeEnum.ORGANIZATION, null);
       assertEquals(4, results.size());
 
       results = testContext.getStore().findIdentityObject(testContext.getCtx(), IdentityTypeEnum.ROLE, null);
       assertEquals(0, results.size());
 
 
       testContext.commit();
 
    }
 
    public void testAttributes() throws Exception
    {
 
       testContext.begin();
 
       IdentityObject user1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Adam", IdentityTypeEnum.USER);
       IdentityObject user2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Eva", IdentityTypeEnum.USER);
 
       testContext.flush();
 
       //Map<String, String[]> attrs = new HashMap<String, String[]>();
       IdentityObjectAttribute[] attrs = new IdentityObjectAttribute[]{
          new SimpleAttribute("key1", new String[]{"val1", "val2", "val3"}),
          new SimpleAttribute("key2", new String[]{"val1", "val2", "val3", "val4"})
       };
 
       testContext.getStore().addAttributes(testContext.getCtx(), user1, attrs);
 
       testContext.flush();
 
       Map<String, IdentityObjectAttribute> persistedAttrs = testContext.getStore().getAttributes(testContext.getCtx(), user1);
 
       assertEquals(2, persistedAttrs.keySet().size());
 
       assertTrue(persistedAttrs.containsKey("key1"));
       assertEquals(3, persistedAttrs.get("key1").getSize());
 
       assertTrue(persistedAttrs.containsKey("key2"));
       assertEquals(4, persistedAttrs.get("key2").getSize());
 
       testContext.flush();
 
       attrs = new IdentityObjectAttribute[]{
          new SimpleAttribute("key3", new String[]{"val1"})
       };
 
       testContext.getStore().addAttributes(testContext.getCtx(), user1, attrs);
 
       testContext.flush();
 
       persistedAttrs = testContext.getStore().getAttributes(testContext.getCtx(), user1);
 
       assertEquals(3, persistedAttrs.keySet().size());
 
       assertTrue(persistedAttrs.containsKey("key1"));
       assertEquals(3, persistedAttrs.get("key1").getSize());
 
       assertTrue(persistedAttrs.containsKey("key2"));
       assertEquals(4, persistedAttrs.get("key2").getSize());
 
       assertTrue(persistedAttrs.containsKey("key3"));
       assertEquals(1, persistedAttrs.get("key3").getSize());
 
       testContext.flush();
 
       attrs = new IdentityObjectAttribute[]{
          new SimpleAttribute("key3", new String[]{"val2"})
       };
 
       testContext.getStore().addAttributes(testContext.getCtx(), user1, attrs);
 
       testContext.flush();
 
       persistedAttrs = testContext.getStore().getAttributes(testContext.getCtx(), user1);
 
       assertEquals(3, persistedAttrs.keySet().size());
 
       assertTrue(persistedAttrs.containsKey("key3"));
       assertEquals(2, persistedAttrs.get("key3").getSize());
 
       testContext.flush();
 
       testContext.getStore().updateAttributes(testContext.getCtx(), user1, attrs);
 
       testContext.flush();
 
       persistedAttrs = testContext.getStore().getAttributes(testContext.getCtx(), user1);
 
       assertEquals(3, persistedAttrs.keySet().size());
 
       assertTrue(persistedAttrs.containsKey("key3"));
       assertEquals(1, persistedAttrs.get("key3").getSize());
 
       testContext.flush();
 
       testContext.getStore().removeAttributes(testContext.getCtx(), user1, new String[] {"key3"});
 
       testContext.flush();
 
       persistedAttrs = testContext.getStore().getAttributes(testContext.getCtx(), user1);
 
       assertEquals(2, persistedAttrs.keySet().size());
 
 
       testContext.commit();
 
    }
 
    public void testRelationships() throws Exception
    {
 
       testContext.begin();
 
       IdentityObject user1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Adam", IdentityTypeEnum.USER);
       IdentityObject user2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Eva", IdentityTypeEnum.USER);
 
       IdentityObject group1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision1", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision2", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group3 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision3", IdentityTypeEnum.ORGANIZATION);
       IdentityObject group4 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Devision4", IdentityTypeEnum.ORGANIZATION);
 
       testContext.flush();
 
       testContext.getStore().createRelationship(testContext.getCtx(), group1, user1, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, null, false);
       testContext.getStore().createRelationship(testContext.getCtx(), group2, user1, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, null, false);
 
       testContext.flush();
 
       assertEquals(1, testContext.getStore().resolveRelationships(testContext.getCtx(), group1, user1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), user1, group1, null).size());
       assertEquals(1, testContext.getStore().resolveRelationships(testContext.getCtx(), group2, user1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), user1, group2, null).size());
 
       testContext.getStore().removeRelationship(testContext.getCtx(), group2, user1, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, null);
 
       testContext.flush();
 
       assertEquals(1, testContext.getStore().resolveRelationships(testContext.getCtx(), group1, user1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), user1, group1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), group2, user1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), user1, group2, null).size());
 
       testContext.getStore().removeRelationships(testContext.getCtx(), user1, group1, false);
 
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), group1, user1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), user1, group1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), group2, user1, null).size());
       assertEquals(0, testContext.getStore().resolveRelationships(testContext.getCtx(), user1, group2, null).size());
 
       testContext.flush();
 
       // test find methods with relationships
 
       testContext.getStore().createRelationship(testContext.getCtx(), group1, user1, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, null, false);
       testContext.getStore().createRelationship(testContext.getCtx(), group1, user2, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, null, false);
 
       testContext.flush();
 
       assertEquals(2, testContext.getStore().findIdentityObject(testContext.getCtx(), group1, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, true, null).size());
       assertEquals(0, testContext.getStore().findIdentityObject(testContext.getCtx(), group1, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, false, null).size());
       assertEquals(1, testContext.getStore().findIdentityObject(testContext.getCtx(), user1, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, false, null).size());
       assertEquals(1, testContext.getStore().findIdentityObject(testContext.getCtx(), user2, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, false, null).size());
       assertEquals(0, testContext.getStore().findIdentityObject(testContext.getCtx(), group2, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, false, null).size());
       assertEquals(0, testContext.getStore().findIdentityObject(testContext.getCtx(), group2, RelationshipTypeEnum.JBOSS_IDENTITY_MEMBERSHIP, true, null).size());
 
       testContext.commit();
 
    }
 
    public void testPasswordCredential() throws Exception
    {
       testContext.begin();
 
       IdentityObject user1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Adam", IdentityTypeEnum.USER);
       IdentityObject user2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Eva", IdentityTypeEnum.USER);
 
       IdentityObjectCredential passwordCredential1 = new PasswordCredential("Password2000");
       IdentityObjectCredential passwordCredential2 = new PasswordCredential("Password2001");
 
       // If PASSWORD is supported
       assertTrue(testContext.getStore().getSupportedFeatures().isCredentialSupported(IdentityTypeEnum.USER, passwordCredential1.getType()));
 
       testContext.getStore().updateCredential(testContext.getCtx(), user1, passwordCredential1);
       testContext.getStore().updateCredential(testContext.getCtx(), user2, passwordCredential2);
 
       assertTrue(testContext.getStore().validateCredential(testContext.getCtx(), user1, passwordCredential1));
       assertTrue(testContext.getStore().validateCredential(testContext.getCtx(), user2, passwordCredential2));
       assertFalse(testContext.getStore().validateCredential(testContext.getCtx(), user1, passwordCredential2));
       assertFalse(testContext.getStore().validateCredential(testContext.getCtx(), user2, passwordCredential1));
       
       testContext.commit();
    }
 
    public void testBinaryCredential() throws Exception
    {
       testContext.begin();
 
       IdentityObject user1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Adam2", IdentityTypeEnum.USER);
       IdentityObject user2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Eva2", IdentityTypeEnum.USER);
 
       Random random = new Random();
 
       
       byte[] data1 = new byte[512000];
       random.nextBytes(data1);
       byte[] data2 = new byte[921600];
       random.nextBytes(data2);
 
       IdentityObjectCredential binaryCredential1 = new BinaryCredential(data1);
       IdentityObjectCredential binaryCredential2 = new BinaryCredential(data2);
 
 
       // If BINARY is supported
       assertTrue(testContext.getStore().getSupportedFeatures().isCredentialSupported(IdentityTypeEnum.USER, binaryCredential1.getType()));
       
       testContext.getStore().updateCredential(testContext.getCtx(), user1, binaryCredential1);
       testContext.getStore().updateCredential(testContext.getCtx(), user2, binaryCredential2);
 
       assertTrue(testContext.getStore().validateCredential(testContext.getCtx(), user1, binaryCredential1));
       assertTrue(testContext.getStore().validateCredential(testContext.getCtx(), user2, binaryCredential2));
       assertFalse(testContext.getStore().validateCredential(testContext.getCtx(), user1, binaryCredential2));
       assertFalse(testContext.getStore().validateCredential(testContext.getCtx(), user2, binaryCredential1));
 
       testContext.commit();
    }
 
 
 
 
    public void testCriteria() throws Exception
    {
       testContext.begin();
 
       IdentityObject group1 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Division1", IdentityTypeEnum.USER);
       IdentityObject group2 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Division2", IdentityTypeEnum.USER);
       IdentityObject group3 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Division3", IdentityTypeEnum.USER);
       IdentityObject group4 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Company1", IdentityTypeEnum.USER);
       IdentityObject group5 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Company2", IdentityTypeEnum.USER);
       IdentityObject group6 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Entity1", IdentityTypeEnum.USER);
       IdentityObject group7 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Entity2", IdentityTypeEnum.USER);
       IdentityObject group8 = testContext.getStore().createIdentityObject(testContext.getCtx(), "Entity3", IdentityTypeEnum.USER);
 
       Collection<IdentityObject> results = null;
       IdentityObjectSearchCriteria criteria = null;
 
       // TODO: by RelationshipType
 
       if (testContext.getStore().getSupportedFeatures().
          isSearchCriteriaTypeSupported(IdentityTypeEnum.USER, IdentityObjectSearchCriteriaType.NAME_FILTER))
       {
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().nameFilter("*");
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(8, results.size());
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().nameFilter("D*");
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(3, results.size());
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().nameFilter("*2");
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(3, results.size());
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().nameFilter("*3");
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(2, results.size());
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().nameFilter("Company1");
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(1, results.size());
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().nameFilter("Toronto");
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(0, results.size());
       }
 
 
       if (testContext.getStore().getSupportedFeatures().
          isSearchCriteriaTypeSupported(IdentityTypeEnum.USER, IdentityObjectSearchCriteriaType.PAGE))
       {
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().page(0, 3);
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(3, results.size());
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().page(2, 2);
 
 
          results = testContext.getStore().
             findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(2, results.size());
 
       }
       if (testContext.getStore().getSupportedFeatures().
          isSearchCriteriaTypeSupported(IdentityTypeEnum.USER, IdentityObjectSearchCriteriaType.SORT))
       {
 
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().sort(SortOrder.ASCENDING);
 
 
          results = testContext.getStore().
                findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(8, results.size());
 
          // Just check the first and the last one
          assertEquals("Company1", ((List<IdentityObject>)results).get(0).getName());
          assertEquals("Entity3", ((List<IdentityObject>)results).get(7).getName());
 
 
          // And reverse order
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().sort(SortOrder.DESCENDING);
 
 
          results = testContext.getStore().
                findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(8, results.size());
 
          // Just check the first and the last one
          assertEquals("Company1", ((List<IdentityObject>)results).get(7).getName());
          assertEquals("Entity3", ((List<IdentityObject>)results).get(0).getName());
 
 
 
 
 
          // Combine criteria to check that the results are diffrent for pagination
          if (testContext.getStore().getSupportedFeatures().
             isSearchCriteriaTypeSupported(IdentityTypeEnum.USER, IdentityObjectSearchCriteriaType.PAGE))
          {
 
             criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().sort(SortOrder.ASCENDING).page(0,3);
 
 
 
             results = testContext.getStore().
                findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
             assertEquals(3, results.size());
             assertEquals("Company1", ((List<IdentityObject>)results).get(0).getName());
             assertEquals("Division1", ((List<IdentityObject>)results).get(2).getName());
 
             criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl().sort(SortOrder.ASCENDING).page(3,1);
 
             results = testContext.getStore().
                findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
             assertEquals(1, results.size());
 
             assertEquals(1, results.size());
             assertEquals("Division2", ((List<IdentityObject>)results).get(0).getName());
 
          }
 
       }
       
       if (testContext.getStore().getSupportedFeatures().
          isSearchCriteriaTypeSupported(IdentityTypeEnum.USER, IdentityObjectSearchCriteriaType.ATTRIBUTE_FILTER))
       {
 
          IdentityObjectAttribute phone = new SimpleAttribute("phone", new String[] {"777 777 777"});
          IdentityObjectAttribute description = new SimpleAttribute("description", new String[] {"sample desc"});
 
          testContext.getStore().addAttributes(testContext.getCtx(), group1, new IdentityObjectAttribute[]{phone, description});
 
          criteria = (IdentityObjectSearchCriteria)new IdentitySearchCriteriaImpl()
             .attributeValuesFilter("phone", new String[] {"777 777 777"})
             .attributeValuesFilter("description", new String[] {"sample desc"});
 
 
 
          results = testContext.getStore().
                findIdentityObject(testContext.getCtx(), IdentityTypeEnum.USER, criteria);
 
          assertEquals(1, results.size());
 
       }
 
       testContext.commit();
 
    }
 
 
 }
