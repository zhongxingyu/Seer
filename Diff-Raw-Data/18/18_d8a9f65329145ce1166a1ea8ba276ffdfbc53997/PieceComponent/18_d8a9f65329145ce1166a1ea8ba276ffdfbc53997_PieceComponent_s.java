 package gui;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceEvent;
 import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseEvent;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 
 import javax.swing.JComponent;
 import javax.swing.TransferHandler;
 
 import framework.Piece;
 import framework.Side;
 
 public class PieceComponent extends JComponent implements Transferable, DragSourceListener, DragGestureListener{
 	
 	private Piece piece;
 	private Rectangle body;
 	private Shape [] sides;
	private boolean inBoard;
 	
 	private TransferHandler t;
 	private DragSource source;
 	
 	public PieceComponent(Piece p) {
 		piece = p;
 		inBoard = false;
 		pieceMaker();
 		
 		dnd();
 	}
 	
 	private void dnd() {
 		t = new TransferHandler(){
 
             public Transferable createTransferable(JComponent c){
                  return exportPiece();
             }
       };
 		TransferHandler t = new TransferHandler("getPiece");
 		setTransferHandler(t);
	}
	
	private Transferable exportPiece() {
		// TODO Auto-generated method stub
		return null;
 	}
 	
 	private void pieceMaker() {
 		
 		body = new Rectangle(50,50,100,100);
 		sides = new Shape[4];
 		for (int i = 0; i < sides.length; i++){
 			int val = piece.getSide(i).getValue();
 			val = Math.abs(val);
 //			switch(val){
 //			case Side.inHeart: sides[i] = PieceShape.getHeart(); break;
 //			case Side.inSpade: sides[i] = PieceShape.getSpade(); break;
 //			case Side.inClub: sides[i] = PieceShape.getClub(); break;
 //			case Side.inDiamond: sides[i] = PieceShape.getDiamond(); break;
 //			}
 		}
 		repaint();
 	}
 	
 	public int rotate(boolean clockwise){
 		if (clockwise){
 			piece.rotateClockwise();
 		}
 		else{
 			piece.rotateCounterClockwise();
 		}
 		pieceMaker();
 		return piece.getOrientation();
 	}
 	
 	public void paint(Graphics g){
 		Graphics2D g2 = (Graphics2D)g;
 		AffineTransform t = new AffineTransform();
 		AffineTransform saveAT = g2.getTransform();
 		g2.setTransform(t);
 		
 		//TODO Draw the piece here
 		
 		g2.setTransform(saveAT);
 	}
 
 	public boolean isInBoard(){
 		return inBoard;
 	}
 	
 	public void setInBoard(boolean tf){
 		inBoard = tf;
 	}
 	
 	public boolean toggleInBoard(){
 		inBoard = !inBoard;
 		return inBoard;
 	}
 	
 	public Piece getPiece(){
 		return piece;
 	}
 
 	@Override
 	public void dragGestureRecognized(DragGestureEvent dge) {
 		source.startDrag(dge, DragSource.DefaultCopyDrop, exportPiece(), this);
 		
 	}
 
 	/**
 	 * Successful DnD operation, copies the framework <code>Piece</code> in the
 	 * PieceComponent into framework <code>Board</code> in the PuzzleGameFrame
 	 * Snaps the piece into place to the location's center
 	 * and TODO highlight the available sides of the piece to snap into
 	 */
 	@Override
 	public void dragDropEnd(DragSourceDropEvent arg0) {
 		repaint();
 	}
 	
 	/**
 	 * Highlights the piece when it is hovering over a droppable location
 	 * outlines/highlights the piece and the location on the board
 	 */
 	@Override
 	public void dragEnter(DragSourceDragEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Un-highlights the piece and snaps it back to the home location outside
 	 * of the board
 	 */
 	@Override
 	public void dragExit(DragSourceEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Highlights the piece when it is hovering over a droppable location
 	 * outlines/highlights the piece and the location on the board that's center
 	 * is closest to the location this PieceComponent
 	 */
 	@Override
 	public void dragOver(DragSourceDragEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Listens for mouse wheel scrolls to rotate the piece
 	 */
 	@Override
 	public void dropActionChanged(DragSourceDragEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Object getTransferData(DataFlavor flavor)
 			throws UnsupportedFlavorException, IOException {
 		return piece;
 	}
 
 	@Override
 	public DataFlavor[] getTransferDataFlavors() {
 		return new DataFlavor[]{new DataFlavor(Piece.class, "Piece")};
 	}
 
 	@Override
 	public boolean isDataFlavorSupported(DataFlavor flavor) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 }
