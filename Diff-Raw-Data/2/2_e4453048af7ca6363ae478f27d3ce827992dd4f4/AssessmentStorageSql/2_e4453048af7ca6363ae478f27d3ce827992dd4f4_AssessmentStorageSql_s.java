 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.mneme.api.Assessment;
 import org.etudes.mneme.api.AssessmentAccess;
 import org.etudes.mneme.api.AssessmentService;
 import org.etudes.mneme.api.AssessmentType;
 import org.etudes.mneme.api.Part;
 import org.etudes.mneme.api.PartDetail;
 import org.etudes.mneme.api.Pool;
 import org.etudes.mneme.api.PoolDraw;
 import org.etudes.mneme.api.PoolService;
 import org.etudes.mneme.api.Question;
 import org.etudes.mneme.api.QuestionGrouping;
 import org.etudes.mneme.api.QuestionPick;
 import org.etudes.mneme.api.QuestionService;
 import org.etudes.mneme.api.ReviewShowCorrect;
 import org.etudes.mneme.api.ReviewTiming;
 import org.etudes.mneme.api.SecurityService;
 import org.etudes.mneme.api.SubmissionService;
 import org.sakaiproject.db.api.SqlReader;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.i18n.InternationalizedMessages;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 /**
  * AssessmentStorageMysql implements AssessmentStorage for SQL databases.
  */
 public abstract class AssessmentStorageSql implements AssessmentStorage
 {
 	/** Our logger. */
 	private static Log M_log = LogFactory.getLog(AssessmentStorageSql.class);
 
 	/** Dependency: AssessmentService. */
 	protected AssessmentService assessmentService = null;
 
 	/** Configuration: to run the ddl on init or not. */
 	protected boolean autoDdl = false;
 
 	/** Messages bundle name. */
 	protected String bundle = null;
 
 	/** Messages. */
 	protected transient InternationalizedMessages messages = null;
 
 	/** Dependency: PoolService. */
 	protected PoolService poolService = null;
 
 	/** Dependency: QuestionService. */
 	protected QuestionService questionService = null;
 
 	/** Dependency: SecuritySevice. */
 	protected SecurityService securityService = null;
 
 	/** Dependency: SqlService. */
 	protected SqlService sqlService = null;
 
 	protected SubmissionService submissionService = null;
 
 	/** Dependency: ThreadLocalManager. */
 	protected ThreadLocalManager threadLocalManager = null;
 
 	/** Dependency: UserDirectoryService. */
 	protected UserDirectoryService userDirectoryService = null;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<String> clearStaleMintAssessments(final Date stale)
 	{
 		final List<String> rv = new ArrayList<String>();
 
 		this.sqlService.transact(new Runnable()
 		{
 			public void run()
 			{
 				clearStaleMintQuestionsTx(stale, rv);
 			}
 		}, "clearStaleMintQuestions: " + stale.toString());
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public AssessmentImpl clone(AssessmentImpl assessment)
 	{
 		return new AssessmentImpl(assessment);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Integer countAssessments(String context)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT COUNT(1) FROM MNEME_ASSESSMENT A");
 		sql.append(" WHERE A.CONTEXT=? AND A.ARCHIVED='0' AND A.MINT='0'");
 		Object[] fields = new Object[1];
 		fields[0] = context;
 		List results = this.sqlService.dbRead(sql.toString(), fields, null);
 		if (results.size() > 0)
 		{
 			return Integer.valueOf((String) results.get(0));
 		}
 
 		return Integer.valueOf(0);
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
 	public Boolean existsAssessment(String id)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT COUNT(1) FROM MNEME_ASSESSMENT A");
 		sql.append(" WHERE A.ID=?");
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(id);
 		List results = this.sqlService.dbRead(sql.toString(), fields, null);
 		if (results.size() > 0)
 		{
 			int size = Integer.parseInt((String) results.get(0));
 			return Boolean.valueOf(size == 1);
 		}
 
 		return Boolean.FALSE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<AssessmentImpl> getArchivedAssessments(String context)
 	{
 		String where = "WHERE A.CONTEXT=? AND A.ARCHIVED='1' AND A.MINT='0'";
 		String order = "ORDER BY DATES_ARCHIVED ASC";
 
 		Object[] fields = new Object[1];
 		fields[0] = context;
 
 		return readAssessments(where, order.toString(), fields);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public AssessmentImpl getAssessment(String id)
 	{
 		return readAssessment(id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<AssessmentImpl> getAssessmentsNeedingResultsEmail()
 	{
 		String where = "WHERE A.RESULTS_EMAIL IS NOT NULL AND A.PUBLISHED = '1' AND A.RESULTS_SENT IS NULL";
 
 		return readAssessments(where, null, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<AssessmentImpl> getContextAssessments(String context, final AssessmentService.AssessmentsSort sort, Boolean publishedOnly)
 	{
 		String where = "WHERE A.CONTEXT=? AND A.ARCHIVED='0' AND A.MINT='0'";
 		if (publishedOnly)
 		{
 			where += " AND A.PUBLISHED='1'";
 		}
 
 		// sort
 		String order = null;
 		switch (sort)
 		{
 			case published_a:
 			{
 				order = " ORDER BY A.PUBLISHED ASC, A.TITLE ASC, A.CREATED_BY_DATE ASC";
 				break;
 			}
 			case published_d:
 			{
 				order = " ORDER BY A.PUBLISHED DESC, A.TITLE DESC, A.CREATED_BY_DATE DESC";
 				break;
 			}
 			case title_a:
 			{
 				order = " ORDER BY A.TITLE ASC, A.CREATED_BY_DATE ASC";
 				break;
 			}
 			case title_d:
 			{
 				order = " ORDER BY A.TITLE DESC, A.CREATED_BY_DATE DESC";
 				break;
 			}
 			case type_a:
 			{
 				// TODO: getType().getSortValue()
 				order = " ORDER BY A.TYPE ASC, A.TITLE ASC, A.CREATED_BY_DATE ASC";
 				break;
 			}
 			case type_d:
 			{
 				// TODO: getType().getSortValue()
 				order = " ORDER BY A.TYPE DESC, A.TITLE DESC, A.CREATED_BY_DATE DESC";
 				break;
 			}
 			case odate_a:
 			{
 				// TODO: null sorts low
 				order = " ORDER BY A.DATES_OPEN ASC, A.TITLE ASC, A.CREATED_BY_DATE ASC";
 				break;
 			}
 			case odate_d:
 			{
 				// TODO: null sorts low
 				order = " ORDER BY A.DATES_OPEN DESC, A.TITLE DESC, A.CREATED_BY_DATE DESC";
 				break;
 			}
 			case ddate_a:
 			{
 				// TODO: null sorts high
 				order = " ORDER BY A.DATES_DUE ASC, A.TITLE ASC, A.CREATED_BY_DATE ASC";
 				break;
 			}
 			case ddate_d:
 			{
 				// TODO: null sorts high
 				order = " ORDER BY A.DATES_DUE DESC, A.TITLE DESC, A.CREATED_BY_DATE DESC";
 				break;
 			}
 			case cdate_a:
 			{
 				order = " ORDER BY A.CREATED_BY_DATE ASC";
 				break;
 			}
 			case cdate_d:
 			{
 				order = " ORDER BY A.CREATED_BY_DATE DESC";
 				break;
 			}
 		}
 
 		Object[] fields = new Object[1];
 		fields[0] = context;
 
 		List<AssessmentImpl> rv = readAssessments(where, (order != null) ? order.toString() : null, fields);
 
 		// since valid is not stored on the db, filter invalid ones out if desired
 		if (publishedOnly)
 		{
 			for (Iterator i = rv.iterator(); i.hasNext();)
 			{
 				AssessmentImpl a = (AssessmentImpl) i.next();
 				if (!a.getIsValid())
 				{
 					i.remove();
 				}
 			}
 		}
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<AssessmentImpl> getContextGbInvalidAssessments(String context)
 	{
 		String where = "WHERE A.CONTEXT=? AND A.GRADING_REJECTED='1'";
 
 		Object[] fields = new Object[1];
 		fields[0] = context;
 
 		List<AssessmentImpl> rv = readAssessments(where, null, fields);
 
 		return rv;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Date getMinStartDate(String context)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT MIN(DATES_ACCEPT_UNTIL), MIN(DATES_DUE), MIN(DATES_OPEN), MIN(REVIEW_DATE)");
 		sql.append(" FROM MNEME_ASSESSMENT");
 		sql.append(" WHERE CONTEXT = ? AND ARCHIVED=0");
 
 		Object[] fields = new Object[1];
 		fields[0] = context;
 
 		List results = this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
 		{
 			public Object readSqlResultRecord(ResultSet result)
 			{
 				try
 				{
 					Date rv = null;
 					for (int i = 1; i <= 4; i++)
 					{
 						Date d = SqlHelper.readDate(result, i);
 						if (rv == null)
 						{
 							rv = d;
 						}
 						else if ((d != null) && d.before(rv))
 						{
 							rv = d;
 						}
 					}
 
 					return rv;
 				}
 				catch (SQLException e)
 				{
 					M_log.warn("getMinStartDate: " + e);
 					return null;
 				}
 			}
 		});
 
 		if (results.size() > 0)
 		{
 			return (Date) results.get(0);
 		}
 
 		return null;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public Date getMaxStartDate(String context)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT MAX(DATES_ACCEPT_UNTIL), MAX(DATES_DUE), MAX(DATES_OPEN), MAX(REVIEW_DATE)");
 		sql.append(" FROM MNEME_ASSESSMENT");
 		sql.append(" WHERE CONTEXT = ? AND ARCHIVED=0");
 
 		Object[] fields = new Object[1];
 		fields[0] = context;
 
 		List results = this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
 		{
 			public Object readSqlResultRecord(ResultSet result)
 			{
 				try
 				{
 					Date rv = null;
 					for (int i = 1; i <= 4; i++)
 					{
 						Date d = SqlHelper.readDate(result, i);
 						if (rv == null)
 						{
 							rv = d;
 						}
						else if ((d != null) && d.before(rv))
 						{
 							rv = d;
 						}
 					}
 
 					return rv;
 				}
 				catch (SQLException e)
 				{
 					M_log.warn("getMaxStartDate: " + e);
 					return null;
 				}
 			}
 		});
 
 		if (results.size() > 0)
 		{
 			return (Date) results.get(0);
 		}
 
 		return null;
 	}	
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void makeLive(final Assessment assessment)
 	{
 		this.sqlService.transact(new Runnable()
 		{
 			public void run()
 			{
 				makeLiveTx(assessment);
 			}
 		}, "makeLive: " + assessment.getId());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public AssessmentImpl newAssessment()
 	{
 		return new AssessmentImpl(this.assessmentService, this.poolService, this.questionService, this.submissionService, this.securityService,
 				this.userDirectoryService, this.messages);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeAssessment(AssessmentImpl assessment)
 	{
 		deleteAssessment(assessment);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeDependency(final Pool pool)
 	{
 		this.sqlService.transact(new Runnable()
 		{
 			public void run()
 			{
 				removeDependencyTx(pool);
 			}
 		}, "removeDependency(pool): " + pool.getId());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeDependency(final Question question)
 	{
 		this.sqlService.transact(new Runnable()
 		{
 			public void run()
 			{
 				removeDependencyTx(question);
 			}
 		}, "removeDependency(question): " + question.getId());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void saveAssessment(AssessmentImpl assessment)
 	{
 		// set the part detail sequences
 		((AssessmentPartsImpl) assessment.getParts()).setDetailSeq();
 
 		// for new assessments
 		if (assessment.getId() == null)
 		{
 			insertAssessment(assessment);
 		}
 
 		// for existing assessments
 		else
 		{
 			updateAssessment(assessment);
 		}
 
 		// clear changed flags and deleted lists
 		assessment.clearChanged();
 		for (AssessmentAccess access : assessment.getSpecialAccess().getAccess())
 		{
 			((AssessmentAccessImpl) access).clearChanged();
 		}
 		((AssessmentSpecialAccessImpl) assessment.getSpecialAccess()).clearDeleted();
 
 		for (Part part : assessment.getParts().getParts())
 		{
 			((PartImpl) part).clearChanged();
 			((PartImpl) part).clearDeleted();
 			for (PartDetail detail : part.getDetails())
 			{
 				((PartDetailImpl) detail).clearChanged();
 			}
 		}
 		((AssessmentPartsImpl) assessment.getParts()).clearDeleted();
 	}
 
 	/**
 	 * Set the AssessmentService.
 	 * 
 	 * @param service
 	 *        The AssessmentService.
 	 */
 	public void setAssessmentService(AssessmentService service)
 	{
 		this.assessmentService = service;
 	}
 
 	/**
 	 * Configuration: to run the ddl on init or not.
 	 * 
 	 * @param value
 	 *        the auto ddl value.
 	 */
 	public void setAutoDdl(String value)
 	{
 		autoDdl = new Boolean(value).booleanValue();
 	}
 
 	/**
 	 * Set the message bundle.
 	 * 
 	 * @param bundle
 	 *        The message bundle.
 	 */
 	public void setBundle(String name)
 	{
 		this.bundle = name;
 	}
 
 	/**
 	 * Set the PoolService.
 	 * 
 	 * @param service
 	 *        The PoolService.
 	 */
 	public void setPoolService(PoolService service)
 	{
 		this.poolService = service;
 	}
 
 	/**
 	 * Set the QuestionService.
 	 * 
 	 * @param service
 	 *        The QuestionService.
 	 */
 	public void setQuestionService(QuestionService service)
 	{
 		this.questionService = service;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setResultsSent(String id, Date date)
 	{
 		setResultsSentTx(id, date);
 	}
 
 	/**
 	 * Set the SecurityService.
 	 * 
 	 * @param service
 	 *        The PoolService.
 	 */
 	public void setSecurityService(SecurityService service)
 	{
 		this.securityService = service;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setSqlService(SqlService service)
 	{
 		this.sqlService = service;
 	}
 
 	/**
 	 * Set the SubmissionService.
 	 * 
 	 * @param service
 	 *        The SubmissionService.
 	 */
 	public void setSubmissionService(SubmissionService service)
 	{
 		this.submissionService = service;
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
 	 * Set the UserDirectoryService.
 	 * 
 	 * @param service
 	 *        The UserDirectoryService.
 	 */
 	public void setUserDirectoryService(UserDirectoryService service)
 	{
 		this.userDirectoryService = service;
 	}
 
 	/**
 	 * Transaction code for clearStaleMintQuestions()
 	 */
 	protected void clearStaleMintQuestionsTx(Date stale, List<String> ids)
 	{
 		// Note: for now, lets assume that a mint assessment can have no parts or access, else its not mint! -ggolden
 
 		Object[] fields = new Object[1];
 		fields[0] = stale.getTime();
 
 		StringBuilder sql = new StringBuilder();
 
 		sql.append("SELECT ID FROM MNEME_ASSESSMENT");
 		sql.append(" WHERE MINT='1' AND CREATED_BY_DATE < ?");
 		List<String> rv = this.sqlService.dbRead(sql.toString(), fields, null);
 		ids.addAll(rv);
 
 		// assessments
 		sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT");
 		sql.append(" WHERE MINT='1' AND CREATED_BY_DATE < ?");
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("clearStaleMintQuestionsTx(assessment): db write failed");
 		}
 	}
 
 	/**
 	 * Delete an assessment.
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessment(final AssessmentImpl assessment)
 	{
 		this.sqlService.transact(new Runnable()
 		{
 			public void run()
 			{
 				deleteAssessmentTx(assessment);
 			}
 		}, "deleteAssessment: " + assessment.getId());
 	}
 
 	/**
 	 * Delete an assessment's access record (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 * @param access
 	 *        The access to delete.
 	 */
 	protected void deleteAssessmentAccessTx(AssessmentAccess access)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT_ACCESS");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(access.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentAccessTx(access): db write failed");
 		}
 	}
 
 	/**
 	 * Delete an assessment's access records (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessmentAccessTx(AssessmentImpl assessment)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT_ACCESS");
 		sql.append(" WHERE ASSESSMENT_ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(assessment.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentAccessTx(assessment): db write failed");
 		}
 	}
 
 	/**
 	 * Delete an assessment's part detail (draw and pick) records (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessmentPartDetailTx(AssessmentImpl assessment)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT_PART_DETAIL");
 		sql.append(" WHERE ASSESSMENT_ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(assessment.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentPartDetailTx(assessment): db write failed");
 		}
 	}
 
 	/**
 	 * Delete an assessment's part detail for a single part(draw and pick) records (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessmentPartDetailTx(Part part)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT_PART_DETAIL");
 		sql.append(" WHERE PART_ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(part.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentPartDetailTx(part): db write failed");
 		}
 	}
 
 	/**
 	 * Delete a detail record (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessmentPartDetailTx(PartDetail detail)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT_PART_DETAIL");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(detail.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentPartDetailTx(detail): db write failed");
 		}
 	}
 
 	/**
 	 * Delete an assessment's part records (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessmentPartTx(AssessmentImpl assessment)
 	{
 		// part pick-draw
 		deleteAssessmentPartDetailTx(assessment);
 
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT_PART");
 		sql.append(" WHERE ASSESSMENT_ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(assessment.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentPartTx(assessment): db write failed");
 		}
 	}
 
 	/**
 	 * Delete an assessment part record (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessmentPartTx(Part part)
 	{
 		// part pick-draw
 		deleteAssessmentPartDetailTx(part);
 
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT_PART");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(part.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentPartTx(part): db write failed");
 		}
 	}
 
 	/**
 	 * Delete an assessment (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void deleteAssessmentTx(AssessmentImpl assessment)
 	{
 		// access
 		deleteAssessmentAccessTx(assessment);
 
 		// parts
 		deleteAssessmentPartTx(assessment);
 
 		// assessment
 		StringBuilder sql = new StringBuilder();
 		sql.append("DELETE FROM MNEME_ASSESSMENT");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(assessment.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("deleteAssessmentTx: db write failed");
 		}
 	}
 
 	/**
 	 * Insert a new assessment.
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void insertAssessment(final AssessmentImpl assessment)
 	{
 		this.sqlService.transact(new Runnable()
 		{
 			public void run()
 			{
 				insertAssessmentTx(assessment);
 			}
 		}, "insertAssessment: " + assessment.getId());
 	}
 
 	/**
 	 * Insert a new assessment access (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected abstract void insertAssessmentAccessTx(AssessmentImpl assessment, AssessmentAccessImpl access);
 
 	/**
 	 * Insert a new assessment's parts (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 * @param part
 	 *        The part.
 	 * @param detail
 	 *        The detail.
 	 */
 	protected abstract void insertAssessmentPartDetailTx(AssessmentImpl assessment, PartImpl part, PartDetailImpl deatil);
 
 	/**
 	 * Insert a new assessment's parts (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 * @param part
 	 *        the part.
 	 */
 	protected abstract void insertAssessmentPartTx(AssessmentImpl assessment, Part part);
 
 	/**
 	 * Insert a new assessment (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected abstract void insertAssessmentTx(AssessmentImpl assessment);
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected void makeLiveTx(Assessment assessment)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE MNEME_ASSESSMENT");
 		sql.append(" SET LIVE='1'");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(assessment.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields, null))
 		{
 			throw new RuntimeException("makeLiveTx: db write failed");
 		}
 	}
 
 	/**
 	 * Read an assessment
 	 * 
 	 * @param id
 	 *        The assessment id.
 	 * @return The assessment.
 	 */
 	protected AssessmentImpl readAssessment(String id)
 	{
 		String where = "WHERE A.ID = ?";
 		Object[] fields = new Object[1];
 		fields[0] = Long.valueOf(id);
 		List<AssessmentImpl> rv = readAssessments(where, null, fields);
 		if (rv.size() > 0)
 		{
 			return rv.get(0);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Read a selection of assessments
 	 * 
 	 * @param where
 	 *        The where clause
 	 * @param order
 	 *        The order clause
 	 * @param fields
 	 *        The bind variables.
 	 * @return The assessments.
 	 */
 	protected List<AssessmentImpl> readAssessments(String where, String order, Object[] fields)
 	{
 		final List<AssessmentImpl> rv = new ArrayList<AssessmentImpl>();
 		final Map<String, AssessmentImpl> assessments = new HashMap<String, AssessmentImpl>();
 
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT A.ARCHIVED, A.CONTEXT, A.CREATED_BY_DATE, A.CREATED_BY_USER,");
 		sql.append(" A.DATES_ACCEPT_UNTIL, A.DATES_ARCHIVED, A.DATES_DUE, A.DATES_OPEN,");
 		sql.append(" A.GRADING_ANONYMOUS, A.GRADING_AUTO_RELEASE, A.GRADING_GRADEBOOK, A.GRADING_REJECTED, A.FORMAL_EVAL, A.RESULTS_EMAIL,");
 		sql.append(" A.RESULTS_SENT, A.HONOR_PLEDGE, A.ID, A.LIVE, A.LOCKED, A.MINT, A.MODIFIED_BY_DATE, A.MODIFIED_BY_USER,");
 		sql.append(" A.PARTS_CONTINUOUS, A.PARTS_SHOW_PRES, A.PASSWORD, A.PRESENTATION_TEXT,");
 		sql.append(" A.PUBLISHED, A.QUESTION_GROUPING, A.RANDOM_ACCESS,");
 		sql.append(" A.REVIEW_DATE, A.REVIEW_SHOW_CORRECT, A.REVIEW_SHOW_FEEDBACK, A.REVIEW_TIMING,");
 		sql.append(" A.SHOW_HINTS, A.SHOW_MODEL_ANSWER, A.SUBMIT_PRES_TEXT, A.TIME_LIMIT, A.TITLE, A.TRIES, A.TYPE, A.POOL, A.NEEDSPOINTS");
 		sql.append(" FROM MNEME_ASSESSMENT A ");
 		sql.append(where);
 		if (order != null) sql.append(order);
 
 		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
 		{
 			public Object readSqlResultRecord(ResultSet result)
 			{
 				try
 				{
 					int i = 1;
 					AssessmentImpl assessment = newAssessment();
 					assessment.initArchived(SqlHelper.readBoolean(result, i++));
 					assessment.setContext(SqlHelper.readString(result, i++));
 					assessment.getCreatedBy().setDate(SqlHelper.readDate(result, i++));
 					assessment.getCreatedBy().setUserId(SqlHelper.readString(result, i++));
 					assessment.getDates().setAcceptUntilDate(SqlHelper.readDate(result, i++));
 					((AssessmentDatesImpl) assessment.getDates()).archived = SqlHelper.readDate(result, i++);
 					((AssessmentDatesImpl) assessment.getDates()).initDueDate(SqlHelper.readDate(result, i++));
 					assessment.getDates().setOpenDate(SqlHelper.readDate(result, i++));
 					assessment.getGrading().setAnonymous(SqlHelper.readBoolean(result, i++));
 					((AssessmentGradingImpl) (assessment.getGrading())).initAutoRelease(SqlHelper.readBoolean(result, i++));
 					((AssessmentGradingImpl) (assessment.getGrading())).initGradebookIntegration(SqlHelper.readBoolean(result, i++));
 					((AssessmentGradingImpl) (assessment.getGrading())).initGradebookRejectedAssessment(SqlHelper.readBoolean(result, i++));
 					assessment.initFormalCourseEval(SqlHelper.readBoolean(result, i++));
 					assessment.initResultsEmail(SqlHelper.readString(result, i++));
 					assessment.initResultsSent(SqlHelper.readDate(result, i++));
 					assessment.setRequireHonorPledge(SqlHelper.readBoolean(result, i++));
 					assessment.initId(SqlHelper.readId(result, i++));
 					assessment.initLive(SqlHelper.readBoolean(result, i++));
 					assessment.initLocked(SqlHelper.readBoolean(result, i++));
 					assessment.initMint(SqlHelper.readBoolean(result, i++));
 					assessment.getModifiedBy().setDate(SqlHelper.readDate(result, i++));
 					assessment.getModifiedBy().setUserId(SqlHelper.readString(result, i++));
 					assessment.getParts().setContinuousNumbering(SqlHelper.readBoolean(result, i++));
 					assessment.getParts().setShowPresentation(SqlHelper.readBoolean(result, i++));
 					assessment.getPassword().setPassword(SqlHelper.readString(result, i++));
 					assessment.getPresentation().setText(SqlHelper.readString(result, i++));
 					assessment.initPublished(SqlHelper.readBoolean(result, i++));
 					assessment.setQuestionGrouping(QuestionGrouping.valueOf(SqlHelper.readString(result, i++)));
 					assessment.setRandomAccess(SqlHelper.readBoolean(result, i++));
 					assessment.getReview().setDate(SqlHelper.readDate(result, i++));
 					assessment.getReview().setShowCorrectAnswer(readReviewShowCorrect(result, i++));
 					assessment.getReview().setShowFeedback(SqlHelper.readBoolean(result, i++));
 					assessment.getReview().setTiming(ReviewTiming.valueOf(SqlHelper.readString(result, i++)));
 					assessment.setShowHints(SqlHelper.readBoolean(result, i++));
 					assessment.initShowModelAnswer(SqlHelper.readBoolean(result, i++));
 					assessment.getSubmitPresentation().setText(SqlHelper.readString(result, i++));
 					assessment.setTimeLimit(SqlHelper.readLong(result, i++));
 					assessment.initTitle(SqlHelper.readString(result, i++));
 					assessment.setTries(SqlHelper.readInteger(result, i++));
 					assessment.initType(AssessmentType.valueOf(SqlHelper.readString(result, i++)));
 					assessment.initPool(SqlHelper.readId(result, i++));
 					assessment.initNeedsPoints(SqlHelper.readBoolean(result, i++));
 
 					rv.add(assessment);
 					assessments.put(assessment.getId(), assessment);
 
 					return null;
 				}
 				catch (SQLException e)
 				{
 					M_log.warn("readAssessments(assessment): " + e);
 					return null;
 				}
 			}
 		});
 
 		// read all the parts for these assessments
 		sql = new StringBuilder();
 		sql.append("SELECT P.ASSESSMENT_ID, P.ID, P.PRESENTATION_TEXT, P.TITLE, P.TYPE, P.RANDOMIZE");
 		sql.append(" FROM MNEME_ASSESSMENT_PART P");
 		sql.append(" JOIN MNEME_ASSESSMENT A ON P.ASSESSMENT_ID=A.ID ");
 		sql.append(where);
 		sql.append(" ORDER BY P.ASSESSMENT_ID ASC, P.SEQUENCE ASC");
 		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
 		{
 			public Object readSqlResultRecord(ResultSet result)
 			{
 				try
 				{
 					String aid = SqlHelper.readId(result, 1);
 					AssessmentImpl a = assessments.get(aid);
 					String type = result.getString(5);
 					Part part = a.getParts().addPart();
 					part.setRandomize(SqlHelper.readBoolean(result, 6));
 
 					// old types M and D were not setting randomize - D should be random, M not
 					if ("M".equals(type))
 					{
 						part.setRandomize(Boolean.FALSE);
 					}
 					else if ("D".equals(type))
 					{
 						part.setRandomize(Boolean.TRUE);
 					}
 
 					((PartImpl) part).initId(SqlHelper.readId(result, 2));
 					part.getPresentation().setText(SqlHelper.readString(result, 3));
 					part.setTitle(SqlHelper.readString(result, 4));
 
 					((PartImpl) part).clearChanged();
 
 					return null;
 				}
 				catch (SQLException e)
 				{
 					M_log.warn("readAssessments(parts): " + e);
 					return null;
 				}
 			}
 		});
 
 		// read all the part details for these assessments
 		sql = new StringBuilder();
 		sql.append("SELECT P.ASSESSMENT_ID, P.NUM_QUESTIONS_SEQ, P.ORIG_PID, P.ORIG_QID, P.PART_ID,");
 		sql.append(" P.POOL_ID, P.QUESTION_ID, P.ID, P.SEQ, P.POINTS");
 		sql.append(" FROM MNEME_ASSESSMENT_PART_DETAIL P");
 		sql.append(" JOIN MNEME_ASSESSMENT A ON P.ASSESSMENT_ID=A.ID ");
 		sql.append(where);
 		sql.append(" ORDER BY P.ASSESSMENT_ID ASC, P.SEQ ASC");
 		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
 		{
 			public Object readSqlResultRecord(ResultSet result)
 			{
 				try
 				{
 					String aid = SqlHelper.readId(result, 1);
 					AssessmentImpl a = assessments.get(aid);
 					String pid = SqlHelper.readId(result, 5);
 					Part p = a.getParts().getPart(pid);
 
 					Integer numQuestions = SqlHelper.readInteger(result, 2);
 					String origPoolId = SqlHelper.readId(result, 3);
 					String origQid = SqlHelper.readId(result, 4);
 					String poolId = SqlHelper.readId(result, 6);
 					String questionId = SqlHelper.readId(result, 7);
 					String detailId = SqlHelper.readId(result, 8);
 					Integer seq = SqlHelper.readInteger(result, 9);
 					Float points = SqlHelper.readFloat(result, 10);
 					PartDetail detail = null;
 					if (questionId != null)
 					{
 						detail = ((PartImpl) p).initPick(detailId, questionId, origQid, points);
 					}
 					else if (poolId != null)
 					{
 						detail = ((PartImpl) p).initDraw(detailId, poolId, origPoolId, numQuestions, points);
 					}
 					else
 					{
 						M_log.warn("no pool no question: part: " + pid);
 					}
 					if (detail != null)
 					{
 						((PartDetailImpl) detail).initSeq(seq.intValue());
 					}
 
 					return null;
 				}
 				catch (SQLException e)
 				{
 					M_log.warn("readAssessments(part details): " + e);
 					return null;
 				}
 			}
 		});
 
 		// read all the access for these assessments
 		sql = new StringBuilder();
 		sql.append("SELECT X.ASSESSMENT_ID, X.DATES_ACCEPT_UNTIL, X.DATES_DUE, X.DATES_OPEN, X.ID,");
 		sql.append(" X.OVERRIDE_ACCEPT_UNTIL, X.OVERRIDE_DUE, X.OVERRIDE_OPEN, X.OVERRIDE_PASSWORD,");
 		sql.append(" X.OVERRIDE_TIME_LIMIT, X.OVERRIDE_TRIES, X.PASSWORD, X.TIME_LIMIT, X.TRIES, X.USERS");
 		sql.append(" FROM MNEME_ASSESSMENT_ACCESS X");
 		sql.append(" JOIN MNEME_ASSESSMENT A ON X.ASSESSMENT_ID=A.ID ");
 		sql.append(where);
 		sql.append(" ORDER BY X.ASSESSMENT_ID ASC, X.ID ASC");
 
 		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
 		{
 			public Object readSqlResultRecord(ResultSet result)
 			{
 				try
 				{
 					String aid = SqlHelper.readId(result, 1);
 					AssessmentImpl a = assessments.get(aid);
 					AssessmentAccessImpl access = (AssessmentAccessImpl) a.getSpecialAccess().addAccess();
 
 					access.initAcceptUntilDate(SqlHelper.readDate(result, 2));
 					access.initDueDate(SqlHelper.readDate(result, 3));
 					access.initOpenDate(SqlHelper.readDate(result, 4));
 					access.initId(SqlHelper.readId(result, 5));
 					access.initOverrideAcceptUntilDate(SqlHelper.readBoolean(result, 6));
 					access.initOverrideDueDate(SqlHelper.readBoolean(result, 7));
 					access.initOverrideOpenDate(SqlHelper.readBoolean(result, 8));
 					access.initOverridePassword(SqlHelper.readBoolean(result, 9));
 					access.initOverrideTimeLimit(SqlHelper.readBoolean(result, 10));
 					access.initOverrideTries(SqlHelper.readBoolean(result, 11));
 					access.initPasswordValue(SqlHelper.readString(result, 12));
 					access.initTimeLimit(SqlHelper.readLong(result, 13));
 					access.initTries(SqlHelper.readInteger(result, 14));
 					access.initUsers(Arrays.asList(SqlHelper.decodeStringArray(SqlHelper.readString(result, 15))));
 
 					a.changed.clearChanged();
 
 					return null;
 				}
 				catch (SQLException e)
 				{
 					M_log.warn("readAssessments(access): " + e);
 					return null;
 				}
 			}
 		});
 
 		// clear changed for the assessments
 		for (AssessmentImpl a : rv)
 		{
 			a.clearChanged();
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Read a review show correct value from the db
 	 * 
 	 * @param results
 	 *        The result set.
 	 * @param index
 	 *        The index.
 	 * @return The ReviewShowCorrect value.
 	 * @throws SQLException
 	 */
 	protected ReviewShowCorrect readReviewShowCorrect(ResultSet result, int index) throws SQLException
 	{
 		String s = result.getString(index);
 		if (s == null) return null;
 		if (s.equals("0")) return ReviewShowCorrect.no;
 		if (s.equals("C")) return ReviewShowCorrect.correct_only;
 		return ReviewShowCorrect.yes;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected abstract void removeDependencyTx(Pool pool);
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected abstract void removeDependencyTx(Question question);
 
 	/**
 	 * Update the assessment email results sent date (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void setResultsSentTx(String id, Date date)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE MNEME_ASSESSMENT SET RESULTS_SENT=? WHERE ID=?");
 
 		Object[] fields = new Object[2];
 		int i = 0;
 		fields[i++] = (date == null) ? null : date.getTime();
 		fields[i++] = Long.valueOf(id);
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("setResultsSentTx: dbWrite failed");
 		}
 	}
 
 	/**
 	 * Update an existing assessment.
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void updateAssessment(final AssessmentImpl assessment)
 	{
 		this.sqlService.transact(new Runnable()
 		{
 			public void run()
 			{
 				updateAssessmentTx(assessment);
 			}
 		}, "updateAssessment: " + assessment.getId());
 	}
 
 	/**
 	 * Update an existing assessment access record (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void updateAssessmentAccessTx(AssessmentAccessImpl access)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE MNEME_ASSESSMENT_ACCESS SET");
 		sql.append(" DATES_ACCEPT_UNTIL=?, DATES_DUE=?, DATES_OPEN=?,");
 		sql.append(" OVERRIDE_ACCEPT_UNTIL=?, OVERRIDE_DUE=?, OVERRIDE_OPEN=?, OVERRIDE_PASSWORD=?,");
 		sql.append(" OVERRIDE_TIME_LIMIT=?, OVERRIDE_TRIES=?, PASSWORD=?, TIME_LIMIT=?, TRIES=?, USERS=?");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[14];
 		int i = 0;
 		fields[i++] = (access.getAcceptUntilDate() == null) ? null : access.getAcceptUntilDate().getTime();
 		fields[i++] = (access.getDueDate() == null) ? null : access.getDueDate().getTime();
 		fields[i++] = (access.getOpenDate() == null) ? null : access.getOpenDate().getTime();
 		fields[i++] = access.getOverrideAcceptUntilDate() ? "1" : "0";
 		fields[i++] = access.getOverrideDueDate() ? "1" : "0";
 		fields[i++] = access.getOverrideOpenDate() ? "1" : "0";
 		fields[i++] = access.getOverridePassword() ? "1" : "0";
 		fields[i++] = access.getOverrideTimeLimit() ? "1" : "0";
 		fields[i++] = access.getOverrideTries() ? "1" : "0";
 		fields[i++] = access.getPassword().getPassword();
 		fields[i++] = access.getTimeLimit();
 		fields[i++] = access.getTries();
 		fields[i++] = SqlHelper.encodeStringArray(access.getUsers().toArray(new String[access.getUsers().size()]));
 		fields[i++] = Long.valueOf(access.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("updateAssessmentAccessTx: dbInsert failed");
 		}
 	}
 
 	/**
 	 * Update an existing assessment access record (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 * @param part
 	 * @param detail
 	 */
 	protected void updateAssessmentPartDetailTx(AssessmentImpl assessment, PartImpl part, PartDetailImpl detail)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE MNEME_ASSESSMENT_PART_DETAIL SET");
 		sql.append(" ASSESSMENT_ID=?, NUM_QUESTIONS_SEQ=?, ORIG_PID=?,");
 		sql.append(" ORIG_QID=?, PART_ID=?, POOL_ID=?, QUESTION_ID=?, SEQ=?, POINTS=?");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[10];
 		int i = 0;
 		fields[i++] = Long.valueOf(assessment.getId());
 
 		if (detail instanceof QuestionPick)
 		{
 			QuestionPick pick = (QuestionPick) detail;
 
 			fields[i++] = Integer.valueOf(1);
 			fields[i++] = null;
 			fields[i++] = (pick.getOrigQuestionId() == null) ? null : Long.valueOf(pick.getOrigQuestionId());
 			fields[i++] = Long.valueOf(part.getId());
 			fields[i++] = null;
 			fields[i++] = Long.valueOf(pick.getQuestionId());
 			fields[i++] = Integer.valueOf(((PartDetailImpl) detail).getSeq());
 			fields[i++] = detail.getPoints();
 		}
 
 		else if (detail instanceof PoolDraw)
 		{
 			PoolDraw draw = (PoolDraw) detail;
 
 			fields[i++] = Integer.valueOf(draw.getNumQuestions());
 			fields[i++] = draw.getOrigPoolId() == null ? null : Long.valueOf(draw.getOrigPoolId());
 			fields[i++] = null;
 			fields[i++] = Long.valueOf(part.getId());
 			fields[i++] = Long.valueOf(draw.getPoolId());
 			fields[i++] = null;
 			fields[i++] = Integer.valueOf(((PartDetailImpl) detail).getSeq());
 			fields[i++] = detail.getPoints();
 		}
 
 		fields[i++] = Long.valueOf(detail.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("updateAssessmentPartDetailTx: dbInsert failed");
 		}
 	}
 
 	/**
 	 * Update an assessment part (transaction code).
 	 * 
 	 * @param part
 	 *        the part.
 	 */
 	protected void updateAssessmentPartTx(AssessmentImpl assessment, Part part)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE MNEME_ASSESSMENT_PART SET");
 		sql.append(" PRESENTATION_TEXT=?, SEQUENCE=?, TITLE=?, RANDOMIZE=?, TYPE=?");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[6];
 		fields[0] = part.getPresentation().getText();
 		fields[1] = part.getOrdering().getPosition();
 		fields[2] = part.getTitle();
 		fields[3] = part.getRandomize() ? "1" : "0";
 		fields[4] = "H";
 		fields[5] = Long.valueOf(part.getId());
 
 		if (!this.sqlService.dbWrite(null, sql.toString(), fields))
 		{
 			throw new RuntimeException("updateAssessmentPartTx: dbInsert failed");
 		}
 
 		// details
 		for (PartDetail detail : part.getDetails())
 		{
 			if (detail.getId() == null)
 			{
 				insertAssessmentPartDetailTx(assessment, (PartImpl) part, (PartDetailImpl) detail);
 			}
 			else if (((PartDetailImpl) detail).getChanged())
 			{
 				updateAssessmentPartDetailTx(assessment, (PartImpl) part, (PartDetailImpl) detail);
 			}
 		}
 		for (PartDetail detail : ((PartImpl) part).getDeleted())
 		{
 			deleteAssessmentPartDetailTx(detail);
 		}
 	}
 
 	/**
 	 * Update an existing assessment (transaction code).
 	 * 
 	 * @param assessment
 	 *        The assessment.
 	 */
 	protected void updateAssessmentTx(AssessmentImpl assessment)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE MNEME_ASSESSMENT SET");
 		sql.append(" ARCHIVED=?, CONTEXT=?,");
 		sql.append(" DATES_ACCEPT_UNTIL=?, DATES_ARCHIVED=?, DATES_DUE=?, DATES_OPEN=?,");
 		sql.append(" GRADING_ANONYMOUS=?, GRADING_AUTO_RELEASE=?, GRADING_GRADEBOOK=?, GRADING_REJECTED=?, FORMAL_EVAL=?, RESULTS_EMAIL=?,");
 		sql.append(" RESULTS_SENT=?, HONOR_PLEDGE=?, LIVE=?, LOCKED=?, MINT=?, MODIFIED_BY_DATE=?, MODIFIED_BY_USER=?,");
 		sql.append(" PARTS_CONTINUOUS=?, PARTS_SHOW_PRES=?, PASSWORD=?, PRESENTATION_TEXT=?,");
 		sql.append(" PUBLISHED=?, QUESTION_GROUPING=?, RANDOM_ACCESS=?,");
 		sql.append(" REVIEW_DATE=?, REVIEW_SHOW_CORRECT=?, REVIEW_SHOW_FEEDBACK=?, REVIEW_TIMING=?,");
 		sql.append(" SHOW_HINTS=?, SHOW_MODEL_ANSWER=?, SUBMIT_PRES_TEXT=?, TIME_LIMIT=?, TITLE=?, TRIES=?, TYPE=?, POOL=?, NEEDSPOINTS=?");
 		sql.append(" WHERE ID=?");
 
 		Object[] fields = new Object[40];
 		int i = 0;
 		fields[i++] = assessment.getArchived() ? "1" : "0";
 		fields[i++] = assessment.getContext();
 		fields[i++] = (assessment.getDates().getAcceptUntilDate() == null) ? null : assessment.getDates().getAcceptUntilDate().getTime();
 		fields[i++] = (assessment.getDates().getArchivedDate() == null) ? null : assessment.getDates().getArchivedDate().getTime();
 		fields[i++] = (assessment.getDates().getDueDate() == null) ? null : assessment.getDates().getDueDate().getTime();
 		fields[i++] = (assessment.getDates().getOpenDate() == null) ? null : assessment.getDates().getOpenDate().getTime();
 		fields[i++] = assessment.getGrading().getAnonymous() ? "1" : "0";
 		fields[i++] = assessment.getGrading().getAutoRelease() ? "1" : "0";
 		fields[i++] = assessment.getGrading().getGradebookIntegration() ? "1" : "0";
 		fields[i++] = assessment.getGrading().getGradebookRejectedAssessment() ? "1" : "0";
 		fields[i++] = assessment.getFormalCourseEval() ? "1" : "0";
 		fields[i++] = assessment.getResultsEmail();
 		fields[i++] = (assessment.getResultsSent() == null) ? null : assessment.getResultsSent().getTime();
 		fields[i++] = assessment.getRequireHonorPledge() ? "1" : "0";
 		fields[i++] = assessment.getIsLive() ? "1" : "0";
 		fields[i++] = assessment.getIsLocked() ? "1" : "0";
 		fields[i++] = assessment.getMint() ? "1" : "0";
 		fields[i++] = assessment.getModifiedBy().getDate().getTime();
 		fields[i++] = assessment.getModifiedBy().getUserId();
 		fields[i++] = assessment.getParts().getContinuousNumbering() ? "1" : "0";
 		fields[i++] = ((AssessmentPartsImpl) assessment.getParts()).showPresentation == null ? null
 				: (((AssessmentPartsImpl) assessment.getParts()).showPresentation ? "1" : "0");
 		fields[i++] = assessment.getPassword().getPassword();
 		fields[i++] = assessment.getPresentation().getText();
 		fields[i++] = assessment.getPublished() ? "1" : "0";
 		fields[i++] = assessment.getQuestionGrouping().toString();
 		fields[i++] = assessment.getRandomAccess() ? "1" : "0";
 		fields[i++] = (assessment.getReview().getDate() == null) ? null : assessment.getReview().getDate().getTime();
 		fields[i++] = assessment.getReview().getShowCorrectAnswer().equals(ReviewShowCorrect.yes) ? "1" : (assessment.getReview()
 				.getShowCorrectAnswer().equals(ReviewShowCorrect.no) ? "0" : "C");
 		fields[i++] = assessment.getReview().getShowFeedback() ? "1" : "0";
 		fields[i++] = assessment.getReview().getTiming().toString();
 		fields[i++] = assessment.getShowHints() ? "1" : "0";
 		fields[i++] = assessment.getShowModelAnswer() ? "1" : "0";
 		fields[i++] = assessment.getSubmitPresentation().getText();
 		fields[i++] = assessment.getTimeLimit();
 		fields[i++] = assessment.getTitle();
 		fields[i++] = assessment.getTries();
 		fields[i++] = assessment.getType().toString();
 		fields[i++] = ((AssessmentImpl) assessment).poolId == null ? null : Long.valueOf(((AssessmentImpl) assessment).poolId);
 		fields[i++] = assessment.getNeedsPoints() == null ? null : (assessment.getNeedsPoints() ? "1" : "0");
 		fields[i++] = Long.valueOf(assessment.getId());
 
 		if (!this.sqlService.dbWrite(sql.toString(), fields))
 		{
 			throw new RuntimeException("updateAssessmentTx: db write failed");
 		}
 
 		// access
 		for (AssessmentAccess access : assessment.getSpecialAccess().getAccess())
 		{
 			if (access.getId() == null)
 			{
 				insertAssessmentAccessTx(assessment, (AssessmentAccessImpl) access);
 			}
 			else if (((AssessmentAccessImpl) access).getChanged())
 			{
 				updateAssessmentAccessTx((AssessmentAccessImpl) access);
 			}
 		}
 		for (AssessmentAccess access : ((AssessmentSpecialAccessImpl) assessment.getSpecialAccess()).getDeleted())
 		{
 			deleteAssessmentAccessTx(access);
 		}
 
 		// parts
 		for (Part part : assessment.getParts().getParts())
 		{
 			if (part.getId() == null)
 			{
 				insertAssessmentPartTx(assessment, part);
 			}
 			else if (((PartImpl) part).getChanged())
 			{
 				updateAssessmentPartTx(assessment, (PartImpl) part);
 			}
 		}
 		for (Part part : ((AssessmentPartsImpl) assessment.getParts()).getDeleted())
 		{
 			deleteAssessmentPartTx(part);
 		}
 	}
 }
