 package zinara.symtable;
 
 import zinara.ast.type.*;
 import zinara.code_generator.Genx86;
 
 public class SymValue{
     private boolean variable;
     public Type type; //@ invariant type != null;
 
     //Variables pertinentes a la generacion de codigo
     private int offset = 0;
 
     //@ requires t != null;
     public SymValue(Type t, boolean var) {
 	this.variable = var;
         this.type = t;
     }
 
     //@ ensures \result != null;
    public Type getType() { 
	Type t = (type instanceof ConstantType ? ((ConstantType)type).getRealType() : type);
	return t;
    }
 
     public boolean isVariable() { return this.variable; }
 
     public int getOffset() { return offset; }
     public void setOffset(int os) { offset = os; }
 
     public String toString() {
     	return "<" + (this.variable ? "Variable" : "Constant") + ": " + this.type + ">";
     }
 
     public boolean isKnownConstant() {
 // 	if (variable || ((ConstantType)getType()).getValue() == null) return false;
 // 	return ((ConstantType)getType()).getValue().isKnown();
 	return false;
     }
 
     public String knownConstant(Genx86 generator) { return ""; }
 }
