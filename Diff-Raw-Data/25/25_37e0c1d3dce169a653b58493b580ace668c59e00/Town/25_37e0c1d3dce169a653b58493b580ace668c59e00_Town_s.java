 package game.gameplayStates;
 
 import game.StaticObject;
 import game.interactables.Interactable;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public abstract class Town extends GamePlayState {
 
 	public Town() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void setupObjects(int city, int dream) throws SlickException {
 		// Generate gameobjects that will always appear in both TownNight and TownDay here
 		StaticObject homeBuilding = new StaticObject("home_building", 
 				10*SIZE, 29*SIZE, "assets/town/home_building.png");
 		this.addObject(homeBuilding, false);
 		m_blocked[10][29] = true;
 		m_blocked[11][29] = true;
 		m_blocked[12][29] = true;
 		
 		// buildings
 		StaticObject building01 = new StaticObject("building01",
 			14*SIZE, 18*SIZE, "assets/town/building01.png");	
 		building01.setRenderPriority(2);
 		this.addObject(building01, false);
 		
 		StaticObject building01TopA = new StaticObject("buildingWalkThrough01A",
 			14*SIZE, 18*SIZE, "assets/town/building_walkthrough01A.png");
 		building01TopA.setRenderPriority(8);
 		this.addObject(building01TopA, false);
 		
 		StaticObject building01TopB = new StaticObject("buildingWalkThrough01B",
 				17*SIZE, 21*SIZE, "assets/town/building_walkthrough01B.png");
 		building01TopB.setRenderPriority(8);
 		this.addObject(building01TopB, false);
 		
 		StaticObject building02 = new StaticObject("building02",
 			18*SIZE, 17*SIZE, "assets/town/building02.png");
 		building02.setRenderPriority(2);
 		this.addObject(building02, false);
 		
 		StaticObject building02Top = new StaticObject("building02Top" ,
 				18*SIZE, 17*SIZE, "assets/town/building02Top.png");
 		building02Top.setRenderPriority(8);
 		this.addObject(building02Top, false);
 		
 		StaticObject building03 = new StaticObject("building03",
 				14*SIZE, 13*SIZE, "assets/town/building03.png");
 		building03.setRenderPriority(2);
 		this.addObject(building03, false);
 		
 		StaticObject building03Top = new StaticObject("building03Top",
 				14*SIZE+2, 13*SIZE, "assets/town/building03Top.png");
 		building03Top.setRenderPriority(8);
 		this.addObject(building03Top, false);
 		
 		StaticObject building04 = new StaticObject("building04" ,
 				21*SIZE, 15*SIZE, "assets/town/building04.png");
 		building04.setRenderPriority(2);
 		this.addObject(building04, false);
 		
 		StaticObject building04Top = new StaticObject("building04Top",
 				21*SIZE, 15*SIZE, "assets/town/building04Top.png");
 		building04Top.setRenderPriority(8);
 		this.addObject(building04Top, false);
 		
 		StaticObject building05 = new StaticObject("building05",
 				21*SIZE, 12*SIZE, "assets/town/building05.png");
 		building05.setRenderPriority(2);
 		this.addObject(building05, false);
 		
 		StaticObject building06 = new StaticObject("building06",
 				17*SIZE, 12*SIZE, "assets/town/building06.png");
 		building06.setRenderPriority(2);
 		this.addObject(building06, false);
 		
 		StaticObject building06Top = new StaticObject("building06Top",
 				17*SIZE+3, 12*SIZE, "assets/town/building06Top.png");
 		building06Top.setRenderPriority(8);
 		this.addObject(building06Top, false);
 		
 		StaticObject building07 = new StaticObject("building07", 
 				20*SIZE, 19*SIZE, "assets/town/building07.png");
 		building07.setRenderPriority(2);
 		this.addObject(building07, false);
 				
 		StaticObject building07Top = new StaticObject("building07Top", 
 				20*SIZE, 19*SIZE, "assets/town/building07Top.png");
 		building07.setRenderPriority(8);
 		this.addObject(building07Top, false);
 		
 		StaticObject building08 = new StaticObject("building08",
 				23*SIZE, 17*SIZE, "assets/town/building08.png");
 		building08.setRenderPriority(8);
 		this.addObject(building08, false);
 		
 		StaticObject building09 = new StaticObject("building09",
 				12*SIZE, 5*SIZE, "assets/town/building09.png");
 		building09.setRenderPriority(9);
 		this.addObject(building09, false);
 		
		
		
 		StaticObject doormat = new StaticObject("doormat",11*SIZE, 28*SIZE, "assets/gameObjects/doormat.png");
 		this.addObject(doormat, false);
 		
 		//hospital stuff
 		StaticObject hospital = new StaticObject("hospital", 4*SIZE, 17*SIZE, "assets/town/hospital.png");
 		hospital.setRenderPriority(3);
 		this.addObject(hospital, false);
 		
 		StaticObject hospitalTop = new StaticObject("hospitalTop", 4*SIZE+4, 17*SIZE, "assets/town/hospitalTop.png");
 		hospitalTop.setRenderPriority(8);
 		this.addObject(hospitalTop, false);
 		
 		
 		StaticObject hospitalTower = new StaticObject("hospitalTower", 2*SIZE, 15*SIZE, 
 				"assets/town/hospitalTower.png");
 		this.addObject(hospitalTower, false);
 		hospitalTower.setRenderPriority(2);
 		StaticObject hospitalTowerTop = new StaticObject("hospitalTowerTop", 2*SIZE+4, 15*SIZE, 
 				"assets/town/hospitalTowerTop.png");
 		hospitalTowerTop.setRenderPriority(8);
 		this.addObject(hospitalTowerTop, false);
 		
 		// zoo
 		StaticObject zoo = new StaticObject("zooBuilding", 7*SIZE, 11*SIZE, "assets/town/zoo.png");
 		this.addObject(zoo, false);
 		zoo.setRenderPriority(2);
 		
 		
 		// random houses
 		StaticObject randomHouse01 = new StaticObject("randomHouse1", 9*SIZE, 5*SIZE, 
 				"assets/town/smallRandomHouse01.png");
 		randomHouse01.setRenderPriority(2);
 		this.addObject(randomHouse01, false);
 		
 		StaticObject randomHouse02 = new StaticObject("randomHouse2", 5*SIZE, 5*SIZE, 
 				"assets/town/smallRandomHouse02.png");
 		randomHouse02.setRenderPriority(4);
 		this.addObject(randomHouse02, false);
 		
 		StaticObject randomHouse03 = new StaticObject("randomHouse3", 11*SIZE, 23*SIZE, 
 				"assets/town/smallRandomHouse03.png");
 		randomHouse03.setRenderPriority(2);
 		this.addObject(randomHouse03, false);
 		m_blocked[11][24] = true;
 		m_blocked[12][24] = true;
 		m_blocked[13][24] = true;
 		m_blocked[11][25] = true;
 		m_blocked[12][25] = true;
 		m_blocked[13][25] = true;
 		
 		StaticObject randomHouse03Top = new StaticObject("randomHouse3Top", 11*SIZE, 23*SIZE,
 				"assets/town/smallRandomHouse03Top.png");
 		randomHouse03Top.setRenderPriority(8);
 		this.addObject(randomHouse03Top, false);
 		
 		
 		StaticObject tree1 = new StaticObject("tree1", 11*SIZE, 18*SIZE, "assets/gameObjects/tree_normal.png");
 		tree1.setRenderPriority(6);
 		this.addObject(tree1, false);
 		
 		m_blocked[11][20] = true;
 		m_blocked[12][20] = true;
 		
 		StaticObject tree2 = new StaticObject("tree2", 19*SIZE, 5*SIZE, "assets/gameObjects/tree_normal.png");
 		tree2.setRenderPriority(6);
 		this.addObject(tree2, false);
 		
 		m_blocked[19][7] = true;
 		m_blocked[20][7] = true;
 		
 		StaticObject tree3 = new StaticObject("tree3", 5*SIZE, 24*SIZE, "assets/gameObjects/tree_normal.png");
 		tree3.setRenderPriority(6);
 		this.addObject(tree3,false);
 		m_blocked[5][26] = true;
 		m_blocked[6][26] = true;
 		
 		StaticObject tree4 = new StaticObject("tree4", 8*SIZE, 24*SIZE, "assets/gameObjects/tree_normal.png");
 		tree4.setRenderPriority(6);
 		this.addObject(tree4, false);
 		m_blocked[8][26] = true;
 		m_blocked[9][26] = true;
 		
 		StaticObject tree5 = new StaticObject("tree5", 14*SIZE, 9*SIZE, "assets/gameObjects/tree_normal.png");
 		tree5.setRenderPriority(6);
 		this.addObject(tree5, false);
 		m_blocked[14][11] = true;
 		m_blocked[15][11] = true;
 		
 		StaticObject sign1 = new StaticObject("sign1", 7*SIZE, 13*SIZE, "assets/gameObjects/sign.png");
 		sign1.setDialogue(new String[] {"\"Horse Stables (formerly the zoo)\""});
 		sign1.setRenderPriority(7);
 		this.addObject(sign1, true);
 		
 		StaticObject sign2 = new StaticObject("sign2", 4*SIZE, 21*SIZE, "assets/gameObjects/sign.png");
 		sign2.setDialogue(new String[] {"\"Hospital\""});
 		sign2.setRenderPriority(7);
 		this.addObject(sign2, true);
 		
 		StaticObject fireHydrant = new StaticObject("fireHydrant", 6*SIZE, 23*SIZE, "assets/firehydrant.png");
 		this.addObject(fireHydrant, true);
 		m_blocked[6][23] = true;
 		
 		int[][] flowerpatches = {{5,9},{5,10},{5,11},{5,12},{5,13},
 				{6,9},{7,9},{8,9},{9,9},{10,9},
 				{11,12}, {11,13},{11,14},{11,15},
 				{12,12}, {12,13},
 				{6,15},{7,15},{8,15},{9,15},{10,15},
 				{1,25},{4,23},{5,23},{5,24},{5,25},{4,24},{4,25},
 				{11,26},{12,26},{13,26},
 				{19,25},
 				{13,5},{14,5},{15,5},{16,5},{17,5},{21,5},
 				{18,4},{19,4},{20,4},{21,4},
 				{13,6},{19,6},{21,6},
 				{13,7},{15,7},{16,7},{17,7},{18,7},{21,7},
 				{20,8},{21,8}};
 				
 		int flowerlength = flowerpatches.length;
 		for (int i=0; i<flowerlength; i++){
 			int xflowcoord = flowerpatches[i][0];
 			int yflowcoord = flowerpatches[i][1];
 			StaticObject flower = new StaticObject("flower"+i,xflowcoord*SIZE, yflowcoord*SIZE, "assets/flowers.png");
 			flower.setRenderPriority(0);
 			this.addObject(flower, false);
 			m_blocked[xflowcoord][yflowcoord] = true;
 		}
 		
 		
 		if (city == 4) {
 			
 		}
 		else if (city == 3) {
 			int[][] dolphinBlocks = {{10,16}, {10,17}, {11,16}, {12,16}, {12,15}, {12,14}, {13,14}};
 			for (int i=0; i<dolphinBlocks.length; i++) {
 				int xBlock = dolphinBlocks[i][0];
 				int yBlock = dolphinBlocks[i][1];
 				StaticObject block = new StaticObject("dolphinBlock"+i, xBlock*SIZE, yBlock*SIZE, "assets/block.png");
 				block.setRenderPriority(4);
 				block.setDialogue(new String[] {"You can't get pass this. The zoo has been blocked by due to the destruction of the dolphin."});
 				this.addObject(block, true);
 				m_blocked[xBlock][yBlock] = true;
 			}
 		}
 		else if (city == 2) {
 			int[][] dolphinBlocks = {{10,16}, {10,17}, {11,16}, {12,16}, {12,15}, {12,14}, {13,14}};
 			for (int i=0; i<dolphinBlocks.length; i++) {
 				int xBlock = dolphinBlocks[i][0];
 				int yBlock = dolphinBlocks[i][1];
 				StaticObject block = new StaticObject("dolphinBlock"+i, xBlock*SIZE, yBlock*SIZE, "assets/block.png");
 				block.setRenderPriority(4);
 				block.setDialogue(new String[] {"You can't get pass this. The zoo has been blocked by due to the destruction of the dolphin."});
 				this.addObject(block, true);
 				m_blocked[xBlock][yBlock] = true;
 			}
 			
 			int[][] virtualRealityBlocks = {{13,16}, {13,17}, {13,18}, {13,19}, {17,25}, {17, 26}, {17, 27}};
 			for (int i=0; i<virtualRealityBlocks.length; i++) {
 				int xBlock = virtualRealityBlocks[i][0];
 				int yBlock = virtualRealityBlocks[i][1];
 				StaticObject block = new StaticObject("virtualRealityBlock"+i, xBlock*SIZE, yBlock*SIZE, "assets/block.png");
 				block.setRenderPriority(4);
 				block.setDialogue(new String[] {"You can't get pass this. The virtual reality centre went out of business and pull down all its surrounding businesses."});
 				this.addObject(block, true);
 				m_blocked[xBlock][yBlock] = true;
 			}	
 		}
 		else if (city == 1) {
 			int[][] dolphinBlocks = {{10,16}, {10,17}, {11,16}, {12,16}, {12,15}, {12,14}, {13,14}};
 			for (int i=0; i<dolphinBlocks.length; i++) {
 				int xBlock = dolphinBlocks[i][0];
 				int yBlock = dolphinBlocks[i][1];
 				StaticObject block = new StaticObject("dolphinBlock"+i, xBlock*SIZE, yBlock*SIZE, "assets/block.png");
 				block.setRenderPriority(4);
 				block.setDialogue(new String[] {"You can't get pass this. The zoo has been blocked to try to contain the rampant horses.",
 						" however a few have escaped."});
 				this.addObject(block, true);
 				m_blocked[xBlock][yBlock] = true;
 			}
 			
 			int[][] virtualRealityBlocks = {{13,16}, {13,17}, {13,18}, {13,19}, {17,25}, {17, 26}, {17, 27}};
 			for (int i=0; i<virtualRealityBlocks.length; i++) {
 				int xBlock = virtualRealityBlocks[i][0];
 				int yBlock = virtualRealityBlocks[i][1];
 				StaticObject block = new StaticObject("virtualRealityBlock"+i, xBlock*SIZE, yBlock*SIZE, "assets/block.png");
 				block.setRenderPriority(4);
 				block.setDialogue(new String[] {"You can't get pass this. The virtual reality centre went out of business and pull down all its surrounding businesses."});
 				this.addObject(block, true);
 				m_blocked[xBlock][yBlock] = true;
 			}		
 			
			int[][] hospitalBlocks = {{9,22}, {9,23}, {8,23}, {7,23}, {7,24}, {7,25}, {7,26}, {7,27}, {7,28}};
 			for (int i=0; i<hospitalBlocks.length; i++) {
 				int xBlock = hospitalBlocks[i][0];
 				int yBlock = hospitalBlocks[i][1];
 				StaticObject block = new StaticObject("hospitalBlock"+i, xBlock*SIZE, yBlock*SIZE, "assets/block.png");
 				block.setRenderPriority(4);
 				block.setDialogue(new String[] {"You can't get pass this. The hospital was shut down and had to let all their untreated patients out... the area is higly contaminated with all kinds of diseases."});
 				this.addObject(block, true);
 				m_blocked[xBlock][yBlock] = true;
 			}	
 			
 		}
 		else if (city == 0) {
 			
 		}
 		
 		if (city < 4) {
 			for (int i=1; i<=4; i++) {
 				StaticObject tree = (StaticObject)this.getObject("tree"+i);
 				tree.setSprite(new Image("assets/gameObjects/treebroken.png"));
 				tree.setRenderPriority(1);
 			}
 
 			
 			this.getObject("fireHydrant").setSprite(new Image("assets/gameObjects/firehydrantbroken.png"));
 			
 			m_blocked[19][6] = false;
 			m_blocked[20][6] = false;
 			m_blocked[11][19] = false;
 			m_blocked[12][19] = false;
 			m_blocked[5][25] = false;
 			m_blocked[6][25] = false;
 			m_blocked[8][25] = false;
 			m_blocked[9][25] = false;
 			m_blocked[14][10] = false;
 			m_blocked[15][10] = false;
 
 			for (int i=0; i<flowerlength; i++){
 				int xflowcoord = flowerpatches[i][0];
 				int yflowcoord = flowerpatches[i][1];
 				this.getObject("flower"+i).setSprite(new Image("assets/flowersdead.png"));
 				this.getObject("flower"+i).setRenderPriority(0);
 			}
 		}
 		
 		// draw all door images (not necessarily functional)
 		StaticObject hospitalDoor = new StaticObject("hospitalDoor", 5*SIZE, 21*SIZE,  "assets/gameObjects/door.png");
 		hospitalDoor.setRenderPriority(4);
 		this.addObject(hospitalDoor, false);
 		hospitalDoor.setRenderPriority(4);
 		StaticObject dolphinDoor = new StaticObject("dolphinDoor", 8*SIZE, 13*SIZE,  "assets/gameObjects/door.png"); 
 		this.addObject(dolphinDoor, false);
 		dolphinDoor.setRenderPriority(4);
 		StaticObject virtualRealityDoor = new StaticObject("virtualRealityDoor", 22*SIZE, 17*SIZE,  "assets/gameObjects/door.png"); 
 		this.addObject(virtualRealityDoor, false);
 		virtualRealityDoor.setRenderPriority(4);
 		StaticObject shopDoor = new StaticObject("shopDoor", 19*SIZE, 14*SIZE,  "assets/gameObjects/door.png"); 
 		this.addObject(shopDoor, false);
 		shopDoor.setRenderPriority(4);
 
 		
 		// townDay and townNight will call this
 		additionalSetupObjects(city, dream);
 	}
 	
 	public abstract void additionalSetupObjects(int city, int dream) throws SlickException;
 
 	@Override
 	public void setupDialogue(GameContainer container, int city, int dream)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	@Deprecated
 	public void dialogueListener(Interactable i) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
