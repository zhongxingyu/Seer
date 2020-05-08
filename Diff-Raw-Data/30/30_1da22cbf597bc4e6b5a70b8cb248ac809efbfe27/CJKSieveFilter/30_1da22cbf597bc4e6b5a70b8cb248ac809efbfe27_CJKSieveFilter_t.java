 /*
 Copyright (c) 2012.
 The Board of Trustees of the Leland Stanford Junior University.
 All rights reserved.
 
 Redistribution and use of this distribution in source and binary forms, with or
 without modification, are permitted provided that: The above copyright notice
 and this permission notice appear in all copies and supporting documentation;
 The name, identifiers, and trademarks of The Board of Trustees of the Leland
 Stanford Junior University are not used in advertising or publicity without the
 express prior written permission of The Board of Trustees of the Leland Stanford
 Junior University; Recipients acknowledge that this distribution is made
 available as a research courtesy, "as is", potentially with defects, without
  any obligation on the part of The Board of Trustees of the Leland Stanford
  Junior University to provide support, services, or repair;
 
 THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL
 WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT
 LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE BOARD OF TRUSTEES OF THE LELAND
 STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT
 LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 SOFTWARE.
 */
 
 package edu.stanford.lucene.analysis.cjk;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.lucene.analysis.TokenFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
 import org.apache.lucene.util.AttributeSource;
 
 import com.ibm.icu.lang.UScript;
 
 /**
  * Emits tokens that are generated from StandardTokenizer or ICUTokenizer,
  * depending on value of emitType
  * <p>
  * If emitType is:
  *  HANGUL, then only emit tokens if at least one Hangul character is found.
  *  JAPANESE, then only emit tokens if at least one Hiragana or Katakana character is found
  *  HAN_SOLO, then only emit tokens if at least one Han character is found, and no Hangul, Hiragana or Katakana chars are found
  *  NO_CJK, then only emit tokens if no characters are found in Han, Hiragana, Katakana or Hangul scripts.
  * <p>
  *
  * cache implementation from   org.apache.lucene.analysis.CachingTokenFilter
  *
  * @author Naomi Dushay
  *
  */
 public class CJKSieveFilter extends TokenFilter
 {
 	// the CJK token types from StandardTokenizer
 	private static final String HAN_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.IDEOGRAPHIC];
 	private static final String HIRAGANA_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HIRAGANA];
 	private static final String KATAKANA_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.KATAKANA];
 	private static final String HANGUL_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HANGUL];
 
 	/** under which conditions should this filter emit tokens? */
 	private final CJKEmitType emitType;
 
 	private boolean tokensHaveHan = false;
 	private boolean tokensHaveHiragana = false;
 	private boolean tokensHaveKatakana = false;
 	private boolean tokensHaveHangul = false;
 
 	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
 	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
 
 	// used for token cache implementation
 	private List<AttributeSource.State> cache = null;
 	private Iterator<AttributeSource.State> iterator = null;
 	private AttributeSource.State finalState;
 
 	/**
 	 * Create a new CJKSieveFilter, emitting tokens per emitType
 	 * @param in
 	 * @param emitType from {@link CJKEmitType},
 	 */
 	public CJKSieveFilter(TokenStream in, CJKEmitType emitType)
 	{
 		super(in);
 		this.emitType = emitType;
 	}
 
 	@Override
 	public final boolean incrementToken() throws IOException
 	{
 		if (cache == null)
 		{
 			// fill cache lazily
 			cache = new LinkedList<AttributeSource.State>();
 			fillCache();
 			iterator = cache.iterator();
 		}
 
 		if (iterator.hasNext())
 		{
 			// Since the TokenFilter can be reset, the tokens need to be preserved as immutable.
 			restoreState(iterator.next());
 
 			switch (emitType)
 			{
 				case HANGUL:
 					if (tokensHaveHangul)
 						return true;
 					break;
 				case JAPANESE:
 					if (tokensHaveHiragana || tokensHaveKatakana)
 						return true;
 					break;
 				case HAN_SOLO:
 					if (tokensHaveHan &&
							!tokensHaveHangul && !tokensHaveHiragana && !tokensHaveKatakana)
 						return true;
 					break;
 				case NO_CJK:
 					if (! (tokensHaveHan || tokensHaveHangul || tokensHaveHiragana || tokensHaveKatakana))
 						return true;
 			}
 
 			return false;
 		}
 
 		// else the cache is exhausted, return false
 		return false;
 	}
 
 	@Override
 	public final void end() throws IOException
 	{
 		if (finalState != null)
 			restoreState(finalState);
 	}
 
 	@Override
 	public void reset() throws IOException
 	{
 		if (cache != null)
 			iterator = cache.iterator();
 	}
 
 	private void fillCache() throws IOException
 	{
 		while(input.incrementToken())
 		{
 			cache.add(captureState());
 			String type = typeAtt.type();
 			if (type == HAN_TYPE)
 				tokensHaveHan = true;
 			else if (type == HIRAGANA_TYPE)
 				tokensHaveHiragana = true;
 			else if (type == KATAKANA_TYPE)
 				tokensHaveKatakana = true;
 			else if (type == HANGUL_TYPE)
 				tokensHaveHangul = true;
			else
 				// we have to do it the hard way
 				readCharsForScript(termAtt.toString());
 		}
 		// capture final state
 		input.end();
 		finalState = captureState();
 	}
 
 	/** linear fast-path for basic latin case */
 	private static final int basicLatin[] = new int[128];
 
 	static {
 	  for (int i = 0; i < basicLatin.length; i++)
 	    basicLatin[i] = UScript.getScript(i);
 	}
 
 	/** fast version of UScript.getScript(). Basic Latin is an array lookup */
 	private static int getScript(int codepoint) {
 	  if (0 <= codepoint && codepoint < basicLatin.length)
 	    return basicLatin[codepoint];
 	  else
 	    return UScript.getScript(codepoint);
 	}
 
 	private void readCharsForScript(String term)
 	{
 		for (int i = 0; i < term.length(); i++)
 		{
 			int cp = term.codePointAt(i);
 			int script = getScript(cp);
 			switch (script) {
 				case UScript.HANGUL:
 					tokensHaveHangul = true;
 					break;
 				case UScript.HIRAGANA:
 					tokensHaveHiragana = true;
 					break;
 				case UScript.KATAKANA:
 					tokensHaveKatakana = true;
 					break;
 				case UScript.HAN:
 					tokensHaveHan = true;
 					break;
 			}
 		}
 	}
 
 }
