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
 public void gridReset (){
     for(int i = 0; i < Grid.length; i++)
     {Grid[i] = 0;}
     for(int i = 0; i < textGrid.length; i++)
     {textGrid[i] = " ";  }
 }
 public void convert() throws IOException{
  int input = Integer.parseInt(TicTacToe.Input());
 if (Grid[input] == 0){
         if (player == true){
          Grid[input] = 1;
 player = false;
         }
 else {
      Grid[input] = 2;
     player = true;
 }
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
  
    public static void main (String[] args) throws IOException {
     System.out.println("Welcome to Denis' Tic Tac Toe game!");
  TicTacToe Grid = new TicTacToe();
   Grid.gridReset();
   Grid.printGrid();
   while ( Grid.gameRun == true){ 
    if (Grid.player == true){
          System.out.println("Where do you want to place (X)?");
    }
    else {
          System.out.println("Where do you want to place (O)?");
    }
    Grid.convert();
    Grid.printGrid1();
  if (Grid.Grid[4] == Grid.Grid[5] && Grid.Grid [5] == Grid.Grid[6] && Grid.Grid[5] != 0){
       Grid.gameRun = false;
   }
       }
            }
    }
