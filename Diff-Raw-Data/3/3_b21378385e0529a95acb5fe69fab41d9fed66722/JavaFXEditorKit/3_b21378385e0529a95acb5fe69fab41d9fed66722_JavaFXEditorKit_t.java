 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package org.netbeans.modules.javafx.editor;
 
 import org.netbeans.api.java.queries.SourceLevelQuery;
 import org.netbeans.editor.BaseAction;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.editor.BaseKit;
 import org.netbeans.editor.LocaleSupport;
 import org.netbeans.modules.editor.NbEditorKit;
 import org.netbeans.modules.editor.NbEditorUtilities;
 import org.netbeans.modules.javafx.editor.imports.JavaFXImports;
 import org.openide.loaders.DataObject;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.text.*;
 import java.awt.event.ActionEvent;
 import java.util.logging.Logger;
 import org.netbeans.editor.ActionFactory.FormatAction;
 import org.netbeans.modules.javafx.editor.preview.JavaFXPreviewTopComponent;
 import org.openide.util.ImageUtilities;
 
 /**
  * @author answer
  */
 public class JavaFXEditorKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider {
 
     private static final String toggleFXPreviewExecution = "toggle-fx-preview-execution";               //NOI18N
     private static final String buttonResetFXPreviewExecution = "toggle-reset-fx-preview-execution";    //NOI18N
     private static final String buttonPrintFXPreview = "print-fx-preview";                              //NOI18N
     public static final String FX_MIME_TYPE = "text/x-fx";                                              //NOI18N
     private static Logger log = Logger.getLogger(JavaFXEditorKit.class.getName());
 
     public JavaFXEditorKit() {
         super();
     }
 
     @Override
     public String getContentType() {
         return FX_MIME_TYPE;
     }
 
 
     @Override
     public Document createDefaultDocument() {
         Document doc = new JavaFXDocument(FX_MIME_TYPE);
         Object mimeType = doc.getProperty("mimeType");                          //NOI18N
         if (mimeType == null) {
             doc.putProperty("mimeType", getContentType());                      //NOI18N
         }
         return doc;
     }
 
     @Override
     protected Action[] createActions() {
         Action[] superActions = super.createActions();
         Action[] javafxActions = new Action[]{
                 new CommentAction("//"),                                        //NOI18N
                 new UncommentAction("//"),                                      //NOI18N
                 new ToggleFXPreviewExecution(),
                 new JavaFXDefaultKeyTypedAction(),
                 new JavaFXDeleteCharAction(deletePrevCharAction, false),
                 new JavaFXGoToDeclarationAction(),
                 new JavaFXGoToSourceAction(),
                 new JavaFXGotoHelpAction(),
                 JavaFXImports.getInstance(),
                 new JavaFXFormatAction(),
                 new JavaFXInsertBreakAction()
         };
         return TextAction.augmentList(superActions, javafxActions);
     }
 
 
     public class ToggleFXPreviewExecution extends BaseAction implements org.openide.util.actions.Presenter.Toolbar {
 
         @Override
         protected Object clone() throws CloneNotSupportedException {
             return super.clone();
         }
 
         public ToggleFXPreviewExecution() {
             super(toggleFXPreviewExecution);
             putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javafx/editor/resources/preview.png"))); // NOI18N
             putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaFXEditorKit.class).getString("toggle-fx-preview-execution")); // NOI18N
         }
 
         public void actionPerformed(ActionEvent evt, JTextComponent target) {
             JavaFXPreviewTopComponent tc = JavaFXPreviewTopComponent.findInstance();
             if (tc.isOpened()) {
                 tc.close();
             } else {
                 tc.open();
                 tc.requestActive();
             }
         }
 
         public java.awt.Component getToolbarPresenter() {
             PreviewButton b = new PreviewButton();
             b.setSelected(false);
             b.setAction(this);
 //            b.setEnabled(Bridge.isStarted());
             b.putClientProperty("enablePreviewMark", Boolean.TRUE);             //NOI18N
             b.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/editor/Bundle").getString("preview_toolbar")); // NOI18N
             return b;
         }
 
         private final class PreviewButton extends JButton {
 
             public PreviewButton() {
                 super();
             }
 
             @Override
             public void setBorderPainted(boolean arg0) {
                 if (!isSelected()) {
                     super.setBorderPainted(arg0);
                 }
             }
 
             @Override
             public void setContentAreaFilled(boolean arg0) {
                 if (!isSelected()) {
                     super.setContentAreaFilled(arg0);
                 }
             }
         }
     }
 
     static class DocEvent implements DocumentEvent {
         Document doc = null;
 
         public DocEvent(Document doc) {
             this.doc = doc;
         }
 
         public int getOffset() {
             return 0;
         }
 
         public int getLength() {
             return 0;
         }
 
         public Document getDocument() {
             return doc;
         }
 
         public EventType getType() {
             return EventType.INSERT;
         }
 
         public ElementChange getChange(Element elem) {
             return null;
         }
 
     }
 
     public static class JavaFXDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
 
         /**
          * Check whether there was any important character typed
          * so that the line should be possibly reformatted.
          */
         @Override
         protected void checkIndent(JTextComponent target, String typedText) {
             super.checkIndent(target, typedText);
         }
 
         @Override
         protected void insertString(BaseDocument doc, int dotPos,
                                     Caret caret, String str,
                                     boolean overwrite) throws BadLocationException {
             char insertedChar = str.charAt(0);
             if (insertedChar == '\"' || insertedChar == '\'') {                 // NOI18N
                 boolean inserted = BracketCompletion.completeQuote(doc, dotPos, caret, insertedChar);
                 if (inserted) {
                     caret.setDot(dotPos + 1);
                 } else {
                     super.insertString(doc, dotPos, caret, str, overwrite);
 
                 }
             } else {
                 super.insertString(doc, dotPos, caret, str, overwrite);
                 BracketCompletion.charInserted(doc, dotPos, caret, insertedChar);
             }
         }
 
         @Override
         protected void replaceSelection(JTextComponent target,
                                         int dotPos,
                                         Caret caret,
                                         String str,
                                         boolean overwrite)
                 throws BadLocationException {
             char insertedChar = str.charAt(0);
             Document doc = target.getDocument();
             if (insertedChar == '\"' || insertedChar == '\'') {                 // NOI18N
                 if (doc != null) {
                     try {
                         boolean inserted = false;
                         int p0 = Math.min(caret.getDot(), caret.getMark());
                         int p1 = Math.max(caret.getDot(), caret.getMark());
                         if (p0 != p1) {
                             doc.remove(p0, p1 - p0);
                         }
                         int caretPosition = caret.getDot();
                         if (doc instanceof BaseDocument) {
                             inserted = BracketCompletion.completeQuote(
                                     (BaseDocument) doc,
                                     caretPosition,
                                     caret, insertedChar);
                         }
                         if (inserted) {
                             caret.setDot(caretPosition + 1);
                         } else {
                             if (str != null && str.length() > 0) {
                                 doc.insertString(p0, str, null);
                             }
                         }
                     } catch (BadLocationException e) {
                         e.printStackTrace();
                     }
                 }
             } else {
                 super.replaceSelection(target, dotPos, caret, str, overwrite);
                 if (doc instanceof BaseDocument) {
                     BracketCompletion.charInserted((BaseDocument) doc, caret.getDot() - 1, caret, insertedChar);
                 }
             }
         }
     }
 
 
 //    public static class JavaInsertBreakAction extends InsertBreakAction {
 //
 //        static final long serialVersionUID = -1506173310438326380L;
 //
 //        @Override
 //        protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
 //            int dotPos = caret.getDot();
 //            if (BracketCompletion.posWithinString(doc, dotPos)) {
 //                try {
 //                    doc.insertString(dotPos, "\"  \"", null);                       // NOI18N
 //                    dotPos += 3;
 //                    caret.setDot(dotPos);
 //                    return dotPos;
 //                } catch (BadLocationException ex) {
 //                    log.severe("Excetion thrown during InsertBreakAction. " + ex);  // NOI18N
 //                }
 //            } else {
 //                try {
 //                    TokenSequence<JFXTokenId> seq = BracketCompletion.getTokenSequence(doc, dotPos);
 //                    JFXTokenId id = seq.moveNext() ? seq.token().id() : null;
 //                    if ((id == JFXTokenId.COMMENT || id == JFXTokenId.DOC_COMMENT) && seq.offset() < dotPos) {
 //                        doc.insertString(dotPos, "* ", null);                       // NOI18N
 //                        caret.setDot(dotPos);
 //                        return dotPos + 3;
 //                    } else {
 //                        return processRawString(doc, caret);
 //                    }
 //                } catch (BadLocationException ex) {
 //                    log.severe("Excetion thrown during InsertBreakAction. " + ex);  // NOI18N
 //                }
 //            }
 //            return null;
 //        }
 //
 //        private Object processRawString(BaseDocument doc, Caret caret) throws BadLocationException {
 //            int dotPos = caret.getDot();
 //            Indent indent = Indent.get(doc);
 //            if (BracketCompletion.isAddRightBrace(doc, dotPos)) {
 //                int end = BracketCompletion.getRowOrBlockEnd(doc, dotPos);
 //                doc.insertString(end, "}", null);                               // NOI18N
 //                indent.reindent(end);
 //                caret.setDot(end);
 //                return end + 1;
 //            } else {
 //                final String epsylon = doc.getText(dotPos - 1, 2);
 //                final char c = epsylon.charAt(0);
 //                if (c == '[' || c == '(' || c == '{') {                         // NOI18N
 //                    if (epsylon.charAt(1) == BracketCompletion.matching(c)) {
 //                        doc.insertString(dotPos + 1, "\n", null);               // NOI18N
 //                        indent.reindent(dotPos);
 //                        caret.setDot(dotPos);
 //                        return dotPos + 1;
 //                    }
 //                } else if (c == '*') {                                          //NOI18N
 //                    return processStartOfComment(doc, caret, indent);
 //                }
 //            }
 //            return null;
 //        }
 //
 //        private Object processStartOfComment(BaseDocument doc, Caret caret, Indent indent) throws BadLocationException {
 //            int dotPos = caret.getDot();
 //            int start = Math.max(dotPos - 3, 0);
 //            String material = doc.getText(start, dotPos).trim();
 //            if (material.startsWith("/*")) {                                        // NOI18N
 //                doc.insertString(dotPos, " * \n */\n", null);                       // NOI18N
 //                indent.reindent(start, Math.min(start + 8, doc.getLength() - 1));
 //                caret.setDot(dotPos);
 //                return dotPos + 4;
 //            }
 //            return null;
 //        }
 //
 //        @Override
 //        protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret, Object cookie) {
 //            if (cookie != null) {
 //                if (cookie instanceof Integer) {
 //                    int newDot = (Integer) cookie;
 //                    // integer
 ////                    int nowDotPos = caret.getDot();
 //                    try {
 //                        caret.setDot(newDot);
 //                        Indent.get(doc).reindent(newDot);
 //                    } catch (BadLocationException ex) {
 //                        log.severe("Exception thrown during InsertBreakAction. " + ex);   // NOI18N
 //                    }
 //                    /*if (newDot > nowDotPos + 1) {
 //                        caret.setDot(newDot);
 //                    } else {
 //                        caret.setDot(nowDotPos + 1);
 //                    }*/
 //                }
 //            }
 //        }
 //
 //    }
 
     public static class JavaFXDeleteCharAction extends ExtDeleteCharAction {
 
         public JavaFXDeleteCharAction(String nm, boolean nextChar) {
             super(nm, nextChar);
         }
 
         @Override
         protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch)
                 throws BadLocationException {
             BracketCompletion.charBackspaced(doc, dotPos, ch);
         }
     }
 
     private static class JavaFXGoToDeclarationAction extends GotoDeclarationAction {
         public
         @Override
         boolean gotoDeclaration(JTextComponent target) {
             if (!(target.getDocument() instanceof BaseDocument)) // Fixed #113062
                 return false;
             GoToSupport.goTo((BaseDocument) target.getDocument(), target.getCaretPosition(), false);
             return true;
         }
     }
 
     private static class JavaFXGoToSourceAction extends BaseAction {
 
         static final long serialVersionUID = -6440495023918097760L;
 
         @SuppressWarnings("deprecation")
         public JavaFXGoToSourceAction() {
             super(gotoSourceAction,
                     ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET
                             | SAVE_POSITION
             );
             putValue(TRIMMED_TEXT, LocaleSupport.getString("goto-source-trimmed"));  //NOI18N            
         }
 
         public void actionPerformed(ActionEvent evt, JTextComponent target) {
             if (target != null && (target.getDocument() instanceof BaseDocument)) {
                 GoToSupport.goTo((BaseDocument) target.getDocument(), target.getCaretPosition(), true);
             }
         }
 
         @Override
         public String getPopupMenuText(JTextComponent target) {
             return NbBundle.getBundle(JavaFXEditorKit.class).getString("goto_source_open_source_not_formatted"); //NOI18N
         }
 
         @Override
         protected Class getShortDescriptionBundleClass() {
             return BaseKit.class;
         }
     }
 
     public String getSourceLevel(BaseDocument doc) {
         DataObject dob = NbEditorUtilities.getDataObject(doc);
         return dob != null ? SourceLevelQuery.getSourceLevel(dob.getPrimaryFile()) : null;
     }
 
     public HelpCtx getHelpCtx() {
         return new org.openide.util.HelpCtx(JavaFXEditorKit.class);
     }
 
     // TODO do it trough new annotation registration
//    @EditorActionRegistration(name = BaseKit.formatAction, mimeType = FX_MIME_TYPE)
     private static class JavaFXFormatAction extends FormatAction {
         public JavaFXFormatAction() {
            putValue(Action.NAME, BaseKit.formatAction);
             setEnabled(false);
         }
     }
 
     private static class JavaFXGotoHelpAction extends BaseAction {
 
         public JavaFXGotoHelpAction() {
             super(gotoHelpAction, ABBREV_RESET | MAGIC_POSITION_RESET
                     | UNDO_MERGE_RESET |SAVE_POSITION);
             putValue ("helpID", JavaFXGotoHelpAction.class.getName ()); // NOI18N
             // fix of #25090; [PENDING] there should be more systematic solution for this problem
             putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaFXEditorKit.class).getString("javafx-desc-goto-help")); // NOI18N
         }
 
         public void actionPerformed(ActionEvent evt, JTextComponent target) {
             if (target != null) {
                 GoToSupport.goToJavadoc(target.getDocument(), target.getCaretPosition());
             }
         }
 
         @Override
         public String getPopupMenuText(JTextComponent target) {
             return NbBundle.getBundle(JavaFXEditorKit.class).getString("show_javadoc"); // NOI18N
         }
 
     }
 
 }
