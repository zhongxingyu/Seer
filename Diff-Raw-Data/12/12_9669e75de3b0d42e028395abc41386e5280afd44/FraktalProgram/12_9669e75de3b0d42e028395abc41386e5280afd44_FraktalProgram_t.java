 package de.otori.engine;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 
 public abstract class FraktalProgram implements Renderable, KeyListener, MouseListener, MouseMotionListener {
 
 	/**
 	 * Indicates zoom level
 	 */
 	protected double zoom;
 	protected Point2F center;
 		
 	protected int winWidth;
 	protected int winHeight;		
 	
 	private enum ProgramState {IDLE, ZOOMING, SHIFTING};		
 	private ProgramState state = ProgramState.IDLE; // For sample zooming implementation
 	
 	public void setWindowSize(int width, int height)
 	{
 		winWidth = width;
 		winHeight = height;
 	}	
 	
 	@Override
 	public void preRendering() {
 		updatePositions();
		//System.out.println(String.format("Debug: Center: (%f,%f) zoom: %f", center.x, center.y, zoom));
 	}
 	
 	@Override
 	public void postRendering() {
 		//Nothing yet...
 		//Feel free to overide HEHE
 	}
 	
 	public void setZoom(double z)
 	{
 		zoom = z;
 	}
 	
 	public void setCenter(Point2F p)
 	{
 		center = new Point2F(p);
 	}
 	
 	private long lastClick = 0, lastClickRight = 0;
 	final static long DOUBLECLICKMAXDIFF = 350;
 	
 	private Point2F zCenterSrc, zCenterDest;	
 	private double zoomStart;
 	private double zoomDest;
 	private long zoomTimeStart;
 	final long ZOOM_DURATION = 400;
 	final double ZOOM_FACTOR = 2.; // to da squareee !! :D
 	/**
 	 * Internal function used for zoom animation.
 	 * @param x
 	 * @param y
 	 * @param zoomIn indicates, whether to zoom in or out.
 	 */
 	private void initZoom(int x, int y, boolean zoomIn)
 	{
 		if(state != ProgramState.IDLE)
 			return;
 		
		System.out.println(String.format("Debug: Center: (%f,%f) zoom: %f", center.x, center.y, zoom));
		System.out.println(String.format("Window size: (%d,%d)", winWidth, winHeight));
		System.out.println("Clicked on (" + x + "," + y + ")");
		
 		state = ProgramState.ZOOMING;
 		
 		zCenterSrc = new Point2F(center);		
 		zCenterDest = Misc.calculatePixelRealCoordinates(x, y, winWidth, winHeight, zoom, center);
		System.out.println(String.format("Center Dest: (%f,%f)", zCenterDest.x, zCenterDest.y));
 		zoomStart = zoom;
 		if(zoomIn)
 			zoomDest = zoom * ZOOM_FACTOR * ZOOM_FACTOR;
 		else
 			zoomDest = zoom / ZOOM_FACTOR / ZOOM_FACTOR;
 		zoomTimeStart = System.currentTimeMillis();
 	}
 		
 	private void updatePositions()
 	{
 		switch (state) {
 		case ZOOMING:
 						
 			long ticksNow = System.currentTimeMillis() - zoomTimeStart;
 			if(ticksNow >= ZOOM_DURATION)
 			{
 				ticksNow = ZOOM_DURATION;
 				state = ProgramState.IDLE;				
 			}
 			
 			double zProgres = ticksNow / (double)ZOOM_DURATION;						
 			zoom = zoomStart + (zoomDest - zoomStart) * zProgres;			
 			center.y = zCenterSrc.y + (zCenterDest.y - zCenterSrc.y) * zProgres;
			center.x = zCenterSrc.x + (zCenterDest.x - zCenterSrc.x) * zProgres;
 			
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		// TODO Auto-generated method stub		
 		switch (e.getButton())
 		{
 		case MouseEvent.BUTTON1:
 			if(System.currentTimeMillis() - lastClick < DOUBLECLICKMAXDIFF)
 			{			
 				initZoom(e.getPoint().x, e.getPoint().y, true);				
 			}
 			else
 			{
 				lastClick = System.currentTimeMillis();
 			}
 			break;
 		case MouseEvent.BUTTON3: 
 			if(System.currentTimeMillis() - lastClickRight < DOUBLECLICKMAXDIFF)
 			{			
 				initZoom(e.getPoint().x, e.getPoint().y, false);
 			}
 			else
 			{
 				lastClickRight = System.currentTimeMillis();
 			}
 			break;
 		}
 	}
 	
 	@Override	
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private Point2F centerPressed = null, centerStart = null;
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		centerPressed = Misc.calculatePixelRealCoordinates(e.getPoint().x, e.getPoint().y, winWidth, winHeight, zoom, center);
 		centerStart = new Point2F(center);
 	}
 	
 	@Override	
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		centerPressed = null;
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 		if(centerPressed != null && state == ProgramState.IDLE)
 		{			
 			Point2F curPos = Misc.calculatePixelRealCoordinates(e.getPoint().x, e.getPoint().y, winWidth, winHeight, zoom, center);
 			
 			double deltaReal = (curPos.x - centerPressed.x) / 1.25;
 			double deltaImag = (curPos.y - centerPressed.y) / 1.25;
 			
 			center.x = centerStart.x - deltaReal;
 			center.y = centerStart.y - deltaImag;
 		}
 	}
 	
 	@Override	
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}	
 	
 }
