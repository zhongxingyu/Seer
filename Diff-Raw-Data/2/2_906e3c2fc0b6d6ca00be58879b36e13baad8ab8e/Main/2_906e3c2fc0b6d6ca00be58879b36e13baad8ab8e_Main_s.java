 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.StringTokenizer;
 
 
 public class Main {
     public static void main(String[] args) {
         boolean useLru    = args[0].matches("1");
         String inputFile  = args[1];
         String outputFile = args[2];
 
         MemoryManagementSystem memory = new MemoryManagementSystem(useLru);
         read_From_File(inputFile,outputFile,memory); // MAKE SURE outputFile DOESN'T EXIST, OR ALL OUTPUT WILL BE APPENDED!
     }
 
     public static void read_From_File(
            tring inputFilename,
             String outputFileName,
             MemoryManagementSystem memory) {
         try {
             File inFile        = new File(inputFilename);
             FileReader ifr     = new FileReader(inFile);
             BufferedReader ibr = new BufferedReader(ifr);
 
             String line = "";
             while (line != null) {
                 line = ibr.readLine();
                 if (line != null) {
                     StringTokenizer st = new StringTokenizer(line);
                     while (st.hasMoreTokens()) {
                         String token = st.nextToken().trim();
                         if (token.equals("read")) {
                             memory.read(Integer.parseInt(st.nextToken().trim()));
                         }
                         if (token.equals("write")) {
                             int index = Integer.parseInt(st.nextToken().trim());
                             char c = st.nextToken().trim().charAt(0);
                             memory.write(index, c);
                         }
                         if (token.equals("print")) {
                             write_To_File(outputFileName, memory.toString());
                         }
                     }
                 }
             }
 
             ibr.close();
             ifr.close();
         }
 
         catch (Exception e) {
             System.out.println("Error \"" + e.toString() + "\" on file " + inputFilename);
             e.printStackTrace();
             System.exit(-1);  // Brutally exit the program.
         }
     }
 
     private static void write_To_File(String outputFilename, String toWrite) {
         try {
             File outFile   = new File(outputFilename);
             FileWriter ofw = new FileWriter(outFile,true);
 
             // Writing to file
             ofw.append(toWrite);
             ofw.append("\n");
             ofw.close();
         } catch (Exception e) {
             System.out.println("Error \"" + e.toString() + "\" on file " + outputFilename);
             e.printStackTrace();
             System.exit(-1); // brutally exit the program
         }
     }
 }
 
