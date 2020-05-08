 package boardnodes;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.JPanel;
 
 
 import whiteboard.BoardActionType;
 
 public class ScribbleNode extends BoardElt implements MouseListener, MouseMotionListener, Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8104681433533441763L;
 	boolean _resizeLock,_dragLock;
 	Point startPt;
 	public final static int POINT_WIDTH = 3;
 	List<ColoredPoint> _pendingStroke; //the last or currentldy-being-drawn List of points
 	Stack<ScribbleNodeEdit> undos;
 	Stack<ScribbleNodeEdit> redos;
 
 	JPanel _scribbleArea; //the scribble area must be contained in this jpanel so we can delete and drag
 	
 	public final static int BORDER_WIDTH = 11; //the size of the red square signifying a delete
 	
 	public ScribbleNode(int ID, whiteboard.Backend w) {
 		super(ID, w);
 		_resizeLock = false;
 		_dragLock = false;
 		setBackground(Color.WHITE);
 		undos = new Stack<ScribbleNodeEdit>();
 		redos = new Stack<ScribbleNodeEdit>();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		setPreferredSize(new Dimension(200,150));
 		setSize(150,200);
 		type = BoardEltType.SCRIBBLE;
 		//setBorder(BorderFactory.createLineBorder(Color.GRAY,7));
 	}
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(300,400);
 	}
 
 	//remove the last drawn stroke
 	public void undo() {
 		if (undos.empty())
 			return;
 		ScribbleNodeEdit f = undos.pop();
 		if (f.type == ScribbleNodeEditType.DRAW) {
 			List<ColoredPoint> e = (List<ColoredPoint>) f.content;
 			redos.push(new ScribbleNodeEdit(e));
 		} else if (f.type == ScribbleNodeEditType.DRAG) {
 			Rectangle r = (Rectangle) f.content;
 			redos.push(new ScribbleNodeEdit(new Rectangle(getBounds())));
 			setBounds(r);
 		}
 	}
 
 
 	@Override
 	public void redo() {
 		if (redos.empty()) 
 			return;
 		ScribbleNodeEdit f = redos.pop();
 		if (f.type == ScribbleNodeEditType.DRAW) {
 			List<ColoredPoint> e = (List<ColoredPoint>) f.content;
 			undos.push(new ScribbleNodeEdit(e));
 		} else if (f.type == ScribbleNodeEditType.DRAG) {
 			Rectangle r = (Rectangle) f.content;
 			undos.push(new ScribbleNodeEdit(new Rectangle(getBounds())));
 			setBounds(r);
 		}
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		if(e.getX() > this.getWidth()-BORDER_WIDTH && e.getY() > this.getHeight()-BORDER_WIDTH){
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
 		}
 		else if(e.getX() > this.getWidth()-BORDER_WIDTH|| e.getX() < BORDER_WIDTH || e.getY() < BORDER_WIDTH|| e.getY() > this.getHeight()-BORDER_WIDTH) {
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		}
 		else {
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (withinDelete(e.getX(), e.getY())) {
 			backend.remove(this.getUID());
 		}
 	}
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 	Rectangle boundsBeforeMove;
 	@Override
 	public void mousePressed(MouseEvent e) {
 		wbp.setListFront(this);
 		startPt = new Point(e.getX(),e.getY());
 		if(e.getX() > this.getWidth()-BORDER_WIDTH && e.getY() > this.getHeight()-BORDER_WIDTH){
 			_resizeLock = true;
 		}
 		else if(e.getX() > this.getWidth()-BORDER_WIDTH|| e.getX() < BORDER_WIDTH || e.getY() < BORDER_WIDTH|| e.getY() > this.getHeight()-BORDER_WIDTH) {
 			_dragLock = true;
 		}
 		else {
 			LinkedList<ColoredPoint> ret = new LinkedList<ColoredPoint>();
 			Color toColor = e.getModifiers()==16?Color.BLACK:Color.white;
 			ret.add(new ColoredPoint(startPt, toColor));
 			ColoredPoint endPt = new ColoredPoint(startPt.x+1, startPt.y+1, toColor);
 			ret.add(endPt);
 			_pendingStroke = ret;
 		}
 		boundsBeforeMove = getBounds();
 	}
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		Rectangle previousBounds = getBounds();
 		int dx =  (e.getX() - startPt.x);
 		int dy =  (e.getY() - startPt.y);
 		if(_resizeLock){
 			if (e.getX() > BORDER_WIDTH*8) {//the resize leaves us with positive width
 				setBounds(previousBounds.x, previousBounds.y, e.getX(), previousBounds.height);
 			}
 			if (e.getY()> BORDER_WIDTH*8) { //the resize leaves us with positive height
 				setBounds(previousBounds.x, previousBounds.y, getBounds().width, e.getY());
 			}
 			wbp.extendPanel(getBounds());
 			startPt.setLocation(e.getX(),e.getY());
 		}
 		else if(_dragLock){
 			if (previousBounds.x + dx >= 0 && previousBounds.y + dy >= 0)
 				setBounds(previousBounds.x + dx,previousBounds.y + dy,previousBounds.width,previousBounds.height);
 			wbp.extendPanel(getBounds());
 		}
 		else{ //we're drawing
 			if (e.getModifiers() == 16) { //left click
 				_pendingStroke.add(new ColoredPoint(e.getPoint(), Color.BLACK));
 			} else if (e.getModifiers() == 4) { //right click
 				_pendingStroke.add(new ColoredPoint(e.getPoint(), Color.WHITE));
 			}
 		}
 		System.out.println("DRAGGING");
 		repaint();
 		backend.getPanel().repaint();
 		revalidate();
 	}
 	//the mouse has been released, stop connecting points
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (_resizeLock || _dragLock) {
 			undos.push(new ScribbleNodeEdit(boundsBeforeMove));
 			notifyBackend(BoardActionType.ELT_MOD);
 		} else {
 			undos.push(new ScribbleNodeEdit(_pendingStroke));
 			notifyBackend(BoardActionType.ELT_MOD);
 			_pendingStroke = null;
 		}
 		_resizeLock = false;
 		_dragLock = false;
 		//if(!withinDelete(e.getX(), e.getY()))
 		//	this.notifyBackend(BoardActionType.ELT_MOD);
 	}
 	
 	public boolean withinDelete(int x, int y) {
 		return (x < BORDER_WIDTH && y < BORDER_WIDTH);
 	}
 
 	@Override
 	public BoardElt clone() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void addAction(ActionObject ao) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void paintComponent(Graphics graphics) {
 		super.paintComponent(graphics);
 		Graphics2D g = (Graphics2D) graphics;
 		g.setColor(Color.WHITE);
 		g.fillRect(BORDER_WIDTH, BORDER_WIDTH, getWidth()-2*BORDER_WIDTH, getHeight() - 2*BORDER_WIDTH);
 		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 		for (ScribbleNodeEdit edit : undos) {
 			if (edit.type != ScribbleNodeEditType.DRAW) { //only draw the DRAW objects
 				continue;
 			}
 			List<ColoredPoint> stroke = (List<ColoredPoint>) edit.content;
 			Iterator<ColoredPoint> it = stroke.iterator();
 			if (!it.hasNext()) continue;
 			ColoredPoint prev = it.next();
 			while(it.hasNext()) {
 				ColoredPoint temp = it.next();
 				g.setColor(temp.c);
 				g.drawLine(prev.x, prev.y, temp.x, temp.y);
 				prev = temp;
 			}
 		}
 		//draw the line currently being drawn
 		if (!(_pendingStroke == null)){
 			Iterator<ColoredPoint> it = _pendingStroke.iterator();
 			if (!it.hasNext()) return;
 			ColoredPoint prev = it.next();
 			while(it.hasNext()) {
 				ColoredPoint temp = it.next();
 				g.setColor(temp.c);
 				g.drawLine(prev.x, prev.y, temp.x, temp.y);
 				prev = temp;
 			}
 		}
 		final int radius = 14;
 		//draw the border
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, getWidth(), BORDER_WIDTH);
 		g.fillRect(0, 0, BORDER_WIDTH, getHeight());
 		g.fillRect(getWidth()-BORDER_WIDTH, 0, BORDER_WIDTH, getHeight());
 		g.fillRect(0, getHeight()-BORDER_WIDTH, getWidth(), BORDER_WIDTH);
 		//draw the delete square
 		g.setColor(Color.RED);
 		g.fillRect(0, 0, BORDER_WIDTH, BORDER_WIDTH);
 		//draw the resize square
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(getWidth()-BORDER_WIDTH, getHeight()-BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
 	}
 	
 	
 	public void ofSerialized(SerializedBoardElt b) {
 		SerializedScribbleNode sn = (SerializedScribbleNode) b;
 		UID = sn.UID;
 		setBounds(sn.bounds);
 		undos = sn.undos;
 		redos = sn.redos;	
 	}
 	
 	@Override
 	public SerializedBoardElt toSerialized() {
 		SerializedScribbleNode toReturn = new SerializedScribbleNode();
 		toReturn.UID = UID;
		toReturn.bounds = getBounds();
 		toReturn.undos = undos;
 		toReturn.redos = redos;
 		return toReturn;
 	}
 }
