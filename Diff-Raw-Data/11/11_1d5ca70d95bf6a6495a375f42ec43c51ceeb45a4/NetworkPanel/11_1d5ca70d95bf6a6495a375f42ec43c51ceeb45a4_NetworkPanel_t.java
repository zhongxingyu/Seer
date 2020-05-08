 package ch.bfh.bti7301.w2013.battleship.gui;
 
 import java.util.LinkedList;
 import java.util.ResourceBundle;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
import javafx.application.Platform;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Pos;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TextField;
 import javafx.scene.effect.InnerShadow;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.VBox;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Circle;
 import javafx.scene.shape.Shape;
 import ch.bfh.bti7301.w2013.battleship.network.Connection;
 import ch.bfh.bti7301.w2013.battleship.network.ConnectionState;
 import ch.bfh.bti7301.w2013.battleship.network.DiscoveryListener;
 import ch.bfh.bti7301.w2013.battleship.network.NetworkInformation;
 
 import com.sun.javafx.collections.ObservableListWrapper;
 
 public class NetworkPanel extends VBox {
 	private ObservableList<NetworkClient> opponents = new ObservableListWrapper<NetworkClient>(
 			new LinkedList<NetworkClient>());
 	private Connection conn = Connection.getInstance();
 
 	final TextField ipAddress = new TextField();
 
 	public NetworkPanel() {
 		setSpacing(4);
 		getChildren().add(getIpBox());
 		final ListView<NetworkClient> ips = new ListView<>();
 
 		conn.addDiscoveryListener(new DiscoveryListener() {
 			@Override
			public void foundOpponent(final String ip, final String name) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						opponents.add(new NetworkClient(ip, name));
					}
				});
 			}
 		});
 		ips.setItems(opponents);
 		ips.setOnMouseClicked(new EventHandler<MouseEvent>() {
 			@Override
 			public void handle(MouseEvent event) {
 				ipAddress.setText(ips.getSelectionModel().getSelectedItem().ip);
 			}
 		});
 		getChildren().add(ips);
 	}
 
 	private HBox getIpBox() {
 		// Temporary input field to enter the opponent's
 		final StateBox stateBox = new StateBox();
 		stateBox.update(conn.getConnectionState(),
 				conn.getConnectionStateMessage());
 		getChildren().add(stateBox);
 
 		final HBox ipBox = new HBox();
 
 		Matcher m = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.").matcher(
 				NetworkInformation.getIntAddresses().toString());
 		if (m.find())
 			ipAddress.setText(m.group());
 
 		ipBox.getChildren().add(ipAddress);
 		final Button connect = new Button(ResourceBundle.getBundle(
 				"translations").getString("connect"));
 		Connection.getInstance().addConnectionStateListener(
 				new GuiConnectionStateListenerAdapter() {
 					@Override
 					public void doStateChanged(ConnectionState newState,
 							String msg) {
 						stateBox.update(newState, msg);
 						switch (newState) {
 						case LISTENING:
 							ipBox.setVisible(true);
 							break;
 						case CONNECTED:
 							ipBox.setVisible(false);
 							break;
 						default:
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
 		return ipBox;
 	}
 
 	private class StateBox extends HBox {
 		private Shape light;
 		private Label text;
 		private InnerShadow glow = new InnerShadow();
 
 		public StateBox() {
 			setSpacing(4);
 			setAlignment(Pos.CENTER_LEFT);
 
 			light = new Circle(6);
 			getChildren().add(light);
 
 			text = new Label();
 			getChildren().add(text);
 		}
 
 		public void update(ConnectionState state, String msg) {
 			switch (state) {
 			case CLOSED:
 				light.setFill(Color.GREY);
 				light.setEffect(null);
 				break;
 			case CONNECTED:
 				light.setFill(Color.GREENYELLOW);
 				glow.setColor(Color.GREEN);
 				light.setEffect(glow);
 				break;
 			case LISTENING:
 				light.setFill(Color.YELLOW);
 				glow.setColor(Color.ORANGE);
 				light.setEffect(glow);
 				break;
 			default:
 				light.setFill(Color.ORANGE);
 				glow.setColor(Color.RED);
 				light.setEffect(glow);
 			}
 			text.setText(msg);
 		}
 	}
 
 	private class NetworkClient {
 		String ip, name;
 
 		public NetworkClient(String ip, String name) {
 			this.ip = ip;
 			this.name = name;
 		}
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 }
