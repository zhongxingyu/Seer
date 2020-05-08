 package ui;
 
 import ai.Determine;
 
 public class Move {
 	private static Main frame;
 	private static boolean[][] yours, enemys;
 
 	private static void finish() {
 		frame.controllable = false;
 		if (Determine.canMoveNum(enemys, yours) != 0)
 			frame.board.turn = !frame.board.turn;
 		else if (Determine.canMoveNum(yours, enemys) == 0) {
 			frame.finished = true;
 			if (frame.board.blackNum > frame.board.whiteNum)
 				frame.winner = 1;
 			if (frame.board.blackNum < frame.board.whiteNum)
 				frame.winner = -1;
 			frame.evaluateWindow.updateLabel(frame);
 			if (frame.showBoard) {
 				frame.infoWindow.updateLabel(frame);
 				frame.repaint();
 			}
			new Thread() {
				@Override
				public void run() {
					frame.finished = false;
					frame.board = frame.initialBoard.clone();
					frame.invoke.invoke(frame);
				}
			}.start();
 		}
 		if (!frame.finished)
 			frame.invoke.invoke(frame);
 	}
 
 	public static void move(Main frame, boolean[][] yours, boolean[][] enemys,
 			Coordinate[] pieces) {
 		Move.frame = frame;
 		Move.yours = yours;
 		Move.enemys = enemys;
 		for (int i = 0; i < pieces.length; i++)
 			if (pieces[i] != null) {
 				yours[pieces[i].x][pieces[i].y] = true;
 				enemys[pieces[i].x][pieces[i].y] = false;
 				if (frame.board.turn) {
 					frame.board.blackNum++;
 					frame.board.whiteNum--;
 				} else {
 					frame.board.whiteNum++;
 					frame.board.blackNum--;
 				}
 			}
 		finish();
 	}
 }
