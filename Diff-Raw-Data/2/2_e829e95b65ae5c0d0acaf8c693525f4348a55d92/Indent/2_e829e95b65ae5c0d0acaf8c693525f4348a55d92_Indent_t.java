 package edu.rit.se.antipattern.htmleditor.models;
 import java.util.regex.*;
 
 /**
  * This class handles indenting for the editor class
  * 
  * @author Zach, Wayne
  */
 public class Indent implements EditorCommand {
     
     private static final char tab = '\t';
     private Buffer b = null;
     
     /**
      * Creates an indent object
      */
     public Indent ( Buffer toIndent ) {
         this.b = toIndent;
     }
     
     public void execute() {
         b.setText( indentText(b.getText(), b.getCursorStartPos(), b.getCursorEndPos()) );
     }
     
     /**
      * Indents the given text over the given range
      * @param text
      * @param startChar
      * @param endChar
      * @return newText
      */
     private String indentText (String text, int startChar, int endChar) {
         String[] newText = text.split("\n", -1);
         //Calculate the line numbers to indent
         int startLine = 0;
         int endLine = 0;
         //Get Start Line
         int i = startChar;
         if (i == text.length())
             i--;
         while (i > 0) {
             if (text.charAt(i) == '\n')
                 startLine++;
             i--;
         }
         //Get End Line
         i = endChar;
         if (i == text.length())
             i--;
         while (i > startChar) {
             if (text.charAt(i) == '\n')
                 endLine++;
             i--;
         }
         endLine += startLine;
         
         //Get the number of tabs from the previous line
         int numTabs = 0;
         if (startLine > 0) {
             numTabs = countTabs(newText[startLine - 1]);
             numTabs += tabDifference(newText[startLine - 1]);
         }
         
         //Add the tabs to the apropriate line
         while (startLine <= endLine) {
             newText[startLine] = newText[startLine].trim();
             if (tabDifference(newText[startLine]) < 0)
                 numTabs += tabDifference(newText[startLine]);
             
             if (numTabs < 0)
                 numTabs = 0;
             
             for (i = 0 ; i < numTabs ; i++){
                 newText[startLine] = tab + newText[startLine];
             }
             
             if (tabDifference(newText[startLine]) > 0)
                 numTabs += tabDifference(newText[startLine]);
             
             startLine++;
         }
         
         //Recompile the text appropriately
         text = "";
         for (i = 0 ; i < newText.length ; i++) {
             text = text + newText[i] + "\n";
         }
         
         return text.trim();
     }
     
     /**
      * Counts the number of tabs in a given line
      * @param line
      * @return tabs
      */
     private static int countTabs (String line) {
         int tabs = 0;
         if (line.length() > 0) {
             while (line.charAt(tabs) == tab && tabs < line.length()-1) {
                 tabs++;
             }
             if (line.charAt(tabs) == tab)
                 tabs++;
         }
         
         return tabs;
     }
     
     /**
      * Calculates the difference in tabs that this line creates
      * @param line
      * @return tabDifference
      */
     private static int tabDifference (String line) {
         int tabDifference = 0;
        Pattern open = Pattern.compile("<[a-zA-Z][a-zA-Z =./:\"]*[^/]>");
         Matcher openTags = open.matcher(line);
         while (openTags.find())
             tabDifference++;
             
         Pattern close = Pattern.compile("</\\w*>");
         Matcher closeTags = close.matcher(line);
         while (closeTags.find())
             tabDifference--;
         
         return tabDifference;
     }
     
     /**
      * Calculates the tabs for a certain line
      * @param text
      * @param cursor
      * @return numTabs
      */
     public static int calulateTabs (String text, int cursor) {
         int numTabs = 0;
         int curLine = 0;
         int i = cursor;
         while (i >= text.length())
             i--;
         while (i > 0) {
             if (text.charAt(i) == '\n')
                 curLine++;
             i--;
         }
         String[] newText = text.split("\n", -1);
         int tabDiff = 0;
         if (curLine > 0) {
             numTabs = countTabs(newText[curLine-1]);
             tabDiff = tabDifference(newText[curLine-1]);
         }
         if (tabDiff < 0) 
             tabDiff = 0;
         numTabs += tabDiff;
         
         if (numTabs < 0)
             numTabs = 0;
         
         return numTabs;
     }
 }
