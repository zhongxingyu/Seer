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
 package de.escidoc.core.test.om.container;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.http.HttpResponse;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.interfaces.ResourceHandlerClientInterface;
 import de.escidoc.core.test.om.OmTestBase;
 
 /**
  * Test the handler of the container resource.
  *
  * @author Michael Schneider
  */
 public class ContainerTestBase extends OmTestBase {
 
     protected static final String XPATH_CONTAINER = "/container";
 
     protected static final String XPATH_CONTAINER_XLINK_HREF = XPATH_CONTAINER + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_XLINK_TYPE = XPATH_CONTAINER + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_XLINK_TITLE = XPATH_CONTAINER + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_XML_BASE = XPATH_CONTAINER + "/@base";
 
     protected static final String XPATH_CONTAINER_PROPERTIES = XPATH_CONTAINER + "/properties";
 
     public static final String XPATH_CONTAINER_PROPERTIES_CMS =
         XPATH_CONTAINER_PROPERTIES + "/" + "content-model-specific";
 
     protected static final String XPATH_CONTAINER_PROPERTIES_XLINK_HREF = XPATH_CONTAINER_PROPERTIES + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_PROPERTIES_XLINK_TYPE = XPATH_CONTAINER_PROPERTIES + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_PROPERTIES_XLINK_TITLE =
         XPATH_CONTAINER_PROPERTIES + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_CONTENT_TYPE = XPATH_CONTAINER_PROPERTIES + "/content-model";
 
     protected static final String XPATH_CONTAINER_CONTENT_TYPE_XLINK_HREF =
         XPATH_CONTAINER_CONTENT_TYPE + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_CONTENT_TYPE_XLINK_TYPE =
         XPATH_CONTAINER_CONTENT_TYPE + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_CONTENT_TYPE_XLINK_TITLE =
         XPATH_CONTAINER_CONTENT_TYPE + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_CONTEXT = XPATH_CONTAINER_PROPERTIES + "/context";
 
     protected static final String XPATH_CONTAINER_CONTEXT_XLINK_HREF = XPATH_CONTAINER_CONTEXT + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_CONTEXT_XLINK_TYPE = XPATH_CONTAINER_CONTEXT + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_CONTEXT_XLINK_TITLE = XPATH_CONTAINER_CONTEXT + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_CREATOR = XPATH_CONTAINER_PROPERTIES + "/creator";
 
     protected static final String XPATH_CONTAINER_CREATOR_XLINK_HREF = XPATH_CONTAINER_CREATOR + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_CREATOR_XLINK_TYPE = XPATH_CONTAINER_CREATOR + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_CREATOR_XLINK_TITLE = XPATH_CONTAINER_CREATOR + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_CURRENT_VERSION = XPATH_CONTAINER_PROPERTIES + "/version";
 
     public static final String XPATH_CONTAINER_CURRENT_VERSION_STATUS =
         XPATH_CONTAINER_CURRENT_VERSION + "/" + NAME_VERSION_STATUS;
 
     protected static final String XPATH_CONTAINER_CURRENT_VERSION_XLINK_HREF =
         XPATH_CONTAINER_CURRENT_VERSION + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_CURRENT_VERSION_XLINK_TYPE =
         XPATH_CONTAINER_CURRENT_VERSION + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_CURRENT_VERSION_XLINK_TITLE =
         XPATH_CONTAINER_CURRENT_VERSION + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_LATEST_RELEASE = XPATH_CONTAINER_PROPERTIES + "/latest-release";
 
     protected static final String XPATH_CONTAINER_LATEST_RELEASE_XLINK_HREF =
         XPATH_CONTAINER_LATEST_RELEASE + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_LATEST_RELEASE_XLINK_TYPE =
         XPATH_CONTAINER_LATEST_RELEASE + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_LATEST_RELEASE_XLINK_TITLE =
         XPATH_CONTAINER_LATEST_RELEASE + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_LATEST_VERSION = XPATH_CONTAINER_PROPERTIES + "/latest-version";
 
     protected static final String XPATH_CONTAINER_LATEST_VERSION_XLINK_HREF =
         XPATH_CONTAINER_LATEST_VERSION + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_LATEST_VERSION_XLINK_TYPE =
         XPATH_CONTAINER_LATEST_VERSION + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_LATEST_VERSION_XLINK_TITLE =
         XPATH_CONTAINER_LATEST_VERSION + PART_XLINK_TITLE;
 
     public static final String XPATH_CONTAINER_STATUS = XPATH_CONTAINER_PROPERTIES + "/" + NAME_PUBLIC_STATUS;
 
     protected static final String XPATH_CONTAINER_MD_RECORDS = XPATH_CONTAINER + "/md-records";
 
     protected static final String XPATH_CONTAINER_MD_RECORDS_XLINK_HREF = XPATH_CONTAINER_MD_RECORDS + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_MD_RECORDS_XLINK_TYPE = XPATH_CONTAINER_MD_RECORDS + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_MD_RECORDS_XLINK_TITLE =
         XPATH_CONTAINER_MD_RECORDS + PART_XLINK_TITLE;
 
     protected static final String XPATH_CONTAINER_MD_RECORD = XPATH_CONTAINER_MD_RECORDS + "/md-record";
 
     protected static final String XPATH_CONTAINER_MD_RECORD_SCHEMA = XPATH_CONTAINER_MD_RECORD + "/@schema";
 
     protected static final String XPATH_CONTAINER_MD_RECORD_XLINK_HREF = XPATH_CONTAINER_MD_RECORD + PART_XLINK_HREF;
 
     protected static final String XPATH_CONTAINER_MD_RECORD_XLINK_TYPE = XPATH_CONTAINER_MD_RECORD + PART_XLINK_TYPE;
 
     protected static final String XPATH_CONTAINER_MD_RECORD_XLINK_TITLE = XPATH_CONTAINER_MD_RECORD + PART_XLINK_TITLE;
 
     private final String containerUrl;
 
     public ContainerTestBase() {
         containerUrl = getFrameworkUrl() + "/ir/container/";
     }
 
     /**
      * @return Returns the containerClient
      */
     @Override
     public ResourceHandlerClientInterface getClient() {
 
         return getContainerClient();
     }
 
     /**
      * Test retrieving the properties of an Container.
      *
      * @param id The id of the container.
      * @return The retrieved properties.
      * @throws Exception If anything fails.
      */
     protected String retrieveProperties(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveProperties(id));
     }
 
     /**
      * Test retrieving the list of containers.
      *
      * @param filter CQL filter
      * @return The retrieved components.
      * @throws Exception If anything fails.
      */
     protected String retrieveContainers(final Map<String, String[]> filter) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveContainers(filter));
     }
 
     public String retrieveMembers(final String id, final Map<String, String[]> filter) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveMembers(id, filter));
     }
 
     public String retrieveTocs(final String id, final Map<String, String[]> filter) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveTocs(id, filter));
     }
 
     protected String retrieveToc(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveToc(id));
     }
 
     protected void deleteToc(final String id) throws Exception {
 
         Object result = getContainerClient().deleteToc(id);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
     }
 
     protected String updateToc(final String id, final String xml) throws Exception {
 
         return handleXmlResult(getContainerClient().updateToc(id, xml));
     }
 
     protected String retrieveTocView(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveTocView(id));
     }
 
     /**
      * Test retrieving the metadata records of an Container.
      *
      * @param id The id of the container.
      * @return The retrieved metadata records.
      * @throws Exception If anything fails.
      */
     protected String retrieveMetadataRecords(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveMetaDataRecords(id));
     }
 
     /**
      * Test retrieving the metadata records of an Container.
      *
      * @param id           The id of the container.
      * @param mdRecordname The name of the md-record.
      * @return The retrieved metadata record.
      * @throws Exception If anything fails.
      */
     protected String retrieveMetadataRecord(final String id, final String mdRecordname) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveMdRecord(id, mdRecordname));
     }
 
     /**
      * Test updating a metadata record of an Container.
      *
      * @param id           The id of the container.
      * @param mdRecordName The name of the metadata record.
      * @param mdRecord     The updated metadata record.
      * @return The created metadata record.
      * @throws Exception If anything fails.
      */
     protected String updateMetadataRecord(final String id, final String mdRecordName, final String mdRecord)
         throws Exception {
 
         return handleXmlResult(getContainerClient().updateMetaDataRecord(id, mdRecordName, mdRecord));
     }
 
     /**
      * Test retrieving parents of an Container.
      *
      * @param id The id of the container.
      * @return The retrieved parents.
      * @throws Exception If anything fails.
      */
     protected String retrieveParents(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveParents(id));
     }
 
     /**
      * Test retrieving the resources of an Container.
      *
      * @param id The id of the container.
      * @return The retrieved resources.
      * @throws Exception If anything fails.
      */
     @Override
     public String retrieveResources(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveResources(id));
     }
 
     /**
      * Test retrieving the resources of an Container.
      *
      * @param id The id of the container.
      * @param methodName The name of the requested method 
      * @return The retrieved resources.
      * @throws Exception If anything fails.
      */
     public String retrieveResource(final String id, final String methodName) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveResource(id, methodName));
     }
 
     /**
      * Test retrieving the struct-map of a Container.
      *
      * @param id The id of the container.
      * @return struct map XML
      * @throws Exception If anything fails.
      */
     public String retrieveStructMap(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveStructMap(id));
     }
 
     /**
      * Test retrieving the version history of a container.
      *
      * @param id The id of the container.
      * @return The retrieved version history.
      * @throws Exception If anything fails.
      */
     public String retrieveVersionHistory(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveVersionHistory(id));
     }
 
     /**
      * Test retrieving the relations of an Item.
      *
      * @param id The id of the container.
      * @return The retrieved relations.
      * @throws Exception If anything fails.
      */
     public String retrieveRelations(final String id) throws Exception {
 
         return handleXmlResult(getContainerClient().retrieveRelations(id));
     }
 
     public String addMembers(final String id, final String taskParam) throws Exception {
         return handleXmlResult(getContainerClient().addMembers(id, taskParam));
     }
 
     public String addTocs(final String id, final String taskParam) throws Exception {
         return handleXmlResult(getContainerClient().addTocs(id, taskParam));
     }
 
     protected String removeMembers(final String id, final String taskParam) throws Exception {
 
         return handleXmlResult(getContainerClient().removeMembers(id, taskParam));
     }
 
     protected String createItem(final String id, final String itemXml) throws Exception {
 
         return handleXmlResult(getContainerClient().createItem(id, itemXml));
     }
 
     /**
      * Create Container as method from Container.
      *
      * @param id           The id of the Container.
      * @param containerXml The xml data of the new to create Container
      * @return The XML representation of the new created Container.
      * @throws Exception Thrown if creation failed.
      */
     protected String createContainer(final String id, final String containerXml) throws Exception {
 
         return handleXmlResult(getContainerClient().createContainer(id, containerXml));
     }
 
     /**
      * Test releasing a Container.
      *
      * @param id    The id of the container.
      * @param param The param indicating the last-modifiaction-date of the Item.
      * @throws Exception If anything fails.
      */
     public String release(final String id, final String param) throws Exception {
 
         // return releaseWithPid(id, param);
         return releaseWithoutPid(id, param);
     }
 
     /**
      * Test releasing a Container and assign (if necessary the version and/or object PIDs).
      *
      * @param id The id of the Container.
      * @throws Exception If anything fails.
      */
     public String releaseWithPid(final String id) throws Exception {
 
         Document itemDoc = null;
 
         if (!getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")
             || !getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
             itemDoc = EscidocAbstractTest.getDocument(retrieve(id));
         }
 
         // assign objectPid
         if (!getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")) {
             // prevent re-assigning
             Node pid = selectSingleNode(itemDoc, XPATH_CONTAINER_OBJECT_PID);
             if (pid == null) {
                 String itemId = getObjidWithoutVersion(id);
                 String pidParam = getPidParam(id, containerUrl + itemId);
                 assignObjectPid(id, pidParam);
             }
         }
 
         // assign versionPid
         if (!getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
 
             // prevent re-assigning
             Node pid = selectSingleNode(itemDoc, XPATH_ITEM_VERSION_PID);
             if (pid == null) {
                 String versionNumber = getVersionNumber(id);
                 String versionId = id;
                 if (versionNumber == null) {
                     versionId = getLatestVersionObjidValue(itemDoc);
                 }
                 String pidParam = getPidParam(versionId, containerUrl + versionId);
                 assignVersionPid(versionId, pidParam);
             }
         }
 
         itemDoc = EscidocAbstractTest.getDocument(retrieve(id));
         String param = getTaskParam(getLastModificationDateValue(itemDoc));
 
         Object result = getContainerClient().release(id, param);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     /**
      * Test releasing an Container.
      *
      * @param id    The id of the Container.
      * @param param The param indicating the last-modifiaction-date of the Item.
      * @throws Exception If anything fails.
      */
     public String releaseWithoutPid(final String id, final String param) throws Exception {
 
         Object result = getContainerClient().release(id, param);
 
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     /**
      * Test revising a container.
      *
      * @param id    The id of the container.
      * @param param The param indicating the last-modification-date of the Container.
      * @throws Exception If anything fails.
      */
     public String revise(final String id, final String param) throws Exception {
 
         Object result = getContainerClient().revise(id, param);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     /**
      * Test submiting a container.
      *
      * @param id    The id of the container.
      * @param param The param indicating the last-modifiaction-date of the Item.
      * @throws Exception If anything fails.
      */
     public String submit(final String id, final String param) throws Exception {
 
         Object result = getContainerClient().submit(id, param);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
 
         return handleXmlResult(result);
     }
 
     /**
      * Test withdrawing a container.
      *
      * @param id    The id of the container.
      * @param param The param indicating the last-modifiaction-date of the Item.
      * @throws Exception If anything fails.
      */
     public String withdraw(final String id, final String param) throws Exception {
 
         Object result = getContainerClient().withdraw(id, param);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     /**
      * Test locking a container.
      *
      * @param id    The id of the container.
      * @param param The param indicating the last-modifiaction-date of the Item.
      * @throws Exception If anything fails.
      */
     protected String lock(final String id, final String param) throws Exception {
 
         Object result = getContainerClient().lock(id, param);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     /**
      * Test unlocking a container.
      *
      * @param id    The id of the container.
      * @param param The param indicating the last-modifiaction-date of the Item.
      * @throws Exception If anything fails.
      */
     protected String unlock(final String id, final String param) throws Exception {
 
         Object result = getContainerClient().unlock(id, param);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     /**
      *
      * @param id
      * @param taskParam
      * @return
      */
     public String addContentRelations(final String id, final String taskParam) throws Exception {
         Object result = getContainerClient().addContentRelations(id, taskParam);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     protected String addCtsElement(final String xml) throws Exception {
         Document doc = EscidocAbstractTest.getDocument(xml);
         doc =
             (Document) addAfter(doc, "/container/properties/content-model-specific/xxx", createElementNode(doc, null,
                 null, "nox", "modified"));
         String newXml = toString(doc, true);
         return newXml;
     }
 
     /**
      *
      * @param id
      * @param taskParam
      * @return
      */
     public String removeContentRelations(final String id, final String taskParam) throws Exception {
 
         Object result = getContainerClient().removeContentRelations(id, taskParam);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             assertHttpStatusOfMethod("", httpRes);
         }
         return handleXmlResult(result);
     }
 
     /**
      * @param id Container Version Identifier (escidoc:123:2)
      */
     public String assignVersionPid(final String id, final String param) throws Exception {
 
         return handleXmlResult(getContainerClient().assignVersionPid(id, param));
     }
 
     /**
      *
      * @param id
      * @param param
      * @return
      * @throws Exception
      */
     public String assignObjectPid(final String id, final String param) throws Exception {
 
         return handleXmlResult(getContainerClient().assignObjectPid(id, param));
     }
 
     /**
      * Create an Container from template.
      *
      * @param templateName The name of the Container template (file).
      * @return objid of the Item.
      * @throws Exception Thrown if creation of Item or extraction of objid fails.
      */
     public String createContainerFromTemplate(final String templateName) throws Exception {
 
         // create an item and save the id
         String xmlData = getContainerTemplate(templateName);
 
         String theContainerXml = handleXmlResult(getContainerClient().create(xmlData));
         return getObjidValue(theContainerXml);
     }
 
     /**
      * Create an Item from template.
      *
      * @param templateName The name of the Item template (file).
      * @return objid of the Item.
      * @throws Exception Thrown if creation of Item or extraction of objid fails.
      */
     public String createItemFromTemplate(final String templateName) throws Exception {
 
         // create an item and save the id
         String xmlData = EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", templateName);
 
         String theItemXml = handleXmlResult(getItemClient().create(xmlData));
         return getObjidValue(theItemXml);
     }
 
     /**
      * Prepares a container for a test.<br> The container is created and set into the specified state.
      *
      * @param creatorUserHandle    The eSciDoc user handle of the creator.
      * @param status               The status to set for the item. If this is <code>null</code>, no item is created and
      *                             <code>null</code> is returned.
      * @param contextId            id of context to create container in.
      * @param createVersionsBefore If this flag is set to <code>true</code>, before each status change, the item is
      *                             updated to create a new version.
      * @param createVersionsAfter  If this flag is set to <code>true</code>, after each status change, the item is
      *                             updated to create a new version, if this is allowed. Currently, this is not allowed
      *                             for objects in state release or withdrawn.
      * @return Returns the XML representation of the created container. In case of withdrawn container, the released
      *         container is returned.
      * @throws Exception If anything fails.
      */
     protected String prepareContainer(
         final String status, final String contextId, final boolean createVersionsBefore,
         final boolean createVersionsAfter) throws Exception {
 
         if (status == null) {
             return null;
         }
 
         String createdXml = null;
         try {
             createdXml = create(prepareContainerData(contextId));
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertNotNull(createdXml);
 
         if (!STATUS_PENDING.equals(status)) {
             Document document = EscidocAbstractTest.getDocument(createdXml);
             final String objidValue = getObjidValue(document);
             if (createVersionsBefore) {
                 createdXml = createdXml.replaceAll("the title", "the title - updated");
                 createdXml = update(objidValue, createdXml);
                 document = EscidocAbstractTest.getDocument(createdXml);
             }
             submit(objidValue, getTaskParam(getLastModificationDateValue(document)));
             createdXml = retrieve(objidValue);
             if (createVersionsAfter) {
                 createdXml = createdXml.replaceAll("the title", "the title - updated");
                 createdXml = update(objidValue, createdXml);
             }
             if (!STATUS_SUBMITTED.equals(status)) {
                 if (createVersionsBefore) {
                     createdXml = createdXml.replaceAll("the title", "the title - updated");
                     createdXml = update(objidValue, createdXml);
                 }
                 document = EscidocAbstractTest.getDocument(createdXml);
                 releaseWithPid(objidValue);
                 createdXml = retrieve(objidValue);
                 if (!STATUS_RELEASED.equals(status)) {
                     if (createVersionsBefore) {
                         createdXml = createdXml.replaceAll("the title", "the title - updated");
                         createdXml = update(objidValue, createdXml);
                     }
                     document = EscidocAbstractTest.getDocument(createdXml);
                     final String taskParam =
                         getWithdrawTaskParam(getLastModificationDateValue(document), "Some withdraw comment");
                     withdraw(getObjidValue(document), taskParam);
                     createdXml = retrieve(objidValue);
                 }
             }
         }
 
         return createdXml;
     }
 
     /**
      * Prepares the data for a container.
      *
      * @param contextId context to create container in
      * @return Returns the xml representation of a container. Depending on the current set transport of the class, the
      *         data is created by either using the template file create_container_WithoutMembers_Restv1.1.xml or by
      *         using the template file create_container_WithoutMembers_Soapv1.1.xml
      * @throws Exception If anything fails.
      */
     private String prepareContainerData(final String contextId) throws Exception {
 
         Document xmlContainer =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_CONTAINER_PATH + "/rest",
                 "create_container_WithoutMembers_v1.1.xml");
         if (contextId != null && !contextId.equals("")) {
             String contextHref = Constants.CONTEXT_BASE_URI + "/" + contextId;
             substitute(xmlContainer, "/container/properties/context/@href", contextHref);
         }
         return toString(xmlContainer, false);
     }
 
     /**
      * Get a Container template. The template is pulled automatically from the rest/soap directory of the container
      * template basedir.
      *
      * @param templateName The name of the Container template (file).
      * @return The String representation of the template.
      * @throws Exception Thrown if anything fails.
      */
     public String getContainerTemplate(final String templateName) throws Exception {
 
         return EscidocAbstractTest.getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest", templateName);
 
     }
 
     /**
      * Get a Item template. The template is pulled automatically from the rest/soap directory of the Item template
      * basedir.
      *
      * @param templateName The name of the Item template (file).
      * @return The String representation of the template.
      * @throws Exception Thrown if anything fails.
      */
     public String getItemTemplate(final String templateName) throws Exception {
 
         return EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", templateName);
 
     }
 
     /**
      * Convert a NodeList to a List.
      *
      * @param nl The NodeList.
      * @return List
      */
     public List<String> nodeList2List(final NodeList nl) {
 
         List<String> list = new Vector<String>();
         for (int i = nl.getLength() - 1; i >= 0; i--) {
             list.add(nl.item(i).getNodeValue());
         }
         return list;
     }
 
     /**
      * Compares if the content of two lists equals.
      * <p/>
      * WARNING: side effect, elements are removed from the second list
      *
      * @param msg  Message for the exception.
      * @param arg0 List one.
      * @param arg1 List two.
      */
     public void assertListContentEqual(final String msg, final List<String> arg0, final List<String> arg1) {
 
         Iterator<String> it = arg0.iterator();
         while (it.hasNext()) {
             String member = (String) it.next();
             if (arg1.contains(member)) {
                 arg1.remove(member);
             }
             else {
                 fail(msg + " (" + member + ")");
             }
         }
         if (!arg1.isEmpty()) {
             fail(msg);
         }
     }
 
     /**
      * Compares if the object is contained in given list.
      * <p/>
      *
      * @param msg  Message for the exception.
      * @param arg0 List one.
      * @param arg1 List two.
      */
     public void assertListContains(final String msg, final List<String> arg0, final String arg1) {
         if (!arg0.contains(arg1)) {
             fail(msg);
         }
     }
 
     /**
      * Prepare the PIDs of Container. Depending on configuration must have a Container an object and a version Pid
      * before you can release it.
      *
      * @param containerId The id of the Container.
      * @param lmd         The last modification date of the Container.
      * @return The new last modification date of the Container. The return last modification date equals the param last
      *         modification date if the Container resource was not altered.
      * @throws Exception Thrown if pid assignment failed.
      */
     public String prepareContainerPid(final String containerId, final String lmd) throws Exception {
 
         String newLmd = lmd;
         String objectPidXml = null;
         String versionPidXml = null;
        String containerXml = retrieve(containerId);
 
         String pidParam;
         // assign pid to member (Container)
         if (getContainerClient().getPidConfig("cmm.Container.objectPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")) {

            // check if container has already object pid
            if (selectSingleNode(getDocument(containerXml), "/container/properties/pid") == null) {

                pidParam =
                    getPidParam2(new DateTime(newLmd, DateTimeZone.UTC), new URL("http://somewhere" + containerId));
                objectPidXml = handleXmlResult(getContainerClient().assignObjectPid(containerId, pidParam));
                assertXmlValidResult(objectPidXml);
                newLmd = getLastModificationDateValue(getDocument(objectPidXml));
            }
         }
         if (getContainerClient().getPidConfig("cmm.Container.versionPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
 
            // check if this version of container has already version pid
            if (selectSingleNode(getDocument(containerXml), "/container/properties/version/pid") == null) {

                pidParam =
                    getPidParam2(new DateTime(newLmd, DateTimeZone.UTC), new URL("http://somewhere" + containerId));
                versionPidXml = handleXmlResult(getContainerClient().assignVersionPid(containerId, pidParam));
                assertXmlValidResult(versionPidXml);
                newLmd = getLastModificationDateValue(getDocument(versionPidXml));
            }
         }
 
         return newLmd;
     }
 
     /**
      * Prepare the release of Item. Depending on configuration must have a Item an object and a version Pid before you
      * can release it.
      *
      * @param itemId The id of the Item.
      * @param lmd    The lastmodification date of the Item.
      * @return The new last modification date of the Item. The return last modification date equals the param last
      *         modification date if the Item resource was not altered.
      * @throws Exception Thrown if pid assignment failed.
      */
     public String prepareItemPid(final String itemId, final String lmd) throws Exception {
 
         String newLmd = lmd;
         String objectPidXml = null;
         String versionPidXml = null;
 
         String pidParam;
         // assign pid to member (item)
         if (getItemClient().getPidConfig("cmm.Item.objectPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
             pidParam = getPidParam2(new DateTime(newLmd, DateTimeZone.UTC), new URL("http://somewhere" + itemId));
 
             objectPidXml = handleXmlResult(getItemClient().assignObjectPid(itemId, pidParam));
             assertXmlValidResult(objectPidXml);
             newLmd = getLastModificationDateValue(getDocument(objectPidXml));
         }
         if (getItemClient().getPidConfig("cmm.Item.versionPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
 
             pidParam = getPidParam2(new DateTime(newLmd, DateTimeZone.UTC), new URL("http://somewhere" + itemId));
 
             versionPidXml = handleXmlResult(getItemClient().assignVersionPid(itemId, pidParam));
             assertXmlValidResult(versionPidXml);
             newLmd = getLastModificationDateValue(getDocument(versionPidXml));
         }
 
         return newLmd;
     }
 
 }
