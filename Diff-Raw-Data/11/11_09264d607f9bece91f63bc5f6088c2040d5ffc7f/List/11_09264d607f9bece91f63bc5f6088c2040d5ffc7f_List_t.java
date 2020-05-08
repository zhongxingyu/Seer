 package lea.syntax;
 
import java.util.LinkedList;

 import lea.types.ListType;
 import lea.types.Type;
 
 public class List extends Expression {
 
 	Pair pair;
 
 	public List(Pair a1) {
 		super(a1, null);
 		pair = a1;
 	}
 
 	public Type getType() {
 		return new ListType(pair.getFirstElementType());
 	}
 
 	public boolean isValid() {
		LinkedList<Expression> list = pair.toList();
		Type type = getType().getLeft();

		for (int i = 0; i < list.size(); i++)
			if (!(list.get(i).getType().equals(type)))
				return false;

 		return true;
 	}
 
 	public String toString() {
 		return "List" + super.toString();
 	}
 
 	public String toDotString() {
 		return "List";
 	}
 
 	public String toJava() {
 		return "new " + getType().toJava() + " {" + pair.toJava() + "}";
 	}
 }
