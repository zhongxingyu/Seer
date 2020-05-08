 
 package minigames.beerpong;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.net.URL;
 import java.util.ArrayList;
 import javax.swing.*;
 
 import main.BackToSchool;
 import main.Day;
 import main.Player;
 import minigames.sudoku.Clock;
 import minigames.sudoku.SudokuGame.Sound;
 /**
  *
  * @author Cagatay Sahin
  * 
  * Use arrow keys to set the x-y direction of the ball
  * Use "x-z" keys to set the z direction of the ball
  */
 
 public class BeerPong extends JPanel{
     private Sound backgroundSong;
     final static int  GS_INIT = 1;
     final static int  GS_MOTION = 2;
     final static int  GS_HIT = 3;
     final static int  GS_MISS = 4;
     final static int  GS_WON = 5;
     final static int  GS_LOST = 6;
     final static int INIT_BALL_RADIUS = 10;
     final static int INIT_BALL_CX = INIT_BALL_RADIUS * 2;
     final static int INIT_BALL_CY = TablePanel.HEIGHT / 2;
     final static int INIT_BALL_CZ = 0;
     
    boolean directionSet;
    
     static int gameState;
     static int missCount;
     static int hitCount;
     
     JButton exit;
     
     JPanel p_buttons;
     TablePanel p_table;
     BeerPanel p_beer;
     
     ArrayList<Sprite> cupList;
     Sprite ball;
     
     private Player student;
     private BackToSchool frame;
     private Day day;
     
     Timer t;
     public BeerPong(Player player, Day current, BackToSchool frame){
 		backgroundSong = new Sound("sounds/Background/POL-stable-boy-short.wav");
 		backgroundSong.playSound();
 		
     	this.frame = frame;
 		student = player;
 		day = current;
         ball = new Sprite();
         
        directionSet=false;
         cupList = new ArrayList<Sprite>();
         cupList.add( new Sprite() );
         cupList.add( new Sprite() );
         cupList.add( new Sprite() );
         cupList.add( new Sprite() );
         cupList.add( new Sprite() );
         cupList.add( new Sprite() );
        // System.out.println(cupList.size());
         
         p_buttons = new JPanel();
         p_beer = new BeerPanel();
         p_table = new TablePanel( cupList, ball, day);
         
         setLayout(new FlowLayout());
         
         p_buttons.setPreferredSize(new Dimension(TablePanel.WIDTH, 125));
         p_buttons.setBackground(Color.WHITE);
        
         
         add(p_beer);
         add(p_table);
         add(p_buttons);
         
         // Adding KeyBindings
         InputMap myInputMap = new InputMap();
 		ActionMap myActionMap = new ActionMap();
 		
 		up up = new up();
 		down down = new down();
 		left left = new left();
 		right right = new right();
 		
 		K k = new K();
 		R r = new R();
 		X x = new X();
 		Z z = new Z();
 		
 		Space space = new Space();
 		
 		myInputMap = this.getInputMap(WHEN_IN_FOCUSED_WINDOW);
 		
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up");
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down");
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right");
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left");
 		
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0, false), "k");
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, false), "r");
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0, false), "x");
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0, false), "z");
 		
 		myInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "space");
 		myActionMap = this.getActionMap();
 		
 		myActionMap.put("up", up);
 		myActionMap.put("down", down);
 		myActionMap.put("right", right);
 		myActionMap.put("left", left);
 		myActionMap.put("k", k);
 		myActionMap.put("r", r);
 		myActionMap.put("x", x);
 		myActionMap.put("z", z);
 
 		myActionMap.put("space", space);
 
         setPreferredSize(new Dimension(550, 450));
        // KeyListener listener = new MyKeyListener();    
        // this.addKeyListener(listener );
         setFocusable(true);
 
         int delay = 10; //milliseconds
         t = new Timer(delay, onEachFrame);
         t.start();
         resetGame();
     }
     
     //Reset the game
     public void resetGame(){           
         p_table.setCupsVisible();
         gameState = GS_INIT;
         initBall();
         initCups();
         missCount = 0;
         hitCount = 0;
         p_beer.p_scala.setVisibleAll(false);
         p_table.repaint();
         p_table.revalidate();
     }
     
     private void initBall(){
        // System.out.println("INIt BALL");
         ball.setRadius(10);
         ball.setCenterX(INIT_BALL_CX);
         ball.setCenterY(INIT_BALL_CY);
         ball.setCenterZ(INIT_BALL_CZ);
         ball.setSpeedX( 0 );
         ball.setSpeedY( 0 );
         ball.setSpeedZ( 0 );
         ball.setAccelerationZ(-100);
         ball.setOut(false);
     }
     
     private void initCups(){
        
         (cupList.get(0)).setRadius(20);
         (cupList.get(0)).setCenterX(465);
         (cupList.get(0)).setCenterY(TablePanel.HEIGHT / 2 - 20);
         (cupList.get(0)).setCenterZ(0);
         (cupList.get(0)).setHit(false);
         
         (cupList.get(1)).setRadius(20);
         (cupList.get(1)).setCenterX(500);
         (cupList.get(1)).setCenterY(TablePanel.HEIGHT / 2 - 40);
         (cupList.get(1)).setCenterZ(0);
         (cupList.get(1)).setHit(false);
         
         (cupList.get(2)).setRadius(20);
         (cupList.get(2)).setCenterX(500);
         (cupList.get(2)).setCenterY(TablePanel.HEIGHT / 2 );
         (cupList.get(2)).setCenterZ(0);
         (cupList.get(2)).setHit(false);
         
         (cupList.get(3)).setRadius(20);
         (cupList.get(3)).setCenterX(500);
         (cupList.get(3)).setCenterY(TablePanel.HEIGHT / 2 + 40);
         (cupList.get(3)).setCenterZ(0);
         (cupList.get(3)).setHit(false);
         
         (cupList.get(4)).setRadius(20);
         (cupList.get(4)).setCenterX(465);
         (cupList.get(4)).setCenterY(TablePanel.HEIGHT / 2 + 20);
         (cupList.get(4)).setCenterZ(0);
         (cupList.get(4)).setHit(false);
         
         (cupList.get(5)).setRadius(20);
         (cupList.get(5)).setCenterX(430);
         (cupList.get(5)).setCenterY(TablePanel.HEIGHT / 2 );
         (cupList.get(5)).setCenterZ(0);
         (cupList.get(5)).setHit(false);
     }
     
     ActionListener onEachFrame = new ActionListener() 
     {
         public void actionPerformed(ActionEvent evt) 
         {
             if( gameState == GS_INIT)
             {
                
             }
             if( gameState == GS_MOTION)
             {
                 ball.move();
                
                //Check any hit 
                 for( int i = 0; i < cupList.size() ; i++)
                 {
                     if( (ball.getCenterZ() == 0) 
                      && !((Sprite) cupList.get(i)).getHit()  
                      && ((Sprite) cupList.get(i)).getDistance(ball) < ((Sprite) cupList.get(i)).getRadius() )
                     {
                         ((Sprite) cupList.get(i)).setHit(true);
                         hitCount++;
                         gameState = BeerPong.GS_HIT;
                         break;
                     }
                 }
                 
                 if( ball.getOut())
                 {   
                     missCount++;
                     gameState = GS_MISS;
                     ball.setOut(false);
                 }            }
             
             if( gameState == GS_HIT)
             {               
 //                System.out.println("HIT:" + hitCount);
 //                System.out.println("MISS:" + missCount );
                
                 if( hitCount == 6 )
                 {
                    gameState = GS_WON;
                 }
                 else
                 {
                     initBall();
                     gameState = GS_INIT;
                 }
             }
             
             if( gameState == GS_MISS)
             {
 //                System.out.println("HIT:" + hitCount);
 //                System.out.println("MISS:" + missCount );
                 
                 p_beer.p_scala.setVisibleCount(missCount);
                 p_beer.repaint();
                 p_beer.revalidate();
                 
                 if(missCount == 10)
                 {
                     gameState = GS_LOST;
                 }
                 else
                 {
                     initBall();
                     gameState = GS_INIT;
                 }
             }
             
             if( gameState == GS_WON)
             {
             	t.stop();
             	increaseStats();
               // 	System.out.println("WIN");
             }
             
             if( gameState == GS_LOST)
             {
             	t.stop();
             	increaseStats();
                // System.out.println("LOST");
             }
             
             
             //Draw the ball
             p_table.l_ball.setBounds( ball.getCenterX() - ball.getRadius(), 
                     ball.getCenterY() - ball.getRadius(), 
                     ball.getRadius() * 2, ball.getRadius() * 2);
             
         
             for( int i = 0; i < cupList.size(); i++)
             {
                 if( !((Sprite) cupList.get(i)).getHit() )
                 (p_table.labelList.get(i)).setBounds(  ((Sprite) cupList.get(i)).getCenterX() -   ((Sprite) cupList.get(i)).getRadius(), 
                             ((Sprite) cupList.get(i)).getCenterY() -   ((Sprite) cupList.get(i)).getRadius(), 
                             ((Sprite) cupList.get(i)).getRadius() * 2, ((Sprite) cupList.get(i)).getRadius() * 2);
                 else
                 (p_table.labelList.get(i)).setVisible(false);    
             }
             
             p_table.repaint();
             p_table.revalidate();
         }
     };
     
     public void increaseStats(){
     	double stats=0;
     	
     	if(gameState == GS_WON)
     		stats=0.5;
     	else if(gameState == GS_LOST)
     		stats=0.1;
     	
     	switch(day.getCourse())
 		{
 		case SCIENCE: //day 3 resets to nextDay to 1, so 1-1 = 0
 			student.increaseSciRigor(stats);
 			break;
 		case HUMANITIES:
 			student.increaseCreativit(stats);
 			break;
 		default:
 			student.increaseQuantReasoning(stats);
 			break;
 		}
     	backgroundSong.stopSound();
     	
     	if(day.isTranscript())
 		{
 			frame.switchPanel(BackToSchool.Screen.TRANSCRIPT);
 		}
 		else
 		{
 			frame.switchPanel(BackToSchool.Screen.CAMPUS);
 		}
     }
     public static void main(String[] args){
         
     	CardLayout layout = new CardLayout();
 		JPanel cards = new JPanel(layout);
 		BackToSchool b2sFrame = new BackToSchool(layout, cards);     
         BeerPong b = new BeerPong(new Player(), new Day(1), b2sFrame);
         
         JFrame f = new JFrame();
         f.setLocation(WIDTH / 4, HEIGHT / 4);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setVisible(true);
         
         f.add(b);
         f.pack();
     }
     
     private class up extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
			directionSet=true;
 			if(gameState == GS_LOST || gameState == GS_WON )
				resetGame();
			if(gameState == GS_INIT){
 				ball.setSpeedY( ball.getSpeedY() - 2000 );
				//   System.out.println( "Ball_y: " + ball.getSpeedY() );
 			}
 			
 			//System.out.println(gameState);
 		}
 		
     }
     
     private class down extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
			directionSet=true;
 			if(gameState == GS_LOST || gameState == GS_WON )
 				 resetGame();
 			 if(gameState == GS_INIT){
 				ball.setSpeedY( ball.getSpeedY() + 2000 );
 				//System.out.println( "Ball_y: " + ball.getSpeedY() );
 			}
 			
 		//	System.out.println(gameState);
 		}
 	
     }
     private class right extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
			directionSet=true;
 			if(gameState == GS_LOST || gameState == GS_WON )
 				 resetGame();
 			if(gameState == GS_INIT){
 				ball.setSpeedX( ball.getSpeedX() + 2000 );
 				//System.out.println( "Ball_x: " + ball.getSpeedX() );
 				//System.out.println( "X: " + ball.getCenterY() );
 			}
 			
 			//System.out.println(gameState);
 		}
 		
     }
     private class left extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
			directionSet=true;
 			if(gameState == GS_LOST || gameState == GS_WON )
 				 resetGame();
 			if(gameState == GS_INIT){
 				 ball.setSpeedX( ball.getSpeedX() - 2000 );
 				 //System.out.println( "Ball_x: " + ball.getSpeedX() );
 			}
 			//System.out.println(gameState);
 		}
     }
     private class K extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
 			//System.out.println("K");
 //			if(gameState == GS_LOST || gameState == GS_WON )
 //				 resetGame();
 			
 			//System.out.println(gameState);
 		}
 		
     }
     private class R extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
 			resetGame();
 			//System.out.println(gameState);
 		}
     }
     private class X extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
 			if(gameState == GS_LOST || gameState == GS_WON )
 				 resetGame();
 			if(gameState == GS_INIT){
 				ball.setSpeedZ( ball.getSpeedZ() + 2000 );
 				//System.out.println( "Ball_z: " + ball.getSpeedZ() );
 			}
 			
 			//System.out.println(gameState);
 		}
     }
     private class Z extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
 			if(gameState == GS_LOST || gameState == GS_WON )
 				 resetGame();
 			if(gameState == GS_INIT){
 				 ball.setSpeedZ( ball.getSpeedZ() - 2000 );
              //  System.out.println( "Ball_z: " + ball.getSpeedZ() );
 			}
 		//	System.out.println(gameState);
 		}
     }
   
     private class Space extends AbstractAction{
 		public void actionPerformed(ActionEvent e)
 		{
 			if(gameState == GS_LOST || gameState == GS_WON )
 				 resetGame();
			if(gameState == GS_INIT && directionSet){
 				 gameState = GS_MOTION;
				 directionSet=false;
 			}	
 			else if( gameState == GS_HIT )
 			{
 				initBall();
 				gameState = GS_INIT;
 				//System.out.println(hitCount);
 			}
 
 			else if( gameState == GS_MISS )
 			{
 				initBall();
 				gameState = GS_INIT;
 				missCount++;
 				
 				if(missCount == 10)
 					gameState = GS_LOST;
 
 				//System.out.println(missCount);
 				repaint();
 				revalidate();
 			}
 			//System.out.println(gameState);
 		}
     }
     public class Sound // Holds one audio file
 	{
 	  private AudioClip song; // Sound player
 	  private URL songPath; // Sound path
 
 	  Sound(String filename){
 	     try
 	     {
 	    	 //System.out.println("file:" + System.getProperty("user.dir") + "\\" + filename);
 	    	 songPath = new URL ("file:" + System.getProperty("user.dir") + "\\" + filename);
 	    	 song = Applet.newAudioClip(songPath);
 	    	// playSound();
 	     }catch(Exception e){
 	         e.printStackTrace();
 	         //e.getMessage();
 	     } // Satisfy the catch
 	  }
 	  
 	  public void playSound(){
 	     song.loop(); // Play 
 	  }
 	  public void stopSound(){
 	     song.stop(); // Stop
 	  }
 	  public void playSoundOnce(){
 	     song.play(); // Play only once
 	  }
 	}
     
 //     private class MyKeyListener implements KeyListener {
 //
 //        
 //        public void keyTyped(KeyEvent e) {
 //            //throw new UnsupportedOperationException("Not supported yet.");
 //        }
 //
 //        
 //        public void keyPressed(KeyEvent e) {
 //            if( e.getKeyCode() == KeyEvent.VK_K )
 //            {
 //                System.out.println("K");
 //            }
 //            
 //            if( e.getKeyCode() == KeyEvent.VK_R || gameState == GS_LOST || gameState == GS_WON  )
 //            {
 //                resetGame();
 //            }
 //            
 //            if( gameState == GS_INIT )
 //            {
 //                if(e.getKeyCode() == KeyEvent.VK_LEFT)
 //                {
 //                    ball.setSpeedX( ball.getSpeedX() - 2000 );
 //                    System.out.println( "Ball_x: " + ball.getSpeedX() );
 //
 //                }
 //                if(e.getKeyCode() == KeyEvent.VK_RIGHT)
 //                {
 //                    ball.setSpeedX( ball.getSpeedX() + 2000 );
 //                    System.out.println( "Ball_x: " + ball.getSpeedX() );
 //                    System.out.println( "X: " + ball.getCenterY() );
 //                }
 //                if(e.getKeyCode() == KeyEvent.VK_UP)
 //                {
 //                    ball.setSpeedY( ball.getSpeedY() - 2000 );
 //                    System.out.println( "Ball_y: " + ball.getSpeedY() );
 //                }
 //                if(e.getKeyCode() == KeyEvent.VK_DOWN)
 //                {
 //                    ball.setSpeedY( ball.getSpeedY() + 2000 );
 //                    System.out.println( "Ball_y: " + ball.getSpeedY() );
 //                }
 //
 //                if(e.getKeyCode() == KeyEvent.VK_X)
 //                {
 //                    ball.setSpeedZ( ball.getSpeedZ() + 2000 );
 //                    System.out.println( "Ball_z: " + ball.getSpeedZ() );
 //                }
 //
 //                if(e.getKeyCode() == KeyEvent.VK_Z)
 //                {
 //                    ball.setSpeedZ( ball.getSpeedZ() - 2000 );
 //                    System.out.println( "Ball_z: " + ball.getSpeedZ() );
 //                }
 //
 //                if(e.getKeyCode() == KeyEvent.VK_SPACE)
 //                {
 //                    gameState = GS_MOTION;
 //                }
 //            }
 //            
 //            if( gameState == GS_HIT )
 //            {
 //                
 //                if(e.getKeyCode() == KeyEvent.VK_SPACE)
 //                {
 //                    initBall();
 //                    gameState = GS_INIT;
 //                }
 //            }
 //            
 //            if( gameState == GS_MISS )
 //            {
 //                if(e.getKeyCode() == KeyEvent.VK_SPACE)
 //                {
 //                    initBall();
 //                    gameState = GS_INIT;
 //                    missCount++;
 //                    if(missCount == 10)
 //                        gameState = GS_LOST;
 //                    
 //                    repaint();
 //                    revalidate();
 //                }
 //           }
 //            System.out.println(gameState);
 //        }
 //
 //        
 //        public void keyReleased(KeyEvent e) {
 //            //throw new UnsupportedOperationException("Not supported yet.");
 //        }
 //    }
 }
