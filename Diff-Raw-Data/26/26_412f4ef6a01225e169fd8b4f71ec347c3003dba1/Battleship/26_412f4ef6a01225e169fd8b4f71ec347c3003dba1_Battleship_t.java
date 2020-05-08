 /**
  * This is free and unencumbered software released into the public domain.
  * 
  * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
  * this software, either in source code form or as a compiled binary, for any
  * purpose, commercial or non-commercial, and by any means.
  * 
  * In jurisdictions that recognize copyright laws, the author or authors of this
  * software dedicate any and all copyright interest in the software to the
  * public domain. We make this dedication for the benefit of the public at large
  * and to the detriment of our heirs and successors. We intend this dedication
  * to be an overt act of relinquishment in perpetuity of all present and future
  * rights to this software under copyright law.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  * For more information, please refer to [http://unlicense.org]
  */
 package ch.bfh.bti7301.w2013.battleship;
 
 import java.util.ResourceBundle;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javafx.animation.ParallelTransitionBuilder;
 import javafx.animation.ScaleTransitionBuilder;
 import javafx.animation.TranslateTransitionBuilder;
 import javafx.application.Application;
import javafx.application.Platform;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextField;
 import javafx.scene.effect.SepiaTone;
 import javafx.scene.layout.HBox;
 import javafx.scene.paint.Color;
 import javafx.scene.transform.Scale;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import ch.bfh.bti7301.w2013.battleship.game.GameRule;
 import ch.bfh.bti7301.w2013.battleship.game.Player;
 import ch.bfh.bti7301.w2013.battleship.game.PlayerStateListener;
 import ch.bfh.bti7301.w2013.battleship.game.players.GenericPlayer.PlayerState;
 import ch.bfh.bti7301.w2013.battleship.gui.BoardView;
 import ch.bfh.bti7301.w2013.battleship.gui.ShipStack;
 import ch.bfh.bti7301.w2013.battleship.network.Connection;
 import ch.bfh.bti7301.w2013.battleship.network.ConnectionState;
 import ch.bfh.bti7301.w2013.battleship.network.ConnectionStateListener;
 import ch.bfh.bti7301.w2013.battleship.network.NetworkInformation;
 
 /**
  * @author Christian Meyer <chrigu.meyer@gmail.com>
  * 
  */
 public class Battleship extends Application {
 	private ResourceBundle labels;
 
 	private Game game;
 	private GameRule rule;
 
 	public Battleship() {
 		labels = ResourceBundle.getBundle("translations");
 		rule = new GameRule();
 		game = Game.getInstance();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// Output this for debugging and testing
 		System.out.println(NetworkInformation.getIntAddresses());
 
 		launch(args);
 	}
 
 	@Override
 	public void start(Stage primaryStage) {
 		primaryStage.setTitle(labels.getString("title"));
 
 		final Group root = new Group();
 		final Scene scene = new Scene(root, 800, 600, Color.WHITE);
 		primaryStage.setScene(scene);
 
 		final BoardView pbv = new BoardView(game.getLocalPlayer().getBoard());
 		pbv.relocate(10, 10);
 		root.getChildren().add(pbv);
 
 		final BoardView obv = new BoardView(game.getOpponent().getBoard());
 		obv.getTransforms().add(new Scale(0.5, 0.5, 0, 0));
 
 		obv.relocate(pbv.getBoundsInParent().getMaxX() + 20, 10);
 		root.getChildren().add(obv);
 
 		final Button ready = buildReadyButton(pbv, obv);
 		ready.relocate(obv.getBoundsInParent().getMinX(), obv
 				.getBoundsInParent().getMaxY() + 8);
 		root.getChildren().add(ready);
 
 		game.getOpponent().addPlayerStateListener(new PlayerStateListener() {
 			@Override
			public void stateChanged(Player p, final PlayerState s) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// javaFX operations should go here
						switch (s) {
						case READY:
							ready.setText(labels.getString("start"));
							break;
						case PLAYING:
							obv.setEffect(new SepiaTone());
							break;
						}
					}
				});
 			}
 		});
 		game.getLocalPlayer().addPlayerStateListener(new PlayerStateListener() {
 			@Override
 			public void stateChanged(Player p, PlayerState s) {
 				switch (s) {
 				case PLAYING:
 					obv.setEffect(null);
 				}
 			}
 		});
 
 		ShipStack shipStack = new ShipStack(game, rule, pbv, ready);
 		shipStack.relocate(obv.getBoundsInParent().getMinX(), obv
 				.getBoundsInParent().getMaxY() + 8);
 		root.getChildren().add(shipStack);
 
 		HBox ipBox = getIpBox();
 		ipBox.relocate(pbv.getBoundsInParent().getMinX(), pbv
 				.getBoundsInParent().getMaxY() + 20);
 		root.getChildren().add(ipBox);
 
 		primaryStage.show();
 	}
 
 	private HBox getIpBox() {
 		// Temporary input field to enter the opponent's
 		final HBox ipBox = new HBox();
 		final TextField ipAddress = new TextField();
 
 		Matcher m = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.").matcher(
 				NetworkInformation.getIntAddresses().toString());
 		if (m.find())
 			ipAddress.setText(m.group());
 
 		ipBox.getChildren().add(ipAddress);
 		final Button connect = new Button(labels.getString("connect"));
 		Connection.getInstance().setConnectionStateListener(
 				new ConnectionStateListener() {
 					@Override
 					public void stateChanged(ConnectionState newState) {
 						switch (newState) {
 						case CLOSED:
 						case LISTENING:
 							ipBox.setVisible(true);
 							break;
 						case CONNECTED:
 							ipBox.setVisible(false);
 							break;
 						}
 					}
 				});
 		connect.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				Connection.getInstance().connectOpponent(ipAddress.getText());
 				System.out.println(ipAddress.getText());
 			}
 		});
 		ipBox.getChildren().add(connect);
 		ipBox.getChildren().add(
 				new Label(NetworkInformation.getIntAddresses().toString()));
 		return ipBox;
 	}
 
 	private Button buildReadyButton(final BoardView pbv, final BoardView obv) {
 		final Button ready = new Button(labels.getString("ready"));
 		ready.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				game.getLocalPlayer().setPlayerState(PlayerState.READY);
 				ParallelTransitionBuilder
 						.create()
 						.children(
 								ScaleTransitionBuilder.create().node(pbv)
 										.duration(Duration.seconds(1)).toX(0.5)
 										.toY(0.5).build(),
 								TranslateTransitionBuilder.create().node(pbv)
 										.duration(Duration.seconds(1))
 										.toX(-100).toY(-100).build(),
 								ScaleTransitionBuilder.create().node(obv)
 										.duration(Duration.seconds(1)).toX(2)
 										.toY(2).build(),
 								TranslateTransitionBuilder.create().node(obv)
 										.duration(Duration.seconds(1)).toY(200)
 										.build()//
 						).build().play();
 				ready.setVisible(false);
 			}
 		});
 		ready.setVisible(false);
 		return ready;
 	}
 }
