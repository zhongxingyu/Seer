 package gov.frb.ma.msu.toC;
 
 import gov.frb.ma.msu.modelEZCommon.Model;
 import gov.frb.ma.msu.toC.AMAtoC;
 
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 
 public class ModelToC extends Model {
 	public ModelToC(String fileName) {
 		// TODO Auto-generated constructor stub
 		super(fileName);
 	}
 
 	public void PrintFunctions() {
 	    int i;
 	    PrintStream dataPS;
 	    PrintStream matrixPS;
 	    PrintStream templatePS;
 	    PrintStream parameterPS;
 	    String lcName = Name;
 	    lcName.toLowerCase();
 	    String dataFileName = lcName + "_AMA_data.c";
 	    String matrixFileName = lcName + "_AMA_matrices.c";
 	    String templateFileName = lcName + "_AMA_template.f90";
 
 	    String parameterFileName = lcName + "_AMA_SetAllParamsZero.f90";
 	    
 	    try {
 	    int numCols=(getNLag()+1+getNLead())*NEq;
 	    templatePS = new PrintStream(new FileOutputStream(templateFileName));
 	    templatePS.println("PROGRAM simpleSparseAMAExample !template file");
 	    templatePS.println();
 	    templatePS.println("IMPLICIT NONE");
 	    templatePS.println("INTEGER :: MAXELEMS, HROWS, HCOLS, LEADS, qrows, qcols");
 	    templatePS.println("INTEGER, DIMENSION(" + (numCols)*NEq + ") :: hmatj, hmati");
	    templatePS.println("REAL(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: hmat");
 	    
 	    templatePS.println();
 	    templatePS.println("INTEGER :: maxNumberOfHElements, aux, rowsinQ, essential, retCODE,i, maxSize, testnp");
 	    
 	    templatePS.println();
 	    templatePS.println("REAL(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: g, h");
 	    templatePS.println("REAL(KIND = 8), DIMENSION(" + NCoeffs + ") :: params");
 	    templatePS.println();
 	    
 	    templatePS.println();
 	    templatePS.println("REAL(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: newHmat, qmat, bmat, rootr, rooti");
 	    templatePS.println("INTEGER(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: newHmatj, newHmati, qmatj, qmati, bmati, bmatj");
 	    templatePS.println("INTEGER, dimension(:), allocatable :: aPointerToVoid");
 	    templatePS.println("INTEGER :: DISCRETE_TIME, ierr");
 	    
 	    templatePS.println();
 	    templatePS.println("INTEGER :: rows, cols");
 	    templatePS.println();
 	    
 	    templatePS.println();
 	    templatePS.println("! Below, all of the parameters used in the model are declared.");
 	    templatePS.println("! However, the user should add to this list any intermediate parameters used in calculation.");
 	    templatePS.println();
 	    
 	    for (i = 0; i < NCoeffs; i++) {
 		    templatePS.println("REAL(KIND = 8) :: " + getCoefficients()[i]);
 		}
 	    
 	    templatePS.println();
 	    templatePS.println("!user must input coefficient values here");
 	    templatePS.println("!Since this file gets overwritten each time the parser is called, the user");
 	    templatePS.println("!may wish to assign these values in a separate routine.");
 	    templatePS.println("!edit and rename the default subroutine that sets all the parameters zero.");
 	    templatePS.println("call "+lcName + "_AMA_SetAllParams(params)");
 	    templatePS.println();
 	    templatePS.println("!zeroing out the input matrices");
 	    templatePS.println("DO i = 1, " + (numCols)*NEq);
 	    templatePS.println("hmatj(i) = 0");
 	    templatePS.println("hmat(i) = 0.0");
 	    templatePS.println("hmati(i) = 0");
 	    templatePS.println("newHmat(i) = 0.0");
 	    templatePS.println("END DO");
 	    templatePS.println();
 	   
 	    
 	    templatePS.println();
	    templatePS.println("call parserwrapper(params, g, h, hmat, HROWS, HCOLS,LEADS)");
 	    
 	    templatePS.println();
 	    templatePS.println("DISCRETE_TIME = 1");
 	    templatePS.println("qrows = HROWS * LEADS");
 	    templatePS.println("qcols = HCOLS - HROWS");
 	    templatePS.println();
 	    
	    templatePS.println("call conversionwrapper(HROWS, HCOLS, hmat, hmat, hmatj, hmati, ierr)");
 	    templatePS.println("MAXELEMS = " + numCols*NEq);
 	    templatePS.println("aux = 0");
 	    templatePS.println("rowsInQ = aux");
 	    templatePS.println("maxSize = MAXELEMS");
 	    templatePS.println();
 	    
 	    templatePS.println();
 	    templatePS.println("!You may wish to print out your sparse-format matrices.");
 	    templatePS.println("!Luckily, there's a routine to handle that, and a c-wrapper to call it.");
 	    templatePS.println("!Below is an example:");
 	    templatePS.println();
 	    templatePS.println("!call cprintsparsewrapper(HROWS,hmat,hmatj,hmati)");
 	    templatePS.println();
 	    
 	    
 	    templatePS.println();
 
 	    templatePS.println("call sparseamawrapper(maxSize, DISCRETE_TIME, HROWS, HCOLS, LEADS, hmat, hmatj, hmati, newHmat, newHmatj, newHmati,&\n aux, rowsInQ, qmat, qmatj, qmati, essential, rootr, rooti, retCode, aPointerToVoid)");
 
 	    templatePS.println();
 	    
 	    templatePS.println();
 	    templatePS.println("call obtainsparsewrapper(maxSize, qrows, qcols, qmat, qmatj, qmati, bmat, bmatj, bmati)");
 	    templatePS.println();
 	    
 	    templatePS.println();
	    templatePS.println("call csrdnswrapper(LEADS*HROWS,HCOLS,bmat,bmatj,bmati,bmat,ierr)");
 	    templatePS.println();
 	    
 	    templatePS.println();
 	    templatePS.println("STOP");
 	    templatePS.println("END PROGRAM simpleSparseAMAExample");
 	    templatePS.println();
 	    
 	    
 	    templatePS.close();
 	    } catch (Exception e) {
 		System.err.println("ERROR: " + e.getMessage());
 	    }
 	    
 	    try {
 	    int numCols=(getNLag()+1+getNLead())*NEq;
 	    parameterPS = new PrintStream(new FileOutputStream(parameterFileName));
 	    parameterPS.println("SUBROUTINE "+lcName + "_AMA_SetAllParams(params) !parameter file");
 	    parameterPS.println();
 	    parameterPS.println("IMPLICIT NONE");
 	    parameterPS.println("INTEGER :: MAXELEMS, HROWS, HCOLS, LEADS, qrows, qcols");
 	    parameterPS.println("INTEGER, DIMENSION(" + (numCols)*NEq + ") :: hmatj, hmati");
 	    parameterPS.println("REAL(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: hmat");
 	    
 	    parameterPS.println();
 	    parameterPS.println("INTEGER :: maxNumberOfHElements, aux, rowsinQ, essential, retCODE,i, maxSize, testnp");
 	    
 	    parameterPS.println();
 	    parameterPS.println("REAL(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: g, h");
 	    parameterPS.println("REAL(KIND = 8), DIMENSION(" + NCoeffs + ") :: params");
 	    parameterPS.println();
 	    
 	    parameterPS.println();
 	    parameterPS.println("REAL(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: newHmat, qmat, bmat, rootr, rooti");
 	    parameterPS.println("INTEGER(KIND = 8), DIMENSION(" + (numCols)*NEq + ") :: newHmatj, newHmati, qmatj, qmati, bmati, bmatj");
 	    parameterPS.println("INTEGER, dimension(:), allocatable :: aPointerToVoid");
 	    parameterPS.println("INTEGER :: DISCRETE_TIME, ierr");
 	    
 	    parameterPS.println();
 	    parameterPS.println("INTEGER :: rows, cols");
 	    parameterPS.println();
 	    
 	    parameterPS.println();
 	    parameterPS.println("! Below, all of the parameters used in the model are declared.");
 	    parameterPS.println("! However, the user should add to this list any intermediate parameters used in calculation.");
 	    parameterPS.println();
 	    
 	    for (i = 0; i < NCoeffs; i++) {
 		    parameterPS.println("REAL(KIND = 8) :: " + getCoefficients()[i]);
 		}
 	    
 	    parameterPS.println();
 	    parameterPS.println("!user must input coefficient values here");
 	    parameterPS.println("!Since this file gets overwritten each time the parser is called, the user");
 	    parameterPS.println("!may wish to assign these values in a separate routine.");
 	
 	    parameterPS.println("print *, \"SETTING PARAMS TO ZERO\"");
 	    parameterPS.println("print *, \"RENAME AND EDIT "+parameterFileName+"\"");
 	    parameterPS.println("!change the following print message to reflect your new file name");
 			    parameterPS.println("print *, \"USING "+lcName+"_AMA_SetAllParamsZero.f90 in parameter setting function call in "+templateFileName+"\"");
 	    for (i = 0; i < NCoeffs; i++) {
 	        parameterPS.println(getCoefficients()[i] + "=0 ");
 	    }
 	    parameterPS.println();
 	    
 	    parameterPS.println();
 	    for (i = 0; i < NCoeffs; i++) {
 	    parameterPS.println("params(" + (i+1) + ") = " + getCoefficients()[i]);
 	    }
 	    parameterPS.println();
 	    
 	    parameterPS.println();
 
 	    
 	    parameterPS.println();
 	    parameterPS.println("END SUBROUTINE " +lcName + "_AMA_SetAllParams ");
 	    parameterPS.println();
 	    
 	    
 	    parameterPS.close();
 	    } catch (Exception e) {
 		System.err.println("ERROR: " + e.getMessage());
 	    }
 	    
 	    
 	    try {
 		dataPS = new PrintStream(new FileOutputStream(dataFileName));
 
 		dataPS.println("int " + lcName + "_AMA_data(int param,np,char *modname,int eqtype,int endog,int delay,int vtype) {");
 		dataPS.println();
 		dataPS.println("// " + lcName + "_AMA_data()");
 		dataPS.println("//     This function will return various information about the AMA model,");
 		dataPS.println("//     but will not compute the G and H matrices.");
 		dataPS.println();
 		
 		dataPS.println("int delay[" + NEq + "][1];");
 		dataPS.println("int vtype[" + NEq + "][1];");
 		dataPS.println("int eqtype[" + NEq + "][1];");
 		dataPS.println();
 		dataPS.println("char modname[] = '" + Name + "';");
 		dataPS.println("int neq = " + NEq + ";");
 		dataPS.println("int np = " + NCoeffs + ";");
 		dataPS.println("int nlag = " + getNLag() + ";");
 		dataPS.println("int nlead = " + getNLead() + ";");
 		dataPS.println("int eqtype[" + NEq + "][1];");
 		dataPS.println();
 		
 		
 		for (i = 0; i < NEq; i++)
 		    dataPS.println("  eqname[" + (i+1) + "] = '" +
 				   getEquations()[i].getName() + "';");
 		dataPS.println("  eqname_ = char(eqname);");
 		dataPS.println();
 	
 		for (i = 0; i < NEq; i++) {
 		    dataPS.print(" eqtype[" + (i+1) + "][1] = " + getEquations()[i].getEqType() +
 				   ";   ");
 		    if (i % 3 == 2)
 		      dataPS.println();
 		}
 		if (i % 3 != 1)
 		  dataPS.println();
 		dataPS.println("  eqtype_ = eqtype;");
 		dataPS.println();
 	
 		for (i = 0; i < NCoeffs; i++)
 		dataPS.println("  param[" + (i+1) + "] = '" + getCoefficients()[i]
 			       + "';");
 		dataPS.println("  param_ = char(param);");
 		dataPS.println();
 	
 		for (i = 0; i < NVars; i++)
 		  dataPS.println("    endog[" + (i+1) + "] = '" +
 				 getVariables()[i].getName() + "';");
 		dataPS.println("  endog_ = char(endog);");
 		dataPS.println();
 	
 		for (i = 0; i < NVars; i++) {
 		  dataPS.print("  delay[" + (i+1) + "][1] = " + getVariables()[i].returnDelay() +
 			       ";   ");
 		  if (i % 3 == 2)
 		    dataPS.println();
 		}
 		if (i % 3 != 1)
 		  dataPS.println();
 		dataPS.println("  delay_ = delay;");
 		dataPS.print("\n");
 		
 		for (i = 0; i < NEq; i++) {
 		  dataPS.print("   vtype[" + (i+1) + "][1] = " + getVariables()[i].getDataType() +
 			       ";   ");
 		  if (i % 3 == 2)
 		    dataPS.println();
 		}
 		if (i % 3 != 1)
 		  dataPS.println();
 		dataPS.println("  vtype_ = vtype;");
 		dataPS.print("\n\n\n");
 	
 		dataPS.println();
 		dataPS.println("return(0);");
 		dataPS.println("}");
 		
 		dataPS.close();
 	    } catch (Exception e) {
 		System.err.println("ERROR: " + e.getMessage());
 	    }
 	
 	/*****************************************************************
 	  Now print out the function compute_AMA_matrices(). This function
 	  will compute the G and H matrices.  Actually this is a script not
 	  a function.  It is easier to deal with the parameters in Matlab if
 	  this part is a script since you don't have to declare them globals
 	  or reassign them inside the function.
 	******************************************************************/
 	
 	    try {
 	      matrixPS = new PrintStream(new FileOutputStream(matrixFileName));
 	  	int numCols=(getNLag()+1+getNLead())*NEq;
 	  	
 	      matrixPS.println("#include <math.h>");
    	        
 	  	
 	      matrixPS.println("void getnumrows_(int *rows)");
 	      matrixPS.println("{");
 	      matrixPS.println("*rows = " + NEq +";");
 	      matrixPS.println("}");
 	      
 	      
 	      matrixPS.println("void getnumcols_(int *cols)");
 	      matrixPS.println("{");
 	      matrixPS.println("*cols =" + numCols + ";");
 	      matrixPS.println("}");
 	      	      
 	      matrixPS.println("void getnumgcols_(int *cols)");
 	      matrixPS.println("{");
 	      matrixPS.println("*cols =" + NEq*(getNLag()+1) + ";");
 	      matrixPS.println("}");
 	     
 	      matrixPS.println("int " + lcName + "_AMA_matrices(double *paramvalues, double *g, double *h) {");
 	      matrixPS.println("//     This script will compute the G and H matrices.");
 	      matrixPS.println();
 	      
 	     
 
 	      
 	      for (i = 0; i < NCoeffs; i++) {
 	        
 	    	matrixPS.println("double " + getCoefficients()[i] + ";");
 	        matrixPS.println(getCoefficients()[i] + "= paramvalues[" + i + "];");
 	        }
 	      	matrixPS.println("");
 	        matrixPS.println();
 	        
 //	      matrixPS.println("double g[" + NEq + "*"+ (getNLag()+1)*NEq +"];");
 //	      matrixPS.println("double  h[" + NEq + "*"+ (getNLag()+1+getNLead())*NEq +"] ;");
 	      matrixPS.println();
 	      
 	      matrixPS.println("int i;");
     
 	      for (i = 0; i < NEq; i++) {
 		getEquations()[i].getLHS().PrintGMatrixEntries(this, i, AMAtoC.Left_Side,
 						     matrixPS);
 		getEquations()[i].getRHS().PrintGMatrixEntries(this, i, AMAtoC.Right_Side,
 						     matrixPS);
 		getEquations()[i].getLHS().PrintHMatrixEntries(this, i, AMAtoC.Left_Side,
 						     matrixPS);
 		getEquations()[i].getRHS().PrintHMatrixEntries(this, i, AMAtoC.Right_Side,
 						     matrixPS);
 	      }
 	
 	      matrixPS.println();
 	      
 	      matrixPS.println();
 		  matrixPS.println("return(0);");
 	      matrixPS.println("}");
 	      
 	      matrixPS.println("int parserwrapper_(double *params,double *g, double *h,double*hmat,int *HROWS,int *HCOLS, int *LEADS)");
 		    matrixPS.println("{");
 		    
 		    matrixPS.println(lcName + "_AMA_matrices(params,g,hmat);");
 		    matrixPS.println("int i;");
 		    matrixPS.println("int k;");
 		    
 		    matrixPS.println("int rows;");
 		    matrixPS.println("int cols;");
 		    matrixPS.println("int gcols;");
 		    
 		    matrixPS.println("rows = " + NEq +";");
 		    matrixPS.println("cols =" + numCols + ";");
 		      matrixPS.println("gcols =" + NEq*(getNLag()+1) + ";");
 		      matrixPS.println("*LEADS = " + getNLead() + ";");
 		      matrixPS.println("*HROWS = rows;");
 		      matrixPS.println("*HCOLS = cols;");
 		      
 		      
 		    matrixPS.println("//getnumrows_(&rows);");
 		    matrixPS.println("//getnumcols_(&cols);");
 		    matrixPS.println("//getnumgcols_(&gcols);");
 		    
 		    matrixPS.println("for(i = 0;i < gcols;i++){");
 		    matrixPS.println("for (k = 0;k < rows;k ++){");
 		    matrixPS.println("hmat[rows*i+k] = hmat[rows*i+k] + g[rows*i + k];");
 		    matrixPS.println(" }");
 		      
 		    matrixPS.println(" }");
 
 		    
 		    matrixPS.println("return(0);");
 		    
 		    matrixPS.println("}");
 	      
 	      matrixPS.close();
 	    } catch (Exception e) {
 	      System.err.println("ERROR: " + e.getMessage());
 	    }
 	  }
 
 }
 
