 package jea;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 public class Evolution {
 
 	public static Permutation mutation(Permutation permutation) {
 		int typ = (int) (Math.random() * 3);
 		Permutation newPermutation = new Permutation(permutation);
		if (3 != typ) {
 			int geneOne = (int) (Math.random() * permutation.getGeneCount());
 			int geneTwo = (int) (Math.random() * permutation.getGeneCount());
 			while (geneOne == geneTwo) {
 				geneTwo = (int) (Math.random() * permutation.getGeneCount());
 			}
 			if (0 == typ) {
 				//Vertauschung
 				newPermutation.setGene(geneOne, permutation.getGene(geneTwo));
 				newPermutation.setGene(geneTwo, permutation.getGene(geneOne));
 			} else {
 				//Inversion
 				if(geneOne > geneTwo) {
 					//sortieren
 					int i = geneTwo;
 					geneTwo = geneOne;
 					geneOne = i;
 				}
 				
 				int j = ((geneTwo - geneOne + 1) / 2) - 1;
 				for(int i = 0; i <= j; i++) {
 					newPermutation.setGene(geneOne + i, permutation.getGene(geneTwo - i));
 					newPermutation.setGene(geneTwo - i, permutation.getGene(geneOne + i));
 				}
 
 			}
 
 		}
 		return newPermutation;
 	}
 	
 	public static Permutation recombination(Permutation father, Permutation mother) {
 		int k = 0;
 		int n = father.getGeneCount();
 		HashMap<Integer, Vector<Integer>> adjs = new HashMap<Integer, Vector<Integer>>();
 		while(k < n) {
 			int i = father.getGenePosition(k);
 			int j = mother.getGenePosition(k);
 			Vector<Integer> adj = new Vector<Integer>();
 			adj.add(father.getGene((i + n - 1) % n));
 			adj.add(father.getGene((i + 1) % n));
 			adj.add(mother.getGene((j + n - 1) % n));
 			adj.add(mother.getGene((j + 1) % n));
 			adjs.put(k, adj);
 			k++;
 		}
 		Permutation child = new Permutation(father.getGeneCount());
 		if(0 == (int) (Math.random() * 2))
 			child.setGene(0, father.getGene(0));
 		else
 			child.setGene(0, mother.getGene(0));
 		
 		for(int i = 1; i < n; i++) {
 			int m = Integer.MAX_VALUE;
 			Vector<Integer> genes = getUnusedGenes(adjs.get(child.getGene(0)), child);
 			for (Integer gene : genes) {
 				int length = getUnusedGenes(adjs.get(gene), child).size();
 				if(length < m)
 					m = length;
 			}
 			Vector<Integer> K = new Vector<Integer>();
 			if(0 < m) {
 				for (Integer gene : genes) {
 					if(m == getUnusedGenes(adjs.get(gene), child).size())
 						K.add(gene);
 				}
 			}
 			if(0 == K.size())
 				K = getUnusedGenes(child);
 			child.setGene(i, K.get((int) (Math.random() * K.size())));
 		}
 		
 		return child;
 	}
 	
 	private static Vector<Integer> getUnusedGenes(Vector<Integer> adj, Permutation permutation) {
 		Vector<Integer> genes = new Vector<Integer>();
 		for (Integer gene : adj) {
 			if(-1 == permutation.getGenePosition(gene))
 				genes.add(gene);
 		}
 		return genes;
 	}
 	
 	private static Vector<Integer> getUnusedGenes(Permutation permutation) {
 		Vector<Integer> genes = new Vector<Integer>();
 		for (int i = 0; i < permutation.getGeneCount(); i++) {
 			genes.add(i);
 		}
 		return getUnusedGenes(genes, permutation);
 	}
 }
