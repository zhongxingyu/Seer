 /**
  * 
  */
 package com.bluespot.parsing;
 
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A stream of tokens.
  * 
  * @author Aaron Faanes
  * 
  */
 public class TokenStream<T extends Token<V>, V extends TokenVisitor> {
 
 	private final List<T> tokens = new LinkedList<T>();
 
 	private final Reader reader;
 
 	public TokenStream(String characters) {
 		if (characters == null) {
 			throw new NullPointerException("characters must not be null");
 		}
 		this.reader = new StringReader(characters);
 	}
 
 	public TokenStream(final Reader reader) {
 		if (reader == null) {
 			throw new NullPointerException("reader must not be null");
 		}
 		if (reader.markSupported()) {
 			this.reader = reader;
 		} else {
 			this.reader = new BufferedReader(reader);
 		}
 	}
 
 	public void accept(V visitor) {
 		if (visitor == null) {
 			throw new NullPointerException("visitor must not be null");
 		}
 		for (T token : this.tokens) {
 			token.accept(visitor);
 		}
 	}
 }
