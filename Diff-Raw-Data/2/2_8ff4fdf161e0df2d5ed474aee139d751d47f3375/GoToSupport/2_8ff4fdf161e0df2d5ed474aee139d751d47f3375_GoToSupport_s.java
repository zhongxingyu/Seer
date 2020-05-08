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
 
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.mjavac.code.Symbol;
 import com.sun.tools.mjavac.code.Type;
 import com.sun.tools.javafx.code.JavafxTypes;
 import java.io.IOException;
 import java.net.URL;
 import java.util.EnumSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeKind;
 import javax.swing.SwingUtilities;
 import javax.swing.text.Document;
 import javax.swing.text.StyledDocument;
 import org.netbeans.api.javafx.editor.Cancellable;
 import org.netbeans.api.javafx.editor.ElementOpen;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.editor.FXSourceUtils.URLResult;
 import org.netbeans.api.javafx.editor.SafeTokenSequence;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.openide.awt.HtmlBrowser;
 import org.openide.cookies.EditorCookie;
 import org.openide.cookies.LineCookie;
 import org.openide.cookies.OpenCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObject;
 import org.openide.text.Line;
 import org.openide.text.NbDocument;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author Petr Nejedly
  */
 public class GoToSupport {
     
     /** Static utility class */
     private GoToSupport() {
     }
 
     public static void goTo(Document doc, int offset, boolean goToSource) {
         performGoTo(doc, offset, goToSource, false, false);
     }
 
     public static void goToJavadoc(Document doc, int offset) {
         performGoTo(doc, offset, false, false, true);
     }
 
     public static String getGoToElementTooltip(Document doc, final int offset, final boolean goToSource) {
         return performGoTo(doc, offset, goToSource, true, false);
     }
 
     private static FileObject getFileObject(Document doc) {
         DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
         return od != null ? od.getPrimaryFile() : null;
     }
 
     private static String performGoTo(final Document doc, final int off, final boolean goToSource, final boolean tooltip, final boolean javadoc) {
         final FileObject fo = getFileObject(doc);
 
         if (fo == null)
             return null;
 
         final JavaFXSource js = JavaFXSource.forFileObject(fo);
 
         if (js == null)
             return null;
 
         final String[] result = new String[1];
 
         final AtomicBoolean cancelled = new AtomicBoolean();
         final Runnable opener = new Runnable() {
             public void run() {
                 try {
                     js.runUserActionTask(new Task<CompilationController>() {
                         public void run(final CompilationController controller) throws Exception {
                             if (controller.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED))
                                 return;
 
                             @SuppressWarnings("unchecked")
                             Token<JFXTokenId>[] token = new Token[1];
                             int[] span = getIdentifierSpan(doc, off, token);
 
                             if (span == null) {
         //                        CALLER.beep(goToSource, javadoc);
                                 return ;
                             }
 
                             final int offset = span[0] + 1;
                             JavaFXTreePath path = controller.getTreeUtilities().pathFor(offset);
 
                             Tree leaf = path.getLeaf();
                             if (leaf == null) return;
 
                             Element el = controller.getTrees().getElement(path);
                             if (el == null) return;
 
                             if (tooltip) {
                                 result[0] = FXSourceUtils.getElementTooltip(controller.getJavafxTypes(), el);
                                 return;
                             } else if (javadoc) {
                                 result[0] = null;
                                 final URLResult res = FXSourceUtils.getJavadoc(el, controller);
                                 URL url = res != null ? res.url : null;
                                 if (url != null) {
                                     HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                                 } else {
         //                            CALLER.beep(goToSource, javadoc);
                                 }
                             } else {
                                 if (goToSource && el instanceof VariableElement) {
                                     Symbol sym = (Symbol)el;
                                     Type type = sym.asType();
 
                                     // handle sequences as their element type
                                     JavafxTypes types = controller.getJavafxTypes();
                                     if (types.isSequence(type)) {
                                         type = types.elementType(type);
                                     }
 
                                     if (type != null && type.getKind() == TypeKind.DECLARED) {
                                         el = ((DeclaredType)type).asElement();
 
                                         if (el == null) return;
                                     }
                                 }
 
                                 JavaFXTreePath elpath = controller.getPath(el);
                                 Tree tree = elpath != null && path.getCompilationUnit() == elpath.getCompilationUnit()? elpath.getLeaf(): null;
 
                                 if (!cancelled.get()) {
                                     if (tree != null) {
                                         long startPos = controller.getTrees().getSourcePositions().getStartPosition(controller.getCompilationUnit(), tree);
 
                                         if (startPos != -1l) doOpen(fo, (int)startPos);
                                     } else {
                                         final Element opening = el;
                                         SwingUtilities.invokeLater(new Runnable() {
 
                                             public void run() {
                                                 try {
                                                     ElementOpen.open(controller, opening);
                                                 } catch (Exception e) {}
                                             }
                                         });
                                     }
                                 }
                             }
                         }
                     }, true);
                 } catch (IOException ioe) {
                     throw new IllegalStateException(ioe);
                 }
             }
         };
 
         if (tooltip) {
             opener.run();
         } else {
             RunOffAWT.runOffAWT(new Runnable() {
 
                 public void run() {
                     opener.run();
                 }
            }, NbBundle.getMessage(GoToSupport.class, "LBL_GoToSupport"), cancelled); // NOI18N
         }
 
         return result[0];
     }
     
     
     private static final Set<JFXTokenId> USABLE_TOKEN_IDS = EnumSet.of(JFXTokenId.IDENTIFIER, JFXTokenId.THIS, JFXTokenId.SUPER);
 
     public static int[] getIdentifierSpan(Document doc, int offset, Token<JFXTokenId>[] token) {
         if (getFileObject(doc) == null) {
             //do nothing if FO is not attached to the document - the goto would not work anyway:
             return null;
         }
         
         TokenHierarchy th = TokenHierarchy.get(doc);
         @SuppressWarnings("unchecked")
         TokenSequence<JFXTokenId> ts_ = (TokenSequence<JFXTokenId>) th.tokenSequence();
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, doc, Cancellable.Dummy.getInstance());
 
         if (ts == null)
             return null;
         
         ts.move(offset);
         if (!ts.moveNext())
             return null;
         
         Token<JFXTokenId> t = ts.token();
         
         if (!USABLE_TOKEN_IDS.contains(t.id())) {
             ts.move(offset - 1);
             if (!ts.moveNext())
                 return null;
             t = ts.token();
             if (!USABLE_TOKEN_IDS.contains(t.id()))
                 return null;
         }
         
         if (token != null)
             token[0] = t;
         
         return new int [] {ts.offset(), ts.offset() + t.length()};
     }
 
     public static boolean doOpen(FileObject fo, int offset) {
         try {
             DataObject od = DataObject.find(fo);
             EditorCookie ec = od.getCookie(EditorCookie.class);
             LineCookie lc = od.getCookie(LineCookie.class);
             
             if (ec != null && lc != null && offset != -1) {                
                 StyledDocument doc = ec.openDocument();                
                 if (doc != null) {
                     int line = NbDocument.findLineNumber(doc, offset);
                     int lineOffset = NbDocument.findLineOffset(doc, line);
                     final int column = offset - lineOffset;
                     
                     if (line != -1) {
                         final Line l = lc.getLineSet().getCurrent(line);
                         
                         if (l != null) {
                             SwingUtilities.invokeLater(new Runnable() {
 
                                 public void run() {
                                     l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, column);
                                 }
                             });
                             
                             return true;
                         }
                     }
                 }
             }
             
             OpenCookie oc = od.getCookie(OpenCookie.class);
             
             if (oc != null) {
                 oc.open();                
                 return true;
             }
         } catch (IOException e) {
         }
         
         return false;
     }
 }
