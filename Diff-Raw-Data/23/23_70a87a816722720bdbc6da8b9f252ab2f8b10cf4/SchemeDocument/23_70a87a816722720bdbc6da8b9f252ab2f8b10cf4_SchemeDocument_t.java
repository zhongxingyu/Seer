 package wombat.gui.text;
 
 import javax.swing.SwingUtilities;
 import javax.swing.text.*;
 
 import wombat.util.Options;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Scheme document.
  * Original source: http://www.squidy-lib.de/
  * (heavily modified)
  */
 public class SchemeDocument extends DefaultStyledDocument {
 	private static final long serialVersionUID = 8684954217591619402L;
 	
 	// For use in formatting.
     DefaultStyledDocument doc;
     Element rootElement;
     boolean multiLineComment;
 
     // Possible attributes.
     static Map<String, MutableAttributeSet> attributes = new HashMap<String, MutableAttributeSet>();;
 
     // For bracket matching (used to determine indentation).
     int currentBracketStart = -1;
     int currentBracketEnd = -1;
     int nextBracketStart = -1;
     int nextBracketEnd = -1;
     
     // Reload all of the settings.
     public static void reload() {
     	attributes.clear();
     	
         for (String key : "default keyword comment string bracket invalid-bracket".split(" "))
         {
             attributes.put(key, new SimpleAttributeSet());
             StyleConstants.setFontSize(attributes.get(key), Options.FontSize);
         }
 
         for (String key : Options.Colors.keySet())
         	if (attributes.containsKey(key) && Options.Colors.containsKey(key))
         		StyleConstants.setForeground(attributes.get(key), Options.Colors.get(key));
     }
     
     /**
      * Create a new Scheme document.
      */
     public SchemeDocument() {
         // Create the basic document.
         doc = this;
         rootElement = doc.getDefaultRootElement();
         putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
     }
 
     /*
     *  Override to apply syntax highlighting after the document has been updated
     */
     public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
         super.insertString(offset, str, a);
         processChangedLines(offset, str.length());
     }
 
     /*
     *  Override to apply syntax highlighting after the document has been updated
     */
     public void remove(int offset, int length) throws BadLocationException {
         super.remove(offset, length);
         processChangedLines(offset, 0);
     }
 
     /*
     *  Determine how many lines have been changed,
     *  then apply highlighting to each line
     */
     public void processChangedLines(int offset, int length) throws BadLocationException {
         String content = doc.getText(0, doc.getLength());
 
         //  The lines affected by the latest document update
         int startLine = rootElement.getElementIndex(offset);
         int endLine = rootElement.getElementIndex(offset + length);
 
         //  Make sure all comment lines prior to the start line are commented
         //  and determine if the start line is still in a multi line comment
         setMultiLineComment(commentLinesBefore(content, startLine));
 
         //  Do the actual highlighting
         for (int i = startLine; i <= endLine; i++)
             applyHighlighting(content, i);
 
         //  Resolve highlighting to the next end multi line delimiter
         if (isMultiLineComment())
             commentLinesAfter(content, endLine);
         else
             highlightLinesAfter(content, endLine);
     }
 
     /*
     *  Highlight lines when a multi line comment is still 'open'
     *  (ie. matching end delimiter has not yet been encountered)
     */
     private boolean commentLinesBefore(String content, int line) {
         int offset = rootElement.getElement(line).getStartOffset();
 
         // If there isn't a start of a comment before the text, we're not in a multiline comment.
         int startDelimiter = lastIndexOf(content, "#|", offset - 2);
         if (startDelimiter < 0)
             return false;
 
         //  Same thing for the end.
         int endDelimiter = indexOf(content, "|#", startDelimiter);
         if (endDelimiter < offset & endDelimiter != -1)
             return false;
 
         //  We're in the middle, so the lines are a comment.
         if (attributes.containsKey("comment"))
         	doc.setCharacterAttributes(startDelimiter, offset - startDelimiter + 1, attributes.get("comment"), true);
         return true;
     }
 
     /*
     *  Highlight comment lines to matching end delimiter
     */
     private void commentLinesAfter(String content, int line) {
         int offset = rootElement.getElement(line).getEndOffset();
 
         //  End of comment not found, nothing to do
 
         int endDelimiter = indexOf(content, "|#", offset);
 
         if (endDelimiter < 0)
             return;
 
         //  Matching start/end of comment found, comment the lines
 
         int startDelimiter = lastIndexOf(content, "#|", endDelimiter);
 
         if (startDelimiter < 0 || startDelimiter <= offset) {
         	if (attributes.containsKey("comment"))
         		doc.setCharacterAttributes(offset, endDelimiter - offset + 1, attributes.get("comment"), true);
         }
     }
 
     /*
     *  Highlight lines to start or end delimiter
     */
     private void highlightLinesAfter(String content, int line)
             throws BadLocationException {
         int offset = rootElement.getElement(line).getEndOffset();
 
         //  Start/End delimiter not found, nothing to do
 
         int startDelimiter = indexOf(content, "#|", offset);
         int endDelimiter = indexOf(content, "|#", offset);
 
         if (startDelimiter < 0)
             startDelimiter = content.length();
 
         if (endDelimiter < 0)
             endDelimiter = content.length();
 
         int delimiter = Math.min(startDelimiter, endDelimiter);
 
         if (delimiter < offset)
             return;
 
         // Start/End delimiter found, reapply highlighting
 
         int endLine = rootElement.getElementIndex(delimiter);
 
         for (int i = line + 1; i < endLine; i++) {
             Element branch = rootElement.getElement(i);
             Element leaf = doc.getCharacterElement(branch.getStartOffset());
             AttributeSet as = leaf.getAttributes();
 
             if (attributes.containsKey("comment"))
             	if (as.isEqual(attributes.get("comment")))
             		applyHighlighting(content, i);
         }
     }
 
     /*
     *  Parse the line to determine the appropriate highlighting
     */
     private void applyHighlighting(String content, int line) throws BadLocationException {
         int startOffset = rootElement.getElement(line).getStartOffset();
         int endOffset = rootElement.getElement(line).getEndOffset() - 1;
 
         int lineLength = endOffset - startOffset;
         int contentLength = content.length();
 
         if (endOffset >= contentLength)
             endOffset = contentLength - 1;
 
         //  check for multi line comments
         //  (always set the comment attribute for the entire line)
 
         if (endingMultiLineComment(content, startOffset, endOffset)
                 || isMultiLineComment()
                 || startingMultiLineComment(content, startOffset, endOffset)) {
         	if (attributes.containsKey("comment"))
         		doc.setCharacterAttributes(startOffset, endOffset - startOffset + 1, attributes.get("comment"), true);
             return;
         }
 
         //  set default attributes for the line
         if (attributes.containsKey("default"))
         	doc.setCharacterAttributes(startOffset, lineLength, attributes.get("default"), true);
 
         //  check for single line comment
 
         int index = content.indexOf(';', startOffset);
 
         if ((index > -1) && (index < endOffset)) {
         	if (attributes.containsKey("comment"))
         		doc.setCharacterAttributes(index, endOffset - index + 1, attributes.get("comment"), true);
             endOffset = index - 1;
         }
 
         //  check for tokens
 
         checkForTokens(content, startOffset, endOffset);
     }
 
     /*
     *  Does this line contain the start delimiter
     */
     private boolean startingMultiLineComment(String content, int startOffset, int endOffset)
             throws BadLocationException {
         int index = indexOf(content, "#|", startOffset);
 
         if ((index < 0) || (index > endOffset))
             return false;
         else {
             setMultiLineComment(true);
             return true;
         }
     }
 
     /*
     *  Does this line contain the end delimiter
     */
     private boolean endingMultiLineComment(String content, int startOffset, int endOffset) throws BadLocationException {
         int index = indexOf(content, "|#", startOffset);
 
         if ((index < 0) || (index > endOffset))
             return false;
         else {
             setMultiLineComment(false);
             return true;
         }
     }
 
     /*
     *  We have found a start delimiter
     *  and are still searching for the end delimiter
     */
     private boolean isMultiLineComment() {
         return multiLineComment;
     }
 
     private void setMultiLineComment(boolean value) {
         multiLineComment = value;
     }
 
     /*
     * Parse the line for tokens to highlight
     */
     private void checkForTokens(String content, int startOffset, int endOffset) {
         while (startOffset <= endOffset) {
             //  skip the delimiters to find the start of a new token
 
             while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
                 if (startOffset < endOffset)
                     startOffset++;
                 else
                     return;
             }
 
             //  Extract and process the entire token
 
             if (content.charAt(startOffset) == '"')
                 startOffset = getQuoteToken(content, startOffset, endOffset);
             else
                 startOffset = getOtherToken(content, startOffset, endOffset);
         }
     }
 
     /*
     *
     */
     private int getQuoteToken(String content, int startOffset, int endOffset) {
         String quoteDelimiter = content.substring(startOffset, startOffset + 1);
         String escapeString = "\\\"";
 
         int index;
         int endOfQuote = startOffset;
 
         //  skip over the escape quotes in this quote
 
         index = content.indexOf(escapeString, endOfQuote + 1);
 
         while ((index > -1) && (index < endOffset)) {
             endOfQuote = index + 1;
             index = content.indexOf(escapeString, endOfQuote);
         }
 
         // now find the matching delimiter
 
         index = content.indexOf(quoteDelimiter, endOfQuote + 1);
 
         if ((index < 0) || (index > endOffset))
             endOfQuote = endOffset;
         else
             endOfQuote = index;
 
         if (attributes.containsKey("string"))
         	doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, attributes.get("string"), true);
 
         return endOfQuote + 1;
     }
 
     /*
     *
     */
     private int getOtherToken(String content, int startOffset, int endOffset) {
         int endOfToken = startOffset + 1;
 
         while (endOfToken <= endOffset) {
             if (isDelimiter(content.substring(endOfToken, endOfToken + 1)))
                 break;
 
             endOfToken++;
         }
 
         String token = content.substring(startOffset, endOfToken);
         
        // When we see a lambda, remember later to fix it for lambda mode.  
         final SchemeDocument me = this;
         if ((Options.LambdaMode && "lambda".equals(token)) || (!Options.LambdaMode && "\u03BB".equals(token))) {
         	SwingUtilities.invokeLater(new Runnable() {
 				@Override public void run() {
 					final String from = Options.LambdaMode ? "lambda" : "\u03BB";
 					final String to = Options.LambdaMode ? "\u03BB" : "lambda";
 					
 					int i = -1;
 					try {
 						while ((i = me.getText(0, me.getLength()).indexOf(from)) != -1) {
 							me.remove(i, from.length());
 							me.insertString(i, to, attributes.get("keyword"));
 						}
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 				}
         	});
         }
 
         if (Options.Keywords.containsKey(token)) {
         	if (attributes.containsKey("keyword"))
         		doc.setCharacterAttributes(startOffset, endOfToken - startOffset, attributes.get("keyword"), true);
         }
 
         return endOfToken + 1;
     }
 
     /*
     *  Assume the needle will the found at the start/end of the line
     */
     private int indexOf(String content, String needle, int offset) {
         int index;
 
         while ((index = content.indexOf(needle, offset)) != -1) {
             String text = getLine(content, index).trim();
 
             if (text.startsWith(needle) || text.endsWith(needle))
                 break;
             else
                 offset = index + 1;
         }
 
         return index;
     }
 
     /*
     *  Assume the needle will the found at the start/end of the line
     */
     private int lastIndexOf(String content, String needle, int offset) {
         int index;
 
         while ((index = content.lastIndexOf(needle, offset)) != -1) {
             String text = getLine(content, index).trim();
 
             if (text.startsWith(needle) || text.endsWith(needle))
                 break;
             else
                 offset = index - 1;
         }
 
         return index;
     }
 
     private String getLine(String content, int offset) {
         int line = rootElement.getElementIndex(offset);
         Element lineElement = rootElement.getElement(line);
         int start = lineElement.getStartOffset();
         int end = lineElement.getEndOffset();
         return content.substring(start, end - 1);
     }
 
     /*
     *  Override for other languages
     */
     protected boolean isDelimiter(String character) {
         return (Character.isWhitespace(character.charAt(0)) || "()[]#|".indexOf(character) != -1);
     }
 }
 
