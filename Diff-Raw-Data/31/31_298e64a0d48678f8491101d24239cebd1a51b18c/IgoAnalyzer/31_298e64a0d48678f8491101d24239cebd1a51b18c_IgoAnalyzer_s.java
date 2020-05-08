 /*
  * Copyright (c) 2011, Shige Takeda All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, 
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice, 
  * this list of conditions and the following disclaimer in the documentation 
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the Shige Takeda nor the names of its contributors 
  * may be used to endorse or promote products derived from this software 
  * without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.mcontents.lucene.analysis;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 
 import net.reduls.igo.Tagger;
 
 import org.apache.lucene.analysis.ASCIIFoldingFilter;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.LowerCaseFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardFilter;
 
 /*
  * Igo Analyzer
  */
 abstract public class IgoAnalyzer extends Analyzer {
 	private String curdir;
 	private Tagger tagger = null;
 
 	public IgoAnalyzer() {
 		this.curdir = System.getenv("PWD");
 	}
 
 	/**
 	 * Returns the dictionary name
 	 * 
 	 * @return dictionary name
 	 */
 	abstract protected String getDictionaryName();
 
 	/**
 	 * Generate {@link TokenStream}
 	 * 
 	 * @params fieldName lucene field name
 	 * @params reader source of text in {@link Reader}
 	 * @return {@link TokenStream} instance
 	 */
 	@Override
 	public final TokenStream tokenStream(String fieldName, Reader reader) {
 		if (this.tagger == null) {
 			try {
 				this.tagger = new Tagger(this.curdir + "/"
 						+ getDictionaryName());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
 		}
 
 		try {
 			TokenStream stream = new IpadicTokenizer2(tagger, reader);
 			stream = new ASCIIFoldingFilter(stream);
 			stream = new StandardFilter(stream); // standard filter
 			stream = new LowerCaseFilter(stream); // lower case filter
 			stream = new JaStopWordFilter(stream); // stop word filter
 
 			return stream;
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * returns reusable {@link TokenStream}
 	 * 
 	 * @params fieldName lucene field name
 	 * @params reader source of text
 	 * @return {@link TokenStream} instance
 	 */
 	@Override
 	public final TokenStream reusableTokenStream(String fieldName, Reader reader)
 			throws IOException {
 		TokenStreamInfo info = (TokenStreamInfo) getPreviousTokenStream();
 
 		if (info == null) {
 			info = new TokenStreamInfo();
 
 			IpadicTokenizer2 tokenizer = new IpadicTokenizer2(tagger, reader);
 			info.tokenizer = tokenizer;
 
 			TokenStream filter = null;
 			filter = new ASCIIFoldingFilter(tokenizer);
 			filter = new StandardFilter(filter);
 			filter = new LowerCaseFilter(filter);
 			filter = new JaStopWordFilter(filter);
 			info.filter = filter;
 
 			setPreviousTokenStream(info);
 		} else {
 			if (info.filter != null) {
 				info.filter.reset();
 			}
 			if (info.tokenizer != null) {
 				info.tokenizer.reset(reader);
 			}
 		}
 
 		if (info.filter != null) {
 			return info.filter;
 		} else {
 			return info.tokenizer;
 		}
 	}
 
 	/**
 	 * token stream cache
 	 * 
 	 */
 	private static class TokenStreamInfo {
 		public IpadicTokenizer2 tokenizer;
 		public TokenStream filter;
 	}
 
 }
