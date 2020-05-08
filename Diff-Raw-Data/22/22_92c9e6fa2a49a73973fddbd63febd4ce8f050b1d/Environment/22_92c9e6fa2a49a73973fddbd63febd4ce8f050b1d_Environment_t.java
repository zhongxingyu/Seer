 package marco.internal;
 
 import marco.internal.bindings.*;
 import marco.lang.*;
 import marco.lang.exception.BindingError;
 import marco.lang.exception.LookUpError;
 import marco.lang.exception.MarcoException;
 import marco.lang.functions.*;
 import marco.lang.functions.numbers.*;
 import marco.lang.macros.*;
 import marco.parser.Parser;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Environment {
     private Map<String, Binding> bindings = new HashMap<>();
 
     public Environment() {
     }
 
     public void prelet(String var) {
         bindings.put(var, new LetBinding(var, null));
     }
 
     public void predefine(String var) {
         if (bindings.containsKey(var)) {
             throw new BindingError(var, bindings.get(var).getValue());
         } else {
             bindings.put(var, new ImmutableBinding(var, null));
         }
     }
 
     public void redefine(String var, MarcoObject value) {
         if (bindings.containsKey(var)) {
             bindings.get(var).redefine(value);
         } else {
             throw new MarcoException("bug");
         }
     }
 
     public void def(String var, MarcoObject value) {
         if (bindings.containsKey(var)) {
             throw new BindingError(var, bindings.get(var).getValue());
         } else {
             bindings.put(var, new ImmutableBinding(var, value));
         }
     }
 
     public void var(String var, MarcoObject value) {
         if (bindings.containsKey(var)) {
             throw new BindingError(var, bindings.get(var).getValue());
         } else {
             bindings.put(var, new MutableBinding(var, value));
         }
     }
 
     public void parameter(String var, MarcoObject value) {
         bindings.put(var, new ParameterBinding(var, value));
     }
 
     public void let(String var, MarcoObject value) {
         bindings.put(var, new LetBinding(var, value));
     }
 
     public MarcoObject lookUp(String var) {
         if (bindings.containsKey(var)) {
             return bindings.get(var).getValue();
         } else {
             throw new LookUpError(var);
         }
     }
 
     public Environment duplicate() {
         Map<String, Binding> newEnvMap = new HashMap();
         newEnvMap.putAll(bindings);
         Environment newEnv = new Environment();
         newEnv.bindings = newEnvMap;
         return newEnv;
     }
 
     public MarcoProgram parse(String code) {
         return Parser.instance().parse(code);
     }
 
     public static Environment initial() {
         Environment environment = new Environment();
 
         loadNativeBindings(environment);
         loadStandardLibrary(environment);
 
         return environment;
     }
 
     private static void loadStandardLibrary(Environment environment) {
         MarcoProgram program = MarcoLoader.load("core");
         program.eval(environment);
     }
 
     private static void loadNativeBindings(Environment environment) {
         environment.def("function", new function());
         environment.def("function?", new MarcoFunction(environment, Arrays.asList("v"), new functionquestion()));
         environment.def("if", new ifmacro());
         environment.def("and", new and());
         environment.def("or", new or());
 
         environment.def("quote", new quote());
 
         environment.def("nil", MarcoNil.NIL);
         environment.def("true", MarcoBoolean.TRUE);
         environment.def("false", MarcoBoolean.FALSE);
 
         environment.def("def", new def());
         environment.def("var", new var());
         environment.def("set!", new setbang());
         environment.def("let", new let());
 
         environment.def("print", new MarcoFunction(environment, Arrays.asList("e"), new print()));
 
         environment.def("parse", new MarcoFunction(environment, Arrays.asList("code"), new parse(Parser.instance())));
         environment.def("eval", new eval());
         environment.def("do", new domacro());
         environment.def("while", new whilemacro());
 
         environment.def("cons", new MarcoFunction(environment, Arrays.asList("first", "second"), new cons()));
         environment.def("first", new MarcoFunction(environment, Arrays.asList("pair"), new first()));
         environment.def("second", new MarcoFunction(environment, Arrays.asList("pair"), new second()));
         environment.def("pair?", new MarcoFunction(environment, Arrays.asList("pair"), new pairquestion()));
 
         environment.def("nil?", new MarcoFunction(environment, Arrays.asList("arg"), new nilquestion()));
 
         environment.def("=", new BinaryOperator(environment, new equal()));
         environment.def("<", new BinaryOperator(environment, new lessthan()));
         environment.def(">", new BinaryOperator(environment, new greaterthan()));
         environment.def("+", new BinaryOperator(environment, new plus()));
         environment.def("-", new BinaryOperator(environment, new minus()));
         environment.def("*", new BinaryOperator(environment, new multiplication()));
         environment.def("/", new BinaryOperator(environment, new division()));
         environment.def("%", new BinaryOperator(environment, new remainder()));
     }
 
     public Environment filter(List<String> vars) {
         Environment result = new Environment();
 
         for (String var : vars) {
             Binding binding = bindings.get(var);
             if (binding == null) {
                 binding = new EmptyBinding(var);
             }
            result.bindings.put(var, binding);
         }
 
         return result;
     }
 
    public Binding get(String name) {
        if (bindings.containsKey(name)) {
            return bindings.get(name);
        } else {
            throw new LookUpError(name);
        }
     }
 }
