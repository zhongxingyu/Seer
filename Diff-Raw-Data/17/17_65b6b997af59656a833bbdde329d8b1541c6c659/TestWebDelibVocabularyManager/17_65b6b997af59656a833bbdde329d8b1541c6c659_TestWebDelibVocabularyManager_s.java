 /*
  * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Benjamin JALON<bjalon@nuxeo.com>
  */
 package org.nuxeo.adullact.webdelib.vocabulary;
 
 import static org.nuxeo.adullact.importer.ImporterServiceImpl.XML_IMPORTER_INITIALIZATION;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.sql.SQLException;
 
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.core.storage.sql.DatabaseHelper;
 import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
 import org.nuxeo.ecm.core.test.annotations.Granularity;
 import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
 import org.nuxeo.ecm.directory.Session;
 import org.nuxeo.ecm.directory.api.DirectoryService;
 import org.nuxeo.ecm.platform.test.PlatformFeature;
 import org.nuxeo.runtime.test.runner.Deploy;
 import org.nuxeo.runtime.test.runner.Features;
 import org.nuxeo.runtime.test.runner.FeaturesRunner;
 
 import com.google.inject.Inject;
 
 /**
  * @since 5.7.2
  *
  */
 @RunWith(FeaturesRunner.class)
 @Features(PlatformFeature.class)
 @Deploy({
         "nuxeo-adullact-webdelib-core:OSGI-INF/extensions/org.nuxeo.adullact.webdelib.vocabulary.WebDelibVocabularyManager.xml",
         "nuxeo-adullact-webdelib-core:OSGI-INF/extensions/org.nuxeo.adullact.webdelib.vocabulary.WebDelibVocabularyManagerListener.xml",
         "nuxeo-adullact-webdelib-core-test:test-adullact-webdelib-vocabulary-contrib.xml" })
 @RepositoryConfig(cleanup = Granularity.METHOD, init = DefaultRepositoryInit.class)
 public class TestWebDelibVocabularyManager {
 
     @Inject
     public CoreSession session;
 
     @Inject
     public WebDelibVocabularyManager service;
 
     @Inject
     public DirectoryService dirService;
 
     @Test
     public void shouldServiceDeployed() {
         assertNotNull(service);
     }
 
     @Ignore
     @Test
     public void shouldNotAddEntryInVocabularyWhenDocCreatedWithEmptyValue()
             throws ClientException {
         DocumentModelList entries = getEntries();
         assertEquals(0, entries.size());
 
         DocumentModel doc = session.createDocumentModel("File");
         doc.putContextData(XML_IMPORTER_INITIALIZATION, true);
         doc = session.createDocument(doc);
         session.save();
 
         entries = getEntries();
         assertEquals(0, entries.size());
 
         doc = session.createDocumentModel("File");
         doc.putContextData(XML_IMPORTER_INITIALIZATION, true);
         doc.setPropertyValue("dc:title", "      ");
         doc = session.createDocument(doc);
         session.save();
 
         entries = getEntries();
         assertEquals(0, entries.size());
     }
 
     @Ignore
     @Test
     public void shouldAddEntryInVocabularyWhenDocCreated()
             throws ClientException {
         DocumentModelList entries = getEntries();
         assertEquals(0, entries.size());
 
         createDoc("toto");
 
         entries = getEntries();
         assertEquals(1, entries.size());
         DocumentModel entry = getEntry("toto");
         assertNotNull(entry);
         assertEquals("toto", entry.getPropertyValue("label"));
         assertEquals(new Long(1), entry.getPropertyValue("ordering"));
         assertEquals(new Long(1), entry.getPropertyValue("obsolete"));
 
     }
 
     @Ignore
     @Test
     public void shouldRemoveEntryInVocabularyWhenDocRemoved()
             throws ClientException {
         DocumentModel entry = getEntry("titi");
 
         createDoc("titi");
         createDoc("titi");
         entry = getEntry("titi");
         assertEquals(new Long(2), entry.getPropertyValue("ordering"));
         assertEquals(new Long(1), entry.getPropertyValue("obsolete"));
 
         removeOneDoc("titi");
         entry = getEntry("titi");
         assertEquals(new Long(1), entry.getPropertyValue("ordering"));
         assertEquals(new Long(1), entry.getPropertyValue("obsolete"));
 
         removeOneDoc("titi");
         entry = getEntry("titi");
         assertEquals(new Long(0), entry.getPropertyValue("ordering"));
         assertEquals(new Long(0), entry.getPropertyValue("obsolete"));
 
         createDoc("titi");
         entry = getEntry("titi");
         assertEquals(new Long(1), entry.getPropertyValue("ordering"));
         assertEquals(new Long(1), entry.getPropertyValue("obsolete"));
     }
 
     // Our removing strategy don't remove structure but should improve to manage
     // that ?
     @Ignore
     @Test
     public void shouldRemoveEntryInVocabularyWhenDocStructureRemoved()
             throws ClientException {
         DocumentModel container = session.createDocumentModel("Folder");
         container.setPathInfo("/", "test");
         container = session.createDocument(container);
         session.save();
 
         DocumentModel doc = session.createDocumentModel("File");
         doc.setPropertyValue("dc:title", "tata");
         doc = session.createDocument(doc);
         session.save();
 
         DocumentModel entry = getEntry("tata");
         assertEquals(new Long(1), entry.getPropertyValue("ordering"));
         assertEquals(new Long(1), entry.getPropertyValue("obsolete"));
 
         session.removeDocument(container.getRef());
         entry = getEntry("tata");
         assertEquals(new Long(0), entry.getPropertyValue("ordering"));
         assertEquals(new Long(0), entry.getPropertyValue("obsolete"));
     }
 
     private void removeOneDoc(String title) throws ClientException {
         DocumentModelList docs = session.query("SELECT * From Document WHERE dc:title = '"
                 + title + "'");
         session.removeDocument(docs.get(0).getRef());
         session.save();
 
     }
 
     private void createDoc(String title) throws ClientException,
             PropertyException {
         DocumentModel doc = session.createDocumentModel("File");
         doc.putContextData(XML_IMPORTER_INITIALIZATION, true);
         doc.setPropertyValue("dc:title", title);
         doc = session.createDocument(doc);
         session.save();
     }
 
     private DocumentModelList getEntries() throws ClientException {
         Session dirSession = dirService.open("voca");
         DocumentModelList entries = dirSession.getEntries();
         return entries;
 
     }
 
     private DocumentModel getEntry(String id) throws ClientException {
         Session dirSession = dirService.open("voca");
         DocumentModel entry = dirSession.getEntry(id);
         return entry;
 
     }
 
     @After
     public void tearDown() throws SQLException {
         DatabaseHelper.DATABASE.tearDown();
     }
 }
