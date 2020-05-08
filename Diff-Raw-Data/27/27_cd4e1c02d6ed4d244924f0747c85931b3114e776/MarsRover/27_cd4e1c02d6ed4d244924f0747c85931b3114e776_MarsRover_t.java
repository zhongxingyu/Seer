 package main;
 
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MarsRover {
 
 
 
     public List<String> readFromFile(String inputFile) throws IOException {
         List<String> input =  new ArrayList<String>();
             File inFile = new File(inputFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(
                     new FileInputStream(inFile)));
         String strLine;
         while ((strLine = br.readLine()) != null ) {
                 input.add(strLine);
             }
         return input;
     }
 
 
     public static void main(String[] args) throws IOException {
         MarsRover marsRover = new MarsRover();
         List<String> input = marsRover.readFromFile("C:\\Users\\sabhinay\\Mars-Rover\\Mars-Rover1\\src\\main\\input.txt");
        //String plateau=input.get(0);
         RoverExecutor.executeRovers(input);
         }
 
 }
