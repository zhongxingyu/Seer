 /**
  *  Andromeda, a galaxy extension language.
  *  Copyright (C) 2010 J. 'gex' Finis  (gekko_tgh@gmx.de, sc2mod.com)
  * 
  *  Because of possible Plagiarism, Andromeda is not yet
  *	Open Source. You are not allowed to redistribute the sources
  *	in any form without my permission.
  *  
  */
 package com.sc2mod.andromeda.environment;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import com.sc2mod.andromeda.environment.types.RecordType;
 import com.sc2mod.andromeda.environment.types.Type;
 import com.sc2mod.andromeda.environment.types.TypeParamMapping;
 import com.sc2mod.andromeda.environment.variables.FieldSet;
 import com.sc2mod.andromeda.environment.variables.GenericVarProxy;
 import com.sc2mod.andromeda.environment.variables.VarDecl;
 import com.sc2mod.andromeda.notifications.CompilationError;
 import com.sc2mod.andromeda.parsing.AndromedaReader;
 import com.sc2mod.andromeda.syntaxNodes.MethodInvocation;
 import com.sc2mod.andromeda.syntaxNodes.SyntaxNode;
 
 public class MethodSet implements Cloneable {
 
 	private LinkedHashMap<String, LinkedHashMap<Signature, AbstractFunction>> methods = new LinkedHashMap<String, LinkedHashMap<Signature, AbstractFunction>>();
 	private ArrayList<AbstractFunction> myMethods = new ArrayList<AbstractFunction>();
 
 	private int numUnimplementedMethods;
 	private RecordType containingType;
 	
 	public ArrayList<AbstractFunction> getMyMethods() {
 		return myMethods;
 	}
 
 	
 	public MethodSet(RecordType containingType) {
 		this.containingType = containingType;
 	}
 
 	public boolean containsUnimplementedMethods() {
 		return numUnimplementedMethods > 0;
 	}
 
 	void addMethod(AbstractFunction m, boolean addToList) {
 		String uid = m.getUid();
 		
 		//Check that it is no constructor
 		if(uid.equals(containingType.getName()))
 			throw new CompilationError(m.getDefinition(),"Constructors may not specify a return type.");
 		
 		LinkedHashMap<Signature, AbstractFunction> meths = methods.get(uid);
 		if (meths == null) {
 			meths = new LinkedHashMap<Signature, AbstractFunction>();
 			methods.put(uid, meths);
 		}
 		addMethod(m, meths,addToList);
 	}
 	
 	public void addMethod(AbstractFunction m){
 		addMethod(m,true);
 	}
 
 	private void addMethod(AbstractFunction m, LinkedHashMap<Signature, AbstractFunction> meths,boolean addToList) {
 		
 		//Add to list
 		if(addToList)myMethods.add(m);
 		
 		//Add to hash
 		AbstractFunction old = meths.put(m.getSignature(), m);
 		
 		//Abstract?
 		if(m.getBody() == null) numUnimplementedMethods++;
 			
 		// No duplicate method? Everything fine!
 		if (old == null){
 			//Unless override was specified
 			if(addToList&&m.isOverride())
				throw new CompilationError(m.getDefinition(),"Method was declared 'override' but no overridden method exists in the superclass.");
 			return;
 		}
 		
 		// Both functions defined in the same type? Fail!
 		if (old.getContainingType() == m.getContainingType()) {
 			throw new CompilationError(m.getDefinition(),old.getDefinition(),
 					"Duplicate method!","First Definition");
 		}
 
 		// Different return types? Fail!
 		if (old.getReturnType() != m.getReturnType()) {
 			throw new CompilationError(
 					m.getDefinition(),
 					old.getDefinition(),
 					"The overriding method must have the same return type than the overridden one.\nConflicting return types: "
 							+ m.getReturnType().getUid()
 							+ ", "
 							+ old.getReturnType().getUid(),
 							"Overriden Definition");
 		}
 
 		// Exactly one of them is static? Fail!
 		if (old.isStatic() ^ m.isStatic()) {
 			throw new CompilationError(
 					m.getDefinition(),
 					old.getDefinition(),
 					"Overwriting a static method with a non-static method or vice versa is not possible.",
 					"Overridden Definition");
 		}
 		
 		// Top method final? Fail!
 		if (old.isFinal())
 			throw new CompilationError(m.getDefinition(),
 					old.getDefinition(),
 					"Cannot override a final method.",
 					"Overridden Definition");
 		
 		//Visibility reduced?
 		if(!old.isStatic()&&Visibility.isLessVisibleThan(m.getVisibility(), old.getVisibility()))
 			throw new CompilationError(m.getDefinition(),
 					old.getDefinition(),
					"Cannot reduce visibility of overridden method.",
 					"Overridden Definition");
 		
 		//Override permitted, add it!
 		old.addOverride(m);
 		
 		//Was the old one without body? Reduce abstract count
 		if(old.getBody() == null){
 			numUnimplementedMethods--;
 		}
 	}
 
 	public void addInheritedMethods(MethodSet m) {
 		for (String methodName : m.methods.keySet()) {
 			HashMap<Signature, AbstractFunction> meths = m.methods.get(methodName);
 
 			for (Signature signature : meths.keySet()) {
 				AbstractFunction meth = meths.get(signature);
 
 				// Private methods are not inherited
 				if (meth.getVisibility() == Visibility.PRIVATE)
 					continue;
 
 				addMethod(meth,false);
 
 			}
 
 		}
 	}
 	
 	public AbstractFunction resolveMethodInvocation(SyntaxNode where, String name, Signature s){
 		LinkedHashMap<Signature, AbstractFunction> meths = methods.get(name);
 		if(meths==null) return null;
 		//No signature specified? Only one method with the name allowed
 		if(s == null){
 			if(meths.size()>1) throw new CompilationError(where,"Ambiguous access! There is more than one method with the name " + name);
 			return meths.entrySet().iterator().next().getValue();			
 		}
 		AbstractFunction m = meths.get(s);
 		if(m == null){
 			Signature candidate = null;
 			for(Signature sig: meths.keySet()){
 				if(s.canImplicitCastTo(sig)){
 					if(candidate != null) 
 						throw new CompilationError(where,
 												meths.get(candidate).getDefinition(),
 												meths.get(sig).getDefinition(),
 												"This method call is ambiguous.",
 												"Methods that could be called:\nFirst",
 												"Second");
 						
 					candidate = sig;					
 				}
 			}
 			return meths.get(candidate);
 		}
 		return m;
 	}
 
 	public void generateMethodIndex() {
 		RecordType container = this.containingType;
 		for(String s: methods.keySet()){
 			int index = 0;
 			LinkedHashMap<Signature, AbstractFunction> funcs = methods.get(s);
 			for(Signature sig: funcs.keySet()){
 				AbstractFunction meth = funcs.get(sig);
 
 				if(meth.hasBody()&&meth.getContainingType()==container) meth.setIndex(index++);
 
 			}
 		}
 	}
 
 	public LinkedHashMap<String, LinkedHashMap<Signature, AbstractFunction>> getMethodTable() {
 		return methods;
 	}
 
 	public AbstractFunction getMethod(String name, Signature signature){
 		LinkedHashMap<Signature, AbstractFunction> meths = methods.get(name);
 		if(meths == null) return null;
 		return meths.get(signature);
 	}
 	
 	public List<AbstractFunction> getUnimplementedMethods(){
 		ArrayList<AbstractFunction> result = new ArrayList<AbstractFunction>();
 		for(Entry<String, LinkedHashMap<Signature, AbstractFunction>> f: methods.entrySet()){
 			LinkedHashMap<Signature, AbstractFunction> funcs = f.getValue();
 			for(Entry<Signature, AbstractFunction> f2: funcs.entrySet()){
 				AbstractFunction m = f2.getValue();
 				if(m.getBody() == null) result.add(m);
 			}
 		}
 		return result;
 	}
 
 
 	public MethodSet getAlteredMethodSet(TypeParamMapping paramMap) {
 		MethodSet result = null;
 		try {
 			result = (MethodSet)this.clone();
 		} catch (CloneNotSupportedException e) {}
 		
 		result.methods = new LinkedHashMap<String, LinkedHashMap<Signature, AbstractFunction>>();
 		for(Entry<String, LinkedHashMap<Signature, AbstractFunction>> e: methods.entrySet()){
 			
 			LinkedHashMap<Signature, AbstractFunction> meths;
 			result.methods.put(e.getKey(),meths = new LinkedHashMap<Signature, AbstractFunction>());
 			
 			for(Entry<Signature, AbstractFunction> e2: e.getValue().entrySet()){
 				
 				AbstractFunction vd = e2.getValue();
 				Type t = vd.getReturnType();
 				Type t2 = t.replaceTypeParameters(paramMap);
 				
 				Signature newSignature = e2.getKey().replaceTypeParameters(paramMap);
 				
 				if(t != t2){
 					//Type has changed we need a generic proxy
 					meths.put(newSignature, new GenericFunctionProxy(vd,t2));
 				} else {
 					//Type has not changed, just use the old field
 					meths.put(newSignature, vd);
 				}
 			}
 			
 		}
 		return result;
 	}
 
 }
