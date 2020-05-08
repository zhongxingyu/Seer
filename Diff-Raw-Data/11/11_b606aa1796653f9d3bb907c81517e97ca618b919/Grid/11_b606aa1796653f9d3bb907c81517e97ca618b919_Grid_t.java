 package edu.stuy.goldfish;
 
 public class Grid {
 
     private Patch[][] _grid;
 
     public Grid() {
         _grid = new Patch[1][1];
         _grid[0][0] = new Patch(this, 0, 0, 0);
     }
 
     public Grid(int x, int y) {
         _grid = new Patch[y][x];
         for (int i = 0; i < x; i++) {
             for (int j = 0; j < y; j++) {
                 _grid[j][i] = new Patch(this, i, j, 0);
             }
         }
     }
 
     private int normalizeX(int x) {
         while (x >= getWidth())
             x -= getWidth();
         while (x < 0)
             x += getWidth();
         return x;
     }
 
     private int normalizeY(int y) {
         while (y >= getHeight())
             y -= getHeight();
         while (y < 0)
             y += getHeight();
         return y;
     }
 
     public int getWidth() {
        return _grid[0].length;
     }
 
     public int getHeight() {
        return _grid.length;
     }
 
     public Patch getPatch(int x, int y) {
         x = normalizeX(x);
         y = normalizeY(y);
         return _grid[y][x];
     }
 
     public void setPatch(int x, int y, Patch p) {
         x = normalizeX(x);
         y = normalizeY(y);
         _grid[y][x] = p;
     }
 
     public String toString() {
         String ans = "";
         for (int i = 0; i < _grid.length; i++) {
             for (int j = 0; j < _grid[i].length; j++) {
                 ans += _grid[i][j];
             }
             ans += "\n";
         }
         return ans;
     }
 }
