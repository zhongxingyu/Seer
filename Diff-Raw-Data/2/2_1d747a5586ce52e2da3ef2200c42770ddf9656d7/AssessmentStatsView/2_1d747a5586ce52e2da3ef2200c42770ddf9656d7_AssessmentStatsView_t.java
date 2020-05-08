 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
  * licensed under the Educational Community License, Version 2.0
  *
  *       http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.mneme.tool;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.ambrosia.api.Context;
 import org.etudes.ambrosia.util.ControllerImpl;
 import org.etudes.mneme.api.Assessment;
 import org.etudes.mneme.api.AssessmentService;
 import org.etudes.mneme.api.Submission;
 import org.etudes.mneme.api.SubmissionService;
 import org.sakaiproject.tool.api.ToolManager;
 import org.sakaiproject.util.Web;
 
 /**
  * The /assessment_stats view for the mneme tool.
  */
 public class AssessmentStatsView extends ControllerImpl
 {
 	/** Our log. */
 	private static Log M_log = LogFactory.getLog(AssessmentStatsView.class);
 
 	/** Assessment service. */
 	protected AssessmentService assessmentService = null;
 
 	/** Submission Service */
 	protected SubmissionService submissionService = null;
 
 	/** Dependency: ToolManager */
 	protected ToolManager toolManager = null;
 
 	/**
 	 * Shutdown.
 	 */
 	public void destroy()
 	{
 		M_log.info("destroy()");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
 	{
 		// [2]sort for /grades, [3]aid
 		if (params.length != 4) throw new IllegalArgumentException();
 
 		// grades sort parameter
 		String gradesSortCode = params[2];
 		context.put("sort_grades", gradesSortCode);
 
 		Assessment assessment = this.assessmentService.getAssessment(params[3]);
 		if (assessment == null)
 		{
 			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
 			return;
 		}
 
 		// check for user permission to access the submission for grading
 		if (!this.submissionService.allowEvaluate(assessment.getContext()))
 		{
 			// redirect to error
 			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
 			return;
 		}
 
 		context.put("assessment", assessment);
 
 		// collect all the submissions for the assessment
 		List<Submission> submissions = this.submissionService.findAssessmentSubmissions(assessment,
 				SubmissionService.FindAssessmentSubmissionsSort.sdate_a, Boolean.FALSE, null, null, null);
 		context.put("submissions", submissions);
 
 		computePercentComplete(submissions, context);
 
 		uiService.render(ui, context);
 	}
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 		super.init();
 		M_log.info("init()");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
 	{
 		// [2]sort for /grades, [3]aid
 		if (params.length != 4) throw new IllegalArgumentException();
 
 		// read form
 		String destination = this.uiService.decode(req, context);
 
 		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
 	}
 
 	/**
 	 * @param assessmentService
 	 *        the assessmentService to set
 	 */
 	public void setAssessmentService(AssessmentService assessmentService)
 	{
 		this.assessmentService = assessmentService;
 	}
 
 	/**
 	 * @param submissionService
 	 *        the submissionService to set
 	 */
 	public void setSubmissionService(SubmissionService submissionService)
 	{
 		this.submissionService = submissionService;
 	}
 
 	/**
 	 * @param toolManager
 	 *        the toolManager to set
 	 */
 	public void setToolManager(ToolManager toolManager)
 	{
 		this.toolManager = toolManager;
 	}
 
 	/**
 	 * Compute the percent complete for the set of submission.
 	 * 
 	 * @param submissions
 	 *        The submissions.
 	 * @param context
 	 *        The context.
 	 */
 	protected void computePercentComplete(List<Submission> submissions, Context context)
 	{
 		Set<String> users = new HashSet<String>();
 		int complete = 0;
 		for (Submission s : submissions)
 		{
 			// if we have never seen this user before
 			if (!users.contains(s.getUserId()))
 			{
 				// if non-phantom and complete, count it
 				if ((!s.getIsPhantom()) && s.getIsComplete())
 				{
 					complete++;
 				}
 
 				users.add(s.getUserId());
 			}
 		}
 
 		// percent
		int pct = users.isEmpty() ? 0 : ((complete * 100) / users.size());
 
 		context.put("complete-percent", Integer.valueOf(pct));
 		context.put("complete-complete", Integer.valueOf(complete));
 		context.put("complete-total", Integer.valueOf(users.size()));
 	}
 }
