 package zinara.ast.expression;
 
 import zinara.ast.type.*;
 import zinara.code_generator.Genx86;
 import zinara.exceptions.InvalidCodeException;
 import zinara.exceptions.TypeClashException;
 import zinara.parser.parser;
 
 import java.io.IOException;
 
 public class CastedExp extends Expression {
     public Type cast;
     public Expression expr;
     
    public CastedExp (Type c, Expression e) {
 	cast=c;
 	expr=e;
     }
     
     public Type getType() throws TypeClashException {
	if (type == null)
	    type = parser.operators.check(parser.operators.cast, this.expr.getType(), null);
 	return type;
     }
 
     public String toString() {
 	return "(<"+cast+"> "+expr+")";
     }
 
     public void tox86(Genx86 generator)
 	throws IOException,InvalidCodeException{
 	String code = "";
 	String reg = generator.regName(register,expr.type);
 	if (expr.type instanceof IntType){
 	    if (cast instanceof FloatType){
 		expr.tox86(generator);      //Se genera la expresion
 		code += generator.fild(reg);//Se guarda la expresion en la pila de flotantes
 		code += generator.fst(reg); //Se saca como flotante
 		code += generator.fninit();  //Se reinicializa la pila de flotantes
 		generator.write(code); 
 	    }
 	}
 	else if (expr.type instanceof FloatType){
 	    if (cast instanceof IntType){
 		expr.tox86(generator);      //Se genera la expresion
 		code += generator.fld(reg); //Se guarda la expresion en la pila de flotantes
 		code += generator.fist(reg);//Se saca como entero
 		code += generator.fninit();  //Se reinicializa la pila de flotantes
 		generator.write(code); 
 	    }
 	}
 	else{
 	    System.out.println("Codigo de CastedExp para "+expr.type+"->"+cast+" no generado");
 	    return;
 	}
     }
 
     public boolean isStaticallyKnown() { return expr.isStaticallyKnown(); }
 
     public Object staticValue() {
 	return null;
     }
 }
