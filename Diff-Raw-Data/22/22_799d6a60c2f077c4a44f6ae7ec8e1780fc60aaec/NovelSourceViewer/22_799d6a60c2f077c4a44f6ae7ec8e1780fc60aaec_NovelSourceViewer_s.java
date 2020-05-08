 /*
  * [The "New BSD" license]
  * Copyright (c) 2012 The Board of Trustees of The University of Alabama
  * All rights reserved.
  *
  * See LICENSE for details.
  */
 package edu.ua.eng.software.novel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.Point;
 
 import java.io.FileReader;
 import java.io.IOException;
 
 import de.uni_bremen.st.rcf.model.File;
 import de.uni_bremen.st.rcf.model.Fragment;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JViewport;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 
 import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 import org.fife.ui.rtextarea.RTextScrollPane;
 
 import edu.ua.eng.software.novel.NovelClassesTree.FragmentCell;
 
 /**
  * Creates the contents of the Source Viewer tab
  * 
  * @author Colin C. Hemphill <colin@hemphill.us>
  */
 @SuppressWarnings("serial")
 public class NovelSourceViewer extends JPanel
 {
     private RSyntaxTextArea sourceLeft;
     private RSyntaxTextArea sourceRight;
     private RTextScrollPane scrollLeft;
     private RTextScrollPane scrollRight;
     private JLabel pathLeft;
     private JLabel pathRight;
     private JSplitPane sourcePaneLeft;
     private JSplitPane sourcePaneRight;
     private JSplitPane splitSource;
     private Color cloneHighlight = Color.YELLOW;
 
     public NovelSourceViewer() {
         super(new BorderLayout());
 
         sourceLeft = new RSyntaxTextArea();
         sourceLeft.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
         sourceLeft.setCodeFoldingEnabled(true);
         sourceLeft.setAntiAliasingEnabled(true);
         sourceLeft.setEditable(false);
 
         sourceRight = new RSyntaxTextArea();
         sourceRight.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
         sourceRight.setCodeFoldingEnabled(true);
         sourceRight.setAntiAliasingEnabled(true);
         sourceRight.setEditable(false);
 
         scrollLeft = new RTextScrollPane(sourceLeft);
         scrollRight = new RTextScrollPane(sourceRight);
 
         pathLeft = new JLabel("Source Location:");
         pathRight = new JLabel("Source Location:");
        pathLeft.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pathRight.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
         sourcePaneLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pathLeft,
                 scrollLeft);
         sourcePaneLeft.setDividerLocation(25);
         sourcePaneLeft.setDividerSize(0);
         sourcePaneLeft.setEnabled(false);
 
         sourcePaneRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pathRight,
                 scrollRight);
         sourcePaneRight.setDividerLocation(25);
         sourcePaneRight.setDividerSize(0);
         sourcePaneRight.setEnabled(false);
 
         splitSource = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
         splitSource.setResizeWeight(0.5);
         splitSource.setDividerSize(0);
         splitSource.setEnabled(false);
         initialize();
     }
    
     private void initialize() {
         super.add(sourcePaneLeft, BorderLayout.CENTER);
     }
 
     public void setSingleSource(File sourceFile) {
         setFile(sourceFile, sourceLeft, pathLeft);
         removeAll();
         super.add(sourcePaneLeft);
         revalidate();
         repaint();
     }
 
     public void setSplitSource(Fragment leftFrag, Fragment rightFrag) {
         setFile(leftFrag.getStart().getFile(), sourceLeft, pathLeft);
        setHighlight(sourceLeft, leftFrag.getStart().getLine(), leftFrag.getEnd().getLine());
         splitSource.setLeftComponent(sourcePaneLeft);
 
         setFile(rightFrag.getStart().getFile(), sourceRight, pathRight);
        setHighlight(sourceRight, rightFrag.getStart().getLine(), rightFrag.getEnd().getLine());
         splitSource.setRightComponent(sourcePaneRight);
 
         removeAll();
         super.add(splitSource);
         revalidate();
         centerLineInScrollPane(sourceLeft, scrollLeft);
         centerLineInScrollPane(sourceRight, scrollRight);
         repaint();
     }
 
     private String getPathFromFile(File file) {
         CloneDataModel model = CloneDataModel.getInstance();
         String path = file.getRelativePath();
         if (path.startsWith("./")) {
             path = path.substring(2, path.length());
         }
         return model.getVersion().getBasepath() + "/" + path;
     }
 
     private void setFile(File file, RSyntaxTextArea text, JLabel label) {
         text.removeAllLineHighlights();
         String path = getPathFromFile(file);
         try {
             FileReader fr = new FileReader(path);
             text.read(fr, null);
             label.setText(path);
             text.setCaretPosition(0);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void setHighlight(RSyntaxTextArea text, int startLine, int endLine) {
         try {
            for(int i = startLine; i <= endLine; i++) {
                 text.addLineHighlight(i - 1, cloneHighlight);
             }
             text.setCaretPosition(text.getLineStartOffset(startLine - 1));
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
     }
 
    public void centerLineInScrollPane(RSyntaxTextArea text, RTextScrollPane scroll) {
        //Totally doesn't work.
         try {
             JViewport viewport = scroll.getViewport();
             Rectangle r = text.modelToView(text.getCaretPosition());
             int extentHeight = viewport.getExtentSize().height;
             int viewHeight = viewport.getViewSize().height;
 
             int y = Math.max(0, r.y);
             y = Math.min(y, viewHeight - extentHeight);
 
             viewport.setViewPosition(new Point(0, y));
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
     }
 }
