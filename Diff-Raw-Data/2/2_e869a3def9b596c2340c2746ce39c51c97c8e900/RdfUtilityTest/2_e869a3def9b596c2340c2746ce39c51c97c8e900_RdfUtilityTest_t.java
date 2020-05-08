 /**
  * 
  */
 package com.github.podd.utils.test;
 
 import java.io.InputStream;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.sail.memory.MemoryStore;
 
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.RdfUtility;
 
 /**
  * @author kutila
  * 
  */
 public class RdfUtilityTest
 {
     
     public static final String TEST_ARTIFACT_INVALID_3_TOP_OBJECTS = "/test/artifacts/bad-3-topobjects.ttl";
     
     private final Object[][] testDatas = new Object[][] {
             { "/test/artifacts/basic-20130206.ttl", RDFFormat.TURTLE, true, 0 },
             
             { "/test/artifacts/connected-1-object.rdf", RDFFormat.RDFXML, true, 0 },
             
             { "/test/artifacts/connected-cycle.rdf", RDFFormat.RDFXML, true, 0 },
             
             // an object has two links from its parent
             { "/test/artifacts/connected-multiple-paths.ttl", RDFFormat.TURTLE, true, 0 },
             
             { "/test/artifacts/disconnected-1-object.rdf", RDFFormat.RDFXML, false, 2 },
             
             { "/test/artifacts/basic-1-internal-object.rdf", RDFFormat.RDFXML, true, 0 },
             
             // disconnected segment has cycles within it
             { "/test/artifacts/disconnected-cycles.ttl", RDFFormat.TURTLE, false, 13 }, };
     
     @Test
     public void testFindDisconnectedNodes() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:concrete:random");
         
         Repository tempRepository = null;
         RepositoryConnection connection = null;
         
         try
         {
             // create a temporary in-memory repository
             tempRepository = new SailRepository(new MemoryStore());
             tempRepository.initialize();
             connection = tempRepository.getConnection();
             connection.begin();
             
             for(final Object[] testData : this.testDatas)
             {
                 final InputStream inputStream = this.getClass().getResourceAsStream((String)testData[0]);
                 Assert.assertNotNull("Null resource", inputStream);
                 
                 // load artifact statements into repository
                 connection.add(inputStream, "", (RDFFormat)testData[1], context);
                 
                 URI root = null;
                 final RepositoryResult<Statement> statements =
                         connection.getStatements(null, PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT, null, false, context);
                 if(statements.hasNext())
                 {
                     root = (URI)statements.next().getSubject();
                 }
                 else
                 {
                     Assert.fail("Could not find root object");
                 }
                 
                 final Set<URI> disconnectedObjects = RdfUtility.findDisconnectedNodes(root, connection, context);
                 Assert.assertEquals("Not the expected validity", testData[3], disconnectedObjects.size());
                 
                 connection.clear();
             }
             
         }
         finally
         {
             if(connection != null && connection.isOpen())
             {
                 connection.rollback();
                 connection.close();
             }
             tempRepository.shutDown();
         }
         
     }
     
     @Test
     public void testInputStreamToModel() throws Exception
     {
         final InputStream resourceStream = this.getClass().getResourceAsStream("/ontologies/poddScience.owl");
         final Model model = RdfUtility.inputStreamToModel(resourceStream, RDFFormat.RDFXML);
         
         Assert.assertNotNull("Model was NULL", model);
         Assert.assertFalse("Model was empty", model.isEmpty());
        Assert.assertEquals("Not the expected number of statements in the Model", 1229, model.size());
     }
     
     @Test
     public void testisConnectedStructure() throws Exception
     {
         for(final Object[] testData : this.testDatas)
         {
             final InputStream inputStream = this.getClass().getResourceAsStream((String)testData[0]);
             Assert.assertNotNull("Null resource", inputStream);
             
             final boolean isConnected = RdfUtility.isConnectedStructure(inputStream, (RDFFormat)testData[1]);
             Assert.assertEquals("Not the expected validity", testData[2], isConnected);
         }
     }
     
     @Test
     public void testisConnectedStructureWithMultipleTopObjects() throws Exception
     {
         final InputStream inputStream =
                 this.getClass().getResourceAsStream(RdfUtilityTest.TEST_ARTIFACT_INVALID_3_TOP_OBJECTS);
         Assert.assertNotNull("Null resource", inputStream);
         
         final boolean isConnected = RdfUtility.isConnectedStructure(inputStream, RDFFormat.TURTLE);
         Assert.assertEquals("Not the expected validity", false, isConnected);
     }
     
 }
