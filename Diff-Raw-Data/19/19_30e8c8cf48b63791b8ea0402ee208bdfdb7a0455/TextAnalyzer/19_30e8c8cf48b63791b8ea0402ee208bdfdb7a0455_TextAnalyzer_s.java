 /*******************************************************************************
  * Copyright (c) 2012 BragiSoft, Inc.
  * This source is subject to the BragiSoft Permissive License.
  * Please see the License.txt file for more information.
  * All other rights reserved.
  * 
  * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY 
  * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
  * PARTICULAR PURPOSE.
  * 
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * Contributors:
  * Benedict Holste - General layout of the class, implementation of totalCharCount, totalWordCount, averageWordLength
  * Sebastian Muszytowski - Documentation and implementation of totalSentenceCount, averageSentenceLength
  *******************************************************************************/
 
 package com.bragi.sonify.textanalyzer;
 
 /**
  * The TextAnalyzer class implements the ITextAnalyzer interface and
  * provides various information about the constitution of an input text.
  * 
  * @author Benedict Holste <benedict@bholste.net>
  * @author Sebastian Muszytowski <sebastian@muszytowski.net>
  */
 public class TextAnalyzer {
 	
 	private int totalCharCount;
 	private int totalWordCount;
 	private int averageWordLength;
 	private int totalSentenceCount;
 	private int averageSentenceCharLength;
 	private int averageSentenceWordLength;
 	
 	/**
 	 * Constructor for TextAnalyzer
 	 * 
 	 * @param text Text to be analyzed. This has to be real text!
 	 */
 	public TextAnalyzer(String text) {
 		analyzeText(text);
 	}
 
 	/**
 	 * Function which returns the calculated character count
 	 * @return Integer Total character count
 	 */
 	public int totalCharCount() {
 		return totalCharCount;
 	}
 
 	/**
 	 * Function which returns the calculated word count
 	 * @return Integer Total word count
 	 */
 	public int totalWordCount() {
 		return totalWordCount;
 	}
 	
 	/**
 	 * Function which returns the calculated sentence count
 	 * @return Integer Total sentence count
 	 */
 	public int totalSentenceCount() {
 		return totalSentenceCount;
 	}
 
 	/**
 	 * Function which returns the calculated average word length
 	 * @return Integer Average word length
 	 */
 	public int averageWordLength() {
 		return averageWordLength;
 	}
 
 	/**
 	 * Function which returns the calculated average sentence length
 	 * 
 	 * The calculated value contains average of the sum of all characters
 	 * in the sentence
 	 * @return Integer Average sentence character length
 	 */
 	public int averageSentenceCharLength() {
 		return averageSentenceCharLength;
 	}
 	
 	/**
 	 * Function which returns the calculated average word length
 	 * 
 	 * The calculated value contains average of the length of all words
 	 * in the sentence
 	 * @return Integer Average sentence word length
 	 */
 	public int averageSentenceWordLength() {
 		return averageSentenceWordLength;
 	}
 
 	/**
 	 * Function which analyzes the text and writes text characteristics into
 	 * the private member variables which then can be accessed via the provided
 	 * functions.
 	 * 
 	 * @param text Text to be analyzed
 	 */
 	private void analyzeText(String text) {
 	
 		/*
 		 * Total word count
 		 */
 		String[] words = text.split("\\s+");
 		totalWordCount = words.length;
 		
 		/*
 		 * Average sentence length
 		 * 
 		 * loop over all words to get the value
 		 */
 		int wordLengthSum = 0;
 		for(String word : words) {
 			wordLengthSum += word.length();
 		}
 		
 		/*
 		 * Average word length
 		 */
 		averageWordLength = Math.round(wordLengthSum/totalWordCount);
 		
 		/*
 		 * Set the total character count
 		 */
 		totalCharCount = text.length();
 		
 		/*
 		 * Sentence count
 		 */
 		String[] sentences = text.split("(?<=[\\S])[\\.\\!\\?]\\s+");
 		totalSentenceCount = sentences.length;
 		
 		/*
 		 * Average sentence length
 		 */
 		int sentenceCharLengthSum = 0;
 		int sentenceWordLengthSum = 0;
 		for(String sentence : sentences) {
 			sentenceCharLengthSum += sentence.length();
 			words = sentence.split("\\s+");
 			for(String word : words) {
 				sentenceWordLengthSum += word.length();
 			}			
 		}		
 		averageSentenceCharLength = Math.round(sentenceCharLengthSum/totalSentenceCount);	
 		averageSentenceWordLength = Math.round(sentenceWordLengthSum/totalSentenceCount);		
 	}
 
 	@Override
 	public int hashCode() {
		final int prime = 31;
 		int result = 1;
 		result = prime * result + averageSentenceCharLength;
 		result = prime * result + averageSentenceWordLength;
 		result = prime * result + averageWordLength;
 		result = prime * result + totalCharCount;
 		result = prime * result + totalSentenceCount;
 		result = prime * result + totalWordCount;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		TextAnalyzer other = (TextAnalyzer) obj;
 		if (averageSentenceCharLength != other.averageSentenceCharLength)
 			return false;
 		if (averageSentenceWordLength != other.averageSentenceWordLength)
 			return false;
 		if (averageWordLength != other.averageWordLength)
 			return false;
 		if (totalCharCount != other.totalCharCount)
 			return false;
 		if (totalSentenceCount != other.totalSentenceCount)
 			return false;
 		if (totalWordCount != other.totalWordCount)
 			return false;
 		return true;
 	}
 	
 	
 	
 }
