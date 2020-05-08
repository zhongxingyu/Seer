 package pl.edu.agh.student.pathfinding.solver;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Random;
 
 import pl.edu.agh.student.pathfinding.IPathGenerator;
 import pl.edu.agh.student.pathfinding.ISolutionModifier;
 import pl.edu.agh.student.pathfinding.Solution;
 
 public class CuckooSolver implements ISolver {
 	private IPathGenerator generator;
 	private int n;
 	private Solution[] nest;
 	private int[] f; // quality/fitness
 	private int maxGeneration;
 	private Random r = new Random();
 	private ISolutionModifier solutionModifier;
 	private int toAbandon;
 
 	public CuckooSolver(IPathGenerator generator,
 			ISolutionModifier solutionModifier, int population,
 			int maxGeneration, double pa) {
 		this.generator = generator;
 		this.n = population;
 		this.maxGeneration = maxGeneration;
 		this.solutionModifier = solutionModifier;
 		nest = new Solution[n];
 		toAbandon = (int) (pa * n);
 		f = new int[n];
 	}
 	
 	private void sort() {
 		Arrays.sort(nest, new Comparator<Solution>() {
 			public int compare(Solution f1, Solution f2) {
 				// cost of f1 is lesser than f2 so f1 is better
 				return f1.f - f2.f;
 			}
 		});
 	}
 
 	@Override
 	public Solution solve() {
 
 		// Generate an initial population of n host nests
 		for (int i = 0; i < n; i++) {
 			nest[i] = generator.getSolution();
 		}
 
 		for (int t = 0; t < maxGeneration; t++) { // While (t<MaxGeneration) or
 													// (stop criterion)
 			// Get a cuckoo randomly (say, i) and replace its solution by
 			// performing random operations;
 			int i = r.nextInt(n);
 			Solution randomNest = nest[i];
 			//FIXME: randomNest = solutionModifier.modify(nest[i]);
 
 			// Evaluate its quality/fitness
 			int fi = randomNest.f();
 
 			// Choose a nest among n (say, j) randomly;
 			int j = r.nextInt(n);
 			int fj = f[j];
 
 			if (fi > fj) {
 				nest[i] = randomNest;
 				f[i] = fi;
 			}
 
 			sort();
 			// A fraction (pa) of the worse nests are abandoned and new ones are built;
			for(i = toAbandon; i<n; i++) {
 				nest[i] = generator.getSolution();
 			}
 			
 			// Rank the solutions/nests and find the current best;
 			sort();

 		}
 
 		return nest[0];
 	}
 }
