 package lea.syntax;
 
 import lea.generator.CodeWriter;
 
 public class Instruction extends SyntaxTree {
 	SyntaxTree left, right;
 
 	public Instruction(SyntaxTree a1, SyntaxTree a2) {
 		super(a1, a2);
 		left = a1;
 		right = a2;
 	}
 
 	public String toString() {
 		return "Instr" + super.toString();
 	}
 
 	public String toDotString() {
 		return "Instr";
 	}
 
 	public void toJava(CodeWriter w) {
 		if (left == null && right == null) {
 			// empty instruction, as in { /* here */ }
 			// don't print anything
 		} else if (left instanceof FunctionCall) {
			((FunctionCall) left).toJava(w);
 		} else {
 			w.writeLine("unhandled_instruction();");
 		}
 	}
 }
