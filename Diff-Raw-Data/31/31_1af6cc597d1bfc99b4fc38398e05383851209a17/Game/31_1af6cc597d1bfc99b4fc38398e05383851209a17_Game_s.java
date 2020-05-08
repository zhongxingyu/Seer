 package Crane;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class Game extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	
 	ArrayList<Drawable> drawables = new ArrayList<Drawable>();
 	private Timer t;
 	private int fps = 40;
 	private boolean dragging = false;
	Point old, current;
 	int clickedIndex;
 	
 	public Game(){
 		super();
 		ActionListener repainter = new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				repaint();
 			}
 		};
 		t = new Timer(1000/fps, repainter);
 		t.start();
 		
 		//for (int i = 0; i < 5; i++)
 
 		Drawable kevin = new Drawable(300, 300, Math.toRadians(180), null, Color.cyan);
 		Drawable bob = new Drawable(0,60, Math.toRadians(40), kevin, Color.blue);
 		Drawable jon = new Drawable (0,60, Math.toRadians(40), bob, Color.gray);
 		//Drawable dan = new Drawable (50, -80, Math.toRadians(0))
 		drawables.add(kevin);
 		drawables.add(bob);
 		drawables.add(jon);
 		
 
 		this.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {				
 		        Point p = e.getPoint();
 //			        if(d.rect.contains(p)) {
 //			        	old = p;
 //			        }
 		        for (int i = drawables.size() - 1; i >= 0; i--) {
 		        	if (drawables.get(i).isInside(p) ) {
 		        		dragging = true;
 		        		System.out.println(i);
 		        		clickedIndex = i;
 		        		break;
 		        	}
 		        }
 		        System.out.println("Mouse pressed at " + p.x +", " + p.y);
 		        
 			}
 		    public void mouseReleased(MouseEvent e) {
 		        System.out.println("Mouse released at " + e.getPoint().x + ", " + e.getPoint().y);
 		        dragging = false;
 		    }
 		});
 		this.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseDragged(MouseEvent e) {
 				if (dragging) {
 					System.out.println("Drag");
 					current = e.getPoint();
 					drawables.get(clickedIndex).moveArm(current);
 					//toMove.paintComponent(g);
 	//					if (graphics2D != null)
 	//						graphics2D.drawLine(oldX, oldY, currentX, currentY);
 					//repaint();
 					//old = current;
 	//					oldX = currentX;
 	//					oldY = currentY;
 				}
 			}
 		});
 	}
 	
 	public static void main(String[] args) {
 		Game canvas = new Game();
 		JFrame f = new JFrame("Crane");
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setSize(800, 600);
 		f.setContentPane(canvas);
 		f.setResizable(false);
 		f.setVisible(true);
 	}
 	
 	public void paintComponent(Graphics g) {
 		g.clearRect(0,0,800,600); // lol
 		for (int i = 0; i < drawables.size(); i++){
 			final Drawable d = drawables.get(i);
 			d.paintComponent(g);
 		}
 	}
 	
 }
