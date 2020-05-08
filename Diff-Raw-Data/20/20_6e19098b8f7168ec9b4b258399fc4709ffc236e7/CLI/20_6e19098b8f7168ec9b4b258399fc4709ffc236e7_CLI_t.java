 package ui;
 
 import java.util.Random;
 import java.util.Scanner;
 
 import logic.Maze;
 import logic.MazeGenerator;
 import utils.Key;
 
 public class CLI
 {
     private static Maze _maze;
     private static Scanner _sc = new Scanner(System.in);
 
     public static void main(String[] args)
     {
         _maze = MazeGenerator.RandomMaze(20, 2, new Random());
 
         while (!_maze.IsFinished())
         {
             boolean success = true;
             System.out.println(_maze);
             System.out.print("Move hero (W, S, A, D): ");
             do
             {
                 if (!success)
                 {
                     System.out.print("Invalid move. Try again: ");
                     success = true;
                 }
 
                 String inputStr = _sc.nextLine();
                if (inputStr.isEmpty() || inputStr.length() > 1)
                {
                    success = false;
                    continue;
                }
 
                 Key input = Key.toEnum(inputStr.toUpperCase().charAt(0));
 
                if (input == null)
                {
                    success = false;
                    continue;
                }
 
                 success = _maze.MoveHero(input);
 
             } while (!success);
 
             System.out.print("\n\n");
 
             success = false;
 
             _maze.Update();
         }
 
         System.out.println(_maze);
         if (_maze.IsHeroAlive())
             System.out.println("You Won!");
         else
             System.out.println("You Lost!");
     }
 }
