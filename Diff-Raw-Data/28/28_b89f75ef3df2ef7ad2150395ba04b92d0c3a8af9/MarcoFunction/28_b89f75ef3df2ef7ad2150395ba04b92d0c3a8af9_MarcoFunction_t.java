 package marco.lang;
 
 import marco.internal.Environment;
 
 import java.util.List;
 
 public class MarcoFunction implements MarcoRunnable {
     private final Environment closureEnv;
     private final MarcoList parameters;
     private final MarcoForm body;
 
     public MarcoFunction(Environment environment, MarcoList parameters, MarcoForm body) {
         this.closureEnv = environment.duplicate();
         this.parameters = parameters;
         this.body = body;
     }
 
     @Override
     public MarcoObject call(Environment environment, List<MarcoForm> arguments) {
         Environment extendedEnv = closureEnv.duplicate();
         for (int i = 0; i < arguments.size(); i++) {
             MarcoObject evaluatedArg = arguments.get(i).eval(environment);
             MarcoSymbol parameterName = (MarcoSymbol) parameters.get(i);
            extendedEnv.rebind(parameterName.getValue(), evaluatedArg);
         }
         return body.eval(extendedEnv);
     }
 }
