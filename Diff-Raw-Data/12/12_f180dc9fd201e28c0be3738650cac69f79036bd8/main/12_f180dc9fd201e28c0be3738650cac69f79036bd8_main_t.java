 
 import sun.security.krb5.internal.PAData;
 import javafx.application.Application;
 import javafx.beans.binding.Bindings;
 import javafx.beans.binding.NumberBinding;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListView;
 import javafx.scene.control.Menu;
 import javafx.scene.control.MenuBar;
 import javafx.scene.control.MenuItem;
 import javafx.scene.control.MenuItemBuilder;
 import javafx.scene.input.KeyCombination;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.GridPane;
 import javafx.scene.chart.*;
 
 import javafx.stage.Stage;
 
 import javafx.stage.WindowEvent;
 
 public  class main extends Application{
 
 
 
 	public static void main(String[] args) {
 		launch(args);
 	}
 
 
 	@Override
 	public void start(final Stage primaryStage) {
 		primaryStage.setTitle("ECS");  		
 		new KeyCombination() {};
 		final Menu menuFichier = new Menu("Fichier");
 		final Menu menuRestaurant = new Menu("Restaurant");
 		final Menu menuCuisine = new Menu("Cuisine");
 		final Menu menuAppareilElectrique = new Menu("Appareils Electriques");
 		final Menu menuPlanAllumage = new Menu("Plans d'allumage");
 		final Menu menuForfait = new Menu("Forfaits");
 		final Menu menuAide = new Menu("Aide");
 
 		primaryStage.setOnShowing(new EventHandler<WindowEvent>()
 				{
 
 			@Override
 			public void handle(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				if(arg0 != null)
 					System.out.println(arg0.toString());
 				else
 					System.out.println("C'est nul !");
 			}
 
 				});
 
 
 		menuFichier.getItems().add(MenuItemBuilder.create()
 				.text("Option")
 				.onAction(
 						new EventHandler<ActionEvent>()
 						{
 							@Override public void handle(ActionEvent e)
 							{
 							}
 						}).accelerator( KeyCombination.keyCombination("ctrl+o")).build());
 
 		menuFichier.getItems().add(MenuItemBuilder.create()
 				.text("Quitter")
 				.onAction(
 						new EventHandler<ActionEvent>()
 						{
 							@Override public void handle(ActionEvent e)
 							{
 								primaryStage.close();
 							}
 						}).accelerator( KeyCombination.keyCombination("ctrl+q")).build());
 
 		final ListView<Restaurant> listRestaurant = new ListView<Restaurant>();
 
 		menuRestaurant.getItems().add(MenuItemBuilder.create()
 				.text("Nouveau")
 				.onAction(
 						new EventHandler<ActionEvent>()
 						{
 							@Override public void handle(ActionEvent e)
 							{
 								NewRestaurantWindow maFenetreNouveauResto = new NewRestaurantWindow();
 								maFenetreNouveauResto.setOnHiding(new EventHandler<WindowEvent>()
 										{
 											@Override
 											public void handle(WindowEvent e) 
 											{
 												System.out.println("Fenetre Restruant : " + e.getEventType().toString());
 												if(e.getEventType() == WindowEvent.WINDOW_HIDING)
 												{
 													ObservableList<Restaurant> itemsRestaurant = FXCollections.observableArrayList (AppCore.getListeRestaurants());
 													listRestaurant.setItems(itemsRestaurant);
 												}
 											}
 											});
 								
 								
 								
 								}
 						}).accelerator( KeyCombination.keyCombination("ctrl+r")).build());
 
 		menuCuisine.getItems().add(MenuItemBuilder.create()
 				.text("Nouveau")
 				.onAction(
 						new EventHandler<ActionEvent>()
 						{
 							@Override public void handle(ActionEvent e)
 							{
 								@SuppressWarnings("unused")
 								NewCuisineWindow maFenetreNouvelleCuisine = new NewCuisineWindow();
 							}
 						}).accelerator( KeyCombination.keyCombination("ctrl+c")).build());
 
 		
 		
 		menuRestaurant.getItems().add(MenuItemBuilder.create()
 				.text("Modifier/Supprimer")
 				.onAction(
 						new EventHandler<ActionEvent>()
 						{
 							@Override public void handle(ActionEvent e)
 							{
 								@SuppressWarnings("unused")
 								modifyRestaurantWindow monModifyRestaurantWindow = new modifyRestaurantWindow();
 							}
 						}).accelerator( KeyCombination.keyCombination("ctrl+shift+R")).build());
 		menuCuisine.getItems().add(new MenuItem("Modifier/Supprimer"));
 		menuForfait.getItems().add(new MenuItem("Nouveau"));
 		menuForfait.getItems().add(new MenuItem("Modifier/Supprimer"));
 		menuAppareilElectrique.getItems().add(MenuItemBuilder.create()
 				.text("Nouveau")
 				.onAction(
 						new EventHandler<ActionEvent>()
 						{
 							@Override public void handle(ActionEvent e)
 							{
 								@SuppressWarnings("unused")
 								newAppareilWindow monAppareilWindow = new newAppareilWindow();
 							}
 						}).accelerator( KeyCombination.keyCombination("ctrl+E")).build());
 		menuAppareilElectrique.getItems().add(new MenuItem("Modifier/Supprimer"));
 		menuPlanAllumage.getItems().add(new MenuItem("Nouveau"));
 		menuPlanAllumage.getItems().add(new MenuItem("Modifier/Supprimer"));
 		menuAide.getItems().add(new MenuItem("Guide Utilisateur"));
 
 		MenuBar menuBar = new MenuBar();
 
 		menuBar.getMenus().addAll(menuFichier, menuRestaurant, menuCuisine, menuAppareilElectrique,menuPlanAllumage,menuForfait,menuAide);
 
 		/*******************Afficchage des restau et leurs cuisines *********************/
 
 		GridPane grid = new GridPane();
 		grid.setAlignment(Pos.CENTER);
 		grid.setHgap(10);
 		grid.setVgap(10);
 		grid.setPadding(new Insets(25, 25, 25, 25));
 
 		final Label CuisineTitleLabel = new Label("Cuisines du restaurant");
 		grid.add(CuisineTitleLabel, 1, 0);
 		CuisineTitleLabel.setVisible(false); 
 
 
 		final ListView<Cuisine> listCuisine = new ListView<Cuisine>();
 		ObservableList<Cuisine> itemsCuisine = FXCollections.observableArrayList (AppCore.getListeCuisines());
 		listCuisine.setItems(itemsCuisine);
 		listCuisine.setPrefHeight(300);
 		listCuisine.setPrefWidth(200);
 		grid.add(listCuisine,1 ,1);
 		listCuisine.setVisible(false);
 
 		final Label AppareilTitleLabel = new Label("Appareils lectriques de la cuisine");
 		grid.add(AppareilTitleLabel, 2, 0);
 		AppareilTitleLabel.setVisible(false); 
 
 
 		final ListView<AppareilElectrique> listAppareil = new ListView<AppareilElectrique>();
 		ObservableList<AppareilElectrique> itemsAppareil = FXCollections.observableArrayList (AppCore.getListeAppareilsElectriques());
 		listAppareil.setItems(itemsAppareil);
 		listAppareil.setPrefHeight(300);
 		listAppareil.setPrefWidth(200);
 		grid.add(listAppareil,2 ,1);
 		listAppareil.setVisible(false);
 		
 		Label RestaurantTitleLabel = new Label("Nom du restaurant:");
 		grid.add(RestaurantTitleLabel, 0, 0);
 
 		
 		ObservableList<Restaurant> itemsRestaurant = FXCollections.observableArrayList (AppCore.getListeRestaurants());
 		listRestaurant.setItems(itemsRestaurant);
 		listRestaurant.setPrefHeight(300);
 		listRestaurant.setPrefWidth(200);
 		grid.add(listRestaurant,0 ,1);
 
 		Button boutonRafraichir = new Button("Rafrachir");
 		grid.add(boutonRafraichir, 0,3);
 
 		boutonRafraichir.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent e) {
 				ObservableList<Restaurant> itemsRestaurant = FXCollections.observableArrayList (AppCore.getListeRestaurants());
 				listRestaurant.setItems(itemsRestaurant);
 				ObservableList<Cuisine> itemsCuisine = FXCollections.observableArrayList (AppCore.getListeCuisines());
 				listCuisine.setItems(itemsCuisine);
 			}
 		});
 
 		listRestaurant.setOnMouseClicked(new EventHandler<MouseEvent>() {
 
 			@Override
 			public void handle(MouseEvent e) {
 				Restaurant restaurantClique = listRestaurant.getFocusModel().getFocusedItem();
 				ObservableList<Cuisine> itemsCuisine = FXCollections.observableArrayList (restaurantClique.getCuisine());
 				listCuisine.setItems(itemsCuisine);
 				listCuisine.setVisible(true);
 				CuisineTitleLabel.setVisible(true);
 			}
 		}); 
 		
 		listCuisine.setOnMouseClicked(new EventHandler<MouseEvent>() {
 
 			@Override
 			public void handle(MouseEvent e) {
 				Cuisine cuisineClique = listCuisine.getFocusModel().getFocusedItem();
 				ObservableList<AppareilElectrique> itemsAppareil = FXCollections.observableArrayList (cuisineClique.ObtenirAppareils());
 				listAppareil.setItems(itemsAppareil);
 				listAppareil.setVisible(true);
 				AppareilTitleLabel.setVisible(true);
 			}
 			
 		});
 
 
 
 		/*******************Fin Affichage des restaurants et leurs cuisines *********************/
 
		final NumberAxis xAxis = new NumberAxis(0, 24, 1);
 	    final NumberAxis yAxis = new NumberAxis();
	    xAxis.setMinorTickCount(0);
 		final AreaChart<Number,Number> ac = new AreaChart<Number,Number>(xAxis,yAxis);
 	    
 		ac.setTitle("Consommation electrique en fonction de la puissance en kW");
		
 	    ac.setPadding(new Insets(30));
 	    GridPane Grille = new GridPane();
 	    GridPane MainGrille = new GridPane();
 	    
 	    Grille.add(ac, 0, 0);
 		final Group root = new Group();  
 		MainGrille.add(grid, 0, 0);
 		MainGrille.add(Grille, 0, 1);
 		
 		
		root.getChildren().add(MainGrille);
		root.getChildren().add(menuBar);	
 		Scene MyScene = new Scene(root, 1000, 600);
 		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
 		MainGrille.prefWidthProperty().bind(primaryStage.widthProperty());
 		Grille.prefWidthProperty().bind(primaryStage.widthProperty());
 		grid.prefWidthProperty().bind(primaryStage.widthProperty());
 		grid.setAlignment(Pos.BASELINE_LEFT);
 		ac.prefWidthProperty().bind(primaryStage.widthProperty());
 		Grille.setAlignment(Pos.BASELINE_CENTER);
 		MyScene.getStylesheets().add(main.class.getResource("style.css").toExternalForm());
 		
 		NumberBinding multiplication = Bindings.divide(primaryStage.heightProperty(),2);
 		Grille.prefHeightProperty().bind(primaryStage.heightProperty());
 		grid.prefHeightProperty().bind(multiplication);
 		ac.prefHeightProperty().bind(multiplication);
 		
 		primaryStage.setScene(MyScene);
 		
 		AppCore.LoadAppareilElectrique();
 		AppCore.LoadCuisine();
 		AppCore.LoadRestaurant();	
 	
 		
 		
 		
 		primaryStage.show();
 
 	}
 }
