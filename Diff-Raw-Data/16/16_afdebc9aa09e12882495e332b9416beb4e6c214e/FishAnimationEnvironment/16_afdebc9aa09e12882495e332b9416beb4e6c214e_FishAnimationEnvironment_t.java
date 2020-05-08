 package edu.ucsb.cs56.projects.games.cs56_games_fish_animation;
 
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
    @author Christina Morris
    @author Mathew Glodack
    @version for CS56, proj02, Spring 2013, UCSB
 **/
 
 public class FishAnimationEnvironment extends JFrame implements java.io.Serializable{
     Thread animate;
     DrawingPanel fishPanel = new DrawingPanel();
     int maxX = 1000, maxY = 750; // Default height and width of the game at start
     int posX = maxX/2, posY = maxY/2;  //used to position the shark at the origin
     int maxWidth = 100; //max width of the fish
     int boatX = maxX;//hold the position of the boat
     int eaten = 0; //number of fish eaten
     int numFish = 75; //number of fish in environment
     int maxD = 10; //holds the maximum diameter of the bubbles
     int numBubbles = 10+(int)(Math.random()*20); //creates a random amount of bubbles
     int numJellyFish; //holds the number of Jellyfish to be created
 	long time1 = System.nanoTime()/1000000000; //Used to get the start time of the game
 	int delay = 20; // The pause button delay
 
     //create ArrayLists for fish, bubbles, and jellyfish.
     ArrayList<Fish> fishArray = new ArrayList<Fish>();    
     ArrayList<Bubbles> bubblesArray = new ArrayList<Bubbles>();
     ArrayList<JellyFish> jellyfish = new ArrayList<JellyFish>();
 		
     
     /** 
        Method addNewBubbles adds Bubbles to the ArrayList
        @param bubble a Bubbles object
     **/
     private void addNewBubbles(Bubbles bubble){
 	bubblesArray.add(bubble);
     }
   
     /** 
        Method addNewFish add Fish to the ArrayList
        @param fish a Fish object
     **/
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
 	Method createsBubbles
 	@param x for the x position 
 	@param y for the y position
 	@param diameter for the diameter of the bubble
     **/
     private Bubbles createBubbles( int x, int y, int diameter){
 	int randomX = (int)(Math.random()*x);
 	int randomY = (int)(Math.random()*y);
 	int randomD = (int)(Math.random()*diameter);
 	return new Bubbles(randomX, randomY, randomD);
     }
 	
     /**
        Constructor for FishAnimationEnvironment. Creates a JFrame and a Jpanel that 
        is placed inside of it. Animation is done on the JPanel
     **/
     public FishAnimationEnvironment(){
 	for(int i=0; i<numFish; i++){
 	    addNewFish(createRandomFish(maxX,maxY,maxWidth));
 		}
 	}
 	
 	/**
        Custom Constructor for FishAnimationEnvironment. Creates a JFrame and a Jpanel that 
        is placed inside of it. Animation is done on the JPanel.  This Constructor takes
        in input from the Menu GUI for the difficulty and creates Jellyfish based on the
        selected difficulty.
     **/
 	public FishAnimationEnvironment(String difficulty){
 		if(difficulty.equals("Easy")){
 			numJellyFish = 3;
 		}
 		else if(difficulty.equals("Medium")){
 			numJellyFish = 7;
 		}
 		else{
 			numJellyFish = 14;
 		}
 	//Adds the fish into the Array that will hold the fish
 	for(int i=0; i<numFish; i++){
 	    addNewFish(createRandomFish(maxX,maxY,maxWidth));
 	}
 	
 	//Adds the random amount of Bubbles into the ArrayList
 	for(int i=0; i<numBubbles; i++){
 	    addNewBubbles(createBubbles(maxX,maxY,maxD));
 	}
 	//Adds the Jellyfish into the game 
 	for(int i = 0; i<numJellyFish; i++){
 	    JellyFish j = new JellyFish((int)(Math.random()*12345)%maxX,maxY,(Math.random()*123)%50+15);
 	    jellyfish.add(j);
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
        the score, the shark, the fish, the jellyfish, the seaweed, 
        the boat, and the bubbles.     
     **/
 
     class DrawingPanel extends JPanel{
 	public void paintComponent(Graphics g){
 	    Graphics2D g2 = (Graphics2D) g;
 	    g2.setColor(Color.BLUE);
 	    g2.fillRect(0,0,this.getWidth(),this.getHeight());	    		
      
 	    //Starts the Action listener to listen for mouse events in the panel
 	    MouseHandler handler = new MouseHandler();
 	    fishPanel.addMouseListener(handler);
 	    fishPanel.addMouseMotionListener(handler);
 		
 	    //Images in the game
 	    Image shark = new ImageIcon("shark.jpg").getImage();
 	    Image image = new ImageIcon("Seaweed.jpg").getImage();
 	    Image boat = new ImageIcon("cartoon-boat.jpg").getImage();
 	    Image pause = new ImageIcon("PauseButton.jpg").getImage();
	    //JButton Pause = new JButton(pause);
 		
 	    //Draws the seaweed at the specified points
 	    for ( int i=0; i < this.getWidth()+125; i+=125 ) {
 		g.drawImage(image, i, this.getHeight()-83, this);
 	    }
 		
 	    //Draws the image of the boat and also animates it
 	    g.drawImage(boat, boatX, -135, this);
 	    boatX-=10;
 	    if(boatX <= -250){
 	       	boatX = this.getWidth();
 	    }
 				
 	    //Draws the fish based off the fish info array
 	    g2.setColor(Color.YELLOW);
 	    for(int i=0; i<fishArray.size(); i++){
 		g2.draw(fishArray.get(i));
 	    }
 	    
 	    //Draws the image of the Shark
 	    Shark s = new Shark(posX, posY);
 	    g2.drawImage(shark, s.getXPos()-160, s.getYPos()-130, this);
 	    
 	    //Draws the bubbles with the blue gradient
 	    Color b = new Color(127, 255, 212);
 	    for(int i=0; i<bubblesArray.size(); i++){
 		int BubblesY = bubblesArray.get(i).getY()-bubblesArray.get(i).getDiameter()/2;
 		int BubblesY2 = bubblesArray.get(i).getY()+bubblesArray.get(i).getDiameter()/2;
 		int BubblesX = bubblesArray.get(i).getX()-bubblesArray.get(i).getDiameter()/2;
 		int BubblesX2 = bubblesArray.get(i).getX()+bubblesArray.get(i).getDiameter()/2;
 		//Creates a gradient color for the Bubble
 		GradientPaint gradient = new GradientPaint(BubblesX, BubblesY, Color.BLUE, BubblesX2, BubblesY2, b);
 		//Sets the paint to the gradient color
 		g2.setPaint(gradient);
 		//Draws a Bubbles object onto the screen
 		int xb = bubblesArray.get(i).getX(); 
 		int yb = bubblesArray.get(i).getY();
 		int db = bubblesArray.get(i).getDiameter();
 		g2.fillOval(xb, yb, db, db);
 	    }
 		
 	    //Sets the Color to PINK for the jellyfish
 	    g2.setColor(Color.PINK);
 	    for(int i = 0; i<jellyfish.size(); i++){
 		if(jellyfish.get(i).CheckJellyFish()==true){		    		    
 		    //Draws the Body of the JellyFish
 		    g2.fillArc(jellyfish.get(i).getX(),jellyfish.get(i).getY(),50,40,0,180);
      		    for(int j = jellyfish.get(i).getX()+5;j<jellyfish.get(i).getX()+50;j+=5){	 		
 			//Draws the Tentacles of the JellyFish
 			g2.drawLine(j,jellyfish.get(i).getY()+75,j,jellyfish.get(i).getY()+10);}
 		}
 		else{
 		    //Draws the Body of the JellyFish
 		    g2.fillArc(jellyfish.get(i).getX()+5,jellyfish.get(i).getY(),40,50,0,180); 
 		    
 		    for(int j = jellyfish.get(i).getX()+10;j<jellyfish.get(i).getX()+45;j+=5){
 			
 			//Draws the Tentacles of the JellyFish
 			g2.drawLine(j,jellyfish.get(i).getY()+95,j,jellyfish.get(i).getY()+10);}
 		}	    
 	    }
 	    	
 	    //displays the number of points
 	    g.setFont(new Font("Verdana", Font.PLAIN, 35));
 	    g.setColor(Color.RED);
 	    String str1 = "Points: " + eaten + "!";
 	    g.drawString(str1,0,35);
 	    
 	    //displays the current elapsed time of the game in seconds
 	    g.setFont(new Font("Verdana", Font.PLAIN, 25));
 	    g.setColor(Color.RED);
 	    String str2 = "Seconds Elapsed: " + ((System.nanoTime()/1000000000) - time1);
 	    g.drawString(str2,0,65);
 	    
 	    //Draws the image of the Pause button
 	    g2.drawImage(pause, (fishPanel.getWidth() - 64), 0, this);
 	    
 	    
 	}
     }//end DrawingPanel
     
     /**
 		Class that animates the game to get the fish and bubbles to move.
 		*/
     class Animate extends Thread{
 	public void run(){
 	    try{
 		while(true){
 		    display(delay);
 		}//end while loop
 	    }catch(Exception ex){
 		if(ex instanceof InterruptedException){}
 		else{//Unexpected exception occurred.
 		    System.out.println(ex);
 		    System.exit(1);//terminate program
 		}//end else
 	    }//end catch
 	}//end run
     
     /** 
         Class that holds the information for each separate fish in order to
         animate the fish and know its whereabouts on the screen.
         */
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
 	 
 	    for(int i=0; i<fishArray.size(); i++){
 
 		//get x position, y position, width, and height of fish
 		int xf = (int)fishArray.get(i).getXPos();
 		int yf = (int)fishArray.get(i).getYPos();
 		int wf = (int)fishArray.get(i).getWidth();
 		int hf = (int)fishArray.get(i).getHeight();
 		
 		/*check if each fish is at sharks mouth and remove from screen and
 		  increment eaten if true*/
 		 
 		if ((xf > posX-40 && xf < posX+40) && (yf > posY-25 && yf < posY+25)){
 		    info.add(new FishInfo(fishPanel.getWidth(), Math.random()*maxY, wf, hf));    
 		    eaten++;
 	      	}
 		else{
 		    info.add(new FishInfo(xf, yf, wf, hf));
 		}
 	    }
 
 	    for(int i=0; i<jellyfish.size(); i++){
 
 		//get x and y positions of jellyfish
 		int xj = jellyfish.get(i).getX();
 		int yj = jellyfish.get(i).getY();
 
 		//check if shark is touching a jellyfish and delete jellyfish and penalize if true
 		if((xj-20 > posX-200 && xj+20 < posX+40) && (yj+60 > posY-60 && yj < posY+25)){
 		    eaten -= 10;
 
 		    //reset jellyfish position
 		    jellyfish.get(i).setX(((int)((Math.random()*12345)%fishPanel.getWidth())));
 		    jellyfish.get(i).setY((int)fishPanel.getHeight());
 		}
 	    } 
 	    fishArray.clear();
 
 	    //Displays the Bubbles of the bubblesArray ArrayList
 	    for(int i=0; i<bubblesArray.size();i++){
 		if ( bubblesArray.get(i).getY() <= -20){
 		    bubblesArray.get(i).setX((int) (Math.random()*fishPanel.getWidth()));
 		    bubblesArray.get(i).setY(fishPanel.getHeight());
 		}
 		else
 		    //moves the bubbles 2 pixels
 		    bubblesArray.get(i).move(2);
 	     
 	    }
 	    //Displays the jellyfish ArrayList
 	    for(int i=0; i<jellyfish.size();i++){
 	      if(jellyfish.get(i).getY()<=-100){
 		    jellyfish.get(i).setX(((int)((Math.random()*12345)%fishPanel.getWidth())));
 		    jellyfish.get(i).setY((int)fishPanel.getHeight());	    
 	      }
 
 	      //If the jellyfish is true it moves 10 pixels and setMove to false
 	      if(jellyfish.get(i).CheckJellyFish()==true){
 		  if ( jellyfish.get(i).getCount()%20 == 0){
 			  jellyfish.get(i).moveY(jellyfish.get(i).getSpeed());
 			  jellyfish.get(i).setMove(false);
 		  }
 	      }
 		//Sets move from false to true
 	      if (jellyfish.get(i).getCount()%20 == 10){
 		  jellyfish.get(i).setMove(true);
 	      }
 	      jellyfish.get(i).setCount();
 	  }
 	  
 	    //Speed of the fish moving across the screen
 	    double currentSpeed = numFish%6;
 	    for(int i=0; i<info.size();i++){
 		if((currentSpeed-1)<2){
 		    currentSpeed = (numFish%10)+1;
 		}
 		else{
 		    currentSpeed--;
 		}
 		double newX = info.get(i).getX()-currentSpeed;
 		int wp = fishPanel.getWidth();
 		int hp = fishPanel.getHeight();
 
 		if(newX<-50){
 		    addNewFish(new Fish(wp,((Math.random()*hp)%hp),info.get(i).getWidth(),info.get(i).getHeight()));
 		}
 		else{
 		    addNewFish(new Fish(newX,info.get(i).getY(),info.get(i).getWidth(),info.get(i).getHeight()));
 		}
 	    }
 	    fishPanel.repaint();
 	    if(Thread.currentThread().interrupted())
 		throw(new InterruptedException());
 		
 	    Thread.currentThread().sleep(delay);
 	}//end display method
     }//end inner class named Animate
     
         /**
        Class to handle mouse events. Some methods are present but not defined
        because every method of the implemented class must be present to avoid
        compiler error
     **/
     public class MouseHandler implements MouseListener, MouseMotionListener{
 	
 	public void mouseClicked(MouseEvent e){
	    /*  if((e.getX() > posX-200 && e.getX() < posX+40) && (e.getY() > posY-60 && e.getY() < posY+25)){*/
		posX = e.getX();
		posY = e.getY();
		//}    
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
	    /* if((e.getX() > posX-200 && e.getX() < posX+40) && (e.getY() > posY-60 && e.getY() < posY+25)){*/
		posX = e.getX();
		posY = e.getY();
		//  }
 	}		
     }
    
 }//end class Animate
