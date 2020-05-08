 package com.epam.lab.intouch.web.util;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.epam.lab.intouch.controller.exception.InputDataFormatException;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.model.member.enums.QualificationLevel;
 import com.epam.lab.intouch.model.member.enums.Role;
 import com.epam.lab.intouch.model.member.enums.Sex;
 import com.epam.lab.intouch.model.member.info.skill.Skill;
 import com.epam.lab.intouch.model.member.info.skill.SkillType;
 import com.epam.lab.intouch.model.project.Project;
 
 public final class RequestParser {
 	private static final Logger LOG = LogManager.getLogger(RequestParser.class);
 	private static final String DATE_FORMAT = "yyyy-MM-dd";
 
 	private RequestParser() {
 
 	}
 
 	public static Member getMember(final ServletRequest request) throws InputDataFormatException {
 		final Member member = new Member();
 
 		member.setFirstName(changeEncoding(request.getParameter(Attribute.MEMBER_FIRST_NAME)));
 		member.setLastName(changeEncoding(request.getParameter(Attribute.MEMBER_LAST_NAME)));
 
 		member.setLogin(changeEncoding(request.getParameter(Attribute.MEMBER_LOGIN)));
 		member.setPassword(changeEncoding(request.getParameter(Attribute.MEMBER_PASSWORD)));
 
 		Random random = new Random(100);
 		int rnd = random.nextInt();
 		String gender = request.getParameter(Attribute.MEMBER_SEX);
 	
 		if (gender.equals("dont_now")) {
 			if (rnd >= 50) {
 				gender = "MALE";
 			
 			} else {
 				gender = "FEMALE";
 		
 			}
 
 		}
 		LOG.debug(gender);
 		member.setSex(Sex.fromString(gender));
 
 		member.setRole(Role.fromString(request.getParameter(Attribute.MEMBER_PROJECT_ROLE)));
 
 		return member;
 	}
 
 	public static Member getUpdatedMember(final HttpServletRequest request) throws InputDataFormatException {
 		final Member sessionMember = (Member) request.getSession().getAttribute("member");
 
 		String firstName = changeEncoding(request.getParameter(Attribute.MEMBER_FIRST_NAME));
 		if (firstName != null) {
 			sessionMember.setFirstName(firstName);
 		}
 
 		String lastName = changeEncoding(request.getParameter(Attribute.MEMBER_LAST_NAME));
 		if (lastName != null) {
 			sessionMember.setLastName(lastName);
 		}
 
 		String login = changeEncoding(request.getParameter(Attribute.MEMBER_LOGIN));
 		if (login != null) {
 			sessionMember.setLogin(login);
 		}
 
 		String password = changeEncoding(request.getParameter(Attribute.MEMBER_PASSWORD));
 		if (password != null)
 			sessionMember.setPassword(password);
 
 		String birthday = changeEncoding(request.getParameter(Attribute.MEMBER_BIRTHDAY));
 		if (birthday != null && !birthday.isEmpty()) {
 			Date birthdayDate = parseDate(birthday);
 			sessionMember.setBirthday(birthdayDate);
 		}
 
 		String experience = request.getParameter(Attribute.MEMBER_EXPERIENCE);
 		if (experience != null) {
 			sessionMember.setExperience(Double.valueOf(experience));
 		}
 
 		String qLevel = request.getParameter(Attribute.MEMBER_QUALIFICATION);
 		if (qLevel != null) {
 			QualificationLevel qlevel = QualificationLevel.fromString(qLevel);
 			sessionMember.setQualificationLevel(qlevel);
 		}
 
 		String gender = request.getParameter(Attribute.MEMBER_SEX);
 		if (gender != null) {
 			Sex sex = Sex.fromString(gender);
 			sessionMember.setSex(sex);
 		}
 
 		String additionalInfo = changeEncoding(request.getParameter(Attribute.MEMBER_ADDITIONAL_INFO));
 		if (additionalInfo != null) {
 			sessionMember.setAdditionalInfo(additionalInfo);
 		}
 
 		return sessionMember;
 	}
 
 	public static Project getProject(final ServletRequest request) throws InputDataFormatException {
 		final Project project = new Project();
 
 		project.setProjectName(changeEncoding(request.getParameter(Attribute.PROJECT_NAME)));
 
		Date estCompletion = null;
		try {
			estCompletion = new SimpleDateFormat(DATE_FORMAT).parse(request.getParameter(Attribute.PROJECT_ESTIMATED_COMPLETION));
		} catch (ParseException e) {
			LOG.error("Can't parse EstimatedCompletion date while updating project", e);
		}

		if (estCompletion != null) {
			project.setEstimatedCompletionDate(estCompletion);
		}
		
		
		/*Date estimatedDate = parseDate(request.getParameter(Attribute.PROJECT_ESTIMATED_COMPLETION));
		project.setEstimatedCompletionDate(estimatedDate);*/
 
 		project.setDescription(changeEncoding(request.getParameter(Attribute.PROJECT_DESCRIPTION)));
 
 		project.setCustomer(changeEncoding(request.getParameter(Attribute.PROJECT_CUSTOMER)));
 
 		return project;
 	}
 
 	public static Project getUpdatedProject(final HttpServletRequest request, final Project project) throws InputDataFormatException {
 
 		Date estCompletion = null;
 		try {
 			estCompletion = new SimpleDateFormat(DATE_FORMAT).parse(request.getParameter(Attribute.PROJECT_ESTIMATED_COMPLETION));
 		} catch (ParseException e) {
 			LOG.error("Can't parse EstimatedCompletion date while updating project", e);
 		}
 
 		if (estCompletion != null) {
 			project.setEstimatedCompletionDate(estCompletion);
 		}
 
 		String description = changeEncoding(request.getParameter(Attribute.PROJECT_DESCRIPTION));
 		if (description != null) {
 			project.setDescription(description);
 		}
 
 		String customer = changeEncoding(request.getParameter(Attribute.PROJECT_CUSTOMER));
 		if (customer != null) {
 			project.setCustomer(customer);
 		}
 
 		return project;
 	}
 
 	private static Date parseDate(final String date) throws InputDataFormatException {
 
 		Date temp = new Date();
 
 		try {
 			temp = new SimpleDateFormat(DATE_FORMAT).parse(date);
 		} catch (ParseException e) {
 			LOG.error("Wrong input format: " + e);
 			throw new InputDataFormatException("Wrong input format: " + e);
 		}
 
 		return temp;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Skill> getUpdatedMemberSkills(final HttpServletRequest request) {
 
 		String[] memberProgrammingSkills = request.getParameterValues("memProgSkills");
 		String[] memberLanguageSkills = request.getParameterValues("memLangSkills");
 		String[] memberTechnologySkills = request.getParameterValues("memTechSkills");
 
 		List<Skill> techSkill = getSkillFromParameters(memberTechnologySkills, SkillType.TECHNOLOGY);
 		List<Skill> langSkill = getSkillFromParameters(memberLanguageSkills, SkillType.LANGUAGE);
 		List<Skill> progSkill = getSkillFromParameters(memberProgrammingSkills, SkillType.PROGRAMMING);
 
 		List<Skill> allSkill = mergeAllSkill(techSkill, langSkill, progSkill);
 		return allSkill;
 	}
 
 	private static List<Skill> getSkillFromParameters(final String[] parameters, final SkillType skillType) {
 		List<Skill> memberSkills = new ArrayList<Skill>();
 
 		if (parameters != null) {
 			for (int i = 0; i < parameters.length; i++) {
 				Skill skill = new Skill();
 				skill.setSkillType(skillType);
 				skill.setName(parameters[i]);
 				memberSkills.add(skill);
 			}
 		}
 
 		return memberSkills;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static List<Skill> mergeAllSkill(final List<Skill>... lists) {
 		final List<Skill> memberSkills = new ArrayList<Skill>();
 
 		for (List<Skill> list : Arrays.asList(lists)) {
 			memberSkills.addAll(list);
 		}
 
 		return memberSkills;
 	}
 
 	public static String changeEncoding(final String input) {
 		String temp = null;
 
 		try {
 			if (input != null) {
 				temp = new String(input.getBytes("ISO-8859-1"), "UTF-8");
 			}
 		} catch (UnsupportedEncodingException e) {
 			LOG.error("Can't change encoding while parsing string", e);
 		}
 
 		return temp;
 	}
 }
