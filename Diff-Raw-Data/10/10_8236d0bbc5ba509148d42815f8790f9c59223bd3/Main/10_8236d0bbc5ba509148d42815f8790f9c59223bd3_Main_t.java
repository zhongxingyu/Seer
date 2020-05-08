 package com.tipumc;
 import java.io.*;
 import java.util.Vector;
 
 
 public class Main {
 
     public static Vector<String> loadBoard(String filename) throws IOException
     {
         Vector<String> board = new Vector<String>();
 
         BufferedReader br = new BufferedReader(new FileReader(filename));
         //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         String line;
         while(br.ready()) {
             line = br.readLine();
             board.add(line);
         } // End while
 
         return board;
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) throws IOException, Exception {
 
         Vector<String> board = loadBoard("maps/test.in");
 
         Map map = new Map(board);
        System.err.println(map.toString());
         State initialState = new State(map, board);
         String toPrint = new String();
         
         toPrint = "";
 
         // Access
         //char = board.get(row).charAt(col);
         
         
 
         Iterable<Direction> moves = Solver.solve(initialState);
         for (Direction move : moves)
         {
             toPrint += move.toString() + " ";
             //System.out.print(move.toString());
         }
         
         System.out.println(toPrint);
     } // main
 } // End Main
