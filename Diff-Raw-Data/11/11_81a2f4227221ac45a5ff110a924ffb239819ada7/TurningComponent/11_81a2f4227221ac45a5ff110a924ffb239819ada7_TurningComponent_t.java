 package atl.space.entities;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class TurningComponent extends Component { //don't use this, use restricted version
 	public Vector3f turn;
 	public TurningComponent(){
		id = "turning";
 		turn = new Vector3f(0, 0, 0);
 	}
 	public TurningComponent(Vector3f t){
		id = "turning";
 		turn = t;
 	}
 	public void update(int delta) {
 		FacingComponent fc = (FacingComponent)owner.getComponent("facing");
 		Vector3f.add(fc.facing, turn, fc.facing);
 		fc.restrictLength();
 	}
 
 	public Component getStepped(int delta) {
 		TurningComponent tc = new TurningComponent(turn);
 		tc.update(delta);
 		return tc;
 	}
 
 
 }
