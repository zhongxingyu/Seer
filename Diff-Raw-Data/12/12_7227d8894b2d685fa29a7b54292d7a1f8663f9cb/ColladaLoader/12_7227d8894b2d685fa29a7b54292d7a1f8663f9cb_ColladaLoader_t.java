 package loader;
 
 import graphics.compatibility.skeleton.Skeleton;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class ColladaLoader {
 
 	public static HashMap<String, Object> load(String filename) {
 
 		//a return value which maps strings to the arrays of their values
 		HashMap<String, Object> values = new HashMap<String, Object>();
 
 		//setup for gross xml parsing.
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = null;
 		Document d = null;
 		try {
 			db = dbf.newDocumentBuilder();
 			d = db.parse(filename);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// get the geometry name to strip, so we can ignore whether a model
 		// started off as a cylinder or cube
 		Node geoNameNode = findChild(d.getElementsByTagName("geometry"), "geometry");
 		String geoName = getAttribute(geoNameNode, "id");
 
 		Node mesh = findChild(d.getElementsByTagName("mesh"), "mesh");
 
 		// get source->float_array instances, add values by their name
 		List<Node> sources = findChildren(mesh.getChildNodes(), "source");
 		Node polylist = findChild(mesh.getChildNodes(), "polylist");
 
 		for (Node n : sources) {
 			Node dataSource;
 
 			if ((dataSource = findChild(n.getChildNodes(), "float_array")) != null) {
 				String data = dataSource.getTextContent();
 				String[] vals = data.split(" ");
 				float[] floats = new float[vals.length];
 
 				for (int k = 0; k < vals.length; k++) {
 					floats[k] = Float.parseFloat(vals[k]);
 				}
 
 				// strip name out
 				String name = getAttribute(dataSource, "id").replaceAll(geoName + "-", "");
 				values.put(name, floats);
 			}
 		}
 
 		//get faces/elements array.
 		Node p = findChild(polylist.getChildNodes(), "p");
 		if (p != null) {
 			String data = p.getTextContent();
 			String[] vals = data.split(" ");
 			float[] floats = new float[vals.length];
 
 			for (int k = 0; k < vals.length; k++) {
 				floats[k] = Float.parseFloat(vals[k]);
 			}
 			values.put("elements", floats);
 			System.out.println("elements");
 		}
 
 		// get skeleton information
 		Skeleton skeleton = new Skeleton();
 		try{
 			d = db.parse(filename);
 		}catch(Exception e){}
 		
 		Node vis_scenes = findChild(d.getElementsByTagName("library_visual_scenes"), "library_visual_scenes");
 		
 		//visual scene contains the joint hierarchy
 		Node visual_scene = findChild(vis_scenes.getChildNodes(), "visual_scene");
 		
 		List<Node> sceneList = findChildren(visual_scene.getChildNodes(), "node");
 		
 		//go through the scenelist and find the armature
 		Node armature = null;
 		for(Node n : sceneList){
 			String attr = getAttribute(n, "id");
 			if(attr.equals("Armature")){
 				armature = n;
 				break;
 			}
 		}
 		
 		//if there is no armature, don't try and get a skeleton
 		if(armature == null){
 			return rearrange(values);
 		}
 		
 		//for some horrible reason, they named the nodes in an armature "node"
 		List<Node> roots = findChildren(armature.getChildNodes(), "node");
 		
 		for(Node n : roots)
 		{
 			//this recursively calls itself to build the joint hierarchy
 			addBones(skeleton, Skeleton.ROOT, n);
 		}
 		
 		values.put("skeleton", skeleton);
 		
 		
 		//TODO: Animation loading time
 		
 		// revise bone objects to just use a transform matrix. That will be input from animation and output to shader/
 		
 		// add some hacks to static models so they use transforms rather than position/orientation. Showing skeletons as a
 		// static model is a hack, so it should get hacky rather than the bone system
 		
 		// loading models
 		// 		each bone has a node in the animation library, if it's animated
 		//		there are two major subnodes. In one, the keyframes are given (as percentages of 24fps)
 		//		in the second, the transform matrices are given inline, so for 3 frames, a 3x16=48 float 
 		//		buffer is given of the transform matrices for that bone. It will need to be parsed into
 		//		keyframes and such for the instance of the thing.
 		
 		//	blender things:
 		// decide if rotation keyframes are enough. I think they should be, but we might need locRot.
 		//		
 		
 		
 		
 		
 		try{
 			d = db.parse(filename);
 		}catch(Exception e){}
 		
 		Node animations = findChild(d.getElementsByTagName("library_animations"), "library_animations");
 		
 		//return
 		if(animations == null){
 			return rearrange(values);
 		}
 		
 		List<Node> animList = findChildren(animations.getChildNodes(), "animation");
 		
 		
 		//per bone:
 		int framecount = 0; //actually per bone?
 		
 		float[] keyframes;
 		Matrix4f[] transforms;
 		
 		for(Node node : animList){
 			String id = getAttribute(node, "id").replace("Armature_", "").replace("_pose_matrix", "").replace("_",".");
 			
 			List<Node> animationSources = findChildren(node.getChildNodes(), "source");
 			
 			for(Node source : animationSources){
 				String sid = getAttribute(source, "id");
 				
 				//input node contains keyframe number and frames
 				if(sid.contains("input")){
 					Node frameNode = findChild(source.getChildNodes(), "float_array");
 					
 					framecount = Integer.parseInt(getAttribute(frameNode, "count"));
 					//get input
 					String sval = frameNode.getTextContent();
 					String[] keyf = sval.split(" ");
 					keyframes = new float[framecount];
 					for(int i = 0; i < keyf.length; i++){
 						keyframes[i] = (int)(24 * Float.parseFloat(keyf[i]));
 					}
 				}
 				//output contains the actual transform matrices
 				else if(sid.contains("output")){
 					Node frameNode = findChild(source.getChildNodes(), "float_array");
 					
 					String sval = frameNode.getTextContent();
 					String[] mat = sval.split(" ");
 					transforms = new Matrix4f[framecount];
 					
 					FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
 					
					for(int i = 0; i < mat.length;){	
 						buffer.put(Float.parseFloat(mat[i++])); //inc here to check mod against zero 
 						
 						//after inserting 16 floats, rewind the buffer, and store it to a transpose matrix
 						if(i%16 == 0){
 							buffer.rewind();
 							
 							//i/16 - 1 <-- gross
 							transforms[i/16 - 1] = new Matrix4f();
							transforms[i/16 - 1].loadTranspose(buffer);
 							transforms[i/16 - 1].transpose();
 							
 							buffer.flip();
 						}
 					}
 					
 				}
 				
 				
 			}
 			
 		}
 		
 
 		System.out.println("sure");
 		
 		
 		return rearrange(values);
 	}
 
 	//recursively add bones if the xml has subnodes.
 	private static void addBones(Skeleton skeleton, String rootName, Node node){
 		String boneName = getAttribute(node, "name");
 		
 		//get the node holding the transform matrix for the bone
 		Node transformNode = findChild(node.getChildNodes(), "matrix");
 		
 		skeleton.addChild(rootName, boneName, getMatrixFrom(transformNode));
 		
 		//for some silly reason the joint nodes are actually called "node"
 		List<Node> subBones = findChildren(node.getChildNodes(), "node");
 		
 		for(Node n : subBones){
 			addBones(skeleton, boneName, n);
 		}		
 	}
 	
 	//gets a matrix from a node with it in the values.
 	private static Matrix4f getMatrixFrom(Node n){
 		String something = n.getTextContent();
 		
 		//TODO: Seriously, you named it stwings!?
 		String[] stwings = something.trim().replaceAll("\\s+", " ").split(" ");
 		FloatBuffer fbuf = BufferUtils.createFloatBuffer(32);
 		int count = 0;
 		for(String s : stwings){
 			if (count++ > 16){
 				break;
 			}
 			
 			fbuf.put(Float.parseFloat(s));
 		}
 		fbuf.rewind();
 		
 		Matrix4f ret = new Matrix4f();
		ret.loadTranspose(fbuf);
 		
 		return ret;
 	}
 	
 	//provides map style lookup to attributes
 	private static String getAttribute(Node node, String key){
 		NamedNodeMap attributes = node.getAttributes();
 		for(int i = 0; i < attributes.getLength(); i++){
 			if(attributes.item(i).getNodeName().equals(key))
 			{
 				return attributes.item(i).getNodeValue();
 			}
 		}
 		
 		return null;
 	}
 	
 	//returns a real java list of nodes which are named the name
 	private static List<Node> findChildren(NodeList nodeList, String pattern) {
 		List<Node> ret = new ArrayList<Node>();
 
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node n = nodeList.item(i);
 			if (n.getNodeName().equals(pattern)) {
 				ret.add(n);
 			}
 		}
 		return ret;
 	}
 
 	//finds the first instance of a node with a name, but lets you not write for loops to make up for nodelist not being iterable
 	private static Node findChild(NodeList nodeList, String s) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node n = nodeList.item(i);
 			if (n.getNodeName().equals(s)) {
 				return n;
 			}
 		}
 		return null;
 	}
 
 	private static HashMap<String, Object> rearrange(
 			HashMap<String, Object> in) {
 
 		// //
 
 		int indexCount = 0;
 
 		float[] vertices;
 		float[] elements;
 		float[] normals;
 		float[] texCoords;
 
 		List<Vector3f> init_vertices = new ArrayList<Vector3f>();
 		List<Vector3f> init_normals = new ArrayList<Vector3f>();
 		List<Vector2f> init_texCoords = new ArrayList<Vector2f>();
 
 		List<Vert[]> init_faces = new ArrayList<Vert[]>();
 
 		List<Vert> unique_verts = new ArrayList<Vert>();
 		List<Integer> unique_indices = new ArrayList<Integer>();
 
 		List<Vector3f> output_vertices = new ArrayList<Vector3f>();
 		List<Vector3f> output_normals = new ArrayList<Vector3f>();
 		List<Vector2f> output_texCoords = new ArrayList<Vector2f>();
 
 		float[] temp = (float[])in.get("positions-array");
 		for (int i = 0; i < temp.length; i += 3) {
 			init_vertices.add(new Vector3f(temp[i], temp[i + 1], temp[i + 2]));
 		}
 
 		temp = (float[])in.get("normals-array");
 		for (int i = 0; i < temp.length; i += 3) {
 			init_normals.add(new Vector3f(temp[i], temp[i + 1], temp[i + 2]));
 		}
 
 		temp = (float[])in.get("map-0-array");
 		for (int i = 0; i < temp.length; i += 2) {
 			init_texCoords.add(new Vector2f(temp[i], temp[i + 1]));
 		}
 
 		temp = (float[])in.get("elements");
 		for (int i = 0; i < temp.length;) {
 			Vert[] face = new Vert[3];
 			for (int j = 0; j < 3; j++) {
 				face[j] = new Vert(indexCount++, (int) temp[i++],
 						(int) temp[i++], (int) temp[i++]);
 			}
 			init_faces.add(face);
 		}
 
 		int uid = 0;
 
 		// iterate over here and add it
 		for (Vert[] f : init_faces) {
 			for (int j = 0; j < 3; j++) {
 				Vert v = f[j];
 				boolean flag = true;
 				int index = -1;
 
 				for (Vert uvert : unique_verts) {
 					if (uvert.vertexIndex == v.vertexIndex
 							&& uvert.textureIndex == v.textureIndex
 							&& uvert.normalIndex == v.normalIndex) {
 						index = uvert.index;
 						flag = false;
 						break;
 					}
 				}
 
 				if (flag) {
 					v.index = uid++;
 					unique_verts.add(v);
 					index = v.index;
 
 					output_vertices.add(init_vertices.get(v.vertexIndex + 1));
 					output_texCoords
 							.add(init_texCoords.get(v.textureIndex + 1));
 					output_normals.add(init_normals.get(v.normalIndex + 1));
 
 				}
 
 				unique_indices.add(index);
 			}
 
 		}
 
 		elements = new float[unique_indices.size()];
 		vertices = new float[3 * output_vertices.size()];
 		texCoords = new float[2 * output_texCoords.size()];
 		normals = new float[3 * output_normals.size()];
 
 		int counter = 0;
 		for (int i = 0; i < unique_indices.size(); i++) {
 			elements[counter++] = unique_indices.get(i);
 		}
 
 		counter = 0;
 		for (Vector3f v : output_vertices) {
 			vertices[counter++] = v.x;
 			vertices[counter++] = v.y;
 			vertices[counter++] = v.z;
 		}
 
 		counter = 0;
 		for (Vector2f v : output_texCoords) {
 			texCoords[counter++] = v.x;
 			texCoords[counter++] = 1.0f - v.y;
 		}
 
 		counter = 0;
 		for (Vector3f v : output_normals) {
 			normals[counter++] = v.x;
 			normals[counter++] = v.y;
 			normals[counter++] = v.z;
 		}
 
 		//remove unorganized parts
 		in.remove("positions");
 		in.remove("normals");
 		in.remove("texCoords");
 		in.remove("elements");
 		
 		
 		//put in organized parts
 		in.put("positions", vertices);
 		in.put("normals", normals);
 		in.put("texCoords", texCoords);
 		in.put("elements", elements);
 
 		return in;
 
 		// //
 
 	}
 
 	protected static class Vert {
 		public int vertexIndex, normalIndex, textureIndex, index;
 
 		public Vert(int index, int v, int n, int t) {
 			this.index = index;
 			this.vertexIndex = v - 1;
 			this.textureIndex = t - 1;
 			this.normalIndex = n - 1;
 		}
 
 		public Vert(int index, String v, String n, String t) {
 			this(index, Integer.parseInt(v), Integer.parseInt(n), Integer
 					.parseInt(t));
 		}
 	}
 
 }
