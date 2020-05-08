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
 package soot.jimple.infoflow.data;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Stack;
 
 import soot.SootField;
 import soot.SootMethod;
 import soot.Unit;
 import soot.Value;
 import soot.jimple.Stmt;
 import soot.jimple.infoflow.Infoflow;
 import soot.jimple.infoflow.heros.InfoflowCFG.UnitContainer;
 /**
  * the abstraction class contains all information that is necessary to track the taint.
  *
  */
 public class Abstraction implements Cloneable {
 	/**
 	 * the access path contains the currently tainted variable or field
 	 */
 	private final AccessPath accessPath;
 	private final Value source;
 	/**
 	 * the statement which contains the source
 	 */
 	private final Stmt sourceContext;
 	/**
 	 * Unit/Stmt which activates the taint when the abstraction passes it
 	 */
 	private Unit activationUnit;
 	/**
 	 * Unit/Stmt which activates the taint when the abstraction passes it,
 	 * adapts to the current level
 	 */
 	private final List<Unit> activationUnitOnCurrentLevel = new ArrayList<Unit>();
 	/**
 	 * active abstraction is tainted value,
 	 * inactive abstraction is an alias to a tainted value that
 	 * might be activated in the future
 	 */
 	private boolean isActive;
 	/**
 	 * taint is thrown by an exception (is set to false when it reaches the catch-Stmt)
 	 */
 	private boolean exceptionThrown;
 	private int hashCode;
 
 	/**
 	 * The postdominators we need to pass in order to leave the current conditional
 	 * branch
 	 */
 	private final Stack<UnitContainer> postdominators = new Stack<UnitContainer>();
 	/**
 	 * The conditional call site. If this site is set, the current code is running
 	 * inside a conditionally-called method.
 	 */
 	private Unit conditionalCallSite = null;
 
 	public Abstraction(Value taint, Value src, Stmt srcContext, boolean exceptionThrown, boolean isActive, Unit activationUnit){
 		this.source = src;
 		this.accessPath = new AccessPath(taint);
 		this.activationUnit = activationUnit;
 		this.sourceContext = srcContext;
 		this.exceptionThrown = exceptionThrown;
 		this.isActive = isActive;
 	}
 		
 	protected Abstraction(AccessPath p, Value src, Stmt srcContext, boolean exceptionThrown, boolean isActive){
 		this.source = src;
 		this.sourceContext = srcContext;
 		this.accessPath = p.clone();
 		this.activationUnit = null;
 		this.exceptionThrown = exceptionThrown;
 		this.isActive = isActive;
 	}
 
 	/**
 	 * Creates an abstraction as a copy of an existing abstraction,
 	 * only exchanging the access path.
 	 * @param p The value to be used as the new access path
 	 * @param original The original abstraction to copy
 	 */
 	protected Abstraction(Value p, Abstraction original){
 		this(new AccessPath(p), original);
 	}
 
 	/**
 	 * Creates an abstraction as a copy of an existing abstraction,
 	 * only exchanging the access path. -> only used by AbstractionWithPath
 	 * @param p The access path for the new abstraction
 	 * @param original The original abstraction to copy
 	 */
 	protected Abstraction(AccessPath p, Abstraction original){
 		if (original == null) {
 			source = null;
 			sourceContext = null;
 			exceptionThrown = false;
 			activationUnit = null;
 			conditionalCallSite = null;
 		}
 		else {
 			source = original.source;
 			sourceContext = original.sourceContext;
 			exceptionThrown = original.exceptionThrown;
 			activationUnit = original.activationUnit;
 			activationUnitOnCurrentLevel.addAll(original.activationUnitOnCurrentLevel);
 			isActive = original.isActive;
 			postdominators.addAll(original.postdominators);
 			assert this.postdominators.equals(original.postdominators);
 			conditionalCallSite = original.conditionalCallSite;
 		}
 		accessPath = p;
 	}
 	
 	public final Abstraction deriveInactiveAbstraction(){
 		return deriveInactiveAbstraction(accessPath);
 	}
 	
 	public final Abstraction deriveInactiveAbstraction(AccessPath p){
 		Abstraction a = deriveNewAbstraction(p);
 		a.isActive = false;
 		return a;
 	}
 
 	public Abstraction deriveNewAbstraction(AccessPath p){
 		return new Abstraction(p, this);
 	}
	
	public final Abstraction deriveNewAbstraction(AccessPath p, Unit newActUnit){
		Abstraction a = deriveNewAbstraction(p);
		/*
		if(isActive && !accessPath.isEmpty() && conditionalCallSite == null) {
			a.activationUnitOnCurrentLevel.add(newActUnit);
			if (activationUnitOnCurrentLevel.size() > Infoflow.getAbstractionDepth())
				a.activationUnitOnCurrentLevel.remove(0);
		}
		*/
		return a;
	}
		
 	public final Abstraction deriveNewAbstraction(Value taint, Unit activationUnit){
 		return this.deriveNewAbstraction(taint, false, activationUnit);
 	}
 	
 	public final Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit){
 		Abstraction a;
 		SootField[] orgFields = accessPath.getFields();
 		SootField[] fields = new SootField[cutFirstField ? orgFields.length - 1 : orgFields.length];
 		for (int i = cutFirstField ? 1 : 0; i < orgFields.length; i++)
 			fields[cutFirstField ? i - 1 : i] = orgFields[i];
 		a = deriveNewAbstraction(new AccessPath(taint, fields));
 		if (isActive)
 			a.activationUnit = newActUnit;
 		return a;
 	}
 
 	/**
 	 * Derives a new abstraction that models the current local being thrown as
 	 * an exception
 	 * @return The newly derived abstraction
 	 */
 	public final Abstraction deriveNewAbstractionOnThrow(){
 		assert !this.exceptionThrown;
 		Abstraction abs = clone();
 		abs.exceptionThrown = true;
 		return abs;
 	}
 	
 	/**
 	 * Derives a new abstraction that models the current local being caught as
 	 * an exception
 	 * @param taint The value in which the tainted exception is stored
 	 * @return The newly derived abstraction
 	 */
 	public final Abstraction deriveNewAbstractionOnCatch(Value taint, Unit newActivationUnit){
 		assert this.exceptionThrown;
 		Abstraction abs = deriveNewAbstraction(new AccessPath(taint));
 		abs.exceptionThrown = false;
 		if(isActive)
 			abs.activationUnit = newActivationUnit;
 		return abs;
 	}
 
 	public Value getSource() {
 		return source;
 	}
 
 	public Stmt getSourceContext() {
 		return this.sourceContext;
 	}
 	
 	public boolean isAbstractionActive(){
 		return isActive;
 	}
 	
 	@Override
 	public String toString(){
 		return (isActive?"":"_")+accessPath.toString() + " | "+(activationUnit==null?"":activationUnit.toString()) + ">>"+ (activationUnitOnCurrentLevel==null?"":activationUnitOnCurrentLevel.toString());
 	}
 	
 	public AccessPath getAccessPath(){
 		return accessPath;
 	}
 	
 	public Unit getActivationUnit(){
 		return activationUnit;
 	}
 	
 	public Abstraction getAbstractionWithNewActivationUnitOnCurrentLevel(Unit u){
 		if (!isActive || this.getAccessPath().isEmpty() || this.conditionalCallSite != null)
 			return this;
 		Abstraction a = this.clone();
 		a.activationUnitOnCurrentLevel.add(u);
 		if (activationUnitOnCurrentLevel.size() > Infoflow.getAbstractionDepth())
 			a.activationUnitOnCurrentLevel.remove(0);
 		return a;
 	}
 	
 	public List<Unit> getActivationUnitOnCurrentLevel(){
 		return Collections.unmodifiableList(activationUnitOnCurrentLevel);
 	}
 		
 	public Abstraction getActiveCopy(){
 		Abstraction a = clone();
 		a.isActive = true;
 		// do not kill the original activation point since we might return into
 		// a caller, find a new alias there and then need to know where both
 		// aliases originally became active.
 //		a.activationUnit = null;
 //		a.activationUnitOnCurrentLevel.clear();
 		return a;
 	}
 	
 	public Abstraction removeActivationUnit(Unit callSite) {
 		if (activationUnitOnCurrentLevel.contains(callSite)) {
 			Abstraction a = clone();
 			a.activationUnitOnCurrentLevel.remove(callSite);
 			return a;
 		}
 		else
 			return this;
 	}
 	
 	public Abstraction clearActivationUnits() {
 		if (activationUnitOnCurrentLevel.isEmpty())
 			return this;
 		else {
 			Abstraction a = clone();
 			a.activationUnitOnCurrentLevel.clear();
 			return a;
 		}
 	}
 
 	/**
 	 * Gets whether this value has been thrown as an exception
 	 * @return True if this value has been thrown as an exception, otherwise
 	 * false
 	 */
 	public boolean getExceptionThrown() {
 		return this.exceptionThrown;
 	}
 	
 	public final Abstraction deriveConditionalAbstractionEnter(UnitContainer postdom) {
 		if (!postdominators.isEmpty() && postdominators.contains(postdom))
 			return this;
 		
 		Abstraction abs = clone();
 		abs.postdominators.push(postdom);
 		return abs;
 	}
 	
 	public final Abstraction deriveConditionalAbstractionCall(Unit conditionalCallSite) {
 		Abstraction abs = deriveNewAbstraction(AccessPath.getEmptyAccessPath());
 		if (abs.conditionalCallSite == null)
 			abs.conditionalCallSite = conditionalCallSite;
 		abs.activationUnit = null;
 		abs.activationUnitOnCurrentLevel.clear();
 		return abs;
 	}
 	
 	public final Abstraction leaveConditionalCall() {
 		Abstraction abs = clone();
 		abs.conditionalCallSite = null;
 		return abs;
 	}
 
 	public final Abstraction dropTopPostdominator() {
 		if (postdominators.isEmpty())
 			return this;
 		
 		Abstraction abs = clone();
 		abs.postdominators.pop();
 		return abs;
 	}
 	
 	public UnitContainer getTopPostdominator() {
 		if (this.postdominators.isEmpty())
 			return null;
 		return this.postdominators.peek();
 	}
 	
 	public boolean isTopPostdominator(Unit u) {
 		UnitContainer uc = getTopPostdominator();
 		if (uc == null)
 			return false;
 		return uc.getUnit() == u;
 	}
 
 	public boolean isTopPostdominator(SootMethod sm) {
 		UnitContainer uc = getTopPostdominator();
 		if (uc == null)
 			return false;
 		return uc.getMethod() == sm;
 	}
 	
 	public Unit getConditionalCallSite() {
 		return this.conditionalCallSite;
 	}
 
 	@Override
 	public Abstraction clone() {
 		Abstraction abs = new Abstraction(accessPath, this);
 		assert abs.equals(this);
 		return abs;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (super.equals(obj))
 			return true;
 		if (obj == null || !(obj instanceof Abstraction))
 			return false;
 		Abstraction other = (Abstraction) obj;
 		if (accessPath == null) {
 			if (other.accessPath != null)
 				return false;
 		} else if (!accessPath.equals(other.accessPath))
 			return false;
 		if (source == null) {
 			if (other.source != null)
 				return false;
 		} else if (!source.equals(other.source))
 			return false;
 		if (sourceContext == null) {
 			if (other.sourceContext != null)
 				return false;
 		} else if (!sourceContext.equals(other.sourceContext))
 			return false;
 		if (activationUnit == null) {
 			if (other.activationUnit != null)
 				return false;
 		} else if (!activationUnit.equals(other.activationUnit))
 			return false;
 		if (activationUnitOnCurrentLevel == null) {
 			if (other.activationUnitOnCurrentLevel != null)
 				return false;
 		} else if (!activationUnitOnCurrentLevel.equals(other.activationUnitOnCurrentLevel))
 			return false;
 		if (this.exceptionThrown != other.exceptionThrown)
 			return false;
 		if(this.isActive != other.isActive)
 			return false;
 		if(!this.postdominators.equals(other.postdominators))
 			return false;
 		if (conditionalCallSite == null) {
 			if (other.conditionalCallSite != null)
 				return false;
 		} else if (!conditionalCallSite.equals(other.conditionalCallSite))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		if (this.hashCode == 0) {
 			this.hashCode = 1;
 			this.hashCode = prime * this.hashCode + ((accessPath == null) ? 0 : accessPath.hashCode());
 			this.hashCode = prime * this.hashCode + ((source == null) ? 0 : source.hashCode());
 			this.hashCode = prime * this.hashCode + ((sourceContext == null) ? 0 : sourceContext.hashCode());
 			this.hashCode = prime * this.hashCode + ((activationUnit == null) ? 0 : activationUnit.hashCode());
 			this.hashCode = prime * this.hashCode + ((activationUnitOnCurrentLevel == null) ? 0 : activationUnitOnCurrentLevel.hashCode());
 			this.hashCode = prime * this.hashCode + (exceptionThrown ? 1231 : 1237);
 			this.hashCode = prime * this.hashCode + (isActive ? 1231 : 1237);
 			this.hashCode = prime * this.hashCode + postdominators.hashCode();
 			this.hashCode = prime * this.hashCode + ((conditionalCallSite == null) ? 0 : conditionalCallSite.hashCode());
 		}
 		return hashCode;
 	}
 		
 }
