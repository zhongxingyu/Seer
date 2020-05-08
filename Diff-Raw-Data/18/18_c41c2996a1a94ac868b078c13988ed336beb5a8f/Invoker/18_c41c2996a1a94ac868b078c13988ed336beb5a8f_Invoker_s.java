 package ui;
 
 import javax.swing.JOptionPane;
 
 import ai.*;
 
 public class Invoker {
 	public AI blackPlayer, whitePlayer;
 	public boolean blackIsHuman = false, whiteIsHuman = false;
 
 	public void invoke(Main frame) {
 		if (frame.board.turn)
 			if (blackIsHuman) {
 				if (frame.history.move > 0)
 					frame.infoWindow.undoButton.setEnabled(true);
				frame.controllable = true;
				frame.infoWindow.updateLabel(frame);
 				Coordinate[] places = Determine.canPlace(frame.board.black,
 						frame.board.white);
 				for (int i = 0; i < places.length; i++) {
 					frame.panel[places[i].x][places[i].y].canPlace = true;
 					frame.panel[places[i].x][places[i].y].repaint();
 				}
 			} else {
 				Coordinate move = blackPlayer.move(frame.board.black,
 						frame.board.white);
 				Coordinate[] pieces = Determine.judge(move, frame.board.black,
 						frame.board.white);
 				if (pieces.length == 0) {
 					JOptionPane.showMessageDialog(frame, "黑方AI作弊", "错误",
 							JOptionPane.ERROR_MESSAGE);
 					System.exit(-1);
 				} else
 					frame.board.add(frame, move, frame.board.black,
 							frame.board.white, pieces);
 			}
 		else if (whiteIsHuman) {
 			if (frame.history.move > 0)
 				frame.infoWindow.undoButton.setEnabled(true);
			frame.controllable = true;
			frame.infoWindow.updateLabel(frame);
			Coordinate[] places=Determine.canPlace(frame.board.white, frame.board.black);
			for (int i=0;i<places.length;i++){
				frame.panel[places[i].x][places[i].y].canPlace=true;
 				frame.panel[places[i].x][places[i].y].repaint();
 			}
 		} else {
 			Coordinate move = whitePlayer.move(frame.board.white,
 					frame.board.black);
 			Coordinate[] pieces = Determine.judge(move, frame.board.white,
 					frame.board.black);
 			if (pieces.length == 0) {
 				JOptionPane.showMessageDialog(frame, "白方AI作弊", "错误",
 						JOptionPane.ERROR_MESSAGE);
 				System.exit(-1);
 			} else
 				frame.board.add(frame, move, frame.board.white,
 						frame.board.black, pieces);
 		}
 	}
 }
