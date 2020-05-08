 package com.hist.askme.models;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.*;
 
 @Entity
 @Table(name = "QUESTION")
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @DiscriminatorValue("4")
 @DiscriminatorColumn(name = "proj_type")
 public class Question implements Serializable {
 
    public final static int RADIO_QUESTION = 0;
    public final static int CHECKBOX_QUESTION = 1;
     public final static int BOOLEAN_QUESTION = 2;
     public final static int TEXT_QUESTION = 3;
     @Id
     @GeneratedValue
     private Long id;
     private String questionText;
     @OneToMany(mappedBy = "question", cascade = CascadeType.PERSIST)
     private List<Answer> options = new ArrayList<Answer>();
     @Transient
     Long answer;
     @Transient
     String textAnswer;
     @Transient
     List<String> answers = new ArrayList<String>();
     @ManyToOne
     private Questionnaire questionnaire;
 
     public Question() {
     }
 
     public Question(String questionText, List<Answer> options) {
         this.questionText = questionText;
         this.options = options;
     }
 
     public void fixOptions() {
         for (Answer a : options) {
             a.setQuestion(this);
         }
     }
 
     public Answer getOptionByString(String ans) {
         for (Answer a : options) {
             if (a.getText().compareTo(ans) == 0) {
                 return a;
             }
         }
         return null;
     }
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getQuestionText() {
         return questionText;
     }
 
     public List<Answer> getOptions() {
         return options;
     }
 
     public void setOptions(ArrayList<Answer> options) {
         this.options = options;
     }
 
     public Questionnaire getQuestionnaire() {
         return questionnaire;
     }
 
     public void setQuestionnaire(Questionnaire questionnaire) {
         this.questionnaire = questionnaire;
     }
 
     public int amountOfAnswered() {
         int amountOfAnswered = 0;
         for (int i = 0; i < options.size(); i++) {
             amountOfAnswered += options.get(i).getResult();
         }
         return amountOfAnswered;
     }
 
     public int getAmountOfOptions() {
         return options.size();
     }
 
     public void setQuestionText(String newQ) {
         questionText = newQ;
     }
 
     public void addOption(String answer) {
         Answer a = new Answer(answer);
         a.setQuestion(this);
         options.add(a);
     }
 
     public void deleteOption(Answer answer) {
         options.remove(answer);
     }
 
     public Long getAnswer() {
         return answer;
     }
 
     public void setAnswer(Long answer) {
         this.answer = answer;
     }
     
     public String getTextAnswer() {
         return textAnswer;
     }
 
     public void setTextAnswer(String textAnswer) {
         this.textAnswer = textAnswer;
     }
     
     public List<String> getAnswers() {
         return answers;
     }
 
     public void setAnswers(List<String> answers) {
         this.answers = answers;
     }
 
     public String getQuestionType() {
         return "Type:" + this.getClass();
     }
 
     public int getQuestionInt() {
         return 4;
     }
 
     public void addTextAnswer(TextEntity answer) {
     }
 
     public List<TextEntity> getTextAnswers() {
         return null;
     }
 
 }
