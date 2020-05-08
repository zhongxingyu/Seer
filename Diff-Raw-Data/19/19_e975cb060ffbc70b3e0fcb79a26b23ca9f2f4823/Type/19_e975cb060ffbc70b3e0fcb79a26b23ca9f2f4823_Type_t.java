 /**
  * miniJava Abstract Syntax Tree classes
  * @author prins
  * @version COMP 520 (v2.2)
  */
 package miniJava.AbstractSyntaxTrees;
 
 import miniJava.SyntacticAnalyzer.SourcePosition;
 
 abstract public class Type extends AST {
     
     public Type(TypeKind typ, SourcePosition posn){
         super(posn);
         typeKind = typ;
     }
     
     public TypeKind typeKind;
 }
 
        
