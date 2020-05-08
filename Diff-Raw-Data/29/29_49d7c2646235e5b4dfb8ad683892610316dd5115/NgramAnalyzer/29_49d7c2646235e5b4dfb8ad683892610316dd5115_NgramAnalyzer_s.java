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
 package net.sf.okapi.lib.search.lucene.analysis;
 
 import java.io.Reader;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.StopFilter;
 import org.apache.lucene.analysis.TokenStream;
 
 /**
  * 
  * @author HaslamJD
  * @author HARGRAVEJE
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
 			" as ", "pro ", " an", " use", " onl", "f th", " com", "e to",
			" inc", "ent ", "form", "ith ", " col", "s in", "age ", "ou c",
 			"e an", "s th", "ick ", "s an", "he c", "u ca", "e on", "s to",
			"sing", "he p", "he a", "from", "on t", " pho", "e pr", "opti",
 			"use ", "ptio", "rom ", "ng t", "ine ", " fro", " opt", "usin",
			"comm", "ents", "lor ", "s of", "able", "es a", "t to", "ayer",
 			"inte", " lay", "an i", "on o", "ble ", "nd t", "to t", "oose",
 			" pre", "tosh", " ele", "g th", "ated", "ons ", " res", "appl",
 			"n im", "he i", "e im", " sel", "be o", "e re", "ers ", "e fo",
 			"ecti", "nter", "d in", "ther", "ate ", "e of", "leme", " dis",
 			"ctio", "es t", "ages", "all ", "to c", " tra", "elem", "info",
 			" inf", "e ad", "s ar", "d to", "ed t", "s yo", "nd c", "nts ",
 			"red ", "r co", "ding", "nfor", "he l", "or t", "t co", "ette",
 			"sion", "ool ", "incl", "ion.", "ide ", "ed i", "o th", "pale",
 			" sta", "alet", "s, a", " be ", "se t", " pal", "h th", "her ",
 			"of a", " int", "e la", "e fi", "he r", "ess ", "ges ", "he t",
 			"he f", "ght ", "op e", "ates", "nt t", "icat", "ppli", "rati",
 			"p el", ", yo", ", or", "twar", "plic"};
 	
 	private Set<Object> stopNgrams;
 
 	private Locale locale;
 	private int ngramLength;
 
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
 			return new StopFilter(false, new AlphabeticNgramTokenizer(reader,
 					ngramLength, locale), stopNgrams);
 		}
 		return new AlphabeticNgramTokenizer(reader, ngramLength, locale);
 	}
 }
