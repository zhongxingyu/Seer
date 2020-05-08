 import java.util.Scanner;
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.FileNotFoundException;
import java.io.InputStream;
 
 public class Simulation {
     private Grid grid;
     private int stepNumber;
     private static Map<Character, String> symbolToClassnameMap;
     private static Map<String, Character> classnameToSymbolMap;
     private static boolean filesParsed = false;
     
     public Simulation(int gridSize) {
         Debug.echo("Constructing a new Simulation object");
         if(!filesParsed){
             parseSymbolMap();
             parseFoodWeb();
             filesParsed = true;
         }
         grid = new Grid(gridSize);
         stepNumber = 0;
     }
 
    public Simulation(InputStream animals, InputStream terrain){ 
         Debug.echo("Constructing a new Simulation object");
         
         if(!filesParsed){
             parseSymbolMap();
             parseFoodWeb();
             filesParsed = true;
         }
         parseGrid(animals, terrain);
         stepNumber = 0;
     }
 
     public void step() {
         stepNumber++;
         Debug.echo("Step:"+stepNumber);
         
         Location[] locations = grid.getLocations();
         Collections.shuffle(Arrays.asList(locations));
         GridSquare current;
         Location loc;
         ArrayList<Organism> organisms = new ArrayList<Organism>();
         for (int idx = 0; idx < locations.length; idx++){
             loc = locations[idx];
             current = grid.get(loc);
             
             //Also add in the location?!?!?
             if(current.getPlant() != null) {
                 organisms.add(current.getPlant());
             }
             if(current.getAnimal() != null) {
                 organisms.add(current.getAnimal());
             }
         }
         for (Organism o: organisms) {
             o.step(grid);
         }
     }
     public void parseSymbolMap() {
         symbolToClassnameMap = new HashMap<Character, String>();
         classnameToSymbolMap = new HashMap<String, Character>();
         try {
            Scanner scanner = new Scanner(Util.stream("resources/symbols.dat"));
             Character symbol;
             String className, line;
             String[] words;
             while(scanner.hasNext()){
                 line = scanner.nextLine().trim();
                 words = line.split("\\s+");
                 symbol = (words[0].toCharArray())[0];
                 className = words[1];
                 symbolToClassnameMap.put(symbol, className);
                 classnameToSymbolMap.put(className, symbol);
             }
         } catch (Exception e) {
             Debug.echo("SymbolMap: Invalid file format! "+e);
         }
     }
     public void parseFoodWeb() {
         Debug.echo("Here is where I would parse the food web file");
 
         try {
             Scanner scanner = new Scanner(new File("resources/foodWeb.dat"));
             while (scanner.hasNextLine()) {
                 String line = scanner.nextLine().trim().replaceAll("[^A-Za-z0-9:;#]", " ");
                 if (line.size() == 0 || line.substring(0, 1).equals("#")) {
                     continue; // do nothing if it's a comment
                 }
 
                 String[] contents = line.split(":");
 
                 // get name of predator
                 String predator = contents[0].trim();
 
                 String[] info = contents[1].split(";");
 
                 // get calories value
                 Double calories = new Double(info[0].trim().replaceAll("\\s*", ""));
 
                 // plant info goes here
                 if (predator.equals("Grass")) {
                     Grass.setCalories(calories);
                 } else if (predator.equals("Carrot")) {
                     Carrot.setCalories(calories);
                 }
 
                 // get names of prey
                 if (info.length > 1) {
                     String[] prey = info[1].trim().split("\\s+");
                     for (String p : prey) {
                         if (predator.equals("Rabbit")) {
                             Rabbit.addPrey(p);
                             Rabbit.setCalories(calories);
                         } else if (predator.equals("Fox")) {
                             Fox.addPrey(p);
                             Fox.setCalories(calories);
                         }
 
                         // Note: Plants don't need to know their predators
                         if      (p.equals("Rabbit")) Rabbit.addPredator(predator);
                         else if (p.equals("Fox"))    Fox.addPredator(predator);
                     }
                 }
 
                 if (info.length > 2) {
                     String[] hidingSpots = info[2].trim().split("\\s+");
                     for (String h : hidingSpots) {
                         if (predator.equals("Rabbit")) {
                             Rabbit.addHidingSpot(h);
                         } else if (predator.equals("Fox")) {
                             Fox.addHidingSpot(h);
                         }
                     }
                 }
             }
         } catch (FileNotFoundException e) {
             Debug.echo("FoodWeb: File not found!");
         } catch (Exception e) {
             Debug.echo("FoodWeb: Invalid file format! "+e);
         }
     }
    public void parseGrid(InputStream animals, InputStream terrain) {
         parseTerrain(terrain);
         parseAnimals(animals);
         Debug.echo("Parsing grid from file");
     }
    private void parseAnimals(InputStream animals){
         try {
             Scanner scanner = new Scanner(animals);
             int gridSize = new Integer(scanner.nextLine());
             if(gridSize != grid.getGridSize()){
                 throw(new Exception("Grid sizes not equal."));
             }
             String line, className;
             int row = 0;
             int col = 0;
             Animal current;
             Location loc;
             while (scanner.hasNext()) {
                 col = 0;
                 line = scanner.nextLine();
                 line = line.replaceAll("[^A-Za-z_]", "");
                 for (char symbol : line.toCharArray()){
                     className = symbolToClassnameMap.get(symbol);
                     if (className != null) {
                         loc = new Location(row, col);
                         // IF MORE ANIMALS ARE ADDED, ADD THEM HERE (IN AN ELSE IF)
                         if (className.equals("Rabbit")) {
                             current = new Rabbit(loc);
                         } else if (className.equals("Fox")) {
                             current = new Fox(loc);
                         } else {
                             current = null;
                         }
                         grid.addAnimal(current, loc);
                     }
                     col++;
                 }
                 row++;
             }
         } catch (Exception e) {
             Debug.echo("Animals: Invalid file format! "+e);
         }
     }
    private void parseTerrain(InputStream terrain){
         try {
             Scanner scanner = new Scanner(terrain);
             int gridSize = new Integer(scanner.nextLine());
             grid = new Grid(gridSize);
             String line, className;
             int row = 0;
             int col = 0;
             Plant current;
             Location loc;
             while (scanner.hasNext()) {
                 col = 0;
                 line = scanner.nextLine();
                 line = line.replaceAll("[^A-Za-z_]", "");
                 for (char symbol : line.toCharArray()){
                     className = symbolToClassnameMap.get(symbol);
                     if (className != null) {
                         loc = new Location(row, col);
                         // IF MORE PLANTS ARE ADDED, ADD THEM HERE (IN AN ELSE IF)
                         if (className.equals("Grass")) {
                             current = new Grass(loc);
                         } else if (className.equals("Carrot")) {
                             current = new Carrot(loc);
                         } else {
                             current = null;
                         }
                         grid.addPlant(current, loc);
                     }
                     col++;
                 }
                 row++;
             }
         } catch (Exception e) {
             Debug.echo("Terrain: Invalid file format! "+e);
         }
     }
     public Grid getGrid() {
         return grid;
     }
     public static String symbolToClassname(Character symbol){
         return symbolToClassnameMap.get(symbol);
     }
     public static Character classnameToSymbol(String classname){
         return classnameToSymbolMap.get(classname);
     }
     public String toString() {
         String output = "";
         output+="SymbolMap="+symbolToClassnameMap.keySet().toString()+"\n"+symbolToClassnameMap.values().toString()+"\n";
         output+="Grid=\n"+grid.toString();
         return output;
     }
 }
