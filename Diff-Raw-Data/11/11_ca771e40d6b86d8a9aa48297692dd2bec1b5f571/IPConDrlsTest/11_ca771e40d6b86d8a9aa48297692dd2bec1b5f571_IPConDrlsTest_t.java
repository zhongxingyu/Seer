 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.definition.type.FactType;
 import org.drools.runtime.ObjectFilter;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.FactHandle;
 import org.drools.runtime.rule.QueryResults;
 import org.drools.runtime.rule.QueryResultsRow;
 import org.drools.runtime.rule.Variable;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.AddRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ArrogateLeadership;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPCNV;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConTime;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.JoinAsLearner;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.LeaveCluster;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Prepare1A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.RemRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ResignLeadership;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Response1B;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Revise;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Submit2A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncAck;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncReq;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.TimeStampedAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Vote2B;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.*;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * @author dws04
  *
  */
 public class IPConDrlsTest {
 
 	final private Logger logger = Logger.getLogger(IPConDrlsTest.class);
 
 	Injector injector;
 	RuleStorage rules;
 	StatefulKnowledgeSession session;
 	FactHandle timeHandle;
 	IPConTime time;
 
 	@Before
 	public void setUp() throws Exception {
 		injector = Guice.createInjector(new RuleModule()
 				//.addClasspathDrlFile("test.drl")
 				//.addClasspathDrlFile("IPCon_Institutional_Facts.drl")
 				.addClasspathDrlFile("IPConUtils.drl")
 				.addClasspathDrlFile("IPConPowPer.drl")
 				.addClasspathDrlFile("IPCon.drl")
 				.addClasspathDrlFile("IPConOblSan.drl")
 				);
 		rules = injector.getInstance(RuleStorage.class);
 		session = injector.getInstance(StatefulKnowledgeSession.class);
 		time = new IPConTime(0);
 		initSession();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		session.dispose();
 	}
 	
 	public void initSession() {
 		session.setGlobal("logger", this.logger);
 		session.setGlobal("IPCNV_val", IPCNV.val());
 		session.setGlobal("IPCNV_bal", IPCNV.bal());
 		//session.setGlobal("session", session);
 		//session.setGlobal("storage", null);
 		
 		for (Role role : Role.values()) {
 			session.insert(role);
 		}
 		timeHandle = session.insert(time);
 	}
 	
 	public void outputObjects() {
 		Collection<Object> objects = session.getObjects();
 		logger.info("\nObjects: " + objects.toString() + " are :");
 		for (Object object : objects) {
 			logger.info(object);
 		}
 		logger.info("/objects");
 		analyseDroolsUsage();
 		logger.info("/usage");
 	}
 	
 	private void insertAction(IPConAction action) {
 		((TimeStampedAction)action).setT(time.getTime());
 		session.insert(action);
 	}
 	
 	private void incrementTime() {
 		session.update(timeHandle, time);
 		rules.incrementTime();
 	}
 	
 	private void analyseDroolsUsage() {
 		Map<Class<?>, Integer> typeCards = new HashMap<Class<?>, Integer>();
 		for (Object o : session.getObjects()) {
 				if (!typeCards.containsKey(o.getClass())) {
 					typeCards.put(o.getClass(), 0);
 				}
 				typeCards.put(o.getClass(), typeCards.get(o.getClass()) + 1);
 		}
 		logger.info("Drools memory usage:");
 		for (Map.Entry<Class<?>, Integer> entry : typeCards.entrySet()) {
 			logger.info(entry.getKey().getSimpleName() + " - " + entry.getValue());
 		}
 	}
 	
 	public void initAgent(IPConAgent agent, Role role, Integer revision, String issue, UUID cluster) {
 		Role[] roles  = new Role[]{role};
 		initAgent(agent, roles, revision, issue, cluster);
 	}
 	
 	public void initAgent(IPConAgent agent, Role[] roles, Integer revision, String issue, UUID cluster) {
 		//IPConAgent agent = new IPConAgent();
 		//session.insert(agent);
 		
 		//Set roles
 		//final FactType hasRoleType = typeFromString("HasRole");
 		for (Role role : roles) {
 			/*Object hasRole = hasRoleType.newInstance();
 			hasRoleType.set(hasRole, "role", role);
 			hasRoleType.set(hasRole, "agent", agent);
 			hasRoleType.set(hasRole, "revision", revision);
 			hasRoleType.set(hasRole, "issue", issue);
 			hasRoleType.set(hasRole, "cluster", cluster);*/
 			session.insert(new HasRole(role, agent, revision, issue, cluster));
 		}
 		
 		
 		// Initially didn't vote
 		/*final FactType votedType = typeFromString("Voted");
 		Object v1Vote = votedType.newInstance();
 		votedType.set(v1Vote, "agent", agent);
 		votedType.set(v1Vote, "revision", revision);
 		votedType.set(v1Vote, "ballot", 0);
 		votedType.set(v1Vote, "value", IPCNV.val());
 		votedType.set(v1Vote, "issue", issue);
 		votedType.set(v1Vote, "cluster", cluster);*/
 		session.insert(new Voted(agent, revision, IPCNV.bal(), IPCNV.val(), issue, cluster));
 		
 		// And the reportedvote for the initially
 		/*final FactType reportedVoteType = typeFromString("ReportedVote");
 		Object v1RVote = reportedVoteType.newInstance();
 		reportedVoteType.set(v1RVote, "agent", agent);
 		reportedVoteType.set(v1RVote, "voteRevision", revision);
 		reportedVoteType.set(v1RVote, "voteBallot", 0);
 		reportedVoteType.set(v1RVote, "voteValue", IPCNV.val());
 		reportedVoteType.set(v1RVote, "revision", revision);
 		reportedVoteType.set(v1RVote, "ballot", 0);
 		reportedVoteType.set(v1RVote, "issue", issue);
 		reportedVoteType.set(v1RVote, "cluster", cluster);*/
 		session.insert(new ReportedVote(agent, revision, IPCNV.bal(), IPCNV.val(), revision, 0, issue, cluster));
 	}
 	
 	@Test
 	public void arrogateLeadershipTest() throws Exception {
 		logger.info("\nStarting arrogateLeadershipTest()");
 		IPConAgent agent = new IPConAgent();
 		Integer revision = 0;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		session.insert(agent);
 		//incrementTime();
 		insertAction(new ArrogateLeadership(agent, revision, issue, cluster));
 		incrementTime();
 		
 		outputObjects();
 		
 		//String hasRoleTypeString = "HasRole";
 		/*final FactType hasRoleType = typeFromString("HasRole");
 		
 		Collection<Object> hasRoles = session.getObjects(new ObjectFilter() {
 			@Override
 			public boolean accept(Object object) {
 				return assertFactType(object, hasRoleType);
 			}
 		});
 		assertEquals(1, hasRoles.size());*/
 		Collection<Object> hasRoles = assertFactCount("HasRole", revision, issue, cluster, 1);
 		HasRole hasRole = (HasRole)Arrays.asList(hasRoles.toArray()).get(0);
 		/*Role role = (Role) typeFromString("HasRole").get(hasRole, "role");
 		assertEquals(role.toString(), "LEADER");*/
 		assertEquals(Role.LEADER, hasRole.getRole());
 		
 		/*
 		 * Check that leader can add roles (Invalid, Learner, Acceptor, Proposer)
 		 * Also that AddRole action isnt inserted by the pow/per
 		 */
 		assertActionCount("getPowers", "AddRole", null, null, null, null, 4);
 		assertActionCount("getPermissions", "AddRole", null, null, null, null, 4);
 		assertFactCount("AddRole", null, null, null, 0);
 		
 		logger.info("Finished arrogateLeadershipTest()\n");
 	}
 	
 	@Test
 	/**
 	 * Checks to make sure you can start one of these from scratch without nullvotes
 	 * @throws Exception
 	 */
 	public void fromScratchTest() throws Exception {
 		logger.info("\nStarting fromScratchTest()");
 		IPConAgent a1 = new IPConAgent("a1");
 		IPConAgent a2 = new IPConAgent("a2");
 		Integer revision = 0;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		session.insert(a1);
 		session.insert(a2);
 		insertAction(new ArrogateLeadership(a1, revision, issue, cluster));
 		incrementTime();
 
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		
 		insertAction(new AddRole(a1, a1, Role.PROPOSER, revision, issue, cluster));
 		insertAction(new AddRole(a1, a1, Role.ACCEPTOR, revision, issue, cluster));
 		insertAction(new AddRole(a1, a2, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		insertAction(new Request0A(a1, revision, 5, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		
 		insertAction(new Prepare1A(a1, revision, 1, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPermissions", "Response1B", a1, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Response1B", a2, revision, issue, cluster, 1);
 
 		insertAction(new Response1B(a1, 0, IPCNV.bal(), IPCNV.val(), revision, 0, issue, cluster));
 		insertAction(new Response1B(a2, 0, IPCNV.bal(), IPCNV.val(), revision, 0, issue, cluster));
 		incrementTime();
 		
 		insertAction(new Submit2A(a1, revision, 1, 5, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPermissions", "Vote2B", a1, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Vote2B", a2, revision, issue, cluster, 1);
 
 		insertAction(new Vote2B(a1, revision, 1, 5, issue, cluster));
 		insertAction(new Vote2B(a2, revision, 1, 5, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 
 		IPConAgent a3 = new IPConAgent("a3");
 		session.insert(a3);
 		insertAction(new AddRole(a1, a3, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 1);
 		
 		insertAction(new SyncReq(a1, a3, 5, revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncAck", a3, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "SyncAck", a3, revision, issue, cluster, 2); // yes or no
 		
 		insertAction(new SyncAck(a3, IPCNV.val(), revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncAck", a3, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // only an indication and not taken into account until new agent is in sync
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0); // actually used in code (not a problem because would be 1 vs 1 with qs of 2 and status quo holds)
 		
 		IPConAgent a4 = new IPConAgent("a4");
 		session.insert(a4);
 		insertAction(new AddRole(a1, a4, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 1);
 		assertActionCount("getObligations", "SyncAck", a4, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // only an indication (agent isnt in Sync yet, so not taken into account)
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0); // actually used in code (not a problem until a4 finishes)
 		
 		insertAction(new SyncReq(a1, a4, 5, revision, issue, cluster));
 		incrementTime();
 
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncAck", a4, revision, issue, cluster, 1);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // only an indication (agent is now in Sync, but would only give 2 vs 2 with qs of 3 and status quo holds)
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0); // actually used in code (not a problem until a4 finishes)
 		
 		insertAction(new SyncAck(a4, IPCNV.val(), revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncAck", a4, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // only an indication (no agents in sync - we have 2 vs 2 with qs of 3, and status quo holds)
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1); // actually used in code (would be 1 vs 2 with qs of 2)
 		
 		IPConAgent a5 = new IPConAgent("a5");
 		session.insert(a5);
 		insertAction(new AddRole(a1, a5, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 1);
 		assertActionCount("getObligations", "SyncAck", a5, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // only an indication (agent isnt in Sync yet, so not taken into account)
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1); // actually used in code (not done until a5 finishes)
 		
 		insertAction(new SyncReq(a1, a5, 5, revision, issue, cluster));
 		incrementTime();
 
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncAck", a5, revision, issue, cluster, 1);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 1); // only an indication (agent is now in Sync, would give 2 vs 3 with qs of 3)
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1); // actually used in code (not done until a5 finishes)
 		
 		insertAction(new SyncAck(a5, IPCNV.val(), revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 1);
 		assertActionCount("getObligations", "SyncReq", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "SyncAck", a5, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 1); // only an indication (no agents in sync but event doesn't expire as it is in use - we have 2 vs 3 with qs of 3 so obligation to revise exists)
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1); // actually used in code (would be 1 vs 3 with qs of 3 - still 1 from before and doesn't doubleup)
 
 		logger.info("Finished fromScratchTest()\n");
 	}
 	
 	/**
 	 * Checks to see if RemRole fact sticks around...
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void remRoleTest() throws Exception {
 		logger.info("\nStarting remRoleTest()");
 		
 		Integer revision = 1;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 	
 		
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1); initAgent(a1, new Role[]{Role.LEADER, Role.PROPOSER, Role.ACCEPTOR}, revision, issue, cluster);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2); initAgent(a2, new Role[]{Role.ACCEPTOR}, revision, issue, cluster);
 	
 		incrementTime();
 
 		assertFactCount("HasRole", revision, issue, cluster, 4);
 		
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 4);
 		
 		insertAction(new RemRole(a1, a2, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 3);
 		
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 3);
 		
 		insertAction(new AddRole(a1, a2, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 4);
 	}
 	
 	@Test
 	public void leaveClusterTest() throws Exception {
 		logger.info("\nStarting leaveClusterTest()");
 		
 	
 		Integer rev0 = 0;
 		Integer rev1 = 1;
 		String iss1 = "Issue1";
 		String iss2 = "Issue2";
 		UUID clus1 = Random.randomUUID();
 		UUID clus2 = Random.randomUUID();
 		
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1);
 		initAgent(a1, new Role[]{Role.LEARNER}, rev0, iss1, clus1);
 		initAgent(a1, new Role[]{Role.LEARNER}, rev1, iss1, clus1);
 		initAgent(a1, new Role[]{Role.LEARNER}, rev0, iss2, clus1);
 		session.insert(new IPConRIC(rev0, iss1, clus1));
 		session.insert(new IPConRIC(rev1, iss1, clus1));
 		session.insert(new IPConRIC(rev0, iss2, clus1));
 		session.insert(new IPConRIC(rev0, iss1, clus2));
 		session.insert(new IPConRIC(rev0, iss2, clus2));
 		
 		incrementTime();
 		outputObjects();
 		
 		//initially assert
 		assertFactCount("HasRole", null, null, null, 3);
 		assertFactCount("AgentLeft", null, null, null, 0);
 		assertFactCount("Sync", null, null, null, 0);
 		assertFactCount("NeedToSync", null, null, null, 0);
 		assertQuorumSize(rev0, iss1, clus1, 0);
 		assertQuorumSize(rev1, iss1, clus1, 0);
 		assertQuorumSize(rev0, iss2, clus1, 0);
 		assertFactCount("Pre_Vote", null, null, null, 0);
 		assertFactCount("Open_Vote", null, null, null, 0);
 		assertFactCount("Chosen", null, null, null, 0);
 		assertFactCount("Voted", null, null, null, 3); // auto-inserted...
 		assertFactCount("ReportedVote", null, null, null, 3); // auto-inserted...
 		assertActionCount("getObligations", null, a1, null, null, null, 0);
 		assertFactCount("PossibleAddRevision", null, null, null, 0);
 		assertFactCount("PossibleRemRevision", null, null, null, 0);
 		
 		assertActionCount("getObligations", null, null, null, null, null, 0);
 		
 		assertActionCount("getPowers", null, a1, null, null, null, 4); // leave*1 + arrogate*1 + join*2
 		assertActionCount("getPermissions", null, a1, null, null, null, 4); // leave*1 + arrogate*1 + join*2
 		assertActionCount("getPowers", "ArrogateLeadership", a1, null, null, null, 1);
 		assertActionCount("getPowers", "LeaveCluster", a1, null, null, null, 1);
 		assertActionCount("getPowers", "JoinAsLearner", a1, null, null, null, 2);
 		assertActionCount("getPermissions", "ArrogateLeadership", a1, null, null, null, 1);
 		assertActionCount("getPermissions", "LeaveCluster", a1, null, null, null, 1);
 		assertActionCount("getPermissions", "JoinAsLearner", a1, null, null, null, 2);
 		
 		/*
 		 * Time 1: 
 		 * A1 leaves and joins clus2 simultaneously
 		 */
 		insertAction(new LeaveCluster(a1, clus1));
 		insertAction(new JoinAsLearner(a1, rev0, iss1, clus2));
 		insertAction(new JoinAsLearner(a1, rev0, iss2, clus2));
 		incrementTime();
 		
 		//outputObjects();
 		
 		//assert
 		assertFactCount("HasRole", null, null, null, 2);
 		assertFactCount("AgentLeft", null, null, null, 0); // we want it to expire instantly
 		assertFactCount("Sync", null, null, null, 0);
 		assertFactCount("NeedToSync", null, null, null, 0);
 		assertQuorumSize(rev0, iss1, clus1, 0);
 		assertQuorumSize(rev1, iss1, clus1, 0);
 		assertQuorumSize(rev0, iss2, clus1, 0);
 		assertFactCount("Pre_Vote", null, null, null, 0);
 		assertFactCount("Open_Vote", null, null, null, 0);
 		assertFactCount("Chosen", null, null, null, 0);
 		assertFactCount("Voted", null, null, null, 3); // auto-inserted...
 		assertFactCount("ReportedVote", null, null, null, 0); // removed by leavecluster
 		assertActionCount("getObligations", null, a1, null, null, null, 0);
 		assertFactCount("PossibleAddRevision", null, null, null, 0);
 		assertFactCount("PossibleRemRevision", null, null, null, 0);
 		
 		assertActionCount("getObligations", null, null, null, null, null, 0);
 		
 
 		assertActionCount("getPowers", "ArrogateLeadership", a1, null, null, null, 1);
 		assertActionCount("getPowers", "LeaveCluster", a1, null, null, null, 1);
 		assertActionCount("getPowers", "JoinAsLearner", a1, null, null, null, 2);
 		assertActionCount("getPermissions", "ArrogateLeadership", a1, null, null, null, 1);
 		assertActionCount("getPermissions", "LeaveCluster", a1, null, null, null, 1);
 		assertActionCount("getPermissions", "JoinAsLearner", a1, null, null, null, 2);
 		assertActionCount("getPowers", null, a1, null, null, null, 4); // leave*1 + arrogate*1 + join*2
 		assertActionCount("getPermissions", null, a1, null, null, null, 4); // leave*1 + arrogate*1 + join*2
 		
 		logger.info("Finished leaveClusterTest()\n");
 	}
 	
 	@Test
 	public void resignTest() throws Exception {
 		logger.info("\nStarting resignTest()");
 		
 		Integer revision = 1;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		Integer ballot = 5;
 		Object v1 = "dfghjk";
 		Object v2 = "v2";
 		
 		/*
 		 * Initially
 		 */
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1); initAgent(a1, new Role[]{Role.LEADER, Role.PROPOSER, Role.ACCEPTOR}, revision, issue, cluster);
 		session.insert(new Voted(a1, revision, ballot-1, v1, issue, cluster));
 		session.insert(new Proposed(revision, v2, issue, cluster));
 		
 		
 		incrementTime();
 	
 		
 		//initially assert
 		assertFactCount("HasRole", revision, issue, cluster, 3);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 1);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 0);
 		assertFactCount("Open_Vote", revision, issue, cluster, 0);
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		assertFactCount("Voted", revision, issue, cluster, 2);
 		assertFactCount("ReportedVote", revision, issue, cluster, 1);
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 1); // obligated to prepare
 		assertActionCount("getObligations", "Response1B", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "Prepare1A", a1, revision, issue, cluster, 1);
 		
 		assertActionCount("getPowers", "ResignLeadership", a1, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "Request0A", a1, revision, issue, cluster, 1);
 		
 		/*
 		 * Time step 1
 		 * A1 resigns
 		 * Obligation & leader powers removed 
 		 */
 		insertAction(new ResignLeadership(a1, revision, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 2);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 1);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 0);
 		assertFactCount("Open_Vote", revision, issue, cluster, 0);
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		assertFactCount("Voted", revision, issue, cluster, 2);
 		assertFactCount("ReportedVote", revision, issue, cluster, 1);
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 0); // obligated to report the vote, and to prepare
 		assertActionCount("getObligations", "Response1B", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "Prepare1A", a1, revision, issue, cluster, 0);
 		
 		assertActionCount("getPowers", "ResignLeadership", a1, revision, issue, cluster, 0);
 		assertActionCount("getPowers", "Request0A", a1, revision, issue, cluster, 1);
 		
 		/*
 		 * Check it lasts
 		 */
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 2);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 1);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 0);
 		assertFactCount("Open_Vote", revision, issue, cluster, 0);
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		assertFactCount("Voted", revision, issue, cluster, 2);
 		assertFactCount("ReportedVote", revision, issue, cluster, 1);
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 0); // obligated to report the vote, and to prepare
 		assertActionCount("getObligations", "Response1B", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "Prepare1A", a1, revision, issue, cluster, 0);
 		
 		assertActionCount("getPowers", "ResignLeadership", a1, revision, issue, cluster, 0);
 		assertActionCount("getPowers", "Request0A", a1, revision, issue, cluster, 1);
 		
 		/*
 		 * Insert arrogate 
 		 */
 		insertAction(new ArrogateLeadership(a1, revision, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 3);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 1);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 0);
 		assertFactCount("Open_Vote", revision, issue, cluster, 0);
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		assertFactCount("Voted", revision, issue, cluster, 2);
 		assertFactCount("ReportedVote", revision, issue, cluster, 1);
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 1); // obligated to prepare
 		assertActionCount("getObligations", "Response1B", a1, revision, issue, cluster, 0);
 		assertActionCount("getObligations", "Prepare1A", a1, revision, issue, cluster, 1);
 		
 		assertActionCount("getPowers", "ResignLeadership", a1, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "Request0A", a1, revision, issue, cluster, 1);
 		
 	}
 	
 	@Test
 	public void requestTest() throws Exception {
 		logger.info("\nStarting requestTest()");
 		
 		Integer revision = 0;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2);
 		Role[] a1Roles = new Role[]{Role.LEADER, Role.ACCEPTOR};
 		Role[] a2Roles = new Role[]{Role.PROPOSER, Role.ACCEPTOR};
 		initAgent(a1, a1Roles, revision, issue, cluster);
 		initAgent(a2, a2Roles, revision, issue, cluster);
 		
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 4);
 		assertFactCount("Proposed", revision, issue, cluster, 0);
 		
 		insertAction(new Request0A( a2, revision, "VALUE", issue, cluster ));
 		
 		incrementTime();
 		
 		assertFactCount("Proposed", revision, issue, cluster, 1);
 		
 		logger.info("Finished requestTest()\n");
 	}
 	
 	/**
 	 * Test to check the power, permission, and obligations around Response1B action
 	 * 
 	 * Pre: Acceptor will be inserted with a previous vote and a pre_vote will be asserted
 	 * Test: Check the agent has power to respond, permission to respond correctly, no permission to respond incorrectly 
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void checkResponsePowPerObl() throws Exception {
 		logger.info("\nStarting checkResponsePowPerObl()");
 		IPConAgent a1 = new IPConAgent("a1");
 		Integer revision = 1;
 		Integer ballot = 10;
 		Object v1 = "1";
 		Object v2 = "v2";
 		Object v3 = 3;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		session.insert(a1);
 		// Insert intially facts:
 		// Agent is acceptor
 		session.insert(new HasRole(Role.ACCEPTOR, a1, revision, issue, cluster));
 		// PreVote exists
 		Pre_Vote preVote = new Pre_Vote(revision, ballot, issue, cluster);
 		session.insert(preVote);
 		// Agent voted in 3 previous ballots
 		session.insert(new Voted(a1, revision, ballot-5, v1, issue, cluster));
 		session.insert(new Voted(a1, revision, ballot-3, v2, issue, cluster));
 		session.insert(new Voted(a1, revision, ballot-2, v3, issue, cluster));
 		
 		incrementTime();
 		
 		/*
 		 * Make sure initially facts hold
 		 */
 		// agent is inserted
 		assertFactCount("IPConAgent", null, null, null, 1);
 		assertFactCount("HasRole", revision, issue, cluster, 1);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 3);
 		
 		assertActionCount("getPowers", "Response1B", null, null, null, null, 1);
 		assertActionCount("getPermissions", "Response1B", null, null, null, null, 1);
 		assertActionCount("getObligations", "Response1B", null, null, null, null, 1);
 		
 		// make sure agent isn't permitted to respond with anything other than highest vote
 		Collection<IPConAction> perSet = getActionQueryResultsForRIC("getPermissions", "Response1B", null, null, null, null);
 		// check agent is obligated to respond
 		Collection<IPConAction> oblSet = getActionQueryResultsForRIC("getObligations", "Response1B", null, null, null, null);
 		
 		//dbl check only one each, and they're the same
 		assertEquals(perSet, oblSet);
 		assertEquals(1, perSet.size());
 		
 		//get the permitted response
 		Response1B response = (Response1B) (perSet.toArray())[0];
 		//check it's correct
 		assertEquals(a1, response.getAgent());
 		assertEquals(revision, response.getRevision());
 		assertEquals(issue, response.getIssue());
 		assertEquals(ballot, response.getBallot());
 		assertEquals(cluster, response.getCluster());
 		assertEquals(v3, response.getVoteValue());
 		assertEquals((Integer)(ballot-2), response.getVoteBallot());
 		assertEquals(revision, response.getVoteRevision());
 		
 		logger.info("Finished checkResponsePowPerObl()\n");
 		
 	}
 	
 	@Test
 	public void narrative1Consensus() throws Exception {
 		narrative1(true);
 	}
 	
 	@Test
 	public void narrative1NoConsensus() throws Exception {
 		narrative1(false);
 	}
 	
 	
 	public void narrative1(Boolean pass) throws Exception {
 		if (pass) {
 			logger.info("\nStarting narrative1 with successful consensus");
 		} else {
 			logger.info("\nStarting narrative1 intending a sanction against leader");
 		}
 		// create agents
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2);
 		IPConAgent a3 = new IPConAgent("a3"); session.insert(a3);
 		IPConAgent a4 = new IPConAgent("a4"); session.insert(a4);
 		IPConAgent a5 = new IPConAgent("a5"); session.insert(a5);
 		//specify revision/issue/cluster
 		Integer revision = 1;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		//set initially roles
 		insertAction(new ArrogateLeadership(a1, revision, issue, cluster));
 		insertAction(new AddRole(a1, a1, Role.PROPOSER, revision, issue, cluster));
 		insertAction(new AddRole(a1, a1, Role.ACCEPTOR, revision, issue, cluster));
 		insertAction(new AddRole(a1, a2, Role.ACCEPTOR, revision, issue, cluster));
 		insertAction(new AddRole(a1, a3, Role.ACCEPTOR, revision, issue, cluster));
 		insertAction(new AddRole(a1, a4, Role.ACCEPTOR, revision, issue, cluster));
 		insertAction(new AddRole(a1, a5, Role.ACCEPTOR, revision, issue, cluster));
 		//set initially voted
 		/*final FactType votedType = typeFromString("Voted");
 		Object a2Vote = votedType.newInstance();
 		votedType.set(a2Vote, "agent", a2);
 		votedType.set(a2Vote, "revision", revision);
 		votedType.set(a2Vote, "ballot", 1);
 		votedType.set(a2Vote, "value", 4);
 		votedType.set(a2Vote, "issue", issue);
 		votedType.set(a2Vote, "cluster", cluster);*/
 		session.insert(new Voted(a2, revision, 1, 4, issue, cluster));
 		/*Object a3Vote = votedType.newInstance();
 		votedType.set(a3Vote, "agent", a3);
 		votedType.set(a3Vote, "revision", revision);
 		votedType.set(a3Vote, "ballot", 1);
 		votedType.set(a3Vote, "value", 4);
 		votedType.set(a3Vote, "issue", issue);
 		votedType.set(a3Vote, "cluster", cluster);*/
 		session.insert(new Voted(a3, revision, 1, 4, issue, cluster));
 		if (!pass) {
 			/*Object a4Vote = votedType.newInstance();
 			votedType.set(a4Vote, "agent", a4);
 			votedType.set(a4Vote, "revision", revision);
 			votedType.set(a4Vote, "ballot", 2);
 			votedType.set(a4Vote, "value", 5);
 			votedType.set(a4Vote, "issue", issue);
 			votedType.set(a4Vote, "cluster", cluster);*/
 			session.insert(new Voted(a4, revision, 2, 5, issue, cluster));
 		}
 		incrementTime();
 		
 		// check there are the right number of roles
 		// 5 acceptors, one leader, one proposer
 		assertFactCount("IPConAgent", null, null, null, 5);
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		
 		// check theres only one agent can request (the proposer)
 		// one proposer has permission
 		//assertFactCount("Request", 1);
 		assertActionCount("getPowers", "Request0A", null, revision, issue, cluster, 1);
 		
 		
 		/*
 		 *  check some arbitrary fact counts
 		 */
 		// everyone can arrogate (leader could arrogate something else for example)
 		//assertFactCount("Arrogate", 5);
 		assertActionCount("getPowers", "ArrogateLeadership", null, revision, issue, cluster, 5);
 		// only leader can resign
 		//assertFactCount("Resign", 1);
 		assertActionCount("getPowers", "ResignLeadership", null, revision, issue, cluster, 1);
 		// all can leave
 		// TODO need to fix this holdsAt so each role doesn't count
 		// inadvertantly fixed this by putting into a HashSet to remove dups :P
 		//assertFactCount("Leave", 7);
 		//outputObjects();
 		// FIXME TODO work out why this sometimes fails ! (gives 4 instead of 5)
 		assertActionCount("getPowers", "LeaveCluster",null, revision, issue, cluster, 5);
 		
 		/*
 		 * 5 roles in total
 		 * leader can add roles:
 		 * 2 for himself
 		 * (4 for others)*4 = 16
 		 * Total: 18
 		 */
 		assertActionCount("getPowers", "AddRole", null, revision, issue, cluster, 18);
 		assertActionCount("getPermissions", "AddRole", null, revision, issue, cluster, 18);
 		/*
 		 * 5 roles in total
 		 * leader can rem roles:
 		 * 3 for himself
 		 * (1 for others)*4 = 4
 		 * Total: 7
 		 */
 		assertActionCount("getPowers", "RemRole", null, revision, issue, cluster, 7);
 		assertActionCount("getPermissions", "RemRole", null, revision, issue, cluster, 7);
 		// leader can revise
 		assertActionCount("getPowers", "Revise", null, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Revise", null, revision, issue, cluster, 1);
 		// leader has pow to syncreq all acceptors (incl himself)
 		assertActionCount("getPowers", "SyncReq", null, revision, issue, cluster, 5);
 		// but not the permission
 		assertActionCount("getPermissions", "SyncReq", null, revision, issue, cluster, 0);
 		//no one can syncack
 		assertActionCount("getPowers", "SyncAck", null, revision, issue, cluster, 0);
 		assertActionCount("getPermissions", "SyncAck", null, revision, issue, cluster, 0);
 		
 		/*
 		 * Begin the time steps.
 		 */
 		
 		/*
 		 * Time step 1 :
 		 * leader/proposer requests 3, check no one can respond yet
 		 */
 		insertAction(new Request0A(a1, revision, 3, issue, cluster));
 		incrementTime();
 		
 		// check no one can response
 		assertActionCount("getPermissions", "Response1B", null, revision, issue, cluster, 0);
 		
 		
 		/*
 		 * Time step 2 :
 		 * leader issues prepare, check 5 acceptors can respond
 		 */
 		insertAction(new Prepare1A(a1, revision, 10, issue, cluster));
 		incrementTime();
 		
 		// check all acceptors can response
 		// 5 acceptors have permission
 		assertActionCount("getPermissions", "Response1B", null, revision, issue, cluster, 5);
 		if (pass) {
 			// 2 acceptors have to respond
 			assertActionCount("getObligations", "Response1B", null, revision, issue, cluster, 2);
 		}else {
 			// 3 have to
 			assertActionCount("getObligations", "Response1B", null, revision, issue, cluster, 3);
 		}
 		
 		
 		/*
 		 * Time step 3 :
 		 * All agents send a response, check the responsecount and per/obl to submit
 		 */
 		insertAction(new Response1B( a1, 1, IPCNV.bal(), IPCNV.val(), 1, 10, issue, cluster));
 		insertAction(new Response1B( a2, 1, 1, 4, 1, 10, issue, cluster));
 		insertAction(new Response1B( a3, 1, 1, 4, 1, 10, issue, cluster));
 		if (pass) {
 			insertAction(new Response1B( a4, 1, IPCNV.bal(), IPCNV.val(), 1, 10, issue, cluster));
 		} else {
 			insertAction(new Response1B( a4, 1, 2, 5, 1, 10, issue, cluster));
 		}
 		insertAction(new Response1B( a5, 1, IPCNV.bal(), IPCNV.val(), 1, 10, issue, cluster));
 		incrementTime();
 		if (pass) {
 			assertActionCount("getPowers", "Submit2A", null, revision, issue, cluster, 1);
 			assertActionCount("getPermissions", "Submit2A", null, revision, issue, cluster, 1);
 			assertActionCount("getObligations", "Submit2A", null, revision, issue, cluster, 1);
 		} else {
 			assertActionCount("getPowers", "Submit2A", null, revision, issue, cluster, 1);
 			assertActionCount("getPermissions", "Submit2A", null, revision, issue, cluster, 0);
 			assertActionCount("getObligations", "Submit2A", null, revision, issue, cluster, 0);
 		}
 		
 		/*
 		 * Time step 3
 		 * Leader submits. Check permission to vote for all agents.
 		 */
 		insertAction( new Submit2A(a1, revision, 10, 3, issue, cluster));
 		incrementTime();
 
 		assertFactCount("Open_Vote", revision, issue, cluster, 1);
 		// all acceptors can vote either way because leader had power
 		assertActionCount("getPermissions", "Vote2B", null, revision, issue, cluster, 5);
 		
 		
 		/*
 		 * Time step 4
 		 * First 2 agents vote correctly, 3rd agent does not.
 		 * Check value is not chosen, and voted is not set for 3rd agent
 		 */
 		insertAction(new Vote2B(a1, 1, 10, 3, issue, cluster));
 		insertAction(new Vote2B(a2, 1, 10, 3, issue, cluster));
 		insertAction(new Vote2B(a3, 1, 10, 4, issue, cluster));
 		incrementTime();
 		
 		if (pass) {
 			//voted count is 4 (2 from before, 2 now)
 			assertFactCount("Voted", revision, issue, cluster, 4);
 		} else {
 			// count ag4 for voted, but not reportedVote (since they had a nullvote before)
 			assertFactCount("Voted", revision, issue, cluster, 5);
 		}
 		//reportedVote count is 7 (5 from before due to nullvotes, 2 from now)
 		assertFactCount("ReportedVote", revision, issue, cluster, 7);
 		// value was not chosen
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		
 		
 		/*
 		 * Time step 5
 		 * Agent 3 votes correctly, 3 agents have now voted, so chosen
 		 * Check correct value has been chosen
 		 */
 		insertAction( new Vote2B(a3, 1, 10, 3, issue, cluster));
 		incrementTime();
 		// value was chosen
 		Collection<Object> chosens = assertFactCount("Chosen", revision, issue, cluster, 1);
 		Chosen chosen = (Chosen)Arrays.asList(chosens.toArray()).get(0);
 		//Object value = typeFromString("Chosen").get(chosen, "value");
 		// correct value was chosen
 		//assertEquals(value, 3);
 		assertEquals(3, chosen.getValue());
 		
 		
 		/*
 		 * Time step 6
 		 * Remaining 2 agents vote correctly
 		 * Check vote counts and correct chosen
 		 */
 		insertAction( new Vote2B(a4, 1, 10, 3, issue, cluster));
 		insertAction( new Vote2B(a5, 1, 10, 3, issue, cluster));
 		incrementTime();
 		
 		if (pass) {
 			//voted count is 7 (4 from before, 3 now)
 			assertFactCount("Voted", revision, issue, cluster, 7);
 		} else {
 			// count ag4's additional previous vote
 			assertFactCount("Voted", revision, issue, cluster, 8);
 		}
 		//reportedVote count is 10 (7 from before, 3 from now)
 		assertFactCount("ReportedVote", revision, issue, cluster, 10);
 		// value was chosen
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		
 		// no risk from adding or removing agents
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		
 		outputObjects();
 		if (pass) {
 			logger.info("Finished narrative1 with successful consensus\n");
 		}
 		else {
 			logger.info("Finished narrative1 with sanction against leader\n");
 		}
 	}
 	
 	public void testPossibleRevisionDetectionSetup(HashMap<String,Object> map) throws Exception {
 		logger.info("\nStarting test to check for detection of possible need for revision");
 		
 		//specify revision/issue/cluster
 		Integer revision = 1;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		// create agents
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2);
 		IPConAgent a3 = new IPConAgent("a3"); session.insert(a3);
 		Role[] leaderRoles = new Role[]{Role.LEADER, Role.PROPOSER, Role.ACCEPTOR};
 		initAgent(a1, leaderRoles, revision, issue, cluster);
 		initAgent(a2, Role.ACCEPTOR, revision, issue, cluster);
 		initAgent(a3, Role.ACCEPTOR, revision, issue, cluster);
 		map.put("issue", issue);
 		map.put("revision", revision);
 		map.put("cluster", cluster);
 		map.put("a1", a1);
 		map.put("a2", a2);
 		map.put("a3", a3);
 		
 		/**/
 		//set initially roles
 		/*session.insert(new ArrogateLeadership(a1, revision, issue, cluster));
 		session.insert(new AddRole(a1, a1, Role.PROPOSER, revision, issue, cluster));
 		session.insert(new AddRole(a1, a1, Role.ACCEPTOR, revision, issue, cluster));
 		session.insert(new AddRole(a1, a2, Role.ACCEPTOR, revision, issue, cluster));
 		session.insert(new AddRole(a1, a3, Role.ACCEPTOR, revision, issue, cluster));*/
 		
 		// Increment now to stop all agents requiring Sync (because theyre being added at the same time)
 		incrementTime();
 		
 		/*
 		 * Set Time step 1 (initially)
 		 */
 		// Agent voted in previous ballot
 		/*final FactType votedType = typeFromString("Voted");
 		Object v1Vote = votedType.newInstance();
 		votedType.set(v1Vote, "agent", a1);
 		votedType.set(v1Vote, "revision", revision);
 		votedType.set(v1Vote, "ballot", 1);
 		votedType.set(v1Vote, "value", "A");
 		votedType.set(v1Vote, "issue", issue);
 		votedType.set(v1Vote, "cluster", cluster);*/
 		session.insert(new Voted(a1, revision, 1, "A", issue, cluster));
 		/*Object v2Vote = votedType.newInstance();
 		votedType.set(v2Vote, "agent", a2);
 		votedType.set(v2Vote, "revision", revision);
 		votedType.set(v2Vote, "ballot", 1);
 		votedType.set(v2Vote, "value", "A");
 		votedType.set(v2Vote, "issue", issue);
 		votedType.set(v2Vote, "cluster", cluster);*/
 		session.insert(new Voted(a2, revision, 1, "A", issue, cluster));
 		
 		// Agent voted in previous ballot
 		/*final FactType reportedVoteType = typeFromString("ReportedVote");
 		Object v1RVote = reportedVoteType.newInstance();
 		reportedVoteType.set(v1RVote, "agent", a1);
 		reportedVoteType.set(v1RVote, "voteRevision", revision);
 		reportedVoteType.set(v1RVote, "voteBallot", 1);
 		reportedVoteType.set(v1RVote, "voteValue", "A");
 		reportedVoteType.set(v1RVote, "revision", revision);
 		reportedVoteType.set(v1RVote, "ballot", 1);
 		reportedVoteType.set(v1RVote, "issue", issue);
 		reportedVoteType.set(v1RVote, "cluster", cluster);*/
 		session.insert(new ReportedVote(a1, revision, 1, "A", revision, 1, issue, cluster));
 		/*Object v2RVote = reportedVoteType.newInstance();
 		reportedVoteType.set(v2RVote, "agent", a2);
 		reportedVoteType.set(v2RVote, "voteRevision", revision);
 		reportedVoteType.set(v2RVote, "voteBallot", 1);
 		reportedVoteType.set(v2RVote, "voteValue", "A");
 		reportedVoteType.set(v2RVote, "revision", revision);
 		reportedVoteType.set(v2RVote, "ballot", 1);
 		reportedVoteType.set(v2RVote, "issue", issue);
 		reportedVoteType.set(v2RVote, "cluster", cluster);*/
 		session.insert(new ReportedVote(a2, revision, 1, "A", revision, 1, issue, cluster));
 		
 		//insert Open_Vote to allow Chosen to kick in
 		/*final FactType openVoteType = typeFromString("Open_Vote");
 		Object openVote = openVoteType.newInstance();
 		openVoteType.set(openVote, "revision", revision);
 		openVoteType.set(openVote, "ballot", 1);
 		openVoteType.set(openVote, "value", "A");
 		openVoteType.set(openVote, "issue", issue);
 		openVoteType.set(openVote, "cluster", cluster);*/
 		session.insert(new Open_Vote(revision, 1, "A", issue, cluster));
 		
 		incrementTime();
 		
 		assertFactCount("IPConAgent", null, null, null, 3);
 		// Remember the initial "didnt vote" facts
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 5);
 		assertFactCount("HasRole", revision, issue, cluster, 5);
 		assertQuorumSize(revision, issue, cluster, 2);
 		
 		outputObjects();
 		
 		// value was chosen
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		
 		// no risk from adding agents
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		// no risk from removing (status quo holds)
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 
 		/*
 		 * Time step 2
 		 * Insert new agent
 		 * Check that there is an obligation to syncreq
 		 */
 		IPConAgent a4 = new IPConAgent("a4"); session.insert(a4); map.put("a4", a4);
 		insertAction(new AddRole(a1, a4, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		// check obligation to sync the new agent
 		Collection<IPConAction> oblColl = getActionQueryResultsForRIC("getObligations", "SyncReq", null, revision, issue, cluster);
 		assertEquals(1, oblColl.size());
 		Object action = Arrays.asList(oblColl.toArray()).get(0);
 		SyncReq fact = null;
 		if (action instanceof SyncReq ) {
 			fact = (SyncReq)action;
 		}
 		else {
 			fail();
 		}
 		assertEquals(fact.getAgent(), a4);
 
 		assertFactCount("HasRole", revision, issue, cluster, 6);
 		outputObjects();
 		assertFactCount("SyncReq", revision, issue, cluster, 0);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 1);
 		
 		//outputObjects();
 		
 		assertQuorumSize(revision, issue, cluster, 2);
 		
 		// value was chosen
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		
 		// no risk from adding or removing agent because status quo holds
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		/*
 		 * Time step 3
 		 * Sync the new agent, check new risks
 		 */
 		insertAction(new SyncReq( a1, a4, "A", revision, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 6);
 		assertFactCount("Sync", revision, issue, cluster, 1);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		
 		// quorumsize should still be 2 because the agent didn't complete the sync
 		assertQuorumSize(revision, issue, cluster, 2);
 		
 		// value was chosen
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		
 		// still no risk because status quo holds
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		/*
 		 * Time step 4
 		 * Agent syncs no
 		 */
 		insertAction(new SyncAck( a4, IPCNV.val(), revision, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getObligations", "SyncAck", null, revision, issue, cluster, 0);
 		
 		assertFactCount("HasRole", revision, issue, cluster, 6);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		
 		// quorumsize should be 3 because the agent completed the sync
 		assertQuorumSize(revision, issue, cluster, 3);
 		
 		// 5 votes but 6 reportedVotes because syncAck no doesn't count as a vote
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 6);
 		
 		// value chosen
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		
 		
 		// still no risk because status quo holds
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 		
 		/*
 		 * Time step 5
 		 * Ag5 joins
 		 */
 		
 		IPConAgent a5 = new IPConAgent("a5"); session.insert(a5); map.put("a5", a5);
 		insertAction(new AddRole(a1, a5, Role.ACCEPTOR, revision, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 1);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // they're not synching yet
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 6);
 		
 		/*
 		 * Time step 6
 		 * Ag5 starts to sync
 		 */
 		
 		insertAction(new SyncReq( a1, a5, "A", revision, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		assertFactCount("Sync", revision, issue, cluster, 1);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 6);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 1); // if ag5 says no, safety is violated, if they say yes then PRR goes away
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1); // if ag1 or 2 leave, safety is violated
 		assertActionCount("getObligations", "Revise", null, revision, issue, cluster, 0);
 		
 		logger.info("Finished checking Possible Revision detection... now checking for outcomes...\n");
 		
 	}
 	
 	@Test
 	public void testSyncAckRevise() throws Exception {
 		logger.info("\nTrying a SyncAck no and testing correct functioning after that...");
 		
 		HashMap<String, Object> map = new HashMap<String,Object>();
 		testPossibleRevisionDetectionSetup(map);
 		
 		//get data out of map
 		IPConAgent a1 = (IPConAgent) map.get("a1");
 		IPConAgent a2 = (IPConAgent) map.get("a2");
 		IPConAgent a3 = (IPConAgent) map.get("a3");
 		IPConAgent a4 = (IPConAgent) map.get("a4");
 		IPConAgent a5 = (IPConAgent) map.get("a5");
 		Integer revision = (Integer) map.get("revision");
 		String issue = (String) map.get("issue");
 		UUID cluster = (UUID) map.get("cluster");
 		
 		
 		// Check things are right after Time step 6
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		assertFactCount("Sync", revision, issue, cluster, 1);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 6);
 		assertActionCount("getObligations", "Revise", null, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 1); // if ag5 says no, safety is violated, if they say yes then PRR goes away
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 		
 		
 		/*
 		 * Time step 7
 		 * Ag5 says no
 		 * Safety should be violated
 		 */
 		insertAction(new SyncAck(a5, IPCNV.val(), revision, issue, cluster));
 		incrementTime();
 		assertFactCount("HasRole", null, null, null, 7);
 		assertFactCount("Sync", null, null, null, 0);
 		assertFactCount("NeedToSync", null, null, null, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Chosen", null, null, null, 1);
 		assertFactCount("Voted", null, null, null, 5); // null votes don't get created because it doesnt make any sense
 		assertFactCount("ReportedVote", null, null, null, 7); // add a null reportedvote
 		
 		outputObjects();
 		
 		// Check safety was violated and an obligation to revise exists
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 1);
 		assertFactCount("PossibleAddRevision", null, null, null, 1);
 		assertFactCount("PossibleRemRevision", null, null, null, 1);
 		
 		
 		/*
 		 * Time step 8
 		 * Leader revises
 		 * Check for clean slate
 		 */
 		insertAction(new Revise(a1, revision, issue, cluster));
 		incrementTime();
 		//logger.debug(getActionQueryResultsForRIC("getPowers", null, null, revision+1, issue, cluster));
 		
 		outputObjects();
 		
 		// Check nothing changed in the old revision
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 7);
 		// except this because obligation was discharged - work out why this sometimes fails
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 		
 		Integer newRevision = revision+1;
 		// check new revision
 		assertFactCount("HasRole", newRevision, issue, cluster, 7);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 3);
 		assertFactCount("Chosen", newRevision, issue, cluster, 0);
 		assertFactCount("Voted", newRevision, issue, cluster, 0);
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 0);
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 		// FactCount would work too, but would drop into objectcount anyway...
 		assertFactCount("IPConAgent", null, null, null, 5);
 		assertActionCount("getPowers", "LeaveCluster", a4, null, null, null, 1); 
 		
 		/*
 		 * Time step 9
 		 * Start the whole thing again to make sure it works (cut out a4 and 5)
 		 * A4 leaves cluster, A5 removed.
 		 */
 		insertAction(new LeaveCluster(a4, cluster));
 		insertAction(new RemRole(a1, a5, Role.ACCEPTOR, revision, issue, cluster));
 		insertAction(new RemRole(a1, a5, Role.ACCEPTOR, newRevision, issue, cluster));
 		incrementTime();
 		
 		outputObjects();
 		
 		assertFactCount("Revise", null, null, null, 0); // fact isn't in the kbase anymore
 		assertFactCount("IPConAgent", null, null, null, 5); // the facts are still there
 		
 		// Check  old revision
 		assertFactCount("HasRole", revision, issue, cluster, 5);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 2);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 6); // a4 left
 		// FIXME TODO not sure why this didn't fire
 		//assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 1); // obligation to revise because agents removed
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // agent no longer synching
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0); // 0 because now the agents are gone
 				
 		// check new revision
 		assertFactCount("HasRole", newRevision, issue, cluster, 5);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", newRevision, issue, cluster, 0);
 		assertFactCount("Open_Vote", newRevision, issue, cluster, 0);
 		assertFactCount("Chosen", newRevision, issue, cluster, 0);
 		assertFactCount("Voted", newRevision, issue, cluster, 0);
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 0);
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 
 		assertActionCount("getObligations", null, a4, null, null, null, 0);
 		assertActionCount("getPowers", null, a4, null, null, null, 2); // can always arrogate and join as learner
 		assertActionCount("getPermissions", null, a4, null, null, null, 2);
 		assertActionCount("getObligations", null, a5, null, null, null, 0);
 		assertActionCount("getPowers", null, a5, null, null, null, 2); // can always arrogate and join as learner
 		assertActionCount("getPermissions", null, a5, null, null, null, 2);
 		
 		
 		/*
 		 * Time step 10
 		 * Actually restart
 		 * A1 proposes a value
 		 */
 		Object value = "ValueString";
 		insertAction(new Request0A(a1, newRevision, value, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPowers", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Response1B", null, newRevision, issue, cluster, 0);
 		assertActionCount("getPowers", "Submit2A", null, newRevision, issue, cluster, 0);
 		assertActionCount("getPowers", "Vote2B", null, newRevision, issue, cluster, 0);
 		
 		assertFactCount("HasRole", newRevision, issue, cluster, 5);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", newRevision, issue, cluster, 0);
 		assertFactCount("Open_Vote", newRevision, issue, cluster, 0);
 		assertFactCount("Chosen", newRevision, issue, cluster, 0);
 		assertFactCount("Voted", newRevision, issue, cluster, 0);
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 0);
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, newRevision, issue, cluster, 1);
 		assertActionCount("getObligations", "Prepare1A", a1, newRevision, issue, cluster, 1);
 		
 		
 		/*
 		 * Time step 11
 		 * A1 sends prepare
 		 * Agents can respond
 		 */
 		Integer ballot = 1;
 		insertAction(new Prepare1A(a1, newRevision, ballot, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPowers", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Response1B", null, newRevision, issue, cluster, 3);
 		assertActionCount("getPermissions", "Response1B", null, newRevision, issue, cluster, 3); 
 		assertActionCount("getPowers", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", null, newRevision, issue, cluster, 0); // has power but not per
 		assertActionCount("getPowers", "Vote2B", null, newRevision, issue, cluster, 0);
 		assertActionCount("getPermissions", "Vote2B", null, newRevision, issue, cluster, 0);
 		
 		assertFactCount("HasRole", newRevision, issue, cluster, 5);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Open_Vote", newRevision, issue, cluster, 0);
 		assertFactCount("Chosen", newRevision, issue, cluster, 0);
 		assertFactCount("Voted", newRevision, issue, cluster, 0);
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 0);
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, newRevision, issue, cluster, 0);
 		assertActionCount("getObligations", "Response1B", null, newRevision, issue, cluster, 0); // no votes, so no obl
 
 		
 		/*
 		 * Time step 12
 		 * Agents respond
 		 * Leader is obligated to submit
 		 */
 		insertAction(new Response1B(a1, newRevision, IPCNV.bal(), IPCNV.val(), newRevision, ballot, issue, cluster));
 		insertAction(new Response1B(a2, newRevision, IPCNV.bal(), IPCNV.val(), newRevision, ballot, issue, cluster));
 		insertAction(new Response1B(a3, newRevision, IPCNV.bal(), IPCNV.val(), newRevision, ballot, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPowers", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Response1B", null, newRevision, issue, cluster, 3);
 		assertActionCount("getPermissions", "Response1B", null, newRevision, issue, cluster, 3); 
 		assertActionCount("getPowers", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Vote2B", null, newRevision, issue, cluster, 0);
 		assertActionCount("getPermissions", "Vote2B", null, newRevision, issue, cluster, 0);
 		
 		assertFactCount("HasRole", newRevision, issue, cluster, 5);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Open_Vote", newRevision, issue, cluster, 0);
 		assertFactCount("Chosen", newRevision, issue, cluster, 0);
 		assertFactCount("Voted", newRevision, issue, cluster, 0);
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 3); // added by the responses
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, newRevision, issue, cluster, 1);
 		assertActionCount("getObligations", "Submit2A", a1, newRevision, issue, cluster, 1); 
 		
 	
 		/*
 		 * Time step 13
 		 * Leader submits
 		 * Agents can vote
 		 */
 		insertAction(new Submit2A(a1, newRevision, ballot, value, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPowers", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Response1B", null, newRevision, issue, cluster, 3); // can still respond
 		assertActionCount("getPermissions", "Response1B", null, newRevision, issue, cluster, 3); 
 		assertActionCount("getPowers", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Vote2B", null, newRevision, issue, cluster, 3);
 		assertActionCount("getPermissions", "Vote2B", null, newRevision, issue, cluster, 3);
 		
 		assertFactCount("HasRole", newRevision, issue, cluster, 5);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Open_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Chosen", newRevision, issue, cluster, 0);
 		assertFactCount("Voted", newRevision, issue, cluster, 0);
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 3);
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, newRevision, issue, cluster, 0);
 		
 		
 		/*
 		 * Time step 14
 		 * Agents vote
 		 * Value should be chosen
 		 */
 		insertAction(new Vote2B(a1, newRevision, ballot, value, issue, cluster));
 		insertAction(new Vote2B(a3, newRevision, ballot, value, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPowers", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Response1B", null, newRevision, issue, cluster, 3); // can still respond
 		assertActionCount("getPermissions", "Response1B", null, newRevision, issue, cluster, 3); 
 		assertActionCount("getPowers", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Vote2B", null, newRevision, issue, cluster, 3);
 		assertActionCount("getPermissions", "Vote2B", null, newRevision, issue, cluster, 3);
 		
 		assertFactCount("HasRole", newRevision, issue, cluster, 5);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Open_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Chosen", newRevision, issue, cluster, 1);
 		assertFactCount("Voted", newRevision, issue, cluster, 2);
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 5); // updated by the responses
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, newRevision, issue, cluster, 0);
 		
 		/*
 		 * Time step 15
 		 * A2 votes, A3 votes again !
 		 * Value should still be chosen, Voted and Reportedvote counts shouldn't change...
 		 */
 		insertAction(new Vote2B(a2, newRevision, ballot, value, issue, cluster));
 		insertAction(new Vote2B(a3, newRevision, ballot, value, issue, cluster));
 		incrementTime();
 		
 		assertActionCount("getPowers", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Response1B", null, newRevision, issue, cluster, 3); // can still respond
 		assertActionCount("getPermissions", "Response1B", null, newRevision, issue, cluster, 3); 
 		assertActionCount("getPowers", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", null, newRevision, issue, cluster, 1);
 		assertActionCount("getPowers", "Vote2B", null, newRevision, issue, cluster, 3);
 		assertActionCount("getPermissions", "Vote2B", null, newRevision, issue, cluster, 3);
 		
 		assertFactCount("HasRole", newRevision, issue, cluster, 5);
 		assertFactCount("Sync", newRevision, issue, cluster, 0);
 		assertFactCount("NeedToSync", newRevision, issue, cluster, 0);
 		assertQuorumSize(newRevision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Open_Vote", newRevision, issue, cluster, 1);
 		assertFactCount("Chosen", newRevision, issue, cluster, 1);
 		assertFactCount("Voted", newRevision, issue, cluster, 3); //only updated by one
 		assertFactCount("ReportedVote", newRevision, issue, cluster, 6);
 		assertActionCount("getObligations", "Revise", a1, newRevision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", newRevision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", newRevision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, newRevision, issue, cluster, 0);
 		
 		logger.info("Finished testing algorithm after Revision following negative Syncack\n");
 		
 	}
 	
 	@Test
 	public void testSyncAckYes() throws Exception {
 		logger.info("\nTrying a SyncAck yes...");
 		
 		HashMap<String, Object> map = new HashMap<String,Object>();
 		testPossibleRevisionDetectionSetup(map);
 		
 		//get data out of map
 		IPConAgent a5 = (IPConAgent) map.get("a5");
 		Integer revision = (Integer) map.get("revision");
 		String issue = (String) map.get("issue");
 		UUID cluster = (UUID) map.get("cluster");
 		
 		
 		// Check things are right after Time step 6
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		assertFactCount("Sync", revision, issue, cluster, 1);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 5);
 		assertFactCount("ReportedVote", revision, issue, cluster, 6);
 		assertActionCount("getObligations", "Revise", null, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 1); // if ag5 says no, safety is violated, if they say yes then PRR goes away
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 		
 		
 		/*
 		 * Time step 7
 		 * Ag5 says yes
 		 * Safety should not be violated
 		 */
 		insertAction(new SyncAck(a5, "A", revision, issue, cluster));
 		incrementTime();
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 6); // add a vote
 		assertFactCount("ReportedVote", revision, issue, cluster, 7); // add a reportedvote
 		
 		outputObjects();
 		
 		// Check safety was preserved and an obligation to revise does not exist
 		assertActionCount("getObligations", "Revise", null, revision, issue, cluster, 0);
 		// PAR still here because we now have 3 for, 2 notFor and QS of 3
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 1);
 		// PRR goes away because theres an additional "yes" vote
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		logger.info("Finished checking SyncAck yes.\n");
 		
 	}
 	
 	@Test
 	public void testRemRevision() throws Exception {
 		
 		testRemRevision(2);
 		tearDown();
 		setUp();
 		testRemRevision(3);
 		tearDown();
 		setUp();
 		testRemRevision(4);
 		tearDown();
 		setUp();
 		testRemRevision(1);
 	}
 	
 	public void testRemRevision(int agentToLeave) throws Exception {
 		logger.info("\nTesting PossRemRevision with agent " + agentToLeave + "...");
 		
 		Integer revision = 1;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		Object v1 = "A";
 		Integer ballot1 = 1;
 		
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1); initAgent(a1, new Role[]{Role.LEADER, Role.ACCEPTOR, Role.PROPOSER}, revision, issue, cluster);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2); initAgent(a2, Role.ACCEPTOR, revision, issue, cluster);
 		IPConAgent a3 = new IPConAgent("a3"); session.insert(a3); initAgent(a3, Role.ACCEPTOR, revision, issue, cluster);
 		IPConAgent a4 = new IPConAgent("a4"); session.insert(a4); initAgent(a4, Role.ACCEPTOR, revision, issue, cluster);
 		IPConAgent a5 = new IPConAgent("a5"); session.insert(a5); initAgent(a5, Role.ACCEPTOR, revision, issue, cluster);
 		
 		// Initially, a1&2&5 voted, 3,4 not.
 		session.insert(new Voted(a1, revision, ballot1, v1, issue, cluster));
 		session.insert(new Voted(a2, revision, ballot1, v1, issue, cluster));
 		session.insert(new Voted(a5, revision, ballot1, v1, issue, cluster));
 		session.insert(new ReportedVote(a1, revision, ballot1, v1, revision, ballot1, issue, cluster));
 		session.insert(new ReportedVote(a2, revision, ballot1, v1, revision, ballot1, issue, cluster));
 		session.insert(new ReportedVote(a3, revision, IPCNV.bal(), IPCNV.val(), revision, ballot1, issue, cluster));
 		session.insert(new ReportedVote(a4, revision, IPCNV.bal(), IPCNV.val(), revision, ballot1, issue, cluster));
 		session.insert(new ReportedVote(a5, revision, ballot1, v1, revision, ballot1, issue, cluster));
 		session.insert(new Pre_Vote(revision, ballot1, issue, cluster));
 		session.insert(new Open_Vote(revision, ballot1, v1, issue, cluster));
 		
 
		//outputObjects();
 		
 		incrementTime();
 		
 		//initially assert
 		assertFactCount("HasRole", revision, issue, cluster, 7);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 1);
 		assertFactCount("Open_Vote", revision, issue, cluster, 1);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 8); // 5 novote at start, 3 actual vote
 		assertFactCount("ReportedVote", revision, issue, cluster, 10); // 5 novote, 3 vote, 2 extra reported novote
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 0);
 		
 		// FIXME TODO work out why this sometimes fails 
 		assertActionCount("getPowers", "LeaveCluster", a5, revision, issue, cluster, 1);
 		
 		
 		/*
 		 * Time Step 1
 		 * A5 leaves; no immediate effect as status quo holds.
 		 * PRR init.
 		 */
 		insertAction(new LeaveCluster(a5, cluster));
 		incrementTime();
 		
		//outputObjects();
		
 		assertFactCount("HasRole", revision, issue, cluster, 6);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 3);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 1);
 		assertFactCount("Open_Vote", revision, issue, cluster, 1);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 8); // 5 novote at start, 3 actual vote
 		assertFactCount("ReportedVote", revision, issue, cluster, 8); // 5 novote, 3 vote, 2 extra reported novote - a5's two that are removed
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // if someone joined then while they're synching this will be 1
		assertFactCount("PossibleRemRevision", revision, issue, cluster, 1); // should be 1 because it's now 2v2, so if one of the voters leaves it will require a revision
 		assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 0);
 		
 		/*
 		 * Time Step 2
 		 * if A1/2 leave : Revise
 		 * if A3/4 leave : No need
 		 */
 		IPConAgent agent = null;
 		switch (agentToLeave) {
 			case 1 :
 				agent = a1;
 				break;
 			case 2:
 				agent = a2;
 				break;
 			case 3 :
 				agent = a3;
 				break;
 			case 4 :
 				agent = a4;
 				break;
 			default : logger.warn("Unrecognised agent: " + agentToLeave); fail(); break;
 		}
 		insertAction(new LeaveCluster(agent, cluster));
 		incrementTime();
 		
		outputObjects();
		
 		// Check common ones...
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 1);
 		assertFactCount("Open_Vote", revision, issue, cluster, 1);
 		assertFactCount("Chosen", revision, issue, cluster, 1);
 		assertFactCount("Voted", revision, issue, cluster, 8); // 5 novote at start, 3 actual vote
 		assertFactCount("ReportedVote", revision, issue, cluster, 6); // 5 novote, 3 vote, 2 extra reported novote, minus a5's 2 and the recent leavee's 2
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		
 		switch (agentToLeave) {
 			case 1 : 
 				//if 1 leaves
 				assertFactCount("HasRole", revision, issue, cluster, 3);
 				assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 				assertActionCount("getObligations", "Revise", null, revision, issue, cluster, 0); // no one to be obligated !
 				break;
 			
 			case 2 : 
 				//if 2 leaves
 				assertFactCount("HasRole", revision, issue, cluster, 5);
 				assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 				assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 1);
 				assertActionCount("getObligations", null, null, revision, issue, cluster, 1);
 				break;
 			
 			case 3 : 
 			case 4 : 
 				//otherwise
 				assertFactCount("HasRole", revision, issue, cluster, 5);
 				assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 				assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 0);
 				assertActionCount("getObligations", null, null, revision, issue, cluster, 0);
 				break;
 
 			default : // won't get here..
 		}
 		
 		/*
 		 * Time step 3
 		 * In the case of a1 leaving, a2 arrogates
 		 * Should get the obligation to revise ?
 		 */
 		if (agentToLeave==1) {
 			insertAction(new ArrogateLeadership(a2, revision, issue, cluster));
 			incrementTime();
 			
 			assertFactCount("HasRole", revision, issue, cluster, 4);
 			assertFactCount("Sync", revision, issue, cluster, 0);
 			assertFactCount("NeedToSync", revision, issue, cluster, 0);
 			assertQuorumSize(revision, issue, cluster, 2);
 			assertFactCount("Pre_Vote", revision, issue, cluster, 1);
 			assertFactCount("Open_Vote", revision, issue, cluster, 1);
 			assertFactCount("Chosen", revision, issue, cluster, 1);
 			assertFactCount("Voted", revision, issue, cluster, 8); // 5 novote at start, 3 actual vote
 			assertFactCount("ReportedVote", revision, issue, cluster, 6); // 5 novote, 3 vote, 2 extra reported novote minus a5*2, and another 2
 			assertFactCount("PossibleAddRevision", revision, issue, cluster, 0); // if someone joined then while they're synching this will be 1
 			assertFactCount("PossibleRemRevision", revision, issue, cluster, 1);
 			
 			assertActionCount("getObligations", "Revise", a2, revision, issue, cluster, 1);
 			assertActionCount("getObligations", null, null, revision, issue, cluster, 1);
 			
 		}
 		else if (agentToLeave==1) {
 			// do nothing
 			incrementTime();
 			// 1 is still obligated
 			assertActionCount("getObligations", "Revise", a1, revision, issue, cluster, 1);
 			assertActionCount("getObligations", null, null, revision, issue, cluster, 1);
 		}
 		else {
 			// do nothing
 		}
 
 		logger.info("Finished testing PossRemRevision with agent " + agentToLeave + "\n");
 	}
 	
 	@Test
 	public void testDuellingLeaders() throws Exception {
 		logger.info("\nTesting obligations on duelling leaders...");
 		
 		Integer revision = 10;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		Object v1 = "Value1";
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1); initAgent(a1, new Role[]{Role.LEADER, Role.ACCEPTOR, Role.PROPOSER}, revision, issue, cluster);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2); initAgent(a2, new Role[]{Role.LEADER,  Role.ACCEPTOR}, revision, issue, cluster);
 		
 		// Initially
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 5);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 0);
 		assertFactCount("Open_Vote", revision, issue, cluster, 0);
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		assertFactCount("Voted", revision, issue, cluster, 2); 
 		assertFactCount("ReportedVote", revision, issue, cluster, 2); // initial novote 
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 0);
 		
 		/*
 		 * Time Step 1
 		 * A1 proposes
 		 * A1 and A2 both obligated to prepare
 		 */
 		insertAction(new Request0A(a1, revision, v1, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 5);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 0);
 		assertFactCount("Open_Vote", revision, issue, cluster, 0);
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		assertFactCount("Voted", revision, issue, cluster, 2); 
 		assertFactCount("ReportedVote", revision, issue, cluster, 2); // initial novote 
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 1); // they both share one...
 		assertActionCount("getObligations", "Prepare1A", a1, revision, issue, cluster, 1);
 		assertActionCount("getObligations", "Prepare1A", a2, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", a1, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Prepare1A", a2, revision, issue, cluster, 1); // FIXME TODO why does this fail sometimes ?
 
 		/*
 		 * Time step 2
 		 * A2 prepares
 		 * Obligations discharged
 		 */
 		Integer ballot = 1;
 		insertAction(new Prepare1A(a2, revision, ballot, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision, issue, cluster, 5);
 		assertFactCount("Sync", revision, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision, issue, cluster, 0);
 		assertQuorumSize(revision, issue, cluster, 2);
 		assertFactCount("Pre_Vote", revision, issue, cluster, 1);
 		assertFactCount("Open_Vote", revision, issue, cluster, 0);
 		assertFactCount("Chosen", revision, issue, cluster, 0);
 		assertFactCount("Voted", revision, issue, cluster, 2); 
 		assertFactCount("ReportedVote", revision, issue, cluster, 2); // initial novote 
 		assertFactCount("PossibleAddRevision", revision, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision, issue, cluster, 2); // acceptor obligation not shared
 		assertActionCount("getObligations", "Response1B", a1, revision, issue, cluster, 1);
 		assertActionCount("getObligations", "Response1B", a2, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Response1B", a1, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Response1B", a2, revision, issue, cluster, 1);
 		
 		logger.info("Finished testing obligations on duelling leaders\n");
 	}
 	
 	@Test
 	public void testBallotOrderingPromise() throws Exception {
 		logger.info("\nTesting ballot ordering promise...");
 		
 		Integer revision1 = 1;
 		Integer revision2 = 2;
 		Integer ballot5 = 5;
 		Integer ballot7 = 7;
 		Integer ballot10 = 10;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		Object v1 = "Value1";
 		Object v2 = "Value2";
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1); initAgent(a1, new Role[]{Role.LEADER, Role.ACCEPTOR, Role.PROPOSER}, revision1, issue, cluster);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2); initAgent(a2, new Role[]{Role.ACCEPTOR}, revision1, issue, cluster);
 		/*
 		 * Initially voted in 1:10, 2:5, 2:10
 		 */
 		session.insert(new Voted(a1, revision1, ballot10, v1, issue, cluster));
 		session.insert(new ReportedVote(a1, revision1, ballot10, v1, revision1, ballot10, issue, cluster));
 		session.insert(new Voted(a2, revision1, ballot10, v1, issue, cluster));
 		session.insert(new ReportedVote(a2, revision1, ballot10, v1, revision1, ballot10, issue, cluster));
 		insertAction(new Revise(a1, revision1, issue, cluster));
 		
 		session.insert(new Pre_Vote(revision2, ballot5, issue, cluster));
 		session.insert(new Open_Vote(revision2, ballot5, v1, issue, cluster));
 		session.insert(new Voted(a2, revision2, ballot5, v1, issue, cluster));
 		session.insert(new ReportedVote(a2, revision2, ballot5, v1, revision2, ballot5, issue, cluster));
 		session.insert(new ReportedVote(a1, revision2, IPCNV.bal(), IPCNV.val(), revision2, ballot5, issue, cluster));
 
 		session.insert(new Pre_Vote(revision2, ballot10, issue, cluster));
 		session.insert(new ReportedVote(a2, revision2, ballot5, v1, revision2, ballot10, issue, cluster));
 		session.insert(new ReportedVote(a1, revision2, IPCNV.bal(), IPCNV.val(), revision2, ballot10, issue, cluster));
 		session.insert(new Open_Vote(revision2, ballot10, v2, issue, cluster));
 		session.insert(new Voted(a1, revision2, ballot10, v2, issue, cluster));
 		session.insert(new ReportedVote(a1, revision2, ballot10, v2, revision2, ballot10, issue, cluster));
 		session.insert(new Voted(a2, revision2, ballot10, v2, issue, cluster));
 		session.insert(new ReportedVote(a2, revision2, ballot10, v2, revision2, ballot10, issue, cluster));
 		
 		insertAction(new Request0A(a1, revision2, null, issue, cluster));
 		
 		incrementTime();
 		
 		assertFactCount("HasRole", revision2, issue, cluster, 4);
 		assertFactCount("Sync", revision2, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision2, issue, cluster, 0);
 		assertQuorumSize(revision2, issue, cluster, 2);
 		assertFactCount("Pre_Vote", revision2, issue, cluster, 2);
 		assertFactCount("Open_Vote", revision2, issue, cluster, 2);
 		assertFactCount("Chosen", revision2, issue, cluster, 1);
 		assertFactCount("Voted", revision2, issue, cluster, 3); 
 		assertFactCount("ReportedVote", revision2, issue, cluster, 6);  
 		assertFactCount("PossibleAddRevision", revision2, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision2, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision2, issue, cluster, 0); // prevote already exists, so no obligation to prepare
 		assertActionCount("getPermissions", "Response1B", a1, revision2, issue, cluster, 2); // can respond to 5 and 10 as many times as they like
 		assertActionCount("getPermissions", "Response1B", a2, revision2, issue, cluster, 2);
 		assertActionCount("getPermissions", "Submit2A", a1, revision2, issue, cluster, 0); // (unrealistically, nothing has been proposed, so no permission) 
 		
 		
 		/*
 		 * Prepare at 2:7
 		 * Check obl/per for response
 		 * Should be no change since no new Pre_Vote created
 		 */
 		insertAction(new Prepare1A(a1, revision2, ballot7, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision2, issue, cluster, 4);
 		assertFactCount("Sync", revision2, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision2, issue, cluster, 0);
 		assertQuorumSize(revision2, issue, cluster, 2);
 		assertFactCount("Pre_Vote", revision2, issue, cluster, 2);
 		assertFactCount("Open_Vote", revision2, issue, cluster, 2);
 		assertFactCount("Chosen", revision2, issue, cluster, 1);
 		assertFactCount("Voted", revision2, issue, cluster, 3); 
 		assertFactCount("ReportedVote", revision2, issue, cluster, 6);  
 		assertFactCount("PossibleAddRevision", revision2, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision2, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision2, issue, cluster, 0);
 		assertActionCount("getPermissions", "Response1B", a1, revision2, issue, cluster, 2);  // no change
 		assertActionCount("getPermissions", "Response1B", a2, revision2, issue, cluster, 2);
 		assertActionCount("getPermissions", "Submit2A", a1, revision2, issue, cluster, 0);
 		assertActionCount("getPermissions", "Vote2B", a1, revision2, issue, cluster, 1); // can revote in 10, but not in 5 (no openvote for 7)
 		assertActionCount("getPermissions", "Vote2B", a2, revision2, issue, cluster, 1);
 		
 		
 		/*
 		 * A1 submits at 7 anyway
 		 * Check no permission to vote (as voted in 10)
 		 */
 		insertAction(new Submit2A(a1, revision2, ballot7, v2, issue, cluster));
 		incrementTime();
 		
 		assertFactCount("HasRole", revision2, issue, cluster, 4);
 		assertFactCount("Sync", revision2, issue, cluster, 0);
 		assertFactCount("NeedToSync", revision2, issue, cluster, 0);
 		assertQuorumSize(revision2, issue, cluster, 2);
 		assertFactCount("Pre_Vote", revision2, issue, cluster, 2);
 		assertFactCount("Open_Vote", revision2, issue, cluster, 3);
 		assertFactCount("Chosen", revision2, issue, cluster, 1);
 		assertFactCount("Voted", revision2, issue, cluster, 3); 
 		assertFactCount("ReportedVote", revision2, issue, cluster, 6);  
 		assertFactCount("PossibleAddRevision", revision2, issue, cluster, 0);
 		assertFactCount("PossibleRemRevision", revision2, issue, cluster, 0);
 		
 		assertActionCount("getObligations", null, null, revision2, issue, cluster, 0);
 		assertActionCount("getPermissions", "Response1B", a1, revision2, issue, cluster, 2);  // no change
 		assertActionCount("getPermissions", "Response1B", a2, revision2, issue, cluster, 2);
 		assertActionCount("getPermissions", "Submit2A", a1, revision2, issue, cluster, 0);
 		assertActionCount("getPermissions", "Vote2B", a1, revision2, issue, cluster, 1); // can revote in 10, but not in 5 or 7 as voted higher
 		assertActionCount("getPermissions", "Vote2B", a2, revision2, issue, cluster, 1);
 		Vote2B a1Act = (Vote2B) getActionQueryResultsForRIC("getPermissions", "Vote2B", a1, revision2, issue, cluster).toArray()[0];
 		Vote2B a2Act = (Vote2B) getActionQueryResultsForRIC("getPermissions", "Vote2B", a2, revision2, issue, cluster).toArray()[0];
 		assertEquals(ballot10, a1Act.getBallot());
 		assertEquals(ballot10, a2Act.getBallot());
 		assertEquals(revision2, a1Act.getRevision());
 		assertEquals(revision2, a2Act.getRevision());
 		
 		logger.info("\nFinished testing ballot ordering promise.");
 	}
 	
 	@Test
 	public void demoNarrative() throws Exception {
 		
 		logger.info("Starting demo narrative....\n");
 		
 		Integer revision1 = 1;
 		Integer revision2 = 2;
 		String issue = "IssueString";
 		UUID cluster = Random.randomUUID();
 		Integer ballot1 = 1;
 		IPConAgent[] none = null;
 		
 		
 		session.insert(new IPConRIC(revision1, issue, cluster));
 		IPConAgent a7 = new IPConAgent("a7"); session.insert(a7); // not given a role yet
 		IPConAgent a8 = new IPConAgent("a8"); session.insert(a8); // (need to add first or leader won't get power)
 		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1); initAgent(a1, Role.ACCEPTOR, revision1, issue, cluster);
 		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2); initAgent(a2, new Role[]{Role.ACCEPTOR, Role.PROPOSER}, revision1, issue, cluster);
 		IPConAgent a3 = new IPConAgent("a3"); session.insert(a3); initAgent(a3, Role.ACCEPTOR, revision1, issue, cluster);
 		IPConAgent a4 = new IPConAgent("a4"); session.insert(a4); initAgent(a4, Role.ACCEPTOR, revision1, issue, cluster);
 		IPConAgent a5 = new IPConAgent("a5"); session.insert(a5); initAgent(a5, Role.ACCEPTOR, revision1, issue, cluster);
 		IPConAgent a6 = new IPConAgent("a6"); session.insert(a6); initAgent(a6, new Role[]{Role.ACCEPTOR, Role.LEADER}, revision1, issue, cluster);
 		
 
 		// A3 did not vote
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a4, a5, a6}) {
 			session.insert(new Voted(ag, revision1, ballot1, "a", issue, cluster));
 			session.insert(new ReportedVote(ag, revision1, ballot1, "a", revision1, ballot1, issue, cluster));
 			
 		}
 		session.insert(new Chosen(revision1, 1, "a", issue, cluster));
 		none = new IPConAgent[]{a7, a8};
 		incrementTime();
 
 		
 		/*
 		 * Check initially
 		 */
 		assertFactCount("IPConRIC", null , issue, cluster, 1);
 		
 		checkProposerPowPer(a2, revision1, issue, cluster, 0, 0);
 		checkLeaderPowPer(a6, revision1, issue, cluster, 8, 8, 6, 0, 0, 0);
 		for (IPConAgent ag : new IPConAgent[]{a1, a3, a4, a5}) {
 			checkAccPowPer(ag, revision1, issue, cluster, 0, 0);
 		}
 		
 		//check obligations
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3, a4, a5, a6, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 		
 		checkNoPowPerObl(none);
 		
 		
 		/*
 		 * Time step 1
 		 * A4-6 leave cluster, A1 arrogates
 		 * Demonstrates fragmentation, role failure, and self-organisation of leadership
 		 * A4-8 have no powers or permissions except Arrogate
 		 * A1 takes leadership powers and permissions
 		 */
 		for (IPConAgent ag : new IPConAgent[]{a4, a5, a6}) {
 			insertAction(new LeaveCluster(ag, cluster));
 		}
 		insertAction(new ArrogateLeadership(a1, revision1, issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision1, issue, cluster, 0, 0);
 		checkLeaderPowPer(a1, revision1, issue, cluster, 8, 5, 3, 0, 0, 0);
 		for (IPConAgent ag : new IPConAgent[]{a3}) {
 			checkAccPowPer(ag, revision1, issue, cluster, 0, 0);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 		
 		none = new IPConAgent[]{a4, a5, a6, a7, a8};
 		checkNoPowPerObl(none);
 		
 		
 		/*
 		 * Time step 2
 		 * A1 adds A7,8
 		 * Demonstrates aggregation (1)
 		 * A1 obligated to SyncAck A7 and A8, gains permission to do so
 		 */
 		insertAction(new AddRole(a1, a7, Role.ACCEPTOR, revision1, issue, cluster));
 		insertAction(new AddRole(a1, a8, Role.ACCEPTOR, revision1, issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision1, issue, cluster, 0, 0);
 		checkLeaderPowPer(a1, revision1, issue, cluster, 8, 7, 5, 2, 0, 0);
 		for (IPConAgent ag : new IPConAgent[]{a3}) {
 			checkAccPowPer(ag, revision1, issue, cluster, 0, 0);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a2, a3, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 		assertActionCount("getObligations", null, a1, null, null, null, 2);
 		assertActionCount("getObligations", "SyncReq", a1, null, null, null, 2);
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		
 		/*
 		 * Time step 3
 		 * A1 issues syncreq to 7 and 8
 		 * Demonstrates aggregation (2)
 		 * A7 and A8 have power, permission, and obligation to SyncAck (pow & per for both yes and no)
 		 */
 		insertAction(new SyncReq(a1, a7, "a", revision1, issue, cluster));
 		insertAction(new SyncReq(a1, a8, "a", revision1, issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision1, issue, cluster, 0, 0);
 		checkLeaderPowPer(a1, revision1, issue, cluster, 8, 7, 5, 0, 0, 0);
 		checkAccPowPer(a3, revision1, issue, cluster, 0, 0);
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a7, a8}) {
 			checkAccPowPer(ag, revision1, issue, cluster, 2, 2); // additional Pow&Per to SyncAck Yes & No
 			assertActionCount("getPowers", "SyncAck", ag, revision1, issue, cluster, 2);
 			assertActionCount("getPermissions", "SyncAck", ag, revision1, issue, cluster, 2);
 			assertActionCount("getObligations", null, ag, null, null, null, 1); // syncAck
 			assertActionCount("getObligations", "SyncAck", ag, revision1, issue, cluster, 1);
 		}
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		
 		/*
 		 * Time step 4
 		 * A7&A8 sync no
 		 * Demonstrates aggregation (3)
 		 * A1 obligated to revise
 		 */
 		insertAction(new SyncAck(a7, IPCNV.val(), revision1, issue, cluster));
 		insertAction(new SyncAck(a8, IPCNV.val(), revision1, issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision1, issue, cluster, 0, 0);
 		checkLeaderPowPer(a1, revision1, issue, cluster, 8, 7, 5, 0, 0, 0);
 		for (IPConAgent ag : new IPConAgent[]{a3, a7, a8}) {
 			checkAccPowPer(ag, revision1, issue, cluster, 0, 0);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a2, a3, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 		
 		
 		assertActionCount("getObligations", null, a1, null, null, null, 1); // revise
 		assertActionCount("getObligations", "Revise", a1, revision1, issue, cluster, 1);
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		
 		
 		/*
 		 * Time step 5
 		 * A1 revises
 		 * Demonstrates revision: parameter change
 		 * A1 no longer obligated to revise
 		 * All agents at standard pow & per
 		 */
 		insertAction(new Revise(a1, revision1, issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision2, issue, cluster, 0, 0);
 		checkLeaderPowPer(a1, revision2, issue, cluster, 8, 7, 5, 0, 0, 0);
 		for (IPConAgent ag : new IPConAgent[]{a3, a7, a8}) {
 			checkAccPowPer(ag, revision2, issue, cluster, 0, 0);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3, a7, a8}) {
 			// TODO FIXME work out why this sometimes fails
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 		
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		
 		/*
 		 * Time step 6
 		 * A2 request/proposes
 		 * Demonstrates algorithm flow (1)
 		 * A1 is obligated to prepare
 		 */
 		insertAction(new Request0A(a2, revision2, "b", issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision2, issue, cluster, 0, 0);
 		checkLeaderPowPer(a1, revision2, issue, cluster, 8, 7, 5, 0, 0, 0);
 		for (IPConAgent ag : new IPConAgent[]{a3, a7, a8}) {
 			checkAccPowPer(ag, revision2, issue, cluster, 0, 0);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a2, a3, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 		assertActionCount("getObligations", "Prepare1A", a1, revision2, issue, cluster, 1);
 		assertActionCount("getObligations", null, a1, revision2, issue, cluster, 1);
 		
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		
 		/*
 		 * Time step 7
 		 * A1 prepares
 		 * Demonstrates algorithm flow (2)
 		 * All gain power and permission to respond
 		 */
 		insertAction(new Prepare1A(a1, revision2, ballot1, issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision2, issue, cluster, 1, 1); // additional powper to response
 		assertActionCount("getPowers", "Response1B", a2, revision2, issue, cluster, 1);
 		assertActionCount("getPermissions", "Response1B", a2, revision2, issue, cluster, 1);
 		assertActionCount("getPowers", "Response1B", a1, revision2, issue, cluster, 1);
 		assertActionCount("getPermissions", "Response1B", a1, revision2, issue, cluster, 1);
 		assertActionCount("getPowers", "Submit2A", a1, revision2, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", a1, revision2, issue, cluster, 0);
 		checkLeaderPowPer(a1, revision2, issue, cluster, 8, 7, 5, 0, 2, 1); // additional powper to response, and pow to submit
 		for (IPConAgent ag : new IPConAgent[]{a3, a7, a8}) {
 			checkAccPowPer(ag, revision2, issue, cluster, 1, 1);
 			assertActionCount("getPowers", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Response1B", ag, revision2, issue, cluster, 1);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0); // no one voted so no obligation
 		}
 		
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		
 		/*
 		 * Time step 8
 		 * All respond
 		 * Demonstrates algorithm flow (3)
 		 * A1 gains permission to, and is obligated to, submit
 		 */
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3, a7, a8}) {
 			insertAction(new Response1B(ag, revision2, IPCNV.bal(), IPCNV.val(), revision2, ballot1, issue, cluster));
 		}
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision2, issue, cluster, 1, 1); // additional powper to response
 		for (IPConAgent ag : new IPConAgent[]{a1, a2}) {
 			assertActionCount("getPowers", "Response1B", ag, revision2, issue, cluster, 1);
 		assertActionCount("getPermissions", "Response1B", ag, revision2, issue, cluster, 1);
 		}
 		assertActionCount("getPowers", "Submit2A", a1, revision2, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", a1, revision2, issue, cluster, 1);
 		checkLeaderPowPer(a1, revision2, issue, cluster, 8, 7, 5, 0, 2, 2); // additional powper to response and submit
 		for (IPConAgent ag : new IPConAgent[]{a3, a7, a8}) {
 			checkAccPowPer(ag, revision2, issue, cluster, 1, 1);
 			assertActionCount("getPowers", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Response1B", ag, revision2, issue, cluster, 1);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a2, a3, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0); // no one voted so no obligation
 		}
 		assertActionCount("getObligations", "Submit2A", a1, revision2, issue, cluster, 1); // obl to submit
 		assertActionCount("getObligations", null, a1, null, null, null, 1);
 		
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		
 		/*
 		 * Time step 9
 		 * A1 submits
 		 * Demonstrates algorithm flow (4)
 		 * All now gain pow & per to vote
 		 * A1 no longer obligated to submit
 		 */
 		insertAction(new Submit2A(a1, revision2, ballot1, "b", issue, cluster));
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision2, issue, cluster, 2, 2); // additional powper to response&vote
 		for (IPConAgent ag : new IPConAgent[]{a1, a2}) {
 			assertActionCount("getPowers", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPowers", "Vote2B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Vote2B", ag, revision2, issue, cluster, 1);
 		}
 		assertActionCount("getPowers", "Submit2A", a1, revision2, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", a1, revision2, issue, cluster, 1);
 		checkLeaderPowPer(a1, revision2, issue, cluster, 8, 7, 5, 0, 3, 3); // additional powper to response and vote and submit
 		for (IPConAgent ag : new IPConAgent[]{a3, a7, a8}) {
 			checkAccPowPer(ag, revision2, issue, cluster, 2, 2);
 			assertActionCount("getPowers", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPowers", "Vote2B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Vote2B", ag, revision2, issue, cluster, 1);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0); // no one voted so no obligation
 		}
 		
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		assertFactCount("Chosen", revision2, issue, cluster, 0);
 		
 		/*
 		 * Time step 10
 		 * All vote
 		 * Demonstrates algorithm flow (5)
 		 * No change in pow or per or obl
 		 * Value now chosen
 		 */
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3, a7, a8}) {
 			insertAction(new Vote2B(ag, revision2, ballot1, "b", issue, cluster));
 		}
 		incrementTime();
 		
 		checkProposerPowPer(a2, revision2, issue, cluster, 2, 2); // additional powper to response&vote
 		for (IPConAgent ag : new IPConAgent[]{a1, a2}) {
 			assertActionCount("getPowers", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPowers", "Vote2B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Vote2B", ag, revision2, issue, cluster, 1);
 		}
 		assertActionCount("getPowers", "Submit2A", a1, revision2, issue, cluster, 1);
 		assertActionCount("getPermissions", "Submit2A", a1, revision2, issue, cluster, 1);
 		checkLeaderPowPer(a1, revision2, issue, cluster, 8, 7, 5, 0, 3, 3); // additional powper to response and vote and submit
 		for (IPConAgent ag : new IPConAgent[]{a3, a7, a8}) {
 			checkAccPowPer(ag, revision2, issue, cluster, 2, 2);
 			assertActionCount("getPowers", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Response1B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPowers", "Vote2B", ag, revision2, issue, cluster, 1);
 			assertActionCount("getPermissions", "Vote2B", ag, revision2, issue, cluster, 1);
 		}
 		for (IPConAgent ag : new IPConAgent[]{a1, a2, a3, a7, a8}) {
 			assertActionCount("getObligations", null, ag, null, null, null, 0); // no one voted so no obligation
 		}
 		
 		none = new IPConAgent[]{a4, a5, a6};
 		checkNoPowPerObl(none);
 		assertFactCount("Chosen", revision2, issue, cluster, 1);
 		
 		logger.info("\nFinished demo narrative.");
 	}
 	
 	private void checkNoPowPerObl(IPConAgent[] list) {
 		for (IPConAgent ag : list) {
 			assertActionCount("getPowers", "ArrogateLeadership", ag, null, null, null, 1);
 			assertActionCount("getPowers", "JoinAsLearner", ag, null, null, null, 1);
 			assertActionCount("getPowers", null, ag, null, null, null, 2); // can always arrogate, join (and leave if you have a role)
 			assertActionCount("getPermissions", "ArrogateLeadership", ag, null, null, null, 1);
 			assertActionCount("getPermissions", "JoinAsLearner", ag, null, null, null, 1);
 			assertActionCount("getPermissions", null, ag, null, null, null, 2);
 			assertActionCount("getObligations", null, ag, null, null, null, 0);
 		}
 	
 	}
 	
 	private void checkProposerPowPer(IPConAgent proposer, Integer revision, String issue, UUID cluster, Integer addPow, Integer addPer){
 		assertActionCount("getPowers", null, proposer, revision, issue, cluster, 3+addPow); // can always arrogate and leave
 		assertActionCount("getPowers", "LeaveCluster", proposer, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "ArrogateLeadership", proposer, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "Request0A", proposer, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", null, proposer, revision, issue, cluster, 3+addPer);
 		assertActionCount("getPermissions", "LeaveCluster", proposer, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "ArrogateLeadership", proposer, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "Request0A", proposer, revision, issue, cluster, 1);
 	}
 	
 	/**
 	 * 
 	 * @param leader
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @param numAgents number of Agents in the kbase
 	 * @param numRoles number of roles in the RIC
 	 * @param numAcc number of acceptors in the RIC
 	 * @param numSync number of agents to be sync'd
 	 * @param addPow number of additional powers
 	 * @param addPer number of additional permissions
 	 */
 	private void checkLeaderPowPer(IPConAgent leader, Integer revision, String issue, UUID cluster, Integer numAgents, Integer numRoles, Integer numAcc, Integer numSync, Integer addPow, Integer addPer) {
 		assertActionCount("getPowers", "LeaveCluster", leader, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "ArrogateLeadership", leader, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "ResignLeadership", leader, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "RemRole", leader, revision, issue, cluster, numRoles);
 		assertActionCount("getPowers", "AddRole", leader, revision, issue, cluster, (numAgents*5 - numRoles)); // Lead, Learn, Acc, Prop, Invalid
 		assertActionCount("getPowers", "Revise", leader, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "SyncReq", leader, revision, issue, cluster, numAcc); // power but not per
 		assertActionCount("getPowers", "Prepare1A", leader, revision, issue, cluster, 1);
 		assertActionCount("getPowers", null, leader, revision, issue, cluster, (1+1+1+numRoles+(numAgents*5 - numRoles)+1+numAcc+1)+addPow);
 		assertActionCount("getPermissions", "LeaveCluster", leader, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "ArrogateLeadership", leader, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "ResignLeadership", leader, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "RemRole", leader, revision, issue, cluster, numRoles);
 		assertActionCount("getPermissions", "AddRole", leader, revision, issue, cluster, (numAgents*5 - numRoles)); // Lead, Learn, Acc, Prop, Invalid
 		assertActionCount("getPermissions", "Revise", leader, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "SyncReq", leader, revision, issue, cluster, numSync);
 		assertActionCount("getPermissions", "Prepare", leader, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", null, leader, revision, issue, cluster, (1+1+1+numRoles+(numAgents*5 - numRoles)+1+1)+addPer+numSync);
 	}
 	
 	private void checkAccPowPer(IPConAgent ag, Integer revision, String issue, UUID cluster, Integer addPow, Integer addPer) {
 		assertActionCount("getPowers", "LeaveCluster", ag, revision, issue, cluster, 1);
 		assertActionCount("getPowers", "ArrogateLeadership", ag, revision, issue, cluster, 1);
 		assertActionCount("getPowers", null, ag, revision, issue, cluster, 2+addPow); // can always arrogate and leave
 		assertActionCount("getPermissions", "LeaveCluster", ag, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", "ArrogateLeadership", ag, revision, issue, cluster, 1);
 		assertActionCount("getPermissions", null, ag, revision, issue, cluster, 2+addPer);
 	}
 	
 	
 	private final FactType typeFromString(String factTypeString) {
 		return rules.getKbase().getFactType("uk.ac.imperial.dws04.Presage2Experiments.IPCon", factTypeString);
 	}
 	
 	/**
 	 * Wrapper function to check an object from the kbase is a drls FactType. Replaces instanceof functionality.
 	 * @param obj
 	 * @param factTypeName
 	 * @return
 	 */
 	@Deprecated
 	private final boolean isFactType(Object obj, FactType factType) {
 		try {
 			return ( ( factType.newInstance().getClass() ).equals( ( obj.getClass() ) ) );
 		} catch (NullPointerException e) {
 			//e.printStackTrace();
 			return false;
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	private final Collection<IPConFact> getFactQueryResults(	final String factType,
 																final Integer revision,
 																final String issue,
 																final UUID cluster) {
 		String queryString = "getFacts";
 		HashSet<IPConFact> set = new HashSet<IPConFact>();
 		ArrayList<Object> lookup = new ArrayList<Object>();
 		lookup.addAll(Arrays.asList(new Object[]{revision, issue, cluster}));
 		if (revision==null) {
 			lookup.set(0, Variable.v);
 		}
 		if (issue==null) {
 			lookup.set(1, Variable.v);
 		}
 		if (cluster==null) {
 			lookup.set(2, Variable.v);
 		}
 		if (factType!=null) {
 			queryString = queryString+"Named";
 			lookup.add(factType);
 		}
 		QueryResults facts = session.getQueryResults(queryString, lookup.toArray());
 		for (QueryResultsRow row : facts) {
 			set.add((IPConFact)row.get("$fact"));
 		}
 		return set;
 	}
 	
 	private final void assertActionCount( final String queryName, final String actionType, final IPConAgent agent, Integer revision, String issue, UUID cluster, final int count) {
 		//assertEquals(count, getActionQueryResults(queryName, actionType, agent).size());
 		assertEquals(count, getActionQueryResultsForRIC(queryName, actionType, agent, revision, issue, cluster).size());
 	}
 	
 	/**
 	 * @param factTypeString
 	 * @param revision
 	 * @param issue 
 	 * @param cluster 
 	 * @param count
 	 * @return the collection of facts that matched the query
 	 */
 	private final Collection<Object> assertFactCount(final String factTypeString, Integer revision, String issue, UUID cluster, int count) {
 
 		Collection<Object> facts = new HashSet<Object>();
 		facts.addAll(getFactQueryResults(factTypeString, revision, issue, cluster));
 		// FIXME Checking to see if this is still required.. ?
 		//if (facts.size()==0) {
 		//	facts.addAll(assertObjectCount(factTypeString, revision, issue, cluster, count));
 		//}
 		assertEquals(count, facts.size());
 		return facts;
 	}
 	
 	/**
 	 * For compatibility with things I haven't updated to the new ways...
 	 * @param factTypeString
 	 * @param revision 
 	 * @param issue
 	 * @param cluster
 	 * @param count
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return
 	 */
 	@Deprecated
 	private final Collection<Object> assertObjectCount(final String factTypeString, final Integer revision, final String issue, final UUID cluster, final int count) {
 		Collection<Object> facts = new HashSet<Object>();
 		// try drls fact types
 		final FactType factType = typeFromString(factTypeString);
 		if (facts.size()==0) {
 			facts.addAll(session.getObjects(new ObjectFilter() {
 				@Override
 				public boolean accept(Object object) {
 					return (isFactType(object, factType) && matchesRIC(object, revision, issue, cluster) );
 				}
 			}));
 		}
 		// try java classes
 		//FIXME TODO would be nice to remove this... 
 		if (facts.size()==0) {
 			facts.addAll(session.getObjects(new ObjectFilter() {
 		
 				@Override
 				public boolean accept(Object object) {
 					Class<?> c;
 					try {
 						c = Class.forName("uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts." + factTypeString);
 						//logger.trace("Testing " + object + " which is a " + object.getClass());
 						return ( c.isInstance(object)  && matchesRIC(object, revision, issue, cluster) );
 					} catch (NullPointerException e) {
 						//e.printStackTrace();
 						return false;
 					} catch (ClassNotFoundException e) {
 						//e.printStackTrace();
 						return false;
 					}
 				}
 				
 			}));
 		}
 		assertEquals(count, facts.size());
 		return facts;
 	}
 	
 	/**
 	 * 
 	 * @param queryName should be "getPowers" "getPermissions" or "getObligations"
 	 * @param actionType type of action to filter by, or null to get all
 	 * @param agent agent to get filter by, or null to get all
 	 * @return Collection of IPConActions
 	 */
 	private final Collection<IPConAction> getActionQueryResults(final String queryName, final String actionType, final IPConAgent agent) {
 		HashSet<IPConAction> set = new HashSet<IPConAction>();
 		QueryResults results = null;
 		// Set agent to look up
 		Object lookup = null;
 		if (agent==null) {
 			lookup = Variable.v;
 		}
 		else {
 			lookup = agent;
 		}
 		// Add "Actions" to the string to get the other query
 		if (actionType!=null) {
 			results = session.getQueryResults(queryName+"Actions", new Object[]{ lookup, actionType });
 		}
 		else {
 			results = session.getQueryResults(queryName, new Object[]{ lookup });
 		}
 		for ( QueryResultsRow row : results ) {
 			set.add((IPConAction)row.get("$action"));
 		}
 		return set;
 	}
 	
 	private final Collection<IPConAction> getActionQueryResultsForRIC(	final String queryName,
 																	final String actionType,
 																	final IPConAgent agent,
 																	final Integer revision,
 																	final String issue,
 																	final UUID cluster) {
 		HashSet<IPConAction> set = new HashSet<IPConAction>();
 		for (IPConAction action : getActionQueryResults(queryName, actionType, agent)) {
 			//logger.trace("Checking: " + action);
 			if (matchesRIC(action, revision, issue, cluster)) {
 				set.add((IPConAction)action);
 			}
 		}
 		return set;
 	}
 	
 	/**
 	 * Utility function to check if an object either has-equal, or does not have, the given RIC.
 	 * If any arg is null, the check is ignored.
 	 * @param object
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return
 	 */
 	private final boolean matchesRIC(Object object, Integer revision, String issue, UUID cluster) {
 		Integer actionRev = null;
 		if (revision!=null) {
 			try {
 				actionRev = (Integer) object.getClass().getMethod("getRevision").invoke(object, (Object[])null);
 			} catch (Exception e) {
 				// do nothing - if it doesn't have such a method then stay null
 				//e.printStackTrace();
 			}
 		}
 		
 		String actionIssue = null;
 		if (issue!=null) {
 			try {
 				actionIssue = (String) object.getClass().getMethod("getIssue").invoke(object, (Object[])null);
 			} catch (Exception e) {
 				// do nothing
 			}
 		}
 		
 		UUID actionCluster = null;
 		if (cluster!=null) {
 			try {
 				actionCluster = (UUID) object.getClass().getMethod("getCluster").invoke(object, (Object[])null);
 			} catch (Exception e) {
 				// do nothing
 			}
 		}
 		
 		//logger.trace("Matching " + object + " against " + revision + ", " + issue + ", " + cluster + 
 		//				" and got r:" + actionRev + ", i:" + actionIssue + ", c:" + actionCluster + ".");
 		
 		return	( ( actionRev==null || actionRev.equals(revision) ) &&
 				( actionIssue==null || actionIssue.equals(issue)) &&
 				( actionCluster==null || actionCluster.equals(cluster)) );
 	}
 	
 	private final void assertQuorumSize(Integer revision, String issue, UUID cluster, Integer quorumSize) {
 		ArrayList<IPConFact> obj = new ArrayList<IPConFact>();
 		obj.addAll(getFactQueryResults("QuorumSize", revision, issue, cluster));
 		assertEquals(1, obj.size());
 		assertEquals(quorumSize, ((QuorumSize)obj.get(0)).getQuorumSize());
 	}
 
 	
 	
 
 }
