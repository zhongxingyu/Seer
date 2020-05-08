 package com.myrontuttle.fin.trade.adapt;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.prefs.Preferences;
 
 import org.joda.time.DateTime;
 import org.joda.time.Minutes;
 
 import com.myrontuttle.fin.trade.adapt.eval.BasicEvaluator;
 import com.myrontuttle.fin.trade.adapt.eval.RandomEvaluator;
 import com.myrontuttle.fin.trade.adapt.express.BasicExpression;
 import com.myrontuttle.fin.trade.adapt.express.NoExpression;
 import com.myrontuttle.sci.evolve.*;
 import com.myrontuttle.sci.evolve.factories.IntArrayFactory;
 import com.myrontuttle.sci.evolve.operators.EvolutionPipeline;
 import com.myrontuttle.sci.evolve.operators.IntArrayCrossover;
 import com.myrontuttle.sci.evolve.operators.IntArrayMutation;
 import com.myrontuttle.sci.evolve.selection.RouletteWheelSelection;
 import com.myrontuttle.sci.evolve.termination.*;
 
 public class Evolver implements EvolveService {
 
 	private final static int NUM_THREADS = 1;
 	private final static int MINUTES_IN_DAY = 60 * 24;
 	final String EVOLVE_ACTIVE = "evolve_active";
 	final String EVOLVE_HOUR = "evolve_hour";
 	final String EVOLVE_MINUTE = "evolve_minute";
 	
 	private GroupDAO groupDAO;
 	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
 	
	public void setGroupDAO(GroupDAO groupDAO) {
 		this.groupDAO = groupDAO;
 	}
 	
 	private final TerminationCondition[] terminationConditions = new TerminationCondition[]{
 																		new UserAbort() };
 	private final EvolutionObserver<int[]> dbObserver = new EvolutionObserver<int[]>() {
 		public void populationUpdate(PopulationStats<? extends int[]> data) {
 			// Use data to update group
 			groupDAO.updateGroupStats(data);
 		}
 	};
 	
 	private ScheduledExecutorService ses;
 	private ScheduledFuture<?> sf;
 	
 	public Evolver() {
 		if (prefs.getBoolean(EVOLVE_ACTIVE, false)) {
 			evolveActiveAt(
 					new DateTime().
 					withHourOfDay(prefs.getInt(EVOLVE_HOUR, 0)).
 					withMinuteOfHour(prefs.getInt(EVOLVE_MINUTE, 0)));
 		}
 	}
 	
 	protected boolean wasMarketOpenToday() {
 		// Currently assumes market open on week days
 		return (new DateTime().getDayOfWeek() < 6);
 	}
 	
 	protected boolean isSaturday() {
 		return (new DateTime().getDayOfWeek() == 6);
 	}
 	
 	protected long minutesToTime(DateTime date) {		
 		return Minutes.minutesBetween(new DateTime(), date).getMinutes();
 	}
 
 	private EvolutionEngine<int[]> createEngine(int totalGeneLength, int geneUpperValueBound,
 											double mutationFactor,
 											ExpressionStrategy<int[]> candidateExpression, 
 											ExpressedFitnessEvaluator<int[]> candidateEvaluator){
 
 		// Setup how strategy candidates are created
 		IntArrayFactory candidateFactory = new IntArrayFactory(totalGeneLength, geneUpperValueBound);
 		
 		
 		// Setup how strategies are evolved
 		List<EvolutionaryOperator<int[]>> operators = new LinkedList<EvolutionaryOperator<int[]>>();
 		operators.add(new IntArrayCrossover());
 		operators.add(new IntArrayMutation(candidateFactory, mutationFactor));
 		EvolutionaryOperator<int[]> pipeline = new EvolutionPipeline<int[]>(operators);
 		SelectionStrategy<Object> selection = new RouletteWheelSelection();
 
 		// Strategy evolution engine
 		EvolutionEngine<int[]> engine = new GenerationalEvolutionEngine<int[]>(candidateFactory,
 		                                              pipeline,
 		                                              candidateEvaluator,
 		                                              candidateExpression,
 		                                              selection,
 		                                              RNG.getRNG(RNG.MARSENNETWISTER));
 		
 		// Add observer for the evolution
 		engine.addEvolutionObserver(dbObserver);
 		
 		return engine;
 	}
 	
 	/*
 	 * Evolve one group right now
 	 */
 	public void evolveNow(String groupId) {
 		
 		List<ExpressedCandidate<int[]>> candidates = new ArrayList<ExpressedCandidate<int[]>>(
 											groupDAO.findCandidatesInGroup(groupId));
 		
 		Group group = groupDAO.findGroup(groupId);
 		
 		int size = group.getSize();
 		int eliteCount = group.getEliteCount();
 
 		ExpressionStrategy<int[]> expressionStrategy = null;
 		if (group.getExpressionStrategy().equals(Group.BASIC_EXPRESSION)) {
 			expressionStrategy = new BasicExpression<int[]>();
 		} else {
 			expressionStrategy = new NoExpression();
 		}
 		
 		int genomeLength = expressionStrategy.getGenomeLength(groupId);
 		
 		ExpressedFitnessEvaluator<int[]> evaluator = null;
 		if (group.getEvaluationStrategy().equals(Group.BASIC_EVALUATOR)) {
 			evaluator = new BasicEvaluator();
 		} else {
 			evaluator = new RandomEvaluator();
 		}
 		
 		EvolutionEngine<int[]> engine = createEngine(genomeLength, 
 														group.getGeneUpperValue(), 
 														group.getMutationFactor(),
 														expressionStrategy, evaluator);
 		
 		if (evaluator.isNatural()) {
 			System.out.println("Evaluation is natural");
 		}
 		
 		engine.evolveToExpression(candidates, groupId, size, eliteCount, 
 				terminationConditions);
 	}
 	
 	/*
 	 * Evolve all groups in database now
 	 */
 	public void evolveAllNow() {
 		List<Group> groups = groupDAO.findGroups();
 		for (Group group : groups) {
 			evolveNow(group.getGroupId());
 		}
 	}
 
 	/*
 	 * Starts evolving all active groups at a specific hour of the day (0 to 23)
 	 */
 	public void evolveActiveAt(DateTime date) {
 		if (date.isBeforeNow()) {
 			System.out.println("Date to evolve is before now.  Setting to evolve at same time tomorrow");
 			date = new DateTime().
 						plusDays(1).
 						withHourOfDay(date.getHourOfDay()).
 						withMinuteOfHour(date.getMinuteOfHour());
 		}
 
         this.ses = Executors.newScheduledThreadPool(NUM_THREADS);
         
 		this.sf = ses.scheduleAtFixedRate(new Runnable () {
 			@Override
 			public void run() {
 				try {
 					List<Group> groups = groupDAO.findGroups();
 					for (Group group : groups) {
 						if (group.isActive()) {
 							if ((group.getFrequency().equals(Group.DAILY) && wasMarketOpenToday()) ||
 								((group.getFrequency().equals(Group.WEEKLY) && isSaturday()))) {
 								evolveNow(group.getGroupId());
 							}
 						}
 					}
 				} catch (Exception e) {
 					System.out.println("Unable to evolve all groups. " + e.getMessage());
 				}
 			}
 		}, minutesToTime(date), MINUTES_IN_DAY, TimeUnit.MINUTES);
 
 		prefs.putInt(EVOLVE_HOUR, date.getHourOfDay());
 		prefs.putInt(EVOLVE_MINUTE, date.getMinuteOfHour());
 		prefs.putBoolean(EVOLVE_ACTIVE, true);
 	}
 	
 	public DateTime getNextEvolveDate() {
 		if (sf == null || sf.isCancelled()) {
 			return null;
 		}
 		return new DateTime().plusSeconds((int) sf.getDelay(TimeUnit.SECONDS));
 	}
 	
 	public boolean stopEvolving() {
 		try {
 			// Stop any existing evolution
 			((UserAbort)terminationConditions[0]).abort();
 			
 	        // Close scheduled service
 	        sf.cancel(true);
 	        ses.shutdown();
 		} catch (SecurityException se) {
 			System.out.println("Unable to stop evolving. " + se.getMessage());
 			return false;
 		}
 		prefs.putBoolean(EVOLVE_ACTIVE, false);
 		return true;
 	}
 }
