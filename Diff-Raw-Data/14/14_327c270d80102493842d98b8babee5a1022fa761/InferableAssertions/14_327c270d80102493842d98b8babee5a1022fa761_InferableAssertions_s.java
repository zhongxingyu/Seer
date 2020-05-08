 /**
  * InferrableAssertions.java, (c) 2012, Immanuel Albrecht; Dresden University of
  * Technology, Professur für die Psychologie des Lernen und Lehrens
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.tu_dresden.psy.inference.regexp.xml;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import de.tu_dresden.psy.inference.AssertionEquivalenceClasses;
 import de.tu_dresden.psy.inference.AssertionInterface;
 import de.tu_dresden.psy.inference.EquivalentAssertions;
 import de.tu_dresden.psy.inference.ExcessLimit;
 import de.tu_dresden.psy.inference.InferenceMap;
 import de.tu_dresden.psy.inference.InferenceMaps;
 import de.tu_dresden.psy.inference.forms.DisjunctiveNormalForm;
 import de.tu_dresden.psy.inference.regexp.ConstrainedAssertionFilter;
 
 /**
  * class for keeping track of inferred assertions
  * 
  * @author immanuel
  * 
  */
 
 public class InferableAssertions {
 
 	public static enum State {
 		/**
 		 * there are given assertions that have to be closed under the given
 		 * rules
 		 */
 		open,
 		/**
 		 * there are no more inferable assertions using the given rules, and
 		 * none of them are invalid
 		 */
 		closed,
 		/**
 		 * some invalid assertions could be inferred
 		 */
 		invalid,
 		/**
 		 * inference did not stop within some given limit
 		 */
 		excess
 	};
 
 	private AssertionEquivalenceClasses givenAssertions;
 	private AssertionEquivalenceClasses validAssertions;
 	private Set<InferenceMap> usedRules;
 	private Set<ConstrainedAssertionFilter> invalid, trivial;
 	private Set<AssertionInterface> invalidInferredAssertions;
 	private InferableAssertions.State state;
 
 	/**
 	 * used to make a premise vector for triviality tests
 	 */
 	private Set<AssertionInterface> trivialInferred;
 
 	/**
 	 * 
 	 * @param implicit
 	 * @param given
 	 * @param collection
 	 * @param invalid
 	 * @param trivial
 	 */
 
 	public InferableAssertions(Set<AssertionInterface> implicit,
 			Set<AssertionInterface> given, Collection<InferenceMap> rules,
 			Set<ConstrainedAssertionFilter> invalid,
 			Set<ConstrainedAssertionFilter> trivial) {
 		givenAssertions = new AssertionEquivalenceClasses();
 
 		givenAssertions.addNewAssertions(implicit);
 
 		for (AssertionInterface a : givenAssertions.getClasses()) {
 			if (a instanceof EquivalentAssertions) {
 				EquivalentAssertions ea = (EquivalentAssertions) a;
 				ea.considerJustified();
 			}
 		}
 
 		givenAssertions.addNewAssertions(given);
 		validAssertions = new AssertionEquivalenceClasses(givenAssertions);
 
 		state = State.open;
 
 		invalidInferredAssertions = new HashSet<AssertionInterface>();
 
 		trivialInferred = new HashSet<AssertionInterface>();
 
 		usedRules = new HashSet<InferenceMap>(rules.size());
 		usedRules.addAll(rules);
 
 		this.invalid = invalid;
 		this.trivial = trivial;
 	}
 
 	/**
 	 * 
 	 * @return valid assertions that have been inferred
 	 */
 
 	public AssertionEquivalenceClasses getValid() {
 		return validAssertions;
 	}
 
 	/**
 	 * 
 	 * @return given assertions
 	 */
 
 	public AssertionEquivalenceClasses getGiven() {
 		return givenAssertions;
 	}
 
 	/**
 	 * 
 	 * @return a quick report about the state of the inference mechanism :)
 	 */
 
 	public String getReport() {
 		String report = "";
 
 		if (state == State.open) {
 			report += "No inference attempts have been made so far.\n";
 		} else if (state == State.closed) {
 			report += "All inferable assertions have been found.\n";
 		} else if (state == State.closed) {
 			report += "It was possible to infer invalid assertions.\n";
 		} else if (state == State.excess) {
 			report += "Inference was stopped because the process exceeded the time limit.\n";
 		}
 
 		if (invalidInferredAssertions.isEmpty() == false) {
 			report += "\nThe following invalid assertions could be inferred:\n";
 			for (AssertionInterface ia : invalidInferredAssertions) {
 				report += " !> " + ia.getSubject() + "·" + ia.getPredicate()
 						+ "·" + ia.getObject() + "\n";
 			}
 		}
 
 		Set<String> orderedOutput = new TreeSet<String>();
 
 		for (AssertionInterface a : validAssertions.getClasses()) {
 			EquivalentAssertions ea = (EquivalentAssertions) a;
 			if (ea.getJustificationDepth() == EquivalentAssertions.notJustified) {
 				orderedOutput.add(" [unjustified] " + ea.getSubject() + "·"
 						+ ea.getPredicate() + "·" + ea.getObject() + "\n");
 			} else {
 				orderedOutput.add(" ["
 						+ String.format("%1d", ea.getJustificationDepth())
 						+ "-justified] " + ea.getSubject() + "·"
 						+ ea.getPredicate() + "·" + ea.getObject() + "\n");
 			}
 		}
 
 		for (String s : orderedOutput) {
 			report += s;
 		}
 
 		return report;
 	}
 
 	/**
 	 * 
 	 * update ancestor terms
 	 * 
 	 * @param excessLimit
 	 * 
 	 */
 
 	public void calculateAncestors(ExcessLimit excessLimit) {
 		validAssertions.calculateAncestors(excessLimit);
 	}
 
 	/**
 	 * 
 	 * @return all inferred assertions
 	 */
 
 	public Set<AssertionInterface> getInferred() {
 		return validAssertions.getClasses();
 	}
 
 	/**
 	 * 
 	 * @return invalid inferred assertions
 	 */
 
 	public Set<AssertionInterface> getInvalid() {
 		return invalidInferredAssertions;
 	}
 
 	/**
 	 * 
 	 * @return closure state
 	 */
 	public InferableAssertions.State getState() {
 		return state;
 	}
 
 	/**
 	 * NOTE: close the valid assertions first
 	 * 
 	 * @param assertion
 	 * @return true, if assertion has been inferred
 	 */
 	public boolean isInferable(AssertionInterface assertion) {
 		return validAssertions.contains(assertion);
 	}
 
 	/**
 	 * NOTE: close the valid assertions first, and update justification
 	 * 
 	 * @param assertion
 	 * @return justification level of the assertion
 	 */
 	public int justificationLevel(AssertionInterface assertion) {
 		return validAssertions.justification(assertion);
 	}
 
 	/**
 	 * NOTE: close the valid assertions first, then update justification, and
 	 * then calculate ancestors
 	 * 
 	 * @param assertion
 	 * @return all ancestors of the assertion
 	 */
 
 	public DisjunctiveNormalForm<EquivalentAssertions> getAncestors(
 			AssertionInterface assertion) {
 		return validAssertions.ancestors(assertion);
 	}
 
 	/**
 	 * use the given filters to update the justification levels
 	 * 
 	 * @param justified
 	 *            filter for justification
 	 * @param useClosure
 	 *            if true, use all validAssertions as justification base,
 	 *            otherwise only given assertions
 	 */
 
 	public void updateJustification(Set<ConstrainedAssertionFilter> justified,
 			AssertionEquivalenceClasses valid, boolean useClosure) {
 		for (ConstrainedAssertionFilter filter : justified) {
 			for (AssertionInterface a : filter
					.filter((useClosure ? validAssertions : givenAssertions)
 					.getClasses())) {
 				if (a instanceof EquivalentAssertions) {
 					if (valid.contains(a)) {
 						EquivalentAssertions ea = (EquivalentAssertions) a;
 						ea.considerJustified();
 					}
 				}
 			}
 		}
 
 		boolean keep_updating = true;
 		while (keep_updating) {
 			keep_updating = false;
 
 			for (AssertionInterface a : validAssertions.getClasses()) {
 				if (a instanceof EquivalentAssertions) {
 					EquivalentAssertions ea = (EquivalentAssertions) a;
 					if (ea.updateJustificationDepth())
 						keep_updating = true;
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * close the valid assertions
 	 * 
 	 * @param limit
 	 *            excess time limit
 	 * @return closure state
 	 */
 
 	public InferableAssertions.State closeValid(ExcessLimit limit) {
 		InferenceMaps maps = new InferenceMaps(usedRules);
 
 		int lastCount = 0;
 		int validCount = validAssertions.getClasses().size();
 
 		state = State.closed;
 
 		while (validCount > lastCount) {
 
 			if (limit.exceeded()) {
 				state = State.excess;
 				break;
 			}
 
 			lastCount = validCount;
 
 			Set<AssertionInterface> inferred = maps.inferNew(
 					validAssertions.getClasses(), limit);
 
 			if (limit.exceeded()) {
 				state = State.excess;
 			}
 
 			trivialInferred.clear();
 
 			for (ConstrainedAssertionFilter filter : trivial) {
 				trivialInferred.addAll(filter.filter(inferred));
 			}
 
 			for (Iterator<AssertionInterface> i_assertion = inferred.iterator(); i_assertion
 					.hasNext();) {
 				if (trivialInferred.contains(i_assertion.next()))
 					i_assertion.remove();
 			}
 
 			for (ConstrainedAssertionFilter filter : invalid) {
 				invalidInferredAssertions.addAll(filter.filter(inferred));
 			}
 
 			validAssertions.addNewAssertions(inferred);
 			validCount = validAssertions.getClasses().size();
 
 			if (invalidInferredAssertions.isEmpty() == false) {
 				state = State.invalid;
 				break;
 			}
 		}
 
 		return state;
 	}
 
 	/**
 	 * 
 	 * @param justificationNeeded
 	 *            assertion for that the lookup is done
 	 * @param otherGivenAssertions
 	 *            other assertions that may be used
 	 * @return all minimal in justification-depth-plus-one sum of non-given
 	 *         assertions direct ancestor missing sets
 	 */
 
 	public Set<Set<EquivalentAssertions>> getMinimalDepthPrecursorSets(
 			AssertionInterface justificationNeeded,
 			Set<EquivalentAssertions> otherGivenAssertions) {
 		Set<Set<EquivalentAssertions>> minimal_precursors = new HashSet<Set<EquivalentAssertions>>();
 
 		if (isInferable(justificationNeeded) == false)
 			return minimal_precursors;
 
 		if (justificationLevel(justificationNeeded) == EquivalentAssertions.notJustified)
 			return minimal_precursors;
 
 		int minimal_depth = Integer.MAX_VALUE;
 
 		for (Set<EquivalentAssertions> precursors : validAssertions.precursors(
 				justificationNeeded).getTerm()) {
 
 			int depth_sum = 0;
 			Set<EquivalentAssertions> missing = new HashSet<EquivalentAssertions>();
 
 			for (EquivalentAssertions ea : precursors) {
 				if (otherGivenAssertions.contains(ea) == false) {
 					depth_sum += ea.getJustificationDepth() + 1;
 					missing.add(ea);
 				}
 			}
 
 			if (missing.isEmpty()) {
 				/**
 				 * this argument is relatively justified with the given
 				 * assertions
 				 */
 				minimal_precursors.clear();
 				return minimal_precursors;
 			}
 
 			if (depth_sum < minimal_depth) {
 				minimal_precursors.clear();
 				minimal_depth = depth_sum;
 			}
 
 			if (depth_sum == minimal_depth) {
 				minimal_precursors.add(missing);
 			}
 
 		}
 
 		return minimal_precursors;
 	}
 
 	/**
 	 * 
 	 * @param needJustification
 	 * @param otherGivenAssertions
 	 * @return tips for justification
 	 */
 
 	public String getJustificationTips(
 			Set<AssertionInterface> needJustification,
 			Set<EquivalentAssertions> otherGivenAssertions,
 			Map<String, ConstrainedAssertionFilter> qualities) {
 
 		Map<EquivalentAssertions, Set<Set<EquivalentAssertions>>> justified_by = new HashMap<EquivalentAssertions, Set<Set<EquivalentAssertions>>>();
 		
 		Set<String> qualities_lacking = new TreeSet<String>();
 		
 		for (AssertionInterface a : needJustification) {
 			EquivalentAssertions ea = new EquivalentAssertions(a);
 			
 			justified_by.put(ea,
 					getMinimalDepthPrecursorSets(ea, otherGivenAssertions));
 		}
 
 		boolean is_closed = false;
 
 		do_again:
 		while (is_closed == false) {
 			is_closed = true;
 			for (EquivalentAssertions ea : justified_by.keySet()) {
 				for (Set<EquivalentAssertions> combinations : justified_by
 						.get(ea)) {
 					for (EquivalentAssertions ea2 : combinations) {
 						if (justified_by.containsKey(ea2) == false) {
 							justified_by.put(
 									ea2,
 									getMinimalDepthPrecursorSets(ea2,
 											otherGivenAssertions));
 							is_closed = false;
 							continue do_again;
 						}
 					}
 				}
 			}
 		}
 
 		Map<String, String> ordered = new TreeMap<String, String>();
 		
 		Set<AssertionInterface> filter_input = new HashSet<AssertionInterface>();
 
 		for (EquivalentAssertions ea : justified_by.keySet()) {
 			String justifications = "";
 			
 			Set<Set<EquivalentAssertions>> ways = justified_by.get(ea);
 			
 			if (ways.isEmpty())
 				continue;
 
 			for (Set<EquivalentAssertions> combination : ways) {
 				if (justifications.isEmpty() == false)
 					justifications += "\n  or";
 
 				boolean add_and = false;
 
 				for (EquivalentAssertions ea2 : combination) {
 					if (add_and) {
 						justifications += "\n    and ";
 					} else {
 						justifications += "\n        ";
 						add_and = true;
 					}
 					justifications += ea2.getSubject() + "·"
 							+ ea2.getPredicate() + "·" + ea2.getObject();
 
 					for (String name : qualities.keySet()) {
 						if (qualities_lacking.contains(name) == false) {
 							filter_input.clear();
 							filter_input.add(ea2);
 							if (qualities.get(name).filter(filter_input)
 									.isEmpty() == false) {
 								qualities_lacking.add(name);
 							}
 						}
 					}
 				}
 			}
 
 			if (needJustification.contains(ea)) {
 				ordered.put(" " + ea.getSubject() + "·" + ea.getPredicate()
 						+ "·" + ea.getObject(), justifications);
 			} else {
 				ordered.put("*" + ea.getSubject() + "·" + ea.getPredicate()
 						+ "·" + ea.getObject(), justifications);
 			}
 			
 			for (String name : qualities.keySet()) {
 				if (qualities_lacking.contains(name) == false) {
 					filter_input.clear();
 					filter_input.add(ea);
 					if (qualities.get(name).filter(filter_input).isEmpty() == false) {
 						qualities_lacking.add(name);
 					}
 				}
 			}
 			
 			
 		}
 
 
 
 		StringBuffer tips = new StringBuffer();
 
 		for (String key : ordered.keySet()) {
 			tips.append("\n\n");
 			tips.append(key);
 			tips.append(" may be justified by further asserting that:");
 			tips.append(ordered.get(key));
 		}
 
 		tips.append("\n\nThere are missing pieces of information regarding:");
 
 		for (String name : qualities_lacking)
 			tips.append("\n     " + name);
 
 		return tips.toString();
 	}
 
 }
