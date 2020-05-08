 package vocab;
 
public abstract class RightWrong implements Comparable<RightWrong> {
 	public int right;
 	public int wrong;
 	
 	public RightWrong(){
 		right = 0;
 		wrong = 0;
 	}
 	
	public int compareTo(RightWrong otherword) {
 		return (right - wrong) - (otherword.right - otherword.wrong);
 	}
 }
