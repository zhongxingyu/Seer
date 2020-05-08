 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.om.item;
 
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.w3c.dom.Node;
 
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.security.AuthorizationException;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.security.client.PWCallback;
 
 /**
  * Test the mock implementation of the item resource.
  * 
  * @author MSC
  * 
  */
 @RunWith(value = Parameterized.class)
 public class ItemRetrieveTest extends ItemTestBase {
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public ItemRetrieveTest(final int transport) {
         super(transport);
     }
 
     /**
      * Test successfully retrieving item with a component without valid-status.
      * Issue 655.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveItemWithoutComponentValidStatus() throws Exception {
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false),
                 "escidoc_item_198_for_create_ComponentWithoutValidStatus.xml");
         String itemXml = create(xml);
         String itemId = getObjidValue(itemXml);
         String componentId =
             getObjidValue(toString(
                 selectSingleNode(getDocument(itemXml),
                     "/item/components/component"), true));
 
         String retrievedItem = retrieve(itemId);
         assertXmlValidItem(retrievedItem);
         if (getTransport() == Constants.TRANSPORT_REST) {
             retrieveContent(itemId, componentId);
         }
         // TODO assert that the retrieved item contains the expected values
         assertCreatedItem(retrievedItem, itemXml, startTimestamp);
     }
 
     /**
      * Test successfully retrieving item.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMRi1a() throws Exception {
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         String itemXml = create(xml);
         String itemId = getObjidValue(itemXml);
 
         String retrievedItem = retrieve(itemId);
         assertXmlValidItem(retrievedItem);
         // TODO assert that the retrieved item contains the expected values
         assertCreatedItem(retrievedItem, itemXml, startTimestamp);
     }
 
     /**
      * Test declining retrieving item with not existing id.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMRi2a() throws Exception {
 
         try {
             retrieve("test");
             fail("No ItemNotFoundException retrieving item with not existing id.");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 ItemNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test declining retrieving item with wrong id (id refers to another object
      * type).
      * 
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMRi5() throws Exception {
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         String itemXml = create(xml);
         String componentId = null;
 
         if (getTransport() == Constants.TRANSPORT_REST) {
             Node componentObjiId =
                 selectSingleNode(EscidocRestSoapTestBase.getDocument(itemXml),
                     "/item/components/component/@href");
             componentId = getObjidFromHref(componentObjiId.getTextContent());
         }
         else {
             Node componentObjiId =
                 selectSingleNode(EscidocRestSoapTestBase.getDocument(itemXml),
                     "/item/components/component/@objid");
             componentId = componentObjiId.getTextContent();
         }
 
         try {
             retrieve(componentId);
         }
         catch (ItemNotFoundException e) {
             return;
         }
         fail("Not expected exception");
     }
 
     /**
      * Test declining retrieving item (input parameter item id is missing).
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMDi3() throws Exception {
 
         try {
 
             retrieve(null);
         }
         catch (MissingMethodParameterException e) {
             return;
         }
         fail("Not expected exception");
     }
 
     /**
      * Test declining retrieving depositor item as anonymous.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveAnonymous() throws Exception {
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         // PWCallback.setHandle(PWCallback.DEPOSITOR_LIB_HANDLE);
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         String itemXml = create(xml);
         String itemId = getObjidValue(itemXml);
 
         PWCallback.setHandle(PWCallback.ANONYMOUS_HANDLE);
         Class ec = AuthorizationException.class;
         try {
             retrieve(itemId);
             fail(ec.getName() + " expected");
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
         finally {
             PWCallback.resetHandle();
         }
     }
 
     /**
      * Test declining retrieving depositor item as author.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveAuthor() throws Exception {
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         // PWCallback.setHandle(PWCallback.DEPOSITOR_LIB_HANDLE);
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         String itemXml = create(xml);
         String itemId = getObjidValue(itemXml);
 
         PWCallback.setHandle(PWCallback.AUTHOR_HANDLE);
         Class ec = AuthorizationException.class;
         try {
             retrieve(itemId);
             fail(ec.getName() + " expected");
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
         finally {
             PWCallback.resetHandle();
         }
     }
 
     /**
      * Test decline retrieving depositor item as other depositor. Issue 608
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveOtherDepositor() throws Exception {
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         String itemXml = create(xml);
         String itemId = getObjidValue(itemXml);
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_LIB_HANDLE);
         try {
             retrieve(itemId);
             EscidocRestSoapTestBase
                 .failMissingException(AuthorizationException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 AuthorizationException.class, e);
         }
         PWCallback.resetHandle();
     }
 
     // /**
     // * Test declining retrieving depositor item as other depositor. Issue 608
     // * (unfixed).
     // *
     // *
     // * @test.status Implemented
     // *
     // * @throws Exception
     // * If anything fails.
     // */
     // public void testRetrieveUnprevDepositor() throws Exception {
     // String xml =
     // EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
     // + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
     // // PWCallback.setHandle(PWCallback.DEPOSITOR_LIB_HANDLE);
     // PWCallback.setHandle("ex_adm");
     // String itemXml = create(xml);
     // String itemId = getObjidValue(itemXml);
     //
     // PWCallback.setHandle("ex_dep");
     // Class ec = AuthorizationException.class;
     // try {
     // retrieve(itemId);
     // fail(ec.getName() + " expected");
     // }
     // catch (Exception e) {
     // assertExceptionType(ec, e);
     // }
     // finally {
     // PWCallback.resetHandle();
     // }
     // }
 
     /**
      * Test retrieving items.
      * 
      * See Bugzilla #586
      * 
      * @throws Exception
      *             Thrown if the retrieved list is invalid.
      */
     @Test
     public void testRetrieveItems() throws Exception {
 
         String reqCT = "escidoc:persistent4";
         final Map<String, String[]> filterParams =
             new HashMap<String, String[]>();
 
         filterParams.put(FILTER_PARAMETER_QUERY,
             new String[] { "\"/properties/content-model/id\"=" + reqCT });
         filterParams
             .put(FILTER_PARAMETER_MAXIMUMRECORDS, new String[] { "10" });
 
         String list = retrieveItems(filterParams);
        assertXmlValidItemList(list);
 
         // assert that the components elements has not empty (or $link etc.)
         // values
     }
 
     /**
      * Test successfully retrieving md-record.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveMdRecord() throws Exception {
         retrieveMdRecord(true, "escidoc");
     }
 
     /**
      * Test decline retrieving md-record without item ID.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveMdRecordWithoutItemID() throws Exception {
         Class ec = MissingMethodParameterException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(false, "escidoc");
         }
         catch (Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test decline retrieving md-record with no name.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveMdRecordWithoutName() throws Exception {
         Class ec = MissingMethodParameterException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(true, null);
         }
         catch (Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test decline retrieving md-record with empty name.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveMdRecordWithEmptyName() throws Exception {
         Class ec = MissingMethodParameterException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(true, "");
         }
         catch (Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test successfully retrieving md-record with non existing name.
      * 
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveMdRecordNonExistingName() throws Exception {
         Class ec = MdRecordNotFoundException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(true, "blablub");
         }
         catch (Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test if the objid is handles right.
      * 
      * see issue INFR-773
      * 
      * The tests creates an Item with one Component and uses then on the Item
      * handler the ComponentID with and without version suffix. The framework
      * has to answer with ItemNotFoundException in all cases.
      * 
      * @throws Exception
      *             If framework behavior is not as expected.
      */
     @Test
     public void testWrongObjid01() throws Exception {
 
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_1_component.xml");
         String itemXml = create(xml);
         // String itemId = getObjidValue(itemXml);
         String componentId =
             getObjidValue(toString(
                 selectSingleNode(getDocument(itemXml),
                     "/item/components/component"), true));
 
         try {
             retrieve(componentId);
         }
         catch (Exception e) {
             assertExceptionType("Wrong exception", ItemNotFoundException.class,
                 e);
         }
 
         try {
             retrieve(componentId + ":1");
         }
         catch (Exception e) {
             assertExceptionType("Wrong exception", ItemNotFoundException.class,
                 e);
         }
 
         try {
             retrieve(componentId + ":a");
         }
         catch (Exception e) {
             assertExceptionType("Wrong exception", ItemNotFoundException.class,
                 e);
         }
     }
 
     /**
      * Creates an Item and retrieves the md-record by given name.
      * 
      * @param resourceId
      *            If the retrieve should be done with resource ID.
      * @param name
      *            The name of the md-record to be retrieved.
      * @throws Exception
      *             If an error occures.
      */
     private void retrieveMdRecord(final boolean resourceId, final String name)
         throws Exception {
         String xml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         String resourceXml = create(xml);
         String itemId = getObjidValue(resourceXml);
         if (!resourceId) {
             itemId = null;
         }
 
         String retrievedMdRecord = retrieveMetadataRecord(itemId, name);
         assertCreatedMdRecord(name, itemId, "item", retrievedMdRecord,
             resourceXml, startTimestamp);
 
     }
 
 }
