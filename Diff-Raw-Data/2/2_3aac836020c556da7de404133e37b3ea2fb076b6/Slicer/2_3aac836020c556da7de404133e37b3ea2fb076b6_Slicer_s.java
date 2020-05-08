 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.analyses.slicer;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 
 import joeq.Class.jq_Class;
 import joeq.Class.jq_Reference;
 import joeq.Class.jq_Field;
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.Operand;
 import joeq.Compiler.Quad.Quad;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.QuadVisitor;
 import joeq.Compiler.Quad.Operand.ParamListOperand;
 import joeq.Compiler.Quad.Operand.RegisterOperand;
 import joeq.Compiler.Quad.Operator.ALoad;
 import joeq.Compiler.Quad.Operator.AStore;
 import joeq.Compiler.Quad.Operator.Getfield;
 import joeq.Compiler.Quad.Operator.Getstatic;
 import joeq.Compiler.Quad.Operator.Invoke;
 import joeq.Compiler.Quad.Operator.Move;
 import joeq.Compiler.Quad.Operator.New;
 import joeq.Compiler.Quad.Operator.Phi;
 import joeq.Compiler.Quad.Operator.NewArray;
 import joeq.Compiler.Quad.Operator.Putfield;
 import joeq.Compiler.Quad.Operator.Putstatic;
 import joeq.Compiler.Quad.Operator.Return;
 import joeq.Compiler.Quad.Operator.Unary;
 import joeq.Compiler.Quad.Operator.Binary;
 import joeq.Compiler.Quad.Operator.ALength;
 import joeq.Compiler.Quad.Operator.InstanceOf;
 import joeq.Compiler.Quad.Operator.IntIfCmp;
 import joeq.Compiler.Quad.Operator.LookupSwitch;
 import joeq.Compiler.Quad.Operator.TableSwitch;
 import joeq.Compiler.Quad.RegisterFactory.Register;
 
 import chord.util.CollectionUtils;
 import chord.util.FileUtils;
 import chord.project.Messages;
 import chord.program.Location;
 import chord.program.MethodSign;
 import chord.analyses.alias.CIObj;
 import chord.analyses.alias.CIAliasAnalysis;
 import chord.analyses.alias.ICICG;
 import chord.analyses.alias.ThrOblAbbrCICGAnalysis;
 import chord.util.tuple.object.Pair;
 import chord.util.tuple.integer.IntPair;
 import chord.program.Program;
 import chord.project.analyses.ProgramRel;
 import chord.project.analyses.rhs.BackwardRHSAnalysis;
 import chord.bddbddb.Rel.IntPairIterable;
 import chord.doms.DomB;
 import chord.project.Chord;
 import chord.project.Project;
 import chord.util.ArraySet;
 import chord.util.Timer;
 
 /**
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 @Chord(
 	    name = "slicer-java"
 	)
 public class Slicer extends BackwardRHSAnalysis<Edge, Edge> {
 	private final Set<Edge> emptyEdgeSet = Collections.emptySet();
 	private final Set<Expr> emptyExprSet = Collections.emptySet();
 	private final Set<Edge> tmpEdgeSet = new ArraySet<Edge>();
 	private final Set<Expr> tmpExprSet = new ArraySet<Expr>();
 	private final Pair<Expr, jq_Method> tmpPair = new Pair<Expr, jq_Method>(null, null);
 	private DomB domB;
     private ICICG cicg;
 	private CIAliasAnalysis cipa;
     private MyQuadVisitor qv = new MyQuadVisitor();
 	private Set<Quad> currSlice = new HashSet<Quad>();
 	private Pair<Location, Expr> currSeed;
     private Map<BasicBlock, Set<Quad>> fullCdepsMap =
 		new HashMap<BasicBlock, Set<Quad>>();
     private Map<Pair<Expr, jq_Method>, Set<Quad>> todoCdepsMap =
 		new HashMap<Pair<Expr, jq_Method>, Set<Quad>>();
 
 	public void run() {
 		domB = (DomB) Project.getTrgt("B");
 		Project.runTask(domB);
 		Project.runTask("P");
 
 		Project.runTask("cdep-dlog");
 		ProgramRel rel = (ProgramRel) Project.getTrgt("cdepBB");
         rel.load();
         IntPairIterable tuples = rel.getAry2IntTuples();
         for (IntPair tuple : tuples) {
             int b1Idx = tuple.idx0;
             int b2Idx = tuple.idx1;
             BasicBlock b1 = domB.get(b1Idx);
             BasicBlock b2 = domB.get(b2Idx);
 			Quad q = b2.getLastQuad();
             Set<Quad> qSet = fullCdepsMap.get(b1);
 			if (qSet == null) {
 				qSet = new ArraySet<Quad>(2);
 				fullCdepsMap.put(b1, qSet);
 			}
 			qSet.add(q);
         }
 
 		// first item in seeds.txt is a method name in format name:desc@type
 		// the following zero or more items are static field names in format
 		// name:desc@type
 		// slicing will be done on each of the static fields at the exit point
 		// of the denoted method
 		List<String> fStrList = FileUtils.readFileToList("seeds.txt");
 		int n = fStrList.size();
 		assert (n > 0);
 		Program program = Program.getProgram();
 		Location seedLoc;
 		{
 			String signStr = fStrList.get(0);
 			MethodSign sign = MethodSign.parse(signStr);
 			jq_Method method = program.getMethod(sign);
 			assert (method != null);
 			BasicBlock bb = method.getCFG().exit();
 			seedLoc = new Location(method, bb, -1, null);
 		}
 		Set<Pair<Location, Expr>> seeds = new HashSet<Pair<Location, Expr>>(n - 1);
 		for (int i = 1; i < n; i++) {
 			String fStr = fStrList.get(i);
 			MethodSign sign = MethodSign.parse(fStr);
 			jq_Reference r = program.getClass(sign.cName);
 			if (r == null) {
 				Messages.logAnon("WARN: Ignoring slicing on field %s: " +
 					" its declaring class was not found.", fStr);
 				continue;
 			}
 			assert (r instanceof jq_Class);
 			jq_Class c = (jq_Class) r;
 			jq_Field f = (jq_Field) c.getDeclaredMember(sign.mName, sign.mDesc);
 			if (f == null) {
 				Messages.fatal("ERROR: Cannot slice on field %s: " +
 					"it was not found in its declaring class.", fStr);
 			}
 			assert(f.isStatic());
 			Expr e = new StatField(f);
 			seeds.add(new Pair<Location, Expr>(seedLoc, e));
 		}
 
 		cipa = (CIAliasAnalysis) Project.getTrgt("cipa-java");
 		Project.runTask(cipa);
 
 		init();
 
         for (Pair<Location, Expr> seed : seeds) {
 			currSlice.clear();
 			currSeed = seed;
 			Timer timer = new Timer("slicer-timer");
 			timer.init();
 			System.out.println("***** SEED: " + seed);
 			runPass();
 			System.out.println("***** SLICE:");
 			for (Quad p : currSlice) {
 				System.out.println("\t" + program.toVerboseStr(p));
 			}
 			
 			timer.done();
 			System.out.println(timer.getInclusiveTimeStr());
 		}
 	}
 
 	@Override
 	public boolean doMerge() {
 		return false;
 	}
 
     @Override
     public ICICG getCallGraph() {
         if (cicg == null) {
             ThrOblAbbrCICGAnalysis cicgAnalysis =
                 (ThrOblAbbrCICGAnalysis) Project.getTrgt("throbl-abbr-cicg-java");
             Project.runTask(cicgAnalysis);
             cicg = cicgAnalysis.getCallGraph();
         }
         return cicg;
     }
 
     @Override
     public Set<Pair<Location, Edge>> getInitPathEdges() {
         Set<Pair<Location, Edge>> initPEs =
             new ArraySet<Pair<Location, Edge>>(1);
 		Location loc = currSeed.val0;
 		Expr e = currSeed.val1;
         Edge pe = new Edge(e, e, false);
         Pair<Location, Edge> pair = new Pair<Location, Edge>(loc, pe);
         initPEs.add(pair);
         return initPEs;
     }
 
 	@Override
 	public Set<Edge> getInitPathEdges(Quad q, jq_Method m2, Edge pe) {
 		// check if pe.dstExpr is return var
 		// if so then create init edge srcExpr==dstExpr==RetExpr.instance
 		// else if pe.dstExpr is non-local var then create init edge similarly
 		// else return emptyset
 		Expr dstExpr = pe.dstExpr;
 		Set<Edge> result;
 		if (dstExpr instanceof LocalVar) {
 			LocalVar e = (LocalVar) dstExpr;
             RegisterOperand vo = Invoke.getDest(q);
             if (vo != null && vo.getRegister() == e.v) {
 				tmpEdgeSet.clear();
				Edge pe2 = new Edge(RetExpr.instance, RetExpr.instance, false);
 				tmpEdgeSet.add(pe2);
 				result = tmpEdgeSet;
 			} else {
 				// ignore local vars other than return vars;
 				// they will be handled by getInvkPathEdges
 				result = emptyEdgeSet;
 			}
 		} else {
 			tmpEdgeSet.clear();
 			tmpEdgeSet.add(pe);
 			result = tmpEdgeSet;
 		}
 		return result;
 	}
 
 	@Override
 	public Set<Edge> getMiscPathEdges(Quad q, Edge pe) {
 		qv.iDstExpr = pe.dstExpr;
 		qv.iSrcExpr = pe.srcExpr;
 		qv.oDstExprSet = null;
 		q.accept(qv);
 		Set<Expr> oDstExprSet = qv.oDstExprSet;
 		tmpEdgeSet.clear();
 		if (oDstExprSet == null) {
 			tmpEdgeSet.add(pe);
 		} else {
 			if (DEBUG) System.out.println("Adding to slice: " + q);
 			currSlice.add(q);
 			Expr srcExpr = pe.srcExpr;
 			Set<Quad> fullCdeps = fullCdepsMap.get(currentBB);
 			if (fullCdeps != null) {
 				Pair<Expr, jq_Method> pair =
 					new Pair<Expr, jq_Method>(srcExpr, currentMethod);
 				Set<Quad> todoCdeps = todoCdepsMap.get(pair);
 				if (todoCdeps == null) {
 					todoCdeps = new ArraySet<Quad>(2);
 					todoCdepsMap.put(pair, todoCdeps);
 				}
 				for (Quad q2 : fullCdeps)
 					todoCdeps.add(q2);
 			}
 			if (DEBUG) {
 				System.out.println(CollectionUtils.toString(
 					oDstExprSet, "oDstExprSet: [", ",", "]"));
 			}
 			for (Expr e : oDstExprSet) {
 				Edge pe2 = new Edge(srcExpr, e, true);
 				tmpEdgeSet.add(pe2);
 			}
 		}
 		return tmpEdgeSet;
 	}
 
 	@Override
 	public Set<Edge> getInvkPathEdges(Quad q, Edge pe) {
 		Expr dstExpr = pe.dstExpr;
 		if (dstExpr instanceof LocalVar) {
 			LocalVar e = (LocalVar) dstExpr;
             RegisterOperand vo = Invoke.getDest(q);
             if (vo == null || vo.getRegister() != e.v) {
 				tmpEdgeSet.clear();
 				tmpEdgeSet.add(pe);
 				return tmpEdgeSet;
 			}
 		}
 		return emptyEdgeSet;
 	}
 
 	@Override
 	public Edge getSummaryEdge(jq_Method m, Edge pe) {
 		return pe;
 	}
 
 	@Override
 	public Edge getInvkPathEdge(Quad q, Edge clrPE, jq_Method tgtM, Edge tgtSE) {
 		Expr dstExpr = clrPE.dstExpr;
 		Expr srcExpr = tgtSE.srcExpr;
 		boolean matched = false;
 		// there are two cases in which a match may occur:
 		// case 1: clrPE ends in a local var which is the return var
 		// and tgtSE starts with RetnExpr.instance (representing return var)
 		// case 2: clrPE ends in an expr other than a local var
 		// and tgtSE starts in the same expr
 		if (dstExpr instanceof LocalVar) {
             LocalVar e = (LocalVar) dstExpr;
             RegisterOperand vo = Invoke.getDest(q);
             if (vo != null) {
                 Register v = vo.getRegister();
                 if (e.v == v && srcExpr == RetnExpr.instance)
 					matched = true;
 			}
 		} else if (dstExpr.equals(srcExpr))
 			matched = true;
 		if (matched) {
 			Expr dstExpr2 = tgtSE.dstExpr;
 			Expr dstExpr3;
 			if (dstExpr2 instanceof LocalVar) {
 				Register v = ((LocalVar) dstExpr2).v;
 				int i = v.getNumber();
 				ParamListOperand l = Invoke.getParamList(q);
 				int numArgs = l.length();
 				assert(i >= 0 && i < numArgs);
 				Register u = l.get(i).getRegister();
 				dstExpr3 = new LocalVar(u); 
 			} else
 				dstExpr3 = dstExpr2;
 			boolean affectedSlice;
 			if (tgtSE.affectedSlice) {
 				currSlice.add(q);
 				affectedSlice = true;
 			} else
 				affectedSlice = clrPE.affectedSlice;
 			return new Edge(clrPE.srcExpr, dstExpr3, affectedSlice);
 		}
 		return null;
 	}
 	
 	class MyQuadVisitor extends QuadVisitor.EmptyVisitor {
 		// iDstExpr can be read by visit* methods (if it is null
 		// then the visited quad is guaranteed to be a return stmt).
 		// oDstExprSet is set to null and may be written by visit*
 		// methods; if it is null upon exit then it will be assumed
 		// that the visited quad is not relevant to the slice, and
 		// the outgoing pe will be the same as the incoming pe
 		Expr iDstExpr;
 		Expr iSrcExpr;
 		Set<Expr> oDstExprSet;
 		@Override
 		public void visitReturn(Quad q) {
 			if (iDstExpr == RetnExpr.instance) {
 				Operand rx = Return.getSrc(q);
 				assert (rx != null);
 				if (rx instanceof RegisterOperand) {
 					tmpExprSet.clear();
 					RegisterOperand ro = (RegisterOperand) rx;
 					Register r = ro.getRegister();
 					tmpExprSet.add(new LocalVar(r));
 					oDstExprSet = tmpExprSet;
 				} else
 					oDstExprSet = emptyExprSet;
 			} else {
 				// cannot be a local var
 				assert (iDstExpr instanceof InstField ||
 						iDstExpr instanceof ArrayElem ||
 						iDstExpr instanceof StatField);
 			}
 		}
 		@Override
 		public void visitCheckCast(Quad q) {
 			visitMove(q);
 		}
 		@Override
 		public void visitMove(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = Move.getDest(q).getRegister();
 				if (e.v == l) {
 					Operand rx = Move.getSrc(q);
 					if (rx instanceof RegisterOperand) {
 						tmpExprSet.clear();
 						Register r = ((RegisterOperand) rx).getRegister();
 						tmpExprSet.add(new LocalVar(r));
 						oDstExprSet = tmpExprSet;
 					} else
 						oDstExprSet = emptyExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitPhi(Quad q) {
 			assert (iDstExpr != null);
 		 	if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = Phi.getDest(q).getRegister();
 				if (e.v == l) {
 					tmpExprSet.clear();
 					ParamListOperand ros = Phi.getSrcs(q);
 					int n = ros.length();
 					for (int i = 0; i < n; i++) {
 						RegisterOperand ro = ros.get(i);
 						if (ro != null) {
 							Register r = ro.getRegister();
 							tmpExprSet.add(new LocalVar(r));
 						}
 					}
 					oDstExprSet = tmpExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitALoad(Quad q) {
 			assert (iDstExpr != null);
 		 	if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = ALoad.getDest(q).getRegister();
 				if (e.v == l) {
 					tmpExprSet.clear();
 					// this stmt reads b, i, and b[i] in l = b[i], so add
 					// all three of them
 					RegisterOperand bo = (RegisterOperand) ALoad.getBase(q);
 					Register b = bo.getRegister();
 					tmpExprSet.add(new LocalVar(b));
 					Operand ix = ALoad.getIndex(q);
 					if (ix instanceof RegisterOperand) {
 						RegisterOperand io = (RegisterOperand) ix;
 						Register i = io.getRegister();
 						tmpExprSet.add(new LocalVar(i));
 					}
 					CIObj bObj = cipa.pointsTo(b);
 					for (Quad q2 : bObj.pts)
 						tmpExprSet.add(new ArrayElem(q2));
 					oDstExprSet = tmpExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitGetfield(Quad q) {
 			assert (iDstExpr != null);
 		 	if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = Getfield.getDest(q).getRegister();
 				if (e.v == l) {
 					tmpExprSet.clear();
 					// this stmt reads b and b.f in l = b.f, so add
 					// both of them
 					Operand bx = Getfield.getBase(q);
 					if (bx instanceof RegisterOperand) {
 						RegisterOperand bo = (RegisterOperand) bx;
 						Register b = bo.getRegister();
 						tmpExprSet.add(new LocalVar(b));
 						CIObj bObj = cipa.pointsTo(b);
 						jq_Field f = Getfield.getField(q).getField();
 						for (Quad q2 : bObj.pts)
 							tmpExprSet.add(new InstField(q2, f));
 					}
 					oDstExprSet = tmpExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitAStore(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof ArrayElem) {
 				Quad q2 = ((ArrayElem) iDstExpr).q;
 				RegisterOperand bo = (RegisterOperand) AStore.getBase(q);
 				Register b = bo.getRegister();
 				CIObj bObj = cipa.pointsTo(b);
 				for (Quad q3 : bObj.pts) {
 					if (q3 == q2) {
 						tmpExprSet.clear();
 						// propagate incoming expr as well since this is a
 						// may-alias check, not a must-alias check
 						tmpExprSet.add(iDstExpr);
 						// this stmt reads b, r, and i in b[i] = r, so add
 						// all three of them as well
 						tmpExprSet.add(new LocalVar(b));
 						Operand rx = AStore.getValue(q);
 						if (rx instanceof RegisterOperand) {
 							RegisterOperand ro = (RegisterOperand) rx;
 							Register r = ro.getRegister();
 							tmpExprSet.add(new LocalVar(r));
 						}
 						Operand ix = AStore.getIndex(q);
 						if (ix instanceof RegisterOperand) {
 							RegisterOperand io = (RegisterOperand) ix;
 							Register i = io.getRegister();
 							tmpExprSet.add(new LocalVar(i));
 						}
 						oDstExprSet = tmpExprSet;
 						break;
 					}
 				}
 			}
 		}
 		@Override
 		public void visitPutfield(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof InstField) {
 				InstField e = (InstField) iDstExpr;
 				jq_Field f = Putfield.getField(q).getField();
 				if (e.f == f) {
 					Operand bx = Putfield.getBase(q);
 					if (bx instanceof RegisterOperand) {
 						RegisterOperand bo = (RegisterOperand) bx;
 						Register b = bo.getRegister();
 						CIObj bObj = cipa.pointsTo(b);
 						Quad q2 = e.q;
 						for (Quad q3 : bObj.pts) {
 							if (q3 == q2) {
 								tmpExprSet.clear();
 								// propagate incoming expr as well since this is a
 								// may-alias check, not a must-alias check
 								tmpExprSet.add(iDstExpr);
 								// this stmt reads b and r in b.f = r, so add
 								// both of them
 								tmpExprSet.add(new LocalVar(b));
 								Operand rx = Putfield.getSrc(q);
 								if (rx instanceof RegisterOperand) {
 									RegisterOperand ro = (RegisterOperand) rx;
 									Register r = ro.getRegister();
 									tmpExprSet.add(new LocalVar(r));
 								}
 								oDstExprSet = tmpExprSet;
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 		@Override
 		public void visitPutstatic(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof StatField) {
 				StatField e = (StatField) iDstExpr;
 				jq_Field f = Putstatic.getField(q).getField();
 				if (f == e.f) {
 					Operand rx = Putstatic.getSrc(q);
 					if (rx instanceof RegisterOperand) {
 						tmpExprSet.clear();
 						RegisterOperand ro = (RegisterOperand) rx;
 						Register r = ro.getRegister();
 						tmpExprSet.add(new LocalVar(r));
 						oDstExprSet = tmpExprSet;
 					} else
 						oDstExprSet = emptyExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitGetstatic(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = Getstatic.getDest(q).getRegister();
 				if (e.v == l) {
 					tmpExprSet.clear();
 					jq_Field f = Getstatic.getField(q).getField();
 					tmpExprSet.add(new StatField(f));
 					oDstExprSet = tmpExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitNew(Quad q) {
 			RegisterOperand vo = New.getDest(q);
 			processAlloc(q, vo);
 		}
 		@Override
 		public void visitNewArray(Quad q) {
 			RegisterOperand vo = NewArray.getDest(q);
 			processAlloc(q, vo);
 		}
 		private void processAlloc(Quad q, RegisterOperand vo) {
 			assert (iDstExpr != null);
 		 	if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register v = vo.getRegister();
 				if (e.v == v)
 					oDstExprSet = emptyExprSet;
 			}
 		}
 		@Override
 		public void visitUnary(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = Unary.getDest(q).getRegister();
 				if (e.v == l) {
 					Operand rx = Unary.getSrc(q);
 					if (rx instanceof RegisterOperand) {
 						tmpExprSet.clear();
 						Register r = ((RegisterOperand) rx).getRegister();
 						tmpExprSet.add(new LocalVar(r));
 						oDstExprSet = tmpExprSet;
 					} else
 						oDstExprSet = emptyExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitBinary(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = Binary.getDest(q).getRegister();
 				if (e.v == l) {
 					tmpExprSet.clear();
 					Operand rx1 = Binary.getSrc1(q);
 					if (rx1 instanceof RegisterOperand) {
 						Register r1 = ((RegisterOperand) rx1).getRegister();
 						tmpExprSet.add(new LocalVar(r1));
 					}
 					Operand rx2 = Binary.getSrc2(q);
 					if (rx2 instanceof RegisterOperand) {
 						Register r2 = ((RegisterOperand) rx2).getRegister();
 						tmpExprSet.add(new LocalVar(r2));
 					}
 					oDstExprSet = tmpExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitInstanceOf(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = InstanceOf.getDest(q).getRegister();
 				if (e.v == l) {
 					Operand rx = InstanceOf.getSrc(q);
 					if (rx instanceof RegisterOperand) {
 						tmpExprSet.clear();
 						Register r = ((RegisterOperand) rx).getRegister();
 						tmpExprSet.add(new LocalVar(r));
 						oDstExprSet = tmpExprSet;
 					} else
 						oDstExprSet = emptyExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitALength(Quad q) {
 			assert (iDstExpr != null);
 			if (iDstExpr instanceof LocalVar) {
 				LocalVar e = (LocalVar) iDstExpr;
 				Register l = ALength.getDest(q).getRegister();
 				if (e.v == l) {
 					Operand rx = ALength.getSrc(q);
 					if (rx instanceof RegisterOperand) {
 						tmpExprSet.clear();
 						Register r = ((RegisterOperand) rx).getRegister();
 						tmpExprSet.add(new LocalVar(r));
 						oDstExprSet = tmpExprSet;
 					} else
 						oDstExprSet = emptyExprSet;
 				}
 			}
 		}
 		@Override
 		public void visitIntIfCmp(Quad q) {
 			tmpPair.val0 = iSrcExpr;
 			tmpPair.val1 = currentMethod;
 			Set<Quad> qSet = todoCdepsMap.get(tmpPair);
 			if (qSet != null && qSet.contains(q)) {
 				tmpExprSet.clear();
 				Operand rx1 = IntIfCmp.getSrc1(q);
 				if (rx1 instanceof RegisterOperand) {
 					Register r1 = ((RegisterOperand) rx1).getRegister();
 					tmpExprSet.add(new LocalVar(r1));
 				}
 				Operand rx2 = IntIfCmp.getSrc2(q);
 				if (rx2 instanceof RegisterOperand) {
 					Register r2 = ((RegisterOperand) rx2).getRegister();
 					tmpExprSet.add(new LocalVar(r2));
 				}
 				tmpExprSet.add(iDstExpr);
 				oDstExprSet = tmpExprSet;
 			}
 		}
 		@Override
 		public void visitLookupSwitch(Quad q) {
 			tmpPair.val0 = iSrcExpr;
 			tmpPair.val1 = currentMethod;
 			Set<Quad> qSet = todoCdepsMap.get(tmpPair);
 			if (qSet != null && qSet.contains(q)) {
 				tmpExprSet.clear();
 				Operand rx = LookupSwitch.getSrc(q);
 				if (rx instanceof RegisterOperand) {
 					Register r = ((RegisterOperand) rx).getRegister();
 					tmpExprSet.add(new LocalVar(r));
 				}
 				tmpExprSet.add(iDstExpr);
 				oDstExprSet = tmpExprSet;
 			}
 		}
 		@Override
 		public void visitTableSwitch(Quad q) {
 			tmpPair.val0 = iSrcExpr;
 			tmpPair.val1 = currentMethod;
 			Set<Quad> qSet = todoCdepsMap.get(tmpPair);
 			if (qSet != null && qSet.contains(q)) {
 				tmpExprSet.clear();
 				Operand rx = TableSwitch.getSrc(q);
 				if (rx instanceof RegisterOperand) {
 					Register r = ((RegisterOperand) rx).getRegister();
 					tmpExprSet.add(new LocalVar(r));
 				}
 				tmpExprSet.add(iDstExpr);
 				oDstExprSet = tmpExprSet;
 			}
 		}
 /*
 		@Override
 		public void visitMonitor(Quad q) {
 		}
 */
 	}
 }
 
