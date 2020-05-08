 package artlife;
 
 import javax.swing.*;
 
 import java.awt.Color;
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D; 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 /**
  * Note that Java console applications need to be run through the java runtime
  * by running "java -jar JarFile.jar" in the command line.
  * Java console applications can not be previewed in the Compilr IDE, only applets can.
  */
 public class ArtLifeMain extends JApplet
 {
     
 	private static final long serialVersionUID = 1L;
 	Grid grid;
 	DrawPanel draw;
 	ActionThread action;
 	volatile boolean paused=false;
 	JLabel mouse;
 	JLabel oldy;
 	JSlider zoom;
 	int x,y, ox, oy, z;
     
     public void init(){
         //Execute a job on the event-dispatching thread:
     	//creating this applet's GUI.
     	try {
         	javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
             	public void run() {
              		grid = Grid.getGrid();
              		setSize(128*6, 128*6);
                 	createGUI();
             	}
         	});
         	System.out.println("We are done running.");
         	DrawThread drawing = new DrawThread();
         	drawing.start();
         	action = new ActionThread();
         	action.start();
     	} catch (Exception e) {
         	System.err.println("createGUI didn't successfully complete");
     	}
     }
     
     public void createGUI(){
         draw = new DrawPanel();
         draw.addMouseMotionListener(new MyMouseMotionListener());
         draw.addMouseListener(new MyMouseListener());
         draw.addMouseWheelListener((MouseWheelListener)draw.getMouseListeners()[0]);
     	getContentPane().add(draw, BorderLayout.CENTER);
     	JPanel buttPanel = new JPanel();
     	mouse = new JLabel("Mouse: ");
     	buttPanel.add(mouse);
     	zoom = new JSlider(1, 50, 10);
     	buttPanel.add(zoom);
     	JButton killAll = new JButton("Kill Everything.");
     	killAll.addActionListener(new ActionListener() {
     		public void actionPerformed(ActionEvent e) {
     			grid.killAll();
     		}
     	});
     	buttPanel.add(killAll);
     	JButton reset = new JButton("Reset");
     	reset.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				grid.createTerrain();
 				grid.createFood();
 				grid.createOrgs();
 				paused = false;
 			}
 		});
     	buttPanel.add(reset);
     	JButton center = new JButton("Center on Oldster");
     	center.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Organism old = grid.oldster;
 				System.out.println("I've lived "+old.age+" and still have "+old.getEnergy()+" energy");
 				x=old.getX()-z/2;
 				y=old.getY()-z/2;
 			}
 		});
     	buttPanel.add(center);
     	oldy = new JLabel("Oldest:");
     	buttPanel.add(oldy);
     	getContentPane().add(buttPanel,BorderLayout.SOUTH);
     }
     
     /**
      * This is the main entry point for the application
      */
     public static void main(String args[]) 
     {
     	ArtLifeMain main = new ArtLifeMain();
         main.init();
         JFrame f = new JFrame();
         f.setContentPane(main);
         f.setSize(800, 800);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setVisible(true);
     }
     
     class DrawThread extends Thread{
     	public void run() {
     		while(true) {
     			try {
    				draw.repaint();
     				sleep(50);
     			}catch (Exception e) {
 					e.printStackTrace();
 				}
     		}
     	}
     }
     
     class ActionThread extends Thread{
     	
     	public void run() {
     		int round = 0;
     		while (true) {
 				while (!paused) {
 					grid.update();
 					round++;
 					if(grid.allDead())
 						paused = true;
 					try {
 						sleep(5);
 						if (round%64==0) {
 							System.out.println(round);
 		        			oldy.setText("Oldest: "+grid.oldster.age);
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				round = 0;
 //				System.out.println("We are paused...");
 			}
     	}
     }
     
     class DrawPanel extends JPanel{
 		private static final long serialVersionUID = 1L;
 
 		public void paintComponent(Graphics graphics){
             super.paintComponent(graphics);
             Graphics2D g = (Graphics2D) graphics;
             setBackground(Color.black);
             if(z==0) {z = draw.getWidth()/zoom.getValue(); }
             grid.draw(g,x,y,Math.min(z, Grid.WIDTH), zoom.getValue());
             repaint();
         }
     }
     
     class MyMouseMotionListener extends MouseMotionAdapter{
     	public void mouseMoved(MouseEvent e) {
     		mouse.setText("Mouse: "+e.getX()/zoom.getValue()+" "+e.getY()/zoom.getValue());
     	}
 
     	public void mouseDragged(MouseEvent e) {
     		int dx = ox-e.getX(), dy = oy-e.getY();
     		if(x+dx >= 0 && x+dx+z<Grid.WIDTH)
     			x+=dx;
     		if(y+dy>=0 && y+dy+z<Grid.WIDTH) {
     			y += dy;
     		}
     		ox = e.getX();
     		oy = e.getY();
     		System.out.println(dx+" "+dy);
     	}
     }
     
     private void fixCoords() {
 		z = draw.getWidth()/zoom.getValue();
     	if(x+z>Grid.WIDTH)
     		x -= x+z-Grid.WIDTH;
     	if(y+z>Grid.WIDTH)
     		y -= y+z-Grid.WIDTH;
     }
     
     class MyMouseListener extends MouseAdapter{
     	
     	public void mouseWheelMoved(MouseWheelEvent e) {
     		zoom.setValue(e.getWheelRotation()+zoom.getValue());
     		fixCoords();
     	}
     	
     	
     }
     
 }
