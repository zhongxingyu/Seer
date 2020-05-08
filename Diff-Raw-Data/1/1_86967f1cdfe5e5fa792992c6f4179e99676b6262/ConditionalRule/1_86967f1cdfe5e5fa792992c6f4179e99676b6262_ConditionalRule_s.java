 package dfh.grammar;
 
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Applies a condition to a {@link Rule}.
  * <p>
  * 
  * @author David F. Houghton - Jul 4, 2012
  * 
  */
 @Reversible
 public class ConditionalRule extends Rule {
 
 	private class ConditionalMatcher extends NonterminalMatcher {
 
 		private final Matcher m;
 
 		protected ConditionalMatcher(Integer offset,
 				Map<Integer, CachedMatch>[] cache, Rule rule, Matcher master) {
 			super(offset, cache, rule, master);
 			m = r.matcher(offset, cache, master);
 		}
 
 		@Override
 		protected void fetchNext() {
 			next = null;
 			while (m.mightHaveNext()) {
 				Match n = m.match();
 				if (n == null)
 					break;
 				Match child = new Match(ConditionalRule.this, n.start(),
 						n.end());
 				Match[] children = { n };
 				child.setChildren(children);
 				boolean passes = c.passes(child, this, s);
 				if (options.debug)
 					logCondition(this, c, passes);
 				if (passes) {
 					next = child;
 					break;
 				}
 			}
 		}
 
 	}
 
 	protected final Rule r;
 	protected Condition c;
 
 	public ConditionalRule(Label label, Rule r, Condition c) {
 		super(label);
 		this.r = r;
 		this.c = c;
 	}
 
 	@Override
 	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
 			Matcher master) {
 		return new ConditionalMatcher(offset, cache, ConditionalRule.this,
 				master);
 	}
 
 	@Override
 	protected String uniqueId() {
 		StringBuilder b = new StringBuilder(r.uniqueId());
 		b.append("(").append(c.describe(true)).append(')');
 		return b.toString();
 	}
 
 	@Override
 	public String description(boolean bool) {
 		if (c.visible()) {
 			StringBuilder b = new StringBuilder(r.description(bool));
 			b.append(" (").append(c.describe(false)).append(')');
 			return b.toString();
 		}
 		return r.description(bool);
 	}
 
 	@Override
 	public Set<Integer> study(CharSequence s,
 			Map<Integer, CachedMatch>[] cache, GlobalState options) {
 		// non-terminal rules don't study
 		return null;
 	}
 
 	@Override
 	public boolean zeroWidth() {
 		return r.zeroWidth();
 	}
 
 	@Override
 	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
 		return r.mayBeZeroWidth(cache);
 	}
 
 	@Override
 	public Rule deepCopy(Label l, String nameBase, Map<String, Rule> cycleMap,
 			Set<String> knownLabels, Set<String> knownConditions) {
 		Condition c2 = c.copy(nameBase, knownConditions);
 		ConditionalRule cr = new ConditionalRule(l, r.deepCopy(nameBase,
 				cycleMap, knownLabels, knownConditions), c2);
 		return cr;
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
 	public Match checkCacheSlip(int i, Match m) {
 		Rule ru = r;
 		while (ru instanceof DeferredDefinitionRule)
 			ru = ((DeferredDefinitionRule) r).r;
 		return new Match(ru, m.start(), m.end());
 	}
 
 }
