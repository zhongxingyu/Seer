 /**********************************************************************************
  *
  * Copyright (c) 2005, 2006 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.evaluation.logic.impl.scheduling;
 
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.api.app.scheduler.DelayedInvocation;
 import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
 import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
 import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
 import org.sakaiproject.evaluation.logic.EvalExternalLogic;
 import org.sakaiproject.evaluation.logic.EvalSettings;
 import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
 import org.sakaiproject.evaluation.logic.utils.EvalUtils;
 import org.sakaiproject.evaluation.model.EvalEvaluation;
 import org.sakaiproject.evaluation.model.constant.EvalConstants;
 import org.sakaiproject.time.api.TimeService;
 
 /**
  * Handle job scheduling related to EvalEvaluation state transitions.</br>
  * Dates that have not passed may be changed, which might then require
  * rescheduling a job to keep jobs and EvalEvaluation dates in sync.
  * 
  * @author rwellis
  *
  */
 public class EvalJobLogicImpl implements EvalJobLogic {
 
 	private static Log log = LogFactory.getLog(EvalJobLogicImpl.class);
 	
 	//the component scheduled by the ScheduledInvocationManager
 	private final String COMPONENT_ID = "org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation";
 
 	private final String SEPARATOR = "/";           // max-32:12345678901234567890123456789012
 	private final String EVENT_EVAL_START                  = "evaluation.state.start";
 	private final String EVENT_EVAL_DUE                    = "evaluation.state.due";
 	private final String EVENT_EVAL_STOP                   = "evaluation.state.stop";
 	private final String EVENT_EVAL_VIEWABLE               = "evaluation.state.viewable";
 	private final String EVENT_EVAL_VIEWABLE_INSTRUCTORS   = "evaluation.state.viewable.inst";
 	private final String EVENT_EVAL_VIEWABLE_STUDENTS      = "evaluation.state.viewable.stud";
 	private final String EVENT_EMAIL_REMINDER              = "evaluation.email.reminder";
 
 	//TODO jleasia: track events
 	
 	private EvalEmailsLogic emails;
 	public void setEmails(EvalEmailsLogic emails) {
 		this.emails = emails;
 	}
 	private EvalEvaluationsLogic evalEvaluationsLogic;
 	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
 		this.evalEvaluationsLogic = evalEvaluationsLogic;
 	}
 	private EvalExternalLogic externalLogic;
 	public void setExternalLogic(EvalExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 	private ScheduledInvocationManager scheduledInvocationManager;
 	public void setScheduledInvocationManager(ScheduledInvocationManager scheduledInvocationManager) {
 		this.scheduledInvocationManager = scheduledInvocationManager;
 	}
 	private EvalSettings settings;
 	public void setSettings(EvalSettings settings) {
 		this.settings = settings;
 	}
 	private TimeService timeService;
 	public void setTimeService(TimeService timeService) {
 		this.timeService = timeService;
 	}
 	
 	public void init()  {
 		log.debug("EvalJobLogicImpl.init()");
 	}
 	
 	public EvalJobLogicImpl() {
 	}
 	
 	/**
 	 * Compare the date when a job will be invoked with the 
 	 * EvalEvaluation date to see if the job needs to be rescheduled.
 	 * 
 	 * @param eval the EvalEvaluation
 	 * @param jobType the type of job (refer to EvalConstants)
 	 * @param  correctDate the date when the job should be invoked
 	 * 
 	 */
 	private void checkInvocationDate(EvalEvaluation eval, String jobType, Date correctDate) {
 		
 		if(eval == null || jobType == null || correctDate == null) return;
 		
 		if(log.isDebugEnabled())
 			log.debug("EvalJobLogicImpl.checkInvocationDate(" + eval.getId() + "," + jobType + "," + correctDate);
 		
 		/* We don't reschedule reminders, because the active date won't change 
 		 * once an evaluation becomes active, and therefore reminder dates also 
 		 * remain fixed. We might remove a reminder if the due date is moved
 		 * forward or reminders are disabled by setting reminder days to 0
 		 */
 		if(EvalConstants.JOB_TYPE_REMINDER.equals(jobType)) return;
 		
 		//get the delayed invocation, a pea with .Date Date
 		String id = eval.getId().toString();
 		String opaqueContext = id + SEPARATOR + jobType;
 		DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 		
 		//if there are no invocations, return
 		if(invocations == null || invocations.length == 0) {
 			return;
 		}
 		else if(invocations.length == 1) {
 			//we expect at most one delayed invocation matching componentId and opaqueContext
 			
 			//if the dates differ
 			if(invocations[0].date.compareTo(correctDate) != 0) {
 				
 				//remove the old invocation
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[0].uuid);
 				if(log.isDebugEnabled())
 					log.debug("EvalJobLogicImpl.checkInvocationDate remove the old invocation " + invocations[0].uuid + "," + invocations[0].contextId + "," + invocations[0].date);
 				
 				//and schedule a new invocation
 				scheduledInvocationManager.createDelayedInvocation(timeService.newTime(correctDate.getTime()), COMPONENT_ID, opaqueContext);
 				if(log.isDebugEnabled())
 					log.debug("EvalJobLogicImpl.checkInvocationDate and schedule a new invocation " + correctDate + "," + COMPONENT_ID + "," + opaqueContext + ")");
 				
 				//the due date was changed, so reminder might need to be removed
 				if(EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
 					fixReminder(eval.getId());
 				}
 			}
 		}
 		else {
 			log.warn(this + ".checkInvocationDate: multiple delayed invocations of componentId '" + COMPONENT_ID + "', opaqueContext '" + opaqueContext +"'");
 		}
 	}
 	
 	/**
 	 * Remove reminder if the due date now comes before the reminder or reminder days was changed to 0
 	 * 
 	 * @param evalId the EvalEvaluation id
 	 */
 	private void fixReminder(Long evaluationId) {
 		EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
 		String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
 		DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 		if(invocations != null && invocations.length == 1) {
 			DelayedInvocation reminder = invocations[0];
 			Date reminderAt = reminder.date;
 			if(eval.getReminderDays().intValue() == 0 || reminderAt.after(eval.getDueDate())) {
 				//remove reminder
 				scheduledInvocationManager.deleteDelayedInvocation(reminder.uuid);
 				if(log.isDebugEnabled())
 					log.debug("EvalJobLogicImpl.fixReminders remove reminder after the due date " + reminder.uuid + "," + reminder.contextId + "," + reminder.date);
 			}
 		}
 	}
 	
 	/**
 	 * Remove all ScheduledInvocationCammand jobs for an EvalEvaluation
 	 * 
 	 * @param evalId the EvalEvaluation id
 	 */
 	public void removeScheduledInvocations(Long evalId) {
 		
 		if(evalId == null) return;
 		String userId = externalLogic.getCurrentUserId();
 		if(evalEvaluationsLogic.canRemoveEvaluation(userId, evalId)) {
 		
 			//TODO be selective based on the state of the EvalEvaluation when deleted
 			String opaqueContext = null;
 			DelayedInvocation[] invocations = null;
 			
 			opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_ACTIVE;
 			invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 			for(int i = 0; i < invocations.length; i++) {
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
 			}
 			opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_DUE;
 			invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 			for(int i = 0; i < invocations.length; i++) {
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
 			}
 			opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_CLOSED;
 			invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 			for(int i = 0; i < invocations.length; i++) {
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
 			}
 			opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
 			invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 			for(int i = 0; i < invocations.length; i++) {
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
 			}
 			opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_VIEWABLE;
 			invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 			for(int i = 0; i < invocations.length; i++) {
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
 			}
 			opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS;
 			invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 			for(int i = 0; i < invocations.length; i++) {
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
 			}
 			opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS;
 			invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
 			for(int i = 0; i < invocations.length; i++) {
 				scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
 			}
 		}
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processNewEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
 	 */
 	public void processNewEvaluation(EvalEvaluation eval) {
 		
 		if(eval == null)
 			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation was null.");
 		
 		if(log.isDebugEnabled())
 			log.debug("EvalJobLogicImpl.processNewEvaluation(" + eval.getId() + ")");
 		
 		String state = EvalUtils.getEvaluationState(eval);
 		if(state == null) 
 			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation state was null.");
 		
 		//send created email if instructor can add questions or opt-in or opt-out
 		int instructorAdds = ((Integer)settings.get(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER)).intValue();
 		if(instructorAdds > 0 || !eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED)) {
 			
 			/* Note: email cannot be sent at this point, because it precedes saveAssignGroup,
 			 * so we schedule email for ten minutes from now, also giving instructor ten minutes 
 			 * to delete the evaluation and its notification
 			 */
 			long runAt = new Date().getTime() + (1000 * 60 * 10);
 			scheduleJob(eval.getId(), new Date(runAt), EvalConstants.JOB_TYPE_CREATED);
 		}
 		scheduleJob(eval.getId(), eval.getStartDate(), EvalConstants.JOB_TYPE_ACTIVE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processEvaluationChange(org.sakaiproject.evaluation.model.EvalEvaluation)
 	 */
 	public void processEvaluationChange(EvalEvaluation eval) {
 		
 		if(log.isDebugEnabled())
 			log.debug("EvalJobLogicImpl.processEvaluationChange(" + eval.getId() + ")");
 		
 		//checks
 		if(eval == null) return;
 		String state = EvalUtils.getEvaluationState(eval);
 		if(EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
 			
 			if(log.isWarnEnabled())
 				log.warn(this + ".processEvaluationChange(Long "+  eval.getId().toString() + ") for " + eval.getTitle()  + ". Evaluation in UNKNOWN state");
 			throw new RuntimeException("Evaluation '"+eval.getTitle()+"' in UNKNOWN state");
 		}
 		try {
 			if(EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState())) {
 
 				//make sure scheduleActive job invocation date matches EvalEvaluation start date
 				checkInvocationDate(eval, EvalConstants.JOB_TYPE_ACTIVE, eval.getStartDate());
 			}
 			else if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
 
 				/* make sure scheduleDue job invocation start date matches EvalEaluation due date
 				 * and moving the due date is reflected in reminder
 				 */
 				checkInvocationDate(eval, EvalConstants.JOB_TYPE_DUE, eval.getDueDate());
 			}
 			else if (EvalConstants.EVALUATION_STATE_DUE.equals(eval.getState())) {
 
 				//make sure scheduleClosed job invocation start date matches EvalEvaluation stop date
 				checkInvocationDate(eval, EvalConstants.JOB_TYPE_CLOSED, eval.getStopDate());
 			}
 			else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(eval.getState())) {
 
 				//make sure scheduleView job invocation start date matches EvalEvaluation view date 
 				checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE, eval.getViewDate());
 				
 				//make sure scheduleView By Instructors job invocation start date matches EvalEvaluation instructor's date
 				checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS, eval.getInstructorsDate());
 				
 				//make sure scheduleView By Students job invocation start date matches EvalEvaluation student's date
 				checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS, eval.getStudentsDate());
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			if(log.isWarnEnabled())
 				log.warn(this + ".processEvaluationChange("+  eval.getId() + ") for '" + eval.getTitle()  + "' " + e);
 			throw new RuntimeException("Evaluation '" + eval.getTitle() + "' " + e);
 		}
 	}
 
 	/**
 	 * Schedule a job using the ScheduledInvocationManager.</br>
 	 * "When" is specified by runDate, "what" by componentId, and "what to do"
 	 * by opaqueContext. OpaqueContext contains an EvalEvaluationId 
 	 * and a jobType from EvalConstants, which is used to keep track of
 	 * pending jobs and reschedule or remove jobs when necessary.
 	 *
 	 *@param evaluationId the id of an EvalEvaluation
 	 *@param runDate the Date when the command should be invoked
 	 *@param jobType the type of job, from EvalConstants
 	 */
 	private void scheduleJob(Long evaluationId, Date runDate, String jobType) {
 		if(evaluationId == null || runDate == null || jobType == null) {
 			if(log.isErrorEnabled())
 				log.error(this + ".scheduleJob null parameter");
 			//TODO: throw exception
 			return;
 		}
 		if(log.isDebugEnabled())
 			log.debug("EvalJobLogicImpl.scheduleJob(" + evaluationId + "," + runDate + "," + jobType + ")");
 		try {
 			String opaqueContext = evaluationId.toString() + SEPARATOR + jobType;
 			scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runDate.getTime()), COMPONENT_ID, opaqueContext);
 			if(log.isDebugEnabled())
 				log.debug("EvalJobLogicImpl.scheduleJob scheduledInvocationManager.createDelayedInvocation(" + 
 						timeService.newTime(runDate.getTime()) + "," + COMPONENT_ID + "," +  opaqueContext + ")");
 		}
 		catch(Exception e) {
 			log.error(this + ".scheduleJob(" + evaluationId + "," + runDate.toString() + "," + jobType + ") " + e);
 		}
 	}
 	
 	/**
 	 * Schedule reminders to be run under the ScheduledInvocationManager.</br>
 	 * If there is time to send a reminder before the due date, schedule it.
 	 * 
 	 * @param evaluationId the EvalEvaluation id
 	 */
 	private void scheduleReminder(Long evaluationId) {
 		
 		try {
 			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
 			String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
 			
 			//schedule reminders at selected intervals while the evaluation is available
			long start = eval.getStartDate().getTime();
 			long due = eval.getDueDate().getTime();
 			long available = due - start;
 			long interval = 1000 * 60 * 60 * 24 * eval.getReminderDays().intValue();
 			if(interval != 0 && available > interval) {
 				start = start + interval;
 				scheduledInvocationManager.createDelayedInvocation(timeService.newTime(start), COMPONENT_ID, opaqueContext);
 				if(log.isDebugEnabled())
 					log.debug("EvalJobLogicImpl.scheduleReminders(" + evaluationId + ") - scheduledInvocationManager.createDelayedInvocation( " + 
 							timeService.newTime(start) + "," + 	COMPONENT_ID + "," + opaqueContext);
 			}
 		}
 		catch(Exception e) {
 			log.error(this + ".scheduleReminder(" + evaluationId + ") " + e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#jobAction(java.lang.Long)
 	 */
 	public void jobAction(Long evaluationId, String jobType) {
 		
 		/* Note: If interactive response time is too slow waiting for
 		 * mail to be sent, sending mail could be done as another type
 		 * of job run by the scheduler in a separate thread.
 		 */
 		if(log.isDebugEnabled())
 			log.debug("EvalJobLogicImpl.jobAction(" + evaluationId + "," + jobType + ")");
 		try
 		{
 			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
 			
 			//fix EvalEvaluation state
 			String state = evalEvaluationsLogic.getEvaluationState(evaluationId);
 			if(log.isDebugEnabled())
 				log.debug("evaluation state " + state + " saved");
 			
 			//dispatch to send email and/or schedule jobs based on jobType
 			if(EvalConstants.JOB_TYPE_CREATED.equals(jobType)) {
 				//if opt-in, opt-out, or questions addable, notify instructors
 				sendCreatedEmail(evaluationId);
 			}
 			else if(EvalConstants.JOB_TYPE_ACTIVE.equals(jobType)) {
 				externalLogic.registerEntityEvent(EVENT_EVAL_START, eval);
 				sendAvailableEmail(evaluationId);
 				scheduleJob(eval.getId(), eval.getDueDate(), EvalConstants.JOB_TYPE_DUE);
 				if(eval.getReminderDays().intValue() != 0)
 					scheduleReminder(eval.getId());
 			}
 			else if(EvalConstants.JOB_TYPE_REMINDER.equals(jobType)) {
 				if(eval.getReminderDays().intValue() != 0) {
 					if(eval.getDueDate().after(new Date())) {
 						sendReminderEmail(evaluationId);
 						scheduleReminder(evaluationId);
 					}
 				}
 			}
 			else if(EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
 				externalLogic.registerEntityEvent(EVENT_EVAL_DUE, eval);
 				if(log.isDebugEnabled())
 					log.debug("EvalJobLogicImpl.jobAction scheduleJob(" + eval.getId() + "," + eval.getStopDate() + "," + EvalConstants.JOB_TYPE_CLOSED + ")");
 				scheduleJob(eval.getId(), eval.getStopDate(), EvalConstants.JOB_TYPE_CLOSED);
 			}
 			else if(EvalConstants.JOB_TYPE_CLOSED.equals(jobType)) {
 				externalLogic.registerEntityEvent(EVENT_EVAL_STOP, eval);
 				//schedule results viewable by owner - admin notification
 				scheduleJob(eval.getId(), eval.getViewDate(), EvalConstants.JOB_TYPE_VIEWABLE);
 				if(! eval.getResultsPrivate().booleanValue()) {
 					if(eval.getInstructorsDate() != null) {
 						Date instructorViewDate = eval.getInstructorsDate();
 						//schedule results viewable by instructors notification
 						scheduleJob(eval.getId(), instructorViewDate, EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS);
 					}
 					if(eval.getStudentsDate() != null) {
 						Date studentViewDate = eval.getStudentsDate();
 						//schedule results viewable by students notification
 						scheduleJob(eval.getId(), studentViewDate, EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS);
 					}
 				}
 			}
 			else if(EvalConstants.JOB_TYPE_VIEWABLE.equals(jobType))  {
 				externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE, eval);
 				//send results viewable notification to owner if private, or all if not
 				sendViewableEmail(evaluationId, jobType, eval.getResultsPrivate());
 			}
 			else if(EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS.equals(jobType))  {
 				externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_INSTRUCTORS, eval);
 				//send results viewable notification to owner if private, or all if not
 				sendViewableEmail(evaluationId, jobType, eval.getResultsPrivate());
 			}
 			else if(EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS.equals(jobType))  {
 				externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_STUDENTS, eval);
 				//send results viewable notification to owner if private, or all if not
 				sendViewableEmail(evaluationId, jobType, eval.getResultsPrivate());
 			}
 		}
 		catch(Exception e) {
 			log.error("jobAction died horribly:" + e.getMessage(), e);
 			throw new RuntimeException(e); // die horribly, as it should -AZ
 		}
 	}
 
 	/**
 	 * Send email to evaluation participants that an evaluation 
 	 * is available for taking by clicking the contained URL
 	 * 
 	 * @param evalId the EvalEvaluation id
 	 */
 	public void sendAvailableEmail(Long evalId) {
 		//For now, we always want to include the evaluatees in the evaluations
 		boolean includeEvaluatees = true;
 		try {
 			String[] sentMessages = emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
 			if(log.isDebugEnabled())
 				log.debug("EvalJobLogicImpl.sendAvailableEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
 		}
 		catch(Exception e) {
 			log.error(this + ".sendAvailableEmail(" + evalId + ")" + e);
 		}
 	}
 
 	/**
 	 *  Send email that an evaluation has been created</br>
 	 *  
 	 * @param evalId the EvalEvaluation id
 	 */
 	public void sendCreatedEmail(Long evalId) {
 		boolean includeOwner = true;
 		try {
 			String[] sentMessages = emails.sendEvalCreatedNotifications(evalId, includeOwner);
 			if(log.isDebugEnabled())
 				log.debug("EvalJobLogicImpl.sendCreatedEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
 		}
 		catch(Exception e) {
 			log.error(this + ".sendCreatedEmail(" + evalId + ")" + e);
 		}
 	}
 
 	/**
 	 * Send a reminder that an evaluation is available 
 	 * for taking to those who have not responded
 	 * 
 	 * @param evalId the EvalEvaluation id
 	 */
 	public void sendReminderEmail(Long evalId) {
 		try {
 			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evalId);
 			externalLogic.registerEntityEvent(EVENT_EMAIL_REMINDER, eval);
 			String includeConstant = EvalConstants.EMAIL_INCLUDE_ALL;
 			String[] sentMessages = emails.sendEvalReminderNotifications(evalId, includeConstant);
 			if(log.isDebugEnabled())
 				log.debug("EvalJobLogicImpl.sendReminderEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
 		}
 		catch(Exception e) {
 			log.error(this + ".sendReminderEmail(" + evalId + ")" + e);
 		}
 	}
 
 	/**
 	 * Send email that the results of an evaluation may be viewed now.</br>
 	 * Notification may be sent to owner only, instructors and students together or
 	 * separately.
 	 * 
 	 * @param evalId the EvalEvaluation id
 	 * @param the job type fom EvalConstants
 	 */
 	public void sendViewableEmail(Long evalId, String jobType, Boolean resultsPrivate) {
 		/*
 		 * TODO when booleans below are set dynamically, replace the use of job type to distinguish
 		 * recipients with the setting of these parameters before calling emails.sendEvalResultsNotifications().
 		 * Then one job type JOB_TYPE_VIEWABLE can be scheduled as needed.
 		 */
 		boolean includeEvaluatees = true;
 		boolean includeAdmins = true;
 		
 		try {
 		
 			//if results are private, only send notification to owner
 			if(resultsPrivate.booleanValue()) {
 				includeEvaluatees = false;
 				includeAdmins = false;
 				String[] sentMessages = emails.sendEvalResultsNotifications(jobType, evalId, includeEvaluatees, includeAdmins);
 				if(log.isDebugEnabled())
 					log.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType + ", resultsPrivate " + resultsPrivate + ")");
 			}
 			else {
 				String[] sentMessages = emails.sendEvalResultsNotifications(jobType, evalId, includeEvaluatees, includeAdmins);
 				if(log.isDebugEnabled())
 					log.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType + ", resultsPrivate " + resultsPrivate + ")");
 			}
 		}
 		catch(Exception e) {
 			log.error(this + ".sendViewableEmail(" + evalId + "," +  jobType + "," + includeAdmins + ")" + e);
 		}
 	}
 }
