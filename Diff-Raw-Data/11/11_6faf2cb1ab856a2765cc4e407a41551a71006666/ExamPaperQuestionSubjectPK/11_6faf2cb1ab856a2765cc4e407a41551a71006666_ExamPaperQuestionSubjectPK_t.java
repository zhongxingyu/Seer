 package com.bigcay.exhubs.model;
 
 import java.io.Serializable;
 
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 public class ExamPaperQuestionSubjectPK implements Serializable {
 
 	private static final long serialVersionUID = -6834275365857565217L;
 
	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "exam_paper_id", referencedColumnName = "id")
 	private ExamPaper examPaper;
 
 	@ManyToOne(fetch = FetchType.LAZY) 
 	@JoinColumn(name = "question_subject_id", referencedColumnName = "id")
 	private QuestionSubject questionSubject;
 
 }
