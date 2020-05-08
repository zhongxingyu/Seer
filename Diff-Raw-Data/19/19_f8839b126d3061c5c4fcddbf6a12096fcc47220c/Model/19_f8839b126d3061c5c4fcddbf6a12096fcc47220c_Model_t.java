 package com.vloxlands.render.model;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.TextureImpl;
 
 import com.vloxlands.util.RenderAssistant;
 import com.vloxlands.util.math.MathHelper;
 
 public class Model
 {
 	public List<Vector3f> vertices = new ArrayList<Vector3f>();
 	public List<Vector3f> normals = new ArrayList<Vector3f>();
 	public List<Vector2f> tetxures = new ArrayList<Vector2f>();
 	
 	public HashMap<String, Material> materials = new HashMap<>();
 	
 	public boolean hasNormals = false;
 	public boolean hasTextures = false;
 	public boolean usesMaterials = false;
 	
 	int displayListID;
 	boolean wantsRender;
 	
 	/**
 	 * A list of all faces that make up this model, divided into smaller groups for use with materials
 	 */
 	public List<List<Face>> faces = new ArrayList<List<Face>>();
 	/**
 	 * A list of all materials used by this model
 	 */
 	public List<String> faceMaterials = new ArrayList<>();
 	
 	public Model()
 	{
 		displayListID = glGenLists(1);
 		wantsRender = true;
 	}
 	
 	/**
 	 * renders the Model with the textures, color, etc. into a displayList
 	 */
 	public void renderModel()
 	{
 		wantsRender = false;
		glNewList(displayListID, GL_COMPILE_AND_EXECUTE);
 		
 		glEnable(GL_TEXTURE_2D);
 		
 		if (!usesMaterials) TextureImpl.bindNone();
 		for (int i = 0; i < faces.size(); i++)
 		{
 			if (usesMaterials)
 			{
 				Material m = materials.get(faceMaterials.get(i));
 				if (m.hasTexture) RenderAssistant.bindTexture(m.texturePath);
				// else TextureImpl.bindNone();
 				
 				// glColor4f(m.difuseColor.x, m.difuseColor.y, m.difuseColor.z, m.difuseColor.w);
 				
 				glMaterial(GL_FRONT, GL_AMBIENT, MathHelper.asFloatBuffer(new float[] { m.ambientColor.x, m.ambientColor.y, m.ambientColor.z, m.ambientColor.w }));
 				glMaterial(GL_FRONT, GL_DIFFUSE, MathHelper.asFloatBuffer(new float[] { m.diffuseColor.x, m.diffuseColor.y, m.diffuseColor.z, m.diffuseColor.w }));
 				glMaterial(GL_FRONT, GL_SPECULAR, MathHelper.asFloatBuffer(new float[] { m.specularColor.x, m.specularColor.y, m.specularColor.z, m.specularColor.w }));
 				
 				glMaterialf(GL_FRONT, GL_SHININESS, m.shininess);
 			}
 			
 			for (Face face : faces.get(i))
 			{
 				glBegin((face.points.length == 3) ? GL_TRIANGLES : GL_QUADS);
 				for (int j = 0; j < face.points.length; j++)
 				{
 					if (hasNormals)
 					{
 						Vector3f n1 = normals.get((int) face.points[j].z - 1);
 						glNormal3f(n1.x, n1.y, n1.z);
 					}
 					if (hasTextures)
 					{
 						Vector2f t1 = tetxures.get((int) face.points[j].y - 1);
 						glTexCoord2f(t1.x, 1 - t1.y);
 					}
 					Vector3f v1 = vertices.get((int) face.points[j].x - 1);
 					glVertex3f(v1.x, v1.y, v1.z);
 				}
 				
 			}
 			glEnd();
 		}
 		
		glEndList();
 	}
 	
 	public void render()
 	{
		if (wantsRender) renderModel();
 		
		glCallList(displayListID);
 	}
 	
 	/**
 	 * @return Returns a list of all faces used by this model without dividing them into material-groups
 	 */
 	public List<Face> getFaceList()
 	{
 		List<Face> l = new ArrayList<>();
 		for (List<Face> p : faces)
 		{
 			for (Face f : p)
 				l.add(f);
 		}
 		return l;
 	}
 	
 	public Vector3f[] getVerticesAsArray()
 	{
 		Vector3f[] v = new Vector3f[vertices.size()];
 		for (int i = 0; i < vertices.size(); i++)
 		{
 			Vector3f w = vertices.get(i);
 			v[i] = new Vector3f(w.x, w.y, w.z);
 		}
 		return v;
 	}
 }
