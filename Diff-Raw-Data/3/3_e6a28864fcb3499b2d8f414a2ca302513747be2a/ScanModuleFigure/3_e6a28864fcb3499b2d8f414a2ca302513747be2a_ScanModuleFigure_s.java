 package de.ptb.epics.eve.editor.graphical.editparts.figures;
 
 import org.apache.log4j.Logger;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.draw2d.XYAnchor;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Path;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * <code>ScanModuleFigure</code>
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class ScanModuleFigure extends Figure {
 	
 	private static Logger logger = 
 			Logger.getLogger(ScanModuleFigure.class.getName());
 	
 	private int xOffset = 0;
 	private int yOffset = 0;
 	
 	// anchor points for incoming, outgoing and nested modules
 	private XYAnchor targetAnchor;
 	private XYAnchor appendedAnchor;
 	private XYAnchor nestedAnchor;
 	
 	// indicates whether the scan module is selected
 	private boolean active;
 	
 	// indicates whether the scan module contains errors
 	private boolean contains_errors;
 	
 	// the text displayed inside of the scan module figure
 	private String text;
 	
 	// mouse coordinates after mouse button was pressed
 	// used to determine if the figure was moved
 	private int mousePressedX;
 	private int mousePressedY;
 	
 	/**
 	 * Constructs a <code>ScanModuleFigure</code>.
 	 * 
 	 * @param text the text content of the scan module figure
 	 * @param x x coordinate of the desired location
 	 * @param y y coordinate of the desired location
 	 */
 	public ScanModuleFigure(final String text, int x, int y) {
 		this.active = false;
 		this.contains_errors = false;
 		this.setBackgroundColor(ColorConstants.white);
 		//this.setOpaque(true);
 		this.setSize(70, 30);
 		this.setLocation(new Point(x, y));
 
 		this.text = text;
 		//this.setToolTip( new Label("Right click to edit me."));
 		
 		// add listener for mouse button events
 		this.addMouseListener(new ScanModuleFigureMouseListener());
 		
 		// add listener for mouse movement
 		this.addMouseMotionListener(new ScanModuleFigureMouseMotionListener());
 		
 		// set anchor points
 		Rectangle rect = this.getBounds();
 		this.targetAnchor = new XYAnchor(
 				new Point(rect.x, rect.y + rect.height/2));
 		
 		this.appendedAnchor = new XYAnchor(
 				new Point(rect.x + rect.width, rect.y + rect.height/2));
 		this.nestedAnchor = new XYAnchor(
 				new Point(rect.x + rect.width/2, rect.y + rect.height));
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void paint(final Graphics graphics) {
 		super.paint(graphics);
 		
 		graphics.setAntialias(SWT.ON);
 		
 		Display display = PlatformUI.getWorkbench().getDisplay();
 		if(active)
 		{
 			// if it is active (selected) -> change color
 			graphics.setForegroundColor(ColorConstants.titleGradient);
 			graphics.setBackgroundColor(ColorConstants.white);
 		} else {
 			// inactive ones are colorless
 			graphics.setForegroundColor(ColorConstants.white);
 			graphics.setBackgroundColor(ColorConstants.lightGray);
 		}
 		
 		// save the old clipping
 		Rectangle oldClipping = graphics.getClip(new Rectangle());
 
 		Path path = new Path(display);
 		int spline_control_point = 8;
 		
 		path.moveTo(this.bounds.width + this.bounds.x - spline_control_point, 
 					this.bounds.y);
 		path.quadTo(this.bounds.width + this.bounds.x, this.bounds.y, 
 					this.bounds.width + this.bounds.x, this.bounds.y + 
 					spline_control_point);
 		// right
 		path.lineTo(this.bounds.width + this.bounds.x, 
 					this.bounds.y + this.bounds.height - spline_control_point);
 		path.quadTo(this.bounds.width + this.bounds.x, 
 					this.bounds.y + this.bounds.height, 
 					this.bounds.width + this.bounds.x - spline_control_point, 
 					this.bounds.y + this.bounds.height);
 		// bottom
 		path.lineTo(this.bounds.x + spline_control_point, 
 					this.bounds.y + this.bounds.height);
 		path.quadTo(this.bounds.x, this.bounds.y + this.bounds.height, 
 					this.bounds.x, this.bounds.y + this.bounds.height - 
 					spline_control_point);
 		// left
 		path.lineTo(this.bounds.x, this.bounds.y + spline_control_point);
 		path.quadTo(this.bounds.x, this.bounds.y, 
 					this.bounds.x + spline_control_point, this.bounds.y);
 		// top
 		path.close();
 		graphics.setClip(path);
 		graphics.fillGradient(this.bounds, true);
 		
 		// red error bar if module contains errors
 		if(contains_errors)
 		{
 			graphics.setForegroundColor(ColorConstants.white);
 			graphics.setBackgroundColor(ColorConstants.red);
			graphics.fillRectangle(getLocation().x+1, getLocation().y+1, 15, 5);
 		}
 		
 		// draw border
 		graphics.setForegroundColor(ColorConstants.black);
 		graphics.setBackgroundColor(ColorConstants.white);
 		graphics.setLineWidth(2);
 		graphics.drawPath(path);
 		
 		// draw scan module name
 		graphics.drawText(
 				this.text, this.getLocation().x + 5, this.getLocation().y + 8);
 		
 		// restore old clipping
 		graphics.setClip(oldClipping);
 		// free
 		path.dispose();
 		
 		logger.debug("paint Scan Module " + this);
 	}
 
 	/**
 	 * Returns the text shown in the scan module.
 	 * 
 	 * @return the text shown in the scan module
 	 */
 	public String getText() {
 		return text;
 	}
 
 	/**
 	 * Sets the text shown in the scan module.
 	 * 
 	 * @param text the text that should be shown
 	 */
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	/**
 	 * Returns the anchor for appended scan modules.
 	 * 
 	 * @return the <code>XYAnchor</code> for appended scan modules
 	 */
 	public XYAnchor getAppendedAnchor() {
 		return appendedAnchor;
 	}
 
 	/**
 	 * Returns the anchor for nested scan modules.
 	 * 
 	 * @return the <code>XYAnchor</code> for nested scan modules
 	 */
 	public XYAnchor getNestedAnchor() {
 		return nestedAnchor;
 	}
 
 	/**
 	 * Returns the anchor targeting the scan module.
 	 * 
 	 * @return the <code>XYAnchor</code> targeting the scan module
 	 */
 	public XYAnchor getTargetAnchor() {
 		return targetAnchor;
 	}
 
 	/**
 	 * Checks whether the scan module is active (selected).
 	 * 
 	 * @return <code>true</code> if the scan module is active (selected),
 	 * 		   <code>false</code> otherwise
 	 */
 	public boolean isActive() {
 		return active;
 	}
 
 	/**
 	 * Sets whether the scan module is active (selected).
 	 * 
 	 * @param active <code>true</code> to activate (select) the scan module,
 	 * 				 <code>false</code> to deactivate (deselect) it
 	 */
 	public void setActive(boolean active) {
 		this.active = active;
 	}	
 	
 	/**
 	 * 
 	 * @param error
 	 */
 	public void setError(boolean error)
 	{
 		contains_errors = error;
 	}
 	
 	// ***************************** Listener ********************************
 	
 	/**
 	 * <code>MouseListener</code> of <code>scanModuleFigure</code>.
 	 */
 	class ScanModuleFigureMouseListener implements MouseListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseDoubleClicked(MouseEvent me) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mousePressed(MouseEvent me) {
 			xOffset = me.x - getLocation().x ;
 			yOffset = me.y - getLocation().y;
 			
 			// save the location the figure had, when the button was pressed
 			mousePressedX = getLocation().x;
 			mousePressedY = getLocation().y;
 			
 			me.consume();
 			
 			logger.debug("Mouse Pressed : x = " + me.x + " , y = " + me.y );
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseReleased(MouseEvent me) {
 			// if scan module reached bounds, reset it
 			if (me.x - xOffset < 0)
 				me.x = xOffset + 10;
 			if (me.y - yOffset < 0)
 				me.y = yOffset + 10;
 			setLocation(new Point(me.x - xOffset, me.y - yOffset));
 
 			Rectangle newLocation = getBounds();
 
 			targetAnchor.setLocation(new Point(newLocation.x, newLocation.y 
 											   + newLocation.height/2));
 			appendedAnchor.setLocation(new Point(newLocation.x + 
 												 newLocation.width, 
 												 newLocation.y + 
 												 newLocation.height/2));
 			nestedAnchor.setLocation(new Point(newLocation.x + 
 											   newLocation.width/2, 
 											   newLocation.y + 
 											   newLocation.height));
 			xOffset = 0;
 			yOffset = 0;
 			
 			// if the figure has moved since the button was pressed ->
 			// fire event
 			if(getLocation().x != mousePressedX || 
 				getLocation().y != mousePressedY)
 			{
 				fireCoordinateSystemChanged();
 			}
 			
 			logger.debug("Mouse Released : x = " + me.x + " , y = " + me.y );
 		}
 	}
 	
 	/**
 	 * <code>ScanModuleFigureMouseMotionListener</code>.
 	 */
 	class ScanModuleFigureMouseMotionListener implements MouseMotionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseDragged(MouseEvent me) {
 			// (re-)set location of scan module box
 			setLocation(new Point(me.x - xOffset, me.y - yOffset));
 			Rectangle newLocation = getBounds();
 			// (re-)set anchor points for outgoing arrows
 			targetAnchor.setLocation(new Point(newLocation.x, newLocation.y 
 											   + newLocation.height/2));
 			appendedAnchor.setLocation(new Point(newLocation.x + 
 												 newLocation.width, 
 												 newLocation.y + 
 												 newLocation.height/2));
 			nestedAnchor.setLocation(new Point(newLocation.x + 
 											   newLocation.width/2, 
 											   newLocation.y + 
 											   newLocation.height));
 			
 			logger.debug("Mouse Dragged : x = " + me.x + " , y = " + me.y );
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseEntered(MouseEvent me) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseExited(MouseEvent me) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseHover(MouseEvent me) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseMoved(MouseEvent me) {
 		}
 	}
 }
