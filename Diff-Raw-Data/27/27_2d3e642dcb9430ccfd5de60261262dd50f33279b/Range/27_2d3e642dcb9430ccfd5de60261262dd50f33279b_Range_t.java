 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.monitor.client;
 
 import java.util.Stack;
 import java.util.StringTokenizer;
 
 /**
  * The {@code Range} class implements a simple solution to specify ranges of
  * integers. A range may consist of several intervals and single values.
  * Intervals may be open or closed or a mixture of them {@link http
  * ://en.wikipedia.org/wiki/Interval_(mathematics)}
  * <p>
  * Ranges are specified with a reverse polish notation, e.g.
  * <ul>
  * <li>1 3 () -> (1,3): just 2 is within the range</li>
  * <li>1 3 [] -> [1,3]: 1,2 and 3 are within the range</li>
  * <li>1 3 [] 5 8 () -> [1,3] (5,8): 1,2,3,6 and 7 are within the range</li>
  * <li>1 3 [] 9 -> [1,3] 9: 1,2,3 and 9 are within the range</li>
  * <li>etc.</li>
  *</ul>
  */
 public class Range {
 
 	private final Stack<Match> stack = new Stack<Match>();
 
 	private static final String CLOSE_OPEN_TOKEN = "[)"; //$NON-NLS-1$
 	private static final String OPEN_CLOSE_TOKEN = "(]"; //$NON-NLS-1$
 	private static final String CLOSE_CLOSE_TOKEN = "[]"; //$NON-NLS-1$
 	private static final String OPEN_OPEN_TOKEN = "()"; //$NON-NLS-1$
 
 	public Range(final String range) throws ParseException {
 		final StringTokenizer tokenizer = new StringTokenizer(range);
 		String token = null;
 		try {
 			while (tokenizer.hasMoreTokens()) {
 				token = tokenizer.nextToken();
 				if (token.equals(OPEN_OPEN_TOKEN)) {
 					stack.push(new OpenOpen());
 				} else if (token.equals(CLOSE_CLOSE_TOKEN)) {
 					stack.push(new CloseClose());
 				} else if (token.equals(OPEN_CLOSE_TOKEN)) {
 					stack.push(new OpenClose());
 				} else if (token.equals(CLOSE_OPEN_TOKEN)) {
 					stack.push(new CloseOpen());
 				} else {
 					stack.push(new Value(token));
 				}
 			}
 		} catch (Throwable t) {
 			throw new ParseException("Error parsing range '" + range + "' with token '" + token + "'.", t); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 	}
 
 	public boolean matches(final int value) {
 		for (Match match : stack) {
 			if (match.matches(value)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Parsing error exception
 	 */
 	public static class ParseException extends Exception {
 
 		private static final long serialVersionUID = -7362170629769240938L;
 
 		public ParseException(final String string, final Throwable t) {
 			super(string, t);
 		}
 
 	}
 
 	private interface Match {
 		boolean matches(int value);
 	}
 
 	private static class Value implements Match {
 		private final int value;
 
 		public Value(final String token) {
 			value = Integer.valueOf(token);
 		}
 
 		public boolean matches(final int value) {
 			return this.value == value;
 		}
 
 		public int getValue() {
 			return value;
 		}
 	}
 
 	private abstract class Interval implements Match {
 		protected final int lower;
 		protected final int upper;
 
 		public Interval() {
 			upper = Integer.valueOf(((Value) stack.pop()).getValue());
 			lower = Integer.valueOf(((Value) stack.pop()).getValue());
 		}
 	}
 
 	private class OpenOpen extends Interval {
 		public boolean matches(final int value) {
 			return lower < value && value < upper;
 		}
 	}
 
 	private class CloseClose extends Interval {
 		public boolean matches(final int value) {
 			return lower <= value && value <= upper;
 		}
 	}
 
 	private class OpenClose extends Interval {
 		public boolean matches(final int value) {
 			return lower < value && value <= upper;
 		}
 	}
 
 	private class CloseOpen extends Interval {
 		public boolean matches(final int value) {
 			return lower <= value && value < upper;
 		}
 	}
 
 }
