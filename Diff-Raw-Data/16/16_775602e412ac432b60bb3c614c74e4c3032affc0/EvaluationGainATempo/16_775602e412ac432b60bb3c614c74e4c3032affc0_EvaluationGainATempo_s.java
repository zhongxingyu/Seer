 package ai;
 
 public class EvaluationGainATempo {
 	public static int pass(boolean[][] a,boolean[][] b){
		if (Determine.canMoveNum(b,a)==0)
 			return 1;
 		else return 0;
 	}
 }
