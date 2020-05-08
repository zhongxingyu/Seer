 package balloon;
 
 import java.awt.*;
 import java.applet.*;
 import java.awt.event.*;//import java.awt.event.*;  goes with ActionListener and actionPerformed
 
 public class Balloon_Applet extends Applet
                              implements ActionListener//import java.awt.event.*;  goes with ActionListener and actionPerformed
 {//variable & object declarations and initializations                                        
    
     private static final long serialVersionUID = 1L; //serial id
     Button north, west, south, east, northeast, northwest, southeast, southwest, center;
     
     public static final int DISPLAY_WIDTH = 600;
     public static final int DISPLAY_HEIGHT = 600;
     
    private int startX = DISPLAY_WIDTH/2;
    private int startY = DISPLAY_HEIGHT/2;
         
    public int boundX = DISPLAY_WIDTH-50;
    public int boundY = DISPLAY_HEIGHT-76;
 
     public static final boolean CONSOLE_LOGGING = true;
         
     public void init()
     {
         west = new Button ("West");
         add (west);
         west.addActionListener (this); 
             
         northwest = new Button ("Northwest");
         add (northwest);
         northwest.addActionListener (this); 
         
         northeast = new Button ("Northeast");
         add (northeast);
         northeast.addActionListener (this); 
                 
         north = new Button ("North");
         add (north);
         north.addActionListener (this); 
         
         center = new Button ("Center");
         add (center);
         center.addActionListener (this);
         
         south = new Button ("South");
         add (south);
         south.addActionListener (this); 
                  
         southwest = new Button ("Southwest");
         add (southwest);
         southwest.addActionListener (this);
         
         southeast = new Button ("Southeast");
         add (southeast);
         southeast.addActionListener (this); 
                 
         east = new Button ("East");
         add (east);
         east.addActionListener (this);  
           
         
     }// endInit 
     public void paint(Graphics g)
     {
     	resize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
         setBackground(Color.CYAN);
         g.setColor(Color.BLACK);
         g.fillOval(startX,startY,50,70);
        g.drawLine(startX+25,startY+70,boundX-250,boundY+100);
        
     }//endPaint
     
     public void actionPerformed(ActionEvent clic)//import java.awt.event.*;  goes with ActionListener and actionPerformed
     {
             if (clic.getSource()== west)
             doWest();
             else if (clic.getSource()== northwest)
             doNorthwest();
             else if (clic.getSource()== northeast)
             doNortheast();
             else if (clic.getSource()== north)
             doNorth();
             else if (clic.getSource()== center)
             doCenter();
             else if (clic.getSource()== south)
             doSouth();
             else if (clic.getSource()== southwest)
             doSouthwest();
             else if (clic.getSource()== southeast)
             doSoutheast();
             else if (clic.getSource()== east)
             doEast();
                               
         repaint();
         
         if (CONSOLE_LOGGING)
                 System.out.println(startX + " " + startY);
             
     }//endActionPerformed
     
     public void doWest()
     {
             if (startX-10 >= 0)
                     startX-=10;
             else
                 startX -= Math.abs(0+startX);
     }
     public void doNorthwest() 
     {
             doNorth();
             doWest();
     }
     public void doNortheast() 
     {
             doNorth();
             doEast();
     }
     public void doNorth()
     {
             if (startY-10 >= 0)
                     startY-=10;
             else
                 startY -= Math.abs(0+startY);
     }
     public void doCenter()
     {
             startX=boundX/2;
             startY=boundY/2;
     }
     public void doSouth() 
     {
             if (startY+10 <= boundY)
                     startY+=10;
             else
                startY += Math.abs(boundY-startY);
     }
     public void doSouthwest() 
     {
             doSouth();
             doWest();
     }
     public void doSoutheast() 
     {
             doSouth();
             doEast();
     }
     public void doEast()
     {
             if (startX+10 <= boundX)
                     startX+=10;
             else
                startX += Math.abs(boundX-startX);
     }
 }   
