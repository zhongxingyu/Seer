 package jea;
 
 public class Permutation {
 	
 	private int geneCount;
 	private int[] genes;
 	private int fitness;
 	
 	public Permutation(int geneCount) {
 		this.geneCount = geneCount;
 		genes = new int[geneCount];
 		for (int i = 0; i < genes.length; i++) {
 			genes[i] = -1;
 		}
 	}
 	
 	public Permutation(Permutation permutation) {
 		this.geneCount = permutation.geneCount;
		genes = new int[geneCount];
		for(int i = 0; i < geneCount; i++) {
			genes[i] = permutation.genes[i];
		}
 	}
 	
 	public static Permutation getRandomPermutation(int geneCount) {
 		Permutation permutation = new Permutation(geneCount);
 		int i = 0;
 		while(i < geneCount) {
 			int gene = (int) (Math.random() * geneCount);
 			if(-1 == permutation.getGenePosition(gene)) {
 				permutation.genes[i] = gene;
 				i++;
 			}
 		}
 		return permutation;
 	}
 	
 	public int getGeneCount() {
 		return geneCount;
 	}
 	
 	public void setGene(int position, int gen) {
 		if(genes.length <= position)
 			return;
 		genes[position] = gen;
 	}
 	
 	public int getGene(int position) {
 		return genes[position];
 	}
 	
 	public int getGenePosition(int gene) {
 		for(int i = 0; i < genes.length; i++) {
 			if(genes[i] == gene)
 				return i;
 		}
 		return -1;
 	}
 	
 	public void calcFitness() {
 		GenePoolSingleton.getInstance().calcFitness(this);
 	}
 	
 	public void setFitness(int fitness) {
 		this.fitness = fitness;
 	}
 	
 	public int getFitness() {
 		return fitness;
 	}
 }
