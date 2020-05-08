 package org.bioinfo.infrared.ws.server.rest.genomic;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.infrared.core.cellbase.Transcript;
 import org.bioinfo.infrared.lib.api.GenomicVariantEffectDBAdaptor;
 import org.bioinfo.infrared.lib.api.TranscriptDBAdaptor;
 import org.bioinfo.infrared.lib.common.GenomicVariant;
import org.bioinfo.infrared.lib.impl.hibernate.GenomicVariantEffectDBAdaptor;
 import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
 import org.bioinfo.infrared.ws.server.rest.exception.VersionException;
 
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.multipart.FormDataParam;
 
 @Path("/{version}/{species}/genomic/variant")
 @Produces("text/plain")
 public class VariantWSServer extends GenericRestWSServer {
 	
 	protected static HashMap<String, List<Transcript>> CACHE_TRANSCRIPT = new HashMap<String, List<Transcript>>();
 
 	public VariantWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
 		super(version, species, uriInfo);
 		
 		if (CACHE_TRANSCRIPT.get(this.species) == null){
 //			logger.debug("\tCACHE_TRANSCRIPT is null");
 			long t0 = System.currentTimeMillis();
 			TranscriptDBAdaptor adaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species);
 //			CACHE_TRANSCRIPT.put(species, adaptor.getAll());
 			logger.debug("\t\tFilling up for " + this.species + " in " + (System.currentTimeMillis() - t0) + " ms");
 //			logger.debug("\t\tNumber of transcripts: " + CACHE_TRANSCRIPT.get(this.species).size());
 		}
 	}
 
 	@GET
 	@Path("/{positionId}/consequence_type")
 	public Response getConsequenceTypeByPositionByGet(	@PathParam("positionId") String query, 
 														@DefaultValue("true")@QueryParam("features")String features,
 														@DefaultValue("true")@QueryParam("variation")String variation,
 														@DefaultValue("true")@QueryParam("regulatory")String regulatory,
 														@DefaultValue("true")@QueryParam("disease")String diseases)
 	{
 		try {
 			return getConsequenceTypeByPosition(query, features, variation, regulatory, diseases);
 		} catch (Exception e) {
 			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 		}
 	}
 	
 	@POST
 	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA, 
 	@Path("/consequence_type")
 	public Response getConsequenceTypeByPositionByPost(@FormDataParam("of") String outputFormat, @FormDataParam("variants") String postQuery) {
 		String features = "true";
 		String variation = "true"; 
 		String regulatory = "true";
 		String diseases = "true";
 //		System.out.println("VariantWSServer ==> postQuery: "+postQuery);
 //		postQuery = postQuery.replace("?", "%");
 //		
 //		String query = Arrays.asList(postQuery.split("%")).get(0);
 //		String queryParams =  Arrays.asList(postQuery.split("%")).get(1);
 //		if (queryParams.toLowerCase().contains("features=false")){
 //			features = "false";
 //		}
 //		if (queryParams.toLowerCase().contains("regulatory=false")){
 //			regulatory = "false";
 //		}
 //		if (queryParams.toLowerCase().contains("variation=false")){
 //			variation = "false";
 //		}
 //		if (queryParams.toLowerCase().contains("disease=false")){
 //			diseases = "false";
 //		}
 		
 		return getConsequenceTypeByPosition(postQuery, features, variation, regulatory, diseases);
 	}
 	
 	
 	private Response getConsequenceTypeByPosition(String query, String features, String variation, String regulatory, String diseases){
 		try {
 			
 			System.out.println("variants: "+query);
 			if(query.length() > 100){
 				logger.debug("VARIANT TOOL WS: " + query.substring(0, 99) + "....");
 			}
 			else{
 				logger.debug("VARIANT TOOL WS: " + query);
 			}
 			List<GenomicVariant> variants = GenomicVariant.parseVariants(query);
 			System.out.println("number of variants: "+variants.size());
 //			GenomicVariantEffect gv = new GenomicVariantEffect(this.species);
 			GenomicVariantEffectDBAdaptor gv = dbAdaptorFactory.getGenomicVariantEffectDBAdaptor(species);
 //			if (features.equalsIgnoreCase("true")){
 //				gv.setShowFeatures(true);
 //			}
 //			else{
 //				gv.setShowFeatures(false);
 //			}
 //			
 //			if (variation.equalsIgnoreCase("true")){
 //				gv.setShowVariation(true);
 //			}
 //			else{
 //				gv.setShowVariation(false);
 //			}
 //			
 //			if (regulatory.equalsIgnoreCase("true")){
 //				gv.setShowRegulatory(true);
 //			}
 //			else{
 //				gv.setShowRegulatory(false);
 //			}
 //			
 //			if (diseases.equalsIgnoreCase("true")){
 //				gv.setShowDiseases(true);
 //			}
 //			else{
 //				gv.setShowDiseases(false);
 //			}
 			
 //			return generateResponse(query, gv.getConsequenceType(variants, CACHE_TRANSCRIPT.get(this.species)));
 			return generateResponse(query, gv.getAllConsequenceTypeByVariantList(variants));
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 		}
 	}
 	
 }
