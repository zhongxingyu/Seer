 package com.cvparse.generators;
 
 import com.cvparse.pojo.ProblemDefinition;
 import com.cvparse.pojo.Variable;
 
 public class JavaGenerator implements CodeGenerator {
 
 	@Override
  	public String generate(ProblemDefinition pd) {
 		String code = "public class " + pd.getTestFunction().getName() + " {\n";
 		
 		code += "  public " + pd.getTestFunction().getReturnValue().getType().toString().toLowerCase();
 		code += " " + pd.getTestFunction().getName().toLowerCase() + "(";
 		
 		boolean first = true;
 		for (Variable v : pd.getTestFunction().getParameters()) {
 			code += first? "" : ", ";
 			code += v.getType().toString().toLowerCase() + " " + v.getName();
 			first = false;
 		}
 		
		code += ") {\n\n";
 		code += "  }\n";
 				
 		code += "}";		
 		return code;
 	}
 
 	@Override
 	public String generateTests(ProblemDefinition pd) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 	
 }
