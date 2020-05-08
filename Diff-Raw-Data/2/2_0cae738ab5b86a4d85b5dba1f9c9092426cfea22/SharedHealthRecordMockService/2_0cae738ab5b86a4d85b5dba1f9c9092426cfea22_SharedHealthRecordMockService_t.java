 package org.jembi.rhea.mocks;
 
 import java.io.IOException;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jembi.rhea.MocksUtil;
 
 import ca.uhn.hl7v2.model.Message;
 import ca.uhn.hl7v2.model.v25.message.ORU_R01;
 import ca.uhn.hl7v2.parser.GenericParser;
 import ca.uhn.hl7v2.parser.Parser;
 
 @Path("/openmrs/ws/rest/RHEA")
 public class SharedHealthRecordMockService {
 	
 	Log log = LogFactory.getLog(this.getClass());
 	
 	@Path("patient/encounters")
 	@POST
 	public Response saveEncounter(String body, @QueryParam("patientId") String pid, @QueryParam("idType") String idType) {
 		log.info("Called mock Shared Health Record: save encounter");
 		
 		log.info("Body recieved: " + body);
 		
 		log.info("Attempting to parser the body as a HL7v2 ORU_R01 message...");
 		
 		Parser p = new GenericParser();
 		
 		Message hl7 = null;
 		try {
 			hl7 = p.parse(body);
 			
 			ORU_R01 oruMsg = (ORU_R01) hl7;
 			
 			if (oruMsg != null) {
 				log.info("Successfully parsed HL7v2 ORU_R01 message!");
 				return Response.created(null).build();
 			}
 		} catch (Exception e) {
 			log.error("Parsing failed: ", e);
 			return Response.status(400).entity("Failed to parse HL7 message: " + e).build();
 		}
 		
 		return Response.status(500).build();
 	}
 	
 	@Path("patient/encounters")
 	@GET
 	@Produces("text/xml")
 	public String queryForEncounters(@QueryParam("patientId") String pid, @QueryParam("idType") String idType) throws IOException {
 		log.info("Called mock Shared Health Record: query for encounters");
 		
 		log.info("Returning list of encounters for patient: " + pid + "...");
 		return MocksUtil.getFileAsString("/hl7/ORU_R01.xml");
 	}
 	
 	// === Services below this line are not used ===
 	
	@Path("patient/{pid}/encounter/{eid}")
 	@GET
 	@Produces("text/xml")
 	public String getEncounter(@PathParam("pid") String pid, @PathParam("eid") String eid) throws IOException {
 		log.info("Called mock Shared Health Record: get encounter");
 		
 		log.info("Returning encounter: " + eid + " for pateint: " + pid + "...");
 		return MocksUtil.getFileAsString("/hl7/ORU_R01.xml");
 	}
 	
 }
