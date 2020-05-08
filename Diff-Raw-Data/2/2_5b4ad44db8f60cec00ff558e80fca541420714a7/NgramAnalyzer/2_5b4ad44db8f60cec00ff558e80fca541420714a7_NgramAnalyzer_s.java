 /*===========================================================================
 Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation; either version 2.1 of the License, or (at
 your option) any later version.
 
 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sf.okapi.tm.pensieve.analyzers;
 
 import java.io.Reader;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.StopFilter;
 import org.apache.lucene.analysis.TokenStream;
 
 /**
  * 
  * @author HaslamJD
  */
 public class NgramAnalyzer extends Analyzer {
 	private static final String[] ENGLISH_STOP_NGRAMS = { "the ", " the",
 			" to ", "tion", " you", "you ", " and", "and ", "ing ", "atio",
 			" not", "t th", "ter ", " of ", "this", "he s", "ion ", "not ",
 			" not", "r th", "ilys", "lyse", "n th", " in ", "e in", "hat ",
 			" thi", "e th", "for ", " for", " tha", " ind", "er t", "that",
 			" con", "our ", " our", "your", " or ", " can", "can ", " is ",
 			"is l", "ment", " are", "are ", "with", " wit", " pro", "ions",
 			"in t", "lect ", " too", "too ", "of t", "ight", "ting", "ing,",
 			" as " };
 	private Set<String> stopNgrams;
 
 	private Locale locale;
 	private int ngramLength;
 
 	@SuppressWarnings("unchecked")
 	public NgramAnalyzer(Locale locale, int ngramLength) {
 		if (ngramLength <= 0) {
 			throw new IllegalArgumentException(
 					"'ngramLength' cannot be less than 1");
 		}
 		this.stopNgrams = StopFilter.makeStopSet(ENGLISH_STOP_NGRAMS);
 		this.locale = locale;
 		this.ngramLength = ngramLength;
 	}
 
 	@Override
 	public TokenStream tokenStream(String fieldName, Reader reader) {
 		if (locale.getLanguage().equalsIgnoreCase("en")) {
			return new StopFilter(true, new AlphabeticNgramTokenizer(reader,
 					ngramLength, locale), stopNgrams);
 		}
 		return new AlphabeticNgramTokenizer(reader, ngramLength, locale);
 	}
 }
