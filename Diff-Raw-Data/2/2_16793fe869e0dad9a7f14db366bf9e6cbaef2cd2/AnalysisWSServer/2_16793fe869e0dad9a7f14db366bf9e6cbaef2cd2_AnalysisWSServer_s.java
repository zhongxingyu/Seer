 package org.bioinfo.gcsa.ws;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.commons.utils.StringUtils;
 import org.bioinfo.gcsa.lib.GcsaUtils;
 import org.bioinfo.gcsa.lib.analysis.AnalysisExecutionException;
 import org.bioinfo.gcsa.lib.analysis.AnalysisJobExecuter;
 import org.bioinfo.gcsa.lib.analysis.beans.Analysis;
 import org.bioinfo.gcsa.lib.analysis.beans.Execution;
 import org.bioinfo.gcsa.lib.analysis.beans.InputParam;
 import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
 import org.bioinfo.gcsa.lib.account.beans.Plugin;
 import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
 
 @Path("/analysis")
 public class AnalysisWSServer extends GenericWSServer {
 	AnalysisJobExecuter aje;
 	String baseUrl;
 
 	public AnalysisWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest)
 			throws IOException {
 		super(uriInfo, httpServletRequest);
 		baseUrl = uriInfo.getBaseUri().toString();
 	}
 
 	@GET
 	@Path("/{analysis}")
 	public Response help1(@DefaultValue("") @PathParam("analysis") String analysis) {
 		try {
 			aje = new AnalysisJobExecuter(analysis);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Analysis not found.");
 		}
 		return createOkResponse(aje.help(baseUrl));
 	}
 
 	@GET
 	@Path("/{analysis}/help")
 	public Response help2(@DefaultValue("") @PathParam("analysis") String analysis) {
 		try {
 			aje = new AnalysisJobExecuter(analysis);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Analysis not found.");
 		}
 		return createOkResponse(aje.help(baseUrl));
 	}
 
 	@GET
 	@Path("/{analysis}/params")
 	public Response showParams(@DefaultValue("") @PathParam("analysis") String analysis) {
 		try {
 			aje = new AnalysisJobExecuter(analysis);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Analysis not found.");
 		}
 		return createOkResponse(aje.params());
 	}
 
 	@GET
 	@Path("/{analysis}/test")
 	public Response test(@DefaultValue("") @PathParam("analysis") String analysis) {
 		try {
 			aje = new AnalysisJobExecuter(analysis);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Analysis not found.");
 		}
 
 		// Create job
 		String jobId = cloudSessionManager.createJob("", null, "", "", new ArrayList<String>(), "", sessionId);
 		String jobFolder = "/tmp/";
 
 		return createOkResponse(aje.test(jobId, jobFolder));
 	}
 
 	@GET
 	@Path("/{analysis}/status")
 	public Response status(@DefaultValue("") @PathParam("analysis") String analysis,
 			@DefaultValue("") @QueryParam("jobid") String jobId) {
 		try {
 			aje = new AnalysisJobExecuter(analysis);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Analysis not found.");
 		}
 
 		return createOkResponse(aje.status(jobId));
 	}
 
 	@GET
 	@Path("/{analysis}/run")
 	public Response analysisGet(@DefaultValue("") @PathParam("analysis") String analysis) {
 		// MultivaluedMap<String, String> params =
 		// this.uriInfo.getQueryParameters();
 		System.out.println("**GET executed***");
 		System.out.println("get params: " + params);
 
 		return this.analysis(analysis, params);
 	}
 
 	@POST
 	@Path("/{analysis}/run")
 	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
 	public Response analysisPost(@DefaultValue("") @PathParam("analysis") String analysis,
 			MultivaluedMap<String, String> postParams) {
 		System.out.println("**POST executed***");
 		System.out.println("post params: " + postParams);
 
 		return this.analysis(analysis, postParams);
 	}
 
 	private Response analysis(String analysisStr, MultivaluedMap<String, String> params) {
 		// TODO Comprobar mas cosas antes de crear el analysis job executer
 		// (permisos, etc..)
 
 		if (params.containsKey("sessionid")) {
 			sessionId = params.get("sessionid").get(0);
 			params.remove("sessionid");
 		} else {
 			return createErrorResponse("ERROR: Session is not initialized yet.");
 		}
 		
 		String accountId = null;
 		if (params.containsKey("accountid")) {
 			accountId = params.get("accountid").get(0);
 			params.remove("accountid");
 		} else {
 			return createErrorResponse("ERROR: Session is not initialized yet.");
 		}
 
 		String bucket = null;
 		if (params.containsKey("jobdestinationbucket")) {
 			bucket = params.get("jobdestinationbucket").get(0);
 			params.remove("jobdestinationbucket");
 		} else {
 			return createErrorResponse("ERROR: unspecified destination bucket.");
 		}
 
 		// Jquery put this parameter and it is sent to the tool
 		if (params.containsKey("_")) {
 			params.remove("_");
 		}
 
 		String analysisName = analysisStr;
 		if (analysisStr.contains(".")) {
 			analysisName = analysisStr.split("\\.")[0];
 		}
 
 		String analysisOwner = "system";
 		try {
 			List<Plugin> userAnalysis = cloudSessionManager.getUserAnalysis(sessionId);
 			for (Plugin a : userAnalysis) {
 				if (a.getName().equals(analysisName)) {
 					analysisOwner = a.getOwnerId();
 					break;
 				}
 			}
 		} catch (AccountManagementException e1) {
 			e1.printStackTrace();
 			return createErrorResponse("ERROR: invalid session id.");
 		}
 
 		Analysis analysis = null;
 		try {
 			aje = new AnalysisJobExecuter(analysisStr, analysisOwner);
 			analysis = aje.getAnalysis();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Analysis not found.");
 		}
 
 		Execution execution = null;
 		try {
 			execution = aje.getExecution();
 		} catch (AnalysisExecutionException e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Executable not found.");
 		}
 
 		String jobName = "";
 		if (params.containsKey("jobname")) {
 			jobName = params.get("jobname").get(0);
 			params.remove("jobname");
 		}
 
 		String jobFolder = null;
 		if (params.containsKey("outdir")) {
 			jobFolder = params.get("outdir").get(0);
 			params.remove("outdir");
 		}
 		
 		boolean example = false;
 		if (params.containsKey("example")) {
 			example = Boolean.parseBoolean(params.get("example").get(0));
 			params.remove("example");
 		}
 		
 
 		String toolName = analysis.getId();
 		
 		// Set input param
 		for (InputParam inputParam : execution.getInputParams()) {
 			if (params.containsKey(inputParam.getName())) {
 				List<String> dataIds = Arrays.asList(params.get(inputParam.getName()).get(0).split(","));
 				List<String> dataPaths = new ArrayList<String>();
 				for (String dataId : dataIds) {
 					String dataPath = null;
 					if(example) {
 						dataPath = aje.getExamplePath(dataId);
 					}
 					else {
 						
 						//TODO
 						/*TEMPORAL, PENSAR OTRA FORMA DE CREAR LOS FICHEROS CON LA LISTA DE NODOS*/
 						/*DE MOMENTO SE CREAN EN TMP*/
 						if(dataId.contains("\n")) {
 							FileWriter fileWriter = null;
 					        try {
 					            String content = dataId;
					            dataPath = "/tmp/" + StringUtils.randomString(8) + "/";
 					            fileWriter = new FileWriter(new File(dataPath));
 					            fileWriter.write(content);
 					            fileWriter.close();
 					        } catch (IOException ex) {
 					        	ex.printStackTrace();
 					        } finally {
 					            try {
 					                fileWriter.close();
 					            } catch (IOException ex) {
 					                ex.printStackTrace();
 					            }
 					        }
 						}
 						/**/
 						else { // is a dataId
 							dataPath = cloudSessionManager.getDataPath(accountId, null, dataId);
 						}
 					}
 					if (dataPath.contains("ERROR")) {
 						return createErrorResponse(dataPath);
 					} else {
 						dataPaths.add(dataPath);
 					}
 				}
 				params.put(inputParam.getName(), dataPaths);
 			}
 		}
 
 		// Create commmand line
 		String commandLine = null;
 		try {
 			commandLine = aje.createCommandLine(execution.getExecutable(), params);
 		} catch (AccountManagementException e) {
 			e.printStackTrace();
 			return createErrorResponse(e.getMessage());
 		}
 
 		String jobId = cloudSessionManager.createJob(jobName, jobFolder, bucket, toolName, new ArrayList<String>(),
 				commandLine, sessionId);
 
 		if (jobFolder == null) {
 			jobFolder = cloudSessionManager.getJobFolder(bucket, jobId, sessionId);
 		}
 
 		// String jobId = execute("SW","HPG.SW", dataIds, params, "-d");
 		String resp;
 		try {
 			resp = aje.execute(jobId, jobFolder, params);
 		} catch (AccountManagementException e) {
 			e.printStackTrace();
 			return createErrorResponse("ERROR: Execution failed.");
 		}
 
 		return createOkResponse(resp);
 	}
 }
