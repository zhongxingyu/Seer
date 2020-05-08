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
 package de.escidoc.core.test.st;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.InputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.StagingFileNotFoundException;
 import de.escidoc.core.test.EntityUtil;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.EscidocTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.HttpHelper;
 import de.escidoc.core.test.security.client.PWCallback;
 
 /**
  * Test suite for the StagingFile.
  * 
  * @author Torsten Tetteroo
  */
 public class StagingFileIT extends StagingFileTestBase {
 
     private final String testUploadFile = "UploadTest.zip";
 
     private final String testUploadFileMimeType = "application/zip";
 
     /**
      * Test successfully creating a StagingFile.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testSTCsf1() throws Exception {
 
         InputStream fileInputStream = retrieveTestData(testUploadFile);
 
         HttpResponse httpRes = null;
         try {
             httpRes = create(fileInputStream, testUploadFileMimeType, testUploadFile);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertNotNull("No HTTPMethod. ", httpRes);
         assertHttpStatusOfMethod("Create failed", httpRes);
         final String stagingFileXml = EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8);
 
         EscidocAbstractTest.assertXmlValidStagingFile(stagingFileXml);
         Document document = EscidocAbstractTest.getDocument(stagingFileXml);
         assertXmlExists("No xlink type", document, "/staging-file/@type");
         assertXmlExists("No xlink href", document, "/staging-file/@href");
         assertXmlExists("No last modification date", document, "/staging-file/@last-modification-date");
     }
 
     /**
      * Test declining the creation of a StagingFile without binary content.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testSTCsf2() throws Exception {
 
         create(null, testUploadFileMimeType, testUploadFile);
     }
 
     /**
      * Test successfully retrieving staging file.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testSTRsf1() throws Exception {
 
         InputStream fileInputStream = retrieveTestData(testUploadFile);
         HttpResponse httpRes = null;
         try {
             httpRes = create(fileInputStream, testUploadFileMimeType, testUploadFile);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertNotNull("No HTTPMethod. ", httpRes);
         assertHttpStatusOfMethod("Create failed", httpRes);
         Document document = EscidocAbstractTest.getDocument(EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8));
 
         String objidValue = getIdFromRootElementHref(document);
 
         try {
             PWCallback.setHandle("");
             httpRes = retrieveStagingFile(objidValue);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         finally {
             PWCallback.resetHandle();
         }
         assertNotNull("Got no HTTP method object", httpRes);
         assertHttpStatusOfMethod("Retrieve failed", httpRes);
         final Header contentTypeHeader = httpRes.getFirstHeader(HttpHelper.HTTP_HEADER_CONTENT_TYPE);
         assertNotNull("Retrieve failed! No returned mime type found", contentTypeHeader);
         assertEquals("Retrieve failed! The returned mime type is wrong,", testUploadFileMimeType, contentTypeHeader
             .getValue());
         StagingFileTestBase.assertResponseContentMatchesSourceFile(httpRes, retrieveTestData(testUploadFile));
 
     }
 
     /**
      * Test declining the retrieval of a StagingFile with missing parameter token.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testSTRsf2() throws Exception {
 
         retrieveStagingFile(null);
     }
 
     /**
      * Test declining the retrieval of a StagingFile with unknown token.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = StagingFileNotFoundException.class)
     public void testSTRsf4() throws Exception {
 
         retrieveStagingFile(UNKNOWN_ID);
     }
 
     /**
      * Test declining the retrieval of a StagingFile with providing the id of an existing resource of another type.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test(expected = StagingFileNotFoundException.class)
     public void testSTRsf4_2() throws Exception {
 
         retrieveStagingFile(CONTEXT_ID);
     }
 
     /**
      * Test declining the retrieval of a staging file that has been previously retrieved.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testSTRsf8() throws Exception {
 
         InputStream fileInputStream = retrieveTestData(testUploadFile);
         HttpResponse httpRes = null;
         try {
             httpRes = create(fileInputStream, testUploadFileMimeType, testUploadFile);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertNotNull("No HTTPMethod. ", httpRes);
         assertHttpStatusOfMethod("Create failed", httpRes);
         Document document = EscidocAbstractTest.getDocument(EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8));
 
         String objidValue = getIdFromRootElementHref(document);
 
         try {
             httpRes = retrieveStagingFile(objidValue);
             EntityUtil.getContent(httpRes.getEntity()).close();
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
 
         try {
             httpRes = retrieveStagingFile(objidValue);
             EntityUtil.getContent(httpRes.getEntity()).close();
             EscidocAbstractTest.failMissingException("Upload Servlet's get method did not decline"
                 + " repeated retrieval of a staging file, ", StagingFileNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("Upload Servlet's get method did not decline"
                 + " repeated retrieval of a staging file, correctly, ", StagingFileNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test successfully creating a StagingFile.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testCreateItemWithStagingFileLink() throws Exception {
 
         InputStream fileInputStream = retrieveTestData(testUploadFile);
 
         HttpResponse httpRes = null;
         try {
             httpRes = create(fileInputStream, testUploadFileMimeType, testUploadFile);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertNotNull("No HTTPMethod. ", httpRes);
         assertHttpStatusOfMethod("Create failed", httpRes);
         final String stagingFileXml = EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8);
 
         EscidocAbstractTest.assertXmlValidStagingFile(stagingFileXml);
         Document document = EscidocAbstractTest.getDocument(stagingFileXml);
         String stagingFileHref =
             Constants.PROTOCOL + "://" + EscidocTestBase.getBaseHost() + ":" + EscidocTestBase.getBasePort()
                + selectSingleNode(document, "/staging-file/@href").getTextContent();
 
         Document itemDoc =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ST_ITEM_PATH, "escidoc_item_for_staging.xml");
         substitute(itemDoc, "/item/components/component/content/@href", stagingFileHref);
 
         try {
             getItemClient().create(toString(itemDoc, false));
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
 
     }
 
 }
