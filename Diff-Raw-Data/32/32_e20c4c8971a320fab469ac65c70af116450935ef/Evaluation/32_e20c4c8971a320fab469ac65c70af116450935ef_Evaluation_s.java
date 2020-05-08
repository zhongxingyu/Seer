 package fr.adrienbrault.notetonsta.entity;
 
 import javax.persistence.*;
 
 /**
  * Author: Adrien Brault
  * Date: 12/12/11
  * Time: 13:16
  */
 
 @Entity
 public class Evaluation {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     protected Integer id;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
     protected Intervention intervention;
 
     @Basic
     protected Integer idBooster;
 
     @Basic
     protected String comment;
 
     @Basic
     protected Float speakerKnowledgeMark;
 
     @Basic
     protected Float speakerTeachingMark;
 
     @Basic
     protected Float speakerAnswersMark;
 
     @Basic
     protected Float slidesContentMark;
 
     @Basic
     protected Float slidesFormatMark;
 
     @Basic
     protected Float slidesExamplesMark;
 
    public Evaluation() {

     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Intervention getIntervention() {
         return intervention;
     }
 
     public void setIntervention(Intervention intervention) {
         this.intervention = intervention;
     }
 
     public Integer getIdBooster() {
         return idBooster;
     }
 
     public void setIdBooster(Integer idBooster) {
         this.idBooster = idBooster;
     }
 
     public String getComment() {
         return comment;
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public Float getSpeakerKnowledgeMark() {
         return speakerKnowledgeMark;
     }
 
     public void setSpeakerKnowledgeMark(Float speakerKnowledgeMark) {
         this.speakerKnowledgeMark = speakerKnowledgeMark;
     }
 
     public Float getSpeakerTeachingMark() {
         return speakerTeachingMark;
     }
 
     public void setSpeakerTeachingMark(Float speakerTeachingMark) {
         this.speakerTeachingMark = speakerTeachingMark;
     }
 
     public Float getSpeakerAnswersMark() {
         return speakerAnswersMark;
     }
 
     public void setSpeakerAnswersMark(Float speakerAnswersMark) {
         this.speakerAnswersMark = speakerAnswersMark;
     }
 
     public Float getSlidesContentMark() {
         return slidesContentMark;
     }
 
     public void setSlidesContentMark(Float slidesContentMark) {
         this.slidesContentMark = slidesContentMark;
     }
 
     public Float getSlidesFormatMark() {
         return slidesFormatMark;
     }
 
     public void setSlidesFormatMark(Float slidesFormatMark) {
         this.slidesFormatMark = slidesFormatMark;
     }
 
     public Float getSlidesExamplesMark() {
         return slidesExamplesMark;
     }
 
     public void setSlidesExamplesMark(Float slidesExamplesMark) {
         this.slidesExamplesMark = slidesExamplesMark;
     }
 
 }
