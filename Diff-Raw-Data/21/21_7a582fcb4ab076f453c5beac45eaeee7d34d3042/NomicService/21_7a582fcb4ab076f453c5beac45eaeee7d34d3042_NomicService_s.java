 package services;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.compiler.DroolsParserException;
 import org.drools.definition.KnowledgePackage;
 import org.drools.definition.rule.Rule;
 import org.drools.io.Resource;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.process.ProcessInstance;
 import org.drools.runtime.rule.FactHandle;
 
 import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.event.EventBus;
 import uk.ac.imperial.presage2.core.event.EventListener;
 import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
 import uk.ac.imperial.presage2.core.simulator.Events;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import actions.ProposeRuleAddition;
 import actions.ProposeRuleChange;
 import actions.ProposeRuleModification;
 import actions.ProposeRuleRemoval;
 import actions.Vote;
 import agents.NomicAgent;
 
 import com.google.inject.Inject;
 
 import enums.RuleChangeType;
 import enums.TurnType;
 import exceptions.InvalidRuleProposalException;
 import exceptions.NoExistentRuleChangeException;
 import facts.Turn;
 
 public class NomicService extends EnvironmentService {
 
 	private final Logger logger = Logger.getLogger(this.getClass());
 	
 	StatefulKnowledgeSession session;
 	int TurnNumber = 0;
 	
 	private ArrayList<NomicAgent> agents;
 	
 	private ArrayList<NomicAgent> VotedThisTurn;
 	
 	Turn currentTurn;
 	
 	FactHandle turnHandle;
 	
 	NomicAgent placeHolderAgent = new NomicAgent(Random.randomUUID(), "placeholder");
 	
 	ProposeRuleChange currentRuleChange;
 	
 	@Inject
 	public NomicService(EnvironmentSharedStateAccess sharedState,
 			StatefulKnowledgeSession session, EventBus e) {
 		super(sharedState);
 		this.session = session;
 		e.subscribe(this);
 		currentTurn = new Turn(0, TurnType.INIT, placeHolderAgent);
 		
 		agents = new ArrayList<NomicAgent>();
 		VotedThisTurn = new ArrayList<NomicAgent>();
 	}
 	
 	@EventListener
 	public void onInitialize(Events.Initialised e) {
 		turnHandle = session.insert(currentTurn);
 	}
 	
 	@EventListener
 	public void onIncrementTime(EndOfTimeCycle e) {
 		if (currentTurn.getType() == TurnType.INIT) {
 			currentTurn.setType(TurnType.PROPOSE);
 			currentTurn.setNumber(TurnNumber);
 		}
 		else if (currentTurn.getType() == TurnType.PROPOSE && currentRuleChange != null) {
 			currentTurn.setType(TurnType.VOTE);
 		}
 		else if (currentTurn.getType() == TurnType.PROPOSE && currentRuleChange == null) {
 			currentTurn.setNumber(++TurnNumber);
 		}
 		else if (currentTurn.getType() == TurnType.VOTE) {
 			if (currentTurn.isAllVoted()) {
 				VotedThisTurn.clear();
 				if (currentRuleChange.getSucceeded()) {
 					ApplyRuleChange(currentRuleChange);
 				}
 				currentRuleChange = null;
 				currentTurn.setType(TurnType.PROPOSE);
 				currentTurn.setNumber(++TurnNumber);
 			}
 		}
 		
		session.update(turnHandle, currentTurn);
 		
 		session.fireAllRules();
 		logger.info("Next move, turn: " + currentTurn.getNumber() + ", " + currentTurn.getType());
 	}
 	
 	private boolean allVoted() {
 		for (int i=0; i < agents.size(); i++) {
 			if (!VotedThisTurn.contains(agents.get(i))) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public void registerParticipant(EnvironmentRegistrationRequest req) {
 		agents.add((NomicAgent)req.getParticipant());
 		super.registerParticipant(req);
 	}
 	
 	public boolean canProposeNow(NomicAgent agent) {
 		return currentTurn.getType() == TurnType.PROPOSE &&
 				currentTurn.getActivePlayer().getID() == agent.getID();
 	}
 	
 	public boolean canVoteNow(NomicAgent agent) {
 		return currentTurn.type == TurnType.VOTE && !VotedThisTurn.contains(agent);
 	}
 	
 	public void RemoveRule(String packageName, String ruleName) {
 		session.getKnowledgeBase().removeRule(packageName, ruleName);
 	}
 	
 	public void Vote(Vote vote) {
 		VotedThisTurn.add(vote.getVoter());
 	}
 	
 	public void ProposeRuleChange(ProposeRuleChange ruleChange) 
 			throws InvalidRuleProposalException {
 		if (currentTurn.type != TurnType.PROPOSE) {
 			throw new InvalidRuleProposalException("This turn has passed its proposal stage.");
 		}
 		
 		currentRuleChange = ruleChange;
 	}
 	
 	public ProposeRuleChange getCurrentRuleChange() throws NoExistentRuleChangeException {
 		if (currentRuleChange == null)
 			throw new NoExistentRuleChangeException("There is no valid rule change proposition.");
 		else 
 			return currentRuleChange;
 	}
 	
 	public void ApplyRuleChange(ProposeRuleChange ruleChange) {
 		RuleChangeType change = ruleChange.getRuleChangeType();
 		if (change == RuleChangeType.MODIFICATION) {
 			ProposeRuleModification ruleMod = (ProposeRuleModification)ruleChange;
 			try {
 				logger.info("Modifying rule \'" + ruleMod.getOldRuleName()
 						+ "\'");
 				RemoveRule(ruleMod.getOldRulePackage(), ruleMod.getOldRuleName());
 				addRule(ruleMod.getNewRule());
 			} catch (DroolsParserException e) {
 				logger.warn("Unable to parse new version of existing rule.", e);
 				// TODO: add old rule back
 			}
 		}
 		else if (change == RuleChangeType.ADDITION) {
 			ProposeRuleAddition ruleMod = (ProposeRuleAddition)ruleChange;
 			try {
 				addRule(ruleMod.getNewRule());
 			} catch (DroolsParserException e) {
 				logger.warn("Unable to parse new rule.", e);
 			}
 		}
 		else if (change == RuleChangeType.REMOVAL) {
 			ProposeRuleRemoval ruleMod = (ProposeRuleRemoval)ruleChange;
 			RemoveRule(ruleMod.getOldRulePackage(), ruleMod.getOldRuleName());
 		}
 	}
 	
 	public void addRule(Collection<String> imports, String ruleName,
 			Collection<String> conditions, Collection<String> actions)
 					throws DroolsParserException {
 		String rule = "";
 		
 		for(String importe : imports) {
 			rule += "import " + importe + " ";
 		}
 		
 		rule += "rule \"" + ruleName + "\" ";
 		
 		rule += "when ";
 		
 		for (String condition : conditions) {
 			rule += condition + " ";
 		}
 		
 		rule += "then ";
 		
 		for (String action : actions) {
 			rule += action + " ";
 		}
 		
 		rule += "end";
 		
 		addRule(rule);
 	}
 	
 	public void addRule(String rule) throws DroolsParserException {
 		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 		
 		Resource myResource = ResourceFactory.newReaderResource(new StringReader(rule));
 		kbuilder.add(myResource, ResourceType.DRL);
 		
 		if (kbuilder.hasErrors()) {
 			throw new DroolsParserException("Unable to parse new rule.\n"
 					+ kbuilder.getErrors().toString());
 		}
 		
 		Collection<KnowledgePackage> packages = kbuilder.getKnowledgePackages();
 		
 		session.getKnowledgeBase().addKnowledgePackages(packages);
 	}
 	
 	public Collection<Rule> getRules() {
 		Collection<KnowledgePackage> packages = session.getKnowledgeBase().getKnowledgePackages();
 		
 		Collection<Rule> rules = null;
 		
 		for (KnowledgePackage pack : packages) {
 			if (rules == null) {
 				rules = pack.getRules();
 			}
 			else {
 				rules.addAll(pack.getRules());
 			}
 		}
 		
 		return rules;
 	}
 	
 	public int getTurnNumber() {
 		return currentTurn.getNumber();
 	}
 }
