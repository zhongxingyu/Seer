 /*
  * dfh.grammar -- a recursive descent parser library for Java
  * 
  * Copyright (C) 2012 David F. Houghton
  * 
  * This software is licensed under the LGPL. Please see accompanying NOTICE file
  * and lgpl.txt.
  */
 package dfh.grammar;
 
 import java.io.Serializable;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Implements zero-width assertions. See {@link AssertionFragment}.
  * <p>
  * <b>Creation date:</b> Apr 7, 2011
  * 
  * @author David Houghton
  * 
  */
 @Reversible
 public class Assertion extends Rule implements Serializable, NonterminalRule {
 	private static final long serialVersionUID = 7L;
 	/**
 	 * suffix added to {@link Rule#uid} of reversed rules to signal their
 	 * reversal.
 	 */
 	public static final String REVERSAL_SUFFIX = ":r";
 
 	class AssertionMatcher extends Matcher {
 		private final Map<Integer, CachedMatch>[] cache;
 		private final Map<Integer, CachedMatch> subCache;
 		private final boolean backward;
 
 		private AssertionMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Matcher master) {
 			super(offset, master);
 			this.cache = cache;
 			this.subCache = cache[rule().cacheIndex];
 			backward = false;
 		}
 
 		public AssertionMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Matcher master,
 				GlobalState gs, boolean b) {
 			super(offset, master, gs);
 			this.cache = cache;
 			this.subCache = cache[rule().cacheIndex];
 			backward = b;
 		}
 
 		private boolean fresh = true;
 
 		@Override
 		public Match match() {
 			if (options.debug)
 				Assertion.this.matchTrace(this);
 			if (fresh) {
 				fresh = false;
 				// TODO: should we check cache here at all?
 				CachedMatch cm = subCache.get(offset);
 				if (cm == null) {
 					Match n = r.matcher(
 							backward ? options.rcs.translate(offset) + 1
 									: offset, cache, this).match();
 					if (positive) {
 						if (n != null) {
 							Match next = new Match(Assertion.this, offset,
 									offset);
 							if (backward)
 								n = reverse(n);
 							next.setChildren(new Match[] { n });
 							n = next;
 						}
 					} else {
 						if (n != null)
 							n = null;
 						else
 							n = new Match(Assertion.this, offset, offset);
 					}
 					if (n == null) {
 						subCache.put(offset, CachedMatch.MISMATCH);
 						if (options.debug)
 							Assertion.this.matchTrace(this, null);
 						return null;
 					}
 					// cm = n == null ? CachedMatch.MISMATCH :
 					// CachedMatch.MATCH;
 					// subCache.put(offset, cm);
 					if (options.debug)
 						Assertion.this.matchTrace(this, n);
 					return register(n);
 				} else if (cm == CachedMatch.MISMATCH) {
 					if (options.debug)
 						Assertion.this.matchTrace(this, null);
 					return null;
 				} else if (positive) {
 					Match n;
 					if (backward) {
 						n = r.matcher(0, cache, this).match();
 						n = reverse(n);
 					} else {
 						n = r.matcher(offset, cache, this).match();
 					}
 					Match next = new Match(Assertion.this, offset, offset);
 					next.setChildren(new Match[] { n });
 					return register(next);
 				} else {
 					Match n = new Match(Assertion.this, offset, offset);
 					if (options.debug)
 						Assertion.this.matchTrace(this, n);
 					return register(n);
 				}
 			}
 			if (options.debug)
 				Assertion.this.matchTrace(this, null);
 			return null;
 		}
 
 		/**
 		 * Swaps all members of match tree and adjusts offsets
 		 * 
 		 * @param n
 		 * @return
 		 */
 		private Match reverse(Match n) {
 			if (options.isReversed) {
 				Match reversed = new Match(n.rule(), options.rcs.translate(n
 						.end()) + 1, options.rcs.translate(n.start()) + 1);
 				if (n.children() != null) {
 					Match[] children = new Match[n.children().length];
 					int half = children.length % 2 == 1 ? children.length / 2
 							: -1;
 					for (int i = 0, lim = children.length / 2; i <= lim; i++) {
 						Match m1 = reverse(n.children()[i]);
 						if (i == half)
 							children[i] = m1;
 						else {
 							int j = children.length - i - 1;
 							Match m2 = reverse(n.children()[j]);
 							children[i] = m2;
 							children[j] = m1;
 						}
 					}
 					reversed.setChildren(children);
 				}
 				return reversed;
 			}
 			return n;
 		}
 
 		@Override
 		protected boolean mightHaveNext() {
 			return fresh;
 		}
 
 		@Override
 		protected Rule rule() {
 			return Assertion.this;
 		}
 
 	}
 
 	protected Rule r;
 	protected final boolean positive;
 	protected final boolean forward;
 	private String subDescription;
 
 	public Assertion(Label label, Rule r, boolean positive, boolean forward) {
 		super(label);
 		this.r = r;
 		this.positive = positive;
 		this.forward = forward;
 	}
 
 	@Override
 	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
 			Matcher master) {
 		if (forward) {
 			if (!master.options.study)
 				return new AssertionMatcher(offset, cache, master);
 			GlobalState gs = master.options.unstudy();
 			return new AssertionMatcher(offset, cache, master, gs, false);
 		}
 		GlobalState gs = master.options.reverse();
 		return new AssertionMatcher(offset, cache, master, gs, true);
 	}
 
 	@Override
 	protected String uniqueId() {
 		if (uid != null)
 			return uid;
 		StringBuilder b = new StringBuilder();
 		b.append(positive ? '~' : '!');
 		b.append(forward ? '+' : '-');
 		b.append(r.uniqueId());
 		return b.toString();
 	}
 
 	@Override
 	public String description(boolean inBrackets) {
 		StringBuilder b = new StringBuilder();
 		b.append(positive ? '~' : '!');
 		if (!forward) {
 			b.append('-');
 			b.append(subDescription);
 		} else
 			subDescription(r, b);
 		return wrap(b);
 	}
 
 	@Override
 	protected void postCopy() {
 		if (!forward) {
 			StringBuilder b = new StringBuilder();
 			Assertion.subDescription(r.unreversed, b);
 			subDescription = b.toString();
 		}
 	}
 
 	/**
 	 * Used in construction of text used by {@link #description()} in backwards
 	 * assertions.
 	 * 
 	 * @param r
 	 * @param b
 	 */
 	static void subDescription(Rule r, StringBuilder b) {
 		if (r.generation == -1) {
 			boolean needsBrackets = needsBrackets(r);
 			if (needsBrackets)
 				b.append("[ ");
 			b.append(r.description(true));
 			if (needsBrackets)
 				b.append(" ]");
 		} else
 			b.append(r.label);
 	}
 
 	/**
 	 * @param r
 	 * @return whether the assertion's rule needs brackets in a description
 	 */
 	protected static boolean needsBrackets(Rule r) {
 		if (r instanceof AlternationRule)
 			return true;
 		if (!(r.labels == null || r.labels.isEmpty()))
 			return true;
 		if (r instanceof SequenceRule) {
 			SequenceRule sr = (SequenceRule) r;
 			int count = 0;
 			for (Rule c : sr.sequence) {
 				if (c.label.id.charAt(0) != '.') {
 					count++;
 					if (count > 1)
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public Set<Integer> study(CharSequence s,
 			Map<Integer, CachedMatch>[] cache, GlobalState options) {
 		// non-terminal rules don't study
 		return null;
 	}
 
 	@Override
 	public boolean zeroWidth() {
 		return true;
 	}
 
 	void setSubDescription(String subDescription) {
 		if (this.subDescription != null)
 			throw new GrammarException(
 					"one cannot reset an assertion sub-descriptoin");
 		this.subDescription = subDescription;
 	}
 
 	@Override
 	protected void setCacheIndex(Map<String, Integer> uids) {
 		if (cacheIndex == -1) {
 			Integer i = uids.get(uid());
 			if (i == null) {
 				i = uids.size();
 				uids.put(uid(), i);
 			}
 			cacheIndex = i;
 			r.setCacheIndex(uids);
 		}
 	}
 
 	@Override
 	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
 		if (visited.contains(this))
 			return currentMax;
 		visited.add(this);
 		int max = Math.max(cacheIndex, currentMax);
 		return r.maxCacheIndex(max, visited);
 	}
 
 	@Override
 	protected void rules(Map<String, Rule> map) {
 		if (!map.containsKey(uid())) {
 			map.put(uid(), this);
 			r.rules(map);
 		}
 	}
 
 	@Override
 	protected void fixAlternation() {
 		r.fixAlternation();
 	}
 
 	@Override
 	protected void subRules(Set<Rule> set, Set<Rule> all, boolean explicit) {
 		if (!all.contains(this)) {
 			all.add(this);
 			if (!set.contains(this)) {
 				if (explicit) {
 					if (generation > -1) {
 						set.add(this);
 					}
 					if (unreversed != null)
 						unreversed.subRules(set, all, explicit);
 				} else
 					set.add(this);
 			}
 			r.subRules(set, all, explicit);
 		}
 	}
 
 	@Override
 	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
 		if (!cache.containsKey(uid())) {
 			cache.put(uid(), true);
 			r.mayBeZeroWidth(cache);
 		}
 		return true;
 	}
 
 	@Override
 	public Rule deepCopy(Label l, String nameBase, Map<String, Rule> cycleMap,
 			Set<String> knownLabels, Set<String> knownConditions) {
 		Rule copy = r
 				.deepCopy(nameBase, cycleMap, knownLabels, knownConditions);
 		Assertion ass = new Assertion(l, copy, positive, forward);
 		ass.r = copy;
 		return ass;
 	}
 
 	@Override
 	public Match checkCacheSlip(int i, Match m) {
 		return new Match(r, m.start(), m.end());
 	}
 }
