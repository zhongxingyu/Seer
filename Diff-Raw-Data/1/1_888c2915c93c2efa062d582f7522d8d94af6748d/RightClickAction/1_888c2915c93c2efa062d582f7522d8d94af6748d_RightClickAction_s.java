 package vooga.rts.manager.actions;
 
 import vooga.rts.action.ManagerAction;
 import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.manager.Manager;
 import vooga.rts.util.Camera;
 import vooga.rts.util.Location3D;
 
 /**
  * This class needs to be pushed into any classes that can be moved, and be mapped 
  * to the right click there.
  * 
  * @author Challen Herzberg-Brovold.
  *
  */
 public class RightClickAction extends ManagerAction {
 
     private Location3D myLocation;
 
     public RightClickAction (Manager manager) {
         super(manager);
     }
 
     @Override
     public void apply () {
         if (myLocation != null) {
             for (InteractiveEntity ie : getManager().getSelected()) {
                 ie.move(myLocation); 
             }
         }
     }
 
     @Override
     public void update (Command command) {
         ClickCommand click = (ClickCommand) command;
         myLocation = Camera.instance().viewtoWorld(click.getPosition());
        // TODO : Check outside of bounds of map
         if (myLocation.getX() < 0 || myLocation.getY() < 0) {
             myLocation = null;
         }
         apply();
     }
 
 }
