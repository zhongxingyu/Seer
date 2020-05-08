 package com.ossia.test.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Set;
 
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
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.annotations.OrderBy;
 
 @Entity
 @Table(name = "T_EVALUATIONS")
 public class Evaluation implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Integer id;
 
 	@ManyToOne @JoinColumn(name = "test_id")
 	@NotNull
 	private TestSheet test;
 
 	@ManyToOne @JoinColumn(name = "profil_id")
 	@NotNull
 	private Profil profil;
 
 	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy = "evaluation") @OrderBy(clause = "question.id ASC")
 	private Set<Response> responses;
 
 	private Integer status;
 	
 	@Column(name = "start_time")
 	private Date startTime;
 	
 	@Column(name = "end_time")
 	private Date endTime;
 
 	public Evaluation() {
 	}
 	
 	public Evaluation(TestSheet test, Profil profil) {
 		super();
 		this.test = test;
 		this.profil = profil;
 	}
 
 	public Evaluation(TestSheet test, Profil profil, Set<Response> responses) {
 		super();
 		this.test = test;
 		this.profil = profil;
 		this.responses = responses;
 	}
 	
 	@Transient
 	public boolean isTestTaken(){
 		return this.status == EvaluationStatus.DONE.getCode();
 	}
 	
 	@Transient
 	public boolean isTestInProgress(){
 		return this.status == EvaluationStatus.IN_PROGRESS.getCode();
 	}	
 
 	@Transient
 	public boolean isTestAssigned(){
 		return this.status == EvaluationStatus.ASSIGNED.getCode();
  	}
 	
 	@Transient
 	public int getNbUnansweredQuestions() {
 		int nbUnansweredQuestions = 0;
 		for (Response response : responses) {
 			if (response.getReponsesChoisies().size() == 0) {
 				nbUnansweredQuestions ++;
 			}
 		}
 		
 		return nbUnansweredQuestions;
 	}
 	
 	@Transient
 	public int getNbGoodAnswers(){
 		int nbGoodAnswers = 0;
 		for (Response response : responses) {
 			if (response.isCorrect()) {
 				nbGoodAnswers ++;
 			}
 		}		
 		return nbGoodAnswers;
 	}
 	
 	/**
 	 * Gets the time needed by the candidate to pass the test.
 	 * @return Time needed in seconds
 	 */
 	@Transient
 	public int getDuration() {
 		int duration = (int) (this.endTime.getTime() - this.startTime.getTime());
 		return duration / 1000;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public TestSheet getTest() {
 		return test;
 	}
 
 	public void setTest(TestSheet test) {
 		this.test = test;
 	}
 
 	public Profil getProfil() {
 		return profil;
 	}
 
 	public void setProfil(Profil profil) {
 		this.profil = profil;
 	}
 
 	public Set<Response> getResponses() {
 		return responses;
 	}
 
 	public void setResponses(Set<Response> responses) {
 		this.responses = responses;
 	}
 
 	public Integer getStatus() {
 		return status;
 	}
 
 	public void setStatus(Integer status) {
 		this.status = status;
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public Date getEndTime() {
 		return endTime;
 	}
 
 	public void setEndTime(Date endTime) {
 		this.endTime = endTime;
 	}
 }
