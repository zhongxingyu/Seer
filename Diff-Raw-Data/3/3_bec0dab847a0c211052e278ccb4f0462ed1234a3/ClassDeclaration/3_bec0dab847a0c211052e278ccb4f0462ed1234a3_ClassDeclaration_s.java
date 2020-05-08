 package edu.kit.pp.minijava.ast;
 
 import edu.kit.pp.minijava.tokens.Identifier;
 import java.util.List;
 
 public class ClassDeclaration extends Node {
 
 	private Identifier _name;
 	private List<ClassMember> _members;
 
 	public ClassDeclaration(Identifier name) {
 		_name = name;
 	}
 
 	public void add(ClassMember member) {
 		_members.add(member);
 	}
 
 }
