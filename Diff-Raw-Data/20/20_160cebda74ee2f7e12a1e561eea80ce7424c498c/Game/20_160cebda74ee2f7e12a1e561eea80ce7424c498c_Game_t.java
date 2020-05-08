package SharkBait;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import javax.swing.JComponent;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 
 //      for radio buttons
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.ButtonGroup;
 import java.awt.GridLayout;
 import java.awt.Dimension;
 
 public class Game extends JComponent implements MessageListener, KeyListener, ActionListener
 {
 
     public static final int						REFRESH_RATE = 60;
     public static final int						PIXELS_PER_METER = 1;
     public static final int						PLAYER_X_CENTER = 500;
     public static final int						PLAYER_Y_CENTER = 300;
     public static final int						SLOOP_TURN_MAX = 3;
     public static final int						FRIGATE_TURN_MAX = 2;
     public static final int						MAN_OF_WAR_TURN_MAX = 1;
     public static final double					TURN_AMOUNT_INCREMENT = 0.5;
     public static final double					SPEED_AMOUNT_INCREMENT = 0.10;
     
 	private static final int					windowWidth  = 1024;
 	private static final int					windowHeight = 768;
 	
 	
     private GUI                                 gameGUI;
 	private	boolean								windUpdateFLAG = false;
 	private boolean								shiftPressed = false;
 	
     protected HashMap<String, Ship>				shipList;
     private ArrayList<Integer>					shipID;
     public	HashMap<String, String>				speedTable;
     private World                               gameWorld = null;
     private int                                 playerID;
     private boolean                             gameRunning = false;
     private int                                 shipSelection = 0;
     private String                              pMessage;
     private int                                 targetID = -1;
     private long 								lastTurn = 0;
     private Lobby								lobbyWindow;
         
     private     JFrame                          frame;
         
     public  Communication						comm;
     public  Thread								commThread;
 	public	String								serverIP;
         
         //      communication / message variables
         
         public  double                          turnAmount = 0.00;
         public  double                          speedAmount = 0.00;
         
         
         
     public Game( String pServerIP )
 	{
 		this.serverIP = pServerIP;
 		lobbyWindow = new Lobby(this);
     	
     	this.lobbyWindow.createLobby();
         this.lobbyWindow.lobbyWindow.setVisible(true );
         
 //        System.out.println(SpeedTable.speedTable[45]);
 			
         this.shipList = new HashMap<String, Ship>();
         this.shipID = new ArrayList<Integer>();
         this.gameWorld = new World( );
 
 		//	pause the system HERE
 
 		//	until communication is established?
         this.frame = new JFrame ("Shark Bait");
         this.gameGUI = new GUI( );
 
         
         this.frame.setResizable(true);
         this.frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
         this.frame.setResizable( false );
         this.frame.pack();
         this.frame.setSize(this.windowWidth, this.windowHeight);
         this.frame.add( this, BorderLayout.CENTER );
 	
 		this.frame.setFocusTraversalKeysEnabled(false);
         this.frame.addKeyListener( this );
         //Thread thread = new Thread(this);
         //thread.start();
         this.commThread = new Thread(this.comm = new Communication(this, pServerIP, 5283));
         this.commThread.start();
         while(true){
 			try {
 				Thread.sleep(1000 / REFRESH_RATE);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
                 e.printStackTrace();
 			}
             if(this.gameRunning)
             {
                 this.update();
             }
 		}
         
 	}
        
 	public void paint ( Graphics g )
     {        
 		Point playerPos = null;
 		
 		if ( this.shipList.size() != 0 ) {
 			playerPos = this.shipList.get(Integer.toString(this.playerID)).getPosition();
         }
         if ( playerPos != null )
         {
             //      draw world
         	this.gameWorld.draw(g, playerPos);
 
             //      draw ships
         
         	for (String key : this.shipList.keySet()) {
         		this.shipList.get(key).draw(g, playerPos, targetID);
         	}
 
 			//  draw chrome
 			this.gameGUI.draw( g, this.shipList, this.playerID, this.targetID );
             if(shipList.size() > 0)
             {
                 for (String key : this.shipList.keySet()) {
                     this.shipList.get(key).draw(g, playerPos, targetID);
                 }//  draw chrome
             }
         }
 
     }
     public void update()
     {
         //update ships
         for (String key : shipList.keySet()) {
             this.shipList.get(key).update(this.gameWorld.getWindDirection());
         }
         //update world
 		if (this.windUpdateFLAG == true) {
 			this.gameGUI.update( this.gameWorld.getWindDirection(), this.gameWorld.getWindSpeed() );
 			this.windUpdateFLAG = false;
 		}
         //update chrome
         this.repaint();
     }
     public void actionPerformed( ActionEvent e )
     {
             System.out.println( "A button is clicked" );
 
             if ( e.getActionCommand().equals( "sloop_selected" ) )
             {
                     System.out.println( "SLOOP class ship selected" );
                     //      set the variable for the user's ship preference
                     this.shipSelection = 0;
             }
             if ( e.getActionCommand().equals( "frigate_selected" ) )
             {
                     System.out.println( "FRIGATE class ship selected" );
                     //      set the variable for the user's ship preference
                     this.shipSelection = 1;
             }
             if ( e.getActionCommand().equals( "man-o-war_selected" ) )
             {
                     System.out.println( "MAN-O-WAR class ship selected" );
                     //      set the variable for the user's ship preference
                     this.shipSelection = 2;
             }       
                     
             if ( e.getActionCommand().equals( "register" ) )
             {
                     System.out.println( "REGISTER button clicked" ) ;
                     this.lobbyWindow.sloopButton.setEnabled( false );
                     this.lobbyWindow.frigateButton.setEnabled( false );
                     this.lobbyWindow.manOwarButton.setEnabled( false );
                     this.lobbyWindow.registerButton.setEnabled( false );
                     this.lobbyWindow.registerButton.setText( "Waiting for registration..." );
                     //      send REGISTER message to server
                     this.comm.sendMessage( "register:"+shipSelection+";" );
                     
             }
             if ( e.getActionCommand().equals( "ready" ) )
             {
                     System.out.println( "READY button clicked" ) ;
                     this.lobbyWindow.readyButton.setEnabled( false );
                     this.lobbyWindow.readyButton.setText( "Waiting to start..." );
                     //      send READY message to server
                     this.comm.sendMessage( "ready;" );
                     
             }
             
 
     }
     public void keyTyped( KeyEvent pEvent )
     {
                 
     }
     public void keyPressed( KeyEvent pEvent )
     {
 		
         //      System.out.println("Key Pressed!!!");
                 if( pEvent.getKeyCode( ) == KeyEvent.VK_UP ){
             System.out.println("Up!");
                         //      change the current speed of the player's ship
                         this.speedAmount += SPEED_AMOUNT_INCREMENT;
 
                         if ( this.speedAmount > 1.00 )
                         {
                                 this.speedAmount = 1.00;
                         }
                         
                         this.pMessage = "speed:" +  this.speedAmount + ";";                     
                         comm.sendMessage(this.pMessage);
                         
                 }
                 else if( pEvent.getKeyCode( ) == KeyEvent.VK_DOWN )
         {
             System.out.println("Down!");
                         //      change the current speed of the player's ship
                         this.speedAmount -= SPEED_AMOUNT_INCREMENT;
 
                         if ( this.speedAmount < 0.00 )
                         {
                                 this.speedAmount = 0.00;
                         }
                         
                         this.pMessage = "speed:" +  this.speedAmount + ";";                     
                         comm.sendMessage(this.pMessage);
                 }
                 else if( pEvent.getKeyCode() == KeyEvent.VK_LEFT )
                 { 
                 	 System.out.println("Left!");
                      
                      int  turn = 0;
                      
                      if (this.shipList.get(Integer.toString(this.playerID)).getType() == 0)
                      {
                      	turn = -1*SLOOP_TURN_MAX;
                      }
                      
                      if (this.shipList.get(Integer.toString(this.playerID)).getType() == 1)
                      {
                      	turn = -1*FRIGATE_TURN_MAX;
                      }
                      
                      if (this.shipList.get(Integer.toString(this.playerID)).getType() == 2)
                      {
                      	turn = -1*MAN_OF_WAR_TURN_MAX;
                      }
          			//	add to the amount of the turn, to a maximum of -25
          			//	turn amount is in degrees?
          			/*if ( this.turnAmount >= -maxIncrement)
          			{
          				this.turnAmount -= TURN_AMOUNT_INCREMENT;
          			}
          			else
          			{
          				this.turnAmount = - maxIncrement;
          			}
          			*/
                     System.out.println(System.currentTimeMillis() - this.lastTurn);
                     if(System.currentTimeMillis() - this.lastTurn > 100 || this.lastTurn == 0)
                     {
                         this.pMessage = "setHeading:" + turn + ";";
                         comm.sendMessage(pMessage);
                         this.lastTurn = System.currentTimeMillis();
                     }
 
          		}
                 else if( pEvent.getKeyCode() == KeyEvent.VK_RIGHT ) 
                 {
                     int  turn = 0;
                     
                     if (this.shipList.get(Integer.toString(this.playerID)).getType() == 0)
                     {
                      	turn = SLOOP_TURN_MAX;
                     }
                     
                     if (this.shipList.get(Integer.toString(this.playerID)).getType() == 1)
                     {
                      	turn = FRIGATE_TURN_MAX;
                     }
                     
                     if (this.shipList.get(Integer.toString(this.playerID)).getType() == 2)
                     {
                      	turn = MAN_OF_WAR_TURN_MAX;
                     }
          			//	add to the amount of the turn, to a maximum of -25
          			//	turn amount is in degrees?
          			/*if ( this.turnAmount >= -maxIncrement)
                      {
                      this.turnAmount -= TURN_AMOUNT_INCREMENT;
                      }
                      else
                      {
                      this.turnAmount = - maxIncrement;
                      }
                      */
                     System.out.println(System.currentTimeMillis() - this.lastTurn);
                     if(System.currentTimeMillis() - this.lastTurn > 100 || this.lastTurn == 0)
                     {
                         this.pMessage = "setHeading:" + turn + ";";
                         comm.sendMessage(pMessage);
                         this.lastTurn = System.currentTimeMillis();
                     }
         		}
                 else if( pEvent.getKeyCode() == KeyEvent.VK_TAB ) 
 				{
 					if (this.targetID == -1 && this.shipID.size() != 0 ){
                     this.targetID = shipID.get(0);
                 }
                 
                 for(int i = 0; i < shipID.size(); i++){
 					if (shipID.get(i).intValue() == targetID){
 						
 /*						if ( pEvent.isShiftDown() )
 						{
 							if ( i > 0 )
 							{
 								this.targetID = shipID.get((i-1) % shipID.size()).intValue();
 							}
 							else
 							{
 								this.targetID = shipID.get((shipID.size()-1) % shipID.size()).intValue();
 							}
 
 							System.out.println( "\tShift is pressed." );
 						}
 						else {
 */							this.targetID = shipID.get((i+1) % shipID.size()).intValue();
 //						}
 
                         break;
 					}
 				}
                 System.out.println(this.targetID);
 				System.out.println("TAB!");
 			}
         else if( pEvent.getKeyCode() == KeyEvent.VK_SPACE)
         {
 			System.out.println("Space!");
 			this.pMessage = "fire:" + this.targetID + ";";
 			comm.sendMessage(pMessage);
         }
         }
     public void keyReleased(KeyEvent e)
     {
                 
                 //      send message
                 /*System.out.println( "sending message to the server" );
                 System.out.println( "Turning " + turnAmount + " degrees." );
                 System.out.println( "Speed requested " + speedAmount );
                 //      clear turnAmount, speedChange, and other message specific variables
                 this.turnAmount = 0;*/
     }
     public void handleMessage(Message pMessage)
         {
         /*
          * Gameplay Messages
          */
         if( pMessage.getMessageName().equals( "shipState" ) )
         {
                     this.shipList.get(pMessage.getArgument( 0 )).updateShip( 
                                         Integer.parseInt( pMessage.getArgument( 1 ) ),          // int x
                                         Integer.parseInt( pMessage.getArgument( 2 ) ),          // int y
                                         Double.parseDouble( pMessage.getArgument( 3 ) ),        // double speed
                                         Double.parseDouble( pMessage.getArgument( 4 ) ),          // int direction
                                         Double.parseDouble( pMessage.getArgument( 5 ) )         // double damage
                                           );
             
         }
         else if( pMessage.getMessageName().equals( "ship" ) )
         {
             this.shipList.put( pMessage.getArgument( 0 ), new Ship( Integer.parseInt( pMessage.getArgument( 0 ) ), 
                                         Integer.parseInt(pMessage.getArgument( 1 ) ), this.playerID ) );
             if (Integer.parseInt( pMessage.getArgument( 0 )) != playerID){
                 shipID.add(new Integer( pMessage.getArgument( 0 )));
             }
         }
         else if( pMessage.getMessageName().equals( "wind" ) )
         {
 			this.windUpdateFLAG = true;
             this.gameWorld.setWindDirection( Integer.parseInt( pMessage.getArgument( 0 ) ) );
 			this.gameGUI.setWindDirection( Integer.parseInt( pMessage.getArgument( 0 ) ) );
 			
 /*
 			//	is windSpeed part of the message?
             this.gameWorld.setWindSpeed( Integer.parseInt( pMessage.getArgument( 1 ) ) );
             this.gameGUI.setWindSpeed( Integer.parseInt( pMessage.getArgument( 1 ) ) );
 */
 		}
         else if( pMessage.getMessageName().equals( "fog" ) )
         {
             this.gameWorld.setFog( Integer.parseInt( pMessage.getArgument( 0 ) ) );
         }
         else if( pMessage.getMessageName().equals( "rain" ) )
         {
             this.gameWorld.setRain( Integer.parseInt( pMessage.getArgument( 0 ) ) );
         }
         else if( pMessage.getMessageName().equals( "time" ) )
         {
             this.gameWorld.setTime( Integer.parseInt( pMessage.getArgument( 0 ) ) );
         }
         else if( pMessage.getMessageName().equals( "firing" ) )
         {
        	System.out.println(Integer.parseInt( pMessage.getArgument( 0 )));
 //			this.shipList.get( Integer.parseInt( pMessage.getArgument( 0 )) ).setFiring( true );
			this.shipList.get( pMessage.getArgument( 0 ) ).setFiring( true );
 
         }
         /*
          * Setup Messages
          */
         else if( pMessage.getMessageName().equals( "start" ) )
         {
             this.gameRunning = true;
             // close lobby window
             this.lobbyWindow.lobbyWindow.setVisible( false );
             this.frame.setVisible(true);
             // show game GUI
             // this.gameGUI.addKeyListener( this );
             // this.gameGUI.addToLayer(this, 1);
             // this.gameGUI.setVisible( true );
             
         }
         else if( pMessage.getMessageName().equals( "registered" ) )
         {
             this.playerID = Integer.parseInt( pMessage.getArgument( 0 ) );
                         //      set register button to "registered"
                         this.lobbyWindow.registerButton.setText( "Registered!" );
                         this.lobbyWindow.readyButton.setText( "Waiting for map" );
         }
         else if( pMessage.getMessageName().equals( "shore" ) )
         {
             // if shore x set ready button active
             if( pMessage.getArgumentsNum() == 1 && pMessage.getArgument(0).equals("x") )
             {
 //                              this.lobbyWindow.setVisible( true );
                                 this.lobbyWindow.readyButton.setEnabled( true );
                                 this.lobbyWindow.readyButton.setText( "Ready to start?" );
             }
             // handle shore 
             else
             {   
                 //ArrayList mX = new ArrayList<Integer> ( );
                 //ArrayList mY = new ArrayList<Integer> ( );
                 int [] mX = new int[ Integer.parseInt( pMessage.getArgument( 0 ))];
                 int [] mY = new int[ Integer.parseInt( pMessage.getArgument( 0 ))];
                 
                 
                 for( int i = 0; i < Integer.parseInt( pMessage.getArgument( 0 ) ); i++)
                 {
                     mX[i] = ( Integer.parseInt( pMessage.getArgument( 2 * i + 1 ) ) );
                     mY[i] = ( Integer.parseInt( pMessage.getArgument( 2 * i + 2 ) ) );
                     
             
                 }
                 this.gameWorld.addShore( mX, mY, Integer.parseInt( pMessage.getArgument( 0 ) ) );
             }
         }
         /*
          * Endgame Messages
          */
         else if( pMessage.getMessageName().equals( "gameover" ) )
         {
             this.comm.closeThread( );
             if( !this.commThread.isAlive( ) )
             {
                 this.comm.disconnect( );
             }
         }
     }
     public synchronized Ship getShip(String key){
     	return this.shipList.get(key);
     }
     public static void main(String[] args)
         {
                 Game game = new Game( args[0] );
         }
 }
