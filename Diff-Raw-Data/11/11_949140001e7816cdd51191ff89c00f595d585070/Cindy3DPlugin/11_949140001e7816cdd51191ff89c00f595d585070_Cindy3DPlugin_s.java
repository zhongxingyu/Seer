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
 import de.tum.in.cindy3dplugin.LightInfo.LightFrame;
 import de.tum.in.cindy3dplugin.LightInfo.LightType;
 import de.tum.in.cindy3dplugin.jogl.JOGLViewer;
 import de.tum.in.cindy3dplugin.jogl.Util;
 
 /**
  * Implementation of the plugin interface
  */
 @SuppressWarnings({"rawtypes", "unchecked"})
 public class Cindy3DPlugin extends CindyScriptPlugin {
 	private Cindy3DViewer cindy3d = null;
 	
 	/**
 	 * Stack of saved point appearances
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
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getAuthor()
 	 */
 	@Override
 	public String getAuthor() {
 		return "Jan Sommer und Matthias Reitinger";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getName()
 	 */
 	@Override
 	public String getName() {
 		return "Cindy3D";
 	}
 
 	/**
 	 * Squares the given number
 	 * 
 	 * @param x
 	 * @return The square of x
 	 */
 	@CindyScript("square")
 	public double square(double x) {
 		return x * x;
 	}
 
 	/**
 	 * Prepares drawing of 3D objects. Must be called before any 3D drawing
 	 * function. TODO: List these functions
 	 */
 	@CindyScript("begin3d")
 	public void begin3d() {
 		cindy3d.begin();
 	}
 
 	/**
 	 * Finalizes the drawing of 3D objects. Displays all objects drawn since the
 	 * last call to <code>begin3d</code>.
 	 */
 	@CindyScript("end3d")
 	public void end3d() {
 		cindy3d.end();
 	}
 
 	/**
 	 * Draws a point.
 	 * 
 	 * @param vec
 	 *            Coordinates of the point
 	 */
 	@CindyScript("draw3d")
 	public void draw3d(ArrayList<Double> vec) {
 		if (vec.size() != 3)
 			return;
 
 		cindy3d.addPoint(vec.get(0), vec.get(1), vec.get(2),
 				applyAppearanceModifiers(pointAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a line.
 	 * 
 	 * @param vec1
 	 *            Coordinates of the first end point
 	 * @param vec2
 	 *            Coordinates of the second end point
 	 */
 	@CindyScript("draw3d")
 	public void draw3d(ArrayList<Double> vec1, ArrayList<Double> vec2) {
 		if (vec1.size() != 3 || vec2.size() != 3)
 			return;
 
 		// Fill in default modifiers
 		Hashtable<String, Object> modifiers = new Hashtable<String, Object>();
 		modifiers.put("type", "Segment");
 
 		// Apply overrides
 		modifiers.putAll(this.modifiers);
 
 		String type = modifiers.get("type").toString();
 
 		AppearanceState appearance = applyAppearanceModifiers(lineAppearance,
 				getModifiers());
 
 		if (type.equalsIgnoreCase("segment")) {
 			cindy3d.addSegment(vec1.get(0), vec1.get(1), vec1.get(2),
 					vec2.get(0), vec2.get(1), vec2.get(2), appearance);
 		} else if (type.equalsIgnoreCase("line")) {
 			cindy3d.addLine(vec1.get(0), vec1.get(1), vec1.get(2), vec2.get(0),
 					vec2.get(1), vec2.get(2), appearance);
 		} else if (type.equalsIgnoreCase("ray")) {
 			cindy3d.addRay(vec1.get(0), vec1.get(1), vec1.get(2), vec2.get(0),
 					vec2.get(1), vec2.get(2), appearance);
 		}
 	}
 	
 	/**
 	 * Connects a list of points by line segments.
 	 * 
 	 * @param points
 	 *            List of points to connect
 	 */
 	@CindyScript("connect3d")
 	public void connect3d(ArrayList<Vec> points) {
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
 	 *            Vertices of the polygon
 	 */
 	@CindyScript("drawpoly3d")
 	public void drawpoly3d(ArrayList<Vec> points) {
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
 	 * Draws a polygon
 	 * 
 	 * @param points
 	 *            Vertices of the polygon
 	 */
 	@CindyScript("fillpoly3d")
 	public void fillpoly3d(ArrayList<Vec> points) {
 		fillpoly3d(points, null);
 	}
 	
 	/**
 	 * Draws a polygon with specifying vertex normals
 	 * 
 	 * @param points
 	 *            Vertices of the polygon
 	 * @param normals
 	 *            Normals for each vertex
 	 */
 	@CindyScript("fillpoly3d")
 	public void fillpoly3d(ArrayList<Vec> points, ArrayList<Vec> normals) {
 		if (points.size() == 0
 				|| (normals != null && points.size() != normals.size()))
 			return;
 
 		double vertices[][] = new double[points.size()][3];
 		double normal[][] = (normals == null) ? null : new double[normals
 				.size()][3];
 
 		for (int i = 0; i < points.size(); ++i) {
 			vertices[i][0] = points.get(i).getXR();
 			vertices[i][1] = points.get(i).getYR();
 			vertices[i][2] = points.get(i).getZR();
 			if (normals != null) {
 				normal[i][0] = normals.get(i).getXR();
 				normal[i][1] = normals.get(i).getYR();
 				normal[i][2] = normals.get(i).getZR();
 			}
 		}
 		cindy3d.addPolygon(vertices, normal,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 	
 	/**
 	 * Draws a filled circle.
 	 * 
 	 * @param center
 	 *            Center of the circle
 	 * @param normal
 	 *            Normal vector (orthogonal to the circle)
 	 * @param radius
 	 *            Radius of the circle
 	 */
 	@CindyScript("fillcircle3d")
 	public void fillcircle3d(ArrayList<Double> center,
 			ArrayList<Double> normal, double radius) {
 		if (center.size() != 3 || normal.size() != 3)
 			return;
 		cindy3d.addCircle(center.get(0), center.get(1), center.get(2),
 				normal.get(0), normal.get(1), normal.get(2), radius,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 	
 	/**
 	 * Draws a grid based mesh.
	 *
	 * @note Normals are generated automatically according to the modifier
	 *       "normaltype". "perFace" is the default and generates one normal per
	 *       grid cell. "perVertex" assigns a separate normal to each grid point
	 *       by averaging the normals of the incident grid cells.
 	 * @param rows
 	 *            Number of rows of vertices
 	 * @param columns
 	 *            Number of columns of vertices
 	 * @param points
 	 *            Vertex positions, in row-major order
 	 */
 	@CindyScript("mesh3d")
 	public void mesh3d(int rows, int columns, ArrayList<Vec> points) {
 		if (rows < 0 || columns < 0 || rows * columns != points.size())
 			return;
 
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
 		} else if (topologyStr.equalsIgnoreCase("closex")) {
 			topology = MeshTopology.CLOSE_X;
 		} else if (topologyStr.equalsIgnoreCase("closey")) {
 			topology = MeshTopology.CLOSE_Y;
 		} else if (topologyStr.equalsIgnoreCase("closexy")) {
 			topology = MeshTopology.CLOSE_XY;
 		}
 
 		cindy3d.addMesh(rows, columns, vertices, normalType, topology,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 	
 	/**
 	 * Draws a grid based mesh with custom normals.
 	 * 
 	 * @param rows
 	 *            Number of rows of vertices
 	 * @param columns
 	 *            Number of columns of vertices
 	 * @param points
 	 *            Vertex positions, in row-major order
 	 * @param normals
 	 *            Vertex normals, in row-major order
 	 */
 	@CindyScript("mesh3d")
 	public void mesh3d(int rows, int columns, ArrayList<Vec> points,
 			ArrayList<Vec> normals) {
 		if (rows < 0 || columns < 0 || rows * columns != points.size()
 				|| rows * columns != normals.size())
 			return;
 
 		double[][] vertices = new double[points.size()][3];
 		double[][] perVertex = new double[normals.size()][3];
 		for (int i = 0; i < points.size(); ++i) {
 			Vec v = points.get(i);
 			vertices[i][0] = v.getXR();
 			vertices[i][1] = v.getYR();
 			vertices[i][2] = v.getZR();
 			v = normals.get(i);
 			perVertex[i][0] = v.getXR();
 			perVertex[i][1] = v.getYR();
 			perVertex[i][2] = v.getZR();
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
 		} else if (topologyStr.equalsIgnoreCase("closex")) {
 			topology = MeshTopology.CLOSE_X;
 		} else if (topologyStr.equalsIgnoreCase("closey")) {
 			topology = MeshTopology.CLOSE_Y;
 		} else if (topologyStr.equalsIgnoreCase("closexy")) {
 			topology = MeshTopology.CLOSE_XY;
 		}
 
 		cindy3d.addMesh(rows, columns, vertices, perVertex, topology,
 				applyAppearanceModifiers(surfaceAppearance, getModifiers()));
 	}
 
 	/**
 	 * Draws a sphere.
 	 * 
 	 * @param center
 	 *            Center of the sphere
 	 * @param radius
 	 *            Radius of the sphere
 	 */
 	@CindyScript("drawsphere3d")
 	public void sphere3d(ArrayList<Double> center, double radius) {
 		if (center.size() != 3)
 			return;
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
 		pointAppearanceStack.push(new AppearanceState(pointAppearance));
 		lineAppearanceStack.push(new AppearanceState(lineAppearance));
 		surfaceAppearanceStack.push(new AppearanceState(surfaceAppearance));
 	}
 
 	/**
 	 * Removes the top element of the appearance stack and replaces the current
 	 * appearance with it.
 	 * 
 	 * @see Cindy3DPlugin#gsave3d()
 	 */
 	@CindyScript("grestore3d")
 	public void grestore3d() {
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
 	 * @param vec
 	 *            Color vector
 	 */
 	@CindyScript("color3d")
 	public void color3d(ArrayList<Double> vec) {
 		setColorState(pointAppearance, vec);
 		setColorState(lineAppearance, vec);
 		setColorState(surfaceAppearance, vec);
 	}
 	
 	/**
 	 * Sets the color of the point appearance.
 	 * 
 	 * @param vec
 	 *            Color vector
 	 */
 	@CindyScript("pointcolor3d")
 	public void pointcolor3d(ArrayList<Double> vec) {
 		setColorState(pointAppearance, vec);
 	}
 
 	/**
 	 * Sets the color of the line appearance.
 	 * 
 	 * @param vec
 	 *            Color vector
 	 */
 	@CindyScript("linecolor3d")
 	public void linecolor3d(ArrayList<Double> vec) {
 		setColorState(lineAppearance, vec);
 	}
 
 	/**
 	 * Sets the color of the surface appearance.
 	 * 
 	 * @param vec
 	 *            Color vector
 	 */
 	@CindyScript("surfacecolor3d")
 	public void surfacecolor3d(ArrayList<Double> vec) {
 		setColorState(surfaceAppearance, vec);
 	}
 
 	/**
 	 * Sets the alpha value of all appearances.
 	 * 
 	 * @param alpha
 	 *            Alpha value between 0 and 1
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
 	 *            Alpha value between 0 and 1
 	 */
 	@CindyScript("surfacealpha3d")
 	public void surfacealpha3d(double alpha) {
 		surfaceAppearance.setAlpha(Math.max(0, Math.min(1, alpha)));
 	}
 
 	/**
 	 * Sets the shininess of all appearances.
 	 * 
 	 * @param shininess
 	 *            Shininess between 0 and 128
 	 */
 	@CindyScript("shininess3d")
 	public void shininess3d(int shininess) {
 		shininess = Math.max(0, Math.min(128, shininess));
 		pointAppearance.setShininess(shininess);
 		lineAppearance.setShininess(shininess);
 		surfaceAppearance.setShininess(shininess);
 	}
 
 	/**
 	 * Sets the shininess of the point appearance.
 	 * 
 	 * @param shininess
 	 *            Shininess between 0 and 128
 	 */
 	@CindyScript("pointshininess3d")
 	public void pointshininess3d(int shininess) {
 		pointAppearance.setShininess(Math.max(0, Math.min(128, shininess)));
 	}
 
 	/**
 	 * Sets the shininess of the line appearance.
 	 * 
 	 * @param shininess
 	 *            Shininess between 0 and 128
 	 */
 	@CindyScript("lineshininess3d")
 	public void lineshininess3d(int shininess) {
 		lineAppearance.setShininess(Math.max(0, Math.min(128, shininess)));
 	}
 
 	/**
 	 * Sets the shininess of the surface appearance.
 	 * 
 	 * @param shininess
 	 *            Shininess between 0 and 128
 	 */
 	@CindyScript("surfaceshininess3d")
 	public void surfaceshininess3d(int shininess) {
 		surfaceAppearance.setShininess(Math.max(0, Math.min(128, shininess)));
 	}
 
 	/**
 	 * Sets the size of all appearances.
 	 * 
 	 * @param size
 	 *            Size
 	 */
 	@CindyScript("size3d")
 	public void size3d(double size) {
 		if (size <= 0)
 			return;
 		pointAppearance.setSize(size);
 		lineAppearance.setSize(size);
 		surfaceAppearance.setSize(size);
 	}
 
 	/**
 	 * Sets the size of the point appearance.
 	 * 
 	 * @param size
 	 *            Point size
 	 */
 	@CindyScript("pointsize3d")
 	public void pointsize3d(double size) {
 		pointAppearance.setSize(Math.max(0, size));
 	}
 
 	/**
 	 * Sets the size of the line appearance.
 	 * 
 	 * @param size
 	 *            Line size
 	 */
 	@CindyScript("linesize3d")
 	public void linesize3d(double size) {
 		lineAppearance.setSize(Math.max(0, size));
 	}
 
 	/**
 	 * Sets the background color.
 	 * 
 	 * @param vec
 	 *            Background color vector
 	 */
 	@CindyScript("background3d")
 	public void background3d(ArrayList<Double> vec) {
 		if (vec.size() != 3)
 			return;
 		cindy3d.setBackgroundColor(Util.toColor(vec));
 	}
 	
 	/**
 	 * Sets the camera's depth range.
 	 * 
 	 * @param near
 	 *            Near distance. All objects with distance below
 	 *            <code>near</code> are not displayed.
 	 * @param far
 	 *            Far distance. All objects with distance above <code>far</code>
 	 *            are not displayed.
 	 */
 	@CindyScript("depthrange3d")
 	public void depthrange3d(double near, double far) {
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
 	 * @param light Light index (between 0 and 7)
 	 */
 	@CindyScript("disablelight3d")
 	public void disablelight3d(int light) {
 		if (light < 0 || light >= Cindy3DViewer.MAX_LIGHTS) {
 			return;
 		}
 		
 		cindy3d.disableLight(light);
 	}
 	
 	/**
 	 * Sets parameters for a point light.
 	 * 
 	 * @param light
 	 *            Light index (between 0 and 7)
 	 */
 	@CindyScript("pointlight3d")
 	public void pointlight3d(int light) {
 		if (light < 0 || light >= Cindy3DViewer.MAX_LIGHTS) {
 			return;
 		}
 		cindy3d.setLight(light,
 				getLightInfoFromModifiers(LightType.POINT_LIGHT, modifiers));
 	}
 
 	/**
 	 * Sets parameters for a directional light.
 	 * 
 	 * @param light
 	 *            Light index (between 0 and 7)
 	 */
 	@CindyScript("directionallight3d")
 	public void directionallight3d(int light) {
 		if (light < 0 || light >= Cindy3DViewer.MAX_LIGHTS) {
 			return;
 		}
 		cindy3d.setLight(
 				light,
 				getLightInfoFromModifiers(LightType.DIRECTIONAL_LIGHT,
 						modifiers));
 	}
 
 	/**
 	 * Apply modifiers to a appearance state.
 	 * 
 	 * @param initialState
 	 *            Appearance to modify
 	 * @param modifiers
 	 *            Modifiers to apply. Recognized modifiers are "color", "size",
 	 *            "alpha", and "shininess".
 	 * @return Modified appearance
 	 */
 	private static AppearanceState applyAppearanceModifiers(
 			AppearanceState initialState, Hashtable modifiers) {
 		AppearanceState result = new AppearanceState(initialState);
 		Object value = null;
 		value = modifiers.get("color");
 		if (value instanceof double[]) {
 			setColorState(result, (double[]) value);
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
 	 *            Light type
 	 * @param modifiers
 	 *            Modifiers
 	 * @return Generated instance of <code>LightInfo</code>
 	 */
 	private static LightInfo getLightInfoFromModifiers(LightType type,
 			Hashtable modifiers) {
 		LightInfo info = new LightInfo();
 
 		info.type = type;
 
 		Object value;
 
 		value = modifiers.get("ambient");
 		if (value instanceof double[]) {
 			info.ambient = Util.toColor((double[]) value);
 		}
 
 		value = modifiers.get("diffuse");
 		if (value instanceof double[]) {
 			info.diffuse = Util.toColor((double[]) value);
 		}
 
 		value = modifiers.get("specular");
 		if (value instanceof double[]) {
 			info.specular = Util.toColor((double[]) value);
 		}
 
 		value = modifiers.get("position");
 		if (value instanceof double[]) {
 			info.position = (double[]) value;
 			if (info.position.length != 3) {
 				info.position = null;
 			}
 		}
 
 		value = modifiers.get("direction");
 		if (value instanceof double[]) {
 			info.direction = (double[]) value;
 			if (info.direction.length != 3) {
 				info.direction = null;
 			}
 		}
 
 		value = modifiers.get("frame");
 		if (value instanceof String) {
 			String frame = (String) value;
 			if (frame.equalsIgnoreCase("world")) {
 				info.frame = LightFrame.WORLD;
 			} else if (frame.equalsIgnoreCase("camera")) {
 				info.frame = LightFrame.CAMERA;
 			}
 		}
 
 		return info;
 	}
 
 	/**
 	 * Sets the color state of an appearance.
 	 * 
 	 * @param appearance
 	 *            Appearance
 	 * @param vec
 	 *            Color vector
 	 */
 	private static void setColorState(AppearanceState appearance,
 			ArrayList<Double> vec) {
 		if (vec.size() != 3)
 			return;
 		appearance.setColor(Util.toColor(vec));
 	}
 
 	/**
 	 * Sets the color state of an appearance.
 	 * 
 	 * @param appearance
 	 *            Appearance
 	 * @param vec
 	 *            Color vector
 	 */
 	private static void setColorState(AppearanceState appearance, double[] vec) {
 		if (vec.length != 3)
 			return;
 		appearance.setColor(Util.toColor(vec));
 	}
 }
