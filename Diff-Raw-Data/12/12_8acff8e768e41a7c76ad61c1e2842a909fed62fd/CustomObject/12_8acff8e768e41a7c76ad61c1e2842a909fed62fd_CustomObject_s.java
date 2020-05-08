 package feynstein.shapes;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileNotFoundException;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class CustomObject extends Shape<CustomObject> {
 	private final String VERTEX = "v";
 	private final String FACE = "f";
 	private final String TEXCOORD = "vt";
 	private final String NORMAL = "vn";
 	
 	private File sourceFile;
 
 	private ArrayList<Particle> verts;
 	private ArrayList<Edge> edges;
 	private ArrayList<Triangle> tris;
 	private ArrayList<Vector3d> normals;
 
     public CustomObject() {
 		objectType = "CustomObject";
 		verts = new ArrayList<Particle>();
 		edges = new ArrayList<Edge>();
 		tris = new ArrayList<Triangle>();
 		normals = new ArrayList<Vector3d>();
 		
     }
     
     public CustomObject set_file(String filename) {
 	sourceFile = new File(filename);
 	return this;
     }
 
     public CustomObject compileShape() {
 	if (sourceFile == null) {
 	    throw new RuntimeException("You must specify the file " +
 				       "attribute of a CustomObject.");
 	} else {
 		String line;
 		try {
 			
 			BufferedReader buffer = new BufferedReader(new FileReader(sourceFile));
 			while ((line = buffer.readLine()) != null) {
 				// remove duplicate whitespace
 				StringTokenizer parts = new StringTokenizer(line, " ");
 				int numTokens = parts.countTokens();
 				if (numTokens == 0)
 					continue;
 				String type = parts.nextToken();
 				
 				// add vertex to particles list
 				if (type.equals(VERTEX)) {
 					double x = Float.parseFloat(parts.nextToken());
 					double y = Float.parseFloat(parts.nextToken());
 					double z = Float.parseFloat(parts.nextToken());
 					
 					Vector3d vertex = new Vector3d(x,y,z);
 					verts.add(new Particle(vertex));
 					
 				} else if (type.equals(FACE)) {
 					
 					Triangle newTri = parseTriangleFace(line, numTokens-1);
 					tris.add(newTri);
 					// add edge for each vertex pair
 					for (int i = 0; i < 3; i++) {
 						for (int j = i+1; j < 3; j++) {
							edges.add(new Edge(i,j));
 						}
 					}
 					
 				} else if (type.equals(NORMAL)) {
 					double x = Float.parseFloat(parts.nextToken());
 					double y = Float.parseFloat(parts.nextToken());
 					double z = Float.parseFloat(parts.nextToken());
 					Vector3d normal = new Vector3d(x,y,z);
 					normals.add(normal);
 				}  
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		localMesh = new Mesh(verts, edges, tris);
 	}
 		return this;
     }
 	
 	private Triangle parseTriangleFace(String line, int faceLength) {
 		boolean emptyVt = line.indexOf("//") > -1;
 		if(emptyVt) {
 			line = line.replace("//", "/");
 		}
 		StringTokenizer parts = new StringTokenizer(line);
 		parts.nextToken();
 		StringTokenizer subParts = new StringTokenizer(parts.nextToken(), "/");
 		int partLength = subParts.countTokens();
 		boolean hasuv = partLength >= 2 && !emptyVt;
 		boolean hasn = partLength == 3 || (partLength == 2 && emptyVt);
 		
 		int [] v = new int[faceLength];
 		int [] uv = new int[faceLength];
 		int [] n = new int[faceLength];
 		
 		for (int i = 0; i < faceLength; i++) {
 			if (i > 0)
 				subParts = new StringTokenizer(parts.nextToken(), "/");
 			
 			int index = i;
 			v[index] = (short) (Short.parseShort(subParts.nextToken()) - 1);
 			if (hasuv) {
 				uv[index] = (short) (Short.parseShort(subParts.nextToken()) - 1);
 			}
 			if (hasn) {
 				n[index] = (short) (Short.parseShort(subParts.nextToken()) - 1);
 			}
 		}
 		// TODO(sam): this method assumes that face length is 3. If there are more, we 
 		// should handle them accordingly
 		Triangle t = new Triangle(v);
 		if(hasn)
 			t.setNormals(normals.get(n[0]), normals.get(n[1]), normals.get(n[2]));
 		return t;
 	}
 }
