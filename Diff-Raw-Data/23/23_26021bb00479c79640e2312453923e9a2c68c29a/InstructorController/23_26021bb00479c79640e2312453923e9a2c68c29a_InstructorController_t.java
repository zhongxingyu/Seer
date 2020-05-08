 package ca.ubc.ctlt.ipeerb2.controller;
 
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.client.RestClientException;
 
 import blackboard.data.ReceiptOptions;
 import blackboard.data.user.User;
 import blackboard.platform.security.AccessException;
 import blackboard.platform.security.SecurityUtil;
 import blackboard.platform.servlet.InlineReceiptUtil;
 import ca.ubc.ctlt.blackboardb2util.B2Util;
 import ca.ubc.ctlt.ipeerb2.Configuration;
 import ca.ubc.ctlt.ipeerb2.CourseInfo;
 import ca.ubc.ctlt.ipeerb2.iPeerB2Util;
 import ca.ubc.ctlt.ipeerb2.domain.Course;
 import ca.ubc.ctlt.ipeerb2.domain.Department;
 import ca.ubc.ctlt.ipeerb2.form.CourseCreationForm;
 import ca.ubc.ctlt.ipeerb2.service.IPeerB2Service;
 
 import com.spvsoftwareproducts.blackboard.utils.B2Context;
 
 @Controller
 @RequestMapping("/instructor")
 @Secured("BB_ENTITLEMENT_course.content.MODIFY")
 public class InstructorController {
 	@Autowired
 	private IPeerB2Service service;
 	
 	@Autowired
     private MessageSource messageSource;
 	
 	@Autowired
 	private Configuration configuration;
 	
 	//private static final Logger logger = LoggerFactory.getLogger(InstructorController.class);
 	
 	@RequestMapping(value="/course")
 	public String course(HttpServletRequest request, @RequestParam("course_id") String bbCourseId, ModelMap model) {
 		if (configuration.connectionExists(bbCourseId)) {
 			int ipeerCourseId = configuration.getIpeerCourseId(bbCourseId);
 			model.addAttribute("course_id", bbCourseId);
 			model.addAttribute("ipeer_course_id", ipeerCourseId);
 			
 			return "manage_course";
 		}
 		B2Context b2Context = new B2Context(request);
 		
 		List<Department> departments = service.getDepartments();
 		model.addAttribute("departments", departments);
 		
 		CourseCreationForm courseForm = new CourseCreationForm();
 		blackboard.data.course.Course course = b2Context.getContext().getCourse();
 		courseForm.setBbCourseId(bbCourseId);
 		if (null != course) {
 			courseForm.setCourse(course.getBatchUid());
 			courseForm.setTitle(course.getDisplayTitle());
 		}
 		model.addAttribute("course", courseForm);
 		
 		return "course_creation_form";
 	}
 	
 	@RequestMapping(value="/course/create", method = RequestMethod.POST)
 	public String createCourse(HttpServletRequest webRequest, @ModelAttribute("course") CourseCreationForm courseForm, BindingResult result, Locale locale) {
 		ReceiptOptions ro = new ReceiptOptions();
 		
 		Course course = new Course();
 		course.setCourse(courseForm.getCourse());
 		course.setTitle(courseForm.getTitle());
 		course.setBbCourseId(courseForm.getBbCourseId());
 		
 		// creating the course in iPeer and the link, associate the departments as well
 		try {
 			service.createCourse(course);
			if (courseForm.getDepartments() != null) {
				for (Department dept : courseForm.getDepartments()) {
					service.assignCourseToDepartment(course.getId(),
							dept.getId());
				}
 			}
 			ro.addSuccessMessage(messageSource.getMessage("message.create_course_success", new Object[]{course.getCourse()}, locale));			
 		} catch (RestClientException e) {
 			ro.addErrorMessage(messageSource.getMessage("message.create_course_failed", new Object[]{course.getCourse()}, locale) + " " + e.getMessage(), e);
 		}
 
 		if (courseForm.isSyncClass()) {
 			syncClass(courseForm.getBbCourseId(), ro, locale);
 		}
 		
 		if (courseForm.isPushGroup()) {
 			pushGroups(courseForm.getBbCourseId(), ro, locale);
 		}
 		
 		if (courseForm.isPullGroup()) {
 			pullGroups(courseForm.getBbCourseId(), ro, locale);
 		}
 		
 		InlineReceiptUtil.addReceiptToRequest(webRequest, ro);
 		
 		return "redirect:/instructor/course?course_id="+course.getBbCourseId();
 	}
 	
 	@RequestMapping(value="/course/disconnect", method = RequestMethod.GET)
 	public String disconnectCourse(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 		ReceiptOptions ro = new ReceiptOptions();
 		
 		try {
 			service.disconnectCourse(bbCourseId);
 			ro.addSuccessMessage(messageSource.getMessage("message.disconnect_course_success", null, locale));
 		} catch (RestClientException e) {
 			ro.addErrorMessage(messageSource.getMessage("message.disconnect_course_failed", null, locale) + " " + e.getMessage(), e);
 		}
 		InlineReceiptUtil.addReceiptToRequest(webRequest, ro);
 		model.addAttribute("course_id", bbCourseId);
 		
 		return "redirect:/instructor/course";
 	}
 	
 	@RequestMapping(value="/course/delete", method = RequestMethod.GET)
 	public String deleteCourse(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 		ReceiptOptions ro = new ReceiptOptions();
 		try{
 			service.deleteCourse(bbCourseId);
 			ro.addSuccessMessage(messageSource.getMessage("message.delete_course_success", null, locale));
 		} catch (RestClientException e) {
 			ro.addErrorMessage(messageSource.getMessage("message.delete_course_failed", null, locale) + " " + e.getMessage(), e);
 		}
 		InlineReceiptUtil.addReceiptToRequest(webRequest, ro);
 		model.addAttribute("course_id", bbCourseId);
 		
 		return "redirect:/instructor/course";
 	}
 	
 	
 	@RequestMapping(value="/course/syncclass", method = RequestMethod.GET)
 	public String syncClass(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 		ReceiptOptions ro = new ReceiptOptions();
 		syncClass(bbCourseId, ro, locale);
 		InlineReceiptUtil.addReceiptToRequest(webRequest, ro);
 		model.addAttribute("course_id", bbCourseId);
 		
 		return "redirect:/instructor/course";
 	}
 	
 	@RequestMapping(value="/course/pushgroups", method = RequestMethod.GET)
 	public String pushGroups(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 		ReceiptOptions ro = new ReceiptOptions();
 		pushGroups(bbCourseId, ro, locale);
 		InlineReceiptUtil.addReceiptToRequest(webRequest, ro);
 		model.addAttribute("course_id", bbCourseId);
 		
 		return "redirect:/instructor/course";
 	}
 	
 	@RequestMapping(value="/course/pullgroups", method = RequestMethod.GET)
 	public String pullGroups(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 		ReceiptOptions ro = new ReceiptOptions();
 		pullGroups(bbCourseId, ro, locale);
 		InlineReceiptUtil.addReceiptToRequest(webRequest, ro);
 		model.addAttribute("course_id", bbCourseId);
 		
 		return "redirect:/instructor/course";
 	}
 	
 	@RequestMapping(value="/course/syncgrades", method = RequestMethod.GET)
 	public String syncGrades(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 		ReceiptOptions ro = new ReceiptOptions();
 		try {
 			service.syncGrades(bbCourseId);
 			ro.addSuccessMessage(messageSource.getMessage("message.sync_grades_success", null, locale));
 		} catch (RestClientException e) {
 			ro.addErrorMessage(messageSource.getMessage("message.sync_grades_failed", null, locale) + " " + e.getMessage(), e);
 		}
 		InlineReceiptUtil.addReceiptToRequest(webRequest, ro);
 		model.addAttribute("course_id", bbCourseId);
 		
 		return "redirect:/instructor/course";
 	}
 	
 	@RequestMapping(value="/course/gotoipeer", method = RequestMethod.GET)
 	public String gotoIpeer(HttpServletRequest request, @RequestParam("redirect") String redirect, Locale locale, ModelMap model) {
 		String url = configuration.getIpeerUrl();
 		User user = B2Util.getCurrentUser(request);
 		long timestamp = System.nanoTime();
 		String key = configuration.getSetting(Configuration.TOKEN_KEY);
 		String secret = configuration.getSetting(Configuration.TOKEN_SECRET);
		String signature = iPeerB2Util.calcSignature(user.getBatchUid(), timestamp, key, secret);
 		return "redirect:"+url+redirect+"?"+
				"&username="+iPeerB2Util.urlEncode(user.getBatchUid())+
 				"&timestamp="+timestamp+
 				"&token="+iPeerB2Util.urlEncode(key)+
 				"&signature="+iPeerB2Util.urlEncode(signature);
 	}
 	
 	// ajax functions
 	@RequestMapping(value="/course/bbcourseinfo", method = RequestMethod.GET)
 	public @ResponseBody CourseInfo getBbCourseInfo(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 	    CourseInfo info = new CourseInfo();
 	    info.setClassSize(service.getBbClassSize(bbCourseId));
 	    info.setGroups(service.getGroupsInBbCourse(bbCourseId));
 	    return info;
 	}
 	
 	@RequestMapping(value="/course/ipeercourseinfo", method = RequestMethod.GET)
 	public @ResponseBody CourseInfo getIPeerCourseInfo(HttpServletRequest webRequest, @RequestParam("course_id") String bbCourseId, Locale locale, ModelMap model) {
 	    CourseInfo info = new CourseInfo();
 	    info.setClassSize(service.getIPeerClassSize(bbCourseId));
 	    info.setGroups(service.getGroupsInIPeerCourse(bbCourseId));
 	    info.setEvents(service.getEventsInCourse(bbCourseId));
 	    return info;
 	}
 	
 	@InitBinder
 	protected void initBinder(HttpServletRequest request,
 			ServletRequestDataBinder binder) throws Exception {
 		binder.registerCustomEditor(Department.class, new CourseDepartmentPropertyEditor());
 	}
 	
 	private void syncClass(String bbCourseId, ReceiptOptions ro, Locale locale) {
 		try{
 			service.syncClass(bbCourseId);
 			ro.addSuccessMessage(messageSource.getMessage("message.sync_class_success", null, locale));
 		} catch (RestClientException e) {
 			ro.addErrorMessage(messageSource.getMessage("message.sync_class_failed", null, locale) + " " + e.getMessage(), e);
 		}
 	}
 	
 	private void pushGroups(String bbCourseId, ReceiptOptions ro, Locale locale) {
 		try{
 			service.pushGroups(bbCourseId);
 			ro.addSuccessMessage(messageSource.getMessage("message.sync_groups_success", null, locale));
 		} catch (RestClientException e) {
 			ro.addErrorMessage(messageSource.getMessage("message.sync_groups_failed", null, locale) + " " + e.getMessage(), e);
 		}
 	}
 	
 	private void pullGroups(String bbCourseId, ReceiptOptions ro, Locale locale) {
 		try{
 			service.pullGroups(bbCourseId);
 			ro.addSuccessMessage(messageSource.getMessage("message.sync_groups_success", null, locale));
 		} catch (RestClientException e) {
 			ro.addErrorMessage(messageSource.getMessage("message.sync_groups_failed", null, locale) + " " + e.getMessage(), e);
 		}
 	}
 	
 	public boolean currentUserHasEntitlementInCurrentContext(String entitlement) {
 		try {
 			SecurityUtil.checkEntitlement(entitlement);
 			return true;
 		} catch (AccessException e) {
 			return false;
 		}
 	}
 }
