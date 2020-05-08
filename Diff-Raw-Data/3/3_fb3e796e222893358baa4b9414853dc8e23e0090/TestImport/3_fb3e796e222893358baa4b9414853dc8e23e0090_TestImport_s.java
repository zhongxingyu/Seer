 /*
  * (C) Copyright 2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  *
  */
 package org.nuxeo.ecm.platform;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.nuxeo.common.utils.FileUtils;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
 import org.nuxeo.ecm.platform.replication.importer.DocumentaryBaseImporterService;
 import org.nuxeo.runtime.api.Framework;
 
 public class TestImport extends SQLRepositoryTestCase {
 
     public TestImport(String name) {
         super(name);
     }
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         deployBundle("org.nuxeo.ecm.core.api");
         deployBundle("org.nuxeo.ecm.core");
         deployBundle("org.nuxeo.ecm.core.schema");
         deployBundle("org.nuxeo.ecm.platform.picture.core");
         deployBundle("org.nuxeo.ecm.platform.forum.core");
         deployBundle("org.nuxeo.ecm.platform.replication.importer.api");
         deployContrib("org.nuxeo.ecm.platform.replication.importer.core",
                 "OSGI-INF/ImporterService.xml");
         deployContrib("org.nuxeo.ecm.platform.replication.importer.core",
                 "OSGI-INF/default-document-xml-transformer-contrib.xml");
         openSession();
     }
 
     @SuppressWarnings("unchecked")
     private File getArchiveFile() throws ZipException, IOException {
         File zip = new File(
                 FileUtils.getResourcePathFromContext("DocumentaryBase.zip"));
         ZipFile arch = new ZipFile(zip);
 
         Path basePath = new Path(System.getProperty("java.io.tmpdir")).append(
                 "TestImport").append(System.currentTimeMillis() + "");
         new File(basePath.toString()).mkdirs();
         Enumeration entries = arch.entries();
         while (entries.hasMoreElements()) {
             ZipEntry entry = (ZipEntry) entries.nextElement();
             InputStream in = arch.getInputStream(entry);
             File out = new File(basePath.append(entry.getName()).toString());
             if (entry.isDirectory()) {
                 out.mkdirs();
             } else {
                 out.createNewFile();
                 FileUtils.copyToFile(in, out);
             }
             in.close();
         }
         return new File(basePath.toString());
     }
 
     public void testImport() throws Exception {
 
         DocumentModel root = session.getRootDocument();
         assertNotNull(root);
 
         DocumentModelList children = session.getChildren(root.getRef());
         assertEquals(0, children.size());
 
         DocumentaryBaseImporterService importer = Framework.getLocalService(DocumentaryBaseImporterService.class);
         assertNotNull(importer);
 
         File archiveDir = getArchiveFile();
         assertTrue(archiveDir.exists());
         assertTrue(archiveDir.list().length > 0);
 
         importer.importDocuments(null, archiveDir, false, true, true, false,
                 false);
         root = session.getRootDocument();
         assertNotNull(root);
 
         // check a top directory
         IdRef ref = new IdRef("b293d80e-9357-484f-9713-ff5bc12a4ac6");
         assertTrue(session.exists(ref));
         DocumentModel doc = session.getDocument(ref);
         assertEquals("WorkspaceRoot", doc.getType());
         assertEquals("Workspaces", doc.getTitle());
 
         // check default domain
         ref = new IdRef("6c0f811c-d13b-4461-8376-3664274a7ba4");
         assertTrue(session.exists(ref));
         doc = session.getDocument(ref);
         assertEquals("Domain", doc.getType());
         assertEquals("Default domain", doc.getTitle());
 
         // check an usual document
         ref = new IdRef("a55ff4f7-556a-4a4b-bd5b-1eabb47b57fb");
         assertTrue(session.exists(ref));
         doc = session.getDocument(ref);
         assertEquals("File", doc.getType());
         assertEquals("file 1", doc.getTitle());
 
         // check usual document: note
         ref = new IdRef("69803adb-1b6e-4658-a95d-52ed706f0548");
         assertTrue(session.exists(ref));
         doc = session.getDocument(ref);
         assertEquals("Note", doc.getType());
         assertEquals("domanu", doc.getTitle());
         assertEquals("<p>Do the dew</p><p>Second dew </p>",
                 doc.getProperty("note", "note"));
 
         // check a version
         ref = new IdRef("e1e9b1fe-9f48-4408-afc1-f8f167524f4b");
         assertTrue(session.exists(ref));
         doc = session.getDocument(ref);
         assertEquals("Note", doc.getType());
         assertEquals("domanu", doc.getTitle());
         assertTrue(doc.isVersion());
         assertEquals("2", doc.getVersionLabel());
 
         // check a proxy
         ref = new IdRef("23080819-2a78-410f-9323-d3938d52c044");
         assertTrue(session.exists(ref));
         doc = session.getDocument(ref);
         assertEquals("Note", doc.getType());
         assertEquals("domanu", doc.getTitle());
         assertTrue(doc.isProxy());
 
     }
 
     public void testDoubleImport() throws Exception {
 
         testImport();
         DocumentaryBaseImporterService importer = Framework.getLocalService(DocumentaryBaseImporterService.class);
         assertNotNull(importer);
 
         File archiveDir = getArchiveFile();
         assertTrue(archiveDir.exists());
         assertTrue(archiveDir.list().length > 0);
         importer.importDocuments(null, archiveDir, false, true, true, false,
                 false);
         session.getRootDocument();
 
     }
 
 }
