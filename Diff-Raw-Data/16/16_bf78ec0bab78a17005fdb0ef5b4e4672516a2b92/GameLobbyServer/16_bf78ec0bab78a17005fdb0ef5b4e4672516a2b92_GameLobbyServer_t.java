 package edu.brown.cs32.goingrogue.game;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import edu.brown.cs32.goingrogue.network.RogueServer;
 
 public class GameLobbyServer extends GameLobbyState {
	private String gameIP;
 	
 	public GameLobbyServer(String bg, String md, int id, MenuGame game){
 		super(bg, md, id, game);
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame gm)
 			throws SlickException {
 		super.init(gc, gm);
 		inputFields.get(0).setText(game.getUserName());
 		inputFields.get(1).setText("" + game.getPortNumber());
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics g)
 			throws SlickException {
 		super.render(gc, game, g);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		if(port != null){
 			this.setPlayerNames(port.getPlayerNames());
 			String msg = "";
 			if(getPlayerNames() != null){
 				for(String elem : getPlayerNames()){
 					msg += "\n" + elem;
 				}
 				msg = msg.substring(1);
 				try{
					if(gameIP == null){
						URL ipcheck = new URL("http://checkip.amazonaws.com/");
						BufferedReader in = new BufferedReader(new InputStreamReader(ipcheck.openStream()));

						gameIP = in.readLine();	//	fetch the IP!
						in.close();
					}
					String serverInfo = "Server: " + gameIP +"\n";
 					msg += serverInfo;
 				}
 				catch(Exception e){
 					msg += "Server: Unknown address!";
 				}
 				textBox.setMsg(msg);
 			}
 		}
 	}
 
 	@Override
 	public void buttonAction(){
 		try{
 			if(port != null){
 				port.close();
 			}
 			game.setUserName(inputFields.get(0).getText());
 			//	Default port number
 			int pn = 54242;
 			try{
 				pn = Integer.parseInt(inputFields.get(1).getText());
 			}
 			catch(Exception e){
 				System.err.println("Bad port number! Defaulting to 54242...");
 			}
 			game.setPortNumber(pn);
 			port = new RogueServer(game.getUserName());
 			port.start("dummy", pn);
 		}
 		catch(Exception e){
 			System.err.println(e.getMessage());
 		}
 	}
 }
