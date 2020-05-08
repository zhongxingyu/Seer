 package languish.base;
 
 import static languish.base.Operation.*;
 
 import java.util.List;
 import java.util.Map;
 
 import languish.base.error.AlreadyReducedError;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.hjfreyer.util.Tree;
 
 public class Reducer {
 
 	private final Map<String, NativeFunction> nativeFunctions;
 
 	public Reducer(Map<String, NativeFunction> nativeFunctions) {
 		this.nativeFunctions = nativeFunctions;
 	}
 
 	public Term reduce(Term term) {
 		Operation op = term.getOperation();
 
 		switch (op) {
 		case APP: {
 			Term func = (Term) term.getFirst();
 			Term arg = (Term) term.getSecond();
 
 			if (func.getOperation() != ABS) {
 				return Terms.app(reduce(func), arg);
 			}
 
 			assert !hasReferencesGreaterThan(arg, 0);
 
 			Term absExp = (Term) func.getFirst();
 
 			return replaceAllReferencesToParam(absExp, 1, arg);
 		}
 
 			// TODO(hjfreyer): Change to only accept primitives - encode fancier
 			// structures as strings.
 		case NATIVE_APPLY: {
 			String funcName = (String) term.getFirst();
 			Term arg = (Term) term.getSecond();
 
 			if (arg.getOperation() != ABS && arg.getOperation() != PRIMITIVE) {
 				return Terms.nativeApply(funcName, reduce(arg));
 			}
 
 			if (!nativeFunctions.containsKey(funcName)) {
 				// TODO(hjfreyer): Make an exception for this
 				throw new RuntimeException("Function not supported: " + funcName);
 			}
 
 			NativeFunction nativeFunc = nativeFunctions.get(funcName);
 
 			Tree<Primitive> argObject = convertTermToJavaObject(arg);
 			Tree<Primitive> result = nativeFunc.apply(argObject);
 
 			return Terms.fromPrimitiveTree(result);
 		}
 
 		case IS_PRIMITIVE: {
 			Term arg = (Term) term.getFirst();
 
 			if (arg.getOperation() == PRIMITIVE) {
 				return Terms.TRUE;
 			} else if (arg.getOperation() == ABS) {
 				return Terms.FALSE;
 			} else {
 				return Terms.isPrimitive(reduce(arg));
 			}
 		}
 		case ABS:
 		case PRIMITIVE:
 		case REF:
 			throw new AlreadyReducedError(term);
 		}
 
 		throw new AssertionError();
 	}
 
 	public static Term replaceAllReferencesToParam(Term term, int id, Term with) {
 		Operation op = term.getOperation();
 
 		switch (op) {
 		case ABS:
 			Term subterm = (Term) term.getFirst();
 			return Terms.abs(replaceAllReferencesToParam(subterm, id + 1, with));
 		case PRIMITIVE:
 			return term;
 
 		case REF:
 			int ref_id = (Integer) term.getFirst();
 
 			return (ref_id == id) ? with : term;
 
 		case NATIVE_APPLY:
			return Terms.nativeApply(
					(String) term.getFirst(),
					replaceAllReferencesToParam((Term) term.getSecond(), id, with));
 
 		case APP:
 		case IS_PRIMITIVE:
 			return propagateReplaceReference(term, id, with);
 		}
 
 		throw new AssertionError();
 	}
 
 	public static Term propagateReplaceReference(Term term, int id, Term with) {
 		return new Term(term.getOperation(), //
 				replaceAllReferencesToParam((Term) term.getFirst(), id, with),
 				replaceAllReferencesToParam((Term) term.getSecond(), id, with));
 	}
 
 	public static boolean hasReferencesGreaterThan(Term term, int max) {
 		Operation op = term.getOperation();
 
 		switch (op) {
 		case REF:
 			return ((Integer) term.getFirst()) > max;
 
 		case PRIMITIVE:
 			return false;
 
 		case ABS:
 			return hasReferencesGreaterThan((Term) term.getFirst(), max + 1);
 
 		case APP:
 			return hasReferencesGreaterThan((Term) term.getFirst(), max)
 					|| hasReferencesGreaterThan((Term) term.getSecond(), max);
 
 		case NATIVE_APPLY:
 			return hasReferencesGreaterThan((Term) term.getSecond(), max);
 		}
 
 		throw new AssertionError();
 	}
 
 	public Term reduceCompletely(Term term) {
 		while (term.getOperation() != ABS && term.getOperation() != PRIMITIVE) {
 			term = reduce(term);
 		}
 		return term;
 	}
 
 	public Tree<Primitive> convertTermToJavaObject(Term term) {
 		term = reduceCompletely(term);
 
 		switch (term.getOperation()) {
 		case PRIMITIVE: {
 			Primitive prim = (Primitive) term.getFirst();
 
 			if (prim.getJavaObject().equals(0)) {
 				return Tree.<Primitive> empty();
 			}
 
 			return Tree.leaf(prim);
 		}
 		case ABS:
 			Tree<Primitive> car = convertTermToJavaObject(Terms.car(term));
 			Tree<Primitive> cdr = convertTermToJavaObject(Terms.cdr(term));
 
 			List<Tree<Primitive>> result = Lists.newLinkedList();
 
 			result.add(car);
 			result.addAll(cdr.asList());
 
 			return Tree.inode(ImmutableList.copyOf(result));
 		}
 
 		throw new IllegalArgumentException("term is not in a convertible state: "
 				+ term);
 	}
 
 }
