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
 package org.netbeans.modules.javafx.refactoring.impl;
 
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import java.io.IOException;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Position.Bias;
 import javax.swing.text.StyledDocument;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.editor.GuardedDocument;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.SourceUtils;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.TreePathHandle;
 import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObject;
 import org.openide.text.DataEditorSupport;
 import org.openide.text.NbDocument;
 import org.openide.text.PositionBounds;
 import org.openide.util.Lookup;
 
 /**
  * An element in the refactoring preview list which holds information about the find-usages-match
  * 
  * @author Jaroslav Bachorik
  */
 
 public class WhereUsedElement extends SimpleRefactoringElementImplementation {
     private PositionBounds bounds;
     private String displayText;
     volatile private int startPosition;
     volatile private int endPosition;
 
     private DataEditorSupport des;
     private GuardedDocument doc;
 
     private final TreePathHandle handle;
     private final Lookup context;
 
     private WhereUsedElement(TreePathHandle handle, Lookup context) throws IOException {
         this.handle = handle;
         this.context = context;
         init();
     }
 
     private void init() throws IOException {
         DataObject dobj = DataObject.find(handle.getFileObject());
         des = (DataEditorSupport)dobj.getCookie(EditorCookie.class);
         doc = (GuardedDocument)des.getDocument();
         if (doc == null) {
             doc = (GuardedDocument)des.openDocument();
         }
 
         JavaFXSource jfxs = JavaFXSource.forFileObject(handle.getFileObject());
         jfxs.runWhenScanFinished(new Task<CompilationController>() {
 
             public void run(CompilationController cc) throws Exception {
                 JavaFXTreePath path = handle.resolve(cc);
                 TokenSequence<JFXTokenId> tokens = cc.getTreeUtilities().tokensFor(path.getLeaf());
                 tokens.moveStart();
                 while (tokens.moveNext()) {
                     Token<JFXTokenId> token = tokens.token();
 
                     if (handle.getSimpleName().equals(token.text().toString())) {
                         startPosition = token.offset(cc.getTokenHierarchy());
                         endPosition = startPosition + token.length();
                         break;
                     }
                 }
             }
         }, true);
         try {
             doc.readLock();
             if (startPosition != -1) {
                 int sta = Utilities.getRowFirstNonWhite(doc, startPosition);
                 if (sta == -1) {
                     sta = Utilities.getRowStart(doc, startPosition);
                 }
                 int en = Utilities.getRowLastNonWhite(doc, startPosition);
 
                 if (en == -1) {
                     en = Utilities.getRowEnd(doc, startPosition);
                 } else {
                     // Last nonwhite - left side of the last char, not inclusive
                     en++;
                 }
 
                 displayText = SourceUtils.getHtml(doc.getText(sta, en - sta + 1));
                 bounds = new PositionBounds(des.createPositionRef(startPosition, Bias.Forward), des.createPositionRef(endPosition, Bias.Forward));
             } else {
                 System.err.println("*** Can not resolve: " + handle);
                 throw new IOException();
             }
         } catch (BadLocationException e) {
            throw new IOException(e);
         } finally {
             doc.readUnlock();
         }
     }
 
     public String getDisplayText() {
         return displayText;
     }
 
     public Lookup getLookup() {
         return context;
     }
 
     public PositionBounds getPosition() {
         return bounds;
     }
 
     public String getText() {
         return displayText;
     }
 
     public void performChange() {
     }
 
     public FileObject getParentFile() {
         return handle.getFileObject();
     }
 
     public static WhereUsedElement create(TreePathHandle handle, Lookup context) {
         try {
             return new WhereUsedElement(handle, context);
         } catch (IOException e) {
 
         }
         return null;
     }
 }
