 package com.schoolquiz.entity;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="question_answer")
 public class QuestionAnswer {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name="ID")
	private long id;
 	
	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="QUESTION_ID")
 	private Question question;
 	
	@ManyToOne( fetch=FetchType.LAZY)
 	@JoinColumn(name="ANSWER_ID")
 	private Answer answer;
 	
 	@Column(name="RIGHT")
 	private Boolean right;
 
	public long getId() {
 		return id;
 	}
 
	public void setId(long id) {
 		this.id = id;
 	}
 
 	public Question getQuestion() {
 		return question;
 	}
 
 	public void setQuestion(Question question) {
 		this.question = question;
 	}
 
 	public Answer getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(Answer answer) {
 		this.answer = answer;
 	}
 
 	public Boolean isRight() {
 		return right;
 	}
 
 	public void setRight(Boolean right) {
 		this.right = right;
 	}
 	
 	@Override
 	public String toString(){
 		return "id - "+id+"; right - "+right+"; question - "+question+"; answer - "+answer;
 	}
 	
 
 }
