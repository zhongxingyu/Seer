 package nodes;
 
 import weka.core.matrix.Matrix;
 
 public class LeastSquaresEstimate implements NodeVisitor{
 	private double[][] input; //needs to contain the 1 at the end
 	private double[] expectedOutput;
 	private double[][] combinedOutputs;
 	private double[] consequentParameters;
 	private boolean isCalculated;
 	private int index;
 	private final double LARGE_NUMBER = 8147483648D;
 	//8147483648D
 	
 	public LeastSquaresEstimate(double[][] input, double[] expectedOutput, int numberOfNormFSNodes) {
 		this.input = input;
 		this.expectedOutput = expectedOutput;
 		combinedOutputs = new double[numberOfNormFSNodes][];
 		isCalculated = false;
 		index = 0;
 	}
 
 	@Override
 	public void visit(FiringStrengthNode fsn) {
 		// do nothing
 	}
 
 	@Override
 	public void visit(MembershipFunctionNode mfn) {
 		// do nothing
 	}
 
 	@Override
 	public void visit(NormFSNode nfsn) {
 		combinedOutputs[index] = nfsn.getSavedOutputs(true);
 		index++;
 	}
 	
 	private double[][] getHelperArray() {
 		double[][] helperArray = new double[combinedOutputs[0].length][combinedOutputs.length * (input[0].length + 1)];
 		int parameterCnt = input[0].length + 1;
 		
 		for(int i = 0; i < combinedOutputs[0].length; i++) {
 			for(int j = 0; j < combinedOutputs.length; j++) {
 				for(int k = 0; k < input[0].length + 1; k++) {
 					if(k == input[0].length) {
 						helperArray[i][j * parameterCnt + k] = combinedOutputs[j][i];
 					} else {
 						helperArray[i][j * parameterCnt + k] = combinedOutputs[j][i] * input[i][k];
 					}
 				}
 			}
 		}
 		
 		return helperArray;
 	}
 	
 	private void calculateLeastSquareEstimate() {
 		
 		double[][] helperArray = getHelperArray();
 //		Matrix helperMatrix = new Matrix(helperArray);
 //		Matrix expectedOutputMatrix = new Matrix(expectedOutput, 1);
 		
 		Matrix X;
 		
 //		try{
 //			X = helperMatrix.transpose().times(helperMatrix).inverse().times(helperMatrix.transpose()).transpose().times(expectedOutputMatrix.transpose());	
 //		} catch (Exception e) {
 			X = calculateLeastSquareEstimate(helperArray);
 //		}
 		
 		consequentParameters = X.transpose().getArray()[0];
 		
 		isCalculated = true;
 		index = 0;
 	}
 	
 	private Matrix calculateLeastSquareEstimate(double[][] helperArray) {
 		
 		Matrix A = new Matrix(helperArray);
 		Matrix B = new Matrix(expectedOutput, 1);
 		
 		double[] x0 = new double[A.getColumnDimension()];
 		Matrix X = new Matrix(x0, A.getColumnDimension());
 		Matrix S = Matrix.identity(A.getColumnDimension(), A.getColumnDimension()).times(LARGE_NUMBER);
 		int[] rowIndex = new int[1];
 		
 		for(int i = 0; i < A.getRowDimension(); i++) {
			
			System.out.println("Iteration: " + i);
			
 			rowIndex[0] = i;
 			Matrix aT = A.getMatrix(rowIndex, 0, A.getColumnDimension()-1);
 			Matrix a = aT.transpose();
 			double b = B.get(0, i);

			System.out.println( "Transposed");
 			
 			S = S.minus((S.times(a).times(aT).times(S)).times(1 / (1.0D + aT.times(S).times(a).get(0,0))));
			
			System.out.println( "Callculated");
			
 			X = X.plus(S.times(a).times( (b - aT.times(X).get(0, 0)) ));
 		}
 		
 		return X;
 				
 //		consequentParameters = X.transpose().getArray()[0];
 //		
 //		isCalculated = true;
 //		index = 0;
 	}
 
 	@Override
 	public void visit(PolynomialNode pn) {
 		if(!isCalculated) {
 			calculateLeastSquareEstimate();
 		}
 		
 		for(int i = index; i < index + input[0].length + 1; i++) {
 			pn.consequentParameter[i % (input[0].length + 1)] = consequentParameters[i];
 		}
 		
 		index += (input[0].length + 1);
 		
 		if(index > consequentParameters.length) {
 			isCalculated = false;
 		}
 	}
 
 	@Override
 	public void visit(OutputNode on) {
 		// do nothing
 	}
 
 }
