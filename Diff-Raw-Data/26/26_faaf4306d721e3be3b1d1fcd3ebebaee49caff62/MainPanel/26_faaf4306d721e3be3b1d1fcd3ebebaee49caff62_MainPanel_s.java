 package gui;
 
 // for graphics
 import javax.swing.JPanel;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Point;
 
 // data struct
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Iterator;
 import java.util.ListIterator;
 
 // for events
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.ComponentListener;
 import java.awt.event.ComponentEvent;
 
 import instructions.*;
 import javax.swing.event.EventListenerList;
 import java.awt.Component;
 
 import arranger.ArrangerConstants;
 import music.*;
 
 public class MainPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {
 	
 	boolean _disabled;
 	
 	// the last toolbar which was used
 	Toolbar _activeToolbar;
 	
 	List<Toolbar> _toolbars;
 	Toolbar _modeToolbar, _noteToolbar, _playToolbar;
 	ScoreWindow _scoreWindow;
 	
 	protected EventListenerList _listeners = new EventListenerList();
 	
 	//--------------------state information--------------------
 	EditMode _currMode = EditMode.NOTE;
 	EditDuration _currDuration = EditDuration.QUARTER;
 	EditModifier _currModifier = null;
 	
 	public MainPanel(Piece piece) {
 		Toolbar.init("images/gui/toolbarHorizontal.png", "images/gui/toolbarVertical.png");
 		ToolbarButton.init("images/gui/button.png", "images/gui/button_over.png");
 		
 		_toolbars = new LinkedList<Toolbar>();
 		
 		DockController dockControl = new DockController();
 		
 		// add left hand side toolbar with buttons for input modes
 		_modeToolbar = new ModeToolbar(dockControl);
 		_noteToolbar = new NoteToolbar(dockControl);
 		_playToolbar = new PlaybackToolbar(dockControl);
 		_scoreWindow = new ScoreWindow(piece);
 		
 		// add toolbars to list of toolbars
 		_toolbars.add(_modeToolbar);
 		_toolbars.add(_noteToolbar);
 		_toolbars.add(_playToolbar);
 		
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addComponentListener(this);
 		
 		setBackground(Color.WHITE);
 		
 		repaint();
 	}
 	
 	public void paint(Graphics g) {
 		super.paint(g);
 		
 		_scoreWindow.drawSelf(g);
 		
 		ListIterator<Toolbar> iter = _toolbars.listIterator(_toolbars.size());
 		while (iter.hasPrevious()) {
 			Drawable drawer = iter.previous();
 			drawer.drawSelf(g);
 		}
 	}
 	
 	public void updateScore() {
 		_scoreWindow.updateScore();
 		repaint();
 	}
 	
 	public Point getEventPoint(MouseEvent e) {
 		Point pt = e.getPoint();
 		//pt.setLocation(pt.getX(), pt.getY() + _scoreWindow.getSlideY());
 		return pt;
 	}
 	
 	/* Handle a mouse press on a toolbar or the score window
 	 * 
 	 */
 	public void mousePressed(MouseEvent e) {
 		if (_disabled)
 			return;
 		
 		Point evtPoint = getEventPoint(e);
 		
 		// clicked on which toolbar?
     		_activeToolbar = mouseEventToolbar(evtPoint);
 		if (_activeToolbar == null) {
 			// clicked on score window
 			_scoreWindow.mousePressed(evtPoint);
 		}
 		else {
 			// clicked on a toolbar
 			_activeToolbar.mousePressed(evtPoint);
 		}
 	}
 
 	/* Handle a mouse release on a toolbar or the score window
 	 * 
 	 */
 	public void mouseReleased(MouseEvent e) {
 		if (_disabled)
 			return;
 		
 		// factor in offset
 		Point evtPoint = getEventPoint(e);
 		
 		// clicked on which toolbar?
 		_activeToolbar = null;
 		Toolbar tbar = mouseEventToolbar(evtPoint);
 		
 		Instruction instr;
 		if (tbar == null) {
 			// clicked on score window
 			List<InstructionIndex> listIndex = _scoreWindow.mouseReleased(evtPoint);
 			instr = new EditInstruction(this, listIndex, EditInstructionType.REPLACE, EditType.MULTINOTE, new MultiNote(new Rational(1, 2)));
 		}
 		else {
 			// clicked on a toolbar
 			instr = tbar.mouseReleased(evtPoint);
 		}
 		
 		sendInstruction(instr);
 		
 		repaint();
 	}
 
 	/* Handle a mouse click on a toolbar or the score window
 	 * 
 	 */
 	public void mouseClicked(MouseEvent e) {
 		if (_disabled)
 			return;
 		
 		Point evtPoint = getEventPoint(e);
 		
 		// clicked on which toolbar?
 		Toolbar tbar = mouseEventToolbar(evtPoint);
 		
 		Instruction instr;
 		if (tbar == null) {
 			// clicked on score window
 			List<InstructionIndex> listIndex = _scoreWindow.mouseClicked(evtPoint);
 			instr = new EditInstruction(this, listIndex, EditInstructionType.REPLACE, EditType.MULTINOTE, new MultiNote(_currDuration.getDuration()));
 		}
 		else {
 			// clicked on a toolbar
 			instr = tbar.mouseClicked(evtPoint);
 		}
 		
 		sendInstruction(instr);
 		
 		repaint();
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 		if (_disabled)
 			return;
 		
 		Point evtPoint = getEventPoint(e);
 		
 		// clicked on which toolbar?
 		Instruction instr;
 		if (_activeToolbar == null) {
 			// clicked on score window
 			List<InstructionIndex> listIndex = _scoreWindow.mouseDragged(evtPoint);
			instr = new EditInstruction(this, listIndex, EditInstructionType.REPLACE, EditType.MULTINOTE, new MultiNote(new Rational(1, 2)));
 		}
 		else {
 			// dragging a toolbar
			instr = _activeToolbar.mouseDragged(evtPoint);
 		}
 		
		sendInstruction(instr);
		
 		repaint();
 	}
 	
 	public void mouseEntered(MouseEvent e) {
 		
 		
 		Point evtPoint = getEventPoint(e);
 	}
 	
 	public void mouseExited(MouseEvent e) {
 		
 		Point evtPoint = getEventPoint(e);
 		
 	}
 	
 	public void mouseMoved(MouseEvent e) {
 		
 	}
 	
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		// slide 50 pixels for every mouse wheel notch
 		_scoreWindow.slide(50 * e.getWheelRotation());
 		repaint();
 	}
 	
 	/*
 	 * Returns: the toolbar on which the mouse event occurred
 	 */
 	private Toolbar mouseEventToolbar(Point e) {
 		Iterator<Toolbar> iter = _toolbars.iterator();
 		while (iter.hasNext()) {
 			Toolbar drawer = iter.next();
 			
 			if (drawer.hitTestPoint(e.getX(), e.getY())) {
 				// mouse clicked on this toolbar
 				
 				// move this toolbar to the front (higher priority for future events)
 				iter.remove();
 				_toolbars.add(drawer);
 				
 				return drawer;
 			}
 		}
 		
 		// mouse event was not on a toolbar
 		return null;
 	}
 	
 	public void componentResized(ComponentEvent e) {
 		// redraw score sheet
 		Component comp = e.getComponent();
 		ArrangerConstants.WINDOW_WIDTH = comp.getWidth();
 		ArrangerConstants.WINDOW_HEIGHT = comp.getHeight();
 		
 		repaint();
 	}
 	public void componentHidden(ComponentEvent e) {
 		
 	}
 	public void componentMoved(ComponentEvent e) {
 		
 	}
 	public void componentShown(ComponentEvent e) {
 		
 	}
 	
 	public synchronized void addInstructionListener(InstructionListener listener)  {
 		_listeners.add(InstructionListener.class, listener);
 	}
 	
 	public synchronized void removeInstructionListener(InstructionListener listener) {
 		_listeners.remove(InstructionListener.class, listener);
 	}
 	
 	// call this method whenever you want to notify
 	//the event listeners of the particular event
 	private synchronized void sendInstruction(Instruction instr) {
 		
 		// intercept GUI instructions
 		if (instr instanceof ModeInstruction) {
 			ModeInstruction modeInstr = (ModeInstruction) instr;
 			switch (modeInstr.getType()) {
 			case MODE:
 				_currMode = (EditMode) modeInstr.getValue();
 				break;
 			case DURATION:
 				_currDuration = (EditDuration) modeInstr.getValue();
 				break;
 			case MODIFIER:
 				_currModifier = (EditModifier) modeInstr.getValue();
 				break;
 			}
 			return;
 		}
 		
 		if (instr == null)
 			return;
 		
 		InstructionListener[] listeners = _listeners.getListeners(InstructionListener.class);
 		
 		for (int i = 0; i < listeners.length; i++) {
 			listeners[i].receiveInstruction(instr);
 		}
 	}
 }
