 package app;
 
 import java.awt.Dimension;
 
 import utils.JsonUtil;
 
 
 import com.golden.gamedev.GameEngine;
 import com.golden.gamedev.GameLoader;
 import com.golden.gamedev.GameObject;
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 
 public class Main extends GameEngine {
 	
 	public static final int TITLE = 0, GAME_MODE = 1;
 	
 	public String configURL;
 	
 	public Main(String configURL) {
 		this.configURL = configURL;
 	}
 	
 	public void initResources() {
 		//nextGameID = GAME_MODE;
 		nextGameID = TITLE;
 	}
 	
 	public GameObject getGame(int GameID) {
 		JsonObject json = JsonUtil.getJSON(configURL);
 		switch (GameID) {
 			case GAME_MODE : return new RPGame(this, configURL);
 			case TITLE : return new Title(this, json.get("background").getAsString());
 		}
 		return null;
 	}
 	
 	public static void main(String[] args) {
 		GameLoader game = new GameLoader();
 		game.setup(new Main("rsc/config/game.json"), new Dimension(640, 480), false);
 		game.start();
 	}
 }
