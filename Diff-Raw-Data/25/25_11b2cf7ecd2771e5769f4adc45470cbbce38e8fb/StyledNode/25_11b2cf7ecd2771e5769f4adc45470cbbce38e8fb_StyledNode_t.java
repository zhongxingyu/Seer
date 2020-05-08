 package boardnodes;
 import java.awt.*;
 import java.util.Stack;
 import whiteboard.Whiteboard;
 import GUI.WhiteboardPanel;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.StyledDocument;
 import javax.swing.undo.UndoableEdit;
 
 import whiteboard.BoardActionType;
 
 import boardnodes.BoardEltType;
 
 public class StyledNode extends BoardElt implements MouseListener, MouseMotionListener{
 	final BoardEltType Type = BoardEltType.NODE;
	public Point startPt,nextPt;
 	JTextPane content;
 	StyledDocument text;
 	Stack<UndoableEdit> undos;
 	Stack<UndoableEdit> redos;
	WhiteboardPanel _wbp;
	boolean _resizeLock;
 	
 	public StyledNode(int UID, whiteboard.Whiteboard w,WhiteboardPanel wbp){
 		super(UID, w,wbp);
 		undos = new Stack<UndoableEdit>();
 		redos = new Stack<UndoableEdit>();
		_resizeLock = false;
		_wbp = wbp;
 		content = createEditorPane();
 		JScrollPane view = 
 			new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		view.setPreferredSize(new Dimension(200,150));
 		System.out.println("Y is : " + view.getHeight() + "  X is : " + view.getWidth());
 		this.add(view);
 		this.setSize(new Dimension(215,165));
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		repaint();
 		view.repaint();
 		revalidate();
 		view.revalidate();
 	}
 	
 	public class BoardCommUndoableEditListener implements UndoableEditListener {
 		@Override
 		public void undoableEditHappened(UndoableEditEvent e) {
 			undos.push(e.getEdit());
 			if(e.getEdit().getPresentationName().equals("addition")) {
 				try {
 					if(text.getText(text.getLength()-1, 1).equals("\n")) {
 						text.insertString(text.getLength(), "\u2022 ", null);
 					}
 				} catch (BadLocationException e1) {
 					e1.printStackTrace();
 				}
 			}
 			notifyWhiteboard(BoardActionType.ELT_MOD);
 		}
 	}
 	
 	private JTextPane createEditorPane() {
 		text = new DefaultStyledDocument();
 		text.addUndoableEditListener(new BoardCommUndoableEditListener());
 		//text.
 		try {
 			text.insertString(0, "\u2022 Make a node", null);
 			text.insertString(text.getLength(), "\n\u2022 Fill it in", null);
 			//if (text.)
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		JTextPane toReturn = new JTextPane(text);
 		toReturn.setPreferredSize(new Dimension(200,150));
 		
 		return toReturn;
 	}
 	
 
 
 	@Override
 	void decode(String obj) {
 	}
 
 
 	@Override
 	public String encode() {
 		return null;
 	}
 	
 
 	@Override
 	public void addAction(ActionObject ao) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void redo() {
 		if (redos.empty()) 
 			return;
 		UndoableEdit e = redos.pop();
 		if (e.canRedo()) {
 			e.redo();
 			undos.push(e);
 		} else {
 			System.out.println("Could not redo " + e);
 		}
 	}
 
 
 	@Override
 	public void undo() {
 		if (undos.empty()) 
 			return;
 		UndoableEdit e = undos.pop();
 		if (e.canUndo()) {
 			e.undo();
 			redos.push(e);
 		} else {
 			System.out.println("Could not undo " + e);
 		}
 	}
 
 
 	@Override
 	public BoardElt clone() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public Point getPos() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public void setPos(Point p) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 	
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		System.out.println("CLICKED!");
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		System.out.println("ENTERED!");
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
		startPt = new Point(e.getX(),e.getY());
		if(e.getX() > this.getWidth()-15 && e.getY() > this.getHeight()-15){
			_resizeLock = true;
		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
		_resizeLock = false;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
		if(_resizeLock){
			System.out.println(startPt);
			System.out.println(e.getX() + "<====X     Y=====>" + e.getY());
			Rectangle currentLocation = getBounds();
			setBounds(currentLocation.x,currentLocation.y,currentLocation.width - (startPt.x - e.getX()),currentLocation.height - (startPt.y - e.getY()));
			startPt.setLocation(e.getX(),e.getY());
 		}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 }
