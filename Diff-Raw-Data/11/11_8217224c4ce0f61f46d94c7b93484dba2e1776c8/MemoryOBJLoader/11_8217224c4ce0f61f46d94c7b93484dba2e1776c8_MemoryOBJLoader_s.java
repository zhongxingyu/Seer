 package main;
 
 import java.io.*;
 
 import math.matrices.*;
 
 public class MemoryOBJLoader {
 
 	// OBJ commands
 	// v [x y z]: vertex
 	// vn [x y z]: normal vector
 	// vt [u v w]: texture vertex
 	// f [v/vt/vn v/vt/vn v/vt/vn v/vt/vn]: face
 	// mtllib [file]: material library
 	// usemtl [matIndex]: use material
 	// #: comment
 	// CMOM
 
 	private static boolean SILENT = true;
 	private InputStream is;
 	private int lineIndex;
 	private MemoryList<Vector3> vertices;
 	private MemoryList<Vector2> textureVertices;
 	private MemoryList<Vector3> normals;
 	private MemoryList<Vector3> vertices1;
 	private MemoryList<Vector2> textureVertices1;
 	private MemoryList<Vector3> normals1;
 	private MemoryList<Vector3> binormals;
 	private MemoryList<Vector3> tangents;
 	private MemoryList<Integer> materialIndices;
 	private MemoryModel model;
 	private String baseDir;
 	private MemoryMaterialLibrary materialLibrary;
 	private int currentMaterial = -1;
 
 	MemoryOBJLoader(InputStream is, String baseDir) {
 		int mb50 = 5 * 1024 * 1024;
 		this.is = is;
 		this.baseDir = baseDir;
 		this.lineIndex = 0;
 		this.vertices = new MemoryList<Vector3>(mb50);
 		this.textureVertices = new MemoryList<Vector2>(mb50);
 		this.normals = new MemoryList<Vector3>(mb50);
 		this.vertices1 = new MemoryList<Vector3>(mb50);
 		this.textureVertices1 = new MemoryList<Vector2>(mb50);
 		this.normals1 = new MemoryList<Vector3>(mb50);
 		this.binormals = new MemoryList<Vector3>(mb50);
 		this.tangents = new MemoryList<Vector3>(mb50);
 		this.materialIndices = new MemoryList<Integer>(mb50);
 	}
 
 	public static MemoryModel loadModelFromOBJ(File file) throws IOException {
 		MemoryOBJLoader loader = new MemoryOBJLoader(new FileInputStream(file), file.getParent());
 		loader.load();
 		return loader.getModel();
 	}
 
 	private void load() throws IOException {
 		byte[] data = Utils.fastRead(is);
 		String line = "";
 		for (int i = 0; i < data.length; i++) {
 			char c = (char) data[i];
 			if (c == '\n') {
 				processLine(line);
 				lineIndex++;
 				line = "";
 			}
 			if (c != '\r') {
 				line += c;
 			}
 		}
 		MemoryVertexBuffer vb = new MemoryVertexBuffer(new int[] { vertices1.size() * 3,
 				textureVertices1.size() * 2, normals1.size() * 3, this.materialIndices.size(),
 				binormals.size() * 3, tangents.size() * 3 }, new int[] { 0, 1, 2, 3, 4, 5 },
 				new int[] { 3, 2, 3, 1, 3, 3 }, new MemoryVertexBuffer.Type[] {
 						MemoryVertexBuffer.Type.FLOAT, MemoryVertexBuffer.Type.FLOAT,
 						MemoryVertexBuffer.Type.FLOAT, MemoryVertexBuffer.Type.FLOAT,
 						MemoryVertexBuffer.Type.FLOAT, MemoryVertexBuffer.Type.FLOAT });
 		float[] vertices = new float[vertices1.size() * 3];
 		for (int i = 0; i < vertices1.size(); i++) {
 			Utils.arrayCopy(vertices1.get(i).get1DData(), 0, vertices, i * 3, 3);
 		}
 		float[] normals = new float[normals1.size() * 3];
 		for (int i = 0; i < normals1.size(); i++) {
 			Utils.arrayCopy(normals1.get(i).get1DData(), 0, normals, i * 3, 3);
 		}
 		float[] textureVertices = new float[textureVertices1.size() * 2];
 		for (int i = 0; i < textureVertices1.size(); i++) {
 			Utils.arrayCopy(textureVertices1.get(i).get1DData(), 0, textureVertices, i * 2, 2);
 		}
 		float[] materialIndices = new float[this.materialIndices.size()];
 		for (int i = 0; i < this.materialIndices.size(); i++) {
 			materialIndices[i] = this.materialIndices.get(i);
 		}
 		float[] binormals = new float[this.binormals.size() * 3];
 		for (int i = 0; i < this.binormals.size(); i++) {
 			Utils.arrayCopy(this.binormals.get(i).get1DData(), 0, binormals, i * 3, 3);
 		}
 		float[] tangents = new float[this.tangents.size() * 3];
 		for (int i = 0; i < this.tangents.size(); i++) {
 			Utils.arrayCopy(this.tangents.get(i).get1DData(), 0, tangents, i * 3, 3);
 		}
 		vb.setData(0, vertices);
 		vb.setData(1, textureVertices);
 		vb.setData(2, normals);
 		vb.setData(3, materialIndices);
 		vb.setData(4, binormals);
 		vb.setData(5, tangents);
 		model = new MemoryModel(vb, vertices1.size(), materialLibrary);
 	}
 
 	private void processLine(String line) {
 		String[] parts = line.trim().replace("  ", " ").split("\\s");
 		String command = parts[0];
 		String[] args = new String[parts.length - 1];
 		System.arraycopy(parts, 1, args, 0, args.length);
 		switch (command) {
 		case "v":
 			readVertex(args);
 			break;
 		case "vt":
 			readTextureVertex(args);
 			break;
 		case "vn":
 			readNormal(args);
 			break;
 		case "f":
 			readFace(args);
 			break;
 		case "mtllib":
 			readMaterialLib(args);
 			break;
 		case "usemtl":
 			useMaterial(args);
 			break;
 		case "#":
 			// ignore comments, because thats what they are for :-D
 			break;
 		default:
 			if (command.length() > 0 && !SILENT) {
 				log("Skipping unknown command: " + command);
 			}
 			break;
 		}
 	}
 
 	private void readMaterialLib(String[] args) {
 		if (args.length != 1) {
 			log("Error reading material library: Wrong argument count.");
 			return;
 		}
 		try {
 			materialLibrary = MemoryMTLLoader.loadMaterialFromMTL(new File(baseDir, args[0]));
 		} catch (IOException e) {
 			log("Error reading material library: File not found.");
 		}
 	}
 
 	private void useMaterial(String[] args) {
 		if (args.length != 1) {
 			log("Error using material: Wrong argument count.");
 			return;
 		}
 		Integer id = materialLibrary.materialNames.get(args[0]);
 		if (id == null) {
 			log("Error using material: Material not found.");
 		} else {
 			this.currentMaterial = id;
 		}
 	}
 
 	private void readVertex(String[] args) {
 		if (args.length != 3) {
 			log("Error reading vertex: Wrong argument count.");
 			return;
 		}
 		try {
 			float[] args1 = parseArgs(args);
 			vertices.add(Matrix.vec3(args1[0], args1[1], args1[2]));
 		} catch (NumberFormatException ex) {
 			log("Error reading vertex: Cannot parse numbers.");
 		}
 	}
 
 	private void readTextureVertex(String[] args) {
 		if (args.length != 2 && args.length != 3) {
 			log("Error reading texture vertex: Wrong argument count.");
 			return;
 		}
 		try {
 			float[] args1 = parseArgs(args);
 			if (args.length == 3 && args1[2] != 0) {
 				log("Warning, discarding non zero w component of a texture vertex.");
 			}
			textureVertices.add(Matrix.vec2(args1[0], args1[1]));
 		} catch (NumberFormatException ex) {
 			log("Error reading texture vertex: Cannot parse numbers.");
 		}
 	}
 
 	private void readNormal(String[] args) {
 		if (args.length != 3) {
 			log("Error reading normal: Wrong argument count.");
 			return;
 		}
 		try {
 			float[] args1 = parseArgs(args);
 			normals.add((Vector3) Matrix.vec3(args1[0], args1[1], args1[2]).normalize());
 		} catch (NumberFormatException ex) {
 			log("Error reading normal: Cannot parse numbers.");
 		}
 	}
 
 	private void readFace(String[] args) {
 		if (args.length != 3 && args.length != 4) {
 			log("Error reading face: Wrong argument count.");
 			return;
 		}
 		int[][] args1 = new int[args.length][];
 		for (int i = 0; i < args.length; i++) {
 			args1[i] = parseArgsInt(args[i].split("/"));
 		}
 		Vector3 v0 = vertices.get(args1[0][0] - 1);
 		Vector3 v1 = vertices.get(args1[1][0] - 1);
 		Vector3 v2 = vertices.get(args1[2][0] - 1);
 		Vector2 t0 = textureVertices.get(args1[0][1] - 1);
 		Vector2 t1 = textureVertices.get(args1[1][1] - 1);
 		Vector2 t2 = textureVertices.get(args1[2][1] - 1);
 		Vector3[] axis = calculateAxis(new Vector3[] { v0, v1, v2 }, new Vector2[] { t0, t1, t2 });
 		for (int i = 0; i < 3; i++) {
 			applyTriple(args1[i]);
 			this.binormals.add(axis[0]);
 			this.tangents.add(axis[1]);
 		}
 		if (args.length == 4) {
 			Vector3 v3 = vertices.get(args1[3][0] - 1);
 			Vector2 t3 = textureVertices.get(args1[3][1] - 1);
 			Vector3[] axis1 = calculateAxis(new Vector3[] { v2, v3, v0 }, new Vector2[] { t2, t3,
 					t0 });
 			for (int i = 0; i < 3; i++) {
 				this.binormals.add(axis1[0]);
 				this.tangents.add(axis1[1]);
 			}
 			applyTriple(args1[2]);
 			applyTriple(args1[3]);
 			applyTriple(args1[0]);
 		}
 	}
 
 	private Vector3[] calculateAxis(Vector3[] vertices, Vector2[] textureCoords) {
 		Vector3 a = (Vector3) vertices[1].subtract(vertices[0]);
 		Vector3 b = (Vector3) vertices[2].subtract(vertices[0]);
 		Vector2 c = (Vector2) textureCoords[2].subtract(textureCoords[0]);
 		Vector2 d = (Vector2) textureCoords[1].subtract(textureCoords[0]);
 		Vector3 t = (Vector3) (a.multiply(c.getY())).subtract(b.multiply(d.getY()));
 		Vector3 u = (Vector3) (a.multiply(c.getX())).subtract(b.multiply(d.getX()));
 		float tFac = d.getX() * c.getY() - d.getY() * c.getX();
 		float uFac = d.getY() * c.getX() - d.getX() * c.getY();
 		t = (Vector3) t.multiply(1f / tFac);
 		u = (Vector3) u.multiply(1f / uFac);
 		return new Vector3[] { (Vector3) t.normalize(), (Vector3) u.normalize() };
 	}
 
 	private void applyTriple(int[] triple) {
 		if (triple[0] <= vertices.size()) {
 			if (triple[0] != -1) {
 				vertices1.add(vertices.get(triple[0] - 1));
 			}
 		} else {
 			log("Error reading face: Referenced vertex does not exist.");
 		}
 		if (triple[1] <= textureVertices.size()) {
 			if (triple[1] != -1) {
 				textureVertices1.add(textureVertices.get(triple[1] - 1));
 			}
 		} else {
 			log("Error reading face: Referenced texture vertex does not exist.");
 		}
 		if (triple[2] <= normals.size()) {
 			if (triple[2] != -1) {
 				normals1.add(normals.get(triple[2] - 1));
 			}
 		} else {
 			log("Error reading face: Referenced normal does not exist.");
 		}
 		this.materialIndices.add(this.currentMaterial);
 	}
 
 	private float[] parseArgs(String[] args) throws NumberFormatException {
 		float[] args1 = new float[args.length];
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].length() == 0) {
 				args1[i] = -1;
 				continue;
 			}
 			args1[i] = Float.parseFloat(args[i]);
 		}
 		return args1;
 	}
 
 	private int[] parseArgsInt(String[] args) throws NumberFormatException {
 		int[] args1 = new int[args.length];
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].length() == 0) {
 				args1[i] = -1;
 				continue;
 			}
 			args1[i] = Integer.parseInt(args[i]);
 		}
 		return args1;
 	}
 
 	private void log(String str) {
 		System.out.println(lineIndex + ": " + str);
 	}
 
 	private MemoryModel getModel() {
 		return model;
 	}
 }
