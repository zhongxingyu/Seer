 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2009 Etudes, Inc.
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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.mneme.api.AssessmentPermissionException;
 import org.etudes.mneme.api.AssessmentService;
 import org.etudes.mneme.api.AttachmentService;
 import org.etudes.mneme.api.GradesService;
 import org.etudes.mneme.api.ImportTextService;
 import org.etudes.mneme.api.Pool;
 import org.etudes.mneme.api.PoolService;
 import org.etudes.mneme.api.Question;
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
 
 /**
  * <p>
  * ImportQtiServiceImpl implements ImportQtiService
  * </p>
  */
 public class ImportTextServiceImpl implements ImportTextService
 {
 	/** Our logger. */
 	private static Log M_log = LogFactory.getLog(ImportTextServiceImpl.class);
 
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
 	
 	/** Hint key */
 	protected static final String hintKey = "hint:";
 	
 	/** Feedback key1 */
 	protected static final String feedbackKey1 = "feedback:";
 	
 	/** Feedback key2 */
 	protected static final String feedbackKey2 = "general feedback:";
 	
 	/** Reason */
 	protected static final String reasonKey = "reason";
 	
 	/** Survey */
 	protected static final String surveyKey = "survey";
 	
 	/** Regular expression for digit with period */
 	protected static final String digitPeriodRegex = "\\*?\\d+\\.";
 	
 	/** Regular expression for alphabet with period */
 	protected static final String alphabetPeriodRegex = "\\*?[a-zA-Z]\\.";
 	
 	/** An enumerate type that declares the types of numbering style */
 	public enum NumberingType
 	{
 		digitperiod, alphabetperiod, none;
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
 	public void importQuestions(String context, Pool pool, String text) throws AssessmentPermissionException
 	{
 		if ((text == null) || (text.length() == 0)) return;
 
 		String titleKey = "title:";
 		String pointsKey = "points:";
 		String descriptionKey = "description:";
 		String difficultyKey = "difficulty:";
 
 		// replace any \r\n with just a \n
 		text = text.replaceAll("\r\n", "\n");
 
 		// parse the text into lines
 		String[] lines = text.split("[\n]");
 
 		// trim each one - record the blank index positions
 		List<Integer> blanks = new ArrayList<Integer>();
 		for (int line = 0; line < lines.length; line++)
 		{
 			lines[line] = lines[line].trim();
 			if (lines[line].length() == 0)
 			{
 				blanks.add(Integer.valueOf(line));
 			}
 		}
 		blanks.add(Integer.valueOf(lines.length));
 
 		// make the groups
 		List<String[]> groups = new ArrayList<String[]>();
 		int pos = 0;
 		for (Integer line : blanks)
 		{
 			// take from pos up to (not including) the index of the next blank into a new group
 			String[] group = new String[line.intValue() - pos];
 			int i = 0;
 			while (pos < line.intValue())
 			{
 				group[i++] = lines[pos++];
 			}
 			groups.add(group);
 
 			// eat the blank line
 			pos++;
 		}
 
 		boolean topUsed = false;
 
 		// if there's no pool given, create one
 		if (pool == null)
 		{
 			// create the pool
 			pool = this.poolService.newPool(context);
 
 			// set the pool attributes
 			String title = "untitled";
 			Float points = null;
 			String description = null;
 			Integer difficulty = null;
 
 			// get the title, description and points from the first group, if present
 			String[] top = groups.get(0);
 			for (String line : top)
 			{
 				String lower = line.toLowerCase();
 				if (lower.startsWith(titleKey))
 				{
 					topUsed = true;
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) title = parts[1].trim();
 				}
 				else if (lower.startsWith(descriptionKey))
 				{
 					topUsed = true;
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) description = parts[1].trim();
 				}
 				else if (lower.startsWith(pointsKey))
 				{
 					topUsed = true;
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1)
 					{
 						try
 						{
 							points = Float.valueOf(parts[1].trim());
 						}
 						catch (NumberFormatException ignore)
 						{
 						}
 					}
 				}
 				else if (lower.startsWith(difficultyKey))
 				{
 					topUsed = true;
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1)
 					{
 						try
 						{
 							difficulty = Integer.valueOf(parts[1].trim());
 						}
 						catch (NumberFormatException ignore)
 						{
 						}
 					}
 				}
 			}
 
 			pool.setTitle(title);
 			if (points != null) pool.setPointsEdit(points);
 			if (description != null) pool.setDescription(description);
 			if (difficulty != null) pool.setDifficulty(difficulty);
 
 			// save
 			this.poolService.savePool(pool);
 		}
 
 		// process each one by creating a question and placing it into the pool
 		boolean first = true;
 		for (String[] group : groups)
 		{
 			if (first)
 			{
 				first = false;
 				if (topUsed) continue;
 			}
 
 			processTextGroup(pool, group);
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
 	 * Process the lines into a question in the pool, if we can.
 	 * 
 	 * @param pool
 	 *        The pool to hold the question.
 	 * @param lines
 	 *        The lines to process.
 	 */
 	protected void processTextGroup(Pool pool, String[] lines) throws AssessmentPermissionException
 	{
 		if (processTextTrueFalse(pool, lines)) return;
 		if (processTextMultipleChoice(pool, lines)) return;
 		if (processTextFillIn(pool, lines)) return;
 		if (processTextEssay(pool, lines)) return;
 		if (processTextMatching(pool, lines)) return;
 	}
 	
 	/**
 	 * Process if it is recognized as a true false question.
 	 * 
 	 * @param pool
 	 * 		  The pool to hold the question.
 	 * @param lines
 	 * 		  The lines to process.
 	 * @return true if successfully recognized and processed, false if not.
 	 * 
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextTrueFalse(Pool pool, String[] lines) throws AssessmentPermissionException
 	{
 		//if there are only two answer choices, and they are true and false and with one correct answer
 		//then that may be a true/false question
 		if (lines.length < 3)
 			return false;
 		
 		boolean foundAnswer = false;
 		boolean isTrue = false;
 		String feedback = null;
 		String hints = null;
 		boolean explainReason = false;
 		boolean isSurvey = false;
 		boolean foundQuestionAttributes = false;
 		boolean numberFormatNeeded = false;
 		boolean foundTrue = false, foundFalse = false;
 		boolean first = true;
 		
 		NumberingType numberingType = null;
 		
 		
 		
 		for (String line : lines)
 		{
 			// ignore first line as first line is question text
 			if (first)
 			{
 				first= false;
 				continue;
 			}
 			
 			// first and second answers must be "true" or "false" and if there are more answers choices it's not a true/false question
 			String[] answer = line.trim().split("\\s+");
 			
 			// first two answers choices should be true or false
 			if (!(foundTrue && foundFalse))
 			{
 				if (answer.length == 2)
 				{
 					if (!numberFormatNeeded && (foundTrue || foundFalse))
 						return false;
 					
 					if (!numberFormatNeeded)
 					{
 						numberingType = establishNumberingType(answer[0]);
 						if (numberingType == NumberingType.none)
 							continue;
 						
 						numberFormatNeeded = true;
 					}		
 					
 					if (!foundTrue && "true".equalsIgnoreCase(answer[1]))
 						foundTrue = true;
 					else if (!foundFalse && "false".equalsIgnoreCase(answer[1]))
 						foundFalse = true;
 					else
 						return false;
 						
 					boolean checkFormat = false;
 					
 					if (answer[0].startsWith("*"))
 					{
 						checkFormat = validateNumberingType(answer[0], numberingType);
 						
 						if (!foundAnswer)
 							foundAnswer = true;
 						else
 							return false;
 						
 						if ("true".equalsIgnoreCase(answer[1]))
 							isTrue = true;
 					}
 					else
 						checkFormat = validateNumberingType(answer[0], numberingType);
 					
 					if (!checkFormat)
 						return false;
 					
 				}
 				else if (answer.length == 1)
 				{
 					if (numberFormatNeeded)
 						return false;
 					
 					if (!foundTrue && ("true".equalsIgnoreCase(answer[0]) || "*true".equalsIgnoreCase(answer[0])))
 						foundTrue = true;
 					else if (!foundFalse && ("false".equalsIgnoreCase(answer[0])|| "*false".equalsIgnoreCase(answer[0])))
 						foundFalse = true;
 					else
 					{
 						if (foundTrue || foundFalse)
 							return false;
 						else
 							continue;
 					}
 											
 					if (answer[0].startsWith("*"))
 					{
 						if (!foundAnswer)
 							foundAnswer = true;
 						else
 							return false;
 						
 						if ("*true".equalsIgnoreCase(answer[0]))
 							isTrue = true;
 					}
 					
 					numberFormatNeeded = false;
 				}
 				else
 					continue;
 			}
 			else
 			{
 				// get feedback, hints, reason, survey. Ignore the line if the key is not found
 				String lower = line.toLowerCase();
 				if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) feedback = parts[1].trim(); 
 					
 					foundQuestionAttributes = true;
 				} 
 				else if (lower.startsWith(hintKey))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) hints = parts[1].trim();
 					
 					foundQuestionAttributes = true;
 				}
 				else if (lower.equalsIgnoreCase(reasonKey))
 				{
 					explainReason = true;
 					
 					foundQuestionAttributes = true;
 				}
 				else if (lower.equalsIgnoreCase(surveyKey))
 				{
 					isSurvey = true;
 					
 					foundQuestionAttributes = true;
 				}
 				
 				if (!foundQuestionAttributes)
 				{
 					// for true/false question there should be only two answer choices
 					if (numberFormatNeeded && validateNumberingType(answer[0], numberingType))
 						return false;
 				}
 			}
 		}
 		
 		if (!foundAnswer && !isSurvey)
 			return false;
 		
 		if (!foundTrue || !foundFalse)
 			return false;
 		
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
 		TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());
 
 		// set the text
 		// If a question starts with a number ("1."), strip out number and dot all the way to first letter. 
 		String clean = null;
 		String text = lines[0].trim();
 		if (text.matches("^\\d+\\.\\s.*"))
 		{
 			String[] parts = StringUtil.splitFirst(text, ".");
 			if (parts.length > 1) 
 			{
 				text = parts[1].trim();
 				clean = HtmlHelper.clean(text);
 			}
 			else
 				return false;
 		}
 		else
 			clean = HtmlHelper.clean(text);
 		
 		question.getPresentation().setText(clean);
 
 		// the correct answer
 		tf.setCorrectAnswer(Boolean.toString(isTrue));
 
 		// add feedback
 		if (StringUtil.trimToNull(feedback) != null)
 		{
 			question.setFeedback(HtmlHelper.clean(feedback));
 		}
 		
 		// add hints
 		if (StringUtil.trimToNull(hints) != null)
 		{
 			question.setHints(HtmlHelper.clean(hints));
 		}
 		
 		// explain reason
 		question.setExplainReason(explainReason);
 		
 		// survey
 		question.setIsSurvey(isSurvey);
 		
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 		
 		return true;
 	}
 	
 	/**
 	 * Process if it is recognized as a multiple choice question.
 	 * 
 	 * @param pool
 	 * 		  The pool to hold the question.
 	 * @param lines
 	 * 		  The lines to process.
 	 * @return true if successfully recognized and processed, false if not.
 	 * 
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextMultipleChoice(Pool pool, String[] lines) throws AssessmentPermissionException
 	{
 		//if there is one or more answers for more answer choices then that may be a multiple choice question
 		if (lines.length < 3)
 			return false;
 		
 		boolean first = true;
 		boolean foundAnswer = false;
 		String answerChoice = null;
 		List<Integer> multipleAnswers = new ArrayList<Integer>();
 		List<String> choices = new ArrayList<String>();
 		String clean = null;
 		
 		String feedback = null;
 		String hints = null;
 		boolean explainReason = false;
 		boolean isSurvey = false;
 		boolean shuffleChoices = false;
 		boolean foundQuestionAttributes = false;
 		
 		boolean numberFormatEstablished = false;
 		
 		NumberingType numberingType = null;
 		
 		String shuffleKey = "shuffle";
 		
 		int answersIndex = 0;
 		for (String line : lines)
 		{
 			// ignore first line as first line is question text
 			if (first)
 			{
 				first = false;
 				continue;
 			}
 			
 			// hints and feedback
 			String lower = line.toLowerCase();
 			if (foundAnswer)
 			{
 				// get feedback, hints, reason, survey. Ignore the line if the key is not found
 				if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) feedback = parts[1].trim(); 
 					foundQuestionAttributes = true;
 				} 
 				else if (lower.startsWith(hintKey))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) hints = parts[1].trim();
 					foundQuestionAttributes = true;
 				}
 				else if (lower.equalsIgnoreCase(reasonKey))
 				{
 					explainReason = true;
 					foundQuestionAttributes = true;
 				}
 				else if (lower.equalsIgnoreCase(surveyKey))
 				{
 					isSurvey = true;
 					foundQuestionAttributes = true;
 				} 
 				else if (lower.equalsIgnoreCase(shuffleKey))
 				{
 					shuffleChoices = true;
 					foundQuestionAttributes = true;
 				}
 				
 				//after finding feedback or hints or reason or survey, ignore any answers
 				if (foundQuestionAttributes)
 					continue;	
 			}
 			
 			String[] answer = line.trim().split("\\s+");
 			
 			// ignore answer choices with incorrect format
 			if (answer.length < 2)
 				continue;
 			
 			if (!numberFormatEstablished)
 			{
 				numberingType = establishNumberingType(answer[0]);
 				
 				if (numberingType == NumberingType.none)
 					continue;
 				
 				numberFormatEstablished = true;
 			}
 			
 			// ignore answer choices with incorrect format
 			if (!validateNumberingType(answer[0], numberingType))
 				continue;
 			
 			if (answer[0].startsWith("*"))
 			{
 				if (!foundAnswer) 
 				{
 					foundAnswer = true;
 				}
 				multipleAnswers.add(Integer.valueOf(answersIndex));
 			}
 						
 			answerChoice = line.substring(answer[0].length()).trim();
 			clean = HtmlHelper.clean(answerChoice);
 			choices.add(clean);
 			answersIndex++;
 		}
 		
 		if (!foundAnswer)
 			return false;
 		
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:MultipleChoice");
 		MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());
 
 		// set the text
 		String text = lines[0].trim();
 		if (text.matches("^\\d+\\.\\s.*"))
 		{
 			String[] parts = StringUtil.splitFirst(text, ".");
 			if (parts.length > 1) 
 			{
 				text = parts[1].trim();
 				clean = HtmlHelper.clean(text);
 			}
 			else
 				return false;
 		}
 		else
 			clean = HtmlHelper.clean(text);
 
 		question.getPresentation().setText(clean);
 
 		// randomize
 		mc.setShuffleChoices(Boolean.toString(false));
 
 		// answer choices
 		if (choices.size() < 2)
 			return false;
 		
 		mc.setAnswerChoices(choices);
 		
 		Set<Integer> correctAnswers = new HashSet<Integer>();
 		List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choicesAuthored = mc.getChoicesAsAuthored();
 
 		// find the answers
 		for (Integer answerIndex : multipleAnswers)
 		{
			if (choicesAuthored.size() < answerIndex)
				return false;
			
 			correctAnswers.add(Integer.valueOf(choicesAuthored.get(answerIndex).getId()));
 		}
 		
 		// correct answer
 		mc.setCorrectAnswerSet(correctAnswers);
 		
 		// single / multiple select
 		if (correctAnswers.size() == 1)
 			mc.setSingleCorrect(Boolean.TRUE.toString());
 		else
 			mc.setSingleCorrect(Boolean.FALSE.toString());
 		
 		// shuffle choices
 		mc.setShuffleChoices(Boolean.toString(shuffleChoices));
 		
 		// add feedback
 		if (StringUtil.trimToNull(feedback) != null)
 		{
 			question.setFeedback(HtmlHelper.clean(feedback));
 		}
 		
 		// add hints
 		if (StringUtil.trimToNull(hints) != null)
 		{
 			question.setHints(HtmlHelper.clean(hints));
 		}
 		
 		// explain reason
 		question.setExplainReason(explainReason);
 		
 		// survey
 		question.setIsSurvey(isSurvey);
 		
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 		
 		return true;
 	}
 	
 	/**
 	 * Process if it is recognized as an essay question.
 	 * 
 	 * @param pool
 	 * 		  The pool to hold the question.
 	 * @param lines
 	 * 		  The lines to process.
 	 * @return true if successfully recognized and processed, false if not.
 	 * 
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextEssay(Pool pool, String[] lines) throws AssessmentPermissionException
 	{
 		//if there are no answers then that may be a essay question
 		if (lines.length == 0)
 			return false;
 		
 		boolean first = true;
 		String clean = null;
 		String feedback = null;
 		String hints = null;
 		boolean explainReason = false;
 		boolean isSurvey = false;
 		String modelAnswer = null;
 		String modelAnswerKey = "model answer:";
 		
 		boolean foundQuestionAttributes = false;
 		
 		// question with braces may be a fill in question
 		if ((lines[0].indexOf("{") != -1) && (lines[0].indexOf("}") != -1) && (lines[0].indexOf("{") < lines[0].indexOf("}")))
 			return false;
 		
 		// model answer, hints and feedback
 		for (String line : lines)
 		{
 			// ignore first line as first line is question text
 			if (first)
 			{
 				first = false;
 				continue;
 			}
 			
 			// get feedback, hints, reason, survey. Ignore the line if the key is not found
 			String lower = line.toLowerCase();
 			if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
 			{
 				String[] parts = StringUtil.splitFirst(line, ":");
 				if (parts.length > 1) feedback = parts[1].trim();
 				
 				foundQuestionAttributes = true;
 			} 
 			else if (lower.startsWith(hintKey))
 			{
 				String[] parts = StringUtil.splitFirst(line, ":");
 				if (parts.length > 1) hints = parts[1].trim();
 				
 				foundQuestionAttributes = true;
 			}
 			else if (lower.startsWith(modelAnswerKey))
 			{
 				String[] parts = StringUtil.splitFirst(line, ":");
 				if (parts.length > 1) modelAnswer = parts[1].trim();
 				
 				foundQuestionAttributes = true;
 			}
 			else if (lower.equalsIgnoreCase(reasonKey))
 			{
 				explainReason = true;
 				foundQuestionAttributes = true;
 			}
 			else if (lower.equalsIgnoreCase(surveyKey))
 			{
 				isSurvey = true;
 				foundQuestionAttributes = true;
 			}
 			else
 			{
 				//if answers are followed by question or followed by choices for matching question then it is not an essay question
 				if (!foundQuestionAttributes)
 				{
 					String[] answer = line.trim().split("\\s+");
 					NumberingType numberingType;
 					numberingType = establishNumberingType(answer[0]);
 					if (!(numberingType == NumberingType.none) || lower.matches("^\\[\\w.*\\].*") || lower.startsWith("*"))
 						return false;
 				}
 			}
 		}
 		
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:Essay");
 		EssayQuestionImpl e = (EssayQuestionImpl) (question.getTypeSpecificQuestion());
 		
 		// set the text
 		String text = lines[0].trim();
 		if (text.matches("^\\d+\\.\\s.*"))
 		{
 			String[] parts = StringUtil.splitFirst(text, ".");
 			if (parts.length > 1) 
 			{
 				text = parts[1].trim();
 				clean = HtmlHelper.clean(text);
 			}
 			else
 				return false;
 		}
 		else
 			clean = HtmlHelper.clean(text);
 		
 		question.getPresentation().setText(clean);
 				
 		// type
 		e.setSubmissionType(EssayQuestionImpl.SubmissionType.inline);
 		
 		// add model answer
 		if (StringUtil.trimToNull(modelAnswer) != null)
 		{
 			e.setModelAnswer(HtmlHelper.clean(modelAnswer));
 		}
 		
 		// add feedback
 		if (StringUtil.trimToNull(feedback) != null)
 		{
 			question.setFeedback(HtmlHelper.clean(feedback));
 		}
 		
 		// add hints
 		if (StringUtil.trimToNull(hints) != null)
 		{
 			question.setHints(HtmlHelper.clean(hints));
 		}
 		
 		// explain reason
 		question.setExplainReason(explainReason);
 		
 		// survey
 		question.setIsSurvey(isSurvey);
 				
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 		
 		return true;
 	}
 	
 	/**
 	 * Process if it is recognized as an fill-in question.
 	 * 
 	 * @param pool
 	 * 		  The pool to hold the question.
 	 * @param lines
 	 * 		  The lines to process.
 	 * @return true if successfully recognized and processed, false if not.
 	 * 
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextFillIn(Pool pool, String[] lines) throws AssessmentPermissionException
 	{
 		// if there are only answers then that may be a fill-in question. Another case is if the question has braces that may be a fill-in question
 		if (lines.length == 0)
 			return false;
 		
 		boolean braces = false;
 		boolean first = true;
 		boolean foundAnswer = false;
 		List<String> answers = new ArrayList<String>();
 		String feedback = null;
 		String hints = null;
 		boolean explainReason = false;
 		boolean isSurvey = false;
 		boolean foundQuestionAttributes = false;
 		boolean bracesNoAnswer = false;
 		boolean isResponseTextual = false;
 		
 		boolean numberFormatEstablished = false;
 		
 		NumberingType numberingType = null;
 		
 		String clean = null;
 			
 		// question with braces may be a fill in question
 		if ((lines[0].indexOf("{") != -1) && (lines[0].indexOf("}") != -1) && (lines[0].indexOf("{") < lines[0].indexOf("}")))
 		{
 			String validateBraces = lines[0];
 			while (validateBraces.indexOf("{") != -1)
 			{
 				validateBraces = validateBraces.substring(validateBraces.indexOf("{")+1);
 				int startBraceIndex = validateBraces.indexOf("{");
 				int endBraceIndex = validateBraces.indexOf("}");
 				String answer;
 				
 				if (startBraceIndex != -1 && endBraceIndex != -1)
 				{
 					if (endBraceIndex > startBraceIndex)
 						return false;
 				}
 				if (endBraceIndex != -1)
 				{
 					answer = validateBraces.substring(0, endBraceIndex);
 					if (StringUtil.trimToNull(answer) == null)
 					{
 						if (lines.length < 1)
 							return false;
 						
 						bracesNoAnswer = true;
 					}
 					else
 					{
 						if (!isResponseTextual)
 						{
 							String[] multiAnswers = answer.split("\\|");
 							if (multiAnswers.length > 1) {
 								for (String multiAnswer : multiAnswers)
 								{
 									try
 									{
 										Float.parseFloat(multiAnswer.trim());
 										
 									} catch (NumberFormatException e)
 									{
 										isResponseTextual = true;
 									}
 								}
 							}
 							else 
 							{
 								try
 								{
 									Float.parseFloat(answer);
 									
 								} catch (NumberFormatException e)
 								{
 									isResponseTextual = true;
 								}
 							}
 						}
 					}
 				}
 				else
 					return false;
 				
 				validateBraces = validateBraces.substring(validateBraces.indexOf("}")+1);
 			}
 			
 			braces = true;
 		}
 		
 		if (braces)
 		{
 			// hints and feedback
 			for (String line : lines)
 			{
 				// ignore first line as first line is question text
 				if (first)
 				{
 					first = false;
 					continue;
 				}
 				
 				if (line.startsWith("*") || line.matches("^\\[\\w.*\\].*"))
 					return false;
 				
 				// hints and feedback
 				String lower = line.toLowerCase();
 				if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) feedback = parts[1].trim();
 				} 
 				else if (lower.startsWith(hintKey))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) hints = parts[1].trim();
 				}
 				else if (lower.equalsIgnoreCase(reasonKey))
 				{
 					explainReason = true;
 				}
 				else if (lower.equalsIgnoreCase(surveyKey))
 				{
 					isSurvey = true;
 				} 
 			}			
 		}
 		else
 		{
 			for (String line : lines)
 			{
 				// ignore first line as first line is question text
 				if (first)
 				{
 					first = false;
 					continue;
 				}
 				
 				if (line.startsWith("*") || line.matches("^\\[\\w.*\\].*"))
 					return false;
 				
 				// hints and feedback
 				String lower = line.toLowerCase();
 				if (foundAnswer)
 				{
 					if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
 					{
 						String[] parts = StringUtil.splitFirst(line, ":");
 						if (parts.length > 1) feedback = parts[1].trim();
 						foundQuestionAttributes = true;
 					} 
 					else if (lower.startsWith(hintKey))
 					{
 						String[] parts = StringUtil.splitFirst(line, ":");
 						if (parts.length > 1) hints = parts[1].trim();
 						foundQuestionAttributes = true;
 					}
 					else if (lower.equalsIgnoreCase(reasonKey))
 					{
 						explainReason = true;
 						foundQuestionAttributes = true;
 					}
 					else if (lower.equalsIgnoreCase(surveyKey))
 					{
 						isSurvey = true;
 						foundQuestionAttributes = true;
 					} 
 					
 					// ignore the answer choices after hints or feedback found
 					if (foundQuestionAttributes)
 							continue;
 				}
 				
 				String[] answer = line.trim().split("\\s+");
 				if (answer.length < 2)
 					return false;
 				
 				if (!numberFormatEstablished)
 				{
 					numberingType = establishNumberingType(answer[0]);
 					
 					if (numberingType == NumberingType.none)
 						continue;
 					
 					numberFormatEstablished = true;
 				}
 				
 				if (validateNumberingType(answer[0], numberingType))
 				{
 					String answerChoice = line.substring(answer[0].length()).trim();
 					answers.add(answerChoice);
 					if (!foundAnswer) foundAnswer = true;
 				}
 				else
 					continue;
 			}
 			
 			if (!foundAnswer)
 				return false;
 		}
 				
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:FillBlanks");
 		FillBlanksQuestionImpl f = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());
 		
 		// case sensitive
 		f.setCaseSensitive(Boolean.FALSE.toString());
 		
 		//mutually exclusive
 		f.setAnyOrder(Boolean.FALSE.toString());
 
 		//if found answers append them at the end of question
 		String questionText = lines[0].trim();
 		if (!braces && foundAnswer) {
 			StringBuffer buildAnswers = new StringBuffer();
 			buildAnswers.append("{");
 			for (String answer : answers)
 			{
 				if (!isResponseTextual)
 				{
 					String[] multiAnswers = answer.split("\\|");
 					if (multiAnswers.length > 1) {
 						for (String multiAnswer : multiAnswers)
 						{
 							try
 							{
 								Float.parseFloat(multiAnswer.trim());
 								
 							} catch (NumberFormatException e)
 							{
 								isResponseTextual = true;
 							}
 						}
 					}
 					else
 					{
 						try
 						{
 							Float.parseFloat(answer);
 							
 						} catch (NumberFormatException e)
 						{
 							isResponseTextual = true;
 						}
 					}
 				}
 				buildAnswers.append(answer);
 				buildAnswers.append("|");
 			}
 			buildAnswers.replace(buildAnswers.length() - 1, buildAnswers.length(), "}");
 			questionText = questionText.concat(buildAnswers.toString());
 		}
 				
 		// set the text
 		if (questionText.matches("^\\d+\\.\\s.*"))
 		{
 			String[] parts = StringUtil.splitFirst(questionText, ".");
 			if (parts.length > 1) 
 			{
 				questionText = parts[1].trim();
 				clean = HtmlHelper.clean(questionText);
 			}
 			else
 				return false;
 		}
 		else
 			clean = HtmlHelper.clean(questionText);
 		
 		f.setText(clean);
 		
 		// text or numeric
 		f.setResponseTextual(Boolean.toString(isResponseTextual));
 		
 		// add feedback
 		if (StringUtil.trimToNull(feedback) != null)
 		{
 			question.setFeedback(HtmlHelper.clean(feedback));
 		}
 		
 		// add hints
 		if (StringUtil.trimToNull(hints) != null)
 		{
 			question.setHints(HtmlHelper.clean(hints));
 		}
 		
 		// explain reason
 		question.setExplainReason(explainReason);
 		
 		if (bracesNoAnswer && !isSurvey)
 			return false;
 		
 		// survey
 		question.setIsSurvey(isSurvey);
 		
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 		
 		return true;
 	}
 	
 	/**
 	 * Process if it is recognized as an match question.
 	 * 
 	 * @param pool
 	 * 		  The pool to hold the question.
 	 * @param lines
 	 * 		  The lines to process.
 	 * @return true if successfully recognized and processed, false if not.
 	 * 
 	 * @throws AssessmentPermissionException
 	 */
 	protected boolean processTextMatching(Pool pool, String[] lines) throws AssessmentPermissionException
 	{
 		/* 1. if the choices start with '[' then it may be a matching question
 		   2. choice and match. choices should be equal or greater by one than matches*/
 		if (lines.length < 3)
 			return false;
 		
 		boolean first = true;
 		boolean blankMatch = false;
 		boolean foundQuestionAttributes = false;
 		boolean foundDrawMatch = false;
 		String feedback = null;
 		String hints = null;
 		boolean isSurvey = false;
 		String distractor = null;
 		Map<String, String> choicePairs = new HashMap<String, String>();
 		Map<String, String> drawChoicePairs = new HashMap<String, String>();
 		
 		boolean drawMatchNumberFormatEstablished = false, numberFormatEstablished = false;
 		
 		NumberingType drawMatchNumberingType = null, numberingType = null;
 		
 		for (String line : lines)
 		{
 			// ignore first line as first line is question text
 			if (first)
 			{
 				first = false;
 				continue;
 			}
 			
 			// draw choices
 			if (!line.startsWith("["))
 		    {
 				String lower = line.toLowerCase();
 				// get feedback, hints, reason, survey. Ignore the line if the key is not found
 				if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) feedback = parts[1].trim(); 
 					foundQuestionAttributes = true;
 				} 
 				else if (lower.startsWith(hintKey))
 				{
 					String[] parts = StringUtil.splitFirst(line, ":");
 					if (parts.length > 1) hints = parts[1].trim();
 					foundQuestionAttributes = true;
 				}
 				else if (lower.equalsIgnoreCase(surveyKey))
 				{
 					isSurvey = true;
 					foundQuestionAttributes = true;
 				}
 				
 				//after finding feedback or hints or reason or survey, ignore paired lists
 				if (foundQuestionAttributes)
 					continue;
 				
 				if (drawChoicePairs.size() < choicePairs.size())
 				{
 					String[] drawMatch = line.trim().split("\\s+");
 					
 					if (drawMatch.length > 1)
 					{
 						//check to see if the relation match starts with a character or digit with optional dot
 						if (!drawMatchNumberFormatEstablished)
 						{
 							drawMatchNumberingType = establishNumberingType(drawMatch[0]);
 							
 							if (drawMatchNumberingType == NumberingType.none)
 								continue;
 							
 							drawMatchNumberFormatEstablished = true;
 						}
 						if (validateNumberingType(drawMatch[0], drawMatchNumberingType))
 						{
 							String key, value;
 							if (drawMatch[0].endsWith("."))
 								key = drawMatch[0].substring(0, drawMatch[0].length() - 1);
 							else
 								key = drawMatch[0].substring(0, drawMatch[0].length());
 							
 							value = line.substring(drawMatch[0].length()+ 1);
 							
 							if((StringUtil.trimToNull(value) == null) || (StringUtil.trimToNull(key) == null))
 								continue;
 							
 							if (drawChoicePairs.containsKey(key) || drawChoicePairs.containsValue(value))
 								return false;
 							
 							drawChoicePairs.put(key, value);
 							
 							foundDrawMatch = true;
 						}
 					}
 				}
 				continue;
 				
 			}
 			else
 			{
 				// once draw matches found no more matching paired lists are added
 				if (foundDrawMatch || foundQuestionAttributes)
 					continue;
 			}
 			
 			if (line.indexOf("]") == -1)
 				continue;
 			
 			// choice
 			String choiceValue = line.substring(line.indexOf("[")+ 1, line.indexOf("]")).trim();
 			
 			String matchLine = line.substring(line.indexOf("]")+1).trim();
 			
 			String[] match = matchLine.trim().split("\\s+");
 			
 			// distractor
 			if (match.length < 2)
 			{
 				if (!blankMatch && match.length == 1) 
 				{
 					distractor = choiceValue;
 					
 					blankMatch = true;
 					continue;
 				}
 				else
 					return false;
 			}
 			
 			if (match.length > 1)
 			{
 				//check to see if paired lists counter starts with a character or digit with optional dot
 				if (!numberFormatEstablished)
 				{
 					numberingType = establishNumberingType(match[0]);
 					
 					if (numberingType == NumberingType.none)
 						continue;
 					
 					numberFormatEstablished = true;
 				}
 				if (validateNumberingType(match[0], numberingType))
 				{
 					String value = choiceValue;
 					String key = line.substring(line.indexOf("]")+1).substring(match[0].length()+ 1).trim();
 					
 					if((StringUtil.trimToNull(value) == null) || (StringUtil.trimToNull(key) == null))
 						continue;
 					
 					if (choicePairs.containsKey(key) || choicePairs.containsValue(value))
 						return false;
 					
 					choicePairs.put(key, value);
 				}
 			}
 			
 		}
 		
 		if (choicePairs.size() < 2)
 			return false;
 		
 		// create the question
 		Question question = this.questionService.newQuestion(pool, "mneme:Match");
 		MatchQuestionImpl m = (MatchQuestionImpl) (question.getTypeSpecificQuestion());
 
 		// set the text
 		String text = lines[0].trim();
 		String clean;
 		if (text.matches("^\\d+\\.\\s.*"))
 		{
 			String[] parts = StringUtil.splitFirst(text, ".");
 			if (parts.length > 1) 
 			{
 				text = parts[1].trim();
 				clean = HtmlHelper.clean(text);
 			}
 			else
 				return false;
 		}
 		else
 			clean = HtmlHelper.clean(text);
 		
 		question.getPresentation().setText(clean);
 		
 		// set the # pairs
 		m.consolidate("INIT:" + choicePairs.size());
 
 		// set the pair values
 		List<MatchQuestionImpl.MatchQuestionPair> pairs = m.getPairs();
 		String value;
 		int index = 0;
 		for (String key : choicePairs.keySet())
 		{
 			clean = HtmlHelper.clean(key);
 			pairs.get(index).setMatch(clean);
 			
 			if (drawChoicePairs.size() > 0)
 			{
 				value = choicePairs.get(key);
 				value = drawChoicePairs.get(value);
 			}
 			else
 				value = choicePairs.get(key);
 			
 			if(StringUtil.trimToNull(value) == null)
 				return false;
 			
 			clean = HtmlHelper.clean(value);
 			pairs.get(index).setChoice(clean);
 			
 			index++;				
 		}
 		
 		if (distractor != null)
 			m.setDistractor(distractor);
 		
 		// add feedback
 		if (StringUtil.trimToNull(feedback) != null)
 		{
 			question.setFeedback(HtmlHelper.clean(feedback));
 		}
 		
 		// add hints
 		if (StringUtil.trimToNull(hints) != null)
 		{
 			question.setHints(HtmlHelper.clean(hints));
 		}
 		
 		// survey
 		question.setIsSurvey(isSurvey);
 		
 		// save
 		question.getTypeSpecificQuestion().consolidate("");
 		this.questionService.saveQuestion(question);
 		
 		return true;
 	}
 	
 	/**
 	 * Get the numbering type
 	 * 
 	 * @param text
 	 * 		The text to process
 	 * @return The numbering type for the text
 	 */
 	protected NumberingType establishNumberingType(String text) 
 	{
 		if (StringUtil.trimToNull(text) == null)
 			return NumberingType.none;
 		
 		if (text.matches(digitPeriodRegex))
 			return NumberingType.digitperiod;
 		
 		if (text.matches(alphabetPeriodRegex))
 			return NumberingType.alphabetperiod;
 		
 		return NumberingType.none;
 	}
 	
 	/**
 	 * Validate the numbering type
 	 * 
 	 * @param text
 	 * 		The text to validate
 	 * @param numberingType
 	 * 		The NumberingType to validate with 
 	 * @return true if the text is valid numberingType else return false
 	 */
 	protected boolean validateNumberingType(String text, NumberingType numberingType) 
 	{
 		if (StringUtil.trimToNull(text) == null)
 			return false;
 		
 		switch (numberingType)
 		{
 			case digitperiod:
 				return text.matches(digitPeriodRegex);
 				
 			case alphabetperiod:
 				return text.matches(alphabetPeriodRegex);
 				
 			default:
 				return false;
 		}
 		
 	}
 	
 }
