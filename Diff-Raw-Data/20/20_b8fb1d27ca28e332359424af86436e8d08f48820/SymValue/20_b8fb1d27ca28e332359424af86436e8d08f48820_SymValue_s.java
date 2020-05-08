 package zinara.symtable;
 
 import zinara.ast.type.*;
 import zinara.code_generator.Genx86;
 
 public class SymValue{
     private Status status;
     public Type type; //@ invariant type != null;
 
     //Variables pertinentes a la generacion de codigo
     private String offset;
     private String area;
     private boolean byValue;
     private boolean specialSymbol;
     //This is because some simbols, like the returns
     //or the parameters to a sub-routine, need to
     //be treated in special ways
 
     //@ requires t != null;
     public SymValue(Type t, Status s) {
 	this.status = s;
         this.type = t;
 	this.specialSymbol = false;
     }
 
     //@ requires t != null;
     public SymValue(Type t, Status s, boolean p, boolean v) {
 	this.status = s;
         this.type = t;
 	this.specialSymbol = p;
 	this.byValue = v;
     }
 
     //@ ensures \result != null;
     public Type getType() { 
 	return type;
     }
 
     public boolean isVariable() { return status instanceof Variable; }
 
     public Status getStatus() { return status; }
 
     public String getOffset() { return offset; }
     public String getArea() { return area; }
     public void setOffset(String os) { offset = os; }
     public void setArea(String a){ this.area = a; }
 
     public boolean isParam() { return this.specialSymbol; }
     public boolean isReturn() { return this.specialSymbol; }
     //Yes, this is redundant, but is more legibile
 
     public boolean byValue() { return this.byValue; }
 
     public String toString() {
     	return "<" + (isVariable() ? "Variable" : "Constant") + ": " + this.type + ">";
     }
 }
