 package EWUPackage.loaders;
 
 import java.io.FileNotFoundException;
 
 import EWUPackage.scene.Scene;
 import EWUPackage.scene.*;
 import EWUPackage.scene.primitives.*;
 import EWUPackage.utilities.*;
 import java.util.*;
 
 import collada.*;
 
 import com.jogamp.common.nio.Buffers;
 
 public class ColladaLoader extends PMesh{
 
 	private static final long serialVersionUID = 1L;
 
 	public ColladaLoader(Scene aScene)
 	{
 		super(aScene);
 	} // end constructor
 	
 	public boolean load(String filepath) throws FileNotFoundException
 	{
 		fileType = "DAE"; // Set the file type to OBJ in case we ever want to later identify its source
 		filePath = Utils.dirFromPath(filepath); // The fileName is the ENTIRE path to the file (including the filename)
 		objName = Utils.fileFromPath(filepath); // The objName is strictly the filename to begin with
 		active = true;	//We've just created an object...so make it active!
 		next = null;	//Next object in the linked list is null
 		
 		Collada collada = new Collada(filepath);
 		
 		//Loads the materials into the PMesh
 		loadEffects(collada.effects);
 		try
 		{
 			loadModel(collada);
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.toString());
 			return false;
 		}
 			
 		//This simply loads the modelMat with an identity matrix.
 		modelMat = MatrixOps.newIdentity();
 		return true;
 	}
 	
 	
 	//loads the first model only from the Collada file
 	/* Date: 04/26/2011
 	 * Author: Joshua Olson
 	 * Changes: Loaded normals into PMesh object. Fixed loading input checking to determine vertex position source
 	 * 	Throws exception upon model not having any normals
 	 */
 	@SuppressWarnings("unused")
 	public void loadModel(Collada collada) throws Exception
 	{
 		String sceneUrl = collada.scene.instance_visual_scene.url.substring(1);  //starting point into the collada file
 		
 		Nodec node = null;		//the first node in the scene
 		boolean flag = true;
 		String igurl;			//the starting url 
 		Mesh mesh=null;			//the mesh structure to be loaded
 		Triangles curTri = null;		//each Triangles is a surfCell
 		//Vertices tempVerties=null;  //the vertices for the mesh
 		
 		//ArrayList<Double3D> normals = new ArrayList<Double3D>();  //holds the normals until they are loaded into PMesh
 		Instance_Geometry instanceGeom;		  //holds the geometry data
 		
 		Library_Visual_Scenes lbs = collada.vscenes;  
 		Library_Geometries lg = collada.geometries;
 			
 		//start at the starting point and find the node we want to draw
 		for(int x = 0; x< lbs.scenes.size() && flag;x++)
 		{
 			Visual_Scene scene_x = (Visual_Scene) lbs.scenes.get(x);
 			if( ( scene_x ).id.matches(sceneUrl) )
 			{
 				
 				for(int y = 0; y < scene_x.nodes.size();y++)
 				{
 					Nodec node_y =(Nodec) scene_x.nodes.get(y);
 					//Make sure we have instance geometry in this node
 					if(node_y.instance_geometry.size() != 0)
 					{
 						node = node_y;
 						flag = false;
 					}
 				}
 			}
 		}
 		
 		if(flag || node == null)
 		{
 			System.out.println("could not load object");
 			return;
 		}
 		
 		instanceGeom  = (Instance_Geometry)node.instance_geometry.get(0);
 		igurl = instanceGeom.url.substring(1);
 		
 		flag = true;
 				
 		//get the mesh data from the node that we are drawing
 		for(int x = 0; x < lg.geometry.size() && flag;x++ )
 		{
 			if(((Geometry)lg.geometry.get(x)).id.matches(igurl))
 			{
 				mesh = ((Geometry)lg.geometry.get(x)).mesh;
 				flag = false;
 			}
 		}
 		
 		if(flag || mesh == null)
 		{
 			System.out.println("could not load object");
 			return;
 		}
 		
 		if(mesh.triangles.size() == 0)
 		{
 			System.out.println("object have to be trianglerized");
 			return;
 		}
 		
 		//start loading pmesh information from the mesh object
 		System.out.println("Ready to load");	
 		
 		ArrayList<Input> temp_input = (ArrayList<Input>) ( (Triangles)mesh.triangles.get(0) ).inputs;
 		
 		//find where the vertices are stored and load the data
 		int pos_location = 0;
 		String vertex_source = "";
 		String normal_source = "";
 		
 		//Check the inputs for normals and vertices the vertices must have a "POSITION" specifier
 		for(int input_num = 0; input_num < temp_input.size(); input_num++)
 		{
 			Input input_x = (Input)temp_input.get(input_num);
 			//Must be upper-case
 			//This is guaranteed to exist by Collada 1.5 spec
 			if(input_x.semantic.matches("VERTEX"))
 			{
 				//Remove the leading '#'
 				vertex_source = input_x.source.substring(1);
 				pos_location = input_num;
 			}
 			//WARNING! Normals are not guaranteed by the Collada 1.5 spec
 			if(input_x.semantic.matches("NORMAL"))
 				//Remove the leading '#'
 				normal_source = input_x.source.substring(1);
 		}
 		
 		//Check the vertices' inputs for "POSITION"
 		//This is guaranteed to exist by Collada 1.5 spec 
 		String position_source = "";
 		temp_input = mesh.vertices.input;
 		for(int x = 0; 	
 				x < temp_input.size() && 
 					!( (Input)temp_input.get(x) ).semantic.matches(vertex_source);
 				x++)
 			//Remove the leading '#'
 			position_source = ( (Input)temp_input.get(x) ).source.substring(1);
 		
 		//Get the "POSITION" source
 		//This is guaranteed to exist by Collada 1.5 spec
 		Source source_pos = null;
 		for(	pos_location = 0; 
 				pos_location < mesh.sources.size() &&
 					!(source_pos = (Source)mesh.sources.get(pos_location) ).id.matches(position_source); 
 				pos_location++)
 			;//source_pos is assigned in the header of the for loop
 		
 		Source source_norm = null;
 		if(!normal_source.isEmpty())
 			for(	pos_location = 0; 
 			pos_location < mesh.sources.size() &&
 				!(source_norm = (Source)mesh.sources.get(pos_location) ).id.matches(normal_source); 
 			pos_location++)
 			;//source_norm is assigned in the header of the for loop, if it exists
 		
 		VertCell tempVertCell;		
 		PolyCell curPolyC;
 		
 		Float_Array fVertArray = source_pos.floatArray;
 		int vVertCount = source_pos.accessor.count;
 		int vVertStride = source_pos.accessor.stride;
 		
 		if(source_norm == null)
 			throw new Exception("NORMALS DO NOT EXIST!! FIX CODE TO HANDLE THIS CASE");
 		
 		Float_Array fNormArray = source_norm.floatArray;
 		int vNormCount = source_norm.accessor.count;
 		int vNormStride = source_norm.accessor.stride;
 		int place =0;
 		
 		vertArray = new ArrayList<VertCell>();
 		numVerts = vVertCount;
 		for(int x = 0; x < vVertCount; x++)
 		{
 			tempVertCell = new VertCell();
 			
 			tempVertCell.worldPos.x = fVertArray.getValue(place);
 			tempVertCell.worldPos.y = fVertArray.getValue(place + 1);
 			tempVertCell.worldPos.z = fVertArray.getValue(place + 2);
 			
 			tempVertCell.polys = null;
 			vertArray.add(x, tempVertCell);
 			
 			place += vNormStride;
 		}
 		
 		//Load the normals here!
 		vertNormArray = new Double3D[vNormCount];
 		numNorms = vNormCount;
 		place = 0;
 		for(int x =0; x < vNormCount; x++)
 		{	
 			vertNormArray[x] = new Double3D();
 			vertNormArray[x].x = fNormArray.getValue(place);
 			vertNormArray[x].y = fNormArray.getValue(place + 1);
 			vertNormArray[x].z = fNormArray.getValue(place + 2);
 			
 			place += vVertStride;
 		}
 		
 		//all of the VertCells are loaded 
 		//and normals if they exist
 		
 		numSurf = mesh.triangles.size();
 		
 		//next 5 objects are used to help load from the collada data to the PMesh data
 		
 		SurfCell curSurf = null;
 		VertListCell curVertLC = null;
 		PolyListCell curPolyLC = null;
 		curPolyC = null;
 		
 		//used for keeping track of the offsets offsets
 		Input vertexO = null,
 		normalO = null,
 		textCoordO = null,
 		textTangO = null,
 		textBinO = null;
 		
 		int offset=0;	//the offset of each vertex
 		int cur = 0;	//the current PolyCell being loaded
 		int vertCount = 0;
 		
 		curSurf = new SurfCell("tempCollada");
 		for(int x = 0; x < numSurf; x++)
 		{
 			//We're only loading a single triangle object right now
 			//curPolyC.next = null;
 			if(x == 0)
 				surfHead = curSurf;
 			else
 			{
 				curSurf.next = new SurfCell("tempCollada" + x);
 				curSurf = curSurf.next;		
 			}
 			
 			curTri = (Triangles)mesh.triangles.get(x);
 			curSurf.numPoly = curTri.count;
 			curSurf.material = getMaterial(curTri.material,instanceGeom,collada.material, collada.effects);
 			vertCount = 0;
 			
 			//Check the triangle inputs for this triangle object/Surface and determine the offset for the next semantic items
 			offset = -1;
 			for(int input_num = 0; input_num < curTri.inputs.size(); input_num++)
 			{
 				
 				Input temp = (Input)curTri.inputs.get(input_num);
 				if(temp.offset > offset)
 				{
 					offset = temp.offset;
 					if(temp.semantic.matches("VERTEX"))
 						vertexO = temp;
 					else if(temp.semantic.matches("NORMAL"))
 						normalO = temp;
 					else if(temp.semantic.matches("TEXCOORD"))
 						textCoordO = temp;
 					else if(temp.semantic.matches("TEXTANGENT"))
 						textTangO = temp;
 					else if(temp.semantic.matches("TEXBINORMAL"))
 						textBinO= temp;
 				}
 			}
 
 			int[] vertexLocations = breakInputs(vertexO,curTri.getLocations(),offset,curTri.count);;
 			int[] normalLocations = null;
 			int[] textCoordLocations = null;
 			int[] textTangLocations = null;
 			int[] textBinLocations = null;
 			
 			
 			if(normalO !=null)
 				normalLocations = breakInputs(normalO,curTri.getLocations(),offset,curTri.count);
 			if(textCoordO !=null)
 				textCoordLocations = breakInputs(textCoordO,curTri.getLocations(),offset,curTri.count);
 			if(textTangO !=null)
 				textTangLocations = breakInputs(textTangO,curTri.getLocations(),offset,curTri.count);
 			if(textBinO !=null)
 				textBinLocations = breakInputs(textBinO,curTri.getLocations(),offset,curTri.count);
 			
 			cur = 0;
 			//Build the surface
 			curPolyC = new PolyCell();
 			for(int tri_num = 0 ; tri_num < curTri.count; tri_num++)
 			{	
 				//Set the SurfCell's polyHead to this first PolyCell
 				if(tri_num == 0)
 					surfHead.polyHead = curPolyC;
 				else
 				{
 					curPolyC.next = new PolyCell();
 					curPolyC = curPolyC.next;
 				}
 				
 				curPolyC.next = null;
 				curPolyC.numVerts = 3;
 				curPolyC.parentSurf = curSurf;
 				vertCount += 3;
 				
 				
 				//Triangles have three vertices
 				curVertLC = new VertListCell();
 				curVertLC.vert = vertexLocations[cur];
 				curVertLC.norm = normalLocations[cur];
 				cur++;
 				
 				//Make a new PolyCellList in the vertArray based on the index (tempVertListCell.vert)
 				curPolyLC = new PolyListCell();
 				curPolyLC.poly = curPolyC;
 				addToVertArrayPoly(curVertLC.vert, curPolyLC);
 				
 				//Assign the first vertex to the PolyCell
 				curPolyC.vert = curVertLC;
 				
 				curVertLC.next = new VertListCell();
 				curVertLC = curVertLC.next;
 				curVertLC.vert = vertexLocations[cur];
 				curVertLC.norm = normalLocations[cur];
 				cur++;
 				
 				//Make a new PolyCellList in the vertArray based on the index (tempVertListCell.vert)
 				curPolyLC = new PolyListCell();
 				curPolyLC.poly = curPolyC;
 				addToVertArrayPoly(curVertLC.vert, curPolyLC);
 				
 				curVertLC.next = new VertListCell();
 				curVertLC = curVertLC.next;
 				curVertLC.vert = vertexLocations[cur];
 				curVertLC.norm = normalLocations[cur];
 				cur++;
 				
 				//Vertex three has no next
 				curVertLC.next = null;
 				
 				//Make a new PolyCellList in the vertArray based on the index (tempVertListCell.vert)
 				curPolyLC = new PolyListCell();
 				curPolyLC.poly = curPolyC;
 				addToVertArrayPoly(curVertLC.vert, curPolyLC);
 			}//for # triangle count
 			
 			curSurf.numVerts = vertCount;
 			float vertices [] = new float[vertCount*3];
 			int vInd = 0;
 			float normals [] = new float[vertCount*3];
 			float texCoords [] = new float[vertCount*3];
 			int nInd = 0;
 			curPolyC=curSurf.polyHead;
 			while(curPolyC != null){
 				curVertLC = curPolyC.vert;
 				while(curVertLC != null){
 				//for(int i = 0; i < curPoly.numVerts; i++);{
 					VertCell curVert = vertArray.get(curVertLC.vert);
 					vertices[vInd++] = (float)curVert.worldPos.x;
 					vertices[vInd++] = (float)curVert.worldPos.y;
 					vertices[vInd++] = (float)curVert.worldPos.z;
 					if(normalO != null)
 					{
 						normals[nInd++]= (float)vertNormArray[curVertLC.vert].x;
 						normals[nInd++]= (float)vertNormArray[curVertLC.vert].y;
 						normals[nInd++]= (float)vertNormArray[curVertLC.vert].z;
 					}
 					curVertLC = curVertLC.next;
 				}
 				curPolyC = curPolyC.next;
 			}
 			
 			// now put vertices and normals into VertexArray or Buffer
 			curSurf.vertexBuffer = Buffers.newDirectFloatBuffer(vertices.length);
 			curSurf.vertexBuffer.put(vertices);
 			curSurf.vertexBuffer.rewind();
 			
 			curSurf.normalBuffer =  Buffers.newDirectFloatBuffer(normals.length);
 			curSurf.normalBuffer.put(normals);
 			curSurf.normalBuffer.rewind();	
 		}//for surf
 		
 		this.calcPolyNorms();
 		this.calcVertNorms();
		this.calcBoundingSphere();
 	}
 	
 	public int getMaterial(String name,Instance_Geometry instanceGeom,Library_Materials lMaterials, Library_Effects lEffects)
 	{
 		
 		ArrayList<Instance_Material> bindings = instanceGeom.instance_materials;
 		String target=null;
 		String effect;
 		
 		for(int x=0;x<bindings.size();x++)
 			if(bindings.get(x).symbol.matches(name))
 			{
 				target = bindings.get(x).target.substring(1);
 				break;
 			}
 		
 		effect = lMaterials.getEffect(target);
 		
 		
 		for(int x=0;x<materials.length;x++)
 			if(materials[x].materialName.matches(effect.substring(1)))
 				return x;
 		
 		return 0;
 	}
 	
 	public Double3D[] convertToDouble3DArray(ArrayList<Double3D> list)
 	{
 		Double3D[] array = new Double3D[list.size()];
 		
 		for(int x=0;x<array.length;x++)
 			array[x] = list.get(x);
 		
 		return array;
 	}
 	
 	public void addToVertArrayPoly(int location, PolyListCell cell)
 	{
 		//Get the right Vertex based on it's index
 		VertCell vc = (VertCell)vertArray.get(location);
 		PolyListCell cur;
 		
 		//If it's the first assign it as the first
 		if(vc.polys == null)
 			vc.polys = cell;
 		else //Otherwise, find it's position
 		{	
 			cur = vc.polys;
 			for(;cur.next != null; cur = cur.next)
 				;
 			cur.next = cell;
 		}
 	}
 	
 	public void printTest()
 	{
 					
 		SurfCell curSurf = surfHead;
 		PolyCell curPoly;
 		VertListCell curVert;
 		
 		while(curSurf != null)
 		{
 			curPoly = curSurf.polyHead;
 			
 			while(curPoly != null)
 			{
 				curVert = curPoly.vert;
 				
 				while(curVert != null)
 				{
 					System.out.print(curVert.vert + " ");
 					curVert = curVert.next;
 				}
 				
 				System.out.println();
 				curPoly = curPoly.next;
 			}
 			
 			curSurf = curSurf.next;
 		}
 	}
 	
 	public Double3D[] getValues(int[] locations, String source, ArrayList<Source> sources)
 	{
 		
 		boolean flag = true;
 		Double3D[] out = new Double3D[locations.length];
 		Source tempSource=null;
 		Float_Array tempArray;
 		int stride;
 		
 		for(int x=0;x<sources.size() && flag;x++)
 		{
 			tempSource = (Source)sources.get(x);
 			
 			if(tempSource.id.matches(source))
 				flag = false;
 		}
 		
 		tempArray = tempSource.floatArray;
 		stride = tempSource.accessor.stride;
 		
 		for(int x=0; x<out.length;x++)
 		{
 			out[x] = new Double3D();
 			out[x].x = tempArray.getValue(locations[x]*stride);
 			out[x].y = tempArray.getValue(locations[x]*stride+1);
 			out[x].z = tempArray.getValue(locations[x]*stride+2);
 			
 		}
 		
 		return out;
 	}
 	
 	public int[] breakInputs(Input input, int[] locations, int offset, int count)
 	{
 		int c = (locations.length/(offset+1));
 		int[] out = new int[c];
 		int next = input.offset;
 		
 		for(int x=0;x<c;x++)
 		{
 			out[x] = locations[next];
 			next+=offset+1;
 		}
 		
 		return out;
 	}
 	
 	public void loadEffects(Library_Effects effects)
 	{
 		Effect tempEffect;
 		MaterialCell tempMat;
 		
 		numMats=effects.effects.size();
 		materials = new MaterialCell[numMats];
 		
 		for(int x=0; x<numMats;x++)
 		{
 			tempEffect = (Effect)effects.effects.get(x);
 			tempMat = new MaterialCell();
 			
 			tempMat.materialName = tempEffect.id;
 			tempMat.ka = new DoubleColor(tempEffect.ambient[0], tempEffect.ambient[1], tempEffect.ambient[2], 1.0);
 			tempMat.kd = new DoubleColor(tempEffect.diffuse[0], tempEffect.diffuse[1], tempEffect.diffuse[2], 1.0);
 			tempMat.ks =  new DoubleColor(tempEffect.specular[0], tempEffect.specular[1], tempEffect.specular[2], 1.0);
 			tempMat.emmColor = new DoubleColor(tempEffect.emission[0], tempEffect.emission[1], tempEffect.emission[2], 1.0);
 			tempMat.refractivity.r = tempEffect.transparency;
 			tempMat.shiny = tempEffect.shininess;
 			tempMat.lineColor = new DoubleColor(tempEffect.diffuse[0], tempEffect.diffuse[1], tempEffect.diffuse[2], 1.0);
 			tempMat.doubleSided = false;
 						
 			this.materials[x]=tempMat;
 			
 		}
 	}
 }
