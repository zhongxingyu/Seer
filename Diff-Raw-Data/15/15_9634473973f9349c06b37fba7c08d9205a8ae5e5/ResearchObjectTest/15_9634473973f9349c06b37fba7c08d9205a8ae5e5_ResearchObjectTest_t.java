 package pl.psnc.dl.wf4ever.model.RO;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.model.SnapshotBuilder;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 import pl.psnc.dl.wf4ever.vocabulary.ROEVO;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 public class ResearchObjectTest extends BaseTest {
 
     /** a new empty RO. */
     private ResearchObject ro;
 
 
     @Override
     @Before
     public void setUp()
             throws Exception {
         super.setUp();
         URI folderResourceMapUri = researchObject.getUri().resolve("folder-rm.ttl");
         Model model = FileManager.get().loadModel(folderResourceMapUri.toString(), folderResourceMapUri.toString(),
             "TURTLE");
         dataset.addNamedModel(folderResourceMapUri.toString(), model);
         clearDLFileSystem();
         ro = ResearchObject.create(builder, URI.create("http://example.org/ro-test/"));
     }
 
 
     @Test
     public void testConstructor() {
         new ResearchObject(userProfile, dataset, true, ro.getUri());
     }
 
 
     @Test
     public void testCreate() {
         Assert.assertNotNull(ro.getManifest());
         Assert.assertNotNull(ro.getEvoInfo());
 
         Model model = ModelFactory.createDefaultModel();
 
         model.read(ro.getEvoInfo().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(ro.getUri().toString());
         Assert.assertTrue(r.hasProperty(RDF.type, ROEVO.LiveRO));
 
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         r = model.getResource(ro.getUri().toString());
         Assert.assertTrue(r.hasProperty(ORE.isDescribedBy, model.getResource(ro.getManifest().getUri().toString())));
     }
 
 
     @Test(expected = ConflictException.class)
     public void testCreateDuplication() {
         ResearchObject.create(builder, ro.getUri());
     }
 
 
     @Test
     public void testCreateEvoInfo() {
         //FIXME is there any way to test it?
         /*
             ResearchObject ro = builder.buildResearchObject(researchObjectUri);
             Assert.assertNull(ro.getEvoInfo());
         */
         /*
          * 
             researchObject ro = ResearchObject.create(builder, URI.create("http://example.org/unit-test-ro/"));
             EvoInfo evo1 = ro.getEvoInfo();
             ro.createEvoInfo();
             Assert.assertFalse(evo1.equals(ro.getEvoInfo()));
          */
     }
 
 
     @Test
     public void testGetEvoInfo() {
         Assert.assertNotNull(ro.getEvoInfo());
     }
 
 
     @Test
     public void testGetLiveEvoInfo() {
         Assert.assertNotNull(ro.getLiveEvoInfo());
     }
 
 
     @Test
     public void testGetManifest() {
         Assert.assertNotNull(ro.getManifest());
     }
 
 
     @Test
     public void testGet() {
         Assert.assertEquals(researchObject, ResearchObject.get(builder, researchObject.getUri()));
     }
 
 
     @Test
     public void testSave() {
         Model model = ModelFactory.createDefaultModel();
 
         model.read(ro.getEvoInfo().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(ro.getUri().toString());
         Assert.assertTrue(r.hasProperty(RDF.type, ROEVO.LiveRO));
 
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         r = model.getResource(ro.getUri().toString());
         Assert.assertTrue(r.hasProperty(ORE.isDescribedBy, model.getResource(ro.getManifest().getUri().toString())));
 
         //TODO
         //check the serialization
     }
 
 
     @Test
     public void testDelete() {
         ro.delete();
     }
 
 
     @Test
     public void testGetImmutableResearchObjects() {
         //TODO implement test!
     }
 
 
     @Test
     public void testAggregate()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/resource.txt");
         pl.psnc.dl.wf4ever.model.RO.Resource r = ro.aggregate("resource.txt", is, "text/plain");
         Assert.assertNotNull(ro.getAggregatedResources().get(r.getUri()));
         Assert.assertEquals(ro, r.getResearchObject());
         Model model = ModelFactory.createDefaultModel();
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource jenaResource = model.getResource(ro.getUri().toString());
         Assert.assertTrue(jenaResource.hasProperty(ORE.aggregates, model.getResource(r.getUri().toString())));
     }
 
 
     @Test
     public void testAggregateExternal() {
         ro.aggregate(researchObject.getUri());
         Assert.assertNotNull(ro.getAggregatedResources().get(researchObject.getUri()));
         Assert.assertNotNull(ResearchObject.get(builder, ro.getUri()).getAggregatedResources()
                 .get(researchObject.getUri()));
     }
 
 
     @Test
     public void testResourceCopy()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/resource.txt");
         pl.psnc.dl.wf4ever.model.RO.Resource r = ro.aggregate("resource.txt", is, "text/plain");
         pl.psnc.dl.wf4ever.model.RO.Resource result = researchObject.copy(r, new SnapshotBuilder());
         Assert.assertNotNull(researchObject.getAggregatedResources().get(result.getUri()));
         Assert.assertTrue(result.getUri().relativize(researchObject.getUri()).equals(researchObject.getUri()));
         Assert.assertTrue(r.getUri().relativize(ro.getUri()).equals(ro.getUri()));
 
     }
 
 
     @Test
     public void testAggregateFolder()
             throws BadRequestException {
         ro.aggregate(URI.create("http://example.org"));
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/folder.rdf");
         Folder f = ro.aggregateFolder(ro.getUri().resolve("folder"), is);
         Assert.assertNotNull(f);
         Assert.assertTrue(ro.getFolders().get(f.getUri()).equals(f));
         Assert.assertTrue(ResearchObject.get(builder, ro.getUri()).getFolders().get(f.getUri()).equals(f));
     }
 
 
     @Test
     public void testCopyFolder()
             throws BadRequestException {
         Folder folder = researchObject.getFolders().values().iterator().next();
         for (FolderEntry entry : folder.getFolderEntries().values()) {
             ro.aggregate(ro.getUri().resolve(entry.getProxyFor().getRawPath()));
         }
         Folder result = ro.copy(folder, new SnapshotBuilder());
         Assert.assertNotNull(ro.getFolders().get(result.getUri()));
         Assert.assertTrue(result.getUri().relativize(ro.getUri()).equals(ro.getUri()));
         Assert.assertTrue(folder.getUri().relativize(researchObject.getUri()).equals(researchObject.getUri()));
     }
 
 
     @Test
     public void testAnnotate()
             throws BadRequestException {
         Set<Thing> targets = new HashSet<>();
         targets.add(ro);
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/body.rdf");
         pl.psnc.dl.wf4ever.model.RO.Resource body = ro.aggregate("body.rdf", is, "application/rdf+xml");
         Annotation annotation = ro.annotate(body.getUri(), targets);
         Assert.assertEquals(annotation, ro.getAnnotations().get(annotation.getUri()));
         Model model = ModelFactory.createDefaultModel();
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(annotation.getUri().toString());
         Assert.assertTrue(r.hasProperty(RDF.type, AO.Annotation));
         Assert.assertTrue(r.hasProperty(RDF.type, RO.AggregatedAnnotation));
         Assert.assertTrue(r.hasProperty(RO.annotatesAggregatedResource, model.getResource(ro.getUri().toString())));
         Assert.assertTrue(r.hasProperty(AO.body, model.getResource(body.getUri().toString())));
     }
 
 
     @Test
     public void testAnnotate2()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/body.rdf");
         pl.psnc.dl.wf4ever.model.RO.Resource body = ro.aggregate("body.rdf", is, "application/rdf+xml");
         is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/annotation.rdf");
         Annotation annotation = ro.annotate(is);
         Assert.assertEquals(annotation, ro.getAnnotations().get(annotation.getUri()));
         Model model = ModelFactory.createDefaultModel();
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(annotation.getUri().toString());
         Assert.assertTrue(r.hasProperty(RDF.type, AO.Annotation));
         Assert.assertTrue(r.hasProperty(RDF.type, RO.AggregatedAnnotation));
         Assert.assertTrue(r.hasProperty(RO.annotatesAggregatedResource, model.getResource(ro.getUri().toString())));
         Assert.assertTrue(r.hasProperty(AO.body, model.getResource(body.getUri().toString())));
     }
 
 
     @Test
     public void testAnnotate3()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/body.rdf");
         pl.psnc.dl.wf4ever.model.RO.Resource body = ro.aggregate("body.rdf", is, "application/rdf+xml");
         Annotation annotation = ro.annotate(body.getUri(), ro);
         Assert.assertEquals(annotation, ro.getAnnotations().get(annotation.getUri()));
         Model model = ModelFactory.createDefaultModel();
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(annotation.getUri().toString());
         Assert.assertTrue(r.hasProperty(RDF.type, AO.Annotation));
         Assert.assertTrue(r.hasProperty(RDF.type, RO.AggregatedAnnotation));
         Assert.assertTrue(r.hasProperty(RO.annotatesAggregatedResource, model.getResource(ro.getUri().toString())));
         Assert.assertTrue(r.hasProperty(AO.body, model.getResource(body.getUri().toString())));
     }
 
 
     @Test
     public void testAnnotate4()
             throws BadRequestException {
         String annotationId = "annotation-id";
         Set<Thing> targets = new HashSet<>();
         targets.add(ro);
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/body.rdf");
         pl.psnc.dl.wf4ever.model.RO.Resource body = ro.aggregate("body.rdf", is, "application/rdf+xml");
         Annotation annotation = ro.annotate(body.getUri(), targets, annotationId);
         Assert.assertEquals(annotation, ro.getAnnotations().get(annotation.getUri()));
         Model model = ModelFactory.createDefaultModel();
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(annotation.getUri().toString());
         Assert.assertEquals(ro.getUri().resolve(".ro/annotations/").resolve(annotationId), annotation.getUri());
         Assert.assertTrue(r.hasProperty(RDF.type, AO.Annotation));
         Assert.assertTrue(r.hasProperty(RDF.type, RO.AggregatedAnnotation));
         Assert.assertTrue(r.hasProperty(RO.annotatesAggregatedResource, model.getResource(ro.getUri().toString())));
         Assert.assertTrue(r.hasProperty(AO.body, model.getResource(body.getUri().toString())));
     }
 
 
     @Test(expected = NullPointerException.class)
     public void testAnnotateWithNullBody()
             throws BadRequestException {
         Set<Thing> targets = new HashSet<>();
         targets.add(ro);
         ro.annotate(null, targets);
     }
 
 
     @Test(expected = NullPointerException.class)
     public void testAnnotateWithNullTarget()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/body.rdf");
         pl.psnc.dl.wf4ever.model.RO.Resource body = ro.aggregate("body.rdf", is, "application/rdf+xml");
         ro.annotate(body.getUri(), (Thing) null);
     }
 
 
     @Test(expected = IllegalArgumentException.class)
     public void testAnnotateWithEmptyTarget()
             throws BadRequestException {
         Set<Thing> targets = new HashSet<>();
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/body.rdf");
         pl.psnc.dl.wf4ever.model.RO.Resource body = ro.aggregate("body.rdf", is, "application/rdf+xml");
         Annotation annotation = ro.annotate(body.getUri(), targets);
         Assert.assertEquals(annotation, ro.getAnnotations().get(annotation.getUri()));
         Model model = ModelFactory.createDefaultModel();
         model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(annotation.getUri().toString());
         Assert.assertTrue(r.hasProperty(RDF.type, AO.Annotation));
         Assert.assertTrue(r.hasProperty(RDF.type, RO.AggregatedAnnotation));
     }
 
 
     @Test
     public void testCopyAnnotation()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/body.rdf");
         pl.psnc.dl.wf4ever.model.RO.Resource body = ro.aggregate("body.rdf", is, "application/rdf+xml");
         Annotation annotation = ro.annotate(body.getUri(), ro);
 
         Annotation copyAnnotation = researchObject.copy(annotation, new SnapshotBuilder());
         Assert.assertEquals(researchObject.getUri().relativize(copyAnnotation.getUri()),
             ro.getUri().relativize(annotation.getUri()));
 
         Model model = ModelFactory.createDefaultModel();
         model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(copyAnnotation.getUri().toString());
         Assert.assertTrue(r.hasProperty(RDF.type, AO.Annotation));
         Assert.assertTrue(r.hasProperty(RDF.type, RO.AggregatedAnnotation));
         Assert.assertTrue(r.hasProperty(RO.annotatesAggregatedResource));
         Assert.assertTrue(r.hasProperty(AO.body));
     }
 
 
     @Ignore
     @Test
     public void testUnpackAndAggregate() {
         //TODO implemnt
     }
 
 
     @Ignore
     @Test
     public void testGetCreated() {
         //TODO implemnt
     }
 
 
     @Ignore
     @Test
     public void testGetCreator() {
         //TODO implemnt
     }
 
 
     @Ignore
     @Test
     public void testIsUriUsed() {
         //TODO implemnt
     }
 
 
     @Ignore
     @Test
     public void testGetAsZipArchive() {
         //TODO implemnt
     }
 
 
     @Ignore
     @Test
     public void testGetAll() {
     }
     //TODO implemnt
 
 }
