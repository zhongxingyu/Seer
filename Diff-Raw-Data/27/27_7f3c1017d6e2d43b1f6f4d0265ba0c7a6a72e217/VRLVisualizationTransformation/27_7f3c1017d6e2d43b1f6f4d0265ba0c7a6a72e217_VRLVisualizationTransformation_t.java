 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.mihosoft.vrl.instrumentation;
 
 import eu.mihosoft.vrl.workflow.FlowFactory;
 import eu.mihosoft.vrl.workflow.IdGenerator;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import org.codehaus.groovy.transform.ASTTransformation;
 import org.codehaus.groovy.transform.GroovyASTTransformation;
 import org.codehaus.groovy.control.SourceUnit;
 import org.codehaus.groovy.control.CompilePhase;
 import org.codehaus.groovy.ast.ASTNode;
 import org.codehaus.groovy.ast.ClassNode;
 import org.codehaus.groovy.ast.MethodNode;
 import org.codehaus.groovy.ast.expr.ArgumentListExpression;
 import org.codehaus.groovy.ast.expr.BinaryExpression;
 import org.codehaus.groovy.ast.expr.BooleanExpression;
 import org.codehaus.groovy.ast.expr.ClassExpression;
 import org.codehaus.groovy.ast.expr.ConstantExpression;
 import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
 import org.codehaus.groovy.ast.expr.DeclarationExpression;
 import org.codehaus.groovy.ast.expr.Expression;
 import org.codehaus.groovy.ast.expr.MethodCallExpression;
 import org.codehaus.groovy.ast.expr.PostfixExpression;
 import org.codehaus.groovy.ast.expr.PrefixExpression;
 import org.codehaus.groovy.ast.expr.PropertyExpression;
 import org.codehaus.groovy.ast.expr.VariableExpression;
 import org.codehaus.groovy.ast.stmt.EmptyStatement;
 import org.codehaus.groovy.ast.stmt.ForStatement;
 import org.codehaus.groovy.ast.stmt.IfStatement;
 import org.codehaus.groovy.ast.stmt.Statement;
 import org.codehaus.groovy.ast.stmt.WhileStatement;
 import org.codehaus.groovy.transform.StaticTypesTransformation;
 import org.codehaus.groovy.transform.stc.StaticTypesMarker;
 
 /**
  * Adds instrumentation to each method call. Use {@link VRLInstrumentation} to
  * request this transformation.
  *
  * @author Michael Hoffer <info@michaelhoffer.de>
  */
 @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
 public class VRLVisualizationTransformation implements ASTTransformation {
 
     @Override
     public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
 
         StaticTypesTransformation transformation = new StaticTypesTransformation();
         transformation.visit(astNodes, sourceUnit);
 
         VisualCodeBuilder_Impl codeBuilder = new VisualCodeBuilder_Impl();
 
         Map<String, List<Scope>> scopes = new HashMap<>();
 
         VGroovyCodeVisitor visitor = new VGroovyCodeVisitor(sourceUnit, codeBuilder);
 
         List<Scope> clsScopes = new ArrayList<>();
         scopes.put(sourceUnit.getName(), clsScopes);
         scopes.get(sourceUnit.getName()).add(visitor.getRootScope());
 
         // apply transformation for each class in the specified source unit
         for (ClassNode clsNode : sourceUnit.getAST().getClasses()) {
 
 //            if (!scopes.containsKey(clsNode.getName())) {
 //
 //                List<Scope> clsScopes = new ArrayList<>();
 //                scopes.put(clsNode.getName(), clsScopes);
 //            }
             //ClassVisitor visitor = new ClassVisitor(sourceUnit, clsNode, codeBuilder);
             visitor.visitClass(clsNode);
 //            clsNode.visitContents(visitor);
 
             //scopes.get(clsNode.getName()).add(visitor.getRootScope());
             for (MethodNode m : clsNode.getAllDeclaredMethods()) {
                 System.out.println("method: " + m.getName());
             }
         }
 
         for (String clazz : scopes.keySet()) {
             for (Scope s : scopes.get(clazz)) {
                 System.out.println(s.toString());
             }
         }
 
         UIBinding.scopes.putAll(scopes);
 
     }
 }
 
 class StateMachine {
 
     private final Stack<Map<String, Boolean>> stateStack = new Stack<>();
 
     public void set(String name, boolean state) {
         stateStack.peek().put(name, state);
     }
 
     public boolean get(String name) {
         Boolean result = stateStack.peek().get(name);
 
         if (result == null) {
             return false;
         }
 
         return result;
     }
 
     public void push(String name, boolean state) {
         stateStack.push(new HashMap<>());
         stateStack.peek().put(name, state);
     }
 
     public void pop() {
         stateStack.pop();
     }
 
 }
 
 class VGroovyCodeVisitor extends org.codehaus.groovy.ast.ClassCodeVisitorSupport {
 
     private SourceUnit sourceUnit;
     private VisualCodeBuilder_Impl codeBuilder;
     private Scope rootScope;
     private Scope currentScope;
     private Invocation lastMethod;
     private Stack<String> vIdStack = new Stack<>();
     private final StateMachine stateMachine = new StateMachine();
     private IdGenerator generator = FlowFactory.newIdGenerator();
 
     private Map<MethodCallExpression, String> returnValuesOfMethods
             = new HashMap<>();
 
     public VGroovyCodeVisitor(SourceUnit sourceUnit, VisualCodeBuilder_Impl codeBuilder) {
 
         this.sourceUnit = sourceUnit;
         this.codeBuilder = codeBuilder;
 
         codeBuilder.setIdRequest(new IdRequest() {
             @Override
             public String request() {
                 return requestId();
             }
         });
 
 //        this.rootScope = codeBuilder.createScope(null, ScopeType.NONE, sourceUnit.getName(), new Object[0]);
         this.rootScope = codeBuilder.declareCompilationUnit(sourceUnit.getName(), "undefined");
         this.currentScope = rootScope;
     }
 
     @Override
     protected SourceUnit getSourceUnit() {
         return sourceUnit;
     }
 
     private String requestId() {
 
         String result = "";
 
         if (!vIdStack.isEmpty()) {
             result = vIdStack.pop();
 
             if (generator.getIds().contains(result)) {
                 System.err.println(">> requestId(): Id already defined: " + result);
                 result = generator.newId();
             } else {
                 generator.addId(result);
                 System.out.println(">> USING ID: " + result);
             }
         } else {
             result = generator.newId();
         }
 
         return result;
     }
 
     @Override
     public void visitClass(ClassNode s) {
 
         System.out.println("CLASS: " + s.getName());
 
 //        currentScope = codeBuilder.createScope(currentScope, ScopeType.CLASS, s.getName(), new Object[0]);
         currentScope = codeBuilder.declareClass((CompilationUnitDeclaration) currentScope,
                 new Type(s.getName(), false),
                 convertModifiers(s.getModifiers()),
                 convertExtends(s),
                 convertImplements(s));
 
         super.visitClass(s);
 
         currentScope = currentScope.getParent();
 
         currentScope.setCode(getCode(s));
 
     }
 
     @Override
     public void visitMethod(MethodNode s) {
 
         System.out.println("m: " + s.getName() + ", parentscope: " + currentScope.getName() + ": " + currentScope.getType());
 
         if (currentScope instanceof ClassDeclaration) {
 
             currentScope = codeBuilder.declareMethod(
                     (ClassDeclaration) currentScope, convertModifiers(s.getModifiers()), new Type(s.getReturnType().getName(), true),
                     s.getName(), convertMethodParameters(s.getParameters()));
         } else {
             throw new RuntimeException("method cannot be declared here! Scope: " + currentScope.getName() + ": " + currentScope.getType());
         }
 
         currentScope.setCode(getCode(s));
 
         super.visitMethod(s);
 
         currentScope = currentScope.getParent();
 
         currentScope.setCode(getCode(s));
 
     }
 
 //    @Override
 //    public void visitBlockStatement(BlockStatement s) {
 //        System.out.println(" --> new Scope");
 //        super.visitBlockStatement(s);
 //        System.out.println(" --> leave Scope");
 //    }
     @Override
     public void visitForLoop(ForStatement s) {
         System.out.println(" --> FOR-LOOP: " + s.getVariable().getName());
 
         // predeclaration, ranges will be defined later
         currentScope = codeBuilder.declareFor(currentScope, s.getVariable().getName(), 0, 0, 0);
 
         stateMachine.push("for-loop", true);
 
         super.visitForLoop(s);
 
         if (!stateMachine.get("for-loop:declaration")) {
             throw new IllegalStateException(
                     "For loop must contain a variable declaration such as 'int i=0'!");
         }
 
         if (!stateMachine.get("for-loop:compareExpression")) {
             throw new IllegalStateException("for-loop: must contain binary"
                     + " expressions of the form 'a <= b' with a, b being"
                     + " constant integers!");
         }
 
         stateMachine.pop();
 
         currentScope = currentScope.getParent();
 
         currentScope.setCode(getCode(s));
 
 //        System.exit(1);
     }
 
     @Override
     public void visitWhileLoop(WhileStatement s) {
         System.out.println(" --> WHILE-LOOP: " + s.getBooleanExpression());
         currentScope = codeBuilder.createScope(currentScope, ScopeType.WHILE, "while", new Object[0]);
         super.visitWhileLoop(s);
         currentScope = currentScope.getParent();
 
         currentScope.setCode(getCode(s));
     }
 
     @Override
     public void visitIfElse(IfStatement ifElse) {
         System.out.println(" --> IF-STATEMENT: " + ifElse.getBooleanExpression());
 
         currentScope = codeBuilder.createScope(currentScope, ScopeType.IF, "if", new Object[0]);
 
         ifElse.getBooleanExpression().visit(this);
         ifElse.getIfBlock().visit(this);
 
         currentScope = currentScope.getParent();
 
         currentScope = codeBuilder.createScope(currentScope, ScopeType.ELSE, "else", new Object[0]);
 
         Statement elseBlock = ifElse.getElseBlock();
         if (elseBlock instanceof EmptyStatement) {
             // dispatching to EmptyStatement will not call back visitor, 
             // must call our visitEmptyStatement explicitly
             visitEmptyStatement((EmptyStatement) elseBlock);
         } else {
             elseBlock.visit(this);
         }
 
         currentScope = currentScope.getParent();
 
         currentScope.setCode(getCode(ifElse));
 
     }
 
     @Override
     public void visitConstructorCallExpression(ConstructorCallExpression s) {
         System.out.println(" --> CONSTRUCTOR: " + s.getType());
 
         super.visitConstructorCallExpression(s);
 
         ArgumentListExpression args = (ArgumentListExpression) s.getArguments();
 
         Variable[] arguments = convertArguments(args);
 
         codeBuilder.createInstance(
                 currentScope, new Type(s.getType().getName(), false),
                 codeBuilder.createVariable(currentScope, new Type(s.getType().getName(), false)).getName(),
                 arguments);
     }
 
     private String getCode(ASTNode n) {
         String code = sourceUnit.getSample(n.getLineNumber(), n.getColumnNumber(), null);
         return code;
     }
 
     @Override
     public void visitMethodCallExpression(MethodCallExpression s) {
         System.out.println(" --> METHOD: " + s.getMethodAsString());
 
         super.visitMethodCallExpression(s);
 
         ArgumentListExpression args = (ArgumentListExpression) s.getArguments();
         Variable[] arguments = convertArguments(args);
 
         String objectName = null;
 
         boolean isIdCall = false;
 
         if (s.getObjectExpression() instanceof VariableExpression) {
             VariableExpression ve = (VariableExpression) s.getObjectExpression();
             objectName = ve.getName();
         } else if (s.getObjectExpression() instanceof ClassExpression) {
             ClassExpression ce = (ClassExpression) s.getObjectExpression();
             objectName = ce.getType().getName();
 
             if (ce.getType().getName().equals(VSource.class.getName())) {
                 isIdCall = true;
                 System.out.println(">> VSource: push");
                 for (Variable arg : arguments) {
                     System.out.println(" -->" + arg.getValue().toString());
                     vIdStack.push(arg.getValue().toString());
                 }
             }
         }
 
         String returnValueName = "void";
 
         boolean isVoid = true;
 
         MethodNode mTarget = (MethodNode) s.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
 
         if (mTarget != null && mTarget.getReturnType() != null) {
             isVoid = mTarget.getReturnType().getName().toLowerCase().equals("void");
             System.out.println("TYPECHECKED!!!");
         }
 
         if (!isVoid) {
             returnValueName = codeBuilder.createVariable(currentScope, new Type(mTarget.getReturnType().getName(), true)).getName();
             returnValuesOfMethods.put(s, returnValueName);
         }
 
         if (!isIdCall) {
             if (objectName != null) {
                 codeBuilder.invokeMethod(currentScope, objectName, s.getMethod().getText(), isVoid,
                         returnValueName, arguments).setCode(getCode(s));
             } else if (s.getMethod().getText().equals("println")) {
                 codeBuilder.invokeStaticMethod(currentScope, new Type("System.out"), s.getMethod().getText(), isVoid,
                         returnValueName, arguments).setCode(getCode(s));
             }
         }
 
     }
 
     @Override
     public void visitDeclarationExpression(DeclarationExpression s) {
         System.out.println(" --> DECLARATION: " + s.getVariableExpression());
         super.visitDeclarationExpression(s);
 
         if (currentScope instanceof ForDeclaration_Impl) {
 
             ForDeclaration_Impl forD = (ForDeclaration_Impl) currentScope;
 
             if (!stateMachine.get("for-loop:declaration")) {
                 forD.setVarName(s.getVariableExpression().getName());
 
                 if (!(s.getRightExpression() instanceof ConstantExpression)) {
                     throw new IllegalStateException("In for-loop: variable '" + forD.getVarName()
                             + "' must be initialized with an integer constant!");
                 }
 
                 ConstantExpression ce = (ConstantExpression) s.getRightExpression();
 
                 if (!(ce.getValue() instanceof Integer)) {
                     throw new IllegalStateException("In for-loop: variable '" + forD.getVarName()
                             + "' must be initialized with an integer constant!");
                 }
 
                 forD.setFrom((Integer) ce.getValue());
 
                 stateMachine.set("for-loop:declaration", true);
             }
 
         } else {
 
             codeBuilder.createVariable(currentScope, new Type(s.getVariableExpression().getType().getName(), true), s.getVariableExpression().getName());
 
             if (s.getRightExpression() instanceof ConstantExpression) {
                 ConstantExpression ce = (ConstantExpression) s.getRightExpression();
                 codeBuilder.assignConstant(currentScope, s.getVariableExpression().getName(), ce.getValue());
             }
         }
     }
 
     @Override
     public void visitBinaryExpression(BinaryExpression s) {
 
         if (currentScope instanceof ForDeclaration_Impl) {

             ForDeclaration_Impl forD = (ForDeclaration_Impl) currentScope;

             if (stateMachine.get("for-loop:declaration")
                     && !stateMachine.get("for-loop:compareExpression")) {
 
                 if (!(s.getLeftExpression() instanceof VariableExpression)) {
                     throw new IllegalStateException("In for-loop: only binary"
                             + " expressions of the form 'a <= b' with a, b being"
                             + " constant integers are supported!");
                 }
 
                 if (!"<=".equals(s.getOperation().getText())) {
                     throw new IllegalStateException("In for-loop: only binary"
                             + " expressions of the form 'a <= b' with a, b being"
                             + " constant integers are supported!");
                 }
 
                 if (!(s.getRightExpression() instanceof ConstantExpression)) {
                     throw new IllegalStateException("In for-loop: only binary"
                             + " expressions of the form 'a <= b' with a, b being"
                             + " constant integers are supported!");
                 }
 
                 ConstantExpression ce = (ConstantExpression) s.getRightExpression();
 
                 if (!(ce.getValue() instanceof Integer)) {
 //                    throw new IllegalStateException("In for-loop: value '" + ce.getValue()
 //                            + "' is not an integer constant! ");
 
                     throw new IllegalStateException("In for-loop: only binary"
                             + " expressions of the form 'a <= b' with a, b being"
                             + " constant integers are supported!");
                 }
 
                 forD.setTo((int) ce.getValue());
 
                 stateMachine.set("for-loop:compareExpression", true);
             } else if (stateMachine.get("for-loop:declaration")
                     && stateMachine.get("for-loop:compareExpression")
                     && !stateMachine.get("for-loop:incExpression")) {
 
                 if (!"+=".equals(s.getOperation().getText())
                         && !"-=".equals(s.getOperation().getText())) {
                     throw new IllegalStateException("In for-loop: inc/dec '"
                             + s.getOperation().getText()
                             + "' not spupported! Must be '+=' or '-='!");
                 }
 
                 if (!(s.getRightExpression() instanceof ConstantExpression)) {
                     throw new IllegalStateException("In for-loop: variable '" + forD.getVarName()
                             + "' must be initialized with an integer constant!");
                 }
 
                 ConstantExpression ce = (ConstantExpression) s.getRightExpression();
 
                 if (!(ce.getValue() instanceof Integer)) {
                     throw new IllegalStateException(
                             "In for-loop: inc/dec must be an integer constant!");
                 }
 
                 if ("+=".equals(s.getOperation().getText())) {
                     forD.setInc((int) ce.getValue());
                 } else if ("-=".equals(s.getOperation().getText())) {
                     forD.setInc(-(int) ce.getValue());
                 }
 
                 stateMachine.set("for-loop:incExpression", true);
 
                 //
             }
         }
 
         super.visitBinaryExpression(s);
     }
 
     @Override
     public void visitBooleanExpression(BooleanExpression s) {
 
         super.visitBooleanExpression(s);
     }
 
     @Override
     public void visitPostfixExpression(PostfixExpression s) {
 
         if (currentScope instanceof ForDeclaration_Impl) {
 
            ForDeclaration_Impl forD = (ForDeclaration_Impl) currentScope;

            if ("++".equals(s.getOperation().getText())) {
                forD.setInc(1);
            } else if ("--".equals(s.getOperation().getText())) {
                forD.setInc(-1);
            }

             stateMachine.set("for-loop:incExpression", true);
         }
 
         super.visitPostfixExpression(s);
     }
 
     @Override
     public void visitPrefixExpression(PrefixExpression expression) {
         super.visitPrefixExpression(expression);
     }
 
     /**
      * @return the rootScope
      */
     public Scope getRootScope() {
         return rootScope;
     }
 
     /**
      * @param rootScope the rootScope to set
      */
     public void setRootScope(Scope rootScope) {
         this.rootScope = rootScope;
     }
 
     private Variable[] convertArguments(ArgumentListExpression args) {
         Variable[] arguments = new Variable[args.getExpressions().size()];
         for (int i = 0; i < args.getExpressions().size(); i++) {
             Expression e = args.getExpression(i);
 
             Variable v = null;
 
             if (e instanceof ConstantExpression) {
                 ConstantExpression ce = (ConstantExpression) e;
 
                 v = VariableFactory.createConstantVariable(currentScope, new Type(ce.getType().getName(), true), "", ce.getValue());
             }
 
             if (e instanceof VariableExpression) {
                 VariableExpression ve = (VariableExpression) e;
 
                 v = VariableFactory.createObjectVariable(currentScope, new Type(ve.getType().getName(), true), ve.getName());
             }
 
             if (e instanceof PropertyExpression) {
                 PropertyExpression pe = (PropertyExpression) e;
 
                 v = VariableFactory.createObjectVariable(currentScope, new Type("vrl.internal.PROPERTYEXPR", true), "don't know");
             }
 
             if (e instanceof MethodCallExpression) {
                 System.out.println("TYPE: " + e);
                 v = currentScope.getVariable(returnValuesOfMethods.get(e));
             }
 
             if (v == null) {
                 System.out.println("TYPE: " + e);
                 v = VariableFactory.createObjectVariable(currentScope, new Type("vrl.internal.unknown", true), "don't know");
             }
 
             arguments[i] = v;
         }
         return arguments;
     }
 
     private Parameters convertMethodParameters(org.codehaus.groovy.ast.Parameter... params) {
 
         Parameter[] result = new Parameter[params.length];
 
         for (int i = 0; i < params.length; i++) {
             org.codehaus.groovy.ast.Parameter p = params[i];
 
             result[i] = new Parameter(new Type(p.getType().getName(), true), p.getName());
         }
 
         return new Parameters(result);
     }
 
     private IModifiers convertModifiers(int modifiers) {
 
         List<Modifier> modifierList = new ArrayList<>();
 
         // TODO rethink modifiers design (21.10.2013)
         if (java.lang.reflect.Modifier.isPublic(modifiers)) {
             modifierList.add(Modifier.PUBLIC);
         } else if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
             modifierList.add(Modifier.PRIVATE);
         } else if (java.lang.reflect.Modifier.isProtected(modifiers)) {
             modifierList.add(Modifier.PROTECTED);
         } else if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
             modifierList.add(Modifier.ABSTRACT);
         } else if (java.lang.reflect.Modifier.isFinal(modifiers)) {
             modifierList.add(Modifier.FINAL);
         } else if (java.lang.reflect.Modifier.isStatic(modifiers)) {
             modifierList.add(Modifier.STATIC);
         }
 
         return new Modifiers(modifierList.toArray(new Modifier[modifierList.size()]));
     }
 
     private Extends convertExtends(ClassNode n) {
 
         ClassNode superType = n.getSuperClass();
 
         Type type = new Type(superType.getName(), false);
 
         Extends result = new Extends(type);
 
         return result;
     }
 
     private Extends convertImplements(ClassNode n) {
 
         Collection<ClassNode> interfaces = n.getAllInterfaces();
 
         Type[] types = new Type[interfaces.size()];
 
         int i = 0;
         for (ClassNode classNode : interfaces) {
             types[i] = new Type(classNode.getName(), false);
             i++;
         }
 
         Extends result = new Extends(types);
 
         return result;
     }
 }
 
 //class ClassVisitor extends org.codehaus.groovy.ast.ClassCodeVisitorSupport {
 //
 //    private SourceUnit sourceUnit;
 //    private ClassNode clsNode;
 //    private VisualCodeBuilder_Impl codeBuilder;
 //    private Scope rootScope;
 //    private Scope currentScope;
 //    private Invocation lastMethod;
 //    private Stack<String> vIdStack = new Stack<>();
 //    private IdGenerator generator = FlowFactory.newIdGenerator();
 //
 //    public ClassVisitor(SourceUnit sourceUnit/*, ClassNode clsNode*/, VisualCodeBuilder_Impl codeBuilder) {
 //
 //        this.sourceUnit = sourceUnit;
 //        this.clsNode = clsNode;
 //        this.codeBuilder = codeBuilder;
 //
 //        codeBuilder.setIdRequest(new IdRequest() {
 //            @Override
 //            public String request() {
 //                return requestId();
 //            }
 //        });
 //
 //        this.rootScope = codeBuilder.createScope(null, ScopeType.CLASS, sourceUnit.getName(), new Object[0]);
 //        this.currentScope = rootScope;
 //
 //
 //    }
 //
 //    private String requestId() {
 //
 //        String result = "";
 //
 //        if (!vIdStack.isEmpty()) {
 //            result = vIdStack.pop();
 //
 //            if (generator.getIds().contains(result)) {
 //                System.err.println(">> requestId(): Id already defined: " + result);
 //                result = generator.newId();
 //            } else {
 //                generator.addId(result);
 //                System.out.println(">> USING ID: " + result);
 //            }
 //        } else {
 //            result = generator.newId();
 //        }
 //
 //        return result;
 //    }
 //
 //    @Override
 //    public void visitClass(ClassNode s) {
 //
 //        currentScope = codeBuilder.createScope(currentScope, ScopeType.CLASS, s.getName(), new Object[0]);
 //
 //        super.visitClass(s);
 //
 //        currentScope = currentScope.getParent();
 //
 //        currentScope.setCode(getCode(s));
 //    }
 //
 //    @Override
 //    public void visitMethod(MethodNode s) {
 //
 //        currentScope = codeBuilder.createScope(currentScope, ScopeType.METHOD, s.getName(), new Object[0]);
 //        currentScope.setCode(getCode(s));
 //
 //        super.visitMethod(s);
 //
 //        currentScope = currentScope.getParent();
 //
 //        currentScope.setCode(getCode(s));
 //    }
 //
 ////    @Override
 ////    public void visitBlockStatement(BlockStatement s) {
 ////        System.out.println(" --> new Scope");
 ////        super.visitBlockStatement(s);
 ////        System.out.println(" --> leave Scope");
 ////    }
 //    @Override
 //    public void visitForLoop(ForStatement s) {
 //        System.out.println(" --> FOR-LOOP: " + s.getVariable());
 //        currentScope = codeBuilder.createScope(currentScope, ScopeType.FOR, "for", new Object[0]);
 ////        currentScope.setCode(sourceUnit.getSource().getReader().);
 //        super.visitForLoop(s);
 //        currentScope = currentScope.getParent();
 //
 //        currentScope.setCode(getCode(s));
 //    }
 //
 //    @Override
 //    public void visitWhileLoop(WhileStatement s) {
 //        System.out.println(" --> WHILE-LOOP: " + s.getBooleanExpression());
 //        currentScope = codeBuilder.createScope(currentScope, ScopeType.WHILE, "while", new Object[0]);
 //        super.visitWhileLoop(s);
 //        currentScope = currentScope.getParent();
 //
 //        currentScope.setCode(getCode(s));
 //    }
 //
 //    @Override
 //    public void visitIfElse(IfStatement ifElse) {
 //        System.out.println(" --> IF-STATEMENT: " + ifElse.getBooleanExpression());
 //
 //        currentScope = codeBuilder.createScope(currentScope, ScopeType.IF, "if", new Object[0]);
 //
 //        ifElse.getBooleanExpression().visit(this);
 //        ifElse.getIfBlock().visit(this);
 //
 //        currentScope = currentScope.getParent();
 //
 //        currentScope = codeBuilder.createScope(currentScope, ScopeType.ELSE, "else", new Object[0]);
 //
 //        Statement elseBlock = ifElse.getElseBlock();
 //        if (elseBlock instanceof EmptyStatement) {
 //            // dispatching to EmptyStatement will not call back visitor, 
 //            // must call our visitEmptyStatement explicitly
 //            visitEmptyStatement((EmptyStatement) elseBlock);
 //        } else {
 //            elseBlock.visit(this);
 //        }
 //
 //        currentScope = currentScope.getParent();
 //
 //        currentScope.setCode(getCode(ifElse));
 //
 //    }
 //
 //    @Override
 //    public void visitConstructorCallExpression(ConstructorCallExpression s) {
 //        System.out.println(" --> CONSTRUCTOR: " + s.getType());
 //
 //        super.visitConstructorCallExpression(s);
 //
 //        ArgumentListExpression args = (ArgumentListExpression) s.getArguments();
 //
 //        Variable[] arguments = convertArguments(args);
 //
 //        codeBuilder.createInstance(
 //                currentScope, s.getType().getName(),
 //                codeBuilder.createVariable(currentScope, s.getType().getName()),
 //                arguments);
 //    }
 //
 //    private String getCode(ASTNode n) {
 //        String code = sourceUnit.getSample(n.getLineNumber(), n.getColumnNumber(), null);
 //        return code;
 //    }
 //
 //    @Override
 //    public void visitMethodCallExpression(MethodCallExpression s) {
 //        System.out.println(" --> METHOD: " + s.getMethodAsString());
 //
 //        super.visitMethodCallExpression(s);
 //
 //        ArgumentListExpression args = (ArgumentListExpression) s.getArguments();
 //        Variable[] arguments = convertArguments(args);
 //
 //        String objectName = "noname";
 //
 //        boolean isIdCall = false;
 //
 //        if (s.getObjectExpression() instanceof VariableExpression) {
 //            VariableExpression ve = (VariableExpression) s.getObjectExpression();
 //            objectName = ve.getName();
 //        } else if (s.getObjectExpression() instanceof ClassExpression) {
 //            ClassExpression ce = (ClassExpression) s.getObjectExpression();
 //            objectName = ce.getType().getName();
 //
 //            if (ce.getType().getName().equals(VSource.class.getName())) {
 //                isIdCall = true;
 //                System.out.println(">> VSource: push");
 //                for (Variable arg : arguments) {
 //                    System.out.println(" -->" + arg.getValue().toString());
 //                    vIdStack.push(arg.getValue().toString());
 //                }
 //            }
 //        }
 //
 //        String returnValueName = "void";
 //
 //        boolean isVoid = false;
 //
 //        if (!isVoid) {
 //            returnValueName = codeBuilder.createVariable(currentScope, "java.lang.Object");
 //        }
 //
 //        if (!isIdCall) {
 //            System.out.println("ID-CALL: ");
 //            codeBuilder.invokeMethod(currentScope, objectName, s.getMethod().getText(), isVoid,
 //                    returnValueName, arguments).setCode(getCode(s));
 //        }
 //    }
 //
 //    @Override
 //    public void visitStaticMethodCallExpression(StaticMethodCallExpression s) {
 //        super.visitStaticMethodCallExpression(s);
 //
 //        ArgumentListExpression args = (ArgumentListExpression) s.getArguments();
 //        Variable[] arguments = convertArguments(args);
 //
 //        String returnValueName = "void";
 //
 //        boolean isVoid = false;
 //
 //        if (!isVoid) {
 //            returnValueName = codeBuilder.createVariable(currentScope, "java.lang.Object");
 //        }
 //
 //        codeBuilder.invokeMethod(currentScope, s.getType().getName(), s.getText(), isVoid,
 //                returnValueName, arguments).setCode(getCode(s));
 //    }
 //
 //    @Override
 //    public void visitDeclarationExpression(DeclarationExpression s) {
 //        System.out.println(" --> DECLARATION: " + s.getVariableExpression());
 //        super.visitDeclarationExpression(s);
 //        codeBuilder.createVariable(currentScope, s.getVariableExpression().getType().getName(), s.getVariableExpression().getName());
 //
 //        if (s.getRightExpression() instanceof ConstantExpression) {
 //            ConstantExpression ce = (ConstantExpression) s.getRightExpression();
 //            codeBuilder.assignConstant(currentScope, s.getVariableExpression().getName(), ce.getValue());
 //        }
 //    }
 //
 //    @Override
 //    protected SourceUnit getSourceUnit() {
 //        return sourceUnit;
 //    }
 //
 //    @Override
 //    public void visitBinaryExpression(BinaryExpression s) {
 //
 //        super.visitBinaryExpression(s);
 //    }
 //
 //    /**
 //     * @return the rootScope
 //     */
 //    public Scope getRootScope() {
 //        return rootScope;
 //    }
 //
 //    /**
 //     * @param rootScope the rootScope to set
 //     */
 //    public void setRootScope(Scope rootScope) {
 //        this.rootScope = rootScope;
 //    }
 //
 //    private Variable[] convertArguments(ArgumentListExpression args) {
 //        Variable[] arguments = new Variable[args.getExpressions().size()];
 //        for (int i = 0; i < args.getExpressions().size(); i++) {
 //            Expression e = args.getExpression(i);
 //
 //            Variable v = null;
 //
 //            if (e instanceof ConstantExpression) {
 //                ConstantExpression ce = (ConstantExpression) e;
 //
 //                // TODO WHY no name???
 //                v = VariableFactory.createConstantVariable(currentScope, ce.getType().getName(), "", ce.getValue());
 //            }
 //
 //            if (e instanceof VariableExpression) {
 //                VariableExpression ve = (VariableExpression) e;
 //
 //                v = currentScope.getVariable(ve.getName());
 //            }
 //
 //            if (e instanceof PropertyExpression) {
 //                PropertyExpression pe = (PropertyExpression) e;
 //
 //                v = VariableFactory.createObjectVariable(currentScope, "PROPERTYEXPR", "don't know");
 //            }
 //
 //            if (v == null) {
 //                System.out.println("TYPE: " + e);
 //                v = VariableFactory.createObjectVariable(currentScope, "unknown", "don't know");
 //            }
 //
 //            arguments[i] = v;
 //        }
 //        return arguments;
 //    }
 //    
 //    
 //    private static List<?> convertMethodParameters(MethodNode s) {
 //        throw new UnsupportedOperationException("TODO NB-AUTOGEN: Not supported yet."); // TODO NB-AUTOGEN
 //    }
 //}
