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
 package de.escidoc.core.test.om.contentRelation;
 
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 
 /**
  * Test content relation create implementation.
  * 
  * @author SWA
  * 
  */
 @RunWith(value = Parameterized.class)
 public class ContentRelationCreateTest extends ContentRelationTestBase {
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public ContentRelationCreateTest(final int transport) {
         super(transport);
     }
 
     /**
      * Test create content relation.
      * 
      * @throws Exception
      *             Thrown if deleting failed.
      */
     @Test
     public void testCreate01() throws Exception {
 
         String contentRelationXml =
             getExampleTemplate("content-relation-01.xml");
         String xml = create(contentRelationXml);
         Document createDoc = getDocument(xml);
         String relationId = getObjidValue(xml);
 
         String retrieveXml = retrieve(relationId);
 
         // check values of retrieved ContentRelation
         assertXmlValidContentRelation(retrieveXml);
         Document retrieveDoc = getDocument(retrieveXml);
 
         // compare description --------------------------------------
         String origDescrValue =
             selectSingleNode(createDoc,
                 "/content-relation/properties/description").getTextContent();
         String descriptionValue =
             selectSingleNode(retrieveDoc,
                 "/content-relation/properties/description").getTextContent();
 
         assertEquals(origDescrValue, descriptionValue);
 
         // compare subject ------------------------------------------
         String origSubjectValue = null;
         String subjectValue = null;
 
         if (Constants.TRANSPORT_REST == getTransport()) {
             origSubjectValue =
                 selectSingleNode(createDoc, "/content-relation/subject/@href")
                     .getNodeValue();
             subjectValue =
                 selectSingleNode(retrieveDoc, "/content-relation/subject/@href")
                     .getNodeValue();
         }
         else {
             origSubjectValue =
                 selectSingleNode(createDoc, "/content-relation/subject/@objid")
                     .getNodeValue();
             subjectValue =
                 selectSingleNode(retrieveDoc,
                     "/content-relation/subject/@objid").getNodeValue();
         }
         assertEquals(origSubjectValue, subjectValue);
 
         // compare subject ------------------------------------------
         String origObjValue = null;
         String objValue = null;
 
         if (Constants.TRANSPORT_REST == getTransport()) {
             origObjValue =
                 selectSingleNode(createDoc, "/content-relation/object/@href")
                     .getNodeValue();
             objValue =
                 selectSingleNode(retrieveDoc, "/content-relation/object/@href")
                     .getNodeValue();
         }
         else {
             origObjValue =
                 selectSingleNode(createDoc, "/content-relation/object/@objid")
                     .getNodeValue();
             objValue =
                 selectSingleNode(retrieveDoc, "/content-relation/object/@objid")
                     .getNodeValue();
         }
         assertEquals(origObjValue, objValue);
 
         // compare meta data
 
         // validate creator, modifier, creation-date, ...
     }
 
     /**
      * Test if create content relation throws the right exception if xml is
      * invalid.
      * 
      * @throws Exception
      *             Thrown if deleting failed.
      */
     @Test
     public void testCreate02() throws Exception {
 
         try {
             create("");
         }
         catch (Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec.getName()
                 + " expected.", ec, e);
         }
     }
 
     /**
      * Test unexpected parser exception instead of InvalidXmlException during
      * create (see issue INFR-911).
      * 
      * @throws Exception
      *             Thrown if behavior is not as expected.
      */
     @Test
     public void testInvalidXml() throws Exception {
 
         /*
          * The infrastructure has thrown an unexpected parser exception during
          * creation if a non XML datastructur is send (e.g. String).
          */
         try {
             create("laber-rababer");
             fail("Missing Invalid XML exception");
         }
         catch (InvalidXmlException e) {
             // that's ok
         }
     }
 
     /**
      * Test if md-record attributes md-type and schema are not delivered if
      * unset.
      * 
     * See issue INFR-??
      * 
      * @throws Exception
      *             Thrown if deleting failed.
      */
     @Test
     public void setOfMdRecordAttributes() throws Exception {
 
         String contentRelationXml =
             getExampleTemplate("content-relation-01.xml");
         String xml = create(contentRelationXml);
         assertXmlValidContentRelation(xml);
 
         Document createDoc = getDocument(xml);
         Node value =
             selectSingleNode(createDoc,
                 "/content-relation/md-records/md-record/@md-type");
         assertNull(value);
         value =
             selectSingleNode(createDoc,
                 "/content-relation/md-records/md-record/@schema");
         assertNull(value);
 
     }
 
 }
