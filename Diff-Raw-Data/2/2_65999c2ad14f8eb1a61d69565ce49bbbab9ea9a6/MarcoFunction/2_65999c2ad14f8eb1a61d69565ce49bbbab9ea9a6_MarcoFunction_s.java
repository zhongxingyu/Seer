 package marco.lang;
 
 import marco.internal.Environment;
 
 import java.util.List;
 
 public class MarcoFunction extends MarcoRunnable {
     private final Environment closureEnv;
     private final List<MarcoSymbol> parameters;
     private final MarcoForm body;
     private final int arity;
 
     public MarcoFunction(Environment environment, List<MarcoSymbol> parameters, MarcoForm body) {
        this.closureEnv = environment.duplicate();
         this.parameters = parameters;
         this.body = body;
         this.arity = parameters.size();
     }
 
     @Override
     public MarcoObject call(Environment environment, List<MarcoForm> arguments) {
         assertArity(arity, arguments.size());
 
         Environment extendedEnv = closureEnv.duplicate();
         for (int i = 0; i < arguments.size(); i++) {
             MarcoObject evaluatedArg = arguments.get(i).eval(environment);
             MarcoSymbol parameterName = parameters.get(i);
             extendedEnv.rebind(parameterName.getValue(), evaluatedArg);
         }
         return body.eval(extendedEnv);
     }
 
     @Override
     public String typeName() {
         return "Function";
     }
 }
