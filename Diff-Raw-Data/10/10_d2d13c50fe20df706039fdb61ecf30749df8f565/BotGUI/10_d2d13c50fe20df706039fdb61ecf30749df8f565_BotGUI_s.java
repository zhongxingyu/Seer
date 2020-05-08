 package net.hexid.hexbot.bot.gui;
 
 import java.util.ArrayList;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.scene.control.Menu;
 import javafx.scene.control.MenuBar;
 import javafx.scene.control.MenuBuilder;
 import javafx.scene.layout.AnchorPane;
 import javafx.stage.Stage;
 import net.hexid.hexbot.bot.Bots;
 
 public class BotGUI extends javafx.application.Application {
 	protected AnchorPane root;
 	protected BotTabPane tabPane;
 
 	public static void init(String[] args) {
 		launch(args);
 	}
 
 	@Override public void start(Stage stage) {
 		root = new AnchorPane();
 		tabPane = createTabPane();
 		root.getChildren().addAll(tabPane, createMenuBar());
 
 		// if there are arguments
 		if(getParameters().getRaw().size() > 0) {
 			// iterate over each argument (names of bots)
 			for(String arg : getParameters().getRaw()) {
 				// add a new tab if the argument is the name of a bot
 				if(Bots.hasBot(arg)) addTab(arg);
 			}
 		}
 		if(tabPane.getTabs().size() == 0) { // if no arguments were passed to the gui
 			System.out.println("args: 'gui' [BotName...]");
 			System.out.println("Available bots: " + Bots.getAvailableBots());
 		}
 
 		stage.setTitle("HexBot by Hexid");
 		stage.setScene(new javafx.scene.Scene(root, 550, 300));
 		stage.show();
 	}
 
 	protected void addTab(String botName) {
 		try {
 			// create a new tab based on the bot's guiClassPath
 			tabPane.addBotTab((BotTab)Class.forName(Bots.getBotGuiClassPath(botName)).newInstance());
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected MenuBar createMenuBar() {
 		MenuBar menuBar = new MenuBar();
 		ArrayList<Menu> menus = new ArrayList<>();
 
 		// iterate over botNames
 		for(final String botName : Bots.botNames()) {
 			// create a menu item with the bot's long name
 			menus.add(MenuBuilder.create().text(Bots.getBotLongName(botName))
 					.onShown(new EventHandler<Event>() {
 						@Override public void handle(Event e) {
 							((Menu)e.getSource()).hide();
 							addTab(botName); // create a new tab
 						}
					// set the items to be an empty menuItem so that when clicked it will disappear immediately
 					}).items(new javafx.scene.control.MenuItem()).build());
 		}
 
 		if(menus.size() == 0) // if no bot tabs were created
 			menus.add(MenuBuilder.create().text("No bots found").build());
 		menuBar.getMenus().addAll(menus);
 		
 		AnchorPane.setTopAnchor(menuBar, 0.0d);
 		AnchorPane.setLeftAnchor(menuBar, 0.0d);
 		AnchorPane.setRightAnchor(menuBar, 0.0d);
 		return menuBar;
 	}
 
 	protected BotTabPane createTabPane() {
 		tabPane = new BotTabPane();
 		AnchorPane.setTopAnchor(tabPane, 24.0d); // fit to the top menu bar
 		AnchorPane.setBottomAnchor(tabPane, 0.0d);
 		AnchorPane.setLeftAnchor(tabPane, 0.0d);
 		AnchorPane.setRightAnchor(tabPane, 0.0d);
 		return tabPane;
 	}
 }
