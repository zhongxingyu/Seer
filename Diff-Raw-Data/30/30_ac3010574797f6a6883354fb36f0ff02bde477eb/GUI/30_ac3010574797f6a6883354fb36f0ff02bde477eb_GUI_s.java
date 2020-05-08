 package game;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class GUI extends JPanel implements KeyListener, MouseListener, MouseMotionListener{
 
 	int mX, mY;
 
 	private static GUI instance;
	private Paint paint = Paint.getInstance();
	
 	JFrame f = new JFrame("Operation Lapis");
 	Timer draw = new Timer();
 	
 	public static GUI getInstance(){
 		if (instance == null){
 			instance = new GUI();
 		}
 		return instance;
 	}
 	
 	public void drawGUI(){
 		
 		draw.schedule(drawTask, 16, 16);
 		f.setSize(800, 600);
 		f.setVisible(true);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setResizable(false);
 		
 		f.addKeyListener(this);
 		f.addMouseListener(this);
 		f.addMouseMotionListener(this); 
 		f.add(Paint.getInstance());
 		
 	}
 
 	TimerTask drawTask = new TimerTask() {
 		public void run() {
			
 			f.repaint();
 		}
 	};
 
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
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_ESCAPE){
			if (paint.isMenu() == false){
				paint.setMenu(true);
			}else{
				paint.setMenu(false);
			}
		}
		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseMoved(MouseEvent e ) {
 		
 		mX = e.getX();
 		mY = e.getY();
 		
 	}
 
 	public int getmX() {
 		return mX;
 	}
 
 	public void setmX(int mX) {
 		this.mX = mX;
 	}
 
 	public int getmY() {
 		return mY;
 	}
 
 	public void setmY(int mY) {
 		this.mY = mY;
 	}
 	
 	
 	
 }
