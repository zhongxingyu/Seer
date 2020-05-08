 package org.nuxeo.ecm.platform.documentrepository.service.tests;
 
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.repository.jcr.testing.RepositoryOSGITestCase;
 import org.nuxeo.ecm.platform.documentrepository.api.DocRepository;
 import org.nuxeo.ecm.platform.documentrepository.service.api.DocumentRepositoryManager;
 import org.nuxeo.ecm.platform.documentrepository.service.plugin.base.AbstractDocumentRepositoryPlugin;
 import org.nuxeo.runtime.api.Framework;
 
 public class TestService extends RepositoryOSGITestCase {
 
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         deployBundle("org.nuxeo.ecm.platform.types.api");
         //deployBundle("org.nuxeo.ecm.platform.documentlink.api");
         deployBundle("org.nuxeo.ecm.platform.dublincore");
         //deployContrib("org.nuxeo.ecm.platform.documentlink.api","OSGI-INF/documentlink-adapter-contrib.xml");
         deployContrib("org.nuxeo.ecm.platform.documentlink.api","OSGI-INF/repository-adapter-contrib.xml");
         //deployBundle("org.nuxeo.ecm.platform.documentlink.types");
         deployContrib("org.nuxeo.ecm.platform.documentlink.types","OSGI-INF/documentlink-types-contrib.xml");
         deployContrib("org.nuxeo.ecm.platform.documentlink.core","OSGI-INF/documentrepository-framework.xml");
         openRepository();
     }
 
 
 
     public void testRepoCreation() throws Exception
     {
 
         DocumentRepositoryManager service =  Framework.getService(DocumentRepositoryManager.class);
         assertNotNull(service);
 
         // check repo creation and security settings
         DocumentModel doc = coreSession.createDocumentModel("/", "testFile","File");
         doc=coreSession.createDocument(doc);
         String creator1 = (String)doc.getProperty("dublincore", "creator");
         assertEquals("Administrator", creator1);
 
         DocRepository repo = service.getDocumentRepository(coreSession, null);
         assertNotNull(repo);
 
         String creator2 = (String)repo.getRepoDoc().getProperty("dublincore", "creator");
         assertEquals("system", creator2);
 
         DocumentModel doc2 = coreSession.createDocumentModel("/", "testFile2","File");
         doc2=coreSession.createDocument(doc2);
         String creator3 = (String)doc.getProperty("dublincore", "creator");
         assertEquals("Administrator", creator3);
 
     }
 
     public void testDocumentCreation() throws Exception
     {
 
         DocumentRepositoryManager service =  Framework.getService(DocumentRepositoryManager.class);
         assertNotNull(service);
 
         DocumentModel doc = coreSession.createDocumentModel("/", "testFile","File");
 
         DocRepository repo = service.getDocumentRepository(coreSession, null);
 
         doc = service.createDocumentInRepository(coreSession,repo.getRepoDoc(), doc);
 
         assertNotNull(doc);
 
         coreSession.save();
 
         assertTrue(doc.getPathAsString().contains(repo.getRepoDoc().getPathAsString()));
 
         String path = doc.getPathAsString();
 
         String[] subPathParts = path.split("/");
 
         assertEquals(3 +  AbstractDocumentRepositoryPlugin.DEFAULT_SUB_PATH_PART_NUMBER, subPathParts.length);
     }
 
     public void testDocumentCreationWithContrib() throws Exception
     {
 
         deployContrib("org.nuxeo.ecm.platform.documentlink.core", "OSGI-INF/test-repoplugin-contrib.xml");
 
         DocumentRepositoryManager service =  Framework.getService(DocumentRepositoryManager.class);
         assertNotNull(service);
 
         DocumentModel doc = coreSession.createDocumentModel("/", "testFile","File");
 
         DocRepository repo = service.getDocumentRepository(coreSession, null);
 
         doc = service.createDocumentInRepository(coreSession,repo.getRepoDoc(), doc);
 
         assertNotNull(doc);
 
         assertTrue(doc.getPathAsString().contains(repo.getRepoDoc().getPathAsString()));
 
     }
 
 
 
 }
