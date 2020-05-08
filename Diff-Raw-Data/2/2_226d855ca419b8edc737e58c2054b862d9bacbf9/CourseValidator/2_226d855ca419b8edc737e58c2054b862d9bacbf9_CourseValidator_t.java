 package org.vist.vistadmin.web.validator;
 
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.context.support.DefaultMessageSourceResolvable;
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 import org.vist.vistadmin.domain.CompletedClass;
 import org.vist.vistadmin.domain.Course;
 import org.vist.vistadmin.domain.CourseIncome;
 import org.vist.vistadmin.domain.CourseStudent;
 import org.vist.vistadmin.domain.CourseTeacher;
 import org.vist.vistadmin.domain.common.ClassStatus;
 import org.vist.vistadmin.service.CompletedClassService;
 import org.vist.vistadmin.service.CourseIncomeService;
 import org.vist.vistadmin.service.CourseService;
 import org.vist.vistadmin.service.CourseStudentService;
 import org.vist.vistadmin.service.CourseTeacherService;
 import org.vist.vistadmin.util.DateUtil;
 import org.vist.vistadmin.web.dto.CourseCopy;
 
 @Configurable
 public class CourseValidator implements Validator {
 
 		private static Logger LOGGER = LoggerFactory.getLogger(CourseValidator.class);
 	
 		@Autowired
 		CourseService courseService;
 	
 		@Autowired
 		CourseTeacherService courseTeacherService;
 		
 		@Autowired
 		CourseStudentService courseStudentService;
 		
 		@Autowired
 		CompletedClassService completedClassService;
 		
 		@Autowired
 		CourseIncomeService courseIncomeService;
 		
 	    private Validator validator;
 
 	    public CourseValidator(Validator validator) {
 	        this.validator = validator;
 	    }
 
 	    @Override
 	    @SuppressWarnings("rawtypes")
 	    public boolean supports(Class clazz) {
 	        return Course.class.equals(clazz) || CourseCopy.class.equals(clazz);
 	    }
 
 	    @Override
 	    public void validate(Object target, Errors errors) {
 	    	validator.validate(target, errors);	    	
 	    	if(target instanceof Course) {	    	
 		    	Course course = (Course)target;
 		    	validateCourse(course, errors, null);
 	    	} else if(target instanceof CourseCopy) { 
 	    		CourseCopy courseCopy = (CourseCopy)target;
 		    	validateCourse(null, errors, courseCopy);
 		    	validateCourseCopy(courseCopy, errors);
 	    	}
 	    }
 	    
 	    private void validateCourseCopy(CourseCopy courseCopy, Errors errors) {
 	    	if(courseCopy.getCourseStudentDiscounts() != null && courseCopy.getCourseStudentDiscounts().length > 0) {
 	    		if(courseCopy.getCourseStudents() == null || courseCopy.getCourseStudents().length == 0) {
 	    			LOGGER.debug("course copy courseStudentDiscounts not valid, courseStudents is empty");
 		    		errors.rejectValue( "courseStudentDiscounts", "error_course_copy_coursestudents_are_empty");	
 	    		} else {
 	    			for(String csdStr : courseCopy.getCourseStudentDiscounts()) {
 	    				LOGGER.trace("csdStr: " + csdStr);
 						String[] csdStrs = csdStr.split("_");
 						boolean courseStudentFound = false;
 						for(String cs : courseCopy.getCourseStudents()) {
 							if(cs.equals(csdStrs[0])) {
 								courseStudentFound = true;
 								break;
 							}
 						}
 						if(!courseStudentFound) {
 							LOGGER.debug("course copy courseStudentDiscounts not valid, no courseStudent for discount");
 				    		errors.rejectValue( "courseStudentDiscounts", "error_course_copy_no_coursestudent_for_discount");
 						}
 					}
 	    		}
 	    	}        	
 	    }
         	
 	    
 	    private void validateCourse(Course course, Errors errors, CourseCopy courseCopy) {
 	    	String keyPrefix = "";
 	    	if(courseCopy != null) {
 	    		keyPrefix = "course.";
 	    		course = courseCopy.getCourse();
 	    	}
 	    	
 	    	boolean isNew = false;
 	    	if(course.getId() == null || courseCopy != null) {
 	    		isNew = true;
 	    	}
 	    		    	
 	    	if(course.getCourseId() == null || course.getCourseId().equals("")) {
 	    		LOGGER.debug("courseId not valid, it is empty");
 	    		errors.rejectValue(keyPrefix + "courseId", "error_common_unique_field");
 	    	} else {
 	    		if(course.getCourseId().contains(".")) {
 	    			errors.rejectValue(keyPrefix + "courseId", "error_course_courseid_contains_dot_char");
 	    		} else {
 		    		List<Course> courses = courseService.findByCourseId(course.getCourseId());
 		    		boolean unique = true;
 		    		if(courses != null && courses.size() > 0) {
 		    			if(isNew) {
 		    				unique = false;
 		    			} else {
 		    				for(Course c : courses) {
		    					if(!c.getId().equals(course.getId())) {
 		    						unique = false;
 		    						break;
 		    					}
 		    				}
 		    			}
 		    		}	    		
 	    		
 		    		if(!unique) {
 		    			LOGGER.debug("courseId not unique");
 		    			errors.rejectValue(keyPrefix + "courseId", "error_common_unique_field", 
 	    					new Object[] {new DefaultMessageSourceResolvable("label_org_vist_vistadmin_domain_course"), 
 	    								new DefaultMessageSourceResolvable("label_org_vist_vistadmin_domain_course_courseid")}, null);
 		    		}
 	    		}
 	    	}
 
 	    	if(isNew) {
 	    		course.setCreationDate(new Date());
 	    	}
 	    	
 	    	if(course.getCompany()) {
 	    		if(!course.getVat()) {
 	    			errors.rejectValue(keyPrefix + "vat", "error_course_comapny_not_vat");
 	    		}
 	    	}
 	    	
 	    	Date startDate = course.getStartDate();
 	    	Date endDate = course.getEndDate();
 	    	if(courseCopy != null) {
 	    		startDate = courseCopy.getNewStartDate();
 	    		endDate = courseCopy.getNewEndDate();
 	    	} 
     		if(startDate == null) {
 	    		LOGGER.debug("startDate not valid");
 	    		errors.rejectValue( courseCopy == null ? "startDate" : "newStartDate", "error_common_required_field", 
 	    					new Object[] {new DefaultMessageSourceResolvable("label_org_vist_vistadmin_domain_course_startdate")}, null);
 	    	}
 	    	if(endDate == null) {
 	    		LOGGER.debug("endDate not valid");
 	    		errors.rejectValue(courseCopy == null ? "endDate" : "newEndDate", "error_common_required_field", 
 	    					new Object[] {new DefaultMessageSourceResolvable("label_org_vist_vistadmin_domain_course_enddate")}, null);
 	    	}	
 	    		    	
 	    	
 	    	if(startDate != null && endDate != null) {
 	    		if(startDate.after(endDate)) {
 	    			LOGGER.debug("startdate - endDate not valid");
 	    			errors.rejectValue(courseCopy == null ? "endDate" : "newEndDate", "error_startdate_after_enddate");
 	    		}
 	    	}
 	    	
 	    	if(!isNew) {
 	    		Course oldCourse = courseService.findCourse(course.getId());
 	    		List<CourseTeacher> courseTeacherList = courseTeacherService.findByCourse(course);
 	    		boolean hasTeacherAndStudent = false;
 	    		if(courseTeacherList != null && courseTeacherList.size() > 0) {
 	    			List<CourseStudent> courseStudentList = courseStudentService.findByCourse(course);
 	    			if(courseStudentList != null && courseStudentList.size() > 0) {
 	    				hasTeacherAndStudent = true;
 	    			}
 	    		}
 	    		if(!hasTeacherAndStudent) {
 	    			if(oldCourse.getStatus().equals(ClassStatus.DRAFT) && !course.getStatus().equals(ClassStatus.DRAFT)) {
 	    				LOGGER.debug("course is moving from draft to any other, while no student and/or teacher is assigned");
 		    			errors.rejectValue(keyPrefix + "status", "error_status_modified_no_teacher_or_student");
 	    			}
 	    		}
 	    		
 	    		if(course.getStatus().equals(ClassStatus.ARCHIVED)) {
 	    			
 	    			if(!oldCourse.getStatus().equals(ClassStatus.ARCHIVED)) {
 	    				LOGGER.debug("archive a course, id: " + course.getId() + ", courseId: " + course.getCourseId());
 	    				Date currDate = new Date();
 	    				if(!(DateUtil.isSameDay(course.getEndDate(), currDate) || course.getEndDate().before(currDate))) {
 	    					LOGGER.debug("course cannot be archived, end date is in the future");
 			    			errors.rejectValue(keyPrefix + "status", "error_status_archived_enddate_future");	
 	    				}
 	    				
 	    				List<CourseIncome> courseIncomeList = courseIncomeService.findByCourse(course);
 	    				if(courseIncomeList != null && courseIncomeList.size() > 0) {
 	    					for (CourseIncome courseIncome : courseIncomeList) {
 								if(!courseIncome.isPayed()) {
 									LOGGER.debug("course cannot be archived, has unpayed courseIncome");
 					    			errors.rejectValue(keyPrefix + "status", "error_status_archived_has_unpayed_courseincome");
 									break;
 								}
 							}
 	    				}
 	    				
 	    				List<CompletedClass> completedClassList = completedClassService.findByCourse(course);
 	    				if(completedClassList != null && completedClassList.size() > 0) {
 	    					for (CompletedClass completedClass : completedClassList) {
 								if(!completedClass.getPayed()) {
 									LOGGER.debug("course cannot be archived, has unpayed completedClass");
 					    			errors.rejectValue(keyPrefix + "status", "error_status_archived_has_unpayed_completedclass");
 									break;
 								}
 							}
 	    				}	    				
 	    			}
 	    		}	    		    		
 	    	} else {
 	    		course.setStatus(ClassStatus.DRAFT);
 	    	}	    
 	    }
 }
