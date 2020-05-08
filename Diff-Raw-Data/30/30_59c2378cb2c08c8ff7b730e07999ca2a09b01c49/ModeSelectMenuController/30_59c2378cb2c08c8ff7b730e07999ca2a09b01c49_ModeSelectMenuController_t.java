 package vooga.fighter.controller;
 
 import util.Location;
 import util.input.AlertObject;
 import util.input.Input;
 import util.input.InputClassTarget;
 import util.input.InputMethodTarget;
 import util.input.PositionObject;
 import vooga.fighter.controller.Controller;
 import vooga.fighter.controller.ControllerDelegate;
 import vooga.fighter.controller.GameInfo;
 import vooga.fighter.controller.OneVOneController;
 import vooga.fighter.model.*;
 import vooga.fighter.model.objects.MouseClickObject;
 import vooga.fighter.util.Paintable;
 import vooga.fighter.view.Canvas;
 
 import java.awt.Dimension;
 import java.util.List;
 import java.util.ResourceBundle;
 
 
 /**
  * 
  * @author Jerry Li & Jack Matteucci
  * 
  */
 
 @InputClassTarget
 public class ModeSelectMenuController extends MenuController {
 	
     private ResourceBundle myResources;
     
     public ModeSelectMenuController () {
         super();
       // myResources = 
     }
         
     public void initializeRest(Canvas frame, ControllerDelegate manager, 
                 GameInfo gameinfo) {
         super.initializeRest(frame, manager, gameinfo);
     }
     /**
      * Checks this controller's end conditions
      */
     
     public void notifyEndCondition(String choice) {
     	removeListener();
     	if(EXIT.equals(choice)){
     		getManager().exit();
     	}
     	else if(BACK.equals(choice)){
     		getManager().notifyEndCondition(BACK);
     	}
     	else { //if(getMode().getMenuNames().contains(choice)){
     		getGameInfo().setGameMode(choice);
    		getGameInfo().setNumCharacters(2);//Integer.parseInt(myResources.getString(choice)));
     		getManager().notifyEndCondition(NEXT);
     		}
     	}
     
     
     @InputMethodTarget(name = "continue")
     public void mouseclick(PositionObject pos)  {
         super.getMode().addObject(new MouseClickObject(pos.getPoint2D()));
     }
     
     public void removeListener(){
     	super.removeListener();
     	getInput().removeListener(this);
     }
     
     public void checkConditions(){
     	String choice = getMode().getChoice();
    	if(!choice.equals("")) notifyEndCondition("1v1");
     	for(String other: getMode().getMenuNames()){
         	if(other.equals(choice)){
         		notifyEndCondition(choice);
         	}
     	}
     }
 
 }
