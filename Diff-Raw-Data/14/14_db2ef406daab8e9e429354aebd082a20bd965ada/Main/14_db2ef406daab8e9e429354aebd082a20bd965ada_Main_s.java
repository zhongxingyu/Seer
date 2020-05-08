 package test;
 
 import edu.stanford.nlp.classify.Dataset;
 import edu.stanford.nlp.classify.LinearClassifier;
 import edu.stanford.nlp.classify.LinearClassifierFactory;
 import edu.stanford.nlp.ling.BasicDatum;
 import edu.stanford.nlp.ling.Datum;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import argumentClassification.ArgumentClassifier;
 //import argumentClassification.ArgumentClassifier.ArgumentWithProbability;
 import argumentClassification.ArgumentClassifierToken;
 
 import predicateClassification.PredicateClassifier;
 import util.CorpusUtils;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	
 	public static void main(String[] args) throws IOException {
 		ArgumentClassifier argumentClassifier;
 		LinearClassifier<String, String> l;
 		LinearClassifier<String, String> pl;
 		Dataset<String, String> testSentence = ArgumentClassifier.dataSetFromCorpus("test.closed");
 		
 		/*Dataset<String, String> trainSet = ArgumentClassifier.dataSetFromCorpus("train.closed");
 		trainSet.applyFeatureCountThreshold(3);
 		l = new LinearClassifierFactory<String, String>().
 				trainClassifier(trainSet);
 		LinearClassifier.writeClassifier(l, "argumentclassifierA.gz");
 		argumentClassifier = new ArgumentClassifier(l);*/
 		
 		l  = LinearClassifier.readClassifier("argumentclassifierA.gz");
 		pl = LinearClassifier.readClassifier("predicateclassifierA.gz");
 		argumentClassifier = new ArgumentClassifier(l);
 		PredicateClassifier predicateClassifier = new PredicateClassifier(pl);
 		
 		List<List<ArgumentClassifierToken>> sentences = ArgumentClassifier.sentencesFromCorpus("test.closed");
 		
 		@SuppressWarnings("unchecked")
 		List<ArgumentClassifierToken> predicates = (List<ArgumentClassifierToken>) predicateClassifier.predicatesInSentence(sentences.get(0));
 		
 		/*for (ArgumentClassifierToken predicate : predicates){
 			for (ArgumentWithProbability a : argumentClassifier.sortedPossibleArgs(predicate))
 				System.out.println(predicate.splitForm + " " + a.asToken().splitForm + " " + a.probability);
 		}*/
 		
		for (ArgumentClassifierToken predicate : predicates){
 			for(ArgumentClassifierToken argument : ArgumentClassifier.argumentCandidates(predicate)){
 				String argClass = argumentClassifier.argClass(argument, predicate);
 				if (!argClass.equals("NIL"))
 					System.out.println(predicate.splitForm + " " + argument.splitForm + " " + argClass);
 			}
 		}
 		
 		//predicateClassifierTest();
 		
 	}
 
 	public static void predicateClassifierTest() throws NumberFormatException, IOException {
 		PredicateClassifier predicateClassifier;
 		LinearClassifier<String, String> l;
 		
 		l  = LinearClassifier.readClassifier("predicateclassifierCREL.gz");
 		
 		
 		
 		/*l = new LinearClassifierFactory<String, String>().
 				trainClassifier(PredicateClassifier.dataSetFromCorpus("train.closed"));
 		LinearClassifier.writeClassifier(l, "predicateclassifierCREL.gz");*/
 
 		Dataset<String, String> dataset = PredicateClassifier.dataSetFromCorpus("devel.closed");
 		predicateClassifier = new PredicateClassifier(l);
 		
 		int[] nounStats = new int[4];
 		int[] verbStats = new int[4];
 		int[] generalStats = new int[4];
 		int statPosition;
 		
 		final int TRUE_POSITIVE = 0;
 		final int TRUE_NEGATIVE = 1;
 		final int FALSE_POSITIVE = 2;
 		final int FALSE_NEGATIVE = 3;
 		
 		for (Datum<String, String> d : dataset){
 			String label = d.label();
 			String pos = "";
 			
 			for (String s : d.asFeatures()){
 				if (s.startsWith("pposu,i|")){
 					pos =  s.substring(8);
 					break;
 				}
 			}
 			
 			
 			if (label.equals("predicate")){
 				if (predicateClassifier.isPredicate(d))
 					statPosition = TRUE_POSITIVE;
 				else
 				{
 					ArrayList<String> splitLemma = new ArrayList<String>();
 					for (String s : d.asFeatures()){
 						if (s.startsWith("splmu"))
 							splitLemma.add(s);
 					}
 					//System.out.println("FALSE- " + splitLemma.get(0) + " " + splitLemma.get(1) + " " + splitLemma.get(2) + " ");
 					//System.out.println();
 					//System.out.println();
 					statPosition = FALSE_NEGATIVE;
 				}
 			}
 			else{
 				if (predicateClassifier.isPredicate(d)){
 					ArrayList<String> splitLemma = new ArrayList<String>();
 					for (String s : d.asFeatures()){
 						if (s.startsWith("splmu"))
 							splitLemma.add(s);
 					}
 					//System.out.println("FALSE+ " + splitLemma.get(0) + " " + splitLemma.get(1) + " " + splitLemma.get(2) + " ");
 					//System.out.println();
 					//System.out.println();
 					statPosition = FALSE_POSITIVE;
 				}
 					
 					
 				else
 					statPosition = TRUE_NEGATIVE;
 				
 			}
 			
 			generalStats[statPosition]++;
 			if (pos.startsWith("NN"))
 				nounStats[statPosition]++;
 			else if (pos.startsWith("VB"))
 				verbStats[statPosition]++;
 
 		}
 
 		System.out.println();
 		System.out.println("Noun stats:");
 		for (int i : nounStats)
 			System.out.println(i);
 		
 		System.out.println();
 		System.out.println("Verb stats:");
 		for (int i : verbStats)
 			System.out.println(i);
 		
 		System.out.println();
 		System.out.println("General stats:");
 		for (int i : generalStats)
 			System.out.println(i);
 		
 
 	}
 
 }
