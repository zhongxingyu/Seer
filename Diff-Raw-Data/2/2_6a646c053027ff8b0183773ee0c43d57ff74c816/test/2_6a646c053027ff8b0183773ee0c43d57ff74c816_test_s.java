 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import javax.swing.*;
 import javax.imageio.*;
 import java.io.*;
 import java.util.*;
 import java.lang.*;
 
 public class test extends JFrame{
 	private basePanel root;
 	private boolean leftPressed;
 	private boolean rightPressed;
 	private boolean upPressed;
 	private boolean downPressed;
 	
 	public static void main(String args[])
 	{
 		test Program = new test();
 
 		Program.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Program.setVisible(true);
 		Program.getContentPane().setVisible(true);
 	
 		Program.pack();
 /*
 		java.util.Timer paintTimer = new java.util.Timer();
 		java.util.Timer updateTimer = new java.util.Timer();
 		paintTask task = new paintTask(Program.getRoot());
 		updateTask task = new updateTask(Program.getRoot());
 		long timerDelay = 16;
 		paintTimer.schedule(task,timerDelay,timerDelay);
 		updateTimer.schedule(task,timerDelay,timerDelay);*/
 		
 		while(true){
 			Program.getRoot().updateComponent();
 			Program.getRoot().paintComponent((Graphics2D)Program.getRoot().getGraphics());
 			try { Thread.sleep(10); } catch (Exception e) {}
 		}
 	}
 
 	test()
 	{
 		super("JAVA GRAPHICS TEST");
 
 		Dimension d = new Dimension(1024,1024);
 		this.setPreferredSize(d);
 		this.setResizable(false);
 		this.setLocationRelativeTo(null);
 		Point p = this.getLocation();
 		this.setLocation((int)(p.getX()-512),(int)(p.getY()-512));
 		root = new basePanel();
 		this.getContentPane().add(root);
 		
 		leftPressed = false;
 		rightPressed = false;
 		upPressed = false;
 		downPressed = false;
 		
 		addKeyListener(new keyInput());
 		
 		setIgnoreRepaint(true);
 	}
 
 	public basePanel getRoot()
 	{
 		return root;
 	}
 	
 	private class keyInput extends KeyAdapter{
 
 		boolean ctrlFlag = false;
 		
 		public void keyPressed(KeyEvent e)
 		{
 			if (e.getKeyCode() == KeyEvent.VK_LEFT)
 			{
 				root.getEntities().elementAt(0).setXDis(-5);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
 			{
 				root.getEntities().elementAt(0).setXDis(5);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_UP)
 			{
 				root.getEntities().elementAt(0).setYDis(-5);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_DOWN)
 			{
 				root.getEntities().elementAt(0).setYDis(5);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_CONTROL)
 			{
 				if(!ctrlFlag)
 				{
 					bullet bulL = new bullet(64,64,root.getEntities().elementAt(0).getPosX(),root.getEntities().elementAt(0).getPosY(),-10,0);
 					bullet bulR = new bullet(64,64,root.getEntities().elementAt(0).getPosX(),root.getEntities().elementAt(0).getPosY(),10,0);
 					bullet bulU = new bullet(64,64,root.getEntities().elementAt(0).getPosX(),root.getEntities().elementAt(0).getPosY(),0,-10);
 					bullet bulD = new bullet(64,64,root.getEntities().elementAt(0).getPosX(),root.getEntities().elementAt(0).getPosY(),0,10);
 					root.createEntity(bulL);
 					root.createEntity(bulR);
 					root.createEntity(bulU);
 					root.createEntity(bulD);
 					ctrlFlag = true;
 				}
 			}
 		}
 		
 		public void keyReleased(KeyEvent e)
 		{
 			if(e.getKeyCode() == KeyEvent.VK_LEFT)
 			{
 				root.getEntities().elementAt(0).setXDis(0);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
 			{
 				root.getEntities().elementAt(0).setXDis(0);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_UP)
 			{
 				root.getEntities().elementAt(0).setYDis(0);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_DOWN)
 			{
 				root.getEntities().elementAt(0).setYDis(0);
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_CONTROL)
 			{
 				ctrlFlag = false;
 			}
 		}
 		
 		public void keyTyped(KeyEvent e)
 		{
 			if (e.getKeyChar() == 27)
 			{
 				System.exit(0);
 			}			
 		}
 	}
 	public boolean getLeftPressed()
 	{
 		return leftPressed;
 	}
 	public boolean getRightPressed()
 	{
 		return rightPressed;
 	}
 	public boolean getUpPressed()
 	{
 		return upPressed;
 	}
 	public boolean getDownPressed()
 	{
 		return downPressed;
 	}
 }
 
 class basePanel extends JPanel{
 	private Vector<entity> entities;
 	
 	basePanel()
 	{
 		entities = new Vector<entity>();
 		Player player = new Player(64,64,512,512,0,0);
 		entities.addElement(player);
 	}
 	
 	public void paintComponent(Graphics2D g)
 	{
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(0,0,getWidth(),getHeight());
 		for(int i=0;i < entities.size();i++)
 		{
 			entities.get(i).paint(g);
 		}
 		g.dispose();
 	}
 	
 	public void updateComponent()
 	{
 		for(int i=0;i < entities.size();i++)
 		{
 			entities.get(i).update();
 		}
 	}
 	
 	public Vector<entity> getEntities()
 	{
 		return entities;
 	}
 	
 	public void createEntity(entity ent)
 	{
 		entities.addElement(ent);
 	}
 }
 
 class paintTask extends TimerTask{
 
 	basePanel paint;
 
 	paintTask(basePanel toPaint)
 	{
 		paint = toPaint;
 	}
 	
 	public void run()
 	{
 		paint.paintComponent((Graphics2D)paint.getGraphics());
 	}
 }
 
 class updateTask extends TimerTask{
 
 	basePanel paint;
 
 	updateTask(basePanel toPaint)
 	{
 		paint = toPaint;
 	}
 	
 	public void run()
 	{
 		paint.updateComponent();
 	}
 }
 
 class entity{
 	protected int width;
 	protected int height;
 	protected Image image;
 	protected int posX;
 	protected int posY;
 	protected int xDis;
 	protected int yDis;
 	
 	entity()
 	{
 		width = 64;
 		height = 64;
 		posX = 512;
 		posY = 512;
 		
 		try{
 		File file = new File("sprites/square.bmp");
 		image = ImageIO.read(file);
 		}catch(Exception e){
 			System.out.println("LOL NO FILE");
 		}
 	}
 	
 	entity(int w, int h, int x, int y, int xP, int yP, String s)
 	{
 		width = w;
 		height = h;
 		posX = x;
 		posY = y;
 		xDis = xP;
 		yDis = yP;
 		try{
 		File file = new File("sprites/"+s+".bmp");
 		image = ImageIO.read(file);
 		}catch(Exception e){
 			System.out.println("LOL NO FILE");
 		}
 	}
 
 	public int getWidth()
 	{
 		return width;
 	}
 
 	public int getHeight()
 	{
 		return height;
 	}
 	
 	public Image getImage()
 	{
 		return image;
 	}
 	
 	public int getPosX()
 	{
 		return posX;
 	}
 	
 	public int getPosY()
 	{
 		return posY;
 	}
 	
 	public void setPosX(int i)
 	{
 		posX = posX + i;
 	}
 	
 	public void setPosY(int i)
 	{
 		posY = posY + i;
 	}
 	
 	public void paint(Graphics2D g)
 	{
 		g.drawImage(this.image,this.posX,this.posY,null);
 	}
 	
 	public void update()
 	{
 		posX += xDis;
 		posY += yDis;
 	}
 	
 	public void setXDis(int xD)
 	{
 		xDis = xD;
 	}
 	
 	public void setYDis(int yD)
 	{
 		yDis = yD;
 	}
 }
 
 class Player extends entity{
 	
 	Player(int w, int h, int x, int y, int xP, int yP)
 	{
 		super(w,h,x,y,xP,yP,"square");
 	}
 	
 	public void update(Graphics2D g)
 	{
 		g.drawImage(this.image,this.posX,this.posY,null);
 	}
 }
 
 class bullet extends entity{
 	
 	bullet(int w, int h, int x, int y, int xP, int yP)
 	{
 		super(w,h,x,y,xP,yP,"bullet");
 	}
 	
 	public void paint(Graphics2D g)
 	{
 		AffineTransform a = g.getTransform();
 		AffineTransform b = new AffineTransform();
 		double r = 0;
 		b.translate(posX+(width/2),posY+(height/2));
 		if(yDis < 0)
 		{
 			r = Math.toRadians(90);
 		}
 		else if(yDis > 0)
 		{
 			r = Math.toRadians(270);
 		}
 		else if(xDis > 0)
 		{
 			r = Math.toRadians(180);
 		}
		posX += xDis;
		posY += yDis;
 		b.rotate(r);
 		b.translate(-posX-(width/2),-posY-(height/2));
 		g.setTransform(b);
 		g.drawImage(this.image,this.posX,this.posY,null);
 		g.setTransform(a);
 	}
 }
 
 class sprite{
 	
 }
 
 class imageFactory{
 	
 }
