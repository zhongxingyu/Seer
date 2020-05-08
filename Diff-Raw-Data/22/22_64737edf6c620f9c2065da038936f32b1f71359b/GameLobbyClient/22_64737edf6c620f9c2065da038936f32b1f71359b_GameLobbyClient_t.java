 package edu.brown.cs32.goingrogue.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import edu.brown.cs32.goingrogue.network.RogueClient;
 
 public class GameLobbyClient extends GameLobbyState {
 	private int sumDelta = 0;
 	private int updateLimit = 1500;
 
 	public GameLobbyClient(String bg, String md, int id, MenuGame game){
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
 		inputFields.get(1).setText(game.getHostName());
 		inputFields.get(1).setCursorPos(game.getHostName().length());
 		inputFields.get(2).setText("" + game.getPortNumber());
 		inputFields.get(2).setCursorPos(("" + game.getPortNumber()).length());
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
 		if(sumDelta > updateLimit){
 			sumDelta = 0;
 			if(port != null){
 				this.setPlayerNames(port.getPlayerNames());
 				//System.out.println(getPlayerNames());
 				String msg = "";
 				if(getPlayerNames() != null){
 					for(String elem : getPlayerNames()){
 						msg += "\n" + elem;
 					}
 					if(msg.length() > 0)
 						msg = msg.substring(1);
 					msg = "Server:\n" + msg;
 					textBox.setMsg(msg);
 					//System.out.println("MSG: " + msg);
 				}
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
 			game.setHostName(inputFields.get(1).getText());
 			//	Default port number
 			int pn = 54242;
 			try{
 				pn = Integer.parseInt(inputFields.get(2).getText());
 			}
 			catch(Exception e){
 				System.err.println("Bad port number! Defaulting to 54242...");
 			}
 			game.setPortNumber(pn);
 			port = new RogueClient(game.getUserName(), game);
 			port.start(game.getHostName(), game.getPortNumber());
 		}
 		catch(Exception e){
 			if(port != null){
 				port.close();
 				port = null;
 			}
 			List<String> msg = new ArrayList<>();
 			msg.add("Failed to connect!");
 			setPlayerNames(msg);
			System.err.println("Client: " + e.getMessage());
 		}
 	}
 }
