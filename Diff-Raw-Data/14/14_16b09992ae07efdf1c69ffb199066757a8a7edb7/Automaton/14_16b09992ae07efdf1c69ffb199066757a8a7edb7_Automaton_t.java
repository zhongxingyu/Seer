 import javax.swing.*;
 //import java.awt.*;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.*;
 import java.util.*;
 
 public class Automaton extends JPanel implements MouseListener, KeyListener
 {
     Graphics g = this.getGraphics();
     static boolean move = true;
     int trait_x = -1;
     int trait_y = -1;
     LinkedList<Integer> coord = new LinkedList<Integer>();
     LinkedList<String[]> traits = new LinkedList<String[]>();
     //private Panneau pan;
     public Automaton()
     {
	setSize(779, 456);
 	//pan = new Panneau(100, 100);
 	//getContentPane().add(pan);
       
 	this.addMouseListener(this);
 	this.addKeyListener(this);
	setVisible(true);
     }
 
     public void mouseClicked(MouseEvent e)
     {
 	System.out.println("click");
 	newNode(e.getX(), e.getY());
     }
 
     public void newNode(int x, int y)
     {
 	boolean go = true;
 	for(int i = 0; i < coord.size(); i+=2)
 	    {
 		if( y >= coord.get(i+1) - 25 && 
 		    y <= coord.get(i+1) + 25 && 
 		    x >= coord.get(i) - 25 && 
 		    x <= coord.get(i) + 25)
 		    {
 			go = false;
 		    }
 	    }
 	if(go)
 	    {
 		getGraphics().drawOval(x-25, y-25, 50, 50);
 		getGraphics().drawString("q" + coord.size()/2, x-5, y+5);
 		coord.add(x);
 		coord.add(y);
 	    }
     }
 
     public void newTrait(int x, int y)
     {
 	for(int i = 0; i < coord.size(); i+=2)
 	    {
 		if( y >= coord.get(i+1) - 25 && 
 		    y <= coord.get(i+1) + 25 && 
 		    x >= coord.get(i) - 25 && 
 		    x <= coord.get(i) + 25)
 		    {
 			this.getGraphics().drawLine(trait_x, trait_y, coord.get(i), coord.get(i+1));
 		    }
 	    }
 	trait_x = -1;
     }
 
     public void mouseEntered(MouseEvent e){}
     public void mouseExited(MouseEvent e){}
 
     public void mousePressed(MouseEvent e)
     {
 	for(int i = 0; i < coord.size(); i+=2)
 	    {
 		if( e.getY() >= coord.get(i+1) - 25 && 
 		    e.getY() <= coord.get(i+1) + 25 && 
 		    e.getX() >= coord.get(i) - 25 && 
 		    e.getX() <= coord.get(i) + 25)
 		    {
 			trait_x = coord.get(i);
 			trait_y = coord.get(i+1);
 		    }
 	    }
     }
     public void mouseReleased(MouseEvent e)
     {
 	if(e.getX() < 0 || e.getY() < 0 || e.getX() >= 500 || e.getY() >= 300)
 	    return;
 	if(trait_x > -1)
 	    {
 		newTrait(e.getX(), e.getY());
 	    }
 	else
 	    {
 		newNode(e.getX(), e.getY());
 	    }
     }
 
     @Override
 	public void keyPressed(KeyEvent arg0) {
 	// TODO Auto-generated method stub
 	System.out.println(arg0.getKeyCode());
     }
 
     @Override
 	public void keyReleased(KeyEvent arg0) {
 	// TODO Auto-generated method stub
 
     }
 
     @Override
 	public void keyTyped(KeyEvent arg0) {
 	// TODO Auto-generated method stub
 
     }
 }
 
