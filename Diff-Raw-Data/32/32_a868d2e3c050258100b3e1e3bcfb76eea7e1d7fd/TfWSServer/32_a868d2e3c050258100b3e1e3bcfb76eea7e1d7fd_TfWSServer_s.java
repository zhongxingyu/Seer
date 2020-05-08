 package org.bioinfo.infrared.ws.server.rest.regulatory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.commons.utils.StringUtils;
 import org.bioinfo.infrared.core.cellbase.Gene;
 import org.bioinfo.infrared.core.cellbase.Protein;
 import org.bioinfo.infrared.core.cellbase.ProteinFeature;
 import org.bioinfo.infrared.core.cellbase.ProteinXref;
 import org.bioinfo.infrared.core.cellbase.Pwm;
 import org.bioinfo.infrared.core.cellbase.Transcript;
 import org.bioinfo.infrared.lib.api.GeneDBAdaptor;
 import org.bioinfo.infrared.lib.api.MirnaDBAdaptor;
 import org.bioinfo.infrared.lib.api.ProteinDBAdaptor;
 import org.bioinfo.infrared.lib.api.TfbsDBAdaptor;
 import org.bioinfo.infrared.lib.api.TranscriptDBAdaptor;
 import org.bioinfo.infrared.ws.server.rest.exception.VersionException;
 
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 
 @Path("/{version}/{species}/regulatory/tf")
 @Produces("text/plain")
 public class TfWSServer extends RegulatoryWSServer {
 
 	public TfWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
 		super(version, species, uriInfo);
 	}
 
 	
 	
 	@GET
	@Path("/{tfId}/info") // Devuelve los TFBSs para el TFId que le das
 	public Response getTfInfo(@PathParam("tfId") String query) {
 		try {
			TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
			return generateResponse(query, tfbsDBAdaptor.getAllByTfGeneNameList(StringUtils.toList(query, ",")));
			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("getTfInfo", e.toString());
 		}
 	}
 	
 
 	@GET
 	@Path("/{tfId}/fullinfo") // Devuelve los TFBSs para el TFId que le das
 	public Response getTfFullInfo(@PathParam("tfId") String query) {
 		try {
 			
 			ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species);
 			GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species);
 			TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species);
 			TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
 			
 			List<List<Gene>> genes = geneDBAdaptor.getAllByTfNameList(StringUtils.toList(query, ","));
 			List<String> ensemblGeneList = new ArrayList<String>();
 			List<String> externalNameList = new ArrayList<String>();
 			for (List<Gene> g : genes) {
 				if (g.size()>0){
 					ensemblGeneList.add(g.get(0).getStableId());
 					externalNameList.add(g.get(0).getExternalName());
 				}
 			}
 			
 			List<List<Protein>> proteinList = proteinDBAdaptor.getAllByGeneNameList(externalNameList);
 			List<List<Transcript>> transcriptList = transcriptDBAdaptor.getAllByProteinNameList(externalNameList);
 			List<List<Gene>> targetGeneList = geneDBAdaptor.getAllByTfList(StringUtils.toList(query, ","));
 			List<List<Pwm>> pwmGeneList =  tfbsDBAdaptor.getAllPwmByTfGeneNameList(StringUtils.toList(query, ","));
 			
 			List<List<ProteinXref>> proteinXrefList = proteinDBAdaptor.getAllProteinXrefsByProteinNameList(externalNameList);
 			List<List<ProteinFeature>> proteinFeature = proteinDBAdaptor.getAllProteinFeaturesByProteinXrefList(externalNameList);
 			
 			StringBuilder response = new StringBuilder();
 			response.append("[");
 			for (int i = 0; i < genes.size(); i++) {
 				response.append("{");
 				response.append("\"proteins\":"+gson.toJson(proteinList.get(i))+",");
 				response.append("\"gene\":"+gson.toJson(genes.get(i).get(0))+",");
 				response.append("\"transcripts\":"+gson.toJson(transcriptList.get(i))+",");
 				response.append("\"pwm\":"+gson.toJson(pwmGeneList.get(i))+",");
 				response.append("\"targetGenes\":"+gson.toJson(targetGeneList.get(i))+",");
 				response.append("\"protein_xref\":"+gson.toJson(proteinXrefList.get(i))+",");
 				response.append("\"protein_feature\":"+gson.toJson(proteinFeature.get(i))+"");
 				response.append("},");
 			}
 			response.append("]");
 			//Remove the last comma
 			response.replace(response.length()-2, response.length()-1, "");
 			
 			return  generateResponse(query,Arrays.asList(response));
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("getTfFullInfo", e.toString());
 		}
 	}
 	
 	@GET
 	@Path("/{tfId}/tfbs")
 	public Response getAllByTfbs(@PathParam("tfId") String query) {
 		try {
 			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
 			return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ",")));
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("getAllByTfbs", e.toString());
 		}
 	}
 	
 	
 	@GET
 	@Path("/{tfId}/gene")
 	public Response getEnsemblGenes(@PathParam("tfId") String query) {
 		try {
 			GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species);
 			return  generateResponse(query, geneDBAdaptor.getAllByTfList(StringUtils.toList(query, ",")));
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("getEnsemblGenes", e.toString());
 		}
 	}
 	
 	
 	@GET
 	@Path("/{tfId}/pwm")
 	public Response getAllPwms(@PathParam("tfId") String query) {
 		try {
 			TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
 			return generateResponse(query, tfbsDBAdaptor.getAllPwmByTfGeneNameList(StringUtils.toList(query, ",")));
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("getAllPwms", e.toString());
 		}
 	}
 	
 	
 	@GET
 	@Path("/annotation")
 	public Response getAnnotation(@DefaultValue("")@QueryParam("celltype") String celltype) {
 		try {
 			TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
 			List<Object> results;
 			if (celltype.equals("")){
 				results = tfbsDBAdaptor.getAllAnnotation();
 			}
 			else{
 				results = tfbsDBAdaptor.getAllAnnotationByCellTypeList(StringUtils.toList(celltype, ","));
 			}
 			List<String> lista = new ArrayList<String>();			
 			
 			for (Object result : results) {
 				lista.add(((Object [])result)[0].toString()+"\t" + ((Object [])result)[1].toString());
 			}
 			return  generateResponse(new String(), lista);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("getAnnotation", e.toString());
 		}
 	}
 
 	@GET
 	public Response getHelp() {
 		return help();
 	}
 	
 	@GET
 	@Path("/help")
 	public Response help() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Resources/actions\n\n");
 		sb.append("- info: Get information about this transcription factor (TF).\n\n");
 		sb.append("- tfbs: Get all transcription factor binding sites (TFBSs) for this TF.\n");
 		sb.append(" Output columns: TF name, target gene name, chromosome, start, end, cell type, sequence, score.\n\n");
 		sb.append("- gene: Get all genes regulated by this TF.\n");
 		sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
 		sb.append("- pwm: Get all position weight matrices associated to this TF.\n");
 		sb.append(" Output columns: TF Name, type, frequency_matrix, description, source, length, jaspar_accession.\n\n");
 		
 		return createOkResponse(sb.toString());
 	}
 }
