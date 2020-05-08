 package marco;
 
 import marco.internal.Environment;
 import marco.lang.*;
 import marco.lang.functions.*;
 import marco.lang.macros.*;
 import marco.parser.Parser;
 
 import java.util.Arrays;
 
 public class MarcoRuntime {
 
     private Parser parser = new Parser();
 
     private Environment environment = new Environment();
 
     public MarcoRuntime() {
         environment.bind("macro", new macro());
         environment.bind("quote", new quote());
        environment.bind("eval", new MarcoFunction(environment, Arrays.asList("arg"), new eval()));
 
        environment.bind("def", new def());
        environment.bind("function", new function());
         environment.bind("nil", MarcoNil.NIL);
        environment.bind("+", new MarcoFunction(new Environment(), Arrays.asList("v1", "v2"), new plus()));
         environment.bind("true", MarcoBoolean.TRUE);
         environment.bind("false", MarcoBoolean.FALSE);
         environment.bind("var", new var());
         environment.bind("set!", new setbang());
        environment.bind("cons", new MarcoFunction(new Environment(), Arrays.asList("first", "second"), new cons()));
        environment.bind("first", new MarcoFunction(new Environment(), Arrays.asList("pair"), new first()));
        environment.bind("second", new MarcoFunction(new Environment(), Arrays.asList("pair"), new second()));
        environment.bind("nil?", new MarcoFunction(new Environment(), Arrays.asList("arg"), new nilquestion()));
        environment.bind("pair?", new MarcoFunction(new Environment(), Arrays.asList("pair"), new pairquestion()));
     }
 
     public MarcoObject eval(String code) {
         MarcoProgram program = parser.parse(code);
         return program.eval(environment);
     }
 
     public MarcoObject lookUp(String var) {
         return environment.lookUp(var);
     }
 }
