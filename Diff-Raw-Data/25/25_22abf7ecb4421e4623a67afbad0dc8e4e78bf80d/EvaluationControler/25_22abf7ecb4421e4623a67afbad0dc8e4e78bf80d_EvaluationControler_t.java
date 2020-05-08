 package com.ossia.test.web;
 
 import java.util.Date;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.ossia.test.domain.Evaluation;
 import com.ossia.test.domain.Profil;
 import com.ossia.test.domain.PropositionReponse;
 import com.ossia.test.domain.Question;
 import com.ossia.test.domain.Response;
 import com.ossia.test.service.EvaluationService;
 import com.ossia.test.service.TestSheetService;
 import com.ossia.test.web.form.QuestionForm;
 
 @Controller @RequestMapping("/tests")
 public class EvaluationControler extends AbstractController {
 	
 	@Autowired
 	private TestSheetService testSheetService;
 	
 	@Autowired 
 	private EvaluationService evaluationService;	
 	
 	@RequestMapping(value = "/setup", method = RequestMethod.GET)
 	public String displayTestSetup(@RequestParam("test") Integer evalId, ModelMap model) {
		// TODO créer une évaluation avec tous les réponses vides (à faire au moment de l'assignation par l'admin?)
 		Evaluation evaluation = evaluationService.getEvaluationById(evalId);		
 		Profil candidate = (Profil)model.get("candidate");
 		if (evaluation == null || evaluation.getProfil().getId() != candidate.getId()) {
 			return "redirect:/errors/404";
 		}
 		
 		model.put("evaluation", evaluation);
 		model.put("testEnCours",  true);
 		model.put("selectedTab", TAB_CURRENT_TEST);
 		return "test-setup";
 	}
 	
 	@RequestMapping(value = "/end-of-test", method = RequestMethod.GET)
 	public String displayEndOfTest(@RequestParam("test") Integer evalId, ModelMap model) {
 		evaluationService.closeTest(evalId);
 		
 		model.put("recentTest", evaluationService.getEvaluationById(evalId));		
 		
 		// Checking if the candidate msut pass other tests
 		Profil candidate = (Profil)model.get("candidate");
 		if (candidate.hasTestsToPass()) {
 			model.put("selectedTab", TAB_HOME);
 			model.put("candidate", candidate);			
 			return "candidate-home";
 		}
 		
 		model.put("testEnCours",  true);
 		model.put("selectedTab", TAB_CURRENT_TEST);
 		return "end-of-test";
 	}
 	
 	@RequestMapping(value = "/question", method = RequestMethod.GET)
 	public String displayQuestion(@RequestParam("test") Integer evalId, @RequestParam("question") Integer questionIndex, @RequestParam(value = "unansweredQuestions", required = false) Integer nbUnansweredQuestions, HttpServletResponse response, ModelMap model) {
 		// Checking that the evaluation exists and that the test is really assign to the candidate
 		Evaluation evaluation = evaluationService.getEvaluationById(evalId);		
 		Profil candidate = (Profil)model.get("candidate");
 		if (evaluation == null || evaluation.getProfil().getId() != candidate.getId()) {
 			return "redirect:/errors/404";
 		}
 		
 		// Checking that the test has questions
 		if (evaluation.getTest().getQuestions().isEmpty()) {
 			return "redirect:/errors/404";
 		}		
 		
 		// Checking that the question number is less that the total questions count
 		if (questionIndex < 1 || questionIndex > evaluation.getTest().getQuestionSize()) { // If not, redirect to question 1
 			return "redirect:/tests/question?test=" + evaluation.getId() + "&question=1";
 		}
 		
 		// If this is the first question, we start the test by setting the start time in the evaluation
 		if (evaluation.isTestAssigned()) {
 			evaluationService.markTestAsStarted(evaluation);
 		}
 		
 		// Checking that the time allowed to pass the test is not over
 		if (evaluationService.isTimeOver(evaluation)) {
 			return "redirect:/tests/end-of-test?test=" + evaluation.getId();
 		}
 					
 		QuestionForm questionForm = buildQuestionForm(evaluation, questionIndex);
 		
 		// Are there unansweredQuestions?
 		model.put("nbUnansweredQuestions", nbUnansweredQuestions);
 		if (nbUnansweredQuestions != null) {
 			questionForm.setWarnedCandidate(true);
 		}
 					
 		model.put("questionForm", questionForm);		
 		
 		model.put("question",  evaluation.getTest().getQuestions().get(questionIndex - 1));
 		model.put("questionCount", questionIndex);
 		model.put("evaluation", evaluation);	
 		
 		// Remaining time
 		model.put("remainingTime", getRemainingTime(evaluation));
 		
 		model.put("testEnCours",  true);
 		model.put("selectedTab", TAB_CURRENT_TEST);
 		
 		// Set no cache to force page reloading when user hits the "back" button in the browser
 		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
         response.setHeader("Pragma", "no-cache"); // HTTP 1.0
         response.setDateHeader("Expires", 0); // Proxies
 		return "question";
 	}	
 	
 	@RequestMapping(value = "/validate-question", method = RequestMethod.POST)
 	public String saveQuestion(@Valid QuestionForm questionForm, BindingResult result, ModelMap model) {
 		
 		Evaluation evaluation = evaluationService.saveCandidateResponse(questionForm);
 		if (evaluation == null) {
 			return "redirect:/errors/404";
 		}
 		
 		// Last question was answer, the test is now over
 		if (evaluation.getTest().getQuestionSize() == questionForm.getQuestionIndex() && questionForm.getNextQuestionIndex() > evaluation.getTest().getQuestionSize()) {
 			// Checking if there are unanswered questions
 			if (evaluation.getNbUnansweredQuestions() > 0 && !questionForm.isWarnedCandidate()) {
 				// Redirecting to last question to warn the candidate
 				return "redirect:/tests/question?test=" + questionForm.getEvaluationId() + "&question=" + evaluation.getTest().getQuestionSize() + "&unansweredQuestions=" + evaluation.getNbUnansweredQuestions();
 			}
 			return "redirect:/tests/end-of-test?test=" + evaluation.getId();
 		}
 		
 		// On to the next question ...
 		return "redirect:/tests/question?test=" + questionForm.getEvaluationId() + "&question=" + questionForm.getNextQuestionIndex();
 	}
 	
 	private QuestionForm buildQuestionForm(Evaluation evaluation, int questionIndex) {
 		QuestionForm form = new QuestionForm();
 		form.setQuestionIndex(questionIndex);
 		form.setEvaluationId(evaluation.getId());
 		Question currentQuestion = evaluation.getTest().getQuestions().get(questionIndex - 1);		
 		form.setQuestionId(currentQuestion.getId());
 		form.setNextQuestionIndex(questionIndex + 1);
 		
 		for(Response response : evaluation.getResponses()) {
 			if (response.getQuestion().getId() == currentQuestion.getId()) {
 				int[] reponsesCochees = new int[response.getReponsesChoisies().size()];
 				int i = 0;
 				for (PropositionReponse proposition : response.getReponsesChoisies()) {
 					reponsesCochees[i] = proposition.getId();
 					i ++;
 				}
 				form.setPropositions(reponsesCochees);
 			}
 		}		
 		return form;
 	}
 	
 	private int getRemainingTime(Evaluation evaluation) {
 		int testLength = evaluation.getTest().getDuree() * 60;
 		Date now = new Date();
 		int elapsedTime = (int) ((now.getTime() - evaluation.getStartTime().getTime()) / 1000);
 		
 		return testLength - elapsedTime;		
 	}
 }
