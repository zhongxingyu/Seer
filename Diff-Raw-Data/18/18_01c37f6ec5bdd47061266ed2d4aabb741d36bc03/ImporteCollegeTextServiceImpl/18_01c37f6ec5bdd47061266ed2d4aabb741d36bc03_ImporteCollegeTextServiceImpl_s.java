 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/etudes/mneme/tags/2.1.10/mneme-impl/impl/src/java/org/etudes/mneme/impl/ImportTextServiceImpl.java $
  * $Id: ImportTextServiceImpl.java 62575 2009-08-13 00:07:27Z murthy@etudes.org $
  ***********************************************************************************
  *
  * Copyright (c) 2012 Etudes, Inc.
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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.mneme.api.Assessment;
 import org.etudes.mneme.api.AssessmentPermissionException;
 import org.etudes.mneme.api.AssessmentPolicyException;
 import org.etudes.mneme.api.AssessmentService;
 import org.etudes.mneme.api.AssessmentType;
 import org.etudes.mneme.api.AttachmentService;
 import org.etudes.mneme.api.GradesService;
 import org.etudes.mneme.api.ImporteCollegeTextService;
 import org.etudes.mneme.api.Part;
 import org.etudes.mneme.api.Pool;
 import org.etudes.mneme.api.PoolService;
 import org.etudes.mneme.api.Question;
 import org.etudes.mneme.api.QuestionPick;
 import org.etudes.mneme.api.QuestionService;
 import org.etudes.mneme.api.SecurityService;
 import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.event.api.EventTrackingService;
 import org.sakaiproject.i18n.InternationalizedMessages;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.util.StringUtil;
 import org.sakaiproject.util.Validator;
 
 /**
  * <p>
  * ImportQtiServiceImpl implements ImportQtiService
  * </p>
  */
 public class ImporteCollegeTextServiceImpl implements ImporteCollegeTextService
 {
 	/** Our logger. */
 	private static Log M_log = LogFactory.getLog(ImporteCollegeTextServiceImpl.class);
 
 	/** Dependency: AssessmentService */
 	protected AssessmentService assessmentService = null;
 
 	/** Dependency: AttachmentService */
 	protected AttachmentService attachmentService = null;
 
 	/** Dependency: AuthzGroupService */
 	protected AuthzGroupService authzGroupService = null;
 
 	/** Messages bundle name. */
 	protected String bundle = null;
 
 	/** Dependency: EntityManager */
 	protected EntityManager entityManager = null;
 
 	/** Dependency: EventTrackingService */
 	protected EventTrackingService eventTrackingService = null;
 
 	/** Dependency: GradesService */
 	protected GradesService gradesService = null;
 
 	/** Messages. */
 	protected transient InternationalizedMessages messages = null;
 
 	/** Dependency: PoolService */
 	protected PoolService poolService = null;
 
 	/** Dependency: QuestionService */
 	protected QuestionService questionService = null;
 
 	/** Dependency: SecurityService */
 	protected SecurityService securityService = null;
 
 	/** Dependency: SessionManager */
 	protected SessionManager sessionManager = null;
 
 	/** Dependency: SiteService */
 	protected SiteService siteService = null;
 
 	/** Dependency: ThreadLocalManager. */
 	protected ThreadLocalManager threadLocalManager = null;
 
 	/**
 	 * Returns to uninitialized state.
 	 */
 	public void destroy()
 	{
 		M_log.info("destroy()");
 	}
 
 	public void importQuestions(String context, Pool pool, String text) throws AssessmentPermissionException
 	{
 		if ((text == null) || (text.length() == 0)) return;
 
 		// replace any \r\n with just a \n
 		text = text.replaceAll("\r\n", "\n");
 
 		String title = "eCollege paste";
 		Float points = new Float("1");
 		
 		if (pool == null)
 		{
 			pool = this.poolService.newPool(context);
 			//read title from the first line ex: Unit 2: Week 2 - Quiz
 			String findTitle = text.substring(0, text.indexOf("\n"));
 			if (findTitle != null)
 			{
 				String[] titleParts = findTitle.split("[:-]");				
 				if (titleParts.length == 2 && titleParts[1] != null && titleParts[1].length() != 0 ) title = titleParts[1].trim();
 				else if (titleParts.length > 2) title = findTitle.substring(findTitle.indexOf(titleParts[1]));
 			}
 			pool.setTitle(title);
 			pool.setPointsEdit(points);
 			
 			// create assessment
 			Assessment assmt = assessmentService.newAssessment(context);
 			assmt.setType(AssessmentType.test);
 			assmt.setTitle(title);
 			
 			Part part = assmt.getParts().addPart();
 			
 			Pattern p_groups = Pattern.compile("Collapse[\\s]*Question(.*?)[\\n]*[\\t]*row[\\t]*Move[\\s]*Question", Pattern.CASE_INSENSITIVE
 					| Pattern.UNICODE_CASE | Pattern.DOTALL);
 			Matcher m = p_groups.matcher(text);
 
 			StringBuffer sb = new StringBuffer();
 			while (m.find())
 			{
 				String workOn = m.group(0);
 				String[] lines = workOn.split("[\\n]");
 				processECollegeTextGroup(pool, part, lines);
 				m.appendReplacement(sb, "");
 			}
 			m.appendTail(sb);
 
 			// remaining last text
 			if (sb != null && sb.length() != 0)
 			{
 				if (sb.indexOf("Collapse Question") != -1)
 				{
 					String workOn = sb.substring(sb.indexOf("Collapse Question"));
 					String[] lines = workOn.split("[\\n]");
 					processECollegeTextGroup(pool, part, lines);
 				}
 			}
 			
 			try
 			{				
 				assmt.getGrading().setGradebookIntegration(Boolean.TRUE);
 
 				if (assmt.getParts().getTotalPoints().floatValue() <= 0)
 				{
 					assmt.setNeedsPoints(Boolean.FALSE);
 				}
 			
 				assessmentService.saveAssessment(assmt);
 			}
 			catch (AssessmentPolicyException ep)
 			{
 	
 			}
 			this.poolService.savePool(pool);
 			
 		}
 	}
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 		// messages
 		if (this.bundle != null) this.messages = new ResourceLoader(this.bundle);
 
 		M_log.info("init()");
 	}
 
 	/**
 	 * Dependency: AssessmentService.
 	 * 
 	 * @param service
 	 *        The AssessmentService.
 	 */
 	public void setAssessmentService(AssessmentService service)
 	{
 		this.assessmentService = service;
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
 	 * Dependency: AuthzGroupService.
 	 * 
 	 * @param service
 	 *        The AuthzGroupService.
 	 */
 	public void setAuthzGroupService(AuthzGroupService service)
 	{
 		authzGroupService = service;
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
 	 * Dependency: EntityManager.
 	 * 
 	 * @param service
 	 *        The EntityManager.
 	 */
 	public void setEntityManager(EntityManager service)
 	{
 		entityManager = service;
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
 		gradesService = service;
 	}
 
 	/**
 	 * Set the PoolService.
 	 * 
 	 * @param service
 	 *        the PoolService.
 	 */
 	public void setPoolService(PoolService service)
 	{
 		this.poolService = service;
 	}
 
 	/**
 	 * Dependency: QuestionService.
 	 * 
 	 * @param service
 	 *        The QuestionService.
 	 */
 	public void setQuestionService(QuestionService service)
 	{
 		this.questionService = service;
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
 	 * Dependency: SiteService.
 	 * 
 	 * @param service
 	 *        The SiteService.
 	 */
 	public void setSiteService(SiteService service)
 	{
 		siteService = service;
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
 	 * Check if answer is textual or numeric
 	 * 
 	 * @param check
 	 *        String to check
 	 * @return false if numeric
 	 */
 	private boolean checkIfTextualorNumeric(String check)
 	{
 		try
 		{
 			Float.parseFloat(check.trim());
 			return false;
 
 		}
 		catch (NumberFormatException e)
 		{
 			return true;
 		}
 	}
 
 	/**
 	 * Get the feedback text starting from Instructor explanation: till Edit Question
 	 * @param lines
 	 * @param line
 	 * @param feedbackText
 	 * @return the line number till it read for feedback
 	 */
 	private int getFeedbackText(String[] lines, int line, String[] feedbackText)
 	{
 		String processText = lines[line].trim();
 		String[] parts = StringUtil.split(processText, "\t");
 		String qtext = "";
 		if(parts.length == 2) qtext = parts[1].trim();
 		while (true)
 		{
 			if (line + 1 >= lines.length) break;
 			String check = lines[(++line)];
 			if (check != null && check.length() != 0)
 			{
 				if (check.contains("Edit QuestionEdit")) break;
				qtext = qtext.concat(check);
 			}			
 		}
 		feedbackText[0] = qtext;
 		return line;
 	}
 	
 	/**
 	 * Gets the question text till it reaches Points statement
 	 * @param lines
 	 * @param line
 	 * @param questionText
 	 * @return the line number till it read the question text
 	 */
 	private int getQuestionText(String[] lines, int line, String[] questionText)
 	{
 		String processText = lines[line].trim();
 		String[] parts = StringUtil.split(processText, "\t");
 		String qtext = "";
 		if(parts.length == 4) qtext = parts[3].trim();
 		while (true)
 		{
 			if (line + 1 == lines.length) break;
 			String check = lines[(++line)];
 			if (check != null && check.length() != 0)
 			{
 				if (check.startsWith("Edit QuestionEdit")) break;
				if (check.startsWith("Points")) break;				
				qtext = qtext.concat(check);
 			}			
 		}
 		questionText[0] = qtext;
 		return line;
 	}
 	
 	/**
 	 * check which type of question and process it accordingly
 	 * 
 	 * @param pool
 	 *        pool where to add questions
 	 * @param lines
 	 *        text lines to parse and create a question
 	 * @throws AssessmentPermissionException
 	 */
 	protected void processECollegeTextGroup(Pool pool, Part part, String[] lines) throws AssessmentPermissionException
 	{
 		try
 		{
 			if (processTextMultipleChoice(pool, part, lines)) return;
 			if (processTextTrueFalse(pool, part, lines)) return;
 			if (processTextEssay(pool, part, lines)) return;
 			if (processTextFillBlanks(pool, part, lines)) return;
 			if (processTextMatch(pool, part, lines)) return;
 		}
 		catch (Exception e)
 		{
 			// do nothing			
 		}
 	}
 
 	/**
 	 * Process if it is recognized as an essay question.
 	 * 
 	 * @param pool
 	 *        The pool to hold the question.
 	 * @param lines
 	 *        The lines to process.
 	 * @return true if successfully recognized and processed, false if not.
 	 * 
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextEssay(Pool pool, Part part, String[] lines) throws AssessmentPermissionException
 	{
 		String firstLine = lines[0].trim();
 		if (!firstLine.startsWith("Collapse Question")) return false;
 		String[] partsType = StringUtil.split(firstLine, "\t");
 
 		String questionType = partsType[1].trim();
 		if (!("EQ".equals(questionType) || "SA".equals(questionType))) return false;
 
 		String text = "";
 
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:Essay");
 		EssayQuestionImpl e = (EssayQuestionImpl) (question.getTypeSpecificQuestion());
 
 		for (int line = 0; line < lines.length; line++)
 		{
 			String processText = lines[line].trim();
 			if (processText.length() == 0) continue;
 			if (processText.startsWith("Points")) continue;
 			if (processText.startsWith("Edit QuestionEdit")) break;
 
 			String[] parts = StringUtil.split(processText, "\t");
 			if (parts == null || parts.length == 0) return true;
 
 			if (processText.startsWith("Instructor Explanation:"))
 			{
 				String[] feedbackText = new String[]{""};
 				line = getFeedbackText(lines, line, feedbackText);
 				question.setFeedback(Validator.escapeHtml(feedbackText[0].trim()));
 				break;
 			}
 
 			// set the text
 			if (line == 0)
 			{
 				String[] questionText = new String[]{""};
 				line = getQuestionText(lines, line, questionText);
 				text = Validator.escapeHtml(questionText[0].trim());
 				question.getPresentation().setText(text);
 			}
 		}
 		e.setSubmissionType(EssayQuestionImpl.SubmissionType.both);
 
 		// survey
 		question.setIsSurvey(false);
 
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 
 		QuestionPick questionPick = part.addPickDetail(question);
 		questionPick.setPoints(Float.parseFloat("1"));	
 		
 		return true;
 	}
 
 	/**
 	 * Parses a Fill in the Blank type question and adds to the pool
 	 * 
 	 * @param pool
 	 *        pool where to add questions
 	 * @param lines
 	 *        text lines to parse and create a question
 	 * @return true if found Fill in the blank question
 	 * 
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextFillBlanks(Pool pool, Part part,String[] lines) throws AssessmentPermissionException
 	{
 		String firstLine = lines[0].trim();
 		if (!firstLine.startsWith("Collapse Question")) return false;
 		String[] partsType = StringUtil.split(firstLine, "\t");
 
 		String questionType = partsType[1].trim();
 		if (!("FB".equals(questionType))) return false;
 
 		String text = "";
 		StringBuffer buildAnswers = new StringBuffer("{");
 		boolean isResponseTextual = true;
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:FillBlanks");
 		FillBlanksQuestionImpl f = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());
 
 		// case sensitive
 		f.setCaseSensitive(Boolean.FALSE.toString());
 
 		// mutually exclusive
 		f.setAnyOrder(Boolean.FALSE.toString());
 
 		for (int line = 0; line < lines.length; line++)
 		{
 			String processText = lines[line].trim();
 			if (processText.length() == 0) continue;
 			if (processText.startsWith("Points")) continue;
 			if (processText.startsWith("Edit QuestionEdit")) break;
 
 			String[] parts = StringUtil.split(processText, "\t");
 			if (parts == null || parts.length == 0) return true;
 
 			if (processText.startsWith("Instructor Explanation:"))
 			{
 				String[] feedbackText = new String[]{ "" };
 				line = getFeedbackText(lines, line, feedbackText);
 				question.setFeedback(Validator.escapeHtml(feedbackText[0].trim()));
 				break;
 			}
 
 			// set the text
 			if (line == 0)
 			{
 				String[] questionText = new String[]{""};
 				line = getQuestionText(lines, line, questionText);
 				text = Validator.escapeHtml(questionText[0].trim());
 				question.getPresentation().setText(text);
 			}
 			else
 			{
 				String answer = parts[0];
 				answer = Validator.escapeHtml(answer);
 				buildAnswers.append(answer);
 				buildAnswers.append("|");
 				isResponseTextual = checkIfTextualorNumeric(answer);
 			}
 		}
 		buildAnswers.replace(buildAnswers.length() - 1, buildAnswers.length(), "}");
 		text = text.concat(buildAnswers.toString());
 		question.getPresentation().setText(text);
 		f.setText(text);
 
 		// text or numeric
 		f.setResponseTextual(Boolean.toString(isResponseTextual));
 
 		// survey
 		question.setIsSurvey(false);
 
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 
 		QuestionPick questionPick = part.addPickDetail(question);
 		questionPick.setPoints(Float.parseFloat("1"));	
 		
 		return true;
 
 	}
 
 	/**
 	 * parses a match type question and adds in the pool
 	 * 
 	 * @param pool
 	 *        pool where to add questions
 	 * @param lines
 	 *        text lines to parse and create a question
 	 * @return true if found match type question     
 	 *   
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextMatch(Pool pool, Part part,String[] lines) throws AssessmentPermissionException
 	{
 		String firstLine = lines[0].trim();
 		if (!firstLine.startsWith("Collapse Question")) return false;
 		String[] partsType = StringUtil.split(firstLine, "\t");
 	
 		String questionType = partsType[1].trim();
 		if (!("MT".equals(questionType))) return false;
 
 		String text = "";
 
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:Match");
 		MatchQuestionImpl m = (MatchQuestionImpl) (question.getTypeSpecificQuestion());
 
 		for (int line = 0; line < lines.length; line++)
 		{
 			String processText = lines[line].trim();
 			if (processText.length() == 0) continue;
 			if (processText.startsWith("Points")) continue;
 			if (processText.startsWith("Edit QuestionEdit")) break;
 
 			String[] parts = StringUtil.split(processText, "\t");
 			if (parts == null || parts.length == 0) return true;
 
 			if (processText.startsWith("Instructor Explanation:"))
 			{
 				String[] feedbackText = new String[]{ "" };
 				line = getFeedbackText(lines, line, feedbackText);
 				question.setFeedback(Validator.escapeHtml(feedbackText[0].trim()));
 				break;
 			}
 
 			// set the text
 			if (line == 0)
 			{
 				String[] questionText = new String[]{""};
 				line = getQuestionText(lines, line, questionText);
 				text = Validator.escapeHtml(questionText[0].trim());
 				question.getPresentation().setText(text);
 			}
 			else
 			{
 				String oneText = parts[0].trim();
 				if (oneText != null) oneText = Validator.escapeHtml(oneText);
 				String twoText = parts[2].trim();
 				if (twoText != null) twoText = Validator.escapeHtml(twoText);
 
 				if (oneText == null && twoText == null) continue;
 				if (oneText != null && oneText.length() != 0)
 					m.addPair(oneText, twoText);
 				else
 					m.setDistractor(twoText);
 			}
 		}
 
 		// survey
 		question.setIsSurvey(false);
 
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 
 		QuestionPick questionPick = part.addPickDetail(question);
 		questionPick.setPoints(Float.parseFloat("1"));	
 		
 		return true;
 	}
 
 	/**
 	 * parses a (single/multiple) multiple choice type question and adds in the pool
 	 * 
 	 * @param pool
 	 *        pool where to add questions
 	 * @param lines
 	 *        text lines to parse and create a question
 	 * @return true if found multiple choice type question    
 	 *    
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextMultipleChoice(Pool pool, Part part,String[] lines) throws AssessmentPermissionException
 	{
 		String firstLine = lines[0].trim();
 		if (!firstLine.startsWith("Collapse Question")) return false;
 		String[] partsType = StringUtil.split(firstLine, "\t");
 	
 		String questionType = partsType[1].trim();
 		if (!("MC".equals(questionType) || "MMC".equals(questionType))) return false;
 
 		String text = "";
 		List<String> choices = new ArrayList<String>();
 		Set<Integer> correctAnswers = new HashSet<Integer>();
 		int choiceNumber = -1;
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:MultipleChoice");
 		MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());
 
 		for (int line = 0; line < lines.length; line++)
 		{
 			String processText = lines[line].trim();
 			if (processText.length() == 0) continue;
 			if (processText.startsWith("Points")) continue;
 			if (processText.startsWith("Edit QuestionEdit")) break;
 
 			String[] parts = StringUtil.split(processText, "\t");
 			if (parts == null || parts.length == 0) return true;
 
 			if (processText.startsWith("Instructor Explanation:"))
 			{
 				String[] feedbackText = new String[]{ "" };
 				line = getFeedbackText(lines, line, feedbackText);
 				question.setFeedback(Validator.escapeHtml(feedbackText[0].trim()));
 				break;
 			}
 
 			// set the text
 			if (line == 0)
 			{
 				String[] questionText = new String[]{""};
 				line = getQuestionText(lines, line, questionText);
 				text = Validator.escapeHtml(questionText[0].trim());
 				question.getPresentation().setText(text);
 			}
 			else
 			{
 				String choiceText = parts[0];
 				choiceText = Validator.escapeHtml(choiceText);
 				choices.add(choiceText);
 				choiceNumber++;
 				boolean correctAnswer = false;
 				if (parts.length >= 2 && ("CORRECT ANSWER").equals(parts[1].trim()))
 				{
 					correctAnswer = true;
 					correctAnswers.add(choiceNumber);
 					if (parts.length >= 3 && (parts[parts.length - 1] != null)) question.setFeedback(parts[parts.length - 1].trim());
 				}
 
 				// check for feedback which starts with /n but is not an option item.
 				if (line + 1 == lines.length) break;
 				String check = lines[line + 1];
 				if (check != null)
 				{
 					if (check.startsWith("Edit QuestionEdit")) break;
 					String[] parts_check = StringUtil.split(check, "\t");
 					if (parts_check.length == 1)
 					{
 						if (correctAnswer) question.setFeedback(Validator.escapeHtml(check.trim()));
 						line++;
 					}
 				} // part of feedback check ends
 			}
 		}
 		// randomize
 		mc.setShuffleChoices(Boolean.toString(false));
 
 		// answer choices
 		mc.setAnswerChoices(choices);
 
 		// correct answer
 		mc.setCorrectAnswerSet(correctAnswers);
 
 		// single / multiple select
 		if (correctAnswers.size() == 1)
 			mc.setSingleCorrect(Boolean.TRUE.toString());
 		else
 			mc.setSingleCorrect(Boolean.FALSE.toString());
 
 		// survey
 		question.setIsSurvey(false);
 
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 
 		QuestionPick questionPick = part.addPickDetail(question);
 		questionPick.setPoints(Float.parseFloat("1"));		
 		
 		return true;
 	}
 
 	/**
 	 * parses a true false type question and adds in the pool
 	 * 
 	 * @param pool
 	 *        pool where to add questions
 	 * @param lines
 	 *        text lines to parse and create a question
 	 * @return true if found TF type question     
 	 *   
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextTrueFalse(Pool pool, Part part,String[] lines) throws AssessmentPermissionException
 	{
 		String firstLine = lines[0].trim();
 		if (!firstLine.startsWith("Collapse Question")) return false;
 		String[] partsType = StringUtil.split(firstLine, "\t");
 
 		String questionType = partsType[1].trim();
 		if (!("TF".equals(questionType))) return false;
 
 		String text = "";
 		boolean isTrue = false;
 
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
 		TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());
 
 		for (int line = 0; line < lines.length; line++)
 		{
 			String processText = lines[line].trim();
 			if (processText.length() == 0) continue;
 			if (processText.startsWith("Points")) continue;
 			if (processText.startsWith("Edit QuestionEdit")) break;
 
 			String[] parts = StringUtil.split(processText, "\t");
 			if (parts == null || parts.length == 0) return true;
 
 			if (processText.startsWith("Instructor Explanation:"))
 			{
 				String[] feedbackText = new String[]{ "" };
 				line = getFeedbackText(lines, line, feedbackText);
 				question.setFeedback(Validator.escapeHtml(feedbackText[0].trim()));
 				break;
 			}
 			
 			// set the text
 			if (line == 0)
 			{
 				String[] questionText = new String[]{""};
 				line = getQuestionText(lines, line, questionText);
 				text = Validator.escapeHtml(questionText[0].trim());
 				question.getPresentation().setText(text);
 			}
 			else
 			{
 				//take feedback for correct answer only
 				if (parts.length >= 2 && ("CORRECT ANSWER").equals(parts[1].trim()) && ("True").equalsIgnoreCase(parts[0].trim()))
 				{
 					isTrue = true;
 					if (parts.length >= 3 && (parts[parts.length - 1] != null)) question.setFeedback(parts[parts.length - 1].trim());
 				}
 				else if (parts.length >= 2 && ("CORRECT ANSWER").equals(parts[1].trim()) && ("False").equalsIgnoreCase(parts[0].trim()))
 				{
 					isTrue = false;
 					if (parts.length >= 3 && (parts[parts.length - 1] != null)) question.setFeedback(parts[parts.length - 1].trim());
 				}				
 			}
 		}
 
 		// answer choices
 		tf.setCorrectAnswer(new Boolean(isTrue).toString());
 
 		// survey
 		question.setIsSurvey(false);
 		
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 		
 		QuestionPick questionPick = part.addPickDetail(question);
 		questionPick.setPoints(Float.parseFloat("1"));		
 		
 		return true;
 	}
 }
