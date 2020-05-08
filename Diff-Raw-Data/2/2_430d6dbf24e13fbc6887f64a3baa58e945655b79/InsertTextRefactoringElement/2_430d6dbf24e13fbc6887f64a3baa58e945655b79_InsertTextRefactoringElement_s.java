  /*
  *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  *  Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
  * 
  *  The contents of this file are subject to the terms of either the GNU
  *  General Public License Version 2 only ("GPL") or the Common
  *  Development and Distribution License("CDDL") (collectively, the
  *  "License"). You may not use this file except in compliance with the
  *  License. You can obtain a copy of the License at
  *  http://www.netbeans.org/cddl-gplv2.html
  *  or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  *  specific language governing permissions and limitations under the
  *  License.  When distributing the software, include this License Header
  *  Notice in each file and include the License file at
  *  nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  *  particular file as subject to the "Classpath" exception as provided
  *  by Sun in the GPL Version 2 section of the License file that
  *  accompanied this code. If applicable, add the following below the
  *  License Header, with the fields enclosed by brackets [] replaced by
  *  your own identifying information:
  *  "Portions Copyrighted [year] [name of copyright owner]"
  * 
  *  Contributor(s):
  * 
  *  Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.refactoring.impl;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.Position.Bias;
 import javax.swing.text.StyledDocument;
 import org.netbeans.editor.GuardedDocument;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
 import org.openide.cookies.EditorCookie;
 import org.openide.cookies.LineCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObject;
 import org.openide.text.DataEditorSupport;
 import org.openide.text.Line;
 import org.openide.text.NbDocument;
 import org.openide.text.PositionBounds;
 import org.openide.util.Lookup;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 public class InsertTextRefactoringElement extends SimpleRefactoringElementImplementation {
     final private static Logger LOGGER = Logger.getLogger(InsertTextRefactoringElement.class.getName());
     final private static boolean DEBUG = LOGGER.isLoggable(Level.FINEST);
     
     private Lookup context;
     private int startPosition = -1;
     private String text = "";
     private int length = -1;
     private boolean forceNewLine;
 
     private FileObject fo;
     private DataEditorSupport des;
     private GuardedDocument doc;
     private LineCookie lc;
 
     final public static InsertTextRefactoringElement create(FileObject fo, int start, boolean forceNewLine, String text, Lookup context) {
         try {
             return new InsertTextRefactoringElement(fo, start, forceNewLine, text, context);
         } catch (IOException e) {
             LOGGER.log(Level.SEVERE, null, e);
         }
         return null;
     }
 
     final public static InsertTextRefactoringElement create(FileObject fo, int start, String text, Lookup context) {
         try {
             return new InsertTextRefactoringElement(fo, start, false, text, context);
         } catch (IOException e) {
             LOGGER.log(Level.SEVERE, null, e);
         }
         return null;
     }
 
     private InsertTextRefactoringElement(FileObject fo, int start, boolean forceNewLine, String text, Lookup context) throws IOException {
         this.startPosition = start;
         this.text = text;
         this.length = text.length();
         this.context = context;
         this.fo = fo;
         this.forceNewLine = forceNewLine;
         init();
     }
 
     private void init() throws IOException {
         DataObject dobj = DataObject.find(fo);
         des = (DataEditorSupport)dobj.getCookie(EditorCookie.class);
         doc = (GuardedDocument)des.openDocument();
         lc = dobj.getCookie(LineCookie.class);
 
         try {
             if (forceNewLine && startPosition > -1) {
                 int lineNo = Utilities.getLineOffset(doc, startPosition);
                 startPosition = NbDocument.findLineOffset(doc, lineNo+1);
             }
         } catch (BadLocationException e) {
            throw new IOException(e);
         }
     }
 
     public String getDisplayText() {
         try {
             StringBuilder origLine = new StringBuilder();
             int delta = extractLine(startPosition, origLine);
 
             StringBuilder newLine = new StringBuilder(origLine);
             if (delta >= 0) {
                 newLine.insert(delta, text);
             } else {
                 if (origLine.length() > 0) {
                     newLine.delete(0, origLine.length() - 1);
                 }
                 newLine.append(text);
             }
 
             return processDiff(newLine.toString(), origLine.toString());
         } catch (Exception e) {
             e.printStackTrace();
             return "Inserting";
         }
     }
 
     public Lookup getLookup() {
         return context;
     }
     public FileObject getParentFile() {
         return fo;
     }
 
     public PositionBounds getPosition() {
         return new PositionBounds(des.createPositionRef(startPosition, Bias.Forward), des.createPositionRef(startPosition + length - 1, Bias.Forward));
     }
 
     public String getText() {
         return "Insert text";
     }
 
     public void performChange() {
         final Document doc = des.getDocument();
         if (doc instanceof GuardedDocument) {
             ((GuardedDocument)doc).runAtomic(new Runnable() {
 
                 public void run() {
                     try {
                         synchronized(doc) {
                             TransformationContext tc = context.lookup(TransformationContext.class);
                             int offset = tc.getRealOffset(startPosition);
                             doc.insertString(offset, text, null);
                             tc.replaceText(startPosition, 0, length);
                         }
                     } catch (BadLocationException e) {
                         e.printStackTrace();
                     }
                 }
             });
         }
     }
 
     private String processDiff(String newText, String oldText) {
         String closingTag = "";
         StringBuilder sb = new StringBuilder();
 
         int newLength = newText.length();
         int oldLength = oldText.length();
 
         if (oldLength == 0) {
             closingTag = "</b>"; // NOI18N
             sb.append("<b>").append(newText); // NOI18N
         } else if (newLength == 0) {
             closingTag = "]</b>"; // NOI18N
             sb.append("<b>[").append(oldText); // NOI18N
         } else {
             char[] newChars = new char[newLength];
             char[] oldChars = new char[oldLength];
 
             newText.getChars(0, newLength, newChars, 0);
             oldText.getChars(0, oldLength, oldChars, 0);
 
             // opt[i][j] = length of LCS of oldChars[i..oldLength] and newChars[j..newLength]
             int[][] opt = new int[oldLength+1][newLength+1];
 
             // compute length of LCS and all subproblems via dynamic programming
             for (int i = oldLength-1; i >= 0; i--) {
                 for (int j = newLength-1; j >= 0; j--) {
                     if (oldChars[i] == newChars[j])
                         opt[i][j] = opt[i+1][j+1] + 1;
                     else
                         opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
                 }
             }
 
             // recover LCS itself and print out non-matching lines to standard output
             int i = 0, j = 0;
 
             while(i < oldLength && j < newLength) {
                 if (oldChars[i] == newChars[j]) {
                     sb.append(closingTag);
                     closingTag = "";
                     sb.append(oldChars[i]);
                     i++;
                     j++;
                 } else {
                     int oldLCS = (i+1 == oldLength) ? Integer.MIN_VALUE : opt[i+1][j];
                     int newLCS = (j+1 == newLength) ? Integer.MIN_VALUE : opt[i][j+1];
                     if (oldLCS >= newLCS)  {
                         if (!closingTag.equals("</b>]")) {
                             sb.append(closingTag);
                             sb.append("[<b>");
                             closingTag = "</b>]";
                         }
                         sb.append(oldChars[i++]);
                     } else {
                         if (!closingTag.equals("</b>")) {
                             sb.append(closingTag);
                             sb.append("<b>");
                             closingTag = "</b>";
                         }
                         sb.append(newChars[j++]);
                     }
                 }
             }
         }
         sb.append(closingTag);
         return sb.toString();
     }
 
     private int extractLine(int offset, StringBuilder sb) throws BadLocationException {
         int lineNo = Utilities.getLineOffset(doc, offset);
         Line l = lc.getLineSet().getCurrent(lineNo);
         sb.append(l.getText().trim());
         int lineOff = NbDocument.findLineOffset((StyledDocument)doc, lineNo);
 
         lineOff = Utilities.getFirstNonWhiteFwd(doc, lineOff);
         return offset - lineOff;
     }
 }
