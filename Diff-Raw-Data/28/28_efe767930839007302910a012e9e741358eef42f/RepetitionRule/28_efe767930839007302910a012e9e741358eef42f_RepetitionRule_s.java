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
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Rule to handle all the various repetition options.
  * <p>
  * <b>Creation date:</b> Mar 16, 2011
  * 
  * @author David Houghton
  * 
  */
 @Reversible
 public class RepetitionRule extends Rule implements Serializable,
 		NonterminalRule {
 	private static final long serialVersionUID = 6L;
 	Rule r;
 	final Repetition repetition;
 	protected Condition c;
 	final Set<String> alternateTags;
 
 	private abstract class RepetitionMatcher extends NonterminalMatcher {
 		protected LinkedList<Match> matched;
 		protected LinkedList<Matcher> matchers;
 
 		public RepetitionMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
 			super(offset, cache, RepetitionRule.this, master);
 		}
 
 		protected boolean grab() {
 			int start = matched.isEmpty() ? offset : matched.peekLast().end();
 			Matcher m;
 			if (matchers.size() > matched.size())
 				m = matchers.peekLast();
 			else {
 				m = r.matcher(start, cache, this);
 				matchers.add(m);
 			}
 			Match n = m.match();
 			if (n == null) {
 				matchers.removeLast();
 				return true;
 			} else {
 				matched.add(n);
 				return false;
 			}
 		}
 	}
 
 	private abstract class GreedyAndPossessive extends RepetitionMatcher {
 
 		private final boolean backtracks;
 
 		protected GreedyAndPossessive(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Label label,
 				boolean backtracks, Matcher master) {
 			super(offset, cache, label, master);
 			this.backtracks = backtracks;
 		}
 
 		protected void initialize() {
 			matched = new LinkedList<Match>();
 			matchers = new LinkedList<Matcher>();
 			while (matched.size() < repetition.top) {
 				if (grab())
 					break;
 			}
 			if (matched.size() < repetition.bottom) {
 				matched.clear();
 				if (backtracks) {
 					matchers.clear();
 					matchers = null;
 				}
 			} else if (!backtracks) {
 				matchers.clear();
 				matchers = null;
 			}
 		}
 	}
 
 	/**
 	 * <b>Creation date:</b> Mar 17, 2011
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	private class GreedyMatcher extends GreedyAndPossessive {
 		protected GreedyMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
 			super(offset, cache, label, true, master);
 		}
 
 		@Override
 		protected void fetchNext() {
 			while (true) {
 				if (matched == null)
 					initialize();
 				else if (matched.isEmpty()) {
 					matchers = null;
 					next = null;
 					done = true;
 					return;
 				} else {
 					matched.removeLast();
 					// see if we can find some other way forward
 					if (matchers.peekLast().mightHaveNext()) {
 						while (!(matchers.isEmpty() || matched.size() == repetition.top)) {
 							if (grab())
 								break;
 						}
 					} else {
 						matchers.removeLast();
 						if (!matchers.isEmpty()
 								&& matchers.peekLast().mightHaveNext())
 							continue;
 					}
 				}
 				if (matched.size() < repetition.bottom) {
 					matched.clear();
 					matchers = null;
 					next = null;
 					done = true;
 				} else {
 					Match[] children = matched
 							.toArray(new Match[matched.size()]);
 					next = new Match(RepetitionRule.this, offset);
 					next.setChildren(children);
 					next.setEnd(matched.isEmpty() ? offset : matched.peekLast()
 							.end());
 					if (testCondition(c, next))
 						break;
 				}
 			}
 		}
 
 	}
 
 	private class StingyMatcher extends RepetitionMatcher {
 		private int goal;
 
 		protected StingyMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
 			super(offset, cache, label, master);
 			matchers = new LinkedList<Matcher>();
 			matched = new LinkedList<Match>();
 			goal = repetition.bottom;
 			seekGoal();
 			if (next == null) {
 				done = true;
 				matched = null;
 				matchers = null;
 			} else if (!testCondition(c, next))
 				fetchNext();
 		}
 
 		private void seekGoal() {
 			boolean found = true;
 			next = null;
 			OUTER: while (matched.size() < goal) {
 				if (grab()) {
 					if (matchers.isEmpty()) {
 						found = false;
 						break;
 					} else {
 						while (!matchers.peekLast().mightHaveNext()) {
 							if (matchers.size() == 1) {
 								found = false;
 								done = true;
 								matchers = null;
 								matched = null;
 								break OUTER;
 							} else {
 								matchers.removeLast();
 								matched.removeLast();
 							}
 						}
 					}
 				}
 			}
 			if (found) {
 				next = new Match(RepetitionRule.this, offset);
 				Match[] children = matched.toArray(new Match[matched.size()]);
 				next.setChildren(children);
 				if (matched.isEmpty())
 					next.setEnd(offset);
 				else
 					next.setEnd(matched.peekLast().end());
 			}
 		}
 
 		@Override
 		protected void fetchNext() {
 			while (true) {
 				if (goal == 0)
 					goal = 1;
 				else
 					matched.removeLast();
 				while (goal <= repetition.top) {
 					seekGoal();
 					if (done)
 						return;
 					if (matchers.isEmpty())
 						goal++;
 					else
 						break;
 				}
 				if (goal > repetition.top) {
 					done = true;
 					matchers = null;
 					matched = null;
 					break;
 				} else if (testCondition(c, next))
 					break;
 			}
 		}
 	}
 
 	private class PossessiveMatcher extends GreedyAndPossessive {
 
 		protected PossessiveMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
 			super(offset, cache, label, false, master);
 		}
 
 		@Override
 		protected void fetchNext() {
 			next = null;
 			if (matched == null) {
 				initialize();
 				if (matched.size() >= repetition.bottom) {
 					next = new Match(RepetitionRule.this, offset);
 					Match[] children = matched
 							.toArray(new Match[matched.size()]);
 					next.setChildren(children);
 					next.setEnd(matched.isEmpty() ? offset : matched.peekLast()
 							.end());
 					if (!(testCondition(c, next))) {
 						next = null;
 						done = true;
 						matched = null;
 					}
 				} else {
 					done = true;
 					matched = null;
 				}
 			} else {
 				done = true;
 				matched = null;
 			}
 		}
 	}
 
 	/**
 	 * Generates a repetition rule with the given state.
 	 * 
 	 * @param label
 	 * @param r
 	 * @param rep
 	 * @param alternateTags
 	 */
 	public RepetitionRule(Label label, Rule r, Repetition rep,
 			Set<String> alternateTags) {
 		super(label);
 		this.r = r;
 		this.repetition = rep;
 		this.alternateTags = alternateTags;
 	}
 
 	@Override
 	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
 			Matcher master) {
 		switch (repetition.t) {
 		case possessive:
 			return new PossessiveMatcher(offset, cache, label, master);
 		case stingy:
 			return new StingyMatcher(offset, cache, label, master);
 		default:
 			return new GreedyMatcher(offset, cache, label, master);
 		}
 	}
 
 	@Override
 	protected String uniqueId() {
 		StringBuilder b = new StringBuilder();
 		b.append(r.uniqueId()).append(repetition);
 		if (condition != null)
 			b.append('(').append(condition).append(')');
 		return b.toString();
 	}
 
 	@Override
 	public String description(boolean inBrackets) {
 		StringBuilder b = new StringBuilder();
 		boolean hasTags = !(alternateTags == null || alternateTags.isEmpty());
		boolean requiresBrackets = r instanceof SequenceRule || r instanceof AlternationRule || hasTags && !inBrackets;
 		if (requiresBrackets)
 			b.append('[');
 		if (hasTags) {
 			b.append('{');
 			boolean ni2 = false;
 			for (String label : alternateTags) {
 				if (ni2)
 					b.append(',');
 				else
 					ni2 = true;
 				b.append(label);
 			}
 			b.append("} ");
 		} else if (requiresBrackets)
 			b.append(' ');
 		if (r.generation == -1) {
 			if ((r instanceof SequenceRule || r instanceof AlternationRule)
 					&& !(inBrackets && repetition.redundant())) {
 				String d = r.description(true);
 				b.append(d);
 			} else
 				b.append(r.description(false));
 		} else
 			b.append(r.label());
 		if (requiresBrackets)
 			b.append(" ]");
 		b.append(repetition);
 		b = new StringBuilder(wrap(b));
 		if (c != null)
 			b.append(" (").append(c.describe()).append(')');
 		return b.toString();
 	}
 
 	@Override
 	public Set<Integer> study(CharSequence s,
 			Map<Integer, CachedMatch>[] cache, GlobalState options) {
 		// non-terminal rules don't study
 		return null;
 	}
 
 	@Override
 	public boolean zeroWidth() {
 		return r.zeroWidth() || repetition.bottom == 0;
 	}
 
 	@Override
 	public Rule conditionalize(Condition c, String id) {
 		if (this.c == null) {
 			this.c = c;
 			this.condition = id;
 		} else {
 			if (this.c instanceof LogicalCondition) {
 				if (!((LogicalCondition) this.c).replace(id, c))
 					throw new GrammarException("could not define " + id
 							+ " in this condition");
 			} else if (this.c instanceof LeafCondition) {
 				LeafCondition lc = (LeafCondition) this.c;
 				if (lc.cnd.equals(id))
 					this.c = c;
 				else
 					throw new GrammarException("rule " + this
 							+ " does not carry condition " + id);
 			} else
 				throw new GrammarException("condition on rule " + this
 						+ " cannot be redefined");
 		}
 		return this;
 	}
 
 	@Override
 	protected void addLabels(Match match, Set<String> labels) {
 		labels.addAll(alternateTags);
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
 				r.subRules(set, all, explicit);
 			}
 		}
 	}
 
 	@Override
 	protected void initialRules(Set<String> initialRules) {
 		if (!initialRules.contains(uid())) {
 			initialRules.add(uid());
 			r.initialRules(initialRules);
 		}
 	}
 
 	@Override
 	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
 		if (cache.containsKey(uid())) {
 			Boolean b = cache.get(uid());
 			if (b == null) {
 				// recursion; we bail
 				b = true;
 				cache.put(uid(), b);
 			}
 			return b;
 		} else {
 			if (repetition.bottom == 0) {
 				Boolean b = true;
 				cache.put(uid(), b);
 				r.mayBeZeroWidth(cache);
 				return b;
 			} else {
 				// mark rule as visited in case there's recursion
 				cache.put(uid(), null);
 				Boolean b = r.mayBeZeroWidth(cache);
 				cache.put(uid(), b);
 				return b;
 			}
 		}
 	}
 
 	@Override
 	public Set<String> conditionNames() {
 		if (c != null)
 			return c.conditionNames();
 		return super.conditionNames();
 	}
 
 	@Override
 	public Rule deepCopy(Label l, String nameBase, Map<String, Rule> cycleMap,
 			Set<String> knownLabels, Set<String> knownConditions) {
 		Set<String> atCopy = new HashSet<String>(alternateTags);
 		Rule copy = r
 				.deepCopy(nameBase, cycleMap, knownLabels, knownConditions);
 		RepetitionRule rr = new RepetitionRule(l, copy, repetition, atCopy);
 		if (c != null) {
 			rr.condition = knownConditions.contains(condition) ? nameBase + ':'
 					+ condition : condition;
 			rr.c = c.copy(nameBase, knownConditions);
 		}
 		return rr;
 	}
 
 	@Override
 	protected boolean findLeftCycle(Rule sought, Set<Rule> cycleCache) {
 		cycleCache.add(this);
 		if (r == sought)
 			return true;
 		if (!cycleCache.contains(r)) {
 			cycleCache.add(r);
 			if (r instanceof CyclicRule) {
 				CyclicRule cr = (CyclicRule) r;
 				if (cr.r == sought)
 					return true;
 				if (cr.r.findLeftCycle(sought, cycleCache))
 					return true;
 			} else if (r.findLeftCycle(sought, cycleCache))
 				return true;
 		}
 		return false;
 	}
 }
