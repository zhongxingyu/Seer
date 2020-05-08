 package net.bluecow.spectro.painting;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 import javax.swing.Scrollable;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 import javax.swing.undo.UndoableEdit;
 import javax.swing.undo.UndoableEditSupport;
 
 import net.bluecow.spectro.PlayerThread;
 import net.bluecow.spectro.clipAndFrame.Clip;
 import net.bluecow.spectro.clipAndFrame.Frame;
 import net.bluecow.spectro.detection.BeatDetector;
 
 public class ClipPanel extends JPanel
 implements Scrollable
 {
 	private final Clip clip;
 	private final BufferedImage img;
 	private final int[] imgPixels;
 	private Rectangle region;
 	private Rectangle oldRegion;
 	private boolean regionMode;
 	private final RegionMouseHandler mouseHandler = new RegionMouseHandler();
 
 	//private final ClipPositionHeader clipPositionHeader = new ClipPositionHeader();
 	private boolean undoing;
 	private ValueColorizer colorizer = new LogarithmicColorizer(this);
 
 	private ClipPlayBackLocation clipPlayLoc;
 
 	//Playback points used for playing back
 	private Point playbackPoint = new Point();
 	private Point offsetPoint = new Point(-50,0);
 
 	private ClipDataChangeListener clipDataChangeHandler = new ClipDataChangeListener()
 	{
 		public void clipDataChanged(ClipDataChangeEvent e)
 		{
 			Rectangle r = ClipPanel.this.toScreenCoords(e.getRegion());
 			ClipPanel.this.updateImage(r);
 			ClipPanel.this.repaint(r);
 		}
 	};
 
 	private final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
 
 	public static ClipPanel newInstance(Clip clip, PlayerThread playerThread)
 	{
 		ClipPanel cp = new ClipPanel(clip);
 		clip.addClipDataChangeListener(cp.clipDataChangeHandler);
 		//playerThread.addPlaybackPositionListener(cp.clipPositionHeader);
 		//cp.clipPositionHeader.setPlayerThread(playerThread);
 		return cp;
 	}
 
 	public ClipPanel(Clip clip) {
 		this.clip = clip;
 		System.out.println("frame count "+clip.getFrameCount());
 		int width = clip.getFrameCount()*( (int)Math.pow( 2.0, 7 ) );
 		setPreferredSize(new Dimension( width, 128+600));
 		this.img = new BufferedImage( width, 128, 1);
 		this.imgPixels = ((DataBufferInt)this.img.getRaster().getDataBuffer()).getData();
 		updateImage(null);
 		setBackground(Color.white);
 	}
 	public Point toClipCoords(Point p)
 	{
 		p.y = (this.clip.getFrameFreqSamples() - p.y);
 		return p;
 	}
 
 	public Rectangle toClipCoords(Rectangle r)
 	{
 		r.y = (this.clip.getFrameFreqSamples() - (r.y + r.height));
 		return r;
 	}
 	public Point toScreenCoords(Point p)
 	{
 		p.y = (this.clip.getFrameFreqSamples() - p.y);
 		return p;
 	}
 	public Rectangle toScreenCoords(Rectangle r)
 	{
 		return toClipCoords(r);
 	}
 
 	public ValueColorizer getColorizer()
 	{
 		return this.colorizer;
 	}
 
 	void updateImage(Rectangle region)
 	{
 		if (this.clip == null) return;
 
 		int width = clip.getFrameCount()*( (int)Math.pow( 2.0, 7 ) );
 
 		if (region == null)
 			region = new Rectangle(0, 0, width, 128);
 		else
 		{
 			region = new Rectangle(region);
 		}
 		toClipCoords(region);
 		int endCol = region.x + region.width;
 		int endRow = region.y + region.height;
 
 		int k = 0;
 		for( int row = region.y; row < endRow; row++ )
 		{
 			//if( row%2 == 1 ) continue;
 			int rowIndex = (region.height - (row - region.y) - 1);///2;
 			int noteNumber = rowIndex%12;
 			int octave = (rowIndex)/12;
 			ArrayList< Frame > frames = clip.getFrame( octave );
 			for( int col = region.x; col < endCol; col++ )
 			{
 				int columnIndex = col - region.x;
 				int frameNumber = columnIndex / ( (int)(Math.pow(2.0, 7.0 )) );
 				Frame f = frames.get( frameNumber );
 				double noteData[] = f.getNoteDataFor( noteNumber );
 				int colInFrame = ( columnIndex % ( (int)(Math.pow(2.0, 7.0 ))));
 				int divide = (int)(Math.pow(2.0, 7.0 )) / noteData.length ;
 				int noteColNumber = colInFrame / divide;
 				double noteValue = noteData[noteColNumber];
 				this.imgPixels[ (columnIndex + /*2**/rowIndex * this.img.getWidth())] = this.colorizer.colorFor( noteValue );
 			}
 		}
 	}
 	protected void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D)g;
 		AffineTransform backupTransform = g2.getTransform();
 		g2.translate(0, this.img.getHeight());
 		g2.scale(1.0D, -1.0D);
 
 		Rectangle clipBounds = g2.getClipBounds();
 
 		if (clipBounds.x + clipBounds.width > this.img.getWidth())
 		{
 			clipBounds.width = (this.img.getWidth() - clipBounds.x);
 		}
 
 		if (clipBounds != null)
 		{
 			g2.drawImage(this.img, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height, Color.BLACK, null);
 		}
 		else
 		{
 			g2.drawImage(this.img, 0, 0, null);
 		}
 
 		g2.setTransform(backupTransform);
 
 		int startY = this.img.getHeight()+100;
 
		//clip.detectors[0].draw(g2, startY+300, 400);

 		for(BeatDetector beat:clip.detectors)
 		{
 			beat.draw(g2, startY, 200);
 			startY+=100;
 		}

 	//	g2.setColor(Color.red);
 	//	g2.fillRect(0, 0, length, 1000);
 
 		if(clipPlayLoc!=null)
 		{
 			g2.setColor(Color.red);
 			int position = (int)clipPlayLoc.clipPosition;
 			int myX = -(int)offsetPoint.getX();
 			if(position<myX)
 			{
 				myX = position;
 			}
 			int x = position-(int)offsetPoint.getX();
 			g2.drawLine(x, 0, x, clipBounds.height);
 			g2.setColor(Color.white);
 			double loc = clipPlayLoc.samplePosition;
 			loc /= 44100.0*2.;
 			g2.drawString("Sample: "+loc+" seconds",x,img.getHeight());
 		}
 
 
 		if (this.region != null)
 		{
 			g2.setColor(Color.YELLOW);
 			g2.drawRect(this.region.x, this.region.y, this.region.width, this.region.height);
 		}
 	}
 
 	public Clip getClip()
 	{
 		return this.clip;
 	}
 
 	private void repaintRegion()
 	{
 		Rectangle newRegion = this.region == null ? null : new Rectangle(this.region);
 		if ((this.oldRegion != null) && (newRegion == null))
 		{
 			repaint(this.oldRegion.x, this.oldRegion.y, this.oldRegion.width + 1, this.oldRegion.height + 1);
 		}
 		else if ((this.oldRegion == null) && (newRegion != null))
 		{
 			repaint(newRegion.x, newRegion.y, newRegion.width + 1, newRegion.height + 1);
 		} else if ((this.oldRegion != null) && (newRegion != null))
 		{
 			this.oldRegion.add(newRegion);
 			repaint(this.oldRegion.x, this.oldRegion.y, this.oldRegion.width + 1, this.oldRegion.height + 1);
 		}
 		this.oldRegion = newRegion;
 	}
 	public void setRegionMode(boolean on)
 	{
 		if (on != this.regionMode)
 		{
 			if (on)
 			{
 				this.regionMode = true;
 				addMouseListener(this.mouseHandler);
 				addMouseMotionListener(this.mouseHandler);
 			} else
 			{
 				this.regionMode = false;
 				setRegion(null);
 				this.oldRegion = null;
 				removeMouseListener(this.mouseHandler);
 				removeMouseMotionListener(this.mouseHandler);
 			}
 		}
 	}
 
 	public Rectangle getRegion()
 	{
 		if (this.region == null)
 		{
 			return null;
 		}
 		return new Rectangle(this.region);
 	}
 
 	private void setRegion(Rectangle r)
 	{
 		Rectangle oldRegion = this.region;
 		this.region = normalized(r);
 		repaintRegion();
 		firePropertyChange("region", oldRegion, this.region);
 		if (!this.undoing)
 			this.undoSupport.postEdit(new RegionMoveEdit(oldRegion));
 		}
 
 	private Rectangle normalized(Rectangle rect)
 	{
 		if (rect == null) return null;
 
 		rect = new Rectangle(rect);
 
 		if (rect.width < 0)
 		{
 			rect.x += rect.width;
 			rect.width *= -1;
 		}
 
 		if (rect.height < 0)
 		{
 			rect.y += rect.height;
 			rect.height *= -1;
 		}
 		return rect;
 	}
 
 	public void addNotify()
 	{
 
 		super.addNotify();
 		Component p = getParent();
 		if (((p instanceof JViewport)) && ((p.getParent() instanceof JScrollPane))) {
 			JScrollPane sp = (JScrollPane)p.getParent();
 		//	sp.setColumnHeaderView(this.clipPositionHeader);
 			}
 		}
 
 	public Dimension getPreferredScrollableViewportSize()
 	{
 		return getPreferredSize();
 	}
 
 	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
 	{
 		return (int)(visibleRect.width * 0.9D);
 	}
 
 	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
 	{
 		return 50;
 	}
 
 	@Override
 	public boolean getScrollableTracksViewportHeight()
 	{
 		return false;
 	}
 
 	public boolean getScrollableTracksViewportWidth()
 	{
 		return false;
 	}
 
 	public void addUndoableEditListener(UndoableEditListener l)
 	{
 		this.undoSupport.addUndoableEditListener(l);
 	}
 
 	public void removeUndoableEditListener(UndoableEditListener l)
 	{
 		this.undoSupport.removeUndoableEditListener(l);
 	}
 
 	private class ClipPositionHeader extends JPanel implements PlaybackPositionListener
 	{
 		private PlayerThread playerThread;
 		volatile long playbackPosition;
 		private final MouseListener repositionHandler = new MouseAdapter()
 		{
 			public void mousePressed(MouseEvent e)
 			{
 				ClipPanel.ClipPositionHeader.this.playerThread.setPlaybackPosition(e.getX() * ClipPanel.this.clip.getFrameTimeSamples());
 				}
 		};
 
 		public ClipPositionHeader()
 		{
 			setPreferredSize(new Dimension(1, 20));
 			addMouseListener(this.repositionHandler);
 		}
 
 		public void setPlayerThread(PlayerThread playerThread)
 		{
 			this.playerThread = playerThread;
 		}
 
 		public void playbackPositionUpdate(PlaybackPositionEvent e)
 		{
 			int oldPixelPosition = playbackPixelPosition();
 			this.playbackPosition = e.getSamplePos();
 			int newPixelPosition = playbackPixelPosition();
 			if (newPixelPosition >= oldPixelPosition)
 				repaint(oldPixelPosition, 0, newPixelPosition - oldPixelPosition + 1, getHeight());
 			else
 				repaint(newPixelPosition, 0, oldPixelPosition - newPixelPosition + 1, getHeight());
 		}
 		protected void paintComponent(Graphics g)
 		{
 			super.paintComponent(g);
 			g.setColor(Color.BLACK);
 			g.drawRect(playbackPixelPosition(), 0, 1, getHeight());
 		}
 		private int playbackPixelPosition()
 		{
 			return (int)(this.playbackPosition / ClipPanel.this.clip.getFrameTimeSamples());
 		}
 	}
 
 	private final class RegionMoveEdit extends AbstractUndoableEdit
 	{
 		private Rectangle oldr;
 		private final Rectangle newr = ClipPanel.this.region;
 		private RegionMoveEdit(Rectangle oldRegion)
 		{
 			this.oldr = oldRegion;
 		}
 		public void undo() throws CannotUndoException
 		{
 			super.undo();
 			ClipPanel.this.undoing = true;
 			ClipPanel.this.setRegion(this.oldr);
 			ClipPanel.this.undoing = false;
 		}
 
 		public void redo() throws CannotRedoException
 		{
 			super.redo();
 			ClipPanel.this.undoing = true;
 			ClipPanel.this.setRegion(this.newr);
 			ClipPanel.this.undoing = false;
 		}
 		public boolean isSignificant()
 		{
 			return false;
 		}
 
 		public boolean replaceEdit(UndoableEdit anEdit)
 		{
 			if ((anEdit instanceof RegionMoveEdit))
 			{
 				RegionMoveEdit replaceMe = (RegionMoveEdit)anEdit;
 				this.oldr = replaceMe.oldr;
 				replaceMe.die();
 				return true;
 			}
 			return false;
 		}
 
 		public String toString()
 		{
 			return "Region move: " + this.oldr + " -> " + this.newr;
 			}
 		}
 	private class RegionMouseHandler
 	implements MouseMotionListener, MouseListener
 	{
 		ClipPanel.MouseMode mode = ClipPanel.MouseMode.IDLE;
 		Point moveHandle;
 		Rectangle tempRegion;
 
 		private RegionMouseHandler()
 		{
 
 		}
 
 		public void mouseDragged(MouseEvent e)
 		{
 
 			/*
        switch (ClipPanel.MouseMode.values()[this.mode.ordinal()]) {
 	       case 1:
 	         startRect(e.getPoint());
 	         break;
 	       case 2:
 	         resizeRect(e.getPoint());
 	         break;
 	       case 3:
 	         moveRect(e.getPoint());
        }
 
        ClipPanel.this.setRegion(this.tempRegion);
        */
 
     }
 
 		public void mousePressed(MouseEvent e)
 		{
 			this.tempRegion = ClipPanel.this.normalized(ClipPanel.this.region);
 			Point p = e.getPoint();
 			if ((this.tempRegion != null) && (this.tempRegion.contains(p)))
 			{
 				this.mode = ClipPanel.MouseMode.MOVING;
 				this.moveHandle = new Point(p.x - this.tempRegion.x, p.y - this.tempRegion.y);
 				}else
 				{
 					startRect(p);
 					this.mode = ClipPanel.MouseMode.SIZING;
 				}
 			ClipPanel.this.setRegion(this.tempRegion);
 			}
 		public void mouseReleased(MouseEvent e)
 		{
 			this.mode = ClipPanel.MouseMode.IDLE;
 			ClipPanel.this.setRegion(this.tempRegion);
 			this.tempRegion = null;
 		}
 
 		public void mouseMoved(MouseEvent e)
 		{
 
 		}
 
 		public void mouseClicked(MouseEvent e)
 		{
 
 		}
 
 		public void mouseEntered(MouseEvent e)
 		{
 
 		}
 
 		public void mouseExited(MouseEvent e)
 		{
 
 		}
 		private void startRect(Point p)
 		{
 			this.tempRegion = new Rectangle(p.x, p.y, 0, 0);
 		}
 
 		private void resizeRect(Point p)
 		{
 			this.tempRegion.width = (p.x - this.tempRegion.x);
 			this.tempRegion.height = (p.y - this.tempRegion.y);
 		}
 
 		private void moveRect(Point p)
 		{
 			this.tempRegion.x = (p.x - this.moveHandle.x);
 			this.tempRegion.y = (p.y - this.moveHandle.y);
 		}
 
 	}
 
 	static enum MouseMode
 	{
 		IDLE, SIZING, MOVING;
 	}
 
 	public void setSpectralToScreenMultiplier(double d)
 	{
 	}
 
 	public Point getOffsetPoint() {
 		return offsetPoint;
 	}
 
 	public void setOffsetPoint(Point offsetPoint) {
 		this.offsetPoint = offsetPoint;
 	}
 
 	public Point getPlaybackPoint() {
 		return playbackPoint;
 	}
 
 	public void setPlaybackPoint(Point playbackPoint) {
 		this.playbackPoint = playbackPoint;
 	}
 
 	public int getTotalSamples() {
 		return clip.getFrameCount()*clip.getFrameFreqSamples();
 	}
 
 	public ClipPlayBackLocation getClipPlayLoc() {
 		return clipPlayLoc;
 	}
 
 	public void setClipPlayLoc(ClipPlayBackLocation clipPlayLoc) {
 		this.clipPlayLoc = clipPlayLoc;
 	}
 }
