 package ai;
 
 public class EvaluationMobility {
 	public static int Score(boolean[][] a,boolean[][] b){
		return Determine.canPlace(a, b).length;
 	}
 }
