 package agents;
 
 import java.util.Collection;
 import java.util.Random;
 import java.util.Set;
 import java.util.UUID;
 
 import org.drools.definition.rule.Rule;
 
 import services.NomicService;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 import actions.ProposeRuleChange;
 import actions.ProposeRuleModification;
 import actions.ProposeRuleRemoval;
 import actions.Vote;
 import enums.VoteType;
 import exceptions.NoExistentRuleChangeException;
 
 public class NomicAgent extends AbstractParticipant {
 
 	String ReverseOrderRule = "import agents.NomicAgent "
 			+ "import facts.* "
 			+ "global org.apache.log4j.Logger logger "
 			+ "rule \"Whose backwards turn is it\" "
 			+ "when"
 			+ "	$agent : NomicAgent($ID : sequentialID)"
			+ "	$n : NomicNumber(getAttribute().compareTo(\"numberOfAgents\") == 0)"
			+ "	$turn : Turn((($n.getNumber() - 1)  - (number % $n.getNumber())) == ($ID) && activePlayer != $agent)"
 			+ "then"
 			+ "	logger.info(\"It's this guy's turn: \" + $agent.getName());"
 			+ "	modify ($turn) {"
 			+ "		setActivePlayer($agent)"
 			+ "	};"
 			+ "end";
 	
 	private int SequentialID;
 	
 	NomicService nomicService;
 
 	public NomicAgent(UUID id, String name) {
 		super(id, name);
 	}
 
 	@Override
 	protected void processInput(Input arg0) {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	protected Set<ParticipantSharedState> getSharedState() {
 		Set<ParticipantSharedState> ss = super.getSharedState();
 		ss.add(new ParticipantSharedState("test", getName(), getID()));
 		return ss;
 	}
 	
 	@Override
 	public void initialise() {
 		super.initialise();
 		
 		try {
 			this.nomicService = getEnvironmentService(NomicService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn("Couldn't get Nomic Environment Service.", e);
 		}
 	}
 	
 	@Override
 	public void incrementTime() {
 		if (nomicService.canProposeNow(this)) {
 			doRuleChanges();
 		}
 		else if (nomicService.canVoteNow(this)) {
 			try {
 				doVoting();
 			} catch (NoExistentRuleChangeException e) {
 				logger.warn("Even though I can vote now, there is no rule to change.");
 			}
 		}
 		else {
 			logger.info("I've decided to do nothing this turn.");
 		}
 		super.incrementTime();
 	}
 	
 	private void doRuleChanges() {
 		logger.info("It's my turn!");
 		
 		Collection<Rule> rules= nomicService.getRules();
 		
 		String oldRuleName = "Whose turn is it";
 		String newRuleName = "Whose backwards turn is it";
 		boolean success = false;
 		for (Rule rule : rules) {
 			if (rule.getName().compareTo(oldRuleName) == 0) {
 				ProposeRuleModification ruleMod = 
 						new ProposeRuleModification(this, 
 								ReverseOrderRule, oldRuleName, rule.getPackageName());
 				
 				logger.info("Proposing turn order modification.");
 				
 				try {
 					environment.act(ruleMod, getID(), authkey);
 					success = true;
 				} catch (ActionHandlingException e) {
 					logger.info("Failed to modify rule.", e);
 				}
 			}
 			if (rule.getName().compareTo(newRuleName) == 0) {
 				
 			}
 		}
 		
 		if (!success) {
 			logger.info("No rule changes from me.");
 		}
 	}
 	
 	private void doVoting() throws NoExistentRuleChangeException {
 		ProposeRuleChange ruleChange = nomicService.getCurrentRuleChange();
 		
 		Vote vote = new Vote(this, chooseVote(ruleChange));
 		boolean success = false;
 		try {
 			environment.act(vote, getID(), authkey);
 			success = true;
 		} catch (ActionHandlingException e) {
 			logger.warn("My attempt to vote " + vote.getVote() + " has failed.");
 			e.printStackTrace();
 		}
 		
 		if (success) {
 			logger.info("I am voting " + vote.getVote() + " for this rule change.");
 		}
 	}
 	
 	public int getSequentialID() {
 		return SequentialID;
 	}
 	
 	public VoteType chooseVote(ProposeRuleChange ruleChange) {
 		if (ruleChange instanceof ProposeRuleModification) {
 			if (SequentialID == 0)
 				return VoteType.NO;
 			else
 				return VoteType.YES;
 		}
 		else {
 			Random rand = new Random();
 			if (rand.nextBoolean()) {
 				return VoteType.YES;
 			}
 			else {
 				return VoteType.NO;
 			}
 		}
 	}
 
 	public void setSequentialID(int sequentialID) {
 		SequentialID = sequentialID;
 	}
 }
