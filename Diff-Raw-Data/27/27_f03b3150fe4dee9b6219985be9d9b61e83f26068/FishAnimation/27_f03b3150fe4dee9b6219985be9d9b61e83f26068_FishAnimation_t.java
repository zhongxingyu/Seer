 package edu.ucsb.cs56.S11.lkhuu.issue0000355;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.*;
 
 /**
   Creates a JFrame that animates Fish and allows for a shark
   to eat the fish. Tracks the amount of fish eaten with an
   elapsed amount of time.
    @author Lawrence Khuu
    @author Casey Barbello
    @author Daryl Pham
    @version for CS56, proj01, Spring 2013, UCSB
 **/
 
 
 public class FishAnimation extends JFrame{
     Thread animate;
     DrawingPanel fishPanel = new DrawingPanel();
     int maxX = 1000;
     int maxY = 750;
     int posX = 500, posY = 375;
     int maxWidth = 100;
     long time1 = System.nanoTime()/1000000000; //Timer to start counting when the game initiallizes
     int eaten = 0;//number of fish eaten
     int numFish = 40+(int)(Math.random()*40);
     ArrayList<Fish> fishArray = new ArrayList<Fish>();    
     
     /**
        class to handle mouse events. Some methods are present but not defined
        because every method of the implemented class must be present to avoid
        compiler error
     **/
     public class MouseHandler implements MouseListener, MouseMotionListener{
 	
 	public void mouseClicked(MouseEvent e){
 	    posX = e.getX();
 	    posY = e.getY();
 	}
 	
 	public void mousePressed(MouseEvent e){
 	}
 	
 	public void mouseEntered(MouseEvent e){
       }
         
       public void mouseExited(MouseEvent e){
       }
 	
 	public void mouseReleased(MouseEvent e){
 	}
 	
 	public void mouseMoved(MouseEvent e){
 	}
 	
 	public void mouseDragged(MouseEvent e){
 	    posX = e.getX();
 	    posY = e.getY();
 	}		
     }
     
     private void addNewFish(Fish fish){
 	fishArray.add(fish);
     }
     
     private Fish createRandomFish(int xBound, int yBound, int maxWidth){
 	int randomX = (int)(Math.random()*xBound);
 	int randomY = (int)(Math.random()*yBound);
 	int randomWidth = 10 + (int)(Math.random()*(maxWidth-10));
 	return new Fish(randomX,randomY,randomWidth,randomWidth/5);
     }
     
     /**
       Constructor for FishAnimation. Creates a JFrame and a Jpanel that 
       is placed inside of it. Animation is done on the JPanel
     **/
     public FishAnimation(){//constructor
 	for(int i=0; i<numFish; i++){
 	    addNewFish(createRandomFish(maxX,maxY,maxWidth));
 	}
 	getContentPane().add(fishPanel);
 	animate = new Animate();
 	animate.start();
 	setDefaultCloseOperation(EXIT_ON_CLOSE);    
 	setSize(maxX,maxY);
 	setVisible(true);
     }//end constructor
     
     /**
       Innerclass for a custom Jpanel for animation.  Draws out the 
       background and the in-game components including a timer, 
       the score, the shark, and the fish.
       
     **/
     class DrawingPanel extends JPanel{
 	public void paintComponent(Graphics g){
 	    Graphics2D g2 = (Graphics2D) g;
 	    g2.setColor(Color.BLUE);
 	    g2.fillRect(0,0,this.getWidth(),this.getHeight());	    
 	    g2.setColor(Color.YELLOW);
 	    for(int i=0; i<fishArray.size(); i++){
 		g2.draw(fishArray.get(i));
 	    }
 	    MouseHandler handler = new MouseHandler();
 	    fishPanel.addMouseListener(handler);
 	    fishPanel.addMouseMotionListener(handler);
 	    Image shark = new ImageIcon("shark.jpg").getImage();
 	    Shark s = new Shark(posX, posY);
 	    g2.drawImage(shark, s.getXPos()-160, s.getYPos()-130, this);
 	    
	    
 	    //displays the number of fish eaten
 	    g.setFont(new Font("ComicSans", Font.PLAIN, 35));
 	    g.setColor(Color.RED);
 	    String str1 = "Fish Eaten: " + eaten + "!";
 	    g.drawString(str1,0,35);
 	    
 	    //displays the current elapsed time of the game in seconds
 	    g.setFont(new Font("ComicSans", Font.PLAIN, 25));
 	    g.setColor(Color.RED);
 	    String str2 = "Seconds Elapsed: " + ((System.nanoTime()/1000000000) - time1);
 	    g.drawString(str2,0,65);
 	}
     }//end DrawingPanel
     
     class Animate extends Thread{
 	public void run(){//begin run method
 	    try{
 		while(true){
 		    display(40);
 		}//end while loop
 	    }catch(Exception ex){
 		if(ex instanceof InterruptedException){}
 		else{//Unexpected exception occurred.
 		    System.out.println(ex);
 		    System.exit(1);//terminate program
 		}//end else
 	    }//end catch
 	}//end run
 	
 	class FishInfo{
 	    double x,y,width,height;
 	    FishInfo(double x, double y, double width, double height){
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 	    }
 	    double getX(){return x;}
 	    double getY(){return y;}
 	    double getWidth(){return width;}
 	    double getHeight(){return height;}
 	}	   
 	
 	/**
	   Creates each frame and also checks if the fish have been eaten.  
	   Removes fish and creates a new fish if it has been eaten.
 	**/
 	void display(int delay) 
 	    throws InterruptedException{
 	    ArrayList<FishInfo> info = new ArrayList<FishInfo>();
 	    
 	    for(int i=0; i<fishArray.size();i++){
 		/*check if fish is at sharks mouth and remove from screen and
 		 increment eaten if true
 		*/
 		if ((fishArray.get(i).getXPos() > posX-40 && fishArray.get(i).getXPos() < posX+40) && (fishArray.get(i).getYPos() > posY-25 && fishArray.get(i).getYPos() < posY+25)){
 		    info.add(new FishInfo(maxX,Math.random()*maxY,fishArray.get(i).getWidth(),fishArray.get(i).getHeight()));
 	      	eaten++;
 	      	}
 		else{
 		    info.add(new FishInfo(fishArray.get(i).getXPos(),fishArray.get(i).getYPos(),fishArray.get(i).getWidth(),fishArray.get(i).getHeight()));
 		}
 	    }
 	    fishArray.clear();
 	    
 	    double currentSpeed = (numFish%10)+1;
 	    for(int i=0; i<info.size();i++){
 		if((currentSpeed-1)<2)
 		    currentSpeed = (numFish%10)+1;
 		else{
 		    currentSpeed--;
 		}	
 		double newX = info.get(i).getX()-currentSpeed;
 		if(newX<0)
 		    addNewFish(new Fish(fishPanel.getWidth(),(Math.random()*fishPanel.getHeight()),info.get(i).getWidth(),info.get(i).getHeight()));
 		else
 		    addNewFish(new Fish(newX,info.get(i).getY(),info.get(i).getWidth(),info.get(i).getHeight()));
             }
 	    fishPanel.repaint();
 	    
 	    if(Thread.currentThread().interrupted())
 		throw(new InterruptedException());
 	    Thread.currentThread().sleep(delay);
 	}//end display method
     }//end inner class named Animate
     
     /**
        Main class for running FishAnimation
     **/
     public static void main(String[] args){
         new FishAnimation();
     }//end main
     
     
 }//end class Animate
