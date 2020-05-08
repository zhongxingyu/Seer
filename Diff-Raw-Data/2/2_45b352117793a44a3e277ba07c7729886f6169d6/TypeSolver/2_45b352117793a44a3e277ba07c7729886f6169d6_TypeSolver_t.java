 package com.rx201.dx.translator;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 
 import com.rx201.dx.translator.RopType.Category;
 
 public class TypeSolver {
 
     enum CascadeType {Equivalent, ArrayToElement, ElementToArray};
 
     private class TypeInfo {
         HashSet<AnalyzedDexInstruction> definedSites;
         HashSet<TypeSolver> unifiedSet;
         HashSet<RopType> constraints;
         RopType type;
         boolean freezed;
         HashMap<TypeSolver, CascadeType> depends;
 
         public TypeInfo() {
             freezed = false;
             type = RopType.Unknown;
             definedSites = new HashSet<AnalyzedDexInstruction>();
             unifiedSet = new HashSet<TypeSolver>();
             constraints = new HashSet<RopType>();
             depends = new HashMap<TypeSolver, CascadeType>();
 
             unifiedSet.add(TypeSolver.this);
         }
     };
 
     TypeInfo info;
 
     public TypeSolver(AnalyzedDexInstruction site) {
         info = new TypeInfo();
         info.definedSites.add(site);
     }
 
     private boolean areEquivalent(TypeSolver other, Set<TypeSolver> visited) {
     	visited.add(other);
     	
         if (other.info == this.info)
         	return true;
         
         for (Entry<TypeSolver, TypeSolver.CascadeType> dependence : other.info.depends.entrySet()) {
         	if (dependence.getValue() != CascadeType.Equivalent)
         		continue;
         	else if (visited.contains(dependence.getKey()))
         		continue;
        	else if (areEquivalent(dependence.getKey(), visited))
         		return true;
         }
         
         return false;
     }
     
     public boolean areEquivalent(TypeSolver other) {
         return areEquivalent(other, new HashSet<TypeSolver>());
     }
 
     public void unify(TypeSolver other) {
         assert this.info.constraints.isEmpty();
         assert other.info.constraints.isEmpty();
         if (other.info == this.info)
             return;
         assert this.info.depends.isEmpty();
         assert other.info.depends.isEmpty();
 
         this.info.definedSites.addAll(other.info.definedSites);
 
         this.info.unifiedSet.addAll(other.info.unifiedSet);
         for(TypeSolver otherTS : other.info.unifiedSet)
             otherTS.info = this.info;
     }
 
     public void addDependingTS(TypeSolver dependsOn, CascadeType type) {
         this.info.depends.put(dependsOn, type);
         switch(type) {
         case ArrayToElement:
             dependsOn.info.depends.put(this, CascadeType.ElementToArray);
             break;
         case ElementToArray:
             dependsOn.info.depends.put(this, CascadeType.ArrayToElement);
             break;
         case Equivalent:
             dependsOn.info.depends.put(this, CascadeType.Equivalent);
             break;
         default:
             throw new RuntimeException("Bad CascadeType");
         }
     }
 
     public boolean addConstraint(RopType constraint, boolean freeze, RuntimeHierarchy hierarchy) {
         if (info.freezed) {
             RopType newType = info.type.merge(constraint, hierarchy);
             assert newType.category != Category.Conflicted;
             return false;
         }
         if (info.constraints.contains(constraint))
             return false;
 
         info.constraints.add(constraint);
         RopType newType = info.type.merge(constraint, hierarchy);
         assert newType.category != Category.Conflicted;
         if (freeze) {
             assert !constraint.isPolymorphic();
             info.freezed = true;
             newType = constraint;
         }
         if (newType != info.type) {
             info.type = newType;
             propagate(hierarchy);
             return true;
         }
         return false;
     }
 
     private void propagate(RuntimeHierarchy hierarchy) {
         for(Entry<TypeSolver, CascadeType> dep : info.depends.entrySet()) {
             switch(dep.getValue()) {
             case ArrayToElement:
                 dep.getKey().addConstraint(info.type.toArrayType(hierarchy.getTypeCache()), info.freezed, hierarchy);
                 break;
             case ElementToArray:
                 dep.getKey().addConstraint(info.type.getArrayElement(), info.freezed, hierarchy);
                 break;
             case Equivalent:
                 dep.getKey().addConstraint(info.type, info.freezed, hierarchy);
                 break;
             default:
                 throw new RuntimeException("Bad CascadeType");
             }
         }
     }
 
     public RopType getType() {
         if (info.type == RopType.One)
             return RopType.Integer;
         else if (info.type == RopType.Zero)
             return RopType.Null;
         else
             return info.type;
     }
 }
