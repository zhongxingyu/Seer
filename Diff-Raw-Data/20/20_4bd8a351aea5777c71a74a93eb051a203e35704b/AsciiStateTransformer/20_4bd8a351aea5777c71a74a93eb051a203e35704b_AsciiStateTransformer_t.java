 package net.edmacdonald.playground;
 
 import com.google.common.collect.Lists;
 import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 public class AsciiStateTransformer {
     /**
      * This method will take input that looks like this:
      *
      * "**  "
      * "**  "
      * "  **"
      * "  **"
      *
      * And create a set of coordinates from it.
      * Note that the example above is the beacon oscillator.
      *
      * @param state
      * @return
      */
     public static Set<Coordinate> getCoordinatesForState(List<String> state){
         Set<Coordinate> coordinates = new HashSet<Coordinate>();
         int x=0;
         int y=0;
         for(String s: state){
             for(Character c: Lists.charactersOf(s)){
                 if(c.equals('*')){
                     coordinates.add(new Coordinate(x,y));
                 }
                 x++;
             }
             x=0;
             y++;
         }
 
         return coordinates;
     }
 
     public static Set<Coordinate> getCoordinatesFromFile(String fileName){
         try{
             return getCoordinatesForState(FileUtils.readLines(new File(fileName)));
         }
         catch (IOException e){
             throw new RuntimeException(e);
         }
     }
 
     public static AsciiGridModel getModelFromCoordinates( final Set<Coordinate> coordinates ) {
         return new AsciiGridModel() {
             @Override
             public Boolean livingCellAt(Coordinate coordinate) {
                 return  coordinates.contains(coordinate);
             }
         };
     }
 }
