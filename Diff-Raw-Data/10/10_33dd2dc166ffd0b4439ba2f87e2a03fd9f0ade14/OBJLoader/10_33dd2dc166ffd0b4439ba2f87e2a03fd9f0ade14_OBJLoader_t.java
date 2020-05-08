 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.media.opengl.GL;
 
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureIO;
 
 //Usage as follows:
 //	 
 //	 //declare globally:
 //	 private int displayList;
 //	 
 //	 //load the model.obj and model.mtl file and create a display list (once in initialization)
 //	 //The .obj and .mtl files are assumend to be in the project folder
 //	 OBJLoader objLoader = new OBJLoader("object", 1.0f, gl);
 //	 displayList = objLoader.getDisplayList();
 //	 
 //	 //render the model (every frame)
 //	 gl.glCallList(displayList);
 
 /** 
  * 
  * Loads an obj. file, supports textures, materials and normals. 
  * 
  * @author derbauer
  *
  */
 public class OBJLoader {
 
 	/**
 	 * Dummy z-value for 2d-texture coordinates. 
 	 */
 	private static final float DUMMY_Z_TC = -5.0f;
 
 	// vertices, normals and texture coordinates of the model
 	private List<Tuple3> vertices;
 	private List<Tuple3> normals;
 	private List<Tuple3> textureCoords;
 	private float[] bounds = {Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE, Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE};
 	
 	/**
 	 * uses 2d or 3d texture coordinates
 	 */
 	private boolean has3DTextureCoords = true;
 	
 
 	/**
 	 * faces
 	 */
 	private Faces faces; 
 	/**
 	 * materials of the faces
 	 */
 	private FaceMaterials faceMaterials; 
 	/**
 	 * materials
 	 */
 	private Materials materials; 
 
 	/**
 	 * filename (without file extension, i.e. "model" instead of "model.obj")
 	 */
 	private String modelFileName; 
 	/**
 	 * scale factor: models should be at the origin and in size between 0 and 1
 	 */
 	private float scalingFactor; 
 
 	/**
 	 * display list of the model
 	 */
 	private int modelDisplayList; 
 
 	
 	/**
 	 * Loads the according .obj and .mtl file, converts them to the 
 	 * (getDisplayList()).
 	 * 
 	 * @param name
 	 *            name of the obj. file without extension: i.e. "model" instead of
 	 *            "model.obj". file must be in the project home. 
 	 * @param size
 	 *            size of the model (1 for no scaling)
 	 * @param gl
 	 *            reference to gl
 	 */
 	public OBJLoader(String name, float size, GL gl) {
 
 		modelFileName = name;
 		scalingFactor = size;
 		initModelData();
 
 		loadModel(modelFileName);
 		scale();
 		drawToList(gl);
 		if(materials!=null)materials.resetRenderMatName();
 		//reportOnModel();
 		
 	}
 
 	public float[] getBounds(){
 		return bounds;
 	}
 	
 	/**
 	 * initialze the fields
 	 */
 	private void initModelData() {
 
 		vertices = new ArrayList<Tuple3>();
 		normals = new ArrayList<Tuple3>();
 		textureCoords = new ArrayList<Tuple3>();
 
 		faces = new Faces(vertices, normals, textureCoords);
 		faceMaterials = new FaceMaterials();
 	}
 
 
 	/**
 	 * reads fileName.obj
 	 * @param fileName the file name
 	 */
 	private void loadModel(String fileName) {
 		
 		fileName = fileName + ".obj";
 		try {
 
 			FileInputStream fileInputStream = new FileInputStream(fileName);
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
 			        fileInputStream));
 			readModel(bufferedReader);
 			bufferedReader.close();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * reads the file line by line, and sets the according fields
 	 * 
 	 * @param br
 	 *            buffered reader that reads the file.
 	 */
 	private void readModel(BufferedReader br){
 
 		boolean isLoaded = true;
 
 		int lineNumber = 0;
 		String line;
 		boolean isFirstCoord = true;
 		boolean isFirstTextureCoord = true;
 		int numberOfFaces = 0;
 
 		try {
 			while (((line = br.readLine()) != null) && isLoaded) {
 				lineNumber++;
 				if (line.length() > 0) {
 					//delete unneccessary whitespaces
 					line = line.trim();
 
 					if (line.startsWith("v ")) { // vertex
 						isLoaded = addVertex(line);
 						if (isFirstCoord)
 							isFirstCoord = false;
 					} else if (line.startsWith("vt")) { // texture coordinate
 						isLoaded = addTexCoord(line, isFirstTextureCoord);
 						if (isFirstTextureCoord)
 							isFirstTextureCoord = false;
 					} else if (line.startsWith("vn")) // normal
 						isLoaded = addNormal(line);
 					else if (line.startsWith("f ")) { // face
 						isLoaded = faces.addFace(line);
 						numberOfFaces++;
 					}
 					// load material
 					else if (line.startsWith("mtllib ")) 
 						materials = new Materials(line.substring(7));
 					else if (line.startsWith("usemtl "))
 						faceMaterials.addUse(numberOfFaces, line.substring(7));
 					else if (line.charAt(0) == 'g') { // group name
 						// not implemented
 					} else if (line.charAt(0) == 's') { // smoothing group
 						// not implemented
 					} else if (line.charAt(0) == '#') // comment line
 						continue;
 					else
 //						System.out.println("Ignoring line " + lineNumber + " : "
 //						        + line);
 						continue;
 				}
 			}
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(1);
 		}
 
 		if (!isLoaded) {
 			System.err.println("Error loading model "+modelFileName);
 			System.exit(1);
 		}
 	} 
 
 	/**
 	 * Adds vertex from line "v x y z" to vertices Arraylist
 	 * @param line
 	 *            line to be addes
 	 * @return true, if successful
 	 */
 	private boolean addVertex(String line)
 	{
 		Tuple3 vertex = readTuple3(line);
 		//convert into other coordinate systems here: x, y, z => x, z, -y
 		//vertex = new Tuple3(vertex.getX(), vertex.getZ(), -vertex.getY());
 		
 		if (vertex.getX() < bounds[0])
 			bounds[0] = vertex.getX();
 		if (vertex.getY() < bounds[1])
 			bounds[1] = vertex.getY();
 		if (vertex.getZ() < bounds[2])
 			bounds[2] = vertex.getZ();
 		if (vertex.getX() > bounds[3])
 			bounds[3] = vertex.getX();
 		if (vertex.getY() > bounds[4])
 			bounds[4] = vertex.getY();
 		if (vertex.getZ() > bounds[5])
 			bounds[5] = vertex.getZ();
 		
 		if (vertex != null) {
 			vertices.add(vertex);
 			return true;
 		}
 		return false;
 	}
 
 	/** 
 	 * Reads from a line starting with v or vn the folowing 3-tupel
 	 * @param line the line
 	 * @return the tupel, or null if not successful
 	 */
 	private Tuple3 readTuple3(String line)
 	{
 		//the values are separated by a whitespace
 		StringTokenizer tokens = new StringTokenizer(line, " ");
 		//the first is v or vn: skip
 		tokens.nextToken();
 
 		try {
 			float x = Float.parseFloat(tokens.nextToken());
 			float y = Float.parseFloat(tokens.nextToken());
 			float z = Float.parseFloat(tokens.nextToken());
 
 			return new Tuple3(x, y, z);
 		} catch (NumberFormatException e) {
 			System.err.println(e.getMessage());
 		}
 
 		return null; 
 	}
 
 	/** 
 	 * Add texture coordinate to textureCoords
 	 * @param line line to be read
 	 * @param isFirstTextureCoord is it the first texture coordinate?
 	 * @return true, if successful
 	 */
 	private boolean addTexCoord(String line, boolean isFirstTextureCoord)
 	{
 
 		if (isFirstTextureCoord) {
 			has3DTextureCoords = checkTextureCoords3D(line);
 		}
 
 		Tuple3 textureCoord = readTextureCoordTuple(line);
 		if (textureCoord != null) {
 			// coord (0,0) is top left !
 			textureCoords.add(new Tuple3(textureCoord.getX(), 1-textureCoord.getY(), textureCoord.getZ()));
 			return true;
 		}
 		return false;
 	} 
 	
 	/**
 	 * check, if 2D or 3D texture coordinates are used. 
 	 * @param line 
 	 * @return true, if 3D
 	 */
 	private boolean checkTextureCoords3D(String line)
 	{
 
 		String[] tokens = line.split("\\s+");
 		return (tokens.length == 4);
 	}
 
 	/**
 	 * Reads a texture coordinate tupel
 	 * @param line 
 	 * @return the tupel, or null if not successful
 	 */
 	private Tuple3 readTextureCoordTuple(String line)
 	{
 		//values are separated by a whitespace
 		StringTokenizer tokens = new StringTokenizer(line, " ");
 		//skip vt
 		tokens.nextToken(); 
 
 		try {
 			float x = Float.parseFloat(tokens.nextToken());
 			float y = Float.parseFloat(tokens.nextToken());
 
 			float z = DUMMY_Z_TC;
 			if (has3DTextureCoords)
 				z = Float.parseFloat(tokens.nextToken());
 
 			return new Tuple3(x, y, z);
 		} catch (NumberFormatException e) {
 			System.err.println(e.getMessage());
 		}
 
 		return null;
 	}
 
 	
 	/**
 	 * Adds a normal to Normals ArrayList
 	 * @param line
 	 * @return true, if successful
 	 */
 	private boolean addNormal(String line)
 	{
 
 		Tuple3 normalCoord = readTuple3(line);
 		//convert into other coordinate systems here: x, y, z => x, z, -y
 		//normalCoord = new Tuple3(normalCoord.getX(), normalCoord.getZ(), -normalCoord.getY());
 		if (normalCoord != null) {
 			normals.add(normalCoord);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * scale the model
 	 */
 	private void scale(){
 		Tuple3 vertex;
 		float x, y, z;
 		for (int i = 0; i < vertices.size(); i++) {
 			vertex = (Tuple3)vertices.get(i);
 			x = vertex.getX() * scalingFactor;
 			vertex.setX(x);
 			y = vertex.getY() * scalingFactor;
 			vertex.setY(y);
 			z = vertex.getZ() * scalingFactor;
 			vertex.setZ(z);
 		}
 	} 
 
 	/**
 	 * generates a displaylist from the model
 	 * @param gl gl reference
 	 */
 	private void drawToList(GL gl){
 
 		modelDisplayList = gl.glGenLists(1);
 		gl.glNewList(modelDisplayList, GL.GL_COMPILE);
 		{
 			gl.glPushMatrix();
 			{
 				// render every face individually
 				String faceMaterial;
 				for (int i = 0; i < faces.getNumFaces(); i++) {
 					// find the material of face i
 					faceMaterial = faceMaterials.findMaterial(i);
 					if (faceMaterial != null && materials != null)
 						// activate material
 						materials.renderWithMaterial(faceMaterial, gl);
 					// draw face i
 					faces.renderFace(i, gl);
 				}
 				if (materials != null)
 					materials.switchOffTex(gl);
 			}
 			gl.glPopMatrix();
 		}
 		gl.glEndList();
 	}
 
 	/**
 	 * Macht Ausgaben zum Model
 	 */
 	private void reportOnModel() {
 
 		System.out.println("No. of vertices: " + vertices.size());
 		System.out.println("No. of normal coords: " + normals.size());
 		System.out.println("No. of tex coords: " + textureCoords.size());
 		System.out.println("No. of faces: " + faces.getNumFaces());
 
 
 		if (materials != null)
 			materials.showMaterials();
 		faceMaterials.showUsedMaterials();
 	}
 
 
 
 	//-------------------- inner Class ---------------------------
 	/**
 	 * Faces stores the information for each face of a model. A face is
 	 * represented by three arrays of indicies for the vertices, normals, and
 	 * tex coords used in that face. facesVertIdxs, facesTexIdxs, and
 	 * facesNormIdxs are ArrayLists of those arrays; one entry for each face.
 	 * renderFace() is supplied with a face index, looks up the associated
 	 * vertices, normals, and tex coords indicies arrays, and uses those arrays
 	 * to access the actual vertices, normals, and tex coords data for rendering
 	 * the face.
 	 */
 	public class Faces {
 
 		private static final float DUMMY_Z_TC = -5.0f;
 
 		/* indicies for vertices, tex coords, and normals used
 		 by each face */
 		public List<int[]> facesVertIdxs;
 		public List<int[]> facesTexIdxs;
 		public List<int[]> facesNormIdxs;
 
 		// references to the model's vertices, normals, and tex coords
 		public List<Tuple3> verts;
 		public List<Tuple3> normals;
 		public List<Tuple3> texCoords;
 
 		// the calculated face normal.
 		// calculated by the normals of the vertices forming this face
 		public List<Tuple3> faceNormals;
 		public Tuple3[] vertNormals;
 
 		public Faces(List<Tuple3> vs, List<Tuple3> ns, List<Tuple3> ts) {
 
 			verts = vs;
 			normals = ns;
 			texCoords = ts;
 
 			facesVertIdxs = new ArrayList<int[]>();
 			facesTexIdxs = new ArrayList<int[]>();
 			facesNormIdxs = new ArrayList<int[]>();
 			faceNormals = new ArrayList<Tuple3>();
 			vertNormals = new Tuple3[400000];
 
 		} // end of Faces()
 
 		public boolean addFace(String line)
 		/* get this face's indicies from line "f v/vt/vn ..."
 		 with vt or vn index values perhaps being absent. */
 		{
 
 			try {
 				line = line.substring(2); // skip the "f "
 				StringTokenizer st = new StringTokenizer(line, " ");
 				int numTokens = st.countTokens(); // number of v/vt/vn tokens
 				// create arrays to hold the v, vt, vn indicies
 				int v[] = new int[numTokens];
 				int vt[] = new int[numTokens];
 				int vn[] = new int[numTokens];
 
 				float[] facenormal = new float[3];
 				facenormal[0] = 0.0f;
 				facenormal[1] = 0.0f;
 				facenormal[2] = 0.0f;
 
 				for (int i = 0; i < numTokens; i++) {
 					String faceToken = addFaceVals(st.nextToken()); // get a v/vt/vn token
 					// System.out.println(faceToken);
 
 					StringTokenizer st2 = new StringTokenizer(faceToken, "/");
 					int numSeps = st2.countTokens(); // how many '/'s are there in the token
 
 					v[i] = Integer.parseInt(st2.nextToken());
 					vt[i] = (numSeps > 1) ? Integer.parseInt(st2.nextToken())
 					        : 0;
 					vn[i] = (numSeps > 2) ? Integer.parseInt(st2.nextToken())
 					        : 0;
 					// add 0's if the vt or vn index values are missing;
 					// 0 is a good choice since real indicies start at 1
 
 					vertNormals[v[i]] = normals.get(vn[i] - 1);
 				}
 
 				// get 2 vectors from the vertices to form a triangle and
 				// then calculate cross product to get face normal!
 
 				Tuple3 vec1 = verts.get(v[1] - 1)
 				        .substract(verts.get(v[0] - 1));
 				Tuple3 vec2 = verts.get(v[2] - 1)
 				        .substract(verts.get(v[0] - 1));
 
 				// calculate cross product
 				vec2 = vec1.crossProduct(vec2);
 				vec2.normalize();
 
 				// store the indicies for this face
 				facesVertIdxs.add(v);
 				facesTexIdxs.add(vt);
 				facesNormIdxs.add(vn);
 				faceNormals.add(vec2);
 			} catch (NumberFormatException e) {
 				System.err.println("Incorrect face index");
 				System.err.println(e.getMessage());
 				return false;
 			}
 			return true;
 		} // end of addFace()
 
 		private String addFaceVals(String faceStr)
 		/* A face token (v/vt/vn) may be missing vt or vn
 		 index values; add 0's in those cases.
 		 */
 		{
 
 			char chars[] = faceStr.toCharArray();
 			StringBuffer sb = new StringBuffer();
 			char prevCh = 'x'; // dummy value
 
 			for (int k = 0; k < chars.length; k++) {
 				if (chars[k] == '/' && prevCh == '/') // if no char between /'s
 					sb.append('0'); // add a '0'
 				prevCh = chars[k];
 				sb.append(prevCh);
 			}
 			return sb.toString();
 		} // end of addFaceVals()
 
 		public void renderFace(int i, GL gl)
 		/* Render the ith face by getting the vertex, normal, and tex
 		 coord indicies for face i. Use those indicies to access the
 		 actual vertex, normal, and tex coord data, and render the face.
 
 		 Each face uses 3 array of indicies; one for the vertex
 		 indicies, one for the normal indicies, and one for the tex
 		 coord indicies.
 
 		 If the model doesn't use normals or tex coords then the indicies
 		 arrays will contain 0's.
 		 */
 		{
 
 			if (i >= facesVertIdxs.size()) // i out of bounds?
 				return;
 
 			int[] vertIdxs = (int[])(facesVertIdxs.get(i));
 			// get the vertex indicies for face i
 
 			int polytype;
 			if (vertIdxs.length == 3)
 				polytype = GL.GL_TRIANGLES;
 			else if (vertIdxs.length == 4)
 				polytype = GL.GL_QUADS;
 			else
 				polytype = GL.GL_POLYGON;
 
 			gl.glBegin(polytype);
 
 			// get the normal and tex coords indicies for face i
 			int[] normIdxs = (int[])(facesNormIdxs.get(i));
 			int[] texIdxs = (int[])(facesTexIdxs.get(i));
 
 			/* render the normals, tex coords, and vertices for face i
 			 by accessing them using their indicies */
 			Tuple3 vert, norm, texCoord;
 			for (int f = 0; f < vertIdxs.length; f++) {
 
 				if (normIdxs[f] != 0) { // if there are normals, render them
 					norm = (Tuple3)normals.get(normIdxs[f] - 1);
 					gl.glNormal3f(norm.getX(), norm.getY(), norm.getZ());
 				}
 
 				if (texIdxs[f] != 0) { // if there are tex coords, render them
 					texCoord = (Tuple3)texCoords.get(texIdxs[f] - 1);
 					if (texCoord.getZ() == DUMMY_Z_TC) // using 2D tex coords
 						gl.glTexCoord2f(texCoord.getX(), texCoord.getY());
 					else
 						// 3D tex coords
 						gl.glTexCoord3f(texCoord.getX(), texCoord.getY(),
 						        texCoord.getZ());
 				}
 
 				vert = (Tuple3)verts.get(vertIdxs[f] - 1); // render the vertices
 				gl.glVertex3f(vert.getX(), vert.getY(), vert.getZ());
 			}
 
 			gl.glEnd();
 		} // end of renderFace()
 
 		public int getNumFaces() {
 
 			return facesVertIdxs.size();
 		}
 	}
 
 	//-------------------------  Inner Class  --------------------------------------
 	
 	/**
 	 * FaceMaterials stores the face indicies where a material is first used. At
 	 * render time, this information is utilized to change the rendering
 	 * material when a given face needs to be drawn.
 	 */
 	public class FaceMaterials {
 
 		private HashMap<Integer, String> faceMats;
 		// the face index (integer) where a material is first used
 
 		// for reporting
 		private HashMap<String, Integer> matCount;
 
 		// how many times a material (string) is used
 
 		public FaceMaterials() {
 
 			faceMats = new HashMap<Integer, String>();
 			matCount = new HashMap<String, Integer>();
 		} // end of FaceMaterials()
 
 		public void addUse(int faceIdx, String matName) {
 
 			// store the face index and the material it uses
 			if (faceMats.containsKey(faceIdx)) // face index already present
 				System.out.println("Face index " + faceIdx
 				        + " changed to use material " + matName);
 			faceMats.put(faceIdx, matName);
 
 			// store how many times matName has been used by faces
 			if (matCount.containsKey(matName)) {
 				int i = (Integer)matCount.get(matName) + 1;
 				matCount.put(matName, i);
 			} else
 				matCount.put(matName, 1);
 		} // end of addUse()
 
 		public String findMaterial(int faceIdx) {
 
 			return (String)faceMats.get(faceIdx);
 		}
 
 		public void showUsedMaterials()
 		/* List all the materials used by faces, and the number of
 		 faces that have used them. */
 		{
 
 			System.out.println("No. of materials used: " + matCount.size());
 
 			// build an iterator of material names
 			Set<String> keys = matCount.keySet();
 			Iterator<String> iter = keys.iterator();
 
 			// cycle through the hashmap showing the count for each material
 			String matName;
 			int count;
 			while (iter.hasNext()) {
 				matName = iter.next();
 				count = (Integer)matCount.get(matName);
 
 				System.out.print(matName + ": " + count);
 				System.out.println();
 			}
 		} // end of showUsedMaterials()
 
 	} // end of FaceMaterials class
 
 	//------------------ inner class Materials -----------------------------------
 	
 	/**
 	 * This class does two main tasks: 1. it loads the material details from the
 	 * MTL file, storing them as Material objects in the materials ArrayList. 2.
 	 * it sets up a specified material's colours or textures to be used when
 	 * rendering -- see renderWithMaterial()
 	 */
 	public class Materials {
 
 		private ArrayList<Material> materials;
 		// stores the Material objects built from the MTL file data
 
 		// for storing the material currently being used for rendering
 		private String renderMatName = null;
 
 		private boolean usingTexture = false;
 
 		public Materials(String mtlFnm) {
 
 			materials = new ArrayList<Material>();
 
 			String mfnm = mtlFnm;
 			try {
 				//System.out.println("Loading material from " + mfnm);
 				BufferedReader br = new BufferedReader(new FileReader(mfnm));
 				readMaterials(br);
 				br.close();
 			} catch (IOException e) {
 				System.err.println(e.getMessage());
 			}
 
 		} // end of Materials()
 
 		private void readMaterials(BufferedReader br)
 		/* Parse the MTL file line-by-line, building Material
 		 objects which are collected in the materials ArrayList. */
 		{
 
 			try {
 				String line;
 				Material currMaterial = null; // current material
 
 				while (((line = br.readLine()) != null)) {
 					line = line.trim();
 					if (line.length() == 0)
 						continue;
 
 					if (line.startsWith("newmtl ")) { // new material
 						if (currMaterial != null) // save previous material
 							materials.add(currMaterial);
 
 						// start collecting info for new material
 						currMaterial = new Material(line.substring(7));
 					} else if (line.startsWith("map_Kd ")) { // texture filename
 						String fileName = line.substring(7);
 						currMaterial.loadTexture(fileName);
 					} else if (line.startsWith("Ka ")) // ambient colour
 						currMaterial.setKa(readTuple3(line));
 					else if (line.startsWith("Kd ")) // diffuse colour
 						currMaterial.setKd(readTuple3(line));
 					else if (line.startsWith("Ks ")) // specular colour
 						currMaterial.setKs(readTuple3(line));
 					else if (line.startsWith("Ns ")) { // shininess
 						float val = Float.valueOf(line.substring(3))
 						        .floatValue();
 						currMaterial.setNs(val);
 					} else if (line.charAt(0) == 'd') { // alpha
 						float val = Float.valueOf(line.substring(2))
 						        .floatValue();
 						currMaterial.setD(val);
 					} else if (line.startsWith("illum ")) { // illumination model
 						// not implemented
 					} else if (line.charAt(0) == '#') // comment line
 						continue;
 					else
 						//System.out.println("Ignoring MTL line: " + line);
 						continue;
 
 				}
 				materials.add(currMaterial);
 			} catch (IOException e) {
 				System.err.println(e.getMessage());
 			}
 		} // end of readMaterials()
 
 		private Tuple3 readTuple3(String line)
 		/* The line starts with an MTL word such as Ka, Kd, Ks, and
 		 the three floats (x, y, z) separated by spaces
 		 */
 		{
 
 			StringTokenizer tokens = new StringTokenizer(line, " ");
 			tokens.nextToken(); // skip MTL word
 
 			try {
 				float x = Float.parseFloat(tokens.nextToken());
 				float y = Float.parseFloat(tokens.nextToken());
 				float z = Float.parseFloat(tokens.nextToken());
 
 				return new Tuple3(x, y, z);
 			} catch (NumberFormatException e) {
 				System.err.println(e.getMessage());
 			}
 
 			return null; // means an error occurred
 		} // end of readTuple3()
 
 		public void showMaterials()
 		// list all the Material objects
 		{
 
 			System.out.println("No. of materials: " + materials.size());
 			Material m;
 			for (int i = 0; i < materials.size(); i++) {
 				m = (Material)materials.get(i);
 				m.showMaterial();
 				// System.out.println();
 			}
 		} // end of showMaterials()
 
 		// ----------------- using a material at render time -----------------
 
 		public void renderWithMaterial(String faceMat, GL gl)
 		/* Render using the texture or colours associated with the
 		 material, faceMat. But only change things if faceMat is
 		 different from the current rendering material, whose name
 		 is stored in renderMatName.
 		 */
 		{
 
 			if (!faceMat.equals(renderMatName)) { // is faceMat is a new material?
 				renderMatName = faceMat;
 				switchOffTex(gl); // switch off any previous texturing
 
 				// set up new rendering material
 				Texture tex = getTexture(renderMatName);
 				if (tex != null) { // use the material's texture
 					// System.out.println("Using texture with " + renderMatName);
 					switchOnTex(tex, gl);
 				} else
 					// use the material's colours
 					setMaterialColors(renderMatName, gl);
 			}
 		} // end of renderWithMaterial()
 
 		public void switchOffTex(GL gl)
 		// switch texturing off and put the lights on;
 		// also called from ObjModel.drawToList()
 		{
 
 			if (usingTexture) {
 				gl.glDisable(GL.GL_TEXTURE_2D);
 				usingTexture = false;
 				gl.glEnable(GL.GL_LIGHTING);
 			}
 		} // end of resetMaterials()
 
 		private void switchOnTex(Texture tex, GL gl)
 		// switch the lights off, and texturing on
 		{
 
 			//gl.glDisable(GL.GL_LIGHTING);
 			gl.glEnable(GL.GL_TEXTURE_2D);
 			usingTexture = true;
 			tex.bind();
 		} // end of resetMaterials()
 
 		private Texture getTexture(String matName)
 		// return the texture associated with the material name
 		{
 
 			Material m;
 			for (int i = 0; i < materials.size(); i++) {
 				m = (Material)materials.get(i);
 				if (m.hasName(matName))
 					return m.getTexture();
 			}
 			return null;
 		} // end of getTexture()
 
 		private void setMaterialColors(String matName, GL gl)
 		// start rendering using the colours specifies by the named material
 		{
 
 			Material m;
 			for (int i = 0; i < materials.size(); i++) {
 				m = (Material)materials.get(i);
 				if (m.hasName(matName))
 					m.setMaterialColors(gl);
 			}
 		} // end of setMaterialColors()
 
 		//-------------------------  inner Class  -----------------------------
 		/**
 		 * A Material object holds colour and texture information for a named
 		 * material. The Material object also manages the rendering using its
 		 * colours (see setMaterialColors()). The rendering using the texture is
 		 * done by the Materials object.
 		 */
 		public class Material {
 
 			private String name;
 
 			// colour info
 			private Tuple3 ka, kd, ks; // ambient, diffuse, specular colours
 			private float ns, d; // shininess and alpha
 
 			// texture info
 			private String texFnm;
 			private Texture texture;
 
 			public Material(String nm) {
 
 				name = nm;
 
 				d = 1.0f;
 				ns = 0.0f;
 				ka = null;
 				kd = null;
 				ks = null;
 
 				texFnm = null;
 				texture = null;
 			} // end of Material()
 
 			public void showMaterial() {
 
 				System.out.println(name);
 				if (ka != null)
 					System.out.println("  Ka: " + ka.toString());
 				if (kd != null)
 					System.out.println("  Kd: " + kd.toString());
 				if (ks != null)
 					System.out.println("  Ks: " + ks.toString());
 				if (ns != 0.0f)
 					System.out.println("  Ns: " + ns);
 				if (d != 1.0f)
 					System.out.println("  d: " + d);
 				if (texFnm != null)
 					System.out.println("  Texture file: " + texFnm);
 			} // end of showMaterial()
 
 			public boolean hasName(String nm) {
 
 				return name.equals(nm);
 			}
 
 			// --------- set/get methods for colour info --------------
 
 			public void setD(float val) {
 
 				d = val;
 			}
 
 			public float getD() {
 
 				return d;
 			}
 
 			public void setNs(float val) {
 
 				ns = val;
 			}
 
 			public float getNs() {
 
 				return ns;
 			}
 
 			public void setKa(Tuple3 t) {
 
 				ka = t;
 			}
 
 			public Tuple3 getKa() {
 
 				return ka;
 			}
 
 			public void setKd(Tuple3 t) {
 
 				kd = t;
 			}
 
 			public Tuple3 getKd() {
 
 				return kd;
 			}
 
 			public void setKs(Tuple3 t) {
 
 				ks = t;
 			}
 
 			public Tuple3 getKs() {
 
 				return ks;
 			}
 
 			public void setMaterialColors(GL gl)
 			// start rendering using this material's colour information
 			{
 
 				//	  	  System.out.println(" --- SET MATERIAL COLOR ---");
 				if (ka != null) { // ambient color
 					float[] colorKa = {ka.getX(), ka.getY(), ka.getZ(), 0.5f};
 					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT,
 					        colorKa, 0);
 				}
 				if (kd != null) { // diffuse color
 					float[] colorKd = {kd.getX(), kd.getY(), kd.getZ(), 0.5f};
 					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE,
 					        colorKd, 0);
 				}
 				if (ks != null) { // specular color
 					float[] colorKs = {ks.getX(), ks.getY(), ks.getZ(), 0.5f};
 					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR,
 					        colorKs, 0);
 				}
 
 				if (ns != 0.0f) { // shininess
 					gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, ns);
 				}
 
 				if (d != 1.0f) { // alpha
 					// not implemented
 				}
 			} // end of setMaterialColors()
 
 			// --------- set/get methods for texture info --------------
 
 			public void loadTexture(String fnm) {
 
 				try {
 					texFnm = fnm;
 					texture = TextureIO.newTexture(new File(texFnm), false);
 					texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER,
 					        GL.GL_NEAREST);
 					texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER,
 					        GL.GL_NEAREST);
		    		texture.setTexParameteri( GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
		    		texture.setTexParameteri( GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
 				} catch (Exception e) {
 					System.err.println("Error loading texture " + texFnm);
 				}
 			} // end of loadTexture()
 
 			public void setTexture(Texture t) {
 
 				texture = t;
 			}
 
 			public Texture getTexture() {
 
 				return texture;
 			}
 
 		} // end of inner Class Material
 
 		
         /**
          * @param renderMatName material name 
          */
         public final void resetRenderMatName() {
         
         	this.renderMatName = "";
         }
 	} // end of Inner class Materials
 
 	/**
 	 * @author derbauer
 	 * datastructure for a 3-tuple
 	 */
 	public class Tuple3 {
 
 		private float x, y, z;
 
 		/**
 		 * constructor
 	     * @param x x
 	     * @param y y
 	     * @param z z
 	     */
 	    public Tuple3(float x, float y, float z) {
 
 		    super();
 		    this.x = x;
 		    this.y = y;
 		    this.z = z;
 	    }
 
 		
 	    /**
 	     * @return returns x
 	     */
 	    public final float getX() {
 	    
 	    	return x;
 	    }
 
 		
 	    /**
 	     * @param x x
 	     */
 	    public final void setX(float x) {
 	    
 	    	this.x = x;
 	    }
 
 		
 	    /**
 	     * @return returns y
 	     */
 	    public final float getY() {
 	    
 	    	return y;
 	    }
 
 		
 	    /**
 	     * @param y y
 	     */
 	    public final void setY(float y) {
 	    
 	    	this.y = y;
 	    }
 
 		
 	    /**
 	     * @return returns z
 	     */
 	    public final float getZ() {
 	    
 	    	return z;
 	    }
 
 		
 	    /**
 	     * @param z z 
 	     */
 	    public final void setZ(float z) {
 	    
 	    	this.z = z;
 	    }
 
 
 		/**
 		 * returns a tuple (this - t) 
 	     * @param t the tuple that shall be subtracted
 	     * @return the tupel (this - t)
 	     */
 	    public Tuple3 substract(Tuple3 t) {
 
 		    return new Tuple3(x - t.getX(), y- t.getY(), z - t.getZ());
 	    }
 
 
 		/**
 		 * returns the cross product (this X t)
 	     * @param t tupel to be multiplied with
 	     * @return crossproduct (this X t)
 	     */
 	    public Tuple3 crossProduct(Tuple3 t) {
 
 	    	return new Tuple3(y * t.getZ() - z * t.getY(), 
 	    			z * t.getX() - x * t.getZ(),
 	    			x * t.getY() - y * t.getX());
 	    }
 
 
 		/**
 	     * normalize the vector
 	     */
 	    public void normalize() {
 
 	    	float length = (float)Math.sqrt(x*x + y*y + z*z);
 		    x /= length;
 		    y /= length;
 		    z /= length;
 		    
 	    }
 	}
 
 	
     /**
      * returns the previously created display list. 
      * @return the diplay list
      */
     public final int getDisplayList() {
     
     	return modelDisplayList;
     }
     
     /**
      * creates and returns the display list in context gl
      * @return the displa list
      */
     public final int getDisplayList(GL gl) {
     	drawToList(gl);
     	return modelDisplayList;
     }
 
     
     
 	
     /**
      * @return returns materials
      */
     public final Materials getMaterials() {
     
     	return materials;
     }
 }
