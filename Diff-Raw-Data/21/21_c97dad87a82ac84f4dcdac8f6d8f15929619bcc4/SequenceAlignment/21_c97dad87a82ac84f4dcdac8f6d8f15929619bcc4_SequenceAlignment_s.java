 package SequenceAlignment;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentMap;
 
 import InputData.InputData;
 import InputData.Suspect;
 import adn.CurrentDNA;
 
 public class SequenceAlignment implements Runnable {
 
 	public InputData input;
 	public Map<String, BestSuspect> bufferBestSuspect;
 	private ConcurrentMap<String, Integer> scoresDNA;
 	public SequenceAlignment(InputData input, Map<String, BestSuspect> bufferBestSuspect, ConcurrentMap<String, Integer> scoresDNA) {
 		this.input = input;
 		this.bufferBestSuspect = bufferBestSuspect;
 		this.scoresDNA = scoresDNA;
 	}
 
 	@Override
 	public void run() {
 		int score = 0;
 		Iterator<Suspect> it = input.suspects.iterator();
 		while (it.hasNext()) {
 			Suspect suspect = (Suspect) it.next();
 			score = evaluate(input.getEvidence(), suspect.getAdn());
 			BestSuspect best = new BestSuspect(suspect, score);
 			String idThread =  Thread.currentThread().getName();
 			if (bufferBestSuspect.containsKey(idThread)) {
 				BestSuspect before = bufferBestSuspect.get(idThread);
 				if (before.getScore() < best.getScore()) {
 					bufferBestSuspect.put(idThread, best);
 				}
 			} else {
 				bufferBestSuspect.put(idThread, best);
 			}
 
 //			System.out.println(suspect.toString() + ", best score:" + score + ", Robot:" + Thread.currentThread().getName() );
 		}
 
 	}
 
 	public int evaluate(String evidence, String suspect) {
 
 		List<Integer> scorePerSolution = new ArrayList<Integer>();
 		CurrentDNA dna = new CurrentDNA(evidence, suspect);
 
 		if (!dna.containsEvidence() && !dna.containsSuspect()) {
 			return 0;
 		} else {
 
 			int scoreBetweenBases = 0;
 
 			if (dna.containsEvidence() && dna.containsSuspect()) {
 				scoreBetweenBases = getScoreBetweenBases(
 						dna.evidence.firstBase, dna.suspect.firstBase);
 				scorePerSolution.add(searchScore(dna.evidence.remaining,
 						dna.suspect.remaining) + scoreBetweenBases);
 			}
 
 			if (dna.areTheBasesDifferent()) {
 
 				if (dna.containsSuspect()) {
 					scoreBetweenBases = getScoreBetweenBases("-",
 							dna.suspect.firstBase);
 					scorePerSolution.add(searchScore(evidence,
 							dna.suspect.remaining) + scoreBetweenBases);
 				}
 
 				if (dna.containsEvidence()) {
 					scoreBetweenBases = getScoreBetweenBases(
 							dna.evidence.firstBase, "-");
 					scorePerSolution.add(searchScore(dna.evidence.remaining,
 							suspect) + scoreBetweenBases);
 				}
 			}
 			return getHighestScore(scorePerSolution);
 		}
 	}
 	
 	private int searchScore(String a, String b)
 	{
 		if ( !scoresDNA.containsKey(a + "-" + b) ) {
 			scoresDNA.put(a + "-" + b, evaluate(a, b));
 		}
 		return scoresDNA.get(a + "-" + b);
 	
 	}
 
 	private int getScoreBetweenBases(String a, String b) {

		return input.scores.get(a).get(b);
 	}
 
 	private int getHighestScore(List<Integer> scorePerSolution) {
 		return Collections.max(scorePerSolution);
 	}
 }
