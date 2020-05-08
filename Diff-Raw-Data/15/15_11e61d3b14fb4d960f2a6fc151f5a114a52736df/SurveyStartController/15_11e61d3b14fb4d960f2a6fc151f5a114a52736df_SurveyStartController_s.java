 package edu.unsw.cse.comp9323.group1.controllers.survey;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import edu.unsw.cse.comp9323.group1.DAOs.QuestionDAO;
 import edu.unsw.cse.comp9323.group1.DAOs.ResponseDAO;
 import edu.unsw.cse.comp9323.group1.forms.QuestionForm;
 import edu.unsw.cse.comp9323.group1.forms.QuestionListForm;
 import edu.unsw.cse.comp9323.group1.models.Question;
 import edu.unsw.cse.comp9323.group1.models.QuestionOption;
 
 /**
  * 
  * This class is controller class of survey detail.
  * 
  * @author z3378491.group1.comp9323-2013s1
  *
  */
 
 @Controller
 @RequestMapping("/survey/start")
 public class SurveyStartController {
 
 	/**
 	 * This method will get all the available question and questionType from surveygizmo.com.
 	 * It will return the list that will be shown when user would like to fill in the survey.
 	 * 
 	 * @param surveyId
 	 * @param model
 	 * @return surveyDetail
 	 */
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String getSurvey(@RequestParam("surveyId") int surveyId,
			@RequestParam("studentId") int studentId,
 			@RequestParam("courseName") String courseName, Model model) {
 
 		List<Question> allQuestions = new ArrayList<Question>();
 
 		QuestionDAO questionDAO = new QuestionDAO();
 		allQuestions = questionDAO.getAllQuestion(surveyId);
 
 		if (allQuestions.size() > 0) {
 			allQuestions.remove(allQuestions.size() - 1);
 		}
 
 		QuestionListForm responseListForm = new QuestionListForm();
 		List<QuestionForm> questionListTmp = new ArrayList<QuestionForm>();
 		Iterator<Question> allQuestionsItr = allQuestions.iterator();
 		int questionIndex = 0;
 		while (allQuestionsItr.hasNext()) {
 			Question questionTemp = allQuestionsItr.next();
 			QuestionForm questionForm = new QuestionForm(questionTemp.getId()
 					.toString(), questionTemp.getTitle(), "", surveyId,
 					questionTemp.getSubtype());
 
 			String responseContiner = new String();
 			if (questionTemp.getSubtype().equals("textbox")) {
 				responseContiner = "<input id=questionForm"
 						+ questionIndex + " name=questionForm["
 						+ questionIndex + "].response //>";
 			} else if (questionTemp.getSubtype().equals("essay")) {
 				responseContiner = "<textarea  id=questionForm"
 						+ questionIndex + " name=questionForm["
 						+ questionIndex
 						+ "].response rows=4 cols=50></textarea>";
 			}else if (questionTemp.getSubtype().equals("radio")) {
 				
 				StringBuffer responseContainerBuff = new StringBuffer();
 				
 				if(questionTemp.getQuestionOptions()!=null){
 					if(questionTemp.getQuestionOptions().size() > 0){
 						Iterator<QuestionOption> questionOptionsItr = questionTemp.getQuestionOptions().iterator();
 						while(questionOptionsItr.hasNext()){
 							QuestionOption questionOption = questionOptionsItr.next();
 							responseContainerBuff.append("<input type=radio id=questionForm"
 						+ questionIndex + " name=questionForm["
 						+ questionIndex + "].response value="+questionOption.getId()+"###"+questionOption.getTitle()+">"+questionOption.getTitle()+"<br>");
 						}
 					}
 				}
 				responseContiner = responseContainerBuff.toString();
 			}else if (questionTemp.getSubtype().equals("checkbox")) {
 				
 				StringBuffer responseContainerBuff = new StringBuffer();
 				
 				if(questionTemp.getQuestionOptions()!=null){
 					if(questionTemp.getQuestionOptions().size() > 0){
 						Iterator<QuestionOption> questionOptionsItr = questionTemp.getQuestionOptions().iterator();
 						while(questionOptionsItr.hasNext()){
 							QuestionOption questionOption = questionOptionsItr.next();
 							responseContainerBuff.append("<input type=checkbox id=questionForm"
 						+ questionIndex + " name=questionForm["
 						+ questionIndex + "].response value="+questionOption.getId()+"###"+questionOption.getTitle()+">"+questionOption.getTitle()+"<br>");
 						}
 					}
 				}
 				responseContiner = responseContainerBuff.toString();
 			}
 
 			questionIndex++;
 			questionForm.setResponseContainer(responseContiner);
 			questionListTmp.add(questionForm);
 		}
 
 		responseListForm.setQuestionForm(questionListTmp);
 		responseListForm.setCourseName(courseName);
 		responseListForm.setStudentId(studentId);
 		responseListForm.setSurveyId(surveyId);
 
 		model.addAttribute("responseListForm", responseListForm);
 
 		return "surveyStart";
 	}
 	
 	/**
 	 * 
 	 * This method handle post HTTP request from user.
 	 * It will be invoke when user submit the survey they already filled.
 	 * It opens communication to surveygizmo.com to submit user response.
 	 * 
 	 * @param questionForm
 	 * @param result
 	 * @return courseDetail
 	 */
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String processForm(
 			@Valid QuestionForm questionForm,
 			@ModelAttribute(value = "SHORT_Q_RESPONSE") QuestionListForm questionListForm,
 			BindingResult result) {
 		if (result.hasErrors()) {
 			return "surveyCreate";
 		} else {
 
 			Iterator<QuestionForm> questionFormInputItr = questionListForm
 					.getQuestionForm().iterator();
 			List<Question> responseList = new ArrayList<Question>();
 			while (questionFormInputItr.hasNext()) {
 
 				QuestionForm questionFormInput = questionFormInputItr.next();
 
 				Question response = new Question();
 				response.setId(Long.parseLong(questionFormInput.getId()));
 				response.setSurveyId(questionFormInput.getSurveyId());
 				response.setResponse(questionFormInput.getResponse());
 				responseList.add(response);
 				
 			}
 			
 			ResponseDAO responseDAO = new ResponseDAO();
 			responseDAO.saveResponse(responseList);
 
 			return "redirect:/courseDetail?courseName="
 					+ questionListForm.getCourseName() + "&studentId="
 					+ questionListForm.getStudentId();
 		}
 	}
 
 }
