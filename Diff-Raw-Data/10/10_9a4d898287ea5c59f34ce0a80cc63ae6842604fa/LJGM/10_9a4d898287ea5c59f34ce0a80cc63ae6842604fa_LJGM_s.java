 package net.dean.ljgm;
 
 import javafx.application.Application;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Scene;
 import javafx.scene.control.Menu;
 import javafx.scene.control.MenuBar;
 import javafx.scene.control.MenuItem;
 import javafx.scene.control.ScrollPane;
 import javafx.scene.control.ScrollPane.ScrollBarPolicy;
 import javafx.scene.control.ScrollPaneBuilder;
 import javafx.scene.image.Image;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.BorderPaneBuilder;
 import javafx.stage.Stage;
 import net.dean.ljgm.gui.GallerySidebar;
 import net.dean.ljgm.gui.StatusBar;
 import net.dean.ljgm.gui.ViewingArea;
 import net.dean.ljgm.gui.gallerycreator.GalleryCreator;
 import net.dean.ljgm.logging.LJGMLogger;
 
// TODO: Auto-generated Javadoc
 /**
  * This is the main class of the Lightweight Java Gallery Manager project.
  * 
  * @author Matthew Dean
  * 
  */
 
 public class LJGM extends Application {
 
 	/** The instance of {@link LJGM}. */
 	private static LJGM instance;
 
 	/**
 	 * Gets the only instance of LJGM.
 	 * 
 	 * @return The instance of LJGM.
 	 */
 	public static LJGM instance() {
 		return instance;
 	}
 
 	/** The logger that is used to log events in the application. */
 	private final LJGMLogger logger = new LJGMLogger();
 
 	/** The {@link ConfigManager} that is used to manage application properties. */
 	private final ConfigManager config = new ConfigManager();
 
 	/**
 	 * The GallerySidebar that is responsible for showing all of the available
 	 * galleries to look through.
 	 */
 	private GallerySidebar gallerySidebar;
 
 	/**
 	 * The {@link GalleryManager} used to manage all of the galleries currently
 	 * recognized by the application through XML.
 	 * 
 	 * @see Gallery
 	 */
 	private GalleryManager galleryManager;
 
 	/** The status bar that shows the status of the current event. */
 	private StatusBar statusBar;
 
 	/**
 	 * The ViewingArea that is responsible for showing images of the selected
 	 * gallery.
 	 */
 	private ViewingArea view;
 
 	/**
 	 * The main stage of the application.
 	 */
 	private Stage ljgmStage;
 
 	/**
 	 * Instantiates a new LJGM object.
 	 */
 	public LJGM() {
 		logger.info("Staring up " + LJGMDefaults.PROJECT_NAME + " v" + LJGMDefaults.PROJECT_VERSION + "...");
 		LJGM.instance = this;
 		this.galleryManager = new GalleryManager();
 		this.statusBar = new StatusBar();
 		this.view = new ViewingArea(this);
 		this.gallerySidebar = new GallerySidebar(galleryManager, view);
 	}
 
 	/**
 	 * Tells the {@link #gallerySidebar} to populate the {@link #view} with the
 	 * current gallery's images. This is mainly used by {@link #galleryManager}
 	 * to tell the application that the galleries have been updated and the
 	 * sidebar needs to refresh.
 	 */
 	public void refreshGalleries() {
 		gallerySidebar.populate(galleryManager.getGalleries());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javafx.application.Application#start(javafx.stage.Stage)
 	 */
 	@Override
 	public void start(Stage primaryStage) throws Exception {
 		if (false) {
 			new GalleryCreator(galleryManager.getGalleries().get(0)).show();
 			return;
 		}
 		logger.info("Setting up main stage...");
 		this.ljgmStage = primaryStage;
 		ljgmStage.setTitle(LJGMUtils.generateStageTitle("Starting..."));
 		ljgmStage.getIcons().add(new Image("file:res/favicon.png"));
 
 		// Create the scroll pane for the viewing area
 		ScrollPane sp = ScrollPaneBuilder.create().content(view).hbarPolicy(ScrollBarPolicy.AS_NEEDED)
 				.vbarPolicy(ScrollBarPolicy.AS_NEEDED).build();
 
 		// Create the main border pane to host all the components
 		// Center: viewing area, left: sidebar, bottom: status bar, top: menu
 		// items
 		// @formatter:off
 		BorderPane bp = BorderPaneBuilder.create().center(sp).left(gallerySidebar).bottom(statusBar).top(createMenuBar()).build();
 		// @formatter:on
 		ljgmStage.setScene(new Scene(bp, 1000, 500));
 
 		// Select the first gallery
 		if (gallerySidebar.getListView().getItems().size() != 0) {
 			view.setFocus(galleryManager.getGallery(gallerySidebar.removeImageCount(gallerySidebar.getListView().getItems()
 					.get(0))));
 			gallerySidebar.getListView().getSelectionModel().select(0);
 		} else {
 			view.setFocus(null);
 		}
 
 		logger.info("Main stage set up.");
 		ljgmStage.show();
 		logger.info("Done!");
 	}
 
 	/**
 	 * Creates the menu bar.
 	 * 
 	 * @return the menu bar
 	 */
 	private MenuBar createMenuBar() {
 		MenuBar menuBar = new MenuBar();
 		Menu file = new Menu("File");
 
 		Menu about = new Menu("About");
 		MenuItem aboutProject = new MenuItem("About LJGM");
 		aboutProject.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent e) {
 				// show about dialog
 			}
 		});
 		about.getItems().add(aboutProject);
 
 		menuBar.getMenus().addAll(file, about);
 
 		return menuBar;
 	}
 	
 	public Stage getStage() {
 		return ljgmStage;
 	}
 
 	/**
 	 * Gets the GalleryManager.
 	 * 
 	 * @return The GalleryManager.
 	 */
 	public GalleryManager getGalleryManager() {
 		return galleryManager;
 	}
 
 	/**
 	 * Gets the ConfigManager.
 	 * 
 	 * @return The ConfigManager.
 	 */
 	public ConfigManager getConfigManager() {
 		return config;
 	}
 
 	/**
 	 * Gets the StatusBar.
 	 * 
 	 * @return the StatusBar
 	 */
 	public StatusBar getStatusBar() {
 		return statusBar;
 	}
 
 	/**
 	 * Gets the logger.
 	 * 
 	 * @return the logger
 	 */
 	public LJGMLogger getLogger() {
 		return logger;
 	}
 
 	/**
 	 * Main method.
 	 * 
 	 * @param args
 	 *            The arguments to pass to the application.
 	 */
 	public static void main(String[] args) {
 		launch(args);
 	}
 }
