 package com.cs408.supersweeper;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 public class GameState {
 
     /** Global Variables */
     private GridUnit[][] _grid;
     private double _time;
     private int _numMines;
     private int _gridWidth;
     private int _gridHeight;
     private int _scoreBonus;
     private boolean _gameIsOver = false;
 
     /** Constructors */
     public GameState(double time, int numMines, int gridWidth, int gridHeight, int score) {
         this._time = time;
         this._numMines = numMines;
         this._gridHeight = gridHeight;
         this._gridWidth = gridWidth;
         this._scoreBonus = score;
 
         _grid = new GridUnit[gridWidth][gridHeight];
 
         // initialize the grid
         for (int x = 0; x < gridWidth; x++) {
             for (int y = 0; y < gridHeight; y++) {
                 _grid[x][y] = new GridUnit();
             }
         }
 
         // set adjacent grid units
         for (int x = 0; x < gridWidth; x++) {
             for (int y = 0; y < gridHeight; y++) {
                 for (int i = x - 1; i <= x + 1; i++) {
                     for (int j = y - 1; j <= y + 1; j++) {
                         if (isValidGridUnit(i, j) && !(i - x == 0 && j - y == 0)) {
                             _grid[x][y].adjacentGridUnits.add(_grid[i][j]);
                         }
                     }
                 }
             }
         }
     }
 
     
     
     /** Game Logic */
     public void endGame(int pointsToAdd) {
         _gameIsOver = true;
         if(pointsToAdd == 0) {
             Utility.infoBox("Boom! You Lost :(", "");
         }
         else {
             Utility.infoBox("Hooray! You won " + pointsToAdd + " points :D!", "");
             
         }
     }
     
     public void resetGrid() {
         for (int x = 0; x < _gridWidth; x++) {
             for (int y = 0; y < _gridHeight; y++) {
                 _grid[x][y].resetUnit();
             }
         }
         _gameIsOver = false;
     }
     
     public void removeMines() {
         for (int x = 0; x < _gridWidth; x++) {
             for (int y = 0; y < _gridHeight; y++) {
                 _grid[x][y].isMined = false;
             }
         }
     }
 
     public void populateMines() {
         GridUnit[] units = Utility.flatten(_grid);
         Collections.shuffle(Arrays.asList(units));
         for (int i = 0; i < _numMines; i++) {
             units[i].isMined = true;
         }
     }
 
     private void stateChanged(GridUnit gu) {
         if (gu.isChecked) {
             // TODO replace grid_unit_empty with grid_unit_0
             if (gu.isMined) {
                 gu.setImage(GridUnit.images.get("mine"));
             } else if (gu.adjacentMineCount() > 0) {
                 gu.setImage(GridUnit.images.get(String.valueOf(gu.adjacentMineCount()))); 
             } else {
                 gu.setImage(GridUnit.images.get("empty"));
             }
         } else if (gu.isFlagged) {
             gu.setImage(GridUnit.images.get("flag"));
         } else if (gu.isPressed) {
             gu.setImage(GridUnit.images.get("press"));
         } else if (gu.isHovered) {
             gu.setImage(GridUnit.images.get("hover"));
         } else {
             gu.setImage(GridUnit.images.get("normal"));
         }
     }
     
     private void exposeMines(GridUnit gu) {
         if (gu.isExposed) {
             return;
         }
 
         gu.isExposed = true;
         if (gu.isMined) {
             gu.isChecked = true;
         }
 
         stateChanged(gu);
 
         for (GridUnit unit : gu.adjacentGridUnits) {
             if (!unit.isExposed) {
                 exposeMines(unit);
             }
         }
     }
 
     private void check(GridUnit gu) {
         gu.isChecked = true;
 
         stateChanged(gu);
 
         if (gu.adjacentMineCount() > 0) {
             return;
         }
 
         for (GridUnit unit : gu.adjacentGridUnits) {
             if (!unit.isChecked && !unit.isFlagged) {
                 check(unit);
             }
         }
     }
 
     public void checkPressed(GridUnit gu) {
         if (!gu.isChecked && !gu.isFlagged) {
             gu.isPressed = true;
         }
 
         stateChanged(gu);
     }
 
     public void checkReleased(GridUnit gu) {
         gu.isPressed = false;
         if (!gu.isFlagged) {
             if (gu.isMined) {
                 exposeMines(gu);
                 endGame(0);
             } else {
                 check(gu);
             }
         }
 
         stateChanged(gu);
     }
 
     public void flagPressed(GridUnit gu) {
         // ignore
 
         stateChanged(gu);
     }
 
     public void flagReleased(GridUnit gu) {
         if (!gu.isChecked) {
             gu.isFlagged = !gu.isFlagged;
         }
 
         stateChanged(gu);
     }
 
     public void checkCancelled(GridUnit gu) {
         gu.isPressed = false;
 
         stateChanged(gu);
     }
 
     
     
     /** Getters */
     public int getGridHeight () {
         return this._gridHeight;
     }
     
     public int getGridWidth () {
         return this._gridWidth;
     }
     
     public GridUnit[][] getGrid() {
         return _grid;
     }
     
     public GridUnit getGridUnit(int x, int y) {
         return _grid[x][y];
     }
     
     public double getTime() {
         return _time;
     }
     
     public boolean isGameOver() {
         return _gameIsOver;
     }
     
     public int getLevelScoreBonus() {
         return this._scoreBonus;
     }
     
     public int getFlagCount() {
         int count = 0;
         for (int i = 0; i < _gridWidth; i++) {
             for (int j = 0; j < _gridHeight; j++) {
                 if (_grid[i][j].isFlagged) {
                     count++;
                 }
             }
         }
         return count;
     }
     
     public boolean isValidGridUnit(int x, int y) {
         return x >= 0 && x < _gridWidth && y >= 0 && y < _gridHeight;
     }
     
     public boolean hasWon() {
         int count = 0;
         for (int i = 0; i < _gridWidth; i++) {
             for (int j = 0; j < _gridHeight; j++) {
                 if (_grid[i][j].isChecked) {
                     count++;
                 }
             }
         }
         if(count >= (_gridWidth*_gridHeight) - _numMines)
             return true;
         return false;
     }
 }
