 package graphics.compatibility;
 
 import game.Game;
 import graphics.Shader;
 import graphics.compatibility.skeleton.Animation;
 import graphics.compatibility.skeleton.Skeleton;
 
 import java.util.HashMap;
 
 import loader.ColladaLoader;
 
 public class CompatibilityModelFactory {
 	
 	private Shader staticShader;
 	private Shader skinnedShader;
 	
 	private Skeleton hackySkeleton;
 	
 	private HashMap<String, CompatibilityMesh> loadedMeshes;
 	
 	public CompatibilityModelFactory(Shader staticShader, Shader skinnedShader)
 	{
 		this.staticShader = staticShader;
 		this.skinnedShader = skinnedShader;
 		this.loadedMeshes = new HashMap<String, CompatibilityMesh>();
 	}
 	
 	
 	public CompatibilityModel getModel(String filename)
 	{
 		if(!loadedMeshes.containsKey(filename))
 		{
 			loadModel(filename);	
 		}
 		
 		CompatibilityMesh m = loadedMeshes.get(filename);
 
		if(m.skinned()){
			System.out.println("testing:useskel");
		}
		
 		return m.skinned()? new CompatibilitySkinnedModel(m, this.skinnedShader, hackySkeleton) : new CompatibilityModel(m, this.staticShader);
 	}
 	
 	public void loadModel(String filename)
 	{
 		if(!loadedMeshes.containsKey(filename))
 		{
 
 			HashMap<String, Object> modelData = ColladaLoader.load(filename);
 			
 			//HACK
 			if(filename.equals("temp/skeletan.dae")){
 				Game.skeleton = (Skeleton)modelData.get("skeleton");
 				Game.animation = (Animation)modelData.get("animation");
 			}
 			
 			
			System.out.println("testing:initskel");
			//only set it if it's true, otherwise we overwrite with null, durr
			if(modelData.containsKey("skeleton")){
				this.hackySkeleton = (Skeleton)modelData.get("skeleton");
			}
 			
 			CompatibilityMesh mesh = null;
 			if(modelData.containsKey("skeleton")){
 				mesh = new CompatibilityMesh(filename, skinnedShader, modelData);
 			}
 			else
 			{
 				mesh = new CompatibilityMesh(filename, staticShader, modelData);
 			}
 			
 			loadedMeshes.put(filename, mesh);
 	
 		}
 	}
 	
 	public void unloadModel(String filename)
 	{
 		loadedMeshes.get(filename).delete();
 		loadedMeshes.remove(filename);
 		
 	}
 	
 	
 	
 }
