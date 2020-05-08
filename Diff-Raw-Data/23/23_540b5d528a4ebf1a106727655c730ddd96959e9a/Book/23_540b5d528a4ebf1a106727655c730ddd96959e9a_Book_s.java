 package com.tnicoll.apps.bookworm.model;
 
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Multiset;
 import com.tnicoll.apps.bookworm.util.BookStats;
 import com.tnicoll.apps.bookworm.util.Dictionary;
 
 
 
 public class Book {
 	
 	/*
 	 * Contains all the information about a book
 	 */
 	//private Map<Word, MutableInt> words;
 	private HashMultiset <Word>words; 
 	
 	
 	public BookStats stats;
 
 	
 	public BookStats getStats() {
 		return stats;
 	}
 
 	public void setStats(BookStats stats) {
 		this.stats = stats;
 	}
 
 	public Multiset <Word>readBook(String content)
 	{
 		stats = new BookStats();
 		words = HashMultiset.create();
 		stats.setSentence_count(countSentences(content));
 		stats.setCharacter_count(content.length());
 		stats.setSpeech_count(countQuotes(content));
 		content = stripPunctuation(content);
 		Scanner lineScanner = new Scanner(content);
 		int paragraph_count = 0, word_count=0, linecount=0, spelling_error_count=0;
 
 		while(lineScanner.hasNextLine())
 		{
 			String temp = lineScanner.nextLine();
 			linecount++;
 			if(!temp.equals(""))
 			{
 				paragraph_count++;
 				Scanner wordScanner = new Scanner(temp);
 				while(wordScanner.hasNext())
 				{
 					
 					Word s = new Word(stripPunctuation(wordScanner.next()));
 					
 					if(!Dictionary.isInDictionary(s.getElement())){
 						spelling_error_count++;
 						s.setRecognised(false);
						System.out.println(s);
 					}
 					words.add(s);
 					word_count++;
 				}
 			}
 		}
 		stats.setParagraph_count(paragraph_count);
 		stats.setWord_count(word_count);
 		stats.setAvg_paragraph_word_count(word_count/paragraph_count);
 		stats.setLines(linecount);
 		stats.setSpelling_error_count(spelling_error_count);
 		
 		return words;
 	}
 	
 	private int countSentences(String content) {
 
 		if (content == null) {
 	        return -1;
 	    }
 		else{
 		    int count = 0;
 		    
 		    content = content.replaceAll("[...]", ".");
 		    
 		    for (int i = 0; i < content.length(); i++) {
 		        char c = content.charAt(i);
 		        if (c == '.') {
 		            count++;
 		        }
 		    }
 		    
 		    return count;
 		}
 	}
 
 	public String stripPunctuation(String token)
 	{
		if(token.substring(token.length()-1).matches("\\p{Punct}+"))
			return token.substring(0, token.length()-1);
		else
			return token;
 	}
 	
 	public int countQuotes(String content){
 		int count = 0;
 		Pattern p = Pattern.compile("['’\"]\\s");
 		Matcher m = p.matcher(content);
 		while(m.find()){
 			count++;
 		}
 		return count;
 	}
 	
 	 
 }
