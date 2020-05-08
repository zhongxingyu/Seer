 package vooga.fighter.controller.levels;
 
 
 
 import java.util.List;
 import java.util.ResourceBundle;
 import util.Vector;
 import util.input.*;
 import vooga.fighter.controller.Controller;
 import vooga.fighter.controller.gameinformation.GameInfo;
 import vooga.fighter.controller.interfaces.ControllerDelegate;
 import vooga.fighter.forces.Force;
 import vooga.fighter.forces.ForceFactory;
 import vooga.fighter.model.objects.AttackObject;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.view.Canvas;
 import vooga.fighter.view.FourPlayerMatchGameLayout;
 
 
 /**
  * A 1v1 fighting game instance
  * 
  * @author Jerry Li
  * @author by Jack Matteucci
  * 
  * Great class to reference when subclasing level controller 
  */
 @InputClassTarget
 public class OneVOneController extends LevelController {
     private static final String INPUT_PATHWAY = "config.leveldefault";
     private static final String SCORE_PATHWAY = "config.score";
     private static final String SCORE = "ScoreScreen";
     private String myInputPathway;
     private String myScorePathway;
     private List<Force> myForces;
     private ResourceBundle myResources;
 
     /**
      * Initial constructor
      */
     public OneVOneController () {
         super();
     }   
 
     /**
      * Concrete constructor
      * @param name      name of controller
      * @param frame     frame
      * @param manager   ControllerManager
      * @param gameinfo  GameInfo
      * @param filepath  FilePath
      */
     public OneVOneController(String name, Canvas frame, ControllerDelegate manager, 
     		GameInfo gameinfo, String filepath) {
     	super(name, frame, manager, gameinfo, filepath);
     	myInputPathway = getHardFilePath() + INPUT_PATHWAY;
     	myScorePathway = getHardFilePath() + SCORE_PATHWAY;
     	myResources = ResourceBundle.getBundle(myScorePathway);
     	ForceFactory forcefactory = new ForceFactory(getHardFilePath());
     	myForces = forcefactory.getForces();
     	getMode().setForces(myForces);
     	frame.setLayout(new FourPlayerMatchGameLayout());
     	setSumOfForces(myForces);
     }
 
     /**
      * Set sum of forces acting upon objects
      * @param forces
      */
     public void setSumOfForces(List<Force> forces) {
         Vector sum = new Vector();
         for (Force force : forces) {
             sum.sum(force.getVector());
         }
         for(CharacterObject character : getInputObjects()) {
             character.setAppliedForces(sum);
         }
     }
 
     /**
      * Return concrete controller
      */
     @Override
 	public Controller getController(String name, Canvas frame, ControllerDelegate manager, GameInfo gameinfo,
                                     String filepath) {
         Controller controller = new OneVOneController(name, frame, manager, gameinfo, filepath);
         return controller;
     }
 
 
     /**
      * notify delegate of condition
      */
     @Override
 	public void notifyEndCondition (String endCondition) {
     	removeListener();
     	getManager().notifyEndCondition(myResources.getString(SCORE));
     }
 
 
     /**
      * Details movement inputs
      * @param alObj
      */
     @InputMethodTarget(name = "player1_jump")
     public void playerOneJumpInput (AlertObject alObj)  {
         getInputObjects().get(0).jump();
     }
 
     @InputMethodTarget(name = "player1_left")
     public void playerOneLeftInput (AlertObject alObj) {
         getInputObjects().get(0).move(180);
 
     }
 
     @InputMethodTarget(name = "player1_right")
     public void playerOneRightInput(AlertObject alObj) {
         getInputObjects().get(0).move(0);
 
     }
 
     @InputMethodTarget(name = "player2_jump")
     public void playerTwoJumpInput (AlertObject alObj)  {
        getInputObjects().get(1).jump();
     }
 
     @InputMethodTarget(name = "player2_left")
     public void playerTwoLeftInput (AlertObject alObj) {
         getInputObjects().get(1).move(180);
 
     }
 
     @InputMethodTarget(name = "player2_right")
     public void playerTwoRightInput(AlertObject alObj) {
         getInputObjects().get(1).move(0);
 
     }
 
 
     @InputMethodTarget(name = "player1_attack")
     public void playerOneAttackInput(AlertObject alObj) {
         AttackObject newAttack = getInputObjects().get(0).attack("weakPunch");
         getMode().addObject(newAttack);
     }
 
     @InputMethodTarget(name = "player2_attack")
     public void playerTwoAttacknput(AlertObject alObj) {
    	AttackObject newAttack = getInputObjects().get(1).attack("weakPunch");
        //getInputObjects().get(1).attack("weakPunch");
    	getMode().addObject(newAttack);
     }
 
     @InputMethodTarget(name = "continue")
     public void mouseclick(PositionObject pos)  {
         //This is a test
         getInputObjects().get(1).changeHealth(-10);
     }
 
     /**
      * Removes listener
      */
     @Override
 	public void removeListener(){
         super.removeListener();
         getInput().removeListener(this);
     }
 
 
 }
