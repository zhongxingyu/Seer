 package zinara.semantic;
 
 import zinara.ast.expression.Expression;
 import zinara.ast.type.Type;
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
 }
