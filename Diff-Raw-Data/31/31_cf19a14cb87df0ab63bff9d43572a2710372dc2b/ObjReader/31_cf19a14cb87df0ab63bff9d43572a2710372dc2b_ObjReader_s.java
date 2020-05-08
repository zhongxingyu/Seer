 package meshes.reader;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.vecmath.Point2f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import meshes.PointCloud;
 import meshes.WireframeMesh;
 
 /**
  * Reads on .obj file including normals and texture coordinates.
  */
 public class ObjReader {
 	
 	public static class RawData{
 		
 		public ArrayList<Point3f> vertices = new ArrayList<Point3f>();
 		public ArrayList<Point2f> texCoords = new ArrayList<>();
 		public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
 		public ArrayList<int[][]> faces = new ArrayList<int[][]>();
 		public boolean hasNormals, hasTex;
 		public float xMin, xMax, yMin, yMax, zMin, zMax;
 		
 		public RawData(){
 			hasNormals = hasTex = false;
 			
 			xMin = Float.MAX_VALUE;
 			xMax = Float.NEGATIVE_INFINITY;
 			yMin = Float.MAX_VALUE;
 			yMax = Float.NEGATIVE_INFINITY;
 			zMin = Float.MAX_VALUE;
 			zMax = Float.NEGATIVE_INFINITY;
 		}
 
 		public void normalize() {
 			float scale = Math.min(Math.min(
 					Math.max(
 					2 / (xMax - xMin), 
 					2 / (yMax - yMin)), 
 					Math.max(
 					2 / (xMax - xMin), 
 					2 / (zMax - zMin))),
 					Math.max(
 					2 / (yMax - yMin), 
 					2 / (zMax - zMin)));
 			
 			
 			Vector3f trans = new Vector3f(
 					-(xMax + xMin) / 2,
 					-(yMax + yMin) / 2,
 					-(zMax + zMin) / 2);
 			
 
 			for(Point3f v: vertices){
 				v.add(trans);
 				v.scale(scale);
 			}
 		}
 	}
 
 	
 	/**
 	 * Read an .obj file and return a wireframe representation of it
 	 * @throws IOException 
 	 */
 	public static WireframeMesh read(String fileName, boolean normalize) throws IOException{
 		
 		RawData data = readRawData(fileName, normalize);
 		WireframeMesh myMesh = new WireframeMesh();
 		myMesh.hasTexture = data.hasTex;
 		myMesh.vertices = data.vertices;
 		myMesh.faces = new ArrayList<int[]>();
 		myMesh.texCoords = new ArrayList<>(Collections.nCopies(data.vertices.size(), new Point2f(-100, -100)));
 		for(int[][] fc : data.faces){
 			int[] face = new int[fc.length];
 			for(int i = 0; i < face.length; i++){
 				face[i] = fc[i][0] -1; //make it 0-based
 				if(data.hasTex)
 					myMesh.texCoords.set(fc[i][0] - 1, data.texCoords.get(fc[i][1] - 1));
 			}
 			myMesh.faces.add(face);
 		}
 		
 		return myMesh;
 	}
 	
 	
 
 	
 	/**
 	 * Simple helper method which reads out an obj file and dumps the data in the rawData 
 	 * struct
 	 * @param fileName
 	 * @return
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private static RawData readRawData(String fileName, boolean normalize) throws FileNotFoundException,
 			IOException {
 		BufferedReader reader;
 		RawData data = new RawData();
 		
 		data.hasNormals = true;
		data.hasTex= true;
 
 		// Extents for normalization
 		reader = new BufferedReader(new FileReader(fileName));
 
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			// Read line
 			String[] s = line.split("\\s+");
 
 			// Parse
 			if (s[0].compareTo("v") == 0) {
 				// Position
 				Point3f v = new Point3f(
 						Float.valueOf(s[1]).floatValue(),
 						Float.valueOf(s[2]).floatValue(),
 						Float.valueOf(s[3]).floatValue());
 				data.vertices.add(v);
 
 				// Update extent
 				if (v.x < data.xMin)
 					data.xMin = v.x;
 				if (v.x > data.xMax)
 					data.xMax = v.x;
 				if (v.y < data.yMin)
 					data.yMin = v.y;
 				if (v.y > data.yMax)
 					data.yMax = v.y;
 				if (v.z < data.zMin)
 					data.zMin = v.z;
 				if (v.z > data.zMax)
 					data.zMax = v.z;
 			} else if (s[0].compareTo("vn") == 0) {
 				// Normal
 				Vector3f n = new Vector3f(
 				 Float.valueOf(s[1]).floatValue(),
 				 Float.valueOf(s[2]).floatValue(),
 				 Float.valueOf(s[3]).floatValue());
 				data.normals.add(n);
 			} else if (s[0].compareTo("vt") == 0) {
 				// Texture
 				Point2f t = new Point2f();
 				t.x = Float.valueOf(s[1]).floatValue();
 				t.y = Float.valueOf(s[2]).floatValue();
 				data.texCoords.add(t);
 			} else if (s[0].compareTo("f") == 0) {
 				// Indices
 				int[][] indices = new int[s.length - 1][3];
 
 				// For all vertices
 				int i = 1;
 				while (i < s.length) {
 					// Get indices for vertex position, tex. coords., and
 					// normals
 					String[] ss = s[i].split("/");
 					int k = 0;
 					while (k < ss.length) {
 						if (ss[k].length() > 0)
 							indices[i - 1][k] = Integer.valueOf(ss[k])
 									.intValue();
 						else {
 							indices[i - 1][k] = -1;
 							if (k == 1)
 								data.hasTex = false;
 							if (k == 2)
 								data.hasNormals = false;
 						}
 						k++;
 					}
 					if (ss.length == 1) {
 						data.hasTex = false;
 						data.hasNormals = false;
 					}
 					i++;
 				}
 				data.faces.add(indices);
 			} else if (s[0].length() > 0 && s[0].charAt(0) != '#') {
 				System.out.print("Unknown token '".concat(line).concat("'\n"));
 			}
 		}
 		reader.close();
 		
 		if(normalize){
 			data.normalize();
 		}
 		return data;
 	}
 
 	public static PointCloud readAsPointCloud(String filename, boolean normalize) throws IOException {
 		RawData data = readRawData(filename, normalize);
 				
 		PointCloud pc = new PointCloud();
 		pc.points = data.vertices;
 		if(data.hasNormals){
 			pc.normals = new ArrayList<Vector3f>();
 			for(Point3f t: pc.points){
 				pc.normals.add(new Vector3f());
 			}
 			for(int[][] fc : data.faces){
 				for(int k = 0; k < fc.length; k++){
 				pc.normals.get(fc[k][0]-1).set( data.normals.get(fc[k][2]-1));
 				}
 			}
 			
 		}
 		else{
 			System.out.println("Read point cloud without normals!");
 		}
 		
 		return pc;
 	}
 }
