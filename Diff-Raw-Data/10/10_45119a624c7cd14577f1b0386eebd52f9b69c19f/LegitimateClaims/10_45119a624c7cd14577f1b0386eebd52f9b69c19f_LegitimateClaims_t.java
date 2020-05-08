 package uk.ac.imperial.lpgdash.allocators;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.QueryResults;
 import org.drools.runtime.rule.Variable;
 
 import uk.ac.imperial.lpgdash.LPGService;
 import uk.ac.imperial.lpgdash.actions.Allocate;
 import uk.ac.imperial.lpgdash.allocators.canons.Canon;
 import uk.ac.imperial.lpgdash.allocators.canons.F1;
 import uk.ac.imperial.lpgdash.allocators.canons.F1a;
 import uk.ac.imperial.lpgdash.allocators.canons.F2;
 import uk.ac.imperial.lpgdash.allocators.canons.F3;
 import uk.ac.imperial.lpgdash.allocators.canons.F4;
 import uk.ac.imperial.lpgdash.allocators.canons.F5;
 import uk.ac.imperial.lpgdash.allocators.canons.F6;
 import uk.ac.imperial.lpgdash.allocators.canons.F7;
 import uk.ac.imperial.lpgdash.allocators.canons.LegitimateClaimsCanon;
 import uk.ac.imperial.lpgdash.facts.Allocation;
 import uk.ac.imperial.lpgdash.facts.BordaRank;
 import uk.ac.imperial.lpgdash.facts.Cluster;
 import uk.ac.imperial.lpgdash.facts.Player;
 import uk.ac.imperial.lpgdash.facts.PlayerHistory;
 import uk.ac.imperial.presage2.core.db.StorageService;
 import uk.ac.imperial.presage2.core.db.persistent.PersistentEnvironment;
 import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
 
 public class LegitimateClaims {
 
 	private final Logger logger = Logger.getLogger(LegitimateClaims.class);
 
 	private final Cluster c;
 	private final double[] weights;
 	private double gamma = 0.1;
 
 	public boolean ratelimit = false;
 	private boolean soHd = true;
 
 	private final StatefulKnowledgeSession session;
 	private final LPGService game;
 	private StorageService sto = null;
 
 	public LegitimateClaims(Cluster c, StatefulKnowledgeSession session,
 			LPGService game) {
 		super();
 		this.c = c;
 		this.session = session;
 		switch (c.getAllocationMethod()) {
 		case LC_F1:
 			weights = new double[] { 1, 0, 0, 0, 0, 0, 0, 0 };
 			break;
 		case LC_F1a:
 			weights = new double[] { 0, 1, 0, 0, 0, 0, 0, 0 };
 			break;
 		case LC_F2:
 			weights = new double[] { 0, 0, 1, 0, 0, 0, 0, 0 };
 			break;
 		case LC_F3:
 			weights = new double[] { 0, 0, 0, 1, 0, 0, 0, 0 };
 			break;
 		case LC_F4:
 			weights = new double[] { 0, 0, 0, 0, 1, 0, 0, 0 };
 			break;
 		case LC_F5:
 			weights = new double[] { 0, 0, 0, 0, 0, 1, 0, 0 };
 			break;
 		case LC_F6:
 			weights = new double[] { 0, 0, 0, 0, 0, 0, 1, 0 };
 			break;
 		case LC_F7:
 			weights = new double[] { 0, 0, 0, 0, 0, 0, 0, 1 };
 			break;
 		case LC_SO:
 		case LC_FIXED:
 		default:
 			weights = new double[] { 0.125, 0.125, 0.125, 0.125, 0.125, 0.125,
 					0.125, 0.125 };
 			break;
 		}
 		this.game = game;
 	}
 
 	public void setStorage(StorageService sto) {
 		this.sto = sto;
 	}
 
 	public void setGamma(double gamma) {
 		this.gamma = gamma;
 	}
 
 	public Cluster getC() {
 		return c;
 	}
 
 	@Override
 	public String toString() {
 		return "LegitimateClaims [c=" + c + ", weights="
 				+ Arrays.toString(weights) + "]";
 	}
 
 	private List<Player> getFunctionRanking(Canon f, List<Player> players) {
 		List<Player> rankedPlayers = new ArrayList<Player>(players);
 		Collections.shuffle(rankedPlayers);
 		LegitimateClaimsCanon canon;
 		switch (f) {
 		case F1:
 			canon = new F1(c);
 			break;
 		case F1a:
 			canon = new F1a(c);
 			break;
 		case F2:
 			canon = new F2(c);
 			break;
 		case F3:
 			canon = new F3(c);
 			break;
 		case F4:
 			canon = new F4(c);
 			break;
 		case F5:
 			canon = new F5(c);
 			break;
 		case F6:
 			canon = new F6(c);
 			break;
 		case F7:
 		default:
 			canon = new F7(c);
 			break;
 		}
 		Collections.sort(rankedPlayers, canon);
 		return rankedPlayers;
 	}
 
 	public void allocate(List<Player> players, double poolSize, int t) {
 
 		Map<Canon, List<Player>> rankOrders = new HashMap<Canon, List<Player>>();
 		players = new ArrayList<Player>(players);
 		for (Canon f : Canon.values()) {
 			rankOrders.put(f, getFunctionRanking(f, players));
 		}
 
 		Map<UUID, BordaRank> ranks = new HashMap<UUID, BordaRank>();
 		for (Player p : players) {
 			ranks.put(p.getId(), new BordaRank(p));
 			storeHistory(p.getId(), p.getHistory().get(c));
 		}
 		ArrayList<BordaRank> bordaPtq = new ArrayList<BordaRank>(ranks.values());
 
 		// associate ranks with agents
 		for (int i = 0; i < players.size(); i++) {
 			for (Map.Entry<Canon, List<Player>> entry : rankOrders.entrySet()) {
 				ranks.get(entry.getValue().get(i).getId()).set(entry.getKey(),
 						i);
 			}
 		}
 
 		final int nPlayers = players.size();
 
 		// sort BordaRanks by borda score DESC.
 		sortBordaRanks(bordaPtq);
 
 		for (BordaRank p : bordaPtq) {
 			double allocation = Math.min(p.getPlayer().getD(), poolSize);
 			session.insert(new Allocate(p.getPlayer(), allocation, t));
 			poolSize -= allocation;
 			logger.info(p + ": " + getScore(p, nPlayers));
 			storeRanks(p, nPlayers);
 		}
 
 		if (c.getAllocationMethod() == Allocation.LC_SO) {
 			updateWeights(bordaPtq, rankOrders);
 		}
 	}
 
 	void sortBordaRanks(List<BordaRank> bordaPtq) {
 		final int nPlayers = bordaPtq.size();
 		Collections.sort(bordaPtq, new Comparator<BordaRank>() {
 			@Override
 			public int compare(BordaRank o1, BordaRank o2) {
 				double p1score = getScore(o1, nPlayers);
 				double p2score = getScore(o2, nPlayers);
 				return Double.compare(p2score, p1score);
 			}
 		});
 	}
 
 	private double getScore(BordaRank r, int nPlayers) {
 		double score = 0;
 		Canon[] canons = Canon.values();
 		for (int i = 0; i < weights.length; i++) {
 			score += weights[i] * (nPlayers - r.get(canons[i]));
 		}
 		return score;
 	}
 
 	private void updateWeights(List<BordaRank> bordaPtq,
 			Map<Canon, List<Player>> canonRankings) {
 		double[] fBorda = fBorda(bordaPtq);
 
 		logger.info("Borda(f, C) = " + Arrays.toString(fBorda));
 
 		// update weights
 		double averageBorda = 0;
 		double totalBorda = 0;
 		for (int i = 0; i < fBorda.length; i++) {
 			totalBorda += fBorda[i];
 		}
 		averageBorda = totalBorda / fBorda.length;
 		for (int i = 0; i < fBorda.length; i++) {
 			weights[i] = weights[i]
 					+ (weights[i] * (fBorda[i] - averageBorda) / totalBorda);
 		}
 
 		normaliseWeights();
 		logger.info("w*(t) = " + Arrays.toString(weights));
 
 		if (soHd) {
 			int[] fHd = new int[8];
 			for (Canon f : Canon.values()) {
 				fHd[f.ordinal()] = hdFBorda(canonRankings.get(f), bordaPtq);
 			}
 			double averageHd = 0;
 			int totalHd = 0;
 			for (int i = 0; i < fHd.length; i++) {
 				totalHd += fHd[i];
 			}
 			averageHd = totalHd / (double) fHd.length;
 			for (int i = 0; i < fHd.length; i++) {
				double delta = 0;
				if (totalHd > 0)
					delta = weights[i] * (fHd[i] - averageHd) / totalHd;
 				if (ratelimit) {
 					if (delta > 0.0007)
 						delta = 0.0007;
 					else if (delta < -0.0007)
 						delta = -0.0007;
 				}
 				weights[i] = weights[i] + delta;
 				if (weights[i] < 0 || weights[i] > 1) {
 					logger.info("This shouldn't happen!");
 				}
 			}
 			normaliseWeights();
 			logger.info("w*(t) = " + Arrays.toString(weights));
 		}
 
 		if (bordaPtq.size() == getCompliantCount()) {
 			final double equalWeight = 1 / (double) weights.length;
 			for (int i = 0; i < weights.length; i++) {
 				if (weights[i] > equalWeight) {
 					weights[i] = weights[i]
 							- (gamma * (weights[i] - equalWeight));
 				} else {
 					weights[i] = weights[i]
 							+ (gamma * (equalWeight - weights[i]));
 				}
 			}
 
 			normaliseWeights();
 			logger.info("w*(t+1) = " + Arrays.toString(weights));
 		}
 		storeWeights();
 	}
 
 	private void normaliseWeights() {
 		double weightsSum = 0.0;
 		for (int i = 0; i < weights.length; i++) {
 			weightsSum += weights[i];
 		}
 		for (int i = 0; i < weights.length; i++) {
 			weights[i] *= 1 / weightsSum;
 		}
 	}
 
 	double[] fBorda(List<BordaRank> bordaPtq) {
 		double[] fBorda = new double[8];
 		Arrays.fill(fBorda, 0.0);
 
 		// calculate Borda(f, C)
 		for (BordaRank p : bordaPtq) {
 			List<FunctionRank> playerRanks = new ArrayList<LegitimateClaims.FunctionRank>();
 			for (Canon f : Canon.values()) {
 				playerRanks.add(new FunctionRank(f, p.get(f)));
 			}
 
 			Collections.sort(playerRanks);
 
 			double bordaAvailable = 0;
 			int lastIndex = 0;
 			int lastRank = 0;
 			int score = Canon.values().length;
 			for (int i = 0; i <= playerRanks.size(); i++) {
 				FunctionRank f;
 				if (i == playerRanks.size())
 					// stub functionrank for last iteration
 					f = new FunctionRank(null, -1);
 				else
 					f = playerRanks.get(i);
 
 				if (f.rank != lastRank) {
 					// shared score between fns
 					int fnCount = i - lastIndex;
 					double bordaPerFn = ((double) bordaAvailable) / fnCount;
 					for (int j = lastIndex; j < i; j++) {
 						FunctionRank fAdd = playerRanks.get(j);
 						fBorda[fAdd.f.ordinal()] += bordaPerFn;
 					}
 
 					bordaAvailable = score;
 					lastIndex = i;
 					lastRank = f.rank;
 				} else {
 					bordaAvailable += score;
 				}
 				score--;
 			}
 
 		}
 		return fBorda;
 	}
 
 	private int hdFBorda(List<Player> functionRanking, List<BordaRank> bordaPtq) {
 		int hd = 0;
 		for (int i = 0; i < functionRanking.size(); i++) {
 			BordaRank r = bordaPtq.get(i);
 			Player p = functionRanking.get(i);
 			if (!r.getPlayer().getId().equals(p.getId())) {
 				hd++;
 			}
 		}
 		return hd;
 	}
 
 	private void storeHistory(UUID id, PlayerHistory playerHistory) {
 		if (sto != null) {
 			TransientAgentState s = sto
 					.getAgentState(id, game.getRoundNumber());
 			s.setProperty("averageAllocated",
 					Double.toString(playerHistory.getAverageAllocated()));
 			s.setProperty("averageDemanded",
 					Double.toString(playerHistory.getAverageDemanded()));
 			s.setProperty("averageProvided",
 					Double.toString(playerHistory.getAverageProvided()));
 			s.setProperty("compliantRounds",
 					Integer.toString(playerHistory.getCompliantRounds()));
 			s.setProperty("roundsAllocated",
 					Integer.toString(playerHistory.getRoundsAllocated()));
 			s.setProperty("roundsAsHead",
 					Integer.toString(playerHistory.getRoundsAsHead()));
 			s.setProperty("roundsParticipanted",
 					Integer.toString(playerHistory.getRoundsParticipated()));
 		}
 	}
 
 	private void storeRanks(BordaRank r, int n) {
 		if (sto != null) {
 			TransientAgentState s = sto.getAgentState(r.getPlayer().getId(),
 					game.getRoundNumber());
 			s.setProperty("f1",
 					Double.toString(weights[0] * (n - r.get(Canon.F1))));
 			s.setProperty("f1a",
 					Double.toString(weights[1] * (n - r.get(Canon.F1a))));
 			s.setProperty("f2",
 					Double.toString(weights[2] * (n - r.get(Canon.F2))));
 			s.setProperty("f3",
 					Double.toString(weights[3] * (n - r.get(Canon.F3))));
 			s.setProperty("f4",
 					Double.toString(weights[4] * (n - r.get(Canon.F4))));
 			s.setProperty("f5",
 					Double.toString(weights[5] * (n - r.get(Canon.F5))));
 			s.setProperty("f6",
 					Double.toString(weights[6] * (n - r.get(Canon.F6))));
 			s.setProperty("f7",
 					Double.toString(weights[7] * (n - r.get(Canon.F7))));
 		}
 	}
 
 	private void storeWeights() {
 		if (sto != null) {
 			PersistentEnvironment e = sto.getSimulation().getEnvironment();
 			int round = game.getRoundNumber();
 			for (Canon f : Canon.values()) {
 				e.setProperty("w_" + f, round,
 						Double.toString(weights[f.ordinal()]));
 			}
 		}
 	}
 
 	private static class FunctionRank implements Comparable<FunctionRank> {
 		Canon f;
 		int rank;
 
 		FunctionRank(Canon f, int rank) {
 			super();
 			this.f = f;
 			this.rank = rank;
 		}
 
 		@Override
 		public int compareTo(FunctionRank o) {
 			return rank - o.rank;
 		}
 
 		@Override
 		public String toString() {
 			return "FunctionRank [f=" + f + ", rank=" + rank + "]";
 		}
 
 	}
 
 	private int getCompliantCount() {
 		QueryResults results = session.getQueryResults("compliantRound",
				new Object[] { this.c, Variable.v, game.getRoundNumber() - 1 });
		int size = results.size();
		return size;
 	}
 
 }
