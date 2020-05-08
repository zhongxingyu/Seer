 package tree;
 
 import output.OutputManager;
 import error.ErrorManager;
 
 public class MethodNode extends ExpressionNode {
 	private String name;
 	private String implicitParam;
 	
	public MethodNode(String name) {
 		this.name = name;
 	}
 	
 	public String getParam () {
 		return this.implicitParam;
 	}
 	
 	public void setName (String name) {
 		this.name = name;
 	}
 	
	public void setImplicitParameter(String implicit) {
		this.implicitParam = implicit;
	}
	
 	public void evaluatePre(int depth, OutputManager out) {
 		out.writeStatement(implicitParam + "." + name + "(");
 	}
 	
 	public void evaluatePost(int depth, OutputManager out) {
 		out.writeStatement(")");
 	}
 	
 	public void analyzePre() {
 		String type = this.getType(name);
 		if (Node.isPrimitive(type)) {
 			ErrorManager.nonFatal(this.getLine(), type + " cannot be dereferenced");
 		}
 	}
 
 	@Override
 	public void analyzePost() {
 		// TODO Auto-generated method stub
 		
 	}
 }
