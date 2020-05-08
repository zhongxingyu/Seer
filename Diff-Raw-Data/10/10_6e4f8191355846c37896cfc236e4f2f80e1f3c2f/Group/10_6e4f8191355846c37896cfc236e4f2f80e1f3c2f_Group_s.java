 package com.myrontuttle.fin.trade.adapt;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import javax.persistence.*;
 
 /**
  * A group of trade strategy candidates which are generated with common settings
  * @author Myron Tuttle
  */
 @Entity(name = "Groups")
 public class Group implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	public static final String DAILY = "DAILY";
 	public static final String WEEKLY = "WEEKLY";
 
 	public static final String RANDOM_EVALUATOR = "RandomEvaluator";
 	public static final String BASIC_EVALUATOR = "BasicEvaluator";
 	public static final String NO_EXPRESSION = "NoExpression";
 	public static final String BASIC_EXPRESSION = "BasicExpression";
 
 	@Id
 	@Column(name = "GroupId", nullable = false)
     @GeneratedValue(strategy = GenerationType.AUTO)
 	private String groupId;
 	
 	@OneToMany(mappedBy = "group", targetEntity = Candidate.class,
 			fetch = FetchType.EAGER, cascade = CascadeType.ALL)
 	private Collection<Candidate> candidates;
 	
 	@OneToOne(mappedBy = "group", targetEntity = Trader.class,
 			optional = true, cascade = CascadeType.ALL)
 	private Trader bestTrader;
 	
 	@Column(name = "AlertReceiver")
 	private String alertReceiver;
 	
	@Column(name = "AlertAddress")
 	private String alertUser;
 	
 	@Column(name = "AlertHost")
 	private String alertHost;
 	
 	@Column(name = "AlertPassword")
 	private String alertPassword;
 
 	@Column(name = "Size")
 	private int size;
 	
 	@Column(name = "EliteCount")
 	private int eliteCount;
 	
 	@Column(name = "GeneUpperValue")
 	private int geneUpperValue;
 	
 	@Column(name = "ExpressionStrategy")
 	private String expressionStrategy;
 	
 	@Column(name = "EvaluationStrategy")
 	private String evaluationStrategy;
 	
 	@Column(name = "TradeStrategy")
 	private String tradeStrategy;
 	
 	@Column(name = "AllowShorting")
 	private boolean allowShorting;
 	
 	@Column(name = "StartTime")
 	private Date startTime;
 	
 	@Column(name = "Frequency")
 	private String frequency;
 	
 	@Column(name = "MutationFactor")
 	private double mutationFactor;
 	
 	@Column(name = "NumberOfScreens")
 	private int numberOfScreens;
 	
 	@Column(name = "MaxSymbolsPerScreen")
 	private int maxSymbolsPerScreen;
 	
 	@Column(name = "AlertsPerSymbol")
 	private int alertsPerSymbol;
 	
 	@Column(name = "StartingCash")
 	private double startingCash;
 	
 	@Column(name = "Generation")
 	private int generation;
 	
 	@Column(name = "Active")
 	private boolean active;
 	
 	@Column(name = "Variability")
 	private double variability;
 	
 	// Group Stats
 	@OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
 	private ArrayList<GroupStats> stats;
 	
 	@Version
     @Column(name = "UpdatedTime")
     private Date updatedTime;
 	
 	public Group() {}
 	
 	public void addCandidate(Candidate c) {
 		this.candidates.add(c);
 		if (c.getGroup() != this) {
 			c.setGroup(this);
 		}
 	}
 	
 	public void removeCandidate(Candidate c) {
 		if (candidates.contains(c)) {
 			candidates.remove(c);
 			c.setGroup(null);
 		}
 	}
 	
 	public Trader getBestTrader() {
 		return bestTrader;
 	}
 
 	public void setBestTrader(Trader bestTrader) {
 		this.bestTrader = bestTrader;
 	}
 	
 	public void removeBestTrader(Trader trader) {
 		this.bestTrader = null;
 		trader.setGroup(null);
 	}
 
 	public void addGroupStats(GroupStats gs) {
 		this.stats.add(gs);
 		if (gs.getGroup() != this) {
 			gs.setGroup(this);
 		}
 	}
 	
 	public void removeStats(GroupStats gs) {
 		if (stats.contains(gs)) {
 			stats.remove(gs);
 			gs.setGroup(null);
 		}
 	}
 
 	public String getGroupId() {
 		return groupId;
 	}
 
 	public void setGroupId(String groupId) {
 		this.groupId = groupId;
 	}
 
 	public Collection<Candidate> getCandidates() {
 		return candidates;
 	}
 
 	public void setCandidates(Collection<Candidate> candidates) {
 		this.candidates = candidates;
 	}
 
 	public String getAlertReceiver() {
 		return alertReceiver;
 	}
 
 	public void setAlertReceiver(String alertReceiver) {
 		this.alertReceiver = alertReceiver;
 	}
 
 	public String getAlertUser() {
 		return alertUser;
 	}
 
 	public void setAlertUser(String alertUser) {
 		this.alertUser = alertUser;
 	}
 
 	public String getAlertHost() {
 		return alertHost;
 	}
 
 	public void setAlertHost(String alertHost) {
 		this.alertHost = alertHost;
 	}
 
 	public String getAlertPassword() {
 		return alertPassword;
 	}
 
 	public void setAlertPassword(String alertPassword) {
 		this.alertPassword = alertPassword;
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 	public int getEliteCount() {
 		return eliteCount;
 	}
 
 	public void setEliteCount(int eliteCount) {
 		this.eliteCount = eliteCount;
 	}
 
 	public int getGeneUpperValue() {
 		return geneUpperValue;
 	}
 
 	public void setGeneUpperValue(int geneUpperValue) {
 		this.geneUpperValue = geneUpperValue;
 	}
 
 	public String getExpressionStrategy() {
 		return expressionStrategy;
 	}
 
 	public void setExpressionStrategy(String expressionStrategy) {
 		this.expressionStrategy = expressionStrategy;
 	}
 
 	public String getEvaluationStrategy() {
 		return evaluationStrategy;
 	}
 
 	public void setEvaluationStrategy(String evaluationStrategy) {
 		this.evaluationStrategy = evaluationStrategy;
 	}
 
 	public String getTradeStrategy() {
 		return tradeStrategy;
 	}
 
 	public void setTradeStrategy(String tradeStrategy) {
 		this.tradeStrategy = tradeStrategy;
 	}
 
 	public boolean isAllowShorting() {
 		return allowShorting;
 	}
 
 	public void setAllowShorting(boolean allowShorting) {
 		this.allowShorting = allowShorting;
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public String getFrequency() {
 		return frequency;
 	}
 
 	public void setFrequency(String frequency) {
 		this.frequency = frequency;
 	}
 
 	public double getMutationFactor() {
 		return mutationFactor;
 	}
 
 	public void setMutationFactor(double mutationFactor) {
 		this.mutationFactor = mutationFactor;
 	}
 
 	public int getNumberOfScreens() {
 		return numberOfScreens;
 	}
 
 	public void setNumberOfScreens(int numberOfScreens) {
 		this.numberOfScreens = numberOfScreens;
 	}
 
 	public int getMaxSymbolsPerScreen() {
 		return maxSymbolsPerScreen;
 	}
 
 	public void setMaxSymbolsPerScreen(int maxSymbolsPerScreen) {
 		this.maxSymbolsPerScreen = maxSymbolsPerScreen;
 	}
 
 	public int getAlertsPerSymbol() {
 		return alertsPerSymbol;
 	}
 
 	public void setAlertsPerSymbol(int alertsPerSymbol) {
 		this.alertsPerSymbol = alertsPerSymbol;
 	}
 
 	public double getStartingCash() {
 		return startingCash;
 	}
 
 	public void setStartingCash(double startingCash) {
 		this.startingCash = startingCash;
 	}
 
 	public int getGeneration() {
 		return generation;
 	}
 
 	public void setGeneration(int generation) {
 		this.generation = generation;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public double getVariability() {
 		return variability;
 	}
 
 	public void setVariability(double variability) {
 		this.variability = variability;
 	}
 
 	public ArrayList<GroupStats> getStats() {
 		return stats;
 	}
 
 	public void setStats(ArrayList<GroupStats> stats) {
 		this.stats = stats;
 	}
 
 	public Date getUpdatedTime() {
 		return updatedTime;
 	}
 
 	public void setUpdatedTime(Date updatedTime) {
 		this.updatedTime = updatedTime;
 	}
 
 }
