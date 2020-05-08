 package fileSplitter;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Scanner;
 import static java.lang.Math.*;
 import javax.swing.JOptionPane;
 import static utilities.TextUtil.*;
 import errorChecking.*;
 
 /**
  * This is the class that handles most of the issues of increasing the
  * tempo of a file without going over the 96-measure limit.
  * @author RehdBlob
  * @since 1.00a
  * @since 2011.0802
  */
 public class FileSplitter {
     private String oldText = "";
     private String file    = "";
     private String start   = "";
     private String end     = "";
     private int splitLine;
     private Integer multiplier = 1;
     private ArrayList<String> text = new ArrayList<String>();
     /*public final int [] BP = {1429 , 1464, 1819, 1936, 2223, 2308, 2609,
 			2728, 2858, 3334, 3530, 4616, 5001, 5455, 8572, 10001, 12001,
 			15001, 20001, 60000, -1, -60000};
      */
 
     /**
      * Default class; initializes the file, oldText,
      * start, end, and text instances.
      * @param fileName The name of the file that we're looking at.
      * @throws Exception Bubbles up any exceptions thrown here.
      */
     public FileSplitter(File fileName) throws Exception {
         Scanner f = new Scanner(fileName);
         file      = fileName.getName();
         oldText   = f.nextLine();
         start     = getStart(oldText);
         end       = getEnd(oldText);
         text      = chop(slice(clean(oldText)));
        f.close();
     }
 
     /**
      * Backs up the old MPC text file into a text file with
      * "old" appended to the end of the file.
      * @throws Exception If the PrintStream is not able to
      * write the file.
      */
     public void writeOldFile() throws Exception {
         PrintStream writer = new PrintStream(
                 file.substring(0,file.indexOf(']')) + "old]MarioPaint.txt");
         writer.print(oldText);
         writer.close();
     }
 
     /**
      * Gets the number of times the user wishes to multiply the tempo
      * of an MPC song file by.
      * @throws Exception Bubbles up any exceptions to the next level.
      */
     public void getMultiplier() throws Exception {
         Object st = null;
         st = JOptionPane.showInputDialog(null,
                 "Increase tempo by how many times?\n" +
                         "Please type an integer.", "File Splitter",
                         JOptionPane.INFORMATION_MESSAGE, null, null, "1");
         if (st == null)
             return;
         multiplier = Integer.parseInt((String)st);
         if(multiplier==1)
             throw new SingleMultiplierException();
         Object [] op = {"96 measures", "Equal Spacing", "Cancel"};
         int st1 = JOptionPane.showOptionDialog(null, "96 measures per file" +
                 " or equally-sized files?", "File Splitter",
                 JOptionPane.YES_NO_CANCEL_OPTION,
                 JOptionPane.PLAIN_MESSAGE, null, op, op[1]);
         switch (st1) {
         case 0:
             splitLine = 384;
             break;
         case 1:
             splitLine = totalLines(text);
             break;
         default:
             throw new CancelException();
         }
     }
 
 
     /**
      * Creates as many new files that the new tempo of an MPC
      * text file requires; aka. if there are more than 96 measures,
      * (384 lines), more than one file will be made.
      * @throws Exception If the PrintStream is not able to write the
      * files.
      */
     public void writeNewFiles() throws Exception {
         multiplyTempo();
         String colon = "";
         for(int i = 1; i < multiplier; i++)
             colon+=":";
         append(colon);
         if (totalLines(text) <= 384) {
             PrintStream writer = new PrintStream(
                     file.substring(0,file.indexOf(']'))
                     + "]MarioPaint.txt");
             writer.print(start);
             for (String s : text)
                 writer.print(s);
             writer.print(end);
             writer.close();
             return;
         }
 
         ArrayList<Fraction> pieces = new ArrayList<Fraction>();
         int divisions = (int)ceil((double)totalLines(text) / splitLine);
 
         for(int i = 1; i < divisions; i++)
             pieces.add(new Fraction(i,divisions));
         PrintStream writer = new PrintStream(
                 file.substring(0,file.indexOf(']'))
                 + "]MarioPaint.txt");
         if(text.size()==0) {
             writer.close();
             throw new NoOutputException();
         }
         writer.print(start);
         for (int i = 0; i < floor (384 / multiplier) ; i++) {
             writer.print(text.remove(0));
         }
         writer.print(end);
         writer.close();
         for(int i = 0; i < pieces.size(); i++) {
             String files  = file.substring(0,file.indexOf(']'))
                     + " " + pieces.get(i).toString().replace('/', '_')+"]MarioPaint.txt";
             String files2 = file.substring(0,file.indexOf(']'))
                     + " " + pieces.get(i).toString()+"]MarioPaint.txt";
 
             writer = new PrintStream(files);
 
             writer.print(start);
             for (int j = 0; j < floor (384 / multiplier) ; j++) {
                 if (text.size() == 0)
                     break;
                 writer.print(text.remove(0));
             }
             writer.print(end);
             writer.close();
             try {
                 Scanner file = new Scanner(new File("MarioPaintSongList.txt"));
                 String reprint = file.nextLine();
                 writer = new PrintStream("MarioPaintSongListold.txt");
                 writer.print(reprint);
                 writer.close();
                 writer = new PrintStream("MarioPaintSongList.txt");
                 writer.print(reprint);
                 writer.print(files2.substring(0,files.indexOf('.'))+"*");
                 writer.close();
                 file.close();
             } catch (FileNotFoundException e) {
                 throw new NoSongListException();
             }
         }
     }
 
     /**
      * @since 1.05
      * @since 2011.1110
      * @param colon The number of colons to be added to the end of an instrument
      * denotation, which is indicative of how many times the tempo is to be
      * multiplied by.
      */
     private void append(String colon) {
         ArrayList<String> newText = new ArrayList<String>();
         for (String s : text) {
             s += colon;
             newText.add(s);
         }
         text = newText;
     }
 
     /**
      * Multiplies the <b>end</b> String by whatever multiplier
      * that the <b>getMultiplier()</b> function returned.
      */
     private void multiplyTempo() {
         int tempo = Integer.parseInt(end.substring(end.indexOf('%')+1));
         tempo *= multiplier;
         end = "%" + tempo;
     }
 
     /**
      * @param txt The ArrayList of notes data from the <b>chop</b>
      * method.
      * @return The total number of colons ":" in the ArrayList.
      */
     private int totalLines(ArrayList<String> txt) {
         int total = 0;
         for (String s : txt)
             total += (s.lastIndexOf(':')-s.indexOf(':') + 1);
         return total;
     }
 
 }
