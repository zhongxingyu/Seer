 package simulations;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.drools.compiler.DroolsParserException;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import services.NomicService;
 import services.RuleClassificationService;
 import services.ScenarioService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.simulator.Scenario;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.network.NetworkModule;
 import actionHandlers.ProposeRuleChangeActionHandler;
 import actionHandlers.VoteActionHandler;
 import actions.ProposeRuleChange;
 import agents.ProxyAgent;
 
 import com.google.inject.AbstractModule;
 
 public class SubScenarioSimulation extends NomicSimulation {
 	
 	private Logger logger = Logger.getLogger(SubScenarioSimulation.class);
 	
 	ScenarioService scenarioService;
 	
 	ProposeRuleChange testedRuleChange;
 	
 	public SubScenarioSimulation(Set<AbstractModule> modules, ScenarioService scenarioService,
 			ProposeRuleChange testedRuleChange) {
 		super(modules);
 		this.scenarioService = scenarioService;
 		this.testedRuleChange = testedRuleChange;
 	}
 
 	@Override
 	protected void addToScenario(Scenario s) {
 		LoadSuperState();
 		for (ProxyAgent proxy : scenarioService.getProxyAgents()) {
 			s.addParticipant(proxy);
 			session.insert(proxy);
 			
 			if (scenarioService.IsController(proxy.getOwner()))
 				LoadProxyRules(proxy);
 		}
 	}
 	
 	@Override
 	protected Set<AbstractModule> getModules() {
 		Set<AbstractModule> modules = new HashSet<AbstractModule>();
 		
 		modules.add(new AbstractEnvironmentModule()
 				.addParticipantGlobalEnvironmentService(NomicService.class)
 				.addParticipantGlobalEnvironmentService(RuleClassificationService.class)
 				.addParticipantEnvironmentService(ScenarioService.class)
 				.addActionHandler(ProposeRuleChangeActionHandler.class)
 				.addActionHandler(VoteActionHandler.class)
 				.setStorage(RuleStorage.class));
 		
 		modules.add(new RuleModule());
 		
 		modules.add(NetworkModule.noNetworkModule());
 		
 		return modules;
 	}
 	
 	public void LoadSuperState() {
 		StatefulKnowledgeSession superSession = scenarioService.getReplacementSession();
 		
 		session.getKnowledgeBase().addKnowledgePackages(
 				superSession.getKnowledgeBase()
 				.getKnowledgePackages());
 		
 		session.setGlobal("logger", logger);
 		session.setGlobal("rand", superSession.getGlobal("rand"));
 		//session.setGlobal("storage", superSession.getGlobal("storage"));
 	}
 	
 	public void LoadProxyRules(ProxyAgent avatar) {
 		String filePath = avatar.getProxyRulesFile();
 		try {
 			NomicService nomicService = getEnvironmentService(NomicService.class);
 			nomicService.AddRuleFile(filePath);
 			
 			nomicService.ApplyRuleChange(testedRuleChange);
 		} catch (UnavailableServiceException e) {
 			logger.warn("Nomic service unavailable for proxy agent rule addition.", e);
 		} catch (DroolsParserException e) {
 			logger.warn("Proxy rules for file " + filePath + " could not be parsed.", e);
 		}
		
		try {
			// Load active settings from super sim definitions
			RuleClassificationService ruleClassificationService = 
					getEnvironmentService(RuleClassificationService.class);
					
			ruleClassificationService.LoadRuleDefinitions(scenarioService.getSuperClassificationService()
							.getDefinitions());
		} catch (UnavailableServiceException e) {
			logger.warn("Unable to load super rule definitions for subsim run by "
					+ scenarioService.getController().getName(), e);
		}
 	}
 }
