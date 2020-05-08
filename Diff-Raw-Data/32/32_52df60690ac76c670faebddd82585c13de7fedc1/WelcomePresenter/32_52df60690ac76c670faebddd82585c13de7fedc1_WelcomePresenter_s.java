 package edu.gatech.statusquo.spacetrader.presenter;
 
 
 import java.io.IOException;
 
 import edu.gatech.statusquo.spacetrader.model.Player;
 import edu.gatech.statusquo.spacetrader.view.*;
 import edu.gatech.statusquo.spacetrader.driver.*;
 
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 
 public class WelcomePresenter {
 	private WelcomeView welcomeView;
 	private Driver driver;
 	
 	/**
 	 * class constructor using listed parameters
 	 * @param d
 	 * @param wv
 	 */
 	public WelcomePresenter(Driver d, WelcomeView wv) {
 		this.driver = d;
 		this.welcomeView = wv;
 		
 		setListeners();
 		
 		while (!welcomeView.shell.isDisposed()) {
 			if (!welcomeView.display.readAndDispatch()) {
 				welcomeView.display.sleep();
 			}
 		}
 	}
 	
 	/**
 	 * sets listeners
 	 */
 	private void setListeners() {
 		welcomeView.btnStartGame.addMouseListener(new MouseAdapter() {
 			@Override
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see
 			 * org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt
 			 * .events.MouseEvent) So this is where the mousedown event happens.
 			 * Notice I set the visibility of all the parts of the first window
 			 * to false, and then I ran the method that would create the
 			 * CharacterCreationScreen in the same shell. -Shawn
 			 */
 			public void mouseDown(MouseEvent e) {
 				welcomeView.btnStartGame.setVisible(false);
 				welcomeView.btnLoadGame.setVisible(false);
 				welcomeView.lblWelcomeToSpace.setVisible(false);
 				// need to return to driver to signify need to create next view
 				//createCharacterCreationScreen();
 				welcomeView.shell.dispose();
 				Driver.player = new Player();
 				driver.generateCreatePlayer();
 		    
                 try {
                     Driver.generateUniverse();
                 } catch (IOException e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
              
 		        
 
 		        int startLocation = (int) (Math.random()*150);
		        Driver.player.setPlayerX(Driver.X.get(startLocation));
		        Driver.player.setPlayerY(Driver.Y.get(startLocation));
 		        Driver.currentSystem = Driver.player.findSystem(Driver.player.getPlayerX(),Driver.player.getPlayerY());
                 driver.generateMainGame();
 				
 			}
 		});
 		
 		welcomeView.btnLoadGame.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				welcomeView.btnStartGame.setVisible(false);
 				welcomeView.btnLoadGame.setVisible(false);
 				welcomeView.lblWelcomeToSpace.setVisible(false);
 				welcomeView.shell.dispose();
 				driver.loadGame();
 				driver.generateMainGame();
 			}
 		});
 
 	}
 }
