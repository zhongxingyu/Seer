 package it.chalmers.tendu.screens;
 
 import it.chalmers.tendu.Tendu;
 import it.chalmers.tendu.controllers.InputController;
 import it.chalmers.tendu.controllers.LobbyController;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.defaults.PlayerColors;
 import it.chalmers.tendu.defaults.TextLabels;
 import it.chalmers.tendu.gamemodel.LobbyModel;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.gamemodel.SimpleTimer;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.math.Vector2;
 
 public class LobbyScreen implements Screen {
 	private LobbyController lobbyController;
 	private Tendu tendu;
 	private TextWidget statusText;
 	private TextWidget readyText;
 	private TextWidget playerText;
 	private TextWidget waitingText;
 	private BitmapFont font;
 	private int playersConnected;
 	private final int maximumPlayers;
 	private TextWidget testStuff;
 	private boolean ready;
 
 	public LobbyScreen(Tendu tendu, boolean isHost) {
 		maximumPlayers = 4;
 		this.tendu = tendu;
 		LobbyModel model = new LobbyModel(maximumPlayers);
 		lobbyController = new LobbyController(model);
 		
 		font = new BitmapFont(Gdx.files.internal("fonts/menuFont.fnt"),
 				Gdx.files.internal("fonts/menuFont.png"), false);
 		
 		readyText = new TextWidget(TextLabels.READY, new Vector2(640, 150), Constants.MENU_FONT_COLOR);
 		waitingText = new TextWidget(TextLabels.WAITING_FOR_PLAYERS, new Vector2(65, 150), Constants.MENU_FONT_COLOR);		
 		playerText = new TextWidget("This will never be shown on screen", new Vector2(65, 450), Constants.MENU_FONT_COLOR);
 		
 		ready = false;
 		
 		if (isHost)
 			initHost();
 		else
 			initClient();
 		
 		testStuff = new TextWidget("test stuff", new Vector2(870, 120),
 				Constants.MENU_FONT_COLOR);
 	}
 
 	private void initHost() {
 		Player.getInstance().setHost(true);
 		tendu.getNetworkHandler().hostSession();
 
 		String myMac = Player.getInstance().getMac();
 		lobbyController.getModel().addPlayer(myMac);
 
 		statusText = new TextWidget(TextLabels.WAITING_FOR_CONNECTIONS, new Vector2(40,
 				620), Constants.MENU_FONT_COLOR);
 	}
 
 	private void initClient() {
 		tendu.getNetworkHandler().joinGame();
 		statusText = new TextWidget(TextLabels.SEARCHING_FOR_SESSION, new Vector2(
 				40, 620), Constants.MENU_FONT_COLOR);
 	}
 
 	public void tick(InputController input) {
 		playersConnected = getModel().getLobbyMembers().entrySet().size();
 
 		if (!Player.getInstance().isHost() && playersConnected > 0) {
 			statusText.setText(TextLabels.CONNECTED_TO_SESSION);
 		} else if (Player.getInstance().isHost() && playersConnected == maximumPlayers) {
 			statusText.setText("Maximum players connected");
 		}
 
 		if (input.isTouchedDown()) {
 			if (readyText.collided(input.getCoordinates())) {
 				Gdx.input.vibrate(25);
 				readyText.setColor(Constants.MENU_FONT_COLOR_PRESSED);
 			}
 		} else if (input.isTouchedUp()) {
 			if (readyText.collided(input.getCoordinates())) {
 				ready = true;
 				
 				// Received by host and client in LobbyController.
 				EventBus.INSTANCE.broadcast(new EventMessage(C.Tag.TO_SELF,
 						C.Msg.PLAYER_READY, Player.getInstance().getMac()));
 			}
 			
 			if (testStuff.collided(input.getCoordinates())) {				
 				// Received by host and client in LobbyController.
				EventBus.INSTANCE.broadcast(new EventMessage(C.Tag.REQUEST_AS_CLIENT,
 						C.Msg.TEST, new SimpleTimer()));
 			}
 
 			readyText.setColor(Constants.MENU_FONT_COLOR);
 		}
 	}
 
 	@Override
 	public void render() {
 
 		statusText.draw(tendu.spriteBatch, font);
 		testStuff.draw(tendu.spriteBatch, font);
 		
 		playerText.setY(580);
 
 		for (Map.Entry<String, Integer> p : getModel().getLobbyMembers()
 				.entrySet()) {
 			
 			if(p.getKey().equals(Player.getInstance().getMac())) {
 				playerText.setText(TextLabels.ME + " - " + TextLabels.PLAYER + ": " + (p.getValue()+1)); 
 			} else {
 				playerText.setText(TextLabels.PLAYER + ": " + (p.getValue()+1) + " Mac = " + p.getKey()); 
 			}
 			playerText.addToY(-65);
 			playerText.setColor(PlayerColors.getPlayerColor(p.getValue()));
 			playerText.draw(tendu.spriteBatch, font);
 		}
 		
 		if (playersConnected > 0 && !ready) {
 			readyText.drawAtCenterPoint(tendu.spriteBatch, font);
 		} else if(playersConnected > 0 && ready) {
 			waitingText.draw(tendu.spriteBatch, font);
 		}
 	}
 
 	private LobbyModel getModel() {
 		return lobbyController.getModel();
 	}
 
 	@Override
 	public void removed() {
 		font.dispose();
 		lobbyController.unregister();
 	}
 
 }
