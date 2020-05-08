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
        environment.bind("function", new function());

         environment.bind("quote", new quote());
 
         environment.bind("nil", MarcoNil.NIL);
         environment.bind("true", MarcoBoolean.TRUE);
         environment.bind("false", MarcoBoolean.FALSE);

        environment.bind("def", new def());
         environment.bind("var", new var());
         environment.bind("set!", new setbang());

        environment.bind("eval", new MarcoFunction(environment, Arrays.asList("arg"), new eval()));
        environment.bind("cons", new MarcoFunction(environment, Arrays.asList("first", "second"), new cons()));
        environment.bind("first", new MarcoFunction(environment, Arrays.asList("pair"), new first()));
        environment.bind("second", new MarcoFunction(environment, Arrays.asList("pair"), new second()));
        environment.bind("nil?", new MarcoFunction(environment, Arrays.asList("arg"), new nilquestion()));
        environment.bind("pair?", new MarcoFunction(environment, Arrays.asList("pair"), new pairquestion()));

        environment.bind("+", new MarcoFunction(environment, Arrays.asList("v1", "v2"), new plus()));
     }
 
     public MarcoObject eval(String code) {
         MarcoProgram program = parser.parse(code);
         return program.eval(environment);
     }
 
     public MarcoObject lookUp(String var) {
         return environment.lookUp(var);
     }
 }
