 package game;
 
 import common.GameField;
 import common.Main;
 import players.Computer;
 import players.Human;
 import players.Player;
 
 import java.util.Scanner;
 
 public class Game {
     private static boolean work;
    private static boolean win = false;
 
     public Game(){
 
     }
 
     public static void setWork(Boolean bool){
         work = bool;
     }
 
     public static void setWin(Boolean bool){
         win = bool;
     }
 
     public static boolean getWork(){
         return work;
     }
 
     public static boolean getWin(){
         return win;
     }
 
     public static void startGame(String params){
         int steps;
         Scanner scanner = new Scanner(System.in);
         Human playerX = new Human('X');
         Player playerO = null;
 
         if(params.contentEquals("pvp")){
             playerO = new Human('O');
         }
         else if(params.contentEquals("pvc")) {
             playerO = new Computer('O');
         }
         else {
             System.out.println("Ошибка!");
             Main.newGame();
         }
 
         System.out.println("Введите размер игрового поля: ");
         GameField gameField = new GameField(scanner.nextInt());
         gameField.eraseField();
 
        //setWin(false);
 
         for(steps = 0; steps < gameField.getMaxSteps(); ){
             setWork(true);
             gameField.viewPlane();
             System.out.println("Ход игрока " + playerX.getSymbol() + ": ");
             while (getWork()){
                 playerX.step(gameField);
             }
             steps++;
 
             if(steps == gameField.getMaxSteps()) break;
             if(getWin()) break;
 
             gameField.viewPlane();
             setWork(true);
             System.out.println("Ход игрока " + playerO.getSymbol() + ": ");
             while (getWork()){
                 playerO.step(gameField);
             }
             steps++;
 
             if(getWin()) break;
         }
 
         if(!getWin() && (steps == gameField.getMaxSteps())) System.out.println("Ничья");
 
         gameField.viewPlane();
     }
 }
