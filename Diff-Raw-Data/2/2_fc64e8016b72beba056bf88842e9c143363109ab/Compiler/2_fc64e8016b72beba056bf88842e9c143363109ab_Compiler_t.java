 package dfh.grammar;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
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
 	private Collection<Label> undefinedTerminals;
 	private Map<String, Rule> redundancyMap = new TreeMap<String, Rule>();
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
 		Set<Label> allLabels = new HashSet<Label>(map.size()), terminals = new HashSet<Label>(
 				map.size()), knownLabels = new HashSet<Label>(map.keySet());
 		// first we extract all the terminals we can
 		for (Iterator<Entry<Label, List<RuleFragment>>> i = map.entrySet()
 				.iterator(); i.hasNext();) {
 			Entry<Label, List<RuleFragment>> e = i.next();
 			if (e.getKey().t == Type.terminal) {
 				Label l = e.getKey();
 				i.remove();
 				allLabels.add(l);
 				knownLabels.add(l);
 				terminals.add(l);
 				Rule ru = new LeafRule(l, ((Regex) e.getValue().get(0)).re);
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
 		for (List<RuleFragment> list : map.values()) {
 			Set<Label> labels = allLabels(list);
 			for (Label l : labels) {
 				if (l.t == Type.terminal && !terminals.contains(l)) {
 					DeferredDefinitionRule ddr = new DeferredDefinitionRule(l,
 							rules);
 					rules.put(l, ddr);
 					terminals.add(l);
 					allLabels.add(l);
 					knownLabels.add(l);
 				}
 			}
 		}
 		// now we define the remainder
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
 			if (map.size() == size)
 				throw new GrammarException(
 						"impossible co-dependencies exist in rule set");
 
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
 				rules.put(e.getKey(), parseRule(e.getKey(), e.getValue()));
 				Set<Label> labels = allLabels(e.getValue());
 				allLabels.addAll(labels);
 				allLabels.add(e.getKey());
 			}
 		}
 
 		// now we look for errors
 
 		terminalLabelMap = new HashMap<String, Label>(terminals.size());
 		for (Label l : terminals)
 			terminalLabelMap.put(l.id, l);
 		allLabels.removeAll(knownLabels);
 		allLabels.removeAll(terminals);
 		if (!allLabels.isEmpty()) {
 			// undefined rules; generate error message
 			LinkedList<String> list = new LinkedList<String>();
 			for (Label l : allLabels)
 				list.add(l.id);
 			Collections.sort(list);
 			StringBuilder b = new StringBuilder(list.pollFirst());
 			for (String s : list)
 				b.append(", ").append(s);
 			throw new GrammarException("undefined rules: " + b);
 		}
 
 		// now we add in all the synthetic rules
 		redundancyMap.keySet().removeAll(redundantLabels);
 		for (Rule ru : redundancyMap.values())
 			rules.put(ru.label(), ru);
 
 		// and we prepare to receive definitions for any deferred rules
 		terminals.removeAll(rules.keySet());
 		undefinedTerminals = new HashSet<Label>(terminals);
 	}
 
 	private Rule parseRule(Label label, List<RuleFragment> fragments) {
 		Rule r;
 		if (fragments.size() == 1)
 			r = makeSingle(label, fragments.get(0));
 		else
 			r = makeSequence(label, fragments);
 		String id = r.uniqueId();
 		Rule old = redundancyMap.remove(id);
 		if (old != null) {
 			redundantLabels.add(id);
 			redundancyMap.put(id, r);
 		}
 		return r;
 	}
 
 	private Rule makeSingle(Label label, RuleFragment ruleFragment) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSingle(ruleFragment);
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
 	 * @return
 	 */
 	private Rule makeSingle(RuleFragment rf) {
 		if (rf instanceof Label) {
 			Label l = (Label) rf;
 			Rule r = rules.get(l);
 			if (l.rep.redundant())
 				return r;
 			Label label = new Label(Type.nonTerminal, l.toString());
 			r = new RepetitionRule(label, r, l.rep);
 			return redundancyCheck(r);
 		} else if (rf instanceof LiteralFragment) {
 			LiteralFragment lf = (LiteralFragment) rf;
 			Label l = new Label(Type.terminal, '"' + lf.literal + '"');
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
 				Rule r = makeSingle(gf.alternates.get(0).get(0));
 				if (gf.rep.redundant())
 					return r;
 				else {
 					Label l = new Label(Type.nonTerminal, r.label().toString()
 							+ gf.rep);
 					r = new RepetitionRule(l, r, gf.rep);
 					return redundancyCheck(r);
 				}
 			}
 			return makeSequence(gf.alternates.get(0));
 		}
 		Rule[] alternates = new Rule[gf.alternates.size()];
 		int index = 0;
 		StringBuilder b = new StringBuilder();
 		b.append('[');
 		boolean nonInitial = false;
 		for (List<RuleFragment> alternate : gf.alternates) {
 			Rule r;
 			if (alternate.size() == 1)
 				r = makeSingle(alternate.get(0));
 			else
 				r = makeSequence(alternate);
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
 
 	private Rule makeSequence(Label label, List<RuleFragment> fragments) {
 		if (rules.containsKey(label))
 			return rules.get(label);
 		Rule r = makeSequence(fragments);
 		r = fixLabel(label, r);
 		return r;
 	}
 
 	private Rule makeSequence(List<RuleFragment> value) {
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
 			Rule r = makeSingle(rf);
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
