 package com.sas.comp.resource;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import com.sas.comp.models.Schedule;
 import com.sas.comp.service.ScheduleService;
 
 @Path("schedules")
 @Produces(MediaType.APPLICATION_JSON)
 public class ScheduleResource {
 
 	private final ScheduleService scheduleService = new ScheduleService();
	private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm");
 
 	@GET
 	public List<Schedule> getSchedules(@QueryParam("date") final String date) throws ParseException {
 		return scheduleService.getSchedules(sdf.parse(date));
 	}
 }
