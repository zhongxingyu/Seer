 package net.todd.games.balls.client.ui.j3d;
 
 import java.awt.BorderLayout;
 import java.awt.GraphicsConfiguration;
 
 import javax.media.j3d.AmbientLight;
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.Bounds;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.DirectionalLight;
 import javax.media.j3d.GeometryArray;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.LineAttributes;
 import javax.media.j3d.Material;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.swing.JPanel;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3d;
 import javax.vecmath.Vector3f;
 
 import net.todd.games.balls.client.ui.IMainView;
 import net.todd.games.balls.client.ui.MainView;
 import net.todd.games.balls.common.Ball;
 
 import com.sun.j3d.utils.geometry.Sphere;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.ViewingPlatform;
 
 public class MainViewIn3D extends MainView implements IMainView {
 	private Bounds bounds;
 
 	@Override
 	public JPanel createMainPanel() {
 		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
 		Canvas3D canvas3D = new Canvas3D(config);
 		canvas3D.setFocusable(true);
 		canvas3D.requestFocus();
 
 		SimpleUniverse su = new SimpleUniverse(canvas3D);
 		su.getViewingPlatform().setNominalViewingTransform();
 		su.getViewer().getView().setMinimumFrameCycleTime(5);
 
 		bounds = new BoundingSphere(new Point3d(0, 0, 0), 100);
 
 		su.addBranchGraph(createScene());
 
 		createCamera(su);
 
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(canvas3D, BorderLayout.CENTER);
 
 		return mainPanel;
 	}
 
 	private BranchGroup createScene() {
 		BranchGroup bg = new BranchGroup();
 
 		lightScene(bg);
 		createGameGrid(bg);
 		createBall(bg);
 
 		return bg;
 	}
 
 	private void createGameGrid(BranchGroup bg) {
 		Shape3D grid = new Shape3D();
 
		Point3f[] points = new Point3f[80];
 		int index = 0;
		for (int i = -10; i < 10; i++) {
 			float zPos = i;
 			points[index * 2] = new Point3f(-10.0f, 0.0f, zPos);
 			points[index * 2 + 1] = new Point3f(10.0f, 0.0f, zPos);
 			index++;
 		}
 
		index = 20;
		for (int i = -10; i < 10; i++) {
 			float xPos = i;
 			points[index * 2] = new Point3f(xPos, 0.0f, -10.0f);
 			points[index * 2 + 1] = new Point3f(xPos, 0.0f, 10.0f);
 			index++;
 		}
 
 		LineArray gridGeometry = new LineArray(points.length, GeometryArray.COORDINATES
 		        | GeometryArray.COLOR_3);
 		gridGeometry.setCoordinates(0, points);
 
 		for (int i = 0; i < points.length; i++) {
 			gridGeometry.setColor(i, new Color3f(1.0f, 1.0f, 1.0f));
 		}
 
 		Appearance gridAppearance = new Appearance();
 		LineAttributes lineAttributes = new LineAttributes();
 		lineAttributes.setLineWidth(1.0f);
 		gridAppearance.setLineAttributes(lineAttributes);
 
 		grid.setGeometry(gridGeometry);
 		grid.setAppearance(gridAppearance);
 
 		bg.addChild(grid);
 	}
 
 	private void createCamera(SimpleUniverse su) {
 		ViewingPlatform vp = su.getViewingPlatform();
 		TransformGroup tg = vp.getViewPlatformTransform();
 		Transform3D t3d = new Transform3D();
 		tg.getTransform(t3d);
		t3d.lookAt(new Point3d(10.0, 10.0, 10.0), new Point3d(0.0, 0.0, 0.0),
 		        new Vector3d(0, 1, 0));
 		t3d.invert();
 		tg.setTransform(t3d);
 	}
 
 	private void createBall(BranchGroup bg) {
 		Color3f blue = new Color3f(0.0f, 0.0f, 0.0f);
 		Color3f black = new Color3f(0.3f, 0.3f, 0.8f);
 		Color3f specular = new Color3f(0.9f, 0.9f, 0.9f);
 
 		Material blueMat = new Material(blue, black, blue, specular, 25.0f);
 		Appearance blueApp = new Appearance();
 		blueApp.setMaterial(blueMat);
 
 		Transform3D t3d = new Transform3D();
 		t3d.set(new Vector3f(0, 0, 0));
 		TransformGroup tg = new TransformGroup(t3d);
 		tg.addChild(new Sphere(0.5f, 1, 50, blueApp));
 
 		bg.addChild(tg);
 	}
 
 	private void lightScene(BranchGroup bg) {
 		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
 		AmbientLight ambient = new AmbientLight(white);
 		ambient.setInfluencingBounds(bounds);
 		bg.addChild(ambient);
 
 		Vector3f direction1 = new Vector3f(-1.0f, -1.0f, -1.0f);
 		DirectionalLight direct1 = new DirectionalLight(white, direction1);
 		direct1.setInfluencingBounds(bounds);
 		bg.addChild(direct1);
 	}
 
 	public void setBallPositions(Ball[] ballData) {
 	}
 }
