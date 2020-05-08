 package algorithm.lc;
 
 /**
  * Given a 2D board and a word, find if the word exists in the grid.
  * 
  * The word can be constructed from letters of sequentially adjacent cell, where
  * "adjacent" cells are those horizontally or vertically neighboring. The same
  * letter cell may not be used more than once.
  * 
  * For example, Given board =
  * 
  * [ ["ABCE"], ["SFCS"], ["ADEE"] ] word = "ABCCED", -> returns true, word =
  * "SEE", -> returns true, word = "ABCB", -> returns false.
  * 
  */
 public class WordSearch {
   
   public static class Solution {
     public boolean exist(char[][] board, String word) {
           // Start typing your Java solution below
           // DO NOT write main() function
       if (board.length == 0 || board[0].length == 0) {
         return false;
       }
       boolean[][] visited = new boolean[board.length][board[0].length];
       for (int i = 0; i < board.length; ++i) {
         for (int j = 0; j < board[0].length; ++j) {
           if (search(board, word, 0, i, j, visited)) {
             return true;
           }
         }
       }
       return false;
     }
     
     private boolean search(char[][] board, String word, int idx, int i, int j, boolean[][] visited) {
       char ch = word.charAt(idx);
       if (board[i][j] != ch) {
         return false;
       }
      if (idx == word.length() - 1) {
        return true;
      }
       visited[i][j] = true;
       if (i - 1 >= 0 && !visited[i - 1][j] && search(board, word, idx + 1, i - 1, j, visited)) {
         return true;
       }
       if (i + 1 < board.length && !visited[i + 1][j] && search(board, word, idx + 1, i + 1, j, visited)) {
         return true;
       }
       if (j - 1 >= 0 && !visited[i][j - 1] && search(board, word, idx + 1, i, j - 1, visited)) {
         return true;
       }
       if (j + 1 < board[0].length && !visited[i][j + 1] && search(board, word, idx + 1, i, j + 1, visited)) {
         return true;
       }
 
       visited[i][j] = false;
       return false;
     }
   }
 }
