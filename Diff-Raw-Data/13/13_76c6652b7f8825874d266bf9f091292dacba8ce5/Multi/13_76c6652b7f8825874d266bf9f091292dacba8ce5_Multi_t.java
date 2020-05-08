 public class Multi {
 
 	public static void main(String[] args){
 		int [][] a = {	{ 1,  2,  3,  4,  5},
 						{ 6,  7,  8,  9, 10},
 						{11, 12, 13, 14, 15}};
 
 		// System.out.println("Sum of the row " + sumRow(0,a));
 		// System.out.println("Sum of the colum " + sumCol(0,a));
 		// System.out.println("Sum of total " + sumTotal(a));
 		// a[0] = reverseRow(0,a);
 		// System.out.println("Reversed row ");
 		// print(a);
 
		int[][] b = snakeDraft(10,3);
		print2(a);
 		int [] r = {1,2,3,4,5,6};
 		int[][] k = rectangularize(r,3,2);
 		System.out.println("rectangularize");
		print2(k);
 
 	}
 	
 	public static void print(int[][] a){
 		for (int r=0; r<a.length; r++) {
 			for (int c=0; c<a[r].length; c++) {
 				System.out.print(a[r][c]+" ");
 			}
 			System.out.println();
 		}
 	}
 
 	public static int sumRow(int n, int [][]a){
 		int sum=0;
 		for (int i=0; i<a[n].length; i++) {
 			sum = sum + a[n][i];
 		}
 		return sum;
 	}
 
 	public static int sumCol(int n, int [][]a){
 		int sum=0;
 		for (int i=0; i<a.length; i++) {
 			sum = sum + a[i][n];
 		}
 		return sum;
 	}
 
 	public static int sumTotal(int[][] a){
 		int sum=0;
 		for (int i=0; i<a.length; i++) {
 			sum = sum + sumRow(i, a);
 		}
 		return sum;
 	}
 
 	public static int[] reverseRow(int n, int [][]a){
 		int [] b = {0, 0, 0, 0, 0};
 		for (int i=0; i<a[n].length; i++) {
 			b[i] = a[n][(a.length+1) - i];
 		}
 		return b;
 	}
 
 	public static int[] reverseCol(int n, int [][]a){
 		int[] b = {0, 0, 0};
 		for (int i=0; i<a.length; i++) {
 			b [i] = a[a.length - i][n];
 		}
 		return b;
 	}
 
 	public static int[][] snakeDraft(int players, int rounds){
 		int[][] b = new int[rounds][players];
 		int counter =1;
 		for (int irow=0; irow<b.length; irow++) {
 			if (irow%2==0) {
 				for (int icol=0; icol<b[irow].length; irow++) {
 					b[irow][icol]= counter;
 					counter+=1;
 				}
 			}else{
 				for (int icol=b[irow].length; icol>=0; icol--) {
 					b[irow][icol]=counter;
 					counter+=1;
 				}
 			}
 		}
 		return b;
 	}
 
 	public static int[][] rectangularize(int[] r, int rows, int cols){
 		int[][] t5= new int[rows][cols];
 		int count = 0;
		for (int i=0; i<t5.length; i++) {
 			for (int j=0; j<t5[i].length; j++) {
 				if (count<r.length) {
 					t5[i][j] = r[count];
 					count+=1;
 				} else{
 					t5[i][j]=0;
 				}
 			}
 		}
 		return t5;
 	}
 
	public static boolean print2(int[][] a){
 		for (int r=0; r<a.length; r++) {
 			for (int c=0; c<a[r].length; c++) {
 				System.out.print(a[r][c] + " ");
 			}
 			System.out.println();
 		}
 		return true;
 	}
 }
