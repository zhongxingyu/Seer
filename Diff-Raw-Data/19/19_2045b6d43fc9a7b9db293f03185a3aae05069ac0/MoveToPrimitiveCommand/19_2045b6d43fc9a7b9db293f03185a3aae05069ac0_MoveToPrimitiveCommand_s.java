 package org.rubenrr.walkeitor.manager.command.primitive;
 
 import android.util.Log;
 import org.andengine.entity.IEntity;
 import org.rubenrr.walkeitor.element.unit.Unit;
 import org.rubenrr.walkeitor.manager.GameManager;
 import org.rubenrr.walkeitor.util.AStarPathModifier;
 import org.rubenrr.walkeitor.util.TileLocatable;
 
 /**
  * User: Ruben Rubio Rey
  * Date: 2/06/13
  * Time: 5:41 PM
  */
 public class MoveToPrimitiveCommand extends PrimitiveCommandBase {
 
 
     private TileLocatable destination;
 
     public MoveToPrimitiveCommand(Unit unit, TileLocatable destination) {
         this.setUnit(unit);
         this.destination = destination;
 
     }
 
     @Override
     public void execute() {
        Log.e("MoveToPrimitiveCommand", "Executing command to move unit to");
         GameManager.getInstance().moveTo(this.getUnit(), this.destination, this.getPathListeners());
     }
 }
