 package com.boszdigital.tdd;
 
 /**
 * Change to see it in Git
 */
 
 public class Sentence {
 
 	private String string;
 
 	public Sentence(String string) {
 		this.string = string;
 	}
 
 	public boolean isPalindrome() {
 		return evaluateString(string);
 	}
 
 	public String removeSpecialCharacters(String stringToClean) {
 		StringBuilder cleanSentence = new StringBuilder();
 
 		for (char character : stringToClean.toCharArray()) {
 			if (Character.isLetterOrDigit(character)) {
 				cleanSentence.append(character);
 			}
 		}
 
 		return cleanSentence.toString();
 	}
 
 	private String cleanWordtoEvaluate(String wordtoEvaluate) {
 		String wordtoEvaluateClean = wordtoEvaluate;
 
 		wordtoEvaluateClean = wordtoEvaluateClean.toLowerCase();
 		wordtoEvaluateClean = wordtoEvaluateClean.replaceAll(" ", "");
 		wordtoEvaluateClean = removeSpecialCharacters(wordtoEvaluateClean);
 
 		return wordtoEvaluateClean;
 	}
 
 	private boolean evaluateString(String wordtoEvaluate) {
 		wordtoEvaluate = cleanWordtoEvaluate(wordtoEvaluate);
 
		int finalLetter = wordtoEvaluate.length() - 1;
 
 		for (int letter = 0; letter < wordtoEvaluate.length(); letter++) {
 			if (wordtoEvaluate.charAt(letter) != wordtoEvaluate
 					.charAt(finalLetter))
 				return false;
 
 			finalLetter--;
 		}

 		return true;
 	}
 
 	public void gitMethodToTest()
 	{
 		String trollName = "Sauuul";
 		System.out.println("¡¡" + trollName + ", hermano!!");
 		
 		trollName = "Rodrigooo";
 		System.out.println("Ahora el troll es... ¡¡" + trollName + ", hermano!!");
 	}
        private boolean evaluateString(String wordtoEvaluate, int numberOfchars){
               evaluateString(wordtoEvaluate);      
        }
 
 }
