 package org.antlr.works.ate;
 
 import edu.usfca.xj.appkit.frame.XJFrame;
 import edu.usfca.xj.appkit.undo.XJUndo;
 import edu.usfca.xj.appkit.utils.XJSmoothScrolling;
 import org.antlr.works.ate.analysis.ATEAnalysisColumn;
 import org.antlr.works.ate.analysis.ATEAnalysisManager;
 import org.antlr.works.ate.breakpoint.ATEBreakpointManager;
 import org.antlr.works.ate.folding.ATEFoldingEntityProxy;
 import org.antlr.works.ate.folding.ATEFoldingManager;
 import org.antlr.works.ate.swing.ATEAutoIndentation;
 import org.antlr.works.ate.swing.ATEImmediateColoring;
 import org.antlr.works.ate.swing.ATEKeyBindings;
 import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
 import org.antlr.works.ate.syntax.misc.ATEColoring;
 import org.antlr.works.ate.syntax.misc.ATELine;
 import org.antlr.works.ate.syntax.misc.ATEToken;
 
 import javax.swing.*;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultEditorKit;
 import javax.swing.text.Element;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.util.List;
 /*
 
 [The "BSD licence"]
 Copyright (c) 2005 Jean Bovet
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
 derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */
 
 public class ATEPanel extends JPanel implements XJSmoothScrolling.ScrollingDelegate {
 
     protected XJFrame parentFrame;
     protected XJSmoothScrolling smoothScrolling;
 
     protected ATEPanelDelegate delegate;
     protected ATETextPane textPane;
     protected ATEKeyBindings keyBindings;
     protected ATEGutter gutter;
     protected ATEAnalysisColumn analysisColumn;
 
     protected ATEBreakpointManager breakpointManager;
     protected ATEFoldingManager foldingManager;
     protected ATEUnderlyingManager underlyingManager;
     protected ATEAnalysisManager analysisManager;
 
     protected ATEColoring colorize;
     protected ATESyntaxEngine engine;
     protected ATEAutoIndentation autoIndent;
     protected ATEImmediateColoring immediateSyntaxColoring;
 
     protected TextPaneListener textPaneListener;
 
     protected boolean isTyping = false;
     protected int caretPosition;
 
     protected static final String unixEndOfLine = "\n";
     protected static int ANALYSIS_COLUMN_WIDTH = 18;
 
     public ATEPanel(XJFrame parentFrame) {
         super(new BorderLayout());
         this.parentFrame = parentFrame;
         colorize = new ATEColoring(this);
         autoIndent = new ATEAutoIndentation(this);
         immediateSyntaxColoring = new ATEImmediateColoring(this);
         createTextPane();
     }
 
     public JFrame getParentFrame() {
         return parentFrame.getJFrame();
     }
 
     public void setParserEngine(ATESyntaxEngine engine) {
         this.engine = engine;
         this.engine.setTextEditor(this);
         this.colorize.setSyntaxEngine(engine);
     }
 
     public ATESyntaxEngine getParserEngine() {
         return engine;
     }
 
     public void setDelegate(ATEPanelDelegate delegate) {
         this.delegate = delegate;
     }
 
     public void setBreakpointManager(ATEBreakpointManager manager) {
         this.breakpointManager = manager;
     }
 
     public void setFoldingManager(ATEFoldingManager manager) {
         this.foldingManager = manager;
     }
 
     public void setUnderlyingManager(ATEUnderlyingManager manager) {
         this.underlyingManager = manager;
     }
 
     public void setAnalysisManager(ATEAnalysisManager manager) {
         this.analysisManager = manager;
     }
 
     public ATEAnalysisManager getAnalysisManager() {
         return analysisManager;
     }
 
     public void setAutoIndent(boolean flag) {
         autoIndent.setEnabled(flag);
     }
 
     public boolean autoIndent() {
         return autoIndent.enabled();
     }
 
     public void setIsTyping(boolean flag) {
         isTyping = flag;
     }
 
     public boolean isTyping() {
         return isTyping;
     }
 
     public void setCaretPosition(int position) {
         setCaretPosition(position, true, false);
     }
 
     public void setCaretPosition(int position, boolean adjustScroll, boolean animate) {
         if(adjustScroll)
             scrollCenterToPosition(position, animate);
         if(!animate)
             textPane.setCaretPosition(position);
     }
 
     public int getCaretPosition() {
         return textPane.getCaretPosition();
     }
 
     public void setHighlightCursorLine(boolean flag) {
         textPane.setHighlightCursorLine(flag);
     }
 
     public void setUnderlying(boolean flag) {
         underlyingManager.setUnderlying(flag);
     }
 
     public boolean isUnderlying() {
         return underlyingManager.underlying;
     }
 
     public void setFoldingEnabled(boolean flag) {
         gutter.setFoldingEnabled(flag);
     }
 
     public void setEnableRecordChange(boolean flag) {
         if(flag)
             textPaneListener.enable();
         else
             textPaneListener.disable();
     }
 
     public void scrollCenterToPosition(int position, boolean animate) {
         try {
             Rectangle r = textPane.modelToView(position);
             if (r != null) {
                 Rectangle vis = getVisibleRect();
                 r.y -= (vis.height / 2);
                 r.height = vis.height;
                 if(animate) {
                     // Will move the caret after the scrolling
                     // has completed (see smoothScrollingDidComplete()) 
                     caretPosition = position;
                     smoothScrolling.scrollTo(r);
                 } else
                     textPane.scrollRectToVisible(r);
             }
         } catch (BadLocationException ble) {
             // ignore
         }
     }
 
     public void smoothScrollingDidComplete() {
         textPane.setCaretPosition(caretPosition);
     }
 
     public void setAnalysisColumnVisible(boolean visible) {
         analysisColumn.setVisible(visible);
         if(visible)
             analysisColumn.setPreferredSize(new Dimension(ANALYSIS_COLUMN_WIDTH, 0));
         else
             analysisColumn.setPreferredSize(new Dimension(0, 0));
     }
 
     public boolean isAnalysisColumnVisible() {
         return analysisColumn.isVisible();
     }
 
     public void toggleAnalysis() {
         setAnalysisColumnVisible(!isAnalysisColumnVisible());
     }
 
     public void resetColoring() {
         colorize.reset();
     }
 
     public void setSyntaxColoring(boolean flag) {
         colorize.setEnable(flag);
     }
 
     public boolean isSyntaxColoring() {
         return colorize.isEnable();
     }
 
     public ATEKeyBindings getKeyBindings() {
         return keyBindings;
     }
 
     public void toggleSyntaxColoring() {
         setSyntaxColoring(!isSyntaxColoring());
         if(colorize.isEnable()) {
             colorize.reset();
             colorize.colorize();
         } else
             colorize.removeColorization();
     }
 
     public void refresh() {
         if(underlyingManager != null)
             underlyingManager.reset();
         
         if(gutter != null)
             gutter.markDirty();
 
         repaint();
     }
 
     public void changeOccurred() {
         // Method called only when a change occurred in the document
         // which needs an immediate effect (in this case, the gutter
         // has to be repainted)
         gutter.markDirty();
         parse();
     }
 
     public int getSelectionStart() {
         return textPane.getSelectionStart();
     }
 
     public int getSelectionEnd() {
         return textPane.getSelectionEnd();
     }
 
     public List getTokens() {
         return engine.getTokens();
     }
 
     public List getLines() {
         return engine.getLines();
     }
 
     public int getCurrentLinePosition() {
         return getLinePositionAtIndex(getCaretPosition());
     }
 
     public int getLinePositionAtIndex(int index) {
         return getLineIndexAtTextPosition(index) + 1;
     }
 
     public int getCurrentColumnPosition() {
         return getColumnPositionAtIndex(getCaretPosition());
     }
 
     public int getColumnPositionAtIndex(int index) {
         int lineIndex = getLineIndexAtTextPosition(index);
         Point linePosition = getLineTextPositionsAtLineIndex(lineIndex);
         if(linePosition == null)
             return 1;
         else
             return getCaretPosition() - linePosition.x + 1;
     }
 
     public int getLineIndexAtTextPosition(int pos) {
         List lines = getLines();
         if(lines == null)
             return -1;
 
         for(int i=0; i<lines.size(); i++) {
             ATELine line = (ATELine)lines.get(i);
             if(line.position > pos) {
                 return i-1;
             }
         }
         return lines.size()-1;
     }
 
     public Point getLineTextPositionsAtTextPosition(int pos) {
         return getLineTextPositionsAtLineIndex(getLineIndexAtTextPosition(pos));
     }
 
     public Point getLineTextPositionsAtLineIndex(int lineIndex) {
         List lines = getLines();
         if(lineIndex == -1 || lines == null)
             return null;
 
         ATELine startLine = (ATELine)lines.get(lineIndex);
         int start = startLine.position;
         if(lineIndex+1 >= lines.size()) {
             return new Point(start, getTextPane().getDocument().getLength()-1);
         } else {
             ATELine endLine = (ATELine)lines.get(lineIndex+1);
             int end = endLine.position;
             return new Point(start, end-1);
         }
     }
 
     /** This method is used when loading the text (mostly for the first time):
      * it loads the text and parse it in the current thread in order to speed-up
      * the display time (see ATEColorizing.processColorize(true)).
      */
 
     public void loadText(String text) {
         setEnableRecordChange(false);
         try {
             ateEngineWillParse();
 
             textPane.setText(text);
             engine.processSyntax();
             colorize.processColorize(true);
 
             textPane.setCaretPosition(0);
             textPane.moveCaretPosition(0);
             textPane.getCaret().setSelectionVisible(true);
 
             ateEngineDidParse();
         } catch(Exception e) {
             e.printStackTrace();
         } finally {
             setEnableRecordChange(true);
         }
     }
 
     public void insertText(int index, String text) {
         try {
             textPane.getDocument().insertString(index, text, null);
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
     }
 
     public void replaceSelectedText(String replace) {
         replaceText(getSelectionStart(), getSelectionEnd(), replace);
     }
 
     public void replaceText(int start, int end, String text) {
         try {
             textPane.getDocument().remove(start, end-start);
             textPane.getDocument().insertString(start, text, null);
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
     }
 
     public void selectTextRange(int start, int end) {
         textPane.setCaretPosition(start);
         textPane.moveCaretPosition(end);
         textPane.getCaret().setSelectionVisible(true);
 
         scrollCenterToPosition(start, false);
     }
 
     public void textPaneDidPaint(Graphics g) {
         if(underlyingManager != null)
             underlyingManager.paint(g);
     }
 
     public void textPaneInvokePopUp(Component component, int x, int y) {
         if(delegate != null)
             delegate.ateInvokePopUp(component, x, y);
     }
 
     protected void createTextPane() {
         textPane = new ATETextPane(this);
         textPane.setBackground(Color.white);
         textPane.setBorder(null);
 
         textPane.setWordWrap(false);
 
         textPane.getDocument().addDocumentListener(textPaneListener = new TextPaneListener());
         // Set by default the end of line property in order to always use the Unix style
         textPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, unixEndOfLine);
 
         textPane.addCaretListener(new TextPaneCaretListener());
         textPane.addMouseListener(new TextPaneMouseAdapter());
         textPane.addMouseMotionListener(new TextPaneMouseMotionAdapter());
 
         smoothScrolling = new XJSmoothScrolling(textPane, this);
         
         // Gutter
         gutter = new ATEGutter(this);
 
         // Key bindings
         keyBindings = new ATEKeyBindings(getTextPane());
 
 
         // Scroll pane
         JScrollPane textScrollPane = new JScrollPane(textPane);
         textScrollPane.setWheelScrollingEnabled(true);
         textScrollPane.setRowHeaderView(gutter);
 
         // Analysis column
         analysisColumn = new ATEAnalysisColumn(this);
         analysisColumn.setMinimumSize(new Dimension(ANALYSIS_COLUMN_WIDTH, 0));
         analysisColumn.setMaximumSize(new Dimension(ANALYSIS_COLUMN_WIDTH, Integer.MAX_VALUE));
         analysisColumn.setPreferredSize(new Dimension(ANALYSIS_COLUMN_WIDTH, analysisColumn.getPreferredSize().height));
 
         Box box = Box.createHorizontalBox();
         box.add(textScrollPane);
         box.add(analysisColumn);
 
         add(box, BorderLayout.CENTER);
     }
 
     public ATETextPane getTextPane() {
         return textPane;
     }
 
     public ATEGutter getGutter() {
         return gutter;
     }
 
     public void parse() {
         engine.parse();
     }
 
     public void ateEngineWillParse() {
         if(delegate != null)
             delegate.ateParserWillParse();
     }
 
     public void ateEngineDidParse() {
         colorize.colorize();
         if(delegate != null)
             delegate.ateParserDidParse();
     }
 
     public void ateAutoIndent(int offset, int length) {
         if(delegate != null)
             delegate.ateAutoIndent(offset, length);
     }
 
     public void ateColoringWillColorize() {
         setEnableRecordChange(false);
         disableUndo();
     }
 
     public void ateColoringDidColorize() {
         setEnableRecordChange(true);
         enableUndo();
     }
 
     public XJUndo getTextPaneUndo() {
         return parentFrame.getUndo(getTextPane());
     }
 
     public void disableUndo() {
         XJUndo undo = getTextPaneUndo();
         if(undo != null)
             undo.disableUndo();
     }
 
     public void enableUndo() {
         XJUndo undo = getTextPaneUndo();
         if(undo != null)
             undo.enableUndo();
     }
 
     protected class TextPaneCaretListener implements CaretListener {
 
         public void caretUpdate(CaretEvent e) {
             if(delegate != null)
                 delegate.ateCaretUpdate(e.getDot());
 
             // Each time the cursor moves, update the visible part of the text pane
             // to redraw the highlighting
             if(textPane.highlightCursorLine)
                 textPane.repaint();
         }
     }
 
     protected class TextPaneListener implements DocumentListener {
 
         protected int enable = 0;
 
         public synchronized void enable() {
             enable--;
         }
 
         public synchronized void disable() {
             enable++;
         }
 
         public synchronized boolean isEnable() {
             return enable == 0;
         }
 
         /** This method shifts every offset past the location in order
          *  for collapsed view to be correctly rendered (the rule has to be
          *  immediately at the correct position and cannot wait for the
          *  parser to finish)
          *  We are also accessing the Token start and end field directly in
          *  order to avoid the overhead of method calling.
          */
 
         protected void adjustTokens(int location, int length) {
             if(location == -1)
                 return;
 
             List tokens = engine.getTokens();
             if(tokens == null)
                 return;
 
             int max = tokens.size();
             for(int t=0; t<max; t++) {
                 ATEToken token = (ATEToken) tokens.get(t);
 
                 /** Mark as modified the token at the current modification location. See comments
                  * in ATEColoring about this modified field.
                  */
 
                 if(location >= token.start && location <= token.end)
                     token.modified = true;
 
                 if(token.start > location) {
                     token.start += length;
                     token.end += length;
                 }
             }
         }
 
         public void changeUpdate(int offset, int length, boolean insert) {
             if(isEnable()) {
                 if(delegate != null)
                     delegate.ateChangeUpdate(offset, length, insert);
 
                 if(insert) {
                     autoIndent.indent(offset, length);
                     immediateSyntaxColoring.colorize(offset, length);
                 }
 
                 adjustTokens(offset, length);
                 colorize.setColorizeLocation(offset, length);
                changeOccurred();
             }
         }
 
         public void insertUpdate(DocumentEvent e) {
             setIsTyping(true);
             changeUpdate(e.getOffset(), e.getLength(), true);
         }
 
         public void removeUpdate(DocumentEvent e) {
             setIsTyping(true);
             changeUpdate(e.getOffset(), -e.getLength(), false);
         }
 
         public void changedUpdate(DocumentEvent e) {
         }
 
     }
 
     protected class TextPaneMouseAdapter extends MouseAdapter {
         public void mousePressed(MouseEvent e) {
             // Update the cursor highligthing
             if(textPane.highlightCursorLine)
                 textPane.repaint();
 
             // Expand any collapsed rule if the caret
             // has been placed in the placeholder zone
             Element elem = textPane.getStyledDocument().getCharacterElement(getCaretPosition());
             ATEFoldingEntityProxy proxy = textPane.getTopLevelEntityProxy(elem);
             if(proxy != null && !proxy.getEntity().foldingEntityIsExpanded()) {
                 textPane.toggleFolding(proxy);
                 gutter.markDirty();
             }
 
             checkForPopupTrigger(e);
 
             if(delegate != null)
                 delegate.ateMousePressed(e.getPoint());
         }
 
         public void mouseReleased(MouseEvent e) {
             checkForPopupTrigger(e);
         }
 
         public void checkForPopupTrigger(MouseEvent e) {
             if(e.isPopupTrigger()) {
                 int index = textPane.viewToModel(e.getPoint());
                 if(textPane.getSelectionStart() != textPane.getSelectionEnd()) {
                     if(index < textPane.getSelectionStart() || index > textPane.getSelectionEnd())
                         setCaretPosition(index, false, false);
                 } else if(index != getCaretPosition())
                     setCaretPosition(index, false, false);
 
                 textPaneInvokePopUp(e.getComponent(), e.getX(), e.getY());
             }
         }
 
         public void mouseExited(MouseEvent e) {
             if(delegate != null)
                 delegate.ateMouseExited();
         }
     }
 
     protected class TextPaneMouseMotionAdapter extends MouseMotionAdapter {
         public void mouseMoved(MouseEvent e) {
             if(delegate != null)
                 delegate.ateMouseMoved(e.getPoint());
         }
     }
 
 }
