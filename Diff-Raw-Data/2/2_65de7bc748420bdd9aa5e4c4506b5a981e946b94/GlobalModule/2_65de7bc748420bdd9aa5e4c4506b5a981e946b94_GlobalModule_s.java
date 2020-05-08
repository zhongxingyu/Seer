 package sterling.lang.runtime;
 
 import static java.lang.ClassLoader.getSystemClassLoader;
 import static java.util.Arrays.asList;
 import static java.util.regex.Pattern.quote;
 import static sterling.lang.runtime.expression.ExpressionFactory.declaration;
 import static sterling.lang.runtime.expression.ExpressionFactory.module;
 import static sterling.lang.util.StringUtil.stringify;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import sterling.lang.SterlingException;
 import sterling.lang.compiler.Compiler;
 import sterling.lang.runtime.exception.LoadModuleException;
 import sterling.lang.runtime.expression.*;
 import sterling.lang.compiler.scanner.InputReader;
 
 public class GlobalModule implements ExpressionLoader {
 
     public static ExpressionLoader global() {
        return new GlobalModule(new ClasspathResolver(getSystemClassLoader()), new sterling.lang.compiler.Compiler());
     }
 
     private final Compiler compiler;
     private final ClasspathResolver resolver;
     private final Map<String, Expression> references;
     private final Map<String, Symbol> symbols;
     private final Map<Symbol, DeclaredExpression> members;
     private final Set<Symbol> loadedMembers;
     private final GlueExpression glue;
 
     public GlobalModule(ClasspathResolver resolver, Compiler compiler) {
         this.compiler = compiler;
         this.resolver = resolver;
         this.references = new ConcurrentHashMap<>();
         this.symbols = new ConcurrentHashMap<>();
         this.members = new ConcurrentHashMap<>();
         this.loadedMembers = new CopyOnWriteArraySet<>();
         this.glue = new GlueExpression(this);
     }
 
     @Override
     public Expression glue() {
         return glue;
     }
 
     @Override
     public Expression load(String name) throws SterlingException {
         Iterator<String> parts = asList(name.split(quote("/"))).iterator();
         if (parts.hasNext()) {
             Expression expression = getMember(symbol(parts.next()));
             while(parts.hasNext()) {
                 expression = expression.access(symbol(parts.next()));
             }
             return expression;
         } else {
             throw new IllegalArgumentException("Illegal identifier: " + name);
         }
     }
 
     @Override
     public Module loadModule(Symbol module) throws SterlingException {
         URL url = resolver.getModuleUrl(module);
         if (url == null) {
             return module(module, this);
         } else {
             try (InputReader reader = readSource(url)) {
                 return module(module, this, compiler.compile(reader, module, this));
             } catch (IOException exception) {
                 throw new LoadModuleException(exception);
             }
         }
     }
 
     @Override
     public Expression reference(String identifier) {
         if (!references.containsKey(identifier)) {
             references.put(identifier, new Reference(symbol(identifier), this));
         }
         return references.get(identifier);
     }
 
     @Override
     public Symbol symbol(String identifier) {
         if (!symbols.containsKey(identifier)) {
             symbols.put(identifier, new Symbol(identifier));
         }
         return symbols.get(identifier);
     }
 
     @Override
     public String toString() {
         return stringify(this);
     }
 
     private Expression getMember(Symbol symbol) throws SterlingException {
         if (isUndefined(symbol)) {
             load(symbol);
         }
         return members.get(symbol).getExpression();
     }
 
     private boolean isUndefined(Symbol symbol) {
         return !members.containsKey(symbol);
     }
 
     private void load(Symbol subModule) throws SterlingException {
         if (!loadedMembers.contains(subModule)) {
             loadedMembers.add(subModule);
             members.put(subModule, declaration(subModule, loadModule(subModule)));
         }
     }
 
     private InputReader readSource(URL url) throws IOException {
         return new InputReader(url.toString(), url.openStream());
     }
 }
