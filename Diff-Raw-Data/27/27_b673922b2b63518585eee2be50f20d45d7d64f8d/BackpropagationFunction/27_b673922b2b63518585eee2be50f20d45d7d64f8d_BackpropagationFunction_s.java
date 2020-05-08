 package nodes;
 
 
 public class BackpropagationFunction implements NodeVisitor{
 	private double input[];
 	private double expectedOutput;
 	private boolean sumUpOutputError;
 	
 	public BackpropagationFunction() {
 		sumUpOutputError = false;
 	}
 	
 	public BackpropagationFunction(boolean sumUpOutputError) {
 		this.sumUpOutputError = sumUpOutputError;
 	}
 
 	public void setInput(double[] input) {
 		this.input = input;
 	}
 	
 	public void setExpectedOutput(double expectedOutput) {
 		this.expectedOutput = expectedOutput;
 	}
 	
 	public void setSumUpOutputError(boolean sumUpOutputError) {
 		this.sumUpOutputError = sumUpOutputError;
 	}
 
 	@Override
 	public void visit(FiringStrengthNode fsn) {
 		double errorResult = 0.0D;
 		double sumOfFiringStrengths = ((NormFSNode)fsn.successorNodes.get(0)).computeSumOfFiringStrengths();
 		for(Node n : fsn.successorNodes) {
 			if(n.equals(fsn.primarySuccessorNode)) {
 				errorResult += n.error * (( sumOfFiringStrengths - fsn.getOutput() ) / sumOfFiringStrengths / sumOfFiringStrengths);
 			} else {
 				errorResult += n.error * fsn.getOutput() / sumOfFiringStrengths / sumOfFiringStrengths;
 			}
 		}
 		fsn.error = errorResult;
 	}
 	
 	private void partialErrorForABC(MembershipFunctionNode mfn) {
 		//TODO: check paper equations for correctness
 		double errorA = 0.0D;
 		double errorB = 0.0D;
 		double errorC = 0.0D;
 		// error for a
 		if(input[mfn.getVarNumber() - 1] != mfn.c) {
 			errorA = 2 * mfn.b / mfn.a * mfn.getOutput() * (1 - mfn.getOutput());
 		}
 		// error for b
 		if(input[mfn.getVarNumber() - 1] != mfn.c) {
 			errorB = -2 * Math.log(Math.abs((input[mfn.getVarNumber() - 1] - mfn.c) / mfn.a)) * mfn.getOutput() * (1 - mfn.getOutput());
 		}
 		// error for c
 		if(input[mfn.getVarNumber() - 1] != mfn.c) {
 			errorC = 2 * mfn.b / (input[mfn.getVarNumber() - 1] - mfn.c) * mfn.getOutput() * (1 - mfn.getOutput());
 		}
 		
 		mfn.updateErrorSumA(errorA * mfn.error);
 		mfn.updateErrorSumB(errorB * mfn.error);
 		mfn.updateErrorSumC(errorC * mfn.error);
 	}
 
 	@Override
 	public void visit(MembershipFunctionNode mfn) {
 		double errorResult = 0.0D;
 		for(Node n : mfn.successorNodes) {
 			errorResult += n.error * ((FiringStrengthNode)n).computeFiringStrength(mfn);
 		}
 		mfn.error = errorResult;
 		
 		partialErrorForABC(mfn);
 	}
 
 	@Override
 	public void visit(NormFSNode nfsn) {
 		double errorResult = 0.0D;
 		for(Node n : nfsn.successorNodes) {
 			errorResult += n.error * ((PolynomialNode)n).polyResult;
 		}
 		nfsn.error = errorResult;
 	}
 
 	@Override
 	public void visit(PolynomialNode pn) {
 		double errorResult = 0.0D;
 		for(Node n : pn.successorNodes) {
 			errorResult += n.error;
 		}
 		pn.error = errorResult;
 	}
 
 	@Override
 	public void visit(OutputNode on) {
 		on.error = -(expectedOutput - on.getOutput());
 		if(sumUpOutputError) {
 			on.updateSumOfError(Math.abs(expectedOutput - on.getOutput()));
			System.out.println("New output is:" + on.sumOfError);
 		}
 	}
 
 }
