 package boardnodes;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JFrame;
 
 import whiteboard.BoardPathType;
 
 public class BoardPath extends BoardElt implements MouseListener, MouseMotionListener{
 	public final static int DRAG_SQUARE_SIZE = 5; //the size of the red/green squares
 	public final static Color START_COLOR = new Color(1,0,0,(float).7);
 	public final static Color END_COLOR = new Color(0,1,0,(float).7);
 	BoardElt _start; //where the base of the arrow points
 	BoardElt _end; //where the tip of the arrow points
 	double _s0, _s1;  //the location on the map of start if _start is null
 	double _e0, _e1; //end location of map is _end is null
 	
	int _holding; //which end you're currently dragging. 0 for start, 1 for end
	
 	Point _seminal; //used for painting, see paint(Graphics)
 	Point _terminal;
 	
 	BoardPathType TYPE;
 	
 	boolean _mouseIn; //true iff the mouse is in the region of this Path
 	
 	/**/
 	public BoardPath(int ID) {
 		super(ID);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		_mouseIn = false;
 		this.TYPE = BoardPathType.SOLID_THIN;
 	}
 	
 	//set the start+end point/nodes
 	public void setStart(BoardElt s) {_start = s;}
 	public void setStart(double s0, double s1) {_s0 = s0; _s1 = s1;}
 	public void setEnd(BoardElt e) {_end = e;}
 	public void setEnd(double e0, double e1) {_e0 = e0; _e1 = e1;}
 		
 	public void paintComponent(Graphics graphics) {
 		super.paintComponent(graphics);
 		Graphics2D g = (Graphics2D) graphics;
 		g.drawLine(_seminal.x, _seminal.y, _terminal.x, _terminal.y);
 		if (_mouseIn) {
 			g.setColor(START_COLOR);
 			g.fillRect(_seminal.x-DRAG_SQUARE_SIZE/2, _seminal.y-DRAG_SQUARE_SIZE/2, DRAG_SQUARE_SIZE, DRAG_SQUARE_SIZE);
 			g.setColor(END_COLOR);
 			g.fillRect(_terminal.x-DRAG_SQUARE_SIZE/2, _terminal.y-DRAG_SQUARE_SIZE/2, DRAG_SQUARE_SIZE,DRAG_SQUARE_SIZE);
 		}
 	}
 	
 	public Dimension getPreferredSize() {
 		return new Dimension(_terminal.x - _seminal.x, _terminal.y - _seminal.y);
 	}
 	
 	@Override
 	void decode(String obj) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	String encode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		System.out.println("Here");
 		_mouseIn = true;
 		repaint();
 	}
 	@Override
 	public void mouseExited(MouseEvent e) {
 		System.out.println("Gone");
 		_mouseIn = false;
 		repaint();
 	}
 	@Override
 	public void mousePressed(MouseEvent e) {
		double startDist = (e.getX() - _seminal.x)*(e.getX() - _seminal.x) + (e.getY() - _seminal.y)*(e.getY() - _seminal.y);
		double endDist = (e.getX() - _terminal.x)*(e.getX() - _terminal.x) + (e.getY() - _terminal.y)*(e.getY() - _terminal.y);
		if (startDist < endDist) { //change the start dot location
			_holding = 0;
		} else {
			_holding = 1;
		}
 	}
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 
 	//change the location of the start/end point (whichever is closest)
 	@Override
 	public void mouseDragged(MouseEvent e) {
		if (_holding == 0) { //change the start dot location
 			_seminal.x = e.getX();
 			_seminal.y = e.getY();
 		} else {
 			_terminal.x = e.getX();
 			_terminal.y = e.getY();
 		}
 		repaint();
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
 	}	
 	
 	public static void main(String[] args) {
 		BoardPath b = new BoardPath(234);
 		b._seminal = new Point(0,0);
 		b._terminal = new Point(50,50);
 		JFrame display = new JFrame("Boardpath test");
 		display.add(b);
 		display.pack();
 		display.setVisible(true);
 		display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 }
