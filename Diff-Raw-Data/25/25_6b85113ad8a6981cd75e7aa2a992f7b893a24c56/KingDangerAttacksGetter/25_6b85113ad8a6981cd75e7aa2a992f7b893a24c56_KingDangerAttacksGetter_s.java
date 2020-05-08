 package util.genetic.mutatorV2.getters;
 
 import java.util.List;
 
 import state4.State4;
 import util.genetic.mutatorV2.Getter;
 import eval.expEvalV3.EvalParameters;
 
 public final class KingDangerAttacksGetter {
 	public static void add(List<Getter> l){
 		final String[] names = new String[7];
 		names[State4.PIECE_TYPE_BISHOP] = "bishop";
 		names[State4.PIECE_TYPE_PAWN] = "pawn";
 		names[State4.PIECE_TYPE_ROOK] = "rook";
 		names[State4.PIECE_TYPE_QUEEN] = "queen";
 		names[State4.PIECE_TYPE_KNIGHT] = "knight";
 		names[State4.PIECE_TYPE_KING] = "king";
 		
 		for(int a = 1; a < 7; a++){
 			final int index = a;
 			l.add(new Getter() {
 				@Override
 				public int get(EvalParameters p) {
 					return p.dangerKingAttacks[index];
 				}
 				@Override
 				public void set(EvalParameters p, int i) {
 					p.dangerKingAttacks[index] = i;
 				}
 				@Override
 				public String toString(){
 					return names[index]+" attacks (king ring)";
 				}
 			});
 		}
 		
 		l.add(new Getter() {
 			public int get(EvalParameters p) { return p.contactCheckQueen;}
			public void set(EvalParameters p, int i) { p.contactCheckQueen = i;}
 			public String toString(){ return "contact check, queen";}
 		});
 		l.add(new Getter() {
 			public int get(EvalParameters p) { return p.contactCheckRook;}
			public void set(EvalParameters p, int i) { p.contactCheckRook = i;}
 			public String toString(){ return "contact check, rook";}
 		});
 		l.add(new Getter() {
 			public int get(EvalParameters p) { return p.queenCheck;}
			public void set(EvalParameters p, int i) { p.queenCheck = i;}
 			public String toString(){ return "check, queen";}
 		});
 		l.add(new Getter() {
 			public int get(EvalParameters p) { return p.rookCheck;}
			public void set(EvalParameters p, int i) { p.rookCheck = i;}
 			public String toString(){ return "check, rook";}
 		});
 		l.add(new Getter() {
 			public int get(EvalParameters p) { return p.knightCheck;}
			public void set(EvalParameters p, int i) { p.knightCheck = i;}
 			public String toString(){ return "check, knight";}
 		});
 		l.add(new Getter() {
 			public int get(EvalParameters p) { return p.bishopCheck;}
			public void set(EvalParameters p, int i) { p.bishopCheck = i;}
 			public String toString(){ return "check, bishop";}
 		});
 	}
 }
