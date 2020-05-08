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
 
 import static org.junit.Assert.fail;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.w3c.dom.Document;
 
import de.escidoc.core.common.exceptions.remote.application.violated.OrganizationalUnitNameNotUniqueException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.test.common.client.servlet.Constants;
 
 /**
  * Test the create method of the organizational unit handler.
  * 
  * @author MSC
  * 
  */
 @RunWith(value = Parameterized.class)
 public class CreateTest extends OrganizationalUnitTestBase {
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public CreateTest(final int transport) {
         super(transport);
     }
 
     /**
      * Test successfully creating an organizational unit where the root element
      * of metadata has no XML prefix.
      * 
      * @test.input Valid Organizational Unit XML representation.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1aMDPrefix() throws Exception {
 
         Document ou =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create_md-without-prefix.xml");
         setUniqueValue(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         String template = toString(ou, false);
         assertXmlValidOrganizationalUnit(template);
 
         String xml = null;
         try {
             xml = create(template);
         }
         catch (Exception e) {
             failException("OU create failed with exception. ", e);
         }
         assertOrganizationalUnit(xml, template, startTimestamp, startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit.
      * 
      * @test.name Create Organizational Unit - Success
      * @test.id OUM_COU-1-a
      * @test.input Valid Organizational Unit XML representation.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1a() throws Exception {
 
         Document ou =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         String template = toString(ou, false);
         assertXmlValidOrganizationalUnit(template);
 
         String xml = null;
         try {
             xml = create(template);
         }
         catch (Exception e) {
             failException("OU create failed with exception. ", e);
         }
         assertOrganizationalUnit(xml, template, startTimestamp, startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with parents in state
      * created.
      * 
      * @test.name Create Organizational Unit - With Parents - Success
      * @test.id OUM_COU-1-b
      * @test.input Valid Organizational Unit XML representation including
      *             specified parent ous.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date. Parents as defined.
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1b() throws Exception {
 
         final String[] parentValues =
             createSuccessfully("escidoc_ou_create.xml", 2);
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (Exception e) {
             failException("Creating OU with parents failed with exception. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp,
             startTimestamp);
     }
 
     // /**
     // * Test successfully creating an organizational unit including pid.
     // *
     // * @test.name Create Organizational Unit - Including Pid
     // * @test.id OUM_COU-1-c
     // * @test.input Organizational Unit XML representation.
     // * @test.expected: The expected result is the XML representation of the
     // * created OrganizationalUnit, corresponding to XML-schema
     // * "organizational-unit.xsd" including generated id, creator
     // * and creation date and pid.
     // * @test.status Revoked - no more requirements for external id handling at
     // * the moment
     // *
     // * @throws Exception
     // * Thrown if anything fails.
     // */
     // public void testOumCou1c() throws Exception {
     //
     // Document toBeCreatedDocument =
     // getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
     // "escidoc_ou_create_with_external_id.xml");
     // setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
     // String toBeCreatedXml = toString(toBeCreatedDocument, false);
     // String createdXml = null;
     // try {
     // createdXml = create(toBeCreatedXml);
     // }
     // catch (Exception e) {
     // failException("Creating OU with PID failed", e);
     // }
     // assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp,
     // startTimestamp);
     //
     // assertXmlEquals("External id mismatch.", toBeCreatedDocument,
     // getDocument(createdXml), XPATH_ORGANIZATIONAL_UNIT_IDENTIFIER);
     // }
     /**
      * Test successfully creating an organizational unit using lax mode.
      * 
      * @test.name Create Organizational Unit - Lax Mode with Objid
      * @test.id OUM_COU-1-d
      * @test.input Organizational Unit XML representation. Attributes of
      *             elements that are handled lax are not provided. Objid instead
      *             of Href is provided in parent-ou elements.
      * @test.expected: <ul>
      *                 <li>In case of REST: XmlSchemaValidationException.</li>
      *                 <li>In case of SOAP: The expected result is the XML
      *                 representation of the created OrganizationalUnit,
      *                 corresponding to XML-schema "organizational-unit.xsd"
      *                 including generated id, creator and creation date.</li>
      *                 </ul>
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1d() throws Exception {
 
         final String[] parentValues =
             createSuccessfully("escidoc_ou_create.xml", 2);
         final String parent1Id = parentValues[0];
         final String parent2Id = parentValues[1];
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         toBeCreatedDocument = getDocument(toString(toBeCreatedDocument, false));
 
         // delete lax attributes
         // organizational-unit root element
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_XLINK_TYPE);
         // properties
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PROPERTIES_XLINK_TYPE);
         // data
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS_XLINK_TYPE);
         // parent-ous
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENTS_XLINK_TYPE);
         // parent-ou
         addAttribute(
             toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENT + "[1]",
             createAttributeNode(toBeCreatedDocument, null, null, NAME_OBJID,
                 parent1Id));
         addAttribute(
             toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENT + "[2]",
             createAttributeNode(toBeCreatedDocument, null, null, NAME_OBJID,
                 parent2Id));
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_HREF);
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TITLE);
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TYPE);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         if (getTransport() == Constants.TRANSPORT_REST) {
 
             final Class<XmlSchemaValidationException> ec =
                 XmlSchemaValidationException.class;
             try {
                 create(toBeCreatedXml);
                 failMissingException(ec);
             }
             catch (Exception e) {
                 assertExceptionType(ec, e);
             }
         }
         else {
             String createdXml = null;
             try {
                 createdXml = create(toBeCreatedXml);
             }
             catch (Exception e) {
                 failException("Lax creating of ou failed. ", e);
             }
             assertOrganizationalUnit(createdXml, toBeCreatedXml,
                 startTimestamp, startTimestamp);
         }
     }
 
     /**
      * Test successfully creating an organizational unit using lax mode.
      * 
      * @test.name Create Organizational Unit - Lax Mode with Href
      * @test.id OUM_COU-1-e
      * @test.input Organizational Unit XML representation. Attributes of
      *             elements that are handled lax are not provided. Href instead
      *             of Objid is provided in parent-ou elements.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1e() throws Exception {
 
         final String[] parentValues =
             createSuccessfully("escidoc_ou_create.xml", 2);
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         toBeCreatedDocument = getDocument(toString(toBeCreatedDocument, false));
 
         // delete lax attributes
         // organizational-unit root element
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_XLINK_TYPE);
         // properties
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PROPERTIES_XLINK_TYPE);
         // data
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS_XLINK_TYPE);
         // parent-ous
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENTS_XLINK_TYPE);
         // parent-ou
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TITLE);
         deleteNodes(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TYPE);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (Exception e) {
             failException("Lax creating of ou failed. ", e);
         }
         assertXmlValidOrganizationalUnit(createdXml);
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp,
             startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with 2 md-record
      * elements.
      * 
      * @test.name Create Organizational Unit - Multiple md-record Elements
      * @test.id OUM_COU-1-f
      * @test.input Organizational Unit XML representation. In md-records 2
      *             md-record elements have been placed.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1f() throws Exception {
 
         // final String[] parentValues =
         // createSuccessfully("escidoc_ou_create_2_md_records.xml", 2);
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create_2_md_records.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         // insertParentsElement(toBeCreatedDocument,
         // XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         toBeCreatedDocument = getDocument(toString(toBeCreatedDocument, false));
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (Exception e) {
             failException("Lax creating of ou failed. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp,
             startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with parents in state
      * opened.
      * 
      * @test.name Create Organizational Unit - With Parents - Success
      * @test.id OUM_COU-1-g
      * @test.input Valid Organizational Unit XML representation including
      *             specified parent ous.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date. Parents as defined.
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1g() throws Exception {
 
         final String[] parentValues =
             createSuccessfully("escidoc_ou_create.xml", 2);
         open(
             parentValues[0],
             getTheLastModificationParam(true, parentValues[0],
                 "Opened organizational unit '" + parentValues[0] + "'."));
         open(
             parentValues[1],
             getTheLastModificationParam(true, parentValues[1],
                 "Opened organizational unit '" + parentValues[1] + "'."));
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (Exception e) {
             failException("Creating OU with parents failed with exception. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp,
             startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with parents - one in
      * state opened and one in state created.
      * 
      * @test.name Create Organizational Unit - With Parents - Success
      * @test.id OUM_COU-1-h
      * @test.input Valid Organizational Unit XML representation including
      *             specified parent ous.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date. Parents as defined.
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou1h() throws Exception {
 
         final String[] parentValues =
             createSuccessfully("escidoc_ou_create.xml", 2);
         open(
             parentValues[0],
             getTheLastModificationParam(true, parentValues[0],
                 "Opened organizational unit '" + parentValues[0] + "'."));
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (Exception e) {
             failException("Creating OU with parents failed with exception. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp,
             startTimestamp);
     }
 
     /**
      * Test declining creating an organizational unit without providing a
      * title/name.
      * 
      * @test.name Create Organizational Unit - No Name
      * @test.id OUM_COU-2-a
      * @test.input Organizational Unit XML representation without a title/name.
      * @test.expected: MissingElementValueException
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou2a() throws Exception {
 
         Class<MissingElementValueException> ec =
             MissingElementValueException.class;
 
         final String toBeCreatedXml =
             toString(
                 deleteElement(
                     getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                         "escidoc_ou_create.xml"),
                     XPATH_ORGANIZATIONAL_UNIT_TITLE), false);
 
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating an organizational unit with an invalid xml
      * representation (xml is an item xml).
      * 
      * @test.name Create Organizational Unit - Invalid xml
      * @test.id OUM_COU-2-b
      * @test.input XML representation of an item.
      * @test.expected: InvalidXmlException
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou2b() throws Exception {
 
         Class<XmlCorruptedException> ec = XmlCorruptedException.class;
 
         final String toBeCreatedXml =
             getTemplateAsString(TEMPLATE_ITEM_PATH, "escidoc_item_198.xml");
 
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating an organizational unit with providing an empty
      * name.
      * 
      * @test.name Create Organizational Unit - Empty Name
      * @test.id OUM_COU-2-c
      * @test.input Organizational Unit XML representation with an empty name.
      * @test.expected: MissingElementValueException
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou2c() throws Exception {
 
         Document ou =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         substitute(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE, "");
         final Class<MissingElementValueException> ec =
             MissingElementValueException.class;
 
         try {
             create(toString(ou, false));
             failMissingException(ec);
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating an organizational unit with providing a parent is
      * state closed.
      * 
      * @test.name Create Organizational Unit - Parent in state closed
      * @test.id OUM_COU-2-d
      * @test.input Organizational Unit XML representation with a parent in state
      *             closed.
      * @test.expected: InvalidStatusException
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou2d() throws Exception {
 
         final String[] parentValues =
             createSuccessfully("escidoc_ou_create.xml", 2);
         String closedParent = parentValues[0];
         open(
             closedParent,
             getTheLastModificationParam(true, closedParent,
                 "Opened organizational unit '" + closedParent + "'."));
         close(
             closedParent,
             getTheLastModificationParam(true, closedParent,
                 "Closed organizational unit '" + closedParent + "'."));
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
         final Class<InvalidStatusException> ec = InvalidStatusException.class;
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
 
     }
 
     /**
      * Test declining creating an organizational unit without an xml
      * representation.
      * 
      * @test.name Create Organizational Unit - Missing XML data
      * @test.id OUM_COU-4
      * @test.input No Organizational Unit XML representation is provided.
      * @test.expected: MissingMethodParameterException
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou4() throws Exception {
 
         Class<MissingMethodParameterException> ec =
             MissingMethodParameterException.class;
         try {
             create(null);
             failMissingException(ec);
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating a top level organizational unit with a non unique
      * name.
      * 
      * @test.name Create Organizational Unit - Duplicate Name of Top Level OU
      * @test.id OUM_COU-5-a
      * @test.input Organizational Unit XML representation of a top level
      *             Organizational unit containing a name of an organizational
      *             unit that just exists for another top level ou.
      * @test.expected: OrganizationalUnitNameNotUniqueException
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Ignore
     @Test
     public void testOumCou5a() throws Exception {
 
         final Class<OrganizationalUnitNameNotUniqueException> ec =
             OrganizationalUnitNameNotUniqueException.class;
         final Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
         create(toBeCreatedXml);
 
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating an organizational unit with a non unique name in
      * the scope of the parents.
      * 
      * @test.name Create Organizational Unit - Duplicate Name in Scope of
      *            Parents
      * @test.id OUM_COU-5-b
      * @test.input Organizational Unit XML representation containing a name of
      *             an organizational unit that just exists in the scope of the
      *             parents.
      * @test.expected: OrganizationalUnitNameNotUniqueException
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Ignore
     @Test
     public void testOumCou5b() throws Exception {
 
         Class<OrganizationalUnitNameNotUniqueException> ec =
             OrganizationalUnitNameNotUniqueException.class;
 
         // create parent
         final String[] topLevelValues =
             createSuccessfully("escidoc_ou_create.xml", 1);
         final String topLevelId = topLevelValues[0];
 
         // create first child
         final String child1Xml =
             createSuccessfullyChild("escidoc_ou_create.xml",
                 new String[] { topLevelId });
         final String child1Name =
             selectSingleNodeAsserted(getDocument(child1Xml),
                 XPATH_ORGANIZATIONAL_UNIT_TITLE).getTextContent();
 
         // create second child with same name
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         substitute(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE,
             child1Name);
         insertParentsElement(toBeCreatedDocument,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, topLevelValues, false);
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test successfully creating an organizational unit with a name of an
      * existing organizational unit in another scope of the parents.
      * 
      * @test.name Create Organizational Unit - Duplicate Name in different
      *            Scopes of Parents
      * @test.id OUM_COU-5-c
      * @test.input Organizational Unit XML representation containing a name of
      *             an organizational unit that just exists, but that is not in
      *             the scope of the parents of the organizational unit to be
      *             created.
      * @test.expected: The expected result is the XML representation of the
      *                 created OrganizationalUnit, corresponding to XML-schema
      *                 "organizational-unit.xsd" including generated id, creator
      *                 and creation date
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOumCou5c() throws Exception {
 
         // create two parent ous
         String[] parentValues = createSuccessfully("escidoc_ou_create.xml", 2);
         final String idParentOu1 = parentValues[0];
         final String idParentOu2 = parentValues[1];
         final String titleParentOu1 = parentValues[2];
         final String titleParentOu2 = parentValues[3];
 
         // create child of first parent ou
         Document childOu1Document =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(childOu1Document, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(childOu1Document,
             XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, new String[] { idParentOu1,
                 titleParentOu1 }, false);
         final String ouName =
             selectSingleNode(childOu1Document, XPATH_ORGANIZATIONAL_UNIT_TITLE)
                 .getTextContent();
         try {
             create(toString(childOu1Document, false));
         }
         catch (Exception e) {
             failException(
                 "Failure during init, creation of first child failed.", e);
         }
 
         // // create child of second parent ou. This second child ou has the
         // same
         // // name as the other child ou.
         // Document childOu2Document =
         // getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
         // "escidoc_ou_create.xml");
         // substitute(childOu2Document, XPATH_ORGANIZATIONAL_UNIT_TITLE,
         // ouName);
         // insertParentsElement(childOu2Document,
         // XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, new String[] { idParentOu2,
         // titleParentOu2 }, false);
         //
         // String toBeCreatedChildOu2Xml = toString(childOu2Document, false);
         //
         // String createdChildOu2Xml = null;
         // try {
         // createdChildOu2Xml = create(toBeCreatedChildOu2Xml);
         // }
         // catch (Exception e) {
         // failException("creating 2nd child ou with same name but in"
         // + " another scope failed with exception.", e);
         // }
         // assertOrganizationalUnit(createdChildOu2Xml, toBeCreatedChildOu2Xml,
         // startTimestamp, startTimestamp);
     }
 
     /**
      * Test if the last modification date of an fresh created OU equals the
      * last-modification-date of the afterwards retrieved OU.
      * 
      * @throws Exception
      *             Thrown if last-modification-date between create and retrieve
      *             differs.
      */
     @Test
     public void testOumLastModificationDate() throws Exception {
 
         String ouXml = createSuccessfully("escidoc_ou_create.xml");
         Document ouDoc = getDocument(ouXml);
         String objid = getObjidValue(ouDoc);
         String lmdCreate = getTheLastModificationDate(ouDoc);
 
         ouXml = retrieve(objid);
         ouDoc = getDocument(ouXml);
         String lmdRetrieve = getTheLastModificationDate(ouDoc);
 
         assertEquals("Timestamps differ.", lmdCreate, lmdRetrieve);
     }
 
     /**
      * Test unexpected parser exception instead of InvalidXmlException during
      * create (see issue INFR-911).
      * 
      * @throws Exception
      *             Thrown if behavior is not as expected.
      */
     @Test(expected = InvalidXmlException.class)
     public void testInvalidXml() throws Exception {
 
         /*
          * The infrastructure has thrown an unexpected parser exception during
          * creation if a non XML datastructur is send (e.g. String).
          */
         create("laber-rababer");
     }
 
     /**
      * Test creating an Organizational Unit with missing required default
      * md-record. It's checked if the expected Exception is thrown.
      * 
      * See issue INFR-1016
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = MissingMdRecordException.class)
     public void testOuCreateWithoutMdRecord() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
 
         substitute(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS
             + "/md-record/@name", "non_default_name");
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         create(toBeCreatedXml);
     }
 
 }
