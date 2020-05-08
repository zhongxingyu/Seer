 package reporting;
 
 public class FunctionCallReport extends Scorable{
 	private String id;
 	public int numRequireFunctionCalls;
 	public int numRequireWrongFunctionCalls;
 	public int numLocalFunctionCalls;
 	public int numUnknownFunctionCalls;
 	
 	public FunctionCallReport(String id) {
 		this.id = id;
 		this.numRequireFunctionCalls = 0;
 		this.numRequireWrongFunctionCalls = 0;
 		this.numLocalFunctionCalls = 0;
 		this.numUnknownFunctionCalls = 0;
 	}
 	
 	public void add(FunctionCallReport other) {
 		this.numRequireFunctionCalls += other.numRequireFunctionCalls;
		this.numRequireWrongFunctionCalls = other.numRequireWrongFunctionCalls;
 		this.numLocalFunctionCalls += other.numLocalFunctionCalls;
 		this.numUnknownFunctionCalls += other.numUnknownFunctionCalls;
 	}
 	
 	public String getId() {
 		return this.id;
 	}
 	
 	public int getTotal() {
 		return this.numRequireFunctionCalls + this.numRequireWrongFunctionCalls + this.numLocalFunctionCalls + this.numUnknownFunctionCalls;
 	}
 	
 	@Override
 	public double getScore() {
 		double max = (double)this.getTotal();
 		double score = ((double)(this.numRequireFunctionCalls + this.numLocalFunctionCalls)) + ((double)this.numRequireWrongFunctionCalls)/2.0;
 		if(max == 0.0)
 			return 1.0;
 		else 
 			return score / max;
 	}
 }
