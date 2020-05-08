 package com.mavenlab.jetset.manager;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.logging.Logger;
 import org.jboss.seam.solder.logging.Category;
 
 import com.mavenlab.jetset.controller.EntryController;
 import com.mavenlab.jetset.model.Entry;
 import com.mavenlab.jetset.response.EntriesResponse;
 import com.mavenlab.jetset.response.EntryResponse;
 import com.mavenlab.jetset.response.Response;
 import com.mavenlab.jetset.util.Pagination;
 
 @Path("/manager/entry")
 @Stateless
 public class EntryRest {
 	
 	private final static String DATE_PATTERN = "dd MMMM yyyy HH:mm:ss";
 	
 	@Inject
 	@Category("jetset.EntryRest")
 	private Logger log;
 	
 	@Inject
 	private EntryController entryController;
 
 	@Path("/list")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public EntriesResponse getEntries(@QueryParam("msisdn") String msisdn, @QueryParam("start") String start,
 			@QueryParam("end") String end, @QueryParam("page") int page, @QueryParam("type") String type) {
 		EntriesResponse response = new EntriesResponse();
 
 		Date startDate = null;
 		Date endDate = null;
 		
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
 		
 		if(msisdn != null) {
 			msisdn = msisdn.trim();
 			if(msisdn.length() > 0 && msisdn.length() < 3) {
 				response.setStatus(Response.STATUS_FAILED);
 				response.addMessage("msisdn", "Please enter at least 3 numbers");
 				return response;
 			} else if(msisdn.length() == 0) {
 				msisdn = null;
 			}
 		}
 		
 		if(start != null && start.trim().length() > 0) {
 			try {
 				start += " 00:00:00";
 				startDate = sdf.parse(start);
 			} catch (ParseException e) {
 				response.setStatus(Response.STATUS_FAILED);
 				response.addMessage("startDate", "Invalid Start Date Format");
 				return response;
 			}
 		}
 
 		if(end != null && end.trim().length() > 0) {
 			try {
 				end += " 23:59:59";
 				endDate = sdf.parse(end);
 			} catch (ParseException e) {
 				response.setStatus(Response.STATUS_FAILED);
 				response.addMessage("endDate", "Invalid End Date Format");
 				return response;
 			}
 		}
 
 		Pagination pagination = response.getPagination();
 		
 		List<Entry> entries = null;
 		
 		if(msisdn != null && (startDate != null || endDate != null)) {
 			log.info("QUERY FOR ENTRIES BY MSISDN DATE: " + startDate + " - " + endDate + " - " + msisdn);
 			pagination.setTotal(entryController.countByTypeMsisdnDate(type, msisdn, startDate, endDate));
 			pagination.setPage(page);
 			entries = entryController.fetchByTypeMsisdnDate(type, msisdn, startDate, endDate, pagination.getOffset(), pagination.getLimit());
 		} else if(msisdn == null && (startDate != null || endDate != null)) {
 			log.info("QUERY FOR ENTRIES BY DATE: " + startDate + " - " + endDate);
 			pagination.setTotal(entryController.countByTypeDate(type, startDate, endDate));
 			pagination.setPage(page);
 			entries = entryController.fetchByTypeDate(type, startDate, endDate, pagination.getOffset(), pagination.getLimit());
 		} else if(msisdn != null && startDate == null && endDate == null) {
 			log.info("QUERY FOR ENTRIES BY MSISDN: " + msisdn);
 			
 			pagination.setTotal(entryController.countByTypeMsisdn(type, msisdn));
 			pagination.setPage(page);
 			entries = entryController.fetchByTypeMsisdn(type, msisdn, pagination.getOffset(), pagination.getLimit());
 		} else {
 			log.info("QUERY FOR ALL ENTRIES");
 
 			pagination.setTotal(entryController.countAllByType(type));
 			pagination.setPage(page);
 			entries = entryController.fetchAllByType(type, pagination.getOffset(), pagination.getLimit());
 		}
 
 		if(entries != null) { 
 			for(Entry entry : entries) {
 				response.addEntry(entry);
 			}
 		}
 
 		return response;
 	}
 	
 	@Path("/sms/{entryId}")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public EntryResponse getSMSEntry(@PathParam("entryId") int entryId) {
 		return new EntryResponse(entryController.getSMSEntry(entryId));
 	}
 	
 	@Path("/web/{entryId}")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public EntryResponse getWebEntry(@PathParam("entryId") int entryId) {
 		return new EntryResponse(entryController.getWebEntry(entryId));
 	}
 }
