<<<<<<< HEAD:Project/src/projectrts/view/controls/MoveControl.java
 package projectrts.view.controls;
=======
package projectrts.controller.controls;
>>>>>>> Fixed warnings in code (mostly unused import):Project/src/projectrts/controller/controls/MoveControl.java
 
 import projectrts.global.utils.Utils;
 import projectrts.model.core.Position;
 import projectrts.model.core.entities.IEntity;
 
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Spatial;
<<<<<<< HEAD:Project/src/projectrts/view/controls/MoveControl.java
=======
import com.jme3.scene.control.AbstractControl;
>>>>>>> Fixed warnings in code (mostly unused import):Project/src/projectrts/controller/controls/MoveControl.java
 import com.jme3.scene.control.Control;
 
 /**
  * A custom Control that handles entity-spatial's movement.
  * @author Markus Ekstrm
  *
  */
 public class MoveControl extends AbstractControl{
 	private IEntity entity;
 	
 	static {
 		ControlFactory.INSTANCE.registerControl("MoveControl", new MoveControl(null));
 	}
 	
 	private MoveControl(IEntity entity) {
 		super();
 		this.entity = entity;
 	}
 	
 	@Override
 	public Control cloneForSpatial(Spatial spatial) {
 		MoveControl control = new MoveControl(entity);
 		control.setSpatial(spatial);
 		return control;
 	}
 
 	@Override
 	protected void controlRender(RenderManager arg0, ViewPort arg1) {
 		
 	}
 
 	/**
 	 * Automatically hooks into the update loop. Should not be manually called!
 	 * @param tpf Time-per-frame
 	 */
 	@Override
 	protected void controlUpdate(float tpf) {
 		if(this.enabled && spatial != null) {
 			Position pos = entity.getPosition(); // Gets the position from it's associated entity. 
 			Vector3f worldPos = Utils.INSTANCE.convertModelToWorld(pos); // Converts it to world position.
 			Vector3f moveVector = worldPos.subtract(spatial.getWorldTranslation()); // Subtracts the current position from the desired to get a movement vector.
 			if(!moveVector.equals(Vector3f.ZERO)) { // If the spatial needs to be moved.
 				spatial.move(moveVector); // Move the spatial according to the movement vector.
 			}		
 		}
 	}
 
 	@Override
 	public AbstractControl createControl(IEntity entity) {
 		return new MoveControl(entity);
 	}
 }
