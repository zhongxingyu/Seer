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
 
 package net.sf.okapi.lib.search.lucene.analysis;
 
 import com.ibm.icu.lang.UCharacter;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import net.sf.okapi.common.exceptions.OkapiIOException;
 
 import org.apache.lucene.analysis.Tokenizer;
 import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
 import org.apache.lucene.analysis.tokenattributes.TermAttribute;
 import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
 
 /**
  * 
  * @author HaslamJD
  */
 public class AlphabeticNgramTokenizer extends Tokenizer {
 	private static final int NO_CHAR = -1;
 	private int ngramLength;
 	private String ngramType;
 	private int offset;
 	private TermAttribute termAttribute;
 	private OffsetAttribute offsetAttribute;
 	private TypeAttribute typeAttribute;
 
 	/**
 	 * Instance variable for performance reasons ONLY
 	 */
 	private List<Character> ngramCache;
 
 	private Locale locale;
 
 	public AlphabeticNgramTokenizer(Reader reader, int ngramLength,
 			Locale locale) {
 		super(reader);
 		if (ngramLength <= 0) {
 			throw new IllegalArgumentException(
 					"'ngramLength' must be greater than 0");
 		}
 		if (reader == null) {
 			throw new IllegalArgumentException("'reader' cannot be null");
 		}
 		this.ngramLength = ngramLength;
 		this.termAttribute = (TermAttribute) addAttribute(TermAttribute.class);
 		this.offsetAttribute = (OffsetAttribute) addAttribute(OffsetAttribute.class);
 		this.typeAttribute = (TypeAttribute) addAttribute(TypeAttribute.class);
 		this.locale = locale;
 		this.ngramCache = new ArrayList<Character>(ngramLength);
 		initializeCache();
 		this.ngramType = "ngram(" + getNgramLength() + ")";
 		this.offset = 0;
 	}
 
 	public Locale getLocale() {
 		return locale;
 	}
 
 	public int getNgramLength() {
 		return ngramLength;
 	}
 	
 	public TermAttribute getTermAttribute() {
 		return termAttribute;		
 	}
 
 	private Reader getReader() {
 		return input;
 	}
 
 	private void initializeCache() {
 		// Clearing for when it's called by reset
 		ngramCache.clear();
 
 		// Place dummy character at beginning for first call to increment to
 		// remove
 		ngramCache.add(Character.MIN_VALUE);
 		int c;
 		for (int i = 1; i < ngramLength; i++) {
 			try {
 				c = getReader().read();
 			} catch (IOException ioe) {
 				throw new OkapiIOException(ioe.getMessage(), ioe);
 			}
 			ngramCache.add((char) c);
 		}

 	}
 
 	@Override
 	public boolean incrementToken() throws IOException {
 		clearAttributes();
 		int c;
 		c = getReader().read();
		if (c == NO_CHAR) {
 			offset = 0;
 			return false;
 		}
 		ngramCache.remove(0);
		ngramCache.add((char) c);
 
 		// Populate Attributes
 		termAttribute.setTermBuffer(toLowerCase(ngramCache));
 		offsetAttribute.setOffset(offset, offset + ngramLength);
 		typeAttribute.setType(ngramType);
 
 		offset++;
 		return true;
 	}
 
 	@Override
 	public void reset(Reader input) throws IOException {
 		super.reset(input);
 		initializeCache();
 		offset = 0;
 	}
 
 	private String toLowerCase(List<Character> ngram) {
 		// TODO: Better way to do convert List<Character> to String
 		StringBuilder sb = new StringBuilder();
 		for (Character cha : ngram) {
 			sb.append(cha);
 		}
 		String termValue = sb.toString();
 		// lowercase term
 		if (locale != null) {
 			termValue = UCharacter.foldCase(termValue, true);
 		}
 		return termValue;
 	}
 }
