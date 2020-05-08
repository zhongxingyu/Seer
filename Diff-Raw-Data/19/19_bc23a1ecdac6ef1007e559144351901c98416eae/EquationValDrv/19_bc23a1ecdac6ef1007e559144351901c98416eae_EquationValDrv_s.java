 package gov.frb.ma.msu.ProjectionMethodToolsJava;
 //import java.lang.Math;
 /*
 http://www.cs.princeton.edu/introcs/96optimization/
 */
 import Jama.Matrix;
 import gov.frb.ma.msu.gsmin.ValueDerivative;
 
 import java.util.Comparator;
 
 import org.apache.commons.math.MathException;
 import org.apache.commons.math.complex.Complex;
 import org.apache.commons.math.special.Erf.*;
 public class EquationValDrv {
    	double zeroThreshold = 1e-10;
     public int numNodes;
     public Matrix theVal;
     public Matrix theJac;
 
     public EquationValDrv() {
     	super();
     }
     public EquationValDrv(double aVal,int numVars,int theNumNodes) {
     	numNodes=theNumNodes;
     	theVal=new Matrix(numNodes,1,aVal);
     	theJac=new Matrix(numNodes,numVars,0);
     }
     public EquationValDrv(Matrix aVal,Matrix aJac) {
     	theVal=aVal;
     	theJac=aJac;
     	numNodes=theVal.getRowDimension();
     	if(aVal.getColumnDimension()>1)throw new ProjectionRuntimeException("can't have cols>1 for EquationValDrv");
     }
     public EquationValDrv(int aNumNodes,Matrix aVal,Matrix aJac) {
     	theVal=aVal;
     	theJac=aJac;
     	numNodes=aNumNodes;
     	if(aVal.getColumnDimension()>1)throw new ProjectionRuntimeException("can't have cols>1 for EquationValDrv");
     }
     /*for a constant give dims for deriv and vals*/
 
     /*MULTIPLY*/
     public EquationValDrv times(double aVal) throws ProjectionRuntimeException{
     	EquationValDrv theRes = new EquationValDrv(theVal.times(aVal),theJac.times(aVal));
     	double [][] du=theRes.theJac.getArrayCopy();
     	double [][] uu=theRes.theVal.getArrayCopy();
     	chkNan(uu,"times:uu");
     	chkNan(du,"times:du");
     	return(theRes);
     }
 
     public EquationValDrv times(EquationValDrv aValDrv) throws ProjectionRuntimeException{
     	double [][] vdu=theJac.getArrayCopy();
     	double [][] udv=aValDrv.theJac.getArrayCopy();
     	int numRows=vdu.length;
     	int numCols=vdu[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		for(jj=0;jj<numCols;jj++){
     			vdu[ii][jj]=aValDrv.theVal.get(ii,0)*vdu[ii][jj];
     			udv[ii][jj]=theVal.get(ii,0)*udv[ii][jj];
     		}
     	}
     	EquationValDrv theRes = new EquationValDrv(theVal.arrayTimes(aValDrv.theVal),new Matrix(vdu).plus(new Matrix(udv)));
     	double [][] du=theRes.theJac.getArrayCopy();
     	double [][] uu=theRes.theVal.getArrayCopy();
     	chkNan(uu,"times:uu");
     	chkNan(du,"times:du");
     	return(theRes);
     }
 
 
 /*DIVIDE*/
     public EquationValDrv divide(double aVal) throws ProjectionRuntimeException{
     	EquationValDrv theRes = new EquationValDrv(theVal.times(1/aVal),theJac.times(1/aVal));
     	double [][] du=theRes.theJac.getArrayCopy();
     	double [][] uu=theRes.theVal.getArrayCopy();
     	chkNan(uu,"divide:uu");
     	chkNan(du,"divide:du");
     	return(theRes);
     }
 
 
     public EquationValDrv divide(EquationValDrv aValDrv) throws ProjectionRuntimeException{
     	double [][] vdu=theJac.getArrayCopy();
     	double [][] udv=aValDrv.theJac.getArrayCopy();
     	int numRows=vdu.length;int numCols=vdu[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		for(jj=0;jj<numCols;jj++){
     			vdu[ii][jj]=(aValDrv.theVal.get(ii,0)*vdu[ii][jj])/(aValDrv.theVal.get(ii,0)*aValDrv.theVal.get(ii,0));
     			udv[ii][jj]=theVal.get(ii,0)*udv[ii][jj]/(aValDrv.theVal.get(ii,0)*aValDrv.theVal.get(ii,0));
     		}
     	}
     	EquationValDrv theRes = new EquationValDrv(theVal.arrayRightDivide(aValDrv.theVal), new Matrix(vdu).plus(new Matrix(udv)));
     	double [][] du=theRes.theJac.getArrayCopy();
     	double [][] uu=theRes.theVal.getArrayCopy();
     	chkNan(uu,"divide:uu");
     	chkNan(du,"divide:du");
     	return(theRes);
     }
 
 
 /*PLUS*/
     public EquationValDrv plus(double aVal) throws ProjectionRuntimeException{ 
     	EquationValDrv theRes = new EquationValDrv(theVal.plus(new Matrix(theVal.getRowDimension(),1,aVal)),theJac);
     	double [][] du=theRes.theJac.getArrayCopy();
 		double [][] uu=theRes.theVal.getArrayCopy();
 		chkNan(uu,"plus:uu");
 		chkNan(du,"plus:du");
 		return(theRes);
     }
     public EquationValDrv plus(EquationValDrv aValDrv) throws ProjectionRuntimeException{
     	EquationValDrv theRes = new EquationValDrv(theVal.plus(aValDrv.theVal), theJac.plus(aValDrv.theJac));
     	double [][] du=theRes.theJac.getArrayCopy();
     	double [][] uu=theRes.theVal.getArrayCopy();
     	chkNan(uu,"plus:uu");
     	chkNan(du,"plus:du");
     	return(theRes);
     }
 /*compare 1 true -1 false, size of deriv matrix should match but values derivatives never used set zero*/
     public EquationValDrv ge(int aVal) throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=uu.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		if(uu[ii][0]>=aVal)
     			{uu[ii][0]=1;} else {uu[ii][0]=-1;};
   	}
     	EquationValDrv theRes = new EquationValDrv(new Matrix(uu),new Matrix(numRows,numCols));
     	return(theRes);
     }
 	
 
     public EquationValDrv ge(double aVal) throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=uu.length;int numCols=du[0].length;
     	int ii;
     	int jj;
     	for(ii=0;ii<numRows;ii++){
        		if(uu[ii][0]>=aVal)
 			{uu[ii][0]=1;} else {uu[ii][0]=-1;};
 	}
 	EquationValDrv theRes = new EquationValDrv(new Matrix(uu),new Matrix(numRows,numCols));
     	return(theRes);
     }
 
 
     public EquationValDrv ge(EquationValDrv anEqValDrv) throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	double [][] vv=anEqValDrv.theVal.getArrayCopy();
     	int numRows=uu.length;int numCols=du[0].length;
     	int ii;
     	int jj;
     	for(ii=0;ii<numRows;ii++){
        		if(uu[ii][0]>vv[ii][0])
 			{uu[ii][0]=1;} else {uu[ii][0]=-1;};
 	}
     	EquationValDrv theRes = new EquationValDrv(new Matrix(uu),new Matrix(numRows,numCols));
     	return(theRes);
     }
 
     public EquationValDrv eqvdIf(EquationValDrv lftEqvd,EquationValDrv rghtEqvd)throws ProjectionRuntimeException {
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	double [][] ldu=lftEqvd.theJac.getArrayCopy();
     	double [][] luu=lftEqvd.theVal.getArrayCopy();
     	double [][] rdu=rghtEqvd.theJac.getArrayCopy();
     	double [][] ruu=rghtEqvd.theVal.getArrayCopy();
     	int numRows=du.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		if(uu[ii][0]==1){
     			uu[ii][0]=luu[ii][0];
     			for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=ldu[ii][jj];
     		}} else {
     			uu[ii][0]=ruu[ii][0];
     			for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=rdu[ii][jj];
     		} 				
     		}
     	}
     	return(new EquationValDrv(new Matrix(uu),new Matrix(du)));
     }
 	   
     
     public EquationValDrv eqvdIf(EquationValDrv lftEqvd,double rghtVal)throws ProjectionRuntimeException {
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	double [][] ldu=lftEqvd.theJac.getArrayCopy();
     	double [][] luu=lftEqvd.theVal.getArrayCopy();
     	int numRows=du.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		if(uu[ii][0]==1){
     			uu[ii][0]=luu[ii][0];
     			for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=ldu[ii][jj];
     		}} else {
     			uu[ii][0]=rghtVal;
     			for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=0;
     		}    // du[ii][0]=1;			
     		}
     	}
     	return(new EquationValDrv(new Matrix(uu),new Matrix(du)));
     }
 	   
 	   
     
  public EquationValDrv eqvdIf(double lftVal,EquationValDrv rghtEqvd)throws ProjectionRuntimeException {
  	double [][] du=theJac.getArrayCopy();
  	double [][] uu=theVal.getArrayCopy();
  	double [][] rdu=rghtEqvd.theJac.getArrayCopy();
  	double [][] ruu=rghtEqvd.theVal.getArrayCopy();
  	int numRows=du.length;int numCols=du[0].length;
  	int ii;int jj;
  	for(ii=0;ii<numRows;ii++){
  		if(uu[ii][0]==-1){
  			uu[ii][0]=ruu[ii][0];
  			for(jj=0;jj<numCols;jj++){
  			du[ii][jj]=rdu[ii][jj];
  		}} else {
  			uu[ii][0]=lftVal;
  			for(jj=0;jj<numCols;jj++){
  			du[ii][jj]=0;
  		}    // du[ii][0]=1;			
  		}
  	}
  	return(new EquationValDrv(new Matrix(uu),new Matrix(du)));
  }
 	      
 /*SUBTRACT*/
     public EquationValDrv minus(double aVal) throws ProjectionRuntimeException{
     	EquationValDrv theRes = new EquationValDrv(theVal.minus(new Matrix(theVal.getRowDimension(),1,aVal)),theJac);
     	double [][] du=theRes.theJac.getArrayCopy();
     	double [][] uu=theRes.theVal.getArrayCopy();
     	chkNan(uu,"minus:uu");
     	chkNan(du,"minus:du");
     	return(theRes);
     }
     public EquationValDrv minus(EquationValDrv aValDrv) throws ProjectionRuntimeException{
     	EquationValDrv theRes = new EquationValDrv(theVal.minus(aValDrv.theVal), theJac.minus(aValDrv.theJac));
     	double [][] du=theRes.theJac.getArrayCopy();
     	double [][] uu=theRes.theVal.getArrayCopy();
     	chkNan(uu,"minus:uu");
     	chkNan(du,"minus:du");
 		return(theRes);
     }
 
 
     public EquationValDrv log() throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=du.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		for(jj=0;jj<numCols;jj++){
     			if(uu[ii][0]>zeroThreshold){
     				du[ii][jj]=(1/uu[ii][0])*du[ii][jj];
     			} else {
     				du[ii][jj]=(1/zeroThreshold)*du[ii][jj];
     			}
     		}
     		if(uu[ii][0]<=zeroThreshold){
     			/*throw new projectionRuntimeException("eqValDrv:log: log of negative number");*/
     			uu[ii][0]=(uu[ii][0] - zeroThreshold)*(1/zeroThreshold);
     		} else {
     			uu[ii][0]=java.lang.Math.log(uu[ii][0]);
     		}
     	}
     	chkNan(uu,"log:uu");
     	chkNan(du,"log:du");
     	return(new EquationValDrv(new Matrix(uu),new Matrix(du)));
     }
     /*ERROR FUNCTION*/
     public EquationValDrv erfc()throws ProjectionRuntimeException {
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=du.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
        		if(uu[ii][0]>4){
     			uu[ii][0]=2;
         		for(jj=0;jj<numCols;jj++){
         			du[ii][jj]=0;
         			
         		}
     		}
     		else if(uu[ii][0]<-4){
     			uu[ii][0]=0;
         		for(jj=0;jj<numCols;jj++){
         			du[ii][jj]=0;
         			
         		}
     		} else
     		try {
 				uu[ii][0]=1-org.apache.commons.math.special.Erf.erf(uu[ii][0]);
 			} catch (MathException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     		for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=(-2)/(myPow(Math.E,myPow(uu[ii][0],2))*Math.sqrt(Math.PI))*du[ii][jj];
     			
     		}
     	}
     	chkNan(uu,"erf:uu");
     	chkNan(du,"erf:du");
     	return(new EquationValDrv(new Matrix(uu),new Matrix(du)));
     }
     public EquationValDrv erf()throws ProjectionRuntimeException {
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=du.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		if(uu[ii][0]>4){
     			uu[ii][0]=1;
         		for(jj=0;jj<numCols;jj++){
         			du[ii][jj]=0;
         			
         		}
     		}
     		else if(uu[ii][0]<-4){
     			uu[ii][0]=-1;
         		for(jj=0;jj<numCols;jj++){
         			du[ii][jj]=0;
         			
         		}
     		} else
     		try {
 				uu[ii][0]=org.apache.commons.math.special.Erf.erf(uu[ii][0]);
 			} catch (MathException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     		for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=2/(myPow(Math.E,myPow(uu[ii][0],2))*Math.sqrt(Math.PI))*du[ii][jj];
     			
     		}
     	}
     	chkNan(uu,"erf:uu");
     	chkNan(du,"erf:du");
     	return(new EquationValDrv(new Matrix(uu),new Matrix(du)));
     }
     /*EXPONENTIAL*/
 
     public EquationValDrv exp()throws ProjectionRuntimeException {
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=du.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		uu[ii][0]=java.lang.Math.exp(uu[ii][0]);
     		for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=uu[ii][0]*du[ii][jj];
     		}
     	}
     	chkNan(uu,"exp:uu");
     	chkNan(du,"exp:du");
     	return(new EquationValDrv(new Matrix(uu),new Matrix(du)));
     }
 	
     /*POWER*/
     
     private double myPow(double base,int expon){
 		if(expon==0)return(1); 
 		else{ 
 			if(expon>0) {
 					return(base*myPow(base,expon-1));
 			} else {
 					return(myPow(base,expon+1)/base);
 			}
 		}
 	}
 
 	private double myPow(double base,double expon){
 		if(base>=zeroThreshold) {
 			return(java.lang.Math.pow(base,expon));
 		} else {
 			return((base-zeroThreshold)*expon*myPow(zeroThreshold,expon-1));
 		}
 	}
 
     public EquationValDrv pow(int aVal) throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=uu.length;int numCols=du[0].length;
     	int ii;int jj;
     	for(ii=0;ii<numRows;ii++){
     		for(jj=0;jj<numCols;jj++){
     			du[ii][jj]=aVal*myPow(uu[ii][0],aVal-1)*du[ii][jj];
     		} 
     		uu[ii][0]=myPow(uu[ii][0],aVal);
     	}
     	chkNan(uu,"pow:uu");
     	chkNan(du,"pow:du");
     	EquationValDrv theRes = new EquationValDrv(new Matrix(uu),new Matrix(du));
     	return(theRes);
     }
 	
 
     public EquationValDrv pow(double aVal) throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=uu.length;int numCols=du[0].length;
     	int ii;
     	int jj;
     	for(ii=0;ii<numRows;ii++){
     		for(jj=0;jj<numCols;jj++){
     			if(uu[ii][0]>zeroThreshold){
     				du[ii][jj]=aVal*myPow(uu[ii][0],aVal-1)*du[ii][jj];
     			} else {
     				du[ii][jj]=aVal*myPow(zeroThreshold,aVal-1);
     			}
     		}
     		uu[ii][0]=myPow(uu[ii][0],aVal);
     	}
     	chkNan(uu,"pow:uu");
     	chkNan(du,"pow:du");
     	EquationValDrv theRes = new EquationValDrv(new Matrix(uu),new Matrix(du));
     	return(theRes);
     }
 
     public EquationValDrv powValBase(double aVal) throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=uu.length;int numCols=du[0].length;
     	int ii;
     	int jj;
     	for(ii=0;ii<numRows;ii++){
     		for(jj=0;jj<numCols;jj++){
     			if(uu[ii][0]>zeroThreshold){
     				du[ii][jj]=myPow(aVal,uu[ii][0])*java.lang.Math.log(aVal)*du[ii][jj];
     			} else {
     				du[ii][jj]=myPow(aVal,zeroThreshold);
     			}
     		}
     		uu[ii][0]=myPow(aVal,uu[ii][0]);
     	}
     	chkNan(uu,"pow:uu");
     	chkNan(du,"pow:du");
     	EquationValDrv theRes = new EquationValDrv(new Matrix(uu),new Matrix(du));
     	return(theRes);
     }
     
     public EquationValDrv powValBase(int aVal) throws ProjectionRuntimeException{
     	double [][] du=theJac.getArrayCopy();
     	double [][] uu=theVal.getArrayCopy();
     	int numRows=uu.length;int numCols=du[0].length;
     	int ii;
     	int jj;
     	for(ii=0;ii<numRows;ii++){
     		for(jj=0;jj<numCols;jj++){
     			if(uu[ii][0]>zeroThreshold){
     				du[ii][jj]=myPow(aVal,uu[ii][0])*java.lang.Math.log(aVal)*du[ii][jj];
     			} else {
     				du[ii][jj]=myPow(aVal,zeroThreshold);
     			}
     		}
     		uu[ii][0]=myPow(aVal,uu[ii][0]);
     	}
     	chkNan(uu,"pow:uu");
     	chkNan(du,"pow:du");
     	EquationValDrv theRes = new EquationValDrv(new Matrix(uu),new Matrix(du));
     	return(theRes);
     }
 
     public void chkNan(double[][] xx,String theVar) throws ProjectionRuntimeException{
     	int ii;int jj;
     	String eStr;
     	for(ii=0;ii<xx.length;ii++){
     		for(jj=0;jj<xx[0].length;jj++){
     			if(Double.isNaN(xx[ii][jj])) {
     				eStr="eqValDrv: NaN in computation at "+theVar+"["+ii+"]["+jj+"]";
     				throw new ProjectionRuntimeException(eStr);
     			}
     		}
     	}
     }
 
 
    public void print() {
 	   System.out.println("theVal");
 	   theVal.print(10,5);
 	   System.out.println("theJac");
 	   theJac.print(10,5);
     }
 
    public EquationValDrv augSys(EquationValDrv moreEqns) throws ProjectionRuntimeException{
 	   int oldRowDim=theJac.getRowDimension();
 	   int oldColDim=theJac.getColumnDimension();
 	   int addRowDim=moreEqns.theJac.getRowDimension();
 	   int addColDim=moreEqns.theJac.getColumnDimension();
 	   int addNumNodes=moreEqns.numNodes;
 	   if(oldColDim == addColDim) {
 		   if(numNodes==addNumNodes){
 			   Matrix theValMat = new Matrix(oldRowDim+addRowDim,1);
 			   theValMat.setMatrix(0,oldRowDim-1,0,0,theVal);
 			   theValMat.setMatrix(oldRowDim,oldRowDim+addRowDim-1,0,0,moreEqns.theVal);
 			   Matrix theJacMat = new Matrix(oldRowDim+addRowDim,oldColDim);
 			   theJacMat.setMatrix(0,oldRowDim-1,0,oldColDim-1,theJac);
 			   theJacMat.setMatrix(oldRowDim,oldRowDim+addRowDim-1,0,oldColDim-1,moreEqns.theJac);
 			   return(new EquationValDrv(numNodes,theValMat,theJacMat));
 		   } else {
 			   throw new ProjectionRuntimeException("eqValDrv:augSys: numNodes should agree");
 		   }
 	   } else {
 		   throw new ProjectionRuntimeException("eqValDrv:augSys: col dims should agree");
 	   }
    }
     
    public ValueDerivative sumSq(){
     	int oldRowDim=theJac.getRowDimension();
     	int oldColDim=theJac.getColumnDimension();
     	EquationValDrv theSquare = this.times(this);
     	int ii,jj;
     	double [][]sum=new double[1][1];double[][] sumArray=theSquare.theVal.getArray();
     	double theMax=sumArray[0][0];
     	double [][]drvSum=new double[oldColDim][1];double[][] drvSumArray=theSquare.theJac.getArray();
     	for(ii=0;ii<oldRowDim;ii++){
     		theMax=java.lang.Math.max(sumArray[ii][0],theMax);
     	}
     	for(ii=0;ii<oldRowDim;ii++){
     		    		sum[0][0]=sum[0][0]/theMax;
     		for(jj=0;jj<oldColDim;jj++){
     			drvSum[jj][0]=drvSum[jj][0]+(drvSumArray[ii][jj]/(1000*theMax*oldRowDim*oldColDim));
     		}
     	}                                       
     	return(new ValueDerivative(sum,drvSum));
     }
     
    	public ValueDerivative stackCnstrns(){ 	                                       
     	return(new ValueDerivative(this.theVal.transpose().getArray(),this.theJac.transpose().getArray()));
     }
     
 	public int getNumNodes() {
 		return numNodes;
 	}
 	
 	public void setNumNodes(int numNodes) {
 		this.numNodes = numNodes;
 	}
 	
 	public Matrix getTheJac() {
 		return theJac;
 	}
 	
 	public void setTheJac(Matrix theJac) {
 		this.theJac = theJac;
 	}
 	
 	public Matrix getTheVal() {
 		return theVal;
 	}
 	
 	public void setTheVal(Matrix theVal) {
 		this.theVal = theVal;
 	}
 	
 	public double getZeroThreshold() {
 		return zeroThreshold;
 	}
 	
 	public void setZeroThreshold(double zeroThreshold) {
 		this.zeroThreshold = zeroThreshold;
 	}
 	
 	Matrix obtainJacobian(Basis solution) {
 		Matrix JJ = theJac;
 		Utilities.chkNan(theVal.getArray(),"theEqValDrv.theVal");
 		Utilities.chkNan(JJ.getArray(),"JJ");
 		return JJ;
 	}
 }
 
 
 class ComplexComparator implements Comparator<Complex> {
     public int compare(Complex cmplxa, Complex cmplxb) {
     	double aAbs=cmplxa.abs();double bAbs=cmplxb.abs();
     	if(aAbs>bAbs) return(-1); else 
     		if(bAbs>aAbs)return(1); else 
     			if (cmplxb.equals(cmplxa)) return(0); else 
     				if(cmplxa.getImaginary()<0) return (-1); else
     					return 0;
     }
 }
