 package yarangi.ai.genetic.nn;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import yarangi.numbers.RandomUtil;
 import yarangi.ai.genetic.IEvolutionStrategy;
 import yarangi.ai.genetic.Pool;
 import yarangi.ai.nn.fast.FullNetwork;
 import yarangi.ai.nn.fast.NetworkDescriptor;
 import yarangi.ai.nn.fast.Serializer;
 
 public abstract class NNEvolutionStrategy implements IEvolutionStrategy <NNCandidate>
 {
 	
 	private LinkedList <NNCandidate> unscoredCandidates = new LinkedList <NNCandidate> ();
 	
 	private NodesEvolver evolver;
 	
 	private NetworkDescriptor descriptor;
 	
 	public NNEvolutionStrategy(NetworkDescriptor descriptor)
 	{
 		this.descriptor = descriptor;
 		this.evolver = new NodesEvolver( descriptor );
 	}
 
 	public NNCandidate createCandidate(Pool <NNCandidate> pool)
 	{
 		if(!unscoredCandidates.isEmpty()) {
 			return unscoredCandidates.pop();
 		}
 		if(!pool.isFull()) {
 			NNCandidate c =  new NNCandidate( createNetwork() );
 //			pool.addCandidate( c );
 			return c;
 		}
 		
 		Collection <NNCandidate> pparents = pool.pickBest( 5 );
		switch(RandomUtil.getRandomInt( 10 )) {
 		case 0: if(pool.getSize() > 0) return pool.getRandomCandidate(); // any outcast gets another chance
 //		case 1: if(pool.getSize() > 0) return pparents.iterator().next(); // alpha-parent retested
 		case 3: case 4: if(pool.getSize() > 0) return createOffspring(pparents.iterator().next()); // alpha-parent gives birth
 		}
 		
 		
 		List <NNCandidate> parents = new LinkedList <NNCandidate> (pparents);
 		
 		NNCandidate candidate = createOffspring( parents );
 		
 		return candidate;
 	}
 	
 	private FullNetwork createNetwork()
 	{
 		return Serializer.createNetwork( descriptor );
 	}
 
 	public void withholdCandidate(NNCandidate candidate) 
 	{
 
 			unscoredCandidates.add( candidate );
 	}
 	
 	public NNCandidate createOffspring(NNCandidate parent) {
 		
 //		C bastard = evolver.crossover( pCandidates );
 		
 		NNCandidate mutatedBastard = evolver.mutate( parent );
 		
 		return mutatedBastard;
 	}
 	
 	public NNCandidate createOffspring(List <NNCandidate> pCandidates) {
 		
 		NNCandidate bastard = evolver.crossover( pCandidates );
 		
 		NNCandidate mutatedBastard = evolver.mutate( bastard );
 		
 		return mutatedBastard;
 	}
 
 }
