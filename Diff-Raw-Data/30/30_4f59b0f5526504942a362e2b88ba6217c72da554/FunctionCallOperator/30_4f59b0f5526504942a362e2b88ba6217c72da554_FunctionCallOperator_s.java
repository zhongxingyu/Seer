 package assembler;
 
public class FunctionCallOperator extends AbsSingleOperator {
 
 	@Override
	public void generate(String functionName) {
		CodeGenerator.assembler.add("CALL label_"+ functionName);
 	}
 
 }
