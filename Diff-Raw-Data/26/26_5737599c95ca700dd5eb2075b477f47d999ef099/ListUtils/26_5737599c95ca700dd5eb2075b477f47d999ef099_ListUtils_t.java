 package ash.lang;
 
 import java.util.Iterator;
 
 import bruce.common.functional.Func1;
 
 
 public final class ListUtils {
	private static final Symbol QUOTE_SYMBOL = Symbol.create("quote");
	
 	public static PersistentList pair(PersistentList keys, PersistentList vals) {
 		if (Symbol.create(".").equals(keys.head())) // (. x) -> (1 2 3 ...) 
 			return new Node(new Node(keys.rest().head(), new Node(vals)));
 		else if (!keys.isEndingNode() && !vals.isEndingNode())
 			return new Node(new Node(keys.head(), new Node(vals.head())), pair(keys.rest(), vals.rest()));
 		return BasicType.NIL;
 	}
 
 	public static int count(PersistentList seq) {
 		if (seq.isEndingNode()) return 0;
 		return 1 + count(seq.rest());
 	}
 	
 	public static PersistentList filter(PersistentList seq, Func1<Boolean, Object> predicate) {
 		if (seq.isEndingNode()) return BasicType.NIL;
 		return predicate.call(seq.head())
 				? new Node(seq.head(), filter(seq.rest(), predicate))
 				: filter(seq.rest(), predicate); 
 	}
 	
 	public static Node take(int count, PersistentList seq) {
 		if (seq.isEndingNode()) return BasicType.NIL;
 		return count == 0 ? BasicType.NIL : new Node(seq.head(), take(count - 1, seq.rest()));
 	}
 	
 	public static PersistentList drop(int count, PersistentList seq) {
 		if (seq.isEndingNode()) return BasicType.NIL;
 		return count == 0 ? seq : drop(count - 1, seq.rest());
 	}
 	
 	public static PersistentList toSeq(Iterator<PersistentList> tailNodeSeq) {
 		if (!tailNodeSeq.hasNext()) return BasicType.NIL;
 		return new Node(tailNodeSeq.next().head(), toSeq(tailNodeSeq));
 	}
 	
 	public static PersistentList toSeq(int skipElems, Object... tailNodeSeq) {
 		if (tailNodeSeq.length == skipElems) return BasicType.NIL;
 		return new Node(tailNodeSeq[skipElems], toSeq(skipElems + 1, tailNodeSeq));
 	}
 		
 	public static Node append(PersistentList left, Node right) {
 		if (left.isEndingNode()) return right;
 		return new Node(left.head(), append(left.rest(), right));
 	}
 	
 	public static Object assoc(String varName, PersistentList environment) {
 		final Node headNode = (Node) environment.head();
 		if (headNode == null)
 			return BasicType.NIL;
 		else if (varName.equals(headNode.head()))
 			return headNode.rest().head();
 		else
 			return assoc(varName, environment.rest());
 	}
 	
 	public static int indexOf(PersistentList seq, Object targetVal, int skiped) {
 		if (seq.isEndingNode()) {
 			return -1;
 		} else if (seq.head().equals(targetVal)) {
 			return skiped;
 		} else {
 			return indexOf(seq.rest(), targetVal, skiped + 1);
 		}
 	}
 	
 	public static Object atom(Object evalRes) {
 		return evalRes instanceof PersistentList
 				? transformBoolean(((PersistentList) evalRes).isEndingNode())
 				: BasicType.T;
 	}
 	
 	public static Object eq(Object a, Object b) { return transformBoolean(a.equals(b)); }
 	
 	public static Object car(PersistentList arg) { return arg.head(); }
 	
 	public static PersistentList cdr(PersistentList arg) { return arg.rest(); }
 
 	public static PersistentList cons(Object a, PersistentList b) { return new Node(a, b); }
 	
 	public static final Object transformBoolean(boolean bl) {
 		return bl ? BasicType.T : BasicType.NIL;
 	}
 	
 	public static final boolean transformBoolean(Object val) {
 		return !(val instanceof PersistentList && ((PersistentList) val).isEndingNode());
 	}

	public static Node quoted(final Object val) {
		return new Node(QUOTE_SYMBOL, new Node(val));
	}
 	
 	public static PersistentList quoteAll(PersistentList seq) {
 		if (seq.isEndingNode()) return BasicType.NIL;
		return new Node(quoted(seq.head()), quoteAll(seq.rest()));
 	}
 }
