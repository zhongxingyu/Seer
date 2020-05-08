 package com.bigcay.exhubs.model;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "exam_papers")
 public class ExamPaper {
 
 	@Id
 	@Column(name = "id")
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Integer id;
 
 	@Column(name = "name")
 	private String name;
 
 	@Column(name = "description")
 	private String description;
 
 	@Column(name = "create_date")
 	private Date createDate;
 
 	@Column(name = "active_flg")
 	private Boolean activeFlag;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "exam_type_id")
 	private ExamType examType;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "user_id")
 	private User user;
 
	@OneToMany(mappedBy = "examPaper", fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
 	private Set<ExamPaperQuestionSubject> examPaperQuestionSubjects = new HashSet<ExamPaperQuestionSubject>();
 
 	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
 	@OrderBy("id ASC")
 	@JoinTable(name = "exam_paper_question_subject", joinColumns = { @JoinColumn(name = "exam_paper_id") }, inverseJoinColumns = { @JoinColumn(name = "question_subject_id") })
 	private Set<QuestionSubject> questionSubjects = new HashSet<QuestionSubject>();
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Date getCreateDate() {
 		return createDate;
 	}
 
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	public Boolean getActiveFlag() {
 		return activeFlag;
 	}
 
 	public void setActiveFlag(Boolean activeFlag) {
 		this.activeFlag = activeFlag;
 	}
 
 	public ExamType getExamType() {
 		return examType;
 	}
 
 	public void setExamType(ExamType examType) {
 		this.examType = examType;
 	}
 
 	public Set<ExamPaperQuestionSubject> getExamPaperQuestionSubjects() {
 		return examPaperQuestionSubjects;
 	}
 
 	public void setExamPaperQuestionSubjects(Set<ExamPaperQuestionSubject> examPaperQuestionSubjects) {
 		this.examPaperQuestionSubjects = examPaperQuestionSubjects;
 	}
 
 	public Set<QuestionSubject> getQuestionSubjects() {
 		return questionSubjects;
 	}
 
 	public void setQuestionSubjects(Set<QuestionSubject> questionSubjects) {
 		this.questionSubjects = questionSubjects;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public void addQuestionSubject(QuestionSubject questionSubject, Integer sortOrder) {
 		ExamPaperQuestionSubject examPaperQuestionSubject = new ExamPaperQuestionSubject();
 		examPaperQuestionSubject.setExamPaper(this);
 		examPaperQuestionSubject.setQuestionSubject(questionSubject);
 		examPaperQuestionSubject.setSortOrder(sortOrder);
 
 		this.getExamPaperQuestionSubjects().add(examPaperQuestionSubject);
 	}
 }
