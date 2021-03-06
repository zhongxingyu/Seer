 package com.datascience.service;
 
 import com.datascience.datastoring.jobs.JobsManager;
 import com.sun.jersey.api.view.Viewable;
 import com.sun.jersey.spi.resource.Singleton;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.*;
 
 @Path("/config/")
 @Singleton
 public class ConfigEntry {
 
 	@Context ServletContext scontext;
 	protected static final Logger logger = Logger.getLogger(ConfigEntry.class);
 
 	public static class NameValue{
 		public String name;
 		public Object value;
 
 		public NameValue(String n, Object v){
 			this.name = n;
 			this.value = v;
 		}
 
 		public String getName(){
 			return name;
 		}
 
 		public Object getValue(){
 			return value;
 		}
 	}
 
 	@GET
 	@Produces("text/html")
 	public Response getConfig() {
 		Map<String, Object> model = new HashMap<String, Object>();
 		Properties properties = (Properties)scontext.getAttribute(Constants.PROPERTIES);
 		Boolean freezed = (Boolean) scontext.getAttribute(Constants.IS_FREEZED);
 		List<NameValue> items = new ArrayList<NameValue>();
 		for (String s : new ArrayList<String>(new TreeSet<String>(properties.stringPropertyNames()))){
 			if (s.equals(Constants.JOBS_STORAGE))
 				continue;
 			if (freezed && (s.startsWith("DB") || s.endsWith("PATH")))
 				continue;
 			items.add(new NameValue(s, properties.get(s)));
 		}
 		model.put(Constants.IS_FREEZED, freezed);
 		model.put("items", items);
 		model.put(Constants.IS_INITIALIZED, scontext.getAttribute(Constants.IS_INITIALIZED));
 		model.put("storages", new String[] {"MEMORY_FULL", "MEMORY_KV", "MEMORY_KV_JSON", "MEMORY_KV_SIMPLE", "DB_FULL", "DB_KV_JSON", "DB_KV_SIMPLE"});
 		model.put(Constants.JOBS_STORAGE, ((Properties) scontext.getAttribute(Constants.PROPERTIES)).getProperty(Constants.JOBS_STORAGE));
 		return Response.ok(new Viewable("/config", model)).build();
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public Response setConfig(MultivaluedMap<String, String> form){
 		if (!(Boolean)scontext.getAttribute(Constants.IS_FREEZED)){
 			Map<String, String> simpleForm = new HashMap<String, String>();
 			for (String s : form.keySet()){
 				String ts = Constants.t(s);
 				if (ts.equals(Constants.IS_FREEZED))
 					scontext.setAttribute(Constants.IS_FREEZED, true);
 				else
 					simpleForm.put(ts, form.getFirst(s));
 			}
 			InitializationSupport.destroyContext(scontext);
 			try{
 				initializeContext(simpleForm);
 			} catch(Exception e){
 				logger.error(e.getMessage(), e);
 				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
 			}
 		}
 		return Response.ok().build();
 	}
 
 	@POST
 	@Path("resetDB")
 	public Response resetDB(){
 		if (!(Boolean)scontext.getAttribute(Constants.IS_FREEZED)){
 			JobsManager jm = (JobsManager) scontext.getAttribute(Constants.JOBS_MANAGER);
 			try {
 				jm.rebuild();
				InitializationSupport.initializeContext(scontext);
 			} catch (Exception e){
 				logger.error(e.getMessage(), e);
 				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
 			}
 		}
 		return Response.ok().build();
 	}
 
 	private void initializeContext(Map<String, String> properties) throws SQLException, IOException, ClassNotFoundException {
 		//update properties
 		Properties props = (Properties) scontext.getAttribute(Constants.PROPERTIES);
 		props.putAll(properties);
 		//and initialize context
 		InitializationSupport.initializeContext(scontext);
 	}
 }
