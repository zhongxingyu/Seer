 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 package org.netbeans.api.javafx.source;
 
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.UnitTree;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.code.JavafxTypes;
 import com.sun.tools.javafx.comp.JavafxEnter;
 import com.sun.tools.javafx.comp.JavafxEnv;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import com.sun.tools.javafx.tree.JFXScript;
 import com.sun.tools.javafx.tree.JFXTree;
 import com.sun.tools.javafx.tree.JFXVar;
 import com.sun.tools.javafx.tree.JavafxTreeScanner;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import javax.tools.Diagnostic;
 import javax.tools.JavaFileObject;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.openide.filesystems.FileObject;
 
 /**
  *
  * @author nenik
  */
 public class CompilationInfo {
 
     final CompilationInfoImpl impl;
     private ElementUtilities elementUtilities;
     private TreeUtilities treeUtilities;
 
     public ElementUtilities getElementUtilities() {
         if (null == elementUtilities) {
             elementUtilities = new ElementUtilities(this);
         }
         return elementUtilities;
     }
 
     public FileObject getFileObject() {
         return impl.getJavaFXSource().getFileObject();
     }
 
     public TreeUtilities getTreeUtilities() {
         if (null == treeUtilities) {
             treeUtilities = new TreeUtilities(this);
         }
         return treeUtilities;
     }
 
     public CompilationInfo(JavaFXSource source) {
         impl = new CompilationInfoImpl(source);
     }
 
     public CompilationInfo(CompilationInfoImpl impl) {
         this.impl = impl;
     }
 
     public JavaFXSource.Phase getPhase() {
         return impl.getPhase();
     }
     
     public JavaFXSource.Phase moveToPhase(Phase phase) throws IOException {
         return impl.toPhase(phase);
     }
     
     /**
      * Return the {@link com.sun.tools.javafx.api.JavafxcTrees} service of the javafxc represented by this {@link CompilationInfo}.
      * @return javafxc Trees service
      */
     public JavafxcTrees getTrees() {
         return JavafxcTrees.instance(impl.getJavafxcTask());
     }
     // XXX: hack around lack of support in compiler
     public JavaFXTreePath getPath(Element e) {
         Symbol sym = (Symbol) e;
         JavafxEnter enter = JavafxEnter.instance(impl.getContext());
         JavafxEnv env = enter.getEnv(sym.enclClass());
         if (env == null) {
             return null;
         }
         JFXTree tree = declarationFor(sym, env.tree);
         return tree == null ? null : getTrees().getPath(getCompilationUnit(), tree);
     }
 
     private static JFXTree declarationFor(final Symbol sym, final JFXTree tree) {
 
         class DeclScanner extends JavafxTreeScanner {
 
             JFXTree result = null;
 
             public @Override void scan(JFXTree tree) {
                 if (tree != null && result == null) {
                     tree.accept(this);
                 }
             }
 
             public @Override void visitScript( JFXScript that) {
                 if (that.packge == sym) {
                     result = that;
                 } else {
                     super.visitScript(that);
                 }
             }
 
             public 
             @Override
             void visitClassDeclaration( JFXClassDeclaration that) {
                 if (that.sym == sym) {
                     result = that;
                 } else {
                     super.visitClassDeclaration(that);
                 }
             }
 
             public 
             @Override
             void visitFunctionDefinition( JFXFunctionDefinition that) {
                 if (that.sym == sym) {
                     result = that;
                 } else {
                     super.visitFunctionDefinition(that);
                 }
             }
 
 
             public 
             @Override
             void visitVar( JFXVar that) {
                 if (that.sym == sym) {
                     result = that;
                 } else {
                     super.visitVar(that);
                 }
             }
 
         }
         DeclScanner s = new DeclScanner();
         tree.accept(s);
         return s.result;
     }
 
     public Types getTypes() {
         return impl.getJavafxcTask().getTypes();
     }
 
     public JavafxTypes getJavafxTypes() {
         return JavafxTypes.instance(impl.getContext());
     }
 
     public Elements getElements() {
         return impl.getJavafxcTask().getElements();
     }
 
     /**
      * Returns {@link JavaFXSource} for which this {@link CompilationInfo} was created.
      * @return JavaFXSource
      */
     public JavaFXSource getJavaFXSource() {
         return impl.getJavaFXSource();
     }
 
     /**
      * Returns the javafxc tree representing the source file.
      * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the,
      * javafx source file.
      * 
      * @throws java.lang.IllegalStateException  when the phase is less than {@link JavaFXSource.Phase#PARSED}
      */
     public UnitTree getCompilationUnit() {
         return impl.getCompilationUnit();
     }
     
     public Iterable <? extends JavaFileObject> getClassBytes() {
         return impl.getClassBytes();
     }
 
     public TokenHierarchy getTokenHierarchy() {
         return impl.getTokenHierarchy();
     }
 
     public List<Diagnostic> getDiagnostics() {
         return this.impl.getDiagnostics();
     }
 
     public List<? extends TypeElement> getTopLevelElements() {
 //        checkConfinement();
 //        if (this.impl.getPositionConverter() == null) {
 //            throw new IllegalStateException ();
 //        }
         final List<TypeElement> result = new ArrayList<TypeElement>();
 //        final JavaSource javaSource = this.impl.getJavaSource();
 //        if (javaSource.isClassFile()) {
 //            Elements elements = getElements();
 //            assert elements != null;
 //            assert javaSource.rootFo != null;
 //            String name = FileObjects.convertFolder2Package(FileObjects.stripExtension(FileUtil.getRelativePath(javaSource.rootFo, getFileObject())));
 //            TypeElement e = ((JavacElements)elements).getTypeElementByBinaryName(name);
 //            if (e != null) {                
 //                result.add (e);
 //            }
 //        }
 //        else {
         UnitTree cu = getCompilationUnit();
         if (cu == null) {
             return null;
         }
 
         final JavafxcTrees trees = getTrees();
         assert trees != null;
         List<? extends Tree> typeDecls = cu.getTypeDecls();
         JavaFXTreePath cuPath = new JavaFXTreePath(cu);
         for (Tree t : typeDecls) {
             JavaFXTreePath p = new JavaFXTreePath(cuPath, t);
             Element e = trees.getElement(p);
             if (e != null && (e.getKind().isClass() || e.getKind().isInterface())) {
                 result.add((TypeElement) e);
             }
         }
 //        }
         return Collections.unmodifiableList(result);
     }
 }
