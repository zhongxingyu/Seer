 /*
  * dfh.grammar -- a recursive descent parser library for Java
  * 
  * Copyright (C) 2012 David F. Houghton
  * 
  * This software is licensed under the LGPL. Please see accompanying NOTICE file
  * and lgpl.txt.
  */
 package dfh.grammar;
 
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 
 import dfh.grammar.Label.Type;
 
 /**
  * A companion to {@link RuleParser}, {@link Compiler} encapsulates the messy
  * code that takes the output of the former and weaves a {@link Grammar}.
  * <p>
  * <b>Creation date:</b> Mar 21, 2011
  * 
  * @author David Houghton
  * 
  */
 public class Compiler {
 	private HashMap<Label, Rule> rules;
 	private Map<String, Label> terminalLabelMap;
 	private Collection<Label> undefinedRules = new HashSet<Label>();
 	private Map<Label, Set<Label>> dependencyMap = new HashMap<Label, Set<Label>>();
 	private Map<Label, Rule> reversedCyclicRuleMap = new HashMap<Label, Rule>();
 	private final Label root;
 	private Map<String, Set<Label>> undefinedConditions = new HashMap<String, Set<Label>>();
 	private Map<Label, Match> conditionMap = new HashMap<Label, Match>();
 	/**
 	 * The grammar of conditions at the end of rules.
 	 */
 	public static final String[] conditionGrammar = {
 			//
 			" ROOT = <exp> ",//
 			"  exp = <group> | <neg> | <conj> | <xor> | <disj> | <cnd>",//
 			"  neg = '!' /\\s*+/ [ <cnd> | <group> ]",//
 			"group = '(' /\\s*+/ <exp> /\\s*+/ ')'",//
 			" conj = <exp> [ [ /\\s*+/ '&' /\\s*+/ | /\\s++/ ] <exp> ]+",//
 			" disj = <exp> [ /\\s*+/ '|' /\\s*+/ <exp> ]+",//
 			"  xor = <exp> [ /\\s*+/ '^' /\\s*+/ <exp> ]+",//
 			"  cnd = /\\w++/",//
 	};
 	/**
 	 * {@link MatchTest} that filters out parse trees that don't respect the
 	 * usual rules of precedence among the logical operators and which enforces
 	 * a normal form, so a & b & c is represented as a single complex
 	 * conjunction rather than a conjunction of conjunctions.
 	 */
 	public static final MatchTest badMatch = new MatchTest() {
 		MatchTest expTest = new MatchTest() {
 			@Override
 			public boolean test(Match m) {
 				return m.hasLabel("exp") || m.hasLabel("group");
 			}
 		};
 
 		@Override
 		public boolean test(Match m) {
 			if (m.hasLabel("conj")) {
 				if (found(m, "disj", "xor", "conj")) {
 					return true;
 				}
 			} else if (m.hasLabel("xor")) {
 				if (found(m, "disj", "xor")) {
 					return true;
 				}
 			} else if (m.hasLabel("disj")) {
 				if (found(m, "disj")) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private boolean found(Match c, String... tags) {
 			Set<String> tagset = new HashSet<String>(tags.length * 2);
 			for (String s : tags)
 				tagset.add(s);
 			return found(c, tagset);
 		}
 
 		private boolean found(Match c, Set<String> tagset) {
 			@SuppressWarnings("unused")
 			String debug = c.group(), debug2;
 			List<Match> exps = c.closest(expTest);
 			if (exps.isEmpty())
 				return false;
 			for (Match exp : exps) {
 				if (exp.hasLabel("group"))
 					continue;
 				debug2 = exp.group();
 				for (String s : tagset) {
 					if (exp.children()[0].hasLabel(s))
 						return true;
 				}
 			}
 			return false;
 		}
 	};
 	private static Grammar cg;
 	static {
 		try {
 			cg = new Grammar(conditionGrammar);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Generates a {@link Compiler} reading rules from the given
 	 * {@link LineReader}.
 	 * 
 	 * @param reader
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Compiler(LineReader reader, Map<String, Rule> precompiledRules)
 			throws GrammarException {
 		String line = null;
 		if (precompiledRules == null)
			precompiledRules = Collections.emptyMap();
 		else {
 			// clone rules to protect them from reuse
 			for (Entry<String, Rule> e : precompiledRules.entrySet()) {
 				Rule r = e.getValue();
 				try {
 					Method m = r.getClass().getMethod("clone");
 					e.setValue((Rule) m.invoke(r));
 				} catch (Exception e1) {
 					throw new GrammarException(
 							"failed to clone precompiled rule "
 									+ r
 									+ "; precompiled rules must be cloned to protect them from reuse");
 				}
 			}
 		}
 		Map<Label, List<RuleFragment>> map = new HashMap<Label, List<RuleFragment>>();
 		Label r = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				LinkedList<RuleFragment> list = RuleParser.parse(line);
 				if (list == null)
 					continue; // blank line or comment
 				Label l = (Label) list.removeFirst();
 				if (r == null) {
 					if (l.t != Type.explicit) {
 						l = new Label(Type.explicit, l.id);
 					}
 					r = l;
 				}
 				if (list.peekLast() instanceof ConditionFragment) {
 					ConditionFragment cf = (ConditionFragment) list
 							.removeLast();
 					String cnd = cf.id.trim();
 					Match m = parseCondition(line, cnd);
 					for (Match cm : m.get("cnd")) {
 						String cs = cm.group();
 						Set<Label> set = undefinedConditions.get(cs);
 						if (set == null) {
 							set = new TreeSet<Label>();
 							undefinedConditions.put(cs, set);
 						}
 						set.add(l);
 					}
 					conditionMap.put(l, m);
 				}
 				if (map.containsKey(l))
 					throw new GrammarException("rule " + l
 							+ " redefined at line " + reader.lineNumber());
 				map.put(l, list);
 			}
 		} catch (IOException e1) {
 			throw new GrammarException(e1);
 		}
 		if (r == null)
 			throw new GrammarException("no root rule found");
 		this.root = r;
 
 		// make space for anonymous rules
 		rules = new HashMap<Label, Rule>(map.size() * 2);
 		Set<Label> terminals = new HashSet<Label>(map.size() * 2);
 		int gen = 1;
 		// first we extract all the terminals we can
 		for (Iterator<Entry<Label, List<RuleFragment>>> i = map.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<Label, List<RuleFragment>> e = i.next();
 			List<RuleFragment> body = e.getValue();
 			RuleFragment rf = body.get(0);
 			if (body.size() == 1
 					&& (rf instanceof Regex || rf instanceof LiteralFragment)) {
 				if (rf instanceof LiteralFragment
 						&& !((RepeatableRuleFragment) rf).rep.redundant())
 					continue;
 				Label l = e.getKey();
 				Match condition = conditionMap.get(l);
 				i.remove();
 				Type t = Type.explicit;
 				l = new Label(t, l.id);
 				terminals.add(l);
 				Rule ru;
 				if (rf instanceof Regex) {
 					Regex rx = (Regex) rf;
 					ru = new LeafRule(l, rx.re, rx.reversible);
 					if (condition != null)
 						ru = new ConditionalLeafRule((LeafRule) ru,
 								LogicalCondition.manufacture(condition),
 								condition.group());
 				} else {
 					ru = new LiteralRule(l, ((LiteralFragment) rf).literal);
 					setCondition(condition, ru);
 				}
 				ru.generation = gen;
 				rules.put(l, ru);
 			}
 		}
 		// now we extract all the deferred definition rules and incorporate
 		// pre-compiled rules
 		gen = 0;
 		Set<String> knownIds = new HashSet<String>(map.size() + rules.size());
 		for (Label l : map.keySet())
 			knownIds.add(l.id);
 		for (Label l : rules.keySet())
 			knownIds.add(l.id);
 		for (List<RuleFragment> list : map.values()) {
 			Set<Label> labels = allLabels(list);
 			for (Label l : labels) {
 				if (l.t == Type.indeterminate && !knownIds.contains(l.id)) {
 					if (precompiledRules.containsKey(l.id)) {
 						Rule ru = precompiledRules.get(l.id);
 						ru.setLabel(l.id);
 						rules.put(ru.label(), ru);
 						terminals.add(ru.label());
 					} else {
 						l = new Label(Type.explicit, l.id);
 						DeferredDefinitionRule ddr = new DeferredDefinitionRule(
 								l);
 						ddr.generation = gen;
 						undefinedRules.add(l);
 						rules.put(l, ddr);
 						terminals.add(l);
 					}
 				}
 			}
 		}
 		// create dependency map
 		for (Entry<Label, List<RuleFragment>> e : map.entrySet()) {
 			Set<Label> dependents = new HashSet<Label>(allLabels(e.getValue()));
 			dependents.retainAll(map.keySet());
 			if (!dependents.isEmpty()) {
 				dependencyMap.put(e.getKey(), dependents);
 			}
 		}
 		// now we define the remainder
 		gen = 2;
 		while (!map.isEmpty()) {
 			int size = map.size();
 			// we process rules by generation, from less dependent to more, in
 			// order to optimize the cache
 			List<Entry<Label, List<RuleFragment>>> generation = new LinkedList<Map.Entry<Label, List<RuleFragment>>>();
 			for (Iterator<Entry<Label, List<RuleFragment>>> i = map.entrySet()
 					.iterator(); i.hasNext();) {
 				Entry<Label, List<RuleFragment>> e = i.next();
 				Set<Label> labels = allLabels(e.getValue());
 				boolean defined = true;
 				for (Label l : labels) {
 					if (!rules.containsKey(l)) {
 						defined = false;
 						break;
 					}
 				}
 				if (defined) {
 					// if all the constituents of a rule are defined, we define
 					// the rule
 					generation.add(e);
 					i.remove();
 				}
 			}
 			if (map.size() == size) {
 				resolveRecursions(map, gen);
 				if (map.size() == size)
 					throw new GrammarException(
 							"could not satisfy all dependencies");
 			}
 
 			// now we generate all these rules
 
 			// first we sort them to ensure those rules which are likely to be
 			// components of others are handled first
 			class Sorter implements Comparable<Sorter> {
 				final Entry<Label, List<RuleFragment>> e;
 				final int length;
 
 				Sorter(Entry<Label, List<RuleFragment>> e) {
 					int i = 0;
 					this.e = e;
 					for (RuleFragment r : e.getValue())
 						i += r.toString().length() + 1;
 					length = i;
 				}
 
 				@Override
 				public int compareTo(Sorter o) {
 					return length - o.length;
 				}
 
 			}
 			List<Sorter> sorters = new ArrayList<Sorter>(generation.size());
 			for (Entry<Label, List<RuleFragment>> e : generation)
 				sorters.add(new Sorter(e));
 			Collections.sort(sorters);
 			// now we make the rules
 			for (Sorter s : sorters) {
 				Label l = s.e.getKey();
 				List<RuleFragment> body = s.e.getValue();
 				Rule ru = parseRule(l, body, null);
 				if (redundancyTest(body)) {
 					Label l2 = (Label) body.get(0);
 					ru.addLabel(l2);
 				}
 				ru.generation = gen;
 				rules.put(l, ru);
 			}
 			gen++;
 		}
 
 		terminalLabelMap = new HashMap<String, Label>(terminals.size() * 2);
 		for (Label l : terminals)
 			terminalLabelMap.put(l.id, l);
 	}
 
 	private boolean redundancyTest(List<RuleFragment> body) {
 		if (body.size() == 1 && body.get(0) instanceof Label)
 			return ((Label) body.get(0)).rep.redundant();
 		return false;
 	}
 
 	static Match parseCondition(String line, String cnd) {
 		if (cnd == null)
 			return null;
 		Matcher cgm = cg.matches(cnd, new Options().keepRightmost(true)
 				.allowOverlap(true));
 		Match m;
 		while ((m = cgm.match()) != null) {
 			if (m.passes(badMatch))
 				continue;
 			else
 				break;
 		}
 		if (m == null) {
 			StringBuilder b = new StringBuilder();
 			b.append("bad condition in line ").append(line);
 			m = cgm.rightmostMatch();
 			if (m != null) {
 				b.append("\n\t");
 				b.append(cnd.substring(0, m.end()));
 				b.append("<-- HERE -->");
 				b.append(cnd.substring(m.end()));
 			}
 			throw new GrammarException(b.toString());
 		}
 		return m;
 	}
 
 	/**
 	 * Resolve those recursions that will not lead to inescapable loops.
 	 * 
 	 * @param map
 	 */
 	private void resolveRecursions(Map<Label, List<RuleFragment>> map,
 			int generation) {
 		for (Iterator<Entry<Label, Set<Label>>> i = dependencyMap.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<Label, Set<Label>> e = i.next();
 			if (map.containsKey(e.getKey()))
 				e.getValue().retainAll(map.keySet());
 			else
 				i.remove();
 		}
 		Map<Label, List<RuleFragment>> copy = new HashMap<Label, List<RuleFragment>>(
 				map);
 		removeStrictlyDominating(copy);
 		List<List<Entry<Label, List<RuleFragment>>>> cycles = separateCycles(copy);
 		for (List<Entry<Label, List<RuleFragment>>> cycle : cycles) {
 			processCycle(cycle, generation);
 			for (Entry<Label, List<RuleFragment>> e : cycle)
 				map.remove(e.getKey());
 		}
 	}
 
 	/**
 	 * Creates a set of mutually dependent rules.
 	 * 
 	 * @param cycle
 	 * @param generation
 	 */
 	private void processCycle(List<Entry<Label, List<RuleFragment>>> cycle,
 			int generation) {
 		testCycle(cycle);
 		Map<Label, CyclicRule> cycleMap = new HashMap<Label, CyclicRule>(
 				cycle.size() * 2);
 		for (Entry<Label, List<RuleFragment>> e : cycle) {
 			CyclicRule ddr = new CyclicRule(new Label(Type.explicit,
 					e.getKey().id));
 			cycleMap.put(e.getKey(), ddr);
 		}
 		for (Entry<Label, List<RuleFragment>> e : cycle) {
 			Rule r = parseRule(e.getKey(), e.getValue(), cycleMap);
 			r.generation = generation;
 			cycleMap.get(e.getKey()).setRule(r);
 			rules.put(e.getKey(), r);
 		}
 	}
 
 	/**
 	 * Makes sure some escape is possible from a cycle.
 	 * 
 	 * @param cycle
 	 */
 	private void testCycle(List<Entry<Label, List<RuleFragment>>> cycle) {
 		Set<Label> set = new HashSet<Label>(cycle.size() * 2);
 		for (Entry<Label, List<RuleFragment>> e : cycle)
 			set.add(e.getKey());
 		for (Entry<Label, List<RuleFragment>> e : cycle) {
 			if (findEscape(e, set))
 				return;
 		}
 		StringBuilder b = new StringBuilder();
 		b.append("cycle found in rules: ");
 		boolean nonInitial = false;
 		for (Entry<Label, List<RuleFragment>> e : cycle) {
 			if (nonInitial)
 				b.append(", ");
 			else
 				nonInitial = true;
 			b.append(e.getKey());
 		}
 		throw new GrammarException(b.toString());
 	}
 
 	/**
 	 * @param e
 	 * @param set
 	 * @return whether there is some way to escape from a mutual dependency
 	 *         cycle in this rule
 	 */
 	private boolean findEscape(Entry<Label, List<RuleFragment>> e,
 			Set<Label> set) {
 		List<RuleFragment> list = e.getValue();
 		for (RuleFragment r : list) {
 			if (!set.contains(r))
 				return true;
 			if (((RepeatableRuleFragment) r).rep.bottom == 0)
 				return true;
 		}
 		return false;
 	}
 
 	private List<List<Entry<Label, List<RuleFragment>>>> separateCycles(
 			Map<Label, List<RuleFragment>> copy) {
 		List<List<Entry<Label, List<RuleFragment>>>> cycles = new LinkedList<List<Entry<Label, List<RuleFragment>>>>();
 		while (true) {
 			// first, we find the entry with the fewest dependencies
 			List<Entry<Label, List<RuleFragment>>> list = new ArrayList<Map.Entry<Label, List<RuleFragment>>>(
 					copy.entrySet());
 			Collections.sort(list,
 					new Comparator<Entry<Label, List<RuleFragment>>>() {
 						@Override
 						public int compare(Entry<Label, List<RuleFragment>> o1,
 								Entry<Label, List<RuleFragment>> o2) {
 							return dependencyMap.get(o1.getKey()).size()
 									- dependencyMap.get(o2.getKey()).size();
 						}
 					});
 			Entry<Label, List<RuleFragment>> least = list.get(0);
 			Set<Label> set = new HashSet<Label>();
 			set.add(least.getKey());
 			List<Entry<Label, List<RuleFragment>>> cycle = new LinkedList<Map.Entry<Label, List<RuleFragment>>>();
 			LinkedList<Label> searchQueue = new LinkedList<Label>(
 					dependencyMap.get(least.getKey()));
 			while (!searchQueue.isEmpty()) {
 				Label l = searchQueue.removeFirst();
 				Set<Label> support = dependencyMap.get(l);
 				for (Label sup : support) {
 					if (!set.contains(l))
 						searchQueue.add(sup);
 				}
 				set.add(l);
 			}
 			for (Iterator<Entry<Label, List<RuleFragment>>> i = copy.entrySet()
 					.iterator(); i.hasNext();) {
 				Entry<Label, List<RuleFragment>> e = i.next();
 				if (set.contains(e.getKey())) {
 					cycle.add(e);
 					i.remove();
 				}
 			}
 			cycles.add(cycle);
 			if (copy.isEmpty())
 				break;
 		}
 		return cycles;
 	}
 
 	/**
 	 * Remove from the given map those rules that depend on others but have no
 	 * others dependent on them.
 	 * 
 	 * @param copy
 	 */
 	private void removeStrictlyDominating(Map<Label, List<RuleFragment>> copy) {
 		while (true) {
 			Set<Label> required = new HashSet<Label>(copy.size() * 2);
 			for (List<RuleFragment> list : copy.values())
 				required.addAll(allLabels(list));
 			boolean changed = false;
 			for (Iterator<Label> i = copy.keySet().iterator(); i.hasNext();) {
 				if (!required.contains(i.next())) {
 					i.remove();
 					changed = true;
 				}
 			}
 			if (!changed)
 				break;
 		}
 	}
 
 	private Rule parseRule(Label label, List<RuleFragment> fragments,
 			Map<Label, CyclicRule> cycleMap) {
 		Rule r;
 		if (fragments.size() == 1) {
 			RuleFragment rf = fragments.get(0);
 			if (isNamedCapture(rf)) {
 				GroupFragment gf = (GroupFragment) rf;
 				r = parseRule(label, gf.alternates.get(0), cycleMap);
 				r.setLabels(gf.alternateTags);
 			} else
 				r = makeSingle(label, fragments.get(0), cycleMap);
 		} else
 			r = makeSequence(label, fragments, cycleMap);
 		return r;
 	}
 
 	private boolean isNamedCapture(RuleFragment rf) {
 		if (rf instanceof GroupFragment) {
 			GroupFragment gf = (GroupFragment) rf;
 			return (gf.rep.redundant() && !gf.alternateTags.isEmpty());
 		}
 		return false;
 	}
 
 	private Rule makeSingle(Label label, RuleFragment ruleFragment,
 			Map<Label, CyclicRule> cycleMap) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSingle(ruleFragment, cycleMap, conditionMap.get(label));
 		r = fixLabel(label, r, conditionMap.get(label));
 		fixConditions(label, r);
 		return r;
 	}
 
 	private void fixConditions(Label label, Rule r) {
 		if (r.condition != null) {
 			Match m = cg.matches(r.condition).match();
 			conditionMap.put(label, m);
 			for (Match n : m.get("cnd")) {
 				Set<Label> set = undefinedConditions().get(n.group());
 				set.add(label);
 			}
 		}
 	}
 
 	/**
 	 * Assigns a name to an anonymous {@link Rule}.
 	 * 
 	 * @param label
 	 * @param r
 	 * @param match
 	 * @return named {@link Rule}
 	 */
 	static Rule fixLabel(Label label, Rule r, Match match) {
 		Rule ru = null;
 		Set<String> labels = r.labels;
 		if (r instanceof AlternationRule) {
 			ru = new AlternationRule(label, ((AlternationRule) r).alternates,
 					((AlternationRule) r).tagMap);
 		} else if (r instanceof RepetitionRule) {
 			RepetitionRule rr = (RepetitionRule) r;
 			ru = new RepetitionRule(label, rr.r, rr.repetition,
 					new HashSet<String>(rr.alternateTags));
 		} else if (r instanceof SequenceRule) {
 			ru = new SequenceRule(label, ((SequenceRule) r).sequence,
 					((SequenceRule) r).tagList);
 		} else if (r instanceof LiteralRule) {
 			ru = new LiteralRule(label, ((LiteralRule) r).literal);
 		} else if (r instanceof LeafRule) {
 			LeafRule lr = (LeafRule) r;
 			ru = new LeafRule(label, lr.p, lr.reversible);
 		} else if (r instanceof DeferredDefinitionRule) {
 			DeferredDefinitionRule old = (DeferredDefinitionRule) r;
 			DeferredDefinitionRule ddr = new DeferredDefinitionRule(label);
 			ddr.r = old;
 			ru = ddr;
 		}
 		if (ru == null)
 			throw new GrammarException("unanticipated rule type: "
 					+ r.getClass().getName());
 		setCondition(match, ru);
 
 		if (match != null && r.condition == null) {
 			ru = ru.conditionalize(LogicalCondition.manufacture(match),
 					match.group());
 		} else if (r.condition != null) {
 			ru.condition = r.condition;
 		}
 		if (labels != null) {
 			if (ru.labels == null)
 				ru.labels = labels;
 			else
 				ru.addLabels(labels);
 		}
 		return ru;
 	}
 
 	/**
 	 * Makes a rule with a bogus label
 	 * 
 	 * @param rf
 	 * @param cycleMap
 	 * @return
 	 */
 	private Rule makeSingle(RuleFragment rf, Map<Label, CyclicRule> cycleMap,
 			Match condition) {
 		if (rf instanceof AssertionFragment) {
 			AssertionFragment af = (AssertionFragment) rf;
 			Rule sr = makeSingle(af.rf, cycleMap, null);
 			String subDescription = null;
 			if (!af.forward) {
 				StringBuilder b = new StringBuilder();
 				Assertion.subDescription(sr, b);
 				subDescription = b.toString();
 				sr = reverse(sr);
 			}
 			String id = (af.positive ? '~' : '!') + (af.forward ? "+" : "-")
 					+ sr.label().toString();
 			if (!af.forward)
 				// strip off redundant ":r" to improve logging
 				id = id.substring(0, id.length() - 2);
 			Label l = new Label(Type.implicit, id);
 			Assertion a = new Assertion(l, sr, af.positive, af.forward);
 			if (subDescription != null)
 				a.setSubDescription(subDescription);
 			return a;
 		} else if (rf instanceof BarrierFragment) {
 			BarrierFragment bf = (BarrierFragment) rf;
 			Rule r = new BacktrackingBarrier(bf.id.length() == 1);
 			return r;
 		} else if (rf instanceof Label) {
 			Label l = (Label) rf;
 			Rule r = rules.get(l);
 			if (r == null)
 				r = cycleMap.get(l);
 			if (l.rep.redundant())
 				return r;
 			Label label = new Label(Type.explicit, l.toString());
 			r = new RepetitionRule(label, r, l.rep, new HashSet<String>(0));
 			setCondition(condition, r);
 			return r;
 		} else if (rf instanceof LiteralFragment) {
 			LiteralFragment lf = (LiteralFragment) rf;
 			Label l = new Label(Type.implicit, '"' + lf.literal + '"');
 			Rule r = new LiteralRule(l, lf.literal);
 			if (lf.rep.redundant()) {
 				setCondition(condition, r);
 				return r;
 			}
 			l = new Label(Type.implicit, lf.toString());
 			r = new RepetitionRule(l, r, lf.rep, new HashSet<String>(0));
 			setCondition(condition, r);
 			return r;
 		} else if (rf instanceof BackReferenceFragment) {
 			BackReferenceFragment brf = (BackReferenceFragment) rf;
 			Label l = new Label(Type.implicit, rf.toString());
 			Rule r = new BackReferenceRule(l, brf.reference);
 			return r;
 		} else if (rf instanceof UplevelBackReferenceFragment) {
 			UplevelBackReferenceFragment ubf = (UplevelBackReferenceFragment) rf;
 			Label l = new Label(Type.implicit, rf.toString());
 			UpLevelBackReferenceRule ulbr = new UpLevelBackReferenceRule(l,
 					ubf.reference, ubf.level);
 			Rule r = ulbr;
 			if (ubf.rep.redundant())
 				return r;
 			l = new Label(Type.implicit, ubf.toString());
 			Set<String> tags = new HashSet<String>(2);
 			r = new UncachedRepetitionRule(l, r, ubf.rep, tags);
 			tags.add(r.uniqueId());
 			return r;
 		} else if (rf instanceof Regex) {
 			Regex rx = (Regex) rf;
 			Label l = new Label(Type.implicit, '/' + rf.toString() + '/');
 			Rule r = new LeafRule(l, rx.re, rx.reversible);
			if (rx.rep.redundant()) {
				setCondition(condition, r);
				return r;
			}
			l = new Label(Type.implicit, rx.toString());
			r = new RepetitionRule(l, r, rx.rep, new HashSet<String>(0));
 			setCondition(condition, r);
 			return r;
 		}
 		GroupFragment gf = (GroupFragment) rf;
 		Set<String> tags = gf.alternateTags;
 		// if we've gotten here then !gf.rep.redundant(), because these
 		// cases are handled by the sequence parser or the initial rule body
 		// parser
 		Rule r = null;
 		if (gf.alternates.size() == 1) {
 			// repeated rule with capture
 			if (gf.alternates.get(0).size() == 1) {
 				r = makeSingle(gf.alternates.get(0).get(0), cycleMap, null);
 			} else {
 				// repeated sequence
 				r = makeSequence(gf.alternates.get(0), cycleMap, null);
 			}
 		} else {
 			// repeated alternation
 			List<Rule> alternates = new ArrayList<Rule>(gf.alternates.size());
 			StringBuilder b = new StringBuilder();
 			b.append('[');
 			boolean nonInitial = false;
 			Map<String, Set<String>> tagMap = new HashMap<String, Set<String>>(
 					gf.alternates.size() * 2);
 			for (List<RuleFragment> alternate : gf.alternates) {
 				Set<String> innerTags = null;
 				if (alternate.size() == 1) {
 					RuleFragment rfInner = alternate.get(0);
 					if (isNamedCapture(rfInner)) {
 						GroupFragment gfInner = (GroupFragment) rfInner;
 						innerTags = new HashSet<String>(gfInner.alternateTags);
 						if (gfInner.alternates.size() == 1) {
 							if (gfInner.alternates.get(0).size() == 1)
 								r = makeSingle(
 										gfInner.alternates.get(0).get(0),
 										cycleMap, null);
 							else
 								r = makeSequence(gfInner.alternates.get(0),
 										cycleMap, null);
 						} else
 							r = makeSingle(rfInner, cycleMap, null);
 					} else {
 						r = makeSingle(rfInner, cycleMap, null);
 					}
 					String id = r.uniqueId();
 					if (tagMap.containsKey(id)) {
 						if (innerTags != null)
 							tagMap.get(id).addAll(innerTags);
 						continue;
 					} else {
 						tagMap.put(id, innerTags == null ? new HashSet<String>(
 								0) : innerTags);
 					}
 				} else {
 					r = makeSequence(alternate, cycleMap, null);
 					tagMap.put(r.uniqueId(), new HashSet<String>(0));
 				}
 				alternates.add(r);
 				if (nonInitial)
 					b.append('|');
 				else
 					nonInitial = true;
 				b.append(r.label().toString());
 			}
 			b.append(']');
 			Label l = new Label(Type.implicit, b.toString());
 			r = new AlternationRule(l, alternates.toArray(new Rule[alternates
 					.size()]), tagMap);
 			if (gf.rep.redundant()) { // TODO confirm that this won't be true
 				setCondition(condition, r);
 				return r;
 			}
 		}
 		Label l = new Label(Type.implicit, r.label().toString() + gf.rep);
 		r = new RepetitionRule(l, r, gf.rep, tags);
 		setCondition(condition, r);
 		return r;
 	}
 
 	private static void setCondition(Match condition, Rule r) {
 		if (condition != null) {
 			r.condition = condition.group();
 			r.conditionalize(LogicalCondition.manufacture(condition),
 					r.condition);
 		}
 	}
 
 	private Rule reverse(Rule sr) {
 		// check to make sure the class has been annotated as reversible
 		boolean irreversible = true;
 		for (Annotation a : sr.getClass().getAnnotations()) {
 			if (a instanceof Reversible) {
 				irreversible = false;
 				break;
 			}
 		}
 		if (irreversible)
 			throw new GrammarException("cannot reverse " + sr + "; its class, "
 					+ sr.getClass()
 					+ " is not annotated as dfh.grammar.Reversible");
 
 		// all's well, so we reverse the rule
 		Rule ru = null;
 		String id = sr.label.id;
 		if (id.endsWith(Assertion.REVERSAL_SUFFIX))
 			id = id.substring(0,
 					id.length() - Assertion.REVERSAL_SUFFIX.length());
 		else
 			id = id + Assertion.REVERSAL_SUFFIX;
 		if (sr instanceof AlternationRule) {
 			AlternationRule ar = (AlternationRule) sr;
 			Rule[] children = new Rule[ar.alternates.length];
 			Map<String, Set<String>> tagMap = new HashMap<String, Set<String>>(
 					ar.tagMap.size() * 2);
 			for (int i = 0; i < children.length; i++) {
 				children[i] = reverse(ar.alternates[i]);
 				tagMap.put(children[i].uniqueId(),
 						ar.tagMap.get(ar.alternates[i].uniqueId()));
 			}
 			Label l = new Label(Type.implicit, id);
 			ru = new AlternationRule(l, children, tagMap);
 		} else if (sr instanceof Assertion) {
 			Assertion as = (Assertion) sr;
 			Rule child = as.forward ? reverse(as.r) : as.r;
 			id = (as.positive ? '~' : '!') + (as.forward ? "-" : "+")
 					+ child.label().toString();
 			Label l = new Label(Type.implicit, id);
 			ru = new Assertion(l, child, as.positive, !as.forward);
 		} else if (sr instanceof BackReferenceRule) {
 			ru = sr;
 		} else if (sr instanceof BacktrackingBarrier) {
 			ru = sr;
 		} else if (sr instanceof LeafRule) {
 			LeafRule lr = (LeafRule) sr;
 			if (!lr.reversible)
 				throw new GrammarException(
 						"terminal rule "
 								+ lr
 								+ " has not been marked as reversible; it cannot be used in a backwards assertion");
 			Label l = new Label(Type.implicit, id);
 			ru = new LeafRule(l, lr.p, true);
 		} else if (sr instanceof LiteralRule) {
 			LiteralRule lr = (LiteralRule) sr;
 			ReversedCharSequence rcs = new ReversedCharSequence(lr.literal);
 			String s = rcs.toString();
 			Label l = new Label(Type.implicit, id);
 			ru = new LiteralRule(l, s);
 		} else if (sr instanceof RepetitionRule) {
 			RepetitionRule rr = (RepetitionRule) sr;
 			Rule child = reverse(rr.r);
 			// check for redundant repetition and eliminate
 			if (rr.repetition.redundant())
 				return child;
 			Label l = new Label(Type.implicit, id);
 			ru = new RepetitionRule(l, child, rr.repetition,
 					new HashSet<String>(rr.alternateTags));
 		} else if (sr instanceof SequenceRule) {
 			SequenceRule sqr = (SequenceRule) sr;
 			// reverse order of last backreference in sequence and submatch
 			// referred to before reversing order of entire sequence
 			Set<Integer> swapped = new HashSet<Integer>(sqr.sequence.length * 2);
 			for (int i = sqr.sequence.length - 1; i >= 0; i--) {
 				if (sqr.sequence[i] instanceof BackReferenceRule) {
 					BackReferenceRule temp = (BackReferenceRule) sqr.sequence[i];
 					if (swapped.contains(temp.index))
 						continue;
 					sqr.sequence[i] = sqr.sequence[temp.index];
 					sqr.sequence[temp.index] = temp;
 					swapped.add(temp.index);
 				}
 			}
 			Rule[] children = new Rule[sqr.sequence.length];
 			for (int i = 0; i < children.length; i++) {
 				children[i] = reverse(sqr.sequence[children.length - i - 1]);
 			}
 			Label l = new Label(Type.implicit, id);
 			List<Set<String>> tagList = new ArrayList<Set<String>>(sqr.tagList);
 			Collections.reverse(tagList);
 			ru = new SequenceRule(l, children, tagList);
 		} else if (sr instanceof CyclicRule) {
 			if (reversedCyclicRuleMap.containsKey(sr.label)) {
 				ru = reversedCyclicRuleMap.get(sr.label);
 			} else {
 				CyclicRule cr = (CyclicRule) sr;
 				Label l = new Label(Type.explicit, id);
 				cr = new CyclicRule(l);
 				reversedCyclicRuleMap.put(sr.label, cr);
 				cr.setRule(reverse(((CyclicRule) sr).r));
 				ru = cr;
 			}
 		} else {
 			ru = sr.reverse(id);
 		}
 		if (sr.condition != null) {
 			ru.condition = sr.condition;
 			ru = adjustCondition(ru, sr);
 		}
 		ru.unreversed = sr;
 		return ru;
 	}
 
 	private Rule adjustCondition(Rule nr, Rule r) {
 		if (r instanceof AlternationRule) {
 			AlternationRule ar = (AlternationRule) r, ar2 = (AlternationRule) nr;
 			ar2.c = duplicateCondition(ar.c);
 		} else if (r instanceof SequenceRule) {
 			SequenceRule sr = (SequenceRule) r, sr2 = (SequenceRule) nr;
 			sr2.c = duplicateCondition(sr.c);
 		} else if (r instanceof RepetitionRule) {
 			RepetitionRule rr = (RepetitionRule) r, rr2 = (RepetitionRule) nr;
 			rr2.c = duplicateCondition(rr.c);
 		} else if (r instanceof ConditionalLeafRule) {
 			ConditionalLeafRule clr = (ConditionalLeafRule) r;
 			LeafRule lr = (LeafRule) nr;
 			return lr.conditionalize(duplicateCondition(clr.c), clr.condition);
 		} else if (r instanceof LiteralRule) {
 			LiteralRule lr = (LiteralRule) r, lr2 = (LiteralRule) nr;
 			lr2.c = duplicateCondition(lr.c);
 		}
 		return nr;
 	}
 
 	private Condition duplicateCondition(Condition c) {
 		if (c instanceof LogicalCondition)
 			return ((LogicalCondition) c).duplicate();
 		return c;
 	}
 
 	private Rule makeSequence(Label label, List<RuleFragment> fragments,
 			Map<Label, CyclicRule> cycleMap) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSequence(fragments, cycleMap, conditionMap.get(label));
 		r = fixLabel(label, r, conditionMap.get(label));
 		fixConditions(label, r);
 		return r;
 	}
 
 	private Rule makeSequence(List<RuleFragment> value,
 			Map<Label, CyclicRule> cycleMap, Match condition) {
 		if (value.size() == 1)
 			throw new GrammarException(
 					"logic error in compiler; no singleton lists should arrive at this point");
 		Rule[] sequence = new Rule[value.size()];
 		int index = 0;
 		boolean nonInitial = false;
 		List<Set<String>> tagList = new ArrayList<Set<String>>(value.size());
 		StringBuilder b = new StringBuilder();
 		b.append('[');
 		for (RuleFragment rf : value) {
 			Set<String> tagSet = null;
 			Rule r = null;
 			if (rf instanceof GroupFragment) {
 				GroupFragment gf = (GroupFragment) rf;
 				if (isNamedCapture(gf)) {
 					tagSet = gf.alternateTags;
 					if (gf.alternates.size() == 1) {
 						if (gf.alternates.get(0).size() == 1)
 							r = makeSingle(gf.alternates.get(0).get(0),
 									cycleMap, null);
 						else
 							r = makeSequence(gf.alternates.get(0), cycleMap,
 									null);
 					}
 				} else {
 					tagSet = new HashSet<String>(0);
 					r = makeSingle(rf, cycleMap, null);
 				}
 			} else {
 				tagSet = new HashSet<String>(0);
 				r = makeSingle(rf, cycleMap, null);
 			}
 			if (nonInitial)
 				b.append(' ');
 			else
 				nonInitial = true;
 			tagList.add(tagSet);
 			sequence[index++] = r;
 			b.append(r.label().toString());
 		}
 		b.append(']');
 		Label l = new Label(Type.implicit, b.toString());
 		Rule r = new SequenceRule(l, sequence, tagList);
 		setCondition(condition, r);
 		return r;
 	}
 
 	Map<Label, Rule> rules() {
 		return new HashMap<Label, Rule>(rules);
 	}
 
 	Map<String, Label> terminalLabelMap() {
 		return new HashMap<String, Label>(terminalLabelMap);
 	}
 
 	HashSet<Label> undefinedTerminals() {
 		return new HashSet<Label>(undefinedRules);
 	}
 
 	private Set<Label> allLabels(List<RuleFragment> value) {
 		Set<Label> allLabels = new TreeSet<Label>();
 		for (RuleFragment rf : value) {
 			if (rf instanceof Label)
 				allLabels.add((Label) rf);
 			else if (rf instanceof GroupFragment) {
 				GroupFragment gf = (GroupFragment) rf;
 				for (List<RuleFragment> l : gf.alternates) {
 					allLabels.addAll(allLabels(l));
 				}
 			} else if (rf instanceof AssertionFragment) {
 				AssertionFragment af = (AssertionFragment) rf;
 				List<RuleFragment> list = new ArrayList<RuleFragment>(1);
 				list.add(af.rf);
 				allLabels.addAll(allLabels(list));
 			}
 		}
 		return allLabels;
 	}
 
 	Label root() {
 		return root;
 	}
 
 	public Map<String, Set<Label>> undefinedConditions() {
 		return new HashMap<String, Set<Label>>(undefinedConditions);
 	}
 }
