 package checkers.fun.jimuva;
 
 import checkers.fun.quals.Anonymous;
 import checkers.fun.quals.Immutable;
 import checkers.fun.quals.ReadOnly;
 import checkers.fun.quals.WriteLocal;
 import checkers.types.AnnotatedTypeMirror;
 import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
 import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
 import checkers.types.BasicAnnotatedTypeFactory;
 import checkers.util.AnnotationUtils;
 import checkers.util.ElementUtils;
 import checkers.util.TreeUtils;
 import com.sun.source.tree.BinaryTree;
 import com.sun.source.tree.ClassTree;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.ConditionalExpressionTree;
 import com.sun.source.tree.ExpressionTree;
 import com.sun.source.tree.IdentifierTree;
 import com.sun.source.tree.LiteralTree;
 import com.sun.source.tree.MethodInvocationTree;
 import com.sun.source.tree.MethodTree;
 import com.sun.source.tree.NewClassTree;
 import com.sun.source.tree.Tree;
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.TypeElement;
 
 /**
  * Determines annotations based on rules governing Funcheck's annotations.
  */
 public class JimuvaAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<JimuvaChecker> {
 
     protected final AnnotationUtils annotationFactory;
     protected JimuvaChecker checker;
     protected JimuvaVisitorState state;
 
     public JimuvaAnnotatedTypeFactory(JimuvaChecker checker, CompilationUnitTree root, JimuvaVisitorState state) {
         super(checker, root, false);
         annotationFactory = checker.getAnnotationFactory();
         this.checker = checker;
         this.state = state;
         state.setFactory(this);
     }
 
     @Override
     protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
         //annotateExpressionWithImmutability(tree, type);
         super.annotateImplicit(tree, type);
         if (tree.getKind() == Tree.Kind.METHOD) {
             refineMethodType((AnnotatedExecutableType) type);
         }
         addMutableAnnotation(type);
     }
 
     @Override
     protected void annotateImplicit(Element el, AnnotatedTypeMirror type) {
         super.annotateImplicit(el, type);
         if (el.getKind() == ElementKind.CONSTRUCTOR || el.getKind() == ElementKind.METHOD) {
             refineMethodType((AnnotatedExecutableType) type);
         }
         addMutableAnnotation(type);
     }
 
     protected void refineMethodType(AnnotatedExecutableType type) {
        if (type.hasAnnotation(checker.READONLY)) {
            AnnotatedTypeMirror.AnnotatedDeclaredType receiver = type.getReceiverType();
            if (receiver != null) {
                receiver.removeAnnotation(checker.MUTABLE);
                receiver.addAnnotation(checker.IMMUTABLE);
            }
        }
         /* Add implicit annotations to methods of Immutable classes */
         ExecutableElement el = type.getElement();
         TypeElement enclosingClass = ElementUtils.enclosingClass(el);
         AnnotatedTypeMirror.AnnotatedDeclaredType enclosingClassType =
                 getAnnotatedType(enclosingClass);
         if (enclosingClassType.hasAnnotation(checker.IMMUTABLE_CLASS)) {
             AnnotationMirror implicit = el.getKind() == ElementKind.CONSTRUCTOR
                     ? checker.ANONYMOUS
                     : checker.READONLY;
            state.addImplicitAnnotation(el, implicit);
            type.addAnnotation(implicit);
         }
     }
 
     /**
      * Add the implicit @Mutable annotation to avoid unqualified types.
      *
      * @param type the type to be refined.
      */
     protected void addMutableAnnotation(AnnotatedTypeMirror type) {
         if (!type.hasAnnotation(checker.IMMUTABLE)) {
             type.addAnnotation(checker.MUTABLE);
         };
 
         if (type instanceof AnnotatedExecutableType) {
             AnnotatedExecutableType ext = (AnnotatedExecutableType) type;
             addMutableAnnotation(ext.getReceiverType());
             addMutableAnnotation(ext.getReturnType());
             for (AnnotatedTypeMirror t : ext.getParameterTypes()) {
                 addMutableAnnotation(t);
             }
         } else if (type instanceof AnnotatedArrayType) {
             AnnotatedArrayType art = (AnnotatedArrayType) type;
             addMutableAnnotation(art.getComponentType());
         }
     }
 }
