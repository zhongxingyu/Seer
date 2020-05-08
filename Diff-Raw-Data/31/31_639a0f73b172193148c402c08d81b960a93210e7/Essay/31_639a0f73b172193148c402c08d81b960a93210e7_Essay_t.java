 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.Model;
 
 import javax.xml.bind.annotation.XmlElement;
 
 /**
  *
  * @author Maria
  */
 public class Essay extends Question {
 
     private int wordLimit;
    private String essayQuestion;
    private String studentAnswer;
     
      /**
      * Empty constructor
      */
     public Essay() {
         
     }
 
     /**
      * Essay constructor with a word limit
      * 
      * @param id
      * @param questionText
      * @param mark
      * @param wordLimit 
      */
     public Essay(int id, String questionText, double mark, int wordLimit) {
         super(id, questionText, mark);
         this.wordLimit = wordLimit;
     }
     
     /**
      * Essay constructor without word limit
      * 
      * @param id
      * @param questionText
      * @param mark 
      */
     public Essay(int id, String questionText, double mark) {
         super(id, questionText, mark);
         this.wordLimit = -1;
     }
 
    public String getEssayQuestion() {
        return essayQuestion;
    }

    @XmlElement(name="Essay question")
    public void setEssayQuestion(String essayQuestion) {
        this.essayQuestion = essayQuestion;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    @XmlElement(name="Student answer")
    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    
     /**
      * Check if an answer is correct
      *
      * @return the result of the check
      */
 
     @Override
     public boolean checkAnswer() {
         return true;
     }
 
     public int getWordLimit() {
         return wordLimit;
     }
 
     public String getStudentEssay() {
        return essayQuestion;
     }
 
     @XmlElement
     public void setWordLimit(int wordLimit) {
         this.wordLimit = wordLimit;
     }
 
    
 }
