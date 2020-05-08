 package net.todd.games.boardgame;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.GraphicsConfiguration;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.media.j3d.AmbientLight;
 import javax.media.j3d.Background;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.DirectionalLight;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.media.j3d.View;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 import javax.vecmath.Vector3f;
 
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.ViewingPlatform;
 
 public class GameView {
 	private static final Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
 
 	private final BoundingSphere bounds;
 
 	public GameView() {
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
 
 		JFrame frame = new JFrame("Board Game");
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 		Container c = new Container();
 		c.setLayout(new BorderLayout());
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(canvas3D, BorderLayout.CENTER);
 		c.add(mainPanel, BorderLayout.CENTER);
 		frame.getContentPane().add(c);
 		frame.setSize(1000, 800);
 		frame.setVisible(true);
 	}
 
 	private BranchGroup createScene() {
 		BranchGroup bg = new BranchGroup();
 
 		lightScene(bg);
 		createGameGrid(bg);
 		createBackground(bg);
 		bg.compile();
 
 		return bg;
 	}
 
 	private void createBackground(BranchGroup bg) {
 		Background background = new Background();
 		background.setApplicationBounds(bounds);
 		background.setColor(0.50f, 0.50f, 0.50f);
 		bg.addChild(background);
 	}
 
 	private void createGameGrid(BranchGroup bg) {
 		CheckeredBoard board = new CheckeredBoard();
 		bg.addChild(board.getBG());
 	}
 
 	private void lightScene(BranchGroup bg) {
 		AmbientLight ambient = new AmbientLight(white);
 		ambient.setInfluencingBounds(bounds);
 		bg.addChild(ambient);
 
 		Vector3f direction1 = new Vector3f(-1.0f, -1.0f, -1.0f);
 		DirectionalLight direct1 = new DirectionalLight(white, direction1);
 		direct1.setInfluencingBounds(bounds);
 		bg.addChild(direct1);
 	}
 
 	private void createCamera(SimpleUniverse su) {
 		ViewingPlatform vp = su.getViewingPlatform();
 		View view = su.getViewer().getView();
 		view.setBackClipDistance(100);
 
 		TransformGroup tg = vp.getViewPlatformTransform();
 		Transform3D t3d = new Transform3D();
 		tg.getTransform(t3d);
		t3d.lookAt(new Point3d(0, 50.0, -100.0), new Point3d(0.0, 0.0, 0.0), new Vector3d(0, 1, 0));
 		t3d.invert();
 		tg.setTransform(t3d);
 	}
 }
