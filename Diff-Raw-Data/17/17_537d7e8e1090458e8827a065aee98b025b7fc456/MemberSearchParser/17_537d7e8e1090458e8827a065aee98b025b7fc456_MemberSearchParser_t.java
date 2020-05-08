 package com.epam.lab.intouch.web.util.request.parser;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.epam.lab.intouch.controller.util.query.builder.QueryBuilder;
 import com.epam.lab.intouch.controller.util.query.from.Table;
 import com.epam.lab.intouch.controller.util.query.select.Column;
 import com.epam.lab.intouch.controller.util.query.where.Condition;
 import com.epam.lab.intouch.controller.util.query.where.ConditionGroup;
 import com.epam.lab.intouch.controller.util.query.where.Operator;
 import com.epam.lab.intouch.util.db.metadata.FieldName;
 import com.epam.lab.intouch.util.db.metadata.TableName;
import static com.epam.lab.intouch.web.util.RequestParser.changeEncoding;
 
 public class MemberSearchParser {
 	private Table member;
 	private Table memberSkills;
 	private Table skills;
 
 	public MemberSearchParser() {
 		member = new Table(TableName.MEMBER);
 		memberSkills = new Table(TableName.MEMBER_SKILLS);
 		skills = new Table(TableName.SKILLS);
 	}
 
 	private ConditionGroup getBoundedConditionGroup(Column field, String lowerBound, String upperBound) {
 		Condition lowerBoundCond = new Condition(field, Operator.GREATER_OR_EQUAL, lowerBound, true);
 		Condition upperBoundCond = new Condition(field, Operator.LESS_OR_EQUAL, upperBound, true);
 
 		ConditionGroup condGroup = new ConditionGroup();
 		condGroup.setOperator(Operator.AND);
 
 		condGroup.addCondition(lowerBoundCond);
 		condGroup.addCondition(upperBoundCond);
 
 		return condGroup;
 	}
 
 	private ConditionGroup getMultipleConditionGroup(String[] parameters, Column field) {
 		ConditionGroup condGroup = new ConditionGroup();
 		condGroup.setOperator(Operator.OR);
 
 		if (parameters != null) {
 			for (int i = 0; i < parameters.length; i++) {
 				Condition cond = new Condition(field, Operator.EQUALS, parameters[i], true);
 				condGroup.addCondition(cond);
 			}
 		}
 
 		return condGroup;
 
 	}
 
 	private Condition getGenderCondition(HttpServletRequest request) {
 		String gender = request.getParameter("sex");
 		if ("all".equals(gender)) {
 			gender = null;
 		}
 
 		return new Condition(member.getColumn(FieldName.SEX), Operator.EQUALS, gender, true);
 	}
 
 	private ConditionGroup getGeneralExperienceCondGroup(HttpServletRequest request) {
 		String lowerBound = request.getParameter("expirienceLowerBound");
 		String upperBound = request.getParameter("expirienceUpperBound");
 
 		return getBoundedConditionGroup(member.getColumn(FieldName.EXPERIENCE), lowerBound, upperBound);
 	}
 
 	private ConditionGroup getQualificationCondGroup(HttpServletRequest request) {
 		String[] qualifications = request.getParameterValues("qualification");
 
 		return getMultipleConditionGroup(qualifications, member.getColumn(FieldName.QLEVEL));
 	}
 
 	private ConditionGroup getRoleCondGroup(HttpServletRequest request) {
 		String[] roles = request.getParameterValues("role");
 
 		return getMultipleConditionGroup(roles, member.getColumn(FieldName.ROLE));
 	}
 
 	private Condition getAddMemberSearchRoleCond(HttpServletRequest request) {
 		String role = request.getParameter("role");
 		Condition cond = null;
 
 		if ("manager".equals(role)) {
 			cond = new Condition(member.getColumn(FieldName.ROLE), Operator.NOT_EQUAL, role, true);
 		} else {
 			cond = new Condition(member.getColumn(FieldName.ROLE), Operator.EQUALS, role, true);
 		}
 		return cond;
 	}
 
 	private ConditionGroup getSkillNameCondGroup(HttpServletRequest request) {
 		String[] skillNames = request.getParameterValues("skillName");
 
 		return getMultipleConditionGroup(skillNames, skills.getColumn(FieldName.NAME));
 	}
 
 	// private ConditionGroup getSkillExperienceCondGroup(HttpServletRequest request) {
 	//
 	// String lowerBound = request.getParameter("expirienceLowerBound");
 	// String upperBound = request.getParameter("skillExperienceUpperBound");
 	//
 	// return getBoundedConditionGroup(memberSkills.getColumn(FieldName.EXPERIENCE), lowerBound, upperBound);
 	// }
 
 	// private ConditionGroup getSkillLevelConditionGroup(HttpServletRequest request) {
 	//
 	// String lowerBound = request.getParameter("lowerBoundLevel");
 	// String upperBound = request.getParameter("upperBoundLevel");
 	//
 	// return getBoundedConditionGroup(memberSkills.getColumn(FieldName.SELF_ASSESSED_LEVEL), lowerBound, upperBound);
 	// }
 
 	// private ConditionGroup getSkillCondGroup(HttpServletRequest request) {
 	// ConditionGroup skillConditionGroup = new ConditionGroup();
 	// skillConditionGroup.setOperator(Operator.AND);
 	//
 	// skillConditionGroup.addCondition(getSkillNameCondGroup(request));
 	//
 	// ConditionGroup skillLevelCondGroup = getSkillLevelConditionGroup(request);
 	// ConditionGroup skillExperienceCondGroup = getSkillExperienceCondGroup(request);
 	//
 	// return skillConditionGroup;
 	// }
 
 	private ConditionGroup getCondGroup(HttpServletRequest request) {
 
 		ConditionGroup conditionGroup = new ConditionGroup();
 		conditionGroup.setOperator(Operator.AND);
 
 		conditionGroup.addCondition(getGenderCondition(request));
 		conditionGroup.addCondition(getGeneralExperienceCondGroup(request));
 		conditionGroup.addCondition(getQualificationCondGroup(request));
 		conditionGroup.addCondition(getSkillNameCondGroup(request));
 
 		return conditionGroup;
 	}
 
 	private QueryBuilder getQueryBuilderBody(HttpServletRequest request) {
 		Condition memberSkillsMemberKey = new Condition(member.getColumn(FieldName.LOGIN), Operator.EQUALS, memberSkills.getColumn(FieldName.MEMBER_ID));
 		Condition skillsMemberSkillsKey = new Condition(memberSkills.getColumn(FieldName.SKILL_ID), Operator.EQUALS, skills.getColumn(FieldName.ID));
 
 		if (!getSkillNameCondGroup(request).isValid()) {
 			skills = null;
 			memberSkills = null;
 		}
 
 		QueryBuilder builder = new QueryBuilder();
 		builder.setDistinct(true);
 
 		builder.select(member.getWildcard()).from(member).inerJoin(memberSkills, memberSkillsMemberKey).inerJoin(skills, skillsMemberSkillsKey);
 
 		return builder;
 	}
 
 	public String getAddNewMemberQuery(HttpServletRequest request) {
 		ConditionGroup conditionGroup = getCondGroup(request);
 		conditionGroup.addCondition(getAddMemberSearchRoleCond(request));
 
 		QueryBuilder builder = getQueryBuilderBody(request);
 		builder.where(conditionGroup);
 
 		return builder.toString();
 	}
 
 	public String getQuery(HttpServletRequest request) {
 		ConditionGroup conditionGroup = getCondGroup(request);
 		QueryBuilder builder = getQueryBuilderBody(request);
 
 		conditionGroup.addCondition(getRoleCondGroup(request));
 
 		builder.where(conditionGroup);
 
 		return builder.toString();
 
 	}
 }
