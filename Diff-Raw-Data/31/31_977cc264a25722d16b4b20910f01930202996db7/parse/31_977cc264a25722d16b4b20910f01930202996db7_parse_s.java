 import java.util.*;
 import java.io.*;
 
 public class parse {
   public static boolean noproblem = true;
   public static void main(String[] args) {
     //recordtest.test();
     
     String input = "null";
     if (args.length == 0) {
       Scanner csc = new Scanner(System.in);
       System.out.println("Please enter the name of the input file: ");
       input = csc.next();
     } else {
       input = args[0];
     }
     File nfile = new File(input);
     try {
     Scanner sc = new Scanner(nfile);
     Record.initRecord(); 
     //System.out.println(input);
     for (int i=0; sc.hasNextLine(); i++) {
       Record.newRecord();
       input = sc.nextLine();
       //System.out.println(input);
       parseRecord(i, input);
       
       if (args.length == 2) 
         if (args[1].equals("-d"))
           recordtest.test(i);
       //recordtest.testDB();
 //      recordDB.title_DB.close();
       //    recordDB.artists_DB.close();
       //recordDB.userrat_DB.close();
     }
     recordDB.configDB();
    recordtest.testDB();
     sc.close();
     } catch (FileNotFoundException e) {
       System.err.print("FileNotFoundException: ");
       System.err.println(e.getMessage());
     }
     
   }
   private static void parseRecord(int i, String input) {
     input = input.trim();
     input = input.substring(1);
     parseID(i, input);
   }
   private static void parseID(int i, String input) {
     String output;
     int indx = 0;
     int id;
     if (input.charAt(0) == '[') {
       indx = input.indexOf("]");
       //System.out.println(indx);
       output = input.substring(1, indx);
       //System.out.println(output);
       id = Integer.parseInt(output);
       Record.addID(i, id);
     } else {
       System.out.println("There is a file format problem, expected '[' but got '" +
                          input.charAt(0) + "' on line " + i);
       noproblem = false;
     }
     output = input.substring(indx+2);
     parseTitle(i, output.trim());
   }
   private static void parseTitle(int i, String input) {
     String output;
     int indx = 0;
     String title;
     if (input.charAt(0) == '[') {
       indx = input.indexOf("]");
       title = input.substring(1, indx);
       Record.addTitle(i, title);
     } else {
       System.out.println("There is a file format problem, expected '[' but got '" +
                          input.charAt(0) + "' on line " + i);
       noproblem = false;
     }
     output = input.substring(indx+2);
     parseArtists(i, output.trim());
   }
   private static void parseArtists(int i, String input) {
     String output;
     int indx = 0;
     int indxin = 0;
     String artists;
 
     if (input.charAt(0) == '[') {
       indx = input.indexOf("]");
       artists = input.substring(1, indx);
       while (artists.indexOf(",") != -1) {
         indxin = artists.indexOf(",");
         Record.addArtist(i, artists.substring(0, indxin));
         artists = artists.substring(indxin+1);
         artists = artists.trim();
       }
       Record.addArtist(i, artists);
     } else {
       System.out.println("There is a file format problem, expected '[' but got '" +
                          input.charAt(0) + "' on line " + i);
       noproblem = false;
     }
     output = input.substring(indx+2);
     parseUserRat(i, output.trim());
   }
   private static void parseUserRat(int i, String input) {
     int indx = 0;
     int cindx = 0;
     String sinput = input;
     String user;
     int rating;
     if (sinput.charAt(0) == '[') {
       sinput = sinput.substring(1);
       while (sinput.charAt(0) == '(') {
         cindx = sinput.indexOf(",");
         indx = sinput.indexOf(")");
         user = sinput.substring(1, cindx);
         rating = Integer.parseInt(sinput.substring(cindx+1, indx));
         Record.addUserRat(i, user, rating);
         sinput = sinput.substring(indx+2);
       }
     } else {
       System.out.println("There is a file format problem, expected '[' but got '" +
                          input.charAt(0) + "' on line " + i);
       noproblem = false;
     }
   }
 }
