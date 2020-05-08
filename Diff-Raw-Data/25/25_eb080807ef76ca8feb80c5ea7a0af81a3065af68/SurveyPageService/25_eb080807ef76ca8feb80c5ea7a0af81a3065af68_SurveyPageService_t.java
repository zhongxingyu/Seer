 /**
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.chai.kevin.survey;
 /**
  * @author Jean Kahigiso M.
  *
  */
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.Enum;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.data.Type.TypeVisitor;
 import org.chai.kevin.data.Type.ValuePredicate;
 import org.chai.kevin.data.Type.ValueType;
 import org.chai.kevin.location.CalculationEntity;
 import org.chai.kevin.location.DataEntityType;
 import org.chai.kevin.location.DataLocationEntity;
 import org.chai.kevin.survey.SurveyQuestion.QuestionType;
 import org.chai.kevin.survey.SurveyValidationService.ValidatableLocator;
 import org.chai.kevin.survey.validation.SurveyEnteredObjective;
 import org.chai.kevin.survey.validation.SurveyEnteredQuestion;
 import org.chai.kevin.survey.validation.SurveyEnteredSection;
 import org.chai.kevin.survey.validation.SurveyEnteredValue;
 import org.chai.kevin.survey.validation.SurveyLog;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.ValidatableValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.hibernate.FlushMode;
 import org.hibernate.LockMode;
 import org.hibernate.LockOptions;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 public class SurveyPageService {
 	
 	private static Log log = LogFactory.getLog(SurveyPageService.class);
 	
 	private SurveyService surveyService;
 	private SurveyValueService surveyValueService;
 	private LocationService locationService;
 	private ValueService valueService;
 	private DataService dataService;
 	private SurveyValidationService surveyValidationService;
 	private SessionFactory sessionFactory;
 	private GrailsApplication grailsApplication;
 	
 	@Transactional(readOnly = true)
 	public Survey getDefaultSurvey() {
 		return (Survey)sessionFactory.getCurrentSession()
 			.createCriteria(Survey.class).add(Restrictions.eq("active", true)).uniqueResult();
 	}
 	
 	private void collectEnums(SurveyElement element, final Map<String, Enum> enums) {
 		element.getDataElement().getType().visit(new TypeVisitor() {
 			@Override
 			public void handle(Type type, String prefix) {
 				if (type.getType() == ValueType.ENUM) {
 					if (!enums.containsKey(type.getEnumCode())) {
 						enums.put(type.getEnumCode(), dataService.findEnumByCode(type.getEnumCode()));
 					}
 				}
 			}
 		});
 	}
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocationEntity entity, SurveyQuestion currentQuestion) {
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(entity="+entity+", currentQuestion="+currentQuestion+")");
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		Map<SurveyElement, SurveyEnteredValue> elements = new HashMap<SurveyElement, SurveyEnteredValue>();
 		Map<SurveyQuestion, SurveyEnteredQuestion> questions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		
 		SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(entity, currentQuestion);
 		questions.put(currentQuestion, enteredQuestion);
 		for (SurveyElement element : currentQuestion.getSurveyElements(entity.getType())) {
 			SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 			elements.put(element, enteredValue);
 			collectEnums(element, enums);
 		}
 		
 		SurveyPage page = new SurveyPage(entity, currentQuestion.getSurvey(), null, null, null, null, questions, elements, enums);
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(...)="+page);
 		return page;
 	}
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocationEntity entity, SurveySection currentSection) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		SurveyObjective currentObjective = currentSection.getObjective();
 		Survey survey = currentObjective.getSurvey();
 		
 		Map<SurveyObjective, SurveyEnteredObjective> objectives = new HashMap<SurveyObjective, SurveyEnteredObjective>();
 		Map<SurveySection, SurveyEnteredSection> sections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyObjective objective : survey.getObjectives(entity.getType())) {
 			SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(entity, objective);
 			objectives.put(objective, enteredObjective);
 			
 			for (SurveySection section : objective.getSections(entity.getType())) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(entity, section);
 				sections.put(section, enteredSection);
 			}
 		}
 		
 		Map<SurveyQuestion, SurveyEnteredQuestion> questions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		Map<SurveyElement, SurveyEnteredValue> elements = new HashMap<SurveyElement, SurveyEnteredValue>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		for (SurveyQuestion question : currentSection.getQuestions(entity.getType())) {
 			SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(entity, question);
 			questions.put(question, enteredQuestion);
 			
 			for (SurveyElement element : question.getSurveyElements(entity.getType())) {
 				SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 				elements.put(element, enteredValue);
 				collectEnums(element, enums);
 			}
 		}
 		
 		SurveyPage page = new SurveyPage(entity, survey, currentObjective, currentSection, objectives, sections, questions, elements, enums);
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(...)="+page);
 		return page;
 	}
 	
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocationEntity entity, SurveyObjective currentObjective) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		Survey survey = currentObjective.getSurvey();
 		
 		Map<SurveyObjective, SurveyEnteredObjective> objectives = new HashMap<SurveyObjective, SurveyEnteredObjective>();
 		Map<SurveySection, SurveyEnteredSection> sections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyObjective objective : survey.getObjectives(entity.getType())) {
 			SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(entity, objective);
 			objectives.put(objective, enteredObjective);
 			
 			for (SurveySection section : objective.getSections(entity.getType())) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(entity, section);
 				sections.put(section, enteredSection);
 			}
 		}
 
 		Map<SurveyQuestion, SurveyEnteredQuestion> questions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		Map<SurveyElement, SurveyEnteredValue> elements = new HashMap<SurveyElement, SurveyEnteredValue>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		for (SurveySection section : currentObjective.getSections(entity.getType())) {
 			section = (SurveySection)sessionFactory.getCurrentSession().get(SurveySection.class, section.getId());
 			for (SurveyQuestion question : section.getQuestions(entity.getType())) {
 				SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(entity, question);
 				questions.put(question, enteredQuestion);
 				
 				for (SurveyElement element : question.getSurveyElements(entity.getType())) {
 					SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 					elements.put(element, enteredValue);
 					collectEnums(element, enums);
 				}
 			}
 		}
 		
 		SurveyPage page = new SurveyPage(entity, survey, currentObjective, null, objectives, sections, questions, elements, enums);
 		if (log.isDebugEnabled()) log.debug("getSurveyPage(...)="+page);
 		return page;
 	}
 	
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPagePrint(DataLocationEntity entity,Survey survey) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		DataEntityType entityUnitGroup = entity.getType();
 		
 		Map<SurveyElement, SurveyEnteredValue> elements = new LinkedHashMap<SurveyElement, SurveyEnteredValue>();
 		Map<String, Enum> enums = new HashMap<String, Enum>();
 		
 		for (SurveyObjective objective : survey.getObjectives(entityUnitGroup)) {
 			for (SurveySection section : objective.getSections(entityUnitGroup)) {
 				for (SurveyQuestion question : section.getQuestions(entityUnitGroup)) {
 					for (SurveyElement element : question.getSurveyElements(entityUnitGroup)) {
 						SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 						elements.put(element, enteredValue);
 						collectEnums(element, enums);
 					}
 				}
 			}
 
 		}
 		return new SurveyPage(entity, survey, null, null, null, null,null, elements, enums);
 	}
 	
 
 	@Transactional(readOnly = false)
 	public SurveyPage getSurveyPage(DataLocationEntity entity, Survey survey) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		
 		Map<SurveyObjective, SurveyEnteredObjective> objectives = new HashMap<SurveyObjective, SurveyEnteredObjective>();
 		Map<SurveySection, SurveyEnteredSection> sections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyObjective objective : survey.getObjectives(entity.getType())) {
 			SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(entity, objective);
 			objectives.put(objective, enteredObjective);
 			
 			for (SurveySection section : objective.getSections(entity.getType())) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(entity, section);
 				sections.put(section, enteredSection);
 			}
 		}
 		return new SurveyPage(entity, survey, null, null, objectives, sections, null, null, null);
 	}
 	
 	@Transactional(readOnly = false)
 	public void refresh(CalculationEntity entity, Survey survey, boolean closeIfComplete) {
 		List<DataLocationEntity> facilities = locationService.getDataEntities(entity);
 	
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 //		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		for (DataLocationEntity facility : facilities) {
 			survey = (Survey)sessionFactory.getCurrentSession().load(Survey.class, survey.getId());
 			facility = (DataLocationEntity)sessionFactory.getCurrentSession().get(DataLocationEntity.class, facility.getId());
 
 			getMe().refreshSurveyForFacilityWithNewTransaction(facility, survey, closeIfComplete);
 			sessionFactory.getCurrentSession().clear();
 		}
 	}
 	
 	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
 	public void refreshSurveyForFacilityWithNewTransaction(DataLocationEntity facility, Survey survey, boolean closeIfComplete) {
 		refreshSurveyForFacility(facility, survey, closeIfComplete);
 	}
 	
 	@Transactional(readOnly = false)
 	public void refreshSurveyForFacility(DataLocationEntity facility, Survey survey, boolean closeIfComplete) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 //		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		Set<SurveyObjective> validObjectives = new HashSet<SurveyObjective>(survey.getObjectives(facility.getType()));
 		for (SurveyObjective objective : survey.getObjectives()) {
 			if (validObjectives.contains(objective)) refreshObjectiveForFacility(facility, objective, closeIfComplete);
 			else deleteSurveyEnteredObjective(objective, facility);
 		}
 	}
 	
 	private void refreshObjectiveForFacility(DataLocationEntity facility, SurveyObjective objective, boolean closeIfComplete) {
 		Set<SurveySection> validSections = new HashSet<SurveySection>(objective.getSections(facility.getType()));
 		for (SurveySection section : objective.getSections()) {
 			if (validSections.contains(section)) refreshSectionForFacility(facility, section);
 			else deleteSurveyEnteredSection(section, facility);
 		}
 		
 		SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(facility, objective);
 		setObjectiveStatus(enteredObjective, facility);
 		if (closeIfComplete && enteredObjective.isComplete() && !enteredObjective.isInvalid()) enteredObjective.setClosed(true); 
 		surveyValueService.save(enteredObjective);
 	}
 	
 //	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
 //	public void refreshSectionForFacilityWithNewTransaction(DataLocationEntity facility, SurveySection section) {
 //		refreshSectionForFacility(facility, section);
 //	}
 	
 	@Transactional(readOnly = false)
 	public void refreshSectionForFacility(DataLocationEntity facility, SurveySection section) {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 //		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		Set<SurveyQuestion> validQuestions = new HashSet<SurveyQuestion>(section.getQuestions(facility.getType()));
 		for (SurveyQuestion question : section.getQuestions()) {
 			if (validQuestions.contains(question)) refreshQuestionForFacility(facility, question);
 			else deleteSurveyEnteredQuestion(question, facility);
 		}
 		SurveyEnteredSection enteredSection = getSurveyEnteredSection(facility, section);
 		setSectionStatus(enteredSection, facility);
 		surveyValueService.save(enteredSection);
 	}
 	
 	private void refreshQuestionForFacility(DataLocationEntity facility, SurveyQuestion question) {
 		Set<SurveyElement> validElements = new HashSet<SurveyElement>(question.getSurveyElements(facility.getType()));
 		for (SurveyElement element : question.getSurveyElements()) {
 			if (validElements.contains(element)) refreshElementForFacility(facility, element);
 			else deleteSurveyEnteredValue(element, facility);
 		}
 		
 		SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(facility, question);
 		setQuestionStatus(enteredQuestion, facility);
 		surveyValueService.save(enteredQuestion);
 	}
 	
 	private void refreshElementForFacility(DataLocationEntity facility, SurveyElement element) {
 		Survey survey = element.getSurvey();
 		
 		SurveyEnteredValue enteredValue = getSurveyEnteredValue(facility, element);
 		RawDataElementValue rawDataElementValue = valueService.getDataElementValue(element.getDataElement(), facility, survey.getPeriod());
 		if (rawDataElementValue != null) enteredValue.setValue(rawDataElementValue.getValue());
 		else enteredValue.setValue(Value.NULL_INSTANCE());
 		if (survey.getLastPeriod() != null) {
 			RawDataElementValue lastDataValue = valueService.getDataElementValue(element.getDataElement(), facility, survey.getLastPeriod());
 			if (lastDataValue != null) enteredValue.setLastValue(lastDataValue.getValue());
 			else enteredValue.setLastValue(Value.NULL_INSTANCE());
 		}
 		surveyValueService.save(enteredValue);
 	}
 	
 	// returns the list of modified elements/questions/sections/objectives (skip, validation, etc..)
 	// we set the isolation level on READ_UNCOMMITTED to avoid deadlocks because in READ_COMMITTED
 	// mode, a write lock is acquired at the beginning and never released till this method terminates
 	// which causes other sessions calling this method to timeout
 	@Transactional(readOnly = false)
 	public SurveyPage modify(DataLocationEntity entity, SurveyObjective objective, List<SurveyElement> elements, Map<String, Object> params) {
 		if (log.isDebugEnabled()) log.debug("modify(entity="+entity+", elements="+elements+")");
 		
 		// we acquire a write lock on the objective
 		// this won't change anything for MyISAM tables
 		SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(entity, objective);
 		sessionFactory.getCurrentSession().buildLockRequest(LockOptions.NONE).setLockMode(LockMode.PESSIMISTIC_WRITE).lock(enteredObjective);
 		
 		SurveyPage surveyPage = null;
 		// if the objective is not closed, we go on with the save
 		if (!enteredObjective.isClosed()) {
 			Map<SurveyElement, SurveyEnteredValue> affectedElements = new HashMap<SurveyElement, SurveyEnteredValue>();
 			// first we save the values
 			for (SurveyElement element : elements) {
 				if (log.isDebugEnabled()) log.debug("setting new value for element: "+element);
 				
 				SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 				ValidatableValue validatableValue = enteredValue.getValidatable();
 				
 				// merge the values
 				// this modifies the value object accordingly
 				validatableValue.mergeValue(params, "elements["+element.getId()+"].value");
 				
 				// set the value and save
 				// here, a write lock is acquired on the SurveyEnteredValue that will be kept
 				// till the end of the transaction, if in READ_COMMITTED isolation mode, a timeout
 				// is likely to occur because the transaction is quite long
 				affectedElements.put(element, enteredValue);
 				
 				// if it is a checkbox question, we need to reset the values to null
 				// FIXME THIS IS A HACK
 				resetCheckboxQuestion(entity, element, affectedElements);
 			}
 			// we evaluate the rules
 			surveyPage = evaluateRulesAndSave(entity, elements, affectedElements);
 		}
 
 		if (log.isDebugEnabled()) log.debug("modify(...)="+surveyPage);
 		return surveyPage;
 	}
 
 	
 	private ValidatableLocator getLocator() {
 		return new ValidatableLocator() {
 			
 			@Override
 			public ValidatableValue getValidatable(Long id, DataLocationEntity location) {
 				SurveyElement element = surveyService.getSurveyElement(id);
 				SurveyEnteredValue enteredValue = surveyValueService.getSurveyEnteredValue(element, location);
 				return enteredValue.getValidatable();
 			}
 		};
 	}
 
 	private SurveyPage evaluateRulesAndSave(DataLocationEntity entity, List<SurveyElement> elements, Map<SurveyElement, SurveyEnteredValue> affectedElements) {  
 		if (log.isDebugEnabled()) log.debug("evaluateRulesAndSave(entity="+entity+", elements="+elements+")");
 		
 		// second we get the rules that could be affected by the changes
 		Set<SurveyValidationRule> validationRules = new HashSet<SurveyValidationRule>();
 		Set<SurveySkipRule> skipRules = new HashSet<SurveySkipRule>();
 		for (SurveyElement element : elements) {
 			if (log.isDebugEnabled()) log.debug("getting skip and validation rules for element: "+element);
 
 			validationRules.addAll(surveyService.searchValidationRules(element, entity.getType()));
 			skipRules.addAll(surveyService.searchSkipRules(element));
 		}
 		
 		// third we evaluate those rules
 		Map<SurveyQuestion, SurveyEnteredQuestion> affectedQuestions = new HashMap<SurveyQuestion, SurveyEnteredQuestion>();
 		for (SurveyValidationRule validationRule : validationRules) {
 			if (log.isDebugEnabled()) log.debug("getting invalid prefixes for validation rule: "+validationRule);
 			
 			Set<String> prefixes = surveyValidationService.getInvalidPrefix(validationRule, entity, getLocator());
 
 			SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, validationRule.getSurveyElement());
 			enteredValue.getValidatable().setInvalid(validationRule, prefixes);
 			
 			affectedElements.put(validationRule.getSurveyElement(), enteredValue);
 		}
 		
 		for (SurveySkipRule surveySkipRule : skipRules) {
 			for (SurveyElement element : surveySkipRule.getSkippedSurveyElements().keySet()) {
 				if (log.isDebugEnabled()) log.debug("getting skipped prefixes for skip rule: "+surveySkipRule+", element: "+element);
 				
 				Set<String> prefixes = surveyValidationService.getSkippedPrefix(element, surveySkipRule, entity, getLocator());
 
 				SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 				enteredValue.getValidatable().setSkipped(surveySkipRule, prefixes);
 				
 				affectedElements.put(element, enteredValue);
 			}
 
 			boolean skipped = surveyValidationService.isSkipped(surveySkipRule, entity, getLocator());
 			for (SurveyQuestion question : surveySkipRule.getSkippedSurveyQuestions()) {
 				
 				SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(entity, question);
 				if (skipped) enteredQuestion.getSkippedRules().add(surveySkipRule);
 				else enteredQuestion.getSkippedRules().remove(surveySkipRule);
 				
 				affectedQuestions.put(question, enteredQuestion);
 			}
 		}
 		
 		// fourth we propagate the affected changes up the survey tree and save
 		if (log.isDebugEnabled()) log.debug("propagating changes up the survey tree");
 		for (SurveyEnteredValue element : affectedElements.values()) {
 			SurveyQuestion question = element.getSurveyElement().getSurveyQuestion();
 			if (!affectedQuestions.containsKey(question)) {
 				SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(entity, question);
 				affectedQuestions.put(question, enteredQuestion);
 			}
 		}
 		
 		Map<SurveySection, SurveyEnteredSection> affectedSections = new HashMap<SurveySection, SurveyEnteredSection>();
 		for (SurveyEnteredQuestion question : affectedQuestions.values()) {
 			// we set the question status correctly and save
 			setQuestionStatus(question, entity);
 			
 			SurveySection section = question.getQuestion().getSection();
 			if (!affectedSections.containsKey(section)) {
 				SurveyEnteredSection enteredSection = getSurveyEnteredSection(entity, section);
 				affectedSections.put(section, enteredSection);
 			}
 			
 		}
 		
 		Map<SurveyObjective, SurveyEnteredObjective> affectedObjectives = new HashMap<SurveyObjective, SurveyEnteredObjective>();
 		for (SurveyEnteredSection section : affectedSections.values()) {
 			// we set the section status correctly and save
 			setSectionStatus(section, entity);
 			
 			SurveyObjective objective = section.getSection().getObjective();
 			if (!affectedObjectives.containsKey(objective)) {
 				SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(entity, objective);
 				affectedObjectives.put(objective, enteredObjective);
 			}
 		}
 		
 		for (SurveyEnteredObjective objective : affectedObjectives.values()) {
 			// if the objective is not closed and available
 			// we set the objective status correctly and save
 			setObjectiveStatus(objective, entity);
 		}
 		
 		// fifth we save all the values
 		for (SurveyEnteredValue surveyEnteredValue : affectedElements.values()) {
 			surveyValueService.save(surveyEnteredValue);
 		}
 		for (SurveyEnteredQuestion surveyEnteredQuestion : affectedQuestions.values()) {
 			surveyValueService.save(surveyEnteredQuestion);
 		}
 		for (SurveyEnteredSection surveyEnteredSection : affectedSections.values()) {
 			surveyValueService.save(surveyEnteredSection);
 		}
 		for (SurveyEnteredObjective surveyEnteredObjective : affectedObjectives.values()) {
 			surveyValueService.save(surveyEnteredObjective);
 		}
 		
 		return new SurveyPage(entity, null, null, null, affectedObjectives, affectedSections, affectedQuestions, affectedElements, null);
 	}
 
 	// FIXME HACK 
 	// TODO get rid of this
 	private void resetCheckboxQuestion(DataLocationEntity entity, SurveyElement element, Map<SurveyElement, SurveyEnteredValue> affectedElements) {
 		if (log.isDebugEnabled()) log.debug("question is of type: "+element.getSurveyQuestion().getType());
 		if (element.getSurveyQuestion().getType() == QuestionType.CHECKBOX) {
 			if (log.isDebugEnabled()) log.debug("checking if checkbox question needs to be reset");
 			boolean reset = true;
 			for (SurveyElement elementInQuestion : element.getSurveyQuestion().getSurveyElements(entity.getType())) {
 				SurveyEnteredValue enteredValueForElementInQuestion = getSurveyEnteredValue(entity, elementInQuestion);
 
 				if (enteredValueForElementInQuestion.getValue().getBooleanValue() == Boolean.TRUE) reset = false;
 			}
 			if (log.isDebugEnabled()) log.debug("resetting checkbox question: "+reset);
 			for (SurveyElement elementInQuestion : element.getSurveyQuestion().getSurveyElements(entity.getType())) {
 				SurveyEnteredValue enteredValueForElementInQuestion = getSurveyEnteredValue(entity, elementInQuestion);
 
 				if (reset) enteredValueForElementInQuestion.getValue().setJsonObject(Value.NULL_INSTANCE().getJsonObject());
 				else if (enteredValueForElementInQuestion.getValue().isNull()) {
 					enteredValueForElementInQuestion.getValue().setJsonObject(enteredValueForElementInQuestion.getType().getValue(false).getJsonObject());
 				}
 				
 				affectedElements.put(elementInQuestion, enteredValueForElementInQuestion);
 			}
 		}
 	}
 	
 	private void setObjectiveStatus(SurveyEnteredObjective objective, DataLocationEntity entity) {
 		Boolean complete = true;
 		Boolean invalid = false;
 		for (SurveySection section : objective.getObjective().getSections(entity.getType())) {
 			SurveyEnteredSection enteredSection = getSurveyEnteredSection(entity, section);
 			if (!enteredSection.isComplete()) complete = false;
 			if (enteredSection.isInvalid()) invalid = true;
 		}
 		objective.setComplete(complete);
 		objective.setInvalid(invalid);
 	}
 	
 	private void setSectionStatus(SurveyEnteredSection section, DataLocationEntity entity) {
 		Boolean complete = true;
 		Boolean invalid = false;
 		for (SurveyQuestion question : section.getSection().getQuestions(entity.getType())) {
 			SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(entity, question);
 			if (!enteredQuestion.isComplete() && !enteredQuestion.isSkipped()) complete = false;
 			if (enteredQuestion.isInvalid() && !enteredQuestion.isSkipped()) invalid = true;
 		}
 		section.setInvalid(invalid);
 		section.setComplete(complete);
 	}
 	
 	private void setQuestionStatus(SurveyEnteredQuestion question, DataLocationEntity entity) {
 		Boolean complete = true;
 		Boolean invalid = false;
 		
 		// TODO replace this method by a call to the survey element service
 		for (SurveyElement element : question.getQuestion().getSurveyElements(entity.getType())) {
 			SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 			if (!enteredValue.getValidatable().isComplete()) complete = false;
 			if (enteredValue.getValidatable().isInvalid()) invalid = true;
 		}
 		question.setInvalid(invalid);
 		question.setComplete(complete);
 	}
 	
 	@Transactional(readOnly = false)
 	public boolean submit(DataLocationEntity entity, SurveyObjective objective) {
 		
 		// first we make sure that the objective is valid and complete, so we revalidate it
 		List<SurveyElement> elements = objective.getElements(entity.getType());
 		evaluateRulesAndSave(entity, elements, new HashMap<SurveyElement, SurveyEnteredValue>());
 		
 		// we get the updated survey and work from that
 		SurveyPage surveyPage = getSurveyPage(entity, objective);
 		if (surveyPage.canSubmit(objective)) {
 			// save all the values to data values
 			for (SurveyElement element : elements) {
 				SurveyEnteredValue enteredValue = getSurveyEnteredValue(entity, element);
 				Value valueToSave = null;
 				// if the question is skipped we save NULL
 				SurveyEnteredQuestion enteredQuestion = getSurveyEnteredQuestion(entity, element.getSurveyQuestion());
 				if (enteredQuestion.isSkipped()) {
 					valueToSave = Value.NULL_INSTANCE();
 				}
 				else {
 					final Type type = enteredValue.getType();
 					valueToSave = new Value(enteredValue.getValue().getJsonValue());
 					type.transformValue(valueToSave, new ValuePredicate() {
 						@Override
 						public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 							// if it is skipped we return NULL
 							if (currentValue.getAttribute("skipped") != null) currentValue.setJsonValue(Value.NULL_INSTANCE().getJsonValue());
 							// we remove the attributes
 							currentValue.setAttribute("skipped", null);
 							currentValue.setAttribute("invalid", null);
 							currentValue.setAttribute("warning", null);
 							
 							return true;
 						}
 					});
 				}
 				
 				RawDataElementValue rawDataElementValue = valueService.getDataElementValue(element.getDataElement(), entity, objective.getSurvey().getPeriod());
 				if (rawDataElementValue == null) {
 					rawDataElementValue = new RawDataElementValue(element.getDataElement(), entity, objective.getSurvey().getPeriod(), null);
 				}
 				rawDataElementValue.setValue(valueToSave);
 				
 				rawDataElementValue.setTimestamp(new Date());
 				valueService.save(rawDataElementValue);
 			}
 			
 			// close the objective
 			SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(entity, objective);
 			enteredObjective.setClosed(true);
 			surveyValueService.save(enteredObjective);
 	
 			// log the event
 			logSurveyEvent(entity, objective, "submit");
 			
 			return true;
 		}
 		else return false;
 	}
 
 	private void logSurveyEvent(DataLocationEntity entity, SurveyObjective objective, String event) {
 		SurveyLog surveyLog = new SurveyLog(objective.getSurvey(), objective, entity);
 		surveyLog.setEvent(event);
 		surveyLog.setTimestamp(new Date());
 		sessionFactory.getCurrentSession().save(surveyLog);
 	}
 	
 	public void reopen(DataLocationEntity entity, SurveyObjective objective) {
 		SurveyEnteredObjective enteredObjective = getSurveyEnteredObjective(entity, objective); 
 		enteredObjective.setClosed(false);
 		surveyValueService.save(enteredObjective);
 	}
 	
 	private SurveyEnteredObjective getSurveyEnteredObjective(DataLocationEntity entity, SurveyObjective surveyObjective) {
 		SurveyEnteredObjective enteredObjective = surveyValueService.getSurveyEnteredObjective(surveyObjective, entity);
 		if (enteredObjective == null) {
 			enteredObjective = new SurveyEnteredObjective(surveyObjective, entity, false, false, false);
 //			setObjectiveStatus(enteredObjective, entity);
 			surveyValueService.save(enteredObjective);
 		}
 		return enteredObjective;
 	}
 	
 	private SurveyEnteredSection getSurveyEnteredSection(DataLocationEntity entity, SurveySection surveySection) {
 		SurveyEnteredSection enteredSection = surveyValueService.getSurveyEnteredSection(surveySection, entity);
 		if (enteredSection == null) {
 			enteredSection = new SurveyEnteredSection(surveySection, entity, false, false);
 //			setSectionStatus(enteredSection, entity);
 			surveyValueService.save(enteredSection);
 		}
 		return enteredSection;
 	}
 	
 	private SurveyEnteredQuestion getSurveyEnteredQuestion(DataLocationEntity entity, SurveyQuestion surveyQuestion) {
 		SurveyEnteredQuestion enteredQuestion = surveyValueService.getSurveyEnteredQuestion(surveyQuestion, entity);
 		if (enteredQuestion == null) {
 			enteredQuestion = new SurveyEnteredQuestion(surveyQuestion, entity, false, false);
 //			setQuestionStatus(enteredQuestion, entity);
 			surveyValueService.save(enteredQuestion);
 		}
 		return enteredQuestion;
 	}
 	
 	private SurveyEnteredValue getSurveyEnteredValue(DataLocationEntity entity, SurveyElement element) {
 		SurveyEnteredValue enteredValue = surveyValueService.getSurveyEnteredValue(element, entity);
 		if (enteredValue == null) {
 //			Value lastValue = null;
 //			if (element.getSurvey().getLastPeriod() != null) {
 //				RawDataElementValue lastDataValue = valueService.getValue(element.getDataElement(), entity(), element.getSurvey().getLastPeriod());
 //				if (lastDataValue != null) lastValue = lastDataValue.getValue();
 //			}
 			enteredValue = new SurveyEnteredValue(element, entity, Value.NULL_INSTANCE(), null);
 			surveyValueService.save(enteredValue);
 		}
 		return enteredValue;
 	}
 
 	private void deleteSurveyEnteredObjective(SurveyObjective objective, DataLocationEntity entity) {
 		SurveyEnteredObjective enteredObjective = surveyValueService.getSurveyEnteredObjective(objective, entity);
 		if (enteredObjective != null) surveyValueService.delete(enteredObjective); 
 		
 		for (SurveySection section : objective.getSections()) {
 			deleteSurveyEnteredSection(section, entity);
 		}
 	}
 
 	private void deleteSurveyEnteredSection(SurveySection section, DataLocationEntity entity) {
 		SurveyEnteredSection enteredSection = surveyValueService.getSurveyEnteredSection(section, entity);
 		if (enteredSection != null) surveyValueService.delete(enteredSection);
 		
 		for (SurveyQuestion question : section.getQuestions()) {
 			deleteSurveyEnteredQuestion(question, entity);
 		}
 	}
 
 	private void deleteSurveyEnteredQuestion(SurveyQuestion question, DataLocationEntity entity) {
 		SurveyEnteredQuestion enteredQuestion = surveyValueService.getSurveyEnteredQuestion(question, entity);
 		if (enteredQuestion != null) surveyValueService.delete(enteredQuestion);
 		
 		for (SurveyElement element : question.getSurveyElements()) {
 			deleteSurveyEnteredValue(element, entity);
 		}
 	}
 
 	private void deleteSurveyEnteredValue(SurveyElement element, DataLocationEntity entity) {
 		SurveyEnteredValue enteredValue = surveyValueService.getSurveyEnteredValue(element, entity);
 		if (enteredValue != null) surveyValueService.delete(enteredValue);
 	}
 	
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 	
 	public void setSurveyValueService(SurveyValueService surveyValueService) {
 		this.surveyValueService = surveyValueService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setSurveyValidationService(SurveyValidationService surveyValidationService) {
 		this.surveyValidationService = surveyValidationService;
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setSurveyService(SurveyService surveyService) {
 		this.surveyService = surveyService;
 	}
 
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setGrailsApplication(GrailsApplication grailsApplication) {
 		this.grailsApplication = grailsApplication;
 	}
 	
 	// for internal call through transactional proxy
 	private SurveyPageService getMe() {
 		return grailsApplication.getMainContext().getBean(SurveyPageService.class);
 	}
 }
