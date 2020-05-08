 package utill;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * This class is a use to divide text in to sentences and sentences in to words.
  */
 public class LenguistikTokenizer {
 	
 	/**
 	 * This method divide a text in to sentences.
 	 * @param text The text needed to be divided.
 	 * @return A {@link List} of sentences.
 	 */
 	public static List<String> sentenceTokrnizer(String text) {
 		List<String> res = new ArrayList<>();
		StringTokenizer sentenceTokenizer =  new StringTokenizer(text, "\n.");
 		while (sentenceTokenizer.hasMoreElements()) {
 			String token = sentenceTokenizer.nextToken();
			String prevToken = null;
			String currSentence = "";
 			if (!(token.equals(".") || token.equals("\n"))) {
				currSentence.concat(token);
 			} else {
 				if (!currSentence.equals("")) {
 					if (token.equals(".")) {
 						res.add(currSentence);
 						currSentence = "";
 					} else if (prevToken.equals("\n")) {
 						res.add(currSentence);
 						currSentence = "";
 					}
 				}
 			}
 			prevToken = token;
 		}
 		return res;
 	}
 	
 	/**
 	 * This method divide a sentence in to a {@link List} of words.
 	 * @param sentence The sentence needed to be divided.
 	 * @return A {@link List} of words.
 	 */
 	public static List<String> wordTokrnizer(String sentence) {
 		List<String> res = new ArrayList<>(); 
 		String inWordTokens = "-\"\'*#@$";
 		StringTokenizer wordTokenizer =  new StringTokenizer(sentence, " \t,\\/!:?~()<>{};",false);
 		while (wordTokenizer.hasMoreElements()) {
 			String word = wordTokenizer.nextToken();
			StringTokenizer inWordTokenizer =  new StringTokenizer(word, inWordTokens);
 			if (inWordTokenizer.countTokens() > 1) {
 				int i=0;
 				String currWord="";
 				while (inWordTokenizer.hasMoreElements()) {
 					String token = inWordTokenizer.nextToken();
 					if (inWordTokens.contains(token)) {
 						if (i != 0 && i < inWordTokenizer.countTokens()) {
 							currWord.concat(token);
 						}
 					}
 					i++;
 				}
 				if (!res.equals("")) {
 					res.add(currWord);
 				}
 			} else {
 				res.add(word);
 			}
 		}
 		return res;
 	}
 
 }
