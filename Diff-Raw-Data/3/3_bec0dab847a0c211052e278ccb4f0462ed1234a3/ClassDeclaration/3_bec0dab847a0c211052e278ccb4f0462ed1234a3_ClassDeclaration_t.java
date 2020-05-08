 package edu.kit.pp.minijava.ast;
 
 import edu.kit.pp.minijava.tokens.Identifier;
import java.util.LinkedList;
 import java.util.List;
 
 public class ClassDeclaration extends Node {
 
 	private Identifier _name;
 	private List<ClassMember> _members;
 
 	public ClassDeclaration(Identifier name) {
 		_name = name;
		_members = new LinkedList<ClassMember>();
 	}
 
 	public void add(ClassMember member) {
 		_members.add(member);
 	}
 
 }
