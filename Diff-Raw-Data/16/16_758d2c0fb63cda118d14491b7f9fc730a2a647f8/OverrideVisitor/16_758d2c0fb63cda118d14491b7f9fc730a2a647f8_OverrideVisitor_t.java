 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.netbeans.modules.javafx.editor.hints;
 
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.FunctionDefinitionTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import org.netbeans.api.javafx.source.CompilationInfo;
 
 /**
  *
  * @author karol harezlak
  */
 final class OverrideVisitor extends JavaFXTreePathScanner<Void, Void> {
 
    private static Logger LOGGER = Logger.getLogger(CreateNewElementTaskFactory.class.getName());

     private CompilationInfo compilationInfo;
     private Collection<Element> classes;
     private Map<Element, List<MethodSymbol>> overriddenMethods;
 
     OverrideVisitor(CompilationInfo compilationInfo,
             Collection<Element> classes,
             Map<Element, List<MethodSymbol>> overriddenMethods) {
 
         this.compilationInfo = compilationInfo;
         this.classes = classes;
         this.overriddenMethods = overriddenMethods;
     }
 
     @Override
     public Void visitClassDeclaration(ClassDeclarationTree node, Void v) {
         Element currentClass = compilationInfo.getTrees().getElement(getCurrentPath());
         if (currentClass != null ) { 
            classes.add(currentClass);
         }
 
         return super.visitClassDeclaration(node, v);
     }
 
     @Override
     @SuppressWarnings("element-type-mismatch")
     public Void visitFunctionDefinition(FunctionDefinitionTree node, Void v) {
//        if (node.toString().contains(" overridefunction ") || node.toString().contains(" override ")) { //NOI18N
             Element element = compilationInfo.getTrees().getElement(getCurrentPath());
             if (element != null) {
                 Element currentClass = element.getEnclosingElement();
                 if (element instanceof MethodSymbol) {
                     if (overriddenMethods.get(currentClass) == null) {
                         overriddenMethods.put(currentClass, new ArrayList<MethodSymbol>());
                     }
                     List<MethodSymbol> methods = overriddenMethods.get(currentClass);
                     if (!methods.contains(element)) {
                         methods.add((MethodSymbol) element);
                     }
                     overriddenMethods.put(currentClass, methods);
                 }
             }
        //}
         return super.visitFunctionDefinition(node, v);
     }
 }
