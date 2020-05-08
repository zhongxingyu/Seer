 public class Solution {
     public int uniquePaths(int m, int n) {
         if(m <=0 || n <=0)
             return -1;
 
         int [][] paths = new int [2][n];
         for(int i = 0; i<2; ++i)
             paths[i] = new int[n];
         for(int i =0; i < n ; ++i)
             paths[0][i] = 1;
 
         for(int i = 1; i<m; ++i){
             for(int j = 0; j<n; ++j){
                 paths[i%2][j] = 0;
                 for(int k = 0; k<=j;++k)
                     paths[i%2][j] += paths[(i-1)%2][k];
 
             }
         }
         return paths[(m-1)%2][n-1];
     }
 
     public int uniquePathsWithObstacles(int[][] obstacleGrid) {
 
         if(obstacleGrid == null || obstacleGrid.length == 0)
             return -1;
         if(obstacleGrid[0] == null || obstacleGrid[0].length == 0)
             return -1;
         int m = obstacleGrid.length;
         int n = obstacleGrid[0].length;
 
         int [][] paths = new int [2][n];
         for(int i = 0; i<2; ++i)
             paths[i] = new int[n];
        //i == 0 will do early break
         for(int i =0; i < n ; ++i){
             if(obstacleGrid[0][i] != 1)
                 paths[0][i] = 1;
            else
                break;
         }
        //bug1: not consider horizonal direction
        //bug2: not consider horizonal direction can be affected by max obstacle
         for(int i = 1; i<m; ++i){
            int maxBlock = -1;
             for(int j = 0; j<n; ++j){
                 paths[i%2][j] = 0;
                if(obstacleGrid[i][j] == 1){
                    maxBlock = j;
                     continue;
                }
                 for(int k = 0; k<=j;++k)
                    if(k >maxBlock)
                        paths[i%2][j] += paths[(i-1)%2][k];
             }
         }
         return paths[(m-1)%2][n-1];
 
     }
 }
 
