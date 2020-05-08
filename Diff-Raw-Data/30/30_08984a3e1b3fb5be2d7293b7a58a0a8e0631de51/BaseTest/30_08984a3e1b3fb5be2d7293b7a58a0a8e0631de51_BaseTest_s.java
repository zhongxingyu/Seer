 package pl.psnc.dl.wf4ever.model;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
 import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.hp.hpl.jena.util.FileManager;
 
 /**
  * Base test class for model package.
  * 
  */
 public class BaseTest {
 
     /** Temporary in-memory dataset. */
     protected Dataset dataset;
 
     /** Test user. */
     protected static UserMetadata userProfile;
 
     /** Builder using the temporary dataset and the test user. */
     protected Builder builder;
 
     /** RO URI as String, mapped using location-mapping.n3 to a local file. */
     protected static final String RESEARCH_OBJECT = "http://example.org/mess-ro/";
 
     /** RO URI as String, mapped using location-mapping.n3 to a local file. */
     protected static final String RESEARCH_OBJECT_2 = "http://example.org/mess-ro-2/";
 
     /** Manifest URI as String, mapped. */
     protected static final String MANIFEST = "http://example.org/mess-ro/.ro/manifest.rdf";
 
     /** Manifest URI as String, mapped. */
     protected static final String MANIFEST_2 = "http://example.org/mess-ro-2/.ro/manifest.rdf";
 
     /** Annotation body URI as String, mapped. */
     protected static final String ANNOTATION_BODY = "http://example.org/mess-ro/.ro/annotationBody.ttl";
 
     /** Resource URI as String, mapped. */
     protected static final String RESOURCE1 = "http://example.org/mess-ro/a%20workflow.t2flow";
 
     /** Resource URI as String, mapped. */
     protected static final String RESOURCE2 = "http://workflows.org/a%20workflow.scufl";
     /** Empty RO. */
     protected ResearchObject messRO;
     /** Empty RO. */
     protected ResearchObject messRO2;
 
 
     /**
      * Prepare filesystem DL.
      * 
      * @throws Exception
      *             when something unexpected happens, declared for subclasses
      */
     @BeforeClass
     public static void setUpBeforeClass()
             throws Exception {
        DigitalLibraryFactory.loadDigitalLibraryConfiguration("connection.properties");
     }
 
 
     /**
      * Create the dataset, load the RDF files.
      * 
      * @throws IOException
      */
     @Before
     public void setUp() {
         dataset = TDBFactory.createDataset();
         Model model;
         model = FileManager.get().loadModel(MANIFEST, MANIFEST, "RDF/XML");
         dataset.addNamedModel(MANIFEST, model);
         model = FileManager.get().loadModel(ANNOTATION_BODY, ANNOTATION_BODY, "TURTLE");
         dataset.addNamedModel(ANNOTATION_BODY, model);
         userProfile = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED, URI.create("http://jank"));
         builder = new Builder(userProfile, dataset, false);
         messRO = builder.buildResearchObject(URI.create(RESEARCH_OBJECT));
         model = FileManager.get().loadModel(MANIFEST_2, MANIFEST_2, "RDF/XML");
         dataset.addNamedModel(MANIFEST_2, model);
         builder = new Builder(userProfile, dataset, false);
         messRO2 = builder.buildResearchObject(URI.create(RESEARCH_OBJECT_2));
 
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
     }
 
 
     /**
      * Close the dataset.
      * 
      * @throws Exception
      *             when the filesystem can't be accessed
      */
     @After
     public void tearDown()
             throws Exception {
         builder.getDataset().close();
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
     }
 
 
     /**
      * Check if the model contains a triple.
      * 
      * @param model
      *            model that should contain the triple
      * @param subjectURI
      *            triple subject
      * @param propertyURI
      *            triple property
      * @param object
      *            triple object (a literal)
      */
     protected void verifyTriple(Model model, URI subjectURI, URI propertyURI, String object) {
         Resource subject = model.createResource(subjectURI.toString());
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     /**
      * Check if the model contains a triple.
      * 
      * @param model
      *            model that should contain the triple
      * @param subjectURI
      *            triple subject as String
      * @param propertyURI
      *            triple property
      * @param object
      *            triple object (a literal)
      */
     protected void verifyTriple(Model model, String subjectURI, URI propertyURI, String object) {
         Resource subject = model.createResource(subjectURI);
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     /**
      * Check if the model contains a triple.
      * 
      * @param model
      *            model that should contain the triple
      * @param subjectURI
      *            triple subject as String
      * @param propertyURI
      *            triple property
      * @param object
      *            triple object (a resource)
      */
     protected void verifyTriple(Model model, String subjectURI, URI propertyURI, Resource object) {
         Resource subject = model.createResource(subjectURI);
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 }
