 package boardnodes;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import javax.swing.BorderFactory;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.StyledDocument;
 
 import networking.ClientInfo;
 
 import whiteboard.BoardActionType;
 import whiteboard.SearchResult;
 import GUI.WhiteboardPanel;
 
 public class StyledNode extends BoardElt implements MouseListener, MouseMotionListener{
 	public static int UIDCounter = 0;
 	private Point startPt,nextPt;
 	JTextPane content;
 	StyledDocument text;
 	Stack<StyledNodeEdit> undos;
 	Stack<StyledNodeEdit> redos;
 	WhiteboardPanel _wbp;
 	JScrollPane view;
 	boolean _resizeLock,_dragLock;
 	JMenu _styleMenu, _colorMenu, _fontSizeMenu;
 	JPopupMenu _fontMenu;
 	JPopupMenu popup;
 
 	String lastText; //what this text says now, or what it said before you focused and started editing
 	Font lastFont;
 	Point lastClick;
 
 	boolean autoBullet; //whether we should add in a bullet every time;
 
 	public final static Dimension DEFAULT_SIZE = new Dimension(200,150);
 
 	public StyledNode(int UID, whiteboard.Backend w){
 		super(UID, w);
 		BORDER_WIDTH = 10;
 		autoBullet = true;
 		_fontMenu = new JPopupMenu();
 		popup = new JPopupMenu();
 
 		//Different Styles of Typing
 		_styleMenu = new JMenu("Styles");
 		final String fonts[] = 
 		{"Times New Roman","Arial","Courier New","Abberancy","Dialog","FreeSerif","Impact","SansSerif","Verdana"};
 		for(int i=0;i<fonts.length;i+=1){
 			final String fontName = fonts[i];
 			JMenuItem fontItem = new JMenuItem(fontName);
 			fontItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					content.setFont(new Font(fontName, content.getFont().getStyle(), content.getFont().getSize()));
 					//notifyBackend(BoardActionType.ELT_MOD);
 				}
 			});
 			_styleMenu.add(fontItem);
 		}
 
 		//Different Colors
 		_colorMenu = new JMenu("Colors");
 		final String colorNames[] = 
 		{"BLACK","BLUE","CYAN","DARK GRAY","GRAY","LIGHT GRAY","MAGENTA","ORANGE","PINK","RED","WHITE","YELLOW", "GREEN"};
 		final Color colors[] = {Color.BLACK,Color.BLUE,Color.CYAN,Color.DARK_GRAY,Color.GRAY,Color.LIGHT_GRAY,Color.MAGENTA,
 				Color.ORANGE,Color.PINK,Color.RED,Color.WHITE,Color.YELLOW, Color.GREEN};
 		for(int i=0;i<colorNames.length;i+=1){
 			final Color color = colors[i];
 			JMenuItem fontItem = new JMenuItem(colorNames[i]);
 			fontItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					content.setForeground(color);
 					//notifyBackend(BoardActionType.ELT_MOD);
 				}
 			});
 			_colorMenu.add(fontItem);
 		}
 
 		//Different Sizes
 		_fontSizeMenu = new JMenu("Size");
 		final int[] sizes = {6, 7, 8, 9, 10, 12, 14, 18, 24, 36, 52, 72};
 		for (final int a : sizes) {
 			JMenuItem fontSize = new JMenuItem(a+"");
 			fontSize.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					Font replaceWith = content.getFont().deriveFont((float)a);
 					content.setFont(replaceWith);
 					//notifyBackend(BoardActionType.ELT_MOD);
 				}
 			});
 			_fontSizeMenu.add(fontSize);
 		}
 		
 		final JMenuItem bulletMenu = new JMenuItem("Turn Off Bullets");
 		bulletMenu.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				autoBullet = !autoBullet;
 				bulletMenu.setText("Turn " + (autoBullet ? "Off" : "On") + " Bullets");
 			}
 		});
 
 		_fontMenu.add(_styleMenu);
 		_fontMenu.addSeparator();
 		_fontMenu.add(_fontSizeMenu);
 		_fontMenu.addSeparator();
 		_fontMenu.add(_colorMenu);
 		_fontMenu.addSeparator();
 		_fontMenu.add(bulletMenu);
 
 		//copying
 		JMenuItem copyItem = new JMenuItem("Copy");
 		popup.add(copyItem);
 		copyItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				backend.copy(StyledNode.this);
 			}
 		});
 		
 		 popup.addSeparator();
 			JMenuItem addPathItem = new JMenuItem("Add Path");
 			addPathItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					BoardPath newPath = (BoardPath) backend.getPanel().newElt(BoardEltType.PATH, BoardPathType.NORMAL);
 					newPath.autoSnapTo(StyledNode.this, lastClick);
 				}
 			});
 			popup.add(addPathItem);
 
 			JMenuItem dottedPathItem = new JMenuItem("Add Dotted Path");
 			dottedPathItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					BoardPath newPath = (BoardPath) backend.getPanel().newElt(BoardEltType.PATH, BoardPathType.DOTTED);
 					newPath.autoSnapTo(StyledNode.this, lastClick);
 				}
 			});
 			popup.add(dottedPathItem);
 
 			JMenuItem arrowPathItem = new JMenuItem("Add Arrow");
 			arrowPathItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					BoardPath newPath = (BoardPath) backend.getPanel().newElt(BoardEltType.PATH, BoardPathType.ARROW);
 					newPath.autoSnapTo(StyledNode.this, lastClick);
 				}
 			});
 			popup.add(arrowPathItem);
 
 			JMenuItem dottedArrowPathItem = new JMenuItem("Add Dotted Arrow");
 			dottedArrowPathItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					BoardPath newPath = (BoardPath) backend.getPanel().newElt(BoardEltType.PATH, BoardPathType.DOTTED_ARROW);
 					newPath.autoSnapTo(StyledNode.this, lastClick);
 				}
 			});
 			popup.add(dottedArrowPathItem);
 		
 
 		type = BoardEltType.STYLED;
 		setLayout(null);
 		undos = new Stack<StyledNodeEdit>();
 		redos = new Stack<StyledNodeEdit>();
 		_resizeLock = false;
 		_dragLock = false;
 		content = createEditorPane();
 		view = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
 		view.setBounds(BORDER_WIDTH, BORDER_WIDTH, DEFAULT_SIZE.width, DEFAULT_SIZE.height);
 		this.add(view);
 		this.setSize(new Dimension(DEFAULT_SIZE.width + BORDER_WIDTH*2, DEFAULT_SIZE.height + BORDER_WIDTH*2));
 		addMouseListener(this);
 		addMouseMotionListener(this);
 
 		content.requestFocusInWindow();
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				content.grabFocus();
 				content.requestFocusInWindow();
 			}
 		});
 		revalidate();
 		view.revalidate();
 		repaint();
 		view.repaint();
 		//notifyBackend(BoardActionType.ELT_MOD); //once you've made text change, pressing undo will revert to initial text
 	}
 
 	@Override
 	public String getText() {
 		return this.content.getText();
 	}
 
 	public class BoardCommUndoableEditListener implements UndoableEditListener {
 		@Override
 		public void undoableEditHappened(UndoableEditEvent e) {
 			//undos.push(new StyledNodeEdit(e.getEdit()));
 			if(e.getEdit().getPresentationName().equals("addition")) {
 				try {
 					if(autoBullet && text.getText(text.getLength()-1, 1).equals("\n")) {
 						text.insertString(text.getLength(), "\u2022 ", null);
 					}
 				} catch (BadLocationException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private JTextPane createEditorPane() {
 		text = new DefaultStyledDocument();
 		try {
 			text.insertString(0, "\u2022 ", null);
 			//text.insertString(text.getLength(), "\n\u2022 Fill it in", null);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		text.addUndoableEditListener(new BoardCommUndoableEditListener());
 		JTextPane toReturn = new JTextPane(text);
 		toReturn.addFocusListener(new FocusListener() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				if (!content.isEditable())
 					return;
 				backend.alertEditingStatus(StyledNode.this, true);
 				System.out.println("StyledNode: GAINED FOCUS, alerted backend if it was editable");
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				System.out.println("Lost focus " + this);
 				if (!content.isEditable()) {
 					return;
 				}
 				content.revalidate();
 				if (!lastText.equals(content.getText()) || !lastFont.equals(content.getFont())) { //send changes over the network
 					System.out.println("Adding to the undo stack");
 					undos.push(new StyledNodeEdit(lastText, lastFont));
 					lastText = content.getText();
 					lastFont = content.getFont();
 					backend.alertEditingStatus(StyledNode.this, false);
 					notifyBackend(BoardActionType.ELT_MOD);
 				}
 				backend.alertEditingStatus(StyledNode.this, false);
 			}
 
 
 		});
 		toReturn.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// 
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				System.out.println(getUID());
 				if (e.getModifiers() == 4) {
					_fontMenu.show(StyledNode.this,e.getX()-view.getViewport().getViewPosition().x,e.getY()-view.getViewport().getViewPosition().y);
 				}
 				wbp.setListFront(StyledNode.this.UID);
 				//content.grabFocus();
 				StyledNode.this.repaint();
 
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		lastText = "\u2022 ";
 		lastFont = toReturn.getFont();
 		undos.push(new StyledNodeEdit(lastText, lastFont));
 		toReturn.setCaretPosition(toReturn.getDocument().getLength());
 		toReturn.grabFocus();
 		return toReturn;
 	}
 
 	@Override
 	public void addAction(ActionObject ao) {
 		// TODO Auto-generated method stub		
 	}
 
 	@Override
 	public void redo() {
 		if (redos.empty()) 
 			return;
 		StyledNodeEdit f = redos.pop();
 		if (f.getType() == StyledNodeEditType.TEXT) {
 			Object[] info = (Object[]) f.getContent();
 			String revertTo = (String) info[0];
 			Font display = (Font) info[1];
 			undos.push(new StyledNodeEdit(content.getText(), content.getFont()));
 			content.setText(revertTo);
 			content.setFont(display);
 		} else if (f.getType() == StyledNodeEditType.DRAG) {
 			Rectangle r = (Rectangle) f.getContent();
 			undos.push(new StyledNodeEdit(new Rectangle(getBounds())));
 			setBounds(r);
 			view.setBounds(BORDER_WIDTH, BORDER_WIDTH, r.width-2*BORDER_WIDTH, r.height-2*BORDER_WIDTH);
 		}
 		revalidate();
 		repaint();
 	}
 
 	@Override
 	public void undo() {
 		if (undos.empty()) 
 			return;
 		StyledNodeEdit f = undos.pop();
 		if (f.getType() == StyledNodeEditType.TEXT) {
 			Object[] info = (Object[]) f.getContent();
 			String revertTo = (String)info[0];
 			Font display = (Font) info[1];
 			System.out.println(revertTo);
 			redos.push(new StyledNodeEdit(content.getText(), content.getFont()));
 			content.setText(revertTo);
 			content.setFont(display);
 		} else if (f.getType() == StyledNodeEditType.DRAG) {
 			Rectangle r = (Rectangle) f.getContent();
 			redos.push(new StyledNodeEdit(new Rectangle(getBounds())));
 			setBounds(r);
 			view.setBounds(BORDER_WIDTH, BORDER_WIDTH, r.width-2*BORDER_WIDTH, r.height-2*BORDER_WIDTH);
 		}
 		revalidate();
 		repaint();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		lastClick = (Point) e.getPoint().clone();
 		if(e.getModifiers()!=4) {
 			if (isBeingEdited)
 				return;
 			if (e.getX() < BORDER_WIDTH && e.getY() < BORDER_WIDTH) {
 				backend.remove(this.getUID());
 				removeAllSnappedPaths();
 			}
 		} else {
 			//right click
 			popup.show(StyledNode.this, e.getX(), e.getY());
 		}
 	}
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if(_mouseListener.draggedPath!=null) {
 			_mouseListener.draggedPath.snapTo(this);
 		}
 	}
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if(_mouseListener.draggedPath!=null && (e.getX()<0 || e.getX()>this.getWidth() || e.getY()<0 || e.getY()>this.getHeight())) {
 			_mouseListener.draggedPath.unsnapDrag(this);
 		}
 	}
 
 	Rectangle boundsBeforeMove;
 	public void mousePressed(MouseEvent e) {
 		if (isBeingEdited) { //you shouldn't be able to do anything if it's already being edited elsewhere
 			return;
 		}
 		wbp.setListFront(this.UID);
 		content.grabFocus();
 		startPt = new Point(e.getX(),e.getY());
 		if(e.getX() > this.getWidth()-BORDER_WIDTH && e.getY() > this.getHeight()-BORDER_WIDTH){
 			_resizeLock = true;
 		}
 		else {
 			_dragLock = true;
 		}
 		boundsBeforeMove = getBounds();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (!isBeingEdited && (_resizeLock || _dragLock)) {
 			undos.push(new StyledNodeEdit(boundsBeforeMove));
 			notifyBackend(BoardActionType.ELT_MOD);
 			System.out.println(undos.size());
 		}
 		_resizeLock = false;
 		_dragLock = false;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if (isBeingEdited) { //you shouldn't have control of it
 			return;
 		}
 		int dx = e.getX() - startPt.x;
 		int dy = e.getY() - startPt.y;
 		int screenX = e.getX() + getBounds().x;
 		int screenY = e.getY() + getBounds().y;
 		Rectangle previousBounds = getBounds();
 		Rectangle prevView = view.getBounds();
 		if(_resizeLock){
 			if (e.getX() > BORDER_WIDTH*8) { //the resize leaves us with positive width
 				setBounds(previousBounds.x, previousBounds.y, e.getX(), previousBounds.height);
 				view.setBounds(prevView.x, prevView.y, e.getX()-BORDER_WIDTH*2, prevView.height);
 			}
 			if (e.getY()> BORDER_WIDTH*8) { //the resize leaves us with positive height
 				setBounds(previousBounds.x, previousBounds.y, getBounds().width, e.getY());
 				view.setBounds(prevView.x, prevView.y, view.getBounds().width, e.getY()-BORDER_WIDTH*2);
 			}
 			view.setPreferredSize(new Dimension(view.getBounds().width, view.getBounds().height));
 			startPt.setLocation(e.getX(), e.getY());
 		} else if (_dragLock) {
 
 			if (previousBounds.x + dx >= 0 && previousBounds.y + dy >= 0)
 				setBounds(previousBounds.x + dx, previousBounds.y + dy, previousBounds.width, previousBounds.height);
 			//if (screenX>= 0 && screenY>= 0)
 			//	setBounds(screenX, screenY, previousBounds.width, previousBounds.height);
 		}
 		wbp.extendPanel(getBounds());
 		repaint();
 		wbp.repaint();
 		revalidate();
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		if(e.getX() > this.getWidth()-BORDER_WIDTH && e.getY() > this.getHeight()-BORDER_WIDTH){
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
 		}
 		else if(e.getX() > this.getWidth()-BORDER_WIDTH|| e.getX() < BORDER_WIDTH || e.getY() < BORDER_WIDTH|| e.getY() > this.getHeight()-BORDER_WIDTH) {
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		}
 
 	}
 
 	public static class StyledNodeEdit implements Serializable{
 		private Object content;
 		private StyledNodeEditType type;
 		//the added edit
 		public StyledNodeEdit(String body, Font curFont) {
 			content = new Object[]{body, curFont};
 			type = StyledNodeEditType.TEXT;
 		}
 		//@param r		the old location of this node
 		public StyledNodeEdit(Rectangle r) {
 			content = r;
 			type = StyledNodeEditType.DRAG;
 		}
 		public Object getContent() {
 			return content;
 		}
 
 		public StyledNodeEditType getType() {
 			return type;
 		}
 	}
 
 	private enum StyledNodeEditType {
 		DRAG, TEXT
 	}
 
 	public void paintComponent(Graphics graphics) {
 		super.paintComponent(graphics);
 		Graphics2D g = (Graphics2D) graphics;
 		//paint background for rounded
 		g.setColor(Color.GRAY);
 		g.fillRect(0,0,getWidth(), getHeight());
 
 		g.setColor(Color.DARK_GRAY);
 		g.fillRoundRect(0,0,getWidth(), getHeight(),10,10);
 		g.setColor(Color.RED);
 		g.fillRect(0, 0, BORDER_WIDTH, BORDER_WIDTH);
 
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(getWidth()-BORDER_WIDTH, getHeight()-BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
 
 		if (isBeingEdited) { //notify the user this is being modified elsewhere
 			if (currentEditor == null) System.err.println("Being edited by null editor");
 			else {
 				g.setColor(currentEditor.color);
 				g.fillOval(getWidth()-BORDER_WIDTH, 0, BORDER_WIDTH, BORDER_WIDTH);
 			}
 		}
 	}
 
 	@Override
 	public void ofSerialized(SerializedBoardElt b) {
 		SerializedStyledNode ssn = (SerializedStyledNode) b;
 		this.setBounds(ssn.bounds);
 		view.setBounds(BORDER_WIDTH, BORDER_WIDTH, getWidth()-2*BORDER_WIDTH, getHeight()-2*BORDER_WIDTH);
 		content.setFont(ssn.style);
 		content.setForeground(ssn.fontColor);
 		content.setText(ssn.text);
 		lastText = ssn.lastText;
 		lastFont = ssn.lastFont;
 		undos = ssn.undos;
 		redos = ssn.redos;
 		this.autoBullet = ssn.autoBullet;
 		repaint();
 		content.repaint();
 		view.revalidate();
 		view.repaint();
 	}
 
 	@Override
 	public void paste(Point pos) {
 		StyledNode toPaste = new StyledNode(0, this.getBackend());//(StyledNode) backend.getPanel().newElt(BoardEltType.STYLED, BoardPathType.NORMAL);
 		toPaste.setBounds(new Rectangle(pos, (Dimension) this.getBounds().getSize().clone()));
 		toPaste.view.setBounds(BORDER_WIDTH, BORDER_WIDTH, getWidth()-2*BORDER_WIDTH, getHeight()-2*BORDER_WIDTH);
 		toPaste.content.setFont(content.getFont());
 		toPaste.content.setText(content.getText());
 		toPaste.content.setForeground(content.getForeground());
 		backend.getPanel().addElt(toPaste);
 		toPaste.repaint();
 	}
 
 	@Override
 	public SerializedBoardElt toSerialized() {
 		SerializedStyledNode toReturn = new SerializedStyledNode();
 		toReturn.bounds = getBounds();
 		toReturn.UID = UID;
 		toReturn.text = new String(content.getText());
 		toReturn.style = content.getFont();
 		toReturn.fontColor = content.getForeground();
 		toReturn.lastText = lastText;
 		toReturn.lastFont = lastFont;
 		toReturn.undos = (Stack<StyledNodeEdit>) undos.clone();
 		toReturn.redos = (Stack<StyledNodeEdit>) redos.clone();
 		toReturn.autoBullet = this.autoBullet;
 		return toReturn;
 	}
 
 	@Override
 	public BoardElt clone() {
 		StyledNode toReturn = new StyledNode(-1, backend);
 		toReturn.setBounds(getBounds());
 		toReturn.content.setFont(content.getFont());
 		toReturn.content.setText(content.getText());
 		toReturn.content.setForeground(content.getForeground());
 		toReturn.autoBullet = this.autoBullet;
 		return toReturn;
 	}
 
 	@Override
 	public ArrayList<SearchResult> search(String query) {
 		ArrayList<SearchResult> toReturn = new ArrayList<SearchResult>();
 		String toSearch = this.getText();
 		int lastIndex = 0;
 		while(lastIndex!=-1) {
 			lastIndex = toSearch.indexOf(query, lastIndex);
 			if(lastIndex!=-1) {
 				toReturn.add(new SearchResult(this, lastIndex));
 				lastIndex++;
 			}
 		}
 		return toReturn;
 	}
 
 	@Override
 	public void highlightText(int index, int len, boolean isfocus) {
 
 		this.content.setHighlighter(hilit);		
 		try {
 			Color toColor = isfocus?Color.YELLOW:Color.LIGHT_GRAY;
 			hilit.addHighlight(index, index+len, new DefaultHighlighter.DefaultHighlightPainter(toColor));
 		} catch (BadLocationException e) {
 			System.out.println("stylednode: the given search index does not exist in the string! ignoring..");
 		}
 	}
 
 	@Override
 	public void clearHighlight() {
 		hilit.removeAllHighlights();		
 	}
 
 	@Override
 	public void setBeingEditedStatus(boolean isBeingEdited, ClientInfo sender) {
 		super.setBeingEditedStatus(isBeingEdited, sender);
 		this.content.setEditable(!isBeingEdited);
 	}
 
 }
