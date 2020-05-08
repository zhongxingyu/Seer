 package eu.dm2e.ws.tests.unit.api;
 
 
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import eu.dm2e.grafeo.gom.SerializablePojo;
 import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.api.JobPojo;
 import eu.dm2e.ws.model.JobStatus;
 import eu.dm2e.ws.tests.OmnomUnitTest;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.junit.Assert.assertEquals;
 
 public class JobPojoTest extends OmnomUnitTest {
     private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	@Test
 	public void testSerializeToJson() {
 		
 		JobPojo job = new JobPojo();
 		job.setStatus(JobStatus.STARTED);
 
 		JsonObject expect = new JsonObject();
 		expect.addProperty(SerializablePojo.JSON_FIELD_UUID, job.getUuid());
         expect.addProperty(NS.OMNOM.PROP_JOB_STATUS, JobStatus.STARTED.name());
         expect.addProperty(NS.OMNOM.PROP_JOB_LATEST_RESULT, job.getLatestResult());
         expect.add(NS.OMNOM.PROP_LOG_ENTRY, new JsonArray());
 		expect.add(NS.OMNOM.PROP_ASSIGNMENT, new JsonArray());
 		expect.addProperty(NS.DCTERMS.PROP_MODIFIED, job.getModified().toString());
 		expect.addProperty(NS.DCTERMS.PROP_CREATED, job.getCreated().toString());
         //expect.addProperty(NS.OMNOM.PROP_JOB_PARENT, job.getParentJob().toString());
         expect.add(NS.OMNOM.PROP_FINISHED_JOB, new JsonArray());
         expect.add(NS.OMNOM.PROP_RUNNING_JOB, new JsonArray());
         //expect.add(NS.OMNOM.PROP_JOB_STARTED, new JsonArray());
         expect.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, job.getRDFClassUri());
 
 		assertEquals(job, GrafeoJsonSerializer.deserializeFromJSON(expect.toString(), JobPojo.class));
 		String expectString =  testGson.toJson(expect);
         // expectString = expectString.replaceAll("\"201\\d[^\"]+\"", "\"\"");
         String actual = GrafeoJsonSerializer.serializeToJSON(job, JobPojo.class);
         // actual = actual.replaceAll("\"201\\d[^\"]+\"", "\"\"");log.info("Expected: " + expectString);
         log.info("Actual: " + actual);
         assertEquals(expectString, actual);
 		assertEquals(testGson.toJson(expect), job.toJson());
 	}
 	
 	@Test
 	public void testDeserializeFromJson() {
 
 		JobPojo expectedJob = new JobPojo();
 		
 		JsonObject expectedJson = new JsonObject();
 		expectedJson.addProperty(NS.OMNOM.PROP_JOB_STATUS, JobStatus.NOT_STARTED.name());
 		expectedJson.addProperty(SerializablePojo.JSON_FIELD_UUID, createUUID());
 //		expectedJson.add("logEntries", new JsonArray());
 //		expectedJson.add("outputParameterAssignments", new JsonArray());
 		expectedJson.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, new JobPojo().getRDFClassUri());
 		
 		JobPojo deserializedJob = GrafeoJsonSerializer.deserializeFromJSON(expectedJson.toString(), JobPojo.class);
 		
 //		log.info(expectedJson.toString());
 		
 		assertEquals(expectedJob, deserializedJob);
 //		assertEquals(testGson.toJson(expectedJson), OmnomJsonSerializer.serializeToJSON(deserializedJob, LogEntryPojo.class));
 //		assertEquals(testGson.toJson(expectedJson), deserializedJob.toJson());
 	}
 
 }
