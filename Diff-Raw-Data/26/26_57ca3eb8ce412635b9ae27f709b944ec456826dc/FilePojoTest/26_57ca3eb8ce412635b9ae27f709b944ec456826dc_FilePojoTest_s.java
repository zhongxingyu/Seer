 package eu.dm2e.ws.services.file;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.URI;
 
 import org.junit.Test;
 
 import com.google.gson.JsonObject;
 
 import eu.dm2e.ws.OmnomUnitTest;
 import eu.dm2e.ws.api.FilePojo;
 import eu.dm2e.ws.api.JobPojo;
 import eu.dm2e.ws.api.SerializablePojo;
 import eu.dm2e.ws.api.json.OmnomJsonSerializer;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 
 public class FilePojoTest extends OmnomUnitTest{
 	
 	@Test
 	public void serializeToJson() {
 		
 		final String retUri = "http://foo/bar.ext";
 		final String editUri = "http://foo/bar.ext/edit";
 		final String fileStatus = "AVAILABLE";
 		final String jobId = "http://job/1";
 		
 		{
 			FilePojo fp = new FilePojo();
 			fp.setFileSize(100L);
 			fp.setFileRetrievalURI(URI.create(retUri));
 			fp.setFileEditURI(editUri);
 			fp.setFileStatus(fileStatus);
 			
 			JsonObject expect = new JsonObject();
 			expect.addProperty("fileSize", 100L);
 			expect.addProperty("fileRetrievalURI", retUri);
 			expect.addProperty("fileEditURI", editUri);
 			expect.addProperty("fileStatus", fileStatus);
 			expect.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, fp.getRDFClassUri());
 			
 			assertEquals(testGson.toJson(expect), OmnomJsonSerializer.serializeToJSON(fp, FilePojo.class));
 			assertEquals(testGson.toJson(expect), fp.toJson());
 		}
 		{
 			JobPojo job = new JobPojo();
 			job.setId(jobId);
 			
 			FilePojo fp = new FilePojo();
 			fp.setFileSize(100L);
 			fp.setFileStatus(fileStatus);
 			fp.setGeneratorJob(job);
 			
 			JsonObject expect = new JsonObject();
 			expect.addProperty("fileSize", 100L);
 			expect.addProperty("fileStatus", fileStatus);
 			JsonObject jobObj = new JsonObject();
 			jobObj.addProperty(SerializablePojo.JSON_FIELD_ID, jobId);
 			expect.add("generatorJob", jobObj);
 			expect.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, fp.getRDFClassUri());
 			
 			assertEquals(testGson.toJson(expect), OmnomJsonSerializer.serializeToJSON(fp, FilePojo.class));
 			assertEquals(testGson.toJson(expect), fp.toJson());
 		}
 		
 	}
 
 	@Test
 	public void testReadFilePojo() {
 		GrafeoImpl g = new GrafeoImpl();
 		String fileUri = "http://foo.bar/baz";
 		StringBuilder fileMetaStr = new StringBuilder();
 		String fileLocationShouldBe = "/foo/bar";
 		String fileRetrievalURIShouldBe = fileUri;
 		fileMetaStr.append("@prefix omnom: <http://onto.dm2e.eu/omnom/>. \n");
 		fileMetaStr.append("@prefix dct: <http://purl.org/dc/terms/>. \n");
 		fileMetaStr.append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. \n");
 		fileMetaStr.append("<").append(fileUri).append("> omnom:fileLocation \"").append(fileLocationShouldBe).append("\". \n");
 		fileMetaStr.append("<").append(fileUri).append("> omnom:fileRetrievalURI <").append(fileRetrievalURIShouldBe).append(">. \n");
 		fileMetaStr.append("<").append(fileUri).append("> dct:extent \"123456\". \n");
 		g.readHeuristically(fileMetaStr.toString());
 		FilePojo fp = g.getObjectMapper().getObject(FilePojo.class, fileUri);
 		assertEquals(fileUri, fp.getId());
 		assertEquals(123456L, fp.getFileSize());
		assertEquals(fileLocationShouldBe, fp.getFileLocation());
 		assertEquals(fileRetrievalURIShouldBe, fp.getFileRetrievalURI().toString());
 //		log.info(g.getNTriples());
 //		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testRoundTripPojo() {
 		GrafeoImpl g1 = new GrafeoImpl()
 				 , g2 = new GrafeoImpl();
 		String fileUri = "http://foo.bar/baz"
 		      , fileLocationShouldBe = "/foo/bar"
 		      , fileRetrievalURIShouldBe = fileUri;
 		
 		FilePojo fp1 = new FilePojo();
 		fp1.setId(fileUri);
 		fp1.setFileRetrievalURI(URI.create(fileRetrievalURIShouldBe));
		fp1.setFileLocation(fileLocationShouldBe);
 		
 		g1.getObjectMapper().addObject(fp1);
 		FilePojo fp2 = g1.getObjectMapper().getObject(FilePojo.class, fileUri);
 		g2.getObjectMapper().addObject(fp2);
 		assertEquals(g1.getCanonicalNTriples(), g2.getCanonicalNTriples());
 		log.info(g2.getNTriples());
 		
 //		FilePojo fp = g.getObject(FilePojo.class, g.resource(fileUri));
 //		assertEquals(fileUri, fp.getId());
 //		assertEquals(123456L, fp.getFileSize());
 //		assertEquals(fileLocationShouldBe, fp.getFileLocation());
 //		assertEquals(fileRetrievalURIShouldBe, fp.getFileRetrievalURI());
 ////		log.info(g.getNTriples());
 ////		fail("Not yet implemented");
 	}
 
 }
