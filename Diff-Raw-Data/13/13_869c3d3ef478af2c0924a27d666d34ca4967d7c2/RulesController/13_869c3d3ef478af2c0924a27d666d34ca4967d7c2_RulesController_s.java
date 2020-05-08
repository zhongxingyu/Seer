 package br.usp.cata.web.controller;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 
 import br.usp.cata.model.CataConstraints;
 import br.usp.cata.model.ExactMatching;
 import br.usp.cata.model.Lemma;
 import br.usp.cata.model.PatternSuggestionPair;
 import br.usp.cata.model.Rule;
 import br.usp.cata.model.RuleCategories;
 import br.usp.cata.model.Source;
 import br.usp.cata.model.TypesOfRules;
 import br.usp.cata.model.TypesOfSources;
 import br.usp.cata.service.ExactMatchingService;
 import br.usp.cata.service.LemmaService;
 import br.usp.cata.service.RuleService;
 import br.usp.cata.service.SourceService;
 import br.usp.cata.web.interceptor.Transactional;
 
 
 @Resource
 public class RulesController { // TODO This class really needs some refactoring
 	
 	private final Result result;
 	private final Validator validator;
 	private final ExactMatchingService exactMatchingService;
 	private final LemmaService lemmaService;
 	private final RuleService ruleService;
 	private final SourceService sourceService;
 	private final UserSession userSession;
 	
 	public RulesController(final Result result, final Validator validator,
 		final ExactMatchingService exactMatchingService, final LemmaService lemmaService,
 		final RuleService ruleService, final SourceService sourceService, final UserSession userSession) {
 		this.result = result;
 		this.validator = validator;
 		this.exactMatchingService = exactMatchingService;
 		this.lemmaService = lemmaService;
 		this.ruleService = ruleService;
 		this.sourceService = sourceService;
 		this.userSession = userSession;
 	}
 	
 	private void includeInformation() {
 		result.include("ruleCategories", RuleCategories.values());
 		result.include("typesOfRules", TypesOfRules.values());
 		result.include("typesOfSources", TypesOfSources.values());
 		result.include("academics", sourceService.findByType(TypesOfSources.ACADEMIC_PUBLISHING));
 		result.include("books", sourceService.findByType(TypesOfSources.BOOK));
 		result.include("handbooks", sourceService.findByType(TypesOfSources.HANDBOOK));
 		result.include("urls", sourceService.findByType(TypesOfSources.INTERNET));
 		result.include("others", sourceService.findByType(TypesOfSources.OTHER));
 	}
 	
 	@Get
 	@Path("/rules/newrule")
 	public void newrule() {
 		includeInformation();
 	}
 	
 	private void validateRule(Rule newRule, List<PatternSuggestionPair> lemmas,
 			List<PatternSuggestionPair> exactMatchings, Source source) {
 		
 		if(lemmas == null && exactMatchings == null) {
 			validator.add(new ValidationMessage(
     				"Você deve cadastrar pelo menos um Lema ou Expressão Exata.", "Lema ou Expressão Exata"));
 		}
 		else {
 			if(lemmas != null) {
 				for(PatternSuggestionPair pair : lemmas) {
 					if(pair.getPattern().equals("") ||
 							pair.getSuggestions().equals("")) {
 						validator.add(new ValidationMessage(
 			    				"Você deve cadastrar um padrão e sugestões para cada Lema.", "Lema"));
 						break;
 					}
 				}
 			}
 			if(exactMatchings != null) {
 				for(PatternSuggestionPair pair : exactMatchings) {
 					if(pair.getPattern().equals("") ||
 							pair.getSuggestions().equals("")) {
 						validator.add(new ValidationMessage(
 			    				"Você deve cadastrar um padrão e sugestões para cada Expressão Exata.", "Expressão Exata"));
 						break;
 					}
 				}
 			}
 		}
 		
		if(source == null || source.getSourceID() == null)
 			validator.add(new ValidationMessage(
     				"Você deve associar uma referência à regra.", "Referência"));
 		else {
 			Source dbSource = sourceService.findByID(source.getSourceID());
 			if(dbSource == null)
 				validator.add(new ValidationMessage(
 	    				"Referência Bibliográfica inválida.", "Referência"));
 		}
 		
 	}
 	
 	private void setLemmasAndExactMatchings(Rule rule, List<PatternSuggestionPair> lemmas,
 			List<PatternSuggestionPair> exactMatchings) {		
 		if(lemmas != null) {
 			for(PatternSuggestionPair pair : lemmas) {
 				pair.setDefaultPair(false);
 				Lemma lemma = new Lemma();
 				lemma.setRule(rule);
 				lemma.setPair(pair);
 				rule.getLemmas().add(lemma);
 			}
 			lemmas.get(0).setDefaultPair(true);
 		}
 		if(exactMatchings != null) {
 			for(PatternSuggestionPair pair : exactMatchings) {
 				pair.setDefaultPair(false);
 				ExactMatching exactMatching = new ExactMatching();
 				exactMatching.setRule(rule);
 				exactMatching.setPair(pair);
 				rule.getExactMatchings().add(exactMatching);
 			}
 			exactMatchings.get(0).setDefaultPair(true);
 		}
 	}
 	
 	@Post
 	@Path("rules/newrule")
 	public void newrule(Rule newRule, List<PatternSuggestionPair> lemmas, 
 			List<PatternSuggestionPair> exactMatchings, Source source) {		
 		validateRule(newRule, lemmas, exactMatchings, source);
 		validator.onErrorRedirectTo(RulesController.class).newrule();
 		
 		if(newRule.getExplanation().equals(""))
 			newRule.setExplanation(null);
 		
 		newRule.setLemmas(new HashSet<Lemma>());
 		newRule.setExactMatchings(new HashSet<ExactMatching>());
 		setLemmasAndExactMatchings(newRule, lemmas, exactMatchings);
 		
 		newRule.setUser(userSession.getUser());
 		
 		newRule.setDate(new Date());
 		
 		//TODO Shoulde be 'false': rules registered by users won't be default rules
 		newRule.setDefaultRule(true);
 		
 		newRule.setSource(sourceService.findByID(source.getSourceID()));
 		
 		ruleService.save(newRule);
 		
 		result.include("messages", "A Regra foi cadastrada com sucesso.");		
 		result.redirectTo(IndexController.class).rules();
 	}
 	
 	@Get
 	@Path("/rules/newsource")
 	public void newsource() {
 		result.include("typesOfSources", TypesOfSources.values());
 	}
 	
 	public void validateSource(Source source) {
 		switch(source.getType()) {
 			case ACADEMIC_PUBLISHING:
 				if(source.getTitle().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				CataConstraints.emptyField, "Título"));
 				if(source.getAuthors().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				CataConstraints.emptyField, "Autor(es)"));
 				
 				if((!source.getPublisher().equals("")) || (!source.getUrl().equals("")))
 					validator.add(new ValidationMessage("Erro inesperado.", "Erro"));
 				else {
 					source.setPublisher(null);
 					source.setUrl(null);
 				}
 				
 				if(source.getInstitution().equals(""))
 					source.setInstitution(null);				
 				if(source.getDate().equals(""))
 					source.setDate(null);			
 				if(source.getMoreInformation().equals(""))
 					source.setMoreInformation(null);
 				
 				break;
 				
 			case BOOK:
 			case HANDBOOK:
 				if(source.getTitle().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				CataConstraints.emptyField, "Título"));
 				if(source.getAuthors().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				CataConstraints.emptyField, "Autor(es)"));
 				
 				if((!source.getInstitution().equals("")) || (!source.getUrl().equals("")))
 					validator.add(new ValidationMessage("Erro inesperado", "Erro"));
 				else {
 					source.setInstitution(null);
 					source.setUrl(null);
 				}
 				
 				if(source.getPublisher().equals(""))
 					source.setPublisher(null);				
 				if(source.getDate().equals(""))
 					source.setDate(null);				
 				if(source.getMoreInformation().equals(""))
 					source.setMoreInformation(null);
 				
 				break;
 				
 			case INTERNET:
 				if(source.getTitle().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				CataConstraints.emptyField, "Título"));
 				if(source.getUrl().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				CataConstraints.emptyField, "URL"));
 				
 				if(!(source.getAuthors().equals("")) || !(source.getInstitution().equals("")) ||
 						!(source.getPublisher().equals("") || !(source.getDate().equals(""))))
 					validator.add(new ValidationMessage("Erro inesperado.", "Erro"));
 				else {
 					source.setAuthors(null);
 					source.setInstitution(null);
 					source.setPublisher(null);
 					source.setDate(null);
 				}
 				
 				if(source.getMoreInformation().equals(""))
 					source.setMoreInformation(null);
 				
 				break;
 				
 			case OTHER:
 				if(source.getMoreInformation().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				CataConstraints.emptyField, "Mais informações"));
 				
 				if((!source.getAuthors().equals("")) || (!source.getInstitution().equals("")) ||
 						(!source.getPublisher().equals("")) || (!source.getTitle().equals("")) ||
 						(!source.getUrl().equals("")) || (!source.getDate().equals("")))
 					validator.add(new ValidationMessage("Erro inesperado.", "Erro"));
 				else {
 					source.setAuthors(null);
 					source.setInstitution(null);
 					source.setPublisher(null);
 					source.setTitle(null);
 					source.setUrl(null);
 					source.setDate(null);
 				}
 				break;
 				
 			default:
 				break;
 		}
 	}
 	
 	@Post
 	@Path("/rules/newsource")
 	@Transactional
 	public void newsource(Source newSource) {
 		validateSource(newSource);
 		result.include("selectedType", newSource.getType());
 		validator.onErrorRedirectTo(RulesController.class).newsource();
 		
 		newSource.setUser(userSession.getUser());
 		
 		newSource.setRegistrationDate(new Date());
 		
 		sourceService.save(newSource);
 		
 		result.include("messages", "A Referência foi cadastrada com sucesso.");
 		result.redirectTo(RulesController.class).newrule();
 	}
 	
 	private boolean userIsAuthorizedToChangeRule(Long ruleID) {
 		Rule rule = ruleService.findByID(ruleID);		
 		return rule.getUser().getUserID().equals(userSession.getUserID());
 	}
 	
 	@Get
 	@Post
 	@Path("rules/editrule")
 	@Transactional
 	public void editrule(Rule ruleToBeUpdated) {
		if(ruleToBeUpdated == null || ruleToBeUpdated.getRuleID() == null)
 			result.redirectTo(UserController.class).profile();
 		
 		if(!userIsAuthorizedToChangeRule(ruleToBeUpdated.getRuleID()))
 			validator.add(new ValidationMessage(
     				"Você não possui autorização.", "Erro"));
 		validator.onErrorRedirectTo(UserController.class).profile();
 		
 		includeInformation();
 		result.include("rule", ruleService.findByID(ruleToBeUpdated.getRuleID()));
 	}
 
 	@Post
 	@Path("rules/updaterule")
 	@Transactional
 	public void updaterule(Rule updatedRule, List<PatternSuggestionPair> lemmas,
 			List<PatternSuggestionPair> exactMatchings, Source source) {
 		if(updatedRule == null || updatedRule.getRuleID() == null)
 			result.redirectTo(UserController.class).profile();
 		
 		if(!userIsAuthorizedToChangeRule(updatedRule.getRuleID()))
 			validator.add(new ValidationMessage(
     				"Você não possui autorização.", "Erro"));
 		validator.onErrorRedirectTo(UserController.class).profile();
 		
 		Rule rule = ruleService.findByID(updatedRule.getRuleID());
		if(source == null || source.getSourceID() == null) {
 			source = new Source();
 			source.setSourceID(rule.getSource().getSourceID());
 		}
 		
 		validateRule(updatedRule, lemmas, exactMatchings, source);
 		validator.onErrorRedirectTo(RulesController.class).editrule(rule);
 		
 		// FIXME
 		for(Lemma lemma : rule.getLemmas())
 			lemmaService.delete(lemma);
 		for(ExactMatching exactMatching : rule.getExactMatchings())
 			exactMatchingService.delete(exactMatching);
 			
 		rule.setCategory(updatedRule.getCategory());
 				
 		rule.setType(updatedRule.getType());
 		
 		if(updatedRule.getExplanation().equals(""))
 			rule.setExplanation(null);
 		else
 			rule.setExplanation(updatedRule.getExplanation());
 		
 		rule.setLemmas(new HashSet<Lemma>());
 		rule.setExactMatchings(new HashSet<ExactMatching>());
 		
 		ruleService.update(rule);
 		
 		setLemmasAndExactMatchings(rule, lemmas, exactMatchings);
 		
 		rule.setSource(sourceService.findByID(source.getSourceID()));
 		
 		ruleService.update(rule);
 		
 		result.include("messages", "A Regra foi editada com sucesso.");
 		result.redirectTo(UserController.class).profile();
 	}
 	
 	@Post
 	@Path("rules/deleterule")
 	@Transactional
 	public void deleterule(Rule ruleToBeDeleted) {
		if(ruleToBeDeleted == null || ruleToBeDeleted.getRuleID() == null)
 			result.redirectTo(UserController.class).profile();
 		
 		if(!userIsAuthorizedToChangeRule(ruleToBeDeleted.getRuleID()))
 			validator.add(new ValidationMessage(
     				"Você não possui autorização.", "Erro"));
 		validator.onErrorRedirectTo(UserController.class).profile();
 		
 		ruleService.delete(ruleService.findByID(ruleToBeDeleted.getRuleID()));
 		
 		result.include("messages", "A Regra foi removida com sucesso.");
 		result.redirectTo(UserController.class).profile();
 	}
 	
 }
