 package game;
 
 import players.Player;
 
 /**
  * Основной класс игры
  */
 public class Game {
 
     private final Player player1, player2;
     //число заполненных клеток
     private int notEmptyCells = 0;
     //общее число клеток
     private final int numberCells;
     private final Field field;
     private final int lineForWin;
 
     public Game(Player player1, Player player2) {
         this(player1, player2, Field.MIN_FIELD_SIZE, Field.MIN_FIELD_SIZE, Field.MIN_FIELD_SIZE);
     }
 
     /**
      *
      * @param player1
      * @param player2
      * @param x высота поля
      * @param y ширина поля
      * @param w длина линии для победы
      */
     public Game(Player player1, Player player2, int x, int y, int w) {
         this.player1 = player1;
         this.player2 = player2;
         this.field = new Field(x, y);
         numberCells = field.field.length * field.field[0].length;
         if (w < Field.MIN_FIELD_SIZE) {
             w = Field.MIN_FIELD_SIZE;
         }
         lineForWin = w;
     }
 
     public void start() {
         boolean firstPlayer = true;
         Player curPlayer;
         char curChar;
         while (haveEmptyCell()) {
             if (firstPlayer) {
                 curPlayer = player1;
                 curChar = 'X';
             } else {
                 curPlayer = player2;
                 curChar = 'O';
             }
             field.showField();
             System.out.println("Ходит игрок " + curPlayer.getName() + " (" + curChar + ")");
             int cell = curPlayer.go(field);
             int x = cell / field.width;
             int y = cell % field.width;
             if (field.validationStep(x, y) != Field.NO_ERROR) {
                 throw new RuntimeException("Недопустимый ход!");
             }
             field.field[x][y] = curChar;
             if (haveWinner(x, y)) {
                 field.showField();
                 System.out.println("Игрок " + curPlayer.getName() + " (" + curChar + ") победил!");
                 return;
             }
             notEmptyCells++;
             firstPlayer = !firstPlayer;
         }
         field.showField();
         System.out.println("Ничья!");
     }
 
     private boolean haveEmptyCell() {
         return notEmptyCells < numberCells;
     }
 
     /**
      * Проверяем, что последний ход в (x,y) стал победным
      *
      * @param x
      * @param y
      * @return
      */
     private boolean haveWinner(int x, int y) {
         int i, j;
         int width = field.width;
         int height = field.height;
         //горизонтально
        for (i = y; i >= 0 && field.field[x][i] == field.field[x][y]; i--);
        for (j = y; j < width && field.field[x][j] == field.field[x][y]; j++);
         if ((j - i - 1) >= lineForWin) {
             return true;
         }
         //вертикально
        for (i = x; i >= 0 && field.field[i][y] == field.field[x][y]; i--);
        for (j = x; j < height && field.field[j][y] == field.field[x][y]; j++);
         if ((j - i - 1) >= lineForWin) {
             return true;
         }
         //диагональ -45*      
         for (i = 1; x - i >= 0 && y - i >= 0 && field.field[x - i][y - i] == field.field[x][y]; i++);
        for (j = 1; x + j < height && y + j < width && field.field[x + j][y + j] == field.field[x][y]; j++);
         if ((j + i - 1) >= lineForWin) {
             return true;
         }
         //диагональ 45* 
        for (i = 1; x + i < height && y - i >= 0 && field.field[x + i][y - i] == field.field[x][y]; i++);
        for (j = 1; x - j >= 0 && y + j < width && field.field[x - j][y + j] == field.field[x][y]; j++);
         if ((j + i - 1) >= lineForWin) {
             return true;
         }
         return false;
     }
 }
