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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 
 package org.netbeans.modules.javafx.editor.completion;
 
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.ReturnTree;
 import com.sun.javafx.api.tree.Scope;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.ThrowTree;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.*;
 import javax.lang.model.util.ElementFilter;
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.Position;
 import org.netbeans.api.editor.completion.Completion;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.*;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
 import org.netbeans.spi.editor.completion.CompletionItem;
 import org.netbeans.spi.editor.completion.CompletionTask;
 import org.netbeans.spi.editor.completion.support.CompletionUtilities;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.xml.XMLUtil;
 
 /**
  *
  * @author Dusan Balek
  */
 public abstract class JavaFXCompletionItem implements CompletionItem {
     
     private static final Logger logger = Logger.getLogger(JavaFXCompletionItem.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     public static final String COLOR_END = "</font>"; //NOI18N
     public static final String STRIKE = "<s>"; //NOI18N
     public static final String STRIKE_END = "</s>"; //NOI18N
     public static final String BOLD = "<b>"; //NOI18N
     public static final String BOLD_END = "</b>"; //NOI18N
 
     public int substitutionOffset;
     public String textToAdd;
     
     protected static int SMART_TYPE = 1000;
     private static final String GENERATE_TEXT = NbBundle.getMessage(JavaFXCompletionItem.class, "generate_Lbl");
 
     public static final JavaFXCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
         return new KeywordItem(kwd, 0, postfix, substitutionOffset, smartType);
     }
     
     public static final JavaFXCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean isDeprecated) {
         return new PackageItem(pkgFQN, substitutionOffset, isDeprecated);
     }
 
     public static final JavaFXCompletionItem createVariableItem(String varName, int substitutionOffset, String textToAdd, boolean smartType) {
         return new VariableItem(null, varName, substitutionOffset, textToAdd, smartType);
     }
     
     public static final JavaFXCompletionItem createVariableItem(String varName, int substitutionOffset, boolean smartType) {
         return new VariableItem(null, varName, substitutionOffset, smartType);
     }
     
     public static final JavaFXCompletionItem createExecutableItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean inImport, boolean smartType) {
         switch (elem.getKind()) {
             case METHOD:
                 return new MethodItem(elem, type, substitutionOffset, isInherited, isDeprecated, inImport, smartType);
             default:
                 throw new IllegalArgumentException("kind=" + elem.getKind());
         }
     }
     
     public static final JavaFXCompletionItem createTypeItem(TypeElement elem, DeclaredType type, int substitutionOffset, boolean isDeprecated, boolean insideNew, boolean smartType, boolean insertImport) {
         return new ClassItem(elem, type, 0, substitutionOffset, isDeprecated, insideNew, smartType, insertImport);
     }
 
     public static final JavaFXCompletionItem createTypeItem(String name,  int substitutionOffset, boolean isDeprecated, boolean insideNew, boolean smartType) {
         return new ClassItem(name, 0, substitutionOffset, isDeprecated, insideNew, smartType);
     }
 
     public static JavaFXCompletionItem createParametersItem(ExecutableElement a, ExecutableType b, int anchorOffset, boolean deprecated, int length, String name) {
         return new ParametersItem(a, b, anchorOffset, deprecated, length, name);
     }
 
     protected JavaFXCompletionItem(int substitutionOffset) {
         this.substitutionOffset = substitutionOffset;
     }
     
     protected JavaFXCompletionItem(int substitutionOffset, String textToAdd) {
         this.substitutionOffset = substitutionOffset;
         this.textToAdd = textToAdd;
     }
     
     public void defaultAction(JTextComponent component) {
         if (component != null) {
             Completion.get().hideDocumentation();
             Completion.get().hideCompletion();
             int caretOffset = component.getSelectionEnd();
             substituteText(component, substitutionOffset, caretOffset - substitutionOffset, textToAdd);
         }
     }
 
     public void processKeyEvent(KeyEvent evt) {
         if (evt.getID() == KeyEvent.KEY_TYPED) {
             switch (evt.getKeyChar()) {
                 case ':':
                 case ';':
                 case ',':
                 case '(':
                     Completion.get().hideDocumentation();
                     Completion.get().hideCompletion();
                     JTextComponent component = (JTextComponent)evt.getSource();
                     int caretOffset = component.getSelectionEnd();
                     substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                     evt.consume();
                     break;
                 case '.':
                     Completion.get().hideDocumentation();
                     component = (JTextComponent)evt.getSource();
                     caretOffset = component.getSelectionEnd();
                     substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                     evt.consume();
                     caretOffset = component.getSelectionEnd();
                     try {
                         if (caretOffset > 0 && !".".equals(component.getDocument().getText(caretOffset - 1, 1))) {
                             Completion.get().hideCompletion();
                             break;
                         }
                     } catch (BadLocationException ble) {}
                     Completion.get().showCompletion();
                     break;
             }
         }
     }
 
     public boolean instantSubstitution(JTextComponent component) {
         if (component != null) {
             try {
                 int caretOffset = component.getSelectionEnd();
                 if (caretOffset > substitutionOffset) {
                     String text = component.getDocument().getText(substitutionOffset, caretOffset - substitutionOffset);
                     if (!getInsertPrefix().toString().startsWith(text)) {
                         return false;
                     }
                 }
             }
             catch (BadLocationException ble) {}
         }
         defaultAction(component);
         return true;
     }
     
     public CompletionTask createDocumentationTask() {
         return null;
     }
     
     public CompletionTask createToolTipTask() {
         return null;
     }
     
     public int getPreferredWidth(Graphics g, Font defaultFont) {
         return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
     }
     
     public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
         CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
     }
 
     protected ImageIcon getIcon() {
         return null;
     }
     
     protected String getLeftHtmlText() {
         return null;
     }
     
     protected String getRightHtmlText() {
         return null;
     }
 
     protected void substituteText(JTextComponent c, final int offset, int len, String toAdd) {
         final BaseDocument doc = (BaseDocument)c.getDocument();
         CharSequence prefix = getInsertPrefix();
         if (prefix == null)
             return;
         final StringBuilder text = new StringBuilder(prefix);
         final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
         if (semiPos > -2)
             toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
         if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
             char ch;
             int i = 0;
             while(i < toAdd.length() && (ch = toAdd.charAt(i)) <= ' ' ) {
                 text.append(ch);
                 i++;
             }
             if (i > 0)
                 toAdd = toAdd.substring(i);
             TokenSequence<JFXTokenId> sequence = JavaFXCompletionProvider.getJavaFXTokenSequence(TokenHierarchy.get(doc), offset + len);
             if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                 text.append(toAdd);
                 toAdd = null;
             }
             boolean added = false;
             while(toAdd != null && toAdd.length() > 0) {
                 String tokenText = sequence.token().text().toString();
                 if (tokenText.startsWith(toAdd)) {
                     len = sequence.offset() - offset + toAdd.length();
                     text.append(toAdd);
                     toAdd = null;
                 } else if (toAdd.startsWith(tokenText)) {
                     sequence.moveNext();
                     len = sequence.offset() - offset;
                     text.append(toAdd.substring(0, tokenText.length()));
                     toAdd = toAdd.substring(tokenText.length());
                     added = true;
                 } else if (sequence.token().id() == JFXTokenId.WS && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                     if (!sequence.moveNext()) {
                         text.append(toAdd);
                         toAdd = null;
                     }
                 } else {
                     if (!added)
                         text.append(toAdd);
                     toAdd = null;
                 }
             }
         }
 
         // TODO this should be tested, if something will not work, comment this block
         try {
             String tx = doc.getText(0, doc.getLength());
             len = FXSourceUtils.getSubstitutionLenght(tx, offset, len);
         } catch (BadLocationException e) {
         }
 
         // Update the text
         final int length = len;
         doc.runAtomic (new Runnable () {
             public void run () {
                 try {
                     String textToReplace = doc.getText(offset, length);
                     if (textToReplace.contentEquals(text)) {
                         if (semiPos > -1)
                             doc.insertString(semiPos, ";", null); //NOI18N
                         return;
                     }
                     Position position = doc.createPosition(offset);
                     Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                     doc.remove(offset, length);
                     doc.insertString(position.getOffset(), text.toString(), null);
                     if (semiPosition != null)
                         doc.insertString(semiPosition.getOffset(), ";", null);
                 } catch (BadLocationException e) {
                     // Can't update
                 }
             }
         });
     }
             
     static class KeywordItem extends JavaFXCompletionItem {
         
         private static final String JAVA_KEYWORD = "org/netbeans/modules/java/editor/resources/javakw_16.png"; //NOI18N
         private static final String KEYWORD_COLOR = "<font color=#000099>"; //NOI18N
         private static ImageIcon icon;
         
         private String kwd;
         private int dim;
         private String postfix;
         private boolean smartType;
         private String leftText;
 
         private KeywordItem(String kwd, int dim, String postfix, int substitutionOffset, boolean smartType) {
             super(substitutionOffset);
             this.kwd = kwd;
             this.dim = dim;
             this.postfix = postfix;
             this.smartType = smartType;
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof KeywordItem)) {
                 return false;
             }
             KeywordItem ki = (KeywordItem) obj;
             return ki.kwd.equals(this.kwd) && ki.postfix.equals(this.postfix);
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 83 * hash + (this.kwd != null ? this.kwd.hashCode() : 0);
             hash = 83 * hash + (this.postfix != null ? this.postfix.hashCode() : 0);
             return hash;
         }
 
         public int getSortPriority() {
             return smartType ? 600 - SMART_TYPE : 600;
         }
         
         public CharSequence getSortText() {
             return kwd;
         }
         
         public CharSequence getInsertPrefix() {
             return kwd;
         }
         
         @Override
         protected ImageIcon getIcon(){
             if (icon == null) icon = new ImageIcon(ImageUtilities.loadImage(JAVA_KEYWORD));
             return icon;            
         }
         
         @Override
         protected String getLeftHtmlText() {
             if (leftText == null) {
                 StringBuilder sb = new StringBuilder();
                 sb.append(KEYWORD_COLOR);
                 sb.append(BOLD);
                 sb.append(kwd);
                 for(int i = 0; i < dim; i++)
                     sb.append("[]"); //NOI18N
                 sb.append(BOLD_END);
                 sb.append(COLOR_END);
                 leftText = sb.toString();
             }
             return leftText;
         }
         
         @Override
         protected void substituteText(JTextComponent c, final int offset, int len, String toAdd) {
             if (dim == 0) {
                 super.substituteText(c, offset, len, toAdd != null ? toAdd : postfix);
                 return;
             }
             final BaseDocument doc = (BaseDocument)c.getDocument();
             final StringBuilder text = new StringBuilder();
             final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
             if (semiPos > -2)
                 toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
             if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                 TokenSequence<JFXTokenId> sequence = JavaFXCompletionProvider.getJavaFXTokenSequence(TokenHierarchy.get(doc), offset + len);
                 if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                     text.append(toAdd);
                     toAdd = null;
                 }
                 boolean added = false;
                 while(toAdd != null && toAdd.length() > 0) {
                     String tokenText = sequence.token().text().toString();
                     if (tokenText.startsWith(toAdd)) {
                         len = sequence.offset() - offset + toAdd.length();
                         text.append(toAdd);
                         toAdd = null;
                     } else if (toAdd.startsWith(tokenText)) {
                         sequence.moveNext();
                         len = sequence.offset() - offset;
                         text.append(toAdd.substring(0, tokenText.length()));
                         toAdd = toAdd.substring(tokenText.length());
                         added = true;
                     } else if (sequence.token().id() == JFXTokenId.WS && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                         if (!sequence.moveNext()) {
                             text.append(toAdd);
                             toAdd = null;
                         }
                     } else {
                         if (!added)
                             text.append(toAdd);
                         toAdd = null;
                     }
                 }
             }
             StringBuilder sb = new StringBuilder();
             int cnt = 1;
             sb.append(kwd);
             for(int i = 0; i < dim; i++) {
                 sb.append("[${PAR"); //NOI18N
                 sb.append(cnt++);
                 sb.append(" instanceof=\"int\" default=\"\"}]"); //NOI18N                
             }
 
             // TODO this should be tested, if something will not work, comment this block
             try {
                 String tx = doc.getText(0, doc.getLength());
                 len = FXSourceUtils.getSubstitutionLenght(tx, offset, len);
             } catch (BadLocationException e) {
             }
 
             final int length = len;
             doc.runAtomic (new Runnable () {
                 public void run () {
                     try {
                         Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                         if (length > 0)
                             doc.remove(offset, length);
                         if (semiPosition != null)
                             doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                     } catch (BadLocationException e) {
                         // Can't update
                     }
                 }
             });
             CodeTemplateManager ctm = CodeTemplateManager.get(doc);
             if (ctm != null) {
                 ctm.createTemporary(sb.append(text).toString()).insert(c);
             }
         }
     
         @Override
         public String toString() {
             return kwd;
         }        
     }
     
     static class PackageItem extends JavaFXCompletionItem {
         
         private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
         private static final String PACKAGE_COLOR = "<font color=#005600>"; //NOI18N
         private static ImageIcon icon;
         
         private boolean isDeprecated;
         private String simpleName;
         private String sortText;
         private String leftText;
 
         private PackageItem(String pkgFQN, int substitutionOffset, boolean isDeprecated) {
             super(substitutionOffset);
             this.isDeprecated = isDeprecated;
             int idx = pkgFQN.lastIndexOf('.');
             this.simpleName = idx < 0 ? pkgFQN : pkgFQN.substring(idx + 1);
             this.sortText = this.simpleName + "#" + pkgFQN; //NOI18N
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof PackageItem)) {
                 return false;
             }
             PackageItem pi = (PackageItem) obj;
             return pi.simpleName.equals(this.simpleName) && pi.sortText.equals(this.sortText);
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 67 * hash + (this.simpleName != null ? this.simpleName.hashCode() : 0);
             hash = 67 * hash + (this.sortText != null ? this.sortText.hashCode() : 0);
             return hash;
         }
 
         public int getSortPriority() {
             return 900;
         }
         
         public CharSequence getSortText() {
             return sortText;
         }
         
         public CharSequence getInsertPrefix() {
             return simpleName;
         }
         
         @Override
         protected ImageIcon getIcon(){
             if (icon == null) icon = new ImageIcon(ImageUtilities.loadImage(PACKAGE));
             return icon;            
         }
         
         @Override
         protected String getLeftHtmlText() {
             if (leftText == null) {
                 StringBuilder sb = new StringBuilder();
                 sb.append(PACKAGE_COLOR);
                 if (isDeprecated)
                     sb.append(STRIKE);
                 sb.append(simpleName);
                 if (isDeprecated)
                     sb.append(STRIKE_END);
                 sb.append(COLOR_END);
                 leftText = sb.toString();
             }
             return leftText;
         }
         
         @Override
         public String toString() {
             return simpleName;
         }        
     }
 
     static class VariableItem extends JavaFXCompletionItem {
         
         private static final String LOCAL_VARIABLE = "org/netbeans/modules/editor/resources/completion/localVariable.gif"; //NOI18N
         private static final String PARAMETER_COLOR = "<font color=#00007c>"; //NOI18N
         private static ImageIcon icon;
 
         private String varName;
         private boolean smartType;
         private String typeName;
         private String leftText;
         private String rightText;
         
         private VariableItem(TypeMirror type, String varName, int substitutionOffset, boolean smartType) {
             super(substitutionOffset);
             this.varName = varName;
             this.smartType = smartType;
             this.typeName = type != null ? type.toString() : null;
         }
 
         private VariableItem(TypeMirror type, String varName, int substitutionOffset, String textToAdd, boolean smartType) {
             super(substitutionOffset, textToAdd);
             this.varName = varName;
             this.smartType = smartType;
             this.typeName = type != null ? type.toString() : null;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof VariableItem)) {
                 return false;
             }
             VariableItem vi = (VariableItem) obj;
             boolean eNames = vi.varName.equals(this.varName);
 //            boolean eTypes = false;
 //            if (vi.typeName != null && this.typeName != null) {
 //                eTypes = vi.typeName.equals(this.typeName);
 //            }
 //            return eNames && eTypes;
             return eNames;
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 97 * hash + (this.varName != null ? this.varName.hashCode() : 0);
 //            hash = 97 * hash + (this.typeName != null ? this.typeName.hashCode() : 0);
             return hash;
         }
 
         public int getSortPriority() {
             return smartType ? 200 - SMART_TYPE : 200;
         }
         
         public CharSequence getSortText() {
             return varName;
         }
         
         public CharSequence getInsertPrefix() {
             return varName;
         }
 
         @Override
         protected String getLeftHtmlText() {
             if (leftText == null)
                 leftText = PARAMETER_COLOR + BOLD + varName + BOLD_END + COLOR_END;
             return leftText;
         }
         
         @Override
         protected String getRightHtmlText() {
             if (rightText == null)
                 rightText = escape(typeName);
             return rightText;
         }
         
         @Override
         protected ImageIcon getIcon(){
             if (icon == null) icon = new ImageIcon(ImageUtilities.loadImage(LOCAL_VARIABLE));
             return icon;            
         }
 
         @Override
         public String toString() {
             return (typeName != null ? typeName + " " : "") + varName; //NOI18N
         }
    }
         static class MethodItem extends JavaFXCompletionItem {
         
         private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
         private static final String METHOD_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_protected_16.png"; //NOI18N
         private static final String METHOD_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_package_private_16.png"; //NOI18N
         private static final String METHOD_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_private_16.png"; //NOI18N        
         private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png"; //NOI18N
         private static final String METHOD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_static_protected_16.png"; //NOI18N
         private static final String METHOD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_static_private_16.png"; //NOI18N
         private static final String METHOD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_static_package_private_16.png"; //NOI18N
         private static final String METHOD_COLOR = "<font color=#000000>"; //NOI18N
         private static final String PARAMETER_NAME_COLOR = "<font color=#a06001>"; //NOI18N
         private static ImageIcon icon[][] = new ImageIcon[2][4];
 
         private boolean isInherited;
         private boolean isDeprecated;
         private boolean inImport;
         private boolean smartType;
         private String simpleName;
         protected Set<Modifier> modifiers;
         private List<ParamDesc> params;
         private String typeName;
         private boolean isPrimitive;
         private String sortText;
         private String leftText;
         private String rightText;
         protected ElementHandle<ExecutableElement> elementHandle;
         
         private MethodItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean inImport, boolean smartType) {
             super(substitutionOffset);
             this.elementHandle = ElementHandle.create(elem);
             this.isInherited = isInherited;
             this.isDeprecated = isDeprecated;
             this.inImport = inImport;
             this.smartType = smartType;
             this.simpleName = elem.getSimpleName().toString();
             this.modifiers = elem.getModifiers();
             this.params = new ArrayList<ParamDesc>();
             Iterator<? extends VariableElement> it = elem.getParameters().iterator();
             Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
             while(it.hasNext() && tIt.hasNext()) {
                 TypeMirror tm = tIt.next();
                 this.params.add(new ParamDesc(tm.toString(), tm.toString(), it.next().getSimpleName().toString()));
             }
             TypeMirror retType = type.getReturnType();
             this.typeName = retType.toString();
             this.isPrimitive = retType.getKind().isPrimitive() || retType.getKind() == TypeKind.VOID;
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof MethodItem)) {
                 return false;
             }
             MethodItem mi = (MethodItem) obj;
             return mi.simpleName.equals(this.simpleName) && mi.modifiers.equals(this.modifiers) && mi.params.equals(this.params) && mi.typeName.equals(this.typeName);
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 53 * hash + (this.simpleName != null ? this.simpleName.hashCode() : 0);
             hash = 53 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
             hash = 53 * hash + (this.params != null ? this.params.hashCode() : 0);
             hash = 53 * hash + (this.typeName != null ? this.typeName.hashCode() : 0);
             return hash;
         }
 
         public int getSortPriority() {
             return smartType ? 500 - SMART_TYPE : 500;
         }
         
         public CharSequence getSortText() {
             if (sortText == null) {
                 StringBuilder sortParams = new StringBuilder();
                 sortParams.append('(');
                 int cnt = 0;
                 for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                     ParamDesc param = it.next();
                     sortParams.append(param.typeName);
                     if (it.hasNext()) {
                         sortParams.append(',');
                     }
                     cnt++;
                 }
                 sortParams.append(')');
                 sortText = simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
             }
             return sortText;
         }
         
         public CharSequence getInsertPrefix() {
             return simpleName;
         }
         
         @Override
         protected String getLeftHtmlText() {
             if (leftText == null) {
                 StringBuilder lText = new StringBuilder();
                 lText.append(METHOD_COLOR);
                 if (!isInherited)
                     lText.append(BOLD);
                 if (isDeprecated)
                     lText.append(STRIKE);
                 lText.append(simpleName);
                 if (isDeprecated)
                     lText.append(STRIKE_END);
                 if (!isInherited)
                     lText.append(BOLD_END);
                 lText.append(COLOR_END);
                 lText.append('(');
                 for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                     ParamDesc paramDesc = it.next();
                     lText.append(paramDesc.name);
                     lText.append(" : ");
                     lText.append(PARAMETER_NAME_COLOR);
                     lText.append(escape(paramDesc.typeName));
                     lText.append(COLOR_END);
                     if (it.hasNext()) {
                         lText.append(", "); //NOI18N
                     }
                 }
                 lText.append(')');
                 return lText.toString();
             }
             return leftText;
         }
         
         @Override
         protected String getRightHtmlText() {
             if (rightText == null)
                 rightText = escape(typeName);
             return rightText;
         }
         
         @Override
         public CompletionTask createDocumentationTask() {
             return JavaFXCompletionProvider.createDocTask(elementHandle);
         }
 
         @Override
         protected ImageIcon getIcon() {
             int level = getProtectionLevel(modifiers);
             boolean isStatic = modifiers.contains(Modifier.STATIC);
             ImageIcon cachedIcon = icon[isStatic?1:0][level];
             if (cachedIcon != null)
                 return cachedIcon;
             
             String iconPath = METHOD_PUBLIC;            
             if (isStatic) {
                 switch (level) {
                     case PRIVATE_LEVEL:
                         iconPath = METHOD_ST_PRIVATE;
                         break;
 
                     case PACKAGE_LEVEL:
                         iconPath = METHOD_ST_PACKAGE;
                         break;
 
                     case PROTECTED_LEVEL:
                         iconPath = METHOD_ST_PROTECTED;
                         break;
 
                     case PUBLIC_LEVEL:
                         iconPath = METHOD_ST_PUBLIC;
                         break;
                 }
             }else{
                 switch (level) {
                     case PRIVATE_LEVEL:
                         iconPath = METHOD_PRIVATE;
                         break;
 
                     case PACKAGE_LEVEL:
                         iconPath = METHOD_PACKAGE;
                         break;
 
                     case PROTECTED_LEVEL:
                         iconPath = METHOD_PROTECTED;
                         break;
 
                     case PUBLIC_LEVEL:
                         iconPath = METHOD_PUBLIC;
                         break;
                 }
             }
             ImageIcon newIcon = new ImageIcon(ImageUtilities.loadImage(iconPath));
             icon[isStatic?1:0][level] = newIcon;
             return newIcon;            
         }
         
         @Override
         protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
             if (toAdd == null) {
                 if (isPrimitive) {
                     try {
                         final String[] ret = new String[1];
                         JavaFXSource js = JavaFXSource.forDocument(c.getDocument());
                         js.runUserActionTask(new Task<CompilationController>() {
 
                             public void run(CompilationController controller) throws Exception {
                                 controller.toPhase(Phase.PARSED);
                                 JavaFXTreePath tp = controller.getTreeUtilities().pathFor(c.getSelectionEnd());
                                 Tree tree = tp.getLeaf();
                                 if (tree.getJavaFXKind() == Tree.JavaFXKind.IDENTIFIER /*|| tree.getJavaFXKind() == Tree.JavaFXKind.PRIMITIVE_TYPE*/)
                                     tp = tp.getParentPath();
                                 if (tp.getLeaf().getJavaFXKind() == Tree.JavaFXKind.MEMBER_SELECT ||
                                     (tp.getLeaf().getJavaFXKind() == Tree.JavaFXKind.METHOD_INVOCATION && ((FunctionInvocationTree)tp.getLeaf()).getMethodSelect() == tree))
                                     tp = tp.getParentPath();
                                 if (/*tp.getLeaf().getJavaFXKind() == Tree.JavaFXKind.EXPRESSION_STATEMENT ||*/ tp.getLeaf().getJavaFXKind() == Tree.JavaFXKind.BLOCK_EXPRESSION)
                                     ret[0] = ";"; //NOI18N
                             }
                         }, true);
                         toAdd = ret[0];
                     } catch (IOException ex) {
                     }
                 }
             }
             if (inImport || params.isEmpty()) {
                 String add = "()"; //NOI18N
                 if (toAdd != null && !add.startsWith(toAdd))
                     add += toAdd;
                 super.substituteText(c, offset, len, add);
                 if ("(".equals(toAdd)) //NOI18N
                     c.setCaretPosition(c.getCaretPosition() - 1);
             } else {
                 String add = "()"; //NOI18N
                 if (toAdd != null && !add.startsWith(toAdd))
                     add += toAdd;
                 final BaseDocument doc = (BaseDocument)c.getDocument();
                 String text = ""; //NOI18N
                 final int semiPos = add.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
                 if (semiPos > -2)
                     add = add.length() > 1 ? add.substring(0, add.length() - 1) : null;
                 JavaFXSource js = JavaFXSource.forDocument(c.getDocument());
                 TokenSequence<JFXTokenId> sequence = ((TokenHierarchy<?>)js.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
                 sequence = sequence.subSequence(offset + len);
                 if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                     text += add;
                     add = null;
                 }
                 boolean added = false;
                 while(add != null && add.length() > 0) {
                     String tokenText = sequence.token().text().toString();
                     if (tokenText.startsWith(add)) {
                         len = sequence.offset() - offset + add.length();
                         text += add;
                         add = null;
                     } else if (add.startsWith(tokenText)) {
                         sequence.moveNext();
                         len = sequence.offset() - offset;
                         text += add.substring(0, tokenText.length());
                         add = add.substring(tokenText.length());
                         added = true;
                     } else if (sequence.token().id() == JFXTokenId.WS && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                         if (!sequence.moveNext()) {
                             text += add;
                             add = null;
                         }
                     } else {
                         if (!added)
                             text += add;
                         add = null;
                     }
                 }
 
                 // TODO this should be tested, if something will not work, comment this block
                 try {
                     String tx = doc.getText(0, doc.getLength());
                     len = FXSourceUtils.getSubstitutionLenght(tx, offset, len);
                 } catch (BadLocationException e) {
                 }
 
                 final int length = len;
                 doc.runAtomic (new Runnable () {
                     public void run () {
                         try {
                             Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                             if (length > 0)
                                 doc.remove(offset, length);
                             doc.insertString(offset, getInsertPrefix().toString(), null);
                             if (semiPosition != null)
                                 doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                         } catch (BadLocationException e) {
                             // Can't update
                         }
                     }
                 });
 
                 CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                 if (ctm != null) {
                     StringBuilder sb = new StringBuilder();
                     sb.append('('); //NOI18N
                     if (text.length() > 1) {
                         for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                             ParamDesc paramDesc = it.next();
                             sb.append("${"); //NOI18N
                             sb.append(paramDesc.name);
                             sb.append('}'); //NOI18N
                             if (it.hasNext())
                                 sb.append(", "); //NOI18N
                         }
                         sb.append(')');//NOI18N
                         if (text.length() > 2)
                             sb.append(text.substring(2));
                     }
                     ctm.createTemporary(sb.toString()).insert(c);
                     Completion.get().showToolTip();
                 }
             }
         }        
 
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder();
             for (Modifier mod : modifiers) {
                 sb.append(mod.toString());
                 sb.append(' ');
             }
             sb.append(typeName);
             sb.append(' ');
             sb.append(simpleName);
             sb.append('(');
             for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                 ParamDesc paramDesc = it.next();
                 sb.append(paramDesc.name);
                 sb.append(" : ");
                 sb.append(paramDesc.typeName);
                 if (it.hasNext()) {
                     sb.append(", "); //NOI18N
                 }
             }
             sb.append(')');
             return sb.toString();
         }   
     }    
     static class ClassItem extends JavaFXCompletionItem {
         
         private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
         private static final String CLASS_COLOR = "<font color=#560000>"; //NOI18N
         private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
         private static ImageIcon icon;
         
         private int dim;
         private boolean isDeprecated;
         private boolean insideNew;
         private boolean smartType;
         private String simpleName;
         private String typeName;
         private CharSequence sortText;
         private String leftText;
         private DeclaredType type;
         private TypeElement elem;
         private boolean insertImport;
 
         private ClassItem(String name, int dim, int substitutionOffset, boolean isDeprecated, boolean insideNew, boolean smartType) {
             super(substitutionOffset);
             this.dim = dim;
             this.isDeprecated = isDeprecated;
             this.insideNew = insideNew;
             this.smartType = smartType;
             this.simpleName = name;
             this.typeName = name;
             this.sortText = this.simpleName;
         }
         
         private ClassItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean isDeprecated, boolean insideNew, boolean smartType, boolean insertImport) {
             super(substitutionOffset);
             this.dim = dim;
             this.elem = elem;
             this.isDeprecated = isDeprecated;
             this.insideNew = insideNew;
             this.smartType = smartType;
             this.simpleName = elem.getSimpleName().toString();
             if (type != null) {
                 TypeElement te = (TypeElement)type.asElement();
                 this.typeName = te.getQualifiedName().toString();
             }
             this.type = type;
             this.sortText = this.simpleName;
             this.insertImport = insertImport;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof ClassItem)) {
                 return false;
             }
             ClassItem ci = (ClassItem) obj;
 //            return ci.simpleName.equals(this.simpleName) && ci.type.equals(this.type);
             return ci.simpleName.equals(this.simpleName);
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 53 * hash + (this.simpleName != null ? this.simpleName.hashCode() : 0);
 //            hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
             return hash;
         }
         
         public int getSortPriority() {
             return smartType ? 800 - SMART_TYPE : 800;
         }
         
         public CharSequence getSortText() {
             return sortText;
         }
         
         public CharSequence getInsertPrefix() {
             return simpleName;
         }
 
         @Override
         public boolean instantSubstitution(JTextComponent component) {
             return false;
         }
     
         @Override
         public CompletionTask createDocumentationTask() {
 //            return typeHandle.getKind() == TypeKind.DECLARED ? JavaCompletionProvider.createDocTask(ElementHandle.from(typeHandle)) : null;
            if (type == null) {
                return null;
            }
             return JavaFXCompletionProvider.createDocTask(ElementHandle.create(type.asElement()));
         }
 
         @Override
         protected ImageIcon getIcon(){
             if (icon == null) icon = new ImageIcon(ImageUtilities.loadImage(CLASS));
             return icon;            
         }
 
         @Override
         protected String getLeftHtmlText() {
             if (leftText == null) {
                 StringBuilder sb = new StringBuilder();
                 sb.append(getColor());
                 if (isDeprecated)
                     sb.append(STRIKE);
                 sb.append(escape(typeName));
                 for(int i = 0; i < dim; i++)
                     sb.append("[]"); //NOI18N
                 if (isDeprecated)
                     sb.append(STRIKE_END);
                 sb.append(COLOR_END);
                 leftText = sb.toString();
             }
             return leftText;
         }
         
         protected String getColor() {
             return CLASS_COLOR;
         }
 
         @Override
         protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
             final BaseDocument doc = (BaseDocument)c.getDocument();
             final StringBuilder text = new StringBuilder();
             final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
             if (semiPos > -2)
                 toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
             if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                 JavaFXSource js = JavaFXSource.forDocument(c.getDocument());
                 TokenSequence<JFXTokenId> sequence = ((TokenHierarchy<?>)js.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
                 sequence = sequence.subSequence(offset + len);
                 if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                     text.append(toAdd);
                     toAdd = null;
                 }
                 boolean added = false;
                 while(toAdd != null && toAdd.length() > 0) {
                     String tokenText = sequence.token().text().toString();
                     if (tokenText.startsWith(toAdd)) {
                         len = sequence.offset() - offset + toAdd.length();
                         text.append(toAdd);
                         toAdd = null;
                     } else if (toAdd.startsWith(tokenText)) {
                         sequence.moveNext();
                         len = sequence.offset() - offset;
                         text.append(toAdd.substring(0, tokenText.length()));
                         toAdd = toAdd.substring(tokenText.length());
                         added = true;
                     } else if (sequence.token().id() == JFXTokenId.WS && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                         if (!sequence.moveNext()) {
                             text.append(toAdd);
                             toAdd = null;
                         }
                     } else {
                         if (!added)
                             text.append(toAdd);
                         toAdd = null;
                     }
                 }
             }
             final int finalLen = len;
             JavaFXSource js = JavaFXSource.forDocument(doc);
             try {
                 js.runUserActionTask(new Task<CompilationController>() {
 
                     public void run(final CompilationController controller) throws IOException {
                         if (controller.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) {
                             if (LOGGABLE) log ("Cannot show code completion due to compiler exception - should be already logged.");
                             return;
                         }
                         final TypeElement eleme = (type != null) ? (TypeElement)type.asElement() : elem;
                         boolean asTemplate = false;
                         StringBuilder sb = new StringBuilder();
                         int cnt = 1;
                         sb.append("${PAR"); //NOI18N
                         sb.append(cnt++);
                         
                         sb.append(" default=\""); //NOI18N
                         if (eleme != null) {
                             sb.append(eleme.getQualifiedName());
                         } else {
                             sb.append(simpleName);
                         }
                             
                         sb.append("\" editable=false}"); //NOI18N
                         for(int i = 0; i < dim; i++) {
                             sb.append("[${PAR"); //NOI18N
                             sb.append(cnt++);
                             sb.append(" instanceof=\"int\" default=\"\"}]"); //NOI18N
                             asTemplate = true;
                         }
 
                         int finalLen2 = finalLen;
                         try {
                             String tx = doc.getText(0, doc.getLength());
                             finalLen2 = FXSourceUtils.getSubstitutionLenght(tx, offset, finalLen);
                         } catch (BadLocationException e) {
                         }
                         
                         if (asTemplate) {
                             if (insideNew)
                                 sb.append("${cursor completionInvoke}"); //NOI18N
                             if (finalLen2 > 0) {
                                 final int finalLen3 = finalLen2;
                                 doc.runAtomic (new Runnable () {
                                     public void run () {
                                         try {
                                             doc.remove(offset, finalLen3);
                                         } catch (BadLocationException e) {
                                             // Can't update
                                         }
                                     }
                                 });
                             }
                             CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                             if (ctm != null)
                                 ctm.createTemporary(sb.append(text).toString()).insert(c);
                         } else {
                             // Update the text
                             final int finalLen3 = finalLen2;
                             doc.runAtomic (new Runnable () {
                                 public void run () {
                                     try {
                                         Position semiPosition = semiPos > -1 && !insideNew ? doc.createPosition(semiPos) : null;
                                         JavaFXTreePath tp = controller.getTreeUtilities().pathFor(offset);
                                         CharSequence cs = simpleName;
                                         if (eleme != null) {
                                             cs = eleme.getSimpleName();
                                             if (eleme.getEnclosingElement().getKind() == ElementKind.CLASS) {
                                                 cs = eleme.getEnclosingElement().getSimpleName() + "." + eleme.getSimpleName();
                                             }
                                         }
                                         if (!insideNew)
                                             cs = text.insert(0, cs);
                                         String textToReplace = doc.getText(offset, finalLen3);
                                         if (textToReplace.contentEquals(cs)) {
                                             if (insertImport && eleme != null) {
                                                 Imports.addImport(c, eleme.getQualifiedName().toString());
                                             }
                                             return;
                                         }
                                         doc.remove(offset, finalLen3);
                                         doc.insertString(offset, cs.toString(), null);
                                         if (semiPosition != null) {
                                             doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                                         }
                                         if (insertImport && eleme != null) {
                                             Imports.addImport(c, eleme.getQualifiedName().toString());
                                         }
                                     } catch (BadLocationException e) {
                                         // Can't update
                                     }
                                 }
                             });
                             if (insideNew && type != null && type.getKind() == TypeKind.DECLARED) {
                                 ExecutableElement ctor = null;
                                 JavafxcTrees trees = controller.getTrees();
                                 Scope scope = controller.getTreeUtilities().scopeFor(offset);
                                 int val = 0; // no constructors seen yet
                                 if (eleme != null) {
                                     for (ExecutableElement ee : ElementFilter.constructorsIn(eleme.getEnclosedElements())) {
                                         if (trees.isAccessible(scope, ee, type)) {
                                             if (ctor != null) {
                                                 val = 2; // more than one accessible constructors seen
                                                 break;
                                             }
                                             ctor = ee;
                                         }
                                         val = 1; // constructor seen
                                     }
                                 }
                                 if (val != 1 || ctor != null) {
                                     final JavaFXCompletionItem item = null;
                                     try {
                                         final Position offPosition = doc.createPosition(offset);
                                         SwingUtilities.invokeLater(new Runnable() {
                                             public void run() {
                                                 if (item != null) {
                                                     item.substituteText(c, offPosition.getOffset(), c.getSelectionEnd() - offPosition.getOffset(), text.toString());
                                                 } else {
                                                     //Temporary ugly solution
                                                     SwingUtilities.invokeLater(new Runnable() {
                                                         public void run() {
                                                             Completion.get().showCompletion();
                                                         }
                                                     });
                                                 }
                                             }
                                         });
                                     }
                                     catch (BadLocationException e) {}
                                 }
                             }
                         }
                     }
                 }, true);
             } catch (IOException ioe) {                
             }
         }
         
         @Override
         public String toString() {
             return simpleName;
         }        
     }
     static class ParametersItem extends JavaFXCompletionItem {
 
         private static final String PARAMETERS_COLOR = "<font color=#808080>"; //NOI18N
         private static final String ACTIVE_PARAMETER_COLOR = "<font color=#000000>"; //NOI18N
 
         protected ElementHandle<ExecutableElement> elementHandle;
         private boolean isDeprecated;
         private int activeParamsIndex;
         private String simpleName;
         private ArrayList<ParamDesc> params;
         private String typeName;
         private String sortText;
         private String leftText;
         private String rightText;
 
         private ParametersItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamsIndex, String name) {
             super(substitutionOffset);
             this.elementHandle = ElementHandle.create(elem);
             this.isDeprecated = isDeprecated;
             this.activeParamsIndex = activeParamsIndex;
             this.simpleName = name != null ? name : elem.getKind() == ElementKind.CONSTRUCTOR ? elem.getEnclosingElement().getSimpleName().toString() : elem.getSimpleName().toString();
             this.params = new ArrayList<ParamDesc>();
             Iterator<? extends VariableElement> it = elem.getParameters().iterator();
             Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
             while(it.hasNext() && tIt.hasNext()) {
                 TypeMirror tm = tIt.next();
                 this.params.add(new ParamDesc(tm.toString(), tm.toString(), it.next().getSimpleName().toString()));
             }
             TypeMirror retType = type.getReturnType();
             this.typeName = retType.toString();
         }
 
         public int getSortPriority() {
             return 100 - SMART_TYPE;
         }
 
         public CharSequence getSortText() {
             if (sortText == null) {
                 StringBuilder sortParams = new StringBuilder();
                 sortParams.append('(');
                 int cnt = 0;
                 for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                     ParamDesc param = it.next();
                     sortParams.append(param.typeName);
                     if (it.hasNext()) {
                         sortParams.append(',');
                     }
                     cnt++;
                 }
                 sortParams.append(')');
                 sortText = "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
             }
             return sortText;
         }
 
         public CharSequence getInsertPrefix() {
             return ""; //NOI18N
         }
 
         @Override
         protected String getLeftHtmlText() {
             if (leftText == null) {
                 StringBuilder lText = new StringBuilder();
                 lText.append(PARAMETERS_COLOR);
                 if (isDeprecated)
                     lText.append(STRIKE);
                 lText.append(simpleName);
                 if (isDeprecated)
                     lText.append(STRIKE_END);
                 lText.append('(');
                 for (int i = 0; i < params.size(); i++) {
                     ParamDesc paramDesc = params.get(i);
                     if (i == activeParamsIndex)
                         lText.append(COLOR_END).append(ACTIVE_PARAMETER_COLOR).append(BOLD);
                     lText.append(paramDesc.name);
                     lText.append(" : ");
                     lText.append(escape(paramDesc.typeName));
                     if (i < params.size() - 1)
                         lText.append(", "); //NOI18N
                     else
                         lText.append(BOLD_END).append(COLOR_END).append(PARAMETERS_COLOR);
                 }
                 lText.append(')');
                 lText.append(COLOR_END);
                 return lText.toString();
             }
             return leftText;
         }
 
         @Override
         protected String getRightHtmlText() {
             if (rightText == null)
                 rightText = PARAMETERS_COLOR + escape(typeName) + COLOR_END;
             return rightText;
         }
 
         @Override
         public CompletionTask createDocumentationTask() {
             return JavaFXCompletionProvider.createDocTask(elementHandle);
         }
 
         @Override
         public boolean instantSubstitution(JTextComponent component) {
             return false;
         }
 
         @Override
         protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
             String add = ")"; //NOI18N
             if (toAdd != null && !add.startsWith(toAdd))
                 add += toAdd;
             if (params.isEmpty()) {
                 super.substituteText(c, offset, len, add);
             } else {
                 final BaseDocument doc = (BaseDocument)c.getDocument();
                 String text = ""; //NOI18N
                 final int semiPos = add.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
                 if (semiPos > -2)
                     add = add.length() > 1 ? add.substring(0, add.length() - 1) : null;
                 TokenSequence<JFXTokenId> sequence = JavaFXCompletionProvider.getJavaFXTokenSequence(TokenHierarchy.get(doc), offset + len);
                 if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                     text += add;
                     add = null;
                 }
                 boolean added = false;
                 while(add != null && add.length() > 0) {
                     String tokenText = sequence.token().text().toString();
                     if (tokenText.startsWith(add)) {
                         len = sequence.offset() - offset + add.length();
                         text += add;
                         add = null;
                     } else if (add.startsWith(tokenText)) {
                         sequence.moveNext();
                         len = sequence.offset() - offset;
                         text += add.substring(0, tokenText.length());
                         add = add.substring(tokenText.length());
                         added = true;
                     } else if (sequence.token().id() == JFXTokenId.WS && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                         if (!sequence.moveNext()) {
                             text += add;
                             add = null;
                         }
                     } else {
                         if (!added)
                             text += add;
                         add = null;
                     }
                 }
                 final int length = len;
                 doc.runAtomic (new Runnable () {
                     public void run () {
                         try {
                             Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                             if (length > 0)
                                 doc.remove(offset, length);
                             if (semiPosition != null)
                                 doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                         } catch (BadLocationException e) {
                             // Can't update
                         }
                     }
                 });
                 CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                 if (ctm != null) {
                     StringBuilder sb = new StringBuilder();
                     boolean guessArgs = false; //Utilities.guessMethodArguments();
                     for (int i = activeParamsIndex; i < params.size(); i++) {
                         ParamDesc paramDesc = params.get(i);
                         sb.append("${"); //NOI18N
                         sb.append(paramDesc.name);
                         if (guessArgs) {
                             sb.append(" named instanceof="); //NOI18N
                             sb.append(paramDesc.fullTypeName);
                         }
                         sb.append("}"); //NOI18N
                         if (i < params.size() - 1)
                             sb.append(", "); //NOI18N
                     }
                     if (text.length() > 0)
                         sb.append(text);
                     ctm.createTemporary(sb.toString()).insert(c);
                     Completion.get().showToolTip();
                 }
             }
         }
 
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder();
             sb.append(typeName);
             sb.append(' ');
             sb.append(simpleName);
             sb.append('(');
             for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                 ParamDesc paramDesc = it.next();
                 sb.append(paramDesc.name);
                 sb.append(" : ");
                 sb.append(paramDesc.typeName);
                 if (it.hasNext()) {
                     sb.append(", "); //NOI18N
                 }
             }
             sb.append(") - parameters"); //NOI18N
             return sb.toString();
         }
     }
 
     private static final int PUBLIC_LEVEL = 3;
     private static final int PROTECTED_LEVEL = 2;
     private static final int PACKAGE_LEVEL = 1;
     private static final int PRIVATE_LEVEL = 0;
     
     private static int getProtectionLevel(Set<Modifier> modifiers) {
         if(modifiers.contains(Modifier.PUBLIC))
             return PUBLIC_LEVEL;
         if(modifiers.contains(Modifier.PROTECTED))
             return PROTECTED_LEVEL;
         if(modifiers.contains(Modifier.PRIVATE))
             return PRIVATE_LEVEL;
         return PACKAGE_LEVEL;
     }
     
     private static String escape(String s) {
         if (s != null) {
             try {
                 return XMLUtil.toAttributeValue(s);
             } catch (Exception ex) {}
         }
         return s;
     }
     
     private static int findPositionForSemicolon(JTextComponent c) {
         final int[] ret = new int[] {-2};
         final int offset = c.getSelectionEnd();
         try {
             JavaFXSource js = JavaFXSource.forDocument(c.getDocument());
             js.runUserActionTask(new Task<CompilationController>() {
 
                 public void run(CompilationController controller) throws Exception {
                     controller.toPhase(JavaFXSource.Phase.PARSED);
                     Tree t = null;
                     JavaFXTreePath tp = controller.getTreeUtilities().pathFor(offset);
                     while (t == null && tp != null) {
                         switch(tp.getLeaf().getJavaFXKind()) {
 /*                            case EXPRESSION_STATEMENT:
                                 ExpressionTree expr = ((ExpressionStatementTree)tp.getLeaf()).getExpression();
                                 if (expr != null && expr.getKind() == Tree.Kind.ERRONEOUS) {
                                     // TODO:
                                 }
                                 break;*/
                             case IMPORT:                                
                                 t = tp.getLeaf();
                                 break;
                             case RETURN:
                                 t = ((ReturnTree)tp.getLeaf()).getExpression();
                                 break;
                             case THROW:
                                 t = ((ThrowTree)tp.getLeaf()).getExpression();
                                 break;
                         }
                         tp = tp.getParentPath();
                     }
                     if (t != null) {
                         SourcePositions sp = controller.getTrees().getSourcePositions();
                         int endPos = (int)sp.getEndPosition(tp.getCompilationUnit(), t);
                         TokenSequence<JFXTokenId> ts = findLastNonWhitespaceToken(controller, offset, endPos);
                         if (ts != null) {
                             ret[0] = ts.token().id() == JFXTokenId.SEMI ? -1 : ts.offset() + ts.token().length();
                         }
                     } else {
                         TokenSequence<JFXTokenId> ts = ((TokenHierarchy<?>)controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
                         ts.move(offset);
                         if (ts.moveNext() &&  ts.token().id() == JFXTokenId.SEMI)
                             ret[0] = -1;
                     }
                 }
             }, true);
         } catch (IOException ex) {
         }
         return ret[0];
     }
     
     private static TokenSequence<JFXTokenId> findLastNonWhitespaceToken(CompilationController controller, int startPos, int endPos) {
         TokenSequence<JFXTokenId> ts = ((TokenHierarchy<?>)controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         ts.move(endPos);
         while(ts.movePrevious()) {
             int offset = ts.offset();
             if (offset < startPos)
                 return null;
             switch (ts.token().id()) {
             case WS:
             case LINE_COMMENT:
             case COMMENT:
             case DOC_COMMENT:
                 break;
             default:
                 return ts;
             }
         }
         return null;
     }
 
     static class ParamDesc {
         private String fullTypeName;
         private String typeName;
         private String name;
     
         public ParamDesc(String fullTypeName, String typeName, String name) {
             this.fullTypeName = fullTypeName;
             this.typeName = typeName;
             this.name = name;
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof ParamDesc)) {
                 return false;
             }
             ParamDesc pd = (ParamDesc) obj;
             return pd.fullTypeName.equals(this.fullTypeName) && pd.typeName.equals(this.typeName) && pd.name.equals(this.name);
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 37 * hash + (this.fullTypeName != null ? this.fullTypeName.hashCode() : 0);
             hash = 37 * hash + (this.typeName != null ? this.typeName.hashCode() : 0);
             hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
             return hash;
         }
         
     }
     
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 
 }
