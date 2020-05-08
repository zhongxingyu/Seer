 package pl.psnc.dl.wf4ever.model.AO;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.model.EvoBuilder;
 import pl.psnc.dl.wf4ever.model.SnapshotBuilder;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 
 /**
  * Test class for AO.Annotation model.
  * 
  * @author pejot
  * 
  */
 public class AnnotationTest extends BaseTest {
 
     @Override
     @Before
     public void setUp() {
         super.setUp();
     }
 
 
     @Test
     public void testAnnotation() {
         Annotation annotation = new Annotation(userProfile, dataset, true, messRO, messRO.getUri().resolve("some-uri"));
         Assert.assertEquals(annotation.getResearchObject(), messRO);
     }
 
 
     @Test
     public void testCreateAnnotationWithExternalBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
         Set<Thing> expectedTarges = new HashSet<Thing>();
         expectedTarges.add(messRO);
 
         Assert.assertEquals(annotation.getUri(), messRO.getUri().resolve("new-annotation"));
         Assert.assertEquals(annotation.getBody().getUri(), URI.create("http://example.org/external.txt"));
         Assert.assertEquals(annotation.getFilename(), "new-annotation");
         Assert.assertEquals(annotation.getAnnotated(), expectedTarges);
     }
 
 
     @Test
     public void testCreateAnnotationWithInteralBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann2.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
         Set<Thing> expectedTarges = new HashSet<Thing>();
         expectedTarges.add(messRO);
         Assert.assertEquals(annotation.getUri(), messRO.getUri().resolve("new-annotation"));
         Assert.assertEquals(annotation.getBody().getUri(), messRO.getUri().resolve("ann2-body.txt"));
         Assert.assertEquals(annotation.getFilename(), "new-annotation");
         Assert.assertEquals(annotation.getAnnotated(), expectedTarges);
     }
 
 
     @Test
     public void testCreateAnnotationWithManyTargets()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/multi-targets.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
         Set<Thing> expectedTargets = new HashSet<Thing>();
 
        expectedTargets.add(builder.buildThing(messRO.getUri().resolve("a%20workflow.t2flow")));
        expectedTargets.add((Thing) messRO);
 
         Assert.assertEquals(annotation.getUri(), messRO.getUri().resolve("new-annotation"));
         Assert.assertEquals(annotation.getBody().getUri(), URI.create("http://example.org/external.txt"));
         Assert.assertEquals(annotation.getFilename(), "new-annotation");
         Assert.assertEquals(annotation.getAnnotated(), expectedTargets);
 
     }
 
 
     @Test(expected = ConflictException.class)
     public void testCreateAnnotationWithThisSameUri()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
         messRO.aggregate(annotation.getUri());
         is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation conflictedAnnotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"),
             is);
         messRO.aggregate(conflictedAnnotation.getUri());
     }
 
 
     @Test
     public void testCreateAnnotationWithNoContent()
             throws BadRequestException {
         //should exception be more precise?
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), null);
     }
 
 
     @Test
     public void testCreateAnnotationWithNotExistentInternalBody()
             throws BadRequestException {
         //internal body witch doesn't exists
         //should be an error?
         InputStream is = getClass().getClassLoader().getResourceAsStream(
             "model/ao/annotation/annotation-wrong-body.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateAnnotationWithNoBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-body.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test
     public void testCreateAnnotationWithNoTargets()
             throws BadRequestException {
         //should be forbidden? 
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-targets.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateAnnotationWithNoBodyNoTargets()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-body-no-targets.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test
     public void testCreateAnnotationWithNoRDF()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-rdf.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateEmptyAnnotation()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/empty.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test
     public void testCopyWithExternalBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
         EvoBuilder evoBuilder = new SnapshotBuilder();
         Annotation annotationCopy = annotation.copy(builder, evoBuilder, messRO2);
         Assert.assertEquals(annotationCopy.getUri().relativize(messRO2.getUri()), (messRO2.getUri()));
         Assert.assertEquals(annotationCopy.getBody().getUri(), (annotation.getBody().getUri()));
     }
 
 
     @Test
     public void testCopyWithInternalBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann2.rdf");
         Annotation annotation = Annotation.create(builder, messRO, messRO.getUri().resolve("new-annotation"), is);
         EvoBuilder evoBuilder = new SnapshotBuilder();
         Annotation annotationCopy = annotation.copy(builder, evoBuilder, messRO2);
         Assert.assertEquals(annotationCopy.getUri().relativize(messRO2.getUri()), (messRO2.getUri()));
         Assert.assertEquals(annotationCopy.getBody().getUri().relativize(messRO2.getUri()), (messRO2.getUri()));
     }
 
 
     @Test
     public void testSave() {
         Annotation annotation = new Annotation(userProfile, dataset, false, messRO,
                 URI.create("new-annotation-save-test"));
         annotation.setBody(builder.buildThing(URI.create("body")));
         Set<Thing> annotated = new HashSet<Thing>();
         annotated.add(messRO);
         annotation.setAnnotated(annotated);
         annotation.save();
         Assert.assertTrue(messRO.getAnnotations().containsKey(annotation.getUri()));
     }
 
 
     @Test
     public void testDelete() {
         Annotation annotation = new Annotation(userProfile, dataset, false, messRO,
                 URI.create("new-annotation-save-test"));
         annotation.setBody(builder.buildThing(URI.create("body")));
         Set<Thing> annotated = new HashSet<Thing>();
         annotated.add(messRO);
         annotation.setAnnotated(annotated);
         annotation.save();
         Assert.assertTrue(messRO.getAnnotations().containsKey(annotation.getUri()));
         annotation.delete();
         Assert.assertFalse(messRO.getAnnotations().containsKey(annotation.getUri()));
     }
 
 
     @Test
     public void testAssemble()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.assemble(builder, messRO, messRO.getUri().resolve("assemble-test"), is);
         Set<Thing> expectedTarges = new HashSet<Thing>();
         expectedTarges.add(messRO);
 
         Assert.assertEquals(annotation.getUri(), messRO.getUri().resolve("assemble-test"));
         Assert.assertEquals(annotation.getBody().getUri(), URI.create("http://example.org/external.txt"));
         Assert.assertEquals(annotation.getFilename(), "assemble-test");
         Assert.assertEquals(annotation.getAnnotated(), expectedTarges);
     }
 
 
     @Test
     public void testUpdate()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.assemble(builder, messRO, messRO.getUri().resolve("assemble-test"), is);
         Annotation newAnnotation = new Annotation(userProfile, dataset, false, messRO, URI.create("new-annotation"));
         Set<Thing> annotated = new HashSet<Thing>();
         annotated.add(new Thing(userProfile, messRO.getUri().resolve("a%20workflow.t2flow")));
         newAnnotation.setAnnotated(annotated);
         Thing newBody = new Thing(userProfile, URI.create("http://example.org/external"));
         newAnnotation.setBody(newBody);
         annotation.update(newAnnotation);
         Assert.assertEquals(annotation.getBody(), newBody);
         Assert.assertEquals(annotation.getAnnotated(), annotated);
 
     }
 
 
     @Test
     public void testIsSpecialResource() {
         Annotation ordinaryAnnotation1 = builder.buildAnnotation(messRO,
             URI.create("http://www.example.com/ROs/ro/annotations/ordinaryAnnotation1"),
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/manifest.rdf/ordinary")),
             new HashSet<Thing>(Arrays.asList(messRO)));
 
         Annotation ordinaryAnnotation2 = builder.buildAnnotation(messRO,
             URI.create("http://www.example.com/ROs/ro/annotations/ordinaryAnnotation1"),
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/evo_info.ttlordinary")),
             new HashSet<Thing>(Arrays.asList(messRO)));
 
         Annotation ordinaryAnnotation3 = builder.buildAnnotation(messRO,
             URI.create("http://www.example.com/ROs/ro/annotations/evo_info.ttl/annotation"),
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/annotations/evo_info.ttl/annotation")),
             new HashSet<Thing>(Arrays.asList(messRO)));
 
         Annotation specialAnnotation1 = builder.buildAnnotation(messRO,
             URI.create("http://www.example.com/ROs/ro/annotations/annotation"),
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/manifest.rdf")),
             new HashSet<Thing>(Arrays.asList(messRO)));
 
         Annotation specialAnnotation2 = builder.buildAnnotation(messRO,
             URI.create("http://www.example.com/ROs/ro/annotations/annotation"),
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/evo_info.ttl")),
             new HashSet<Thing>(Arrays.asList(messRO)));
 
         Annotation specialAnnotation3 = builder.buildAnnotation(messRO, URI
                 .create("http://www.example.com/ROs/ro/annotations/annotation"), builder.buildThing(URI
                 .create("http://www.example.com/ROs/ro/.ro/evo_info.ttl/")), new HashSet<Thing>(Arrays.asList(messRO)));
 
         Assert.assertFalse(ordinaryAnnotation1.isSpecialResource());
         Assert.assertFalse(ordinaryAnnotation2.isSpecialResource());
         Assert.assertFalse(ordinaryAnnotation3.isSpecialResource());
         Assert.assertTrue(specialAnnotation1.isSpecialResource());
         Assert.assertTrue(specialAnnotation2.isSpecialResource());
         Assert.assertTrue(specialAnnotation3.isSpecialResource());
     }
 }
