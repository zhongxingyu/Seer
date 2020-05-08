 package zinara.ast.instructions;
 import zinara.code_generator.*;
 
 import zinara.ast.expression.Expression;
 import zinara.ast.type.Type;
 import zinara.ast.type.ListType;
 import zinara.exceptions.InvalidCodeException;
 import zinara.exceptions.TypeClashException;
 import zinara.symtable.SymTable;
 import zinara.symtable.SymValue;
 
 import java.io.IOException;
 
 public class For extends Instruction{
     private Expression expr;
     public CodeBlock code;
     private String i; //Iteration variable name
 
     public For(String i, Expression ex, CodeBlock cb){
 	this.i = i;
 	this.code = cb;
 	this.expr = ex;
     }
 
     public String getI(){
 	return this.i;
     }
 
     public CodeBlock getCode(){
 	return this.code;
     }
 
     public Expression getExpression(){
 	return this.expr;
     }
 
     public String toString() {
 	return "<For " + i + " in " + expr + ": " + code + ">";
     }   
 
     public void checkNoReturns(){
 	code.checkNoReturns();
     }
 
     public void tox86(Genx86 generator)
 	throws IOException,InvalidCodeException{
 	Type listType = ((ListType)expr.type).getInsideType();
 	String exprAddr = generator.addrRegName(register);  //direccion del la lista
 	String iteration_var = generator.regName(register+1, listType);
	String iteration_var_addr = generator.global_offset();
	iteration_var_addr += "+"+code.getSymTable().getSymbol(this.i).getOffset();
 
 	expr.register = register;
 	code.register = register+2;
 
 	//save
 	generator.write(generator.save(register+1));
 	generator.write(generator.save(register+2));
 
 	break_label = nextInst;
 
 	expr.tox86(generator);
 	for(int i = 0; i< ((ListType)expr.type).len(); ++i){
 	    continue_label = generator.newLabel();
 	    set_breaks_continues(code,break_label,continue_label);
 
 	    //Muevo el siguiente valor de la lista un registro
 	    //y de ahi a la direccion de la variable de iteracion
 	    generator.write(generator.mov(iteration_var,"["+exprAddr+"]",listType));
 	    generator.write(generator.mov("["+iteration_var_addr+"]",iteration_var,listType));
 
 	    //Genero el codigo
 	    code.tox86(generator);
 
 	    generator.writeLabel(continue_label);
 	    //Muevo exprAddr a apuntar a la siguiente posicion de la lista
 	    generator.write(generator.add(exprAddr,Integer.toString(listType.size())));
 	}
 
 	//restore
 	generator.write(generator.restore(register+2));
 	generator.write(generator.restore(register+1));
     }
 }
