 package edu.brown.cs32.goingrogue.game;
 
 import java.net.Inet4Address;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import edu.brown.cs32.goingrogue.network.RogueServer;
 
 public class GameLobbyServer extends GameLobbyState {
 	private int sumDelta = 0;
 	private int updateLimit = 500;
 	private String gameIP;
 	
 	public GameLobbyServer(String bg, String md, int id, MenuGame game){
 		super(bg, md, id, game);
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame gm)
 			throws SlickException {
 		super.init(gc, gm);
 		if(components == null)
 			return;
 		inputFields.get(0).setText(game.getUserName());
 		inputFields.get(0).setCursorPos(game.getUserName().length());
 		inputFields.get(1).setText("" + game.getPortNumber());
 		inputFields.get(1).setCursorPos(("" + game.getPortNumber()).length());
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics g)
 			throws SlickException {
 		super.render(gc, game, g);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		sumDelta += delta;
 		if(port != null && sumDelta > updateLimit){
 			sumDelta = 0;
 			this.setPlayerNames(port.getPlayerNames());
 			String msg = "";
 			if(getPlayerNames() != null){
 				for(String elem : getPlayerNames()){
 					msg += "\n" + elem;
 				}
 				msg = msg.substring(1);
 				try{
 					if(gameIP == null){
 						gameIP = Inet4Address.getLocalHost().getHostAddress();
 					}
 					String serverInfo = "Server: " + gameIP +"\n";
 					msg = serverInfo + msg;
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
 				((RogueServer) port).beginGame();
 				return; 
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
 			port = new RogueServer(game.getUserName(), game);
 			port.start("dummy", pn);
 		}
 		catch(Exception e){
			System.err.println(e.getMessage());
 		}
 	}
 }
