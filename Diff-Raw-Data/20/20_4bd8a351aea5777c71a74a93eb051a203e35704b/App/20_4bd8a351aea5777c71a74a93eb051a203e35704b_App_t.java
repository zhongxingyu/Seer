 package net.edmacdonald.playground;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class App
 {
     final static String ESC = "\033[";
 
     public static void main( String[] args )
     {
         int width = 40;
         int height = 40;
 
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
        Board conway;

        try{
            conway = new Board(AsciiStateTransformer.getCoordinatesFromFile(args[0]));
        }
        catch (Exception e){
            throw new RuntimeException("Did you specify an input file you can read from?", e);
        }
         GridModelRenderer conwayRenderer = new GridModelRenderer();
 
         conwayRenderer.renderGridModel(AsciiStateTransformer.getModelFromCoordinates(conway.getLivingCells()), width, height);
 
         try{
             //while(in.readLine() != null){
             while(true){
                 Thread.sleep(200);
                 System.out.print(ESC + "2J"); System.out.flush();
                 conway.tick();
                 conwayRenderer.renderGridModel(AsciiStateTransformer.getModelFromCoordinates(conway.getLivingCells()), width, height);
             }
         }
         catch (Exception e){
             throw new RuntimeException(e);
         }
     }
 }
