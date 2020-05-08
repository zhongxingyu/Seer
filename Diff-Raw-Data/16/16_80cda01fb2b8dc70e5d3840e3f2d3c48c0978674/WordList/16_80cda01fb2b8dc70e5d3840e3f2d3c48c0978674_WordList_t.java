 package model;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 public class WordList extends ArrayList<Word> {
 	
 	public ArrayList<String> getStatistics() {
 		ArrayList<String> out = new ArrayList<String>();
		if (size() <= 0) return out;
 		calculateStats();
 		
 		DecimalFormat df = new DecimalFormat("#.##");
 		
 		out.add("Number of entered words: " + size());
 		
 		out.add("Average word length: "+ df.format(averageSize));
 		WordList longestWords = getLongestWords();
 		if (longestWords.size() > 1) out.add( "Longest words - " + longestWords.get(0).size() + " letters");
 		else out.add("Longest word - " + longestWords.get(0).size() + " letters");
 		
 		for (int i = 0; i < longestWords.size(); i++) {
 			out.add("     -" + longestWords.get(i).toString());
 			
 		}
 		
 		out.add("Average word score: "+ df.format(averageScore));
 		WordList highestScoringWords = getHighestScoringWords();
 		if (highestScoringWords.size() > 1) out.add("Highest scoring words - " + highestScoringWords.get(0).getWordScore() + " points");
 		else out.add("Highest scoring word - " + highestScoringWords.get(0).getWordScore() + " points");
 		
 		for (int i = 0; i < highestScoringWords.size(); i++) {
 			out.add("     -" + highestScoringWords.get(i).toString());
 		}
 		
 		return out;
 	}
 	
 	
 	private int longest = 0;
 	private int highest = 0;
 	private double averageSize = 0;
 	private double averageScore = 0;
 	
 	private void calculateStats() {
 		double sumSize = 0;
 		double sumScore = 0;
 		for (Word word : this) {
 			if (word.size() > longest) longest = word.size();
 			if (word.getWordScore() > highest) highest = word.getWordScore();
 			sumSize += word.size();
 			sumScore += word.getWordScore();
 		}
 		averageSize = sumSize / size();
 		averageScore = sumScore / size();
 	}
 	
 	private WordList getHighestScoringWords() {
 		WordList out = new WordList();
 		for (Word word : this) {
 			if (word.getWordScore() == highest) out.add(word);
 		}
 		return out;
 	}
 
 	private WordList getLongestWords() {
 		WordList out = new WordList();
 		for (Word word : this) {
 			if (word.size() == longest) out.add(word);
 		}
 		return out;
 	}
 	
 }
