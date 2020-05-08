 /**
  * EquivalentAssertions.java, (c) 2012, Immanuel Albrecht; Dresden University of
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
 
 package de.tu_dresden.psy.inference;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import de.tu_dresden.psy.inference.compiler.StringIds;
 import de.tu_dresden.psy.inference.forms.AnnotableDisjunctiveNormalForm;
 import de.tu_dresden.psy.inference.forms.Annotated;
 
 /**
  * implements an AssertionInterface that will take care of differently derived
  * but otherwise equivalent assertions
  * 
  * @author albrecht
  * 
  */
 
 public class EquivalentAssertions implements AssertionInterface {
 
 	private Object subject, predicate, object;
 
 	private int hashSubject, hashPredicate, hashObject;
 
 	private Set<AssertionInterface> assertions;
 
 	private Set<ArrayList<Integer>> sampleSolutions;
 	private Integer assertion_id;
 
 	private boolean old;
 
 	private AnnotableDisjunctiveNormalForm<EquivalentAssertions> directAncestors;
 	private AnnotableDisjunctiveNormalForm<EquivalentAssertions> ruleAncestors;
 	private AnnotableDisjunctiveNormalForm<EquivalentAssertions> allAncestors;
 
 	public static int notJustified = -1;
 
 	/**
 	 * justification depth, where 0 means that the assertion is justified
 	 * itself, and if the assertion can be inferred from assertions with
 	 * justification depth k or less, the justification depth of the assertion
 	 * is (k+1)
 	 * 
 	 * where -1 means not justified at all
 	 */
 
 	private int justificationDepth;
 
 	private Map<ArrayList<Integer>, Integer> modifiedJustificationDepth;
 
 	@Override
 	public boolean isOld() {
 		return this.old;
 	}
 
 	@Override
 	public void markAsOld() {
 		this.old = true;
 	}
 
 	/**
 	 * create a new equivalence class from a representing assertion only
 	 * containing this single assertion
 	 * 
 	 * @param representant
 	 */
 	public EquivalentAssertions(AssertionInterface representant,
 			Set<ArrayList<Integer>> sampleSolutions, StringIds assertionDomain) {
 		this.assertion_id = assertionDomain.fromAssertion(representant);
 		this.sampleSolutions = sampleSolutions;
 		this.assertions = new HashSet<AssertionInterface>();
 		this.assertions.add(representant);
 		this.subject = representant.getSubject();
 		this.object = representant.getObject();
 		this.predicate = representant.getPredicate();
 		this.old = false;
 		this.hashSubject = this.subject.hashCode();
 		this.hashPredicate = this.predicate.hashCode();
 		this.hashObject = this.object.hashCode();
 		this.justificationDepth = notJustified;
 		this.directAncestors = new AnnotableDisjunctiveNormalForm<EquivalentAssertions>();
 		this.ruleAncestors = new AnnotableDisjunctiveNormalForm<EquivalentAssertions>();
 		this.allAncestors = new AnnotableDisjunctiveNormalForm<EquivalentAssertions>();
 
 		this.modifiedJustificationDepth = new HashMap<ArrayList<Integer>, Integer>();
 		for (ArrayList<Integer> m : this.sampleSolutions) {
 			this.modifiedJustificationDepth.put(m, notJustified);
 		}
 	}
 
 	/**
 	 * add new conjunctions to directAncestors & allAncestors
 	 * 
 	 * @param excessLimit
 	 */
 
 	public void updateDirectAncestors(ExcessLimit excessLimit) {
 
 		for (AssertionInterface assertion : this.assertions) {
 
 			if (excessLimit.continueTask() == false) {
 				break;
 			}
 
 			if (assertion instanceof InferredAssertion) {
 
 				InferredAssertion inferred = (InferredAssertion) assertion;
 
 				boolean all_equivalence_classes_and_precursors = true;
 				Set<EquivalentAssertions> ancestors = new HashSet<EquivalentAssertions>();
 
 				for (AssertionInterface premise : inferred.getPremises()) {
 					if (premise instanceof EquivalentAssertions) {
 						EquivalentAssertions ea = (EquivalentAssertions) premise;
 
 						if ((ea.getJustificationDepth() == notJustified)
 								|| (ea.getJustificationDepth() >= this.justificationDepth)) {
 							all_equivalence_classes_and_precursors = false;
 							break;
 						}
 
 						ancestors.add(ea);
 
 					} else {
 						all_equivalence_classes_and_precursors = false;
 						break;
 					}
 				}
 
 				if (all_equivalence_classes_and_precursors == true) {
 					this.directAncestors
 					.join(new AnnotableDisjunctiveNormalForm<EquivalentAssertions>(
 							ancestors, inferred
 							.isInferredByTrivialRule()));
 				}
 			}
 		}
 
 		this.allAncestors.join(this.directAncestors);
 	}
 
 	/**
 	 * add new conjunctions to ruleAncestors
 	 * 
 	 * @param excessLimit
 	 */
 
 	public void updateRuleAncestors(ExcessLimit excessLimit) {
 
 		for (AssertionInterface assertion : this.assertions) {
 
 			if (excessLimit.continueTask() == false) {
 				break;
 			}
 
 			if (assertion instanceof InferredAssertion) {
 
 				InferredAssertion inferred = (InferredAssertion) assertion;
 
 				boolean all_equivalence_classes_and_precursors = true;
 				Set<EquivalentAssertions> ancestors = new HashSet<EquivalentAssertions>();
 
 				for (AssertionInterface premise : inferred.getPremises()) {
 					if (premise instanceof EquivalentAssertions) {
 						EquivalentAssertions ea = (EquivalentAssertions) premise;
 
 						if ((ea.getJustificationDepth() == notJustified)) {
 							all_equivalence_classes_and_precursors = false;
 							break;
 						}
 
 						ancestors.add(ea);
 
 					} else {
 						all_equivalence_classes_and_precursors = false;
 						break;
 					}
 				}
 
 				if (all_equivalence_classes_and_precursors == true) {
 					this.ruleAncestors
 					.join(new AnnotableDisjunctiveNormalForm<EquivalentAssertions>(
 							ancestors, inferred
 							.isInferredByTrivialRule()));
 				}
 
 				/**
 				 * 
 				 * DEBUG CODE
 				 */
 				// if (inferred.isInferredByTrivialRule()) {
 				// System.err.println("Trivial Rule:" + inferred);
 				// System.err
 				// .println(new
 				// AnnotableDisjunctiveNormalForm<EquivalentAssertions>(
 				// ancestors, inferred
 				// .isInferredByTrivialRule()));
 				// System.err.println(" *** ");
 				// System.err.println(this.ruleAncestors);
 				// System.err.println(" *** TRIVIAL PART *** ");
 				// System.err.println(this.ruleAncestors.getTrivialPart());
 				// }
 				/**
 				 * /DEBUGCODE
 				 */
 			}
 		}
 
 	}
 
 	/**
 	 * add new conjunctions to allAncestors
 	 * 
 	 * MIGHT NEED SOME FIX for triviality annotations in annotated disjunctive
 	 * normal forms, currently, we just ignore triviality annotated atoms and do
 	 * not replace them
 	 * 
 	 * @param excessLimit
 	 */
 
 	public boolean updateAllAncestors(ExcessLimit excessLimit) {
 		int initial_size = this.allAncestors.getTerm().size();
 
 		Set<EquivalentAssertions> current_ancestors = new HashSet<EquivalentAssertions>();
 
 		for (Set<EquivalentAssertions> c : this.allAncestors.getTerm()) {
 			current_ancestors.addAll(c);
 		}
 
 		for (EquivalentAssertions a : current_ancestors) {
 
 			if (excessLimit.continueTask() == false) {
 				break;
 			}
 
 			/**
 			 * Here, we simply ignore ancestors that have been marked trivial;
 			 * well well...
 			 * 
 			 * needs fix?? since we don't use this structure anymore, I guess
 			 * not...
 			 */
 
 			this.allAncestors.replaceJoin(
 					new Annotated<EquivalentAssertions>(a), a.allAncestors);
 		}
 
 		return this.allAncestors.getTerm().size() > initial_size;
 	}
 
 	/**
 	 * 
 	 * @return the justification level of the assertion, or
 	 *         EquivalentAssertions.notJustified
 	 */
 
 	public int getJustificationDepth() {
 		return this.justificationDepth;
 	}
 
 	/** update the modified justification depth structure used for output */
 
 	public void putModifiedJustificationDepths(
 			Map<ArrayList<Integer>, Map<Integer, Integer>> modifiedDepths) {
 		for (ArrayList<Integer> m : this.modifiedJustificationDepth.keySet()) {
 			modifiedDepths.get(m).put(this.assertion_id,
 					this.modifiedJustificationDepth.get(m));
 		}
 	}
 
 	/**
 	 * makes this assertion class to be considered justified
 	 */
 
 	public void considerJustified() {
 		this.justificationDepth = 0;
 		for (ArrayList<Integer> m : this.modifiedJustificationDepth.keySet()) {
 			if (m.contains(this.assertion_id)) {
 				this.modifiedJustificationDepth.put(m, 0);
 			} else {
 				/**
 				 * at this point, we penalize points that are not part of the
 				 * sample solution in a way that the complete sample solution is
 				 * always preferred over non-sample solution points.
 				 */
 				this.modifiedJustificationDepth.put(m, m.size() + 1);
 			}
 
 		}
 	}
 
 	/**
 	 * checks whether the justification depth can be reduced due to inference
 	 * 
 	 * @return true, if justification depth has improved
 	 */
 
 	public boolean updateJustificationDepth() {
 		boolean any_better = this.updateJustificationDepthOld();
 
 		for (ArrayList<Integer> m : this.modifiedJustificationDepth.keySet())
 		{
 			any_better = any_better || this.updateJustificationDepthSolution(m);
 		}
 
 		return any_better;
 	}
 
 	private boolean updateJustificationDepthSolution(ArrayList<Integer> m) {
 
 		if ((this.modifiedJustificationDepth.get(m) >= 0)
 				&& (this.modifiedJustificationDepth.get(m) <= 1)) {
 			return false;
 		}
 
 		boolean better = false;
 
		int add_one = 2;
 
 		if (m.contains(this.assertion_id)) {
 			add_one = 1;
 		}
 
 		for (AssertionInterface assertion : this.assertions) {
 			if (assertion instanceof InferredAssertion) {
 
 				InferredAssertion inferred = (InferredAssertion) assertion;
 
 				boolean all_justified = true;
				int sum_level = 0;
 
 				for (AssertionInterface premise : inferred.getPremises()) {
 					if (premise instanceof EquivalentAssertions) {
 						EquivalentAssertions ea = (EquivalentAssertions) premise;
 
 						if (ea.modifiedJustificationDepth.get(m) == notJustified) {
 							all_justified = false;
 							break;
 						}
 
						sum_level += ea.modifiedJustificationDepth.get(m);


 					} else {
 						all_justified = false;
 						break;
 					}
 				}
 
 				if (all_justified == true) {
					if (((sum_level + add_one) < this.modifiedJustificationDepth
 							.get(m))
 							|| (this.modifiedJustificationDepth.get(m) == notJustified)) {
 
						this.modifiedJustificationDepth.put(m, sum_level
 								+ add_one);
 						better = true;
 					}
 				}
 			}
 		}
 
 		return better;
 	}
 
 	private boolean updateJustificationDepthOld() {
 
 		if ((this.justificationDepth >= 0) && (this.justificationDepth <= 1)) {
 			return false;
 		}
 
 		boolean better = false;
 
 		for (AssertionInterface assertion : this.assertions) {
 			if (assertion instanceof InferredAssertion) {
 
 				InferredAssertion inferred = (InferredAssertion) assertion;
 
 				boolean all_justified = true;
 				int max_level = 0;
 
 				for (AssertionInterface premise : inferred.getPremises()) {
 					if (premise instanceof EquivalentAssertions) {
 						EquivalentAssertions ea = (EquivalentAssertions) premise;
 
 						if (ea.justificationDepth == notJustified) {
 							all_justified = false;
 							break;
 						}
 
 						if (max_level < ea.justificationDepth) {
 							max_level = ea.justificationDepth;
 						}
 					} else {
 						all_justified = false;
 						break;
 					}
 				}
 
 				if (all_justified == true) {
 					if (((max_level + 1) < this.justificationDepth)
 							|| (this.justificationDepth == notJustified)) {
 
 						this.justificationDepth = max_level + 1;
 						better = true;
 					}
 				}
 			}
 		}
 
 		return better;
 	}
 
 	/**
 	 * copy constructor
 	 * 
 	 * @param copyContents
 	 */
 
 	public EquivalentAssertions(EquivalentAssertions copyContents) {
 		this.assertions = new HashSet<AssertionInterface>(
 				copyContents.assertions.size());
 		this.assertions.addAll(copyContents.assertions);
 		this.subject = copyContents.subject;
 		this.object = copyContents.object;
 		this.predicate = copyContents.predicate;
 		this.old = false;
 		this.hashSubject = copyContents.hashSubject;
 		this.hashPredicate = copyContents.hashPredicate;
 		this.hashObject = copyContents.hashObject;
 		this.justificationDepth = copyContents.justificationDepth;
 		this.directAncestors = new AnnotableDisjunctiveNormalForm<EquivalentAssertions>();
 		this.allAncestors = new AnnotableDisjunctiveNormalForm<EquivalentAssertions>();
 		this.ruleAncestors = new AnnotableDisjunctiveNormalForm<EquivalentAssertions>();
 
 		this.directAncestors.join(copyContents.directAncestors);
 		this.allAncestors.join(copyContents.allAncestors);
 		this.ruleAncestors.join(copyContents.ruleAncestors);
 
 		this.assertion_id = copyContents.assertion_id;
 		this.modifiedJustificationDepth = new HashMap<ArrayList<Integer>, Integer>();
 		this.modifiedJustificationDepth
 		.putAll(copyContents.modifiedJustificationDepth);
 	}
 
 	/**
 	 * add another assertion to the equivalence class if possible
 	 * 
 	 * @param a
 	 */
 	public void add(AssertionInterface a) {
 		if (this.isEqualTo(a) != true) {
 			return;
 		}
 		if (a == this) {
 			return;
 		}
 
 		if (a instanceof EquivalentAssertions) {
 			EquivalentAssertions ea = (EquivalentAssertions) a;
 			this.assertions.addAll(ea.assertions);
 		} else {
 			this.assertions.add(a);
 		}
 	}
 
 	@Override
 	public Object getSubject() {
 		return this.subject;
 	}
 
 	@Override
 	public Object getObject() {
 		return this.object;
 	}
 
 	@Override
 	public Object getPredicate() {
 		return this.predicate;
 	}
 
 	@Override
 	public boolean isEqualTo(AssertionInterface assertion) {
 		if (this.hashPredicate != assertion.getPredicate().hashCode()) {
 			return false;
 		}
 		if (this.hashSubject != assertion.getSubject().hashCode()) {
 			return false;
 		}
 		if (this.hashObject != assertion.getObject().hashCode()) {
 			return false;
 		}
 
 		if (assertion.getPredicate().equals(this.predicate) == false) {
 			return false;
 		}
 
 		if (assertion.getSubject().equals(this.subject) == false) {
 			return false;
 		}
 
 		return assertion.getObject().equals(this.object);
 	}
 
 	@Override
 	public boolean isPremise(AssertionInterface assertion) {
 		for (AssertionInterface a : this.assertions) {
 			if (a.isPremise(assertion)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = (prime * result) + this.hashObject;
 		result = (prime * result) + this.hashPredicate;
 		result = (prime * result) + this.hashSubject;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 
 		return this.isEqualTo((EquivalentAssertions) obj);
 	}
 
 	@Override
 	public String toString() {
 
 		String p = this.subject.toString() + "·" + this.predicate.toString() + "·"
 				+ this.object.toString() + " [" + this.assertions.size() + "]";
 
 		return p;
 
 		// TreeSet<String> ordered = new TreeSet<String>();
 		//
 		// for (AssertionInterface assertion : assertions) {
 		// ordered.add(assertion.toString());
 		// }
 		// for (Iterator<String> it = ordered.iterator(); it.hasNext();) {
 		// p += "\n" + it.next();
 		// }
 		//
 		// return p + "\n";
 	}
 
 	/**
 	 * 
 	 * @return allAncestors
 	 */
 
 	public AnnotableDisjunctiveNormalForm<EquivalentAssertions> ancestors() {
 		return this.allAncestors;
 	}
 
 	/**
 	 * 
 	 * @return directAncestors
 	 */
 
 	public AnnotableDisjunctiveNormalForm<EquivalentAssertions> precursors() {
 		return this.directAncestors;
 	}
 
 	/**
 	 * 
 	 * @return ruleAncestors
 	 */
 
 	public AnnotableDisjunctiveNormalForm<EquivalentAssertions> preimages() {
 		return this.ruleAncestors;
 	}
 }
