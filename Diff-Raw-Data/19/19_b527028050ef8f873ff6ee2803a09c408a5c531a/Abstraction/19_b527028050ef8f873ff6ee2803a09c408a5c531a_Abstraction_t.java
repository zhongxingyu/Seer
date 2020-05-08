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
 
 
 import heros.solver.LinkedNode;
 
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import soot.NullType;
 import soot.SootField;
 import soot.SootMethod;
 import soot.Type;
 import soot.Unit;
 import soot.Value;
 import soot.jimple.Stmt;
 import soot.jimple.infoflow.solver.IInfoflowCFG.UnitContainer;
 import soot.jimple.internal.JimpleLocal;
 
 import com.google.common.collect.Sets;
 /**
  * the abstraction class contains all information that is necessary to track the taint.
  *
  */
 public class Abstraction implements Cloneable, LinkedNode<Abstraction> {
 
 	private static Abstraction zeroValue = null;
     
 	/**
 	 * Class representing a source value together with the statement that created it
 	 * 
 	 * @author Steven Arzt
 	 */
 	public class SourceContext implements Cloneable {
 		private final Value value;
 		private final Stmt stmt;
 		
 		public SourceContext(Value value, Stmt stmt) {
 			this.value = value;
 			this.stmt = stmt;
 		}
 		
 		public Value getValue() {
 			return this.value;
 		}
 		
 		public Stmt getStmt() {
 			return this.stmt;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
 			result = prime * result + ((value == null) ? 0 : value.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null || !(obj instanceof SourceContext))
 				return false;
 			SourceContext other = (SourceContext) obj;
 			if (stmt == null) {
 				if (other.stmt != null)
 					return false;
 			} else if (!stmt.equals(other.stmt))
 				return false;
 			if (value == null) {
 				if (other.value != null)
 					return false;
 			} else if (!value.equals(other.value))
 				return false;
 			return true;
 		}
 		
 		@Override
 		public SourceContext clone() {
 			SourceContext sc = new SourceContext(value, stmt);
 			assert sc.equals(this);
 			return sc;
 		}
 
 		@Override
 		public String toString() {
 			return value.toString();
 		}
 	}
 	
 	public class SourceContextAndPath extends SourceContext implements Cloneable {
 		private final List<Stmt> path = new LinkedList<Stmt>();
 		
 		public SourceContextAndPath(Value value, Stmt stmt) {
 			super(value, stmt);
 		}
 		
 		public List<Stmt> getPath() {
 			return Collections.unmodifiableList(this.path);
 		}
 		
 		public SourceContextAndPath extendPath(Stmt s) {
 			SourceContextAndPath scap = clone();
 			scap.path.add(s);
 			return scap;
 		}
 		
 		@Override
 		public boolean equals(Object other) {
 			if (this == other)
 				return true;
 			if (other == null || !(other instanceof SourceContextAndPath))
 				return false;
 			if (!super.equals(other))
 				return false;
 			//SourceContextAndPath scap = (SourceContextAndPath) other;
 			return true; //this.path.equals(scap.path);
 		}
 		
 		@Override
 		public int hashCode() {
 			return 31 * super.hashCode(); // + 7 * path.hashCode();
 		}
 		
 		@Override
 		public SourceContextAndPath clone() {
 			SourceContextAndPath scap = new SourceContextAndPath(getValue(), getStmt());
 			scap.path.addAll(this.path);
 			assert scap.equals(this);
 			return scap;
 		}
 		
 		@Override
 		public String toString() {
 			return super.toString() + "\n\ton Path: " + path;
 		}	
 	}
 	
 	/**
 	 * the access path contains the currently tainted variable or field
 	 */
 	private final AccessPath accessPath;
 	
 	private Abstraction predecessor = null;
 	private Set<Abstraction> neighbors = null;
 	private Stmt currentStmt = null;
 	
 	private SourceContext sourceContext = null;
 
 	// only used in path generation
 	private Set<SourceContextAndPath> pathCache = null;
 	
 	/**
 	 * Unit/Stmt which activates the taint when the abstraction passes it
 	 */
 	private Unit activationUnit = null;
 	/**
 	 * active abstraction is tainted value,
 	 * inactive abstraction is an alias to a tainted value that
 	 * might be activated in the future
 	 */
 	private boolean isActive = true;
 	/**
 	 * taint is thrown by an exception (is set to false when it reaches the catch-Stmt)
 	 */
 	private boolean exceptionThrown = false;
 	private int hashCode = 0;
 
 	/**
 	 * The postdominators we need to pass in order to leave the current conditional
 	 * branch. Do not use the synchronized Stack class here to avoid deadlocks.
 	 */
 	private List<UnitContainer> postdominators = null;
 	
 	/**
 	 * Only valid for inactive abstractions. Specifies whether an access paths
 	 * has been cut during alias analysis.
 	 */
 	private boolean dependsOnCutAP = false;
 	
 	private final boolean flowSensitiveAliasing;
 	
 	public Abstraction(Value taint, boolean taintSubFields,
 			Value sourceVal, Stmt sourceStmt,
 			boolean exceptionThrown,
 			boolean isActive, Unit activationUnit,
 			boolean flowSensitiveAliasing){
 		this.sourceContext = new SourceContext(sourceVal, sourceStmt);
 		this.accessPath = new AccessPath(taint, taintSubFields);
 		
 		if (flowSensitiveAliasing)
 			this.activationUnit = activationUnit;
 		else
 			this.activationUnit = null;
 		
 		this.exceptionThrown = exceptionThrown;
 		
 		if (flowSensitiveAliasing)
 			this.isActive = isActive;
 		else
 			this.isActive = true;
 		
 		this.flowSensitiveAliasing = flowSensitiveAliasing;
 		this.neighbors = null;
 	}
 
 	/**
 	 * Creates an abstraction as a copy of an existing abstraction,
 	 * only exchanging the access path. -> only used by AbstractionWithPath
 	 * @param p The access path for the new abstraction
 	 * @param original The original abstraction to copy
 	 */
 	protected Abstraction(AccessPath p, Abstraction original){
 		if (original == null) {
 			sourceContext = null;
 			exceptionThrown = false;
 			activationUnit = null;
 			flowSensitiveAliasing = true;
 		}
 		else {
 			sourceContext = original.sourceContext;
 			exceptionThrown = original.exceptionThrown;
 			activationUnit = original.activationUnit;
 			
 			isActive = original.isActive;
 			
 			postdominators = original.postdominators == null ? null
 					: new ArrayList<UnitContainer>(original.postdominators);
 			
 			flowSensitiveAliasing = original.flowSensitiveAliasing;
 			assert flowSensitiveAliasing || this.activationUnit == null;
 			assert this.isActive || flowSensitiveAliasing;
 			
 			dependsOnCutAP = original.dependsOnCutAP;
 		}
 		accessPath = p;
 		neighbors = null;
 	}
 	
 	public final Abstraction deriveInactiveAbstraction(){
 		if (!flowSensitiveAliasing)
 			return this;
 		
 		// If this abstraction is already inactive, we keep it
 		if (!this.isActive)
 			return this;
 
 		Abstraction a = deriveNewAbstractionMutable(accessPath, null);
 		a.isActive = false;
 		a.postdominators = null;
 		return a;
 	}
 
 	public Abstraction deriveNewAbstraction(AccessPath p, Stmt currentStmt){
 		return deriveNewAbstraction(p, currentStmt, null);
 	}
 
 	public Abstraction deriveNewAbstraction(AccessPath p, Stmt currentStmt, Stmt activationUnit){
 		if (this.accessPath.equals(p) && this.currentStmt == currentStmt)
 			return this;
 		Abstraction abs = deriveNewAbstractionMutable(p, currentStmt);
 		if (abs.isActive && flowSensitiveAliasing && activationUnit != null)
 			abs.activationUnit = activationUnit;
 		return abs;
 	}
 	
 	private Abstraction deriveNewAbstractionMutable(AccessPath p, Stmt currentStmt){
 		if (this.accessPath.equals(p) && this.currentStmt == currentStmt)
 			return clone();
 		
 		Abstraction abs = new Abstraction(p, this);
 		abs.predecessor = this;
 		abs.currentStmt = currentStmt;
 		
 		if (!abs.getAccessPath().isEmpty())
 			abs.postdominators = null;
 		if (!abs.isActive)
 			abs.dependsOnCutAP = abs.dependsOnCutAP || p.isCutOffApproximation();
 		
 		abs.sourceContext = null;
 		return abs;
 	}
 	
 	public final Abstraction deriveNewAbstraction(Value taint, Unit activationUnit){
 		return this.deriveNewAbstraction(taint, false, activationUnit, null);
 	}
 	
 	public final Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit,
 			Type baseType){
 		assert !this.getAccessPath().isEmpty();
 
 		Abstraction a;
 		SootField[] orgFields = accessPath.getFields();
 		SootField[] fields = null;
 
 		if (orgFields != null) {
 			fields = new SootField[cutFirstField ? orgFields.length - 1 : orgFields.length];
 			for (int i = cutFirstField ? 1 : 0; i < orgFields.length; i++)
 				fields[cutFirstField ? i - 1 : i] = orgFields[i];
 		}
 
 		Type[] orgTypes = accessPath.getFieldTypes();
 		Type[] types = null;
 		
 		if (orgTypes != null) {
 			types = new Type[cutFirstField ? orgTypes.length - 1 : orgTypes.length];
 			for (int i = cutFirstField ? 1 : 0; i < orgTypes.length; i++)
 				types[cutFirstField ? i - 1 : i] = orgTypes[i];
 		}
 		
 		if (cutFirstField)
 			baseType = accessPath.getFirstFieldType();
 		
 		AccessPath newAP = new AccessPath(taint, fields, baseType, types,
 				accessPath.getTaintSubFields());
 		
 		a = deriveNewAbstractionMutable(newAP, (Stmt) newActUnit);
 		if (flowSensitiveAliasing && isActive) {
 			assert newActUnit != null;
 			if (!this.getAccessPath().isEmpty())
 				a.activationUnit = newActUnit;
 		}
 		return a;
 	}
 
 	/**
 	 * Derives a new abstraction that models the current local being thrown as
 	 * an exception
 	 * @param throwStmt The statement at which the exception was thrown
 	 * @return The newly derived abstraction
 	 */
 	public final Abstraction deriveNewAbstractionOnThrow(Stmt throwStmt){
 		assert !this.exceptionThrown;
 		Abstraction abs = cloneWithPredecessor(throwStmt);
 		
 		abs.sourceContext = null;
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
 		Abstraction abs = deriveNewAbstractionMutable(new AccessPath(taint, true),
 				(Stmt) newActivationUnit);
 		abs.exceptionThrown = false;
 		
 		if(flowSensitiveAliasing && isActive) {
 			assert newActivationUnit != null;
 			abs.activationUnit = newActivationUnit;
 		}
 		return abs;
 	}
 		
 	/**
 	 * Gets the path of statements from the source to the current statement
 	 * with which this abstraction is associated. If this path is ambiguous,
 	 * a single path is selected randomly.
 	 * @return The path from the source to the current statement
 	 */
 	public Set<SourceContextAndPath> getPaths() {
 		Runtime.getRuntime().gc();
 		return getPaths(true, this);
 	}
 	
 	/**
 	 * Gets the path of statements from the source to the current statement
 	 * with which this abstraction is associated. If this path is ambiguous,
 	 * a single path is selected randomly.
 	 * @return The path from the source to the current statement
 	 */
 	public Set<SourceContextAndPath> getSources() {
 		Runtime.getRuntime().gc();
 		return getPaths(false, this);
 	}
 	
 	private Abstraction sinkAbs = null;
 	
 	/**
 	 * Gets the path of statements from the source to the current statement
 	 * with which this abstraction is associated. If this path is ambiguous,
 	 * a single path is selected randomly.
 	 * @return The path from the source to the current statement
 	 */
 	private Set<SourceContextAndPath> getPaths(boolean reconstructPaths, Abstraction flagAbs) {
 		if (pathCache != null && sinkAbs == flagAbs)
 			return Collections.unmodifiableSet(pathCache);
 
 		if (sinkAbs == flagAbs)
 			return Collections.emptySet();
 		sinkAbs = flagAbs;
 		
 		if (sourceContext != null) {
 			// Construct the path root
 			SourceContextAndPath sourceAndPath = new SourceContextAndPath
 					(sourceContext.value, sourceContext.stmt).extendPath
 					(sourceContext.stmt);
 			pathCache = Collections.singleton(sourceAndPath);
 			
 			// Sources may not have neighbors
 			assert neighbors == null;
 			assert predecessor == null;
 		}
 		else {
 			this.pathCache = Sets.newHashSet();
 			
 			for (SourceContextAndPath curScap : predecessor.getPaths(reconstructPaths, flagAbs)) {
 				SourceContextAndPath extendedPath = (currentStmt == null || !reconstructPaths)
 						? curScap : curScap.extendPath(currentStmt);
 				pathCache.add(extendedPath);
 			}
 			
 			if (neighbors != null)
 				for (Abstraction nb : neighbors)
 					pathCache.addAll(nb.getPaths(reconstructPaths, flagAbs));
 		}
 		
 		assert pathCache != null;
 		return Collections.unmodifiableSet(pathCache);
 	}
 	
 	public boolean isAbstractionActive(){
 		return isActive;
 	}
 	
 	@Override
 	public String toString(){
 		return (isActive?"":"_")+accessPath.toString() + " | "+(activationUnit==null?"":activationUnit.toString()) + ">>";
 	}
 	
 	public AccessPath getAccessPath(){
 		return accessPath;
 	}
 	
 	public Unit getActivationUnit(){
 		return activationUnit;
 	}
 	
 	public Abstraction getActiveCopy(){
 		assert !this.isActive;
 		
 		Abstraction a = cloneWithPredecessor(null);
 		a.sourceContext = null;
 		a.isActive = true;
 		// do not kill the original activation point since we might return into
 		// a caller, find a new alias there and then need to know where both
 		// aliases originally became active.
 //		a.activationUnit = null;
 		return a;
 	}
 	
 	/**
 	 * Gets whether this value has been thrown as an exception
 	 * @return True if this value has been thrown as an exception, otherwise
 	 * false
 	 */
 	public boolean getExceptionThrown() {
 		return this.exceptionThrown;
 	}
 	
 	public final Abstraction deriveConditionalAbstractionEnter(UnitContainer postdom,
 			Stmt conditionalUnit) {
 		if (postdominators != null && postdominators.contains(postdom))
 			return this;
 		
 		Abstraction abs = deriveNewAbstractionMutable
 				(AccessPath.getEmptyAccessPath(), conditionalUnit);
 		// TODO
 //		abs.activationUnit = null;
 		if (abs.postdominators == null)
 			abs.postdominators = Collections.singletonList(postdom);
 		else
 			abs.postdominators.add(0, postdom);
 		abs.isActive = true;
 		return abs;
 	}
 	
 	public final Abstraction deriveConditionalAbstractionCall(Unit conditionalCallSite) {
 		assert conditionalCallSite != null;
 		
 		Abstraction abs = deriveNewAbstractionMutable
 				(AccessPath.getEmptyAccessPath(), (Stmt) conditionalCallSite);
 		abs.isActive = true;
 		abs.activationUnit = conditionalCallSite;
 		
 		// Postdominators are only kept intraprocedurally in order to not
 		// mess up the summary functions with caller-side information
 		abs.postdominators = null;
 
 		return abs;
 	}
 	
 	public final Abstraction dropTopPostdominator() {
 		if (postdominators == null || postdominators.isEmpty())
 			return this;
 		
 		Abstraction abs = clone();
 		abs.sourceContext = null;
 		abs.postdominators.remove(0);
 		return abs;
 	}
 	
 	public UnitContainer getTopPostdominator() {
 		if (postdominators == null || postdominators.isEmpty())
 			return null;
 		return this.postdominators.get(0);
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
 	
 	@Override
 	public Abstraction clone() {
 		return cloneWithPredecessor(null);
 	}
 	
 	private Abstraction cloneWithPredecessor(Stmt predStmt) {
 		Abstraction abs = new Abstraction(accessPath, this);
 		abs.predecessor = this;
 		abs.currentStmt = predStmt;
 		abs.neighbors = null;
 		
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
 		
 		return localEquals(other);
 	}
 	
 	/**
 	 * Checks whether this object locally equals the given object, i.e. the both
 	 * are equal modulo the access path
 	 * @param other The object to compare this object with
 	 * @return True if this object is locally equal to the given one, otherwise
 	 * false
 	 */
 	private boolean localEquals(Abstraction other) {
 		// deliberately ignore prevAbs
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
 		if (this.exceptionThrown != other.exceptionThrown)
 			return false;
 		if(this.isActive != other.isActive)
 			return false;
 		if (postdominators == null) {
 			if (other.postdominators != null)
 				return false;
 		} else if (!postdominators.equals(other.postdominators))
 			return false;
 		if(this.flowSensitiveAliasing != other.flowSensitiveAliasing)
 			return false;
 		if(this.dependsOnCutAP != other.dependsOnCutAP)
 			return false;
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		if (this.hashCode != 0)
 			return hashCode;
 
 		final int prime = 31;
 		synchronized (this) { 
 			this.hashCode = 1;
 	
 			// deliberately ignore prevAbs
 			this.hashCode = prime * this.hashCode + ((sourceContext == null) ? 0 : sourceContext.hashCode());
 			this.hashCode = prime * this.hashCode + ((accessPath == null) ? 0 : accessPath.hashCode());
 			this.hashCode = prime * this.hashCode + ((activationUnit == null) ? 0 : activationUnit.hashCode());
 			this.hashCode = prime * this.hashCode + (exceptionThrown ? 1231 : 1237);
 			this.hashCode = prime * this.hashCode + (isActive ? 1231 : 1237);
 			this.hashCode = prime * this.hashCode + ((postdominators == null) ? 0 : postdominators.hashCode());
 			this.hashCode = prime * this.hashCode + (flowSensitiveAliasing ? 1231 : 1237);
 			this.hashCode = prime * this.hashCode + (dependsOnCutAP ? 1231 : 1237);
 			return hashCode;
 		}
 	}
 	
 	/**
 	 * Checks whether this abstraction entails the given abstraction, i.e. this
 	 * taint also taints everything that is tainted by the given taint.
 	 * @param other The other taint abstraction
 	 * @return True if this object at least taints everything that is also tainted
 	 * by the given object
 	 */
 	public boolean entails(Abstraction other) {
 		if (accessPath == null) {
 			if (other.accessPath != null)
 				return false;
 		} else if (!accessPath.entails(other.accessPath))
 			return false;
 		return localEquals(other);
 	}
 
 	/**
 	 * Gets the context of the taint, i.e. the statement and value of the source
 	 * @return The statement and value of the source
 	 */
 	public SourceContext getSourceContext() {
 		return sourceContext;
 	}
 	
 	public boolean dependsOnCutAP() {
 		return dependsOnCutAP;
 	}
 	
 	@Override
 	public void addNeighbor(Abstraction originalAbstraction) {
 		assert this != zeroValue;
 		assert originalAbstraction.equals(this);
 		
 		// We should not register ourselves as a neighbor
 		if (originalAbstraction == this)
 			return;
 		
		Set<Abstraction> orgNeighbors = null;
		synchronized (originalAbstraction) {
			if (originalAbstraction.neighbors != null) {
				orgNeighbors = new HashSet<Abstraction>(originalAbstraction.neighbors);
				originalAbstraction.neighbors = null;
			}
		}

 		synchronized (this) {
 			if (neighbors == null)
 				neighbors = Sets.newIdentityHashSet();
			if (orgNeighbors != null)
				neighbors.addAll(orgNeighbors);
 			
 			if (this.predecessor != originalAbstraction.predecessor
 					|| this.currentStmt != originalAbstraction.currentStmt)
 				this.neighbors.add(originalAbstraction);
 		}
 	}
 		
 	public static Abstraction getZeroAbstraction(boolean flowSensitiveAliasing) {
 		if (zeroValue == null)
 			zeroValue = new Abstraction(new JimpleLocal("zero", NullType.v()), false, null,
 					null, false, true, null, flowSensitiveAliasing);
 		return zeroValue;
 	}
 	
 }
