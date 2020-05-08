 package com.github.podd.resources.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.ServerSocket;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.util.Collection;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.TemporaryFolder;
 import org.junit.rules.Timeout;
 import org.openrdf.model.Model;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.UnsupportedRDFormatException;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.restlet.Component;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Protocol;
 import org.restlet.data.Status;
 import org.restlet.representation.FileRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.restlet.ApplicationUtils;
 import com.github.podd.restlet.PoddWebServiceApplication;
 import com.github.podd.restlet.PoddWebServiceApplicationImpl;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * Abstract test implementation that contains common components required by resource implementation
  * tests, including setting up the application and component, along with the TEST_PORT number to use
  * in the tests.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class AbstractResourceImplTest
 {
     @Rule
     public TemporaryFolder tempDirectory = new TemporaryFolder();
     
     /**
      * Timeout tests after 30 seconds.
      */
     @Rule
     public Timeout timeout = new Timeout(30000);
     
     /**
      * Determines the TEST_PORT number to use for the test server
      */
     protected int TEST_PORT;
     
     protected final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /**
      * A constant used to make requests that require admin privileges easier to recognise inside
      * tests.
      */
     protected final boolean testWithAdminPrivileges = true;
     
     /**
      * A constant used to make requests that do not require admin privileges easier to recognise
      * inside tests.
      */
     protected final boolean testNoAdminPrivileges = false;
     
     private Component component;
     
     protected Path testDir;
     
     public AbstractResourceImplTest()
     {
         super();
     }
     
     /**
      * Utility method to verify that freemarker has not encountered errors when generating output.
      * 
      * @param body
      *            Generated output in which to look for possible freemarker errors.
      */
     protected void assertFreemarker(final String body)
     {
         Assert.assertFalse("Freemarker error.", body.contains("Java backtrace for programmers:"));
         Assert.assertFalse("Freemarker error.", body.contains("freemarker.core."));
     }
     
     /**
      * Utility method to verify that RDF documents can be parsed and the resulting number of
      * statements is as expected.
      * 
      * @param inputStream
      * @param format
      * @param expectedStatements
      * @return
      * @throws RDFParseException
      * @throws RDFHandlerException
      * @throws IOException
      */
     protected Model assertRdf(final InputStream inputStream, final RDFFormat format, final int expectedStatements)
         throws RDFParseException, RDFHandlerException, IOException
     {
         final Model model = new LinkedHashModel();
         
         final RDFParser parser = Rio.createParser(format);
         parser.setRDFHandler(new StatementCollector(model));
         parser.parse(inputStream, "http://test.podd.example.org/should/not/occur/in/a/real/graph/");
         
         if(expectedStatements != model.size())
         {
             System.out.println("Number of statements was not as expected found:" + model.size() + " expected:"
                     + expectedStatements);
             DebugUtils.printContents(model);
         }
         
         Assert.assertEquals(expectedStatements, model.size());
         
         return model;
     }
     
     /**
      * Builds a {@link Representation} from a Resource.
      * 
      * @param resourcePath
      * @param mediaType
      * @return
      * @throws IOException
      */
     protected FileRepresentation buildRepresentationFromResource(final String resourcePath, final MediaType mediaType)
         throws IOException
     {
         final Path target = this.testDir.resolve(Paths.get(resourcePath).getFileName());
         
         try (final InputStream input = this.getClass().getResourceAsStream(resourcePath))
         {
             Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
         }
         
         final FileRepresentation fileRep = new FileRepresentation(target.toFile(), mediaType);
         return fileRep;
     }
     
     /**
      * Retrieves the asserted statements of a given artifact from the Server as a String.
      * 
      * @param artifactUri
      *            The URI of the artifact requested
      * @param mediaType
      *            The format in which statements should be retrieved
      * @return The artifact's asserted statements represented as a String
      * @throws Exception
      */
     protected String getArtifactAsString(final String artifactUri, final MediaType mediaType) throws Exception
     {
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         return results.getText();
     }
     
     /**
      * Copied from sshj net.schmizz.sshj.util.BasicFixture.java
      * 
      * @return
      */
     private int getFreePort()
     {
         try
         {
             ServerSocket s = null;
             try
             {
                 s = new ServerSocket(0);
                 s.setReuseAddress(true);
                 return s.getLocalPort();
             }
             finally
             {
                 if(s != null)
                 {
                     s.close();
                 }
             }
         }
         catch(final IOException e)
         {
             throw new RuntimeException(e);
         }
     }
     
     /**
      * Override this to change the test aliases for a given test.
      * 
      * @return A {@link Model} containing the statements relevant to test aliases.
      * @throws IOException
      * @throws UnsupportedRDFormatException
      * @throws RDFParseException
      */
     protected Model getTestAliases() throws RDFParseException, UnsupportedRDFormatException, IOException
     {
         final String configuration =
                 IOUtils.toString(this.getClass().getResourceAsStream("/test/test-alias.ttl"), StandardCharsets.UTF_8);
         
         return Rio.parse(new StringReader(configuration), "", RDFFormat.TURTLE);
     }
     
     /**
      * Returns the URI that can be used to access the given path.
      * 
      * @param path
      *            The path on the temporary test server to access. If the path does not start with a
      *            slash one will be added.
      * @return A full URI that can be used to dereference the given path on the test server.
      */
     protected String getUrl(final String path)
     {
         if(!path.startsWith("/"))
         {
             return "http://localhost:" + this.TEST_PORT + "/podd/" + path;
         }
         else
         {
             return "http://localhost:" + this.TEST_PORT + "/podd" + path;
         }
     }
     
     /**
      * Loads a test artifact in RDF/XML format.
      * 
      * @param resourceName
      * @return The loaded artifact's URI
      * @throws Exception
      */
     protected String loadTestArtifact(final String resourceName) throws Exception
     {
         return this.loadTestArtifact(resourceName, MediaType.APPLICATION_RDF_XML).getOntologyIRI().toString();
     }
     
     /**
      * Loads a test artifact in a given format.
      * 
      * @param resourceName
      * @param mediaType
      *            Specifies the media type of the resource to load (e.g. RDF/XML, Turtle)
      * @return An InferredOWLOntologyID identifying the loaded artifact
      * @throws Exception
      */
     protected InferredOWLOntologyID loadTestArtifact(final String resourceName, final MediaType mediaType)
         throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input = this.buildRepresentationFromResource(resourceName, mediaType);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: results (expecting the added artifact's ontology IRI)
         final String body = results.getText();
         
         this.log.info(body);
         this.assertFreemarker(body);
         
         final Collection<InferredOWLOntologyID> ontologyIDs = OntologyUtils.stringToOntologyID(body, RDFFormat.TURTLE);
         
         Assert.assertEquals("Should have got only 1 Ontology ID", 1, ontologyIDs.size());
         return ontologyIDs.iterator().next();
     }
     
     /**
      * Create a new server for each test.
      * 
      * State will only be shared when they use a common database.
      * 
      * @throws Exception
      */
     @Before
     public void setUp() throws Exception
     {
         this.component = new Component();
         
         this.TEST_PORT = this.getFreePort();
         
         // Add a new HTTP server listening on the given TEST_PORT.
         this.component.getServers().add(Protocol.HTTP, this.TEST_PORT);
         
         this.component.getClients().add(Protocol.CLAP);
         this.component.getClients().add(Protocol.HTTP);
         
         final PoddWebServiceApplication nextApplication = new PoddWebServiceApplicationImpl();
         
         // Attach the sample application.
         this.component.getDefaultHost().attach(
         // PropertyUtil.get(OasProps.PROP_WS_URI_PATH, OasProps.DEF_WS_URI_PATH),
                 "/podd/", nextApplication);
         
         nextApplication.setAliasesConfiguration(this.getTestAliases());
         
         // The application cannot be setup properly until it is attached, as it requires
         // Application.getContext() to not return null
         ApplicationUtils.setupApplication(nextApplication, nextApplication.getContext());
         
         // Start the component.
         this.component.start();
         
         this.testDir = this.tempDirectory.newFolder(this.getClass().getSimpleName()).toPath();
     }
     
     /**
      * Stop and nullify the test server object after each test.
      * 
      * NOTE: Does not clear any databases.
      * 
      * @throws Exception
      */
     @After
     public void tearDown() throws Exception
     {
         // Stop the component
         if(this.component != null)
         {
             this.component.stop();
         }
         
         // nullify the reference to the component
         this.component = null;
     }
     
 }
