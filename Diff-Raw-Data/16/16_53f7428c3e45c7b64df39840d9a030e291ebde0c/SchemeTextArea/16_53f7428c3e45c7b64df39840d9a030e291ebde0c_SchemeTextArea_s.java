 package gui;
 
 import javax.swing.*;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.*;
 import javax.swing.undo.UndoManager;
 
 import wombat.Options;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * Text area specialized for Scheme (Woo!)
  */
 public class SchemeTextArea extends JPanel {
 	private static final long serialVersionUID = -5290625425897085428L;
 
 	public File myFile;
 	public net.infonode.docking.View myView;
 	public JTextPane code;
     public static String NL = "\n"; //System.getProperty("line.separator");
     public int SavedHash;
     public UndoManager Undo = new UndoManager();
     
     public static final Pattern WhitespaceEOL = Pattern.compile("[ \\t]+\\n");
     
     /**
      * Create a new Scheme text area.
      */
     public SchemeTextArea() {
         super();
         setLayout(new BorderLayout());
 
         code = new JTextPane() {
 			private static final long serialVersionUID = 2523699493531510651L;
 
 			@Override
         	public void paint(Graphics go) {
             	super.paint(go);
             	
             	Graphics2D g = (Graphics2D) go;
             	
             	
             	int width = 2 + 80 * g.getFontMetrics(new Font("Monospaced", Font.PLAIN, Options.FontSize)).charWidth(' '); 
             	
             	g.setColor(Color.LIGHT_GRAY);
             	g.drawLine(width, 0, width, getHeight() + 10);
         	}
         };
         final JScrollPane cs = new JScrollPane(code);
         add(cs);
         
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < 100; i++)
         	sb.append(i + "\n");
         
         final SchemeDocument doc = new SchemeDocument();
         final StyledEditorKit sek = new StyledEditorKit() {
 			private static final long serialVersionUID = 8558935103754214456L;
 
 			public Document createDefaultDocument() {
                 return doc;
             }
         };
 
         code.setFont(new Font("Monospaced", Font.PLAIN, Options.FontSize));
         code.setEditorKitForContentType("text/scheme", sek);
         code.setContentType("text/scheme");
         code.setEditorKit(sek);
         code.setDocument(doc);
 
         code.getInputMap().put(KeyStroke.getKeyStroke("TAB"), new actions.Tab());
         code.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), new actions.Return());
         
         for (String name : new String[]{
         		"New", "Open", "Save", "Save as", "Close", "Exit", 
         		"Cut", "Copy", "Paste", "Undo", "Redo", 
         		"Run", "Format"}) {
         	JMenuItem item = MenuManager.itemForName(name);
         	code.getInputMap().put(item.getAccelerator(), item.getAction());
         }
         
         // Bracket highlighting.
         code.addCaretListener(new BracketMatcher(this));
         
         // Undo/redo
         
 
         // Listen for undo and redo events
         doc.addUndoableEditListener(new UndoableEditListener() {
             public void undoableEditHappened(UndoableEditEvent evt) {
                 Undo.addEdit(evt.getEdit());
             }
         });
     }
 
     /**
      * Create a new Scheme text area with content.
      *
      * @param text Content.
      * @throws FileNotFoundException, IOException 
      */
     public SchemeTextArea(File file) throws FileNotFoundException, IOException {
         this();
         myFile = file;
         load();
     }
     
     /**
      * Load the document from it's file (throws an exception if the file hasn't been set).
      * @throws FileNotFoundException, IOException If we can't save based on a file error.
      */
     public void load() throws FileNotFoundException, IOException {
     	if (myFile == null) throw new FileNotFoundException("No file set");
     	
     	Scanner scanner = new Scanner(myFile);
         StringBuilder content = new StringBuilder();
         String NL = "\n"; //System.getProperty("line.separator");
 
         while (scanner.hasNextLine()) {
             content.append(scanner.nextLine());
             content.append(NL);
         }
         
         setText(content.toString());
         SavedHash = getText().hashCode();
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
     		int pos = code.getCaretPosition();
     		setText(WhitespaceEOL.matcher(text).replaceAll("\n"));
     		if (pos > text.length())
 	    		code.setCaretPosition(text.length());
 	    	else
 	    		code.setCaretPosition(pos);
     	}
     	
     	// Remove extra whitespace at the end of the file.
     	if (Character.isWhitespace(text.charAt(text.length() - 1))) {
 	    	int pos = code.getCaretPosition();
 	    	text = getText().replaceAll("\\s+$", "");
 	    	setText(text);
 	    	if (pos > text.length())
 	    		code.setCaretPosition(text.length());
 	    	else
 	    		code.setCaretPosition(pos);
     	}
     	
     	// Remove extra whitespace from the end of lines.
     	
     	
     	Writer out = new OutputStreamWriter(new FileOutputStream(myFile));
         out.write(getText());
         out.flush();
         out.close();
         
         SavedHash = getText().hashCode();
     }
     
     /**
      * Is the document dirty?
      * @return If it has changed since the last time.
      */
     public boolean isDirty() {
     	return getText().hashCode() != SavedHash;
     }
     
     /**
      * Perform a tab at the current position.
      */
     public void tab() {
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
         if (text.lastIndexOf("#!eof", pos) >= 0) return;
         
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
 
             setText(text.substring(0, insertAt) + toInsert + text.substring(insertAt));
             code.setCaretPosition(pos + (indentTo - indentNow));
         }
 
         // Or remove it, if we need to.
         else if (indentNow > indentTo) {
             setText(text.substring(0, insertAt) + text.substring(insertAt + (indentNow - indentTo)));
            code.setCaretPosition(pos - (indentNow - indentTo));
         }
     }
 
     /**
      * Format the document.
      */
     public void format() {
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
            } else
                 break;
         }
     }
 
     /**
      * Is the text area empty?
      *
      * @return True/false
      */
     public boolean isEmpty() {
         return getText().length() == 0;
     }
 
     /**
      * Set the file that this code is associated with.
      *
      * @param f The file.
      */
     public void setFile(File f) {
         myFile = f;
     }
 
     /**
      * Get the file that this code is associated with (might be null).
      *
      * @return The file.
      */
     public File getFile() {
         return myFile;
     }
 
     /**
      * Get the code.
      *
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
      *
      * @param text Text to append.
      */
     public void append(String text) {
         setText(getText() + text);
     }
 
     /**
      * Update the font.
      */
 	public void updateFont() {
 		// For the document to refresh.
 		try {
 			((SchemeDocument) code.getDocument()).processChangedLines(0, getText().length());
 		} catch (BadLocationException e) {
 		}
 	}
 }
