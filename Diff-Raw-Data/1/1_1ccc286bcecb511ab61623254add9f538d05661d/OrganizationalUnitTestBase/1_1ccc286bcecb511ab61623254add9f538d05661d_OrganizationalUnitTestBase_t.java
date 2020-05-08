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
 package de.escidoc.core.test.oum.organizationalunit;
 
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.Constants;
 import de.escidoc.core.test.oum.OumTestBase;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.apache.xerces.dom.ElementImpl;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 /**
  * Test the implementation of the organizational unit resource.
  *
  * @author Michael Schneider
  */
 public class OrganizationalUnitTestBase extends OumTestBase {
 
     public static final String XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_MEMBER =
         XPATH_SRW_RESPONSE_RECORD + "/recordData/search-result-record";
 
     public static final String XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_ORGANIZATIONAL_UNIT =
         XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_MEMBER + "/" + NAME_ORGANIZATIONAL_UNIT;
 
     private static String DEFAULT_OU_FOR_CREATE = "escidoc_ou_create.xml";
 
     protected static String ouTop1Id = null;
 
     protected static String ouTop1Xml = null;
 
     protected static String ouTop2Id = null;
 
     protected static String ouChild1ParentId = null;
 
     protected static String ouChild1ParentName;
 
     protected static String ouChild2ParentsId = null;
 
     /**
      * Successfully creates an organizational unit.
      *
      * @param templateName The name of the template to use.
      * @return Returns the XML representation of the created organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String createSuccessfully(final String templateName) throws Exception {
 
         final Document toBeCreatedDocument = getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, templateName);
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         final String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (final Exception e) {
             failException("Create of OU failed. ", e);
         }
         return createdXml;
     }
 
     /**
      * Successfully creates a specified number of organizational units.
      *
      * @param templateName The name of the template to use.
      * @param number       The number of organizational units that shall be created.
      * @return Returns a string array containing the object ids of the created organizational units and the titles, e.g.
      *         [id1, id2, title1, title2].
      * @throws Exception Thrown if anything fails.
      */
     protected String[] createSuccessfully(final String templateName, final int number) throws Exception {
 
         String[] ret = new String[number * 2];
         for (int i = 0; i < number; i++) {
             final String createdXml = createSuccessfully(templateName);
             final Document createdDocument = getDocument(createdXml);
             ret[i] = getObjidValue(createdDocument);
             ret[i + number] = getTitleValue(createdDocument);
         }
         return ret;
     }
 
     /**
      * Successfully creates a child organizational unit with the specified parent organizational units.
      *
      * @param templateName The name of the template to use.
      * @param parentIds    The ids of the parent organizational units.
      * @return Returns the Xml representation of the created child organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     protected String createSuccessfullyChild(final String templateName, final String[] parentIds) throws Exception {
 
         final Document childDocument = getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, templateName);
         setUniqueValue(childDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         final int length = parentIds.length;
         final String[] parentValues = new String[length * 2];
         for (int i = 0; i < length; i++) {
             parentValues[i] = parentIds[i];
             parentValues[i + length] = null;
         }
         insertParentsElement(childDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
         final String toBeCreatedXml = toString(childDocument, true);
         return create(toBeCreatedXml);
     }
 
     /**
      * Tries to create a child organizational unit with the specified parent organizational units.
      *
      * @param handle                 The user-handle.
      * @param parentIds              The ids of the parent organizational units.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the created child organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestCreate(final String handle, final String[] parentIds, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = createSuccessfullyChild(DEFAULT_OU_FOR_CREATE, parentIds);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Creates and insert the parent ous element at the specified position in the provided document. This element
      * contains references to the specified organizational units.
      *
      * @param document         The document for that the element shall be created.
      * @param xpathBefore      The xpath after that the parent-ous element shall be inserted in the document.
      * @param parentValues     The ids and titles of the parent organizational units, e.g. [id1, id2, title1, title2]
      * @param withRestReadOnly Flag indicating if the parent-ous element shall contain the REST specific read only
      *                         attributes.
      * @return Returns the created <code>Element</code> object.
      * @throws Exception Thrown if anything fails.
      */
     public Node insertParentsElement(
         final Document document, final String xpathBefore, final String[] parentValues, final boolean withRestReadOnly)
         throws Exception {
 
         String prefix = determineOrganizationalUnitNamespacePrefix(document);
         String xlinkPrefix = determineXlinkNamespacePrefix(document);
         String srelPrefix = determineSrelNamespacePrefix(document);
         String parentsPrefix = lookupPrefixForNamespace(document, Constants.NS_COMMON_PARENTS);
 
         Element parents = null;
         final int numberParents = parentValues.length / 2;
         if (numberParents > 0) {
             if (!withRestReadOnly) {
                 parents = createElementNode(document, Constants.NS_COMMON_PARENTS, parentsPrefix, NAME_PARENTS, null);
             }
             else {
                 parents =
                     createReferencingElementNode(document, Constants.NS_COMMON_PARENTS, parentsPrefix, NAME_PARENTS,
                         xlinkPrefix, "Some Title", "/some/href/some:id", true);
             }
             addAfter(document, xpathBefore, parents);
             for (int i = numberParents - 1; i >= 0; i--) {
                 final String parentId = parentValues[i];
                 final String parentTitle = parentValues[i + numberParents];
                 final Element parentRef =
                     createReferencingElementNode(document, Constants.NS_COMMON_SREL, srelPrefix, NAME_PARENT,
                         xlinkPrefix, parentTitle, "/oum/organizational-unit/" + parentId, withRestReadOnly);
                 parents.appendChild(parentRef);
             }
         }
         return parents;
     }
 
     private String lookupNamespacePrefix(String namespaceURI, Element el) {
         // REVISIT: if no prefix is available is it null or empty string, or
         //          could be both?
         String prefix = null;
 
         if (el.hasAttributes()) {
             NamedNodeMap map = el.getAttributes();
             int length = map.getLength();
             for (int i = 0; i < length; i++) {
                 Node attr = map.item(i);
                 String attrPrefix = attr.getPrefix();
                 String value = attr.getNodeValue();
                 String namespace = attr.getNamespaceURI();
                 if (namespace != null && namespace.equals("http://www.w3.org/2000/xmlns/")) {
                     // DOM Level 2 nodes
                     if (((attr.getNodeName().equals("xmlns")) || (attrPrefix != null && attrPrefix.equals("xmlns"))
                         && value.equals(namespaceURI))) {
 
                         String localname = attr.getLocalName();
                         String foundNamespace = el.lookupNamespaceURI(localname);
                         if (foundNamespace != null && foundNamespace.equals(namespaceURI)) {
                             return localname;
                         }
                     }
 
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Creates an organizational unit hierarchy. Four organizational units are created:<br> <ul> <li>Two top level Ous
      * ouTop1 and ouTop2</li> <li>One mid level Ou ouChild1Parent as child of ouTop1</li> <li>One low level Ou
      * ouChild2Parents as child of ouTop2 and ouChild1Parent</li> </ul>
      *
      * @throws Exception Thrown if anything fails.
      */
     protected void createOuHierarchie() throws Exception {
 
         ouTop1Xml = createSuccessfully("escidoc_ou_create.xml");
         ouTop1Id = getObjidValue(getDocument(ouTop1Xml));
 
         final Document child1Document =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         ouChild1ParentName = setUniqueValue(child1Document, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(child1Document, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, new String[] { ouTop1Id, null },
             false);
         String child1CreatedXml = create(toString(child1Document, true));
         ouChild1ParentId = getObjidValue(getDocument(child1CreatedXml));
 
         final String ouXmlTop2 = createSuccessfully("escidoc_ou_create.xml");
         ouTop2Id = getObjidValue(getDocument(ouXmlTop2));
 
         final Document child2Document =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(child2Document, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(child2Document, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, new String[] { ouChild1ParentId,
             ouTop2Id }, false);
         final String ouChild2ParentsXml = create(toString(child2Document, true));
         ouChild2ParentsId = getObjidValue(getDocument(ouChild2ParentsXml));
     }
 
     /**
      * Determines the namespace prefix of the organizational unit used in the document.
      *
      * @param document The document to look up the namespace in. This must contain an organizational unit.
      * @return Returns the namespace prefix of the root element of the document
      * @throws Exception If anything fails.
      */
     private String determineOrganizationalUnitNamespacePrefix(final Document document) throws Exception {
 
         Node root = selectSingleNode(document, XPATH_ORGANIZATIONAL_UNIT);
         if (root == null) {
             root = selectSingleNode(document, XPATH_ORGANIZATION_MD_RECORDS);
         }
         if (root == null) {
             root = selectSingleNode(document, XPATH_PARENTS);
         }
         return determinePrefix(root);
     }
 
     /**
      * Determines the namespace prefix of the structural relations used in the document.
      *
      * @param document The document to look up the namespace in.
      * @return Returns the namespace prefix of the structural relations element of the document
      * @throws Exception If anything fails.
      */
     protected String determineSrelNamespacePrefix(final Document document) throws Exception {
 
         Node root = selectSingleNode(document, XPATH_ORGANIZATIONAL_UNIT_CREATED_BY);
         if (root != null) {
             return determinePrefix(root);
         }
         else {
             NodeList list = document.getChildNodes();
             for (int i = 0; i < list.getLength(); i++) {
                 Node n = list.item(i);
                 NamedNodeMap attributes = n.getAttributes();
                 if (attributes != null) {
                     for (int j = 0; j < attributes.getLength(); j++) {
                         Node no = attributes.item(j);
                         if (no.getNodeValue().equals(Constants.NS_COMMON_SREL)) {
                             return no.getNodeName().replaceAll(".*?:(.*)", "$1");
                         }
                     }
                 }
             }
             return SREL_PREFIX_TEMPLATES;
         }
     }
 
     /**
      * Determines the namespace prefix of the structural relations used in the document.
      *
      * @param document The document to look up the namespace in.
      * @return Returns the namespace prefix of the structural relations element of the document
      * @throws Exception If anything fails.
      */
     protected String determineOuNamespacePrefix(final Document document) throws Exception {
 
         Node root = selectSingleNode(document, XPATH_ORGANIZATIONAL_UNIT);
         if (root != null) {
             return determinePrefix(root);
         }
         else {
             NodeList list = document.getChildNodes();
             for (int i = 0; i < list.getLength(); i++) {
                 Node n = list.item(i);
                 NamedNodeMap attributes = n.getAttributes();
                 if (attributes != null) {
                     for (int j = 0; j < attributes.getLength(); j++) {
                         Node no = attributes.item(j);
                         if (no.getNodeValue().equals(Constants.NS_OUM_OU)) {
                             return no.getNodeName().replaceAll(".*?:(.*)", "$1");
                         }
                     }
                 }
             }
             return SREL_PREFIX_TEMPLATES;
         }
     }
 
     /**
      * Determines the namespace prefix of the xlink attributes used in the document.
      *
      * @param document The document to look up the namespace prefix in.
      * @return Returns the namespace prefix of the xlink href attribute in the root element of the document. If there is
      *         no xlink href attribute in the document, the value of the constant <code>XLINK_PREFIX_TEMPLATES</code> is
      *         returned.
      * @throws Exception If anything fails.
      */
     protected String determineXlinkNamespacePrefix(final Document document) throws Exception {
 
         Node hrefAttr = selectSingleNode(document, "/" + PART_XLINK_HREF);
         if (hrefAttr != null) {
             return determinePrefix(hrefAttr);
         }
         else {
             return XLINK_PREFIX_TEMPLATES;
         }
     }
 
     /**
      * Determines the namespace prefix of the provided node.
      *
      * @param node The <code>Node</code> to get the namespace prefix from.
      * @return Returns the namespace prefix of the provided <code>Node</code>.
      */
     protected String determinePrefix(final Node node) {
         String prefix = node.getPrefix();
         if (prefix == null) {
             prefix = node.getNodeName().replaceAll(":.*", "");
         }
         return prefix;
     }
 
     /**
      * Test retrieving an organizational unit from the framework.
      *
      * @param id The id of the organizational unit.
      * @return The retrieved organizational unit.
      * @throws Exception If anything fails.
      */
     @Override
     public String retrieve(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieve(id));
     }
 
     /**
      * Tries to retrieve a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestRetrieve(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = retrieve(ouId);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test retrieving the properties from the framework.
      *
      * @param id The id of the organizational unit.
      * @return The retrieved properties.
      * @throws Exception If anything fails.
      */
     public String retrieveProperties(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveProperties(id));
     }
 
     /**
      * Test retrieving resources from the framework.
      *
      * @param id The id of the organizational unit.
      * @return The retrieved list of resources.
      * @throws Exception If anything fails.
      */
     @Override
     public String retrieveResources(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveResources(id));
     }
 
     /**
      * Retrieving an md-record of the organization unit by name.
      * 
      * @param id
      *            The id of the organizational unit.
      * @param name
      *            The name of the md-record
      * @return The retrieved properties.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieveMdRecord(final String id, final String name) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveMdRecord(id, name));
     }
 
     /**
      * Test retrieving the organization-details from the framework.
      *
      * @param id The id of the organizational unit.
      * @return The retrieved properties.
      * @throws Exception If anything fails.
      */
     public String retrieveMdRecords(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveMdRecords(id));
     }
 
     /**
      * Tries to retrieve md-records of a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestRetrieveMdRecords(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = retrieveMdRecords(ouId);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test retrieving the parent-ous from the framework.
      *
      * @param id The id of the organizational unit.
      * @return The retrieved parent-ous.
      * @throws Exception If anything fails.
      */
     public String retrieveParents(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveParents(id));
     }
 
     /**
      * Tries to retrieve parents of a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestRetrieveParents(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = retrieveParents(ouId);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test retrieving the children of an organizational unit from the framework.
      *
      * @param id The id of the organizational unit.
      * @return The retrieved organizational unit.
      * @throws Exception If anything fails.
      */
     protected String retrieveChildObjects(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveChildObjects(id));
     }
 
     /**
      * Tries to retrieve children of a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestRetrieveChildObjects(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = retrieveChildObjects(ouId);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test retrieving the parents of an organizational unit from the framework.
      *
      * @param id The id of the organizational unit.
      * @return The retrieved organizational unit.
      * @throws Exception If anything fails.
      */
     protected String retrieveParentObjects(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveParentObjects(id));
     }
 
     /**
      * Tries to retrieve parents of a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestRetrieveParentObjects(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = retrieveParentObjects(ouId);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test creating an organizational unit in the framework.
      *
      * @param xml The xml representation of the organizational unit.
      * @return The created organizational unit.
      * @throws Exception If anything fails.
      */
     @Override
     public String create(final String xml) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().create(xml));
     }
 
     /**
      * Test creating an organizational unit in the framework.
      *
      * @param id The id of the organizational unit.
      * @throws Exception If anything fails.
      */
     @Override
     public void delete(final String id) throws Exception {
 
         getOrganizationalUnitClient().delete(id);
     }
 
     /**
      * Tries to delete a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @throws Exception Thrown if anything fails.
      */
     public void doTestDelete(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         PWCallback.setHandle(handle);
         try {
             delete(ouId);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Test closing an organizational unit in the framework.
      *
      * @param id        The id of the organizational unit.
      * @param taskParam TODO
      * @return The result XML (result.xsd)
      * @throws Exception If anything fails.
      */
     public String close(final String id, final String taskParam) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().close(id, taskParam));
     }
 
     /**
      * Tries to close a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestClose(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = close(ouId, getTheLastModificationParam(true, ouId, "Opened organizational unit '" + ouId + "'."));
 
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test opening an organizational unit in the framework.
      *
      * @param id        The id of the organizational unit.
      * @param taskParam TODO
      * @return The result XML (result.xsd)
      * @throws Exception If anything fails.
      */
     public String open(final String id, final String taskParam) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().open(id, taskParam));
     }
 
     /**
      * Tries to open a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestOpen(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = null;
         PWCallback.setHandle(handle);
         try {
             ouXml = open(ouId, getTheLastModificationParam(true, ouId, "Opened organizational unit '" + ouId + "'."));
 
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test updating an organizational unit of the framework.
      *
      * @param id  The id of the organizational unit.
      * @param xml The xml representation of the organizational unit.
      * @return The updated organizational unit.
      * @throws Exception If anything fails.
      */
     @Override
     public String update(final String id, final String xml) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().update(id, xml));
     }
 
     /**
      * Tries to update a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestUpdate(final String handle, final String ouId, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = retrieve(ouId);
         Document ouDoc = getDocument(ouXml);
         substitute(ouDoc, XPATH_ORGANIZATIONAL_UNIT_DESCRIPTION, "NewDescription");
 
         PWCallback.setHandle(handle);
         try {
             ouXml = update(ouId, toString(ouDoc, false));
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Tries to update a organizational unit.
      *
      * @param handle                 The user-handle.
      * @param ouId                   The id of the ou to update.
      * @param parentIds              The ids of the parents.
      * @param expectedExceptionClass The class of the expected exception or <code>null</code> in case of expected
      *                               success.
      * @return Returns the Xml representation of the updated organizational unit.
      * @throws Exception Thrown if anything fails.
      */
     public String doTestUpdateWithChangedParents(
         final String handle, final String ouId, final String[] parentIds, final Class expectedExceptionClass)
         throws Exception {
 
         String ouXml = retrieve(ouId);
         Document ouDoc = getDocument(ouXml);
         String[] parentsWithTitle = null;
         if (parentIds != null) {
             parentsWithTitle = new String[parentIds.length * 2];
             System.arraycopy(parentIds, 0, parentsWithTitle, 0, parentIds.length);
         }
 
         // delete old parents and add new parent to child2
         // this is done by replacing the parent ous element
         deleteElement(ouDoc, XPATH_ORGANIZATIONAL_UNIT_PARENTS);
         insertParentsElement(ouDoc, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentsWithTitle, false);
 
         String toBeUpdatedXml = toString(ouDoc, false);
 
         PWCallback.setHandle(handle);
         try {
             ouXml = update(ouId, toBeUpdatedXml);
             if (expectedExceptionClass != null) {
                 EscidocAbstractTest.failMissingException(expectedExceptionClass);
             }
         }
         catch (final Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocAbstractTest.failException(e);
             }
             else {
                 EscidocAbstractTest.assertExceptionType(expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return ouXml;
     }
 
     /**
      * Test updating the md records sub resource of an organizational unit of the framework.
      *
      * @param id  The id of the organizational unit.
      * @param xml The xml representation of the the organization-details sub resource organizational unit.
      * @return The updated organization-details sub resource of the organizational unit.
      * @throws Exception If anything fails.
      */
     public String updateMdRecords(final String id, final String xml) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().updateMdRecords(id, xml));
     }
 
     /**
      * Test updating the parent-ous sub resource of an organizational unit of the framework.
      *
      * @param id  The id of the organizational unit.
      * @param xml The xml representation of the the organization-details sub resource organizational unit.
      * @return The updated parent-ous sub resource of the organizational unit.
      * @throws Exception If anything fails.
      */
     public String updateParentOus(final String id, final String xml) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().updateParents(id, xml));
     }
 
     /**
      * Test retrieving a filtered list of organizational units from the framework.
      *
      * @param filter The filter criteria.
      * @return The retrieved list of organizational units.
      * @throws Exception If anything fails.
      */
     protected String retrieveOrganizationalUnits(final Map<String, String[]> filter) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveOrganizationalUnits(filter));
     }
 
     /**
      * Test retrieving a organizationalunitpathlist of organizational.
      *
      * @param id The organizational unit id.
      * @return The retrieved list of references to organizational units.
      * @throws Exception If anything fails.
      */
     protected String retrievePathList(final String id) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrievePathList(id));
     }
 
     /**
      * Test retrieving list of successors of organizational unit.
      *
      * @param objid The organizational unit id.
      * @return The retrieved list of references to organizational units.
      * @throws Exception If anything fails.
      */
     protected String retrieveSuccessors(final String objid) throws Exception {
 
         return handleXmlResult(getOrganizationalUnitClient().retrieveSuccessors(objid));
     }
 
     /**
      * Assert the retrieved organization-details has all expected elements.
      *
      * @param organizationalUnitId The organizational unit id.
      * @param mdRecords            The retrieved md records sub resource.
      * @param originalDocument     The document used to create the organizational-unit
      * @param timestampBeforeLastModification
      *                             A timestamp before the last modification has been started. This is used to check the
      *                             last modification date.
      * @throws Exception If anything fails.
      */
     public void assertEscidocMdRecord(
         final String organizationalUnitId, final Node mdRecords, final Node originalDocument,
         final String timestampBeforeLastModification) throws Exception {
 
         String messagePrefix = "OU organization-details error: ";
         Node hrefNode = selectSingleNode(mdRecords, XPATH_ORGANIZATION_MD_RECORDS + PART_XLINK_HREF);
         assertNotNull(messagePrefix + " No href found! ", hrefNode);
         String href = hrefNode.getNodeValue();
         // String hrefBase = XPATH_ORGANIZATIONAL_UNIT_ORGANIZATION_DETAILS
         assertEquals(messagePrefix + "href wrong baseurl! ", href, Constants.ORGANIZATIONAL_UNIT_BASE_URI + "/"
             + organizationalUnitId + "/" + NAME_MD_RECORDS);
         final String xpathLastModificationDate = XPATH_ORGANIZATION_MD_RECORDS + PART_LAST_MODIFICATION_DATE;
         assertXmlExists(messagePrefix + "Missing last modification date. ", mdRecords, xpathLastModificationDate);
         final String lastModificationDate = selectSingleNode(mdRecords, xpathLastModificationDate).getTextContent();
         assertNotEquals(messagePrefix + "Empty last modification date. ", "", lastModificationDate);
         if (timestampBeforeLastModification != null) {
             assertTimestampIsEqualOrAfter(messagePrefix + "last-modification-date is not as expected. ",
                 lastModificationDate, timestampBeforeLastModification);
         }
 
         // dc:title
         assertXmlEquals("OU error: dc:title mismatch, ", mdRecords, XPATH_MD_RECORDS_ESCIDOC_MD_RECORD + "/"
             + NAME_TITLE, selectSingleNode(originalDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE).getTextContent());
 
         // type
         assertXmlEquals("OU error: type mismatch, ", mdRecords, XPATH_MD_RECORDS_ESCIDOC_MD_RECORD + "/"
             + NAME_ORGANIZATION_TYPE, selectSingleNode(originalDocument, XPATH_ORGANIZATIONAL_UNIT_ORGANIZATION_TYPE)
             .getTextContent());
 
         // dc:description
         Node descriptionNode = selectSingleNode(originalDocument, XPATH_ORGANIZATIONAL_UNIT_DESCRIPTION);
         if (descriptionNode != null) {
             assertXmlEquals("OU error: dc:description mismatch", mdRecords, XPATH_MD_RECORDS_ESCIDOC_MD_RECORD + "/"
                 + NAME_DESCRIPTION, descriptionNode.getTextContent().trim());
         }
 
         // dc:identifier
         Node identifierNode = selectSingleNode(originalDocument, XPATH_ORGANIZATIONAL_UNIT_IDENTIFIER);
         if (identifierNode != null) {
             assertXmlEquals("OU error: dc:identifier mismatch", mdRecords, XPATH_MD_RECORDS_ESCIDOC_MD_RECORD + "/"
                 + NAME_IDENTIFIER, identifierNode.getTextContent().trim());
         }
         //        
         // // abbreviation
         // assertXmlEquals(messagePrefix + "abbrevation mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_ALTERNATIVE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_ALTERNATIVE).getTextContent());
         //
         // // name
         // assertXmlEquals(messagePrefix + "name mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_NAME, selectSingleNode(
         // originalDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE)
         // .getTextContent());
         //
         // // uri
         // assertXmlEquals(messagePrefix + "uri mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_URI, selectSingleNode(
         // originalDocument, XPATH_ORGANIZATIONAL_UNIT_URI)
         // .getTextContent());
         //
         // // type
         // assertXmlEquals(messagePrefix + "type mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_ORGANIZATION_TYPE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_ORGANIZATION_TYPE).getTextContent());
         //
         // // description
         // assertXmlEquals(messagePrefix + "description mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_DESCRIPTION,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_DESCRIPTION).getTextContent());
         //
         // // external id
         // if (selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_IDENTIFIER) != null) {
         // assertXmlEquals(messagePrefix + "external-id mismatch, ",
         // mdRecords, XPATH_ORGANIZATION_MD_RECORDS + "/"
         // + NAME_IDENTIFIER, selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_IDENTIFIER).getTextContent());
         // }
         //
         // // postcode
         // assertXmlEquals(messagePrefix + "postcode mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_POSTCODE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_POSTCODE).getTextContent());
         //
         // // country
         // assertXmlEquals(messagePrefix + "country mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_COUNTRY,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_COUNTRY).getTextContent());
         //
         // // region
         // assertXmlEquals(
         // messagePrefix + "region mismatch, ",
         // mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_REGION,
         // selectSingleNode(originalDocument, XPATH_ORGANIZATIONAL_UNIT_REGION)
         // .getTextContent());
         //
         // // address
         // assertXmlEquals(messagePrefix + "address mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_START_DATE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_START_DATE).getTextContent());
         //
         // // city
         // assertXmlEquals(messagePrefix + "city mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_CITY, selectSingleNode(
         // originalDocument, XPATH_ORGANIZATIONAL_UNIT_CITY)
         // .getTextContent());
         //
         // // telephone
         // assertXmlEquals(messagePrefix + "telephone mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_TELEPHONE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_TELEPHONE).getTextContent());
         //
         // // fax
         // assertXmlEquals(messagePrefix + "fax mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_FAX, selectSingleNode(
         // originalDocument, XPATH_ORGANIZATIONAL_UNIT_FAX)
         // .getTextContent());
         //
         // // email
         // assertXmlEquals(messagePrefix + "email mismatch, ", mdRecords,
         // XPATH_ORGANIZATION_MD_RECORDS + "/" + NAME_END_DATE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_END_DATE).getTextContent());
         //
         // // longitude
         // assertXmlEquals(messagePrefix + "location-longitude mismatch, ",
         // mdRecords, XPATH_ORGANIZATION_MD_RECORDS + "/"
         // + NAME_GEO_COORDINATE + "/" + NAME_LOCATION_LONGITUDE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_GEO_COORDINATE_LONGITUDE)
         // .getTextContent());
         //
         // // latitude
         // assertXmlEquals(messagePrefix + "location-latitude mismatch, ",
         // mdRecords, XPATH_ORGANIZATION_MD_RECORDS + "/"
         // + NAME_GEO_COORDINATE + "/" + NAME_LOCATION_LATITUDE,
         // selectSingleNode(originalDocument,
         // XPATH_ORGANIZATIONAL_UNIT_GEO_COORDINATE_LATITUDE)
         // .getTextContent());
 
     }
 
     /**
      * Assert that the organizational unit has all required elements.
      *
      * @param toBeAssertedXml         The created/updated organizational unit.
      * @param originalXml             The template organizational unit used to create/update the organizational unit. If
      *                                this parameter is <code>null</code>, no check with the original data is
      *                                performed.
      * @param timestampBeforeCreation A timestamp before the creation has been started. This is used to check the
      *                                creation date.
      * @param timestampBeforeLastModification
      *                                A timestamp before the last modification has been started. This is used to check
      *                                the last modification date.
      * @return Returns the document representing the provided xml data.
      * @throws Exception If anything fails.
      */
     public Document assertOrganizationalUnit(
         final String toBeAssertedXml, final String originalXml, final String timestampBeforeCreation,
         final String timestampBeforeLastModification) throws Exception {
 
         return assertOrganizationalUnit(toBeAssertedXml, originalXml, timestampBeforeCreation,
             timestampBeforeLastModification, true, false);
     }
 
     /**
      * Assert that the organizational unit has all required elements.
      *
      * @param toBeAssertedXml         The created/updated organizational unit.
      * @param originalXml             The template organizational unit used to create/update the organizational unit. If
      *                                this parameter is <code>null</code>, no check with the original data is
      *                                performed.
      * @param timestampBeforeCreation A timestamp before the creation has been started. This is used to check the
      *                                creation date.
      * @param timestampBeforeLastModification
      *                                A timestamp before the last modification has been started. This is used to check
      *                                the last modification date.
      * @param assertCreationDate      Flag to indicate if the creation-date and created-by values shall be asserted
      *                                (<code>true</code>) or not ( <code>false</code>).
      * @param comparePublicStatus     Flag to indicate if the public-status in <code>toBeAssertedXml</code> and
      *                                <code>originalXml</code> should be compared (<code>true</code>) or not (
      *                                <code>false</code>). If not, the public-status in <code>toBeAssertedXml</code> is
      *                                assumed to be created.
      * @return Returns the document representing the provided xml data.
      * @throws Exception If anything fails.
      */
     public Document assertOrganizationalUnit(
         final String toBeAssertedXml, final String originalXml, final String timestampBeforeCreation,
         final String timestampBeforeLastModification, final boolean assertCreationDate,
         final boolean comparePublicStatus) throws Exception {
 
         assertXmlValidOrganizationalUnit(toBeAssertedXml);
         Document document = getDocument(toBeAssertedXml);
 
         String[] rootValues =
             assertRootElement("Invalid OU root element. ", document, XPATH_ORGANIZATIONAL_UNIT,
                 Constants.ORGANIZATIONAL_UNIT_BASE_URI, timestampBeforeLastModification);
         final String id = rootValues[0];
 
         assertReferencingElement("Assert of resources failed. ", document, XPATH_ORGANIZATIONAL_UNIT_RESOURCES, null);
         assertReferencingElement("Assert of resource children failed. ", document, XPATH_ORGANIZATIONAL_UNIT_RESOURCES
             + "/" + "child-objects[@href=\"/oum/organizational-unit/" + id + "/resources/child-objects\"]", null);
         assertReferencingElement("Assert of resource parents failed. ", document, XPATH_ORGANIZATIONAL_UNIT_RESOURCES
             + "/" + "parent-objects[@href=\"/oum/organizational-unit/" + id + "/resources/parent-objects\"]", null);
         assertReferencingElement("Assert of resource path-list failed. ", document, XPATH_ORGANIZATIONAL_UNIT_RESOURCES
             + "/" + "path-list[@href=\"/oum/organizational-unit/" + id + "/resources/path-list\"]", null);
 
         // assert properties
         assertPropertiesElementUnversioned("Asserting OU properties failed. ", document,
             XPATH_ORGANIZATIONAL_UNIT_PROPERTIES, timestampBeforeCreation);
 
         // public-status
         assertXmlExists("OU error: status was not set!", document, XPATH_ORGANIZATIONAL_UNIT_PUBLIC_STATUS);
 
         if (originalXml == null) {
             assertXmlEquals("OU error: status has wrong value!", document, XPATH_ORGANIZATIONAL_UNIT_PUBLIC_STATUS,
                 ORGANIZATIONAL_UNIT_STATUS_CREATED);
         }
 
         // has-children
         assertXmlExists("OU error: " + " Missing has-children", document, XPATH_ORGANIZATIONAL_UNIT_HAS_CHILDREN);
 
         Document originalDocument = null;
         if (originalXml != null) {
             originalDocument = getDocument(originalXml);
 
             if (comparePublicStatus) {
                 assertXmlEquals("OU error: status has wrong value!", document, XPATH_ORGANIZATIONAL_UNIT_PUBLIC_STATUS,
                     originalDocument, XPATH_ORGANIZATIONAL_UNIT_PUBLIC_STATUS);
                 assertXmlEquals("OU error: status was not set!", document, XPATH_ORGANIZATIONAL_UNIT_PUBLIC_STATUS,
                     ORGANIZATIONAL_UNIT_STATUS_CREATED);
             }
 
             if (assertCreationDate) {
                 final String expectedCreationDate = getCreationDateValue(originalDocument);
                 if (expectedCreationDate != null) {
 
                     // creation-date
                     assertXmlEquals("OU error: " + "creation date mismatch, ", document,
                         XPATH_ORGANIZATIONAL_UNIT_CREATION_DATE, expectedCreationDate);
 
                     // created-by
                     assertCreatedBy("OU error: " + "created-by invalid", originalDocument, document);
                 }
             }
 
             // assert number of md-record elements
             assertOrganizationalUnitMdRecordsElement("OU error: assert md-records element failed: ", originalDocument,
                 XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, document, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS);
 
         }
 
         // assert parents
         // FIXME: sorted = false
         assertListOfReferences("OU error: Asserting parent ous failed. ", originalDocument, document,
             XPATH_ORGANIZATIONAL_UNIT_PARENTS, NAME_PARENT, Constants.ORGANIZATIONAL_UNIT_BASE_URI + "/" + id + "/"
                 + NAME_PARENTS, false, timestampBeforeLastModification, Constants.ORGANIZATIONAL_UNIT_BASE_URI, false);
 
         return document;
     }
 
     /**
      * Compare md-record elements of organizational units.
      * <p/>
      * <p> There is one issue with eSciDoc XML documents and md-records. The order of md-record elements is out of
      * focus. This means that for comparing of md-records the position within the XML document doesn't count (as far it
      * is within md-records elements of course). One has to distinguish md-records by attribute name. </p>
      *
      * @param message           Assert message (lead text).
      * @param original          Original document
      * @param originalBaseXpath base path of original document
      * @param document          document to compare
      * @param documentBaseXpath base path of to compare document
      * @throws Exception If documents differ or document structure is not as expected.
      */
     public void assertOrganizationalUnitMdRecordsElement(
         final String message, final Document original, final String originalBaseXpath, final Document document,
         final String documentBaseXpath) throws Exception {
 
         String originalXpathMdRecord = originalBaseXpath + "/" + NAME_MD_RECORD;
         String documentXpathMdRecord = documentBaseXpath + "/" + NAME_MD_RECORD;
 
         // assert number of md-record elements
         int noOrigMdRecord = selectNodeList(original, originalXpathMdRecord).getLength();
         int noRetrMdRecord = selectNodeList(document, documentXpathMdRecord).getLength();
         assertEquals(message + " Number of md-record not the same.", noOrigMdRecord, noRetrMdRecord);
 
         if (noOrigMdRecord > 0) {
             NodeList mdRecordElementsOrig = selectNodeList(original, originalXpathMdRecord);
 
             for (int i = 0; i < noOrigMdRecord; ++i) {
                 String name =
                     getAttributeValue(mdRecordElementsOrig.item(i), originalXpathMdRecord + "[" + (i + 1) + "]", "name");
                 assertNotNull(message + " Name of md-record not found. ", name);
 
                 // pick md-record with equal name of to compare document
                 NodeList mdRecordByName = selectNodeList(document, documentXpathMdRecord + "[@name='" + name + "']");
 
                 // assert name
                 assertEquals(message + " More than one md-record with same name.", 1, mdRecordByName.getLength());
 
                 // assert md-type
                 Node mdRecord = mdRecordByName.item(0);
 
                 String typeOrig =
                     getAttributeValue(mdRecordElementsOrig.item(i), originalXpathMdRecord + "[" + (i + 1) + "]",
                         "md-type");
 
                 Node typeDocNode = mdRecord.getAttributes().getNamedItem("md-type");
 
                 if (typeOrig != null) {
                     assertEquals("md-type does not match", typeOrig, typeDocNode.getTextContent());
                 }
                 else {
                     assertNull("md-type does not match", typeDocNode);
                 }
 
                 String appendXPath = "[@name='" + name + "'";
                 if (typeOrig != null) {
                     assertNotEquals(message + "The md-type is set even if its empty", typeOrig, "null");
                     appendXPath += " and @md-type='" + typeOrig + "'";
                 }
                 appendXPath += "]";
                 assertXmlEquals(message, original, originalXpathMdRecord + appendXPath, document, documentXpathMdRecord
                     + appendXPath);
             }
         }
 
     }
 
     /**
      * Asserts that the provided document contains a valid Xml representation of an organizational unit list (like
      * retrieved from retrieveParents, retrieveChildren), and contains the expected organizational units.
      *
      * @param message         The assertion failed message.
      * @param expectedOus     The map from id to xml representation of the expected organizational units.
      * @param toBeAssertedXml The xml data to be asserted.
      * @return Returns the <code>Document</code> for the provided xml data.
      * @throws Exception Thrown if anything fails.
      */
     public Document assertOrganizationalUnitList(
         final String message, final Map<String, String> expectedOus, final String toBeAssertedXml) throws Exception {
 
         final String msg = prepareAssertionFailedMessage(message);
 
         assertXmlValidSrwResponse(toBeAssertedXml);
         Document toBeAssertedDocument = getDocument(toBeAssertedXml);
 
         NodeList ouNodes = selectNodeList(toBeAssertedDocument, XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_ORGANIZATIONAL_UNIT);
         assertEquals(msg + "Number of list entries mismatch.", expectedOus.size(), ouNodes.getLength());
 
         for (int i = 0; i < ouNodes.getLength(); i++) {
             final String toBeAssertedOuXml = toString(ouNodes.item(i), true);
             final String toBeAssertedOuId = getObjidValue(toBeAssertedOuXml);
             final String expectedXml = expectedOus.get(toBeAssertedOuId);
             assertNotNull(msg + "Unexpected list entry [" + i + ", " + toBeAssertedOuId + "]", expectedXml);
             assertOrganizationalUnit(toBeAssertedOuXml, expectedXml, startTimestamp, startTimestamp);
             expectedOus.remove(toBeAssertedOuId);
         }
         assertEmptyMap(msg + "Search didnt find expected OUs:", expectedOus);
 
         return toBeAssertedDocument;
     }
 
     /**
      * Asserts that the provided document contains a valid Xml representation of an organizational unit list (like
      * retrieved from retrieveParents, retrieveChildren), and contains the expected organizational units.
      *
      * @param message         The assertion failed message.
      * @param expectedBaseUri The expected base uri in the root element.<br/> If this parameter is <code>null</code>,
      *                        the root element will not be checked for containing xlink attributes.
      * @param expectedOus     The map from id to xml representation of the expected organizational units.
      * @param toBeAssertedXml The xml data to be asserted.
      * @return Returns the <code>Document</code> for the provided xml data.
      * @throws Exception Thrown if anything fails.
      */
     public Document assertOrganizationalUnitSrwList(
         final String message, final String expectedBaseUri, final Map<String, String> expectedOus,
         final String toBeAssertedXml) throws Exception {
 
         final String msg = prepareAssertionFailedMessage(message);
 
         assertXmlValidSrwResponse(toBeAssertedXml);
         Document toBeAssertedDocument = getDocument(toBeAssertedXml);
 
         // organizational-unit-list (root element)
         if (expectedBaseUri != null) {
             assertXlinkElement(msg + "Asserting root element failed. ", toBeAssertedDocument, XPATH_SRW_RESPONSE_ROOT,
                 expectedBaseUri);
         }
 
         NodeList ouNodes = selectNodeList(toBeAssertedDocument, XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_ORGANIZATIONAL_UNIT);
         assertEquals(msg + "Number of list entries mismatch.", expectedOus.size(), ouNodes.getLength());
 
         for (int i = 0; i < ouNodes.getLength(); i++) {
             final String toBeAssertedOuXml =
                 toString(selectSingleNode(toBeAssertedDocument, XPATH_SRW_RESPONSE_RECORD + "[" + (i + 1) + "]"
                     + XPATH_SRW_RESPONSE_OBJECT_SUBPATH + NAME_ORGANIZATIONAL_UNIT), true);
             final String toBeAssertedOuId = getObjidValue(toBeAssertedOuXml);
             final String expectedXml = expectedOus.get(toBeAssertedOuId);
             assertNotNull(msg + "Unexpected list entry [" + i + ", " + toBeAssertedOuId + "]", expectedXml);
             assertOrganizationalUnit(toBeAssertedOuXml, expectedXml, startTimestamp, startTimestamp);
             expectedOus.remove(toBeAssertedOuId);
         }
 
         return toBeAssertedDocument;
     }
 
     /**
      * Asserts that the provided document contains a valid Xml representation of an organizational unit ref list, and
      * contains refernces to the expected organizational units.
      *
      * @param message         The assertion failed message.
      * @param expectedBaseUri The expected base uri in the root element.
      * @param expectedOus     The map from id to xml representation of the expected organizational units.
      * @param toBeAssertedXml The xml data to be asserted.
      * @return Returns the <code>Document</code> for the provided xml data.
      * @throws Exception Thrown if anything fails.
      */
     public Document assertOrganizationalUnitRefList(
         final String message, final String expectedBaseUri, final Map<String, String> expectedOus,
         final String toBeAssertedXml) throws Exception {
 
         final String msg = prepareAssertionFailedMessage(message);
 
         assertXmlValidOrganizationalUnitsRefs(toBeAssertedXml);
         Document toBeAssertedDocument = getDocument(toBeAssertedXml);
 
         NodeList ouRefNodes =
             selectNodeList(toBeAssertedDocument, XPATH_ORGANIZATIONAL_UNIT_REF_LIST_ORGANIZATIONAL_UNIT_REF);
         assertEquals(msg + "Number of list entries mismatch.", expectedOus.size(), ouRefNodes.getLength());
 
         for (int i = 0; i < ouRefNodes.getLength(); i++) {
             final String toBeAssertedOuId =
                 getObjidValue(toBeAssertedDocument, XPATH_ORGANIZATIONAL_UNIT_REF_LIST_ORGANIZATIONAL_UNIT_REF + "["
                     + (i + 1) + "]");
             assertNotNull(msg + " Unexpected reference [" + i + ", " + toBeAssertedOuId + "]", expectedOus
                 .get(toBeAssertedOuId));
             expectedOus.remove(toBeAssertedOuId);
         }
 
         return toBeAssertedDocument;
     }
 }
