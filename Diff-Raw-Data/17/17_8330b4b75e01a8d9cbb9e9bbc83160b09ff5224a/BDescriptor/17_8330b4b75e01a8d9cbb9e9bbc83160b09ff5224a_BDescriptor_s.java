 
 public class BDescriptor extends Descriptor {
 
 	public BDescriptor(String desc) {
 		super(desc);
 		// TODO Auto-generated constructor stub
 	}
 	
	public boolean bs(String s) {
		for(int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if(ch == 'b' || ch == 'B') {
				return true;
			}
		}
		return false;
 	}
 }
