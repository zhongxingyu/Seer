 package languish.base;
 
 import static languish.base.Terms.*;
 import static languish.base.testing.TestUtil.*;
 
 import java.util.Map;
 
 import junit.framework.TestCase;
 import languish.base.testing.LanguishTestCase;
 import languish.base.testing.TestUtil;
 import languish.util.PrimitiveTree;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.hjfreyer.util.Tree;
 
 public class NativeFunctionTest extends TestCase {
 	public static final NativeFunction TRIVIAL = new NativeFunction() {
 		public Tree<Primitive> apply(Tree<Primitive> arg) {
 			return PrimitiveTree.from(42);
 		}
 	};
 
 	public static final NativeFunction IDENTITY = new NativeFunction() {
 		public Tree<Primitive> apply(Tree<Primitive> arg) {
 			return arg;
 		}
 	};
 
 	public static final Map<String, NativeFunction> FUNCTIONS =
 			ImmutableMap.of("TRIVIAL", TRIVIAL, "IDENTITY", IDENTITY);
 
 	public static final Reducer REDUCER = new Reducer(FUNCTIONS);
 
 	public enum Tests implements LanguishTestCase {
 		TRIVIAL_NATIVE_FUNC( //
 				nativeApply("TRIVIAL", NULL),
 				null,
 				primitive(new Primitive(42)),
 				PrimitiveTree.from(42)),
 
 		TRIVIAL_NATIVE_WITH_REDUCE( //
 				nativeApply("TRIVIAL", app(abs(primitive(FOUR)), NULL)),
 				null,
 				nativeApply("TRIVIAL", primitive(FOUR)),
 				PrimitiveTree.from(42)),
 
 		TRIVIAL_NATIVE_WITH_LIST( //
 				Terms.nativeApply("TRIVIAL", cons(primitive(THREE), cons(
 						primitive(FOUR),
 						NULL))),
 				null,
 				null,// Terms.primitive(new Primitive(42)),
 				PrimitiveTree.from(42)),
 
 		IDENT_NATIVE_FUNC( //
 				nativeApply("IDENTITY", NULL),
 				null,
 				NULL,
 				PrimitiveTree.from(ImmutableList.of())),
 
 		IDENT_NATIVE_WITH_REDUCE( //
 				nativeApply("IDENTITY", app(abs(primitive(FOUR)), NULL)),
 				null,
 				nativeApply("IDENTITY", primitive(FOUR)),
 				PrimitiveTree.from(4)),
 
 		IDENT_NATIVE_WITH_LIST( //
 				Terms.nativeApply("IDENTITY", cons(primitive(THREE), cons(
 						primitive(FOUR),
 						NULL))),
 				null,
 				null,// cons(primitive(THREE), cons(primitive(FOUR), NULL)),
 				PrimitiveTree.from(ImmutableList.of(3, 4))),
 
 		;
 
 		private final Term expression;
 		private final String code;
 		private final Term reducedOnce;
 		private final Tree<Primitive> reducedCompletely;
 
 		private Tests(
 				Term expression,
 				String code,
 				Term reducedOnce,
 				Tree<Primitive> reducedCompletely) {
 			this.expression = expression;
 			this.code = code;
 			this.reducedOnce = reducedOnce;
 			this.reducedCompletely = reducedCompletely;
 		}
 
 		public Term getExpression() {
 			return expression;
 		}
 
 		public String getCode() {
 			return code;
 		}
 
 		public Term getReducedOnce() {
 			return reducedOnce;
 		}
 
 		public Tree<Primitive> getReducedCompletely() {
 			return reducedCompletely;
 		}
 	}
 
 	public void test() {
 		for (LanguishTestCase test : Tests.values()) {
 			TestUtil.assertLanguishTestCase(REDUCER, test);
 		}
 	}
 }
