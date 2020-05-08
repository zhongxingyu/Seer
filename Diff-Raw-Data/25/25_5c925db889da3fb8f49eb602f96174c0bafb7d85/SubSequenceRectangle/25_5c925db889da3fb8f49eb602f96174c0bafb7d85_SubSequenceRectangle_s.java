 package ViewCanvas;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 
 import javax.swing.event.MouseInputListener;
 
 import Engine.LAP;
 
 import com.clcbio.api.clc.graphics.framework.ChildDrawingNode;
 import com.clcbio.api.clc.graphics.framework.DrawingLayer;
 import com.clcbio.api.clc.graphics.framework.DrawingResult;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.alignment.AlignmentSequenceIndexer;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.index.BasicIndexer;
 import com.clcbio.api.free.editors.framework.MouseMode;
 
 public class SubSequenceRectangle extends ChildDrawingNode implements MouseInputListener {
 
 	private boolean dragging;
 	private Point startDragPoint;
 	private Point curDragPoint;
 	private boolean hasMouseListener;
 	private LAP lap;
 	private AlignmentSequenceIndexer indexer;
 	private boolean valid = false;
 	private boolean finished = false;
 	private boolean drawn = false;
 
 	private double xStart;
 	private double xEnd;
 	private boolean startHover;
 	private boolean endHover;
 	private boolean startDragged = false;
 	private long prevClick;
 	
 	public SubSequenceRectangle(LAP lap){
 		this.lap = lap;
 		this.prevClick = 0;
 		
 	}
 	
 	protected DrawingResult internalDraw(Graphics2D g2, boolean drawoutline, DrawingLayer drawinglayer, double minx, double maxx, double miny, double maxy){
 		if(!hasMouseListener){
 			this.addMouseInputListener(this);
 			hasMouseListener = true;
 		}
 		int startX;
 		int startY;
 		if(dragging && lap.getEditor().getMouseMode() == MouseMode.SELECT_MODE){
 			startX = startDragPoint.getX() > curDragPoint.getX() ? (int)(lap.getXViewBounds()+curDragPoint.getX()) : (int)(lap.getXViewBounds()+startDragPoint.getX());
 			startY = startDragPoint.getY() > curDragPoint.getY() ? (int)(lap.getYViewBounds()+curDragPoint.getY()) : (int)(lap.getYViewBounds()+startDragPoint.getY());
 			xStart = startX;
 			xEnd = startX+(Math.abs((int)(curDragPoint.getX()-startDragPoint.getX())));
 			
 			g2.drawRect(startX,startY,Math.abs((int)(curDragPoint.getX()-startDragPoint.getX())),Math.abs((int)(curDragPoint.getY()-startDragPoint.getY())));
 		}
 		if(finished){
 			 startX = startDragPoint.getX() > curDragPoint.getX() ? (int)(lap.getXViewBounds()+curDragPoint.getX()) : (int)(lap.getXViewBounds()+startDragPoint.getX());
 			 startY = startDragPoint.getY() > curDragPoint.getY() ? (int)(lap.getYViewBounds()+curDragPoint.getY()) : (int)(lap.getYViewBounds()+startDragPoint.getY());
 			 xStart = startX > 0 ? startX : 0;
 			 xEnd = startX+(Math.abs((int)(curDragPoint.getX()-startDragPoint.getX())));
 			 Color col;
 			 col = !valid ? new Color(Color.RED.getRed(),Color.RED.getGreen(),Color.RED.getBlue(),140) : new Color(Color.BLUE.getRed(),Color.BLUE.getGreen(),Color.BLUE.getBlue(),140);
 			 g2.setColor(col);
 			 g2.fillRect(startX,startY,Math.abs((int)(curDragPoint.getX()-startDragPoint.getX())),Math.abs((int)(curDragPoint.getY()-startDragPoint.getY())));
 			 drawn = true;
 		}
 		System.out.println(xStart + " xStart");
 		System.out.println();
 		return DrawingResult.NORMAL;		
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if(lap.getXViewBounds()+e.getPoint().getX() <xEnd && lap.getXViewBounds()+e.getPoint().getX() > xStart){
 			if(e.getWhen() - prevClick < 500){
 				double newXStart;		
 				double newXEnd;		
 				newXStart = xStart/lap.getScaleX();
 				newXEnd = xEnd/lap.getScaleX();
 				
 				int XNucleotideStart = (int)Math.round(newXStart);
 				int XNucleotideEnd = (int)Math.round(newXEnd);
 				
 				indexer = new AlignmentSequenceIndexer(lap.getAlign(), lap.getCurrentSequenceNumber());
 				
 				int seqPositionStart;
 				
 				if(XNucleotideStart < lap.getAlign().getLength()){
 					seqPositionStart = XNucleotideStart >= 0 ? indexer.getSequencePosition(XNucleotideStart) : 0;
 					seqPositionStart = seqPositionStart < 0 ? 0 : seqPositionStart;
 				} else {
 					 this.repaint();
 					 return;
 				}
 				
 				int seqPositionEnd;
 				
 				if(XNucleotideEnd > 0){
 					seqPositionEnd = XNucleotideEnd <= lap.getAlign().getLength() ? indexer.getSequencePosition(XNucleotideEnd) : lap.getAlign().getLength();
 					seqPositionEnd = seqPositionEnd < 0 ? lap.getSequence().getLength() : seqPositionEnd;
 				}else {
 					 this.repaint();
 					 return;
 				}
 				
 				
 				
 				
 				if (validRect(seqPositionStart, seqPositionEnd)) valid = true;
 				else valid = false;
 				
 				lap.showSub(seqPositionStart, seqPositionEnd);
 			} else {
 				prevClick = e.getWhen();
 			}
 		} else {
 			finished = false;
 			repaint();
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		//int lap.getXViewBounds() = lap.getXViewBounds() < 0 ? 0 : lap.getXViewBounds();
 		System.out.println("xStart: " + xStart + " e.getPoint().getX(): " + e.getPoint().getX() + " xEnd: " + xEnd + " e + viewbounds: " + (e.getPoint().getX()+(int)lap.getXViewBounds()));
 		if(drawn){
 			if(xEnd > (lap.getXViewBounds()+e.getPoint().getX()) && xStart < (lap.getXViewBounds()+e.getPoint().getX())) {
 				if(xEnd - (lap.getXViewBounds()+e.getPoint().getX()) < (lap.getXViewBounds()+e.getPoint().getX())- xStart){
 					System.out.println("end closest");
 					//xEnd -= (xEnd-(e.getPoint().getX()+lap.getXViewBounds()));
 					//curDragPoint.setLocation(xEnd, startDragPoint.getY());
 					System.out.println(startDragPoint);
 					startDragPoint.setLocation(xStart-lap.getXViewBounds(), startDragPoint.getY());
 					System.out.println(xStart + " is startdrag point in mousepressed");
 				} else {
 					System.out.println("start closest");
 					//xStart += ((e.getPoint().getX()+lap.getXViewBounds())-xStart);
 					//curDragPoint.setLocation(xStart, startDragPoint.getY());
 					System.out.println(startDragPoint);
 					System.out.println(xEnd + " is x end in mousepressed");
 					startDragPoint.setLocation(xEnd-lap.getXViewBounds(), startDragPoint.getY());
 					System.out.println(startDragPoint.getX() + " after being set");
 					startDragged = true;
 				}
 			} else {
 				System.out.println("drawn but not inside");
 				startDragPoint = e.getPoint();
 				curDragPoint = startDragPoint;
 			}
 		} else {		
 		startDragPoint = e.getPoint();
 		curDragPoint = startDragPoint;
 		}
 		drawn = false;
 		dragging = true;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if(Math.abs(curDragPoint.getX() - startDragPoint.getX()) < 10 || Math.abs(curDragPoint.getY() - startDragPoint.getY()) < 10 ) {
 			dragging = false;
 			return;
 		}
 		
 		startDragged = false;
 		dragging = false;
 		finished = true;
 		
 		double newXStart;		
 		double newXEnd;		
 		newXStart = xStart/lap.getScaleX();
 		newXEnd = xEnd/lap.getScaleX();
 		
 		int XNucleotideStart = (int)Math.round(newXStart);
 		int XNucleotideEnd = (int)Math.round(newXEnd);
 		
 		indexer = new AlignmentSequenceIndexer(lap.getAlign(), lap.getCurrentSequenceNumber());
 		
 		int seqPositionStart;
 		
 		if(XNucleotideStart < lap.getAlign().getLength()){
 			seqPositionStart = XNucleotideStart >= 0 ? indexer.getSequencePosition(XNucleotideStart) : 0;
 			seqPositionStart = seqPositionStart < 0 ? 0 : seqPositionStart;
 		} else {
 			 this.repaint();
 			 return;
 		}
 		
 		int seqPositionEnd;
 		
 		if(XNucleotideEnd > 0){
 			seqPositionEnd = XNucleotideEnd <= lap.getAlign().getLength() ? indexer.getSequencePosition(XNucleotideEnd) : lap.getAlign().getLength();
 			seqPositionEnd = seqPositionEnd < 0 ? lap.getSequence().getLength() : seqPositionEnd;
 		}else {
 			 this.repaint();
 			 return;
 		}
 		
 		System.out.println(lap.getAlign().getLength() + " Align length");
 		
 		System.out.println(lap.getPairings().length + " pairings length");
 		
 		System.out.println(XNucleotideStart + " start NuclX");
 		System.out.println(XNucleotideEnd + " end NuclX");
 		System.out.println(seqPositionStart + " seq start");
 		System.out.println(seqPositionEnd + " seq end");
 		
 		
 		if (validRect(seqPositionStart, seqPositionEnd)) valid = true;
 		else valid = false;
 		repaint();
 		//lap.showSub(seqPositionStart, seqPositionEnd);
 		
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
 	public void mouseDragged(MouseEvent e) {
 		//if(Math.abs(e.getPoint().getX()-curDragPoint.getX()) > 15 || Math.abs(e.getPoint().getY()-curDragPoint.getY()) > 15){
 		
 		
 		curDragPoint = e.getPoint();
 		//if (startDragged) xStart = startDragPoint.getX();
 		
 		this.repaint();
 		//}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public boolean validRect(int start, int end){
 		int c = start;
 		while(c < end){
 			int tmp = lap.getPairings()[c];
 		
 			System.out.println(tmp + " tmp");
 			System.out.println(c + " c");
 			
 			if(tmp > 0 && tmp < c){
 				return false;				
 			} else if( tmp > c){
 				if(tmp > end) return false;
 				else c = tmp;
 			}
 			c++;
 		}
 		return true;
 	}
 	
 	
 }
