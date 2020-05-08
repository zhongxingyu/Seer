 package de.thomasvoecking.screenruler.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JFrame;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.sun.awt.AWTUtilities;
 
 import de.thomasvoecking.screenruler.ui.buttons.CloseButton;
 
 /**
  * The main frame. All components are directly painted on this component.
  * Also controls all dragging events.
  * 
  * @author thomas
  */
 public class ScreenrulerFrame extends JFrame implements MouseListener, MouseMotionListener
 {
 
 	/**
 	 * SUID
 	 */
 	private static final long serialVersionUID = 2158183440540339826L;
 
 	/**
 	 * The logger
 	 */
 	private static final Log log = LogFactory.getLog(ScreenrulerFrame.class);
 
 	/**
 	 * The configuration
 	 */
 	private final Configuration configuration;
 	
 	/**
 	 * The padding between the ruler and the resize controls.
 	 */
 	private static final int rulerPadding = 10;
 	
 	/**
 	 * The overlay color
 	 */
 	private static final Color overlayColor = new Color(255, 245, 173);
 	
 	/**
 	 * The left resize control
 	 */
 	private final ScreenrulerResizeControl leftResizeControl = ScreenrulerResizeControl.LEFT;
 	
 	/**
 	 * The right resize control
 	 */
 	private final ScreenrulerResizeControl rightResizeControl = ScreenrulerResizeControl.RIGHT;
 	
 	/** 
 	 * The ruler
 	 */
 	private final Ruler ruler = new Ruler("cm", 48, 129);
 	
 	/**
 	 * The close button
 	 */
 	private final CloseButton closeButton = new CloseButton();
 	
 	/**
 	 * Contains stateful data that is necessary for the dragging behaviour.
 	 */
 	private ScreenrulerDraggingData screenrulerDraggingData = new ScreenrulerDraggingData();
 
 	/**
 	 * Constructor
 	 * 
 	 * @param configuration The configuration 
 	 */
 	public ScreenrulerFrame(final Configuration configuration) 
 	{
 		log.debug("Initializing frame.");
 		
 		this.configuration = configuration;
 		
 		// Set window properties
 		this.setUndecorated(true);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		final Dimension size = new Dimension(
 				this.configuration.getInt("screenrulerFrame.size[@width]"), 
 				this.configuration.getInt("screenrulerFrame.size[@height]"));
 		log.debug("Setting size: " + size); 
 		this.setSize(size);
 		this.setResizable(false);
 
 		log.debug("Setting opacity: " + this.configuration.getFloat("screenrulerFrame.window[@opacity]"));
 		AWTUtilities.setWindowOpacity(this, this.configuration.getFloat("screenrulerFrame.window[@opacity]"));
 		
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 	}
 	
 	/**
 	 * @see java.awt.Window#paint(java.awt.Graphics)
 	 */
 	@Override
 	public void paint(final Graphics g) 
 	{
 		
 		final Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);		
 		g.clearRect(0, 0, this.getWidth(), this.getHeight());
 		
 		// When the ruler is currently in resizing state, draw no ruler and no resize controls.
 		// Instead draw an overlay.
 		final boolean paintOverlay = 
 			this.screenrulerDraggingData.draggingMode == DraggingMode.RESIZE_LEFT || this.screenrulerDraggingData.draggingMode == DraggingMode.RESIZE_RIGHT; 
 		
 		if (paintOverlay)
 		{
 			this.paintOverlay(g);
 		}
 		else
 		{
 			this.paintArrows(g);
 			this.ruler.paint(g, new Rectangle(
					this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]") + rulerPadding, 
 					0, 
 					this.getWidth() - 2 * this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]") - 2 * rulerPadding, 
 					this.getHeight()));
 			this.closeButton.paint(g, this.getCloseButtonBoundingBox());
 		}
 	}
 	
 	/**
 	 * Paints the overlay.
 	 * 
 	 * @param g The {@link Graphics} Object.
 	 */
 	private void paintOverlay(final Graphics g)
 	{
 		final Color color = g.getColor();
 		g.setColor(overlayColor);
 		g.fillRect(0, 0, this.getWidth() + 100, this.getHeight());
 		g.setColor(color);
 		g.drawLine(
 				this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]") + rulerPadding, 0, 
 				this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]") + rulerPadding, (int) (this.getHeight() / 2.0));
 		g.drawLine(
 				this.getWidth() - this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]") - rulerPadding, 0, 
 				this.getWidth() - this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]") - rulerPadding, (int) (this.getHeight() / 2.0));
 	}
 	
 	/**
 	 * Paints the resize controls.
 	 * 
 	 * @param g The {@link Graphics} Object.
 	 */
 	private void paintArrows(final Graphics g)
 	{
 		this.leftResizeControl.paint(g, this.getLeftResizeControlBoundingBox());
 		this.rightResizeControl.paint(g, this.getRightResizeControlBoundingBox());
 	}
 	
 	
 	/**
 	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mousePressed(final MouseEvent e) 
 	{
 		if (e.getButton() == MouseEvent.BUTTON1)
 		{
 			this.screenrulerDraggingData.componentRelativeMouseLocationFromLeft = e.getPoint();
 			this.screenrulerDraggingData.componentRelativeMouseLocationFromRight = new Point(
 					(int) (this.getWidth() - e.getPoint().getX()), 
 					(int) (this.getHeight() - e.getPoint().getY()));
 			this.screenrulerDraggingData.bottomRight = new Point(
 					(int) (this.getLocationOnScreen().getX() + this.getWidth()),
 					(int) (this.getLocationOnScreen().getY() + this.getHeight()));
 			
 			if (this.getLeftResizeControlBoundingBox().contains(e.getPoint()))
 				this.screenrulerDraggingData.draggingMode = DraggingMode.RESIZE_LEFT;
 			else if (this.getRightResizeControlBoundingBox().contains(e.getPoint()))
 				this.screenrulerDraggingData.draggingMode = DraggingMode.RESIZE_RIGHT;
 			else
 				this.screenrulerDraggingData.draggingMode = DraggingMode.MOVE;
 			
 			log.debug("New dragging mode: " + this.screenrulerDraggingData.draggingMode);
 		}
 	}
 
 	/**
 	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseDragged(final MouseEvent e) 
 	{
 		if (this.screenrulerDraggingData.draggingMode == DraggingMode.RESIZE_LEFT)
 		{
 			final Rectangle newBounds = new Rectangle(
 					(int) (e.getLocationOnScreen().getX() - this.screenrulerDraggingData.componentRelativeMouseLocationFromLeft.getX()), 
 					(int) (this.getLocationOnScreen().getY()), 
 					(int) (this.screenrulerDraggingData.bottomRight.getX() - e.getLocationOnScreen().getX() - this.screenrulerDraggingData.componentRelativeMouseLocationFromLeft.getX()), 
 					this.getHeight());
 			if (newBounds.getWidth() >= this.configuration.getInt("screenrulerFrame.size[@minWidth]")) this.setBounds(newBounds);
 		}
 		else if (this.screenrulerDraggingData.draggingMode == DraggingMode.RESIZE_RIGHT)
 		{
 			final Dimension newSize = new Dimension(
 					(int) (e.getLocationOnScreen().getX() - this.getLocationOnScreen().getX() + this.screenrulerDraggingData.componentRelativeMouseLocationFromRight.getX()), 
 					this.getHeight());
 			if (newSize.getWidth() >= this.configuration.getInt("screenrulerFrame.size[@minWidth]")) this.setSize(newSize);
 		}
 		else if (this.screenrulerDraggingData.draggingMode == DraggingMode.MOVE)
 		{
 			this.setLocation(
 					(int) (e.getLocationOnScreen().getX() - this.screenrulerDraggingData.componentRelativeMouseLocationFromLeft.getX()),
 					(int) (e.getLocationOnScreen().getY() - this.screenrulerDraggingData.componentRelativeMouseLocationFromLeft.getY()));
 		}
 	}	
 
 	/**
 	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseReleased(final MouseEvent e) 
 	{
 		if (this.screenrulerDraggingData.draggingMode != null)
 		{
 			log.debug("Releasing dragging mode: " + this.screenrulerDraggingData.draggingMode);
 			this.screenrulerDraggingData.draggingMode = null;
 			this.repaint();
 		}
 	}
 
 	/**
 	 * @return the bounding box for the left resize control.
 	 */
 	private Rectangle getLeftResizeControlBoundingBox()
 	{
 		return new Rectangle(0, 0, 
 				this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]"), 
 				this.configuration.getInt("screenrulerFrame.resizeControl.size[@height]"));
 	}
 
 	/**
 	 * @return the bounding box for the right resize control.
 	 */
 	private Rectangle getRightResizeControlBoundingBox()
 	{
 		return new Rectangle(
 				this.getWidth() - this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]"), 
 				0, 
 				this.configuration.getInt("screenrulerFrame.resizeControl.size[@width]"), 
 				this.configuration.getInt("screenrulerFrame.resizeControl.size[@height]"));
 	}
 
 	/**
 	 * @return the bounding box where the close button should be drawn to.
 	 */
 	private Rectangle getCloseButtonBoundingBox()
 	{
 		return new Rectangle(4, this.getHeight() - 20, 16, 16);
 	}
 
 	/**
 	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseMoved(final MouseEvent e) 
 	{
 		// Check if we are over the close button
 		final Rectangle closeButtonBoundingBox = this.getCloseButtonBoundingBox();
 		if (this.closeButton.setMouseOver(closeButtonBoundingBox.contains(e.getPoint()))) this.repaint(
 				(int) closeButtonBoundingBox.getX(), (int) closeButtonBoundingBox.getY(), 
 				(int) closeButtonBoundingBox.getWidth(), (int) closeButtonBoundingBox.getHeight());
 	}
 
 	
 	
 	/**
 	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseClicked(final MouseEvent e) 
 	{
 		// Check if we have clicked the exit button
 		if (this.getCloseButtonBoundingBox().contains(e.getPoint())) System.exit(0);
 		
 	}
 
 	/**
 	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseEntered(final MouseEvent e) { /* unused */ }
 
 	/**
 	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseExited(final MouseEvent e) { /* unused */ }
 
 }
