 package DD.GUI;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import DD.MapTool.*;
 import DD.SlickTools.Component;
 import DD.SlickTools.RenderComponent;
 
 import org.newdawn.slick.*;
 
 public class MapToolState extends BasicGameState {
 	
 	private int stateID = 0;
 	private MapTool maptool = null;
 	private String mousePos;
 	private int x1, x2, y1, y2;
 	private boolean clicked = true;
	static Input mouse = new Input(650);
 	int posX;
 	int posY;
 	
 	private Image makeSelection = null;
 	private Image removeSelection = null;
 	
 	public MapToolState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		//don't show fps in top left corner
 		gc.setShowFPS(false);
 		
 		//Initialize Images
 		makeSelection = new Image("Images/MapTool/MakeSelection.png");
 		removeSelection = new Image("Images/MapTool/RemoveSelection.png");
 		maptool = new MapTool();
 		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
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
 		
     	makeSelection.draw(660, 0);
     	removeSelection.draw(830, 0);
     	
     	g.drawString("BACK", 1130, 615);
     	g.drawString(mousePos, 900, 0);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
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
 		
     	posX = mouse.getMouseX();
     	posY = mouse.getMouseY();
 		mousePos = "Mouse position: " + posX + " " + posY;
 		
 		//Back button
     	if((posX > 1130 && posX < 1170) && (posY > 615 && posY < 630))
 		{
 			if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 			{
				//go back to main menu
 				sb.enterState(0);
 			}
 		}

     	//Clicking on map
    	//if((posX > 0 && posX < 648) && (posY > 40 && posY < 670)) {
    	if((posX > 40 && posX < 1200) && (posY > 40 && posY < 670)) {
     		//you are inside map area
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
	    		if(mouse.isMouseButtonDown(0)) {
	    			getMapCoord();
	    			//System.out.println("click");
	    		}
	    		//System.out.println("Over map");
     		}
     	}
     	
     	//Make Selection Button
     	if((posX > 660 && posX < 660 + makeSelection.getWidth()) && (posY > 0 && posY < makeSelection.getHeight())) {
     		
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().massAddSelectedList(x1, y1, x2, y2);
     			System.out.println("selection button");
     		}
     	}
     	
     	//Remove selection Button
     	if((posX > 830 && posX < 830 + removeSelection.getWidth()) && (posY > 0 && posY < removeSelection.getHeight())) {
     		
     		//if you click on the button
     		if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON)) {
     			maptool.getSelectedList().massRemoveSelectedList(x1, y1, x2, y2);
     			System.out.println("remove button");
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
