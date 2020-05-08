 package vooga.fighter.controller;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import arcade.games.ArcadeInteraction;
 import arcade.games.Game;
 import arcade.games.GameData;
 import arcade.games.UserGameData;
 import util.Pixmap;
 import vooga.fighter.model.loaders.MapLoader;
import vooga.fighter.util.Text;
 import vooga.fighter.view.Canvas;
 import vooga.fighter.controller.ControllerManager;
 import vooga.fighter.controller.GameInfo;
 
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 
 
 public class GameManager {//extends Game{
 public static final Dimension SIZE = new Dimension(800, 600);
     public static final String TITLE = "Fighter!";
     public static final int THREE_TOP_HIGH_SCORES = 3;
     private Canvas myCanvas;
     private ControllerManager myControllerManager;
     private GameInfo myGameInfo;
 
     public GameManager(){//ArcadeInteraction arcade) {
     	//super(arcade);
         myCanvas = new Canvas(SIZE); 
         myGameInfo = new GameInfo(new MapLoader().getMapNames());
         //myGameInfo.setHighScores(getArcade().getHighScores(THREE_TOP_HIGH_SCORES));
         ControllerFactory factory = makeFactory(myCanvas);
         ControlProgressionManager progressionmanager = new ControlProgressionManager(myGameInfo);
         myControllerManager = new ControllerManager(myCanvas, myGameInfo, factory, progressionmanager);
         JFrame frame = new JFrame(TITLE);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         // add our user interface components
         frame.getContentPane().add(myCanvas, BorderLayout.CENTER);
         // display them
         frame.pack();
         frame.setVisible(true);
 	}
     
     
 	 public void run (){
 		 myControllerManager.run();
 	    }
 	 
 	 public ControllerFactory makeFactory(Canvas canvas){
 		 return new ControllerFactory(canvas);
 	 }
 
 	 public UserGameData generateNewProfile(){
 		return myGameInfo;
 	 }
 
 
 }
