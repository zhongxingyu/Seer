 package reversi;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 
 /**
  * 
  * @author kAG0
  *
  */
 public class Tile extends JButton implements ActionListener, TileForm{
 	public enum State {
 		BLACK, WHITE, BLANK;
 		public static State getOppositeState(State state){
 			if(state == State.BLANK)
 				return State.BLANK;
 			if(state == State.BLACK)
 				return State.WHITE;
 			else {
 				return State.BLACK;
 			}
 		}
 	}
 	
 	public static final String BLACK_IMG = "../resources/black_tile.png";
 	public static final String WHITE_IMG = "../resources/white_tile.png";
 	public static final String BLACK_HI_IMG = "../resources/black_tile_highlight.png";
 	public static final String WHITE_HI_IMG = "../resources/white_tile_highlight.png";
 	public static final String BLANK_IMG = "../resources/blank_tile.png";
 
 	private State state = State.BLANK;
 	private Board board;
 	private int x;
 	private int y;
 	
 	public Tile(Board board, int x, int y){
 		this(State.BLANK, board, x, y);
 	}
 	
 	public Tile(State state, Board board, int x, int y) {
 		this.board = board;
 		this.x = x;
 		this.y = y;
 		setState(state);
 		setImage(state);
 		board.addTile(state);
         addActionListener(this); 
         
         setHorizontalTextPosition(JButton.CENTER);
         setVerticalTextPosition(JButton.CENTER);
         
 		String column = getCharForNumber(y+1);
        setText(column+(x+1));
 	}
 	
 	private String getCharForNumber(int i) {
 	    return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
 	}
 
 	public int getXCoord() {
 		return x;
 	}
 	
 	public int getYCoord() {
 		return y;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
     	//Only accepts clicks on blank tiles
     	if (state == State.BLANK) {
     		//Check if game is ready to continue
     		board.tileClick(x,y);
     	}
     }
 
 	public void flip(){
 		if (state == State.BLACK) {
 			setState(State.WHITE);
 		}
 		else if (state == State.WHITE) {
 			setState(State.BLACK);
 		}
 		highlight();
 	}
 
 	public boolean isBlank(){
 		return state.equals(State.BLANK);
 	}
 	
 	public State getReverseState(){
 		if(state == State.BLACK)
 			return State.WHITE;
 		else {
 			return State.BLACK;
 		}
 	}
 	
 	public static State getOppositeState(State state){
 		if(state == State.BLANK)
 			return State.BLANK;
 		if(state == State.BLACK)
 			return State.WHITE;
 		else {
 			return State.BLACK;
 		}
 	}
 
 	public void setState(State state){
 		if (this.state != state) {
 			board.removeTile(getState());
 			board.addTile(state);
 			setImage(state);
 			this.state = state;
 		}
 	}
 	
 	private void setImage(State state) {
 		//Change image
 		Image img;
 		switch (state) {
 		case BLACK:
 			img = Toolkit.getDefaultToolkit().getImage(getClass().getResource(BLACK_IMG));
 			break;
 		case WHITE:
 			img = Toolkit.getDefaultToolkit().getImage(getClass().getResource(WHITE_IMG));
 			break;
 		default:
 			img = Toolkit.getDefaultToolkit().getImage(getClass().getResource(BLANK_IMG));
 			break;
 		}
 		Image newimg = img.getScaledInstance( 50, 50,  java.awt.Image.SCALE_SMOOTH ) ;  
 		ImageIcon icon = new ImageIcon(newimg);
 		setIcon(icon);
 	}
 
 	public void highlight(){
 		Image img;
 		switch (state) {
 		case BLACK:
 			img = Toolkit.getDefaultToolkit().getImage(getClass().getResource(BLACK_HI_IMG));
 			break;
 		case WHITE:
 			img = Toolkit.getDefaultToolkit().getImage(getClass().getResource(WHITE_HI_IMG));
 			break;
 		default:
 			img = Toolkit.getDefaultToolkit().getImage(getClass().getResource(BLANK_IMG));
 			break;
 		}
 		Image newimg = img.getScaledInstance( 50, 50,  java.awt.Image.SCALE_SMOOTH ) ;  
 		ImageIcon icon = new ImageIcon(newimg);
 		setIcon(icon);
 	}
 	
 	public void lowlight(){
 		setImage(state);
 	}
 
 	public State getState(){
 		return state;
 	}
 	
 	public String toString(){
 		return state.toString();
 	}
 }
