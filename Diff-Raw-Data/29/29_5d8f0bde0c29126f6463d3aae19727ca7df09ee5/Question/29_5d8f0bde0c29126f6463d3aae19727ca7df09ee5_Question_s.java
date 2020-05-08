 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.preppa.web.entities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.apache.tapestry5.beaneditor.NonVisual;
 
 
 /**
  *
  * @author newtonik
  */
 @Entity
 public class Question implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @Basic(optional = false)
     @Column(name = "id")
     @NonVisual
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Integer id;
     @ManyToOne(targetEntity=Questiontype.class)
     @JoinColumn(name="questiontype_id")
     private Questiontype questiontype;
     @Lob
     @Column(name = "question")
     private String question;
     @ManyToMany(cascade=CascadeType.ALL,  targetEntity=Tag.class)
      @JoinTable(name = "Question_Tag",
     joinColumns = {
       @JoinColumn(name="question_id")
         },
     inverseJoinColumns = {
       @JoinColumn(name="tag_id")
     })
     private List<Tag> taglist;
     @Lob
     @Column(name = "source")
     private String source;
     @Lob
     @Column(name = "explanation")
     private String explanation;
     private Integer difficulty;
     @OneToMany(cascade=CascadeType.ALL,  fetch=FetchType.EAGER)
     @JoinColumn(name="question_id")
     private List<QuestionAnswer> choices = new ArrayList<QuestionAnswer>();
     //this will be used for validation.
     private Integer numCorrect;
     private String correctAnswer;
     @Basic(optional = false)
     @NonVisual
     @Column(name = "created_at")
     @Temporal(TemporalType.TIMESTAMP)
     private Date createdAt;
     @Basic(optional = false)
     @NonVisual
     @Column(name = "updated_at")
     @Temporal(TemporalType.TIMESTAMP)
     private Date updatedAt;
     
 
     public Question() {
     }
 
     public Question(Integer id) {
         this.id = id;
     }
 
     public Question(Integer id, Date createdAt, Date updatedAt) {
         this.id = id;
         this.createdAt = createdAt;
         this.updatedAt = updatedAt;
     }
 
     @NonVisual
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getQuestion() {
         return question;
     }
 
     public void setQuestion(String question) {
         this.question = question;
     }
 
     
     public String getSource() {
         return source;
     }
 
     public void setSource(String source) {
         this.source = source;
     }
 
     public String getExplanation() {
         return explanation;
     }
 
     public void setExplanation(String explanation) {
         this.explanation = explanation;
     }
 
     public Date getCreatedAt() {
         return createdAt;
     }
 
     public void setCreatedAt(Date createdAt) {
         this.createdAt = createdAt;
     }
 
     public Date getUpdatedAt() {
         return updatedAt;
     }
 
     public void setUpdatedAt(Date updatedAt) {
         this.updatedAt = updatedAt;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Question)) {
             return false;
         }
         Question other = (Question) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.preppa.web.entities.Questions[id=" + id + "]";
     }
 
     /**
      * @return the questiontype
      */
     public Questiontype getQuestiontype() {
         return questiontype;
     }
 
     /**
      * @param questiontype the questiontype to set
      */
     public void setQuestiontype(Questiontype questiontype) {
         this.questiontype = questiontype;
     }
 
     /**
      * @return the difficulty
      */
     public Integer getDifficulty() {
         return difficulty;
     }
 
     /**
      * @param difficulty the difficulty to set
      */
     public void setDifficulty(Integer difficulty) {
         this.difficulty = difficulty;
     }
 
     /**
      * @return the numCorrect
      */
     public Integer getNumCorrect() {
         return numCorrect;
     }
 
     /**
      * @param numCorrect the numCorrect to set
      */
     public void setNumCorrect(Integer numCorrect) {
         this.numCorrect = numCorrect;
     }
 
     /**
      * @return the choices
      */
     public List<QuestionAnswer> getChoices() {
         return choices;
     }
 
     /**
      * @param choices the choices to set
      */
     public void setChoices(List<QuestionAnswer> choices) {
         this.choices = choices;
     }
 
     /**
      * @return the correctAnswer
      */
     public String getCorrectAnswer() {
         return correctAnswer;
     }
 
     /**
      * @param correctAnswer the correctAnswer to set
      */
     public void setCorrectAnswer(String correctAnswer) {
         this.correctAnswer = correctAnswer;
     }
 
     /**
      * @return the taglist
      */
     public List<Tag> getTaglist() {
         return taglist;
     }
 
     /**
      * @param taglist the taglist to set
      */
     public void setTaglist(List<Tag> tagltist) {
         this.taglist = tagltist;
     }
 
   
 
 }
