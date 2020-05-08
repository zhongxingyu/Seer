 package main;
 import java.awt.Dimension;
import java.io.File;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 
 import view.GameView;
 import view.MenuDifficultyView;
 import view.MenuHomeView;
 import view.MenuLevelView;
 import view.MenuLoadView;
 import view.MenuNewView;
 import view.MenuOptionsView;
 import controller.GameController;
 import controller.MenuController;
 import model.GameModel;
 import model.Player;
 import model.entities.Entity;
 
 @SuppressWarnings("serial")
 public class GameFrame extends JFrame {
 	private MenuHomeView angryMenuHomeView;
 	private MenuNewView angryMenuNewView;
 	private MenuLoadView angryMenuLoadView;
 	private MenuOptionsView angryMenuOptionsView;
 	private MenuDifficultyView angryMenuDifficultyView;
 	private MenuLevelView angryMenuLevelView;
 	private GameView angryView;
 	private GameController angryController;
 	private MenuController angryMenuController;
 	private GameModel angryModel;
 	private String winName;
 	ArrayList<Entity> angryEntities;
 	ArrayList<Player> angryPlayers;
 	
 	public GameFrame(String name) {
 		winName = name;
 		
		//creation du dossier de sauvegardes
		File fb = new File("save/"); 
		fb.mkdir();
		
 		angryModel = new GameModel();
 		angryEntities = angryModel.getEntityList();
 		angryPlayers = angryModel.getPlayers();
 		
 		//Views
 		angryMenuHomeView = new MenuHomeView();
 		angryMenuNewView = new MenuNewView();
 		angryMenuLoadView = new MenuLoadView(angryPlayers);
 		angryMenuOptionsView = new MenuOptionsView();
 		angryMenuDifficultyView = new MenuDifficultyView();
 		angryMenuLevelView = new MenuLevelView();
 		
 		angryView = new GameView(angryEntities);
 		
 		angryModel.setAngryView(angryView);
 		
 		//Controller 
 		angryController = new GameController(this);
 		angryMenuController = new MenuController(this);
 		
 		//Listener
 		angryMenuHomeView.addKeyListener(angryMenuController);
 		angryMenuNewView.addKeyListener(angryMenuController);
 		angryMenuNewView.addMouseListener(angryMenuController);
 		angryMenuLoadView.addKeyListener(angryMenuController);
 		angryMenuLoadView.addMouseListener(angryMenuController);
 		angryMenuOptionsView.addKeyListener(angryMenuController);
 		angryMenuDifficultyView.addKeyListener(angryMenuController);
 		angryMenuLevelView.addKeyListener(angryMenuController);
 		
 		angryView.addKeyListener(angryController);
 		angryView.addMouseListener(angryController);
 		angryView.addMouseMotionListener(angryController);
 		angryModel.addListListener(angryView);
		angryModel.addListListener(angryController);	
 
 		this.setContentPane(angryMenuHomeView);
 		
 		this.setTitle(winName);
 		this.setSize((int)GameFrame.getFrameSize().getWidth(), (int)GameFrame.getFrameSize().getHeight());
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(false);
 		this.setVisible(true);		
     }
 
 	public GameView getAngryView() {
 		return angryView;
 	}
 	
 	public MenuHomeView getAngryMenuHomeView()
 	{
 		return angryMenuHomeView;
 	}
 	
 	public MenuNewView getAngryMenuNewView()
 	{
 		return angryMenuNewView;
 	}
 	
 	public MenuLoadView getAngryMenuLoadView()
 	{
 		return angryMenuLoadView;
 	}
 	
 	public MenuOptionsView getAngryMenuOptionsView()
 	{
 		return angryMenuOptionsView;
 	}
 	
 	public MenuDifficultyView getAngryMenuDifficultyView()
 	{
 		return angryMenuDifficultyView;
 	}
 	
 	public MenuLevelView getAngryMenuLevelView()
 	{
 		return angryMenuLevelView;
 	}
 	
 	public GameModel getAngryModel()
 	{
 		return angryModel;
 	}
 	public GameController getController()
 	{
 		return angryController;
 	}
 
 	public void setMenuLevel()
 	{
 
 		this.setContentPane(angryMenuLevelView);
 		angryMenuLevelView.requestFocus();
 		this.setVisible(true);
 		
 	}
 	
 	public void setGame()
 	{
 		this.setContentPane(angryView);
 		angryView.requestFocus();
 		this.setVisible(true);
 		
 	}
 	
 	public void setPlayers(ArrayList<Player> players) {
 		angryModel.setPlayers(players);
 	}
 	
 	public void setCurrentPlayer(Player p) {
 		angryModel.setCurrentPlayer(p);
 	}
 	
 	public void setDifficulty(String dif) {
 		angryModel.setDifficulty(dif);
 	}
 	
 	public void setCurrentLevel(int l) {
 		angryModel.setCurrentLevel(l);
 	}
 	
 	public void setCurrentHighScore() {
 		angryModel.setCurrentHighScore();
 	}
 	
 	static public Dimension getFrameSize() {
 		Dimension frameSize = new Dimension(1200,600);
 		return frameSize;
 	}
 	
 }
