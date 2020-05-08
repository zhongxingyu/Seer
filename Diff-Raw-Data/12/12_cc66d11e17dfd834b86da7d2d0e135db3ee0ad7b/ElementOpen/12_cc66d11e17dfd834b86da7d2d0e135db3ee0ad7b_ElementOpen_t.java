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
 
 package org.netbeans.modules.javafx.editor;
 
 import com.sun.source.tree.Tree;
 import com.sun.source.util.TreePath;
 import com.sun.tools.javac.code.Symbol;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.List;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.ArrayType;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.java.queries.SourceForBinaryQuery;
 import org.netbeans.api.java.source.ElementHandle;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.javafx.source.Task;
 import org.openide.filesystems.FileObject;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author nenik
  */
 final class ElementOpen {
 
     private ElementOpen() {}
     
     /**
      * Opens the source file containing given {@link Element} and seek to
      * the declaration.
      * 
      * @param cpInfo ClasspathInfo which should be used for the search
      * @param el     declaration to open
      */
     public static void open(final CompilationInfo comp, final Element el) throws Exception {
         TypeElement tel = getEnclosingClassElement(el);
 
         if (!comp.getJavafxTypes().isJFXClass((Symbol)tel)) { // java
             openThroughJavaSupport(comp.getJavaFXSource().getFileObject(), el);
        } //else {
             // Find the source file
             final FileObject srcFile = getFile(el, comp);
             if (srcFile == null) return;
 
             JavaFXSource js = JavaFXSource.forFileObject(srcFile);
             
             if (js == null) return;
 
             js.runUserActionTask(new Task<CompilationController>() {
                 public void run(CompilationController controller) throws Exception {
                     if (controller.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) return;
                     
                     // convert the element to local element
                     Element real = resolve(el, controller);
                     if (real == null) return;
                     
                     System.err.println("real=" + real);
                     
                     TreePath elpath = controller.getPath(real);
                     Tree tree = elpath != null ? elpath.getLeaf() : null;
 
                     if (tree != null) {
                         long startPos = controller.getTrees().getSourcePositions().getStartPosition(controller.getCompilationUnit(), tree);
 
                         if (startPos != -1l) GoToSupport.doOpen(srcFile, (int)startPos);
                     }
                 }
             }, true);
     
//        }
     }
 
     private static Element resolve(Element orig, CompilationController context) {
         String[] sig = getSignatures(orig);
         ElementKind kind = orig.getKind();
         switch (kind) {
             case PACKAGE:
                 break;
             case CLASS:
             case INTERFACE:
             case ENUM:
             case ANNOTATION_TYPE:
                 return context.getElements().getTypeElement(sig[0].replace('$', '.'));
 
             case METHOD:
             case CONSTRUCTOR:                
                 assert sig.length == 3;
                 TypeElement type = context.getElements().getTypeElement(sig[0].replace('$', '.'));
                 if (type != null) {
                    final List<? extends Element> members = type.getEnclosedElements();
                    for (Element member : members) {
                        if (kind == member.getKind()) {
                            String[] desc = createExecutableDescriptor((ExecutableElement)member);
                            assert desc.length == 3;
                            if (sig[1].equals(desc[1]) && sig[2].equals(desc[2])) return member;
                        }
                    }
                 }
                 break;
             case INSTANCE_INIT:
             case STATIC_INIT:
                 assert sig.length == 2;
                 type = context.getElements().getTypeElement(sig[0].replace('$', '.'));
                 if (type != null) {
                    final List<? extends Element> members = type.getEnclosedElements();
                    for (Element member : members) {
                        if (kind == member.getKind()) {
                            String[] desc = createExecutableDescriptor((ExecutableElement)member);
                            assert desc.length == 2;
                            if (sig[1].equals(desc[1])) return member;
                        }
                    }
                 }
                 break;
 
             case FIELD:
             case ENUM_CONSTANT:
                 assert sig.length == 3;
                 type = context.getElements().getTypeElement(sig[0].replace('$', '.'));
                 if (type != null) {
                     final List<? extends Element> members = type.getEnclosedElements();
                     for (Element member : members) {
                         if (kind == member.getKind()) {
                             String[] desc = createFieldDescriptor((VariableElement)member);
                             assert desc.length == 3;
                             if (sig[1].equals(desc[1]) && sig[2].equals(desc[2])) return member;
                         }
                     }
                 }
                 break;
         }
         return null;
     }   
     
     private static TypeElement getEnclosingClassElement(Element el) {
         while (el != null && el.getKind() != ElementKind.CLASS) {
             el = el.getEnclosingElement();
         }
         if (el == null) return null;
         
         while (el.getEnclosingElement() != null && el.getEnclosingElement().getKind() == ElementKind.CLASS) {
             el = el.getEnclosingElement();
         }
         return (TypeElement)el; // or null
     }        
 
     
     private static FileObject getFile (Element elem, final CompilationInfo comp) {
         assert elem != null;
         assert comp != null;
 
         try {
             FileObject ref = comp.getJavaFXSource().getFileObject();
             TypeElement tel = getEnclosingClassElement(elem);
             Symbol.ClassSymbol cs = (Symbol.ClassSymbol)tel;
             String name = cs.className().replace('.', '/') + ".fx"; // NOI18N
 
             ClasspathInfo cpi = ClasspathInfo.create(ref);
             ClassPath[] all = new ClassPath[] {
                 cpi.getClassPath(ClasspathInfo.PathKind.BOOT),
                 cpi.getClassPath(ClasspathInfo.PathKind.COMPILE),
                 cpi.getClassPath(ClasspathInfo.PathKind.SOURCE)
             };
 
             for (ClassPath cp : all) { // cp never null
                 for (FileObject binRoot : cp.getRoots()) {
                    FileObject fo = binRoot.getFileObject(name);
                    if (fo != null) return fo;

                     SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(binRoot.getURL());
                     for (FileObject srcRoot : res.getRoots()) {
                        fo = srcRoot.getFileObject(name);
                         if (fo != null) return fo;
                     }
                 }
             }
             
         } catch (IOException e) {
             Exceptions.printStackTrace(e);
         }
         return null;        
     }
 
     
 
     // All of the following code is a hack to call through to the java
     // implementation of open support, where we need to pass "their"
     // implementation of Element/ElementHandle.
     // All of the Element serialization logic is copied and adapted from
     // javasource's ElementHandle and ClassFileUtils
     @SuppressWarnings("unchecked")
     private static void openThroughJavaSupport(FileObject reference, Element el) throws Exception {
         org.netbeans.api.java.source.ClasspathInfo cpi = 
                 org.netbeans.api.java.source.ClasspathInfo.create(reference);
         // Load the right version of the ElementKind class and convert our instance to it
         Class ekClass = ElementHandle.class.getClassLoader().loadClass("javax.lang.model.element.ElementKind");
         Object ekInstance = Enum.valueOf(ekClass, el.getKind().name());
         
         String[] sig = getSignatures(el);
         Class strArrClass = sig.getClass();
         
         Constructor ehCtor = ElementHandle.class.getDeclaredConstructor(ekClass, strArrClass);
         ehCtor.setAccessible(true);
         ElementHandle eh = (ElementHandle)ehCtor.newInstance(ekInstance, sig);
         
         org.netbeans.api.java.source.ui.ElementOpen.open(cpi, eh);
     }
     
 
     private static String[] getSignatures (final Element element) throws IllegalArgumentException {
         assert element != null;
         ElementKind kind = element.getKind();
         String[] signatures;
         switch (kind) {
             case PACKAGE:
                 assert element instanceof PackageElement;
                 signatures = new String[]{((PackageElement)element).getQualifiedName().toString()};
                 break;
             case CLASS:
             case INTERFACE:
             case ENUM:
             case ANNOTATION_TYPE:
                 signatures = new String[] {encodeClassNameOrArray((TypeElement)element)};
                 break;
             case METHOD:
             case CONSTRUCTOR:                
             case INSTANCE_INIT:
             case STATIC_INIT:
                 signatures = createExecutableDescriptor((ExecutableElement)element);
                 break;
             case FIELD:
             case ENUM_CONSTANT:
                 signatures = createFieldDescriptor((VariableElement)element);
                 break;
             default:
                 throw new IllegalArgumentException(kind.toString());
         }
         return signatures;
     }
 
     private static String encodeClassNameOrArray (TypeElement td) {
         assert td != null;
         CharSequence qname = td.getQualifiedName();
         TypeMirror enclosingType = td.getEnclosingElement().asType();
         if (qname != null && enclosingType != null && enclosingType.getKind() == TypeKind.NONE && "Array".equals(qname.toString())) {     //NOI18N
             return "[";  //NOI18N
         }
         else {
             return encodeClassName(td);
         }
     }
     
     private static String encodeClassName (TypeElement td) {
         assert td != null;
         StringBuilder sb = new StringBuilder ();
         encodeClassName(td, sb,'.');    // NOI18N
         return sb.toString();
     }
     
     private static void encodeType (final TypeMirror type, final StringBuilder sb) {
 	switch (type.getKind()) {
 	    case VOID:
 		sb.append('V');	    // NOI18N
 		break;
 	    case BOOLEAN:
 		sb.append('Z');	    // NOI18N
 		break;
 	    case BYTE:
 		sb.append('B');	    // NOI18N
 		break;
 	    case SHORT:
 		sb.append('S');	    // NOI18N
 		break;
 	    case INT:
 		sb.append('I');	    // NOI18N
 		break;
 	    case LONG:
 		sb.append('J');	    // NOI18N
 		break;
 	    case CHAR:
 		sb.append('C');	    // NOI18N
 		break;
 	    case FLOAT:
 		sb.append('F');	    // NOI18N
 		break;
 	    case DOUBLE:
 		sb.append('D');	    // NOI18N
 		break;
 	    case ARRAY:
 		sb.append('[');	    // NOI18N
 		assert type instanceof ArrayType;
 		encodeType(((ArrayType)type).getComponentType(),sb);
 		break;
 	    case DECLARED:
             {
 		sb.append('L');	    // NOI18N
 		TypeElement te = (TypeElement) ((DeclaredType)type).asElement();
 		encodeClassName(te, sb,'/');
 		sb.append(';');	    // NOI18N
 		break;
             }
 	    default:
 		throw new IllegalArgumentException ();
 	}                
     }        
     
     private static void encodeClassName (TypeElement te, final StringBuilder sb, final char separator) {
         sb.append(((Symbol.ClassSymbol)te).flatname.toString().replace('.', separator));
     }
 
     private static String[] createExecutableDescriptor (final ExecutableElement ee) {
         assert ee != null;
         final ElementKind kind = ee.getKind();
         final String[] result = (kind == ElementKind.STATIC_INIT || kind == ElementKind.INSTANCE_INIT) ? new String[2] : new String[3];
         final Element enclosingType = ee.getEnclosingElement();
 	assert enclosingType instanceof TypeElement;
         result[0] = encodeClassNameOrArray ((TypeElement)enclosingType);        
         if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
             final StringBuilder retType = new StringBuilder ();
             if (kind == ElementKind.METHOD) {
                 result[1] = ee.getSimpleName().toString();
                 encodeType(ee.getReturnType(), retType);
             }
             else {
                 result[1] = "<init>";   // NOI18N
                 retType.append('V');    // NOI18N
             }
             StringBuilder sb = new StringBuilder ();
             sb.append('(');             // NOI18N
             for (VariableElement pd : ee.getParameters()) {
                 encodeType(pd.asType(),sb);
             }
             sb.append(')');             // NOI18N
             sb.append(retType);
             result[2] = sb.toString();
         }
         else if (kind == ElementKind.INSTANCE_INIT) {
             result[1] = "<init>";       // NOI18N
         } 
         else if (kind == ElementKind.STATIC_INIT) {
             result[1] = "<cinit>";      // NOI18N
         }
         else {
             throw new IllegalArgumentException ();
         }
         return result;
     }
 
     private static String[] createFieldDescriptor (final VariableElement ve) {
 	assert ve != null;
         String[] result = new String[3];
 	Element enclosingElement = ve.getEnclosingElement();
 	assert enclosingElement instanceof TypeElement;
         result[0] = encodeClassNameOrArray ((TypeElement) enclosingElement);
         result[1] = ve.getSimpleName().toString();
         StringBuilder sb = new StringBuilder ();
         encodeType(ve.asType(),sb);
         result[2] = sb.toString();        
         return result;
     }        
 
 }
