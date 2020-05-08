 public class Solution {
     private int getX (int index, int width){
         return index%width; 
     }
 
     private int getY (int index, int width){
         return index/width; 
     }
     private int getIndex (int x, int y, int width)
     {
         return y*width+x;
     }
 
     public void solve(char[][] board) {
         // Start typing your Java solution below
         // DO NOT write main() function
         Set<Integer> whiteList = new HashSet<Integer>();     
         List<Integer> starts = new LinkedList<Integer>(); 
         
         if (board == null || board.length ==0 || board[0].length==0)
             return;
 
         int height = board.length;
         int width =  board[0].length;
        //find all starting points of 'O' on the boarder 
         for (int i=0; i < width; i++) {
             if(board[0][i] == 'O'){
                 starts.add(getIndex(i,0,width));         
             }
             if(board[height-1][i] == 'O'){
                 starts.add(getIndex(i,height-1,width));
             }         
         }
         for (int i=0; i < board.length; i++) {
             if(board[i][0] == 'O'){
                 starts.add(getIndex(0,i, width));         
             }
             if(board[i][width-1] == 'O'){
                 starts.add(getIndex(width-1,i,width));
             }         
         }
 
 
         for (Integer startIndex: starts) {
             if(whiteList.contains(startIndex)) {
                 continue;
             }
             LinkedList<Integer> myQueue = new LinkedList<Integer>();
             myQueue.addFirst(startIndex);
             while(myQueue.size()!=0) {
                 Integer thisNodeIndex = myQueue.removeLast();
                if(whiteList.contains(startIndex)) {
                    continue;
                }
                 whiteList.add(thisNodeIndex);
                 int x = getX(thisNodeIndex, width);
                 int y = getY(thisNodeIndex, width);
                 if (x >0 && board[y][x-1] == 'O' 
                         && !whiteList.contains(getIndex(x-1,y,width))){
                     myQueue.addFirst(getIndex(x-1,y,width));
                     whiteList.add(getIndex(x-1,y,width));
                 }
                 if (x <width-1 && board[y][x+1] == 'O' 
                         && !whiteList.contains(getIndex(x+1,y,width))){
                     myQueue.addFirst(getIndex(x+1,y,width));
                     whiteList.add(getIndex(x+1,y,width));
                 }
                 if (y >0 && board[y-1][x] == 'O' 
                         && !whiteList.contains(getIndex(x,y-1,width))){
                     myQueue.addFirst(getIndex(x,y-1,width));
                     whiteList.add(getIndex(x,y-1,width));
                 }
                 if (y <height-1 && board[y+1][x] == 'O' 
                         && !whiteList.contains(getIndex(x,y+1,width))){
                     myQueue.addFirst(getIndex(x,y+1,width));
                     whiteList.add(getIndex(x,y+1,width));
                 }
             }
         }
         for (int y=0; y<height; y++)
             for (int x=0; x<width; x++)
                 if(board[y][x] == 'O' && 
                     !whiteList.contains(getIndex(x,y,width)))
                     board[y][x] = 'X';
 
     }
 }
 
