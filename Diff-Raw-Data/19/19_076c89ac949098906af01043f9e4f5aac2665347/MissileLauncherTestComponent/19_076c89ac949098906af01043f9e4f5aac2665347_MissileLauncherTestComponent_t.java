 package atl.space.components.emission;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import atl.space.components.Component;
 import atl.space.entities.Entity;
import atl.space.entities.EntityBuilder;
 
 public class MissileLauncherTestComponent extends FTLauncherComponent {
 	
	
 	public MissileLauncherTestComponent(Vector3f dir,
 			float speed) {
 		super(dir, speed);
 	}
	public MissileLauncherTestComponent(MissileLauncherTestComponent mltc){
		super(mltc);
	}
 
 	@Override
 	protected Entity buildEmission() {
		return buildMissile(.01f, 0f, .01f);
	}
	
	private Entity buildMissile(float maxAccelF, float maxAccelB, float maxturn){
		return EntityBuilder.missile(maxAccelF, maxAccelB, maxturn);
 	}
 
 	@Override
 	protected boolean canEmit() {
 		return true;
 	}
 
 	@Override
 	public Component clone() {
		return new MissileLauncherTestComponent(this);
 	}
 	
 }
