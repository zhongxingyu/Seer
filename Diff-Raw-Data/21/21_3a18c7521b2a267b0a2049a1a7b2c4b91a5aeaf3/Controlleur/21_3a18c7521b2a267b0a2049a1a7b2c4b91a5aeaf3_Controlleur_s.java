 import java.util.Scanner;
 
 
 public class Controlleur {
   static Cube cube;
   private static Scanner sc = new Scanner(System.in);
   static int  scoreGame = 0;
   static int  scoreTotal = 0;
   private static boolean isIAPlaying = true; 
   public static IA ia;
   public static boolean printRound = true;
   public static boolean printGame = true;
   public static boolean printStat = true;
   public static int gameCount = 1;
   public static int currentGameIndex = 0;
  private static int maxDepth = 21;
  private static int size = 3;
   
   
   /**
    * @param args
    */
   public static void main(String[] args)  {
    for (currentGameIndex = 1; currentGameIndex <= gameCount; currentGameIndex++) {
       playGame();
     }
     System.out.println(gameCount + " games for size " + Cube.SIZE +
         " mean score is " + scoreTotal / gameCount);
   }
 
   /**
    * Launches a new game.
    */
   public static void playGame() {
     scoreGame = 0;
     cube = new Cube(size);
     cube.fillGridShuffle();
     if (printGame)
       System.out.println("Start Game with \n" + cube);
     if (printGame)
       System.out.println(cube.getScore());
     ia = new IA (cube, maxDepth);
     while (!cube.isGridGood()) {
       playRound();
     }
     if (printGame)
       System.out.println(cube.isGridGood());
     scoreTotal += scoreGame;
     if (printGame || printStat)
       System.out.println("Congrats ! Your score is : " + scoreGame +
          " score total" + scoreTotal +" " + scoreTotal / currentGameIndex);
     
   }
   
   /**
    * Plays a round of the game.
    */
   public static void playRound() {
     int move = 0;
     if (isIAPlaying) {
       move = ia.getIAMove();
     } else {
       for (int i = 0; i < Cube.getMoves().length; i++) {
         if (printRound)
           System.out.println( i + " : " + Cube.getMoves()[i]);
       }
       move = sc.nextInt();      
     }
     
     if (cube.move(move)) {
       if (printRound)
         System.out.println("Valid" + move);
       scoreGame++;
     } else {
       if (printRound)
         System.out.println("Invalid" + move);
     }
     if (printRound)
       System.out.println(cube);
     if (printRound)
       System.out.println("Turn : " + scoreGame);
   }
   
 }
