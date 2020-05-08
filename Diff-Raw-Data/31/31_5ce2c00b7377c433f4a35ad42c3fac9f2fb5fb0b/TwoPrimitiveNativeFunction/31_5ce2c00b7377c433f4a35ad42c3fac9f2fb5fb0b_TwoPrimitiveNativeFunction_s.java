 package languish.lib;
 
 import languish.base.NativeFunction;
 import languish.base.Primitive;
 
 import com.hjfreyer.util.Tree;
 
 public abstract class TwoPrimitiveNativeFunction implements NativeFunction {
 
 	public final Tree<Primitive> apply(Tree<Primitive> arg) {
 		Tree<Primitive> first = arg.asList().get(0);
 		Tree<Primitive> second = arg.asList().get(1);
 
 		Primitive firstPrim = first.isEmpty() ? new Primitive(0) : first.asLeaf();
 		Primitive secondPrim =
 				second.isEmpty() ? new Primitive(0) : second.asLeaf();
 
 		return apply(firstPrim, secondPrim);
 	}
 
 	protected abstract Tree<Primitive> apply(Primitive a, Primitive b);
 }
