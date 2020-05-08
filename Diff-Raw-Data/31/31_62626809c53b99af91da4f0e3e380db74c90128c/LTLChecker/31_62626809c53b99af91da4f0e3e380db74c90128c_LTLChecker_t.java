 package org.net9.simplex.ppmc.checker;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.net9.simplex.ppmc.core.DTMC;
 import org.net9.simplex.ppmc.core.GeneralDTMC;
 import org.net9.simplex.ppmc.core.SimpleDTMC;
 import org.net9.simplex.ppmc.ltl.SimpleLTLConverter;
 import org.net9.simplex.ppmc.mat.MatrixIndex;
 import org.net9.simplex.ppmc.mat.SmartMatrix;
 import org.net9.simplex.ppmc.mat.SparseMatrix;
 import org.net9.simplex.ppmc.prop.PropLTL;
 import org.net9.simplex.ppmc.prop.StateProperty;
 import org.net9.simplex.ppmc.solver.Solver;
 import org.prismmodelchecker.jltl2ba.APElement;
 import org.prismmodelchecker.jltl2ba.APSet;
 import org.prismmodelchecker.jltl2ba.MyBitSet;
 import org.prismmodelchecker.jltl2ba.SimpleLTL;
 import org.prismmodelchecker.jltl2dstar.DA_State;
 import org.prismmodelchecker.jltl2dstar.DRA;
 import org.prismmodelchecker.jltl2dstar.LTL2Rabin;
 import org.prismmodelchecker.prism.PrismException;
 
 public class LTLChecker {
 	
 	DTMC model;
 	ModelChecker mc;
 	static final String ACCEPTING_LABEL = "accept";
 	
 	public LTLChecker (DTMC model, ModelChecker mc) {
 		this.model = model;
 		this.mc = mc;
 	}
 	
 	public Solver check(PropLTL prop) {
 		SimpleLTLConverter slc = new SimpleLTLConverter();
 		SimpleLTL ltl = slc.convert(prop);
 		APSet apSet = slc.getAPSet();
 		Map<String, BitSet> apMap = slc.getAPMap();
 		Map<String, StateProperty> subFormulaMap = slc.getSubFormula();
 		for (Entry<String, StateProperty> e:subFormulaMap.entrySet()) {
 			Solver s = (Solver) e.getValue().accept(this.mc);
 			if (!s.isConstant()) throw new UnsupportedOperationException();
 			apMap.put(e.getKey(), s.solveSet(null));
 		}
 		BitSet[] ap = new BitSet[apSet.size()];
 		for(int i=0;i<apSet.size();++i) {
 			ap[i]=apMap.get(apSet.getAP(i));
 		}
 		DRA dra;
 		try {
 			dra = LTL2Rabin.ltl2rabin(ltl, apSet);
			//dra.print(System.out);
 		} catch (PrismException e1) {
 			throw new IllegalArgumentException("Cannot convert LTL to DRA.");
 		}
 		SimpleDTMC product = constructProductMC(this.model, dra, ap) ;
 		SimplePCTLChecker checker = new SimplePCTLChecker(product);
 		return checker.solveEventually(product.getInitState(), product.getAP().get(ACCEPTING_LABEL));
 	}
 	
 	SimpleDTMC constructProductMC(DTMC dtmc, DRA dra, BitSet[] ap) {
 		SparseMatrix<Integer> statesMap = new SparseMatrix<Integer>();
 		ArrayList<MatrixIndex> states = new ArrayList<MatrixIndex>();
 		SparseMatrix<Double> trans = new SparseMatrix<Double>();
 		SparseMatrix<String> syms = new SparseMatrix<String>();
 		int size0 = dtmc.size();
 		SmartMatrix dtmcTrans = dtmc.getTrans();
 		MatrixIndex initial = new MatrixIndex(dtmc.getInitState(), dra.getStartState().getName());
 		statesMap.put(initial, 0);
 		states.add(initial);
 		APElement apElement = new APElement(ap.length);
 		for (int i=0;i<states.size(); ++i){
 			MatrixIndex state = states.get(i);
 			int s0=state.row, s1 = state.col;
 			HashMap<APElement, DA_State> edges = dra.get(s1).edges();
 			for (int t0=0;t0<size0;++t0) {
 				double prob = dtmcTrans.getNumericEntry(s0, t0);
 				if (prob==0) continue;
 				for (int j=0;j<ap.length;++j) {
 					apElement.set(j, ap[j].get(t0));
 				}
 				int t1 = edges.get(apElement).getName();
 				Integer target = statesMap.getN(t0, t1);
 				if (target == null) {
 					target = states.size();
 					MatrixIndex newState = new MatrixIndex(t0,t1);
 					statesMap.put(newState,target);
 					states.add(newState);
 				}
 				if (prob>0) {
 					trans.put(i, target, prob);
 				} else {
 					syms.put(i, target, dtmcTrans.getSymbolicEntry(s0, t0));
 				}
 			}
 		}
 		
 		double[][] dtrans = new double[states.size()][states.size()];
 		for (Entry<MatrixIndex, Double> e:trans.getMap().entrySet()){
 			dtrans[e.getKey().row][e.getKey().col] = e.getValue();
 		}
 		for (MatrixIndex e: syms.getMap().keySet()){
 			dtrans[e.row][e.col] = -1;
 		}
 		GeneralDTMC product = new GeneralDTMC(new SmartMatrix(dtrans, syms),
 				0, new HashMap<String, BitSet>());
 		int accSize = 0;
 		try {
 			if (!dra.acceptance().isCompact())
 				dra.acceptance().makeCompact();
 			 accSize = dra.acceptance().size();
 		} catch (PrismException e1) {
 			e1.printStackTrace();
 		}
 		BitSet accept = new BitSet(product.size());
 		int ergoricBase = product.absorbingTrans.size() - product.ergoric.length;
 		for (int i=0;i<product.ergoric.length; ++i) {
 			for (int j=0;j<accSize;++j) {
 				MyBitSet L = dra.acceptance().getAcceptance_L(j);	//L \caps inf \neq empty
 				MyBitSet U = dra.acceptance().getAcceptance_U(j);	//U \caps inf = empty
 				boolean satL = false;
 				boolean satU = true;
 				for (int k : product.ergoric[i].states) {
 					int state1 = states.get(k).col;
 					if (U.get(state1)) {
 						satU = false; 
 						break;
 					}
 					if (L.get(state1)) satL = true;
 				}
 				if (satL && satU) {
 					accept.set(ergoricBase + i);
 					break;
 				}
 			}
 		}
 		SimpleDTMC absorbing = product.absorbingTrans;
 		absorbing.getAP().put(ACCEPTING_LABEL, accept);
 		return absorbing;
 	}
 }
