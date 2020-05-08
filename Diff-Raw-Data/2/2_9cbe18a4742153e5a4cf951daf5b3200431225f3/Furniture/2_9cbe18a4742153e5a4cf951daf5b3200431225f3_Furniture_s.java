 package poppio.cg;
 // for store data of furnitures
 public class Furniture extends roomObject{
 	
 	private String path;
 	private WavefrontObjectLoader_VertexBufferObject obj;
 	float colorR,colorG,colorB;
 	
 	public Furniture (int id, int furnitureCount, int objectID) {
 		super(id); 
 		
 		
 		if(objectID == 1){
 			// bin
 			this.name = "bin"+furnitureCount;
 			this.path = "obj/bin.obj";
 			this.colorR = new Float(0.5);
 			this.colorG = new Float(0.445);
 			this.colorB = new Float(0.447);
 			this.coorZ = -7f;
 		}else if(objectID == 2){
 			// table
 			this.name = "table"+furnitureCount;
 			this.path = "obj/table.obj";
 			this.colorR = new Float(0.633);
 			this.colorG = new Float(0.423);
 			this.colorB = new Float(0.240);
 			this.coorZ = -7f;
 		}else if(objectID == 3){
 			// table
 			this.name = "chair"+furnitureCount;
 			this.path = "obj/chair.obj";
 			this.colorR = new Float(0.633);
 			this.colorG = new Float(0.423);
 			this.colorB = new Float(0.240);
			this.coorZ = -7f;
 		}else if(objectID == 4){
 			// cuble
 			this.name = "cube"+furnitureCount;
 			this.path = "obj/cube.obj";
 			this.colorR = new Float(1);
 			this.colorG = new Float(0);
 			this.colorB = new Float(0.750);
 			this.coorZ = -8.5f;
 		}else if(objectID == 5){
 			// table
 			this.name = "sphere"+furnitureCount;
 			this.path = "obj/sphere_tex.obj";
 			this.colorR = new Float(0);
 			this.colorG = new Float(0.667);
 			this.colorB = new Float(1);
 			this.coorZ = -9.2f;
 		}else if(objectID == 6){
 			// table
 			this.name = "urn"+furnitureCount;
 			this.path = "obj/urn.obj";
 			this.colorR = new Float(0.962);
 			this.colorG = new Float(0.860);
 			this.colorB = new Float(0.317);
 			this.coorZ = -8.5f;
 		}else if(objectID == 7){
 			// table
 			this.name = "shelf"+furnitureCount;
 			this.path = "obj/shelf.obj";
 			this.colorR = new Float(0.633);
 			this.colorG = new Float(0.423);
 			this.colorB = new Float(0.240);
 			this.coorZ = -8.7f;
 		}else if(objectID == 8){
 			// table
 			this.name = "wardrobe"+furnitureCount;
 			this.path = "obj/wardrobe.obj";
 			this.colorR = new Float(0.633);
 			this.colorG = new Float(0.423);
 			this.colorB = new Float(0.240);
 			this.coorZ = -3.4f;
 		}else{ // gonna add more
 			// wtf
 		}
 		this.obj = new WavefrontObjectLoader_VertexBufferObject(path);
 	}
 	
 	public WavefrontObjectLoader_VertexBufferObject getObjectLoader() {
 		return this.obj;
 	}
 	
 	
 }
