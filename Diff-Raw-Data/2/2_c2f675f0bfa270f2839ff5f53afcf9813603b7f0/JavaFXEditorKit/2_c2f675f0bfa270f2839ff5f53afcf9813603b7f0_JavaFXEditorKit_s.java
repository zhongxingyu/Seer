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
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.print.PrintManager;
 import org.netbeans.editor.*;
 import org.netbeans.modules.editor.NbEditorUtilities;
 import org.netbeans.modules.editor.indent.api.Indent;
 import org.netbeans.modules.javafx.editor.imports.JavaFXImports;
 import org.netbeans.modules.javafx.preview.Bridge;
 import org.netbeans.modules.javafx.preview.SerializableImage;
 import org.netbeans.modules.lexer.editorbridge.LexerEditorKit;
 import org.openide.loaders.DataObject;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.text.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 import java.rmi.RemoteException;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * @author answer
  */
 public class JavaFXEditorKit extends LexerEditorKit implements org.openide.util.HelpCtx.Provider {
 
     private static final String toggleFXPreviewExecution = "toggle-fx-preview-execution"; //NOI18N
     private static final String buttonResetFXPreviewExecution = "toggle-reset-fx-preview-execution"; //NOI18N
     private static final String buttonPrintFXPreview = "print-fx-preview"; //NOI18N
     private static final String FX_MIME_TYPE = "text/x-fx";
     private static Logger log = Logger.getLogger(JavaFXEditorKit.class.getName());
 
     public JavaFXEditorKit() {
         super(FX_MIME_TYPE);
 
         Settings.addInitializer(new Settings.Initializer() {
             public String getName() {
                 return FX_MIME_TYPE;
             }
 
             @SuppressWarnings("unchecked")
             public void updateSettingsMap(Class kitClass, Map settingsMap) {
                 settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
             }
 
         });
     }
 
     @Override
     public String getContentType() {
         return FX_MIME_TYPE;
     }
 
 
     @Override
     public Document createDefaultDocument() {
         Document doc = new JavaFXDocument(FX_MIME_TYPE);
         Object mimeType = doc.getProperty("mimeType");
         if (mimeType == null) {
             doc.putProperty("mimeType", getContentType());
         }
         return doc;
     }
 
     @Override
     protected Action[] createActions() {
         Action[] superActions = super.createActions();
         ResetFXPreviewExecution resetAction = new ResetFXPreviewExecution();
         PrintFXPreview previewAction = new PrintFXPreview();
         Action[] javafxActions = new Action[]{
                 new CommentAction("//"),
                 new UncommentAction("//"),
                 new ToggleFXPreviewExecution(resetAction, previewAction),
                 resetAction,
                 previewAction,
                 new JavaDefaultKeyTypedAction(),
                 new JavaDeleteCharAction(deletePrevCharAction, false),
                 new JavaFXGoToDeclarationAction(),
                 new JavaFXGoToSourceAction(),
                 JavaFXImports.getInstance(),
                 new JavaInsertBreakAction()
         };
         return TextAction.augmentList(superActions, javafxActions);
     }
 
 
     public class ToggleFXPreviewExecution extends BaseAction implements org.openide.util.actions.Presenter.Toolbar {
         ResetFXPreviewExecution resetAction = null;
         PrintFXPreview previewAction = null;
 
         @Override
         protected Object clone() throws CloneNotSupportedException {
             return super.clone();
         }
 
         public ToggleFXPreviewExecution(ResetFXPreviewExecution resetAction, PrintFXPreview previewAction) {
             super(toggleFXPreviewExecution);
             this.resetAction = resetAction;
             this.previewAction = previewAction;
             putValue(Action.SMALL_ICON, new ImageIcon(org.openide.util.Utilities.loadImage(
                     "org/netbeans/modules/javafx/editor/resources/preview.png"))); // NOI18N
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaFXEditorKit.class).getString("enable-fx-preview-execution"));
         }
 
         public void actionPerformed(ActionEvent evt, JTextComponent target) {
             if (target != null) {
                 JavaFXDocument doc = (JavaFXDocument) target.getDocument();
                 if (doc != null) {
                     doc.enableExecution(true);
                 }
             }
         }
 
 
         public java.awt.Component getToolbarPresenter() {
             PreviewButton b = new PreviewButton();
             b.setSelected(false);
             b.setAction(this);
             b.setEnabled(Bridge.isStarted());
             b.putClientProperty("enablePreviewMark", Boolean.TRUE); //NOI18N
             b.setText("");
             return b;
         }
 
         private final class PreviewButton extends JButton implements ChangeListener {
 
             public PreviewButton() {
                 super();
                 Bridge.addStartListener(this);
             }
 
             public void stateChanged(ChangeEvent evt) {
                 setEnabled(Bridge.isStarted());
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
 
     public static class ResetFXPreviewExecution extends BaseAction implements org.openide.util.actions.Presenter.Toolbar {
 
         public ResetFXPreviewExecution() {
             super(buttonResetFXPreviewExecution);
             putValue(Action.SMALL_ICON, new ImageIcon(org.openide.util.Utilities.loadImage(
                     "org/netbeans/modules/javafx/editor/resources/reset_preview.png"))); // NOI18N
             putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaFXEditorKit.class).getString("reset-fx-preview-execution"));
         }
 
         public void actionPerformed(ActionEvent evt, JTextComponent target) {
             if (target != null) {
                 JavaFXDocument doc = (JavaFXDocument) target.getDocument();
             }
             Bridge.restart();
         }
 
         private final class ResetButton extends JButton implements ChangeListener {
             public ResetButton() {
                 super();
                 Bridge.addStartListener(this);
             }
 
             public void stateChanged(ChangeEvent e) {
                 setEnabled(Bridge.isStarted());
             }
         }
 
         public java.awt.Component getToolbarPresenter() {
             ResetButton b = new ResetButton();
             b.setAction(this);
             b.putClientProperty("resetPreviewMark", Boolean.TRUE);                  //NOI18N
             b.setText("");
             b.setEnabled(false);
             return b;
         }
     }
 
 
     public static class PrintFXPreview extends BaseAction implements org.openide.util.actions.Presenter.Toolbar {
 
         class JPrintPanel extends JPanel {
             private BufferedImage offscreenBuffer = null;
 
             public JPrintPanel() {
                 super();
                 putClientProperty("print.printable", Boolean.TRUE); // NOI18N
             }
 
             public void setImage(SerializableImage image) {
                 offscreenBuffer = image.getBufferedImage();
                 setSize(offscreenBuffer.getWidth(), offscreenBuffer.getHeight());
             }
 
             @Override
             public void print(Graphics g) {
                 g.drawImage(offscreenBuffer, 0, 0, null);
             }
 
             @Override
             public boolean isShowing() {
                 return true;
             }
         }
 
         public PrintFXPreview() {
             super(buttonPrintFXPreview);
             putValue(Action.SMALL_ICON, new ImageIcon(org.openide.util.Utilities.loadImage(
                     "org/netbeans/modules/javafx/editor/resources/print_preview.png"))); // NOI18N
             putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaFXEditorKit.class).getString("print-fx-preview"));
         }
 
         public void actionPerformed(ActionEvent evt, JTextComponent target) {
             if (target != null) {
                 JavaFXDocument doc = (JavaFXDocument) target.getDocument();
                 if (doc != null) {
                     try {
                         SerializableImage image = Bridge.getPreview(doc).getPicture();
                         JPrintPanel printPanel = new JPrintPanel();
                         printPanel.setImage(image);
                         PrintManager.printAction(printPanel).actionPerformed(evt);
                     } catch (RemoteException ex) {
                         ex.printStackTrace();
                     }
                 }
             }
         }
 
         public java.awt.Component getToolbarPresenter() {
             JButton b = new JButton(this);
             b.setAction(this);
             b.putClientProperty("printPreviewMark", Boolean.TRUE);                  //NOI18N
             b.setText("");
             b.setEnabled(false);
             return b;
         }
     }
 
     public static class JavaDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
 
         @Override
         protected void insertString(BaseDocument doc, int dotPos,
                                     Caret caret, String str,
                                     boolean overwrite) throws BadLocationException {
             char insertedChar = str.charAt(0);
             if (insertedChar == '\"' || insertedChar == '\'') {
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
             if (insertedChar == '\"' || insertedChar == '\'') {
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
 
     public static class JavaInsertBreakAction extends InsertBreakAction {
 
         static final long serialVersionUID = -1506173310438326380L;
 
         @Override
         protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
             int dotPos = caret.getDot();
             if (BracketCompletion.posWithinString(doc, dotPos)) {
                 try {
                     doc.insertString(dotPos, "\"  \"", null); //NOI18N
                     dotPos += 3;
                     caret.setDot(dotPos);
                     return dotPos;
                 } catch (BadLocationException ex) {
                     log.severe("Excetion thrown during InsertBreakAction. " + ex);
                 }
             } else {
                 try {
                     JFXTokenId id = BracketCompletion.tokenAt(doc, dotPos);
                     if (id == JFXTokenId.COMMENT || id == JFXTokenId.DOC_COMMENT) {
                         doc.insertString(dotPos, "* ", null);
                         caret.setDot(dotPos);
                         return dotPos+3;
                     } else {
                         return processRawString(doc, caret);
                     }
                 } catch (BadLocationException ex) {
                     log.severe("Excetion thrown during InsertBreakAction. " + ex);
                 }
             }
             return null;
         }
 
         private Object processRawString(BaseDocument doc, Caret caret) throws BadLocationException {
             int dotPos = caret.getDot();
             Indent indent = Indent.get(doc);
             if (BracketCompletion.isAddRightBrace(doc, dotPos)) {
                 int end = BracketCompletion.getRowOrBlockEnd(doc, dotPos);
                 doc.insertString(end, "}", null); // NOI18N
                 indent.reindent(end);
                 caret.setDot(dotPos);
                 return Boolean.TRUE;
             } else {
                 final String epsylon = doc.getText(dotPos - 1, 2);
                 final char c = epsylon.charAt(0);
                 if (c == '[' || c == '(' || c == '{') {
                     if (epsylon.charAt(1) == BracketCompletion.matching(c)) {
                         doc.insertString(dotPos + 1, "\n", null);
                         indent.reindent(dotPos);
                         caret.setDot(dotPos);
                         return Boolean.TRUE;
                     }
                 } else if (c == '*') {
                     return processStartOfComment(doc, caret, indent);
                 }
             }
             return null;
         }
 
         private Object processStartOfComment(BaseDocument doc, Caret caret, Indent indent) throws BadLocationException {
             int dotPos = caret.getDot();
             int start = Math.max(dotPos - 3, 0);
             String material = doc.getText(start, dotPos).trim();
             if (material.startsWith("/*")) {
                 doc.insertString(dotPos, " * \n */\n", null);
                 indent.reindent(start, Math.min(start + 8, doc.getLength() - 1));
                 caret.setDot(dotPos);
                 return dotPos + 4;
             }
             return null;
         }
 
         @Override
         protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret, Object cookie) {
             if (cookie != null) {
                 if (cookie instanceof Integer) {
                     int newDot = (Integer) cookie;
                     // integer
                     int nowDotPos = caret.getDot();
                     try {
                         Indent.get(doc).reindent(newDot);
                     } catch (BadLocationException ex) {
                         log.severe("Excetion throw during InsertBreakAction. " + ex);
                     }
                     if (newDot > nowDotPos+1) {
                         caret.setDot(newDot);
                     } else {
                         caret.setDot(nowDotPos + 1);
                     }
                 }
             }
         }
 
     }
 
     public static class JavaDeleteCharAction extends ExtDeleteCharAction {
 
         public JavaDeleteCharAction(String nm, boolean nextChar) {
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
 
         public String getPopupMenuText(JTextComponent target) {
             return NbBundle.getBundle(JavaFXEditorKit.class).getString("goto_source_open_source_not_formatted"); //NOI18N
         }
 
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
 }
