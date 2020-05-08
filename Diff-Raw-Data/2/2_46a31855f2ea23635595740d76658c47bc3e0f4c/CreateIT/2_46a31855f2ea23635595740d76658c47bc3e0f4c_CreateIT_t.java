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
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Test the create method of the organizational unit handler.
  *
  * @author Michael Schneider
  */
 public class CreateIT extends OrganizationalUnitTestBase {
 
     /**
      * Test successfully creating an organizational unit where the root element of metadata has no XML prefix.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1aMDPrefix() throws Exception {
 
         Document ou =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create_md-without-prefix.xml");
         setUniqueValue(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         String template = toString(ou, false);
         assertXmlValidOrganizationalUnit(template);
 
         String xml = null;
         try {
             xml = create(template);
         }
         catch (final Exception e) {
             failException("OU create failed with exception. ", e);
         }
         assertOrganizationalUnit(xml, template, startTimestamp, startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1a() throws Exception {
 
         Document ou = getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         String template = toString(ou, false);
         assertXmlValidOrganizationalUnit(template);
 
         String xml = null;
         try {
             xml = create(template);
         }
         catch (final Exception e) {
             failException("OU create failed with exception. ", e);
         }
         assertOrganizationalUnit(xml, template, startTimestamp, startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with parents in state created.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1b() throws Exception {
 
         final String[] parentValues = createSuccessfully("escidoc_ou_create.xml", 2);
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (final Exception e) {
             failException("Creating OU with parents failed with exception. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp, startTimestamp);
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
     // catch (final Exception e) {
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
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1d() throws Exception {
 
         final String[] parentValues = createSuccessfully("escidoc_ou_create.xml", 2);
         final String parent1Id = parentValues[0];
         final String parent2Id = parentValues[1];
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         toBeCreatedDocument = getDocument(toString(toBeCreatedDocument, false));
 
         // delete lax attributes
         // organizational-unit root element
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_XLINK_TYPE);
         // properties
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PROPERTIES_XLINK_TYPE);
         // data
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS_XLINK_TYPE);
         // parent-ous
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENTS_XLINK_TYPE);
         // parent-ou
         addAttribute(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENT + "[1]", createAttributeNode(
             toBeCreatedDocument, null, null, NAME_OBJID, parent1Id));
         addAttribute(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENT + "[2]", createAttributeNode(
             toBeCreatedDocument, null, null, NAME_OBJID, parent2Id));
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_HREF);
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TITLE);
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TYPE);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
         final Class<XmlSchemaValidationException> ec = XmlSchemaValidationException.class;
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (final Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test successfully creating an organizational unit using lax mode.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1e() throws Exception {
 
         final String[] parentValues = createSuccessfully("escidoc_ou_create.xml", 2);
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         toBeCreatedDocument = getDocument(toString(toBeCreatedDocument, false));
 
         // delete lax attributes
         // organizational-unit root element
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_XLINK_TYPE);
         // properties
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PROPERTIES_XLINK_TYPE);
         // data
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS_XLINK_TYPE);
         // parent-ous
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENTS_XLINK_TYPE);
         // parent-ou
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TITLE);
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_PARENT_XLINK_TYPE);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (final Exception e) {
             failException("Lax creating of ou failed. ", e);
         }
         assertXmlValidOrganizationalUnit(createdXml);
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp, startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with 2 md-record elements.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1f() throws Exception {
 
         // final String[] parentValues =
         // createSuccessfully("escidoc_ou_create_2_md_records.xml", 2);
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create_2_md_records.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         // insertParentsElement(toBeCreatedDocument,
         // XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         toBeCreatedDocument = getDocument(toString(toBeCreatedDocument, false));
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (final Exception e) {
             failException("Lax creating of ou failed. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp, startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with parents in state opened.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1g() throws Exception {
 
         final String[] parentValues = createSuccessfully("escidoc_ou_create.xml", 2);
         open(parentValues[0], getTheLastModificationParam(true, parentValues[0], "Opened organizational unit '"
             + parentValues[0] + "'."));
         open(parentValues[1], getTheLastModificationParam(true, parentValues[1], "Opened organizational unit '"
             + parentValues[1] + "'."));
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (final Exception e) {
             failException("Creating OU with parents failed with exception. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp, startTimestamp);
     }
 
     /**
      * Test successfully creating an organizational unit with parents - one in state opened and one in state created.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou1h() throws Exception {
 
         final String[] parentValues = createSuccessfully("escidoc_ou_create.xml", 2);
         open(parentValues[0], getTheLastModificationParam(true, parentValues[0], "Opened organizational unit '"
             + parentValues[0] + "'."));
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (final Exception e) {
             failException("Creating OU with parents failed with exception. ", e);
         }
         assertOrganizationalUnit(createdXml, toBeCreatedXml, startTimestamp, startTimestamp);
     }
 
     /**
      * Test declining creating an organizational unit without providing a title/name.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou2a() throws Exception {
 
         Class<MissingElementValueException> ec = MissingElementValueException.class;
 
         final String toBeCreatedXml =
             toString(deleteElement(getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml"),
                 XPATH_ORGANIZATIONAL_UNIT_TITLE), false);
 
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (final Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating an organizational unit with an invalid xml representation (xml is an item xml).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou2b() throws Exception {
 
         Class<XmlCorruptedException> ec = XmlCorruptedException.class;
 
         final String toBeCreatedXml = getTemplateAsString(TEMPLATE_ITEM_PATH, "escidoc_item_198.xml");
 
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (final Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating an organizational unit with providing an empty name.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou2c() throws Exception {
 
         Document ou = getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         substitute(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE, "");
         final Class<MissingElementValueException> ec = MissingElementValueException.class;
 
         try {
             create(toString(ou, false));
             failMissingException(ec);
         }
         catch (final Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining creating an organizational unit with providing a parent is state closed.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou2d() throws Exception {
 
         final String[] parentValues = createSuccessfully("escidoc_ou_create.xml", 2);
         String closedParent = parentValues[0];
         open(closedParent, getTheLastModificationParam(true, closedParent, "Opened organizational unit '"
             + closedParent + "'."));
         close(closedParent, getTheLastModificationParam(true, closedParent, "Closed organizational unit '"
             + closedParent + "'."));
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
         final Class<InvalidStatusException> ec = InvalidStatusException.class;
         try {
             create(toBeCreatedXml);
             failMissingException(ec);
         }
         catch (final Exception e) {
             assertExceptionType(ec, e);
         }
 
     }
 
     /**
      * Test declining creating an organizational unit without an xml representation.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOumCou4() throws Exception {
 
         Class<MissingMethodParameterException> ec = MissingMethodParameterException.class;
         try {
             create(null);
             failMissingException(ec);
         }
         catch (final Exception e) {
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test successfully creating an organizational unit with a name of an existing organizational unit in another scope
      * of the parents.
      *
      * @throws Exception If anything fails.
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
         Document childOu1Document = getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(childOu1Document, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         insertParentsElement(childOu1Document, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, new String[] { idParentOu1,
             titleParentOu1 }, false);
         final String ouName = selectSingleNode(childOu1Document, XPATH_ORGANIZATIONAL_UNIT_TITLE).getTextContent();
         try {
             create(toString(childOu1Document, false));
         }
         catch (final Exception e) {
             failException("Failure during init, creation of first child failed.", e);
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
         // catch (final Exception e) {
         // failException("creating 2nd child ou with same name but in"
         // + " another scope failed with exception.", e);
         // }
         // assertOrganizationalUnit(createdChildOu2Xml, toBeCreatedChildOu2Xml,
         // startTimestamp, startTimestamp);
     }
 
     /**
      * Test if the last modification date of an fresh created OU equals the last-modification-date of the afterwards
      * retrieved OU.
      *
      * @throws Exception Thrown if last-modification-date between create and retrieve differs.
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
      * Test unexpected parser exception instead of InvalidXmlException during create (see issue INFR-911).
      *
      * @throws Exception Thrown if behavior is not as expected.
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
      * Test creating an Organizational Unit with missing required default md-record. It's checked if the expected
      * Exception is thrown.
      * <p/>
      * See issue INFR-1016
      *
      * @throws Exception If anything fails.
      */
     @Test(expected = MissingMdRecordException.class)
     public void testOuCreateWithoutEscidocMdRecord() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
 
         substitute(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS + "/md-record/@name", "non_default_name");
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         create(toBeCreatedXml);
     }
 
     /**
      * Test creating an Organizational Unit with no md-record given. It's checked if the expected
      * Exception is thrown.
      * <p/>
      * See issue INFR-1016
      *
      * @throws Exception If anything fails.
      */
    @Test(expected = XmlSchemaValidationException.class)
     public void testOuCreateWithoutMdRecord() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
 
         deleteNodes(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS + "/md-record");
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         create(toBeCreatedXml);
     }
 
 }
