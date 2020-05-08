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
 package de.escidoc.core.test.om.item.rest;
 
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import de.escidoc.core.common.exceptions.remote.system.WebserverSystemException;
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.om.interfaces.ItemXpathsProvider;
 import de.escidoc.core.test.om.item.ItemTestBase;
 
 /**
  * Item tests with REST transport.
  * 
  * @author MSC
  * 
  */
 public class ItemComponentExternalContentRestTest extends ItemTestBase
     implements ItemXpathsProvider {
 
     /**
      * Constructor.
      * 
      */
     public ItemComponentExternalContentRestTest() {
         super(Constants.TRANSPORT_REST);
     }
 
     /**
      * Test successfully creating an item with a component containing a binary
      * content,referenced by an URL, and the attribute 'storage' set to
      * 'external-url'. The retrieve of the content is successful.
      * 
      * @throws Exception
      */
    //@Test
     public void testCreateItemWithExternalBinaryContentAndExternalExternalUrl()
         throws Exception {
         try {
             String[] ids = createItemWithExternalBinaryContent("external-url");
 
             this.getItemClient().getHttpClient().getParams()
                 .setParameter("http.protocol.handle-redirects", Boolean.TRUE);
             retrieveContent(ids[0], ids[1]);
         }
         finally {
             this.getItemClient().getHttpClient().getParams()
                 .setParameter("http.protocol.handle-redirects", Boolean.FALSE);
         }
     }
 
     /**
      * Test successfully creating an item with a component containing a binary
      * content,referenced by an URL, and the attribute 'storage' set to
      * 'external-managed'. The retrieve of the content is successful.
      * 
      * @throws Exception
      */
    //@Test
     public void testCreateItemWithExternalBinaryContentAndExternalManaged()
         throws Exception {
 
         String[] ids = createItemWithExternalBinaryContent("external-managed");
 
         retrieveContent(ids[0], ids[1]);
     }
 
     /**
      * Test declining retrieve a component content of a component containing the
      * attribute 'storage' set to 'external-managed' and a wrong URL.
      * 
      * @throws Exception
      */
     @Test
     public void testRetrieveItemWithStorageExternalUrlAndWrongUrl()
         throws Exception {
         Document item =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         String storageBeforeCreate = "external-managed";
         Document newItem =
             (Document) substitute(item,
                 "/item/components/component[2]/content/@storage",
                 storageBeforeCreate);
         Document newItem2 =
             (Document) substitute(newItem,
                 "/item/components/component[2]/content/@href",
                 "http://www.bla.de");
         Node itemWithoutSecondComponent =
             deleteElement(newItem2, "/item/components/component[1]");
         String xmlData = toString(itemWithoutSecondComponent, false);
 
         String theItemXml = create(xmlData);
         String theItemId =
             getObjidValue(EscidocRestSoapTestBase.getDocument(theItemXml));
 
         assertXmlValidItem(xmlData);
         Document createdItem = getDocument(theItemXml);
         String componentId;
         String componentHrefValue =
             selectSingleNode(createdItem, "/item/components/component/@href")
                 .getNodeValue();
 
         componentId = getObjidFromHref(componentHrefValue);
         try {
             retrieveContent(theItemId, componentId);
             fail("No exception occurred on retrieve content of a component with "
                + "the attribute 'storage' set to 'external-managed and a wrong url "
                + "/ir/item/" + theItemId 
                + "/components/component/" + componentId + "/content");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "WebserverSystemException", WebserverSystemException.class, e);
         }
     }
 }
