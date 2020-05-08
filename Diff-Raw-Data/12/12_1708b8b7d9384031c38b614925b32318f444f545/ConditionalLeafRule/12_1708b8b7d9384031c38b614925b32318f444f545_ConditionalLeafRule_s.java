 package dfh.grammar;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 @Reversible
 public class ConditionalLeafRule extends LeafRule {
 	private static final long serialVersionUID = 1L;
 	private Condition c;
 
 	private class LeafMatcher extends Matcher {
 		private Map<Integer, CachedMatch> cache;
 		private boolean fresh = true;
 
 		public LeafMatcher(CharSequence s, Integer offset,
 				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
 			super(s, offset, master);
 			this.cache = cache.get(label);
 		}
 
 		@Override
 		public Match match() {
 			if (options.debug)
 				ConditionalLeafRule.this.matchTrace(this);
 			if (fresh) {
 				fresh = false;
 				CachedMatch cm = cache.get(offset);
 				if (cm == null) {
					if (options.study) {
 						if (options.debug)
 							ConditionalLeafRule.this.matchTrace(this, null);
 						return null;
 					}
 				} else {
 					if (options.debug)
 						ConditionalLeafRule.this.matchTrace(this, cm.m);
 					return register(cm.m);
 				}
 				java.util.regex.Matcher m = p.matcher(s);
 				m.region(offset, options.end);
 				m.useTransparentBounds(true);
 				m.useAnchoringBounds(false);
 				if (m.lookingAt()) {
 					Match n = new Match(ConditionalLeafRule.this, offset,
 							m.end());
 					if (c.passes(n, this, s))
 						cm = new CachedMatch(n);
 					else
 						cm = CachedMatch.MISMATCH;
 				} else
 					cm = CachedMatch.MISMATCH;
 				cache.put(offset, cm);
 				if (options.debug)
 					ConditionalLeafRule.this.matchTrace(this, cm.m);
 				return register(cm.m);
 			}
 			if (options.debug)
 				ConditionalLeafRule.this.matchTrace(this, null);
 			return null;
 		}
 
 		@Override
 		public boolean mightHaveNext() {
 			return fresh;
 		}
 
 		@Override
 		public String toString() {
 			return "M:" + ConditionalLeafRule.this;
 		}
 
 		@Override
 		protected Rule rule() {
 			return ConditionalLeafRule.this;
 		}
 	}
 
 	public ConditionalLeafRule(LeafRule lr, Condition c, String id) {
 		super(lr.label, lr.p, lr.reversible);
 		this.condition = id;
 		this.generation = lr.generation;
 		this.c = c;
 	}
 
 	@Override
 	public Matcher matcher(CharSequence s, final Integer offset,
 			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
 		return new LeafMatcher(s, offset, cache, master);
 	}
 
 	@Override
 	public Set<Integer> study(CharSequence s,
 			Map<Label, Map<Integer, CachedMatch>> cache,
 			Set<Rule> studiedRules, GlobalState options) {
 		Map<Integer, CachedMatch> subCache = cache.get(label);
 		Set<Integer> startOffsets = new HashSet<Integer>();
 		if (subCache.keySet().isEmpty()) {
 			if (studiedRules.contains(this))
 				return startOffsets;
 			else
 				studiedRules.add(this);
 			java.util.regex.Matcher m = p.matcher(s);
 			m.useAnchoringBounds(false);
 			m.useTransparentBounds(true);
 			m.region(options.start, options.end);
 			while (m.find()) {
 				Integer i = m.start();
 				startOffsets.add(i);
 				Match n = new Match(this, m.start(), m.end());
 				if (c.passes(n, null, s))
 					subCache.put(i, new CachedMatch(n));
 				int newStart = m.start() + 1;
 				if (newStart == options.end)
 					break;
 				m.region(newStart, options.end);
 			}
 		} else {
 			startOffsets.addAll(subCache.keySet());
 		}
 		return startOffsets;
 	}
 
 	@Override
 	public Rule conditionalize(Condition c, String id) {
 		if (this.c == null) {
 			this.c = c;
 			this.condition = id;
 		} else {
 			if (this.c instanceof LogicalCondition) {
 				if (!((LogicalCondition) this.c).replace(id, c))
 					throw new GrammarException("could not defined " + id
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
 
 }
