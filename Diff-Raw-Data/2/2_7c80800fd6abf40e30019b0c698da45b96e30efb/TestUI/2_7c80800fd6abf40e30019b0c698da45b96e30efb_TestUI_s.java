 package initial3d.test;
 
 import java.io.FileInputStream;
 
 import common.entity.MovableEntity;
 import common.entity.Player;
 import comp261.modelview.MeshLoader;
 
 import initial3d.engine.*;
 
 public class TestUI {
 
 	public static void main(String[] args) throws Throwable {
 		
 		RenderWindow rwin = RenderWindow.create(848, 480);
 		rwin.setLocationRelativeTo(null);
 		rwin.setVisible(true);
 		
 		SceneManager sman = new SceneManager(848, 480);
 		sman.setDisplayTarget(rwin);
 		rwin.addKeyListener(sman);
 		rwin.addCanvasMouseListener(sman);
 		rwin.addMouseWheelListener(sman);
 		
 		Scene scene = new Scene();
 		sman.attachToScene(scene);
 		
 		// stuff
 		
		MovableEntity ball = new Player(Vec3.zero);
 		
 		FileInputStream fis = new FileInputStream("ball.txt");
 		ball.setMeshContexts(MeshLoader.loadComp261(fis));
 		fis.close();
 		
 		MeshContext mc = ball.getMeshContexts().get(0);
 		mc.requestInputEnabled(true);
 		mc.requestFocus();
 		
 		scene.addDrawable(mc);
 		
 		MovableReferenceFrame camera_rf = new MovableReferenceFrame(null);
 		scene.getCamera().trackReferenceFrame(camera_rf);
 		camera_rf.setPosition(Vec3.create(-3, 3, -3));
 		camera_rf.setOrientation(Quat.create(Math.PI / 16, Vec3.i));
 		
 		camera_rf.setOrientation(camera_rf.getOrientation().mul(Quat.create(Math.PI / 4, Vec3.j)));
 		
 		ball.updateMotion(Vec3.create(0, 0.5, 0), Vec3.create(0.5, 0, 0.2), Quat.one, Vec3.zero, System.currentTimeMillis());
 		
 	}
 	
 }
