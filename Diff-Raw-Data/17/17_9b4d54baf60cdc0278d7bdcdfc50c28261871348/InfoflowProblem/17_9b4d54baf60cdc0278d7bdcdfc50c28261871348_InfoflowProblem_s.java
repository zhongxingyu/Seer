 /*******************************************************************************
  * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
  * Bodden, and others.
  ******************************************************************************/
 package soot.jimple.infoflow.problems;
 
 import heros.FlowFunction;
 import heros.FlowFunctions;
 import heros.InterproceduralCFG;
 import heros.flowfunc.Identity;
 import heros.flowfunc.KillAll;
 import heros.solver.PathEdge;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import soot.Local;
 import soot.SootField;
 import soot.SootMethod;
 import soot.Unit;
 import soot.Value;
 import soot.ValueBox;
 import soot.jimple.ArrayRef;
 import soot.jimple.AssignStmt;
 import soot.jimple.CaughtExceptionRef;
 import soot.jimple.Constant;
 import soot.jimple.DefinitionStmt;
 import soot.jimple.FieldRef;
 import soot.jimple.IdentityStmt;
 import soot.jimple.IfStmt;
 import soot.jimple.InstanceFieldRef;
 import soot.jimple.InstanceInvokeExpr;
 import soot.jimple.InvokeExpr;
 import soot.jimple.LookupSwitchStmt;
 import soot.jimple.ReturnStmt;
 import soot.jimple.StaticFieldRef;
 import soot.jimple.Stmt;
 import soot.jimple.TableSwitchStmt;
 import soot.jimple.ThrowStmt;
 import soot.jimple.infoflow.InfoflowResults;
 import soot.jimple.infoflow.data.Abstraction;
 import soot.jimple.infoflow.data.Abstraction.SourceContextAndPath;
 import soot.jimple.infoflow.data.AbstractionAtSink;
 import soot.jimple.infoflow.data.AccessPath;
 import soot.jimple.infoflow.heros.ConcurrentHashSet;
 import soot.jimple.infoflow.heros.InfoflowCFG.UnitContainer;
 import soot.jimple.infoflow.heros.InfoflowSolver;
 import soot.jimple.infoflow.heros.SolverCallToReturnFlowFunction;
 import soot.jimple.infoflow.heros.SolverNormalFlowFunction;
 import soot.jimple.infoflow.heros.SolverReturnFlowFunction;
 import soot.jimple.infoflow.source.DefaultSourceSinkManager;
 import soot.jimple.infoflow.source.ISourceSinkManager;
 import soot.jimple.infoflow.util.BaseSelector;
 import soot.jimple.toolkits.ide.icfg.JimpleBasedBiDiICFG;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Table;
 
 public class InfoflowProblem extends AbstractInfoflowProblem {
 
 	private InfoflowSolver bSolver; 
 	private final ISourceSinkManager sourceSinkManager;
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
     
     private final Map<Unit, Set<Unit>> activationUnitsToCallSites = new ConcurrentHashMap<Unit, Set<Unit>>();
     private final Table<SootMethod, AccessPath, Set<AccessPath>> globalAliases = HashBasedTable.create();
     
 	protected final Set<AbstractionAtSink> results = new HashSet<AbstractionAtSink>();
 	protected InfoflowResults infoflowResults = null;
 
 	/**
 	 * Computes the taints produced by a taint wrapper object
 	 * @param d1 The context (abstraction at the method's start node)
 	 * @param iStmt The call statement the taint wrapper shall check for well-
 	 * known methods that introduce black-box taint propagation
 	 * @param source The taint source
 	 * @return The taints computed by the wrapper
 	 */
 	private Set<Abstraction> computeWrapperTaints
 			(Abstraction d1,
 			final Stmt iStmt,
 			Abstraction source) {
 		Set<Abstraction> res = new HashSet<Abstraction>();
 		if(taintWrapper == null)
 			return Collections.emptySet();
 		
 		if (!source.getAccessPath().isStaticFieldRef() && !source.getAccessPath().isEmpty())
 			if(iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
 				InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
 				boolean found = iiExpr.getBase().equals(source.getAccessPath().getPlainValue());
 				if (!found)
 					for (Value param : iiExpr.getArgs())
 						if (source.getAccessPath().getPlainValue().equals(param)) {
 							found = true;
 							break;
 				}
 				if (!found)
 					return Collections.emptySet();
 			}
 			
 		Set<AccessPath> vals = taintWrapper.getTaintsForMethod(iStmt, source.getAccessPath());
 		if(vals != null) {
 			for (AccessPath val : vals) {
 				Abstraction newAbs = source.deriveNewAbstraction(val, iStmt);
 				res.add(newAbs);
 
 				// If the taint wrapper creates a new taint, this must be propagated
 				// backwards as there might be aliases for the base object
 				if ((enableStaticFields && newAbs.getAccessPath().isStaticFieldRef())
 						|| triggerInaktiveTaintOrReverseFlow(val.getPlainValue(), newAbs))
 					computeAliasTaints(d1, (Stmt) iStmt, val.getPlainValue(), res,
 							interproceduralCFG().getMethodOf(iStmt), newAbs);											
 			}
 		}
 
 		return res;
 	}
 	
 	/**
 	 * Computes the taints for the aliases of a given tainted variable
 	 * @param d1 The context in which the variable has been tainted
 	 * @param src The statement that tainted the variable
 	 * @param targetValue The target value which has been tainted
 	 * @param taintSet The set to which all generated alias taints shall be
 	 * added
 	 * @param method The method containing src
 	 * @param newAbs The newly generated abstraction for the variable taint
 	 */
 	private void computeAliasTaints
 			(final Abstraction d1, final Stmt src,
 			final Value targetValue, Set<Abstraction> taintSet,
 			SootMethod method, Abstraction newAbs) {
 		// If we are not in a conditionally-called method, we run the
 		// full alias analysis algorithm. Otherwise, we use a global
 		// non-flow-sensitive approximation.
 		if (!d1.getAccessPath().isEmpty()) {
 			Abstraction bwAbs = newAbs.deriveInactiveAbstraction();
 			for (Unit predUnit : interproceduralCFG().getPredsOf(src))
 				bSolver.processEdge(new PathEdge<Unit, Abstraction>(d1,
 						predUnit, bwAbs));
 		} else if (targetValue instanceof InstanceFieldRef) {
 			// Use global aliasing
 			Value baseValue = ((InstanceFieldRef) targetValue).getBase();
 			Set<AccessPath> aliases = globalAliases.get(method, new AccessPath(
 					baseValue));
 			if (aliases != null)
 				for (AccessPath ap : aliases) {
 					Abstraction aliasAbs = newAbs.deriveNewAbstraction(
 							ap.merge(newAbs.getAccessPath()), src);
 					taintSet.add(aliasAbs);
 				}
 		}
 	}
 	
 	/**
 	 * Computes the global non-flow-sensitive alias information for the given
 	 * method
 	 * @param method The method for which to compute the alias information
 	 */
 	private void computeGlobalAliases(SootMethod method) {
 		assert enableImplicitFlows;
 		
 		synchronized (globalAliases) {
 			// If we already know the aliases for the given method, there is nothing
 			// left to be done
 			if (globalAliases.containsRow(method))
 				return;
 	
 			// Find the aliases
 			for (Unit u : method.getActiveBody().getUnits()) {
 				if (!(u instanceof AssignStmt))
 					continue;
 				final AssignStmt assign = (AssignStmt) u;
 				
 				// Aliases can only be generated on the heap
 				if (!(assign.getLeftOp() instanceof FieldRef
 						&& (assign.getRightOp() instanceof FieldRef
 								|| assign.getRightOp() instanceof Local)))
 					if (!(assign.getRightOp() instanceof FieldRef
 							&& (assign.getLeftOp() instanceof FieldRef
 									|| assign.getLeftOp() instanceof Local)))
 						continue;
 				
 				final AccessPath apLeft = new AccessPath(assign.getLeftOp());
 				final AccessPath apRight = new AccessPath(assign.getRightOp());
 
 				Set<AccessPath> mapLeft = globalAliases.get(method, apLeft);
 				if (mapLeft == null) {
 					mapLeft = new HashSet<AccessPath>();
 					globalAliases.put(method, apLeft, mapLeft);
 				}
 				mapLeft.add(apRight);
 
 				Set<AccessPath> mapRight = globalAliases.get(method, apRight);
 				if (mapRight == null) {
 					mapRight = new HashSet<AccessPath>();
 					globalAliases.put(method, apRight, mapRight);
 				}
 				mapLeft.add(apLeft);
 			}
 		}
 	}
 	
 	@Override
 	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
 		return new FlowFunctions<Unit, Abstraction, SootMethod>() {
 
 			/**
 			 * Creates a new taint abstraction for the given value
 			 * @param src The source statement from which the taint originated
 			 * @param targetValue The target value that shall now be tainted
 			 * @param source The incoming taint abstraction from the source
 			 * @param taintSet The taint set to which to add all newly produced
 			 * taints
 			 */
 			private void addTaintViaStmt
 					(final Abstraction d1,
 					final Stmt src,
 					final Value targetValue,
 					Abstraction source,
 					Set<Abstraction> taintSet,
 					boolean cutFirstField,
 					SootMethod method) {
 				// Keep the original taint
 				taintSet.add(source);
 				
 				// Do not taint static fields unless the option is enabled
 				if (!enableStaticFields && targetValue instanceof StaticFieldRef)
 					return;
 				
 				// Strip array references to their respective base
 				Value baseTarget = targetValue;
 				if (targetValue instanceof ArrayRef)
 					baseTarget = ((ArrayRef) targetValue).getBase();
 
 				// also taint the target of the assignment
 				Abstraction newAbs;
 				if (!source.getAccessPath().isEmpty())
 					newAbs = source.deriveNewAbstraction(baseTarget, cutFirstField, src);
 				else
 					newAbs = source.deriveNewAbstraction(new AccessPath(targetValue), src);
 				taintSet.add(newAbs);
 								
 				if (triggerInaktiveTaintOrReverseFlow(targetValue, newAbs) && newAbs.isAbstractionActive()) {
 					// If we overwrite the complete local, there is no need for
 					// a backwards analysis
 					if (!(targetValue.equals(newAbs.getAccessPath().getPlainValue())
 							&& newAbs.getAccessPath().isLocal()))
 						computeAliasTaints(d1, src, targetValue, taintSet, method, newAbs);
 				}
 			}
 			
 			private boolean isFieldReadByCallee(
 					final Set<?> fieldsReadByCallee, Abstraction source) {
 				boolean isFieldRead = fieldsReadByCallee == null;
 				if (!isFieldRead) {
 					for (Object oField : fieldsReadByCallee)
 						if (oField instanceof SootField)
 							if (source.getAccessPath().getFirstField() == oField) {
 								isFieldRead = true;
 								break;
 							}
 				}
 				return isFieldRead;
 			}
 
 			private boolean isCallSiteActivatingTaint(Unit callSite, Unit activationUnit) {
 				if (activationUnit == null)
 					return false;
 				Set<Unit> callSites = activationUnitsToCallSites.get(activationUnit);
 				return (callSites != null && callSites.contains(callSite));
 			}
 			
 			private boolean registerActivationCallSite(Unit callSite, Abstraction activationAbs) {
 				if (!activationAbs.isAbstractionActive())
 					return false;
 				Unit activationUnit = activationAbs.getActivationUnit();
 				
 				synchronized (activationUnitsToCallSites) {
 					if (!activationUnitsToCallSites.containsKey(activationUnit))
 						activationUnitsToCallSites.put(activationUnit, new ConcurrentHashSet<Unit>());
 				}
 					
 				Set<Unit> callSites = activationUnitsToCallSites.get(activationUnit);
 				return callSites.add(callSite);
 			}
 
 			@Override
 			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest) {
 				// If we compute flows on parameters, we create the initial
 				// flow fact here
 				if (src instanceof IdentityStmt) {
 					final IdentityStmt is = (IdentityStmt) src;
 					return new FlowFunction<Abstraction>() {
 
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction source) {
 							if (stopAfterFirstFlow && !results.isEmpty())
 								return Collections.emptySet();
 
 							// Check whether we must leave a conditional branch
 							if (source.isTopPostdominator(is)) {
 								source = source.dropTopPostdominator();
 								// Have we dropped the last postdominator for an empty taint?
 								if (source.getAccessPath().isEmpty() && source.getTopPostdominator() == null)
 									return Collections.emptySet();
 							}
 
 							Set<Abstraction> res = new HashSet<Abstraction>();
 							boolean addOriginal = true;
 							if (is.getRightOp() instanceof CaughtExceptionRef) {
 								if (source.getExceptionThrown()) {
 									res.add(source.deriveNewAbstractionOnCatch(is.getLeftOp(), is));
 									addOriginal = false;
 								}
 							}
 
 							if (addOriginal)
 								res.add(source);
 							
 							if (sourceSinkManager.isSource(is, interproceduralCFG())) {
 								Abstraction abs = new Abstraction(is.getLeftOp(), is.getRightOp(), is, false, true, is);
 								res.add(abs);
 							}
 							
 							return res;
 						}
 					};
 
 				}
 
 				// taint is propagated with assignStmt
 				else if (src instanceof AssignStmt) {
 					final AssignStmt assignStmt = (AssignStmt) src;
 					Value right = assignStmt.getRightOp();
 					Value left = assignStmt.getLeftOp();
 
 					final Value leftValue = BaseSelector.selectBase(left, true);
 					final Set<Value> rightVals = BaseSelector.selectBaseList(right, true);
 
 					final boolean isSink = sourceSinkManager != null
 							? sourceSinkManager.isSink(assignStmt, interproceduralCFG()) : false;
 
 					return new SolverNormalFlowFunction() {
 
 						 @Override
 						public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source) {
 							if (stopAfterFirstFlow && !results.isEmpty())
 								return Collections.emptySet();
 							
 							boolean addLeftValue = false;
 							boolean cutFirstField = false;
 							Set<Abstraction> res = new HashSet<Abstraction>();
 							
 							// shortcuts:
 							// on NormalFlow taint cannot be created
 							if (source.equals(zeroValue)) {
 								return Collections.emptySet();
 							}
 
 							// Check whether we must leave a conditional branch
 							if (source.isTopPostdominator(assignStmt)) {
 								source = source.dropTopPostdominator();
 								// Have we dropped the last postdominator for an empty taint?
 								if (source.getAccessPath().isEmpty() && source.getTopPostdominator() == null)
 									return Collections.emptySet();
 							}
 							
 							Abstraction newSource;
 							if (!source.isAbstractionActive() && src.equals(source.getActivationUnit()))
 								newSource = source.getActiveCopy();
 							else
 								newSource = source;
 							
							if (interproceduralCFG().getMethodOf(src).getName().equals("setVal"))
								System.out.println("x");
																					
 							// If we have a non-empty postdominator stack, we taint
 							// every assignment target
 							if (newSource.getTopPostdominator() != null || newSource.getAccessPath().isEmpty()) {
 								// We can skip over all local assignments inside conditionally-
 								// called functions since they are not visible in the caller
 								// anyway
 								if (d1 != null && d1.getAccessPath().isEmpty() && !(leftValue instanceof FieldRef))
 									return Collections.singleton(newSource);
 								
 								if (newSource.getAccessPath().isEmpty())
 									addLeftValue = true;
 							}
 							
 							if (!addLeftValue) {
 								for (Value rightValue : rightVals) {
 									// check if static variable is tainted (same name, same class)
 									//y = X.f && X.f tainted --> y, X.f tainted
 									if (enableStaticFields
 											&& newSource.getAccessPath().isStaticFieldRef()
 											&& rightValue instanceof StaticFieldRef) {
 										StaticFieldRef rightRef = (StaticFieldRef) rightValue;
 										if (newSource.getAccessPath().getFirstField().equals(rightRef.getField())) {
 											addLeftValue = true;
 											cutFirstField = true;
 										}
 									}
 									// if both are fields, we have to compare their fieldName via equals and their bases
 									//y = x.f && x tainted --> y, x tainted
 									//y = x.f && x.f tainted --> y, x tainted
 									else if (rightValue instanceof InstanceFieldRef) {								
 										InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
 										Local rightBase = (Local) rightRef.getBase();
 										Local sourceBase =  newSource.getAccessPath().getPlainLocal();
 										if (rightBase.equals(sourceBase)) {
 											if (newSource.getAccessPath().isInstanceFieldRef()) {
 												if (rightRef.getField().equals(newSource.getAccessPath().getFirstField())) {
 													addLeftValue = true;
 													cutFirstField = true;
 												}
 											}
 											else
 												addLeftValue = true;
 										}
 									}
 									// indirect taint propagation:
 									// if rightvalue is local and source is instancefield of this local:
 									// y = x && x.f tainted --> y.f, x.f tainted
 									// y.g = x && x.f tainted --> y.g.f, x.f tainted
 									else if (rightValue instanceof Local && newSource.getAccessPath().isInstanceFieldRef()) {
 										Local base = newSource.getAccessPath().getPlainLocal();
 										if (rightValue.equals(base)) {
 											addLeftValue = true;
 										}
 									}
 									//y = x[i] && x tainted -> x, y tainted
 									else if (rightValue instanceof ArrayRef) {
 										Local rightBase = (Local) ((ArrayRef) rightValue).getBase();
 										if (rightBase.equals(newSource.getAccessPath().getPlainValue()))
 											addLeftValue = true;
 									}
 									// generic case, is true for Locals, ArrayRefs that are equal etc..
 									//y = x && x tainted --> y, x tainted
 									else if (rightValue.equals(newSource.getAccessPath().getPlainValue())) {
 										addLeftValue = true;
 									}
 								}
 							}
 
 							// if one of them is true -> add leftValue
 							if (addLeftValue) {
 								if (isSink && newSource.isAbstractionActive() && newSource.getAccessPath().isEmpty())
 									results.add(new AbstractionAtSink(newSource, leftValue, assignStmt));
 								if(triggerInaktiveTaintOrReverseFlow(leftValue, newSource) || newSource.isAbstractionActive())
 									addTaintViaStmt(d1, (Stmt) src, leftValue, newSource, res, cutFirstField,
 											interproceduralCFG().getMethodOf(src));
 								res.add(newSource);
 								return res;
 							}
 							
 							// If we have propagated taint, we have returned from this method by now
 							
 							//if leftvalue contains the tainted value -> it is overwritten - remove taint:
 							//but not for arrayRefs:
 							// x[i] = y --> taint is preserved since we do not distinguish between elements of collections 
 							//because we do not use a MUST-Alias analysis, we cannot delete aliases of taints 
 							if (assignStmt.getLeftOp() instanceof ArrayRef)
 								return Collections.singleton(newSource);
 							
 							if(newSource.getAccessPath().isInstanceFieldRef()) {
 								//x.f = y && x.f tainted --> no taint propagated
 								if (leftValue instanceof InstanceFieldRef) {
 									InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
 									if (leftRef.getBase().equals(newSource.getAccessPath().getPlainValue())) {
 										if (leftRef.getField().equals(newSource.getAccessPath().getFirstField())) {
 											return Collections.emptySet();
 										}
 										
 									}
 								}
 								//x = y && x.f tainted -> no taint propagated
 								else if (leftValue instanceof Local){
 									if (leftValue.equals(newSource.getAccessPath().getPlainValue())) {
 										return Collections.emptySet();
 									}
 								}	
 							}
 							//X.f = y && X.f tainted -> no taint propagated. Kills are allowed even if
 							// static field tracking is disabled
 							else if (newSource.getAccessPath().isStaticFieldRef()){
 								if(leftValue instanceof StaticFieldRef
 										&& ((StaticFieldRef)leftValue).getField().equals(newSource.getAccessPath().getFirstField())){
 									return Collections.emptySet();
 								}
 								
 							}
 							//when the fields of an object are tainted, but the base object is overwritten
 							// then the fields should not be tainted any more
 							//x = y && x.f tainted -> no taint propagated
 							else if(newSource.getAccessPath().isLocal() && leftValue.equals(newSource.getAccessPath().getPlainValue())){
 								return Collections.emptySet();
 							}
 							//nothing applies: z = y && x tainted -> taint is preserved
 							res.add(newSource);
 							return res;
 						}
 					};
 				}
 				// for unbalanced problems, return statements correspond to
 				// normal flows, not return flows, because there is no return
 				// site we could jump to
 				else if (src instanceof ReturnStmt) {
 					final ReturnStmt returnStmt = (ReturnStmt) src;
 					return new FlowFunction<Abstraction>() {
 
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction source) {
 							if (stopAfterFirstFlow && !results.isEmpty())
 								return Collections.emptySet();
 
 							// Check whether we must leave a conditional branch
 							if (source.isTopPostdominator(returnStmt)) {
 								source = source.dropTopPostdominator();
 								// Have we dropped the last postdominator for an empty taint?
 								if (source.getAccessPath().isEmpty() && source.getTopPostdominator() == null)
 									return Collections.emptySet();
 							}
 							
 							// Check whether we have reached a sink
 							if (returnStmt.getOp().equals(source.getAccessPath().getPlainValue())
 									&& source.isAbstractionActive()
 									&& sourceSinkManager.isSink(returnStmt, interproceduralCFG())
 									&& source.getAccessPath().isEmpty())
 								results.add(new AbstractionAtSink(source, returnStmt.getOp(), returnStmt));
 
 							return Collections.singleton(source);
 						}
 					};
 				}
 				else if (src instanceof ThrowStmt) {
 					final ThrowStmt throwStmt = (ThrowStmt) src;
 					return new FlowFunction<Abstraction>() {
 
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction source) {
 							if (stopAfterFirstFlow && !results.isEmpty())
 								return Collections.emptySet();
 							
 							// Check whether we must leave a conditional branch
 							if (source.isTopPostdominator(throwStmt)) {
 								source = source.dropTopPostdominator();
 								// Have we dropped the last postdominator for an empty taint?
 								if (source.getAccessPath().isEmpty() && source.getTopPostdominator() == null)
 									return Collections.emptySet();
 							}
 
 							if (throwStmt.getOp().equals(source.getAccessPath().getPlainLocal()))
 								return Collections.singleton(source.deriveNewAbstractionOnThrow(throwStmt));
 							return Collections.singleton(source);
 						}
 					};
 				}
 				// IF statements can lead to implicit flows
 				else if (enableImplicitFlows && (src instanceof IfStmt || src instanceof LookupSwitchStmt
 						|| src instanceof TableSwitchStmt)) {
 					final Value condition = src instanceof IfStmt ? ((IfStmt) src).getCondition()
 							: src instanceof LookupSwitchStmt ? ((LookupSwitchStmt) src).getKey()
 							: ((TableSwitchStmt) src).getKey();
 					return new FlowFunction<Abstraction>() {
 
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction source) {
 							// Check whether we must leave a conditional branch
 							if (source.isTopPostdominator(src)) {
 								source = source.dropTopPostdominator();
 								// Have we dropped the last postdominator for an empty taint?
 								if (source.getAccessPath().isEmpty() && source.getTopPostdominator() == null)
 									return Collections.emptySet();
 							}
 							
 							// If we are in a conditionally-called method, there is no
 							// need to care about further conditionals, since all
 							// assignment targets will be tainted anyway
 							if (source.getAccessPath().isEmpty())
 								return Collections.singleton(source);
 							
 							Set<Abstraction> res = new HashSet<Abstraction>();
 							res.add(source);
 
 							Set<Value> values = new HashSet<Value>();
 							if (condition instanceof Local)
 								values.add(condition);
 							else
 								for (ValueBox box : condition.getUseBoxes())
 									values.add(box.getValue());
 														
 							for (Value val : values)
 								if (val.equals(source.getAccessPath().getPlainValue())) {
 									// ok, we are now in a branch that depends on a secret value.
 									// We now need the postdominator to know when we leave the
 									// branch again.
 									UnitContainer postdom = interproceduralCFG().getPostdominatorOf(src);
 									if (!(postdom.getMethod() == null
 											&& source.getTopPostdominator() != null
 											&& interproceduralCFG().getMethodOf(postdom.getUnit()) == source.getTopPostdominator().getMethod())) {
 										Abstraction newAbs = source.deriveConditionalAbstractionEnter(postdom, (Stmt) src);
 										res.add(newAbs);
 										break;
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
                 if (!dest.isConcrete()){
                     logger.debug("Call skipped because target has no body: {} -> {}", src, dest);
                     return KillAll.v();
                 }
 
 				final Stmt stmt = (Stmt) src;
 				final InvokeExpr ie = stmt.getInvokeExpr();
 				final List<Value> callArgs = ie.getArgs();
 				final List<Value> paramLocals = new ArrayList<Value>();
 				for (int i = 0; i < dest.getParameterCount(); i++) {
 					paramLocals.add(dest.getActiveBody().getParameterLocal(i));
 				}
 				
 				final boolean isSource = sourceSinkManager != null
 						? sourceSinkManager.isSource(stmt, interproceduralCFG()) : false;
 				final boolean isSink = sourceSinkManager != null
 						? sourceSinkManager.isSink(stmt, interproceduralCFG()) : false;
 				
 				final Set<?> fieldsReadByCallee = interproceduralCFG().getReadVariables
 						(interproceduralCFG().getMethodOf(stmt), stmt);
 
 				return new FlowFunction<Abstraction>() {
 
 					@Override
 					public Set<Abstraction> computeTargets(Abstraction source) {
 						if (stopAfterFirstFlow && !results.isEmpty())
 							return Collections.emptySet();
 						if (source.equals(zeroValue)) {
 							return Collections.singleton(source);
 						}
 						if(taintWrapper != null && taintWrapper.isExclusive(stmt, source.getAccessPath())) {
 							//taint is propagated in CallToReturnFunction, so we do not need any taint here:
 							return Collections.emptySet();
 						}
 						
						if (dest.getName().equals("alias"))
							System.out.println("x");
 						
 						//if we do not have to look into sources or sinks:
 						if (!inspectSources && isSource)
 							return Collections.emptySet();
 						if (!inspectSinks && isSink)
 							return Collections.emptySet();
 
 						// Check whether we must leave a conditional branch
 						if (source.isTopPostdominator(stmt)) {
 							source = source.dropTopPostdominator();
 							// Have we dropped the last postdominator for an empty taint?
 							if (source.getAccessPath().isEmpty() && source.getTopPostdominator() == null)
 								return Collections.emptySet();
 						}
 						
 						// If no parameter is tainted, but we are in a conditional, we create a
 						// pseudo abstraction. We do not map parameters if we are handling an
 						// implicit flow anyway.
 						if (source.getAccessPath().isEmpty()) {
 							// Compute the aliases in the callee
 							computeGlobalAliases(dest);
 							Abstraction abs = source.deriveConditionalAbstractionCall(src);
 							return Collections.singleton(abs);
 						}
						else if (source.getTopPostdominator() != null && source.isAbstractionActive())
 								return Collections.emptySet();
 
 						// TODO: use implicit flag
 						/*
 						if ( && !source.getAccessPath().isEmpty())
 							return Collections.emptySet();
 						*/
 
 						// Only propagate the taint if the target field is actually read
 						if (source.getAccessPath().isStaticFieldRef())
 							if (fieldsReadByCallee != null && !isFieldReadByCallee(fieldsReadByCallee, source))
 								return Collections.emptySet();
 						
 						Set<Abstraction> res = new HashSet<Abstraction>();
 						// check if whole object is tainted (happens with strings, for example:)
 						if (!dest.isStatic() && ie instanceof InstanceInvokeExpr) {
 							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
 							// this might be enough because every call must happen with a local variable which is tainted itself:
 							if (vie.getBase().equals(source.getAccessPath().getPlainValue())) {
 								Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
 										(dest.getActiveBody().getThisLocal()), stmt);
 
 								//add new callArgs:
 								assert abs != source; 		// our source abstraction must be immutable
 								res.add(abs);
 							}
 						}
 
 						//special treatment for clinit methods - no param mapping possible
 						if(!dest.getName().equals("<clinit>")) {
 							assert dest.getParameterCount() == callArgs.size();
 							// check if param is tainted:
 							for (int i = 0; i < callArgs.size(); i++) {
 								if (callArgs.get(i).equals(source.getAccessPath().getPlainLocal()) &&
 										(triggerInaktiveTaintOrReverseFlow(callArgs.get(i), source) || source.isAbstractionActive())) {
 									Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
 											(paramLocals.get(i)), stmt);
 
 									assert abs != source;		// our source abstraction must be immutable
 									res.add(abs);
 								}
 							}
 						}
 
 						// staticfieldRefs must be analyzed even if they are not part of the params:
 						if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
 							res.add(source);
 
 						return res;
 					}
 				};
 			}
 
 			@Override
 			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, final Unit retSite) {
 				
 				final ReturnStmt returnStmt = (exitStmt instanceof ReturnStmt) ? (ReturnStmt) exitStmt : null;
 				final boolean isSink = (returnStmt != null && sourceSinkManager != null)
 						? sourceSinkManager.isSink(returnStmt, interproceduralCFG()) : false;
 
 				return new SolverReturnFlowFunction() {
 
 					@Override
 					public Set<Abstraction> computeTargets(Abstraction source, Set<Abstraction> callerD1s) {
 						if (stopAfterFirstFlow && !results.isEmpty())
 							return Collections.emptySet();
 						if (source.equals(zeroValue))
 							return Collections.emptySet();
 
 						boolean callerD1sConditional = false;
 						for (Abstraction d1 : callerD1s)
 							if (d1.getAccessPath().isEmpty()) {
 								callerD1sConditional = true;
 								break;
 							}
 						
						if (callee.getName().equals("staticFieldAccess"))
							System.out.println("x");
						
 						// Activate taint if necessary
 						Abstraction newSource = source;
 						if(!source.isAbstractionActive())
 							if(callSite != null)
 								if (callSite.equals(source.getActivationUnit()) || isCallSiteActivatingTaint(callSite, source.getActivationUnit()))
 									newSource = source.getActiveCopy();
 						
 						// Empty access paths are never propagated over return edges
 						if (source.getAccessPath().isEmpty()) {
 							// If we return a constant, we must taint it
 							if (returnStmt != null && returnStmt.getOp() instanceof Constant)
 								if (callSite instanceof DefinitionStmt) {
 									DefinitionStmt def = (DefinitionStmt) callSite;
 									Abstraction abs = newSource.deriveNewAbstraction
 											(newSource.getAccessPath().copyWithNewValue(def.getLeftOp()), (Stmt) exitStmt);
 									registerActivationCallSite(callSite, abs);
 
 									HashSet<Abstraction> res = new HashSet<Abstraction>();
 									res.add(abs);
 									
 									if(triggerInaktiveTaintOrReverseFlow(def.getLeftOp(), abs) && !callerD1sConditional)
 										for (Abstraction d1 : callerD1s)
 											computeAliasTaints(d1, (Stmt) callSite, def.getLeftOp(), res,
 													interproceduralCFG().getMethodOf(callSite), abs);
 									
 									return res;
 								}
 							
 							return Collections.emptySet();
 						}
 
 						// Are we still inside a conditional? We check this before we
 						// leave the method since the return value is still assigned
 						// inside the method.
 						boolean insideConditional = newSource.getTopPostdominator() != null
 								|| newSource.getAccessPath().isEmpty();
 
 						// Check whether we must leave a conditional branch
 						if (newSource.isTopPostdominator(exitStmt) || newSource.isTopPostdominator(callee)) {
 							newSource = newSource.dropTopPostdominator();
 							// Have we dropped the last postdominator for an empty taint?
 							if (!insideConditional && newSource.getAccessPath().isEmpty() && newSource.getTopPostdominator() == null)
 								return Collections.emptySet();
 						}
 
 						//if abstraction is not active and activeStmt was in this method, it will not get activated = it can be removed:
 						if(!newSource.isAbstractionActive() && newSource.getActivationUnit() != null)
 							if (interproceduralCFG().getMethodOf(newSource.getActivationUnit()).equals(callee))
 								return Collections.emptySet();
 						
 						Set<Abstraction> res = new HashSet<Abstraction>();
 
 						// Check whether this return is treated as a sink
 						if (returnStmt != null) {
 							assert returnStmt.getOp() == null
 									|| returnStmt.getOp() instanceof Local
 									|| returnStmt.getOp() instanceof Constant;
 							
 							boolean mustTaintSink = insideConditional;
 							mustTaintSink |= returnStmt.getOp() != null
 									&& newSource.getAccessPath().isLocal()
 									&& newSource.getAccessPath().getPlainValue().equals(returnStmt.getOp());
 							if (mustTaintSink && isSink
									&& newSource.isAbstractionActive()
									&& newSource.getAccessPath().isEmpty())
 								results.add(new AbstractionAtSink(newSource, returnStmt.getOp(), returnStmt));
 						}
 												
 						// If we have no caller, we have nowhere to propagate. This
 						// can happen when leaving the main method.
 						if (callSite == null)
 							return Collections.emptySet();
 						
 						// if we have a returnStmt we have to look at the returned value:
 						if (returnStmt != null && callSite instanceof DefinitionStmt) {
 							Value retLocal = returnStmt.getOp();
 							DefinitionStmt defnStmt = (DefinitionStmt) callSite;
 							Value leftOp = defnStmt.getLeftOp();
 								
 							if (insideConditional || retLocal.equals(newSource.getAccessPath().getPlainLocal())) {
 								Abstraction abs = newSource.deriveNewAbstraction
 										(newSource.getAccessPath().copyWithNewValue(leftOp), (Stmt) exitStmt);
 								registerActivationCallSite(callSite, abs);
 
 								assert abs != newSource;		// our source abstraction must be immutable
 								res.add(abs);
 								
 								if(triggerInaktiveTaintOrReverseFlow(leftOp, abs) && !callerD1sConditional)
 									for (Abstraction d1 : callerD1s)
 										computeAliasTaints(d1, (Stmt) callSite, leftOp, res,
 												interproceduralCFG().getMethodOf(callSite), abs);
 							}
 						}
 
 						// easy: static
 						if (enableStaticFields && newSource.getAccessPath().isStaticFieldRef()) {
 							// Simply pass on the taint
 							Abstraction abs = newSource;
 							registerActivationCallSite(callSite, abs);
 							res.add(abs);
 
 							for (Abstraction d1 : callerD1s)
 								computeAliasTaints(d1, (Stmt) callSite, null, res,
 										interproceduralCFG().getMethodOf(callSite), abs);
 						}
 						
 						// checks: this/params/fields
 
 						// check one of the call params are tainted (not if simple type)
 						Value sourceBase = newSource.getAccessPath().getPlainLocal();
 						{
 						Value originalCallArg = null;
 						for (int i = 0; i < callee.getParameterCount(); i++) {
 							if (callee.getActiveBody().getParameterLocal(i).equals(sourceBase)) {
 								if (callSite instanceof Stmt) {
 									Stmt iStmt = (Stmt) callSite;
 									originalCallArg = iStmt.getInvokeExpr().getArg(i);
 									
 									// If this is a constant parameter, we can safely ignore it
 									if (!AccessPath.canContainValue(originalCallArg))
 										continue;
 
 									Abstraction abs = newSource.deriveNewAbstraction
 											(newSource.getAccessPath().copyWithNewValue(originalCallArg), (Stmt) exitStmt);
 									registerActivationCallSite(callSite, abs);
 
 									res.add(abs);
 									if (triggerInaktiveTaintOrReverseFlow(originalCallArg, newSource)) {
 										if(triggerInaktiveTaintOrReverseFlow(originalCallArg, abs) && !callerD1sConditional){
 											for (Abstraction d1 : callerD1s)
 												computeAliasTaints(d1, (Stmt) callSite, originalCallArg, res,
 													interproceduralCFG().getMethodOf(callSite), abs);											
 										}
 									}
 								}
 							}
 						}
 						}
 
 						
 						{
 						if (!callee.isStatic()) {
 							Local thisL = callee.getActiveBody().getThisLocal();
 							if (thisL.equals(sourceBase)) {
 								boolean param = false;
 								// check if it is not one of the params (then we have already fixed it)
 								for (int i = 0; i < callee.getParameterCount(); i++) {
 									if (callee.getActiveBody().getParameterLocal(i).equals(sourceBase)) {
 										param = true;
 										break;
 									}
 								}
 								if (!param) {
 									if (callSite instanceof Stmt) {
 										Stmt stmt = (Stmt) callSite;
 										if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
 											InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
 											Abstraction abs = newSource.deriveNewAbstraction
 													(newSource.getAccessPath().copyWithNewValue(iIExpr.getBase()), stmt);
 											registerActivationCallSite(callSite, abs);
 											res.add(abs);
 
 											if(triggerInaktiveTaintOrReverseFlow(iIExpr.getBase(), abs) && !callerD1sConditional){
 												for (Abstraction d1 : callerD1s)
 													computeAliasTaints(d1, (Stmt) callSite, iIExpr.getBase(), res,
 															interproceduralCFG().getMethodOf(callSite), abs);											
 											}
 										}
 									}
 								}
 							}
 							}
 						}
 
 						return res;
 					}
 
 				};
 			}
 
 			@Override
 			public FlowFunction<Abstraction> getCallToReturnFlowFunction(final Unit call, final Unit returnSite) {
 				// special treatment for native methods:
 				if (call instanceof Stmt) {
 					final Stmt iStmt = (Stmt) call;
 					final InvokeExpr invExpr = iStmt.getInvokeExpr();
 					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();
 					
 					final boolean isSource = (sourceSinkManager != null)
 							? sourceSinkManager.isSource(iStmt, interproceduralCFG()) : false;
 					final boolean isSink = (sourceSinkManager != null)
 							? sourceSinkManager.isSink(iStmt, interproceduralCFG()) : false;
 
 					final Set<?> fieldsReadByCallee = interproceduralCFG().getReadVariables
 							(interproceduralCFG().getMethodOf(call), (Stmt) call);
 					final Set<?> fieldsWrittenByCallee = interproceduralCFG().getWriteVariables
 							(interproceduralCFG().getMethodOf(call), (Stmt) call);
 
 					return new SolverCallToReturnFlowFunction() {
 
 						@Override
 						public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source) {
 							if (stopAfterFirstFlow && !results.isEmpty())
 								return Collections.emptySet();
 							Abstraction newSource;
 
 							// Check whether we must leave a conditional branch
 							if (source.isTopPostdominator(iStmt)) {
 								source = source.dropTopPostdominator();
 								// Have we dropped the last postdominator for an empty taint?
 								if (source.getAccessPath().isEmpty() && source.getTopPostdominator() == null)
 									return Collections.emptySet();
 							}
 							
 							//check inactive elements:
 							if (!source.isAbstractionActive() && (call.equals(source.getActivationUnit()))
 									|| isCallSiteActivatingTaint(call, source.getActivationUnit()))
 								newSource = source.getActiveCopy();
 							else
 								newSource = source;
 							
 							Set<Abstraction> res = new HashSet<Abstraction>();
 							res.addAll(computeWrapperTaints(d1, iStmt, newSource));
 							
 							// Implicit flows: taint return value
 							if (call instanceof DefinitionStmt && (newSource.getTopPostdominator() != null
 									|| newSource.getAccessPath().isEmpty())) {
 								Value leftVal = ((DefinitionStmt) call).getLeftOp();
 								Abstraction abs = newSource.deriveNewAbstraction(new AccessPath(leftVal), (Stmt) call);
 								res.add(abs);
 							}
 
 							// We can only pass on a taint if it is neither a parameter nor the
 							// base object of the current call. If this call overwrites the left
 							// side, the taint is never passed on.
 							boolean passOn = !newSource.getAccessPath().isStaticFieldRef()
 									&& !(call instanceof DefinitionStmt && ((DefinitionStmt) call).getLeftOp().equals
 											(newSource.getAccessPath().getPlainLocal()));
 							//we only can remove the taint if we step into the call/return edges
 							//otherwise we will loose taint - see ArrayTests/arrayCopyTest
 							if (passOn && newSource.getAccessPath().isInstanceFieldRef())
 								if (inspectSinks || !isSink)
 									if(hasValidCallees(call) || (taintWrapper != null
 											&& taintWrapper.isExclusive(iStmt, newSource.getAccessPath()))) {
 										if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr)
 											if (((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase().equals
 													(newSource.getAccessPath().getPlainLocal())) {
 												passOn = false;
 											}
 											if (passOn)
 												for (int i = 0; i < callArgs.size(); i++)
 													if (callArgs.get(i).equals(newSource.getAccessPath().getPlainLocal())) {
 														passOn = false;
 														break;
 													}
 											//static variables are always propagated if they are not overwritten. So if we have at least one call/return edge pair,
 											//we can be sure that the value does not get "lost" if we do not pass it on:
 											if(newSource.getAccessPath().isStaticFieldRef())
 												passOn = false;
 										}
 							// If the callee does not read the given value, we also need to pass it on
 							// since we do not propagate it into the callee.
 							if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
 								if (fieldsReadByCallee != null && !isFieldReadByCallee(fieldsReadByCallee, source)
 										&& !isFieldReadByCallee(fieldsWrittenByCallee, source))
 									passOn = true;
 							// Implicit taints are always passed over conditionally called methods
 							passOn |= source.getTopPostdominator() != null || source.getAccessPath().isEmpty();
 							if (passOn)
 								res.add(newSource);
 							
 							if (iStmt.getInvokeExpr().getMethod().isNative())
 								if (callArgs.contains(newSource.getAccessPath().getPlainValue())) {
 									// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
 									Set<Abstraction> nativeAbs = ncHandler.getTaintedValues(iStmt, newSource, callArgs);
 									res.addAll(nativeAbs);
 									
 									// Compute the aliases
 									for (Abstraction abs : nativeAbs)
 										if (abs.getAccessPath().isStaticFieldRef()
 												|| triggerInaktiveTaintOrReverseFlow(abs.getAccessPath().getPlainValue(), abs))
 											computeAliasTaints(d1, (Stmt) call, abs.getAccessPath().getPlainValue(), res,
 													interproceduralCFG().getMethodOf(call), abs);
 								}
 							
 							// Sources can either be assignments like x = getSecret() or
 							// instance method calls like constructor invocations
 							if (source == zeroValue && (iStmt instanceof AssignStmt || invExpr instanceof InstanceInvokeExpr)) {
 								if (isSource) {
 									final Value target;
 									if (iStmt instanceof AssignStmt)
 										target = ((AssignStmt) iStmt).getLeftOp();
 									else {
 										target = ((InstanceInvokeExpr) invExpr).getBase();
 									}
 									
 									final Abstraction abs = new Abstraction(target, iStmt.getInvokeExpr(), iStmt, false, true, iStmt);
 									
 									res.add(abs);
 									res.remove(zeroValue);
 								}
 							}
 							
 							// if we have called a sink we have to store the path from the source - in case one of the params is tainted!
 							if (isSink) {
 								// If we are inside a conditional branch, we consider every sink call a leak
 								boolean taintedParam = (newSource.getTopPostdominator() != null
 											|| newSource.getAccessPath().isEmpty())
 										&& newSource.isAbstractionActive();
 								// If the base object is tainted, we also consider the "code" associated
 								// with the object's class as tainted.
 								if (!taintedParam) {
 									for (int i = 0; i < callArgs.size(); i++) {
 										if (callArgs.get(i).equals(newSource.getAccessPath().getPlainLocal())) {
 											taintedParam = true;
 											break;
 										}
 									}
 								}
 
 								if (newSource.isAbstractionActive() && taintedParam)
 									results.add(new AbstractionAtSink(newSource, iStmt.getInvokeExpr(), iStmt));
 								// if the base object which executes the method is tainted the sink is reached, too.
 								if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
 									InstanceInvokeExpr vie = (InstanceInvokeExpr) iStmt.getInvokeExpr();
 									if (newSource.isAbstractionActive() && vie.getBase().equals(newSource.getAccessPath().getPlainValue()))
 										results.add(new AbstractionAtSink(newSource, iStmt.getInvokeExpr(), iStmt));
 								}
 							}
 							return res;
 						}
 
 						/**
 						 * Checks whether the given call has at least one valid target,
 						 * i.e. a callee with a body.
 						 * @param call The call site to check
 						 * @return True if there is at least one callee implementation
 						 * for the given call, otherwise false
 						 */
 						private boolean hasValidCallees(Unit call) {
 							Set<SootMethod> callees = interproceduralCFG().getCalleesOfCallAt(call);
 							for (SootMethod callee : callees)
 								if (callee.isConcrete())
 										return true;
 							return false;
 						}
 
 
 					};
 				}
 				return Identity.v();
 			}
 		};
 	}
 
 	public InfoflowProblem(List<String> sourceList, List<String> sinkList) {
 		this(new JimpleBasedBiDiICFG(), new DefaultSourceSinkManager(sourceList, sinkList));
 	}
 
 	public InfoflowProblem(ISourceSinkManager sourceSinkManager) {
 		this(new JimpleBasedBiDiICFG(), sourceSinkManager);
 	}
 
 	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, List<String> sourceList, List<String> sinkList) {
 		this(icfg, new DefaultSourceSinkManager(sourceList, sinkList));
 	}
 
 	public InfoflowProblem(ISourceSinkManager mySourceSinkManager, Set<Unit> analysisSeeds) {
 	    this(new JimpleBasedBiDiICFG(), mySourceSinkManager);
 	    for (Unit u : analysisSeeds)
 	    	this.initialSeeds.put(u, Collections.singleton(zeroValue));
     }
 
 	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, ISourceSinkManager sourceSinkManager) {
 		super(icfg);
 		this.sourceSinkManager = sourceSinkManager;
 	}
 
 	public void setBackwardSolver(InfoflowSolver backwardSolver){
 		bSolver = backwardSolver;
 	}
 
 	@Override
 	public boolean autoAddZero() {
 		return false;
 	}
 
 	/**
 	 * Gets the results of the data flow analysis
 	 * @return The paths between sources and sinks found by the data flow
 	 * analysis
 	 */
     public InfoflowResults getResults(){
     	if (this.infoflowResults != null)
     		return this.infoflowResults;
     	
     	logger.debug("Running path reconstruction");
     	InfoflowResults results = new InfoflowResults();
     	for (AbstractionAtSink abs : this.results)
     		for (SourceContextAndPath context : abs.getAbstraction().getPaths())
 				results.addResult(abs.getSinkValue(), abs.getSinkStmt(),
 						context.getValue(), context.getStmt(),
 						context.getPath(), abs.getSinkStmt());
     	logger.debug("Path reconstruction done.");
     	
     	this.infoflowResults = results;
 	    return results;
 	}
 
 }
 
