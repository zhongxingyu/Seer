 package uk.ac.imperial.lpgdash;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import uk.ac.imperial.lpgdash.LPGPlayer.ClusterLeaveAlgorithm;
 import uk.ac.imperial.lpgdash.LPGPlayer.ClusterSelectionAlgorithm;
 import uk.ac.imperial.lpgdash.actions.Generate;
 import uk.ac.imperial.lpgdash.actions.JoinCluster;
 import uk.ac.imperial.lpgdash.actions.LPGActionHandler;
 import uk.ac.imperial.lpgdash.allocators.LegitimateClaims;
 import uk.ac.imperial.lpgdash.allocators.QueueAllocator;
 import uk.ac.imperial.lpgdash.facts.Allocation;
 import uk.ac.imperial.lpgdash.facts.Cluster;
 import uk.ac.imperial.lpgdash.facts.Player;
import uk.ac.imperial.presage2.core.TimeDriven;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
 import uk.ac.imperial.presage2.core.simulator.Parameter;
 import uk.ac.imperial.presage2.core.simulator.Scenario;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 import uk.ac.imperial.presage2.rules.facts.SimParticipantsTranslator;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.network.NetworkModule;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 
public class LPGGameSimulation extends InjectedSimulation implements TimeDriven {
 
 	private final Logger logger = Logger
 			.getLogger("uk.ac.imperial.lpgdash.RuleEngine");
 	private StatefulKnowledgeSession session;
 
 	private Set<Player> players = new HashSet<Player>();
 	private LPGService game;
 
 	@Parameter(name = "cCount")
 	public int cCount;
 	@Parameter(name = "cPCheat")
 	public double cPCheat;
 
 	@Parameter(name = "ncCount")
 	public int ncCount;
 	@Parameter(name = "ncPCheat")
 	public double ncPCheat;
 
 	@Parameter(name = "clusters")
 	public String clusters;
 
 	@Parameter(name = "alpha")
 	public double alpha;
 	@Parameter(name = "beta")
 	public double beta;
 	@Parameter(name = "gamma", optional = true)
 	public double gamma = 0.1;
 	@Parameter(name = "seed")
 	public int seed;
 
 	@Parameter(name = "soHd", optional = true)
 	public boolean soHd = true;
 	@Parameter(name = "soHack", optional = true)
 	public boolean soHack = true;
 
 	@Parameter(name = "cheatOn", optional = true)
 	public String cheatOn = "provision";
 
 	@Parameter(name = "clusterLeave", optional = true)
 	public String clusterLeave = ClusterLeaveAlgorithm.THRESHOLD.name();
 	@Parameter(name = "clusterSelect", optional = true)
 	public String clusterSelect = ClusterSelectionAlgorithm.PREFERRED.name();
 	@Parameter(name = "resetSatisfaction", optional = true)
 	public boolean resetSatisfaction = false;
 
 	@Parameter(name = "rankMemory", optional = true)
 	public int rankMemory = 1;
 
 	public LPGGameSimulation(Set<AbstractModule> modules) {
 		super(modules);
 	}
 
 	@Inject
 	public void setSession(StatefulKnowledgeSession session) {
 		this.session = session;
 	}
 
