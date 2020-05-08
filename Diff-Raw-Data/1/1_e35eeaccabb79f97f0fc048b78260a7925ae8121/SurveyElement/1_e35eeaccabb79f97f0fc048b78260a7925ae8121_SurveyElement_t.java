 package org.chai.kevin.survey;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.MapKey;
 import javax.persistence.MapKeyColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Translation;
 import org.chai.kevin.data.DataElement;
 
 @Entity(name = "SurveyElement")
 @Table(name = "dhsst_survey_element")
 public class SurveyElement {
 
 	private Long id;
 	private DataElement dataElement;
 	private SurveyQuestion surveyQuestion;
 	
 	private Map<String, SurveyValidationRule> validationRules = new HashMap<String, SurveyValidationRule>();
 	private Map<String, Translation> headers = new HashMap<String, Translation>();
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@ManyToOne(optional=false, targetEntity=DataElement.class)
 	@JoinColumn(nullable=false)
 	public DataElement getDataElement() {
 		return dataElement;
 	}
 	
 	public void setDataElement(DataElement dataElement) {
 		this.dataElement = dataElement;
 	}
 
 	@OneToMany(mappedBy="surveyElement", targetEntity=SurveyValidationRule.class)
 //	@JoinTable(name="dhsst_survey_element_validation_rules")
 	@MapKeyColumn(name="prefix")
 	public Map<String, SurveyValidationRule> getValidationRules() {
 		return validationRules;
 	}
 	
 	public void setValidationRules(Map<String, SurveyValidationRule> validationRules) {
 		this.validationRules = validationRules;
 	}
 	
 	public void addValidationRule(SurveyValidationRule validationRule) {
 		validationRule.setSurveyElement(this);
 		validationRules.put(validationRule.getPrefix(), validationRule);
 	}
 	
 	@ManyToOne(targetEntity=SurveyQuestion.class, optional=false)
 	@JoinColumn(nullable=false)
 	public SurveyQuestion getSurveyQuestion() {
 		return surveyQuestion;
 	}
 	
 	public void setSurveyQuestion(SurveyQuestion surveyQuestion) {
 		this.surveyQuestion = surveyQuestion;
 	}
 	
 	@ElementCollection(targetClass=Translation.class)
 //	@AttributeOverrides({
 //		@AttributeOverride(name="value.jsonText", column=@Column(name="headers"))
 //	})
 //	@MapKey(name="prefix")
 	@JoinTable(name="dhsst_survey_element_headers")
 	public Map<String, Translation> getHeaders() {
 		return headers;
 	}
 	
 	public void setHeaders(Map<String, Translation> headers) {
 		this.headers = headers;
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
 		SurveyElement other = (SurveyElement) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	@Transient
 	public Survey getSurvey() {
 		return surveyQuestion.getSurvey();
 	}
 
 	@Transient
 	public Set<String> getOrganisationUnitGroupApplicable(){
 		return this.surveyQuestion.getOrganisationUnitGroupApplicable(this);
 	}
 
 	@Transient
 	protected void deepCopy(SurveyElement copy, SurveyCloner cloner) {
 		copy.setDataElement(getDataElement());
 		copy.setSurveyQuestion(cloner.getQuestion(getSurveyQuestion()));
 	}
 	
 	@Transient
 	protected void copyRules(SurveyElement copy, SurveyCloner cloner) {
 		for (Entry<String, SurveyValidationRule> entry : getValidationRules().entrySet()) {
 			copy.getValidationRules().put(entry.getKey(), cloner.getValidationRule(entry.getValue()));
 		}
 		for (Entry<String, Translation> entry : getHeaders().entrySet()) {
 			copy.getHeaders().put(entry.getKey(), entry.getValue());
 		}
 	}
 	
 }
