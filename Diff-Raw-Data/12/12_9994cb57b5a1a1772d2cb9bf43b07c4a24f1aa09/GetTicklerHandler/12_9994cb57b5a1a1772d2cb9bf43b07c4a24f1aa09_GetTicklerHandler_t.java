 /*
  * Copyright 2010 Ian Hilt
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.eastway.echarts.server;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.eastway.echarts.domain.AssignmentImpl;
 import org.eastway.echarts.shared.Assignment;
 import org.eastway.echarts.shared.DbException;
 import org.eastway.echarts.shared.DueDateStatus;
 import org.eastway.echarts.shared.GetTickler;
 import org.eastway.echarts.shared.GetTicklerResult;
 import org.eastway.echarts.shared.Patient;
 import org.eastway.echarts.shared.SessionExpiredException;
 import org.eastway.echarts.shared.Tickler;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import net.customware.gwt.dispatch.server.ActionHandler;
 import net.customware.gwt.dispatch.server.ExecutionContext;
 import net.customware.gwt.dispatch.shared.ActionException;
 
 public class GetTicklerHandler implements ActionHandler<GetTickler, GetTicklerResult> {
 
	private String staffId = "";

 	@Override
 	public GetTicklerResult execute(GetTickler action, ExecutionContext context)
 			throws ActionException {
 		ServiceUtil util = new ServiceUtil();
 		try {
 			util.checkSessionExpire(action.getSessionId());
 		} catch (SessionExpiredException e) {
 			throw new ActionException(e.getMessage());
 		} catch (DbException e) {
 			throw new ActionException("Database error");
 		}
 
 		EntityManager em = EchartsEntityManagerFactory.getEntityManagerFactory().createEntityManager();
 		try {
 			List<AssignmentImpl> assignments = em.createQuery(
 					"SELECT a From AssignmentImpl a Where a.staff = '" + action.getStaffId() + "' And a.disposition = 'Open' And a.service Like 'S%' Order By a.patient.lastName ASC, a.patient.firstName ASC, a.orderDate DESC", AssignmentImpl.class)
 					.getResultList();
			staffId = action.getStaffId();
 			return new GetTicklerResult(setDates(assignments));
 		} finally {
 			em.close();
 		}
 	}
 
 	@Override
 	public Class<GetTickler> getActionType() {
 		return GetTickler.class;
 	}
 
 	@Override
 	public synchronized void rollback(GetTickler list, GetTicklerResult result, ExecutionContext context) throws ActionException {
 		
 	}
 
 	private long NOW = new Date().getTime();
 	private long day = 24L * 3600L * 1000L;
 	private long YEAR = 365L * day;
 	private long THIRTY_DAYS = 30L * day;
 	private long NINETY_DAYS = 90L * day;
 	private long ONE_HUNDRED_EIGHTY_DAYS = 180L * day;
 
 	private List<Tickler> setDates(List<AssignmentImpl> assignments) {
 		List<Tickler> tickler = new ArrayList<Tickler>();
 		for (AssignmentImpl assignment : assignments) {
 			Tickler result = new Tickler();
 			Patient patient = assignment.getPatient();
 			result.setName(assignment.getPatient().getName());
 			result.setCaseNumber(patient.getCaseNumber());
 	
 			if (patient.getHipaaDateCompleted() != null)
 				result.setHipaaDateCompleted(formatDueDate(patient.getHipaaDateCompleted().getTime()));
 	
 			long ispDueDate = 0L;
 			long ispDateCompleted = 0L;
 			if (patient.getIspDateCompleted() != null) {
 				ispDueDate = patient.getIspDateCompleted().getTime() + YEAR;
 				ispDateCompleted = patient.getIspDateCompleted().getTime();
 			}
 			result.setIspDueDate(formatDueDate(ispDueDate), getDueDateStatus(ispDueDate));
 	
 			long ispReviewDueDate = getIspReviewDueDate(ispDueDate, ispDateCompleted, assignment, patient);
 			result.setIspReviewDueDate(formatDueDate(ispReviewDueDate), getDueDateStatus(ispReviewDueDate));
 	
 			long healthHistoryDueDate = 0L;
 			if (patient.getHealthHistoryDateCompleted() != null)
 				healthHistoryDueDate = patient.getHealthHistoryDateCompleted().getTime() + YEAR;
 			result.setHealthHistoryDueDate(formatDueDate(healthHistoryDueDate), getDueDateStatus(healthHistoryDueDate));
 	
 			long diagnosticAssessmentUpdateDueDate = 0L;
 			if (patient.getDiagnosticAssessmentDateCompleted() != null) {
 				if (assignment.getService().matches("Pgm021")
 						|| assignment.getService().matches("Pgm022")
 						|| assignment.getService().matches("Pgm023")
 						|| assignment.getService().matches("Pgm030"))
 					diagnosticAssessmentUpdateDueDate = patient.getDiagnosticAssessmentDateCompleted().getTime() + YEAR;
 				else
 					diagnosticAssessmentUpdateDueDate = patient.getDiagnosticAssessmentDateCompleted().getTime() + (2L * YEAR);
 			}
 			result.setDiagnosticAssessmentUpdate(formatDueDate(diagnosticAssessmentUpdateDueDate), getDueDateStatus(diagnosticAssessmentUpdateDueDate));
 	
 			long financialDueDate = 0L;
 			if (patient.getFinancialDateCompleted() != null) {
 				if (patient.isTitleTwenty())
 					financialDueDate = patient.getFinancialDateCompleted().getTime() + ONE_HUNDRED_EIGHTY_DAYS;
 				else
 					financialDueDate = patient.getFinancialDateCompleted().getTime() + YEAR;
 			}
 			result.setFinancialDueDate(formatDueDate(financialDueDate), getDueDateStatus(financialDueDate));
 	
 			long oocDueDate = 0L;
 			if (patient.getOutcomesConsumerDateCompleted() != null)
 				oocDueDate = patient.getOutcomesConsumerDateCompleted().getTime() + YEAR;
 			result.setOoc(formatDueDate(oocDueDate), getDueDateStatus(oocDueDate));
 			result.setService(assignment.getService());
 			result.setStaffName(assignment.getStaffName());
 			tickler.add(result);
 		}
 		return tickler;
 	}
 
 	private long getIspReviewDueDate(long ispDueDate, long ispDateCompleted, Assignment assignment, Patient patient) {
 		long ispReviewDueDate = 0L;
 		if (getDueDateStatus(ispDueDate) == DueDateStatus.OVERDUE) {
 			return ispDueDate;
 		} else {
			boolean aodStatus = (assignment.getDemographics().isAlcoholDrug()==null?false:assignment.getDemographics().isAlcoholDrug())&&(staffId=="5542"||staffId=="5396");
 			if (patient.getIspReviewDateCompleted() != null) {
 				if (assignment.getService().matches("Pgm076")
						|| aodStatus
 						|| assignment.getService().matches("Pgm021")
 						|| assignment.getService().matches("Pgm022")
 						|| assignment.getService().matches("Pgm023")
 						|| assignment.getService().matches("Pgm030")) {
 					ispReviewDueDate = patient.getIspReviewDateCompleted().getTime() + NINETY_DAYS;
 				} else {
 					ispReviewDueDate = patient.getIspReviewDateCompleted().getTime() + ONE_HUNDRED_EIGHTY_DAYS;
 				}
 				if (ispReviewDueDate > ispDueDate)
 					return ispDueDate;
 			} else {
				if (assignment.getService().matches("Pgm076") || aodStatus) {
 					ispReviewDueDate = ispDateCompleted + NINETY_DAYS;
 				} else {
 					ispReviewDueDate = ispDateCompleted + ONE_HUNDRED_EIGHTY_DAYS;
 				}
 				return ispReviewDueDate;
 			}
 		}
 		return ispReviewDueDate;
 	}
 
 	private int getDueDateStatus(long dueDate) {
 		if (dueDate == 0L)
 			return DueDateStatus.NO_DATA;
 		if ((dueDate - NOW) > THIRTY_DAYS)
 			return DueDateStatus.COMPLIANT;
 		else if (dueDate > NOW)
 			return DueDateStatus.DUE_IN_THIRTY_DAYS;
 		else
 			return DueDateStatus.OVERDUE;
 	}
 
 
 	private String formatDueDate(long dueDate) {
 		String dateFormat = "M/d/y";
 		DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);
 		return new DateTime(dueDate).toString(formatter);
 	}
 }
