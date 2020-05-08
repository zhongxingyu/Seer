 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class GameGUI extends JFrame implements KeyListener
 {
 	private static final long serialVersionUID = 1L;
 
 	JPanel mainPanel = new JPanel()
 	{
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		protected void paintComponent(Graphics g)
 		{
 			super.paintComponent(g);
 			
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setColor(Color.LIGHT_GRAY);
 			g2.setFont(new Font("Arial", Font.BOLD, 24));
 			g2.fillRect(390, 0, 20, 620);
 			g2.setColor(Color.GRAY);
 			g2.drawString("Player 1", 50, 20);
 			g2.drawString("Player 2", 625, 20);
 			
 			g2.setColor(Color.BLACK);
 			g2.drawString(Integer.toString(player1Score), 350, 20);
 			g2.drawString(Integer.toString(player2Score), 433, 20);
 			
 			g2.fillRect((int)p1.getX(), (int)p1.getY(), (int)p1.getWidth(), (int)p1.getHeight());
 			g2.fillRect((int)p2.getX(), (int)p2.getY(), (int)p2.getWidth(), (int)p2.getHeight());
 			g2.fillRect((int)ball.getX(), (int)ball.getY(), (int)ball.getWidth(), (int)ball.getHeight());
 		}
 	};
 	
 	public static DatagramSocket clientSocket;
 	public static DatagramPacket sendPacket;
 	public static DatagramPacket receivePacket;
 	public static byte[] sendData = new byte[1024];
 	public static byte[] receiveData = new byte[1024];
 	
 	public static Graphics2D g2;
 	
 	public static Rectangle p1 = new Rectangle(20, 240, 20, 100);
 	public static Rectangle p2 = new Rectangle(750, 240, 20, 100);
 	public static Rectangle ball = new Rectangle(395, 295, 10, 10);
 	
 	public static int player = 1;
 	public static int player1Score = 0;
 	public static int player2Score = 0;
 	
 	public static Thread thread;
 	
 	GameGUI()
 	{
 		super("ECE 369 - JPong (" + Resource.VERSION_NUMBER + " - " + Resource.VERSION_CODENAME + ")");
 		FlowLayout fl = new FlowLayout();
 		fl.setAlignment(FlowLayout.LEFT);
 		setLayout(fl);
 		mainPanel.setPreferredSize(new Dimension(800, 600));
 		
 		add(mainPanel);
 
 		try
 		{
 			sendData = "/connected".getBytes();
 			
 			clientSocket = new DatagramSocket(Integer.parseInt(Resource.PORT));
 			sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(Resource.IP), Integer.parseInt(Resource.PORT));
 			receivePacket = new DatagramPacket(receiveData, receiveData.length);
 			// Send Ready
 			clientSocket.send(sendPacket);
 			
 			// Wait for Player ID
 			clientSocket.receive(receivePacket);
 			if((new String(receivePacket.getData())).trim().contains("/player"))
 			{
 				player = Integer.parseInt((new String(receivePacket.getData())).trim().split(" ")[1]);
 
 				// Wait for GO (Other Player to be Ready)
 				receiveData = new byte[1024];
 				receivePacket = new DatagramPacket(receiveData, receiveData.length);
 				clientSocket.receive(receivePacket);
 				if(!(new String(receivePacket.getData()).trim()).contains("/go"))
 					System.out.println("Server Goofed Up with the GO Signal");
 			}
 			else
 				System.out.println("Server Goofed Up with your ID!");
 		}
 		catch (Exception e) { e.printStackTrace(); }
 
 		// Info in, Players Ready, Commence!
 		thread = (new Thread()
 		{
 			@Override
 			public void run()
 			{
 				while(this.isAlive())
 				{
 					sendData = getPlayerCoordinates(player).getBytes();
 					try
 					{
 						sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(Resource.IP), Integer.parseInt(Resource.PORT));
 						clientSocket.send(sendPacket);
 					}
 					catch(Exception e) { e.printStackTrace(); }
 					
 					receiveData = new byte[1024];
 					receivePacket = new DatagramPacket(receiveData, receiveData.length);
 					try { clientSocket.receive(receivePacket); }
 					catch(Exception e) { e.printStackTrace(); }
 
 					String incomingMessage = new String(receivePacket.getData()).trim();
 					System.out.println(incomingMessage);
 					
 					if(incomingMessage.contains("/coordinates"))
 						updateCoordinates(incomingMessage);
 					else if(incomingMessage.contains("/score"))
 						updateScore(incomingMessage);
 				}
 			}
 		});
 		thread.start();
 		
 		addKeyListener(this);
 	}
 
 	public static String getPlayerCoordinates(int player)
 	{
 		switch(player)
 		{
 			case 1:
 				return "/coords 1\\" + p1.getY();
 			case 2:
 				return "/coords 2\\" + p2.getY();
 		}
 		
 		return null;
 	} 
 	
 	public static void updateScore(String incomingMessage)
 	{
 		int player = Integer.parseInt(incomingMessage.split(" ")[1]);
 		
 		if(player == 1)
 			player1Score++;
 		else
 			player2Score++;
 	}
 	
 	@Override
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 	
 	public static void updateCoordinates(String incomingMessage)
 	{
 		incomingMessage = incomingMessage.substring(13);
 		
 		String[] coords = incomingMessage.split("\\\\");
 		if(player == 1)
 			p2.move((int)p2.getX(), Integer.parseInt(coords[0].split("\\.")[0]));
 		else
 			p1.move((int)p1.getX(), Integer.parseInt(coords[0].split("\\.")[0]));
 		
		ball.move(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		if((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_W))
 		{
 			if((player == 1) && (p1.getY() > 10))
 				p1.move((int)(p1.getX()), (int)p1.getY() - 20);
 			else if((player == 2) && (p2.getY() > 10))
 				p2.move((int)(p2.getX()), (int)p2.getY() - 20);
 		}
 		if((e.getKeyCode() == KeyEvent.VK_DOWN) || (e.getKeyCode() == KeyEvent.VK_S))
 		{
 			if((player == 1) && (p1.getY() < 490))
 				p1.move((int)(p1.getX()), (int)p1.getY() + 20);
 			else if((player == 2) && (p2.getY() < 490))
 				p2.move((int)(p2.getX()), (int)p2.getY() + 20);
 		}
 		
 		repaint();
 	}
 	
 	@Override
 	public void keyTyped(KeyEvent e) {}
 	@Override
 	public void keyReleased(KeyEvent e) {}
 }
