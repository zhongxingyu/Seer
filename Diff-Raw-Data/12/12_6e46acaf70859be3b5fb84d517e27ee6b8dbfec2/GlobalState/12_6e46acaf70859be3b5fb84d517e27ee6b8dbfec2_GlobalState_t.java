 /*
  * dfh.grammar -- a recursive descent parser library for Java
  * 
  * Copyright (C) 2012 David F. Houghton
  * 
  * This software is licensed under the LGPL. Please see accompanying NOTICE file
  * and lgpl.txt.
  */
 package dfh.grammar;
 
 import java.io.PrintStream;
 
 /**
  * Immutable* data structure holding match options.
  * <p>
  * <b>Creation date:</b> Apr 3, 2011
  * <p>
  * * For a backwards match a temporary mutable matching cache is created, but
  * this should be treated as immutable.
  * 
  * @author David Houghton
  * 
  */
 public class GlobalState {
 	public final boolean allowOverlap, study, keepRightmost;
 	public final int start, end, rcsEnd;
 	public final PrintStream trace;
 	public final boolean debug;
 	public final int maxDepth;
 	public final CharSequence cs;
 	public final ReversedCharSequence rcs;
 	public final boolean isReversed;
 	public final int length;
 
 	/**
 	 * Constructor called in {@link Grammar} only.
 	 * 
 	 * @param o
 	 * @param containsCycles
 	 */
 	GlobalState(CharSequence cs, Options o) {
 		this(cs, new ReversedCharSequence(cs), false, o.allowOverlap, o.start,
				o.end == -1 || o.end > cs.length() ? cs.length() : o.end,
				o.maxRecursionDepth, o.trace, o.study, o.keepRightmost);
 	}
 
 	private GlobalState(GlobalState gs) {
 		this.cs = gs.cs;
 		this.rcs = gs.rcs;
 		this.isReversed = !gs.isReversed;
 		this.allowOverlap = gs.allowOverlap;
 		this.start = gs.start;
 		this.end = gs.end;
 		this.maxDepth = gs.maxDepth;
 		this.trace = gs.trace;
 		this.keepRightmost = gs.keepRightmost;
 		this.debug = gs.debug;
 		this.rcsEnd = gs.rcsEnd;
 		this.study = gs.study;
 		this.length = this.end - this.start;
 	}
 
 	/**
 	 * @return {@link GlobalState} for use in backwards {@link Assertion}
 	 */
 	GlobalState reverse() {
 		return new GlobalState(this);
 	}
 
 	private GlobalState(CharSequence cs, ReversedCharSequence rcs,
 			boolean isReversed, boolean allowOverlap, int start, int end,
 			int maxDepth, PrintStream trace, boolean study,
 			boolean keepRightmost) {
 		this.cs = cs;
 		this.rcs = rcs;
 		this.isReversed = isReversed;
 		this.allowOverlap = allowOverlap;
 		this.start = start;
 		this.end = end;
 		this.maxDepth = maxDepth;
 		this.trace = trace;
 		this.keepRightmost = keepRightmost;
 		this.debug = trace != null;
 		this.rcsEnd = rcs.translate(start) + 1;
 		this.study = study;
 		this.length = end - start;
 	}
 
 	/**
 	 * Returns character sequence appropriate to current match context:
 	 * {@link #cs} or {@link #rcs}.
 	 * 
 	 * @return character sequence appropriate to current match context
 	 */
 	public CharSequence seq() {
 		return isReversed ? rcs : cs;
 	}
 
 	/**
 	 * Returns end offset appropriate to current match context: {@link #end} or
 	 * {@link #rcsEnd}.
 	 * 
 	 * @return end offset appropriate to current match context
 	 */
 	public int end() {
 		return isReversed ? rcsEnd : end;
 	}
 
 	/*
 	 * for debugging
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "[overlap: " + allowOverlap + "; study: " + study + "; start: "
 				+ start + "; end: " + end + "; debug: " + debug + ']';
 	}
 }
