 package pps.et.client;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import pps.et.logic.ConnectionHandler;
 import pps.et.logic.GameHandler;
 import pps.et.logic.GameMap;
 import pps.et.logic.Player;
 import pps.et.logic.entity.Entity;
 
 import java.awt.image.BufferedImage;
 
 class ClientSwingRewrite extends JFrame implements KeyListener {
     
 	private ConnectionHandler connection;
 	private Player player;
 	private GameHandler game;
 	GameMap map;
 	
 	protected JLabel chatPane;
 	 
 	 
 	PaintPanel canvas;
     
     public ClientSwingRewrite(final ClientConnectionHandler connection, final Player player, final GameHandler game) {
     	this.connection	= connection;
 		this.player 	= player;
 		this.game 		= game;
 		this.map 		= game.getMap();
     	
 		
 		canvas = new PaintPanel(connection, player, game);
 		
     	//--- create the buttons
         JButton connectButton = new JButton("Connect");
         connectButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	//TODO
             }});
         
         
         JButton disconnectButton = new JButton("Disconnect");
         disconnectButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	//TODO
             }});
         
         chatPane = new JLabel();
         chatPane.setBackground(Color.white);
         chatPane.setAlignmentY(TOP_ALIGNMENT);
         chatPane.setText("Welcome to the game.");
         
       
        // JPanel buttonPanel = new JPanel();
        // buttonPanel.setLayout(new GridLayout(2, 1));
        //        buttonPanel.add(connectButton);
    //        buttonPanel.add(disconnectButton);
        // buttonPanel.add(chatPane);
        // buttonPanel.add(chatInput);
         
         
         Container content = this.getContentPane();
         
         content.setLayout(new BorderLayout());
         content.add(canvas, BorderLayout.CENTER);
        // content.add(buttonPanel, BorderLayout.EAST);
         
         
         this.setTitle("PPS13Project");
         
         this.setResizable(false);
         
         this.setVisible(true);
         
         this.pack();
         
     }
     
 
 	public void start(){
         canvas.run();
     }
 
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
 
 class PaintPanel extends JPanel implements MouseListener, 
                                            MouseMotionListener, 
                                            Runnable,
                                            KeyListener {
 
 	private ConnectionHandler connection;
 	private Player player;
 	private GameHandler game;
 	GameMap map;
 
 	private Image wall, grass, crate, fire, player_one, tnt, death, opponent, team_mate, treasure;
 	
     
     private BufferedImage _bufImage = null;
     
     private static final int SIZE = 610;
     
     public PaintPanel(final ClientConnectionHandler connection, final Player player, final GameHandler game) {
     	
     	this.connection	= connection;
 		this.player 	= player;
 		this.game 		= game;
 		this.map 		= game.getMap();
 		
 		
         setPreferredSize(new Dimension(SIZE, SIZE));
         setBackground(Color.white);
       
         this.setFocusable(true);
         this.requestFocusInWindow();
         
         
         this.addKeyListener(this);
          
         this.addMouseListener(this); 
         this.addMouseMotionListener(this);
         
         player_one = Toolkit.getDefaultToolkit().getImage("../p1.png");
         wall = Toolkit.getDefaultToolkit().getImage("../wall.png");
         grass = Toolkit.getDefaultToolkit().getImage("../grass.jpg");
         crate = Toolkit.getDefaultToolkit().getImage("../crate.png");
         fire = Toolkit.getDefaultToolkit().getImage("../fire.png");
         tnt = Toolkit.getDefaultToolkit().getImage("../TNT.png");
         death = Toolkit.getDefaultToolkit().getImage("../death.png");
         opponent = Toolkit.getDefaultToolkit().getImage("../opponent.png");
         team_mate = Toolkit.getDefaultToolkit().getImage("../team_mate.png");
         treasure = Toolkit.getDefaultToolkit().getImage("../treasure.png");
         
     }
     
     
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         
         Graphics2D g2 = (Graphics2D)g;  // downcast to Graphics2D
         if (_bufImage == null) {
            
         	//--- This is the first time, initialize _bufImage
             int w = this.getWidth();
             int h = this.getHeight();
             _bufImage = (BufferedImage)this.createImage(w, h);
             Graphics2D gc = _bufImage.createGraphics();
             
             gc.setColor(Color.white);
             gc.fillRect(0, 0, w, h); // fill in background
         }
         
         g2.drawImage(_bufImage, null, 0, 0);  // draw previous shapes
         
         draw(g2);
     }
     
     
     private void draw(Graphics2D g2) {	
     
     	g2.drawImage(grass, 0, 0, 610, 610, this);	
     	
     	for(Entity e : map.getEntities()){
     		if (e == null)
     			break;
     		
     		if(e.getType().equals("Wall")){
     			g2.drawImage(wall, e.getX()*10, e.getY()*10, this);
     			
     		}
     		
     		if (e.getType().equals("Mine")){
     			g2.drawImage(tnt, e.getX()*10, e.getY()*10, 10, 10, this);
     		}
     		
     		if (e.getType().equals("Damage")){
 				g2.drawImage(fire,e.getX()*10,e.getY()*10, this);
     		}
     		
     		if (e.getType().equals("Barrier")){
     			g2.drawImage(crate, e.getX()*10, e.getY()*10, this);
     		}
     		
     		if (e.getType().equals("Box")){
     			g2.drawImage(treasure, e.getX()*10, e.getY()*10, this);
     		}
     		
     	}
     	
     	for (Player p : game.players) {
 			if (p.getID() != player.getID()){
 				if(!p.isAlive()){
 					g2.drawImage(death, player.getX()*10, player.getY()*10, this);					
 				}else{
 					if(p.getTeam() == 0)
 						g2.drawImage(team_mate, p.getX()*10, p.getY()*10, this);
 					else
 						g2.drawImage(opponent, p.getX()*10, p.getY()*10, this);
 				}
 			}
 		}
     	//Self
     	g2.drawImage(player_one, player.getX()*10, player.getY()*10, this);
     	
     	if(!player.isAlive()){
     		g2.drawImage(death, player.getX()*10, player.getY()*10, this);
     	}
     	
     }
 
     public void mousePressed(MouseEvent e) {  
     }
 
     public void mouseDragged(MouseEvent e) {
     }
     
    
     public void mouseReleased(MouseEvent e) {
         
 
     }    
     
     
     public void mouseMoved   (MouseEvent e) {}
     public void mouseEntered (MouseEvent e) {}
     public void mouseExited  (MouseEvent e) {}
     public void mouseClicked (MouseEvent e) {}
 
 
     
     /**
      * This should repaint it all
      */
 	@Override
 	public void run() {
 		while(true){
 
 			if (_bufImage != null){
 				Graphics2D grafarea = _bufImage.createGraphics();
 
 		        draw(grafarea);
 				
 				this.repaint();
 			}else{
 				System.err.println("Broke..");
 			}
 		}
 	}
 
 	
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyCode() == 37 || e.getKeyCode() == 65) {
 			// left or a
 			game.movePlayer(player, "L");
 			connection.send("move L");
 		} else if (e.getKeyCode() == 38 || e.getKeyCode() == 87) {
 			// up or w
 			game.movePlayer(player, "D");
 			connection.send("move D");
 		} else if (e.getKeyCode() == 39 || e.getKeyCode() == 68) {
 			// 	right or d
 			game.movePlayer(player, "R");
 			connection.send("move R");
 		} else if (e.getKeyCode() == 40 || e.getKeyCode() == 83) {
 			// down or s
 			game.movePlayer(player, "U");
 			connection.send("move U");
 		} else if (e.getKeyCode() == 27) {
 			//escape
 			connection.quit();
 		} else if (e.getKeyCode() == 32) {
 			// Space
 			
 			connection.send("build Mine at " + player.getPos());
 			game.build(player, "Mine", player.getX(), player.getY());
 			
 		} else if (e.getKeyCode() == 10) {
 			// enter
 		} else if (e.getKeyCode() == 84) {
 			// t
 			// for talk/chat
 		} else if (e.getKeyCode() == 66) {
 			// build
 			// "player :id build :what at :x :y
 			
 			connection.send("build Mine at " + player.getPos());
 			game.build(player, "Mine", player.getX(), player.getY());
 		} else {
 		
 		}
 	}
 
 
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 }
