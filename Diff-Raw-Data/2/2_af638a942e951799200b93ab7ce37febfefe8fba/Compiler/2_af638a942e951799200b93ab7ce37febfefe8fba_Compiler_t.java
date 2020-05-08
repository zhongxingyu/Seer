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
 import dfh.grammar.Label.Whitespace;
 
 /**
  * A companion to {@link RuleParser}, {@link Compiler} encapsulates the messy
  * code that takes the output of the former and weaves a {@link Grammar}.
  * {@link RuleParser} is responsible for syntax; {@link Compiler}, for
  * semantics.
  * <p>
  * <b>Creation date:</b> Mar 21, 2011
  * 
  * @author David Houghton
  * 
  */
 final class Compiler {
 	private HashMap<Label, Rule> rules;
 	private Map<String, Label> terminalLabelMap;
 	private Collection<Label> undefinedRules = new HashSet<Label>();
 	private Map<Label, Set<Label>> dependencyMap = new HashMap<Label, Set<Label>>();
 	private Map<Label, Rule> reversedCyclicRuleMap = new HashMap<Label, Rule>();
 	private Label root;
 	private Map<String, Set<String>> undefinedConditions = new HashMap<String, Set<String>>();
 	private Map<String, Condition> conditionMap = new HashMap<String, Condition>();
 	private boolean setWhitespaceCondition = false;
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
 			"  cnd = /\\w++/ | <res>",//
 			"  res = /\\.\\w++/",//
 	};
 	/**
 	 * {@link MatchTest} that filters out parse trees that don't respect the
 	 * usual rules of precedence among the logical operators and which enforces
 	 * a normal form, so a & b & c is represented as a single complex
 	 * conjunction rather than a conjunction of conjunctions.
 	 */
 	public static final MatchTest badMatch = new MatchTest() {
 		private static final long serialVersionUID = 3L;
 		MatchTest expTest = new MatchTest() {
 			private static final long serialVersionUID = 3L;
 
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
 	private static final Set<String> EMPTY_STR_SET = Collections.emptySet();
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
 	@SuppressWarnings("unchecked")
 	Compiler(LineReader reader, Map<String, Rule> precompiledRules)
 			throws GrammarException {
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
 		Map<Label, SyntacticParse> map = new HashMap<Label, SyntacticParse>();
 		RuleParser parser = new RuleParser(reader);
 		try {
 			SyntacticParse parsed;
 			while ((parsed = parser.next()) != null) {
 				Label l = parsed.l;
 				if (root == null) {
 					if (l.t != Type.explicit) {
 						l = new Label(Type.explicit, l.id);
 					}
 					root = l;
 				}
 				if (parsed.c != null) {
 					ConditionFragment cf = parsed.c;
 					cf.id = cf.id.trim();
 					if (!conditionMap.containsKey(cf.id)) {
 						Match m = parseCondition(parsed.text, cf.id);
 						Condition c = LogicalCondition.manufacture(m);
 						conditionMap.put(cf.id, c);
 					}
 				}
 				if (map.containsKey(l))
 					throw new GrammarException("rule " + l
 							+ " redefined at line " + parser.getLineNumber());
 				map.put(l, parsed);
 			}
 		} catch (IOException e1) {
 			throw new GrammarException(e1);
 		}
 		if (root == null)
 			throw new GrammarException("no root rule found");
 
 		// make space for anonymous rules
 		rules = new HashMap<Label, Rule>(map.size() * 2);
 		Set<Label> terminals = new HashSet<Label>(map.size() * 2);
 		int gen = 1;
 		// first we extract all the terminals we can
 		boolean addedAS = false, addedS = false; // to speed things up a bit
 		for (Iterator<Entry<Label, SyntacticParse>> i = map.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<Label, SyntacticParse> e = i.next();
 			Label label = e.getKey();
 			// handling for .s and .as
 			if (!(label.ws == Whitespace.none || addedAS && addedS)) {
 				for (Label l : e.getValue().getHiddenSpaces()) {
 					if (l == HiddenSpace.ASSERTION_LABEL) {
 						rules.put(HiddenSpace.ASSERTION_LABEL,
 								HiddenSpace.assertion());
 						addedAS = true;
 					} else {
 						rules.put(HiddenSpace.LABEL, HiddenSpace.ordinary());
 						addedS = true;
 					}
 				}
 			}
 			SyntacticParse sp = e.getValue();
 			SequenceFragment body = sp.f;
 			// handling for .
 			checkForVisibleSpace(body);
 			RuleFragment rf = body.get(0);
 			if (body.size() == 1
 					&& (rf instanceof Regex || rf instanceof LiteralFragment)) {
 				if (!((RepeatableRuleFragment) rf).rep.redundant())
 					continue;
 				Label l = e.getKey();
 				Type t = Type.explicit;
 				l = new Label(t, l.id);
 				terminals.add(l);
 				Rule ru;
 				if (sp.c == null) {
 					if (rf instanceof Regex) {
 						Regex rx = (Regex) rf;
 						ru = new LeafRule(l, rx.re, rx.reversible);
 					} else {
 						ru = new LiteralRule(l, ((LiteralFragment) rf).literal);
 					}
 				} else {
 					ru = makeSingle(rf, Collections.EMPTY_MAP);
 					ru = new ConditionalRule(l, ru, conditionMap.get(sp.c.id));
 				}
 				i.remove();
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
 		for (SyntacticParse sp : map.values()) {
 			SequenceFragment list = sp.f;
 			Set<Label> labels = allLabels(list);
 			for (Label l : labels) {
 				if (l.t == Type.indeterminate && !knownIds.contains(l.id)) {
 					if (precompiledRules.containsKey(l.id)) {
 						Rule ru = precompiledRules.get(l.id);
						ru.generation = 0;
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
 		for (Entry<Label, SyntacticParse> e : map.entrySet()) {
 			Set<Label> dependents = new HashSet<Label>(
 					allLabels(e.getValue().f));
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
 			List<Entry<Label, SyntacticParse>> generation = new LinkedList<Entry<Label, SyntacticParse>>();
 			for (Iterator<Entry<Label, SyntacticParse>> i = map.entrySet()
 					.iterator(); i.hasNext();) {
 				Entry<Label, SyntacticParse> e = i.next();
 				Set<Label> labels = allLabels(e.getValue().f);
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
 				final Entry<Label, SyntacticParse> e;
 				final int length;
 
 				Sorter(Entry<Label, SyntacticParse> e2) {
 					int i = 0;
 					this.e = e2;
 					for (RuleFragment r : e2.getValue().f.sequence)
 						i += r.toString().length() + 1;
 					length = i;
 				}
 
 				@Override
 				public int compareTo(Sorter o) {
 					return length - o.length;
 				}
 
 			}
 			List<Sorter> sorters = new ArrayList<Sorter>(generation.size());
 			for (Entry<Label, SyntacticParse> e : generation)
 				sorters.add(new Sorter(e));
 			Collections.sort(sorters);
 			// now we make the rules
 			for (Sorter s : sorters) {
 				Label l = s.e.getKey();
 				SequenceFragment body = s.e.getValue().f;
 				Rule ru = parseRule(l, body, null, s.e.getValue().c);
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
 
 	private void checkForVisibleSpace(SequenceFragment body) {
 		if (!rules.containsKey(VisibleSpace.LABEL)) {
 			for (RuleFragment rf : body.sequence) {
 				if (rf instanceof Label) {
 					Label l = (Label) rf;
 					if (l.id.equals(VisibleSpace.LABEL.id)) {
 						rules.put(VisibleSpace.LABEL, new VisibleSpace());
 						return;
 					}
 				} else if (rf instanceof GroupFragment) {
 					for (SequenceFragment sf : ((GroupFragment) rf).alternates) {
 						checkForVisibleSpace(sf);
 						if (rules.containsKey(VisibleSpace.LABEL))
 							return;
 					}
 				}
 			}
 		}
 	}
 
 	private boolean redundancyTest(SequenceFragment body) {
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
 	private void resolveRecursions(Map<Label, SyntacticParse> map,
 			int generation) {
 		for (Iterator<Entry<Label, Set<Label>>> i = dependencyMap.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<Label, Set<Label>> e = i.next();
 			if (map.containsKey(e.getKey()))
 				e.getValue().retainAll(map.keySet());
 			else
 				i.remove();
 		}
 		Map<Label, SyntacticParse> copy = new HashMap<Label, SyntacticParse>(
 				map);
 		removeStrictlyDominating(copy);
 		List<List<Entry<Label, SyntacticParse>>> cycles = separateCycles(copy);
 		for (List<Entry<Label, SyntacticParse>> cycle : cycles) {
 			processCycle(cycle, generation);
 			for (Entry<Label, SyntacticParse> e : cycle)
 				map.remove(e.getKey());
 		}
 	}
 
 	/**
 	 * Creates a set of mutually dependent rules.
 	 * 
 	 * @param cycle
 	 * @param generation
 	 */
 	private void processCycle(List<Entry<Label, SyntacticParse>> cycle,
 			int generation) {
 		testCycle(cycle);
 		Map<Label, CyclicRule> cycleMap = new HashMap<Label, CyclicRule>(
 				cycle.size() * 2);
 		for (Entry<Label, SyntacticParse> e : cycle) {
 			CyclicRule ddr = new CyclicRule(new Label(Type.explicit,
 					e.getKey().id));
 			cycleMap.put(e.getKey(), ddr);
 		}
 		for (Entry<Label, SyntacticParse> e : cycle) {
 			Rule r = parseRule(e.getKey(), e.getValue().f, cycleMap,
 					e.getValue().c);
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
 	private void testCycle(List<Entry<Label, SyntacticParse>> cycle) {
 		Set<Label> set = new HashSet<Label>(cycle.size() * 2);
 		for (Entry<Label, SyntacticParse> e : cycle)
 			set.add(e.getKey());
 		for (Entry<Label, SyntacticParse> e : cycle) {
 			if (findEscape(e, set))
 				return;
 		}
 		StringBuilder b = new StringBuilder();
 		b.append("cycle found in rules: ");
 		boolean nonInitial = false;
 		for (Entry<Label, SyntacticParse> e : cycle) {
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
 	private boolean findEscape(Entry<Label, SyntacticParse> e, Set<Label> set) {
 		SyntacticParse list = e.getValue();
 		for (RuleFragment r : list.f.sequence) {
 			if (!set.contains(r))
 				return true;
 			if (((RepeatableRuleFragment) r).rep.bottom == 0)
 				return true;
 		}
 		return false;
 	}
 
 	private List<List<Entry<Label, SyntacticParse>>> separateCycles(
 			Map<Label, SyntacticParse> copy) {
 		List<List<Entry<Label, SyntacticParse>>> cycles = new LinkedList<List<Entry<Label, SyntacticParse>>>();
 		while (true) {
 			// first, we find the entry with the fewest dependencies
 			List<Entry<Label, SyntacticParse>> list = new ArrayList<Entry<Label, SyntacticParse>>(
 					copy.entrySet());
 			Collections.sort(list,
 					new Comparator<Entry<Label, SyntacticParse>>() {
 						@Override
 						public int compare(Entry<Label, SyntacticParse> o1,
 								Entry<Label, SyntacticParse> o2) {
 							return dependencyMap.get(o1.getKey()).size()
 									- dependencyMap.get(o2.getKey()).size();
 						}
 					});
 			Entry<Label, SyntacticParse> least = list.get(0);
 			Set<Label> set = new HashSet<Label>();
 			set.add(least.getKey());
 			List<Entry<Label, SyntacticParse>> cycle = new LinkedList<Entry<Label, SyntacticParse>>();
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
 			for (Iterator<Entry<Label, SyntacticParse>> i = copy.entrySet()
 					.iterator(); i.hasNext();) {
 				Entry<Label, SyntacticParse> e = i.next();
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
 	private void removeStrictlyDominating(Map<Label, SyntacticParse> copy) {
 		while (true) {
 			Set<Label> required = new HashSet<Label>(copy.size() * 2);
 			for (SyntacticParse list : copy.values())
 				required.addAll(allLabels(list.f));
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
 
 	private Rule parseRule(Label label, SequenceFragment fragments,
 			Map<Label, CyclicRule> cycleMap, ConditionFragment cf) {
 		Rule r;
 		if (fragments.size() == 1) {
 			RuleFragment rf = fragments.get(0);
 			if (isNamedCapture(rf)) {
 				GroupFragment gf = (GroupFragment) rf;
 				r = parseRule(label, gf.alternates.get(0), cycleMap, cf);
 				r.setLabels(gf.alternateTags);
 			} else
 				r = makeSingle(label, fragments.get(0), cycleMap, cf);
 		} else
 			r = makeSequence(label, fragments, cycleMap, cf);
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
 			Map<Label, CyclicRule> cycleMap, ConditionFragment cf) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSingle(ruleFragment, cycleMap);
 		if (cf == null)
 			r = fixLabel(label, r);
 		else
 			r = new ConditionalRule(label, r, conditionMap.get(cf.id));
 		return r;
 	}
 
 	/**
 	 * Assigns a name to an anonymous {@link Rule}.
 	 * 
 	 * @param label
 	 * @param r
 	 * @return named {@link Rule}
 	 */
 	Rule fixLabel(Label label, Rule r) {
 		Rule ru = null;
 		Set<String> labels = r.labels;
 		if (r instanceof AlternationRule) {
 			AlternationRule ar = (AlternationRule) r;
 			AlternationRule ar2 = new AlternationRule(label, ar.alternates,
 					ar.tagMap);
 			ru = ar2;
 		} else if (r instanceof RepetitionRule) {
 			RepetitionRule rr = (RepetitionRule) r;
 			RepetitionRule rr2 = new RepetitionRule(label, rr.r, rr.repetition,
 					new HashSet<String>(rr.alternateTags));
 			ru = rr2;
 		} else if (r instanceof SequenceRule) {
 			SequenceRule sr = (SequenceRule) r;
 			SequenceRule sr2 = new SequenceRule(label, sr.sequence, sr.tagList);
 			ru = sr2;
 		} else if (r instanceof LiteralRule) {
 			LiteralRule lr = (LiteralRule) r;
 			LiteralRule lr2 = new LiteralRule(label, lr.literal);
 			ru = lr2;
 		} else if (r instanceof ConditionalRule) {
 			ConditionalRule ocr = (ConditionalRule) r;
 			ConditionalRule cr = new ConditionalRule(label, ocr.r, ocr.c);
 			ru = cr;
 		} else if (r instanceof LeafRule) {
 			LeafRule lr = (LeafRule) r;
 			ru = new LeafRule(label, lr.p, lr.reversible);
 		} else if (r instanceof DeferredDefinitionRule) {
 			DeferredDefinitionRule ddr = (DeferredDefinitionRule) r;
 			DeferredDefinitionRule ddr2 = new DeferredDefinitionRule(label);
 			ddr2.r = ddr;
 			ru = ddr2;
 		}
 		if (ru == null)
 			throw new GrammarException("unanticipated rule type: "
 					+ r.getClass().getName());
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
 	 * @param whitespaceCondition
 	 * @return
 	 */
 	private Rule makeSingle(RuleFragment rf, Map<Label, CyclicRule> cycleMap) {
 		if (rf instanceof AssertionFragment) {
 			AssertionFragment af = (AssertionFragment) rf;
 			Rule sr = makeSingle(af.rf, cycleMap);
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
 			if (!l.rep.redundant()) {
 				Label label = new Label(Type.implicit, new Label(Type.explicit,
 						l.id) + l.rep.toString());
 				r = new RepetitionRule(label, r, l.rep, EMPTY_STR_SET);
 			}
 			return r;
 		} else if (rf instanceof LiteralFragment) {
 			LiteralFragment lf = (LiteralFragment) rf;
 			Label l = new Label(Type.implicit, '"' + lf.literal + '"');
 			Rule r = new LiteralRule(l, lf.literal);
 			if (lf.rep.redundant()) {
 				return r;
 			}
 			l = new Label(Type.implicit, lf.toString());
 			r = new RepetitionRule(l, r, lf.rep, EMPTY_STR_SET);
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
 			Label l = new Label(Type.implicit, rf.toString());
 			Rule r = new LeafRule(l, rx.re, rx.reversible);
 			if (rx.rep.redundant()) {
 				return r;
 			}
 			l = new Label(Type.implicit, rx.toString() + rx.rep);
 			r = new RepetitionRule(l, r, rx.rep, EMPTY_STR_SET);
 			return r;
 		}
 		GroupFragment gf = (GroupFragment) rf;
 		Set<String> tags = gf.alternateTags;
 		Rule r = null;
 		if (gf.alternates.size() == 1) {
 			// repeated rule with capture
 			if (gf.alternates.get(0).size() == 1) {
 				r = makeSingle(gf.alternates.get(0).get(0), cycleMap);
 			} else {
 				// repeated sequence
 				r = makeSequence(gf.alternates.get(0), cycleMap);
 			}
 		} else {
 			// repeated alternation
 			List<Rule> alternates = new ArrayList<Rule>(gf.alternates.size());
 			StringBuilder b = new StringBuilder();
 			b.append('[');
 			boolean nonInitial = false;
 			Map<String, Set<String>> tagMap = new HashMap<String, Set<String>>(
 					gf.alternates.size() * 2);
 			for (SequenceFragment alternate : gf.alternates) {
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
 										cycleMap);
 							else
 								r = makeSequence(gfInner.alternates.get(0),
 										cycleMap);
 						} else
 							r = makeSingle(rfInner, cycleMap);
 					} else {
 						r = makeSingle(rfInner, cycleMap);
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
 					r = makeSequence(alternate, cycleMap);
 					tagMap.put(r.uniqueId(), EMPTY_STR_SET);
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
 				return r;
 			}
 		}
 		if (!gf.rep.redundant()) {
 			Label l = new Label(Type.implicit, r.label().toString() + gf.rep);
 			r = new RepetitionRule(l, r, gf.rep, tags);
 		}
 		return r;
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
 		} else if (sr instanceof ConditionalRule) {
 			ConditionalRule cl = (ConditionalRule) sr;
 			Rule rr = reverse(cl.r);
 			ConditionalRule cl2 = new ConditionalRule(new Label(Type.implicit,
 					id), rr, cl.c);
 			ru = cl2;
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
 		ru.unreversed = sr;
 		return ru;
 	}
 
 	private Rule makeSequence(Label label, SequenceFragment fragments,
 			Map<Label, CyclicRule> cycleMap, ConditionFragment cf) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSequence(fragments, cycleMap);
 		if (cf == null)
 			r = fixLabel(label, r);
 		else
 			r = new ConditionalRule(label, r, conditionMap.get(cf.id));
 		return r;
 	}
 
 	private Rule makeSequence(SequenceFragment value,
 			Map<Label, CyclicRule> cycleMap) {
 		if (value.size() == 1)
 			throw new GrammarException(
 					"logic error in compiler; no singleton lists should arrive at this point");
 		Rule[] sequence = new Rule[value.size()];
 		int index = 0;
 		boolean nonInitial = false;
 		List<Set<String>> tagList = new ArrayList<Set<String>>(value.size());
 		StringBuilder b = new StringBuilder();
 		b.append('[');
 		for (RuleFragment rf : value.sequence) {
 			Set<String> tagSet = null;
 			Rule r = null;
 			if (rf instanceof GroupFragment) {
 				GroupFragment gf = (GroupFragment) rf;
 				if (isNamedCapture(gf)) {
 					tagSet = gf.alternateTags;
 					if (gf.alternates.size() == 1) {
 						if (gf.alternates.get(0).size() == 1)
 							r = makeSingle(gf.alternates.get(0).get(0),
 									cycleMap);
 						else
 							r = makeSequence(gf.alternates.get(0), cycleMap);
 					}
 				} else {
 					tagSet = EMPTY_STR_SET;
 					r = makeSingle(rf, cycleMap);
 				}
 			} else {
 				tagSet = EMPTY_STR_SET;
 				r = makeSingle(rf, cycleMap);
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
 		if (value.getSpaceRequired() != Label.Whitespace.none) {
 			Condition c = value.getSpaceRequired() == Label.Whitespace.required ? new SpaceCondition()
 					: new MaybeSpaceCondition();
 			l = new Label(Type.implicit, b.append('(').append(c.describe(true))
 					.append(')').toString());
 			r = new ConditionalRule(l, r, c);
 		}
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
 
 	private Set<Label> allLabels(SequenceFragment list2) {
 		Set<Label> allLabels = new TreeSet<Label>();
 		for (RuleFragment rf : list2.sequence) {
 			if (rf instanceof Label)
 				allLabels.add((Label) rf);
 			else if (rf instanceof GroupFragment) {
 				GroupFragment gf = (GroupFragment) rf;
 				for (SequenceFragment l : gf.alternates) {
 					allLabels.addAll(allLabels(l));
 				}
 			} else if (rf instanceof AssertionFragment) {
 				AssertionFragment af = (AssertionFragment) rf;
 				SequenceFragment list = new SequenceFragment();
 				list.add(af.rf);
 				allLabels.addAll(allLabels(list));
 			}
 		}
 		return allLabels;
 	}
 
 	Label root() {
 		return root;
 	}
 
 	Map<String, Set<String>> undefinedConditions() {
 		return undefinedConditions;
 	}
 
 	public boolean setWhitespaceCondition() {
 		return setWhitespaceCondition;
 	}
 }
