 /**
  * 
  */
 package edu.illinois.ncsa.versus.rest;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.resteasy.annotations.Form;
 
 import com.google.inject.Injector;
 
 import edu.illinois.ncsa.versus.engine.impl.ComparisonStatusHandler;
 import edu.illinois.ncsa.versus.engine.impl.ExecutionEngine;
 import edu.illinois.ncsa.versus.engine.impl.Job;
 import edu.illinois.ncsa.versus.engine.impl.Job.ComparisonStatus;
 import edu.illinois.ncsa.versus.engine.impl.PairwiseComparison;
 import edu.illinois.ncsa.versus.registry.CompareRegistry;
 import edu.illinois.ncsa.versus.restlet.Comparison;
 import edu.illinois.ncsa.versus.store.ComparisonServiceImpl;
 
 /**
  * Submit and query comparisons.
  * 
  * @author Luigi Marini
  * 
  */
 @Path("/comparisons")
 public class ComparisonResource {
 
 	private static Log log = LogFactory.getLog(ComparisonResource.class);
 
 	@GET
 	@Produces("application/json")
 	public List<String> list(@Context ServletContext context) {
 
 		Injector injector = (Injector) context.getAttribute(Injector.class
 				.getName());
 		ComparisonServiceImpl comparisonService = injector
 				.getInstance(ComparisonServiceImpl.class);
 		log.debug("I am inside GET list ");
 		System.out.println("I am inside GET list ");
 		List<String> json = new ArrayList<String>();
 		for (Comparison comparison : comparisonService.listAll()) {
 			json.add(comparison.getId());
 		}
 		return json;
 	}
 
 	@GET
 	@Path("/{id}")
 	@Produces("application/json")
 	public Response getComparison(@PathParam("id") String id,
 			@Context ServletContext context) {
 
 		Injector injector = (Injector) context.getAttribute(Injector.class
 				.getName());
 
 		ComparisonServiceImpl comparisonService = injector
 				.getInstance(ComparisonServiceImpl.class);
 		log.debug("I am inside GET getComparison ");
 		System.out.println("I am inside GET getComparison ");
 		Comparison c = comparisonService.getComparison(id);
 		if (c == null) {
 			log.debug("Comparison not found " + id);
 			return Response.status(404)
 					.entity("Comparison " + id + " not found.").build();
 		} else {
 			log.debug("Comparison found " + id);
 			return Response.status(200).entity(c).build();
 		}
 	}
 
 	@GET
 	@Path("/{id}/status")
 	@Produces("text/plain")
 	public Response getStatus(@PathParam("id") String id,
 			@Context ServletContext context) {
 
 		Injector injector = (Injector) context.getAttribute(Injector.class
 				.getName());
 
 		ComparisonServiceImpl comparisonService = injector
 				.getInstance(ComparisonServiceImpl.class);
 		log.debug("I am inside GET getStatus ");
 		System.out.println("I am inside GET getStatus ");
 		Comparison c = comparisonService.getComparison(id);
 		if (c == null) {
 			log.debug("Comparison not found " + id);
 			return Response.status(404)
 					.entity("Comparison " + id + " not found.").build();
 		} else {
 			log.debug("Comparison found " + id);
 			return Response.status(200).entity(c.getStatus()).build();
 		}
 	}
 
 	@GET
 	@Path("/{id}/value")
 	@Produces("application/json")
 	//@Produces("text/plain")
 	//public Response getValue(@PathParam("id") String id,
 		//	@Context ServletContext context) {
 	public Map<String, Object> getJson(@PathParam("id") String id,
 			@Context ServletContext context) {
 		Injector injector = (Injector) context.getAttribute(Injector.class
 				.getName());
 
 		ComparisonServiceImpl comparisonService = injector
 				.getInstance(ComparisonServiceImpl.class);
 		log.debug("I am inside GET getValue ");
 		System.out.println("I am inside GET getValue ");
 		Comparison c = comparisonService.getComparison(id);
 		/*if (c == null) {
 			log.debug("Comparison not found " + id);
 			return Response.status(404)
 					.entity("Comparison " + id + " not found.").build();
 		} else {
 			log.debug("Comparison found " + id);
 			return Response.status(200).entity(c.getValue()).build();
 		}*/
 		//log.debug("getjson");
 		Map<String, Object> json = new HashMap<String, Object>();
		log.debug("Comparison status: "+c.getStatus());
		
		if( c.getStatus() != ComparisonStatus.DONE ){
			 // while(c.getStatus()!=ComparisonStatus.DONE){	
			    try {
					//Thread.sleep(5);
			    	synchronized(c){
			    	c.wait();
			    	}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		//while(c.getStatus()!=ComparisonStatus.DONE){
		//}	
 		
 			log.debug("getjson");
 			log.debug("c.getValue"+c.getValue());
 			log.debug("status"+c.getStatus());
 		json.put("value",c.getValue());
 		json.put("status", c.getStatus());
 		json.put("other", c.getExtractorId());
 		//}
 		return json;
 	}
 
 	/**
 	 * Submit new comparison.
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	@POST
 	@Produces(MediaType.TEXT_HTML)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String submit(@Form ComparisonForm comparisonForm,
 			@Context ServletContext context) {
 
 		CompareRegistry registry = (CompareRegistry) context
 				.getAttribute(CompareRegistry.class.getName());
 		ExecutionEngine engine = (ExecutionEngine) context
 				.getAttribute(ExecutionEngine.class.getName());
 		Injector injector = (Injector) context.getAttribute(Injector.class
 				.getName());
 		ComparisonServiceImpl comparisonService = injector
 				.getInstance(ComparisonServiceImpl.class);
 
 		if (checkRequirements(registry, comparisonForm.adapter,
 				comparisonForm.extractor, comparisonForm.measure)) {
 			try {
 
 				final PairwiseComparison comparison = new PairwiseComparison();
 				comparison.setId(UUID.randomUUID().toString());
 				comparison.setFirstDataset(getFile(comparisonForm.dataset1));
 				comparison.setSecondDataset(getFile(comparisonForm.dataset2));
 				comparison.setAdapterId(comparisonForm.adapter);
 				comparison.setExtractorId(comparisonForm.extractor);
 				comparison.setMeasureId(comparisonForm.measure);
 
 				return createLocalJob(comparison, comparisonForm.dataset1,
 						comparisonForm.dataset2, comparisonService, engine);
 			} catch (IOException e) {
 				log.error("Internal error writing to disk", e);
 				return "Internal error writing to disk";
 			}
 		} else {
 			return querySlaves(comparisonForm, comparisonForm);
 		}
 	}
 
 	/**
 	 * 
 	 * @param form
 	 * @param registry
 	 * @param adapterId
 	 * @param extractorId
 	 * @param measureId
 	 * @return
 	 */
 	public boolean checkRequirements(CompareRegistry registry,
 			String adapterId, String extractorId, String measureId) {
 		boolean adapter = registry.getAvailableAdaptersIds()
 				.contains(adapterId);
 		boolean extractor = registry.getAvailableExtractorsIds().contains(
 				extractorId);
 		boolean measure = registry.getAvailableMeasuresIds()
 				.contains(measureId);
 		if (adapter && extractor && measure) {
 			log.debug("Local requirements fullfilled");
 			return true;
 		} else {
 			log.debug("Local requirements not fullfilled");
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param comparisoForm
 	 * @param comparisons
 	 * @param engine
 	 * @return
 	 * @throws IOException
 	 */
 	public String createLocalJob(final PairwiseComparison comparison,
 			String dataset1, String dataset2,
 			final ComparisonServiceImpl comparisons, ExecutionEngine engine)
 			throws IOException {
 
 		// store
 		comparisons.addComparison(comparison, dataset1, dataset2);
 
 		engine.submit(comparison, new ComparisonStatusHandler() {
 
 			@Override
 			public void onDone(double value) {
 				log.debug("Comparison " + comparison.getId()
 						+ " done. Result is: " + value);
 				comparisons.setStatus(comparison.getId(), ComparisonStatus.DONE);
 				//if(comparison.)
 				//notify();
 				//comparison.notify();
 				comparisons.updateValue(comparison.getId(), value);
 			}
 
 			@Override
 			public void onStarted() {
 				log.debug("Comparison " + comparison.getId() + " started.");
 				comparisons.setStatus(comparison.getId(),
 						Job.ComparisonStatus.STARTED);
 			}
 
 			@Override
 			public void onFailed(String msg, Throwable e) {
 				log.debug("Comparison " + comparison.getId() + " failed. "
 						+ msg + " " + e);
 				comparisons.setStatus(comparison.getId(),
 						Job.ComparisonStatus.FAILED);
 
 			}
 
 			@Override
 			public void onAborted(String msg) {
 				log.debug("Comparison " + comparison.getId() + " aborted. "
 						+ msg);
 				comparisons.setStatus(comparison.getId(),
 						Job.ComparisonStatus.ABORTED);
 			}
 		});
 
 		return comparison.getId();
 	}
 
 	/**
 	 * 
 	 * @param comparison
 	 * @param comparison2
 	 * @return
 	 */
 	private String querySlaves(ComparisonForm comparison,
 			ComparisonForm comparison2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Make temp copy of remote file.
 	 * 
 	 * @param remoteURL
 	 * @return
 	 * @throws IOException
 	 */
 	public File getFile(String remoteURL) throws IOException {
 		log.debug("I am inside getFile in Comparison resource");
 		
 	    URL url = new URL(remoteURL); //commented by smruti
 		byte[] buff = new byte[10240];
 		int len;
 		File file;
 		//log.debug("I am inside getFile in Comparison resource");
 		//------------------------------------------------------------ The original code-----------------------
 		if (url.getPath().isEmpty()
 			|| url.getPath().matches(".*/versus[\\d]+.tmp")) {
 				file = File.createTempFile("versus", ".tmp");
 		} else {
 			String filename = new File(url.getPath()).getName().replaceAll(
 					"[\\d]+\\.", ".");
 			int idx = filename.lastIndexOf(".");
 			if (idx > 3) {
 				file = File.createTempFile(filename.substring(0, idx),
 						filename.substring(idx));
 			} else if (idx != -1) {
 				file = File.createTempFile("versus", filename.substring(idx));
 			} else {
 				file = File.createTempFile(filename, ".tmp");
 			}
 		}
 		//-------------------------------------------------------------------------
 		
 		file.deleteOnExit(); // TODO only gets called when jvm exits
 		FileOutputStream fos = new FileOutputStream(file);
 		 InputStream is = url.openStream();
 		while ((len = is.read(buff)) != -1) {
 			fos.write(buff, 0, len);
 		}
 		is.close();
 		fos.close();
 		log.debug("");
 		return file;
 	}
 
 	public static class ComparisonForm {
 
 		@FormParam("dataset1")
 		private String dataset1;
 
 		@FormParam("dataset2")
 		private String dataset2;
 
 		@FormParam("adapter")
 		private String adapter;
 
 		@FormParam("extractor")
 		private String extractor;
 
 		@FormParam("measure")
 		private String measure;
 
 		@Override
 		public String toString() {
 			return "dataset1=" + dataset1 + "&dataset2=" + dataset2
 					+ "&adapter=" + adapter + "&extractor=" + extractor
 					+ "&measure=" + measure;
 		}
 
 	}
 
 }
