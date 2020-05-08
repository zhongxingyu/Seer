 package org.cchmc.bmi.snpomics.annotation.annotator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.cchmc.bmi.snpomics.GenomicSpan;
 import org.cchmc.bmi.snpomics.SimpleVariant;
 import org.cchmc.bmi.snpomics.annotation.factory.AnnotationFactory;
 import org.cchmc.bmi.snpomics.annotation.interactive.NearestTranscriptAnnotation;
 import org.cchmc.bmi.snpomics.annotation.loader.TranscriptLoader;
 import org.cchmc.bmi.snpomics.annotation.reference.TranscriptAnnotation;
 
 public class NearestTranscriptAnnotator implements
 		Annotator<NearestTranscriptAnnotation> {
 
 	private static int NUM_HITS = 2;
 	
 	private static class Pair implements Comparable<Pair> {
 		public Pair(TranscriptAnnotation tx, long dist) {
 			this.tx = tx;
 			this.dist = dist;
 		}
 		public TranscriptAnnotation tx;
 		public long dist;
 		
 		@Override
 		public int compareTo(Pair o) {
 			return (int) (Math.abs(dist) - Math.abs(o.dist));
 		}
 	}
 	
 	@Override
 	public List<NearestTranscriptAnnotation> annotate(SimpleVariant variant,
 			AnnotationFactory factory) {
 		TranscriptLoader loader = (TranscriptLoader) factory.getLoader(TranscriptAnnotation.class);
 		loader.disableLookaheadCache();
 		NearestTranscriptAnnotation result = new NearestTranscriptAnnotation();
 		List<Pair> leftTx = new ArrayList<Pair>();
 		List<Pair> leftGene = new ArrayList<Pair>();
 		int bin = variant.getPosition().getBin();
 		GenomicSpan binSpan = GenomicSpan.fromBin(variant.getPosition().getChromosome(), bin);
 		Set<String> txId = new HashSet<String>();
 		Set<String> geneID = new HashSet<String>();
 		//Look left
 		do {
 			for (TranscriptAnnotation trans : loader.loadByOverlappingPosition(binSpan)) {
 				long dist = distance(variant, trans);
 				leftTx.add(new Pair(trans, dist));
 				leftGene.add(new Pair(trans, dist));
 			}
 			
 			Collections.sort(leftTx);
 			txId.clear();
 			int i=0;
 			while (i < leftTx.size()) {
 				if (txId.contains(leftTx.get(i).tx.getID()))
 					leftTx.remove(i);
 				else {
 					txId.add(leftTx.get(i).tx.getID());
 					i++;
 				}				
 			}
 			if (leftTx.size() > NUM_HITS)
 				leftTx.subList(NUM_HITS, leftTx.size()).clear();
 			
 			Collections.sort(leftGene);
 			geneID.clear();
 			i=0;
 			while (i < leftGene.size()) {
 				if (geneID.contains(leftGene.get(i).tx.getName()))
 					leftGene.remove(i);
 				else {
 					geneID.add(leftGene.get(i).tx.getName());
 					i++;
 				}
 			}
 			if (leftGene.size() > NUM_HITS) {
 				leftGene.subList(NUM_HITS, leftGene.size()).clear();
 				break;
 			}
 			
 			bin--;
 			binSpan = GenomicSpan.fromBin(variant.getPosition().getChromosome(), bin);
 		} while (binSpan.getEnd() < variant.getPosition().getStart());
 		
 		//Look right
 		List<Pair> rightTx = new ArrayList<Pair>();
 		List<Pair> rightGene = new ArrayList<Pair>();
 		bin = variant.getPosition().getBin()+1;
 		binSpan = GenomicSpan.fromBin(variant.getPosition().getChromosome(), bin);
 		//Iterate until we either get to the "next level" of bins (which will wrap back to start at 0
 		//or until we're farther from the variant than the farthest gene found to the left
 		//Note that there's a short-circuit in the loop to quit when we've found NUM_HITS genes
 		while (binSpan.getStart() > variant.getPosition().getEnd() &&
 				binSpan.getStart() - variant.getPosition().getEnd() < 1E6) {
 			for (TranscriptAnnotation trans : loader.loadByOverlappingPosition(binSpan)) {
 				long dist = distance(variant, trans);
 				rightTx.add(new Pair(trans, dist));
 				rightGene.add(new Pair(trans, dist));
 			}
 			
 			Collections.sort(rightTx);
 			txId.clear();
 			int i=0;
 			while (i < rightTx.size()) {
 				if (txId.contains(rightTx.get(i).tx.getID()))
 					rightTx.remove(i);
 				else {
 					txId.add(rightTx.get(i).tx.getID());
 					i++;
 				}				
 			}
 			if (rightTx.size() > NUM_HITS)
 				rightTx.subList(NUM_HITS, rightTx.size()).clear();
 			
 			Collections.sort(rightGene);
 			geneID.clear();
 			i=0;
 			while (i < rightGene.size()) {
 				if (geneID.contains(rightGene.get(i).tx.getName()))
 					rightGene.remove(i);
 				else {
 					geneID.add(rightGene.get(i).tx.getName());
 					i++;
 				}
 			}
 			if (rightGene.size() > NUM_HITS) {
 				rightGene.subList(NUM_HITS, rightGene.size()).clear();
 				break;
 			}
 			
 			if (leftGene.size() > 0 && Math.abs(leftGene.get(leftGene.size()-1).dist) < binSpan.getEnd() - variant.getPosition().getEnd())
 				break;
 			
 			bin++;
 			binSpan = GenomicSpan.fromBin(variant.getPosition().getChromosome(), bin);
 		}
 
 		//Merge results:
 		leftTx.addAll(rightTx);
 		leftGene.addAll(rightGene);
 		Collections.sort(leftTx);
 		txId.clear();
 		int i=0;
 		while (i < leftTx.size()) {
 			if (txId.contains(leftTx.get(i).tx.getID()))
 				leftTx.remove(i);
 			else {
 				txId.add(leftTx.get(i).tx.getID());
 				i++;
 			}				
 		}
 		if (leftTx.size() > NUM_HITS)
 			leftTx.subList(NUM_HITS, leftTx.size()).clear();
 		
 		Collections.sort(leftGene);
 		geneID.clear();
 		i=0;
 		while (i < leftGene.size()) {
 			if (geneID.contains(leftGene.get(i).tx.getName()))
 				leftGene.remove(i);
 			else {
 				geneID.add(leftGene.get(i).tx.getName());
 				i++;
 			}
 		}
 		if (leftGene.size() > NUM_HITS)
 			leftGene.subList(NUM_HITS, leftGene.size()).clear();
 
 		//Save results:
 		List<TranscriptAnnotation> t = new ArrayList<TranscriptAnnotation>();
 		List<Long> d = new ArrayList<Long>();
 		for (Pair p : leftTx) {
 			t.add(p.tx);
 			d.add(p.dist);
 		}
 		result.setNearestTranscripts(t, d);
 		
 		t = new ArrayList<TranscriptAnnotation>();
 		d = new ArrayList<Long>();
 		for (Pair p : leftGene) {
 			t.add(p.tx);
 			d.add(p.dist);
 		}
 		result.setNearestGenes(t, d);
 		return Collections.singletonList(result);
 	}
 	
 	/*
 	 * Computes the distance between the variant and the TSS.  Positive values are downstream of the
 	 * transcript, negative values are upstream
 	 */
 	protected long distance(SimpleVariant variant, TranscriptAnnotation tx) {
 		if (tx.isOnForwardStrand())
 			return variant.getPosition().getStart() - tx.getPosition().getStart();
 		else
			return tx.getPosition().getStart() - variant.getPosition().getEnd();
 	}
 
 	@Override
 	public Class<NearestTranscriptAnnotation> getAnnotationClass() {
 		return NearestTranscriptAnnotation.class;
 	}
 
 }
