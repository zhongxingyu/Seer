 package xhl.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.annotation.*;
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import xhl.core.elements.Block;
 import xhl.core.elements.SList;
 import xhl.core.elements.Symbol;
 import xhl.core.validator.*;
 import xhl.core.validator.ElementSchema.ParamSpec;
 
 import com.google.common.base.Optional;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static java.util.Arrays.asList;
 
 import static com.google.common.base.Predicates.instanceOf;
 import static com.google.common.collect.Iterables.tryFind;
 import static com.google.common.collect.Lists.newArrayList;
 
 /**
  * Base class for implementing new modules.
  *
  * Allows to implement module functions as Java methods marked with annotation
  * <code>&#64;Element</code>.
  *
  * @author Sergej Chodarev
  */
 public abstract class GenericModule implements Module {
 
     protected Evaluator evaluator;
     private final Environment<Object> table = new Environment<Object>();
     private final Map<Symbol, ElementValidator> validators = newHashMap();
 
     public GenericModule() {
         findEvalFunctions();
         findCheckFunctions();
     }
 
     @Override
     public Environment<Object> getSymbols() {
         return table;
     }
 
     @Override
     public void setEvaluator(Evaluator evaluator) {
         this.evaluator = evaluator;
     }
 
     /**
      * Return module schema.
      *
      * Try to read module schema from a file with the same name as the class but
      * with extension ".schema". If it is not available, create generic schema,
      * that contains all elements defined in the module with the most generic
      * properties allowing to pass the validation.
      */
     @Override
     public Schema getSchema() {
         Schema schema;
         InputStream in = findSchemaStream();
         if (in != null) {
             ValidatorLanguage lang = new ValidatorLanguage();
             LanguageProcessor schemaProcessor = new LanguageProcessor(lang);
             Block program;
             try {
                 program = Reader.read(new InputStreamReader(in));
                 schemaProcessor.executeWithoutValidation(program);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
            schema = lang.getReadSchema();
         } else {
             schema = makeGenericSchema();
         }
         addCustomValidators(schema);
         return schema;
     }
 
     protected InputStream findSchemaStream() {
         Class<? extends GenericModule> clazz = this.getClass();
         return clazz.getResourceAsStream(clazz.getSimpleName() + ".schema");
     }
 
     private Schema makeGenericSchema() {
         Schema schema = new Schema();
         for (Symbol symbol : table.keySet()) {
             ElementSchema elem = new ElementSchema(symbol);
             elem.setType(Type.AnyType);
             elem.setParams(newArrayList(ParamSpec.variadic(ParamSpec
                     .val(Type.AnyType))));
             schema.put(elem);
         }
         return schema;
     }
 
     private void addCustomValidators(Schema schema) {
         for (ElementSchema element : schema) {
             if (validators.containsKey(element.getSymbol()))
                 element.setValidator(validators.get(element.getSymbol()));
         }
     }
 
     /**
      * Export symbol from module
      */
     protected void addSymbol(Symbol symbol, Object value) {
         table.put(symbol, value);
     }
 
     /**
      * Export self-referencing symbol
      */
     protected void addKeyword(Symbol symbol) {
         table.put(symbol, symbol);
     }
 
     private void findEvalFunctions() {
         Method[] methods = this.getClass().getDeclaredMethods();
         for (Method method : methods) {
             tryAddFunction(table, method);
         }
     }
 
     private void findCheckFunctions() {
         Method[] methods = this.getClass().getDeclaredMethods();
         for (Method method : methods) {
             tryAddCheckFunction(method);
         }
     }
 
     /**
      * If method has <code>&#64;Element</code> annotation, add it to symbols
      * table.
      */
     private void tryAddFunction(Environment<Object> table, Method method) {
         Element annotation = method.getAnnotation(Element.class);
         if (annotation != null) {
             // Find function name
             String name;
             if (annotation.name().equals(""))
                 name = method.getName();
             else
                 name = annotation.name();
 
             // Check which parameters to evaluate
             Annotation[][] pann = method.getParameterAnnotations();
             boolean[] evaluateArgs = new boolean[pann.length];
             for (int i = 0; i < pann.length; i++) {
                 Optional<Annotation> ann =
                         tryFind(asList(pann[i]), instanceOf(Symbolic.class));
                 if (ann.isPresent())
                     evaluateArgs[i] = false;
                 else
                     evaluateArgs[i] = annotation.evaluateArgs();
             }
 
             Executable exec = new GenericExecutable(method, evaluateArgs);
             table.put(new Symbol(name), exec);
         }
     }
 
     /**
      * If a method has the <code>&#64;Check</code> annotation,
      * register it as a check function for an element.
      */
     private void tryAddCheckFunction(Method method) {
         Check annotation = method.getAnnotation(Check.class);
         if (annotation != null) {
             String name = annotation.name();
             ElementValidator validator = new CustomValidator(method);
             validators.put(new Symbol(name), validator);
         }
     }
 
     /**
      * Indicates that a method is an evaluation function for a module element.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     protected @interface Element {
         /** Element name. If not specified, method name is uses. */
         public String name() default "";
 
         public boolean evaluateArgs() default true;
     }
 
     /**
      * Mark for an element check function.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     protected @interface Check {
         /** The name of checked method. */
         public String name();
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.PARAMETER)
     protected @interface Symbolic {
     }
 
 
     private class GenericExecutable implements Executable {
         private final Method method;
         private final boolean[] evaluateArgs;
 
         public GenericExecutable(Method method, boolean[] evaluateArgs) {
             this.method = method;
             this.evaluateArgs = evaluateArgs;
         }
 
         @Override
         public Object exec(SList args) throws EvaluationException {
             // Prepare arguments
             List<Object> evArgs = new ArrayList<Object>(args.size());
             for (int i = 0; i < args.size(); i++) {
                 if (i >= evaluateArgs.length // for varargs
                         || evaluateArgs[i])
                     evArgs.add(evaluator.eval(args.get(i)));
                 else
                     evArgs.add(args.get(i));
             }
             processBuilders(evArgs);
 
             if (method.isVarArgs())
                 packVarArgs(evArgs);
 
             try {
                 return method.invoke(GenericModule.this, evArgs.toArray());
             } catch (InvocationTargetException e) {
                 throw new EvaluationException(e.getCause());
             } catch (Exception e) {
                 throw new EvaluationException(e);
             }
         }
 
         private void processBuilders(List<Object> evArgs) {
             Class<?>[] params = method.getParameterTypes();
             for (int i = 0; i < evArgs.size(); i++) {
                 Object arg = evArgs.get(i);
                 Class<?> param;
                 if (i < params.length)
                     param = params[i];
                 else // varargs
                     param = params[params.length - 1];
                 boolean pb = Producer.class.isAssignableFrom(param);
                 boolean ab = Producer.class.isAssignableFrom(arg.getClass());
                 if (!pb && ab) {
                     evArgs.set(i, ((Producer<?>) arg).toValue());
                 } else if (pb && !ab) {
                     ConstProducer<?> b = new ConstProducer<Object>(arg);
                     evArgs.set(i, b);
                 }
             }
         }
 
         /**
          * Pack arguments to array for method with variable number of arguments.
          */
         private void packVarArgs(List<Object> evArgs) {
             Class<?>[] params = method.getParameterTypes();
             int last = params.length - 1; // index of varargs parameter
             Class<?> type = params[last].getComponentType(); // it's type
 
             Object[] varArgs = evArgs.subList(last, evArgs.size()).toArray();
 
             // New array for varargs, properly typed
             Object[] varArgsT =
                     (Object[]) Array.newInstance(type, varArgs.length);
             System.arraycopy(varArgs, 0, varArgsT, 0, varArgs.length);
 
             // Pack varargs
             evArgs.set(last, varArgsT);
             evArgs.subList(last + 1, evArgs.size()).clear();
         }
     }
 
 
     private class CustomValidator implements ElementValidator {
 
         private final Method method;
 
         public CustomValidator(Method method) {
             this.method = method;
         }
 
         @Override
         public Map<Symbol, Type> forwardDefinitions(SList args) {
             return newHashMap();
         }
 
         @Override
         public ValidationResult check(Validator validator, SList tail) {
             Object[] args = new Object[tail.size() + 1];
             args[0] = validator;
             for (int i = 0; i < tail.size(); i++) {
                 args[i+1] = tail.get(i);
             }
             try {
                 return (ValidationResult) method.invoke(GenericModule.this,
                         args);
             } catch (InvocationTargetException e) {
                 throw new EvaluationException(e.getCause());
             } catch (Exception e) {
                 throw new EvaluationException(e);
             }
         }
     }
 }
