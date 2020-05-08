 package se.liu.ida.geoza435.tddc69.project.GUI.editor;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JOptionPane;
 
 import se.liu.ida.geoza435.tddc69.project.GUI.BoardDisplay;
 import se.liu.ida.geoza435.tddc69.project.GUI.MarkDisplay;
 import se.liu.ida.geoza435.tddc69.project.game.Connection;
 import se.liu.ida.geoza435.tddc69.project.game.Mark;
 import se.liu.ida.geoza435.tddc69.project.game.MarkType;
 import se.liu.ida.geoza435.tddc69.project.game.Position;
 
 public class AddListener extends MouseAdapter {
 
 	BoardDisplay bd;
 
 	public AddListener(BoardDisplay bd) {
 		this.bd = bd;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		MarkDisplay selectedMark;
 		if ((selectedMark = bd.getSelectedMark()) != null) {
 			Mark newmark = new Mark(MarkType.normal,
 					new Position(e.getX() - MarkDisplay.SIZE / 2,
 							e.getY() - MarkDisplay.SIZE / 2));
 			bd.getBoard().addMark(newmark);
 			Connection newConnection = new Connection(selectedMark.mark,
 					newmark);
 			MarkDisplay newmd = bd.addMarkDisplay(newmark);
			bd.addNewConnectionDisplay(newConnection);
 			bd.selectNone();
 			bd.selectOne(newmd);
 			bd.repaint();
 		} else {
 			JOptionPane.showMessageDialog(null,
 					"You have to select a mark first.");
 		}
 	}
 }
