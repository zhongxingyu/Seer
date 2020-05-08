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
 
 public class ProjectSearchParser {
 	private Table project;
 
 	public ProjectSearchParser() {
 		project = new Table(TableName.PROJECT);
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
 
 	private Condition getCustomerCondition(HttpServletRequest request) {
 		String customer = request.getParameter("customer");
 
 		return new Condition(project.getColumn(FieldName.CUSTOMER), Operator.EQUALS, customer, true);
 	}
 
 	private ConditionGroup getCreatedCondGroup(HttpServletRequest request) {
 		String createdLowerBound = request.getParameter("createdLowerBound");
 		String createdUpperBound = request.getParameter("createdUpperBound");
 
 		return getBoundedConditionGroup(project.getColumn(FieldName.CREATED), createdLowerBound, createdUpperBound);
 	}
 
 	private ConditionGroup getEstimatedCompletionCondGroup(HttpServletRequest request) {
 		String estimatedLowerBound = request.getParameter("estimatedLowerBound");
 		String estimatedUpperBound = request.getParameter("estimatedUpperBound");
 
 		return getBoundedConditionGroup(project.getColumn(FieldName.ESTIMATED_COMPLETION), estimatedLowerBound, estimatedUpperBound);
 	}
 
 	private ConditionGroup getCompletedDateCondGroup(HttpServletRequest request) {
 		String completedLowerBound = request.getParameter("completedLowerBound");
 		String completedUpperBound = request.getParameter("completedUpperBound");
 
 		return getBoundedConditionGroup(project.getColumn(FieldName.COMPLETED), completedLowerBound, completedUpperBound);
 	}
 
 	private ConditionGroup getStatusesCondGroup(HttpServletRequest request) {
 
		String status = request.getParameter("status");
 		ConditionGroup condGroup = new ConditionGroup();
 		condGroup.setOperator(Operator.OR);
 
		if ("all".equals(status)) {

			Condition openCond = new Condition(project.getColumn(FieldName.STATUS), Operator.EQUALS, "open", true);
			Condition closedCond = new Condition(project.getColumn(FieldName.STATUS), Operator.EQUALS, "closed", true);

			condGroup.addCondition(openCond);
			condGroup.addCondition(closedCond);

		} else {
			Condition chosenCond = new Condition(project.getColumn(FieldName.STATUS), Operator.EQUALS, status, true);
			condGroup.addCondition(chosenCond);
 		}
 
 		return condGroup;
 	}
 
 	public String getQuery(HttpServletRequest request) {
 
 		ConditionGroup conditionGroup = new ConditionGroup();
 		conditionGroup.setOperator(Operator.AND);
 
 		conditionGroup.addCondition(getCustomerCondition(request));
 		conditionGroup.addCondition(getCreatedCondGroup(request));
 		conditionGroup.addCondition(getEstimatedCompletionCondGroup(request));
 		conditionGroup.addCondition(getCompletedDateCondGroup(request));
 		conditionGroup.addCondition(getStatusesCondGroup(request));
 
 		QueryBuilder builder = new QueryBuilder();
 		builder.setDistinct(true);

 		builder.select(project.getWildcard()).from(project).where(conditionGroup);
 
 		return builder.toString();
 	}
 }
