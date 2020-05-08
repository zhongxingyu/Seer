 package org.noip.staticattic.GUI;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import org.noip.staticattic.GUI.Events.ShowCharacterCreationScreen;
 import org.noip.staticattic.GUI.Events.ShowMenuScreen;
 import org.noip.staticattic.GUI.Events.ShowTitleScreen;
 import org.noip.staticattic.entities.Player;
 import org.noip.staticattic.entities.Tile;
 import org.noip.staticattic.entities.Human.AnimationState;
 import org.noip.staticattic.fileutils.TextureHandler;
 import org.noip.staticattic.world.Environment;
 import org.noip.staticattic.world.EnvironmentHandler;
 
 public class MainWindow extends JFrame implements ActionListener {
 	
 	private static final long serialVersionUID = 1L;
 	public Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
 	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);
 	private UIHandler UIhandler = new UIHandler();
 	private EnvironmentHandler ENhandler = new EnvironmentHandler(this);
 	public JPanel mainpanel;
 	private Player player;
 	private MainWindow main;
 	
 	public MainWindow() {
 		
 		main = this;
 		
 		this.setTitle("Village");
 		this.setUndecorated(true);
 		this.setResizable(false);
         this.setBounds(0,0,screensize.width, screensize.height);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		
 		this.setIconImage(TextureHandler.getIcon().getImage());
 		
 		mainpanel = new JPanel();
 		mainpanel.setVisible(true);
 		mainpanel.setLayout(null);
 		
 		mainpanel.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "rightpressed");
 		mainpanel.getActionMap().put("rightpressed", new RightPressed());
 		
 		mainpanel.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "leftpressed");
 		mainpanel.getActionMap().put("leftpressed", new LeftPressed());
 		
 		mainpanel.getInputMap().put(KeyStroke.getKeyStroke("UP"), "uppressed");
 		mainpanel.getActionMap().put("uppressed", new UpPressed());
 		
 		mainpanel.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "downpressed");
 		mainpanel.getActionMap().put("downpressed", new DownPressed());
 		
 		mainpanel.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escpressed");
 		mainpanel.getActionMap().put("escpressed", new EscPressed());
 		
 		this.add(mainpanel);
 		this.setVisible(true);
 
 		executor.scheduleAtFixedRate(UIhandler, 0L, 20L, TimeUnit.MILLISECONDS);
 		
 		UIhandler.addToQueue(new ShowTitleScreen(this, 0));
 		UIhandler.addToQueue(new ShowMenuScreen(this, 4000));
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent a) {
 		
 		if (a.getActionCommand().equals("exit")) {
 			
 			ExitApplication();
 			
 		} else if (a.getActionCommand().equals("newgame")) {
 			
 			UIhandler.addToQueue(new ShowCharacterCreationScreen(this, 0));
 			
 		} else if (a.getActionCommand().equals("createcharacter")) {
 			
 			player = new Player((int)(screensize.getWidth()/2)-17, (int)(screensize.getHeight()/2)-22);
 			ENhandler.setMainEnvironment(new Environment());
 			
 			player.setCurrentEnvironment(ENhandler.getMainEnvironment());
 			
 			Tile[][] array = player.getCurrentEnvironment().getArray();
 			player.setCurrentTile(array[5][5]);
			player.setXID(49);
			player.setYID(49);			
 			
 			mainpanel.removeAll();
 			
 			executor.scheduleAtFixedRate(ENhandler, 0L, 20L, TimeUnit.MILLISECONDS);
 			
 		}
 		
 	}
 	
 	public void ExitApplication() {
 		
 		executor.shutdownNow();
 		System.exit(0);
 		
 	}
 
 	public Player getPlayer() {
 		
 		return player;
 		
 	}
 
 	public void setPlayer(Player player) {
 		
 		this.player = player;
 		
 	}
 	
 	class RightPressed extends AbstractAction {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			
 			if (player.getAnimationState().equals(AnimationState.IDLE_RIGHT)) {
 			
 				//player.setAnimationState(AnimationState.WALKING_RIGHT);
 				ENhandler.moveLeft();
 				
 			} else {
 				
 				player.setIcon(TextureHandler.getPlayerRight());
 				player.getLabel().setIcon(player.getIcon());
 				
 				player.setAnimationState(AnimationState.IDLE_RIGHT);
 				
 			}
 			
 		}
 	}
 	
 	class LeftPressed extends AbstractAction {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 
 			if (player.getAnimationState().equals(AnimationState.IDLE_LEFT)) {
 			
 				//player.setAnimationState(AnimationState.WALKING_LEFT);
 				ENhandler.moveRight();
 				
 			} else {
 				
 				player.setIcon(TextureHandler.getPlayerLeft());
 				player.getLabel().setIcon(player.getIcon());
 				
 				player.setAnimationState(AnimationState.IDLE_LEFT);
 				
 			}
 			
 		}
 	}
 	
 	class UpPressed extends AbstractAction {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 
 			if (player.getAnimationState().equals(AnimationState.IDLE_UP)) {
 				
 				//player.setAnimationState(AnimationState.WALKING_UP);
 				ENhandler.moveDown();
 				
 			} else {
 				
 				player.setIcon(TextureHandler.getPlayerUp());
 				player.getLabel().setIcon(player.getIcon());
 				
 				player.setAnimationState(AnimationState.IDLE_UP);
 				
 			}
 			
 		}
 	}
 	
 	class DownPressed extends AbstractAction {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			
 			if (player.getAnimationState().equals(AnimationState.IDLE_DOWN)) {
 			
 				//player.setAnimationState(AnimationState.WALKING_DOWN);
 				ENhandler.moveUp();
 				
 			} else {
 				
 				player.setIcon(TextureHandler.getPlayerDown());
 				player.getLabel().setIcon(player.getIcon());
 				
 				player.setAnimationState(AnimationState.IDLE_DOWN);
 				
 			}
 			
 		}
 	}
 	
 	class EscPressed extends AbstractAction {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			
 			UIhandler.purgeQueue();
 			UIhandler.addToQueue(new ShowMenuScreen(main, 0));
 			
 		}
 	}
 	
 }
