 package com.hackfmi.summer2013.exam.scheduler.controllers;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 
import com.hackfmi.summer2013.exam.scheduler.dto.ExamRequest;
 import com.hackfmi.summer2013.exam.scheduler.services.DummyService;
 import com.hackfmi.summer2013.exam.scheduler.utils.DateUtil;
 
 @Controller
 @RequestMapping("/calendar")
 public class CalendarController {
 
 	@Autowired
 	private DummyService dummyService;
 
 	@Autowired
 	private View jsonView;
 
 	private Logger log = Logger.getLogger(CalendarController.class);
 
 	@RequestMapping("/load")
 	public ModelAndView loadCalendar() {
 		return createNewErrorResponse("loadCalendar placeholder");
 	}
 
 	@RequestMapping(value = "/getSubjects/", method = RequestMethod.GET)
 	public ModelAndView getSubjects(@RequestParam("teacherId") int teacherId) {
 		return createNewErrorResponse("getSubjects placeholder");
 	}
 
 	@RequestMapping(value = "/getSpecialty/", method = RequestMethod.GET)
 	public ModelAndView getSpecialty(@RequestParam("teacherId") int teacherId,
 			@RequestParam("subjectId") int subjectId) {
 		return createNewErrorResponse("teacherId = " + teacherId
 				+ ", subjectId = " + subjectId);
 	}
 
 	@RequestMapping(value = "/getGroups/", method = RequestMethod.GET)
 	public ModelAndView getGroups(@RequestParam("teacherId") int teacherId,
 			@RequestParam("subjectId") int subjectId,
 			@RequestParam("specialtyId") int specialtyId) {
 		return createNewErrorResponse("getGroups placeholder");
 	}
 	
 	@RequestMapping(value="/saveExam/", method = RequestMethod.POST)
	public ModelAndView saveExam(@RequestBody ExamRequest exam) {
 		return createNewErrorResponse("saveExam placeholder");
 	}
 	
 	@RequestMapping(value="/test/", method = RequestMethod.GET)
 	public ModelAndView test(@RequestParam("date") String date) {
 		return createNewErrorResponse(DateUtil.stringToDate(date).toString());
 	}
 
 	public ModelAndView createNewErrorResponse(String message) {
 		return new ModelAndView(jsonView, "errorMsg", message);
 	}
 }
