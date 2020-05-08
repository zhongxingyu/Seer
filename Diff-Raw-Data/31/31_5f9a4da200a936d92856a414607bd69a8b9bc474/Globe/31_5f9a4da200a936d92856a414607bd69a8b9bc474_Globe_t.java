 import java.awt.Color;
 import java.awt.Container;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.Material;
 import javax.media.j3d.Texture;
 import javax.media.j3d.TextureAttributes;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Color3f;
 import javax.vecmath.Color4f;
 import javax.vecmath.Vector3f;
 
 import com.sun.j3d.utils.geometry.Primitive;
 import com.sun.j3d.utils.geometry.Sphere;
 import com.sun.j3d.utils.image.TextureLoader;
 
 public class Globe {
 	Appearance app;
 
 	public void setAppearance() {
 		app = new Appearance();
		
		
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);

		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

		Color3f red = new Color3f(0.7f, .15f, .15f);
 
 		// Set up the texture map
 		TextureLoader loader = new TextureLoader(
				"textures//GlobeTexture_512.png",
 				"LUMINANCE", new Container());
 		Texture texture = loader.getTexture();
 		texture.setBoundaryModeS(Texture.WRAP);
 		texture.setBoundaryModeT(Texture.WRAP);
 		texture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));
 
 		// Set up the texture attributes
 
 		// could be REPLACE, BLEND or DECAL instead of MODULATE
 
 		TextureAttributes texAttr = new TextureAttributes();
 
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		
 		//app.setMaterial(new Material(color, color, color, color, shine));
 
 		app.setTexture(texture);
 
 		app.setTextureAttributes(texAttr);
		
		app.setMaterial(new Material(white, black, white, black, 1.0f));
 	}
 
 	public TransformGroup createGlobe(float x, float y, float z) {
 		int primflags = Primitive.GENERATE_NORMALS
 				+ Primitive.GENERATE_TEXTURE_COORDS;
 		Sphere sphere = new Sphere(0.5f, primflags, app);
 		TransformGroup tg = new TransformGroup();
 		Transform3D transform = new Transform3D();
 		Vector3f pos = new Vector3f(x, y, z);
 		transform.setTranslation(pos);
 		tg.setTransform(transform);
 		tg.addChild(sphere);
 		return tg;
 	}
 
 }
