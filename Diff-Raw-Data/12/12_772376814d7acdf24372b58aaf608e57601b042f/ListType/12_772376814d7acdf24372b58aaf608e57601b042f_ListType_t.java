 package type;
 
 public class ListType extends Type {
 	Type elementType;
 
 	public Type getElementType() {
 		return elementType;
 	}
 
 	public ListType(Type elementType) {
 		super();
 		this.elementType = elementType;
 	}

 	private static ListType dummyInstance = new ListType(NullType.getInstance());
 
 	public static ListType getDummyInstance() {
 		return dummyInstance;
 	}

 	@Override
 	public boolean equals(Type other) {
 		if (other == null || (other instanceof ListType) == false) {
 			return false;
 		}
		ListType otherListType = (ListType) other;
		return elementType == NullType.getInstance()
				|| elementType.equals(otherListType.elementType)
				|| otherListType == ListType.getDummyInstance();//other ==Nil
 	}
 
 	@Override
 	public String getName() {
 		return elementType.getName() + " list";
 	}
 }
