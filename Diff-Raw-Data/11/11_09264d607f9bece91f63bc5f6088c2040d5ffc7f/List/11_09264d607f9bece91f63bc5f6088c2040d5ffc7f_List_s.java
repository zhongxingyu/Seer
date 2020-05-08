 package lea.syntax;
 
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
		// TODO: check if all elements have same type!
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
