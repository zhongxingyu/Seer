 package languish.api;
 
 import languish.base.NativeFunction;
 import languish.base.Primitive;
 import languish.util.PrimitiveTree;
 
 public abstract class TwoArgumentNativeFunction implements NativeFunction {
   @Override
   public final PrimitiveTree apply(PrimitiveTree arg) {
     Primitive first = arg.asList().get(0).asPrimitive();
    Primitive second = arg.asList().get(0).asPrimitive();
 
     return apply(first, second);
   }
 
   protected abstract PrimitiveTree apply(Primitive arg1, Primitive arg2);
 }
