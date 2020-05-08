 package rmi;
 
 import java.rmi.server.UnicastRemoteObject;
 import javax.swing.JOptionPane;
 
 import ai.Determine;
 import ui.*;
 
 @SuppressWarnings("serial")
 public class Server extends UnicastRemoteObject implements Interface {
 	private Main frame;
 	private String map;
 
 	public Server() throws Exception {
 	}
 
 	@Override
 	public void move(Coordinate c) throws Exception {
 		for (int i = 0; i < 8; i++)
 			for (int j = 0; j < 8; j++)
 				if (frame.panel[i][j].turn || frame.panel[i][j].focus) {
 					frame.panel[i][j].turn = false;
 					frame.panel[i][j].focus = false;
 					frame.panel[i][j].repaint();
 				}
 		boolean[][] yours, enemys;
 		if (frame.board.turn) {
 			yours = frame.board.black;
 			enemys = frame.board.white;
 		} else {
 			yours = frame.board.white;
 			enemys = frame.board.black;
 		}
 		Coordinate[] pieces = Determine.judge(c, yours, enemys);
 		if (pieces.length != 0)
 			frame.board.add(frame, c, yours, enemys, pieces);
 		else
 			throw (new Exception());
 	}
 
 	@Override
 	public void mouseEntered(Coordinate c) {
 		// TODO 自动生成的方法存根
 
 	}
 
 	@Override
 	public void mouseExited(Coordinate c) {
 		// TODO 自动生成的方法存根
 
 	}
 
 	@Override
 	public void connect(boolean black, Interface remote, String map)
 			throws Exception {
 		frame.waitingWindow.dispose();
 		if (frame.networkHost) {
 			String[] options = { "黑方", "白方" };
 			if (JOptionPane.showOptionDialog(null, "请选择", "局域网对战",
 					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 					null, options, options[0]) == 0)
 				frame.networkBlack = true;
			frame.remote = remote;
			remote.connect(!frame.networkBlack, null, this.map);
 		} else {
 			frame.networkBlack = black;
 			frame.board = new Chessboard(map);
 		}
 		frame.network = true;
 		if (frame.networkBlack)
 			frame.controllable = true;
		frame.start();
 	}
 
 	@Override
 	public void set(Main frame, String map) {
 		this.frame = frame;
 		this.map = map;
 	}
 
 	@Override
 	public void close() throws Exception {
 		JOptionPane.showMessageDialog(frame, "对方退出", "游戏停止",
 				JOptionPane.INFORMATION_MESSAGE);
		frame.infoWindow.dispose();
 		frame.dispose();
 		new ResetThread(frame).start();
 	}
 
 	@Override
 	public void restart() throws Exception {
 		// TODO
 	}
 }
