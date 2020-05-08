 package stein.dictionary;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 public class FormWordsWithTiles {
 
 	private Dictionary w;
 	private Random r;
 	private ArrayList<Character> tiles;
 
 	public FormWordsWithTiles(int numTiles) throws IOException {
 		w = new Dictionary(new File("./wordlist.txt"));
 		r = new Random();
 		tiles = new ArrayList<Character>();
 		addTiles(numTiles);
 	}
 
 	public FormWordsWithTiles(ArrayList<Character> tiles)
 			throws IOException {
 		w = new Dictionary(new File("./wordlist.txt"));
 		r = new Random();
 		this.tiles = tiles;
 	}
 
 	public ArrayList<Character> getTiles() {
 		return tiles;
 	}
 
 	private void addTiles(int numTiles) {
 		for (int i = 0; i < numTiles; i++) {
 			tiles.add(generateALetter());
 		}
 
 	}
 
 	public ArrayList<String> getWordsThatCanBeFormedWithTiles() {
 		ArrayList<String> words = new ArrayList<String>();
 		Set<String> keySet = w.getWordMap().keySet();
 		Iterator<String> wordIterator = keySet.iterator();
 		while (wordIterator.hasNext()) {
 			String s = wordIterator.next();
 			if (isAbleToBeFormedByTiles(s)) {
 				words.add(s);
 			}
 		}
 		return words;
 	}
 
 	public int numTimesLetterAppears(char[] array, char c) {
 		int i = 0;
 		for (char each : array) {
 			if (each == c) {
 				i++;
 			}
 		}
 		return i;
 	}
 
 	public int numTimesLetterAppearsInTiles(char c) {
 		int i = 0;
 		for (Character each : tiles) {
 			if (each == c) {
 				i++;
 			}
 		}
 		return i;
 	}
 
 	public boolean isAbleToBeFormedByTiles(String s) {
 		char[] stringArray = s.toCharArray();
 		boolean ableToBeFormed = true;
 		for (char c : stringArray) {
 			if (!tiles.contains(c)) {
 				ableToBeFormed = false;
 				break;
 			} else if (!(numTimesLetterAppears(stringArray, c) <= numTimesLetterAppearsInTiles(c))) {
 				ableToBeFormed = false;
 				break;
 			}
 		}
 		return ableToBeFormed;
 	}
 
 	private Character generateALetter() {
 		int i = r.nextInt(144);
 		if (i < 18)
 			return 'e';
 		else if (i < 31)
 			return 'a';
 		else if (i < 43)
 			return 'i';
 		else if (i < 54)
 			return 'o';
 		else if (i < 63)
 			return 't';
 		else if (i < 72)
 			return 'r';
 		else if (i < 80)
 			return 'n';
 		else if (i < 86)
 			return 'd';
 		else if (i < 92)
 			return 's';
 		else if (i < 98)
 			return 'u';
 		else if (i < 103)
 			return 'l';
 		else if (i < 107)
 			return 'g';
 		else if (i < 110)
 			return 'b';
 		else if (i < 113)
 			return 'c';
 		else if (i < 116)
 			return 'f';
 		else if (i < 119)
 			return 'h';
 		else if (i < 122)
 			return 'm';
 		else if (i < 125)
 			return 'p';
 		else if (i < 128)
 			return 'v';
 		else if (i < 131)
 			return 'w';
 		else if (i < 134)
 			return 'y';
 		else if (i < 136)
 			return 'j';
 		else if (i < 138)
 			return 'k';
 		else if (i < 140)
 			return 'q';
 		else if (i < 142)
 			return 'x';
 		else
 			return 'z';
 	}
 
 }
