 package com.zarcode.data.resources;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import com.zarcode.data.dao.ReportDao;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.model.*;
 import com.zarcode.data.exception.*;
 
 @Path("/reports")
 public class Report extends ResourceBase {
 	
 	private Logger logger = Logger.getLogger(Report.class.getName());
 	
 	@Context 
 	UriInfo uriInfo = null;
     
 	@Context 
     Request request = null;
 	
 	String container = null;
 	
 	private static final int MAXPAGE = 10;
 	
 	
 	/*
 	@GET 
 	@Produces("text/plain")
 	@Path("/init")
 	public int init() {
 		int count = 0;
 		String str = null;
 		RecipeDO recipe1 = null;
 		RecipeDO recipe2 = null;
 		RecipeDO recipe3 = null;
 		GAEDao dao = null;
 		
 		try {
 			dao = new GAEDao();
 		
 			recipe1 = new RecipeDO();
 			recipe1.setAuthor("Billy Bob");
 			recipe1.setRecipeName("Donut Pizza II");
 			recipe1.setDesc("dfsfsdfsdf ssfasa asfsafasf");
 			recipe1.setDirections("Mix in bowl");
 			recipe1.setIngredients("Donuts, Cheese and Pepperoni");
 			dao.insertRecipe(recipe1);
 			count++;
 			
 			recipe2 = new RecipeDO();
 			Long id = new Long(9999);
 			recipe2.setRecipeId(id);
 			recipe2.setAuthor("Joe Kirven");
 			recipe2.setRecipeName("Mouse Pudding");
 			recipe2.setDirections("Mix in bowl");
 			recipe2.setDesc("dfsfsdfsdf ssfasa asfsafasf");
 			recipe2.setIngredients("Fresh rat, fresh pasta");
 			dao.insertRecipe(recipe2);
 			count++;
 		}
 		catch (Exception e) {
 			logger.severe("init(): [EXCEPTION]\n" + getStackTrace(e));
 		}
 		finally {
 			if (dao != null) {
 				dao.close();
 			}
 		}
 		return count;
 	}
 	*/
 	
 	@GET 
 	@Path("/latest/{resname}")
 	@Produces("application/json")
 	public ReportDO getLatestReport(@PathParam("resname") String resName) {
 		int i = 0;
 		List<ReportDO> list = null;
 		String xmlStr = null;
 		Long id = null;
 		ReportDao reportDao = null;
 		WaterResourceDao waterDao = null;
 		WaterResourceDO res = null;
 		ReportDO latestReport = null;
 		
 		logger.info("getLatestReport(): Entered -- resName: " + resName);
 		
 		try {
 			waterDao = new WaterResourceDao();
 			res = waterDao.getResourceByName(resName);
 		}
 		catch (Exception e) {
 		}
 		
 		if (res == null) {
 			logger.warning("getLatestReport(): Unable to find water resource.");
 		}
 		
 		try {
 			if (res != null) {
 				// list = reportDao.getReportsById(res.getResourceId());
 				if (list != null && list.size() > 0) {
 					latestReport = list.get(0);
 				}
 			}
 		}
 		catch (Exception e) {
 			logger.severe("getLatestReport: " + getStackTrace(e));
 		}
 		return latestReport;
 	}
 	
 	@GET 
 	@Path("/byradius/{radius}")
 	@Produces("application/json")
 	public List<ReportDO> getReportsByRadius(@PathParam("radius") int radius, @QueryParam("lat") double lat, @QueryParam("lng") double lng) {
 		int i = 0;
 		List<ReportDO> list = null;
 		ReportDao reportDao = null;
 		WaterResourceDO res = null;
 		
 		logger.info("getReportsByRadius(): Entered -- radius=" + radius);
 		
 		try {
 			if (res != null) {
 				// list = reportDao.getReportsByRadius(lat, lng, radius);
 			}
 		}
 		catch (Exception e) {
 			logger.severe("getReportsByRadius: " + getStackTrace(e));
 		}
 		return list;
 	}
 	
 	@GET 
 	@Path("/reportId/{reportId}")
 	@Produces("application/json")
 	public ReportDO getReportByReportId(@PathParam("reportId") long reportId) {
 		int i = 0;
 		List<ReportDO> list = null;
 		ReportDao reportDao = null;
 		ReportDO res = null;
 		
		logger.info("getReportByReportId(): Entered -- reportId=" + reportId);
 		
 		try {
			if (res != null) {
				reportDao = new ReportDao();
				res = reportDao.getReportById(reportId);
			}
 		}
 		catch (Exception e) {
 			logger.severe("getReportByReportId: " + getStackTrace(e));
 		}
 		return res;
 	}
 	
 	@GET 
 	@Path("/state/{state}")
 	@Produces("application/json")
 	public List<ReportDO> getReportsByState(@PathParam("state") String state) {
 		int i = 0;
 		List<ReportDO> list = null;
 		ReportDao reportDao = null;
 		
 		logger.info("getReportsByState(): Entered -- state=" + state);
 	
 		if (state != null & state.length() == 2) {
 			try {
 				state = state.toUpperCase();
 				reportDao = new ReportDao();
 				list = reportDao.getReportsByState(state);
 			}
 			catch (Exception e) {
 				logger.severe("getReportsByState: " + getStackTrace(e));
 			}
 		}
 		return list;
 	}
 	
 	@GET 
 	@Path("/lakes/{state}")
 	@Produces("application/json")
 	public List<ShortReportDO> getLakesByState(@PathParam("state") String state) {
 		int i = 0;
 		List<ShortReportDO> list = null;
 		ReportDao reportDao = null;
 		
 		logger.info("getLakesByState(): Entered -- state=" + state);
 	
 		if (state != null & state.length() == 2) {
 			try {
 				state = state.toUpperCase();
 				reportDao = new ReportDao();
 				list = reportDao.getShortReportsByState(state);
 			}
 			catch (Exception e) {
 				logger.severe("getLakesByState: " + getStackTrace(e));
 			}
 		}
 		return list;
 	}
 	
 	/*
 	private List<RecipeDO> returnPage(List<RecipeDO> res, int pageIndex) {
 		List<RecipeDO> empty = new ArrayList<RecipeDO>();
 		//
 		// return empty set
 		//
 		if (res == null) {
 			return empty;
 		}
 	
 		//
 		// return subset
 		//
 		int start = 0;
 		int end = 0;
 		if (pageIndex > 0) {
 			start = pageIndex * MAXPAGE;
 			end = start + MAXPAGE;
 			if (end > (res.size()-1)) {
 				end = (res.size()-1); 
 			}
 			if (start <= end) {
 				logger.info("returnPage(): start=" + start + " end=" + end);
 				return res.subList(start, end);
 			}
 			else {
 				return empty;
 			}
 		}
 		else {
 			if (res != null && res.size() < MAXPAGE) {
 				end = res.size() - 1;
 			}
 			else {
 				end = MAXPAGE - 1;
 			}
 			logger.info("returnPage(): start=" + start + " end=" + end);
 			return (res != null ? res.subList(start, end) : null);
 		}
 	}
 	*/
 	
 	private String getStackTrace(Exception e) {
 		StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         String str = "\n" + sw.toString();
         return str;
 	}
 
 }
