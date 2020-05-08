 import java.io.*;
 import java.util.*;
 
 public class parseText {
 
     public static void main(String args[]) {
         /*The following program will take a file, defined by changing
         the fileName string, formatted in the style of the SSL observatory
         dump, parse it, and save a csv in the same folder. The outputted csv
         will have the following columns:
                 
         Column 1: SHA Fingerprint
         Column 2: Unique ID number
        Column 3: IP Address
         Column 4: Public Modulus
         Column 5: The size of the Modulus
         Column 6: The Signature         */
         
         
         String rfileName = "valid-certs.csv";
         String wfileName = "formatted-certs.csv";
         int lineNum = 1;
         
         try {
             FileWriter fstream = new FileWriter(wfileName, false);
             BufferedWriter out = new BufferedWriter(fstream);
             
             BufferedReader in = new BufferedReader(new FileReader(rfileName));
             String line = in.readLine();
         
            while (line != null) {             
                 if (line != null && !line.isEmpty()) {
                     //TESTING
                     if (lineNum%1000 == 0){
                         System.out.println(lineNum);
                     }
                     //TESTING
                     out.write(parse(line));
                     out.newLine();
                }               
                //Itterate the lineNum variable and read the next line
                 lineNum++;
                line = in.readLine();
             }
             in.close();
             out.close();
         } catch (Exception e) {
             System.out.println("*****************************\n" +
             "ERROR: The File crash on line " + (lineNum + 1) + 
             "\n*****************************");
         }
     }
     
     public static String parse(String text) {
        /*This function will take as its input a line of text and strip
         away all the none-essential information and leave only the
         pieces of information outlines in main. Each line contains
         70 fields.
         
         input: String textLine
             a line of text with 70 comma delianted fields just asking to
             get their faces parsed.
             
         output: String retString
             a comma deliniated string with values: 
                 Column 1: SHA Fingerprint
                 Column 2: Unique ID number
                Column 3: IP Address
                 Column 4: Public Modulus
                 Column 5: The size of the Modulus
                 Column 6: The Signature         */       
         
         String retString = "";
         
         //The text cannot be parsed on commas because there are certain fields
         //(indentification data) that include commas between quotes meaning
         //their input is a string. There are two types of fields, strings 
         //inclosed in quotes and "\N". We resolve it as follows
         String temp = text.replace("\\N", "\"\\N\"");
         //By splitting the text on quotes we generate an array of twice the 
         //size of the original input
         String[] splitLineReplaced = temp.split("\"");
         
         String[] splitLine = new String[70];
                         
         if (splitLineReplaced.length == 140) {
             //Here the quote deliniated array will only select the text inside 
             //the quotes
             int a = 1;
             for (int i = 0; i < splitLine.length; i++) {
                 splitLine[i] = splitLineReplaced[a];
                 a+=2;
             }
         
             //And finally we put together the information we care about
             retString = splitLine[2] + "," + splitLine[3] +
                                "," + splitLine[4] +"," + splitLine[9] + "," +
                                splitLine[10] + "," + splitLine[12];   
         }
         else {
             retString = "na,na,na,na,na,na";
         }     
         return retString;
     }
 }
 
 
 
 
 
