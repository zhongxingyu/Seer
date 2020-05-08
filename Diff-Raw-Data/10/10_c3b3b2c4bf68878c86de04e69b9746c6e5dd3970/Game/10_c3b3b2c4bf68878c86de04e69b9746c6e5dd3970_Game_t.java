 package client;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class Game extends BasicGame {
 
 	private final Map<Byte, Location> players = new HashMap<Byte, Location>();
 
 	DatagramSocket socket;
 	InetAddress address;
 	int sendPort = 4444;
 
 	int radius = 30;
 	int time;
 
 	Image semi;
 
 	public Game(String title, String serverAddress) throws IOException,
 			SlickException {
 		super(title);
 
 		socket = new DatagramSocket();
 		address = InetAddress.getByName(serverAddress);
 
 		ClientListener listener = new ClientListener(players);
 		listener.start();
 
 	}
 
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		semi = new Image("new semi.png");
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		g.setColor(Color.blue);
 		g.fillRect(0, 0, 800, 600);
 
 		g.setColor(Color.black);
 		g.fillRect(0, 550, 800, 50);
 
 		g.setColor(Color.white);
 		g.fillRect(398, 500, 4, 50);
 
 		for (Entry<Byte, Location> entry : players.entrySet()) {
 
 			int x = entry.getValue().x + 400;
 			int y = 550 - entry.getValue().y;
 
 			semi.draw(x - radius, y - radius, radius * 2, radius);
 		}
 
 	}
 
 	@Override
 	public void update(GameContainer container, int delta)
 			throws SlickException {
 
 		time += delta;
 
 		if (time >= 5) {
 			byte[] buf = new byte[] { 0 };
 
 			DatagramPacket packet = new DatagramPacket(buf, buf.length,
 					address, sendPort);
 
 			try {
 				socket.send(packet);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 
 			Input input = container.getInput();
 
 			byte x = 0, y = 0;
 
 			if (input.isKeyPressed(Input.KEY_SPACE)) {
 				y++;
 			}
 
 			if (input.isKeyDown(Input.KEY_RIGHT)) {
 
 				x++;
 			}
 
 			if (input.isKeyDown(Input.KEY_LEFT)) {
 
 				x--;
 			}
 
 			if (x != 0 || y != 0) {
 				buf = ByteBuffer.allocate(8).putInt(x).putInt(y).array();
 
 				packet = new DatagramPacket(buf, buf.length,
 						address, sendPort);
 
 				try {
 					socket.send(packet);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			time = 0;
 		}
 
 	}
 	
 	@Override
 	public boolean closeRequested(){
 		byte[] buf = new byte[] { 1 };
 
 		DatagramPacket packet = new DatagramPacket(buf, buf.length,
 				address, sendPort);
 
 		try {
 			socket.send(packet);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		
 		return true;
 	}
 }
