 package tdc2013.hibernate.processors;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.AnnotationValue;
 import javax.lang.model.element.ExecutableElement;
 
 public class TypeDefNameVisitor extends AbstractTypeDefVisitor<Void, Set<String>> {
 
     @Override
     public Void visitAnnotation(AnnotationMirror a, Set<String> p) {
 //        Logger.getGlobal().log(Level.WARNING, "visitAnnotation {0}...", a.toString());
         for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : a.getElementValues().entrySet()) {
 //            entry.getValue().accept(this, null);
             Logger.getGlobal().log(Level.WARNING, "AV {0}...", entry.toString());
             final ExecutableElement ee = entry.getKey();
             if (ee.getSimpleName().contentEquals("name")) {
                 p.add(entry.getValue().getValue().toString());
             }
         }
         return null;
     }
 
 }
