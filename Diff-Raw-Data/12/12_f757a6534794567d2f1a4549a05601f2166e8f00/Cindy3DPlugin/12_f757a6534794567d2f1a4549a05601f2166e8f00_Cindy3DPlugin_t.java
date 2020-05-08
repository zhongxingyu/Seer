 package de.tum.in.cindy3dplugin;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Stack;
 
 import de.cinderella.api.cs.CindyScript;
 import de.cinderella.api.cs.CindyScriptPlugin;
 import de.cinderella.math.Vec;
 import de.tum.in.cindy3dplugin.Cindy3DViewer.MeshTopology;
 import de.tum.in.cindy3dplugin.Cindy3DViewer.NormalType;
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightFrame;
 import de.tum.in.cindy3dplugin.LightModificationInfo.LightType;
 import de.tum.in.cindy3dplugin.jogl.JOGLViewer;
 
 /**
  * Implementation of the plugin interface.
  * 
  * This class is responsible for
  * <ul>
  * <li>communicating with Cinderella by providing CindyScript methods and plugin
  * callbacks,
  * <li>handling the appearance states and appearance stack,
  * <li>managing the life cycle of a single {@link Cindy3DViewer} instance, and
  * <li>forwarding CindyScript calls to the {@link Cindy3DViewer} instance,
  * translating between the interfaces
  * </ul>
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class Cindy3DPlugin extends CindyScriptPlugin {
 	private Cindy3DViewer cindy3d = null;
 
 	/**
 	 * Stack of saved point appearances
 	 * 
 	 * @see Cindy3DPlugin#gsave3d()
 	 * @see Cindy3DPlugin#grestore3d()
 	 */
 	private Stack<AppearanceState> pointAppearanceStack;
 	/**
 	 * The current point appearance
 	 */
 	private AppearanceState pointAppearance;
 
 	/**
 	 * Stack of saved line appearances
 	 * 
 	 * @see Cindy3DPlugin#gsave3d()
 	 * @see Cindy3DPlugin#grestore3d()
 	 */
 	private Stack<AppearanceState> lineAppearanceStack;
 
 	/**
 	 * The current line appearance
 	 */
 	private AppearanceState lineAppearance;
 
 	/**
 	 * Stack of saved surface appearances
 	 * 
 	 * @see Cindy3DPlugin#gsave3d()
 	 * @see Cindy3DPlugin#grestore3d()
 	 */
 	private Stack<AppearanceState> surfaceAppearanceStack;
 	/**
 	 * The current surface appearance
 	 */
 	private AppearanceState surfaceAppearance;
 
 	/**
 	 * Modifiers for the current CindyScript function call
 	 */
 	private Hashtable modifiers;
 
 	/**
 	 * Creates a new plugin instance
 	 */
 	public Cindy3DPlugin() {
 		pointAppearanceStack = new Stack<AppearanceState>();
 		pointAppearance = new AppearanceState(Color.RED, 60, 1, 1);
 		lineAppearanceStack = new Stack<AppearanceState>();
 		lineAppearance = new AppearanceState(Color.BLUE, 60, 1, 1);
 		surfaceAppearanceStack = new Stack<AppearanceState>();
 		surfaceAppearance = new AppearanceState(Color.GREEN, 60, 1, 1);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#register()
 	 */
 	@Override
 	public void register() {
 		if (cindy3d == null)
 			cindy3d = new JOGLViewer(null);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#unregister()
 	 */
 	@Override
 	public void unregister() {
 		if (cindy3d != null)
 			cindy3d.shutdown();
 		cindy3d = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#setModifiers(java.util.Hashtable)
 	 */
 	@Override
 	public void setModifiers(Hashtable m) {
 		modifiers = m;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getModifiers()
 	 */
 	@Override
 	public Hashtable getModifiers() {
 		return modifiers;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getAuthor()
 	 */
 	@Override
 	public String getAuthor() {
 		return "Jan Sommer and Matthias Reitinger";
 	}
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getName()
 	 */
 	@Override
 	public String getName() {
 		return "Cindy3D";
 	}
 
 	/**
 	 * Prepares drawing of 3D objects.
 	 * 
 	 * Must be called before any 3D drawing function. TODO: List these functions
 	 */
 	@CindyScript("begin3d")
 	public void begin3d() {
 		cindy3d.begin();
 	}
 
 	/**
 	 * Finalizes the drawing of 3D objects. Displays all objects drawn since the
 	 * last call to {@link #begin3d}.
 	 */
 	@CindyScript("end3d")
 	public void end3d() {
 		cindy3d.end();
 	}
 
 	/**
 	 * Draws a point.
 	 * 
 	 * @param point
 	 *            coordinates of the point
 	 * @throws IllegalArgumentException
 	 *             if the size of <code>point</code> is not 3 
 	 */
 	@CindyScript("draw3d")
 	public void draw3d(ArrayList<Double> point) {
 		if (point.size() != 3) {
 			throw new IllegalArgumentException("point size not 3");
 		}
 		
 		cindy3d.addPoint(point.get(0), point.get(1), point.get(2),
 				applyAppearanceModifiers(pointAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a line.
 	 * 
 	 * @param firstPoint
 	 *            coordinates of the first end point
 	 * @param secondPoint
 	 *            coordinates of the second end point
 	 * @throws IllegalArgumentException
 	 *             if the size of <code>firstPoint</code> or
 	 *             <code>secondPoint</code> is not 3
 	 */
 	@CindyScript("draw3d")
 	public void draw3d(ArrayList<Double> firstPoint,
 			ArrayList<Double> secondPoint) {
 		if (firstPoint.size() != 3) {
 			throw new IllegalArgumentException("first point size not 3");
 		}
 		if (secondPoint.size() != 3) {
 			throw new IllegalArgumentException("second point size not 3");
 		}
 
 		// Fill in default modifiers
 		Hashtable<String, Object> modifiers = new Hashtable<String, Object>();
 		modifiers.put("type", "Segment");
 
 		// Apply overrides
 		modifiers.putAll(this.modifiers);
 
 		String type = modifiers.get("type").toString();
 
 		AppearanceState appearance = applyAppearanceModifiers(lineAppearance,
 				getModifiers());
 
 		if (type.equalsIgnoreCase("segment")) {
 			cindy3d.addSegment(firstPoint.get(0), firstPoint.get(1),
 					firstPoint.get(2), secondPoint.get(0), secondPoint.get(1),
 					secondPoint.get(2), appearance);
 		} else if (type.equalsIgnoreCase("line")) {
 			cindy3d.addLine(firstPoint.get(0), firstPoint.get(1),
 					firstPoint.get(2), secondPoint.get(0), secondPoint.get(1),
 					secondPoint.get(2), appearance);
 		} else if (type.equalsIgnoreCase("ray")) {
 			cindy3d.addRay(firstPoint.get(0), firstPoint.get(1),
 					firstPoint.get(2), secondPoint.get(0), secondPoint.get(1),
 					secondPoint.get(2), appearance);
 		}
 	}
 
 	/**
 	 * Connects a list of points by line segments.
 	 * 
 	 * @param points
 	 *            list of points to connect
 	 * @throws IllegalArgumentException
 	 *             if <code>points</code> is empty
 	 */
 	@CindyScript("connect3d")
 	public void connect3d(ArrayList<Vec> points) {
 		if (points.isEmpty()) {
 			throw new IllegalArgumentException("no points");
 		}
 		
 		double vertices[][] = new double[points.size()][3];
 
 		for (int i = 0; i < points.size(); ++i) {
 			vertices[i][0] = points.get(i).getXR();
 			vertices[i][1] = points.get(i).getYR();
 			vertices[i][2] = points.get(i).getZR();
 		}
 
 		cindy3d.addLineStrip(vertices,
 				applyAppearanceModifiers(lineAppearance, getModifiers()), false);
 	}
 
 	/**
 	 * Draws the outline of a polygon.
 	 * 
 	 * @param points
 	 *            vertices of the polygon
 	 * @throws IllegalArgumentException
 	 *             if <code>points</code> is empty
 	 */
 	@CindyScript("drawpoly3d")
 	public void drawpoly3d(ArrayList<Vec> points) {
 		if (points.isEmpty()) {
 			throw new IllegalArgumentException("no vertices");
 		}
 		
 		double vertices[][] = new double[points.size()][3];
 
 		for (int i = 0; i < points.size(); ++i) {
 			vertices[i][0] = points.get(i).getXR();
 			vertices[i][1] = points.get(i).getYR();
 			vertices[i][2] = points.get(i).getZR();
 		}
 
 		cindy3d.addLineStrip(vertices,
 				applyAppearanceModifiers(lineAppearance, getModifiers()), true);
 	}
 
 	/**
 	 * Draws a polygon.
 	 * 
 	 * @param points
 	 *            vertices of the polygon
 	 * @throws IllegalArgumentException
 	 *             if <code>points</code> is empty
 	 */
 	@CindyScript("fillpoly3d")
 	public void fillpoly3d(ArrayList<Vec> points) {
 		if (points.size() == 0) {
 			throw new IllegalArgumentException("no points");
 		}
 
 		double pointsArray[][] = new double[points.size()][3];
 
 		for (int i = 0; i < points.size(); ++i) {
 			pointsArray[i][0] = points.get(i).getXR();
 			pointsArray[i][1] = points.get(i).getYR();
 			pointsArray[i][2] = points.get(i).getZR();
 		}
 
 		cindy3d.addPolygon(pointsArray, null,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a polygon with specifying vertex normals.
 	 * 
 	 * @param points
 	 *            vertices of the polygon
 	 * @param normals
 	 *            normals for each vertex
 	 * @throws IllegalArgumentException
 	 *             if <code>points</code> or <code>normals</code> is empty
 	 * @throws IllegalArgumentException
 	 *             if <code>points</code> and <code>normals</code> have
 	 *             different sizes
 	 */
 	@CindyScript("fillpoly3d")
 	public void fillpoly3d(ArrayList<Vec> points, ArrayList<Vec> normals) {
 		if (points.size() == 0) {
 			throw new IllegalArgumentException("no points");
 		}
 		if (normals.size() == 0) {
 			throw new IllegalArgumentException("no normals");
 		}
 		if (points.size() != normals.size()) {
 			throw new IllegalArgumentException(
 					"points and normals have different sizes");
 		}
 
 		double pointsArray[][] = new double[points.size()][3];
 		double normalsArray[][] = new double[normals.size()][3];
 
 		for (int i = 0; i < points.size(); ++i) {
 			pointsArray[i][0] = points.get(i).getXR();
 			pointsArray[i][1] = points.get(i).getYR();
 			pointsArray[i][2] = points.get(i).getZR();
 			if (normals != null) {
 				normalsArray[i][0] = normals.get(i).getXR();
 				normalsArray[i][1] = normals.get(i).getYR();
 				normalsArray[i][2] = normals.get(i).getZR();
 			}
 		}
 
 		cindy3d.addPolygon(pointsArray, normalsArray,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a filled circle.
 	 * 
 	 * @param center
 	 *            center of the circle
 	 * @param normal
 	 *            normal vector (orthogonal to the circle)
 	 * @param radius
 	 *            radius of the circle
 	 * @throws IllegalArgumentException
 	 *             if the size of <code>center</code> or <code>normal</code> is
 	 *             not 3
 	 * @throws IllegalArgumentException
 	 *             if <code>radius</code> is less than or equal to 0
 	 */
 	@CindyScript("fillcircle3d")
 	public void fillcircle3d(ArrayList<Double> center,
 			ArrayList<Double> normal, double radius) {
 		if (center.size() != 3) {
 			throw new IllegalArgumentException("center size not 3");
 		}
 		if (normal.size() != 3) {
 			throw new IllegalArgumentException("normal size not 3");
 		}
 		if (radius <= 0) {
 			throw new IllegalArgumentException("radius not positive");
 		}
 		cindy3d.addCircle(center.get(0), center.get(1), center.get(2),
 				normal.get(0), normal.get(1), normal.get(2), radius,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a grid based mesh.
 	 * 
 	 * Normals are generated automatically according to the modifier
 	 * "normaltype". "perFace" is the default and generates one normal per grid
 	 * cell. "perVertex" assigns a separate normal to each grid point by
 	 * averaging the normals of the incident grid cells.
 	 * 
 	 * @param rows
 	 *            number of rows of vertices
 	 * @param columns
 	 *            number of columns of vertices
 	 * @param points
 	 *            vertex positions, in row-major order
 	 * @throws IllegalArgumentException
 	 *             if <code>rows</code> or <code>columns</code> is less than or
 	 *             equal to 0
 	 * @throws IllegalArgumentException
 	 *             if the size of <code>points</code> is not
 	 *             <code>rows * columns</code>
 	 */
 	@CindyScript("mesh3d")
 	public void mesh3d(int rows, int columns, ArrayList<Vec> points) {
 		if (rows <= 0) {
 			throw new IllegalArgumentException("row count not positive");
 		}
 		if (columns <= 0) {
 			throw new IllegalArgumentException("row count not positive");
 		}
 		if (rows * columns != points.size()) {
 			throw new IllegalArgumentException("wrong point count");
 		}
 
 		// Fill in default modifiers
 		Hashtable<String, Object> modifiers = new Hashtable<String, Object>();
 		modifiers.put("normaltype", "perface");
 		modifiers.put("topology", "open");
 
 		// Apply overrides
 		modifiers.putAll(this.modifiers);
 
 		String type = modifiers.get("normaltype").toString();
 
 		NormalType normalType = NormalType.PER_FACE;
 		if (type.equalsIgnoreCase("pervertex")) {
 			normalType = NormalType.PER_VERTEX;
 		}
 
 		double[][] vertices = new double[points.size()][3];
 		for (int i = 0; i < points.size(); ++i) {
 			Vec v = points.get(i);
 			vertices[i][0] = v.getXR();
 			vertices[i][1] = v.getYR();
 			vertices[i][2] = v.getZR();
 		}
 
 		String topologyStr = modifiers.get("topology").toString();
 		MeshTopology topology = MeshTopology.OPEN;
 		if (topologyStr.equalsIgnoreCase("open")) {
 			topology = MeshTopology.OPEN;
 		} else if (topologyStr.equalsIgnoreCase("closerows")) {
 			topology = MeshTopology.CLOSE_ROWS;
 		} else if (topologyStr.equalsIgnoreCase("closecolumns")) {
 			topology = MeshTopology.CLOSE_COLUMNS;
 		} else if (topologyStr.equalsIgnoreCase("closeboth")) {
 			topology = MeshTopology.CLOSE_BOTH;
 		}
 
 		cindy3d.addMesh(rows, columns, vertices, normalType, topology,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a grid based mesh with user-supplied normals.
 	 * 
 	 * @param rows
 	 *            number of rows of vertices
 	 * @param columns
 	 *            number of columns of vertices
 	 * @param points
 	 *            vertex positions, in row-major order
 	 * @param normals
 	 *            vertex normals, in row-major order
 	 * @throws IllegalArgumentException
 	 *             if <code>rows</code> or <code>columns</code> is less than or
 	 *             equal to 0
 	 * @throws IllegalArgumentException
 	 *             if the size of <code>points</code> or <code>normals</code> is
 	 *             not <code>rows * columns</code>
 	 */
 	@CindyScript("mesh3d")
 	public void mesh3d(int rows, int columns, ArrayList<Vec> points,
 			ArrayList<Vec> normals) {
 		if (rows <= 0) {
 			throw new IllegalArgumentException("row count not positive");
 		}
 		if (columns <= 0) {
 			throw new IllegalArgumentException("row count not positive");
 		}
 		if (rows * columns != points.size()) {
 			throw new IllegalArgumentException("wrong point count");
 		}
 		if (rows * columns != normals.size()) {
 			throw new IllegalArgumentException("wrong normal count");
 		}
 
 		double[][] pointsArray = new double[points.size()][3];
 		double[][] normalsArray = new double[normals.size()][3];
 		for (int i = 0; i < points.size(); ++i) {
 			Vec v = points.get(i);
 			pointsArray[i][0] = v.getXR();
 			pointsArray[i][1] = v.getYR();
 			pointsArray[i][2] = v.getZR();
 			v = normals.get(i);
 			normalsArray[i][0] = v.getXR();
 			normalsArray[i][1] = v.getYR();
 			normalsArray[i][2] = v.getZR();
 		}
 
 		// Fill in default modifiers
 		Hashtable<String, Object> modifiers = new Hashtable<String, Object>();
 		modifiers.put("topology", "open");
 
 		// Apply overrides
 		modifiers.putAll(this.modifiers);
 
 		String topologyStr = modifiers.get("topology").toString();
 		MeshTopology topology = MeshTopology.OPEN;
 		if (topologyStr.equalsIgnoreCase("open")) {
 			topology = MeshTopology.OPEN;
 		} else if (topologyStr.equalsIgnoreCase("closerows")) {
 			topology = MeshTopology.CLOSE_ROWS;
 		} else if (topologyStr.equalsIgnoreCase("closecolumns")) {
 			topology = MeshTopology.CLOSE_COLUMNS;
 		} else if (topologyStr.equalsIgnoreCase("closeboth")) {
 			topology = MeshTopology.CLOSE_BOTH;
 		}
 
 		cindy3d.addMesh(rows, columns, pointsArray, normalsArray, topology,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a sphere.
 	 * 
 	 * @param center
 	 *            center of the sphere
 	 * @param radius
 	 *            radius of the sphere
 	 * @throws IllegalArgumentException
 	 *             if the size of <code>center</code> is not 3
 	 * @throws IllegalArgumentException
 	 *             if <code>radius</code> is less than or equal to 0
 	 */
 	@CindyScript("drawsphere3d")
 	public void sphere3d(ArrayList<Double> center, double radius) {
 		if (center.size() != 3) {
 			throw new IllegalArgumentException("center size not 3");
 		}
 		if (radius <= 0) {
 			throw new IllegalArgumentException("radius not positive");
 		}
 			
 		cindy3d.addSphere(center.get(0), center.get(1), center.get(2), radius,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 
 	/**
 	 * Pushes the current appearance on the appearance stack.
 	 * 
 	 * @see Cindy3DPlugin#grestore3d()
 	 */
 	@CindyScript("gsave3d")
 	public void gsave3d() {
 		pointAppearanceStack.push(pointAppearance.clone());
 		lineAppearanceStack.push(lineAppearance.clone());
 		surfaceAppearanceStack.push(surfaceAppearance.clone());
 	}
 
 	/**
 	 * Restores the top element of the appearance stack and removes it.
 	 * 
 	 * @throws IllegalStateException
 	 *             if the appearance stack is empty
 	 * @see Cindy3DPlugin#gsave3d()
 	 */
 	@CindyScript("grestore3d")
 	public void grestore3d() {
 		if (pointAppearanceStack.isEmpty() || lineAppearanceStack.isEmpty()
 				|| surfaceAppearanceStack.isEmpty()) {
 			throw new IllegalStateException("appearance stack empty");
 		}
 		
 		if (!pointAppearanceStack.isEmpty())
 			pointAppearance = pointAppearanceStack.pop();
 		if (!lineAppearanceStack.isEmpty())
 			lineAppearance = lineAppearanceStack.pop();
 		if (!surfaceAppearanceStack.isEmpty())
 			surfaceAppearance = surfaceAppearanceStack.pop();
 	}
 
 	/**
 	 * Sets the color of all appearances.
 	 * 
 	 * @param color
 	 *            color vector
 	 * @throws IllegalArgumentException
 	 *             if the length of <code>color</code> is not 3
 	 */
 	@CindyScript("color3d")
 	public void color3d(ArrayList<Double> color) {
 		if (color.size() != 3) {
 			throw new IllegalArgumentException("color size not 3");
 		}
 		
 		pointAppearance.setColor(convertToColor(color));
 		lineAppearance.setColor(convertToColor(color));
 		surfaceAppearance.setColor(convertToColor(color));
 	}
 
 	/**
 	 * Sets the color of the point appearance.
 	 * 
 	 * @param color
 	 *            color vector
 	 * @throws IllegalArgumentException
 	 *             if the length of <code>color</code> is not 3
 	 */
 	@CindyScript("pointcolor3d")
 	public void pointcolor3d(ArrayList<Double> color) {
 		if (color.size() != 3) {
 			throw new IllegalArgumentException("color size not 3");
 		}
 		
 		pointAppearance.setColor(convertToColor(color));
 	}
 
 	/**
 	 * Sets the color of the line appearance.
 	 * 
 	 * @param color
 	 *            color vector
 	 * @throws IllegalArgumentException
 	 *             if the length of <code>color</code> is not 3
 	 */
 	@CindyScript("linecolor3d")
 	public void linecolor3d(ArrayList<Double> color) {
 		if (color.size() != 3) {
 			throw new IllegalArgumentException("color size not 3");
 		}
 		
 		lineAppearance.setColor(convertToColor(color));
 	}
 
 	/**
 	 * Sets the color of the surface appearance.
 	 * 
 	 * @param color
 	 *            color vector
 	 * @throws IllegalArgumentException
 	 *             if the length of <code>color</code> is not 3
 	 */
 	@CindyScript("surfacecolor3d")
 	public void surfacecolor3d(ArrayList<Double> color) {
 		if (color.size() != 3) {
 			throw new IllegalArgumentException("color size not 3");
 		}
 		
 		surfaceAppearance.setColor(convertToColor(color));
 	}
 
 	/**
 	 * Sets the alpha value of all appearances.
 	 * 
 	 * @param alpha
 	 *            alpha value, between 0 and 1
 	 */
 	@CindyScript("alpha3d")
 	public void alpha3d(double alpha) {
 		alpha = Math.max(0, Math.min(1, alpha));
 		surfaceAppearance.setAlpha(alpha);
 	}
 
 	/**
 	 * Sets the alpha value of the surface appearance.
 	 * 
 	 * @param alpha
 	 *            alpha value, between 0 and 1
 	 */
 	@CindyScript("surfacealpha3d")
 	public void surfacealpha3d(double alpha) {
 		surfaceAppearance.setAlpha(Math.max(0, Math.min(1, alpha)));
 	}
 
 	/**
 	 * Sets the shininess of all appearances.
 	 * 
 	 * @param shininess
 	 *            shininess, between 0 and 128
 	 */
 	@CindyScript("shininess3d")
 	public void shininess3d(double shininess) {
 		shininess = Math.max(0, Math.min(128, shininess));
 		pointAppearance.setShininess(shininess);
 		lineAppearance.setShininess(shininess);
 		surfaceAppearance.setShininess(shininess);
 	}
 
 	/**
 	 * Sets the shininess of the point appearance.
 	 * 
 	 * @param shininess
 	 *            shininess, between 0 and 128
 	 */
 	@CindyScript("pointshininess3d")
 	public void pointshininess3d(double shininess) {
 		pointAppearance.setShininess(Math.max(0, Math.min(128, shininess)));
 	}
 
 	/**
 	 * Sets the shininess of the line appearance.
 	 * 
 	 * @param shininess
 	 *            shininess, between 0 and 128
 	 */
 	@CindyScript("lineshininess3d")
 	public void lineshininess3d(double shininess) {
 		lineAppearance.setShininess(Math.max(0, Math.min(128, shininess)));
 	}
 
 	/**
 	 * Sets the shininess of the surface appearance.
 	 * 
 	 * @param shininess
 	 *            shininess, between 0 and 128
 	 */
 	@CindyScript("surfaceshininess3d")
 	public void surfaceshininess3d(double shininess) {
 		surfaceAppearance.setShininess(Math.max(0, Math.min(128, shininess)));
 	}
 
 	/**
 	 * Sets the size of all appearances.
 	 * 
 	 * @param size
 	 *            size
 	 * @throws IllegalArgumentException
 	 *             if <code>size</code> is lower than or equal to 0
 	 */
 	@CindyScript("size3d")
 	public void size3d(double size) {
 		if (size <= 0) {
 			throw new IllegalArgumentException("size not positive");
 		}
 		
 		pointAppearance.setSize(size);
 		lineAppearance.setSize(size);
 		surfaceAppearance.setSize(size);
 	}
 
 	/**
 	 * Sets the size of the point appearance.
 	 * 
 	 * @param size
 	 *            point size
 	 * @throws IllegalArgumentException
 	 *             if <code>size</code> is lower than or equal to 0
 	 */
 	@CindyScript("pointsize3d")
 	public void pointsize3d(double size) {
 		if (size <= 0) {
 			throw new IllegalArgumentException("size not positive");
 		}
 		
 		pointAppearance.setSize(size);
 	}
 
 	/**
 	 * Sets the size of the line appearance.
 	 * 
 	 * @param size
 	 *            line size
 	 * @throws IllegalArgumentException
 	 *             if <code>size</code> is lower than or equal to 0
 	 */
 	@CindyScript("linesize3d")
 	public void linesize3d(double size) {
 		if (size <= 0) {
 			throw new IllegalArgumentException("size not positive");
 		}
 		
 		lineAppearance.setSize(size);
 	}
 
 	/**
 	 * Sets the background color.
 	 * 
 	 * @param color
 	 *            background color vector
 	 * @throws IllegalArgumentException
 	 *             if the length of <code>color</code> is not 3
 	 */
 	@CindyScript("background3d")
 	public void background3d(ArrayList<Double> color) {
 		if (color.size() != 3) {
 			throw new IllegalArgumentException("color size not 3");
 		}
 
 		cindy3d.setBackgroundColor(convertToColor(color));
 	}
 
 	/**
 	 * Sets the camera's depth range.
 	 * 
 	 * All objects with distance below <code>near</code> or above
 	 * <code>far</code> are not displayed.
 	 * 
 	 * @param near
 	 *            near distance
 	 * @param far
 	 *            far distance
 	 * @throws IllegalArgumentException
 	 *             if <code>near</code> or <code>far</code> is less than or
 	 *             equal to 0
 	 * @throws IllegalArgumentException
 	 *             if <code>near</code> is not less than <code>far</code>
 	 */
 	@CindyScript("depthrange3d")
 	public void depthrange3d(double near, double far) {
 		if (near <= 0) {
 			throw new IllegalArgumentException("near distance not positive");
 		}
 		if (far <= 0) {
 			throw new IllegalArgumentException("far distance not positive");
 		}
 		if (near >= far) {
 			throw new IllegalArgumentException(
 					"near distance not smaller than far distance");
 		}
 		
 		cindy3d.setDepthRange(near, far);
 	}
 
 	/**
 	 * Sets render hints.
 	 */
 	@CindyScript("renderhints3d")
 	public void renderhints3d() {
 		cindy3d.setRenderHints(modifiers);
 	}
 
 	/**
 	 * Disables a single light source.
 	 * 
 	 * @param light
 	 *            light index, between 0 (inclusive) and
 	 *            {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *            (exclusive)
 	 * @throws IndexOutOfBoundsException
 	 *             if light index is not between 0 (inclusive) and
 	 *             {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *             (exclusive)
 	 */
 	@CindyScript("disablelight3d")
 	public void disablelight3d(int light) {
 		if (light < 0 || light >= Cindy3DViewer.MAX_LIGHTS) {
 			throw new IndexOutOfBoundsException("light index out of bounds");
 		}
 
 		cindy3d.disableLight(light);
 	}
 
 	/**
 	 * Sets parameters for a point light.
 	 * 
 	 * @param light
 	 *            light index, between 0 (inclusive) and
 	 *            {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *            (exclusive)
 	 * @throws IndexOutOfBoundsException
 	 *             if light index is not between 0 (inclusive) and
 	 *             {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *             (exclusive)
 	 */
 	@CindyScript("pointlight3d")
 	public void pointlight3d(int light) {
 		if (light < 0 || light >= Cindy3DViewer.MAX_LIGHTS) {
 			throw new IndexOutOfBoundsException("light index out of bounds");
 		}
 		cindy3d.setLight(
 				light,
 				getLightModificationInfoFromModifiers(LightType.POINT_LIGHT,
 						modifiers));
 	}
 
 	/**
 	 * Sets parameters for a directional light.
 	 * 
 	 * @param light
 	 *            light index, between 0 (inclusive) and
 	 *            {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *            (exclusive)
 	 * @throws IndexOutOfBoundsException
 	 *             if light index is not between 0 (inclusive) and
 	 *             {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *             (exclusive)
 	 */
 	@CindyScript("directionallight3d")
 	public void directionallight3d(int light) {
 		if (light < 0 || light >= Cindy3DViewer.MAX_LIGHTS) {
 			throw new IndexOutOfBoundsException("light index out of bounds");
 		}
 		cindy3d.setLight(
 				light,
 				getLightModificationInfoFromModifiers(
 						LightType.DIRECTIONAL_LIGHT, modifiers));
 	}
 	
 	/**
 	 * Sets parameters for a spot light.
 	 * 
 	 * @param light
 	 *            light index, between 0 (inclusive) and
 	 *            {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *            (exclusive)
 	 * 
 	 * @throws IndexOutOfBoundsException
 	 *             if light index is not between 0 (inclusive) and
 	 *             {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
 	 *             (exclusive)
 	 */
 	@CindyScript("spotlight3d")
 	public void spotlight3d(int light) {
 		if (light < 0 || light >= Cindy3DViewer.MAX_LIGHTS) {
 			throw new IndexOutOfBoundsException("light index out of bounds");
 		}
 		cindy3d.setLight(
 				light,
 				getLightModificationInfoFromModifiers(LightType.SPOT_LIGHT,
 						modifiers));
 	}
 	
 	/**
 	 * Positions the camera.
 	 * 
 	 * @param eye
 	 *            position of the camera
 	 * @param lookat
 	 *            point the camera is looking at
 	 * @param up
 	 *            up direction of the camera
 	 * @throws IllegalArgumentException
 	 *             if the length any argument is not 3
 	 */
 	@CindyScript("lookat3d")
 	public void lookat3d(ArrayList<Double> eye, ArrayList<Double> lookat,
 			ArrayList<Double> up) {
 		if (eye.size() != 3) {
 			throw new IllegalArgumentException("eye vector size not 3");
 		}
 		if (lookat.size() != 3) {
 			throw new IllegalArgumentException("lookat vector size not 3");
 		}
 		if (up.size() != 3) {
 			throw new IllegalArgumentException("up vector size not 3");
 		}
 		cindy3d.setCamera(eye.get(0), eye.get(1), eye.get(2), lookat.get(0),
 				lookat.get(1), lookat.get(2), up.get(0), up.get(1), up.get(2));
 	}
 	
 	/**
 	 * Sets the camera's field of view.
 	 * 
 	 * @param fieldOfView
	 *            the field of view to set, in radians
 	 * @throws IllegalArgumentException
 	 *             if the field of view is not between 0 and 180 (exclusive)
 	 */
 	@CindyScript("fieldofview3d")
 	public void fieldofview3d(double fieldOfView) {
 		if (fieldOfView <= 0 || fieldOfView >= Math.PI) {
 			throw new IllegalArgumentException("field of view out of range");
 		}
		cindy3d.setFieldOfView(Math.toDegrees(fieldOfView));
 	}
 
 	/**
 	 * Creates a Color object from an array of RGB components.
 	 * 
 	 * Components outside the range [0, 1] are clamped.
 	 * 
 	 * @param color
 	 *            RGB components
 	 * @return Color object
 	 * @throws IllegalArgumentException
 	 *             if the length of <code>color</code> is not 3
 	 */
 	private static Color convertToColor(double[] color) {
 		if (color.length != 3) {
 			throw new IllegalArgumentException("color size not 3");
 		}
 		return new Color(
 				(float) Math.max(0, Math.min(1, color[0])),
 				(float) Math.max(0, Math.min(1, color[1])),
 				(float) Math.max(0,	Math.min(1, color[2])));
 	}
 
 	/**
 	 * Creates a Color object from an ArrayList of RGB components.
 	 * 
 	 * Components outside the range [0, 1] are clamped.
 	 * 
 	 * @param color
 	 *            RGB components
 	 * @return Color object
 	 * @throws IllegalArgumentException
 	 *             if the size of <code>color</code> is not 3
 	 */
 	private static Color convertToColor(ArrayList<Double> color) {
 		if (color.size() != 3) {
 			throw new IllegalArgumentException("color size not 3");
 		}
 		return new Color(
 				(float) Math.max(0, Math.min(1, color.get(0))),
 				(float) Math.max(0, Math.min(1, color.get(1))),
 				(float) Math.max(0, Math.min(1, color.get(2))));
 	}
 
 	/**
 	 * Apply modifiers to a appearance state.
 	 * 
 	 * @param initialState
 	 *            appearance to modify
 	 * @param modifiers
 	 *            modifiers to apply. Recognized modifiers are "color", "size",
 	 *            "alpha", and "shininess".
 	 * @return modified appearance
 	 */
 	private static AppearanceState applyAppearanceModifiers(
 			AppearanceState initialState, Hashtable modifiers) {
 		AppearanceState result = initialState.clone();
 		Object value = null;
 		value = modifiers.get("color");
 		if (value instanceof double[]) {
 			result.setColor(convertToColor((double[]) value));
 		}
 		value = modifiers.get("size");
 		if (value instanceof Double) {
 			result.setSize((Double) value);
 		}
 		value = modifiers.get("alpha");
 		if (value instanceof Double) {
 			double alpha = Math.max(0, Math.min(1, (Double) value));
 			result.setAlpha(alpha);
 		}
 		value = modifiers.get("shininess");
 		if (value instanceof Double) {
 			double shininess = Math.max(0, Math.min(128, (Double) value));
 			result.setShininess((int) shininess);
 		}
 		return result;
 	}
 
 	/**
 	 * Translates modifiers to a <code>LightInfo</code> instance.
 	 * 
 	 * @param type
 	 *            light type
 	 * @param modifiers
 	 *            modifiers
 	 * @return generated instance of <code>LightInfo</code>
 	 */
 	private static LightModificationInfo getLightModificationInfoFromModifiers(
 			LightType type, Hashtable modifiers) {
 		LightModificationInfo info = new LightModificationInfo(type);
 
 		Object value;
 
 		value = modifiers.get("ambient");
 		if (value instanceof double[]) {
 			info.setAmbient(convertToColor((double[]) value));
 		}
 
 		value = modifiers.get("diffuse");
 		if (value instanceof double[]) {
 			info.setDiffuse(convertToColor((double[]) value));
 		}
 
 		value = modifiers.get("specular");
 		if (value instanceof double[]) {
 			info.setSpecular(convertToColor((double[]) value));
 		}
 
 		value = modifiers.get("position");
 		if (value instanceof double[]) {
 			double[] position = (double[]) value;
 			if (position.length == 3) {
 				info.setPosition(position);
 			}
 		}
 
 		value = modifiers.get("direction");
 		if (value instanceof double[]) {
 			double[] direction = (double[]) value;
 			if (direction.length == 3) {
 				info.setDirection(direction);
 			}
 		}
 		
 		value = modifiers.get("cutoffangle");
 		if (value instanceof Double) {
 			Double cutoffangle = (Double) value;
 			info.setCutoffAngle(cutoffangle);
 		}
 		
 		value = modifiers.get("exponent");
 		if (value instanceof Double) {
 			Double exponent = (Double)value;
 			info.setSpotExponent(exponent);
 		}
 
 		value = modifiers.get("frame");
 		if (value instanceof String) {
 			String frame = (String) value;
 			if (frame.equalsIgnoreCase("world")) {
 				info.setFrame(LightFrame.WORLD);
 			} else if (frame.equalsIgnoreCase("camera")) {
 				info.setFrame(LightFrame.CAMERA);
 			}
 		}
 
 		return info;
 	}
 }
