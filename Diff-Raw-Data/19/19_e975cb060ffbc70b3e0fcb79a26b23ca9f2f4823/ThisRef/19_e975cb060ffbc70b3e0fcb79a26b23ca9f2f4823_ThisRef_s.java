 /*
  * File: ThisRef.java
  * Author: Rajesh Gopidi
  * PID:    720367703
  * Course : COMP520
  */
 
 package miniJava.AbstractSyntaxTrees;
 
 import miniJava.SyntacticAnalyzer.SourcePosition;
 
 public class ThisRef extends Reference 
 {
 
     public ThisRef (MemberDecl decl, ClassDecl cdecl, SourcePosition posn) 
     {
         super(posn);
         this.decl = decl;
 	this.cdecl = cdecl;
     }
 
     public ThisRef (ClassDecl decl, SourcePosition posn) 
     {
         super(posn);
         this.decl = null;
 	this.cdecl = decl;
     }
     
     public ThisRef (SourcePosition posn)
     {
         super(posn);
         this.decl = null;
 	this.cdecl = null;
     }
     
     public void setMemberDecl(MemberDecl decl) 
     {
         this.decl = decl;
     }
 
     public void setDecl(ClassDecl decl) 
     {
         this.cdecl = decl;
     }
 
     public <A,R> R visit(Visitor<A,R> v, A o)
     {
         return v.visitThisRef(this, o);
     }
 
     public MemberDecl decl;
     public ClassDecl cdecl;
 }
