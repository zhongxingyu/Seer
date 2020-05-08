 package com.epam.lab.intouch.web.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 
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
 		member.setFirstName(request.getParameter(Attribute.MEMBER_FIRST_NAME));
 		member.setLastName(request.getParameter(Attribute.MEMBER_LAST_NAME));
 		member.setLogin(request.getParameter(Attribute.MEMBER_LOGIN));
 		member.setPassword(request.getParameter(Attribute.MEMBER_PASSWORD));
 		member.setAdditionalInfo(Attribute.MEMBER_ADDITIONAL_INFO);
 		String birthDate = request.getParameter(Attribute.MEMBER_BIRTHDAY);
 		if (birthDate != null) {
 			Date birthdayDate = parseDate(birthDate);
 			member.setBirthday(birthdayDate);
 		}
 		Enumeration<String> tmp = request.getParameterNames();
 		Sex sex = Sex.fromString(request.getParameter(Attribute.MEMBER_SEX));
 		member.setSex(sex);
 
 		String qualification = request.getParameter(Attribute.MEMBER_QUALIFICATION);
 		QualificationLevel qlevel = QualificationLevel.fromString(qualification);
 		member.setQualificationLevel(qlevel);
 
 		String experience = request.getParameter(Attribute.MEMBER_EXPERIENCE);
 		if (experience != null) {
 			member.setExperience(Double.valueOf(experience));
 		}
 		member.setPhotoLink(request.getParameter(Attribute.MEMBER_PHOTO));
 		member.setRole(Role.fromString(request.getParameter(Attribute.MEMBER_PROJECT_ROLE)));
 
 		return member;
 	}
 
 	public static Member getUpdatedMember(HttpServletRequest request) throws InputDataFormatException {
		Member sessionMember = (Member) request.getSession().getAttribute("member");
 		String firstName = request.getParameter(Attribute.MEMBER_FIRST_NAME);
 		if (firstName != null)
 			sessionMember.setFirstName(firstName);
 		String lastName = request.getParameter(Attribute.MEMBER_LAST_NAME);
 		if (lastName != null)
 			sessionMember.setLastName(lastName);
 		String login = request.getParameter(Attribute.MEMBER_LOGIN);
 		if (login != null)
 			sessionMember.setLogin(login);
 		String password = request.getParameter(Attribute.MEMBER_PASSWORD);
 		if (password != null)
 			sessionMember.setPassword(password);
 		String birthday = request.getParameter(Attribute.MEMBER_BIRTHDAY);
 		if (birthday != null && birthday!="") {
 			Date birthdayDate = parseDate(birthday);
 			sessionMember.setBirthday(birthdayDate);
 		}
 		String experience = request.getParameter(Attribute.MEMBER_EXPERIENCE);
 		if (experience != null)
 			sessionMember.setExperience(Double.valueOf(experience));
 		String qLevel = request.getParameter(Attribute.MEMBER_QUALIFICATION);
 		if (qLevel != null) {
 			QualificationLevel qlevel = QualificationLevel.fromString(qLevel);
 			sessionMember.setQualificationLevel(qlevel);
 		}
 		return sessionMember;
 
 	}
 
 	private static Date parseDate(String date) throws InputDataFormatException {
 		Date birthdayDate = new Date();
 		try {
 			birthdayDate = new SimpleDateFormat(DATE_FORMAT).parse(date);
 		} catch (ParseException e) {
 			LOG.error("Wrong input format: " + e);
 			throw new InputDataFormatException("Wrong input format: " + e);
 		}
 
 		return birthdayDate;
 	}
 
 }
