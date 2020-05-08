 package tictactoe;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 public class TicTacToe {
     //1 = X, 2 = O
 
     public boolean player = true; // X = True, O = False
     public int Grid[] = new int[10];
     public String textGrid[] = new String[10];
     public boolean gameRun = true;
     public int roundNo = 0;
 
     public void gridReset() {
         for (int i = 0; i < Grid.length; i++) {
             Grid[i] = 0;
         }
         for (int i = 0; i < textGrid.length; i++) {
             textGrid[i] = " ";
             roundNo = 0;
             player = true;
         }
     }
 
     public void convert() throws IOException, NumberFormatException {
         try {
             int input = Integer.parseInt(TicTacToe.Input());
             if (input > 9 || input < 1) {
                 System.out.println("Number out of bounds");
             } else {
 
 
                 if (Grid[input] == 0) {
                     if (player == true) {
                         Grid[input] = 1;
                         player = false;
                         roundNo++;
                     } else {
                         Grid[input] = 2;
                         player = true;
                         roundNo++;
                     }
                 } else {
                     System.out.println("There is already something there. Try again");
                 }
                 for (int i = 0; i < Grid.length; i++) {
                     switch (Grid[i]) {
                         case 1:
                             textGrid[i] = "X";
                             break;
                         case 2:
                             textGrid[i] = "O";
                             break;
                     }
                 }
             }
         } catch (NumberFormatException bad) {
             System.out.println("That's not a number...");
         }
     }
 
     public void printGrid1() {
         int gridNo = 1;
         for (int row = 0; row < 3; row++) {
             for (int line = 0; line < 3; line++) {
                 System.out.print("[" + textGrid[gridNo] + "]");
                 gridNo++;
             }
             System.out.printf("%n");
 
         }
     }
 
     public void printGrid() {
         int gridNo1 = 1;
         for (int row = 0; row < 3; row++) {
             for (int line = 0; line < 3; line++) {
 
                 System.out.print("[" + gridNo1 + "]");
                 gridNo1++;
             }
             System.out.printf("%n");
         }
     }
 
     public static String Input() throws IOException {
         InputStreamReader istream = new InputStreamReader(System.in);
         BufferedReader bufRead = new BufferedReader(istream);
         String Input = bufRead.readLine();
         return Input;
     }
 
     public void checkWin() {
         for (int x = 0; x <= 6; x += 3) {
             if (Grid[x + 1] == Grid[x + 2] && Grid[x + 2] == Grid[x + 3] && Grid[x + 1] == 1) {
                 gameRun = false;
                 System.out.println("The winner is X!");
             }
             if (Grid[x + 1] == Grid[x + 2] && Grid[x + 2] == Grid[x + 3] && Grid[x + 1] == 2) {
                 gameRun = false;
                 System.out.println("The winner is O!");
             }
         }
         for (int l = 0; l < 3; l++) {
             if (Grid[l + 1] == Grid[l + 4] && Grid[l + 4] == Grid[l + 7] && Grid[l + 1] == 1) {
                 gameRun = false;
                 System.out.println("The winner is X!");
             }
             if (Grid[l + 1] == Grid[l + 4] && Grid[l + 4] == Grid[l + 7] && Grid[l + 1] == 2) {
                 gameRun = false;
                 System.out.println("The winner is O!");
             }
         }
         if (Grid[1] == Grid[5] && Grid[5] == Grid[9] && Grid[1] == 1) {
             gameRun = false;
             System.out.println("The winner is X!");
         }
         if (Grid[1] == Grid[5] && Grid[5] == Grid[9] && Grid[1] == 2) {
             gameRun = false;
             System.out.println("The winner is O!");
         }
         if (Grid[3] == Grid[5] && Grid[5] == Grid[7] && Grid[3] == 1) {
             gameRun = false;
             System.out.println("The winner is X!");
         }
         if (Grid[3] == Grid[5] && Grid[5] == Grid[7] && Grid[3] == 2) {
             gameRun = false;
             System.out.println("The winner is O!");
         }
     }
 
     public void AI() throws IOException {
 
         Random rand = new Random();
         try {
             boolean turnWork = true;
             if (player == true && roundNo != 9) {
                 while (turnWork == true) {
                     int input = Integer.parseInt(TicTacToe.Input());
                     if (input > 9 || input < 1) {
                         System.out.println("Number out of bounds");
                     } else {
                         if (Grid[input] == 0) {
                             Grid[input] = 1;
                             roundNo++;
                             turnWork = false;
                             player = false;
                         } else {
                             System.out.println("There is already something there. Try again");
                         }
                     }
                 }
             }
            if (player == false && roundNo != 9) {
                 roundNo++;
                 boolean AIwork = true;
                 while (AIwork == true) {
                     for (int x = 0; x <= 6; x += 2) {
                         if (Grid[x + 1] == Grid[x + 2] && Grid[x + 2] == 2 && Grid[x + 3] == 0  && AIwork == true) {
                             Grid[x + 3] = 2;
                             AIwork = false;
                         } else if (Grid[x + 2] == Grid[x + 3] && Grid[x + 3] == 2 && Grid[x + 1] == 0 && AIwork == true) {
                             Grid[x + 1] = 2;
                             AIwork = false;
                         } else if (Grid[x + 1] == Grid[x + 3] && Grid[x + 3] == 2 && Grid[x + 2] == 0 && AIwork == true) {
                             Grid[x + 2] = 2;
                             AIwork = false;
                         }
                         for (int l = 0; l < 3; l++) {
                              if (Grid[l + 1] == Grid[l + 4] && Grid[l + 4] == 2 && Grid[l + 7] == 0 && AIwork == true) {
                                 Grid[l + 7] = 2;
                                 AIwork = false;
                             } else if (Grid[l + 4] == Grid[l + 7] && Grid[l + 4] == 2 && Grid[l + 1] == 0 && AIwork == true) {
                                 Grid[l + 1] = 2;
                                 AIwork = false;
                             } else if (Grid[l + 1] == Grid[l + 7] && Grid[l + 1] == 2 && Grid[l + 4] == 0 && AIwork == true) {
                                 Grid[l + 4] = 2;
                                 AIwork = false;
                             }
                         }
                         if (Grid[1] == Grid[5] && Grid[5] == 2 && Grid[9] == 0 && AIwork == true) {
                             Grid[9] = 2;
                             AIwork = false;
                         } else if (Grid[5] == Grid[9] && Grid[5] == 2 && Grid[1] == 0 && AIwork == true) {
                             Grid[1] = 2;
                             AIwork = false;
                         } else if (Grid[1] == Grid[9] && Grid[9] == 2 && Grid[5] == 0 && AIwork == true) {
                             Grid[5] = 2;
                             AIwork = false;
                         } else if (Grid[3] == Grid[5] && Grid[5] == 2 && Grid[7] == 0 && AIwork == true) {
                             Grid[7] = 2;
                             AIwork = false;
                         } else if (Grid[3] == Grid[7] && Grid[3] == 2 && Grid[5] == 0 && AIwork == true) {
                             Grid[5] = 2;
                             AIwork = false;
                         } else if (Grid[5] == Grid[7] && Grid[5] == 2 && Grid[3] == 0 && AIwork == true) {
                             Grid[3] = 2;
                             AIwork = false;
                         }
                     }
                           for ( int x = 0; x <= 6; x += 2) {
                         if (Grid[x + 1] == Grid[x + 2] && Grid[x + 2] == 1 && Grid[x + 3] == 0  && AIwork == true) {
                             Grid[x + 3] = 2;
                             AIwork = false;
                         } else if (Grid[x + 2] == Grid[x + 3] && Grid[x + 3] == 1 && Grid[x + 1] == 0 && AIwork == true) {
                             Grid[x + 1] = 2;
                             AIwork = false;
                         } else if (Grid[x + 1] == Grid[x + 3] && Grid[x + 3] == 1 && Grid[x + 2] == 0 && AIwork == true) {
                             Grid[x + 2] = 2;
                             AIwork = false;
                         }
                         for (int l = 0; l < 3; l++) {
                              if (Grid[l + 1] == Grid[l + 4] && Grid[l + 4] == 1 && Grid[l + 7] == 0 && AIwork == true) {
                                 Grid[l + 7] = 2;
                                 AIwork = false;
                             } else if (Grid[l + 4] == Grid[l + 7] && Grid[l + 4] == 1 && Grid[l + 1] == 0 && AIwork == true) {
                                 Grid[l + 1] = 2;
                                 AIwork = false;
                             } else if (Grid[l + 1] == Grid[l + 7] && Grid[l + 1] == 1 && Grid[l + 4] == 0 && AIwork == true) {
                                 Grid[l + 4] = 2;
                                 AIwork = false;
                             }
                         }
                         if (Grid[1] == Grid[5] && Grid[5] == 1 && Grid[9] == 0 && AIwork == true) {
                             Grid[9] = 2;
                             AIwork = false;
                         } else if (Grid[5] == Grid[9] && Grid[5] == 1 && Grid[1] == 0 && AIwork == true) {
                             Grid[1] = 2;
                             AIwork = false;
                         } else if (Grid[1] == Grid[9] && Grid[9] == 1 && Grid[5] == 0 && AIwork == true) {
                             Grid[5] = 2;
                             AIwork = false;
                         } else if (Grid[3] == Grid[5] && Grid[5] == 1 && Grid[7] == 0 && AIwork == true) {
                             Grid[7] = 2;
                             AIwork = false;
                         } else if (Grid[3] == Grid[7] && Grid[3] == 1 && Grid[5] == 0 && AIwork == true) {
                             Grid[5] = 2;
                             AIwork = false;
                         } else if (Grid[5] == Grid[7] && Grid[5] == 1 && Grid[3] == 0 && AIwork == true) {
                             Grid[3] = 2;
                             AIwork = false;
                         }
                     }
                     int AIplace = 1 + rand.nextInt(9);
                     if (Grid[AIplace] == 0  && AIwork == true) {
                         Grid[AIplace] = 2;
                         AIwork = false;
                     }
                 }
                 player = true;
             }
             for (int i = 0; i < Grid.length; i++) {
                 switch (Grid[i]) {
                     case 1:
                         textGrid[i] = "X";
                         break;
                     case 2:
                         textGrid[i] = "O";
                         break;
                 }
             }
 
 
             printGrid1();
         } catch (NumberFormatException bad) {
             System.out.println("That's not a number...");
         }
     }
 
     public static void main(String[] args) throws IOException {
         System.out.println("Welcome to Denis' Tic Tac Toe game!");
         boolean run = true;
         while (run == true) {
             TicTacToe Grid = new TicTacToe();
             Grid.gridReset();
             Grid.printGrid();
             boolean asks = true;
             System.out.println("Would you like to play AI or human?\n 1 = AI \t 2 = human");
             while (asks == true) {
                 switch (TicTacToe.Input()) {
                     case "1":
                         System.out.println(System.getProperty("user.name") + " VS AI");
                         Grid.gridReset();
                         asks = false;
                         while (Grid.gameRun == true) {
                             Grid.AI();
                             Grid.checkWin();
                             if (Grid.roundNo == 9 && Grid.gameRun == true) {
                                 Grid.gameRun = false;
                                 System.out.println("It's a tie!");
                             }
                         }
                         break;
                     case "2":
                         System.out.println(System.getProperty("user.name") + " VS human");
                         asks = false;
                         while (Grid.gameRun == true) {
                             if (Grid.player == true) {
                                 System.out.println("Where do you want to place (X)?");
                             } else {
                                 System.out.println("Where do you want to place (O)?");
                             }
                             Grid.convert();
                             Grid.printGrid1();
                             Grid.checkWin();
                             if (Grid.roundNo == 9 && Grid.gameRun == true) {
                                 System.out.println("It's a tie!");
                                 Grid.gameRun = false;
                             }
                         }
                         break;
                     default:
                         System.out.println("Not valid answer");
                         break;
                 }
             }
             System.out.println("Would you like to play another round?(y/n)");
             boolean ask = true;
             while (ask == true) {
                 switch (TicTacToe.Input()) {
                     case "n":
                         run = false;
                         System.out.println("Thanks for playing!");
                         ask = false;
                         break;
                     case "y":
                         ask = false;
                         break;
                     default:
                         System.out.println("Not valid answer.");
                         break;
                 }
             }
         }
     }
 }
