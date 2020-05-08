 /*
  * 	Reads in obj files, a fairly simplistic model format.
  * 
  * 
  */
 package engine.importing;
 
 import engine.render.Model;
 import engine.render.model_pieces.*;
 import engine.render.ubos.Material;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import javax.vecmath.Vector3f;
 
 public class Obj_Parser extends Parser {
 
 	private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>(); // Vertex
 																		// Coordinates
 	private ArrayList<Vector3f> vertexNormals = new ArrayList<Vector3f>(); // Vertex
 																			// Coordinates
 																			// Normals
 	private ArrayList<String> comments = new ArrayList<String>(); // Vertex
	private ArrayList<String> objects = new ArrayList<String>(); 
 	// Coordinates
 	// Normals
 	// private ArrayList<Vector3f> vertexTextures = new ArrayList<Vector3f>();
 	// // Vertex Coordinates Textures
 
 	private Model model;
 
 	public Obj_Parser() {
 		super();
 		model = new Model();
 	}
 
 	@Override
 	public void readFile(InputStream in) throws Exception {
 		parseObj(
 			new BufferedReader(
 				new InputStreamReader(in)
 			)
 		);
 	}
 
 	@Override
 	public void readUrl(String url) throws Exception {
 		BufferedReader file = new BufferedReader(
 			new InputStreamReader(
 				this.getClass().getClassLoader().getResourceAsStream(url)
 			)
 		);
 		parseObj(file);
 	}
 
 	public void parseObj(BufferedReader file) throws Exception {
 		Mesh mesh = new Mesh();
 		String currentLine = file.readLine();
 		
 		while(currentLine != null) {
 			switch(currentLine.split("\\s+")[0]) {
				case "o":
					objects.add(currentLine.split("\\s+")[1]);
					break;
 				case "v":
 					vertices.add(parseVector(currentLine));
 					break;
 				case "vn":
 					vertexNormals.add(parseVector(currentLine));
 					break;
 				case "f":
 					mesh.addFace(readFace(currentLine));
 					break;
 				case "#":
 					comments.add(currentLine);
 					break;
				case "s":
					//I know this exists, but I really don't care about it
					break;					
 				default:
 					System.out.println("Unknown def encountered while parsing obj");
 					System.out.println("\t"+currentLine);
 					break;
 			}
 			currentLine = file.readLine();
 		}
 		
 		// Give mesh fake material
 		mesh.setMaterial(new Material());
 		model.addMesh(mesh);
 	}
 
 	private Face readFace(String string) throws Exception {
 		Face f = new Face();
 		String[] verts;
 
 		// Split on white Spaces
 		String[] tokens = string.trim().split("\\s+");
 
 		if( string.contains("/")) {
 			for (int i = 1; i < tokens.length; i++) {
 				verts = tokens[i].split("/");
 	
 				if (verts.length == 3) {
 					f.addVertex(vertices.get(Integer.parseInt(verts[0]) - 1));
 					f.addVertexNorm(vertexNormals.get(Integer.parseInt(verts[2]) - 1));
 				} else {
 					System.out.println("Weird OBJ file.  Uses alternative face format");
 					throw new Exception();
 				}
 			}
 		} else {
 			if (tokens.length == 4) {
 				f.addVertex(vertices.get(Integer.parseInt(tokens[1])-1));
 				f.addVertex(vertices.get(Integer.parseInt(tokens[2])-1));
 				f.addVertex(vertices.get(Integer.parseInt(tokens[3])-1));
 			} else {
 				System.out.println("Weird OBJ file.  Uses alternative face format");
 				throw new Exception();
 			}
 		}
 		//faceCount++;
 		return f;
 	}
 
 	private Vector3f parseVector(String string) throws Exception {
 		String[] line = string.trim().split("\\s+");
 
 		return new Vector3f(
 			Float.parseFloat(line[1]), Float.parseFloat(line[2]), Float.parseFloat(line[3])
 		);
 	}
 
 	@Override
 	public Model createModel() {
 		return new Model(model);
 	}
 
 }
