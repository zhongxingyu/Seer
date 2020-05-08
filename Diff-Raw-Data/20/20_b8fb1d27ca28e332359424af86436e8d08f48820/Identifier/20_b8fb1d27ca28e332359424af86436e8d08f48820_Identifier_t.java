 package zinara.ast.expression;
 
 import zinara.ast.type.BoolType;
 import zinara.ast.type.FloatType;
 import zinara.ast.type.IntType;
 import zinara.ast.type.ListType;
 import zinara.ast.type.DictType;
 import zinara.ast.type.Type;
 import zinara.code_generator.Genx86;
 import zinara.exceptions.InvalidCodeException;
 import zinara.symtable.*;
 
 import java.io.IOException;
 
 public class Identifier extends LValue {
     private String identifier;
     private SymTable symtable;
 
     public Identifier (String id, SymTable st) {
 	identifier = id;
 	symtable = st;
     }
 
     public String getIdentifier() { return identifier; }
     public SymTable getSymTable() { return symtable; }
     public SymValue getSymValue(){
 	return symtable.getSymbol(identifier);
     }
 
     public Type getType() {
 	if (type != null) return type;
 	type = symtable.getSymValueForId(identifier).getType();
 	return type;
     }
     public String toString() { return identifier; }
 
     public void tox86(Genx86 generator)
 	throws IOException, InvalidCodeException {
	System.out.println(identifier);
 	String reg = generator.regName(register,getType());
 	String addrReg = generator.addrRegName(register);
 
 	storeValue(generator, reg, addrReg);
     }
 
     public void currentDirection(Genx86 generator) throws IOException{
 	    String code = "";
 	    String reg = generator.addrRegName(register);
 	    SymValue id = getSymValue();
 	    
 	    code += generator.movAddr(reg,id.getArea());
 	    code += generator.add(reg,id.getOffset());
 	    if (! id.byValue())
 		code += generator.movAddr(reg,"["+reg+"]");
 	    
 	    generator.write(code);
     }
     
     private void storeValue(Genx86 generator, String currentReg, String addrReg)
 	throws IOException,InvalidCodeException{
         //Si es un tipo numerico o boleano, se copian los contenidos
 	currentDirection(generator);
 	generator.write(generator.mov(currentReg,
 				      "["+addrReg+"]"));
     }
 
     public boolean isStaticallyKnown() {
 	SymValue sv = symtable.getSymbol(identifier);
 	if (sv.isVariable()) return false;
 	// Recursively check the content of the expression
 	else return ((Constant)sv.getStatus()).getExpression().isStaticallyKnown();
     }
 
     public Object staticValue() {
 	SymValue sv = symtable.getSymbol(identifier);
 	return ((Constant)sv.getStatus()).getExpression().staticValue();
     }
 
     public boolean isConstant() {
 	SymValue sv = symtable.getSymbol(identifier);
 	return !sv.isVariable();
     }
 }
