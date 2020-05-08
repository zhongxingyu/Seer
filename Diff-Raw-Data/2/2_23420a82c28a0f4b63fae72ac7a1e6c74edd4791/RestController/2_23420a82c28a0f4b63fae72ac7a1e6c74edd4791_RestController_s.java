 package com.scheduler.controllers;
  
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.json.simple.parser.ParseException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.scheduler.models.Campus;
 import com.scheduler.models.Department;
 import com.scheduler.models.DepartmentTimeslotLinkage;
 import com.scheduler.models.Departmenttimeslot;
 import com.scheduler.models.User;
 import com.scheduler.models.Utility;
 import com.scheduler.request.CommonResponse;
 import com.scheduler.services.CampusService;
 import com.scheduler.services.DepartmentService;
 import com.scheduler.services.DepartmentTimeslotService;
  
 @RequestMapping("/api")
 @Controller
 public class RestController {
  
     protected static final String JSON_CONTENT = "application/json";
     
     @Autowired(required = true)
     private CampusService campusService;
     @Autowired(required = true)
     private DepartmentService departmentService;
     @Autowired(required = true)
     private DepartmentTimeslotService departmentTimeslotService;
        
     
     @RequestMapping(value = "/getCampusByClient/{id}", method = RequestMethod.GET, produces = JSON_CONTENT)
     @ResponseBody
     public List<Campus> getCampusByClient(@PathVariable int id) {
     	List<Campus> campuses = campusService.campusByClient(id);
         return campuses;
     }
     
     @RequestMapping(value = "/getDepartmentByCampus/{id}", method = RequestMethod.GET, produces = JSON_CONTENT)
     @ResponseBody
     public List<Department> getDepartmentByCampus(@PathVariable int id) {
     	List<Department> departments = departmentService.departmentByCampus(id);
         return departments;
     }
     
     
     @RequestMapping(value = "/getDepartmentTimeslot/{id}/{dt}", method = RequestMethod.GET, produces = JSON_CONTENT)
     @ResponseBody
     public List<DepartmentTimeslotLinkage> getDepartmentTimeslot(@PathVariable int id, @PathVariable String dt) throws java.text.ParseException, ParseException {
     	Date d = null;
     	d = new SimpleDateFormat("yyyy-mm-dd").parse(dt);
		Utility u = new Utility(id, d);
 		System.out.println(u.getDepartmentId() + " - "+ u.getAppointmentDate());
 		List<DepartmentTimeslotLinkage> availableTimeslots = departmentTimeslotService.timeslotByDepartment(u);
     	return availableTimeslots;
     }
 }
