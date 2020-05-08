 package ui;
 
 import java.awt.Point;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.File;
 
 import life.EvolveManager;
 import life.EvolveManagerState;
 
 public class LifeController extends ComponentAdapter implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener{
 	
 	protected DrawPanel drawer;
 	protected EvolveManager evolver;
 	protected Point lastMousePos = new Point(0, 0);
 	protected boolean leftButton = false;
 	protected boolean paused = false;
 	protected int speed = 0;
 	protected boolean reverse = false;
 	
 	public LifeController() {
 		
 	}
 	
 	public void setDrawer(DrawPanel p) {
 		this.drawer = p;
 	}
 	
 	public DrawPanel getDrawer() {
 		return drawer;
 	}
 	
 	public void setEvolver(EvolveManager evolver) {
 		this.evolver = evolver;
 	}
 	
 	public void loadFromFile(File f) {
 		evolver.loadFromFile(f);
 	}
 	
 	public void saveToFile(File f) {
 		evolver.saveToFile(f);
 	}
 
 	public void setSpeed(int s){
 		int sign = reverse ? -1 : 1;
 		if (s >= 0){
 			drawer.getDrawer().setInterval(33);
 			evolver.setSpeed(sign * (1 << s));
 		}else{
 			drawer.getDrawer().setInterval(33 << -s);
 			evolver.setSpeed(sign);
 		}
 	}
 	
 	public void componentResized(ComponentEvent e) {
 		drawer.onResize();
 	}
 	
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		int i = e.getWheelRotation();
 		if(i < 0){
 			drawer.zoomIn(-i, lastMousePos.x, lastMousePos.y);
 		}else{
 			drawer.zoomOut(i, lastMousePos.x, lastMousePos.y);
 		}		
 	}
 	
 	public void mouseClicked(MouseEvent e) {
 		if(paused && e.getButton() == MouseEvent.BUTTON3) {
 			Point pt = e.getPoint();
 			Point origin = drawer.getOrigin();
 			int zoom = drawer.getZoom();
 			int x = pt.x - origin.x, y = pt.y - origin.y;
 			if(zoom >= 0) {
 				x >>= zoom;
 			y >>= zoom;
 			} else {
 				x <<= -zoom;
 				y <<= -zoom;
 			}
 			evolver.resetState(drawer.getDrawer().getLastDrawnState());
 			evolver.toggleCell(y, x);
 		}
 	}
 	
 	public void mouseEntered(MouseEvent e) {
 	
 	}
 	
 	public void mouseExited(MouseEvent e) {
 	
 	}
 
 	public void mousePressed(MouseEvent e) {
 		lastMousePos = e.getPoint();
 		if(e.getButton() == MouseEvent.BUTTON1) {
 			leftButton = true;
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		if(e.getButton() == MouseEvent.BUTTON1) {
 			leftButton = false;
 		}
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		if(leftButton) {
 			Point pt = e.getPoint();
 			drawer.translate(pt.x - lastMousePos.x, pt.y - lastMousePos.y);
 			lastMousePos = pt;
 		}
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		lastMousePos = e.getPoint();
 	}
 	
 	public void keyPressed(KeyEvent e) {
 		switch(e.getKeyCode()){
 			case KeyEvent.VK_ADD:
 			case KeyEvent.VK_PLUS:
 			case KeyEvent.VK_I:
 				drawer.zoomIn(1, drawer.getWidth()/2, drawer.getHeight()/2);
 				break;
 			case KeyEvent.VK_SUBTRACT:
 			case KeyEvent.VK_MINUS:
 			case KeyEvent.VK_K:
 				drawer.zoomOut(1, drawer.getWidth()/2, drawer.getHeight()/2);	
 				break;
 			case KeyEvent.VK_UP:
 				drawer.translate(0, 50);
 				break;
 			case KeyEvent.VK_DOWN:
 				drawer.translate(0, -50);
 				break;
 			case KeyEvent.VK_LEFT:
 				drawer.translate(50, 0);
 				break;
 			case KeyEvent.VK_RIGHT:
 				drawer.translate(-50, 0);
 				break;
 
 			case KeyEvent.VK_P:
 				paused = ! paused;
 				drawer.getDrawer().setPaused(paused);
 				break;
 			case KeyEvent.VK_Y:
 				speed = Math.min(speed + 1, 12);
 				setSpeed(speed);
 				break;			
 			case KeyEvent.VK_T:
 				speed = Math.max(speed - 1, -6);
 				setSpeed(speed);
 				break;
 			case KeyEvent.VK_R:
 				reverse = ! reverse;
 				setSpeed(speed);
				drawer.getDrawer().flushOps();
 				break;
 
 			default:
 				break;
 		}
 	}
 
 	public void keyReleased(KeyEvent arg0) {
 	}
 
 	public void keyTyped(KeyEvent e) {
 	}
 
 	public boolean needsMoreEvolve() {
 		if(!reverse){
 			return (! paused) && drawer.getDrawer().opLength() < 4;
 		}else{
 			return (! paused) && drawer.getDrawer().opLength() < 1;
 		}
 	}
 	
 	public void onNewState(EvolveManagerState s){
 		drawer.getDrawer().addOp(s.algo.getDrawer(), s.algo.getState(), s.forced, s);
 	}
 }
