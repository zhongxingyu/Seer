 package com.hist.askme.beans;
 
 import com.hist.askme.models.Question;
 import com.hist.askme.models.Questionnaire;
 import com.hist.askme.models.QuestionnaireService;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 @ManagedBean
 @SessionScoped
 public class QuestionnaireBean implements Serializable {
 
     String name = "ID" + new Random().nextInt(9999);
     int pubTime = 0;
     ArrayList<Question> questions = new ArrayList<Question>();
     Questionnaire questionnaire = new Questionnaire("", questions);
     QuestionnaireService questionnaireService = new QuestionnaireService();
     ArrayList<Questionnaire> questionnaires;
     boolean hasAnswered = false;
     boolean published = false;
     boolean valid = true;
 
     public boolean isValid() {
         return valid;
     }
 
     public void checkID() {
         if (name.trim().equals("")) {
             valid = false;
         } else {
             valid = true;
         }
         for (Questionnaire q : questionnaireService.getQuestionnaires()) {
             if (q.getName().equals(name)) {
                 valid = false;
             }
         }
     }
     String link = "askme.hist.no";
     Long answer;
 
     public Long getAnswer() {
         return answer;
     }
 
     public void setAnswer(Long answer) {
         this.answer = answer;
     }
 
     public boolean getPublished() {
         return published;
     }
 
     public QuestionnaireService getQuestionnaireService() {
         return questionnaireService;
     }
 
     public void setQuestionnaireService(QuestionnaireService qs) {
         questionnaireService = qs;
     }
 
     public ArrayList<Questionnaire> getQuestionnaires() {
         questionnaires = questionnaireService.getQuestionnaires();
         return questionnaires;
     }
 
     public void addQuestionnaire(Questionnaire q) {
         checkID();
         if (valid) {
             questionnaireService.addQuestionnaire(q);
             published = true;
             questions = new ArrayList<Question>();
         } else {
             published = false;
         }
     }
 
     public Questionnaire newQuestionnaire() {
         questionnaire = new Questionnaire(name, questions);
         questionnaire.fixQuestions();
         return questionnaire;
     }
 
     public String getName() {
         return name;
     }
 
     public String getLink() {
         return link + "/" + name;
     }
 
     public void setName(String newName) {
         name = newName;
     }
 
     public int getPubTime() {
         return pubTime;
     }
 
     public void setPubTime(int newTime) {
         pubTime = newTime;
     }
 
     public Questionnaire getQuestionnaire() {
         return questionnaire;
     }
 
     public ArrayList<Question> getQuestions() {
         return questions;
     }
 
     public void addQuestion(Question q) {
         questions.add(q);
     }
 
     public void deleteQuestion(Question q) {
         questions.remove(q);
     }
 
     public String answerQuestionnaire() {
         if (IPAlreadyUsed()) {
             return "pretty:questionnaire";
         } else {
             for (Question q : questionnaire.getQuestions()) {
                if (q.getQuestionInt() == Question.TEXT_QUESTION) {
                     questionnaireService.updateTextQuestionnaire(q);
                } else if (q.getQuestionInt() == Question.CHECKBOX_QUESTION) {
                    if(q.getAnswers() != null) {
                        questionnaireService.updateCheckboxQuestionnaire(q.getAnswers());
                    }
                 } else {
                     questionnaireService.updateQuestionnaire(q.getAnswer());
                 }
             }
             endSession();
             return "pretty:result";
         }
     }
 
     public String getUserIP() {
         HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         return httpServletRequest.getRemoteAddr();
     }
 
     public void setUserIP() {
         questionnaireService.setUserIP(getUserIP(), questionnaire);
 
     }
 
     public boolean getHasAnswered() {
         return hasAnswered;
 
     }
 
     public void endSession() {
         FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
 
     }
 
     public boolean IPAlreadyUsed() {
         for (int i = 0; i < questionnaire.getIPList().size(); i++) {
             if (questionnaire.getIPList().get(i).getIp().equals(getUserIP())) {
                 hasAnswered = true;
                 return true;
             }
         }
         setUserIP();
         return false;
 
     }
 
     //-----------------------------------------------------------------------------
     public void init() {
         if (name.trim() == null) {
             String message = "Bad request. Please use a link from within the system.";
             FacesContext.getCurrentInstance().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
             return;
         }
         if (questionnaires == null) {
             questionnaires = questionnaireService.getQuestionnaires();
         }
         questionnaire = questionnaireService.find(name, questionnaires);
 
         if (questionnaire == null) {
             String message = "Bad request. Unknown questionnaire.";
             FacesContext.getCurrentInstance().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
             return;
         }
     }
 }
