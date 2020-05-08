 public class Solution {
     public int maximalRectangle(char[][] matrix) {
         
     	if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
     		return 0;
     	
     	int height = matrix.length;
     	int width = matrix[0].length;
     	
     	int [][] dp = new int [height][width];
     	for(int i = 0; i< height; ++i)
     		dp [i] = new int [width];
     	
     	for(int i = 0; i<height; ++i)
     		for(int j = 0; j<width; ++j)
     			dp[i][j] = 1;
 
     	int max = 0;
     	for(int h = 0; h<width; ++h){
     		for(int x = 0; x+h < height; ++x){
     			int last = -1;
     			int maxWidth = 0;
     			for(int y = 0; y<width; ++y){
    				if(dp[x][y] == 1 && matrix[x+h][y] == '1'){
     					if(last == -1)
     						last = y;
     					dp[x][y] = 1;
     					maxWidth = (maxWidth > y-last+1)?(maxWidth):(y-last+1);
     				}
     				else{
     					last = -1;
     					dp[x][y] = 0;
     				}
     			}
     			max = (max > maxWidth*(h+1))?(max):(maxWidth*(h+1));
     		}
     	}
     	return max;
     }
 }
