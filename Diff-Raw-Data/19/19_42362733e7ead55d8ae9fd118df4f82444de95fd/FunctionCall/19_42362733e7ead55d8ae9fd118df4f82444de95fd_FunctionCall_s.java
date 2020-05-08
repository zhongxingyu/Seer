 package lea.syntax;
 
import lea.generator.CodeWriter;
 
 public class FunctionCall extends Expression {
 	FunctionRef fRef;
 	Expression exp;
 
 	public FunctionCall(FunctionRef a1, Expression a2) {
 		super(a1, a2);
 		fRef = a1;
 		exp = a2;
 
 	}
 
 	public String toString() {
 		return "FunctionCall" + super.toString();
 	}
 
 	public String toDotString() {
 		return "Call";
 	}
 
	public void toJava(CodeWriter w) {
		if (exp != null)
			w.writeLine(fRef.toJava() + exp.toJava() + ");");
		else
			w.writeLine(fRef.toJava() + ")");

	}

 	public String toJava() {
 		if (exp != null)
 			return fRef.toJava() + exp.toJava() + ")";
 		else
 			return fRef.toJava() + ")";
 
 	}
 }
