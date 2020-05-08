 package GUI2;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 
 import javafx.application.Platform;
 import javafx.embed.swing.JFXPanel;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.JavaFXBuilderFactory;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.layout.Pane;
 
 import javax.swing.JFrame;
 
 import server.AutocorrectEngines;
 import API.Wrapper;
 import API.YummlyAPIWrapper;
 import GUI.GUIScene;
 import UserInfo.Account;
 import UserInfo.Kitchen;
 import UserInfo.KitchenName;
 import client.Client;
 
 public class GUI2Frame extends JFrame {
 	
 	private static final long serialVersionUID = 1L;
 	private Client _client;
 	private JFXPanel _panel;
 	private Account _account;
 	private AutocorrectEngines _engines;
 	private Map<KitchenName,Kitchen> _kitchens;
 	private Controller _controller;
 	
 	public GUI2Frame(Client client, Account account, final Map<KitchenName,Kitchen> kitchens, AutocorrectEngines engines) {
 		super("Cooking with Friends!");
 		System.out.println("CONSTRUCTING GUI2FRAME");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		this.addWindowListener(new java.awt.event.WindowAdapter() {
 		    @Override
 		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
 		    	System.out.println("CLOSEEEINGING WINDOWNNNNNNNNNN");
 				_client.close();
 		    }
 		});
 		
		this.setSize(1280, 980);
     	this.setVisible(true);
     	
     	_panel = new JFXPanel();
     	this.add(_panel);
    	this.setSize(1280, 980);
     	this.setVisible(true);
    	_panel.setPreferredSize(new java.awt.Dimension(1280, 980));
     	
     	_client = client;
     	_account = account;   	
     	_engines = engines;
    	_kitchens = kitchens;    	
     	
     	Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     
 		    	Pane page;
 				try {
 					URL location = getClass().getResource("CookingWithFriends.fxml");
 
 					FXMLLoader fxmlLoader = new FXMLLoader();
 					fxmlLoader.setLocation(location);
 					fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
 
 					Parent p = (Parent) fxmlLoader.load(location.openStream());
 					
 			        _controller = (Controller) fxmlLoader.getController();
 
 			        Scene scene = new Scene(p);
 			        
 			        _controller.setUp(_client, _account, _kitchens, _engines);
 			        _panel.setScene(scene);
 				} catch (IOException e) {
 					System.out.println("ERROR: IN GUI 2 Frame");
 					e.printStackTrace();
 				}
     	
     		}
 		});
 	}
 	
 	public void sendEmail(final boolean userInDatabase){
 		System.out.println("CALLED UPDATE KITCHEN");
 		Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     			//Displays the message asking if the user actually wants to invite the user.
     			_controller.sendInviteEmails(userInDatabase);
     		}
 		});
 	}
 	
 	public void updateKitchen(){
 		System.out.println("CALLED UPDATE KITCHEN");
 		Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     			_controller.reDisplayKitchen();
     		}
 		});
 	}
 	
 	//TODO: Make sure this jives.
 	public void displayNewKitchen(final Kitchen k){
 		Platform.runLater(new Runnable() {
 			@Override
 			public void run(){
 				_controller.displayKitchen(k.getKitchenName());
 			}
 		});
 	}
 	
 	public void updateSearch(){
 		System.out.println("CALLED UPDATE SEARC");
 		Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     			_controller.populateSearchIngredients();
     		}
 		});
 	}
 	
 	public void updateKitchenDropDown(){
 		Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     			_controller.populateKitchenSelector();
     		}
 		});
 	}
 	
 	public void refreshSearchAccordian(){
 		Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     			_controller.populateSearchIngredients();
     		}
 		});
 	}
 	
 }
