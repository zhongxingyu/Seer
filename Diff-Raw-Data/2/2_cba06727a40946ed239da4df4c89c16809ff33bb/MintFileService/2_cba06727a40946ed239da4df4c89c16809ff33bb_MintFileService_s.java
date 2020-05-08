 package eu.dm2e.ws.services.mint;
 
 import java.net.URI;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import eu.dm2e.ws.Config;
 import eu.dm2e.ws.api.FilePojo;
 import eu.dm2e.ws.api.WebservicePojo;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.services.AbstractRDFService;
 
 @Path("/mint-file")
 public class MintFileService extends AbstractRDFService {
 	
 	protected MintApiTranslator mintApiTranslator = new MintApiTranslator(
 			Config.getString("dm2e.service.mint-file.base_uri"),
 			Config.getString("dm2e.service.mint-file.mint_base"),
 			Config.getString("dm2e.service.mint-file.username"),
 			Config.getString("dm2e.service.mint-file.password")
 	);
 	
 	@Override
 	public WebservicePojo getWebServicePojo() {
 		WebservicePojo ws = super.getWebServicePojo();
 		ws.setLabel("MINT file service yay.");
 		return ws;
 	}
 	
 	
 	@Produces({
 		MediaType.WILDCARD
 	})
 	@GET
	@Path("/")
 	public Response getFileList() {
 		Grafeo g = mintApiTranslator.buildGrafeoFromMintFiles();
 		
 		return Response.ok().entity(getResponseEntity(g)).build();
 	}
 
 	/**
 	 * Retrieve metadata/file data for a file stored in MINT
 	 * 
 	 * @param fileId
 	 * @return
 	 */
 	@GET
 	@Path("{id}")
 	public Response getFileById() {
 		return getFile(getRequestUriWithoutQuery());
 	}
 
 	/**
 	 * Decides whether to fire the get method for file data or metadata.
 	 * 
 	 * @param uri
 	 * @return
 	 */
 	Response getFile(URI uri) {
         log.info("File requested: " + uri);
 		// if the accept header is a RDF type, send metadata, otherwise data
 		if (expectsMetadataResponse()) {
 			log.info("METADATA will be sent");
             return getFileMetaDataByUri(uri);
 		} else {
             log.info("FILE will be sent");
             return getFileDataByUri(uri);
 		}
 	}
 
 
 	private Response getFileMetaDataByUri(URI uri) {
 		FilePojo filePojo = mintApiTranslator.getFilePojoForUri(uri);
 		if (null == filePojo)
 			return Response.status(404).entity("No such file in MINT.").build();
 		Response resp;
 		if (expectsRdfResponse()) {
 			Grafeo outG = new GrafeoImpl();
 			outG.getObjectMapper().addObject(filePojo);
 			resp = getResponse(outG);
 		} else if (expectsRdfResponse()) {
 			resp = Response
 					.ok()
 					.type(MediaType.APPLICATION_JSON)
 					.entity(filePojo.toJson())
 					.build();
 		} else {
 			return throwServiceError("Unhandled metadata type.");
 		}
 		return resp	;
 	}
 
 	
 	private Response getFileDataByUri(URI uri) {
 		FilePojo filePojo = mintApiTranslator.getFilePojoForUri(uri);
 		if (null == filePojo)
 			return Response.status(404).entity("No such file in MINT.").build();
 		return Response.seeOther(filePojo.getFileRetrievalURI()).build();
 	}
 
 }
