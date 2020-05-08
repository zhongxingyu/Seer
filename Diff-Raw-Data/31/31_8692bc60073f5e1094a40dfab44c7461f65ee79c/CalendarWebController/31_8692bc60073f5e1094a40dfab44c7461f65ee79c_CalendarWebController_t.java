 package org.societies.rdpartyService.enterprise.cal.controllers;
 
import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import org.societies.api.cis.management.ICis;
 import org.societies.api.cis.management.ICisManager;
 import org.societies.api.ext3p.schema.sharedcalendar.SharedCalendarResult;
 import org.societies.rdpartyService.enterprise.interfaces.ISharedCalendarClientRich;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
 
 @Controller
 public class CalendarWebController {
 	
 	private static long _DEADLINE = 3000;
 	
 	public CalendarWebController() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	private SharedCalendarResult result = null;
 	
 	private Gson gson = new Gson();
 	
 	@Autowired
 	private ISharedCalendarClientRich calClientService = null;
 	@Autowired
 	private ICisManager cisManagerService = null;
 	
 	private CalendarWebResultCallback cb = null;
 
 	public ISharedCalendarClientRich getCalClientService() {
 		return calClientService;
 	}
 	
 	public void setCalClientService(ISharedCalendarClientRich calClientService) {
 		this.calClientService = calClientService;
 	}
 	
 	public ICisManager getCisManagerService() {
 		return cisManagerService;
 	}
 
 	public void setCisManagerService(ICisManager cisManagerService) {
 		this.cisManagerService = cisManagerService;
 	}
 	
 	/*
 	 <xs:enumeration value="createCISCalendar"/>
       <xs:enumeration value="deleteCISCalendar"/>
       <xs:enumeration value="retrieveCISCalendarList"/>
       <xs:enumeration value="retrieveCISCalendarEvents"/>
       <xs:enumeration value="createEventOnCISCalendar"/>
       <xs:enumeration value="deleteEventOnCISCalendar"/>
       <xs:enumeration value="subscribeToEvent"/>
       <xs:enumeration value="findEvents"/>
       <xs:enumeration value="unsubscribeFromEvent"/>
       <xs:enumeration value="createPrivateCalendar"/>
       <xs:enumeration value="deletePrivateCalendar"/>
       <xs:enumeration value="createEventOnPrivateCalendar"/>
       <xs:enumeration value="retrieveEventsOnPrivateCalendar"/>
       <xs:enumeration value="deleteEventOnPrivateCalendar"/>
     */
 	
 	private boolean inDeadline(long start){
 		return (System.currentTimeMillis()-start<CalendarWebController._DEADLINE);
 	}
 	
 	private void wait4semaphore(){
 		Semaphore s = this.cb.getSem();
 		long start = System.currentTimeMillis();
 		//TODO Add a deadline to avoid infinite wait
 		while (!s.tryAcquire() && inDeadline(start)){
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				//Do nothing
 			}
 		}		
 	}
 	
 	@RequestMapping("/getAllRelevantCis.do")
 	public @ResponseBody String getRelevantCis() {
 		this.cb = new CalendarWebResultCallback(this);
 		List<ICis> foundCiss = new ArrayList<ICis>();
		List<MyCisRecord> foundCisRecords = new ArrayList<MyCisRecord>();
		Type listType = new TypeToken<List<MyCisRecord>>(){}.getType();
 		if (this.cisManagerService!=null){
 			foundCiss = this.cisManagerService.getCisList();
 		}
		for (ICis currentCis : foundCiss) {
			foundCisRecords.add(new MyCisRecord(currentCis.getName(), currentCis.getCisId()));
		}
		String ajaxResult = this.gson.toJson(foundCisRecords, listType);
 		return ajaxResult;
 	}
 	
 	@RequestMapping("/getAllCisCalendarsAjax.do")
 	public @ResponseBody String getContactsAjax(@RequestParam(defaultValue="TestCIS", value="cisId") String cisId) {
 		//calClientService.retrieveEventsPrivateCalendar(this.cb);
 		this.cb = new CalendarWebResultCallback(this);
 		//TODO remove hard-coded string
 		this.calClientService.retrieveCISCalendars(this.cb, cisId);
 		this.wait4semaphore();
 		String ajaxResult = this.gson.toJson(this.result);
 		return ajaxResult;
 	}
 	
 	@RequestMapping("/createCisCalendarAjax.do")
 	public @ResponseBody String createCisCalendarAjax(@RequestParam(defaultValue="TestCIS", value="cisId") String cisId, @RequestParam(defaultValue="Calendar Summary", value="cisSummary") String summary) {
 		//calClientService.retrieveEventsPrivateCalendar(this.cb);
 		this.cb = new CalendarWebResultCallback(this);
 		this.calClientService.createCISCalendar(this.cb, summary, cisId);
 		this.wait4semaphore();		
 		String ajaxResult = this.gson.toJson(this.result);
 		return ajaxResult;
 	}
 	
 	@RequestMapping("/getCisCalendarEvents.do")
 	public @ResponseBody String getCisCalendarEvents(@RequestParam(defaultValue="myCisCalendarId", value="calendarId") String calendarId) {
 		this.cb = new CalendarWebResultCallback(this);
 		this.calClientService.retrieveCISCalendarEvents(this.cb, calendarId);
 		this.wait4semaphore();
 		String ajaxResult = this.calClientService.createJSONOEvents(this.result.getEventList());
 		return ajaxResult;
 	}
 	
 	@RequestMapping("/createCssCalendarAjax.do")
 	public @ResponseBody String createCssCalendarAjax(@RequestParam(defaultValue="CSS Calendar Summary", value="cssSummary") String summary) {
 		//calClientService.retrieveEventsPrivateCalendar(this.cb);
 		this.cb = new CalendarWebResultCallback(this);
 		this.calClientService.createPrivateCalendar(this.cb, summary);
 		this.wait4semaphore();
 		String ajaxResult = this.gson.toJson(this.result);
 		return ajaxResult;
 	}
 		
 	@RequestMapping("/getPrivateEvents.do")
 	public @ResponseBody String getPrivateEvents() {
 		this.cb = new CalendarWebResultCallback(this);
 		this.calClientService.retrieveEventsPrivateCalendar(this.cb);
 		this.wait4semaphore();
 		String ajaxResult = this.calClientService.createJSONOEvents(this.result.getEventList());
 		return ajaxResult;
 	}
 	
 	@RequestMapping("/deletePrivateCalendar.do")
 	public @ResponseBody String deletePrivateCalendar() {
 		this.cb = new CalendarWebResultCallback(this);
 		this.calClientService.deletePrivateCalendar(this.cb);
 		this.wait4semaphore();
 		String ajaxResult = this.gson.toJson(this.result);
 		return ajaxResult;
 	}
 	
 	@RequestMapping("/deleteCisCalendar.do")
 	public @ResponseBody String deleteCisCalendar(@RequestParam(defaultValue="myCisCalendarId", value="calendarId") String calendarId) {
 		this.cb = new CalendarWebResultCallback(this);
 		this.calClientService.deleteCISCalendar(this.cb, calendarId);
 		this.wait4semaphore();
 		String ajaxResult = this.gson.toJson(this.result);
 		return ajaxResult;
 	}
 	
 	@RequestMapping("/index.do")
 	public String getHome() {
 		return "Index";
 	}
 
 
 	public SharedCalendarResult getResult() {
 		return result;
 	}
 
 	public void setResult(SharedCalendarResult result) {
 		this.result = result;
 	}
 
 }
