 package gov.frb.ma.msu.ProjectionMethodToolsJava;
 import Jama.Matrix;
 /**
  * Associates state variables with an outer-product of Chebyshev polynomials
  * evaluates polynomial on grid
  * should be refactored to simplify the common calculations
  * @author m1gsa00
  *
  */
 public class StateVariablePolynomials extends NonIteratedStateVariablePolynomials {
 	private Matrix variablesIteratedFromChebNodesTimeTP1;
 	private Matrix variablesIteratedFromChebNodesTimeTP1DerivWRTWts;
 	private Matrix variablesIteratedFromChebNodesTimeTP1NSP;
 
 			public StateVariablePolynomials(GridPointsSpec aGrid) throws ProjectionRuntimeException{
 				setTheGrid(aGrid);
 				}
 
 
 /**
  * returns statevarialbepolynomials with the grid ranges shrunken to the middle frac of orignal intervals
  * @param frac
  * @return
  * @throws ProjectionRuntimeException
  */
 	public StateVariablePolynomials theMiddle(double frac) throws ProjectionRuntimeException {
 		StateVariablePolynomials midSP=new StateVariablePolynomials(getTheGrid().theMiddle(frac));
 		return(midSP);}
 /**
  * increases the power associated with each variable by one unless goal power attained for a given variable
  * @param goalPowers
  * @return
  * @throws ProjectionRuntimeException
  */
 	public StateVariablePolynomials incPowers(int [] goalPowers) throws ProjectionRuntimeException {
 		StateVariablePolynomials incSP=new StateVariablePolynomials(getTheGrid().incPowers(goalPowers));
 		return(incSP);}
 
 /**
  * evaluates the derivatives of the time t+1 statevariables 
  * @return Matrix[] one matrix for each polynomial outer product in ProjectionMethodTools Order
  * @throws ProjectionRuntimeException
  */
 	private  Matrix []evaluateDrvsAtNxtNodes() throws ProjectionRuntimeException {
 		int ultSize=1;
 		int ii;    
 		ultSize=getTheGrid().computeOuterProductUltimateSize();
 		int [][] aPolyOrder=getTheGrid().generatePolyOrdersForOuterProduct();
 		int numState=aPolyOrder[0].length;
 		Matrix [] theRes=new Matrix[ultSize];
 		for(ii=0;ii<ultSize;ii++){
 			theRes[ii]=new Matrix(ultSize,numState);	
 		}
 		for(ii=0;ii<ultSize;ii++){
 			Utilities.placeValuesNodewise(ii,evaluateDrvAtNxtNodesHelp(aPolyOrder[ii]),theRes);
 		}
 		return theRes;
 	}
 	/**
 	 * evaluates the derivatives of the time t statevariables 
 	 * @return double [][] one matrix the given polynomial outer product number of eval points rows
 	 * by the number of state variables
 	 * @throws ProjectionRuntimeException
 	 */
 	double [][]
 	          evaluateDrvAtCurNodesHelp(int[] thePolyOrders) throws ProjectionRuntimeException {
 		double [][] theRes=new double[getTheGrid().getTheChebPoly().getOuterProdAtEvalPoints().length][thePolyOrders.length];
 		theRes=evaluateDrvAtNxtNodesHelpHelp(thePolyOrders,getVariablesAtChebyshevNodesTimeT());
 
 		return(theRes);
 	}
 
 /**
  * evaluates derives of time t+1 state variables for given polynomial outer product
  * @param thePolyOrders int[]
  * @return
  * @throws ProjectionRuntimeException
  */
 	private  double [][]
 	                   evaluateDrvAtNxtNodesHelp(int[] thePolyOrders) throws ProjectionRuntimeException {
 		double [][] theRes=new double[getTheGrid().getTheChebPoly().getOuterProdAtEvalPoints().length][thePolyOrders.length];
 		theRes=evaluateDrvAtNxtNodesHelpHelp(thePolyOrders,getVariablesIteratedFromChebNodesTimeTP1().transpose());
 
 		return(theRes);
 	}
 	
 	/**
 	 * evaluates the derivates of the time t+1 statevariables for a given outer product at a given evaluation point
 	 * @param thePolyOrders
 	 * @param evalPts
 	 * @return
 	 * @throws ProjectionRuntimeException
 	 */
 	private  double [][]
 	                   evaluateDrvAtNxtNodesHelpHelp(int[] thePolyOrders,Matrix evalPts) throws ProjectionRuntimeException {
 		double [][] theRes=new double[getTheGrid().getTheChebPoly().getOuterProdAtEvalPoints().length][thePolyOrders.length];
 		int ioutpt;
 		double [] xx;
 		double [][] nxtNodePts=getVariablesAtChebyshevNodesTimeT().getArray();
 		for(ioutpt=0;ioutpt<getTheGrid().getTheChebPoly().getOuterProdAtEvalPoints().length;ioutpt++){
 			xx=nxtNodePts[ioutpt];
 			if(xx.length != thePolyOrders.length) throw new 
 			ProjectionRuntimeException("xx different length than thePolyOrders");
 
 			theRes[ioutpt]=
 				ChebyshevPolynomial.evaluateFirstDrvWRTx(thePolyOrders,xx,
 						getTheGrid().getTheChebPoly().getTheMin(),getTheGrid().getTheChebPoly().getTheMax());
 		}
 		return(theRes);
 	}
 
 /**
  * comutes derivative of the next nonstate variables at chebyshev nodes
  * @param nonStateVariablePolynomials
  * @return
  * @throws ProjectionRuntimeException
  */
 	private Matrix computeJacobianNxtStateAtNodes(NonStateVariablePolynomials nonStateVariablePolynomials) throws ProjectionRuntimeException{
 		int numSVars;   double [][] statePart=null;
 		double [] shockVal= new double[getTheGrid().getNumberOfShocks()];
 		if(getStateVariablePolynomialWeights()==null){
 			numSVars=0;}else{
 				numSVars=getStateVariablePolynomialWeights().getRowDimension();
 				statePart=
 					getVariablesIteratedFromChebNodesTimeTP1DerivWRTWts().getArray();}
 		int numNSVars=nonStateVariablePolynomials.getRelevantWeightsNSP().getRowDimension();
 
 		int numPolys=nonStateVariablePolynomials.getRelevantWeightsNSP().getColumnDimension();
 		int numEls=(numNSVars+numSVars)*numPolys;
 		int numSEls=numSVars*numPolys;
 		double [][] jjRaw = new double[numEls][numEls];
 		int ii;int jj;int kk;
 		for(ii=0;ii<numSVars*numPolys;ii++){
 			for(jj=0;jj<numSVars*numPolys;jj++){
 				jjRaw[ii][jj]=statePart[ii][jj];
 			}}
 
 		double [][][] rawDrvs=evaluateShockIterNSP(nonStateVariablePolynomials, shockVal);
 
 		for(ii=0;ii<numPolys;ii++){
 			for(jj=0;jj<numNSVars;jj++){
 				for(kk=0;kk<numPolys*(numSVars+numNSVars);kk++){
 
 					jjRaw[numSEls+jj*numPolys+ii][kk]=rawDrvs[ii][jj][kk];}}}
 		Matrix jac = new Matrix(jjRaw);
 		return(jac);
 	}
 /*
 	public NonStateVariablePolynomials  incPowers(NonStateVariablePolynomials nonStateVariablePolynomials, int[] goalPowers) throws ProjectionRuntimeException{
 		StateVariablePolynomials incSP=new StateVariablePolynomials(getTheGrid().incPowers(goalPowers));
 		NonStateVariablePolynomials incNSP = new NonStateVariablePolynomials(incSP,nonStateVariablePolynomials.getNonStateVariableNames());
 		return(incNSP);}
 */
 /**
  * evaluate current and future state variable values at grid points reflecting new weights
  * @throws ProjectionRuntimeException
  */
 	public void updateUsingNewNodePtVals() throws ProjectionRuntimeException {
 		if(getStateVariablePolynomialWeights()!=null)	{
 			setVariablesAtChebNodesTimeTDerivWRTWts(computeJacobianCurStateAtNodes());
 			setVariablesIteratedFromChebNodesTimeTP1(computeNxtStateAtNodes());
 			setVariablesIteratedFromChebNodesTimeTP1DerivWRTWts(computeJacobianNxtStateAtNodes());
 			setChebPolyDrvsWRTxTimeTp1Correct(evaluateDrvsAtNxtNodes());
 		}
 
 	}
 	
 	/**
 	 * evaluate current and future non-state variable values at grid points reflecting new weights
 	 * 
 	 * @param nonStateVariablePolynomials
 	 * @throws ProjectionRuntimeException
 	 */
 	public void updateUsingNewNodePtVals(NonStateVariablePolynomials nonStateVariablePolynomials) throws ProjectionRuntimeException {
 setVariablesAtChebNodesTimeTDerivWRTWtsNSP(computeJacobianCurStateAtNodes(nonStateVariablePolynomials));
 if(getStateVariablePolynomialWeights()!=null){
 
 	setVariablesIteratedFromChebNodesTimeTP1NSP(computeNxtStateAtNodes(nonStateVariablePolynomials));
 	setVariablesIteratedFromChebNodesTimeTP1NSP(computeNxtStateAtNodes(nonStateVariablePolynomials));
 		setVariablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP(computeJacobianNxtStateAtNodes(nonStateVariablePolynomials));
 }
 
 
 
 	}
 	
 public Matrix getVariablesAtChebNodesTimeTDerivWRTWtsNSP() {
 		return variablesAtChebNodesTimeTDerivWRTWtsNSP;
 	}
 	public void setVariablesAtChebNodesTimeTDerivWRTWtsNSP(
 			Matrix variablesAtChebNodesTimeTDerivWRTWtsNSP) {
 		this.variablesAtChebNodesTimeTDerivWRTWtsNSP = variablesAtChebNodesTimeTDerivWRTWtsNSP;
 	}
 	public Matrix getVariablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP() {
 		return variablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP;
 	}
 	public void setVariablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP(
 			Matrix variablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP) {
 		this.variablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP = variablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP;
 	}
 
 Matrix	variablesAtChebNodesTimeTDerivWRTWtsNSP;
 Matrix		variablesIteratedFromChebNodesTimeTP1DerivWRTWtsNSP;
 	
 	public Matrix getVariablesIteratedFromChebNodesTimeTP1NSP() {
 			return variablesIteratedFromChebNodesTimeTP1NSP;
 		}
 		private void setVariablesIteratedFromChebNodesTimeTP1NSP(
 				Matrix variablesIteratedFromChebNodesTimeTP1NSP) {
 			this.variablesIteratedFromChebNodesTimeTP1NSP = variablesIteratedFromChebNodesTimeTP1NSP;
 		}
 	private Matrix computeJacobianCurStateAtNodes() {
 			int numVars=getStateVariablePolynomialWeights().getRowDimension();
 			int numPolys=getStateVariablePolynomialWeights().getColumnDimension();
 			int numEls=(numVars)*numPolys;
 			double [][] jjRaw = new double[numEls][numEls];
 			int ii;int jj;int kk;
 			for(ii=0;ii<numVars;ii++){
 				for(jj=0;jj<numPolys;jj++){
 					for(kk=0;kk<numPolys;kk++){
 						jjRaw[ii*numPolys+jj][ii*numPolys+kk]=
 							getTheGrid().getBasisAtChebNodes()[jj][kk];
 					}}}
 
 			Matrix jac = new 
 			Matrix(jjRaw);
 			return(jac.transpose());
 		}
 		public Matrix computeNxtStateAtNodes() throws ProjectionRuntimeException {
 			double []shockVal=getTheShockVals();
 			Matrix ff = new 
 			Matrix(evaluateShock(shockVal));
 			int mm=ff.getRowDimension();
 			int nn=ff.getColumnDimension();
 			Matrix resMat=new Matrix(ff.getColumnPackedCopy(),mm*nn);
 			return(resMat);
 		}
 
 		public Matrix computeNxtStateAtNodes(NonStateVariablePolynomials nonStateVariablePolynomials) throws ProjectionRuntimeException {
 
 			double []shockVal=this.getTheShockVals();
 
 
 			Matrix ff = new 
 			Matrix(evaluateShockNSP(nonStateVariablePolynomials, shockVal));
 			int mm=ff.getRowDimension();
 			int nn=ff.getColumnDimension();
 			Matrix resMat=new Matrix(ff.getColumnPackedCopy(),mm*nn);
 			return(resMat);
 		}
 
 
 		private double[][] evaluateShockIter(double[] xx,double[]shockVal)throws ProjectionRuntimeException{
 
 			
 
 
 			double [][] bigRes=new double[getNumPolys()*getStateDimWithoutShocks()][getStateDimWithoutShocks()];
 			setNumberOfShocks(shockVal.length);
 			double[] nxtX=Utilities.augmentVecWithVal(evaluate(xx),shockVal);
 			double [] dirEff =getTheGrid().getTheChebPoly().evaluateBasisPolysAtPt(nxtX);
 			double [] notDirEff=getTheGrid().getTheChebPoly().evaluateBasisPolysAtPt(xx);
 			Matrix polyDrvs =new Matrix(
 					getTheGrid().getTheChebPoly().evaluateBasisPolysDrvsAtPt(nxtX));
 			Matrix theDrvProd=getStateVariablePolynomialWeights().times(polyDrvs);
 			int ii;int jj;int kk;
 			for(ii=0;ii<getStateDimWithoutShocks();ii++){
 				for(jj=0;jj<getStateDimWithoutShocks();jj++){
 					for(kk=0;kk<getNumPolys();kk++){
 						bigRes[ii*getNumPolys()+kk][jj]=notDirEff[kk]*theDrvProd.get(jj,ii);
 					}
 				}
 				for(kk=0;kk<getNumPolys();kk++){
 					bigRes[ii*getNumPolys()+kk][ii]+=dirEff[kk];
 				}
 			}
 			return(bigRes);};
 			private double [][][] evaluateShockIter(double[][] xx, double [] shockVal) throws ProjectionRuntimeException {
 				int ii;double [][][] theRes = new double[xx.length][][];
 				for(ii=0;ii<xx.length;ii++){
 					theRes[ii]=evaluateShockIter(xx[ii],shockVal);
 				}
 				return(theRes);
 			}
 			private double [][][] evaluateShockIter(double [] shockVal) throws ProjectionRuntimeException {
 
 				return(evaluateShockIter(getXformedChebNodePts(),shockVal));	
 
 			}
 
 			public Matrix computeJacobianNxtStateAtNodes()
 			throws ProjectionRuntimeException {
 				double [] shockVal= new double[getTheGrid().getNumberOfShocks()];
 				int numVars=getStateVariablePolynomialWeights().getRowDimension();
 				int numPolys=getStateVariablePolynomialWeights().getColumnDimension();
 				int numEls=(numVars)*numPolys;
 				double [][] jjRaw = new double[numEls][numEls];
 				int ii;int jj;int kk;
 				double [][][] rawDrvs=evaluateShockIter(shockVal);
 
 				for(ii=0;ii<numVars;ii++){
 					for(jj=0;jj<numPolys;jj++){
 						for(kk=0;kk<numPolys*numVars;kk++){
 
 							jjRaw[kk][ii*numPolys+jj]=rawDrvs[jj][kk][ii];}}}
 				Matrix jac = new Matrix(jjRaw);
 				return(jac.transpose());
 			}
 			public Matrix getVariablesIteratedFromChebNodesTimeTP1() {
 				return variablesIteratedFromChebNodesTimeTP1;
 			}
 			public void setVariablesIteratedFromChebNodesTimeTP1(Matrix varsIteratedFromChebNodesTimeTP1) {
 				this.variablesIteratedFromChebNodesTimeTP1 = varsIteratedFromChebNodesTimeTP1;
 			}
 			public Matrix getVariablesIteratedFromChebNodesTimeTP1DerivWRTWts() {
 				return variablesIteratedFromChebNodesTimeTP1DerivWRTWts;
 			}
 			public void setVariablesIteratedFromChebNodesTimeTP1DerivWRTWts(Matrix varsIteratedFromChebNodesTimeTP1DerivWRTWts) {
 				this.variablesIteratedFromChebNodesTimeTP1DerivWRTWts = varsIteratedFromChebNodesTimeTP1DerivWRTWts;
 			}
 
 
 			public void setZeroWeights(){
 					double[][] zeroMat=new  double[this.getTheGrid().getVariableNames().length][getTheGrid().powersPlusOneProd()];
 					setTheWeights(zeroMat);
 				}	
 
 /**
  * sets the Chebyshev polynomial outer-product weights
  * each row corresponds to a variable each column corresponds toa polynomial in ProjectionMethodTools order
  * @see GridPointsSpec
  * @param wtVal
  * @throws ProjectionRuntimeException
  */
 				public void setTheWeights(double [][] wtVal) throws ProjectionRuntimeException {
					this.setTheShockVals(new double[this.getNumberOfShocks()]);
 					if(wtVal.length>0){setVariablePolynomialWeights(new Matrix(wtVal));
 					Matrix topMat =getStateVariablePolynomialWeights().times(getBasisAtChebNodesAsMatrix());
 					Matrix wholeMat = new Matrix(topMat.getRowDimension()+getNumberOfShocks(),topMat.getColumnDimension());
 					wholeMat.setMatrix(0,topMat.getRowDimension()-1,0,topMat.getColumnDimension()-1,topMat);
 					setVariablesAtChebyshevNodesTimeT(wholeMat);}
 					updateUsingNewNodePtVals();
 				}
 
 				public void setTheWeights(NonStateVariablePolynomials nonStateVariablePolynomials, double[][] wtVal) throws ProjectionRuntimeException {
 					if(wtVal.length==0) {nonStateVariablePolynomials.setRelevantWeightsNSP(null);}
 					else {nonStateVariablePolynomials.setRelevantWeightsNSP(new Matrix(wtVal));
 					}
 
 					if(wtVal.length==0){
 
 						setVariablesAtChebyshevNodesTimeTNSP(null);
 					}
 					else {
 
 						setVariablesAtChebyshevNodesTimeTNSP(nonStateVariablePolynomials.getRelevantWeightsNSP().times(getBasisAtChebNodesAsMatrix()));
 					}
 					if(wtVal.length!=0)	updateUsingNewNodePtVals(nonStateVariablePolynomials);
 				}
 Matrix variablesAtChebyshevNodesTimeTNSP;
 private double[] theShockVals = new double[0];
 private Matrix variablesAtChebyshevNodesTimeT;
 private Matrix variablesAtChebNodesTimeTDerivWRTWts;
 private Matrix [] chebPolyDrvsWRTxTimeTp1Correct;
 public Matrix getVariablesAtChebyshevNodesTimeTNSP() {
 	return variablesAtChebyshevNodesTimeTNSP.transpose();
 }
 public void setVariablesAtChebyshevNodesTimeTNSP(
 		Matrix variablesAtChebyshevNodesTimeTNSP) {
 	this.variablesAtChebyshevNodesTimeTNSP = variablesAtChebyshevNodesTimeTNSP;
 }
 double[] evaluateNSP(NonStateVariablePolynomials nonStateVariablePolynomials, double[] xx)throws ProjectionRuntimeException{	
 	double [] theRes =null;
 	double [] dblVals=getTheChebyshevPolysAtEvalPoints().evaluateBasisPolysAtPt(xx);
 	Matrix polysAtPt=new Matrix(dblVals,dblVals.length);
 	if(nonStateVariablePolynomials.getRelevantWeightsNSP()==null)
 		System.out.println("found it");
 
 	Matrix matRes=nonStateVariablePolynomials.getRelevantWeightsNSP().times(polysAtPt);
 	theRes=matRes.getColumnPackedCopy();
 	return(theRes); }
 double [][] evaluateNSP(NonStateVariablePolynomials nonStateVariablePolynomials, double[][] xx) throws ProjectionRuntimeException {
 
 	double [][] theRes=new double[xx.length][];
 int ii;
 for(ii=0;ii<xx.length;ii++){
     theRes[ii]=evaluateNSP(nonStateVariablePolynomials, xx[ii]);
 }
 return(theRes);
 }
 private double [][]  evaluateShockNSP(NonStateVariablePolynomials nonStateVariablePolynomials, double[] shockVal)throws ProjectionRuntimeException{
 	setNumberOfShocks(shockVal.length);
 		double [][] withAug =Utilities.augmentMatrixWithVal(
 				evaluate(getTheChebyshevPolysAtEvalPoints().getXformedOuterProdEvalPoints()
 						  ),shockVal);
 		double[][]theRes=evaluateNSP(nonStateVariablePolynomials, withAug);
 	return(theRes);
 	}
 double[][] evaluateShockIterNSP(NonStateVariablePolynomials nonStateVariablePolynomials, double[] xx, double[] shockVal)throws ProjectionRuntimeException{
 	
 	int numPolys=0;int numSVars=0;int numNSVars=0;
 if(getStateVariablePolynomialWeights()!=null){
 	numPolys=getStateVariablePolynomialWeights().getColumnDimension();
 	numSVars=getStateVariablePolynomialWeights().getRowDimension();} 
 if(nonStateVariablePolynomials.getRelevantWeightsNSP()!=null){
  numNSVars=nonStateVariablePolynomials.getRelevantWeightsNSP().getRowDimension();
 numPolys=nonStateVariablePolynomials.getRelevantWeightsNSP().getColumnDimension();} 
 	double [][] betterRes=
 		new double[numNSVars][numPolys*(numNSVars+numSVars)];
 	double [] nxtX;		
 	double [] dirEff;
 	double [] notDirEff;
 		Matrix polyDrvs;
 		setNumberOfShocks(shockVal.length);
 	 nxtX=Utilities.augmentVecWithVal(evaluate(xx),shockVal);
 	 dirEff =
 		 getTheGrid().getTheChebPoly().evaluateBasisPolysAtPt(nxtX);
 	 notDirEff =
 		 getTheGrid().getTheChebPoly().evaluateBasisPolysAtPt(xx);
 	polyDrvs =new Matrix(
 			getTheGrid().getTheChebPoly().evaluateBasisPolysDrvsAtPt(nxtX));
 	Matrix theDrvProd=nonStateVariablePolynomials.getRelevantWeightsNSP().times(polyDrvs);
 	int ii;int jj;int kk;
 
 	for(ii=0;ii<numNSVars;ii++){
 		for(kk=0;kk<numPolys;kk++){
 			betterRes[ii][(numSVars+ii)*numPolys+kk]=dirEff[kk];
 		}}
 	for(ii=0;ii<numNSVars;ii++){
 		for(jj=0;jj<numSVars;jj++){
 			for(kk=0;kk<numPolys;kk++){
 				betterRes[ii][jj*numPolys+kk]=
 					notDirEff[kk]*theDrvProd.get(ii,jj);
 			}}}
 	return(betterRes); }
 private double [][][] evaluateShockIterNSP(NonStateVariablePolynomials nonStateVariablePolynomials, double[] shockVal) throws ProjectionRuntimeException {
 
 	return(evaluateShockIterNSP(nonStateVariablePolynomials, getTheChebyshevPolysAtEvalPoints().getXformedOuterProdEvalPoints(),shockVal));	
 
 }
 private double [][][] evaluateShockIterNSP(NonStateVariablePolynomials nonStateVariablePolynomials, double[][] xx, double[] shockVal) throws ProjectionRuntimeException {
 	int ii;double [][][] theRes = new double[xx.length][][];
 	for(ii=0;ii<xx.length;ii++){
 		theRes[ii]=evaluateShockIterNSP(nonStateVariablePolynomials, xx[ii],shockVal);
 	}
 	return(theRes);
 }
 
 private double[] getTheShockVals() {
 
 	return theShockVals;
 }
 
 public void setTheShockVals(double[] theShockVals) {
 	this.theShockVals = theShockVals;
 }
 
 
 /**
  * returns values iterated forward one period from chebyshev node grid values
  * @return
  */
 public Matrix getVariablesAtChebyshevNodesTimeT() {
 	return variablesAtChebyshevNodesTimeT.transpose();
 }
 
 
 public void setVariablesAtChebyshevNodesTimeT(Matrix variablesAtChebyshevNodesTimeT) {
 	this.variablesAtChebyshevNodesTimeT = variablesAtChebyshevNodesTimeT;
 }
 
 
 public Matrix getVariablesAtChebNodesTimeTDerivWRTWts() {
 	return variablesAtChebNodesTimeTDerivWRTWts;
 }
 
 
 public void setVariablesAtChebNodesTimeTDerivWRTWts(Matrix varsAtChebNodesTimeTDerivWRTWts) {
 	this.variablesAtChebNodesTimeTDerivWRTWts = varsAtChebNodesTimeTDerivWRTWts;
 }
 
 
 public Matrix computeJacobianCurStateAtNodes(NonStateVariablePolynomials nonStateVariablePolynomials) {
 	int numSVars;double[][] statePart=null;
 	if(getStateVariablePolynomialWeights()==null)
 	{numSVars=0;} else{
 		numSVars=getStateVariablePolynomialWeights().getRowDimension();	
 		statePart=
 			getVariablesAtChebNodesTimeTDerivWRTWts().transpose().getArray();}
 	int numNSVars;
 	if(nonStateVariablePolynomials.getRelevantWeightsNSP()==null)
 		numNSVars=0; else
 			numNSVars=nonStateVariablePolynomials.getRelevantWeightsNSP().getRowDimension();
 	int numPolys=0;
 	if(nonStateVariablePolynomials.getRelevantWeightsNSP()!=null)
 		numPolys=nonStateVariablePolynomials.getRelevantWeightsNSP().getColumnDimension();else
 			getStateVariablePolynomialWeights().getColumnDimension();		
 	int numEls=(numNSVars+numSVars)*numPolys;
 	int numStateEls=numSVars*numPolys;
 	double [][] jjRaw = new double[numEls][numEls];
 	int ii;int jj;int kk;
 	for(ii=0;ii<numSVars;ii++){
 		for(jj=0;jj<numPolys;jj++){
 			for(kk=0;kk<numPolys;kk++){
 				jjRaw[numPolys*ii+jj][numPolys*ii+kk]=
 					statePart[numPolys*ii+jj][numPolys*ii+kk];
 			}}}
 	for(ii=0;ii<numNSVars;ii++){
 		for(jj=0;jj<numPolys;jj++){
 			for(kk=0;kk<numPolys;kk++){
 				jjRaw[numStateEls+ii*numPolys+jj][numStateEls+ii*numPolys+kk]=
 					getTheGrid().getTheChebPoly().getBasisAtEvalPoints()[jj][kk];
 			}}}
 	if(jjRaw.length>0){Matrix jac = new 
 		Matrix(jjRaw);
 	return(jac.transpose());} else {return(null);}
 }
 
 
 public Matrix[] getChebPolyDrvsWRTxTimeTp1Correct() {
 	return chebPolyDrvsWRTxTimeTp1Correct;
 }
 
 
 public void setChebPolyDrvsWRTxTimeTp1Correct(Matrix[] chebPolyDrvsWRTxTimeTp1Correct) {
 	this.chebPolyDrvsWRTxTimeTp1Correct = chebPolyDrvsWRTxTimeTp1Correct;
 }
 
 
 /**
  * applies state polynomial weights to time t outerproduct of polynomials evaluated at given point and augments with shock value
  * @param xx
  * @param shockVal
  * @return
  * @throws ProjectionRuntimeException
  */
 
 /**
  * applies state polynomial weights to time t outerproduct of polynomials evaluated at chebyshev node points given points (augmented by the shock value)
  * time t shocks handled like time t-1 state variables. 
  * get time t shocks from grid and use shockVal as same time index as regular t+1
  * state variables
  * rows of xx typically range over node points or iterated node points
  * @param xx
  * @param shockVal
  * @return
  * @throws ProjectionRuntimeException
  */
 private double [][] evaluateShock(double [] shockVal) throws ProjectionRuntimeException {
 	double [][] withAug =Utilities.augmentMatrixWithVal(
 			evaluate(
 					getXformedChebNodePts()),shockVal);
 	double[][]theRes=evaluate(withAug);
 	return(theRes);
 }
 /**
  * applies state polynomial weight to the outerproduct of polynomials evaluated at the given point
  * @param xx
  * @return
  * @throws ProjectionRuntimeException
  */
 public double[] evaluate(double[] xx) throws ProjectionRuntimeException {	
 double [] theRes =null;
 double [] dblVals=getTheChebyshevPolysAtEvalPoints().evaluateBasisPolysAtPt(xx);
 Matrix polysAtPt=new Matrix(dblVals,dblVals.length);
 Matrix matRes=getStateVariablePolynomialWeights().times(polysAtPt);
 theRes=matRes.getColumnPackedCopy();
 return(theRes); }
 
 
 /**
  * applies state polynomial weight to the outerproduct of polynomials evaluated at the given points
  * typically rows range over node points or iterated node points
  * @param xx
  * @return
  * @throws ProjectionRuntimeException
  */
 private double [][] evaluate(double [] [] xx) throws ProjectionRuntimeException {
 	double [][] theRes=new double[xx.length][];
 	int ii;
 	for(ii=0;ii<xx.length;ii++){
 		theRes[ii]=this.evaluate(xx[ii]);
 	}
 	return(theRes);
 }
 
 
 				
 }
 
 