 	@Inject
 	public void setServiceProvider(EnvironmentServiceProvider serviceProvider) {
 		try {
 			this.game = serviceProvider.getEnvironmentService(LPGService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn("", e);
 		}
 	}
 
 	@Override
 	protected Set<AbstractModule> getModules() {
 		Set<AbstractModule> modules = new HashSet<AbstractModule>();
 		modules.add(new AbstractEnvironmentModule()
 				.addActionHandler(LPGActionHandler.class)
 				.addParticipantGlobalEnvironmentService(LPGService.class)
 				.setStorage(RuleStorage.class));
 		modules.add(new RuleModule().addClasspathDrlFile("LPGDash.drl")
 				.addClasspathDrlFile("RationAllocation.drl")
 				.addClasspathDrlFile("RandomAllocation.drl")
 				.addClasspathDrlFile("LegitimateClaimsAllocation.drl")
 				.addClasspathDrlFile("QueueAllocation.drl")
 				.addStateTranslator(SimParticipantsTranslator.class));
 		modules.add(NetworkModule.noNetworkModule());
 		return modules;
 	}
 
 	@Override
 	protected void addToScenario(Scenario s) {
 		Random.seed = this.seed;
		s.addTimeDriven(this);
 		session.setGlobal("logger", this.logger);
 		session.setGlobal("session", session);
 		session.setGlobal("storage", this.storage);
 
 		Cluster[] clusterArr = initClusters();
 
 		for (int n = 0; n < cCount; n++) {
 			UUID pid = Random.randomUUID();
 			s.addParticipant(new LPGPlayer(pid, "c" + n, cPCheat, alpha, beta,
 					getCheatOn(), getClusterLeave(), getClusterSelect(),
 					resetSatisfaction));
 			Player p = new Player(pid, "c" + n, "C", alpha, beta);
 			players.add(p);
 			session.insert(p);
 			session.insert(new JoinCluster(p, clusterArr[n % clusterArr.length]));
 			session.insert(new Generate(p, game.getRoundNumber() + 1));
 		}
 		for (int n = 0; n < ncCount; n++) {
 			UUID pid = Random.randomUUID();
 			s.addParticipant(new LPGPlayer(pid, "nc" + n, ncPCheat, alpha,
 					beta, getCheatOn(), getClusterLeave(), getClusterSelect(),
 					resetSatisfaction));
 			Player p = new Player(pid, "nc" + n, "N", alpha, beta);
 			players.add(p);
 			session.insert(p);
 			session.insert(new JoinCluster(p, clusterArr[n % clusterArr.length]));
 			session.insert(new Generate(p, game.getRoundNumber() + 1));
 		}
 	}
 
 	protected Cluster[] initClusters() {
 		String[] clusterNames = StringUtils.split(this.clusters, ',');
 		Cluster[] clusters = new Cluster[clusterNames.length];
 		int clusterCtr = 0;
 		for (int i = 0; i < clusterNames.length; i++) {
 			Allocation method = null;
 			for (Allocation a : Allocation.values()) {
 				if (clusterNames[i].equalsIgnoreCase(a.name())) {
 					method = a;
 					break;
 				}
 			}
 			if (method == null)
 				throw new RuntimeException("Unknown allocation method '"
 						+ clusterNames[i] + "', could not create cluster!");
 			Cluster c = new Cluster(clusterCtr++, method);
 			session.insert(c);
 			if (c.isLC()) {
 				LegitimateClaims lc = new LegitimateClaims(c, session,
 						this.game);
 				lc.setStorage(storage);
 				lc.setGamma(gamma);
 				lc.enableHack = soHack;
 				lc.soHd = soHd;
 				lc.rankMemory = rankMemory;
 				session.insert(lc);
			} else if(c.getAllocationMethod() == Allocation.QUEUE) {
 				QueueAllocator q = new QueueAllocator(c, session);
 				session.insert(q);
 			}
 			clusters[i] = c;
 		}
 		return clusters;
 	}
 
 	protected Cheat getCheatOn() {
 		for (Cheat c : Cheat.values()) {
 			if (this.cheatOn.equalsIgnoreCase(c.name())) {
 				return c;
 			}
 		}
 		Cheat[] cs = Cheat.values();
 		Cheat c = cs[Random.randomInt(cs.length)];
 		logger.debug("Cheat on: " + c);
 		return c;
 	}
 
 	public ClusterLeaveAlgorithm getClusterLeave() {
 		return ClusterLeaveAlgorithm.valueOf(clusterLeave);
 	}
 
 	public ClusterSelectionAlgorithm getClusterSelect() {
 		return ClusterSelectionAlgorithm.valueOf(clusterSelect);
 	}
 
	@Override
	public void incrementTime() {
 		if (this.game.getRound() == RoundType.APPROPRIATE) {
 			// generate new g and q
 			for (Player p : players) {
 				session.insert(new Generate(p, game.getRoundNumber() + 1));
 			}
 		}
 		// analyse objects in working memory.
 		// analyseDroolsUsage();
 	}
 
 	@SuppressWarnings("unused")
 	private void analyseDroolsUsage() {
 		Map<Class<?>, Integer> typeCards = new HashMap<Class<?>, Integer>();
 		for (Object o : session.getObjects()) {
 			if (!typeCards.containsKey(o.getClass())) {
 				typeCards.put(o.getClass(), 0);
 			}
 			typeCards.put(o.getClass(), typeCards.get(o.getClass()) + 1);
 		}
 		logger.info("Drools memory:");
 		for (Map.Entry<Class<?>, Integer> entry : typeCards.entrySet()) {
 			logger.info(entry.getKey().getSimpleName() + " - "
 					+ entry.getValue());
 		}
 	}
 
 }
