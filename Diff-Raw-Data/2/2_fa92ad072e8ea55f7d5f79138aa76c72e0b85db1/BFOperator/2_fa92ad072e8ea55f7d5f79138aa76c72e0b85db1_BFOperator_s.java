 package assembler;
 
 public class BFOperator extends AbsSingleOperator {
 
 	public void generate(String operand) {  
		CodeGenerator.assembler.add(CodeGenerator.check+" "+CodeGenerator.context+"_label"+ operand+":");
 		//System.out.println(jumpType+" label_"+ direction + System.lineSeparator());
 	}
 }
