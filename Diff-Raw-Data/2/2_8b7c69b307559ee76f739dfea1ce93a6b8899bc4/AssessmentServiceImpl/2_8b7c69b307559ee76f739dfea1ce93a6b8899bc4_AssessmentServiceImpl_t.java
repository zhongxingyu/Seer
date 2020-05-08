 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
  * 
  * Portions completed before September 1, 2008
  * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.mneme.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.mneme.api.Assessment;
 import org.etudes.mneme.api.AssessmentPermissionException;
 import org.etudes.mneme.api.AssessmentPolicyException;
 import org.etudes.mneme.api.AssessmentService;
 import org.etudes.mneme.api.AssessmentType;
 import org.etudes.mneme.api.AttachmentService;
 import org.etudes.mneme.api.GradesRejectsAssessmentException;
 import org.etudes.mneme.api.GradesService;
 import org.etudes.mneme.api.MnemeService;
 import org.etudes.mneme.api.Part;
 import org.etudes.mneme.api.PartDetail;
 import org.etudes.mneme.api.Pool;
 import org.etudes.mneme.api.PoolService;
 import org.etudes.mneme.api.Question;
 import org.etudes.mneme.api.QuestionService;
 import org.etudes.mneme.api.ReviewTiming;
 import org.etudes.mneme.api.SecurityService;
 import org.etudes.mneme.api.SubmissionService;
 import org.etudes.util.api.Translation;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.event.api.EventTrackingService;
 import org.sakaiproject.memory.api.Cache;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 /**
  * AssessmentServiceImpl implements AssessmentService.
  */
 public class AssessmentServiceImpl implements AssessmentService
 {
 	/** Our logger. */
 	private static Log M_log = LogFactory.getLog(AssessmentServiceImpl.class);
 
 	/** A cache of assessments. */
 	protected Cache assessmentCache = null;
 
 	/** Dependency: AttachmentService */
 	protected AttachmentService attachmentService = null;
 
 	/** Dependency: EventTrackingService */
 	protected EventTrackingService eventTrackingService = null;
 
 	/** Dependency: GradesService */
 	protected GradesService gradesService = null;
 
 	/** Dependency: PoolService */
 	protected PoolService poolService = null;
 
 	/** Dependency: QuestionService */
 	protected QuestionService questionService = null;
 
 	/** Dependency: SecurityService */
 	protected SecurityService securityService = null;
 
 	/** Dependency: SessionManager */
 	protected SessionManager sessionManager = null;
 
 	/** Dependency: SqlService */
 	protected SqlService sqlService = null;
 
 	/** Storage handler. */
 	protected AssessmentStorage storage = null;
 
 	/** Storage option map key for the option to use. */
 	protected String storageKey = null;
 
 	/** Map of registered PoolStorage options. */
 	protected Map<String, AssessmentStorage> storgeOptions;
 
 	/** Dependency: SubmissionService */
 	protected SubmissionServiceImpl submissionService = null;
 
 	/** Dependency: ThreadLocalManager. */
 	protected ThreadLocalManager threadLocalManager = null;
 
 	protected UserDirectoryService userDirectoryService = null;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Boolean allowEditAssessment(Assessment assessment)
 	{
 		if (assessment == null) throw new IllegalArgumentException();
 		String userId = sessionManager.getCurrentSessionUserId();
 
 		if (M_log.isDebugEnabled()) M_log.debug("allowEditAssessment: " + assessment.getId() + ": " + userId);
 
 		// check permission - user must have MANAGE_PERMISSION in the context
 		boolean ok = securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, assessment.getContext());
 
 		return ok;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Boolean allowGuest(String context)
 	{
 		if (context == null) throw new IllegalArgumentException();
 		String userId = sessionManager.getCurrentSessionUserId();
 
 		if (M_log.isDebugEnabled()) M_log.debug("allowGuest: " + context + ": " + userId);
 
 		// check permission - user must have GUEST_PERMISSION in the context
 		boolean ok = securityService.checkSecurity(userId, MnemeService.GUEST_PERMISSION, context);
 
 		return ok;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Boolean allowListDeliveryAssessment(String context)
 	{
 		if (context == null) throw new IllegalArgumentException();
 		String userId = sessionManager.getCurrentSessionUserId();
 
 		if (M_log.isDebugEnabled()) M_log.debug("allowListDeliveryAssessment: " + context + ": " + userId);
 
 		// check permission - user must have SUBMIT_PERMISSION or MANAGE in the context
 		boolean ok = securityService.checkSecurity(userId, MnemeService.SUBMIT_PERMISSION, context)
 				|| securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context);
 
 		return Boolean.valueOf(ok);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Boolean allowManageAssessments(String context)
 	{
 		if (context == null) throw new IllegalArgumentException();
 		String userId = sessionManager.getCurrentSessionUserId();
 
 		if (M_log.isDebugEnabled()) M_log.debug("allowManageAssessments: " + context + ": " + userId);
 
 		// check permission - user must have MANAGE_PERMISSION in the context
 		boolean ok = securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context);
 
 		return ok;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Boolean allowRemoveAssessment(Assessment assessment)
 	{
 		if (assessment == null) throw new IllegalArgumentException();
 
 		if (M_log.isDebugEnabled()) M_log.debug("allowRemoveAssessment: " + assessment.getId());
 
 		// user must have manage permission
 		if (!this.allowManageAssessments(assessment.getContext())) return Boolean.FALSE;
 
 		// check policy
 		return satisfyAssessmentRemovalPolicy(assessment);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void applyBaseDateTx(String context, int time_diff)
 	{
 		try
 		{
 			// security check
 			securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, context);
 
 			this.storage.applyBaseDateTx(context, time_diff);
 		}
 		catch (AssessmentPermissionException ape)
 		{
 			throw new RuntimeException("applyBaseDateTx: security check failed " + ape.toString());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void clearStaleMintAssessments()
 	{
 		// give it a day
 		Date stale = new Date();
 		stale.setTime(stale.getTime() - (1000l * 60l * 60l * 24l));
 
 		if (M_log.isDebugEnabled()) M_log.debug("clearStaleMintAssessments");
 
 		List<String> ids = this.storage.clearStaleMintAssessments(stale);
 
 		// events
 		for (String id : ids)
 		{
 			eventTrackingService.post(eventTrackingService.newEvent(MnemeService.ASSESSMENT_DELETE, getAssessmentReference(id), true));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Assessment copyAssessment(String context, Assessment assessment) throws AssessmentPermissionException
 	{
 		if (context == null) throw new IllegalArgumentException();
 		if (assessment == null) throw new IllegalArgumentException();
 
 		// security check
 		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, context);
 
 		AssessmentImpl rv = doCopyAssessment(context, assessment, null, null, true, null);
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Integer countAssessments(String context)
 	{
 		if (context == null) throw new IllegalArgumentException();
 
 		if (M_log.isDebugEnabled()) M_log.debug("countAssessments: " + context);
 
 		return this.storage.countAssessments(context);
 	}
 
 	/**
 	 * Returns to uninitialized state.
 	 */
 	public void destroy()
 	{
 		M_log.info("destroy()");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Assessment> getArchivedAssessments(String context)
 	{
 		if (context == null) throw new IllegalArgumentException();
 
 		if (M_log.isDebugEnabled()) M_log.debug("getArchivedAssessments: " + context);
 
 		List<Assessment> rv = new ArrayList<Assessment>(this.storage.getArchivedAssessments(context));
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Assessment getAssessment(String id)
 	{
 		if (id == null) throw new IllegalArgumentException();
 
 		// for thread-local caching
 		String key = cacheKey(id);
 		AssessmentImpl rv = (AssessmentImpl) this.threadLocalManager.get(key);
 		if (rv != null)
 		{
 			// return a copy
 			return this.storage.clone(rv);
 		}
 
 		if (M_log.isDebugEnabled()) M_log.debug("getAssessment: " + id);
 
 		rv = this.storage.getAssessment(id);
 
 		// thread-local cache (a copy)
 		if (rv != null) this.threadLocalManager.set(key, this.storage.clone(rv));
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Assessment> getContextAssessments(String context, AssessmentsSort sort, Boolean publishedOnly)
 	{
 		if (context == null) throw new IllegalArgumentException();
 		if (publishedOnly == null) throw new IllegalArgumentException();
 		if (sort == null) sort = AssessmentsSort.cdate_a;
 
 		if (M_log.isDebugEnabled()) M_log.debug("getContextAssessments: " + context + " sort: " + sort + " publishOnly: " + publishedOnly);
 
 		List<Assessment> rv = new ArrayList<Assessment>(this.storage.getContextAssessments(context, sort, publishedOnly));
 
 		// TODO: needed?
 		// // thread-local cache each found assessment
 		// for (Assessment assessment : rv)
 		// {
 		// String key = cacheKey(assessment.getId());
 		// this.threadLocalManager.set(key, this.storage.newAssessment((AssessmentImpl) assessment));
 		// }
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Date getMinStartDate(String context)
 	{
 		Date minDate = this.storage.getMinStartDate(context);
 		return minDate;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<User> getSubmitUsers(String context)
 	{
 		if (M_log.isDebugEnabled()) M_log.debug("getSubmitUsers: " + context);
 
 		// get the ids
 		Set<String> ids = this.securityService.getUsersIsAllowed(MnemeService.SUBMIT_PERMISSION, context);
 
 		// turn into users
 		List<User> users = this.userDirectoryService.getUsers(ids);
 
 		// sort - by user sort name
 		Collections.sort(users, new Comparator()
 		{
 			public int compare(Object arg0, Object arg1)
 			{
 				int rv = ((User) arg0).getSortName().compareTo(((User) arg1).getSortName());
 				return rv;
 			}
 		});
 
 		return users;
 	}
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 		try
 		{
 			// storage - as configured
 			if (this.storageKey != null)
 			{
 				// if set to "SQL", replace with the current SQL vendor
 				if ("SQL".equals(this.storageKey))
 				{
 					this.storageKey = sqlService.getVendor();
 				}
 
 				this.storage = this.storgeOptions.get(this.storageKey);
 			}
 
 			// use "default" if needed
 			if (this.storage == null)
 			{
 				this.storage = this.storgeOptions.get("default");
 			}
 
 			if (storage == null) M_log.warn("no storage set: " + this.storageKey);
 
 			storage.init();
 
 			M_log.info("init(): storage: " + this.storage);
 		}
 		catch (Throwable t)
 		{
 			M_log.warn("init(): ", t);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Assessment newAssessment(String context) throws AssessmentPermissionException
 	{
 		if (context == null) throw new IllegalArgumentException();
 
 		if (M_log.isDebugEnabled()) M_log.debug("newAssessment: " + context);
 
 		// security check
 		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, context);
 
 		AssessmentImpl rv = this.storage.newAssessment();
 		rv.setContext(context);
 
 		// if we have a gradebook, enable gb integration
 		if (this.gradesService.available(context))
 		{
 			rv.getGrading().setGradebookIntegration(Boolean.TRUE);
 		}
 
 		save(rv);
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeAssessment(Assessment assessment) throws AssessmentPermissionException, AssessmentPolicyException
 	{
 		if (assessment == null) throw new IllegalArgumentException();
 
 		if (M_log.isDebugEnabled()) M_log.debug("removeAssessment: " + assessment.getId());
 
 		// security check
 		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, assessment.getContext());
 
 		// policy check
 		if (!satisfyAssessmentRemovalPolicy(assessment)) throw new AssessmentPolicyException();
 
 		// clear any test-drive submissions for this assessment
 		this.submissionService.removeTestDriveSubmissions(assessment);
 
 		// clear the cache
 		String key = cacheKey(assessment.getId());
 		this.threadLocalManager.set(key, null);
 
 		// retract the test from the gb
 		if (assessment.getIsValid() && assessment.getGrading().getGradebookIntegration() && assessment.getPublished())
 		{
 			this.gradesService.retractAssessmentGrades(assessment);
 		}
 
 		this.storage.removeAssessment((AssessmentImpl) assessment);
 
 		// event
 		eventTrackingService.post(eventTrackingService.newEvent(MnemeService.ASSESSMENT_DELETE, getAssessmentReference(assessment.getId()), true));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void rescoreAssessment(Assessment assessment) throws AssessmentPermissionException, GradesRejectsAssessmentException
 	{
 		// secure
 		this.securityService.secure(this.sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, assessment.getContext());
 
		// ignore if not locked
 		if (!assessment.getIsLocked()) return;
 
 		// pull the assessment from the grading authority
 		if (assessment.getGradebookIntegration() && assessment.getPublished())
 		{
 			this.gradesService.retractAssessmentGrades(assessment);
 		}
 
 		// re-score
 		this.submissionService.rescoreSubmission(assessment);
 
 		// return to the grading authority
 		if (assessment.getIsValid() && assessment.getGradebookIntegration() && assessment.getPublished())
 		{
 			// we should not be in the gb!
 			if (this.gradesService.assessmentReported(assessment))
 			{
 				throw new GradesRejectsAssessmentException();
 			}
 
 			// try to get into the gb
 			this.gradesService.initAssessmentGrades(assessment);
 
 			// report any completed official submissions
 			this.gradesService.reportAssessmentGrades(assessment);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void saveAssessment(Assessment assessment) throws AssessmentPermissionException, AssessmentPolicyException
 	{
 		if (assessment == null) throw new IllegalArgumentException();
 		if (assessment.getId() == null) throw new IllegalArgumentException();
 
 		// check for empty special access
 		((AssessmentSpecialAccessImpl) assessment.getSpecialAccess()).consolidate();
 
 		// if the type is changed to assignment, enforce related settings changes
 		if ((((AssessmentImpl) assessment).getTypeChanged()) && (assessment.getType() == AssessmentType.assignment))
 		{
 			// assignments always are flexible
 			assessment.setRandomAccess(Boolean.TRUE);
 
 			// also default to "review available upon submission" and "manual release"
 			assessment.getReview().setTiming(ReviewTiming.submitted);
 			assessment.getGrading().setAutoRelease(Boolean.FALSE);
 		}
 
 		// if any changes made, clear mint
 		if (assessment.getIsChanged())
 		{
 			((AssessmentImpl) assessment).clearMint();
 		}
 
 		// otherwise we don't save: but if mint, we delete
 		else
 		{
 			// if mint, delete instead of save
 			if (((AssessmentImpl) assessment).getMint())
 			{
 				if (M_log.isDebugEnabled()) M_log.debug("saveAssessment: deleting mint: " + assessment.getId());
 
 				// clear the cache
 				this.threadLocalManager.set(cacheKey(assessment.getId()), null);
 
 				this.storage.removeAssessment((AssessmentImpl) assessment);
 
 				// event
 				eventTrackingService.post(eventTrackingService.newEvent(MnemeService.ASSESSMENT_DELETE, getAssessmentReference(assessment.getId()),
 						true));
 			}
 
 			return;
 		}
 
 		if (M_log.isDebugEnabled()) M_log.debug("saveAssessment: " + assessment.getId());
 
 		// security check
 		securityService.secure(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, assessment.getContext());
 
 		// check for changes not allowed if locked
 		if ((assessment.getIsLocked()) && ((AssessmentImpl) assessment).getIsLockedChanged()) throw new AssessmentPolicyException();
 
 		// clear any test-drive submissions for this assessment
 		this.submissionService.removeTestDriveSubmissions(assessment);
 
 		// see if we need to retract or release grades
 		boolean retract = false;
 		boolean release = false;
 		if (((AssessmentGradingImpl) (assessment.getGrading())).getAutoReleaseChanged())
 		{
 			if (assessment.getGrading().getAutoRelease())
 			{
 				release = true;
 			}
 			else
 			{
 				retract = true;
 			}
 		}
 		// clear the auto-release change tracking
 		((AssessmentGradingImpl) (assessment.getGrading())).initAutoRelease(assessment.getGrading().getAutoRelease());
 
 		// see if we have had a title change (and clear)
 		boolean titleChanged = ((AssessmentImpl) assessment).getTitleChanged();
 		((AssessmentImpl) assessment).initTitle(assessment.getTitle());
 
 		// see if we had a change in published (and clear)
 		boolean publishedChanged = ((AssessmentImpl) assessment).getPublishedChanged();
 		((AssessmentImpl) assessment).initPublished(assessment.getPublished());
 
 		// see if we have had a due date change (and clear)
 		boolean dueChanged = ((AssessmentDatesImpl) assessment.getDates()).getDueDateChanged();
 		((AssessmentDatesImpl) assessment.getDates()).initDueDate(assessment.getDates().getDueDate());
 
 		// see if we have just been archived (and clear)
 		boolean archivedChanged = ((AssessmentImpl) assessment).getArchivedChanged();
 		((AssessmentImpl) assessment).initArchived(assessment.getArchived());
 
 		// see if we have changed our gradebook integration (and clear)
 		boolean gbIntegrationChanged = ((AssessmentGradingImpl) (assessment.getGrading())).getGradebookIntegrationChanged();
 		((AssessmentGradingImpl) (assessment.getGrading())).initGradebookIntegration(assessment.getGrading().getGradebookIntegration());
 
 		// see if we need to re-score (and clear)
 		boolean rescore = assessment.getIsLocked() && ((AssessmentImpl) assessment).getNeedsRescore();
 		((AssessmentImpl) assessment).initNeedsRescore(false);
 
 		// see if the type changed (and clear)
 		boolean typeChanged = ((AssessmentImpl) assessment).getTypeChanged();
 		assessment.initType(assessment.getType());
 
 		// make sure we are not still considered invalid for gb - if we are, we will pick that up down below
 		((AssessmentGradingImpl) (assessment.getGrading())).initGradebookRejectedAssessment(Boolean.FALSE);
 
 		// see if we have changed our validity
 		boolean validityChanged = false;
 		boolean nowValid = assessment.getIsValid();
 		Assessment b4 = getAssessment(assessment.getId());
 		if (b4 != null)
 		{
 			validityChanged = (b4.getIsValid().booleanValue() != nowValid);
 		}
 
 		// if we are just going published and not yet live, bring the assessment live
 		if (!assessment.getIsLive() && publishedChanged && assessment.getPublished())
 		{
 			((AssessmentImpl) assessment).lock();
 		}
 
 		// get the assessment before these changes
 		Assessment current = getAssessment(assessment.getId());
 
 		// save the changes
 		save((AssessmentImpl) assessment);
 
 		// event for change in published
 		if (publishedChanged)
 		{
 			if (assessment.getPublished())
 			{
 				eventTrackingService.post(eventTrackingService.newEvent(MnemeService.ASSESSMENT_PUBLISH, getAssessmentReference(assessment.getId()),
 						true));
 			}
 			else
 			{
 				eventTrackingService.post(eventTrackingService.newEvent(MnemeService.ASSESSMENT_UNPUBLISH,
 						getAssessmentReference(assessment.getId()), true));
 			}
 		}
 
 		// if the name or due date has changed, or we are retracting submissions, or we are now unpublished,
 		// or we are now invalid, or we have just been archived, or we are now not gradebook integrated,
 		// or we are releasing (we need to remove our entry so we can add it back without conflict)
 		// or we changed type to survey
 		// retract the assessment from the grades authority
 		if (rescore || titleChanged || dueChanged || retract || release || (publishedChanged && !assessment.getPublished())
 				|| (validityChanged && !nowValid) || (archivedChanged && assessment.getArchived())
 				|| (gbIntegrationChanged && !assessment.getGradebookIntegration()) || (typeChanged && assessment.getType() == AssessmentType.survey))
 		{
 			// retract the entire assessment from grades - use the old information (title) (if we existed before this call)
 			// ONLY IF we were expecting to be in the gb based on current values
 			if ((current != null) && current.getIsValid() && current.getGradebookIntegration() && current.getPublished())
 			{
 				this.gradesService.retractAssessmentGrades(current);
 			}
 
 			// retract the submissions
 			if (retract)
 			{
 				this.submissionService.retractSubmissions(assessment);
 			}
 		}
 
 		// re-score the submissions if needed
 		if (rescore)
 		{
 			this.submissionService.rescoreSubmission(assessment);
 		}
 
 		// if the name or due date has changed, or we are releasing submissions, or we are now published,
 		// or we are now valid (and are published), or we are now gradebook integrated,
 		// or we are retracting (we need to add the entry back in that we just removed)
 		// report the assessment and all completed submissions to the grades authority
 		if (rescore || titleChanged || dueChanged || release || retract || (publishedChanged && assessment.getPublished())
 				|| (validityChanged && nowValid && assessment.getPublished()) || (gbIntegrationChanged && assessment.getGradebookIntegration()))
 		{
 			if (assessment.getIsValid() && assessment.getGradebookIntegration() && assessment.getPublished())
 			{
 				try
 				{
 					// we should not be in the gb!
 					if (this.gradesService.assessmentReported(assessment))
 					{
 						throw new GradesRejectsAssessmentException();
 					}
 
 					// try to get into the gb
 					this.gradesService.initAssessmentGrades(assessment);
 
 					// report any completed official submissions
 					this.gradesService.reportAssessmentGrades(assessment);
 				}
 				catch (GradesRejectsAssessmentException e)
 				{
 					// mark as invalid
 					((AssessmentGradingImpl) (assessment.getGrading())).initGradebookRejectedAssessment(Boolean.TRUE);
 
 					// re-save
 					save((AssessmentImpl) assessment);
 				}
 			}
 
 			// release the submissions, if we need to (each will have the grade reported)
 			if (release)
 			{
 				this.submissionService.releaseSubmissions(assessment, Boolean.FALSE);
 			}
 		}
 
 		// our change might make other gradebook-invalid assessments valid - only if we were in the gb to stat with
 		if ((current != null) && current.getPublished() && current.getGradebookIntegration() && (!current.getArchived()) && current.getIsValid())
 		{
 			if (titleChanged || (publishedChanged && (!assessment.getPublished())) || (archivedChanged && assessment.getArchived())
 					|| (gbIntegrationChanged && (!assessment.getGradebookIntegration())))
 			{
 
 				// get all context assessments that are invalid due to gb integration
 				List<AssessmentImpl> gbInvalid = this.storage.getContextGbInvalidAssessments(assessment.getContext());
 
 				// for each one
 				for (AssessmentImpl a : gbInvalid)
 				{
 					// clear the invalid (so it does not trigger the getIsValid call)
 					((AssessmentGradingImpl) (a.getGrading())).initGradebookRejectedAssessment(Boolean.FALSE);
 
 					if (a.getIsValid() && a.getGradebookIntegration() && a.getPublished())
 					{
 						try
 						{
 							// we should not be in the gb!
 							if (this.gradesService.assessmentReported(a))
 							{
 								throw new GradesRejectsAssessmentException();
 							}
 
 							// try to get into the gb
 							this.gradesService.initAssessmentGrades(a);
 
 							// report any completed official submissions
 							this.gradesService.reportAssessmentGrades(a);
 
 							// save (the invalid flag is cleared)
 							save((AssessmentImpl) a);
 						}
 						catch (GradesRejectsAssessmentException e)
 						{
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Dependency: AttachmentService.
 	 * 
 	 * @param service
 	 *        The AttachmentService.
 	 */
 	public void setAttachmentService(AttachmentService service)
 	{
 		attachmentService = service;
 	}
 
 	/**
 	 * Dependency: EventTrackingService.
 	 * 
 	 * @param service
 	 *        The EventTrackingService.
 	 */
 	public void setEventTrackingService(EventTrackingService service)
 	{
 		eventTrackingService = service;
 	}
 
 	/**
 	 * Dependency: GradesService.
 	 * 
 	 * @param service
 	 *        The GradesService.
 	 */
 	public void setGradesService(GradesService service)
 	{
 		this.gradesService = service;
 	}
 
 	/**
 	 * Dependency: PoolService.
 	 * 
 	 * @param service
 	 *        The PoolService.
 	 */
 	public void setPoolService(PoolService service)
 	{
 		poolService = service;
 	}
 
 	/**
 	 * Dependency: QuestionService.
 	 * 
 	 * @param service
 	 *        The QuestionService.
 	 */
 	public void setQuestionService(QuestionService service)
 	{
 		questionService = service;
 	}
 
 	/**
 	 * Dependency: SecurityService.
 	 * 
 	 * @param service
 	 *        The SecurityService.
 	 */
 	public void setSecurityService(SecurityService service)
 	{
 		securityService = service;
 	}
 
 	/**
 	 * Dependency: SessionManager.
 	 * 
 	 * @param service
 	 *        The SessionManager.
 	 */
 	public void setSessionManager(SessionManager service)
 	{
 		sessionManager = service;
 	}
 
 	/**
 	 * Dependency: SqlService.
 	 * 
 	 * @param service
 	 *        The SqlService.
 	 */
 	public void setSqlService(SqlService service)
 	{
 		sqlService = service;
 	}
 
 	/**
 	 * Set the storage class options.
 	 * 
 	 * @param options
 	 *        The PoolStorage options.
 	 */
 	public void setStorage(Map options)
 	{
 		this.storgeOptions = options;
 	}
 
 	/**
 	 * Set the storage option key to use, selecting which PoolStorage to use.
 	 * 
 	 * @param key
 	 *        The storage option key.
 	 */
 	public void setStorageKey(String key)
 	{
 		this.storageKey = key;
 	}
 
 	/**
 	 * Dependency: SubmissionService.
 	 * 
 	 * @param service
 	 *        The SubmissionService.
 	 */
 	public void setSubmissionService(SubmissionService service)
 	{
 		submissionService = (SubmissionServiceImpl) service;
 	}
 
 	/**
 	 * Dependency: ThreadLocalManager.
 	 * 
 	 * @param service
 	 *        The SqlService.
 	 */
 	public void setThreadLocalManager(ThreadLocalManager service)
 	{
 		threadLocalManager = service;
 	}
 
 	/**
 	 * Dependency: UserDirectoryService.
 	 * 
 	 * @param service
 	 *        The UserDirectoryService.
 	 */
 	public void setUserDirectoryService(UserDirectoryService service)
 	{
 		userDirectoryService = service;
 	}
 
 	/**
 	 * Form a key for caching an assessment.
 	 * 
 	 * @param assessmentId
 	 *        The assessment id.
 	 * @return The cache key.
 	 */
 	protected String cacheKey(String assessmentId)
 	{
 		String key = "mneme:assessment:" + assessmentId;
 		return key;
 	}
 
 	/**
 	 * Copy an assessment
 	 * 
 	 * @param context
 	 *        The destination context.
 	 * @param assessment
 	 *        The source assessment.
 	 * @param pidMap
 	 *        A map (old pool id -> new pool id) to use to convert all pool references.
 	 * @param qidMap
 	 *        A map (old question id -> new question id) to use to convert all question references.
 	 * @param appendTitle
 	 *        if true, append text to the title, else leave the title an exact copy.
 	 * @param attachmentTranslations
 	 *        A list of Translations for attachments and embedded media.
 	 */
 	protected AssessmentImpl doCopyAssessment(String context, Assessment assessment, Map<String, String> pidMap, Map<String, String> qidMap,
 			boolean appendTitle, List<Translation> attachmentTranslations)
 	{
 		if (context == null) throw new IllegalArgumentException();
 		if (assessment == null) throw new IllegalArgumentException();
 
 		if (M_log.isDebugEnabled()) M_log.debug("doCopyAssessment: context:" + context + " id: " + assessment.getId());
 
 		String userId = sessionManager.getCurrentSessionUserId();
 		Date now = new Date();
 
 		AssessmentImpl rv = this.storage.clone((AssessmentImpl) assessment);
 
 		// clear the id to make it a new one
 		rv.id = null;
 
 		// set the context
 		rv.setContext(context);
 
 		// add to the title
 		if (appendTitle)
 		{
 			rv.setTitle(((PoolServiceImpl) this.poolService).addDate("copy-text", rv.getTitle(), now));
 		}
 
 		// clear archived
 		rv.initArchived(Boolean.FALSE);
 
 		// clear out any special access
 		rv.getSpecialAccess().clear();
 
 		// start out unpublished
 		rv.initPublished(Boolean.FALSE);
 
 		// and not-live, non-locked
 		rv.initLive(Boolean.FALSE);
 		rv.initLocked(Boolean.FALSE);
 
 		((AssessmentGradingImpl) (rv.getGrading())).initGradebookRejectedAssessment(Boolean.FALSE);
 
 		// update created and last modified information
 		rv.getCreatedBy().setDate(now);
 		rv.getCreatedBy().setUserId(userId);
 		rv.getModifiedBy().setDate(now);
 		rv.getModifiedBy().setUserId(userId);
 
 		// set the parts to their original question and pool values
 		for (Part part : rv.getParts().getParts())
 		{
 			// if any detail fails to restore, remove it
 			for (Iterator<PartDetail> i = part.getDetails().iterator(); i.hasNext();)
 			{
 				PartDetail detail = i.next();
 				if (!detail.restoreToOriginal(pidMap, qidMap))
 				{
 					i.remove();
 				}
 			}
 		}
 
 		// translate embedded media references
 		if (attachmentTranslations != null)
 		{
 			rv.getPresentation().setText(this.attachmentService.translateEmbeddedReferences(rv.getPresentation().getText(), attachmentTranslations));
 			rv.getSubmitPresentation().setText(
 					this.attachmentService.translateEmbeddedReferences(rv.getSubmitPresentation().getText(), attachmentTranslations));
 			for (Part p : rv.getParts().getParts())
 			{
 				p.getPresentation()
 						.setText(this.attachmentService.translateEmbeddedReferences(p.getPresentation().getText(), attachmentTranslations));
 			}
 		}
 
 		// change the auto-pool to the imported version of the pool
 		if (rv.poolId != null)
 		{
 			// if we have pool translations, see if we can find our auto-pool in the new set (would happen on an import assessment from site)
 			if (pidMap != null)
 			{
 				String translated = pidMap.get(rv.poolId);
 				if (translated != null)
 				{
 					rv.poolId = translated;
 				}
 				else
 				{
 					rv.poolId = null;
 				}
 			}
 
 			// otherwise just clear our auto-pool (would happen on a copy assessment)
 			else
 			{
 				rv.poolId = null;
 			}
 		}
 
 		// save
 		this.storage.saveAssessment(rv);
 
 		// event
 		eventTrackingService.post(eventTrackingService.newEvent(MnemeService.ASSESSMENT_NEW, getAssessmentReference(rv.getId()), true));
 
 		return rv;
 	}
 
 	/**
 	 * Form an assessment reference for this assessment id.
 	 * 
 	 * @param assessmentId
 	 *        the assessment id.
 	 * @return the assessment reference for this assessment id.
 	 */
 	protected String getAssessmentReference(String assessmentId)
 	{
 		String ref = MnemeService.REFERENCE_ROOT + "/" + MnemeService.ASSESSMENT_TYPE + "/" + assessmentId;
 		return ref;
 	}
 
 	/**
 	 * Set this assessment to be live.
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void makeLive(Assessment assessment)
 	{
 		// clear the cache
 		this.threadLocalManager.set(cacheKey(assessment.getId()), null);
 
 		this.storage.makeLive(assessment);
 	}
 
 	/**
 	 * Remove any draw dependencies on this pool from all unlocked assessments.
 	 * 
 	 * @param question
 	 *        The question.
 	 */
 	protected void removeDependency(Pool pool)
 	{
 		// clear any test-drive submissions for this assessment
 		this.submissionService.removeTestDriveSubmissions(pool.getContext());
 
 		this.storage.removeDependency(pool);
 	}
 
 	/**
 	 * Remove any pick dependencies on this question from all unlocked assessments.
 	 * 
 	 * @param question
 	 *        The question.
 	 */
 	protected void removeDependency(Question question)
 	{
 		// clear any test-drive submissions for this assessment
 		this.submissionService.removeTestDriveSubmissions(question.getContext());
 
 		this.storage.removeDependency(question);
 	}
 
 	/**
 	 * Check if this assessment meets the delete policy.
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 * @return TRUE if the assessment may be deleted, FALSE if not.
 	 */
 	protected Boolean satisfyAssessmentRemovalPolicy(Assessment assessment)
 	{
 		// live tests may not be deleted
 		if (assessment.getIsLive()) return Boolean.FALSE;
 
 		return Boolean.TRUE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected void save(AssessmentImpl assessment)
 	{
 		if (M_log.isDebugEnabled()) M_log.debug("save: " + assessment.getId());
 
 		Date now = new Date();
 		String userId = sessionManager.getCurrentSessionUserId();
 
 		String event = MnemeService.ASSESSMENT_EDIT;
 
 		// if the assessment is new (i.e. no id), set the createdBy information, if not already set
 		if (assessment.getId() == null)
 		{
 			if (assessment.getCreatedBy().getUserId() == null)
 			{
 				assessment.getCreatedBy().setDate(now);
 				assessment.getCreatedBy().setUserId(userId);
 			}
 
 			event = MnemeService.ASSESSMENT_NEW;
 		}
 
 		// update last modified information
 		assessment.getModifiedBy().setDate(now);
 		assessment.getModifiedBy().setUserId(userId);
 
 		// clear the cache
 		this.threadLocalManager.set(cacheKey(assessment.getId()), null);
 
 		// save
 		this.storage.saveAssessment(assessment);
 
 		// event
 		eventTrackingService.post(eventTrackingService.newEvent(event, getAssessmentReference(assessment.getId()), true));
 	}
 }
