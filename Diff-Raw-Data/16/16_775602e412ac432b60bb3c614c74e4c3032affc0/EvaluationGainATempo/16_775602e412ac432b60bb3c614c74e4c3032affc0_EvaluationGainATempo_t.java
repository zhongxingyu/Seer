 package ai;
 
 public class EvaluationGainATempo {
 	public static int pass(boolean[][] a,boolean[][] b){
		if (Determine.canPlace(b,a).length==0)
 			return 1;
 		else return 0;
 	}
 }
