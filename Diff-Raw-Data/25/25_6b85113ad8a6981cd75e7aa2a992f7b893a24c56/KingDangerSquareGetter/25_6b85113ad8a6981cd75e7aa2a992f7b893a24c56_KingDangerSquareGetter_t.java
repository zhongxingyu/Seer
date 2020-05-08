 package util.genetic.mutatorV2.getters;
 
 import java.util.List;
 
 import util.genetic.mutatorV2.Getter;
 import eval.expEvalV3.EvalParameters;
 
 public final class KingDangerSquareGetter {
 	private final static int[][] zones;
 	
 	static{
 		zones = new int[10][];
 		zones[0] = new int[]{0,8}; //r=1, corners
 		zones[1] = new int[]{2,5}; //r=1, between corner, center
 		zones[2] = new int[]{3,4}; //r=1, center
 		zones[3] = new int[]{9,10,15,16}; //r=2, edges
 		zones[4] = new int[]{11,14}; //r=2, btween edge, center
 		zones[5] = new int[]{12,13}; //r=2, center
 		zones[6] = new int[]{16,23}; //r=3, edge
 		zones[7] = new int[]{17,22}; //r=3, between edge, center
 		zones[8] = new int[]{18,19,20,21}; //r=3, center
 		
 		zones[9] = new int[64-24]; //remaining
 		for(int a = 0; a < 64-24; a++){
 			zones[9][a] = a+24;
 		}
 	}
 	
 	public static void add(List<Getter> l){
 		final String name = "king danger square";
 		
 		for(int a = 0; a < zones.length; a++){
 			final int index = a;
 			l.add(new Getter() {
 				@Override
 				public int get(EvalParameters p) {
 					return p.kingDangerSquares[0][zones[index][0]];
 				}
 				@Override
 				public void set(EvalParameters p, int i) {
					if(i < 0) i = 0;
 					for(int q = 0; q < zones[index].length; q++){
 						p.kingDangerSquares[0][zones[index][q]] = i;
 					}
 					arrayReverse(p.kingDangerSquares[0], p.kingDangerSquares[1]);
 				}
 				@Override
 				public String toString(){
 					return name+", zone="+index;
 				}
 			});
 		}
 	}
 	
 	private static void arrayReverse(final int[] src, final int[] target){
 		final int len = src.length;
 		for(int a = 0; a < len; a++){
 			target[len-a-1] = src[a];
 		}
 	}
 }
