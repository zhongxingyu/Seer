 package gridlock;
 
 import utils.BoardUtils;
 import api.GPSRule;
 import api.GPSState;
 import exception.NotAppliableException;
 
 public class UpRule implements GPSRule {
 
 	Token token;
 
 	public UpRule(Token token) {
 		this.token = token;
 	}
 
 	
 	@Override
 	public GPSState evalRule(GPSState state) throws NotAppliableException {
 	//	System.out.println("UpRule - Token: " + (char)token.getValue() + " I:" + token.getI() + " J:" + token.getJ());	
 		GPSState ans = null;
 		Board board = ((GridLockState) state).getBoard();
 		Token[][] rawBoard = board.getRawBoard();
 		Board temp;
 		temp = checkUP(rawBoard);
 		if (temp != null) {
 			ans = new GridLockState(temp);
 		}
 
 		return ans;
 	}
 	
 	
 	public Board checkUP(Token[][] board) {
 		Token[][] ans = new Token[6][6];
 		BoardUtils.copyBoard(ans, board);
 		for(int x=0; x <6;x++) {
 			for(int y=0; y<6;y++) {
 				ans[x][y].setI(x);
 				ans[x][y].setJ(y);
 			}
 		}
 		int size, tokenValue, distance, k;
 		int i = token.getI();
 		int j = token.getJ();
 		//int[][] board = BoardUtils.getIntBoard(tokenBoard);
 
 		if (i == 0) {
 			return null;
 		}
 	//	int[][] ans = new int[board.length][board.length];
 		
 		Token upToken = board[i - 1][j];
 		if (BoardUtils.isHorizontal(upToken.getValue())) {
 			return null;
 		} else if (BoardUtils.isVertical(upToken.getValue())) {
			
 			size = BoardUtils.getVTokenSize(board, upToken.getValue(), i - 1, j);
	//		System.out.println("Entro al corto - " + i + " " + j + " size: " + size + " Token: " + (char)upToken.getValue());	
 			Token aux = ans[i-size][j];
 			ans[i-size][j] = token;
 			ans[i][j] = aux;
 		} else {
 	//		System.out.println("Entro al largo - " + i + " " + j);
 			int h;
 			for (h = i - 1; h >= 0; h--) {
 				tokenValue = board[h][j].getValue();
 				if (BoardUtils.isHorizontal(tokenValue)) {
 					return null;
 				}
 				if (BoardUtils.isVertical(tokenValue)) {
 					distance = i-h;
 					size = BoardUtils.getVTokenSize(board, tokenValue, h, j);
 		//			System.out.println("Size " + size + " - " + "distance: " + distance);
 					for (k = i; size > 0; k--, size--) {
 			//			System.out.println("k: " + k + " k+size:" + (k+distance) + " j:" + j);
 						Token aux = ans[k-distance][j];
 						ans[k-distance][j] = ans[k][j];
 						ans[k][j] = aux;
 					}
 					break;
 				}
 			}
 			if (h == -1) {
 				return null;
 			}
 		}
 
 		
 		return new Board(ans);
 	}
 	@Override
 	public Integer getCost() {
 		return 1;
 	}
 
 	@Override
 	public String getName() {
 		return "Se mueve arriba la pieza";
 	}
 
 }
