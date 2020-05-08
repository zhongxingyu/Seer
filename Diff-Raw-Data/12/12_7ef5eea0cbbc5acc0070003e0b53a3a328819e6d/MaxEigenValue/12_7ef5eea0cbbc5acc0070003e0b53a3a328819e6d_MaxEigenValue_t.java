 package nikitin.numanalysis.task12;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.Locale;
 import java.util.Scanner;
 
 /**
  * Created by kilok_000 on 09.04.14.
  */
 public class MaxEigenValue {
     private static PrintStream out;
     private static Scanner in;
     private static double eps = 0.000001;
     public static void main(String[] args)
     {
         Locale.setDefault(Locale.ENGLISH);
         out = System.out;
         try {
             in = new Scanner(new FileInputStream("config/EigenValues.txt"));
         } catch (FileNotFoundException e) {
             out.println("File not found!");
             e.printStackTrace();
         }
 
        out.println("Степенной метод поиска максимального по модулю собственного числа");
         String line = in.nextLine();
         String[] words = line.split(" ");
         int n = words.length;
 
         double[][] A = new double[n][n];
         for (int j = 0; j < n; j++) A[0][j] = Double.valueOf(words[j]);
         for (int i = 1; i < n; i++) {
             for (int j = 0; j < n; j++) {
                 A[i][j] = in.nextDouble();
             }
         }
         out.println("Матрица A:");
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n - 1; j++)
                 out.printf("%8.4f ", A[i][j]);
             out.printf("%8.4f \n", A[i][n - 1]);
         }
         double lambda = getMaxEigenValue(A);
         out.printf("λ=%8.6f\n", lambda);
     }
     private static double getMaxEigenValue(double[][] a)
     {
         double lambda=0;
         int k=0;
         int n=a.length;
         double[] y=new double[n];
         double[] t=new double[n];
         t[0]=1;
        out.print("Начатьный вектор: [");
        for(int i=0;i<n-1;i++) out.printf("%8.6f, ", t[i]);
        out.printf("%8.6f]\n",t[n-1]);
         double error;
         do{
             k++;
             double[] temp=t;t=y;y=temp;
             for(int i=0;i<n;i++) t[i]=0;
             for(int i=0;i<n;i++)
                 for(int j=0;j<n;j++) t[i]+=a[i][j]*y[j];
             lambda=t[0]/y[0];
            double[] b = new double[n];
            for(int i=0;i<n;i++) b[i]=t[i]/y[i];
            out.print(k+"-я итерация: "+"y["+(k+1)+"]/y["+k+"]=[");
            for(int i=0;i<n-1;i++) out.printf("%8.6f, ", b[i]);
            out.printf("%8.6f]\n",b[n-1]);
             error=getError(t,y);
            for(int i=n-1;i>=0;i--) t[i]/=t[0];
         } while(error>eps);
         out.println(k+" итераций");
         return lambda;
     }
     private static double getError(double[] t,double[] y)
     {
         int n=t.length;
         double[] a = new double[n];
         for(int i=0;i<n;i++) a[i]=t[i]/y[i];
         double max=0;
         for(int i=0;i<n;i++) if(Math.abs(a[i]-a[0])>max) max=Math.abs(a[i]-a[0]);
         return max;
     }
 
 }
