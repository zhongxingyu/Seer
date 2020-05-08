 package snacks.lang;
 
 import static me.qmx.jitescript.util.CodegenUtils.*;
 import static org.objectweb.asm.Opcodes.ACC_FINAL;
 import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
 import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
 import static org.objectweb.asm.Opcodes.ACC_STATIC;
 import static snacks.lang.SnacksRuntime.BOOTSTRAP;
 import static snacks.lang.compiler.ast.Type.isInstantiable;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.*;
 import me.qmx.jitescript.CodeBlock;
 import me.qmx.jitescript.JDKVersion;
 import me.qmx.jitescript.JiteClass;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.tree.LabelNode;
 import snacks.lang.compiler.ast.*;
 
 public class Compiler implements AstVisitor {
 
     private static final Map<String, String> replacements;
 
     static {
         replacements = new LinkedHashMap<>();
         replacements.put("?", "$Query");
         replacements.put("!", "$Bang");
         replacements.put("+", "$Plus");
         replacements.put("**", "$Exponent");
         replacements.put("*", "$Multiply");
         replacements.put("/", "$Divide");
         replacements.put("%", "$Modulo");
         replacements.put("unary+", "$Positive");
         replacements.put("unary-", "$Negative");
         replacements.put("unary~", "$BitNot");
         replacements.put("&", "$BitAnd");
         replacements.put("|", "$BitOr");
         replacements.put("^", "$BitXor");
         replacements.put("[]", "$Index");
         replacements.put("...", "$XRange");
         replacements.put("..", "$Range");
         replacements.put("==", "$Equals");
         replacements.put("<<", "$LShift");
         replacements.put(">>>", "$URShift");
         replacements.put(">>", "$RShift");
         replacements.put("<=", "$LessThanEquals");
         replacements.put(">=", "$GreaterThanEquals");
         replacements.put("<", "$LessThan");
         replacements.put(">", "$GreaterThan");
     }
 
     private final List<JiteClass> acceptedClasses;
     private final Deque<State> states;
     private boolean popValue;
 
     public Compiler() {
         acceptedClasses = new ArrayList<>();
         states = new ArrayDeque<>();
     }
 
     public ClassLoader compile(Set<AstNode> declarations) throws CompileException {
         for (AstNode declaration : declarations) {
             compile(declaration);
         }
         SnacksLoader loader = new SnacksLoader(getClass().getClassLoader());
         for (JiteClass jiteClass : acceptedClasses) {
             byte[] bytes = jiteClass.toBytes(JDKVersion.V1_7);
             loader.defineClass(c(jiteClass.getClassName()), bytes);
             writeClass(new File(jiteClass.getClassName() + ".class"), bytes);
         }
         return loader;
     }
 
     @Override
     public void visitApply(Apply node) {
         compile(node.getFunction());
         compile(node.getArgument());
         block().invokedynamic("apply", sig(Object.class, Object.class, Object.class), BOOTSTRAP);
     }
 
     @Override
     public void visitBooleanConstant(BooleanConstant node) {
         block().ldc(node.getValue());
        block().invokestatic(p(Boolean.class), "valueOf", sig(Boolean.class, boolean.class));
     }
 
     @Override
     public void visitClosure(Closure node) {
         state().setFields(node.getEnvironment());
         defineClosureFields(node);
         defineClosureConstructor(node);
         compileApply(node.getBody());
     }
 
     @Override
     public void visitClosureLocator(ClosureLocator locator) {
         CodeBlock block = block();
         String className = javaClass(locator);
         block.newobj(className);
         block.dup();
         for (String variable : locator.getEnvironment()) {
             loadVariable(variable);
         }
         block.invokespecial(className, "<init>", sig(params(void.class, Object.class, locator.getEnvironment().size())));
     }
 
     @Override
     public void visitDeclarationLocator(ExpressionLocator locator) {
         CodeBlock block = block();
         String className = javaClass(locator);
         block.invokestatic(className, "instance", sig(Object.class));
     }
 
     @Override
     public void visitDeclaredArgument(DeclaredArgument node) {
         // intentionally empty
     }
 
     @Override
     public void visitDeclaredExpression(DeclaredExpression node) {
         beginClass(javaClass(node), interfacesFor(node.getType()));
         compile(node.getBody());
         acceptClass();
     }
 
     @Override
     public void visitDoubleConstant(DoubleConstant node) {
         block().ldc(node.getValue());
     }
 
     @Override
     public void visitExpressionConstant(ExpressionConstant node) {
         CodeBlock block = beginBlock();
         LabelNode returnValue = new LabelNode(new Label());
         JiteClass jiteClass = jiteClass();
         jiteClass.defineField("instance", ACC_PRIVATE | ACC_STATIC, ci(Object.class), null);
         block.getstatic(jiteClass.getClassName(), "instance", ci(Object.class));
         block.ifnonnull(returnValue);
         compile(node.getValue());
         block.putstatic(jiteClass.getClassName(), "instance", ci(Object.class));
         block.label(returnValue);
         block.getstatic(jiteClass.getClassName(), "instance", ci(Object.class));
         block.areturn();
         jiteClass.defineMethod("instance", ACC_PUBLIC | ACC_STATIC, sig(Object.class), acceptBlock());
     }
 
     @Override
     public void visitFunction(Function node) {
         defineFunctionInitializer();
         compileApply(node.getBody());
     }
 
     @Override
     public void visitIntegerConstant(IntegerConstant node) {
         CodeBlock block = block();
         block().ldc(node.getValue());
         block.invokestatic(p(Integer.class), "valueOf", sig(Integer.class, int.class));
     }
 
     @Override
     public void visitReference(Reference node) {
         compile(node.getLocator());
     }
 
     @Override
     public void visitResult(Result node) {
         compile(node.getValue());
         block().areturn();
     }
 
     @Override
     public void visitSequence(Sequence node) {
         List<AstNode> elements = node.getElements();
         for (int i = 0; i < elements.size() - 1; i++) {
             popValue = true;
             compile(elements.get(i));
             if (popValue) {
                 block().pop();
             }
         }
         compile(elements.get(elements.size() - 1));
     }
 
     @Override
     public void visitStringConstant(StringConstant node) {
         block().ldc(node.getValue());
     }
 
     @Override
     public void visitVariableDeclaration(VariableDeclaration node) {
         compile(node.getValue());
         block().astore(getVariable(node.getName()));
         popValue = false;
     }
 
     @Override
     public void visitVariableLocator(VariableLocator locator) {
         loadVariable(locator.getName());
     }
 
     @Override
     public void visitVoidApply(VoidApply node) {
         compile(node.getInstantiable());
         block().invokeinterface(p(Invokable.class), "invoke", sig(Object.class));
     }
 
     @Override
     public void visitVoidFunction(VoidFunction node) {
         defineFunctionInitializer();
         beginBlock();
         compile(node.getBody());
         if (!block().returns()) {
             block().areturn();
         }
         jiteClass().defineMethod("invoke", ACC_PUBLIC, sig(Object.class), acceptBlock());
     }
 
     private CodeBlock acceptBlock() {
         return state().acceptBlock();
     }
 
     private void acceptClass() {
         acceptedClasses.add(states.pop().getJiteClass());
     }
 
     private CodeBlock beginBlock() {
         return state().beginBlock();
     }
 
     private JiteClass beginClass(String name, List<String> interfaces) {
         JiteClass jiteClass = new JiteClass(name, p(Object.class), interfaces.toArray(new String[interfaces.size()]));
         states.push(new State(jiteClass));
         return jiteClass;
     }
 
     private CodeBlock block() {
         return state().block();
     }
 
     private void compile(AstNode node) {
         node.accept(this);
     }
 
     private void compile(Locator locator) {
         locator.accept(this);
     }
 
     private void compileApply(AstNode body) {
         CodeBlock block = beginBlock();
         compile(body);
         if (!block.returns()) {
             block.areturn();
         }
         jiteClass().defineMethod("apply", ACC_PUBLIC, sig(Object.class, Object.class), acceptBlock());
     }
 
     private void defineClosureConstructor(Closure closure) {
         String signature = sig(params(void.class, Object.class, closure.getEnvironment().size()));
         CodeBlock block = beginBlock();
         block.aload(0);
         block.invokespecial(p(Object.class), "<init>", sig(void.class));
         int i = 1;
         for (String field : closure.getEnvironment()) {
             block.aload(0);
             block.aload(i++);
             block.putfield(jiteClass().getClassName(), field, ci(Object.class));
         }
         block.voidreturn();
         jiteClass().defineMethod("<init>", ACC_PUBLIC, signature, acceptBlock());
     }
 
     private void defineClosureFields(Closure closure) {
         JiteClass jiteClass = jiteClass();
         for (String field : closure.getEnvironment()) {
             jiteClass.defineField(field, ACC_PRIVATE | ACC_FINAL, ci(Object.class), null);
         }
     }
 
     private void defineFunctionInitializer() {
         JiteClass jiteClass = jiteClass();
         CodeBlock block = beginBlock();
         LabelNode returnValue = new LabelNode(new Label());
         jiteClass.defineField("instance", ACC_PRIVATE | ACC_STATIC, ci(Object.class), null);
         block.getstatic(jiteClass.getClassName(), "instance", ci(Object.class));
         block.ifnonnull(returnValue);
         block.newobj(jiteClass.getClassName());
         block.dup();
         block.invokespecial(jiteClass.getClassName(), "<init>", sig(void.class));
         block.putstatic(jiteClass.getClassName(), "instance", ci(Object.class));
         block.label(returnValue);
         block.getstatic(jiteClass.getClassName(), "instance", ci(Object.class));
         block.areturn();
         jiteClass.defineMethod("instance", ACC_PUBLIC | ACC_STATIC, sig(Object.class), acceptBlock());
         jiteClass.defineDefaultConstructor();
     }
 
     private int getVariable(String name) {
         return state().getVariable(name);
     }
 
     private List<String> interfacesFor(Type type) {
         List<String> interfaces = new ArrayList<>();
         if (isInstantiable(type)) {
             interfaces.add(p(Invokable.class));
         }
         return interfaces;
     }
 
     private boolean isField(String name) {
         return state().isField(name);
     }
 
     private boolean isVariable(String name) {
         return state().isVariable(name);
     }
 
     private String javaClass(DeclaredExpression expression) {
         return javaClass(expression.getModule(), expression.getName());
     }
 
     private String javaClass(ExpressionLocator locator) {
         return javaClass(locator.getModule(), locator.getName());
     }
 
     private String javaClass(ClosureLocator locator) {
         return javaClass(locator.getModule(), locator.getName());
     }
 
     private String javaClass(String module, String name) {
         return module + "/" + javafy(name);
     }
 
     private String javafy(String name) {
         String javafiedName;
         if ("?".equals(name)) {
             javafiedName = "$Coalesce";
         } else {
             javafiedName = name.substring(0, 1).toUpperCase() + name.substring(1);
             for (String replacement : replacements.keySet()) {
                 javafiedName = javafiedName.replace(replacement, replacements.get(replacement));
             }
         }
         if (javafiedName.startsWith("$")) {
             javafiedName = "Op" + javafiedName;
         }
         return javafiedName;
     }
 
     private JiteClass jiteClass() {
         return state().jiteClass;
     }
 
     private void loadVariable(String name) {
         if (isField(name)) {
             block().aload(0);
             block().getfield(jiteClass().getClassName(), name, ci(Object.class));
         } else if (isVariable(name)) {
             block().aload(getVariable(name));
         } else {
             block().aload(1);
         }
     }
 
     private State state() {
         return states.peek();
     }
 
     private void writeClass(File file, byte[] bytes) throws CompileException {
         try {
             if (!file.getParentFile().mkdirs() && !file.getParentFile().exists()) {
                 throw new IOException("Failed to mkdirs: " + file.getParentFile());
             }
             try (FileOutputStream output = new FileOutputStream(file)) {
                 output.write(bytes);
             }
         } catch (IOException exception) {
             throw new CompileException(exception);
         }
     }
 
     private static final class BlockState {
 
         private final CodeBlock block;
         private final Map<String, Integer> variables;
 
         public BlockState(CodeBlock block) {
             this.block = block;
             this.variables = new LinkedHashMap<>();
         }
 
         public CodeBlock getBlock() {
             return block;
         }
 
         public int getVariable(String name) {
             if (!isVariable(name)) {
                 variables.put(name, variables.size() + 2);
             }
             return variables.get(name);
         }
 
         public boolean isVariable(String name) {
             return variables.containsKey(name);
         }
     }
 
     private static final class SnacksLoader extends ClassLoader {
 
         public SnacksLoader(ClassLoader parent) {
             super(parent);
         }
 
         public Class defineClass(String name, byte[] bytes) {
             return super.defineClass(name, bytes, 0, bytes.length);
         }
     }
 
     private static final class State {
 
         private final JiteClass jiteClass;
         private final Deque<BlockState> blocks;
         private List<String> fields;
 
         public State(JiteClass jiteClass) {
             this.jiteClass = jiteClass;
             this.fields = new ArrayList<>();
             this.blocks = new ArrayDeque<>();
         }
 
         public CodeBlock acceptBlock() {
             return blocks.pop().getBlock();
         }
 
         public CodeBlock beginBlock() {
             blocks.push(new BlockState(new CodeBlock()));
             return block();
         }
 
         public CodeBlock block() {
             return blocks.peek().getBlock();
         }
 
         public JiteClass getJiteClass() {
             return jiteClass;
         }
 
         public int getVariable(String name) {
             return blocks.peek().getVariable(name);
         }
 
         public boolean isField(String name) {
             return fields.contains(name);
         }
 
         public boolean isVariable(String name) {
             return blocks.peek().isVariable(name);
         }
 
         public void setFields(Collection<String> fields) {
             this.fields = new ArrayList<>(fields);
         }
     }
 }
