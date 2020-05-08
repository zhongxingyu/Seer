 package zinara.ast.expression;
 
 import zinara.ast.type.BoolType;
 import zinara.ast.type.FloatType;
 import zinara.ast.type.IntType;
 import zinara.ast.type.ListType;
import zinara.ast.type.Dictype;
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
 
     public void tox86(Genx86 generator) throws IOException, InvalidCodeException {
 	// if (isExpression() && !getSymValue().isKnownConstant())
 	//     generator.write(getSymValue().knownConstant(generator));
 
 	// generator.write(generator.mov(generator.regName(register),
 	// 			      generator.global_offset()+
 	// 			      "+"+
 	// 			      Integer.toString(getSymValue().getOffset())));
 	String reg = generator.regName(register,getType());
 
 	storeValue(generator, reg);
 	// generator.write(generator.add(generator.regName(register),
 	// 			      generator.global_space()));
 
 	// if (isExpression()) {
 	//     if (isBool())
 	// 	writeBooleanExpression(generator);
 	//     else
 	// 	writeExpression(generator);
 	// }
     }
 
     public void currentDirection(Genx86 generator) throws IOException{
 	String reg = generator.addrRegName(register);
 	generator.write(
 			generator.movAddr(reg,
 					  generator.global_offset()+
 					  "+"+
 					  getSymValue().getOffset())
 			);
     }
     
     private void storeValue(Genx86 generator, String currentReg) throws IOException{
         //Si es un tipo numerico o boleano, se copian los contenidos
         if (type.getType() instanceof IntType)
             generator.write(generator.movInt(currentReg,
                                           "[" + generator.global_offset() +
                                           "+" + getSymValue().getOffset() + 
                                           "]"));
         else if (type.getType() instanceof FloatType)
             generator.write(generator.movReal(currentReg,
                                           "[" + generator.global_offset() +
                                           "+" + getSymValue().getOffset() + 
                                           "]"));
         else if (type.getType() instanceof BoolType)
             generator.write(generator.movBool(currentReg,
                                           "[" + generator.global_offset() +
                                           "+" + getSymValue().getOffset() + 
                                           "]"));
         //Si es una lista o diccionario, devuelvo su direccion
         else if ((type.getType() instanceof ListType)||
 		 (type.getType() instanceof DictType))
             generator.write(generator.movAddr(currentReg,
 					      generator.global_offset()+
 					      "+"+
 					      Integer.toString(getSymValue().getOffset())));
         else
             generator.write("Identificador para el tipo "+type.getType().toString()+" no implementado\n");
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
