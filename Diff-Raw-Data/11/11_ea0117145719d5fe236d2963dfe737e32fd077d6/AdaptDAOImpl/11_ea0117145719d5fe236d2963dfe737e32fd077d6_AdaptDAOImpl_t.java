 package com.myrontuttle.fin.trade.adapt;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import com.myrontuttle.sci.evolve.api.PopulationStats;
 
 public class AdaptDAOImpl implements AdaptDAO {
 
	private static final Logger logger = LoggerFactory.getLogger(AdaptDAOImpl.class);

 	private EntityManager em;
 
 	public void setEntityManager(EntityManager em) {
 		this.em = em;
 	}
 
 	public void saveGroup(Group group) {
 		em.persist(group);
 	}
 	
 	// Get the group based on a groupId
 	public Group findGroup(long groupId) {
 		return em.find(Group.class, groupId);
 	}
 
 	// Retrieve all groups
 	public List<Group> findGroups() {
 		return em.createQuery(
 				"SELECT g FROM GROUPS g", Group.class).getResultList();
 	}
 	
 	public Group updateGroup(Group group) {
     	group.setLong("Evolve.UpdatedTime", System.currentTimeMillis());
 		return em.merge(group);
 	}
 	
 	public void removeGroup(long groupId) {
 		em.remove(em.find(Group.class, groupId));
 	}
 	
 	public void addCandidate(Candidate candidate, long groupId) {
 		Group group = em.find(Group.class, groupId);
 		group.addCandidate(candidate);
 	}
 
 	// Retrieve a candidate based on it's ID
 	public Candidate findCandidate(long candidateId) {
 		return em.find(Candidate.class, candidateId);
 	}
 
 	// Retrieve candidates from database
 	public List<Candidate> findCandidatesInGroup(long groupId) {
 		return em.createQuery(
 				"SELECT c FROM CANDIDATES c WHERE c.groupId = :groupId", 
 				Candidate.class).setParameter("groupId", groupId).getResultList();
 	}
 	
 	public Candidate findCandidateByGenome(int[] genome) throws Exception {
 		List<Candidate> candidates = em.createQuery(
 				"SELECT c FROM CANDIDATES c WHERE c.genomeString = :genomeString", 
 					Candidate.class).
 				setParameter("genomeString", Candidate.generateGenomeString(genome)).
 				getResultList();
 		if (candidates.size() > 0) {
 			Candidate c = candidates.get(0);
 			c.setGenome(genome);
 			return c;
 		} else {
 			throw new Exception("No candidate found with genome: " + Arrays.toString(genome));
 		}
 	}
 	
 	public Candidate updateCandidate(Candidate candidate) {
 		return em.merge(candidate);
 	}
 	
 	public void removeCandidate(long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		Group group = em.find(Group.class, candidate.getGroupId());
 		group.removeCandidate(candidate);
 		em.remove(candidate);
 	}
 
 	public void removeAllCandidates(long groupId) {
 		Group group = em.find(Group.class, groupId);
 		for (Candidate c : group.getCandidates()) {
 			em.remove(c);
 		}
 		group.getCandidates().clear();
 	}
 	
 	public void updateGroupStats(PopulationStats<? extends int[]> data) {
 
 		Group group = em.find(Group.class, data.getPopulationId());
 		
 		GroupStats stats = new GroupStats(
 				data.getPopulationId(), 
 				data.getBestCandidateFitness(), data.getMeanFitness(), 
 				data.getFitnessStandardDeviation(), data.getGenerationNumber(),
 				group.getDouble("Express.Variability"));
 
 		group.addGroupStats(stats);
 	}
 
 	// Retrieve group stats from database
 	public List<GroupStats> findStatsForGroup(long groupId) {
 		return em.createQuery(
 				"SELECT s FROM GROUP_STATS s WHERE s.groupId = :groupId", 
 				GroupStats.class).setParameter("groupId", groupId).getResultList();
 	}
 
 	// Get the groupstats based on a statsId
 	public GroupStats findStats(long statsId) {
 		return em.find(GroupStats.class, statsId);
 	}
 	
 	public void removeStats(long statsId) {
 		GroupStats stats = em.find(GroupStats.class, statsId);
 		Group group = em.find(Group.class, stats.getGroupId());
 		group.removeStats(stats);
 		em.remove(stats);
 	}
 
 	@Override
 	public void removeAllStats(long groupId) {
 		Group group = em.find(Group.class, groupId);
 		for (GroupStats gs : group.getStats()) {
 			em.remove(gs);
 		}
 		group.getStats().clear();
 	}
 	
 	@Override
 	public void setBestCandidate(long candidateId, long groupId) {
 		Group group = em.find(Group.class, groupId);		
 		group.setBestCandidateId(candidateId);		
 	}
 
 	@Override
 	public Candidate getBestCandidate(long groupId) {
 		Group group = em.find(Group.class, groupId);
 		try {
 			return em.createQuery(
 					"SELECT c FROM CANDIDATES c WHERE c.candidateId = :bestId", 
 					Candidate.class).setParameter("bestId", group.getBestCandidateId()).getSingleResult();
		} catch (Exception nre) {
			logger.warn("Can't get best candidate for group: {}", groupId, nre);
 			return null;
 		}
 	}
 
 	@Override
 	public void addSavedScreen(SavedScreen screen, long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		candidate.addScreen(screen);
 	}
 
 	@Override
 	public void addSymbol(String symbol, long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		candidate.addSymbol(symbol);
 	}
 
 	@Override
 	public void addSavedAlert(SavedAlert alert, long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		candidate.addAlert(alert);
 	}
 
 	@Override
 	public void addTradeParameter(TradeParameter p,
 			long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		candidate.addTradeParameter(p);
 	}
 
 	@Override
 	public List<SavedScreen> findScreensForCandidate(long candidateId) {
 		return em.createQuery(
 				"SELECT s FROM SAVED_SCREENS s WHERE s.candidateId = :candidateId", 
 				SavedScreen.class).setParameter("candidateId", candidateId).getResultList();
 	}
 
 	@Override
 	public List<String> findSymbolsForCandidate(long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		return candidate.getSymbols();
 	}
 
 	@Override
 	public List<SavedAlert> findAlertsForCandidate(long candidateId) {
 		return em.createQuery(
 				"SELECT a FROM SAVED_ALERTS a WHERE a.candidateId = :candidateId", 
 				SavedAlert.class).setParameter("candidateId", candidateId).getResultList();
 	}
 
 	@Override
 	public List<TradeParameter> findParametersForCandidate(long candidateId) {
 		return em.createQuery(
 				"SELECT t FROM TRADE_PARAMETERS t WHERE t.candidateId = :candidateId", 
 				TradeParameter.class).setParameter("candidateId", candidateId).getResultList();
 	}
 
 	@Override
 	public SavedScreen findScreen(long savedScreenId) {
 		return em.find(SavedScreen.class, savedScreenId);
 	}
 
 	@Override
 	public SavedAlert findAlert(long savedAlertId) {
 		return em.find(SavedAlert.class, savedAlertId);
 	}
 
 	@Override
 	public TradeParameter findTradeParameter(long tradeParameterId) {
 		return em.find(TradeParameter.class, tradeParameterId);
 	}
 
 	@Override
 	public void removeSavedScreens(long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		for (SavedScreen ss : candidate.getSavedScreens()) {
 			em.remove(ss);
 		}
 		candidate.getSavedScreens().clear();
 	}
 
 	@Override
 	public void removeSymbols(long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		for (String symbol : candidate.getSymbols()) {
 			em.remove(symbol);
 		}
 		candidate.getSymbols().clear();
 	}
 
 	@Override
 	public void removeSavedAlerts(long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		for (SavedAlert sa : candidate.getSavedAlerts()) {
 			em.remove(sa);
 		}
 		candidate.getSavedAlerts().clear();
 	}
 
 	@Override
 	public void removeTradeParameters(long candidateId) {
 		Candidate candidate = em.find(Candidate.class, candidateId);
 		for (TradeParameter tp : candidate.getTradeParameters()) {
 			em.remove(tp);
 		}
 		candidate.getTradeParameters().clear();
 	}
 }
