 package com.infinitiessoft.btrs.action;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Out;
 import org.jboss.seam.framework.EntityQuery;
 import org.jboss.seam.log.Log;
 
 import com.infinitiessoft.btrs.enums.ReportTypeEnum;
 import com.infinitiessoft.btrs.enums.RoleEnum;
 import com.infinitiessoft.btrs.enums.StatusEnum;
 import com.infinitiessoft.btrs.exceptions.BtrsRuntimeException;
 import com.infinitiessoft.btrs.model.Report;
 import com.infinitiessoft.btrs.model.Role;
 import com.infinitiessoft.btrs.model.User;
 
 @Name("reportList")
 public class ReportList extends EntityQuery<Report> {
 	
 	private static final long serialVersionUID = 3852316905644428617L;
 	
 	@Logger Log log;
 	
 	@In
 	User currentUser;
 	
 	
 	private static final String LOGIC_OPERATOR_OR = "or";
 	
 	@Out(required = false)
 	private List<Report> preparedReports;
 
 	@Out(required = false)
 	private Map<String, Integer> reportsCounts = new HashMap<String, Integer>();
 	
 	private static final String EJBQL = "select report from Report report";
 
 	private static final String[] RESTRICTIONS = {
 			"report.owner = #{reportList.report.owner}",
 			"report.reviewer = #{reportList.report.reviewer}",
 			"report.currentStatus = #{reportList.report.currentStatus}"};
 	
 
 	private Report report = new Report();
 
 	public ReportList() {
 		setEjbql(EJBQL);
 		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
 		setMaxResults(25);
 	}
 
 	public Report getReport() {
 		return report;
 	}
 
 	public void prepareCounts() {
 		int outgoingCount = 0;
 		int submittedCount = 0;
 		int rejectedCount = 0;
 		int incomingCount = 0;
 		int allCount = 0;
 		
 		// backup
 		String savedOperator = getRestrictionLogicOperator();
 		Integer savedMaxResults = getMaxResults();
 		Integer savedFirstResult = getFirstResult();
 		
 		// get all reports belonging to current user
 		setRestrictionLogicOperator(LOGIC_OPERATOR_OR);
 		setMaxResults(null);
 		setFirstResult(0);
 		report.setOwner(currentUser);
 		report.setReviewer(currentUser);
 		List<Report> usersReports = getResultList();
 		
 		for (Report report : usersReports) {
 			if (currentUser.equals(report.getReviewer())) {
 				if (StatusEnum.SUBMITTED.equals(report.getCurrentStatus())) {
 					incomingCount++;
				}
			} else if (currentUser.equals(report.getOwner())) {
				if (StatusEnum.SUBMITTED.equals(report.getCurrentStatus())) {
					submittedCount++;
					outgoingCount++;
				} else if (StatusEnum.REJECTED.equals(report.getCurrentStatus())) {
					rejectedCount++;
					outgoingCount++;
 				}
 			} else {
 				String message = "Report (id=" + report.getId() + ") doesn't belong to user " + currentUser;
 				log.error(message);
 				throw new BtrsRuntimeException(message);
 			}
 		}
 		allCount = usersReports.size();
 		
 		reportsCounts.put(ReportTypeEnum.OUTGOING.getLabel(), outgoingCount);
 		reportsCounts.put(ReportTypeEnum.SUBMITTED.getLabel(), submittedCount);
 		reportsCounts.put(ReportTypeEnum.REJECTED.getLabel(), rejectedCount);
 		reportsCounts.put(ReportTypeEnum.INCOMING.getLabel(), incomingCount);
 		reportsCounts.put(ReportTypeEnum.ALL.getLabel(), allCount);
 		
 		log.debug("Reports Counts are: {0}", reportsCounts);
 		
 		// reset
 		setRestrictionLogicOperator(savedOperator);
 		setMaxResults(savedMaxResults);
 		setFirstResult(savedFirstResult);
 		report = new Report();
 	}
 
 	public void prepareList(String type) {
 
 		if (type == null || type.trim().isEmpty()) {
 			type = determineType();
 		}
 		
 		if (ReportTypeEnum.OUTGOING.name().equalsIgnoreCase(type)) {
 			report.setOwner(currentUser);
 			report.setCurrentStatus(StatusEnum.APPROVED);
 			List<String> restrictions = new ArrayList<String>(Arrays.asList(RESTRICTIONS));
 			restrictions.set(2, "report.currentStatus <> #{reportList.report.currentStatus}");
 			setRestrictionExpressionStrings(restrictions);
 		} else if (ReportTypeEnum.INCOMING.name().equalsIgnoreCase(type)) {
 			report.setReviewer(currentUser);
 			report.setCurrentStatus(StatusEnum.SUBMITTED);
 		} else if (ReportTypeEnum.SUBMITTED.name().equalsIgnoreCase(type)) {
 			report.setOwner(currentUser);
 			report.setCurrentStatus(StatusEnum.SUBMITTED);
 		} else if (ReportTypeEnum.REJECTED.name().equalsIgnoreCase(type)) {
 			report.setOwner(currentUser);
 			report.setCurrentStatus(StatusEnum.REJECTED);
 		} else { // ALL
 			setRestrictionLogicOperator(LOGIC_OPERATOR_OR);
 			report.setOwner(currentUser);
 			report.setReviewer(currentUser);
 		}
 		preparedReports = getResultList();
 		log.debug("List of prepared Reports is: {0}.", preparedReports);
 	}
 	
 	private String determineType() {
 		for (Role role : currentUser.getRoles()) {
 			if (RoleEnum.ACCOUNTANT.equals(role.getValue())) {
 				return ReportTypeEnum.INCOMING.name().toLowerCase();
 			}
 		}
 		return ReportTypeEnum.OUTGOING.name().toLowerCase();
 	}
 	
 
 	
 }
