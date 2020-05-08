 package game.modelloader;
 
 import initial3d.engine.Mesh;
 import initial3d.engine.MeshLOD;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 
 import java.util.List;
 import java.util.StringTokenizer;
 
 /***
  * Reads .obj wavefront files - derived from http://code.google.com/p/android-gl/source/browse/ModelLoader/src/edu/union/graphics/ObjLoader.java?r=18
  * @author Ben
  *
  */
 public class WavefrontLoader extends AbstractContentLoader {
 	public static final int maxPolys = 12000;
	public static final int maxPolyVerts = 50;
 	public static final int maxVerts = 12000;
 	public static final int maxTexCoords = 12000;
 	public static final int maxNormals = 12000;
 	public static final int maxVertColors = 12000;
 
 	public WavefrontLoader()
 	{
 	}
 	
 	public Object Load(String filename)
 	{
 		try {
 			return Load(new FileInputStream(filename));
 		} catch (FileNotFoundException e) {
 			System.out.println("Waaa! Pomf =3. What are we going to do on stderr?");
 		}
 		
 		return null;
 	}
 	
 	public Object Load(InputStream in)
 	{
 		MeshLOD mlod = new MeshLOD(maxPolys, maxPolyVerts, maxVerts, maxTexCoords, maxNormals, maxVertColors);
 		
 		LineNumberReader input = new LineNumberReader(new InputStreamReader(in));
 		String line = null;
 		
 		try
 		{
 			for(line = input.readLine(); line != null; line = input.readLine())
 			{
 				if(line.length() > 0)
 				{
 					StringTokenizer tok = new StringTokenizer(line);
 					if(tok.hasMoreElements())
 						tok.nextToken();
 					else
 						continue;
 					
 					if(line.startsWith("v "))
 					{
 						double x = Double.parseDouble(tok.nextToken()), y = Double.parseDouble(tok.nextToken()), z = Double.parseDouble(tok.nextToken());
 						mlod.addVertex(x, y, z);
 					}
 					else if(line.startsWith("vt "))
 					{
 						double u = Double.parseDouble(tok.nextToken()), v = Double.parseDouble(tok.nextToken());
 						mlod.addTexCoord(u, v);
 					}
 					else if(line.startsWith("vn "))
 					{
 						double i = Double.parseDouble(tok.nextToken()), j = Double.parseDouble(tok.nextToken()), k = Double.parseDouble(tok.nextToken());
 						mlod.addNormal(i, j, k);
 					}
 					else if(line.startsWith("f "))
 					{
 						List<Integer> vertices = new ArrayList<Integer>();
 						List<Integer> textures = new ArrayList<Integer>();
 						List<Integer> normals = new ArrayList<Integer>();
 						while(tok.hasMoreTokens())
 						{
 							String token = tok.nextToken();
 							String first = token, second = null, third = null;
 							if(token.indexOf('/') > -1)
 							{
 								first = token.substring(0,  token.indexOf('/'));
 								second = token.substring(token.indexOf('/')+1, token.length());
 								if(second.indexOf('/') > -1)
 								{
 									third = second.substring(second.indexOf('/')+1, second.length());
 									second = second.substring(0, second.indexOf('/'));
 								}
 							}   
 														
 							int vi = Integer.parseInt(first), ti = -1, ni = -1;
 							vertices.add(vi);
 							
 							if(second != null && second.length() > 0)
 							{
 								ti = Integer.parseInt(second); 
 								textures.add(ti);
 							}
 							
 							if(third != null && third.length() > 0)
 							{
 								ni = Integer.parseInt(third);
 								normals.add(ni);
 							}
 							
 						}
 						int[] v = toIntArray(vertices);
 						int[] vt = toIntArray(textures);
 						int[] vn = toIntArray(normals);
 						
 						mlod.addPolygon(v, vt, vn, null);
 					}
 				}
 			}
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 		}
 
 		Mesh m = new Mesh();
 		m.add(mlod);
 		return m;
 	}
 	
 	protected static int[] toIntArray(List<Integer> l)
 	{
 		if(l.size() < 1) return null;
 		int[] ret = new int[l.size()];
 		int index = 0;
 		for(Integer i : l)
 		{
 			ret[index++] = i;
 		}
 		
 		return ret;
 	}
 }
