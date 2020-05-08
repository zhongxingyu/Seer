 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.GameAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.PlayerView;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.input.GestureDetector.GestureListener;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 
 public class GameController implements GestureListener {
 
 	private GdxGame game;
 	private OrthographicCamera boardCamera;
 	private boolean modifyBoard, modifyCards;
 	private final Client client;
 	private float maxZoom;
 	private Vector3 defaultPosition;
 	private DeckView deckView;
 
 	public GameController(GdxGame game) {
 		this.game = game;
 		this.boardCamera = this.game.getBoardCamera();
 		this.client = new Client(1);
 		this.deckView = game.getDeckView();
 		
 		maxZoom = 0.4f;
 		defaultPosition = new Vector3(240, 400, 0f);
 		
 		maxZoom = 0.4f;
 		defaultPosition = new Vector3(240, 400, 0f);
 		
 		Texture boardTexture = new Texture(Gdx.files.internal("textures/testTile.png"));
 		boardTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		Texture cardTexture = new Texture(Gdx.files.internal("textures/card.png"));
 		boardTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		game.getBoardView().createBoard(boardTexture, client.getMap());
 		
 		for(PlayerView player : client.getPlayers(boardTexture, game.getBoardView().getStartPositions())) {
 			game.getBoardView().addPlayer(player);
 		}		
 		
 //		PlayerView p = game.getBoardView().getPlayer(1);
 //		p.addAction(Actions.sequence(Actions.moveBy(40, 0, 2),
 //				Actions.parallel(Actions.fadeOut(2), Actions.scaleTo(0.3f, 0.3f, 2))));
 	
 		game.getDeckView().setDeckCards(client.getCards(cardTexture));
 		
 		List<GameAction> actions = client.getRoundResult();
 		for(GameAction a : actions) {
 			a.action(game.getBoardView().getPlayers());
 		}
 	}
 
 	@Override
 	public boolean tap(float arg0, float arg1, int arg2, int arg3) {
 		if(modifyBoard && arg2 == 2) {
 			if(boardCamera.zoom == maxZoom){
 				boardCamera.zoom = 1.0f;
 				boardCamera.position.set(defaultPosition);
 			} else {
 				boardCamera.zoom = maxZoom;
 				boardCamera.position.set(arg0, arg1, 0f);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(float arg0, float arg1, int arg2, int arg3) {
		if (arg1 < Gdx.graphics.getHeight() * 2 / 3) {
 			modifyBoard = true;
 			modifyCards = false;
 		} else {
 			modifyBoard = false;
 			modifyCards = true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean longPress(float arg0, float arg1) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean pan(float arg0, float arg1, float arg2, float arg3) {
 		if (modifyBoard) {
 			if (boardCamera.zoom == 1.0f) {
 				boardCamera.translate(0f, arg3);
 			} else {
 				boardCamera.translate(-arg2 * boardCamera.zoom, arg3
 						* boardCamera.zoom);
 			}
 		} else if (modifyCards) {
 			if (arg2 > 0) {
 				if (deckView.getPositionX() + arg2 > 0) {
 					deckView.setPositionX(0);
 				} else {
 					deckView.setPositionX(deckView.getPositionX() + (int)arg2);
 				}
 			} else if (arg2 < 0) {
 				 if (deckView.getLastPositionX() + arg2 < 480) {
 					 deckView.setPositionX(480);
 				 } else {
 					 deckView.setPositionX(deckView.getPositionX() + (int)arg2);
 				 }
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean fling(float arg0, float arg1, int arg2) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean zoom(float arg0, float arg1) {
 		if (modifyBoard) {
 			if (arg1 - arg0 > 0 && boardCamera.zoom > 0.4f) {
 				boardCamera.zoom -= 0.05f;
 			} else if (arg0 - arg1 > 0 && boardCamera.zoom < 1.0f) {
 				boardCamera.zoom += 0.05f;
 			} else if (boardCamera.zoom == 1.0f) {
 				boardCamera.position.set(240, 400, 0f);
 			}
 			// checkCameraBounds();
 		}
 		return false;
 	}
 
 	@Override
 	public boolean pinch(Vector2 arg0, Vector2 arg1, Vector2 arg2, Vector2 arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void checkCameraBounds() {
 	}
 }
