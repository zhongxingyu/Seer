 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JPanel;
 
 import view.framework.G2DAbstractCanvas;
 import view.framework.G2DObject;
 import controller.BuildController;
 import controller.GraphicsController;
 import controller.IController;
 
 public class ViewCanvas extends JPanel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	private final Dimension windowSize 		= new Dimension(1000, 800);
 	private final Dimension canvasSize 		= new Dimension(1000, 1000);
 	private final Dimension gridSize   		= new Dimension(20, 20);
 	private boolean runningMode;
 	
 	private IController eventListener;
 	private GraphicsController graphics;
 	private BuildController buildCont;
 	
 	private G2DAbstractCanvas abstractCanvas;
 
 	public ViewCanvas() {
 		super();
 		setPreferredSize(this.windowSize);
 		abstractCanvas 	= new G2DAbstractCanvas(canvasSize.getWidth(), canvasSize.getHeight());
 		/*
 		 * Add event listener to key presses.
 		 * 
 		 * Start the timer for Action performed.
 		 * 
 		 * Request window focus.
 		 */
 		setFocusable(true);
 		requestFocus();
 		setVisible(true);
 	}
 
 	
 	public int mouseX(int x){
 		return (int) (abstractCanvas.abstractX(x) / (canvasSize.getWidth() / gridSize.getWidth()));
 	}
 	
 	public int mouseY(int y){
 		return (int) (abstractCanvas.abstractY(y) / (canvasSize.getWidth() / gridSize.getWidth()));
 	}
 	
 	public void addController(IController ic, GraphicsController gc, BuildController bc){
 		eventListener = ic;
 		graphics = gc;
 		buildCont = bc;
 		addKeyListener(eventListener);
 		addMouseListener(bc);
 	}
 	
 	public void setMode(boolean running){
 		runningMode = running;
 		if(running){
 			eventListener.start();
 			removeKeyListener(buildCont);
 			addKeyListener(eventListener);
 			requestFocus();
 			requestFocusInWindow();
 		}else{
 			eventListener.stop();
 			removeKeyListener(eventListener);
 			addKeyListener(buildCont);
 			requestFocus();
 			requestFocusInWindow();
 		}
 	}
 	
 	public Dimension getGridSize(){
 		return gridSize;
 	}
 	
 	public Dimension getCanvasSize(){
 		return canvasSize;
 	}
 	
 	private Image bufferImage;
 	
 	@Override 
 	public void paint(Graphics g)
 	{
 		super.paint(g);
 		
 		bufferImage = createImage(getWidth(), getHeight());
 		
 		Graphics buffer = bufferImage.getGraphics();
 		abstractCanvas.setPhysicalDisplay(getWidth(), getHeight(), buffer);
 		
 		buffer.clearRect(0, 0, getWidth(), getHeight());
 		buffer.setColor(Color.BLACK);
 		buffer.fillRect(0, 0, getWidth(), getHeight());
 		
 		if(!runningMode){
 			graphics.factoryDraw(abstractCanvas, 20, 20, (double)50, (double)50);
 		}
 		
 		for(String gizmo : eventListener.getGizmos()){
 			if(graphics.getGraphicsGizmo(gizmo) != null){
 				graphics.getGraphicsGizmo(gizmo).draw(abstractCanvas);
 			}
			if(graphics.getGizSelected(gizmo) == true){
 				for(G2DObject ob : graphics.getGraphicsBounds(gizmo)){
 					ob.draw(abstractCanvas);
 				}
 			}
 		}
 		
 		for(String ball : eventListener.getBalls()){
 			graphics.getGraphicsBall(ball).draw(abstractCanvas);
 		}
 		
 		if(!runningMode){
 			if(graphics.getGizTriggers().size() > 0){
 				for(String connect : graphics.getGizTriggers()){
 					for(String to : graphics.getGizTriggers(connect)){
 						if(graphics.getGraphicsLine(connect, to) != null){
 							graphics.getGraphicsLine(connect, to).draw(abstractCanvas);
 						}
 					}
 				}
 			}
 		}   
 		g.drawImage(bufferImage, 0, 0, null);
 	}
 
 	@Override
 	public void update(Observable o, Object arg) 
 	{
 		repaint();
 	}
 	
 	// This is just here so that we can accept the keyboard focus
 	public boolean isFocusable() { return true; }
 }
