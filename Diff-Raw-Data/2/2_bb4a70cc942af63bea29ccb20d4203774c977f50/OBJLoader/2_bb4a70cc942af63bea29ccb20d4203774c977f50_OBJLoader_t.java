 package render.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.lwjgl.util.vector.Vector4f;
 
 import render.Face;
 import render.Material;
 import render.Model;
 
 public class OBJLoader
 {
 	/**
 	 * Loads a .obj file and creates a Model object from it
 	 * @param path The path to the file relative to the .jar's directory 
 	 * @param file The name of the .obj file
 	 * @return an instance of Model
 	 * @throws IOException
 	 */
 	public static Model loadModel(String path, String file) throws IOException
 	{
 		File OBJFile = new File(path + file);
 		BufferedReader reader = new BufferedReader(new FileReader(OBJFile));
 		Model m = new Model();
 		String line;
 		
 		ArrayList<Face> faces = new ArrayList<Face>();
 
 		while ((line = reader.readLine()) != null)
 		{
 			if(line.startsWith("mtllib"))
 			{
 				loadMaterialsFile(path, line.split(" ")[1], m);
 			}
 			else if (line.startsWith("v "))
 			{
 				float x = Float.valueOf(line.split(" ")[1]);
 				float y = Float.valueOf(line.split(" ")[2]);
 				float z = Float.valueOf(line.split(" ")[3]);
 
 				m.vertices.add(new Vector3f(x, y, z));
 			}
 			else if (line.startsWith("vn "))
 			{
 				float x = Float.valueOf(line.split(" ")[1]);
 				float y = Float.valueOf(line.split(" ")[2]);
 				float z = Float.valueOf(line.split(" ")[3]);
 
 				m.normals.add(new Vector3f(x, y, z));
 			}
 			else if (line.startsWith("vt "))
 			{
 				float x = Float.valueOf(line.split(" ")[1]);
 				float y = Float.valueOf(line.split(" ")[2]);
 
 				m.tetxures.add(new Vector2f(x, y));
 			}
 			else if (line.startsWith("f "))
 			{
 				Vector3f[] indices = new Vector3f[line.split(" ").length - 1]; // <-- Das ist keine List deshalb funktioniert add() nicht
 				// TODO: entweder aus indices eine List machen oder das Array mit einer for-Schleife durchlaufen
 				
 				for(int i = 0; i < line.split(" ").length - 1; i++)
 				{
 					String s = line.split(" ")[i + 1];
 					float v = Float.valueOf(s.split("/")[0]);
 					float t = Float.valueOf(s.split("/")[1]);
 					float n = Float.valueOf(s.split("/")[2]);
 					
					indices.add(new Vector3f(v, t, n);
 				}
 				faces.add(new Face(indices));
 			}
 			else if (line.startsWith("usemtl"))
 			{
 				m.faces.add(faces);
 				m.faceMaterials.add(line.split(" ")[1]);
 			}
 		}
 		reader.close();
 		return m;
 	}
 
 	private static void loadMaterialsFile(String path, String file, Model model) throws IOException
 	{
 		model.usesMaterials = true;
 		
 		File materialsFile = new File(path + file);
 		BufferedReader reader = new BufferedReader(new FileReader(materialsFile));
 		
 		Material m = null;		
 		String line;
 		
 		while ((line = reader.readLine()) != null)
 		{
 			if(line.startsWith("newmtl "))
 			{
 				m = new Material();
 				model.materials.put(line.split(" ")[1], m);
 				model.faceMaterials.add(line.split(" ")[1]);
 			}
 			else if(line.startsWith("Ka "))
 			{
 				float red = Float.valueOf(line.split(" ")[1]);
 				float green = Float.valueOf(line.split(" ")[2]);
 				float blue = Float.valueOf(line.split(" ")[3]);
 				float alpha = 1;
 				if(line.split(" ").length > 4) alpha = Float.valueOf(line.split(" ")[4]);
 				m.ambientColor = new Vector4f(red, green, blue, alpha);
 			}
 			else if(line.startsWith("Kd "))
 			{
 				float red = Float.valueOf(line.split(" ")[1]);
 				float green = Float.valueOf(line.split(" ")[2]);
 				float blue = Float.valueOf(line.split(" ")[3]);
 				float alpha = 1;
 				if(line.split(" ").length > 4) alpha = Float.valueOf(line.split(" ")[4]);
 				m.diffuseColor = new Vector4f(red, green, blue, alpha);
 			}
 			else if(line.startsWith("Ks "))
 			{
 				float red = Float.valueOf(line.split(" ")[1]);
 				float green = Float.valueOf(line.split(" ")[2]);
 				float blue = Float.valueOf(line.split(" ")[3]);
 				float alpha = 1;
 				if(line.split(" ").length > 4) alpha = Float.valueOf(line.split(" ")[4]);
 				m.specularColor = new Vector4f(red, green, blue, alpha);
 				
 			}
 			else if(line.startsWith("Ns "))
 			{
 				m.shininess = Float.valueOf(line.split(" ")[1]);
 			}
 			else if(line.startsWith("d ") || line.startsWith("Tr "))
 			{
 				m.transperency = Float.valueOf(line.split(" ")[1]);
 			}
 			else if(line.startsWith("map_Kd "))
 			{
 				m.hasTexture = true;
 				m.texturePath = path + line.split(" ")[1];
 			}
 		}
 		reader.close();
 	}
 }
