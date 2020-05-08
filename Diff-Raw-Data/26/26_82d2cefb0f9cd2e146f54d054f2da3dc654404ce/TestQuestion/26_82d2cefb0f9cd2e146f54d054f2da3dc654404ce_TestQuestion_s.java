 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tictac.test_question;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author bkt
  */
 @Entity
 @Table(name = "test_question")
 public class TestQuestion implements Serializable {
     @Id
     @GeneratedValue
     @Column(name = "id")
    protected Long _id;
     @Column(name = "testid")
    protected Long _testId;
     @Column(name = "questionid")
    protected Long _questionId;
 
     public TestQuestion() {
         this._testId = (long) 0;
         this._questionId = (long) 0;
     }
 
     public TestQuestion(Long _testid, Long _questionid) {
         this._testId = _testid;
         this._questionId = _questionid;
     }
         
     public Long getId() {
         return _id;
     }
 
     public void setId(Long _id) {
         this._id = _id;
     }
 
     public Long getQuestionId() {
         return _questionId;
     }
 
     public void setQuestionId(Long _questionid) {
         this._questionId = _questionid;
     }
 
     public Long getTestId() {
         return _testId;
     }
 
     public void setTestId(Long _testid) {
         this._testId = _testid;
     }
 
     
     
 }
