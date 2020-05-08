 package ai;
 
 public class EvaluationMobility {
 	public static int Score(boolean[][] a,boolean[][] b){
		return Determine.canMoveNum(a, b);
 	}
 }
