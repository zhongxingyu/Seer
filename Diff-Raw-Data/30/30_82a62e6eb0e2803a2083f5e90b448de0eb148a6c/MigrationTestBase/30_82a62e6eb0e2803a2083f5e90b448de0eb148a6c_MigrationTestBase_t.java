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
  * Copyright 2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.migration;
 
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.client.servlet.aa.RoleClient;
 import de.escidoc.core.test.common.client.servlet.aa.UserAccountClient;
 import de.escidoc.core.test.common.client.servlet.cmm.ContentModelClient;
 import de.escidoc.core.test.common.client.servlet.om.ContainerClient;
 import de.escidoc.core.test.common.client.servlet.om.ContextClient;
 import de.escidoc.core.test.common.client.servlet.om.ItemClient;
 import de.escidoc.core.test.common.client.servlet.oum.OrganizationalUnitClient;
 import de.escidoc.core.test.common.fedora.TripleStoreTestBase;
 import de.escidoc.core.test.common.resources.ResourceProvider;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 /**
  * @author Michael Schneider
  */
 public class MigrationTestBase extends EscidocAbstractTest {
 
     private static final String OBJECT_ID_PATH = "build/migration";
 
     private static final String OBJECT_ID_FILENAME = "object-ids.properties";
 
     private static final String CHECK_RESULT_PATH = "build/migration/check";
 
     private final ContainerClient containerClient = new ContainerClient();
 
     private final ContentModelClient contentModelClient = new ContentModelClient();
 
     private final ContextClient contextClient = new ContextClient();
 
     private final ItemClient itemClient = new ItemClient();
 
     private final OrganizationalUnitClient organizationalUnitClient = new OrganizationalUnitClient();
 
     private final RoleClient roleClient = new RoleClient();
 
     private final UserAccountClient userAccountClient = new UserAccountClient();
 
     static final String OBJECT_TYPE_FED_2_CONTAINER = "container";
 
     static final String OBJECT_TYPE_FED_2_CONTENT_MODEL = "content-model";
 
     static final String OBJECT_TYPE_FED_2_CONTEXT = "context";
 
     static final String OBJECT_TYPE_FED_2_ITEM = "item";
 
     static final String OBJECT_TYPE_FED_2_ORGANIZATIONAL_UNIT = "organizational-unit";
 
     static final String OBJECT_TYPE_ROLE = "role";
 
     static final String OBJECT_TYPE_USER_ACCOUNT = "user-account";
 
     static final String FAILING = "failing";
 
     private static final int SLEEP_TIME = 100;
 
     /**
      * Check the given container ids if they are retrievable.
      *
      * @param ids      The list of ids.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The list of ids for which the retrieval/validation failed.
      */
     public Collection<String> checkContainers(final Collection<String> ids, final boolean validate) {
 
         Collection<String> result = new Vector<String>();
 
         Iterator<String> idIter = ids.iterator();
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 retrieveContainer(id, validate);
             }
             catch (final Exception e) {
                 result.add(id);
             }
         }
         sleep();
         return result;
     }
 
     /**
      * Check the given context ids if they are retrievable.
      *
      * @param ids      The list of ids.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The list of ids for which the retrieval/validation failed.
      */
     public Collection<String> checkContexts(final Collection<String> ids, final boolean validate) {
 
         Collection<String> result = new Vector<String>();
 
         Iterator<String> idIter = ids.iterator();
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 retrieveContext(id, validate);
             }
             catch (final Exception e) {
                 result.add(id);
             }
         }
         sleep();
         return result;
     }
 
     /**
      * Check the given content model ids if they are retrievable.
      *
      * @param ids The list of ids.
      * @return The list of ids for which the retrieval/validation failed.
      */
     public Collection<String> checkContentModels(final Collection<String> ids) {
 
         Collection<String> result = new Vector<String>();
 
         Iterator<String> idIter = ids.iterator();
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 retrieveContentModel(id);
             }
             catch (final Exception e) {
                 result.add(id);
             }
         }
         sleep();
         return result;
     }
 
     /**
      * Check the given item ids if they are retrievable.
      *
      * @param ids      The list of ids.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The list of ids for which the retrieval/validation failed.
      */
     public Collection<String> checkItems(final Collection<String> ids, final boolean validate) {
 
         Collection<String> result = new Vector<String>();
 
         Iterator<String> idIter = ids.iterator();
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 retrieveItem(id, validate);
             }
             catch (final Exception e) {
                 result.add(id);
             }
         }
         sleep();
         return result;
     }
 
     /**
      * Check the given organizational unit ids if they are retrievable.
      *
      * @param ids      The list of ids.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The list of ids for which the retrieval/validation failed.
      */
     public Collection<String> checkOrganizationalUnits(final Collection<String> ids, final boolean validate) {
 
         Collection<String> result = new Vector<String>();
 
         Iterator<String> idIter = ids.iterator();
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 retrieveOrganizationalUnit(id, validate);
             }
             catch (final Exception e) {
                 result.add(id);
             }
         }
         sleep();
         return result;
     }
 
     /**
      * Check the given role ids if they are retrievable.
      *
      * @param ids      The list of ids.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The list of ids for which the retrieval/validation failed.
      */
     public Collection<String> checkRoles(final Collection<String> ids, final boolean validate) {
 
         Collection<String> result = new Vector<String>();
 
         Iterator<String> idIter = ids.iterator();
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 retrieveRole(id, validate);
             }
             catch (final Exception e) {
                 result.add(id);
             }
         }
         return result;
     }
 
     /**
      * Check the given user account ids if they are retrievable.
      *
      * @param ids      The list of ids.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The list of ids for which the retrieval/validation failed.
      */
     public Collection<String> checkUserAccounts(final Collection<String> ids, final boolean validate) {
 
         Collection<String> result = new Vector<String>();
 
         Iterator<String> idIter = ids.iterator();
         while (idIter.hasNext()) {
             String id = idIter.next();
             try {
                 retrieveUserAccount(id, validate);
             }
             catch (final Exception e) {
                 result.add(id);
             }
         }
         return result;
     }
 
     /**
      * Retrieve a Container.
      *
      * @param id       the id.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The retrieved object.
      * @throws Exception If anything fails.
      */
     public String retrieveContainer(final String id, final boolean validate) throws Exception {
         final String result = handleXmlResult(containerClient.retrieve(id));
         if (validate) {
             assertXmlValidContainer(result);
         }
         return result;
     }
 
     /**
      * Retrieve a Context.
      *
      * @param id       the id.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The retrieved object.
      * @throws Exception If anything fails.
      */
     public String retrieveContext(final String id, final boolean validate) throws Exception {
         final String result = handleXmlResult(contextClient.retrieve(id));
         if (validate) {
             assertXmlValidContext(result);
         }
         return result;
     }
 
     /**
      * Retrieve a Content-Model.
      *
      * @param id       the id.
      * @return The retrieved object.
      * @throws Exception If anything fails.
      */
     public String retrieveContentModel(final String id) throws Exception {
         final String result = handleXmlResult(contentModelClient.retrieve(id));
         return result;
     }
 
     /**
      * Retrieve an Item.
      *
      * @param id       the id.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The retrieved object.
      * @throws Exception If anything fails.
      */
     public String retrieveItem(final String id, final boolean validate) throws Exception {
         final String result = handleXmlResult(itemClient.retrieve(id));
         if (validate) {
             assertXmlValidItem(result);
         }
         return result;
     }
 
     /**
      * Retrieve an Organizational-Unit.
      *
      * @param id       the id.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The retrieved object.
      * @throws Exception If anything fails.
      */
     public String retrieveOrganizationalUnit(final String id, final boolean validate) throws Exception {
         final String result = handleXmlResult(organizationalUnitClient.retrieve(id));
         if (validate) {
             assertXmlValidOrganizationalUnit(result);
         }
         return result;
     }
 
     /**
      * Retrieve a Role.
      *
      * @param id       the id.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The retrieved object.
      * @throws Exception If anything fails.
      */
     public String retrieveRole(final String id, final boolean validate) throws Exception {
         final String result = handleXmlResult(roleClient.retrieve(id));
         if (validate) {
             assertXmlValidRole(result);
         }
         return result;
     }
 
     /**
      * Retrieve an User-Account.
      *
      * @param id       the id.
      * @param validate If true, retrieved object is validated against the corresponding schema.
      * @return The retrieved object.
      * @throws Exception If anything fails.
      */
     public String retrieveUserAccount(final String id, final boolean validate) throws Exception {
         final String result = handleXmlResult(userAccountClient.retrieve(id));
         if (validate) {
             assertXmlValidUserAccount(result);
         }
         return result;
     }
 
     public Map<String, String> retrieveObjectIdsHead() throws Exception {
         Map<String, String> idMap = new HashMap<String, String>();
         String objectType = "Context";
         String objectIds = retrieveObjectIdsHead(objectType);
         idMap.put(objectType, objectIds);
 
         objectType = "Item";
         objectIds = retrieveObjectIdsHead(objectType);
         idMap.put(objectType, objectIds);
 
         objectType = "Container";
         objectIds = retrieveObjectIdsHead(objectType);
         idMap.put(objectType, objectIds);
 
         objectType = "OrganizationalUnit";
         objectIds = retrieveObjectIdsHead(objectType);
         idMap.put(objectType, objectIds);
 
         objectType = "ContentModel";
         objectIds = retrieveObjectIdsHead(objectType);
         idMap.put(objectType, objectIds);
         return idMap;
 
     }
 
     /**
      * Retrieve all object ids for the given object type from the resource index (Fedora 3.x).
      *
      * @param objectType The object type.
      * @return The list of ids (format CSV).
      * @throws Exception If anything fails.
      */
     protected String retrieveObjectIdsHead(final String objectType) throws Exception {
 
         TripleStoreTestBase triplstoreConnector = new TripleStoreTestBase();
         String query =
             "select $s from <#ri> where $s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://escidoc.de/core/01/resources/"
                 + objectType + ">";
         String queryResult = triplstoreConnector.requestMPT(query, TripleStoreTestBase.FORMAT_CSV);
         queryResult = queryResult.replaceAll("info:fedora", "");
         StringTokenizer resultTokenizer = new StringTokenizer(queryResult, "/");
         String ids = new String();
         if (resultTokenizer.countTokens() > 1) {
 
             String next = resultTokenizer.nextToken().trim();
             while (resultTokenizer.hasMoreTokens()) {
                 next = resultTokenizer.nextToken().trim();
                if (!ids.isEmpty()) {
                     ids += ",";
                 }
                 ids += next;
             }
         }
         return ids;
     }
 
     protected void saveCheckResult(final Map<String, String> ids, Collection<String> formerlyFailed) throws Exception {
 
         String text = new String();
         Iterator<String> keysIter = ids.keySet().iterator();
         while (keysIter.hasNext()) {
             String key = keysIter.next();
             if (!FAILING.equals(key)) {
                 text += key + "=" + ids.get(key) + "\n";
             }
         }
         text += "\n" + FAILING + "=" + ids.get(FAILING) + "\n";
         Collection<String> failed = getValuesFromCsv(ids.get(FAILING));
         if (failed.isEmpty()) {
             text += "\n[TEST PASSED]\n";
         }
         else {
             text += "\n[TEST FAILED] - failing objects=" + putValuesToCsv(failed) + "\n";
         }
         ResourceProvider.saveToFile(CHECK_RESULT_PATH, getObjectIdFilename(), text);
     }
 
     /**
      *
      * @param ids
      * @throws Exception
      */
     protected void saveObjectIds(final Map<String, String> ids) throws Exception {
 
         String text = new String();
         Iterator<String> keysIter = ids.keySet().iterator();
         while (keysIter.hasNext()) {
             String key = keysIter.next();
             if (!FAILING.equals(key)) {
                 text += key + "=" + ids.get(key) + "\n";
             }
         }
         text += "\n" + FAILING + "=" + ids.get(FAILING) + "\n";
         ResourceProvider.saveToFile(OBJECT_ID_PATH, getObjectIdFilename(), text);
     }
 
     protected String getObjectIdFilename() {
         return "rest-" + OBJECT_ID_FILENAME;
     }
 
     protected Map<String, String> loadObjectIds() {
 
         Map<String, String> result = new HashMap<String, String>();
         Properties properties = new Properties();
         try {
             InputStream fis = null;
             String search = ResourceProvider.concatenatePath(OBJECT_ID_PATH, getObjectIdFilename());
             File file = new File(search);
             if (file.exists()) {
                 fis = ResourceProvider.getFileInputStreamFromFile(OBJECT_ID_PATH, getObjectIdFilename());
             }
             if (fis != null) {
                 properties.load(fis);
                 Iterator<Object> keyIter = properties.keySet().iterator();
                 while (keyIter.hasNext()) {
                     String key = (String) keyIter.next();
                     String ids = properties.getProperty(key);
                     result.put(key, ids);
                 }
                 fis.close();
             }
             else {
                 result = null;
             }
         }
         catch (final IOException e) {
             result = null;
         }
         return result;
     }
 
     protected Collection<String> getValuesFromCsv(final String csv) {
         Collection<String> result = new Vector<String>();
         StringTokenizer csvTokens = new StringTokenizer(csv, ",");
         while (csvTokens.hasMoreTokens()) {
             result.add(csvTokens.nextToken().trim());
         }
         return result;
     }
 
     protected String putValuesToCsv(final Collection<String> values) {
         String result = "";
         Iterator<String> valueIter = values.iterator();
         if (valueIter.hasNext()) {
             result += valueIter.next();
         }
         while (valueIter.hasNext()) {
             result += "," + valueIter.next();
         }
         return result;
     }
 
     protected void sleep() {
         try {
             Thread.sleep(SLEEP_TIME);
         }
         catch (final InterruptedException e) {
             // ignore
         }
     }
 }
