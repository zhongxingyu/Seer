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
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * The object implementing rules such as
  * 
  * <pre>
  * &lt;a&gt; = &lt;b&gt; | &lt;c&gt;
  * </pre>
  * 
  * @author David Houghton
  * 
  */
 @Reversible
 public class AlternationRule extends Rule implements Serializable,
 		NonterminalRule {
 	private static final long serialVersionUID = 8L;
 
 	private class AlternationMatcher extends NonterminalMatcher {
 		int index = 0;
 		Matcher mostRecent = null;
 
 		public AlternationMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Matcher master) {
 			super(offset, cache, AlternationRule.this, master);
 		}
 
 		@Override
 		protected void fetchNext() {
 			if (mostRecent == null) {
 				mostRecent = alternates[index].matcher(offset, cache, this);
 			}
 			Match child = null;
 			boolean test;
 			OUTER: while ((test = mostRecent.mightHaveNext())
 					|| index < alternates.length) {
 				// check condition
 				while (test) {
 					child = mostRecent.match();
 					if (child != null)
 						break OUTER;
 					test = mostRecent.mightHaveNext();
 				}
 				if (++index == alternates.length)
 					break;
 				mostRecent = alternates[index].matcher(offset, cache, this);
 			}
 			if (child == null) {
 				done = true;
 				next = null;
 			} else {
 				next = new Match(AlternationRule.this, offset, child.end());
 				Match[] children = new Match[] { child };
 				next.setChildren(children);
 			}
 		}
 	}
 
 	protected final Rule[] alternates;
 	Map<String, Set<String>> tagMap;
 
 	/**
 	 * Generates a rule from the given label and alternates.
 	 * 
 	 * @param label
 	 * @param alternates
 	 * @param tagMap
 	 */
 	public AlternationRule(Label label, Rule[] alternates,
 			Map<String, Set<String>> tagMap) {
 		super(label);
 		this.alternates = alternates;
 		this.tagMap = tagMap;
 	}
 
 	@Override
 	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
 			Matcher master) {
 		return new AlternationMatcher(offset, cache, master);
 	}
 
 	@Override
 	protected String uniqueId() {
 		StringBuilder b = new StringBuilder();
 		b.append('[');
 		boolean nonInitial = false;
 		for (Rule r : alternates) {
 			if (nonInitial)
 				b.append('|');
 			else
 				nonInitial = true;
 			b.append(r.uniqueId());
 		}
 		b.append(']');
 		return b.toString();
 	}
 
 	@Override
 	public String description(boolean inBrackets) {
 		StringBuilder b = new StringBuilder();
 		boolean nonInitial = false;
 		for (Rule r : alternates) {
 			Set<String> tags;
 			if (r instanceof CyclicRule)
 				tags = tagMap.get(((CyclicRule) r).r.uniqueId());
			else if (r instanceof DeferredDefinitionRule) {
				DeferredDefinitionRule ddr = (DeferredDefinitionRule) r;
				tags = tagMap.get(ddr.r == null ? ddr.uniqueId() : ddr.r.uniqueId());
			} else
 				tags = tagMap.get(r.uniqueId());
 			if (nonInitial)
 				b.append(" | ");
 			else
 				nonInitial = true;
 			if (!(tags == null || tags.isEmpty())) {
 				b.append("[{");
 				boolean ni2 = false;
 				for (String label : tags) {
 					if (ni2)
 						b.append(',');
 					else
 						ni2 = true;
 					b.append(label);
 				}
 				b.append("} ");
 			}
 			if (r.generation == -1) {
 				b.append(r.description(false));
 			} else
 				b.append(r.label());
 			if (!(tags == null || tags.isEmpty())) {
 				b.append(" ]");
 			}
 		}
 		return wrap(b);
 	}
 
 	@Override
 	public Set<Integer> study(CharSequence s,
 			Map<Integer, CachedMatch>[] cache, GlobalState options) {
 		// non-terminal rules don't study
 		return null;
 	}
 
 	@Override
 	public boolean zeroWidth() {
 		for (Rule r : alternates) {
 			if (r.zeroWidth())
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void addLabels(Match match, Set<String> labels) {
 		Collection<String> c = tagMap.get(match.rule().uid());
 		if (c != null)
 			labels.addAll(c);
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
 			for (Rule r : alternates)
 				r.setCacheIndex(uids);
 		}
 
 	}
 
 	@Override
 	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
 		if (visited.contains(this))
 			return currentMax;
 		visited.add(this);
 		int max = Math.max(cacheIndex, currentMax);
 		for (Rule r : alternates)
 			max = Math.max(max, r.maxCacheIndex(max, visited));
 		return max;
 	}
 
 	@Override
 	protected void rules(Map<String, Rule> map) {
 		if (!map.containsKey(uid())) {
 			map.put(uid(), this);
 			for (Rule r : alternates)
 				r.rules(map);
 		}
 	}
 
 	@Override
 	protected void fixAlternation() {
 		for (Rule r : alternates) {
 			if (r instanceof CyclicRule) {
 				Set<String> set = tagMap.remove(r.uid());
 				if (set != null) {
 					r = ((CyclicRule) r).r;
 					tagMap.put(r.uid(), set);
 				}
 			} else if (r instanceof DeferredDefinitionRule) {
 				Set<String> set = tagMap.remove(r.uid());
 				if (set != null) {
 					r = ((DeferredDefinitionRule) r).r;
 					tagMap.put(r.uid(), set);
 				}
 			}
 			r.fixAlternation();
 		}
 	}
 
 	@Override
 	protected void subRules(Set<Rule> set, Set<Rule> all, boolean explicit) {
 		if (!all.contains(this)) {
 			all.add(this);
 			if (!(set.contains(this))) {
 				if (explicit) {
 					if (generation > -1) {
 						set.add(this);
 					}
 					if (unreversed != null)
 						unreversed.subRules(set, all, explicit);
 				} else
 					set.add(this);
 				for (Rule r : alternates)
 					r.subRules(set, all, explicit);
 			}
 		}
 	}
 
 	@Override
 	protected void initialRules(Set<String> initialRules) {
 		if (!initialRules.contains(uid())) {
 			initialRules.add(uid());
 			for (Rule r : alternates)
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
 			cache.put(uid(), null);
 			boolean anyZero = false;
 			for (Rule r : alternates) {
 				anyZero |= r.mayBeZeroWidth(cache);
 				if (anyZero)
 					cache.put(uid(), true);
 			}
 			cache.put(uid(), anyZero);
 			return anyZero;
 		}
 	}
 
 	// @Override
 	// public Set<String> conditionNames() {
 	// if (c != null)
 	// return c.conditionNames();
 	// return super.conditionNames();
 	// }
 
 	@Override
 	protected Rule deepCopy(Label l, String nameBase,
 			Map<String, Rule> cycleMap, Set<String> knownLabels,
 			Set<String> knownConditions) {
 		Map<String, Set<String>> tmCopy = new HashMap<String, Set<String>>(
 				tagMap.size() * 2);
 		Rule[] copies = new Rule[alternates.length];
 		for (int i = 0; i < copies.length; i++) {
 			Rule r = alternates[i];
 			Set<String> copySet = new HashSet<String>(tagMap.get(r.uid()));
 			Rule copy = cycleMap.get(r.label().id);
 			if (copy == null)
 				copy = r.deepCopy(nameBase, cycleMap, knownLabels,
 						knownConditions);
 			tmCopy.put(copy.uid(), copySet);
 			copies[i] = copy;
 		}
 		AlternationRule r = new AlternationRule(l, copies, tmCopy);
 		return r;
 	}
 
 	@Override
 	protected boolean findLeftCycle(Rule sought, Set<Rule> cycleCache) {
 		cycleCache.add(this);
 		for (Rule o : alternates) {
 			if (o == sought)
 				return true;
 			if (cycleCache.contains(o))
 				continue;
 			cycleCache.add(o);
 			if (o instanceof CyclicRule) {
 				CyclicRule cr = (CyclicRule) o;
 				if (cr.r == sought)
 					return true;
 				if (cr.r.findLeftCycle(sought, cycleCache))
 					return true;
 			} else if (o.findLeftCycle(sought, cycleCache))
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Match checkCacheSlip(int i, Match m) {
 		for (Rule r : alternates) {
 			while (r instanceof DeferredDefinitionRule)
 				r = ((DeferredDefinitionRule) r).r;
 			if (r.cacheIndex == m.rule().cacheIndex)
 				return new Match(r, m.start(), m.end());
 		}
 		return null;
 	}
 }
