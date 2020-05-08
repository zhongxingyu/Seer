 package client;
 
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.gui.MouseOverArea;
 import org.newdawn.slick.gui.TextField;
 
 
 public class Client extends BasicGame {
 	Image sendButton;
 	Image newgameButton;
 	Image joingameButton;
	TextField txt;
 	static AppGameContainer agc;
 	ClientConnectionHandler cch;
 	public Client(String title,String host, String port) {
 		super(title);
 		try {
 			cch = new ClientConnectionHandler(InetAddress.getByName(host), Integer.parseInt(port));
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 	//args0 = hostname, args1 = port
 	public static void main(String args[]){
 		
 		try {
 			
 			Client client = new Client("client",args[0],args[1]);
 			agc = null;
 			try {
 				agc = new AppGameContainer(client);
 			} catch (SlickException e1) {
 				e1.printStackTrace();
 			}
 			try {
 				agc.setDisplayMode(1024, 768, false);
 			} catch (SlickException e) {
 			}
 			agc.setTargetFrameRate(60);
 			try {
 				agc.start();
 			} catch (SlickException e) {
 				e.printStackTrace();
 			}
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		}
 
 	 @Override
 	    public boolean closeRequested()
 	    {
 		 	cch.closeConnection();
 		 	agc.exit();
 	      return false;
 	    }
 	
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) throws SlickException {
 	
 		arg1.drawImage(sendButton, 400, 300);
 		arg1.drawImage(newgameButton, 200, 300);
 		arg1.drawImage(joingameButton, 600, 300);
		txt.render(arg0, arg1);
 	}
 
 	@Override
 	public void init(GameContainer arg0) throws SlickException {
 		sendButton = new Image("resources/gfx/SendButton.png");
 		joingameButton = new Image("resources/gfx/JoingameButton.png");
		newgameButton = new Image("resources/gfx/newgameButton.png");

		txt  = new TextField(arg0, arg0.getDefaultFont() , 400, 250, sendButton.getWidth(), 30); 
		txt.setBackgroundColor(new Color(0,0,0));
 		Music bgMusic = new Music("resources/audio/Invincible.ogg");
 		bgMusic.loop();
 		
 	}
 
 	@Override
 	public void update(GameContainer arg0, int arg1) throws SlickException {		
 		
 		/*
 		 * Sends chat message to server
 		 */
 		MouseOverArea moa = new MouseOverArea(arg0, sendButton, 400, 300, sendButton.getWidth(), sendButton.getHeight());
		if(moa.isMouseOver() && Mouse.isButtonDown(0) && !(txt.getText().equals(""))){ 
			System.out.println("PRESSED! Message is: "+ txt.getText()); 
			cch.sendMessage(txt.getText());
			txt.setText("");
 		}
 		/*
 		 * Creates new game in server
 		 */
 		moa = new MouseOverArea(arg0, newgameButton, 200, 300, newgameButton.getWidth(), newgameButton.getHeight());
 		if(moa.isMouseOver() && Mouse.isButtonDown(0)){ //Enters several times...
 			System.out.println("PRESSED! NEW GAME"); 
 			//IMPLEMENT
 		}
 		/*
 		 * Joins existing game
 		 */
 		moa = new MouseOverArea(arg0, joingameButton, 600, 300, joingameButton.getWidth(), joingameButton.getHeight());
 		if(moa.isMouseOver() && Mouse.isButtonDown(0)){   //Enters several times...
 			System.out.println("PRESSED! JOIN GAME"); 
 			//IMPLEMENT
 		}
 	}
 }
