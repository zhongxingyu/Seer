 public class MatrixOperations
 {
 	public static boolean echelonCheck(double [][] matrix)
 	{
 		int matrixx = matrix.length;
 		int matrixy = matrix[0].length;
 		int a;
 		int count = 0;
 		int leadingEntryCompare[] = new int [matrixx];
 		boolean check = false;
 		for (int i=0;i<matrixx;i++)
 		{
 			a = 0;
 			for (int j=0;j<matrixy;j++)
 			{
 				if (matrix[i][a] == 0)
 				{
 					a = a + 1;
 				}
 			}
 			leadingEntryCompare[i] = a;
 		}
 		for (int i = 0;i<matrixx-1;i++)
 		{
 			if (leadingEntryCompare[i] < leadingEntryCompare[i+1])
 			{
 				count = count + 1;
 			}
 		}
 		if (count == matrixx-1)
 		{
 			check = true;
 		}
 		else
 		{
 			check = false;
 		}
 		return check;
 	}
 	public static boolean reducedEchelonCheck(double [][] matrix)
 	{
 		int matrixx = matrix.length;
 		int matrixy = matrix[0].length;
 		int a;
 		int count = 0;
 		int leadingEntryCompare[] = new int [matrixx];
 		boolean check = false;
 		boolean leading = true;
 		for (int i=1;i<matrixx;i++)
 		{
 			a = 0;
 			leading = true;
 			for (int j=0;j<matrixy;j++)
 			{
 				if (matrix[i][j] != 0 && leading == true)
 				{
 					if (i > 0)
 					{
 						if (matrix[i-1][j] == 0)
 						{
 							a = a + 1;
 							leading = false;
 						}
 					}
 				}
 			}
 			leadingEntryCompare[i] = a;
 		}
 		for (int i = 0;i < matrixx-1;i++)
 		{
 			if (leadingEntryCompare[i] < leadingEntryCompare[i+1])
 			{
 				count = count + 1;
 			}
 		}
 		if (count == matrixx-1)
 		{
 			check = true;
 		}
 		else
 		{
 			check = false;
 		}
 		return check;
 	}
 	public static double [][] echelonTransform(double [][] matrix)
 	{
 		int matrixx = matrix.length;
 		int matrixy = matrix[0].length;
 		int column = 0;
 		while(!echelonCheck(matrix)&&column<matrixy&&!(matrixy>matrixx))
 		{
 			for(int i=column;i<matrixx;i++)
 			{
 				if(matrix[i][column]!=0)
 				{
 					matrixRowMulti(i,(1/matrix[i][column]),matrix);
 				}
 			}
 			for(int i=1+column;i<matrixx;i++)
 			{
 				if(leadingEntryPos(column,matrix)>leadingEntryPos(i,matrix))
 				{
 					matrixRowSwitch(column,i,matrix);
 				}
 			}
 			for(int i=1+column;i<matrixx;i++)
 			{
 				if(matrix[i][column]!=0)
 				{
 					matrixRowsMultiAdd(i,column,-1,matrix);
 				}
 			}
 			column = column + 1;
 		}
 		return matrix;
 	}
 	private static double [][] matrixRowSwitch(int row1, int row2, double [][] matrix)
 	{
 		int matrixy = matrix[0].length;
 		double [] temp = new double [matrixy];
 		for(int i=0;i<matrixy;i++)
 		{
 			temp[i] = matrix[row1][i];
 			matrix[row1][i]=matrix[row2][i];
			matrix[row2][i]=matrix[row1][i];
 		}
 		return matrix;
 	}
 	private static double [][] matrixRowMulti(int row, double multi, double [][] matrix)
 	{
 		int matrixy = matrix[0].length;
 		for (int i = 0;i < matrixy;i++)
 		{
 			matrix[row][i] = multi * matrix[row][i];
 		}
 		return matrix;
 	}
 	private static double [][] matrixRowsMultiAdd(int row1, int row2, double multi, double [][] matrix)
 	{
 		int matrixy = matrix[0].length;
 		for (int i = 0;i < matrixy;i++)
 		{
 			matrix[row1][i] = matrix[row1][i] + (multi * matrix[row2][i]);
 		}
 		return matrix;
 	}
 	private static int leadingEntryPos(int row, double [][] matrix)
 	{
 		int pos = 0;
 		for (int j=0;j<matrix[0].length;j++)
 		{
 			if (matrix[row][pos] == 0)
 			{
 				pos = pos + 1;
 			}
 		}
 		return pos;
 	}
 	public static double [][] matrixMultiplier(double [][] matrix1, double [][] matrix2)
 	{
 		int matrix1x = matrix1.length;
 		int matrix1y = matrix1[0].length;
 		int matrix2x = matrix2.length;
 		int matrix2y = matrix2[0].length;
 		double sum[][] = new double [matrix1x][matrix2y];
 		if (matrix1y == matrix2x)
 		{
 			for(int i=0;i<matrix2y;i++)
 			{
 				for(int j=0;j<matrix1x;j++)
 				{
 					for(int k=0;k<matrix1y;k++)
 					{
 						sum[j][i] = sum[j][i] + (matrix1[j][k] * matrix2[k][i]);
 					}
 				}
 			}
 		}
 		return sum;
 	}
 }
