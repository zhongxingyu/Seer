 package org.chai.kevin.survey;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.chai.kevin.ExpressionService;
 import org.chai.kevin.util.LanguageUtils;
 import org.hibernate.SessionFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 public class SurveyCopyService {
 
 	private SessionFactory sessionFactory;
 	
	@Transactional(readOnly=false)
 	public SurveyCopy<SurveyValidationRule> copyValidationRule(SurveyValidationRule rule) {
 		SurveyCloner cloner = new SurveyCloner() {};
 		SurveyValidationRule copy = new SurveyValidationRule();
 		rule.deepCopy(copy, cloner);
 		
 		sessionFactory.getCurrentSession().save(copy);
 		return new SurveyCopy<SurveyValidationRule>(copy);
 	}
 	
 	@Transactional(readOnly=false)
 	public SurveyCopy<Survey> copySurvey(Survey survey) {
 		CompleteSurveyCloner cloner = new CompleteSurveyCloner(survey);
 		cloner.cloneTree();
 		sessionFactory.getCurrentSession().save(cloner.getSurvey());
 		
 		cloner.cloneRules();
 		for (SurveyValidationRule validationRule : cloner.getValidationRules()) {
 			sessionFactory.getCurrentSession().save(validationRule);
 		}
 		for (SurveySkipRule skipRule : cloner.getSkipRules()) {
 			sessionFactory.getCurrentSession().save(skipRule);
 		}
 		
 		return new SurveyCopy<Survey>(cloner.getSurvey(), cloner.getUnchangedValidationRules(), cloner.getUnchangedSkipRules());
 	}
 	
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	private static class CompleteSurveyCloner extends SurveyCloner {
 
 		private Survey survey;
 		private Survey copy;
 
 		private List<SurveyElement> oldElements = new ArrayList<SurveyElement>();
 		private Map<Long, SurveyObjective> objectives = new HashMap<Long, SurveyObjective>();
 		private Map<Long, SurveySection> sections = new HashMap<Long, SurveySection>();
 		private Map<Long, SurveyQuestion> questions = new HashMap<Long, SurveyQuestion>();
 		private Map<Long, SurveyElement> elements = new HashMap<Long, SurveyElement>();
 		private Map<Long, SurveySkipRule> skipRules = new HashMap<Long, SurveySkipRule>();
 		private Map<Long, SurveyValidationRule> validationRules = new HashMap<Long, SurveyValidationRule>();
 
 		private Map<SurveyValidationRule, Long> unchangedValidationRules = new HashMap<SurveyValidationRule, Long>();
 		private Map<SurveySkipRule, Long> unchangedSkipRules = new HashMap<SurveySkipRule, Long>();
 		
 		CompleteSurveyCloner(Survey survey) {
 			this.survey = survey;
 		}
 
 		Survey getSurvey() {
 			return copy;
 		}
 		
 		Map<SurveyValidationRule, Long> getUnchangedValidationRules() {
 			return unchangedValidationRules;
 		}
 
 		Map<SurveySkipRule, Long> getUnchangedSkipRules() {
 			return unchangedSkipRules;
 		}
 
 		void cloneTree() {
 			this.getSurvey(survey);
 		}
 		
 		void cloneRules() {
 			if (copy.getId() == null) throw new IllegalStateException();
 			survey.copyRules(copy, this);
 			for (SurveyElement element : oldElements) {
 				element.copyRules(elements.get(element.getId()), this);
 			}
 		}
 		
 		public Collection<SurveyValidationRule> getValidationRules() {
 			return validationRules.values();
 		}
 		
 		public Collection<SurveySkipRule> getSkipRules() {
 			return skipRules.values();
 		}
 		
 		@Override
 		public void addUnchangedValidationRule(SurveyValidationRule rule, Long id) {
 			this.unchangedValidationRules.put(rule, id);
 		}
 
 		@Override
 		public void addUnchangedSkipRule(SurveySkipRule rule, Long id) {
 			this.unchangedSkipRules.put(rule, id);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getExpression(java.lang.String, org.chai.kevin.survey.SurveyValidationRule)
 		 */
 		@Override
 		public String getExpression(String expression, SurveyValidationRule rule) {
 			Set<String> placeholders = ExpressionService.getVariables(expression);
 			Map<String, String> mapping = new HashMap<String, String>();
 			for (String placeholder : placeholders) {
 				Long id = Long.parseLong(placeholder.replace("$", ""));
 				if (elements.containsKey(id)) {
 					mapping.put(placeholder, "$"+elements.get(id).getId().toString());
 				}
 				else {
 					unchangedValidationRules.put(rule, id);
 				}
 			}
 			return ExpressionService.convertStringExpression(expression, mapping);
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getExpression(java.lang.String, org.chai.kevin.survey.SurveySkipRule)
 		 */
 		@Override
 		public String getExpression(String expression, SurveySkipRule rule) {
 			Set<String> placeholders = ExpressionService.getVariables(expression);
 			Map<String, String> mapping = new HashMap<String, String>();
 			for (String placeholder : placeholders) {
 				Long id = Long.parseLong(placeholder.replace("$", ""));
 				if (elements.containsKey(id)) {
 					mapping.put(placeholder, "$"+elements.get(id).getId().toString());
 				}
 				else {
 					unchangedSkipRules.put(rule, id);
 				}
 			}
 			return ExpressionService.convertStringExpression(expression, mapping);
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getSurvey(org.chai.kevin.survey.Survey)
 		 */
 		@Override
 		public Survey getSurvey(Survey survey) {
 			if (!survey.equals(this.survey)) throw new IllegalArgumentException();
 			if (copy == null) {
 				copy = new Survey(); 
 				survey.deepCopy(copy, this);
 				for (String language : LanguageUtils.getAvailableLanguages()) {
 					// TODO localize "copy"
 					copy.getNames().put(language, survey.getNames().get(language) + " (copy)");
 				}
 			}
 			return copy;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getObjective(org.chai.kevin.survey.SurveyObjective)
 		 */
 		@Override
 		public SurveyObjective getObjective(SurveyObjective objective) {
 			if (!objectives.containsKey(objective.getId())) {
 				SurveyObjective copy = new SurveyObjective(); 
 				objectives.put(objective.getId(), copy);
 				objective.deepCopy(copy, this);
 			}
 			return objectives.get(objective.getId());
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getSection(org.chai.kevin.survey.SurveySection)
 		 */
 		@Override
 		public SurveySection getSection(SurveySection section) {
 			if (!sections.containsKey(section.getId())) {
 				SurveySection copy = new SurveySection();
 				sections.put(section.getId(), copy);
 				section.deepCopy(copy, this);
 			}
 			return sections.get(section.getId());
 		}
 
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getQuestion(org.chai.kevin.survey.SurveyQuestion)
 		 */
 		@Override
 		public SurveyQuestion getQuestion(SurveyQuestion question) {
 			if (!questions.containsKey(question.getId())) {
 				SurveyQuestion copy = question.newInstance();
 				questions.put(question.getId(), copy);
 				question.deepCopy(copy, this);
 			}
 			return questions.get(question.getId());
 
 		}
 
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getElement(org.chai.kevin.survey.SurveyElement)
 		 */
 		@Override
 		public SurveyElement getElement(SurveyElement element) {
 			if (element == null) return null;
 			
 			if (!elements.containsKey(element.getId())) {
 				SurveyElement copy = new SurveyElement(); 
 				elements.put(element.getId(), copy);
 				element.deepCopy(copy, this);
 				oldElements.add(element);
 			}
 			
 			return elements.get(element.getId());
 		}
 
 
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getSkipRule(org.chai.kevin.survey.SurveySkipRule)
 		 */
 		@Override
 		public SurveySkipRule getSkipRule(SurveySkipRule skipRule) {
 			if (!skipRules.containsKey(skipRule.getId())) {
 				SurveySkipRule copy = new SurveySkipRule();
 				skipRules.put(skipRule.getId(), copy);
 				skipRule.deepCopy(copy, this);
 			}
 			return skipRules.get(skipRule.getId());
 		}
 
 		/* (non-Javadoc)
 		 * @see org.chai.kevin.survey.SurveyCloner#getValidationRule(org.chai.kevin.survey.SurveyValidationRule)
 		 */
 		@Override
 		public SurveyValidationRule getValidationRule(SurveyValidationRule validationRule) {
 			if (!validationRules.containsKey(validationRule.getId())) {
 				SurveyValidationRule copy = new SurveyValidationRule(); 
 				validationRules.put(validationRule.getId(), copy);
 				validationRule.deepCopy(copy, this);
 			}
 			return validationRules.get(validationRule.getId());
 		}
 		
 	}
 
 	
 }
