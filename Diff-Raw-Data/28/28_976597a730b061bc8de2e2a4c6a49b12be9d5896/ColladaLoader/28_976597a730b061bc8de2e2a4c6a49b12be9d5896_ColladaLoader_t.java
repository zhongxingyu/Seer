 package loader;
 
 import game.Game;
 import graphics.compatibility.skeleton.Animation;
 import graphics.compatibility.skeleton.Bone;
 import graphics.compatibility.skeleton.Pose;
 import graphics.compatibility.skeleton.Skeleton;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
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
 		
 		List<Bone> boneIndexList = new LinkedList<Bone>();
 		
 		for(Node n : roots)
 		{
 			//this recursively calls itself to build the joint hierarchy
 			addBones(skeleton, Skeleton.ROOT, n, boneIndexList);
 		}
 		
 		Map<String, Integer> boneIndices = new HashMap<String, Integer>(); 
 		int bicount = 0;
 		for(Bone b : boneIndexList){
 			boneIndices.put(b.name, bicount++);
 		}
 		values.put("boneIndices", boneIndices);
 		skeleton.addBoneIndexMap(boneIndices);
 		
 		values.put("skeleton", skeleton);
 		
 		//Load animation keyframes
 		try{
 			d = db.parse(filename);
 		}catch(Exception e){}
 		
 		Node animations = findChild(d.getElementsByTagName("library_animations"), "library_animations");
 		
 		//return
 		if(animations == null){
 			return rearrange(values);
 		}
 		
 		List<Node> animList = findChildren(animations.getChildNodes(), "animation");
 		
 		//list of poses to become the animation
 		List<Pose> poses;
 		
 		Animation anim = new Animation("whatever");
 		
 		
 		for(Node node : animList){
 			float[] keyframes = null;
 			Matrix4f[] transforms = null;
 			int framecount = 0;
 				
 			
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
 							
 							buffer.flip();
 						}
 					}
 					
 				}
 				
 				if(transforms == null || keyframes == null){
 					continue;
 				}
 				
 				poses = new ArrayList<Pose>();
 				for(int i = 0; i <  framecount; i++){
 					poses.add(new Pose(keyframes[i], transforms[i]));
 				}
 				
 				anim.addBone(id, poses);
 				
 			}
 			
 		}
 		values.put("animation", anim);
 		
 		//add the animation to the skeleton
 		//TODO: make this generalized
 		skeleton.animations.add(anim);
 
 		
 		
 		//Load the vertex weights
 		
 		//There are 3 things we need to worry about
 		
 		//	1. Armature_Cube-skin-weights-array:	a list of all the weights, this corresponds exactly with the second thing.
 		//	2. v:									this is the id of whatever vertices are weighted to. I wish it were joints but it's not
 		// 	3. vcount:								This is the number of joints? each vertex is weighted to, sort of like a variable stride
 		
 		//	New things
 		// 	Inverse bind poses look like they are necessary. Looks like they are used to go between world and bone spaces. 
 		
 		Node lib_controller = findChild(d.getElementsByTagName("library_controllers"), "library_controllers");
 		
 		Node controller = findChild(lib_controller.getChildNodes(), "controller");
 		Node skin = findChild(controller.getChildNodes(), "skin");
 		
 		
 		//get bindPoses and Skin weights
 		List<Matrix4f> bindPoses = new ArrayList<Matrix4f>();
 		List<Float> skinWeights  = new ArrayList<Float>();
 		List<String> joints = new ArrayList<String>();
 		
 		
 		
 		for(Node s : findChildren(skin.getChildNodes(), "source")){
 			String sid = getAttribute(s, "id");
 			
 			
 			if(sid.contains("bind_poses")){
 				Node frameNode = findChild(s.getChildNodes(), "float_array");
 		
 				FloatBuffer fbuf = BufferUtils.createFloatBuffer(32);
 				int buffered = 0;
 				
 				String sval = frameNode.getTextContent();
 				String[] vals = sval.split(" ");
 				for(String str : vals){
 					buffered++;
 					fbuf.put(Float.parseFloat(str));
 					
 					if(buffered == 16){
 						buffered = 0;
 						fbuf.rewind();
 						Matrix4f mat = new Matrix4f();
 						mat.loadTranspose(fbuf);
 						fbuf.rewind();
 						bindPoses.add(mat);
 					}
 					
 				}
 				
 			}
 			
 			if(sid.contains("weights")){
 				Node frameNode = findChild(s.getChildNodes(), "float_array");
 				
 				String sval = frameNode.getTextContent();
 				String[] vals = sval.split(" ");
 				for(String str : vals){
 					skinWeights.add(Float.parseFloat(str));
 				}
 			}
 			
 			if(sid.contains("joints")){
 				Node frameNode = findChild(s.getChildNodes(), "Name_array");
 				
 				String sval = frameNode.getTextContent();
 				String[] vals = sval.split(" ");
 				for(String str : vals){
 					joints.add(str.replace("_", "."));
 				}
 			}
 		}
 		
 		Game.bindPoses = bindPoses;
 		Game.joints = joints;
 		
 		List<Integer> v = new ArrayList<Integer>();
 		List<Integer> vcount = new ArrayList<Integer>();
 		
 		//get v/vcount node
 		Node vertWeight = findChild(skin.getChildNodes(), "vertex_weights");
 		
 		//get v
 		Node vcountNode = findChild(vertWeight.getChildNodes(),"vcount");
 		String sval = vcountNode.getTextContent();
 		String[] vals = sval.split(" ");
 		for(String str : vals){
 			vcount.add(Integer.parseInt(str));
 		}
 		
 		//get v
 		Node vNode = findChild(vertWeight.getChildNodes(),"v");
 		sval = vNode.getTextContent();
 		vals = sval.split(" ");
 		for(String str : vals){
 			v.add(Integer.parseInt(str));
 		}
 		
 		
 		
 		
 		//iterate over vcount and v, for each vertex we read in the number of weights w from vcount, 
 		//then the next w pairs are jointweight index pairs
 		int vIndex = 0;
 		
 		//we want an array of lists of pairs, uck java. Array of maps maybe?
 		List<Map<String, Float>> VertexJointWeights = new ArrayList<Map<String, Float>>();
 		int vertexIndex = 0;
 		
 		for(int i : vcount)
 		{
 			Map<String, Float> m = new HashMap<String, Float>();
 			
 			for(int j = vIndex; j < vIndex+i; j++){
 				int joint = v.get(2 * j);
 				int weight = v.get(2 * j + 1);
 				
 				m.put(joints.get(joint), skinWeights.get(weight));
 				
 			}
 			VertexJointWeights.add(vertexIndex++,m);
 			vIndex += i;
 		}
 		
 		
 		//Go over each bone index and add inverse bind to that bone
 		for(Entry<String,Integer> e : boneIndices.entrySet()){
 			skeleton.bones.get(e.getKey()).invBind = bindPoses.get(e.getValue());
 		}
 		
 		
 		values.put("vertexJointWeights", VertexJointWeights);
 		
 		return rearrange(values);
 	}
 
 	//recursively add bones if the xml has subnodes.
 	private static void addBones(Skeleton skeleton, String rootName, Node node, List<Bone> boneIndices){
 		String boneName = getAttribute(node, "name");
 		
 		//get the node holding the transform matrix for the bone
 		Node transformNode = findChild(node.getChildNodes(), "matrix");
 		
 		Bone b = skeleton.addChild(rootName, boneName, getMatrixFrom(transformNode));
 		boneIndices.add(b);
 		
 		
 		//for some silly reason the joint nodes are actually called "node"
 		List<Node> subBones = findChildren(node.getChildNodes(), "node");
 		
 		for(Node n : subBones){
 			addBones(skeleton, boneName, n, boneIndices);
 		}		
 	}
 	
 	//gets a matrix from a node with it in the values.
 	private static Matrix4f getMatrixFrom(Node n){
 		String something = n.getTextContent();
 		
 		String[] vals = something.trim().replaceAll("\\s+", " ").split(" ");
 		FloatBuffer fbuf = BufferUtils.createFloatBuffer(32);
 		int count = 0;
 		for(String s : vals){
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
 
 	//uses one kinda hacky unchecked to do the list<map<>> stuff for weights
 	@SuppressWarnings("unchecked")
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
 
 		//input joints
 		Object vwo = in.get("vertexJointWeights");
 		
 		List<Map<String,Float>> vw = null;
 		
 		if(vwo != null){
 			vw = (List<Map<String, Float>>)vwo;
 		}
 		
 		
 		
 		List<Vert[]> init_faces = new ArrayList<Vert[]>();
 
 		List<Vert> unique_verts = new ArrayList<Vert>();
 		List<Integer> unique_indices = new ArrayList<Integer>();
 
 		List<Vector3f> output_vertices = new ArrayList<Vector3f>();
 		List<Vector3f> output_normals = new ArrayList<Vector3f>();
 		List<Vector2f> output_texCoords = new ArrayList<Vector2f>();
 		List<Map<String,Float>> output_weights = new ArrayList<Map<String, Float>>();
 		
 		
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
 					
 					//
 					if(vw != null){
 						output_weights.add(vw.get(v.vertexIndex + 1));	
 					}
 					
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
 		
 		//
 		Map<String, Integer> boneIndices = (Map<String,Integer>)in.get("boneIndices");
 		
 		if(vw != null){
 			//turn output weights into two float arrays, jointweights, inlined array of the weight values, indices are skeleton bone index
 			float[] jointWeights = new float[output_weights.size() * 4];
 			int[] jointIndices = new int[output_weights.size() * 4];
 			
 			counter = 0;
 			for(int i = 0; i < output_weights.size(); i++){
 				Float[] weightAmounts = (Float[])(output_weights.get(i).values()).toArray(new Float[4]);
 				String[] weightNames = (String[])(output_weights.get(i).keySet().toArray(new String[4]));
 				
				for(int j = 0; j < 3; j++){
 					//joint gets 0 weight, index 0 for null record
					jointWeights[counter] = weightAmounts[j] == null ? 0.f : weightAmounts[j];
 					jointIndices[counter++] = weightNames[j] == null ? 0 : boneIndices.get(weightNames[j]);
 				}
 			}
 			
 			in.put("jointWeights", jointWeights);
 			in.put("jointIndices", jointIndices);
 		}
 
 		return in;
 
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
