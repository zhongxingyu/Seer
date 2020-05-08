 package com.kodingen.cetrin.player;
 
 import com.kodingen.cetrin.model.GameModel;
 import com.kodingen.cetrin.model.Move;
 
 public class ComputerPlayer extends Player {
 
     public ComputerPlayer() {
         this(GameModel.O, false);
     }
 
     private ComputerPlayer(int symbol, boolean showInputForm) {
         super(symbol, showInputForm);
     }
 
     @Override
     public Move getMove() {
         if (gm.hasWinner()) return null;
         if (!gm.hasMoreMoves()) return null;
         Move move;
         // if we can immediately win we do it
         move = tryWin();
         if (move != null) return move;
         // or try prevent other player to win
         move = tryPreventOtherWin();
         if (move != null) return move;
 
         // Если крестики сделали первый ход в центр, до конца игры ходить в любой угол,
         // а если это невозможно — в любую клетку
         if (gm.getMove(0).isCenterMove()) {
             move = tryCorners();
             if (move != null) return move;
             move = tryAnyEmptyPosition();
             if (move != null) return move;
         }
 
         // Если крестики сделали первый ход в угол, ответить ходом в центр.
         // Следующим ходом занять угол, противоположный первому ходу крестиков, а если это невозможно — пойти на сторону.
         if (gm.getMove(0).isCornerMove()) {
             move = tryCenter();
             if (move != null) return move;
             if (gm.movesCount() == 3) {
                 move = tryOppositeCorner(gm.getMove(0), gm);
                 if (move != null) return move;
                 move = trySides();
                 if (move != null) return move;
             }
         }
 
 
         // Если первый ход на сторону
         if (gm.movesCount() <= 3 && gm.getMove(0).isSideMove()) {
             // Если крестики сделали первый ход на сторону, ответить ходом в центр
             move = tryCenter();
            if (move != null) return move;
             // Если следующий ход крестиков — в угол, занять противоположный угол
             if (gm.movesCount() == 3 && gm.getMove(2).isCornerMove()) {
                 move = tryOppositeCorner(gm.getMove(2), gm);
                 if (move != null) return move;
             }
             // Если следующий ход крестиков — на противоположную сторону, пойти в любой угол
             if (gm.movesCount() == 3) {
                 if (Math.abs(gm.getLastMove().getX() - gm.getFieldSize() + 1) == gm.getMove(0).getX() &&
                         Math.abs(gm.getLastMove().getY() - gm.getFieldSize() + 1) == gm.getMove(0).getY()) {
                     move = tryCorners();
                     if (move != null) return move;
                 }
             }
             // Если следующий ход крестиков — на сторону рядом с их первым ходом,
             // пойти в угол рядом с обоими крестиками
             if (gm.movesCount() == 3 && gm.getLastMove().isSideMove() &&
                     gm.getMove(0).getX() != gm.getLastMove().getX() && gm.getMove(0).getY() != gm.getLastMove().getY()) {
                 int x = 0, y = 0;
                 if (gm.getMove(0).getX() == 0 || gm.getMove(0).getX() == gm.getFieldSize() - 1) {
                     x = gm.getMove(0).getX();
                 } else if (gm.getLastMove().getX() == 0 || gm.getLastMove().getX() == gm.getFieldSize() - 1) {
                     x = gm.getLastMove().getX();
                 }
                 if (gm.getMove(0).getY() == 0 || gm.getMove(0).getY() == gm.getFieldSize() - 1) {
                     y = gm.getMove(0).getY();
                 } else if (gm.getLastMove().getY() == 0 || gm.getLastMove().getY() == gm.getFieldSize() - 1) {
                     y = gm.getLastMove().getY();
                 }
                 move = tryCell(x, y);
                 if (move != null) return move;
             }
         }
 
         // go to any empty position
         return tryAnyEmptyPosition();
     }
 
     private Move tryWin() {
         Move move;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             move = checkHorizontalLineForWin(i);
             if (move != null) return move;
             move = checkVerticalLineForWin(i);
             if (move != null) return move;
         }
         return checkDiagonalsForWin();
     }
 
     private Move checkHorizontalLineForWin(int row) {
         int myCellsCount = 0;
         int emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(i, row) == this.getSymbolCode()) {
                 myCellsCount++;
             } else if (gm.getFieldCell(i, row) == GameModel.EMPTY) {
                 emptyCellNumber = i;
             }
         }
         if (myCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(emptyCellNumber, row);
         }
         return null;
     }
 
     private Move checkVerticalLineForWin(int column) {
         int myCellsCount = 0;
         int emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(column, i) == this.getSymbolCode()) {
                 myCellsCount++;
             } else if (gm.getFieldCell(column, i) == GameModel.EMPTY) {
                 emptyCellNumber = i;
             }
         }
         if (myCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(column, emptyCellNumber);
         }
         return null;
     }
 
     private Move checkDiagonalsForWin() {
         int myCellsCount = 0;
         int emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(i, i) == this.getSymbolCode()) {
                 myCellsCount++;
             } else if (gm.getFieldCell(i, i) == GameModel.EMPTY) {
                 emptyCellNumber = i;
             }
         }
         if (myCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(emptyCellNumber, emptyCellNumber);
         }
         myCellsCount = 0;
         emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(i, gm.getFieldSize() - 1 - i) == this.getSymbolCode()) {
                 myCellsCount++;
             } else if (gm.getFieldCell(i, gm.getFieldSize() - 1 - i) == GameModel.EMPTY){
                 emptyCellNumber = i;
             }
         }
         if (myCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(emptyCellNumber, gm.getFieldSize() - 1 - emptyCellNumber);
         }
         return null;
     }
 
     private Move tryPreventOtherWin() {
         Move move;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             move = checkHorizontalLineForPrevent(i);
             if (move != null) return move;
             move = checkVerticalLineForPrevent(i);
             if (move != null) return move;
         }
         return checkDiagonalsForPrevent();
     }
 
     private Move checkHorizontalLineForPrevent(int row) {
         int oppositeCellsCount = 0;
         int emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(i, row) == -this.getSymbolCode()) {
                 oppositeCellsCount++;
             } else if (gm.getFieldCell(i, row) == GameModel.EMPTY) {
                 emptyCellNumber = i;
             }
         }
         if (oppositeCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(emptyCellNumber, row);
         }
         return null;
     }
 
     private Move checkVerticalLineForPrevent(int column) {
         int oppositeCellsCount = 0;
         int emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(column, i) == -this.getSymbolCode()) {
                 oppositeCellsCount++;
             } else if (gm.getFieldCell(column, i) == GameModel.EMPTY) {
                 emptyCellNumber = i;
             }
         }
         if (oppositeCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(column, emptyCellNumber);
         }
         return null;
     }
 
     private Move checkDiagonalsForPrevent() {
         int oppositeCellsCount = 0;
         int emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(i, i) == -this.getSymbolCode()) {
                 oppositeCellsCount++;
             } else if (gm.getFieldCell(i, i) == GameModel.EMPTY) {
                 emptyCellNumber = i;
             }
         }
         if (oppositeCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(emptyCellNumber, emptyCellNumber);
         }
         oppositeCellsCount = 0;
         emptyCellNumber = -1;
         for (int i = 0; i < gm.getFieldSize(); i++) {
             if (gm.getFieldCell(i, gm.getFieldSize() - 1 - i) == -this.getSymbolCode()) {
                 oppositeCellsCount++;
             } else if (gm.getFieldCell(i, gm.getFieldSize() - 1 - i) == GameModel.EMPTY) {
                 emptyCellNumber = i;
             }
         }
         if (oppositeCellsCount == 2 && emptyCellNumber != -1) {
             return new Move(emptyCellNumber, gm.getFieldSize() - 1 - emptyCellNumber);
         }
         return null;
     }
 
     private Move tryCenter() {
         int centerPosition = gm.getFieldSize() / 2;
         if (gm.getFieldCell(centerPosition, centerPosition) == GameModel.EMPTY) {
             return new Move(centerPosition, centerPosition);
         }
         return null;
     }
 
     private Move tryOppositeCorner(Move move, GameModel gm) {
         if (move.isCornerMove()) {
             return tryCell(Math.abs(move.getX() - gm.getFieldSize() + 1),
                     Math.abs(move.getY() - gm.getFieldSize() + 1));
         }
         return null;
     }
 
     private Move tryCorners() {
         Move move;
         move = tryCell(0, 0);
         if (move != null) {
             return move;
         }
         move = tryCell(0, gm.getFieldSize() - 1);
         if (move != null) {
             return move;
         }
         move = tryCell(gm.getFieldSize() - 1, 0);
         if (move != null) {
             return move;
         }
         return tryCell(gm.getFieldSize() - 1, gm.getFieldSize() - 1);
     }
 
     private Move trySides() {
         Move move;
         move = tryCell(gm.getFieldSize() / 2, 0);
         if (move != null) {
             return move;
         }
         move = tryCell(gm.getFieldSize() - 1, gm.getFieldSize() / 2);
         if (move != null) {
             return move;
         }
         move = tryCell(0, gm.getFieldSize() / 2);
         if (move != null) {
             return move;
         }
         return tryCell(gm.getFieldSize() / 2, gm.getFieldSize() - 1);
     }
 
     private Move tryCell(int x, int y) {
         if (gm.getFieldCell(x, y) == GameModel.EMPTY) {
             return new Move(x, y);
         }
         return null;
     }
 
     private Move tryAnyEmptyPosition() {
         for (int x = 0; x < gm.getFieldSize(); x++) {
             for (int y = 0; y < gm.getFieldSize(); y++) {
                 if (gm.getFieldCell(x, y) == GameModel.EMPTY) {
                     return new Move(x, y);
                 }
             }
         }
         return null;
     }
 
 }
