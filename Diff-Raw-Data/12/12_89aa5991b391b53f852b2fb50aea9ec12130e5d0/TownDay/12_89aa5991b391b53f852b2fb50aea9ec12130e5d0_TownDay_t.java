 package game.gameplayStates;
 
 import java.util.ArrayList;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import game.Person;
 import game.Scene;
 import game.StateManager;
 import game.StaticObject;
 import game.GameObject.Types;
 import game.collectables.*;
 import game.interactables.Animal;
 import game.interactables.Banana;
 import game.interactables.ChickenWing;
 import game.interactables.Cigarette;
 import game.interactables.Door;
 import game.interactables.Horse;
 import game.interactables.InvisiblePortal;
 import game.interactables.Interactable;
 import game.interactables.OutsideTrashcan;
 import game.interactables.PortalObject;
 import game.interactables.Wrench;
 import game.quests.Quest;
 import game.quests.QuestGoal;
 import game.quests.QuestReward;
 import game.quests.QuestStage;
 
 public class TownDay extends Town{
 	
 	private boolean m_quest1Given;
 	private boolean m_horsesFreed;
 	private Image m_overlay;
 	
 	public TownDay(int stateID) {
 		m_stateID = stateID;
 	}
 	
 	@Override
 	public void additionalEnter(GameContainer container, StateBasedGame stateManager){
 //		m_horsesFreed = true;
 		if(m_horsesFreed){
 			m_horsesFreed = false;
 
 			Horse horseherd;
 			try {
 				int[][] horse_stops = {{12,16},{14,27}};
 				horseherd = new Horse(true, false, "horseHerd", this, m_player, 12*SIZE, 16*SIZE, horse_stops);
 				
 				horseherd.setRenderPriority(9);
 				this.addObject(horseherd, true);
 				m_enemies.add(horseherd);
 				Sound gallop = new Sound("assets/sounds/HorsesRunning.wav");
 				gallop.play();
 				//TODO freeze player for 5 seconds
 				
 				this.displayDialogue(new String[] {"Horses make you tired. Maybe you should go to bed."});
 				
 			} catch (SlickException e) {
 				System.err.println("horse error!");
 			}
 		}
 
 	}
 	@Override
 	public void additionalInit(GameContainer container, StateBasedGame stateManager) throws SlickException {
 		m_playerX = SIZE*11;
 		m_playerY = SIZE*28;
 		
 		if(m_mapPath != null) {
 			m_tiledMap = new TiledMap(m_mapPath);
 		}
 		else
 			try {
 				m_tiledMap = new TiledMap("assets/maps/townDay.tmx");
 			} catch (SlickException e) {
 				System.out.println("ERROR: Could not townDay.tmx");
 			}
 		m_map = new simpleMap();
 		m_blocked = new boolean[m_tiledMap.getWidth()][m_tiledMap.getHeight()];
 		for (int xAxis=0; xAxis<m_tiledMap.getWidth(); xAxis++) {
 			for (int yAxis=0; yAxis<m_tiledMap.getHeight(); yAxis++) {
 				int tileID = m_tiledMap.getTileId(xAxis, yAxis, 0);
 				String value = m_tiledMap.getTileProperty(tileID, "blocked", "false");
 				if ("true".equals(value)) {
 					m_blocked[xAxis][yAxis] = true;
 				}
 			}
 		}
 		
 		if (!this.isLoaded()) {
 			// setup objects
 			//m_interactables = new HashMap<String, Interactable>();
 			//m_objects = new HashMap<String, GameObject>();	
 			
 			//PEOPLE
 			Person person_1 = new Person("person_1", 9*SIZE, 22*SIZE, "assets/characters/human_2.png", null);
 			person_1.setDialogue(new String[] 
 					{"\"Can you help me find my cats? There are 2 of them.\""});
 			this.addObject(person_1, true);
 			m_blocked[9][22] = true;
 			
 			if(m_player.getInventory().contains(Types.HAIR_VIAL)||m_player.getInventory().contains(Types.WRENCH)){
 				Person mustacheMan = new Person("mustacheMan", 16*SIZE, 25*SIZE, "assets/characters/human_3.png", new HairVial(-1,-1));
 				mustacheMan.setDialogue(new String[] 
 						{"\"Hurry up young man! No time to waste.\"",
 						"\"You aren't making any friends with that silly moustache of yours!\""});
 				this.addObject(mustacheMan, true);
 			}else{
 				Person mustacheMan = new Person("mustacheMan", 16*SIZE, 25*SIZE, "assets/characters/human_3.png", new HairVial(-1,-1));
 				mustacheMan.setDialogue(new String[] 
 						{"\"Young man, for what reason have you let your mustache grow?\"",
 						"\"It really looks quite terrible on a face like ours.\"",
 						"\"Here's something I found, maybe you can fix your face with it.\"",
 						"* you've received a some kind of liquid in a vial*"});
 				this.addObject(mustacheMan, true);
 			}
 			m_blocked[16][25] = true;
 			
 			Person buttPlugPerson = new Person("buttPlugPerson", 4*SIZE, 8*SIZE, "assets/characters/human_2.png", new BigPlug());
 			buttPlugPerson.setDialogue(new String[]
 					{"\"!\"",
 					"\"You startled me!\"",
 					"\"Let's just keep this our little secret\"",
 					"\"Take this as payment\"",
 					"* You've received a butt-plug *"
 					});
 			this.addObject(buttPlugPerson, true);
 			m_blocked[4][8] = true;
 			
 			Person dolphinHater = new Person("dolphinHater", 10*SIZE, 18*SIZE,"assets/characters/human_4.png", null);
 			dolphinHater.setDialogue(new String[] {
 					"\"Those dolphins are always up to no good.\"",
 					"\"They turned the old zoo into a horse stable!\"",
 					"\"I wish that someone would shove something up all their holes!\""});
 			this.addObject(dolphinHater, true);
 			m_blocked[10][18] = true;
 			
			Person optimist = new Person("optimist", 13*SIZE, 16*SIZE,"assets/characters/human_4.png", null);
 			optimist.setDialogue(new String[] {
 					"\"You know what I think about this town?\"",
 					"\"I think it's a great place to live.\"",
 					"\"We have the best fire response system in the world!\""});
 			this.addObject(optimist, true);
			m_blocked[13][16] = true;
 			
 			Person shady = new Person("shady", 24*SIZE, 25*SIZE,"assets/characters/woman_1.png", null);
 			shady.setDialogue(new String[] {
 					"\"Have you looked behind the buildings lately?\"",
 					"\"Lots of great stuff you can find there.\"",
 					"\"Look in the trash cans, especially.\""});
 			this.addObject(shady, true);
 			m_blocked[24][25] = true;
 			
 			//TODO: add new people who give information about stuff
 			
 			
 			//DOORS
 			PortalObject portal = new InvisiblePortal("portal", 11*SIZE, 29*SIZE, StateManager.HOME_STATE, 2, 3);
 			this.addObject(portal, true);
 			
 			Door dolphinDoor = new Door("dolphinDoor", 8*SIZE, 13*SIZE, StateManager.DOLPHIN_ENTRANCE, 2, 8);
 			this.addObject(dolphinDoor, true);
 			
 			Door virtualRealityRoomDoor = new Door("virtualRealityRoomDoor", 22*SIZE, 17*SIZE, StateManager.VIRTUAL_REALITY_ROOM_STATE, 3, 4);
 			this.addObject(virtualRealityRoomDoor, true);
 			
 			
 			System.out.println("HOSPITAL DOOR");
 			Door hospitalDoor = new Door("hospitalDoor", 5*SIZE, 21*SIZE, StateManager.HOSPITAL_ENTRANCE_STATE, -1, -1); 
 			hospitalDoor.setRenderPriority(9);
 			this.addObject(hospitalDoor, true);
 			
 			Door shopDoor = new Door("shopDoorDay", 19*SIZE, 14*SIZE, StateManager.SHOP_STATE, -1, -1);
 			shopDoor.setRenderPriority(4);
 			this.addObject(shopDoor, true);
 			
 			//OTHER
 			//TODO put somewhere more reasonable
 			OutsideTrashcan trash = new OutsideTrashcan("trash", 20*SIZE, 12*SIZE);
 			this.addObject(trash, true);
 
 			Banana banana = new Banana("banana",24*SIZE, 23*SIZE);
 			this.addObject(banana,true);
 			
 			//CATS
 			Animal cat1 = new Animal("cat1", "assets/cat1.png", this, m_player,
 					 14*SIZE, 17*SIZE, 23, 12);
 			this.addObject(cat1, true);
 			m_enemies.add(cat1);
 			
 			Animal cat2 = new Animal("cat2", "assets/cat2.png", this, m_player,
 					 4*SIZE, 26*SIZE, 1, 18);
 			this.addObject(cat2, true);
 			m_enemies.add(cat2);
 
 
 		}
 
 		
 	}
 	
 	@Override
 	public void additionalRender(GameContainer container, StateBasedGame stateManager, Graphics g) {
 		if(m_overlay!=null)
 			g.drawImage(m_overlay, 0, 0);
 	}
 
 	@Override
 	public void dialogueListener(Interactable i) {
 
 		
 	}
 
 	@Override
 	public void additionalSetupObjects(int city, int dream) throws SlickException {
 		
 		//LEVEL 1 (NO DEGRADATION) 
 		if(city==4) {
 			
 			if(m_quest1Given == false) {
 				
 				StaticObject fireHydrant = (StaticObject)this.getObject("fireHydrant");
 				fireHydrant.setDialogue(new String[] {"This is a fire hydrant."});
 				
 				Quest fireHydrantQuest = new Quest("fireHydrantQuest");
 				QuestStage goal1 = new QuestStage().addGoal(new QuestGoal.InteractionNameGoal("fireHydrant"));
 				goal1.addGoal(new QuestGoal.ItemEquippedGoal(new Wrench(-1,-1)));
 				goal1.setReward(new QuestReward.WaterDownReward());
 				//goal1.setStartText(new String[] {"You wonder how you can close down the zoo."});
 				goal1.setEndText(new String[] {"You open the fire hydrant with the force of your brawn, and a wrench.",
 						"A great column of water sprays upward like a geyser. There are pretty rainbows."});
 				fireHydrantQuest.addStage(goal1);
 
 				m_player.addQuest(fireHydrantQuest);
 				fireHydrantQuest.startQuest(this);
 				m_quest1Given = true;
 				
 				//TODO: add cat quest
 				Quest catQuest = new Quest("catQuest");
 				QuestStage c1 = new QuestStage().addGoal(new QuestGoal.InteractionNameGoal("person_1"));
 				QuestStage c2 = new QuestStage().setStartText(new String[]
 						{"You want to find some cats!"});
 				ArrayList<Interactable> cats = new ArrayList<Interactable>();
 				cats.add(getInteractable("cat1"));
 				cats.add(getInteractable("cat2"));
 				c2.addGoal(new QuestGoal.MultiInteractGoal(cats));
 				catQuest.addStage(c1);
 				catQuest.addStage(c2);
 				
 			}
 		}
 		
 		//LEVEL 2 (DEGRADED: 1)
 		else if(city==3) {
 			m_overlay = new Image("assets/black_15.png");
 			
 			//PEOPLE DIALOGUE
 			((Person) this.getObject("buttPlugPerson")).setDialogue(new String[] {"\"You didn't tell anyone about yesterday " +
 					"right?" , "I don't want my family to see what I've become!"
 			});
 			((Person) this.getObject("dolphinHater")).setDialogue(new String[] {"\"Can you do something about all" +
 					" the horses? I hate horses.\""});
 			((Person) this.getObject("person_1")).setDialogue(new String[] {"\"My cats...*sob*...*sob*\""});
 			((Person) this.getObject("optimist")).setDialogue(new String[] {"\"What happened to the flowers?\"",
 					"\"They were so beautiful...\"" ,
 					"\"But, at least now, there are beautiful rainbow puddles!\""});
 			if(m_player.getInventory().contains(Types.CIGARETTE)){
 				((Person) this.getObject("mustacheMan")).setItem(null);
 				((Person) this.getObject("mustacheMan")).setDialogue(new String[] {"\"Young man, How's that cigarette " +
 						"treating you?\"",
 						"\"I know the doctors say it's no good for you\"",
 						"\"but, for my money, it's the best thrill a red-blooded man like us can recieve\"",
 						"\"HO HO HO HO HO\""});
 			}
 			((Person) this.getObject("mustacheMan")).setItem(new Cigarette("cigarette", -1, -1));
 			((Person) this.getObject("mustacheMan")).setDialogue(new String[] {"\"Young man, for what reason " +
 					"have you not fixed your face yet?\"",
 					"\"Did you do something else with that potion I gave you?\"",
 					"\"Here, take this cigarette. It might save your life someday.\"",
 					"* You've received a cigarette *"});
 			
 			((Person) this.getObject("shady")).setItem(new ChickenWing("chickenWing", -1, -1));
 			((Person) this.getObject("shady")).setDialogue(new String[] {
 					"\"Hey, guess what?\"",
 					"\"While everyone else was running for their lives, I managed to sneak this chicken wing.\"",
 					"\"Here, take this. I swear it's safe to sell.\"",
 					"* You've received a chicken wing *"});
 			
 			//REMOVE DOOR
 			this.removeObject("dolphinDoor");
 			StaticObject dolphinDoor = new StaticObject("dolphinDoor", 8*SIZE, 13*SIZE, "assets/gameObjects/door.png");
 			dolphinDoor.setDialogue(new String[] {"The door is locked.",
 					"\"Stables closed due to disturbances,\"" +
 					" a sign on the door reads."});
 			this.addObject(dolphinDoor, true);
 			
 			//BREAKING TREES/FLOWERS
 			for (int i=1; i<=5; i++) {
 				StaticObject tree = (StaticObject)this.getObject("tree"+i);
 				tree.setSprite(new Image("assets/gameObjects/treebroken.png"));
 				tree.setRenderPriority(1);
 			}
 			m_blocked[5][25] = true;
 			m_blocked[6][25] = true;
 			m_blocked[8][25] = true;
 			m_blocked[9][25] = true;
 			m_blocked[14][10] = true;
 			m_blocked[15][10] = true;
 			m_blocked[19][6] = true;
 			m_blocked[20][6] = true;
 			m_blocked[11][19] = true;
 			m_blocked[12][19] =true; 
 			int[][] flowerpatches = {{5,9},{5,10},{5,11},{5,12},{5,13},
 					{6,9},{7,9},{8,9},{9,9},{10,9},
 					{11,12}, {11,13},{11,14},{11,15},
 					{12,12}, {12,13},
 					{6,15},{7,15},{8,15},{9,15},{10,15},
 					{1,25},{4,23},{5,23},{5,24},{5,25},{4,24},{4,25},
 					{11,26},{12,26},{13,26},
 					{19,25}};
 
 					
 			int flowerlength = flowerpatches.length;
 			for (int i=0; i<flowerlength; i++){
 				StaticObject flower = (StaticObject)this.getObject("flower"+i);
 				flower.setSprite(new Image("assets/flowersdead.png"));
 				flower.setRenderPriority(0);
 			}
 			
 			//KILLING CATS
 			try {
 				Animal cat1 = (Animal)this.getObject("cat1");
 				Animal cat2 = (Animal)this.getObject("cat2");
 				int[] cat1_loc = cat1.getSquare();
 				int[] cat2_loc = cat2.getSquare();
 			
 				m_enemies.remove(cat1);
 				m_enemies.remove(cat2);
 				this.removeObject("cat1");
 				this.removeObject("cat2");
 				StaticObject static_cat1 = new StaticObject("cat1",  cat1_loc[0]*SIZE, cat1_loc[1]*SIZE,"assets/cat1dead.png");
 				StaticObject static_cat2 = new StaticObject("cat2", cat2_loc[0]*SIZE, cat2_loc[1]*SIZE, "assets/cat2dead.png");
 				static_cat1.setDialogue(new String[] {"A deceased cat. Its body is covered with the imprints of hoofs."});
 				static_cat2.setDialogue(new String[] {"A deceased cat. Its body is covered with the imprints of hoofs."});
 				this.addObject(static_cat1,true);
 				this.addObject(static_cat2,true);
 			
 				//PUTTING IN HORSES
 				int[][] horse_stops1 = {{13,13},{16,12}};
 				int[][] horse_stops2 = {{4,22},{17,25}};
 				int[][] horse_stops3 = {{4,12},{10,10}};
 				int[][] horse_stops4 = {{10,14},{6,10}};
 				int[][] horse_stops5 = {{9,16},{5,16}};
 				int[][] horse_stops6 = {{20,27},{23,23}};
 				Horse horse1 = new Horse(false, false, "horse1", this, m_player, 13*SIZE, 13*SIZE, horse_stops1);
 				horse1.setRenderPriority(5);
 				this.addObject(horse1, true);
 				m_enemies.add(horse1);
 			
 				Horse horse2 = new Horse(false, false, "horse2", this, m_player, 4*SIZE, 22*SIZE, horse_stops2);
 				horse2.setRenderPriority(5);
 				this.addObject(horse2, true);
 				m_enemies.add(horse2);
 			
 				Horse horse3 = new Horse(false, false, "horse3", this, m_player, 4*SIZE, 12*SIZE, horse_stops3);
 				horse3.setRenderPriority(5);
 				this.addObject(horse3, true);
 				m_enemies.add(horse3);	
 				
 				Horse horse4 = new Horse(false, false, "horse4", this, m_player, 10*SIZE, 14*SIZE, horse_stops4);
 				horse4.setRenderPriority(5);
 				this.addObject(horse4, true);
 				m_enemies.add(horse4); 
 				
 				Horse horse5 = new Horse(false, false, "horse5", this, m_player, 9*SIZE, 16*SIZE, horse_stops5);
 				horse5.setRenderPriority(5);
 				this.addObject(horse5, true);
 				m_enemies.add(horse5); 
 			
 				Horse horse6 = new Horse(false, false, "horse6", this, m_player, 20*SIZE, 27*SIZE, horse_stops6);
 				horse5.setRenderPriority(5);
 				this.addObject(horse6, true);
 				m_enemies.add(horse6); 
 				
 			} catch(Exception e) {
 				
 			}
 			
 			
 			
 		}
 		//LEVEL 3 (DEGRADED 2)
 		else if(city==2) {
 			m_overlay = new Image("assets/black_40.png");
 			
 			//PEOPLE
 			((Person) this.getObject("dolphinHater")).setDialogue(new String[] {"\"These horses are so messed up!\"",
 					"I'm beginning to wonder if the dolphins weren't right all along"});
 			((Person) this.getObject("person_1")).setDialogue(new String[] {"\"Yesterday my cats were trampled, and today my brother is in the hospital\"",
 					"There was some sort of crazy Virtual Reality accident", "\"This is the worst weekend ever\""});
 			((Person) this.getObject("optimist")).setDialogue(new String[] {"\"I can't believe they closed the entertainment center\"",
 				"\"I can't even go to my virtual garden anymore\"", "What do should do..."});
 			((Person) this.getObject("buttPlugPerson")).setDialogue(new String[] {"\"What's going on around here lately.\"", "\"Seems as though "+
 				"everyone is wailing about this and that\"", "\"People should be more like me, and find one corner and stick to it\"", "\"Then they'd have" +
 			" nothing to worry about\""});
 
 			//TODO-set new patrol routes for horses
 			/*((Horse) this.getObject("horse1")).setPatrolPoints(new int[][]{{1,1},{2,2}});
 			((Horse) this.getObject("horse2")).setPatrolPoints(new int[][]{{1,1},{2,2}});
 			((Horse) this.getObject("horse3")).setPatrolPoints(new int[][]{{1,1},{2,2}});*/
 			
 		} 
 		//LEVEL 4 (DEGRADED 3 : MAX)
 		else if (city==1) {
 			
 		}
 		
 	}
 
 	@Override
 	public void setupDialogue(GameContainer container, int city, int dream) {
 	}
 	public void setFree(){
 		m_horsesFreed = true;
 	}
 }
