 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.gui.text;
 
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.undo.*;
 
 import wombat.util.Options;
 import wombat.util.errors.ErrorManager;
 
 import java.awt.*;
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * Text area specialized for Scheme (Woo!)
  */
 public class SchemeTextArea extends JPanel {
 	private static final long serialVersionUID = -5290625425897085428L;
 
 	// File used for saving the document.
 	public File myFile;
 	
 	// View displaying the document.
 	public net.infonode.docking.View myView;
 	
 	// Actual source code.
 	public JTextPane code;
 	
 	// Always use bare newlines, regardless of OS.
     public static String NL = "\n"; //System.getProperty("line.separator");
     
     // Used to determine if a document needs to be saved.
     public int SavedHash;
     
     // Used for undo/redo stack.
     public UndoManager Undo = new UndoManager();
     
     // Match whitespace at the end of a line.
     public static final Pattern WhitespaceEOL = Pattern.compile("[ \\t]+\\n");
     
     // Line number panel (or null if this shouldn't have one).
     LineNumberPanel MyLineNumbers;
     
     /**
      * Create a new Scheme text area.
      */
     public SchemeTextArea(boolean lineNumbers) {
         super();
         setLayout(new BorderLayout());
         
         code = new LinedTextPane(this);
         JScrollPane scroll = new JScrollPane(code);
         add(scroll);
         
         if (lineNumbers) {
         	MyLineNumbers = new LineNumberPanel(this, scroll);
         	add(MyLineNumbers, BorderLayout.WEST);
         }
     }
     
     /**
      * Any cleanup that this object may need to do.
      */
     public void close() {
     }
 
     /**
      * Create a new Scheme text area with content.
      *
      * @param text Content.
      * @throws FileNotFoundException, IOException 
      */
     public SchemeTextArea(File file, boolean lineNumbers) throws FileNotFoundException, IOException {
         this(lineNumbers);
         myFile = file;
         load();
     }
     
     /**
      * Load the document from it's file (throws an exception if the file hasn't been set).
      * @throws FileNotFoundException, IOException If we can't save based on a file error.
      */
     public void load() throws FileNotFoundException, IOException {
     	if (myFile == null) throw new FileNotFoundException("No file set");
     	
     	// Load the document.
     	Scanner scanner = new Scanner(myFile);
         StringBuilder content = new StringBuilder();
 
         while (scanner.hasNextLine()) {
             content.append(scanner.nextLine());
             content.append(NL);
         }
         
         String text = content.toString();
         
         // Change lambda string to character in lambda mode.
         if (Options.LambdaMode) text = text.replace("lambda", "\u03BB");
         
         setText(text);
         SavedHash = text.hashCode();
     }
     
     /**
      * Save the document to its file (throws an exception if the file hasn't been set).
      * @throws FileNotFoundException, IOException If it doesn't work.
      */
     public void save() throws FileNotFoundException, IOException {
     	if (myFile == null) throw new FileNotFoundException("No file set");
     	
     	String text = getText();
     	
     	// Remove extra whitespace at the ends of lines.
     	if (WhitespaceEOL.matcher(text).find()) {
     		text = WhitespaceEOL.matcher(text).replaceAll("\n");
     	}
     	
     	// Remove extra whitespace at the end of the file.
     	if (text.length() > 0 && Character.isWhitespace(text.charAt(text.length() - 1))) {
 	    	text = text.replaceAll("\\s+$", "");
     	}
 
     	// Replace lambda character with lambda string
     	if (text.contains("\u03BB")) {
     		text = text.replace("\u03BB", "lambda");
     	}
     	
     	// If the file already exists (and the option is set), copy the old version to a backup file.
    	// Don't save a backup of a backup.
    	if (myFile.exists() && Options.BackupOnSave && myFile.getCanonicalPath().charAt(myFile.getCanonicalPath().length() - 1) != '~') {
     		File backupFile = new File(myFile.getCanonicalPath() + "~");
     		
     		if(!backupFile.exists()) {
     			backupFile.createNewFile();
     	    }
 
     	    FileChannel source = null;
     	    FileChannel destination = null;
 
     	    try {
     	        source = new FileInputStream(myFile).getChannel();
     	        destination = new FileOutputStream(backupFile).getChannel();
     	        destination.transferFrom(source, 0, source.size());
     	    } finally {
     	        if(source != null) source.close();
     	        if(destination != null) destination.close();
     	    }
     	}
     	
     	// Write to file.
     	Writer out = new OutputStreamWriter(new FileOutputStream(myFile));
         out.write(text);
         out.flush();
         out.close();
         SavedHash = text.hashCode();
     }
     
     /**
      * Is the document dirty?
      * @return If it has changed since the last time, based on hash.
      */
     public boolean isDirty() {
     	return getText().hashCode() != SavedHash;
     }
     
     /**
      * Perform a tab at the current position.
      * @return How far the caret moved.
      */
     public int tab() {
     	// Things that break tokens.
         String delimiters = "()[] ";
 
         // Get the text and current position.
         String text = getText();
         int pos = code.getCaretPosition();
         int len = text.length();
         
         // Fix tabs.
         if (text.indexOf('\t') != -1) {
         	text = text.replace('\t', ' ');
         	setText(text);
         	code.setCaretPosition(pos);
         }
         
         // If we're after the #!eof, don't format.
         if (text.lastIndexOf("#!eof", pos) >= 0) return 0;
         
         // Variables we are trying to determine.
         int indentNow = 0;
         int indentTo = 0;
         int insertAt = 0;
         int tokenStart = 0;
         int tokenEnd = pos;
 
         // Get the start of this line.
         int lineStart = text.lastIndexOf(NL, pos - 1);
         insertAt = (lineStart < 0 ? 0 : lineStart + NL.length());
 
         // Get the indentation on the current line.
         for (int i = Math.max(0, lineStart + NL.length()); i < len && text.charAt(i) == ' '; i++)
             indentNow++;
 
         // If we're on the first line, don't indent.
         if (lineStart == -1)
             indentTo = 0;
 
             // Otherwise, figure out how far we want to indent.
         else {
         	// Don't reallocate.
         	char c, cp;
         	boolean delimCP, delimC;
         	
             // Scan upwards until we find the first unmatched opening bracket.
             boolean unmatched = false;
             int index;
             Stack<Character> brackets = new Stack<Character>();
             for (int i = lineStart; i >= 0; i--) {
                 c = text.charAt(i);
                 
                 index = text.lastIndexOf(';', i);
                 if (index >= 0 && text.lastIndexOf('\n', i) < index) {
                 	i = index;
                 	continue;
                 }
                 
                 index = text.lastIndexOf("|#", i);
                 if (index >= 0 && text.lastIndexOf('\n', i) < index) {
                 	i = text.lastIndexOf("#|", index);
                 	continue;
                 }
 
 		// Ignore character literals
 		if (i >= 2 && text.charAt(i - 1) == '\\' && text.charAt(i - 2) == '#')
 		    continue;
                 
                 if (c == ')') brackets.push('(');
                 if (c == ']') brackets.push('[');
 
                 if (c == '(' || c == '[') {
                     if (brackets.isEmpty() || brackets.peek() != c) {
                         int thatLine = text.lastIndexOf(NL, i);
                         if (thatLine < 0)
                             thatLine = 0;
                         else
                             thatLine = thatLine + NL.length();
 
                         indentTo = i - thatLine;
                         unmatched = true;
                         break;
                     } else {
                         brackets.pop();
                     }
                 }
                 
                 if (i > 0) {
                     cp = text.charAt(i - 1);
 
                     delimCP = (delimiters.indexOf(cp) != -1);
                     delimC = (delimiters.indexOf(c) != -1);
                     
                     if (delimCP && !delimC) tokenStart = i;
                     if (!delimCP && delimC) tokenEnd = i; 
                     if (delimCP && delimC) tokenStart = tokenEnd = i;
                 }
             }
             
             // Get the token.
             String token = null;
             try {
                 token = text.substring(tokenStart, tokenEnd).trim();
             } catch (StringIndexOutOfBoundsException sioobe) {
             }
             
             // If there aren't any unmatched brackets, start a line.
             if (!unmatched)
                 indentTo = 0;
 
             // If there isn't a string, don't add anything.
             else if (token == null || token.isEmpty())
             	indentTo += 1;
             
             // Otherwise, if there's a valid keyword, indent based on that.
             else if (Options.Keywords.containsKey(token))
                 indentTo += Options.Keywords.get(token);
 
             // Otherwise, fall back on the default indentation.
             else
                 indentTo += 2;
         }
         
         // Add new indentation if we need to.
         if (indentNow < indentTo) {
             String toInsert = "";
             for (int i = indentNow; i < indentTo; i++)
                 toInsert += " ";
 
             try {
 				code.getDocument().insertString(insertAt, toInsert, null);
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
             
             return (indentTo - indentNow);
         }
 
         // Or remove it, if we need to.
         else if (indentNow > indentTo) {
         	try {
 				code.getDocument().remove(insertAt, indentNow - indentTo);
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
         	
         	return -1 * (indentTo - indentNow);
         }
         
         // Shouldn't get this far.
         return 0;
     }
 
     /**
      * Format the document by fixing indentation to scheme standards.
      */
     public void format() {
     	// TODO: Push this into it's own thread with a progress indicator.
     	
     	code.setCaretPosition(0);
         tab();
     	
         int next = -1;
         int eof = getText().indexOf("#!eof");
         if (eof == -1) eof = getText().length();
         
         while (true) {
         	next = getText().indexOf(NL, next + 1) + NL.length();
         	
             if (next > 0 && next < eof) {
                 try {
                     code.setCaretPosition(next);
                     tab();
                 } catch (IllegalArgumentException iae) {
                     // Means there's extra lines. Just ignore it.
                 }
             } else {
                 break;
             }
         }
     }
 
     /**
      * Is the text area empty?
      * @return True/false
      */
     public boolean isEmpty() {
         return getText().length() == 0;
     }
 
     /**
      * Set the file that this code is associated with.
      * @param f The file.
      */
     public void setFile(File f) {
         myFile = f;
     }
 
     /**
      * Get the file that this code is associated with (might be null).
      * @return The file.
      */
     public File getFile() {
         return myFile;
     }
 
     /**
      * Get the code.
      * @return The code.
      */
     public String getText() {
         return code.getText();
     }
 
     /**
      * Set the code.
      */
     public void setText(String text) {
         code.setText(text);
     }
 
     /**
      * Append text to the end of the code area.
      * @param text Text to append.
      */
     public synchronized void append(String text) {
     	try {
 			code.getDocument().insertString(code.getDocument().getLength(), text, null);
 		} catch (BadLocationException e) {
 			ErrorManager.logError(e.getMessage());
 		}
     }
     
     /**
      * Jump to the end of the text area.
      */
     public void goToEnd() {
     	code.scrollRectToVisible(new Rectangle(0, code.getHeight() - 1, 1, 1));
     }
 
     /**
      * Update the font.
      */
 	public void refresh() {
 		// Force the document to refresh.
 		try {
 			// Process the entire document.
 			
 			// Clear the original styles.
 			// This is necessary to not have a last line with the incorrect height.
 			((SchemeDocument) code.getDocument()).setCharacterAttributes(0, getText().length(), SchemeDocument.attributes.get("default"), true);
 			Enumeration<?> e = ((SchemeDocument) code.getDocument()).getStyleNames();
 			while (e.hasMoreElements()) 
 				((SchemeDocument) code.getDocument()).removeStyle((String) e.nextElement());
 			
 			// Reformat the code.
 			((SchemeDocument) code.getDocument()).processChangedLines(0, getText().length());
 			
 			// Check to show/hide the line numbers.
 			if (MyLineNumbers != null)
 				MyLineNumbers.setVisible(Options.ViewLineNumbers);
 		} catch (BadLocationException e) {
 		}
 	}
 }
 
