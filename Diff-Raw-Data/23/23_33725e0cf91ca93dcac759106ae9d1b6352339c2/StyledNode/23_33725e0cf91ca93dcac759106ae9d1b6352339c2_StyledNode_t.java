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
 import java.util.Stack;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.StyledDocument;
 import javax.swing.undo.UndoableEdit;
 
 import whiteboard.BoardActionType;
 import GUI.WhiteboardPanel;
 
 public class StyledNode extends BoardElt implements MouseListener, MouseMotionListener{
     /**
      * 
      */
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
 
     public final static int BORDER_WIDTH = 10;
     public final static Dimension DEFAULT_SIZE = new Dimension(200,150);
 
     public StyledNode(int UID, whiteboard.Backend w){
         super(UID, w);
         _fontMenu = new JPopupMenu();
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
                 	notifyBackend(BoardActionType.ELT_MOD);
                 }
             });
             _styleMenu.add(fontItem);
         }
 
         //Different Colors
         _colorMenu = new JMenu("Colors");
         final String colorNames[] = 
         {"BLACK","BLUE","CYAN","DARK_GRAY","GRAY","LIGHT_GRAY","MAGENTA","ORANGE","PINK","RED","WHITE","YELLOW"};
         final Color colors[] = {Color.BLACK,Color.BLUE,Color.CYAN,Color.DARK_GRAY,Color.GRAY,Color.LIGHT_GRAY,Color.MAGENTA,
                 Color.ORANGE,Color.PINK,Color.RED,Color.WHITE,Color.YELLOW};
         for(int i=0;i<colorNames.length;i+=1){
             final Color color = colors[i];
             JMenuItem fontItem = new JMenuItem(colorNames[i]);
             fontItem.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     content.setForeground(color);
                     notifyBackend(BoardActionType.ELT_MOD);
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
                     notifyBackend(BoardActionType.ELT_MOD);
         		}
         	});
             _fontSizeMenu.add(fontSize);
         }
 
         _fontMenu.add(_styleMenu);
         _fontMenu.addSeparator();
         _fontMenu.add(_fontSizeMenu);
         _fontMenu.addSeparator();
         _fontMenu.add(_colorMenu);
 
 
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
         
         System.out.println(content.requestFocusInWindow());
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
     }
 
     public class BoardCommUndoableEditListener implements UndoableEditListener {
         @Override
         public void undoableEditHappened(UndoableEditEvent e) {
             undos.push(new StyledNodeEdit(e.getEdit()));
             if(e.getEdit().getPresentationName().equals("addition")) {
                 try {
                     if(text.getText(text.getLength()-1, 1).equals("\n")) {
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
             text.insertString(0, "\u2022 Make a node", null);
             text.insertString(text.getLength(), "\n\u2022 Fill it in", null);
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
         text.addUndoableEditListener(new BoardCommUndoableEditListener());
         JTextPane toReturn = new JTextPane(text);
         toReturn.addFocusListener(new FocusListener() {
         	String lastText = "";
             @Override
             public void focusGained(FocusEvent e) {
             	backend.alertEditingStatus(StyledNode.this, true);
                 System.out.println("StyledNode: GAINED FOCUS, alerted backend");
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             	System.out.println("Lost focus");
                 content.revalidate();
                 if (!lastText.equals(content.getText())) { //send changes over the network
                 	notifyBackend(BoardActionType.ELT_MOD);
                 }
                 lastText = content.getText();
                 backend.alertEditingStatus(StyledNode.this, false);
             }
 
 
         });
         toReturn.addMouseListener(new MouseListener() {
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 // TODO Auto-generated method stub
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
                 // TODO Auto-generated method stub
                 System.out.println(getUID());
                 if (e.getModifiers() == 4) {
                     _fontMenu.show(StyledNode.this,e.getX(),e.getY());
                 }
                 wbp.setListFront(StyledNode.this.UID);
                 content.grabFocus();
                 StyledNode.this.repaint();
 
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 // TODO Auto-generated method stub
 
             }
         });
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
             UndoableEdit e = (UndoableEdit) f.getContent();
             if (e.canRedo()) {
                 e.redo();
                 undos.push(f);
             } else {
                 System.out.println("Could not redo " + e);
             }
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
             UndoableEdit e = (UndoableEdit) f.getContent();
             if (e.canUndo()) {
                 e.undo();
                 redos.push(f);
             } else {
                 System.out.println("Could not undo " + e);
             }
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
     public BoardElt clone() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         if (e.getX() < BORDER_WIDTH && e.getY() < BORDER_WIDTH) {
             backend.remove(this.getUID());
         }
     }
     @Override
     public void mouseEntered(MouseEvent e) {
     }
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     Rectangle boundsBeforeMove;
     public void mousePressed(MouseEvent e) {
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
         if (_resizeLock || _dragLock) {
             System.out.println("releasing lock " + _resizeLock + _dragLock);
             undos.push(new StyledNodeEdit(boundsBeforeMove));
             notifyBackend(BoardActionType.ELT_MOD);
             System.out.println(undos.size());
         }
         _resizeLock = false;
         _dragLock = false;
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
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
 
     public static class StyledNodeEdit {
         private Object content;
         private StyledNodeEditType type;
         //the added edit
         public StyledNodeEdit(UndoableEdit e) {
             content = e;
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
         	g.setColor(Color.ORANGE);
        	g.fillOval(getWidth()-BORDER_WIDTH, 0, BORDER_WIDTH, BORDER_WIDTH);
         }
 
 
     }
 
     @Override
     public void ofSerialized(SerializedBoardElt b) {
         System.out.println("ofSerialized stylednode");
         SerializedStyledNode ssn = (SerializedStyledNode) b;
         this.setBounds(ssn.bounds);
         view.setBounds(BORDER_WIDTH, BORDER_WIDTH, getWidth()-2*BORDER_WIDTH, getHeight()-2*BORDER_WIDTH);
         //undos = ssn.undos;
         //redos = ssn.redos;
         content.setFont(ssn.style);
         content.setForeground(ssn.fontColor);
         System.out.println("Setting text to " + ssn.text);
         content.setText(ssn.text);
         repaint();
         content.repaint();
         view.revalidate();
         view.repaint();
     }
 
     @Override
     public SerializedBoardElt toSerialized() {
         return new SerializedStyledNode(this);
     }
 
 }
