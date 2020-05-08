 package se2.e.engine3d.j3d;
 
 import java.io.FileNotFoundException;
 
 import javax.media.j3d.AmbientLight;
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Texture;
 import javax.media.j3d.TextureAttributes;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 
 import com.sun.j3d.loaders.IncorrectFormatException;
 import com.sun.j3d.loaders.ParsingErrorException;
 import com.sun.j3d.loaders.Scene;
 import com.sun.j3d.loaders.objectfile.ObjectFile;
 import com.sun.j3d.utils.geometry.ColorCube;
 import com.sun.j3d.utils.geometry.Sphere;
 import com.sun.j3d.utils.image.TextureLoader;
 
 import appearance.AppearanceInfo;
 import appearance.Model3D;
 import appearance.Object3D;
 import appearance.Shape;
 import appearance.Shape3D;
 
 import se2.e.engine3d.GeometryAndAppearanceLoader;
 
 /**
  * A factory for creating DynamicBranch objects. It is the responsibility of this factory to create the Java3D nodes and
  * connect the {@link TransformGroup} to the {@link BranchGroup}, before putting them in the {@link DynamicBranch}.
  * 
  * @author marius, cosmin (just initial structure)
  */
 public class DynamicBranchFactory {
 
 	/** The loader. */
 	private GeometryAndAppearanceLoader loader;
 	private J3DEngine engine;
 
 	/**
 	 * Instantiates a new dynamic branch factory.
 	 *
 	 * @param loader the loader
 	 * @param engine the engine
 	 */
 	public DynamicBranchFactory(GeometryAndAppearanceLoader loader, J3DEngine engine) {
 		super();
 		this.loader = loader;
 		this.engine = engine;
 	}
 
 	/**
 	 * Gets the token branch for a specific label (e.g. 'red_train').
 	 * 
 	 * @param label the label
 	 * @return the token branch
 	 */
 	public DynamicBranch getTokenBranch(String label) {
 		AppearanceInfo appearanceInfo = this.loader.getAppearanceInfo(label);
 		BranchGroup branchGroup = new BranchGroup();
 		TransformGroup tg = null;
 		
 		//String apinfo = appearanceInfo.getLabel();
 		//switch - case with strings only in JRE 7. for compatibility issues, I'm using if - else
 		if (appearanceInfo instanceof Shape3D){
 			Object3D type = ((Shape3D)appearanceInfo).getType();
 			if (type == Object3D.CUBE)
 			{
 				ColorCube model = new ColorCube(0.5f);
 				tg = new TransformGroup();
 				tg.addChild(model);
 				branchGroup.addChild(tg);
 			}
 			else if (type == Object3D.SPHERE)
 			{
 				//load earth texture on sphere
 				Appearance app = new Appearance();
				Texture tex = new TextureLoader("graphics/textures/earth.jpg", engine).getTexture();
 				//FIXME: may need to be changed from / to \ depending on the operating system
 				app.setTexture(tex);
 				TextureAttributes texAttr = new TextureAttributes();
 				texAttr.setTextureMode(TextureAttributes.MODULATE);
 				app.setTextureAttributes(texAttr);
 				Sphere model = new Sphere(0.86f, Sphere.GENERATE_TEXTURE_COORDS, app);
 				tg = new TransformGroup();
 				tg.addChild(model);
 				branchGroup.addChild(tg);
 			}
 		}
 		else if (appearanceInfo instanceof Model3D){
 			String filepath = ((Model3D) appearanceInfo).getFile();
 			
 			Scene s = null;
 	    	ObjectFile f = new ObjectFile ();
 	    	f.setFlags (ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
 	    	
 			try {
 				s = f.load (filepath);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IncorrectFormatException e) {
 				e.printStackTrace();
 			} catch (ParsingErrorException e) {
 				e.printStackTrace();
 			}
 			tg = new TransformGroup();
 			tg.addChild (s.getSceneGroup ());
 			
 			BoundingSphere bounds = new BoundingSphere (new Point3d (0.0, 0.0, 5.0), 200.0);
 	        Color3f ambientColor = new Color3f (1.0f, 1.0f, 1.0f);
 	        AmbientLight ambientLightNode = new AmbientLight (ambientColor);
 	        ambientLightNode.setInfluencingBounds (bounds);
 	        tg.addChild (ambientLightNode);
 	        
 			branchGroup.addChild(tg);
 		}
 		
 
 		
 		
 		
 		
 		DynamicBranch branch = new DynamicBranch(branchGroup, tg);
 		return branch;
 	}
 
 
 
 }
