 package com.NSBCoding;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class Keys extends JPanel {
 	
 	
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public Rectangle character;
 	public Rectangle FinalBoss;
 	public Rectangle Boss;
 	public Rectangle Boss2;
 	public Rectangle Boss3;
 	public Rectangle Boss4;
 	public Rectangle Boss5;
     public Rectangle StartingPoint;
     public Rectangle Top;
     public Rectangle Bottom;
     public Rectangle Left;
     public Rectangle Right;
     public Rectangle Line;
     public Rectangle Line2;
     public Rectangle InvLine;
     public Rectangle InvLine2;
     public Rectangle Line3;
     public Rectangle Line4;
     public Rectangle Line5;
     public Rectangle Line6;
     public Rectangle Line7;
     public Rectangle Line8;
     public Rectangle Line9;
     public Rectangle Line10;
     public Rectangle Line11;
     public Rectangle Line12;
     public Rectangle Line13;
     public Rectangle Line14;   
     public Rectangle line3;
     public Rectangle line4;
     public Rectangle line5;
     public Rectangle line6;
     public Rectangle line7;
     public Rectangle line8;
     public Rectangle line9;
     public Rectangle line10;
     public Rectangle line11;
     public Rectangle line12;
     public Rectangle line13;
     public Rectangle line14;
    
 
 
 	public int charW = 25;
 	public int charH = 25;
     public int LineH = 720;
     public int LineW = 5;
     public int Line2H = 242;
     public int LineInv = 60;
     public int InvRectH = 720;
     public int InvRectW = 100;
     public int FBossW = 10;
     public int FBossH = 25;
     public int BossW = 30;
     public int BossH = 400;
     
 
     public long jumpingTime = 200;
 
 
     public float verticalSpeed = 1f;
 
     
 	public boolean right = false;
 	public boolean left = false;
 	public boolean mouseActive = false;
     public boolean LeftSide = false;
     public boolean RightSide = false;
     public boolean up = false;
     public boolean down = false;
     public boolean jumping = false;
     public boolean Passed = false;
     public boolean Reset = false;
     public boolean StopReset1 = false;
     public boolean Restart = false;
     public boolean KeysIns = false;
     public boolean Dead = false;
     public boolean DeathScreen = false;
     public boolean isMoving = true;
     public boolean FMoving = true;
     public boolean BossMoving = true;
     public boolean BossMoving2 = true;
     public boolean BossMoving3 = true;
     public boolean BossMoving4 = true;
     public boolean BossMoving5 = true;
     public boolean FUp = true;
     public boolean FDown = false;
     public boolean FLeft = true;
     public boolean FRight = false;
     public boolean BUp = true;
     public boolean BDown = false;
     public boolean BUp2 = true;
     public boolean BDown2 = false;
     public boolean BUp3 = true;
     public boolean BDown3 = false;
     public boolean BUp4 = true;
     public boolean BDown4 = false;
     public boolean BUp5 = true;
     public boolean BDown5 = false;
    
     public Point mouse;
     
 	public Keys(Display f, ImagePanel i){
         //if(i.imagesLoaded){
 		//Rectangles Being Drawn
 		
 		character = new Rectangle(52, 52, charW, charH);
 		FinalBoss = new Rectangle(0, 0, FBossW, FBossH);
 		Boss = new Rectangle(315, 315, BossW, BossH);
 		Boss2 = new Rectangle(892, 130, BossW - 10, BossH - 200);
 		Boss3 = new Rectangle(629, 38, BossW - 10, BossH - 300);
 		Boss4 = new Rectangle(972, 106, BossW - 10, BossH - 100);
 		Boss5 = new Rectangle(455, 130, BossW - 10, BossH - 250);
 		StartingPoint = new Rectangle(52, 52, charW, charH);
         Top = new Rectangle(0, 0, 1280, 1);
         Bottom = new Rectangle(0, 720, 1280, 1);
         Left = new Rectangle(0, 0, 1, 720);
         Right = new Rectangle(1280, 0, 1, 720);
         Line = new Rectangle(1180, 302, LineW, LineH);
         Line2 = new Rectangle(1180, 0, LineW,  Line2H);
         InvLine = new Rectangle(1180, 242, LineW, LineInv);
         InvLine2 = new Rectangle(1220, 0, InvRectW, InvRectH);
         Line3 = new Rectangle(109, 0, LineW, 500);
         Line4 = new Rectangle(235, 0, LineW, 300);
         Line5 = new Rectangle(403, 0, LineW, 120);
         Line6 = new Rectangle(511, 0, LineW, 30);
         Line7 = new Rectangle(580, 0, LineW, 50);
         Line8 = new Rectangle(671, 0, LineW, 300);
         Line9 = new Rectangle(770, 0, LineW, 200);
         Line10 = new Rectangle(838, 0, LineW, 500);
         Line11 = new Rectangle(936, 0, LineW, 600);
         Line12 = new Rectangle(1016, 0, LineW, 50);
         Line13 = new Rectangle(1066, 0, LineW, 450);
         Line14 = new Rectangle(1131, 0, LineW, 10);
         
         line3 = new Rectangle(109, 600, LineW, 1000);
         line4 = new Rectangle(235, 360, LineW, 1000);
         line5 = new Rectangle(403, 160, LineW, 1000);
         line6 = new Rectangle(511, 70, LineW, 1000);        
         line7 = new Rectangle(580, 80, LineW, 1000);        
         line8 = new Rectangle(671, 400, LineW, 10000);      
         line9 = new Rectangle(770, 250, LineW, 100000);      
         line10 = new Rectangle(838, 550, LineW, 10000);       
         line11 = new Rectangle(936, 630, LineW, 100000);      
         line12 = new Rectangle(1016, 100, LineW, 100000);       
         line13 = new Rectangle(1066, 500, LineW, 100000);        
         line14 = new Rectangle(1131, 60, LineW, 100000);
 		
       
     //KeyListener    
         if(isMoving){
         f.addKeyListener(new KeyAdapter(){       	
 			public void keyPressed(KeyEvent e){
 				if(e.getKeyCode() == KeyEvent.VK_D){
 					right = true;
                     RightSide = true;
 
 				}
                 if(e.getKeyCode() == KeyEvent.VK_RIGHT){
                     right = true;
                     RightSide = true;
 
                 }
 				if(e.getKeyCode() == KeyEvent.VK_A){
 					left = true;
                     LeftSide = true;
 
 				}
                 if(e.getKeyCode() == KeyEvent.VK_K){
                     KeysIns = true;
                 }
                 if(e.getKeyCode() == KeyEvent.VK_LEFT){
                     left = true;
                     LeftSide = true;
 
                 }
                 if(e.getKeyCode() == KeyEvent.VK_M) {
                     mouseActive = true;
                     System.out.println(mouse.x);
                     System.out.println(mouse.y);
                 }
                 if(e.getKeyCode() == KeyEvent.VK_S){
                     down = true; }
                 if(e.getKeyCode() == KeyEvent.VK_DOWN){
                     down = true; }
 
                 if(e.getKeyCode() == KeyEvent.VK_W){
                     up = true; }
                 if(e.getKeyCode() == KeyEvent.VK_UP){
                     up = true; }
                 if(e.getKeyCode() == KeyEvent.VK_P){
                     Passed = true; }
                 if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    //jumping = true;
                     //new Thread(new thread().start();
                      }
                      if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                     }
                  if(e.getKeyCode() == KeyEvent.VK_R) {
                      Restart = true;
                  }
                  if(e.getKeyCode() == KeyEvent.VK_C) {
                      //mouseActive = true;
                 	 }
                  
                 } 
        
         
 			public void keyReleased(KeyEvent e){
                 if(e.getKeyCode() == KeyEvent.VK_D){
 					right = false;
                     mouseActive = false;
                     RightSide = false;
 
 				}
                 if(e.getKeyCode() == KeyEvent.VK_RIGHT){
                     right = false;
                     mouseActive = false;
                     RightSide = false;
 
                 }
 
 				if(e.getKeyCode() == KeyEvent.VK_A){
 					left = false;
 		           mouseActive = false;
                     LeftSide = false;   }                
                 if(e.getKeyCode() == KeyEvent.VK_LEFT){
                     left = false;
                     mouseActive = false;
                     LeftSide = false;   }
                 if(e.getKeyCode() == KeyEvent.VK_M) {
                     mouseActive = false;
                 }
 
                 if(e.getKeyCode() == KeyEvent.VK_S){
                     down = false; }
                 if(e.getKeyCode() == KeyEvent.VK_DOWN){
                     down = false; }
 
                 if(e.getKeyCode() == KeyEvent.VK_W){
                     up = false; }
                 if(e.getKeyCode() == KeyEvent.VK_UP){
                     up = false; }
                 if(e.getKeyCode() == KeyEvent.VK_P){
                     Passed = false; }
                 if(e.getKeyCode() == KeyEvent.VK_R) {
                     Restart = false;
                 }
                 if(e.getKeyCode() == KeyEvent.VK_K) {
                     KeysIns = false;
                 }
                 isMoving = false;
                 if(e.getKeyCode() == KeyEvent.VK_C) {
                     //mouseActive = false;
                 	//System.out.println(System.getProperty("user.dir"));
                 	}
 				}
 
 
 		});
 
         //MouseListener
 
 		f.addMouseMotionListener(new MouseMotionAdapter() {
 
             public void mouseMoved(MouseEvent e){
                 mouse = new Point(e.getX(), e.getY() -25);
                  if(mouseActive){
                 	character.x = mouse.x;
                  }
 
                 repaint();
              }
 
         }); }
         }
 
 	
 
     //}
 	
 	@Override
 	public void paintComponent(Graphics g){
         //if(Main.f.i.imagesLoaded) {
         super.paintComponent(g);
                
         this.setBackground(Color.WHITE);
         if(Reset){
             character.x -= 10;
            
 
         }
 
  //Invisible lines being colored and filled
         g.setColor(getBackground());
         g.fillRect(InvLine.x, InvLine.y, InvLine.width, InvLine.height);
         g.setColor(getBackground());
         g.fillRect(InvLine2.x, InvLine2.y, InvLine2.width, InvLine2.height);
         g.fillRect(StartingPoint.x, StartingPoint.y, StartingPoint.width, StartingPoint.height);
 
   // Boss and character being colored and filled
         
        g.setColor(Color.BLUE);
        g.fillRect(FinalBoss.x, FinalBoss.y, FinalBoss.width, FinalBoss.height);
         
         g.setColor(Color.RED);
         g.fillRect(Boss.x, Boss.y, Boss.width, Boss.height);
         g.fillRect(Boss2.x, Boss2.y, Boss2.width, Boss2.height);
         g.fillRect(Boss3.x, Boss3.y, Boss3.width, Boss3.height);
         g.fillRect(Boss4.x, Boss4.y, Boss4.width, Boss4.height);
         g.fillRect(Boss5.x, Boss5.y, Boss5.width, Boss5.height);
         
         if(RightSide)    
             g.setColor(Color.BLUE);
             else             
     		g.setColor(Color.BLUE);
             if(LeftSide)
             g.setColor(Color.BLUE);
             g.fillRect(character.x, character.y, character.width, character.height);
 
   //Rectangle Being colored and filled
 
         
         g.setColor(Color.BLACK);
         g.fillRect(Top.x, Top.y, Top.width, Top.height);
         g.fillRect(Bottom.x, Bottom.y, Bottom.width, Bottom.height);
         g.fillRect(Left.x, Left.y, Left.width, Left.height);
         g.fillRect(Right.x, Right.y, Right.width, Right.height);
         g.fillRect(Line.x, Line.y, Line.width, Line.height);
         g.fillRect(Line2.x, Line2.y, Line2.width, Line2.height);
         g.setColor(Color.GREEN);
         g.fillRect(Line3.x, Line3.y, Line3.width, Line3.height);
         g.fillRect(Line4.x, Line4.y, Line4.width, Line4.height);
         g.fillRect(Line5.x, Line5.y, Line5.width, Line5.height);
         g.fillRect(Line6.x, Line6.y, Line6.width, Line6.height);
         g.fillRect(line3.x, line3.y, line3.width, line3.height);
         g.fillRect(line4.x, line4.y, line4.width, line4.height);
         g.fillRect(line5.x, line5.y, line5.width, line5.height);
         g.fillRect(line6.x, line6.y, line6.width, line6.height);
         g.fillRect(line7.x, line7.y, line7.width, line7.height);
         g.fillRect(Line7.x, Line7.y, Line7.width, Line7.height);
         g.fillRect(Line8.x, Line8.y, Line8.width, Line8.height);
         g.fillRect(line8.x, line8.y, line8.width, line8.height);
         g.fillRect(Line9.x, Line9.y, Line9.width, Line9.height);
         g.fillRect(line9.x, line9.y, line9.width, line9.height);
         g.fillRect(Line10.x, Line10.y, Line10.width, Line10.height);
         g.fillRect(line10.x, line10.y, line10.width, line10.height);
         g.fillRect(Line11.x, Line11.y, Line11.width, Line11.height);
         g.fillRect(line11.x, line11.y, line11.width, line11.height);
         g.fillRect(Line12.x, Line12.y, Line12.width, Line12.height);
         g.fillRect(line12.x, line12.y, line12.width, line12.height);
         g.fillRect(Line13.x, Line13.y, Line13.width, Line13.height);
         g.fillRect(line13.x, line13.y, line13.width, line13.height);
         g.fillRect(Line14.x, Line14.y, Line14.width, Line14.height);
         g.fillRect(line14.x, line14.y, line14.width, line14.height);
         
 
   //Information If statement     
        
 
         if(KeysIns){
             g.setColor(Color.BLACK);
             g.setFont(g.getFont().deriveFont(30f));
             g.drawString("Your goal", 123, 140);
             g.drawString("is to get", 126, 200);
             g.drawString("your character", 126, 250);
             g.drawString("to the SafeZone", 126, 300);
             g.drawString("Without touching the black line", 126, 350);
             g.drawString("Careful that you can go to the left", 126, 400);
             g.drawString("Through the walls but not right.", 126, 450);
             g.drawString("Be careful not to touch the Busses.", 126, 500);
             g.setColor(Color.BLUE);           
             g.drawString("WASD / arrow", 251,550);
             g.drawString("keys to move", 251, 600);
             g.drawString("R to Reset", 251, 650);
             g.drawString("Escape to Quit", 251, 670);
         
    //IF Statements        
         
             
             		
 
             
             
         if(DeathScreen){
         	g.setColor(Color.BLACK);
         	g.setFont(g.getFont().deriveFont(30f));
         	g.drawString("Press R to restart", 496, 300);
         	isMoving = false;
             }
         	 
         }
 
        if(Dead) {
             g.setColor(Color.RED);
             g.setFont(g.getFont().deriveFont(30f));
             g.drawString("You Died", 496, 260); 
             g.drawString("Press R to restart", 496, 310); 
             isMoving = false;
             }
 
         if(Restart){
             character.x = 52;
             character.y = 52;
             Dead = false;
             DeathScreen = false;
 
         }
 
         if(StopReset1) {
             Reset = false;
         }
 
         if(FUp){
         	FDown = false;
         	FinalBoss.y -= 1;
         }
         if(FDown){
         	FUp = false;
         	FinalBoss.y += 1;
         }
         if(FRight){
         	FLeft = false;
         	FinalBoss.x += 1;
         }
         if(FLeft){
         	FRight = false;
         	FinalBoss.x -= 1;
         }
         if(FMoving){
         	 
         	if(FinalBoss.intersects(Top)){
         		FUp = false;
         		FDown = true;
         	}
         	if(FinalBoss.intersects(Bottom)){
         		FDown = false;
         		FUp = true;
         	}
         	if(FinalBoss.intersects(Line)){
         		FRight = false;
         		FLeft = true;
         	}
         	if(FinalBoss.intersects(Line2)){
         		FRight = false;
         		FLeft = true;
         	}
         	if(FinalBoss.intersects(Line14)){
         		FLeft = false;
         		FRight = true;
         	}
         	if(FinalBoss.intersects(line14)){
         		FLeft = false;
         		FRight = true;
         	}
         	
        	}
         
         if(BUp){
         	BDown = false;
         	Boss.y -= 1;
         }
         if(BDown){
         	BUp = false;
         	Boss.y += 1;       	
         }
         if(BUp2){
         	BDown2 = false;       	
         	Boss2.y -= 1;
         }
         if(BDown2){
         	BUp2 = false;        	
         	Boss2.y += 1;
         }
         if(BUp3){
         	BDown3 = false;       	
         	Boss3.y -= 1;
         }
         if(BDown3){
         	BUp3 = false;        	
         	Boss3.y += 1;
         }
         if(BUp4){
         	BDown4 = false;       	
         	Boss4.y -= 1;
         }
         if(BDown4){
         	BUp4 = false;        	
         	Boss4.y += 1;
         }
         if(BUp5){
         	BDown5 = false;       	
         	Boss5.y -= 1;
         }
         if(BDown5){
         	BUp5 = false;        	
         	Boss5.y += 1;
         }
         
         if(BossMoving){
          	 
          	if(Boss.intersects(Top)){
          		BUp = false;
          		BDown = true;
          	}
          	if(Boss.intersects(Bottom)){
          		BDown = false;
          		BUp = true;
          	}
          	
         	}
         if(BossMoving2){
         	 
          
          	if(Boss2.intersects(Top)){
          		BUp2 = false;
          		BDown2 = true;
          	}
          	if(Boss2.intersects(Bottom)){
          		BDown2 = false;
          		BUp2 = true;
          	}
         	}
         if(BossMoving3){
        	 
             
          	if(Boss3.intersects(Top)){
          		BUp3 = false;
          		BDown3 = true;
          	}
          	if(Boss3.intersects(Bottom)){
          		BDown3 = false;
          		BUp3 = true;
          	}
         	}
         if(BossMoving4){
        	 
             
          	if(Boss4.intersects(Top)){
          		BUp4 = false;
          		BDown4 = true;
          	}
          	if(Boss4.intersects(Bottom)){
          		BDown4 = false;
          		BUp4 = true;
          	}
         	}
         if(BossMoving5){
        	 
             
          	if(Boss5.intersects(Top)){
          		BUp5 = false;
          		BDown5 = true;
          	}
          	if(Boss3.intersects(Bottom)){
          		BDown5 = false;
          		BUp5 = true;
          	}
         	}
         
         
        
     //If Statements for walls    
         
         if(character.intersects(FinalBoss))  {
             
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
            
 
         }
         
         if(character.intersects(Boss))  {
             
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
            
 
         }
         
         if(character.intersects(Boss2))  {
             
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
            
 
         }
         if(character.intersects(Boss3))  {
             
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
            
 
         }
         if(character.intersects(Boss4))  {
     
         	Dead = true;
         	isMoving = false;
     	DeathScreen = true;
     	character.x = 40000;
     
 
         }
         if(character.intersects(Boss5))  {
         	
         	Dead = true;
         	isMoving = false;
         	DeathScreen = true;
         	character.x = 40000;
    
 
         }
 
         if(character.intersects(Right))  {
             character.x -= 10;
             StopReset1 = true;
 
         }
         
         if(character.intersects(Left))  {
             character.x += 10;
             StopReset1 = true;
 
         }
 
         if(character.intersects(Bottom))  {
             character.y -= 10;
             StopReset1 = true;
 
         }
 
         if(character.intersects(Top))  {
             character.y += 10;
             StopReset1 = true;
 
         }
 
         if(character.intersects(Line))  {
                      
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
            
 
         }
         if(character.intersects(Line2))  {
         	       
             Dead = true;
             isMoving = false;
             DeathScreen = true;
             character.x = 40000;
             
         }
         if(character.intersects(Line3))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(Line4))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(Line5))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(Line6))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line3))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line4))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line5))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line6))  {
             Reset = true;
             StopReset1 = true;
 
         }
         if(character.intersects(line7))  {
             Reset = true;
             StopReset1 = true;
         }
         if(character.intersects(Line7))  {
             Reset = true;
             StopReset1 = true;
         }
         if(character.intersects(line8))  {
             Reset = true;
             StopReset1 = true;}
             
         if(character.intersects(Line8))  {
                 Reset = true;
                 StopReset1 = true;}
         if(character.intersects(line9))  {
             Reset = true;          
             StopReset1 = true;}    
                                    
         if(character.intersects(Line9))  {
                 Reset = true;
                 StopReset1 = true;}
         
         if(character.intersects(line10))  {
             Reset = true;           
             StopReset1 = true;}     
                                     
         if(character.intersects(Line10))  {
                 Reset = true;
                 StopReset1 = true;}
         
         if(character.intersects(line11))  {
             Reset = true;            
             StopReset1 = true;}      
                                      
         if(character.intersects(Line11))  {
                 Reset = true;
                 StopReset1 = true;}
                                     
         if(character.intersects(line12))  {
             Reset = true;            
             StopReset1 = true;}      
                                      
         if(character.intersects(Line12))  {
                 Reset = true;        
                 StopReset1 = true;}  
                                      
         if(character.intersects(line13))  {
             Reset = true;            
             StopReset1 = true;}      
                                      
         if(character.intersects(Line13))  {
                 Reset = true;        
                 StopReset1 = true;}  
                                      
         if(character.intersects(line14))  {
             Reset = true;            
             StopReset1 = true;}      
                                      
         if(character.intersects(Line14))  {
                 Reset = true;        
                 StopReset1 = true;}
         
 
        //If Statements for Invisible lines 
         
         if(character.intersects(InvLine)) {
 
             g.setColor(Color.BLUE);
             g.setFont(g.getFont().deriveFont(30f));
             g.drawString("You", 496, 260);
             g.drawString("Pass!", 496, 300);
            
         }
         
         if(character.intersects(InvLine2)) {
 
             g.setColor(Color.BLUE);
             g.setFont(g.getFont().deriveFont(30f));
             g.drawString("You", 496, 260);
             g.drawString("Pass!", 496, 300);
             System.out.println("Ended");
             try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				
 				e.printStackTrace();
 			}
             System.exit(0);	
             
             
         }
         
       
        
         if(character.intersects(StartingPoint)) {
         g.setColor(Color.BLACK);
         g.setFont(g.getFont().deriveFont(30f));
         g.drawString("Press K", 258, 100);
         g.drawString("for Instructions", 260, 145);
         g.setColor(Color.BLUE);
         g.drawString("WASD / arrow", 251,550);
         g.drawString("keys to move", 251, 600);
         }
   //If Statements for character movement
 
         if(jumping){
             character.y --;
         }
 
 		if(right){
 			character.x += verticalSpeed;
         }
 		if(left){
 			character.x -= verticalSpeed;
 		}
             if(up)
                 character.y += -verticalSpeed;
 
             if(down){
                 character.y += verticalSpeed;
             }
             
             
                
                
 			           
             
 
 
 		repaint();
 	}
 
 	}
 	
      //}
 
