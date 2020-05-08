 package zinara.ast.expression;
 
 import zinara.ast.type.Type;
 import zinara.ast.type.BoolType;
 import zinara.code_generator.Genx86;
 import zinara.parser.parser;
 import zinara.exceptions.InvalidCodeException;
 import zinara.exceptions.TypeClashException;
 
 import java.io.IOException;
 
 public class UnaryBooleanExp extends BooleanExp {
     public int operator;
    public BooleanExp operand;
     
     public UnaryBooleanExp ( int o, Expression e )
 	throws TypeClashException {
	if (!(e instanceof BooleanExp))
 	    throw new TypeClashException("La expresion " + e + " no es del tipo Bool por lo tanto no puede ser negada");
 	operator = o;
	operand  = (BooleanExp)e;
 	type = new BoolType();
     }
 
     public Type getType() {
 	return type;
    }
 
     public String toString() {
 	return "not " + operand;
     }
 
     public void tox86(Genx86 generator)
 	throws IOException,InvalidCodeException{
 	operand.yesLabel = noLabel;
 	operand.noLabel  = yesLabel;
 	operand.tox86(generator);
     }
 
     public boolean isStaticallyKnown() { return operand.isStaticallyKnown(); }
 
     public Object staticValue() { return new Boolean(!((Boolean)operand.staticValue()).booleanValue()); };
 }
