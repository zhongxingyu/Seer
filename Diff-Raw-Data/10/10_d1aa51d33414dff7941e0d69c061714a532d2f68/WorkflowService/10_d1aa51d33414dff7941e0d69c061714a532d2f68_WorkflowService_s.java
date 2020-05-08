 package eu.dm2e.ws.services.workflow;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import com.google.gson.Gson;
 import com.hp.hpl.jena.query.ParameterizedSparqlString;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
 
 import eu.dm2e.grafeo.GResource;
 import eu.dm2e.grafeo.GStatement;
 import eu.dm2e.grafeo.Grafeo;
 import eu.dm2e.grafeo.jena.GrafeoImpl;
 import eu.dm2e.grafeo.jena.SparqlUpdate;
 import eu.dm2e.logback.LogbackMarkers;
 import eu.dm2e.ws.Config;
 import eu.dm2e.ws.ConfigProp;
 import eu.dm2e.ws.DM2E_MediaType;
 import eu.dm2e.ws.ErrorMsg;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.api.ParameterPojo;
 import eu.dm2e.ws.api.ValidationReport;
 import eu.dm2e.ws.api.WebservicePojo;
 import eu.dm2e.ws.api.WorkflowPojo;
 import eu.dm2e.ws.api.WorkflowPositionPojo;
 import eu.dm2e.ws.services.AbstractRDFService;
 import eu.dm2e.ws.services.file.FileStatus;
 
 /**
  * Service for the creation and execution of workflows
  */
 @Path("/workflow")
 public class WorkflowService extends AbstractRDFService {
 
 	public static String PARAM_POLL_INTERVAL = "pollInterval";
 	public static String PARAM_JOB_TIMEOUT = "jobTimeout";
 	public static String PARAM_COMPLETE_LOG = "completeLog";
 
 	private class WorkflowPojoComparator implements Comparator<WorkflowPojo> {
 		private String compareProp; // e.g. NS.DCTERMS.PROP_MODIFIED
 		private String order; // "asc" or "desc"
 		public WorkflowPojoComparator(String compareProp, String order) {
 			this.compareProp = compareProp;
 			this.order = order;
 		}
 		@Override
 		public int compare(WorkflowPojo o1, WorkflowPojo o2) {
 			if (this.order.equals("desc")) {
 				WorkflowPojo swap = o1;
 				o1 = o2;
 				o2 = swap;
 			}
 			log.debug("comparing unsing " + this.compareProp);
 			if (compareProp.equals(NS.DCTERMS.PROP_MODIFIED)) {
 				return o1.getModified().compareTo(o2.getModified());
 			} else if (compareProp.equals(NS.DCTERMS.PROP_CREATOR)) {
 				return o1.getCreator().getId().compareTo(o2.getCreator().getId());
 			} else {
 				return 0;
 			}
 		}
 	}
 
     @Override
 	public WebservicePojo getWebServicePojo() {
 		WebservicePojo ws = new WebservicePojo();
 		ws.setLabel("Workflow Storage Service");
 		return ws;
 	}
 
 	/**
 	 * GET /list/facets
 	 * @return
 	 */
 	@GET
 	@Path("list/facets")
 	@Produces({
 		MediaType.APPLICATION_JSON
 	})
 	public Response getWorkflowFacets() {
     	ParameterizedSparqlString sb = new ParameterizedSparqlString();
     	sb.setNsPrefix("rdf", NS.RDF.BASE);
     	sb.setNsPrefix("dcterms", NS.DCTERMS.BASE);
     	sb.setNsPrefix("omnom", NS.OMNOM.BASE);
     	sb.append("SELECT ?creator {  \n");
     	sb.append("  GRAPH ?wf {  \n");
     	sb.append("    ?wf rdf:type omnom:Workflow .  \n");
         sb.append("    OPTIONAL { ?wf dcterms:creator ?creator . }  \n");
     	sb.append("  }    \n");
     	sb.append("}  \n");
 //    	GrafeoImpl g = new GrafeoImpl();
     	Query query = sb.asQuery();
     	QueryEngineHTTP qExec = QueryExecutionFactory.createServiceRequest(Config.get(ConfigProp.ENDPOINT_QUERY), query);
     	long startTime = System.currentTimeMillis();
     	log.debug("About to execute facet SELECT query.");
     	ResultSet resultSet = qExec.execSelect();
     	long estimatedTime = System.currentTimeMillis() - startTime;
     	log.debug(LogbackMarkers.TRACE_TIME, "SELECT workflow facet query took " + estimatedTime + "ms.");
 
         PojoListFacet creatorFacet = new PojoListFacet();
         creatorFacet.setLabel("Creator");
         creatorFacet.setQueryParam("user");
         creatorFacet.setRdfProp(NS.DCTERMS.PROP_CREATOR);
 
         while (resultSet.hasNext()) {
         	QuerySolution sol = resultSet.next();
         	if (null != sol.get("creator"))
         		creatorFacet.getValues().add(sol.get("creator").asNode().toString());
         }
 
         List<PojoListFacet> retList = new ArrayList<>();
         retList.add(creatorFacet);
     	return Response.ok(new Gson().toJson(retList).toString()).build();
 	}
 
 	/**
 	 * GET /list
 	 * @return
 	 */
 	@GET
 	@Path("list")
 	@Produces({
 		MediaType.APPLICATION_JSON
 	})
 	public Response getWorkflowList(
 			@QueryParam("user") String filterUser,
 			@QueryParam("sort") String sortProp,
 			@QueryParam("order") String sortOrder
 			) {
     	GrafeoImpl g = new GrafeoImpl();
     	ParameterizedSparqlString sb = new ParameterizedSparqlString();
     	sb.setNsPrefix("rdf", NS.RDF.BASE);
     	sb.setNsPrefix("dcterms", NS.DCTERMS.BASE);
     	sb.setNsPrefix("omnom", NS.OMNOM.BASE);
 //    	sb.setParam("filterUser", g.resource(filterUser).getJenaRDFNode());
     	sb.append("CONSTRUCT {  \n");
     	sb.append("    ?s ?p ?o .  \n");
     	sb.append("} WHERE {  \n");
     	sb.append("  GRAPH ?wf {  \n");
     	sb.append("    ?wf rdf:type omnom:Workflow .  \n");
     	sb.append("	   ?wf omnom:status ?status .  \n");											// Filter unavailable workflows
     	sb.append("    FILTER(STR(?status) != \"" + FileStatus.DELETED.toString() + "\") .  \n"); // i.e. those DELETED
     	if (null != filterUser) {
     	sb.append("    ?conf dcterms:creator <" + filterUser + ">  .  \n");
     	}
     	sb.append("    ?s ?p ?o .  \n");
     	sb.append("  }    \n");
     	sb.append("}  \n");
     	Query query = sb.asQuery();
     	QueryEngineHTTP qExec = QueryExecutionFactory.createServiceRequest(Config.get(ConfigProp.ENDPOINT_QUERY), query);
     	{
     		long startTime = System.currentTimeMillis();
     		qExec.execConstruct(g.getModel());
     		long endTime  = System.currentTimeMillis();
     		long elapsed = endTime - startTime;
     		log.debug(LogbackMarkers.TRACE_TIME, "CONSTRUCT of workflows took " + elapsed + "ms. ");
     	}
     	List<WorkflowPojo> wfList = new ArrayList<>();
     	{
     		long startTime = System.currentTimeMillis();
     		for (GStatement typeStmt : g.listStatements(null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WORKFLOW))) {
     			WorkflowPojo pojo = g.getObjectMapper().getObject(WorkflowPojo.class, typeStmt.getSubject());
     			wfList.add(pojo);
     		}
     		long endTime  = System.currentTimeMillis();
     		long elapsed = endTime - startTime;
     		log.debug(LogbackMarkers.TRACE_TIME, "POJOization of workflowconfigs took " + elapsed + "ms. ");
     	}
     	{
     		long startTime = System.currentTimeMillis();
     		if (null == sortProp)
     			sortProp = NS.DCTERMS.PROP_MODIFIED;
     		if (null == sortOrder)
     			sortOrder = "asc";
     		Collections.sort(wfList, new WorkflowPojoComparator(sortProp, sortOrder));
     		long endTime  = System.currentTimeMillis();
     		long elapsed = endTime - startTime;
     		log.debug(LogbackMarkers.TRACE_TIME, "SORTING of workflowconfigs took " + elapsed + "ms. ");
     	}
 		return Response.ok(wfList).build();
 	}
 
 	/**
 	 * GET /{id}/validate
 	 * @return
 	 */
 	@GET
 	@Produces({
 		DM2E_MediaType.TEXT_PLAIN,
 	})
 	@Path("{id}/validate")
 	public Response validateWorkflow() {
 		URI wfUri = popPath(getRequestUriWithoutQuery());
 		GrafeoImpl g = new GrafeoImpl();
 		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri);
 		if (g.isEmpty()) {
 			return Response.status(404).build();
 		}
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		ValidationReport report = wf.validate();
 		if (report.valid()) {
 			return Response.status(Response.Status.OK).entity(report.toString()).build();
 		} else {
 			return Response.status(Response.Status.PRECONDITION_FAILED).entity(report.toString()).build();
 		}
 	}
 
 	/**
 	 * GET /{id}
 	 * @return
 	 */
 	@GET
 	@Produces({
 		MediaType.WILDCARD,
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		MediaType.APPLICATION_JSON
 	})
 	@Path("{id}")
 	public Response getWorkflow() {
 		URI wfUri = getRequestUriWithoutQuery();
 		GrafeoImpl g = new GrafeoImpl();
 		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri);
 		if (g.isEmpty()) {
 			return Response.status(Response.Status.NOT_FOUND).build();
 		}
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		Status respStatus = Response.Status.OK;
 		if (wf.getWorkflowStatus().equals(FileStatus.DELETED.toString())) {
 			respStatus = Response.Status.GONE;
 		}
 		return Response.status(respStatus).entity(wf).build();
 	}
 
 	/*
 	 * DELETE /{id}
 	 * @return
 	 */
 	@DELETE
 	@Path("{id}")
 	public Response deleteWorkflow() {
 		URI wfUri = getRequestUriWithoutQuery();
 		GrafeoImpl g = new GrafeoImpl();
 		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri);
 		if (g.isEmpty()) {
 			return Response.status(Response.Status.NOT_FOUND).build();
 		}
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		wf.setWorkflowStatus(FileStatus.DELETED.toString());
 		wf.getGrafeo().putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUri);
 		return Response.status(Response.Status.OK).entity(wf).build();
 	}
 	
 	/*
 	 * POST /{id}/autowire
 	 * @return
 	 */
 	@POST
 	@Path("{id}/autowire")
 	public Response autowireWorkflow() {
 		URI wfUri = popPath();
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri);
		if (g.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
 		}
		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		wf.autowire();
 		wf.getGrafeo().putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUri);
 		return Response.status(Response.Status.OK).entity(wf).build();
 	}
 	/**
 	 * GET /{id}/png		[AC: * / *]
 	 * 
 	 * Get DOT visualization of workflow
 	 * 
 	 * @return
 	 * @throws IOException 
 	 */
 	@GET
 	@Produces({"image/png"})
 	@Path("{id}/png")
 	public Response getWorkflowPngByUrl() throws IOException {
 		URI wfUri = popPath(getRequestUriWithoutQuery());
 		GrafeoImpl g = new GrafeoImpl();
 		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri.toString());
 		if (g.isEmpty()) {
 			return Response.status(404).build();
 		}
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		for (WorkflowPositionPojo pos : wf.getPositions()) {
 			pos.getWebservice().refresh(1, true);
 		}
 		String cmd = "dot -Tpng";
 		Process p = Runtime.getRuntime().exec(cmd);
 		InputStream is = p.getInputStream();
 		OutputStream os = p.getOutputStream();
 		OutputStreamWriter osWriter = new OutputStreamWriter(os);
 		BufferedWriter out = new BufferedWriter(osWriter);
 		out.write(wf.getFullDot());
 		out.write("\n");
 		out.flush();
 		out.close();
 		return Response.ok(is).build();
 	}
 	/**
 	 * GET /{id}		[CT: image/png]
 	 * 
 	 * Get DOT visualization of workflow
 	 * 
 	 * @return
 	 * @throws IOException 
 	 */
 	@GET
 	@Produces({"image/png"})
 	@Path("{id}")
 	public Response getWorkflowPNG() throws IOException {
 		return Response.seeOther(appendPath(getRequestUriWithoutQuery(), "png")).build();
 	}
 
 	/**
 	 * GET /{id}		[CT: text/vnd.graphviz]
 	 * 
 	 * Get DOT visualization of workflow
 	 * 
 	 * @return
 	 */
 	@GET
 	@Produces({"text/vnd.graphviz"})
 	@Path("{id}")
 	public Response getWorkflowDOT() {
 		URI wfUri = getRequestUriWithoutQuery();
 		GrafeoImpl g = new GrafeoImpl();
 		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri.toString());
 		if (g.isEmpty()) {
 			return Response.status(404).build();
 		}
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 		for (WorkflowPositionPojo pos : wf.getPositions()) {
 			pos.getWebservice().refresh(1, true);
 		}
 		return Response.ok(wf.getFullDot()).build();
 	}
 
 	/**
 	 * PUT {workflowID} 	Accept: json
 	 * @param wf
 	 * @return
 	 */
 	@PUT
 	@Path("{workflowID}")
 	@Consumes({
 		MediaType.APPLICATION_JSON
 	})
 	public Response putWorkflowAsJSON(WorkflowPojo wf) {
 		return putGrafeo(wf.getGrafeo());
 	}
 
 	/**
 	 * PUT {workflowID}		Accept: rdf
 	 * TODO refactor
 	 * @param rdfString
 	 * @return
 	 */
 	@PUT
 	@Path("{workflowID}")
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response putRDF(String rdfString) {
 		Grafeo g = null;
 		try {
 			g = new GrafeoImpl();
 			g.readHeuristically(rdfString);
 			assert(g != null);
 		}
 		catch (Exception e) {
 			throwServiceError(e);
 		}
 		return this.putGrafeo(g);
 	}
 
 	public Response putGrafeo(Grafeo g) {
 		final URI wfUri = getRequestUriWithoutQuery();
 		final String wfUriStr = getRequestUriWithoutQuery().toString();
 
         // Link to the default webservice (WorkflowExecutionService)
         g.addTriple(g.resource(wfUri), NS.OMNOM.PROP_EXEC_WEBSERVICE, g.resource(pushPathFromBeginning(uriInfo, "exec")));
 
 		// TODO FIXME What if the user changed the default parameters defined in post?
 
 		log.info("Skolemizing parameters.");
 		{
 			g.skolemizeByLabel(wfUriStr, NS.OMNOM.PROP_INPUT_PARAM, "param");
 			g.skolemizeByLabel(wfUriStr, NS.OMNOM.PROP_OUTPUT_PARAM, "param");
 		}
 
 		log.info("Skolemizing Positions.");
 		{
 			g.skolemizeSequential(wfUriStr, NS.OMNOM.PROP_WORKFLOW_POSITION, "position");
 		}
 
 		log.info("Skolemizing Connectors.");
 		{
 			g.skolemizeSequential(wfUriStr, NS.OMNOM.PROP_PARAMETER_CONNECTOR, "connector");
 		}
 //		log.info("Skolemnizing WebserviceConfigs");
 //		{
 //			for (GResource subj : g.findByClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)) {
 //				g.skolemizeUUID(subj.toString(), NS.OMNOM.PROP_WEBSERVICE_CONFIG, "webserviceconfig");
 //			}
 //		}
 
 		log.info("Writing updated workflow to endpoint.");
 		try {
 			g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUriStr);
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 		log.info("DONE Writing updating workflow to endpoint: " + wfUriStr);
 
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUriStr);
 		ValidationReport rep = null;
 		try {
 			rep = wf.validate();
 		} catch (Exception e) {
 			return throwServiceError(rep.toString());
 		}
 		return Response.ok(wf).location(wfUri).build();
 	}
 
 	/**
 	 * POST /				Accept: json
 	 * @param wf
 	 * @return
 	 */
 	@POST
 	@Consumes({
 		MediaType.APPLICATION_JSON
 	})
 	public Response postWorkflowAsJSON(WorkflowPojo wf) {
 //		WorkflowPojo wf = OmnomJsonSerializer.deserializeFromJSON(s, WorkflowPojo.class);
 		log.error(wf.getTerseTurtle());
 		return postGrafeo(wf.getGrafeo());
 	}
 
     /**
      * Convenience method that accepts a configuration, publishes it
      * directly to the ConfigurationService and then calls the TransformationService
      * with the persistent URI.
      *
      * <del>Only to be used for development, not for production!</del>
      * This is necessary for workflows. [kb, Jul 15, 2013 12:03:08 AM]
      *
      * TODO test whether we can replace this with the MessageBodyWriter<Grafeo>
      *
      * @param rdfString
      * @return
      */
     @POST
     @Consumes({
             DM2E_MediaType.APPLICATION_RDF_TRIPLES,
             DM2E_MediaType.APPLICATION_RDF_XML,
             DM2E_MediaType.APPLICATION_X_TURTLE,
             DM2E_MediaType.TEXT_RDF_N3,
             DM2E_MediaType.TEXT_TURTLE
             // , MediaType.TEXT_HTML
             // , DM2E_MediaType.TEXT_PLAIN,
             // , MediaType.APPLICATION_JSON
     })
     public Response postRDF(String rdfString) {
         Grafeo g = null;
         try {
             g = new GrafeoImpl();
             g.readHeuristically(rdfString);
             assert(g != null);
         }
         catch (Exception e) {
             throwServiceError(e);
         }
         return this.postGrafeo(g);
     }
 
 	public Response postGrafeo(Grafeo g) {
 		GResource wfRes = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW);
 		log.trace("Workflow before Posting: " + g.getTerseTurtle());
 		log.info("Number of positions: " + g.listStatements(null, "omnom:hasPosition", null).size());
 
 		if (null == wfRes) {
 			return throwServiceError(NS.OMNOM.CLASS_WORKFLOW, ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		String uid =   createUniqueStr();
         String wfUri = getRequestUriWithoutQuery() + "/" + uid;
 		wfRes.rename(wfUri);
 
         // Link to the default webservice (WorkflowExecutionService)
 
         g.addTriple(wfRes, NS.OMNOM.PROP_EXEC_WEBSERVICE, g.resource(pushPathFromBeginning(uriInfo,"exec") + "/" + uid));
 
 		/*
 		 * Add global workflow parameters
 		 */
 		log.info("Adding global workflow parameters");
 		{
 			{
 				ParameterPojo pollTimeParam = new ParameterPojo();
 				pollTimeParam.setLabel(PARAM_POLL_INTERVAL);
 				pollTimeParam.setComment("Time to wait between polls for job status, in milliseconds. [Default: 2000ms]");
 				pollTimeParam.setDefaultValue("" + 2 * 1000);
 				GResource pollTimeParamRes = g.getObjectMapper().addObject(pollTimeParam);
 				g.addTriple(wfRes, NS.OMNOM.PROP_INPUT_PARAM, pollTimeParamRes);
 			}
 			{
 				ParameterPojo maxJobWaitParam = new ParameterPojo();
 				maxJobWaitParam.setLabel(PARAM_JOB_TIMEOUT);
 				maxJobWaitParam.setComment("Maximum time to wait for a job to finish, in seconds. [Default: 120s]");
 				maxJobWaitParam.setDefaultValue("" + 120);
 				GResource maxJobWaitParamRes = g.getObjectMapper().addObject(maxJobWaitParam);
 				g.addTriple(wfRes, NS.OMNOM.PROP_INPUT_PARAM, maxJobWaitParamRes);
 			}
 			{
 				ParameterPojo completeLogParam = new ParameterPojo();
 				completeLogParam.setLabel(PARAM_COMPLETE_LOG);
 				completeLogParam.setComment("The complete log file of the workflow job and its webservice jobs.");
 				GResource completeLogParamRes = g.getObjectMapper().addObject(completeLogParam);
 				g.addTriple(wfRes, NS.OMNOM.PROP_OUTPUT_PARAM, completeLogParamRes);
 			}
 		}
 
 		log.info("Skolemnizing parameters.");
 		{
 			g.skolemizeByLabel(wfUri, NS.OMNOM.PROP_INPUT_PARAM, "param");
 			g.skolemizeByLabel(wfUri, NS.OMNOM.PROP_OUTPUT_PARAM, "param");
 		}
 
 		log.info("Skolemizing Positions.");
 		{
 			g.skolemizeSequential(wfUri, NS.OMNOM.PROP_WORKFLOW_POSITION, "position");
 		}
 
 		log.info("Skolemnizing Connectors.");
 		{
 			g.skolemizeSequential(wfUri, NS.OMNOM.PROP_PARAMETER_CONNECTOR, "connector");
 		}
 //		log.info("Skolemnizing WebserviceConfigs");
 //		{
 //			for (GResource subj : g.findByClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)) {
 //				g.skolemizeUUID(subj.toString(), NS.OMNOM.PROP_WEBSERVICE_CONFIG, "webserviceconfig");
 //			}
 //		}
 
 		log.info("Writing workflow to triple store.");
 		try {
 			g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUri);
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 		log.info("Done Writing workflow to triple store: " + wfUri);
 
 		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
 
 		/*
 		 *  kb Tue Nov 26 20:00:39 CET 2013
 		 *  We must not validate here because the workflow POSTed can be invalid and that's okay, the user will make it valid.
 		 *  Only PUTting an invalid workflow is a no-no.
 		 */
 
 //		try {
 //			wf.validate();
 //		} catch (Exception e) {
 //			return throwServiceError(e);
 //		}
 
 		return Response.ok().entity(wf).location(URI.create(wfUri)).build();
 	}
 	@POST
 	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES, DM2E_MediaType.APPLICATION_RDF_XML,
 			DM2E_MediaType.APPLICATION_X_TURTLE, DM2E_MediaType.TEXT_PLAIN,
 			DM2E_MediaType.TEXT_RDF_N3, DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/position")
 	public Response postWorkflowPosition(@PathParam("workflowId") String workflowId, String rdfString) {
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_POSITION);
 		if (null == blank) {
 			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		URI newUri = appendPath(createUniqueStr());
 		URI workflowUri = popPath("position");
 		blank.rename(newUri);
 		// TODO rename blank sub-resources
 		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
 		return Response.created(newUri).build();
 	}
 	@PUT
 	@Path("{workflowId}/position/{posId}")
 	public Response putWorkflowPosition(@PathParam("workflowId") String workflowId, @PathParam("posId") String posId, String rdfString) {
 		URI posUri = getRequestUriWithoutQuery();
 		URI workflowUri = popPath(popPath());
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 			.delete(posUri + "?s ?o")
 			.insert(g)
 			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 			.graph(workflowUri)
 			.build();
 		sparul.execute();
 		return getResponse(g);
 	}
 	@GET
 	@Path("{workflowId}/position/{posId}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response getWorkflowPosition(@PathParam("workflowId") String workflowId, @PathParam("posId") String posId) {
 		URI workflowUri = popPath(popPath());
 		return Response.seeOther(workflowUri).build();
 	}
 	@POST
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/connector")
 	public Response postWorkflowConnector(@PathParam("workflowId") String workflowId, String rdfString) { Grafeo g = new GrafeoImpl(rdfString, null);
 		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER_CONNECTOR);
 		if (null == blank) {
 			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		URI newUri = appendPath(createUniqueStr());
 		URI workflowUri = popPath("connector");
 		blank.rename(newUri);
 		// TODO rename blank sub-resources
 		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
 		return Response.created(newUri).build();
 	}
 	@PUT
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/connector/{conId}")
 	public Response putWorkflowConnector(@PathParam("workflowId") String workflowId, @PathParam("conId") String conId, String rdfString) {
 		URI conUri = getRequestUriWithoutQuery();
 		URI workflowUri = popPath(popPath());
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 			.delete(conUri + "?s ?o")
 			.insert(g.toString())
 			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 			.graph(workflowUri).build();
 		sparul.execute();
 		return getResponse(g);
 	}
 	@GET
 	@Path("{workflowId}/connector/{conId}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response getWorkflowConnector( @PathParam("workflowId") String workflowId, @PathParam("conId") String conId) {
 		URI workflowUri = popPath(popPath());
 		return Response.seeOther(workflowUri).build();
 	}
 	@POST
 	@Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE })
 	@Path("{workflowId}/param")
 	public Response postWorkflowParameter(@PathParam("workflowId") String workflowId, String rdfString) {
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER);
 		if (null == blank) {
 			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
 		}
 		URI newUri = appendPath(createUniqueStr());
 		URI workflowUri = popPath();
 		blank.rename(newUri);
 		// TODO rename blank sub-resources
 		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
 		return Response.created(newUri).build();
 	}
 	@PUT
 	@Path("{workflowId}/param/{paramId}")
 	public Response putWorkflowParamaeter(@PathParam("workflowId") String workflowId, @PathParam("paramId") String paramId, String rdfString) {
 		URI conUri = getRequestUriWithoutQuery();
 		URI workflowUri = popPath(popPath());
 		Grafeo g = new GrafeoImpl(rdfString, null);
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 			.delete(conUri + "?s ?o")
 			.insert(g.toString())
 			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 			.graph(workflowUri).build();
 		sparul.execute();
 		return getResponse(g);
 	}
 	@GET
 	@Path("{workflowId}/param/{paramId}")
 	@Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_PLAIN,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 	})
 	public Response getWorkflowParameter( @PathParam("workflowId") String workflowId, @PathParam("paramId") String paramId) {
 		URI workflowUri = popPath(popPath());
 		return Response.seeOther(workflowUri).build();
 	}
 
 
 }
