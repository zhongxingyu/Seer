 package net.hi117.unique.swingui;
 
 import net.hi117.unique.Game;
 
 import javax.swing.*;
 import java.awt.*;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * @author Yanus Poluektovich (ypoluektovich@gmail.com)
  */
 public class MainFrame extends JFrame {
 
 	private static final String MENU_SCREEN = "Menu screen";
 
 	private static final String GAME_SCREEN = "Game screen";
 
 	private final CardLayout myCardLayout = new CardLayout();
 
 	private final MenuScreen myMenuScreen;
 
 	private final GameScreen myGameScreen;
 
 	public MainFrame() throws InvocationTargetException, InterruptedException {
 		super("Unique");
 
 		getContentPane().setLayout(myCardLayout);
 
 		myMenuScreen = new MenuScreen(new Runnable() {
 			@Override
 			public void run() {
 				myCardLayout.show(getContentPane(), GAME_SCREEN);
 				new GameWorkerThread(new Game()).execute();
 			}
 		});
 		getContentPane().add(myMenuScreen, MENU_SCREEN);
 
 		myGameScreen = new GameScreen(new Callback<Boolean>() {
 			@Override
 			public void doWith(final Boolean victory) {
 				myMenuScreen.setLabel(victory);
 				myCardLayout.show(getContentPane(), MENU_SCREEN);
 			}
 		});
 		getContentPane().add(myGameScreen, GAME_SCREEN);
 
 		myCardLayout.show(getContentPane(), MENU_SCREEN);
 
		setMinimumSize(new Dimension(400, 300));
 		pack();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 	}
 
 }
