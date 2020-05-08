 package zinara.semantic;
 
 import java.util.HashMap;
 
 import zinara.ast.type.*;
 import zinara.exceptions.TypeClashException;
 import zinara.parser.sym;
 
 /*
   Does type cohersion and simple operator's type checking
  */
 public class Operators {
     private class OP {
 	int operator; 
 	Type rightType;
 	Type leftType;
 	public OP(int o, Type l, Type r) { this.operator = o; this.leftType = l; this.rightType = r; }
 
 	//@ requires other != null;
 	public boolean equals(OP other) {
 	    return (this.leftType.equals(other.leftType)) && (this.rightType.equals(other.rightType)) && (this.operator == other.operator);
 	}
 
 	public int hashCode() {
 	    String hash;
 	    if (rightType != null)
 		hash = leftType.getType().toString() + operator + rightType.getType().toString();
 	    else
 		hash = leftType.getType().toString() + operator;
 	    return Math.abs(hash.hashCode());
 	}
 
 	public String toString() {
 	    if (rightType != null)
 		return leftType.getType().toString() + operator + rightType.getType().toString();
 	    else
 		return leftType.getType().toString() + operator;
 
 	}
     }
 
     public HashMap table; //@invariant table != null;
     public final int arithmetic = -1;
     public final int relational = -2;
     public final int logical    = -3;
 
     public Operators() {
 	this.table = new HashMap();
 	/*
 	  Binary Operators
 	 */
 	// Arithmetic
 	this.table.put(new OP(this.arithmetic, new IntType(), new IntType()).toString(), new IntType()); // new IntType() -> new IntType()
 	this.table.put(new OP(this.arithmetic, new FloatType(), new FloatType()).toString(), new FloatType());
 	this.table.put(new OP(this.arithmetic, new IntType(), new FloatType()).toString(), new FloatType());
 	this.table.put(new OP(this.arithmetic, new FloatType(), new IntType()).toString(), new FloatType());
 	//this.table.put(new OP(sym.PLUS, new CharType(), new CharType()), STRING);
 
 	// Relational
 	this.table.put(new OP(this.relational, new IntType(), new IntType()).toString(), new BoolType());
 	this.table.put(new OP(this.relational, new FloatType(), new FloatType()).toString(), new BoolType());
 	this.table.put(new OP(this.relational, new CharType(), new CharType()).toString(), new BoolType());
 	this.table.put(new OP(this.relational, new BoolType(), new BoolType()).toString(), new BoolType());
 	this.table.put(new OP(this.relational, new IntType(), new FloatType()).toString(), new BoolType());
 	this.table.put(new OP(this.relational, new FloatType(), new IntType()).toString(), new BoolType());
 
 	// Logical
 	this.table.put(new OP(this.logical, new IntType(), new IntType()).toString(), new BoolType());
 	this.table.put(new OP(this.logical, new FloatType(), new FloatType()).toString(), new BoolType());
 	this.table.put(new OP(this.logical, new BoolType(), new BoolType()).toString(), new BoolType());
 	this.table.put(new OP(this.logical, new IntType(), new FloatType()).toString(), new BoolType());
 	this.table.put(new OP(this.logical, new FloatType(), new IntType()).toString(), new BoolType());
 
 	/*
 	  Unary Operators
 	 */
 	this.table.put(new OP(sym.NOEQ, new IntType(), null).toString(), new BoolType());
 	this.table.put(new OP(sym.NOEQ, new FloatType(), null).toString(), new BoolType());
 	this.table.put(new OP(sym.NOEQ, new BoolType(), null).toString(), new BoolType());
 
 	this.table.put(new OP(sym.UMINUS, new IntType(), null).toString(), new IntType());
 	this.table.put(new OP(sym.UMINUS, new FloatType(), null).toString(), new FloatType());
     }
 
     // also returns the type cohersion if needed
     public Type check(int o, Type l, Type r) throws TypeClashException {
 	// maps the operator to `arithmetic`, `relational` or `logical`
 	int om = -1;
 	switch (o) {
 	case sym.PLUS:
 	case sym.MINUS:
 	case sym.TIMES:
 	case sym.DIVIDE:
 	case sym.MOD:
 	case sym.POW:
 	    om = arithmetic;
 	    break;
 	case sym.GT:
 	case sym.LT:
 	case sym.GTE:
 	case sym.LTE:
 	case sym.SHEQ:
 	case sym.DEEQ:
 	    om = relational;
 	    break;
 	case sym.AND:
 	case sym.OR:
 	case sym.SAND:
 	case sym.SOR:
 	    om = logical;
 	    break;
 	}
 
	String comp = new OP(om,r,l).toString();
 	//@ assume \typeof(table.get(comp)) == \type(Type);
 	Type result = (Type)table.get(comp);
 	if (!table.containsKey(comp)) throw new TypeClashException("Error de tipos con el operador " + o + " en la linea tal..."); // mejorar
 	return result;
     }
 }
