 package game.states;
 
 import java.awt.event.KeyEvent;
 
 import initial3d.engine.*;
 import initial3d.renderer.Util;
 import game.Game;
 import game.GameState;
 import game.bound.BoundingSphere;
 import game.entity.Entity;
 import game.entity.MoveableEntity;
 import game.entity.PlayerEntity;
 import game.entity.WallEntity;
 import game.floor.Floor;
 import game.floor.FloorGenerator;
 import game.net.packets.MovementPacket;
 
 /***
  * The game!
  * 
  * @author Ben
  * 
  */
 public class PlayState extends GameState {
 	public PlayState(Game parent) {
 		super(parent);
 	}
 
 	MovableReferenceFrame cameraRf = null;
 	private boolean mouseLock = false;
 	
 	private boolean transmittedStop = false;
 
 	private double cam_pitch = 0;
 	private double player_yaw = 0;
 	
 	private Floor floor;
 
 	@Override
 	public void initalise() {
 		game.player.addToScene(scene);
 		for(int i = 0; i < game.players.length; i++)
 		{
 			if(game.players[i] == game.player)
 				continue;
 			
 			System.out.printf("Adding %d to scene.. %s\n", i, game.players[i]);
 			game.players[i].addToScene(scene);
 		}
 
 		FloorGenerator fg = new FloorGenerator(123873123312l);
 		floor = fg.getFloor(2);
 		floor.addToScene(scene);
 		
 //		for(WallEntity we : floor.getWalls())
 //		{
 //			we.addToScene(scene);
 //		}
 //
 //		MeshLOD mlod = new MeshLOD(1, 5, 5, 5, 5, 5);
 //		mlod.addVertex(-0.5, 0, -0.5);
 //		mlod.addVertex(-0.5, 0, 0.5);
 //		mlod.addVertex(0.5, 0, -0.5);
 //		mlod.addVertex(0.5, 0, 0.5);
 //		mlod.addPolygon(new int[] { 1, 2, 4, 3 }, null, null, null);
 //		Mesh floorMesh = new Mesh();
 //		floorMesh.add(mlod);
 //
 //		Material mat = new Material(Color.BLACK, new Color(0.1f, 0.1f, 0.1f), new Color(0.3f, 0.3f, 0.3f), new Color(
 //				0f, 0f, 0f), 1f, 1f);
 //		MovableReferenceFrame floorRf = new MovableReferenceFrame(ReferenceFrame.SCENE_ROOT);
 //		floorRf.setPosition(Vec3.create(5, -0.5, 5));
 //		MeshContext mc = new MeshContext(floorMesh, mat, floorRf);
 //		mc.setScale(10);
 //		scene.addDrawable(mc);
 
 		MovableReferenceFrame cameraRf = new MovableReferenceFrame(game.player);
 		scene.getCamera().trackReferenceFrame(cameraRf);
 		cameraRf.setPosition(Vec3.create(0, 0.3, -0.5));
 //		cameraRf.setOrientation(Quat.create(Math.PI / 3.6f, Vec3.i));
 		
 		game.getWindow().setMouseCapture(true);
 		
 		scene.setAmbient(new Color(0.2f, 0.2f, 0.2f));
 		scene.setFogColor(Color.GRAY);
 		scene.setFogParams(255f * 1.5f, 1024f * 1.5f);
 		scene.setFogEnabled(true);
 		
 		Light l = new Light.DirectionalLight(ReferenceFrame.SCENE_ROOT, Color.WHITE, Vec3.create(0, 1, 1));
 		scene.addLight(l);
 		
 		Light l2 = new Light.SphericalPointLight(game.player, Color.GREEN, 0.5f);
 		scene.addLight(l2);
 	}
 
 	@Override
 	public void update(double delta) {
 		RenderWindow rwin = game.getWindow();
 
 		double speed = 1;
 
 		int mx = rwin.pollMouseTravelX();
 		int my = rwin.pollMouseTravelY();
 
 		// 200px == pi / 4 ??
 
 		// these are confusing names, aren't they?
 		double rotx = mx / 800d * Math.PI;
 		double roty = my / 800d * Math.PI;
 
 		cam_pitch = Util.clamp(cam_pitch + roty, -Math.PI * 0.499, Math.PI * 0.499);
 		player_yaw -= rotx;
 
 		Camera cam = scene.getCamera();
 
 		MovableReferenceFrame rf = (MovableReferenceFrame) cam.getTrackedReferenceFrame();
 		rf.setOrientation(Quat.create(cam_pitch, Vec3.i));
 		
 		Vec3 cnorm = cam.getNormal().flattenY().unit();
 		Vec3 cup = Vec3.j;
 		Vec3 cside = Vec3.j.cross(cnorm);
 
 		Vec3 v = Vec3.zero;
 
 		if (rwin.getKey(KeyEvent.VK_W)) {
 			v = v.add(cnorm);
 		}
 		if (rwin.getKey(KeyEvent.VK_S)) {
 			v = v.add(cnorm.neg());
 		}
 		if (rwin.getKey(KeyEvent.VK_A)) {
 			v = v.add(cside);
 		}
 		if (rwin.getKey(KeyEvent.VK_D)) {
 			v = v.add(cside.neg());
 		}
 
 		if (v.mag() > 0.0001) {
 			v = v.unit().scale(speed);
 		}
 		
 		//TODO also needs to be changed
 		game.player.updateMotion(game.player.getPosition(), v, Quat.create(player_yaw, Vec3.j), Vec3.zero, System.currentTimeMillis());
 		
 		//other Ben's doing...
 		if(!v.equals(Vec3.zero))
 		{
 			game.transmitPlayerPosition();
 		}
 		else if(!transmittedStop)
 		{
 			game.transmitPlayerPosition();
 			transmittedStop = true;
 		}
 		
 //		for(WallEntity w : floor.getWalls()){
 //			if(w.getBound().intersects(game.player.getBound())){
 //				System.out.println("denied");
 //				game.player.setVelocity(Vec3.zero);
 //				break;
 //			}
 //		}
 	}
 
 	@Override
 	public void destroy() {
 	}
 
 	private void setFirstPerson(boolean _val) {
 		MovableReferenceFrame cameraRf = (MovableReferenceFrame) scene.getCamera().getTrackedReferenceFrame();
 		if (_val) {
 			cameraRf.setPosition(Vec3.create(0, 0.5, 0));
 			cameraRf.setOrientation(game.player.getOrientation());
 		} else {
 			cameraRf.setPosition(Vec3.create(0, 9, -10));
 			cameraRf.setOrientation(Quat.create(Math.PI / 3.6f, Vec3.i));
 		}
 	}
 	
 	private void scroll(double val){
 		if ((val < 0 && cameraRf.getPosition().mag() < 1) || 
 			(val > 0 && cameraRf.getPosition().mag() > 20) || 
 			(val==0)){
 			return;
 		}
 		
 		cameraRf.setPosition(cameraRf.getPosition().scale(val));
 	}
 }
