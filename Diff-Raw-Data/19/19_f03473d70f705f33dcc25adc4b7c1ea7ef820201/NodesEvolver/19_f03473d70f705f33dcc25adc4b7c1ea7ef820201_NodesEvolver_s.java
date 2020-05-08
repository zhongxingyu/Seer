 package yarangi.ai.genetic.nn;
 
 import java.util.Arrays;
 import java.util.List;
 
 import yarangi.ai.genetic.IEvolver;
 import yarangi.ai.nn.fast.NetworkDescriptor;
 import yarangi.ai.nn.fast.Serializer;
 import yarangi.numbers.RandomUtil;
 
 public class NodesEvolver implements IEvolver <NNCandidate>
 {
 
 	public static double MUTATION_STRENGTH = 1; 
 	
 	/**
 	 * Network layer nodes counter:
 	 */
 	private NetworkDescriptor networkDescriptor;
 	
 	private int count = 0;
 	
 	public NodesEvolver(NetworkDescriptor descriptor) 
 	{
 		this.networkDescriptor = descriptor;
 		
 		count = 0;
 		for(int layerSize : descriptor.getLayers()) 
 		{
 			count += layerSize;
 		}
 	}
 	
 	/**
 	 * For randomly picked node, shifts its weight values 
 	 * by normal distribution with variance of {@link #MUTATION_STRENGTH} 
 	 */
 	@Override
 	public NNCandidate mutate(NNCandidate parent)
 	{
 		double [] mutant = Arrays.copyOf(parent.getChromosome(), parent.getChromosome().length);
 		
 		// picking node to mutate:
 		for(int node = 0; node < 10; node ++) {
			int mutatedNodeIdx = RandomUtil.getRandomInt( count );
 			
 			// getting node weights:
 			int nodeIdxs[] = Serializer.getNodeIndices( networkDescriptor, mutatedNodeIdx );
 			
 			int nodeOffset = nodeIdxs[0];
 			int nodeLength = nodeIdxs[1];
 			
 			// mutating:
 			for(int idx = nodeOffset; idx < nodeOffset + nodeLength; idx ++)
 				mutant[idx] = RandomUtil.U( mutant[idx], MUTATION_STRENGTH );
 		}
 		return new NNCandidate(mutant, networkDescriptor);
 	}
 
 	/**
 	 * For each node randomly picks one of the parent and uses it weights for 
 	 * corresponding offspring node
 	 */
 	@Override
 	public NNCandidate crossover(List <NNCandidate>  parents)
 	{
 		
 		NNCandidate alphaParent = parents.get(0);
 		// copying array to preserve layer delimeters:
 		double [] offspring = Arrays.copyOf( alphaParent.getChromosome(), alphaParent.getChromosome().length );
 		
 		NNCandidate pickedParent;
 		
 		// commencing orgy:
 		for(int nodeIdx = 0; nodeIdx < count; nodeIdx ++)
 		{
 
 			int nodeIdxs[] = Serializer.getNodeIndices( networkDescriptor, nodeIdx );
 			
 			int nodeOffset = nodeIdxs[0];
 			int nodeLength = nodeIdxs[1];
 			
			pickedParent = parents.get(RandomUtil.getRandomInt( parents.size()));
 			
 			try {
 			System.arraycopy( pickedParent.getChromosome(), nodeOffset, offspring, nodeOffset, nodeLength );
 			}
 			catch(ArrayIndexOutOfBoundsException ex) {
 				ex.printStackTrace();
 			}
 		}
 		
 		
 		return new NNCandidate(offspring, networkDescriptor);
 	}
 
 /*	public static void main(String ... args)
 	{
 		FullNetwork network = new FullNetwork(new TanHAF());
 		
 		network.addLayer( new FullLayer(3, 3) );
 		network.addLayer( new FullLayer(2, 3) );
 		network.addLayer( new FullLayer(1, 2) );
 		
 		int [] desc = Serializer.getDescriptor( network );
 		
 		double [] net = Serializer.toArray( network );
 		FullNetwork restored = Serializer.fromArray( net, desc, new TanHAF() );
 
 		
 		double [] output = restored.activate( new double [] { 1, 0.5, 0} );
 		
 		NodesEvolver evolver = new NodesEvolver( desc, 6, new LogisticAF() );
 		
 		NNCandidate parent = new NNCandidate( network );
 		// spawning heavily mutated bastard:
 		NNCandidate mutant = evolver.mutate( parent );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 		mutant = evolver.mutate( mutant );
 
 		
 		// parthenogenic incest is probably not immoral:
 		List <NNCandidate> candidates;
 //		NNCandidate  offspring = evolver.crossover( parent, mutant );
 	}*/
 
 }
