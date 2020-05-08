 package org.chai.kevin.survey;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Basic;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.MapKeyJoinColumn;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Translation;
 import org.chai.kevin.survey.Survey;
 import org.chai.kevin.survey.SurveyElement;
 import org.chai.kevin.survey.SurveyQuestion;
 
 import org.chai.kevin.util.Utils;
 
 @Entity(name="SurveySkipRule")
 @Table(name="dhsst_survey_skip_rule")
 public class SurveySkipRule {
 
 	private Long id;
 	private Survey survey;
 	private String expression;
 	private Translation descriptions = new Translation();
 	
 	private Map<SurveyElement, String> skippedSurveyElements = new HashMap<SurveyElement, String>();
 	private Set<SurveyQuestion> skippedSurveyQuestions = new HashSet<SurveyQuestion>();
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@ManyToOne(targetEntity=Survey.class, fetch=FetchType.LAZY)
 	@JoinColumn(nullable=false)
 	public Survey getSurvey() {
 		return survey;
 	}
 	
 	public void setSurvey(Survey survey) {
 		this.survey = survey;
 	}
 	
 	@Basic
 	@Column(nullable=false)
 	public String getExpression() {
 		return expression;
 	}
 	
 	public void setExpression(String expression) {
 		this.expression = expression;
 	}
 	
 	@Embedded
 	@AttributeOverrides({ @AttributeOverride(name = "jsonText", column = @Column(name = "jsonDescriptions", nullable = false)) })
 	public Translation getDescriptions() {
 		return descriptions;
 	}
 
 	public void setDescriptions(Translation descriptions) {
 		this.descriptions = descriptions;
 	}
 
 	@ElementCollection
 	@CollectionTable(name="dhsst_survey_skipped_survey_elements")
 	@MapKeyJoinColumn
 	public Map<SurveyElement, String> getSkippedSurveyElements() {
 		return skippedSurveyElements;
 	}
 	
 	public void setSkippedSurveyElements(Map<SurveyElement, String> skippedSurveyElements) {
 		this.skippedSurveyElements = skippedSurveyElements;
 	}
 	
 	@Transient
 	public Set<String> getSkippedPrefixes(SurveyElement element) {
 		Set<String> result = new HashSet<String>();
 		if (skippedSurveyElements.containsKey(element)) {
 			String text = skippedSurveyElements.get(element);
 			if (text.isEmpty()) result.add(text);
 			result.addAll(Utils.split(text));
 		}
 		return result;
 	}
 	
 	@ManyToMany(targetEntity=SurveyQuestion.class)
 	@JoinTable(name="dhsst_survey_skipped_survey_questions")
 	public Set<SurveyQuestion> getSkippedSurveyQuestions() {
 		return skippedSurveyQuestions;
 	}
 	
 	public void setSkippedSurveyQuestions(Set<SurveyQuestion> skippedSurveyQuestions) {
 		this.skippedSurveyQuestions = skippedSurveyQuestions;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SurveySkipRule other = (SurveySkipRule) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	protected void deepCopy(SurveySkipRule copy, SurveyCloner surveyCloner) {
 		copy.setExpression(surveyCloner.getExpression(getExpression(), copy));
 		copy.setSurvey(surveyCloner.getSurvey(getSurvey()));
 		for (SurveyQuestion question : getSkippedSurveyQuestions()) {
 			copy.getSkippedSurveyQuestions().add(surveyCloner.getQuestion(question));
 		}
 		for (Entry<SurveyElement, String> entry : getSkippedSurveyElements().entrySet()) {
 			copy.getSkippedSurveyElements().put(surveyCloner.getElement(entry.getKey()), entry.getValue());
 		}
 	}
 
 	
 }
