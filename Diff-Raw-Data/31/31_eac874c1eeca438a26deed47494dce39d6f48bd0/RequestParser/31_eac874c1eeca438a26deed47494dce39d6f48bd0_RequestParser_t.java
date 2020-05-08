 package com.epam.lab.intouch.web.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.servlet.ServletRequest;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.epam.lab.intouch.controller.exception.InputDataFormatException;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.model.member.enums.QualificationLevel;
 import com.epam.lab.intouch.model.member.enums.Role;
 import com.epam.lab.intouch.model.member.enums.Sex;
 import com.epam.lab.intouch.model.project.Project;
 import com.epam.lab.intouch.model.project.enums.ProjectStatus;
 
 public class RequestParser {
 	private final static Logger LOG = LogManager.getLogger(RequestParser.class);
 
 	// we need localization to be able to do date format in more wide way
 	private static final String DATE_FORMAT = "yyyy-MM-dd";
 
 	public static Project getProject(ServletRequest request) throws InputDataFormatException {
 		Project project = new Project();
 		project.setId(Long.valueOf(request.getParameter(Attribute.PROJECT_ID)));
 		project.setProjectName(request.getParameter(Attribute.PROJECT_NAME));
 
 		Date creationDate = parseDate(request.getParameter(Attribute.PROJECT_CREATED));
 		project.setCreationDate(creationDate);
 
 		Date estimatedDate = parseDate(request.getParameter(Attribute.PROJECT_ESTIMATED_COMPLETION));
 		project.setEstimatedCompletionDate(estimatedDate);
 
 		Date completionDate = parseDate(request.getParameter(Attribute.PROJECT_COMPLETED));
 		project.setEstimatedCompletionDate(completionDate);
 
 		project.setDescription(request.getParameter(Attribute.PROJECT_DESCRIPTION));
 		project.setCustomer(request.getParameter(Attribute.PROJECT_CUSTOMER));
 		project.setStatus(ProjectStatus.fromString(request.getParameter(Attribute.PROJECT_STATUS)));
 
 		return project;
 	}
 
 	public static Member getMember(ServletRequest request) throws InputDataFormatException {
 		Member member = new Member();
 		member.setFirstName(request.getParameter(Attribute.MEMBER_NAME));
 		member.setLastName(request.getParameter(Attribute.MEMBER_SURNAME));
 		member.setLogin(request.getParameter(Attribute.MEMBER_LOGIN));
 		member.setPassword(request.getParameter(Attribute.MEMBER_PASSWORD));
 
		String birthDate = request.getParameter(Attribute.MEMBER_BIRTHDAY);
		if (birthDate != null) {
			Date birthdayDate = parseDate(birthDate);
			member.setBirthday(birthdayDate);
		}
 
 		Sex sex = Sex.fromString(request.getParameter(Attribute.MEMBER_SEX));
 		member.setSex(sex);
 
 		String qualification = request.getParameter(Attribute.MEMBER_QUALIFICATION);
 		QualificationLevel qlevel = QualificationLevel.fromString(qualification);
 		member.setQualificationLevel(qlevel);
 
		String experience = request.getParameter(Attribute.MEMBER_EXPERIENCE);
		if (experience != null) {
			member.setExperience(Double.valueOf(experience));
		}
 		member.setPhotoURI(request.getParameter(Attribute.MEMBER_PHOTO));
 		member.setRole(Role.fromString(request.getParameter(Attribute.MEMBER_PROJECT_ROLE)));
 
 		return member;
 	}
 
 	private static Date parseDate(String date) throws InputDataFormatException {
 		Date birthdayDate = null;
 		try {
 			birthdayDate = new SimpleDateFormat(DATE_FORMAT).parse(date);
 		} catch (ParseException e) {
 			LOG.error("Wrong input format: " + e);
 			throw new InputDataFormatException("Wrong input format: " + e);
 		}
 
 		return birthdayDate;
 	}
 
 }
