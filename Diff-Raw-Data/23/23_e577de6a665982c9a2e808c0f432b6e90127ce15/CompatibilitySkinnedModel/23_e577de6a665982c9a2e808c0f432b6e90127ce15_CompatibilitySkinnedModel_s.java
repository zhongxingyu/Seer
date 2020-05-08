 package graphics.compatibility;
 
 import static org.lwjgl.opengl.GL20.glUniformMatrix4;
 
 import graphics.GLOperations;
 import graphics.Model;
 import graphics.Shader;
 import graphics.compatibility.skeleton.Animation;
 import graphics.compatibility.skeleton.Pose;
 import graphics.compatibility.skeleton.Skeleton;
 
 import java.nio.FloatBuffer;
 import java.util.List;
 
 public class CompatibilitySkinnedModel extends CompatibilityModel{
 
 	
 	private Skeleton skeleton;
 	private Pose pose;
 	private long lastTime;
 	
 	private int inverseBindUniform;
 	private int skeletonUniform;
 
 	protected CompatibilitySkinnedModel(CompatibilityMesh mesh, Shader shader, Skeleton skeleton)
 	{	
 		super(mesh,shader);
 		
 		this.inverseBindUniform = shader.getUniforms().get("jointInvBinds");
 		this.skeletonUniform = shader.getUniforms().get("joints");
 		
 		FloatBuffer skelebuf = GLOperations.generateInverseBindFloatBuffer(skeleton);
 
 		bufferUniformArray(skelebuf, inverseBindUniform);
 		
 		
 		FloatBuffer skelebuf2 = GLOperations.generatePoseFloatBuffer(skeleton);
 		bufferUniformArray(skelebuf2, skeletonUniform);
 		
 		
 		
 		this.skeleton = skeleton;
 		lastTime = System.currentTimeMillis();
 	}
 	
 	//buffers uniform arrays using contiguous graphics memory hack
 	private void bufferUniformArray(FloatBuffer skelebuf, int uniform){
 		for(int i = 0; i < skelebuf.capacity()/16; i++){
 			glUniformMatrix4(uniform + i, false, skelebuf);
 			skelebuf.position(16 * i);
 		}
 	}
 	
 	
 	@Override
 	public void draw(long time) {
 		
 		FloatBuffer skelebuf = GLOperations.generatePoseFloatBuffer(skeleton);
 		bufferUniformArray(skelebuf, skeletonUniform);
 		
 	
 		//TODO: fix animation stuff here
 		List<Animation> animations = skeleton.animations;
//		Animation first = animations.get(0);
//		System.out.println("first: " + first);
 		
 		//TODO: actually draw it
 		super.draw(time);
 		
 	}
 
 	@Override
 	public void addChild(Model model) {
 		// TODO This should add it to a bone in particular! YEAH
 		
 	}
 
 	@Override
 	public void removeChild(Model model) {
 		// TODO Do I need to do something here.
 		
 	}
 
 }
