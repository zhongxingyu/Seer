 package jp.ac.tsukuba.cs.conclave.geneticalgorithm.realarray;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import jp.ac.tsukuba.cs.conclave.geneticalgorithm.BreedingPipeline;
 import jp.ac.tsukuba.cs.conclave.geneticalgorithm.Genome;
 import jp.ac.tsukuba.cs.conclave.utils.Parameter;
 
 /**
  * Implements the ANDX crossover operator
  * 
  * Reference
  * "Striking a Mean- and Parent-Centric Balance in Real-Valued Crossover Operators"
  * Hiroshi Someya, Transactions of Evolutionary Computation, Volume 17, Issue 6
  * 737-754
  * 
  * @author Claus Aranha (caranha@cs.tsukuba.ac.jp)
  *
  */
 public class RAandx implements BreedingPipeline {
 
 	ArrayList<BreedingPipeline> upstream;
 	double crossoverchance;
 	double sigma; // variance of normal distribution
 	double Zeta; // mean-centric parameter
 	
 	Random dice;
 	
 	public RAandx()
 	{
 		upstream = new ArrayList<BreedingPipeline>();
 	}
 	
 	@Override
 	public void setup(Parameter p, Random d) {
 		crossoverchance = Double.parseDouble(p.getParameter("crossover chance", "0.9"));
 		sigma = Double.parseDouble(p.getParameter("andx sigma","1"));
 		Zeta = Double.parseDouble(p.getParameter("andx zeta","0.5"));		
 		
 		dice = d;
 		for (BreedingPipeline aux: upstream)
 			aux.setup(p, d);
 	}
 
 	@Override
 	public void preGenerationHook() {
 		for (BreedingPipeline aux:upstream)
 			aux.preGenerationHook();
 		// TODO: make BreedingPipeline an abstract type 
 		// and encapsulate this.
 	}
 
 	@Override
 	public List<Genome> apply(List<Genome> parents) {
 		ArrayList<Genome> p = new ArrayList<Genome>();
 		ArrayList<Genome> c = new ArrayList<Genome>();
 		
 		for (BreedingPipeline aux: upstream)
 			p.addAll(aux.apply(parents));
 		
 		if (p.size() < 1)
 		{
 			return null;
 		}
 		
 		if (dice.nextDouble() >= crossoverchance)
 			return p; // no crossover
 			
 		int length = ((RealArrayGenome) p.get(0)).genes.length;
 		
 		RealArrayGenome centroid = new RealArrayGenome(length);
 		for (Genome aux: p)
 		{
 			RealArrayGenome paux = (RealArrayGenome) aux;
 			for (int i = 0; i < length; i++)
 				centroid.genes[i] += paux.genes[i]/length;
 		}
 		
 		for(int i = 0; i < p.size(); i++)
 		{
 			RealArrayGenome xc = (RealArrayGenome) p.get(i).clone();
 			for (int j = 0; j < p.size(); j++)
 				if (j != i)
 				{
 					RealArrayGenome paux = (RealArrayGenome) p.get(j);
 					// TODO: implement the influence of andx parameters
 					double movement = dice.nextGaussian();
 					for (int k = 0; k < length; k++)
 						xc.genes[k] -= paux.genes[k]*movement;
				}				
 		}
 			
 		return c;
 	}
 
 
 	@Override
 	public void chainBreedingPipeline(BreedingPipeline bp) {
 		upstream.add(bp);
 	}
 
 }
