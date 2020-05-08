 package vooga.fighter.controller;
 
import java.util.List;
 
 import vooga.fighter.input.AlertObject;
import vooga.fighter.input.Input;
 import vooga.fighter.input.InputClassTarget;
 import vooga.fighter.input.InputMethodTarget;
 import vooga.fighter.input.PositionObject;
 import vooga.fighter.model.LevelMode;
 import vooga.fighter.model.MapEditorMode;
 import vooga.fighter.model.Mode;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.MapObject;
 import vooga.fighter.view.Canvas;
 
 @InputClassTarget
 public class MapEditorController extends Controller{
 	
 	    private static final String INPUT_PATHWAY = "vooga.fighter.input.MapEditorMapping_en_US";
 	    private MapEditorMode myEditTarget;
 
 	    public MapEditorController (String name, Canvas frame) {
 	        super(name, frame);
 	    }
 		
 	    public MapEditorController(String name, Canvas frame, ControllerDelegate manager, 
 	    		GameInfo gameinfo) {
 	    	super(name, frame, manager, gameinfo);
 	    	//frame.setViewDataSource(this);
 	    	loadMode();
 	    	GameLoopInfo gameLoopInfo = new GameLoopInfo((LevelMode) super.getMode());
 	    	setGameLoopInfo(gameLoopInfo);
 	    	frame.setViewDataSource(gameLoopInfo);
 	    	
 	    }
 	    
 
 	    
 	    public void loadMode() {
 	        List<Integer> characterNames = myGameInfo.getCharacters();
 	        int mapID = myGameInfo.getMapName();
 	        Mode temp = new LevelMode(this, characterNames, mapID);
 	        setMode(temp);
 	    }
 
 	    public void setEditTarget(MapEditorMode map) {
 	    	myEditTarget = map;
 	    }
 
 	    @Override
 	    public Controller getController (ControllerDelegate delegate, GameInfo gameinfo) {
 	        return new MapEditorController(super.getName(), super.getView(),
 	                                   delegate, gameinfo);
 	    }
 
 	    @Override
 	    protected Input makeInput () {
 	        Input input = new Input(INPUT_PATHWAY, super.getView());
 	        input.addListenerTo(this);
 	    	return input;
 	    }
 	    
 	    @InputMethodTarget(name = "load")
 	    public void loadMap (AlertObject alObj)  {
 	    	
 	    }
 	    
 	    @InputMethodTarget(name = "save")
 	    public void saveMap (AlertObject alObj)  {
 	    	
 	    }
 	    
 	    @InputMethodTarget(name = "select")
 	    public void select (PositionObject posObj)  {
 	    	myEditTarget.select(posObj.getX(), posObj.getY());
 	    }
 	    
 	    @InputMethodTarget(name = "nextObject")
 	    public void nextObject (AlertObject alObj)  {
 	    	myEditTarget.nextObject();
 	    }
 	    
 	    @InputMethodTarget(name = "prevObject")
 	    public void prevObject (AlertObject alObj)  {
 	    	myEditTarget.prevObject();
 	    }
 	    
 	    
 	    
 	    @Override
 	    public void notifyEndCondition () {
 	        System.out.println(" controller notify end is working");
 	        myGameInfo.setMapName(2);
 	        myGameInfo.getCharacters().clear();
 	        myManager.notifyEndCondition("GameOver");
 	        
 	    }
 	    
 	    @Override
 	    public void notifyEndCondition (String endCondition) {
 	    	
 	    }
 	    
 }
 
