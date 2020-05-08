 package pl.psnc.dl.wf4ever.model.RDF;
 
 import java.net.URI;
 
 import junit.framework.Assert;
 
import org.junit.Before;
 import org.junit.Test;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * Test Class for RDF.Thing model.
  * 
  * @author pejot
  * 
  */
 public class ThingTest extends BaseTest {
 
     @Override
    @Before
     public void setUp() {
         super.setUp();
     }
 
 
     /**
      * Test that the resources under control of RODL are properly detected.
      */
     //FIXME this method is buggy, it shouldn't use URIs
     @Test
     public void testIsSpecial() {
         Thing ordinaryThing1 = new Thing(null, URI.create("http:///www.example.com/ROS/manifest/thing/"));
         Thing ordinaryThing2 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest/"));
         Thing ordinaryThing3 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest.rdf/thing/"));
         Thing ordinaryThing4 = new Thing(null, URI.create("http:///www.example.com/ROs/oridnary_resource"));
         Thing specialThing1 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest.rdf/"));
         Thing specialThing2 = new Thing(null, URI.create("http:///www.example.com/ROs/evo_info.ttl/"));
         Thing specialThing3 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest.rdf"));
         Thing specialThing4 = new Thing(null, URI.create("http:///www.example.com/ROs/evo_info.ttl"));
         Thing specialThing5 = new Thing(null, URI.create("manifest.rdf"));
         Thing specialThing6 = new Thing(null, URI.create("evo_info.ttl"));
         Assert.assertFalse(ordinaryThing1.isSpecialResource());
         Assert.assertFalse(ordinaryThing2.isSpecialResource());
         Assert.assertFalse(ordinaryThing3.isSpecialResource());
         Assert.assertFalse(ordinaryThing4.isSpecialResource());
         Assert.assertTrue(specialThing1.isSpecialResource());
         Assert.assertTrue(specialThing2.isSpecialResource());
         Assert.assertTrue(specialThing3.isSpecialResource());
         Assert.assertTrue(specialThing4.isSpecialResource());
         Assert.assertTrue(specialThing6.isSpecialResource());
         Assert.assertTrue(specialThing5.isSpecialResource());
     }
 
 
    @Test
    public void testEquals() {
        Thing first = builder.buildThing(URI.create("http://example.org/example-thing"));
        Thing second = builder.buildThing(URI.create("http://example.org/example-thing"));
        Assert.assertTrue(first.equals(second));
    }


     /**
      * Test the manifest can be retrieved together with the annotation body.
      */
     @Test
     public void testGetGraphAsInputStreamWithNamedGraphs() {
         Thing thing = builder.buildThing(URI.create(MANIFEST));
         NamedGraphSet graphset = new NamedGraphSetImpl();
         graphset.read(thing.getGraphAsInputStream(RDFFormat.TRIG), "TRIG", null);
 
         Quad sampleAgg = new Quad(Node.createURI(MANIFEST), Node.createURI(RESEARCH_OBJECT),
                 Node.createURI(ORE.aggregates.getURI()), Node.createURI(RESOURCE1));
         Assert.assertTrue("Contains a sample aggregation", graphset.containsQuad(sampleAgg));
 
         Quad sampleAnn = new Quad(Node.createURI(ANNOTATION_BODY), Node.createURI(RESOURCE1),
                 Node.createURI(DCTerms.license.getURI()), Node.createLiteral("GPL"));
         Assert.assertTrue("Contains a sample annotation", graphset.containsQuad(sampleAnn));
     }
 
 
     /**
      * Test the annotation body can be retrieved with relative URIs.
      */
     @Test
     public void testGetGraphAsInputStreamWithRelativeURIs() {
         Thing thing = builder.buildThing(URI.create(ANNOTATION_BODY));
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(thing.getGraphAsInputStreamWithRelativeURIs(URI.create(RESEARCH_OBJECT), RDFFormat.RDFXML), "",
             "RDF/XML");
         //FIXME this does not work correctly, for some reason ".." is stripped when reading the model
         verifyTriple(model, /* "../a_workflow.t2flow" */"a%20workflow.t2flow",
             URI.create("http://purl.org/dc/terms/title"), "A test");
         verifyTriple(model, URI.create("a%20workflow.t2flow"), URI.create(DCTerms.license.getURI()), "GPL");
         verifyTriple(model, URI.create(RESOURCE2), URI.create(DCTerms.description.getURI()), "Something interesting");
         verifyTriple(model, /* "../a_workflow.t2flow#somePartOfIt" */"a%20workflow.t2flow#somePartOfIt",
             URI.create(DCTerms.description.getURI()), "The key part");
     }
 }
