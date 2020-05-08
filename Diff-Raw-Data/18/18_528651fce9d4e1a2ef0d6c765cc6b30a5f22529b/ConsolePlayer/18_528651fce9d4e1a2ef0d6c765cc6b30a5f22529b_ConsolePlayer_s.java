 package fr.iutvalence.java.mp.tictactoe;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 public class ConsolePlayer implements Player
 {
 
     public Position getChoice()
     {
         InputStreamReader isr = null;
         try
         {
             isr = new InputStreamReader(System.in, "US-ASCII");
         }
         catch (UnsupportedEncodingException e1)
         {
             // ignore this, it can not occur
         }
         BufferedReader br = new BufferedReader(isr);
         String s = null;
         
         try
         {
             // TODO (fix) Fix completely fucked up method
             s = br.readLine();
             int column = Integer.parseInt(s);
             System.out.println(column);
             int line = Integer.parseInt(s);
             System.out.println(line);
             isr.close();
             br.close();
             return new Position(column, line);
         }
         catch (IOException e)
         {
             return new Position(-1, -1);
         }
     }
     
 }
