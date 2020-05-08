 package edu.allatom;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.HashMap;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import edu.allatom.Protein.RotationType;
 import edu.math.Line;
 import edu.math.Matrix;
 import edu.math.TransformationMatrix3D;
 import edu.math.Vector;
 
 public class Bender {
 
 	public static int WINDOW_SIZE = 7;
 	private static float DAMPING_FACTOR = 1f;
 	public static int WINDOW_REPETITIONS = 15;
 	private static int REPETITIONS = 10;
 	public static int ROTAMER_SEARCH_DEPTH = 3;
 
 	/**
 	 * Bend a protein to try to match a given c_alpha trace.
 	 * 
 	 * TODO Remove renderer from argument list
 	 * 
 	 * @param p
 	 *            Protein to bend
 	 * @param trace
 	 *            Trace to try to match
 	 * @param renderer
 	 *            Debug renderer
 	 */
 	public static void bendProteinBackbone(Protein p, LinkedList<Atom> trace,
 			Renderer renderer) {
 
 		// create reverse trace and backbone
 		List<AminoAcid> aaSeq = p.aaSeq;
 		LinkedList<AminoAcid> aaSeqReverse = new LinkedList<AminoAcid>();
 		LinkedList<Atom> traceReverse = new LinkedList<Atom>();
 		for (int i = 0; i < aaSeq.size(); i++) {
 			aaSeqReverse.add(aaSeq.get(aaSeq.size() - i - 1));
 			traceReverse.add(trace.get(aaSeq.size() - i - 1));
 		}
 
 		// align the first 2 CA's before starting iterative bending.
 		// extract first amino acids
 		ListIterator<AminoAcid> aaIterator = aaSeq.listIterator();
 		ListIterator<Atom> traceIterator = trace.listIterator();
 
 		AminoAcid aa = aaIterator.next();
 		AminoAcid aaNext = Util.iteratorPeek(aaIterator);
 		Atom ca = aa.getAtom("CA");
 		Atom caNext = aaNext.getAtom("CA");
 		Atom tCA = traceIterator.next();
 		Atom traceCANext = Util.iteratorPeek(traceIterator);
 		// translate so starting CA coincides with the first CA of the trace
 		Vector t = ca.vectorTo(tCA);// atomDistance(ca, traceCA);
 		Matrix m = TransformationMatrix3D.createTranslation(t);
 		p.transformProtein(m);
 		// rotate so the second CA is as close as possible to the second CA of
 		// the trace
 		Vector normal = tCA.vectorTo(traceCANext).cross(tCA.vectorTo(caNext))
 				.normIn();
 		float angle = -tCA.vectorTo(traceCANext).angle(tCA.vectorTo(caNext));
 
 		m = TransformationMatrix3D.createRotation(new Vector(tCA.position),
 				normal, angle);
 		p.transformProtein(m);
 
 		// start bending
 		int actualCollisionsAfterElimination = 1;
 		int repetition = 0;
 		double rmsd = 10;
 		while (repetition < REPETITIONS ) {
 //				&& (actualCollisionsAfterElimination > 0)) {//
 //						|| rmsd > .5)) {
 
 			boolean reverse = repetition % 2 == 1;
 			repetition++;
 			if (reverse) {
 				aaIterator = aaSeqReverse.listIterator();
 				traceIterator = traceReverse.listIterator();
 			} else {
 				aaIterator = aaSeq.listIterator();
 				traceIterator = trace.listIterator();
 			}
 
 			// iterate over the residues and bend each residue
 			while (aaIterator.nextIndex() < p.aaSeq.size()
 					&& traceIterator.nextIndex() < trace.size()) {
 				List<AminoAcid> backboneAA = new ArrayList<AminoAcid>();
 				List<Atom> traceCA = new ArrayList<Atom>();
 				int lookaheadWindow = 0;
 				while (lookaheadWindow < WINDOW_SIZE && aaIterator.hasNext()
 						&& traceIterator.hasNext()) {
 					backboneAA.add(aaIterator.next());
 					traceCA.add(traceIterator.next());
 					lookaheadWindow++;
 				}
 				// hurrah for Java-iteratorer
 				while (lookaheadWindow > 1) {
 					aaIterator.previous();
 					traceIterator.previous();
 					lookaheadWindow--;
 				}
 
 				// index of current residue offset
 				int aaIndex = aaIterator.nextIndex() - 1;
 
 				// adjust phi and psi of residues in window
 				// TODO use a distance threshold instead of fixed loop limit?
 				for (int step = 0; step < WINDOW_REPETITIONS; step++) {
 					for (int windowOffset = 0; windowOffset < Math.min(
 							WINDOW_SIZE - 1, p.aaSeq.size() - aaIndex - 1); windowOffset++) {
 						aa = backboneAA.get(windowOffset);
 						ca = aa.getAtom("CA");
 						Atom c = aa.getAtom("C");
 						Atom n = aa.getAtom("N");
 						List<AminoAcid> backboneWindow = backboneAA.subList(
 								windowOffset + 1, backboneAA.size());
 						List<Atom> traceWindow = traceCA.subList(
 								windowOffset + 1, traceCA.size());
 						if (reverse) {
 							// find optimal psi rotation angle
 							Line psiRotationAxis = new Line(new Vector(
 									ca.position), c.vectorTo(ca));
 							float psiAngleDiff = angleFromCCD(psiRotationAxis,
 									backboneWindow, traceWindow, renderer,
 									false);
 							rotate(psiAngleDiff, aaIndex + windowOffset,
 									RotationType.PSI, aaSeqReverse, reverse);
 							// find optimal phi rotation angle
 							Line phiRotationAxis = new Line(new Vector(
 									n.position), ca.vectorTo(n));
 							float phiAngleDiff = angleFromCCD(phiRotationAxis,
 									backboneWindow, traceWindow, renderer,
 									false);
 							rotate(phiAngleDiff, aaIndex + windowOffset,
 									RotationType.PHI, aaSeqReverse, reverse);
 
 						} else {
 							// find optimal phi rotation angle
 							Line phiRotationAxis = new Line(new Vector(
 									ca.position), n.vectorTo(ca));
 							float phiAngleDiff = angleFromCCD(phiRotationAxis,
 									backboneWindow, traceWindow, renderer,
 									false);
 							rotate(phiAngleDiff, aaIndex + windowOffset,
 									RotationType.PHI, aaSeq, reverse);
 
 							// find optimal psi rotation angle
 							Line psiRotationAxis = new Line(new Vector(
 									c.position), ca.vectorTo(c));
 							float psiAngleDiff = angleFromCCD(psiRotationAxis,
 									backboneWindow, traceWindow, renderer,
 									false);
 							rotate(psiAngleDiff, aaIndex + windowOffset,
 									RotationType.PSI, aaSeq, reverse);
 						}
 					}
 				}
 			}
 			int collisionsBeforeElimination = countCollisions(p);
 
 			searchForRotamers(p);
 			actualCollisionsAfterElimination = countCollisions(p);
 
 			rmsd = p.cATraceRMSD(trace);
			//BendingStats.print("      ["+rmsd
			System.out.print("  [" + rmsd 
 					+ ", [" + collisionsBeforeElimination
 					+ "," + actualCollisionsAfterElimination + "]],\n");
 		}
 	}
 
 	public static int countCollisions(Protein p) {
 		int count = 0;
 		HashMap<AminoAcid, AminoAcid> foundCollisions = new HashMap<AminoAcid, AminoAcid>();
 		for (AminoAcid aaa : p.aaSeq) {
 			AminoAcid before = foundCollisions.get(aaa);
 			Collection<AminoAcid> collidees = aaa.collides(p);
 			for(AminoAcid collidee : collidees) {
 				if (before == collidee)
 					continue;
 				
 				if (collidee != null) {
 					foundCollisions.put(collidee, aaa);
 					count++;
 				}
 			}
 		}
 		return count;
 	}
 
 	public static void rotate(float angle, int aaIndex,
 			RotationType rotationType, List<AminoAcid> aaSeq, boolean reverse) {
 		AminoAcid aa = aaSeq.get(aaIndex);
 		Atom ca = aa.getAtom("CA");
 
 		if (reverse) {
 			angle = -angle;
 		}
 		switch (rotationType) {
 		case PHI: {
 			Atom n = aa.getAtom("N");
 			Line rotationAxis = new Line(new Vector(ca.position), n
 					.vectorTo(ca));
 			Matrix rotation = TransformationMatrix3D.createRotation(
 					rotationAxis, angle);
 			transformProtein(rotation, aaIndex, RotationType.PHI, aaSeq,
 					reverse);
 			break;
 		}
 		case PSI: {
 			Atom c = aa.getAtom("C");
 			Line rotationAxis = new Line(new Vector(c.position), ca.vectorTo(c));
 			Matrix rotation = TransformationMatrix3D.createRotation(
 					rotationAxis, angle);
 			transformProtein(rotation, aaIndex, RotationType.PSI, aaSeq,
 					reverse);
 			break;
 		}
 		case OMEGA: {
 			throw new NotImplementedException();
 		}
 		}
 	}
 
 	private static void transformProtein(Matrix m, int aaIdx,
 			RotationType type, List<AminoAcid> aaSeq, boolean reverse) {
 		AminoAcid aa = aaSeq.get(aaIdx);
 		if (reverse) {
 			switch (type) {
 			case PHI:
 				Atom a = aa.getAtom("H");
 				if (a != null) { // prolin doesn't have H
 					Vector v = new Vector(a.position);
 					a.position = m.applyToIn(v);
 				}
 				break;
 			case PSI:
 				for (Atom at : aa.getAtoms()) {
 					if (at.label.equals("O")) {
 						continue;
 					}
 					Vector v = new Vector(at.position);
 					at.position = m.applyToIn(v);
 				}
 				break;
 			case OMEGA:
 				// TODO
 				break;
 			}
 		} else {
 			switch (type) {
 			case PHI:
 				for (Atom a : aa.getAtoms()) {
 					if (a.label.equals("H")) {
 						continue;
 					}
 					Vector v = new Vector(a.position);
 					a.position = m.applyToIn(v);
 				}
 				break;
 			case PSI:
 				Atom a = aa.getAtom("O");
 				Vector v = new Vector(a.position);
 				a.position = m.applyToIn(v);
 				break;
 			case OMEGA:
 				// TODO
 				break;
 			}
 		}
 		for (int i = aaIdx + 1; i < aaSeq.size(); i++) {
 			aa = aaSeq.get(i);
 			for (Atom a : aa.getAtoms()) {
 				Vector v = new Vector(a.position);
 				a.position = m.applyToIn(v);
 			}
 		}
 	}
 
 	private static float angleFromCCD(Line rotationAxis,
 			List<AminoAcid> backboneAA, List<Atom> traceCA, Renderer renderer,
 			boolean debug) {
 		int lookahead = backboneAA.size();
 
 		float[] r_mag = new float[lookahead];
 		Vector[] r_unit = new Vector[lookahead];
 		Vector[] t = new Vector[lookahead];
 		Vector[] n_unit = new Vector[lookahead];
 
 		// collect vectors
 		for (int l = 0; l < lookahead; l++) {
 			Vector Ca = new Vector(backboneAA.get(l).getAtom("CA").position);
 			Vector T = new Vector(traceCA.get(l).position);
 			Vector O = rotationAxis.projection(Ca);
 			Vector r = O.vectorTo(Ca);
 			r_mag[l] = r.length();
 			r_unit[l] = r.norm();
 			t[l] = O.vectorTo(T);
 			n_unit[l] = rotationAxis.getDirection().norm().cross(r_unit[l]);
 		}
 
 		// calculate angle
 		float tan_alpha_denom = 0;
 		float tan_alpha_num = 0;
 		for (int i = 0; i < lookahead; i++) {
 			tan_alpha_num += t[i].dot(n_unit[i]) * r_mag[i];
 			tan_alpha_denom += t[i].dot(r_unit[i]) * r_mag[i];
 		}
 		float tan_alpha = tan_alpha_num / tan_alpha_denom;
 		float alpha = (float) Math.atan(tan_alpha);
 
 		// determine 2. derivative
 		float second_derivative = 0;
 		for (int i = 0; i < lookahead; i++) {
 			second_derivative += Math.cos(alpha) * t[i].dot(r_unit[i]) * 2
 					* r_mag[i] + Math.sin(alpha) * t[i].dot(n_unit[i]) * 2
 					* r_mag[i];
 		}
 
 		// adjust angle according to 2. derivative
 		if (second_derivative < 0) {
 			if (alpha > 0) {
 				alpha -= Math.PI;
 			} else {
 				alpha += Math.PI;
 			}
 		}
 		return alpha * DAMPING_FACTOR;
 	}
 
 	public static void searchForRotamers(Protein p) {
 		for (AminoAcid aaa : p.aaSeq) {
 			if (!aaa.collides(p).isEmpty()) {
 				checkCollision(p, aaa, ROTAMER_SEARCH_DEPTH,
 						new LinkedList<AminoAcid>());
 			}
 		}
 	}
 
 	// Returns true if the collision is solved
 	private static boolean checkCollision(Protein p, AminoAcid aa, int depth,
 			Collection<AminoAcid> visitedTrace) {
 		// prøv alle rotamerer
 		List<Rotamer> rotamers = RotamerLibrary.lookupRotamers(aa.type);
		List<Tuple<Rotamer, Collection<AminoAcid>>> collidees = new ArrayList<Tuple<Rotamer, Collection<AminoAcid>>>();
 		for (Rotamer r : rotamers) {
 			aa.applyRotamer(r);
 			Collection<AminoAcid> collisions = aa.collides(p);
 			if (collisions.isEmpty()) {
 				return true;
 			}
			
			collidees.add(new Tuple(r,collisions));
 		}
 		depth--;
 		if (depth == 0) {
 			return false;
 		}
 		// hvis der stadig er kollisioner spørger vi naboerne
		for(Tuple<Rotamer, Collection<AminoAcid>> t : collidees) {
			Rotamer r = t.a;
 			aa.applyRotamer(r);
 			visitedTrace.add(aa);
			for(AminoAcid collidee : t.b) {
				if (visitedTrace.contains(collidee))
					continue;
				if (checkCollision(p, collidee, depth, visitedTrace)) {
					return true;
				}
 			}
 		}
 		return false;
 	}
 }
