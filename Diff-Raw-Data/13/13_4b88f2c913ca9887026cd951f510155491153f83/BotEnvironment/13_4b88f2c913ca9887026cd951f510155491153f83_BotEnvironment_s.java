 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 import java.io.*;
 import java.awt.event.*;
 
 public class BotEnvironment extends JFrame implements MouseListener, Runnable
 {
 	public static final int BOTS = 25;
 	public static final int FOOD = 100;
 
 	public static final int WIDTH = 700;
 	public static final int HEIGHT = 450;
 
 	Vector<Bot> bots = new Vector();
 	Vector<Point> food = new Vector();
 	long time = System.currentTimeMillis();
 	int generation = 0;
 
 	public BotEnvironment(String fileName)
 	{
 		if(fileName == null)
 		{
 			FeedforwardNeuralNet brain = new FeedforwardNeuralNet(2,3,1,5);
 			for(int i = 0; i < BOTS; i++)
 			{
 				bots.add(new Bot(brain.getChild()));
 			}
 		}
 		else
 		{
 			//To be writen.
 		}
 		for(int i = 0; i < FOOD; i++)
 		{
 			food.add(new Point((int)(Math.random()*BotEnvironment.WIDTH),(int)(Math.random()*BotEnvironment.HEIGHT)));
 		}
 
 		addMouseListener(this);
 		setSize(WIDTH,HEIGHT);
 		setTitle("Testing in progress...");
 		setVisible(true);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		new Thread(this).start();
 		time = System.currentTimeMillis();
 	}
 
 	public void run()
 	{
 		while(true)
 		{
 			update();
 			repaint();
 			try
 			{
 				Thread.sleep(25);
 			}
 			catch(Exception ex)
 			{
 			}
 
 			if((food.size()==0) || (System.currentTimeMillis()-time > 15*1000))
 			{
 				generation++;
 				boolean change = true;
 				while(change)
 				{
 					change = false;
 					for(int i = 1; i < bots.size(); i++)
 					{
 						if(bots.get(i-1).getFood()>bots.get(i).getFood())
 						{
 							change = true;
 							Bot temp = bots.get(i);
 							bots.remove(i);
 							bots.add(i-1, temp);
 						}
 					}
 				}
 				while(bots.size() > 10)
 					bots.remove(0);
 				food = new Vector();
 				for(int i = 0; i < FOOD; i++)
 				{
 					food.add(new Point((int)(Math.random()*BotEnvironment.WIDTH),(int)(Math.random()*BotEnvironment.HEIGHT)));
 				}
 				Vector<Bot> oldBots = bots;
 				bots = new Vector();
 				for(int i = 0; i < BOTS; i++)
 				{
 					int father = (int)(Math.random()*10);
 					bots.add(new Bot(oldBots.get(father).getBrain().getChild()));
 				}
 				time = System.currentTimeMillis();
 			}
 		}
 	}
 
 	public void update()
 	{
 		for(int i = 0; i < bots.size(); i++)
 		{
 			bots.get(i).update(food);
 			for(int j = 0; j < food.size(); j++)
 			{
 				if(bots.get(i).distanceFrom(food.get(j))<=Bot.RADIUS+1)
 				{
 					bots.get(i).eat();
 					food.remove(j);
 					j--;
 				}
 			}
 		}
 	}
 
	public void paint(Graphics g)
 	{
 		//create blank canvas
 		g.setColor(Color.WHITE);
 		g.fillRect(0,0,WIDTH, HEIGHT);
 
 		g.setColor(Color.BLACK);
 		g.drawString("Gen: " + generation, 50, 50);
 
 		//draw food
 		g.setColor(Color.GREEN);
 		for(int i = 0; i < food.size(); i++)
 		{
 			g.fillOval((int)food.get(i).getX()-4, (int)food.get(i).getY()-4, 8, 8);
 		}
 
 		//draw Bots
 		for(int i = 0; i < bots.size(); i++)
 		{
 			bots.get(i).draw(g);
 		}
 

 	}
 
 	public void mouseExited(MouseEvent me){}
 	public void mouseEntered(MouseEvent me){}
 	public void mousePressed(MouseEvent me){}
 	public void mouseReleased(MouseEvent me){}
 
 	public void mouseClicked(MouseEvent me)
 	{
 	}
 
 	public static void main(String[] args)
 	{
 		new BotEnvironment(null);
 	}
 }
