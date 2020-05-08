 package pl.psnc.dl.wf4ever.model.RO;
 
 import java.io.InputStream;
 import java.net.URI;
 
 import junit.framework.Assert;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.model.SnapshotBuilder;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 public class FolderTest extends BaseTest {
 
     private String folderName = "folder";
     private FolderBuilder folderBuilder;
     private URI folderUri;
 
 
     @Override
     @Before
     public void setUp()
             throws Exception {
         super.setUp();
         folderUri = researchObject.getUri().resolve(folderName);
         folderBuilder = new FolderBuilder();
     }
 
 
     @Test
     public void testConstructor() {
         Folder folder = new Folder(userProfile, dataset, true, researchObject, folderUri, null);
         Assert.assertEquals(researchObject, folder.getResearchObject());
         Assert.assertEquals(folderUri, folder.getUri());
     }
 
 
     @Test
     public void testGetFolderEntries()
             throws BadRequestException {
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         Assert.assertEquals(2, folder.getFolderEntries().size());
     }
 
 
     @Test
     public void testGetAggregatedResources()
             throws BadRequestException {
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         Assert.assertEquals(2, folder.getFolderEntries().size());
     }
 
 
     @Test
     public void testGetResourceMap()
             throws BadRequestException {
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         Assert.assertNotNull(folder.getResourceMap());
         //TODO something else?
     }
 
 
     @Test
     public void testGet()
             throws BadRequestException {
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         Folder.get(builder, folder.getUri());
         folder.equals(Folder.get(builder, folder.getUri()));
     }
 
 
     @Test
     public void testSave() {
         Folder folder = builder.buildFolder(folderUri, researchObject, userProfile, DateTime.now(), null);
         folder.save();
         Model model = ModelFactory.createDefaultModel();
         model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Assert.assertTrue(model.containsResource(model.getResource(folder.getUri().toString())));
     }
 
 
     @Test
     public void testDelete() {
         Folder folder = builder.buildFolder(folderUri, researchObject, userProfile, DateTime.now(), null);
         folder.save();
         Model model = ModelFactory.createDefaultModel();
         model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Assert.assertNotNull(model.getResource(folder.getUri().toString()));
 
         folder.delete();
         model = ModelFactory.createDefaultModel();
         model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Assert.assertFalse(model.containsResource(model.getResource(folder.getUri().toString())));
     }
 
 
     @Test
     public void testCreate()
             throws BadRequestException {
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         Assert.assertNotNull(folder);
     }
 
 
     @Test
     public void testCreateEmptyFolder()
             throws BadRequestException {
         Folder folder = folderBuilder.init("model/ro/folder/empty_folder.rdf", builder, researchObject, folderUri);
         Assert.assertNotNull(folder);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateFolderNoRDFContent()
             throws BadRequestException {
         folderBuilder.init("model/ro/folder/empty.rdf", builder, researchObject, folderUri);
     }
 
 
     public void testCreateFolderRDFNoFolder()
             throws BadRequestException {
         Folder folder = folderBuilder.init("model/ro/folder/no_folder.rdf", builder, researchObject, folderUri);
         Assert.assertNull(folder);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateTwoFolders()
             throws BadRequestException {
         Folder folder = folderBuilder.init("model/ro/folder/two_folders.rdf", builder, researchObject, folderUri);
         Assert.assertNull(folder);
     }
 
 
     @Test(expected = ConflictException.class)
     public void testCreateDuplication()
             throws BadRequestException {
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         Assert.assertNull(folder);
     }
 
 
     @Test(expected = NullPointerException.class)
     public void testCreateFolderNullInputStream()
             throws BadRequestException {
         folderBuilder.createROAggregated(builder, researchObject, folderUri);
         Folder.create(builder, researchObject, folderUri, null);
     }
 
 
     @Test
     public void testCopy()
             throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Folder folderCopy = folder.copy(builder, new SnapshotBuilder(), researchObject2);
         Assert.assertNotNull(folderCopy.getCopyAuthor());
         Assert.assertNotNull(folderCopy.getCopyDateTime());
         URI expected = researchObject.getUri().relativize(folder.getUri());
        URI result = researchObject2.getUri().relativize(folderCopy.getUri());
         Assert.assertEquals(expected, result);
         for (FolderEntry entry : folderCopy.getFolderEntries().values()) {
             URI oldResourceUri = researchObject.getUri().resolve(entry.getProxyFor().getRawPath());
             String expected2 = researchObject.getFolderEntriesByResourceUri().get(oldResourceUri).iterator().next()
                     .getEntryName();
             String result2 = entry.getEntryName();
             Assert.assertEquals(expected2, result2);
         }
         for (AggregatedResource resource : folderCopy.getAggregatedResources().values()) {
            Assert.assertEquals(resource.getRawPath(), researchObject2.getUri().relativize(resource.getUri())
                     .getRawPath());
         }
     }
 
 
     @Test
     public void testCreateFolderEntry()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
         researchObject.aggregate("new-resource", is, "text/plain");
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         is = getClass().getClassLoader().getResourceAsStream("model/ro/folder/folder_entry.rdf");
         FolderEntry fe = folder.createFolderEntry(is);
         Assert.assertEquals(folder.getFolderEntries().get(fe.getUri()), fe);
     }
 
 
     @Test(expected = ConflictException.class)
     public void testCreateDuplicatedFolderEntry()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
         researchObject.aggregate("new-resource", is, "text/plain");
         Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         is = getClass().getClassLoader().getResourceAsStream("model/ro/folder/duplicated_folder_entry.rdf");
         FolderEntry fe = folder.createFolderEntry(is);
         Assert.assertEquals(folder.getFolderEntries().get(fe.getUri()), fe);
     }
 
 
     @Test
     public void testAddFolderEntry()
             throws BadRequestException {
         Folder f = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         AggregatedResource resource = researchObject.aggregate(URI.create("http://example.org/fake-uri"));
         FolderEntry fe = builder.buildFolderEntry(folderUri.resolve("fe"), resource, f, "fe");
         f.addFolderEntry(fe);
         Assert.assertEquals(fe, f.getFolderEntries().get(fe.getUri()));
     }
 
 
     @Test(expected = NullPointerException.class)
     public void testAddFolderEntryAsNull()
             throws BadRequestException {
         Folder f = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
         f.addFolderEntry(null);
     }
 
 
     @Test
     public void testUpdate()
             throws BadRequestException {
         Folder f = folderBuilder.init("model/ro/folder/empty_folder.rdf", builder, researchObject, folderUri);
         Assert.assertEquals(0, f.getFolderEntries().size());
         InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
         f.update(is, "application/rdf+xml");
         Assert.assertEquals(2, f.getFolderEntries().size());
     }
 
 }
