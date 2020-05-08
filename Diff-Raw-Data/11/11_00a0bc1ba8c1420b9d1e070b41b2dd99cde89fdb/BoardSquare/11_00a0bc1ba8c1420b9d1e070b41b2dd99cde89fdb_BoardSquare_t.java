 package board;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 import piece.BlankPiece;
 import piece.GenericGamePiece;
 
 @SuppressWarnings("serial")
 public class BoardSquare extends JPanel {
 
 	public static final int CELL_WIDTH = 60;
 	public static final int NONE = -5;
 	public static final int HIGHLIGHTED = -6;
 	public static final int SELECTED = -7;
 	public static final String STATUS_CHANGE = "statuschange";
 
	//private Color color;
 	private int status;
 	private GenericGamePiece piece;
 	private int iD;
 
 	public BoardSquare() {
 		this(Color.BLACK, GenericBoard.curID++);
 	}
 
 	public BoardSquare(Color color, int uniqueID) {
 		super(new BorderLayout());
		//this.color = color;
 		iD = uniqueID;
 		setPreferredSize(new Dimension(CELL_WIDTH, CELL_WIDTH));
 		setBackground(color);
 		status = NONE;
 		piece = BlankPiece.BLANK();
 		addMouseListener(mouseEnterExit);
 	}
 
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if (status == HIGHLIGHTED) {
 			setBorder(BorderFactory.createLineBorder(Color.WHITE));
 		} else if (status == SELECTED) {
 			setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
 		} else {
 			setBorder(BorderFactory.createEmptyBorder());
 		}
 	}
 
 	/**
 	 * Replaces the old piece on this square with the new one
 	 * 
 	 * @param p
 	 */
 	public void setPiece(GenericGamePiece p) {
 		piece = p;
 		removeAll();
 		add(p);
 		repaint();
 	}
 
 	public GenericGamePiece getPiece() {
 		return piece;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 		repaint();
 	}
 
 	public int getID() {
 		return iD;
 	}
 
 	private MouseListener mouseEnterExit = new MouseListener() {
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			int oldStatus;
 			if (status == SELECTED) {
 				oldStatus = status;
 				status = HIGHLIGHTED;
 				firePropertyChange(BoardSquare.STATUS_CHANGE, oldStatus, HIGHLIGHTED);
 			} else {
 				oldStatus = status;
 				status = SELECTED;
 				firePropertyChange(BoardSquare.STATUS_CHANGE, oldStatus, SELECTED);
 			}
 			repaint();
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			int oldStatus;
 			if (status != SELECTED) {
 				oldStatus = status;
 				status = HIGHLIGHTED;
 				firePropertyChange(BoardSquare.STATUS_CHANGE, oldStatus, HIGHLIGHTED);
 			}
 			repaint();
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			int oldStatus;
 			if (status != SELECTED) {
 				oldStatus = status;
 				status = NONE;
 				firePropertyChange(BoardSquare.STATUS_CHANGE, oldStatus, NONE);
 			}
 			repaint();
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 		}
 	};
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		BoardSquare other = (BoardSquare) obj;
 		return other.getID() == this.getID();
 	}
 }
