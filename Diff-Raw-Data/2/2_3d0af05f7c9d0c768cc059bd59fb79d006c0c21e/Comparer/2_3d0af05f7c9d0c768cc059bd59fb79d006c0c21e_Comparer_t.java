 package suite.node.util;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import suite.node.Atom;
 import suite.node.Int;
 import suite.node.Node;
 import suite.node.Reference;
 import suite.node.Str;
 import suite.node.Tree;
 
 public class Comparer implements Comparator<Node> {
 
 	public static final Comparer comparer = new Comparer();
 
 	private static Map<Class<? extends Node>, Integer> order = new HashMap<>();
 	static {
 		order.put(Reference.class, 0);
 		order.put(Int.class, 10);
 		order.put(Atom.class, 20);
 		order.put(Str.class, 30);
 		order.put(Tree.class, 40);
 	}
 
 	@Override
 	public int compare(Node n0, Node n1) {
 		n0 = n0.finalNode();
 		n1 = n1.finalNode();
 		Class<? extends Node> clazz0 = n0.getClass();
 		Class<? extends Node> clazz1 = n1.getClass();
 
 		if (clazz0 == clazz1)
 			if (clazz0 == Atom.class)
 				return ((Atom) n0).getName().compareTo(((Atom) n1).getName());
 			else if (clazz0 == Int.class)
 				return ((Int) n0).getNumber() - ((Int) n1).getNumber();
			else if (clazz0 == Reference.class)
				return ((Reference) n0).getId() - ((Reference) n1).getId();
 			else if (clazz0 == Str.class)
 				return ((Str) n0).getValue().compareTo(((Str) n1).getValue());
 			else if (clazz0 == Tree.class) {
 				Tree t1 = (Tree) n0;
 				Tree t2 = (Tree) n1;
 				int c = t1.getOperator().getPrecedence() - t2.getOperator().getPrecedence();
 				c = c != 0 ? c : compare(t1.getLeft(), t2.getLeft());
 				c = c != 0 ? c : compare(t1.getRight(), t2.getRight());
 				return c;
 			} else
 				return n0.hashCode() - n1.hashCode();
 		else
 			return order.get(clazz0) - order.get(clazz1);
 	}
 
 }
