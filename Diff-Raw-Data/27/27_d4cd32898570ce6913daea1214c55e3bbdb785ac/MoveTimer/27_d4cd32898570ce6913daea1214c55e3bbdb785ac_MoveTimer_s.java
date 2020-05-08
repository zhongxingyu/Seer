 package ui;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.JPanel;
 
 import ai.Determine;
 
 public class MoveTimer extends TimerTask {
 	private Main frame;
 	private boolean[][] yours;
 	private boolean[][] enemys;
 	private Coordinate[] pieces;
 
 	private class RepaintTimer extends TimerTask {
 		private JPanel panel;
 
 		public RepaintTimer(JPanel panel) {
 			this.panel = panel;
 		}
 
 		@Override
 		public void run() {
 			if (frame.board.turn) {
 				frame.board.blackNum++;
 				frame.board.whiteNum--;
 			} else {
 				frame.board.whiteNum++;
 				frame.board.blackNum--;
 			}
 			panel.repaint();
 			frame.infoWindow.updateLabel(frame);
 		}
 	}
 
 	private class Finish extends TimerTask {
 		@Override
 		public void run() {
 			frame.controllable = false;
 			if (Determine.canMove(enemys, yours))
 				frame.board.turn = !frame.board.turn;
			else if ((frame.board.emptyNum == 0) || (frame.board.blackNum == 0)
					|| (frame.board.whiteNum == 0)
					|| !Determine.canMove(yours, enemys)) {
 				frame.finished = true;
 				if (frame.board.blackNum > frame.board.whiteNum)
 					frame.winner = 1;
 				if (frame.board.blackNum < frame.board.whiteNum)
 					frame.winner = -1;
 			}
 			frame.infoWindow.updateLabel(frame);
 			if (!frame.finished)
 				frame.invoke.invoke(frame);
 		}
 	}
 
 	public MoveTimer(Main frame, boolean[][] yours, boolean[][] enemys,
 			Coordinate[] pieces) {
 		this.frame = frame;
 		this.yours = yours;
 		this.enemys = enemys;
 		this.pieces = pieces;
 	}
 
 	@Override
 	public void run() {
 		int time = 0;
 		Timer timer = new Timer();
 		for (int i = 0; i < pieces.length; i++)
 			if (pieces[i] == null)
 				time += 200;
 			else {
 				yours[pieces[i].x][pieces[i].y] = true;
 				enemys[pieces[i].x][pieces[i].y] = false;
 				timer.schedule(new RepaintTimer(
 						frame.panel[pieces[i].x][pieces[i].y]), time);
 			}
 		if (frame.invoke.blackIsHuman || frame.invoke.whiteIsHuman)
 			time += 100;
 		else
 			time += 500;
 		timer.schedule(new Finish(), time);
 	}
 }
