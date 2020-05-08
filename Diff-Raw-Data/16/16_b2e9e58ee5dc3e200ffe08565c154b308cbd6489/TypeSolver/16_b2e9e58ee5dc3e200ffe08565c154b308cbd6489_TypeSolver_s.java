 package com.rx201.dx.translator;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 import com.rx201.dx.translator.RopType.Category;
 
 
 
 public class TypeSolver {
 
 	enum CascadeType {Equivalent, ArrayToElement, ElementToArray};
 	
 	private class TypeInfo {
 		HashSet<AnalyzedDexInstruction> definedSites;
 		HashSet<RopType> constraints;
 		RopType type;
 		boolean freezed;
 		HashMap<TypeSolver, CascadeType> depends;
 		
 		public TypeInfo() {
 			freezed = false;
 			type = RopType.Unknown;
 			definedSites = new HashSet<AnalyzedDexInstruction>();
 			constraints = new HashSet<RopType>();
 			depends = new HashMap<TypeSolver, CascadeType>();
 		}
 	};
 	
 	TypeInfo info;
 	
 	public TypeSolver(AnalyzedDexInstruction site) {
 		info = new TypeInfo();
 		info.definedSites.add(site);
 	}
 	
 	public void unify(TypeSolver other) {
 		if (other.info == this.info)
 			return;
 		for(Entry<TypeSolver, CascadeType> dep : other.info.depends.entrySet()) {
 			if (this.info.depends.containsKey(dep.getKey()))
 				assert this.info.depends.get(dep.getKey()) == dep.getValue();
 			this.info.depends.put(dep.getKey(), dep.getValue());
 		}
 		this.info.definedSites.addAll(other.info.definedSites);
 		other.info = this.info;
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
 	
 	public boolean addConstraint(RopType constraint, boolean freeze) {
 		if (info.freezed) {
 			RopType newType = info.type.merge(constraint);
 			assert newType.category != Category.Conflicted;
 			return false;
 		}
 		if (info.constraints.contains(constraint))
 			return false;
 		
 		info.constraints.add(constraint);
 		RopType newType = info.type.merge(constraint);
 		assert newType.category != Category.Conflicted;
 		if (freeze) {
 			assert !constraint.isPolymorphic();
 			info.freezed = true;
 			newType = constraint;
 		}
 		if (newType != info.type) {
 			info.type = newType;
 			propagate();
 			return true;
 		}
 		return false;
 	}
 
 	private void propagate() {
 		for(Entry<TypeSolver, CascadeType> dep : info.depends.entrySet()) {
 			switch(dep.getValue()){
 			case ArrayToElement:
 				dep.getKey().addConstraint(info.type.toArrayType(), info.freezed);
 				break;
 			case ElementToArray:
 				dep.getKey().addConstraint(info.type.getArrayElement(), info.freezed);
 				break;
 			case Equivalent:
 				dep.getKey().addConstraint(info.type, info.freezed);
 				break;
 			default:
 				throw new RuntimeException("Bad CascadeType");
 			}
 		}
 	}
 
 	public RopType getType() {
		if (info.type.isPolymorphic() && info.constraints.size() > 1) {
			int x = 0;
		}
 		if (info.type == RopType.One)
 			return RopType.Integer;
 		else if (info.type == RopType.Zero)
 			return RopType.Null;
 		else
 			return info.type;
 	}
 }
