 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.math.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 /**
  * 
  */
 
 /**
  * @author sebek
  *
  */
 
 public class Calculation {
 	private String filePathName;	
 	private Integer NumberOfColumns;
 	private Integer NumberOfFields;
 	private Integer NumberOfPolinomes;
 	private Integer polinomesVector[];
 	private Double  meaningAtPoints[];
 	private Double  inputMatrix [][];
 	private Double polinomesResults[];
 	private Double  error;
 
 
 	public Calculation(String fileP, Integer numberOfX, Integer polinom) throws IDENTException {
 		filePathName = fileP;
 		NumberOfColumns = numberOfX;	
 		NumberOfFields  = calculateNumberOfFieldsInFile();
 		NumberOfPolinomes = polinom;
 
 		polinomesVector = new Integer[polinom];
 		polinomesResults = new Double[NumberOfPolinomes];
 
 		for (int i = 0; i < polinom; i++)
 			polinomesVector[i] = i;
 
 		inputMatrix = new Double [NumberOfFields ][];
 		for (int i = 0; i < NumberOfFields; i++)
 		{
 			inputMatrix[i] = new Double [NumberOfColumns];
 			for (int j = 0; j < NumberOfColumns; j++)
 				inputMatrix[i][j] = new Double(0);			
 		}
 		fillInputMatrix();			
 		for (int i = 0; i < inputMatrix.length; i++)
 		{
 			for(int j = 0; j < inputMatrix[i].length; j++)
 				System.out.print(" "+ inputMatrix[i][j]);
 			System.out.println();
 		}
 		generalUnsverFinder();
 		System.out.println("Results :");
 		for (int i = 0; i < NumberOfPolinomes; i++)
 			System.out.printf("c(%d)=%-10.2g%c",i,polinomesResults[i],((i+1)%5==0 ? '\n':' '));
 
 		System.out.println();
 		System.out.printf("error=%-11.2g\n",error);
 
 		for (int i = 0; i < NumberOfFields; i++ )
 			System.out.printf("ff(%d)=%-10.2g%c",i, meaningAtPoints[i],((i+1)%5==0 ? '\n':' '));
 
 	}
 
 	public void fillInputMatrix() throws IDENTException
 	{
 		int n = 0;
 		int i = 0;
 		int j = 0;
 		
 		Scanner inpfile;	
 		try {
 			inpfile = new Scanner(
 					new BufferedReader(new FileReader(filePathName)));
 
 			while (inpfile.hasNext()) 
 			{
 
 				if (n < NumberOfColumns ) 
 				{
 					inputMatrix[i][n] = new Double(inpfile.next());
 					n++;
 				}
 				else
 				{
 					n = 0;
 					i++;
 					inputMatrix[i][n] = new Double(inpfile.next());
 					n++;
 				}
 
 			}
 		}
 		catch (Exception e) {
 			// TODO: handle exception
 			throw new IDENTException("File not found");
 		}
 
 	}
 
 	public Integer calculateNumberOfFieldsInFile() throws IDENTException
 	{
 		int i = 0;
 		Scanner inpfile;	
 		try {
 			inpfile = new Scanner(
 					new BufferedReader(new FileReader(filePathName)));
 
 			while (inpfile.hasNext()) 
 			{
 				inpfile.next();
 				i++;
 			}
 		}
 		catch (Exception e) {
 			// TODO: handle exception
 			throw new IDENTException("File not found");
 		}
 		if (i % NumberOfColumns != 0)
 			throw new IDENTException("Icorect data!");
 		return i/ NumberOfColumns;
 	}
 
 	int abba (Integer j, Integer i)
 	{
 		int i1, i2, i4;
 		int s1;
 		i2 = polinomesVector[j];
 		s1 = 1;
 		if (i2 == 0)
 			return(s1);
 		i1 = 1;
 		while (i1 != 0 )
 		{
 			i1 = i2 / 10;
 			i4 = i2 - 10 * i1 -1;
 			s1 *= inputMatrix[i][i4];
 			if (i1 == 0)
 				return(s1);
 			i2 = i1;
 		}
 		return s1;
 	}
 
 	private void generalUnsverFinder() throws IDENTException
 	{
 		int i,j,k;
 
 		Double localPolinomesMatrix [][] = new Double[NumberOfPolinomes][NumberOfPolinomes];
 
 		Double localIV[] = new Double[NumberOfPolinomes];
 		Double localPolynomesVector[] = new Double[NumberOfPolinomes];
 		for (i = 0 ;i< NumberOfPolinomes; i++ ) {
 			localPolynomesVector[i] = new Double(0);
 			localIV[i] = new Double(0);
 			localPolinomesMatrix[i] = new Double[NumberOfPolinomes];
 			for (j=0 ;j < NumberOfPolinomes;j++)
 				localPolinomesMatrix[i][j] = 0.0;
 		}
 
 		for (k = 0 ;k < NumberOfPolinomes; k++ )
 			for (j = 0 ;j <= k;j++ )
 				for (i = 0;i < NumberOfPolinomes;i++ )
 					localPolinomesMatrix[j][k] += abba(k,i) * abba(j,i);
 
 		for (k = 0; k < NumberOfPolinomes -1 ;k++ )
 			for (j = k + 1; j < NumberOfPolinomes;j++ )
 				localPolinomesMatrix[j][k] = localPolinomesMatrix[k][j];
 
 		for (k = 0 ;k < NumberOfPolinomes; k++ )
 			for (i = 0; i < NumberOfFields; i++ )
 				localPolynomesVector[k] += inputMatrix[i][NumberOfColumns - 1] * abba(k,i);
 
 		crout2(localPolinomesMatrix,localPolynomesVector, NumberOfPolinomes, 0, localIV);
 		Double localEs;
 		error = 0.0;
 		meaningAtPoints = new Double[NumberOfFields]; 
 		for ( i = 0; i < NumberOfFields; i++ ){
 			localEs = 0.0;
 			for (j = 0; j < NumberOfPolinomes; j++ )
 				localEs += polinomesVector[j] * abba(j,i);
 			meaningAtPoints[i] = new Double (localEs);
 			error += (inputMatrix[i][NumberOfPolinomes -1] - localEs) * (inputMatrix[i][NumberOfPolinomes -1] - localEs);
 		}
 		error = Math.sqrt(error/NumberOfFields);
 	}
 
 	private void crout2 (Double as[][], Double bs[], Integer n, Integer ir, Double iv[]) throws IDENTException
	//double as[IM][IM], *bs,*ys;
	//int n,ir,*iv;
 	{
 		int i, k, imax = 0, p;
 		double A[][] = new double[NumberOfPolinomes][NumberOfPolinomes];
 		double b[] = new double [NumberOfPolinomes];
 		double y[] = new double [NumberOfPolinomes];
 		double t,s;
 
 		for (int j = 0; j < NumberOfPolinomes; j++)
 		{
 			A[j] = new double[NumberOfPolinomes];
 			b[j] = 0;
 			for (int z = 0; z < NumberOfPolinomes; z++)
 			{
 				A[j][z] = 0;
 			}
 		}
 

 		for (i = 0; i < n; i++ )
 			b[i]=bs[i];
 
 		if (ir == 1) 
 		{
 			for (k=0; k<n; k++)
 			{
 				t = b[(int) Math.floor(iv[k])];
 				b[(int) Math.floor(iv[k])] = b[k];
 				b[k] = t;
 				s = 0;
 				for (p = 0; p < k; p++)
 					s += A[k][p] * b[p];
 				b[k] -= s;
 			}      
 		}
 		else
 		{
 			for (i = 0; i < n; i++ )
 				for (k = 0; k < n; k++ )
 					A[i][k] = as[i][k];
 
 			for (k = 0; k < n; k++ )
 			{
 				t = 0;
 				for (i = k; i < n; i++ )
 				{
 					s = 0;
 					for ( p = 0; p < k; p++ )
 						s += A[i][p] * A[p][k];
 
 					A[i][k] -= s;
 
 					if (Math.abs(A[i][k]) > t ) 
 					{
 						t = Math.abs(A[i][k]);
 						imax = i;
 					}
 				}
 
 				iv[k] = imax * 1.0;
 
 				if (imax != k )
 				{
 					for (i = 0; i < n; i++)
 					{
 						t = A[k][i];
 						A[k][i] = A[imax][i];
 						A[imax][i] = t;
 					}
 
 					t = b[k];
 					b[k] = b[imax];
 					b[imax] = t;
 				}
 				if (A[k][k] == 0) {
//					System.out.println("error det=0\n");
//					throw new IDENTException("division by zero");
					t = 0;
 				}
 				else
 				 t = 1 / A[k][k];
 
 				for (i = k + 1; i < n; i++)
 					A[i][k] *= t;
 
 				for (i = k + 1; i < n; i++)
 				{
 					s = 0;
 					for (p = 0; p < k ; p++) 
 						s += A[k][p] * A[p][i];
 
 					A[k][i] -=s;
 				} 
 
 				s = 0;
 
 				for (p = 0; p < k; p++)
 					s += A[k][p] * b[p];
 
 				b[k] -=s;
 			}
 
 			for (k = n - 1; k >= 0; k--)
 			{
 				s = 0;
 				for (p = k + 1; p < n; p++)
 					s += A[k][p] * y[p] / A[k][k];
 
 				y[k] = b[k] / A[k][k]- s;
 			}
 			for (k = 0; k < NumberOfPolinomes; k++)
 				polinomesResults[k] = y[k] /*1.0000001*/;
 		}
 
 	}
 
 }
