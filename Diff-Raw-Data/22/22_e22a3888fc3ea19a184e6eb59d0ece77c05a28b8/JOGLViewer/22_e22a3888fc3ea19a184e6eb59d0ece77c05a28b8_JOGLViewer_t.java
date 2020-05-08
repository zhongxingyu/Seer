 package de.tum.in.cindy3dplugin.jogl;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.EventQueue;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.Hashtable;
 import java.util.logging.Level;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 import javax.swing.JFrame;
 
 import org.apache.commons.math.geometry.Vector3D;
 
 import de.tum.in.cindy3dplugin.AppearanceState;
 import de.tum.in.cindy3dplugin.Cindy3DViewer;
 import de.tum.in.cindy3dplugin.LightModificationInfo;
 import de.tum.in.cindy3dplugin.jogl.RenderHints.RenderMode;
 import de.tum.in.cindy3dplugin.jogl.lighting.LightManager;
 import de.tum.in.cindy3dplugin.jogl.primitives.Circle;
 import de.tum.in.cindy3dplugin.jogl.primitives.Line;
 import de.tum.in.cindy3dplugin.jogl.primitives.Line.LineType;
 import de.tum.in.cindy3dplugin.jogl.primitives.renderers.fixedfunc.FixedfuncPrimitiveRendererFactory;
 import de.tum.in.cindy3dplugin.jogl.primitives.renderers.shader.ShaderPrimitiveRendererFactory;
 import de.tum.in.cindy3dplugin.jogl.primitives.Mesh;
 import de.tum.in.cindy3dplugin.jogl.primitives.Sphere;
 import de.tum.in.cindy3dplugin.jogl.primitives.Polygon;
 import de.tum.in.cindy3dplugin.jogl.primitives.Scene;
 import de.tum.in.cindy3dplugin.jogl.renderers.DefaultRenderer;
 import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderer;
 import de.tum.in.cindy3dplugin.jogl.renderers.SupersampledRenderer;
 
 /**
 * JOGL implementation of {@link Cindy3DViewer}.
  */
 public class JOGLViewer extends MouseAdapter implements Cindy3DViewer,
 		MouseMotionListener, MouseWheelListener {
 	private static final double POINT_SCALE = 0.05;
 	
 	private boolean standalone;
 	private Container container;
 	private GLCanvas canvas = null;
 	
 	private JOGLRenderer renderer = null;
 
 	private Scene scene = new Scene();
 	private ModelViewerCamera camera = new ModelViewerCamera();
 	private LightManager lightManager = new LightManager();
 	
 	private double[] mousePosition = new double[2];
 	
 	private boolean drawPending = false;
 
 	private final RenderHints[] qualityHints = new RenderHints[] {
 		new RenderHints(RenderMode.FIXED_FUNCTION_PIPELINE, 1, 1),
 		new RenderHints(RenderMode.FIXED_FUNCTION_PIPELINE, 2, 1),
 		new RenderHints(RenderMode.FIXED_FUNCTION_PIPELINE, 4, 1),
 		new RenderHints(RenderMode.FIXED_FUNCTION_PIPELINE, 8, 1),
 		new RenderHints(RenderMode.PROGRAMMABLE_PIPELINE, 1, 1),
 		new RenderHints(RenderMode.PROGRAMMABLE_PIPELINE, 2, 1),
 		new RenderHints(RenderMode.PROGRAMMABLE_PIPELINE, 3, 1),
 		new RenderHints(RenderMode.PROGRAMMABLE_PIPELINE, 4, 1),
 		new RenderHints(RenderMode.PROGRAMMABLE_PIPELINE, 8, 1),
 	};
 	
 	private RenderHints renderHints = null;
 	private RenderHints requestedRenderHints = null; 
 	
 	public JOGLViewer() {
 		this(null);
 	}
 
 	public JOGLViewer(Container container) {
 		Util.initLogger();
 		Util.setupGluegenClassLoading();
 		
 		if (container == null) {
 			standalone = true;
 			Util.getLogger().info("Creating standalone frame");
 			JFrame frame = new JFrame("Cindy3D (JOGL)");
 			frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 			frame.setLayout(new BorderLayout());
 			frame.setSize(640, 480);
 			this.container = frame;
 		} else {
 			standalone = false;
 			this.container = container;
 		}
 
 		camera.lookAt(new Vector3D(0, 0, 5), Vector3D.ZERO, Vector3D.PLUS_J);
 
 		try {
 			Util.getLogger().info("Trying to call GLProfile.initSingleton");
 			GLProfile.initSingleton(true);
 		} catch (Exception e) {
 			Util.getLogger().log(Level.SEVERE, e.toString(), e);
 		}
 		
 		applyHints(qualityHints[0]);
 	}
 
 	private void applyHints(RenderHints hints) {
 		Util.getLogger().info("Creating renderer");
 		
 		if (renderHints != null && renderHints.equals(hints)) {
 			return;
 		}
 		
 		renderHints = hints;
 		
 		try {
 			GLProfile profile = GLProfile.getDefault();
 			GLCapabilities caps = new GLCapabilities(profile);
 			
 			if (hints.getRenderMode() == RenderMode.FIXED_FUNCTION_PIPELINE) {
 				if (hints.getSamplingRate() > 1) {
 					caps.setSampleBuffers(true);
 					caps.setNumSamples(hints.getSamplingRate());
 				}
 				renderer = new DefaultRenderer(hints, scene, camera,
 						lightManager, new FixedfuncPrimitiveRendererFactory());
 			} else {
 				if (hints.getSamplingRate() == 1) {
 					renderer = new DefaultRenderer(hints, scene, camera,
 							lightManager, new ShaderPrimitiveRendererFactory());
 				} else {
 					renderer = new SupersampledRenderer(hints, scene, camera,
 							lightManager, new ShaderPrimitiveRendererFactory());
 				}
 			}
 			
 			Util.getLogger().info("GLProfile: " + profile);
 			Util.getLogger().info("GLCapabilities: " + caps);
 			
 			if (canvas != null) {
 				canvas.destroy();
 				this.container.remove(canvas);
 			}
 			
 			Util.getLogger().info("Renderer: " + renderer);
 			
 			Util.getLogger().info("Creating canvas");
 			
 			canvas = new GLCanvas(caps);
 			canvas.addGLEventListener(renderer);
 			canvas.addMouseListener(this);
 			canvas.addMouseMotionListener(this);
 			canvas.addMouseWheelListener(this);
 			canvas.setSize(this.container.getSize());
 			
 			Util.getLogger().info("Canvas: " + canvas.getClass().getCanonicalName());
 			
 			this.container.add(canvas, BorderLayout.CENTER);
 		} catch (Exception e) {
 			Util.getLogger().log(Level.SEVERE, e.toString(), e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#begin()
 	 */
 	@Override
 	public void begin() {
 		Util.getLogger().info("begin()");
 		
 		if (requestedRenderHints != null) {
 			applyHints(requestedRenderHints);
 		}
 		scene.clear();
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#end()
 	 */
 	@Override
 	public void end() {
 		Util.getLogger().info("end()");
 
 		try {
 			Util.getLogger().info("Setting container visible");
 			if (!container.isVisible()) {
 				container.setVisible(true);
 			}
 			Util.getLogger().info("Calling canvas.display()");
 			canvas.display();
 		} catch (Exception e) {
 			Util.getLogger().log(Level.SEVERE, e.toString(), e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#shutdown()
 	 */
 	@Override
 	public void shutdown() {
 		Util.getLogger().info("shutdown()");
 		if (standalone && container instanceof JFrame) {
 			((JFrame)container).dispose();
 		}
 		container = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#setBackgroundColor(java.awt.Color)
 	 */
 	@Override
 	public void setBackgroundColor(Color color) {
 		scene.setBackgroundColor(color);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addPoint(double, double, double, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addPoint(double x, double y, double z,
 			AppearanceState appearance) {
 //		Util.getLogger().info("addPoint(" + x + "," + y + "," + z + ")");
 		scene.addSphere(new Sphere(x, y, z, appearance.getSize() * POINT_SCALE,
 				new Material(appearance.getColor(), appearance.getShininess(),
 						1)));
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addCircle(double, double, double, double, double, double, double, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addCircle(double cx, double cy, double cz, double nx,
 			double ny, double nz, double radius, AppearanceState appearance) {
 //		Util.getLogger().info("addCircle(" + cx + "," + cy + "," + cz + "," + nx + "," + ny
 //				+ "," + nz + "," + radius + ")");
 		scene.addCircle(new Circle(cx, cy, cz, nx, ny, nz, radius,
 				new Material(appearance.getColor(), appearance.getShininess(),
 						appearance.getAlpha())));
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addSegment(double, double, double, double, double, double, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addSegment(double x1, double y1, double z1, double x2,
 			double y2, double z2, AppearanceState appearance) {
 //		Util.getLogger().info("addSegment(" + x1 + "," + y1 + "," + z1 + "," + x2 + "," + y2
 //				+ "," + z2 + ")");
 		
 		addPoint(x1, y1, z1, appearance);
 		addPoint(x2, y2, z2, appearance);
 		
 		scene.addLine(new Line(x1, y1, z1, x2, y2, z2, appearance.getSize()
 				* POINT_SCALE, LineType.SEGMENT, new Material(appearance
 				.getColor(), appearance.getShininess(), 1)));
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addLine(double, double, double, double, double, double, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addLine(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance) {
 //		Util.getLogger().info("addLine(" + x1 + "," + y1 + "," + z1 + "," + x2 + "," + y2
 //				+ "," + z2 + ")");
 		scene.addLine(new Line(x1, y1, z1, x2, y2, z2, appearance.getSize()
 				* POINT_SCALE, LineType.LINE, new Material(appearance
 				.getColor(), appearance.getShininess(), 1)));
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addRay(double, double, double, double, double, double, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addRay(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance) {
 //		Util.getLogger().info("addRay(" + x1 + "," + y1 + "," + z1 + "," + x2 + "," + y2
 //				+ "," + z2 + ")");
 		
 		addPoint(x1, y1, z1, appearance);
 		
 		scene.addLine(new Line(x1, y1, z1, x2, y2, z2, appearance.getSize()
 				* POINT_SCALE, LineType.RAY, new Material(
 				appearance.getColor(), appearance.getShininess(), 1)));
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addPolygon(double[][], double[][], de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addPolygon(double[][] vertices, double[][] normals,
 			AppearanceState appearance) {
 		scene.addPolygon(new Polygon(vertices, normals, new Material(appearance
 				.getColor(), appearance.getShininess(), appearance.getAlpha())));
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addLineStrip(double[][], de.tum.in.cindy3dplugin.AppearanceState, boolean)
 	 */
 	@Override
 	public void addLineStrip(double[][] vertices, AppearanceState appearance,
 			boolean closed) {
 		for (int i = 1; i < vertices.length; ++i) {
 			scene.addLine(new Line(vertices[i - 1][0], vertices[i - 1][1],
 					vertices[i - 1][2], vertices[i][0], vertices[i][1],
 					vertices[i][2], appearance.getSize() * POINT_SCALE,
 					LineType.SEGMENT, new Material(appearance.getColor(),
 							appearance.getShininess(), 1)));
 			scene.addSphere(new Sphere(vertices[i][0], vertices[i][1],
 					vertices[i][2], appearance.getSize() * POINT_SCALE,
 					new Material(appearance.getColor(), appearance
 							.getShininess(), 1)));
 		}
 		scene.addSphere(new Sphere(vertices[0][0], vertices[0][1],
 				vertices[0][2], appearance.getSize() * POINT_SCALE,
 				new Material(appearance.getColor(), appearance.getShininess(),
 						1)));
 		if (closed) {
 			scene.addLine(new Line(vertices[vertices.length - 1][0],
 					vertices[vertices.length - 1][1],
 					vertices[vertices.length - 1][2], vertices[0][0],
 					vertices[0][1], vertices[0][2], appearance.getSize()
 							* POINT_SCALE, LineType.SEGMENT,
 					new Material(appearance.getColor(), appearance
 							.getShininess(), 1)));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addMesh(int, int, double[][], double[][], de.tum.in.cindy3dplugin.Cindy3DViewer.MeshTopology, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addMesh(int rows, int columns, double[][] vertices,
 			double[][] normals, MeshTopology topology,
 			AppearanceState appearance) {
 		scene.addMesh(new Mesh(rows, columns, vertices, normals, topology,
 				new Material(appearance.getColor(), appearance.getShininess(),
 						appearance.getAlpha())));
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addMesh(int, int, double[][], de.tum.in.cindy3dplugin.Cindy3DViewer.NormalType, de.tum.in.cindy3dplugin.Cindy3DViewer.MeshTopology, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addMesh(int rows, int columns, double[][] vertices,
 			NormalType normalType, MeshTopology topology,
 			AppearanceState appearance) {
 		scene.addMesh(new Mesh(rows, columns, vertices, normalType, topology,
 				new Material(appearance.getColor(), appearance.getShininess(),
 						appearance.getAlpha())));
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#addSphere(double, double, double, double, de.tum.in.cindy3dplugin.AppearanceState)
 	 */
 	@Override
 	public void addSphere(double cx, double cy, double cz, double radius,
 			AppearanceState appearance)	{
 		scene.addSphere(new Sphere(cx, cy, cz, radius, new Material(appearance
 				.getColor(), appearance.getShininess(), appearance.getAlpha())));
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mousePressed(MouseEvent e) {
 		mousePosition[0] = e.getX();
 		mousePosition[1] = e.getY();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
 			if (e.isControlDown()) {
 				camera.mousePan(e.getX() - mousePosition[0],
 						e.getY() - mousePosition[1]);
 			} else {
 				camera.mouseRotate(e.getX() - mousePosition[0],
 						e.getY() - mousePosition[1]);
 			}
 			drawLater();
 		} else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
 			drawLater();
 		}
 
 		mousePosition[0] = e.getX();
 		mousePosition[1] = e.getY();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
 	 */
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		camera.mouseWheel(e.getWheelRotation());
 		drawLater();
 	}
 	
 	private void drawLater() {
 		if (!drawPending) {
 			drawPending = true;
 			EventQueue.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					canvas.display();
 					drawPending = false;
 				}
 			});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#setDepthRange(double, double)
 	 */
 	@Override
 	public void setDepthRange(double near, double far) {
 		camera.setPerspective(camera.getFieldOfView(), canvas.getWidth(),
 				canvas.getHeight(), near, far);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#setLight(int, de.tum.in.cindy3dplugin.LightInfo)
 	 */
 	@Override
 	public void setLight(int light, LightModificationInfo info) {
 		lightManager.setLight(light, info);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#disableLight(int)
 	 */
 	@Override
 	public void disableLight(int light) {
 		lightManager.disableLight(light);
 	}
 
 	/**
 	 * Supported hints:
 	 * - quality, range [0,8]
 	 * - renderMode, "simple" or "raycasted"
 	 * - samplingRate, range [1,oo[
 	 * 
 	 * quality selects from a fixed set of render hints, which can be modified
 	 * by specifying renderMode and samplingRate. Providing a valid render mode
 	 * resets the sampling rate to 1. 
 	 */
 	/* (non-Javadoc)
 	 * @see de.tum.in.cindy3dplugin.Cindy3DViewer#setRenderHints(java.util.Hashtable)
 	 */
 	@Override
 	public void setRenderHints(Hashtable<String, Object> hints) {
 		requestedRenderHints = renderHints.clone();
 
 		Object value;
 
 		value = hints.get("quality");
 		if (value instanceof Double) {
 			int quality = ((Double) value).intValue();
 			quality = Math.max(0, Math.min(quality, qualityHints.length - 1));
 			requestedRenderHints = qualityHints[quality];
 		}
 		
 		value = hints.get("screenError");
 		if (value instanceof Double) {
 			requestedRenderHints.setAllowedScreenSpaceError((Double)value);
 		}
 
 		value = hints.get("renderMode");
 		if (value instanceof String) {
 			String renderMode = (String) value;
 			if (renderMode.equalsIgnoreCase("simple")) {
 				requestedRenderHints.setSamplingRate(1);
 				requestedRenderHints
 						.setRenderMode(RenderMode.FIXED_FUNCTION_PIPELINE);
 			} else if (renderMode.equalsIgnoreCase("raycasted")) {
 				requestedRenderHints.setSamplingRate(1);
 				requestedRenderHints
 						.setRenderMode(RenderMode.PROGRAMMABLE_PIPELINE);
 			}
 		}
 
 		value = hints.get("samplingRate");
 		if (value instanceof Double) {
 			requestedRenderHints.setSamplingRate(((Double) value).intValue());
 		}
 	}
 }
