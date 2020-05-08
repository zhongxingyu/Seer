 public abstract class Component {
 	/*Constructors*/
	public Component(String name) {
		mName = name;
		mSize = -1;
 	}
 	
 	/*Attributes*/
 	private String mName;
 	private int mSize;
 	
	public String name() {
 		return mName;
 	}
	public int size() {
		return mSize;
	}
 	
 	/*Methods*/
 }
