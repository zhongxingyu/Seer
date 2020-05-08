 package soot.jimple.infoflow;
 
 import heros.FlowFunction;
 import heros.FlowFunctions;
 import heros.InterproceduralCFG;
 import heros.flowfunc.Identity;
 import heros.flowfunc.KillAll;
 import heros.solver.PathEdge;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import soot.Local;
 import soot.SootMethod;
 import soot.Unit;
 import soot.Value;
 import soot.jimple.ArrayRef;
 import soot.jimple.AssignStmt;
 import soot.jimple.BinopExpr;
 import soot.jimple.Constant;
 import soot.jimple.DefinitionStmt;
 import soot.jimple.FieldRef;
 import soot.jimple.IdentityStmt;
 import soot.jimple.InstanceFieldRef;
 import soot.jimple.InstanceInvokeExpr;
 import soot.jimple.NewArrayExpr;
 import soot.jimple.NewExpr;
 import soot.jimple.ReturnStmt;
 import soot.jimple.StaticFieldRef;
 import soot.jimple.Stmt;
 import soot.jimple.infoflow.data.Abstraction;
 import soot.jimple.infoflow.heros.InfoflowSolver;
 import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
 import soot.jimple.infoflow.util.BaseSelector;
 import soot.jimple.toolkits.ide.icfg.BackwardsInterproceduralCFG;
 
 /**
  * class which contains the flow functions for the backwards solver. This is required for on-demand alias analysis.
  */
 public class BackwardsInfoflowProblem extends AbstractInfoflowProblem {
 	private InfoflowSolver fSolver;
 
 	public void setTaintWrapper(ITaintPropagationWrapper wrapper) {
 		taintWrapper = wrapper;
 	}
 
 	public BackwardsInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
 		super(icfg);
 	}
 
 	public BackwardsInfoflowProblem() {
 		super(new BackwardsInterproceduralCFG());
 	}
 
 	public void setForwardSolver(InfoflowSolver forwardSolver) {
 		fSolver = forwardSolver;
 	}
 
 	@Override
 	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
 		return new FlowFunctions<Unit, Abstraction, SootMethod>() {
 
 			@Override
 			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest) {
 				
 				if(src instanceof IdentityStmt){
 					//invoke forward solver - but only if we already inspected some stmts:
 					return new FlowFunction<Abstraction>() {
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction source) {
 							IdentityStmt iStmt = (IdentityStmt) src;
 							if(iStmt.getLeftOp().equals(source.getAccessPath().getPlainValue())){
 								for (Unit u : ((BackwardsInterproceduralCFG) interproceduralCFG()).getPredsOf(src))
 									fSolver.processEdge(new PathEdge<Unit, Abstraction>
 										(source.getAbstractionFromCallEdge(), u, source));
 								return Collections.emptySet();
 							}
 							return Collections.singleton(source);
 						}
 					};
 				}
 				// taint is propagated with assignStmt
 				if (src instanceof AssignStmt) {
 					AssignStmt assignStmt = (AssignStmt) src;
 
 					final Value leftValue = BaseSelector.selectBase(assignStmt.getLeftOp(), true);
 					final Value rightValue = BaseSelector.selectBase(assignStmt.getRightOp(), false);
 
 					return new FlowFunction<Abstraction>() {
 
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction source) {
 							// A backward analysis looks for aliases of existing taints and thus
 							// cannot create new taints out of thin air
 							if (source.equals(zeroValue))
 								return Collections.emptySet();
 																					
 							// Check whether the left side of the assignment matches our
 							// current taint abstraction
 							boolean leftSideMatches = leftValue instanceof Local
 									&& leftValue.equals(source.getAccessPath().getPlainValue());
 							if (!leftSideMatches && leftValue instanceof InstanceFieldRef) {
 								InstanceFieldRef ifr = (InstanceFieldRef) leftValue;
 								if (ifr.getBase().equals(source.getAccessPath().getPlainValue())
 										&& ifr.getField().equals(source.getAccessPath().getFirstField()))
 									leftSideMatches = true;
 							}
 							if (!leftSideMatches && leftValue instanceof StaticFieldRef) {
 								StaticFieldRef sfr = (StaticFieldRef) leftValue;
 								if (sfr.getField().equals(source.getAccessPath().getFirstField()))
 									leftSideMatches = true;
 							}
 							
 							// Is the left side overwritten completely?
 							if (leftSideMatches) {
 								// If we have an assignment to the base local of the current taint,
 								// all taint propagations must be below that point, so this is the
 								// right point to turn around.
 								if (triggerInaktiveTaintOrReverseFlow(leftValue, source))
 									for (Unit u : ((BackwardsInterproceduralCFG) interproceduralCFG()).getPredsOf(src))
 										fSolver.processEdge(new PathEdge<Unit, Abstraction>(source.getAbstractionFromCallEdge(), u, source));
 
 								// Termination shortcut: If the right side is a value we do not track,
 								// we can stop here.
 								if (rightValue instanceof NewExpr
 											|| rightValue instanceof NewArrayExpr
 											|| rightValue instanceof Constant) {
 									return Collections.emptySet();
 								}
 							}
 
 							Set<Abstraction> res = new HashSet<Abstraction>();
 							if (!leftSideMatches)
 								res.add(source);
 
 							// If we assign a constant, there is no need to track the right side
 							// any further or do any forward propagation since constants cannot
 							// carry taint.
 							if (rightValue instanceof Constant)
 								return res;
 							
 							// We only process heap objects. Binary operations can only
 							// be performed on primitive objects.
 							if (rightValue instanceof BinopExpr)
 								return res;
 
 							// Check whether we need to start a forward search for taints.
 							if (triggerInaktiveTaintOrReverseFlow(leftValue, source)) {
 								// If the tainted value is assigned to some other local variable,
 								// this variable is an alias as well.
 								if (rightValue instanceof InstanceFieldRef) {
 									InstanceFieldRef ref = (InstanceFieldRef) rightValue;
 									if (source.getAccessPath().isInstanceFieldRef()
 											&& ref.getBase().equals(source.getAccessPath().getPlainValue())
 											&& ref.getField().equals(source.getAccessPath().getFirstField())) {
 										Abstraction abs = source.deriveNewAbstraction(leftValue, true, null);
 										res.add(abs);
 									}
 								}
 								else if (rightValue instanceof StaticFieldRef) {
 									StaticFieldRef ref = (StaticFieldRef) rightValue;
 									if (source.getAccessPath().isStaticFieldRef()
 											&& ref.getField().equals(source.getAccessPath().getFirstField())) {
 										Abstraction abs = source.deriveNewAbstraction(leftValue, true, null);
 										res.add(abs);
 									}
 								}
 								else if (rightValue.equals(source.getAccessPath().getPlainValue())) {
 									Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(leftValue));
 									res.add(abs);
 								}
 							}
 														
 							if (!(rightValue instanceof NewExpr || rightValue instanceof NewArrayExpr
 									|| rightValue instanceof Constant
 									|| rightValue instanceof BinopExpr)) {
 								// if we have the tainted value on the left side of the assignment,
 								// we also have to track the right side of the assignment
 								boolean addRightValue = false;
 								boolean cutFirstField = false;
 
 								// if both are fields, we have to compare their fieldName via equals and their bases via PTS
 								if (leftValue instanceof InstanceFieldRef) {
 									InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
 									if (leftRef.getBase().equals(source.getAccessPath().getPlainLocal())) {
 										if (source.getAccessPath().isInstanceFieldRef()) {
 											if (leftRef.getField().equals(source.getAccessPath().getFirstField())) {
 												addRightValue = true;
 												cutFirstField = true;
 											}
 										} else {
 											addRightValue = true;
 										}
 									}
 									// indirect taint propagation:
 									// if leftValue is local and source is instancefield of this local:
 								} else if (leftValue instanceof Local && source.getAccessPath().isInstanceFieldRef()) {
 									Local base = source.getAccessPath().getPlainLocal(); // ?
 									if (leftValue.equals(base)) {
 										res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(rightValue)));
 									}
 								} else if (leftValue instanceof ArrayRef) {
 									Local leftBase = (Local) ((ArrayRef) leftValue).getBase();
 									if (leftBase.equals(source.getAccessPath().getPlainValue())) {
 										addRightValue = true;
 									}
 									// generic case, is true for Locals, ArrayRefs that are equal etc..
 								} else if (leftValue.equals(source.getAccessPath().getPlainValue())) {
 									addRightValue = true;
 								}
 								
 								// if one of them is true -> add rightValue
 								if (addRightValue) {
 									Abstraction newAbs = source.deriveNewAbstraction(rightValue, cutFirstField, null);
 									res.add(newAbs);
 								}
 							}
 							
 							for (Abstraction newAbs : res) {
 								// we have to send forward pass, for example for
 								// $r1 = l0.<java.lang.AbstractStringBuilder: char[] value>
 								if (newAbs.getAccessPath().isStaticFieldRef()
 										|| triggerInaktiveTaintOrReverseFlow(newAbs.getAccessPath().getPlainValue(), newAbs)) {
 									for (Unit u : ((BackwardsInterproceduralCFG) interproceduralCFG()).getPredsOf(src)){
 										fSolver.processEdge(new PathEdge<Unit, Abstraction>(source.getAbstractionFromCallEdge(), u, newAbs));
 									}
 								}
 							}
 							
 							return res; 
 						}
 					};
 
 				}
 				return Identity.v();
 			}
 
 			@Override
 			public FlowFunction<Abstraction> getCallFlowFunction(final Unit src, final SootMethod dest) {
 				return new FlowFunction<Abstraction>() {
 
 					@Override
 					public Set<Abstraction> computeTargets(Abstraction source) {
 						if (source.equals(zeroValue)) {
 							return Collections.emptySet();
 						}
 						/* Backwards taint wrapping is highly complicated, so we ignore it for the moment
 						if (taintWrapper != null && taintWrapper.supportsBackwardWrapping() && taintWrapper.supportsTaintWrappingForClass(dest.getDeclaringClass())) {
 							// taint is propagated in CallToReturnFunction, so we do not need any taint here if it is exclusive:
 							if(taintWrapper.isExclusive((Stmt)src, 0, null)){
 								return Collections.emptySet();
 							}
 						}
 						*/
 
 						Set<Abstraction> res = new HashSet<Abstraction>();
 						
 						// if the returned value is tainted - taint values from return statements
 						if (src instanceof DefinitionStmt) {
 							DefinitionStmt defnStmt = (DefinitionStmt) src;
 							Value leftOp = defnStmt.getLeftOp();
 							if (leftOp.equals(source.getAccessPath().getPlainValue())) {
 								// look for returnStmts:
 								for (Unit u : dest.getActiveBody().getUnits()) {
 									if (u instanceof ReturnStmt) {
 										ReturnStmt rStmt = (ReturnStmt) u;
 										if (rStmt.getOp() instanceof Local
 												|| rStmt.getOp() instanceof FieldRef) {
 											Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(rStmt.getOp()));
											abs.setAbstractionFromCallEdge(abs);
 											assert abs != source;		// our source abstraction must be immutable
 											res.add(abs);
 										}
 									}
 								}
 							}
 						}
 
 						// easy: static
 						if (source.getAccessPath().isStaticFieldRef()) {
 							Abstraction abs = source.clone();
							abs.setAbstractionFromCallEdge(abs);
 							assert (abs.equals(source) && abs.hashCode() == source.hashCode());
 							assert abs != source;		// our source abstraction must be immutable
 							res.add(abs);
 						}
 
 						// checks: this/fields
 
 						Value sourceBase = source.getAccessPath().getPlainValue();
 						Stmt iStmt = (Stmt) src;
 						if (!dest.isStatic()) {
 							Local thisL = dest.getActiveBody().getThisLocal();
 							InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
 							if (iIExpr.getBase().equals(sourceBase)) {
 								// there is only one case in which this must be added, too: if the caller-Method has the same thisLocal - check this:
 
 								boolean param = false;
 								// check if it is not one of the params (then we have already fixed it)
 								for (int i = 0; i < dest.getParameterCount(); i++) {
 									if (iStmt.getInvokeExpr().getArg(i).equals(sourceBase)) {
 										param = true;
 										break;
 									}
 								}
 								if (!param) {
 									if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
 										Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(thisL));
										abs.setAbstractionFromCallEdge(abs);
 										assert abs != source;		// our source abstraction must be immutable
 										res.add(abs);
 									}
 								}
 							}
 							// TODO: add testcase which requires params to be propagated
 
 						}
 					
 						return res;
 					}
 				};
 			}
 
 			@Override
 			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, Unit exitStmt, final Unit retSite) {					
 				return KillAll.v();
 			}
 
 			@Override
 			public FlowFunction<Abstraction> getCallToReturnFlowFunction(Unit call, final Unit returnSite) {
 				// special treatment for native methods:
 				if (call instanceof Stmt) {
 					final Stmt iStmt = (Stmt) call;
 
 					return new FlowFunction<Abstraction>() {
 
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction source) {
 							Set<Abstraction> res = new HashSet<Abstraction>();
 							// only pass source if the source is not created by this methodcall
 							if (iStmt instanceof DefinitionStmt && ((DefinitionStmt) iStmt).getLeftOp().equals
 									(source.getAccessPath().getPlainValue())){
 								//terminates here, but we have to start a forward pass to consider all method calls:
 								for (Unit u : ((BackwardsInterproceduralCFG) interproceduralCFG()).getPredsOf(iStmt))
 									fSolver.processEdge(new PathEdge<Unit, Abstraction>(source.getAbstractionFromCallEdge(), u, source));
 							}else{
 								res.add(source);
 							}
 							return res;
 						}
 					};
 				}
 				return Identity.v();
 			}
 		};
 	}
 
 }
