 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package praat;
 
 import java.awt.Color;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 
 /**
  *
  * @author David Lukes
  */
 public class format {
     // GLOBAL VARIABLES:
 
     // I thought I needed to take care of the different line separators across
     // OSs myself, but I probably don't (because the following commented out code
     // actually ADDS bugs on Windows instead of ensuring that the program runs
     // correctly); thank you Java, good to know.
 //    private static final String lineSep = System.getProperty("line.separator");
     private static final String lineSep = "\n";
     // stuff for the parser:
     private static final String praatProcCall = "@\\w+";
     private static final String praatKeywords = "(for|endfor|if|elsif|else|endif"
             + "|from|to|form|endform|proc|endproc|editor|endeditor|and|or|not"
             + "|while|endwhile|repeat|until)";
     private static final String praatBuiltins = "(clearinfo|printline|print"
             + "|e|pi|undefined|select|all|plus|minus|selected|selected\\$|do"
             + "|numberOfSelected|index|exit|execute|writeInfoLine|appendInfo"
             + "|appendInfoLine|fileReadable|filedelete|fileappend|system"
             + "|environment\\$|stopwatch|pause|beginPause|endPause|chooseReadFile\\$"
             + "|sendsocket|assert|nowarn|nocheck)";
 //    private static final String praatVarContent = "'\\S+?'";
     private static final String praatTokenDelimiters = "(\\s|\\(|\\)|\\+|-"
             + "|/|\\*|\\^|&|%|=|>|<|:|,|\\.|\\?|\\||\\[|\\]|\\{|\\}|\"|')";
     private static final String praatZeroWidthDelimiters = "((?<="
             + praatTokenDelimiters + ")|(?=" + praatTokenDelimiters + "))";
     private static Boolean ongoingString;
     private static Boolean ongoingVarContent;
     // syntax highlighting via a StyledDocument:
     private static final StyleContext cont = StyleContext.getDefaultStyleContext();
     private static final AttributeSet attrKeyword = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Foreground, colors.PRAATKEY);
     private static final AttributeSet attrBold = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Bold, true);
     private static final AttributeSet attrNonBold = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Bold, false);
     private static final AttributeSet attrString = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Foreground, colors.PRAATSTRING);
     private static final AttributeSet attrComment = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Foreground, colors.PRAATCOMMENT);
     private static final AttributeSet attrDefault = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Foreground, Color.LIGHT_GRAY);
     private static final AttributeSet attrBuiltIn = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Foreground, colors.PRAATBUILTIN);
     private static final AttributeSet attrVarContent = cont.addAttribute(
             cont.getEmptySet(), StyleConstants.Foreground, colors.PRAATVARCONTENT);
 
     public static String highlightCurrentLine(DefaultStyledDocument doc, String str, int offset) throws BadLocationException {
         /*
          * Find what the current line (the line just modified) is based on the 
          * offset of the string str inserted, parse it and apply syntax highlighting.
          * When a newline is inserted, "current line" means the one ending in the
          * newline just inserted.
          */
         
         // reset the ongoingString and ongoingVarContent flags:
         ongoingString = false;
         ongoingVarContent = false;
         
         String text = doc.getText(0, doc.getLength());
 
         // the offset relative to which the current line is determined:
         int anchor = str.equals(lineSep) ? offset - 1 : offset;
         // determine start and end of line:
         int endOfLine = text.indexOf(lineSep, anchor);
         endOfLine = (endOfLine == -1) ? text.length() : endOfLine;
         int startOfLine = text.lastIndexOf(lineSep, anchor);
         startOfLine = (startOfLine == -1) ? 0 : startOfLine;
         // don't use .trim() on the line, otherwise the highlighting will come out
         // shifted w.r.t. to the real position of the text:
         String line = text.substring(startOfLine, endOfLine);
         String[] parsedLine = line.split(praatZeroWidthDelimiters);
//        System.out.println(Arrays.toString(parsedLine) + parsedLine.length); //DEBUG
 
         // set up initial line offset:
         int lineOffset = 0;
         for (int i = 0; i < parsedLine.length; i++) {
             if (parsedLine[i].startsWith(";") || parsedLine[i].startsWith("#")) {
                 doc.setCharacterAttributes(startOfLine + lineOffset, endOfLine - startOfLine - lineOffset, attrComment, true);
                 break;
             }
             lineOffset = highlightToken(doc, parsedLine[i], startOfLine, lineOffset);
         }
 
         return line;
     }
 
     private static int highlightToken(DefaultStyledDocument doc, String token, int startOfLine, int lineOffset) {
         /*
          * Highlight a single token on a line of text and return updated line offset.
          */
         int tokenLength = token.length();
         int tokenL = startOfLine + lineOffset;
         if (ongoingString || token.equals("\"")) {
             doc.setCharacterAttributes(tokenL, tokenLength, attrString, true);
             if (token.equals("\"")) {
                 ongoingString = !ongoingString;
             }
         } else if (ongoingVarContent || token.equals("'")) {
             doc.setCharacterAttributes(tokenL, tokenLength, attrVarContent, true);
             if (token.equals("'")) {
                 ongoingVarContent = !ongoingVarContent;
             }
         } else if (token.matches(praatKeywords)) {
             doc.setCharacterAttributes(tokenL, tokenLength, attrKeyword, true);
             doc.setCharacterAttributes(tokenL, tokenLength, attrBold, false);
         } else if (token.matches(praatBuiltins)) {
             doc.setCharacterAttributes(tokenL, tokenLength, attrBuiltIn, true);
         } else if (token.matches(praatProcCall)) {
             doc.setCharacterAttributes(tokenL, tokenLength, attrBold, true);
         } else {
             doc.setCharacterAttributes(tokenL, tokenLength, attrDefault, true);
 //            doc.setCharacterAttributes(tokenL, tokenLength, attrNonBold, false);
         }
         return lineOffset + tokenLength;
     }
 }
