 package tictactoe;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 public class TicTacToe { 
     //1 = X, 2 = O
 public boolean player = true; // X = True, O = False
 public int Grid[] = new int[10];
 public String textGrid[] = new String[10];
 public boolean gameRun = true;
public int roundNo = 0;
 public void gridReset (){
     for(int i = 0; i < Grid.length; i++)
     {Grid[i] = 0;}
     for(int i = 0; i < textGrid.length; i++)
     {textGrid[i] = " ";  }
 }
 public void convert() throws IOException, NumberFormatException {
 try{
      int input = Integer.parseInt(TicTacToe.Input());
  if (input > 9 || input < 1){
      System.out.println("Number out of bounds");
  }
  else{
      
   
  if (Grid[input] == 0){
         if (player == true){
          Grid[input] = 1;
 player = false;
       roundNo++;  }
 else {
      Grid[input] = 2;
     player = true;
roundNo++;}
 }
 else{
     System.out.println("There is already something there. Try again");
 }
  for(int i = 0; i < Grid.length; i++){
  switch (Grid[i]){
      case 1:
          textGrid[i] = "X";
          break;
      case 2: textGrid[i] = "O";
          break;
       }
     }
 }
 }
 catch(NumberFormatException bad){
   System.out.println("That's not a number...");  
 }
 }
  public void printGrid1(){
     int gridNo = 1;
      for(int row = 0; row < 3; row ++){
         for (int line = 0; line < 3; line++) {
             
             System.out.print("["+textGrid[gridNo]+"]");
             gridNo++;
         }
         System.out.printf("%n");
  
     }  
  }
  public void printGrid(){
      int gridNo1 = 1;
      for(int row = 0; row < 3; row ++){
         for (int line = 0; line < 3; line++) {
             
             System.out.print("["+gridNo1+"]");
             gridNo1++;
         }
         System.out.printf("%n");
  
     }
  }
  
  public static String Input () throws IOException{
     InputStreamReader istream = new InputStreamReader(System.in) ;
     BufferedReader bufRead = new BufferedReader(istream) ;
     String Input = bufRead.readLine();
      return Input;
     }
  public void checkWin(){
     for (int x = 0; x < 6;x+=3 )
   {
      if (Grid[x+1] == Grid[x+2] && Grid [x+2] == Grid[x+3] && Grid[x+1] == 1){
          gameRun = false;
          System.out.println("The winner is X!");
      }
      if (Grid[x+1] == Grid[x+2] && Grid [x+2] == Grid[x+3] && Grid[x+1] == 2){
          gameRun = false;
          System.out.println("The winner is O!");
      }
   }
     for (int l =0; l < 3; l++) {
         if (Grid[l+1] == Grid[l+4] && Grid [l+4] == Grid[l+7] && Grid[l+1] == 1){
          gameRun = false;
          System.out.println("The winner is X!");
      }
      if (Grid[l+1] == Grid[l+4] && Grid [l+4] == Grid[l+7] && Grid[l+1] == 2){
          gameRun = false;
          System.out.println("The winner is O!");
      }
      
      }
     if (Grid[1] == Grid[5] && Grid [5] == Grid[9] && Grid[1] == 1){
          gameRun = false;
          System.out.println("The winner is X!");
      }
      if (Grid[1] == Grid[5] && Grid [5] == Grid[9] && Grid[1] == 2){
          gameRun = false;
          System.out.println("The winner is O!");
      }
      if (Grid[3] == Grid[5] && Grid [5] == Grid[7] && Grid[3] == 1){
          gameRun = false;
          System.out.println("The winner is X!");
      }
      if (Grid[3] == Grid[5] && Grid [5] == Grid[7] && Grid[3] == 2){
          gameRun = false;
          System.out.println("The winner is O!");
      }
  } 
    public static void main (String[] args) throws IOException {
     System.out.println("Welcome to Denis' Tic Tac Toe game!");
  TicTacToe Grid = new TicTacToe();
   Grid.gridReset();
   Grid.printGrid();
   int x = 0;
   while ( Grid.gameRun == true){ 
    if (Grid.player == true){
          System.out.println("Where do you want to place (X)?");
    }
    else {
          System.out.println("Where do you want to place (O)?");
    }
    Grid.convert();
    Grid.printGrid1();
    Grid.checkWin();
   if (Grid.roundNo == 9){
        Grid.gameRun = false;
        System.out.println("It's a tie!");
    }
       }
            }
    }
