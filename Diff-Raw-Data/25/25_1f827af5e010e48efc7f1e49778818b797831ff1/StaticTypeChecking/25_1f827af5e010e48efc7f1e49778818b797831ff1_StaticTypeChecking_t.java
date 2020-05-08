 package zinara.semantic;
 
 import zinara.ast.expression.Expression;
import zinara.ast.instructions.Return;
 import zinara.ast.type.Type;
import zinara.exceptions.InvalidInstructionException;
 import zinara.exceptions.TypeClashException;
 import zinara.symtable.*;
 
 public class StaticTypeChecking {
     public static boolean compareTypes(Type type1, Type type2) {
 	return (type1 == type2);
     }
 
     /*
       Checks if a given expression is of a given type
      */
     public static void checkExpression(Expression expr, Type type)
 	throws TypeClashException {
 	if (!type.equals(expr.getType()))
 	    throw new TypeClashException("Conflicto de tipos en la expresion " + expr + ". Se espera " + type + " y se obtuvo " + expr.getType());
     }

    public static Return checkReturnValue(Expression expr, SymTable st)
	throws TypeClashException, InvalidInstructionException {
	SymValue idSymValue = st.getSymbolRecursively("return");
	if (idSymValue != null) { // we're inside a funtion
	    if (expr.getType().equals(idSymValue.getType())) return new Return(expr);
	    else throw new TypeClashException("Tipo de retorno de la funcion " + idSymValue.getType() + " difiere del tipo de la expresion " + expr);
	} else throw new InvalidInstructionException("Instruccion `return` no permitida en el main");
    }
 }
