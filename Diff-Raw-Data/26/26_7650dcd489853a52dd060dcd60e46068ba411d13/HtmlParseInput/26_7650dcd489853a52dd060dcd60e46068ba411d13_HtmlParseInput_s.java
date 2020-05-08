 /*******************************************************************************
  * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.ecommons.text;
 
 import java.util.HashMap;
 
 
 /**
  * Parse input stripping out HTML markups and encoding and providing the text content.
  */
 public class HtmlParseInput extends SourceParseInput implements CharSequence {
 	
 	
 	private static final int BUFFER_LENGTH = 1024;
 	private static final int BUFFER_PREFETCH = 1008;
 	
 	
 	private static final HashMap<String, Character> gEntityTable;
 	static {
 		gEntityTable = new HashMap<String,Character>();
		final String[] entityName = {"zwnj","aring","gt","yen","ograve","Chi","delta","rang","sup","trade","Ntilde","xi","upsih","nbsp","Atilde","radic","otimes","aelig","oelig","equiv","ni","infin","Psi","auml","cup","Epsilon","otilde","lt","Icirc","Eacute","Lambda","sbquo","Prime","prime","psi","Kappa","rsaquo","Tau","uacute","ocirc","lrm","zwj","cedil","Alpha","not","amp","AElig","oslash","acute","lceil","alefsym","laquo","shy","loz","ge","Igrave","nu","Ograve","lsaquo","sube","euro","rarr","sdot","rdquo","Yacute","lfloor","lArr","Auml","Dagger","brvbar","Otilde","szlig","clubs","diams","agrave","Ocirc","Iota","Theta","Pi","zeta","Scaron","frac14","egrave","sub","iexcl","frac12","ordf","sum","prop","Uuml","ntilde","atilde","asymp","uml","prod","nsub","reg","rArr","Oslash","emsp","THORN","yuml","aacute","Mu","hArr","le","thinsp","dArr","ecirc","bdquo","Sigma","Aring","tilde","nabla","mdash","uarr","times","Ugrave","Eta","Agrave","chi","real","circ","eth","rceil","iuml","gamma","lambda","harr","Egrave","frac34","dagger","divide","Ouml","image","ndash","hellip","igrave","Yuml","ang","alpha","frasl","ETH","lowast","Nu","plusmn","bull","sup1","sup2","sup3","Aacute","cent","oline","Beta","perp","Delta","there4","pi","iota","empty","euml","notin","iacute","para","epsilon","weierp","OElig","uuml","larr","icirc","Upsilon","omicron","upsilon","copy","Iuml","Oacute","Xi","kappa","ccedil","Ucirc","cap","mu","scaron","lsquo","isin","Zeta","minus","deg","and","tau","pound","curren","int","ucirc","rfloor","ensp","crarr","ugrave","exist","cong","theta","oplus","permil","Acirc","piv","Euml","Phi","Iacute","quot","Uacute","Omicron","ne","iquest","eta","rsquo","yacute","Rho","darr","Ecirc","Omega","acirc","sim","phi","sigmaf","macr","thetasym","Ccedil","ordm","uArr","forall","beta","fnof","rho","micro","eacute","omega","middot","Gamma","rlm","lang","spades","supe","thorn","ouml","or","raquo","part","sect","ldquo","hearts","sigma","oacute"};
		final char[] entityChar = { 8204,229,62,165,242,935,948,9002,8835,8482,209,958,978,160,195,8730,8855,230,339,8801,8715,8734,936,228,8746,917,245,60,206,201,923,8218,8243,8242,968,922,8250,932,250,244,8206,8205,184,913,172,38,198,248,180,8968,8501,171,173,9674,8805,204,957,210,8249,8838,8364,8594,8901,8221,221,8970,8656,196,8225,166,213,223,9827,9830,224,212,921,920,928,950,352,188,232,8834,161,189,170,8721,8733,220,241,227,8776,168,8719,8836,174,8658,216,8195,222,255,225,924,8660,8804,8201,8659,234,8222,931,197,732,8711,8212,8593,215,217,919,192,967,8476,710,240,8969,239,947,955,8596,200,190,8224,247,214,8465,8211,8230,236,376,8736,945,8260,208,8727,925,177,8226,185,178,179,193,162,8254,914,8869,916,8756,960,953,8709,235,8713,237,182,949,8472,338,252,8592,238,933,959,965,169,207,211,926,954,231,219,8745,956,353,8216,8712,918,8722,176,8743,964,163,164,8747,251,8971,8194,8629,249,8707,8773,952,8853,8240,194,982,203,934,205,34,218,927,8800,191,951,8217,253,929,8595,202,937,226,8764,966,962,175,977,199,186,8657,8704,946,402,961,181,233,969,183,915,8207,9001,9824,8839,254,246,8744,187,8706,167,8220,9829,963,243};
 		for (int i = 0; i < entityName.length; i++) {
 			gEntityTable.put(entityName[i], Character.valueOf(entityChar[i]));
 		}
 		// special-case nbsp to a simple space instead of 0xa0
 		gEntityTable.put("nbsp",new Character(' '));
 	}
 	
 	
 	private char[] fContent;
 	private int fIndexInContent;
 	private final char[] fBuffer = new char[BUFFER_LENGTH];
 	private final int[] fBufferCharIndex = new int[BUFFER_LENGTH];
 	private final int[] fBufferCharLength = new int[BUFFER_LENGTH+1];
 	private int fBufferSize;
 	
 	
 	public HtmlParseInput() {
 		fContent = new char[0];
 		fBufferCharLength[0] = 0;
 	}
 	
 	public HtmlParseInput(final String code) {
 		fContent = code.toCharArray();
 		fBufferCharLength[0] = 0;
 	}
 	
 	
 	public void reset(final String code) {
 		fContent = code.toCharArray();
 		fIndexInContent = 0;
 		fBufferSize = 0;
 		fBufferCharIndex[0] = 0;
 		fBufferCharLength[0] = 0;
 		init();
 	}
 	
 	
 	@Override
 	protected void updateBuffer(int min) {
 		min = Math.min(Math.max(min, BUFFER_PREFETCH), BUFFER_LENGTH);
 		int index = getIndex();
 		final int indexInBuffer = getIndexInBuffer();
 		if (indexInBuffer > 0) {
 			if (indexInBuffer < fBufferSize) {
 				fBufferSize -= indexInBuffer;
 				System.arraycopy(fBuffer, indexInBuffer, fBuffer, 0, fBufferSize);
 				System.arraycopy(fBufferCharIndex, indexInBuffer, fBufferCharIndex, 0, fBufferSize);
 				System.arraycopy(fBufferCharLength, indexInBuffer, fBufferCharLength, 0, fBufferSize+1);
 			}
 			else {
 				fBufferCharLength[0] = fBufferCharLength[fBufferSize];
 				fBufferSize = 0;
 			}
 		}
 		
 		while (fBufferSize < min && fIndexInContent < fContent.length) {
 			index = fBufferCharIndex[fBufferSize] = fIndexInContent;
 			final char c = fContent[fIndexInContent++];
 			switch (c) {
 			case '<':
 				if (!readTag()) {
 					fBuffer[fBufferSize++] = c;
 				}
 				break;
 			case '&':
 				if (!readEntity()) {
 					fBuffer[fBufferSize++] = c;
 				}
 				break;
 			default:
 				fBuffer[fBufferSize++] = c;
 				break;
 			}
 			fBufferCharLength[fBufferSize] = fIndexInContent-index;
 		}
 		setBuffer(fBuffer, fBufferSize, 0);
 	}
 	
 	private boolean readEntity() {
 		int idx = fIndexInContent;
 		if (idx >= fContent.length) {
 			return false;
 		}
 		char c = fContent[idx++];
 		if (c == '#') {
 			if (idx >= fContent.length) {
 				return false;
 			}
 			c = fContent[idx++];
 			if (c == 'x') {
 				if (idx >= fContent.length) {
 					return false;
 				}
 				c = fContent[idx++];
 				if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <='f')) {
 					while (idx < fContent.length) {
 						c = fContent[idx++];
 						if (c == ';') {
 							try {
 								fBuffer[fBufferSize++] = (char) Integer.parseInt(new String(fContent,
 										fIndexInContent+2, idx-fIndexInContent-3), 16);
 								fIndexInContent = idx;
 								return true;
 							}
 							catch (final NumberFormatException e) {
 								break;
 							}
 						}
 						if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <='f')) {
 							continue;
 						}
 						break;
 					}
 				}
 				return false;
 			}
 			if (c >= '0' && c <= '9') {
 				while (idx < fContent.length) {
 					c = fContent[idx++];
 					if (c == ';') {
 						try {
 							fBuffer[fBufferSize++] = (char) Integer.parseInt(new String(fContent,
 									fIndexInContent+1, idx-fIndexInContent-2), 10);
 							fIndexInContent = idx;
 							return true;
 						}
 						catch (final NumberFormatException e) {
 							break;
 						}
 					}
 					if (c >='0' && c <= '9') {
 						continue;
 					}
 					break;
 				}
 			}
 			return false;
 		}
 		if (c >= 'a' && c <= 'z') {
 			while (idx < fContent.length) {
 				c = fContent[idx++];
 				if (c == ';') {
 					final Character value = gEntityTable.get(new String(fContent, fIndexInContent, idx-fIndexInContent-1));
 					if (value != null) {
 						fBuffer[fBufferSize++] = value.charValue();
 						fIndexInContent = idx;
 						return true;
 					}
 					else {
 						break;
 					}
 				}
 				if (c >= 'a' && c <= 'z') {
 					continue;
 				}
 				break;
 			}
 			return false;
 		}
 		return false;
 	}
 	
 	private boolean readTag() {
 		int idx = fIndexInContent;
 		if (idx >= fContent.length) {
 			return false;
 		}
 		char c = fContent[idx++];
 		if (c == '!') {
 			if (idx >= fContent.length) {
 				return false;
 			}
 			c = fContent[idx++];
 			if (c == '-') {
 				if (idx >= fContent.length) {
 					return false;
 				}
 				c = fContent[idx++];
 				if (c == '-') {
 					while (idx < fContent.length) {
 						c = fContent[idx++];
 						if (c == '-') {
 							while (idx < fContent.length) {
 								c = fContent[idx++];
 								if (c == '>') {
 									fIndexInContent = idx;
 									return true;
 								}
 								if (c == '-') {
 									continue;
 								}
 								break;
 							}
 						}
 					}
 				}
 			}
 			while (idx < fContent.length) {
 				c = fContent[idx++];
 				if (c == '>') {
 					fIndexInContent = idx;
 					return true;
 				}
 			}
 			return false;
 		}
 		if (c == '?') {
 			while (idx < fContent.length) {
 				c = fContent[idx++];
 				if (c == '?') {
 					while (idx < fContent.length) {
 						c = fContent[idx++];
 						if (c == '>') {
 							fIndexInContent = idx;
 							return true;
 						}
 						if (c == '?') {
 							continue;
 						}
 						break;
 					}
 				}
 			}
 			return false;
 		}
 		while (idx < fContent.length) {
 			c = fContent[idx++];
 			if (c == '>') {
 				fIndexInContent = idx;
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	@Override
 	public void consume(final int num) {
 		setConsume(num, fBufferCharIndex[getIndexInBuffer()+num]);
 	}
 	
 	@Override
 	public void consume(final int num, final int length) {
 		int indexInBuffer = getIndexInBuffer() + num;
 		if (indexInBuffer <= fBufferSize) {
 			updateBuffer(num+1);
 			indexInBuffer = getIndexInBuffer() + num;
 		}
 		if (indexInBuffer < fBufferSize) {
 			setConsume(num, fBufferCharIndex[indexInBuffer]);
 		}
 		else if (indexInBuffer > 0) {
 			setConsume(num, fBufferCharIndex[indexInBuffer-1]+fBufferCharLength[indexInBuffer]);
 		}
 		else {
 			setConsume(num, fContent.length);
 		}
 	}
 	
 	@Override
 	public int getLength(final int num) {
 		if (num == 0) {
 			return 0;
 		}
 		final int indexInBuffer = getIndexInBuffer();
 		return (fBufferCharIndex[indexInBuffer+num-1] + fBufferCharLength[indexInBuffer+num] -
 				fBufferCharIndex[indexInBuffer]);
 	}
 	
 	
 	public int length() {
 		return fContent.length;
 	}
 	
 	public char charAt(final int index) {
 		return fContent[index];
 	}
 	
 	public CharSequence subSequence(final int start, final int end) {
 		return new String(fContent, start, end-start);
 	}
 	
 }
