 import java.util.Scanner;
 public class Intermediate140
 {
 	public static void main(String[] args)
 	{
 		Scanner s = new Scanner(System.in);
 		String[] dimensions = s.nextLine().split(" ");
 		int N = Integer.parseInt(dimensions[0]), M = Integer.parseInt(dimensions[1]);
 		int[][] matrix = new int[N][N];
 		for(int i = 0 ; i < M; ++i)
 		{
 		  String line = s.nextLine();
		  String[] rows = line.substring(0, line.indexOf(" ->")).split(" ");
      String[] adj = line.substring(line.indexOf(">")+2).split(" ");
      for(String rr : rows) {
        int row = Integer.parseInt(rr);
        for(String ss : adj)
          matrix[row][Integer.parseInt(ss)] = 1;
      }
 		}
 		for(int i = 0 ; i < N; ++i) {
 		  for(int j = 0 ; j < N ; ++j)
 		    System.out.print(matrix[i][j]);
 		  if(i != N-1) System.out.println();
 		}
 	}
 }
