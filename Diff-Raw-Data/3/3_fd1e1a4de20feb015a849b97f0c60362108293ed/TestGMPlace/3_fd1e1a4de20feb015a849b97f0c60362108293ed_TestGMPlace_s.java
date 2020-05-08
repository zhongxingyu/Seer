 package DD.MapTool;
 
 import java.io.Console; 
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import slick.path2glory.SimpleGame.TestGame;
 import DD.ActionBox.ActionBox;
 import DD.Character.DDCharacter;
 import DD.Character.Abilities.Ability;
 import DD.Character.Abilities.DefaultAbilities.Move.Move;
 import DD.Character.CharacterSheet.CharacterClass;
 import DD.Character.CharacterSheet.CharacterSheet;
 import DD.CombatSystem.CombatSystem;
 import DD.CombatSystem.TargetingSystem.Coordinate;
 import DD.CombatSystem.TargetingSystem.TargetingSystem;
 import DD.GMToolsBox.GMToolsBox;
 import DD.GMToolsBox.HolderTuple;
 import DD.GMToolsBox.PlaceCharacter;
 import DD.Message.ChooseTargetMessage;
 import DD.Network.NetworkSystem;
 import DD.SlickTools.Component;
 import DD.SlickTools.DDImage;
 import DD.System.DDSystem;
 
 // @author Carlos Vasquez
 
 public class TestGMPlace extends BasicGame
 {
 	public TestGMPlace(String title) {
 		super(title);
 
 	}
  
     public TestGMPlace()
     {
         super("Slick2DPath2Glory - SimpleGame");
     }
  
     @Override
     public void init(GameContainer gc) 
 			throws SlickException {
     	int i = 0; // component and entity ids
     	DDSystem system = new DDSystem();
     	ActionBox ab = new ActionBox(i++, 0, 0);
     	GMToolsBox gmt = new GMToolsBox(i++,0,0);
     	system.linkBoxes(ab);
     	system.server();
 		
 		/*
 		 * IMPORTANT: for this main to work, mapSize needs to be 21 or larger due to hardcodes massPlaceObjectsLine values.
 		 * other wise you will get nullpointers.
 		 */
 		
 		/* VERY FREAKING IMPORTANT STUFF */
 		
 		/* Initialize sheets */
 		int size = 10;
 		CharacterSheet[] sheet = new CharacterSheet[size];
 		for(int j = 0; j < size; j++) sheet[j] = new CharacterSheet();
 		
 		/* make sheets: */
 		for (int j = 0; j < size; j++)
 		{
 			sheet[j].fillBasic("Name" + j, 
 					"Skanks-Worth", 
 					0, 
 					"Elvish, Common", 
 					0, 
 					1, 
 					5, 
 					150, 
 					200, 
 					"Chaotic Neutral", 
 					"Apple", 
 					"Noble", 
 					"Archer");
 			sheet[j].fillAbilities();
 			CharacterClass barb = sheet[j].chooseClass(0);	//this is barbarian
 			sheet[j].fillRecorder(barb);
 			sheet[j].fillAttacksAndDefense(barb);
 			sheet[j].setNetID(NetworkSystem.GM_USER_ID);
 			sheet[j].setImage(new DDImage("Images/Test/DungeonCrawl_ProjectUtumnoTileset.png", 2530, 1440, 33, 34));
 		} /* end for loop */
 		
 		
 		
 		/* gm tools time */
 		HolderTuple[] holder;
 		Integer[] inPlay;
 		Component[] comp;
 		PlaceCharacter[] place = new PlaceCharacter[size];
 		String temp;
 		for (int k = 0; k < size; k++)
 		{
 			System.out.println("Adding character " + sheet[k].getName() + ": ");
 			place[k] = gmt.addCharacter(GMToolsBox.Holder.MOB, sheet[k]);
 			
 			System.out.println("Characters in mob holder:");
 			holder = gmt.getHolder(GMToolsBox.Holder.MOB);
 			for(int j = 0; j < holder.length; j++) System.out.println("Component ID: " + holder[j].id + " Name: " + holder[j].sheet.getName());
 			
 			System.out.println("Characters in player holder:");
 			holder = gmt.getHolder(GMToolsBox.Holder.PLAYER);
 			for(int j = 0; j < holder.length; j++) System.out.println("Component ID: " + holder[j].id + " Name: " + holder[j].sheet.getName());
 			
 			System.out.println("Characters in play:");
 			inPlay = gmt.getCharactersInPlay();
 			temp = "";
 			for (int j = 0; j < inPlay.length; j++) temp += (inPlay[j] + ", " );
 			System.out.println(temp);
 			
 			System.out.println("Components left:");
 			comp = gmt.getComponents();
 			for (int j = 0; j < comp.length; j++) System.out.println("CompID: " + comp[j].getId());
 		}
 		
 		/* VERY FREAKING IMPORTANT STUFF */
 		World world = new World("TESTME");
 		system.setMap(world.world[0][0]);
 	
 
 		Wall wall = new Wall("walll", null,0,0, world.world[0][0]);
 		world.world[0][0].massPlaceObjectsLine(0, 0, 10, 0, wall);
 		world.world[0][0].massPlaceObjectsLine(0, 1, 0, 19, wall);
 		world.world[0][0].massPlaceObjectsLine(1, 10, 10, 10, wall);
 		world.world[0][0].massPlaceObjectsLine(10, 9, 10, 1, wall);
 		world.world[0][0].massPlaceObjectsLine(1, 19, 10, 19, wall);
 		world.world[0][0].massPlaceObjectsLine(10, 11, 10, 19, wall);
 
 		world.world[0][0].removeObjects(6, 0);
 		world.world[0][0].removeObjects(7, 0);
 		world.world[0][0].removeObjects(6, 10);
 		world.world[0][0].removeObjects(7, 10);
 
 		System.out.println("The world: ");
 		System.out.println(world.world[0][0].toString());
 		
 		int x = 5;
 		int y = 6;
 		int k = 0;
 		
		place[k] = gmt.addCharacter(GMToolsBox.Holder.MOB, sheet[k]);
 		System.out.println("Placing " + sheet[k].getName()  + " at " + x + ", " + y);
 		place[k].testPlace();
 		System.out.println("placement options:");
 		System.out.println(world.world[0][0].toString());
 		ObjectsPriorityStack stack = world.world[0][0].objectsStack[x][y]; // where the character is to be placed
 		((TargetBlock)stack.peek()).select();
 		System.out.println(world.world[0][0].toString());
 		
 		System.out.println("Characters in mob holder:");
 		holder = gmt.getHolder(GMToolsBox.Holder.MOB);
 		for(int j = 0; j < holder.length; j++) System.out.println("Component ID: " + holder[j].id + " Name: " + holder[j].sheet.getName());
 		
 		System.out.println("Characters in player holder:");
 		holder = gmt.getHolder(GMToolsBox.Holder.PLAYER);
 		for(int j = 0; j < holder.length; j++) System.out.println("Component ID: " + holder[j].id + " Name: " + holder[j].sheet.getName());
 		
 		System.out.println("Characters in play:");
 		inPlay = gmt.getCharactersInPlay();
 		temp = "";
 		for (int j = 0; j < inPlay.length; j++) temp += (inPlay[j] + ", " );
 		System.out.println(temp);
 		
 		System.out.println("Components left:");
 		comp = gmt.getComponents();
 		for (int j = 0; j < comp.length; j++) System.out.println("CompID: " + comp[j].getId());
 		
 		
 		
 		/* next character */
 		k = 5;
 		x+= 2;
		place[k] = gmt.addCharacter(GMToolsBox.Holder.MOB, sheet[k]);
 		System.out.println("Placing " + sheet[k].getName()  + " at " + x + ", " + y);
 		place[k].testPlace();
 		System.out.println("placement options:");
 		System.out.println(world.world[0][0].toString());
 		stack = world.world[0][0].objectsStack[x][y]; // where the character is to be placed
 		((TargetBlock)stack.peek()).select();
 		System.out.println(world.world[0][0].toString());
 		
 		System.out.println("Characters in mob holder:");
 		holder = gmt.getHolder(GMToolsBox.Holder.MOB);
 		for(int j = 0; j < holder.length; j++) System.out.println("Component ID: " + holder[j].id + " Name: " + holder[j].sheet.getName());
 		
 		System.out.println("Characters in player holder:");
 		holder = gmt.getHolder(GMToolsBox.Holder.PLAYER);
 		for(int j = 0; j < holder.length; j++) System.out.println("Component ID: " + holder[j].id + " Name: " + holder[j].sheet.getName());
 		
 		System.out.println("Characters in play:");
 		inPlay = gmt.getCharactersInPlay();
 		temp = "";
 		for (int j = 0; j < inPlay.length; j++) temp += (inPlay[j] + ", " );
 		System.out.println(temp);
 		
 		System.out.println("Components left:");
 		comp = gmt.getComponents();
 		for (int j = 0; j < comp.length; j++) System.out.println("CompID: " + comp[j].getId());;
     }
  
     @Override
     public void update(GameContainer gc, int delta) 
 			throws SlickException     
     {
 
     }
  
     public void render(GameContainer gc, Graphics g) 
 			throws SlickException 
     {
 
     }
  
     public static void main(String[] args) 
 			throws SlickException
     {
     	AppGameContainer app = 
 			new AppGameContainer(new TestGMPlace());
  
          //app.setDisplayMode(800, 600, false);
          app.start();
     }
     
     
 }
