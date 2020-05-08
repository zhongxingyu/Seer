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
 @Table(name = "questions")
 public class Question {
 	
 	public static int ONE_ANSWER = 1;
 	public static int MORE_THAN_ONE_ANSWER = 2;
 	public static int QUESTION_WITH_SUBQUESTIONS = 3;
 	public static int QUESTION_FROM_SUBQUESTIONS_GROUP = 4;
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name="ID")
 	private Long id;
 	
	@OneToMany(cascade={CascadeType.REFRESH}, fetch=FetchType.LAZY, mappedBy="question")
 	private List<QuestionAnswer> questionAnswerList;
 	
 	@ManyToOne(cascade={CascadeType.REFRESH}, fetch=FetchType.LAZY)
 	@JoinColumn(name="QUESTION_GROUP")
 	private QuestionGroup questionGroup;
 	
 	@OneToMany(cascade={CascadeType.REFRESH}, fetch=FetchType.LAZY, mappedBy="question")
 	private List<UserAnswer> userAnswers;
 	
 //	private List<Answer> answerList;
 	
 	@Column(name="QUESTION_TEXT")
 	private String questionText;
 	
 	@Column(name="RESPONSE_TYPE")
 	private int responseType;
 	
 	@Column(name="QUESTION_PARENT_ID")
 	private Long parentId;
 	
 	@Column(name="ENABLED")
 	private Boolean enabled;
 	
 	@Column(name="DELETED")
 	private boolean deleted;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getQuestionText() {
 		return questionText;
 	}
 
 	public void setQuestionText(String questionText) {
 		this.questionText = questionText;
 	}
 
 	public int getResponseType() {
 		return responseType;
 	}
 
 	public void setResponseType(int responseType) {
 		this.responseType = responseType;
 	}
 	
 	public List<UserAnswer> getUserAnswers() {
 		return userAnswers;
 	}
 
 	public void setUserAnswers(List<UserAnswer> userAnswers) {
 		this.userAnswers = userAnswers;
 	}
 	
 	public QuestionGroup getQuestionGroup() {
 		return questionGroup;
 	}
 
 	public void setQuestionGroup(QuestionGroup questionGroup) {
 		this.questionGroup = questionGroup;
 	}
 	
 	public List<QuestionAnswer> getQuestionAnswerList() {
 		return questionAnswerList;
 	}
 
 	public void setQuestionAnswerList(List<QuestionAnswer> questionAnswerList) {
 		this.questionAnswerList = questionAnswerList;
 	}
 	
 	
 	
 //	public List<Answer> getAnswerList() {
 //		return answerList;
 //	}
 //
 //	public void setAnswerList(List<Answer> answerList) {
 //		this.answerList = answerList;
 //	}
 
 	public Long getParentId() {
 		return parentId;
 	}
 
 	public void setParentId(Long parentId) {
 		this.parentId = parentId;
 	}
 	
 	
 	public Boolean getEnabled() {
 		return enabled;
 	}
 
 	public void setEnabled(Boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	@Override
 	public String toString(){
 		return "id - "+id+"; questionText - "+questionText+"; responseType - "+responseType;
 	}
 
 	public boolean isDeleted() {
 		return deleted;
 	}
 
 	public void setDeleted(boolean deleted) {
 		this.deleted = deleted;
 	}
 	
 
 }
