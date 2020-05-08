 package frontend;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class Source {
     public static final char EOL = '\n';      // End-of-Line character
     public static final char EOF = (char) 0;  // End-of-File character
 
     private BufferedReader reader;
     private String line;
     private int lineNum;
     private int lineIndex;
 
     public Source(String sourceFile) {
         try {
             reader = new BufferedReader(new FileReader(sourceFile));
             line = reader.readLine();
            lineNum = 0;
             lineIndex = 0;
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public char currentChar() throws IOException {
         // Need to read the next line?
         if (lineIndex > line.length()) {
             if (readLine()) {
                 return nextChar();
             }
             else {
                 return EOF;
             }
         }
 
         // Return the character at the current position.
         else {
             return line.charAt(lineIndex);
         }
     }
 
     public char nextChar() throws IOException {
         lineIndex++;
         return currentChar();
     }
 
     // Look at next character without consuming the current one
     public char peekChar() throws IOException {
         currentChar();
         if (line == null) {
             return EOF;
         }
 
         int nextPos = lineIndex + 1;
         return nextPos < line.length() ? line.charAt(nextPos) : EOL;
     }
 
     public boolean readLine() throws IOException {
        line = reader.readLine();  // Null when at the end of the source
         lineIndex = 0;
 
         if (line != null) {
             lineNum++;
             System.out.println(lineNum + " " + line);
             return true;
         }
 
         return false;
     }
 
     public int getLineNum() {
         return lineNum;
     }
 
     public int getPosition() {
         return lineIndex;
     }
 
     public void close() throws IOException {
         if (reader != null) {
             try {
                 reader.close();
             }
             catch (IOException e) {
                 e.printStackTrace();
                 throw e;
             }
         }
     }
 }
