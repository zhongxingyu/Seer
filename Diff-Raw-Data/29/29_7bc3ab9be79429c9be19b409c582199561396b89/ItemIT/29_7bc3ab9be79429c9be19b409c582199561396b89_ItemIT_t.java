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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.FileNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.Constants;
 import de.escidoc.core.test.om.interfaces.ItemXpathsProvider;
 
 /**
  * Test the mock implementation of the item resource.
  * 
  * @author Michael Schneider
  */
 public class ItemIT extends ItemTestBase {
 
     private String theItemId = null;
 
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testIssue575() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_issue575.xml");
         String item = toString(xmlItem, true);
 
         item = create(item);
         assertXmlValidItem(item);
         Node node = selectSingleNode(EscidocAbstractTest.getDocument(item), "/item/components");
         assertNotNull("components container not available.", node);
 
         NodeList nodes = selectNodeList(EscidocAbstractTest.getDocument(item), "/item/components/component");
         assertEquals("Found some components, but none expected.", 0, nodes.getLength());
         assertNotNull(node);
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testNatasaItemWithWhitespaces() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "ItemWithWhitespaces.xml");
         String item = toString(xmlItem, true);
 
         item = create(item);
         assertXmlValidItem(item);
         Node node = selectSingleNode(EscidocAbstractTest.getDocument(item), "/item/components");
         assertNotNull("components container not available.", node);
 
     }
 
     /**
      * Successfully create item and component with md-record "escidoc" containing MODS. Issue 632.
      */
     @Test
     public void testModsMd() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_MODS-MD.xml");
         String item = toString(xmlItem, true);
 
         item = create(item);
         assertXmlValidItem(item);
     }
 
     /**
      * Test content-model-specific is not need but can be added.
      */
     @Test
     public void testContentModelSpecific() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_issue575.xml");
         String item = toString(deleteNodes(xmlItem, "/item/properties/content-model-specific"), true);
 
         // create item without c-m-s
         String itemId = null;
         try {
             item = create(item);
             itemId = getObjidValue(item);
         }
         catch (final Exception e) {
             failException("No exception expected!", e);
         }
         assertXmlValidItem(item);
         Document itemDoc = EscidocAbstractTest.getDocument(item);
         Node node = selectSingleNode(itemDoc, "/item/properties/content-model-specific");
         assertNull("No element content-model-specific expected.", node);
 
         // add c-m-s
         String propertiesNamespacePrefix = determinePropertiesNamespacePrefix(itemDoc);
         String itemNamespacePrefix = determineItemNamespacePrefix(itemDoc);
        if (!propertiesNamespacePrefix.isEmpty()) {
            propertiesNamespacePrefix += ":";
        }
        if (!itemNamespacePrefix.isEmpty()) {
            itemNamespacePrefix += ":";
        }
         item =
            item.replaceFirst("</" + itemNamespacePrefix + "properties", "<" + propertiesNamespacePrefix
                + "content-model-specific/></" + itemNamespacePrefix + "properties");
         item = update(itemId, item);
         selectSingleNodeAsserted(getDocument(item), "/item/properties/content-model-specific");
         // insert into c-m-s
         item =
            item.replaceFirst("<" + propertiesNamespacePrefix + "content-model-specific[^>]*>", "<"
                + propertiesNamespacePrefix + "content-model-specific><nix></nix></" + propertiesNamespacePrefix
                + "content-model-specific>");
         item = update(itemId, item);
         selectSingleNodeAsserted(getDocument(item), "/item/properties/content-model-specific/nix");
         // remove content from c-m-s
         item = toString(deleteNodes(getDocument(item), "/item/properties/content-model-specific/nix"), true);
         item = update(itemId, item);
         node = selectSingleNode(EscidocAbstractTest.getDocument(item), "/item/properties/content-model-specific/nix");
         assertNull("No element content-model-specific expected.", node);
         // remove c-m-s should not delete
         item = toString(deleteNodes(getDocument(item), "/item/properties/content-model-specific"), true);
         item = update(itemId, item);
         selectSingleNodeAsserted(getDocument(item), "/item/properties/content-model-specific");
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testZimPBItemCreate() throws Exception {
         Document xmlItem = EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "item-001.xml");
         String item = toString(xmlItem, true);
 
         item = create(item);
         assertXmlValidItem(item);
         Node node = selectSingleNode(EscidocAbstractTest.getDocument(item), "/item/properties/pid");
         assertNotNull(node);
 
         Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("objid=\"([^\"]*)\"");
         Matcher m = PATTERN_OBJID_ATTRIBUTE.matcher(item);
         if (m.find()) {
             delete(m.group(1));
         }
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testZimJMItemCreate() throws Exception {
 
         Document xmlItem = EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "item-002.xml");
         String item = toString(xmlItem, true);
         // assertXmlValidItem(item);
 
         item = create(item);
         assertXmlValidItem(item);
 
         Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("objid=\"([^\"]*)\"");
         Matcher m = PATTERN_OBJID_ATTRIBUTE.matcher(item);
         if (m.find()) {
             delete(m.group(1));
         }
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateZim2Item() throws Exception {
 
         Document xmlItem = EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "zim2-create.xml");
         String item = toString(xmlItem, true);
         // assertXmlValidItem(item);
 
         item = create(item);
         assertXmlValidItem(item);
 
         String createdItemId = getIdFromRootElement(item);
 
         Document item4Update =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "zim2-updateTest.xml");
         String item4UpdateXml = toString(item4Update, true);
 
         item4UpdateXml = item4UpdateXml.replaceAll("##ITEMID##", createdItemId);
         item4UpdateXml =
             item4UpdateXml.replaceAll("##LASTMODIFICATIONDATE##", selectSingleNode(
                 EscidocAbstractTest.getDocument(item), "/item/@last-modification-date").getNodeValue());
         item4UpdateXml =
             item4UpdateXml.replaceAll("##CREATIONDATE##", selectSingleNode(EscidocAbstractTest.getDocument(item),
                 "/item/properties/creation-date/text()").getNodeValue());
         // assertXmlValidItem(item4UpdateXml);
         String updatedItem = update(createdItemId, item4UpdateXml);
         assertXmlValidItem(updatedItem);
 
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testCreateUpdateIssue615() throws Exception {
 
         Document xmlItem = EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "zim2-create.xml");
         String item = toString(xmlItem, true);
         // assertXmlValidItem(item);
 
         item = create(item);
         assertXmlValidItem(item);
 
         String createdItemId = getIdFromRootElement(item);
         String createTitle = selectSingleNode(getDocument(item), "/item/@title").getNodeValue();
 
         Document item4Update =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "zim2-updateTest.xml");
         // remove all changes except title in metadata
         item4Update = (Document) deleteElement(item4Update, "/item/properties/content-model-specific/*");
         String item4UpdateXml = toString(item4Update, true);
 
         item4UpdateXml = item4UpdateXml.replaceAll("##ITEMID##", createdItemId);
         item4UpdateXml =
             item4UpdateXml.replaceAll("##LASTMODIFICATIONDATE##", selectSingleNode(
                 EscidocAbstractTest.getDocument(item), "/item/@last-modification-date").getNodeValue());
         item4UpdateXml =
             item4UpdateXml.replaceAll("##CREATIONDATE##", selectSingleNode(EscidocAbstractTest.getDocument(item),
                 "/item/properties/creation-date/text()").getNodeValue());
         // assertXmlValidItem(item4UpdateXml);
 
         String updatedItem = update(createdItemId, item4UpdateXml);
         assertXmlValidItem(updatedItem);
         updatedItem = retrieve(createdItemId);
         assertXmlValidItem(updatedItem);
         String updateTitle = selectSingleNode(getDocument(updatedItem), "/item/@title").getNodeValue();
 
         assertNotEquals("Title should be changed", createTitle, updateTitle);
 
     }
 
     /**
      * Test successfully creating content item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi1a() throws Exception {
 
         // String fileName =
         // "src/java/de/escidoc/core/test/om/template/item/escidoc_item_198_for_create.xml";
         // String fileName =
         // "C:/workarea/projects/EscidocTest/src/java/de/escidoc/core/test/om/template/item/escidoc_item_198_for_create.xml";
         // File xmlNeu = new File(fileName);
         // File file = new File(
         // fileName);
         // InputStream is = new FileInputStream(file);
         // int b;
         // StringBuffer sb = new StringBuffer();
         // String xmlData = null;
         // while ((b = is.read()) != -1) {
         // sb = sb.append((char) b);
         // }
         // xmlData = sb.toString();
         // String test = getTemplateAsString(TEMPLATE_ITEM_PATH,
         // "escidoc_item_198_for_create.xml");
 
         // String xmlData = getTemplateAsString(TEMPLATE_ITEM_PATH,
         // "escidoc_item_198_for_create.xml");
 
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
 
         Node xmlItemWithoutComponents = deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         final String createdXml = create(itemWithoutComponents);
 
         assertXmlValidItem(createdXml);
         final Document createdDocument = EscidocAbstractTest.getDocument(createdXml);
 
         this.theItemId = getIdFromRootElement(createdXml);
 
         String creatorId = null;
         Pattern PATTERN_OBJID_ATTRIBUTE = null;
         PATTERN_OBJID_ATTRIBUTE = Pattern.compile(".*\\/([^\"\\/]*)");
 
         Node itemObjiId = selectSingleNode(EscidocAbstractTest.getDocument(createdXml), "/item/@href");
 
         Matcher m1 = PATTERN_OBJID_ATTRIBUTE.matcher(itemObjiId.getTextContent());
         if (m1.find()) {
             this.theItemId = m1.group(1);
         }
 
         Node creatorIdNode =
             selectSingleNode(EscidocAbstractTest.getDocument(createdXml), "/item/properties/created-by/@href");
         Matcher m3 = PATTERN_OBJID_ATTRIBUTE.matcher(creatorIdNode.getTextContent());
         if (m3.find()) {
             creatorId = m3.group(1);
         }
         assertXmlEquals("href value is wrong", createdDocument, "/item/@href", "/ir/item/" + theItemId);
         assertXmlEquals("href value is wrong", createdDocument, "/item/md-records/@href ", "/ir/item/" + theItemId
             + "/md-records");
         assertXmlEquals("href value is wrong", createdDocument, "/item/md-records/md-record[1]/@href ", "/ir/item/"
             + theItemId + "/md-records/md-record/escidoc");
         assertXmlEquals("href value is wrong", createdDocument, "/item/properties/@href ", "/ir/item/" + theItemId
             + "/properties");
         assertXmlEquals("href value is wrong", createdDocument, "/item/md-records/md-record[1]/@href ", "/ir/item/"
             + theItemId + "/md-records/md-record/escidoc");
         assertXmlEquals("href value is wrong", createdDocument, "/item/properties/@href ", "/ir/item/" + theItemId
             + "/properties");
         Node itemTitle = selectSingleNode(createdDocument, "/item/@title");
         assertNotNull(itemTitle);
         assertFalse("item title is not set", itemTitle.getTextContent().equals(""));
         Node creatorTitle = selectSingleNode(createdDocument, "/item/properties/created-by/@title");
         assertNotNull(creatorTitle.getTextContent());
         assertNotNull(creatorId);
 
         Node modifiedDate = selectSingleNode(createdDocument, "/item/@last-modification-date");
         assertNotNull(modifiedDate);
         assertFalse("modified date is not set", modifiedDate.getTextContent().equals(""));
         assertXmlEquals("status value is wrong", createdDocument, "/item/properties/public-status", "pending");
         Node createdDate = selectSingleNode(createdDocument, "/item/properties/creation-date");
         assertNotNull(createdDate);
         assertFalse("created date is not set", createdDate.getTextContent().equals(""));
         assertXmlEquals("lock-status value is wrong", createdDocument, "/item/properties/lock-status", "unlocked");
         assertXmlEquals("current version number is wrong", createdDocument, "/item/properties/version/number", "1");
         assertXmlEquals("current version status is wrong", createdDocument, "/item/properties/version/status",
             "pending");
         // assertXmlEquals("current version valid-status is wrong",
         // createdDocument, "/item/properties/version/valid-status", "valid");
         assertXmlEquals("current version date is wrong", createdDocument, "/item/properties/version/date", modifiedDate
             .getTextContent());
         assertXmlEquals("latest version number is wrong", createdDocument, "/item/properties/latest-version/number",
             "1");
         assertXmlEquals("latest version date is wrong", createdDocument, "/item/properties/latest-version/date",
             modifiedDate.getTextContent());
 
         // assert metadata /publication/creator/person containing backslash
         // (see issue #286)
         assertXmlEquals("Metadata /publication/creator/person different", xmlItem, createdDocument,
             XPATH_ITEM_MD_RECORD + "/publication/creator/person/complete-name");
 
     }
 
     /**
      * Test successfully creating duplicate content item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi1b() throws Exception {
 
         Document xmlItem1 =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents1 = deleteElement(xmlItem1, "/item/components");
         String itemWithoutComponents1 = toString(xmlItemWithoutComponents1, false);
         // create the first item
         String xml1 = create(itemWithoutComponents1);
 
         assertXmlValidItem(xml1);
         // Node itemObjiId1 = selectSingleNode(getDocument(xml1),
         // "/item/@objid");
         // String itemId1 = itemObjiId1.getTextContent();
         String itemId1 = getIdFromRootElement(xml1);
 
         // create the second item
         String xml2 = create(itemWithoutComponents1);
         assertXmlValidItem(xml2);
 
         String itemId2 = getIdFromRootElement(xml2);
         assertNotEquals("item id is not changed", itemId2, itemId1);
         delete(itemId1);
         delete(itemId2);
     }
 
     /**
      * Test declining creating an item without specifying the content model id, using data provided for issue 365.
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test(expected = XmlCorruptedException.class)
     public void testOMCi_issue365() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         deleteElement(toBeCreatedDocument, XPATH_ITEM_CONTENT_MODEL);
         addAfter(toBeCreatedDocument, XPATH_ITEM_CONTEXT, createElementNode(toBeCreatedDocument,
             Constants.NS_COMMON_SREL, SREL_PREFIX_ESCIDOC, NAME_CONTENT_MODEL, null));
 
         String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         create(toBeCreatedXml);
         // if not fail: Creating item with empty content-model element not declined.
     }
 
     /**
      * Test declining creating content item with missing Context ID.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = MissingAttributeValueException.class)
     public void testOMCi2a() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents = deleteElement(xmlItem, "/item/components");
 
         Node xmlItemWithoutContextId = substitute(xmlItemWithoutComponents, "/item/properties/context/@href", "");
 
         String itemWithoutContextId = toString(xmlItemWithoutContextId, true);
 
         create(itemWithoutContextId);
     }
 
     /**
      * Test declining creating content item with missing Content Type.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = XmlCorruptedException.class)
     public void testOMCi2b() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents = deleteElement(xmlItem, "/item/components");
         Node xmlItemWithoutContentType = deleteElement(xmlItemWithoutComponents, "/item/properties/content-model");
 
         String itemWithoutContentType = toString(xmlItemWithoutContentType, true);
 
         create(itemWithoutContentType);
     }
 
     /**
      * Test declining creating content item without any md-record.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = XmlSchemaValidationException.class)
     public void testOMCi2c() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents = deleteElement(xmlItem, "/item/components");
         Node xmlItemWithoutEscidocMetadata =
             deleteElement(xmlItemWithoutComponents, "/item/md-records/md-record/publication");
 
         String itemWithoutEscidocMetadata = toString(xmlItemWithoutEscidocMetadata, true);
 
         create(itemWithoutEscidocMetadata);
     }
 
     /**
      * Test declining creating content item with missing Escidoc Internal Metadata Set.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi2e() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents = deleteElement(xmlItem, "/item/components");
         // Node attribute =
         // selectSingleNode(xmlItemWithoutComponents,
         // "/item/md-records/md-record/[@name = 'escidoc']/@name");
         // "/item/md-records/md-record/publication");
         Node xmlItemWithoutInternalMetadata =
             substitute(xmlItemWithoutComponents, "/item/md-records/md-record[@name = 'escidoc']/@name", "bla");
         String itemWithoutInternalMetadataXml = toString(xmlItemWithoutInternalMetadata, true);
 
         Class<?> ec = MissingMdRecordException.class;
         try {
             String xml = create(itemWithoutInternalMetadataXml);
             EscidocAbstractTest.failMissingException(ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating content item with not existing Context.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = ContextNotFoundException.class)
     public void testOMCi3a() throws Exception {
 
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents = deleteElement(xmlItem, "/item/components");
         Node xmlItemWithNotExistingContext = null;
 
         xmlItemWithNotExistingContext = substitute(xmlItemWithoutComponents, "/item/properties/context/@href", "bla");
         String itemWithNotExistingContext = toString(xmlItemWithNotExistingContext, true);
 
         create(itemWithNotExistingContext);
     }
 
     /**
      * Test successfully creating content item with one Content Component.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi4() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutSecondComponent = deleteElement(xmlItem, "/item/components/component[2]");
         String itemWithoutSecondComponent = toString(xmlItemWithoutSecondComponent, true);
 
         String xml = create(itemWithoutSecondComponent);
         assertXmlValidItem(xml);
 
         String componentId = null;
         String creatorId = null;
         Pattern PATTERN_OBJID_ATTRIBUTE = null;
         PATTERN_OBJID_ATTRIBUTE = Pattern.compile(".*\\/([^\"\\/]*)");
 
         Node itemObjiId = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@href");
 
         Matcher m1 = PATTERN_OBJID_ATTRIBUTE.matcher(itemObjiId.getTextContent());
         if (m1.find()) {
             this.theItemId = m1.group(1);
         }
 
         Node componentObjiId =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/components/component/@href");
 
         Matcher m2 = PATTERN_OBJID_ATTRIBUTE.matcher(componentObjiId.getTextContent());
         if (m2.find()) {
             componentId = m2.group(1);
         }
 
         Node creatorIdNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@href");
         Matcher m3 = PATTERN_OBJID_ATTRIBUTE.matcher(creatorIdNode.getTextContent());
         if (m3.find()) {
             creatorId = m3.group(1);
         }
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/@href", "/ir/item/"
             + this.theItemId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/md-records/@href ",
             "/ir/item/" + this.theItemId + "/md-records");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/md-records/md-record[1]/@href ", "/ir/item/" + this.theItemId + "/md-records/md-record/escidoc");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/properties/@href ",
             "/ir/item/" + this.theItemId + "/properties");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/components/@href",
             "/ir/item/" + this.theItemId + "/components");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component/@href", "/ir/item/" + this.theItemId + "/components/component/" + componentId);
         assertXmlEquals("creator value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component/properties/created-by/@href", "/aa/user-account/" + creatorId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/content/@href", "/ir/item/" + this.theItemId + "/components/component/"
                 + componentId + "/content");
         Node itemTitle = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@title");
         assertNotNull(itemTitle);
         assertFalse("item title is not set", itemTitle.getTextContent().equals(""));
         Node creatorTitle =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@title");
         assertNotNull(creatorTitle.getTextContent());
         Node componentTitleNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/components/component/@title");
         String componentTitle = componentTitleNode.getTextContent();
         assertFalse("component title is not set", componentTitle.equals(""));
         assertXmlEquals("creator title value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component/properties/created-by/@title", creatorTitle.getTextContent());
         Node contentTitleNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/components/component/content/@title");
         assertNotNull(contentTitleNode);
 
         assertFalse("component content title is not set", contentTitleNode.getTextContent().equals(""));
 
         Node modifiedDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@last-modification-date");
         assertNotNull(modifiedDate);
         assertFalse("modified date is not set", modifiedDate.getTextContent().equals(""));
         assertXmlEquals("status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/public-status", "pending");
         Node createdDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/creation-date");
         assertNotNull(createdDate);
         assertFalse("created date is not set", createdDate.getTextContent().equals(""));
         assertXmlEquals("lock-status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/lock-status", "unlocked");
         assertXmlEquals("current version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties//number", "1");
         assertXmlEquals("current version status is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/status", "pending");
         // assertXmlEquals("current version valid-status is wrong",
         // getDocument(xml), "/item/properties/version/valid-status", "valid");
         assertXmlEquals("current version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/date", modifiedDate.getTextContent());
         assertXmlEquals("latest version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/number", "1");
         assertXmlEquals("latest version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/date", modifiedDate.getTextContent());
 
         assertNotNull(creatorId);
 
         Node componentCreationDate1 =
             selectSingleNode(EscidocAbstractTest.getDocument(xml),
                 "/item/components/component/properties/creation-date");
         assertNotNull(componentCreationDate1);
         assertFalse("component creation date is not set", componentCreationDate1.getTextContent().equals(""));
 
         delete(this.theItemId);
     }
 
     /**
      * Test successfully creating content item with one Content Component containing two md-records.
      */
     @Test
     public void testCreatingItemWithOneComponentWithTwoMdRecords() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_create_2_Component_Md-Records.xml");
         NodeList mdRecordsBeforeCreate = selectNodeList(xmlItem, "/item/components/component[1]/md-records/md-record");
         String xml = create(toString(xmlItem, false));
         assertXmlValidItem(xml);
         Document createdItem = EscidocAbstractTest.getDocument(xml);
         NodeList mdRecordsAfterCreate =
             selectNodeList(createdItem, "/item/components/component[1]/md-records/md-record");
         assertEquals("Number of md-records is wrong ", mdRecordsAfterCreate.getLength(), mdRecordsBeforeCreate
             .getLength());
 
     }
 
     /**
      * Test successfully creating content item with two Content Components.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi5() throws Exception {
 
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmiItemWithSEcondInlineContent = substitute(xmlItem, "/item/components/component[2]/content", "blablabla");
         Node xmiItemWithoutContentUrl =
             deleteAttribute(xmiItemWithSEcondInlineContent, "/item/components/component[2]/content",
                 XLINK_HREF_TEMPLATES);
         String itemWithSEcondInlineContent = toString(xmiItemWithoutContentUrl, true);
 
         String xml = create(itemWithSEcondInlineContent);
 
         assertXmlValidItem(xml);
         this.theItemId = getIdFromRootElement(xml);
 
         String creatorId = null;
         Vector<String> componentIds = new Vector<String>();
         Pattern PATTERN_OBJID_ATTRIBUTE = null;
         PATTERN_OBJID_ATTRIBUTE = Pattern.compile(".*\\/([^\"\\/]*)");
 
         NodeList components = selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/components/component/@href");
 
         for (int i = 0; i < components.getLength(); i++) {
             Node componentHrefNode = components.item(i);
             String componentHref = componentHrefNode.getTextContent();
 
             Matcher m2 = PATTERN_OBJID_ATTRIBUTE.matcher(componentHref);
             if (m2.find()) {
                 componentIds.add(m2.group(1));
             }
 
         }
 
         Node creatorIdNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@href");
         Matcher m3 = PATTERN_OBJID_ATTRIBUTE.matcher(creatorIdNode.getTextContent());
         if (m3.find()) {
             creatorId = m3.group(1);
         }
 
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/content/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(0) + "/content");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/@href", "/ir/item/"
             + theItemId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/md-records/@href ",
             "/ir/item/" + theItemId + "/md-records");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/md-records/md-record[1]/@href ", "/ir/item/" + theItemId + "/md-records/md-record/escidoc");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/properties/@href ",
             "/ir/item/" + theItemId + "/properties");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/components/@href",
             "/ir/item/" + theItemId + "/components");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(0));
         assertXmlEquals("creator value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/properties/created-by/@href", "/aa/user-account/" + creatorId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(1));
         assertXmlEquals("creator value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/properties/created-by/@href", "/aa/user-account/" + creatorId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/content/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(1) + "/content");
         Node itemTitle = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@title");
         assertNotNull(itemTitle);
         assertFalse("item title is not set", itemTitle.getTextContent().equals(""));
         Node creatorTitle =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@title");
         assertNotNull(creatorTitle.getTextContent());
         NodeList componentTitles =
             selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/components/component/@title");
         Vector<String> componentTitlesValues = new Vector<String>();
         for (int i = 0; i < componentTitles.getLength(); i++) {
             Node componentTitle = componentTitles.item(i);
             String title = componentTitle.getTextContent();
             componentTitlesValues.add(title);
         }
         assertFalse("component title is not set", componentTitlesValues.get(0).equals(""));
         assertXmlEquals("creator title value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/properties/created-by/@title", creatorTitle.getTextContent());
         NodeList componentContentTitles =
             selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/components/component/content/@title");
         Vector<String> componentContentTitlesValues = new Vector<String>();
         for (int i = 0; i < componentContentTitles.getLength(); i++) {
             Node componentContentTitle = componentContentTitles.item(i);
             String contentTitle = componentContentTitle.getTextContent();
             componentContentTitlesValues.add(contentTitle);
 
         }
         assertFalse("component content title is not set", componentContentTitlesValues.get(1).equals(""));
 
         assertFalse("component title is not set", componentTitlesValues.get(1).equals(""));
         assertXmlEquals("creator title value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/properties/created-by/@title", creatorTitle.getTextContent());
         assertFalse("component content title is not set", componentContentTitlesValues.get(1).equals(""));
 
         Node modifiedDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@last-modification-date");
         assertNotNull(modifiedDate);
         assertFalse("modified date is not set", modifiedDate.getTextContent().equals(""));
         assertXmlEquals("status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/public-status", "pending");
         Node createdDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/creation-date");
         assertNotNull(createdDate);
         assertFalse("created date is not set", createdDate.getTextContent().equals(""));
         assertXmlEquals("lock-status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/lock-status", "unlocked");
         assertXmlEquals("current version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/number", "1");
         assertXmlEquals("current version status is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/status", "pending");
         // assertXmlEquals("current version valid-status is wrong",
         // getDocument(xml), "/item/properties/version/valid-status", "valid");
         assertXmlEquals("current version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/date", modifiedDate.getTextContent());
         assertXmlEquals("latest version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/number", "1");
         assertXmlEquals("latest version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/date", modifiedDate.getTextContent());
 
         assertNotNull(creatorId);
 
         Node componentCreationDate1 =
             selectSingleNode(EscidocAbstractTest.getDocument(xml),
                 "/item/components/component[1]/properties/creation-date");
         assertNotNull(componentCreationDate1);
         assertFalse("component creation date is not set", componentCreationDate1.getTextContent().equals(""));
 
         Node componentCreationDate2 =
             selectSingleNode(EscidocAbstractTest.getDocument(xml),
                 "/item/components/component[2]/properties/creation-date");
         assertNotNull(componentCreationDate2);
         assertFalse("component creation date is not set", componentCreationDate2.getTextContent().equals(""));
 
         delete(theItemId);
     }
 
     /**
      * Test successfully creating content item with one Content Component.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi8() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutFirstComponent = deleteElement(xmlItem, "/item/components/component[1]");
         String itemWithoutFirstComponent = toString(xmlItemWithoutFirstComponent, true);
         String xml = create(itemWithoutFirstComponent);
         assertXmlValidItem(xml);
         this.theItemId = getIdFromRootElement(xml);
 
         String componentId = null;
         String creatorId = null;
         Pattern PATTERN_OBJID_ATTRIBUTE = null;
         PATTERN_OBJID_ATTRIBUTE = Pattern.compile(".*\\/([^\"\\/]*)");
 
         Node itemObjiId = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@href");
 
         Matcher m1 = PATTERN_OBJID_ATTRIBUTE.matcher(itemObjiId.getTextContent());
         if (m1.find()) {
             this.theItemId = m1.group(1);
         }
 
         Node componentObjiId =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/components/component/@href");
 
         assertNotNull("Component href missing", componentObjiId);
         assertNotNull("Component href value missing", componentObjiId.getTextContent());
 
         Matcher m2 = PATTERN_OBJID_ATTRIBUTE.matcher(componentObjiId.getTextContent());
         if (m2.find()) {
             componentId = m2.group(1);
         }
 
         Node creatorIdNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@href");
         Matcher m3 = PATTERN_OBJID_ATTRIBUTE.matcher(creatorIdNode.getTextContent());
         if (m3.find()) {
             creatorId = m3.group(1);
         }
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/@href", "/ir/item/"
             + theItemId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/md-records/@href ",
             "/ir/item/" + theItemId + "/md-records");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/md-records/md-record[1]/@href ", "/ir/item/" + theItemId + "/md-records/md-record/escidoc");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/properties/@href ",
             "/ir/item/" + theItemId + "/properties");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/components/@href",
             "/ir/item/" + theItemId + "/components");
 
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component/@href", "/ir/item/" + theItemId + "/components/component/" + componentId);
         assertXmlEquals("creator value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component/properties/created-by/@href", "/aa/user-account/" + creatorId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/content/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentId + "/content");
         Node itemTitle = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@title");
         assertNotNull(itemTitle);
         assertFalse("item title is not set", itemTitle.getTextContent().equals(""));
         Node creatorTitle =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@title");
         assertNotNull(creatorTitle.getTextContent());
 
         Node componentTitleNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/components/component/@title");
         String componentTitle = componentTitleNode.getTextContent();
 
         assertFalse("component title is not set", componentTitle.equals(""));
         assertXmlEquals("creator title value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component/properties/created-by/@title", creatorTitle.getTextContent());
         Node contentTitleNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/components/component/content/@title");
         assertNotNull(contentTitleNode);
 
         assertFalse("component content title is not set", contentTitleNode.getTextContent().equals(""));
 
         Node modifiedDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@last-modification-date");
         assertNotNull(modifiedDate);
         assertFalse("modified date is not set", modifiedDate.getTextContent().equals(""));
         assertXmlEquals("status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/public-status", "pending");
         Node createdDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/creation-date");
         assertNotNull(createdDate);
         assertFalse("created date is not set", createdDate.getTextContent().equals(""));
         assertXmlEquals("lock-status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/lock-status", "unlocked");
         assertXmlEquals("current version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/number", "1");
         assertXmlEquals("current version status is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/status", "pending");
         // assertXmlEquals("current version valid-status is wrong",
         // getDocument(xml), "/item/properties/version/valid-status", "valid");
         assertXmlEquals("current version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/date", modifiedDate.getTextContent());
         assertXmlEquals("latest version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/number", "1");
         assertXmlEquals("latest version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/date", modifiedDate.getTextContent());
 
         assertNotNull(creatorId);
         Node componentCreationDate1 =
             selectSingleNode(EscidocAbstractTest.getDocument(xml),
                 "/item/components/component/properties/creation-date");
         assertNotNull(componentCreationDate1);
         assertFalse("component creation date is not set", componentCreationDate1.getTextContent().equals(""));
 
         delete(theItemId);
     }
 
     /**
      * Test successfully creating content item with two Content Components.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi9() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_2_components.xml");
 
         String xml = create(toString(xmlItem, false));
         assertXmlValidItem(xml);
         this.theItemId = getIdFromRootElement(xml);
 
         String creatorId = null;
         Vector<String> componentIds = new Vector<String>();
         Pattern PATTERN_OBJID_ATTRIBUTE = null;
         PATTERN_OBJID_ATTRIBUTE = Pattern.compile(".*\\/([^\"\\/]*)");
 
         NodeList components = selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/components/component/@href");
 
         for (int i = 0; i < components.getLength(); i++) {
             Node componentHrefNode = components.item(i);
             String componentHref = componentHrefNode.getTextContent();
 
             Matcher m2 = PATTERN_OBJID_ATTRIBUTE.matcher(componentHref);
             if (m2.find()) {
                 componentIds.add(m2.group(1));
             }
 
         }
 
         Node creatorIdNode =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@href");
         Matcher m3 = PATTERN_OBJID_ATTRIBUTE.matcher(creatorIdNode.getTextContent());
         if (m3.find()) {
             creatorId = m3.group(1);
         }
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/@href", "/ir/item/"
             + theItemId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/md-records/@href ",
             "/ir/item/" + theItemId + "/md-records");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/md-records/md-record[1]/@href ", "/ir/item/" + theItemId + "/md-records/md-record/escidoc");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/properties/@href ",
             "/ir/item/" + theItemId + "/properties");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml), "/item/components/@href",
             "/ir/item/" + theItemId + "/components");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(0));
         assertXmlEquals("creator value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/properties/created-by/@href", "/aa/user-account/" + creatorId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/content/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(0) + "/content");
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(1));
         assertXmlEquals("creator value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/properties/created-by/@href", "/aa/user-account/" + creatorId);
         assertXmlEquals("href value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/content/@href", "/ir/item/" + theItemId + "/components/component/"
                 + componentIds.get(1) + "/content");
         Node itemTitle = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@title");
         assertNotNull(itemTitle);
         assertFalse("item title is not set", itemTitle.getTextContent().equals(""));
         Node creatorTitle =
             selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/created-by/@title");
         assertNotNull(creatorTitle.getTextContent());
 
         NodeList componentTitles =
             selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/components/component/@title");
         Vector<String> componentTitlesValues = new Vector<String>();
         for (int i = 0; i < componentTitles.getLength(); i++) {
             Node componentTitle = componentTitles.item(i);
             String title = componentTitle.getTextContent();
             componentTitlesValues.add(title);
         }
 
         assertFalse("component title is not set", componentTitlesValues.get(0).equals(""));
         assertXmlEquals("creator title value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[1]/properties/created-by/@title", creatorTitle.getTextContent());
         NodeList componentContentTitles =
             selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/components/component/content/@title");
         Vector<String> componentContentTitlesValues = new Vector<String>();
         for (int i = 0; i < componentContentTitles.getLength(); i++) {
             Node componentContentTitle = componentContentTitles.item(i);
             String contentTitle = componentContentTitle.getTextContent();
             componentContentTitlesValues.add(contentTitle);
 
         }
         assertFalse("component content title is not set", componentContentTitlesValues.get(1).equals(""));
 
         assertFalse("component title is not set", componentTitlesValues.get(1).equals(""));
         assertXmlEquals("creator title value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/components/component[2]/properties/created-by/@title", creatorTitle.getTextContent());
 
         assertFalse("component content title is not set", componentContentTitlesValues.get(1).equals(""));
         Node modifiedDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/@last-modification-date");
         assertNotNull(modifiedDate);
         assertFalse("modified date is not set", modifiedDate.getTextContent().equals(""));
         assertXmlEquals("status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/public-status", "pending");
         Node createdDate = selectSingleNode(EscidocAbstractTest.getDocument(xml), "/item/properties/creation-date");
         assertNotNull(createdDate);
         assertFalse("created date is not set", createdDate.getTextContent().equals(""));
         assertXmlEquals("lock-status value is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/lock-status", "unlocked");
         assertXmlEquals("current version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/number", "1");
         assertXmlEquals("current version status is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/status", "pending");
         // assertXmlEquals("current version valid-status is wrong",
         // getDocument(xml), "/item/properties/version/valid-status", "valid");
         assertXmlEquals("current version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/version/date", modifiedDate.getTextContent());
         assertXmlEquals("latest version number is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/number", "1");
         assertXmlEquals("latest version date is wrong", EscidocAbstractTest.getDocument(xml),
             "/item/properties/latest-version/date", modifiedDate.getTextContent());
 
         assertNotNull(creatorId);
 
         Node componentCreationDate1 =
             selectSingleNode(EscidocAbstractTest.getDocument(xml),
                 "/item/components/component[1]/properties/creation-date");
         assertNotNull(componentCreationDate1);
         assertFalse("component creation date is not set", componentCreationDate1.getTextContent().equals(""));
 
         Node componentCreationDate2 =
             selectSingleNode(EscidocAbstractTest.getDocument(xml),
                 "/item/components/component[2]/properties/creation-date");
         assertNotNull(componentCreationDate2);
         assertFalse("component creation date is not set", componentCreationDate2.getTextContent().equals(""));
 
         delete(theItemId);
     }
 
     /**
      * Test successfully creating content item with two Content Components, The first component does not contain
      * md-records.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testCreateItemWithFirstComponendWithoutMdRecords() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_2_components.xml");
         Node itemWith1ComponentWithoutMdRecords = deleteElement(xmlItem, "item/components/component[1]/md-records");
         Node itemWithContentin1ComponentWithExternalUrl =
             substitute(itemWith1ComponentWithoutMdRecords, "item/components/component[1]/content/@storage",
                 "external-url");
 
         String xml = create(toString(itemWith1ComponentWithoutMdRecords, false));
         assertXmlValidItem(xml);
         this.theItemId = getIdFromRootElement(xml);
         Document createdItem = getDocument(xml);
         Node firstComponentContent = selectSingleNode(createdItem, "item/components/component[1]/content/@storage");
         if (firstComponentContent.getNodeValue().equals("external-url")) {
             Node mdRecords = selectSingleNode(createdItem, "item/components/component[1]/md-records");
             assertNull("the component may not have md-records", mdRecords);
         }
         else {
             Node mdRecords = selectSingleNode(createdItem, "item/components/component[2]/md-records");
             assertNull("the component may not have md-records", mdRecords);
         }
 
         // delete(itemId);
     }
 
     /**
      * Test declining creating content item and Content Component with wrong href.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = FileNotFoundException.class)
     public void testOMCi12a() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutFirstComponent = deleteElement(xmlItem, "/item/components/component[1]");
         Node xmiItemWithWrongtHrefContent =
             substitute(xmlItemWithoutFirstComponent, "/item/components/component/content/@href", "http://localhost/bla");
 
         String itemWithWrongtHrefContent = toString(xmiItemWithWrongtHrefContent, true);
         create(itemWithWrongtHrefContent);
     }
 
     /**
      * Test declining creating content item without content components - item xml with set read only element.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi12e() throws Exception {
         String newDate = "1970-01-01T01:00:00.000Z";
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Element creator = xmlItem.createElementNS(Constants.NS_COMMON_PROPERTIES, "prop:creation-date");
         creator.setTextContent(newDate);
         NodeList propertiesList = xmlItem.getElementsByTagName("escidocItem:properties");
         Node properties = null;
         for (int i = 0; i < propertiesList.getLength(); i++) {
             properties = propertiesList.item(i);
 
         }
         NodeList ctsList = xmlItem.getElementsByTagName("srel:context");
         Node cts = null;
         for (int i = 0; i < ctsList.getLength(); i++) {
             cts = ctsList.item(i);
 
         }
         properties.insertBefore(creator, cts);
 
         String itemWithCreator = toString(xmlItem, true);
 
         Class<?> ec = ReadonlyElementViolationException.class;
         String xml = null;
         try {
             xml = create(itemWithCreator);
             // read only elements are ignored now
             // failMissingException(ec);
         }
         catch (final Exception e) {
             // read only elements are ignored now
             fail("Exception on update with read-only element set.");
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
         // read only elements are ignored now
         assertNotEquals("Read-only element should not be set ", selectSingleNode(EscidocAbstractTest.getDocument(xml),
             "/item/properties/creation-date").getTextContent(), newDate);
     }
 
     /**
      * Test successfully creating item with two relations.
      */
     @Test
     public void testRelations() throws Exception {
         String itemXml1 =
             create(EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_create.xml"));
         String itemXml2 =
             create(EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_create.xml"));
 
         String createdItemId1 = getIdFromRootElement(itemXml1);
         String createdItemId2 = getIdFromRootElement(itemXml2);
 
         String href1 = "/ir/item/" + createdItemId1;
         String href2 = "/ir/item/" + createdItemId2;
         String itemForCreateWithRelationsXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_createWithRelations.xml");
 
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID1##", createdItemId1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID2##", createdItemId2);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF1##", href1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF2##", href2);
         Document itemForCreateWithRelations = EscidocAbstractTest.getDocument(itemForCreateWithRelationsXml);
         Node xmlItemWithoutComponents = deleteElement(itemForCreateWithRelations, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
         NodeList relations =
             selectNodeList(EscidocAbstractTest.getDocument(itemWithoutComponents), "/item/relations/relation");
 
         String xml = create(itemWithoutComponents);
         NodeList relationsAfterCreate =
             selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/relations/relation");
 
         assertXmlValidItem(xml);
         assertEquals("Number of relations is wrong ", relations.getLength(), relationsAfterCreate.getLength());
 
         String createdItemId = getIdFromRootElement(xml);
         this.theItemId = createdItemId;
         xml = retrieve(createdItemId);
         relationsAfterCreate = selectNodeList(EscidocAbstractTest.getDocument(xml), "/item/relations/relation");
 
         assertXmlValidItem(xml);
         assertEquals("Number of relations is wrong ", relations.getLength(), relationsAfterCreate.getLength());
 
     }
 
     /**
      * Test successfully creating item with two relations. The Item is created from template. Each Item for content
      * relation is created during test.
      */
     @Test
     public void testRelations02() throws Exception {
 
         String itemTemplate = getExampleTemplate("item-minimal-for-create-01.xml");
 
         String itemXml1 = create(itemTemplate);
         String itemXml2 = create(itemTemplate);
 
         String itemId1 = getIdFromRootElement(itemXml1);
         String itemId2 = getIdFromRootElement(itemXml2);
 
         // make hrefs from objid
         itemId1 = "/ir/item/" + itemId1;
         itemId2 = "/ir/item/" + itemId2;
 
         String itemRelationsXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_relation_01.xml");
 
         itemRelationsXml = itemRelationsXml.replaceAll("###ITEM_ID1###", itemId1);
         itemRelationsXml = itemRelationsXml.replaceAll("###ITEM_ID2###", itemId2);
 
         String xmlCreated = create(itemRelationsXml);
 
         assertXmlValidItem(xmlCreated);
 
         NodeList relationsAfterCreate =
             selectNodeList(EscidocAbstractTest.getDocument(xmlCreated), "/item/relations/relation");
 
         assertEquals("Number of relations is wrong ", 2, relationsAfterCreate.getLength());
 
         String createdItemId = getIdFromRootElement(xmlCreated);
 
         String xmlRetrieved = retrieve(createdItemId);
         assertXmlValidItem(xmlRetrieved);
 
         NodeList relationsAfterRetrieve =
             selectNodeList(EscidocAbstractTest.getDocument(xmlRetrieved), "/item/relations/relation");
 
         assertEquals("Number of relations differs between create and retieve ", relationsAfterRetrieve.getLength(),
             relationsAfterCreate.getLength());
 
     }
 
     /**
      * Test declining creating item with relations, whose targets references non existing resources.
      */
     @Test(expected = ReferencedResourceNotFoundException.class)
     public void testRelationsWithWrongTarget() throws Exception {
 
         String createdItemId1 = "bla1";
         String createdItemId2 = "bla2";
 
         String href1 = "/ir/item/" + createdItemId1;
         String href2 = "/ir/item/" + createdItemId2;
         String itemForCreateWithRelationsXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_createWithRelations.xml");
 
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID1##", createdItemId1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID2##", createdItemId2);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF1##", href1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF2##", href2);
         Document itemForCreateWithRelations = EscidocAbstractTest.getDocument(itemForCreateWithRelationsXml);
         Node xmlItemWithoutComponents = deleteElement(itemForCreateWithRelations, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         create(itemWithoutComponents);
         // if not fail: No exception occurred on item created with relations, which references non existing targets.
     }
 
     /**
      * Tess declining creating item with relations, whose target ids containing a version number.
      */
     @Test
     public void testRelationsWithTargetContainingVersionNumber() throws Exception {
 
         String createdItemId1 = "escidoc:123:2";
         String createdItemId2 = "escidoc:123:3";
 
         String href1 = "/ir/item/" + createdItemId1;
         String href2 = "/ir/item/" + createdItemId2;
         String itemForCreateWithRelationsXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_createWithRelations.xml");
 
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID1##", createdItemId1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID2##", createdItemId2);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF1##", href1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF2##", href2);
         Document itemForCreateWithRelations = EscidocAbstractTest.getDocument(itemForCreateWithRelationsXml);
         Node xmlItemWithoutComponents = deleteElement(itemForCreateWithRelations, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         try {
             create(itemWithoutComponents);
             fail("No exception occured on item crate with relations, which "
                 + " target ids containing a version number.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("InvalidContentException expected.", InvalidContentException.class,
                 e);
         }
     }
 
     /**
      * Test declining creating item with relations with non existing predicate.
      */
     @Test
     public void testRelationsWithWrongPredicate() throws Exception {
 
         String itemXml1 =
             create(EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_create.xml"));
         String itemXml2 =
             create(EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_create.xml"));
 
         String createdItemId1 = null;
         String createdItemId2 = null;
 
         Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("href=\"/ir/item/([^\"]*)\"");
 
         Matcher m1 = PATTERN_OBJID_ATTRIBUTE.matcher(itemXml1);
         if (m1.find()) {
             createdItemId1 = m1.group(1);
         }
         Matcher m2 = PATTERN_OBJID_ATTRIBUTE.matcher(itemXml2);
         if (m2.find()) {
             createdItemId2 = m2.group(1);
         }
 
         String href1 = "/ir/item/" + createdItemId1;
         String href2 = "/ir/item/" + createdItemId2;
         String itemForCreateWithRelationsXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_198_for_createWithRelations.xml");
 
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID1##", createdItemId1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_ID2##", createdItemId2);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF1##", href1);
         itemForCreateWithRelationsXml = itemForCreateWithRelationsXml.replaceAll("##ITEM_HREF2##", href2);
         Document itemForCreateWithRelations = EscidocAbstractTest.getDocument(itemForCreateWithRelationsXml);
         Node xmlItemWithoutComponents = deleteElement(itemForCreateWithRelations, "/item/components");
 
         Node relationPredicate = selectSingleNode(xmlItemWithoutComponents, "/item/relations/relation[1]/@predicate");
         relationPredicate.setNodeValue("http://www.bla.de#bla");
 
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
         try {
             create(itemWithoutComponents);
             fail("No exception occured on item create with relations, which " + " references non existing predicate.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("RelationPredicateNotFoundException expected.",
                 RelationPredicateNotFoundException.class, e);
         }
 
     }
 
     @Test
     public void testComponentsInFirstVersion() throws Exception {
         // create an item with components
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         String itemXml = toString(xmlItem, true);
         final String createdItem = create(itemXml);
         String itemId = getIdFromRootElement(createdItem);
 
         // make second version
         xmlItem = EscidocAbstractTest.getDocument(createdItem);
         selectSingleNode(xmlItem, "/item/properties/content-model-specific").appendChild(
             xmlItem.createElement("nischt"));
         itemXml = toString(xmlItem, true);
         final String updatedItem = update(itemId, itemXml);
 
         // retrieve first version
         final String firstItem = retrieve(itemId + ":1");
         xmlItem = EscidocAbstractTest.getDocument(firstItem);
         selectSingleNodeAsserted(xmlItem, "/item/components");
         selectSingleNodeAsserted(xmlItem, "/item/components/component");
         selectSingleNodeAsserted(xmlItem, "/item/components/component/properties");
         selectSingleNodeAsserted(xmlItem, "/item/components/component/content");
         assertXmlValidItem(firstItem);
 
         delete(itemId);
     }
 
     @Test
     public void testCreateFromRetrieve() throws Exception {
 
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         deleteElement(xmlItem, "/item/components/component[1]");
 
         String itemId = getObjidValue(create(toString(xmlItem, true)));
         String createdItem = retrieve(itemId);
         Document createdItemDoc = EscidocAbstractTest.getDocument(createdItem);
         createdItemDoc =
             (Document) substitute(createdItemDoc, "/item/components/component/content/@href", getBaseUrl());
         createdItem = toString(createdItemDoc, true);
         final String createdXml = create(createdItem);
 
         assertXmlValidItem(createdXml);
     }
 
     /**
      * Test successfully creatig of an item with 2 md-records.
      * 
      * @throws Exception
      *             Thrown if anything failed.
      */
     @Test
     public void testCreateItemWith2Mdrecords() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_2_md_records_for_create.xml");
         NodeList mdrecords = selectNodeList(xmlItem, "/item/md-records/md-record");
         Node xmlItemWithoutComponents = deleteElement(xmlItem, "/item/components");
 
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
         final String createdXml = create(itemWithoutComponents);
         assertXmlValidItem(createdXml);
         final Document createdDocument = EscidocAbstractTest.getDocument(createdXml);
 
         NodeList mdrecordsAfterCreate = selectNodeList(createdDocument, "/item/md-records/md-record");
         assertEquals(mdrecords.getLength(), mdrecordsAfterCreate.getLength());
 
     }
 
     /**
      * Test decleaning creatig of an item with 2 escidoc md-records.
      * 
      * @throws Exception
      *             Thrown if anything failed.
      */
     @Test
     public void testCreateItemWith2EscidocMdrecords() throws Exception {
         String xmlItem =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_2_escidoc_md_records_for_create.xml");
         try {
             create(xmlItem);
             fail("InvalidContentException expected if cretaing item without" + " Md-records.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidContentException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
     }
 
     /**
      * Test decleaning create an item without md-records.
      * 
      * @throws Exception
      *             Thrown if anything failed.
      */
     @Test
     public void testCreateItemWithoutMdrecords() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest",
                 "escidoc_item_2_md_records_for_create.xml");
 
         Node xmlItemWithoutMdRecords = deleteElement(xmlItem, "/item/md-records");
 
         String itemWithoutMdRecords = toString(xmlItemWithoutMdRecords, true);
 
         try {
             create(itemWithoutMdRecords);
             fail("MissingMdRecordException expected if cretaing item without" + " Md-records.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMdRecordException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
     }
 
     /**
      * Test deleting component via deleteComponent method.
      * 
      * @throws Exception
      *             Thrown if anything failed.
      */
     @Test
     public void testDeleteComponent01() throws Exception {
 
         String itemXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         String createdItem = create(itemXml);
         Document itemDoc = getDocument(createdItem);
 
         String itemId = getObjidValue(itemDoc);
 
         Node componentObjiId = selectSingleNode(itemDoc, "/item/components/component/@href");
         String componentHref = componentObjiId.getTextContent();
         String componentId = getObjidFromHref(componentHref);
         ;
 
         deleteComponent(itemId, componentId);
 
         // check if the component is realy deleted
         itemXml = retrieve(itemId);
         itemDoc = getDocument(itemXml);
 
         assertXmlNotExists("Component not deleted", itemDoc, "/item/components/component[@href='" + componentHref
             + "']");
     }
 
     /**
      * Test delete of not existing component via deleteComponent method.
      * 
      * @throws Exception
      *             Thrown if anything failed.
      */
     @Test
     public void testDeleteComponent02() throws Exception {
 
         String itemXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         String createdItem = create(itemXml);
         Document itemDoc = getDocument(createdItem);
 
         String itemId = getObjidValue(itemDoc);
 
         try {
             deleteComponent(itemId, itemId);
             fail("ComponentNotFoundException expected if deleting not" + " existing Component.");
         }
         catch (final Exception e) {
             Class<?> ec = ComponentNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     @Test
     public void testExternalUrlWithParam() throws Exception {
 
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         Node xmlItemDoc = deleteElement(xmlItem, "/item/components/component[not(@href) or position() = 1]");
         // set URL
         String urlWithParam = getBaseUrl() + "?p1=v1&p2=v2";
         xmlItemDoc = substitute(xmlItemDoc, "/item/components/component/content/@href", urlWithParam);
         // set storage
         String storage = "external-url";
         xmlItemDoc = substitute(xmlItemDoc, "/item/components/component/content/@storage", storage);
 
         String xml = toString(xmlItemDoc, true);
 
         final String createdXml = create(xml);
         assertXmlValidItem(createdXml);
 
         final Document createdDocument = EscidocAbstractTest.getDocument(createdXml);
 
         // check URL
         assertEquals(urlWithParam, selectSingleNode(createdDocument, "/item/components/component/content/@href")
             .getNodeValue());
 
         this.theItemId = getIdFromRootElement(createdXml);
         delete(this.theItemId);
 
     }
 
     /**
      * Test if the right Exception is thrown if multiple Metadata Records with same name attribute are created.
      */
     @Test
     public void testMDRecordUniqueNames01() throws Exception {
 
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
 
         // add new md-record
         String xmlMdRecord = EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "md-record.xml");
         Node mdRecord = selectSingleNode(EscidocAbstractTest.getDocument(xmlMdRecord), "/md-record");
 
         xmlItem.importNode(mdRecord, true);
 
         Node mdRecords = selectSingleNode(xmlItem, "/item/md-records");
         mdRecords.appendChild(xmlItem.adoptNode(mdRecord));
         xmlItem.normalize();
 
         NodeList mdRe = selectNodeList(xmlItem, "/item/md-records/md-record[@name='escidoc']");
         assertTrue("Not enough md-record with name 'escidoc' for test. ", mdRe.getLength() > 1);
 
         try {
             create(toString(xmlItem, true));
             fail("Missing Exception if Item contains more than one md-record" + " with name 'escidoc'.");
         }
         catch (final Exception e) {
             // FIXME correct exception? its a value to much! JavaDoc: a
             // mandatory attribute value is not set
             Class<?> ec = InvalidContentException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
 
         }
     }
 
     /**
      * Test creating Item with null parameter.
      * 
      * @throws Exception
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testCreateWithNullInput() throws Exception {
 
         create(null);
     }
 
     /**
      * Test declining creation of Item with providing reference to context with invalid href (substring context not in
      * href).
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = ContextNotFoundException.class)
     public void testOMCi13_1_rest() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
 
         String href =
             selectSingleNodeAsserted(toBeCreatedDocument, ItemXpathsProvider.XPATH_ITEM_CONTEXT_XLINK_HREF)
                 .getTextContent();
         href = href.replaceFirst(Constants.CONTEXT_BASE_URI, Constants.ORGANIZATIONAL_UNIT_BASE_URI);
         substitute(toBeCreatedDocument, ItemXpathsProvider.XPATH_ITEM_CONTEXT_XLINK_HREF, href);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         create(toBeCreatedXml);
         // if fail: "Creating item with invalid object href not declined.
     }
 
     /**
      * Test declining creation of Item with providing reference to content-model with invalid href (substring
      * content-model not in href).
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = ContentModelNotFoundException.class)
     public void testOMCi13_2_rest() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
 
         String href =
             selectSingleNodeAsserted(toBeCreatedDocument, ItemXpathsProvider.XPATH_ITEM_CONTENT_TYPE_XLINK_HREF)
                 .getTextContent();
         href = href.replaceFirst(Constants.CONTENT_MODEL_BASE_URI, Constants.ORGANIZATIONAL_UNIT_BASE_URI);
         substitute(toBeCreatedDocument, ItemXpathsProvider.XPATH_ITEM_CONTENT_TYPE_XLINK_HREF, href);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         create(toBeCreatedXml);
         // if fail: Creating item with invalid object href not declined.
     }
 
 }
