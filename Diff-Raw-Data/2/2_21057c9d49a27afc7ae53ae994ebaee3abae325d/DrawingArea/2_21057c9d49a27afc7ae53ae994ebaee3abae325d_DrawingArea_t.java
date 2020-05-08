 // File   : gui/low-level/drawing2/DrawingArea.java
 // Purpose: Demo subclassing JPanel, overriding paintComponent,
 //          private state, use of repaint().
 // Author : Fred Swartz - 21 Sept 2006 - Placed in public domain.
 
 import java.awt.*;
 import javax.swing.*;
 
 
 
 ///////////////////////////////////////////////////////// DrawingArea
 class DrawingArea extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	
 	private Room room;
 	protected int diameter=12;
 	protected int radius=diameter/2;
 	protected double mlength=0;
 	
 	//======================================================== fields
     private Color _ovalColor;      // Color of oval.
     private GameSystem gameSystem;
     
     //=================================================== constructor
     public DrawingArea(GameSystem game) {
         _ovalColor = Color.black;  // Initial color.
         setPreferredSize(new Dimension(320,320));
         gameSystem = game;
     }
 
     //================================================ paintComponent
     @Override public void paintComponent(Graphics g) {
         super.paintComponent(g);  // Ask parent to paint background.
 
         g.setColor(_ovalColor);
         
         
                 
         if(gameSystem.getGame() != null )
         {
         	g.drawRect(50,50,200,200);
         	room = gameSystem.getGame().getPlayer().getRoom();
         
         	Font regular = g.getFont();
         	
         	g.setColor(Color.BLACK);
         	g.setFont( new Font("Serif", Font.ITALIC | Font.BOLD, 20));      	    
         	g.drawString( room.getRoomName(),125, 150);
         	
         
         	g.setFont(regular);
         	
         	if( gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.west) != null)
         	{
         		drawLeftArrow(g, 40, 150, 50, 150);
         		g.drawString(gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.west).toString(), 5, 145);
         	}
         	if( gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.east) != null)
         	{
         		drawRightArrow(g, 260, 150, 250, 150);
         		g.drawString(gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.east).toString(), 260, 150);
         	}
         	if( gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.north) != null)
         	{
         		drawUpArrow(g, 150, 40, 150, 50);
         		g.drawString(gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.north).toString(), 150, 20);
         	}
         	if( gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.south) != null)
         	{
             	drawDownArrow(g, 150, 260, 150, 250);
         		g.drawString(gameSystem.getGame().getPlayer().getRoom().getExitRoom(Exit.south).toString(), 150, 275);
         	}
         	if(gameSystem.getGame().getPlayer().getRoom().hasMonster())
         	{
         		g.setColor(Color.RED);
         	    Font myFont = new Font("Serif", Font.ITALIC | Font.BOLD, 20);
         	    g.setFont(myFont);
         	    g.drawString(gameSystem.getGame().getPlayer().getRoom().getMonster().getName(), 130, 70);
         	}
         	
         	if(gameSystem.getGame().getPlayer().getRoom().getAllItems() != null)
         	{
         		g.setColor(Color.GREEN);
         	    Font myFont = new Font("Serif", Font.ITALIC | Font.BOLD, 20);
         	    g.setFont(myFont);
        	    if (gameSystem.getGame().getPlayer().getRoom().numOfItems() <= 1)
         	    	g.drawString(gameSystem.getGame().getPlayer().getRoom().getAllItems(), 100, 200);
         	    else g.drawString("*Multiple Items*", 100, 200);
         	}
         	
         	
 
         }
         
         
     }
 
     //==================================================== setMyColor
     public void setMyColor(Color col) {
         _ovalColor = col;  // Remember color.
         repaint();         // Color changed, must repaint
     }
     
     public void drawLeftArrow(Graphics g,int x0,int y0, int x1,int y1){
 
          g.drawLine(x0,y0,x1,y1);
          g.drawLine(x0 ,y0,x0 + 5 ,y1 - 5);
          g.drawLine(x0 ,y0,x0 + 5, y1 + 5);
         
       }
     public void drawRightArrow(Graphics g,int x0,int y0, int x1,int y1){
         
         g.drawLine(x0,y0,x1,y1);
         g.drawLine(x0 ,y0,x0 - 5 ,y1 - 5);
         g.drawLine(x0 ,y0,x0 - 5, y1 + 5);
         
      }
     
     public void drawUpArrow(Graphics g,int x0,int y0, int x1,int y1){
         
         g.drawLine(x0,y0,x1,y1);
         g.drawLine(x0 ,y0,x0 +5 ,y1 - 5);
         g.drawLine(x0 ,y0,x0 - 5, y1 - 5);
       
      }
     
     public void drawDownArrow(Graphics g,int x0,int y0, int x1,int y1){
 
         g.drawLine(x0,y0,x1,y1);
         g.drawLine(x0 ,y0,x0 - 5 ,y1 + 5);
         g.drawLine(x0 ,y0,x0 + 5, y1 + 5);
       
      }
 }
