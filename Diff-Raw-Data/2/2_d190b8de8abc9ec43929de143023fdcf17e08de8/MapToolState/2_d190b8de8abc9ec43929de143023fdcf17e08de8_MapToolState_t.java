 package DD.GUI;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import DD.Character.DDCharacter;
 import DD.Character.CharacterSheet.Monster.Goblin;
 import DD.MapTool.*;
 import DD.SlickTools.Component;
 import DD.SlickTools.RenderComponent;
 
 import org.newdawn.slick.*;
 
 public class MapToolState extends BasicGameState {
 	
 	private int stateID = 0;
 	private MapTool maptool = null;
 	private String mousePos;
 	private int x1, x2, y1, y2;	//coordinates for last two mouse clicks on map
 	private Objects object = null;	//object used to place Objects onto the map
 	private boolean clicked = true;
 	static Input mouse = new Input(650);
 	int posX;
 	int posY;
 	
 	//Images
 	private Image spriteSheet = null;
 	private Image makeSelection = null;
 	private Image removeSelection = null;
 	private Image placeOnMap = null;
 	private Image removeFromMap = null;
 	private Image goblinButton = null;
 	private Image wallButton = null;
 	
 	public MapToolState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		//don't show fps in top left corner
 		gc.setShowFPS(false);
 		
 		//Initialize Images
 		spriteSheet = new Image("Images/Test/DungeonCrawl_ProjectUtumnoTileset.png");
 		makeSelection = new Image("Images/MapTool/MakeSelection.png");
 		removeSelection = new Image("Images/MapTool/RemoveSelection.png");
 		placeOnMap = new Image("Images/MapTool/PlaceOnMap.png");
 		removeFromMap = new Image("Images/MapTool/RemoveFromMap.png");
 		goblinButton = new Image("Images/MapTool/Goblin.png");
 		wallButton = new Image("Images/MapTool/Wall.png");
 		maptool = new MapTool();
 		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
 		//Render Map
     	RenderComponent renderComponent = null;
     	
     	for(int i = 0; i < maptool.getCurrentMap().mapSize; i++) {
     		for(int j = 0; j < maptool.getCurrentMap().mapSize; j++) {
     			Objects[] list = new Objects[maptool.getCurrentMap().objectsStack[i][j].size()];
     			System.arraycopy(maptool.getCurrentMap().objectsStack[i][j].toArray(), 0, list, 0, maptool.getCurrentMap().objectsStack[i][j].size());
     			for(int k = list.length; k > 0; k--) {
     				Component component = (Component)list[k-1];
     				if (RenderComponent.class.isInstance(component))
     				{
     					renderComponent = (RenderComponent) component;
     					renderComponent.render(gc, sb, g);
     				}
     			}
     		}
     	}
 		
     	//draw Make Selection and Remove Selection buttons to screen
     	makeSelection.draw(660, 0);
     	removeSelection.draw(830, 0);
     	placeOnMap.draw(660, 40);
     	removeFromMap.draw(830, 40);
     	g.drawString("Available To Place", 660, 80);
     	goblinButton.draw(660, 100);
     	wallButton.draw(755, 100);
     	
     	g.drawString("BACK", 1130, 615);
     	g.drawString(mousePos, 900, 0);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
 		posX = mouse.getMouseX();
     	posY = mouse.getMouseY();
 		mousePos = "Mouse position: " + posX + " " + posY;
 		
 		clickMap(gc);
 		
 		///Update Map
     	RenderComponent renderComponent = null;
     	for(int i = 0; i < maptool.getCurrentMap().mapSize; i++) {
     		for(int j = 0; j < maptool.getCurrentMap().mapSize; j++) {
     			Objects[] list = new Objects[maptool.getCurrentMap().objectsStack[i][j].size()];
     			System.arraycopy(maptool.getCurrentMap().objectsStack[i][j].toArray(), 0, list, 0, maptool.getCurrentMap().objectsStack[i][j].size());
     			for(int k = 0; k < list.length; k++) {
     				Component component = (Component)list[k];
     				if (RenderComponent.class.isInstance(component))
     				{
     					
     					renderComponent = (RenderComponent) component;
     					renderComponent.update(gc, sb, delta);
     				}
     			}
     		}
     	}
 		
 		//update buttons on screen
 		backButton(gc, sb);
     	makeSelectionButton(gc);
     	removeSelectionButton(gc);
     	placeOnMapButton(gc);
     	removeFromMapButton(gc);
     	goblinButton(gc);
     	wallButton(gc);
 	}
 	
 	//Clicking on map
 	public void clickMap(GameContainer gc) {
 		//NOTE THIS NEEDS TO BE THE FIRST THING IN UPDATE METHOD!!!
    	if((posX > 0 && posX < 620) && (posY > 40 && posY < 640)) {
     		//you are inside map area
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
 	    		getMapCoord();
     		}
     	}
 	
 	}
 	
 	//Make Selection Button
 	public void makeSelectionButton(GameContainer gc) throws SlickException {
     	if((posX > 660 && posX < 660 + makeSelection.getWidth()) && (posY > 0 && posY < makeSelection.getHeight())) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().massAddSelectedList(x1, y1, x2, y2);
     			System.out.println("selection button");
     		}
     	}
 	}
 	
 	//Remove selection Button
 	public void removeSelectionButton(GameContainer gc) throws SlickException {
     	if((posX > 830 && posX < 830 + removeSelection.getWidth()) && (posY > 0 && posY < removeSelection.getHeight())) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().massRemoveSelectedList(x1, y1, x2, y2);
     			System.out.println("remove selection button");
     		}
     	}
 	}
 	
 	//Place on map button
 	public void placeOnMapButton(GameContainer gc) throws SlickException {
 		if((posX > 660 && posX < 660 + placeOnMap.getWidth()) && (posY > 40 && posY < (40 + placeOnMap.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			if(object == null) {
     				
     			}
     			else {
 	    			maptool.getSelectedList().placeSelectedListOnMap(object);
 	    			System.out.println("place on map button");
     			}
     		}
     	}
 	}
 	
 	//Remove from map button
 	public void removeFromMapButton(GameContainer gc) throws SlickException {
 		if((posX > 830 && posX < 830 + removeFromMap.getWidth()) && (posY > 40 && posY < (40 + removeFromMap.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().removeSelectedListOnMap();
     			System.out.println("remove from map button");
     		}
     	}
 	}
 	
 	public void goblinButton(GameContainer gc) throws SlickException {
 		if((posX > 660 && posX < 660 + goblinButton.getWidth()) && (posY > 100 && posY < (100 + goblinButton.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			//Create a new goblin character
     			DDCharacter goblinChar = new DDCharacter(stateID++);
     			goblinChar.setCharacterSheet(new Goblin());
     			goblinChar.setCharacterID(stateID++);
     			CharacterObjects goblinObj = new CharacterObjects("Goblin", goblinChar.getImage(), 0, 0, maptool.getCurrentMap(), goblinChar);
     			object = goblinObj;	//set object to the goblin object to place into the map 
     			System.out.println("chose goblin");
     		}
     	}
 	}
 	
 	public void wallButton(GameContainer gc) throws SlickException {
 		if((posX > 755 && posX < 755 + goblinButton.getWidth()) && (posY > 100 && posY < (100 + goblinButton.getHeight()))) {
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			Wall wall = new Wall("Wall", maptool.getCurrentMap());
     			object = wall;
     			System.out.println("chose wall");
     		}
     	}
 	}
 	
 	//Back Button
 	public void backButton(GameContainer gc, StateBasedGame sb) {
     	if((posX > 1130 && posX < 1170) && (posY > 615 && posY < 630))
 		{
 			if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 			{
 				//go back to main menu
 				sb.enterState(0);
 			}
 		}
 	}
 	
 	public void getMapCoord() {
 		if(clicked) {
 			x1 = (int)(posX / 30.85);
 			y1 = (posY / 30) - 1;
 			System.out.println("x1: " + x1 + " y1: " + y1);
 			clicked = false;
 		}
 		else if(!clicked) {
 			x2 = (int)(posX / 30.85);
 			y2 = (posY / 30) - 1;
 			System.out.println("x2: " + x2 + " y2: " + y2);
 			clicked = true;
 		}
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
