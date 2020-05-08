 package com.schoolquiz.entity;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="Answers")
 public class Answer {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name="ID")
 	private long id;
 	
 	@OneToMany(cascade={CascadeType.REFRESH}, fetch=FetchType.LAZY, mappedBy="answer")
 	private List<UserAnswer> userAnswers;
 	
	@OneToMany(cascade={CascadeType.REFRESH}, fetch=FetchType.LAZY, mappedBy="answer")
 	private List<QuestionAnswer> questionAnswerList;
 	
 	@Column(name="ANSWER_TEXT")
 	private String answerText;
 	
 	@Column(name="ENABLED")
 	private Boolean enabled;
 	
 	@Column(name="DELETED")
 	private boolean deleted; 
 	
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getAnswerText() {
 		return answerText;
 	}
 
 	public void setAnswerText(String answerText) {
 		this.answerText = answerText;
 	}
 
 	
 	public List<UserAnswer> getUserAnswers() {
 		return userAnswers;
 	}
 
 	public void setUserAnswers(List<UserAnswer> userAnswers) {
 		this.userAnswers = userAnswers;
 	}
 	
 	
 
 	public Boolean getEnabled() {
 		return enabled;
 	}
 
 	public void setEnabled(Boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	@Override
 	public String toString(){
 		return "id - "+id+"; answer text - "+answerText;
 	}
 
 	public boolean isDeleted() {
 		return deleted;
 	}
 
 	public void setDeleted(boolean deleted) {
 		this.deleted = deleted;
 	}
 	
 	
 }
