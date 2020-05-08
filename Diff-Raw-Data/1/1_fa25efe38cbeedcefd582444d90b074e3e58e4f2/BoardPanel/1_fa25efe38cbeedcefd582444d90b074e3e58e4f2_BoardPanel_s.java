 package com.nanu.chess.gui;
 
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JPanel;
 
 import com.nanu.chess.board.Board;
 import com.nanu.chess.board.Square;
 import com.nanu.chess.gui.GUIConstants;
 import com.nanu.chess.support.Team;
 
 @SuppressWarnings("serial")
 public class BoardPanel extends JPanel {
 
 	Board _board;
 	Team _team;
 	
 	public BoardPanel (Board board) {
 		_board = board;
 		_team = Team.WHITE;
 	}
 	
 	public void setTeam(Team team) {
 		_team = team;
 		repaint();
 	}
 	
 	public void paintComponent(Graphics g) {
 		for ( int i = 0; i < 8; i++ ) {
 			for ( int j = 0; j < 8; j++ ) {
 				if ( ((i+j)%2 == 0 && _team.equals(Team.WHITE)) || ((i+j)%2 == 1 && _team.equals(Team.BLACK)) )
 					g.setColor(GUIConstants.square.BLACK);
 				if ( ((i+j)%2 == 0 && _team.equals(Team.BLACK)) || ((i+j)%2 == 1 && _team.equals(Team.WHITE)) )
 					g.setColor(GUIConstants.square.WHITE);
 				if ( _board.getSquare(i, j) == curSquare )
 					g.setColor(GUIConstants.square.SELECTED);
 				g.fillRect(GUIConstants.PADDING+i*GUIConstants.SQUARE_WIDTH,
 						GUIConstants.PADDING+j*GUIConstants.SQUARE_HEIGHT,
 						GUIConstants.SQUARE_WIDTH,
 						GUIConstants.SQUARE_HEIGHT);
 				if ( _board.getSquare(i,j).getPiece() != null ) {
 					GUIConstants.piece.getIcon(_board.getSquare(i,j).getPiece()).paintIcon( this, g,
 							GUIConstants.PADDING+i*GUIConstants.SQUARE_WIDTH+
 								(GUIConstants.SQUARE_WIDTH-GUIConstants.piece.ICON_WIDTH)/2,
 							GUIConstants.PADDING+j*GUIConstants.SQUARE_HEIGHT+
 								(GUIConstants.SQUARE_HEIGHT-GUIConstants.piece.ICON_HEIGHT)/2);
 				}
 			}
 		}
 	}
 	
 	public String getMove() {
 		boolean validMove = false;
 		ClickListener click = new ClickListener();
 		this.addMouseListener(click);
 		Square start = null, end = null;
 		while( !validMove ) {
 			synchronized(lock) {
 				try {
 					lock.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			if ( curSquare.getPiece() != null && curSquare.getPiece().getTeam().equals(_team) ) {
 				start = curSquare;
 				repaint();
 			}
 			else if ( start != null && start.getPiece().getLegalMoves(_board, start).contains(curSquare) ) {
 				end = curSquare;
 				curSquare = null;
 				validMove = true;
 			} else {
 				curSquare = null;
 				repaint();
 			}
 		}
 		this.removeMouseListener(click);
 		end.setPiece(start.getPiece());
 		start.setPiece(null);
 		repaint();
 		return (-start.getX()+7)+""+(-start.getY()+7)+","+(-end.getX()+7)+""+(-end.getY()+7);
 	}
 	
 	public class ClickListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			int x = e.getX();
 			int y = e.getY();
 			if ( x > GUIConstants.PADDING && x < GUIConstants.DISPLAY_WIDTH - GUIConstants.PADDING &&
 					y > GUIConstants.PADDING && y < GUIConstants.DISPLAY_HEIGHT - GUIConstants.PADDING ) {
 				x = (x - GUIConstants.PADDING)/GUIConstants.SQUARE_WIDTH;
 				y = (y - GUIConstants.PADDING)/GUIConstants.SQUARE_HEIGHT;
 				curSquare = _board.getSquare(x,y);
 				synchronized(lock) { lock.notify(); }
 			}
 		}
 	}
 	
 	private Object lock = new Object();
 	private Square curSquare;
 	@SuppressWarnings("unused")
 	private Square hoverSquare;
 }
