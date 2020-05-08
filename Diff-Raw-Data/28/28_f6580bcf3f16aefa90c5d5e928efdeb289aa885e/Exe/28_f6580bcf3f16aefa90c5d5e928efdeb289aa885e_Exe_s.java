 import java.io.*;
 import java.util.Scanner;
 
 public class Exe
 {
 	static int x = 5;
 	static int y = 5;
 	static int z = 5;
     static int[][][] location;
     static String chName = "";
     static final String EOL = System.getProperty("line.separator");
 
 	public static void main (String[] args)
 	{
 		System.out.println(
             "Hello there. Welcome to Isaac and Alison's " + EOL +
             "Glorious Text Adventure For Great Victory (TM & C)!" + EOL +
             "To get started, please type your character's name.");
        System.out.print(">"); chName = readInput();
         System.out.println(EOL +
             "Why hello there " + chName + ". Why don't you " + EOL +
             "check out your surroundings by typing 'look'?");
		System.out.print(">"); Verbs.parse(readInput());
         System.out.println(EOL +
             "Wow! What a cool place. Why don't you examine the" + EOL +
             "box? Type 'examine box' to take a closer look at the box.");
        System.out.print(">"); Verbs.parse(readInput());
         System.out.println(EOL +
             "Alright, from here on you're on your own." + EOL +
             "You can type 'help' or '?' to see a list of commands.");
         while(true){
             System.out.println();
             System.out.print(">"); Verbs.parse(readInput());
         }
     }
 
     public static String getChName(){
         return chName;
     }
 
     public static int[] getCoords(){
         return new int[] {x, y, z};
     }
 
     public static int getX(){
         return x;
     }
 
     public static int getY(){
         return y;
     }
 
     public static int getZ(){
         return z;
     }
 
     public static void setCoords(int[] coords){
         x = coords[0];
         y = coords[1];
         z = coords[2];
     }
 	
     public static void setX(int x){
         x = x;
     }
 
     public static void setY(int y){
         y = y;
     }
 
     public static void setZ(int z){
         z = z;
     }
 
     public static String readInput()
 	{
 //      Scanner scan = new Scanner(System.in);
 //      String input = scan.nextLine();
         return new Scanner(System.in).nextLine(); 
 //      return input;
 
 //      while (scan.hasNext())
 //      	input += scan.next();
 //      	if (input != null)
 //       	  return input;
 //          else
 //       	  return null;	
 //
 
     } 
     
     /**
     * Reads in the file defining the rooms in the world. 
     * File should be in the following format:<br />
     * <br />
     * "TOTALNUMBEROFROOMS 3<br />
     * 1 1 1 1<br />
     * This is a description of room 1.<br />
     * F F F F T F<br />
     * 2 1 2 1<br />
     * This is a description of room 2.<br />
     * F F T F F F<br />
     * 3 2 2 1<br />
     * This is a description of room 3.<br />
     * F F T F F F<br />
     * ...<br />
     * 1000 x y z<br />
     * This is a description of room 1000.<br />
     * NExit SExit EExit WExit UpExit DownExit"<br />
     * <br />
     * The first number is the room ID, and the last three numbers are the room's
     * x, y, and z coordinates, respectively. Worlds cannot exceed 1000 rooms 
     * (i.e. a 10x10x10 three dimension array). 
     */
     public static void readSaveFile (String pathname)
     {
     	try {
             Scanner scanPlace = new Scanner(new File (pathname));
             Scanner scanItems = new Scanner(new File (pathname)).useDelimiter("\\d");
 
             int currentLocationX;
             int currentLocationY;
             int currentLocationZ;
             int numCurrentItems;
             ItemBag currentItems = new ItemBag();
             // currentItems.numberOfItems = numCurrentItems; Is this needed?
              
             currentLocationX = scanPlace.nextInt();
             currentLocationY = scanPlace.nextInt();
             currentLocationZ = scanPlace.nextInt();
             numCurrentItems = scanItems.nextInt();
 
             for (int i = 0; i < numCurrentItems; i++) 
                 currentItems.addItem(new Item(scanItems.next()));
             }
         catch (FileNotFoundException ex)
         {
             System.out.println("File not found.");
         }
     }
     
     public static void saveFile ()
     {
         
     }     
  
 	public static void addRoom (Room r)
 	{
 		
 	}
 }
 
