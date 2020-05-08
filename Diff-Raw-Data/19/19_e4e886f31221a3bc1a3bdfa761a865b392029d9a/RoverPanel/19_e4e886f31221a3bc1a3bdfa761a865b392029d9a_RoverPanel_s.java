 //********************************************************************
 //  RoverPanel.java       
 //
 //********************************************************************
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.awt.geom.*;
 import java.lang.*;
 
 public class RoverPanel extends JPanel implements ActionListener, KeyListener
 {
     private static Font sanSerifFont = new Font("SanSerif", Font.PLAIN, 12);
 
     private int width = 800, height = 800;
     public int delay=5;
     private final int IMAGE_SIZE = 35;
     private ImageIcon image;
     private JButton faster,slower,pause;
     private JButton modeButton;
     private Timer timer;
     private double x, y, moveX, moveY;
     private double roverRotation, wheelRotation, rotationSpeed, roverRotationSpeed;
     private boolean mode;		//mode = false -> Ackermann, mode = true -> Crab
    //-----------------------------------------------------------------
    //  Sets up the panel, including the timer for the animation.
    //-----------------------------------------------------------------
    public RoverPanel()
    {
         timer = new Timer(delay, this);
 
         x = 200;
         y = 700;
         moveX = 0;
         moveY =0;
         roverRotation = wheelRotation = 0;
         rotationSpeed=0;
         roverRotationSpeed=0;
         mode = false;
         setFocusable(false);
         addKeyListener(this);
         setFocusable(true);
         setFocusTraversalKeysEnabled(false);
         setPreferredSize (new Dimension(width, height));
         setBackground (Color.lightGray);
         timer.start();
    }
 
    //-----------------------------------------------------------------
    //  Draws the image in the current location.
    //-----------------------------------------------------------------
    @Override
    public void paintComponent (Graphics page)
    {
         this.height=getHeight();
         this.width=getWidth();
         super.paintComponent (page);
         Graphics2D g2d = (Graphics2D) page;
         g2d.setFont(sanSerifFont);
         FontMetrics fm = g2d.getFontMetrics();
         //Add in obstacles
         g2d.setColor(Color.orange);
         Ellipse2D.Double obs1 = new Ellipse2D.Double(50, 600, 60, 60);
 	    g2d.fill(obs1);
         
 	    Ellipse2D.Double obs2 = new Ellipse2D.Double(200, 400, 20, 20);
 	    g2d.fill(obs2);
 	    
 	    Ellipse2D.Double obs3 = new Ellipse2D.Double(300, 300, 20, 20);
 	    g2d.fill(obs3);
 	    
 	    Ellipse2D.Double obs4 = new Ellipse2D.Double(400, 400, 20, 20);
 	    g2d.fill(obs4);
 	    
 	    Ellipse2D.Double obs5 = new Ellipse2D.Double(200, 200, 25, 25);
 	    g2d.fill(obs5);
 	    
 	    Ellipse2D.Double obs6 = new Ellipse2D.Double(300, 100, 20, 20);
 	    g2d.fill(obs6);
 	    
 	    Ellipse2D.Double obs7 = new Ellipse2D.Double(500, 600, 40, 40);
 	    g2d.fill(obs7);
 	    
 	    Ellipse2D.Double obs8 = new Ellipse2D.Double(600, 415, 60, 60);
 	    g2d.fill(obs8);
 	    
 	    Ellipse2D.Double obs9 = new Ellipse2D.Double(500, 275, 40, 40);
 	    g2d.fill(obs9);
 	    
 	    Ellipse2D.Double obs10 = new Ellipse2D.Double(550, 150, 40, 40);
 	    g2d.fill(obs10);
 	    
 	    g2d.setColor(Color.black);
 	if(rotationSpeed>2.5){
             rotationSpeed=2.5;
         }	  
         if(rotationSpeed<-2.5){
             rotationSpeed= -2.5;
         }
         int w = fm.stringWidth("Enter: Change modes   Up/Down: Move    Left/Right: Steer");
         int h = fm.getAscent();
         g2d.drawString("Enter: Change modes   Up/Down: Move    Left/Right: Steer", 400 - (w / 2), 10 + (h / 4));
 	if(mode) {//Crab
             w = fm.stringWidth("Crab Steering");
             h = fm.getAscent();
             g2d.drawString("Crab Steering", 100 - (w / 2), 10 + (h / 4));
             wheelRotation+=rotationSpeed;
             if(roverRotationSpeed==0){
                 x-=moveY*Math.sin(Math.toRadians(wheelRotation));
                 y+=moveY*Math.cos(Math.toRadians(wheelRotation));
                 g2d.setColor(Color.GRAY);
                 Rectangle2D.Double body = new Rectangle2D.Double(-25+x,-50+y, 50, 100);
                 g2d.fill(body);
 
                 g2d.setColor(Color.BLACK);
                 Rectangle2D.Double frontLeftWheel = new Rectangle2D.Double(-35+x,-40+y, 10, 25);
                 g2d.translate(-35+5+x, -40+12.5+y);	//translate coordinates to center of wheel for rotation
                 g2d.rotate(Math.toRadians(wheelRotation));
                 g2d.translate(35-5-x, 40-12.5-y);
                 g2d.fill(frontLeftWheel);
                 g2d.translate(-35+5+x, -40+12.5+y);	//translate coordinates back to origin
                 g2d.rotate(Math.toRadians(-wheelRotation));
                 g2d.translate(35-5-x, 40-12.5-y);
 
                 g2d.setColor(Color.BLACK);
                 Rectangle2D.Double frontRightWheel = new Rectangle2D.Double(25+x,-40+y, 10, 25);
                 g2d.translate(25+5+x, -40+12.5+y);	//translate coordinates to center of wheel for rotation
                 g2d.rotate(Math.toRadians(wheelRotation));
                 g2d.translate(-25-5-x, 40-12.5-y);
                 g2d.fill(frontRightWheel);
                 g2d.translate(25+5+x, -40+12.5+y);	//translate coordinates back to origin
                 g2d.rotate(Math.toRadians(-wheelRotation));
                 g2d.translate(-25-5-x, 40-12.5-y);
 
                 g2d.setColor(Color.BLACK);
                 Rectangle2D.Double backRightWheel = new Rectangle2D.Double(25+x, 20+y, 10, 25);
                 g2d.translate(25+5+x, 20+12.5+y);	//translate coordinates to center of wheel for rotation
                 g2d.rotate(Math.toRadians(wheelRotation));
                 g2d.translate(-25-5-x, -20-12.5-y);
                 g2d.fill(backRightWheel);
                 g2d.translate(25+5+x, 20+12.5+y);	//translate coordinates back to origin
                 g2d.rotate(Math.toRadians(-wheelRotation));
                 g2d.translate(-25-5-x, -20-12.5-y);
 
                 g2d.setColor(Color.BLACK);
                 Rectangle2D.Double backLeftWheel = new Rectangle2D.Double(-35+x, 20+y, 10, 25);
                 g2d.translate(-35+5+x, 20+12.5+y);	//translate coordinates to center of wheel for rotation
                 g2d.rotate(Math.toRadians(wheelRotation));
                 g2d.translate(35-5-x, -20-12.5-y);
                 g2d.fill(backLeftWheel);
                 g2d.translate(-35+5+x, 20+12.5+y);	//translate coordinates back to origin
                 g2d.rotate(Math.toRadians(-wheelRotation));
                 g2d.translate(35-5-x, -20-12.5-y);
             }
             else{
                
                 //this is where the thing rotates about its center
             }
         }	//end if (Crab) 
 	else {//Ackermann
             w = fm.stringWidth("Ackermann Steering");
             h = fm.getAscent();
             g2d.drawString("Ackermann Steering", 100 - (w / 2), 10 + (h / 4));
             wheelRotation+=rotationSpeed;
             if((wheelRotation-roverRotation)>60){	//prevent over-rotating wheels
                 wheelRotation=roverRotation+60;
             }
             else if((wheelRotation-roverRotation)<-60){
                 wheelRotation=roverRotation-60;
             }
 				if(wheelRotation>360) {	//keep angles within -360 to 360 range
 					 wheelRotation-=360;
 					 roverRotation-=360;
 				}
 				else if(wheelRotation<-360) {
 					 wheelRotation+=360;
 					 roverRotation+=360;
 				}
             x-=moveY*Math.sin(Math.toRadians(wheelRotation));
             y+=moveY*Math.cos(Math.toRadians(wheelRotation));
             g2d.setColor(Color.BLACK);
             Rectangle2D.Double frontLeftWheel = new Rectangle2D.Double(-35+x,-40+y, 10, 25);
 	    g2d.translate(x, y);	//translate coordinates to center of wheel for rotation
 	    g2d.rotate(Math.toRadians(wheelRotation));
 	    g2d.translate(-x, -y);
 	    g2d.fill(frontLeftWheel);
        /*     g2d.translate(-35+5+x, -40+12.5+y);	//translate coordinates back to origin
 	    g2d.rotate(Math.toRadians(-wheelRotation));
 	    g2d.translate(35-5-x, 40-12.5-y);*/
                 
             g2d.setColor(Color.BLACK);
 	    Rectangle2D.Double frontRightWheel = new Rectangle2D.Double(25+x,-40+y, 10, 25);
             /*g2d.translate(25+5+x, -40+12.5+y);	//translate coordinates to center of wheel for rotation
 	    g2d.rotate(Math.toRadians(wheelRotation));
 	    g2d.translate(-25-5-x, 40-12.5-y);*/
 	    g2d.fill(frontRightWheel);
             /*g2d.translate(25+5+x, -40+12.5+y);	//translate coordinates back to origin
 	    g2d.rotate(Math.toRadians(-wheelRotation));
 	    g2d.translate(-25-5-x, 40-12.5-y);*/
 
             g2d.setColor(Color.GRAY);
 	    Rectangle2D.Double body = new Rectangle2D.Double(-25+x,-50+y, 50, 100);
             g2d.translate(x, y);
 				g2d.rotate(Math.toRadians(-wheelRotation));
             g2d.rotate(Math.toRadians(roverRotation));
             g2d.translate(-x, -y);
 	    g2d.fill(body);
             
             g2d.setColor(Color.BLACK);
 	    Rectangle2D.Double backRightWheel = new Rectangle2D.Double(25+x, 20+y, 10, 25);
             //g2d.rotate(Math.toRadians(-roverRotation));
             //g2d.rotate(Math.toRadians(roverRotation/2));
 	    g2d.fill(backRightWheel);
             
             g2d.setColor(Color.BLACK);
 	    Rectangle2D.Double backLeftWheel = new Rectangle2D.Double(-35+x, 20+y, 10, 25);
             //g2d.rotate(Math.toRadians(-roverRotation));
             //g2d.rotate(Math.toRadians(roverRotation/2));
 	    g2d.fill(backLeftWheel);
         }	//end else (Ackermann)
     }	//end paintComponent
 
 	/** this method changes delay of the repaint method thought timer.
 	* @param delta determines what the change in the timer will be on click
 	*/
     @Override
     public void actionPerformed(ActionEvent e) {
         repaint();
     }	//end actionPerformed
     public void up(){
         moveY-=.2;
 		  double tempRot = wheelRotation;
		  wheelRotation += (wheelRotation-roverRotation)/5;	//the 5 is arbitrary
		  roverRotation += wheelRotation-tempRot;	//constant rotation difference b/w rover & wheels
 		  if(moveY<-2.5){
             moveY=-2.5;
         }
     }	//end up
     public void down(){
         moveY+=.2;
 		  double tempRot = wheelRotation;
		  wheelRotation -= (wheelRotation-roverRotation)/5;
 		  roverRotation += wheelRotation-tempRot;
 		  if(moveY>2.5){
             moveY=2.5;
         }
     }	//end down
     public void left(){
         rotationSpeed-=.2;
     }	//end left
     public void right(){
         rotationSpeed+=.2;
     }	//end right
     public void rotateRoverLeft(){
         roverRotationSpeed +=.2;
         if(roverRotationSpeed>2){
             roverRotationSpeed=2;
         }
     }
     public void rotateRoverRight(){
         roverRotationSpeed -=.2;
         if(roverRotationSpeed<-2){
             roverRotationSpeed= -2;
         }
     }
     @Override
     public void keyPressed(KeyEvent e){
         int code = e.getKeyCode();
         if(code ==KeyEvent.VK_A){
             rotateRoverLeft();
         }
         if(code ==KeyEvent.VK_S){
             rotateRoverRight();
         }
         if(code==KeyEvent.VK_UP){
             up();
         }
         if(code==KeyEvent.VK_DOWN){
             down();
         }
         if(code==KeyEvent.VK_LEFT){
             left();
         }
         if(code==KeyEvent.VK_RIGHT){
             right();
         }
         if(code==KeyEvent.VK_ENTER){
             if(mode == false){	//switch steering mode on key input of enter
                 mode = true;
             }
             else{
 		mode = false;
             }
         }
    }	//end keyPressed
     @Override
     public void keyTyped(KeyEvent e){}
     @Override
     public void keyReleased(KeyEvent e){
         int code = e.getKeyCode();
         if(code ==KeyEvent.VK_A){
             roverRotationSpeed=0;
         }
         if(code ==KeyEvent.VK_S){
             roverRotationSpeed=0;
         }
         if(code==KeyEvent.VK_UP){
             moveY=0;
         }	//end if
         if(code==KeyEvent.VK_DOWN){
             moveY=0;
         }	//end if
         if(code==KeyEvent.VK_LEFT){
             rotationSpeed=0;
             //moveX=0;
         }	//end if
         if(code==KeyEvent.VK_RIGHT){
             rotationSpeed=0;
             //moveX=0;
         }	//end if
     }	//end keyReleased	
 }
