 package se.liu.ida.geoza435.tddc69.project.GUI;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import se.liu.ida.geoza435.tddc69.project.game.Board;
 import se.liu.ida.geoza435.tddc69.project.game.Connection;
 import se.liu.ida.geoza435.tddc69.project.game.Mark;
 
 @SuppressWarnings("serial")
 public class BoardDisplay extends JPanel {
 	private Board board;
 	// TODO: Better names
 	List<MarkDisplay> md;
 	List<ConnectionDisplay> cd;
 	MarkDisplay selectedMark;
 
 	public BoardDisplay(Board board) {
 		this.md = new ArrayList<MarkDisplay>();
 		this.cd = new ArrayList<ConnectionDisplay>();
 		this.board = board;
 		this.setLayout(null);
 		this.setSize(400, 400);
 		this.setPreferredSize(new Dimension(400, 400));
 	}
 
 	public MarkDisplay addMarkDisplay(Mark m) {
 		MarkDisplay newMarkDisplay = new MarkDisplay(m);
 		this.md.add(newMarkDisplay);
 		this.add(newMarkDisplay);
 		return newMarkDisplay;
 	}
 
 	public ConnectionDisplay addConnection(Connection c) {
 		ConnectionDisplay newCD = new ConnectionDisplay(c);
 		this.cd.add(newCD);
 		this.add(newCD);
 		return newCD;
 	}
 
 	public void addNewConnectionDisplay(Connection c) {
		ConnectionDisplay newCD = new ConnectionDisplay(c);
		this.cd.add(newCD);
 		this.board.addConnection(c);
		this.add(newCD);
 	}
 
 	public List<MarkDisplay> getMarkDisplays() {
 		return this.md;
 	}
 
 	public void deleteMarkDisplay(MarkDisplay md) {
 
 		Iterator<ConnectionDisplay> iterator = cd.iterator();
 
 		while (iterator.hasNext()) {
 			ConnectionDisplay c = iterator.next();
 			if (md.mark.getConnections().contains(c.connection)) {
 				this.remove(c);
 				iterator.remove();
 			}
 		}
 
 		board.deleteMark(md.mark);
 		md.remove(md);
 		this.remove(md);
 	}
 
 	public void selectNone() {
 		selectedMark = null;
 		getBoard().selectNone();
 	}
 
 	public void selectOne(MarkDisplay md) {
 		selectedMark = md;
 		md.mark.setSelected(true);
 	}
 
 	public MarkDisplay getSelectedMark() {
 		return selectedMark;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public List<ConnectionDisplay> getConnectionDisplays() {
 		return this.cd;
 	}
 }
