 
 public class Question4 {
 	public static double maxSpending(double[] amounts){
 		double max=0;
		for(int i=0;i<amounts.length;i++)
			max=Math.max(max, best(amounts,0));
 		return max;
 	}
 	private static double best(double[] amounts,double currentTotal){
 		if(currentTotal>100.0)
 			return 0;
 		double best=currentTotal;
 		for(double d:amounts){
 			double value=best(amounts,currentTotal+d);
			if(value>best&&value<=100.0){
 				best=value;
			}
 		}
 		return best;
 	}
 }
