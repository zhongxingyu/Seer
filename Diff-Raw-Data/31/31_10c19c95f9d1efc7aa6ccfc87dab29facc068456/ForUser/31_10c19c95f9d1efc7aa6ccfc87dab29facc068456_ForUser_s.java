 public class ForUser {
 
	static ForIt forItr;

 	public static int sum1(For r) {
 		forItr=new ForIt(r);
		int sum=forItr.currentVal;
 		if(!forItr.hasNext())    //empty return 0
 			return 0;
		while(forItr.hasNext())  //if for loop has element keep adding sum
			sum+=forItr.next();
 		return sum;
 		//return 0;
 	}
 }
