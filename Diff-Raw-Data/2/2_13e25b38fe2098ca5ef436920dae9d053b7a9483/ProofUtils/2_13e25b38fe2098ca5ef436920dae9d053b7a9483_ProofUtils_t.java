 // =================================================================
 // Copyright (C) 2009-2010 DFKI GmbH Talking Robots
 // Miroslav Janicek (miroslav.janicek@dfki.de)
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public License
 // as published by the Free Software Foundation; either version 2.1 of
 // the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 // 02111-1307, USA.
 // =================================================================
 
 package de.dfki.lt.tr.infer.weigabd;
 
 import de.dfki.lt.tr.infer.weigabd.slice.Atom;
 import de.dfki.lt.tr.infer.weigabd.slice.AssertedQuery;
 import de.dfki.lt.tr.infer.weigabd.slice.AssumedQuery;
 import de.dfki.lt.tr.infer.weigabd.slice.FunctionTerm;
 import de.dfki.lt.tr.infer.weigabd.slice.MarkedQuery;
 import de.dfki.lt.tr.infer.weigabd.slice.ModalisedAtom;
 import de.dfki.lt.tr.infer.weigabd.slice.Modality;
 import de.dfki.lt.tr.infer.weigabd.slice.NullAssumabilityFunction;
 import de.dfki.lt.tr.infer.weigabd.slice.Term;
 import de.dfki.lt.tr.infer.weigabd.slice.UnsolvedQuery;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Utilities for manipulating proofs.
  *
  * @author Miroslav Janicek
  */
 public abstract class ProofUtils {
 
 	/**
 	 * Given a sequence of queries, return all queries marked as asserted.
 	 * 
 	 * @param qs the sequence of queries (proof)
 	 * @return all elements of qs that are of type AssertedQuery
 	 */
 	public static List<AssertedQuery> filterAsserted(List<MarkedQuery> qs) {
 		ArrayList<AssertedQuery> result = new ArrayList<AssertedQuery>();
 		for (MarkedQuery q : qs) {
 			if (q instanceof AssertedQuery) {
 				result.add((AssertedQuery) q);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Given a sequence of queries, return all queries that are marked as assumed.
 	 * 
 	 * @param qs the sequence of queries (proof)
 	 * @return all elements of qs that are of type AssumedQuery
 	 */
 	public static List<AssumedQuery> filterAssumed(List<MarkedQuery> qs) {
 		ArrayList<AssumedQuery> result = new ArrayList<AssumedQuery>();
 		for (MarkedQuery q : qs) {
 			if (q instanceof AssumedQuery) {
 				result.add((AssumedQuery) q);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Convert a proof to a sequence of modalised atoms, thereby stripping
 	 * the proof of the markings.
 	 *
 	 * @param qs the sequence of queries (proof)
 	 * @return sequence of the corresponding modalised atoms
 	 */
 	public static List<ModalisedAtom> stripMarking(List<MarkedQuery> qs) {
 		ArrayList<ModalisedAtom> result = new ArrayList<ModalisedAtom>();
 		for (MarkedQuery q : qs) {
 			result.add(q.atom);
 		}
 		return result;
 	}
 
 	/**
 	 * Filter a sequence of modalised formulas by a modality prefix.
 	 * Formulas thus prefixed will be included in the output with the
 	 * prefix removed.
 	 *
 	 * TODO: evaluated on class equality rather than class *content* equality
 	 *
 	 * @param mas sequence of modalised atoms
 	 * @param m modality prefix
 	 * @return sequence of modalised formulas from mas with m removed
 	 */
 	public static List<ModalisedAtom> filterStripByModalityPrefix(List<ModalisedAtom> mas, List<Modality> m) {
 		ArrayList<ModalisedAtom> result = new ArrayList<ModalisedAtom>();
 		for (ModalisedAtom ma : mas) {
 			if (ma.m.size() >= m.size()) {
 				boolean good = true;
 				for (int j = 0; j < m.size(); j++) {
 					good = good && ma.m.get(j) == m.get(j);
 				}
 				if (good) {
 					ArrayList<Modality> ms = new ArrayList<Modality>();
 					for (int k = m.size(); k < ma.m.size(); k++) {
 						ms.add(ma.m.get(k));
 					}
 					result.add(new ModalisedAtom(ms, (Atom) ma.a.clone()));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Create a new unsolved proof.
 	 *
 	 * @param goal the goal formula
 	 * @return the proof
 	 */
 	public static ArrayList<UnsolvedQuery> newUnsolvedProof(ModalisedAtom goal) {
 		ArrayList<UnsolvedQuery> result = new ArrayList<UnsolvedQuery>();
		result.add(new UnsolvedQuery(goal, new NullAssumabilityFunction()));
 		return result;
 	}
 }
