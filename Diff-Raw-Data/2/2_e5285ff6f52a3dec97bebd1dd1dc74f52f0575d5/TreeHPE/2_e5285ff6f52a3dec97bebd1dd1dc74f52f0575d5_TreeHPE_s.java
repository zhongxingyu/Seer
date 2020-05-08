 package org.civet;
 
 import java.util.Iterator;
 
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.Modifier;
 
 import org.civet.HPEObject.Tag;
 
 import com.sun.source.tree.AnnotationTree;
 import com.sun.source.tree.ArrayAccessTree;
 import com.sun.source.tree.ArrayTypeTree;
 import com.sun.source.tree.AssertTree;
 import com.sun.source.tree.AssignmentTree;
 import com.sun.source.tree.BinaryTree;
 import com.sun.source.tree.BlockTree;
 import com.sun.source.tree.BreakTree;
 import com.sun.source.tree.CaseTree;
 import com.sun.source.tree.CatchTree;
 import com.sun.source.tree.ClassTree;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.CompoundAssignmentTree;
 import com.sun.source.tree.ConditionalExpressionTree;
 import com.sun.source.tree.ContinueTree;
 import com.sun.source.tree.DoWhileLoopTree;
 import com.sun.source.tree.EmptyStatementTree;
 import com.sun.source.tree.EnhancedForLoopTree;
 import com.sun.source.tree.ErroneousTree;
 import com.sun.source.tree.ExpressionStatementTree;
 import com.sun.source.tree.ExpressionTree;
 import com.sun.source.tree.ForLoopTree;
 import com.sun.source.tree.IdentifierTree;
 import com.sun.source.tree.IfTree;
 import com.sun.source.tree.ImportTree;
 import com.sun.source.tree.InstanceOfTree;
 import com.sun.source.tree.LabeledStatementTree;
 import com.sun.source.tree.LiteralTree;
 import com.sun.source.tree.MemberSelectTree;
 import com.sun.source.tree.MethodInvocationTree;
 import com.sun.source.tree.MethodTree;
 import com.sun.source.tree.ModifiersTree;
 import com.sun.source.tree.NewArrayTree;
 import com.sun.source.tree.NewClassTree;
 import com.sun.source.tree.ParameterizedTypeTree;
 import com.sun.source.tree.ParenthesizedTree;
 import com.sun.source.tree.PrimitiveTypeTree;
 import com.sun.source.tree.ReturnTree;
 import com.sun.source.tree.SwitchTree;
 import com.sun.source.tree.SynchronizedTree;
 import com.sun.source.tree.ThrowTree;
 import com.sun.source.tree.Tree;
 import com.sun.source.tree.Tree.Kind;
 import com.sun.source.tree.TryTree;
 import com.sun.source.tree.TypeCastTree;
 import com.sun.source.tree.TypeParameterTree;
 import com.sun.source.tree.UnaryTree;
 import com.sun.source.tree.UnionTypeTree;
 import com.sun.source.tree.VariableTree;
 import com.sun.source.tree.WhileLoopTree;
 import com.sun.source.tree.WildcardTree;
 import com.sun.source.util.TreeScanner;
 import com.sun.tools.javac.code.Flags;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Symbol.ClassSymbol;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
 import com.sun.tools.javac.tree.JCTree.JCAssign;
 import com.sun.tools.javac.tree.JCTree.JCAssignOp;
 import com.sun.tools.javac.tree.JCTree.JCBinary;
 import com.sun.tools.javac.tree.JCTree.JCBlock;
 import com.sun.tools.javac.tree.JCTree.JCCase;
 import com.sun.tools.javac.tree.JCTree.JCClassDecl;
 import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
 import com.sun.tools.javac.tree.JCTree.JCConditional;
 import com.sun.tools.javac.tree.JCTree.JCExpression;
 import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
 import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
 import com.sun.tools.javac.tree.JCTree.JCForLoop;
 import com.sun.tools.javac.tree.JCTree.JCIdent;
 import com.sun.tools.javac.tree.JCTree.JCIf;
 import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
 import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
 import com.sun.tools.javac.tree.JCTree.JCLiteral;
 import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
 import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
 import com.sun.tools.javac.tree.JCTree.JCNewArray;
 import com.sun.tools.javac.tree.JCTree.JCNewClass;
 import com.sun.tools.javac.tree.JCTree.JCParens;
 import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
 import com.sun.tools.javac.tree.JCTree.JCReturn;
 import com.sun.tools.javac.tree.JCTree.JCStatement;
 import com.sun.tools.javac.tree.JCTree.JCSwitch;
 import com.sun.tools.javac.tree.JCTree.JCTry;
 import com.sun.tools.javac.tree.JCTree.JCTypeApply;
 import com.sun.tools.javac.tree.JCTree.JCTypeCast;
 import com.sun.tools.javac.tree.JCTree.JCUnary;
 import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
 import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
 import com.sun.tools.javac.tree.TreeInfo;
 import com.sun.tools.javac.util.List;
 import com.sun.tools.javac.util.ListBuffer;
 import com.sun.tools.javac.util.Pair;
 
 public class TreeHPE extends TreeScanner<HPEResult, HPEContext> {
 
   private JCCompilationUnit currentCompilationUnit;
 
   @Override
   public HPEResult visitCompilationUnit(CompilationUnitTree node, HPEContext p) {
     JCCompilationUnit compilationUnit = (JCCompilationUnit) node;
    ListBuffer<JCTree> l = new ListBuffer<>();
     JCCompilationUnit newCompilationUnit = p.treeMaker.TopLevel(
         compilationUnit.packageAnnotations, compilationUnit.pid,
         List.<JCTree> nil());
     currentCompilationUnit = compilationUnit;
     for (Tree t : node.getTypeDecls()) {
       HPEResult r = t.accept(this, p);
       l.append((JCTree) r.code);
     }
     newCompilationUnit.defs = newCompilationUnit.defs.appendList(l);
     return new HPEResult(newCompilationUnit, HPEObject.Top);
   }
 
   @Override
   public HPEResult visitImport(ImportTree node, HPEContext p) {
     return new HPEResult(node, HPEObject.Top);
   }
 
   @Override
   public HPEResult visitClass(ClassTree node, HPEContext p) {
     JCClassDecl classDecl = (JCClassDecl) node;
     List<JCMethodDecl> methodsToHPE = HPEUtil.findTaggedMethods(classDecl);
     for (JCMethodDecl m : methodsToHPE) {
       m.accept(this, p);
     }
     return new HPEResult(node, new HPEObject.Bottom()); // no specialization
   }
 
   @Override
   public HPEResult visitMethod(MethodTree node, HPEContext p) {
     JCMethodDecl methodDecl = (JCMethodDecl) node;
     HPEObject thiz = new CLASS((ClassSymbol) methodDecl.sym.owner)
         .setTag(Tag.ABSTRACT);
     p.env.inStack(thiz);
     /* setting all the parameters to Top in env */
     for (JCVariableDecl vd : methodDecl.params) {
       p.env.add(HPESymbol.local(vd.sym.name), HPEObject.Top);
     }
     HPEResult result = scan(node.getBody(), p);
     p.env.outStack();
     methodDecl.body = (JCBlock) result.code;
     return new HPEResult(methodDecl, HPEObject.Top);
   }
 
   @Override
   public HPEResult visitVariable(VariableTree node, HPEContext p) {
     JCVariableDecl variableDecl = (JCVariableDecl) node;
     if (variableDecl.init == null) {
       p.env.add(HPESymbol.local(variableDecl.sym.name), new HPEObject.Bottom());
       return new HPEResult(variableDecl, new HPEObject.Bottom());
     } else {
       HPEResult initResult = variableDecl.getInitializer().accept(this, p);
       p.env.add(HPESymbol.local(variableDecl.sym.name), initResult.value());
       if (initResult.value().isConcrete()) {
         return new HPEResult(p.treeMaker.Skip(), new HPEObject.Bottom());
       } else {
         assert (initResult.code instanceof JCExpression);
         JCVariableDecl newVariableDecl = p.treeMaker.VarDef(variableDecl.mods,
             variableDecl.name, variableDecl.vartype,
             (JCExpression) initResult.code);
         return new HPEResult(newVariableDecl, new HPEObject.Bottom());
       }
     }
   }
 
   @Override
   public HPEResult visitEmptyStatement(EmptyStatementTree node, HPEContext p) {
     return new HPEResult(node, HPEObject.Top);
   }
 
   @Override
   public HPEResult visitBlock(BlockTree node, HPEContext p) {
     p.env.inFrame();
     JCBlock block = (JCBlock) node;
     ListBuffer<Tree> newStat = new ListBuffer<>();
     for (JCStatement s : block.getStatements()) {
       HPEResult r = s.accept(this, p);
       if (r == null) {
         System.out.println("aaaaaaa");
       }
       newStat.append(r.code);
     }
     p.env.outFrame();
     return new HPEResult(HPEUtil.newBlock(block.flags, newStat.toList(), p),
         HPEObject.Top);
   }
 
   @Override
   public HPEResult visitDoWhileLoop(DoWhileLoopTree node, HPEContext p) {
     return p.treeMaker.Block(
         0,
         List.of((JCStatement) node.getStatement(), p.treeMaker.WhileLoop(
             (JCExpression) node.getCondition(),
             (JCStatement) node.getStatement()))).accept(this, p);
   }
 
   @Override
   public HPEResult visitWhileLoop(WhileLoopTree node, HPEContext p) {
     JCWhileLoop whileLoop = (JCWhileLoop) node;
     HPEResult condResult = whileLoop.cond.accept(this, p);
     HPEObject condValue = condResult.value();
     if (condValue.isConcrete()) {
       ListBuffer<JCStatement> stats = new ListBuffer<>();
       while (Boolean.valueOf(condValue.object.toString())) {
         HPEResult r = whileLoop.body.accept(this, p);
         stats.append((JCStatement) r.code);
         condResult = whileLoop.cond.accept(this, p);
         condValue = condResult.value();
       }
       return new HPEResult(HPEUtil.newBlock(0, stats.toList(), p),
           HPEObject.Top);
     } else {
       // TODO: sanity and inconsistency check!!
       HPEResult r = whileLoop.body.accept(this, p);
       return new HPEResult(p.treeMaker.WhileLoop(
           (JCExpression) condResult.code, (JCStatement) r.code),
           new HPEObject.Bottom());
     }
   }
 
   @Override
   public HPEResult visitForLoop(ForLoopTree node, final HPEContext p) {
     HPEResult result = null;
     JCForLoop forloop = (JCForLoop) node;
     p.env.inFrame();
     List<HPEResult> inits = HPEUtil.hpeList(forloop.init, this, p);
     HPEResult condResult = forloop.getCondition().accept(this, p);
     if (condResult.value().isConcrete()) {
       ListBuffer<JCStatement> stats = new ListBuffer<JCTree.JCStatement>();
       stats.appendList(HPEUtil.filterEmptyStmt(HPEUtil
           .<JCStatement> unboxHPEResults(inits)));
       List<HPEResult> updates = List.nil();
       while (Boolean.valueOf(condResult.value().object.toString())) {
         HPEResult bodyResult = forloop.body.accept(this, p);
         stats.appendList(HPEUtil.filterEmptyStmt(HPEUtil
             .<JCStatement> unboxHPEResults(updates)));
         stats.append((JCStatement) bodyResult.code);
         updates = HPEUtil.hpeList(forloop.step, this, p);
         condResult = forloop.cond.accept(this, p);
       }
       return new HPEResult(HPEUtil.newBlock(0, stats.toList(), p),
           HPEObject.Top);
     } else {
       List<HPEResult> updates = HPEUtil.hpeList(forloop.step, this, p);
       HPEResult stResult = forloop.getStatement().accept(this, p);
 
       result = new HPEResult(p.treeMaker.ForLoop(
           HPEUtil.<JCStatement> unboxHPEResults(inits),
           (JCExpression) condResult.code,
           Function.filter(new Function<Boolean, JCExpressionStatement>() {
             @Override
             public Boolean apply(JCExpressionStatement t) {
               return t.getKind() == Kind.EMPTY_STATEMENT;
             }
           }, HPEUtil.<JCExpressionStatement> unboxHPEResults(updates)),
           (JCStatement) stResult.code), HPEObject.Top);
     }
     p.env.outFrame();
     return result;
   }
 
   @Override
   public HPEResult visitEnhancedForLoop(EnhancedForLoopTree node, HPEContext p) {
     HPEResult eResult = node.getExpression().accept(this, p);
     if (eResult.value().isConcrete()) {
       ListBuffer<JCStatement> stats = new ListBuffer<>();
       Iterable<?> iterable = (Iterable<?>) eResult.value().object;
       Iterator<?> iterator = iterable.iterator();
       JCVariableDecl variableDecl = (JCVariableDecl) node.getVariable();
       while (iterator.hasNext()) {
         p.env.add(HPESymbol.local(variableDecl.sym.name),
             HPEObject.fromObject(iterator.next()).setTag(Tag.CONCRETE));
         HPEResult r = node.getStatement().accept(this, p);
         stats.append((JCStatement) r.code);
       }
       return new HPEResult(HPEUtil.newBlock(0, stats.toList(), p),
           new HPEObject.Bottom());
     } else {
       // TODO: sanity and inconsistency check!!
       HPEResult r = node.getStatement().accept(this, p);
       return new HPEResult(p.treeMaker.ForeachLoop(
           (JCVariableDecl) node.getVariable(), (JCExpression) eResult.code,
           (JCStatement) r.code), HPEObject.Top);
     }
   }
 
   @Override
   public HPEResult visitLabeledStatement(LabeledStatementTree node, HPEContext p) {
     JCLabeledStatement labeledStatement = (JCLabeledStatement) node;
     HPEResult st = labeledStatement.getStatement().accept(this, p);
     return new HPEResult(p.treeMaker.Labelled(labeledStatement.label,
         (JCStatement) st.code), HPEObject.Top);
   }
 
   @Override
   public HPEResult visitSwitch(SwitchTree node, HPEContext p) {
     JCSwitch jcSwitch = (JCSwitch) node;
     HPEResult exprResult = jcSwitch.getExpression().accept(this, p);
     if (exprResult.value().isConcrete()) {
 
     } else {
       List<HPEResult> casesResult = HPEUtil.hpeList(jcSwitch.getCases(), this,
           p);
 
     }
     return super.visitSwitch(node, p);
   }
 
   @Override
   public HPEResult visitCase(CaseTree node, HPEContext p) {
     JCCase jcCase = (JCCase) node;
     List<HPEResult> statsResults = HPEUtil.hpeList(jcCase.getStatements(),
         this, p);
 
     return super.visitCase(node, p);
   }
 
   @Override
   public HPEResult visitSynchronized(SynchronizedTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitSynchronized(node, p);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public HPEResult visitTry(TryTree node, HPEContext p) {
     JCTry jcTry = (JCTry) node;
     HPEResult blockResult = jcTry.getBlock().accept(this, p);
 
     return new HPEResult(
         p.treeMaker.Try((List<JCTree>) jcTry.getResources(),
             (JCBlock) blockResult.code, jcTry.getCatches(),
             jcTry.getFinallyBlock()), HPEObject.Top);
   }
 
   @Override
   public HPEResult visitCatch(CatchTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitCatch(node, p);
   }
 
   @Override
   public HPEResult visitConditionalExpression(ConditionalExpressionTree node,
       HPEContext p) {
     JCConditional jcConditional = (JCConditional) node;
     HPEResult condResult = jcConditional.cond.accept(this, p);
     HPEObject condValue = condResult.value();
     if (condValue.isConcrete()) {
       if (Boolean.valueOf(condValue.object.toString())) {
         return jcConditional.getTrueExpression().accept(this, p);
       } else {
         return jcConditional.getFalseExpression().accept(this, p);
       }
     } else {
       // TODO: sanity and inconsistency check!!
       JCExpression thenStatement = (JCExpression) jcConditional
           .getTrueExpression().accept(this, p).code;
 
       JCExpression elseStatement = (JCExpression) jcConditional
           .getFalseExpression().accept(this, p).code;
 
       return new HPEResult(p.treeMaker.Conditional(
           (JCExpression) condResult.code, thenStatement, elseStatement),
           new HPEObject.Bottom());
     }
   }
 
   @Override
   public HPEResult visitIf(IfTree node, HPEContext p) {
     JCIf jcIf = (JCIf) node;
     HPEResult condResult = jcIf.cond.accept(this, p);
     HPEObject condValue = condResult.value();
     if (condValue.isConcrete()) {
       if (Boolean.valueOf(condValue.object.toString())) {
         return jcIf.getThenStatement().accept(this, p);
       } else {
         if (jcIf.getElseStatement() != null)
           return jcIf.getElseStatement().accept(this, p);
         else {
           return new HPEResult(p.treeMaker.Skip(), HPEObject.Top);
         }
       }
     } else {
       // TODO: sanity and inconsistency check!!
       JCStatement thenStatement = (JCStatement) jcIf.getThenStatement().accept(
           this, p).code;
 
       JCStatement elseStatement = null;
       if (jcIf.getElseStatement() != null)
         elseStatement = (JCStatement) jcIf.getElseStatement().accept(this, p).code;
 
       return new HPEResult(p.treeMaker.If((JCExpression) condResult.code,
           thenStatement, elseStatement), new HPEObject.Bottom());
     }
   }
 
   @Override
   public HPEResult visitExpressionStatement(ExpressionStatementTree node,
       HPEContext p) {
     JCExpressionStatement es = (JCExpressionStatement) node;
     HPEResult r = es.expr.accept(this, p);
     if (r.code.getKind() == Kind.EMPTY_STATEMENT) {
       return r;
     }
     return new HPEResult(p.treeMaker.Exec((JCExpression) r.code),
         new HPEObject.Bottom());
   }
 
   @Override
   public HPEResult visitBreak(BreakTree node, HPEContext p) {
     return new HPEResult(node, HPEObject.Top);
   }
 
   @Override
   public HPEResult visitContinue(ContinueTree node, HPEContext p) {
     return new HPEResult(node, HPEObject.Top);
   }
 
   @Override
   public HPEResult visitReturn(ReturnTree node, HPEContext p) {
     JCReturn jcReturn = (JCReturn) node;
     if (node.getExpression() != null) {
       HPEResult r = jcReturn.expr.accept(this, p);
       p.env.getCurrentStack().setReturnValue(r.value());
       if (r.value().isConcrete()) {
         return new HPEResult(
             p.treeMaker.Return(p.treeMaker.Literal(r.value().object)),
             new HPEObject.Bottom());
       } else {
         return new HPEResult(p.treeMaker.Return((JCExpression) r.code),
             new HPEObject.Bottom());
       }
     } else {
       return new HPEResult(node, HPEObject.Top);
     }
   }
 
   @Override
   public HPEResult visitThrow(ThrowTree node, HPEContext p) {
     HPEResult r = node.getExpression().accept(this, p);
     return new HPEResult(p.treeMaker.Throw((JCTree) r.code), HPEObject.Top);
   }
 
   @Override
   public HPEResult visitAssert(AssertTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitAssert(node, p);
   }
 
   @Override
   public HPEResult visitMethodInvocation(MethodInvocationTree node, HPEContext p) {
     HPEResult methodSelectResult = node.getMethodSelect().accept(this, p);
     CLOSURE closure = (CLOSURE) methodSelectResult.value();
     if (closure.isCTMethod()) {
       TreeFull treeFull = new TreeFull();
       HPEObject argval = node.getArguments().get(0).accept(treeFull, p);
       return new HPEResult(node.getArguments().get(0),
           argval.setTag(Tag.CONCRETE));
     } else if (closure.isCTIfMethod()) {
       HPEResult ifCond = node.getArguments().get(1).accept(this, p);
       if (new Boolean(ifCond.value().value().toString())) {
         TreeFull treeFull = new TreeFull();
         HPEObject argval = node.getArguments().get(0).accept(treeFull, p);
         return new HPEResult(node.getArguments().get(0),
             argval.setTag(Tag.CONCRETE));
       } else {
         HPEResult argval = node.getArguments().get(0).accept(this, p);
         return new HPEResult(HPEUtil.makeLiteral(argval.value(), p), argval
             .value().setTag(Tag.ABSTRACT));
       }
     } else if (closure.isIsCTMethod()) {
       HPEResult argval = node.getArguments().get(0).accept(this, p);
       if (argval.value().isConcrete()) {
         return new HPEResult(
             HPEUtil.makeLiteral(new Primitive.BOOLEAN(true), p),
             new Primitive.BOOLEAN(true).setTag(Tag.CONCRETE));
       } else {
         return new HPEResult(HPEUtil.makeLiteral(new Primitive.BOOLEAN(false),
             p), new Primitive.BOOLEAN(false).setTag(Tag.CONCRETE));
       }
     } else if (closure.isRTMethod()) {
       HPEResult argval = node.getArguments().get(0).accept(this, p);
 
       if (argval.value().isConcrete()) {
         if (!argval.value().isPrimitive()) {
           throw new HPEException("ERROR: Trying to move a compile-time object "
               + "to the runtime stage. >>>\n" + node);
         }
         return new HPEResult(HPEUtil.makeLiteral(argval.value(), p), argval
             .value().setTag(Tag.ABSTRACT));
       } else {
         return argval;
       }
     } else {
       List<HPEResult> args = List.nil();
       for (ExpressionTree e : node.getArguments()) {
         args = args.append(e.accept(this, p));
       }
       List<Parameter> ctArgs = List.nil();
       List<JCExpression> rtArgs = List.nil();
       List<Type> rtTypes = List.nil();
       for (int i = 0; i < args.size(); i++) {
         HPEResult r = args.get(i);
         if (r.value().isConcrete())
           ctArgs = ctArgs.append(new Parameter(i, r.value()));
         else {
           rtArgs = rtArgs.append((JCExpression) r.code);
           rtTypes = rtTypes
               .append(closure.methodSymbol.getParameters().get(i).type);
         }
       }
       Action action = HPEUtil.findActionForCall(args, closure, p);
       switch (action) {
         case ERROR:
           throw new HPEException("ERROR: Trying to specialize a method for "
               + "which source is not avilable. >>>\n" + node);
 
         case CODE:
           ListBuffer<JCExpression> paramsExpressions = new ListBuffer<>();
           for (HPEResult e : args) {
             if (e.value().isConcrete() && e.value().isPrimitive())
               paramsExpressions.append(HPEUtil.makeLiteral(e.value(), p));
             else
               paramsExpressions.append((JCExpression) e.code);
           }
           return new HPEResult(p.treeMaker.App(
               (JCExpression) methodSelectResult.code,
               paramsExpressions.toList()), HPEObject.Top);
 
         case SPECIALIZE:
           boolean createStatic = closure.methodSymbol.isStatic();
           if (closure.thiz.isConcrete()) {
             ctArgs = ctArgs.append(new Parameter(-1, closure.thiz));
             createStatic = true;
           }
           Pair<Method, Boolean> genMethod = p.generateName(
               closure.methodSymbol, ctArgs);
           if (!genMethod.snd) { // already not generated?
             MethodSymbol newMS = p.generateMethodSymbol(closure.methodSymbol,
                 p.names.fromString(genMethod.fst.getGenMethodName()), rtTypes,
                 ctArgs, createStatic);
             genMethod.fst.setGenMethodSymbol(newMS);
 
             List<? extends Symbol> subs = List.of(closure.methodSymbol.owner);
             if (closure.methodSymbol.owner.isInterface()
                 || closure.methodSymbol.owner.getModifiers().contains(
                     Modifier.ABSTRACT)) {
               subs = p.subTypes(closure.methodSymbol.owner);
             }
             for (Symbol asub : subs) {
               JCClassDecl classDecl = (JCClassDecl) p.trees.getTree(asub);
               JCMethodDecl newMD = hpeMethodDecl(newMS, closure.methodSymbol,
                   classDecl, ctArgs, p, closure.thiz);
               classDecl.defs = classDecl.defs.append(newMD);
             }
           }
 
           JCMethodInvocation mi = null;
           if (createStatic)
             mi = p.generateStaticMethodInvocation(
                 genMethod.fst.getGenMethodSymbol(), rtArgs);
           else {
             if (methodSelectResult.code instanceof JCFieldAccess)
               mi = p.generateInstanceMethodInvocation(
                   ((JCFieldAccess) methodSelectResult.code).selected,
                   genMethod.fst.getGenMethodSymbol(), rtArgs);
             else
               mi = p.generateInstanceMethodInvocation(
                   genMethod.fst.getGenMethodSymbol(), rtArgs);
           }
           return new HPEResult(mi, HPEObject.Top);
         case INVOKE_OR_CODE:
         case INVOKE_OR_ERROR:
         case INVOKE_OR_SPECIALIZE:
         case INVOKE:
           List<HPEObject> hpeObjects = List.nil();
           for (Parameter param : ctArgs) {
             hpeObjects = hpeObjects.append(param.value);
           }
           HPEObject callResult = closure.invoke(hpeObjects);
           Tree genCode = JCSkipExpr.instance();
           return new HPEResult(genCode, callResult.setTag(Tag.CONCRETE));
         default:
           throw new HPEException("Invalid state in HPE.");
       }
     }
   }
 
   @Override
   public HPEResult visitNewClass(NewClassTree node, HPEContext p) {
     JCNewClass newClass = (JCNewClass) node;
     HPEResult identResult = node.getIdentifier().accept(this, p);
 
     List<HPEResult> args = List.nil();
     for (ExpressionTree e : node.getArguments()) {
       args = args.append(e.accept(this, p));
     }
     List<Parameter> ctArgs = List.nil();
     List<JCExpression> rtArgs = List.nil();
     List<Type> rtTypes = List.nil();
     for (int i = 0; i < args.size(); i++) {
       HPEResult r = args.get(i);
       if (r.value().isConcrete())
         ctArgs = ctArgs.append(new Parameter(i, r.value()));
       else {
         rtArgs = rtArgs.append((JCExpression) r.code);
         rtTypes = rtTypes.append(newClass.constructorType.getParameterTypes()
             .get(i));
       }
     }
 
     if (ctArgs.size() > 0) {
       MethodSymbol constructorSymbol = (MethodSymbol) newClass.constructor;
       Pair<Method, Boolean> genClass = p
           .generateName(constructorSymbol, ctArgs);
       if (!genClass.snd) { // already not generated?
         MethodSymbol newCS = p.generateMethodSymbol(constructorSymbol,
             p.names.fromString(genClass.fst.getGenMethodName()), rtTypes,
             ctArgs, false);
         genClass.fst.setGenMethodSymbol(newCS);
 
         specializeClass(constructorSymbol, newCS,
             (ClassSymbol) constructorSymbol.owner, ctArgs, p);
 
         // TODO: add generated class
 
       }
       return new HPEResult(p.treeMaker.NewClass(newClass.encl, newClass
           .getTypeArguments(), p.treeMaker.Ident(p.names
           .fromString(genClass.fst.getGenMethodName())), rtArgs, genClass.fst
           .getSpecializedClass()), HPEObject.Top);
     } else {
       ListBuffer<JCExpression> paramsExpressions = new ListBuffer<>();
       for (HPEResult e : args) {
         if (e.value().isConcrete() && e.value().isPrimitive())
           paramsExpressions.append(HPEUtil.makeLiteral(e.value(), p));
         else
           paramsExpressions.append((JCExpression) e.code);
       }
       return new HPEResult(p.treeMaker.NewClass(newClass.encl,
           newClass.getTypeArguments(), (JCExpression) identResult.code,
           paramsExpressions.toList(), newClass.def), HPEObject.Top);
     }
   }
 
   public JCClassDecl specializeClass(MethodSymbol oldConstructor,
       MethodSymbol newConstructor, ClassSymbol classSymbol,
       List<Parameter> ctArgs, final HPEContext hpeContext) {
     JCClassDecl cd = (JCClassDecl) hpeContext.trees.getTree(classSymbol);
 
     JCClassDecl clonedCD = hpeContext.treeMaker.ClassDef(cd.mods,
         newConstructor.name, cd.typarams, hpeContext.treeMaker.Ident(cd.name),
         cd.implementing, List.<JCTree> nil());
 
     PartialObject po = new PartialObject();
 
     for (JCTree aMember : cd.defs) {
       if (aMember instanceof JCVariableDecl) {
         po.setFieldValue(((JCVariableDecl) aMember).getName(), new HPEObject.Bottom());
       }
     }
     JCMethodDecl newConsDecl = hpeMethodDecl(newConstructor, oldConstructor,
         cd, ctArgs, hpeContext, po);
     newConsDecl.sym = newConstructor;
     newConsDecl.name = hpeContext.names.init;
     clonedCD.defs = clonedCD.defs.append(newConsDecl);
     clonedCD.mods.flags &= ~Flags.PUBLIC;
 
     for (JCTree aDef : cd.defs) {
       if (aDef.getKind() == Kind.METHOD) {
         JCMethodDecl md = (JCMethodDecl) aDef;
         long flags = md.sym.flags();
         if (((flags & Flags.ABSTRACT) != 0) || ((flags & Flags.STATIC) != 0)
             || ((flags & Flags.NATIVE) != 0) || ((flags & Flags.FINAL) != 0) ||
             md.sym.name.equals(hpeContext.names.init))
           continue;
         clonedCD.defs = clonedCD.defs.append(hpeMethodDecl(md.sym, md.sym, cd,
             List.<Parameter> nil(), hpeContext, po));
       }
     }
 
     currentCompilationUnit.defs = currentCompilationUnit.defs.append(clonedCD);
     return clonedCD;
   }
 
   @Override
   public HPEResult visitNewArray(NewArrayTree node, final HPEContext p) {
     JCNewArray newArray = (JCNewArray) node;
     HPEResult typeResult = newArray.getType().accept(this, p);
     List<JCExpression> dims = HPEUtil.unboxHPEResults(HPEUtil.hpeList(
         newArray.getDimensions(), this, p));
 
     List<JCExpression> inits = HPEUtil.unboxHPEResults(HPEUtil.hpeList(
         newArray.getInitializers(), this, p));
 
     return new HPEResult(p.treeMaker.NewArray((JCExpression) typeResult.code,
         dims, inits), HPEObject.Top);
   }
 
   @Override
   public HPEResult visitParenthesized(ParenthesizedTree node, HPEContext p) {
     JCParens parens = (JCParens) node;
     HPEResult r = parens.getExpression().accept(this, p);
     if (r.code.getKind() == Kind.EMPTY_STATEMENT) {
       return r;
     }
     return new HPEResult(p.treeMaker.Parens((JCExpression) r.code), r.value());
   }
 
   JCMethodDecl hpeMethodDecl(MethodSymbol newMethodSymbol, MethodSymbol oldMS,
       JCClassDecl classDecl, List<Parameter> ctArgs, HPEContext hpeContext,
       HPEObject thiz) {
     JCMethodDecl md = (JCMethodDecl) hpeContext.trees.getTree(hpeContext
         .findMethodSymbolInClassSymbol(oldMS, classDecl.sym));
 
     MethodSymbol cloned = newMethodSymbol.clone(classDecl.sym);
     if (oldMS.getModifiers().contains(Modifier.ABSTRACT)
         && classDecl.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
       cloned.flags_field &= Flags.ABSTRACT;
     } else {
       cloned.flags_field &= ~Flags.ABSTRACT;
     }
     cloned.params = newMethodSymbol.params;
     if (md.body == null) {
       return hpeContext.treeMaker.MethodDef(cloned, null);
     } else {
       hpeContext.env.inStack(thiz);
       for (int i = 0; i < md.params.length(); i++) {
         boolean isCT = false;
         for (int j = 0; j < ctArgs.length(); j++) {
           if (ctArgs.get(j).position == i) {
             hpeContext.env.add(HPESymbol.local(md.params.get(i).sym.name),
                 ctArgs.get(j).value);
             isCT = true;
             break;
           }
         }
         if (!isCT) {
           hpeContext.env.add(HPESymbol.local(md.params.get(i).sym.name),
               HPEObject.Top);
         }
       }
       HPEResult r = md.body.accept(this, hpeContext);
       hpeContext.env.outStack();
       return hpeContext.treeMaker.MethodDef(cloned, (JCBlock) r.code);
     }
   }
 
   @Override
   public HPEResult visitAssignment(AssignmentTree node, HPEContext p) {
     JCAssign assign = (JCAssign) node;
     HPEResult rhsResult = assign.rhs.accept(this, p);
     HPEResult lhsResult = assign.lhs.accept(this, p);
     HPEObject v = lhsResult.value();
     HPESymbol x = v.symbol;
     HPEObject rv = rhsResult.value();
     if (v.isConcrete()) {
       if (!rv.isConcrete()) {
         if (rv.isPrimitive()) {
           p.env.add(x, rv.setTag(Tag.CONCRETE));
           return new HPEResult(p.treeMaker.Skip(), HPEObject.Top);
         } else {
           if (!x.isField) { // FIXME: dirty hack to solve the issue of assigning
                             // a top value to a concrete local value
             p.env.add(x, rv);
             return new HPEResult(p.treeMaker.Assign(p.treeMaker.Ident(x.name),
                 (JCExpression) rhsResult.code), rv);
           }
           System.err.println("The right-hand side of the assignment "
               + "must evaluate to a compile-time value.");
           System.err.println(node);
           System.exit(-1);
           return null;
         }
       } else {
         p.env.add(x, rv);
         return new HPEResult(p.treeMaker.Skip(), HPEObject.Top);
       }
     } else if (v.isAbstract()) {
       if (rv.isConcrete()) {
         System.err
             .println("Trying to assign a concrete value to an abstract symbol.");
         System.err.println(node);
         System.exit(-1);
         return null;
       }
       p.env.add(x, rv);
       return new HPEResult(p.treeMaker.Assign((JCIdent) lhsResult.code,
           (JCExpression) rhsResult.code), rv);
     } else if (v.isTop()) {
       if (rv.isConcrete()) {
         if (rv.isPrimitive()) {
           return new HPEResult(p.treeMaker.Assign(
               (JCExpression) lhsResult.code, (JCExpression) rhsResult.code),
               HPEObject.Top);
         } else {
           System.err
               .println("Trying to assign a non-Top value to a symbol with Top value.");
           System.err.println(node);
           System.exit(-1);
           return null;
         }
       }
       return new HPEResult(p.treeMaker.Assign((JCExpression) lhsResult.code,
           (JCExpression) rhsResult.code), rv);
     } else { // is Bottom
       p.env.add(x, rv);
       if (rv.isConcrete()) {
         return new HPEResult(p.treeMaker.Skip(), new HPEObject.Bottom());
       } else {
         return new HPEResult(p.treeMaker.Assign((JCExpression) lhsResult.code,
             (JCExpression) rhsResult.code), HPEObject.Top);
       }
     }
   }
 
   @Override
   public HPEResult visitCompoundAssignment(CompoundAssignmentTree node,
       HPEContext p) {
     JCAssignOp assignOp = (JCAssignOp) node;
     HPEResult varResult = assignOp.getVariable().accept(this, p);
     HPEResult exprResult = assignOp.getExpression().accept(this, p);
     return applyBinaryOp(assignOp.getTag(), varResult, exprResult, p);
   }
 
   @Override
   public HPEResult visitUnary(UnaryTree node, HPEContext p) {
     JCUnary unary = (JCUnary) node;
     HPEResult argResult = unary.arg.accept(this, p);
     HPEObject argv = argResult.value();
 
     if (argv.isConcrete()) {
       HPEObject v = HPEUtil.unaryOp(unary, argv, p);
       return new HPEResult(p.treeMaker.Skip(), v.setTag(argv.tag));
     } else if (argv.isAbstract()) {
       HPEObject v = HPEUtil.unaryOp(unary, argv, p);
       return new HPEResult(p.treeMaker.Unary(unary.getTag(),
           (JCExpression) argResult.code), v.setTag(argv.tag));
     } else {
       return new HPEResult(p.treeMaker.Unary(unary.getTag(),
           (JCExpression) argResult.code), HPEObject.Top);
     }
   }
 
   @Override
   public HPEResult visitBinary(BinaryTree node, HPEContext p) {
     JCBinary binary = (JCBinary) node;
     HPEResult lhsResult = binary.lhs.accept(this, p);
     HPEResult rhsResult = binary.rhs.accept(this, p);
     return applyBinaryOp(binary.getTag(), lhsResult, rhsResult, p);
   }
 
   HPEResult applyBinaryOp(int binaryTag, HPEResult lhs, HPEResult rhs,
       HPEContext p) {
     HPEObject lv = lhs.value();
     HPEObject rv = rhs.value();
     Kind binary = TreeInfo.tagToKind(binaryTag);
     try {
       if ((lv.isConcrete() && rv.isConcrete())
           || (lv.isConcrete() && rv.isAbstract())
           || (lv.isAbstract() && rv.isConcrete())) {
         HPEObject v = HPEUtil.binOp(binary, lv, rv);
         return new HPEResult(HPEUtil.makeLiteral(v, p), v.setTag(Tag.CONCRETE));
       } else if (lv.isAbstract() && rv.isAbstract()) {
         HPEObject v = HPEUtil.binOp(binary, lv, rv);
         return new HPEResult(p.treeMaker.Binary(binaryTag,
             (JCExpression) lhs.code, (JCExpression) rhs.code),
             v.setTag(Tag.ABSTRACT));
       } else if (lv.isConcrete()) {
         return new HPEResult(p.treeMaker.Binary(binaryTag,
             HPEUtil.makeLiteral(lv, p), (JCExpression) rhs.code), HPEObject.Top);
       } else if (rv.isConcrete()) {
         return new HPEResult(p.treeMaker.Binary(binaryTag,
             (JCExpression) lhs.code, HPEUtil.makeLiteral(rv, p)), HPEObject.Top);
       } else {
         return new HPEResult(p.treeMaker.Binary(binaryTag,
             (JCExpression) lhs.code, (JCExpression) rhs.code), HPEObject.Top);
       }
     } catch (HPEException e) {
       System.err.println(e);
       e.printStackTrace();
       System.exit(-1);
       return null;
     }
   }
 
   @Override
   public HPEResult visitTypeCast(TypeCastTree node, HPEContext p) {
     JCTypeCast typeCast = (JCTypeCast) node;
     HPEResult clazzResult = typeCast.getType().accept(this, p);
     HPEResult exprResult = typeCast.getExpression().accept(this, p);
     return new HPEResult(p.treeMaker.TypeCast((JCTree) clazzResult.code,
         (JCExpression) exprResult.code), HPEObject.Top);
   }
 
   @Override
   public HPEResult visitInstanceOf(InstanceOfTree node, HPEContext p) {
     JCInstanceOf instanceOf = (JCInstanceOf) node;
     HPEResult exprResult = instanceOf.getExpression().accept(this, p);
     HPEResult clazzResult = instanceOf.getType().accept(this, p);
     if (exprResult.value().isAbstract() || exprResult.value().isConcrete()) {
       Type exprType = p.getType(exprResult.value().object.getClass());
       CLASS clazz = (CLASS) clazzResult.value();
       boolean isInsOf = false;
       if (p.types.isSubtype(exprType, clazz.getType())) {
         isInsOf = true;
       }
       return new HPEResult(p.treeMaker.TypeTest((JCExpression) exprResult.code,
           (JCTree) clazzResult.code),
           new Primitive.BOOLEAN(isInsOf).setTag(exprResult.value().tag));
     } else {
       return new HPEResult(p.treeMaker.TypeTest((JCExpression) exprResult.code,
           (JCTree) clazzResult.code), HPEObject.Top);
     }
   }
 
   @Override
   public HPEResult visitArrayAccess(ArrayAccessTree node, HPEContext p) {
     // FIXME: which one is evaluated first, index or expr?
     // FIXME: fix the return result
     HPEResult exprResult = node.getExpression().accept(this, p);
     HPEObject exprObject = exprResult.value();
     HPEResult indxResult = node.getIndex().accept(this, p);
     HPEObject indxObject = indxResult.value();
     if (exprObject.isConcrete()) {
       if (indxObject.isConcrete() || indxObject.isAbstract()) {
         Integer i = new Integer(indxObject.value().toString());
         HPEObject item = ((ObjectLiteral) exprObject).getArrayItem(i);
         item.symbol = HPESymbol.arrayItem(exprObject, i);
         return new HPEResult(p.treeMaker.Indexed(
             (JCExpression) exprResult.code, (JCExpression) indxResult.code),
             item);
       } else {
         throw new HPEException(
             "Invalid array index. Array index must be a compile-time value.");
       }
     } else {
       if (indxObject.isConcrete()) {
         return new HPEResult(
             p.treeMaker.Indexed((JCExpression) exprResult.code,
                 HPEUtil.makeLiteral(indxObject, p)), HPEObject.Top);
       } else {
         return new HPEResult(p.treeMaker.Indexed(
             (JCExpression) exprResult.code, (JCExpression) indxResult.code),
             HPEObject.Top);
       }
     }
   }
 
   @Override
   public HPEResult visitMemberSelect(MemberSelectTree node, HPEContext p) {
     JCFieldAccess fieldAccess = (JCFieldAccess) node;
     if (fieldAccess.selected.getTag() == JCTree.SELECT) {
       JCFieldAccess selectedAccess = (JCFieldAccess) fieldAccess.selected;
       if (selectedAccess.sym.getKind() == ElementKind.PACKAGE) {
         HPEObject r = HPEUtil.resolveSymbol(fieldAccess.sym, null, p);
         return new HPEResult(node, r);
       } else {
         HPEResult receiver = fieldAccess.selected.accept(this, p);
         HPEObject r = HPEUtil.resolveSymbol(fieldAccess.sym, receiver.value(),
             p);
         return new HPEResult(p.treeMaker.Select((JCExpression) receiver.code,
             fieldAccess.sym), r);
       }
     } else {
       HPEResult receiver = fieldAccess.selected.accept(this, p);
       HPEObject r = HPEUtil.resolveSymbol(fieldAccess.sym, receiver.value(), p);
       return new HPEResult(p.treeMaker.Select((JCExpression) receiver.code,
           fieldAccess.sym), r);
     }
   }
 
   @Override
   public HPEResult visitIdentifier(IdentifierTree node, HPEContext p) {
     JCIdent ident = (JCIdent) node;
     HPEObject v = HPEUtil.resolveSymbol(ident.sym, p.env.getCurrentThis(), p);
     if (v.isConcrete()) {
       if (v.isPrimitive())
         return new HPEResult(HPEUtil.makeLiteral(v, p), v);
       else
         return new HPEResult(node, v); // FIXME: this is an error
                                        // prone case
     } else {
       return new HPEResult(node, v);
     }
   }
 
   @Override
   public HPEResult visitLiteral(LiteralTree node, HPEContext p) {
     JCLiteral jcLiteral = (JCLiteral) node;
     switch (jcLiteral.getKind()) {
       case INT_LITERAL:
         return new HPEResult(node, Primitive.NUMBER.INT(
             (Integer) node.getValue()).setTag(Tag.ABSTRACT));
       case FLOAT_LITERAL:
         return new HPEResult(node, Primitive.NUMBER.FLOAT(
             (Float) node.getValue()).setTag(Tag.ABSTRACT));
       case DOUBLE_LITERAL:
         return new HPEResult(node, Primitive.NUMBER.DOUBLE(
             (Double) node.getValue()).setTag(Tag.ABSTRACT));
       case LONG_LITERAL:
         return new HPEResult(node, Primitive.NUMBER
             .LONG((Long) node.getValue()).setTag(Tag.ABSTRACT));
       case STRING_LITERAL:
         return new HPEResult(node, new Primitive.STRING(node.getValue()
             .toString()).setTag(Tag.ABSTRACT));
       case NULL_LITERAL:
         return new HPEResult(node, new Primitive.NULL().setTag(Tag.ABSTRACT));
       case BOOLEAN_LITERAL:
         return new HPEResult(node, new Primitive.BOOLEAN(node.getValue()
             .toString()).setTag(Tag.ABSTRACT));
       case CHAR_LITERAL:
         return new HPEResult(node, new Primitive.CHAR(node.getValue()
             .toString().charAt(0)).setTag(Tag.ABSTRACT));
       default:
         assert (false);
         break;
     }
     return super.visitLiteral(node, p);
   }
 
   @Override
   public HPEResult visitPrimitiveType(PrimitiveTypeTree node, HPEContext p) {
     JCPrimitiveTypeTree primitiveTypeTree = (JCPrimitiveTypeTree) node;
     return new HPEResult(node, new CLASS(
         (ClassSymbol) p.findSymbolForClass(HPEUtil
             .toClass(primitiveTypeTree.type))));
   }
 
   @Override
   public HPEResult visitArrayType(ArrayTypeTree node, HPEContext p) {
     JCArrayTypeTree arrayTypeTree = (JCArrayTypeTree) node;
     HPEResult arrayType = arrayTypeTree.getType().accept(this, p);
     CLASS componentType = (CLASS) arrayType.value();
 
     return new HPEResult(node, new CLASS(new Type.ArrayType(
         componentType.getType(), p.symtab.arrayClass)));
   }
 
   @Override
   public HPEResult visitParameterizedType(ParameterizedTypeTree node,
       HPEContext p) {
     HPEResult r = node.getType().accept(this, p);
     JCExpression clazz = (JCExpression) r.code;
     ListBuffer<JCExpression> parameters = new ListBuffer<>();
     for (Tree t : node.getTypeArguments()) {
       parameters.add((JCExpression) t.accept(this, p).code);
     }
     JCTypeApply typeApply = p.treeMaker.TypeApply(clazz, parameters.toList());
     return new HPEResult(typeApply, HPEObject.Top);
   }
 
   @Override
   public HPEResult visitUnionType(UnionTypeTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitUnionType(node, p);
   }
 
   @Override
   public HPEResult visitTypeParameter(TypeParameterTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitTypeParameter(node, p);
   }
 
   @Override
   public HPEResult visitWildcard(WildcardTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitWildcard(node, p);
   }
 
   @Override
   public HPEResult visitModifiers(ModifiersTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitModifiers(node, p);
   }
 
   @Override
   public HPEResult visitAnnotation(AnnotationTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitAnnotation(node, p);
   }
 
   @Override
   public HPEResult visitOther(Tree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitOther(node, p);
   }
 
   @Override
   public HPEResult visitErroneous(ErroneousTree node, HPEContext p) {
     // TODO Auto-generated method stub
     return super.visitErroneous(node, p);
   }
 }
