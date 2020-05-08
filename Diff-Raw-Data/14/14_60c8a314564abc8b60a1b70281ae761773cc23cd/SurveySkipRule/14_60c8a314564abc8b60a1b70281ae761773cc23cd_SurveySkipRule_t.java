 package org.chai.kevin.survey;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.chai.kevin.form.FormCloner;
 import org.chai.kevin.form.FormElement.ElementCalculator;
import org.chai.kevin.form.FormSkipRule;
 import org.chai.kevin.location.DataLocationEntity;
 import org.chai.kevin.survey.SurveyElement.SurveyElementCalculator;
 import org.chai.kevin.survey.validation.SurveyEnteredQuestion;
 
 @Entity(name="SurveySkipRule")
 @Table(name="dhsst_survey_skip_rule")
 public class SurveySkipRule extends FormSkipRule {
 
 	private Survey survey;
 	private Set<SurveyQuestion> skippedSurveyQuestions = new HashSet<SurveyQuestion>();
 	
 	@ManyToOne(targetEntity=Survey.class, fetch=FetchType.LAZY)
 	@JoinColumn(nullable=false)
 	public Survey getSurvey() {
 		return survey;
 	}
 	
 	public void setSurvey(Survey survey) {
 		this.survey = survey;
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
 	public void deepCopy(FormSkipRule copy, FormCloner surveyCloner) {
 		super.deepCopy(copy, surveyCloner);
 		
 		SurveySkipRule surveyCopy = (SurveySkipRule)copy;
 		surveyCopy.setSurvey(((SurveyCloner)surveyCloner).getSurvey(getSurvey()));
 		for (SurveyQuestion question : getSkippedSurveyQuestions()) {
 			surveyCopy.getSkippedSurveyQuestions().add(((SurveyCloner)surveyCloner).getQuestion(question));
 		}
 	}
 	
 	@Override
 	public void evaluate(DataLocationEntity entity, ElementCalculator calculator) {
 		super.evaluate(entity, calculator);
 		
 		SurveyElementCalculator surveyCalculator = (SurveyElementCalculator)calculator;
 		boolean skipped = surveyCalculator.getFormValidationService().isSkipped(this, entity, calculator.getValidatableLocator());
 		for (SurveyQuestion question : getSkippedSurveyQuestions()) {
 
 			SurveyEnteredQuestion enteredQuestion = surveyCalculator.getSurveyValueService().getOrCreateSurveyEnteredQuestion(entity, question);
 			if (skipped) enteredQuestion.getSkippedRules().add(this);
 			else enteredQuestion.getSkippedRules().remove(this);
 			
 			surveyCalculator.addAffectedQuestion(enteredQuestion);
 		}
 	}
 
 }
