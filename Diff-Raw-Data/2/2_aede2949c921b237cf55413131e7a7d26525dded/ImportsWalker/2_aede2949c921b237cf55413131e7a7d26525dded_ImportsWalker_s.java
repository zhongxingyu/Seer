 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.netbeans.modules.javafx.editor.imports;
 
 import com.sun.javafx.api.tree.IdentifierTree;
 import com.sun.javafx.api.tree.ImportTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.TriggerTree;
 import com.sun.javafx.api.tree.TypeClassTree;
 import com.sun.javafx.api.tree.VariableTree;
 import com.sun.tools.javafx.tree.JFXOverrideClassVar;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.TypeKind;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 final public class ImportsWalker extends JavaFXTreePathScanner<Void, ImportsModel> {
     final private EnumSet<ClassIndex.SearchScope> SCOPE = EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE);
     final private CompilationInfo ci;
     final private Set<String> variableNames = new HashSet<String>();
     final private ClassIndex index;
     final private SourcePositions sp;
     final private boolean resolve;
 
     final private Map<String, Set<ElementHandle<TypeElement>>> indexCache = new HashMap<String, Set<ElementHandle<TypeElement>>>();
 
     public ImportsWalker(CompilationInfo info, ClassIndex index, boolean resolve) {
         this.ci = info;
         this.index = index;
         this.resolve = resolve;
         this.sp = info.getTrees().getSourcePositions();
     }
 
     public ImportsWalker(CompilationInfo info, ClassIndex index) {
         this(info, index, true);
     }
 
     public ImportsWalker(CompilationInfo info) {
         this(info, null, false);
     }
 
     @Override
     public Void visitVariable(VariableTree node, ImportsModel model) {
        if (node != null || node.getName() != null) {
             variableNames.add(node.getName().toString());
         }
         return super.visitVariable(node, model);
     }
 
     @Override
     public Void visitIdentifier(IdentifierTree node, ImportsModel model) {
         if (node != null && node.getName() != null) {
             String nodeName = node.getName().toString();
             if (isResolving(nodeName) || variableNames.contains(nodeName)) {
                 return null;
             }
             Element e = ci.getTrees().getElement(getCurrentPath());
 
             processItem(e, nodeName, node, model);
         }
 
         return super.visitIdentifier(node, model);
     }
 
     @Override
     public Void visitTypeClass(TypeClassTree node, ImportsModel model) {
         if (node != null && node.getClassName() != null) {
             String nodeName = node.getClassName().toString();
             if (isResolving(nodeName) || variableNames.contains(nodeName)) {
                 return null;
             }
             Element e = ci.getTrees().getElement(getCurrentPath());
 
             processItem(e, nodeName, node, model);
         }
         
         return super.visitTypeClass(node, model);
     }
 
     @Override
     public Void visitImport(ImportTree node, ImportsModel model) {
         if (node != null && node.getQualifiedIdentifier() != null) {
             long start = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), node);
             long end = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), node);
             model.addDeclaredImport(node.getQualifiedIdentifier().toString(), start, end);
         }
         return super.visitImport(node, model);
     }
 
     @Override
     public Void visitTrigger(TriggerTree node, ImportsModel model) {
         if (node != null) {
             Tree t = ((JFXOverrideClassVar)node).getInitializer();
             if (t != null) {
                 t.accept(this, model);
             }
         }
         return super.visitTrigger(node, model);
     }
 
     /**
      * Will use the class index to resolve the type name and update the model
      * @param typeName The type name to resolve
      * @param t The corresponding {@linkplain Tree} instance
      * @param model The {@linkplain ImportsModel} instance to update
      */
     private void doResolve(String typeName, Tree t, ImportsModel model) {
         assert index != null;
         typeName = getMainType(typeName);
         Set<ElementHandle<TypeElement>> options = index.getDeclaredTypes(typeName, ClassIndex.NameKind.SIMPLE_NAME, SCOPE);
         indexCache.put(typeName, options);
         long pos = sp.getStartPosition(ci.getCompilationUnit(), t);
         model.addUnresolved(typeName, options, pos);
     }
 
     /**
      * Is the type name being already resolved?
      * @param typeName The type name
      * @return Returns true if the type name is already being resolved
      */
     private boolean isResolving(String typeName) {
         typeName = getMainType(typeName);
         return indexCache.containsKey(typeName);
     }
 
     /**
      * Gets the topmost outer type name
      * @param typeName The inner type name
      * @return Returns the topmost outer type name
      */
     private static String getMainType(String typeName) {
         if (!typeName.contains(".")) {
             return typeName;
         } else {
             return typeName.substring(0, typeName.indexOf("."));
         }
     }
 
     /**
      * Will try to walk up the enclosement hierarchy to get the containing class
      * @param clazz The current class
      * @return The containing class (eg. java.util.Map for java.util.Map.Entry)
      */
     private static Element findTopClass(Element clazz) {
         Element enclosing = clazz.getEnclosingElement();
         if (enclosing != null &&
              (enclosing.getKind() == ElementKind.CLASS ||
               enclosing.getKind() == ElementKind.INTERFACE ||
               enclosing.getKind() == ElementKind.ENUM)) {
             return findTopClass(enclosing);
         }
         return clazz;
     }
 
     /**
      * Will process the given item specified by its {@linkplain Element}, name and {@linkplain Tree}
      * If the item is unresolved it will try to resolve it otherwise it will update the imports usage
      *
      * @param e The associated {@linkplain Element} instance
      * @param nodeName The textual representation of the item
      * @param tree The {@linkplain Tree} isntance corresponding to the given element
      * @param model The model to update accordingly
      */
     private void processItem(Element e, String nodeName, Tree tree, ImportsModel model) {
         if (e != null) {
             if (e.asType().getKind() == TypeKind.ERROR || (e.asType().getKind() == TypeKind.PACKAGE && ci.getElements().getPackageElement(nodeName) == null)) {
                 if (resolve) {
                     doResolve(nodeName, tree, model);
                 }
             } else {
                 model.addUsage(findTopClass(e).toString());
             }
         } else {
             if (resolve) {
                 doResolve(nodeName, tree, model);
             }
         }
     }
 }
