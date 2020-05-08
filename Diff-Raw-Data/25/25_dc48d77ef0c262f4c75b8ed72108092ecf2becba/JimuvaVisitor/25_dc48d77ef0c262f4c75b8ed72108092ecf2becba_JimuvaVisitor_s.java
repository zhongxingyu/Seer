 package checkers.fun.jimuva;
 
 import checkers.basetype.BaseTypeVisitor;
 import checkers.fun.quals.Anonymous;
 import checkers.fun.quals.ImmutableClass;
 import checkers.source.Result;
 import checkers.types.AnnotatedTypeMirror;
 import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
 import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
 import checkers.util.AnnotationUtils;
 import checkers.util.InternalUtils;
 import checkers.util.TreeUtils;
 import com.sun.source.tree.ArrayAccessTree;
 import com.sun.source.tree.ArrayTypeTree;
 import com.sun.source.tree.AssignmentTree;
 import com.sun.source.tree.ClassTree;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.ExpressionTree;
 import com.sun.source.tree.MemberSelectTree;
 import com.sun.source.tree.MethodInvocationTree;
 import com.sun.source.tree.MethodTree;
 import com.sun.source.tree.NewArrayTree;
 import com.sun.source.tree.NewClassTree;
 import com.sun.source.tree.ReturnTree;
 import com.sun.source.tree.Tree;
 import com.sun.source.tree.VariableTree;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 
 /**
  *
  * @author saf
  */
 public class JimuvaVisitor extends BaseTypeVisitor<Void, Void> {
 
     protected JimuvaChecker checker;
 
     protected JimuvaAnnotatedTypeFactory atypeFactory;
     protected JimuvaVisitorState state;
 
     public JimuvaVisitor(JimuvaChecker checker, CompilationUnitTree root, JimuvaVisitorState state) throws IOException {
         super(checker, root);
         this.checker = checker;
         atypeFactory = new JimuvaAnnotatedTypeFactory(checker, root, state);
         this.state = state;
     }
 
     @Override
     public Void visitAssignment(AssignmentTree node, Void p) {
         ExpressionTree varTree = node.getVariable();
         ExpressionTree valueTree = node.getExpression();
         AnnotatedTypeMirror valueType = atypeFactory.getAnnotatedType(valueTree);
 
         AnnotatedTypeMirror receiver = atypeFactory.getReceiver(varTree);
         Element varElement = InternalUtils.symbol(varTree);
         if (receiver != null && receiver.hasAnnotation(checker.IMMUTABLE)
                 && varElement.getKind().isField()
                 && receiver.getKind() != TypeKind.ARRAY) {
             if (TreeUtils.isSelfAccess(varTree) && varElement.getKind() == ElementKind.FIELD) {
                 checker.report(Result.failure("assign.readonly.receiver.field", 
                         varTree.toString(), state.getCurrentMethodName()), node);
                 if (state.inImplicitlyAnnotatedMethod()) {
                     checker.note(null, "readonly.implicit",
                             state.getCurrentMethodName(), state.getCurrentClassName());
                 }
             } else {
                 checker.report(Result.failure("assign.immutable.field", 
                         varTree.toString(), receiver.getElement().toString()), node);
             }
         }
         AnnotatedTypeMirror methodReceiver = state.getCurrentMethod().getReceiverType();
         checkAssignmentRep(node, methodReceiver != null
                 && methodReceiver.hasAnnotation(checker.IMMUTABLE));
 
         if (state.isCurrentMethod(checker.ANONYMOUS)) {
             checkAssignmentAnonymous(node);
         }
 
         /* Cannot create static aliases to @Safe values */
         if (varElement.getKind() == ElementKind.FIELD && valueType.hasAnnotation(checker.SAFE)) {
             checker.report(Result.failure("static.alias.to.safe",
                     varElement.toString(), valueTree.toString()), node);
         }
 
         return super.visitAssignment(node, p);
     }
 
     @Override
     public Void visitVariable(VariableTree node, Void p) {
         /* Check that no @Rep field is public */
         VariableElement el = TreeUtils.elementFromDeclaration(node);
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(el);
         if (el.getKind().isField()
                 && el.getModifiers().contains(Modifier.PUBLIC)
                 && type.hasAnnotation(checker.REP)) {
             checker.report(Result.failure("public.rep.field", el.getSimpleName()), node);
         }
         if (el.getKind().isField()
                 && state.isCurrentClass(checker.IMMUTABLE_CLASS)
                 && el.getModifiers().contains(Modifier.PUBLIC)
                 && !el.getModifiers().contains(Modifier.FINAL)) {
             checker.report(Result.failure("public.field.of.immutable.class",
                     el.getSimpleName(), state.getCurrentClassName()), el);
         }
         return super.visitVariable(node, p);
     }
 
     @Override
     protected void commonAssignmentCheck(AnnotatedTypeMirror varType, AnnotatedTypeMirror valueType, Tree valueTree, String errorKey, Void p) {
 //        System.err.println("Assigning " + valueTree.toString() + " (" + valueType.toString() +
 //               ") to " + varType.toString());
 
         AnnotatedTypeMirror varCopy = annoTypes.deepCopy(varType);
 
         /* An object cannot lose or gain the @Immutable annotation */
         if (varCopy.hasAnnotation(checker.MUTABLE)
                 && valueType.hasAnnotation(checker.IMMUTABLE)) {
             checker.report(Result.failure(errorKey, valueType.toString(), varType.toString()), valueTree);
         } else {
             varCopy.removeAnnotation(checker.IMMUTABLE);
         }
 
         if (varCopy.getKind() == TypeKind.ARRAY && valueType.getKind() == TypeKind.ARRAY) {
             AnnotatedTypeMirror.AnnotatedArrayType varArrayType = (AnnotatedTypeMirror.AnnotatedArrayType) varType;
             AnnotatedTypeMirror.AnnotatedArrayType valueArrayType = (AnnotatedTypeMirror.AnnotatedArrayType) valueType;
 
             commonAssignmentCheck(varArrayType.getComponentType(), valueArrayType.getComponentType(),
                     valueTree, errorKey, p);
         }
 
         super.commonAssignmentCheck(varCopy, valueType, valueTree, errorKey, p);
     }
 
     @Override
     public Void visitClass(ClassTree node, Void p) {
         state.enterClass(node);
         try {
             return super.visitClass(node, p);
         } finally {
             state.leaveClass();
         }
     }
 
     @Override
     public Void visitMethod(MethodTree node, Void p) {
         state.enterMethod(node);
         try {
             return super.visitMethod(node, p);
         } finally {
             state.leaveMethod();
         }
     }
 
     @Override
     public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
         AnnotatedExecutableType calledMethod = atypeFactory.methodFromUse(node);
 //        System.err.println("INVOCATION: " + node.toString());
         AnnotatedTypeMirror receiver = atypeFactory.getReceiver(node);
         AnnotatedTypeMirror calledMethodReceiver = calledMethod.getReceiverType();
 
 //        System.err.println(receiver.toString() + " ...---> " + calledMethod.toString());
 
         /* Method calls cannot alter the inner representation of @Immutable objects */
         if (state.isCurrentMethodReceiver(checker.IMMUTABLE)
                 && receiver != null && receiver.hasAnnotation(checker.REP)
                 && calledMethodReceiver != null && !calledMethodReceiver.hasAnnotation(checker.IMMUTABLE)) {
             checker.report(Result.failure("nonreadonly.call.on.rep",
                     calledMethod.getElement().getSimpleName().toString(),
                     calledMethodReceiver.getElement().getSimpleName().toString()), node);
         }
 
         /* Disallow passing references to 'this' as arguments of foreign methods. */
         if (state.isCurrentMethod(checker.ANONYMOUS)) {
             checkCallAnonymous(node);
         }
 
 
         if ((!TreeUtils.isSelfAccess(node)
                 || (receiver != null && !receiver.hasAnnotation(checker.THIS)))
                 && !(receiver != null && receiver.hasAnnotation(checker.REP))) {
             /* Check that @Rep objects are not passed to foreign methods */
             checkArgumentsRep(node.getArguments(), calledMethod.getParameterTypes());
         }
 
         state.setInvocationReceiver(receiver); /* Pass additional info to checkArguments */
         return super.visitMethodInvocation(node, p);
     }
 
     /**
      * Refine the required arguments based on the ownership of the current method receiver,
      * stored in the VisitorState
      *
      * @param requiredArgs
      * @param passedArgs
      * @param p
      */
     @Override
     protected void checkArguments(List<? extends AnnotatedTypeMirror> requiredArgs, List<? extends ExpressionTree> passedArgs, Void p) {
         List<AnnotatedTypeMirror> refinedRequiredArgs = new LinkedList<AnnotatedTypeMirror>();
         for (AnnotatedTypeMirror arg : requiredArgs) {
             AnnotatedTypeMirror refined = annoTypes.deepCopy(arg);
             if (refined.hasAnnotation(checker.PEER)) {
                 if (state.isReceiver(checker.REP)) {
                     refined.removeAnnotation(checker.PEER);
                     refined.addAnnotation(checker.REP);
                 } else if (!(state.isReceiver(checker.PEER))) {
                     refined.removeAnnotation(checker.PEER);
                     refined.addAnnotation(checker.WORLD);
                 }
             }
             refinedRequiredArgs.add(refined);
         }
         super.checkArguments(refinedRequiredArgs, passedArgs, p);
     }
 
 
 
     @Override
     public Void visitReturn(ReturnTree node, Void p) {
         if (state.isCurrentMethod(checker.ANONYMOUS)
                 && mayBeThis(node.getExpression())) {
             checker.report(Result.failure("anonymous.returns.this"), node);
         }
 
         /* Prohibit returning a @Rep object. This is only allowed in the case of private or
          * protected methods or ImmutableClasses. */
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getExpression());
         if (type.hasAnnotation(checker.REP)) {
             ExecutableElement currentMethod = state.getCurrentMethod().getElement();
             if (currentMethod.getModifiers().contains(Modifier.PUBLIC)
                     || state.getCurrentClass().hasAnnotation(checker.IMMUTABLE_CLASS)) {
                 checker.report(Result.failure("returning.rep", node.getExpression().toString()), node);
             }
         }
         return super.visitReturn(node, p);
     }
 
     @Override
     public Void visitNewArray(NewArrayTree node, Void p) {
         checkNewReferenceType(node.getType());
         checkNewReferenceFromDefaultConstructor(node.getType());
         return super.visitNewArray(node, p);
     }
 
     /**
      * Verify that no immutable reference is created using a non-@Anonymous
      * constructor while creating a new array
      * 
      * @param tree The array type specification tree.
      */
     protected void checkNewReferenceFromDefaultConstructor(Tree tree) {
         AnnotatedTypeMirror type = atypeFactory.fromTypeTree(tree);
         if (type.getKind() == TypeKind.DECLARED) {
             Element element = ((AnnotatedDeclaredType) type).getUnderlyingType().asElement();
             assert element.getKind() == ElementKind.CLASS : "Array initializer not a class";
             TypeElement typeElement = (TypeElement) element;
             if (type.hasAnnotation(checker.IMMUTABLE) && !isDefaultConstructorAnonymous(typeElement)) {
                 checker.report(Result.warning("immutable.untrusted.constructor"), tree);
             }
         } else if (type.getKind() == TypeKind.ARRAY) {
             ArrayTypeTree aTree = (ArrayTypeTree) tree;
             checkNewReferenceFromDefaultConstructor(aTree.getType());
         }
     }
 
     protected Boolean isDefaultConstructorAnonymous(TypeElement typeElement) {
         for (Element el : typeElement.getEnclosedElements()) {
             if (el.getKind() == ElementKind.CONSTRUCTOR) {
                 ExecutableElement constructor = (ExecutableElement) el;
                 if (constructor.getParameters().isEmpty()) {
                     return constructor.getAnnotation(Anonymous.class) != null;
                 }
             }
         }
         TypeMirror superclass = typeElement.getSuperclass();
         if (superclass.getKind() == TypeKind.NONE) {
             /* The constructor is Object.<init>(). It does nothing, so we
                assume that it is anonymous. */
             return true;
         } else if (superclass.getKind() == TypeKind.DECLARED) {
             Element superclassElement = ((DeclaredType) superclass).asElement();
             if (superclassElement.getKind() == ElementKind.CLASS) {
                 /* Proceed with the superclass */
                 return isDefaultConstructorAnonymous((TypeElement) superclassElement);
             } else {
                 throw new IllegalStateException("Superclass is not a class");
             }
         } else {
             throw new IllegalStateException("Superclass is not a DeclaredType or NoType");
         }
     }
 
     @Override
     public Void visitNewClass(NewClassTree node, Void p) {
         if (state.isCurrentMethod(checker.ANONYMOUS)) {
             /* Do not pass this to foreign constructors. */
             checkArgumentsAnonymous(node.getArguments());
         }
         AnnotatedExecutableType con = atypeFactory.getAnnotatedType(TreeUtils.elementFromUse(node));
         checkArgumentsRep(node.getArguments(), con.getParameterTypes());
         checkNewReferenceType(node.getIdentifier());
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getIdentifier());
         AnnotatedExecutableType constructor = atypeFactory.constructorFromUse(node);
         if (type.hasAnnotation(checker.IMMUTABLE) 
                 && constructor.getAnnotation(Anonymous.class) == null) {
             checker.report(Result.warning("immutable.untrusted.constructor"), node);
         }
         return super.visitNewClass(node, p);
     }
 
     /**
      * Check that the reference type is correct, i.e. that there are no
      * mutable references to objects of an ImmutableClass created.
      *
      * @param tree The Tree representing the declared type of the new object,
      */
     protected void checkNewReferenceType(Tree tree) {
         AnnotatedTypeMirror type = atypeFactory.fromTypeTree(tree);
         
         if (type.getKind() == TypeKind.DECLARED) {
             AnnotatedDeclaredType declaredType = (AnnotatedDeclaredType) type;
             Element typeElement = declaredType.getUnderlyingType().asElement();
             if (typeElement.getKind() == ElementKind.CLASS) {
                 if (typeElement.getAnnotation(ImmutableClass.class) != null
                         && !type.hasAnnotation(checker.IMMUTABLE)) {
                     /* Creating a mutable reference to an immutable object */
                     checker.report(Result.failure("mutable.reference.to.immutable.class"), tree);
                 }
             }
         } else if (type.getKind() == TypeKind.ARRAY) {
             ArrayTypeTree aTree = (ArrayTypeTree) tree;
             checkNewReferenceType(aTree.getType());
         }
     }
 
     @Override
     protected boolean checkMethodInvocability(AnnotatedExecutableType method, MethodInvocationTree node) {
         AnnotatedExecutableType mcp = (AnnotatedExecutableType) annoTypes.deepCopy(method);
         AnnotatedTypeMirror methodReceiver = mcp.getReceiverType().getErased();
         AnnotatedTypeMirror treeReceiver = methodReceiver.getCopy(false);
         treeReceiver.addAnnotations(atypeFactory.getReceiver(node).getAnnotations());
 
 //        System.err.println("Method receiver: " + methodReceiver.toString()
 //                + "Tree receiver: " + treeReceiver.toString());
 
         if (treeReceiver.hasAnnotation(checker.MUTABLE)
                 && methodReceiver.hasAnnotation(checker.IMMUTABLE)) {
             /* Allow for @ReadOnly calls on @Mutable references. */
             mcp.getReceiverType().removeAnnotation(checker.IMMUTABLE);
             mcp.getReceiverType().addAnnotation(checker.MUTABLE);
         } else if (treeReceiver.hasAnnotation(checker.IMMUTABLE) 
                 && methodReceiver.hasAnnotation(checker.MUTABLE)) {
             /* Disallow non-@Readonly calls on @Immutable */
             checker.report(Result.failure("immutable.calls.nonreadonly", mcp.getElement().toString(),
                     treeReceiver.toString()), node);
         }
         return super.checkMethodInvocability(mcp, node);
     }
 
     @Override
     public Void visitMemberSelect(MemberSelectTree node, Void p) {
         /* Prevent access to inner representation of a foreign object. */
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
         if (type.hasAnnotation(checker.REP)) {
             ExpressionTree expression = node.getExpression();
             AnnotatedTypeMirror selectType = atypeFactory.getAnnotatedType(expression);
             if (selectType.hasAnnotation(checker.MAYBE_THIS)
                     || selectType.hasAnnotation(checker.NOT_THIS)) {
                 checker.report(Result.failure("accessing.foreign.rep", node.toString()), node);
             }
         }
         return super.visitMemberSelect(node, p);
     }
 
     /**
      * Check that an assignment does not modify @Rep objects.
      */
     protected void checkAssignmentRep(AssignmentTree node, boolean receiverImmutable) {
         ExpressionTree varTree = node.getVariable();
         if (varTree instanceof ArrayAccessTree) {
             ArrayAccessTree t = (ArrayAccessTree) varTree;
             boolean dig = true;
             boolean rep = false;
             while (dig) {
                 ExpressionTree e = t.getExpression();
                 AnnotatedTypeMirror et = atypeFactory.getAnnotatedType(e);
 //                System.err.println("ARRAY: " + e.toString() + " : " + et.toString());
                 if (et.hasAnnotation(checker.IMMUTABLE)) {
                     String errorKey = rep
                             ? "assignment.to.rep.array.of.immutable"
                             : "assignment.to.field.of.immutable.array";
                     checker.report(Result.failure(errorKey, node.getVariable().toString(), e.toString()), varTree);
                     dig = false;
                 } else if (!(e instanceof ArrayAccessTree)) {
 //                    System.err.println((et.hasAnnotation(checker.REP) ? "yes " : " no")
 //                            + (TreeUtils.isSelfAccess(e) ? " yes" : " no")
 //                            + (receiverImmutable ? " yes" : " no"));
                     if (et.hasAnnotation(checker.REP)
                             && TreeUtils.isSelfAccess(e)
                             && receiverImmutable) {
                         String errorKey = rep
                                 ? "assignment.to.rep.array.of.rep.array.receiver"
                                 : "assignment.to.rep.array.of.receiver";
                         checker.report(Result.failure(errorKey, node.getVariable().toString(), t.toString()), varTree);
                     }
                     dig = false;
                 } else if (et.hasAnnotation(checker.REP)) {
                     rep = true;
                     t = (ArrayAccessTree) e;
                 } else {
                     dig = false;
                 }
             }
         } else {
             AnnotatedTypeMirror receiver = atypeFactory.getReceiver(varTree);
             if (receiver != null && receiver.hasAnnotation(checker.REP) && receiverImmutable) {
                 checker.report(Result.failure("assignment.to.field.of.rep",
                         varTree.toString(), receiver.getElement().toString()), node);
             }
         }
     }
 
     /**
      * Check an assignment within an @Anonymous method.
      * @param node
      */
     protected void checkAssignmentAnonymous(AssignmentTree node) {
         ExpressionTree ex = node.getExpression();
         ExpressionTree var = node.getVariable();
         if (mayBeThis(ex)) {
             try {
                 Element varElement = TreeUtils.elementFromUse(var);
                 if (varElement.getKind() == ElementKind.FIELD) {
                     checker.report(Result.failure("anonymous.assigns.this.to.field",
                             var.toString()), var);
                 }
             } catch (IllegalArgumentException e) {
                 /* Tree is not an element use */
                 if (var.getKind() == Tree.Kind.ARRAY_ACCESS) {
                     checker.report(Result.failure("anonymous.assigns.this.to.array.field",
                             var.toString()), var);
                 }
             }
         }
     }
 
     /**
      * Check a function call inside an @Anonymous method.
      * @param node
      */
     protected void checkCallAnonymous(MethodInvocationTree node) {
         checkArgumentsAnonymous(node.getArguments());
 
         /* Check that non-@Anonymous methods are not called on [this] */
         AnnotatedExecutableType method = atypeFactory.methodFromUse(node);
         if (!method.hasAnnotation(checker.ANONYMOUS)
                     && !isBaseConstructorCall(node)) {
             if (TreeUtils.isSelfAccess(node)) {
                 checker.report(Result.failure("anonymous.calls.non.anonymous", 
                         method.getElement().getSimpleName().toString(),
                         state.getCurrentMethodName()), node);
                 if (state.inImplicitlyAnnotatedMethod()) {
                     checker.note(null, "anonymous.implicit", 
                             state.getCurrentMethodName(), state.getCurrentClassName());
                 }
             } else {
                 ExpressionTree select = node.getMethodSelect();
                 if (select.getKind() == Tree.Kind.MEMBER_SELECT) {
                     MemberSelectTree selTree = (MemberSelectTree) select;
                     if (mayBeThis(selTree)) {
                         checker.report(Result.failure("anonymous.calls.non.anonymous.on.alias"), node);
                         if (state.inImplicitlyAnnotatedMethod()) {
                             checker.note(null, "anonymous.implicit", state.getCurrentMethodName());
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Validate arguments of a method/constructor call within an Anonymous
      * method by checking that they don't evaluate to a reference to this.
      *
      * @param args The list of arguments to be checked.
      */
     protected void checkArgumentsAnonymous(List<? extends ExpressionTree> args) {
         for (ExpressionTree arg : args) {
             if (mayBeThis(arg)) {
                 checker.report(Result.failure("argument.may.be.this", arg.toString()), arg);
                 if (state.inImplicitlyAnnotatedMethod()) {
                     checker.note(null, "anonymous.implicit",
                             state.getCurrentMethodName(), state.getCurrentClassName());
                 }
             }
         }
     }
 
     /**
      * Validate a call of a foreign method/constructor by checking that no
      * @Rep objects are passed as arguments.
      *
      * @param args The list of arguments to be checked.
      */
     protected void checkArgumentsRep(List<? extends ExpressionTree> args, List<AnnotatedTypeMirror> params) {
         Iterator<? extends ExpressionTree> ait = args.iterator();
         Iterator<AnnotatedTypeMirror> pit = params.iterator();
 
         Boolean safe = false;
         while (ait.hasNext()) {
             safe = pit.hasNext() ? pit.next().hasAnnotation(checker.SAFE) : safe; /* VarArgs possible */
             ExpressionTree a = ait.next();
             AnnotatedTypeMirror at = atypeFactory.getAnnotatedType(a);
             if (at.hasAnnotation(checker.REP) && !safe) {
                 checker.report(Result.failure("passing.rep.to.foreign.method", a.toString()), a);
             }
         }
     }
 
     /**
      * @param node a method call tree
      * @return true if the called method is the base constructor Object()
      */
     protected boolean isBaseConstructorCall(MethodInvocationTree node) {
         AnnotatedExecutableType method = atypeFactory.methodFromUse(node);
         ExecutableElement methodElement = method.getElement();
         //System.err.println("Base? " + methodElement.getEnclosingElement().getSimpleName().toString()
         //        + " " + methodElement.getSimpleName());
         if (methodElement.getEnclosingElement().getSimpleName().contentEquals("Object")
                 && methodElement.getSimpleName().contentEquals("<init>")) {
             return true;
         }
         return false;
     }
 
     public enum ThisReferenceSource {
         THIS_LITERAL {
             public void print(JimuvaChecker ch) {}
         },
         ALIAS {
             public void print(JimuvaChecker ch) {
                 ch.note(location, "this.ref.alias", aliasName);
             }
         },
         NONANONYMOUS_ON_THIS {
             public void print(JimuvaChecker ch) {
                 ch.note(null, "this.ref.nonanonymous.on.this", methodName);
             }
         },
         NONANONYMOUS_ON_ALIAS {
             public void print(JimuvaChecker ch) {
                 ch.note(null, "this.ref.nonanonymous.on.alias", methodName, aliasName);
                 innerReferenceSource.print(ch);
             }
         };
 
         String aliasName;
         String methodName;
         ThisReferenceSource innerReferenceSource;
         Tree location;
 
         public void setLocation(Tree t) { this.location = t; }
         public void setAliasName(String s) { this.aliasName = s; }
         public void setMethodName(String s) { this.methodName = s; }
         public void setInnerReferenceSource(ThisReferenceSource innerReferenceSource) {
             this.innerReferenceSource = innerReferenceSource;
         }
 
         public abstract void print(JimuvaChecker ch);
     }
 
     protected Boolean mayBeThis(ExpressionTree node) {
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
         return type.hasAnnotation(checker.THIS) || type.hasAnnotation(checker.MAYBE_THIS);
     }
 }
