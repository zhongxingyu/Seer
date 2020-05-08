 package fi.helsinki.cs.tmc.author.highlight;
 
 import java.awt.Color;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import org.netbeans.api.editor.settings.EditorStyleConstants;
 import org.netbeans.spi.editor.highlighting.HighlightsContainer;
 import org.netbeans.spi.editor.highlighting.HighlightsSequence;
 import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
 import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
 
 public class TmcAuthorHighlightsContainer extends AbstractHighlightsContainer {
 
     private Document doc;
     private OffsetsBag highlights;
     private AttributeSet solutionCodeAttrs;
     private AttributeSet stubCommentAttrs;
     private AttributeSet stubCodeAttrs;
     private AttributeSet incorrectStubAttrs;
 
     TmcAuthorHighlightsContainer(Document doc) {
         this.doc = doc;
         this.highlights = new OffsetsBag(doc);
         
         this.solutionCodeAttrs = makeSolutionCodeAttrs();
         this.stubCommentAttrs = makeStubCommentAttrs();
         this.stubCodeAttrs = makeStubCodeAttrs();
         this.incorrectStubAttrs = makeIncorrectStubAttrs();
         remakeHighlights();
         
         doc.addDocumentListener(docListener);
     }
 
     private static AttributeSet makeSolutionCodeAttrs() {
         SimpleAttributeSet attrs = new SimpleAttributeSet();
         StyleConstants.setBackground(attrs, Color.ORANGE);
         return attrs;
     }
     
     private static AttributeSet makeStubCommentAttrs() {
         SimpleAttributeSet attrs = new SimpleAttributeSet();
         StyleConstants.setBackground(attrs, new Color(0x7D1DF1));
         return attrs;
     }
     
     private static AttributeSet makeStubCodeAttrs() {
         SimpleAttributeSet attrs = new SimpleAttributeSet();
         StyleConstants.setBackground(attrs, new Color(0xA65DFF));
         StyleConstants.setForeground(attrs, Color.BLACK);
         return attrs;
     }
     
     private static AttributeSet makeIncorrectStubAttrs() {
         SimpleAttributeSet attrs = new SimpleAttributeSet();
         attrs.addAttribute(EditorStyleConstants.WaveUnderlineColor, Color.RED);
         StyleConstants.setBackground(attrs, Color.ORANGE);
         return attrs;
     }
 
     private DocumentListener docListener = new DocumentListener() {
         @Override
         public void insertUpdate(DocumentEvent e) {
             remakeHighlights();
         }
 
         @Override
         public void removeUpdate(DocumentEvent e) {
             remakeHighlights();
         }
 
         @Override
         public void changedUpdate(DocumentEvent e) {
         }
     };
 
     @Override
     public HighlightsSequence getHighlights(int startOffset, int endOffset) {
         return highlights.getHighlights(startOffset, endOffset);
     }
 
     private void remakeHighlights() {
         removeAllHighlights();
         
         String text = documentText();
         makeStubHighlights(text);
         makeSolutionHighlights(text);
     }
 
     private void removeAllHighlights() {
         HighlightsContainer oldHighlights = highlights;
         highlights = new OffsetsBag(doc);
         HighlightsSequence seq = oldHighlights.getHighlights(0, doc.getLength());
         while (seq.moveNext()) {
             fireHighlightsChange(seq.getStartOffset(), seq.getEndOffset());
         }
     }
     
     private static final Pattern stubPattern = Pattern.compile("^[ \t]*//[ \t]*STUB:[ \t]*(.*)$", Pattern.MULTILINE);
     private static final Pattern beginEndSolutionPattern = Pattern.compile("(^[ \t]*//[ \t]*BEGIN[ \t]+SOLUTION[ \t]*$)|(^[ \t]*//[ \t]*END[ \t]+SOLUTION[ \t]*\n)", Pattern.MULTILINE);
    private static final Pattern wholeFilePattern = Pattern.compile("^[ \t]*//[ \t]*SOLUTION[ \t]+FILE[ \t]*$", Pattern.MULTILINE);
     
     private void makeStubHighlights(String text) {
         Matcher matcher = stubPattern.matcher(text);
         while (matcher.find()) {
             int start = matcher.start();
             int contentStart = matcher.start(1);
             int end = matcher.end();
             
             highlights.addHighlight(start, contentStart, stubCommentAttrs);
             highlights.addHighlight(contentStart, end, stubCodeAttrs);
             fireHighlightsChange(start, end);
         }
     }
     
     private void makeSolutionHighlights(String text) {
         Matcher stubMatcher = stubPattern.matcher(text);
         
         if (wholeFilePattern.matcher(text).find()) {
             highlights.addHighlight(0, doc.getLength(), solutionCodeAttrs);
             
             while (stubMatcher.find()) {
                 highlights.addHighlight(stubMatcher.start(), stubMatcher.end(), incorrectStubAttrs);
             }
             
             fireHighlightsChange(0, doc.getLength());
             return;
         }
         
         int start = -1;
         
         Matcher solutionMatcher = beginEndSolutionPattern.matcher(text);
         
         while (solutionMatcher.find()) {
             if (start == -1 && solutionMatcher.group(1) != null) { // "BEGIN SOLUTION"
                 start = solutionMatcher.start();
             } else if (start > -1 && solutionMatcher.group(2) != null) { // "END SOLUTION"
                 int end = solutionMatcher.end();
 
                 highlights.addHighlight(start, end, solutionCodeAttrs);
 
                 if (stubMatcher.find(start) && stubMatcher.end() < end) {
                     highlights.addHighlight(stubMatcher.start(), stubMatcher.end(), incorrectStubAttrs);
                 }
 
                 fireHighlightsChange(start, end);
                 start = -1;
             }
         }
     }
     
     private String documentText() throws RuntimeException {
         try {
             return doc.getText(0, doc.getLength());
         } catch (BadLocationException ex) {
             throw new RuntimeException(ex);
         }
     }
 }
