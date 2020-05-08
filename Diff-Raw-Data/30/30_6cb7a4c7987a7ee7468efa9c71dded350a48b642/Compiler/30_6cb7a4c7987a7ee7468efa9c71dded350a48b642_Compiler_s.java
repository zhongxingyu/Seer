 package dfh.grammar;
 
 import java.io.IOException;
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
 import java.util.TreeMap;
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
 	private Collection<Label> undefinedTerminals = new HashSet<Label>();
 	private Map<String, Rule> redundancyMap = new TreeMap<String, Rule>();
 	private Map<Label, Set<Label>> dependencyMap = new HashMap<Label, Set<Label>>();
 	private List<String> redundantLabels = new LinkedList<String>();
 	private final Label root;
 
 	public Compiler(LineReader reader) throws GrammarException, IOException {
 		String line = null;
 		Map<Label, List<RuleFragment>> map = new HashMap<Label, List<RuleFragment>>();
 		Label r = null;
 		while ((line = reader.readLine()) != null) {
 			List<RuleFragment> list = RuleParser.parse(line);
 			if (list == null)
 				continue; // blank line or comment
 			Label l = (Label) list.remove(0);
 			if (map.containsKey(l))
 				throw new GrammarException("rule " + l + " redefined at line "
 						+ reader.lineNumber());
 			map.put(l, list);
 			if (l.t == Type.root)
 				r = l;
 		}
 		if (r == null)
 			throw new GrammarException("no root rule found");
 		this.root = r;
 
 		// make space for anonymous rules
 		rules = new HashMap<Label, Rule>(map.size() * 2);
 		Set<Label> terminals = new HashSet<Label>(map.size());
 		int gen = 1;
 		// first we extract all the terminals we can
 		for (Iterator<Entry<Label, List<RuleFragment>>> i = map.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<Label, List<RuleFragment>> e = i.next();
 			if (e.getKey().t == Type.terminal) {
 				Label l = e.getKey();
 				i.remove();
 				terminals.add(l);
 				Rule ru = new LeafRule(l, ((Regex) e.getValue().get(0)).re);
 				ru.generation = gen;
 				String id = ru.uniqueId();
 				Rule old = redundancyMap.get(id);
 				if (old == null) {
 					redundancyMap.put(id, ru);
 					rules.put(l, ru);
 				} else
 					rules.put(l, old);
 			}
 		}
 		// now we extract all the deferred definition rules
 		gen = 0;
 		for (List<RuleFragment> list : map.values()) {
 			Set<Label> labels = allLabels(list);
 			for (Label l : labels) {
 				if (l.t == Type.terminal && !terminals.contains(l)) {
 					DeferredDefinitionRule ddr = new DeferredDefinitionRule(l,
 							rules);
 					ddr.generation = gen;
 					undefinedTerminals.add(l);
 					rules.put(l, ddr);
 					terminals.add(l);
 				}
 			}
 		}
 		// create dependency map
 		for (Entry<Label, List<RuleFragment>> e : map.entrySet()) {
 			Set<Label> dependents = new HashSet<Label>(allLabels(e.getValue()));
 			for (Label l : dependents) {
 				if (!(map.containsKey(l) || rules.containsKey(l)))
 					throw new GrammarException("undefined rule: " + l);
 			}
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
				resolveRecursions(map);
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
 				Entry<Label, List<RuleFragment>> e = s.e;
 				Rule ru = parseRule(e.getKey(), e.getValue(), null);
 				ru.generation = gen;
 				rules.put(e.getKey(), ru);
 			}
 			gen++;
 		}
 
 		terminalLabelMap = new HashMap<String, Label>(terminals.size());
 		for (Label l : terminals)
 			terminalLabelMap.put(l.id, l);
 
 		// now we add in all the synthetic rules
 		redundancyMap.keySet().removeAll(redundantLabels);
 		for (Rule ru : redundancyMap.values())
 			rules.put(ru.label(), ru);
 	}
 
 	/**
 	 * Resolve those recursions that will not lead to inescapable loops.
 	 * 
 	 * @param map
 	 */
	private void resolveRecursions(Map<Label, List<RuleFragment>> map) {
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
			processCycle(cycle);
 			for (Entry<Label, List<RuleFragment>> e : cycle)
 				map.remove(e.getKey());
 		}
 	}
 
 	/**
 	 * Creates a set of mutually dependent rules.
 	 * 
 	 * @param cycle
 	 */
	private void processCycle(List<Entry<Label, List<RuleFragment>>> cycle) {
 		testCycle(cycle);
 		Map<Label, CyclicRule> cycleMap = new HashMap<Label, CyclicRule>(
 				cycle.size());
 		for (Entry<Label, List<RuleFragment>> e : cycle) {
 			CyclicRule ddr = new CyclicRule(e.getKey());
 			cycleMap.put(e.getKey(), ddr);
 		}
 		for (Entry<Label, List<RuleFragment>> e : cycle) {
 			Rule r = parseRule(e.getKey(), e.getValue(), cycleMap);
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
 		Set<Label> set = new HashSet<Label>(cycle.size());
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
 			Set<Label> required = new HashSet<Label>(copy.size());
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
 		if (fragments.size() == 1)
 			r = makeSingle(label, fragments.get(0), cycleMap);
 		else
 			r = makeSequence(label, fragments, cycleMap);
 		String id = r.uniqueId();
 		Rule old = redundancyMap.remove(id);
 		if (old != null) {
 			redundantLabels.add(id);
 			redundancyMap.put(id, r);
 		}
 		return r;
 	}
 
 	private Rule makeSingle(Label label, RuleFragment ruleFragment,
 			Map<Label, CyclicRule> cycleMap) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSingle(ruleFragment, cycleMap);
 		r = fixLabel(label, r);
 		return r;
 	}
 
 	private Rule fixLabel(Label label, Rule r) {
 		if (r instanceof AlternationRule)
 			return new AlternationRule(label, ((AlternationRule) r).alternates);
 		if (r instanceof RepetitionRule) {
 			RepetitionRule rr = (RepetitionRule) r;
 			return new RepetitionRule(label, rr.r, rr.repetition);
 		}
 		if (r instanceof SequenceRule)
 			return new SequenceRule(label, ((SequenceRule) r).sequence);
 		if (r instanceof LiteralRule)
 			return new LiteralRule(label, ((LiteralRule) r).literal);
 		throw new GrammarException("unanticipated rule type: "
 				+ r.getClass().getName());
 	}
 
 	private Rule redundancyCheck(Rule r) {
 		String id = r.uniqueId();
 		if (redundancyMap.containsKey(id))
 			return redundancyMap.get(id);
 		else
 			redundancyMap.put(id, r);
 		return r;
 	}
 
 	/**
 	 * Makes a rule with a bogus label
 	 * 
 	 * @param rf
 	 * @param cycleMap
 	 * @return
 	 */
 	private Rule makeSingle(RuleFragment rf, Map<Label, CyclicRule> cycleMap) {
 		if (rf instanceof Label) {
 			Label l = (Label) rf;
 			Rule r = rules.get(l);
 			if (r == null)
 				r = cycleMap.get(l);
 			if (l.rep.redundant())
 				return r;
 			Label label = new Label(Type.nonTerminal, l.toString());
 			r = new RepetitionRule(label, r, l.rep);
 			return redundancyCheck(r);
 		} else if (rf instanceof LiteralFragment) {
 			LiteralFragment lf = (LiteralFragment) rf;
 			Label l = new Label(Type.literal, '"' + lf.literal + '"');
 			Rule r = new LiteralRule(l, lf.literal);
 			r = redundancyCheck(r);
 			if (lf.rep.redundant())
 				return r;
 			l = new Label(Type.nonTerminal, lf.toString());
 			r = new RepetitionRule(l, r, lf.rep);
 			return redundancyCheck(r);
 		} else if (rf instanceof BackReferenceFragment) {
 			BackReferenceFragment brf = (BackReferenceFragment) rf;
 			Label l = new Label(Type.backreference, rf.toString());
 			Rule r = new BackReferenceRule(l, brf.reference);
 			return redundancyCheck(r);
 		}
 		GroupFragment gf = (GroupFragment) rf;
 		if (gf.alternates.size() == 1) {
 			if (gf.alternates.get(0).size() == 1) {
 				Rule r = makeSingle(gf.alternates.get(0).get(0), cycleMap);
 				if (gf.rep.redundant())
 					return r;
 				else {
 					Label l = new Label(Type.nonTerminal, r.label().toString()
 							+ gf.rep);
 					r = new RepetitionRule(l, r, gf.rep);
 					return redundancyCheck(r);
 				}
 			}
 			Rule r = makeSequence(gf.alternates.get(0), cycleMap);
 			if (gf.rep.redundant())
 				return r;
 			Label l = new Label(Type.nonTerminal, r.label().toString() + gf.rep);
 			r = new RepetitionRule(l, r, gf.rep);
 			return redundancyCheck(r);
 		}
 		Rule[] alternates = new Rule[gf.alternates.size()];
 		int index = 0;
 		StringBuilder b = new StringBuilder();
 		b.append('[');
 		boolean nonInitial = false;
 		for (List<RuleFragment> alternate : gf.alternates) {
 			Rule r;
 			if (alternate.size() == 1)
 				r = makeSingle(alternate.get(0), cycleMap);
 			else
 				r = makeSequence(alternate, cycleMap);
 			alternates[index++] = r;
 			if (nonInitial)
 				b.append('|');
 			else
 				nonInitial = true;
 			b.append(r.label());
 		}
 		b.append(']');
 		Label l = new Label(Type.nonTerminal, b.toString());
 		Rule r = new AlternationRule(l, alternates);
 		r = redundancyCheck(r);
 		if (gf.rep.redundant())
 			return r;
 		l = new Label(Type.nonTerminal, l.toString() + gf.rep);
 		r = new RepetitionRule(l, r, gf.rep);
 		return redundancyCheck(r);
 	}
 
 	private Rule makeSequence(Label label, List<RuleFragment> fragments,
 			Map<Label, CyclicRule> cycleMap) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSequence(fragments, cycleMap);
 		r = fixLabel(label, r);
 		return r;
 	}
 
 	private Rule makeSequence(List<RuleFragment> value,
 			Map<Label, CyclicRule> cycleMap) {
 		if (value.size() == 1)
 			throw new GrammarException(
 					"logic error in compiler; no singleton lists should arrive at this point");
 		Rule[] sequence = new Rule[value.size()];
 		int index = 0;
 		boolean nonInitial = false;
 		StringBuilder b = new StringBuilder();
 		b.append('[');
 		for (RuleFragment rf : value) {
 			if (nonInitial)
 				b.append(' ');
 			else
 				nonInitial = true;
 			Rule r = makeSingle(rf, cycleMap);
 			sequence[index++] = r;
 			b.append(r.label());
 		}
 		b.append(']');
 		Label l = new Label(Type.nonTerminal, b.toString());
 		Rule r = new SequenceRule(l, sequence);
 		return redundancyCheck(r);
 	}
 
 	public Map<Label, Rule> rules() {
 		return new HashMap<Label, Rule>(rules);
 	}
 
 	public Map<String, Label> terminalLabelMap() {
 		return new HashMap<String, Label>(terminalLabelMap);
 	}
 
 	public HashSet<Label> undefinedTerminals() {
 		return new HashSet<Label>(undefinedTerminals);
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
 			}
 		}
 		return allLabels;
 	}
 
 	public Label root() {
 		return root;
 	}
 }
