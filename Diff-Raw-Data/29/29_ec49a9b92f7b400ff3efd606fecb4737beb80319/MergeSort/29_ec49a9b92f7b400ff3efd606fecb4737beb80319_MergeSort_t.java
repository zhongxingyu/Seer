 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public class MergeSort {
 	

 	public static void sort(int a[]) {
 		int n = a.length;
		for (int sz = 1; sz < n ; sz += sz)
			for (int lo = 0; lo < n -sz; lo += sz + sz)
				merge(a, lo, lo + sz -1, Math.min(lo + sz + sz, n));
 	}
 	
 	
 	public static void merge(int a[], int lo, int mid, int hi) {
 		int i = lo; 
 		int j = mid+1;
 		int k = hi;
		int b[] = new int[j - i];
 		int p = 0;
 		System.arraycopy(a, i, b, 0, j - i);
 		while (i < k) {
 			if (a[j] < b[p])
 				a[i++] = a[j++];
 			else
 				a[i++] = b[p++];
 			if (j == k)	break;
 			if (i == j)	break;
 		}
 		System.arraycopy(b, p, a, i, b.length - p);
 	}
 	
 	public static void main(String[] args) throws IOException {
 		Scanner in = new Scanner(new File("inMergeSort.txt"));
 		PrintWriter out = new PrintWriter(new File("outMergeSort.txt"));
 		int n = in.nextInt();
 		int a[] = new int[n];
 		for (int i = 0; i < n; i++)
 			a[i] = in.nextInt();
 		sort(a);
 		for (int t: a)
 			out.print(t + " ");
 		in.close();
 		out.close();
 	}
 }
