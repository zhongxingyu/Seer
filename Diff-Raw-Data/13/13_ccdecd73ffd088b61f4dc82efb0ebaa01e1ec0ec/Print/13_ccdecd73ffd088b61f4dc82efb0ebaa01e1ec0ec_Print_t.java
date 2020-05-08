 package zinara.ast.instructions;
 import zinara.code_generator.*;
 
 import zinara.ast.expression.Expression;
 import zinara.ast.type.IntType;
 import zinara.ast.type.FloatType;
 import zinara.ast.type.CharType;
 import zinara.ast.type.BoolType;
 
 import java.io.IOException;
 
 public class Print extends Instruction{
     private Expression expr;
 
     public Print(Expression ex){
 	this.expr = ex;
     }
 
     public Expression getExpression(){
 	return this.expr;
     }
 
     public String toString() {
 	return "<Print " + expr + ">";
     }
 
     public void tox86(Genx86 generate) throws IOException{
 	//Por ahora solo sirve con numeros, bools, chars y flotantes
 	expr.register = register;
 	String expReg = generate.regName(expr.register);
 
 	expr.tox86(generate);
 
 	generate.write(generate.push("rdi"));
 
 	if (expr.type.getType() instanceof IntType){
 	    generate.write(generate.movInt("rdi",expReg));
	    generate.write("call print_int\n");
 	}
 	else if (expr.type.getType() instanceof BoolType){
 	    generate.write(generate.movBool("rdi",expReg));
	    generate.write("call print_int\n");
 	}
 	else if (expr.type.getType() instanceof FloatType){
 	    generate.write(generate.movReal("rdi",expReg));
	    generate.write("call print_float\n");
 	}
 	else if (expr.type.getType() instanceof CharType){
 	    generate.write(generate.movChar("rdi",expReg));
	    generate.write("call print_char\n");
 	}
 	else{
 	    generate.write("print de "+expr.type.getType()+" no implementado");
 	    System.out.println("====================");
 	    System.out.println("print de "+expr.type.getType()+" no implementado");
 	    System.out.println("====================");
 	}
 	
	generate.write(generate.pop("rdi"));
 	// // Por ahora se asume que todas las expresiones son numeros enteros
 	// //de un solo digito.
 	// String code = "";
 	// String expReg = generate.current_reg();
 
 	// expr.tox86(generate);
 
 	// //"Transformo" a ASCII
 	// code += generate.add(expReg,"48");
 
 	// code += generate.save_print_regs();
 
 	// code += generate.mov("["+generate.stack_pointer()+"]",expReg);
 	// // Pongo el valor de la expresion en la pila, ya que la llamada
         // //sys_write necesita que el String este en memoria.
 	
 	// code += generate.setup_print(generate.stack_pointer(),"1","4");
 	// code += generate.syscall();
 	// code += generate.restore_print_regs();
 	    	    
 	// generate.write(code);
     }
 }
