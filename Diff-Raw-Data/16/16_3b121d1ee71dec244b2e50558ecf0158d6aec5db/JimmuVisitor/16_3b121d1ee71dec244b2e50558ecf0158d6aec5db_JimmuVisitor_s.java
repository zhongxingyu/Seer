 package checkers.jimmu;
 
 import checkers.basetype.BaseTypeVisitor;
 import checkers.jimmu.quals.Anonymous;
 import checkers.jimmu.quals.ImmutableClass;
 import checkers.source.Result;
 import checkers.types.AnnotatedTypeMirror;
 import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
 import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
 import checkers.util.ElementUtils;
 import checkers.util.InternalUtils;
 import checkers.util.TreeUtils;
 import com.sun.source.tree.ArrayAccessTree;
 import com.sun.source.tree.ArrayTypeTree;
 import com.sun.source.tree.AssignmentTree;
 import com.sun.source.tree.BlockTree;
 import com.sun.source.tree.ClassTree;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.ExpressionTree;
 import com.sun.source.tree.IdentifierTree;
 import com.sun.source.tree.MemberSelectTree;
 import com.sun.source.tree.MethodInvocationTree;
 import com.sun.source.tree.MethodTree;
 import com.sun.source.tree.NewArrayTree;
 import com.sun.source.tree.NewClassTree;
 import com.sun.source.tree.ReturnTree;
 import com.sun.source.tree.Tree;
 import com.sun.source.tree.VariableTree;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
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
  * The source code visitor for JimmuChecker.
  *
  * @author saf
  */
 public class JimmuVisitor extends BaseTypeVisitor<Void, Void> {
 
     protected JimmuChecker checker;
     protected JimmuAnnotatedTypeFactory atypeFactory;
     protected JimmuVisitorState state;
 
     public JimmuVisitor(JimmuChecker checker, CompilationUnitTree root, JimmuVisitorState state) throws IOException {
         super(checker, root);
         this.checker = checker;
         atypeFactory = new JimmuAnnotatedTypeFactory(checker, root, state);
         this.state = state;
     }
 
     protected enum Protection {
         NONE,        /* No protection */
         IMM,         /* Protecting @Immutable object */
         MYACC        /* Protecting @Myaccess object in @ReadOnly method */
     }
 
     protected Protection getProtection(AnnotatedTypeMirror t) {
         AnnotatedTypeMirror methodReceiver = state.getCurrentMethod().getReceiverType();
         if (t != null && t.hasAnnotation(checker.IMMUTABLE)) {
             return Protection.IMM;
         } else if (t != null && t.hasAnnotation(checker.MYACCESS)
                 && methodReceiver != null
                 && methodReceiver.hasAnnotation(checker.IMMUTABLE)) {
             return Protection.MYACC;
         } else {
             return Protection.NONE;
         }
     }
 
     @Override
     public Void visitAssignment(AssignmentTree node, Void p) {
         ExpressionTree varTree = node.getVariable();
         ExpressionTree valueTree = node.getExpression();
         AnnotatedTypeMirror valueType = atypeFactory.getAnnotatedType(valueTree);
 
         AnnotatedTypeMirror receiver = atypeFactory.getReceiver(varTree);
         Element varElement = InternalUtils.symbol(varTree);
 
         Protection prot = getProtection(receiver);
         /* Prohibit assignment to @Rep fields of @Immutable owners */
         if (receiver != null 
                 && prot != Protection.NONE
                 && varElement.getKind().isField()
                 && receiver.getKind() != TypeKind.ARRAY) {
             if (varElement.getKind() == ElementKind.FIELD) {
                 if (TreeUtils.isSelfAccess(varTree)) {
                     checker.report(Result.failure(prot == Protection.IMM 
                             ? "assign.readonly.receiver.field"
                             : "modifying.myaccess.in.readonly",
                             varTree.toString(), state.getCurrentMethodName()), node);
                     if (state.inImplicitlyAnnotatedMethod()) {
                         checker.note(null, "readonly.implicit",
                                 state.getCurrentMethodName(), state.getCurrentClassName());
                     }
                 } else {
                     checker.report(Result.failure("assign.immutable.field",
                             varTree.toString(), receiver.getElement().toString()), node);
                     if ((receiver.hasAnnotation(checker.PEER) || receiver.hasAnnotation(checker.OWNEDBY))
                             && state.isCurrentMethod(checker.READONLY)) {
                         checker.note(null, "immutable.implicit.on.encap.in.readonly");
                         if (state.inImplicitlyAnnotatedMethod()) {
                             checker.note(null, "readonly.implicit",
                                 state.getCurrentMethodName(), state.getCurrentClassName());
                         }
                     }
                 }
             }
         }
 
         AnnotatedTypeMirror methodReceiver = state.getCurrentMethod().getReceiverType();
         /* Check that @Rep values are not modified */
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
         AnnotatedExecutableType meth = state.getCurrentMethod();
         if (el.getKind().isField()
                 && el.getModifiers().contains(Modifier.PUBLIC)
                 && type.hasAnnotation(checker.REP)
                 && !type.hasAnnotation(checker.IMMUTABLE)) {
             checker.report(Result.failure("public.rep.field", el.getSimpleName()), node);
         }
         if (el.getKind().isField()
                 && state.isCurrentClass(checker.IMMUTABLE_CLASS)
                 && el.getModifiers().contains(Modifier.PUBLIC)
                 && !el.getModifiers().contains(Modifier.FINAL)) {
             checker.report(Result.failure("public.field.of.immutable.class",
                     el.getSimpleName(), state.getCurrentClassName()), el);
         }
 
         /* In static methods and constructors, prohibit the use of @Myaccess */
         if (meth != null 
                 && (meth.getReturnType() == null || meth.getElement().getModifiers().contains(Modifier.STATIC))
                 && type.hasAnnotation(checker.MYACCESS)) {
             checker.report(Result.failure("myaccess.variable.in.static.method",
                     node.getName().toString(), state.getCurrentMethodName()), node);
         }
 
         /* Prohibit @Myaccess static members */
         if (el.getKind().isField() && el.getModifiers().contains(Modifier.STATIC)
                 && type.hasAnnotation(checker.MYACCESS)) {
             checker.report(Result.failure("static.myaccess.field", node.getName().toString()), node);
         }
 
         /* Check ownership declaration */
         try {
             new Owner(el, atypeFactory); /* Constructor performs checking */
         } catch (Owner.OwnerDescriptionError err) {
             checker.report(err.getResult(), node);
         }
 
         /* Add variables to stack, so that they can be owners */
         if (el.getKind() == ElementKind.LOCAL_VARIABLE) {
             state.addVariable(el.getSimpleName().toString(), type);
         } else {
             state.shadowVariable(el.getSimpleName().toString());
         }
 
         return super.visitVariable(node, p);
     }
 
     @Override
     protected void commonAssignmentCheck(AnnotatedTypeMirror varType, AnnotatedTypeMirror valueType, Tree valueTree, String errorKey, Void p) {
         AnnotatedTypeMirror varCopy = annoTypes.deepCopy(varType);
 
         /* Generally, an object cannot lose or gain the @Immutable annotation. This is
          * enforced because @Immutable and @Mutable are uncomparable. However, the user may
          * want to allow for upcasting @Mutable values to @Immutable references using the
          * "allow.upcast" option. */
         if (checker.allowUpcast() && varCopy.hasAnnotation(checker.IMMUTABLE)
                 && valueType.hasAnnotation(checker.MUTABLE)) {
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
 
         /* Static methods and constructors cannot have @Myaccess parameters
          * or return a @Myaccess result */
         AnnotatedExecutableType type = atypeFactory.getAnnotatedType(node);
         ExecutableElement elt = TreeUtils.elementFromDeclaration(node);
         if (elt.getModifiers().contains(Modifier.STATIC) || type.getReturnType() == null) {
             if (type.getReturnType() != null
                     && type.getReturnType().hasAnnotation(checker.MYACCESS)) {
                 checker.report(Result.failure("static.method.returns.myaccess",
                         node.getName()), node);
             }
             for (AnnotatedTypeMirror pt : type.getParameterTypes()) {
                 if (pt.hasAnnotation(checker.MYACCESS)) {
                     checker.report(Result.failure(type.getReturnType() == null
                             ? "constructor.myaccess.parameter"
                             : "static.method.myaccess.parameter",
                             node.getName()), node);
                 }
             }
         }
         
         state.enterMethod(node);
 
         try {
             /* Step into the method */
             return super.visitMethod(node, p);
         } finally {
             state.leaveMethod();
         }
     }
 
     @Override
     public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
         AnnotatedExecutableType calledMethod = atypeFactory.methodFromUse(node);
         AnnotatedTypeMirror receiver = atypeFactory.getReceiver(node);
         AnnotatedTypeMirror calledMethodReceiver = calledMethod.getReceiverType();
         ExpressionTree methodSelect = node.getMethodSelect();
         String methodName;
         if (methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
             methodName = ((MemberSelectTree) methodSelect).getIdentifier().toString();
         } else {
             methodName = node.toString();
         }
 
         /*
          * When inside a @ReadOnly method, we cannot modify @Myaccess objects, as the access 
          * rights variable could be instantiated to @Immutable.
          */
         if (state.isCurrentMethod(checker.READONLY) && receiver != null
             && !calledMethod.hasAnnotation(checker.READONLY)) {
             if (receiver.hasAnnotation(checker.MYACCESS)) {
                 checker.report(Result.failure("nonreadonly.call.on.myaccess.in.readonly",
                     methodName,
                     receiver.getElement().getSimpleName().toString()), node);
             } else if (receiver.hasAnnotation(checker.REP)) {
                 checker.report(Result.failure("nonreadonly.call.on.rep",
                     methodName,
                     receiver.getElement().getSimpleName().toString()), node);
             }
         }
 
         /* Disallow passing references to 'this' as arguments of foreign methods.
          * and calling non-@Anonymous methods on this */
         if (state.isCurrentMethod(checker.ANONYMOUS) && receiver != null) {
             if (mayBeThis(receiver)) {
                 checkCallAnonymous(node);
             } else if (mayBeForeign(receiver)) {
                 checkArgumentsAnonymous(node.getArguments());
             }
         }
 
         if ((!TreeUtils.isSelfAccess(node) && (receiver != null && !receiver.hasAnnotation(checker.THIS)))) {
             /* Check that encapsulated objects are not passed to foreign methods */
             checkArgumentsEncap(node.getArguments(), calledMethod.getParameterTypes(), receiver);
         }
 
         if (receiver != null && receiver.hasAnnotation(checker.SAFE)
                 && calledMethodReceiver != null && !calledMethodReceiver.hasAnnotation(checker.SAFE)) {
            checker.report(Result.failure("unsafe.call.on.safe.value", receiver), node);
         }
         checkArgumentsSafe(node.getArguments(), calledMethod.getParameterTypes());
 
         state.setCurrentInvocation(node); /* Pass additional info to checkArguments */
         return super.visitMethodInvocation(node, p);
     }
 
     /**
      * Refine the required arguments based on the ownership of the current method receiver,
      * stored in the JimmuVisitorState
      */
     @Override
     protected void checkArguments(List<? extends AnnotatedTypeMirror> requiredArgs, List<? extends ExpressionTree> passedArgs, Void p) {
         List<AnnotatedTypeMirror> refinedRequiredArgs = new LinkedList<AnnotatedTypeMirror>();
         MethodInvocationTree invocation = state.getCurrentInvocation();
         for (AnnotatedTypeMirror arg : requiredArgs) {
             AnnotatedTypeMirror refined = annoTypes.deepCopy(arg);
             
             /* Resolve @Myaccess annotations on arguments to the access rights of receiver */
             if (refined.hasAnnotation(checker.MYACCESS)) {
                 if (state.isReceiver(checker.IMMUTABLE)) {
                     refined.removeAnnotation(checker.MYACCESS);
                     refined.addAnnotation(checker.IMMUTABLE);
                 } else if (state.isReceiver(checker.MUTABLE)) {
                     refined.removeAnnotation(checker.MYACCESS);
                     refined.addAnnotation(checker.MUTABLE);
                 }
             }
 
             /* Resolve @Peer annotations on arguments relative to the receiver */
             if (invocation != null && refined.hasAnnotation(checker.REP)) {
                 if (!TreeUtils.isSelfAccess(invocation)) {
                     MemberSelectTree methodSelect = (MemberSelectTree) invocation.getMethodSelect();
                     refined.removeAnnotation(checker.REP);
                     Owner rcv = new Owner(methodSelect.getExpression(), atypeFactory);
                     refined.addAnnotation(atypeFactory.ownerAnnotation(rcv.asString()));
                 }
             } else if (refined.hasAnnotation(checker.PEER)) {
                 if (state.isReceiver(checker.REP)) {
                     refined.removeAnnotation(checker.PEER);
                     refined.addAnnotation(checker.REP);
                 } else if (state.isReceiver(checker.OWNEDBY)) {
                     Owner rcvOwner = state.getReceiverOwner();
                     refined.removeAnnotation(checker.PEER);
                     if (rcvOwner != null) {
                         refined.addAnnotation(atypeFactory.ownerAnnotation(rcvOwner.asString()));
                     } else {
                         /* Possible on error in owner description. Do not produce spurious errors
                         stemming from there. */
                         refined.addAnnotation(checker.ANYOWNER); /* Suppress errors */
                     }
                 } else if (!(state.isReceiver(checker.PEER))) {
                     refined.removeAnnotation(checker.PEER);
                     refined.addAnnotation(checker.WORLD);
                 }
             } else if (invocation != null && refined.hasAnnotation(checker.OWNEDBY)
                     && !TreeUtils.isSelfAccess(invocation)) {
                 Owner desiredOwner = new Owner(refined.getElement(), atypeFactory);
                 MemberSelectTree recvSelect = (MemberSelectTree) invocation.getMethodSelect();
                 Owner rcv = new Owner(recvSelect.getExpression(), atypeFactory);
                 rcv.append(desiredOwner);
                 refined.removeAnnotation(checker.OWNEDBY);
                 refined.addAnnotation(atypeFactory.ownerAnnotation(rcv.asString()));
             }
             refinedRequiredArgs.add(refined);
         }
 
 
         /* Check that, inside a read-only method, no @Rep value is passed
          * as a non-immutable parameter to a method or constructor */
         Iterator<? extends ExpressionTree> ait = passedArgs.iterator();
         Iterator<AnnotatedTypeMirror> pit = refinedRequiredArgs.iterator();
         AnnotatedTypeMirror paramType = null;
         while (ait.hasNext()) {
             paramType = pit.hasNext() ? pit.next() : paramType; /* Handling varArgs */
             ExpressionTree a = ait.next();
             AnnotatedTypeMirror at = atypeFactory.getAnnotatedType(a);
 
             if (state.isCurrentMethod(checker.READONLY)
                 && at.hasAnnotation(checker.REP)
                 && !paramType.hasAnnotation(checker.IMMUTABLE)) {
                 checker.report(Result.failure("passing.rep.in.readonly", a.toString()), a);
             }
         }
 
         super.checkArguments(refinedRequiredArgs, passedArgs, p);
     }
 
     @Override
     public Void visitReturn(ReturnTree node, Void p) {
         if (state.isCurrentMethod(checker.ANONYMOUS)
                 && mayBeThis(node.getExpression())) {
             checker.report(Result.failure("anonymous.returns.this"), node);
         }
 
         /* Prohibit returning a read-write reference to a @Rep object. */
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getExpression());
         if (type.hasAnnotation(checker.REP)
                 && !type.hasAnnotation(checker.IMMUTABLE)) {
             checker.report(Result.failure("returning.rep", node.getExpression().toString()), node);
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
             /* Do not pass 'this' to foreign constructors. */
             checkArgumentsAnonymous(node.getArguments());
         }
         AnnotatedTypeMirror type = atypeFactory.fromTypeTree(node.getIdentifier());
         AnnotatedExecutableType con = atypeFactory.getAnnotatedType(TreeUtils.elementFromUse(node));
         checkArgumentsEncap(node.getArguments(), con.getParameterTypes(), type);
         checkNewReferenceType(node.getIdentifier());
         AnnotatedExecutableType constructor = atypeFactory.constructorFromUse(node);
         if (type.hasAnnotation(checker.IMMUTABLE)
                 && constructor.getAnnotation(Anonymous.class) == null) {
             checker.report(Result.warning("immutable.untrusted.constructor"), node);
         }
 
         state.setCurrentInvocation(node);
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
 
         if (treeReceiver.hasAnnotation(checker.MUTABLE)
                 && mcp.hasAnnotation(checker.READONLY)) {
             /* Allow for @ReadOnly calls on @Mutable references. */
             mcp.getReceiverType().removeAnnotation(checker.IMMUTABLE);
             mcp.getReceiverType().addAnnotation(checker.MUTABLE);
         } else if (treeReceiver.hasAnnotation(checker.IMMUTABLE)
                 && !mcp.hasAnnotation(checker.READONLY)) {
             /* Disallow non-@Readonly calls on @Immutable */
             checker.report(Result.failure("immutable.calls.nonreadonly", mcp.getElement().toString()), node);
             if ((treeReceiver.hasAnnotation(checker.PEER) || treeReceiver.hasAnnotation(checker.OWNEDBY))
                     && state.isCurrentMethod(checker.READONLY)) {
                 checker.note(null, "immutable.implicit.on.encap.in.readonly");
             }
         }
         return super.checkMethodInvocability(mcp, node);
     }
 
     @Override
     public Void visitMemberSelect(MemberSelectTree node, Void p) {
         /* Prevent access to inner representation of a foreign object. */
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
         if (type.hasAnnotation(checker.REP)
                 && !type.hasAnnotation(checker.IMMUTABLE)) {
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
                 if (et.hasAnnotation(checker.IMMUTABLE)) {
                     String errorKey = rep
                             ? "assignment.to.rep.array.of.immutable"
                             : "assignment.to.field.of.immutable.array";
                     checker.report(Result.failure(errorKey, node.getVariable().toString(), e.toString()), varTree);
                     dig = false;
                 } else if (!(e instanceof ArrayAccessTree)) {
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
      */
     protected void checkCallAnonymous(MethodInvocationTree node) {
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
                 /* Calling via reference */
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
      * @Rep or @Peer objects are passed as arguments. They may only be passed
      * as @Immutable references, @Safe values or to methods whose receivers
      * are @Rep or @Peer.
      *
      * @param args The list of arguments to be checked.
      * @param params The list of
      * @param receiver The type of the method's receiver.
      */
     protected void checkArgumentsEncap(List<? extends ExpressionTree> args,
             List<AnnotatedTypeMirror> params, AnnotatedTypeMirror receiver) {
         Iterator<? extends ExpressionTree> ait = args.iterator();
         Iterator<AnnotatedTypeMirror> pit = params.iterator();
 
         AnnotatedTypeMirror paramType = null;
         while (ait.hasNext()) {
             paramType = pit.hasNext() ? pit.next() : paramType; /* Handling varArgs */
             ExpressionTree a = ait.next();
             AnnotatedTypeMirror at = atypeFactory.getAnnotatedType(a);
             if ((receiver == null || !receiver.hasAnnotation(checker.REP))
                     && at.hasAnnotation(checker.REP)
                     && !paramType.hasAnnotation(checker.IMMUTABLE)
                     && !paramType.hasAnnotation(checker.SAFE)) {
                 checker.report(Result.failure("passing.rep.to.foreign.method", a.toString()), a);
             } else if ((receiver == null || (!receiver.hasAnnotation(checker.PEER) && !receiver.hasAnnotation(checker.REP)))
                     && at.hasAnnotation(checker.PEER)
                     && !paramType.hasAnnotation(checker.IMMUTABLE)
                     && !paramType.hasAnnotation(checker.SAFE)) {
                 checker.report(Result.failure("passing.peer.to.foreign.method", a.toString()), a);
             }
         }
     }
 
     /**
      * Check that @Safe values are not passed as unsafe parameters
      */
     protected void checkArgumentsSafe(List<? extends ExpressionTree> args, List<AnnotatedTypeMirror> params) {
         Iterator<? extends ExpressionTree> ait = args.iterator();
         Iterator<AnnotatedTypeMirror> pit = params.iterator();
 
         Boolean safe = false;
         while (ait.hasNext()) {
             safe = pit.hasNext() ? pit.next().hasAnnotation(checker.SAFE) : safe;
             ExpressionTree a = ait.next();
             AnnotatedTypeMirror at = atypeFactory.getAnnotatedType(a);
             if (at.hasAnnotation(checker.SAFE) && !safe) {
                 checker.report(Result.failure("passing.safe.to.unsafe.parameter", a.toString()), a);
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
         if (methodElement.getEnclosingElement().getSimpleName().contentEquals("Object")
                 && methodElement.getSimpleName().contentEquals("<init>")) {
             return true;
         }
         return false;
     }
 
     @Override
     public Void visitBlock(BlockTree node, Void p) {
         try {
             state.enterBlock();
             return super.visitBlock(node, p);
         } finally {
             state.leaveBlock();
         }
     }
 
     protected Boolean mayBeThis(ExpressionTree node) {
         AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
         return mayBeThis(type);
     }
 
     protected Boolean mayBeThis(AnnotatedTypeMirror type) {
         return type.hasAnnotation(checker.THIS) || type.hasAnnotation(checker.MAYBE_THIS);
     }
 
     protected Boolean mayBeForeign(AnnotatedTypeMirror type) {
         return type.hasAnnotation(checker.MAYBE_THIS) || type.hasAnnotation(checker.NOT_THIS);
     }
 
     /** 
      * Represents information on an object's owner, based on the @OwnedBy annotation.
      */
     public static class Owner {
 
         Element element;
         List<PathStep> path;
         JimmuAnnotatedTypeFactory af;
 
         public class OwnerDescriptionError extends RuntimeException {
 
             Result result;
 
             public OwnerDescriptionError(Result r) {
                 super();
                 result = r;
             }
 
             public Result getResult() {
                 return result;
             }
         }
 
         protected static class PathStep {
 
             public static enum PathStepKind {
 
                 CLASS, FIELD, LOCAL, PARAMETER, UNKNOWN
             }
             public String name;
             public PathStepKind kind;
             public Boolean isStatic;
             public AnnotatedTypeMirror type; /* Null in CLASS steps */
 
 
             public PathStep(String name, PathStepKind kind, Boolean isStatic, AnnotatedTypeMirror type) {
                 this.name = name;
                 this.kind = kind;
                 this.isStatic = isStatic;
                 this.type = type;
             }
         }
 
         public Owner(Element elt, JimmuAnnotatedTypeFactory af) throws OwnerDescriptionError {
             this.af = af;
             this.element = elt;
 
             if (element == null) {
                 /* This kind of error should never be reported */
                 throw new OwnerDescriptionError(Result.failure("owner.must.be.chain.of.identifiers"));
             }
 
             String owner;
             if (element.getKind() == ElementKind.METHOD) {
                 AnnotatedExecutableType type = (AnnotatedExecutableType) af.getAnnotatedType(element);
                 owner = af.getOwner(type.getReturnType());
             } else {
                 owner = af.getOwner(element);
             }
 
             if (owner != null) {
                 path = new LinkedList<PathStep>();
                 List<String> parts = Arrays.asList(owner.split("\\."));
                 List<String> significant = new LinkedList<String>();
 
                 for (String p : parts) {
                     if (!isValidName(p)) {
                         throw new OwnerDescriptionError(Result.failure("owner.invalid.identifier", p));
                     } else if (!"this".equals(p)) {
                         significant.add(p);
                     }
                 }
                 constructPath(significant);
             }
         }
 
         /**
          * Create Owner from a given [ExpressionTree]. We will only handle
          * simple-formed MemberSelectTrees, which contain an IdentifierTree at the end
          * of the chain.
          */
         public Owner(ExpressionTree tree, JimmuAnnotatedTypeFactory af) {
             this.af = af;
             path = new LinkedList<PathStep>();
 
             Tree t = tree;
             while (t.getKind() == Tree.Kind.MEMBER_SELECT) {
                 MemberSelectTree mst = (MemberSelectTree) t;
                 AnnotatedTypeMirror type = af.getAnnotatedType(t);
                 path.add(new PathStep(mst.getIdentifier().toString(),
                         type.getElement().getKind() == ElementKind.CLASS
                         ? PathStep.PathStepKind.CLASS
                         : type.getElement().getKind() == ElementKind.LOCAL_VARIABLE
                         ? PathStep.PathStepKind.LOCAL
                         : PathStep.PathStepKind.FIELD,
                         type.getElement().getModifiers().contains(Modifier.STATIC), type));
                 t = TreeUtils.skipParens(mst.getExpression());
             }
             if (t.getKind() == Tree.Kind.IDENTIFIER) {
                 IdentifierTree lastId = (IdentifierTree) t;
                 AnnotatedTypeMirror type = af.getAnnotatedType(t);
                 if (!"this".equals(lastId.getName().toString())) {
                     path.add(new PathStep(lastId.getName().toString(),
                             type.getElement().getKind() == ElementKind.CLASS
                             ? PathStep.PathStepKind.CLASS
                             : type.getElement().getKind() == ElementKind.LOCAL_VARIABLE
                             ? PathStep.PathStepKind.LOCAL
                             : PathStep.PathStepKind.FIELD,
                             type.getElement().getModifiers().contains(Modifier.STATIC), type));
                 }
             } else {
                 /* Kind of hack... If the MemberSelect is of
                  * non-identifier-sequence form, e.g.
                  *
                  *    (new X()).z
                  *
                  * then return _ to mark ownership by an unknown object.
                  * Naturally, the returned OwnedBy annotation will not be a subclass
                  * of any valid OwnedBy annotation.
                  */
                 path.add(new PathStep(null, PathStep.PathStepKind.UNKNOWN, false, null));
             }
             Collections.reverse(path);
         }
 
         private Boolean isCapitalized(String s) {
             return s.matches("^[A-Z][A-Za-z0-9_]*$");
         }
 
         private Boolean isValidName(String s) {
             return s.matches("^[A-Za-z][A-Za-z0-9_]*$");
         }
 
         protected AnnotatedTypeMirror findMember(TypeElement t, String f,
                 Boolean mustBeStatic, Boolean mustBePublic) {
             for (Element e : t.getEnclosedElements()) {
                 if (e.getSimpleName().toString().equals(f)) {
                     if (mustBeStatic && !e.getModifiers().contains(Modifier.STATIC)) {
                         throw new OwnerDescriptionError(Result.failure("owner.nonstatic.member", f));
                     } else if (mustBePublic && !e.getModifiers().contains(Modifier.PUBLIC)) {
                         throw new OwnerDescriptionError(Result.failure("owner.nonpublic.member", f));
                     } else {
                         return af.getAnnotatedType(e);
                     }
                 }
             }
             TypeMirror s = t.getSuperclass();
             if (s.getKind() == TypeKind.NONE) {
                 return null; /* Top of hierarchy, nothing found */
             } else if (s.getKind() == TypeKind.DECLARED) {
                 Element superclassElement = ((DeclaredType) s).asElement();
                 return findMember(((TypeElement) superclassElement), f, mustBeStatic, mustBePublic);
             } else {
                 throw new IllegalStateException("Superclass is not a class");
             }
         }
 
         protected void addMembers(List<String> p, TypeMirror t, Boolean insideClass) {
             /* Assuming p is not empty! */
 
             String f = p.remove(0);
             if (t.getKind() == TypeKind.DECLARED) {
                 TypeElement te = (TypeElement) ((DeclaredType) t).asElement();
                 AnnotatedTypeMirror am = findMember(te, f, insideClass, true);
                 if (am != null) {
                     if (am.getElement().getKind() == ElementKind.CLASS) {
                         path.add(new PathStep(f, PathStep.PathStepKind.CLASS,
                                 am.getElement().getModifiers().contains(Modifier.STATIC), null));
                         if (p.isEmpty()) {
                             throw new OwnerDescriptionError(Result.failure("owner.class.cannot.own"));
                         } else {
                             addMembers(p, am.getElement().asType(), true);
                         }
                     } else {
                         path.add(new PathStep(f, PathStep.PathStepKind.FIELD,
                                 am.getElement().getModifiers().contains(Modifier.STATIC), am));
                         if (!p.isEmpty()) {
                             addMembers(p, am.getUnderlyingType(), false);
                         }
                     }
                 } else {
                     throw new OwnerDescriptionError(Result.failure("owner.no.such.field", f, am.getUnderlyingType().toString()));
                 }
             } else if (t.getKind() == TypeKind.WILDCARD || t.getKind() == TypeKind.TYPEVAR) {
                 throw new OwnerDescriptionError(Result.failure("owner.peeking.unsupported"));
             } else {
                 throw new OwnerDescriptionError(Result.failure("owner.simple.type"));
             }
         }
 
         public void constructPath(List<String> p) throws OwnerDescriptionError {
             Element enclosing = element.getKind() == ElementKind.PARAMETER
                     ? ElementUtils.enclosingClass(element)
                     : element.getEnclosingElement();
 
             List<String> rest = new LinkedList<String>(p);
             String base = rest.remove(0);
             Boolean found = false;
 
             /* Try to find base among local variables. */
             AnnotatedTypeMirror localType = af.checker.getState().localVariable(base);
             if (localType != null) {
                 found = true;
                 path.add(new PathStep(base, PathStep.PathStepKind.LOCAL, false, localType));
                 if (!rest.isEmpty()) {
                     addMembers(rest, localType.getUnderlyingType(), false);
                 }
             }
 
             do {
                 /*
                  * Traverse elements enclosing [element] to find one that
                  * contains the field f accessible from [element]
                  */
                 if (enclosing.getKind() == ElementKind.METHOD) {
                     ExecutableElement m = (ExecutableElement) enclosing;
                     for (VariableElement v : m.getParameters()) {
                         if (v.getSimpleName().toString().equals(base)) {
                             found = true;
                             AnnotatedTypeMirror am = af.getAnnotatedType(v);
                             path.add(new PathStep(base, PathStep.PathStepKind.PARAMETER, false, am));
                             if (!rest.isEmpty()) {
                                 addMembers(rest, am.getUnderlyingType(), false);
                             }
                         }
                     }
                 } else if (enclosing.getKind() == ElementKind.CLASS) {
                     TypeElement t = (TypeElement) enclosing;
                     AnnotatedTypeMirror ft = findMember(t, base, false, false);
                     if (ft != null) {
                         found = true;
                         if (ft.getElement().getKind() == ElementKind.CLASS) {
                             if (rest.isEmpty()) {
                                 throw new OwnerDescriptionError(Result.failure("owner.class.cannot.own"));
                             } else {
                                 path.add(new PathStep(base, PathStep.PathStepKind.CLASS,
                                         ft.getElement().getModifiers().contains(Modifier.STATIC), null));
                                 addMembers(rest, ft.getElement().asType(), true);
                             }
                         } else { /* Field */
                             path.add(new PathStep(base, PathStep.PathStepKind.FIELD,
                                     ft.getElement().getModifiers().contains(Modifier.STATIC), ft));
                             if (!rest.isEmpty()) {
                                 addMembers(rest, ft.getUnderlyingType(), false);
                             }
                         }
                     }
                 }
                 enclosing = enclosing.getEnclosingElement();
             } while (!found && enclosing.getKind() != ElementKind.PACKAGE);
 
             if (!found) {
                 throw new OwnerDescriptionError(Result.failure("owner.no.such.field", base, element.toString()));
             }
         }
 
         public void append(Owner owner) {
             for (PathStep s : owner.path) {
                 path.add(s);
             }
         }
 
         public Boolean isImmutable() {
             return path != null && path.get(path.size() - 1).type.hasAnnotation(af.checker.IMMUTABLE);
         }
 
         public Boolean isMyaccess() {
             return path != null && path.get(path.size() - 1).type.hasAnnotation(af.checker.MYACCESS);
         }
 
         public Boolean isFullyStatic() {
             if (path == null) {
                 return null;
             } else {
                 for (PathStep ps : path) {
                     if (!ps.isStatic) {
                         return false;
                     }
                 }
                 return true;
             }
         }
 
         public String asString() {
             StringBuilder b = new StringBuilder();
             for (PathStep s : path) {
                 b.append(s.name + ".");
             }
             b.deleteCharAt(b.length() - 1);
             return b.toString();
         }
 
         @Override
         public String toString() {
             if (path == null) {
                 return "[null path]";
             } else {
                 StringBuilder builder = new StringBuilder("[");
                 for (PathStep s : path) {
                     builder.append(s.name + ", ");
                 }
                 builder.append("]");
                 return builder.toString();
             }
         }
     }
 }
