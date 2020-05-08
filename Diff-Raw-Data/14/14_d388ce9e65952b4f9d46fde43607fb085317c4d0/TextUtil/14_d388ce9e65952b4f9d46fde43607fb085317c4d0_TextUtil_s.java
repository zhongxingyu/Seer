 package utilities;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import javax.swing.JOptionPane;
 import errorChecking.NoTextException;
 
 
 
 /**
  * Basically a class to hold some useful methods that
  * were programmed and seem to be used a lot.
  * Since v1.07, we have moved onto using regexes.
  * @author RehdBlob
  * @since 1.05
  * @since 2011.0902
  */
 public class TextUtil {
 
     /** This is the regex pattern that denotes a note in an MPC text file. */
     private static String note = "[a-z]?[a-z]?[#;]?";
 
     /** This is the regex pattern that denotes a kind of a concat operator. */
    private static String plus = "+?";
 
     /**
      * This is the regex pattern that denotes a kind of a concat operator but
      * forces you to have at least one of them.
      */
    private static String plusForce = "+{1}";
 
     /** This is the regex pattern that denotes a volume reading. */
     private static String vol = "[a-z]?:";
 
     /**
      * This is a regex pattern that catches a note and a plus sign
      * of the form ab;+
      */
     private static String noteCat = note + plus;
 
     /**
      * This is a regex pattern that catches a note and a plus sign
      * of the form ab;+ but forces you to catch the plus.
      */
     private static String noteCatForce = note + plusForce;
 
     /**
      * This is the regex pattern that denotes a line of notes.
      * The format must be able to catch all lines in the form
      * ab+cd+ef+gh+ij+q:
      * or ++++++q:
      * or :
      */
     private static String noteLine = noteCat + noteCat + noteCat + noteCat
             + noteCat + vol;
 
     /**
      * This is the regex pattern that denotes a line of notes.
     * The format will be able to catch all of the linse in the form
      * ab+cd+ef+gh+ij+q:
      * or ++++++q:
      * but not :
      */
     private static String noteLineForce = noteCatForce + noteCatForce
             + noteCatForce + noteCatForce + noteCatForce + noteCatForce
             + vol;
 
     /**
      * This is the pattern that denotes a line of notes.
      */
     private static Pattern line = Pattern.compile(noteLine);
 
     /**
      * This is the pattern that denotes a line of notes that will have
      * at least some form of volume.
      */
     private static Pattern lineForce = Pattern.compile(noteLineForce);
 
     /**
      * Uses regex to clean up an MPC text file
      * @param t The song text file to be "cleaned," that is, the
      * song file that has unnecessarily empty note lines.
      * @return The "cleaned" string.
      * @since 1.07
      * @since 2013.0205
      */
     public static String clean(String t) {
        return t.replaceAll("++++++q:", ":");
     }
 
 
     /**
      * Displays an information dialog given a title and text to
      * display.
      * @since 1.06
      * @since 2011.1127
      * @param title The title of the dialog box.
      * @param text The text to be displayed in the text box.
      * @throws NoTextException If the <b>text</b> parameter happens
      * to be <b>null</b> or equals the empty "" string.
      */
     public static void infoDialog(String title, String text)
             throws NoTextException {
         if (text == null | text.equals(""))
             throw new NoTextException();
         JOptionPane.showMessageDialog(null, text, title,
                 JOptionPane.INFORMATION_MESSAGE);
     }
 
     /**
      * @since 1.05
      * @since 2011.1108
      * @param s The song text file that is to be "chopped" into
      * note lines.
      * @return All note lines that have data in them.
      */
     public static ArrayList<String> chop(String s) {
         s = clean(s);
         ArrayList<String> result = new ArrayList<String>();
         Matcher m = line.matcher(s);
         while (m.find()) {
             result.add(m.group());
         }
         return result;
     }
 
     /**
      * @since 1.05
      * @since 2011.1108
      * @param read The song text file that is to be "sliced" into
      * just the note data.
      * @return The song text file with the time signature and tempo removed.
      */
     public static String slice(String read) {
         return read.substring(read.indexOf('*') + 1,read.lastIndexOf(':') + 1);
     }
 
     /**
      * Splits a single MPC note line into its singular
      * instrumental components.
      * @param s The MPC note line to be converted into an ArrayList.
      */
     public static ArrayList<String> separate(String s) {
         ArrayList<String> result = new ArrayList<String>();
         while (s.indexOf('+')!= -1) {
             result.add(s.substring(0,s.indexOf('+')+1));
             s = s.substring(s.indexOf('+')+1);
         }
         result.add(s);
         return result;
     }
 
     /**
      * @since 1.05
      * @since 2011.1108
      * @return The time signature of the song text file.
      */
     public static String getStart(String read) {
         return read.substring(0,read.indexOf('*') + 1);
     }
 
     /**
      * @since 1.05
      * @since 2011.1108
      * @return The tempo of the song text file.
      */
     public static String getEnd(String read) {
         return read.substring(read.lastIndexOf(':') + 1);
     }
 
     /**
      * Checks the number of lines in an MPC text file.
      * @param read The text file that we have fed to the function.
      * @return The number of lines in this MPC text file. Ideally,
      * this number is 384.
      */
    public static int checkLines(String read) {
         Matcher m = line.matcher(read);
         int counter = 0;
         while (m.find()) {
             counter++;
         }
         return counter;
     }
 }
