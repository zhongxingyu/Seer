 package DD.GUI;
 
 import java.io.File;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 
 import DD.System.DDSystem;
 
 public class Game extends StateBasedGame
 {
 	public static final String gamename = "Dungeons & Dragons!";
 	public static final int Xsize = 1200;
 	public static final int Ysize = 650;
 	
 	public static final int menu = 0;
 	public static final int JoinLob = 1;
 	public static final int LobbyRoom = 2;
 	public static final int createLob = 3;
 	public static final int gameplay = 4;
 	public static final int maptool = 5;
 	public static final int cc = 6; //this is for character creation
 	public static final int gmGameplay = 7;
 	public static DDSystem system;
 	
 	public Game(String gamename)
 	{
 		super(gamename);
 	}
 	
 	public void initStatesList(GameContainer gc) throws SlickException
 	{
 		
 		this.addState(new Menu(menu));
 		this.addState(new JoinLob(JoinLob));
 		this.addState(new LobbyRoom(LobbyRoom));
 		this.addState(new CreateLob(createLob));
		this.addState(new GameplayState(gameplay));
 		this.addState(new CharCreate(cc)); //this is for char create
 		//this.addState(new MapTestState(gameplay));	//for testing only
//		this.addState(new GMGameplayState(gameplay));
 		this.addState(new MapToolState(maptool));
//		this.addState(new GMGameplayState(gmGameplay));
 
 
 		
 		
 		/* Note that initStatesList is where we ADD states. Slick will get them for us. If we have addState
 		 * in the constructor and getState in the initStatesList, then we will run the states twice (and maybe even
 		 * have two instances of the state) */
 //		this.getState(menu).init(gc, this);
 //		this.getState(JoinLob).init(gc, this);
 //		this.getState(LobbyRoom).init(gc, this);
 //		this.getState(createLob).init(gc, this);
 //		this.getState(gameplay).init(gc, this);
 		
 		this.enterState(menu);
 		
 	}
 	
 	public static void main(String[] args)
 	{
 		/* These 2 lines need to be the first lines in the main for us to be able to run a slick based game. 
 		 * Furthermore, the lwjgl.dll needs to be in lib/lwjgl.dll next to the jar when it is exported. 
 		 */
 		System.setProperty("org.lwjgl.librarypath", new File(System.getProperty("user.dir"), "lib").getAbsolutePath());
 		System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
 		system = new DDSystem();
 		
 		
 		AppGameContainer appgc;
 		try{
 			String ddPath = "C:/Program Files (x86)/DD";
 			File theDir = new File(ddPath);
 			if (!theDir.exists())
 			{
 				theDir.mkdir();  
 			}
 			appgc = new AppGameContainer(new Game(gamename));
 			appgc.setDisplayMode(1200, 650, false);
 			appgc.setTargetFrameRate(60);
 			appgc.start();
 		}catch(SlickException e){
 			e.printStackTrace();
 		}
 	}
 
 }
