 package yarangi.ai.genetic;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import yarangi.numbers.RandomUtil;
 
 /**
  * TODO: sort the soup
  * @author dveyarangi
  *
  * @param <C>
  */
 public class Pool <C extends ICandidate>
 {
 	
 	private Set <C> soup = new HashSet <C> ();
 
 	
 	private IEvolutionStrategy <C> strategy;
 	
 	private int maxSize;
 	
 	private double worstScore ;
 	
 	public Pool(IEvolutionStrategy <C> evolver, int size) 
 	{
 		this.strategy = evolver;
 		this.maxSize = size;
 	}
 	
 	public void step() {
 		C candidate = strategy.createCandidate( this );
 		
 		double score = strategy.evaluateCandidate( candidate );
 		
 		submitCandidate( candidate );
 	}
 	
 	public void submitCandidate(C candidate) 
 	{
 		if(candidate.hasScore())
 			addCandidate( candidate );
 		else
 			strategy.withholdCandidate( candidate );
 	}
 	
 	private  void addCandidate(C candidate) 
 	{
 		
 		if(soup.size() == maxSize && worstScore < candidate.getScore())
 			return;
 		
 		soup.add( candidate );
 		
 		if(soup.size() > maxSize) {
 //			C worst = pickWorst( 2 ).iterator().; 
 			soup.removeAll( pickWorst( 1 ) );
 		}
 	}
 	
 	public void removeCandidate(C candidate) 
 	{
 		soup.remove( candidate );
 	}
 	
 	/**
 	 * Picks specified amount of candidates that perform best over specified input.
 	 * @param num
 	 * @param input
 	 * @param realOutput
 	 * @return
 	 */
 	public Collection <C> pickBest(int num)
 	{
 		SortedMap <Double, C> bestCandidates = new TreeMap <Double, C> ();
 		
 		double maxError = Double.MIN_VALUE;
 		double minError = Double.MAX_VALUE;
 		double sum = 0;
 		int determined = 0;
 		for(C candidate : soup) 
 		{
 			if(!candidate.hasScore())
 				continue;
 			double performance = candidate.getScore();
 				if(performance > maxError)
 					maxError = performance;
 				if(performance < minError)
 					minError = performance;
 				sum += performance;
 			determined ++;
 			if(bestCandidates.size() < num || bestCandidates.lastKey() > performance)
 			{
 				bestCandidates.put( performance, candidate );
 				if(bestCandidates.size() > num)
 					bestCandidates.remove( bestCandidates.lastKey() );
 			}
 		}
 		
 		worstScore = maxError;
 		if(bestCandidates.size() > 0)
 			System.out.println("Best: " + bestCandidates.firstKey() + ", max: " +  maxError + ", min: " + minError + ", avg: " + sum/soup.size() + ", total: " + soup.size());
 		return bestCandidates.values();
 	}
 	/**
 	 * Picks specified amount of candidates that perform best over specified input.
 	 * @param num
 	 * @param input
 	 * @param realOutput
 	 * @return
 	 */
 	public Collection <C> pickWorst(int num)
 	{
 		SortedMap <Double, C> worstCandidates = new TreeMap <Double, C> ();
 		
 		for(C candidate : soup) 
 		{
 			if(!candidate.hasScore())
 				continue;
 			double performance = candidate.getScore();
 			if(worstCandidates.size() < num || worstCandidates.firstKey() < performance)
 			{
 				worstCandidates.put( performance, candidate );
 				if(worstCandidates.size() > num)
 					worstCandidates.remove( worstCandidates.firstKey() );
 			}
 		}
 		
 		return worstCandidates.values();
 	}	
 	
 	
 	public int getSize() { return soup.size(); }
 	
 	public boolean isFull() { return soup.size() == maxSize; }
 
 	public C getRandomCandidate()
 	{
 		Iterator <C> it = soup.iterator();
 		C candidate = it.next();
		int n = RandomUtil.N( soup.size() ) ;
 		for(int i = 1; i <= n; i ++)
 			candidate = it.next();
 		
 		return candidate;
 	}
 	
 
 }
