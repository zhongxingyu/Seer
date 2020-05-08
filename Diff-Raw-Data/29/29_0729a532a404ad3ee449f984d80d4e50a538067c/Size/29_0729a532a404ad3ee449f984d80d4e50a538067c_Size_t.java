 package voteVis;
 
 public enum Size {
 	XS,
 	S,
 	M,
 	L,
 	XL;
 	
 	public static int get_dim_from_size(Size size) {
 		switch (size) {
 		case XL:
 			return Settings.UNIT_DIM * 3 + Settings.BOX_GAP * 2;
 		case L:
 			return Settings.UNIT_DIM * 2 + Settings.BOX_GAP;
 		case M:
			return (int) (Settings.UNIT_DIM * 1.7 + Settings.BOX_GAP); // not 1.75 / 1.25 for rounding size errors
 		case S:
 			return (int) (Settings.UNIT_DIM * 1.2 + Settings.BOX_GAP);
 		case XS:
 		default:
 			return Settings.UNIT_DIM;
 		}
 	}
 	
 	// rank 0 = #1 (most votes)
 	// rank 1 = #2
 	// ...
 	public static Size get_size_from_rank(int rank) {
 		switch (rank) {
 		case 0:
 			return XL;
 		case 1:
 			return L;
 		case 2:
 			return M;
 		case 3:
 			return S;
 		case 4:
 		default:
 			return XS;
 		}
 	}
 }
