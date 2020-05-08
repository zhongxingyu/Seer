 package nodes;
 
 // layer 4
 
 public class PolynomialNode extends Node {
 	public double[] consequentParameter;
 	
 	public PolynomialNode(double[] consequentParameter) {
 		super();
 		this.consequentParameter = consequentParameter;
 	}
 	
 	public PolynomialNode(int length) {
 		super();
 		this.generateRandomConsequentParameter(length);
 	}
 	
 	public void updateConsequentParameter(double[] newConsequentParameter) {
 		consequentParameter = newConsequentParameter;
 	}
 
 	@Override
 	public void accept(NodeVisitor visitor) {
 		visitor.visit(this);
 	}
 	
 	public void generateRandomConsequentParameter (int length){
 		consequentParameter = new double [length];
		for (int i = 0; i < consequentParameter.length; i++){
			int back = getRandom();
			consequentParameter[i] = (double) back;
 		}
 	}
 	
	public int getRandom(){
		int back = (int) (Math.random() * 10);
		back -= 5;
		
		if (back == 0){
 			back = getRandom();
 		}
 		return back;
 	}
 }
