 package vocab;
 
public abstract class RightWrong implements Comparable{
 	public int right;
 	public int wrong;
 	
 	public RightWrong(){
 		right = 0;
 		wrong = 0;
 	}
 	
	public int compareTo(Object arg0) {
		RightWrong otherword = (RightWrong)arg0;
 		return (right - wrong) - (otherword.right - otherword.wrong);
 	}
 }
