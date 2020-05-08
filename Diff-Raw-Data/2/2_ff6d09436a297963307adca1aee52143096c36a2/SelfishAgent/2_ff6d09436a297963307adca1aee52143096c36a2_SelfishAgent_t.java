 package agents;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import actions.ProposeNoRuleChange;
 import actions.ProposeRuleChange;
 import actions.ProposeRuleRemoval;
 import enums.RuleFlavor;
 import enums.VoteType;
 import facts.RuleDefinition;
 
 public class SelfishAgent extends NomicAgent {
 
 	public SelfishAgent(UUID id, String name) {
 		super(id, name);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public void incrementTime() {
 		super.incrementTime();
 	}
 	
 	@Override
 	protected ProposeRuleChange chooseProposal() {
 		
 		// first check to see if we like the current state of affairs
 		ProposeNoRuleChange noChange = new ProposeNoRuleChange(this);
 		scenarioService.RunQuerySimulation(noChange, getSubsimulationLength(noChange));
 		
 		// If we do, let's add/modify a rule
 		if (isPreferred(scenarioService.getPreference())) {
 			
 			ArrayList<RuleDefinition> rulesILike = ruleClassificationService.getAllInActiveRulesWithFlavor(RuleFlavor.WINCONDITION);
 			rulesILike.addAll(ruleClassificationService.getAllInActiveRulesWithFlavor(RuleFlavor.BENEFICIAL));
 			
 			if (!rulesILike.isEmpty()) {
 				RuleDefinition definition = rulesILike.get(rand.nextInt(rulesILike.size()));
 				
 				ProposeRuleChange ruleChange = definition.getRuleChange(this);
 				
 				scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
 				
 				if (isPreferred(scenarioService.getPreference())) {
 					return ruleChange;
 				}
 			}
 		}
 		// If we don't like the result of the current rule set, then let's remove a rule
 		else {
 			// If we don't like it and the subsim was won, someone else won, so let's remove the win conditions
 			if (scenarioService.isSimWon()) {
 				ArrayList<RuleDefinition> winRules = ruleClassificationService.getAllActiveRulesWithFlavor(RuleFlavor.WINCONDITION);
 				
 				if (!winRules.isEmpty()) {
 					// Choose a random win condition to remove, since we can't tell 'how' the subsim was won
 					RuleDefinition chosenRemoval = winRules.get(rand.nextInt(winRules.size()));
 					
 					ProposeRuleChange winRemove = new ProposeRuleRemoval(this, chosenRemoval.getName(), RuleDefinition.RulePackage);
 					
 					return winRemove;
 				}
 			}
 			// Otherwise destructive or detrimental rules might be stopping us from winning
 			else {
 				ArrayList<RuleDefinition> rules = ruleClassificationService.getAllActiveRulesWithFlavor(RuleFlavor.DESTRUCTIVE);
				rules.addAll(ruleClassificationService.getAllActiveRulesWithFlavor(RuleFlavor.DETRIMENTAL));
 				
 				if (!rules.isEmpty()) {
 					RuleDefinition chosenRemoval = rules.get(rand.nextInt(rules.size()));
 					
 					ProposeRuleRemoval destructiveRemove = new ProposeRuleRemoval(this, chosenRemoval.getName(), RuleDefinition.RulePackage);
 					
 					return destructiveRemove;
 				}
 				
 			}
 		}
 		
 		// If we've gotten this far, this agent can't decide what to propose, so we'll give up for this turn
 		return super.chooseProposal();
 	}
 	
 	@Override
 	public VoteType chooseVote(ProposeRuleChange ruleChange) {
 		logger.info("Run subsimulation for rule query now. Wish me luck.");
 		scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
 		
 		logger.info("This simulation had a preference of: " + scenarioService.getPreference());
 		
 		return chooseVoteFromProbability(scenarioService.getPreference());
 	}
 	
 	@Override
 	public String getProxyRulesFile() {
 		return "src/main/resources/SelfishProxy.drl";
 	}
 }
