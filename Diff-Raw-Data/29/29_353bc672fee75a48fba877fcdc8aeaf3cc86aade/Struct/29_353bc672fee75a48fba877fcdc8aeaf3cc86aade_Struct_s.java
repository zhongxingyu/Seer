 /**
  *  Andromeda, a galaxy extension language.
  *  Copyright (C) 2010 J. 'gex' Finis  (gekko_tgh@gmx.de, sc2mod.com)
  * 
  *  Because of possible Plagiarism, Andromeda is not yet
  *	Open Source. You are not allowed to redistribute the sources
  *	in any form without my permission.
  *  
  */
 package com.sc2mod.andromeda.environment.types;
 
 import java.util.HashSet;
 
 import com.sc2mod.andromeda.environment.Scope;
 import com.sc2mod.andromeda.environment.variables.FieldDecl;
 import com.sc2mod.andromeda.notifications.CompilationError;
 import com.sc2mod.andromeda.parsing.AndromedaReader;
 import com.sc2mod.andromeda.parsing.SourceEnvironment;
 import com.sc2mod.andromeda.syntaxNodes.StructDeclaration;
 
 public class Struct extends RecordType {
 
 	private StructDeclaration declaration;
 
 	public Struct(StructDeclaration declaration, Scope scope) {
 		super(declaration, scope);
 		this.declaration = declaration;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Struct, defined at:\n" + SourceEnvironment.getLastEnvironment().getSourceInformation(declaration);
 	}
 	
 
 	@Override
 	public int getCategory() {
 		return STRUCT;
 	}
 	
 	@Override
 	void resolveMembers(TypeProvider t) {
 		super.resolveMembers(t);
 		for(String name: fields.getFieldNames()){
			//Since the parser forbids accessors in structs, we can savely cast to fieldDecl here
 			FieldDecl field = (FieldDecl)fields.getFieldByName(name);
 			if(!field.getFieldDeclaration().getFieldModifiers().isEmpty()){
 				throw new CompilationError(field.getFieldDeclaration().getFieldModifiers(), "Struct fields cannot have modifiers!");
 			}
 		}
 	}
 	
 	/**
 	 * Structs have no hierarchy, nothing to check :D
 	 * Just add the struct as a root type (because it has no super types)
 	 */
 	public void checkHierarchy(TypeProvider t){
 		t.addRootRecord(this);
 	}
 	protected void checkForHierarchyCircle(TypeProvider typeProvider,HashSet<RecordType> marked) {}
 
 	/**
 	 * Structs cannot be passed as parameter or returned
 	 */
 	@Override
 	public boolean isValidAsParameter() {
 		return false;
 	}
 }
