 
 public class BDescriptor extends Descriptor {
 
 	public BDescriptor(String desc) {
 		super(desc);
 		// TODO Auto-generated constructor stub
 	}
 	
	public boolean bs() {
		return desc.contains("B") | desc.contains("b");
 	}
 }
