 package game.gameplayStates;
 
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 import game.StateManager;
 import game.StaticObject;
 import game.interactables.Interactable;
 import game.interactables.InvisiblePortal;
 import game.interactables.RealityExecutable;
 import game.interactables.TableToHack;
 import game.interactables.VirtualDoor;
 import game.interactables.VirtualTrash;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class VirtualRealityHome extends GamePlayState {
 
 	private boolean m_door = false, m_bedState = false, m_tvState = false;
 	private TableToHack m_table;
 	private VirtualDoor m_vdoor;
 	private StaticObject m_bed, m_tv;
 	public VirtualRealityHome(int stateID) {
 		m_stateID = stateID;
 	}
 	
 	@Override
 	public void additionalEnter(GameContainer container, StateBasedGame stateManager) {
 		this.displayDialogue(new String[] {"Wait... this is my room.", 
 				"Why was I brought here? Why is justin beiber on my poster?",
 				"... This can't be my room"});
 	}
 	
 	@Override
 	public void additionalInit(GameContainer container, StateBasedGame stateManager)
 		throws SlickException {
 		
 		// set player initial location
 		m_playerX = SIZE*2;
 		m_playerY = SIZE*4;
 		
 		// set up map
 		this.m_tiledMap = new TiledMap("assets/maps/home.tmx");
 		this.m_map = new simpleMap();
 		this.setBlockedTiles();
 		
 		//set up objects that will not change
 		if (!this.isLoaded()) {
			VirtualTrash vtc = new VirtualTrash("Virtual Trash", 1*SIZE, 3*SIZE);
			this.addObject(vtc, true);
			m_blocked[1][3] = true;
 			
 			StaticObject posters = 
 				new StaticObject("posters", 3*SIZE, 1*SIZE, "assets/gameObjects/bieberPoster.png");
 			this.addObject(posters, false);
 			
 			StaticObject carpet = 
 				new StaticObject("carpet", 3*SIZE, 3*SIZE, "assets/gameObjects/carpet.png");
 			this.addObject(carpet, false);
 			
 			StaticObject bedTable = 
 				new StaticObject("bedTable", 4*SIZE, 4*SIZE, "assets/gameObjects/bedTable.png");
 			bedTable.setDialogue(new String[] {"\"Wow I left my phone in the exact same place\"," +
 					"you see some text on your phone... ",
 					"\"JB is your luck, your curse... and your exit\""});
 			bedTable.setRenderPriority(2);
 			this.addObject(bedTable, true);
 			m_blocked[4][4] = true;
 			
 			// you interact with the table to hack the system
 			try {
 				m_table = new TableToHack("table", SIZE, 4*SIZE);			
 				m_blocked[1][4] = true;
 				m_blocked[1][5] = true;
 				this.addObject(m_table, true);
 			} catch (FileNotFoundException e) { e.printStackTrace();
 			} catch (UnsupportedEncodingException e) { e.printStackTrace();}
 			
 
			//m_vdoor = new VirtualDoor("door", 2*SIZE, 2*SIZE);
			//this.addObject(m_vdoor, true);
 			
 			m_bed = new StaticObject("bed", 3*SIZE, 5*SIZE, "assets/gameObjects/bed.png");
 			m_bed.setDialogue(new String[] {"This is way less comfortable than your bed at home. We're talking rock hard. Might as well sleep on the floor"});
 			m_blocked[3][5] = true;
 			m_blocked[4][5] = true;
 			this.addObject(m_bed, true);
 			
 			/*m_tv = new StaticObject("bed", 4*SIZE, 4*SIZE, "assets/colors/clear.png");
 			m_tv.setDialogue(new String[] {"Nothing good on. Not even teen mom"});
 			this.addObject(m_tv, true);*/
 
 		}
 		
 	}
 	
 	public void additionalUpdate(GameContainer container, StateBasedGame stateManager, int delta) {
 		if(m_door!=m_table.getDoor()){
 			m_door = m_table.getDoor();
 			m_vdoor.setOpen(m_door);
 		}
 		if(m_bedState!=m_table.getBed()){
 			m_bedState = m_table.getBed();
 			if(m_bedState){
 				m_bed.setDialogue(new String[]{"Now this is comfortable! Down feather, silk sheets, and a huge thread count! Maybe you should take a nap. But... what happens if you fall asleep in the virtual world? Maybe you aren't so tired after all."});
 			}else{
 				m_bed.setDialogue(new String[] {"This is way less comfortable than your bed at home. We're talking rock hard. Might as well sleep on the floor"});
 			}
 		}
 		
 		/*if(m_tvState!=m_table.getTV()){
 			m_tvState = m_table.getTV();
 			if(m_tvState){
 				m_tv.setDialogue(new String[] {"Ancient Aliens! I love this show"});
 			}else{
 				m_tv.setDialogue(new String[] {"Nothing good on. Not even teen mom"});
 			}
 		}*/
 	}
 	
 	public void completePuzzles() {
 		InvisiblePortal invisiblePortal;
 		try {
 			StateManager.m_cityState--;
 			invisiblePortal = new InvisiblePortal("invisiblePortal", 4*SIZE, 2*SIZE, StateManager.VIRTUAL_REALITY_ROOM_STATE, 6, 2);
 			this.addObject(invisiblePortal, true);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void setupObjects(int city, int dream) throws SlickException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setupDialogue(GameContainer container, int city, int dream)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dialogueListener(Interactable i) {
 		// TODO Auto-generated method stub
 
 	}
 	public void stage3Complete() {
 		this.displayDialogue(new String[] {"Before you can even touch it the door starts to slowly swing open", "Oh... it's only a closet"+
 				" and an empty one at that", "You see one piece of paper, completely covered in text and decide to pick it up"});
 		try {
 			m_player.getInventory().addItem(new RealityExecutable());
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	public void finish(){
 		this.displayDialogue(new String[]{"A booming voice speaks- \"What have you done!?\""});
 		this.shakeCamera(5000);
 		StateManager.getInstance().enterState(StateManager.VIRTUAL_REALITY_ROOM_STATE, new FadeOutTransition(Color.white, 4000), 
 				new FadeInTransition(Color.white, 1000));
 		VirtualRealityRoom destinationState = (VirtualRealityRoom) StateManager.getInstance().getState(StateManager.VIRTUAL_REALITY_ROOM_STATE);
 		destinationState.setPlayerLocation(6*SIZE, 2*SIZE);
 		destinationState.setRuined();
 	}
 }
