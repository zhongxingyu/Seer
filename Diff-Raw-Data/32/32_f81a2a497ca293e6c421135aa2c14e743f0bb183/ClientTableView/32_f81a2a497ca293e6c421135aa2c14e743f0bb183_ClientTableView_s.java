 package com.ricex.rpi.server.player;
 
 import javafx.application.Platform;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.layout.BorderPane;
 
 import com.ricex.rpi.common.RPIStatus;
 import com.ricex.rpi.server.RPIServer;
 import com.ricex.rpi.server.client.Client;
 import com.ricex.rpi.server.client.ClientChangeListener;
 import com.ricex.rpi.server.client.ClientConnectionListener;
 import com.ricex.rpi.server.client.RPIClient;
 
 
 /**The view for displaying a table of the known clients, and thier status + id and possibly name
  * 
  * @author Mitchell
  *
  */
 
 public class ClientTableView extends BorderPane implements ClientChangeListener<RPIClient>, ClientConnectionListener<RPIClient> {
 
 	/** Instance of the running server to fetch information about the clients */
 	private RPIServer server;
 	
 	/** List of the clients currently being displayed */	
 	private ObservableList<RPIClient> rpiClients;
 	
 	/** The table view for displaying the clients */
 	private TableView<RPIClient> clientTable;
 	
 	
 	public ClientTableView(RPIServer server) {
 		this.server = server;
 		
 		server.addConnectionListener(this);
 		
 		clientTable = new TableView<RPIClient>();
 		rpiClients = FXCollections.observableArrayList(server.getConnectedClients());
 		clientTable.setItems(rpiClients);
 		
 		TableColumn<RPIClient, Long> idColumn = new TableColumn<RPIClient, Long>("Id");
 		TableColumn<RPIClient, String> nameColumn = new TableColumn<RPIClient, String>("Name");
 		TableColumn<RPIClient, RPIStatus> statusColumn = new TableColumn<RPIClient, RPIStatus>("Status");
 		
 		idColumn.setCellValueFactory(new PropertyValueFactory<RPIClient, Long>("id"));
 		nameColumn.setCellValueFactory(new PropertyValueFactory<RPIClient, String>("name"));
 		statusColumn.setCellValueFactory(new PropertyValueFactory<RPIClient, RPIStatus>("status"));
 		
 		//set the widths.
 		idColumn.prefWidthProperty().bind(clientTable.widthProperty().multiply(0.10)); // 10%
 		nameColumn.prefWidthProperty().bind(clientTable.widthProperty().multiply(0.45));
 		statusColumn.prefWidthProperty().bind(clientTable.widthProperty().multiply(0.45));
 		
 		clientTable.getColumns().addAll(idColumn, nameColumn, statusColumn);
 		
 		setCenter(clientTable);
 	}
 
 
 	public void clientConnected(RPIClient rpiClient) {
 		rpiClients.add(rpiClient);
 		rpiClient.addChangeListener(this);
 	}
 
 	public void clientDisconnected(RPIClient rpiClient) {
 		rpiClients.remove(rpiClient);
 		rpiClient.removeChangeListener(this);
 	}
 
 	public void clientChanged(RPIClient rpiClient) {
 		System.out.println("Client has changed thier status");
 		
 		Platform.runLater(new Runnable() {
 			public void run() {
 				clientTable.getColumns().get(0).setVisible(false);
 				clientTable.getColumns().get(0).setVisible(true);
 			}
 		});
 		
 	}
 }
