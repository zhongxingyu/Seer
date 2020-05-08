 package client;
 
 import initial3d.engine.*;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 import common.entity.*;
 import common.map.Segment;
 import comp261.modelview.MeshLoader;
 
 public class Game {
 	private static Game instance = null;
 	public static Game getInstance()
 	{
 		if(instance == null)
 			instance = new Game();
 		
 		return instance;
 	}
 	
 	private double trackDistance = 100;
 	private double existDistance = 400;
 	
 	private Map<Long, MovableEntity> movableEntities = new HashMap<Long, MovableEntity>();	
 	private Map<Long, Entity> staticEntities = new HashMap<Long, Entity>();	
 	
 	private Map<Long, Segment> terrain = new HashMap<Long, Segment>();	
 	
 	private Scene world;
 	private MovableEntity player = null;
 	
 	public Game(){
 		instance = this;
 	}
 	
 	
 	public void loadScene(Scene _world){
 		world = _world;
 		world.getCamera().trackReferenceFrame(player);
 		for(MovableEntity me : movableEntities.values()){
 			//add mesh
 		}
 		for(Entity m : staticEntities.values()){
 			//add mesh
 		}
 		for(Segment s : terrain.values()){
 			//add mesh
 		}
 		
 		// TODO remove
 		MovableEntity ball = new Player(Vec3.zero);
 		FileInputStream fis;
 		try {
 			fis = new FileInputStream("ball.txt");
 			ball.setMeshContexts(MeshLoader.loadComp261(fis));
 			fis.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		for (Drawable d : ball.getMeshContexts()) {
 			world.addDrawable(d);
 		}
 		
 		MovableReferenceFrame camera_rf = new MovableReferenceFrame(ball);
 		world.getCamera().trackReferenceFrame(camera_rf);
 		camera_rf.setPosition(Vec3.create(-10, 10, -10));
 		camera_rf.setOrientation(Quat.create(Math.PI / 8, Vec3.i));
 		
 		camera_rf.setOrientation(camera_rf.getOrientation().mul(Quat.create(Math.PI / 4, Vec3.j)));
 		
 		ball.updateMotion(Vec3.create(0, 0.5, 0), Vec3.create(0.5, 0, 0.5), Quat.one, Vec3.zero, System.currentTimeMillis());
 		
 	}
 	
 	public Scene getScene(){
 		return world;
 	}
 	
 	public void enterWorld(int worldID)
 	{
 		System.out.printf("Entering world %d\n", worldID);
 	}
 
 	public boolean setPlayer(long eid){
 		player = movableEntities.get(eid);
		System.out.println(world);
 		world.getCamera().trackReferenceFrame(player);
 		return player!=null;
 	}
 	
 	public void entityMoved(Long eid, Vec3 pos, Vec3 linVel, Quat ori, Vec3 angVel, Long time){
 		MovableEntity e = movableEntities.get(eid);
 		if(e!=null ){//TODO fix the time signiture 
 			e.updateMotion(pos, linVel, ori, angVel, time);
 		}
 	}
 	
 	//TODO wtf is this method going to do?
 	public void entityStartTracking(long eid){ 
 		//System.out.println("(OTHER)BEN GET THE FUCK HERE");
 	}
 	
 	public void entityStopTracking(long eid){
 		//System.out.println("(OTHER)BEN GET THE FUCK HERE");
 	}
 	
 	public void addMoveableEntity(MovableEntity jim){
 		//add mesh
 		movableEntities.put(jim.id, jim);
 	}
 	
 	public void addStaticEntity(Entity jim){
 		//add mesh
 		staticEntities.put(jim.id, jim);
 	}
 	
 	public void addTerrain(Segment tim){
 		//add mesh
 		terrain.put(tim.id, tim);
 		MeshContext mc = tim.getMeshContext();
 		this.world.addDrawable(mc);
 		
 		
 	}
 }
