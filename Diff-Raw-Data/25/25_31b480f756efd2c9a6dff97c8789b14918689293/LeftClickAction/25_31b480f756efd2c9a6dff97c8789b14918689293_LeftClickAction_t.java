 package vooga.rts.manager.actions;
 
 import vooga.rts.action.ManagerAction;
 import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.manager.Manager;
 import vooga.rts.util.Camera;
 import vooga.rts.util.Location3D;
 

 /**
  * A left click action, which selects the units at the location.
 * 
  * @author Challen Herzberg-Brovold
  * 
  */
 public class LeftClickAction extends ManagerAction {
 
     private Location3D myLocation;
 
     public LeftClickAction (Manager manager) {
         super(manager);
     }
 
     @Override
     public void apply () {
         getManager().select(myLocation);
     }
 
     @Override
     public void update (Command command) {
         /*
          * if (getManager().getSelected().size() > 0) {
          * getManager().applyAction(command);
          * }
          * else {
          */
         ClickCommand click = (ClickCommand) command;
         myLocation = Camera.instance().viewtoWorld(click.getPosition());
        apply();
     }
 
 }
