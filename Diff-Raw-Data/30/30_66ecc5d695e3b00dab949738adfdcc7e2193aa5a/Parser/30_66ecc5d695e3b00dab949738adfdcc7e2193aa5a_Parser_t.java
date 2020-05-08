 /**
  * 
  */
 package com.bluespot.parsing;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Iterator;
 
 public class Parser<T extends Token<?>> {
 
 	/**
 	 * @return
 	 */
 	public RuleSet<T> ruleSet() {
 		return new RuleSet<T>();
 	}
 
 	/**
 	 * @param c
 	 * @return
 	 */
 	public Group<T> split(char c) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * @param reader
 	 * @param fileReader
 	 * @return
 	 * @throws IOException
 	 */
 	public Iterator<T> feed(Reader reader) throws IOException {
 		return new TokenIterator();
 	}
 
 	private class TokenIterator implements Iterator<T> {
 
 		@Override
 		public boolean hasNext() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public T next() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("Removal is not supported");
 		}
 
 	}
 }
