 package game;
 import java.awt.event.KeyEvent;
 
 import network.NetworkManager;
 
 import std.StdDraw;
 import std.StdIO;
 import std.StdIO.KeyEventType;
 import game.checkpoint.Checkpoint;
 import game.item.ItemInstance;
 import game.item.ItemType;
 import game.player.Player;
 import gamestate.GameStates;
 import gamestate.GlobalGameState;
 import gamestate.IGameState;
 import map.Map;
 import monster.EnemyPlayer;
 
 
 public class GSGame implements IGameState, StdIO.IKeyListener {
 	private Player m_player;
 	private Map m_map;
 	private GameInterface m_ui;
 	private static GSGame s_this;
 	private boolean m_isTyping = false;
 	
 	boolean s_shutdown = false;
 
 	public static GSGame getInstance(){
 		return s_this;
 	}
 
 	@Override
 	public void render() {	
 		StdDraw.picture(0,0, "data/ui/background.png");
 		
 		StdDraw.setAlpha(0.33f);
 		StdDraw.setPenColor(StdDraw.BLACK);
 		StdDraw.filledRectangle(0,0,800,600);
 		StdDraw.setAlpha(1.0f);
 		
 		m_map.render();
 		m_player.render();
 		m_ui.render();
 		ChatWindow.render();
 		
 		StdDraw.setPenColor(StdDraw.BLACK);
 		StdDraw.setPenRadius(2.f);
 		StdDraw.rectangle(44, 44, 512, 512);
 		StdDraw.setPenRadius(1.f);
 	}
 
 	@Override
 	public void update() {
 		NetworkManager.update();
 		
 		m_player.update(m_map);
 		
 		Map.getInstance().getMonsterPool().updateAll();
 		Map.getInstance().getMonsterPool().issueRandomWanderOrders();
 		
 		if(m_player.isAlive() == false){
 			System.out.println("Error! Player alive check not passed in GSGame.");
 		}
 		
 	}
 
 	private void resetPlayer(){
 	
 		if( !m_map.loadLevel("data/outside1.txt") ){
 			s_shutdown = true;
 			return;
 		}
 		
 		m_player.setPosition( m_map.getCanvasX(3), m_map.getCanvasY(11) );
 		
 	}
 	
 	public void startGameMulti(boolean isHost){
 		m_ui = new GameInterface();
 		m_player = new Player();
 		
 		//Erstellt die Map an der Position 144,44 (von oben links gesehen).
 		//Die Zahlen sind so gewhlt, dass die Map genau in der Mitte des Fensters ist.
 		m_map = new Map(44,44, "data/tiles.txt");
		m_map.loadLevel("data/mplayer.txt");
 				
 		EnemyPlayer p = new EnemyPlayer( 0, 0, "data/player/deep_elf_m.png" ); //m_map.getCanvasX(5), m_map.getCanvasY(5)
 
 		if(isHost){
			m_player.setPosition( m_map.getCanvasX(5), m_map.getCanvasY(4) );
			p.setPosition( m_map.getCanvasX(5), m_map.getCanvasY(8) );
 		}else{
			p.setPosition( m_map.getCanvasX(5), m_map.getCanvasY(4) );
			m_player.setPosition( m_map.getCanvasX(5), m_map.getCanvasY(8) );
 		}
 		
 		if( isHost)
 			NetworkManager.declareHost();
 			
 		NetworkManager.setEnemyPlayer( p );
 		m_map.getMonsterPool().addMonster( p );
 		
		Player.getInstance().getInventory().addItem( new ItemInstance(ItemType.getItemType("leatherarmor") ) );
 		Checkpoint.save();
 	}
 	
 	public void startGameSingle(){
 		m_ui = new GameInterface();
 		m_player = new Player();
 		
 		//Erstellt die Map an der Position 144,44 (von oben links gesehen).
 		//Die Zahlen sind so gewhlt, dass die Map genau in der Mitte des Fensters ist.
 		m_map = new Map(44,44, "data/tiles.txt");
 				
 		resetPlayer();
 		Checkpoint.save();
 	}
 
 	public GSGame(){
 		s_this = this;
 	}
 
 
 	@Override
 	public void onEnter() {
 		StdIO.addKeyListener(this, KeyEventType.KeyReleased);
 		
 		ChatWindow.setPosition(560, 455);
 	}
 
 	@Override
 	public void onExit() {
 		StdIO.removeKeyListener(this, KeyEventType.KeyReleased);
 	}
 
 	@Override
 	public void receiveEvent(KeyEvent e) {
 	
 		if( NetworkManager.isMultiplayer()){
 			if(m_isTyping){
 				
 				if( ChatWindow.processKey(e) ){
 					m_isTyping = false;
 					Player.getInstance().setMovable(true);
 				}
 				return;
 			}
 		
 			if(e.getKeyCode() == KeyEvent.VK_Z){
 				m_isTyping = true;
 				Player.getInstance().setMovable(false);
 				return;
 			}
 		}
 	
 		if(e.getKeyCode() == KeyEvent.VK_I){
 			GlobalGameState.setActiveGameState(GameStates.INVENTORY);
 		}else if(e.getKeyCode() == KeyEvent.VK_ENTER){
 			m_player.attempInteraction();
 		}
 	}
 }
