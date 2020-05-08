 import java.io.*;
 
 class MultipleLongInt
 {
 	public static void main(String args[])
 	{
 		System.out.println("cool");
		int[] A = {9, 9, 9};
		int[] B = {9, 9, 9};
 		int[] S = Multiply4(A, B);
         for(int i=0; i<S.length; i++)
             System.out.print("["+S[i]+"]");
 	}
 	public static int[][] Multiply(int[] A1, int[] A2)
 	{
 		int[][] S = null;
 		if( A1 != null && A2 != null)
 		{
 			int len1 = A1.length;
 			int len2 = A2.length;
 			S = new int[len2][len2+len1];
 			int[] total = new int[len1+len2];
 			for(int i=0; i<len1; i++)
 				for(int j=0; j<len1+len2; j++)
 					S[i][j]=0;
 			int c=0;
 			int mod=0;
 			for(int i=0; i<len2; i++)
 			{
 				int j=0;
 				c = 0;
 				mod=0;
 				for(j=0; j<len1; j++)
 				{
 					mod = (c + A2[len2-1-i]*A1[len1-1-j])%10;
 					c = 	(c + A2[len2-1-i]*A1[len1-1-j])/10;
 					S[i][i+j] = mod;
 				}
 				if( c>0 )
 				{
 					S[i][i+j] = c;
 				}
 			}
 
 			c = 0;
 			int i=0;
 			for(i=0; i<len1+len2; i++)
 			{
 				int sum=0;
 				mod = 0;
 				for(int j=0; j<len2; j++)
 				{
 						sum += S[j][i];
 				}
 				mod = (c + sum)%10;
 				c   = (c + sum)/10;
 				total[i] = mod;	
 			}
 			if(c>0)
 				total[i]=c;	
 
 			
 			int len3 = total.length; 
 			for(i=len3-1; i>=0 && total[i]==0; i--)
 				;
 			for(int k=0; k<(i+1)/2; k++)
 			{
 				int tmp = total[k];
 				total[k] = total[(i+1)-1-k];
 				total[(i+1)-1-k] = tmp;
 			}
 			
 
 			for(i=0; i<len1+len2+1; i++)
 			{
 				System.out.print("["+total[i]+"]");
 			}
 			System.out.println();
 			
 			for(i=0; i<len2; i++)
 			{ for(int j=0; j<(len2+len1); j++)
 					System.out.print(S[i][j]+" ");
 				System.out.println();
 			}
 		}
 		return S;
 	}
     public static void Reverse(int[] A)
     {
         if(A != null)
         {
             int len = A.length;
             for(int i=0; i<len/2; i++)
             {
                 int tmp = A[i];
                 A[i] = A[len-1-i];
                 A[len-1-i] = tmp;
             }
         }
     }
     public static int[] Multiply2(int[] A, int[] B)
     {
         int[][] C = null;
         int[] total = null;
         if( A != null && B != null)
         {
             int len1 = A.length;
             int len2 = B.length;
             C = new int[len1][len1+len2];
 
             Reverse(A);
             Reverse(B);
             for(int i=0; i<A.length; i++)
             {
                 int carry = 0;
                 int j = 0;
                 for(j=0; j<B.length; j++)
                 {
                     C[i][j+i] = (B[j]*A[i] + carry)%10; 
                     carry = (B[j]*A[i] + carry)/10;
                 }
                 C[i][j+i] = carry;
             }
             for(int i=0; i<C.length; i++)
             {
                 for(int j=0; j<C[0].length; j++)
                 {
                     System.out.print("["+C[i][j]+"]");
                 }
                 System.out.println();
             }
 
             total = new int[len1+len2];
             int car = 0;
             for(int i=0; i<C[0].length; i++)
             {
                 int s = 0;
                 for(int j=0; j<C.length; j++)
                 {
                     s += C[j][i];     
                 }
                 total[i] = (s + car)%10; 
                 car = (s + car)/10;
             }
             Reverse(total);
         }
         return total;
     }
     public static int[] Multiply3(int[] A, int[] B)
     {
         int[][] C = null;
         int[] total = null;
         if( A != null && B != null)
         {
            C = new int[A.length+B.length][B.length]; 
            Reverse(A);
            Reverse(B); 
            //for(int i=0; i<A.length+B.length; i++)
            {
                for(int j=0; j<B.length; j++)
                {
                    int carry = 0;
                    for(int k=0; k<A.length; k++)
                    {
                         C[k+j][j] = (A[k]*B[j]+carry)%10;
                         carry = (A[k]*B[j]+carry)/10;
                    }
                }
            }
            total = new int[A.length+B.length];
            int carry = 0;
            for(int i=0; i<C.length; i++)
            {
                int s=0;
                for(int j=0; j<C[0].length; j++)
                {
                   s += C[i][j];
                }
                total[i] = (s+carry)%10; 
                carry = (s+carry)/10;
            }
 
            for(int i=0; i<C.length; i++)
            {
                for(int j=0; j<C[0].length; j++)
                {
                    System.out.print("["+C[i][j]+"]");
                }
                System.out.println();
            }
         }
         Reverse(total);
         return total;
     }
     public static int[] Multiply4(int[] A, int[] B)
     {
         int[][] c = null;
         int[] total = null;
         if( A != null && B != null)
         {
             if(A.length > 0 && B.length > 0)
             {
                 if(A.length == 1 && A[0] == 0 || B.length == 1 && B[0] == 0)
                 {
                    total = new int[1];
                     total[0] = 0;
                 }
                 else
                 {
                     int lena = A.length;
                     int lenb = B.length;
                     c = new int[lenb][lena+lenb];
                     for(int i=0; i<lenb; i++)
                     {
                         int carry = 0;
                         int j=0;
                         for(j=0; j<lena; j++)
                         {
                            c[lenb-1-i][lena+lenb - 1- j-i] = (A[lena - 1 - j]*B[lenb - 1 - i] + carry)%10;
                             carry = (A[lena - 1 - j]*B[lenb - 1 - i] + carry)/10;
                         }
                        c[lenb-1-i][lena+lenb-1-j-i] = carry;
                     }
                     int numRow = c.length;
                     int numCol = c[0].length;
                     total = new int[numCol];
                     int carry = 0;
                     for(int i=0; i<numCol; i++)
                     {
                         int s=0;
                         int j=0;
                         for(j=0; j<numRow; j++)
                         {
                             s += c[numRow-1-j][numCol-1-i];
                         }
                         total[numCol-1-i] = (s+carry)%10;
                         carry = (s+carry)/10;
                     }
                 }
             }
         }
         return total;
     }
 }
