 public class Solution {
    
    
    
     private boolean isConnect(String src, String dest){
         char[]src_arr = src.toCharArray();
         char[]dest_arr = dest.toCharArray();
         int misMatch = 0;
         for(int i=0; i<src.length(); ++i){
             if(src_arr[i]!=dest_arr[i])
                 misMatch +=1;
         }
         return (misMatch == 1);
     }
 
     private boolean[][] buildAdjMap(String[] arr){
        int len = arr.length();
        boolean[][] adjMap = new boolean [len][len]
         for(int i = 0; i< len; ++i){
             adjMap[i][i] = false;
             for(int j = 0; j< len; ++j){
                 boolean val = isConnect(arr[i], arr[j]);
                 adjMap[i][j] = val;
                 adjMap[j][i] = val;
             }
         }
     }
         
     public int ladderLength(String start, String end, HashSet<String> dict) {
         if(dict == null || dict.isEmpty() )
             return 0;
         dict.add(start);
         dict.add(end);
         String[] strs = new String[dict.size()];
         int i = 0;
         int start_index = 0;
         for(String key:dict){
             strs[i] = key;
            if(key == start){
                 start_index = i;
            }  
         }
         boolean[][] adj = buildAdjMap(strs);
         int[] res = new int[1];
         res[0] = Integer.MAX_VALUE;
         Set<String> visited = new HashSet<String>();
        dfs(strs,visited,adj,start_index,end,0,res);
         if(res[0] == Integer.MAX_VALUE)
             res[0] = 0;
         return res[0]; 
     }
     private void dfs(String [] arr, Set<String> visited, boolean[][] adj, int start,
     String end, int step, int [] res){
         //find the end        
        if(arr[start] == end){
           if(step < res[0])
              res[0] = step;
              return; 
         }
        for(int i = 0; i<arr.length();++i){
             if(visited.contains(arr[i]) ||
                     !adj[start][i]){
                 continue;
             }
             visited.add(arr[i]);
             dfs(arr,visited,adj,i,end,step+1,res);
             visited.remove(arr[i]);
         }
    
     }
 }
