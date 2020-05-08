 package org.mmadsen.sim.transmission.analysis;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Closure;
 import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
 import org.mmadsen.sim.transmission.interfaces.IAgent;
 import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;
 import org.mmadsen.sim.transmission.interfaces.IDataCollector;
 import org.mmadsen.sim.transmission.models.TransmissionLabModel;
 
 import uchicago.src.sim.analysis.AverageSequence;
 import uchicago.src.sim.analysis.OpenSequenceGraph;
 import uchicago.src.sim.analysis.Sequence;
 import uchicago.src.sim.engine.ActionUtilities;
 import uchicago.src.sim.engine.BasicAction;
 import uchicago.src.sim.engine.Schedule;
 
 
 /**
  * TraitFrequencyAnalyzer is an IDataCollector module for analyzing the "top 40" properties
  * of a set of RCMAgents which store cultural variants.  Currently the assumption is that
  * each agent only possesses one variant but this will be generalized in future releases.
  * 
  * The general idea here is that we're going to make a single pass through the list of 
  * agents, either creating a new TraitCount value object if we see a new trait (with count = 1),
  * or incrementing an existing one.  This is facilitated by temporarily storing the TraitCounts
  * in a TreeMap indexed by trait ID.  The TraitCount value object not only stores the count
  * and trait ID together for easy access to either (an improvement on a raw Map) but allows
  * us to provide a custom sort order based on frequency, not trait number or object ID.  Thus
  * we implement Comparable and provide a compareTo() implementation that sorts by trait count.
  * 
  * The counting pass is facilitated by creating a Closure (from Commons Collections) that 
  * does the actual TraitCount manipulation.  This closure is then passed to 
  * CollectionUtils.forAlldo() over the agent list.  
  * 
  * Once the counting pass is completed, we initialize a List<TraitCount> from the TreeMap,
  * and since TraitCount implements Comparable, Collections.sort() provides us the 
  * list sorted by trait count in descending order (what we want if we're reading off "top N"
  * statistics).
  * 
  * Finally, "turnover" in "top N" statistics become relatively easy.   
  * If we assume that turnover is defined strictly in terms of the number of 
  * elements which are in either list BUT NOT BOTH, we can figure out "turnover" of 
  * that list by finding the cardinality of the complement of set intersection of an "old"
  * and "new" top n collection.  CollectionUtils.intersection() returns a collection which 
  * is the intersection (e.g., intersectionCollection), and thus turnover can be defined 
  * for a "top N" list as N - intersectionCollection.size();  
  * 
  * To calculate turnover, we store the sorted results of the previous TraitCount pass
  * in addition to the current results.  We do not store any older (previous previous, etc) 
  * versions of the lists.  
  * 
  * Uses the default implementation of getDataCollectorSchedule() from AbstractDataCollector
  * 
  * @author mark
  *
  */
 
 public class TraitFrequencyAnalyzer extends AbstractDataCollector implements IDataCollector {
 	public TraitFrequencyAnalyzer(Object m) {
 		super(m);
 		// TODO Auto-generated constructor stub
 	}
 
 	public static final String TRAIT_COUNT_LIST_KEY = "TRAIT_COUNT_LIST_KEY";
 	private OpenSequenceGraph turnGraph = null;
 	private OpenSequenceGraph totalVariabilityGraph = null;
 	private TransmissionLabModel model = null;
 	private Log log = null;
 	private Closure freqCounter = null;
 	private Map<Integer, TraitCount> freqMap = null;
 	private ArrayList<TraitCount> prevSortedTraitCounts = null;
 	private ArrayList<TraitCount> curSortedTraitCounts = null;
 	private final String TYPE_CODE = this.getClass().getSimpleName();
 	private int topNListSize = 0;
 	private double ewensVariationLevel = 0.0;
 	private int ewensThetaMultipler = 0;
 	
 	/**
 	 * TraitCount is a value class for tracking trait frequencies. 
 	 * We use a value class rather than just primitive types held
 	 * in collections because we want to make it easy to get a custom
 	 * sort order, based on trait frequency (in this case, make it easy
 	 * to recover the "top N" traits, by frequency in descending order.
 	 * Thus, we implement Comparable and store the count and trait ID.
 	 * @author mark
 	 *
 	 */
 	
 	class TraitCount implements Comparable {
 		private Integer trait = null;
 		private Integer count = 0;
 		
 		public TraitCount(Integer t) {
 			this.trait = t;
 			this.count = 1;
 		}
 
 		public void increment() {
 			this.count++;
 		}
 		
 		public void decrement() {
 			this.count--;
 		}
 		
 		public Integer getTrait() {
 			return this.trait;
 		}
 		
 		public Integer getCount() {
 			return this.count;
 		}
 		 
 		public int compareTo(Object arg0) {
 			// MEM (v1.3): removed the explicit sign reversal, which was hackish 
 			// and possibly fragile, in favor of an explicit Collections.reverse() in process().
 			return this.count.compareTo(((TraitCount)arg0).getCount());
 		}
 		
 	}
 	
 	/**
 	 * FrequencyCounter implements a Closure from the Jakarta Commons Collections 
 	 * library, thus allowing it to act like a functor (or "function object").  Its
 	 * purpose will be to track the frequency of each variant as the closure is 
 	 * applied to a list of agents by CollectionUtils.forAlldo().  
 	 * @author mark
 	 *
 	 */
 	
 	class FrequencyCounter implements Closure {
 		private int agentCount = 0;
 		private int variantCount = 0;
 		TraitFrequencyAnalyzer analyzer = null;
 		
 		public FrequencyCounter() {
 			analyzer = TraitFrequencyAnalyzer.this;
 		}
 		
 		public void execute(Object arg0) {
 			// the following is the only place in this entire set of nested classes that we "know"
 			// the concrete class of the agent objects....
 			AgentSingleIntegerVariant agent = (AgentSingleIntegerVariant) arg0;
 			Integer agentVariant = (Integer) agent.getAgentVariant();
 		
 			if ( analyzer.freqMap.containsKey(agentVariant) == true ) {
 				// we've seen the variant before; increment the count. 
 				TraitCount tc = analyzer.freqMap.get(agentVariant);
 				tc.increment();
 				analyzer.freqMap.put(agentVariant, tc);
 				//log.debug("incrementing count for variant " + agentVariant + " to " + tc.getCount());
 			} else {
 				// this is first time we've seen this variant, initialize the count
 				//log.debug("first encounter of varant " + agentVariant + ": initializing to 1");
 				analyzer.freqMap.put(agentVariant, new TraitCount(agentVariant));
 				variantCount++;
 			}
 			agentCount++;
 		}
 		
 		// next three methods are purely for debugging - DO NOT USE IN PRODUCTION CODE
 		public void debugResetAgentCounter() {
 			agentCount = 0;
 			variantCount = 0;
 		}
 		
 		public int debugGetVariantCounter() {
 			return variantCount;
 		}
 		
 		public int debugGetAgentCounter() {
 			return agentCount;
 		}
 	}
 	
 	/**
 	 * TurnoverSequence is a data Sequence from the Repast libraries, 
 	 * designed to provide a stream of double values to an OpenSequenceGraph.
 	 * 
 	 * What this does is actually calculate a turnover value, given the 
 	 * sorted list of TraitCounts from the previous time step and this time step.
 	 * 
 	 * The sequence method getSValue() will be called by OpenSequenceGraph.step(), 
 	 * so the precondition contract here is that the graph's step() method must be 
 	 * called from within IDataCollector.process(), at a time when prevSortedTraitCounts
 	 * holds the counts from tickCount - 1, and curSortedTraitCounts holds the counts
 	 * from the current model tick.  This is kinda hard to guarantee programmatically, 
 	 * but if you have bugs, beware -- call step() at the right time!
 	 * @author mark
 	 *
 	 */
 	class TurnoverSequence implements Sequence {
 		
 		/**
 		 * precondition contract:  prevSortedTraitCounts != null, curSortedTraitCounts != null
 		 * if prevSortedTraitCounts == null, it's the first tick on the model and we return 0
 		 * otherwise, calculate "set intersection turnover"
 		 * 
 		 * we define "set intersection turnover" as the number of traits which are NOT part 
 		 * of the intersection of the prev and cur TraitCount lists.  This, in turn, means 
 		 * that the turnover is:  (prev.size + cur.size) - ( 2 * intersection.size )	
 		 * 
 		 */
 		public double getSValue() {
 			double turnover = 0.0;
 			//log.debug("lists should be trimmed to " + topNListSize);
 	
 			if ( prevSortedTraitCounts == null ) {
 				// this will happen on the first tick, after that we should be fine
 				return 0;
 			}
 
 			// given the sorted trait frequencies tallied in the IDataCollector process()
 			// method, extract just the sorted trait IDs, trim the list to top "N" if needed
 			List prevList = getTopNTraits(prevSortedTraitCounts);
 			List curList = getTopNTraits(curSortedTraitCounts);
 			
 			// now find the intersection of these two sorted trait ID lists
 			Collection intersection = CollectionUtils.intersection(prevList, curList);
 			log.debug("TFA:  previous: " + Arrays.deepToString(prevList.toArray()));
 			log.debug("TFA:  current: " + Arrays.deepToString(curList.toArray()));
 			log.debug("TFA:  intersection: " + Arrays.deepToString(intersection.toArray()));
 			
 			// now use the list sizes and the cardinality of the intersection set to calculate turnover
 			int prevSize = prevList.size();
 			int curSize = curList.size();
 			int intersectionSize = intersection.size();
 			turnover = (prevSize + curSize) - ( 2 * intersection.size());
 			log.debug("prev size: " + prevSize + " cursize: " + curSize + " intersection size: " + intersectionSize + " turnover: " + turnover);
 			
 			return turnover;
 		}
 		
 		
 	}
 	
 	class TotalVariabilitySequence implements Sequence {
 
 		public double getSValue() {
 			return (double) curSortedTraitCounts.size();
 		}
 		
 	}
 	
 	class EwensSequence implements Sequence {
 
 		public double getSValue() {
 			// We return a constant value here since we're aiming at a "reference" line on the 
 			// total variation graph
 			return ewensVariationLevel;
 		}
 		
 	}
 	
 	class AverageTraitCountSequence implements Sequence {
 		private AverageSequence seq = null;
 		
 		public AverageTraitCountSequence() {
 			this.seq = new AverageSequence(curSortedTraitCounts, "size");
 		}
 		
 		public double getSValue() {
 			return this.seq.getSValue();
 		}
 		
 	}
 	
 	public void build(Object m) {
 		this.model = (TransmissionLabModel) m;
 		this.log = model.getLog();
 
 		this.log.debug("Entering TraitFrequencyAnalyzer.build()");
 		this.freqCounter = new FrequencyCounter();
 		this.freqMap = new TreeMap<Integer, TraitCount>();
 		
 	}
 
 	public void completion() {
 		this.log.debug("entering TraitFrequencyAnalyzer.completion");
 		if ( this.turnGraph != null ) {
 			this.turnGraph.dispose();
 		}
 		if ( this.totalVariabilityGraph != null) {
 			this.totalVariabilityGraph.dispose();
 		}
 
 		this.curSortedTraitCounts = null;
 		this.prevSortedTraitCounts = null;
 		this.model.removeSharedObject(TRAIT_COUNT_LIST_KEY);
 	}
 
 	public void initialize() {
 		this.log.debug("entering TraitFrequencyAnalyzer.initialize");
 		this.topNListSize = this.model.getTopNListSize();
 		this.ewensThetaMultipler = this.model.getEwensThetaMultipler();
 		
 		this.ewensVariationLevel = this.ewensThetaMultipler * this.model.getMu() * this.model.getNumAgents();
 		this.log.info("Ewens " + this.ewensThetaMultipler + "Nmu variation level is: " + this.ewensVariationLevel);
 		
 		this.turnGraph = new OpenSequenceGraph("New Top N Analyzer", this.model);
 		this.turnGraph.setAxisTitles("time", "turnover");
 		StringBuffer sb = new StringBuffer();
 		sb.append("Top ");
 		sb.append(this.topNListSize);
 		this.turnGraph.addSequence(sb.toString(), new TurnoverSequence());
 		this.turnGraph.setXRange(0, 50);
 		this.turnGraph.setYRange(0, 30);
 		this.turnGraph.setSize(400, 250);
 		this.turnGraph.display();
 		
 		this.totalVariabilityGraph = new OpenSequenceGraph("Total Number of Traits in Population", this.model);
 		this.totalVariabilityGraph.setAxisTitles("time", "# of Traits");
 		this.totalVariabilityGraph.addSequence("num traits", new TotalVariabilitySequence());
 		this.totalVariabilityGraph.addSequence("Ewens " + this.ewensThetaMultipler + "Nmu", new EwensSequence());
 		//this.totalVariabilityGraph.addSequence("Avg. Traits", new AverageTraitCountSequence());
 		this.totalVariabilityGraph.setXRange(0, 50);
 		this.totalVariabilityGraph.setYRange(0, 100);
 		this.totalVariabilityGraph.setSize(400, 250);
 		this.totalVariabilityGraph.display();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void process() {
 		this.log.debug("Entering TraitFrequencyAnalyzer.process at time " + this.model.getTickCount());
 		// cache a fresh copy of the agent list since it may have changed due to other module's actions
 		IAgentPopulation population = this.model.getPopulation();
 		List<IAgent> agentList = population.getAgentList();
 
 		// clear out the frequency map, and current list of sorted TraitCounts and recount
 		this.freqMap.clear();
 		this.curSortedTraitCounts = null;
 		
 		// fill up the frequency map
 		CollectionUtils.forAllDo(agentList, this.freqCounter);
 		
 		// At this point, we've got all the counts, so let's prepare a sorted List
 		// of TraitCounts for further processing
 		this.curSortedTraitCounts = new ArrayList<TraitCount>();
 		this.curSortedTraitCounts.addAll(this.freqMap.values());
 		Collections.sort(curSortedTraitCounts);
 		Collections.reverse(curSortedTraitCounts);
 		
 		// debug only
 	/*	log.debug("Sorted map of trait frequencies, in descending frequency order:");
 		for( TraitCount trait: curSortedTraitCounts ) {
 			log.debug("    " + trait.getTrait() + " = " + trait.getCount());
 		}
 		*/
 		// this is the right time to call the graph step() -- prevSortedTraitCounts still
 		// represents tickCount - 1, and curSortedTraitCounts represents this tick.
 		this.turnGraph.step();
 		this.totalVariabilityGraph.step();
 		
 		// housekeeping - store cur in prev for comparison next time around
 		// and cache the current trait counts in the model shared repository for 
 		// other modules to use
 		this.model.storeSharedObject(TRAIT_COUNT_LIST_KEY, this.curSortedTraitCounts);
 		this.prevSortedTraitCounts = this.curSortedTraitCounts;
 	}
 	
 	//	 helper method to reduce duplication - held in the outer class so it
 	// can be used by all inner classes.
 	private List<Integer> getTopNTraits( List<TraitCount> traitCounts ) {
 		ArrayList<Integer> listOfTraits = new ArrayList<Integer>();
 		for( TraitCount trait: traitCounts ) {
 			listOfTraits.add(trait.getTrait());
 		}
 		if (listOfTraits.size() > topNListSize ) {
 			return listOfTraits.subList(0, topNListSize);
 		}
 		// otherwise return the whole list if it's smaller than "top N"
 		return listOfTraits;
 	}
 	
 	public String getDataCollectorName() {
 		return this.TYPE_CODE;
 	}
 
 	@Override
 	protected Schedule getSpecificSchedule(BasicAction actionToSchedule) {
 		Schedule sched = new Schedule();
		sched.scheduleActionBeginning(1, actionToSchedule);
 		return sched;
 	}
 
 
 
 }
