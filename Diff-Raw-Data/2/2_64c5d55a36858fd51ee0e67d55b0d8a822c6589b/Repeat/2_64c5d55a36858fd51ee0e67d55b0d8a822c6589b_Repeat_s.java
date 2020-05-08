 package lea.syntax;
 
 import lea.generator.CodeWriter;
 
 public class Repeat extends Instruction {
 	Expression condition;
 	Instruction instruction;
 
 	public Repeat(Expression a1, Instruction a2) {
 		super(a1, a2);
 		condition = a1;
 		instruction = a2;
 	}
 
 	public String toString() {
 		return "Repeat" + super.toString();
 	}
 
 	public String toDotString() {
 		return "Repeat";
 	}
 
 	public void toJava(CodeWriter w) {
		w.writeLine("repeat");
 		w.openBlock();
 		instruction.toJava(w);
 		w.closeBlock();
 		w.writeLine("while (" + condition.toJava() + ");");
 	}
 }
