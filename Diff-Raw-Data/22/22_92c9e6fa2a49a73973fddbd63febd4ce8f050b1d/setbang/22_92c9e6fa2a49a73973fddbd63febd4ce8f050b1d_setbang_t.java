 package marco.lang.macros;
 
 import marco.internal.Cast;
 import marco.internal.Environment;
 import marco.internal.bindings.Binding;
 import marco.lang.*;
 import marco.lang.contracts.Contract;
 
 public class setbang extends MarcoMacro {
     public setbang() {
         super(new Contract(2));
     }
 
     @Override
     public MarcoObject performInvoke(Environment environment, MarcoList arguments) {
         MarcoSymbol var = Cast.toSymbol(arguments.get(0));
         MarcoObject value = arguments.get(1).eval(environment);
 
        Binding binding = environment.get(var.getValue());
         binding.mutate(value);
 
         return MarcoNil.NIL;
     }
 }
