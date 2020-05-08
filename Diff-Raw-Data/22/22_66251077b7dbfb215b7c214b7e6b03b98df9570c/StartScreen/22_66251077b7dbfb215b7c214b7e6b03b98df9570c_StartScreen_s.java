 package riskyspace.view.lobby;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Observer;
 
 import javax.swing.JPanel;
 import javax.xml.bind.ParseConversionEvent;
 
 import riskyspace.data.GameDataHandler;
 import riskyspace.data.Settings;
 import riskyspace.sound.PlayList;
 import riskyspace.view.Action;
 import riskyspace.view.Clickable;
 import riskyspace.view.IMenu;
 import riskyspace.view.swing.SwingRenderAble;
 import riskyspace.view.swing.impl.SwingButton;
 
 public class StartScreen extends JPanel {
 	
 	/**
 	 * Serializable ID
 	 */
 	private static final long serialVersionUID = -7626063623723164535L;
 	private int width;
 	private int height;
 	
 	private boolean startScreenVisible = true;
 	
 	private Lobby localLobby = null;
 	private IMenu loadGameMenu = null;
 	
 	private PreMultiplayerMenu preMultiplayerMenu = null;
 	
 	private SwingButton localGame = null;
 	private SwingButton musicButton = null;
 	private SwingButton multiplayer = null;
 	private SwingButton loadGame = null;
 	private SwingButton exit = null;
 	private SwingButton backButton = null;
 	
 	private ClickHandler clickHandler = null;
 	
 	private PlayList playlist = null;
 	
 	/*
 	 * Background
 	 */
 	private Image backGround = null;
 	private Image title = null;
 
 
 	public StartScreen(PlayList playlist) {
 		measureScreen();
 		backGround = Toolkit.getDefaultToolkit().getImage("res/menu/lobby/background3.png").
 				getScaledInstance(width, height, Image.SCALE_DEFAULT);
 		title = Toolkit.getDefaultToolkit().getImage("res/menu/lobby/sector_zero.png").
 				getScaledInstance(500, 130, Image.SCALE_DEFAULT);
 		createButtons();
 		createMenus();
 		clickHandler = new ClickHandler();
 		this.playlist = playlist;
 		this.playlist.start();
 		addMouseListener(clickHandler);
 	}	
 
 	public void setObserver(Observer o) {
 		localLobby.setObserver(o);
 		preMultiplayerMenu.setObserver(o);
 	}
 	
 	private void measureScreen() {
 		width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 		height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
 	}
 
 	public void createMenus() {
 		localLobby = new Lobby(width/6, height/6, 2*width/3, 2*height/3);
 		preMultiplayerMenu = new PreMultiplayerMenu(4*width/10, height/3, width/5, 2*height/6);
 		
 		/* Convert game name String[] to ArrayList */
 		String[] savedList = GameDataHandler.getSavedGames();
 		ArrayList<String> gameList = new ArrayList<String>();
 		
 		for (int i = 0; i < savedList.length; i++) {
 			gameList.add(savedList[i]);
 		}
 		loadGameMenu = new LoadGameMenu<String>(width/6, height/6, 2*width/3, 2*height/3, gameList);
 	}
 	
 	private void hideMenus() {
 		localLobby.setVisible(false);
 		preMultiplayerMenu.setVisible(false);
 		loadGameMenu.setVisible(false);
 	}
 	
 	public void createButtons() {
 		localGame = new SwingButton(width/2 - 125, height/2 - 200, 250, 50);
 		localGame.setImage("res/menu/lobby/local_game_button.png");
 		localGame.setAction(new Action(){
 			@Override
 			public void performAction() {
 				localLobby.setGameCreate(null);
 				startScreenVisible = false;
 			}
 		});
 		multiplayer = new SwingButton(width/2 - 125, height/2 - 200 + 100, 250, 50);
 		multiplayer.setImage("res/menu/lobby/multiplayer_button.png");
 		multiplayer.setAction(new Action(){
 			@Override
 			public void performAction() {
 				preMultiplayerMenu.setVisible(true);
 				startScreenVisible = false;
 			}
 		});
 		loadGame = new SwingButton(width/2 - 125, height/2 - 200 + 200, 250, 50);
 		loadGame.setImage("res/menu/lobby/load_game_button.png");
 		loadGame.setAction(new Action(){
 			@Override
 			public void performAction() {
 				loadGameMenu.setVisible(true);
 				startScreenVisible = false;
 			}
 		});
 		musicButton = new SwingButton(width/2 - 125, height/2 - 200 + 300, 250, 50);
 		if (Settings.isMusicOn()) {
 		     musicButton.setImage("res/menu/lobby/music_on_button.png");
 		 } else {
 			 musicButton.setImage("res/menu/lobby/music_off_button.png");
 		 }
 		musicButton.setAction(new Action(){
 			@Override
 			public void performAction() { 
 				 if (Settings.isMusicOn()) {
 					 Settings.setProperty("music_enabled=false");
 				     musicButton.setImage("res/menu/lobby/music_off_button.png");
 				     playlist.pause();
 				 } else {
 					 Settings.setProperty("music_enabled=true");
 					 musicButton.setImage("res/menu/lobby/music_on_button.png");
 					 playlist.start();
 				 }
 			}
 		});
 		exit = new SwingButton(width/2 - 125, height/2 - 200 + 400, 250, 50);
 		exit.setImage("res/menu/lobby/exit_game_button.png");
 		exit.setAction(new Action(){
 			@Override
 			public void performAction() {
 				System.exit(0);
 			}
 		});
 		backButton = new SwingButton(width/2 - width/3 + 10, height/2 + height/3 - 60, 180, 50);
 		backButton.setImage("res/menu/lobby/back_button.png");
 		backButton.setAction(new Action(){
 			@Override
 			public void performAction() {
				startScreenVisible = true;
				preMultiplayerMenu.close();
 			}
 		});
 	}
 	
 	public void paintComponent(Graphics g) {
 		
 		g.drawImage(backGround, 0, 0, null);
 		g.drawImage(title, width/2 - 250, height/20, null);
 		
 		if (!startScreenVisible) {
 			backButton.draw(g);
 			if (localLobby.isVisible()) {
 				((SwingRenderAble) localLobby).draw(g);
 			}
 			if (loadGameMenu.isVisible()) {
 				((SwingRenderAble) loadGameMenu).draw(g);
 			}
 			((SwingRenderAble) preMultiplayerMenu).draw(g);
 		}
 		else if (startScreenVisible) {
 			localGame.draw(g);
 			multiplayer.draw(g);
 			loadGame.draw(g);
 			musicButton.draw(g);
 			exit.draw(g);
 		}
 	}
 
 	private class ClickHandler implements MouseListener {
 		/*
 		 * Click handling for different lobby menus
 		 */
 		public boolean menuClick(Point point) {
 			if (startScreenVisible) {
 				if (localGame.mousePressed(point)) {return true;}
 				if (multiplayer.mousePressed(point)) {return true;}
 				if (loadGame.mousePressed(point)) {return true;}
 				if (musicButton.mousePressed(point)) {return true;}
 				if (exit.mousePressed(point)) {return true;}
 				
 			}
 			if (backButton.mousePressed(point)) {
 				hideMenus();
 				return true;
 			}
 			if (preMultiplayerMenu instanceof Clickable) {
 				if (((Clickable) preMultiplayerMenu).mousePressed(point)) {
 					return true;
 				}
 			}
 			if (localLobby instanceof Clickable) {
 				if (((Clickable) localLobby).mousePressed(point)) {
 					return true;
 				}
 			}
 			if (loadGameMenu instanceof Clickable) {
 				if (((Clickable) loadGameMenu).mousePressed(point)) {
 					return true;
 				}
 			}
 			
 			return false;
 		}
 		
 		private boolean menuRelease(Point point) {
 			if (localLobby instanceof Clickable) {
 				if (((Clickable) localLobby).mouseReleased(point)) {
 					return true;
 				}
 			}
 			if (loadGameMenu instanceof Clickable) {
 				if (((Clickable) loadGameMenu).mouseReleased(point)) {
 					return true;
 				}
 			}
 			
 			return false;
 		}
 
 		@Override public void mouseClicked(MouseEvent me) {}
 		@Override public void mouseReleased(MouseEvent me) {
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				if (menuRelease(me.getPoint())) {return;}
 			}
 		}
 
 		@Override public void mouseEntered(MouseEvent me) {}
 		@Override public void mouseExited(MouseEvent me) {}
 		@Override public void mousePressed(MouseEvent me) {
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				if (menuClick(me.getPoint())) {return;}
 			}
 		}
 	}
 
 	public KeyListener getKeyListener() {
 		return preMultiplayerMenu.getKeyListener();
 	}
 }
