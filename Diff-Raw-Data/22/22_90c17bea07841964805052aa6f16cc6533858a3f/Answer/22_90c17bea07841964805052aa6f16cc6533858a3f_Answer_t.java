 package be.kdg.groepi.model;
 
 import be.kdg.groepi.utils.CompareUtil;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Gregory
  * Date: 21/02/13
  * Time: 13:26
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name = "T_ANSWER")
 public class Answer {
     @Id
     @GeneratedValue
     @Column(name = "answer_id")
     private Long fId;
     @Column(name = "answers")
     @ElementCollection(fetch = FetchType.EAGER)
    private List<String> fAnswers = new ArrayList<>();// = new ArrayList<String>();
     @Column(name = "correctAnswer")
     private int fCorrectAnswer;
     @Column(name = "correctAnswerDescription")
     private String fCorrectAnswerDescription;
 
     @ManyToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "stop_id", nullable = false)
     private Stop fStop;
 
 
     public Answer() {
     }
 
     public Answer(Stop fStop) {
         this.fStop = fStop;
     }
 
     public Answer(List<String> answers, int correctAnswer, String correctAnswerDescription, Stop fStop) {
         this.fAnswers = answers;
         if (correctAnswer < 0 || correctAnswer >= answers.size()) {
             correctAnswer = 0;
         }
         this.fCorrectAnswer = correctAnswer;
         this.fCorrectAnswerDescription = correctAnswerDescription;
         this.fStop = fStop;
     }
 
     public Long getId() {
         return fId;
     }
 
     public void setId(Long fId) {
         this.fId = fId;
     }
 
     public List<String> getAnswers() {
         return fAnswers;
     }
 
     public void setAnswers(List<String> fAnswers) {
         this.fAnswers = fAnswers;
     }
 
     public void addAnswer(String answer) {
         fAnswers.add(answer);
     }
 
     public void removeAnswer(String answer) {
         int indexOfAnswer = fAnswers.indexOf(answer);
         if (fCorrectAnswer > indexOfAnswer) {
             fCorrectAnswer--;
         } else if (fCorrectAnswer == indexOfAnswer) {
             fCorrectAnswer = 0;
         }
         fAnswers.remove(answer);
     }
 
     public void removeAnswer(int index) {
         if (fCorrectAnswer > index) fCorrectAnswer--;
         fAnswers.remove(index);
     }
 
     public int getCorrectAnswer() {
         return fCorrectAnswer;
     }
 
 
     public void setCorrectAnswer(int fCorrectAnswer) {
         if (fCorrectAnswer < 0 || fCorrectAnswer >= fAnswers.size()) {
             fCorrectAnswer = 0;
         }
         this.fCorrectAnswer = fCorrectAnswer;
     }
 
     public String getCorrectAnswerDescription() {
         return fCorrectAnswerDescription;
     }
 
     public void setCorrectAnswerDescription(String fCorrectAnswerDescription) {
         this.fCorrectAnswerDescription = fCorrectAnswerDescription;
     }
 
     public Stop getStop() {
         return fStop;
     }
 
     public void setStop(Stop fStop) {
         this.fStop = fStop;
     }
 
     public boolean isAnswerCorrect(int givenAnswer) {
         if (givenAnswer == fCorrectAnswer) {
             return true;
         } else {
             return false;
         }
     }
     //TODO: equals methoden in TripInstance + afgeleiden
 
     @Override
     public boolean equals(Object o) {
         Answer answer = (Answer) o;
 //        if (this == answer) return false;
 
         int comparison = this.fCorrectAnswerDescription.compareTo(answer.getCorrectAnswerDescription());
         if (comparison != 0) return false;
 
         if (this.fCorrectAnswer != answer.getCorrectAnswer()) return false;
 
         if (!CompareUtil.compareList(this.fAnswers, answer.getAnswers())) return false;
 
 //        if (this.fStop.getId() != answer.getStop().getId()) return false;
 
         return true;
     }
 
 }
