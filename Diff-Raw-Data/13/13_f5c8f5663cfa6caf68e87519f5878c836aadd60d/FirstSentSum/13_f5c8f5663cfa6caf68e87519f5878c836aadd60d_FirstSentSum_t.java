 package ethz.nlp.headgen.sum;
 
 import java.util.List;
 
 import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.util.CoreMap;
 import ethz.nlp.headgen.Doc;
 
 public class FirstSentSum implements Summerizer {
 	private static final String[] START_POS = { "NNP", "NN" };
 	private static final String[] END_POS = { "NNP", "NN" };
 	private static final String[] FLUFF_POS = { "RB" }; // POS that can be
 														// removed without
 														// changing sentence
 														// information EX: very,
 														// much, super
 	private static final String[] SEPERATOR_POS = { ",", ";", "." };
 
 	private Doc doc;
 	private Annotation anot;
 	private int sumLeng;
 	private String firstSent;
 
 	public FirstSentSum(Doc doc, Annotation anot, int summaryLength) {
 		this.doc = doc;
 		this.anot = anot;
 		this.sumLeng = summaryLength;
 	}
 
 	@Override
 	public String summary() {
 		CoreMap firstSentence = findFirstSent();
 		 trimSentenceEnds(firstSentence);
 		 trimUseless(firstSentence.get(TokensAnnotation.class));
 		 removeInternalDependentClause(firstSentence.get(TokensAnnotation.class));
 //		 firstSentence.get(TokensAnnotation.class);
 //		 firstSentence.set(TokensAnnotation.class, outTest);
 		// you can undo this stuff i was just testing things
 
 //		String before = firstSentence.get(TokensAnnotation.class).toString();
 //		firstSentence.get(TokensAnnotation.class).remove(0);
 //		String after = firstSentence.get(TokensAnnotation.class).toString();
 //		String test = toString(firstSentence);
 
 		// TODO Auto-generated method stub
 		// Jared is the best
 		return toString(firstSentence);
 	}
 
 	private CoreMap findFirstSent() {
 		CoreMap sentence = anot.get(SentencesAnnotation.class).get(0);
 		return sentence;
 	}
 
 	public String getFirstSent() {
 		if (firstSent == null) {
 			CoreMap tmp = findFirstSent();
 			firstSent = tmp.toString();
 
 		}
 		return firstSent;
 	}
 
 	// Get rid of words that don't fit at the beginning or end of the sentence
 	private void trimSentenceEnds(CoreMap sentence) {
 		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 		trimBeginning(tokens);
 		trimEnd(tokens);
 	}
 
 	private void trimBeginning(List<CoreLabel> tokens) {
 		while (tokens.size() > 0) {
 			String pos = tokens.get(0).get(PartOfSpeechAnnotation.class);
 
 			for (String s : START_POS) {
 				if (s.equals(pos)) {
 					return; // Suitable starting token found
 				}
 			}
 
 			// Remove the first token
 			tokens.remove(0);
 		}
 	}
 
 	private void trimEnd(List<CoreLabel> tokens) {
 		while (tokens.size() > 0) {
 			String pos = tokens.get(tokens.size() - 1).get(
 					PartOfSpeechAnnotation.class);
 
 			for (String s : END_POS) {
 				if (s.equals(pos)) {
 					return; // Suitable ending token found
 				}
 			}
 
 			// Remove the last token
 			tokens.remove(tokens.size() - 1);
 		}
 	}
 
 	private void trimUseless(List<CoreLabel> tokens) {
 		for (int i = 0; i < tokens.size(); i++) {
 			String pos = tokens.get(i).get(PartOfSpeechAnnotation.class);
 
 			for (String s : FLUFF_POS) {
 				if (s.equals(pos)) {
 					tokens.remove(i);
 				}
 			}
 
 			// Remove the last token
 
 		}
 	}
 
 	private void removeInternalDependentClause(List<CoreLabel> tokens) {
 		boolean dependOn = false;
 		for (int i = 0; i < tokens.size(); i++) {
 			String pos = tokens.get(i).get(PartOfSpeechAnnotation.class);
			String debugTxt=tokens.get(i).get(TextAnnotation.class);
 			if (pos.equals(",")) {
 				dependOn = !dependOn;
 				tokens.remove(i);
				i--;
 			} else if (pos.equals(".")) {
 				dependOn = !dependOn;
 			} else {
				if (dependOn){
				tokens.remove(i);
				i--;
				}
					
					
 			}
 
 			// Remove the last token
 			// System.out.println(tokens.toString());
 		}
 
 	}
 
 	public String toString(CoreMap sentence) {
 		StringBuilder sb = new StringBuilder();
 		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
 			sb.append(token.get(TextAnnotation.class) + " ");
 		}
 		sb.deleteCharAt(sb.length()-1);
 		return sb.toString();
 	}
 }
