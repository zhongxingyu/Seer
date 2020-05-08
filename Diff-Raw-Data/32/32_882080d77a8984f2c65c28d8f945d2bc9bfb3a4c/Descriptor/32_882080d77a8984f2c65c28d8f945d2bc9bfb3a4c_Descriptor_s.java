 public abstract class Descriptor{
 	
 	protected String s;
 	
 	public Descriptor(String s){
		str = s;
 	}
 	
 	public boolean compareTo(Descriptor d){
 		return false;
 	}
 	
 	public String toString(){
 		return s;
 	}
 }
